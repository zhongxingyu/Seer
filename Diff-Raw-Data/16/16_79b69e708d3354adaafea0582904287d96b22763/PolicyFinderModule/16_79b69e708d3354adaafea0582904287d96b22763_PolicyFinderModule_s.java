 /*
  * Created on Aug 12, 2004
  *
  * To change the template for this generated file go to
  * Window&gt;Preferences&gt;Java&gt;Code Generation&gt;Code and Comments
  */
 package fedora.server.security;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URI;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Vector;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.servlet.ServletContext;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import org.w3c.dom.Element;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.SAXException;
 import com.sun.xacml.AbstractPolicy;
 import com.sun.xacml.EvaluationCtx;
 import com.sun.xacml.ParsingException;
 import com.sun.xacml.Policy;
 import com.sun.xacml.PolicySet;
 import com.sun.xacml.combine.PolicyCombiningAlgorithm;
 import com.sun.xacml.ctx.Status;
 import com.sun.xacml.finder.PolicyFinder;
 import com.sun.xacml.finder.PolicyFinderResult;
 import fedora.server.ReadOnlyContext;
 import fedora.server.errors.GeneralException;
 import fedora.server.errors.ObjectNotInLowlevelStorageException;
 import fedora.server.errors.ServerException;
 import fedora.server.storage.DOManager;
 import fedora.server.storage.DOReader;
 import fedora.server.storage.types.Datastream;
 
 /**
  * @author wdn5e
  * to understand why this class is needed 
  * (why configuring the xacml pdp with all of the multiplexed policy finders just won't work),
 * @see http://sourceforge.net/mailarchive/message.php?msg_id=6068981
  */
 public class PolicyFinderModule extends com.sun.xacml.finder.PolicyFinderModule {
 	private static final Logger logger = Logger.getLogger(PolicyFinderModule.class.getName());	
 	private String combiningAlgorithm = null;
 	private PolicyFinder finder;
 	private List repositoryPolicies = null;
 	private File objectPolicyDirectory = null;
 	private File schemaFile = null;
 	private DOManager doManager;
 
 	public PolicyFinderModule(String combiningAlgorithm, String repositoryPolicyDirectoryPath, String objectPolicyDirectoryPath, DOManager doManager) throws GeneralException {
 		this.combiningAlgorithm = combiningAlgorithm;
 		List filelist = new ArrayList();
 		System.err.println("before building file list");
 		buildRepositoryPolicyFileList(new File(repositoryPolicyDirectoryPath),  filelist);
 		System.err.println("after building file list");
 		System.err.println("before getting repo policies");
 		repositoryPolicies = getRepositoryPolicies(filelist);
 		
 		File objectPolicyDirectory = new File(objectPolicyDirectoryPath);
 System.err.println("objectPolicyDirectory="+objectPolicyDirectory);
 		if (objectPolicyDirectory.isDirectory()) {
 System.err.println("is a directory");			
 			this.objectPolicyDirectory = objectPolicyDirectory;
 		} else {
 System.err.println("is NOT a directory");
 		}
 		
 		this.doManager = doManager;
 
 		String schemaName = System.getProperty(POLICY_SCHEMA_PROPERTY);
 		if (schemaName != null) {
 			schemaFile = new File(schemaName);
 		}
 	}
 
 	public static final String POLICY_SCHEMA_PROPERTY = "com.sun.xacml.PolicySchema";
 
 	public static final String JAXP_SCHEMA_LANGUAGE = "http://java.sun.com/xml/jaxp/properties/schemaLanguage";
 
 	public static final String W3C_XML_SCHEMA = "http://www.w3.org/2001/XMLSchema";
 
 	public static final String JAXP_SCHEMA_SOURCE = "http://java.sun.com/xml/jaxp/properties/schemaSource";
 	
 	private final DocumentBuilder getDocumentBuilder(ErrorHandler handler) throws ParserConfigurationException {
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		factory.setIgnoringComments(true);
 
 		DocumentBuilder builder = null;
 
 		// as of 1.2, we always are namespace aware
 		factory.setNamespaceAware(true);
 
 		if (schemaFile == null) {
 			factory.setValidating(false);
 			builder = factory.newDocumentBuilder();
 		} else {
 			factory.setValidating(true);
 			factory.setAttribute(JAXP_SCHEMA_LANGUAGE, W3C_XML_SCHEMA);
 			factory.setAttribute(JAXP_SCHEMA_SOURCE, schemaFile);
 			builder = factory.newDocumentBuilder();
 			builder.setErrorHandler(handler);
 		}
 		return builder;
 	}
 	
 	private static final AbstractPolicy getAbstractPolicyFromDOM(Element rootElement, String errorLabel) throws GeneralException {
         AbstractPolicy abstractPolicy = null;
 		String name = rootElement.getTagName();
 		try {
 			if (name.equals("Policy")) {
 				abstractPolicy = Policy.getInstance(rootElement);
 			} else if (name.equals("PolicySet")) {
 				abstractPolicy = PolicySet.getInstance(rootElement);
 			} else {
 				String msg = "bad root node for repo-wide policy in " + errorLabel;
 				logger.log(Level.INFO, msg);
 				throw new GeneralException(msg);
 			}
 		} catch (ParsingException e) {
 			String msg = "couldn't parse repo-wide policy in " + errorLabel;
 			logger.log(Level.INFO, msg);
 			throw new GeneralException(msg);
 		}
 		return abstractPolicy;
 	}
 	
 	private static int classErrors = 0;
 	public static final int getClassErrors() {
 		return classErrors;
 	}
 
 	private final int logNgo(int errors, String msg, String detail) {
         log(msg);
         if (detail != null) { 
         	log("\t" + detail);
         }
         return errors + 1;
 	}
 	
 	private final Vector getRepositoryPolicies(List filelist) throws GeneralException {
 		Vector repositoryPolicies = new Vector();
 		Iterator it = filelist.iterator();
 		int methodErrors = 0;
 		while (it.hasNext()) {
 			String filepath = (String) it.next();
 			System.err.println("filepath=" + filepath);
             File file = new File(filepath);
             if (!file.exists()) {
             	methodErrors = logNgo(methodErrors, "error loading repository-wide policy at " + filepath, "file not found");
             } 
             Element rootElement = null;
             if (methodErrors == 0) {
     			try {
     				DocumentBuilder builder = getDocumentBuilder(null);
     				rootElement = builder.parse(file).getDocumentElement();
     			} catch (ParserConfigurationException e) {
                 	methodErrors = logNgo(methodErrors, "error loading repository-wide policy at " + filepath, e.getMessage());
     			} catch (SAXException e) {
                 	methodErrors = logNgo(methodErrors, "error loading repository-wide policy at " + filepath, e.getMessage());
     			} catch (IOException e) {
                 	methodErrors = logNgo(methodErrors, "error loading repository-wide policy at " + filepath, e.getMessage());
     			}
             }
 			System.err.println("methodErrors=" + methodErrors);
             if (methodErrors == 0) {
                 AbstractPolicy abstractPolicy;
 				try {
 					System.err.println("before getting abstract policy from dom, at " + filepath);
 					abstractPolicy = getAbstractPolicyFromDOM(rootElement, filepath);
 					System.err.println("after getting abstract policy from dom");
 					repositoryPolicies.add(abstractPolicy);  
 				} catch (GeneralException e) {
                 	methodErrors = logNgo(methodErrors, "error loading repository-wide policy at " + filepath, e.getMessage());
 					e.printStackTrace();                	
 				} catch (Throwable other) {
 					System.err.println("other exception is" + other.getMessage() + " " + other);
 					other.printStackTrace(); 
 				}
             }
 		}
 		classErrors += methodErrors;
 		System.err.println("classErrors=" + classErrors);
 		if (classErrors != 0) {
 			throw new GeneralException("problems loading repo-wide policies");			
 		}
 		return repositoryPolicies;
 	}
 
 	private final AbstractPolicy getObjectPolicyFromObject(String pid) throws Throwable {
 		AbstractPolicy objectPolicyFromObject = null;
 		DOReader reader = null;
 		try {
 			reader = doManager.getReader(ReadOnlyContext.EMPTY, pid);
 		} catch (ObjectNotInLowlevelStorageException ee) {
 			// nonexistent object is not an error (action is to create the object)			
 		} catch (Throwable e) {
 			logger.log(Level.INFO, "error reading policy from xml for object " + pid);
 			throw e;
 		}
 		if (reader != null) {
 			Datastream policyDatastream = null;
 			try {
 				policyDatastream = reader.getDatastream("POLICY", null);
 			} catch (ServerException e1) {
 				// policy in object is optional and is not an error
 			}
 			if (policyDatastream != null) {
 				try {
 					InputStream instream = policyDatastream.getContentStream();
 					DocumentBuilder builder = getDocumentBuilder(null);
 					Element rootElement = builder.parse(instream).getDocumentElement();
 					objectPolicyFromObject = getAbstractPolicyFromDOM(rootElement, "object xml for " + pid);
 				} catch (Throwable e) {
 					logger.log(Level.INFO, "error reading policy from xml for object " + pid);
 					throw e;
 				}
 			}			
 		}
 		return objectPolicyFromObject;
 	}
 
 
     private AbstractPolicy getPolicyFromFile(String filepath) throws GeneralException {
 		AbstractPolicy objectPolicyFromObject = null;		
 		//String filepath = objectPolicyDirectory.getPath() + File.separator + pid.replaceAll(":", "-") + ".xml";
 System.err.println(">>>>>>>>filepath=" + filepath);
 		File file = new File(filepath);
 		if (file.exists()) {
 			if (!file.canRead()) {
 				String msg = "error reading policy from xml at " + filepath; 
 				logger.log(Level.INFO, msg);
 				throw new GeneralException(msg);			
 			}
 			try {
 				DocumentBuilder builder = getDocumentBuilder(null);
 				Element rootElement = builder.parse(file).getDocumentElement();
 				objectPolicyFromObject = getAbstractPolicyFromDOM(rootElement, "policy file from xml at " + filepath);
 			} catch (Throwable e) {
 				String msg = "error reading policy from xml from xml at " + filepath; 
 				logger.log(Level.INFO, msg);
 				throw new GeneralException(msg);
 			}			
 		}
 		return objectPolicyFromObject;
 	}
 
 	
 	private static final void buildRepositoryPolicyFileList(File directory,  List filelist) {
 		String[] files = directory.list();
 		for (int i = 0; i < files.length; i++) {
 			File file = new File(directory.getPath() + File.separator + files[i]);
 			if (file.isDirectory()) {
 				buildRepositoryPolicyFileList(file, filelist);
 			} else {
 				String temp = file.getAbsolutePath();
 				filelist.add(temp);
 			}				
 		}
 	}
 
 	/**
 	 * pass along an init() call to the various multiplexed PolicyFinderModules
 	 */
     public void init(PolicyFinder finder) {
     	this.finder = finder;
     }
 
     /**
 	 * the set of multiplexed PolicyFinderModules can support the request
 	 * if -any- of the various PolicyFinderModules individually can
 	 */
     public boolean isRequestSupported() {
         return true;
     }
     
     private static final List ERROR_CODE_LIST = new ArrayList(1); 
     static {
     	ERROR_CODE_LIST.add(Status.STATUS_PROCESSING_ERROR);    	
     }
     
     /* return a deny-biased policy set which includes all repository-wide and any object-specific policies
      */
     public PolicyFinderResult findPolicy(EvaluationCtx context) {
 		PolicyFinderResult policyFinderResult = null;
 		try {
 	    	List policies = new Vector(repositoryPolicies);
 			String pid = context.getResourceId().encode();
 			if ((pid != null) && ! "".equals(pid)) {
 		    	AbstractPolicy objectPolicyFromObject = getObjectPolicyFromObject(pid);
 		    	if (objectPolicyFromObject != null) {
 		    		policies.add(objectPolicyFromObject);
 		    	}
 				String filepath = objectPolicyDirectory.getPath() + File.separator + pid.replaceAll(":", "-") + ".xml";
 		    	AbstractPolicy objectPolicyFromFile = getPolicyFromFile(filepath);
 		    	if (objectPolicyFromFile != null) {
 		    		policies.add(objectPolicyFromFile);
 		    	} 
 			}
 			PolicyCombiningAlgorithm policyCombiningAlgorithm = (PolicyCombiningAlgorithm) Class.forName(combiningAlgorithm).newInstance();
 				//new OrderedDenyOverridesPolicyAlg();
 			PolicySet policySet = new PolicySet(new URI(""), policyCombiningAlgorithm, "", 
 					null /*no general target beyond those of multiplexed individual policies*/, policies);
     		policyFinderResult = new PolicyFinderResult(policySet); 
 		} catch (Throwable e) {			
 			e.printStackTrace();
 			policyFinderResult = new PolicyFinderResult(new Status(ERROR_CODE_LIST, e.getMessage()));
 		}
 		return policyFinderResult;
     }
 
     ServletContext servletContext = null;
     
 	private final void log(String msg) {
 		if (servletContext != null) {
 			servletContext.log(msg);
 		} else {
 			System.err.println(msg);			
 		}
 	}
 
     
 	public static void main(String[] args) {
 	}
 }
