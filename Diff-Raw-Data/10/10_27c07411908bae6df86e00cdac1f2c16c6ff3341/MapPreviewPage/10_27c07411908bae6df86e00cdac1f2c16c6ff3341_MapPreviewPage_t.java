 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, available at the root
  * application directory.
  */
 package org.geoserver.web.demo;
 
 import static org.geoserver.web.demo.PreviewLayerProvider.*;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashSet;
 import java.util.List;
 import java.util.logging.Level;
 
 import org.apache.wicket.AttributeModifier;
 import org.apache.wicket.Component;
 import org.apache.wicket.behavior.AttributeAppender;
 import org.apache.wicket.markup.html.WebMarkupContainer;
 import org.apache.wicket.markup.html.basic.Label;
 import org.apache.wicket.markup.html.image.Image;
 import org.apache.wicket.markup.html.panel.Fragment;
 import org.apache.wicket.markup.repeater.RepeatingView;
 import org.apache.wicket.model.IModel;
 import org.apache.wicket.model.Model;
 import org.geoserver.catalog.LayerInfo;
 import org.geoserver.web.GeoServerApplication;
 import org.geoserver.web.GeoServerBasePage;
 import org.geoserver.web.demo.PreviewLayer.PreviewLayerType;
 import org.geoserver.web.wicket.GeoServerTablePanel;
 import org.geoserver.web.wicket.SimpleExternalLink;
 import org.geoserver.web.wicket.GeoServerDataProvider.Property;
 import org.geoserver.wfs.WFSGetFeatureOutputFormat;
 import org.vfny.geoserver.wms.GetMapProducer;
 
 /**
  * Shows a paged list of the available layers and points to previews
  * in various formats 
  */
 @SuppressWarnings("serial")
 public class MapPreviewPage extends GeoServerBasePage {
 
     PreviewLayerProvider provider = new PreviewLayerProvider();
 
     GeoServerTablePanel<PreviewLayer> table;
 
     public MapPreviewPage() {
         // output formats for the drop downs
         final List<String> wmsOutputFormats = getAvailableWMSFormats();
         final List<String> wfsOutputFormats = getAvailableWFSFormats();
 
         // build the table
         table = new GeoServerTablePanel<PreviewLayer>("table", provider) {
 
             @Override
             protected Component getComponentForProperty(String id,
                     final IModel itemModel, Property<PreviewLayer> property) {
                 PreviewLayer layer = (PreviewLayer) itemModel.getObject();
 
                 if (property == TYPE) {
                     Fragment f = new Fragment(id, "iconFragment", MapPreviewPage.this);
                     f.add(new Image("layerIcon", layer.getIcon()));
                     return f;
                 } else if (property == NAME) {
                     return new Label(id, property.getModel(itemModel));
                 } else if (property == TITLE) {
                     return new Label(id, property.getModel(itemModel));
                 } else if (property == COMMON) {
                     // openlayers preview
                     Fragment f = new Fragment(id, "commonLinks", MapPreviewPage.this);
                     String olUrl = "window.open('" + layer.getWmsLink()
                             + "&format=application/openlayers" + "');";
                     f.add(buildJSExternalLink("ol", olUrl, "OpenLayers"));
                     
                     // kml preview
                    String kmlUrl = "window.open('../wms/kml?layers=" + layer.getName() + "')";
                     f.add(buildJSExternalLink("kml", kmlUrl, "KML"));
                     
                     // gml preview (we actually want it only for vector layers)
                    String gmlUrl = "window.open('../ows?service=WFS&version=1.0.0&request=GetFeature&typeName="
                            + layer.getName() + "&maxFeatures=50')";
                     Component gmlLink = buildJSExternalLink("gml", gmlUrl, "GML");
                     f.add(gmlLink);
                     gmlLink.setVisible(layer.getType() == PreviewLayerType.Vector);
                     
                     return f;
                 } else if (property == ALL) {
                     return buildJSWMSSelect(id, wmsOutputFormats, wfsOutputFormats, layer);
                 } 
                 throw new IllegalArgumentException(
                         "Don't know a property named " + property.getName());
             }
 
         };
         table.setOutputMarkupId(true);
         add(table);
     }
 
     private List<String> getAvailableWMSFormats() {
         List<String> formats = new ArrayList<String>();
 
         final GeoServerApplication application = getGeoServerApplication();
         for (GetMapProducer producer : application
                 .getBeansOfType(GetMapProducer.class)) {
             formats.add(producer.getOutputFormat());
         }
         formats = new ArrayList<String>(new HashSet<String>(formats));
         Collections.sort(formats, new FormatComparator("format.wms."));
 
         return formats;
     }
 
     private List<String> getAvailableWFSFormats() {
         List<String> formats = new ArrayList<String>();
 
         final GeoServerApplication application = getGeoServerApplication();
         for (WFSGetFeatureOutputFormat producer : application
                 .getBeansOfType(WFSGetFeatureOutputFormat.class)) {
             formats.add((String) producer.getOutputFormats().iterator().next());
         }
         Collections.sort(formats, new FormatComparator("format.wfs."));
 
         return formats;
     }
 
     /**
      * Builds an external link that uses javascript to open the target in a new
      * window
      */
     private Component buildJSExternalLink(String id, String url, String title) {
         SimpleExternalLink sel = new SimpleExternalLink(id, new Model("#"),
                 new Model(title));
        sel.getLink().add(new AttributeAppender("onclick", new Model(url), ";"));
         return sel;
     }
 
     /**
      * Builds a select that reacts like a menu, fully javascript based, for wms outputs 
      */
     private Component buildJSWMSSelect(String id,
             List<String> wmsOutputFormats, List<String> wfsOutputFormats, PreviewLayer layer) {
         Fragment f = new Fragment(id, "menuFragment", MapPreviewPage.this);
         WebMarkupContainer menu = new WebMarkupContainer("menu");
         
         RepeatingView wmsFormats = new RepeatingView("wmsFormats");
         for (int i = 0; i < wmsOutputFormats.size(); i++) {
             String wmsOutputFormat = wmsOutputFormats.get(i);
             String label = translateFormat("format.wms." + wmsOutputFormat);
             // build option with text and value
             Label format = new Label(i + "", label);
             format.add(new AttributeModifier("value", true, new Model(wmsOutputFormat)));
             wmsFormats.add(format);
         }
         menu.add(wmsFormats);
         
         // the vector ones, it depends, we might have to hide them
         boolean vector = layer.groupInfo == null && layer.layerInfo.getType() != LayerInfo.Type.RASTER;
         WebMarkupContainer wfsFormatsGroup = new WebMarkupContainer("wfs");
         RepeatingView wfsFormats = new RepeatingView("wfsFormats");
         if(vector) {
             for (int i = 0; i < wfsOutputFormats.size(); i++) {
                 String wfsOutputFormat = wfsOutputFormats.get(i);
                 String label = translateFormat("format.wfs." + wfsOutputFormat);
                 // build option with text and value
                 Label format = new Label(i + "", label);
                 format.add(new AttributeModifier("value", true, new Model(wfsOutputFormat)));
                 wfsFormats.add(format);
             }
         }
         wfsFormatsGroup.add(wfsFormats);
         menu.add(wfsFormatsGroup);
         
         // build the wms request, redirect to it in a new window, reset the selection
         String wmsUrl = "'" + layer.getWmsLink()
                 + "&format=' + this.options[this.selectedIndex].value";
         String wfsUrl = "'../ows?service=WFS&version=1.0.0&request=GetFeature&typeName="
           + layer.getName()
           + "&maxFeatures=50"
           + "&outputFormat=' + this.options[this.selectedIndex].value";
         String choice = "(this.options[this.selectedIndex].parentNode.label == 'WMS') ? " + wmsUrl + " : " + wfsUrl;
         menu.add(new AttributeAppender("onchange", new Model("window.open("
                 + choice + ");this.selectedIndex=0"), ";"));
         f.add(menu);
         return f;
     }
     
     private String translateFormat(String format) {
         try {
             return getLocalizer().getString(format, this);
         } catch(Exception e) {
             LOGGER.log(Level.WARNING, e.getMessage());
             return "?" + format + "?";
         }
     }
     
     /**
      * Sorts the formats using the i18n translated name
      * @author aaime
      *
      */
     private class FormatComparator implements Comparator<String> {
         
         String prefix;
         
         public FormatComparator(String prefix) {
             this.prefix = prefix;
         }
 
         public int compare(String f1, String f2) {
             String t1 = translateFormat(prefix + f1);
             String t2 = translateFormat(prefix + f2);
             return t1.compareTo(t2);
         }
         
     }
 }
