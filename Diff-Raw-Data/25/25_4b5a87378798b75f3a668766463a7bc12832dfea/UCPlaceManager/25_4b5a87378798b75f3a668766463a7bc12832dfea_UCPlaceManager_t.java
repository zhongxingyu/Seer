 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package BT.managers.UC;
 
 import BT.managers.CoordinateManager;
 import java.util.ArrayList;
 
 /**
  *
  * @author Karel Hala
  */
 public class UCPlaceManager {
     private ArrayList<UCActor> actors = new ArrayList<>();
     private ArrayList<UCUseCase> UseCases = new ArrayList<>();
     private ArrayList<UCJoinEdge> joinEdges = new ArrayList<>();
 
     
     public void addJoinEdge(UCJoinEdge joinEdge)
     {
         this.joinEdges.add(joinEdge);
     }
     
     public ArrayList<UCJoinEdge> getJoinEdges() 
     {
         return this.joinEdges;
     }
     
     public ArrayList<UCActor> getActors()
     {
         return this.actors;
     }
     
     public void addActor(UCActor place) 
     {
         this.actors.add(place);
     }
     
     public ArrayList<UCUseCase> getUseCases()
     {
         return this.UseCases;
     }
     
     public void addUseCase(UCUseCase place) 
     {
         this.UseCases.add(place);
     }
 
     public void removePlace(CoordinateManager selectedObject) {
         if (selectedObject instanceof UCActor)
         {
             this.actors.remove(selectedObject);
         }
         else if (selectedObject instanceof UCUseCase)
         {
             this.UseCases.remove(selectedObject);
         }
     }
    
    public void removeJointEdge(UCJoinEdge jointEdge)
    {
        this.joinEdges.remove(jointEdge);
    }
 }
