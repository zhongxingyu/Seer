 package model_example;
 
 public class Context extends edu.berkeley.path.model_base.Context {
   public Context(int id) {
     super(id);
   }
   
  public Run makeRun(Long runId, long seed0, long seed1) {
     return new Run(runId, this, seed0, seed1);
   }
 }
