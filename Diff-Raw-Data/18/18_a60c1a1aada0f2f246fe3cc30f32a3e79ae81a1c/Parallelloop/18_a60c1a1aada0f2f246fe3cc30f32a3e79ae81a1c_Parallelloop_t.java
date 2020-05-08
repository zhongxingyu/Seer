 package com.yahoo.dtf.actions.flowcontrol;
 
 import com.yahoo.dtf.actions.flowcontrol.Loop;
 import com.yahoo.dtf.actions.flowcontrol.Sequence;
 import com.yahoo.dtf.distribution.Worker;
 import com.yahoo.dtf.exception.BreakException;
 import com.yahoo.dtf.exception.DTFException;
 import com.yahoo.dtf.exception.InterruptionException;
 import com.yahoo.dtf.range.Range;
 import com.yahoo.dtf.range.RangeFactory;
 import com.yahoo.dtf.state.DTFState;
 
 /**
  * @dtf.tag parallelloop
  * 
  * @dtf.since 1.0
  * @dtf.author Rodney Gomes
  * 
  * @dtf.tag.desc The parallelloop tag will take the child tag and spawn as many 
  *               times as the number of items in the range expression and execute 
  *               those in parallel.
  * 
  * @dtf.tag.example
  * <parallelloop property="j" range="[1..6]">
  *     <parallelloop property="k" range="[1..6]">
  *         <log>Creating property property.${j}.${k}</log>
  *         <property name="property.${j}.${k}" value="${j}-${k}"/>
  *     </parallelloop>
  * </parallelloop>
  *   
  * @dtf.tag.example
  * <parallelloop property="var1" range="[1,2,3,4,5,6]">
  *     <log>Looping on ${var1}</log>
  * </parallelloop>
  */
 public class Parallelloop extends Loop {
 
     public Parallelloop() { }
    
     public void execute() throws DTFException {
         Range range = RangeFactory.getRange(getRange());
         int workerCount = range.size();
         
         Sequence children = new Sequence();
         children.addActions(children());
       
         Worker[] workers =  new Worker[workerCount];
        
         int i = 0;
         while (range.hasMoreElements()) {
             String value = range.nextElement();
             DTFState state = getState().duplicate();
             state.getConfig().setProperty(getProperty(), value);
             workers[i++] = new Worker(children, state, value);
         }
        
         for(i = 0; i < workerCount; i++) 
             workers[i].start();
       
         /*
          * Wait for all underlying threads to terminate and log all of them 
          * except the last exception which we can throw to any parent tag to 
          * handle the error.
          */
         DTFException lastexception = null;
         boolean interrupted = false;
         for (int index = 0; index < workers.length; index++) {
             try { 
                 workers[index].waitFor();
             } catch (InterruptionException e) { 
                 if ( getLogger().isDebugEnabled() ) {
                     getLogger().debug("Thread interrupted [" + 
                                       workers[index].getName() + "]",e);
                 }
                 interrupted = true;
             } catch (BreakException e) { 
                 // break point
                 if ( getLogger().isDebugEnabled() )
                     getLogger().debug("Break point hit", e);
             } catch (DTFException e) { 
                 if ( lastexception != null ) 
                     getLogger().error("Child failed.", lastexception);
                 
                 lastexception = e;
             }
         }
         
         if ( interrupted && getLogger().isDebugEnabled() ) 
             getLogger().debug("Execution interrupted.");
         
         if ( lastexception != null ) 
             throw lastexception;
     }
 }
