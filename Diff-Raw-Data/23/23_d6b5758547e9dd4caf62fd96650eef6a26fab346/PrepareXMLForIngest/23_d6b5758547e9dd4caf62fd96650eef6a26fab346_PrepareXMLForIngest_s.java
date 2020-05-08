 package com.xmlmachines.temp;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URI;
 import java.text.MessageFormat;
 import java.util.UUID;
 
 import org.apache.commons.io.output.ByteArrayOutputStream;
 import org.apache.log4j.Logger;
 
 import com.marklogic.xcc.ContentSource;
 import com.marklogic.xcc.ContentSourceFactory;
import com.marklogic.xcc.Request;
 import com.marklogic.xcc.Session;
 import com.marklogic.xcc.exceptions.RequestException;
 import com.ximpleware.AutoPilot;
 import com.ximpleware.EOFException;
 import com.ximpleware.EncodingException;
 import com.ximpleware.EntityException;
 import com.ximpleware.NavException;
 import com.ximpleware.ParseException;
 import com.ximpleware.VTDGen;
 import com.ximpleware.VTDNav;
 import com.ximpleware.XPathEvalException;
 import com.ximpleware.XPathParseException;
 import com.xmlmachines.TestExtractXML;
 
 public class PrepareXMLForIngest {
 
 	private final static Logger LOG = Logger.getLogger(TestExtractXML.class);
 	private final static String path = "/home/ableasdale/workspace/medline-data/";
 	private final static String uriStr = "xcc://admin:admin@localhost:8010/Medline";
 	private static ContentSource cs;
 
 	public static void main(String[] s) throws Exception {
 
 		URI uri = new URI(uriStr);
 		cs = ContentSourceFactory.newContentSource(uri);
 
 		LOG.info("Scanning folder: " + path);
 		// for all xml files in folder do
 		File folder = new File(path);
 		File[] listOfFiles = folder.listFiles();
 
 		for (File f : listOfFiles) {
 			if (f.isFile()) {
 				if (f.getName().endsWith(".xml")) {
 					// TODO - kick off a separate Thread for each of these...
 					processXmlDocument(f);
 				}
 			}
 			/*
 			 * else if (listOfFiles[i].isDirectory()) {
 			 * System.out.println("Directory " + listOfFiles[i].getName()); }
 			 */
 		}
 
 	}
 
 	private static void processZipFile(File f) {
 		LOG.info("Processing: " + f.getName());
 		// TODO - low priority
 	}
 
 	private static void processXmlDocument(File f) {
 		LOG.info("Processing document: " + f.getName());
 		try {
 			processAsXml(f);
 		} catch (EncodingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (EOFException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (EntityException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (ParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (XPathParseException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (XPathEvalException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NavException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	private static void processAsXml(File f) throws FileNotFoundException,
 			IOException, EncodingException, EOFException, EntityException,
 			ParseException, XPathParseException, XPathEvalException,
 			NavException {
 
 		int count = 0;
 
 		FileInputStream fis = new FileInputStream(f);
 		byte[] b = new byte[(int) f.length()];
 		fis.read(b);
 		VTDGen vg = new VTDGen();
 		vg.setDoc(b);
 		vg.parse(true);
 
 		VTDNav vn = vg.getNav();
 		AutoPilot ap = new AutoPilot();
 		ap.bind(vn);
 		byte[] ba = vn.getXML().getBytes();
 		ap.selectXPath("/MedlineCitationSet/MedlineCitation");
 		int i = -1;
 		while ((i = ap.evalXPath()) != -1) {
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			long l = vn.getElementFragment();
 			int offset = (int) l;
 			int len = (int) (l >> 32);
 			baos.write(ba, offset, len);
 			baos.flush();
 			Session session = null;
			Request request = null;
 			try {
 				session = cs.newSession();
				request = session.newAdhocQuery("xdmp:document-insert(\"/"
						+ UUID.randomUUID() + ".xml\", "
						+ baos.toString("utf-8") + ")");
				session.submitRequest(request);
 			} catch (RequestException e) {
 				LOG.error("Unable to perform insert on one document", e);
				LOG.debug(request);
 			} finally {
 				session.close();
 			}

 			count++;
 		}
		LOG.info(MessageFormat.format("Total: {0}", count));
 	}
 }
