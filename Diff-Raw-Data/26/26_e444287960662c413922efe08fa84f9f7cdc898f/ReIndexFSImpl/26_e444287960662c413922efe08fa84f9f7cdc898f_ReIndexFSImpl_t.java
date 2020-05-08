 /**
  * 
  */
 package org.ariadne_eu.utils.lucene.reindex;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringReader;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.Vector;
 
 import javax.xml.parsers.DocumentBuilderFactory;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.LineIterator;
 import org.apache.log4j.Logger;
 import org.apache.xml.serialize.OutputFormat;
 import org.apache.xml.serialize.XMLSerializer;
 import org.apache.xpath.XPathAPI;
 import org.ariadne.config.PropertiesManager;
 import org.ariadne_eu.metadata.insert.InsertMetadataFactory;
 import org.ariadne_eu.metadata.insert.InsertMetadataImpl;
 import org.ariadne_eu.metadata.insert.InsertMetadataLuceneImpl;
 import org.ariadne_eu.utils.config.RepositoryConstants;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.InputSource;
 
 /**
  * @author gonzalo
  * 
  */
 public class ReIndexFSImpl extends ReIndexImpl {
 
 	private static Logger log = Logger.getLogger(ReIndexFSImpl.class);
 	private String dirString;
 	private String repos;
 	private static Vector xpathQueries;
 
 	public ReIndexFSImpl() {
 		initialize();
 	}
 
 	void initialize() {
 		super.initialize();
 
 		try {
 			dirString = PropertiesManager.getInstance().getProperty(
 					RepositoryConstants.getInstance().MD_SPIFS_DIR);
 			repos = PropertiesManager.getInstance().getProperty(
 					RepositoryConstants.getInstance().SR_LUCENE_REINDEX_REPOS);
 
 			if (dirString == null)
 				log.error("initialize failed: no "
 						+ RepositoryConstants.getInstance().MD_SPIFS_DIR
 						+ " found");
 
 			if (repos == null) {
 				log.error("initialize failed: no repositories defined for reindexing");
 				log.error("Using default *(all repositories) option.");
 				repos = "*";
 			}
 
 			File dir = new File(dirString);
 			if (!dir.isDirectory())
 				log.error("initialize failed: "
 						+ RepositoryConstants.getInstance().MD_SPIFS_DIR
 						+ " invalid directory");
 			xpathQueries = new Vector();
 			if (PropertiesManager.getInstance().getProperty(
 					RepositoryConstants.getInstance().SR_XPATH_QRY_ID + ".1") == null)
 				xpathQueries.add("general/identifier/entry/text()");
 			else {
 				int i = 1;
 				while (PropertiesManager.getInstance().getProperty(
 						RepositoryConstants.getInstance().SR_XPATH_QRY_ID + "."
 								+ i) != null) {
 					xpathQueries
 							.add(PropertiesManager
 									.getInstance()
 									.getProperty(
 											RepositoryConstants.getInstance().SR_XPATH_QRY_ID
 													+ "." + i));
 					i++;
 				}
 			}
 			// TODO: check for valid lucene index
 
 		} catch (Throwable t) {
 			log.error("initialize: ", t);
 		}
 
 	}
 
 	public void reIndexMetadata(String outputDir, String repositories) {
 		File mdFile;
 
 		String[] repoSelected = { repositories };
 		File dir = new File(outputDir);
 
 		Vector<File> files = new Vector<>();
 
 		if (!repositories.equals("*")) {
 			repoSelected = repositories.split(",");
 
 			for (int i = 0; i < repoSelected.length; i++) {
 
 				File repo = new File(dir, repoSelected[i]);
				if (repo.exists()) {
					log.info("Resitory:" + repo.getName()
							+ " will be reindexed.");

					if (!files.contains(repo))
						files.add(repo);
					else
						log.error("Resitory:" + repo.getName()
								+ " is allready in the list to be reindexed.");
				} else
					log.error("Repository:" + repo.getName()
							+ " does not exist.");
 
 			}
		} else {
 			log.error("Indexing all repositories");
			File[] allFiles = dir.listFiles();
			for (int i = 0; i < allFiles.length; i++) {
				files.add(allFiles[i]);
			}
		}
 
 		InsertMetadataImpl[] insertImpls = InsertMetadataFactory
 				.getInsertImpl();
 		InsertMetadataLuceneImpl luceneImpl = null;
 		for (int i = 0; i < insertImpls.length; i++) {
 			InsertMetadataImpl insertImpl = insertImpls[i];
 			if (insertImpl instanceof InsertMetadataLuceneImpl)
 				luceneImpl = (InsertMetadataLuceneImpl) insertImpl;
 		}
 
 		if (luceneImpl == null)
 			return;
 
 		// luceneImpl.createLuceneIndex();
 		luceneImpl.openLuceneIndex();
 
 		String implementation = PropertiesManager.getInstance().getProperty(
 				RepositoryConstants.getInstance().MD_INSERT_IMPLEMENTATION);
 		if (implementation != null) {
 
 			for (int i = 0; i < files.size(); i++) {
 				mdFile = files.elementAt(i);
 				if (mdFile.isDirectory()) {
 					indexFile(mdFile, luceneImpl,
 							new String[] { mdFile.getName() });
 				} else {
 					indexFile(mdFile, luceneImpl, new String[] { "ARIADNE" });
 				}
 			}
 		}
 	}
 
 	public void reIndexMetadata() {
 		reIndexMetadata(dirString, repos);
 	}
 
 	private static void indexFile(File mdFile,
 			InsertMetadataLuceneImpl luceneImpl, String[] cName) {
 		String xml = null;
 		if (!mdFile.getName().equalsIgnoreCase(".DS_Store")) {
 			if (mdFile.isDirectory()) {
 
 				File[] collection = mdFile.listFiles();
 				for (int j = 0; j < collection.length; j++) {
 					if (collection[j].isDirectory()) {
 						List<String> allCnames = new ArrayList<String>(
 								Arrays.asList(cName));
 						allCnames.add(collection[j].getName());
 						String[] newcName = allCnames.toArray(new String[1]);
 						System.out.println(allCnames);
 						indexFile(collection[j], luceneImpl, newcName);
 					} else {
 						indexFile(collection[j], luceneImpl, cName);
 					}
 				}
 			} else {
 				xml = readFile(mdFile, "UTF-8");
 				try {
 
 					Document doc = getDoc(xml);
 
 					String identifier = getIdentifier(doc);
 
 					StringWriter out = new StringWriter();
 					XMLSerializer serializer = new XMLSerializer(out,
 							new OutputFormat(doc));
 					serializer.serialize((Element) doc.getFirstChild());
 					String lom = out.toString();
 
 					if (identifier != null)
 						luceneImpl.insertMetadata(identifier, lom, cName);
 				} catch (Exception e) {
 					log.error("indexFile: fileName=" + mdFile.getName(), e);
 				}
 
 			}
 		}
 	}
 
 	private static String getIdentifier(Document doc) {
 		String identifier = null;
 		for (int j = 0; j < xpathQueries.size() && identifier == null; j++) {
 			String xpathQuery = (String) xpathQueries.elementAt(j);
 			try {
 				identifier = XPathAPI.selectSingleNode(doc.getFirstChild(),
 						xpathQuery).getNodeValue();
 			} catch (Exception e) {
 				log.debug("getIdentifier", e);
 			}
 		}
 		return identifier;
 	}
 
 	private static Document getDoc(String xml) {
 		Document doc = null;
 		StringReader stringReader = new StringReader(xml);
 		InputSource input = new InputSource(stringReader);
 		try {
 			DocumentBuilderFactory factory = DocumentBuilderFactory
 					.newInstance();
 			doc = factory.newDocumentBuilder().parse(input);
 		} catch (Exception e) {
 			log.error("getDoc:", e);
 		}
 		return doc;
 	}
 
 	public static String readFile(File file, String encoding) {
 		String content = "";
 		LineIterator it;
 		try {
 			it = FileUtils.lineIterator(file, encoding);
 			while (it.hasNext()) {
 				String line = it.nextLine();
 				content = content + line + "\n";
 			}
 		} catch (IOException e) {
 			log.error("readFile: fileName=" + file.getName(), e);
 			return "";
 		}
 		LineIterator.closeQuietly(it);
 		return content;
 
 	}
 
 }
