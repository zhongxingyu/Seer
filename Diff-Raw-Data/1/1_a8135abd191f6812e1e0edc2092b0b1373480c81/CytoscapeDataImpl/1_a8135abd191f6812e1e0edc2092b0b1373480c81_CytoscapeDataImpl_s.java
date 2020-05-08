 package cytoscape.data;
 
 import java.util.*;
 import java.io.*;
 import java.lang.reflect.*;
 
 import cytoscape.Cytoscape;
 import cytoscape.data.readers.*;
 import cytoscape.util.Misc;
 import cytoscape.task.TaskMonitor;
 
 import cytoscape.data.attr.CyData;
 import cytoscape.data.attr.CyDataDefinition;
 import cytoscape.data.attr.CyDataDefinitionListener;
 import cytoscape.data.attr.CyDataListener;
 import cytoscape.data.attr.CountedIterator;
 import cytoscape.data.attr.util.ExtensibleCyDataModel;
 import cytoscape.data.attr.util.CyDataHelpers;
 
 import giny.model.GraphObject;
 
 public class CytoscapeDataImpl 
   extends ExtensibleCyDataModel
   implements CytoscapeData {
 
   private TaskMonitor taskMonitor;
 
   private Object objectModel;
   private cytoscape.data.attr.CyData data;
   private CyDataDefinition definition;
 
   public final byte TYPE_BOOLEAN = 1;
   public final byte TYPE_FLOATING_POINT = 2;
   public final byte TYPE_INTEGER = 3;
   public final byte TYPE_STRING = 4;
 
   public static final byte NODES = 1;
   public static final byte EDGES = 2;
     /**
      * Used to specify a CytoscapeData object that is associated with
      * objects other than Nodes or Edges (e.g., HyperEdges):
      */
   public static final byte OTHER = 3;
 
   private byte type;
 
   private static final String LIST = "LIST";
   private static final Object[] LIST_KEY = {"LIST"};
   private static final Object[] ZERO = {Integer.toString(0)};
 
   private Map labelMap;
   Vector listeners = new Vector();
 
   public CytoscapeDataImpl ( byte type) {
     super();
     this.type = type;
     //objectModel = CyDataFactory.instantiateDataModel();
     data = ( cytoscape.data.attr.CyData )this;
     definition = ( CyDataDefinition )this;
 
     labelMap = new HashMap();
 
   }
 
   public void applyLabel ( String attributeName, String labelName ) {
 
     if ( labelMap.get( labelName ) == null ) {
       Set atts = new HashSet();
       labelMap.put( labelName, atts );
     }
 
     Set atts = ( Set )labelMap.get( labelName );
 
     atts.add( attributeName );
     notifyListeners();
   }
 
   public void removeLabel ( String attributeName, String labelName ) {
     if ( labelMap.get( labelName ) == null ) 
       return;
 
     Set atts = ( Set )labelMap.get( labelName );
 
     atts.remove( attributeName );
     notifyListeners();
   }
 
   public Set getAttributesByLabel ( String labelName ) {
      if ( labelMap.get( labelName ) == null ) {
        return null;
      }
 
      return ( Set )labelMap.get( labelName );
   }
 
   public Set getLabelNames () {
     return labelMap.keySet();
   }
 
   /**
    * SLOW
    */
   public Set getLabelsOfAttribute ( String attributeName ) {
     Set new_set = new HashSet();
     for ( Iterator i = labelMap.keySet().iterator(); i.hasNext(); ) {
       String label = (String)i.next();
       if ( labelMap.get( label ) == null ) 
         continue;
       Set atts = ( Set )labelMap.get( label );
       if ( atts.contains( attributeName ) )
         new_set.add( label );
     }
     return new_set;
   }
   
   public void addCytoscapeDataListener ( CytoscapeDataListener listener ) {
     listeners.add( listener );
   }
 
   public void removeCytoscapeDataListener ( CytoscapeDataListener listener ) {
     listeners.remove( listener );
   }
 
   public void notifyListeners (){
     for(Iterator listenIt = listeners.iterator();listenIt.hasNext();){
       ((CytoscapeDataListener)listenIt.next()).labelStateChange();
     }
   }
 
 
   ////////////////////////////////////////
   ////////////////////////////////////////
   // CytoscapeData Implementation
  
   public Object objectAsType ( Object value, byte type ) {
    
     if ( value == null )
       return null;
 
     if ( type == TYPE_BOOLEAN && value instanceof Boolean ) {
       return value;
     } else if ( type == TYPE_FLOATING_POINT && value instanceof Double ) {
       return value;
     } else if ( type == TYPE_INTEGER && value instanceof Integer ) {
       return value;
     } else if ( type == TYPE_STRING && value instanceof String ) {
       return value;
     } else {
       return value.toString();
     }
   }
 
 //   /**
 //    * If we are guessing, first try to cast as a Double. 
 //    * Then Boolean, then default to String. We never guess Integer.
 //    */
 //   public byte wildGuessAndDefineObjectType ( Object value, String attributeName ) {
    
 //     Object attribute;
 //     // Test for Double
 //     try { 
 //       attribute = new Double( value.toString() );
 //       defineAttribute( attributeName,
 //                        TYPE_FLOATING_POINT,
 //                        new byte[] {TYPE_STRING} );
 //       return TYPE_FLOATING_POINT;
 //     } catch ( Exception e ) {}
     
 //     // Test for Boolean
 //     try { 
 //       if ( value.toString().equals("true") || 
 //            value.toString().equals("false") ) {
 //         defineAttribute( attributeName,
 //                          TYPE_BOOLEAN,
 //                          new byte[] {TYPE_STRING} );
 //         return TYPE_BOOLEAN;
 //       }
 //     } catch ( Exception e ) {}
     
 //     // Default is String
 //     defineAttribute( attributeName,
 //                      TYPE_STRING,
 //                      new byte[] {TYPE_STRING} );
 //     return TYPE_STRING;
     
 //   }
   
 
 
 
   /**
    * If we are guessing, first try to cast as a Double. 
    * Then Boolean, then default to String. We never guess Integer.
    */
   public byte guessAndDefineObjectType ( Object value, String attributeName ) {
    
 
 
     if ( value instanceof Boolean ) {
       defineAttribute( attributeName,
                        TYPE_BOOLEAN,
                        new byte[] {TYPE_STRING} );
       return TYPE_BOOLEAN;
     } else if ( value instanceof String ) {
       defineAttribute( attributeName,
                        TYPE_STRING,
                        new byte[] {TYPE_STRING} );
       return TYPE_STRING;
     }else if ( value instanceof Double ) {
       defineAttribute( attributeName,
                        TYPE_FLOATING_POINT,
                        new byte[] {TYPE_STRING} );
       return TYPE_FLOATING_POINT;
     }else if ( value instanceof Integer ) {
       defineAttribute( attributeName,
                        TYPE_INTEGER,
                        new byte[] {TYPE_STRING} );
       return TYPE_INTEGER;
     } else {
       Object attribute;
       // Test for Double
       try { 
         attribute = new Double( value.toString() );
         defineAttribute( attributeName,
                          TYPE_FLOATING_POINT,
                          new byte[] {TYPE_STRING} );
         return TYPE_FLOATING_POINT;
       } catch ( Exception e ) {}
       
       // Test for Boolean
       try { 
         if ( value.toString().equals("true") || 
              value.toString().equals("false") ) {
           defineAttribute( attributeName,
                            TYPE_BOOLEAN,
                            new byte[] {TYPE_STRING} );
           return TYPE_BOOLEAN;
         }
       } catch ( Exception e ) {}
       
       // Default is String
       defineAttribute( attributeName,
                        TYPE_STRING,
                        new byte[] {TYPE_STRING} );
       return TYPE_STRING;
       
     }
   }
 
 
   ////////////////////////////////////////
   //SECTION 1: 1 value per attribute
 
   /**
    * Preset the type of value for a given attribute.  Any values that 
    * do not conform to this type will throw an error. Once a type is set, 
    * any attempt to change the value will result in an  error.
    * @param attribute the name of the attribute
    * @param type one of TYPE_BOOLEAN, TYPE_FLOATING_POINT, TYPE_INTEGER, TYPE_STRING
    */
   public void initializeAttributeType ( String attribute,
                                         byte type ) {
 
     defineAttribute( attribute,
                      type,
                      new byte[] {TYPE_STRING} );
                      
   }
 
 
 
   /**
    * @param identifier the unique identifier of the GraphObject
    * @param attribute the name of the attribute
    * @param value the value for this identifier attribute pair
    * @return the previous value for this attribute, or null if empty 
    */
   public Object setAttributeValue ( String identifier,
                                     String attribute,
                                     Object value ) {
     byte set = definition.getAttributeValueType( attribute );
     if ( set == -1 ) {
       // not set, guess the type
       set = guessAndDefineObjectType( value, attribute );
     }
     try { 
       return setAttributeValue( identifier,
                                 attribute,
                                 objectAsType( value, set ),
                                 ZERO );
       } catch ( Exception e ) {
         //System.out.println( "set is failing: "+attribute+" "+identifier+ "  "+value );
         e.printStackTrace();
         return null;
       }
   }
 
       
  
   /**
    * @param identifier the unique identifier of the GraphObject
    * @param attribute the name of the attribute
    * @return the value stored for this attribute and object
    */
   public Object getAttributeValue ( String identifier,
                                     String attribute ) {
 
     try {
       return getAttributeValue( identifier,
                                      attribute,
                                      ZERO );
     } catch ( Exception e ) {
       return null;
     }
   }
 
  /**
    * @param identifier the unique identifier of the GraphObject
    * @param attribute the name of the attribute
    * @return the value removed for this attribute and object
    */
   public Object deleteAttributeValue ( String identifier,
                                        String attribute ) {
     try {
       return removeAttributeValue( identifier,
                                    attribute,
                                    ZERO );
     } catch ( Exception e ) {
       return null;
     }
   }
 
 
   ////////////////////////////////////////
   // SECTION 2: List attributes
 
   /**
    * Using "Add" means that you adding this value to the 
    * end of the list of values, similar to a java.util.List
    * @param identifier the unique identifier of the GraphObject
    * @param attribute the name of the attribute
    * @param value the value for this identifier attribute pair, that
    *              will be added to the end of the list
    * @return the new length of the list of values for this identifier
    *          attribute pair
    */
   public int addAttributeListValue ( String identifier,
                                      String attribute,
                                      Object value ) {
 
     byte set = definition.getAttributeValueType( attribute );
     if ( set == -1 ) {
       // first we need the type
       set = guessAndDefineObjectType( value, attribute );
     } 
     
     // first find the end of the list
     int span = getAttributeKeyspan( identifier, 
                                     attribute,
                                     null).numRemaining();
     try {
       // now insert the current_val into the end of the list
       setAttributeValue( identifier,
                          attribute,
                          objectAsType( value, set ),
                          new Object[] {Integer.toString(span)} );
     } catch ( Exception e ) {
       return -1;
     }
     return span++;
   }
 
 
   /**
    * @param identifier the unique identifier of the GraphObject
    * @param attribute the name of the attribute
    * @return the number of values for this list of values
    *         for this attribute 
    */
   public int getAttributeValueListCount ( String identifier,
                                           String attribute ) {
     return getAttributeKeyspan( identifier, 
                                 attribute,
                                 null).numRemaining();
   }
 
 
   /**
    * @param identifier the unique identifier of the GraphObject
    * @param attribute the name of the attribute
    * @param position the position in the list
    * @return the value stored for this attribute and object at this position
    */
   public Object getAttributeValueListElement ( String identifier,
                                                String attribute,
                                                int position ) {
  
     if ( position >= getAttributeValueListCount( identifier, attribute ) )
       return null;
 
     try {
       return getAttributeValue( identifier,
                                 attribute,
                                 new Object[] { Integer.toString(position) } );
     } catch ( Exception e ) {
       return null;
     }
 
   }
 
   /**
    * Returns the values as a new list
    * @param identifier the unique identifier of the GraphObject
    * @param attribute the name of the attribute
    * @return all of the values for this  identifier attribute pair
    *         as a list.
    */
   public List getAttributeValueList ( String identifier,
                                       String attribute ) {
 
     Set keys = getAttributeKeySet( identifier, attribute );
     List arraylist = new ArrayList(keys.size());
     for ( Iterator i = keys.iterator(); i.hasNext(); ) {
       arraylist.add( data.getAttributeValue( identifier,
                                              attribute,
                                              new Object[] { (String)i.next() } ) );
     }
     return arraylist;
   }
 
 
   /**
    * @param identifier the unique identifier of the GraphObject
    * @param attribute the name of the attribute
    * @param position the position in the list
    * @return the value removed for this attribute and object at this position
    */
   public Object deleteAttributeListValue ( String identifier,
                                            String attribute,
                                            int position ) {
 
     if ( position >= getAttributeValueListCount( identifier, attribute ) )
       return null;
 
     try {
       return removeAttributeValue( identifier,
                                    attribute,
                                    new Object[] { Integer.toString(position) } );
     } catch ( Exception e ) {
       return null;
     }
 
 
   }
 
   ////////////////////////////////////////
   // SECTION 3: Hash Attributes
 
 
   /**
    * @param identifier the unique identifier of the GraphObject
    * @param attribute the name of the attribute
    * @param key the user-given key to access this value
    * @param value the value stored for this identifier, attribute, key 
    *              combination
    * @return the size of this hash
    */
   public int putAttributeKeyValue ( String identifier,
                                     String attribute,
                                     String key,
                                     Object value ) {
 
     byte set = definition.getAttributeValueType( attribute );
     if ( set == -1 ) {
       // first we need the type
       set = guessAndDefineObjectType( value, attribute );
     } 
     
     try {
       // now insert the current_val into the end of the list
       setAttributeValue( identifier,
                          attribute,
                          objectAsType( value, set ),
                          new Object[] {key} );
     } catch ( Exception e ) {
       return -1;
     }
 
 
     return getAttributeKeyspan( identifier, 
                                 attribute,
                                 null).numRemaining();
  
   }
 
   /**
    * @param identifier the unique identifier of the GraphObject
    * @param attribute the name of the attribute
    * @return a new Set of all the keys for this attribute
    */
   public Set getAttributeKeySet ( String identifier,
                                   String attribute ) {
 
     CountedIterator ce =  getAttributeKeyspan( identifier, 
                                                   attribute,
                                                   null);
     Set set = new HashSet();
     while ( ce.hasNext() ) {
       set.add( ce.next() );
     }
     return set;
   }
 
   /**
    * @param identifier the unique identifier of the GraphObject
    * @param attribute the name of the attribute
    * @param position the position in the list
    * @return the value stored for this attribute and object for this key
    */
   public Object getAttributeKeyValue ( String identifier,
                                        String attribute,
                                        String key ) {
     try {
       return getAttributeValue( identifier,
                                 attribute,
                                 new Object[] { key } );
     } catch ( Exception e ) {
       return null;
     }
   }
   
 
   /**
    * @param identifier the unique identifier of the GraphObject
    * @param attribute the name of the attribute
    * @return the a new Map of the collection of keys and values 
    */
   public Map getAttributeValuesMap ( String identifier,
                                      String attribute ) {
 
     CountedIterator ce =  getAttributeKeyspan( identifier, 
                                                   attribute,
                                                   null);
     Map map = new HashMap();
     while ( ce.hasNext() ) {
       Object key = ce.next();
       map.put( key, getAttributeValue( identifier,
                                        attribute,
                                        new Object[] { key } ) );
     }
     return map;
   }
 
 
   /**
    * @param identifier the unique identifier of the GraphObject
    * @param attribute the name of the attribute
    * @param position the position in the list
    * @return remove the value stored for this attribute and object for this key
    */
   public Object deleteAttributeKeyValue ( String identifier,
                                           String attribute,
                                           String key ) {
     try {
       return removeAttributeValue( identifier,
                                    attribute,
                                    new Object[] { key } );
     } catch ( Exception e ) {
       return null;
     }
   }
   
 
 
   ////////////////////////////////////////
   // SECTION 4: helpepr methods and convience
 
 
   /**
    * @return the identifiers of all objects with a given attribute defined
    */
   public Set getDefinedForAttribute ( String attributeName ) {
 
     Set set = new HashSet();
     CountedIterator ce = getObjectKeys( attributeName );
     while ( ce.hasNext() ) {
       set.add( ce.next() );
     }
     return set;
 
   }
 
 
   /**
    * @return the unique values among the values of all objects with a given attribute.
    */
   public Set getUniquAttributeValues ( String attributeName ) {
 
     Set set = new HashSet();
     CountedIterator ce = getObjectKeys( attributeName );
     while ( ce.hasNext() ) {
       String id = ( String )ce.next();
       List list = CyDataHelpers.getAllAttributeValues( id,  
                                                        attributeName, 
                                                        data, 
                                                        definition );
       set.addAll( list );
     }
     return set;
   }
 
 
 
   ////////////////////////////////////////
   // Expression Data Implementation
  
 
 
 
   ////////////////////////////////////////
   // GraphObjAttributes Implementation
 
 
   /**
    * Sets a TaskMonitor for tracking loading of node attribute files.
    * @param taskMonitor
    */
   public void setTaskMonitor ( TaskMonitor taskMonitor ) {
     this.taskMonitor = taskMonitor;
   }
 
   /**
    * All objects "getIdentifier" method returns the String id used to
    * represent the object, so this is redundant.
    * @deprecated Object ID is set on creation
    */
   public void addNameMapping ( String id, 
                                Object graphObject ) {
 
     // we could check for the getIdentifier() being equal to id, but it may 
     // not really get us anywhere.
 
   }
 
   /**
    * Objects CANNOT have their names removed
    * @deprecated makes no sense
    */
   public void removeNameMapping ( String canonicalName ) {}
 
   /**
    * Objects CANNOT have their names removed
    * @deprecated makes no sense
    */
   public void removeObjectMapping ( Object graphObj ) {}
  
   /**
    * @deprecated  makes no sense
    */
   public void clearNameMap () {}
 
   /**
    * @deprecated  makes no sense
    */
   public void clearObjectMap () {}
 
   /**
    * This is a HashMap that is a relationship of all objects to their identifiers
    * KEY: Object
    * VALUE: ID
    * @deprecated instead iterate over objects and use getIdentifier()
    */
   public HashMap getNameMap () {
     
     if (type == OTHER) return null;
     HashMap map = new HashMap();
     Iterator i;
     if ( type == NODES ) 
       i = Cytoscape.getRootGraph().nodesIterator();
     else
       i = Cytoscape.getRootGraph().edgesIterator();
 
     while( i.hasNext() ) {
       GraphObject gobj = ( GraphObject )i.next();
       map.put( gobj, gobj.getIdentifier() );
     }
     return map;
   }
 
 
   /**
    * This is a HashMap that is a relationship of all objects to their identifiers
    * KEY: ID
    * VALUE: Object
    * @deprecated instead iterate over objects and use getIdentifier()
    */
   public HashMap getObjectMap () {
     
     if (type == OTHER) return null;
     HashMap map = new HashMap();
     Iterator i;
     if ( type == NODES ) 
       i = Cytoscape.getRootGraph().nodesIterator();
     else
       i = Cytoscape.getRootGraph().edgesIterator();
 
     while( i.hasNext() ) {
       GraphObject gobj = ( GraphObject )i.next();
       map.put( gobj.getIdentifier(), gobj );
     }
     return map;
   }
 
 
   /**
    * KEY: AttributeName
    * VALUE: Type ( as a java.lang.Class )
    * @deprecated 
    */
   public HashMap getClassMap () {
 
     HashMap map = new HashMap();
     Iterator iter = definition.getDefinedAttributes();
     while ( iter.hasNext() ) {
       String attr = ( String )iter.next();
       byte type = definition.getAttributeValueType(attr);
 
       if ( type == TYPE_BOOLEAN )
         map.put( attr, Boolean.class );
       else if ( type == TYPE_FLOATING_POINT )
         map.put( attr, Double.class );
       else if (type == TYPE_INTEGER )
         map.put( attr, Integer.class );
       else if (type == TYPE_STRING )
         map.put( attr, String.class );
     }
     return map;
   }
 
   /**
    * This takes a HashMap of :
    * KEY: AttributeName
    * VALUE: Type ( as a java.lang.Class )
    * and calls defineDataAttribute
    * @deprecated
    */
   public void addClassMap ( HashMap newClassMap ) {
 
     Iterator keys = newClassMap.keySet().iterator();
     while( keys.hasNext() ) {
       String key = ( String )keys.next();
       Class dude = ( Class )newClassMap.get( key );
       try { 
         if ( dude == Boolean.class ) {
           definition.defineAttribute( key,
                                       TYPE_BOOLEAN,
                                       new byte[] {TYPE_STRING} );
         } else if ( dude == Double.class ) {
           definition.defineAttribute( key,
                                       TYPE_FLOATING_POINT,
                                       new byte[] {TYPE_STRING} );
         } else if (dude == Integer.class ) {
           definition.defineAttribute( key,
                                       TYPE_INTEGER,
                                       new byte[] {TYPE_STRING} );
         } else if (dude == String.class ) {
           definition.defineAttribute( key,
                                       TYPE_STRING,
                                       new byte[] {TYPE_STRING} );
         } else {
         }
       } catch ( Exception e ) {
         //
       }
     }
   }
       
 
 
   /**
    * @deprecated makes no sense
    */
   public void addNameMap ( HashMap nameMapping ) {}
 
   /**
    * @deprecated makes no sense
    */
   public void addObjectMap ( HashMap objectMapping ) {}
 
   /**
    * @deprecated 
    * @see giny.model.GraphObject#getIdentifier()
    */
   public String getCanonicalName ( Object graphObject ) {
     return ( ( GraphObject)graphObject).getIdentifier();
   }
 
   /**
    * @return A GraphObject that matches this name
    */
   public Object getGraphObject ( String identifier ) {
 
     if (type == OTHER) return null;
     //TODO LINEAR TIME
 
     if ( type == NODES ) {
       return Cytoscape.getRootGraph().getNode( identifier );
     } else {
       return Cytoscape.getRootGraph().getEdge( identifier );
     }
   }
 
   /**
    * @deprecated since there is only of these for a given session
    */
   public void set ( GraphObjAttributes attributes ) {}
 
  
  
   /**
    * @deprecated
    * This method takes a GraphObject, as well as an attribute, and associated
    * value. The value is retrievable later by using the same GraphObject and 
    * attribute combination.
    *
    * The attribute definition should be set, but if it is not, a type will be guessed
    * for it.
    * @param attributeName the name of the attribute
    * @param graphObjectName  the identifier of a Node or Edge
    * @param value the value for this object for this attribute 
    * @return true if the value was set, false if it was not set
    */
   public boolean set ( String attributeName, 
                        String graphObjectName, 
                        Object value ) {
   
     setAttributeValue( graphObjectName,
                        attributeName,
                        value );
     return true;
   }
   
 
   /**
    * @deprecated
    * Using this method means that you are converting to a list, and further that you are
    * are adding this value to the end of the list
    */
   public boolean append ( String attributeName, 
                           String graphObjectName, 
                           Object value ) {
 
     addAttributeListValue( graphObjectName,
                            attributeName,
                            value );
     return true;
 
   }
   
   /**
    * @deprecated use Object not double
    *  a convenience method; value will be promoted to Double
    */
   public boolean set ( String attributeName, 
                        String graphObjectName, 
                        double value ) {
     return set( attributeName,
                 graphObjectName,
                 new Double(value) );
   }
 
   /**
    * @deprecated dumb
    */
   public boolean set ( String graphObjectName, HashMap bundle ) {
 
     String [] keys = (String []) bundle.keySet().toArray (new String [0]);
     boolean success = true;
     for (int i=0; i < keys.length; i++) {
       String attributeName = keys [i];
       Object value = bundle.get (attributeName);
       if(!set (attributeName, graphObjectName, value)){
         success = false;
       }
     }
 
     return success;
 
 
   }
   
   /**
    * the number of different attributes currently registered
    * @deprecated
    */
   public int numberOfAttributes () {
     return definition.getDefinedAttributes().numRemaining();
   }
 
   /**
    * get the names of all of the attributes
    * @deprecated
    */
   public String[] getAttributeNames () {
 
     List list = new ArrayList();
     Iterator iter = definition.getDefinedAttributes();
     while ( iter.hasNext() ) {
       list.add( ( String )iter.next() );
     }
     return (String []) list.toArray (new String [0]);
   }
 
   /**
    * return the identifier of all objects with a given attribute.
    */
   public String[] getObjectNames ( String attributeName ) {
     Iterator iter = data.getObjectKeys( attributeName );
     List list = new ArrayList();
     while ( iter.hasNext() ) {
       list.add( ( String )iter.next() );
     }
     return (String []) list.toArray (new String [0]);
   } 
 
   /**
    * return the unique values among the values of all objects with a given attribute.
    */
   public Object[] getUniqueValues ( String attributeName ) {
 
     Set unique = new HashSet();
     
     Iterator i = getObjectKeys( attributeName );
 
     while( i.hasNext() ) {
       //GraphObject gobj = ( GraphObject )i.next();
       unique.addAll( getList( attributeName,
                               (String)i.next() ) );
     }
 
      return ( Object[] )unique.toArray (new Object [0]);
 
   }
 
       
   /** @deprecated */
   public List getList ( String attributeName,
                         String graphObjectName ) {
     return getAttributeValueList( graphObjectName, attributeName );
   }
 
  
   /**
    * return the unique Strings among the values of all objects with a given attribute.
    */
   public String[] getUniqueStringValues ( String attributeName ) {
     if ( definition.getAttributeValueType( attributeName ) != TYPE_STRING ) 
       return new String[] {};
 
     Set unique = new HashSet();
 
     Iterator i = getObjectKeys( attributeName );
 
     while( i.hasNext() ) {
       //GraphObject gobj = ( GraphObject )i.next();
       unique.addAll( getList( attributeName,
                               (String)i.next() ) );
     }
 
      return ( String[] )unique.toArray (new String [0]);
 
   }
 
   /**
    * return the number of graph objects with the specified attribute.
    */
   public int getObjectCount ( String attributeName ) {
     try {
       return data.getObjectKeys( attributeName ).numRemaining();
     } catch ( Exception e ) {
       return 0;
     }
   }
  
   /**
    * I guess this is if there is a definition
    */ 
   public boolean hasAttribute ( String attributeName ) {
     try {
       if ( definition.getAttributeValueType( attributeName ) != -1 ) 
         return true;
     } catch ( Exception e ) {
       return false;
     }
     return false;
   }
 
   /**
    * Is there and attribute defined for this object 
    */
   public boolean hasAttribute ( String attributeName, String graphObjName ) {
     if ( data.getAttributeKeyspan(graphObjName,  
                              attributeName, 
                              null).numRemaining() != -1 )
       return true;
     return false;
   }
  
   /**
    *  return a hash whose keys are graphObjectName Strings, and whose values are
    *  a Vector of java objects the class of these objects, and
    *  the category of the attribute (annotation, data, URL) may be learned
    *  by calling getClass and getCategory
    *
    *  @return   a HashMap whose keys are graph object names (typically canonical names
    *            for nodes and edges) and whose values are Vectors of java objects.
    *
    *  @see #getClass
    *  @see #getCategory
    *
    */
   public HashMap getAttribute ( String attributeName ) {
 
     HashMap map = new HashMap();
 
     if ( attributeName == null || !(attributeName instanceof String) )
       return map;
     
     if ( getAttributeValueType( attributeName ) == -1 ) {
       return map;
     }
 
     Iterator i = getObjectKeys( attributeName );
 
     while( i.hasNext() ) {
       //GraphObject gobj = ( GraphObject )i.next();
       String iden = (String)i.next();
       map.put( iden, getAttributeValue( iden, attributeName ) );
     }
     return map;
   }
  
   /**
    * @deprecated
    *  remove the entire second level Hashmap whose key is the specified attributeName
    */
   public void deleteAttribute ( String attributeName ) {
     definition.undefineAttribute( attributeName );
   }
 
   /**
    *  remove the specified attribute from the specified node or edge
    */
   public void deleteAttribute ( String attribute, String identifier ) {
     //data.removeAttributeValue( graphObjectName, attributeName, ZERO );
     CountedIterator ce =  getAttributeKeyspan( identifier, 
                                                   attribute,
                                                   null);
     while ( ce.hasNext() ) {
       Object key = ce.next();
       removeAttributeValue( identifier,
                             attribute,
                             new Object[] { key } );
     }
   
     
   }
 
   /**
    *  remove the specified attribute value from the specified node or edge.
    *  there may be multiple values associated with the attribute, so search through
    *  the list, and if it is found, remove it
    */
   public void deleteAttributeValue ( String attributeName, String graphObjectName, Object value ) {
     try {
       data.removeAttributeValue( graphObjectName, attributeName, ZERO );
     } catch ( Exception e ) {}
   }
 
   /**
    *  specify the class of this attribute.  all subsequently added attribute
    *  values must be of the exactly this class
    */
   public boolean setClass ( String attributeName, Class attributeClass ) {
 
     try { 
       if ( attributeClass == Boolean.class ) {
         definition.defineAttribute( attributeName,
                                     TYPE_BOOLEAN,
                                     new byte[] {TYPE_STRING} );
         return true;
       }
       else if (  attributeClass == Double.class ) {
         definition.defineAttribute( attributeName,
                                     TYPE_FLOATING_POINT,
                                     new byte[] {TYPE_STRING} );
         return true;
       }
       else if (  attributeClass == Integer.class ) {
         definition.defineAttribute( attributeName,
                                     TYPE_INTEGER,
                                     new byte[] {TYPE_STRING} );
         return true;
       }
       else if (  attributeClass == String.class ) {
         definition.defineAttribute( attributeName,
                                  TYPE_STRING,
                                     new byte[] {TYPE_STRING} );
         return true;
       } 
     } catch ( Exception e ) {
       return false;
     }
     return false;
   }
 
   /**
    *  all attributes are lists (java.lang.Vector) sharing the same base type; discover
    *  and return that here
    */
   public Class getClass ( String attributeName ) {
     try {
       byte type = definition.getAttributeValueType( attributeName );
       if ( type == TYPE_BOOLEAN )
         return Boolean.class;
       else if ( type == TYPE_FLOATING_POINT )
         return Double.class;
       else if ( type == TYPE_INTEGER )
         return Integer.class;
       else if ( type == TYPE_STRING )
         return String.class;
       else
         return Object.class;
     } catch ( Exception e ) {
       return Object.class;
     }
 
    
     
   }
 
   
   /**
    *  for backwards compatibility:  the value of an attribute used to be
    *  strictly a single scalar; now -- even though attributes are all lists
    *  of scalars -- we support the old idiom by retrieving the first scalar
    *  from the list.
    */
   public Object getValue ( String attributeName, 
                            String graphObjectName ) {
     return get( attributeName, graphObjectName );
   }
   
   public Object get ( String attributeName, 
                       String graphObjectName ) {
     try {
       return data.getAttributeValue( graphObjectName,
                                      attributeName,
                                      ZERO );
     } catch ( Exception e ) {
       return null;
     }
   }
 
   /**
    *  a convenience method:  convert the Vector of objects into an array
    */
   public Object[] getArrayValues ( String attributeName, 
                                    String graphObjectName ) {
     
     return ( Object[] )getAttributeValueList( graphObjectName, 
                                               attributeName )
       .toArray (new Object [0]);
   }
 
   /**
    *  a convenience method, useful if you are certain that the attribute stores
    *  Strings; convert the Vector of Objects into an array of Strings
    */
   public String [] getStringArrayValues ( String attributeName, 
                                           String graphObjectName ) {
 
     if ( definition.getAttributeValueType( attributeName ) != TYPE_STRING ) 
       return new String[] {};
 
      return ( String[] )getAttributeValueList( graphObjectName, 
                                                attributeName )
        .toArray (new String [0]);
 
   }
 
   /**
    *  construe the possibly multiple values of the attribute as a scalar Double,
    *  if possible
    */
   public Double getDoubleValue ( String attributeName, 
                                  String graphObjectName ) {
 
     if ( definition.getAttributeValueType( attributeName ) != TYPE_FLOATING_POINT )
       return new Double(0);
 
     return ( Double )data.getAttributeValue( graphObjectName,
                                              attributeName,
                                              ZERO );
   }
 
   /**
    *  construe the possibly multiple values of the attribute as a scalar Integer,
    *  if possible
    */
   public Integer getIntegerValue ( String attributeName, 
                                    String graphObjectName ){
 
     if ( definition.getAttributeValueType( attributeName ) != TYPE_INTEGER )
       return new Integer(0);
 
     return ( Integer )data.getAttributeValue( graphObjectName,
                                              attributeName,
                                              ZERO );
   } 
 
   /**
    *  construe the possibly multiple values of the attribute as a scalar String
    */
   public String getStringValue ( String attributeName, 
                                  String graphObjectName ) {
 
     if ( definition.getAttributeValueType( attributeName ) != TYPE_STRING )
       return "";
 
     return ( String )data.getAttributeValue( graphObjectName,
                                              attributeName,
                                              ZERO );
   }
 
 
   /**
    *  for the graphObject named by canonicalName, extract and return all attributes
    *
    *  @see #getValue
 
    * KEY: Attributename 
    * VALUE: value[0]
    */
   public HashMap getAttributes ( String identifier ) {
     HashMap map = new HashMap();
     
     Iterator iter = definition.getDefinedAttributes();
     while ( iter.hasNext() ) {
       String attr = ( String )iter.next();
       if ( data.getAttributeKeyspan( identifier,
                                      attr,
                                      null ).numRemaining() != 0 ) {
         map.put( attr, data.getAttributeValue( identifier, attr, ZERO ) );
       }
     }
     return map;
   }
 
   /**
    *  deduce attribute name, category, and java class from the first
    *  line of the attributes file.   the form of the first line is
    *  <pre>
    *  attribute name  (category=xxxx) (class=yyyy)
    *  </pre>
    *  category and class are optional; if absent, class will be inferred
    *  (see deduceClass), and category set to DEFAULT_CATEGORY
   * every attribute file must have, at minimum, the name attribute in the first line,
     * possibly with embedded spaces
     * in addition, the first line may have category and class information, as in
     *     homologene  (category=staticWebPage) (class=java.net.URL)
     * the present method extracts the mandatory attribute name, and the optional
     * category and class information.
     * 
     * note: category and class information, if present, are not only parsed here:  the
     *       information is also stored as appropriate in the current class data members 
   */
   public String processFileHeader ( String text ) {
     String attributeName = "";
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
         if (name.equals ("class")) {
           try {
             attributeClass = Class.forName (value);
           }
           catch (ClassNotFoundException ignore) {;}
         } // if name == 'class'
       } // while strtok
     } // else: at least one (x=y) found
     
     setClass (attributeName, attributeClass); // ******* Could fail *********
 
     return attributeName;
     
   }
     
   
   public void readAttributesFromFile ( File file ) {
     readAttributesFromFile ( file.getPath() );
   }
 
 
   /**
    *  Reads attributes from a file.  There is one basic format for attribute
    *  files, but a few aspects of the format are flexible.
    *
    *  The simplest form looks like this:
    *  <pre>
    *  expresssion ratio
    *  geneA = 0.1
    *  geneB = 8.9
    *  ...
    *  geneZ = 23.2
    *  </pre>
    */
   public void readAttributesFromFile ( String filename )  {
 
     if (taskMonitor != null) {
       taskMonitor.setStatus("Importing Attributes...");
     }
     
     String rawText = null;
     if (filename.trim().startsWith("jar://")) {
       try {
         TextJarReader reader = new TextJarReader(filename);
         reader.read();
         rawText = reader.getText();
         } catch ( Exception e ) {
           //throw new IOException( e.getMessage() );
         } // end of try-catch
       } else if ( filename.trim().startsWith("http://") || filename.trim().startsWith( "file://") ) {
       try {
         TextHttpReader reader = new TextHttpReader( filename );
         rawText = reader.getText();
       } catch ( Exception e ) {
         //throw new IOException( e.getMessage() );
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
       if (taskMonitor != null) {
         double percent = ((double) lineNumber / numTokens) * 100.0;
         taskMonitor.setPercentCompleted((int) percent);
       }
 
       if (newLine.trim().startsWith("#")) continue;
       lineNumber++;
       StringTokenizer strtok2 = new StringTokenizer(newLine, "=");
       if (strtok2.countTokens() < 2) {
         //throw new IOException
         //  ("Cannot parse line number " + lineNumber
         //   + ":\n\t" + newLine + ".  This may not be a valid "
         //   + "attributes file.");
       }
       String graphObjectName = strtok2.nextToken().trim();
       
       String rawString = newLine.substring(newLine.indexOf("=") + 1).trim();
 
       //System.out.println( "line: "+newLine+" rawStrng: "+rawString);
 
       String[] rawList;
       boolean isList = false;
       if (Misc.isList(rawString, "(", ")", "::")) {
         rawList = Misc.parseList(rawString, "(", ")", "::");
         isList = true;
       } else {
         rawList = new String[1];
         rawList[0] = rawString;
       }
       //if (extractingFirstValue && getClass(attributeName) == null) {
       //  extractingFirstValue = false;  // henceforth
       //  Class deducedClass = deduceClass(rawList[0]);
         //setClass(attributeName, deducedClass); // ***** Could fail ******* //
       //}
       Object[] objs = new Object[rawList.length];
       Class stringClass = (new String()).getClass();
 
       if (getClass(attributeName).equals(stringClass)) {
         for (int i = 0; i < rawList.length; i++) {
           rawList[i] = rawList[i].replaceAll("\\\\n", "\n");
         }
       }
 
 
       for (int i = 0; i < rawList.length; i++) {
         try {
 
           
 
           //objs[i] = createInstanceFromString
           //  (getClass(attributeName), rawList[i]);
 
           
           //System.out.println( "LAODING: "+rawList[i]+ "for: "+graphObjectName+" in "+attributeName );
 
 
           if (isList) {
             append(attributeName, graphObjectName, rawList[i]);
           } else {
             set(attributeName, graphObjectName, rawList[i]);
           }
         } catch (Exception e) {
           throw new IllegalArgumentException
             ("Could not create an instance of " +
              getClass(attributeName) + " from " + rawList[i]);
         }
       }
     }
 
     //  Inform User of What Just Happened.
     if (taskMonitor != null) {
       File  file = new File (filename);
       taskMonitor.setPercentCompleted (100);
       StringBuffer sb = new StringBuffer();
       sb.append("Succesfully loaded attributes from:  "
                 + file.getName());
       sb.append("\n\nAttribute Name:  " + attributeName);
       sb.append("\n\nNumber of Attributes:  " + lineNumber);
       taskMonitor.setStatus(sb.toString());
     }
   }
 
 
   /**
    *  return attributeName/attributeClass pairs, for every known attribute
    */
   public HashMap getSummary () {
 
     HashMap result = new HashMap ();
     String [] attributeNames = getAttributeNames ();
 
     for (int i=0; i < attributeNames.length; i++) {
       String attributeName = attributeNames [i];
       String firstObjectName = getObjectNames (attributeName)[0];
       Object firstValue = getValue (attributeName, firstObjectName);
       result.put (attributeName, firstValue.getClass ());
     } // for i
     
     return result;
   }
 
 
   /**
    * @deprecated terrible
    */
   public int countIdentical ( String graphObjectName ) {
     return 0;
   }
   
   
   /**
    * @deprecated
    *  given a string and a class, dynamically create an instance of that class from
    *  the string
    */
   static public Object createInstanceFromString ( Class requestedClass, 
                                                   String ctorArg ) {
     try {
       Class [] ctorArgsClasses = new Class [1];
       ctorArgsClasses [0] =  Class.forName ("java.lang.String");
       Object [] ctorArgs = new Object [1];
       ctorArgs [0] = new String (ctorArg);
       Constructor ctor = requestedClass.getConstructor (ctorArgsClasses);
       return ctor.newInstance (ctorArgs);
     } catch ( Exception e ) {
       return null;
     }
   } // createInstanceFromString
 
   /**
    * @deprecated
    *  determine (heuristically) the most-specialized class instance which can be
    *  constructed from the supplied string.
    */
   static public Class deduceClass ( String string ) {
     String [] classNames = {"java.net.URL",
                             "java.lang.Integer",
                             "java.lang.Double",
                             "java.lang.String"};
     for (int i=0; i < classNames.length; i++) {
       try {
         Object obj = createInstanceFromString (Class.forName (classNames [i]), string);
         return obj.getClass ();
       }
       catch (Exception e) {
         ; // try the next class
       }
     } // for i
     
   return null;
   
   } // deduceClass
 
   /**
    * @deprecated
    */
   public static String[] unpackPossiblyCompoundStringAttributeValue ( Object value ) {
     String [] result = new String [0];
     try {
       if (value.getClass () == Class.forName ("java.lang.String")) {
         result = new String [1];
         result [0] = (String) value;
       }
       else if (value.getClass () == Class.forName ("[Ljava.lang.String;")) {
         result = (String []) value;
       }
       else if (value.getClass () == Class.forName ("java.util.Vector")) {
         Vector tmp = (Vector) value;
         result = (String []) tmp.toArray (new String [0]);
       }
       else {
         String msg = "AnnotationGui.unpackPossiblyCompoundAttributeValue, unrecognized class: " +
           value.getClass ();
         System.err.println (msg);
       }
     } // try
     catch (ClassNotFoundException ignore) {
       ignore.printStackTrace ();
     }
     
     return result;
     
   } // unpackPossiblyCompoundAttributeValue
 
 }
