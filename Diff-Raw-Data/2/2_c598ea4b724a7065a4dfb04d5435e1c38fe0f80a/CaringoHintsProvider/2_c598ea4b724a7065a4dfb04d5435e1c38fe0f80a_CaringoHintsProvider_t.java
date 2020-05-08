 package edu.illinois.medusa;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.net.URLEncoder;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.TimeZone;
 
 import org.fcrepo.server.errors.ObjectIntegrityException;
 import org.fcrepo.server.errors.RepositoryConfigurationException;
 import org.fcrepo.server.errors.StreamIOException;
 import org.fcrepo.server.storage.FedoraStorageHintProvider;
 import org.fcrepo.server.storage.types.Datastream;
 import org.fcrepo.server.storage.types.DigitalObject;
 import org.fcrepo.server.utilities.DCField;
 import org.fcrepo.server.utilities.DCFields;
 
 /**
  * Send hint information from fedora to the low level storage
  */
 
 public class CaringoHintsProvider implements FedoraStorageHintProvider {
 
 	
 	Properties properties;
     HashSet<String> datastreamHeaders;
     HashSet<String> objectHeaders;
     HashSet<String> dublinCoreHeaders;
 	
     protected CaringoHintsProvider(String configFilePath) {
     	properties = loadConfigFile(configFilePath);
     	datastreamHeaders = new HashSet<String>();
     	objectHeaders = new HashSet<String>();
     	dublinCoreHeaders = new HashSet<String>();
     	loadDatastreamHeaders();
     	loadObjectHeaders();
     	loadDublinCoreHeaders();
     }
 
 	
 	/**
      * Load object headers from properties
      *
      */
 	protected void loadObjectHeaders() {
 		String objectHeaderString = properties.getProperty("fedora-headers.object");
 		if(objectHeaderString!=null){
 			for(String val:objectHeaderString.split(",")){
 				objectHeaders.add(val);
 			}
 		}
 	}
 
 
 	/**
      * Load datastream headers from properties
      *
      */
 	protected void loadDatastreamHeaders() {
 		String datastreamHeaderString = properties.getProperty("fedora-headers.datastream");
 		if(datastreamHeaderString!=null){
 			for(String val:datastreamHeaderString.split(",")){
 				datastreamHeaders.add(val);
 			}
 		}
 	}
 
 
 	/**
      * Load Dublin Core headers from properties
      *
      */
 	protected void loadDublinCoreHeaders() {
 		String dublinCoreHeaderString = properties.getProperty("fedora-headers.dublin-core");
 		if(dublinCoreHeaderString!=null){
 			for(String val:dublinCoreHeaderString.split(",")){
 				dublinCoreHeaders.add(val);
 			}
 		}
 	}
 	
 	/**
 	 * Add datastream headers according to the config file to the metadata map for the Datastream
 	 * Acceptable values for header: [createDT,state,versionable,versionID,label,checksum,checksumType,controlGrp,formatURI,infoType,location,locationType]
 	 * Any other values will be ignored.
 	 * 
 	 * @param object: The parent DigitalObject for the datastream
 	 * @param datastream: The Datastream for which metadata is to be added
 	 * @param metadata: a map which contains all metadata to be added for the datastream
 	 */
 	private void addDatastreamMetadataForDataStream(DigitalObject object, Datastream ds, Map<String,String> metadata){
 		String ds_full_path = object.getPid()+"/"+ds.DatastreamID+"/"+ds.DSVersionID;
 		metadata.put(":Content-Type",ds.DSMIME);
 		//metadata.put(":Content-Disposition",ds_full_path);
 		metadata.put("fedora:stream-id","info:fedora/"+ds_full_path);
 
 		if(datastreamHeaders.isEmpty())
 			return;
 
 		if(ds.DSCreateDT!=null && datastreamHeaders.contains("createDT"))
 			metadata.put("fedora:createDT",ds.DSCreateDT.toString());
 		if(ds.DSState!=null && datastreamHeaders.contains("state"))
 			metadata.put("fedora:state",ds.DSState);
 		if(datastreamHeaders.contains("versionable"))
 			metadata.put("fedora:versionable",ds.DSVersionable+"");
 		if(ds.DSVersionID!=null && datastreamHeaders.contains("versionID"))
 			metadata.put("fedora:versionID",ds.DSVersionID);
 		if(ds.DSLabel!=null && datastreamHeaders.contains("label"))
 			metadata.put("fedora:label",ds.DSLabel);
 		if(ds.DSChecksum!=null && datastreamHeaders.contains("checksum"))
 			metadata.put("fedora:checksum",ds.DSChecksum);
 		if(ds.DSChecksumType!=null && datastreamHeaders.contains("checksumType"))
 			metadata.put("fedora:checksum-type",ds.DSChecksumType);
 		if(ds.DSControlGrp!=null && datastreamHeaders.contains("controlGrp"))
 			metadata.put("fedora:controlgrp",ds.DSControlGrp);
 		if(ds.DSFormatURI!=null && datastreamHeaders.contains("formatURI"))
 			metadata.put("fedora:format-uri",ds.DSFormatURI);
 		if(ds.DSInfoType!=null && datastreamHeaders.contains("infoType"))
 			metadata.put("fedora:info-type",ds.DSInfoType);
 		if(ds.DSLocation!=null && datastreamHeaders.contains("location"))
 			metadata.put("fedora:location",ds.DSLocation);
 		if(ds.DSLocationType!=null && datastreamHeaders.contains("locationType"))
 			metadata.put("fedora:location-type",ds.DSLocationType);		
 	}
 
 	/**
      * Send datastream hints
      *
      * @param object The parent object of the datastream to be stored
      * @param datastream The prefix for the datastreamId to be stored
      * @return metadata containing hints/header information
      */
 	public Map<String, String> getHintsForAboutToBeStoredDatastream(
 			DigitalObject object, String datastream) {
 		Map<String,String> metadata = new HashMap<String, String>();
 		Date lastDate = new Date(0);
 		Datastream latestVersion=null;
 		for(Datastream ds:object.datastreams(datastream)){
 			if(ds.DSCreateDT.after(lastDate)){
 				lastDate = ds.DSCreateDT;
 				latestVersion = ds;
 			}
 		}
 		if(latestVersion!=null){
 			addDatastreamMetadataForDataStream(object, latestVersion, metadata);
 			return metadata;
 		}
 		return null;
 	}
 
 	/**
      * Convert the list of DCField into a String
      *
      * @param object The parent object of the datastream to be stored
      * @param datastream The prefix for the datastreamId to be stored
      * @return string representing the DCField
      */
 	private String getCommaSeparatedDCFieldValues(List<DCField> fieldList){
 		String commaSeparatedDCFieldValues = "";
 		for(DCField field:fieldList){
 			commaSeparatedDCFieldValues+=","+urlEncode(field.getValue()+(field.getLang()!=null&&field.getLang()!=""?"("+field.getLang()+")":""));
 		}
 		return commaSeparatedDCFieldValues!=""?commaSeparatedDCFieldValues.substring(1):"";
 	}
 
 	/**
 	 * Add Dublin Core headers according to the config file to the metadata map for the Dublin core datastream
 	 * Acceptable DC headers [title,creator,subject,description,publisher,contributor,date,type,format,identifier,source,language,relation,coverage,rights].
 	 * 
 	 * @param object: The Dublin Core Datastream for the object for which metadata is to be added
 	 * @param metadata: a map which contains all metadata to be added for the object
 	 */
 	private void addDublinCoreMetadataForDataStream(Datastream ds, Map<String,String> metadata){
 		try {
 			DCFields dCFields = new DCFields(ds.getContentStream());
 			if(dublinCoreHeaders.contains("title"))
 				metadata.put("fedora:dc-title",getCommaSeparatedDCFieldValues(dCFields.titles()));
 			if(dublinCoreHeaders.contains("creator"))
 				metadata.put("fedora:dc-creator",getCommaSeparatedDCFieldValues(dCFields.creators()));
 			if(dublinCoreHeaders.contains("subject"))
 				metadata.put("fedora:dc-subject",getCommaSeparatedDCFieldValues(dCFields.subjects()));
 			if(dublinCoreHeaders.contains("description"))
 				metadata.put("fedora:dc-description",getCommaSeparatedDCFieldValues(dCFields.descriptions()));
 			if(dublinCoreHeaders.contains("publisher"))
 				metadata.put("fedora:dc-publisher",getCommaSeparatedDCFieldValues(dCFields.publishers()));
 			if(dublinCoreHeaders.contains("contributor"))
 				metadata.put("fedora:dc-contributor",getCommaSeparatedDCFieldValues(dCFields.contributors()));
 			if(dublinCoreHeaders.contains("date"))
 				metadata.put("fedora:dc-date",getCommaSeparatedDCFieldValues(dCFields.dates()));
 			if(dublinCoreHeaders.contains("type"))
 				metadata.put("fedora:dc-type",getCommaSeparatedDCFieldValues(dCFields.types()));
 			if(dublinCoreHeaders.contains("format"))
 				metadata.put("fedora:dc-format",getCommaSeparatedDCFieldValues(dCFields.formats()));
 			if(dublinCoreHeaders.contains("identifier"))
 				metadata.put("fedora:dc-identifier",getCommaSeparatedDCFieldValues(dCFields.identifiers()));
 			if(dublinCoreHeaders.contains("source"))
 				metadata.put("fedora:dc-source",getCommaSeparatedDCFieldValues(dCFields.sources()));
 			if(dublinCoreHeaders.contains("language"))
 				metadata.put("fedora:dc-language",getCommaSeparatedDCFieldValues(dCFields.languages()));
 			if(dublinCoreHeaders.contains("relation"))
 				metadata.put("fedora:dc-relation",getCommaSeparatedDCFieldValues(dCFields.relations()));
 			if(dublinCoreHeaders.contains("coverage"))
 				metadata.put("fedora:dc-coverage",getCommaSeparatedDCFieldValues(dCFields.coverages()));
 			if(dublinCoreHeaders.contains("rights"))
 				metadata.put("fedora:dc-rights",getCommaSeparatedDCFieldValues(dCFields.rights()));
 		} catch (ObjectIntegrityException e) {
 			e.printStackTrace();
 		} catch (RepositoryConfigurationException e) {
 			e.printStackTrace();
 		} catch (StreamIOException e) {
 			e.printStackTrace();
 		}		
 	}
 	
 	/**
 	 * Add object headers according to the config file to the metadata map for the DigitalObject
	 * Acceptable values for header: [createDT,label,lastModDT,ownerID,state]. Any other header value in the config will be ignored
 	 * 
 	 * @param object: The DigitalObject for which metadata is to be added
 	 * @param metadata: a map which contains all metadata to be added for the object
 	 */
 	private void addObjectMetadataForDigitalObject(DigitalObject object, Map<String,String> metadata){
 	    SimpleDateFormat sd = new SimpleDateFormat("EEE, dd MMM yyyy HH:mm:ss 'GMT'");
 	    sd.setTimeZone(TimeZone.getTimeZone("GMT"));
 	    
 	    if(objectHeaders.isEmpty()){
 	    	return;
 	    }
 	    
 		if(object.getCreateDate()!=null && objectHeaders.contains("createDT"))
 			metadata.put("fedora:createDT",sd.format(object.getCreateDate()));
 		if(object.getLabel()!=null && objectHeaders.contains("label"))
 			metadata.put("fedora:label",object.getLabel());
 		if(object.getCreateDate()!=null && objectHeaders.contains("lastModDT"))
 			metadata.put("fedora:lastModDT",sd.format(object.getLastModDate()));
 		if(object.getOwnerId()!=null && objectHeaders.contains("ownerID"))
 			metadata.put("fedora:ownerID",object.getOwnerId());
 		if(object.getState()!=null && objectHeaders.contains("state"))
 			metadata.put("fedora:state",object.getState());
 	
 	} 
 	
 	/**
 	 * Add Dublin Core headers according to the config file to the metadata map for the DigitalObject
 	 * This function just calls addDublinCoreMetadataForDatastream() for the Dublin Core datastream for the given object
 	 * 
 	 * @param object: The DigitalObject for which metadata is to be added
 	 * @param metadata: a map which contains all metadata to be added for the object
 	 */
 	private void addDublinCoreMetadataForDigitalObject(DigitalObject object, Map<String, String>metadata){
 		Date lastDate = new Date(0);
 		Datastream latestVersion=null;
 		for(Datastream ds:object.datastreams("DC")){
 			if(ds.DSCreateDT.after(lastDate)){
 				lastDate = ds.DSCreateDT;
 				latestVersion = ds;
 			}
 		}
 		if(latestVersion!=null)
 			addDublinCoreMetadataForDataStream(latestVersion, metadata);
 	}
 	/**
      * Send object hints
      *
      * @param object The parent object of the datastream to be stored
      * @return metadata containing hints/header information
      */
 	
 	public Map<String, String> getHintsForAboutToBeStoredObject(
 			DigitalObject object) {
 		Map<String,String> metadata = new HashMap<String, String>();
 		metadata.put(":Content-Type","text/xml");
 		//metadata.put(":Content-Disposition",object.getPid());
 		metadata.put(":x-fedora-meta-stream-id","info:fedora/"+object.getPid());
 		if(!objectHeaders.isEmpty())
 			addObjectMetadataForDigitalObject(object, metadata);
 		if(!dublinCoreHeaders.isEmpty())
 			addDublinCoreMetadataForDigitalObject(object, metadata);
 		return metadata;
 	}
 
 	/**
      * Load configuration information from a file into a Properties object
      *
      * @param configFilePath Path to configuration file
      * @return Properties containing configuration information
      */
     protected Properties loadConfigFile(String configFilePath) {
         try {
             Properties properties = new Properties();
             FileInputStream propertyStream = null;
             try {
                 propertyStream = new FileInputStream(configFilePath);
                 properties.load(propertyStream);
             } finally {
                 if (propertyStream != null)
                     propertyStream.close();
             }
             return properties;
         } catch (FileNotFoundException e) {
             throw new RuntimeException("Akubra-Caringo config file " + configFilePath + " not found.");
         } catch (IOException e) {
             throw new RuntimeException("IOException initializing Akubra BlobStore");
         }
     }
 
     /**
      * URL encode a String
      *
      * @param value String to be encoded
      * @return Encoded string
      */
     protected String urlEncode(String value) {
         try {
             return URLEncoder.encode(value, "UTF-8");
         } catch (Exception e) {
             throw new RuntimeException();
         }
     }
 
     
 }
