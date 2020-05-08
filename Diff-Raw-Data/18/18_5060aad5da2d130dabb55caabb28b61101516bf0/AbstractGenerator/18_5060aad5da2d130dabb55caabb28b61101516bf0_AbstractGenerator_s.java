 /**
  * 
  */
 package org.feature.multi.perspective.generator.viewmodel;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.feature.model.utilities.FeatureModelUtil;
 import org.feature.model.utilities.HighQualityRandom;
 import org.feature.model.utilities.ResourceUtil;
 import org.feature.multi.perspective.model.viewmodel.ViewmodelPackage;
 import org.feature.multi.perspective.model.viewmodel.GroupModel;
 import org.featuremapper.models.feature.FeatureModel;
 import org.featuremapper.models.feature.FeaturePackage;
 import org.featuremapper.models.featuremapping.FeatureMappingModel;
 import org.featuremapper.models.featuremapping.FeatureMappingPackage;
 
 /**
  * @author <a href=mailto:info@juliaschroeter.de>Julia Schroeter</a>
  * 
  */
 public class AbstractGenerator {
 
    private static Logger log = Logger.getLogger(AbstractGenerator.class);
 
    private static String generatedProject = "generatedProject";
    protected static String viewmodelFolder = ViewmodelPackage.eNS_PREFIX;
   protected static String mappingFolder = "mapping";
    protected static String featureFolder = FeaturePackage.eNS_PREFIX;
    private Random generator;
 
    public static String balanced = "balanced";
    public static String param_height = "_height";
    public static String param_countChildren = "_countChildren";
    public static String param_vpCount = "_vpCount";
    public static String param_groupsVP = "_groupsVP";
    public static String param_FCount = "_FCount";
 
    public static String coreViewpointName = "V_Core";
    
    protected static int[] assignedFeaturesPerGroup = new int[] { 3 };
    protected static int[] height = new int[] { 5 };
    protected static int[] noMaxChildren = new int[] { 3 };
 
    protected static int[] noViewpoints = new int[] {
    10 };
 //   10, 20, 30, 40, 50, 60, 70, 80, 90, 100 };
 
 
    protected static int noGroupsAssignedToViewPoint = 3;
    
 
    public static boolean generateViewmodel = true;
    public static boolean generateConsistentMapping = true;
    public static boolean reuseMapping = true;
    
    
    protected Random getRandomGenerator() {
       if (generator == null) {
          generator = new HighQualityRandom();
       }
       return generator;
    }
 
    protected void persistModel(EObject model, String type, String nsPrefix) {
       // String time = DateUtil.getFormattedCurrentTime();
       IProject genProject = ResourceUtil.getProject(generatedProject);
       IFolder projectFolder = ResourceUtil.getProjectFolder(nsPrefix, genProject);
       if (projectFolder != null) {
          IPath path = projectFolder.getRawLocation();
          if (path != null) {
             String absoluteFileUri = path + "/" + type + "." + nsPrefix;
             log.info("Persist model " + absoluteFileUri);
             ResourceUtil.persistModel(model, absoluteFileUri);
          }
          try {
             projectFolder.refreshLocal(IResource.DEPTH_ZERO, null);
          } catch (CoreException e) {
             log.error("Could not refresh Folder " + projectFolder);
          }
       }
    }
 
    
    protected void saveGroupModel(GroupModel groupModel) {
       Resource resource = groupModel.eResource();
       try {
          resource.save(null);
       } catch (IOException e) {
          log.error("Could not save changed groupModel");
       }
    }
    
    protected List<GroupModel> getAllViewModels(ResourceSet resourceSet) {
       List<GroupModel> models = new ArrayList<GroupModel>();
       IProject project = ResourceUtil.getProject(generatedProject);
       IFolder viewmodel = ResourceUtil.getProjectFolder(viewmodelFolder, project);
       try {
          List<IFile> files = ResourceUtil.getFiles(viewmodel);
          for (IFile file : files) {
             EObject model = ResourceUtil.getModel(file, resourceSet);
             if (model instanceof GroupModel) {
                GroupModel groupModel = (GroupModel) model;
                models.add(groupModel);
             }
          }
       } catch (CoreException e) {
          log.error("Could not load viewmodel files in the workspace");
       }
       return models;
    }
 
    protected List<FeatureModel> getAllFeatureModels(ResourceSet set) {
       List<FeatureModel> models = new ArrayList<FeatureModel>();
       IProject project = ResourceUtil.getProject(generatedProject);
       IFolder viewmodel = ResourceUtil.getProjectFolder(featureFolder, project);
       List<IFile> files;
       try {
          files = ResourceUtil.getFiles(viewmodel);
          for (IFile file : files) {
             FeatureModel model = FeatureModelUtil.getFeatureModel(file, set);
             models.add(model);
          }
       } catch (CoreException e) {
          log.error("Could not load featuremodel files in the workspace");
       }
       return models;
    }
 
    protected List<FeatureMappingModel> getAllMappingModels(ResourceSet resourceSet) {
       List<FeatureMappingModel> models = new ArrayList<FeatureMappingModel>();
       IProject project = ResourceUtil.getProject(generatedProject);
       IFolder viewmodel = ResourceUtil.getProjectFolder(mappingFolder, project);
       try {
          List<IFile> files = ResourceUtil.getFiles(viewmodel);
          for (IFile file : files) {
             EObject model = ResourceUtil.getModel(file, resourceSet);
             if (model instanceof FeatureMappingModel) {
                FeatureMappingModel mappingModel = (FeatureMappingModel) model;
                models.add(mappingModel);
             }
          }
       } catch (CoreException e) {
          log.error("Could not load viewmodel files in the workspace");
       }
       return models;
    }
 
    protected void cleanResourceSet(ResourceSet resourceSet) {}
 
 }
