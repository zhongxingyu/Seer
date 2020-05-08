 /* -*- mode: Java; c-basic-offset: 2; -*- */
 
 /**
  * LZX Attributes
  */
 
 package org.openlaszlo.compiler;
 
 import org.openlaszlo.xml.internal.Schema.Type;
 import org.openlaszlo.xml.internal.XMLUtils;
 import org.jdom.Element;
 
 /** Contains information about an attribute of a laszlo viewsystem class.
  */
 public class AttributeSpec {
     /** The source Element from which this attribute was parsed */
     Element source = null;
     /** The attribute name */
     public String name;
     /** The default value */
     public String defaultValue = null;
     /** The setter function */
     String setter;
     /** The type of the attribute value*/ 
     public Type type;
     /** Is this attribute required to instantiate an instance of this class? */
     public boolean required = false;
     /** When does the initial value for this attribute get evaluated? */
     String when = NodeModel.WHEN_IMMEDIATELY;
 
     /**
      * Is this property allocated on the instance or the class.
      * Legal values are NodeModel.ALLOCATION_INSTANCE, and NodeModel.ALLOCATION_CLASS
      */
     String allocation = NodeModel.ALLOCATION_INSTANCE;
 
     /** If this is a method, the arglist */
     public String arglist = null;
 
     /** Can this attribute be overridden without a warning? value is null, 'true' or 'false' */
     String isfinal = null;
 
     /** Is this attribute equivalent to element content of a given type? */
     int contentType = NO_CONTENT;
 
     /** Element content types: */
     static final int NO_CONTENT = 0;
     static final int TEXT_CONTENT = 1;
     static final int HTML_CONTENT = 2;
 
   private String typeToLZX() {
     switch (contentType) {
       case TEXT_CONTENT:
         return "text";
       case HTML_CONTENT:
         return "html";
       default:
         return type.toString();
     }
   }
 
   public String toLZX(String indent, ClassModel superclass) {
     AttributeSpec superSpec = superclass.getAttribute(name, allocation);
     if (ViewSchema.METHOD_TYPE.equals(type)) {
      return indent + "  <method name='" + name + "'" + (("".equals(arglist))?"":(" args='" + arglist +"'")) + " />";
     }
 
     if (superSpec == null) {
       if (ViewSchema.EVENT_HANDLER_TYPE.equals(type)) {
         return indent + "<event name='" + name + "' />";
       }
       return indent + "<attribute name='" + name + "'" +
        ((defaultValue != null)?(" value='" + defaultValue + "'"):"") +
         ((type != null)?(" type='" + typeToLZX() + "'"):"") +
         ((when != NodeModel.WHEN_IMMEDIATELY)?(" when='" + when + "'"):"") + 
         (required?(" required='true'"):"") +
         " />";
     } else if (! ViewSchema.EVENT_HANDLER_TYPE.equals(type)) {
       String attrs = "";
       if (defaultValue != null &&
           (! defaultValue.equals(superSpec.defaultValue))) {
        attrs += " value='" + defaultValue + "'";
       }
       if (type != null &&
           (! type.equals(superclass.getAttributeType(name, allocation)))) {
         attrs += " type='" + typeToLZX() + "'";
       }
       if (when != null &&
           (! when.equals(superSpec.when))) {
         attrs += " when='" + when + "'";
       }
       if (required != superSpec.required) {
         attrs += " required='" + required + "'";
       }
       if (attrs.length() > 0) {
         return  indent + "<attribute name='" + name + "'" + attrs + " />";
       }
     }
     return null;
   }
 
   public String toString() {
     if (ViewSchema.METHOD_TYPE.equals(type)) {
       return "[AttributeSpec: method name='" + name + "'" + (("".equals(arglist))?"":(" args='" + arglist +"'")) + " isfinal="+(isfinal  == null ?  "null" : ("'"+isfinal+"'"))+ " allocation="+allocation+"]";
     } 
     if (ViewSchema.EVENT_HANDLER_TYPE.equals(type)) {
       return "[AttributeSpec: event name='" + name + "' ]";
     }
     return "[AttributeSpec: attribute name='" + name + "'" +
         ((defaultValue != null)?(" value='" + defaultValue + "'"):"") +
         ((type != null)?(" type='" + typeToLZX() + "'"):"") +
         ((when != NodeModel.WHEN_IMMEDIATELY)?(" when='" + when + "'"):"") + 
         (required?(" required='true'"):"") +
       " allocation="+allocation+
         " ";
   }
     
     AttributeSpec (String name, Type type, String defaultValue, String setter, Element source) {
         this.source = source;
         this.name = name;
         this.type = type;
         this.defaultValue = defaultValue;
         this.setter = setter;
         this.when = XMLUtils.getAttributeValue(source, "when", NodeModel.WHEN_IMMEDIATELY);
     }
 
     AttributeSpec (String name, Type type, String defaultValue, String setter, boolean required, Element source) {
         this.source = source;
         this.name = name;
         this.type = type;
         this.defaultValue = defaultValue;
         this.setter = setter;
         this.required = required;
         this.when = XMLUtils.getAttributeValue(source, "when", NodeModel.WHEN_IMMEDIATELY);
     }
 
     public AttributeSpec (String name, Type type, String defaultValue, String setter) {
         this.name = name;
         this.type = type;
         this.defaultValue = defaultValue;
         this.setter = setter;
     }
 
     AttributeSpec (String name, Type type, String defaultValue, String setter, boolean required) {
         this.name = name;
         this.type = type;
         this.defaultValue = defaultValue;
         this.setter = setter;
         this.required = required;
     }
 }
 
 /**
 * @copyright Copyright 2001-2008 Laszlo Systems, Inc.  All Rights
  * Reserved.  Use is subject to license terms.
  */
