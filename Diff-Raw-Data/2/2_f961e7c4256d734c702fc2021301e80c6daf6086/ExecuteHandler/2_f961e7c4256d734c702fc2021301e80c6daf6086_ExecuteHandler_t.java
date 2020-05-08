 package org.eclipse.b3.commands;
 
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.b3.backend.core.B3Engine;
 import org.eclipse.b3.backend.evaluator.b3backend.BFunction;
 import org.eclipse.b3.beeLang.BeeModel;
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.expressions.EvaluationContext;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.xtext.ui.common.editor.outline.ContentOutlineNode;
 import org.eclipse.xtext.util.concurrent.IUnitOfWork;
 
 public class ExecuteHandler extends AbstractHandler {
 
 	@SuppressWarnings("unchecked")
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		System.out.print("Running the main function...\n");
 		EvaluationContext ctx = (EvaluationContext)event.getApplicationContext();
 		List<ContentOutlineNode> nodes = (List<ContentOutlineNode>)ctx.getDefaultVariable();
 		ContentOutlineNode node = nodes.get(0);
 		Object result = node.getEObjectHandle().readOnly(new IUnitOfWork<Object, EObject>() {
 
 			public Object exec(EObject state) throws Exception {
 				B3Engine engine = new B3Engine();
 				// Define all functions, and 
 				// find a function called main (use the first found) and call it with a List<Object> argv
 				BFunction main = null;
 				for(BFunction f : ((BeeModel)state).getFunctions()) {
 					engine.getContext().defineFunction(f);
 					if("main".equals(f.getName())) {
 						main = f;
 					}
 				}
 				if(main == null)
 					return null;
 				final List<Object> argv = new ArrayList<Object>();
 				try {
 					return engine.getContext().callFunction("main", new Object[] { argv }, new Type[] { List.class });
 				} catch (Throwable e) {
 					// Just print some errors
 					e.printStackTrace();
 				}
 				
 				return null;
 			}
 		}
 		);
		System.out.print("Result = " + (result == null ? "null" : result.toString())+"\n");
 		return null;
 	}
 
 }
