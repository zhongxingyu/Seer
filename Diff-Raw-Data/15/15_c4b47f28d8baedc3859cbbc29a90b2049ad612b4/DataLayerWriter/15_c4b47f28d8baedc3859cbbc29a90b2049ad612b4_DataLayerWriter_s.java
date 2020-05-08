 package pipe.common.dataLayer;
 
 //Collections
 import java.io.File;
 import java.io.IOException;
 
 import java.net.URISyntaxException;
 import java.util.LinkedList;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import javax.xml.parsers.ParserConfigurationException;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 import javax.xml.transform.stream.StreamSource;
 
 import org.w3c.dom.Attr;
 import org.w3c.dom.DOMException;
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.Node;
 
 
 /**
  * Create DataLayerWriter object
  * @param DataLayer object containing net to save
  * @author Ben Kirby
  * @author Pere Bonet (minor changes)
  */
 public class DataLayerWriter {
 
    /** DataLayer object passed in to save */
    private DataLayerInterface netModel;
    
    /** Create a writer with the DataLayer object to save*/
    public DataLayerWriter(DataLayerInterface currentNet) {
       netModel = currentNet;
    }
 
    /**
     * Save the Petri-Net
     * @param filename URI location to save file
     * @throws ParserConfigurationException
     * @throws DOMException
     * @throws TransformerConfigurationException
     * @throws TransformerException
     */
    public void savePNML(File file) throws NullPointerException, IOException,
            ParserConfigurationException, DOMException,
            TransformerConfigurationException, TransformerException {
 
       // Error checking
       if (file == null) {
          throw new NullPointerException("Null file in savePNML");
       }
       /*    
       System.out.println("=======================================");
       System.out.println("dataLayer SAVING FILE=\"" + file.getCanonicalPath() + "\"");
       System.out.println("=======================================");
        */
       Document pnDOM = null;
 
       StreamSource xsltSource = null;
       Transformer transformer = null;
       try {
          // Build a Petri Net XML Document
          DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
          DocumentBuilder builder = builderFactory.newDocumentBuilder();
          pnDOM = builder.newDocument();
 
          Element PNML = pnDOM.createElement("pnml"); // PNML Top Level Element
          pnDOM.appendChild(PNML);
 
          Attr pnmlAttr = pnDOM.createAttribute("xmlns"); // PNML "xmlns" Attribute
          pnmlAttr.setValue("http://www.informatik.hu-berlin.de/top/pnml/ptNetb");
          PNML.setAttributeNode(pnmlAttr);
 
          Element NET = pnDOM.createElement("net"); // Net Element
          PNML.appendChild(NET);
          Attr netAttrId = pnDOM.createAttribute("id"); // Net "id" Attribute
          netAttrId.setValue("Net-One");
          NET.setAttributeNode(netAttrId);
          Attr netAttrType = pnDOM.createAttribute("type"); // Net "type" Attribute
          netAttrType.setValue("P/T net");
          NET.setAttributeNode(netAttrType);
 
          LinkedList<TokenClass> tokenclasses = netModel.getTokenClasses();
          for (int i = 0; i < tokenclasses.size(); i++) {
         	 if(tokenclasses.get(i).isEnabled() || !tokenclasses.get(i).getID().equals(""))
         		 NET.appendChild(createTokenClassElement(tokenclasses.get(i), pnDOM));
          }
          
          AnnotationNote[] labels = netModel.getLabels();
          for (int i = 0; i < labels.length; i++) {
             NET.appendChild(createAnnotationNoteElement(labels[i], pnDOM));
          }
 
          RateParameter[] rateParameters = netModel.getRateParameters();
          for (int i = 0; i < rateParameters.length; i++) {
             NET.appendChild(createDefinition(rateParameters[i], pnDOM));
          }
 
          Place[] places = netModel.getPlaces();
          for (int i = 0; i < places.length; i++) {
             NET.appendChild(createPlaceElement(places[i], pnDOM));
          }
 
          Transition[] transitions = netModel.getTransitions();
          for (int i = 0; i < transitions.length; i++) {
             NET.appendChild(createTransitionElement(transitions[i], pnDOM));
          }
 
          Arc[] arcs = netModel.getArcs();
          for (int i = 0; i < arcs.length; i++) {
             Element newArc = createArcElement(arcs[i], pnDOM);
 
             int arcPoints = arcs[i].getArcPath().getArcPathDetails().length;
             String[][] point = arcs[i].getArcPath().getArcPathDetails();
             for (int j = 0; j < arcPoints; j++) {
                newArc.appendChild(createArcPoint(point[j][0], point[j][1], point[j][2], pnDOM, j));
             }
             NET.appendChild(newArc);
          //newArc = null;
          }
 
          InhibitorArc[] inhibitorArcs = netModel.getInhibitors();
          for (int i = 0; i < inhibitorArcs.length; i++) {
             Element newArc = createArcElement(inhibitorArcs[i], pnDOM);
 
             int arcPoints = inhibitorArcs[i].getArcPath().getArcPathDetails().length;
             String[][] point = inhibitorArcs[i].getArcPath().getArcPathDetails();
             for (int j = 0; j < arcPoints; j++) {
                newArc.appendChild(createArcPoint(point[j][0],
                        point[j][1],
                        point[j][2],
                        pnDOM, j));
             }
             NET.appendChild(newArc);
          }
 
          StateGroup[] stateGroups = netModel.getStateGroups();
          for (int i = 0; i < stateGroups.length; i++) {
             Element newStateGroup = createStateGroupElement(stateGroups[i], pnDOM);
 
             int numConditions = stateGroups[i].numElements();
             String[] conditions = stateGroups[i].getConditions();
             for (int j = 0; j < numConditions; j++) {
                newStateGroup.appendChild(createCondition(conditions[j], pnDOM));
             }
             NET.appendChild(newStateGroup);
          }
          //stateGroups = null;         
 
          pnDOM.normalize();
          // Create Transformer with XSL Source File
          xsltSource = new StreamSource(Thread.currentThread().
                  getContextClassLoader().getResourceAsStream("xslt" +
                  System.getProperty("file.separator") + "GeneratePNML.xsl"));
 
          transformer = TransformerFactory.newInstance().newTransformer(xsltSource);
          // Write file and do XSLT transformation to generate correct PNML
          File outputObjectArrayList = file;//new File(filename); // Output for XSLT Transformation
          DOMSource source = new DOMSource(pnDOM);
          StreamResult result = new StreamResult(outputObjectArrayList);
          transformer.transform(source, result);
       } catch (ParserConfigurationException e) {
          // System.out.println("=====================================================================================");
          System.out.println("ParserConfigurationException thrown in savePNML() " +
                  ": dataLayerWriter Class : dataLayer Package: filename=\"" +
                  file.getCanonicalPath() + "\" xslt=\"" +
                  xsltSource.getSystemId() + "\" transformer=\"" +
                  transformer.getURIResolver() + "\"");
       // System.out.println("=====================================================================================");
       // e.printStackTrace(System.err);
       } catch (DOMException e) {
          // System.out.println("=====================================================================");
          System.out.println("DOMException thrown in savePNML() " +
                  ": dataLayerWriter Class : dataLayer Package: filename=\"" +
                  file.getCanonicalPath() + "\" xslt=\"" +
                  xsltSource.getSystemId() + "\" transformer=\"" +
                  transformer.getURIResolver() + "\"");
       // System.out.println("=====================================================================");
       // e.printStackTrace(System.err);
       } catch (TransformerConfigurationException e) {
          // System.out.println("==========================================================================================");
          System.out.println("TransformerConfigurationException thrown in savePNML() " +
                  ": dataLayerWriter Class : dataLayer Package: filename=\"" + file.getCanonicalPath() + "\" xslt=\"" + xsltSource.getSystemId() + "\" transformer=\"" + transformer.getURIResolver() + "\"");
       // System.out.println("==========================================================================================");
       // e.printStackTrace(System.err);
       } catch (TransformerException e) {
          // System.out.println("=============================================================================");
          System.out.println("TransformerException thrown in savePNML() : dataLayerWriter Class : dataLayer Package: filename=\"" + file.getCanonicalPath() + "\" xslt=\"" + xsltSource.getSystemId() + "\" transformer=\"" + transformer.getURIResolver() + "\"" + e);
       // System.out.println("=============================================================================");
       // e.printStackTrace(System.err);
       }
    }
 
    /**
     * Creates a Place Element for a PNML Petri-Net DOM
     * @param inputPlace Input Place
     * @param document Any DOM to enable creation of Elements and Attributes
     * @return Place Element for a PNML Petri-Net DOM
     */
    private Element createPlaceElement(Place inputPlace, Document document) {
       Element placeElement = null;
 
       if (document != null) {
          placeElement = document.createElement("place");
       }
 
       if (inputPlace != null) {
          Integer attrValue = null;
          Double positionXInput = inputPlace.getPositionXObject();
          Double positionYInput = inputPlace.getPositionYObject();
          String idInput = inputPlace.getId();
          String nameInput = inputPlace.getName();
          Double nameOffsetXInput = inputPlace.getNameOffsetXObject();
          Double nameOffsetYInput = inputPlace.getNameOffsetYObject();
          LinkedList<Marking> initialMarkingInput = inputPlace.getCurrentMarkingObject();
          Double markingOffsetXInput = inputPlace.getMarkingOffsetXObject();
          Double markingOffsetYInput = inputPlace.getMarkingOffsetYObject();
          Integer capacityInput = inputPlace.getCapacity();
 
          placeElement.setAttribute("positionX", (positionXInput != null ? String.valueOf(positionXInput) : ""));
          placeElement.setAttribute("positionY", (positionYInput != null ? String.valueOf(positionYInput) : ""));
          placeElement.setAttribute("name", (nameInput != null ? nameInput : (idInput != null && idInput.length() > 0 ? idInput : "")));
          placeElement.setAttribute("id", (idInput != null ? idInput : "error"));
          placeElement.setAttribute("nameOffsetX", (nameOffsetXInput != null ? String.valueOf(nameOffsetXInput) : ""));
          placeElement.setAttribute("nameOffsetY", (nameOffsetYInput != null ? String.valueOf(nameOffsetYInput) : ""));
          String markingOutput = initialMarkingInput.get(0).getTokenClass().getID() + "," + initialMarkingInput.get(0).getCurrentMarking();
          for(int i = 1; i < initialMarkingInput.size(); i++){
         	 markingOutput += "," + initialMarkingInput.get(i).getTokenClass().getID() + "," + initialMarkingInput.get(i).getCurrentMarking();
          }
          placeElement.setAttribute("initialMarking", markingOutput);
          placeElement.setAttribute("markingOffsetX", (markingOffsetXInput != null ? String.valueOf(markingOffsetXInput) : ""));
          placeElement.setAttribute("markingOffsetY", (markingOffsetYInput != null ? String.valueOf(markingOffsetYInput) : ""));
          placeElement.setAttribute("capacity", (capacityInput != null ? String.valueOf(capacityInput) : ""));
       }
       return placeElement;
    }
 
    /**
     * Creates a label Element for a PNML Petri-Net DOM
     * @param inputLabel input label
     * @param document Any DOM to enable creation of Elements and Attributes
     * @return label Element for a PNML Petri-Net DOM
     */
    private Element createAnnotationNoteElement(AnnotationNote inputLabel, Document document) {
       Element labelElement = null;
 
       if (document != null) {
          labelElement = document.createElement("labels");
       }
 
       if (inputLabel != null) {
          int positionXInput = inputLabel.getOriginalX();
          int positionYInput = inputLabel.getOriginalY();
          int widthInput = inputLabel.getNoteWidth();
          int heightInput = inputLabel.getNoteHeight();
          String nameInput = inputLabel.getNoteText();
          boolean borderInput = inputLabel.isShowingBorder();
 
          labelElement.setAttribute("positionX",
                  (positionXInput >= 0.0 ? String.valueOf(positionXInput) : ""));
          labelElement.setAttribute("positionY",
                  (positionYInput >= 0.0 ? String.valueOf(positionYInput) : ""));
          labelElement.setAttribute("width",
                  (widthInput >= 0.0 ? String.valueOf(widthInput) : ""));
          labelElement.setAttribute("height",
                  (heightInput >= 0.0 ? String.valueOf(heightInput) : ""));
          labelElement.setAttribute("border", String.valueOf(borderInput));
          labelElement.setAttribute("text", (nameInput != null ? nameInput : ""));
       }
       return labelElement;
    }
 
    /**
     * Creates a Transition Element for a PNML Petri-Net DOM
     * @param inputTransition Input Transition
     * @param document Any DOM to enable creation of Elements and Attributes
     * @return Transition Element for a PNML Petri-Net DOM
     */
    private Element createTransitionElement(Transition inputTransition,
            Document document) {
       Element transitionElement = null;
 
       if (document != null) {
          transitionElement = document.createElement("transition");
       }
 
       if (inputTransition != null) {
          Integer attrValue = null;
          Double positionXInput = inputTransition.getPositionXObject();
          Double positionYInput = inputTransition.getPositionYObject();
          Double nameOffsetXInput = inputTransition.getNameOffsetXObject();
          Double nameOffsetYInput = inputTransition.getNameOffsetYObject();
          String idInput = inputTransition.getId();
          String nameInput = inputTransition.getName();
          double aRate = inputTransition.getRate();
          boolean timedTrans = inputTransition.isTimed();
          boolean deterministicTrans = inputTransition.isDeterministic();
          boolean infiniteServer = inputTransition.isInfiniteServer(); 
          int orientation = inputTransition.getAngle();
          int priority = inputTransition.getPriority();
          double delay = inputTransition.getDelay();
          String rateParameter = "";
          if (inputTransition.getRateParameter() != null) {
             rateParameter = inputTransition.getRateParameter().getName();
          }
 
          transitionElement.setAttribute("positionX",
                  (positionXInput != null ? String.valueOf(positionXInput) : ""));
          transitionElement.setAttribute("positionY",
                  (positionYInput != null ? String.valueOf(positionYInput) : ""));
          transitionElement.setAttribute("nameOffsetX",
                  (nameOffsetXInput != null ? String.valueOf(nameOffsetXInput) : ""));
          transitionElement.setAttribute("nameOffsetY",
                  (nameOffsetYInput != null ? String.valueOf(nameOffsetYInput) : ""));
          transitionElement.setAttribute("name",
                  (nameInput != null ? nameInput : (idInput != null && idInput.length() > 0 ? idInput : "")));
          transitionElement.setAttribute("id",
                  (idInput != null ? idInput : "error"));
          transitionElement.setAttribute("rate",
                  (aRate != 1 ? String.valueOf(aRate) : "1.0"));
          transitionElement.setAttribute("timed", String.valueOf(timedTrans));
          transitionElement.setAttribute("deterministic", String.valueOf(deterministicTrans));
          transitionElement.setAttribute("infiniteServer",
                  String.valueOf(infiniteServer));
          transitionElement.setAttribute("angle", String.valueOf(orientation));
          transitionElement.setAttribute("priority", String.valueOf(priority));
          transitionElement.setAttribute("delay", String.valueOf(delay));
          transitionElement.setAttribute("parameter",
                  (rateParameter != null ? rateParameter : ""));
       }
       return transitionElement;
    }
 
    /**
     * Creates a Arc Element for a PNML Petri-Net DOM
     * @param inputArc Input Arc
     * @param document Any DOM to enable creation of Elements and Attributes
     * @return Arc Element for a PNML Petri-Net DOM
     */
    private Element createArcElement(Arc inputArc, Document document) {
       Element arcElement = null;
 
       if (document != null) {
          arcElement = document.createElement("arc");
       }
 
       if (inputArc != null) {
          String idInput = inputArc.getId();
          String sourceInput = inputArc.getSource().getId();
          String targetInput = inputArc.getTarget().getId();
          LinkedList<Marking> weightInput = inputArc.getWeight();
          // Double inscriptionPositionXInput = inputArc.getInscriptionOffsetXObject();        
          // Double inscriptionPositionYInput = inputArc.getInscriptionOffsetYObject();
          arcElement.setAttribute("id", (idInput != null ? idInput : "error"));
          arcElement.setAttribute("source", (sourceInput != null ? sourceInput : ""));
          arcElement.setAttribute("target", (targetInput != null ? targetInput : ""));
          arcElement.setAttribute("type", inputArc.getType());
 
         String weightOutput = weightInput.get(0).getTokenClass().getID() + "," + weightInput.get(0).getCurrentMarking();
         for(int i = 1; i < weightInput.size(); i++){
         	 weightOutput += "," + weightInput.get(i).getTokenClass().getID() + "," + weightInput.get(i).getCurrentMarking();
          }
          arcElement.setAttribute("inscription", weightOutput);
          // arcElement.setAttribute("inscriptionOffsetX", (inscriptionPositionXInput != null ? String.valueOf(inscriptionPositionXInput) : ""));
          // arcElement.setAttribute("inscriptionOffsetY", (inscriptionPositionYInput != null ? String.valueOf(inscriptionPositionYInput) : ""));
 
          if (inputArc instanceof NormalArc) {
             boolean tagged = ((NormalArc) inputArc).isTagged();
             arcElement.setAttribute("tagged", tagged ? "true" : "false");
          }
       }
       return arcElement;
    }
 
    private Element createArcPoint(String x, String y, String type,
            Document document, int id) {
       Element arcPoint = null;
 
       if (document != null) {
          arcPoint = document.createElement("arcpath");
       }
       String pointId = String.valueOf(id);
       if (pointId.length() < 3) {
          pointId = "0" + pointId;
       }
       if (pointId.length() < 3) {
          pointId = "0" + pointId;
       }
       arcPoint.setAttribute("id", pointId);
       arcPoint.setAttribute("xCoord", x);
       arcPoint.setAttribute("yCoord", y);
       arcPoint.setAttribute("arcPointType", type);
 
       return arcPoint;
    }
 
    private Node createDefinition(RateParameter inputParameter, Document document) {
       Element labelElement = null;
 
       if (document != null) {
          labelElement = document.createElement("definitions");
       }
 
       if (inputParameter != null) {
 
          int positionXInput = inputParameter.getOriginalX();//getX()
          int positionYInput = inputParameter.getOriginalY();//getY()
          int widthInput = inputParameter.getNoteWidth();
          int heightInput = inputParameter.getNoteHeight();
          double valueInput = inputParameter.getValue();
          String idInput = inputParameter.getName();
          boolean borderInput = inputParameter.isShowingBorder();
          labelElement.setAttribute("defType", "real");
          labelElement.setAttribute("expression", String.valueOf(valueInput));
          labelElement.setAttribute("id", idInput);
          labelElement.setAttribute("name", idInput);
          labelElement.setAttribute("type", "text");
          labelElement.setAttribute("positionX",
                  (positionXInput >= 0.0 ? String.valueOf(positionXInput) : ""));
          labelElement.setAttribute("positionY",
                  (positionYInput >= 0.0 ? String.valueOf(positionYInput) : ""));
 
          labelElement.setAttribute("width",
                  (widthInput >= 0.0 ? String.valueOf(widthInput) : ""));
          labelElement.setAttribute("height",
                  (heightInput >= 0.0 ? String.valueOf(heightInput) : ""));
          labelElement.setAttribute("border", String.valueOf(borderInput));
       }
       return labelElement;
    }
 
    /**
     * Creates a State Group Element for a PNML Petri-Net DOM
     *
     * @param inputStateGroup Input State Group
     * @param document Any DOM to enable creation of Elements and Attributes
     * @return State Group Element for a PNML Petri-Net DOM
     * @author Barry Kearns, August 2007
     */
    private Element createStateGroupElement(StateGroup inputStateGroup, Document document) {
       Element stateGroupElement = null;
 
       if (document != null) {
          stateGroupElement = document.createElement("stategroup");
       }
 
       if (inputStateGroup != null) {
          String idInput = inputStateGroup.getId();
          String nameInput = inputStateGroup.getName();
 
          stateGroupElement.setAttribute("name",
                  (nameInput != null ? nameInput
                  : (idInput != null && idInput.length() > 0 ? idInput : "")));
          stateGroupElement.setAttribute("id", (idInput != null ? idInput : "error"));
       }
       return stateGroupElement;
    }
 
    /**
     * Creates a TokenClass Element for a PNML Petri-Net DOM
     * @param inputTokenClass inpout TokenClass 
     * @param document Any DOM to enable creation of Elements and Attributes
     * @return TokenClass Element for a PNML Petri-Net DOM
     */
    private Element createTokenClassElement(TokenClass inputTokenClass, Document document) {
       Element tokenClassElement = null;
 
       if (document != null) {
          tokenClassElement = document.createElement("tokenclass");
       }
 
       if (inputTokenClass != null) {
          String idInput = inputTokenClass.getID();
          Integer redInput  = inputTokenClass.getColour().getRed();
          Integer greenInput  = inputTokenClass.getColour().getGreen();
          Integer blueInput  = inputTokenClass.getColour().getBlue();
          boolean enabledInput = inputTokenClass.isEnabled();
 
          tokenClassElement.setAttribute("id", (idInput != null ? idInput : "error"));
          tokenClassElement.setAttribute("enabled", String.valueOf(enabledInput));
          tokenClassElement.setAttribute("red", (redInput != null ? String.valueOf(redInput) : ""));
          tokenClassElement.setAttribute("green", (greenInput != null ? String.valueOf(greenInput) : ""));
          tokenClassElement.setAttribute("blue", (blueInput != null ? String.valueOf(blueInput) : ""));
       }
       return tokenClassElement;
    }
    
    private Element createCondition(String condition, Document document) {
       Element stateCondition = null;
 
       if (document != null) {
          stateCondition = document.createElement("statecondition");
       }
 
       stateCondition.setAttribute("condition", condition);
       return stateCondition;
    }
 
 
 
 
    public static void saveTemporaryFile(DataLayerInterface data, String className) {
       // desar la xarxa a un arxiu temporal per si hi ha cap problema
       try {
          //current working dir?
          File dir = new File(Thread.currentThread().getContextClassLoader().
                  getResource("").toURI());
 
          // temporary file
          File tempFile = File.createTempFile("petrinet[" + className + "][" +
                  System.nanoTime() + "]", ".xml", dir);
 
          DataLayerWriter saveModel = new DataLayerWriter(data);
 
          saveModel.savePNML(tempFile);
 
          // Delete temp file when program exits. If PIPE crashes,
          // tempFile won't be deleted.
          tempFile.deleteOnExit();
 
          // If the directory does not exists, IOException will be thrown
          // and temporary file will not be created.
          System.out.println("Temporary file created at : " + tempFile.getPath());
       } catch (URISyntaxException ex) {
          Logger.getLogger(className).log(Level.SEVERE, null, ex);
       } catch (IOException ioe) {
          System.out.println("Exception creating temporary file : " + ioe);
       } catch (NullPointerException ex) {
          System.out.println("Exception creating temporary file : " + ex);
          Logger.getLogger(className).log(Level.SEVERE, null, ex);
       } catch (ParserConfigurationException ex) {
          System.out.println("Exception creating temporary file : " + ex);
          Logger.getLogger(className).log(Level.SEVERE, null, ex);
       } catch (DOMException ex) {
          System.out.println("Exception creating temporary file : " + ex);
          Logger.getLogger(className).log(Level.SEVERE, null, ex);
       } catch (TransformerConfigurationException ex) {
          System.out.println("Exception creating temporary file : " + ex);
          Logger.getLogger(className).log(Level.SEVERE, null, ex);
       } catch (TransformerException ex) {
          System.out.println("Exception creating temporary file : " + ex);
          Logger.getLogger(className).log(Level.SEVERE, null, ex);
       }
    }
 
 }
