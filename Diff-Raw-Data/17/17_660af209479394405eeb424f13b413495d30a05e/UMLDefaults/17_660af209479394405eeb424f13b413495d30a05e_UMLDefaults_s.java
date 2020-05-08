 package gov.nih.nci.ncicb.cadsr.loader;
 
 import gov.nih.nci.ncicb.cadsr.dao.*;
 import gov.nih.nci.ncicb.cadsr.domain.*;
 import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;
 import gov.nih.nci.ncicb.cadsr.spring.*;
 
 import gov.nih.nci.ncicb.cadsr.loader.persister.PersisterException;
 import gov.nih.nci.ncicb.cadsr.loader.persister.LogUtil;
 
 import org.apache.log4j.Logger;
 
 import java.util.*;
 
 public class UMLDefaults {
   private static Logger logger = Logger.getLogger(UMLDefaults.class.getName());
 
   private static UMLDefaults singleton = new UMLDefaults();
 
   private LoaderDAO loaderDAO;
   private ContextDAO contextDAO;
   private ConceptualDomainDAO conceptualDomainDAO;
   private ClassificationSchemeDAO classificationSchemeDAO;
   private ClassificationSchemeItemDAO classificationSchemeItemDAO;
 
   private String projectName;
   private String projectVersion;
   private String workflowStatus;
   private Float version;
   private Context context;
   private ConceptualDomain conceptualDomain;
   private ClassificationSchemeItem domainCsi;
   private ClassificationScheme projectCs;
   private ClassSchemeClassSchemeItem projectCsCsi;
 
   // default Audit holds username
   private Audit audit;
   private Map packageCsCsis = new HashMap();
 
   private Map params = new HashMap();
 
   private Map packageFilter = new HashMap();
 
   private UMLDefaults() {
     //     logger.debug("Loading ContextDAO bean");
     contextDAO = (ContextDAO) ApplicationContextFactory.getApplicationContext()
       .getBean("contextDAO");
 
     //     logger.debug("Loading CDDAO bean");
     conceptualDomainDAO = (ConceptualDomainDAO) ApplicationContextFactory.getApplicationContext()
       .getBean("conceptualDomainDAO");
 
     //     logger.debug("Loading LoaderDAO bean");
     loaderDAO = (LoaderDAO) ApplicationContextFactory.getApplicationContext()
       .getBean("loaderDAO");
 
     //     logger.debug("Loading CSDAO bean");
     classificationSchemeDAO = (ClassificationSchemeDAO) ApplicationContextFactory.getApplicationContext()
       .getBean("classificationSchemeDAO");
 
     //     logger.debug("Loading CSIDAO bean");
     classificationSchemeItemDAO = (ClassificationSchemeItemDAO) ApplicationContextFactory.getApplicationContext()
       .getBean("classificationSchemeItemDAO");
 
   }
 
   public static UMLDefaults getInstance() {
     return singleton;
   }
 
 //   public void initParams(Map p) throws PersisterException {
   public void initParams(String projectName, String username) throws PersisterException {
 
     audit = DomainObjectFactory.newAudit();
     audit.setCreatedBy(username);
     
     this.projectName = projectName;
 
     // !!!! TODO Use version Too
     LoaderDefault loaderDefault = loaderDAO.findByName(projectName);
     
     if (loaderDefault == null) {
       throw new PersisterException(
 	"Defaults not found. Please create a profile first.");
     }
     
     String cName = loaderDefault.getContextName();
     
     if (cName == null) {
       throw new PersisterException("Context Name not Set.");
     }
     
     context = contextDAO.findByName(cName);
     
     if (context == null) {
       throw new PersisterException("Context: " + cName + " not found.");
     }
     
     version = new Float(loaderDefault.getVersion().toString());
     projectVersion = loaderDefault.getProjectVersion().toString();
     
     workflowStatus = loaderDefault.getWorkflowStatus();
     
     if (workflowStatus == null) {
       throw new PersisterException("WorkflowStatus not Set.");
     }
     
     conceptualDomain = DomainObjectFactory.newConceptualDomain();
     conceptualDomain.setPreferredName(loaderDefault.getCdName());
     
     Context cdContext = contextDAO.findByName(loaderDefault.getCdContextName());
     
     if (cdContext == null) {
       throw new PersisterException("CD Context not found.");
     }
     
     conceptualDomain.setContext(cdContext);
 
     try {
       conceptualDomain = (ConceptualDomain) conceptualDomainDAO.find(conceptualDomain)
 	.get(0);
     } catch (NullPointerException e) {
       throw new PersisterException("CD: " +
 				   conceptualDomain.getPreferredName() + " not found.");
     }
 
     String[] pkgs = loaderDefault.getPackageFilter().split(",");
     for(int i=0; i<pkgs.length; i++) {
      int ind = pkgs[i].indexOf(">");
 
       String alias = null; 
       String pkg = null;
       if(ind > 0) {
        alias = pkgs[i].substring(1, ind).trim();
        pkg = pkgs[i].substring(ind+1).trim();
       } else {
        alias = pkg = pkgs[i];
       }
       
       packageFilter.put(pkg, alias);
     }
 
   }
 
   public void initClassifications() throws PersisterException {
     domainCsi = DomainObjectFactory.newClassificationSchemeItem();
 
     // !!!! TODO
     domainCsi.setName("Essai-Domain Model");
 
     // !!!! TODO
     domainCsi.setType("TEST");
 
     List result = classificationSchemeItemDAO.find(domainCsi);
 
     if (result.size() == 0) {
       throw new PersisterException("Classification Scheme Item: " +
 				   domainCsi.getName() + " does not exist on DB.");
     }
 
     domainCsi = (ClassificationSchemeItem) result.get(0);
 
     projectCs = DomainObjectFactory.newClassificationScheme();
     projectCs.setLongName(projectName);
     projectCs.setVersion(new Float(projectVersion));
     projectCs.setContext(context);
 
     ArrayList eager = new ArrayList();
     eager.add(EagerConstants.CS_CSI);
     result = classificationSchemeDAO.find(projectCs, eager);
 
     if (result.size() == 0) { // need to add projectName CS
       projectCs.setPreferredName(projectName);
       projectCs.setWorkflowStatus(workflowStatus);
 
       // !!! TODO
       projectCs.setPreferredDefinition("Un essai de CS. Nom du projet.");
 
       // !!! TODO
       projectCs.setType("TEST");
       projectCs.setLabelType(ClassificationScheme.LABEL_TYPE_ALPHA);
 
       projectCs.setAudit(audit);
       projectCs.setId(classificationSchemeDAO.create(projectCs));
       logger.info("Added Project CS: ");
       LogUtil.logAc(projectCs, logger);
       logger.info("-- Type: " + projectCs.getType());
 
       projectCsCsi = DomainObjectFactory.newClassSchemeClassSchemeItem();
       projectCsCsi.setCs(projectCs);
       projectCsCsi.setCsi(domainCsi);
       projectCsCsi.setLabel(projectName);
       projectCsCsi.setAudit(audit);
 
       classificationSchemeDAO.addClassificationSchemeItem(projectCs,
 							  projectCsCsi);
       logger.info("Added Project CS_CSI: ");
       logger.info("-- Label: " + projectCsCsi.getLabel());
     } else { // is domainCsi linked?
       logger.info("Project CS existed");
       projectCs = (ClassificationScheme) result.get(0);
 
       List csCsis = projectCs.getCsCsis();
       boolean found = false;
 
       for (ListIterator it = csCsis.listIterator(); it.hasNext();) {
 	ClassSchemeClassSchemeItem csCsi = (ClassSchemeClassSchemeItem) it.next();
 
 	if (csCsi.getCsi().getName().equals(domainCsi.getName())) {
 	  projectCsCsi = csCsi;
 	  found = true;
 	}
       }
 
       if (!found) {
 	logger.info(
 	  "Project CS was not linked to Domain CSI -- linking it now.");
 	projectCsCsi = DomainObjectFactory.newClassSchemeClassSchemeItem();
 	projectCsCsi.setCs(projectCs);
 	projectCsCsi.setCsi(domainCsi);
 	projectCsCsi.setLabel(projectName);
 	projectCsCsi.setAudit(audit);
 
 	classificationSchemeDAO.addClassificationSchemeItem(projectCs,
 							    projectCsCsi);
       }
     }
   }
 
 //   private String projectName;
 //   private String projectVersion;
   public String getWorkflowStatus() {
     return workflowStatus;
   }
   public Context getContext() {
     return context;
   }
   public Float getVersion() {
     return version;
   }
   public ConceptualDomain getConceptualDomain() {
     return conceptualDomain;
   }
   public ClassificationSchemeItem getDomainCsi() {
     return domainCsi;
   }
   public ClassificationScheme getProjectCs() {
     return projectCs;
   }
   public ClassSchemeClassSchemeItem getProjectCsCsi() {
     return projectCsCsi;
   }
   public Audit getAudit() {
     return audit;
   }
   public Map getPackageCsCsis() {
     return packageCsCsis;
   }
   public Map getPackageFilter() {
     return packageFilter;
   }
 
 }
