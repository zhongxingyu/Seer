 /*******************************************************************************
  * Copyright (c) 6 jun. 2013 NetXForge.
  * 
  * This program is free software: you can redistribute it and/or modify it under
  * the terms of the GNU General Public License as published by the Free Software
  * Foundation, either version 3 of the License, or (at your option) any later
  * version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details. You should have received a copy of the GNU General Public License
  * along with this program. If not, see <http://www.gnu.org/licenses/>
  * 
  * Contributors: Christophe Bouhier - initial API and implementation and/or
  * initial documentation
  *******************************************************************************/
 package com.netxforge.netxstudio.server.jsp;
 
 import static com.google.inject.Guice.createInjector;
 import static org.ops4j.peaberry.Peaberry.osgiModule;
 import static org.ops4j.peaberry.Peaberry.service;
 
 import javax.servlet.Servlet;
 
 import org.eclipse.equinox.http.helper.BundleEntryHttpContext;
 import org.eclipse.equinox.http.helper.ContextPathServletAdaptor;
 import org.eclipse.equinox.jsp.jasper.JspServlet;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 import org.osgi.framework.ServiceReference;
 import org.osgi.service.http.HttpContext;
 import org.osgi.service.http.HttpService;
 import org.osgi.util.tracker.ServiceTracker;
 
 import com.google.inject.AbstractModule;
 import com.google.inject.Injector;
 import com.netxforge.netxstudio.common.model.ModelUtils;
import com.netxforge.netxstudio.server.IDPProvider;
 import com.netxforge.netxstudio.server.Server;
 
 public class JSPActivator implements BundleActivator {
 
 	@SuppressWarnings("rawtypes")
 	private ServiceTracker httpServiceTracker;
 
 	private Injector injector;
 
 	private static JSPActivator INSTANCE;
 
 	public static final String CONTEXT_PATH = "/remote";
 
 	public static JSPActivator getInstance() {
 		return INSTANCE;
 	}
 
 	public void start(BundleContext context) throws Exception {
 		INSTANCE = this;
 
 		injector = createInjector(osgiModule(context), new AbstractModule() {
 
 			@Override
 			protected void configure() {
 
 				// {@link CommonModule}
 				bind(ModelUtils.class).toProvider(
 						service(ModelUtils.class).single());
 
 				// {@link ServerModule}
				bind(IDPProvider.class).annotatedWith(Server.class).toProvider(
						service(IDPProvider.class).single());
 
				
 			}
 
 		});
 
 		injector.injectMembers(INSTANCE);
 
 		httpServiceTracker = new HttpServiceTracker(context);
 		httpServiceTracker.open();
 
 	}
 
 	public Injector getInjector() {
 		return injector;
 	}
 
 	public void stop(BundleContext context) throws Exception {
 		httpServiceTracker.close();
 	}
 
 	@SuppressWarnings("rawtypes")
 	private class HttpServiceTracker extends ServiceTracker {
 
 		@SuppressWarnings("unchecked")
 		public HttpServiceTracker(BundleContext context) {
 			super(context, HttpService.class.getName(), null);
 		}
 
 		@SuppressWarnings("unchecked")
 		public Object addingService(ServiceReference reference) {
 			final HttpService httpService = (HttpService) context
 					.getService(reference);
 			try {
 				HttpContext commonContext = new BundleEntryHttpContext(
 						context.getBundle(), "/web"); //$NON-NLS-1$
 				httpService.registerResources(CONTEXT_PATH, "/", commonContext); //$NON-NLS-1$ //$NON-NLS-2$
 
 				Servlet adaptedJspServlet = new ContextPathServletAdaptor(
 						new JspServlet(context.getBundle(), "/web"), CONTEXT_PATH); //$NON-NLS-1$//$NON-NLS-2$
 				httpService
 						.registerServlet(
 								CONTEXT_PATH + "/*.jsp", adaptedJspServlet, null, commonContext); //$NON-NLS-1$
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			return httpService;
 		}
 
 		@SuppressWarnings("unchecked")
 		public void removedService(ServiceReference reference, Object service) {
 			final HttpService httpService = (HttpService) service;
 			httpService.unregister(CONTEXT_PATH); //$NON-NLS-1$
 			httpService.unregister(CONTEXT_PATH + "/*.jsp"); //$NON-NLS-1$
 			super.removedService(reference, service);
 		}
 	}
 }
