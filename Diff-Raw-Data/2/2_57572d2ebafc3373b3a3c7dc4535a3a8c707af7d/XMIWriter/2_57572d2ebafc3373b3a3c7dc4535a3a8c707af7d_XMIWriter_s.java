 package gov.nih.nci.ncicb.cadsr.loader.parser;
 
 import gov.nih.nci.ncicb.cadsr.domain.*;
 import gov.nih.nci.ncicb.cadsr.loader.*;
 import gov.nih.nci.ncicb.cadsr.loader.util.LookupUtil;
 
 import org.jdom.*;
 import org.jdom.input.SAXBuilder;
 import org.jaxen.JaxenException;
 import org.jaxen.jdom.JDOMXPath;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 
 import java.io.*;
 import java.util.*;
 
 public class XMIWriter implements ElementWriter {
 
   private String output = null;
 
   private UserSelections userSelections = UserSelections.getInstance();
   private String input = (String)userSelections.getProperty("FILENAME");
 
   private Element modelElement;
 
   private HashMap<String, Element> elements = new HashMap();
 
   private ElementsLists cadsrObjects = null;
 
   private ReviewTracker reviewTracker = ReviewTracker.getInstance();
 
   
   public XMIWriter() {
     try {
       SAXBuilder builder = new SAXBuilder();
       Document doc = builder.build(input);
       modelElement = doc.getRootElement();
     } catch (Exception ex) {
       throw new RuntimeException("Error initializing model", ex);
     }
   }
 
   public void write(ElementsLists elements) {
 
     this.cadsrObjects = elements;
     
     readModel();
     markHumanReviewed();
     writeModel();
 
   }
 
   public void setOutput(String url) {
     this.output = url;
   }
 
   /**
    * Copy / Paste from caCORE-Toolkit ModelAnnotator
    *
    */
   private void addTaggedValue
     (String tagName,
      String value,
      String newId,
      String xmiid,
      Namespace namespace) throws JaxenException {
 
       Element taggedValue = new Element("TaggedValue", namespace);
       taggedValue.setNamespace(Namespace.getNamespace("UML", "href://org.omg/UML"));
       taggedValue.setAttribute(new Attribute("xmi.id", newId));
       taggedValue.setAttribute(new Attribute("tag", tagName));
       taggedValue.setAttribute(new Attribute("modelElement", xmiid));
       taggedValue.setAttribute(new Attribute("value", value));
       
       Element elem1 = getElement(modelElement, "//*[local-name()='Model'");
       Element parentElement = elem1.getParentElement();
       parentElement.addContent(taggedValue);
 
   }
 
   /**
    *
    * Copy / Paste from caCORE-Toolkit ModelAnnotator
    *
    */
   private Element getElement
     (Element classElement, String exp)
     throws JaxenException {
 
     Element element = null;
     try {
       element = (Element) (new JDOMXPath(exp)).selectSingleNode(classElement);
       
     } catch (Exception ex) {
       throw new RuntimeException("Error searching for expression " + exp
                                  + " in class " + classElement.getAttributeValue("name"), ex);
     }
     return element;
   }
 
 
   private List<Element> getElements
     (Element classElement, String elementName)
     throws JaxenException {
 
     List<Element> elementList = null;
     try {
       String exp = ".//*[local-name()='"+elementName+"']";
       elementList = (List<Element>) (new JDOMXPath(exp)).selectNodes(classElement);
       
     } catch (Exception ex) {
       throw new RuntimeException("Error searching for elements " + elementName
                                  + " for class " + classElement.getAttributeValue("name"), ex);
     }
     return elementList;
   }
 
 
   /**
    * Copy / Paste from caCORE-Toolkit ModelAnnotator
    *
    */
   private void writeModel() {
     try {
       File f = new File(output);
       
       Writer writer = new OutputStreamWriter
         (new FileOutputStream(f), "UTF-8");
       XMLOutputter xmlout = new XMLOutputter();
       xmlout.setFormat(Format.getPrettyFormat());
       writer.write(xmlout.outputString(modelElement));
       writer.flush();
       writer.close();
     } catch (Exception ex) {
       throw new RuntimeException("Error writing to " + output, ex);
     }
   }
   
   /**
    * Copy / Paste from caCORE-Toolkit ModelAnnotator
    *
    */
   private String getNewId(String xmiid) throws JaxenException
   {
     String id = null;
     try
       {
         String exp = "//*[local-name()='TaggedValue' and @modelElement='"+ xmiid +"']";
         List tvs = (List)(new JDOMXPath(exp)).selectNodes(modelElement);
         Element tv = (Element)tvs.get(tvs.size()-1);
         
         if(tv != null)
           id = (String)tv.getAttributeValue("xmi.id")+"_tag";
         else
           id = xmiid+"_tag";
         
       }
     catch(Exception e)
       {
         throw new RuntimeException("Exception while creating getNewId"+e.getMessage());
       }
     
     return id;
   }
   
  
   /**
    * Copy / Paste from caCORE-Toolkit ModelAnnotator
    *
    */
   private void readModel(){
     try {
       String xpath = "//*[local-name()='Model']";
       doPackage(xpath, "");
     } catch (JaxenException e){
     } // end of try-catch
   }
 
   private void markHumanReviewed() {
 
     try{ 
     List<ObjectClass> ocs = (List<ObjectClass>)cadsrObjects.getElements(DomainObjectFactory.newObjectClass().getClass());
     List<DataElementConcept> decs = (List<DataElementConcept>) cadsrObjects.getElements(DomainObjectFactory.newDataElementConcept().getClass());
 
     for(ObjectClass oc : ocs) {
       Element classElement = elements.get(oc.getLongName());
       String xpath = "//*[local-name()='TaggedValue' and @tag='HUMAN_REVIEWED' and modelElement='"
         + classElement.getAttributeValue("xmi.id")
         + "']";
 
       JDOMXPath path = new JDOMXPath(xpath);
       Element tv = (Element)path.selectSingleNode(modelElement);
       boolean reviewed = reviewTracker.get(oc.getLongName());
       if(tv == null) {
         addTaggedValue("HUMAN_REVIEWED",
                        reviewed?"1":"0",
                        getNewId(classElement.getAttributeValue("xmi.id")),
                        classElement.getAttributeValue("xmi.id"),
                        classElement.getNamespace());
       } else {
         tv.setAttribute("value", "1");
       }
 
     }
     for(DataElementConcept dec : decs) {
       String fullPropName = dec.getObjectClass().getLongName() + "." + dec.getProperty().getLongName();
 
       Boolean reviewed = reviewTracker.get(fullPropName);
       if(reviewed == null) {
         continue;
       }
 
       Element attributeElement = elements.get(fullPropName);
       String xpath = "//*[local-name()='TaggedValue' and @tag='HUMAN_REVIEWED' and modelElement='"
         + attributeElement.getAttributeValue("xmi.id")
         + "']";
 
       JDOMXPath path = new JDOMXPath(xpath);
       Element tv = (Element)path.selectSingleNode(modelElement);
       if(tv == null) {
         addTaggedValue("HUMAN_REVIEWED",
                        reviewed?"1":"0",
                        getNewId(attributeElement.getAttributeValue("xmi.id")),
                        attributeElement.getAttributeValue("xmi.id"),
                        attributeElement.getNamespace());
       } else {
         tv.setAttribute("value", reviewed?"1":"0");
       }
     }
 
 
     } catch (JaxenException e){
     } // end of try-catch
     
       
     
 
   }
   
   
     private void doPackage(String xpath, String packageName) throws JaxenException {
       xpath = xpath + "/*[local-name()='Namespace.ownedElement']/*[local-name()='Package']";
       
       JDOMXPath path = new JDOMXPath(xpath);
       Collection<Element> packages = (Collection<Element>)path.selectNodes(modelElement);
       
       if(packages.size() == 0)
         return;
 
       for(Element pkg : packages) {
         String packName = pkg.getAttributeValue("name");
 
        if(packName.indexOf("0") != -1) {
           doPackage(xpath, packageName);
           return;
         }
 
         if (packageName.length() == 0) {
           packageName = packName;
         }
         else {
           packageName += ("." + packName);
         }
 
         doPackage(xpath, packageName);
 
         String classXpath = xpath + "/*[local-name()='Namespace.ownedElement']/*[local-name() = 'Class']";
         path = new JDOMXPath(classXpath);
         Collection<Element> classes = (Collection<Element>)path.selectNodes(modelElement);
         
         for (Element classElement : classes) {
           String className = packageName + "." + classElement.getAttributeValue("name");
 
           elements.put(className, classElement);
           
           List<Element> attributes = getElements(classElement, "Attribute");
           
           for(Element attributeElt : attributes) {
             String attributeName = 
               className + "." + attributeElt.getAttributeValue("name");
             elements.put(attributeName, attributeElt);
           }
         }
       }
     }
  
 }
