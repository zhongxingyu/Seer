 package interiores.business.models.backtracking;
 
 import interiores.business.events.backtracking.ActualVariableSetEvent;
 import interiores.business.events.backtracking.NextValueEvent;
 import interiores.business.events.backtracking.ValueAssignedEvent;
 import interiores.business.events.backtracking.ValueUnassignedEvent;
import interiores.business.models.room.FurnitureType;
 import interiores.business.models.WishList;
 import interiores.business.models.catalogs.NamedCatalog;
 import interiores.core.Debug;
 import interiores.core.Event;
 import interiores.core.Observable;
 import interiores.core.Observer;
 import interiores.core.business.BusinessException;
 import interiores.shared.backtracking.NoSolutionException;
 import interiores.shared.backtracking.Value;
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  *
  * @author hector
  */
 public class FurnitureVariableSetDebugger
     extends FurnitureVariableSet
     implements Observable
 {
     private List<Observer> debuggers;
     
     public FurnitureVariableSetDebugger(WishList wishList, NamedCatalog<FurnitureType> furnitureCatalog)
             throws BusinessException
     {
         super(wishList, furnitureCatalog);
         debuggers = new ArrayList();
     }
     
     @Override
     protected void setActualVariable() {
         super.setActualVariable();
         
        if(! allAssigned)
             notify(new ActualVariableSetEvent(actual));
     }
     
     @Override
     protected boolean actualHasMoreValues() {
         if(shouldStop())
             return false; // Force to stop checking current variable
         
         return super.actualHasMoreValues();
     }
     
     @Override
     protected Value getNextActualDomainValue() {
         Value value = super.getNextActualDomainValue();
         notify(new NextValueEvent((FurnitureValue) value));
         
         Thread current = Thread.currentThread();
         
         synchronized (current) {
             if(!shouldStop()) { 
                 try {
                    Debug.println("Pausing solver...");
                     current.wait();
                    Debug.println("Solver resumed!");
                 }
                 catch(Exception e) {
                     e.printStackTrace();
                 }
             }
         }
         
         return value;
     }
     
     @Override
     protected void assignToActual(Value value) {
         super.assignToActual(value);
         
         notify(new ValueAssignedEvent((FurnitureValue) value));
     }
     
     @Override
     protected void undoAssignToActual() {
         super.undoAssignToActual();
         
         notify(new ValueUnassignedEvent());
     }
     
     @Override
     public void backtracking() throws NoSolutionException
     {
         if(shouldStop())
             throw new NoSolutionException("Solver stopped manually");
         
         super.backtracking();    
     }
     
     @Override
     public void undoSetActualVariable() {
         super.undoSetActualVariable();
         
         notify(new ActualVariableSetEvent(actual));
     }
     
     @Override
     public void addListener(Observer observer) {
         debuggers.add(observer);
     }
     
     @Override
     public void notify(Event event) {
         if(shouldStop())
             return; // Stop notifying
         
         for(Observer debugger : debuggers)
             debugger.notify(event);
     }
     
     public boolean shouldStop() {
         return Thread.currentThread().isInterrupted();
     }
 }
