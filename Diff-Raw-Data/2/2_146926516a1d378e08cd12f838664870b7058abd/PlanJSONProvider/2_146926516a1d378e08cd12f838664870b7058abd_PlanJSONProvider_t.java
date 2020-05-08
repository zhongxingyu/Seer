 package com.socialcomputing.wps.server.generator.json;
 
 import java.awt.Point;
 import java.io.IOException;
 import java.util.Set;
 
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.codehaus.jackson.map.ObjectMapper;
 import org.codehaus.jackson.node.ArrayNode;
 import org.codehaus.jackson.node.ObjectNode;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.socialcomputing.wps.client.applet.ActiveZone;
 import com.socialcomputing.wps.client.applet.BagZone;
 import com.socialcomputing.wps.client.applet.Base;
 import com.socialcomputing.wps.client.applet.ColorX;
 import com.socialcomputing.wps.client.applet.Env;
 import com.socialcomputing.wps.client.applet.FontX;
 import com.socialcomputing.wps.client.applet.HTMLText;
 import com.socialcomputing.wps.client.applet.LinkZone;
 import com.socialcomputing.wps.client.applet.MenuX;
 import com.socialcomputing.wps.client.applet.Plan;
 import com.socialcomputing.wps.client.applet.Satellite;
 import com.socialcomputing.wps.client.applet.ShapeX;
 import com.socialcomputing.wps.client.applet.Slice;
 import com.socialcomputing.wps.client.applet.Swatch;
 import com.socialcomputing.wps.client.applet.Transfo;
 import com.socialcomputing.wps.client.applet.VContainer;
 import com.socialcomputing.wps.server.generator.PlanContainer;
 
 /**
  * @author "Jonathan Dray <jonathan@social-computing.com>"
  */
 public class PlanJSONProvider {
 
     private static final ObjectMapper mapper = new ObjectMapper();
     private static final Logger LOG = LoggerFactory.getLogger(PlanJSONProvider.class);
 
     
     public static ObjectMapper GetMapper() {
         return mapper;
     }
 
     static public String planToString(ObjectNode node) {
         String result = null;
         try {
             result = mapper.writeValueAsString(node);
         }
         catch (JsonGenerationException e) {
             e.printStackTrace();
         }
         catch (JsonMappingException e) {
             e.printStackTrace();
         }
         catch (IOException e) {
             e.printStackTrace();
         }
         LOG.info("End JSON");
         return result;
     }
     
     /*
      * (non-Javadoc)
      * 
      * @see
      * com.socialcomputing.wps.server.generator.json.PlanJSONProvider#planToJSON
      * 
      */
     static public ObjectNode planToJSON(PlanContainer container) {
         LOG.info("Generating JSON representation of the plan");
         return toJSON(container);
     }
 
     static public void putValue(ObjectNode parent, String key, Object value) {
         if( value != null) {
             if( value instanceof Object[]) {
                 fillArray( parent.putArray(key), (Object[])value);
             }
             else {
                 fillNode( parent, key, value);
             }
         }
     }
     
     static public void fillArray(ArrayNode tab, Object[] value) {
         if( value == null)
             return;
         for (Object val : value) {
             if (val instanceof String)
                 tab.add((String) val);
             else if (val instanceof Integer)
                 tab.add((Integer) val);
             else if (val instanceof Float)
                 tab.add((Float) val);
             else if (val instanceof Long)
                 tab.add((Long) val);
            else if (val instanceof Boolean)
                tab.add((Boolean) val);
             else
                 tab.add(toJSON( val));
         }
     }
     
     static public void fillNode(ObjectNode node, String key, Object value) {
         if( value == null)
             return;
         if (value instanceof String)
             node.put(key, (String) value);
         else if (value instanceof Integer)
             node.put(key, (Integer) value);
         else if (value instanceof Float)
             node.put(key, (Float) value);
         else if (value instanceof Long)
             node.put(key, (Long) value);
         else if (value instanceof Boolean)
             node.put(key, (Boolean) value);
         else
             node.put(key, toJSON( value));
     }
 
     static protected ObjectNode toJSON(PlanContainer container) {
         ObjectNode root = mapper.createObjectNode();
         if (container.m_env == null) { return root; }
         root.put("env", toJSON(container.m_env));
         root.put("plan", toJSON(container.m_plan));
         return root;
     }
 
     static protected ObjectNode createObjectNode(String className) {
         ObjectNode node = mapper.createObjectNode();
         node.put( "cls", className);
         return node;
     }
     
     static private ObjectNode toJSON(Object value) {
         if (value == null)
             return null;
         else if (value instanceof ObjectNode)
             return ( ObjectNode)value;
         else if (value instanceof Slice)
             return toJSON((Slice) value);
         else if (value instanceof MenuX)
             return toJSON((MenuX) value);
         else if (value instanceof ColorX)
             return toJSON((ColorX) value);
         else if (value instanceof Transfo)
             return toJSON((Transfo) value);
         else if (value instanceof HTMLText)
             return toJSON((HTMLText) value);
         else if (value instanceof FontX)
             return toJSON((FontX) value);
         else if (value instanceof Point)
             return toJSON((Point) value);
         else {
             LOG.error( "toJSON class '{}' not bound: ", value.getClass().getName());
         }
         return null;
     }
     
     static private ObjectNode toJSON(Env env) {
         ObjectNode node = mapper.createObjectNode();
         node.put("flags", env.m_flags);
         node.put("inColor", toJSON(env.m_inCol));
         node.put("outColor", toJSON(env.m_outCol));
         node.put("filterColor", toJSON(env.m_filterCol));
         node.put("transfo", toJSON(env.m_transfo));
         ObjectNode props = node.putObject("props");
         for (String key : (Set<String>) env.m_props.keySet()) {
             Object val = env.m_props.get(key);
             if( val instanceof String)
                 props.put(key, (String) val);
             else if( val instanceof Object[]) {
                 ArrayNode a = props.putArray( key);
                 for( Object v : (Object[])val) {
                     if( v instanceof Integer)
                         a.add( (Integer)v);
                     else if( v instanceof Double)
                         a.add( (Double)v);
                     else
                         a.add( (String)v);
                 }
             }
         }
         ObjectNode sel = node.putObject("selections");
         for (String key : (Set<String>) env.m_selections.keySet()) {
             putValue(sel, key, env.m_selections.get(key));
         }
         return node;
     }
 
     static private ObjectNode toJSON(Plan plan) {
         ObjectNode node = mapper.createObjectNode();
         // Add the array of nodes
         node.put("nodesCnt", plan.m_nodesCnt);
         ArrayNode nodes = node.putArray("nodes");
         for( int i = 0; i < plan.m_nodesCnt; ++i) {
             // Only BagZone
             nodes.add(toJSON(plan.m_nodes[i]));
         }
         // Add the array of links
         node.put("linksCnt", plan.m_linksCnt);
         ArrayNode links = node.putArray("links");
         for (ActiveZone zone : plan.m_links) {
             links.add(toJSON(zone));
         }
         return node;
     }
 
     static private ObjectNode toJSON(Point point) {
         ObjectNode node = createObjectNode("Point");
         node.put("x", point.x);
         node.put("y", point.y);
         return node;
     }
     
     // Ok
     static private ObjectNode toJSON(ColorX color) {
         ObjectNode node = createObjectNode("ColorX");
         node.put("color", color.m_color);
         if (color.m_scolor != null)
             node.put("scolor", color.m_scolor);
         return node;
     }
 
     /*
      * LinkZone / BagZone : ActiveZone subclasses
      */
     // Ok
     static private ObjectNode toJSON(ActiveZone zone) {
         ObjectNode node = null;
         if (zone == null)
             node = createObjectNode("ActiveZone");
         else if (zone instanceof BagZone)
             node = toJSON((BagZone) zone);
         else if (zone instanceof LinkZone)
             node = toJSON((LinkZone) zone);
         else 
             node = createObjectNode("ActiveZone");
         
         node.put("flags", zone.m_flags);
         node.put("curSwatch", toJSON(zone.getCurSwatch()));
         node.put("restSwatch", toJSON(zone.getRestSwatch()));
         ObjectNode propsnode = node.putObject("props");
         for (String key : (Set<String>)zone.keySet()) {
             putValue(propsnode, key, zone.get(key));
         }
         return node;
     }
 
     // Ok
     static private ObjectNode toJSON(BagZone zone) {
         ObjectNode node = createObjectNode("BagZone");
         ArrayNode subzone = node.putArray("subZones");
         if (zone != null&& zone.m_subZones != null) {
             for (ActiveZone az : zone.m_subZones) {
                 subzone.add(toJSON(az));
             }
         }
         return node;
     }
 
     // Ok
     static private ObjectNode toJSON(LinkZone zone) {
         ObjectNode node = createObjectNode("LinkZone");
         node.put("from", zone.m_from == null ? -1 : (Integer)zone.m_from.get("_INDEX"));
         node.put("to", zone.m_to == null ? -1 : (Integer)zone.m_to.get("_INDEX"));
         return node;
     }
 
     // Ok
     static private ObjectNode toJSON(Transfo transfo) {
         ObjectNode node = createObjectNode("Transfo");
         node.put("dir", transfo.m_dir);
         node.put("pos", transfo.m_pos);
         node.put("scl", transfo.m_scl);
         node.put("flags", transfo.m_flags);
         return node;
     }
     
     /*
      * Satellite / ShapeX / Slice / Swatch / HTMLText / FontX / MenuX : Base subclasses
      */
     // Ok
     static private ObjectNode toJSON(Satellite satellite) {
         ObjectNode node = createObjectNode("Satellite");
         node.put("shapex", toJSON(satellite.getShape()));
         ArrayNode slices = node.putArray("slices");
         for (Slice slice : satellite.getSlices()) {
             slices.add(toJSON(slice));
         }
         return toJSON( satellite, node);
     }
     
     // Ok
     static private ObjectNode toJSON(ShapeX shape) {
         ObjectNode node = createObjectNode("ShapeX");
         return toJSON( shape, node);
     }
     
     // Ok
     static private ObjectNode toJSON(Slice slice) {
         ObjectNode node = createObjectNode("Slice");
         return toJSON( slice, node);
     }
     
     // Ok
     static private ObjectNode toJSON(Swatch swatch) {
         ObjectNode node = createObjectNode("Swatch");
         ObjectNode refs = node.putObject("refs");
         for (String key : (Set<String>) swatch.m_refs.keySet()) {
             putValue(refs, key, swatch.m_refs.get(key));
         }
         ArrayNode sats = node.putArray("satellites");
         for (Satellite sat : swatch.getSatellites()) {
             sats.add(toJSON(sat));
         }
         return toJSON( swatch, node);
     }
     
     // Ok
     static private ObjectNode toJSON(HTMLText text) {
         ObjectNode node = createObjectNode("HtmlText");
         return toJSON( text, node);
     }
     
     // Ok
     static private ObjectNode toJSON(FontX font) {
         ObjectNode node = createObjectNode("FontX");
         return toJSON( font, node);
     }
 
     // Ok
     static private ObjectNode toJSON(MenuX menu) {
         ObjectNode node = createObjectNode("MenuX");
         ArrayNode sats = node.putArray("menu");
         if( menu != null && menu.m_items != null) {
             for (MenuX submenu : menu.m_items) {
                 sats.add(toJSON(submenu));
             }
         }
         return toJSON( menu, node);
     }
     
    // Ok
     static private ObjectNode toJSON(Base base, ObjectNode node) {
         ArrayNode containers = node.putArray("containers");
         for (VContainer container : base.m_containers) {
             if( container == null)
                 containers.add( "null");
             else
                 containers.add(toJSON(container));
         }
         return node;
     }
 
     // Ok
     static private ObjectNode toJSON(VContainer container) {
         ObjectNode node = createObjectNode("VContainer");
         if( container.m_value != null) {
             fillNode( node, "value", container.m_value);
         }
         node.put("bound", container.isBound());
         return node;
     }
  }
