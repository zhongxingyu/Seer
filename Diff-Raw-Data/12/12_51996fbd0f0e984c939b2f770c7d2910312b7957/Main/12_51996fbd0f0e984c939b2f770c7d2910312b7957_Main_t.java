 package com.miravtech.graphmlconvert;
 
 import java.io.File;
 import java.io.FilenameFilter;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 
 import org.graphdrawing.graphml.xmlns.graphml.Graphml;
 
 import com.miravtech.SBGNUtils.SBGNUtils;
 import com.miravtech.sbgn.SBGNPDl1;
 
 public class Main {
 
 	/**
 	 * @param args
 	 * @throws JAXBException
 	 */
 	public static void main(String[] args) throws JAXBException {
 
 		String source, destination;
 		// String source =
 		// "l:\\Documents and Settings\\r\\My Documents\\dcthera\\gpml tutti";
 		// String destination = "c:\\temp";
 		if (args.length >= 2) {
 			source = args[0];
 			destination = args[1];
 		} else {
 			throw new IllegalArgumentException(
 					"Must provide source and destination directory");
 		}
 		File srcDir = new File(source);
 		File destDir = new File(destination);
		int convert = 0;
 		if (srcDir.isDirectory()) {
 		for (File f : srcDir.listFiles(new XMLFiles())) {
 			File destGraphML = new File(destDir, XMLFiles.getName(f.getName())
 					+ ".graphml");
 			graphmlconvert(f, destGraphML);
			convert++;
 		}
 		} else {
 			graphmlconvert(srcDir, destDir);
			convert++;
 		}
		System.out.println("Converted: " + convert + " files.");
 	}
 
 	private static JAXBContext jaxbContext;
 	private static JAXBContext jaxbContext2;
 	private static Unmarshaller unmarshaller;
 	private static Marshaller marshaller;
 	/**
 	 * 
 	 * Converts the gpml to SBGN files.
 	 * 
 	 * @param sourceGPML
 	 *            the source file (must exist)
 	 * @param destSBGN
 	 *            the destination file (will be overwritten)
 	 * @throws JAXBException
 	 *             if any issue occurs with IO or with XML parsing
 	 */
 	public static void graphmlconvert(File sourceSBGN, File destGraphML)
 			throws JAXBException {
 
 		if (jaxbContext == null) {
 			jaxbContext = JAXBContext
 					.newInstance("com.miravtech.sbgn:com.miravtech.sbgn_graphics"); //
 			unmarshaller = jaxbContext.createUnmarshaller();
 		}
 
 
 		SBGNPDl1 root = (SBGNPDl1) unmarshaller.unmarshal(sourceSBGN);
 
 		SBGNUtils.setIDs(root);
 		Graphml out = SBGNUtils.asGraphML(root);
 
 		if (jaxbContext2 == null) {
 			jaxbContext2 = JAXBContext
 					.newInstance("com.yworks.xml.graphml:org.graphdrawing.graphml.xmlns.graphml"); //
 			marshaller = jaxbContext2.createMarshaller();
 		}
 
 
 		marshaller.marshal(out, destGraphML);
 
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
