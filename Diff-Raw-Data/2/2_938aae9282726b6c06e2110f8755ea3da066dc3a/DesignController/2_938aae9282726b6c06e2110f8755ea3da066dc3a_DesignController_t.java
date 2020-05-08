 package interiores.business.controllers;
 
 import interiores.business.models.FurnitureModel;
 import interiores.business.models.Room;
 import interiores.business.models.WantedFurniture;
 import interiores.business.models.WishList;
 import interiores.business.models.backtracking.FurnitureVariableSet;
 import interiores.business.models.constraints.BinaryConstraintSet;
 import interiores.business.models.constraints.UnaryConstraint;
 import interiores.core.business.BusinessController;
 import interiores.core.data.JAXBDataController;
 import interiores.shared.backtracking.NoSolutionException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 
 /**
  *
  * @author alvaro
  */
 public class DesignController extends BusinessController
 {
     
     private boolean solutionFound = false;
     private String lastSolution;
     
     public DesignController(JAXBDataController data) {
         super(data);
     }
     
     
     public void solve() {
         
         WishList wishList = (WishList) data.get("wishList");
         Room room = (Room) data.get("room");
         
         Collection<WantedFurniture> furniture = wishList.getWantedFurniture();
         
         List<List<FurnitureModel>> variableModels = new ArrayList();
         List<List<UnaryConstraint>> variableConstraints = new ArrayList();
         
         for (WantedFurniture wf : furniture) {
             variableModels.add(wf.getType().getFurnitureModels());
            variableConstraints.add(new ArrayList<UnaryConstraint>(wf.getConstraints()));
         }
         
         FurnitureVariableSet furVarSet = new FurnitureVariableSet(room, variableModels,
                                                                   variableConstraints, new BinaryConstraintSet());
         try {
             furVarSet.solve();
             solutionFound = true;
             lastSolution = furVarSet.toString();
         }
         catch (NoSolutionException nse) {
             solutionFound = false;
         }
         
     }
     
     public boolean hasSolution() {
         return solutionFound;
     }
     
     public String getDesign() {
         return lastSolution;
     }
     
     
 }
