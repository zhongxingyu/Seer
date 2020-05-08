 /**
  */
 package com.github.kanafghan.welipse.joomlagen.impl;
 
 import java.lang.reflect.InvocationTargetException;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 import java.util.Map;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.notify.NotificationChain;
 import org.eclipse.emf.common.util.BasicEList;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EClassifier;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EPackage;
 import org.eclipse.emf.ecore.InternalEObject;
 import org.eclipse.emf.ecore.impl.ENotificationImpl;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.util.EObjectContainmentEList;
 import org.eclipse.emf.ecore.util.InternalEList;
 
 import com.github.kanafghan.welipse.joomlagen.DatabaseTable;
 import com.github.kanafghan.welipse.joomlagen.ExtensionType;
 import com.github.kanafghan.welipse.joomlagen.GenAttribute;
 import com.github.kanafghan.welipse.joomlagen.GenClass;
 import com.github.kanafghan.welipse.joomlagen.GenClassifier;
 import com.github.kanafghan.welipse.joomlagen.GenFeature;
 import com.github.kanafghan.welipse.joomlagen.GenPackage;
 import com.github.kanafghan.welipse.joomlagen.GenReference;
 import com.github.kanafghan.welipse.joomlagen.JoomlaGenFactory;
 import com.github.kanafghan.welipse.joomlagen.JoomlaGenModel;
 import com.github.kanafghan.welipse.joomlagen.JoomlaGenPackage;
 import com.github.kanafghan.welipse.joomlagen.JoomlaVersion;
 import com.github.kanafghan.welipse.webdsl.Page;
 import com.github.kanafghan.welipse.webdsl.Parameter;
 import com.github.kanafghan.welipse.webdsl.Website;
 
 /**
  * <!-- begin-user-doc -->
  * An implementation of the model object '<em><b>Model</b></em>'.
  * <!-- end-user-doc -->
  * <p>
  * The following features are implemented:
  * <ul>
  *   <li>{@link com.github.kanafghan.welipse.joomlagen.impl.JoomlaGenModelImpl#getExtensionType <em>Extension Type</em>}</li>
  *   <li>{@link com.github.kanafghan.welipse.joomlagen.impl.JoomlaGenModelImpl#getJoomlaVersion <em>Joomla Version</em>}</li>
  *   <li>{@link com.github.kanafghan.welipse.joomlagen.impl.JoomlaGenModelImpl#getWebmodel <em>Webmodel</em>}</li>
  *   <li>{@link com.github.kanafghan.welipse.joomlagen.impl.JoomlaGenModelImpl#getCustomCSSFiles <em>Custom CSS Files</em>}</li>
  *   <li>{@link com.github.kanafghan.welipse.joomlagen.impl.JoomlaGenModelImpl#getDatamodel <em>Datamodel</em>}</li>
  *   <li>{@link com.github.kanafghan.welipse.joomlagen.impl.JoomlaGenModelImpl#isUseBootstrap <em>Use Bootstrap</em>}</li>
  *   <li>{@link com.github.kanafghan.welipse.joomlagen.impl.JoomlaGenModelImpl#isMinifiedBootsrap <em>Minified Bootsrap</em>}</li>
  *   <li>{@link com.github.kanafghan.welipse.joomlagen.impl.JoomlaGenModelImpl#getExtensionName <em>Extension Name</em>}</li>
  *   <li>{@link com.github.kanafghan.welipse.joomlagen.impl.JoomlaGenModelImpl#getCreationDate <em>Creation Date</em>}</li>
  *   <li>{@link com.github.kanafghan.welipse.joomlagen.impl.JoomlaGenModelImpl#getAuthor <em>Author</em>}</li>
  *   <li>{@link com.github.kanafghan.welipse.joomlagen.impl.JoomlaGenModelImpl#getAuthorEmail <em>Author Email</em>}</li>
  *   <li>{@link com.github.kanafghan.welipse.joomlagen.impl.JoomlaGenModelImpl#getAuthorURL <em>Author URL</em>}</li>
  *   <li>{@link com.github.kanafghan.welipse.joomlagen.impl.JoomlaGenModelImpl#getCopyright <em>Copyright</em>}</li>
  *   <li>{@link com.github.kanafghan.welipse.joomlagen.impl.JoomlaGenModelImpl#getLicense <em>License</em>}</li>
  *   <li>{@link com.github.kanafghan.welipse.joomlagen.impl.JoomlaGenModelImpl#getVersion <em>Version</em>}</li>
  *   <li>{@link com.github.kanafghan.welipse.joomlagen.impl.JoomlaGenModelImpl#getDescription <em>Description</em>}</li>
  *   <li>{@link com.github.kanafghan.welipse.joomlagen.impl.JoomlaGenModelImpl#getDatabaseTablePrefix <em>Database Table Prefix</em>}</li>
  *   <li>{@link com.github.kanafghan.welipse.joomlagen.impl.JoomlaGenModelImpl#getComponentName <em>Component Name</em>}</li>
  *   <li>{@link com.github.kanafghan.welipse.joomlagen.impl.JoomlaGenModelImpl#getDatabaseTables <em>Database Tables</em>}</li>
  *   <li>{@link com.github.kanafghan.welipse.joomlagen.impl.JoomlaGenModelImpl#getInitialData <em>Initial Data</em>}</li>
  * </ul>
  * </p>
  *
  * @generated
  */
 public class JoomlaGenModelImpl extends GenBaseImpl implements JoomlaGenModel {
 	/**
 	 * The default value of the '{@link #getExtensionType() <em>Extension Type</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getExtensionType()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final ExtensionType EXTENSION_TYPE_EDEFAULT = ExtensionType.COMPONENT;
 
 	/**
 	 * The cached value of the '{@link #getExtensionType() <em>Extension Type</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getExtensionType()
 	 * @generated
 	 * @ordered
 	 */
 	protected ExtensionType extensionType = EXTENSION_TYPE_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getJoomlaVersion() <em>Joomla Version</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getJoomlaVersion()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final JoomlaVersion JOOMLA_VERSION_EDEFAULT = JoomlaVersion.J25;
 
 	/**
 	 * The cached value of the '{@link #getJoomlaVersion() <em>Joomla Version</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getJoomlaVersion()
 	 * @generated
 	 * @ordered
 	 */
 	protected JoomlaVersion joomlaVersion = JOOMLA_VERSION_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getWebmodel() <em>Webmodel</em>}' reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getWebmodel()
 	 * @generated
 	 * @ordered
 	 */
 	protected Website webmodel;
 
 	/**
 	 * The default value of the '{@link #getCustomCSSFiles() <em>Custom CSS Files</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCustomCSSFiles()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String CUSTOM_CSS_FILES_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getCustomCSSFiles() <em>Custom CSS Files</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCustomCSSFiles()
 	 * @generated
 	 * @ordered
 	 */
 	protected String customCSSFiles = CUSTOM_CSS_FILES_EDEFAULT;
 
 	/**
 	 * The cached value of the '{@link #getDatamodel() <em>Datamodel</em>}' containment reference.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDatamodel()
 	 * @generated
 	 * @ordered
 	 */
 	protected GenPackage datamodel;
 
 	/**
 	 * The default value of the '{@link #isUseBootstrap() <em>Use Bootstrap</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isUseBootstrap()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean USE_BOOTSTRAP_EDEFAULT = true;
 
 	/**
 	 * The cached value of the '{@link #isUseBootstrap() <em>Use Bootstrap</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isUseBootstrap()
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean useBootstrap = USE_BOOTSTRAP_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #isMinifiedBootsrap() <em>Minified Bootsrap</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isMinifiedBootsrap()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final boolean MINIFIED_BOOTSRAP_EDEFAULT = false;
 
 	/**
 	 * The cached value of the '{@link #isMinifiedBootsrap() <em>Minified Bootsrap</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #isMinifiedBootsrap()
 	 * @generated
 	 * @ordered
 	 */
 	protected boolean minifiedBootsrap = MINIFIED_BOOTSRAP_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getExtensionName() <em>Extension Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getExtensionName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String EXTENSION_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getExtensionName() <em>Extension Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getExtensionName()
 	 * @generated
 	 * @ordered
 	 */
 	protected String extensionName = EXTENSION_NAME_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getCreationDate() <em>Creation Date</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCreationDate()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String CREATION_DATE_EDEFAULT = "";
 
 	/**
 	 * The cached value of the '{@link #getCreationDate() <em>Creation Date</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCreationDate()
 	 * @generated
 	 * @ordered
 	 */
 	protected String creationDate = CREATION_DATE_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getAuthor() <em>Author</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getAuthor()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String AUTHOR_EDEFAULT = "Welipse";
 
 	/**
 	 * The cached value of the '{@link #getAuthor() <em>Author</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getAuthor()
 	 * @generated
 	 * @ordered
 	 */
 	protected String author = AUTHOR_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getAuthorEmail() <em>Author Email</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getAuthorEmail()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String AUTHOR_EMAIL_EDEFAULT = "support@welipse.com";
 
 	/**
 	 * The cached value of the '{@link #getAuthorEmail() <em>Author Email</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getAuthorEmail()
 	 * @generated
 	 * @ordered
 	 */
 	protected String authorEmail = AUTHOR_EMAIL_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getAuthorURL() <em>Author URL</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getAuthorURL()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String AUTHOR_URL_EDEFAULT = "http://www.welipse.com";
 
 	/**
 	 * The cached value of the '{@link #getAuthorURL() <em>Author URL</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getAuthorURL()
 	 * @generated
 	 * @ordered
 	 */
 	protected String authorURL = AUTHOR_URL_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getCopyright() <em>Copyright</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCopyright()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String COPYRIGHT_EDEFAULT = "Copyright Welipse, All rights are reserved.";
 
 	/**
 	 * The cached value of the '{@link #getCopyright() <em>Copyright</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getCopyright()
 	 * @generated
 	 * @ordered
 	 */
 	protected String copyright = COPYRIGHT_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getLicense() <em>License</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLicense()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String LICENSE_EDEFAULT = "see license.txt";
 
 	/**
 	 * The cached value of the '{@link #getLicense() <em>License</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getLicense()
 	 * @generated
 	 * @ordered
 	 */
 	protected String license = LICENSE_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getVersion() <em>Version</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getVersion()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String VERSION_EDEFAULT = "0.0.1";
 
 	/**
 	 * The cached value of the '{@link #getVersion() <em>Version</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getVersion()
 	 * @generated
 	 * @ordered
 	 */
 	protected String version = VERSION_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getDescription() <em>Description</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDescription()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String DESCRIPTION_EDEFAULT = "Automatically generated Joomla! extension by Welipse.";
 
 	/**
 	 * The cached value of the '{@link #getDescription() <em>Description</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDescription()
 	 * @generated
 	 * @ordered
 	 */
 	protected String description = DESCRIPTION_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getDatabaseTablePrefix() <em>Database Table Prefix</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDatabaseTablePrefix()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String DATABASE_TABLE_PREFIX_EDEFAULT = "#__";
 
 	/**
 	 * The cached value of the '{@link #getDatabaseTablePrefix() <em>Database Table Prefix</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDatabaseTablePrefix()
 	 * @generated
 	 * @ordered
 	 */
 	protected String databaseTablePrefix = DATABASE_TABLE_PREFIX_EDEFAULT;
 
 	/**
 	 * The default value of the '{@link #getComponentName() <em>Component Name</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getComponentName()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String COMPONENT_NAME_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getDatabaseTables() <em>Database Tables</em>}' containment reference list.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getDatabaseTables()
 	 * @generated
 	 * @ordered
 	 */
 	protected EList<DatabaseTable> databaseTables;
 
 	/**
 	 * The default value of the '{@link #getInitialData() <em>Initial Data</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getInitialData()
 	 * @generated
 	 * @ordered
 	 */
 	protected static final String INITIAL_DATA_EDEFAULT = null;
 
 	/**
 	 * The cached value of the '{@link #getInitialData() <em>Initial Data</em>}' attribute.
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @see #getInitialData()
 	 * @generated
 	 * @ordered
 	 */
 	protected String initialData = INITIAL_DATA_EDEFAULT;
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	protected JoomlaGenModelImpl() {
 		super();
 		
 		Calendar now = Calendar.getInstance();
 		Locale locale = Locale.getDefault();
 		
 		String month = now.getDisplayName(Calendar.MONTH, Calendar.LONG, locale);
 		int year = now.get(Calendar.YEAR);
 		
 		// Set the default values of some of the Manifest elements
 		setCreationDate(month +" "+ year);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	protected EClass eStaticClass() {
 		return JoomlaGenPackage.Literals.JOOMLA_GEN_MODEL;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public ExtensionType getExtensionType() {
 		return extensionType;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setExtensionType(ExtensionType newExtensionType) {
 		ExtensionType oldExtensionType = extensionType;
 		extensionType = newExtensionType == null ? EXTENSION_TYPE_EDEFAULT : newExtensionType;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, JoomlaGenPackage.JOOMLA_GEN_MODEL__EXTENSION_TYPE, oldExtensionType, extensionType));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public JoomlaVersion getJoomlaVersion() {
 		return joomlaVersion;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setJoomlaVersion(JoomlaVersion newJoomlaVersion) {
 		JoomlaVersion oldJoomlaVersion = joomlaVersion;
 		joomlaVersion = newJoomlaVersion == null ? JOOMLA_VERSION_EDEFAULT : newJoomlaVersion;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, JoomlaGenPackage.JOOMLA_GEN_MODEL__JOOMLA_VERSION, oldJoomlaVersion, joomlaVersion));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Website getWebmodel() {
 		if (webmodel != null && webmodel.eIsProxy()) {
 			InternalEObject oldWebmodel = (InternalEObject)webmodel;
 			webmodel = (Website)eResolveProxy(oldWebmodel);
 			if (webmodel != oldWebmodel) {
 				if (eNotificationRequired())
 					eNotify(new ENotificationImpl(this, Notification.RESOLVE, JoomlaGenPackage.JOOMLA_GEN_MODEL__WEBMODEL, oldWebmodel, webmodel));
 			}
 		}
 		return webmodel;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public Website basicGetWebmodel() {
 		return webmodel;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * It will also retrieve the datamodel and set it
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public void setWebmodel(Website newWebmodel) {
 		Website oldWebmodel = webmodel;
 		webmodel = newWebmodel;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, JoomlaGenPackage.JOOMLA_GEN_MODEL__WEBMODEL, oldWebmodel, webmodel));
 		
 		if (webmodel == null) {
 			getDatabaseTables().clear();
 			setDatamodel(null);
 		} else {
 			if (hasDatamodel(webmodel)) {
 				// Retrieve the datamodel from webmodel and set it
 				Resource resource = webmodel.eResource();
 				if (resource != null) {
 					ResourceSet resourceSet = resource.getResourceSet();
 					if (resourceSet != null) {
 						for (Resource res : resourceSet.getResources()) {
 							if (!res.getURI().equals(resource.getURI())) {
 								EList<EObject> contents = res.getContents();
 								if (!contents.isEmpty()) {
 									EObject object = contents.get(0);
 									if (object instanceof EPackage) {
 										EPackage ePackage = (EPackage) object;							
 										GenPackage dm = JoomlaGenFactory.eINSTANCE.createGenPackage();
 										dm.initialize(ePackage);
 										setDatamodel(dm);
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 	}
 
 	private boolean hasDatamodel(Website model) {
 		for (Page page : model.getPages()) {
 			if (!page.getParameters().isEmpty()) {
 				Parameter param = page.getParameters().get(0);
 				if (param != null) {
 					EClassifier type = param.getType();
 					if (type != null) {
 						return true;
 					}
 				}
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getCustomCSSFiles() {
 		return customCSSFiles;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setCustomCSSFiles(String newCustomCSSFiles) {
 		String oldCustomCSSFiles = customCSSFiles;
 		customCSSFiles = newCustomCSSFiles;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, JoomlaGenPackage.JOOMLA_GEN_MODEL__CUSTOM_CSS_FILES, oldCustomCSSFiles, customCSSFiles));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getExtensionName() {
 		return extensionName;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public void setExtensionName(String newExtensionName) {
 		String oldExtensionName = extensionName;
 		extensionName = newExtensionName;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, JoomlaGenPackage.JOOMLA_GEN_MODEL__EXTENSION_NAME, oldExtensionName, extensionName));
 		
		setDatabaseTablePrefix(DATABASE_TABLE_PREFIX_EDEFAULT + extensionName +"_");
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getCreationDate() {
 		return creationDate;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setCreationDate(String newCreationDate) {
 		String oldCreationDate = creationDate;
 		creationDate = newCreationDate;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, JoomlaGenPackage.JOOMLA_GEN_MODEL__CREATION_DATE, oldCreationDate, creationDate));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getAuthor() {
 		return author;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setAuthor(String newAuthor) {
 		String oldAuthor = author;
 		author = newAuthor;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, JoomlaGenPackage.JOOMLA_GEN_MODEL__AUTHOR, oldAuthor, author));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getAuthorEmail() {
 		return authorEmail;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setAuthorEmail(String newAuthorEmail) {
 		String oldAuthorEmail = authorEmail;
 		authorEmail = newAuthorEmail;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, JoomlaGenPackage.JOOMLA_GEN_MODEL__AUTHOR_EMAIL, oldAuthorEmail, authorEmail));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getAuthorURL() {
 		return authorURL;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setAuthorURL(String newAuthorURL) {
 		String oldAuthorURL = authorURL;
 		authorURL = newAuthorURL;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, JoomlaGenPackage.JOOMLA_GEN_MODEL__AUTHOR_URL, oldAuthorURL, authorURL));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getCopyright() {
 		return copyright;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setCopyright(String newCopyright) {
 		String oldCopyright = copyright;
 		copyright = newCopyright;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, JoomlaGenPackage.JOOMLA_GEN_MODEL__COPYRIGHT, oldCopyright, copyright));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getLicense() {
 		return license;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setLicense(String newLicense) {
 		String oldLicense = license;
 		license = newLicense;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, JoomlaGenPackage.JOOMLA_GEN_MODEL__LICENSE, oldLicense, license));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getVersion() {
 		return version;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setVersion(String newVersion) {
 		String oldVersion = version;
 		version = newVersion;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, JoomlaGenPackage.JOOMLA_GEN_MODEL__VERSION, oldVersion, version));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getDescription() {
 		return description;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDescription(String newDescription) {
 		String oldDescription = description;
 		description = newDescription;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, JoomlaGenPackage.JOOMLA_GEN_MODEL__DESCRIPTION, oldDescription, description));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getDatabaseTablePrefix() {
 		return databaseTablePrefix;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDatabaseTablePrefix(String newDatabaseTablePrefix) {
 		String oldDatabaseTablePrefix = databaseTablePrefix;
 		databaseTablePrefix = newDatabaseTablePrefix;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, JoomlaGenPackage.JOOMLA_GEN_MODEL__DATABASE_TABLE_PREFIX, oldDatabaseTablePrefix, databaseTablePrefix));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getComponentName() {
 		return "com_" + getExtensionName();
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public void setComponentName(String newComponentName) {
 		// Not Relevant
 	}
 
 	/*
 	 * Data structures needed in order to transform datamodel
 	 * to Database tables  
 	 */
 	Map<String, DatabaseTable> tables = new HashMap<String, DatabaseTable>();
 	Map<String, GenReference> genReferences = new HashMap<String, GenReference>();
 	
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public EList<DatabaseTable> getDatabaseTables() {
 		if (databaseTables == null) {
 			databaseTables = new EObjectContainmentEList<DatabaseTable>(DatabaseTable.class, this, JoomlaGenPackage.JOOMLA_GEN_MODEL__DATABASE_TABLES);
 			
 			// Generate database tables for the provided datamodel 
 			if (reconcile()) {
 				List<GenClass> classes = new ArrayList<GenClass>(getDatamodel().getGenClasses());
 				for (GenClass genClass : classes) {
 					DatabaseTable table = tables.get(genClass.getDatabaseTableName());
 					if (table == null) {
 						table = createDatabaseTable(genClass);
 					} else {
 						// Add the attributes and references of genClass to table
 						for (GenFeature genFeature : genClass.getGenFeatures()) {
 							if (genFeature instanceof GenAttribute) {
 								createDatabaseTableColumn(table, (GenAttribute) genFeature);
 							} else {
 								createDatabaseTableForeignKey(table, (GenReference) genFeature);
 							}
 						}
 					}
 					databaseTables.add(table);
 				}
 			} else {
 				if (getDatamodel() != null) {					
 					throw new Error("Could not reconcile the datamodel.");
 				}
 			}
 		}
 		return databaseTables;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public String getInitialData() {
 		return initialData;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setInitialData(String newInitialData) {
 		String oldInitialData = initialData;
 		initialData = newInitialData;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, JoomlaGenPackage.JOOMLA_GEN_MODEL__INITIAL_DATA, oldInitialData, initialData));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated NOT
 	 */
 	public String getAssociationTableName(GenClass cls1, GenClass cls2) {
 		String name1 = generateAssociationTableName(cls1, cls2);
 		String name2 = generateAssociationTableName(cls2, cls1);
 		String result = "";
 		for (DatabaseTable table : getDatabaseTables()) {
 			if (table.getName().equals(name1)) {
 				result = name1;
 				break;
 			}
 			if (table.getName().equals(name2)) {
 				result = name2;
 				break;
 			}
 		}
 		
 		return getDatabaseTablePrefix() + result;
 	}
 
 	private DatabaseTable createDatabaseTable(GenClass genClass) {
 		DatabaseTable table = JoomlaGenFactory.eINSTANCE.createDatabaseTable();
 		table.setGenClass(genClass);
 		tables.put(genClass.getDatabaseTableName(), table);
 		
 		for (GenFeature feature : genClass.getGenFeatures()) {		
 			if (feature instanceof GenAttribute) {	
 				createDatabaseTableColumn(table, (GenAttribute) feature);
 			} else {	
 				createDatabaseTableForeignKey(table, (GenReference) feature);
 			}
 		}
 		
 		return table;
 	}
 
 	private void createDatabaseTableForeignKey(DatabaseTable table, GenReference reference) {
 		if (!reference.getEcoreReference().isDerived()) {					
 			// Check whether we have already processed this reference
 			GenReference processedRef = genReferences.get(getReferenceID(reference));
 			if (processedRef == null) {	
 				genReferences.put(getReferenceID(reference), reference);
 				if (reference.isManyToMany()) {
 					// Apply the Association Table pattern
 					applyAssociationTablePattern(table, reference);
 				} else if (reference.isOneToMany()) {
 					// Apply the Foreign Key Association pattern
 					applyForeignKeyAssociationPattern(table, reference);
 				} else {					
 					// Add table foreign keys (GenReferences)
 					table.getForeignKeys().add(reference);
 					// Remember the opposite reference (if any)
 					if (reference.getOpposite() != null) {
 						genReferences.put(getReferenceID(reference.getOpposite()), reference.getOpposite());
 					}
 				}
 			}
 		}	
 	}
 
 	private String getReferenceID(GenReference reference) {
 		return reference.getGenClass().getDatabaseTableName() +"_"+ reference.getName();
 	}
 
 	private void applyForeignKeyAssociationPattern(DatabaseTable table, GenReference reference) {
 		// The case where reference is multiplicity-many and 
 		// the opposite reference (if any) is multiplicity-one
 		if (reference.isMany()) {
 			GenClass type =  (GenClass) reference.getType();
 			
 			// Lookup the table that represents the type of the reference
 			DatabaseTable typeTable = tables.get(type.getDatabaseTableName());
 			if (typeTable == null) {
 				typeTable = createDatabaseTable(type);
 			}
 			
 			// Add the reference as foreign key to the referenced table
 			typeTable.getForeignKeys().add(reference);
 			
 			// Remember the opposite reference (if any)
 			if (reference.getOpposite() != null) {
 				genReferences.put(getReferenceID(reference.getOpposite()), reference.getOpposite());
 			}
 		} else {
 			if (reference.getOpposite() != null) {
 				// Add the opposite reference as foreign to the table
 				table.getForeignKeys().add(reference.getOpposite());
 				
 				// Remember the opposite reference
 				genReferences.put(getReferenceID(reference.getOpposite()), reference.getOpposite());
 			}
 		}
 	}
 
 	private void applyAssociationTablePattern(DatabaseTable table, GenReference reference) {
 		GenClass refGenClass = (GenClass) reference.getType(); 
 
 		// Create the association table and add the references to it
 		DatabaseTable assocTable = createAssociationTable(table.getGenClass(), refGenClass);
 		assocTable.getForeignKeys().add(reference);
 		assocTable.getForeignKeys().add(reference.getOpposite());
 		
 		// Remember the opposite reference
 		genReferences.put(getReferenceID(reference.getOpposite()), reference.getOpposite());	
 	}
 
 	private void createDatabaseTableColumn(DatabaseTable table, GenAttribute attribute) {
 		if (!attribute.getEcoreAttribute().isDerived()) {
 			for (GenAttribute genAttribute : table.getColumns()) {
 				if (genAttribute.getName().equals(attribute.getName())) return;
 			}
 			
 			// Add table columns (GenAttributes)
 			table.getColumns().add(attribute);
 		}
 	}
 
 	private DatabaseTable createAssociationTable(GenClass cls, GenClass refCls) {
 		String tableName = generateAssociationTableName(cls, refCls);
 		DatabaseTable table = tables.get(tableName);
 		if (table == null) {
 			
 			table = JoomlaGenFactory.eINSTANCE.createDatabaseTable();
 			
 			// Create (or find) the GenClass for this association table
 			GenClass genClass = null;
 			for (GenClass genCls : getDatamodel().getGenClasses()) {
 				if (genCls.getDatabaseTableName().equals(tableName)) {
 					genClass = genCls;
 				}
 			}
 			// If we could not find it, create one
 			if (genClass == null) {				
 				genClass = JoomlaGenFactory.eINSTANCE.createGenClass();
 				genClass.setDatabaseTableName(tableName);
 				genClass.setGenerateContentManager(false);
 				getDatamodel().getGenClasses().add(genClass);
 			}
 			table.setGenClass(genClass);
 			getDatabaseTables().add(table);
 			tables.put(tableName, table);
 		} else {
 			throw new Error("It appears that there are more than one many-to-many association between two classes."+
 					" Class 1: "+ cls.getItemMVCName() +", Class 2: "+ refCls.getItemMVCName());
 		}
 		
 		return table;
 	}
 	
 	private String generateAssociationTableName(GenClass cls, GenClass refCls) {
 		return cls.getDatabaseTableName() +"_"+ refCls.getDatabaseTableName() +"_map";
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseAdd(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__DATAMODEL:
 				if (datamodel != null)
 					msgs = ((InternalEObject)datamodel).eInverseRemove(this, EOPPOSITE_FEATURE_BASE - JoomlaGenPackage.JOOMLA_GEN_MODEL__DATAMODEL, null, msgs);
 				return basicSetDatamodel((GenPackage)otherEnd, msgs);
 		}
 		return super.eInverseAdd(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public NotificationChain eInverseRemove(InternalEObject otherEnd, int featureID, NotificationChain msgs) {
 		switch (featureID) {
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__DATAMODEL:
 				return basicSetDatamodel(null, msgs);
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__DATABASE_TABLES:
 				return ((InternalEList<?>)getDatabaseTables()).basicRemove(otherEnd, msgs);
 		}
 		return super.eInverseRemove(otherEnd, featureID, msgs);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public GenPackage getDatamodel() {
 		return datamodel;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public NotificationChain basicSetDatamodel(GenPackage newDatamodel, NotificationChain msgs) {
 		GenPackage oldDatamodel = datamodel;
 		datamodel = newDatamodel;
 		if (eNotificationRequired()) {
 			ENotificationImpl notification = new ENotificationImpl(this, Notification.SET, JoomlaGenPackage.JOOMLA_GEN_MODEL__DATAMODEL, oldDatamodel, newDatamodel);
 			if (msgs == null) msgs = notification; else msgs.add(notification);
 		}
 		return msgs;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setDatamodel(GenPackage newDatamodel) {
 		if (newDatamodel != datamodel) {
 			NotificationChain msgs = null;
 			if (datamodel != null)
 				msgs = ((InternalEObject)datamodel).eInverseRemove(this, JoomlaGenPackage.GEN_PACKAGE__GEN_MODEL, GenPackage.class, msgs);
 			if (newDatamodel != null)
 				msgs = ((InternalEObject)newDatamodel).eInverseAdd(this, JoomlaGenPackage.GEN_PACKAGE__GEN_MODEL, GenPackage.class, msgs);
 			msgs = basicSetDatamodel(newDatamodel, msgs);
 			if (msgs != null) msgs.dispatch();
 		}
 		else if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, JoomlaGenPackage.JOOMLA_GEN_MODEL__DATAMODEL, newDatamodel, newDatamodel));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isUseBootstrap() {
 		return useBootstrap;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setUseBootstrap(boolean newUseBootstrap) {
 		boolean oldUseBootstrap = useBootstrap;
 		useBootstrap = newUseBootstrap;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, JoomlaGenPackage.JOOMLA_GEN_MODEL__USE_BOOTSTRAP, oldUseBootstrap, useBootstrap));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public boolean isMinifiedBootsrap() {
 		return minifiedBootsrap;
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	public void setMinifiedBootsrap(boolean newMinifiedBootsrap) {
 		boolean oldMinifiedBootsrap = minifiedBootsrap;
 		minifiedBootsrap = newMinifiedBootsrap;
 		if (eNotificationRequired())
 			eNotify(new ENotificationImpl(this, Notification.SET, JoomlaGenPackage.JOOMLA_GEN_MODEL__MINIFIED_BOOTSRAP, oldMinifiedBootsrap, minifiedBootsrap));
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eGet(int featureID, boolean resolve, boolean coreType) {
 		switch (featureID) {
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__EXTENSION_TYPE:
 				return getExtensionType();
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__JOOMLA_VERSION:
 				return getJoomlaVersion();
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__WEBMODEL:
 				if (resolve) return getWebmodel();
 				return basicGetWebmodel();
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__CUSTOM_CSS_FILES:
 				return getCustomCSSFiles();
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__DATAMODEL:
 				return getDatamodel();
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__USE_BOOTSTRAP:
 				return isUseBootstrap();
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__MINIFIED_BOOTSRAP:
 				return isMinifiedBootsrap();
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__EXTENSION_NAME:
 				return getExtensionName();
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__CREATION_DATE:
 				return getCreationDate();
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__AUTHOR:
 				return getAuthor();
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__AUTHOR_EMAIL:
 				return getAuthorEmail();
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__AUTHOR_URL:
 				return getAuthorURL();
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__COPYRIGHT:
 				return getCopyright();
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__LICENSE:
 				return getLicense();
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__VERSION:
 				return getVersion();
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__DESCRIPTION:
 				return getDescription();
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__DATABASE_TABLE_PREFIX:
 				return getDatabaseTablePrefix();
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__COMPONENT_NAME:
 				return getComponentName();
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__DATABASE_TABLES:
 				return getDatabaseTables();
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__INITIAL_DATA:
 				return getInitialData();
 		}
 		return super.eGet(featureID, resolve, coreType);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public void eSet(int featureID, Object newValue) {
 		switch (featureID) {
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__EXTENSION_TYPE:
 				setExtensionType((ExtensionType)newValue);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__JOOMLA_VERSION:
 				setJoomlaVersion((JoomlaVersion)newValue);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__WEBMODEL:
 				setWebmodel((Website)newValue);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__CUSTOM_CSS_FILES:
 				setCustomCSSFiles((String)newValue);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__DATAMODEL:
 				setDatamodel((GenPackage)newValue);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__USE_BOOTSTRAP:
 				setUseBootstrap((Boolean)newValue);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__MINIFIED_BOOTSRAP:
 				setMinifiedBootsrap((Boolean)newValue);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__EXTENSION_NAME:
 				setExtensionName((String)newValue);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__CREATION_DATE:
 				setCreationDate((String)newValue);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__AUTHOR:
 				setAuthor((String)newValue);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__AUTHOR_EMAIL:
 				setAuthorEmail((String)newValue);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__AUTHOR_URL:
 				setAuthorURL((String)newValue);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__COPYRIGHT:
 				setCopyright((String)newValue);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__LICENSE:
 				setLicense((String)newValue);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__VERSION:
 				setVersion((String)newValue);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__DESCRIPTION:
 				setDescription((String)newValue);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__DATABASE_TABLE_PREFIX:
 				setDatabaseTablePrefix((String)newValue);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__COMPONENT_NAME:
 				setComponentName((String)newValue);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__DATABASE_TABLES:
 				getDatabaseTables().clear();
 				getDatabaseTables().addAll((Collection<? extends DatabaseTable>)newValue);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__INITIAL_DATA:
 				setInitialData((String)newValue);
 				return;
 		}
 		super.eSet(featureID, newValue);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public void eUnset(int featureID) {
 		switch (featureID) {
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__EXTENSION_TYPE:
 				setExtensionType(EXTENSION_TYPE_EDEFAULT);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__JOOMLA_VERSION:
 				setJoomlaVersion(JOOMLA_VERSION_EDEFAULT);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__WEBMODEL:
 				setWebmodel((Website)null);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__CUSTOM_CSS_FILES:
 				setCustomCSSFiles(CUSTOM_CSS_FILES_EDEFAULT);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__DATAMODEL:
 				setDatamodel((GenPackage)null);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__USE_BOOTSTRAP:
 				setUseBootstrap(USE_BOOTSTRAP_EDEFAULT);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__MINIFIED_BOOTSRAP:
 				setMinifiedBootsrap(MINIFIED_BOOTSRAP_EDEFAULT);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__EXTENSION_NAME:
 				setExtensionName(EXTENSION_NAME_EDEFAULT);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__CREATION_DATE:
 				setCreationDate(CREATION_DATE_EDEFAULT);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__AUTHOR:
 				setAuthor(AUTHOR_EDEFAULT);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__AUTHOR_EMAIL:
 				setAuthorEmail(AUTHOR_EMAIL_EDEFAULT);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__AUTHOR_URL:
 				setAuthorURL(AUTHOR_URL_EDEFAULT);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__COPYRIGHT:
 				setCopyright(COPYRIGHT_EDEFAULT);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__LICENSE:
 				setLicense(LICENSE_EDEFAULT);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__VERSION:
 				setVersion(VERSION_EDEFAULT);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__DESCRIPTION:
 				setDescription(DESCRIPTION_EDEFAULT);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__DATABASE_TABLE_PREFIX:
 				setDatabaseTablePrefix(DATABASE_TABLE_PREFIX_EDEFAULT);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__COMPONENT_NAME:
 				setComponentName(COMPONENT_NAME_EDEFAULT);
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__DATABASE_TABLES:
 				getDatabaseTables().clear();
 				return;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__INITIAL_DATA:
 				setInitialData(INITIAL_DATA_EDEFAULT);
 				return;
 		}
 		super.eUnset(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public boolean eIsSet(int featureID) {
 		switch (featureID) {
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__EXTENSION_TYPE:
 				return extensionType != EXTENSION_TYPE_EDEFAULT;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__JOOMLA_VERSION:
 				return joomlaVersion != JOOMLA_VERSION_EDEFAULT;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__WEBMODEL:
 				return webmodel != null;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__CUSTOM_CSS_FILES:
 				return CUSTOM_CSS_FILES_EDEFAULT == null ? customCSSFiles != null : !CUSTOM_CSS_FILES_EDEFAULT.equals(customCSSFiles);
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__DATAMODEL:
 				return datamodel != null;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__USE_BOOTSTRAP:
 				return useBootstrap != USE_BOOTSTRAP_EDEFAULT;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__MINIFIED_BOOTSRAP:
 				return minifiedBootsrap != MINIFIED_BOOTSRAP_EDEFAULT;
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__EXTENSION_NAME:
 				return EXTENSION_NAME_EDEFAULT == null ? extensionName != null : !EXTENSION_NAME_EDEFAULT.equals(extensionName);
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__CREATION_DATE:
 				return CREATION_DATE_EDEFAULT == null ? creationDate != null : !CREATION_DATE_EDEFAULT.equals(creationDate);
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__AUTHOR:
 				return AUTHOR_EDEFAULT == null ? author != null : !AUTHOR_EDEFAULT.equals(author);
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__AUTHOR_EMAIL:
 				return AUTHOR_EMAIL_EDEFAULT == null ? authorEmail != null : !AUTHOR_EMAIL_EDEFAULT.equals(authorEmail);
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__AUTHOR_URL:
 				return AUTHOR_URL_EDEFAULT == null ? authorURL != null : !AUTHOR_URL_EDEFAULT.equals(authorURL);
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__COPYRIGHT:
 				return COPYRIGHT_EDEFAULT == null ? copyright != null : !COPYRIGHT_EDEFAULT.equals(copyright);
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__LICENSE:
 				return LICENSE_EDEFAULT == null ? license != null : !LICENSE_EDEFAULT.equals(license);
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__VERSION:
 				return VERSION_EDEFAULT == null ? version != null : !VERSION_EDEFAULT.equals(version);
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__DESCRIPTION:
 				return DESCRIPTION_EDEFAULT == null ? description != null : !DESCRIPTION_EDEFAULT.equals(description);
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__DATABASE_TABLE_PREFIX:
 				return DATABASE_TABLE_PREFIX_EDEFAULT == null ? databaseTablePrefix != null : !DATABASE_TABLE_PREFIX_EDEFAULT.equals(databaseTablePrefix);
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__COMPONENT_NAME:
 				return COMPONENT_NAME_EDEFAULT == null ? getComponentName() != null : !COMPONENT_NAME_EDEFAULT.equals(getComponentName());
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__DATABASE_TABLES:
 				return databaseTables != null && !databaseTables.isEmpty();
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL__INITIAL_DATA:
 				return INITIAL_DATA_EDEFAULT == null ? initialData != null : !INITIAL_DATA_EDEFAULT.equals(initialData);
 		}
 		return super.eIsSet(featureID);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public Object eInvoke(int operationID, EList<?> arguments) throws InvocationTargetException {
 		switch (operationID) {
 			case JoomlaGenPackage.JOOMLA_GEN_MODEL___GET_ASSOCIATION_TABLE_NAME__GENCLASS_GENCLASS:
 				return getAssociationTableName((GenClass)arguments.get(0), (GenClass)arguments.get(1));
 		}
 		return super.eInvoke(operationID, arguments);
 	}
 
 	/**
 	 * <!-- begin-user-doc -->
 	 * <!-- end-user-doc -->
 	 * @generated
 	 */
 	@Override
 	public String toString() {
 		if (eIsProxy()) return super.toString();
 
 		StringBuffer result = new StringBuffer(super.toString());
 		result.append(" (extensionType: ");
 		result.append(extensionType);
 		result.append(", joomlaVersion: ");
 		result.append(joomlaVersion);
 		result.append(", customCSSFiles: ");
 		result.append(customCSSFiles);
 		result.append(", useBootstrap: ");
 		result.append(useBootstrap);
 		result.append(", minifiedBootsrap: ");
 		result.append(minifiedBootsrap);
 		result.append(", extensionName: ");
 		result.append(extensionName);
 		result.append(", creationDate: ");
 		result.append(creationDate);
 		result.append(", author: ");
 		result.append(author);
 		result.append(", authorEmail: ");
 		result.append(authorEmail);
 		result.append(", authorURL: ");
 		result.append(authorURL);
 		result.append(", copyright: ");
 		result.append(copyright);
 		result.append(", license: ");
 		result.append(license);
 		result.append(", version: ");
 		result.append(version);
 		result.append(", description: ");
 		result.append(description);
 		result.append(", databaseTablePrefix: ");
 		result.append(databaseTablePrefix);
 		result.append(", initialData: ");
 		result.append(initialData);
 		result.append(')');
 		return result.toString();
 	}
 
 	@Override
 	public boolean reconcile(JoomlaGenModel oldGenModelVersion) {
 		boolean result = false;
 		
 		if (oldGenModelVersion != null) {
 			if (getDatamodel() != null && oldGenModelVersion.getDatamodel() != null) {
 				if (getDatamodel().reconcile(oldGenModelVersion.getDatamodel())) {
 					result = true;
 				}
 			}
 			
 			reconcileSettings(oldGenModelVersion);
 		}
 		
 		return result;
 	}
 
 	protected void reconcileSettings(JoomlaGenModel oldGenModelVersion) {
 		setAuthor(oldGenModelVersion.getAuthor());
 		setAuthorEmail(oldGenModelVersion.getAuthorEmail());
 		setAuthorURL(oldGenModelVersion.getAuthorURL());
 		setCopyright(oldGenModelVersion.getCopyright());
 		setCreationDate(oldGenModelVersion.getCreationDate());
 		setCustomCSSFiles(oldGenModelVersion.getCustomCSSFiles());
 		setDatabaseTablePrefix(oldGenModelVersion.getDatabaseTablePrefix());
 		setDescription(oldGenModelVersion.getDescription());
 		setExtensionName(oldGenModelVersion.getExtensionName());
 		setExtensionType(oldGenModelVersion.getExtensionType());
 		setInitialData(oldGenModelVersion.getInitialData());
 		setJoomlaVersion(oldGenModelVersion.getJoomlaVersion());
 		setLicense(oldGenModelVersion.getLicense());
 		setMinifiedBootsrap(oldGenModelVersion.isMinifiedBootsrap());
 		setUseBootstrap(oldGenModelVersion.isUseBootstrap());
 		setVersion(oldGenModelVersion.getVersion());
 	}
 
 	@Override
 	public boolean reconcile() {
 		if (getDatamodel() != null) {
 			return getDatamodel().reconcile();
 		}
 		return false;
 	}
 
 	@Override
 	public JoomlaGenModel getGenModel() {
 		return this;
 	}
 	
 	Map<EClassifier, GenClassifier> eClassifierToGenClassifierMap;
 	
 	@Override
 	protected GenClass findGenClass(EClass eClass) {
 		if (eClassifierToGenClassifierMap == null) {
 			eClassifierToGenClassifierMap = new HashMap<EClassifier, GenClassifier>();
 		} else {
 			GenClass result = (GenClass) eClassifierToGenClassifierMap
 					.get(eClass);
 			if (result != null) {
 				return result;
 			}
 		}
 
 		EPackage ePackage = eClass.getEPackage();
 		GenPackage genPackage = getDatamodel();
 		if (genPackage != null) {
 			EPackage targetEPackage = genPackage.getEcorePackage();
 			EClassifier targetEClassifier = targetEPackage == ePackage ? eClass
 					: targetEPackage.getEClassifier(eClass.getName());
 			EList<GenClass> genClasses = genPackage.getGenClasses();
 			GenClass[] genClassesData = (GenClass[]) ((BasicEList<GenClass>) genClasses)
 					.data();
 			for (int i = 0, size = genClasses.size(); i < size; ++i) {
 				GenClass genClass = genClassesData[i];
 				if (targetEClassifier == genClass.getEcoreClass()) {
 					eClassifierToGenClassifierMap.put(eClass, genClass);
 					return genClass;
 				}
 			}
 		}
 		return null;
 	}
 
 } //JoomlaGenModelImpl
