 package util;
 
 import io.ExternalTool;
 import io.Logger;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.List;
 
 import babel.OBSmartsMatcher;
 
 public class FminerWrapper
 {
 	public static void main(String[] args) throws Exception
 	{
 		//		String smiles[] = new String[] { "c1cccc1",
 		//				"COC1=CC=C(C=C1)C2=NC(=C([NH]2)C3=CC=CC=C3)C4=CC=CC=C4",
 		//				"O1[C@H](CO)[C@@H](O)[C@H](O)[C@@H](O)[C@H]1O[C@@]2(O[C@@H]([C@@H](O)[C@@H]2O)CO)CO",
 		//				"OC[C@H]1OC(O)[C@H](O)[C@@H](O)[C@@H]1O", "CN1C=NC2=C1C(=O)N(C(=O)N2C)C",
 		//				"CCCCCC1=CC2=C(C3C=C(CCC3C(O2)(C)C)C)C(=C1)O",
 		//				"O=C4\\C=C2/[C@]([C@H]1CC[C@@]3([C@@H](O)CC[C@H]3[C@@H]1CC2)C)(C)CC4",
 		//				"CN(CCC1)[C@@H]1C2=CC=CN=C2", "C1=CC(=CN=C1)C(=O)O",
 		//				"CN1C2=C(C(C3=CC=CC=C3)=NCC1=O)C=C(Cl)C=C2", "CC(=O)NC1=CC=C(C=C1)O",
 		//				"OC(=O)CC(O)(C(=O)O)CC(=O)O", "OS(=O)(=O)O", "OP(=O)(O)O",
 		//				"C1=CC(=CC=C1C(=O)NC(CCC(=O)O)C(=O)O)NCC2=CN=C3C(=N2)C(=O)N=C(N3)N", "c1cc(c(cc1CCN)O)O" };
 		String content = FileUtil.readStringFromFile("/home/martin/data/beta.smi");
 		List<String> smi = new ArrayList<String>();
 		for (String s : content.split("\n"))
 		{
 			smi.add(s.split("\\s")[1]);
 			if (smi.size() >= 30)
 				break;
 		}
 		String smiles[] = ArrayUtil.toArray(smi);
 
 		new FminerWrapper(new Logger(null, true), "/home/martin/software/fminer2/fminer/fminer",
				"/home/martin/software/fminer2/libbbrc/libbbrc.so", null,
 				"/home/martin/software/openbabel-2.3.1/install/bin/babel",
 				//		"/home/martin/software/fminer2/liblast/liblast.so",
 				smiles, 1);
 	}
 
 	String smarts[];
 	List<boolean[]> smilesHits;
 	List<boolean[]> smartsHits;
 
 	public String[] getSmarts()
 	{
 		return smarts;
 	}
 
 	public boolean[] getHitsForSmarts(int smartsIndex)
 	{
 		return smartsHits.get(smartsIndex);
 	}
 
	public FminerWrapper(Logger logger, String fminerPath, String libPath, String fminerLDPath, String babelPath,
			String[] smiles, int minF) throws Exception
 	{
 		File inputSmiles = null;
 		File inputSmiles2 = null;
 		try
 		{
 			String idPlusSmiles = "";
 			//		String activity = "";
 			for (int i = 0; i < smiles.length; i++)
 			{
 				idPlusSmiles += (i + 1) + "\t" + smiles[i] + "\n";
 				//			activity += (i + 1) + "\tactive\t" + (new Random().nextBoolean() ? "1" : "1") + "\n";
 			}
 
 			inputSmiles = new File("/tmp/input" + System.currentTimeMillis() + fminerPath.hashCode() + ".smi");
 			FileUtil.writeStringToFile(inputSmiles.getAbsolutePath(), idPlusSmiles);
 
 			//		File inputClass = new File("/tmp/input" + System.currentTimeMillis() + fminerPath.hashCode() + ".class");
 			//		FileUtil.writeStringToFile(inputClass.getAbsolutePath(), activity);
 
 			double minRelMinF = 0.01;
 			int minMinF = (int) (smiles.length * minRelMinF);
 			if (minF < minMinF)
 			{
 				System.err.println("relative min frequency for miner is " + minRelMinF
 						+ ", absolute min frequency for this dataset increased to " + minMinF);
 				minF = minMinF;
 			}
 
 			ExternalTool ext = new ExternalTool(logger);
			String[] env = new String[] { "FMINER_SMARTS=1", "FMINER_LAZAR=1" };
			if (fminerLDPath != null)
				env = ArrayUtil.concat(env, new String[] { "LD_LIBRARY_PATH=" + fminerLDPath });
			logger.debug("env for fminer: " + ArrayUtil.toString(env));
 			String out = ext.get("fminer", new String[] { fminerPath, libPath, "-l2", //"-d", "-b", "-u", 
 					//				"-p", "0",//
 					"-f" + minF + "", inputSmiles.getAbsolutePath(),//
 			//				inputClass.getAbsolutePath() //
					}, env);
 
 			// do matching yourself
 			smarts = out.split("\n");
 			int numSmarts = out.split("\n").length;
 			logger.debug("found " + smarts.length + " num fragments");
 			//			if (numSmarts <= 1000)
 			//				System.out.println(ArrayUtil.toString(smarts));
 			String babel = babelPath;
 			String dataDir = "/tmp";
 			inputSmiles2 = new File("/tmp/input" + System.currentTimeMillis() + babel.hashCode() + ".smi");
 			FileUtil.writeStringToFile(inputSmiles2.getAbsolutePath(), ArrayUtil.toString(smiles, "\n", "", "", ""));
 			OBSmartsMatcher ob = new OBSmartsMatcher(babel, dataDir, logger);
 			smartsHits = ob.match(ArrayUtil.toList(smarts), inputSmiles2.getAbsolutePath(), smiles.length);
 			smilesHits = new ArrayList<boolean[]>();
 			for (int i = 0; i < smiles.length; i++)
 				smilesHits.add(new boolean[numSmarts]);
 			for (int i = 0; i < smiles.length; i++)
 				for (int j = 0; j < numSmarts; j++)
 					smilesHits.get(i)[j] = smartsHits.get(j)[i];
 
 			//		int numSmarts = out.split("\n").length;
 			//		smarts = new String[numSmarts];
 			//		smilesHits = new ArrayList<boolean[]>();
 			//		for (int i = 0; i < smiles.length; i++)
 			//			smilesHits.add(new boolean[numSmarts]);
 			//		smartsHits = new ArrayList<boolean[]>();
 			//		for (int i = 0; i < numSmarts; i++)
 			//			smartsHits.add(new boolean[smiles.length]);
 			//
 			//		System.out.println(out);
 			//		System.out.println(numSmarts);
 			//
 			//		int i = 0;
 			//		for (String line : out.split("\n"))
 			//		{
 			//			String smartsAndHits[] = line.split("\t");
 			//			if (smartsAndHits.length != 2)
 			//				throw new Error(line);
 			//			String sma = smartsAndHits[0];
 			//			String hitsStrBr = smartsAndHits[1];
 			//			if (!hitsStrBr.startsWith("[") || !hitsStrBr.endsWith("]"))
 			//				throw new Error("oops : " + line);
 			//			String hitsStr = hitsStrBr.substring(1, hitsStrBr.length() - 1).trim();
 			//
 			//			smarts[i] = sma;
 			//			for (String hit : hitsStr.split(" "))
 			//			{
 			//				int smilesIdx = Integer.parseInt(hit.trim()) - 1;
 			//				smilesHits.get(smilesIdx)[i] = true;
 			//				smartsHits.get(i)[smilesIdx] = true;
 			//			}
 			//			i++;
 			//		}
 			//			System.out.println(smarts.length);
 			//			System.out.println(ArrayUtil.toString(smarts));
 			//		for (int j = 0; j < smiles.length; j++)
 			//			System.out.println(ArrayUtil.toString(smilesHits.get(j)));
 		}
 		finally
 		{
 			if (inputSmiles != null)
 				inputSmiles.delete();
 			if (inputSmiles2 != null)
 				inputSmiles2.delete();
 		}
 	}
 }
