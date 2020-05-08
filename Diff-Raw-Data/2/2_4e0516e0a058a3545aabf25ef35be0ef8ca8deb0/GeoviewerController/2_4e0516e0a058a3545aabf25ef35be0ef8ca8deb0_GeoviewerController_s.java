 package fr.ifremer.seagis.geoviewer.controller;
 
 import com.liferay.util.bridges.jsf.common.JSFPortletUtil;
 
 import fr.ifremer.seagis.geoviewer.service.DefaultGeoviewerService;
 import fr.ifremer.seagis.geoviewer.service.GeoviewerService;
 import fr.ifremer.seagis.model.SextantConfig;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.Serializable;
 import java.io.UnsupportedEncodingException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 
 import javax.faces.event.ActionEvent;
 import javax.faces.context.FacesContext;
 import javax.faces.model.SelectItem;
 import javax.portlet.ActionRequest;
 import javax.xml.bind.JAXBException;
 import javax.xml.stream.XMLStreamException;
 
 import org.apache.commons.fileupload.FileItem;
 
 import org.geotoolkit.map.MapBuilder;
 import org.geotoolkit.map.MapContext;
 import org.geotoolkit.referencing.CRS;
 
 import org.mapfaces.component.context.UIContext;
 import org.mapfaces.model.Context;
 import org.mapfaces.model.DefaultDownloadedFile;
 import org.mapfaces.model.DownloadedFile;
 import org.mapfaces.model.FeaturesStore;
 import org.mapfaces.model.wpstool.ExecuteResponse;
 import org.mapfaces.utils.XMLContextUtilities;
 import org.mapfaces.event.wps.ReceiveResponseEvent;
 import org.mapfaces.utils.FacesUtils;
 
 import org.mapfaces.utils.WPSUtils;
 import org.opengis.feature.type.PropertyDescriptor;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.util.FactoryException;
 
 /**
  * Controller for the Geoviewer portlet.
  * 
  * @author leopratlong
  * @author Fabien BERNARD (Geomatys)
  */
 public class GeoviewerController implements Serializable {
 
     private final static Logger LOGGER = Logger.getLogger(GeoviewerController.class.getName());
     
     // Geoviewer service
     private final static GeoviewerService GEOVIEWERSERVICE = new DefaultGeoviewerService();
     
     // User community
     private String currentCommunity;
     
     // Geoviewer configuration
    private Object ctxService = "/data/netmar.xml";
     private Boolean WPSTool;
     private Boolean graticuleTool;
     private Boolean distanceTool;
     private Boolean surfaceTool;
     private String maxExtent;
     private String maxExtentLocalisation;
     private String mdViewerUrl;
     private List<SelectItem> zoomList = new ArrayList<SelectItem>();
     private List<SelectItem> WMSList = null;
     private List<SelectItem> WPSList = null;
     
     // GetFeatureInfo variables
     private String getFeatureInfoOutputFormat = "application/vnd.ogc.gml";
     private List<String> getFeatureInfoResults = new ArrayList<String>();
     private List<FeaturesStore> featuresStores = new ArrayList<FeaturesStore>();
     
     // MapContext used to add dynamically layers to the mappane
     private MapContext mapContext;
     
     // Render or not the WPS popup
     private boolean renderWPS = false;
     
     // SessionMap key to get the MapContext file to download
     private final static String CONTEXT_KEY = "mainCtx";
     
     
     /**
      * This method build the initial configuration for the current community.
      */
     private void makeConfig() {
         final FacesContext fc = FacesContext.getCurrentInstance();
         final String newCommunity = GEOVIEWERSERVICE.getCurrentCommunity(fc);
         
         // If community found and has changed or is null, tries to get the configuration 
         // file from session (if not found tries to get it from WEB-INF folder)
         if ((newCommunity != null && !newCommunity.equals(currentCommunity)) || newCommunity == null) {
             makeSextantConfig(GEOVIEWERSERVICE.getConfiguration(fc));
             
             currentCommunity = newCommunity;
             
             // Empty MapContext
             mapContext = MapBuilder.createContext();
         }
         
         // Refresh some properties on page load
         renderWPS = false;
         fc.getViewRoot().setLocale(GEOVIEWERSERVICE.getLocale(fc));
     }
     
     /**
      * Initialize Geoviewer configuration variables.
      * 
      * @param fc the current FacesContext instance
      * @param cfg the SextantConfig model to use
      */
     private void makeSextantConfig(final SextantConfig sxtCfg) {
         
         // Initialize Geoviewer config from SextantConfig model
         ctxService = sxtCfg.getGeoviewerOWCUrl() != null ? sxtCfg.getGeoviewerOWCUrl() : ctxService;
         graticuleTool = "yes".equals(sxtCfg.getGeoviewerToolGraticule());
         distanceTool = "yes".equals(sxtCfg.getGeoviewerToolDistance());
         surfaceTool = "yes".equals(sxtCfg.getGeoviewerToolSurface());
         GEOVIEWERSERVICE.addZoomsToList(sxtCfg.getGeoviewerZoomList(), zoomList);
         WMSList = GEOVIEWERSERVICE.addUrlsToList(sxtCfg.getGeoviewerWMSList());
         WPSTool = "yes".equals(sxtCfg.getGeoviewerWPSActive());
         WPSList = GEOVIEWERSERVICE.addUrlsToList(sxtCfg.getGeoviewerWPSList());
         maxExtent = GEOVIEWERSERVICE.getMaxExtentFromString(
                 sxtCfg.getGeoviewerWest(), sxtCfg.getGeoviewerSouth(),
                 sxtCfg.getGeoviewerEast(), sxtCfg.getGeoviewerNorth());
         maxExtentLocalisation = GEOVIEWERSERVICE.getMaxExtentFromString(
                 sxtCfg.getGeoviewerLocalWest(), sxtCfg.getGeoviewerLocalSouth(),
                 sxtCfg.getGeoviewerLocalEast(), sxtCfg.getGeoviewerLocalNorth());
         mdViewerUrl = sxtCfg.getMdViewerUrl();
     }
 
     /**
      * Action called when user want to download the OWC file.
      *
      * @return DownloadedFile : MapFaces model for the downloaded File (name, type, resource...).
      */
     public DownloadedFile getOWCFile() {
         final FacesContext fc = FacesContext.getCurrentInstance();
         final DownloadedFile downloadedFile = new DefaultDownloadedFile();
 
         try {
             final File f = GEOVIEWERSERVICE.getOWSContextFile(fc, CONTEXT_KEY);
             if (f != null) {
                 FileInputStream fis;
                 try {
                     fis = new FileInputStream(f);
                     downloadedFile.setResource(fis);
                     downloadedFile.setFileName("ows_context.xml");
                     downloadedFile.setMimeType("text/xml");
                 } catch (FileNotFoundException e) {
                     LOGGER.log(Level.WARNING, "Error while trying to write the OWS file.");
                 }
                 f.delete();
             }
         } catch (UnsupportedEncodingException e1) {
             LOGGER.log(Level.WARNING, "Error while trying to write the OWS file.");
         } catch (JAXBException e1) {
             LOGGER.log(Level.WARNING, "Error while trying to write the OWS file.");
         } catch (IOException e1) {
             LOGGER.log(Level.WARNING, "Error while trying to write the OWS file.");
         }
         return downloadedFile;
     }
 
     /**
      * Action called when user validate his OWC file upload.
      */
     public void uploadOWCFile() {
         final FacesContext fc = FacesContext.getCurrentInstance();
         final ActionRequest request = (ActionRequest) JSFPortletUtil.getPortletRequest(fc);
         final FileItem item = (FileItem) request.getAttribute("resumeFile");
 
         try {
             final Context model = XMLContextUtilities.readContext(item.getInputStream());
             if (model != null) {
                 model.setReloading(true);
                 setCtxService(model);
                 
                 // Empty MapContext
                 mapContext = MapBuilder.createContext();
             }
         } catch (UnsupportedEncodingException e) {
             LOGGER.log(Level.WARNING, "Error while trying to read the OWS file.");
         } catch (MalformedURLException e) {
             LOGGER.log(Level.WARNING, "Error while trying to read the OWS file.");
         } catch (JAXBException e) {
             LOGGER.log(Level.WARNING, "Error while trying to read the OWS file.");
         } catch (IOException e) {
             LOGGER.log(Level.WARNING, "Error while trying to read the OWS file.");
         }
     }
 
     /**
      * This is an action event for the getFeatureInfo
      * @throws JAXBException
      * @throws IOException
      * @throws XMLStreamException
      */
     public void afterGetFeatureInfoRequests() throws JAXBException, IOException, XMLStreamException {
         System.out.println("Action afterGetFeatureInfoRequests");
 
         for (final FeaturesStore featuresStore : featuresStores) {
             System.out.println("FeatureType : " + featuresStore.getFeatureType().getName().getLocalPart());
 
             for (final org.opengis.feature.Feature feature : featuresStore.getFeatures()) {
                 System.out.println("\t\t Feature : " + feature.getIdentifier().getID());
 
                 for (final PropertyDescriptor prop : featuresStore.getFeatureType().getDescriptors()) {
                     System.out.println("\t\t\t\t Property : " + prop.getName().getLocalPart() + " = " + feature.getProperty(prop.getName()));
                 }
             }
         }
     }
 
     /**
      * Proceed the WPS execute response to display results in the mappane
      * @param event 
      */
     public void proceedWPSExecuteResponse(final ActionEvent event) {
 
         if (event instanceof ReceiveResponseEvent) {
             final Object responseObject = ((ReceiveResponseEvent) event).getResponseObject();
 
             if (responseObject instanceof ExecuteResponse) {
                 final ExecuteResponse execResp = (ExecuteResponse) responseObject;
                 
                 // Init the dynamic mapcontext CRS
                 final CoordinateReferenceSystem crs = getMapfacesCRS();
                 if (crs != null) {
                     mapContext.setCoordinateReferenceSystem(crs);
                 }
         
                 // Try to display results
                 mapContext.layers().addAll(WPSUtils.createWPSResultsMapLayers(execResp));
             }
         }
     }
 
     /**
      * Add a layer to the user basket. Technically, we use IPC to send 
      * information to the "Panier" portlet.
      */
     public void addToBasket() {
         GEOVIEWERSERVICE.findLayerAndAddToBasket(FacesContext.getCurrentInstance(), CONTEXT_KEY);
     }
     
     /**
      * Changes the renderWPS attribute.
      */
     public void renderOrNotWPS() {
         renderWPS = !renderWPS;
     }
     
     /**
      * Return the current CRS use by the mappane
      * 
      * @return the CoordinateReferenceSystem istance
      */
     private CoordinateReferenceSystem getMapfacesCRS() {
         final FacesContext fc = FacesContext.getCurrentInstance();
         
         if (fc != null) {
             final UIContext uiContext = (UIContext) FacesUtils.findComponentById(fc.getViewRoot(), CONTEXT_KEY);
             
             if (uiContext != null) {
                 
                 if (uiContext.getModel() != null) {
                     final Context model = uiContext.getModel();
                     final String srs = model.getSrs();
                     
                     if (srs != null) {
                         try {
                             return CRS.decode(srs);
                         } catch (FactoryException ex) {
                             LOGGER.log(Level.WARNING, "Invalid SRS definition : " + srs, ex);
                         }
                     }
                 }
             }
         }
         
         return null;
     }
 
     /**
      * Initializes the Geoviewer portlet state : it has been called by the geoviewer.xhtml through
      * an outputText. It will not render a text since we return a null value, but will initialize
      * all needed values for the Geoviewer.
      * 
      * @return null
      */
     public String getInit() {
         try {
             makeConfig();
         } catch (Exception ex) {
             LOGGER.log(Level.SEVERE, ex.getMessage());
         } catch (Error er) {
             LOGGER.log(Level.SEVERE, er.getMessage());
         }
         return null;
     }
 
     /**
      * Called by geoviewer_ipc portlet.
      * 
      * @return
      */
     public String getShareSession() {
         final FacesContext context = FacesContext.getCurrentInstance();
         GEOVIEWERSERVICE.shareSession(context);
         GEOVIEWERSERVICE.removeLayersFromCatalogue(context);
         return null;
     }
 
     /**
      * @return the MapContext Geotoolkit object used to add dynamically layers 
      * to the mappane
      */
     public MapContext getMapContext() {
         return mapContext;
     }
 
     /**
      * @param mapContext the MapContext to set
      */
     public void setMapContext(MapContext mapContext) {
         this.mapContext = mapContext;
     }
 
     /**
      * @return the URL used to format metadata
      */
     public String getMdViewerUrl() {
         return mdViewerUrl;
     }
 
     /**
      * @return the featuresStores
      */
     public List<FeaturesStore> getFeaturesStores() {
         return featuresStores;
     }
 
     /**
      * @param ctxService the ctxService to set
      */
     public void setCtxService(Object ctxService) {
         this.ctxService = ctxService;
     }
 
     /**
      * @return the ctxService
      */
     public Object getCtxService() {
         return ctxService;
     }
 
     /**
      * @return the GetFeatureInfoOutputFormat
      */
     public String getGetFeatureInfoOutputFormat() {
         return getFeatureInfoOutputFormat;
     }
 
     /**
      * @return the GetFeatureInfoResults
      */
     public List<String> getGetFeatureInfoResults() {
         return getFeatureInfoResults;
     }
 
     /**
      * @param getFeatureInfoResults the GetFeatureInfoResults to set
      */
     public void setGetFeatureInfoResults(List<String> getFeatureInfoResults) {
         this.getFeatureInfoResults = getFeatureInfoResults;
     }
 
     /**
      * @param featuresStores the featuresStores to set
      */
     public void setFeaturesStores(List<FeaturesStore> featuresStores) {
         this.featuresStores = featuresStores;
     }
 
     /**
      * @return the zoomList
      */
     public List<SelectItem> getZoomList() {
         return zoomList;
     }
 
     /**
      * @return the maxExtent
      */
     public String getMaxExtent() {
         return maxExtent;
     }
 
     /**
      * @return the maxExtentLocalisation
      */
     public String getMaxExtentLocalisation() {
         if (maxExtentLocalisation == null) {
             maxExtentLocalisation = "-180,-90,180,90";
         }
         return maxExtentLocalisation;
     }
 
     /**
      * @return the current community
      */
     public String getCurrentCommunity() {
         return GEOVIEWERSERVICE.getCurrentCommunity(FacesContext.getCurrentInstance());
     }
 
     /**
      * @return the nbLayers
      */
     public int getNbLayers() {
         return GEOVIEWERSERVICE.getNbLayer(FacesContext.getCurrentInstance());
     }
 
     /**
      * @return the WMS url list available for WMS tool
      */
     public List<SelectItem> getWMSList() {
         return WMSList;
     }
 
     /**
      * @return the WPS url list available for WPS tool
      */
     public List<SelectItem> getWPSList() {
         return WPSList;
     }
 
     /**
      * @return true if the WPS tool is activated
      */
     public Boolean getWPSTool() {
         return WPSTool;
     }
 
     /**
      * @return true if the Graticule tool is activated
      */
     public Boolean getGraticuleTool() {
         return graticuleTool;
     }
     
     /**
      * @return true if the Graticule tool is activated
      */
     public Boolean getDistanceTool() {
         return distanceTool;
     }
     
     /**
      * @return true if the Graticule tool is activated
      */
     public Boolean getSurfaceTool() {
         return surfaceTool;
     }
 
     /**
      * @return render or not the WPS tool
      */
     public boolean isRenderWPS() {
         return renderWPS;
     }
 }
