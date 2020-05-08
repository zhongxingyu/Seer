 /*
  * Copyright (c) 2000-2004 Netspective Communications LLC. All rights reserved.
  *
  * Netspective Communications LLC ("Netspective") permits redistribution, modification and use of this file in source
  * and binary form ("The Software") under the Netspective Source License ("NSL" or "The License"). The following
  * conditions are provided as a summary of the NSL but the NSL remains the canonical license and must be accepted
  * before using The Software. Any use of The Software indicates agreement with the NSL.
  *
  * 1. Each copy or derived work of The Software must preserve the copyright notice and this notice unmodified.
  *
  * 2. Redistribution of The Software is allowed in object code form only (as Java .class files or a .jar file
  *    containing the .class files) and only as part of an application that uses The Software as part of its primary
  *    functionality. No distribution of the package is allowed as part of a software development kit, other library,
  *    or development tool without written consent of Netspective. Any modified form of The Software is bound by these
  *    same restrictions.
  *
  * 3. Redistributions of The Software in any form must include an unmodified copy of The License, normally in a plain
  *    ASCII text file unless otherwise agreed to, in writing, by Netspective.
  *
  * 4. The names "Netspective", "Axiom", "Commons", "Junxion", and "Sparx" are trademarks of Netspective and may not be
  *    used to endorse or appear in products derived from The Software without written consent of Netspective.
  *
  * THE SOFTWARE IS PROVIDED "AS IS" WITHOUT A WARRANTY OF ANY KIND. ALL EXPRESS OR IMPLIED REPRESENTATIONS AND
  * WARRANTIES, INCLUDING ANY IMPLIED WARRANTY OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE OR NON-INFRINGEMENT,
  * ARE HEREBY DISCLAIMED.
  *
  * NETSPECTIVE AND ITS LICENSORS SHALL NOT BE LIABLE FOR ANY DAMAGES SUFFERED BY LICENSEE OR ANY THIRD PARTY AS A
  * RESULT OF USING OR DISTRIBUTING THE SOFTWARE. IN NO EVENT WILL NETSPECTIVE OR ITS LICENSORS BE LIABLE FOR ANY LOST
  * REVENUE, PROFIT OR DATA, OR FOR DIRECT, INDIRECT, SPECIAL, CONSEQUENTIAL, INCIDENTAL OR PUNITIVE DAMAGES, HOWEVER
  * CAUSED AND REGARDLESS OF THE THEORY OF LIABILITY, ARISING OUT OF THE USE OF OR INABILITY TO USE THE SOFTWARE, EVEN
  * IF IT HAS BEEN ADVISED OF THE POSSIBILITY OF SUCH DAMAGES.
  */
 package com.netspective.commons.xdm;
 
 import java.io.File;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Field;
 import java.lang.reflect.InvocationTargetException;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.Locale;
 import java.util.Map;
 import java.util.Properties;
 import java.util.ResourceBundle;
 import java.util.Set;
 import java.util.TreeSet;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.netspective.commons.command.Command;
 import com.netspective.commons.command.CommandNotFoundException;
 import com.netspective.commons.command.Commands;
 import com.netspective.commons.io.InputSourceLocator;
 import com.netspective.commons.io.InputSourceTracker;
 import com.netspective.commons.io.PropertiesLoader;
 import com.netspective.commons.lang.ClassJavaDoc;
 import com.netspective.commons.lang.JavaDocs;
 import com.netspective.commons.lang.MethodJavaDoc;
 import com.netspective.commons.text.TextUtils;
 import com.netspective.commons.value.ValueSource;
 import com.netspective.commons.value.ValueSources;
 import com.netspective.commons.value.source.RedirectValueSource;
 import com.netspective.commons.xdm.exception.DataModelException;
 import com.netspective.commons.xdm.exception.UnsupportedAttributeException;
 import com.netspective.commons.xdm.exception.UnsupportedElementException;
 import com.netspective.commons.xdm.exception.UnsupportedTextException;
 import com.netspective.commons.xml.template.TemplateConsumer;
 import com.netspective.commons.xml.template.TemplateProducer;
 import com.netspective.commons.xml.template.TemplateProducerParent;
 import com.netspective.commons.xml.template.TemplateProducers;
 
 /**
  * This class is used to introspect existing classes and allow parsing of XML
  * and unmarshalling the XML elements into an exact replica of a Java object
  * model. This is very useful when an XML structure needs to be unmarshalled
  * into a set of java classes (called a DataModel) that mimics the XML.
  * This class's original code (and indeed the very idea) was taken from the
  * Jakarta Ant project but this version is very generic (whereas the Ant
  * code was specific to TaskDefs). The DataModelSchema is most appropriate
  * for cases where unmarshalling of XML into a structured Java object model
  * is critical; this class does not (yet) handle the marshalling of Java into
  * XML using any particular rules (though it wouldn't be hard to add that
  * capability).
  *
  * @author <a href="mailto:stefan.bodewig@epost.de">Stefan Bodewig</a>
  * @author <a href="mailto:snshah@netspective.com">Shahid N. Shah</a>
  */
 public class XmlDataModelSchema
 {
     private static final Log log = LogFactory.getLog(XmlDataModelSchema.class);
 
     public static final String XMLDATAMODEL_SCHEMA_OPTIONS_FIELD_NAME = "XML_DATA_MODEL_SCHEMA_OPTIONS";
     public static final String ADD_TEXT_DEFAULT_METHOD_NAME = "addText";
 
     public interface CustomElementCreator
     {
         public Object createCustomDataModelElement(XdmParseContext pc, XmlDataModelSchema schema, Object parent, String elementName, String alternateClassName)
                 throws DataModelException, InvocationTargetException, IllegalAccessException, InstantiationException;
     }
 
     public interface CustomElementStorer
     {
         public void storeCustomDataModelElement(XdmParseContext pc, XmlDataModelSchema schema, Object parent, Object child, String elementName)
                 throws DataModelException, InvocationTargetException, IllegalAccessException, InstantiationException;
     }
 
     public interface CustomElementAttributeSetter
     {
         public void setCustomDataModelElementAttribute(XdmParseContext pc, XmlDataModelSchema schema, Object parent, String attrName, String attrValue)
                 throws DataModelException, InvocationTargetException, IllegalAccessException, DataModelException;
     }
 
     public interface ConstructionFinalizeListener
     {
         public void finalizeConstruction(XdmParseContext pc, Object element, String elementName) throws DataModelException;
     }
 
     public interface InputSourceTrackerListener
     {
         public void setInputSourceTracker(InputSourceTracker tracker);
     }
 
     public interface InputSourceLocatorListener
     {
         public InputSourceLocator getInputSourceLocator();
 
         public void setInputSourceLocator(InputSourceLocator locator);
     }
 
     public static class Options
     {
         private String pcDataHandlerMethodName = ADD_TEXT_DEFAULT_METHOD_NAME;
         private boolean generateAutoAliases = true;
         private boolean ignorePcData;
         private Map aliases = new HashMap();
         private Set ignoreNestedElements = new HashSet();
         private Set ignoreAttributes = new HashSet();
         private Set requiredAttributes = new HashSet();
         private Set requiredNestedElements = new HashSet();
         private Map subclassedItemsClasses = new HashMap();
 
         public Options()
         {
         }
 
         public Options(Options copy)
         {
             pcDataHandlerMethodName = copy.pcDataHandlerMethodName;
             generateAutoAliases = copy.generateAutoAliases;
             ignorePcData = copy.ignorePcData;
             aliases.putAll(copy.aliases);
             ignoreNestedElements.addAll(copy.ignoreNestedElements);
             ignoreAttributes.addAll(copy.ignoreAttributes);
             requiredAttributes.addAll(copy.requiredAttributes);
             requiredNestedElements.addAll(copy.requiredNestedElements);
         }
 
         public void setPropertyNames(PropertyNames propertyNames)
         {
             String[] allNames = propertyNames.getAllNames();
             for(int i = 0; i < allNames.length; i++)
             {
                 String alias = allNames[i];
                 if(ignoreAttributes.contains(alias))
                     ignoreAttributes.addAll(propertyNames.getAliases());
                 if(ignoreNestedElements.contains(alias))
                     ignoreNestedElements.addAll(propertyNames.getAliases());
             }
         }
 
         public boolean isGenerateAutoAliases()
         {
             return generateAutoAliases;
         }
 
         public boolean ignoreAttribute(String attrName)
         {
             return attrName.startsWith("__") || ignoreAttributes.contains(attrName);
         }
 
         public boolean ignoreNestedElement(String elementName)
         {
             return ignoreNestedElements.contains(elementName);
         }
 
         public Set getIgnoreAttributes()
         {
             return ignoreAttributes;
         }
 
         public Set getIgnoreNestedElements()
         {
             return ignoreNestedElements;
         }
 
         public Set getRequiredAttributes()
         {
             return requiredAttributes;
         }
 
         public Set getRequiredNestedElements()
         {
             return requiredNestedElements;
         }
 
         public boolean isIgnorePcData()
         {
             return ignorePcData;
         }
 
         public String getPcDataHandlerMethodName()
         {
             return pcDataHandlerMethodName;
         }
 
         public Options setIgnoreAttributes(Set ignoreAttributes)
         {
             this.ignoreAttributes = ignoreAttributes;
             return this;
         }
 
         public Options setIgnoreNestedElements(Set ignoreNestedElements)
         {
             this.ignoreNestedElements = ignoreNestedElements;
             return this;
         }
 
         public Options addIgnoreAttributes(String[] ignoreAttributesList)
         {
             for(int i = 0; i < ignoreAttributesList.length; i++)
                 ignoreAttributes.add(ignoreAttributesList[i]);
             return this;
         }
 
         public Options addIgnoreNestedElements(String[] ignoreElementsList)
         {
             for(int i = 0; i < ignoreElementsList.length; i++)
                 ignoreNestedElements.add(ignoreElementsList[i]);
             return this;
         }
 
         public Options setRequiredAttributes(Set requiredAttributes)
         {
             this.requiredAttributes = requiredAttributes;
             return this;
         }
 
         public Options setRequiredNestedElements(Set requiredNestedElements)
         {
             this.requiredNestedElements = requiredNestedElements;
             return this;
         }
 
         public Options addRequiredAttributes(String[] requiredAttributesList)
         {
             for(int i = 0; i < requiredAttributesList.length; i++)
                 requiredAttributes.add(requiredAttributesList[i]);
             return this;
         }
 
         public Options addRequiredNestedElements(String[] requiredElementsList)
         {
             for(int i = 0; i < requiredElementsList.length; i++)
                 requiredNestedElements.add(requiredElementsList[i]);
             return this;
         }
 
         public Options setIgnorePcData(boolean ignorePcData)
         {
             this.ignorePcData = ignorePcData;
             return this;
         }
 
         public Options setPcDataHandlerMethodName(String pcDataHandlerMethodName)
         {
             this.pcDataHandlerMethodName = pcDataHandlerMethodName;
             return this;
         }
 
         public void setGenerateAutoAliases(boolean generateAutoAliases)
         {
             this.generateAutoAliases = generateAutoAliases;
         }
 
         public Map getAliases()
         {
             return aliases;
         }
 
         public void addAliases(String primaryName, String[] alternateNames)
         {
             if(null == alternateNames)
                 return;
 
             Set altNames = new HashSet();
             for(int i = 0; i < alternateNames.length; i++)
                 altNames.add(alternateNames[i]);
 
             Set set = (Set) aliases.get(primaryName);
             if(set == null)
                 aliases.put(primaryName, altNames);
             else
                 set.addAll(altNames);
         }
 
         public void addAliases(String primaryName, Set alternateNames)
         {
             if(null == alternateNames)
                 return;
 
             Set set = (Set) aliases.get(primaryName);
             if(set == null)
                 aliases.put(primaryName, alternateNames);
             else
                 set.addAll(alternateNames);
         }
 
         public Map getSubclassedItemsClasses()
         {
             return subclassedItemsClasses;
         }
 
         public Class getSubclassedItemClass(String item, Class clsDefault)
         {
             Class cls = (Class) subclassedItemsClasses.get(item);
             return cls != null ? cls : clsDefault;
         }
 
         public void addSubclassedItemClass(String item, Class cls)
         {
             subclassedItemsClasses.put(item, cls);
         }
     }
 
     /**
      * Holds options parameters for this schema
      */
     private Options options;
 
     /**
      * holds the names of the properties (key is a name, value is the PropertyName object associated with it)
      */
     private Map propertyNames;
 
     /**
      * holds the types of the attributes that could be set.
      */
     private Map attributeTypes;
 
     /**
      * holds the attribute setter anonymous classes.
      */
     private Map attributeSetters;
 
     /**
      * holds the attribute setter methods
      */
     private Map attributeSetterMethods;
 
     /**
      * holds the classes that have accessors
      */
     private Map attributeAccessors;
 
     /**
      * holds the classes that can manage bitmasked flags
      */
     private Map flagsAttributeAccessors;
 
     /**
      * Holds the types of nested elements that could be created.
      */
     private Map nestedTypes;
 
     /**
      * Holds methods to create nested elements.
      */
     private Map nestedCreators;
 
     /**
      * Holds methods to create nested elements.
      */
     private Map nestedAltClassNameCreators;
 
     /**
      * Holds methods to create nested elements.
      */
     private Map nestedAdders;
 
     /**
      * Holds methods to store configured or custom nested elements.
      */
     private Map nestedStorers;
 
     /**
      * The method to add PCDATA text.
      */
     private Method addText = null;
 
     /**
      * The Class that's been introspected.
      */
     private Class bean;
 
     /**
      * The settable attributes list useful for creating DTDs and other documentation for this bean
      */
     private AttributeDetailList settableAttrsDetailWithFlagsExpanded;
 
     /**
      * The settable attributes list useful for creating DTDs and other documentation for this bean
      */
     private AttributeDetailList settableAttrsDetailWithoutFlagsExpanded;
 
     /**
      * The list of nested elements useful for creating DTDs and other documentation for this bean
      */
     private ElementDetailList nestedElementsDetail;
 
     /**
      * instances we've already created
      */
     private static Map schemas = new HashMap();
 
     public static Map getSchemas()
     {
         return schemas;
     }
 
     /**
      * Factory method for schema objects.
      */
     public synchronized static XmlDataModelSchema getSchema(Class c)
     {
         XmlDataModelSchema schema = (XmlDataModelSchema) schemas.get(c);
         if(schema == null)
         {
             schema = new XmlDataModelSchema(c);
             schemas.put(c, schema);
         }
         return schema;
     }
 
     public Options getOptions()
     {
         return options;
     }
 
     public Map getPropertyNames()
     {
         return propertyNames;
     }
 
     /**
      * Gets the description of the bean represented by this Schema by using the runtime JavaDoc XML resources.
      *
      * @return The description of the class provided in the JavaDoc XML
      */
     public ClassJavaDoc getJavaDoc()
     {
         return JavaDocs.getInstance().getClassJavaDoc(getBean());
     }
 
     public class ElementDetail
     {
         private String elemName;
         private Class elemType;
         private TemplateProducer templateProducer;
         private boolean required;
 
         public ElementDetail(String name) throws DataModelException
         {
             this.elemName = name;
             this.elemType = getElementType(name);
             this.required = getOptions().getRequiredAttributes().contains(name);
         }
 
         public ElementDetail(String name, TemplateProducer templateProducer)
         {
             this.elemName = name;
             this.elemType = TemplateProducer.class;
             this.required = false;
             this.templateProducer = templateProducer;
         }
 
         public String getElemName()
         {
             return elemName;
         }
 
         public Class getElemType()
         {
             return elemType;
         }
 
         public boolean isRequired()
         {
             return required;
         }
 
         public boolean isInherited()
         {
             NestedStorer storer = (NestedStorer) nestedStorers.get(elemName);
             if(storer != null)
                 return storer.isInherited();
             else
             {
                 NestedCreator creator = (NestedCreator) nestedCreators.get(elemName);
                 if(creator != null)
                     return creator.isInherited();
                 else
                     return false;
             }
         }
 
         public Class getDeclaringClass()
         {
             if(isTemplateProducer())
                 return XmlDataModelSchema.this.bean;
 
             NestedStorer storer = (NestedStorer) nestedStorers.get(elemName);
             NestedCreator creator = (NestedCreator) nestedCreators.get(elemName);
 
             if((storer != null && storer.isInherited()) && (creator != null && creator.isInherited()))
                 return creator.getDeclaringClass();
 
             if((storer != null && storer.isInherited()) && (creator != null && !creator.isInherited()))
                 return creator.getDeclaringClass();
 
             if((storer != null && !storer.isInherited()) && (creator != null && creator.isInherited()))
                 return storer.getDeclaringClass();
 
             return creator != null
                    ? creator.getDeclaringClass()
                    : (storer != null ? storer.getDeclaringClass() : XmlDataModelSchema.this.bean);
         }
 
         public boolean isTemplateProducer()
         {
             return templateProducer != null;
         }
 
         public TemplateProducer getTemplateProducer()
         {
             return templateProducer;
         }
 
         public boolean isTemplateConsumer()
         {
             return TemplateConsumer.class.isAssignableFrom(elemType);
         }
 
         public ClassJavaDoc getJavaDoc()
         {
             return JavaDocs.getInstance().getClassJavaDoc(elemType);
         }
     }
 
     public class ElementDetailList extends ArrayList
     {
 
     }
 
     public ElementDetailList getNestedElementsDetail() throws DataModelException
     {
         if(nestedElementsDetail == null)
         {
             nestedElementsDetail = new ElementDetailList();
 
             TemplateProducers templateProducers = null;
             if(TemplateProducerParent.class.isAssignableFrom(getBean()))
             {
                 try
                 {
                     Object instance = getBean().newInstance();
                     templateProducers = ((TemplateProducerParent) instance).getTemplateProducers();
                 }
                 catch(InstantiationException e)
                 {
                     log.warn("Unable to create instance for template producers", e);
                 }
                 catch(IllegalAccessException e)
                 {
                     log.error("Unable to create instance for template producers", e);
                 }
             }
 
             Map childPropertyNames = getPropertyNames();
             Set sortedChildPropertyNames = new TreeSet(getNestedElements().keySet());
             if(templateProducers != null)
                 sortedChildPropertyNames.addAll(templateProducers.getElementNames());
 
             Iterator iterator = sortedChildPropertyNames.iterator();
             while(iterator.hasNext())
             {
                 String attrName = (String) iterator.next();
 
                 TemplateProducer producer = templateProducers != null ? templateProducers.get(attrName) : null;
                 if(producer != null)
                 {
                     nestedElementsDetail.add(new ElementDetail(attrName, producer));
                     continue;
                 }
 
                 if(getOptions().ignoreAttribute(attrName))
                     continue;
 
                 XmlDataModelSchema.PropertyNames attrNames = (XmlDataModelSchema.PropertyNames) childPropertyNames.get(attrName);
                 if(attrNames != null && !attrNames.isPrimaryName(attrName))
                     continue;
 
                 nestedElementsDetail.add(new ElementDetail(attrName));
             }
         }
 
         return nestedElementsDetail;
     }
 
     public class AttributeDetail
     {
         private static final String ATTRNAME_CLASS = "class";
 
         private String attrName;
         private Class attrType;
         private String primaryFlagsAttrName;
         private XdmBitmaskedFlagsAttribute flags;
         private XdmBitmaskedFlagsAttribute.FlagDefn flagAlias;
         private boolean required;
 
         public AttributeDetail(String name) throws DataModelException
         {
             if(name.equals(ATTRNAME_CLASS))
             {
                 this.attrName = name;
                 this.attrType = Class.class;
                 this.required = false;
             }
             else
             {
                 this.attrName = name;
                 this.attrType = getAttributeType(name);
                 this.required = getOptions().getRequiredAttributes().contains(name);
             }
         }
 
         public AttributeDetail(String name, XdmBitmaskedFlagsAttribute flags)
         {
             this.attrName = name;
             this.attrType = flags.getClass();
             this.flags = flags;
             this.required = getOptions().getRequiredAttributes().contains(name);
         }
 
         public AttributeDetail(String name, String primaryFlagsAttrName, XdmBitmaskedFlagsAttribute flags, XdmBitmaskedFlagsAttribute.FlagDefn flagAlias)
         {
             this.attrName = name;
             this.attrType = flags.getClass();
             this.primaryFlagsAttrName = primaryFlagsAttrName;
             this.flags = flags;
             this.flagAlias = flagAlias;
             this.required = getOptions().getRequiredAttributes().contains(name);
         }
 
         public boolean hasAccessor()
         {
             return attributeAccessors.get(attrName) != null;
         }
 
         public AttributeAccessor getAccessor()
         {
             return (AttributeAccessor) attributeAccessors.get(attrName);
         }
 
         public String getAccessorValue(Object parent, String valueIfNull) throws DataModelException, IllegalAccessException, InvocationTargetException
         {
             AttributeAccessor accessor = getAccessor();
             if(accessor == null)
                 return valueIfNull;
             Object value = accessor.get(null, parent);
             if(value == null)
                 return valueIfNull;
 
             if(value instanceof String[])
                 return TextUtils.getInstance().join((String[]) value, ", ");
 
             return value.toString();
         }
 
         public boolean isRequired()
         {
             return required;
         }
 
         public boolean isInherited()
         {
             if(attrName.equals(ATTRNAME_CLASS))
                 return true;
 
             AttributeSetter setter = (AttributeSetter) attributeSetters.get(primaryFlagsAttrName != null
                                                                             ? primaryFlagsAttrName : attrName);
             if(setter != null)
                 return setter.isInherited();
             else
                 return false;
         }
 
         public Class getDeclaringClass()
         {
             if(attrName.equals(ATTRNAME_CLASS))
                 return Object.class;
 
             AttributeSetter setter = (AttributeSetter) attributeSetters.get(primaryFlagsAttrName != null
                                                                             ? primaryFlagsAttrName : attrName);
             if(setter != null)
                 return setter.getDeclaringClass();
             else
                 return XmlDataModelSchema.this.bean;
         }
 
         public MethodJavaDoc getJavaDoc()
         {
             if(isFlagAlias())
                 return JavaDocs.getInstance().getClassJavaDoc(getBean()).getMethodDoc("set" + TextUtils.getInstance().xmlTextToJavaIdentifier(primaryFlagsAttrName, true));
             else
                 return JavaDocs.getInstance().getClassJavaDoc(getBean()).getMethodDoc("set" + TextUtils.getInstance().xmlTextToJavaIdentifier(attrName, true));
         }
 
         public boolean isFlagsPrimary()
         {
             return flags != null && flagAlias == null;
         }
 
         public boolean isFlagAlias()
         {
             return flags != null && flagAlias != null;
         }
 
         public XdmBitmaskedFlagsAttribute.FlagDefn getFlagAlias()
         {
             return flagAlias;
         }
 
         public XdmBitmaskedFlagsAttribute getFlags()
         {
             return flags;
         }
 
         public String getAttrName()
         {
             return attrName;
         }
 
         public Class getAttrType()
         {
             return attrType;
         }
 
         public String getPrimaryFlagsAttrName()
         {
             return primaryFlagsAttrName;
         }
 
         public boolean hasChoices()
         {
             if(attrType == null)
                 return false;
 
             return isFlagAlias() ||
                    isFlagsPrimary() ||
                    XdmEnumeratedAttribute.class.isAssignableFrom(attrType) ||
                    (Boolean.class.isAssignableFrom(attrType) || (attrType == boolean.class));
         }
 
         public String getChoices()
         {
             if(attrType == null)
                 return "";
 
             TextUtils textUtils = TextUtils.getInstance();
             if(isFlagAlias())
                 return textUtils.join(textUtils.getBooleanChoices(), ", ");
 
             if(isFlagsPrimary())
                 return textUtils.join(flags.getFlagNamesWithXdmAccess(), " | ");
 
             if(XdmEnumeratedAttribute.class.isAssignableFrom(attrType))
             {
                 XdmEnumeratedAttribute ea;
                 try
                 {
                     ea = (XdmEnumeratedAttribute) attrType.newInstance();
                 }
                 catch(Exception e)
                 {
                     log.error("Error retrieving enumeration data", e);
                     return e.toString();
                 }
 
                 return textUtils.join(ea.getValues(), ", ");
             }
 
             if(Boolean.class.isAssignableFrom(attrType) || (attrType == boolean.class))
                 return textUtils.join(textUtils.getBooleanChoices(), ", ");
 
             return "";
         }
     }
 
     public class AttributeDetailList extends ArrayList
     {
     }
 
     public AttributeDetailList getSettableAttributesDetail(boolean expandFlagAliases) throws IllegalAccessException, InstantiationException, InvocationTargetException, DataModelException
     {
         AttributeDetailList result = expandFlagAliases
                                      ? settableAttrsDetailWithFlagsExpanded : settableAttrsDetailWithoutFlagsExpanded;
         if(result != null)
             return result;
 
         Map childPropertyNames = getPropertyNames();
 
         Map flagSetterPrimaries = new HashMap();
         Map flagSetterAliases = new HashMap();
         for(Iterator i = getAttributes().iterator(); i.hasNext();)
         {
             String attrName = (String) i.next();
             Class attrType = getAttributeType(attrName);
             if(!XdmBitmaskedFlagsAttribute.class.isAssignableFrom(attrType))
                 continue;
 
             PropertyNames pNames = (PropertyNames) childPropertyNames.get(attrName);
             if(!pNames.isPrimaryName(attrName))
                 continue;
 
             XdmBitmaskedFlagsAttribute bfa;
             XmlDataModelSchema.NestedCreator creator = (XmlDataModelSchema.NestedCreator) getNestedCreators().get(attrName);
             if(creator != null)
             {
                 Object flagsGetterInstance;
                 try
                 {
                     flagsGetterInstance = createInstance();
                     bfa = (XdmBitmaskedFlagsAttribute) creator.create(flagsGetterInstance);
                 }
                 catch(Exception e)
                 {
                     try
                     {
                         bfa = (XdmBitmaskedFlagsAttribute) attrType.newInstance();
                     }
                     catch(Exception e1)
                     {
                         log.error(e1);
                         bfa = null;
                     }
                 }
             }
             else
                 bfa = (XdmBitmaskedFlagsAttribute) attrType.newInstance();
 
             if(bfa != null)
             {
                 flagSetterPrimaries.put(attrName, bfa);
                 if(expandFlagAliases)
                 {
                     Map xmlNodeNames = bfa.getFlagSetterXmlNodeNames();
                     for(Iterator xmliter = xmlNodeNames.keySet().iterator(); xmliter.hasNext();)
                     {
                         String xmlNodeName = (String) xmliter.next();
                         if(!childPropertyNames.containsKey(xmlNodeName))
                             flagSetterAliases.put(xmlNodeName, new Object[]{
                                 attrName, bfa, xmlNodeNames.get(xmlNodeName)
                             });
                     }
                 }
             }
         }
 
         result = new AttributeDetailList();
 
         Set sortedChildPropertyNames = new TreeSet(getAttributes());
         sortedChildPropertyNames.addAll(flagSetterAliases.keySet());
         sortedChildPropertyNames.add(AttributeDetail.ATTRNAME_CLASS);
         Iterator iterator = sortedChildPropertyNames.iterator();
         while(iterator.hasNext())
         {
             String attrName = (String) iterator.next();
 
             if(flagSetterAliases.containsKey(attrName))
             {
                 Object[] flagSetterInfo = (Object[]) flagSetterAliases.get(attrName);
                 result.add(new AttributeDetail(attrName, (String) flagSetterInfo[0], (XdmBitmaskedFlagsAttribute) flagSetterInfo[1], (XdmBitmaskedFlagsAttribute.FlagDefn) flagSetterInfo[2]));
                 continue;
             }
 
             if(flagSetterPrimaries.containsKey(attrName))
             {
                 result.add(new AttributeDetail(attrName, (XdmBitmaskedFlagsAttribute) flagSetterPrimaries.get(attrName)));
                 continue;
             }
 
             if(getOptions().ignoreAttribute(attrName))
                 continue;
 
             XmlDataModelSchema.PropertyNames attrNames = (XmlDataModelSchema.PropertyNames) childPropertyNames.get(attrName);
             if(attrNames != null && !attrNames.isPrimaryName(attrName))
                 continue;
 
             result.add(new AttributeDetail(attrName));
         }
 
         if(expandFlagAliases)
             settableAttrsDetailWithFlagsExpanded = result;
         else
             settableAttrsDetailWithoutFlagsExpanded = result;
 
         return result;
     }
 
     public Map getAttributeSetters()
     {
         return attributeSetters;
     }
 
     public Map getAttributeAccessors()
     {
         return attributeAccessors;
     }
 
     public Map getAttributeSetterMethods()
     {
         return attributeSetterMethods;
     }
 
     public Map getFlagsAttributeAccessors()
     {
         return flagsAttributeAccessors;
     }
 
     public Map getAttributeTypes()
     {
         return attributeTypes;
     }
 
     public Map getNestedTypes()
     {
         return nestedTypes;
     }
 
     public Map getNestedStorers()
     {
         return nestedStorers;
     }
 
     public Map getNestedCreators()
     {
         return nestedCreators;
     }
 
     public Map getNestedAdders()
     {
         return nestedAdders;
     }
 
     public Class getBean()
     {
         return bean;
     }
 
     public boolean isTemplateConsumer()
     {
         return TemplateConsumer.class.isAssignableFrom(bean);
     }
 
     public Object createInstance() throws IllegalAccessException, InstantiationException
     {
         return bean.newInstance();
     }
 
     private static Object getStaticFieldValue(final Class bean, final String name)
     {
         try
         {
             Field field = bean.getField(name);
             return field.get(null);
         }
         catch(NoSuchFieldException e)
         {
             return null;
         }
         catch(SecurityException e)
         {
             return null;
         }
         catch(IllegalArgumentException e)
         {
             return null;
         }
         catch(IllegalAccessException e)
         {
             return null;
         }
     }
 
     private XmlDataModelSchema(final Class bean)
     {
         propertyNames = new HashMap();
         attributeTypes = new HashMap();
         attributeSetters = new HashMap();
         attributeSetterMethods = new HashMap();
         attributeAccessors = new HashMap();
         flagsAttributeAccessors = new HashMap();
         nestedTypes = new HashMap();
         nestedCreators = new HashMap();
         nestedAltClassNameCreators = new HashMap();
         nestedAdders = new HashMap();
         nestedStorers = new HashMap();
 
         this.bean = bean;
 
         Options customOptions = (Options) getStaticFieldValue(bean, XMLDATAMODEL_SCHEMA_OPTIONS_FIELD_NAME);
         options = customOptions != null ? customOptions : new Options();
 
         Method[] methods = bean.getMethods();
         for(int i = 0; i < methods.length; i++)
         {
             final Method m = methods[i];
             final String name = m.getName();
             Class returnType = m.getReturnType();
             Class[] args = m.getParameterTypes();
 
             if(name.equals(options.pcDataHandlerMethodName)
                && java.lang.Void.TYPE.equals(returnType)
                && args.length == 1
                && java.lang.String.class.equals(args[0]))
             {
                 addText = methods[i];
             }
             else if(name.startsWith("get") && args.length == 0)
             {
                 String[] propNames = getPropertyNames(name, "get");
                 for(int pn = 0; pn < propNames.length; pn++)
                 {
                     if(propNames[pn].length() == 0)
                         continue;
 
                     AttributeAccessor aa = createAttributeAccessor(m, propNames[pn], returnType);
                     if(aa != null)
                         attributeAccessors.put(propNames[pn], aa);
                 }
             }
             else if(name.startsWith("is") && args.length == 0)
             {
                 String[] propNames = getPropertyNames(name, "is");
                 for(int pn = 0; pn < propNames.length; pn++)
                 {
                     if(propNames[pn].length() == 0)
                         continue;
 
                     AttributeAccessor aa = createAttributeAccessor(m, propNames[pn], returnType);
                     if(aa != null)
                         attributeAccessors.put(propNames[pn], aa);
                 }
             }
             else if(name.startsWith("set")
                     && java.lang.Void.TYPE.equals(returnType)
                     && args.length == 1
                     && (!args[0].isArray() || java.lang.String[].class.equals(args[0])))
             {
                 String[] propNames = getPropertyNames(name, "set");
                 for(int pn = 0; pn < propNames.length; pn++)
                 {
                     if(propNames[pn].length() == 0)
                         continue;
 
                     attributeSetterMethods.put(propNames[pn], m);
                     AttributeSetter as = createAttributeSetter(m, propNames[pn], args[0]);
                     if(as != null)
                     {
                         attributeTypes.put(propNames[pn], args[0]);
                         attributeSetters.put(propNames[pn], as);
                     }
                 }
             }
             else if(name.startsWith("create")
                     && !returnType.isArray()
                     && !returnType.isPrimitive()
                     && (args.length == 0))
             {
                 // prevent infinite recursion for nested recursive elements
                 if(!returnType.getClass().equals(bean.getClass()))
                     getSchema(returnType);
                 String[] propNames = getPropertyNames(name, "create");
                 for(int pn = 0; pn < propNames.length; pn++)
                 {
                     if(propNames[pn].length() == 0)
                         continue;
 
                     nestedTypes.put(propNames[pn], returnType);
                     nestedCreators.put(propNames[pn], new NestedCreator()
                     {
                         public Object create(Object parent) throws InvocationTargetException, IllegalAccessException
                         {
                             return m.invoke(parent, new Object[]{});
                         }
 
                         public boolean isInherited()
                         {
                             return !m.getDeclaringClass().equals(bean);
                         }
 
                         public Class getDeclaringClass()
                         {
                             return m.getDeclaringClass();
                         }
                     });
                 }
             }
             else if(name.startsWith("create")
                     && !returnType.isArray()
                     && !returnType.isPrimitive()
                     && (args.length == 1 && args[0] == Class.class))
             {
                 // prevent infinite recursion for nested recursive elements
                 if(!returnType.getClass().equals(bean.getClass()))
                     getSchema(returnType);
                 String[] propNames = getPropertyNames(name, "create");
                 for(int pn = 0; pn < propNames.length; pn++)
                 {
                     if(propNames[pn].length() == 0)
                         continue;
 
                     nestedTypes.put(propNames[pn], returnType);
                     nestedAltClassNameCreators.put(propNames[pn], new NestedAltClassCreator()
                     {
                         public Object create(Object parent, Class cls) throws InvocationTargetException, IllegalAccessException
                         {
                             return m.invoke(parent, new Object[]{cls});
                         }
 
                         public boolean isInherited()
                         {
                             return !m.getDeclaringClass().equals(bean);
                         }
 
                         public Class getDeclaringClass()
                         {
                             return m.getDeclaringClass();
                         }
                     });
                 }
             }
             else if(name.startsWith("add")
                     && java.lang.Void.TYPE.equals(returnType)
                     && args.length == 1
                     && !java.lang.String.class.equals(args[0])
                     && !args[0].isArray()
                     && !args[0].isPrimitive())
             {
                 String[] propNames = getPropertyNames(name, "add");
                 try
                 {
                     final Constructor c = args[0].getConstructor(new Class[]{});
                     for(int pn = 0; pn < propNames.length; pn++)
                     {
                         if(propNames[pn].length() == 0)
                             continue;
 
                         if(!nestedCreators.containsKey(propNames[pn]))
                         {
                             nestedTypes.put(propNames[pn], args[0]);
                             nestedCreators.put(propNames[pn], new NestedCreator()
                             {
                                 public boolean allowAlternateClass() { return false; }
 
                                 public Object create(Object parent)
                                         throws InvocationTargetException, IllegalAccessException, InstantiationException
                                 {
                                     return c.newInstance(new Object[]{});
                                 }
 
                                 public Object create(Object parent, Class cls)
                                         throws InvocationTargetException, IllegalAccessException, InstantiationException
                                 {
                                     return c.newInstance(new Object[]{cls});
                                 }
 
                                 public boolean isInherited()
                                 {
                                     return !m.getDeclaringClass().equals(bean);
                                 }
 
                                 public Class getDeclaringClass()
                                 {
                                     return m.getDeclaringClass();
                                 }
                             });
                         }
                     }
                 }
                 catch(NoSuchMethodException nse)
                 {
                     //log.warn("Unable to create nestedCreator for " + name + " " + args[0] + ", registering type only without a creator.", nse);
                     for(int pn = 0; pn < propNames.length; pn++)
                     {
                         if(propNames[pn].length() > 0)
                             nestedTypes.put(propNames[pn], args[0]);
                     }
                 }
 
                 // prevent infinite recursion for nested recursive elements
                 if(!args[0].getClass().equals(bean.getClass()))
                     getSchema(args[0]);
                 for(int pn = 0; pn < propNames.length; pn++)
                 {
                     if(propNames[pn].length() == 0)
                         continue;
 
                     nestedStorers.put(propNames[pn], new NestedStorer()
                     {
                         public void store(Object parent, Object child)
                                 throws InvocationTargetException, IllegalAccessException
                         {
                             m.invoke(parent, new Object[]{child});
                         }
 
                         public boolean isInherited()
                         {
                             return !m.getDeclaringClass().equals(bean);
                         }
 
                         public Class getDeclaringClass()
                         {
                             return m.getDeclaringClass();
                         }
                     });
                 }
             }
         }
     }
 
     /**
      * Sets the named attribute.
      */
     public void setAttribute(XdmParseContext pc, Object element, String attributeName, String value, boolean withinCustom) throws DataModelException, UnsupportedAttributeException
     {
         boolean hasCustom = element instanceof CustomElementAttributeSetter;
         AttributeSetter as = (AttributeSetter) attributeSetters.get(attributeName);
         if(as == null && (withinCustom || !hasCustom))
         {
             // see if we're trying to set a named flag
             for(Iterator i = flagsAttributeAccessors.entrySet().iterator(); i.hasNext();)
             {
                 Map.Entry entry = (Map.Entry) i.next();
                 AttributeAccessor accessor = (AttributeAccessor) entry.getValue();
                 Object returnVal = null;
                 try
                 {
                     returnVal = accessor.get(pc, element);
                 }
                 catch(Exception e)
                 {
                     pc.addError("Unable to set attribute '" + attributeName + "' to '" + value + "' at " + pc.getLocator().getSystemId() +
                                 " line " + pc.getLocator().getLineNumber() + ": " + e.getMessage());
                     log.error(e);
                     if(pc.isThrowErrorException())
                         throw new DataModelException(pc, e);
                 }
 
                 if(returnVal instanceof XdmBitmaskedFlagsAttribute)
                 {
                     XdmBitmaskedFlagsAttribute bfa = (XdmBitmaskedFlagsAttribute) returnVal;
                     if(bfa.updateFlag(attributeName, TextUtils.getInstance().toBoolean(value)))
                         return;
                 }
             }
 
             UnsupportedAttributeException e = new UnsupportedAttributeException(pc, element, attributeName);
             pc.addError(e);
             if(pc.isThrowErrorException())
                 throw e;
             else
                 return;
         }
         try
         {
             if(as == null && (!withinCustom && hasCustom))
                 ((CustomElementAttributeSetter) element).setCustomDataModelElementAttribute(pc, this, element, attributeName, value);
             else
                 as.set(pc, element, value);
         }
         catch(InvocationTargetException ite)
         {
             pc.addError("Unable to set attribute '" + attributeName + "' to '" + value + "' using " + as.getDeclaringClass() + " at " + pc.getLocator().getSystemId() +
                        " line " + pc.getLocator().getLineNumber() + ": " + ite.getTargetException().getMessage());
             log.error(ite.getTargetException());
             if(pc.isThrowErrorException())
             {
                 Throwable t = ite.getTargetException();
                 if(t instanceof DataModelException)
                 {
                     throw (DataModelException) t;
                 }
                 throw new DataModelException(pc, t);
             }
         }
         catch(Exception e)
         {
             pc.addError("Unable to set attribute '" + attributeName + "' to '" + value + "' at " + pc.getLocator().getSystemId() +
                         " line " + pc.getLocator().getLineNumber() + ": " + e.getMessage());
             log.error(e);
             if(pc.isThrowErrorException())
                 throw new DataModelException(pc, e);
         }
     }
 
     /**
      * Adds PCDATA areas.
      */
     public void addText(XdmParseContext pc, Object element, String text) throws DataModelException, UnsupportedTextException
     {
         if(options.ignorePcData) return;
 
         if(addText == null)
         {
             UnsupportedTextException e = new UnsupportedTextException(pc, element, text);
             pc.addError(e);
             if(pc.isThrowErrorException())
                 throw e;
             else
                 return;
         }
         try
         {
             addText.invoke(element, new String[]{text});
         }
         catch(InvocationTargetException ite)
         {
             pc.addError("Unable to add text '" + text + "' at " + pc.getLocator().getSystemId() + " line " + pc.getLocator().getLineNumber() + ": " + ite.getMessage());
             log.error(ite);
             if(pc.isThrowErrorException())
             {
                 Throwable t = ite.getTargetException();
                 if(t instanceof DataModelException)
                 {
                     throw (DataModelException) t;
                 }
                 throw new DataModelException(pc, t);
             }
         }
         catch(Exception e)
         {
             pc.addError("Unable to add text '" + text + "' at " + pc.getLocator().getSystemId() + " line " + pc.getLocator().getLineNumber() + ": " + e.getMessage());
             log.error(e);
             if(pc.isThrowErrorException())
                 throw new DataModelException(pc, e);
         }
     }
 
     protected Object createElement(XdmParseContext pc, String alternateClassName, Object element, String elementName) throws DataModelException, UnsupportedElementException
     {
         try
         {
             if(alternateClassName != null)
             {
                 Class cls = null;
                 try
                 {
                     cls = Class.forName(alternateClassName);
                 }
                 catch(ClassNotFoundException e)
                 {
                     pc.addError("Class '" + alternateClassName + "' for element '" + elementName + "' not found at " + pc.getLocator().getSystemId() +
                                 " line " + pc.getLocator().getLineNumber() + ". " + e.getMessage());
                     log.error(e);
                     if(pc.isThrowErrorException())
                         throw new DataModelException(pc, e);
                     else
                     {
                         NestedCreator nc = (NestedCreator) nestedCreators.get(elementName);
                         if(nc != null)
                             return nc.create(element);
                     }
                 }
 
                 NestedAltClassCreator nac = (NestedAltClassCreator) nestedAltClassNameCreators.get(elementName);
                 if(nac != null)
                     return nac.create(element, cls);
                 else
                 {
                     // check to make sure that either a storer or creator is available to ensure it's a valid tag
                     if(nestedCreators.get(elementName) != null || nestedStorers.get(elementName) != null)
                         return cls.newInstance();
 
                     UnsupportedElementException e = new UnsupportedElementException(this, pc, element, elementName);
                     if(pc != null)
                     {
                         pc.addError(e);
                         if(pc.isThrowErrorException())
                             throw e;
                         else
                             return null;
                     }
                     else
                         return null;
                 }
             }
             else
             {
                 NestedCreator nc = (NestedCreator) nestedCreators.get(elementName);
                 if(nc != null)
                     return nc.create(element);
             }
         }
         catch(InvocationTargetException ite)
         {
             pc.addError("Could not create class '" + alternateClassName + "' for element '" + elementName + "' at " + pc.getLocator().getSystemId() +
                         " line " + pc.getLocator().getLineNumber() + ": " + ite.getMessage());
             log.error(ite);
             if(pc.isThrowErrorException())
             {
                 Throwable t = ite.getTargetException();
                 if(t instanceof DataModelException)
                 {
                     throw (DataModelException) t;
                 }
                 throw new DataModelException(pc, t);
             }
         }
         catch(Exception e)
         {
             pc.addError("Could not create class '" + alternateClassName + "' for element '" + elementName + "' at " + pc.getLocator().getSystemId() +
                         " line " + pc.getLocator().getLineNumber() + ": " + e.getMessage());
             log.error(e);
             if(pc.isThrowErrorException())
                 throw new DataModelException(pc, e);
         }
 
         // if the element is being defined as a sub-element but has an attribute of the same name, it's a convenience attribute setter
         AttributeSetter as = (AttributeSetter) attributeSetters.get(elementName);
         if(as != null)
             return element;
         else
         {
             // see if we're trying to set a named flag as a sub-element
             for(Iterator i = flagsAttributeAccessors.entrySet().iterator(); i.hasNext();)
             {
                 Map.Entry entry = (Map.Entry) i.next();
                 AttributeAccessor accessor = (AttributeAccessor) entry.getValue();
                 Object returnVal = null;
                 try
                 {
                     returnVal = accessor.get(pc, element);
                 }
                 catch(Exception e)
                 {
                 }
 
                 if(returnVal instanceof XdmBitmaskedFlagsAttribute)
                     return element;
             }
 
             UnsupportedElementException e = new UnsupportedElementException(this, pc, element, elementName);
             if(pc != null)
             {
                 pc.addError(e);
                 if(pc.isThrowErrorException())
                     throw e;
                 else
                     return null;
             }
             else
                 return null;
         }
     }
 
     /**
      * Creates a named nested element.
      */
     public Object createElement(XdmParseContext pc, String alternateClassName, Object element, String elementName, boolean withinCustom) throws DataModelException, UnsupportedElementException
     {
         //System.out.println("Creating: " + element.getClass().getName() + " " + elementName + " " + alternateClassName + " " + withinCustom);
 
         try
         {
             if(element instanceof CustomElementCreator && !withinCustom)
                 return ((CustomElementCreator) element).createCustomDataModelElement(pc, this, element, elementName, alternateClassName);
             else
                 return createElement(pc, alternateClassName, element, elementName);
         }
         catch(InvocationTargetException ite)
         {
             pc.addError("Could not create class for element '" + elementName + "' at " + pc.getLocator().getSystemId() +
                         " line " + pc.getLocator().getLineNumber() + ": " + ite.getMessage());
             log.error(ite);
             if(pc.isThrowErrorException())
             {
                 Throwable t = ite.getTargetException();
                 if(t instanceof DataModelException)
                 {
                     throw (DataModelException) t;
                 }
                 throw new DataModelException(pc, t);
             }
             else
                 return null;
         }
         catch(Exception e)
         {
             pc.addError("Could not create class for element '" + elementName + "' at " + pc.getLocator().getSystemId() +
                         " line " + pc.getLocator().getLineNumber() + ": " + e.getMessage());
             log.error(e);
             if(pc.isThrowErrorException())
                 throw new DataModelException(pc, e);
             return null;
         }
     }
 
     public void storeElement(XdmParseContext pc, Object element, Object child, String elementName, boolean withinCustom) throws DataModelException
     {
         if(elementName == null)
             return;
 
         NestedStorer ns = (NestedStorer) nestedStorers.get(elementName);
         try
         {
             if(ns == null && (!withinCustom && element instanceof CustomElementStorer))
                 ((CustomElementStorer) element).storeCustomDataModelElement(pc, this, element, child, elementName);
             else if(ns != null)
                 ns.store(element, child);
         }
         catch(InvocationTargetException ite)
         {
             pc.addError("Could not store data for for element '" + elementName + "' at " + pc.getLocator().getSystemId() +
                         " line " + pc.getLocator().getLineNumber() + ": " + ite.getMessage());
             log.error(ite);
             if(pc.isThrowErrorException())
             {
                 Throwable t = ite.getTargetException();
                 if(t instanceof DataModelException)
                 {
                     throw (DataModelException) t;
                 }
                 throw new DataModelException(pc, t);
             }
         }
         catch(Exception e)
         {
             pc.addError("Could not store data for for element '" + elementName + "' at " + pc.getLocator().getSystemId() +
                         " line " + pc.getLocator().getLineNumber() + ": " + e.getMessage());
             log.error(e);
             if(pc.isThrowErrorException())
                 throw new DataModelException(pc, e);
         }
     }
 
     public void finalizeElementConstruction(XdmParseContext pc, Object element, String elementName) throws DataModelException
     {
         if(element instanceof ConstructionFinalizeListener)
             ((ConstructionFinalizeListener) element).finalizeConstruction(pc, element, elementName);
     }
 
     public boolean assignInstanceValue(final Object element, String key, final Object value, final boolean required) throws IllegalAccessException, InvocationTargetException, DataModelException
     {
         key = key.toLowerCase();
         Method method = (Method) getAttributeSetterMethods().get(key);
         if(method != null)
         {
             Class[] args = method.getParameterTypes();
             if(args.length == 1)
             {
                 XmlDataModelSchema.AttributeSetter as = (XmlDataModelSchema.AttributeSetter) getAttributeSetters().get(key);
                 if(as != null)
                 {
                     as.set(null, element, value == null ? null : value.toString());
                     return true;
                 }
                 else
                 {
                     try
                     {
                         method.invoke(element, new Object[]{value});
                         return true;
                     }
                     catch(Exception e)
                     {
                         log.error("Attempting to assign '" + key + "' to a method in '" + element.getClass() + "' but no attribute setter was found and unable to call setXXX(Object).", e);
                     }
                 }
             }
             else if(required && log.isErrorEnabled())
                 log.error("Attempting to assign '" + key + "' to a method in '" + element.getClass() + "' but the method has more than one argument.");
         }
         else if(required && log.isErrorEnabled())
             log.error("Attempting to assign '" + key + "' to a method in '" + element.getClass() + "' but there is no mutator available.");
 
         // if we get to here, we couldn't find the method to assign the value to
         return false;
     }
 
     public boolean assignMapValue(final Object element, final Map map, String key, final String defaultValue) throws IllegalAccessException, InvocationTargetException, DataModelException
     {
         Object value = map.get(key);
 
         boolean required = false;
         if(key.endsWith("!"))
         {
             required = true;
             key = key.substring(0, key.length() - 1);
         }
 
         if(value == null)
         {
             if(required)
                 throw new RuntimeException("Map key '" + key + "' is required but not available.");
 
             value = defaultValue;
         }
 
         return assignInstanceValue(element, key, value, required);
     }
 
     /**
      * Given a Map, assign its current values using appropriate accessor methods of the instance object
      * (using Java reflection).
      *
      * @param element The object who's mutator methods should be matched
      * @param map     The Map for which the key is a setXXX(YYY) call [XXX is the key] and values are the YYY parameter
      * @param keys    The names of the keys that should be assigned to the mutators of the instance object.
      *                This may be '*' (for all keys in the map) or a comma-separated list of names. The parameter names
      *                may optionally be followed by an '=' to indicate a default value for the parameter. Parameter
      *                names may optionally be terminated with an '!' to indicate that they are required (an exception
      *                is thrown if the parameter is unavailable. For example, "a,b!,c" would mean that parameter
      *                'a', 'b' and 'c' should be assigned using setA(), setB() and setC() if available but an
      *                exception should be thrown if 'b' is not available as a request parameter.
      */
 
     public void assignMapValues(Object element, Map map, String keys) throws IllegalAccessException, DataModelException, InvocationTargetException
     {
         if(keys.equals("*"))
         {
             for(Iterator i = map.keySet().iterator(); i.hasNext();)
             {
                 String key = (String) i.next();
                 assignMapValue(element, map, key, null);
             }
         }
         else
         {
             String[] keysArray = TextUtils.getInstance().split(keys, ",", true);
             for(int i = 0; i < keysArray.length; i++)
             {
                 String key = keysArray[i];
                 int defaultValuePos = key.indexOf('=');
                 if(defaultValuePos > 0)
                     assignMapValue(element, map, key.substring(0, defaultValuePos), key.substring(defaultValuePos + 1));
                 else
                     assignMapValue(element, map, key, null);
             }
         }
     }
 
     /**
      * returns the type of a named nested element.
      */
     public Class getElementType(String elementName)
             throws DataModelException
     {
         Class nt = options.getSubclassedItemClass(elementName, (Class) nestedTypes.get(elementName));
         if(nt == null)
         {
             String msg = "Class " + bean.getName() + " doesn't support the nested \"" + elementName + "\" element.";
             throw new DataModelException(null, msg);
         }
         return nt;
     }
 
     /**
      * returns the type of a named attribute.
      */
     public Class getAttributeType(String attributeName)
             throws DataModelException
     {
         Class at = options.getSubclassedItemClass(attributeName, (Class) attributeTypes.get(attributeName));
         if(at == null)
         {
             String msg = "Class " + bean.getName() + " doesn't support the \"" + attributeName + "\" attribute.";
             throw new DataModelException(null, msg);
         }
         return at;
     }
 
     /**
      * Does the introspected class ignore PCDATA
      */
     public boolean isIgnoreText()
     {
         return options.ignorePcData;
     }
 
     /**
      * Does the introspected class support PCDATA?
      */
     public boolean supportsCharacters()
     {
         return !options.ignorePcData && addText != null;
     }
 
     /**
      * Does the introspected class ignore PCDATA
      */
     public boolean hasAddTextMethod()
     {
         return addText != null;
     }
 
     /**
      * Return all attribues supported by the introspected class.
      */
     public Set getAttributes()
     {
         return attributeSetters.keySet();
     }
 
     /**
      * Return all nested elements supported by the introspected class.
      */
     public Map getNestedElements()
     {
         return nestedTypes;
     }
 
     /**
      * Create a proper implementation of AttributeAccessor for the given
      * attribute type.
      */
     private AttributeAccessor createAttributeAccessor(final Method m, final String attrName, final Class arg)
     {
         AttributeAccessor result = new AttributeAccessor()
         {
             public Object get(XdmParseContext pc, Object parent)
                     throws InvocationTargetException, IllegalAccessException
             {
                 return m.invoke(parent, null);
             }
 
             public boolean isInherited()
             {
                 return !m.getDeclaringClass().equals(bean);
             }
 
             public Class getDeclaringClass()
             {
                 return m.getDeclaringClass();
             }
         };
 
         if(XdmBitmaskedFlagsAttribute.class.isAssignableFrom(arg))
             flagsAttributeAccessors.put(attrName, result);
 
         return result;
     }
 
     /**
      * Create a proper implementation of AttributeSetter for the given
      * attribute type.
      */
     private AttributeSetter createAttributeSetter(final Method m, final String attrName, final Class arg)
     {
         if(java.lang.String[].class.equals(arg))
         {
             return new AttributeSetter()
             {
                 public void set(XdmParseContext pc, Object parent, String value)
                         throws InvocationTargetException, IllegalAccessException
                 {
                     m.invoke(parent, new Object[]{TextUtils.getInstance().split(value, ",", true)});
                 }
 
                 public boolean isInherited()
                 {
                     return !m.getDeclaringClass().equals(bean);
                 }
 
                 public Class getDeclaringClass()
                 {
                     return m.getDeclaringClass();
                 }
             };
         }
         else if(java.lang.String.class.equals(arg))
         {
             return new AttributeSetter()
             {
                 public void set(XdmParseContext pc, Object parent, String value)
                         throws InvocationTargetException, IllegalAccessException
                 {
                     m.invoke(parent, new String[]{value});
                 }
 
                 public boolean isInherited()
                 {
                     return !m.getDeclaringClass().equals(bean);
                 }
 
                 public Class getDeclaringClass()
                 {
                     return m.getDeclaringClass();
                 }
             };
         }
         else if(java.lang.Character.class.equals(arg)
                 || java.lang.Character.TYPE.equals(arg))
         {
             return new AttributeSetter()
             {
                 public void set(XdmParseContext pc, Object parent, String value)
                         throws InvocationTargetException, IllegalAccessException
                 {
                     m.invoke(parent, new Character[]{new Character(value.charAt(0))});
                 }
 
                 public boolean isInherited()
                 {
                     return !m.getDeclaringClass().equals(bean);
                 }
 
                 public Class getDeclaringClass()
                 {
                     return m.getDeclaringClass();
                 }
             };
         }
         else if(java.lang.Byte.TYPE.equals(arg))
         {
             return new AttributeSetter()
             {
                 public void set(XdmParseContext pc, Object parent, String value)
                         throws InvocationTargetException, IllegalAccessException
                 {
                     m.invoke(parent, new Byte[]{new Byte(value)});
                 }
 
                 public boolean isInherited()
                 {
                     return !m.getDeclaringClass().equals(bean);
                 }
 
                 public Class getDeclaringClass()
                 {
                     return m.getDeclaringClass();
                 }
             };
         }
         else if(java.lang.Short.TYPE.equals(arg))
         {
             return new AttributeSetter()
             {
                 public void set(XdmParseContext pc, Object parent, String value)
                         throws InvocationTargetException, IllegalAccessException
                 {
                     m.invoke(parent, new Short[]{new Short(value)});
                 }
 
                 public boolean isInherited()
                 {
                     return !m.getDeclaringClass().equals(bean);
                 }
 
                 public Class getDeclaringClass()
                 {
                     return m.getDeclaringClass();
                 }
             };
         }
         else if(java.lang.Integer.TYPE.equals(arg))
         {
             return new AttributeSetter()
             {
                 public void set(XdmParseContext pc, Object parent, String value)
                         throws InvocationTargetException, IllegalAccessException
                 {
                     m.invoke(parent, new Integer[]{new Integer(value)});
                 }
 
                 public boolean isInherited()
                 {
                     return !m.getDeclaringClass().equals(bean);
                 }
 
                 public Class getDeclaringClass()
                 {
                     return m.getDeclaringClass();
                 }
             };
         }
         else if(java.lang.Long.TYPE.equals(arg))
         {
             return new AttributeSetter()
             {
                 public void set(XdmParseContext pc, Object parent, String value)
                         throws InvocationTargetException, IllegalAccessException
                 {
                     m.invoke(parent, new Long[]{new Long(value)});
                 }
 
                 public boolean isInherited()
                 {
                     return !m.getDeclaringClass().equals(bean);
                 }
 
                 public Class getDeclaringClass()
                 {
                     return m.getDeclaringClass();
                 }
             };
         }
         else if(java.lang.Float.TYPE.equals(arg))
         {
             return new AttributeSetter()
             {
                 public void set(XdmParseContext pc, Object parent, String value)
                         throws InvocationTargetException, IllegalAccessException
                 {
                     m.invoke(parent, new Float[]{new Float(value)});
                 }
 
                 public boolean isInherited()
                 {
                     return !m.getDeclaringClass().equals(bean);
                 }
 
                 public Class getDeclaringClass()
                 {
                     return m.getDeclaringClass();
                 }
             };
         }
         else if(java.lang.Double.TYPE.equals(arg))
         {
             return new AttributeSetter()
             {
                 public void set(XdmParseContext pc, Object parent, String value)
                         throws InvocationTargetException, IllegalAccessException
                 {
                     m.invoke(parent, new Double[]{new Double(value)});
                 }
 
                 public boolean isInherited()
                 {
                     return !m.getDeclaringClass().equals(bean);
                 }
 
                 public Class getDeclaringClass()
                 {
                     return m.getDeclaringClass();
                 }
             };
         }
         // boolean gets an extra treatment, because we have a nice method
         else if(java.lang.Boolean.class.equals(arg)
                 || java.lang.Boolean.TYPE.equals(arg))
         {
             return new AttributeSetter()
             {
                 public void set(XdmParseContext pc, Object parent, String value)
                         throws InvocationTargetException, IllegalAccessException
                 {
                     m.invoke(parent, new Boolean[]{new Boolean(TextUtils.getInstance().toBoolean(value))});
                 }
 
                 public boolean isInherited()
                 {
                     return !m.getDeclaringClass().equals(bean);
                 }
 
                 public Class getDeclaringClass()
                 {
                     return m.getDeclaringClass();
                 }
             };
         }
         // Class doesn't have a String constructor but a decent factory method
         else if(java.lang.Class.class.equals(arg))
         {
             return new AttributeSetter()
             {
                 public void set(XdmParseContext pc, Object parent, String value)
                         throws InvocationTargetException, IllegalAccessException, DataModelException
                 {
                     try
                     {
                         m.invoke(parent, new Class[]{Class.forName(value)});
                     }
                     catch(ClassNotFoundException ce)
                     {
                         if(pc != null)
                         {
                             DataModelException dme = new DataModelException(pc, ce);
                             pc.addError(dme);
                             if(pc.isThrowErrorException())
                                 throw dme;
                         }
                         else
                             log.error(ce);
                     }
                 }
 
                 public boolean isInherited()
                 {
                     return !m.getDeclaringClass().equals(bean);
                 }
 
                 public Class getDeclaringClass()
                 {
                     return m.getDeclaringClass();
                 }
             };
         }
         else if(java.io.File.class.equals(arg))
         {
             return new AttributeSetter()
             {
                 public void set(XdmParseContext pc, Object parent, String value)
                         throws InvocationTargetException, IllegalAccessException
                 {
                     // resolve relative paths through DataModel
                     m.invoke(parent, new File[]{pc != null ? pc.resolveFile(value) : new File(value)});
                 }
 
                 public boolean isInherited()
                 {
                     return !m.getDeclaringClass().equals(bean);
                 }
 
                 public Class getDeclaringClass()
                 {
                     return m.getDeclaringClass();
                 }
             };
         }
         else if(RedirectValueSource.class.isAssignableFrom(arg))
         {
             return new AttributeSetter()
             {
                 public void set(XdmParseContext pc, Object parent, String value)
                         throws InvocationTargetException, IllegalAccessException
                 {
                     TextUtils textUtils = TextUtils.getInstance();
                     ValueSource vs = ValueSources.getInstance().getValueSourceOrStatic(value);
                     if(vs == null)
                     {
                         // better to throw an error here since if there are objects which are based on null/non-null
                         // value of the value source, it is easier to debug
                         pc.addError("Unable to find ValueSource '" + value + "' to wrap in RedirectValueSource at " + pc.getLocator().getSystemId() +
                                     " line " + pc.getLocator().getLineNumber() + ". Valid value sources are: " + textUtils.join(ValueSources.getInstance().getAllValueSourceIdentifiers(), ", "));
                     }
                     try
                     {
                         RedirectValueSource redirectValueSource = (RedirectValueSource) arg.newInstance();
                         redirectValueSource.setValueSource(vs);
                         m.invoke(parent, new RedirectValueSource[]{redirectValueSource});
                     }
                     catch(InstantiationException e)
                     {
                         pc.addError("Unable to create RedirectValueSource for '" + value + "' at " + pc.getLocator().getSystemId() +
                                     " line " + pc.getLocator().getLineNumber() + ". Valid value sources are: " + textUtils.join(ValueSources.getInstance().getAllValueSourceIdentifiers(), ", "));
                     }
                 }
 
                 public boolean isInherited()
                 {
                     return !m.getDeclaringClass().equals(bean);
                 }
 
                 public Class getDeclaringClass()
                 {
                     return m.getDeclaringClass();
                 }
             };
         }
         else if(ValueSource.class.equals(arg))
         {
             return new AttributeSetter()
             {
                 public void set(XdmParseContext pc, Object parent, String value)
                         throws InvocationTargetException, IllegalAccessException
                 {
                     TextUtils textUtils = TextUtils.getInstance();
                     ValueSource vs = ValueSources.getInstance().getValueSourceOrStatic(value);
                     if(vs == null)
                     {
                         // better to throw an error here since if there are objects which are based on null/non-null
                         // value of the value source, it is easier to debug
                         if(pc != null)
                             pc.addError("Unable to create ValueSource for '" + value + "' at " + pc.getLocator().getSystemId() +
                                         " line " + pc.getLocator().getLineNumber() + ". Valid value sources are: " + textUtils.join(ValueSources.getInstance().getAllValueSourceIdentifiers(), ", "));
                         else
                             log.error("Unable to create ValueSource for '" + value + ". Valid value sources are: " + textUtils.join(ValueSources.getInstance().getAllValueSourceIdentifiers(), ", "));
                     }
                     m.invoke(parent, new ValueSource[]{vs});
                 }
 
                 public boolean isInherited()
                 {
                     return !m.getDeclaringClass().equals(bean);
                 }
 
                 public Class getDeclaringClass()
                 {
                     return m.getDeclaringClass();
                 }
             };
         }
         else if(Command.class.isAssignableFrom(arg))
         {
             return new AttributeSetter()
             {
                 public void set(XdmParseContext pc, Object parent, String value)
                         throws InvocationTargetException, IllegalAccessException, DataModelException
                 {
                     try
                     {
                         m.invoke(parent, new Command[]{Commands.getInstance().getCommand(value)});
                     }
                     catch(CommandNotFoundException e)
                     {
                         if(pc != null)
                         {
                             pc.addError("Unable to create Command for '" + value + "' at " + pc.getLocator().getSystemId() + " line " + pc.getLocator().getLineNumber() + ".");
                             if(pc.isThrowErrorException())
                                 throw new DataModelException(pc, e);
                         }
                         else
                             log.error("Unable to create Command for '" + value + "'", e);
                     }
                 }
 
                 public boolean isInherited()
                 {
                     return !m.getDeclaringClass().equals(bean);
                 }
 
                 public Class getDeclaringClass()
                 {
                     return m.getDeclaringClass();
                 }
             };
         }
         else if(XdmEnumeratedAttribute.class.isAssignableFrom(arg))
         {
             return new AttributeSetter()
             {
                 public void set(XdmParseContext pc, Object parent, String value)
                         throws InvocationTargetException, IllegalAccessException, DataModelException
                 {
                     try
                     {
                         XdmEnumeratedAttribute ea = (XdmEnumeratedAttribute) arg.newInstance();
                         ea.setValue(pc, parent, attrName, value);
                         m.invoke(parent, new XdmEnumeratedAttribute[]{ea});
                     }
                     catch(InstantiationException ie)
                     {
                         pc.addError(ie);
                         if(pc.isThrowErrorException())
                             throw new DataModelException(pc, ie);
                     }
                 }
 
                 public boolean isInherited()
                 {
                     return !m.getDeclaringClass().equals(bean);
                 }
 
                 public Class getDeclaringClass()
                 {
                     return m.getDeclaringClass();
                 }
             };
         }
         else if(XdmBitmaskedFlagsAttribute.class.isAssignableFrom(arg))
         {
             return new AttributeSetter()
             {
                 public void set(XdmParseContext pc, Object parent, String value)
                         throws InvocationTargetException, IllegalAccessException, DataModelException
                 {
                     try
                     {
                         XdmBitmaskedFlagsAttribute bfa;
                         NestedCreator creator = (NestedCreator) nestedCreators.get(attrName);
                         if(creator != null)
                             bfa = (XdmBitmaskedFlagsAttribute) creator.create(parent);
                         else
                             bfa = (XdmBitmaskedFlagsAttribute) arg.newInstance();
                         bfa.setValue(pc, parent, attrName, value);
                         m.invoke(parent, new XdmBitmaskedFlagsAttribute[]{bfa});
                     }
                     catch(InstantiationException ie)
                     {
                         pc.addError(ie);
                         if(pc.isThrowErrorException())
                             throw new DataModelException(pc, ie);
                     }
                 }
 
                 public boolean isInherited()
                 {
                     return !m.getDeclaringClass().equals(bean);
                 }
 
                 public Class getDeclaringClass()
                 {
                     return m.getDeclaringClass();
                 }
             };
         }
         else if(Locale.class.isAssignableFrom(arg))
         {
             return new AttributeSetter()
             {
                 public void set(XdmParseContext pc, Object parent, String value)
                         throws InvocationTargetException, IllegalAccessException, DataModelException
                 {
                     String[] items = TextUtils.getInstance().split(value, ",", true);
                     switch(items.length)
                     {
                         case 1:
                             m.invoke(parent, new Locale[]{new Locale(items[0], "")});
                             break;
 
                         case 2:
                             m.invoke(parent, new Locale[]{new Locale(items[1], items[2])});
                             break;
 
                         case 3:
                             m.invoke(parent, new Locale[]{new Locale(items[1], items[2], items[3])});
                             break;
 
                         case 4:
                             if(pc != null)
                                 throw new DataModelException(pc, "Too many items in Locale constructor.");
                             else
                                 log.error("Too many items in Locale constructor: " + value);
                     }
                 }
 
                 public boolean isInherited()
                 {
                     return !m.getDeclaringClass().equals(bean);
                 }
 
                 public Class getDeclaringClass()
                 {
                     return m.getDeclaringClass();
                 }
             };
         }
         else if(ResourceBundle.class.isAssignableFrom(arg))
         {
             return new AttributeSetter()
             {
                 public void set(XdmParseContext pc, Object parent, String value)
                         throws InvocationTargetException, IllegalAccessException, DataModelException
                 {
                     String[] items = TextUtils.getInstance().split(value, ",", true);
                     switch(items.length)
                     {
                         case 1:
                             m.invoke(parent, new ResourceBundle[]{ResourceBundle.getBundle(items[0])});
                             break;
 
                         case 2:
                             m.invoke(parent, new ResourceBundle[]{
                                 ResourceBundle.getBundle(items[0], new Locale(items[1], Locale.US.getCountry()))
                             });
                             break;
 
                         case 3:
                             m.invoke(parent, new ResourceBundle[]{
                                 ResourceBundle.getBundle(items[0], new Locale(items[1], items[2]))
                             });
                             break;
 
                         case 4:
                             m.invoke(parent, new ResourceBundle[]{
                                 ResourceBundle.getBundle(items[0], new Locale(items[1], items[2], items[3]))
                             });
 
                         default:
                             if(pc != null)
                                 throw new DataModelException(pc, "Too many items in ResourceBundle constructor.");
                             else
                                 log.error("Too many items in Locale constructor: " + value);
 
                     }
                 }
 
                 public boolean isInherited()
                 {
                     return !m.getDeclaringClass().equals(bean);
                 }
 
                 public Class getDeclaringClass()
                 {
                     return m.getDeclaringClass();
                 }
             };
         }
         else if(Properties.class.isAssignableFrom(arg))
         {
             return new AttributeSetter()
             {
                 public void set(XdmParseContext pc, Object parent, String value)
                         throws InvocationTargetException, IllegalAccessException, DataModelException
                 {
                     // when specifying properties the following are valid
                     // <xxx properties="abc.properties">  <!-- load this property file, throw exception if not found -->
                     // <xxx properties="abc.properties:optional">  <!-- load this property file, no exception if not found -->
                     // <xxx properties="/a/b/abc.properties,/x/y/def.properties"> <!-- load the first property file found, throw exception if none found -->
                     // <xxx properties="/a/b/abc.properties,/x/y/def.properties:optional"> <!-- load the first property file found, no exception if none found -->
 
                     final TextUtils textUtils = TextUtils.getInstance();
                     final String[] options = textUtils.split(value, ":", true);
                     final String[] fileNames = textUtils.split(value, ",", true);
                     final Properties properties;
                     switch(options.length)
                     {
                         case 1:
                             properties = PropertiesLoader.loadProperties(fileNames, true, false);
                             m.invoke(parent, new Properties[]{properties});
                             break;
 
                         case 2:
                             properties = PropertiesLoader.loadProperties(fileNames, options[1].equals("optional")
                                                                                     ? false : true, false);
                             if(properties != null)
                                 m.invoke(parent, new Properties[]{properties});
                             break;
 
                         default:
                             if(pc != null)
                                 throw new DataModelException(pc, "Don't know how to get properties from PropertiesLoader: " + value);
                             else
                                 log.error("Don't know how to get properties from PropertiesLoader:" + value);
                     }
                 }
 
                 public boolean isInherited()
                 {
                     return !m.getDeclaringClass().equals(bean);
                 }
 
                 public Class getDeclaringClass()
                 {
                     return m.getDeclaringClass();
                 }
             };
         }
         else
         {
             // worst case. look for a public String constructor and use it
             try
             {
                 final Constructor c = arg.getConstructor(new Class[]{java.lang.String.class});
                 return new AttributeSetter()
                 {
                     public void set(XdmParseContext pc, Object parent, String value)
                             throws InvocationTargetException, IllegalAccessException, DataModelException
                     {
                         try
                         {
                             Object attribute = c.newInstance(new String[]{value});
                             m.invoke(parent, new Object[]{attribute});
                         }
                         catch(InstantiationException ie)
                         {
                             if(pc != null)
                             {
                                 pc.addError(ie);
                                 if(pc.isThrowErrorException())
                                     throw new DataModelException(pc, ie);
                             }
                             else
                                 log.error(ie);
                         }
                     }
 
                     public boolean isInherited()
                     {
                         return !m.getDeclaringClass().equals(bean);
                     }
 
                     public Class getDeclaringClass()
                     {
                         return m.getDeclaringClass();
                     }
                 };
             }
             catch(NoSuchMethodException nme)
             {
             }
         }
 
         return null;
     }
 
     public class PropertyNames
     {
         private String primaryName;
         private Set aliases = new HashSet();
 
         public PropertyNames(String methodName, String prefix)
         {
             int start = prefix.length();
 
             String nameMinusPrefix = methodName.substring(start);
             String xmlNodeName = TextUtils.getInstance().javaIdentifierToXmlNodeName(nameMinusPrefix);
 
             primaryName = xmlNodeName.toLowerCase();
             aliases.add(primaryName);
 
             if(options.isGenerateAutoAliases() && !xmlNodeName.equals(nameMinusPrefix))
                 aliases.add(nameMinusPrefix.toLowerCase());
 
             // map all the property names to this object
             for(Iterator i = aliases.iterator(); i.hasNext();)
             {
                 String name = (String) i.next();
                 propertyNames.put(name, this);
             }
 
             Map customAliasesMap = options.getAliases();
             String[] allNames = getAllNames();
             for(int i = 0; i < allNames.length; i++)
             {
                 String name = allNames[i];
                 Set customAliases = (Set) customAliasesMap.get(name);
                 if(customAliases != null)
                     aliases.addAll(customAliases);
             }
 
             options.setPropertyNames(this);
         }
 
         public String getPrimaryName()
         {
             return primaryName;
         }
 
         public boolean isPrimaryName(String name)
         {
             return primaryName.equals(name);
         }
 
         public Set getAliases()
         {
             return aliases;
         }
 
         public String[] getAllNames()
         {
             return (String[]) aliases.toArray(new String[aliases.size()]);
         }
 
         /**
          * Returns a string representation of the object. In general, the
          * <code>toString</code> method returns a string that
          * "textually represents" this object. The result should
          * be a concise but informative representation that is easy for a
          * person to read.
          * It is recommended that all subclasses override this method.
          * <p/>
          * The <code>toString</code> method for class <code>Object</code>
          * returns a string consisting of the name of the class of which the
          * object is an instance, the at-sign character `<code>@</code>', and
          * the unsigned hexadecimal representation of the hash code of the
          * object. In other words, this method returns a string equal to the
          * value of:
          * <blockquote>
          * <pre>
          * getClass().getName() + '@' + Integer.toHexString(hashCode())
          * </pre></blockquote>
          *
          * @return a string representation of the object.
          */
         public String toString()
         {
             String output = primaryName + ": [";
             String[] aliases = getAllNames();
 
             for(int i = 0; i < aliases.length; i++)
             {
                 if(0 != i)
                     output += ", ";
 
                 output += aliases[i];
             }
 
             output += "]";
             return output;
         }
     }
 
     private String[] getPropertyNames(String methodName, String prefix)
     {
         PropertyNames result = new PropertyNames(methodName, prefix);
         return result.getAllNames();
     }
 
     public interface NestedCreator
     {
         public Object create(Object parent)
                 throws InvocationTargetException, IllegalAccessException, InstantiationException;
 
         public boolean isInherited();
 
         public Class getDeclaringClass();
     }
 
     public interface NestedAltClassCreator
     {
         public Object create(Object parent, Class cls)
                 throws InvocationTargetException, IllegalAccessException, InstantiationException;
 
         public boolean isInherited();
 
         public Class getDeclaringClass();
     }
 
     public interface NestedStorer
     {
         public void store(Object parent, Object child)
                 throws InvocationTargetException, IllegalAccessException, InstantiationException;
 
         public boolean isInherited();
 
         public Class getDeclaringClass();
     }
 
     public interface AttributeSetter
     {
         public void set(XdmParseContext pc, Object parent, String value)
                 throws InvocationTargetException, IllegalAccessException,
                        DataModelException;
 
         public boolean isInherited();
 
         public Class getDeclaringClass();
     }
 
     public interface AttributeAccessor
     {
         public Object get(XdmParseContext pc, Object parent)
                 throws InvocationTargetException, IllegalAccessException,
                        DataModelException;
 
         public boolean isInherited();
 
         public Class getDeclaringClass();
     }
 }
