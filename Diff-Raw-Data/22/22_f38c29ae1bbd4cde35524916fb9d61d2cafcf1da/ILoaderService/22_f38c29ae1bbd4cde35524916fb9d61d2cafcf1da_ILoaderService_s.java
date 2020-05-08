 /*-
  * Copyright (c) 2013 Diamond Light Source Ltd.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  */ 
 package uk.ac.diamond.scisoft.analysis.io;
 
 import java.util.Collection;
 
 import uk.ac.diamond.scisoft.analysis.dataset.IDataset;
 import uk.ac.diamond.scisoft.analysis.monitor.IMonitor;
 
 /**
  * A service with loads data using any loader it can, depending on the 
 * service implementation and returns it as an AbstractDataset
  * 
  * The implementor or this service contributes using an eclipse extension
  * point and then later any plugin may ask for an implementation of the service.
  * 
  * @author fcp94556
  *
  */
 public interface ILoaderService {
 	
 	/**
 	 * Reads a dataset and returns it as an AbstractDataset
 	 * @param filePath
 	 * @return IDataHolder
 	 * @throws Throwable
 	 */
     public IDataHolder getData(String filePath, final IMonitor monitor) throws Throwable;
 
 
 	/**
 	 * Reads a dataset and returns it as an AbstractDataset, with progress
 	 * @param filePath
 	 * @return IDataHolder
 	 * @throws Throwable
 	 */
     public IDataset getDataset(String filePath, final IMonitor monitor) throws Throwable;
     
 	/**
 	 * Reads a dataset and returns it as an AbstractDataset, with progress
 	 * @param filePath
 	 * @param datasetPath for instance nexus path, may be null.
 	 * @return IDataHolder
 	 * @throws Throwable
 	 */
     public IDataset getDataset(String filePath, final String datasetPath, final IMonitor monitor) throws Throwable;
 
 	 
 	 /**
 	  * This method can be used to load meta data. It will use Fabio if
 	  * LoaderFactory does not work.
 	  */
     public IMetaData getMetaData(final String filePath, final IMonitor monitor) throws Exception;
 
 
     /**
      * The locked diffraction data if any. Usually this will be null unless someone
      * has specifically decided to override the meta data.
      * 
      * @return IDataHolder
      */
     public IDiffractionMetadata getLockedDiffractionMetaData();
 
     /**
      * Call to set the locked data, this will mean that the real diffraction data is
      * ignored in some cases.
      * 
      * @param diffMetaData
      * @return the old one if any.
      */
     public IDiffractionMetadata setLockedDiffractionMetaData(IDiffractionMetadata diffMetaData);
 
 
     /**
      * 
      * @return list of supported file extensions.
      */
 	public Collection<String> getSupportedExtensions();
 

 }
