 /*
  * Copyright (c) 2012 European Synchrotron Radiation Facility,
  *                    Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package org.dawb.gda.extensions.loaders;
 
 import java.net.URL;
 import java.util.Collection;
 
 import org.eclipse.ui.services.AbstractServiceFactory;
 import org.eclipse.ui.services.IServiceLocator;
 
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.io.IDataHolder;
 import uk.ac.diamond.scisoft.analysis.io.IDiffractionMetadata;
 import uk.ac.diamond.scisoft.analysis.io.ILoaderService;
 import uk.ac.diamond.scisoft.analysis.io.IMetaData;
 import uk.ac.diamond.scisoft.analysis.io.LoaderFactory;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 //import org.dawb.fabio.FabioFile;
 
 /**
  * Provides a class which will use any loaders available to load a particular file
  * 
  * TODO FIXME This class should be moved to a proper OSGI service.
  * 
  * @author gerring
  *
  */
 public class LoaderService extends AbstractServiceFactory implements ILoaderService {
 
 	
 	public IDataHolder getData(String filePath, final IMonitor monitor) throws Throwable {
 	    IMonitor mon = monitor!=null ? monitor : new IMonitor.Stub(); 
 		return LoaderFactory.getData(filePath, mon);
 	}
 
 	
 	public IDataset getDataset(String filePath, final IMonitor monitor) throws Throwable {
 	    try {
 		    final URL uri = new URL(filePath);
 		    filePath = uri.getPath();
 		} catch (Throwable ignored) {
 		    // We try the file path anyway
 		}
 	    
 	    IMonitor mon = monitor!=null ? monitor : new IMonitor.Stub(); 
 		final IDataHolder dh  = LoaderFactory.getData(filePath, mon);
 		return dh!=null ? dh.getDataset(0) : null;
 	}
 	
 	
 	public IDataset getDataset(final String path, final String datasetName, final IMonitor monitor) throws Throwable {
 	    
 	    IMonitor mon = monitor!=null ? monitor : new IMonitor.Stub(); 
 		return LoaderFactory.getDataSet(path, datasetName, mon);
 	}
 	
 	public IMetaData getMetaData(final String filePath, final IMonitor monitor) throws Exception {
 				
 	    IMonitor mon = monitor!=null ? monitor : new IMonitor.Stub(); 
 		return LoaderFactory.getMetaData(filePath, mon);
 	}
 
 
 
 	@Override
 	public Object create(@SuppressWarnings("rawtypes") Class serviceInterface, 
 			             IServiceLocator parentLocator,
 			             IServiceLocator locator) {
 		
         if (serviceInterface==ILoaderService.class) {
         	return new LoaderService();
         }
 		return null;
 	}
 	
 	private IDiffractionMetadata lockedDiffractionMetaData;
 
 	@Override
 	public IDiffractionMetadata getLockedDiffractionMetaData() {
 		return lockedDiffractionMetaData;
 	}
 
 	@Override
 	public IDiffractionMetadata setLockedDiffractionMetaData(IDiffractionMetadata diffMetaData) {
 		IDiffractionMetadata old = lockedDiffractionMetaData;
 		lockedDiffractionMetaData= diffMetaData;
 		return old;
 	}
 
 
 	@Override
 	public Collection<String> getSupportedExtensions() {
 		return LoaderFactory.getSupportedExtensions();
 	}
 }
