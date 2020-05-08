 package edu.uwlax.toze.domain;
 
 import edu.uwlax.toze.objectz.TozeToken;
 
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlElements;
 import javax.xml.bind.annotation.XmlRootElement;
 import javax.xml.bind.annotation.XmlType;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 @XmlRootElement(name = "TOZE")
 @XmlType(propOrder =
                  {
                         "specObjectList"
                  })
 public class Specification extends SpecObject
 {
     private List<SpecObject> specObjectList = new ArrayList<SpecObject>();
 
     @XmlElements
             ({
                      @XmlElement(name = "predicate", type=Predicate.class),
                      @XmlElement(name = "basicTypeDef", type=BasicTypeDef.class),
                      @XmlElement(name = "axiomaticDef", type=AxiomaticDef.class),
                      @XmlElement(name = "genericDef", type=GenericDef.class),
                      @XmlElement(name = "abbreviationDef", type=AbbreviationDef.class),
                      @XmlElement(name = "freeTypeDef", type=FreeTypeDef.class),
                      @XmlElement(name = "classDef", type=ClassDef.class)
              })
     public List<SpecObject> getSpecObjectList()
     {
        // Can't encapsulate the List because JAXB needs direct access
        return specObjectList;
     }
 
     public void setSpecObjectList(final List<SpecObject> specObjectList)
     {
        // Can't encapsulate the List because JAXB needs direct access
        this.specObjectList = specObjectList;
     }
 
     public void addSpecObject(final SpecObject specObject)
     {
         specObjectList.add(specObject);
     }
 
     public void removeSpecObject(final SpecObject specObject)
     {
         specObjectList.remove(specObject);
     }
 
 
     public List<ClassDef> getClassDefList()
     {
         ArrayList<ClassDef> classDefList = new ArrayList<ClassDef>();
 
         for (SpecObject specObject : specObjectList)
             {
             if (specObject instanceof ClassDef)
                 {
                 classDefList.add((ClassDef)specObject);
                 }
             }
 
         return classDefList;
     }
 
     private List<TozeToken> getErrorsInList(List list)
     {
         List<TozeToken> errorList = new ArrayList<TozeToken>();
         List<SpecObject> specObjectList = list;
 
         for (SpecObject specObject : specObjectList)
             {
             errorList.addAll(specObject.getErrors());
             }
 
         return errorList;
     }
 
     @Override
     public List<TozeToken> getErrors()
     {
         List<TozeToken> errorList = super.getErrors();
 
         for (SpecObject specObject : specObjectList)
             {
             errorList.addAll(specObject.getErrors());
 
             if (specObject instanceof ClassDef)
                 {
                 ClassDef classDef = (ClassDef)specObject;
                 errorList.addAll(notNullGetErrors(classDef.getInheritedClass()));
                 errorList.addAll(notNullGetErrors(classDef.getState()));
                 errorList.addAll(notNullGetErrors(classDef.getInitialState()));
                 errorList.addAll(getErrorsInList(classDef.getSpecObjectList()));
                 errorList.addAll(getErrorsInList(classDef.getOperationList()));
                 }
             }
 
         return errorList;
     }
 
     @Override
     public void clearErrors()
     {
         super.clearErrors();
 
         for (SpecObject specObject : specObjectList)
             {
             specObject.clearErrors();
             }
     }
 
     private SpecObject findObjectWithError(TozeToken errorToken, List specObjectList)
     {
         SpecObject objectWithError = null;
 
         Iterator iterator = specObjectList.iterator();
         while (objectWithError == null && iterator.hasNext())
             {
             SpecObject specObject = (SpecObject) iterator.next();
             if (specObject.getErrors().contains(errorToken))
                 {
                 objectWithError = specObject;
                 }
             if (specObject instanceof  ClassDef)
                 {
                 ClassDef classDef = (ClassDef)specObject;
                 if (notNullGetErrors(classDef.getInheritedClass()).contains(errorToken))
                     {
                     objectWithError = classDef.getInheritedClass();
                     }
                 else if (notNullGetErrors(classDef.getInitialState()).contains(errorToken))
                     {
                     objectWithError = classDef.getInitialState();
                     }
                 else if (notNullGetErrors(classDef.getState()).contains(errorToken))
                     {
                     objectWithError = classDef.getState();
                     }
                 else
                     {
                     objectWithError = findObjectWithError(errorToken, classDef.getSpecObjectList());
                     }
                 if (objectWithError == null)
                     {
                     objectWithError = findObjectWithError(errorToken, classDef.getOperationList());
                     }
                 }
             }
 
         return objectWithError;
     }
 
     public SpecObject findObjectWithError(TozeToken errorToken)
     {
         SpecObject objectWithError = null;
 
         if (super.getErrors().contains(errorToken))
             {
             objectWithError = this;
             }
 
         if (objectWithError == null)
             {
             objectWithError = findObjectWithError(errorToken, specObjectList);
             }
 
         return objectWithError;
     }
 
     @Override
     public Specification clone() throws CloneNotSupportedException
     {
         super.clone();
 
         Specification specificationClone = new Specification();
 
         for (SpecObject specObject : specObjectList)
             {
             specificationClone.addSpecObject(specObject.clone());
             }
 
         return specificationClone;
     }
 }
