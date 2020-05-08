 package jmonkey.office.jwp.support;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.GraphicsEnvironment;
 import java.awt.Image;
 import java.awt.Toolkit;
 import java.awt.event.ActionEvent;
 import java.io.Serializable;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import javax.swing.AbstractAction;
 import javax.swing.Action;
 import javax.swing.ImageIcon;
 import javax.swing.JColorChooser;
 import javax.swing.JEditorPane;
 import javax.swing.JFrame;
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 import javax.swing.text.AbstractDocument;
 import javax.swing.text.AttributeSet;
 import javax.swing.text.Document;
 import javax.swing.text.Element;
 import javax.swing.text.JTextComponent;
 import javax.swing.text.MutableAttributeSet;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledDocument;
 
 import jmonkey.office.jwp.support.images.Loader;
 
 
 public class EditorActionManager extends ActionManager {
   // Modifier Constants
   public static final int COLOUR_BLACK = Color.black.getRGB();
   public static final int COLOUR_BLUE = Color.blue.getRGB();
   public static final int COLOUR_CYAN = Color.cyan.getRGB();
   public static final int COLOUR_DARKGRAY = Color.darkGray.getRGB();
   public static final int COLOUR_GRAY = Color.gray.getRGB();
   public static final int COLOUR_GREEN = Color.green.getRGB();
   public static final int COLOUR_LIGHTGRAY = Color.lightGray.getRGB();
   public static final int COLOUR_MAGENTA = Color.magenta.getRGB();
   public static final int COLOUR_ORANGE = Color.orange.getRGB();
   public static final int COLOUR_PINK = Color.pink.getRGB();
   public static final int COLOUR_RED = Color.red.getRGB();
   public static final int COLOUR_WHITE = Color.white.getRGB();
   public static final int COLOUR_YELLOW = Color.yellow.getRGB();
   
   // Action Types.
   public static final String B_A_P = "Beep";
   public static final String A_L_A_P = "Align Left";
   public static final String A_R_A_P = "Align Right";
   public static final String A_C_A_P = "Align Center";
   public static final String A_J_A_P = "Align Justified";
   public static final String BD_A_P = "Bold";
   public static final String IT_A_P = "Italic";
   public static final String U_A_P = "Underline";
   public static final String STR_A_P = "Strikethrough";
   public static final String C_A_P = "Cut";
   public static final String CO_A_P = "Copy";
   public static final String P_A_P = "Paste";
   public static final String SEL_A_P = "Select All";
   public static final String SEL_N_A_P = "Select None";
   public static final String UDO_A_AP = "Undo";
   public static final String RDO_A_P = "Redo";
   public static final String C_C_A_P = "Colour Chooser...";
   public static final String F_C_A_P = "Font Chooser...";
   public static final String SEA_A_P = "Find...";
   public static final String REP_A_P = "Find & Replace...";
   public static final String F_N_A_P = "New";
   public static final String F_O_A_P = "Open...";
   public static final String F_OP_A_P = "Open As...";
   public static final String F_R_A_P = "Revert To Saved";
   public static final String F_S_A_P = "Save";
   public static final String F_SA_A_P = "Save As...";
   public static final String F_SC_A_P = "Save Copy...";
 
   private final CaretListener m_attributeTracker = new AttributeTracker();
 
   private static Editor s_editor = null;
 
   private Map m_actions = Collections.synchronizedMap(new HashMap());
   
   public EditorActionManager(JFrame app, FileActionListener agent) {
 
     m_actions.put(A_C_A_P, 
         new AlignmentAction(A_C_A_P, 
             StyleConstants.ALIGN_CENTER));
     m_actions.put(A_J_A_P, 
         new AlignmentAction(A_J_A_P, 
             StyleConstants.ALIGN_JUSTIFIED));
     m_actions.put(A_L_A_P, 
         new AlignmentAction(A_L_A_P, 
             StyleConstants.ALIGN_LEFT));
     m_actions.put(A_R_A_P, 
         new AlignmentAction(A_R_A_P, 
             StyleConstants.ALIGN_RIGHT));
     m_actions.put(B_A_P, new BeepAction());
     m_actions.put(BD_A_P, new BoldAction());
     m_actions.put(C_C_A_P, 
         new ColourChooserAction(C_C_A_P, app));
     m_actions.put(CO_A_P, new CopyAction());
     m_actions.put(C_A_P, new CutAction());
     m_actions.put(F_C_A_P, 
         new FontChooserAction(F_C_A_P, app));
     m_actions.put(IT_A_P, new ItalicAction());
     m_actions.put(F_N_A_P, 
         new NewAction(F_N_A_P, app, agent));
     m_actions.put(F_O_A_P, 
         new OpenAction(F_O_A_P, app, agent));
     m_actions.put(F_OP_A_P, 
         new OpenAsAction(F_OP_A_P, app, agent));
     m_actions.put(P_A_P, new PasteAction());
     m_actions.put(RDO_A_P, new RedoAction());
     m_actions.put(REP_A_P, 
         new FontChooserAction(REP_A_P, app));
     m_actions.put(F_R_A_P, 
         new RevertAction(F_R_A_P, app, agent));
     m_actions.put(F_S_A_P, 
         new SaveAction(F_S_A_P, app, agent));
     m_actions.put(F_SA_A_P, 
         new SaveAsAction(F_SA_A_P, app, agent));
     m_actions.put(F_SC_A_P, 
         new SaveCopyAction(F_SC_A_P, app, agent));
     m_actions.put(SEA_A_P, 
         new FontChooserAction(SEA_A_P, app));
     m_actions.put(SEL_A_P, new SelectAllAction());
     m_actions.put(SEL_N_A_P, new SelectNoneAction());
     m_actions.put(STR_A_P, new StrikeThroughAction());
     m_actions.put(U_A_P, new UnderlineAction());
     m_actions.put(UDO_A_AP, new UndoAction());
   }
 
 
   private final class AttributeTracker implements CaretListener, Serializable {
     protected AttributeTracker() {
       super();
     }
 
     public void caretUpdate(CaretEvent e) {
       Editor ed = getActiveEditor();
       if (ed != null) {
         int dot = e.getDot();
         int mark = e.getMark();
         if (dot == mark) {
 
           JTextComponent c = (JTextComponent) e.getSource();
           StyledDocument doc = (StyledDocument) c.getDocument();
           Element run = doc.getCharacterElement(Math.max(dot - 1, 0));
           ed.setCurrentParagraph(doc.getParagraphElement(dot));
           if (run != ed.getCurrentRun()) {
             ed.setCurrentRun(run);
             createInputAttributes(ed.getCurrentRun(), ed.getInputAttributes());
           }
         }
       }
     }
 
   } 
 
   private final class FontChooserAction extends AbstractAction {
     // BufferedImage
     private JFrame m_parent;
 
     public FontChooserAction(String nm, JFrame component) {
       super(nm);
       m_parent = component;
     }
 
     public void actionPerformed(ActionEvent e) {
       Editor editor = getActiveEditor();
       AttributeSet initial = getCharacterAttributes(editor.getTextComponent());
       FontChooser fc = new FontChooser(m_parent, "Font Chooser", true, initial);
       fc.setVisible(true);
       
       if (fc.getOutcome()) {
         String family = fc.getFontFamily();
         int size = fc.getFontSize();
         boolean isBold = (fc.getFontStyle() & Font.BOLD) != 0;
         boolean isItalic = (fc.getFontStyle() & Font.ITALIC) != 0;
 
         if (family != null) {
           MutableAttributeSet attr = editor.getSimpleAttributeSet();
           StyleConstants.setFontFamily(attr, family);
           StyleConstants.setFontSize(attr, size);
           StyleConstants.setItalic(attr, isItalic);
           StyleConstants.setBold(attr, isBold);
           setCharacterAttributes(editor.getTextComponent(), attr, false);
         }
         else {
           Toolkit.getDefaultToolkit().beep();
         }
       }
     }
   }
 
   private final class FontFamilyAction extends AbstractAction {
     private String m_family;
 
     public FontFamilyAction(String nm, String family) {
       super(nm);
       m_family = family;
     }
 
     public void actionPerformed(ActionEvent e) {
       Editor editor = EditorActionManager.getActiveEditor();
       if (editor != null) {
         String family = m_family;
         if ((e != null) && (e.getSource() == editor)) {
           String s = e.getActionCommand();
           if (s != null) {
             family = s;
             Code.debug("family: " + s);
           }
         }
         if (family != null) {
           MutableAttributeSet attr = editor.getSimpleAttributeSet();
           StyleConstants.setFontFamily(attr, family);
           setCharacterAttributes(editor.getTextComponent(), attr, false);
         }
         else {
           Toolkit.getDefaultToolkit().beep();
         }
       }
     }
   } // End FontFamilyAction class
     // ===================================================
 
   private final class FontSizeAction extends AbstractAction {
     private int m_size;
 
     public FontSizeAction(String nm, int size) {
       super(nm);
       m_size = size;
     }
 
     public void actionPerformed(ActionEvent e) {
       Editor editor = EditorActionManager.getActiveEditor();
       if (editor != null) {
         int size = m_size;
         if ((e != null) && (e.getSource() == editor)) {
           String s = e.getActionCommand();
           try {
             size = Integer.parseInt(s, 10);
           }
           catch (NumberFormatException nfe) {
           }
         }
         if (size != 0) {
           MutableAttributeSet attr = editor.getSimpleAttributeSet();
           StyleConstants.setFontSize(attr, size);
           setCharacterAttributes(editor.getTextComponent(), attr, false); //fixed bug here
         }
         else {
           Toolkit.getDefaultToolkit().beep();
         }
       }
     }
   } // End FontSizeAction
     // ==============================================================
 
   private final class ColourChooserAction extends AbstractAction {
     // BufferedImage
     private JFrame m_parent;
 
     public ColourChooserAction(String nm, JFrame component) {
       super(nm);
       m_parent = component;
     }
 
     public void actionPerformed(ActionEvent e) {
       Editor editor = EditorActionManager.getActiveEditor();
       if (editor != null) {
         Color fg = 
           JColorChooser.showDialog(m_parent, "Choose a colour...", null);
         if ((e != null) && (e.getSource() == editor) && (fg != null)) {
           try {
             fg = Color.decode(e.getActionCommand());
           }
           catch (NumberFormatException nfe) {
           }
         }
         if (fg != null) {
           MutableAttributeSet attr = editor.getSimpleAttributeSet();
           StyleConstants.setForeground(attr, fg);
           setCharacterAttributes(editor.getTextComponent(), attr, false);
         }
         else {
           Toolkit.getDefaultToolkit().beep();
         }
       }
     }
   }
 
   private final class ForegroundAction extends AbstractAction {
     protected Color m_fg = null;
 
     protected String m_name = null;
 
     public ForegroundAction(String nm, Color fg) {
       // super(nm, new
       // ImageIcon(EditorActionManager.instance().create16x16ColourRec(c, fg)));
       super(nm);
       m_name = nm;
       m_fg = fg;
     }
 
     public void actionPerformed(ActionEvent e) {
       Editor editor = EditorActionManager.getActiveEditor();
       if (editor != null) {
         Color fg = m_fg;
         if (e != null && e.getSource() == editor) {
           try {
             fg = Color.decode(e.getActionCommand());
           }
           catch (NumberFormatException nfe) {
           }
         }
 
         if (fg != null) {
           MutableAttributeSet attr = editor.getSimpleAttributeSet();
           StyleConstants.setForeground(attr, fg);
           setCharacterAttributes(editor.getTextComponent(), attr, false);
         }
         else {
           Toolkit.getDefaultToolkit().beep();
         }
       }
     }
   }
 
   private final class AlignmentAction extends AbstractAction {
     private int m_alignment;
 
     public AlignmentAction(String nm, int alignment) {
       super(nm);
       m_alignment = alignment;
       String icon;
       switch (alignment) {
       case StyleConstants.ALIGN_RIGHT:
         icon = "align_right16.gif";
         break;
       case StyleConstants.ALIGN_LEFT:
         icon = "align_left16.gif";
         break;
       case StyleConstants.ALIGN_CENTER:
         icon = "align_center16.gif";
         break;
       case StyleConstants.ALIGN_JUSTIFIED:
         icon = "align_justify16.gif";
         break;
       default:
         return;
       }
       putValue(Action.SMALL_ICON, new ImageIcon(Loader.load(icon)));
     }
 
     public void actionPerformed(ActionEvent e) {
       Editor editor = EditorActionManager.getActiveEditor();
       if (editor != null) {
         int a = this.m_alignment;
         if ((e != null) && (e.getSource() == editor)) {
           String s = e.getActionCommand();
           try {
             a = Integer.parseInt(s, 10);
           }
           catch (NumberFormatException nfe) {
           }
         }
         MutableAttributeSet attr = editor.getSimpleAttributeSet();
         StyleConstants.setAlignment(attr, a);
         setParagraphAttributes(editor.getTextComponent(), attr, false);
       }
     }
   }
 
   private final class BoldAction extends AbstractAction {
     public BoldAction() {
       super(BD_A_P);
       putValue(Action.SMALL_ICON, 
                new ImageIcon(Loader.load("bold_action16.gif")));
     }
 
     public void actionPerformed(ActionEvent e) {
       Editor editor = EditorActionManager.getActiveEditor();
       if (editor != null) {
         MutableAttributeSet attr = editor.getInputAttributes();
         boolean bold = !StyleConstants.isBold(attr);
         StyleConstants.setBold(attr, bold);
         setCharacterAttributes(editor.getTextComponent(), attr, false);
       }
     }
   }
 
   private final class ItalicAction extends AbstractAction {
     public ItalicAction() {
       super(IT_A_P);
       putValue(Action.SMALL_ICON, 
                new ImageIcon(Loader.load("italic_action16.gif")));
     }
 
     public void actionPerformed(ActionEvent e) {
       Editor editor = EditorActionManager.getActiveEditor();
       if (editor != null) {
         MutableAttributeSet attr = editor.getInputAttributes();
         boolean italic = !StyleConstants.isItalic(attr);
         StyleConstants.setItalic(attr, italic);
         setCharacterAttributes(editor.getTextComponent(), attr, false);
       }
     }
   }
 
   private final class UnderlineAction extends AbstractAction {
     public UnderlineAction() {
       super(U_A_P);
       putValue(Action.SMALL_ICON, 
                new ImageIcon(Loader.load("underline_action16.gif")));
     }
 
     public void actionPerformed(ActionEvent e) {
       Editor editor = EditorActionManager.getActiveEditor();
       if (editor != null) {
         MutableAttributeSet attr = editor.getInputAttributes();
         boolean underline = !StyleConstants.isUnderline(attr);
         StyleConstants.setUnderline(attr, underline);
         setCharacterAttributes(editor.getTextComponent(), attr, false);
       }
     }
   }
 
   private final class StrikeThroughAction extends AbstractAction {
     public StrikeThroughAction() {
       super(STR_A_P);
       putValue(Action.SMALL_ICON, 
                new ImageIcon(Loader.load("strikethrough_action16.gif")));
     }
 
     public void actionPerformed(ActionEvent e) {
       Editor editor = EditorActionManager.getActiveEditor();
       if (editor != null) {
         MutableAttributeSet attr = 
           EditorActionManager.getActiveEditor().getInputAttributes();
         boolean bold = !StyleConstants.isStrikeThrough(attr);
         StyleConstants.setStrikeThrough(attr, bold);
         setCharacterAttributes(editor.getTextComponent(), attr, false);
       }
     }
   }
 
   private static final class CutAction extends AbstractAction {
     public CutAction() {
       super(C_A_P);
       putValue(Action.SMALL_ICON, 
                new ImageIcon(Loader.load("cut_action16.gif")));
     }
 
     public void actionPerformed(ActionEvent e) {
       Editor target = EditorActionManager.getActiveEditor();
       if (target != null) {
         target.getTextComponent().cut();
       }
     }
   }
 
   private static final class CopyAction extends AbstractAction {
     public CopyAction() {
       super(CO_A_P);
       putValue(Action.SMALL_ICON, 
                new ImageIcon(Loader.load("copy_action16.gif")));
     }
 
     public void actionPerformed(ActionEvent e) {
       Editor target = EditorActionManager.getActiveEditor();
       if (target != null) {
         target.getTextComponent().copy();
         
       }
     }
   }
 
   private static final class PasteAction extends AbstractAction {
     public PasteAction() {
       super(P_A_P);
       putValue(Action.SMALL_ICON, 
                new ImageIcon(Loader.load("paste_action16.gif")));
     }
 
     public void actionPerformed(ActionEvent e) {
       Editor target = EditorActionManager.getActiveEditor();
       if (target != null) {
         target.getTextComponent().paste();
       }
     }
   }
 
   private static final class BeepAction extends AbstractAction {
     public BeepAction() {
       super(B_A_P);
     }
 
     public void actionPerformed(ActionEvent e) {
       Toolkit.getDefaultToolkit().beep();
     }
   }
 
   private static final class SelectAllAction extends AbstractAction {
     protected SelectAllAction() {
       super(SEL_A_P);
     }
 
     public void actionPerformed(ActionEvent e) {
       Editor target = EditorActionManager.getActiveEditor();
       if (target != null) {
         JEditorPane tc = target.getTextComponent();
         tc.setCaretPosition(0);
         tc.moveCaretPosition(tc.getDocument().getLength());
       }
     }
   }
 
   private static final class SelectNoneAction extends AbstractAction {
     protected SelectNoneAction() {
       super(SEL_N_A_P);
     }
 
     public void actionPerformed(ActionEvent e) {
       Editor target = EditorActionManager.getActiveEditor();
       if (target != null) {
         JEditorPane tc = target.getTextComponent();
         if (tc.getSelectionStart() != tc.getSelectionEnd()) {
           int dot = tc.getSelectionStart();
           tc.setSelectionStart(dot);
           tc.setSelectionEnd(dot);
           tc.setCaretPosition(dot);
         }
 
       }
     }
   }
 
   protected static final class UndoAction extends AbstractAction {
     protected UndoAction() {
       super(UDO_A_AP);
       putValue(Action.SMALL_ICON, 
                new ImageIcon(Loader.load("undo_action16.gif")));
     }
 
     public void actionPerformed(ActionEvent e) {
       Editor target = EditorActionManager.getActiveEditor();
       if (target != null) {
         if (target.getUndoManager().canUndo()) {
           target.getUndoManager().undo();
         }
       }
     }
   }
 
   private static final class RedoAction extends AbstractAction {
     protected RedoAction() {
       super(RDO_A_P);
       putValue(Action.SMALL_ICON, 
                new ImageIcon(Loader.load("redo_action16.gif")));
     }
 
     public void actionPerformed(ActionEvent e) {
       Editor target = EditorActionManager.getActiveEditor();
       if (target != null) {
         if (target.getUndoManager().canRedo()) {
           target.getUndoManager().redo();
         }
       }
     }
   }
 
   private static final class SearchAction extends AbstractAction {
 
     public SearchAction(String nm, JFrame component) {
       super(nm);
     }
 
     public void actionPerformed(ActionEvent e) {
       Code.message("Search Activated...");
     }
   }
 
   private static final class ReplaceAction extends AbstractAction {
 
     public ReplaceAction(String nm, JFrame component) {
       super(nm);
     }
 
     public void actionPerformed(ActionEvent e) {
       Code.message("Replace Activated...");
     }
   }
 
   private static final class NewAction extends AbstractAction {
     private FileActionListener m_listener;
 
     public NewAction(String name, JFrame component, FileActionListener agent) {
       super(name);
       m_listener = agent;
       putValue(Action.SMALL_ICON, 
                new ImageIcon(Loader.load("new_document16.gif")));
     }
 
     public void actionPerformed(ActionEvent e) {
       m_listener.editorNew();
     }
   }
 
   private static final class OpenAction extends AbstractAction {
     private FileActionListener m_listener;
 
     public OpenAction(String name, JFrame component, FileActionListener agent) {
       super(name);
       m_listener = agent;
       putValue(Action.SMALL_ICON, 
                new ImageIcon(Loader.load("open_document16.gif")));
     }
 
     public void actionPerformed(ActionEvent e) {
       m_listener.editorOpen();
     }
   }
 
   private static final class OpenAsAction extends AbstractAction {
     private FileActionListener m_listener;
 
     public OpenAsAction(String name, JFrame component, FileActionListener agent) {
       super(name);
       m_listener = agent;
     }
 
     public void actionPerformed(ActionEvent e) {
       m_listener.editorOpenAs();
     }
   }
 
   private static final class RevertAction extends AbstractAction {
     private FileActionListener m_listener;
 
     public RevertAction(String name, JFrame component, FileActionListener agent) {
       super(name);
       m_listener = agent;
     }
 
     public void actionPerformed(ActionEvent e) {
       if (EditorActionManager.getActiveEditor() != null) {
         m_listener.editorRevert(EditorActionManager.getActiveEditor());
       }
     }
   }
 
   private static final class SaveAction extends AbstractAction {
     private FileActionListener m_listener;
 
     public SaveAction(String name, JFrame component, FileActionListener agent) {
       super(name);
       m_listener = agent;
       this.putValue(Action.SMALL_ICON, 
                     new ImageIcon(Loader.load("save_document16.gif")));
     }
 
     public void actionPerformed(ActionEvent e) {
       if (EditorActionManager.getActiveEditor() != null) {
         m_listener.editorSave(EditorActionManager.getActiveEditor());
       }
     }
   }
 
   private static final class SaveAsAction extends AbstractAction {
     private FileActionListener m_listener = null;
 
     public SaveAsAction(String name, JFrame component, FileActionListener agent) {
       super(name);
       m_listener = agent;
     }
 
     public void actionPerformed(ActionEvent e) {
       if (EditorActionManager.getActiveEditor() != null) {
         m_listener.editorSaveAs(EditorActionManager.getActiveEditor());
       }
     }
   }
 
   private static final class SaveCopyAction extends AbstractAction {
     private FileActionListener m_listener = null;
 
     public SaveCopyAction(String name, JFrame component,
         FileActionListener agent) {
       super(name);
       m_listener = agent;
     }
 
     public void actionPerformed(ActionEvent e) {
       if (EditorActionManager.getActiveEditor() != null) {
         m_listener.editorSaveCopy(EditorActionManager.getActiveEditor());
       }
     }
   }
 
   /**
    * Add an editor to the action manager.
    */
   public void activate(Editor editor) {
     // First deactivate the current editor if there is one.
     if (EditorActionManager.s_editor != null) {
       deactivate(EditorActionManager.s_editor);
     }
 
     EditorActionManager.s_editor = editor;
     editor.hasBeenActivated(editor);
 
     editor.getTextComponent().addCaretListener(
         editor.getEditorActionManager().m_attributeTracker);
   }
 
   private Image create16x16ColourRec(Component c, Color colour) {
     Code.debug("Component=" + c);
     Code.debug("Color=" + colour);
     Image img = c.createImage(16, 16);
     Code.debug("Image=" + img);
     Graphics g = img.getGraphics();
     Code.debug("Graphics=" + g);
     g.setColor(colour);
     g.fillRect(0, 0, 16, 16);
     Code.debug("Coloured Image=" + img);
     return img;
   }
 
   /**
    * Create the default actions of the type: FONT_COLOUR_ACTION
    */
   public final Action[] createDefaultColourActions() {
    Action[] a = new Action[13];
     a[0] = this.getColourAction("White", Color.white);
     a[1] = this.getColourAction("Black", Color.black);
     a[2] = this.getColourAction("Red", Color.red);
     a[3] = this.getColourAction("Green", Color.green);
     a[4] = this.getColourAction("Blue", Color.blue);
     a[5] = this.getColourAction("Orange", Color.orange);
     a[6] = this.getColourAction("Dark Gray", Color.darkGray);
     a[7] = this.getColourAction("Gray", Color.gray);
     a[8] = this.getColourAction("Light Gray", Color.lightGray);
     a[9] = this.getColourAction("Cyan", Color.cyan);
     a[10] = this.getColourAction("Magenta", Color.magenta);
     a[11] = this.getColourAction("Pink", Color.pink);
     a[12] = this.getColourAction("Yellow", Color.yellow);
     return a;
   }
 
   /**
    * Create the default font family actions.
    */
   public final Action[] createDefaultFontFaceActions() {
 
     String[] families = GraphicsEnvironment.getLocalGraphicsEnvironment()
         .getAvailableFontFamilyNames();
     Map fontFamilyRange = Collections.synchronizedMap(new HashMap());
     Action a = null;
     for (int i = 0; i < families.length; i++) {
       if (families[i].indexOf(".") == -1) {
 
         a = this.getFontFaceAction(families[i]);
         fontFamilyRange.put(families[i], a);
       }
     }
 
     Action[] output = new Action[fontFamilyRange.size()];
     for (int i = 0; i < output.length; i++) {
       if (families[i].indexOf(".") == -1) {
         output[i] = (Action) fontFamilyRange.get(families[i]);
       }
     }
     return output;
   }
 
   /**
    * Copies the key/values in <code>element</code>s AttributeSet into
    * <code>set</code>. This does not copy component, icon, or element names
    * attributes. Subclasses may wish to refine what is and what isn't copied
    * here. But be sure to first remove all the attributes that are in
    * <code>set</code>.
    * <p>
    * This is called anytime the caret moves over a different location.
    * 
    */
   protected void createInputAttributes(Element element, MutableAttributeSet set) {
     set.removeAttributes(set);
     set.addAttributes(element.getAttributes());
     set.removeAttribute(StyleConstants.ComponentAttribute);
     set.removeAttribute(StyleConstants.IconAttribute);
     set.removeAttribute(AbstractDocument.ElementNameAttribute);
     set.removeAttribute(StyleConstants.ComposedTextAttribute);
   }
 
   /**
    * Remove an editor to the action manager.
    */
   public void deactivate(Editor editor) {
     if (EditorActionManager.s_editor != null) {
       // disable Caret events.
       Editor currentEditor = EditorActionManager.getActiveEditor();
       currentEditor.getTextComponent().removeCaretListener(m_attributeTracker);
       // Before the editor is removed so that any calls to
       // EditorActionManager.getActiveEditor() will return something.
       currentEditor.hasBeenDeactivated(EditorActionManager.getActiveEditor());
       EditorActionManager.s_editor = null;
     }
   }
 
   /**
    * Sets the enabled attribute of all actions matching or containing the
    * specified pattern. To disable a specific action, use a specific name. if
    * the name is found in the list, it is the only one disabled. Otherwise, all
    * actions that contain the pattern will be enabled/disabled.
    */
   public final void enableAction(String pattern, boolean enabled) {
     Code.debug("enableAction: " + pattern + ", " + enabled);
     if (m_actions.containsKey(pattern)) {
       ((Action) m_actions.get(pattern)).setEnabled(enabled);
     }
   }
 
   /**
    * Sets the enabled attribute of all format actions .
    * 
    * @param enabled
    *          boolean
    */
   public final void enableColourActions(boolean enabled) {
     Code.debug("enableColourActions: " + enabled);
     Iterator it = m_actions.entrySet().iterator();
     while (it.hasNext()) {
       Object o = it.next();
       if ((o instanceof ForegroundAction) || (o instanceof ColourChooserAction)) {
         ((Action) o).setEnabled(enabled);
       }
     }
   }
 
   /**
    * Sets the enabled attribute of all Document actions .
    * 
    * @param enabled
    *          boolean
    */
   public final void enableDocumentActions(boolean enabled) {
     Code.debug("enableDocumentActions: " + enabled);
     enableAction(C_A_P, enabled);
     enableAction(CO_A_P, enabled);
     enableAction(P_A_P, enabled);
     enableAction(SEL_A_P, enabled);
     enableAction(SEL_N_A_P, enabled);
     enableAction(UDO_A_AP, enabled);
     enableAction(RDO_A_P, enabled);
     enableAction(SEA_A_P, enabled);
     enableAction(REP_A_P, enabled);
   }
 
   /**
    * Sets the enabled attribute of all format actions .
    * 
    * @param enabled
    *          boolean
    */
   public final void enableFontActions(boolean enabled) {
     Code.debug("enableFontActions: " + enabled);
     Iterator it = m_actions.entrySet().iterator();
     while (it.hasNext()) {
       Object o = it.next();
       if ((o instanceof FontSizeAction) || (o instanceof FontFamilyAction)
           || (o instanceof FontChooserAction)) {
         ((Action) o).setEnabled(enabled);
       }
     }
   }
 
   /**
    * Sets the enabled attribute of all format actions .
    * 
    * @param enabled
    *          boolean
    */
   public final void enableFormatActions(boolean enabled) {
     Code.debug("enableFormatActions: " + enabled);
     enableAction(A_L_A_P, enabled);
     enableAction(A_R_A_P, enabled);
     enableAction(A_C_A_P, enabled);
     enableAction(A_J_A_P, enabled);
     enableAction(BD_A_P, enabled);
     enableAction(IT_A_P, enabled);
     enableAction(U_A_P, enabled);
     enableAction(STR_A_P, enabled);
     enableAction(C_C_A_P, enabled);
     enableAction(F_C_A_P, enabled);
     enableColourActions(enabled);
     enableFontActions(enabled);
   }
 
   /**
    * Sets the enabled attribute of all Generic actions .
    * 
    * @param enabled
    *          boolean
    */
   public final void enableGenericActions(boolean enabled) {
     Code.debug("enableGenericActions: " + enabled);
     enableAction(B_A_P, enabled);
   }
   
   public final Action getActionByKey(String key) {
     return (Action) m_actions.get(key);
   }
 
   public static final Editor getActiveEditor() {
     return EditorActionManager.s_editor;
   }
 
   public final Action getAlignCenterAction() {
     return (Action) m_actions.get(A_C_A_P);
   }
 
   public final Action getAlignJustifyAction() {
     return (Action) m_actions.get(A_J_A_P);
   }
 
   public final Action getAlignLeftAction() {
     return (Action) m_actions.get(A_L_A_P);
   }
 
   public final Action getAlignRightAction() {
     return (Action) m_actions.get(A_R_A_P);
   }
 
   public final Action getBeepAction() {
     return (Action) m_actions.get(B_A_P);
   }
 
   public final Action getBoldAction() {
     return (Action) m_actions.get(BD_A_P);
   }
 
   public final Action getColourAction(String name, Color colour) {
     if (!m_actions.containsKey(name)) {
       m_actions.put(name, new ForegroundAction(name, colour));
     }
     return (Action) m_actions.get(name);
   }
 
   public final Color getColourAtCaret() {
     Editor editor = EditorActionManager.getActiveEditor();
     if (editor != null) {
       return StyleConstants.getForeground(editor.getInputAttributes());
     }
     else {
       return null;
     }
   }
 
   public final Action getColourChooserAction() {
     return (Action) m_actions.get(C_C_A_P);
   }
 
   public final Action getCopyAction() {
     return (Action) m_actions.get(CO_A_P);
   }
 
   public final Action getCutAction() {
     return (Action) m_actions.get(C_A_P);
   }
 
   // =============== BEGIN ADD ACTION METHODS
   // =========================================
   public final Action getFontChooserAction() {
     return (Action) m_actions.get(F_C_A_P);
   }
 
   public final Action getFontFaceAction(Font font) {
     return this.getFontFaceAction(font.getFontName());
   }
 
   public final Action getFontFaceAction(String name) {
     if (!m_actions.containsKey(name)) {
       m_actions.put(name, new FontFamilyAction(name, name));
     }
     return (Action) m_actions.get(name);
   }
 
   public final Action getFontSizeAction(int size) {
     String key = Integer.toString(size);
     if (!m_actions.containsKey(key)) {
       m_actions.put(key, new FontSizeAction(key, size));
     }
     return (Action) m_actions.get(key);
   }
 
   public final Action getItalicAction() {
     return (Action) m_actions.get(IT_A_P);
   }
 
   // ===== Editor File Actions ============================
 
   public final Action getNewAction() {
     return (Action) m_actions.get(F_N_A_P);
   }
 
   public final Action getOpenAction() {
     return (Action) m_actions.get(F_O_A_P);
   }
 
   public final Action getOpenAsAction() {
     return (Action) m_actions.get(F_OP_A_P);
   }
 
   public final Action getPasteAction() {
     return (Action) m_actions.get(P_A_P);
   }
 
   public final Action getRedoAction() {
     return (Action) m_actions.get(RDO_A_P);
   }
 
   public final Action getReplaceAction() {
     return (Action) m_actions.get(REP_A_P);
   }
 
   public final Action getRevertAction() {
     return (Action) m_actions.get(F_R_A_P);
   }
 
   public final Action getSaveAction() {
     return (Action) m_actions.get(F_S_A_P);
   }
 
   public final Action getSaveAsAction() {
     return (Action) m_actions.get(F_SA_A_P);
   }
 
   public final Action getSaveCopyAction() {
     return (Action) m_actions.get(F_SC_A_P);
   }
 
   // ===== Edit File Actions ============================
   public final Action getSearchAction() {
     return (Action) m_actions.get(SEA_A_P);
   }
 
   public final Action getSelectAllAction() {
     return (Action) m_actions.get(SEL_A_P);
   }
 
   public final Action getSelectNoneAction() {
     return (Action) m_actions.get(SEL_N_A_P);
   }
 
   public final Action getStrikeThroughAction() {
     return (Action) m_actions.get(STR_A_P);
   }
 
   public final Action getUnderlineAction() {
     return (Action) m_actions.get(U_A_P);
   }
 
   public final Action getUndoAction() {
     return (Action) m_actions.get(UDO_A_AP);
   }
 
 
   /**
    * Returns true if there is an active editor in the action manager. false
    * otherwise.
    * @return boolean
    */
   public static final boolean isActiveEditor() {
     return (EditorActionManager.s_editor != null);
   }
 
   /**
    * Applies the given attributes to character content. If there is a selection,
    * the attributes are applied to the selection range. If there is no
    * selection, the attributes are applied to the input attribute set which
    * defines the attributes for any new text that gets inserted.
    * 
    * @param editor the editor
    * @param attr the attributes
    * @param replace
    *          if true, then replace the existing attributes first
    */
   protected final void setCharacterAttributes(JEditorPane editor,
       AttributeSet attr, boolean replace) {
     int p0 = editor.getSelectionStart();
     int p1 = editor.getSelectionEnd();
     Editor active = EditorActionManager.getActiveEditor();
     if (p0 != p1) {
       Document doc = active.getTextComponent().getDocument();
       if (doc instanceof StyledDocument) {
         ((StyledDocument) doc).setCharacterAttributes(p0, p1 - p0, attr, 
                                                       replace);
       }
     }
     else {
       MutableAttributeSet inputAttributes = active.getInputAttributes();
       if (replace) {
         inputAttributes.removeAttributes(inputAttributes);
       }
       inputAttributes.addAttributes(attr);
     }
     editor.requestFocus();
   }
   
   protected final AttributeSet getCharacterAttributes(JEditorPane editor) {
     int p0 = editor.getSelectionStart();
     int p1 = editor.getSelectionEnd();
     if (p0 != p1) {
       StyledDocument doc = (StyledDocument) editor.getDocument();
       return doc.getCharacterElement(p0).getAttributes();
     }
     else {
       return EditorActionManager.getActiveEditor().getInputAttributes();
     }
   }
 
   /**
    * Applies the given attributes to paragraphs.  If
    * there is a selection, the attributes are applied
    * to the paragraphs that intersect the selection.
    * if there is no selection, the attributes are applied
    * to the paragraph at the current caret position.
    */
   protected final void setParagraphAttributes(JEditorPane editor,
       AttributeSet attr, boolean replace) {
       int p = editor.getSelectionStart();
       int q = editor.getSelectionEnd(); //fixed
       
     Editor active = EditorActionManager.getActiveEditor();
     Document doc = active.getTextComponent().getDocument();
     if (doc instanceof StyledDocument) {
       ((StyledDocument) doc).setParagraphAttributes(p, q-p, attr, replace); //2nd arg p -> q-p
     }
     editor.requestFocus();
   }
 
   /**
    * Returns a running thread wrapping the runnable object.
    * @param r java.lang.Runnable
    * @return java.lang.Thread
    */
   public static final Runnable threads(Runnable r) {
     Thread t = new Thread(r);
     t.start();
     return t;
 
   }
 }
