 package teamwork.goodVibrations.triggers;
 
import java.util.ArrayList;

 import teamwork.goodVibrations.functions.Function;
 
 public interface Trigger
 {
  boolean addFunction(Integer f);
   void removeFunction();
  long getNextExecutionTime();
  ArrayList<Integer> getFunctions();
}
