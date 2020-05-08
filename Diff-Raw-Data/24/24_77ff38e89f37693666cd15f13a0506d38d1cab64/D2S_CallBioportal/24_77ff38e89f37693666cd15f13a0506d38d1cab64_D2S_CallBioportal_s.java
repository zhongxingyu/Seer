 package org.data2semantics.modules;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.List;
 import java.util.Scanner;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.apache.commons.io.comparator.LastModifiedFileComparator;
 import org.apache.commons.io.filefilter.DirectoryFileFilter;
 import org.data2semantics.recognize.D2S_BioportalClient;
 import org.data2semantics.util.D2S_Utils;
 import org.data2semantics.util.Vocab;
 import org.eclipse.jetty.util.log.Log;
 import org.jsoup.Jsoup;
 import org.jsoup.safety.Whitelist;
 import org.openrdf.model.Resource;
 import org.openrdf.model.Statement;
 import org.openrdf.model.URI;
 import org.openrdf.model.Value;
 import org.openrdf.model.ValueFactory;
 import org.openrdf.model.vocabulary.XMLSchema;
 import org.openrdf.repository.Repository;
 import org.openrdf.repository.RepositoryConnection;
 import org.openrdf.repository.RepositoryException;
 import org.openrdf.repository.RepositoryResult;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 import org.xml.sax.SAXException;
 
 /**
  * 
  * @author wibisono Class that will call the bioportal client, to provide
  *         annotations in xml format. Works on the snapshot that is already
  *         generated using D2S_CreateSnapshots.
  * 
  */
 public class D2S_CallBioportal extends AbstractModule {
 
 	private Logger log = LoggerFactory.getLogger(D2S_CallBioportal.class);
 
 	String DEFAULT_SNAPSHOT_DIRECTORY = "results/snapshots";
 	String DEFAULT_BIOPORTAL_DIRECTORY = "results/bioportal";
 	String BIOPORTAL_DIRECTORY;
 	String snapshotTimestamp = "";
 	String timestamp;
 	Vocab vocab;
 
 	D2S_CallBioportal(Repository repo, URI graph, URI resource) {
 		super(repo, graph, resource);
 
 		SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
 		timestamp = sdf.format(new Date());
 
 		vocab = new Vocab(repo.getValueFactory());
 
 		try {
 			RepositoryConnection con = repo.getConnection();
 
 			try {
 				RepositoryResult<Statement> bioportalIterator = con
 						.getStatements(resource,
 								vocab.d2s("bioportalDirectory"), null, true);
 				// Set bioportal_dir to the default value
 				String bioportalDirectoryName = DEFAULT_BIOPORTAL_DIRECTORY;
 
 				while (bioportalIterator.hasNext()) {
 					Statement s = bioportalIterator.next();
 
 					bioportalDirectoryName = s.getObject().stringValue();
 
 					// We only need one bioportal_dir location
 					break;
 				}
 
 				BIOPORTAL_DIRECTORY = bioportalDirectoryName + "/" + timestamp;
 
 				log.info("Annotations will be stored in " + BIOPORTAL_DIRECTORY);
 
 			} finally {
 				con.close();
 			}
 
 		} catch (RepositoryException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		// Check if the bioportal directory exists, if not, create one
 		File bioportal_directory = new File(BIOPORTAL_DIRECTORY);
 		if (!bioportal_directory.exists()) {
 			bioportal_directory.mkdirs();
 			log.info("Created directory " + BIOPORTAL_DIRECTORY);
 		}
 	}
 
 	// D2S_CallBioportal(String snapshotDirectory, String bioportalDirectory){
 	// File snapDir = new File(snapshotDirectory);
 	//
 	// File[] timeDirs = snapDir.listFiles((FileFilter)
 	// DirectoryFileFilter.INSTANCE);
 	//
 	// Arrays.sort(timeDirs, LastModifiedFileComparator.LASTMODIFIED_REVERSE);
 	//
 	// SNAPSHOT_DIRECTORY = timeDirs[0].getPath();
 	// System.out.println(SNAPSHOT_DIRECTORY);
 	//
 	// snapshotTimestamp =
 	// SNAPSHOT_DIRECTORY.substring(SNAPSHOT_DIRECTORY.lastIndexOf('/') + 1 );
 	//
 	// // Make sure that we have a timestamp for the annotations
 	// SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss");
 	//
 	// String timestamp = sdf.format(new Date());
 	//
 	// BIOPORTAL_OUTPUT_DIRECTORY += "/" + timestamp;
 	//
 	// File bpDir = new File(BIOPORTAL_OUTPUT_DIRECTORY);
 	// if(!bpDir.exists()){
 	// bpDir.mkdirs();
 	// }
 	//
 	// try {
 	// FileWriter writer = new FileWriter(BIOPORTAL_OUTPUT_DIRECTORY +
 	// "/snapshotTimestamp" );
 	// writer.write(snapshotTimestamp);
 	// writer.close();
 	// } catch (IOException e) {
 	// // TODO Auto-generated catch block
 	// e.printStackTrace();
 	// }
 	//
 	// System.out.println("Calling bioportal for files from directory: "+SNAPSHOT_DIRECTORY+" storing output into: "+BIOPORTAL_OUTPUT_DIRECTORY);
 	//
 	// }
 
 	public Repository start() {
 
 		try {
 			RepositoryConnection con = repo.getConnection();
 			ValueFactory vf = repo.getValueFactory();
 
 			try {
 				RepositoryResult<Statement> documentIterator = con
 						.getStatements(resource, vocab.d2s("resource"), null,
 								true);
 
 				while (documentIterator.hasNext()) {
 					Statement docStatement = documentIterator.next();
 					URI documentURI = (URI) docStatement.getObject();
 					log.info("Going to annotate document "
 							+ documentURI.stringValue());
 
 					RepositoryResult<Statement> cacheIterator = con
 							.getStatements(documentURI, vocab.d2s("hasCache"),
 									null, true);
 
 					Resource latestCacheResource = D2S_Utils.getLatest(con,
 							cacheIterator, vocab.d2s("cacheTime"));
 
 					RepositoryResult<Statement> cacheLocationIterator = con
 							.getStatements(latestCacheResource,
 									vocab.d2s("cacheLocation"), null, true);
 
 					String cacheFileName = "";
 
 					while (cacheLocationIterator.hasNext()) {
 						Statement cacheLocationStatement = cacheLocationIterator
 								.next();
 
 						cacheFileName = cacheLocationStatement.getObject()
 								.stringValue();
 
 						// We only need one cache file
 						break;
 
 					}
 
 					log.info("Cache location found at " + cacheFileName);
 					try {
 						String annotationsFileName = process(documentURI,
 								cacheFileName);
 
 						Statement annotationStatement = vf.createStatement(
								documentURI, vocab.d2s("annotationLocation"),
 								vf.createLiteral(annotationsFileName,
 										XMLSchema.STRING));
 
 						con.add(annotationStatement);
 
 					} catch (FileNotFoundException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (UnsupportedEncodingException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					} catch (InterruptedException e) {
 						// TODO Auto-generated catch block
 						e.printStackTrace();
 					}
 				}
 
 			} finally {
 				con.close();
 			}
 
 		} catch (RepositoryException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 
 		return repo;
 
 	}
 
 	private String process(URI documentURI, String cacheFileName)
 			throws FileNotFoundException, UnsupportedEncodingException,
 			InterruptedException {
 
 		String outputFilePath = BIOPORTAL_DIRECTORY + "/"
 				+ URLEncoder.encode(documentURI.stringValue(), "utf-8")
 				+ ".xml";
 		;
 
 		// IF this file already created, move on.
 		if (new File(outputFilePath).exists()) {
 			log.info("File " + outputFilePath + " already exists!");
 			return outputFilePath;
 		}
 
 		File cacheFile = new File(cacheFileName);
 		Scanner fileScanner = new Scanner(new FileInputStream(cacheFile));
 
 		StringBuilder stringBuilder = new StringBuilder();
 		D2S_BioportalClient client = new D2S_BioportalClient();
 
 		try {
 			log.info("Reading cache file " + cacheFileName);
 			while (fileScanner.hasNextLine()) {
 				stringBuilder.append(fileScanner.nextLine() + "\n");
 			}
 			log.info("Done");
 			String textToAnnotate = Jsoup.clean(stringBuilder.toString(),
 					Whitelist.none());
 			log.info("Starting annotator");
 			client.annotateToFile(textToAnnotate, "xml", new File(
 					outputFilePath));
			System.out.println("Wrote annotations to " + outputFilePath);
 		} finally {
 			fileScanner.close();
 		}
 
 		return outputFilePath;
 
 	}
 
 	// private void processHTMLFiles(String bioportalOutputDirectory, File[]
 	// htmlFiles) throws FileNotFoundException, InterruptedException {
 	//
 	// for (File currentHTMLFile : htmlFiles) {
 	// String outputName = currentHTMLFile.getName();
 	// outputName=outputName.replaceAll("html","xml");
 	// String outputFilePath = bioportalOutputDirectory+"/"+outputName;
 	//
 	// // IF this file already created, move on.
 	// if(new File(outputFilePath).exists()) continue;
 	//
 	// Scanner fileScanner = new Scanner(new FileInputStream(currentHTMLFile));
 	//
 	// StringBuilder stringBuilder = new StringBuilder();
 	// D2S_BioportalClient client = new D2S_BioportalClient();
 	//
 	// try {
 	// System.out.println("Start writing "+outputFilePath);
 	// while (fileScanner.hasNextLine()) {
 	// stringBuilder.append(fileScanner.nextLine() + "\n");
 	// }
 	// String textToAnnotate = Jsoup.clean(stringBuilder.toString(),
 	// Whitelist.none());
 	// client.annotateToFile(textToAnnotate,"xml",new File(outputFilePath));
 	// System.out.println("Done writing "+outputFilePath);
 	// } finally {
 	// fileScanner.close();
 	// }
 	// System.out.println("Going to sleep for a while... (20 secs)");
 	// Thread.sleep(20000);
 	// System.out.println("Back to work!");
 	// }
 	// }
 
 	/**
 	 * Processing xml files, getting from the body part to be annotated using
 	 * bioportal. TODO Rewrite this for the AbstractModule setup... this is
 	 * currently never called!
 	 * */
 	// private void processXMLFiles(String bioportalOutputDirectory, File[]
 	// xmlFiles)
 	// throws ParserConfigurationException, SAXException, IOException,
 	// InterruptedException {
 	//
 	// for (File currentXMLFile : xmlFiles) {
 	//
 	// // Append output- to the content file name
 	// String outputFilePath = bioportalOutputDirectory + "/" +
 	// currentXMLFile.getName();
 	//
 	// // IF this file already created, move on.
 	// if (new File(outputFilePath).exists())
 	// continue;
 	//
 	// DocumentBuilderFactory builderFactory = DocumentBuilderFactory
 	// .newInstance();
 	// DocumentBuilder builder = builderFactory.newDocumentBuilder();
 	// Document doc = builder.parse(currentXMLFile);
 	//
 	// // We are annotating only the body part
 	// NodeList list = doc.getElementsByTagName("body");
 	// Element el = (Element) list.item(0);
 	// String textContent = el.getTextContent();
 	//
 	// FileWriter writer = new FileWriter(outputFilePath);
 	// D2S_BioportalClient client = new D2S_BioportalClient();
 	// String annotationResult = client.annotateText(textContent, "xml");
 	// writer.write(annotationResult);
 	// writer.close();
 	//
 	// Thread.sleep(20000);
 	// }
 	// }
 
 	// public static void main(String[] args) throws SAXException, IOException,
 	// ParserConfigurationException, InterruptedException {
 	// if (args.length < 2) {
 	// System.out
 	// .println("Please provide the directory to read the snapshot of files to be annotated, and the output directory");
 	// return;
 	// }
 	//
 	// File snapshotDirectory = new File(args[0]);
 	// if (!snapshotDirectory.exists()) {
 	// System.out
 	// .println("Please run first mvn -P create-snapshot, since there is no snapshot to be processed");
 	// return;
 	// }
 	//
 	// File bioportalDirectory = new File(args[1]);
 	// if(!bioportalDirectory.exists()){
 	// bioportalDirectory.mkdirs();
 	// }
 	//
 	// D2S_CallBioportal bioportalCaller = new D2S_CallBioportal(args[0],
 	// args[1]);
 	// bioportalCaller.processSnapshot();
 	//
 	// }
 }
