 package teamwork.goodVibrations.triggers;
 
 import teamwork.goodVibrations.functions.Function;
 
 public interface Trigger
 {
  boolean addFunction(Function f);
   void removeFunction();
  void executeFunctions();
  int getNextExecutionTime();
  Integer[] getFunctions();
}
