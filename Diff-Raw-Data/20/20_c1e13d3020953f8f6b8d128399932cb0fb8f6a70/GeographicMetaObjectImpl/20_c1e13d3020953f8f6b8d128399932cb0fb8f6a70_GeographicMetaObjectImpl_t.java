 package org.fao.fi.gis.metadata.association;
 
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.fao.fi.gis.metadata.entity.EntityAddin;
 import org.fao.fi.gis.metadata.entity.GeographicEntity;
 import org.fao.fi.gis.metadata.feature.FeatureTypeProperty;
 import org.fao.fi.gis.metadata.model.MetadataConfig;
 import org.fao.fi.gis.metadata.model.content.MetadataContent;
 import org.fao.fi.gis.metadata.model.settings.GeographicServerSettings;
 import org.fao.fi.gis.metadata.model.settings.MetadataCatalogueSettings;
 import org.fao.fi.gis.metadata.model.settings.PublicationSettings;
 import org.fao.fi.gis.metadata.util.Utils;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 
 import com.vividsolutions.jts.geom.Envelope;
 
 /**
  * GeographicAssociationImpl
  * 
  * 
  * @author eblondel
  *
  */
 public class GeographicMetaObjectImpl implements GeographicMetaObject {
 
 	private GeographicServerSettings gsSettings;
 	private MetadataCatalogueSettings mdSettings;
 	private PublicationSettings pubSettings;
 
 	private String collection;
 	private MetadataContent template;
 	private List<GeographicEntity> entities;
 	private String code;
 	private String refName;
 	private String metaId;
 	private String metaTitle;
 	private String targetLayername;
 	
 	private Map<EntityAddin,String> addins;
 	private Map<GeographicMetaObjectProperty,List<String>> specificProperties;
 	
 	private Map<FeatureTypeProperty, Object> geoproperties;
 	private URI graphicOverview;
 	
 
 	private boolean figis;
 	private URI viewerResource;
 	private String factsheet;
 	
  
 	/**
 	 * Constructs a geographic Association
 	 * 
 	 * @param entities
 	 * @param template
 	 * @param geoproperties
 	 * @param addins
 	 * @param gsSettings
 	 * @param metaSettings
 	 * @param domain
 	 * @param vieweridentifier
 	 * @throws URISyntaxException
 	 */
 	public GeographicMetaObjectImpl(List<GeographicEntity> entities,
 			Map<FeatureTypeProperty, Object> geoproperties, Map<GeographicMetaObjectProperty, List<String>> objectProperties,
 			Map<EntityAddin, String> addins,
 			MetadataConfig config) throws URISyntaxException {
 
 		//settings
 		this.gsSettings = config.getSettings().getGeographicServerSettings();
 		this.mdSettings = config.getSettings().getMetadataCatalogueSettings();
 		this.pubSettings = config.getSettings().getPublicationSettings();
 			
 		//identification
 		this.template = config.getContent();
 		this.collection = this.pubSettings.getCollectionType();
 		
 		this.setEntities(entities);
 		this.setCode(entities);
 		this.setRefname(entities);
 		this.setMetaIdentifier();
 		this.setMetaTitle();
 		
 		this.addins = addins;
 		this.setSpecificProperties(entities, objectProperties);
 		this.setTargetLayername(this.gsSettings.getTargetLayerPrefix());
 		
 		//gis
 		this.geoproperties = geoproperties;
 		this.setLayerGraphicOverview();
 		
 		//Figis
 		this.figis = config.getSettings().getPublicationSettings().isFigis();
 		this.setFigisViewerResource();
 		this.setFigisFactsheet();
 
 	}
 	
 	/** ==============================================
 	 *  		    IDENTIFICATION METHODS
 	 *  ==============================================
 	 */ 
 	
 	/**
 	 * @return the entities
 	 */
 	public List<GeographicEntity> getEntities() {
 		return entities;
 	}
 
 	/**
 	 * @param entities the entities to set
 	 */
 	public void setEntities(List<GeographicEntity> entities) {
 		this.entities = entities;
 	}
 
 	/**
 	 * Get Code
 	 * 
 	 */
 	public String getCode(){
 		return this.code;
 	}
 	
 	/**
 	 * Set Code
 	 * @param entities
 	 */
 	public void setCode(List<GeographicEntity> entities){
 		
 		String codes ="";
 		for(int i = 0; i <entities.size();i++){
 			if(i==0){
 				codes = entities.get(i).getCode();
 			}else{
 				codes += "_x_"+entities.get(i).getCode();
 			}
 		}
 		this.code = codes;
 		
 	}
 	
 	/**
 	 * Alternative method
 	 * 
 	 * @param codes
 	 */
 	public void setCode(String[] codes){
 		String refCodes = "";
 		for(int i = 0; i <codes.length;i++){
 			if(i==0){
 				refCodes = codes[i];
 			}else{
 				refCodes += "_x_"+codes[i];
 			}
 		}
 		this.code = refCodes;
 	}
 
 	/**
 	 * Get Ref Name
 	 * 
 	 */
 	public String getRefName() {
 		return this.refName;
 	}
 	
 	/**
 	 * Set Ref Name
 	 * 
 	 * @param entities
 	 */
 	public void setRefname(List<GeographicEntity> entities){
 		
 		String refName = "";
 		if(entities.size() == 1){
 			refName += entities.get(0).getRefName();
 		}else if(entities.size() > 1){
 		
 			for(int i=0;i<entities.size();i++){
 				if(i==0){
 					refName += entities.get(i).getRefName();
 				}else{
 					refName += " | "+entities.get(i).getRefName();
 				}
 			}
 		}
 		this.refName = refName;
 		
 	}
 	
 	/**
 	 * Alternative method to set names
 	 * 
 	 * @param names
 	 */
 	public void setRefName(String[] names){
 		String refNames = "";
 		for(int i = 0; i <names.length;i++){
 			if(i==0){
 				refNames = names[i];
 			}else{
 				refNames += " | "+names[i];
 			}
 		}
 		this.refName = refNames;
 		
 	}
 	
 	
 	/**
 	 * Get Target layer name
 	 * 
 	 */
 	public String getTargetLayerName() {
 		return this.targetLayername;
 	}
 	
 	/**
 	 * Set Target layer name
 	 * 
 	 * @param trgLayerPrefix
 	 */
 	public void setTargetLayername(String trgLayerPrefix) {
 		if (trgLayerPrefix == null | trgLayerPrefix == "") {
 			this.targetLayername = this.getCode();
 		} else {
 			this.targetLayername = trgLayerPrefix + "_" + this.getCode();
 		}
 	}
 	
 	
 	/**
 	 * Get Collection prefix
 	 * 
 	 */
 	public String getCollection() {
 		return this.collection;
 	}
 	
 	/**
 	 * Get Meta Identifier
 	 * 
 	 */
 	public String getMetaIdentifier(){
 		return this.metaId;
 	}
 	
 	/**
 	 * Set Meta Identifier
 	 * 
 	 * @param entities
 	 */
 	public void setMetaIdentifier(){
 		if(entities.size() > 1){
 			this.metaId = Utils.buildMetadataIdentifier(entities, this.getTemplate().getOrganizationContact().getAcronym(), this.collection);
 		}else{
 			this.metaId = Utils.buildMetadataIdentifier(entities.get(0));
 		}
 		
 	}
 
 	
 	/**
 	 * Get Ref Name
 	 * 
 	 */
 	public String getMetaTitle() {
 		return this.metaTitle;
 	}
 	
 	/**
 	 * Set Ref Name
 	 * 
 	 * @param entities
 	 */
 	public void setMetaTitle(){
 		
 		this.metaTitle = "";
 		if(this.getTemplate().getHasBaseTitle()){
 			this.metaTitle += this.getTemplate().getBaseTitle();
 		}
 		this.metaTitle += this.refName;
 		
 	}
 	
 	
 	/**
 	 * Get Object Addins
 	 * 
 	 * @return
 	 */
 	public Map<EntityAddin,String> getAddins(){
 		return this.addins;
 	}
 	
 	public void setSpecificProperties(List<GeographicEntity> entities, Map<GeographicMetaObjectProperty, List<String>> objectProperties){
 		this.specificProperties = new HashMap<GeographicMetaObjectProperty, List<String>>();
 		if(objectProperties != null){
 			this.specificProperties.putAll(objectProperties);
 		}
 		
 		for(GeographicEntity entity : entities){
 			this.specificProperties.putAll(entity.getSpecificProperties());
 		}
 		
 	}
 	
 	/** ==============================================
 	 *  		    SETTINGS METHODS
 	 *  ==============================================
 	 */
 	
 	
 	public GeographicServerSettings getGeographicServerSettings() {
 		return this.gsSettings;
 	}
 
 	public MetadataCatalogueSettings getMetadataCatalogueSettings() {
 		return this.mdSettings;
 	}
 
 	public PublicationSettings getPublicationSettings() {
 		return this.pubSettings;
 	}
 
 	public MetadataContent getTemplate(){
 		return this.template;
 	}
 
 	public Map<GeographicMetaObjectProperty, List<String>> getSpecificProperties() {
 		return this.specificProperties;
 	}
 	
 	/** ==============================================
 	 *  		    GIS PROPERTIES METHODS
 	 *  ==============================================
 	 */
 	
 	/**
 	 * Get Geo properties
 	 * 
 	 * @return
 	 */
 	public Map<FeatureTypeProperty, Object> getGeoProperties() {
 		return this.geoproperties;
 	}
 
 	
 	public Envelope getBBOX() {
 		Envelope bbox = null;
		if(this.geoproperties != null){
			if (this.geoproperties.containsKey(FeatureTypeProperty.BBOX)) {
				bbox = (Envelope) this.geoproperties.get(FeatureTypeProperty.BBOX);
			}
 		}
 		return bbox;
 	}
 
 	public int getFeaturesCount() {
 		Integer count = 0;
		if(this.geoproperties != null){
			if (this.geoproperties.containsKey(FeatureTypeProperty.COUNT)) {
				count = (Integer) this.geoproperties.get(FeatureTypeProperty.COUNT);
			}
 		}
 		return count;
 	}
 	
 
 	public CoordinateReferenceSystem getCRS() {
 		CoordinateReferenceSystem crs = null;
		if(this.geoproperties != null){
			if (!this.geoproperties.containsKey(FeatureTypeProperty.CRS)) {
				crs = (CoordinateReferenceSystem) this.geoproperties.get(FeatureTypeProperty.CRS);
			}
 		}
 		return crs;
 	}
 	
 
 	/**
 	 * Get Layer Graphic Overview
 	 * 
 	 */
 	public URI getLayerGraphicOverview() {
 		return this.graphicOverview;
 	}
 	
 	/**
 	 * Method that will handle the layer graphic overview to be appended to the
 	 * layer metadata as layer preview
 	 * 
 	 * @return URI
 	 * @throws URISyntaxException
 	 */
 	public void setLayerGraphicOverview() throws URISyntaxException {
 
 		// compute the image size
 		double minX = -180.0;
 		double maxX = 180.0;
 		double minY = -90.0;
 		double maxY = 90.0;
 		int width = 600;
 		int height = 300;
 
 		Envelope bbox = this.getBBOX();
 		if (bbox != null) {
 			minX = bbox.getMinX();
 			maxX = bbox.getMaxX();
 			minY = bbox.getMinY();
 			maxY = bbox.getMaxY();
 
 			// adjust width &height
 			double rangeX = maxX - minX;
 			double rangeY = maxY - minY;
 			double newheight = width * rangeY / rangeX;
 			height = (int) Math.round(newheight);
 
 			// check height size and adjust width size if necessary
 			if (height > width) {
 				width = (int) Math.round(Math.pow(width, 2) / height);
 			}
 
 		}
 
 		// build the layer preview URI
 		String completeBaseLayerName = this.gsSettings.getBaseLayerWorkspace() +":"+this.gsSettings.getBaseLayerName();
 		String completeLayerName = this.gsSettings.getTargetWorkspace() + ":"
 				+ this.targetLayername;
 		String graphicLink = this.gsSettings.getUrl()
 				+ "/wms?service=WMS&version=1.1.0&request=GetMap" + "&layers="
 				+ completeBaseLayerName+"," + completeLayerName + "&bbox=" + minX
 				+ "," + minY + "," + maxX + "," + maxY + "&width=" + width
 				+ "&height=" + height + "&srs=EPSG:4326" + // for now only
 															// EPSG:4326 as
 															// projection
 				"&format=image%2Fpng";
 
 		this.graphicOverview = new URI(graphicLink);
 	}
 
 	
 	/** ==============================================
 	 *  		    FIGIS SPECIFIC METHODS
 	 *  ==============================================
 	 */
 	
 	/**
 	 * Returns true if the collection is from FIGIS
 	 * 
 	 */
 	public boolean isFromFigis(){
 		return this.figis;
 	}
 	
 	
 	/**
 	 * Get FIGIS Viewer Resource (specific to FAO)
 	 * 
 	 * @return the URI for the FIGIS map viewer
 	 * @throws URISyntaxException
 	 */
 	private void setFigisViewerResource() throws URISyntaxException {
 		
 		this.viewerResource = null;		
 		
 		//only apply to figis collections
 		if(figis){
 
 			Envelope bbox = this.getBBOX();
 			String figisDomain = this.entities.get(0).getFigisDomain();
 			
 			String resource = null;
 			if (bbox != null) {
 				resource = "http://www.fao.org/figis/geoserver/factsheets/" + figisDomain
 						+ ".html?" + this.collection + "=" + this.entities.get(0).getFigisViewerId()
 						+ "&extent=" + bbox.getMinX() + "," + bbox.getMinY() + ","
 						+ bbox.getMaxX() + "," + bbox.getMaxY() + "&prj=4326"; 
 			}
 			
 			if (figisDomain != null && this.collection != null && bbox != null) {
 				this.viewerResource = new URI(resource);
 			}
 		}
 	}
 
 	public URI getFigisViewerResource() {
 		return this.viewerResource;
 	}
 
 	/**
 	 * Set Figis Factsheet URL
 	 * 
 	 */
 	public void setFigisFactsheet() {	
 		
 		this.factsheet = null;
 		
 		//only apply to figis collections
 		if(figis){
 			String figisViewerDomain = this.collection;
 			this.factsheet = "http://www.fao.org/fishery/"+figisViewerDomain + "/" + this.entities.get(0).getFigisId();
 		}
 	}
 	
 	/**
 	 * Get Figis Factsheet URL
 	 * 
 	 */
 	public String getFigisFactsheet(){
 		return this.factsheet;
 	}
 	
 }
