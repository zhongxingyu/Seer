 package info.freelibrary.bagit;
 
 import info.freelibrary.util.DOMUtils;
 import info.freelibrary.util.FileUtils;
 import info.freelibrary.util.I18nObject;
 import info.freelibrary.util.RegexDirFilter;
 import info.freelibrary.util.RegexFileFilter;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import java.util.Arrays;
 import java.util.Collection;
 import java.util.Comparator;
 import java.util.Date;
 
 import javax.xml.parsers.ParserConfigurationException;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import eu.medsea.mimeutil.MimeType;
 import eu.medsea.mimeutil.MimeUtil;
 import eu.medsea.mimeutil.detector.MagicMimeMimeDetector;
 
 /**
  * The <code>BagIt</code> structure for conveying files and metadata.
  * 
  * @author Kevin S. Clarke &lt;<a
  *         href="mailto:ksclarke@gmail.com">ksclarke@gmail.com</a>&gt;
  */
 public class Bag extends I18nObject implements BagConstants {
 
 	private static final String BAGIT_AUTOCLEAN_PROPERTY = "bagit.autoclean";
 
 	private static final Logger LOGGER = LoggerFactory.getLogger(Bag.class);
 
 	private static final String MIME_DETECTOR = MagicMimeMimeDetector.class
 			.getName();
 
 	private boolean isValid;
 
 	private BagInfo myBagInfo;
 
 	private Declaration myDeclaration;
 
 	private PayloadManifest myManifest;
 
 	private TagManifest myTagManifest;
 
 	private boolean myBagIsOverwritten;
 
 	File myDir;
 
 	/**
 	 * Creates a new <code>Bag</code> from scratch or by completing an existing
 	 * partial one. If a supplied <code>Bag</code> isn't valid, it attempts to
 	 * make it valid without throwing validity exceptions; if it can't create a
 	 * valid <code>Bag</code>, though, it will throw an exception.
 	 * 
	 * @param aBagDir A <code>Bag</code> directory
 	 * @throws IOException An exception indicating there was problem reading or
 	 *         writing the <code>Bag</code>
 	 */
 	public Bag(File aBag) throws IOException {
 		this(aBag, false);
 	}
 
 	/**
 	 * Creates a new <code>Bag</code> from scratch or by completing an existing
 	 * partial one. If a supplied <code>Bag</code> isn't valid, it attempts to
 	 * make it valid without throwing validity exceptions; if it can't create a
 	 * valid <code>Bag</code>, though, it will throw an exception.
 	 * 
 	 * Overwrite true only makes sense for directories, not .tar, .zip, etc.
 	 * 
	 * @param aBagDir A <code>Bag</code> directory
 	 * @throws IOException An exception indicating there was problem reading or
 	 *         writing the <code>Bag</code>
 	 */
 	public Bag(File aBag, boolean aOverwrite) throws IOException {
 		myBagIsOverwritten = aOverwrite;
 		
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug(getI18n("bagit.debug.creating"), aBag.getName());
 		}
 
 		// If we're using an existing bag, copy its contents into our dir
 		if (aBag.exists()) {
 			if (aBag.isDirectory()) {
 				myDir = !aOverwrite ? createWorkingDir(aBag) : aBag;
 
 				// We want to work on a copy rather than the original directory
 				if (!aOverwrite && !myDir.mkdirs()) {
 					throw new IOException(getI18n("bagit.dir_create", myDir));
 				}
 				
 				if (LOGGER.isDebugEnabled()) {
 					LOGGER.debug(getI18n("bagit.debug.existing"));
 				}
 
 				if (!aOverwrite) {
 					FileUtils.copy(aBag, myDir);
 				}
 			}
 			else {
 				MimeUtil.registerMimeDetector(MIME_DETECTOR);
 				Collection<?> types = MimeUtil.getMimeTypes(aBag);
 				MimeType type = MimeUtil.getMostSpecificMimeType(types);
 				String mimeType = type.toString();
 
 				if (LOGGER.isDebugEnabled()) {
 					LOGGER.debug(getI18n("bagit.debug.mime", new String[] {
 							aBag.getName(), mimeType }));
 				}
 
 				if (type.equals("application/x-gzip")) {
 					myDir = BagPackager.fromTarGz(aBag).myDir;
 					myBagIsOverwritten = true;
 				}
 				else if (type.equals("application/x-tar")) {
 					myDir = BagPackager.fromTar(aBag).myDir;
 					myBagIsOverwritten = true;
 				}
 				else if (type.equals("application/zip")) {
 					myDir = BagPackager.fromZip(aBag).myDir;
 					myBagIsOverwritten = true;
 				}
 				else if (type.equals("application/x-bzip2")) {
 					myDir = BagPackager.fromTarBZip2(aBag).myDir;
 					myBagIsOverwritten = true;
 				}
 				else {
 					String errorMessage = getI18n("bagit.unknown_mime_type",
 							new String[] { aBag.getAbsolutePath(), mimeType });
 
 					if (LOGGER.isErrorEnabled()) {
 						LOGGER.error(errorMessage);
 					}
 
 					try {
 						finalize();
 					}
 					catch (Throwable throwable) {
 						LOGGER.warn(throwable.getMessage());
 					}
 
 					throw new IOException(errorMessage);
 				}
 			}
 
 			setBagInfo(new BagInfo(myDir));
 		}
 		else {
 			myDir = !aOverwrite ? createWorkingDir(aBag) : aBag;
 
 			// We want to work on a copy rather than the original directory
 			if (!aOverwrite && !myDir.mkdirs()) {
 				throw new IOException(getI18n("bagit.dir_create", myDir));
 			}
 		}
 
 		setManifest(new PayloadManifest(myDir));
 		setTagManifest(new TagManifest(myDir));
 
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug(getI18n("bagit.debug.data_dir"));
 		}
 
 		File dataDir = new File(myDir, "data");
 		
 		if (!dataDir.exists() && !dataDir.mkdir()) {
 			throw new IOException(getI18n("bagit.dir_create", dataDir));
 		}
 	}
 
 	/**
 	 * Creates a new <code>Bag</code> from scratch or by completing an existing
 	 * partial one. If a supplied <code>Bag</code> isn't valid, it attempts to
 	 * make it valid without throwing validity exceptions; if it can't create a
 	 * valid <code>Bag</code>, though, it will throw an exception.
 	 * 
 	 * @param aBagName A name of a new or existing <code>Bag</code>
 	 * @throws IOException An exception indicating there was problem reading or
 	 *         writing the <code>Bag</code>
 	 */
 	public Bag(String aBagName) throws IOException {
 		this(new File(aBagName));
 	}
 
 	/**
 	 * Add files and directories to the <code>Bag</code> payload.
 	 * 
 	 * @param aFiles File to add to the <code>Bag</code> payload.
 	 * @throws IOException If there is difficulty writing the new files
 	 * @throws RuntimeException If the <code>Bag</code> to which we're adding
 	 *         files has already been validated
 	 */
 	public void addData(File... aFiles) throws IOException, RuntimeException {
 		File dataDir = new File(myDir, "data");
 
 		if (isValid) {
 			throw new RuntimeException(getI18n("bagit.validated"));
 		}
 
 		for (File fromFile : aFiles) {
 			if (LOGGER.isDebugEnabled()) {
 				LOGGER.debug("bagit.debug.add_data", fromFile.getName());
 			}
 
 			FileUtils.copy(fromFile, new File(dataDir, fromFile.getName()));
 		}
 	}
 
 	/**
 	 * Returns the <code>BagInfo</code> for this <code>Bag</code>.
 	 * 
 	 * @return The <code>BagInfo</code> for this <code>Bag</code>
 	 */
 	public BagInfo getBagInfo() {
 		if (myBagInfo == null) {
 			myBagInfo = new BagInfo();
 		}
 
 		return myBagInfo;
 	}
 
 	/**
 	 * Sets the <code>BagInfo</code> for this <code>Bag</code>.
 	 * 
 	 * @param aBagInfo The <code>BagInfo</code> for this <code>Bag</code>
 	 * @throws RuntimeException If the <code>Bag</code> to which the
 	 *         <code>BagInfo</code> is being added has already been validated
 	 */
 	public void setBagInfo(BagInfo aBagInfo) throws RuntimeException {
 		if (isValid) {
 			throw new RuntimeException(getI18n("bagit.validated"));
 		}
 
 		if (LOGGER.isDebugEnabled()) {
 			LOGGER.debug(getI18n("bagit.debug.setting_info"));
 		}
 
 		myBagInfo = aBagInfo;
 	}
 
 	public String toString() {
 		String eol = System.getProperty("line.separator");
 		String systemPath = myDir.getAbsolutePath();
 		StringBuilder builder = new StringBuilder();
 		File dataDir = new File(myDir, "data");
 		String dataDirXML;
 
 		DOMUtils.brokenUp(true); // pretty-print(ish) our XML
 
 		try {
 			if (LOGGER.isDebugEnabled()) {
 				LOGGER.debug(getI18n("bagit.debug.reading", dataDir));
 			}
 
 			dataDirXML = FileUtils.toXML(dataDir.getAbsolutePath(), true);
 			dataDirXML = dataDirXML.replace(systemPath + "/", "");
 		}
 		catch (ParserConfigurationException details) {
 			throw new RuntimeException(details);
 		}
 		catch (FileNotFoundException details) {
 			throw new RuntimeException(details);
 		}
 
 		builder.append("<bag name=\"").append(myDir.getName()).append("\">");
 		builder.append(eol).append(getDeclaration().toString());
 		builder.append(getBagInfo().toString());
 		builder.append(getManifest().toString());
 		builder.append(getTagManifest().toString()).append(dataDirXML);
 		builder.append("</bag>");
 
 		return builder.toString();
 	}
 
 	private File createWorkingDir(File aBagDir) {
 		String workDirPath = System.getProperty(BAGIT_WORK_DIR_PROPERTY);
 		String fileName = aBagDir.getName() + "_" + new Date().getTime();
 		File workDir;
 
 		// If we have a work directory, use that
 		if (workDirPath != null) {
 			workDir = new File(workDirPath);
 		}
 		else {
 			workDir = aBagDir.getParentFile();
 		}
 
 		// Create a temporary working directory for our bag
 		File tmpBagDir = new File(workDir, fileName);
 		return new File(tmpBagDir, aBagDir.getName());
 	}
 
 	/**
 	 * Cleans up all the temporary files created during the manipulation of the
 	 * bag. The JVM will not reliably call this method, so if the work files
 	 * should be removed, this method needs to be called explicitly or as a part
 	 * of the serialization of the bag.
 	 */
 	protected void finalize() throws Throwable {
 		String clean = System.getProperty(BAGIT_AUTOCLEAN_PROPERTY, "true");
 
 		if (clean.equals("true")) {
 			File workDir = myBagIsOverwritten ? myDir : myDir.getParentFile();
 
 			if (!workDir.exists()) {
 				return; // already cleaned up, perhaps manually
 			}
 
 			if (LOGGER.isDebugEnabled()) {
 				LOGGER.debug(getI18n("bagit.cleanup", workDir));
 			}
 
 			try {
 				RegexFileFilter fileFilter = new RegexFileFilter(".*");
 				RegexDirFilter dirFilter = new RegexDirFilter(".*");
 				File[] files = FileUtils.listFiles(workDir, fileFilter, true);
 				File[] dirs = FileUtils.listFiles(workDir, dirFilter, true);
 
 				// We have to clean-up the files first
 				for (File file : files) {
 					if (!file.delete()) {
 						throw new RuntimeException(getI18n("bagit.file_delete",
 								file));
 					}
 				}
 
 				// Before cleaning dirs we need to sort them to get files first
 				Arrays.sort(dirs, new Comparator<File>() {
 					public int compare(File a1st, File a2nd) {
 						return a2nd.getAbsolutePath().length()
 								- a1st.getAbsolutePath().length();
 					}
 				});
 
 				// Then we clean-up the directories, starting with the children
 				for (File dir : dirs) {
 					if (!dir.delete()) {
 						throw new RuntimeException(getI18n("bagit.dir_delete",
 								dir));
 					}
 				}
 
 				// Lastly, the top-level work directory can be removed
 				workDir.delete();
 			}
 			catch (IOException details) {
 				if (LOGGER.isWarnEnabled()) {
 					LOGGER.warn(getI18n("bagit.cleanup_failed"), details);
 				}
 			}
 		}
 	}
 
 	Declaration getDeclaration() {
 		if (myDeclaration == null) {
 			myDeclaration = new Declaration();
 		}
 
 		return myDeclaration;
 	}
 
 	PayloadManifest getManifest() {
 		return myManifest;
 	}
 
 	TagManifest getTagManifest() {
 		return myTagManifest;
 	}
 
 	void setDeclaration(Declaration aDeclaration) {
 		myDeclaration = aDeclaration;
 	}
 
 	void setManifest(PayloadManifest aManifest) throws RuntimeException {
 		myManifest = aManifest;
 	}
 
 	void setTagManifest(TagManifest aManifest) throws RuntimeException {
 		myTagManifest = aManifest;
 	}
 
 	void validate() {
 		isValid = true;
 		myBagInfo.isValid = true;
 	}
 
 }
