 package edu.wustl.cab2b.client.ui.dag.ambiguityresolver;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.Vector;
 
 import edu.common.dynamicextensions.domaininterface.EntityInterface;
 import edu.wustl.cab2b.client.ui.WindowUtilities;
 import edu.wustl.cab2b.client.ui.mainframe.NewWelcomePanel;
 import edu.wustl.cab2b.client.ui.query.IPathFinder;
 import edu.wustl.cab2b.common.util.Constants;
 import edu.wustl.common.querysuite.metadata.path.ICuratedPath;
 import edu.wustl.common.querysuite.metadata.path.IPath;
 import edu.wustl.common.util.logger.Logger;
 
 /**
  * Class to resolve mabiguity between two categories
  * 
  * @author Administrator
  * 
  */
 public class ResolveAmbiguity {
 
     private Vector<AmbiguityObject> m_ambiguityObjects;
 
     int m_currentAmbigityIndex = 0;
 
     int m_totalAmbiguityObjects;
 
     Map<AmbiguityObject, List<IPath>> m_ambiguityObjectToPathsMap = new HashMap<AmbiguityObject, List<IPath>>();
 
     private IPathFinder m_pathFinder;
 
     public static int pathIdentity = 1;
 
     public ResolveAmbiguity(Vector<AmbiguityObject> ambiguityObjects, IPathFinder pathFinder) {
         m_ambiguityObjects = ambiguityObjects;
         m_totalAmbiguityObjects = m_ambiguityObjects.size();
         m_pathFinder = pathFinder;
     }
 
     public ResolveAmbiguity(AmbiguityObject ambiguityObject, IPathFinder pathFinder) {
         m_ambiguityObjects = new Vector<AmbiguityObject>();
         m_ambiguityObjects.add(ambiguityObject);
         m_totalAmbiguityObjects = m_ambiguityObjects.size();
         m_pathFinder = pathFinder;
     }
 
     public Map<AmbiguityObject, List<IPath>> getPathsForAllAmbiguities() {
         for (int i = 0; i < m_totalAmbiguityObjects; i++) {
             AmbiguityObject ambiguityObject = m_ambiguityObjects.get(i);
             Map<String, List<IPath>> allPathMap = getPaths(ambiguityObject.getSourceEntity(),
                                                            ambiguityObject.getTargetEntity());
 
             List<IPath> selectedPathList = allPathMap.get(Constants.SELECTED_PATH);
             if (!selectedPathList.isEmpty()) {
                 m_ambiguityObjectToPathsMap.put(ambiguityObject, selectedPathList);
             } else {
                List<IPath> curratedPathList =  allPathMap.get(Constants.CURATED_PATH);
                List<IPath> generalPathList =  allPathMap.get(Constants.GENERAL_PATH);
                List<IPath> selectedPaths = null;
                if(!curratedPathList.isEmpty() && !generalPathList.isEmpty()) {
                    selectedPaths = showAmbiguityResolverUI(allPathMap);
                }
                 m_ambiguityObjectToPathsMap.put(ambiguityObject, selectedPaths);
             }
         }
         return m_ambiguityObjectToPathsMap;
     }
 
     /**
      * Method to show ambiguity resolver UI
      * 
      */
     private List<IPath> showAmbiguityResolverUI(Map<String, List<IPath>> allPathMap) {
         AvailablePathsPanel availablePathsPanel = new AvailablePathsPanel(allPathMap);
         WindowUtilities.showInDialog(NewWelcomePanel.mainFrame, availablePathsPanel, "Path Ambiguity Resolver",
                                      Constants.WIZARD_SIZE2_DIMENSION, true, false);
 
         return availablePathsPanel.getUserSelectedPaths();
     }
 
     /**
      * Method to get all possible paths for given source and destination entity
      * 
      * @param sourceEntity
      * @param destinationEntity
      * @return
      */
     private Map<String, List<IPath>> getPaths(EntityInterface sourceEntity, EntityInterface destinationEntity) {
         Set<ICuratedPath> allCuratedPaths = m_pathFinder.getCuratedPaths(sourceEntity, destinationEntity);
         Logger.out.debug("  getCuratedPaths() executed : " + allCuratedPaths.size());
 
         List<IPath> selectedPaths = new ArrayList<IPath>();
         List<IPath> curatedPaths = new ArrayList<IPath>();
         for (ICuratedPath iCuratedPaths : allCuratedPaths) {
             Set<IPath> iPathSet = iCuratedPaths.getPaths();
             if (iPathSet != null && !iPathSet.isEmpty()) {
                 for (IPath iPath : iPathSet) {
                     if (iCuratedPaths.isSelected()) {
                         selectedPaths.add(iPath);
                     } else {
                         curatedPaths.add(iPath);
                     }
                 }
             }
         }
 
         List<IPath> generalPaths = m_pathFinder.getAllPossiblePaths(sourceEntity, destinationEntity);
 
         Map<String, List<IPath>> allPathMap = new HashMap<String, List<IPath>>(3);
         allPathMap.put(Constants.SELECTED_PATH, selectedPaths);
         allPathMap.put(Constants.CURATED_PATH, curatedPaths);
         allPathMap.put(Constants.GENERAL_PATH, generalPaths);
 
         return allPathMap;
     }
 
 }
