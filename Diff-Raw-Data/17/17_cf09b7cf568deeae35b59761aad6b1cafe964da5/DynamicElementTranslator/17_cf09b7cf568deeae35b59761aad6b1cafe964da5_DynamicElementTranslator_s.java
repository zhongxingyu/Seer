 /**
  * 
  */
 package org.eclipse.jst.jsf.facesconfig.internal.translator;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.emf.common.notify.Notifier;
 import org.eclipse.emf.common.util.EList;
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.ecore.EStructuralFeature;
 import org.eclipse.emf.ecore.resource.Resource;
 import org.eclipse.jst.jsf.facesconfig.emf.DynamicAttribute;
 import org.eclipse.jst.jsf.facesconfig.emf.DynamicElement;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigFactory;
 import org.eclipse.jst.jsf.facesconfig.emf.FacesConfigPackage;
 import org.eclipse.wst.common.internal.emf.resource.ReadAheadHelper;
 import org.eclipse.wst.common.internal.emf.resource.Translator;
 import org.eclipse.wst.common.internal.emf.resource.TranslatorPath;
 import org.eclipse.wst.common.internal.emf.resource.VariableTranslatorFactory;
 import org.w3c.dom.Attr;
 import org.w3c.dom.NamedNodeMap;
 import org.w3c.dom.Node;
 
 class DynamicElementTranslator extends Translator implements IAnyTranslator
 {
     public boolean isManagedByParent() {
         return false;
     }
 
     protected Translator[] getChildren() 
     {
         FacesConfigPackage facesConfigPackage = FacesConfigPackage.eINSTANCE;
 
         return new Translator[]
         {
            new Translator(TEXT_ATTRIBUTE_VALUE, facesConfigPackage.getDynamicElement_TextContent()),
            new DynamicElementTranslator("*", facesConfigPackage.getDynamicElement_ChildNodes())
         };
     }
 
     public DynamicElementTranslator(String domNameAndPath,
             EStructuralFeature feature) 
     {
         super(domNameAndPath, feature);
     }
     public boolean isMapFor(String domName) {
         return true;
     }
     
     public EObject createEMFObject(String nodeName, String readAheadName) {
         // tag name is based on the name property
         DynamicElement element = FacesConfigFactory.eINSTANCE.createDynamicElement();
         element.setName(nodeName);
         return element;
     }
 
     public String getDOMName(Object value) {
         // tag name is based on the name property
         if (value instanceof DynamicElement)
         {
             return ((DynamicElement)value).getName();
         }
         return "";
     }
 
     public Translator[] getDynamicAttributeTranslators(Node element) {
         NamedNodeMap attributeMap = element.getAttributes();
         List attributes = new ArrayList(attributeMap.getLength());
         for (int i = 0; i < attributeMap.getLength(); i++)
         {
             Attr attr = (Attr) attributeMap.item(i);
             attributes.add(
                     new DynamicAttributeTranslator(attr.getNodeName(),
                             FacesConfigPackage.eINSTANCE.getDynamicElement_Attributes()));
         }
         
         return (Translator[]) attributes.toArray(new Translator[0]);
     }
     
     public Translator[] getDynamicAttributeTranslators(DynamicElement element) {
         List attributes = element.getAttributes();
         Translator[] translators = new Translator[attributes.size()];
         int i = 0;
         for (Iterator it = attributes.iterator(); it.hasNext();)
         {
             DynamicAttribute attribute = (DynamicAttribute) it.next();
             translators[i++] = new DynamicAttributeTranslator(attribute.getName(),
                     FacesConfigPackage.eINSTANCE.getDynamicElement_Attributes());
         }
         return translators;
     }
 
     private static class DynamicAttributeTranslator extends Translator
     {
 
         public DynamicAttributeTranslator(String domNameAndPath, EStructuralFeature feature) 
         {
             // the "*" is a place holder.  The dom path gets ignored in
             // this translator.  It's a handy one because it is invalid
             // tag name data so if it gets transmitted to the model
             // we will get an exception
             super(domNameAndPath, feature, DOM_ATTRIBUTE);
         }
         
         public boolean isMapFor(String domName) {
             return super.isMapFor(domName);
         }
         
         public EObject createEMFObject(String nodeName, String readAheadName) {
             // attribute name is based on the name property
             DynamicAttribute attribute = FacesConfigFactory.eINSTANCE.createDynamicAttribute();
             attribute.setName(nodeName);
             return attribute;
         }
         
         protected Translator[] getChildren() {
             return new Translator[0];
         }
 
         public void addReadAheadHelper(ReadAheadHelper helper) {
             // TODO Auto-generated method stub
             super.addReadAheadHelper(helper);
         }
 
         public EObject basicGetDependencyObject(EObject parent) {
             // TODO Auto-generated method stub
             return super.basicGetDependencyObject(parent);
         }
 
         public void clearList(EObject mofObject) {
             // TODO Auto-generated method stub
             super.clearList(mofObject);
         }
 
         public Object convertStringToValue(String strValue, EObject owner) {
             // TODO Auto-generated method stub
             return super.convertStringToValue(strValue, owner);
         }
 
         public Object convertStringToValue(String nodeName,
                 String readAheadName, String value, Notifier owner) {
             // TODO Auto-generated method stub
             return super.convertStringToValue(nodeName, readAheadName, value, owner);
         }
 
         public String convertValueToString(Object value, EObject owner) {
             // TODO Auto-generated method stub
             return super.convertValueToString(value, owner);
         }
 
         public boolean equals(Object object) {
             // TODO Auto-generated method stub
             return super.equals(object);
         }
 
         public String extractStringValue(EObject emfObject) {
             // TODO Auto-generated method stub
             return super.extractStringValue(emfObject);
         }
 
         public boolean featureExists(EObject emfObject) {
             // TODO Auto-generated method stub
             return super.featureExists(emfObject);
         }
 
         public Translator findChild(String tagName, Object target, int versionID) {
             // TODO Auto-generated method stub
             return super.findChild(tagName, target, versionID);
         }
 
         public Translator[] getChildren(Object target, int versionID) {
             // TODO Auto-generated method stub
             return super.getChildren(target, versionID);
         }
 
         public EStructuralFeature getDependencyFeature() {
             // TODO Auto-generated method stub
             return super.getDependencyFeature();
         }
 
         public String[] getDOMNames() {
             // TODO Auto-generated method stub
             return super.getDOMNames();
         }
 
         public String getDOMPath() {
             // TODO Auto-generated method stub
             return super.getDOMPath();
         }
 
         public EStructuralFeature getFeature() {
             // TODO Auto-generated method stub
             return super.getFeature();
         }
 
         public List getMOFChildren(EObject mofObject) {
             // TODO Auto-generated method stub
             return super.getMOFChildren(mofObject);
         }
 
         public Object getMOFValue(EObject mofObject) {
             if (mofObject instanceof DynamicElement)
             {
                 EList attributes = ((DynamicElement)mofObject).getAttributes();
                 
                 for (final Iterator it = attributes.iterator(); it.hasNext();)
                 {
                     DynamicAttribute attribute = (DynamicAttribute) it.next();
                     if (fDOMNames[0].equals(attribute.getName()))
                     {
                         return attribute.getValue();
                     }
                 }
             }
             
             return null;
         }
 
         public String getNameSpace() {
             // TODO Auto-generated method stub
             return super.getNameSpace();
         }
 
         public ReadAheadHelper getReadAheadHelper(String parentName) {
             // TODO Auto-generated method stub
             return super.getReadAheadHelper(parentName);
         }
 
         public TranslatorPath[] getTranslatorPaths() {
             // TODO Auto-generated method stub
             return super.getTranslatorPaths();
         }
 
         public Translator[] getVariableChildren(Notifier target, int version) {
             // TODO Auto-generated method stub
             return super.getVariableChildren(target, version);
         }
 
         public VariableTranslatorFactory getVariableTranslatorFactory() {
             // TODO Auto-generated method stub
             return super.getVariableTranslatorFactory();
         }
 
         public boolean hasDOMPath() {
             // TODO Auto-generated method stub
             return super.hasDOMPath();
         }
 
         public boolean hasReadAheadNames() {
             // TODO Auto-generated method stub
             return super.hasReadAheadNames();
         }
 
         protected void initializeDOMNameAndPath(String domNameAndPathArg) {
             // TODO Auto-generated method stub
             super.initializeDOMNameAndPath(domNameAndPathArg);
         }
 
         public boolean isBooleanFeature() {
             // TODO Auto-generated method stub
             return super.isBooleanFeature();
         }
 
         public boolean isBooleanUppercase() {
             // TODO Auto-generated method stub
             return super.isBooleanUppercase();
         }
 
         public boolean isCDATAContent() {
             // TODO Auto-generated method stub
             return super.isCDATAContent();
         }
 
         public boolean isComment() {
             // TODO Auto-generated method stub
             return super.isComment();
         }
 
         public boolean isDataType() {
             // TODO Auto-generated method stub
             return super.isDataType();
         }
 
         public boolean isDependencyChild() {
             // TODO Auto-generated method stub
             return super.isDependencyChild();
         }
 
         public boolean isDependencyParent() {
             // TODO Auto-generated method stub
             return super.isDependencyParent();
         }
 
         public boolean isDOMAttribute() {
             // TODO Auto-generated method stub
             return super.isDOMAttribute();
         }
 
         public boolean isDOMTextValue() {
             // TODO Auto-generated method stub
             return super.isDOMTextValue();
         }
 
         public boolean isEmptyContentSignificant() {
             // TODO Auto-generated method stub
             return super.isEmptyContentSignificant();
         }
 
         public boolean isEmptyTag() {
             // TODO Auto-generated method stub
             return super.isEmptyTag();
         }
 
         public boolean isEnumFeature() {
             // TODO Auto-generated method stub
             return super.isEnumFeature();
         }
 
         public boolean isEnumWithHyphens() {
             // TODO Auto-generated method stub
             return super.isEnumWithHyphens();
         }
 
         public boolean isIDMap() {
             // TODO Auto-generated method stub
             return super.isIDMap();
         }
 
         public boolean isLinkMap() {
             // TODO Auto-generated method stub
             return super.isLinkMap();
         }
 
         public boolean isManagedByParent() {
             // TODO Auto-generated method stub
             return super.isManagedByParent();
         }
 
         public boolean isMapFor(Object feature, Object oldValue, Object newValue) {
             // TODO Auto-generated method stub
             return super.isMapFor(feature, oldValue, newValue);
         }
 
         public boolean isMultiValued() {
             // we want to treat each attribute as a single value
             return false;
         }
 
         public boolean isObjectMap() {
             // TODO Auto-generated method stub
             return super.isObjectMap();
         }
 
         public boolean isSetMOFValue(EObject emfObject) {
             // TODO Auto-generated method stub
             return super.isSetMOFValue(emfObject);
         }
 
         public boolean isShared() {
             // TODO Auto-generated method stub
             return super.isShared();
         }
 
         public boolean isTargetLinkMap() {
             // TODO Auto-generated method stub
             return super.isTargetLinkMap();
         }
 
         public boolean isUnsettable() {
             // TODO Auto-generated method stub
             return super.isUnsettable();
         }
 
         protected String[] parseDOMNames(String domNamesString) {
             // TODO Auto-generated method stub
             return super.parseDOMNames(domNamesString);
         }
 
         public void removeMOFValue(Notifier owner, Object value) {
             // TODO Auto-generated method stub
             super.removeMOFValue(owner, value);
         }
 
         protected void setEMFClass(EClass anEClass) {
             // TODO Auto-generated method stub
             super.setEMFClass(anEClass);
         }
 
         protected void setFeature(EStructuralFeature feature) {
             // TODO Auto-generated method stub
             super.setFeature(feature);
         }
 
         public void setMOFValue(Notifier owner, Object value, int newIndex) {
             if (owner instanceof DynamicElement)
             {
                 DynamicAttribute attribute = FacesConfigFactory.eINSTANCE.createDynamicAttribute();
                 attribute.setName(fDOMNames[0]);
                 attribute.setValue(value.toString());
 
                 EList attributes = ((DynamicElement)owner).getAttributes();
                 if (newIndex < 0 || newIndex >= attributes.size())
                 {
                     attributes.add(attribute);
                 }
                 else
                 {
                     attributes.set(newIndex, attribute);
                 }
             }
             // otherwise do nothing
         }
 
         public void setMOFValue(Notifier owner, Object value) {
             // TODO Auto-generated method stub
             super.setMOFValue(owner, value);
         }
 
         public void setMOFValue(Resource res, Object value) {
             // TODO Auto-generated method stub
             super.setMOFValue(res, value);
         }
 
         public void setMOFValueFromEmptyDOMPath(EObject object) {
             // TODO Auto-generated method stub
             super.setMOFValueFromEmptyDOMPath(object);
         }
 
         public void setNameSpace(String string) {
             // TODO Auto-generated method stub
             super.setNameSpace(string);
         }
 
         public void setTextValueIfNecessary(String textValue, Notifier owner,
                 int versionId) {
             // TODO Auto-generated method stub
             super.setTextValueIfNecessary(textValue, owner, versionId);
         }
 
         public boolean shouldIndentEndTag() {
             // TODO Auto-generated method stub
             return super.shouldIndentEndTag();
         }
 
         public boolean shouldRenderEmptyDOMPath(EObject object) {
             // TODO Auto-generated method stub
             return super.shouldRenderEmptyDOMPath(object);
         }
 
         public String toString() {
             // TODO Auto-generated method stub
             return super.toString();
         }
 
         public void unSetMOFValue(EObject emfObject) {
             // TODO Auto-generated method stub
             super.unSetMOFValue(emfObject);
         }
         
         
     }
 }
