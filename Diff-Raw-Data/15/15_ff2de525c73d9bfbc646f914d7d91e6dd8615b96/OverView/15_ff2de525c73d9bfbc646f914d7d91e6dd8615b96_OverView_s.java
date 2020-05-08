 
 package org.paxle.gui.impl.servlets;
 
 import java.lang.reflect.Method;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.Collection;
 import java.util.LinkedList;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.velocity.Template;
 import org.apache.velocity.context.Context;
 import org.osgi.framework.Constants;
 import org.paxle.core.IMWComponent;
 import org.paxle.gui.ALayoutServlet;
 import org.paxle.gui.impl.ServiceManager;
 
 public class OverView extends ALayoutServlet {
 	
 	private static final long serialVersionUID = 1L;
 	
	private static final String[] QUEUES = {
		"crawler", "parser", "indexer"
	};
 	
 	public OverView(final String bundleLocation) {
 		super(bundleLocation);
 	}
 	
 	@Override
 	public Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context context) {
 		
 		Template template = null;
 		
 		try {
 			final ServiceManager manager = (ServiceManager)context.get(SERVICE_MANAGER);
 			
 			context.put("runtime", Runtime.getRuntime());
 			context.put("sysprops", System.getProperties());
 			context.put("port", manager.getProperty("org.osgi.service.http.port"));
 			context.put("osgiVersion", manager.getProperty(Constants.FRAMEWORK_VERSION));
 			context.put("frameworkVendor", manager.getProperty(Constants.FRAMEWORK_VENDOR));
 			context.put("frameworkVersion", manager.getProperty("osgi.framework.version"));
 			
 			try {
 				final InetAddress localhost = InetAddress.getLocalHost();
 				context.put("hostname", localhost.getCanonicalHostName());
 				context.put("ip", localhost.getHostAddress());
 			} catch (UnknownHostException e) {
 				context.put("hostname", "localhost");
 				context.put("ip", "127.0.0.1");
 			}
 			
			// final LinkedHashMap<String,Object> servicesMap = new LinkedHashMap<String,Object>();
 			final LinkedList<Entry> servicesList = new LinkedList<Entry>();
 			for (final String queue : QUEUES) {
 				final String id = "org.paxle." + queue;
 				final Object[] services = manager.getServices("org.paxle.core.IMWComponent", String.format("(component.ID=%s)", id));
 				if (services != null && services.length == 1 && services[0] instanceof IMWComponent) {
 					final String name = Character.toUpperCase(queue.charAt(0)) + queue.substring(1);
 					
 					// check if resume or pause is demanded
 					if (request.getParameter("service") != null && request.getParameter("service").equals(name)) {
 						if (request.getParameter("pause") != null) {
 							((IMWComponent)services[0]).pause();
 							response.sendRedirect("/overview");
 						} else if (request.getParameter("resume") != null) {
 							((IMWComponent)services[0]).resume();
 							response.sendRedirect("/overview");
 						}
 					}
 					
 					final int count;
					if (queue.equals("indexer")) {
 						count = 1;
 					} else {
						final Object subManager = manager.getService(id + ".ISub" + name + "Manager");
 						if (subManager == null) {
 							count = 0;
 						} else {
 							int tempCount;
 							try {
 								final Method getSub_List = subManager.getClass().getMethod("getSub" + name + "s");
 								final Collection<?> list = (Collection<?>)getSub_List.invoke(subManager);
 								tempCount = list.size();
 							} catch (Exception e) {
 								e.printStackTrace();
 								tempCount = -1;
 							}
 							count = tempCount;
 						}
 					}
 					
 					servicesList.add(new Entry(name, id, services[0], count));
 				}
 			}
 			context.put("services", servicesList);
 			
 			template = getTemplate("/resources/templates/OverView.vm");
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		
 		return template;
 	}
 	
 	public static final class Entry {
 		
 		public final Object service;
 		public final CharSequence name;
 		public final CharSequence id;
 		public final int count;
 		
 		public Entry(final CharSequence name, final CharSequence id, final Object service, final int count) {
 			this.name = name;
 			this.id = id;
 			this.service = service;
 			this.count = count;
 		}
 		
 		public Object getService() {
 			return service;
 		}
 		
 		public CharSequence getName() {
 			return name;
 		}
 		
 		public CharSequence getId() {
 			return id;
 		}
 		
 		public int getCount() {
 			return count;
 		}
 	}
 }
