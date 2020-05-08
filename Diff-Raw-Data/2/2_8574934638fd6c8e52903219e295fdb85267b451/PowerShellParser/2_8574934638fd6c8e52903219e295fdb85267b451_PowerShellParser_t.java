 /*
  * Copyright © 2010 Red Hat, Inc.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.redhat.rhevm.api.powershell.util;
 
 import java.io.IOException;
 import java.io.StringReader;
 import java.math.BigDecimal;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 import org.w3c.dom.NodeList;
 import org.xml.sax.InputSource;
 import org.xml.sax.SAXException;
 
 import com.redhat.rhevm.api.model.Version;
 import com.redhat.rhevm.api.powershell.enums.EnumMapper;
 
 public class PowerShellParser {
     private static final Log log = LogFactory.getLog(PowerShellCmd.class);
 
     public static final String DATE_TYPE = "System.DateTime";
     public static final String STRING_TYPE = "System.String";
 
     private DocumentBuilder documentBuilder;
     private EnumMapper enumMapper;
 
     public void setDocumentBuilder(DocumentBuilder documentBuilder) {
         this.documentBuilder = documentBuilder;
     }
     public void setEnumMapper(EnumMapper enumMapper) {
         this.enumMapper = enumMapper;
     }
 
     /* REVIST: powershell seems to be wrapping long lines
      */
     private String stripNewlines(String contents) {
         return contents.replaceAll("\r", "").replaceAll("\n", "");
     }
 
     public synchronized List<Entity> parse(String contents) {
         log.info("Parsing powershell output '" + contents + "'");
         InputSource source = new InputSource(new StringReader(stripNewlines(contents)));
         Node doc;
         try {
             doc = documentBuilder.parse(source);
         } catch (SAXException saxe) {
             throw new PowerShellException("XML parsing error", saxe);
         } catch (IOException ioe) {
             throw new PowerShellException("I/O error parsing XML", ioe);
         }
         return parseDoc(doc);
     }
 
     private List<Entity> parseDoc(Node doc) {
         NodeList children = doc.getChildNodes();
         for (int i = 0; i < children.getLength(); i++) {
             Node child = children.item(i);
             if (child.getNodeType() == Node.ELEMENT_NODE &&
                 child.getNodeName().equals("Objects")) {
                 return parseObjects(child);
             }
         }
         return new ArrayList<Entity>();
     }
 
     private List<Entity> parseObjects(Node objects) {
         List<Entity> ret = new ArrayList<Entity>();
         NodeList children = objects.getChildNodes();
         for (int i = 0; i < children.getLength(); i++) {
             Node child = children.item(i);
             if (child.getNodeType() == Node.ELEMENT_NODE &&
                 child.getNodeName().equals("Object")) {
                 Entity entity = parseObject(child);
                 if (entity.getType() != null) {
                     ret.add(entity);
                 }
             }
         }
         return ret;
     }
 
     private Entity parseObject(Node object) {
         Entity ret = new Entity();
         NamedNodeMap attrs = object.getAttributes();
         ret.setType(getAttr(attrs, "Type"));
         ret.setValue(getText(object));
         for (Property prop : getProperties(object, enumMapper)) {
             ret.addProperty(prop);
         }
         return ret;
     }
 
     private static String getAttr(NamedNodeMap attrs, String name) {
         Node type = attrs.getNamedItem(name);
         if (type != null) {
             return type.getNodeValue();
         } else {
             return null;
         }
     }
 
     private static String getText(Node node) {
         StringBuffer buf = new StringBuffer();
         NodeList children = node.getChildNodes();
         for (int i = 0; i < children.getLength(); i++) {
             Node child = children.item(i);
             if (child.getNodeType() == Node.TEXT_NODE) {
                 buf.append(child.getNodeValue());
             }
         }
         String ret = buf.toString().trim();
         return !ret.isEmpty() ? ret : null;
     }
 
     private static List<Property> getProperties(Node node, EnumMapper enumMapper) {
         List<Property> ret = new ArrayList<Property>();
         NodeList children = node.getChildNodes();
         for (int i = 0; i < children.getLength(); i++) {
             Node child = children.item(i);
             if (child.getNodeType() == Node.ELEMENT_NODE &&
                 child.getNodeName().equals("Property")) {
                 ret.add(new Property(child, enumMapper));
             }
         }
         return ret;
     }
 
     private static Property getProperty(Node node, EnumMapper enumMapper) {
         return getProperties(node, enumMapper).get(0);
     }
 
     public static class Entity {
 
         private String type;
         private String value;
         private Map<String, Property> properties = new HashMap<String, Property>();
 
         public void setType(String type) {
             this.type = type;
         }
         public String getType() {
             return type;
         }
 
         public void setValue(String value) {
             this.value = value;
         }
         public String getValue() {
             return value;
         }
 
         public Map<String, Property> getProperties() {
             return properties;
         }
 
         public void addProperty(Property property) {
             properties.put(property.getName(), property);
         }
 
         public <T> T get(String name, Class<T> type) {
             return properties.get(name).getValue(type);
         }
 
         public Object get(String name, Class<?> current, Class<?> legacy) {
             return properties.get(name).getValue(current, legacy);
         }
 
         public String get(String name) {
             return properties.get(name).getValue(String.class);
         }
 
         public boolean isSet(String name) {
             return properties.get(name) != null && properties.get(name).isValueSet();
         }
     }
 
     public static class Property {
 
         private EnumMapper enumMapper;
         private String name;
         private String type;
         private Object value;
 
         public Property(Node prop, EnumMapper enumMapper) {
             this.enumMapper = enumMapper;
             NamedNodeMap attrs = prop.getAttributes();
             this.name = getAttr(attrs, "Name");
             if (this.name != null) {
                 this.name = this.name.toLowerCase();
             }
             this.type = getAttr(attrs, "Type");
             this.value = parseValue(prop);
         }
 
         public void setName(String name) {
             this.type = name;
         }
         public String getName() {
             return name;
         }
 
         public void setType(String type) {
             this.type = type;
         }
         public String getType() {
             return type;
         }
 
         public <T> void setValue(Class<T> type, T value) {
             this.value = type.cast(value);
         }
         public <T> T getValue(Class<T> type) {
             return type.cast(value);
         }
         public Object getValue(Class<?> current, Class<?> legacy) {
             if (current.isAssignableFrom(value.getClass())) {
                 return current.cast(value);
             } else if (legacy.isAssignableFrom(value.getClass())) {
                 return legacy.cast(value);
             } else {
                 return null;
             }
         }
         public boolean isValueSet() {
             return value != null;
         }
 
         private Object parseValue(Node node) {
             String text = getText(node);
 
             if (type.endsWith("[]")) {
                 List<Object> ret = new ArrayList<Object>();
                 for (Property prop : getProperties(node, enumMapper)) {
                     ret.add(prop.getValue(Object.class));
                 }
                 return ret;
             } else if (type.startsWith("System.Nullable") && text == null) {
                 return null;
             } else if (type.contains("System.Boolean")) {
                 return text.equals("True");
             } else if (type.contains("System.Int32") || type.contains("System.Int16")) {
                 return Integer.parseInt(text);
             } else if (type.contains("System.Int64")) {
                 return Long.parseLong(text);
             } else if (type.contains("System.Double")) {
                 return Double.parseDouble(text);
             } else if (type.contains("System.Decimal")) {
                 return new BigDecimal(text);
             } else if (enumMapper.isEnum(type)) {
                 return enumMapper.parseEnum(type, getProperty(node, enumMapper).getValue(Integer.class));
             } else if (type.contains("RhevmCmd.CLICompatibilityVersion")) {
                 return parseVersion(node, enumMapper);
             } else if (type.contains("RhevmCmd.CLIHostPowerManagement")) {
                 return new PowerManagement(node, enumMapper);
             } else if (type.startsWith("System.Collections.Generic.List")) {
                 // REVIST: ignoring for now
                 return null;
             } else if (type.contains("System.String") ||
                        type.contains("System.Guid") ||
                        type.contains("System.DateTime") ||
                        type.contains("System.TimeSpan")) {
                 return text;
             } else {
                 assert false : type;
                 return null;
             }
         }
 
         private static Version parseVersion(Node node, EnumMapper enumMapper) {
             Version version = new Version();
             for (Property prop : getProperties(node, enumMapper)) {
                 if (prop.getName().equals("major")) {
                     version.setMajor(prop.getValue(Integer.class));
                 }
                 if (prop.getName().equals("minor")) {
                     version.setMinor(prop.getValue(Integer.class));
                 }
             }
             return version;
         }
     }
 
     public static class PowerManagement {
         private boolean enabled;
         private String address;
         private String type;
         private String username;
         private String password;
         private int port;
         private int slot;
         private boolean secure;
         private String options;
 
         public PowerManagement(Node node, EnumMapper enumMapper) {
             for (Property prop : getProperties(node, enumMapper)) {
                 if (prop.getName().equals("enabled")) {
                     enabled = prop.getValue(Boolean.class);
                 }
                 if (prop.getName().equals("address")) {
                     address = prop.getValue(String.class);
                 }
                 if (prop.getName().equals("type")) {
                     type = prop.getValue(String.class);
                 }
                 if (prop.getName().equals("username")) {
                     username = prop.getValue(String.class);
                 }
                 if (prop.getName().equals("password")) {
                     password = prop.getValue(String.class);
                 }
                 if (prop.getName().equals("port") && prop.isValueSet()) {
                     port = prop.getValue(Integer.class);
                 }
                 if (prop.getName().equals("slot") && prop.isValueSet()) {
                     slot = prop.getValue(Integer.class);
                 }
                 if (prop.getName().equals("secure") && prop.isValueSet()) {
                     secure = prop.getValue(Boolean.class);
                 }
                 if (prop.getName().equals("options")) {
                     options = prop.getValue(String.class);
                 }
             }
         }
 
         public boolean getEnabled() {
             return enabled;
         }
         public void setEnabled(boolean enabled) {
             this.enabled = enabled;
         }
 
         public String getAddress() {
             return address;
         }
         public void setAddress(String address) {
             this.address = address;
         }
 
         public String getType() {
             return type;
         }
         public void setType(String type) {
             this.type = type;
         }
 
         public String getUsername() {
             return username;
         }
        public void setUsername(String username) {
             this.username = username;
         }
 
         public String getPassword() {
             return password;
         }
         public void setPassword(String password) {
             this.password = password;
         }
 
         public int getPort() {
             return port;
         }
         public void setPort(int port) {
             this.port = port;
         }
 
         public int getSlot() {
             return slot;
         }
         public void setSlot(int slot) {
             this.slot = slot;
         }
 
         public boolean getSecure() {
             return secure;
         }
         public void setSecure(boolean secure) {
             this.secure = secure;
         }
 
         public String getOptions() {
             return options;
         }
         public void setOptions(String options) {
             this.options = options;
         }
     }
 
     /* Only intended for unit tests */
     public static PowerShellParser newInstance() throws Exception {
         DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
         DocumentBuilder documentBuilder = factory.newDocumentBuilder();
 
         EnumMapper enumMapper = new EnumMapper();
 
         PowerShellParser p = new PowerShellParser();
         p.setDocumentBuilder(documentBuilder);
         p.setEnumMapper(enumMapper);
 
         return p;
     }
 }
