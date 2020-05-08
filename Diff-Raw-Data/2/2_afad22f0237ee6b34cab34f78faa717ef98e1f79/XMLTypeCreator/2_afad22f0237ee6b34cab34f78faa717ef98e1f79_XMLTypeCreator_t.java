 package org.codehaus.xfire.aegis.type;
 
 import java.beans.PropertyDescriptor;
 import java.io.InputStream;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.namespace.QName;
 import javax.xml.stream.XMLStreamException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.codehaus.xfire.XFireRuntimeException;
 import org.codehaus.xfire.aegis.type.basic.BeanType;
 import org.codehaus.xfire.aegis.type.basic.XMLBeanTypeInfo;
 import org.codehaus.xfire.util.ClassLoaderUtils;
 import org.codehaus.xfire.util.NamespaceHelper;
 import org.codehaus.xfire.util.jdom.StaxBuilder;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.Namespace;
 import org.jdom.xpath.XPath;
 
 /**
  * Deduce mapping information from an xml file.
  * The xml file should be in the same packages as the class, with the name <code>className.aegis.xml</code>.
  * For example, given the following service interface:
  * <p/>
  * <pre>
  * public Collection getResultsForValues(String id, Collection values); //method 1
  * public Collection getResultsForValues(int id, Collection values); //method 2
  * public String getResultForValue(String value); //method 3
  * </pre>
  * An example of the type xml is:
  * <pre>
  * &lt;mappings&gt;
  *  &lt;mapping&gt;
  *    &lt;method name="getResultsForValues"&gt;
  *      &lt;return-type componentType="com.acme.ResultBean" /&gt;
  *      &lt;!-- no need to specify index 0, since it's a String --&gt;
  *      &lt;parameter index="1" componentType="java.lang.String" /&gt;
  *    &lt;/method&gt;
  *  &lt;/mapping&gt;
  * &lt;/mappings&gt;
  * </pre>
  * <p/>
  * Note that for values which can be easily deduced (such as the String parameter, or the second service method)
  * no mapping need be specified in the xml descriptor, which is why no mapping is specified for method 3.
  * <p/>
  * However, if you have overloaded methods with different semantics, then you will need to specify enough
  * parameters to disambiguate the method and uniquely identify it. So in the example above, the mapping
  * specifies will apply to both method 1 and method 2, since the parameter at index 0 is not specified.
  *
  * @author Hani Suleiman
  *         Date: Jun 14, 2005
  *         Time: 7:47:56 PM
  * @author <a href="mailto:mikagoeckel@codehaus.org">Mika Gckel</a>
  * @author yvind Matheson Wergeland 
  */
 public class XMLTypeCreator extends AbstractTypeCreator
 {
     private static final Log log = LogFactory.getLog(XMLTypeCreator.class);
     //cache of classes to documents
     private Map documents = new HashMap();
 
     private static List stopClasses = new ArrayList();
     static 
     {
         stopClasses.add(Object.class);
         stopClasses.add(Exception.class);
         stopClasses.add(RuntimeException.class);
         stopClasses.add(Throwable.class);
     }
     
     protected Document getDocument(Class clazz)
     {
         if(clazz == null) return null;
         Document doc = (Document) documents.get(clazz.getName());
         if(doc != null)
         {
             return doc;
         }
         String path = '/' + clazz.getName().replace('.', '/') + ".aegis.xml";
         InputStream is = clazz.getResourceAsStream(path);
         if(is == null) return null;
         try
         {
             doc = new StaxBuilder().build(is);
             documents.put(clazz.getName(), doc);
             return doc;
         }
         catch(XMLStreamException e)
         {
             log.error("Error loading file " + path, e);
         }
         return null;
     }
  
     protected boolean isEnum(Class javaType)
     {
         Element mapping = findMapping(javaType);
         if (mapping != null)
         {
             return super.isEnum(javaType);
         }
         else
         {
             return nextCreator.isEnum(javaType); 
         }
     }
 
 
 
     public Type createEnumType(TypeClassInfo info)
     {
         Element mapping = findMapping(info.getTypeClass());
         if (mapping != null)
         {
             return super.createEnumType(info);
         }
         else
         {
             return nextCreator.createEnumType(info); 
         }
     }
 
 
     protected Type createMapType(TypeClassInfo info)
     {
         if(info.getGenericType() instanceof Class)
         {
         	Collection componentMappings = findMappings((Class)info.getGenericType());
         	if((componentMappings != null) && (componentMappings.size() > 0))
         	{
         		return super.createMapType(info);
         	}
         }
         
         if(info.getKeyType() instanceof Class)
         {
         	Collection keyMappings = findMappings((Class)info.getKeyType());
         	if((keyMappings != null) && (keyMappings.size() > 0))
         	{
             	return super.createMapType(info);
         	}
         }
 
         Element mapping = findMapping(info.getTypeClass());
         if(mapping != null)
         {
         	return super.createMapType(info);
         }
         
         return nextCreator.createMapType(info); 
     }
 
 
     public Type createCollectionType(TypeClassInfo info)
     {
         if(info.getGenericType() instanceof Class)
         {
             Element componentMapping = findMapping((Class)info.getGenericType());
             if(componentMapping != null)
             {
             	return createCollectionType(info, (Class)info.getGenericType());
             }
         }
 
         Element mapping = findMapping(info.getTypeClass());
         if(mapping != null)
         {
             return createCollectionType(info, (Class)info.getGenericType());
         }
 
         return nextCreator.createCollectionType(info); 
     }
 
     public TypeClassInfo createClassInfo(PropertyDescriptor pd)
     {
         Element mapping = findMapping(pd.getReadMethod().getDeclaringClass());
         if(mapping == null)
         {
             return nextCreator.createClassInfo(pd);
         }
         
         Element propertyEl = getMatch(mapping, "./property[@name='" + pd.getName() + "']");
         if(propertyEl == null) 
         {
             return nextCreator.createClassInfo(pd);
         }
 
         TypeClassInfo info = new TypeClassInfo();
         info.setTypeClass(pd.getReadMethod().getReturnType());
         readMetadata(info, propertyEl);
         
         return info;
     }
     
     protected Element findMapping(Class clazz)
     {
         Document doc = getDocument(clazz);
         if(doc == null) return null;
         
         Element mapping = getMatch(doc, "/mappings/mapping[@uri='" + getTypeMapping().getEncodingStyleURI() + "']");
         if (mapping == null)
         {
             mapping = getMatch(doc, "/mappings/mapping[not(@uri)]");
         }
         
         return mapping;
     }
 
     protected List findMappings(Class clazz)
     {
         ArrayList mappings = new ArrayList();
         
         Element top = findMapping(clazz);
         if (top != null) mappings.add(top);
         
         Class parent = clazz;
         while(true)
         {
         	
             // Read mappings for interfaces as well
 			Class[] interfaces = parent.getInterfaces();
 			for (int i = 0; i < interfaces.length; i++) {
 				Class interfaze = interfaces[i];
 				List interfaceMappings = findMappings(interfaze);
 				mappings.addAll(interfaceMappings);
 			}
 
             Class sup = parent.getSuperclass();
             
             if (sup == null || stopClasses.contains(sup)) 
                 break;
             
             Element mapping = findMapping(sup);
             if (mapping != null)
             {
                 mappings.add(mapping);
             }
             
             parent = sup;
         }
         
         return mappings;
     }
     
     public Type createDefaultType(TypeClassInfo info)
     {
         Element mapping = findMapping(info.getTypeClass());
         List mappings = findMappings(info.getTypeClass());
 
         if (mapping != null || mappings.size() > 0 )
         {
             String typeNameAtt = null;
             if (mapping != null) typeNameAtt = mapping.getAttributeValue("name");
             
             String extensibleElements = null;
             if (mapping != null) extensibleElements = mapping.getAttributeValue("extensibleElements");
             
             String extensibleAttributes = null;
             if (mapping != null) extensibleAttributes = mapping.getAttributeValue("extensibleAttributes");
             
             String defaultNS = NamespaceHelper.makeNamespaceFromClassName(info.getTypeClass().getName(), "http");
             QName name = null;
             if (typeNameAtt != null)
             {
                 name = NamespaceHelper.createQName(mapping, typeNameAtt, defaultNS);
                 
                 defaultNS = name.getNamespaceURI();
             }
             
             XMLBeanTypeInfo btinfo = new XMLBeanTypeInfo(info.getTypeClass(), 
                                                          mappings,
                                                          defaultNS);
             btinfo.setTypeMapping(getTypeMapping());
             btinfo.setDefaultMinOccurs(getConfiguration().getDefaultMinOccurs());
             btinfo.setDefaultNillable( getConfiguration().isDefaultNillable() );
 
             if ( extensibleElements != null ) btinfo.setExtensibleElements( Boolean.valueOf( extensibleElements ).booleanValue() );
             else btinfo.setExtensibleElements(getConfiguration().isDefaultExtensibleElements());
             
             if ( extensibleAttributes != null ) btinfo.setExtensibleAttributes( Boolean.valueOf( extensibleAttributes ).booleanValue() );
             else btinfo.setExtensibleAttributes(getConfiguration().isDefaultExtensibleAttributes());
             
             BeanType type = new BeanType(btinfo);
             
             if (name == null) name = createQName(info.getTypeClass());
             
             type.setSchemaType(name);
             
             type.setTypeClass(info.getTypeClass());
             type.setTypeMapping(getTypeMapping());
 
             return type;
         }
         else
         {
             return nextCreator.createDefaultType(info);
         }
     }
 
     public TypeClassInfo createClassInfo(Method m, int index)
     {
         Element mapping = findMapping(m.getDeclaringClass());
 
         if(mapping == null) return nextCreator.createClassInfo(m, index);
         
         //find the elements that apply to the specified method
         TypeClassInfo info = new TypeClassInfo();
         if(index >= 0)
         {
             if(index >= m.getParameterTypes().length)
             {
                 throw new XFireRuntimeException("Method " + m + " does not have a parameter at index " + index);
             }
             //we don't want nodes for which the specified index is not specified
             List nodes = getMatches(mapping, "./method[@name='" + m.getName() + "']/parameter[@index='" + index + "']/parent::*");
             if(nodes.size() == 0)
             {
                 //no mapping for this method
                 return nextCreator.createClassInfo(m, index);
             }
             //pick the best matching node
             Element bestMatch = getBestMatch(mapping, m, nodes);
 
             if(bestMatch == null)
             {
                 //no mapping for this method
                 return nextCreator.createClassInfo(m, index);
             }
             info.setTypeClass(m.getParameterTypes()[index]);
             //info.setAnnotations(m.getParameterAnnotations()[index]);
             Element parameter = getMatch(bestMatch, "parameter[@index='" + index + "']");
             readMetadata(info, parameter);
         }
         else
         {
             List nodes = getMatches(mapping, "./method[@name='" + m.getName() + "']/return-type/parent::*");
             if(nodes.size() == 0) return nextCreator.createClassInfo(m, index);
             Element bestMatch = getBestMatch(mapping, m, nodes);
             if(bestMatch == null)
             {
                 //no mapping for this method
                 return nextCreator.createClassInfo(m, index);
             }
             info.setTypeClass(m.getReturnType());
             //info.setAnnotations(m.getAnnotations());
             Element rtElement = bestMatch.getChild("return-type");
             readMetadata(info, rtElement);
         }
 
         return info;
     }
 
     protected void readMetadata(TypeClassInfo info, Element parameter)
     {        
         info.setTypeName(createQName(parameter, parameter.getAttributeValue("typeName")));
         info.setMappedName(createQName(parameter, parameter.getAttributeValue("mappedName")));
         setComponentType(info, parameter);
         setKeyType(info, parameter);
         setType(info, parameter);
         
         String min = parameter.getAttributeValue("minOccurs");
         if (min != null) info.setMinOccurs(Long.parseLong(min));
         
         String max = parameter.getAttributeValue("maxOccurs");
         if (max != null) info.setMaxOccurs(Long.parseLong(max));
         
         String flat = parameter.getAttributeValue("flat");
        if (flat != null) info.setFlat(Boolean.valueOf(flat.toLowerCase()).booleanValue());
     }
     
     protected void setComponentType(TypeClassInfo info, Element parameter)
     {
         String componentType = parameter.getAttributeValue("componentType");
         if(componentType != null)
         {
             try
             {
                 info.setGenericType(ClassLoaderUtils.loadClass(componentType, getClass()));
             }
             catch(ClassNotFoundException e)
             {
                 throw new XFireRuntimeException("Unable to load component type class " + componentType, e);
             }
         }
     }
 
     protected void setType(TypeClassInfo info, Element parameter)
     {
         String type = parameter.getAttributeValue("type");
         if(type != null)
         {
             try
             {
                 info.setType(ClassLoaderUtils.loadClass(type, getClass()));
             }
             catch(ClassNotFoundException e)
             {
                 throw new XFireRuntimeException("Unable to load type class " + type, e);
             }
         }
     }
 
     protected void setKeyType(TypeClassInfo info, Element parameter)
     {
         String componentType = parameter.getAttributeValue("keyType");
         if(componentType != null)
         {
             try
             {
                 info.setKeyType(ClassLoaderUtils.loadClass(componentType, getClass()));
             }
             catch(ClassNotFoundException e)
             {
                 log.error("Unable to load mapping class " + componentType);
             }
         }
     }
     
     private Element getBestMatch(Element mapping, Method method, List availableNodes)
     {
         //first find all the matching method names
         List nodes = getMatches(mapping, "./method[@name='" + method.getName() + "']");
         //remove the ones that aren't in our acceptable set, if one is specified
         if(availableNodes != null)
         {
             nodes.retainAll(availableNodes);
         }
         //no name found, so no matches
         if(nodes.size() == 0) return null;
         //if the method has no params, then more than one mapping is pointless
         Class[] parameterTypes = method.getParameterTypes();
         if(parameterTypes.length == 0) return (Element)nodes.get(0);
         //here's the fun part.
         //we go through the method parameters, ruling out matches
         for(int i = 0; i < parameterTypes.length; i++)
         {
             Class parameterType = parameterTypes[i];
             for(Iterator iterator = nodes.iterator(); iterator.hasNext();)
             {
                 Element element = (Element)iterator.next();
                 //first we check if the parameter index is specified
                 Element match = getMatch(element, "parameter[@index='" + i + "']");
                 if(match != null)
                 {
                     //we check if the type is specified and matches
                     if(match.getAttributeValue("class") != null)
                     {
                         //if it doesn't match, then we can definitely rule out this result
                         if(!match.getAttributeValue("class").equals(parameterType.getName()))
                         {
                             iterator.remove();
                         }
                     }
                 }
             }
         }
         //if we have just one node left, then it has to be the best match
         if(nodes.size() == 1) return (Element)nodes.get(0);
         //all remaining definitions could apply, so we need to now pick the best one
         //the best one is the one with the most parameters specified
         Element bestCandidate = null;
         int highestSpecified = 0;
         for(Iterator iterator = nodes.iterator(); iterator.hasNext();)
         {
             Element element = (Element)iterator.next();
             int availableParameters = element.getChildren("parameter").size();
             if(availableParameters > highestSpecified)
             {
                 bestCandidate = element;
                 highestSpecified = availableParameters;
             }
         }
         return bestCandidate;
     }
 
     private Element getMatch(Object doc, String xpath)
     {
         try
         {
             XPath path = XPath.newInstance(xpath);
             return (Element)path.selectSingleNode(doc);
         }
         catch(JDOMException e)
         {
             throw new XFireRuntimeException("Error evaluating xpath " + xpath, e);
         }
     }
 
     private List getMatches(Object doc, String xpath)
     {
         try
         {
             XPath path = XPath.newInstance(xpath);
             List result = path.selectNodes(doc);
             return result;
         }
         catch(JDOMException e)
         {
             throw new XFireRuntimeException("Error evaluating xpath " + xpath, e);
         }
     }
 
     /**
      * Creates a QName from a string, such as "ns:Element".
      */
     protected QName createQName(Element e, String value)
     {
         if (value == null || value.length() == 0) return null;
         
         int index = value.indexOf(":");
         
         if (index == -1)
         {
             return new QName(getTypeMapping().getEncodingStyleURI(), value);
         }
         
         String prefix = value.substring(0, index);
         String localName = value.substring(index+1);
         Namespace ns = e.getNamespace(prefix);
         
         if (ns == null || localName == null)
             throw new XFireRuntimeException("Invalid QName in mapping: " + value);
         
         return new QName(ns.getURI(), localName, prefix);
     }
 }
