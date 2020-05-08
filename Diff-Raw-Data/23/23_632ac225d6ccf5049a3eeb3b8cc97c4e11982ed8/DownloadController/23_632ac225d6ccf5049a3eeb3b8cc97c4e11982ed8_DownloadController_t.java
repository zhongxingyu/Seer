 /**
  * 
  */
 package org.dataportal;
 
import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.net.MalformedURLException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 import java.util.UUID;
 import java.util.concurrent.ExecutionException;
 import java.util.concurrent.ExecutorService;
 import java.util.concurrent.Executors;
 import java.util.concurrent.Future;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.xpath.XPath;
 import javax.xml.xpath.XPathConstants;
 import javax.xml.xpath.XPathFactory;
 
 import org.apache.commons.compress.archivers.ArchiveEntry;
 import org.apache.commons.compress.archivers.zip.ZipArchiveOutputStream;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.log4j.Logger;
 import org.dataportal.controllers.JPADownloadController;
 import org.dataportal.controllers.JPAUserController;
 import org.dataportal.csw.Catalog;
 import org.dataportal.csw.GetRecordById;
 import org.dataportal.csw.DataPortalNS;
 import org.dataportal.model.Download;
 import org.dataportal.model.DownloadItem;
 import org.dataportal.model.User;
 import org.dataportal.utils.DataPortalException;
 import org.dataportal.utils.Utils;
 import org.w3c.dom.Document;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 
 /**
  * Class to control the files download. Use a Cached Thread Pool to download
  * files concurrently
  * 
  * @author Micho Garcia
  * 
  */
 public class DownloadController implements DataportalCodes {
 
 	private static Logger logger = Logger.getLogger(DownloadController.class);
 
 	private static final String IDS = "//id/child::node()";
 	private static final String IDENTIFIERS = "//BriefRecord/identifier/child::node()";
 	private static final String ITEMS = "//item";
 	private static final String UNDERSCORE = "_";
 	private static final String SLASH = "/";
 
 	private static final int MARK = 1;
 
 	private static final String ZIP = ".zip";
 
 	private String tempDir;
 	private static Catalog catalogo;
 	private String id = null;
 	private JPAUserController userJPAController = null;
 	private JPADownloadController downloadJPAController = null;
 	private DataPortalNS dataPortalCtx = new DataPortalNS();
 	private DataPortalException dtException;
 
 	/**
 	 * Constructor. Reads tempDir from properties file.
 	 */
 	public DownloadController() {
 		this.tempDir = Config.get("temp.dir");
 		String url = Config.get("csw.url");
 		try {
 			catalogo = new Catalog(url);
 		} catch (MalformedURLException e) {
 			logger.error(e.getMessage());
 		}
 	}
 
 	/**
 	 * Create the pathfile with the information extracted from properties and
 	 * create the directory if not exists
 	 * 
 	 * @param userName
 	 *            User name from session (String)
 	 */
 	public String createPathFile(String userName) {
 
 		String pathFile = this.tempDir + SLASH + userName;
 		File personalDirectory = new File(pathFile);
 		boolean exists = personalDirectory.exists();
 		if (!exists) {
 			boolean createDir = personalDirectory.mkdir();
 			if (!createDir)
 				pathFile = null;
 		}
 
 		return pathFile;
 	}
 
 	/**
 	 * Extract id's from client XML request. Checks whether id's are in the
 	 * server using a GetRecordById request. If checking is OK, extract the
 	 * URL's from client XML request and sends to DownloadController to download
 	 * files
 	 * 
 	 * @param InputStream
 	 *            with the XML sends by client
 	 * @param userName
 	 *            String with user name
 	 * @return
 	 * @throws Exception
 	 */
 	public String askgn2download(InputStream isRequestXML, String userName)
 			throws Exception {
 
 		StringBuffer response = new StringBuffer();
 
		// No tengo claro esto...
		byte[] b = IOUtils.toByteArray(isRequestXML);
		InputStream aBufferedIsRequestXML = new ByteArrayInputStream(b);
		InputStream otherBufferedIsRequestXML2 = new ByteArrayInputStream(b);
 
		NodeList idNodeList = extractNodeList(aBufferedIsRequestXML, IDS);
 		ArrayList<String> requestIdes = Utils.nodeList2ArrayList(idNodeList);
 
 		GetRecordById getRecordById = new GetRecordById("brief");
 		String getRecordByIdQuery = getRecordById.createQuery(requestIdes);
 		InputStream isGetRecordByIdResponse = catalogo
 				.sendCatalogRequest(getRecordByIdQuery);
 
 		// TODO Controlar excepción retornada del servidor
 		NodeList identifierNodeList = extractNodeList(isGetRecordByIdResponse,
 				IDENTIFIERS);
 		ArrayList<String> responseIdes = Utils
 				.nodeList2ArrayList(identifierNodeList);
 
 		ArrayList<String> noIdsResponse = Utils.compare2Arraylist(requestIdes,
 				responseIdes);
 
 		if (noIdsResponse.size() != 0) {
 			logger.info("ID'S NO ENCONTRADOS: "
 					+ String.valueOf(noIdsResponse.size()) + " -> "
 					+ StringUtils.join(noIdsResponse, " : "));
 			dtException = new DataPortalException(
 					"The following dataset IDs where not found: "
 							+ StringUtils.join(noIdsResponse, ", "));
 			dtException.setCode(IDNOTFOUND);
 			throw dtException;
 		} else {
			NodeList itemsNodeList = extractNodeList(otherBufferedIsRequestXML2,
 					ITEMS);
 
 			int nItems = itemsNodeList.getLength();
 			ArrayList<DownloadItem> items = new ArrayList<DownloadItem>(nItems);
 
 			XPathFactory factory = XPathFactory.newInstance();
 			XPath xpath = factory.newXPath();
 			xpath.setNamespaceContext(dataPortalCtx);
 			for (int i = 0; i < nItems; i++) {
 				DownloadItem item = new DownloadItem();
 				Node itemDom = itemsNodeList.item(i);
 				item.setItemId(xpath.evaluate("id", itemDom));
 				item.setUrl(xpath.evaluate("data_link", itemDom));
 				item.setInstitution(xpath.evaluate("institution", itemDom));
 				item.setIcosDomain(xpath.evaluate("icos_domain", itemDom));
 				items.add(item);
 			}
 
 			// TODO Enviar volumen descarga al usuario y actuar en función
 
 			String resultDownload = downloadDatasets(items, userName);
 			response.append(resultDownload);
 		}
 
 		logger.debug("RESPONSE CONTROLLER: " + response.toString());
 
 		return response.toString();
 	}
 
 	public String getId() {
 		return this.id;
 	}
 
 	/**
 	 * 
 	 * Insert download into RDBMS
 	 * 
 	 * @param user
 	 *            User
 	 * @param fileName
 	 *            String with filename
 	 * @param urlsRequest
 	 *            url request array
 	 * @throws Exception
 	 */
 	private void insertDownload(User user, String uid, String fileName,
 			ArrayList<DownloadItem> downloadItems) throws Exception {
 
 		Timestamp timeStamp = Utils.extractDateSystemTimeStamp();
 		Download download = new Download(uid, fileName, timeStamp, user);
 		downloadJPAController = new JPADownloadController();
 		downloadJPAController.insertItems(download, downloadItems);
 	}
 
 	/**
 	 * 
 	 * Method to obtain DOI
 	 * 
 	 * @return String with DOI
 	 */
 	private String generateId() {
 		this.id = UUID.randomUUID().toString();
 		return this.id;
 	}
 
 	/**
 	 * @param inputStream
 	 * @param expresion
 	 * @return
 	 * @throws Exception
 	 */
 	private NodeList extractNodeList(InputStream inputStream, String expresion)
 			throws Exception {
 
 		NodeList nodelist;
 		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 		Document documentXML = (Document) dBuilder.parse(inputStream);
 
 		XPathFactory factory = XPathFactory.newInstance();
 		XPath xpath = factory.newXPath();
 		xpath.setNamespaceContext(dataPortalCtx);
 
 		nodelist = (NodeList) xpath.evaluate(expresion, documentXML,
 				XPathConstants.NODESET);
 
 		return nodelist;
 	}
 
 	/**
 	 * Method to download files from a url's array and make some necessary
 	 * operations like create readme file, compress files and save to rdbms the
 	 * download information
 	 * 
 	 * @param urlsRequest
 	 *            ArrayList with url's (ArrayList)
 	 * @return String with file name
 	 * @throws Exception
 	 */
 	private String downloadDatasets(ArrayList<DownloadItem> downloadItems,
 			String userName) throws Exception {
 
 		User anUser = new User(userName);
 		userJPAController = new JPAUserController();
 		User user = userJPAController.existsInto(anUser);
 
 		if (user == null) {
 			dtException = new DataPortalException(
 					"The following User where not found: " + userName);
 			dtException.setCode(USERNOTFOUND);
 			throw dtException;
 		}
 
 		StringBuffer response = new StringBuffer();
 		String pathFile = createPathFile(userName);
 		String nameFile = userName + UNDERSCORE + Utils.extractDateSystem();
 
 		if (pathFile == null) {
 			dtException = new DataPortalException("Failed to create directory");
 			dtException.setCode(FAILECREATEDIRECTORY);
 			throw dtException;
 		} else {
 			createDownloadThreads(downloadItems, pathFile);
 			String uid = this.generateId();
 			Map<String, String> params = new HashMap<String, String>();
 			params.put("ddi", uid);
 			createReadmeFile(params, pathFile);
 			compressFiles(pathFile, nameFile);
 			insertDownload(user, uid, nameFile + ZIP, downloadItems);
 
 			logger.debug("FILE to download: " + nameFile + ZIP);
 			response.append(nameFile + ZIP);
 		}
 
 		return response.toString();
 	}
 
 	/**
 	 * Create README file in user folder with download information
 	 * 
 	 * @param vars
 	 * @param pathFile
 	 * @throws IOException
 	 */
 	private void createReadmeFile(Map<String, String> vars, String pathFile)
 			throws IOException {
 		InputStream is = DownloadController.class
 				.getResourceAsStream("/README.txt");
 		String readmeString = IOUtils.toString(is, "UTF-8");
 		for (Map.Entry<String, String> var : vars.entrySet()) {
 			readmeString = readmeString.replaceAll(
 					"\\{" + var.getKey() + "\\}", var.getValue());
 		}
 		File readmeFile = new File(pathFile + "/README.txt");
 		FileUtils.writeStringToFile(readmeFile, readmeString);
 	}
 
 	/**
 	 * 
 	 * Create all Threads to download files
 	 * 
 	 * @param urlsRequest
 	 *            URL's ArrayList
 	 * @param pathFile
 	 *            path to save files
 	 * @throws MalformedURLException
 	 * @throws InterruptedException
 	 * @throws ExecutionException
 	 */
 	private void createDownloadThreads(ArrayList<DownloadItem> downloadItems,
 			String pathFile) throws MalformedURLException,
 			InterruptedException, ExecutionException {
 
 		int nItems = downloadItems.size();
 
 		ExecutorService threadsPool = Executors.newCachedThreadPool();
 		Future futures[] = new Future[nItems];
 
 		for (int i = 0; i < nItems; i++) {
 			String url = downloadItems.get(i).getUrl();
 			String name = StringUtils.substringAfterLast(url, SLASH);
 
 			DownloadCallable hiloDescarga = new DownloadCallable(url, name,
 					pathFile);
 			futures[i] = threadsPool.submit(hiloDescarga);
 
 			Thread.sleep(2000);
 		}
 
 		for (int i = 0; i < nItems; i++) {
 			String tarea = (String) futures[i].get();
 			logger.info("DOWNLOADING FINISH: " + tarea);
 		}
 	}
 
 	/**
 	 * Returns an inputStream to download the compressed file
 	 * 
 	 * @param fileName
 	 *            Name of compressed file
 	 * @param userName
 	 *            User name that generated the file
 	 * @return
 	 * @throws FileNotFoundException
 	 */
 	public InputStream getFileContents(String fileName, String userName)
 			throws FileNotFoundException {
 		File file = new File(this.tempDir + SLASH + userName + SLASH + fileName);
 		return new FileInputStream(file);
 	}
 
 	/**
 	 * Returns the downloadable file size
 	 * 
 	 * @param fileName
 	 *            Name of compressed file
 	 * @param userName
 	 *            User name that generated the file
 	 * @return
 	 * @throws FileNotFoundException
 	 */
 	public long getFileSize(String fileName, String userName)
 			throws FileNotFoundException {
 		File file = new File(this.tempDir + SLASH + userName + SLASH + fileName);
 		if (file.exists() && file.isFile())
 			return file.length();
 		else
 			return 0;
 	}
 
 	/**
 	 * 
 	 * Compress in tar format files in directory
 	 * 
 	 * @param pathDir
 	 *            directory path (String)
 	 * @param nameFile
 	 *            archive name (String)
 	 * @throws IOException
 	 */
 	private void compressFiles(String pathDir, String nameFile)
 			throws IOException {
 
 		String filePathName = pathDir + SLASH + nameFile + ".zip";
 		OutputStream os = new FileOutputStream(filePathName);
 		ZipArchiveOutputStream zipOs = new ZipArchiveOutputStream(os);
 
 		File directory = new File(pathDir);
 		String extensions[] = { "nc", "txt" };
 		Iterator<File> itFiles = FileUtils.iterateFiles(directory, extensions,
 				false);
 
 		while (itFiles.hasNext()) {
 			File fl = itFiles.next();
 			ArchiveEntry archFl = zipOs.createArchiveEntry(fl, fl.getName());
 			zipOs.putArchiveEntry(archFl);
 			zipOs.write(FileUtils.readFileToByteArray(fl));
 			zipOs.flush();
 			zipOs.closeArchiveEntry();
 
 			FileUtils.forceDelete(fl);
 		}
 
 		zipOs.finish();
 		zipOs.close();
 
 		os.close();
 	}
 
 }
