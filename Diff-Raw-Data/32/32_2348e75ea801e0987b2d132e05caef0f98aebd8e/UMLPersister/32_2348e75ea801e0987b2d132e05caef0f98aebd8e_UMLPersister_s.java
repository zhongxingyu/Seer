 package gov.nih.nci.ncicb.cadsr.loader.persister;
 
 import gov.nih.nci.ncicb.cadsr.loader.ElementsLists;
 
 import gov.nih.nci.ncicb.cadsr.domain.*;
 import gov.nih.nci.ncicb.cadsr.dao.*;
 
 import gov.nih.nci.ncicb.cadsr.spring.*;
 
 import java.util.*;
 
 public class UMLPersister implements Persister {
 
   private ElementsLists elements = null;
 
   private AdminComponentDAO adminComponentDAO;
   private DataElementDAO dataElementDAO;
   private ContextDAO contextDAO;
   private DataElementConceptDAO dataElementConceptDAO;
   private ValueDomainDAO valueDomainDAO; 
   private ConceptualDomainDAO conceptualDomainDAO;
   private PropertyDAO propertyDAO;
   private ObjectClassDAO objectClassDAO;
   private ClassificationSchemeDAO classificationSchemeDAO;
   private ClassificationSchemeItemDAO classificationSchemeItemDAO;
 
   private HashMap params = new HashMap();
 
   
   private String projectName, version, workflowStatus;
   private Context context;
   private ConceptualDomain conceptualDomain;
 
   private ClassificationSchemeItem projectCsi, versionCsi;
   private ClassificationScheme domainCs;
   private ClassSchemeClassSchemeItem projectCsCsi, versionCsCsi;
 
 
   public UMLPersister(ElementsLists list) {
     this.elements = list;
 
     ApplicationContextFactory.init("applicationContext.xml");
 
 
     System.out.println("Loading ContextDAO bean");
     contextDAO = (ContextDAO) ApplicationContextFactory.getApplicationContext().getBean("contextDAO");
 
 
 //     System.out.println("Loading DataElementDAO bean");
 //     dataElementDAO = (DataElementDAO) ApplicationContextFactory.getApplicationContext().getBean("dataElementDAO");
 
     System.out.println("Loading AdminComponentDAO bean");
     adminComponentDAO = (AdminComponentDAO) ApplicationContextFactory.getApplicationContext().getBean("adminComponentDAO");
 
     System.out.println("Loading DataElementConceptDAO bean");
     dataElementConceptDAO = (DataElementConceptDAO) ApplicationContextFactory.getApplicationContext().getBean("dataElementConceptDAO");
 
     System.out.println("Loading CDDAO bean");
     conceptualDomainDAO = (ConceptualDomainDAO) ApplicationContextFactory.getApplicationContext().getBean("conceptualDomainDAO");
 
     System.out.println("Loading VDDAO bean");
     valueDomainDAO = (ValueDomainDAO) ApplicationContextFactory.getApplicationContext().getBean("valueDomainDAO");
 
     System.out.println("Loading PropertyDAO bean");
     propertyDAO = (PropertyDAO) ApplicationContextFactory.getApplicationContext().getBean("propertyDAO");
 
     System.out.println("Loading ObjectClassDAO bean");
     objectClassDAO = (ObjectClassDAO) ApplicationContextFactory.getApplicationContext().getBean("objectClassDAO");
 
     System.out.println("Loading CSDAO bean");
     classificationSchemeDAO = (ClassificationSchemeDAO) ApplicationContextFactory.getApplicationContext().getBean("classificationSchemeDAO");
 
     System.out.println("Loading CSIDAO bean");
     classificationSchemeItemDAO = (ClassificationSchemeItemDAO) ApplicationContextFactory.getApplicationContext().getBean("classificationSchemeItemDAO");
 
   }
 
   public void setParameter(String key, Object value) {
     params.put(key, value);
   }
 
   public void persist() throws PersisterException {
 
     initParams();
     
     initClassifications();
 
     persistProperties();
     
     persistObjectClasses();
 
     persistDecs();
 
   }
 
 
   public void persistDes() throws PersisterException {
     DataElement de = DomainObjectFactory.newDataElement();
     List des = (List)elements.getElements(de.getClass());
     
     if(des != null)
       for(int i=0; i<des.size(); i++) {
 	de = (DataElement)des.get(i);
 
 
       }
 
   }
 
   // !!!! TODO 
   // EVS CONCEPT CODE.
   private void persistProperties() throws PersisterException {
 
     Property prop = DomainObjectFactory.newProperty();
     List props = (List)elements.getElements(prop.getClass());
 
     if(props != null)
      for(int i=0; i<props.size(); i++) {
	prop = (Property)props.get(i);
 
	elements.removeElement(prop);
	
 	prop.setContext(context);
 	
 	// does this property exist?
 	List l = propertyDAO.find(prop);
 	if(l.size() == 0) {
 	  // !!!!! TODO
 	  prop.setPreferredDefinition(prop.getLongName());
 	  prop.setPreferredName(prop.getLongName());
 	  
 	  prop.setVersion(new Float(1.0f));
 	  
 	  prop.setWorkflowStatus(workflowStatus);
 	  prop.setId(propertyDAO.create(prop));
 	} else {
 	  prop = (Property)l.get(0);
 	}	
 	
 	addClassificationSchemes(prop);
	elements.addElement(prop);
       }
 
   }
 
   
   private void persistObjectClasses() throws PersisterException {
 
     ObjectClass oc = DomainObjectFactory.newObjectClass();
     List ocs = (List)elements.getElements(oc.getClass());
 
     System.out.println("ocs...");
     
     if(ocs != null)
      for(int i=0; i<ocs.size(); i++) {
	oc = (ObjectClass)ocs.get(i);

	elements.removeElement(oc);
	
 	oc.setContext(context);
 
 	String className = oc.getLongName();
 	int ind = className.lastIndexOf(".");
 	String packageName = className.substring(0, ind - 1);
 	className = className.substring(ind + 1);
 	
 	oc.setLongName(className);
 	// does this oc exist?
 	List l = objectClassDAO.find(oc);
 	if(l.size() == 0) {
 	  // !!!!! TODO
 	  oc.setPreferredDefinition(oc.getLongName());
 	  oc.setPreferredName(oc.getLongName());
 
 	  oc.setVersion(new Float(1.0f));
 
 	  oc.setWorkflowStatus(workflowStatus);
 	  oc.setId(objectClassDAO.create(oc));
 	} else {
 	  oc = (ObjectClass)l.get(0);
 	}	
 
 	addClassificationSchemes(oc);
	elements.addElement(oc);
 
 	// add designation to hold package name
 	// !!!! TODO
 	
       }
 
   }
 
   private void persistDecs() throws PersisterException {
     DataElementConcept dec = DomainObjectFactory.newDataElementConcept();
     List decs = (List)elements.getElements(dec.getClass());
     
     System.out.println("decs: " + decs.size());
 
     if(decs != null)
      for(int i=0; i<decs.size(); i++) {
	dec = (DataElementConcept)decs.get(i);
	elements.removeElement(dec);
 	
 	dec.setContext(context);
 	dec.setConceptualDomain(conceptualDomain);
 
 	int ind = dec.getLongName().lastIndexOf(".");
 	if(ind > 0)
 	  dec.setLongName(dec.getLongName().substring(ind+1));
 
 	System.out.println("dec name: " + dec.getLongName());
 
 	// does this dec exist?
 	List l = dataElementConceptDAO.find(dec);
 	if(l.size() == 0) {
 	  // !!!!! TODO
 	  dec.setPreferredDefinition(dec.getLongName());
 	  dec.setPreferredName(dec.getLongName());
 

 	  dec.setVersion(new Float(1.0f));
 
 	  dec.setWorkflowStatus(workflowStatus);
 
 	  List ocs = elements.getElements(DomainObjectFactory.newObjectClass().getClass());
 	  for(int j=0; j<ocs.size(); j++) {
 	    ObjectClass o = (ObjectClass)ocs.get(j);
 	    if(o.getLongName().equals(dec.getObjectClass().getLongName()))
 	      dec.setObjectClass(o);
 	  }
 
 	  List props = elements.getElements(DomainObjectFactory.newProperty().getClass());
 	  for(int j=0; j<props.size(); j++) {
 	    Property o = (Property)props.get(j);
 	    if(o.getLongName().equals(dec.getProperty().getLongName()))
 	      dec.setProperty(o);
 	  }
 
 	  
 	  dec.setId(dataElementConceptDAO.create(dec));
 	} else {
 	  dec = (DataElementConcept)l.get(0);
 	}	
 
 	addClassificationSchemes(dec);
	elements.addElement(dec);
 
 	// add designation to hold package name
 	// !!!! TODO
 	
       }
 
   }
 
 
   private void addClassificationSchemes(AdminComponent ac) throws PersisterException {
 
     // Add Classification Schemes
     List l = adminComponentDAO.getClassSchemeClassSchemeItems(ac);
     
     // are projectCsi and versionCsi linked?
     boolean pfound = false, vfound = false;
     
     for(int i=0; i<l.size(); i++) {
       Object o = l.get(i);
       ClassSchemeClassSchemeItem csCsi = (ClassSchemeClassSchemeItem)l.get(i);
 
       if(csCsi.getCs().getLongName().equals(domainCs.getLongName()))
 	if(csCsi.getCsi().getName().equals(projectCsi.getName()))
 	  pfound = true;
 	else if(csCsi.getCsi().getName().equals(versionCsi.getName()))
 	  vfound = true;
       
 	}
     
     l = new ArrayList();
     if(!pfound) {
       l.add(projectCsCsi);
     }
     
     if(!vfound) {
 	  l.add(versionCsCsi);
     }
 	
     if(l.size() > 0)
       adminComponentDAO.addClassSchemeClassSchemeItems(ac, l);
     
     
   }
   
   private void initParams() throws PersisterException {
 
     String cName = (String)params.get("contextName");
     if(cName == null)
       throw new PersisterException("Context Name not Set.");
 
     context = contextDAO.findByName(cName);
     if(context == null)
       throw new PersisterException("Context: " + cName + " not found.");
 
 
     projectName = (String)params.get("projectName");
     version = (String)params.get("version");
     if(projectName == null || version == null)
       throw new PersisterException("Project Name / Version not Set.");
 
 
     workflowStatus = (String)params.get("workflowStatus");
     if(workflowStatus == null)
       throw new PersisterException("WorkflowStatus not Set.");
 
     
     conceptualDomain = DomainObjectFactory.newConceptualDomain();
     conceptualDomain.setLongName((String)params.get("conceptualDomain"));
     conceptualDomain.setContext(context);
 
     conceptualDomain = (ConceptualDomain)conceptualDomainDAO.find(conceptualDomain).get(0);
 
 
   }
   
   private void initClassifications() throws PersisterException {
     domainCs = DomainObjectFactory.newClassificationScheme();
     domainCs.setLongName("essai-UML");
     ArrayList eager = new ArrayList();
     eager.add(EagerConstants.CS_CSI);
     List result = classificationSchemeDAO.find(domainCs, eager);
 
     if(result.size() == 0)
       throw new PersisterException("Classification Scheme: " + domainCs.getLongName() + " does not exist on DB.");
 
     domainCs = (ClassificationScheme)result.get(0);
     
     List csCsis = domainCs.getCsCsis();
     for(int i=0; i<csCsis.size(); i++) {
       ClassSchemeClassSchemeItem csCsi = (ClassSchemeClassSchemeItem)csCsis.get(i);
       if(csCsi.getCsi().getName().equals(projectName)) {
 	projectCsCsi = csCsi;
 	projectCsi = projectCsCsi.getCsi();
       }
     }    
 
 
     if(projectCsCsi == null) { // need to add projectName CSI
       ClassificationSchemeItem csi = DomainObjectFactory.newClassificationSchemeItem();
       csi.setName(projectName);
       csi.setType("TEST");
 
       ClassSchemeClassSchemeItem csCsi = DomainObjectFactory.newClassSchemeClassSchemeItem();
       csCsi.setCs(domainCs);
       csCsi.setCsi(csi);
       csCsi.setLabel(projectName);
 
       csCsi.setId((String)classificationSchemeDAO.addClassificationSchemeItem(domainCs, csCsi));
 
       List list = classificationSchemeItemDAO.find(csi);
       projectCsi = (ClassificationSchemeItem)list.get(0);
       
       projectCsCsi = csCsi;
 
     }
 
 
     for(int i=0; i<csCsis.size(); i++) {      
       ClassSchemeClassSchemeItem csCsi = (ClassSchemeClassSchemeItem)csCsis.get(i);
       if(csCsi.getCsi().getName().equals(projectName + "-" + version)) {
 	versionCsCsi = csCsi;
 	versionCsi = versionCsCsi.getCsi();
       }
     }
     if(versionCsCsi == null) { // need to add version CSI
       ClassificationSchemeItem csi = DomainObjectFactory.newClassificationSchemeItem();
       csi.setName(projectName + "-" + version);
       csi.setType("TEST");
 
       ClassSchemeClassSchemeItem csCsi = DomainObjectFactory.newClassSchemeClassSchemeItem();
       csCsi.setCs(domainCs);
       csCsi.setCsi(csi);
       csCsi.setLabel(csi.getName());
       
       csCsi.setId((String)classificationSchemeDAO.addClassificationSchemeItem(domainCs, csCsi));
 
       List list = classificationSchemeItemDAO.find(csi);
       versionCsi = (ClassificationSchemeItem)list.get(0);
 
       versionCsCsi = csCsi;
 
     }
 
   }
 
 }
