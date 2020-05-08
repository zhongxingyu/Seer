 package com.miravtech.sbgn.exporter;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.util.Properties;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 
 import joptsimple.OptionParser;
 import joptsimple.OptionSet;
 
 import com.miravtech.SBGNUtils.INIConfiguration;
 import com.miravtech.SBGNUtils.SBGNUtils;
 import com.miravtech.sbgn.SBGNPDl1;
 
 public class Main {
 
 	static JAXBContext jaxbContext;
 	static Unmarshaller unmarshaller;
 	static Marshaller marshaller;
 
 	/**
 	 * @param args
 	 * @throws JAXBException
 	 */
 	public static void main(String[] args) throws Exception {
 
 		String organism ;
 		String db ;
 		String method ;
 		File srcDir;
 		File destDir;
 		boolean filtering = true;
 
 		Properties p = INIConfiguration.getConfiguration();
 		
 		OptionParser parser = new OptionParser();
 		parser.accepts("srcSBGN", "Name of the SBGN file to use.")
 				.withRequiredArg().ofType(File.class).describedAs("file path");
 		parser.accepts("outFile", "The target file.").withRequiredArg().ofType(
 				File.class).describedAs("file path");
 
 		String prop;
 		prop = p.getProperty("organism","HS");
 		parser.accepts("organism", "The name of the organism to consider")
 				.withOptionalArg().ofType(String.class).describedAs("Organism")
 				.defaultsTo(prop);
 		
 		
		prop = p.getProperty("database","EntrezGeneID");
 		parser.accepts("db", "The database to consider.").withOptionalArg()
 				.ofType(String.class).describedAs("Database").defaultsTo(
 						prop);
 
 		
 		prop = p.getProperty("exporter.method","GeneList");
 		parser.accepts("method", "The method to use.").withOptionalArg()
 				.ofType(String.class).describedAs("GeneList").defaultsTo(
 						prop);
 
 		parser.accepts("disableFilter", "Disable filtering.");
 
 		try {
 			OptionSet opts = parser.parse(args);
 
 			srcDir = (File) opts.valueOf("srcSBGN");
 			destDir = (File) opts.valueOf("outFile");
 
 			method = (String) opts.valueOf("method");
 			organism = (String) opts.valueOf("organism");
 			db = (String) opts.valueOf("db");
 			
 			if (opts.has("disableFilter"))
 				filtering = false;
 			
 			if (srcDir.getAbsolutePath().compareToIgnoreCase(
 					destDir.getAbsolutePath()) == 0)
 				throw new RuntimeException(
 						"Source and destination directories or files cannot be identical!");
 
 			jaxbContext = JAXBContext
 					.newInstance("com.miravtech.sbgn:com.miravtech.sbgn_graphics");
 			unmarshaller = jaxbContext.createUnmarshaller();
 			marshaller = jaxbContext.createMarshaller();
 
 		} catch (Exception e) {
 			System.out.println("Exception occured: " + e.toString()
 					+ "\nPossible commands:\n");
 			parser.printHelpOn(System.out);
 			return;
 		}
 
 		// run the function, we have the arguments;
 		if (method.equalsIgnoreCase("GeneList"))
 			exportSBGN(srcDir, destDir, organism, db, filtering);
 		else
 			throw new Exception(
 					"Method not supported, currently supported: GeneList");
 
 	}
 
 	public static void exportSBGN(File sourceSBGN, File destFile,
 			final String organism, final String db, final boolean usefilter)
 			throws JAXBException, IOException {
 
 		SBGNPDl1 sbgnpath = (SBGNPDl1) unmarshaller.unmarshal(sourceSBGN);
 		SBGNUtils utils = new SBGNUtils(sbgnpath.getValue());
 
 		utils.fillRedundantData();
 
 		Set<String> genes = utils.getSymbols(organism, db, usefilter);
 		FileOutputStream fos = new FileOutputStream(destFile);
 		PrintWriter pr = new PrintWriter(fos);
 		for (String g : genes)
 			pr.println(g);
 		pr.close();
 
 	}
 }
 
 class XMLFiles implements FilenameFilter {
 	public final static String XML = "xml";
 	private static Pattern file = Pattern.compile("(.*)\\." + XML);
 
 	@Override
 	public boolean accept(File dir, String name) {
 		return file.matcher(name.toLowerCase()).matches();
 	}
 
 	public static String getName(String name) {
 		Matcher m = file.matcher(name.toLowerCase());
 		if (!m.matches())
 			throw new RuntimeException("The name does not match the pattern");
 		return m.group(0);
 	}
 }
