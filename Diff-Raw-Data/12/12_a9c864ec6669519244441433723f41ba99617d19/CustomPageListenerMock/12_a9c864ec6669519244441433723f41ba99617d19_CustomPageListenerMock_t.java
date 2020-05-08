 package controllers.origo.coretest;
 
 import play.modules.origo.core.Node;
 import play.modules.origo.core.annotations.OnLoad;
 import play.modules.origo.core.annotations.UIElementType;
 import play.modules.origo.core.ui.NavigationElement;
 import play.modules.origo.core.ui.UIElement;
 
 import java.util.Collection;
 
 public class CustomPageListenerMock {
 
     /**
      * TODO: Just for testing, remove eventually
      *
      * @param node the node to modify
      */
    @OnLoad(type = OnLoad.TYPE_NODE, with = "models.origo.core.BasicPage")
     public static void setupTestData(Node node) {
 
         UIElement metaElement = new UIElement(UIElementType.META, 10);
         metaElement.getAttributes().put("http-equiv", "Content-Type");
         metaElement.getAttributes().put("content", "text/html; charset=utf-8");
         node.addHeadUIElement(metaElement);
 
         UIElement styleElementWithContent = new UIElement(UIElementType.STYLE, 10, "\n\tbody {\n\t\tbackground-color: #ff0000;\n}");
         styleElementWithContent.put("type", "text/css");
         node.addHeadUIElement(styleElementWithContent);
 
         UIElement styleElementWithSrc = new UIElement(UIElementType.STYLE, 10);
         styleElementWithSrc.put("type", "text/css");
         styleElementWithSrc.put("src", "test/css/style_head.css");
         node.addHeadUIElement(styleElementWithSrc);
 
         UIElement inlineStyleElement = new UIElement(UIElementType.STYLE, 10);
         inlineStyleElement.put("type", "text/css");
         inlineStyleElement.put("src", "test/css/style_inline.css");
         node.addUIElement(inlineStyleElement);
 
         UIElement panel = new UIElement(UIElementType.PANEL, 10);
         node.addUIElement(panel);
 
         panel.addChild(new UIElement(UIElementType.TEXT, 12, "Bla bla"));
 
     }
 
    @OnLoad(type = OnLoad.TYPE_NAVIGATION)
     public static void setupTestNavigation(Node node, Collection<NavigationElement> navigationElements, String section) {
         navigationElements.add(new NavigationElement(section, "Programmatically Added", "http://google.com"));
     }
 
 }
