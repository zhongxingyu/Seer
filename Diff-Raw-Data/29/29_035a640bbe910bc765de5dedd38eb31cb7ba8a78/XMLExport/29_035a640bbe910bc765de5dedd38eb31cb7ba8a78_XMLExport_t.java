 package com.joshondesign.treegui.actions;
 
 import com.joshondesign.treegui.Binding;
 import com.joshondesign.treegui.docmodel.*;
 import com.joshondesign.treegui.modes.aminojava.DynamicNode;
 import com.joshondesign.treegui.modes.aminojava.Property;
 import com.joshondesign.xml.XMLWriter;
 import java.io.PrintWriter;
 import java.net.URI;
 import java.net.URISyntaxException;
 import org.joshy.gfx.util.u;
 
 public class XMLExport {
 
     public static void exportToXML(PrintWriter printWriter, Page page, SketchDocument document) throws URISyntaxException {
         XMLWriter xml = new XMLWriter(printWriter, new URI(""));
         xml.header();
         xml.start("page");
         xml.attr("mode",document.getModeId());
         // render non-visual nodes first
 
         xml.start("layers");
         //render visual nodes next
         for(Layer layer : page.children()) {
             xml.start("layer");
             exportChildren(xml, layer.children(), 200, 200);
             xml.end();
         }
         xml.end();
 
         //bindings
         xml.start("bindings");
         for(Binding binding : document.getBindings()) {
             u.p("exporting: " + binding.toString());
             xml.start("binding");
             xml.attr("sourceid", binding.getSource().getId());
             Property src = binding.getSourceProperty();
             xml.attr("sourceprop", src.getName());
             xml.attr("sourcevirtual",Boolean.toString(src.isProxy()));
             if(src.isProxy()) {
                 xml.attr("sourcemaster",src.getMasterProperty());
             }
             xml.attr("targetid",binding.getTarget().getId());
             xml.attr("targetprop",binding.getTargetProperty().getName());
             DynamicNode nd = binding.getSource();
             if(nd.isMirror()) {
                 xml.attr("mirror","true");
             }
             xml.end();
         }
         xml.end();
 
         xml.end();
         xml.close();
     }
 
     private static void nonvisualNodeToXML(SketchNode node, XMLWriter xml) {
         if(!node.isVisual() && node instanceof DynamicNode) {
             DynamicNode nd = (DynamicNode) node;
            u.p("non visual node " + nd.getName());
             xml.start("node");
             if(nd.hasProperty("class")) {
                 xml.attr("class",nd.getProperty("class").encode());
             } else {
                 xml.attr("class","unknown");
             }
             xml
                     .attr("name", nd.getName())
                     .attr("id", nd.getId())
                     .attr("mirror",Boolean.toString(nd.isMirror()))
             ;
             for (Property prop : nd.getSortedProperties()) {
                 xml.start("property");
                 xml.attr("name", prop.getName());
                 xml.attr("value", prop.encode());
                 xml.attr("type", prop.getType().getName());
                 xml.attr("exported",Boolean.toString(prop.isExported()));
                 xml.attr("visible", Boolean.toString(prop.isVisible()));
                 xml.attr("bindable", Boolean.toString(prop.isBindable()));
                 xml.attr("compound",Boolean.toString(prop.isCompound()));
                 xml.attr("list",Boolean.toString(prop.isList()));
                 xml.attr("masterprop",""+prop.getMasterProperty());
                 if(prop.getExportName() != null) {
                     xml.attr("exportname", prop.getExportName());
                 }
                 xml.end();
             }
             xml.end();
         }
         for(SketchNode child : node.children()) {
             nonvisualNodeToXML(child, xml);
         }
     }
 
     private static void groupNodeToXML(XMLWriter xml, Group node) {
         xml.start("group")
             .attr("id",node.getId())
                 .attr("translateX",""+node.getTranslateX())
                 .attr("translateY",""+node.getTranslateY());
         exportChildren(xml, node.children(), 200, 200);
         xml.end();
     }
 
     private static void exportChildren(XMLWriter xml, Iterable<SketchNode> children, double width, double height) {
         xml.start("children");
         for(SketchNode node : children) {
             if(node instanceof DynamicNode) {
                 DynamicNode nd2 = (DynamicNode) node;
                 if(nd2.isVisual()) {
                     visualNodeToXML(xml, nd2, width, height, true);
                 } else {
                     nonvisualNodeToXML(nd2, xml);
                 }
             } else {
                 u.p("not a dynamic node. what do we do?");
                 if(node instanceof Group) {
                     groupNodeToXML(xml, (Group)node);
                 }
             }
         }
         xml.end();
     }
 
     private static void visualNodeToXML(XMLWriter xml, DynamicNode node, double width, double height, boolean parentAnchor) {
        u.p("visual node " + node.getName());
         xml.start("node")
                 .attr("id", node.getId())
                 .attr("class", node.getProperty("class").encode())
                 .attr("visual", Boolean.toString(node.isVisual()))
                 .attr("resize", node.getResize().name())
                 .attr("container", Boolean.toString(node.isContainer()))
                 .attr("custom", Boolean.toString(node.isCustom()))
                 .attr("name", node.getName())
         ;
         if(node.isCustom()) {
             xml.attr("customClass",node.getProperty("customClass").getStringValue());
         }
         for (Property prop : node.getSortedProperties()) {
 
             if(prop.getName().equals("customClass")) continue;
             xml.start("property");
             xml.attr("name", prop.getName());
             if(prop.getExportName() != null) {
                 xml.attr("exportname", prop.getExportName());
             }
             if(prop.getType().isEnum()) {
                 xml.attr("enum", "true");
             }
             xml.attr("value", prop.encode())
                     .attr("type", prop.getType().getName());
             xml.attr("exported",Boolean.toString(prop.isExported()));
             xml.attr("visible", Boolean.toString(prop.isVisible()));
             xml.attr("bindable", Boolean.toString(prop.isBindable()));
             xml.attr("compound",Boolean.toString(prop.isCompound()));
             xml.attr("list",Boolean.toString(prop.isList()));
             xml.attr("masterprop",""+prop.getMasterProperty());
             xml.end();
         }
 
         if(parentAnchor) {
             double w = node.getProperty("width").getDoubleValue();
             double h = node.getProperty("height").getDoubleValue();
             double tx = node.getTranslateX();
             double ty = node.getTranslateY();
             xml.start("property","name","right")
                     .attr("type","java.lang.Double")
                     .attr("value",""+(width-tx - w))
                     .end()
             ;
             xml.start("property","name","bottom")
                     .attr("type","java.lang.Double")
                     .attr("value",""+(height-ty-h))
                     .end();
         }
 
         exportChildren(xml, node.children(),
                 node.getProperty("width").getDoubleValue(),
                 node.getProperty("height").getDoubleValue());
         xml.end();
     }
 
 }
