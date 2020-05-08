 import java.io.*;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.*;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.commons.lang3.StringUtils;
 
 public class Normaliser
 {
 	enum Section
 	{
 		None, Header, Expressions, Gauges, Logs, FrontPage, Constants
 	};
 
 	private static Pattern				bits				= Pattern
 																	.compile("(\\w*)\\s*=\\s*bits\\s*,\\s*(.*),\\s*(.*),\\s*\\[(\\d):(.*)\\].*");
 	private static Pattern				scalar				= Pattern
 																	.compile("(\\w*)\\s*=\\s*scalar\\s*,\\s*([U|S]\\d+)\\s*,\\s*(\\d+)\\s*,\\s*(.*,.*)");
 	private static Pattern				expr				= Pattern.compile("(\\w*)\\s*=\\s*\\{\\s*(.*)\\s*\\}.*");
 	private static Pattern				ternary				= Pattern.compile("(.*?)\\?(.*)");
	private static Pattern				log					= Pattern.compile("\\s*entry\\s*=\\s*(\\w+)\\s*,\\s*\"(.*)\",.*");
 	private static Pattern				binary				= Pattern.compile("(.*)0b([01]{8})(.*)");
 	private static Pattern				gauge				= Pattern
 																	.compile("\\s*(.*?)\\s*=\\s*(.*?)\\s*,\\s*\"(.*?)\"\\s*,\\s*\"(.*?)\"\\s*,\\s*(.*?)\\s*,\\s*(.*?)\\s*,\\s*(.*?)\\s*,\\s*(.*?)\\s*,\\s*(.*?)\\s*,\\s*(.*?)\\s*,\\s*(.*?)\\s*,\\s*(.*)");
 
 	private static Pattern				queryCommand		= Pattern.compile("\\s*queryCommand\\s*=\\s*\"(.*)\".*");
 	private static Pattern				signature			= Pattern.compile("\\s*signature\\s*=\\s*\"(.*)\".*");
 	private static Pattern				ochGetCommand		= Pattern.compile("\\s*ochGetCommand\\s*=\\s*\"(.*)\".*");
 	private static Pattern				ochBlockSize		= Pattern.compile("\\s*ochBlockSize\\s*=\\s*(\\d*).*");
 	private static Pattern				pageActivationDelay	= Pattern.compile("\\s*pageActivationDelay\\s*=\\s*(\\d*).*");
 	private static Pattern				blockReadTimeout	= Pattern.compile("\\s*blockReadTimeout\\s*=\\s*(\\d*).*");
 	private static Pattern				defaultGauge		= Pattern.compile("\\s*gauge\\d\\s*=\\s*(\\w*)");
 	private static Pattern				page				= Pattern.compile("\\s*page\\s*=\\s*(\\d*)");
 	// private static Pattern nPages =
 	// Pattern.compile("\\s*nPages\\s*=\\s*(\\d*)");
 	private static Pattern				pageSize			= Pattern.compile("\\s*pageSize\\s*=\\s*(.*)");
 	private static Pattern				pageActivate		= Pattern.compile("\\s*pageActivate\\s*=\\s*(.*)");
 	private static Pattern				pageReadCommand		= Pattern.compile("\\s*pageReadCommand\\s*=\\s*(.*)");
 
 	private static Pattern				constantScalar		= Pattern
 																	.compile("\\s*(\\w*)\\s*=\\s*(scalar)\\s*,\\s*(.*?)\\s*,\\s*(\\d*)\\s*,\\s*\\\"(.*)\\\"\\s*,\\s*([-+]?\\d*\\.?\\d*)\\s*,\\s*([-+]?\\d*\\.?\\d*)\\s*,\\s*([-+]?\\d*\\.?\\d*)\\s*,\\s*([-+]?\\d*\\.?\\d*)\\s*,\\s*([-+]?\\d*\\.?\\d*).*");
 	private static Pattern				constantSimple		= Pattern
 																	.compile("\\s*(\\w*)\\s*=\\s*(scalar)\\s*,\\s*(.*?)\\s*,\\s*(\\d*)\\s*,\\s*\\\"(.*)\\\"\\s*,\\s*([-+]?\\d*\\.?\\d*)\\s*,\\s*([-+]?\\d*\\.?\\d*).*");
 	// private static Pattern constantArray = Pattern
 	// .compile("\\s*(\\w*)\\s*=\\s*(\\w*)\\s*,\\s*(.*?)\\s*,\\s*(\\d*)\\s*,\\s*(.*)\\s*,\\s*\\\"(.*)\\\"\\s*,\\s*([-+]?\\d*.?\\d*)\\s*,\\s*([-+]?\\d*\\.?\\d*)\\s*,\\s*([-+]?\\d*\\.?\\d*)\\s*,\\s*([-+]?\\d*\\.?\\d*)\\s*,\\s*([-+]?\\d*\\.?\\d*)");
 	private static List<String>			runtime				= new ArrayList<String>();
 	private static List<String>			logHeader			= new ArrayList<String>();
 	private static List<String>			logRecord			= new ArrayList<String>();
 	private static List<String>			gaugeDef			= new ArrayList<String>();
 	private static Map<String, String>	runtimeVars;
 	private static Map<String, String>	evalVars;
 	private static Map<String, String>	constantVars;
 	private static Set<String>			flags;
 	private static String				fingerprintSource;
 	private static ArrayList<String>	gaugeDoc;
 	private static String				signatureStr;
 	private static String				queryCommandStr;
 	private static String				ochGetCommandStr;
 	private static String				ochBlockSizeStr;
 	private static ArrayList<String>	defaultGauges;
 	private static int					currentPage			= 0;
 	private static ArrayList<Constant>	constants;
 	private static ArrayList<String>	pageSizes;
 	private static ArrayList<String>	pageActivateCommands;
 	private static ArrayList<String>	pageReadCommands;
 	private static Section				currentSection;
 	private static String				className;
 	private static int					blockReadTimeoutVal;
 	private static int					pageActivationDelayVal;
 	private static Set<String>			alwaysInt			= new HashSet<String>(Arrays.asList(new String[] {}));
 	private static Set<String>			alwaysDouble		= new HashSet<String>(Arrays.asList(new String[] { "pulseWidth",
 			"throttle", "accDecEnrich", "accDecEnrichPcnt", "accEnrichPcnt", "accEnrichMS", "decEnrichPcnt", "decEnrichMS", "time",
 			"egoVoltage", "egoVoltage2","egoCorrection","veCurr"					}));
 
 	/**
 	 * @param args
 	 * @throws FileNotFoundException
 	 */
 	public static void main(String[] args) throws IOException
 	{
 		File f = new File(args[0]);
 		BufferedReader br = new BufferedReader(new FileReader(f));
 
 		String line;
 
 		while ((line = br.readLine()) != null)
 		{
 			preProcess(line);
 		}
 	}
 
 	private static void preProcess(String filename) throws IOException
 	{
 		signatureStr = "";
 		queryCommandStr = "";
 		runtime = new ArrayList<String>();
 		logHeader = new ArrayList<String>();
 		logRecord = new ArrayList<String>();
 		runtimeVars = new HashMap<String, String>();
 		evalVars = new HashMap<String, String>();
 		constantVars = new HashMap<String, String>();
 		constants = new ArrayList<Constant>();
 		flags = new HashSet<String>();
 		gaugeDef = new ArrayList<String>();
 		gaugeDoc = new ArrayList<String>();
 		defaultGauges = new ArrayList<String>();
 		pageActivateCommands = new ArrayList<String>();
 		fingerprintSource = "";
 		currentPage = 0;
 		constants = new ArrayList<Constant>();
 		currentSection = Section.None;
 
 		File f = new File(filename);
 		if (f.isDirectory())
 			return;
 
 		className = f.getName();
 		process(f);
 	}
 
 	private static void process(File f) throws IOException
 	{
 
 		BufferedReader br = new BufferedReader(new FileReader(f));
 
 		String line;
 
 		while ((line = br.readLine()) != null)
 		{
 			line = line.trim();
 			if (line.startsWith("#include "))
 			{
 				handleImport(line, f);
 				continue;
 			}
 
 			if (line.trim().equals("[MegaTune]"))
 			{
 				currentSection = Section.Header;
 				continue;
 			}
 			else if (line.trim().equals("[OutputChannels]"))
 			{
 				currentSection = Section.Expressions;
 				continue;
 			}
 			else if (line.trim().equals("[Datalog]"))
 			{
 				currentSection = Section.Logs;
 				continue;
 
 			}
 			else if (line.trim().equals("[GaugeConfigurations]"))
 			{
 				currentSection = Section.Gauges;
 				continue;
 
 			}
 			else if (line.trim().equals("[FrontPage]"))
 			{
 				currentSection = Section.FrontPage;
 			}
 			else if (line.trim().equals("[Constants]"))
 			{
 				currentSection = Section.Constants;
 			}
 			else if (line.trim().startsWith("["))
 			{
 				currentSection = Section.None;
 				continue;
 
 			}
 			switch (currentSection)
 			{
 			case Expressions:
 				processExpr(line);
 				break;
 			case Logs:
 				processLogEntry(line);
 				break;
 			case Gauges:
 				processGaugeEntry(line);
 				break;
 			case Header:
 				processHeader(line);
 				break;
 			case FrontPage:
 				processFrontPage(line);
 				break;
 			case Constants:
 				processConstants(line);
 				break;
 			}
 
 		}
 		writeFile(f.getParent(), className);
 	}
 
 	private static void handleImport(String line, File parentFile) throws IOException
 	{
 		Pattern importFile = Pattern.compile(".*\\\"(.*)\\\"");
 
 		Matcher importFileM = importFile.matcher(line);
 		if (importFileM.matches())
 		{
 			String fileName = importFileM.group(1);
 			File imported = new File(parentFile.getAbsoluteFile().getParentFile(), fileName);
 			process(imported);
 		}
 
 	}
 
 	private static void processConstants(String line)
 	{
 		line += "; junk";
 		line = StringUtils.trim(line).split(";")[0];
 		if (StringUtils.isEmpty(line))
 		{
 			return;
 		}
 
 		Matcher pageM = page.matcher(line);
 		if (pageM.matches())
 		{
 			currentPage = Integer.parseInt(pageM.group(1));
 			return;
 		}
 		Matcher pageSizesM = pageSize.matcher(line);
 		if (pageSizesM.matches())
 		{
 			String values = StringUtils.remove(pageSizesM.group(1), ' ');
 			String[] list = values.split(",");
 			pageSizes = new ArrayList<String>(Arrays.asList(list));
 		}
 
 		Matcher pageActivateM = pageActivate.matcher(line);
 		if (pageActivateM.matches())
 		{
 			String values = StringUtils.remove(pageActivateM.group(1), ' ');
 			values = StringUtils.remove(values, '"');
 			String[] list = values.split(",");
 			pageActivateCommands = new ArrayList<String>(Arrays.asList(list));
 		}
 
 		Matcher pageReadCommandM = pageReadCommand.matcher(line);
 		if (pageReadCommandM.matches())
 		{
 			String values = StringUtils.remove(pageReadCommandM.group(1), ' ');
 			values = StringUtils.remove(values, '"');
 			String[] list = values.split(",");
 			pageReadCommands = new ArrayList<String>(Arrays.asList(list));
 		}
 
 		Matcher blockReadTimeoutM = blockReadTimeout.matcher(line);
 		if (blockReadTimeoutM.matches())
 		{
 			blockReadTimeoutVal = Integer.parseInt(blockReadTimeoutM.group(1));
 			return;
 		}
 		Matcher pageActivationDelayM = pageActivationDelay.matcher(line);
 		if (pageActivationDelayM.matches())
 		{
 			pageActivationDelayVal = Integer.parseInt(pageActivationDelayM.group(1));
 			return;
 		}
 
 		Matcher bitsM = bits.matcher(line);
 		Matcher constantM = constantScalar.matcher(line);
 		Matcher constantSimpleM = constantSimple.matcher(line);
 		if (constantM.matches())
 		{
 			String name = constantM.group(1);
 			String classtype = constantM.group(2);
 			String type = constantM.group(3);
 			int offset = Integer.parseInt(constantM.group(4));
 			String units = constantM.group(5);
 			String scaleText = constantM.group(6);
 			String translateText = constantM.group(7);
 			String lowText = constantM.group(8);
 			String highText = constantM.group(9);
 			String digitsText = constantM.group(10);
 			double scale = !StringUtils.isEmpty(scaleText) ? Double.parseDouble(scaleText) : 0;
 			double translate = !StringUtils.isEmpty(translateText) ? Double.parseDouble(translateText) : 0;
 			double low = !StringUtils.isEmpty(lowText) ? Double.parseDouble(lowText) : 0;
 			double high = !StringUtils.isEmpty(highText) ? Double.parseDouble(highText) : 0;
 			int digits = !StringUtils.isEmpty(digitsText) ? (int) Double.parseDouble(digitsText) : 0;
 
 			Constant c = new Constant(currentPage, name, classtype, type, offset, "", units, scale, translate, low, high, digits);
 
 			if (scale == 1.0)
 			{
 				constantVars.put(name, "int");
 			}
 			else
 			{
 				constantVars.put(name, "double");
 			}
 			constants.add(c);
 		}
 		else if (constantSimpleM.matches())
 		{
 			String name = constantSimpleM.group(1);
 			String classtype = constantSimpleM.group(2);
 			String type = constantSimpleM.group(3);
 			int offset = Integer.parseInt(constantSimpleM.group(4));
 			String units = constantSimpleM.group(5);
 			double scale = Double.parseDouble(constantSimpleM.group(6));
 			double translate = Double.parseDouble(constantSimpleM.group(7));
 
 			Constant c = new Constant(currentPage, name, classtype, type, offset, "", units, scale, translate, 0, 0, 0);
 
 			if (scale == 1.0)
 			{
 				constantVars.put(name, "int");
 			}
 			else
 			{
 				constantVars.put(name, "double");
 			}
 			constants.add(c);
 
 		}
 		else if (bitsM.matches())
 		{
 			String name = bitsM.group(1);
 			String offset = bitsM.group(3);
 			String start = bitsM.group(4);
 			String end = bitsM.group(5);
 
 			Constant c = new Constant(currentPage, name, "bits", "", Integer.parseInt(offset), "[" + start + ":" + end + "]", "",
 					1, 0, 0, 0, 0);
 			constantVars.put(name, "int");
 			constants.add(c);
 
 		}
 		else if (line.startsWith("#"))
 		{
 			String preproc = (processPreprocessor(line));
 			Constant c = new Constant(currentPage, preproc, "", "PREPROC", 0, "", "", 0, 0, 0, 0, 0);
 			constants.add(c);
 		}
 	}
 
 	private static void processFrontPage(String line)
 	{
 		Matcher dgM = defaultGauge.matcher(line);
 		if (dgM.matches())
 		{
 			defaultGauges.add(dgM.group(1));
 		}
 	}
 
 	private static void processHeader(String line)
 	{
 		Matcher queryM = queryCommand.matcher(line);
 		if (queryM.matches())
 		{
 			queryCommandStr = "byte[] queryCommand=new byte[]{'" + queryM.group(1) + "'};";
 			return;
 		}
 
 		Matcher sigM = signature.matcher(line);
 		if (sigM.matches())
 		{
 			String tmpsig = sigM.group(1);
 			if (line.contains("null"))
 			{
 				tmpsig += "\\0";
 			}
 			signatureStr = "String signature=\"" + tmpsig + "\";";
 		}
 	}
 
 	private static void processGaugeEntry(String line)
 	{
 		Matcher m = gauge.matcher(line);
 		if (m.matches())
 		{
 			String name = m.group(1);
 			String channel = m.group(2);
 			String title = m.group(3);
 			String units = m.group(4);
 			String lo = m.group(5);
 			String hi = m.group(6);
 			String loD = m.group(7);
 			String loW = m.group(8);
 			String hiW = m.group(9);
 			String hiD = m.group(10);
 			String vd = m.group(11);
 			String ld = m.group(12);
 
 			String g = String.format(
 					"GaugeRegister.INSTANCE.addGauge(new GaugeDetails(\"%s\",\"%s\",%s,\"%s\",\"%s\",%s,%s,%s,%s,%s,%s,%s,%s,0));",
 					name, channel, channel, title, units, lo, hi, loD, loW, hiW, hiD, vd, ld);
 			String gd = String
 					.format("<tr><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td><td>%s</td></tr>",
 							name, channel, title, units, lo, hi, loD, loW, hiW, hiD, vd, ld);
 			gaugeDoc.add(gd);
 			gaugeDef.add(g);
 
 		}
 		else if (line.startsWith("#"))
 		{
 			gaugeDef.add(processPreprocessor(line));
 			gaugeDoc.add(String.format("<tr><td colspan=\"12\" id=\"preprocessor\">%s</td></tr>", line));
 		}
 
 	}
 
 	private static void writeFile(String path, String className) throws IOException
 	{
 		className = StringUtils.capitalize(className);
 		className = StringUtils.remove(className, ".");
 		className = StringUtils.remove(className, "ini");
 		className = StringUtils.replace(className, " ", "_");
 		className = StringUtils.replace(className, "-", "_");
 		className = "ZZ" + className;
 
 		String classFile = "";
 		if (path != null)
 		{
 			classFile += path + "/";
 		}
 
 		classFile += "gen_src/" + className + ".java";
 		System.out.println("Writing to " + classFile);
 		PrintWriter writer = new PrintWriter(new FileWriter(classFile));
 		String fingerprint = getFingerprint();
 		System.out.println(fingerprint + " : " + className);
 
 		outputPackageAndIncludes(writer);
 		writer.println("/*");
 		writer.println("Fingerprint : " + fingerprint);
 		writer.println("*/");
 
 		writer.println("public class " + className + " extends Megasquirt\n{");
 		outputConstructor(writer, className);
 		writer.println(queryCommandStr);
 		writer.println(signatureStr);
 		writer.println(ochGetCommandStr);
 		writer.println(ochBlockSizeStr);
 		writer.println("private Set<String> sigs = new HashSet<String>(Arrays.asList(new String[] { signature }));");
 		outputGlobalVars(writer);
 		outputRTCalcs(writer);
 		outputLogInfo(writer);
 		outputGauges(writer);
 
 		// outputGaugeDoc(writer);
 
 		outputOverrides(writer);
 		outputLoadConstants(writer);
 		writer.println("\n}\n");
 
 		writer.close();
 	}
 
 	private static void outputGlobalVars(PrintWriter writer)
 	{
 		writer.println("//Flags");
 		for (String name : flags)
 		{
 			writer.println("boolean " + name + ";");
 		}
 		writer.println("//Runtime vars");
 		for (String name : runtimeVars.keySet())
 		{
 			String type = getType(name, runtimeVars);
 			writer.println(type + " " + name + ";");
 		}
 		writer.println("\n//eval vars");
 		for (String name : evalVars.keySet())
 		{
 			String type = getType(name, evalVars);
 			writer.println(type + " " + name + ";");
 		}
 		writer.println("\n//Constants");
 		for (String name : constantVars.keySet())
 		{
 			String type = getType(name, constantVars);
 			writer.println(type + " " + name + ";");
 		}
 		writer.println("\n");
 		writer.println("private String[] defaultGauges = {");
 		boolean first = true;
 		for (String dg : defaultGauges)
 		{
 			if (!first)
 				writer.print(",");
 			first = false;
 			writer.println("\"" + dg + "\"");
 		}
 		writer.println("};");
 
 	}
 
 	private static String getType(String name, Map<String, String> vars)
 	{
 		String type = vars.get(name);
 		if (alwaysInt.contains(name))
 		{
 			type = "int";
 		}
 		else if (alwaysDouble.contains(name))
 		{
 			type = "double";
 		}
 		return type;
 	}
 
 	private static void outputRTCalcs(PrintWriter writer)
 	{
 		writer.println("	@Override");
 		writer.println("	public void calculate(byte[] ochBuffer) throws IOException");
 		writer.println("{");
 		for (String defn : runtime)
 		{
 			writer.println(defn);
 			// System.out.println(defn);
 		}
 		writer.println("}");
 	}
 
 	private static void outputLogInfo(PrintWriter writer)
 	{
 		writer.println("@Override");
 		writer.println("public String getLogHeader()");
 		writer.println("{");
 		writer.println("	StringBuffer b = new StringBuffer();");
 		for (String header : logHeader)
 		{
 			writer.println(header);
 		}
 		writer.println("b.append(MSUtils.getLocationLogHeader());");
 		writer.println("    return b.toString();\n}\n");
 		writer.println("@Override");
 		writer.println("public String getLogRow()");
 		writer.println("{");
 		writer.println("	StringBuffer b = new StringBuffer();");
 
 		for (String record : logRecord)
 		{
 			writer.println(record);
 		}
 		writer.println("b.append(MSUtils.getLocationLogRow());");
 		writer.println("    return b.toString();\n}\n");
 	}
 
 	private static void outputGauges(PrintWriter writer)
 	{
 		writer.println("@Override");
 		writer.println("public void initGauges()");
 		writer.println("{");
 		for (String gauge : gaugeDef)
 		{
 			writer.println(gauge);
 		}
 		writer.println("\n}\n");
 	}
 
 	@SuppressWarnings("unused")
 	private static void outputGaugeDoc(PrintWriter writer)
 	{
 		writer.println("/*");
 		for (String gauge : gaugeDoc)
 		{
 			writer.println(gauge);
 		}
 
 		writer.println("*/");
 	}
 
 	private static void outputConstructor(PrintWriter writer, String className)
 	{
 		writer.println("public " + className + "(Context c)");
 		writer.println("{");
 		writer.println("    super(c);");
 		writer.println("    refreshFlags();");
 		writer.println(" }");
 		writer.println("@Override");
 		writer.println("public void refreshFlags()");
 		writer.println("{");
 		for (String flag : flags)
 		{
 			writer.println("    " + flag + " = isSet(\"" + flag + "\");");
 		}
 		writer.println(" }");
 
 	}
 
 	private static void outputPackageAndIncludes(PrintWriter writer)
 	{
 		writer.println("package uk.org.smithfamily.mslogger.ecuDef.gen;");
 		writer.println("");
 		writer.println("import java.io.IOException;");
 		writer.println("import java.util.*;");
 		writer.println("");
 		writer.println("import android.content.Context;");
 		writer.println("");
 		writer.println("import uk.org.smithfamily.mslogger.ecuDef.MSUtils;");
 		writer.println("import uk.org.smithfamily.mslogger.ecuDef.Megasquirt;");
 		writer.println("import uk.org.smithfamily.mslogger.widgets.GaugeDetails;");
 		writer.println("import uk.org.smithfamily.mslogger.widgets.GaugeRegister;");
 
 	}
 
 	private static void outputLoadConstants(PrintWriter writer)
 	{
 		writer.println("@Override");
 		writer.println("public void loadConstants(boolean simulated)");
 		writer.println("{");
 		writer.println("byte[] pageBuffer = null;");
 		int pageNo = 0;
 		int pageOffset = -Integer.parseInt(pageSizes.get(0));
 		for (Constant c : constants)
 		{
 			if (c.getPage() != pageNo)
 			{
 				pageNo = c.getPage();
 				int pageSize = Integer.parseInt(pageSizes.get(pageNo - 1));
 				pageOffset += pageSize;
 				String activateCommand = null;
 				if (pageNo - 1 < pageActivateCommands.size())
 				{
 					activateCommand = pageActivateCommands.get(pageNo - 1);
 				}
 				String readCommand = null;
 				if (pageNo - 1 < pageReadCommands.size())
 				{
 					readCommand = pageReadCommands.get(pageNo - 1);
 				}
 
 				outputLoadPage(pageNo, 0, pageSize, activateCommand, readCommand, writer);
 			}
 			// getScalar(String bufferName,String name, String dataType, String
 			// offset, String scale, String numOffset)
 			String name = c.getName();
 			if (!"PREPROC".equals(c.getType()))
 			{
 				String def;
 				if ("bits".equals(c.getClassType()))
 				{
 					String bitspec = StringUtils.remove(StringUtils.remove(c.getShape(), '['), ']');
 					String[] bits = bitspec.split(":");
 					int offset = c.getOffset();
 					String start = bits[0];
 					String end = bits[1];
 					String ofs = "0";
 					if (end.contains("+"))
 					{
 						String[] parts = end.split("\\+");
 						end = parts[0];
 						ofs = parts[1];
 					}
 					def = (name + " = MSUtils.getBits(pageBuffer," + offset + "," + start + "," + end + "," + ofs + ");");
 				}
 				else
 				{
 					def = getScalar("pageBuffer", constantVars.get(name), name, c.getType(), "" + c.getOffset(), "" + c.getScale(),
 							"" + c.getTranslate());
 				}
 				writer.println(def);
 			}
 			else
 			{
 				writer.println(name);
 			}
 
 		}
 
 		writer.println("}");
 	}
 
 	private static void outputLoadPage(int pageNo, int pageOffset, int pageSize, String activate, String read, PrintWriter writer)
 	{
 		if (activate != null)
 		{
 
 			activate = processStringToBytes(activate, pageOffset, pageSize);
 		}
 		if (read != null)
 		{
 			read = processStringToBytes(read, pageOffset, pageSize);
 		}
 		writer.println(String.format("pageBuffer = loadPage(%d,%d,%d,%s,%s);", pageNo, pageOffset, pageSize, activate, read));
 
 	}
 
 	private static String processStringToBytes(String s, int offset, int count)
 	{
 	    String digits = "0123456789abcdef";
 		String ret = "new byte[]{";
 		boolean first = true;
 		
 		for (int p = 0; p < s.length(); p++)
 		{
 			if (!first)
 				ret += ",";
 			
             char c = s.charAt(p);
 			switch (c)
 			{
 			case '\\':
 				p++;
 				c = s.charAt(p);
 				assert c == 'x';
 				p++;
 				c = s.charAt(p);
 				c=Character.toLowerCase(c);
 				int val = 0;
 				int digit = digits.indexOf(c);
 				val = digit * 16;
 				p++;
 				c = s.charAt(p);
 				c=Character.toLowerCase(c);
                 digit = digits.indexOf(c);
 				val = val + digit;
 				ret += val;
 				break;
 
 			case '%':
 				p++;
 				c = s.charAt(p);
                 
 				assert c == '2';
 				p++;
 				c = s.charAt(p);
                 if (c == 'o')
 				{
 					ret += bytes(offset);
 				}
 				else
 				{
 					ret += bytes(count);
 				}
 				break;
 
 			default:
 				ret += Byte.toString((byte) c);
 				break;
 			}
 			first = false;
 		}
 
 		ret += "}";
 		return ret;
 	}
 
 	private static String bytes(int val)
 	{
 		int hi = val / 256;
 		int low = val % 256;
 		if (hi > 127)
 			hi -= 256;
 		if (low > 127)
 			low -= 256;
 		return "" + hi + "," + low;
 	}
 
 	private static void outputOverrides(PrintWriter writer)
 	{
 		String overrides = "@Override\n" + "public Set<String> getSignature()\n" + "{\n" + "    return sigs;\n" + "}\n"
 				+ "@Override\n" + "public byte[] getOchCommand()\n" + "{\n" + "    \n" + "    return this.ochGetCommand;\n" + "}\n"
 				+
 
 				"@Override\n" + "public byte[] getSigCommand()\n" + "{\n" + "    return this.queryCommand;\n" + "}\n" +
 
 				"@Override\n" + "public int getBlockSize()\n" + "{\n" + "    return this.ochBlockSize;\n" + "}\n" +
 
 				"@Override\n" + "public int getSigSize()\n" + "{\n" + "    return signature.length();\n" + "}\n" +
 
 				"@Override\n" + "public int getPageActivationDelay()\n" + "{\n" + "    return " + pageActivationDelayVal + ";\n"
 				+ "}\n" +
 
 				"@Override\n" + "public int getInterWriteDelay()\n" + "{\n" + "    return " + blockReadTimeoutVal + ";\n" + "}\n" +
 
 				"@Override\n" + "public int getCurrentTPS()\n" + "{\n" + "   \n" + "    return (int)tpsADC;\n" + "}\n" +
 
 				"@Override\n" + "public String[] defaultGauges()\n" + "{\n" + "    return defaultGauges;\n" + "}\n";
 
 		writer.println(overrides);
 	}
 
 	private static String getFingerprint()
 	{
 		StringBuffer b = new StringBuffer();
 		try
 		{
 			MessageDigest md = MessageDigest.getInstance("MD5");
 			byte[] array = md.digest(fingerprintSource.getBytes());
 			for (int i = 0; i < array.length; i++)
 			{
 				b.append(Integer.toHexString((array[i] & 0xFF) | 0x100).substring(1, 3));
 			}
 		}
 		catch (NoSuchAlgorithmException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return b.toString();
 	}
 
 	private static void processLogEntry(String line)
 	{
 		Matcher logM = log.matcher(line);
 		if (logM.matches())
 		{
 			String header = logM.group(2);
 			String variable = logM.group(1);
 			if ("double".equals(runtimeVars.get(variable)))
 			{
 				variable = "round(" + variable + ")";
 			}
 			logHeader.add("b.append(\"" + header + "\").append(\"\\t\");");
 			logRecord.add("b.append(" + variable + ").append(\"\\t\");");
 		}
 		else if (line.startsWith("#"))
 		{
 			String directive = processPreprocessor(line);
 			logHeader.add(directive);
 			logRecord.add(directive);
 		}
 	}
 
 	private static void processExpr(String line)
 	{
 		String definition = null;
 		line += "; junk";
 		line = StringUtils.trim(line).split(";")[0];
 		line = StringUtils.replace(line, "timeNow", "timeNow()");
 		Matcher bitsM = bits.matcher(line);
 		Matcher scalarM = scalar.matcher(line);
 		Matcher exprM = expr.matcher(line);
 		Matcher ochGetCommandM = ochGetCommand.matcher(line);
 		Matcher ochBlockSizeM = ochBlockSize.matcher(line);
 		if (bitsM.matches())
 		{
 			String name = bitsM.group(1);
 			String offset = bitsM.group(3);
 			String start = bitsM.group(4);
 			String end = bitsM.group(5);
 			String ofs = "0";
 			if (end.contains("+"))
 			{
 				String[] parts = end.split("\\+");
 				end = parts[0];
 				ofs = parts[1];
 			}
 			definition = (name + " = MSUtils.getBits(ochBuffer," + offset + "," + start + "," + end + "," + ofs + ");");
 			runtime.add(definition);
 			runtimeVars.put(name, "int");
 		}
 		else if (scalarM.matches())
 		{
 			String name = scalarM.group(1);
 			String dataType = scalarM.group(2);
 			String offset = scalarM.group(3);
 			String scalingRaw = scalarM.group(4);
 			String[] scaling = scalingRaw.split(",");
 			String scale = scaling[1].trim();
 			String numOffset = scaling[2].trim();
 			if (Double.parseDouble(scale) != 1)
 			{
 				runtimeVars.put(name, "double");
 			}
 			else
 			{
 				runtimeVars.put(name, "int");
 			}
 			definition = getScalar("ochBuffer", runtimeVars.get(name), name, dataType, offset, scale, numOffset);
 			fingerprintSource += definition;
 			runtime.add(definition);
 		}
 		else if (exprM.matches())
 		{
 			String name = exprM.group(1);
 			String expression = deBinary(exprM.group(2).trim());
 			Matcher ternaryM = ternary.matcher(expression);
 			if (ternaryM.matches())
 			{
 				// System.out.println("BEFORE : " + expression);
 				String test = ternaryM.group(1);
 				String values = ternaryM.group(2);
 				if (StringUtils.containsAny(test, "<>!="))
 				{
 					expression = "(" + test + ") ? " + values;
 				}
 				else
 				{
 					expression = "((" + test + ") != 0 ) ? " + values;
 				}
 				// System.out.println("AFTER  : " + expression + "\n");
 			}
 			definition = name + " = (" + expression + ");";
 			runtime.add(definition);
 			if (isFloatingExpression(expression))
 			{
 				evalVars.put(name, "double");
 			}
 			else
 			{
 				evalVars.put(name, "int");
 			}
 		}
 		else if (ochGetCommandM.matches())
 		{
 			ochGetCommandStr = "byte [] ochGetCommand = new byte[]{'" + ochGetCommandM.group(1) + "'};";
 		}
 		else if (ochBlockSizeM.matches())
 		{
 			ochBlockSizeStr = "int ochBlockSize = " + ochBlockSizeM.group(1) + ";";
 		}
 		else if (line.startsWith("#"))
 		{
 			runtime.add(processPreprocessor(line));
 		}
 		else if (!StringUtils.isEmpty(line))
 		{
 			System.out.println(line);
 		}
 	}
 
 	private static String deBinary(String group)
 	{
 		Matcher binNumber = binary.matcher(group);
 		if (!binNumber.matches())
 		{
 			return group;
 		}
 		else
 		{
 			String binNum = binNumber.group(2);
 			int num = Integer.parseInt(binNum, 2);
 			String expr = binNumber.group(1) + num + binNumber.group(3);
 			return deBinary(expr);
 		}
 	}
 
 	private static boolean isFloatingExpression(String expression)
 	{
 		return expression.contains(".");
 	}
 
 	private static String processPreprocessor(String line)
 	{
 		String filtered;
 		boolean stripped = false;
 
 		filtered = line.replace("  ", " ");
 		stripped = filtered.equals(line);
 		while (!stripped)
 		{
 			line = filtered;
 			filtered = line.replace("  ", " ");
 			stripped = filtered.equals(line);
 		}
 		String[] components = line.split(" ");
 		String flagName = components.length > 1 ? sanitize(components[1]) : "";
 		if (components[0].equals("#if"))
 		{
 			flags.add(flagName);
 			return ("if (" + flagName + ")\n{");
 		}
 		if (components[0].equals("#elif"))
 		{
 			flags.add(flagName);
 			return ("}\nelse if (" + flagName + ")\n{");
 		}
 		if (components[0].equals("#else"))
 		{
 			return ("}\nelse\n{");
 		}
 		if (components[0].equals("#endif"))
 		{
 			return ("}");
 		}
 
 		return "";
 	}
 
 	private static String sanitize(String flagName)
 	{
 		return StringUtils.replace(flagName, "!", "n");
 	}
 
 	private static String getScalar(String bufferName, String javaType, String name, String dataType, String offset, String scale,
 			String numOffset)
 	{
 		String definition = name + " = (" + javaType + ")((MSUtils.get";
 		if (dataType.startsWith("S"))
 		{
 			definition += "Signed";
 		}
 		int size = Integer.parseInt(dataType.substring(1));
 		switch (size)
 		{
 		case 8:
 			definition += "Byte";
 			break;
 		case 16:
 			definition += "Word";
 			break;
 		case 32:
 			definition += "Long";
 			break;
 		default:
 			definition += dataType;
 			break;
 		}
 		definition += "(" + bufferName + "," + offset + ") + " + numOffset + ") * " + scale + ");";
 		return definition;
 	}
 
 }
