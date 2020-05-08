 package org.dawnsci.plotting.tools.expressions;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.dawb.common.services.ServiceManager;
 import org.dawb.common.services.expressions.ExpressionEngineEvent;
 import org.dawb.common.services.expressions.IExpressionEngine;
 import org.dawb.common.services.expressions.IExpressionEngineListener;
 import org.dawb.common.services.expressions.IExpressionService;
 import org.eclipse.jface.text.DocumentEvent;
 import org.eclipse.jface.text.IDocumentListener;
 import org.eclipse.ui.console.IOConsole;
 import org.eclipse.ui.console.IOConsoleOutputStream;
 
 
 public class ExpressionConsole {
 	final IOConsole console;
 	final IOConsoleOutputStream stream;
 	IExpressionEngine engine;
     IExpressionEngineListener listener;
     
     private HashSet<IExpressionVariableListener> listeners;
     
 	
     public ExpressionConsole() {
     	console = new IOConsole("ExpressionTool", null);
     	stream = console.newOutputStream();
     	listeners = new HashSet<IExpressionVariableListener>();
 
     	try {
     		IExpressionService service = (IExpressionService)ServiceManager.getService(IExpressionService.class);
     		engine = service.getExpressionEngine();
 
     		listener = new IExpressionEngineListener() {
 
     			@Override
     			public void calculationDone(ExpressionEngineEvent evt) {
     				try {
     					
     					Object answer = evt.getResult();
 
     					if (answer!=null) {
     						stream.write(answer.toString());
     					} else {
     						stream.write("No Output");
     					}
     					stream.write("\n");
 
     					writeToConsole("Dawn>");
     					updateVariables();
     					
     				} catch (Exception e){
     					e.printStackTrace();
     				}
 
     			}
     		};
 
     		engine.addExpressionEngineListener(listener);
 
     		writeToConsole("Dawn>");
     		console.getDocument().addDocumentListener(new IDocumentListener() {
 
     			@Override
     			public void documentChanged(DocumentEvent event) {
    				if (event.getText().equals("\n") || event.getText().equals("\r\n")) {
     					processText(console.getDocument().get());
     				}
     			}
 
     			@Override
     			public void documentAboutToBeChanged(DocumentEvent event) {
     				// TODO Auto-generated method stub
     			}
     		});
 
     	} catch (Exception e) {
     		// TODO Auto-generated catch block
     		e.printStackTrace();
     		engine = null;
     	}
     	//engine = JexlUtils.getDawnJexlEngine();
     }
 	
 	public Map<String,Object> getFunctions() {
 		return engine.getFunctions();
 	}
 	
 	public void setFunctions(Map<String,Object> functions) {
 		engine.setFunctions(functions);
 	}
 	
 	public void addToContext(String name, Object object) {
 		engine.addLoadedVariable(name, object);
 	}
 	
 	public String getVariableValue(String name) {
 		Object object = engine.getLoadedVariable(name);
 		
 		if (object != null) return object.toString();
 		return null;
 	}
 	
 	public IOConsole getConsole() {
 		return console;
 	}
 	
 	private void writeToConsole(String msg) {
         try {
             stream.write(msg);
         } catch (IOException e) {
             e.printStackTrace();
         } 
     }
 	
 	private void processText(String text) {
 		try {
 			int index = text.lastIndexOf("Dawn>");
 			String sub = text.substring(index+5);
 			try {
 				engine.createExpression(sub);
 			} catch (Exception e) {
 				stream.write(e.getMessage());
 				stream.write("\n");
                 writeToConsole("Dawn>");
 			}
 			
 			engine.evaluateWithEvent(null);
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	
 	private void updateVariables() {
 		Collection<String> names = engine.getVariableNamesFromExpression();
 		
 		Iterator<String> iterator = names.iterator();
 		
 		List<String> namesList = new ArrayList<String>(names.size());
 		
 		while (iterator.hasNext()) {
 			namesList.add(iterator.next());
 		}
 		
 		ExpressionVariableEvent event = new ExpressionVariableEvent(this, namesList);
 		for (IExpressionVariableListener l  : listeners){
 			l.variableCreated(event);
 		}
 	}
 	
 	public void addListener(IExpressionVariableListener listener) {
 		listeners.add(listener);
 	}
 	
 	public void removeListener(IExpressionVariableListener listener) {
 		listeners.remove(listener);
 	}
 }
 
