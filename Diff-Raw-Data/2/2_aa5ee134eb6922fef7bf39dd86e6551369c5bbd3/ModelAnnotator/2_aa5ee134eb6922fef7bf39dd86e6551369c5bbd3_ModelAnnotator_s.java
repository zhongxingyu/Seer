 package gov.nih.nci.ncicb.cadsr.semconn;
 
 
 import gov.nih.nci.ncicb.cadsr.loader.event.ProgressEvent;
 import gov.nih.nci.ncicb.cadsr.loader.event.ProgressListener;
 import gov.nih.nci.ncicb.cadsr.semconn.SemanticConnectorException;
 import org.apache.log4j.*;
 import org.jaxen.JaxenException;
 import org.jaxen.jdom.JDOMXPath;
 import org.jdom.Attribute;
 import org.jdom.Document;
 import org.jdom.Element;
 import org.jdom.Namespace;
 import org.jdom.input.SAXBuilder;
 import org.jdom.output.Format;
 import org.jdom.output.XMLOutputter;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 
 import org.jdom.JDOMException;
 
 
 /**
  * <!-- LICENSE_TEXT_START -->
  * <!-- LICENSE_TEXT_END -->
  * Copyright 2001-2004 SAIC. Copyright 2001-2003 SAIC. This software was
  * developed in conjunction with the National Cancer Institute, and so to the
  * extent government employees are co-authors, any rights in such works shall
  * be subject to Title 17 of the United States Code, section 105.
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * 1. Redistributions of source code must retain the above copyright notice,
  * this list of conditions and the disclaimer of Article 3, below.
  * Redistributions in binary form must reproduce the above copyright notice,
  * this list of conditions and the following disclaimer in the documentation
  * and/or other materials provided with the distribution. 2. The end-user
  * documentation included with the redistribution, if any, must include the
  * following acknowledgment: "This product includes software developed by the
  * SAIC and the National Cancer Institute." If no such end-user documentation
  * is to be included, this acknowledgment shall appear in the software itself,
  * wherever such third-party acknowledgments normally appear. 3. The names
  * "The National Cancer Institute", "NCI" and "SAIC" must not be used to
  * endorse or promote products derived from this software. 4. This license
  * does not authorize the incorporation of this software into any third party
  * proprietary programs. This license does not authorize the recipient to use
  * any trademarks owned by either NCI or SAIC-Frederick. 5. THIS SOFTWARE IS
  * PROVIDED "AS IS," AND ANY EXPRESSED OR IMPLIED WARRANTIES, (INCLUDING, BUT
  * NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
  * PARTICULAR PURPOSE) ARE DISCLAIMED. IN NO EVENT SHALL THE NATIONAL CANCER
  * INSTITUTE, SAIC, OR THEIR AFFILIATES BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
  * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
  * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
  * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 /**
  * @author caBIO Team, refactored by SIW Team.
  * @version 1.0
  */
 public final class ModelAnnotator extends SubjectClass{
   private static Logger log = Logger.getLogger(ModelAnnotator.class.getName());
   
   private Element rootElement = null;
   private ReportHandler reportHandler = null;
   
   /*result of the previous readModel()*/
   private List umlEntities;
 
   
   static {
       Configuration.loadProperties(); //TODO handle file does not exist exception
   }
 
   /**
    * Constructor
    */
   public ModelAnnotator(){
   }
 
   /**
    * Search EVS and generate report in CSV format.
    *
    * @param inputXMIFileName 
    * @param outputCsvFileName
    */
   public void generateEVSReport (
     String inputXMIFileName,
     String outputCsvFileName,
     boolean override) 
     throws FileDoesNotExistException, FileAlreadyExistsException, SemanticConnectorException{
 
     umlEntities = readModel(inputXMIFileName);        
 
     //report is written in the ReportHandler.
    reportHandler.generateEVSReport(umlEntities, outputCsvFileName, override);
   }//end of method;
 
    /**
      * Search EVS and generate report in CSV format based on the alredy loaded model.
      * @param outputCsvFileName
      * @throws gov.nih.nci.ncicb.cadsr.semconn.SemanticConnectorException
      */
    public void generateEVSReport (
      String outputCsvFileName,
      boolean override) 
      throws FileAlreadyExistsException, SemanticConnectorException{
 
      //santity check
      List loadedModel = getLoadedModel();
      if (loadedModel == null || loadedModel.size() == 0){
         log.warn("Model does not exist. No EVS report is generated.");
         throw new SemanticConnectorException("Model does not exist. Could not generate EVS report");
      }
 
      //report is written in the ReportHandler.
      getReportHandler().generateEVSReport(loadedModel, outputCsvFileName, override);
    }//end of method;
 
 
     /**
      * Search EVS and generate report in CSV format based on the already loaded model.
      * @param csvFileName
      * @param outputXMIFileName
      * @throws gov.nih.nci.ncicb.cadsr.semconn.SemanticConnectorException
      */     
      public void annotateModel (
      String csvFileName,
      String outputXMIFileName,
      boolean override
      ) 
      throws FileDoesNotExistException, FileAlreadyExistsException, SemanticConnectorException{
 
     //sanity check
     if (umlEntities == null || umlEntities.size()==0){
         log.warn("Model does not exist.");
         throw new SemanticConnectorException(new Exception("Model does not exist. No annotated XMI is generated."));        
     }
     if (!doesFileExist(csvFileName)){
         throw new FileDoesNotExistException("File " + csvFileName +" does not exist.");
     }
 
     if (!override && doesFileExist(outputXMIFileName)){
         throw new FileAlreadyExistsException("File " + outputXMIFileName +" already exists.");
     }
 
     //model is already loaded
     try{
          //report is written in the ReportHandler.
          if (reportHandler == null){
             reportHandler = new ReportHandler(); 
          } 
                   
          List updatedList = reportHandler.getUpdatedEntities(getLoadedModel(), csvFileName);
          //TODO - missed human verified
          updateModel(updatedList);
          writeModel(outputXMIFileName, override);
          
          //notify done
          notifyEventDone("Annotation Done.");         
      }catch (FileAlreadyExistsException e){
          throw e;
      }catch (SemanticConnectorException e){
          throw e;
      }
      catch (Exception e) {
        log.error("Exception: " + e.getMessage());
        throw new SemanticConnectorException(e);          
      }
    }//end of method;
 
 
     /**
      * Load model from the input XMI file, search EVS and generate report in CSV format.
      * @param inputXMIFileName
      * @param csvFileName
      * @param outputXMIFileName
      * @throws gov.nih.nci.ncicb.cadsr.semconn.SemanticConnectorException
      */ 
     public void annotateXMI (
       String inputXMIFileName,
       String csvFileName,
       String outputXMIFileName,
       boolean override
       ) 
       throws FileDoesNotExistException, FileAlreadyExistsException,
              SemanticConnectorException{
 
       umlEntities = readModel(inputXMIFileName);        
       annotateModel(csvFileName, outputXMIFileName, override);
     }//end of method;
 
 
 
   
   /**
    * Reads the model and creates list of umlEntities
    */
   public List readModel(String inputXMIFileName) 
   throws FileDoesNotExistException,SemanticConnectorException{
   
     if (!doesFileExist(inputXMIFileName)){
         throw new FileDoesNotExistException(" File " + inputXMIFileName + " does not exist.");
     }
 
     try{        
         SAXBuilder builder = new SAXBuilder();         
         Document doc = builder.build(inputXMIFileName);    
         rootElement = doc.getRootElement();
     }catch (JDOMException e){
         throw new SemanticConnectorException("Could not parse XMI file " + inputXMIFileName);
     }catch (IOException ioe){
         ;//will not happen.
     }
     
     try{
         //Objects eligible for semantic lookup must reside in a UML:Package entity 
         //Collection classes = getClasses(rootElement);
         Collection classes = getClassesWithPackage(rootElement);
 
         String UMLClass = null;
         HashMap elementValues = null;
         
         umlEntities = null;        
         umlEntities = new ArrayList();
 
         ProgressEvent event = new ProgressEvent();
         event.setGoal(classes.size() + 1);
         event.setMessage("Reading Model...");
         
         int index = 0;
         for (Iterator i = classes.iterator(); i.hasNext();) {
             //send progress notification
             event.setStatus(++index);
             notify(event);
             
             Element classElement = (Element) i.next();
             elementValues = new HashMap();
             UMLClass = (String) classElement.getAttributeValue("name");
             
             elementValues.put(Configuration.getUMLClassCol(), UMLClass);
             
             HashMap documentTaggedValueMap = getTaggedValue(classElement, "documentation");
             elementValues.putAll(documentTaggedValueMap);
             umlEntities.add(elementValues);
             
             List attributeList = getElements(classElement, "Attribute");
 
             for (Iterator iter = attributeList.iterator(); iter.hasNext();) {
               elementValues = new HashMap();
               elementValues.put(Configuration.getUMLClassCol(), UMLClass);
     
               HashMap taggedValues = getTaggedValue((Element) iter.next(), "description");
               elementValues.putAll(taggedValues);
               //TODO - should we add to umlEntities here? 
               umlEntities.add(elementValues);
             }//end of for
       }//end of all classes.            
     }
     catch (Exception ex) {
       log.error("Exception occured while reading model: " + ex.getMessage());
       throw new SemanticConnectorException("Error while reading model", ex);
     }
     return umlEntities;
   }//end of readModel();
 
 
     /**
      * Gets classes from the given element.
      *
      * @param rootElement - the element to search from.
      *
      * @return a collection of Element;
      *
      * @throws JaxenException
      */  
     private Collection getClasses(Element rootElement)throws JaxenException{
         /*String exp = 
               "//*[local-name()='Package' and @name='Logical Model']//*[local-name()='Class' and @isRoot='false']";
               */
          String exp = 
                "//*[local-name()='Package']//*[local-name()='Class' and @isRoot='false']";
         return search(rootElement, exp);
     }
    
    
    private Collection getClassesWithPackage(Element rootElement) throws Exception{
        String xpath = "//*[local-name()='Model']/*[local-name()='Namespace.ownedElement']/*[local-name()='Package']";
 
        Collection packages = search(rootElement, xpath);
        if (packages == null || packages.isEmpty()){       
         return getClasses(rootElement);        
        }
        
        //loop through each package.
        Iterator it = packages.iterator();
        Collection classList = new ArrayList();
        StringBuffer packageName = new StringBuffer();
        while (it.hasNext()){
            Element e = (Element)it.next();
            packageName.append(e.getAttributeValue("name"));
            doPackage(e, packageName, classList);
            packageName.delete(0,packageName.length()); //clear out
        }
        return classList;
    }
    
    /**
      * add all classes into classList and the class name is prefixed with the package name.
      * @param startingElement
      * @param packageName
      * @param classList
      * @throws Exception
      */
    private void doPackage(Element startingElement, StringBuffer packageName, Collection classList) throws Exception{
        //first get all the classes in the current package.
        Element namespaceElt = startingElement.getChild("Namespace.ownedElement", startingElement.getNamespace());
        Collection classes = namespaceElt.getChildren("Class", startingElement.getNamespace());
         
        if (classes !=null && !classes.isEmpty()){
            addPackageName(classes, packageName);  
            //need to keep these classes;
             classList.addAll(classes);
        }
        
        //continue to check sub-packages
         Collection packages = namespaceElt.getChildren("Package", startingElement.getNamespace());
         if (packages == null || packages.isEmpty()){       
             //no more subpackage, job is done.
             return;
        }
        
        //loop through each package.
        Iterator it = packages.iterator();
        while (it.hasNext()){
            Element e = (Element)it.next();
            packageName.append(".").append(e.getAttributeValue("name"));
            doPackage(e, new StringBuffer(packageName), classList);           
        }
    }
    
    /**
      * Add package name as part of the class name.
      * @param classes
      * @param packageName
      */
    private void addPackageName(Collection classes, StringBuffer packageName){
        if (classes == null || classes.isEmpty()){
            return;
        }
               
        Iterator it = classes.iterator();
        while (it.hasNext()){
            Element classElement = (Element)it.next();
            
            classElement.setAttribute("name", packageName.toString() + "." + classElement.getAttributeValue("name"));
            System.out.println("name=" + classElement.getAttributeValue("name"));
        }
        return;
    }
    
    
    
   /**
    * Gets elements starting from the provided element for the given expression
    *
    * @param xpathExpression
    *
    * @return a collection of Element;
    *
    * @throws JaxenException
    */
   private Collection search(Element rootElement, String xpathExpression)
     throws JaxenException {
     JDOMXPath path = new JDOMXPath(xpathExpression);
     Collection elementCollection = path.selectNodes(rootElement);
     return elementCollection;
   }
   
   
 
   /**
    * Gets the specified elements of the specified classElement
    *
    * @param classElement
    * @param elementName
    *
    * @return
    */
   private List getElements(
     Element classElement,
     String elementName) throws JaxenException, Exception {
     List elementList = null;
 
     try {
       String exp = ".//*[local-name()='" + elementName + "']";
 
       elementList = (List) (new JDOMXPath(exp)).selectNodes(classElement);
     }
     catch (Exception ex) {
       log.error(
         "Exception occured while searching for elements " + elementName +
         " for class " + classElement.getAttributeValue("name") +
         ex.getMessage());
 
       throw new Exception(
         "Error searching for elements " + elementName + " for class " +
         classElement.getAttributeValue("name"), ex);
     }
 
     return elementList;
   }
 
   /**
    * Gets the element of the specified classElement
    *
    * @param classElement
    *
    * @return
    */
   private Element getElement(
     Element classElement,
     String exp) throws Exception {
     Element element = null;
 
     try {
       element = (Element) (new JDOMXPath(exp)).selectSingleNode(classElement);
     }
     catch (Exception ex) {
       log.error(
         "Exception occured while searching for expression " + exp +
         " in class " + classElement.getAttributeValue("name") +
         ex.getMessage());
       throw ex;
     }
     return element;
   }
 
   /**
    * Gets the TaggedValue element for the specified classElement and tag, and
    * adds to the list
    *
    * @param classElement
    * @param tag
    */
   private HashMap getTaggedValue( Element classElement,String tag)
     throws Exception{
     Element taggedValue = null;
 
     String id = classElement.getAttributeValue("xmi.id");
     String tagName = null;
     ArrayList ccodeList = new ArrayList();
     ArrayList classificationList = new ArrayList();
     String description = null;
     HashMap taggedValueMap = new HashMap();
 
     taggedValueMap.put(
       Configuration.getUMLEntityCol(), classElement.getAttributeValue("name"));
 
     try {
       String exp =
         "//*[local-name()='TaggedValue' and @modelElement='" + id + "']";
 
       //why not useing the classElement but the rootElement?
       //List taggedValues = (List) (new JDOMXPath(exp)).selectNodes(modelElement);
       Collection taggedValues = search(classElement, exp);
 
       ArrayList tagNames = Configuration.getTagNames();
 
       for (Iterator iter = taggedValues.iterator(); iter.hasNext();) {
         taggedValue = (Element) iter.next();
         tagName = (String) taggedValue.getAttributeValue("tag");
 
         if (tagName.equalsIgnoreCase(tag)) {
           description = (String) taggedValue.getAttributeValue("value");
           taggedValueMap.put(Configuration.getUMLDescriptionCol(), description);
         }else {
           if (tagNames.contains(tagName)) {
             taggedValueMap.put(tagName, taggedValue.getAttributeValue("value"));
           }
         }
       }
      return taggedValueMap;
       //umlEntities.add(elementValues);
     }
     catch (Exception ex) {
       log.error(
         "Error searching for TaggedValue " + tag + " for class " +
         classElement.getAttributeValue("name") + ex.getMessage());
 
       throw new Exception(
         "Error searching for TaggedValue " + tag + " for class " +
         classElement.getAttributeValue("name"), ex);
     }
   }
 
   /**
    * Gets TaggedValue Element
    *
    * @param id
    * @param tag
    *
    * @return Element
    *
    * @throws Exception
    */
   private Element getTaggedValue(
     String id,
     String tag) throws Exception {
     Element tv = null;
 
     try {
       String exp =
         "//*[local-name()='TaggedValue' and @modelElement='" + id +
         "' and @tag='" + tag + "']";
 
       tv = (Element) (new JDOMXPath(exp)).selectSingleNode(rootElement);
     }
     catch (Exception ex) {
       log.error(
         "Error searching for TaggedValue " + tag + " for modelElement " + id +
         ", " + ex.getMessage());
       throw new Exception(
         "Error searching for TaggedValue " + tag + " for modelElement " + id, ex);
     }
     return tv;
   }
 
   /**
    * Write Model
    */
   private void writeModel(String outputXMILocation, boolean override) 
   throws FileAlreadyExistsException, SemanticConnectorException{
     try {
       File f = new File(outputXMILocation);
       if (f.exists() && !override){
           throw new FileAlreadyExistsException("Output XMI file " + outputXMILocation + " already exists.");
       }
       if (f.exists()){
           f.delete(); //delete the existing one.
       }
       
       //remove package name from class name
       removePackageName();
       
       Writer writer = new OutputStreamWriter(new FileOutputStream(f), "UTF-8");
       XMLOutputter xmlout = new XMLOutputter();
       xmlout.setFormat(Format.getPrettyFormat());
       writer.write(xmlout.outputString(rootElement));
       writer.flush();
       writer.close();
     }
     catch (FileAlreadyExistsException e){
         throw e;
     }
     catch (Exception ex) {
       log.error(
         "Error writing to " + outputXMILocation + ": " + ex.getMessage());
       throw new SemanticConnectorException("Error writing to " + outputXMILocation, ex);
     }
   }
 
   /**
    * Update Model with the specified list
    *
    * @param evsValues
    *
    * @throws Exception
    */
   private void updateModel(List evsValues) throws SemanticConnectorException {
     HashMap taggedValuesMap = null;
     String UMLClass = null;
     String exp = null;
     String UMLEntity = null;
     Element classElement = null;
     String xmiid = null;
 
     try {
       System.out.println("Annotating Model....");
 
       ProgressEvent event = new ProgressEvent();
       event.setGoal(evsValues.size());
       event.setMessage("Annotating...");
 
       for (int i = 0; i < evsValues.size(); i++) {
         //send event
         event.setStatus(i+1);
         notify(event);
         
         taggedValuesMap = (HashMap) evsValues.get(i);
         UMLClass = (String) taggedValuesMap.get(Configuration.getUMLClassCol());
         UMLEntity =
           (String) taggedValuesMap.get(Configuration.getUMLEntityCol());
         if (UMLClass.equals(UMLEntity)) {
           exp = "//*[local-name()='Class'and @name='" + UMLEntity + "']";
           classElement = getElement(rootElement, exp);
           xmiid = classElement.getAttributeValue("xmi.id");
         }
         else {
           exp = "//*[local-name()='Class'and @name='" + UMLClass + "']";
           classElement = getElement(rootElement, exp);
           exp = ".//*[local-name()='Attribute'and @name='" + UMLEntity + "']";
           Element attributeElement = getElement(classElement, exp);
           xmiid = attributeElement.getAttributeValue("xmi.id");
         }
 
         String VerifiedFlag =
           (String) taggedValuesMap.get(Configuration.getVerifiedFlagCol());
         if (VerifiedFlag.equals("1")) {
           buildTaggedValue(taggedValuesMap, xmiid, classElement.getNamespace());
         }
       }
     }
     catch (Exception e) {
       log.error("Exception: " + e.getMessage());
       throw new SemanticConnectorException("Annotating model failed", e);
     }
   }
 
   /**
    * Builds new TaggedValue elements and adds it to the parent element
    *
    * @param taggedValuesMap
    * @param xmiid
    * @param namespace
    * @param parentElement
    *
    * @throws Exception
    */
   private void buildTaggedValue(
     HashMap taggedValuesMap,
     String xmiid,
     Namespace namespace) throws Exception {
     ArrayList tagNames = new ArrayList();
 
     tagNames = Configuration.getTagNames();
 
     ArrayList newTagNames = new ArrayList();
 
     boolean multipleCodes = false;
 
     Element tv = null;
 
     int index = 0;
 
     try {
       String classification =
         (String) taggedValuesMap.get(Configuration.getClassificationCol());
 
       String newId = getNewId(xmiid);
 
       for (int i = 0; i < tagNames.size(); i++) {
         String tagName = (String) tagNames.get(i);
 
         if (taggedValuesMap.containsKey(tagName)) {
           //get TaggedValue element for this tagName
           tv = getTaggedValue(xmiid, tagName);
 
           index = index + 1;
 
           //add  taggedvalue element
           addTaggedValue(
             tv, tagName, (String) taggedValuesMap.get(tagName),
             newId + (new Integer(index)).toString(), xmiid, namespace);
         }
       }
     }
     catch (Exception e) {
     e.printStackTrace();
       log.error("Exception: " + e.getMessage());
       throw new Exception("Exception in buildTaggedValue: " + e.getMessage());
     }
   }
 
   /**
    * Build new id for xmi.id
    *
    * @param xmiid
    *
    * @return id throws JaxenException
    */
   private String getNewId(String xmiid) throws JaxenException, Exception {
     String id = null;
 
     try {
       String exp =
         "//*[local-name()='TaggedValue' and @modelElement='" + xmiid + "']";
 
       List tvs = (List) (new JDOMXPath(exp)).selectNodes(rootElement);
 
       //need to verify...
       if (tvs != null && tvs.size()>0){
           Element tv = (Element) tvs.get(tvs.size() - 1);
     
           if (tv != null) {
             id = (String) tv.getAttributeValue("xmi.id") + "_tag";
           }
       }
       else
       {
         id = xmiid + "_tag";
       }
     }
 
     catch (Exception e) {
       log.error("Exception while creating getNewId: " + e.getMessage());
 
       throw new Exception(
         "Exception while creating getNewId" + e.getMessage());
     }
 
     return id;
   }
 
   /**
    * Create and add TaggedValues to the model
    *
    * @param element
    * @param tagName
    * @param value
    * @param xmiid
    * @param namespace
    *
    * @throws Exception
    */
   private void addTaggedValue(
     Element element,
     String tagName,
     String value,
     String newId,
     String xmiid,
     Namespace namespace) throws Exception {
     try {
       if (element != null) {
         element.setAttribute("value", value);
       }
 
       else {
         Element taggedValue = new Element("TaggedValue", namespace);
 
         taggedValue.setNamespace(
           Namespace.getNamespace("UML", "href://org.omg/UML"));
 
         taggedValue.setAttribute(new Attribute("xmi.id", newId));
 
         taggedValue.setAttribute(new Attribute("tag", tagName));
 
         taggedValue.setAttribute(new Attribute("modelElement", xmiid));
 
         taggedValue.setAttribute(new Attribute("value", value));
 
         Element elem1 = getElement(rootElement, "//*[local-name()='Model'");
 
         Element parentElement = elem1.getParentElement();
 
         parentElement.addContent(taggedValue);
       }
     }
     catch (Exception e) {
     e.printStackTrace();
       log.error(
         "Exception while creating new TaggedValue element: " + e.getMessage());
 
       throw new Exception(
         "Exception in creating new TaggedValue element: " + e.getMessage());
     }
   }
 
 
   private List getLoadedModel(){
       return umlEntities;
   }
  
   private boolean doesFileExist(String fileName){
      File file = new File(fileName);
      return file.exists();
   }
     
    
   private ReportHandler getReportHandler(){
       if (reportHandler == null){
           reportHandler = new ReportHandler(); 
           reportHandler.addProgressListeners(getProgressListeners());      
       }
       return reportHandler;
   }
   
 
   private void removePackageName() throws Exception{
       Collection classes = getClasses(rootElement);
       if (classes == null || classes.isEmpty()){
           return;
       }
       
       Iterator it = classes.iterator();
       while (it.hasNext()){
           Element aClass = (Element)it.next();
           String fullName = aClass.getAttributeValue("name");
           int index = fullName.lastIndexOf(".");
           String name = fullName.substring(index+1);
           aClass.setAttribute("name", name);
       }
   }
  
  //testing
   public static void main(String[] args) {
    final String HINT = 
     "To generate EVS report please provide the XMI file name and specify the EVS report file name. \n" +
     "\tExample: java ModelAnnotator generateEVSReport <XMI file name> <EVS report file name>\n" +
     "To annotate the a XMI file please specify the input XMI file name, the EVS report file name and the output XMI file name.\n" +
     "\tExample: java ModelAnnotator annotateXMI <XMI file name> <EVS report file name> <output XMI file name>\n" ;
       
       //help info
     if (args==null || args.length==0 || "help".equalsIgnoreCase(args[0])){
         System.out.println("Refactored Semmantic Connector");
         System.out.println(HINT);
         return;
     }
 
     try{        
         ModelAnnotator sc = new ModelAnnotator();
         ProgressListener pl = new MyProgressListener();
         sc.addProgressListener(pl);
 
         if ("generateEVSReport".equalsIgnoreCase(args[0])){
             sc.generateEVSReport(args[1], args[2], true);
             return;
         }    
         if ("annotateModel".equalsIgnoreCase(args[0])){
             sc.annotateModel(args[1], args[2], true);
             return;
         }    
         if ("annotateXMI".equalsIgnoreCase(args[0])){
             sc.annotateXMI(args[1], args[2], args[3], true);
             return;
         }else{
             System.out.println("Refactored Semmantic Connector");
             System.out.println(HINT);
             return;
         }
     }catch (Exception e){
         e.printStackTrace();
         log.error(e);
     }
   }
 
 /*methods that are listed but not implemented in the previous version.
   public void addXMIElement() {
   }
 
   public void emailSemanticReport() {
   }
 
   public int getModelEntityCount() {
     return 0;
   }
 
   public boolean isValidXMI() {
     return false;
   }
 
   public void modifyXMIElement() {
   }
 */
 }
