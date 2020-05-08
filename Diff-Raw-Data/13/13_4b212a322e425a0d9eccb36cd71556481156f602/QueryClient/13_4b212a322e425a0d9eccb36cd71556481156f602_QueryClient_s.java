 package au.edu.sapac.grid.mds;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.TreeSet;
 
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import au.edu.sapac.grid.glueschema.transform.GLUEQueryTransformer;
 import au.edu.sapac.grid.glueschema.transform.GLUEQueryTransformerFactory;
 import au.edu.sapac.grid.glueschema.transform.ParseException;
 import au.edu.sapac.grid.glueschema.transform.UnknownGLUESchemaVersion;
 import au.org.arcs.jcommons.constants.Constants;
 import au.org.arcs.jcommons.interfaces.GridInfoInterface;
 
 public class QueryClient implements GridInfoInterface {
 	
 	private QueryEngine qEngine;
 	private GLUEQueryTransformerFactory factory;
 	private GLUEQueryTransformer transformer;
 	
 	private static Log logger = 
         LogFactory.getLog(QueryClient.class.getName());
 	
 	public QueryClient() {
 		factory =  GLUEQueryTransformerFactory.newInstance();
 		try {
 		transformer = 
 		    factory.createGLUEQueryTransformer(GLUEQueryTransformerFactory.GLUE_SCHEMA_V12_R2);
 		} catch (UnknownGLUESchemaVersion e) {
 			// don't do anything yet
 		}
 		qEngine = new QueryEngine();
 	}	
 	
 	public QueryClient(String mdsCacheLocation) {
 		this();
 		qEngine.setCacheFileLocation(mdsCacheLocation);
 	}
 	
 	public QueryEngine getQueryEngine() {
 		return qEngine;
 	}
 	
 	// GERSON: will remove All from all the methods for consistency
 	
     /**
      * Get an array containing the names of all the sites on the Grid.
      * 
      * @return An array of site names
      */
     public String[] getSitesOnGrid() {
     	String[] sites = null;
         String xpathQuery;
         try {
         	xpathQuery = transformer.transform("get Site");
             
         	// Query MDS file.
         	NodeList hostLists = qEngine.turboMDSquery(xpathQuery);
         
             Element siteEl;
         	
         	// Keep sites Unique using TreeSet.
         	TreeSet<String> myTreeSet = new TreeSet<String>();
         
         	for (int i = 0; i < hostLists.getLength(); i++)
             {
         		siteEl = (Element) hostLists.item(i);
         		String siteElStr = qEngine.getTextValue(siteEl, "Name");
         		
         		// need to make sure we are not adding empty string into
         		// the set
         		if (!siteElStr.equals("")) {
         			myTreeSet.add(siteElStr);
         		}
         	}
         
             // Shove it into a String[] array.
         	sites = myTreeSet.toArray(new String[myTreeSet.size()]);
         }
     	catch (ParseException e) {
     	}    
     	return sites;
     }
     
     /**
      * Get the site where this host belongs to
      * 
      * @param host hostname
      * @return the site where the host belongs to
      */
     public String getSiteForHost(String host) {
     	String site = null;
         String xpathQuery;
         try {        	
         	// try first if the host is a GridFTP Server
         	xpathQuery = transformer.transform("get Site where AccessProtocol.Endpoint like '%" + host + "%'");
         	
         	logger.debug("xpath: " + xpathQuery);
         	
         	// Query MDS file.
         	NodeList hostLists = qEngine.turboMDSquery(xpathQuery);
         
             Element siteEl;
         	
         	// Keep sites Unique using TreeSet.
         	TreeSet<String> myTreeSet = new TreeSet<String>();
         
         	for (int i = 0; i < hostLists.getLength(); i++)
             {
         		siteEl = (Element) hostLists.item(i);
         		// just assume that even if there are two sites in the result
         		// the returned result will just be the same
         		site = qEngine.getTextValue(siteEl, "Name");
         	}
         	
         	// now try if it's a GRAM Server
         	xpathQuery = transformer.transform("get Site where ComputingElement.ContactString like '%" + host + "%'");
         	
         	logger.debug("xpath: " + xpathQuery);        	
         	hostLists = qEngine.turboMDSquery(xpathQuery);
         	myTreeSet = new TreeSet<String>();            
         	for (int i = 0; i < hostLists.getLength(); i++)
             {
         		siteEl = (Element) hostLists.item(i);
         		site = qEngine.getTextValue(siteEl, "Name");
         	}
         	
         	// now try if it's a Cluster host
         	xpathQuery = transformer.transform("get Site where Cluster.Name like '%" + host + "%'");
         	
         	logger.debug("xpath: " + xpathQuery);        	
         	hostLists = qEngine.turboMDSquery(xpathQuery);
         	myTreeSet = new TreeSet<String>();            
         	for (int i = 0; i < hostLists.getLength(); i++)
             {
         		siteEl = (Element) hostLists.item(i);
         		site = qEngine.getTextValue(siteEl, "Name");
         	}
         	
         	// or lastly, try if it's a SubCluster host
         	xpathQuery = transformer.transform("get Site where SubCluster.Name like '%" + host + "%'");
         	
         	logger.debug("xpath: " + xpathQuery);        	
         	hostLists = qEngine.turboMDSquery(xpathQuery);
         	myTreeSet = new TreeSet<String>();            
         	for (int i = 0; i < hostLists.getLength(); i++)
             {
         		siteEl = (Element) hostLists.item(i);
         		site = qEngine.getTextValue(siteEl, "Name");
         	}        	
         	
         } catch (ParseException e) {
         	
     	}    
     	return site;
     }
     
     public String[] getSitesForVO(String fqan) {
     	String[] sites = null;
         String xpathQuery;
         try {
         	xpathQuery = transformer.transform("get Site where ACL.Rule='" + fqan + "'");
             
         	// Query MDS file.
         	NodeList hostLists = qEngine.turboMDSquery(xpathQuery);
         
             Element siteEl;
         	
         	// Keep sites Unique using TreeSet.
         	TreeSet<String> myTreeSet = new TreeSet<String>();
         
         	for (int i = 0; i < hostLists.getLength(); i++)
             {
         		siteEl = (Element) hostLists.item(i);
         		myTreeSet.add(qEngine.getTextValue(siteEl, "Name"));
         	}
         
             // Shove it into a String[] array.
         	sites = myTreeSet.toArray(new String[myTreeSet.size()]);
         }
     	catch (ParseException e) {
     	}    
     	return sites;
     }
       
     
     // *************************************************
     // GERSON: StorageElement specific methods follow...
     
     /**
      * Get an array containing the addresses of all the GridFTP servers on the 
      * Grid
      * 
      * @return An array of GridFTP server addresses
      */
     public String[] getGridFTPServersOnGrid() {
     	return null;
     }
     
     
     // GERSON: This used to be called getGatewayGridFTPServerAtSite
     // having Gateway in the name makes it too specific to the Gateway method and
     // not generic enough to be used by other Grids
     
     /**
      * Get the GridFTP endpoint addresses for this site
      * 
      * @param site The site to check
      * @return The addresses of the GridFTP servers for this site
      */
     public String[] getGridFTPServersAtSite(String site) {
     	String[] gridftpServers = null;
         
 	  	String xpathQuery;
 	    try {
         	xpathQuery = 
         		transformer.transform("get AccessProtocol where " +
         				"Site.Name='" + site +
         				"' and AccessProtocol.Type='gsiftp'");
         	
         	logger.debug("xpath: " + xpathQuery);
 	       
 			NodeList storageElementNodeList = qEngine.turboMDSquery(xpathQuery);
 			// create a String array to house the list of codes available
 			// module = new String[codeVersionNodeList.getLength()];
 			logger.debug("SE element length: " + storageElementNodeList.getLength());
 
 			Element storageEl;
 			
 			// Keep unique using TreeSet
 	        TreeSet<String> myTreeSet = new TreeSet<String>();
 	
 	        for (int i = 0; i < storageElementNodeList.getLength(); i++)
 	        {
 	        	storageEl = (Element) storageElementNodeList.item(i);
 	            myTreeSet.add(qEngine.getTextValue(storageEl, "Endpoint"));
 	        }
 	
 	        gridftpServers = myTreeSet.toArray(new String[myTreeSet.size()]);
 			
 	    } catch (ParseException e) {
 	    	
 	    }
 	    return gridftpServers;
     }
     
     
     // GERSON: I've added the following methods which can be used to return 
     // GridFTP details for a queue and site. The old method which is called 
     // getClusterGridFTPServerAtSite won't work because it has the assumption 
     // that all the queues in a cluster can only use one GridFTP server    
     
     public String[] getGridFTPServersForQueueAtSite(String site, String queue) {
     	String defaultStorageElement = getDefaultStorageElementForQueueAtSite(site, queue);
     	if (defaultStorageElement != null) {
     		return getGridFTPServersForStorageElementAtSite(site, defaultStorageElement);
     	}
     	return null;
     }
     
     public String[] getStorageElementsForSite(String site) {
     	String[] storageElements = null;
         
 	  	String xpathQuery;
 	    try {
         	xpathQuery = 
         		transformer.transform("get StorageElement where Site.Name='" + site + "'");
         	
         	logger.debug("xpath: " + xpathQuery);
 	       
 			NodeList storageElementNodeList = qEngine.turboMDSquery(xpathQuery);
 			
 			logger.debug("SE element length: " + storageElementNodeList.getLength());
 
 			Element storageEl;
 			
 			// Keep unique using TreeSet
 	        TreeSet<String> myTreeSet = new TreeSet<String>();
 	
 	        for (int i = 0; i < storageElementNodeList.getLength(); i++)
 	        {
 	        	storageEl = (Element) storageElementNodeList.item(i);
 	            myTreeSet.add(qEngine.getAttributeValue(storageEl, "UniqueID"));
 	        }
 	
 	        storageElements = myTreeSet.toArray(new String[myTreeSet.size()]);
 			
 	    } catch (ParseException e) {
 	    	
 	    }
 	    return storageElements;
     }
     
     public it.infn.cnaf.forge.glueschema.spec.v12.r2.StorageElementType[]
     	getStorageElementsForVO(String fqan) {
     	
     	it.infn.cnaf.forge.glueschema.spec.v12.r2.StorageElementType[] 
     	    storageElements = null;
         
 	  	String xpathQuery;
 	    try {
         	xpathQuery = 
         		transformer.transform("get StorageElement where ACL.Rule='" + fqan + "'");
         	
         	logger.debug("xpath: " + xpathQuery);
 	       
 			NodeList storageElementNodeList = qEngine.turboMDSquery(xpathQuery);
 			
 			logger.debug("SE element length: " + storageElementNodeList.getLength());
 
 			org.w3c.dom.Node storageEl;
 			
 			// Keep unique using TreeSet
 	        java.util.List<it.infn.cnaf.forge.glueschema.spec.v12.r2.StorageElementType> myTreeSet = 
 	        	new java.util.ArrayList<it.infn.cnaf.forge.glueschema.spec.v12.r2.StorageElementType>();
 	
 	        for (int i = 0; i < storageElementNodeList.getLength(); i++)
 	        {
 	        	storageEl = (org.w3c.dom.Node) storageElementNodeList.item(i);	        	
 	        	try {	        	
 	        		myTreeSet.add(it.infn.cnaf.forge.glueschema.spec.v12.r2.StorageElementDocument.Factory.parse(storageEl).getStorageElement());
 	            
 	        	} catch (Exception e) {
 	        		
 	        	}
 	        }	
 	        storageElements = myTreeSet.
 	        	toArray(new it.infn.cnaf.forge.glueschema.spec.v12.r2.StorageElementType[myTreeSet.size()]);
 			
 	    } catch (ParseException e) {
 	    	
 	    }
 	    return storageElements;
     }
     
     public String[] getGridFTPServersForStorageElementAtSite(String site, String storageElement) {
     	String[] gridftpServers = null;
         
 	  	String xpathQuery;
 	    try {
         	xpathQuery = 
         		transformer.transform("get AccessProtocol where " +
         				"Site.Name='" + site + 
         				"' and StorageElement.UniqueID='" + storageElement + 
         				"' and AccessProtocol.Type='gsiftp'");
         	
         	logger.debug("xpath: " + xpathQuery);
 	       
 			NodeList storageElementNodeList = qEngine.turboMDSquery(xpathQuery);
 			// create a String array to house the list of codes available
 			// module = new String[codeVersionNodeList.getLength()];
 			logger.debug("SE element length: " + storageElementNodeList.getLength());
 
 			Element storageEl;
 			
 			// Keep unique using TreeSet
 	        TreeSet<String> myTreeSet = new TreeSet<String>();
 	
 	        for (int i = 0; i < storageElementNodeList.getLength(); i++)
 	        {
 	        	storageEl = (Element) storageElementNodeList.item(i);
 	            myTreeSet.add(qEngine.getTextValue(storageEl, "Endpoint"));
 	        }
 	
 	        gridftpServers = myTreeSet.toArray(new String[myTreeSet.size()]);
 			
 	    } catch (ParseException e) {
 	    	
 	    }
 	    return gridftpServers;
     }
     
     public String getDefaultStorageElementForQueueAtSite(String site, String queue) {
     	String defaultStorageElement = null;
         
         String xpathQuery;
  	    try {
           	xpathQuery = 
           		transformer.transform(
           				"get ComputingElement where Site.Name='" + site + 
           				"' and ComputingElement.Name='" + queue + "'");
           	logger.debug("xpath: " + xpathQuery);
  	    
 
 	        Element computingEl;
 	        NodeList computingElementNodeList = qEngine.turboMDSquery(xpathQuery);
 	
 	        // TODO: might probably need to come up with a better way of doing this
 	        // than iterating through the list of packages returned by the query
 	        for (int i = 0; i < computingElementNodeList.getLength(); i++) {
 	        	computingEl = (Element) computingElementNodeList.item(i);
 	        	defaultStorageElement = qEngine.getTextValue(computingEl, "DefaultSE");
 	        }
  	    } catch (ParseException e) {
 	    	
 	    }
         return defaultStorageElement;
     }
     
     public String[] getDataDir(String site, String storageElement, String fqan) {
     	String[] dataDirs = null;
         
 	  	String xpathQuery;
 	    try {
         	xpathQuery = 
         		transformer.transform("get StorageArea where " +
         				"Site.Name='" + site + 
         				"' and StorageElement.UniqueID='" + storageElement + 
         				"' and ACL.Rule='" + fqan + "'");
         	
         	logger.debug("xpath: " + xpathQuery);
 	       
 			NodeList storageAreaNodeList = qEngine.turboMDSquery(xpathQuery);
 			logger.debug("SA element length: " + storageAreaNodeList.getLength());
 
 			Element storageArea;
 			
 			// Keep unique using TreeSet
 	        TreeSet<String> myTreeSet = new TreeSet<String>();
 	
 	        for (int i = 0; i < storageAreaNodeList.getLength(); i++)
 	        {
 	        	storageArea = (Element) storageAreaNodeList.item(i);
 	            myTreeSet.add(qEngine.getTextValue(storageArea, "Path"));
 	        }
 	
 	        dataDirs = myTreeSet.toArray(new String[myTreeSet.size()]);
 			
 	    } catch (ParseException e) {
 	    	
 	    }
 	    return dataDirs;
     }
     
     public String getStorageElementForGridFTPServer(String gridFtp) {
     	String storageElement = null;
         String xpathQuery;
         try {        	
         	// try first if the host is a GridFTP Server
         	xpathQuery = transformer.transform("get StorageElement where AccessProtocol.Endpoint like '%" + gridFtp + "%'");
         	
         	logger.debug("xpath: " + xpathQuery);
         	
         	// Query MDS file.
         	NodeList storageElementNodeList = qEngine.turboMDSquery(xpathQuery);
         
             Element storageEl;
         
         	for (int i = 0; i < storageElementNodeList.getLength(); i++)
 	        {
 	        	storageEl = (Element) storageElementNodeList.item(i);
 	            storageElement = qEngine.getAttributeValue(storageEl, "UniqueID");
 	        }   	
         	
         } catch (ParseException e) {
         	
     	}    
     	return storageElement;
     }
     
     
     
     
     
     // **************************************************
     // GERSON: SoftwarePackage specific methods follow...
     
     /**
      * Get an array of all the different (unique) codes available on the Grid.
      * 
      * @return An array of available codes
      */
     public String[] getCodesOnGrid() {
     	String[] codes = null;
         
         String xpathQuery;
  	    try {
           	xpathQuery = 
           		transformer.transform(
           				"get SoftwarePackage");
           	logger.debug("xpath: " + xpathQuery);
  	    
 
 	        Element softwarePackageEl;
 	        NodeList codeVersionNodeList = qEngine.turboMDSquery(xpathQuery);
 	
 	        TreeSet<String> myTreeSet = new TreeSet<String>();
 	        for (int i = 0; i < codeVersionNodeList.getLength(); i++) {
 	        	softwarePackageEl = (Element) codeVersionNodeList.item(i);
 	            myTreeSet.add(qEngine.getTextValue(softwarePackageEl, "Name"));
 	        }
 	
 	        codes = myTreeSet.toArray(new String[myTreeSet.size()]);
  	    } catch (ParseException e) {
 	    	
 	    }
         return codes;
     }
     
     /**
      * Get an array of all the different (unique) codes available on the Grid.
      * 
      * @return An array of available codes
      */
     public String[] getCodesOnGridForVO(String fqan) {
     	String[] codes = null;
         
         String xpathQuery;
  	    try {
           	xpathQuery = 
           		transformer.transform(
           				"get SoftwarePackage where ACL.Rule='" + fqan + "'");
           	logger.debug("xpath: " + xpathQuery);
  	    
 
 	        Element softwarePackageEl;
 	        NodeList codeVersionNodeList = qEngine.turboMDSquery(xpathQuery);
 	
 	        TreeSet<String> myTreeSet = new TreeSet<String>();
 	        for (int i = 0; i < codeVersionNodeList.getLength(); i++) {
 	        	softwarePackageEl = (Element) codeVersionNodeList.item(i);
 	            myTreeSet.add(qEngine.getTextValue(softwarePackageEl, "Name"));
 	        }
 	
 	        codes = myTreeSet.toArray(new String[myTreeSet.size()]);
  	    } catch (ParseException e) {
 	    	e.printStackTrace();
 	    }
         return codes;
     }
     
     public it.infn.cnaf.forge.glueschema.spec.v12.r2.ClusterType 
         getClusterForCE(String ceUID) {
     	String xpathQuery;
  	    try {
          	xpathQuery = 
          		transformer.transform("get Cluster where ComputingElement.UniqueID='" +
          				ceUID + "'");
          	logger.debug("xpath: " + xpathQuery);
          	
          	NodeList clusterNodeList = qEngine.turboMDSquery(xpathQuery);
  			
  			logger.debug("Cluster element length: " + clusterNodeList.getLength());
  			
  			// the clusterNodeList will always be only one, if there are more 
  			// than one item returned, then there's something wrong with the MDS
  			// info. for now just assume that there's only one 			
 
  			org.w3c.dom.Node clusterEl;
  			
  			java.util.Set<it.infn.cnaf.forge.glueschema.spec.v12.r2.ClusterType> myTreeSet = 
  	        	new java.util.TreeSet<it.infn.cnaf.forge.glueschema.spec.v12.r2.ClusterType>();
  			
  	        for (int i = 0; i < clusterNodeList.getLength(); i++)
  	        {
  	        	clusterEl = (org.w3c.dom.Node) clusterNodeList.item(i);	        	
  	        	try {	        	
  	        		
  	        		myTreeSet.add(it.infn.cnaf.forge.glueschema.spec.v12.r2.ClusterDocument
 	        				.Factory.parse(clusterEl).getCluster());
  	        		
  	        		 	            
  	        	} catch (Exception e) {
  	        		
  	        	}
  	        }
  	        return myTreeSet.toArray(
  	        		new it.infn.cnaf.forge.glueschema.spec.v12.r2.ClusterType[myTreeSet.size()])[0];
          	
  	    } catch (ParseException e) {
  	    	
  	    }
     	
     	return null;
     }
     
     public it.infn.cnaf.forge.glueschema.spec.v12.r2.SiteType 
     getSiteForCE(String ceUID) {
 	String xpathQuery;
 	    try {
      	xpathQuery = 
      		transformer.transform("get Site where ComputingElement.UniqueID='" +
      				ceUID + "'");
      	logger.debug("xpath: " + xpathQuery);
      	
      	NodeList siteNodeList = qEngine.turboMDSquery(xpathQuery);
 			
 			logger.debug("Site element length: " + siteNodeList.getLength());
 			
 			// the clusterNodeList will always be only one, if there are more 
 			// than one item returned, then there's something wrong with the MDS
 			// info. for now just assume that there's only one 			
 
 			org.w3c.dom.Node siteEl;
 			
 			java.util.Set<it.infn.cnaf.forge.glueschema.spec.v12.r2.SiteType> myTreeSet = 
 	        	new java.util.TreeSet<it.infn.cnaf.forge.glueschema.spec.v12.r2.SiteType>();
 			
 	        for (int i = 0; i < siteNodeList.getLength(); i++)
 	        {
 	        	siteEl = (org.w3c.dom.Node) siteNodeList.item(i);	        	
 	        	try {	        	
 	        		
 	        		myTreeSet.add(it.infn.cnaf.forge.glueschema.spec.v12.r2.SiteDocument
         				.Factory.parse(siteEl).getSite());
 	        		
 	        		 	            
 	        	} catch (Exception e) {
 	        		
 	        	}
 	        }
 	        return myTreeSet.toArray(
 	        		new it.infn.cnaf.forge.glueschema.spec.v12.r2.SiteType[myTreeSet.size()])[0];
      	
 	    } catch (ParseException e) {
 	    	
 	    }
 	
 		return null;
 	}
     
     public it.infn.cnaf.forge.glueschema.spec.v12.r2.ComputingElementType[]
         getComputingElementsForApplicationAndVO(String code, String fqan) {
     	return getComputingElementsForApplicationAndVO(code, null, fqan);
     }
     
     public it.infn.cnaf.forge.glueschema.spec.v12.r2.ComputingElementType[]
         getComputingElementsForApplicationAndVO(String code, String version, String fqan) {
     	it.infn.cnaf.forge.glueschema.spec.v12.r2.ComputingElementType[] 
     	    computingElements = null;
     	
     	//it.infn.cnaf.forge.glueschema.spec.v12.r2.ClusterType[]
     	//    clusters = null;
     	             
  	  	String xpathQuery;
  	    try {
 	    	if ( StringUtils.isBlank(version) || Constants.NO_VERSION_INDICATOR_STRING.equals(version) ) {
 	         	xpathQuery = 
 	         		transformer.transform("get Cluster where ACL.Rule='" + fqan + "' and " +
 	         				"SoftwarePackage.Name='" + code + "'");
  	    	} else {
  	    		xpathQuery = 
  	    			transformer.transform("get Cluster where ACL.Rule='" + fqan + "' and " +
 	         				"SoftwarePackage.Name='" + code + "' and " +
 	         				"SoftwarePackage.Version='" + version + "'");
  	    	}
          	
          	logger.debug("xpath: " + xpathQuery);
  	       
  			NodeList clusterNodeList = qEngine.turboMDSquery(xpathQuery);
  			
  			logger.debug("Cluster element length: " + clusterNodeList.getLength());
 
  			org.w3c.dom.Node clusterEl;
  			
  			// Keep unique using TreeSet
  	        //java.util.List<it.infn.cnaf.forge.glueschema.spec.v12.r2.ClusterType> myTreeSet = 
  	        //	new java.util.ArrayList<it.infn.cnaf.forge.glueschema.spec.v12.r2.ClusterType>();
  	
  			java.util.List<it.infn.cnaf.forge.glueschema.spec.v12.r2.ComputingElementType> myTreeSet = 
  	        	new java.util.ArrayList<it.infn.cnaf.forge.glueschema.spec.v12.r2.ComputingElementType>();
  			
  	        for (int i = 0; i < clusterNodeList.getLength(); i++)
  	        {
  	        	clusterEl = (org.w3c.dom.Node) clusterNodeList.item(i);	        	
  	        	try {	        	
  	        		
  	        		it.infn.cnaf.forge.glueschema.spec.v12.r2.ComputingElementType[]
  	        		    ceArrayTmp = it.infn.cnaf.forge.glueschema.spec.v12.r2.ClusterDocument
 	        				.Factory.parse(clusterEl).getCluster().getComputingElementArray();
  	        		
  	        		// now only find the CEs that give permission to fqan
  	        		for (int j = 0; j < ceArrayTmp.length; j++) {
  	        			String[] ruleArray = ceArrayTmp[j].getACL().getRuleArray();
  	        			for (int k = 0; k < ruleArray.length; k++) {
  	        				if (ruleArray[k].equals(fqan)) {
  	        					myTreeSet.add(ceArrayTmp[j]);
  	        					break;
  	        				} 	        				
  	        			}     			
  	        		}
  	        		//myTreeSet.add(it.infn.cnaf.forge.glueschema.spec.v12.r2.ClusterDocument
  	        		//		.Factory.parse(clusterEl).getCluster()); 	            
  	        	} catch (Exception e) {
  	        		
  	        	}
  	        }	
  	        computingElements = myTreeSet.
  	        	toArray(new it.infn.cnaf.forge.glueschema.spec.v12.r2.ComputingElementType[myTreeSet.size()]);
  			
  	    } catch (ParseException e) {
  	    	
  	    }
  	    return computingElements;
     }
     
     
     
     /**
      * Get an array of all the different versions of a Code available on the
      * Grid.
      * 
      * @param code The code to check
      * @return An array of version labels
      */
     public String[] getVersionsOfCodeOnGrid(String code) {
     	String[] versions = null;
         
         String xpathQuery;
  	    try {
           	xpathQuery = 
           		transformer.transform(
           				"get SoftwarePackage where SoftwarePackage.Name='" + code + "'");
           	logger.debug("xpath: " + xpathQuery);
  	    
 
 	        Element softwarePackageEl;
 	        NodeList codeVersionNodeList = qEngine.turboMDSquery(xpathQuery);
 	
 	        TreeSet<String> myTreeSet = new TreeSet<String>();
 	        for (int i = 0; i < codeVersionNodeList.getLength(); i++) {
 	        	softwarePackageEl = (Element) codeVersionNodeList.item(i);
 	        	
 	        	String codeName = qEngine.getTextValue(softwarePackageEl, "Name");
 	        	
 	        	if ( code.toLowerCase().equals(codeName.toLowerCase()) ) {
 	        		myTreeSet.add(qEngine.getTextValue(softwarePackageEl, "Version"));
 	        	}
 	        }
 	
 	        versions = myTreeSet.toArray(new String[myTreeSet.size()]);
  	    } catch (ParseException e) {
 	    	
 	    }
         return versions;
     }
     
     /**
      * Get an array of all the different versions of a Code available on the
      * Grid for a certain VO.
      * 
      * @param code The code to check
      * @param fqan the fqan of the vo
      * @return An array of version labels
      */
     public String[] getVersionsOfCodeOnGridForVO(String code, String fqan) {
     	String[] versions = null;
         
         String xpathQuery;
  	    try {
           	xpathQuery = 
           		transformer.transform(
           				"get SoftwarePackage where ACL.Rule='" + fqan + "' and SoftwarePackage.Name='" + code + "'");
           	logger.debug("xpath: " + xpathQuery);
  	    
 
 	        Element softwarePackageEl;
 	        NodeList codeVersionNodeList = qEngine.turboMDSquery(xpathQuery);
 	
 	        TreeSet<String> myTreeSet = new TreeSet<String>();
 	        for (int i = 0; i < codeVersionNodeList.getLength(); i++) {
 	        	softwarePackageEl = (Element) codeVersionNodeList.item(i);
 	        	
 	        	String codeName = qEngine.getTextValue(softwarePackageEl, "Name");
 	        	
 	        	if ( code.toLowerCase().equals(codeName.toLowerCase()) ) {
 	        		myTreeSet.add(qEngine.getTextValue(softwarePackageEl, "Version"));
 	        	}
 	        }
 	
 	        versions = myTreeSet.toArray(new String[myTreeSet.size()]);
  	    } catch (ParseException e) {
 	    	
 	    }
         return versions;
     }
     
     /**
      * Get all the codes at a particular site.
      * 
      * @param site The site to check
      * @return An array of codes
      */
     public String[] getCodesAtSite(String site) {
     	String[] codes = null;
         
         String xpathQuery;
  	    try {
           	xpathQuery = 
           		transformer.transform(
           				"get SoftwarePackage where Site.Name='" + site + "'");
           	logger.debug("xpath: " + xpathQuery);
  	    
 
 	        Element softwarePackageEl;
 	        NodeList codeVersionNodeList = qEngine.turboMDSquery(xpathQuery);
 	
 	        TreeSet<String> myTreeSet = new TreeSet<String>();
 	        for (int i = 0; i < codeVersionNodeList.getLength(); i++) {
 	        	softwarePackageEl = (Element) codeVersionNodeList.item(i);
 	            myTreeSet.add(qEngine.getTextValue(softwarePackageEl, "Name"));
 	        }
 	
 	        codes = myTreeSet.toArray(new String[myTreeSet.size()]);
  	    } catch (ParseException e) {
 	    	
 	    }
         return codes;
     }
     
     /**
      * Get all the versions of a particular code at a site.
      * 
      * @param site The site to check
      * @param code The code to check
      * @return An array of versions
      */
     public String[] getVersionsOfCodeAtSite(String site, String code) {
     	String[] versions = null;
         
         String xpathQuery;
  	    try {
           	xpathQuery = 
           		transformer.transform(
           				"get SoftwarePackage where Site.Name='" + site + 
           				"' and SoftwarePackage.Name='" + code + "'");
           	logger.debug("xpath: " + xpathQuery);
  	    
 
 	        Element softwarePackageEl;
 	        NodeList codeVersionNodeList = qEngine.turboMDSquery(xpathQuery);
 	
 	        TreeSet<String> myTreeSet = new TreeSet<String>();
 	        for (int i = 0; i < codeVersionNodeList.getLength(); i++) {
 	        	softwarePackageEl = (Element) codeVersionNodeList.item(i);
 	            myTreeSet.add(qEngine.getTextValue(softwarePackageEl, "Version"));
 	        }
 	
 	        versions = myTreeSet.toArray(new String[myTreeSet.size()]);
  	    } catch (ParseException e) {
 	    	
 	    }
         return versions;
     }
     
     public String[] getVersionsOfCodeForQueueAndContactString(String queueName, String contactString, String code) {
     	String[] versions = null;
         
 //    	System.out.println("Getting versions of code for queue and contact string: "+code+" "+queueName+" "+contactString);
     	
         String xpathQuery;
  	    try {
           	xpathQuery = 
           		transformer.transform(
           				"get SoftwarePackage where ComputingElement.Name='" + queueName + 
           				"' and ComputingElement.ContactString like '%" + contactString + 
           				"%' and SoftwarePackage.Name='" + code + "'");
           	logger.debug("xpath: " + xpathQuery);
  	    
 
 	        Element softwarePackageEl;
 	        NodeList codeVersionNodeList = qEngine.turboMDSquery(xpathQuery);
 	
 	        TreeSet<String> myTreeSet = new TreeSet<String>();
 	        for (int i = 0; i < codeVersionNodeList.getLength(); i++) {
 	        	softwarePackageEl = (Element) codeVersionNodeList.item(i);
 	            myTreeSet.add(qEngine.getTextValue(softwarePackageEl, "Version"));
 	        }
 	
 	        versions = myTreeSet.toArray(new String[myTreeSet.size()]);
  	    } catch (ParseException e) {
 	    	
 	    }
  	    
  	    
         return versions;
     }
     
     public String[] getVersionsOfCodeForCE(String computingElementUID, String code) {
     	String[] versions = null;
         
         String xpathQuery;
  	    try {
           	xpathQuery = 
           		transformer.transform(
           				"get SoftwarePackage where ComputingElement.UniqueID='" + computingElementUID + 
           				"' and SoftwarePackage.Name='" + code + "'");
           	logger.debug("xpath: " + xpathQuery);
  	    
 
 	        Element softwarePackageEl;
 	        NodeList codeVersionNodeList = qEngine.turboMDSquery(xpathQuery);
 	
 	        TreeSet<String> myTreeSet = new TreeSet<String>();
 	        for (int i = 0; i < codeVersionNodeList.getLength(); i++) {
 	        	softwarePackageEl = (Element) codeVersionNodeList.item(i);
 	            myTreeSet.add(qEngine.getTextValue(softwarePackageEl, "Version"));
 	        }
 	
 	        versions = myTreeSet.toArray(new String[myTreeSet.size()]);
  	    } catch (ParseException e) {
 	    	
 	    }
         return versions;
     }    
 
     public String[] getClustersForCodeAtSite(String site, String code, String version) {
     	return null;
     }
     
     public String[] getQueueNamesForCodeAtSite(String site, String code, String version) {
     	String[] queueNames = null;
 
         String xpathQuery;
         try {
         	if ( StringUtils.isBlank(version) || Constants.NO_VERSION_INDICATOR_STRING.equals(version) ) {
         	xpathQuery = transformer.transform(
       				"get ComputingElement where Site.Name='" + site + 
       				"' and SoftwarePackage.Name='" + code + "'"
         			);
         	} else {
         	xpathQuery = 
           		transformer.transform(
           				"get ComputingElement where Site.Name='" + site + 
           				"' and SoftwarePackage.Version='" + version + 
           				"' and SoftwarePackage.Name='" + code + "'");
         	}
         	logger.debug("xpath: " + xpathQuery);
         	
 	        // Query MDS file.
 	        Element siteEl;
 	        NodeList queuesNodeList = qEngine.turboMDSquery(xpathQuery);
 	
 	        // Keep unique using TreeSet.
 	        TreeSet<String> myTreeSet = new TreeSet<String>();
 	
 	        for (int i = 0; i < queuesNodeList.getLength(); i++)
 	        {
 	            siteEl = (Element) queuesNodeList.item(i);
 	            myTreeSet.add(qEngine.getTextValue(siteEl, "Name"));
 	        }
 	
 	        // Shove in array.
 	        queueNames = myTreeSet.toArray(new String[myTreeSet.size()]);
         } catch (ParseException e) {
         }
 
         return queueNames;
     }
     
     public String[] getQueueNamesForCodeAtSite(String site, String code) {
     	String[] queueNames = null;
 
     	
         String xpathQuery;
         try {
         	xpathQuery = 
           		transformer.transform(
           				"get ComputingElement where Site.Name='" + site + 
           				"' and SoftwarePackage.Name='" + code + "'");
 	
         	logger.debug("xpath: " + xpathQuery);
         	
 	        // Query MDS file.
 	        Element siteEl;
 	        NodeList queuesNodeList = qEngine.turboMDSquery(xpathQuery);
 	
 	        // Keep unique using TreeSet.
 	        TreeSet<String> myTreeSet = new TreeSet<String>();
 	
 	        for (int i = 0; i < queuesNodeList.getLength(); i++)
 	        {
 	            siteEl = (Element) queuesNodeList.item(i);
 	            myTreeSet.add(qEngine.getTextValue(siteEl, "Name"));
 	        }
 	
 	        // Shove in array.
 	        queueNames = myTreeSet.toArray(new String[myTreeSet.size()]);
         } catch (ParseException e) {
         }
 
         return queueNames;
     }
     
     /**
      * Get a list of all sites that have a particular version of the specified
      * code.
      * 
      * @param code The code to check
      * @param version The version of the code to check
      * @return A list of sites that have the specified version of the code
      */
     public String[] getSitesWithAVersionOfACode(String code, String version) {
     	String[] sites = null;
         String xpathQuery;
         try {
         	xpathQuery = transformer.transform(
         			"get Site where SoftwarePackage.Name='" + code +
         			"' and SoftwarePackage.Version='" + version + "'");
         	
         	logger.debug("xpath: " + xpathQuery);
         	
         	// Query MDS file.
         	NodeList hostLists = qEngine.turboMDSquery(xpathQuery);
         
             Element siteEl;
         	
         	// Keep sites Unique using TreeSet.
         	TreeSet<String> myTreeSet = new TreeSet<String>();
         
         	for (int i = 0; i < hostLists.getLength(); i++)
             {
         		siteEl = (Element) hostLists.item(i);
         		myTreeSet.add(qEngine.getTextValue(siteEl, "Name"));
         	}
         
             // Shove it into a String[] array.
         	sites = myTreeSet.toArray(new String[myTreeSet.size()]);
         } catch (ParseException e) {
         	
     	}    
     	return sites;
     }
     
     public String[] getSitesWithCode(String code) {
     	String[] sites = null;
         String xpathQuery;
         try {
         	xpathQuery = transformer.transform(
         			"get Site where SoftwarePackage.Name='" + code +"'");
         	
         	logger.debug("xpath: " + xpathQuery);
         	
         	// Query MDS file.
         	NodeList hostLists = qEngine.turboMDSquery(xpathQuery);
         
             Element siteEl;
         	
         	// Keep sites Unique using TreeSet.
         	TreeSet<String> myTreeSet = new TreeSet<String>();
         
         	for (int i = 0; i < hostLists.getLength(); i++)
             {
         		siteEl = (Element) hostLists.item(i);
         		myTreeSet.add(qEngine.getTextValue(siteEl, "Name"));
         	}
         
             // Shove it into a String[] array.
         	sites = myTreeSet.toArray(new String[myTreeSet.size()]);
         } catch (ParseException e) {
         	
     	}    
     	return sites;
     }
     
     // GERSON: An issue with the three methods below is that they both assume that
     // a site with more than one cluster will have the same software executable 
     // naming convention on the hosts where these applications are installed
     
     /**
      * Get the name of the executable to be run. This may or may not be same as
      * the name of the code. For instance 'List' maps to <code>ls</code>.
      *  
      * @param site The site to check
      * @param code The code to check
      * @param version The version of the code
      * @return The executable name of the code
      */
     public String[] getExeNameOfCodeAtSite(String site, String code, String version) {
     	String[] executables = null;
         
         String xpathQuery;
  	    try {
           	xpathQuery = 
           		transformer.transform(
           				"get SoftwareExecutable where Site.Name='" + site + 
           				"' and SoftwarePackage.Version='" + version + 
           				"' and SoftwarePackage.Name='" + code + "'");
           	logger.debug("xpath: " + xpathQuery);
  	    
 
 	        Element softwareExecutableEl;
 	        NodeList softwareExecutableNodeList = qEngine.turboMDSquery(xpathQuery);
 	
 	        TreeSet<String> myTreeSet = new TreeSet<String>();
 	        for (int i = 0; i < softwareExecutableNodeList.getLength(); i++) {
 	        	softwareExecutableEl = (Element) softwareExecutableNodeList.item(i);
 	            myTreeSet.add(qEngine.getTextValue(softwareExecutableEl, "Name"));
 	        }
 	
 	        executables = myTreeSet.toArray(new String[myTreeSet.size()]);
 //	        System.out.println("--------------------------------");
 //	        System.out.println("getExeNameOfCodeAtSite for: "+site+" "+code+" "+version);
 //	        for (String exe : executables ) {
 //	        	System.out.println(exe);
 //	        }
 //	        System.out.println("--------------------------------");
 
  	    } catch (ParseException e) {
 	    	
 	    }
         return executables;
     }
     
     /**
      * Get the name of the executable to be run. This may or may not be same as
      * the name of the code. For instance 'List' maps to <code>ls</code>.
      *  
      * @param site The site to check
      * @param code The code to check
      * @param version The version of the code
      * @return The executable name of the code
      */
     public String[] getExeNameOfCodeAtQueueAtSite(String site, String queue, String code, String version) {
     	String[] executables = null;
         
         String xpathQuery;
  	    try {
           	xpathQuery = 
           		transformer.transform(
           				"get SoftwareExecutable where Site.Name='" + site + 
           				"' and ComputingElement.Name='" + queue + 
           				"' and SoftwarePackage.Version='" + version + 
           				"' and SoftwarePackage.Name='" + code + "'");
           	logger.debug("xpath: " + xpathQuery);
  	    
 
 	        Element softwareExecutableEl;
 	        NodeList softwareExecutableNodeList = qEngine.turboMDSquery(xpathQuery);
 	
 	        TreeSet<String> myTreeSet = new TreeSet<String>();
 	        for (int i = 0; i < softwareExecutableNodeList.getLength(); i++) {
 	        	softwareExecutableEl = (Element) softwareExecutableNodeList.item(i);
 	            myTreeSet.add(qEngine.getTextValue(softwareExecutableEl, "Name"));
 	        }
 	
 	        executables = myTreeSet.toArray(new String[myTreeSet.size()]);
 	        
 //	        System.out.println("--------------------------------");
 //	        System.out.println("getExeNameOfCodeAtQueueAtSite for: "+site+" "+queue+" "+code+" "+version);
 //	        for (String exe : executables ) {
 //	        	System.out.println(exe);
 //	        }
 //	        System.out.println("--------------------------------");
  	    } catch (ParseException e) {
 	    	
 	    }
         return executables;
     }
     
     /**
      * Get the job type that the specified code supports.
      * 
      * @param site The site to check
      * @param code The code to check
      * @param version The version of the code
      * @return The job type
      */
     public String getJobTypeOfCodeAtSite(String site, String code, String version) {
     	return null;
     }
     
     /**
      * Get the name of the module required by a particular version of a code at 
      * a particular site. 
      * 
      * @param site The site to check
      * @param code The code to check
      * @param version The version of the code
      * @return The name of the module needed
      */
     public String getModuleNameOfCodeAtSite(String site, String code, String version) {
     	String moduleName = null;
         
         String xpathQuery;
         String xpathQueryString;
         if ( version == null || version.length() == 0 || Constants.NO_VERSION_INDICATOR_STRING.equals(version)) {
         	throw new RuntimeException("Version is not specified.");
         } else {
         	xpathQueryString = "get SoftwarePackage where Site.Name='" + site + 
 			"' and SoftwarePackage.Version='" + version + 
 				"' and SoftwarePackage.Name='" + code + "'";
         }
  	    try {
           	xpathQuery = 
           		transformer.transform(xpathQueryString);
           	logger.debug("xpath: " + xpathQuery);
  	    
 
 	        Element softwarePackageEl;
 	        NodeList softPackNodeList = qEngine.turboMDSquery(xpathQuery);
 	
 	        // TODO: might probably need to come up with a better way of doing this
 	        // than iterating through the list of packages returned by the query
 	        for (int i = 0; i < softPackNodeList.getLength(); i++) {
 	        	softwarePackageEl = (Element) softPackNodeList.item(i);
 	            moduleName = qEngine.getTextValue(softwarePackageEl, "Module");
 	        }
  	    } catch (ParseException e) {
 	    	
 	    }
         return moduleName;
     }
     
     public boolean isSerialAvailForCodeAtSite(String site, String code, String version) {
     	boolean serialAvail = true;
         
         String xpathQuery;
         String xpathQueryString;
         
         if (version == null || version.length() == 0 || Constants.NO_VERSION_INDICATOR_STRING.equals(version) ) {
         	throw new RuntimeException("No version specified.");
         } else {
         	xpathQueryString = "get SoftwarePackage where Site.Name='" + site + 
 				"' and SoftwarePackage.Version='" + version + 
   				"' and SoftwarePackage.Name='" + code + "'";
         }
  	    try {
           	xpathQuery = 
           		transformer.transform(xpathQueryString);
           	logger.debug("xpath: " + xpathQuery);
  	    
 
 	        Element softwarePackageEl;
 	        NodeList softPackNodeList = qEngine.turboMDSquery(xpathQuery);
 	
 	        // TODO: might probably need to come up with a better way of doing this
 	        // than iterating through the list of packages returned by the query
 	        for (int i = 0; i < softPackNodeList.getLength(); i++) {
 	        	softwarePackageEl = (Element) softPackNodeList.item(i);
 	            String serialAvailStr = qEngine.getTextValue(softwarePackageEl, "SerialAvail");
 	            if (serialAvailStr != null)
 	            	serialAvail = Boolean.parseBoolean(serialAvailStr);
 	        }
  	    } catch (ParseException e) {
 	    	
 	    }
         return serialAvail;
     }
     
     public boolean isParallelAvailForCodeAtSite(String site, String code, String version) {
     	boolean parallelAvail = false;
         
         String xpathQuery;
         
         if ( version == null || version.length() == 0 || Constants.NO_VERSION_INDICATOR_STRING.equals(version)) {
         	throw new RuntimeException("No version specified.");
         }
  	    try {
           	xpathQuery = 
           		transformer.transform(
           				"get SoftwarePackage where Site.Name='" + site + 
           				"' and SoftwarePackage.Version='" + version + 
           				"' and SoftwarePackage.Name='" + code + "'");
           	logger.debug("xpath: " + xpathQuery);
  	    
 
 	        Element softwarePackageEl;
 	        NodeList softPackNodeList = qEngine.turboMDSquery(xpathQuery);
 	
 	        // TODO: might probably need to come up with a better way of doing this
 	        // than iterating through the list of packages returned by the query
 	        for (int i = 0; i < softPackNodeList.getLength(); i++) {
 	        	softwarePackageEl = (Element) softPackNodeList.item(i);
 	            String parallelAvailStr = qEngine.getTextValue(softwarePackageEl, "ParallelAvail");
 	            if (parallelAvailStr != null) {
 	            	parallelAvail = Boolean.parseBoolean(parallelAvailStr);
 	            }
 	        }
  	    } catch (ParseException e) {
 	    	
 	    }
         return parallelAvail;
     }
     
     
     
     
     
     // ***************************************************
     // GERSON: ComputingElement specific methods follow...    
     
     /**
      * Get the names of all the queues at a particular site.
      * 
      * @param site The site to check
      * @return An array of job queues
      */
     public String[] getQueueNamesAtSite(String site) {
     	String[] queueNames = null;
 
         String xpathQuery;
         try {
         	logger.debug("calling: getQueueNamesAtSite("+ site + ")");
         	xpathQuery = transformer.transform(
         			"get ComputingElement where Site.Name='" + site + "'");
 	
         	logger.debug("xpath: " + xpathQuery);
         	
 	        // Query MDS file.
 	        Element siteEl;
 	        NodeList queuesNodeList = qEngine.turboMDSquery(xpathQuery);
 	
 	        // Keep unique using TreeSet.
 	        TreeSet<String> myTreeSet = new TreeSet<String>();
 	
 	        for (int i = 0; i < queuesNodeList.getLength(); i++)
 	        {
 	            siteEl = (Element) queuesNodeList.item(i);
 	            myTreeSet.add(qEngine.getTextValue(siteEl, "Name"));
 	        }
 	
 	        // Shove in array.
 	        queueNames = myTreeSet.toArray(new String[myTreeSet.size()]);
         } catch (ParseException e) {
         }
 
         return queueNames;
     }
     
     /**
      * Get the names of all the queues at a particular site.
      * 
      * @param site The site to check
      * @return An array of job queues
      */
     public String[] getQueueNamesAtSite(String site, String fqan) {
     	String[] queueNames = null;
 
         String xpathQuery;
         try {
         	logger.debug("calling: getQueueNamesAtSite("+ site + ", "+ fqan +")");
         	xpathQuery = transformer.transform(
         			"get ComputingElement where Site.Name='" + site + "' and ACL.Rule='" + fqan + "'");
 	
         	logger.debug("xpath: " + xpathQuery);
         	
 	        // Query MDS file.
 	        Element siteEl;
 	        NodeList queuesNodeList = qEngine.turboMDSquery(xpathQuery);
 	
 	        // Keep unique using TreeSet.
 	        TreeSet<String> myTreeSet = new TreeSet<String>();
 	
 	        for (int i = 0; i < queuesNodeList.getLength(); i++)
 	        {
 	            siteEl = (Element) queuesNodeList.item(i);
 	            myTreeSet.add(qEngine.getTextValue(siteEl, "Name"));
 	        }
 	
 	        // Shove in array.
 	        queueNames = myTreeSet.toArray(new String[myTreeSet.size()]);
         } catch (ParseException e) {
         }
 
         return queueNames;
     }
     
     
     /**
      * Get the job manager of the cluster at a particular site.
      * 
      * @param site The site to check
      * @param queue The name of the queue
      * @return The job manager of the particular queue
      */
     public String getJobManagerOfQueueAtSite(String site, String queue) {
     	String jobManager = null;
         
 	  	String xpathQuery;
 	    try {
         	xpathQuery = 
         		transformer.transform("get ComputingElement where " +
         				"Site.Name='" + site + 
         				"' and ComputingElement.Name='" + queue + "'");
         	
         	logger.debug("xpath: " + xpathQuery);
 	       
 			NodeList computeElementNodeList = qEngine.turboMDSquery(xpathQuery);
 			// create a String array to house the list of codes available
 			// module = new String[codeVersionNodeList.getLength()];
 			logger.debug("CE element length: " + computeElementNodeList.getLength());
 
 			Element computeEl;
 			
 			// Keep unique using TreeSet
 	        TreeSet<String> myTreeSet = new TreeSet<String>();
 	
 	        for (int i = 0; i < computeElementNodeList.getLength(); i++)
 	        {
 	        	computeEl = (Element) computeElementNodeList.item(i);
 	        	jobManager = qEngine.getTextValue(computeEl, "JobManager");
 	        }
 			
 	    } catch (ParseException e) {
 	    	
 	    }
 	    return jobManager;
     }    
     
     /**
      * Get the GRAM URIs of the queue at the site
      * @param site
      * @param queue
      * @return contact string of the queue
      */
     public String[] getContactStringOfQueueAtSite(String site, String queue) {
     	String[] hostAddresses = null;
         
 	  	String xpathQuery;
 	    try {
         	xpathQuery = 
         		transformer.transform("get ComputingElement where " +
         				"Site.Name='" + site + 
         				"' and ComputingElement.Name='" + queue + "'");
         	
         	logger.debug("xpath: " + xpathQuery);
 	       
 			NodeList computeElementNodeList = qEngine.turboMDSquery(xpathQuery);
 			// create a String array to house the list of codes available
 			// module = new String[codeVersionNodeList.getLength()];
 			logger.debug("CE element length: " + computeElementNodeList.getLength());
 
 			Element computeEl;
 			
 			// Keep unique using TreeSet
 	        TreeSet<String> myTreeSet = new TreeSet<String>();
 	
 	        for (int i = 0; i < computeElementNodeList.getLength(); i++)
 	        {
 	        	computeEl = (Element) computeElementNodeList.item(i);
 	            myTreeSet.add(qEngine.getTextValue(computeEl, "ContactString"));
 	        }
 	
 	        hostAddresses = myTreeSet.toArray(new String[myTreeSet.size()]);
 			
 	    } catch (ParseException e) {
 	    	
 	    }
 	    return hostAddresses;
     }
     
     // GERSON: has been renamed from getComputeElementsOfClusterAtSite to be 
     // consistent with calling ComputingElements as Queues
     
     /**
      * Get the queue names of the cluster at site
      * site.
      * 
      * @param site The site to check
      * @return An array of compute elements
      */
     public String[] getQueueNamesForClusterAtSite(String site, String cluster) {
     	return null;
     }    
     
     
     // ***************************************************
     // GERSON: Cluster specific methods follow...  
     
     /**
      * Get the names of all the clusters at a particular site.
      * 
      * @param site The site to check
      * @return An array of cluster names
      */
     public String[] getClusterNamesAtSite(String site) {
     	return null;
     }
     
     public String getLRMSTypeOfQueueAtSite(String site, String queue) {
     	String lrmsType = null;
         
 	  	String xpathQuery;
 	    try {
         	xpathQuery = 
         		transformer.transform("get ComputingElement where " +
         				"Site.Name='" + site + 
         				"' and ComputingElement.Name='" + queue + "'");
         	
         	logger.debug("xpath: " + xpathQuery);
 	       
 			NodeList computeElementNodeList = qEngine.turboMDSquery(xpathQuery);
 			// create a String array to house the list of codes available
 			// module = new String[codeVersionNodeList.getLength()];
 			logger.debug("CE element length: " + computeElementNodeList.getLength());
 
 			Element computeEl;
 			
 			// Keep unique using TreeSet
 	        TreeSet<String> myTreeSet = new TreeSet<String>();
 	
 	        for (int i = 0; i < computeElementNodeList.getLength(); i++)
 	        {
 	        	computeEl = (Element) computeElementNodeList.item(i);
 	        	lrmsType = qEngine.getTextValue(computeEl, "LRMSType");
 	        }
 			
 	    } catch (ParseException e) {
 	    	
 	    }
 	    return lrmsType;
     }
     
     public static void main(String[] args) throws Exception {
     	Map<String, String[]> dataLocationsMap = new HashMap<String, String[]>();
 		
 		QueryClient client = new QueryClient();
 		String fqan = "/APACGrid/NGAdmin";
 		it.infn.cnaf.forge.glueschema.spec.v12.r2.StorageElementType[] storageElements = 
 			((QueryClient)client).getStorageElementsForVO(fqan);
 		if (storageElements != null) {
 			for (int j = 0; j < storageElements.length; j++) {
 				System.out.println("StorageElement: " + storageElements[j].getUniqueID());
 				it.infn.cnaf.forge.glueschema.spec.v12.r2.SEAccessProtocolType[] accessProtocols = 
 					storageElements[j].getAccessProtocolArray();
 				
 				
 				it.infn.cnaf.forge.glueschema.spec.v12.r2.StorageAreaType[] allStorageAreas =
 					storageElements[j].getStorageAreaArray();
 				
 				java.util.List<String> voStorageAreaList = 
 					new java.util.ArrayList<String>();
 				
 				for (int k = 0; k < allStorageAreas.length; k++) {
 					String[] aclTypes =
 						allStorageAreas[k].getACL().getRuleArray();
 					
 					for (int l = 0; l < aclTypes.length; l++) {
 						if (aclTypes[l].equalsIgnoreCase(fqan)) {
 							voStorageAreaList.add(allStorageAreas[k].getPath());
 						}
 					}
 				}
 				
 				for (int k = 0; k < accessProtocols.length; k++) {
 					System.out.println("AccessProtocol: " + accessProtocols[k].getEndpoint());
 					if (!voStorageAreaList.isEmpty()) {
 						dataLocationsMap.put(accessProtocols[k].getEndpoint(), 
 								(String[])voStorageAreaList.toArray(new String[voStorageAreaList.size()]));
 						logger.debug("gerson: " + accessProtocols[k].getEndpoint() + ": " 
 								+ (String[])voStorageAreaList.toArray(new String[voStorageAreaList.size()]));
 					}
 				}
 				
 			}
 			
 //			client.getExeNameOfCodeAtSite("Canterbury", "java", "jdk-1.6.0_04-fcs");
 //			
 //			client.getExeNameOfCodeAtQueueAtSite("Canterbury", "small", "java", "jdk-1.6.0_04-fcs");
 //			
 //			client.getApplicationNamesThatProvideExecutable("java");
 //			
 //			for (String code : client.getCodesOnGridForVO("/ARCS/NGAdmin") ) {
 //				System.out.println(code);
 //			}
 			
 			for (String version : client.getVersionsOfCodeOnGrid("java") ) {
 				System.out.println(version);
 			}
 			System.out.println("------------------------------");
 			for (String version : client.getVersionsOfCodeOnGridForVO("java", "/ARCS/VPAC") ) {
 				System.out.println(version);
 			}
 		}
     	
     	
     	
     }
     
     public static void testGrisu() {
     	// test for grisu
     	QueryClient client = new QueryClient();
     	
     	List<String> list = new ArrayList();
     	
     	// get all sites    	
     	String[] sites = client.getSitesWithCode("ls");
     	
     	for (int i = 0; sites != null && i < sites.length; i++) {
     		
     		System.out.println("site[" + i + "]: " + sites[i]);
     		
     		// once site names are found, find all the queues for each of the sites
     		String[] queues = client.getQueueNamesAtSite(sites[i]);
     		
     		for (int j = 0; queues != null && j < queues.length; j++) {
     			
     			// once queues are found, find all the contact string for all the queues
     			String[] contactStrings = 
     				client.getContactStringOfQueueAtSite(sites[i], queues[j]);
     			
     			for (int k = 0; contactStrings != null && k < contactStrings.length; k++) {
     				String hostname = contactStrings[k].substring(
     						contactStrings[k].indexOf("https://") != 0 ? 0 : 8,
     								contactStrings[k].indexOf(":8443"));        			
         			list.add(queues[j] + ":" + hostname);
     			}
     			
     		}
     	}
     	
     	for (String str : list) {
     		System.out.println(str);    		
     	}
     }
     
     private static void sitesOnGrid(QueryClient client) {
     	List<String> list = new ArrayList();
     	
     	// get all sites    	
     	String[] sites = client.getSitesOnGrid();
     	
     	for (int i = 0; sites != null && i < sites.length; i++) {
     		
     		// once site names are found, find all the queues for each of the sites
     		String[] queues = client.getQueueNamesAtSite(sites[i]);
     		
     		for (int j = 0; queues != null && j < queues.length; j++) {
     			
     			// once queues are found, find all the contact string for all the queues
     			String[] contactStrings = 
     				client.getContactStringOfQueueAtSite(sites[i], queues[j]);
     			
     			for (int k = 0; contactStrings != null && k < contactStrings.length; k++) {
     				String hostname = contactStrings[k].substring(
     						contactStrings[k].indexOf("https://") != 0 ? 0 : 8,
     								contactStrings[k].indexOf(":8443"));        			
         			list.add(queues[j] + ":" + hostname);
     			}
     			
     		}
     	}
     	
     	for (String str : list) {
     		System.out.println(str);    		
     	}
     }
     
     
     // Event stuff
     public void addGridInformationListener(GridInformationListener l) {
     	getQueryEngine().addGridInformationListener(l);
     }
     public void removeGridInformationListener(GridInformationListener l) {
     	getQueryEngine().removeGridInformationListener(l);
     }
 
 	public String[] getApplicationNamesThatProvideExecutable(String executable) {
 		
     	String[] applications = null;
         
         String xpathQuery;
  	    try {
           	xpathQuery = 
           		transformer.transform(
 //          				"get SoftwarePackage where SoftwareExecutable.Name like '%"+executable + "'"); 
           				"get SoftwarePackage where SoftwareExecutable.Name = '"+executable + "'"); 
           	logger.debug("xpath: " + xpathQuery);
  	    
 
 	        Element softwareExecutableEl;
 	        NodeList softwareExecutableNodeList = qEngine.turboMDSquery(xpathQuery);
 	
 	        TreeSet<String> myTreeSet = new TreeSet<String>();
 	        for (int i = 0; i < softwareExecutableNodeList.getLength(); i++) {
 	        	softwareExecutableEl = (Element) softwareExecutableNodeList.item(i);
 	            myTreeSet.add(qEngine.getTextValue(softwareExecutableEl, "Name"));
 	        }
 	
 	        applications = myTreeSet.toArray(new String[myTreeSet.size()]);
 //	        System.out.println("--------------------------------");
 //	        System.out.println("getApplicationNamesThatProvideExecutable for: "+executable);
 //	        for (String app : applications ) {
 //	        	System.out.println(app);
 //	        }
 //	        System.out.println("--------------------------------");
 
  	    } catch (ParseException e) {
 	    	e.printStackTrace();
 	    }
         return applications;
 	}
 }
