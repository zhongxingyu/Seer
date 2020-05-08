 package com.runwaysdk.eclipse.plugin.schema.runwayxml;
 
 import java.util.HashMap;
 import java.util.Map;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NamedNodeMap;
 
 import com.runwaysdk.eclipse.plugin.runway.MetaData;
 
 abstract public class XMLMetadata
 {
   private Map<String, String> xmlAttributes = new HashMap<String, String>();
 
   private MetaData            metadata;
 
   private Element             doItExport, undoItExport;
 
   public static final int     UNMODIFIED    = 0;
 
   public static final int     CREATE        = 1;
 
   public static final int     UPDATE        = 2;
 
   public static final int     DELETE        = 3;
 
   private int                 crudFlag      = UNMODIFIED;
 
   public XMLMetadata()
   {
 
   }
 
   public void setXMLAttribute(String key, String value)
   {
     xmlAttributes.put(key, value);
   }
 
   public String getXMLAttribute(String key)
   {
     return xmlAttributes.get(key);
   }
 
   public MetaData readFromXML(NamedNodeMap nodeMap)
   {
     MetaData metadata = getMetadata();
 
     // TODO : set attributes
 
     return metadata;
   }
 
   public Element writeDoItXML(Document dom)
   {
     Element xml = doItExport;
 
     Object[] keys = xmlAttributes.keySet().toArray();
     for (int i = 0; i < keys.length; ++i)
     {
       String key = (String) keys[i];
       xml.setAttribute(key, xmlAttributes.get(key));
     }
 
     return xml;
   }
 
   public Element writeUndoItXML(Document dom)
   {
     Element xml = undoItExport;
 
     Object[] keys = xmlAttributes.keySet().toArray();
     for (int i = 0; i < keys.length; ++i)
     {
       String key = (String) keys[i];
       if (key == "name") {
     	xml.setAttribute("key", xmlAttributes.get(key));
    	xml.setAttribute("type", "com.runwaysdk.system.metadata." + doItExport.getNodeName());
     	break;
       }
     }
 
     return xml;
   }
 
   public MetaData getMetadata()
   {
     return metadata;
   }
 
   public void setMetadata(MetaData mdBiz)
   {
     this.metadata = mdBiz;
   }
 
   public int getCrudFlag()
   {
     return crudFlag;
   }
 
   public void setCrudFlag(int crudFlag)
   {
     this.crudFlag = crudFlag;
   }
 
   public Element getDoItExport()
   {
     return doItExport;
   }
 
   public void setDoItExport(Element doItExport)
   {
     this.doItExport = doItExport;
   }
 
   public Element getUndoItExport()
   {
     return undoItExport;
   }
 
   public void setUndoItExport(Element undoItExport)
   {
     this.undoItExport = undoItExport;
   }
 
 }
