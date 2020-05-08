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
 package org.sociotech.communitymashup.source.qrcode;
 
 import java.io.UnsupportedEncodingException;
 import java.net.URLEncoder;
 import java.util.Random;
 
 import org.eclipse.emf.common.notify.Notification;
 import org.eclipse.emf.common.util.EList;
 import org.osgi.service.log.LogService;
 import org.sociotech.communitymashup.application.Source;
 import org.sociotech.communitymashup.data.DataSet;
 import org.sociotech.communitymashup.data.Image;
 import org.sociotech.communitymashup.data.InformationObject;
 import org.sociotech.communitymashup.data.WebSite;
 import org.sociotech.communitymashup.data.observer.dataset.DataSetChangeObserver;
 import org.sociotech.communitymashup.data.observer.dataset.DataSetChangedInterface;
 import org.sociotech.communitymashup.source.impl.SourceServiceFacadeImpl;
 import org.sociotech.communitymashup.source.qrcode.meta.QRCodeTags;
 import org.sociotech.communitymashup.source.qrcode.properties.QRCodeProperties;
 
 /**
  * This is the main class of a source service that enriches a data set with
  * qr code markers using the google infographics api 
  * @see http://code.google.com/intl/de-DE/apis/chart/infographics/docs/overview.html
  * 
  * @author Peter Lachenmaier
  */
 public class QRCodeSourceService extends SourceServiceFacadeImpl implements DataSetChangedInterface {
 
 	/**
 	 * Observe to react on data set changes
 	 */
 	private DataSetChangeObserver dataSetChangeObserver;
 
 	/**
 	 * MetaTag needed for an information object to be enriched
 	 */
 	private String neededIOMetaTag;
 
 	/**
 	 * MetaTag needed for an website to be processed
 	 */
 	private String neededWebSiteMetaTag;
 
 	/**
 	 * The configured marker size
 	 */
 	private String markerSize;
 
 	/**
 	 * Indicates if load balancing is switched on.
 	 */
 	private boolean loadBalancing;
 
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.source.impl.SourceServiceFacadeImpl#initialize(org.sociotech.communitymashup.application.Source)
 	 */
 	@Override
 	public boolean initialize(Source configuration) {
 
 		boolean initialized = super.initialize(configuration);
 
 		if (initialized) {
 			// read configuration
 			neededIOMetaTag = source.getPropertyValue(
 					QRCodeProperties.PROCESS_IO_ONLY_WITH_METATAG_PROPERTY);
 
 			neededWebSiteMetaTag = source.getPropertyValue(
 					QRCodeProperties.PROCESS_WEBSITE_ONLY_WITH_METATAG_PROPERTY);
 
 			markerSize = getMarkerSize();
 			
 			loadBalancing = source.isPropertyTrueElseDefault(
 					QRCodeProperties.USE_LOAD_BALANCING_PROPERTY,
 					QRCodeProperties.USE_LOAD_BALANCING_DEFAULT);
 					
 		}
 
 		this.setInitialized(initialized);
 		return initialized;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.source.impl.SourceServiceFacadeImpl#enrichDataSet()
 	 */
 	@Override
 	public void enrichDataSet() {
 		
 		// let the default implentation work
 		super.enrichDataSet();
 		
 		DataSet dataSet = source.getDataSet();
 		
 		// be nice and check null values
 		if(dataSet == null)
 		{
 			// nothing can be done
 			return;
 		}
 		
 		// add qr markers to all information object having a website
 		EList<WebSite> webSites = dataSet.getWebSites();
 		
 		if(webSites != null)
 		{
 			for(WebSite webSite : webSites)
 			{
 				enrichInformationObjectsOfWebSite(webSite);
 			}
 		}
 		
 		// add adapter to get informed about new or changed items
 		dataSetChangeObserver = new DataSetChangeObserver(dataSet, this);
 	}
 
 	/**
 	 * Attaches an qr marker to all information objects the given web site belongs
 	 * to ({@link WebSite#getInformationObjects()})
 	 * 
 	 * @param webSite
 	 */
 	public void enrichInformationObjectsOfWebSite(WebSite webSite) {
 		
 		// check input parameters
 		if(webSite == null)
 		{
 			return;
 		}
 		
		if(neededWebSiteMetaTag != null && !webSite.hasMetaTag(neededWebSiteMetaTag)) {
			// skip
			return;
		}
		
 		EList<InformationObject> informationObjects = webSite.getInformationObjects();
 		
 		if(informationObjects == null || informationObjects.isEmpty())
 		{
 			// not attached to an information object
 			return;
 		}
 		
 		String markerUrl = createMarkerUrl(webSite.getAdress()); 
 		
 		if(markerUrl == null)
 		{
 			log("Could not create qr marker url for " + webSite.getAdress(), LogService.LOG_WARNING);
 			return;
 		}
 		
 		// attach the image to all information objects
 		for(InformationObject informationObject : informationObjects)
 		{
 			enrichInfomationObject(informationObject, markerUrl);
 		}
 		
 	}
 
 	/**
 	 * Enriches the given information object with an qr marker linking to the given web site.
 	 * 
 	 * @param informationObject Information object to enrich
 	 * @param webSite Web site the qr marker should link to
 	 */
 	public void enrichInformationObject(InformationObject informationObject, WebSite webSite)
 	{
 		if(informationObject == null || webSite == null)
 		{
 			return;
 		}
 		
		if(neededWebSiteMetaTag != null && !webSite.hasMetaTag(neededWebSiteMetaTag)) {
			// skip
			return;
		}
		
 		// check if this two belong together
 		if(!webSite.getInformationObjects().contains(informationObject))
 		{
 			// not allowed to enrich information object with value of not connected website
 			return;
 		}
 		
 		// TODO check if this marker already exists as attachment of this information object
 		
 		String markerUrl = createMarkerUrl(webSite.getAdress()); 
 				
 		if(markerUrl == null)
 		{
 			log("Could not create qr marker url for " + webSite.getAdress(), LogService.LOG_WARNING);
 			return;
 		}
 		
 		// attach the image to the information objects
 		Image markerImage = enrichInfomationObject(informationObject, markerUrl);
 		
 		if(markerImage != null) {
 			// delete it when website gets deleted
 			markerImage.deleteOnDeleteOf(webSite);
 		}
 	}
 	
 	/**
 	 * Attaches an image with the given marker url to the given information object.
 	 * 
 	 * @param informationObject Information object to attach marker image to
 	 * @param markerUrl Url of the marker image
 	 */
 	private Image enrichInfomationObject(InformationObject informationObject, String markerUrl) {
 		
 		if(neededIOMetaTag != null && !informationObject.hasMetaTag(neededIOMetaTag)) {
 			return null;
 		}
 		
 		String ioImageIdent = informationObject.getIdent() + "_" + markerUrl.hashCode();
 		
 		Image markerImage = informationObject.attachImage(markerUrl);
 		
 		// add it explicitly to this source
 		markerImage = this.add(markerImage, ioImageIdent);
 		
 		if(markerImage == null) {
 			log("Could not add marker image with ident " + ioImageIdent, LogService.LOG_WARNING);
 			return null;
 		}
 		
 		// delete it when the io gets deleted
 		markerImage.deleteOnDeleteOf(informationObject);
 				
 		// tag it
 		markerImage.metaTag(QRCodeTags.QRCODE);
 		// tag with size
 		markerImage.metaTag(markerSize);
 		
 		log("Added qr marker " + markerUrl + " to " + informationObject.getName(), LogService.LOG_INFO);
 		
 		return markerImage;
 	}
 
 	/**
 	 * Creates an url of a marker image using the google chart api. The size will be determined
 	 * in the source configuration.
 	 * 
 	 * @param url Url the qr marker should link to
 	 * @return The url of a qr marker image, null in error case.
 	 */
 	private String createMarkerUrl(String url) {
 		
 		if(url == null)
 		{
 			return null;
 		}
 		
 		String balancePrefix = "";
 		if(loadBalancing)
 		{
 			// create a prefix between 0 and 9
 			balancePrefix += new Random().nextInt(10) + "."; 
 		}
 		
 		String encodedUrl = url;
 		
 		// try to encode the url
 		try {
 			encodedUrl = URLEncoder.encode(url, "UTF-8");
 		} catch (UnsupportedEncodingException e) {
 			log("Could not UTF-8 ecode url " + url + " due to " + e.getMessage(), LogService.LOG_WARNING);
 		}
 		
 		return "http://" + balancePrefix + "chart.googleapis.com/chart?chs=" + markerSize + "&cht=qr&chl=" + encodedUrl;
 	}
 
 	/**
 	 * Determine the needed marker size out of the configuration entry or the default value.
 	 * 
 	 * @return The marker size as String in Form {X}x{Y}
 	 */
 	private String getMarkerSize() {
 		String size = source.getPropertyValue(QRCodeProperties.MARKER_SIZE_PROPERTY);
 		
 		if(size == null || size.isEmpty())
 		{
 			size = QRCodeProperties.MARKER_SIZE_DEFAULT;
 		}
 		
 		if(!size.contains("x"))
 		{
 			// make quadratic size
 			size = size + "x" + size;
 		}
 		return size;
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.data.observer.dataset.DataSetChangedInterface#dataSetChanged(org.eclipse.emf.common.notify.Notification)
 	 */
 	@Override
 	public void dataSetChanged(Notification notification) {
 		if(notification == null)
 		{
 			return;
 		}
 		
 		// new website added to the data set
 		if(notification.getEventType() == Notification.ADD && notification.getNotifier() instanceof DataSet && notification.getNewValue() instanceof WebSite)
 		{
 			WebSite newWebSite = (WebSite) notification.getNewValue();
 			// enrich information objects of website
 			enrichInformationObjectsOfWebSite(newWebSite);
 		}
 		// information object got new web site
 		else if(notification.getEventType() == Notification.ADD && notification.getNotifier() instanceof InformationObject && notification.getNewValue() instanceof WebSite)
 		{
 			// information object with new website
 			InformationObject changedIO = (InformationObject) notification.getNotifier();
 			
 			// get added website
 			WebSite addedWebSite = (WebSite) notification.getNewValue();
 				
 			// enrich with website
 			enrichInformationObject(changedIO, addedWebSite);
 		}
 	}
 	
 	/* (non-Javadoc)
 	 * @see org.sociotech.communitymashup.source.impl.SourceServiceFacadeImpl#stop()
 	 */
 	@Override
 	protected void stop() {
 		// disconnect data set observer
 		if (dataSetChangeObserver != null) {
 			dataSetChangeObserver.disconnect();
 		}
 
 		super.stop();
 	}
 }
