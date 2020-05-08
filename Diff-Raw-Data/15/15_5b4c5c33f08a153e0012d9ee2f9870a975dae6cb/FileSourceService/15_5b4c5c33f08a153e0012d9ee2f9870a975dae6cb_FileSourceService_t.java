 /*******************************************************************************
  * Copyright (c) 2013 Peter Lachenmaier - Cooperation Systems Center Munich (CSCM).
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Peter Lachenmaier - Design and initial implementation
  ******************************************************************************/
 /**
  * 
  */
 package org.sociotech.communitymashup.source.file;
 
 import java.net.URISyntaxException;
 import java.net.URL;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.common.util.URI;
 import org.osgi.service.log.LogService;
 import org.sociotech.communitymashup.application.Source;
 import org.sociotech.communitymashup.data.DataSet;
 import org.sociotech.communitymashup.data.Item;
 import org.sociotech.communitymashup.source.file.properties.FileProperties;
 import org.sociotech.communitymashup.source.file.util.ItemsLoader;
 import org.sociotech.communitymashup.source.impl.SourceServiceFacadeImpl;
 
 /**
  * Main class of the file source service.
  * 
  * @author Peter Lachenmaier
  */
 public class FileSourceService extends SourceServiceFacadeImpl
 {
 
 	public static String WORKINGDIRECTORY_FOLDER_PLACEHOLDER = "{workingDirectory}";
 	
 	/**
 	 * Local map to look up added items to speed up loading
 	 */
 	private Map<String, Item> addedItems = new HashMap<String, Item>();
 	
	/**
	 * Prefix to add to all idents before adding to avoid wrong merges.
	 */
	private static final String identPrefix = "file_";
	
 	private boolean speedUp;
 	
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.source.impl.SourceServiceFacadeImpl#initialize(org.sociotech.communitymashup.application.Source)
 	 */
 	@Override
 	public boolean initialize(Source configuration)
 	{
 		// TODO check if file exists
 		
 		// get speed up property
 		speedUp = configuration.isPropertyTrueElseDefault(FileProperties.SPEED_UP_PROPERTY, FileProperties.SPEED_UP_DEFAULT);
 		
 		return super.initialize(configuration);
 	}
 
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.source.impl.SourceServiceFacadeImpl#fillDataSet(org.sociotech.communitymashup.data.DataSet)
 	 */
 	@Override
 	public void fillDataSet(DataSet dataSet)
 	{
 		super.fillDataSet(dataSet);
 
 		// clear temporary lookup list
 		addedItems.clear();
 		
 		// load items from file
 		String resourceName = source.getPropertyValue(FileProperties.FILE_NAME_PROPERTY);
 
 		// check resource
 		if(resourceName == null)
 		{
 			log("No file specified, please use " + FileProperties.FILE_NAME_PROPERTY + " to specify a file in the configuration", LogService.LOG_ERROR);
 			return;
 		}
 
 		log("Loading items from resource", LogService.LOG_DEBUG);
 		
 		EList<Item> items = loadItemsFromResource(resourceName);
 
 		if(items == null || items.isEmpty())
 		{
 			// nothing to do
 			log("No items contained in resource", LogService.LOG_DEBUG);
 			return;
 		}
 
 		log("Loaded " + items.size() + " items from resource", LogService.LOG_DEBUG);
 		
 		// duplicate list to avoid concurrent modifications
 		List<Item> tmpList = new LinkedList<Item>(items);
 
 		// add all items
 		for(Item item : tmpList)
 		{
			if(item.getIdent() != null)
			{
				item.setIdent(identPrefix + item.getIdent());
			}
		}
				
		// add all items
		for(Item item : tmpList)
		{
 			add(item);
 		}
 		
 		// clear temporary lookup list
 		addedItems.clear();		
 	}
 
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.source.impl.SourceServiceFacadeImpl#enrichDataSet()
 	 */
 	@Override
 	public void enrichDataSet()
 	{
 		// nothing more to do
 		super.enrichDataSet();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.source.impl.SourceServiceFacadeImpl#updateDataSet()
 	 */
 	@Override
 	public void updateDataSet()
 	{
 		// nothing more to do
 		super.updateDataSet();
 	}
 
 	/**
 	 * Creates an URI for the resource and uses {@link ItemsLoader#loadItems(URI)}
 	 * to load the items.
 	 * 
 	 * @param resourceNameOrPath Name of the resource contained in the local resource folder or
 	 * 			defined by full path.
 	 * @return A list of items. Null in error case.
 	 */
 	private EList<Item> loadItemsFromResource(String resourceNameOrPath)
 	{
 		if(resourceNameOrPath == null)
 		{
 			return null;
 		}
 
 		URI resourceURI = null;
 		URL fileURL = getClass().getResource("/resources/" + resourceNameOrPath);
 
 		if(fileURL == null)
 		{
 			// not contained in resources so try to load from external path
 			// replace possible working dir placeholder
 			String workingDirectory = System.getProperty("org.sociotech.communitymashup.configuration.workingdirectory");
 			String filePath = resourceNameOrPath.replace(WORKINGDIRECTORY_FOLDER_PLACEHOLDER, workingDirectory);
 			
 			resourceURI = URI.createFileURI(filePath);
 		}
 		else
 		{
 			try {
 				resourceURI = URI.createURI((fileURL.toURI().toString()));
 			}
 			catch (URISyntaxException e)
 			{
 				log("Could not create uri for bundled resource file " + resourceNameOrPath, LogService.LOG_ERROR);
 				// return null in error case
 				return null;
 			}
 		}
 		
 		if(resourceURI == null)
 		{
 			log("File " + resourceNameOrPath + " does not exist", LogService.LOG_ERROR);
 			return null;
 		}
 
 		EList<Item> items = null;
 		try {
 			items = ItemsLoader.loadItems(resourceURI);
 		} catch (Exception e) {
 			log("Items could not be loaded (" + e.getMessage() + ") from file " + resourceNameOrPath, LogService.LOG_ERROR);
 			return null;
 		}
 		 
 		if(items == null)
 		{
 			log("Items could not be loaded from file " + resourceNameOrPath, LogService.LOG_ERROR);
 		}
 		else
 		{
 			log("Loaded items from " + resourceNameOrPath, LogService.LOG_DEBUG);
 		}
 
 		return items;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.source.impl.SourceServiceFacadeImpl#add(org.sociotech.communitymashup.data.Item)
 	 */
 	@SuppressWarnings("unchecked")
 	@Override
 	public <T extends Item> T add(T item) {
 		if(item.getDataSet() == source.getDataSet())
 		{
 			// merged item -> dont use in lookup table
 			return super.add(item);
 		}
 		
 		String originalIdent = item.getIdent();
 		if(speedUp && originalIdent != null && addedItems.containsKey(originalIdent))
 		{
 			log("Item " + originalIdent + " already added.", LogService.LOG_DEBUG);
 			// must be of type T
 			return (T)addedItems.get(item.getIdent());
 		}
 		
 		log("Adding item " + originalIdent, LogService.LOG_DEBUG);
 		
 		
 		// add it to the temporary list
 		T addedItem = super.add(item);
 		if(addedItem != null)
 		{
 			// put it in list with ident of original item
 			addedItems.put(originalIdent, addedItem);
 		}
 		
 		return addedItem;
 	}
 }
