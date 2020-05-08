 package org.eclipse.b3.commands;
 
 import java.io.PrintStream;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.b3.backend.core.B3BackendException;
 import org.eclipse.b3.backend.core.B3Engine;
 import org.eclipse.b3.backend.evaluator.b3backend.B3JavaImport;
 import org.eclipse.b3.backend.evaluator.b3backend.B3MetaClass;
 import org.eclipse.b3.backend.evaluator.b3backend.B3backendFactory;
 import org.eclipse.b3.backend.evaluator.b3backend.BExpression;
import org.eclipse.b3.backend.evaluator.b3backend.BFunction;
 import org.eclipse.b3.backend.evaluator.b3backend.IFunction;
 import org.eclipse.b3.backend.evaluator.typesystem.TypeUtils;
 import org.eclipse.b3.beelang.ui.BeeLangConsoleUtils;
 import org.eclipse.b3.build.build.BeeModel;
 import org.eclipse.core.commands.AbstractHandler;
 import org.eclipse.core.commands.ExecutionEvent;
 import org.eclipse.core.commands.ExecutionException;
 import org.eclipse.core.expressions.EvaluationContext;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.ui.console.MessageConsole;
 import org.eclipse.xtext.parsetree.CompositeNode;
 import org.eclipse.xtext.parsetree.NodeAdapter;
 import org.eclipse.xtext.parsetree.NodeUtil;
 import org.eclipse.xtext.ui.common.editor.outline.ContentOutlineNode;
 import org.eclipse.xtext.util.concurrent.IUnitOfWork;
 
 public class ExecuteHandler extends AbstractHandler {
 
 	@SuppressWarnings("unchecked")
 	public Object execute(ExecutionEvent event) throws ExecutionException {
 		final MessageConsole b3Console = BeeLangConsoleUtils.getBeeLangConsole();
 		PrintStream b3ConsoleOutputStream = BeeLangConsoleUtils.getConsoleOutputStream(b3Console);
 		try {
 			b3ConsoleOutputStream.println("Running the main function...");
 			EvaluationContext ctx = (EvaluationContext) event.getApplicationContext();
 			List<ContentOutlineNode> nodes = (List<ContentOutlineNode>) ctx.getDefaultVariable();
 			ContentOutlineNode node = nodes.get(0);
 			Object result = node.getEObjectHandle().readOnly(new IUnitOfWork<Object, EObject>(){
 				public Object exec(EObject state) throws Exception {
 					B3Engine engine = new B3Engine();
 					// Define all functions, and
 					// find a function called main (use the first found) and call it with a List<Object> argv
 					IFunction main = null;
 					for(IFunction f : ((BeeModel) state).getFunctions()) {
 						engine.getContext().defineFunction(f);
 						if("main".equals(f.getName())) {
 							main = f;
 						}
 					}
 					// Define all imports as constants
 					for(Type t : ((BeeModel) state).getImports()) {
 						if(t instanceof B3JavaImport) {
 							Class<?> x = TypeUtils.getRaw(t);
 							B3MetaClass metaClass = B3backendFactory.eINSTANCE.createB3MetaClass();
 							metaClass.setInstanceClass(x);
 							engine.getContext().defineValue(((B3JavaImport) t).getName(), x, metaClass);
 						}
 					}
 					if(main == null)
 						return null;
 					final List<Object> argv = new ArrayList<Object>();
 					try {
 						return engine.getContext().callFunction("main", new Object[] { argv },
 								new Type[] { List.class });
 					} catch(B3BackendException exprException) {
 						exprException.printStackTrace();
 						int lineNumber = 0;
 						BExpression expr = exprException.getExpression();
 						if(expr != null) {
 							NodeAdapter adapter = NodeUtil.getNodeAdapter(expr);
 							if(adapter != null) {
 								CompositeNode node = adapter.getParserNode();
 								if(node != null) {
 									lineNumber = node.getLine();
 								}
 							}
 						}
 					
 						PrintStream b3ConsoleErrorStream = BeeLangConsoleUtils.getConsoleErrorStream(b3Console);
 						try {
 							b3ConsoleErrorStream.println(exprException.getMessage());
 							b3ConsoleErrorStream.println("        at <function name TBD>("
 									+exprException.getLocationString()
 									+":"
 									+ lineNumber
 									+").");
 							if(exprException.getCause() != null) {
 								b3ConsoleErrorStream.println("Caused by: " + exprException.getCause().getMessage());
 							}
 							
 						} finally {
 							b3ConsoleErrorStream.close();
 						}
 						
 					} catch(Throwable e) {
 						// Just print some errors
 						e.printStackTrace();
 						PrintStream b3ConsoleErrorStream = BeeLangConsoleUtils.getConsoleErrorStream(b3Console);
 						try {
 							b3ConsoleErrorStream.println(e.getMessage());
 						} finally {
 							b3ConsoleErrorStream.close();
 						}
 					}
 
 					return null;
 				}
 			});
 			b3ConsoleOutputStream.println("Result = " + (result == null ? "null" : result.toString()));
 			return null;
 		}  finally {
 			b3ConsoleOutputStream.close();
 		}
 	}
 }
