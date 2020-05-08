 package uk.codingbadgers.bFundamentals.update;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.net.URL;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.xml.sax.SAXException;
 
 import com.google.common.io.Files;
 
 import uk.codingbadgers.bFundamentals.bFundamentals;
 import uk.codingbadgers.bFundamentals.module.Module;
 
 /**
  * The Updater for all coding badger modules.
  */
 public class BadgerUpdater extends Updater {
 
 	/**
 	 * Instantiates a new badger updater.
 	 *
 	 * @param module the module to update
 	 */
 	public BadgerUpdater(Module module) {
 		super(module, "BadgerUpdater");
 		
 		if (!module.getName().startsWith("b"))
 			throw new IllegalArgumentException("Badger updater can only be used on coding badger modules");
 		
 		try {
 			m_repository = new URL("http://repository-codingbadgers.forge.cloudbees.com/snapshot/uk/thecodingbadgers/" + module.getName());
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.codingbadgers.bFundamentals.update.Updater#checkUpdate()
 	 */
 	@Override
 	public boolean checkUpdate() {
 		boolean upToDate = true;
 		String current = m_module.getVersion();
 		String website = "";
 		
 		try {
 			URL metadata = new URL(m_repository + "/maven-metadata.xml");
 			
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 			Document doc = dBuilder.parse(metadata.openStream());
 			doc.getDocumentElement().normalize();
 			
 			Element versions = (Element) doc.getElementsByTagName("versioning").item(0);
 			
 			String latest = getTagValue("release", versions);
 			website = latest;
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (SAXException e) {
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		}
 		
 		if (website.endsWith("-SNAPSHOT")) {
 			website = website.substring(0, website.indexOf("-SNAPSHOT"));
 		}
 		
		if (current.endsWith("-SNAPSHOT")) {
			current = current.substring(0, website.indexOf("-SNAPSHOT"));
		}
		
 		String[] currentArray = current.split("\\.");
 		String[] webArray = website.split("\\.");
 		// 1.0 1.1
 		for (int i = 0; i < currentArray.length; i++) {
 			try {
 				int curPart = Integer.parseInt(currentArray[i]);
 				int webPart = Integer.parseInt(webArray[i]);
 			
 				if (curPart < webPart) {
 					upToDate = false;
 					break;
 				} else if (curPart == webPart) {
 					if (i == currentArray.length-1 && website.endsWith("-SNAPSHOT")) {
 						upToDate = true;
 						break;
 					}
 					continue;
 				}
 				break;
 			} catch (ArrayIndexOutOfBoundsException ex) {
 				return true;
 			}
 		}
 		
 		if (!upToDate) {
 			m_newVersion = website;
 			m_download = true;
 			m_log.info("Module " + m_module.getName() + " is out of date, current:" + current + " new:" + website);
 		} else {
 			m_newVersion = current;
 			m_download = false;
 			m_log.info("Module " + m_module.getName() + " isn't out of date");
 		}
 		
 		return !upToDate;
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.codingbadgers.bFundamentals.update.Updater#downloadUpdate()
 	 */
 	@Override
 	public void downloadUpdate() throws Exception {
 		if (!m_download)
 			return;
 		
 		m_log.info("Automaticaly downloading update for " + m_module.getName() + " version: " + m_newVersion);
 		String artifact = m_module.getName();
 		try {
 			URL metadata = new URL(m_repository + "/maven-metadata.xml");
 			
 			DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
 			DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
 			Document doc = dBuilder.parse(metadata.openStream());
 			doc.getDocumentElement().normalize();
 			
 			Element node = (Element) doc.getElementsByTagName("metadata").item(0);
 			
 			artifact = getTagValue("artifactId", node);
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 			return;
 		} catch (IOException e) {
 			e.printStackTrace();
 			return;
 		} catch (SAXException e) {
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 			return;
 		}
 		
 		m_downloadLink = new URL(m_repository + "/" + m_newVersion + "/" + artifact + "-" + m_newVersion + ".jar");
 		
 		File output = new File(m_downloadFolder + File.separator + m_module.getName() + ".jar");
 		if (output.exists()) {
 			output.delete();
 		}
 		output.createNewFile();
 		System.out.println(output.getAbsolutePath());
 		
 		UpdaterUtils.download(m_downloadLink, output);
 	}
 
 	/* (non-Javadoc)
 	 * @see uk.codingbadgers.bFundamentals.update.Updater#applyUpdate()
 	 */
 	@Override
 	public void applyUpdate() throws Exception{
 		m_log.info("Applying update for " + m_module.getName() + " version: " + m_newVersion);
 		File output = new File(m_downloadFolder + File.separator + m_module.getName() + ".jar");
 		if (output.exists()) {
 			return;
 		}
 		
 		File dest = new File(bFundamentals.getModuleLoader().getModuleDir() + File.separator + m_module.getFile().getName() + ".jar");
 		File backup = new File(m_backupFolder + File.separator + m_module + ".jar");
 		
 		if (dest.exists()) {
 			Files.copy(dest, backup);
 			dest.delete();
 		}
 		
 		if (backup.exists()) {
 			backup.delete();
 		}
 		
 		Files.copy(output, dest);	
 		
 		// reload the module
 		bFundamentals.getModuleLoader().unload(m_module);
 		bFundamentals.getModuleLoader().load(dest);
 	}
 
 }
