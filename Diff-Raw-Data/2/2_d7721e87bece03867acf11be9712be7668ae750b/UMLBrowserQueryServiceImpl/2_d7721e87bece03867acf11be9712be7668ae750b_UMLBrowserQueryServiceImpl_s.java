 package gov.nih.nci.ncicb.cadsr.service.impl;
 
 import gov.nih.nci.cadsr.domain.AdministeredComponent;
 import gov.nih.nci.cadsr.domain.AdministeredComponentContact;
 import gov.nih.nci.cadsr.domain.ClassificationScheme;
 import gov.nih.nci.cadsr.domain.ClassificationSchemeRelationship;
 import gov.nih.nci.cadsr.domain.Context;
 import gov.nih.nci.cadsr.umlproject.domain.Project;
 import gov.nih.nci.cadsr.umlproject.domain.SubProject;
 import gov.nih.nci.cadsr.umlproject.domain.UMLAttributeMetadata;
 import gov.nih.nci.cadsr.umlproject.domain.UMLClassMetadata;
 import gov.nih.nci.cadsr.umlproject.domain.UMLPackageMetadata;
 
 import gov.nih.nci.ncicb.cadsr.service.UMLBrowserQueryService;
 
 import gov.nih.nci.ncicb.cadsr.servicelocator.ApplicationServiceLocator;
 import gov.nih.nci.ncicb.cadsr.umlmodelbrowser.dto.SearchPreferences;
 import gov.nih.nci.ncicb.cadsr.util.UMLBrowserParams;
 import gov.nih.nci.system.applicationservice.ApplicationService;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import org.hibernate.criterion.DetachedCriteria;
 import org.hibernate.criterion.Expression;
 import org.hibernate.criterion.Order;
 import org.hibernate.criterion.Restrictions;
 import org.hibernate.criterion.SimpleExpression;
 
 public class UMLBrowserQueryServiceImpl implements UMLBrowserQueryService
 {
    public UMLBrowserQueryServiceImpl() {
    }
    private ApplicationService service = null;
    private ApplicationServiceLocator serviceLocator = null;
    private Context testContext = null;
    private Context trainingContext = null;
    private Log log = LogFactory.getLog(UMLBrowserQueryServiceImpl.class);
 
 
 
   protected Context getTestContext() throws Exception {
       if (testContext == null) {
         try {
           ApplicationService caCoreService = getCaCoreAPIService();
           Context context = new Context();
           UMLBrowserParams params = UMLBrowserParams.getInstance();
           context.setName(params.getTestContext());
           List<Context> contexts = caCoreService.search(Context.class,context);
           if (contexts.size()>0) {
               testContext = contexts.get(0);
           }
         }
         catch (Exception e) {
             log.error("Error getting test context.",e);
             throw e;
         }
       }
       return testContext;
 
   }
   protected Context getTrainingContext() throws Exception {
         if (trainingContext == null) {
           try {
             ApplicationService caCoreService = getCaCoreAPIService();
             Context context = new Context();
             UMLBrowserParams params = UMLBrowserParams.getInstance();
            context.setName(params.getTestContext());
             List<Context> contexts = caCoreService.search(Context.class,context);
             if (contexts.size()>0) {
                 trainingContext = contexts.get(0);
             }
           }
           catch (Exception e) {
               log.error("Error getting test context.",e);
               throw e;
           }
         }
         return trainingContext;
     }  
 
   protected DetachedCriteria applyContextSearchPreferences(SearchPreferences searchPreferences, DetachedCriteria detachedCriteria) throws Exception{
       if (searchPreferences != null)
       {
          if (searchPreferences.isExcludeTestContext())
          {
             Context testContext = getTestContext();
             if ((testContext!= null)&&(testContext.getId()!= null))
             {
                detachedCriteria.add(Expression.ne("id", testContext.getId()));
             }
          }
          if (searchPreferences.isExcludeTrainingContext()){
             Context trainingContext = getTrainingContext();
             if ((trainingContext!=null)&&(trainingContext.getId()!=null))
             {
                detachedCriteria.add(Expression.ne("id",trainingContext.getId()));
             }
          }
       }
       return detachedCriteria;
   }
   /**
    * Retrieves all contexts
    *
    * @param
    *
    * @return List of Context objects
    *
    * @throws java.lang.Exception
    */
   public List<Context> getAllContexts() throws Exception {
      DetachedCriteria contextCriteria =
        DetachedCriteria.forClass(Context.class);
      contextCriteria.addOrder(Order.asc("name"));
       ApplicationService caCoreService = getCaCoreAPIService();
      List results = caCoreService.query(contextCriteria, Context.class.getName());
     return results;
   }
 
     public List<Context> getAllContexts(SearchPreferences searchPreferences) throws Exception {
        DetachedCriteria contextCriteria =
          DetachedCriteria.forClass(Context.class);
          applyContextSearchPreferences(searchPreferences,contextCriteria);
        contextCriteria.addOrder(Order.asc("name"));
         ApplicationService caCoreService = getCaCoreAPIService();
        List results = caCoreService.query(contextCriteria, Context.class.getName());
       return results;
     }
 
   public List<Project> getAllProjects() throws Exception {
       ApplicationService caCoreService = getCaCoreAPIService();
       DetachedCriteria projectCriteria =
         DetachedCriteria.forClass(Project.class);
       projectCriteria.addOrder(Order.asc("shortName"));
      List<Project> results = caCoreService.query(projectCriteria, Project.class.getName());;
     return results;
   }
   
   public List<Project> getAllProjects(SearchPreferences searchPreferences) throws Exception {
         ApplicationService caCoreService = getCaCoreAPIService();
         DetachedCriteria projectCriteria =
           DetachedCriteria.forClass(Project.class);
           DetachedCriteria contextCriteria = projectCriteria.createCriteria("classificationScheme")
                                              .createCriteria("context");
         applyContextSearchPreferences(searchPreferences, contextCriteria);
         projectCriteria.addOrder(Order.asc("shortName"));
         List<Project> results = caCoreService.query(projectCriteria, Project.class.getName());;
        
         return results;
     }  
   
   public List<Project> getProjectForContext(Context context) throws Exception{
       ApplicationService caCoreService = getCaCoreAPIService();
       DetachedCriteria projectCriteria =
         DetachedCriteria.forClass(Project.class);
       projectCriteria.addOrder(Order.asc("longName"));
 
      if (context != null && context.getId().length() >0) {
         DetachedCriteria csCri = projectCriteria.createCriteria("classificationScheme");
         csCri.add(Expression.eq("latestVersionIndicator", "Yes"));
         DetachedCriteria contextCri= csCri.createCriteria("context");
         contextCri.add(Expression.eq("id", context.getId()));
      }
      List<Project> results = caCoreService.query(projectCriteria, Project.class.getName());;
     return results;
   }
     public List<SubProject> getAllSubProjects() throws Exception {
         ApplicationService caCoreService = getCaCoreAPIService();
         DetachedCriteria subProjectCriteria =
           DetachedCriteria.forClass(SubProject.class);
         subProjectCriteria.addOrder(Order.asc("name"));
        List<SubProject> results = caCoreService.query(subProjectCriteria, SubProject.class.getName());;
       return results;
     }
     
     public List<SubProject> getAllSubProjects(SearchPreferences searchPreferences) throws Exception {
         ApplicationService caCoreService = getCaCoreAPIService();
         DetachedCriteria subProjectCriteria =
           DetachedCriteria.forClass(SubProject.class);
           DetachedCriteria contextCriteria = subProjectCriteria.createCriteria("project")
                                              .createCriteria("classificationScheme")
                                              .createCriteria("context");
          applyContextSearchPreferences(searchPreferences, contextCriteria);
         subProjectCriteria.addOrder(Order.asc("name"));
        List<SubProject> results = caCoreService.query(subProjectCriteria, SubProject.class.getName());;
       return results;
     }    
     
     public List<UMLPackageMetadata> getAllPackages() throws Exception {
         ApplicationService caCoreService = getCaCoreAPIService();
         DetachedCriteria subPackageCriteria =
           DetachedCriteria.forClass(UMLPackageMetadata.class);
         subPackageCriteria.addOrder(Order.asc("name"));
        List<UMLPackageMetadata> results = caCoreService.query(subPackageCriteria, UMLPackageMetadata.class.getName());;
       return results;
     }
     
     public List<UMLPackageMetadata> getAllPackages(SearchPreferences searchPreferences) throws Exception {
         ApplicationService caCoreService = getCaCoreAPIService();
         DetachedCriteria subPackageCriteria =
           DetachedCriteria.forClass(UMLPackageMetadata.class);
           DetachedCriteria contextCriteria = subPackageCriteria.createCriteria("project")
                                              .createCriteria("classificationScheme")
                                              .createCriteria("context");
          applyContextSearchPreferences(searchPreferences, contextCriteria);
         subPackageCriteria.addOrder(Order.asc("name"));
        List<UMLPackageMetadata> results = caCoreService.query(subPackageCriteria, UMLPackageMetadata.class.getName());;
       return results;
     }    
     
     public List<UMLClassMetadata> getAllClasses() throws Exception {
         ApplicationService caCoreService = getCaCoreAPIService();
         DetachedCriteria umlClassMetadataCriteria =
           DetachedCriteria.forClass(UMLClassMetadata.class);
           umlClassMetadataCriteria.addOrder(Order.asc("name"));
        List<UMLClassMetadata> results = caCoreService.query(umlClassMetadataCriteria, UMLClassMetadata.class.getName());;
       return results;
     }    
     
     public List<UMLClassMetadata> getAllClasses(SearchPreferences searchPreferences) throws Exception {
         ApplicationService caCoreService = getCaCoreAPIService();
         DetachedCriteria umlClassMetadataCriteria =
           DetachedCriteria.forClass(UMLClassMetadata.class);
           DetachedCriteria contextCriteria = umlClassMetadataCriteria.createCriteria("project")
                                              .createCriteria("classificationScheme")
                                              .createCriteria("context");
           applyContextSearchPreferences(searchPreferences,contextCriteria);
           umlClassMetadataCriteria.addOrder(Order.asc("name"));
        List<UMLClassMetadata> results = caCoreService.query(umlClassMetadataCriteria, UMLClassMetadata.class.getName());;
       return results;
     }        
     
     
  
 
    public void setServiceLocator(ApplicationServiceLocator serviceLocator) {
       this.serviceLocator = serviceLocator;
    }
 
    public ApplicationServiceLocator getServiceLocator() {
       return serviceLocator;
    }
 
    protected ApplicationService getCaCoreAPIService() {
       if (service == null)
          service = serviceLocator.findCaCoreAPIService();
       return service;
    }
 
    public List<UMLPackageMetadata> getAllPackageForProject(Project project){
       List resultList =null;
       UMLPackageMetadata umlPkg = new UMLPackageMetadata();
       Project proj = new Project();
       proj.setId(project.getId());
       umlPkg.setProject(proj);
       try {
          resultList = getCaCoreAPIService().search(UMLPackageMetadata.class, umlPkg);
       }     catch (Exception e) {
        e.printStackTrace();
    }
   return resultList;
 
    }
    
    public List<UMLClassMetadata> getClassesForContext(String contextId){
       List resultList =null;
       try {
         DetachedCriteria classCriteria = DetachedCriteria.forClass(UMLClassMetadata.class);
  //        classCriteria.addOrder( Order.asc("name").ignoreCase() );
         classCriteria.addOrder( Order.asc("name"));
         if (contextId != null && contextId.length() >0) {
            DetachedCriteria contextCri= classCriteria.createCriteria("project").createCriteria("classificationScheme").createCriteria("context");
            contextCri.add(Expression.eq("id", contextId));
         }
         resultList = getCaCoreAPIService().query(classCriteria, UMLClassMetadata.class.getName());
                
       } catch (Exception e) {
        e.printStackTrace();
    }
    return resultList;
 
    }
    
  
    
 public List findUmlClass(UMLClassMetadata umlClass){
    List resultList =null;
    
    try {
        resultList = getCaCoreAPIService().search(UMLClassMetadata.class, umlClass);
    } catch (Exception e) {
        e.printStackTrace();
    }
   return resultList;
 }
 
   public List findUmlClassForContainer(String csId) throws Exception{
   
 //first get cs
     ClassificationScheme container = new ClassificationScheme();
     container.setId(csId);
     List<ClassificationScheme> csResult = getCaCoreAPIService().search(ClassificationScheme.class,container);
     if (csResult != null)
         container = csResult.get(0);
        List resultList = new ArrayList();
        
        //first get all project under this container
        List<String> projIds = getProjectIdsForContainer(container);
        
        if (projIds != null && projIds.size() >0) {
            DetachedCriteria classCriteria = DetachedCriteria.forClass(UMLClassMetadata.class);
            DetachedCriteria projectCriteria = classCriteria.createCriteria("project");
     
            projectCriteria.add(Restrictions.in("id", projIds));
            resultList = getCaCoreAPIService().query(classCriteria, UMLClassMetadata.class.getName());
        }
        return resultList;
     }
 
 public List findUmlClass(UMLClassMetadata umlClass, SearchPreferences searchPreferences) throws Exception{
      List resultList =null;
        
        try {
           DetachedCriteria classCriteria = DetachedCriteria.forClass(UMLClassMetadata.class);
           DetachedCriteria projectCriteria = classCriteria.createCriteria("project");
 
 
           if (umlClass != null)
           {
               if (umlClass.getName() != null) {
                   classCriteria.add(Restrictions.ilike("name",umlClass.getName()));
               }
               if (umlClass.getProject() != null)
               {
                  Project project = umlClass.getProject();
                  if (project.getId() != null) projectCriteria.add(Restrictions.eq("id",project.getId()));
                  if (project.getVersion()!=null) projectCriteria.add(Restrictions.like("version", project.getVersion()));
               }
               if (umlClass.getUMLPackageMetadata()!=null) {
                   UMLPackageMetadata umlPackage = umlClass.getUMLPackageMetadata();
                   DetachedCriteria packageCriteria = classCriteria.createCriteria("UMLPackageMetadata");
                   if (umlPackage.getId() != null) packageCriteria.add(Restrictions.eq("id", umlPackage.getId()));
                   if (umlPackage.getSubProject() != null){
                       SubProject subProject = umlPackage.getSubProject();
                       DetachedCriteria subProjectCriteria = packageCriteria.createCriteria("subProject");
                       if (subProject.getId() != null) subProjectCriteria.add(Restrictions.eq("id",subProject.getId()));
                   }
               }
           } //umlClas != null 
            DetachedCriteria contextCriteria= projectCriteria.createCriteria("classificationScheme")
                                                             .createCriteria("context");
            applyContextSearchPreferences(searchPreferences, contextCriteria);
            resultList = getCaCoreAPIService().query(classCriteria, UMLClassMetadata.class.getName());
        } catch (Exception e) {
           log.error(e);
           e.printStackTrace();
           throw e;
        }
       return resultList;
 }
 
    public List findUmlAttributes(UMLAttributeMetadata umlAttribute){
       List resultList =null;
       
       try {
           resultList = getCaCoreAPIService().search(UMLAttributeMetadata.class, umlAttribute);
       } catch (Exception e) {
           e.printStackTrace();
       }
      return resultList;
    }
    
     public List findUmlAttributes(UMLAttributeMetadata umlAttribute, SearchPreferences searchPreferences) throws Exception
     {
        List resultList = null;
         try {
            DetachedCriteria attributeCriteria = DetachedCriteria.forClass(UMLAttributeMetadata.class);
            DetachedCriteria projectCriteria = attributeCriteria.createCriteria("project");
             DetachedCriteria classCriteria = null;
            DetachedCriteria contextCriteria = projectCriteria.createCriteria("classificationScheme")
                                                             .createCriteria("context");
 
           
            if (umlAttribute != null)
            {
                UMLClassMetadata umlClass = umlAttribute.getUMLClassMetadata();
                if (umlAttribute.getName() != null) {
                    attributeCriteria.add(Restrictions.ilike("name", umlAttribute.getName()));
                }
                if ((umlClass != null)&&(umlClass.getName() != null)) {
                    classCriteria = attributeCriteria.createCriteria("UMLClassMetadata");
                    classCriteria.add(Restrictions.ilike("name",umlClass.getName()));
                }
                if ((umlClass != null)&&(umlClass.getProject() != null))
                {
                   Project project = umlClass.getProject();
                   if (project.getId() != null) projectCriteria.add(Restrictions.eq("id",project.getId()));
                   if (project.getVersion()!=null) projectCriteria.add(Restrictions.eq("version", project.getVersion()));
                }
                if ((umlClass != null)&&(umlClass.getUMLPackageMetadata()!=null)) {
                    UMLPackageMetadata umlPackage = umlClass.getUMLPackageMetadata();
                    if (classCriteria == null){
                        classCriteria = attributeCriteria.createCriteria("UMLClassMetadata");
                    }
                    DetachedCriteria packageCriteria = classCriteria.createCriteria("UMLPackageMetadata");
                    if (umlPackage.getId() != null) packageCriteria.add(Restrictions.eq("id", umlPackage.getId()));
                    if (umlPackage.getSubProject() != null){
                        DetachedCriteria subProjectCriteria = packageCriteria.createCriteria("subProject");
                        SubProject subProject = umlPackage.getSubProject();
                        if (subProject.getId() != null) subProjectCriteria.add(Restrictions.eq("id",subProject.getId()));
                    }
                }
             }//umlAttribute != null
             
             applyContextSearchPreferences(searchPreferences, contextCriteria);
             resultList = getCaCoreAPIService().query(attributeCriteria, UMLAttributeMetadata.class.getName());
 
         } catch (Exception e) {
            log.error(e);
            throw e;
         }
         return resultList;       
    
     }
 
 
     public List findProject(Project project){
        List resultList =null;
        
        try {
            resultList = getCaCoreAPIService().search(Project.class, project);
        } catch (Exception e) {
            e.printStackTrace();
        }
       return resultList;
     }
     
     public List<ClassificationScheme> findAllCSContainers() throws Exception {
     
         DetachedCriteria csCriteria = DetachedCriteria.forClass(ClassificationScheme.class);
         csCriteria.add(Restrictions.eq("type", "Container"));
         csCriteria.add(Restrictions.eq("workflowStatusName", "RELEASED"));
 
         csCriteria.addOrder(Order.asc("longName").ignoreCase());
         List<ClassificationScheme> results = getCaCoreAPIService().query(csCriteria, ClassificationScheme.class.getName());
         return results;
         
     }
     
     public List<String> getProjectIdsForContainer(ClassificationScheme container) {
         List<String> result = new ArrayList<String>();
         Iterator<ClassificationSchemeRelationship> childIter = container.getParentClassificationSchemeRelationshipCollection().iterator();
         while (childIter.hasNext()) {
             ClassificationSchemeRelationship childRel = childIter.next();
             if (childRel.getName().equalsIgnoreCase("HAS_A")) {
                 ClassificationScheme childCs = childRel.getChildClassificationScheme();
                 if (childCs.getType().equalsIgnoreCase("Container")) 
                     result.addAll(this.getProjectIdsForContainer(childCs));
                 else if (childCs.getType().equalsIgnoreCase("Project")) {
                     result.add(childCs.getId());
                 }
             }
         }
         return result;
         
     }
 }
