 
 package org.paxle.gui.impl.servlets;
 
 import java.io.File;
 import java.io.IOException;
 import java.lang.reflect.Method;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.Collection;
 import java.util.LinkedList;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.FileSystemUtils;
 import org.apache.velocity.Template;
 import org.apache.velocity.context.Context;
 import org.osgi.framework.Constants;
 import org.paxle.core.IMWComponent;
 import org.paxle.gui.ALayoutServlet;
 import org.paxle.gui.IServletManager;
 import org.paxle.gui.impl.ServiceManager;
 
 public class OverView extends ALayoutServlet {
 	
 	private static final long serialVersionUID = 1L;
 	
 	private static final String Q_CRAWLER = "crawler";
 	private static final String Q_PARSER = "parser";
 	private static final String Q_INDEXER = "indexer";
 	private static final String[] QUEUES = { Q_CRAWLER, Q_PARSER, Q_INDEXER };
 	
 	@Override
 	public Template handleRequest(HttpServletRequest request, HttpServletResponse response, Context context) {
 		
 		Template template = null;
 		
 		try {
 			final ServiceManager manager = (ServiceManager)context.get(SERVICE_MANAGER);
 			
 			if (request.getParameter("gc") != null) {
 				System.gc();
 				response.sendRedirect(super.servletLocation + "#dmemory");
 			}
 			
 			// set system
 			context.put("runtime", Runtime.getRuntime());
 			context.put("sysprops", System.getProperties());
 			context.put("port", manager.getProperty("org.osgi.service.http.port"));
 			context.put("osgiVersion", manager.getProperty(Constants.FRAMEWORK_VERSION));
 			context.put("frameworkVendor", manager.getProperty(Constants.FRAMEWORK_VENDOR));
 			context.put("frameworkVersion", manager.getProperty("osgi.framework.version"));
 			
 			// set host
 			try {
 				final InetAddress localhost = InetAddress.getLocalHost();
 				context.put("hostname", localhost.getCanonicalHostName());
 				context.put("ip", localhost.getHostAddress());
 			} catch (UnknownHostException e) {
 				context.put("hostname", "localhost");
 				context.put("ip", "127.0.0.1");
 			}
 			
 			// set activity
 			final LinkedList<Entry> servicesList = new LinkedList<Entry>();
 			for (final String queue : QUEUES) {
 				final String id = "org.paxle." + queue;
 				final Object[] services = manager.getServices("org.paxle.core.IMWComponent", String.format("(component.ID=%s)", id));
 				if (services != null && services.length == 1 && services[0] instanceof IMWComponent) {
 					final String name = Character.toUpperCase(queue.charAt(0)) + queue.substring(1);
 					
 					// check if resume or pause is demanded
 					if (request.getParameter("service") != null && request.getParameter("service").equals(name)) {
 						if (request.getParameter("pause") != null) {
 							((IMWComponent<?>)services[0]).pause();
 							response.sendRedirect(super.servletLocation);
 						} else if (request.getParameter("resume") != null) {
 							((IMWComponent<?>)services[0]).resume();
 							response.sendRedirect(super.servletLocation);
 						}
 					}
 					
 					final int count;
 					if (queue == Q_INDEXER) {
 						count = 1;
 					} else {
 						final Object subManager = manager.getService(String.format("%s.ISub%sManager", id, name));
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
 			
 			// determine whether the queues-servlet is installed and therefore links can be displayed
 			final IServletManager servletManager = (IServletManager)manager.getService(IServletManager.class.getName());
 			context.put("queueServletExists", Boolean.valueOf(servletManager.hasServlet("/queue")));
 			
 			// set index searcher
 			context.put("indexSearcher", manager.getService("org.paxle.se.index.IIndexSearcher"));
 			
 			// put the class itself into the context
 			context.put("overview",this);
 			
 			String reload = request.getParameter("reload");
 			if (reload == null) {
 				template = getTemplate("/resources/templates/OverView.vm");
 			} else if (reload.equals("activity")) {
 				// we don't want full html 
 				context.put("layout", "plain.vm");
 				
 				// just return the activity overview
 				template = getTemplate("/resources/templates/OverViewActivity.vm");
 			} else if (reload.equals("knownDocuments")) {
 				// we don't want full html 
 				context.put("layout", "plain.vm");
 				
 				// just return the knownDocuments overview
 				template = getTemplate("/resources/templates/OverViewKnownDocuments.vm");
 			}
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
 	
 	public long getFreeDiskspaceKb() {
 		try {
 			// for now we just query the paxle directory
 			return FileSystemUtils.freeSpaceKb(new File("/").toString());
 		} catch (IOException e) {
 			return -1;
 		}
 	}
 }
