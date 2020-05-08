 package gov.nih.nci.ncicb.cadsr.umlmodelbrowser.tree;
 
 import gov.nih.nci.cadsr.domain.ClassificationScheme;
 import gov.nih.nci.cadsr.domain.ClassificationSchemeRelationship;
 import gov.nih.nci.cadsr.domain.Context;
 import gov.nih.nci.cadsr.umlproject.domain.Project;
 import gov.nih.nci.cadsr.umlproject.domain.SubProject;
 import gov.nih.nci.cadsr.umlproject.domain.UMLClassMetadata;
 import gov.nih.nci.cadsr.umlproject.domain.UMLPackageMetadata;
 import gov.nih.nci.ncicb.cadsr.service.UMLBrowserQueryService;
 import gov.nih.nci.ncicb.cadsr.servicelocator.ApplicationServiceLocator;
 import gov.nih.nci.ncicb.cadsr.util.UMLBrowserParams;
 import gov.nih.nci.ncicb.webtree.LazyActionTreeNode;
 
 import java.io.Serializable;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 public class UMLBrowserTreeData implements Serializable {
    private static final long serialVersionUID = 1L;
 
    protected static Log log = LogFactory.getLog(UMLBrowserTreeData.class.getName());
    private static ApplicationServiceLocator appServiceLocator = null;
    private static List<LazyActionTreeNode> contextNodes = null;
    private static LazyActionTreeNode testNode = null;
    private static LazyActionTreeNode trainingNode = null;
    
    
    public UMLBrowserTreeData() {
    }
    
    private synchronized static void buildTree() {
        List<LazyActionTreeNode> newContextNodes = new ArrayList<LazyActionTreeNode>();
       log.info("Building UML Browser tree start ....");
       String testContext = UMLBrowserParams.getInstance().getTestContext();
       String trainingContext = UMLBrowserParams.getInstance().getTrainingContext();
       LazyActionTreeNode contextFolder =
          new LazyActionTreeNode("Context Folder", "caDSR Contexts",
              "javascript:classSearchAction('P_PARAM_TYPE=Context')",
              false);
       HashMap<String,LazyActionTreeNode> contextMap = new HashMap();
       HashMap<String,Project> projectMap = new HashMap();
       
       //get the node path
 
       //first build a text folder node context.
       try {
 
          UMLBrowserQueryService queryService =
             appServiceLocator.findQuerySerivce();
          List<Context> contexts = queryService.getAllContexts();
          for (Iterator iter = contexts.iterator(); iter.hasNext(); ) {
             Context context = (Context)iter.next();
             LazyActionTreeNode contextNode =
                new LazyActionTreeNode("Context", 
                context.name + " (" + context.getDescription() + ")",
                  "javascript:classSearchAction('P_PARAM_TYPE=Context&P_IDSEQ=" +
                  context.getId() + "&treeBreadCrumbs=caDSR Contexts>>" +
                  context.getName() +   " ')", context.getId(), false);
             if (context.name.equalsIgnoreCase(testContext))
                 testNode = contextNode;
             else if (context.name.equalsIgnoreCase(trainingContext))
                 trainingNode = contextNode;
             else
                 newContextNodes.add(contextNode);
                 
             contextMap.put(context.getId(), contextNode);
          }
         // Build project nodes
         List<Project> projects = queryService.getAllProjects();
         for (Iterator projIter = projects.iterator(); projIter.hasNext(); ) {
            Project project = (Project)projIter.next();
            Context projContext = project.getClassificationScheme().getContext();
            addProject(project, contextMap.get(projContext.getId()));
            projectMap.put(project.getClassificationScheme().getId(), project);
         }
             //add all containers
          List<ClassificationScheme> containers = queryService.findAllCSContainers();
          for (Iterator csIter = containers.iterator(); csIter.hasNext(); ) {
             ClassificationScheme container = (ClassificationScheme)csIter.next();
             addContainer(container, contextMap.get(container.getContext().getId()),projectMap);
 
         }
             
          }
 
        catch (Exception e) {
          log.error("Exception caught when building the tree", e);
          throw new RuntimeException(e);
       }
       contextNodes = newContextNodes;
       log.info("Finished Building UML Browser tree");
 
    }
    public void setAppServiceLocator(ApplicationServiceLocator appServiceLocator) {
       this.appServiceLocator = appServiceLocator;
    }
 
    public ApplicationServiceLocator getAppServiceLocator() {
       return appServiceLocator;
    }
 
    private static void addPackageNodes(Collection<UMLPackageMetadata> packages, 
    LazyActionTreeNode parentNode){
       boolean isParentProject = parentNode.getType().equalsIgnoreCase("Project Folder");
    
       if (packages == null) return;
       
       
       //build class nodes
        int bcIndex = parentNode.getAction().indexOf("&treeBreadCrumbs=");
        String parentBreadCrumb = parentNode.getAction().subSequence(bcIndex, 
               parentNode.getAction().length()-3).toString();
       
       for (Iterator pkgIter = packages.iterator(); pkgIter.hasNext(); ) {
          UMLPackageMetadata pkg = (UMLPackageMetadata)pkgIter.next();
          if (pkg.getSubProject() != null && isParentProject )
             continue;
          LazyActionTreeNode pkgNode =
             new LazyActionTreeNode("Package Folder", pkg.getName(),
           "javascript:classSearchAction('P_PARAM_TYPE=PACKAGE&P_IDSEQ=" +
           pkg.getId() + parentBreadCrumb + ">>" + pkg.getName() + " ')", 
           pkg.getId(), false);
           parentNode.getChildren().add(pkgNode);
          
           //addClassNodes to each package node
           addClassNodes(pkg.getUMLClassMetadataCollection(), pkgNode );
       }
       
    }
 
    private static void addClassNodes(Collection<UMLClassMetadata> umlClasses, 
    LazyActionTreeNode pkgNode){
       //build class nodes
        int bcIndex = pkgNode.getAction().indexOf("&treeBreadCrumbs=");
        String pkgBreadCrumb = pkgNode.getAction().subSequence(bcIndex, pkgNode.getAction().length()-3).toString();
       
        for (Iterator clsIter = umlClasses.iterator(); clsIter.hasNext(); ) {
           UMLClassMetadata umlClass = (UMLClassMetadata)clsIter.next();
           LazyActionTreeNode clsNode =
              new LazyActionTreeNode("Class Node", umlClass.getName(),
            "javascript:classSearchAction('P_PARAM_TYPE=CLASS&P_IDSEQ=" +
            umlClass.getId() +  pkgBreadCrumb + ">>" + umlClass.getName() +" ')",
            umlClass.getId(), false);
           pkgNode.getChildren().add(clsNode);
          }
       
    }
    public static void refreshTree()   {
       buildTree();
    }
 
    public static LazyActionTreeNode getTreeData(boolean excludeTest, boolean excludeTraining) {
       if (contextNodes == null)
          buildTree();
          
        LazyActionTreeNode contextFolder =
           new LazyActionTreeNode("Context Folder", "caDSR Contexts",
               "javascript:classSearchAction('P_PARAM_TYPE=Context')",
               false);
 
       for (Iterator iter= contextNodes.iterator(); iter.hasNext();)   
         contextFolder.getChildren().add((LazyActionTreeNode) iter.next());
        
       if (!excludeTest && testNode != null) 
         contextFolder.getChildren().add(testNode);
       
       if (!excludeTraining && trainingNode != null)
         contextFolder.getChildren().add(trainingNode);
       return contextFolder;
    }
    
    private static void addProject(Project project, LazyActionTreeNode pNode){
        if (project == null)
         return;
        if (project.getClassificationScheme().getLatestVersionIndicator().equalsIgnoreCase("YES")) {
           Collection<SubProject> subProjects = project.getSubProjectCollection();
           Collection<UMLPackageMetadata> pkgs = project.getUMLPackageMetadataCollection();
           Context projContext = project.getClassificationScheme().getContext();
           LazyActionTreeNode projectNode =
              new LazyActionTreeNode("Project Folder",
                project.getLongName(),
                "javascript:classSearchAction('P_PARAM_TYPE=PROJECT&P_IDSEQ=" +
                project.getId() +
                "&treeBreadCrumbs=caDSR Contexts>>" +
                projContext.getName() +
                ">>Projects>>" + project.getLongName() +
                " ')",  project.getId(),
                false);
           projectNode.setToolTip(project.getClassificationScheme().getPreferredDefinition());
           
           pNode.getChildren().add(projectNode);
        
           // build sub project nodes
        
           if (subProjects != null) {
           for (Iterator subprojIter = subProjects.iterator();
                subprojIter.hasNext(); ) {
              SubProject subProject = (SubProject)subprojIter.next();
              LazyActionTreeNode subprojectNode =
                 new LazyActionTreeNode("SubProject Folder",
                subProject.getName(),
                "javascript:classSearchAction('P_PARAM_TYPE=SUBPROJECT&P_IDSEQ=" +
                subProject.getId() +
                "&treeBreadCrumbs=caDSR Contexts>>" +
                projContext.getName() +
                ">>Projects>>" + project.getLongName() +
                ">>" +  subProject.getName() +
                " ')",
                subProject.getId(),
                false);
             projectNode.getChildren().add(subprojectNode);
              
              // build package nodes under sub project
              addPackageNodes(subProject.getUMLPackageMetadataCollection(), subprojectNode);
        
           }
        }
        // then build package nodes directly under project
        addPackageNodes(pkgs, projectNode);
 
        }
        
    }
    
     private static void addContainer(ClassificationScheme container, 
     LazyActionTreeNode pNode, HashMap<String,Project> projectMap){
         LazyActionTreeNode containerNode =
            new LazyActionTreeNode("Container",
              container.getLongName(),
              "javascript:classSearchAction('P_PARAM_TYPE=CONTAINER&P_IDSEQ=" +
              container.getId() +
              "&treeBreadCrumbs=caDSR Contexts>>" +
              container.getContext().getName() +
              ">>Container>>" + container.getLongName() +
              " ')",  container.getId(),
              false);
         containerNode.setToolTip(container.getPreferredDefinition());
         
         pNode.getChildren().add(containerNode);
         
         Iterator<ClassificationSchemeRelationship> childIter = container.getParentClassificationSchemeRelationshipCollection().iterator();
         while (childIter.hasNext()) {
             ClassificationSchemeRelationship childRel = childIter.next();
             if (childRel.getName().equalsIgnoreCase("HAS_A")) {
                 ClassificationScheme childCs = childRel.getChildClassificationScheme();
                 if (childCs.getType().equalsIgnoreCase("Container")) 
                     addContainer(childCs, containerNode, projectMap);
                 else if (childCs.getType().equalsIgnoreCase("Project")) {
                     addProject(projectMap.get(childCs.getId()), containerNode);
                 }
             }
         }
         
     
    
     }
 }
