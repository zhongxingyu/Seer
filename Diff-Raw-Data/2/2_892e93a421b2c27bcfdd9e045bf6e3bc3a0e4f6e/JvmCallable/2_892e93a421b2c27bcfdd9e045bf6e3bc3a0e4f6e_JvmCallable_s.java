 package ca.cutterslade.util.processpool;
 
 import java.util.concurrent.Callable;
 
 import ca.cutterslade.util.jvmbuilder.JvmFactory;
 
 class JvmCallable<T, R extends Runnable & SpecifiesJvmFactory> implements Callable<T>, SpecifiesJvmFactory {
   private final R runnable;
   private final T result;
 
   JvmCallable(final R runnable, final T result) {
     this.runnable = runnable;
     this.result = result;
   }
 
   @Override
   public T call() {
     runnable.run();
     return result;
   }
 
   @Override
  public JvmFactory getJvmFactory() {
     return runnable.getJvmFactory();
   }
 }
