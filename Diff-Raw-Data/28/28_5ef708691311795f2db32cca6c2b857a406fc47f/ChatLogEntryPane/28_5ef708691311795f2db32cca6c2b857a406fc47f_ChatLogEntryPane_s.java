 /*
  * ChatLogPane.java: GameTable is in the Public Domain.
  */
 
 
 package com.galactanet.gametable;
 
 import java.awt.Component;
 import java.awt.event.ActionEvent;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 
 import javax.swing.AbstractAction;
 import javax.swing.JComponent;
 import javax.swing.JEditorPane;
 import javax.swing.KeyStroke;
 import javax.swing.border.BevelBorder;
 import javax.swing.event.CaretEvent;
 import javax.swing.event.CaretListener;
 import javax.swing.text.*;
 import javax.swing.text.html.HTMLDocument;
 
 
 
 public class ChatLogEntryPane extends JEditorPane
 {
     // --- Types -----------------------------------------------------------------------------------------------------
 
     private class StyleAction extends AbstractAction
     {
         String style;
 
         StyleAction(String styleName)
         {
             style = styleName;
         }
 
         /*
          * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
          */
         public void actionPerformed(ActionEvent e)
         {
             toggleStyle(style);
         }
     }
 
     // --- Members ---------------------------------------------------------------------------------------------------
 
     /**
      * List of sent items.
      */
     private List                history         = new ArrayList();
     private int                 historyPosition = 0;
     private StyledEntryToolbar  toolbar         = null;
     private MutableAttributeSet styleOverride   = null;
     private GametableFrame      frame;
 
     // --- Constructors ----------------------------------------------------------------------------------------------
 
     public ChatLogEntryPane(GametableFrame parentFrame)
     {
         super("text/html", ChatLogPane.DEFAULT_TEXT);
         frame = parentFrame;
         initialize();
         clear();
     }
 
     public void setToolbar(StyledEntryToolbar bar)
     {
         toolbar = bar;
     }
 
     /**
      * @return the component to add to UIs
      */
     public Component getComponentToAdd()
     {
         return this;
     }
 
     /**
      * Clears this text pane.
      */
     public void clear()
     {
         setText("");
     }
 
     /**
      * @return the useful part of the text of this component.
      */
     public String getText()
     {
         return UtilityFunctions.getBodyContent(super.getText());
     }
 
     public String getPlainText()
     {
         HTMLDocument doc = (HTMLDocument)getDocument();
         try
         {
             return doc.getText(doc.getStartPosition().getOffset(), doc.getLength());
         }
         catch (Exception e)
         {
             Log.log(Log.SYS, e);
             return "";
         }
     }
 
     public void setText(String text)
     {
         super.setText(ChatLogPane.DEFAULT_TEXT_HEADER + text + ChatLogPane.DEFAULT_TEXT_FOOTER);
     }
 
     public void toggleStyle(String style)
     {
         HTMLDocument doc = (HTMLDocument)getDocument();
         Caret c = getCaret();
         int start = Math.min(c.getMark(), c.getDot());
         int end = Math.max(c.getMark(), c.getDot());
 
         if (start == end)
         {
             setCurrentStyle(style, !isCurrentStyle(style));
 
             getAttributeString(styleOverride);
             toolbar.updateStyles();
             return;
         }
 
         AttributeSet styleOn = getCleanStyle(doc, "." + style);
         AttributeSet styleOff = getCleanStyle(doc, ".no-" + style);
         applyStyle(doc, start, end, styleOn, styleOff);
 
         toolbar.updateStyles();
     }
 
     public boolean isCurrentStyle(String style)
     {
         AttributeSet current = getCurrentStyle();
         HTMLDocument doc = (HTMLDocument)getDocument();
         AttributeSet styleOn = getCleanStyle(doc, "." + style);
 
         return current.containsAttributes(styleOn);
     }
 
     public void setCurrentStyle(String style, boolean status)
     {
         HTMLDocument doc = (HTMLDocument)getDocument();
         AttributeSet styleOn = getCleanStyle(doc, "." + style);
         AttributeSet styleOff = getCleanStyle(doc, ".no-" + style);
 
         AttributeSet current = getCurrentStyle();
         if (styleOverride == null)
         {
             styleOverride = new SimpleAttributeSet(current);
         }
 
         if (current.containsAttributes(styleOn))
         {
             styleOverride.removeAttributes(styleOn);
             styleOverride.addAttributes(styleOff);
         }
         else
         {
             styleOverride.addAttributes(styleOn);
             styleOverride.removeAttributes(styleOff);
         }
     }
 
     /**
      * Initializes this object.
      */
     private void initialize()
     {
         setEditable(true);
         setFocusable(true);
         setBorder(new BevelBorder(BevelBorder.LOWERED));
 
         getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("pressed UP"), "historyBack");
         getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("pressed DOWN"), "historyForward");
         getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("pressed ENTER"), "enter");
         getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("control pressed B"), "bold");
         getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("control pressed I"), "italics");
         getInputMap(JComponent.WHEN_FOCUSED).put(KeyStroke.getKeyStroke("control pressed U"), "underline");
         addKeyListener(new KeyAdapter()
         {
             /*
              * @see java.awt.event.KeyAdapter#keyTyped(java.awt.event.KeyEvent)
              */
             public void keyTyped(KeyEvent e)
             {
                 if ((e.getModifiersEx() & InputEvent.CTRL_DOWN_MASK) != 0)
                 {
                     return;
                 }
 
                if (e.getKeyChar() == '\n')
                 {
                     return;
                 }
 
                 HTMLDocument doc = (HTMLDocument)getDocument();
                 String charStr = String.valueOf(e.getKeyChar());
                 if (styleOverride != null)
                 {
                     try
                     {
                         int dotPos = getCaret().getDot();
                         doc.insertAfterEnd(doc.getCharacterElement(dotPos), charStr);
                         doc.setCharacterAttributes(dotPos, charStr.length(), styleOverride, false);
                         // Hack to force the carat style to be what we just typed
                         setCaretPosition(dotPos);
                         setCaretPosition(dotPos + charStr.length());
                     }
                     catch (Exception ex)
                     {
                         Log.log(Log.SYS, ex);
                     }
 
                     // Hack to get around weird first-character bug in edit pane
                     if (doc.getLength() > 2)
                     {
                         styleOverride = null;
                     }
                     e.consume();
                 }
             }
         });
 
         addCaretListener(new CaretListener()
         {
             /*
              * @see javax.swing.event.CaretListener#caretUpdate(javax.swing.event.CaretEvent)
              */
             public void caretUpdate(CaretEvent e)
             {
                toolbar.updateStyles();
             }
         });
 
         getActionMap().put("bold", new StyleAction("bold"));
         getActionMap().put("italics", new StyleAction("italics"));
         getActionMap().put("underline", new StyleAction("underline"));
 
         getActionMap().put("historyBack", new AbstractAction()
         {
             /*
              * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
              */
             public void actionPerformed(ActionEvent e)
             {
                 historyPosition--;
                 if (historyPosition < 0)
                 {
                     historyPosition = 0;
                 }
                 else
                 {
                     setText((String)history.get(historyPosition));
                 }
                 toolbar.updateStyles();
             }
         });
 
         getActionMap().put("historyForward", new AbstractAction()
         {
             /*
              * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
              */
             public void actionPerformed(ActionEvent e)
             {
                 historyPosition++;
 
                 if (historyPosition > history.size())
                 {
                     historyPosition = history.size();
                 }
                 else
                 {
                     if (historyPosition == history.size())
                     {
                         clear();
                     }
                     else
                     {
                         setText((String)history.get(historyPosition));
                     }
                 }
                 toolbar.updateStyles();
             }
         });
 
         getActionMap().put("enter", new AbstractAction()
         {
             /*
              * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
              */
             public void actionPerformed(ActionEvent e)
             {
                 // they hit return on the text bar
                 String entered = getText().trim();
                 if (entered.length() == 0)
                 {
                     // useless string.
                     // return focus to the map
                     frame.getGametableCanvas().requestFocus();
                     return;
                 }
 
                 history.add(entered);
                 historyPosition = history.size();
 
               
                 // parse for commands
                 String plain = getPlainText().trim();
                 if (plain.length() > 0 && plain.charAt(0) == '/')
                 {
                     frame.parseSlashCommand(plain);
                 }
                 else
                 {
                     frame.postMessage("<b>" + frame.getMyPlayer().getCharacterName() + "&gt;</b> " + entered);
                 }
 
                 if (styleOverride == null)
                 {
                     styleOverride = new SimpleAttributeSet(getCurrentStyle());
                 }
 
                 clear();
                 toolbar.updateStyles();
             }
         });
     }
 
     private AttributeSet getCurrentStyle()
     {
         if (styleOverride != null)
         {
             return styleOverride;
         }
 
         HTMLDocument doc = (HTMLDocument)getDocument();
         int pos = getCaretPosition();
         if (pos != doc.getStartPosition().getOffset() + 1)
         {
             --pos;
         }
         return doc.getCharacterElement(pos).getAttributes();
     }
 
     private static AttributeSet getCleanStyle(HTMLDocument doc, String name)
     {
         return getCleanStyle(doc.getStyle(name));
     }
 
     private static AttributeSet getCleanStyle(AttributeSet attributes)
     {
         SimpleAttributeSet retVal = new SimpleAttributeSet(attributes);
         retVal.removeAttribute(StyleConstants.NameAttribute);
         return retVal;
     }
 
     private static void applyStyle(HTMLDocument doc, int start, int end, AttributeSet styleOn, AttributeSet styleOff)
     {
         boolean allSet = true;
 
         List elements = getElementsIn(doc, start, end);
         for (int i = 0, size = elements.size(); i < size; ++i)
         {
             AbstractDocument.LeafElement element = (AbstractDocument.LeafElement)elements.get(i);
             if (!element.containsAttributes(styleOn))
             {
                 allSet = false;
                 break;
             }
         }
 
         if (allSet)
         {
             if (styleOff != null)
             {
                 doc.setCharacterAttributes(start, end - start, styleOff, false);
             }
             return;
         }
 
         doc.setCharacterAttributes(start, end - start, styleOn, false);
     }
 
     private static List getElementsIn(HTMLDocument doc, int start, int end)
     {
         List retVal = new ArrayList();
         int pos = start;
         while (true)
         {
             AbstractDocument.LeafElement elem = (AbstractDocument.LeafElement)doc.getCharacterElement(pos);
             retVal.add(elem);
             if (elem.getEndOffset() >= end)
             {
                 break;
             }
             pos = elem.getEndOffset();
         }
 
         return retVal;
     }
 
     private static String getAttributeString(AttributeSet attributes)
     {
         AttributeSet clean = getCleanStyle(attributes);
         StringBuffer buffer = new StringBuffer();
         buffer.append('{');
         for (Enumeration e = clean.getAttributeNames(); e.hasMoreElements();)
         {
             Object key = e.nextElement();
             buffer.append(' ');
             buffer.append(key.toString());
             if (!(key instanceof String))
             {
                 buffer.append(" (");
                 buffer.append(key.getClass().getName());
                 buffer.append(')');
             }
             buffer.append(" => ");
             Object value = clean.getAttribute(key);
             buffer.append(value.toString());
             if (!(value instanceof String))
             {
                 buffer.append(" (");
                 buffer.append(value.getClass().getName());
                 buffer.append(')');
             }
         }
         buffer.append(" }");
 
         return buffer.toString();
     }
 }
