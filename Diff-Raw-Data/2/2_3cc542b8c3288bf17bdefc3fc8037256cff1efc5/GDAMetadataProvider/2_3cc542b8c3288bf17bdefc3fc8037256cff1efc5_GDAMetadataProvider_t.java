 /*-
  * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
  * Facilities Council
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.data.metadata;
 
 import gda.factory.Finder;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * Class that holds a singleton metadata instance.
  */
 public class GDAMetadataProvider {
 	
 	private static final Logger logger = LoggerFactory.getLogger(GDAMetadataProvider.class);
 	/**
 	 * The single instance of GdaMetadata should be named GDAMetadata
 	 */
 	public static final String GDAMETADATANAME = "GDAMetadata"; 
 	
 	/*
 	 * key for metadataEntry used to provide value for facility run cycle
 	 */
 	public static final String FACILITY_RUN_CYCLE = "facility.run_cycle";
 
 	/*
 	 * key for metadataEntry used to provide value for collection description
 	 */
 	public static final String COLLECTION_DESCRIPTION = "collection_description";
 
 	/*
 	 * key for metadataEntry used to provide value for collection identifier
 	 */
 	public static final String COLLECTION_IDENTIFIER = "collection_identifier";
 
 	/*
 	 * key for metadataEntry used to provide value for experiment description
 	 */
 	public static final String EXPERIMENT_DESCRIPTION = "experiment_description";
 
 	/*
 	 * key for metadataEntry used to provide value for experiment identifier
 	 */
	public static final String EXPERIMENT_IDENTIFIER = "visit";
 
 	/*
 	 * key for metadataEntry used to provide value for proposal
 	 */
 	public static final String PROPOSAL = "proposal";
 
 	/*
 	 * key for metadataEntry used to provide value for investigation
 	 */
 	public static final String INVESTIGATION = "investigation";
 
 	/*
 	 * key for metadataEntry used to provide value for facility run cycle
 	 */
 	public static final String TITLE = "title";	
 	
 	/*
 	 * key for metadataEntry used to provide value for scan identifier
 	 */
 	public static final String SCAN_IDENTIFIER = "scan_identifier";
 	
 	private static Metadata instance;
 
 	/**
 	 * @return an Object in the finder implementing Metadata. If non-existent then a GdaMetadata instance is constructed
 	 */
 	public synchronized static Metadata getInstance() {
 		return getInstance(true);
 	}
 
 	/**
 	 * @param createIfNonExistent
 	 * @return an Object in the finder implementing Metadata. If non-existent then a GdaMetadata can be constructed if
 	 *         required.
 	 */
 	public synchronized static Metadata getInstance(boolean createIfNonExistent) {
 		if (instance == null) {
 
 			instance = Finder.getInstance().find(GDAMETADATANAME);
 			
 			if(instance == null){
 				if (createIfNonExistent) {
 					instance = new GdaMetadata();
 				}
 			}
 		}
 		return instance;
 	}
 
 	/**
 	 * Sets the singleton metadata instance held by this class. This allows the metadata to be constructed elsewhere and
 	 * registered with this provider class.
 	 * 
 	 * @param metadata
 	 *            the metadata instance
 	 * @throws RuntimeException
 	 *             if the singleton instance has already been set
 	 */
 	public synchronized static void setInstance(Metadata metadata) {
 		if (instance != null) {
 			throw new RuntimeException("Metadata instance has already been set");
 		}
 		instance = metadata;
 	}
 	
 	/**
 	 * call this only when you wish to setup the metadata for test purposes
 	 * without this method a tests would interfere via the singleton instance 
 	 * 
 	 * @param metadata Metadata
 	 */	
 	public synchronized static void setInstanceForTesting(Metadata metadata) {
 		logger.warn("setInstanceForTesting called");
 		instance=metadata;
 	}	
 }
