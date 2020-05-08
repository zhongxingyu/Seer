 package vooga.scroller.level_editor.controllerSuite;
 
 import java.util.HashMap;
 import java.util.Map;
 import vooga.scroller.level_editor.ILevelEditor;
 import vooga.scroller.level_editor.StartPoint;
 import vooga.scroller.level_editor.library.BackgroundLib;
 import vooga.scroller.level_editor.library.IBackgroundLibrary;
 import vooga.scroller.level_editor.library.ISpriteLibrary;
 import vooga.scroller.level_editor.view.LEView;
 import vooga.scroller.sprites.Sprite;
import vooga.scroller.sprites.interfaces.IDoor;
 import vooga.scroller.util.mvc.IWindow;
 import vooga.scroller.util.mvc.vcFramework.Window;
 
 
 public class ToolsManager {
 
     private ISpriteLibrary mySpriteLib;
     private IBackgroundLibrary myBackgroundLib;
     private Map<Integer, Sprite> mySpriteMap;
     private LETools myViewTools;
 
 //    public ToolsManager (ISpriteLibrary l) {
 //        this(l, new BackgroundLib(new String[0]));
 //    }
 
     public ToolsManager (ISpriteLibrary l, IBackgroundLibrary bgLib) {
         myBackgroundLib = bgLib;
         mySpriteLib = l;
         setTools();
     }
 
     private void setTools () {
 
         int i = 1;
         mySpriteMap = new HashMap<Integer, Sprite>();
         myViewTools = new LETools();
         myViewTools.addBackgrounds(myBackgroundLib.getBackgrounds());
         Sprite sprite;
         for (Class<? extends Sprite> c : mySpriteLib.getSpritesClasses()) {
             try {
                 sprite = (Sprite) c.newInstance();
                if(IDoor.class.isAssignableFrom(sprite.getClass())){
                    setupTool(-i,sprite);
                }
                else{
                    setupTool(i, sprite);
                }
             }
             catch (InstantiationException e) {
                 e.printStackTrace();
             }
             catch (IllegalAccessException e) {
                 e.printStackTrace();
             }
             i++;
         }
         sprite = new StartPoint();
         setupTool(ILevelEditor.START_ID, sprite);
     }
 
     private void setupTool (int i, Sprite sprite) {
        System.out.println(i+" "+sprite.getClass().getName());
         mySpriteMap.put(i, sprite);
         myViewTools.addSpriteOption(sprite, i);
     }
 
     public LETools getViewTools () {
         return myViewTools;
     }
 
     public Map<Integer, Sprite> getSpriteMap () {
         return mySpriteMap;
     }
 
     public String getSpriteLibPath () {
         return mySpriteLib.getClass().getName();
     }
 }
