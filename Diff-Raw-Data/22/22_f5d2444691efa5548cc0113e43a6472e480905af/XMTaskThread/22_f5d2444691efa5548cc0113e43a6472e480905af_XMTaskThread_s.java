 package org.haibara.autoxm;
 
 import java.io.IOException;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.http.client.ClientProtocolException;
 
 public class XMTaskThread implements Runnable {
 
 	private List<Method> todoList;
 	private Map<Method, List<XMRobot>> invokerMap;
 	private Map<Method, String[]> methodParamsMap;
 
 	XMTaskThread(List<Method> todoList, Map<Method, List<XMRobot>> invokerMap,
 			Map<Method, String[]> methodParamsMap) {
 		this.todoList = todoList;
 		this.invokerMap = invokerMap;
 		this.methodParamsMap = methodParamsMap;
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void run() {
 		List<String> lastStdOutput = null;
 		for (Method m : todoList) {
 			int lastSOCounter = 0;
 			String[] methodParams = methodParamsMap.get(m);
 			if (methodParams == null || methodParams.length == 0) {

 			} else {
 				for (int i = 0; i < methodParams.length; i++) {
 					if ("[stdin]".equals(methodParams[i].toLowerCase())) {
 						if (lastStdOutput != null
 								&& lastSOCounter < lastStdOutput.size()) {
 							methodParams[i] = lastStdOutput.get(lastSOCounter);
 							lastSOCounter++;
 						} else {
 							throw new RuntimeException("Error action config");
 						}
 					}
 				}
 				for (XMRobot robot : invokerMap.get(m)) {
 					try {
 						if (robot.loginXM()) {
							lastStdOutput = ((List<String>) m.invoke(robot, methodParams));
 							if (lastStdOutput.contains("fail")) {
 								System.err.println(robot.id + " " + m.toString()
 										+ "failed");
 								return;
 							}
 						} else {
 							System.err.println("login xm failed:" + robot.id);
 						}
 					} catch (IllegalAccessException | IllegalArgumentException
 							| InvocationTargetException e) {
 						System.err.println("invoke method failed:" + robot.id
 								+ "," + m.toString() + ","
 								+ methodParams.toString());
 						e.printStackTrace();
 					} catch (ClientProtocolException e) {
 						System.err.println("login xm failed:" + robot.id);
 						e.printStackTrace();
 					} catch (IOException e) {
 						System.err.println("login xm failed:" + robot.id);
 						e.printStackTrace();
 					}
 				}
 			}
 		}
 	}
 }
