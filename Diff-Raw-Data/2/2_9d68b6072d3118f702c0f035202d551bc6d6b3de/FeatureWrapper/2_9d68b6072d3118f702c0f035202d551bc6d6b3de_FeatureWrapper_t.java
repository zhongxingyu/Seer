 /* Copyright (c) 2001 - 2007 TOPP - www.openplans.org. All rights reserved.
  * This code is licensed under the GPL 2.0 license, availible at the root
  * application directory.
  */
 package org.geoserver.template;
 
 import java.text.DateFormat;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 
 import org.geotools.feature.FeatureCollection;
 import org.opengis.feature.simple.SimpleFeature;
 import org.opengis.feature.simple.SimpleFeatureType;
 import org.opengis.feature.type.AttributeDescriptor;
 import org.opengis.feature.type.GeometryDescriptor;
 
 import com.vividsolutions.jts.geom.Geometry;
 
 import freemarker.ext.beans.BeansWrapper;
 import freemarker.ext.beans.CollectionModel;
 import freemarker.template.Configuration;
 import freemarker.template.SimpleHash;
 import freemarker.template.SimpleSequence;
 import freemarker.template.TemplateModel;
 import freemarker.template.TemplateModelException;
 
 
 /**
  * Wraps a {@link SimpleFeature} in the freemarker {@link BeansWrapper} interface
  * allowing a template to be directly applied to a {@link SimpleFeature} or
  * {@link FeatureCollection}.
  * <p>
  * When a {@link FeatureCollection} is being processed by the template, it is
  * available via the <code>$features</code> variable, which can be broken down into single features and attributes following this hierarchy:
  * <ul>
  *   <li>features -> feature</li>
  *     <ul>
  *       <li>fid (String)</li>
  *       <li>typeName (String)</li>
  *       <li>attributes -> attribute</li>
  *       <ul>
  *         <li>value (String), a default String representation of the attribute value</li>
  *         <li>rawValue (Object), the actual attribute value if it's non null, the empty string otherwise</li>
  *         <li>name (String)</li>
  *         <li>type (String)</li>
  *         <li>isGeometry (Boolean)</li>
  *       </ul>
  *     </ul>
  * </ul>
  * Example of a template processing a feature collection which will print
  * out the features id of every feature in the collection.
  * <pre><code>
  *  &lt;#list features as feature&gt;
  *  FeatureId: ${feature.fid}
  *  &lt;/#list&gt;
  * </code></pre>
  * </p>
  * <p>
  * To use this wrapper,use the {@link Configuration#setObjectWrapper(freemarker.template.ObjectWrapper)}
  * method:
  * <pre>
  *         <code>
  *  //features we want to apply template to
  *  FeatureCollection<SimpleFeatureType, SimpleFeature> features = ...;
  *
  *  //create the configuration and set the wrapper
  *  Configuration cfg = new Configuration();
  *  cfg.setObjectWrapper( new FeatureWrapper() );
  *
  *  //get the template and go
  *  Template template = cfg.getTemplate( "foo.ftl" );
  *  template.process( features, System.out );
  *
  *         </code>
  * </pre>
  * </p>
  * </p>
  * @author Justin Deoliveira, The Open Planning Project, jdeolive@openplans.org
  * @author Andrea Aime, TOPP
  *
  */
 public class FeatureWrapper extends BeansWrapper {
     public FeatureWrapper() {
         setSimpleMapWrapper(true);
     }
     
     /**
      * Returns a sensible String value for attributes so they are
      * easily used by templates.
      * <p>
      * Special cases:
      * <ul>
      * <li>for Date values returns a default {@link DateFormat} representation</li>
      * <li>for Boolean values returns "true" or "false"</li>
      * <li>for null values returns an empty string</li>
      * <li>for any other value returns its toString()</li>
      * </ul> 
      * </p>
      * 
      * @param o could be an instance of Date (a special case)
      * @return the formated date as a String, or the object
      */
     protected String wrapValue(Object o) {
         if(o == null){
            //nulls throw templates off, use empty string
             return "";
         }
     	if ( o instanceof Date ) { 
     	    return DateFormat.getInstance().format( (Date)o ); 
     	}
     	if( o instanceof Boolean){
     	    return ((Boolean)o).booleanValue()? "true" : "false";
     	}
     	return String.valueOf(o);
     }
 
     public TemplateModel wrap(Object object) throws TemplateModelException {
         //check for feature collection
         if (object instanceof FeatureCollection) {
             //create a model with just one variable called 'features'
             SimpleHash map = new SimpleHash();
             map.put("features", new CollectionModel((FeatureCollection) object, this));
             map.put("type", wrap(((FeatureCollection) object).getSchema()));
 
             return map;
         } else if (object instanceof SimpleFeatureType) {
             SimpleFeatureType ft = (SimpleFeatureType) object;
             
             // create a variable "attributes" which his a list of all the 
             // attributes, but at the same time, is a map keyed by name
             Map attributeMap = new LinkedHashMap();
             for (int i = 0; i < ft.getAttributeCount(); i++) {
                 AttributeDescriptor type = ft.getAttribute(i);
 
                 Map attribute = new HashMap();
                 attribute.put("name", type.getLocalName());
                 attribute.put("type", type.getType().getBinding().getName());
                 attribute.put("isGeometry", Boolean.valueOf(type instanceof GeometryDescriptor));
 
                 attributeMap.put(type.getLocalName(), attribute);
             }
 
             // build up the result, feature type is represented by its name an attributes
             SimpleHash map = new SimpleHash();
             map.put("attributes", new SequenceMapModel(attributeMap, this));
             map.put("name", ft.getTypeName());
             return map;
         } else if (object instanceof SimpleFeature) {
             SimpleFeature feature = (SimpleFeature) object;
 
             //create the model
             SimpleHash map = new SimpleHash();
 
             //first add the feature id
             map.put("fid", feature.getID());
             map.put("typeName", feature.getFeatureType().getTypeName());
 
             //next add variables for each attribute, variable name = name of attribute
             SimpleSequence attributes = new SimpleSequence();
             Map attributeMap = new LinkedHashMap();
 
             for (int i = 0; i < feature.getAttributeCount(); i++) {
                 AttributeDescriptor type = feature.getFeatureType().getAttribute(i);
 
                 Map attribute = new HashMap();
                 Object value = feature.getAttribute(i);
                 attribute.put("value", wrapValue(value));
                 if ( value == null ) {
                     //some special case checks
                     attribute.put("rawValue", "");
                     attribute.put("isGeometry", Boolean.valueOf(Geometry.class.isAssignableFrom(type.getType().getBinding())));
                 } else {
                     attribute.put("rawValue", value);
                     attribute.put("isGeometry", Boolean.valueOf(value instanceof Geometry));
                 }
 
                 attribute.put("name", type.getName());
                 attribute.put("type", type.getType().getName());
 
                 map.put(type.getLocalName(), attribute);
                 attributeMap.put(type.getName(), attribute);
                 attributes.add(attribute);
             }
 
             // create a variable "attributes" which his a list of all the 
             // attributes, but at the same time, is a map keyed by name
             map.put("attributes", new SequenceMapModel(attributeMap, this));
 
             return map;
         }
         
         return super.wrap(object);
     }
 }
