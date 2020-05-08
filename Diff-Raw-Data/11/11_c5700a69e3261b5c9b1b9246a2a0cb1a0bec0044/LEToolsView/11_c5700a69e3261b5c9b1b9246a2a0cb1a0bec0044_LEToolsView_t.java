 package vooga.scroller.level_editor.view;
 
 import java.awt.Component;
 import java.awt.GridLayout;
 import java.awt.Image;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.Map;
 import javax.swing.BoxLayout;
 import javax.swing.Icon;
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.SpringLayout;
 import vooga.scroller.level_editor.commands.CommandConstants;
 import vooga.scroller.level_editor.controllerSuite.LETools;
 import vooga.scroller.util.Renderable;
 import vooga.scroller.util.Renderer;
 import vooga.scroller.util.mvc.IView;
 import vooga.scroller.util.mvc.vcFramework.WindowComponent;
 import vooga.scroller.viewUtil.BackgroundButton;
 import vooga.scroller.viewUtil.EasyGridFactory;
 import vooga.scroller.viewUtil.RadioGroup;
 
 
 /**
  * EditorView is responsible for presenting editing tools to the user.
  * It provides a UI for the user to select various kind of sprites or
  * environment elements and add them to the actual level being edited.
  * 
  * @author Dagbedji Fagnisse
  * 
  */
 public class LEToolsView extends WindowComponent implements Renderer<LETools> {
 
     public class SelectBackgroundListener implements ActionListener {
 
         @Override
         public void actionPerformed (ActionEvent e) {
             setSelectedBackground(e.getActionCommand());
         }
 
     }
 
     private class SelectSpriteListener implements ActionListener {
 
         @Override
         public void actionPerformed (ActionEvent e) {
             setSelectedSprite(e.getActionCommand());
         }
 
     }
 
     private static final long serialVersionUID = 1L;
 
     public static double getDefaultHeightRatio () {
         return LevelEditing.VIEW_CONSTANTS.DEFAULT_TOOLSVIEW_HEIGHT_RATIO;
     }
 
     public static double getDefaultWidthRatio () {
         return LevelEditing.VIEW_CONSTANTS.DEFAULT_TOOLSVIEW_WIDTH_RATIO;
     }
 
     private JPanel myOtherUI;
 
     private String mySelectedSpecialPoint;
     private String mySelectedSprite;
     private JPanel mySpriteUI;
 
     private JTabbedPane myTabs;
 
     private LETools myTools;
 
     public LEToolsView (LETools leTools, IView parent) {
         super(parent, getDefaultWidthRatio(), getDefaultHeightRatio());
         myTabs = new JTabbedPane();
         myTools = leTools;
         mySpriteUI = new JPanel();
         mySpriteUI.setLayout(new BoxLayout(mySpriteUI, BoxLayout.PAGE_AXIS));
 //        mySpriteUI.setLayout(new SpringLayout());
         for (Map<Object, String> m : myTools.getAllSprites()) {
            if (m.size()>0) {
             JPanel spriteButtons = new RadioGroup(this.getSize(), new SelectSpriteListener(), m);
             mySpriteUI.add(spriteButtons);
            }
         }
 
         myOtherUI = new JPanel();
 //        myOtherUI.setLayout(new GridLayout());
         JPanel backgroundView = new RadioGroup(this.getSize(), new SelectBackgroundListener(),
                                                myTools.getBackgrounds());
         myOtherUI.add(backgroundView);
         myTabs.add(mySpriteUI, "Sprites");
         myTabs.add(myOtherUI, "Other");
         EasyGridFactory.layout(this, myTabs);
     }
 
     @Override
     public LETools getRenderable () {
         // TODO Auto-generated method stub
         return null;
     }
 
     public String getSelectedSpecialPoint () {
         return mySelectedSpecialPoint;
     }
 
     public String getSelectedSpriteID () {
         return mySelectedSprite;
     }
 
     public int getSelectedTab () {
         return myTabs.getSelectedIndex();
     }
 
     @Override
     public void render (LETools renderable) {
         // TODO Auto-generated method stub
 
     }
 
     @Override
     public void render (Renderable r) { // TODO - Should not really be used/needed
         if (r instanceof LETools) {
             LETools t = (LETools) r;
             myTools = t;
             t.initializeRenderer(getResponsible());
         }
     }
 
     @Override
     public void setRenderable (LETools tools) {
         myTools = tools;
     }
 
     public void setSelectedBackground (String id) {
         process(CommandConstants.CHANGE_BACKGROUND + CommandConstants.SPACE + id);       
     }
 
     private void setSelectedSprite (String spriteID) {
         mySelectedSprite = spriteID;
     }
 
     public void setTools (LETools t) {
         myTools = t;
     }
 
 }
