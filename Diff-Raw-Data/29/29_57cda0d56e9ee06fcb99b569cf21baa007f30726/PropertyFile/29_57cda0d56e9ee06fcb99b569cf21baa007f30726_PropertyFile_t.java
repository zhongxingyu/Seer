 package de.fhk.spacequest.vis.gui;
 
 import org.w3c.dom.Document;
 import org.w3c.dom.Element;
 import org.w3c.dom.NodeList;
 
 import javax.xml.parsers.DocumentBuilder;
 import javax.xml.parsers.DocumentBuilderFactory;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 /**
  * Klasse, die Konfigurationen aus einer XML-Datei entnimmt und daraus das Benutzerinterface erzeugt.
  *
  * @author Adrian Wagner
  */
public class PropertyFile {
 
     private static String[] elementTypes = {"button", "clock", "slider", "factory"};
     private static String[] buttonSubtypes = {"start_toggle", "preset_toggle"};
     private static String[] clockSubtypes = {"analog", "digital"};
     private static String[] sliderSubtypes = {"zoom"};
     private static String[] factorySubtypes = {"phase_button"};
 
    public List<GUIComponent> createGUIElements(String pathToFile, GUI gui) throws Exception {
 
         ArrayList<GUIComponent> components = new ArrayList<GUIComponent>();
 
         DocumentBuilderFactory builderFactory = DocumentBuilderFactory.newInstance();
         DocumentBuilder builder = builderFactory.newDocumentBuilder();
        InputStream in = getClass().getResourceAsStream(pathToFile);
         Document template = builder.parse(in);
 
 
         NodeList elementNodes = template.getElementsByTagName("ELEMENT");
 
         for (int index = 0; index < elementNodes.getLength(); index++) {
             Element node = (Element) elementNodes.item(index);
             String type = node.getAttribute("type");
 
             int typeNumber = 0;
 
             for (; typeNumber < elementTypes.length; typeNumber++) {
                 if (type.equals(elementTypes[typeNumber])) {
                     break;
                 }
             }
 
             switch (typeNumber) {
                 case (0):
                     components.add(createButtonFromNode(node, gui));
                     //System.out.println("Button");
                     break;
                 case (1):
                     components.add(createClockFromNode(node, gui));
                     //System.out.println("Clock");
                     break;
                 case (2):
                     components.add(createSliderFromNode(node, gui));
                     //System.out.println("Slider");
                     break;
                 case (3):
                     createFactoryFromNode(node, components, gui);
                     //System.out.println("Factory");
                     break;
                 default:
                     break;
             }
         }
 
         return components;
 
     }
 
    private GUIComponent createButtonFromNode(Element node, GUI gui) {
 
         GUIComponent component = null;
 
         String subtype = node.getAttribute("subtype");
         int subtypeNumber = 0;
 
         for (; subtypeNumber < buttonSubtypes.length; subtypeNumber++) {
             if (subtype.equals(buttonSubtypes[subtypeNumber])) {
                 break;
             }
         }
 
         try {
             switch (subtypeNumber) {
                 case (0):
                     component = new StartToggleButton((float) Integer.parseInt(node.getAttribute("x")), (float) Integer.parseInt(node.getAttribute("y")), gui, gui.iniFile.get(3), gui.iniFile.get(4));
                     break;
                 case (1):
                     component = new PresetToggleButton((float) Integer.parseInt(node.getAttribute("x")), (float) Integer.parseInt(node.getAttribute("y")), gui, gui.iniFile.get(3), gui.iniFile.get(4));
                     break;
                 default:
                     break;
             }
         } catch (Exception error) {
             System.out.println("Error Button");
             component = null;
         }
 
         return component;
     }
 
    private GUIComponent createClockFromNode(Element node, GUI gui) {
 
         GUIComponent component = null;
 
         String subtype = node.getAttribute("subtype");
         int subtypeNumber = 0;
 
         for (; subtypeNumber < clockSubtypes.length; subtypeNumber++) {
             if (subtype.equals(clockSubtypes[subtypeNumber])) {
                 break;
             }
         }
 
         try {
             switch (subtypeNumber) {
                 case (0):
                     component = new AnalogClock((float) Integer.parseInt(node.getAttribute("x")), (float) Integer.parseInt(node.getAttribute("y")), (float) Integer.parseInt(node.getAttribute("size")), gui, gui.iniFile.get(2));
                     break;
                 case (1):
                     component = new DigitalClock((float) Integer.parseInt(node.getAttribute("x")), (float) Integer.parseInt(node.getAttribute("y")), gui, gui.iniFile.get(2));
                     break;
                 default:
                     break;
             }
         } catch (Exception error) {
             System.out.println("Error Clock");
             component = null;
         }
 
         return component;
     }
 
    private GUIComponent createSliderFromNode(Element node, GUI gui) {
 
         GUIComponent component = null;
 
         String subtype = node.getAttribute("subtype");
         int subtypeNumber = 0;
 
         for (; subtypeNumber < sliderSubtypes.length; subtypeNumber++) {
             if (subtype.equals(sliderSubtypes[subtypeNumber])) {
                 break;
             }
         }
 
         try {
             switch (subtypeNumber) {
                 case (0):
                     component = new ZoomSlider((float) Integer.parseInt(node.getAttribute("x")), (float) Integer.parseInt(node.getAttribute("y")), Float.parseFloat(node.getAttribute("min")), Float.parseFloat(node.getAttribute("max")), (float) Integer.parseInt(node.getAttribute("size")), gui, gui.iniFile.get(7));
                     break;
                 default:
                     break;
             }
         } catch (Exception error) {
             System.out.println("Error Slider");
             component = null;
         }
 
         return component;
     }
 
    private void createFactoryFromNode(Element node, Collection<GUIComponent> components, GUI gui) {
 
         String subtype = node.getAttribute("subtype");
 
         try {
             if (subtype.equals("phase_button")) {
                 for (PhaseButton pb : PhaseButtonFactory.createPhaseButtons((float) Integer.parseInt(node.getAttribute("x")), (float) Integer.parseInt(node.getAttribute("y")), node.getAttribute("alignment").equals("vertical"), gui, gui.iniFile.get(3), gui.iniFile.get(4))) {
                     components.add(pb);
                 }
             }
         } catch (Exception error) {
             System.out.println("Error Factory");
         }
     }
 }
