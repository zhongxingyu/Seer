 
 package actions;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  *
  * @author xissburg
  */
public class SequenceAction extends FiniteAction {
 
     private List<FiniteAction> actions;
     private List<Float> durationRatios;
     private int currentActionIndex;
     private float currentActionTime;
     
     public SequenceAction(FiniteAction ... actions) {
         super(actionsDuration(actions));
         
         currentActionIndex = 0;
         currentActionTime = 0f;
         
         this.actions = new ArrayList<FiniteAction>(actions.length);
         this.actions.addAll(Arrays.asList(actions));
         
         durationRatios = new ArrayList<Float>(this.actions.size());
         
         for (FiniteAction a: this.actions) {
             float f = this.getDuration()>0? a.getDuration()/this.getDuration(): 0f;
             durationRatios.add(f);
         }
     }
     
     @Override
     public void update(float t) {
         if (currentActionIndex >= actions.size()-1) {
             return;
         }
         
         FiniteAction currentAction = actions.get(currentActionIndex);
         
         while (true) {
             currentAction.step(t*getDuration());
             currentActionTime += t;
 
             if (currentAction.isFinished()/*currentActionTime > currentAction.getDuration()*/) {
                 t = currentActionTime - currentAction.getDuration();
                 currentActionTime = 0f;
                 
                 if (currentActionIndex >= actions.size()-1) {
                     break;
                 }
                 
                 ++currentActionIndex;
                 currentAction = actions.get(currentActionIndex);
             }
             else {
                 break;
             }
         }
     }
     
     private static float actionsDuration(FiniteAction ... actions) {
         float duration = 0f;
         
         for (FiniteAction a: actions) {
             duration += a.getDuration();
         }
         
         return duration;
     }
 }
