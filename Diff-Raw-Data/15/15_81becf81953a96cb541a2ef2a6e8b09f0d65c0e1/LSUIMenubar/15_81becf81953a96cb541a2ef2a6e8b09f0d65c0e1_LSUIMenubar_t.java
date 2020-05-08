 package rocks6205.editor.viewcomponents.toolbars;
 
 //~--- non-JDK imports --------------------------------------------------------
 
 import rocks6205.editor.actions.LSAbstractAction.ChineseLanguageToggleAction;
 import rocks6205.editor.actions.LSAbstractAction.DeleteAction;
 import rocks6205.editor.actions.LSAbstractAction.DeselectAllAction;
import rocks6205.editor.actions.LSAbstractAction.DocumentPropertiesAction;
 import rocks6205.editor.actions.LSAbstractAction.EnglishLanguageToggleAction;
 import rocks6205.editor.actions.LSAbstractAction.ExitAction;
 import rocks6205.editor.actions.LSAbstractAction.GroupAction;
 import rocks6205.editor.actions.LSAbstractAction.JapaneseLanguageToggleAction;
 import rocks6205.editor.actions.LSAbstractAction.MalayLanguageToggleAction;
 import rocks6205.editor.actions.LSAbstractAction.NewDocumentAction;
 import rocks6205.editor.actions.LSAbstractAction.OpenFileAction;
 import rocks6205.editor.actions.LSAbstractAction.SaveFileAction;
 import rocks6205.editor.actions.LSAbstractAction.SaveFileAsAction;
 import rocks6205.editor.actions.LSAbstractAction.SelectAllAction;
 import rocks6205.editor.actions.LSAbstractAction.TamilLanguageToggleAction;
 import rocks6205.editor.actions.LSAbstractAction.ThaiLanguageToggleAction;
 import rocks6205.editor.actions.LSAbstractAction.ToggleCodeViewAction;
 import rocks6205.editor.actions.LSAbstractAction.UngroupAction;
 import rocks6205.editor.actions.LSAbstractAction.ZoomInViewAction;
 import rocks6205.editor.actions.LSAbstractAction.ZoomOutViewAction;
 import rocks6205.editor.core.LSEditor;
 import rocks6205.editor.core.LSView;
 import rocks6205.editor.viewcomponents.LSUIProtocol;
 
 //~--- JDK imports ------------------------------------------------------------
 
 import java.awt.event.KeyEvent;
 
 import java.util.ResourceBundle;
 
 import javax.swing.ButtonGroup;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 
 /**
  * The main menu bar for the GUI.
  *
  * @author Toh Huey Jing
  *
  * @since 1.2
  *
  */
 public final class LSUIMenubar extends JMenuBar implements LSUIProtocol {
 
     /**
      * Parent component
      */
     private LSView parent;
 
     /*
      * GUI COMPONENTS
      */
     private JMenu       fileMenu;
     private JMenu       editMenu;
     private JMenu       insertMenu;
     private JMenu       windowMenu;
     private JMenu       helpMenu;
     private JMenu       languageMenu;
     private ButtonGroup languageGrp;
     private JMenuItem   newMenuItem;
     private JMenuItem   openMenuItem;
     private JMenuItem   saveMenuItem;
     private JMenuItem   saveAsMenuItem;
     private JMenuItem   docPropMenuItem;
     private JMenuItem   exitMenuItem;
     private JMenuItem   selectAllMenuItem;
     private JMenuItem   deselectAllMenuItem;
     private JMenuItem   groupMenuItem;
     private JMenuItem   ungroupMenuItem;
     private JMenuItem   deleteMenuItem;
     private JMenuItem   insertRectMenuItem;
     private JMenuItem   insertCircleMenuItem;
     private JMenuItem   insertLineMenuItem;
     private JMenuItem   zoomInMenuItem;
     private JMenuItem   zoomOutMenuItem;
     private JMenuItem   toggleCodeAreaViewMenuItem;
     private JMenuItem   faqMenuItem;
     private JMenuItem   aboutMenuItem;
     private JMenuItem   malayLanguageMenuItem;
     private JMenuItem   englishLanguageMenuItem;
     private JMenuItem   japaneseLanguageMenuItem;
     private JMenuItem   chineseLanguageMenuItem;
     private JMenuItem   tamilLanguageMenuItem;
     private JMenuItem   thaiLanguageMenuItem;
 
     /*
      * ACTION COMPONENTS
      */
     private NewDocumentAction            newAct;
     private OpenFileAction               openAct;
     private SaveFileAction               saveAct;
     private SaveFileAsAction             saveAsAct;
     private ExitAction                   exitAct;
     private SelectAllAction              selectAllAct;
     private DeselectAllAction            deselectAllAct;
     private GroupAction                  groupAction;
     private UngroupAction                ungroupAction;
     private DeleteAction                 deleteAction;
     private ZoomInViewAction             zoomInAction;
     private ZoomOutViewAction            zoomOutAction;
     private ToggleCodeViewAction         toggleCodeAct;
     private MalayLanguageToggleAction    malayLanguageToggleAction;
     private EnglishLanguageToggleAction  englishLanguageToggleAction;
     private JapaneseLanguageToggleAction japaneseLanguageToggleAction;
     private ChineseLanguageToggleAction  chineseLanguageToggleAction;
     private TamilLanguageToggleAction    tamilLanguageToggleAction;
     private ThaiLanguageToggleAction     thaiLanguageToggleAction;
    private DocumentPropertiesAction     docPropAct;
 
     /*
      * CONSTRUCTOR
      */
 
     /**
      * Constructs a <code>SVGViewMenubar</code> with parent component
      * <code>view</code> and with components initialised and properly customised.
      *
      * @param view Parent component
      */
     public LSUIMenubar(LSView view) {
         super();
         parent = view;
         initialise();
         customise();
     }
 
     /**
      * Initialisation of GUI components.
      */
     @Override
     public void initialise() {
         fileMenu                   = new JMenu();
         editMenu                   = new JMenu();
         insertMenu                 = new JMenu("Insert");
         windowMenu                 = new JMenu();
         helpMenu                   = new JMenu();
         languageMenu               = new JMenu();
         newMenuItem                = new JMenuItem();
         openMenuItem               = new JMenuItem();
         saveMenuItem               = new JMenuItem();
         saveAsMenuItem             = new JMenuItem();
         docPropMenuItem            = new JMenuItem();
         exitMenuItem               = new JMenuItem();
         selectAllMenuItem          = new JMenuItem();
         deselectAllMenuItem        = new JMenuItem();
         groupMenuItem              = new JMenuItem();
         ungroupMenuItem            = new JMenuItem();
         deleteMenuItem             = new JMenuItem();
         insertRectMenuItem         = new JMenuItem("Rectangle");
         insertCircleMenuItem       = new JMenuItem("Circle");
         insertLineMenuItem         = new JMenuItem("Line");
         zoomInMenuItem             = new JMenuItem();
         zoomOutMenuItem            = new JMenuItem();
         toggleCodeAreaViewMenuItem = new JMenuItem();
         faqMenuItem                = new JMenuItem("FAQ");
         aboutMenuItem              = new JMenuItem("About");
         malayLanguageMenuItem      = new JMenuItem();
         englishLanguageMenuItem    = new JMenuItem();
         japaneseLanguageMenuItem   = new JMenuItem();
         chineseLanguageMenuItem    = new JMenuItem();
         tamilLanguageMenuItem      = new JMenuItem();
         thaiLanguageMenuItem       = new JMenuItem();
         languageGrp                = new ButtonGroup();
 
         /**
          * Initialising actions
          */
         newAct                       = new NewDocumentAction(parent);
         openAct                      = new OpenFileAction(parent);
         saveAct                      = new SaveFileAction(parent);
         saveAsAct                    = new SaveFileAsAction(parent);
         exitAct                      = new ExitAction(parent);
         selectAllAct                 = new SelectAllAction(parent);
         deselectAllAct               = new DeselectAllAction(parent);
         groupAction                  = new GroupAction(parent);
         ungroupAction                = new UngroupAction(parent);
         deleteAction                 = new DeleteAction(parent);
         zoomOutAction                = new ZoomOutViewAction(parent);
         zoomInAction                 = new ZoomInViewAction(parent);
         toggleCodeAct                = new ToggleCodeViewAction(parent);
        docPropAct                   = new DocumentPropertiesAction(parent);
         malayLanguageToggleAction    = new MalayLanguageToggleAction(parent);
         englishLanguageToggleAction  = new EnglishLanguageToggleAction(parent);
         japaneseLanguageToggleAction = new JapaneseLanguageToggleAction(parent);
         chineseLanguageToggleAction  = new ChineseLanguageToggleAction(parent);
         tamilLanguageToggleAction    = new TamilLanguageToggleAction(parent);
         thaiLanguageToggleAction     = new ThaiLanguageToggleAction(parent);
     }
 
     /**
      * Customisation of GUI Components
      */
     @Override
     public void customise() {
         reloadString(LSEditor.titleBundle);
         setMnemonicForMenus();
         layoutFileMenuItemList();
         layoutEditMenuItemList();
 
 //      layoutInsertMenuItemList();
         layoutWindowMenuItemList();
         layoutHelpMenuItemList();
         add(fileMenu);
         add(editMenu);
 
 //      add(insertMenu);
         add(windowMenu);
         add(helpMenu);
         bindHandlers();
     }
 
     /**
      * Setting up mnemonic keys events for respective file menu
      */
     private void setMnemonicForMenus() {
         fileMenu.setMnemonic(KeyEvent.VK_F);
         editMenu.setMnemonic(KeyEvent.VK_E);
         insertMenu.setMnemonic(KeyEvent.VK_I);
         windowMenu.setMnemonic(KeyEvent.VK_W);
         helpMenu.setMnemonic(KeyEvent.VK_H);
     }
 
     /**
      * Layout file menu with menu items
      */
     private void layoutFileMenuItemList() {
         fileMenu.add(newMenuItem);
         fileMenu.add(openMenuItem);
         fileMenu.addSeparator();
         fileMenu.add(saveMenuItem);
         fileMenu.add(saveAsMenuItem);
         fileMenu.addSeparator();
         fileMenu.add(docPropMenuItem);
         fileMenu.addSeparator();
         fileMenu.add(exitMenuItem);
     }
 
     /**
      * Layout file menu with menu items
      */
     private void layoutEditMenuItemList() {
         editMenu.add(selectAllMenuItem);
         editMenu.add(deselectAllMenuItem);
         editMenu.addSeparator();
         editMenu.add(groupMenuItem);
         editMenu.add(ungroupMenuItem);
         editMenu.addSeparator();
         editMenu.add(deleteMenuItem);
     }
 
     /**
      * Layout file menu with menu items
      */
     private void layoutInsertMenuItemList() {
         insertMenu.add(insertRectMenuItem);
         insertMenu.add(insertCircleMenuItem);
         insertMenu.add(insertLineMenuItem);
     }
 
     /**
      * Layout file menu with menu items
      */
     private void layoutHelpMenuItemList() {
 
 //      helpMenu.add(faqMenuItem);
 //      helpMenu.add(aboutMenuItem);
         englishLanguageMenuItem.setSelected(true);
         languageMenu.add(englishLanguageMenuItem);
         languageMenu.add(malayLanguageMenuItem);
         languageMenu.add(japaneseLanguageMenuItem);
         languageMenu.add(chineseLanguageMenuItem);
         languageMenu.add(tamilLanguageMenuItem);
         languageMenu.add(thaiLanguageMenuItem);
         helpMenu.add(languageMenu);
         languageGrp.add(englishLanguageMenuItem);
         languageGrp.add(malayLanguageMenuItem);
         languageGrp.add(japaneseLanguageMenuItem);
         languageGrp.add(chineseLanguageMenuItem);
         languageGrp.add(tamilLanguageMenuItem);
         languageGrp.add(thaiLanguageMenuItem);
 
 //      languageMenu.setEnabled(false);
     }
 
     /**
      * Layout file menu with menu items
      */
     private void layoutWindowMenuItemList() {
         windowMenu.add(zoomInMenuItem);
         windowMenu.add(zoomOutMenuItem);
         windowMenu.add(toggleCodeAreaViewMenuItem);
     }
 
     /**
      * {@inheritDoc}
      */
     @Override
     public void bindHandlers() {
         setActionForMenuItem();
     }
 
     /**
      * Configure action for buttons
      */
     private void setActionForMenuItem() {
 
         // Group 1
         newMenuItem.setAction(newAct);
         openMenuItem.setAction(openAct);
         saveMenuItem.setAction(saveAct);
         saveAsMenuItem.setAction(saveAsAct);
         exitMenuItem.setAction(exitAct);
        docPropMenuItem.setAction(docPropAct);
 
         // Group 2
         selectAllMenuItem.setAction(selectAllAct);
         deselectAllMenuItem.setAction(deselectAllAct);
         groupMenuItem.setAction(groupAction);
         ungroupMenuItem.setAction(ungroupAction);
         deleteMenuItem.setAction(deleteAction);
 
         // Group 3
         zoomInMenuItem.setAction(zoomInAction);
         zoomOutMenuItem.setAction(zoomOutAction);
         zoomInAction.setZoomOutPartnerAction(zoomOutAction);
         toggleCodeAreaViewMenuItem.setAction(toggleCodeAct);
 
         // Group 4
         englishLanguageMenuItem.setAction(englishLanguageToggleAction);
         malayLanguageMenuItem.setAction(malayLanguageToggleAction);
         japaneseLanguageMenuItem.setAction(japaneseLanguageToggleAction);
         chineseLanguageMenuItem.setAction(chineseLanguageToggleAction);
         tamilLanguageMenuItem.setAction(tamilLanguageToggleAction);
         thaiLanguageMenuItem.setAction(thaiLanguageToggleAction);
     }
 
     /**
      * Enable zoom in button after SVG file is opened sucessfully
      */
     public void enableZoomIn() {
         zoomInMenuItem.setEnabled(true);
     }
 
     /**
      * Disable zoom out button when it has resumed to its original size of image
      */
     public void setZoomOut(boolean flag) {
         zoomOutMenuItem.setEnabled(flag);
     }
 
     public void updateActionStatusFromView(boolean status) {
         selectAllMenuItem.setEnabled(!status);
         deselectAllMenuItem.setEnabled(status);
     }
 
     public void toggleCodeMenuTitle(boolean flag) {
         toggleCodeAreaViewMenuItem.setText(!flag
                                            ? LSEditor.titleBundle.getString("action.closecodeview.title")
                                           : LSEditor.titleBundle.getString("action.opencodeview.title"));
     }
 
     public void reloadString(ResourceBundle b) {
         fileMenu.setText(b.getString("menubar.file.title"));
         editMenu.setText(b.getString("menubar.edit.title"));
         windowMenu.setText(b.getString("menubar.window.title"));
         helpMenu.setText(b.getString("menubar.help.title"));
         languageMenu.setText(b.getString("menubar.language.title"));
        docPropAct.setText(b.getString("action.docprop.title"));
         newAct.setText(b.getString("action.new.title"));
         openAct.setText(b.getString("action.open.title"));
         saveAct.setText(b.getString("action.save.title"));
         saveAsAct.setText(b.getString("action.saveas.title"));
         exitAct.setText(b.getString("action.exit.title"));
         selectAllAct.setText(b.getString("action.selall.title"));
         deselectAllAct.setText(b.getString("action.deselall.title"));
         groupAction.setText(b.getString("action.group.title"));
         ungroupAction.setText(b.getString("action.ungroup.title"));
         deleteAction.setText(b.getString("action.delete.title"));
         zoomOutAction.setText(b.getString("action.zoomout.title"));
         zoomInAction.setText(b.getString("action.zoomin.title"));
         toggleCodeAct.setText(b.getString("action.closecodeview.title"));
         malayLanguageToggleAction.setText(b.getString("action.malayLanguage.name"));
         englishLanguageToggleAction.setText(b.getString("action.englishLanguage.name"));
         japaneseLanguageToggleAction.setText(b.getString("action.japaneseLanguage.name"));
         chineseLanguageToggleAction.setText(b.getString("action.chineseLanguage.name"));
         tamilLanguageToggleAction.setText(b.getString("action.tamilLanguage.name"));
         thaiLanguageToggleAction.setText(b.getString("action.thaiLanguage.name"));
     }
 }
