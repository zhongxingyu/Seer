package com.xmlmachines.data;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URI;
 import java.text.MessageFormat;
 
 import org.apache.commons.io.output.ByteArrayOutputStream;
 import org.apache.log4j.Logger;
 
 import com.marklogic.xcc.Content;
 import com.marklogic.xcc.ContentCreateOptions;
 import com.marklogic.xcc.ContentFactory;
 import com.marklogic.xcc.ContentSource;
 import com.marklogic.xcc.ContentSourceFactory;
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
 
 /**
  * Prepare 60,000 Medline docs for db ingest
  * 
  * Takes around 76 seconds on a dual core laptop to ingest 60K docs using
  * vtd-xml and 2 XCC Session Threads (for 2 sample docs, each containing 30,000
  * records) to ingest the content
  * 
  * @author ableasdale
  * 
  */
 
 public class PrepareMedlineXMLForIngest {
 
 	private static final String MEDLINE_CITATION_XPATH = "/MedlineCitationSet/MedlineCitation";
 	private final static Logger LOG = Logger
 			.getLogger(PrepareMedlineXMLForIngest.class);
 	private final static String SRC_FOLDER = "/home/ableasdale/workspace/medline-data/";
 	private final static String XCC_URI = "xcc://admin:admin@localhost:8010/Medline";
 	private static ContentSource cs;
 
 	public static void main(String[] s) throws Exception {
 		PrepareMedlineXMLForIngest ingest = new PrepareMedlineXMLForIngest();
 		URI uri = new URI(XCC_URI);
 		cs = ContentSourceFactory.newContentSource(uri);
 
 		LOG.info(MessageFormat.format("Scanning folder: {0}", SRC_FOLDER));
 		File folder = new File(SRC_FOLDER);
 		File[] listOfFiles = folder.listFiles();
 
 		for (File f : listOfFiles) {
 			if (f.isFile() && f.getName().endsWith(".xml")) {
 				XmlProcessorThread xpt = ingest.new XmlProcessorThread(f);
 				new Thread(xpt).start();
 			}
 		}
 	}
 
 	private void logException(Exception e) {
 		LOG.error("Error encountered" + e.getMessage(), e);
 	}
 
 	private void processAsXml(File f) throws FileNotFoundException,
 			IOException, EncodingException, EOFException, EntityException,
 			ParseException, XPathParseException, XPathEvalException,
 			NavException {
 		Session session = cs.newSession();
 		ContentCreateOptions opts = new ContentCreateOptions();
 
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
 		ap.selectXPath(MEDLINE_CITATION_XPATH);
 		int i = -1;
 		while ((i = ap.evalXPath()) != -1) {
 			ByteArrayOutputStream baos = new ByteArrayOutputStream();
 			long l = vn.getElementFragment();
 			int offset = (int) l;
 			int len = (int) (l >> 32);
 			baos.write(ba, offset, len);
 			baos.flush();
 			String docUri = MessageFormat.format("{0}-{1}",
 					String.format("%05d", count), f.getName());
 			insertDocumentIntoMarkLogic(docUri, session, opts, baos);
 			count++;
 		}
 		session.close();
 		LOG.info(MessageFormat.format("Processed {0} documents", count));
 	}
 
 	private void insertDocumentIntoMarkLogic(String docUri, Session session,
 			ContentCreateOptions opts, ByteArrayOutputStream baos) {
 		try {
 			Content c = ContentFactory.newContent(docUri, baos.toByteArray(),
 					opts);
 			session.insertContent(c);
 		} catch (RequestException e) {
 			logException(e);
 		}
 	}
 
 	public class XmlProcessorThread implements Runnable {
 
 		File file;
 
 		public XmlProcessorThread(File f) {
 			this.file = f;
 			LOG.debug(MessageFormat.format("Processing file: {0}",
 					file.getName()));
 		}
 
 		@Override
 		public void run() {
 			try {
 				processAsXml(file);
 			} catch (Exception e) {
 				logException(e);
 			}
 			LOG.debug(MessageFormat.format("Ingest completed for {0}",
 					file.getName()));
 		}
 	}
 }
