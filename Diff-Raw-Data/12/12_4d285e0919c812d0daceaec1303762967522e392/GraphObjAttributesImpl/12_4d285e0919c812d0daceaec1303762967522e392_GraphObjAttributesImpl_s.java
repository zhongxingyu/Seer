 package cytoscape.data;
 
 import cytoscape.data.attr.CountedIterator;
 import cytoscape.data.attr.MultiHashMap;
 import cytoscape.data.attr.MultiHashMapDefinition;
 import cytoscape.data.readers.TextFileReader;
 import cytoscape.data.readers.TextHttpReader;
 import cytoscape.data.readers.TextJarReader;
 import cytoscape.task.TaskMonitor;
 import cytoscape.util.Misc;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 
 public class GraphObjAttributesImpl implements GraphObjAttributes
 {
 
   private final HashMap m_localMap = new HashMap();
   private final CyAttributes m_cyAttrs;
   private TaskMonitor m_taskMonitor;
 
   public GraphObjAttributesImpl(CyAttributes cyAttrs)
   {
     m_cyAttrs = cyAttrs;
   }
 
  private Class deduceClass(String string)
   {
     String[] classNames = { //"java.net.URL",
                             "java.lang.Integer",
                             "java.lang.Double",
                             "java.lang.String" };
 
     for (int i = 0; i < classNames.length; i++) {
       try {
         Object obj = createInstanceFromString
           (Class.forName(classNames[i]), string);
         return obj.getClass(); }
       catch (Exception e) {
         ;
       }
     }
     return null;
   }
 
  private Object createInstanceFromString(Class requestedClass,
                                           String ctorArg) throws Exception
   {
     Class[] ctorArgsClasses = new Class[1];
     ctorArgsClasses[0] = Class.forName("java.lang.String");
     Object[] ctorArgs = new Object[1];
     ctorArgs[0] = new String(ctorArg);
     Constructor ctor = requestedClass.getConstructor(ctorArgsClasses);
     return ctor.newInstance(ctorArgs);
   }
 
   public boolean set(String attributeName, String id, Object value)
   {
 //     if (value instanceof Boolean) {
 //       m_cyAttrs.setAttribute(id, attributeName, (Boolean) value);
 //       return true; }
 //     else if (value instanceof Integer) {
 //       m_cyAttrs.setAttribute(id, attributeName, (Integer) value);
 //       return true; }
 //     else if (value instanceof Double) {
 //       m_cyAttrs.setAttribute(id, attributeName, (Double) value);
 //       return true; }
 //     else if (value instanceof String) {
 //       m_cyAttrs.setAttribute(id, attributeName, (String) value);
 //       return true; }
 //     throw new IllegalArgumentException
 //       ("this Object type is not supported - so sorry");
     try {
       List l = new ArrayList();
       l.add(value);
       m_cyAttrs.setAttributeList(id, attributeName, l);
       return true; }
     catch (IllegalArgumentException e) {
       if (m_cyAttrs.getType(attributeName) != CyAttributes.TYPE_UNDEFINED)
         throw e;
       HashMap stupidMap = (HashMap) m_stupidMaps.get(attributeName);
       if (stupidMap == null) {
         stupidMap = new HashMap();
         m_stupidMaps.put(attributeName, stupidMap); }
       stupidMap.put(id, value);
       return true; }
   }
 
   private final HashMap m_stupidMaps = new HashMap();
 
   public boolean append(String attributeName, String id, Object value)
   {
     List l = m_cyAttrs.getAttributeList(id, attributeName);
     if (l == null) { l = new ArrayList(); }
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
     case MultiHashMapDefinition.TYPE_STRING:
       return String.class;
     default:
       return null; }
   }
 
   public List getList(String attributeName, String id)
   {
     return m_cyAttrs.getAttributeList(id, attributeName);
   }
 
   public Object getValue(String attributeName, String id)
   {
     switch (m_cyAttrs.getType(attributeName)) {
     case CyAttributes.TYPE_BOOLEAN:
       return m_cyAttrs.getBooleanAttribute(id, attributeName);
     case CyAttributes.TYPE_INTEGER:
       return m_cyAttrs.getIntegerAttribute(id, attributeName);
     case CyAttributes.TYPE_FLOATING:
       return m_cyAttrs.getDoubleAttribute(id, attributeName);
     case CyAttributes.TYPE_STRING:
       return m_cyAttrs.getStringAttribute(id, attributeName);
     case CyAttributes.TYPE_SIMPLE_LIST:
       List l = m_cyAttrs.getAttributeList(id, attributeName);
       if (l.size() > 0) return l.get(0);
       else return null;
     case CyAttributes.TYPE_SIMPLE_MAP:
       return m_cyAttrs.getAttributeMap(id, attributeName);
     default:
       return null; }
   }
 
   public Object get(String attributeName, String id)
   {
     HashMap stupidMap;
     if ((stupidMap = (HashMap) m_stupidMaps.get(attributeName)) != null) {
       return stupidMap.get(id); }
     return getValue(attributeName, id);
   }
 
   public Double getDoubleValue(String attributeName, String id)
   {
     if (m_cyAttrs.getType(attributeName) == CyAttributes.TYPE_FLOATING) {
       return m_cyAttrs.getDoubleAttribute(id, attributeName); }
     else if (m_cyAttrs.getType(attributeName) ==
              CyAttributes.TYPE_SIMPLE_LIST &&
              m_cyAttrs.getMultiHashMapDefinition().
              getAttributeValueType(attributeName) ==
              MultiHashMapDefinition.TYPE_FLOATING_POINT) {
       List l = m_cyAttrs.getAttributeList(id, attributeName);
       if (l != null && l.size() > 0) {
         return (Double) l.get(0); } }
     return null;
   }
 
   public Integer getIntegerValue(String attributeName, String id)
   {
     if (m_cyAttrs.getType(attributeName) == CyAttributes.TYPE_INTEGER) {
       return m_cyAttrs.getIntegerAttribute(id, attributeName); }
     else if (m_cyAttrs.getType(attributeName) ==
              CyAttributes.TYPE_SIMPLE_LIST &&
              m_cyAttrs.getMultiHashMapDefinition().
              getAttributeValueType(attributeName) ==
              MultiHashMapDefinition.TYPE_INTEGER) {
       List l = m_cyAttrs.getAttributeList(id, attributeName);
       if (l != null && l.size() > 0) {
         return (Integer) l.get(0); } }
     return null;
   }
 
   public String getStringValue(String attributeName, String id)
   {
     if (m_cyAttrs.getType(attributeName) == CyAttributes.TYPE_STRING) {
       return m_cyAttrs.getStringAttribute(id, attributeName); }
     else if (m_cyAttrs.getType(attributeName) ==
              CyAttributes.TYPE_SIMPLE_LIST &&
              m_cyAttrs.getMultiHashMapDefinition().
              getAttributeValueType(attributeName) ==
              MultiHashMapDefinition.TYPE_STRING) {
       List l = m_cyAttrs.getAttributeList(id, attributeName);
       if (l != null && l.size() > 0) {
         return (String) l.get(0); } }
     return null;
   }
 
   public HashMap getAttribute(String attributeName)
   {
     final HashMap returnThis = new HashMap();
     final MultiHashMap mmap = m_cyAttrs.getMultiHashMap();
     final MultiHashMapDefinition mmapDef =
       m_cyAttrs.getMultiHashMapDefinition();
     if (mmapDef.getAttributeValueType(attributeName) != -1) {
       final Iterator objs = mmap.getObjectKeys(attributeName);
       while (objs.hasNext()) {
         final String obj = (String) objs.next();
         returnThis.put(obj, get(attributeName, obj)); } }
     return returnThis.size() == 0 ? null : returnThis;
   }
 
   public String[] getStringArrayValues(String attributeName, String id)
   {
     List l = m_cyAttrs.getAttributeList(id, attributeName);
     if (l == null) {
       final String strVal = m_cyAttrs.getStringAttribute(id, attributeName);
       if (strVal == null) return new String[0];
       else return new String[] { strVal }; }
     else {
       final String[] returnThis = new String[l.size()];
       final Object[] arr = l.toArray();
       System.arraycopy(arr, 0, returnThis, 0, arr.length);
       return returnThis; }
   }
 
   public String toString()
   {
     return "Greetings.  This is a human readable string.";
   }
 
   public boolean set(String graphObjName, HashMap bundle)
   {
     final Iterator keys = bundle.keySet().iterator();
     while (keys.hasNext()) {
       final String attrName = (String) keys.next();
       set(attrName, graphObjName, bundle.get(attrName)); }
     return true;
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
     Iterator keys = nameMapping.keySet().iterator();
     while (keys.hasNext()) {
       Object key = keys.next();
       addNameMapping((String) nameMapping.get(key), key); }
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
     List l = m_cyAttrs.getAttributeList(graphObjName, attributeName);
     if (l == null) return;
     l.remove(value);
     if (l.size() > 0) {
       m_cyAttrs.setAttributeList(graphObjName, attributeName, l); }
     else {
       m_cyAttrs.deleteAttribute(graphObjName, attributeName); }
   }
 
   public void readAttributesFromFile(File file) throws IOException
   {
     readAttributesFromFile(file.getPath());
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
     try {
       return m_cyAttrs.getMultiHashMap().
         getObjectKeys(attributeName).numRemaining(); }
     catch (RuntimeException e) { return 0; }
   }
 
   public String getCanonicalName(Object graphObj)
   {
 //     return ((giny.model.GraphObject) graphObj).getIdentifier();
 
 //     final Set entrySet = m_localMap.entrySet();
 //     final Iterator setIter = entrySet.iterator();
 //     Object key = null;
 //     while (setIter.hasNext()) {
 //       final Map.Entry entry = (Map.Entry) setIter.next();
 //       if (entry.getValue() == graphObj) {
 //         key = entry.getKey();
 //         break; } }
 //     return (String) key;
 
     return (String) m_localMap.get(graphObj);
   }
 
   public HashMap getAttributes(String canonicalName)
   {
     // Populate hashmap with single-valued attributes.
 
     final HashMap returnThis = new HashMap();
     final String[] attrNames = m_cyAttrs.getAttributeNames();
     for (int i = 0; i < attrNames.length; i++) {
       final byte type = m_cyAttrs.getType(attrNames[i]);
       if (m_cyAttrs.hasAttribute(canonicalName, attrNames[i])) {
         if (type == CyAttributes.TYPE_SIMPLE_LIST) {
           List l = m_cyAttrs.getAttributeList(canonicalName, attrNames[i]);
           if (l != null && l.size() > 0) {
             returnThis.put(attrNames[i], l.get(0)); } }
         else if (type == CyAttributes.TYPE_BOOLEAN) {
           returnThis.put
             (attrNames[i],
              m_cyAttrs.getBooleanAttribute(canonicalName, attrNames[i])); }
         else if (type == CyAttributes.TYPE_INTEGER) {
           returnThis.put
             (attrNames[i],
              m_cyAttrs.getIntegerAttribute(canonicalName, attrNames[i])); }
         else if (type == CyAttributes.TYPE_FLOATING) {
           returnThis.put
             (attrNames[i],
              m_cyAttrs.getDoubleAttribute(canonicalName, attrNames[i])); }
         else if (type == CyAttributes.TYPE_STRING) {
           returnThis.put
             (attrNames[i],
              m_cyAttrs.getStringAttribute(canonicalName, attrNames[i])); } } }
     return returnThis;
   }
 
   public void addNameMapping(String canonicalName, Object graphObject)
   {
     m_localMap.put(graphObject, canonicalName);
   }
 
   public Object getGraphObject(String canonicalName)
   {
 //     return m_localMap.get(canonicalName);
 
     final Set entrySet = m_localMap.entrySet();
     final Iterator setIter = entrySet.iterator();
     Object key = null;
     while (setIter.hasNext()) {
       final Map.Entry entry = (Map.Entry) setIter.next();
       if (entry.getValue().equals(canonicalName)) {
         key = entry.getKey();
         break; } }
     return key;
   }
 
   public String[] getObjectNames(String attributeName)
   {
     final MultiHashMap mmap = m_cyAttrs.getMultiHashMap();
     final CountedIterator keys = mmap.getObjectKeys(attributeName);
     final String[] returnThis = new String[keys.numRemaining()];
     int inx = 0;
     while (keys.hasNext()) {
       returnThis[inx++] = (String) keys.next(); }
     return returnThis;
   }
 
   public void removeNameMapping(String canonicalName)
   {
 //     m_localMap.remove(canonicalName);
 
     final Set entrySet = m_localMap.entrySet();
     final Iterator setIter = entrySet.iterator();
     Object key = null;
     while (setIter.hasNext()) {
       final Map.Entry entry = (Map.Entry) setIter.next();
       if (entry.getValue().equals(canonicalName)) {
         key = entry.getKey();
         break; } }
     if (key != null) m_localMap.remove(key);
   }
 
   public boolean setClass(String attributeName, Class attributeClass)
   {
     final MultiHashMapDefinition mmapDef =
       m_cyAttrs.getMultiHashMapDefinition();
     try {
       if (attributeClass.equals(Boolean.class)) {
         mmapDef.defineAttribute
           (attributeName,
            MultiHashMapDefinition.TYPE_BOOLEAN,
            new byte[] { MultiHashMapDefinition.TYPE_INTEGER }); }
       else if (attributeClass.equals(Integer.class)) {
         mmapDef.defineAttribute
           (attributeName,
            MultiHashMapDefinition.TYPE_INTEGER,
            new byte[] { MultiHashMapDefinition.TYPE_INTEGER }); }
       else if (attributeClass.equals(Double.class)) {
         mmapDef.defineAttribute
           (attributeName,
            MultiHashMapDefinition.TYPE_FLOATING_POINT,
            new byte[] { MultiHashMapDefinition.TYPE_INTEGER }); }
       else if (attributeClass.equals(String.class)) {
         mmapDef.defineAttribute
           (attributeName,
            MultiHashMapDefinition.TYPE_STRING,
            new byte[] { MultiHashMapDefinition.TYPE_INTEGER }); }
       else return false; }
     catch (Exception e) { return false; }
     return true;        
   }
 
   public void removeObjectMapping(Object graphObj)
   {
 //     final Set entrySet = m_localMap.entrySet();
 //     final Iterator setIter = entrySet.iterator();
 //     Object key = null;
 //     while (setIter.hasNext()) {
 //       final Map.Entry entry = (Map.Entry) setIter.next();
 //       if (entry.getValue() == graphObj) {
 //         key = entry.getKey();
 //         break; } }
 //     if (key != null) m_localMap.remove(key);
 
     m_localMap.remove(graphObj);
   }
 
   public Object[] getArrayValues(String attributeName,
                                  String graphObjectName)
   {
     // If this attributeName is List, convert to array.
     // Otherwise return new Object[0].
     if (m_cyAttrs.getType(attributeName) != CyAttributes.TYPE_SIMPLE_LIST) {
       return new Object[0]; }
     return m_cyAttrs.getAttributeList(graphObjectName,
                                       attributeName).toArray();
   }
 
   public void readAttributesFromFile(String filename) throws IOException
   {
 //     // I may only want to read and write simple values or maybe even lists.
 //     //
 //     // CyAttributesReader
 //     //   public static void loadAttributes(CyAttributes, InputStream)
 //     //
 //     // CyAttributesWriter
 //     //   public static void writeAttributes(CyAttributes, OutputStream)
 //     //
 //     // AttributesSaverDialog - split apart
 //     try {
 //       FileInputStream fin = new FileInputStream(filename);
 //       CyAttributesReader.loadAttributes(m_cyAttrs, fin); }
 //     catch (IOException e) { }
 
     if (m_taskMonitor != null) {
       m_taskMonitor.setStatus("Importing Attributes..."); }
 
     String rawText;
     if (filename.trim().startsWith("jar://")) {
       TextJarReader reader = new TextJarReader(filename);
       reader.read();
       rawText = reader.getText();
     } else if ( filename.trim().startsWith("http://") || filename.trim().startsWith( "file://") ) {
       try {
         TextHttpReader reader = new TextHttpReader( filename );
         rawText = reader.getText();
       } catch ( Exception e ) {
         throw new IOException( e.getMessage() );
       } // end of try-catch
 
     } else {
       TextFileReader reader = new TextFileReader(filename);
       reader.read();
       rawText = reader.getText();
     }
 
     StringTokenizer lineTokenizer = new StringTokenizer(rawText, "\n");
 
     int lineNumber = 0;
     if (lineTokenizer.countTokens() < 2) {
       throw new IllegalArgumentException
         (filename + " must have at least 2 lines");
     }
 
     String attributeName = processFileHeader
       (lineTokenizer.nextToken().trim());
     boolean extractingFirstValue = true;
 
     int numTokens = lineTokenizer.countTokens();
 
     while (lineTokenizer.hasMoreElements()) {
       String newLine = (String) lineTokenizer.nextElement();
 
       //  Track Progress
       if (m_taskMonitor != null) {
         double percent = ((double) lineNumber / numTokens) * 100.0;
         m_taskMonitor.setPercentCompleted((int) percent);
       }
 
       if (newLine.trim().startsWith("#")) continue;
       lineNumber++;
       StringTokenizer strtok2 = new StringTokenizer(newLine, "=");
       if (strtok2.countTokens() < 2) {
         throw new IOException
           ("Cannot parse line number " + lineNumber
            + ":\n\t" + newLine + ".  This may not be a valid "
            + "attributes file.");
       }
       String graphObjectName = strtok2.nextToken().trim();
 
       String rawString = newLine.substring(newLine.indexOf("=") + 1).trim();
       String[] rawList;
       boolean isList = false;
       if (Misc.isList(rawString, "(", ")", "::")) {
         rawList = Misc.parseList(rawString, "(", ")", "::");
         isList = true;
       } else {
         rawList = new String[1];
         rawList[0] = rawString;
       }
       if (extractingFirstValue && getClass(attributeName) == null) {
         extractingFirstValue = false;  // henceforth
         Class deducedClass = deduceClass(rawList[0]);
         setClass(attributeName, deducedClass); // ***** Could fail ******* //
       }
       Object[] objs = new Object[rawList.length];
       Class stringClass = (new String()).getClass();
 
       if (getClass(attributeName).equals(stringClass)) {
         for (int i = 0; i < rawList.length; i++) {
           rawList[i] = rawList[i].replaceAll("\\\\n", "\n");
         }
       }
 
       for (int i = 0; i < rawList.length; i++) {
         try {
           objs[i] = createInstanceFromString
             (getClass(attributeName), rawList[i]);
           if (isList) {
             append(attributeName, graphObjectName, objs[i]);
           } else {
             set(attributeName, graphObjectName, objs[i]);
           }
         } catch (Exception e) {
           throw new IllegalArgumentException
             ("Could not create an instance of " +
              getClass(attributeName) + " from " + rawList[i]);
         }
       }
     }
 
     //  Inform User of What Just Happened.
     if (m_taskMonitor != null) {
       File  file = new File (filename);
       m_taskMonitor.setPercentCompleted (100);
       StringBuffer sb = new StringBuffer();
       sb.append("Succesfully loaded attributes from:  "
                 + file.getName());
       sb.append("\n\nAttribute Name:  " + attributeName);
       sb.append("\n\nNumber of Attributes:  " + lineNumber);
       m_taskMonitor.setStatus(sb.toString());
     }
   }
 
   public HashMap getNameMap()
   {
     // Returns reference to local obj->name hashmap.
     return m_localMap;
   }
 
   public Object[] getUniqueValues(String attributeName)
   {
     final HashMap dupsFilter = new HashMap();
     final MultiHashMapDefinition mmapDef =
       m_cyAttrs.getMultiHashMapDefinition();
     final MultiHashMap mmap = m_cyAttrs.getMultiHashMap();
     final byte type = m_cyAttrs.getType(attributeName);
     if (type == CyAttributes.TYPE_SIMPLE_LIST) {
       final Iterator objs = mmap.getObjectKeys(attributeName);
       while (objs.hasNext()) {
         final String obj = (String) objs.next();
         final List l = m_cyAttrs.getAttributeList(obj, attributeName);
         final Iterator liter = l.iterator();
         while (liter.hasNext()) {
           final Object val = liter.next();
           dupsFilter.put(val, val); } } }
     else if (type == CyAttributes.TYPE_BOOLEAN ||
              type == CyAttributes.TYPE_FLOATING ||
              type == CyAttributes.TYPE_INTEGER ||
              type == CyAttributes.TYPE_STRING) {
       final Iterator objs = mmap.getObjectKeys(attributeName);
       while (objs.hasNext()) {
         final String obj = (String) objs.next();
         final Object val = mmap.getAttributeValue(obj, attributeName, null);
         dupsFilter.put(val, val); } }
     else { return new Object[0]; }
     final Object[] returnThis = new Object[dupsFilter.size()];
     Iterator uniqueIter = dupsFilter.keySet().iterator();
     int inx = 0;
     while (uniqueIter.hasNext()) { returnThis[inx++] = uniqueIter.next(); }
     return returnThis;
   }
 
   public String[] getUniqueStringValues(String attributeName)
   {
     final HashMap dupsFilter = new HashMap();
     final MultiHashMapDefinition mmapDef =
       m_cyAttrs.getMultiHashMapDefinition();
     final MultiHashMap mmap = m_cyAttrs.getMultiHashMap();
     final byte type = m_cyAttrs.getType(attributeName);
     if (type == CyAttributes.TYPE_SIMPLE_LIST &&
         mmapDef.getAttributeValueType(attributeName) ==
         MultiHashMapDefinition.TYPE_STRING) {
       final Iterator objs = mmap.getObjectKeys(attributeName);
       while (objs.hasNext()) {
         final String obj = (String) objs.next();
         final List l = m_cyAttrs.getAttributeList(obj, attributeName);
         final Iterator liter = l.iterator();
         while (liter.hasNext()) {
           final Object val = liter.next();
           dupsFilter.put(val, val); } } }
     else if (type == CyAttributes.TYPE_STRING) {
       final Iterator objs = mmap.getObjectKeys(attributeName);
       while (objs.hasNext()) {
         final String obj = (String) objs.next();
         final Object val = mmap.getAttributeValue(obj, attributeName, null);
         dupsFilter.put(val, val); } }
     else { return new String[0]; }
     final String[] returnThis = new String[dupsFilter.size()];
     Iterator uniqueIter = dupsFilter.keySet().iterator();
     int inx = 0;
     while (uniqueIter.hasNext()) {
       returnThis[inx++] = (String) uniqueIter.next(); }
     return returnThis;
   }
 
   public String processFileHeader(String text)
   {
     // Copy old code from old GraphObjAttributes.
     String attributeName = "";
     String attributeCategory = DEFAULT_CATEGORY;
     Class  attributeClass = null;
 
     if (text.indexOf ("(") < 0)
       attributeName = text.trim ();
     else {
       StringTokenizer strtok = new StringTokenizer (text, "(");
       attributeName = strtok.nextToken ().trim();
       while (strtok.hasMoreElements ()) {
         String rawValuePair = strtok.nextToken().trim();
         if (!rawValuePair.endsWith (")")) continue;
         String valuePair = rawValuePair.substring (0,rawValuePair.length()-1);
         int locationOfEqualSign = valuePair.indexOf ("=");
         if (locationOfEqualSign < 0) continue;
         if (valuePair.endsWith ("=")) continue;
         StringTokenizer strtok2 = new StringTokenizer (valuePair, "=");
         String name = strtok2.nextToken ();
         String value = strtok2.nextToken ();
         if (name.equals ("category"))
           attributeCategory = value;
         if (name.equals ("class")) {
           try {
             attributeClass = Class.forName (value);
           }
           catch (ClassNotFoundException ignore) {;}
         } // if name == 'class'
       } // while strtok
     } // else: at least one (x=y) found
     setCategory (attributeName, attributeCategory);
     setClass (attributeName, attributeClass); // ******* Could fail *********
     return attributeName;
   }
 
   public void add(GraphObjAttributes attributes) {}
 
   public boolean add(String attributeName, String id, Object value)
   {
     return append(attributeName, id, value);
   }
 
   public boolean add(String attributeName, String id, double value)
   {
     return append(attributeName, id, new Double(value));
   }
 
   public boolean add(String graphObjectName, HashMap bundle)
   {
     return false;
   }
 
   private final HashMap m_categoryMap = new HashMap();
 
   public void setCategory(String attributeName, String newValue)
   {
     m_categoryMap.put(attributeName, newValue);
   }
 
   public String getCategory(String attributeName)
   {
     final String returnThis =  (String) m_categoryMap.get(attributeName);
 //     if (returnThis == null) { return GraphObjAttributes.DEFAULT_CATEGORY; }
     return returnThis;
   }
 }
