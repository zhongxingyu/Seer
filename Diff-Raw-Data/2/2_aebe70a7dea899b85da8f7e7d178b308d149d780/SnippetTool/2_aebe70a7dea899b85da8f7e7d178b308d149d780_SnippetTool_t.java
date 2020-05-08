 package org.abratuhi.snippettool.model;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Properties;
 
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import org.abratuhi.snippettool.gui._frame_SnippetTool;
 import org.abratuhi.snippettool.util.DbUtil;
 import org.abratuhi.snippettool.util.ErrorUtil;
 import org.abratuhi.snippettool.util.FileUtil;
 import org.abratuhi.snippettool.util.PrefUtil;
 import org.abratuhi.snippettool.util.XMLUtil;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 public class SnippetTool extends Observable {
 	private static final Logger logger = LoggerFactory.getLogger(SnippetTool.class);
 
 	/** Default .properties file **/
 	public final static String DEFAULT_PROPERTIES_FILE = "snippet-tool.properties";
 
 	/** Default .preferences file **/
 	public final static String PREFERENCES_FILE = "snippet-tool.preferences";
 
 	/** Backup .preferences file **/
 	public final static String BACKUP_PREFERENCES_FILE = "data/snippet-tool.preferences";
 
 	/** Application is initialized together with an Inscript Object **/
 	public Inscript inscript;
 	public _frame_SnippetTool gui;
 
 	public Properties props;
 	public Properties prefs;
 
 	public boolean existingSign = false;
 	private double scale = 1.0;
 	public double scaleFactor = 1.1;
 
 	/**
 	 * Creates a new SnipppetTool instance which uses the specified
 	 * propertiesFile to initialize itself.
 	 * 
 	 * @param propertiesFile
 	 *            the properties file to read
 	 */
 	public SnippetTool(String propertiesFile) {
 		props = PrefUtil.loadProperties(propertiesFile);
 
 		if (new File(PREFERENCES_FILE).canRead())
 			prefs = PrefUtil.loadProperties(PREFERENCES_FILE);
 		else
 			prefs = PrefUtil.loadProperties(BACKUP_PREFERENCES_FILE);
 
 		inscript = new Inscript();
 		inscript.setFont(props.getProperty("local.font.file"));
 	}
 
 	/**
 	 * Creates a new SnipppetTool instance which uses the default properties
 	 * file to initialize itself.
 	 */
 	public SnippetTool() {
 		this(DEFAULT_PROPERTIES_FILE);
 	}
 
 	public void loadInscriptTextFromRemoteResource(String collection, String resource) throws Exception {
 		String user = props.getProperty("db.data.user");
 		String password = props.getProperty("db.data.password");
 		String xml_temp_dir = props.getProperty("local.inscript.dir");
 
 		inscript.setId(resource.substring(0, resource.length() - ".xml".length()));
 		inscript.setPath(collection + resource);
 
 		setInscriptText(DbUtil.downloadXMLResource(collection, resource, user, password, xml_temp_dir));
 
 	}
 
 	public void loadInscriptTextFromLocalFile(File file) throws Exception {
 		String name = file.getName();
 		inscript.setId(name.substring(0, name.length() - ".xml".length()));
 		inscript.setPath(file.getCanonicalPath());
 
 		setInscriptText(file);
 	}
 
 	private void setInscriptText(final File inscriptFile) throws Exception {
		logger.debug("Loading inscript text from {}", inscriptFile);
 
 		String xsltFilename = props.getProperty("local.xslt.file");
 
 		TransformerFactory tFactory = TransformerFactory.newInstance();
 		Source xslSource = new StreamSource(new File(xsltFilename));
 
 		Transformer transformer = tFactory.newTransformer(xslSource);
 
 		StringWriter sw = new StringWriter();
 		transformer.transform(new StreamSource(inscriptFile), new StreamResult(sw));
 
 		String transformedInscriptText = sw.toString();
 
 		if (logger.isDebugEnabled()) {
 			FileUtil.writeXMLStringToFile(new File("2.xml"), transformedInscriptText);
 		}
 
 		String standardizedText = XMLUtil.standardizeXML(transformedInscriptText);
 
 		if (logger.isDebugEnabled()) {
 			FileUtil.writeXMLStringToFile(new File("3.xml"), standardizedText);
 		}
 
 		inscript.setTextFromXML(standardizedText);
 	}
 
 	public void setInscriptImageToLocalFile(File file) {
 		inscript.setAbsoluteRubbingPath(file.getPath());
 		inscript.loadLocalImage(file);
 		setScale(1.0f);
 	}
 
 	public void setInscriptImageToRemoteRessource(String url) {
 		String user = props.getProperty("db.data.user");
 		String password = props.getProperty("db.data.password");
 		String image_temp_dir = props.getProperty("local.image.dir");
 
 		String collection = url.substring(0, url.lastIndexOf("/"));
 		String resource = url.substring(url.lastIndexOf("/") + 1);
 
 		try {
 			File image = DbUtil.downloadBinaryResource(collection, resource, user, password, image_temp_dir);
 			inscript.loadLocalImage(image);
 			inscript.setAbsoluteRubbingPath(url);
 			setScale(1.0f);
 		} catch (IOException e) {
 			logger.error("IOException occurred in " + "setInscriptImageToRemoteRessource", e);
 			ErrorUtil.showError(gui, "I/O error while loading image", e);
 		}
 
 	}
 
 	public void updateInscriptImagePathFromAppearances() {
 		String collection = props.getProperty("db.unicode.dir");
 		String user = props.getProperty("db.unicode.user");
 		String password = props.getProperty("db.unicode.password");
 		String query = "//appearance[contains(@id, '" + inscript.getId() + "_')]/rubbing/text()";
 
 		String[] paths = DbUtil.convertResourceSetToStrings(DbUtil.executeQuery(collection, user, password, query));
 		if (paths.length > 0) {
 			String rubbingPath = paths[0];
 			if (rubbingPath.startsWith("xmldb:")) {
 				logger.warn("Rubbing path {} is absolute.", rubbingPath);
 				rubbingPath = rubbingPath.replaceFirst("xmldb:.*?/db/", props.getProperty("db.data.uri"));
 				logger.debug("Mapping to {}", rubbingPath);
 			} else {
 				rubbingPath = props.getProperty("db.data.uri") + rubbingPath;
 			}
 			inscript.setAbsoluteRubbingPath(rubbingPath);
 		}
 	}
 
 	public void updateInscriptCoordinates() {
 		String collection = props.getProperty("db.unicode.dir");
 		String user = props.getProperty("db.unicode.user");
 		String password = props.getProperty("db.unicode.password");
 		String query = "//appearance[source='" + inscript.getId() + "'][@variant='0']";
 
 		Element[] appearances = DbUtil.convertResourceSetToElements(DbUtil.executeQuery(collection, user, password,
 				query));
 		inscript.updateCoordinates(appearances);
 	}
 
 	public void submitInscript() {
 		submitInscriptSnippets("snippet");
 		submitInscriptCoordinates();
 	}
 
 	private void submitInscriptCoordinates() {
 		String uri = props.getProperty("db.unicode.uri");
 		String collection = props.getProperty("db.unicode.dir");
 		String user = props.getProperty("db.unicode.user");
 		String password = props.getProperty("db.unicode.password");
 
 		// XMLUtil.clearAppearances(user, password, collection,
 		// inscript.getId());
 		XMLUtil.updateXML(inscript.getXUpdate("/db/" + collection.substring(uri.length())), user, password, collection);
 	}
 
 	public void submitInscriptSnippets(String snippetBasename) {
 		String collection = props.getProperty("db.snippet.dir");
 		String user = props.getProperty("db.snippet.user");
 		String password = props.getProperty("db.snippet.password");
 
 		String snippetdir = props.getProperty("local.snippet.dir");
 
 		if (!snippetdir.endsWith(File.separator))
 			snippetdir += File.separator;
 
 		File[] preferredSnippets;
 		try {
 			preferredSnippets = inscript.getPyramidalImage().cutSnippets(inscript.getPreferredReadingText(),
 					snippetdir, "subimage");
 
 			DbUtil.uploadBinaryResources(preferredSnippets, collection, user, password);
 			for (int i = 0; i < preferredSnippets.length; i++) {
 				File snippet = preferredSnippets[i];
 				if (snippet != null) {
 					inscript.updatePathToSnippet(snippet.getName(), i);
 				}
 			}
 		} catch (IOException e) {
 			logger.error("I/O error while cutting snippets", e);
 			ErrorUtil.showError(gui, "I/O error while cutting snippets", e);
 		}
 	}
 
 	public void clearInscript() {
 		inscript.clear();
 	}
 
 	public void saveLocal() {
 		saveLocalCoordinates();
 		saveLocalSnippets("tcut");
 	}
 
 	public void saveLocalCoordinates() {
 		String unicodedir = props.getProperty("local.unicode.dir");
 		if (!unicodedir.endsWith(File.separator))
 			unicodedir += File.separator;
 
 		Document document = new Document(new Element("inscript").setAttribute("id", inscript.getId()).setAttribute(
 				"xml", inscript.getPath()).setAttribute("img", inscript.getAbsoluteRubbingPath()));
 
 		for (int i = 0; i < inscript.getText().size(); i++) {
 			for (int j = 0; j < inscript.getText().get(i).size(); j++) {
 				for (int k = 0; k < inscript.getText().get(i).get(j).size(); k++) {
 					InscriptCharacter csign = inscript.getText().get(i).get(j).get(k);
 					document.getRootElement().addContent(csign.toAppearance());
 				}
 			}
 		}
 
 		FileUtil.writeXMLDocumentToFile(new File(unicodedir + "tmarking_" + inscript.getId() + ".xml"), document);
 	}
 
 	public void saveLocalSnippets(String snippetBasename) {
 		String snippetdir = props.getProperty("local.snippet.dir");
 		String imagedir = props.getProperty("local.image.dir");
 
 		if (!snippetdir.endsWith(File.separator))
 			snippetdir += File.separator;
 		if (!imagedir.endsWith(File.separator))
 			imagedir += File.separator;
 
 		List<InscriptCharacter> preferredReading = new ArrayList<InscriptCharacter>();
 		for (int i = 0; i < inscript.getText().size(); i++) {
 			preferredReading.add(inscript.getText().get(i).get(0).get(0));
 		}
 
 		try {
 			inscript.getPyramidalImage().cutSnippets(preferredReading, snippetdir, snippetBasename);
 		} catch (IOException e) {
 			logger.error("I/O error while cutting snippets", e);
 			ErrorUtil.showError(gui, "I/O error while cutting snippets", e);
 		}
 	}
 
 	@SuppressWarnings("unchecked")
 	public void loadLocal(File f) throws Exception {
 		if (f == null)
 			return;
 
 		Document document = FileUtil.readXMLDocumentFromFile(f);
 		Element documentRootElement = document.getRootElement();
 		String xml = documentRootElement.getAttributeValue("xml");
 		String img = documentRootElement.getAttributeValue("img");
 
 		if (xml.startsWith("xmldb:")) {
 			String xml_collection = xml.substring(0, xml.lastIndexOf("/"));
 			String xml_resource = xml.substring(xml.lastIndexOf("/") + 1);
 			loadInscriptTextFromRemoteResource(xml_collection, xml_resource);
 		} else {
 			loadInscriptTextFromLocalFile(new File(xml));
 		}
 
 		if (img.startsWith("xmldb:")) {
 			setInscriptImageToRemoteRessource(img);
 		} else {
 			setInscriptImageToLocalFile(new File(img));
 		}
 		List<Element> apps = documentRootElement.getChildren("appearance");
 		inscript.updateCoordinates(apps.toArray(new Element[apps.size()]));
 
 	}
 
 	public void exit() {
 		// PrefUtil.saveProperties(props, propfile);
 		PrefUtil.saveProperties(prefs, PREFERENCES_FILE);
 		System.exit(0);
 	}
 
 	/**
 	 * @param scale
 	 *            the scale to set
 	 */
 	public void setScale(double scale) {
 		this.scale = scale;
 		setChanged();
 		notifyObservers();
 	}
 
 	/**
 	 * @return the scale
 	 */
 	public double getScale() {
 		return scale;
 	}
 
 }
