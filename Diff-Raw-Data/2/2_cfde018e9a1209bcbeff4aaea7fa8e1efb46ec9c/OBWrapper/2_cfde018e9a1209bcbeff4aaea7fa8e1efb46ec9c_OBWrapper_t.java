 package babel;
 
 import io.ExternalTool;
 import io.Logger;
 import io.SDFUtil;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import util.ArrayUtil;
 import util.FileUtil;
 import util.StringUtil;
 
 public class OBWrapper
 {
 	public static interface Aborter
 	{
 		public boolean abort();
 	}
 
 	ExternalTool ext;
 	String babelPath;
 	private Logger logger;
 	private String oBabelPath;
 
 	public OBWrapper(String babelPath, String oBabelPath, Logger logger)
 	{
 		this.babelPath = babelPath;
 		this.oBabelPath = oBabelPath;
 		this.logger = logger;
 		ext = new ExternalTool(logger);
 	}
 
 	private HashMap<String, String> version = new HashMap<String, String>();
 
 	public String getVersion()
 	{
 		if (!version.containsKey(babelPath))
 		{
 			try
 			{
 				String v = ext.get("babel", new String[] { babelPath, "-V" });
 				Pattern pattern = Pattern.compile("^.*([0-9]+\\.[0-9]+\\.[0-9]+).*$");
 				for (String s : v.split("\n"))
 				{
 					Matcher matcher = pattern.matcher(s);
 					if (matcher.matches())
 					{
 						version.put(babelPath, matcher.group(1));
 						break;
 					}
 				}
 			}
 			catch (Exception e)
 			{
 				throw new Error(e);
 			}
 		}
 		return version.get(babelPath);
 	}
 
 	public String[] computeInchiFromSmiles(String[] smiles)
 	{
 		String inchi[] = new String[smiles.length];
 		for (int i = 0; i < inchi.length; i++)
 			inchi[i] = ext.get("obinchi", new String[] { oBabelPath, "-:" + smiles[i] + "", "-oinchi" });
 		return inchi;
 	}
 
 	public void computeInchiFromSDF(String sdfFile, String outputInchiFile)
 	{
 		System.out.println("computing openbabel inchi, source: " + sdfFile + ", dest: " + outputInchiFile);
 		ext.run("obgen3d", new String[] { babelPath, "-d", "-isdf", sdfFile, "-oinchi", outputInchiFile });
 	}
 
 	private boolean[] compute3D(String cacheDir, String type, String content[], boolean isMixture[],
 			String outputSDFile, String title[], Aborter aborter)
 	{
 		if (new File(outputSDFile).exists())
 			if (!new File(outputSDFile).delete())
 				throw new Error("could not delete already existing file");
 		String extendedCacheDir = cacheDir + File.separator + getVersion() + File.separator + type;
 		int cached = 0;
 		int count = 0;
 		boolean valid[] = new boolean[content.length];
 		for (String mol : content)
 		{
 			String digest = StringUtil.getMD5(mol);
 			String file = extendedCacheDir + File.separator + digest;
 			valid[count] = isMixture == null || !isMixture[count];
 			if (!valid[count] && new File(file).exists())
 				new File(file).delete();
 			if (!new File(file).exists())
 			{
 				try
 				{
 					FileUtil.createParentFolders(file);
 					File tmp = File.createTempFile(type + "file", type);
 					File out = File.createTempFile("sdffile", "sdf");
 					BufferedWriter b = new BufferedWriter(new FileWriter(tmp));
 					b.write(mol + "\n");
 					b.close();
 					String gen3d = "--gen3d";
 					if (isMixture != null && isMixture[count])
 					{
 						logger.warn("babel does not work for mixtures, using gen2d: " + mol);
 						gen3d = "--gen2d";
 					}
 					ext.run("obgen3d", new String[] { babelPath, gen3d, "-d", "-i" + type, tmp.getAbsolutePath(),
 							"-osdf", out.getAbsolutePath() }, null, true, null);
 					if (!FileUtil.robustRenameTo(out.getAbsolutePath(), file))
 						throw new Error("cannot move obresult file");
 				}
 				catch (IOException e)
 				{
 					throw new Error(e);
 				}
 			}
 			else
 			{
 				cached++;
 				System.out.println("3d result cached: " + file);
 			}
 			if (title == null)
 			{
 				boolean merge = FileUtil.concat(new File(outputSDFile), new File(file), true);
 				if (!merge)
 					throw new Error("could not merge to sdf file");
 			}
 			else
 			{
 				String sdf[] = FileUtil.readStringFromFile(file).split("\n");
 				if (sdf[0].length() > 0)
 					throw new Error("already a title " + sdf[0]);
 				sdf[0] = title[count];
 				FileUtil.writeStringToFile(outputSDFile, ArrayUtil.toString(sdf, "\n", "", "", "") + "\n", true);
 			}
 			count++;
 			if (aborter != null && aborter.abort())
 				return null;
 		}
 		System.out.println(cached + "/" + content.length + " compounds were precomputed at '" + extendedCacheDir
 				+ "', merged obgen3d result to: " + outputSDFile);
 		return valid;
 	}
 
 	public boolean[] compute3DfromSDF(String cacheDir, String inputSDFile, boolean isMixture[], String outputSDFile,
 			Aborter aborter)
 	{
 		System.out.println("computing openbabel 3d, source: " + inputSDFile + ", dest: " + outputSDFile);
 		return compute3D(cacheDir, "sdf", SDFUtil.readSdf(inputSDFile), isMixture, outputSDFile, null, aborter);
 	}
 
 	public boolean[] compute3DfromSmiles(String cacheDir, String inputSmilesFile, String outputSDFile, Aborter aborter)
 	{
 		System.out.println("computing openbabel 3d, source: " + inputSmilesFile + ", dest: " + outputSDFile);
 		List<String> content = new ArrayList<String>();
 		List<Boolean> isMixture = new ArrayList<Boolean>();
 		List<String> title = new ArrayList<String>();
 		for (String line : FileUtil.readStringFromFile(inputSmilesFile).split("\n"))
 		{
 			String words[] = line.split("\t");
 			if (words.length < 1 || words.length > 2)
 				throw new Error();
 			content.add(words[0]);
			isMixture.add(words[0].contains("."));
 			if (words.length > 1)
 				title.add(words[1]);
 			else
 				title.add(null);
 		}
 		String[] titles = ArrayUtil.toArray(String.class, title);
 		if (ArrayUtil.removeNullValues(titles).size() == 0)
 			titles = null;
 		return compute3D(cacheDir, "smi", ArrayUtil.toArray(content), ArrayUtil.toPrimitiveBooleanArray(isMixture),
 				outputSDFile, titles, aborter);
 	}
 
 	public boolean[] compute3DfromSmiles(String cacheDir, String smiles[], String outputSDFile, Aborter aborter)
 	{
 		System.out.println("computing openbabel 3d, source is smiles-array, dest: " + outputSDFile);
 		boolean[] isMixture = new boolean[smiles.length];
 		for (int i = 0; i < isMixture.length; i++)
 			isMixture[i] = smiles[i].contains(".");
 		return compute3D(cacheDir, "smi", smiles, isMixture, outputSDFile, null, aborter);
 	}
 
 	public List<boolean[]> matchSmarts(List<String> smarts, int numCompounds, String sdfFile, String newFP,
 			String newBabelDataDir)
 	{
 		List<boolean[]> l = new ArrayList<boolean[]>();
 		for (int i = 0; i < smarts.size(); i++)
 			l.add(new boolean[numCompounds]);
 
 		File tmp = null;
 		try
 		{
 			tmp = File.createTempFile("sdf" + numCompounds, "OBsmarts");
 			String cmd[] = { babelPath, "-isdf", sdfFile, "-ofpt", "-xf", newFP, "-xs" };
 			logger.debug("Running babel: " + ArrayUtil.toString(cmd, " ", "", ""));
 			ext.run("ob-fingerprints", cmd, tmp, true, new String[] { "BABEL_DATADIR=" + newBabelDataDir });
 			logger.debug("Parsing smarts");
 			BufferedReader buffy = new BufferedReader(new FileReader(tmp));
 			String line = null;
 			int compoundIndex = -1;
 			while ((line = buffy.readLine()) != null)
 			{
 				if (line.startsWith(">"))
 				{
 					compoundIndex++;
 					line = line.replaceAll("^>[^\\s]*", "").trim();
 				}
 				if (line.length() > 0)
 				{
 					// Settings.LOGGER.warn("frags: " + line);
 					boolean minFreq = false;
 					for (String s : line.split("\\t"))
 					{
 						if (s.trim().length() == 0)
 							continue;
 						if (minFreq && s.matches("^\\*[2-4].*"))
 							s = s.substring(2);
 						int smartsIndex = Integer.parseInt(s.split(":")[0]);
 						l.get(smartsIndex)[compoundIndex] = true;
 						minFreq = s.matches(".*>(\\s)*[1-3].*");
 					}
 				}
 			}
 		}
 		catch (Exception e)
 		{
 			throw new Error("Error while matching smarts with OpenBabel: " + e.getMessage(), e);
 		}
 		finally
 		{
 			tmp.delete();
 		}
 		return l;
 	}
 
 	public void main(String args[])
 	{
 		try
 		{
 			OBWrapper obwrapper = new OBWrapper("/home/martin/software/openbabel-2.3.1/install/bin/babel",
 					"/home/martin/software/openbabel-2.3.1/install/bin/obabel", null);
 
 			File tmp = File.createTempFile("smiles", "smi");
 			BufferedWriter b = new BufferedWriter(new FileWriter(tmp));
 			b.write("c1cccc1\t123\n");
 			b.write("c1ccnc1\t456\n");
 			b.close();
 
 			obwrapper.compute3DfromSmiles("/tmp/babel3d", tmp.getAbsolutePath(), "/tmp/delme.sdf", null);
 
 			obwrapper.compute3DfromSDF("/tmp/babel3d", "/tmp/delme.sdf", null, "/tmp/delme.too.sdf", null);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 
 	}
 }
