 package vooga.towerdefense.gameeditor.gameloader.xmlloaders;
 
 import java.awt.Dimension;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.InvocationTargetException;
 import java.util.List;
 import java.util.Map;
 import javax.swing.JPanel;
 import org.w3c.dom.Element;
 import util.XMLTool;
 import vooga.towerdefense.controller.Controller;
 import vooga.towerdefense.view.TDView;
 import vooga.towerdefense.view.gamescreens.GameElementInformationScreen;
 import vooga.towerdefense.view.gamescreens.GameStatsScreen;
 import vooga.towerdefense.view.gamescreens.MapScreen;
 import vooga.towerdefense.view.gamescreens.MultipleScreenPanel;
 import vooga.towerdefense.view.gamescreens.NextWaveScreen;
 import vooga.towerdefense.view.gamescreens.ShopScreen;
 
 /**
  * ViewXMLLoader loads in the View from the XML file.
  * @author Angelica Schwartz
  */
 public class ViewXMLLoader {
     private XMLTool myXMLTool;
     
     private static final String VIEW_TAG = "view";
     private static final String DIMENSION_TAG = "dimension";
     private static final String LOCATION_TAG = "location";
     private static final String MAPSCREEN_TAG = "MapScreen";
     private static final String SHOPSCREEN_TAG = "ShopScreen";
     private static final String GAME_STATS_SCREEN_TAG = "GameStatsScreen";
     private static final String GAME_ELEMENTS_SCREEN_TAG = "GameElementInformationScreen";
     private static final String NEXT_WAVE_SCREEN_TAG = "NextWaveScreen";
     private static final String MULTIPLE_SCREEN_PANEL_TAG = "MultipleScreenPanel";
     private static final String VIEWSCREENS_PATH = "vooga.towerdefense.view.gamescreens.";
     
     public ViewXMLLoader(XMLTool xmlTool) {
         myXMLTool = xmlTool;
     }
     
    /**
     * makes the view from the XML file.
     * @param view
     * @param controller
     * @return the updated TDView
     * @throws InvocationTargetException 
     * @throws IllegalAccessException 
     * @throws InstantiationException 
     * @throws ClassNotFoundException 
     * @throws IllegalArgumentException 
     */
     public TDView makeView(TDView view, Controller controller) throws IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
         Element viewElement = myXMLTool.getElement(VIEW_TAG);
         List<Element> subElements = myXMLTool.getChildrenList(viewElement);
         for (int i = 0; i < subElements.size(); i++) {
             if (myXMLTool.getTagName(subElements.get(i)).equals(DIMENSION_TAG)) {
                 Element dimensionElement = subElements.get(i);
                 Dimension dimension = makeDimensionFrom(myXMLTool.getContent(dimensionElement));
                 view.setSize(dimension);
                 subElements.remove(i);
                 i--;
             }
             else if (!(myXMLTool.getTagName(subElements.get(i)).equals(MULTIPLE_SCREEN_PANEL_TAG))) {
                 Element element = subElements.get(i);
                JPanel screen;
                screen = getScreen(view, element, controller);
                 Element locationElement = myXMLTool.getChildrenElementMap(element).get(LOCATION_TAG);
                 String location = myXMLTool.getContent(locationElement);
                 view.addScreen(screen, location);
             }
             else {
                 Element multiplePanelScreen = subElements.get(i);
                 MultipleScreenPanel multipleScreenPanel = (MultipleScreenPanel)getScreen(view, multiplePanelScreen, controller);
                 Element dimensionElement = myXMLTool.getChildrenElementMap(multiplePanelScreen).get(DIMENSION_TAG);
                 Dimension dimension = makeDimensionFrom(myXMLTool.getContent(dimensionElement));
                 multipleScreenPanel.setSize(dimension);
                 Element locationElement = myXMLTool.getChildrenElementMap(multiplePanelScreen).get(LOCATION_TAG);
                 String location = myXMLTool.getContent(locationElement);
                 multipleScreenPanel = createMultipleScreenPanel(view, multipleScreenPanel, multiplePanelScreen, controller);
                 view.addScreen(multipleScreenPanel, location);
             }
         }
         return view;
     }
     
     private MultipleScreenPanel createMultipleScreenPanel(TDView view, MultipleScreenPanel panel, Element element, Controller controller) throws IllegalArgumentException, ClassNotFoundException, InstantiationException, IllegalAccessException, InvocationTargetException {
         Map<String, Element> subMultiples = myXMLTool.getChildrenElementMap(element);
         for (String s : subMultiples.keySet()) {
             if (!((s.equals("dimension")) || s.equals("location"))) {
                 Element subScreenElement = subMultiples.get(s);
                 JPanel subScreen = getScreen(view, subScreenElement, controller);
                 Element locElement = myXMLTool.getChildrenElementMap(subScreenElement).get(LOCATION_TAG);
                 String location = myXMLTool.getContent(locElement);
                 panel.addScreen(subScreen, location);
             }
         }
         return panel;
     }
     
     /**
      * adds this screen to the view.
      * @param element
      * @param controller
      * @return the screen
      * @throws ClassNotFoundException
      * @throws IllegalArgumentException
      * @throws InstantiationException
      * @throws IllegalAccessException
      * @throws InvocationTargetException
      */
    @SuppressWarnings("rawtypes")
     private JPanel getScreen(TDView view, Element element, Controller controller) throws ClassNotFoundException, IllegalArgumentException, InstantiationException, IllegalAccessException, InvocationTargetException {
         Map<String, Element> subElements = myXMLTool.getChildrenElementMap(element);
         Element dimensionElement = subElements.get(DIMENSION_TAG);
         Dimension dimension = makeDimensionFrom(myXMLTool.getContent(dimensionElement));
         Class c = Class.forName(VIEWSCREENS_PATH + myXMLTool.getTagName(element));
         Constructor[] constructors = c.getConstructors();
         Constructor cons = constructors[0];
         JPanel screen = (JPanel) cons.newInstance(dimension, controller);
         if (myXMLTool.getTagName(element).equals(MAPSCREEN_TAG)) {
             view.setMapScreen((MapScreen)screen);
         }
         else if (myXMLTool.getTagName(element).equals(SHOPSCREEN_TAG)) {
             view.setShopScreen((ShopScreen)screen);
         }
         else if (myXMLTool.getTagName(element).equals(GAME_ELEMENTS_SCREEN_TAG)) {
             view.setGameElementInformationScreen((GameElementInformationScreen)screen);
         }
         else if (myXMLTool.getTagName(element).equals(GAME_STATS_SCREEN_TAG)) {
             view.setStatsScreen((GameStatsScreen)screen);
         }
         else if (myXMLTool.getTagName(element).equals(NEXT_WAVE_SCREEN_TAG)) {
             view.setNextWaveScreen((NextWaveScreen)screen);
         }
         return screen;
     }
     
     /**
      * helper method to parse the dimension correctly.
      * @param dimensionString
      * @return
      */
     private Dimension makeDimensionFrom(String dimensionString) {
         String[] dimensionPieces = dimensionString.split(", ");
         String width = dimensionPieces[0];
         String height = dimensionPieces[1];
         return new Dimension(Integer.parseInt(width), Integer.parseInt(height));
     }
     
     
 }
