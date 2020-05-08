 /**
  * Junk - AbstractHtmlStyleAction.java
  * Copyright 2011 Dave Huffman (dave dot huffman at me dot com).
  * TODO license info.
  */
 package dmh.swing.huxley;
 
 import java.util.Map;
 
 import javax.swing.Icon;
 import javax.swing.text.Element;
 import javax.swing.text.JTextComponent;
 import javax.swing.text.MutableAttributeSet;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.html.HTML;
 import javax.swing.text.html.HTMLDocument;
 
 import dmh.swing.AbstractActionObserver;
 import dmh.swing.html.SwingHtmlUtil;
 
 /**
  * TODO Document.
  *
  * @author davehuffman
  */
 public abstract class AbstractHtmlStyleAction extends AbstractActionObserver {
     private static final long serialVersionUID = 1L;
     
     @Deprecated
     public static final String KEY_SELECTION = "huxley-selection";
 
     public AbstractHtmlStyleAction() {}
 
     public AbstractHtmlStyleAction(String name, Icon icon) {
         super(name, icon);
     }
 
     public AbstractHtmlStyleAction(String name) {
         super(name);
     }
 
     protected void performStyleChange(JTextComponent textComponent, HTML.Tag tag) {
         performStyleChange(textComponent, tag, null);
     }
     
     protected void performStyleChange(final JTextComponent textComponent, 
                 HTML.Tag tag, Map<String, String> attributes) {  
         final HTMLDocument htmlDocument = (HTMLDocument) textComponent.getDocument();
 
         final int start = textComponent.getSelectionStart();
         final int end = textComponent.getSelectionEnd();
 
         Element element = htmlDocument.getParagraphElement(start);
         final MutableAttributeSet newAttrs = new SimpleAttributeSet(element.getAttributes());
         if (tag != null) {
             newAttrs.addAttribute(StyleConstants.NameAttribute, tag);
         } else {
             newAttrs.removeAttribute(StyleConstants.NameAttribute);
         }
         if (attributes != null) {
             for (Map.Entry<String, String> entry: attributes.entrySet()) {
                 newAttrs.addAttribute(entry.getKey(), entry.getValue());
             }
         }
 
 //        SwingUtilities.invokeLater(new Runnable() {
 //            @Override
 //            public void run() {
                 htmlDocument.setParagraphAttributes(start, end - start, newAttrs, true);
                 SwingHtmlUtil.refreshJTextComponent(textComponent);
 //            }
 //        });
     }
 
 }
