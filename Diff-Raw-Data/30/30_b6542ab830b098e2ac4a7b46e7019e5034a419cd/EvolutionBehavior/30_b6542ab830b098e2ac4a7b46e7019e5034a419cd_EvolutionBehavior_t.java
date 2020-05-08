 package org.nju.artemis.aejb.evolution.behaviors;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.nju.artemis.aejb.evolution.OperationContext;
 import org.nju.artemis.aejb.evolution.OperationFailedException;
 import org.nju.artemis.aejb.evolution.handlers.OperationStepHandler;
 
 /**
  * Evolution behavior, composed by some simple handlers.
  * 
  * @author <a href="wangjue1199@gmail.com">Jason</a>
  */
 public abstract class EvolutionBehavior implements OperationStepHandler{
 
 	List<OperationStepHandler> handlers = new ArrayList<OperationStepHandler>();
 	
 	abstract protected void initializeStepHandlers();
 	
 	protected void perform() throws OperationFailedException {
 		OperationContext context = generateOperationContext();
 		initializeStepHandlers();
 
 		for(Iterator<OperationStepHandler> iterator = handlers.iterator(); iterator.hasNext();) {
 			OperationStepHandler handler = iterator.next();
 			if(handler.execute(context) == OperationResult.UnExpected)
				rollBackWhenUnExpectedResult(handler);
 		}
 	}
 	
 	abstract protected OperationContext generateOperationContext();
	
	abstract void rollBackWhenUnExpectedResult(OperationStepHandler handler) throws OperationFailedException;
 }
