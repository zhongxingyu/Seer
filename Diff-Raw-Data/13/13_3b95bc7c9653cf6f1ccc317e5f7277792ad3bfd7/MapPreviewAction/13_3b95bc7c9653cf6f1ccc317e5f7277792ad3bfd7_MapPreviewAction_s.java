 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org.  All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.vfny.geoserver.action;
 
 import com.vividsolutions.jts.geom.Envelope;
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.apache.struts.action.DynaActionForm;
 import org.geoserver.feature.FeatureSourceUtils;
 import org.geoserver.util.ReaderUtils;
 import org.geotools.geometry.jts.ReferencedEnvelope;
 import org.geotools.referencing.CRS;
 import org.opengis.referencing.FactoryException;
 import org.opengis.referencing.NoSuchAuthorityCodeException;
 import org.opengis.referencing.crs.CoordinateReferenceSystem;
 import org.opengis.referencing.operation.TransformException;
 import org.vfny.geoserver.global.ConfigurationException;
 import org.vfny.geoserver.global.CoverageInfo;
 import org.vfny.geoserver.global.Data;
 import org.vfny.geoserver.global.FeatureTypeInfo;
 import org.vfny.geoserver.global.WMS;
 import org.vfny.geoserver.util.requests.CapabilitiesRequest;
 import org.vfny.geoserver.wms.servlets.Capabilities;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 
 /**
  * <b>MapPreviewAction</b><br> Sep 26, 2005<br>
  * <b>Purpose:</b><br>
  * Gathers up all the FeatureTypes in use and returns the informaion to the
  * .jsp .<br>
  * It will also generate three files per FeatureType that will be used for
  * MapBuilder to render the data. The files are labeled as such:<br>
  * - [FeatureTypeName].html<br>
  * - [FeatureTypeName]Config.xml<br>
  * - [FeatureTypeName].xml <br>
  * <br>
  * This will communicate to a .jsp and return it three arrays of strings that contain:<br>
  * - The Featuretype's name<br>
  * - The DataStore name of the FeatureType<br>
  * - The bounding box of the FeatureType<br>
  * To change what data is output to the .jsp, you must change <b>struts-config.xml</b>.<br>
  * Look for the line:<br>
  * &lt;form-bean <br>
  * name="mapPreviewForm"<br>
  *
  * @author Brent Owens (The Open Planning Project)
  * @version
  */
 public class MapPreviewAction extends GeoServerAction {
     /* (non-Javadoc)
      * @see org.apache.struts.action.Action#execute(org.apache.struts.action.ActionMapping, org.apache.struts.action.ActionForm, javax.servlet.http.HttpServletRequest, javax.servlet.http.HttpServletResponse)
      */
     public ActionForward execute(ActionMapping mapping, ActionForm form,
         HttpServletRequest request, HttpServletResponse response)
         throws IOException, ServletException {
         ArrayList dsList = new ArrayList();
         ArrayList ftList = new ArrayList();
         ArrayList bboxList = new ArrayList();
         ArrayList ftnsList = new ArrayList();
 
         // 1) get the capabilities info so we can find out our feature types
         WMS wms = getWMS(request);
         Capabilities caps = new Capabilities(wms);
         CapabilitiesRequest capRequest = new CapabilitiesRequest("WMS", caps);
         capRequest.setHttpServletRequest(request);
 
         Data catalog = wms.getData();
         List ftypes = new ArrayList(catalog.getFeatureTypeInfos().values());
         Collections.sort(ftypes, new FeatureTypeInfoNameComparator());
 
         List ctypes = new ArrayList(catalog.getCoverageInfos().values());
         Collections.sort(ctypes, new CoverageInfoNameComparator());
 
         // 2) delete any existing generated files in the generation directory
         ServletContext sc = request.getSession().getServletContext();
 
         //File rootDir =  GeoserverDataDirectory.getGeoserverDataDirectory(sc);
         //File previewDir = new File(sc.getRealPath("data/generated"));
         if (sc.getRealPath("preview") == null) {
             //There's a problem here, since we can't get a real path for the "preview" directory.
             //On tomcat this happens when "unpackWars=false" is set for this context.
             throw new RuntimeException(
                 "Couldn't populate preview directory...is the war running in unpacked mode?");
         }
 
         File previewDir = new File(sc.getRealPath("preview"));
 
         //File previewDir = new File(rootDir, "data/generated");
         if (!previewDir.exists()) {
             previewDir.mkdirs();
         }
 
         try {
             emptyGeneratedDirectory(previewDir); // clear the contents of the directory
         } catch (ConfigurationException e) {
             e.printStackTrace();
         }
 
         // We need to create a 4326 CRS for comparison to layer's crs
         CoordinateReferenceSystem latLonCrs = null;
 
         try { // get the CRS object for lat/lon 4326
             latLonCrs = CRS.decode("EPSG:" + 4326);
         } catch (NoSuchAuthorityCodeException e) {
             String msg = "Error looking up SRS for EPSG: " + 4326 + ":" + e.getLocalizedMessage();
             LOGGER.warning(msg);
         } catch (FactoryException e) {
             String msg = "Error looking up SRS for EPSG: " + 4326 + ":" + e.getLocalizedMessage();
             LOGGER.warning(msg);
         }
 
         // 3) Go through each *FeatureType* and collect information && write out config files
         for (Iterator it = ftypes.iterator(); it.hasNext();) {
             FeatureTypeInfo layer = (FeatureTypeInfo) it.next();
 
             if (!layer.isEnabled()) {
                 continue; // if it isn't enabled, move to the next layer
             }
 
             CoordinateReferenceSystem layerCrs = layer.getDeclaredCRS();
 
             /* A quick and efficient way to grab the bounding box is to get it
              * from the featuretype info where the lat/lon bbox is loaded
              * from the DTO. We do this with layer.getLatLongBoundingBox().
              * We need to reproject the bounding box from lat/lon to the layer crs
              * for display in MapBuilder.
              */
             Envelope orig_bbox = layer.getLatLongBoundingBox();
 
             if ((orig_bbox.getWidth() == 0) || (orig_bbox.getHeight() == 0)) {
                 orig_bbox.expandBy(0.1);
             }
 
             ReferencedEnvelope bbox = new ReferencedEnvelope(orig_bbox, latLonCrs);
 
             if (!CRS.equalsIgnoreMetadata(layerCrs, latLonCrs)) {
                 try { // reproject the bbox to the layer crs
                     bbox = bbox.transform(layerCrs, true);
                 } catch (TransformException e) {
                     e.printStackTrace();
                 } catch (FactoryException e) {
                     e.printStackTrace();
                 }
             }
 
             // we now have a bounding box in the same CRS as the layer
             if ((bbox.getWidth() == 0) || (bbox.getHeight() == 0)) {
                 bbox.expandBy(0.1);
             }
 
             if (layer.isEnabled()) {
                 // prepare strings for web output
                 ftList.add(layer.getNameSpace().getPrefix() + "_"
                     + layer.getFeatureType().getTypeName()); // FeatureType name
                 ftnsList.add(layer.getNameSpace().getPrefix() + ":"
                     + layer.getFeatureType().getTypeName());
                 dsList.add(layer.getDataStoreInfo().getId()); // DataStore info
                                                               // bounding box of the FeatureType
 
                 bboxList.add(bbox.getMinX() + ", " + bbox.getMinY() + ", " + bbox.getMaxX() + ", "
                     + bbox.getMaxY());
                 //save out the mapbuilder files
                 makeMapBuilderFiles(previewDir, layer.getFeatureType().getTypeName(),
                     layer.getNameSpace().getPrefix(), bbox, layer.getSRS());
             }
         }
 
         // 3.5) Go through each *Coverage* and collect its info
         for (Iterator it = ctypes.iterator(); it.hasNext();) {
             CoverageInfo layer = (CoverageInfo) it.next();
 
             // upper right corner? lower left corner? Who knows?! Better naming conventions needed guys.
             double[] lowerLeft = layer.getEnvelope().getLowerCorner().getCoordinates();
             double[] upperRight = layer.getEnvelope().getUpperCorner().getCoordinates();
             Envelope bbox = new Envelope(lowerLeft[0], upperRight[0], lowerLeft[1], upperRight[1]);
 
             if (layer.isEnabled()) {
                 // prepare strings for web output
                 String shortLayerName = layer.getName().split(":")[1]; // we don't want the namespace prefix
 
                 ftList.add(layer.getNameSpace().getPrefix() + "_" + shortLayerName); // Coverage name
                 ftnsList.add(layer.getNameSpace().getPrefix() + ":" + shortLayerName);
 
                 dsList.add(layer.getFormatInfo().getId()); // DataStore info
                                                            // bounding box of the Coverage
 
                 bboxList.add(bbox.getMinX() + ", " + bbox.getMinY() + ", " + bbox.getMaxX() + ", "
                     + bbox.getMaxY());
 
                 //save out the mapbuilder files
                 String srsValue = layer.getSrsName().split(":")[1]; // it will look like: "EPSG:4326", so we remove the 'EPSG:"
                 makeMapBuilderFiles(previewDir, shortLayerName, layer.getNameSpace().getPrefix(),
                     bbox, srsValue);
             }
         }
 
         // 4) send off gathered information to the .jsp
         DynaActionForm myForm = (DynaActionForm) form;
 
         myForm.set("DSNameList", dsList.toArray(new String[dsList.size()]));
         myForm.set("FTNameList", ftList.toArray(new String[ftList.size()]));
         myForm.set("BBoxList", bboxList.toArray(new String[bboxList.size()]));
         myForm.set("FTNamespaceList", ftnsList.toArray(new String[ftnsList.size()]));
 
         return mapping.findForward("success");
     }
 
     /**
      * emptyGeneratedDirectory Description: Deletes all files in the
      * supplied directory (file). The directory itself will not be deleted. It
      * will also recursively go in all sub directories and delete their files
      * as well.
      *
      * @param dir The directory to be cleaned
      *
      * @throws FileNotFoundException
      * @throws ConfigurationException
      */
     private void emptyGeneratedDirectory(File dir)
         throws FileNotFoundException, ConfigurationException {
         try {
             dir = ReaderUtils.checkFile(dir, true);
         } catch (Exception e) {
             throw new ConfigurationException(e);
         }
 
         String[] files = dir.list();
 
         if ((files == null) || (files.length == 0)) {
             return;
         }
 
         for (int i = 0; i < files.length; i++) {
             File f = new File(dir, files[i]);
 
             if (f.isDirectory()) {
                 emptyGeneratedDirectory(f);
             } else {
                 if (f.exists()) {
                     f.delete();
                 }
 
                 //					if (!f.delete())	// try to delete the file
                 //						throw new FileNotFoundException("could not delete file: "+f.getName());
             }
         }
     }
 
     /**
      * makeMapBuilderFiles Description: Generates the three required
      * files to show the data in MapBuilder. The first file is the
      * FeatureTypeName.html file that points to the xml configuration to load
      * the data. The second file is the configuration file,
      * FeatureTypeNameConfig.xml that contains all the tools that MapBuilder
      * will be using (zoom widgets, preview map, etc...). The third file
      * points to the FeatureType, FeatureTypeName.xml.
      *
      * @param previewDir the root generated directory where the files will be
      *        written to
      * @param layerName the FeatureType in question
      * @param namespace DOCUMENT ME!
      * @param bbox the bounding box of the FeatureType
      * @param srsValue DOCUMENT ME!
      *
      * @throws FileNotFoundException
      * @throws IOException
      */
     private void makeMapBuilderFiles(File previewDir, String layerName, String namespace,
         Envelope bbox, String srsValue) throws FileNotFoundException, IOException {
         //layerName = layerName.replaceAll(":", "_");	// Coverage names come with the namespace, remove the colon
         //  so we can write out the file without error
         File html_file = new File(previewDir, namespace + "_" + layerName + ".html");
         File config_file = new File(previewDir, namespace + "_" + layerName + "Config.xml");
         File xml_file = new File(previewDir, namespace + "_" + layerName + ".xml");
 
         //JD: making the exists check, since they dont have to be generated
         // every time
 
         // *.html
         if (!html_file.exists()) {
             FileOutputStream html_fos = new FileOutputStream(html_file);
             PrintStream html_out = new PrintStream(html_fos);
             createIndexHTML(html_out, layerName, namespace);
             html_out.close();
         }
 
         if (!config_file.exists()) {
             //*Config.xml
             FileOutputStream config_fos = new FileOutputStream(config_file);
             PrintStream config_out = new PrintStream(config_fos);
             createConfigXML(config_out, layerName, namespace, srsValue);
             config_out.close();
         }
 
         if (!xml_file.exists()) {
             //*.xml
             FileOutputStream xml_fos = new FileOutputStream(xml_file);
             PrintStream xml_out = new PrintStream(xml_fos);
             createLayersXML(xml_out, layerName, namespace, bbox, srsValue);
             xml_out.close();
         }
     }
 
     private void createIndexHTML(PrintStream out, String ft_name, String ft_namespace) {
         out.println("<!DOCTYPE HTML PUBLIC \"-//W3C//DTD HTML 3.2 Final//EN\">");
         out.println("<!--");
         out.println("Description: A Browser based Transactional Web Feature Server client");
         out.println("             based on javascript and XSL");
         out.println("             libraries from http://mapbuilder.sourceforge.net .");
         out.println("Licence:     GPL as per: http://www.gnu.org/copyleft/gpl.html");
         out.println("-->");
         out.println("");
         out.println("<html>");
         out.println("<head>");
         // CHANGE HERE
         out.println("<title>" + ft_namespace + ":" + ft_name + " Preview</title>");
         out.println("");
         out.println("<link rel=\"stylesheet\" href=\"../../style.css\"");
         out.println("	type=\"text/css\">");
         out.println("<link rel=\"stylesheet\" href=\"../mb/lib/skin/default/html.css\"");
         out.println("	type=\"text/css\">");
         out.println("");
         out.println("<script type=\"text/javascript\">");
         out.println("      // URL of Mapbuilder configuration file.");
         // CHANGE HERE
         out.println("      var mbConfigUrl='" + ft_namespace + "_" + ft_name + "Config.xml" + "';");
         out.println("    </script>");
         out.println("<script type=\"text/javascript\" src=\"../mb/lib/Mapbuilder.js\"></script>");
         out.println("</head>");
         out.println("");
         out.println("<body onload=\"mbDoLoad()\">");
         out.println("<!-- Layout mapbuilder widgets and HTML -->");
         out.println("<h2><a href=\"http://geoserver.sf.net\">GeoServer</a>/<a");
         out.println("	href=\"http://mapbuilder.sourceforge.net\">Map Builder</a>");
         // CHANGE HERE
         out.println("" + ft_namespace + ":" + ft_name + " preview</h2>");
         out.println("");
         out.println("<table border=\"0\">");
         out.println("	<tr>");
         out.println("		<td valign=\"top\" id=\"locatorMap\" style=\"background-color: white;\" />");
         out.println("		<td rowspan=\"2\" valign=\"top\" />");
         out.println("		<table border=\"0\">");
         out.println("			<tr>");
         out.println("				<td align=\"left\" id=\"mainButtonBar\"/>");
         out.println("				<td align=\"right\" id=\"cursorTrack\" />");
         out.println("			</tr>");
         out.println("			<tr>");
         out.println(
             "				<td colspan=\"2\" id=\"mainMapPane\" style=\"background-color: white;\" />");
         out.println("			</tr>");
         out.println("	<tr align=\"right\">");
         out.println("		<td colspan=\"2\">");
         out.println("		<table>");
         out.println("			<tr>");
         out.println("        <td align=\"left\" id=\"mapScaleText\"/>");
         out.println(
             "				<td align=\"right\">Powered by <a href=\"http://mapbuilder.sourceforge.net\">Community Map");
         out.println("				Builder</a></td>");
         out.println("				<td><img src=\"../mb/lib/skin/default/images/Icon.gif\" alt=\"\" /></td>");
         out.println("			</tr>");
         out.println("		</table>");
         out.println("		</td>");
         out.println("	</tr>");
         out.println("		</table>");
         out.println("		</td>");
         out.println("	</tr>");
         out.println("	<tr>");
         out.println("		<td id=\"legend\" />");
         out.println("	</tr>");
         out.println("	<tr>");
         out.println("		<td colspan=\"3\" id=\"featureList\" />");
         out.println("	</tr>");
         out.println("	<tr>");
         out.println("		<td colspan=\"3\" id=\"transactionResponse\" />");
         out.println("	</tr>");
         out.println("	<tr>");
         out.println("		<td colspan=\"3\">");
         out.println("		<div id=\"eventLog\" />");
         out.println("		</td>");
         out.println("	</tr>");
         out.println("</table>");
         out.println("</body>");
         out.println("</html>");
     }
 
     private void createConfigXML(PrintStream out, String ft_name, String ft_namespace,
         String srsValue) {
         out.println("<?xml version=\"1.0\" encoding=\"utf-8\" standalone=\"no\"?>");
         out.println(
             "<MapbuilderConfig version=\"0.2.1\" id=\"referenceTemplate\" xmlns=\"http://mapbuilder.sourceforge.net/mapbuilder\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://mapbuilder.sourceforge.net/mapbuilder ../../mapbuilder/lib/schemas/config.xsd\">");
         out.println("  <!--");
         out.println("    Description: This configuration file determines what components from the");
         out.println(
             "                 Mapbuilder library are to be included in a Main Mapbuilder web");
         out.println("                 page.");
         out.println("    Licence:     GPL as per: http://www.gnu.org/copyleft/gpl.html ");
         out.println("");
         out.println("    $Id$");
         out.println("  -->");
         out.println("  <!--");
         out.println("    All static images should be relative to this URL.");
         out.println("    This dir is relative the dir of the main html file.");
         out.println("  -->");
         out.println("  <models>");
         out.println("    <Context id=\"mainMap\">");
         // CHANGE HERE
         out.println("      <defaultModelUrl>" + ft_namespace + "_" + ft_name
             + ".xml</defaultModelUrl>");
         out.println("      <widgets>");
         out.println("        <MapPane id=\"mainMapWidget\">");
         out.println("          <htmlTagId>mainMapPane</htmlTagId>");
         out.println("          <mapContainerId>mainMapContainer</mapContainerId>");
         out.println("        </MapPane>");
         out.println("        <AoiBoxWZ id=\"aoiBox2\">");
         out.println("          <htmlTagId>mainMapPane</htmlTagId>");
         out.println("          <stylesheet>../mb/lib/widget/GmlRenderer.xsl</stylesheet>");
         out.println("          <lineColor>#FF0000</lineColor>");
         out.println("          <lineWidth>1</lineWidth>");
         out.println("          <crossSize>15</crossSize>");
         out.println("          <mapContainerId>mainMapContainer</mapContainerId>");
         out.println("        </AoiBoxWZ>");
         out.println("        <CursorTrack id=\"cursorTrack\">");
         out.println("          <mouseHandler>mainMap</mouseHandler>");
 
         if (!srsValue.equalsIgnoreCase("4326")) { // temporary so it doesn't die with custom crs
             out.println("          <showLatLong>false</showLatLong>");
 
             //out.println("          <showXY>true<showXY>");
             // <showXY> is supposed to work but isn't, look into this.
         }
 
         out.println("        </CursorTrack>");
         out.println("        <Legend id=\"legend\">");
         out.println("        </Legend>");
         out.println("        <MapScaleText id=\"mapScaleText\">");
         out.println("        </MapScaleText>");
         out.println("      </widgets>");
         out.println("      <tools>");
         out.println("        <AoiMouseHandler id=\"mainAoi\"/>");
         out.println("        <DragPanHandler id=\"mainDragPan\">");
         out.println("          <enabled>false</enabled>");
         out.println("        </DragPanHandler>");
         out.println("        <MouseClickHandler id=\"mainMouseClick\"/>");
         out.println("      </tools>");
         out.println("    </Context>");
         out.println("");
         out.println("    <Context id=\"locator\">");
         // CHANGE HERE
         out.println("      <defaultModelUrl>" + ft_namespace + "_" + ft_name
             + ".xml</defaultModelUrl>");
         out.println("      <widgets>");
         out.println("        <MapPane id=\"locatorWidget\">");
         out.println("          <htmlTagId>locatorMap</htmlTagId>");
         out.println("          <targetModel>mainMap</targetModel>");
         out.println("          <mapContainerId>locatorContainer</mapContainerId>");
         out.println("          <fixedWidth>180</fixedWidth>");
         out.println("        </MapPane>");
         out.println("        <AoiBoxWZ id=\"aoiBox2\">");
         out.println("          <htmlTagId>locatorMap</htmlTagId>");
         out.println("          <stylesheet>../mb/lib/widget/GmlRenderer.xsl</stylesheet>");
         out.println("          <lineColor>#FF0000</lineColor>");
         out.println("          <lineWidth>1</lineWidth>");
         out.println("          <crossSize>15</crossSize>");
         out.println("          <mapContainerId>locatorContainer</mapContainerId>");
         out.println("        </AoiBoxWZ>");
         out.println("      </widgets>");
         out.println("      <tools>");
         out.println("        <AoiMouseHandler id=\"locatorAoi\"/>");
         out.println("        <ZoomToAoi id=\"locatorZoomToAoi\">");
         out.println("          <targetModel>mainMap</targetModel>");
         out.println("        </ZoomToAoi>");
         out.println("      </tools>");
         out.println("    </Context>");
         out.println("");
         out.println("    <Transaction id=\"transaction\">");
         out.println("      <widgets>");
         out.println("        <TransactionResponse id=\"transactionResponse\">");
         out.println("        </TransactionResponse>");
         out.println("      </widgets>");
         out.println("    </Transaction>");
         out.println("    <FeatureCollection id=\"featureCollection\">");
         out.println(
             "      <namespace>xmlns:gml='http://www.opengis.net/gml' xmlns:wfs='http://www.opengis.net/wfs' xmlns:topp='http://www.openplans.org/topp'</namespace>");
         out.println("      <widgets>");
         out.println("        <GmlRendererWZ id=\"testGmlRenderer\">");
         out.println("          <htmlTagId>mainMapPane</htmlTagId>");
         out.println("          <targetModel>mainMap</targetModel>");
         out.println("          <mapContainerId>mainMapContainer</mapContainerId>");
         out.println("          <lineColor>#FF0000</lineColor>");
         out.println("          <lineWidth>1</lineWidth>");
         out.println("          <pointDiameter>10</pointDiameter>");
         out.println("        </GmlRendererWZ>");
         out.println("        <FeatureList id=\"featureList\">");
         out.println("        </FeatureList>");
         out.println("      </widgets>");
         out.println("    </FeatureCollection>");
         out.println("  </models>");
         out.println("  <widgets>");
         out.println("    <ZoomIn id=\"zoomIn\">");
         out.println("      <buttonBar>mainButtonBar</buttonBar>");
         out.println("      <targetModel>mainMap</targetModel>");
         out.println("      <mouseHandler>mainAoi</mouseHandler>");
         out.println("      <class>RadioButton</class>");
         out.println("      <selected>true</selected>");
         out.println("      <enabledSrc>/images/ZoomInEnable.gif</enabledSrc>");
         out.println("      <disabledSrc>/images/ZoomInDisable.gif</disabledSrc>");
         out.println("      <tooltip xml:lang=\"en\">click and drag to zoom in</tooltip>");
         out.println(
             "      <tooltip xml:lang=\"fr\">cliquer et faire glisser la souris pour agrandir</tooltip>");
         out.println("    </ZoomIn>");
         out.println("    <ZoomOut id=\"zoomOut\">");
         out.println("      <buttonBar>mainButtonBar</buttonBar>");
         out.println("      <targetModel>mainMap</targetModel>");
         out.println("      <mouseHandler>mainAoi</mouseHandler>");
         out.println("      <class>RadioButton</class>");
         out.println("      <enabledSrc>/images/ZoomOutEnable.gif</enabledSrc>");
         out.println("      <disabledSrc>/images/ZoomOutDisable.gif</disabledSrc>");
         out.println("      <tooltip xml:lang=\"en\">click to zoom out</tooltip>");
         out.println("      <tooltip xml:lang=\"fr\">cliquer pour r???e</tooltip>");
         out.println("    </ZoomOut>");
         out.println("    <DragPan id=\"dragPan\">");
         out.println("      <buttonBar>mainButtonBar</buttonBar>");
         out.println("      <targetModel>mainMap</targetModel>");
         out.println("      <mouseHandler>mainDragPan</mouseHandler>");
         out.println("      <class>RadioButton</class>");
         out.println("      <enabledSrc>/images/PanEnable.gif</enabledSrc>");
         out.println("      <disabledSrc>/images/PanDisable.gif</disabledSrc>");
         out.println("      <tooltip xml:lang=\"en\">click and drag to pan</tooltip>");
         out.println(
             "      <tooltip xml:lang=\"fr\">cliquer et faire glisser la souris pour voir un autre parti de la carte</tooltip>");
         out.println("    </DragPan>");
         out.println("    <Reset id=\"reset\">");
         out.println("      <buttonBar>mainButtonBar</buttonBar>");
         out.println("      <targetModel>mainMap</targetModel>");
         out.println("      <class>Button</class>");
         out.println("      <disabledSrc>/images/ResetExtentDisable.gif</disabledSrc>");
         out.println("      <tooltip xml:lang=\"en\">reset the map to full extent</tooltip>");
         out.println(
             "      <tooltip xml:lang=\"fr\">redonner la carte ses dimensions compl???ts</tooltip>");
         out.println("    </Reset>");
         out.println("  </widgets>");
         out.println("  <skinDir>../mb/lib/skin/default</skinDir>");
         out.println("</MapbuilderConfig>");
     }
 
     private void createLayersXML(PrintStream out, String ft_name, String ft_namespace,
         Envelope bbox, String srsValue) throws IOException {
         int width;
         int height;
         double ratio = bbox.getHeight() / bbox.getWidth();
 
         if (ratio < 1) {
             width = 500;
             height = (int) Math.round(500 * ratio);
         } else {
             width = (int) Math.round(500 / ratio);
             height = 500;
         }
 
         out.println("<?xml version=\"1.0\" encoding=\"ISO-8859-1\" standalone=\"no\"?>");
         out.println(
             "<ViewContext version=\"1.0.0\" id=\"atlas_world\" xmlns=\"http://www.opengis.net/context\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xsi:schemaLocation=\"http://www.opengis.net/context http://schemas.opengis.net/context/1.0.0/context.xsd\">");
         out.println("  <General>");
         out.println("    <Window width=\"" + width + "\" height=\"" + height + "\"/>");
         out.println("    <BoundingBox SRS=\"EPSG:" + srsValue + "\" minx=\"" + bbox.getMinX()
             + "\" miny=\"" + bbox.getMinY() + "\" maxx=\"" + bbox.getMaxX() + "\" maxy=\""
             + bbox.getMaxY() + "\"/>");
         // CHANGE HERE
         out.println("    <Title>" + ft_namespace + ":" + ft_name + " Map</Title>");
         out.println("    <KeywordList>");
         // CHANGE HERE
         out.println("      <Keyword>" + ft_namespace + ":" + ft_name + "</Keyword>");
         out.println("    </KeywordList>");
         out.println("    <Abstract></Abstract>");
         out.println("  </General>");
         out.println("  <LayerList>");
         out.println("    <Layer queryable=\"1\" hidden=\"0\">");
         // CHANGE HERE
         out.println("      <Server service=\"OGC:WMS\" version=\"1.1.1\" title=\"" + ft_namespace
             + ":" + ft_name + " Preview\">");
         out.println("        <OnlineResource xlink:type=\"simple\" xlink:href=\"../wms\"/>");
         out.println("      </Server>");
         // CHANGE HERE
         out.println("      <Name>" + ft_namespace + ":" + ft_name + "</Name>");
         // CHANGE HERE
         out.println("      <Title>" + ft_namespace + ":" + ft_name + "</Title>");
         out.println("      <SRS>EPSG:" + srsValue + "</SRS>");
         out.println("      <FormatList><Format current=\"1\">image/png</Format></FormatList>");
         out.println("    </Layer>");
         out.println("  </LayerList>");
         out.println("</ViewContext>");
     }
 
     private static class FeatureTypeInfoNameComparator implements Comparator {
         public int compare(Object o1, Object o2) {
             FeatureTypeInfo ft1 = (FeatureTypeInfo) o1;
             FeatureTypeInfo ft2 = (FeatureTypeInfo) o2;
             String ft1Name = ft1.getNameSpace().getPrefix() + ft1.getName();
             String ft2Name = ft2.getNameSpace().getPrefix() + ft2.getName();
 
             return ft1Name.compareTo(ft2Name);
         }
     }
 
     private static class CoverageInfoNameComparator implements Comparator {
         public int compare(Object o1, Object o2) {
             CoverageInfo c1 = (CoverageInfo) o1;
             CoverageInfo c2 = (CoverageInfo) o2;
             String ft1Name = c1.getNameSpace().getPrefix() + c1.getName();
             String ft2Name = c2.getNameSpace().getPrefix() + c2.getName();
 
             return ft1Name.compareTo(ft2Name);
         }
     }
 }
