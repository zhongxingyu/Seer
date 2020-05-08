 package cytoscape.data;
 
 import cytoscape.task.TaskMonitor;
 import java.io.File;
 import java.util.HashMap;
 import java.util.List;
 
 public class GraphObjAttributesImpl implements GraphObjAttributes
 {
 
   private final CyAttributes m_cyAttrs;
   private TaskMonitor m_taskMonitor;
 
   public GraphObjAttributesImpl(CyAttributes cyAttrs)
   {
     m_cyAttrs = cyAttrs;
   }
 
   public boolean set(String attributeName, String id, Object value)
   {
     if (value instanceof Boolean) {
       m_cyAttrs.setAttribute(id, attributeName, (Boolean) value);
       return true; }
     else if (value instanceof Integer) {
       m_cyAttrs.setAttribute(id, attributeName, (Integer) value);
       return true; }
     else if (value instanceof Double) {
       m_cyAttrs.setAttribute(id, attributeName, (Double) value);
       return true; }
     else if (value instanceof String) {
       m_cyAttrs.setAttribute(id, attributeName, (String) value);
       return true; }
     throw new IllegalArgumentException
       ("this Object type is not supported - so sorry");
   }
 
   public boolean append(String attributeName, String id, Object value)
   {
     List l = m_cyAttrs.getAttributeList(id, attributeName);
     l.add(value);
     m_cyAttrs.setAttributeList(id, attributeName, l);
     return true;
   }
 
   public boolean set(String attributeName, String id, double value)
   {
     return set(attributeName, id, new Double(value));
   }
 
   public void setTaskMonitor(TaskMonitor taskMonitor)
   {
     m_taskMonitor = taskMonitor;
   }
 
   public int numberOfAttributes()
   {
     return m_cyAttrs.getMultiHashMapDefinition().getDefinedAttributes().
       numRemaining();
   }
 
   public String[] getAttributeNames()
   {
     return m_cyAttrs.getAttributeNames();
   }
 
   public boolean hasAttribute(String attributeName)
   {
     return m_cyAttrs.getType(attributeName) != CyAttributes.TYPE_UNDEFINED;
   }
 
   public boolean hasAttribute(String attributeName, String id)
   {
     return m_cyAttrs.hasAttribute(id, attributeName);
   }
 
   public void deleteAttribute(String attributeName)
   {
     m_cyAttrs.deleteAttribute(attributeName);
   }
 
   public void deleteAttribute(String attributeName, String id)
   {
     m_cyAttrs.deleteAttribute(id, attributeName);
   }
 
   public Class getClass(String attributeName)
   {
     switch (m_cyAttrs.getMultiHashMapDefinition().
             getAttributeValueType(attributeName)) {
     case MultiHashMapDefinition.TYPE_BOOLEAN:
       return Boolean.class;
     case MultiHashMapDefinition.TYPE_INTEGER:
       return Integer.class;
     case MultiHashMapDefinition.TYPE_FLOATING_POINT:
       return Double.class;
     default: // case MultiHashMapDefinition.TYPE_STRING:
       return String.class; }
   }
 
   public List getList(String attributeName, String id)
   {
     return null;
   }
 
   public Object getValue(String attributeName, String id)
   {
     return null;
   }
 
   public Object get(String attributeName, String id)
   {
     return null;
   }
 
   public Double getDoubleValue(String attributeName, String id)
   {
     return null;
   }
 
   public Integer getIntegerValue(String attributeName, String id)
   {
     return null;
   }
 
   public String getStringValue(String attributeName, String id)
   {
     return null;
   }
 
   public HashMap getAttribute(String attributeName)
   {
     return null;
   }
 
   public String[] getStringArrayValues(String attributeName, String id)
   {
     return null;
   }
 
   public String toString()
   {
     return null;
   }
 
   public boolean set(String graphObjName, HashMap bundle)
   {
     return false;
   }
 
   public void clearNameMap()
   {
   }
 
   public void clearObjectMap()
   {
   }
 
   public HashMap getClassMap()
   {
     return null;
   }
 
   public void addClassMap(HashMap newClassMap)
   {
   }
 
   public HashMap getObjectMap()
   {
     return null;
   }
 
   public void addNameMap(HashMap nameMapping)
   {
   }
 
   public void addObjectMap(HashMap objectMapping)
   {
   }
 
   public void set(GraphObjAttributes attributes)
   {
   }
 
   public void deleteAttributeValue(String attributeName,
                                    String graphObjName, Object value)
   {
   }
 
   public void readAttributesFromFile(File file)
   {
   }
 
   public HashMap getSummary()
   {
     return null;
   }
 
   public int countIdentical(String graphObjName)
   {
     return 0;
   }
 
   public int getObjectCount(String attributeName)
   {
     return 0;
   }
 
   public String getCanonicalName(Object graphObj)
   {
     return null;
   }
 
   public HashMap getAttributes(String canonicalName)
   {
     return null;
   }
 
   public void addNameMapping(String canonicalName, Object graphObject)
   {
   }
 
   public Object getGraphObject(String canonicalName)
   {
     return null;
   }
 
   public String[] getObjectNames(String attributeName)
   {
     return null;
   }
 
   public void removeNameMapping(String canonicalName)
   {
   }
 
   public boolean setClass(String attributeName, Class attributeClass)
   {
     return false;
   }
 
   public void removeObjectMapping(Object graphObj)
   {
   }
 
   public Object[] getArrayValues(String attributeName,
                                  String graphObjectName)
   {
     return null;
   }
 
   public void readAttributesFromFile(String filename)
   {
   }
 
   public HashMap getNameMap()
   {
     return null;
   }
 
   public Object[] getUniqueValues(String attributeName)
   {
     return null;
   }
 
   public String[] getUniqueStringValues(String attributeName)
   {
     return null;
   }
 
   public String processFileHeader(String text)
   {
     return null;
   }
 
 }
