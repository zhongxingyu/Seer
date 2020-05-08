 package org.feature.multi.perspective.generator.viewmodel;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.util.EcoreUtil;
 import org.feature.model.utilities.GroupModelUtil;
 import org.feature.multi.perspective.model.viewmodel.AbstractGroup;
 import org.feature.multi.perspective.model.viewmodel.CoreGroup;
 import org.feature.multi.perspective.model.viewmodel.Group;
 import org.feature.multi.perspective.model.viewmodel.GroupModel;
 import org.feature.multi.perspective.model.viewmodel.ViewPoint;
 import org.feature.multi.perspective.model.viewmodel.ViewPointContainer;
 import org.feature.multi.perspective.model.viewmodel.ViewmodelFactory;
 
 /**
  * Generator generates view models consisting of view groups and viewpoints.
  * 
  * @author <a href=mailto:info@juliaschroeter.de>Julia Schroeter</a>
  * 
  */
 public class ViewModelGenerator extends AbstractGenerator {
 
    private static Logger log = Logger.getLogger(ViewModelGenerator.class);
 
    /**
     * create a new view model.
     */
    public void generateViewModel() {
       // parameter are: groups, height,
       // further parameter are viewpoints, viewpoints assigned to groups.
       int minGroups = 30;
       int maxGroups = 40;
 
       GroupModel groupModel = null;
 
       for (int maxHeight : height) {
          for (int maxChildren : noMaxChildren) {
             for (int numberViewPoints : noViewpoints) {
                if (groupModel == null) {
                   groupModel = generateModel(maxHeight, maxChildren, minGroups, maxGroups, numberViewPoints);
                }
                // create a copy of the initial groupModel before reusing it
                GroupModel groupModelCopy = EcoreUtil.copy(groupModel);
                generateViewPoints(groupModelCopy, numberViewPoints, noGroupsAssignedToViewPoint);
                String type =
                   balanced + param_height + maxHeight + param_countChildren + maxChildren + param_vpCount + numberViewPoints
                         + param_groupsVP + noGroupsAssignedToViewPoint;
                persistModel(groupModelCopy, type, viewmodelFolder);
             }
          }
       }
    }
 
    private GroupModel generateModel(int maxHeight, int maxChildren, int minGroups, int maxGroups, int numberViewPoints) {
       int allGroupsSize = 0;
       GroupModel groupModel = null;
       do {
          groupModel = generateCoreGroupModel(maxHeight, maxChildren);
          allGroupsSize = GroupModelUtil.getAllGroups(groupModel, false).size();
       }
       while (allGroupsSize < minGroups || allGroupsSize > maxGroups);
       log.info("number groups: " + allGroupsSize);
       // generateViewPoints(groupModel, numberViewPoints, noGroupsAssignedToViewPoint);
       return groupModel;
    }
 
    private GroupModel generateCoreGroupModel(int maxHeight, int maxChildren) {
       GroupModel groupModel = ViewmodelFactory.eINSTANCE.createGroupModel();
       CoreGroup coreGroup = ViewmodelFactory.eINSTANCE.createCoreGroup();
       coreGroup.setName("Core");
       groupModel.setCoreGroup(coreGroup);
       // generate group hierarchy
       generateBalancedTree(coreGroup, maxHeight, maxChildren);
       return groupModel;
    }
 
    private void generateBalancedTree(CoreGroup coreGroup, int maxHeight, int noMaxChildren) {
       int currentHeight = 0;
       String parentId = "";
       createGroups(coreGroup, maxHeight, currentHeight, noMaxChildren, true, parentId);
    }
 
    private void createGroups(AbstractGroup parent, int maxHeight, int currentHeight, int noMaxChildren, boolean randomChildNumber,
                              String parentId) {
       if (currentHeight < maxHeight) {
          currentHeight++;
          int children;
          if (randomChildNumber) {
             children = getRandomGenerator().nextInt(noMaxChildren) + 1;
          } else {
             children = noMaxChildren;
          }
          for (int i = 0; i < children; i++) {
             int groupNumber = i + 1;
             String newparentId = parentId + groupNumber;
             Group childGroup = createChildGroup(parent, "G_" + newparentId);
             createGroups(childGroup, maxHeight, currentHeight, noMaxChildren, randomChildNumber, newparentId);
          }
       }
    }
 
    private Group createChildGroup(AbstractGroup parentGroup, String id) {
       Group childGroup = createGroup(id);
       parentGroup.getGroups().add(childGroup);
       return childGroup;
    }
 
    private Group createGroup(String id) {
       Group childGroup = ViewmodelFactory.eINSTANCE.createGroup();
       childGroup.setName(id);
       return childGroup;
    }
 
    private void generateViewPoints(GroupModel groupModel, int numberOfTotalViewpoints, int groupsAssignedToViewPoint) {
       ViewPointContainer container = ViewmodelFactory.eINSTANCE.createViewPointContainer();
       groupModel.setViewPointContainer(container);
 
       // create one vp for the coregroup
       int numberOfViewpoints = numberOfTotalViewpoints - 1;
       List<ViewPoint> viewpoints = createViewpoints(numberOfViewpoints, container);
 
       List<AbstractGroup> allGroups = GroupModelUtil.getAllGroups(groupModel, false);
       // parameter for group assignment: groupnumber
       int noGroups = allGroups.size();
       int groupsAssigned = groupsAssignedToViewPoint;
       if (groupsAssignedToViewPoint > noGroups) {
          groupsAssigned = noGroups;
       }
       for (ViewPoint vp : viewpoints) {
          assignViewPointToGroup(vp, groupsAssigned, allGroups);
       }
       createCoreGroupVP(groupModel);
    }
 
    public void removeViewPointFromGroups(ViewPoint vp) {
       EList<AbstractGroup> containedInGroup = vp.getContainedInGroup();
       AbstractGroup[] content = (AbstractGroup[]) containedInGroup.toArray(new AbstractGroup[containedInGroup.size()]);
       for (AbstractGroup group : content) {
         EList<ViewPoint> viewPointReference = group.getViewPointReference();
         viewPointReference.remove(vp);
          containedInGroup.remove(group);
       }
    }
 
    /**
     * exchange the mapping between groups and viewpoint.
     * 
     * @param vp
     * @param groupModel
     */
    public void exchangeViewpoint(ViewPoint vp, GroupModel groupModel) {
       List<AbstractGroup> allGroups = GroupModelUtil.getAllGroups(groupModel, false);
       removeViewPointFromGroups(vp);
       assignViewPointToGroup(vp, noGroupsAssignedToViewPoint, allGroups);
    }
 
    private void assignViewPointToGroup(ViewPoint vp, int groupsToAssign, List<AbstractGroup> allGroups) {
       int noGroups = allGroups.size();
       for (int i = 0; i < groupsToAssign; i++) {
          int index = getRandomGenerator().nextInt(noGroups - 1);
          AbstractGroup assignableGroup = allGroups.get(index);
         EList<ViewPoint> viewPointReference = assignableGroup.getViewPointReference();
         if (!viewPointReference.contains(vp)) {
            viewPointReference.add(vp);
          }
       }
    }
 
    private void createCoreGroupVP(GroupModel groupModel) {
       ViewPointContainer container = groupModel.getViewPointContainer();
       CoreGroup coreGroup = groupModel.getCoreGroup();
       ViewPoint coreVP = createViewPoint(coreViewpointName, container);
       coreVP.getContainedInGroup().add(coreGroup);
    }
 
    private ViewPoint createViewPoint(String name, ViewPointContainer container) {
       ViewPoint viewPoint = ViewmodelFactory.eINSTANCE.createViewPoint();
       String vpName = name;
       viewPoint.setName(vpName);
       container.getViewPoints().add(viewPoint);
       return viewPoint;
    }
 
    private List<ViewPoint> createViewpoints(int numberOfViewpoints, ViewPointContainer container) {
       for (int i = 0; i < numberOfViewpoints; i++) {
          String name = "V_" + i;
          createViewPoint(name, container);
       }
       return container.getViewPoints();
    }
 
 }
