 /**
  * 
  */
 package org.feature.cluster.model.editor.editors.algorithms;
 
 import java.text.DecimalFormat;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.MatchResult;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import org.apache.log4j.Logger;
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IWorkspace;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.resource.ResourceSet;
 import org.eclipse.emf.ecore.resource.impl.ResourceSetImpl;
 import org.feature.cluster.model.cluster.CoreGroup;
 import org.feature.cluster.model.cluster.GroupModel;
 import org.feature.cluster.model.cluster.ViewPoint;
 import org.feature.cluster.model.cluster.ViewPointContainer;
 import org.feature.cluster.model.editor.editors.View;
 import org.feature.cluster.model.editor.editors.ViewCreater;
 import org.featuremapper.models.feature.FeatureModel;
 import org.featuremapper.models.featuremapping.FeatureMappingModel;
 import org.featuremapper.models.featuremapping.FeatureMappingPackage;
 import org.featuremapper.models.featuremapping.FeatureModelRef;
 import org.js.model.utilities.FeatureMappingUtil;
 import org.js.model.utilities.FeatureModelUtil;
 
 /**
  * Handler that triggers both consistency check algorithm to compare their preformance.
  * 
  * @author <a href=mailto:info@juliaschroeter.de>Julia Schroeter</a>
  * 
  */
 public class ConsistencyCheckHandler extends AbstractHandler {
 
    private static String generatedProject = "generatedProject";
   // private static String[] genProjects = new String[] { "car", "cd_od", "crisis", "dell", "dms", "eCNF500",
   // "electronic", "graph", "printer", "weather" };
   private static String[] genProjects = new String[] { "dms" };
 
    protected static String mappingFolder = FeatureMappingPackage.eNS_PREFIX;
 
    private static Logger log = Logger.getLogger(ConsistencyCheckHandler.class);
 
    List<Long> bruteforceTimeList = new LinkedList<Long>();
    List<Long> heuristicTimeList = new LinkedList<Long>();
    List<Integer> numberFeatures = new LinkedList<Integer>();
    List<Integer> numberViews = new LinkedList<Integer>();
    List<Integer> numberViewPoints = new LinkedList<Integer>();
    List<Integer> numberConstraints = new LinkedList<Integer>();
    List<String> groupTreeHeight = new LinkedList<String>();
    List<String> groupMaxChildren = new LinkedList<String>();
    List<String> groupsPerVP = new LinkedList<String>();
    List<String> featuresPerGroup = new LinkedList<String>();
    List<Double> bruteForceConsistentVPRatio = new LinkedList<Double>();
    List<Double> heuristicConsistentVPRatio = new LinkedList<Double>();
 
    public List<Double> getBruteForceConsistentVPRatio() {
       return bruteForceConsistentVPRatio;
    }
 
    public void resetLists() {
       bruteforceTimeList = new LinkedList<Long>();
       heuristicTimeList = new LinkedList<Long>();
       numberFeatures = new LinkedList<Integer>();
       numberViews = new LinkedList<Integer>();
       numberViewPoints = new LinkedList<Integer>();
       numberConstraints = new LinkedList<Integer>();
       groupTreeHeight = new LinkedList<String>();
       groupMaxChildren = new LinkedList<String>();
       groupsPerVP = new LinkedList<String>();
       featuresPerGroup = new LinkedList<String>();
       bruteForceConsistentVPRatio = new LinkedList<Double>();
       heuristicConsistentVPRatio = new LinkedList<Double>();
    }
 
    @Override
    public Object execute(ExecutionEvent event) throws ExecutionException {
       IWorkspace workspace = ResourcesPlugin.getWorkspace();
       for (int i = 0; i < genProjects.length; i++) {
          log.debug("-------------------------------");
         // String projectPart = genProjects[i];
         // String projectName = generatedProject + "_" + projectPart;
         String projectName = generatedProject;
          log.debug("check consistency of " + projectName);
 
          IProject project = workspace.getRoot().getProject(projectName);
          if (project.exists()) {
             resetLists();
             IFolder projectFolder = project.getFolder(mappingFolder);
             if (projectFolder.exists()) {
                ResourceSet resourceSet = new ResourceSetImpl();
                IResource[] members;
                try {
                   members = projectFolder.members();
                   for (IResource iResource : members) {
                      if (iResource instanceof IFile) {
                         IFile file = (IFile) iResource;
                         FeatureMappingModel mapping = FeatureMappingUtil.getFeatureMapping(file, resourceSet);
                         if (mapping != null) {
                            determineInfo(file);
                            checkConsistency(mapping, resourceSet);
                           // checkCoreVPConsistency(mapping, resourceSet);
                         }
                      }
                   }
                   if (log.isDebugEnabled()) {
                      printPerformanceMeasure();
                   }
                } catch (CoreException e) {
                   log.error("Could not determine children of folder " + projectFolder);
                }
             }
 
          }
       }
       return null;
    }
 
    private void checkCoreVPConsistency(FeatureMappingModel featureMapping, ResourceSet resourceSet) {
       GroupModel groupModel = FeatureMappingUtil.getSolutionClusterModel(featureMapping, resourceSet);
       FeatureModelRef fmRef = featureMapping.getFeatureModel();
       if (fmRef != null) {
          FeatureModel featureModel = fmRef.getValue();
          checkCoreVPConsistency(groupModel, featureMapping, featureModel);
       }
    }
 
    private void checkCoreVPConsistency(GroupModel groupModel, FeatureMappingModel featureMapping, FeatureModel featureModel) {
       boolean result = false;
       ViewCreater viewCreator = new ViewCreater(groupModel, featureMapping, featureModel);
       List<View> views = viewCreator.getViews();
 
       BruteForceAlgorithm bruteForce = new BruteForceAlgorithm(groupModel, views, featureModel);
       ViewPointContainer container = groupModel.getViewPointContainer();
       ViewPoint coreVP = null;
       for (ViewPoint vp : container.getViewPoints()) {
          if ("V_Core".equals(vp.getName())) {
             coreVP = vp;
             break;
          }
       }
       if (coreVP != null) {
          CoreGroup coreGroup = groupModel.getCoreGroup();
 
          HashMap<EObject, View> viewMemory = new HashMap<EObject, View>();
          View view = bruteForce.checkViewpoint(coreVP, coreGroup, viewMemory);
          result = view.isConsistent();
          log.debug("V_Core is valid: " + result);
       }
    }
 
    private void determineInfo(IFile file) {
       determineInfo(groupTreeHeight, "_height", file);
       determineInfo(groupMaxChildren, "_countChildren", file);
       determineInfo(groupsPerVP, "_groupsVP", file);
       determineInfo(featuresPerGroup, "_FCount", file);
    }
 
    private void determineInfo(List<String> paramList, String param, IFile file) {
       String fileName = file.getName();
       // example:
       // documentmanagement.feature_balanced_height2_countChildren2_vpCount10_groupsVP1_2012-03-31-082230484.cluster_FCount_2012-03-31-082231424.featuremapping
       String regex = param + "([0-9])*";
       Pattern pattern = Pattern.compile(regex);
       Matcher matcher = pattern.matcher(fileName);
       while (matcher.find()) {
          MatchResult result = matcher.toMatchResult();
          String match = result.group();
          String number = match.substring(param.length());
          paramList.add(number);
       }
    }
 
    private void printPerformanceMeasure() {
       // TODO: avg. time algorithm
       // TODO: ratio inconsistent/ consistent vps
       printCollection("NumberFeatures    ", numberFeatures);
       printCollection("NumberConstraints ", numberConstraints);
       printCollection("NumberViewpoints  ", numberViewPoints);
       printCollection("NumberViews       ", numberViews);
       printCollection("Time Bruteforce   ", bruteforceTimeList);
       printCollection("Time Heuristic    ", heuristicTimeList);
       printStringCollection("Group TreeHeight  ", groupTreeHeight);
       printStringCollection("Group MaxChildren ", groupMaxChildren);
       printStringCollection("Groups per VP     ", groupsPerVP);
       printStringCollection("Features per Group", featuresPerGroup);
       printDoubleCollection("ConsistentRatio Bruteforce", bruteForceConsistentVPRatio);
       printDoubleCollection("ConsistentRatio Heuristic ", heuristicConsistentVPRatio);
    }
 
    private void printDoubleCollection(String description, List<Double> bruteForceConsistentVPRatio2) {
       StringBuffer s = new StringBuffer();
       s.append(description);
       s.append("{");
       for (Double ratio : bruteForceConsistentVPRatio2) {
          DecimalFormat df = new DecimalFormat("0.00");
          String ratioStr = df.format(ratio);
          s.append(ratioStr);
          s.append(", ");
       }
       s.append("}");
       log.debug(s);
    }
 
    private void printStringCollection(String description, List<String> list) {
       StringBuffer s = new StringBuffer();
       s.append(description);
       s.append("{");
       for (String l : list) {
          s.append(l);
          s.append(", ");
       }
       s.append("}");
       log.debug(s);
    }
 
    private void printCollection(String description, Collection<Integer> list) {
       StringBuffer s = new StringBuffer();
       s.append(description);
       s.append("{");
       for (Integer l : list) {
          s.append(l);
          s.append(", ");
       }
       s.append("}");
       log.debug(s);
    }
 
    private void printCollection(String description, List<Long> list) {
       StringBuffer s = new StringBuffer();
       s.append(description);
       s.append("{");
       for (Long l : list) {
          s.append(l);
          s.append(", ");
       }
       s.append("};");
       log.debug(s);
    }
 
    private void printVPCollection(String description, List<ViewPointWrapper> list) {
       StringBuffer s = new StringBuffer();
       s.append(description);
       s.append("{");
       for (ViewPointWrapper l : list) {
          s.append(l);
          s.append(", ");
       }
       s.append("};");
       log.debug(s);
    }
 
    private double getRatio(List<ViewPointWrapper> list) {
       StringBuffer s = new StringBuffer();
       List<ViewPointWrapper> consistent = new ArrayList<ViewPointWrapper>(list.size());
 
       for (ViewPointWrapper viewPointWrapper : list) {
          if (viewPointWrapper.isConsistent) {
             consistent.add(viewPointWrapper);
          }
       }
 
       double ratio = consistent.size() * 1.00 / list.size();
       return ratio;
    }
 
    private void checkConsistency(FeatureMappingModel featureMapping, ResourceSet resourceSet) {
       GroupModel groupModel = FeatureMappingUtil.getSolutionClusterModel(featureMapping, resourceSet);
       FeatureModelRef fmRef = featureMapping.getFeatureModel();
       if (fmRef != null) {
          FeatureModel featureModel = fmRef.getValue();
          ViewCreater viewCreator = new ViewCreater(groupModel, featureMapping, featureModel);
          List<View> views = viewCreator.getViews();
          log.debug("GroupModel " + groupModel.eResource().getURI());
 
          ViewPointContainer container = groupModel.getViewPointContainer();
          int viewpoints = 0;
          if (container != null) {
             viewpoints = container.getViewPoints().size();
          }
          numberViewPoints.add(viewpoints);
          numberFeatures.add(FeatureModelUtil.getAllFeatures(featureModel).size());
          numberConstraints.add(FeatureModelUtil.getConstraints(featureModel, FeatureModelUtil.cps_constraintLanguage).size());
          numberViews.add(views.size());
 
          long startB = System.currentTimeMillis();
          List<ViewPointWrapper> bfViewPoints = runBruteForce(views, groupModel, featureModel);
          long endB = System.currentTimeMillis();
          long timeB = endB - startB;
          bruteforceTimeList.add(timeB);
 
          printVPCollection("BruteForce VPs", bfViewPoints);
          double bRatio = getRatio(bfViewPoints);
          bruteForceConsistentVPRatio.add(bRatio);
 
          long startH = System.currentTimeMillis();
          List<ViewPointWrapper> hViewPoints = runHeuristic(views, groupModel, featureModel);
          long endH = System.currentTimeMillis();
          long timeH = endH - startH;
          heuristicTimeList.add(timeH);
          printVPCollection("Heuristic VPs ", hViewPoints);
          double hRatio = getRatio(hViewPoints);
          heuristicConsistentVPRatio.add(hRatio);
 
          log.debug("Bruteforce [" + bRatio + "] " + timeB + "ms , Heuristic [" + hRatio + "]" + timeH + "ms");
          log.debug("-------------------------------");
       }
    }
 
    private List<ViewPointWrapper> runHeuristic(List<View> views, GroupModel groupModel, FeatureModel featureModel) {
       IncrementalAlgorithm algorithm = new IncrementalAlgorithm(views, groupModel, featureModel);
       List<ViewPointWrapper> viewpoints = algorithm.checkViewpoints();
       return viewpoints;
    }
 
    private List<ViewPointWrapper> runBruteForce(List<View> views, GroupModel groupModel, FeatureModel featureModel) {
       BruteForceAlgorithm algorithm = new BruteForceAlgorithm(groupModel, views, featureModel);
       List<ViewPointWrapper> viewPoints = algorithm.checkViewPoints();
       return viewPoints;
    }
 
 }
