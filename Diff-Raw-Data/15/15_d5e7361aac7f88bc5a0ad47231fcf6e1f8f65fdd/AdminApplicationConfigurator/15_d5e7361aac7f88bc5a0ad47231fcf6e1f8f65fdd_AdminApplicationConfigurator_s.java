 /*******************************************************************************
 * Copyright (c) 2012 <enter-company-name-here> and others.
  * All rights reserved.
  *
  * This program and the accompanying materials are made available under the
  * terms of the Eclipse Public License v1.0 which accompanies this distribution,
  * and is available at http://www.eclipse.org/legal/epl-v10.html.
  *
  * Contributors:
 *     <enter-developer-name-here> - initial API and implementation
  *******************************************************************************/
 package org.eclipse.gyrex.admin.ui.internal.application;
 
 import java.lang.reflect.Constructor;
 
 import org.eclipse.gyrex.admin.ui.internal.AdminUiActivator;
 
 import org.eclipse.rap.ui.internal.servlet.WorkbenchApplicationConfigurator;
 import org.eclipse.rwt.application.ApplicationConfiguration;
 import org.eclipse.rwt.application.ApplicationConfigurator;
 
 import org.osgi.framework.Bundle;
 import org.osgi.framework.ServiceReference;
 import org.osgi.framework.ServiceRegistration;
 
 import org.apache.commons.lang.UnhandledException;
 
 @SuppressWarnings("restriction")
 public class AdminApplicationConfigurator implements ApplicationConfigurator {
 //	public static final String THEME_ID_RAP_DEFAULT = "org.eclipse.rap.rwt.theme.Default";
 //	public static final String EP_ADMIN = "admin";
 
 	/** FILTER */
 	public static final String FILTER = "(service.pid=" + AdminApplicationConfigurator.class.getName() + ")";
 
 	@Override
 	public void configure(final ApplicationConfiguration configuration) {
 		final ServiceRegistration dummyServiceRegistration = AdminUiActivator.getInstance().getServiceHelper().registerService(AdminApplicationConfigurator.class.getName(), this, "Eclipse Gyrex", "Dummy service for configuring RAP. Do not use!", AdminApplicationConfigurator.class.getName(), null);
 		try {
 			final Constructor<WorkbenchApplicationConfigurator> constructor = WorkbenchApplicationConfigurator.class.getDeclaredConstructor(ServiceReference.class);
 			if (!constructor.isAccessible()) {
 				constructor.setAccessible(true);
 			}
 			final WorkbenchApplicationConfigurator configurer = constructor.newInstance(dummyServiceRegistration.getReference());
 			configurer.configure(configuration);
 		} catch (final Exception e) {
 			throw new UnhandledException("Unable to intantiate internal workbench configurator.", e);
 		} finally {
 			dummyServiceRegistration.unregister();
 		}
 
 //		configuration.setOperationMode(OperationMode.SWT_COMPATIBILITY);
 //		configuration.addEntryPoint(EP_ADMIN, AdminApplication.class);
 //		configuration.addStyleSheet(THEME_ID_RAP_DEFAULT, "theme/admin.css", new ResourceLoader() {
 //			@Override
 //			public InputStream getResourceAsStream(final String resourceName) throws IOException {
 //				InputStream result = null;
 //				final IPath path = new Path(resourceName);
 //				final URL url = FileLocator.find(getBundle(), path, null);
 //				if (url != null) {
 //					result = url.openStream();
 //				}
 //				return result;
 //			}
 //		});
 //		configuration.setSettingStoreFactory(new WorkbenchFileSettingStoreFactory());
 //		configuration.addBranding(new AbstractBranding() {
 //			@Override
 //			public String getBody() {
 //				InputStream stream = null;
 //				try {
 //					stream = FileLocator.openStream(getBundle(), new Path("html/body.html"), false);
 //					if (null != stream) {
 //						return IOUtils.toString(stream);
 //					}
 //				} catch (final IOException e) {
 //					throw new UnhandledException("Unable to load theme body. Please check deployment.", e);
 //				} finally {
 //					IOUtils.closeQuietly(stream);
 //				}
 //				return super.getBody();
 //			}
 //
 //			@Override
 //			public String getDefaultEntryPoint() {
 //				return EP_ADMIN;
 //			}
 //
 //			@Override
 //			public String[] getEntryPoints() {
 //				return new String[] { EP_ADMIN };
 //			}
 //
 //			@Override
 //			public String getFavIcon() {
 //				return "img/gyrex/eclipse.ico";
 //			}
 //
 //			@Override
 //			public String getServletName() {
 //				return EP_ADMIN;
 //			}
 //
 //			@Override
 //			public String getTitle() {
 //				return "Gyrex Admin Console";
 //			}
 //
 //			private void registerBundleResource(final String name, final IPath bundleRelativePath) throws IOException {
 //				final InputStream stream = FileLocator.openStream(getBundle(), bundleRelativePath, false);
 //				if (stream != null) {
 //					try {
 //						RWT.getResourceManager().register(name, stream);
 //					} finally {
 //						stream.close();
 //					}
 //				}
 //			}
 //
 //			@Override
 //			public void registerResources() throws IOException {
 //				registerBundleResource("img/gyrex/eclipse.ico", new Path("img/gyrex/eclipse.ico"));
 //			}
 //		});
 	}
 
 	Bundle getBundle() {
 		return AdminUiActivator.getInstance().getBundle();
 	}
 }
