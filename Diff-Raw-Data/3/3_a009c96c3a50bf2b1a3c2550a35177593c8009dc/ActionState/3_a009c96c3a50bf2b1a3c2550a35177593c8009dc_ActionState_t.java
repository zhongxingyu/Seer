 package de.fhac.ti.yagi.vm.memory;
 
 import de.fhac.ti.yagi.vm.exceptions.InvalidModelException;
 import de.fhac.ti.yagi.vm.exceptions.ModelNotFoundException;
 import de.fhac.ti.yagi.vm.interfaces.AbstractModel;
 import de.fhac.ti.yagi.vm.interfaces.State;
 import de.fhac.ti.yagi.vm.memory.models.Fluent;
 import de.fhac.ti.yagi.vm.memory.models.Var;
 import de.fhac.ti.yagi.vm.memory.models.action.Action;
 
 import java.util.HashMap;
 import java.util.Map;
 
 public class ActionState implements State {
 
     private static final String INVALID_MODEL = "Model instance is not of type <action>.";
     private static final String MODEL_NOT_FOUND = "Model could not be found. It wasn't added yet.";
 
     private Map<String, Action> mActions;
 
     public ActionState() {
         this.mActions = new HashMap<String, Action>();
     }
 
     @Override
     public void add(AbstractModel model) throws InvalidModelException {
         if (! (model instanceof  Action)) {
             throw new InvalidModelException(INVALID_MODEL);
         }
 
         Action action = (Action)model;
         mActions.put(action.getName(), action);
     }
 
     @Override
     public void remove(AbstractModel model) throws InvalidModelException {
         if (! (model instanceof  Action)) {
             throw new InvalidModelException(INVALID_MODEL);
         }
 
         Action action = (Action)model;
         mActions.remove(action.getName());
     }
 
     @Override
     public AbstractModel get(String identifier) {
         if (! mActions.containsKey(identifier)) {
             return null;
         }
 
         return mActions.get(identifier);
     }
 
     @Override
     public boolean contains(String id) {
         return mActions.containsKey(id);
     }
 
     @Override
     public String listState() {
         StringBuilder strBuilder = new StringBuilder();
         for (Map.Entry<String, Action> actionEntry : mActions.entrySet()) {
             String actionName = actionEntry.getKey();
             Action action = actionEntry.getValue();
             strBuilder.append(actionName).append(":  ");
             // handle the varlist's output
             for (Map.Entry<String, Var> scopeEntry : action.getScope().entrySet()) {
                 strBuilder.append("Var [").append(scopeEntry.getKey()).append("]").append(" / ");
             }
             strBuilder.delete(strBuilder.length() - 3, strBuilder.length()).append("\n");
 
             // handle formula's output, if present
             if (action.isFormulaInit()) {
                 strBuilder.append(action.getFormula().toString()).append("\n");
             }
         }
        if (strBuilder.length() == 0) {
            strBuilder = new StringBuilder("There are no actions declared yet.");
        }
         return strBuilder.toString();
     }
 
     @Override
     public void clearState() {
         mActions.clear();
     }
 }
