 /*******************************************************************************
  * Copyright (c) 15 jan. 2013 NetXForge.
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
 package com.netxforge.netxstudio.data.internal;
 
 import static com.google.inject.util.Modules.override;
 import static org.ops4j.peaberry.Peaberry.osgiModule;
 
 import java.util.Dictionary;
 import java.util.Hashtable;
 
 import org.eclipse.osgi.service.debug.DebugOptions;
 import org.eclipse.osgi.service.debug.DebugOptionsListener;
 import org.eclipse.osgi.service.debug.DebugTrace;
 import org.ops4j.peaberry.Export;
 import org.osgi.framework.BundleActivator;
 import org.osgi.framework.BundleContext;
 
 import com.google.inject.Guice;
 import com.google.inject.Inject;
 import com.google.inject.Injector;
 import com.google.inject.Module;
 import com.netxforge.netxstudio.data.IDataService;
 import com.netxforge.netxstudio.data.IQueryService;
 import com.netxforge.netxstudio.data.actions.ServerRequest;
 import com.netxforge.netxstudio.data.cdo.IClientDPProvider;
 import com.netxforge.netxstudio.data.importer.ICSVMetricValuesImporterProvider;
 import com.netxforge.netxstudio.data.importer.IComponentLocator;
 import com.netxforge.netxstudio.data.importer.IImporterHelper;
import com.netxforge.netxstudio.data.importer.RDBMSMetricValuesImporterProvider;
 import com.netxforge.netxstudio.data.importer.ResultProcessor;
import com.netxforge.netxstudio.data.importer.XLSMetricValuesImporterProvider;
 import com.netxforge.netxstudio.data.index.IComponentMappingIndex;
 
 /**
  * @author Christophe Bouhier
  */
 public class DataActivator implements BundleActivator, DebugOptionsListener {
 
 	private static final String PLUGIN_ID = "com.netxforge.netxstudio.data";
 
 	private static BundleContext context;
 	private static Injector injector;
 
 	// fields to cache the debug flags
 	public static boolean DEBUG = false;
 	public static DebugTrace TRACE = null;
 
 	// Tracing for DataProvider
 	public static String TRACE_DATA_OPTION = "/trace.data";
 	public static String TRACE_DATA_DETAILS_OPTION = "/trace.data.details";
 
 	// Tracing for importing.
 	public static final String TRACE_IMPORT_OPTION = "/trace.import";
 	public static final String TRACE_IMPORT_DETAILS_OPTION = "/trace.import.details";
 	public static final String TRACE_IMPORT_LOCATOR_OPTION = "/trace.import.locator";
 	public static final String TRACE_IMPORT_HELPER_OPTION = "/trace.import.helper";
 
 	// Tracing for various types of result processing.
 
 	public static final String TRACE_RESULT_VALUE_OPTION = "/trace.result.value";
 	public static final String TRACE_RESULT_EXPRESSION_OPTION = "/trace.result.expression";
 	public static final String TRACE_RESULT_TOL_OPTION = "/trace.result.tolerance";
 
 	// Tracing for our component index service.
 	public static final String TRACE_COMPONENT_INDEX_OPTION = "/trace.component.index";
 
 	public void optionsChanged(DebugOptions options) {
 		DEBUG = options.getBooleanOption(PLUGIN_ID + "/debug", false);
 		TRACE = options.newDebugTrace(PLUGIN_ID);
 	}
 
 	public static BundleContext getContext() {
 		return context;
 	}
 	
 	// Note: Order is imported. I.e. ServerRequest requires the IDataService.
 	
 	@Inject
 	Export<IDataService> dataService;
 	
 	@Inject
 	Export<IClientDPProvider> clientDPProvider;
 	
 	@Inject
 	Export<ServerRequest> serverRequestService;
 
 	@Inject
 	Export<IQueryService> queryService;
 
 	@Inject
	Export<XLSMetricValuesImporterProvider> xlsImportService;
 
 	@Inject
 	Export<ICSVMetricValuesImporterProvider> csvImportService;
 
 	@Inject
	Export<RDBMSMetricValuesImporterProvider> rdbmsImportService;
 
 	@Inject
 	Export<IComponentMappingIndex> componentMappingIndex;
 
 	@Inject
 	Export<IComponentLocator> componentLocator;
 
 	// CB Consider not exposing the binding here, but in the client
 	@Inject
 	Export<IImporterHelper> importHelper;
 
 	@Inject
 	Export<ResultProcessor> resultProcessor;
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.osgi.framework.BundleActivator#start(org.osgi.framework.BundleContext
 	 * )
 	 */
 	public void start(BundleContext ctx) throws Exception {
 		DataActivator.context = ctx;
 		Module om = new DataModule();
 		// om = override(om).with(new CommonModule());
 		om = override(om).with(new ImporterModule());
 
 		injector = Guice.createInjector(osgiModule(ctx), om);
 		injector.injectMembers(this);
 
 		Dictionary<String, String> props = new Hashtable<String, String>(4);
 		props.put(DebugOptions.LISTENER_SYMBOLICNAME, PLUGIN_ID);
 		context.registerService(DebugOptionsListener.class.getName(), this,
 				props);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.osgi.framework.BundleActivator#stop(org.osgi.framework.BundleContext)
 	 */
 	public void stop(BundleContext bundleContext) throws Exception {
 		DataActivator.context = null;
 	}
 
 	public static Injector getInjector() {
 		return injector;
 	}
 
 }
