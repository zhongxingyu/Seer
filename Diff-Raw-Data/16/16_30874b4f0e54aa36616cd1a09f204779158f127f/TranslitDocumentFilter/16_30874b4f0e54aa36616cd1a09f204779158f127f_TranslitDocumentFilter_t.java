 package org.romppu.translit.swing.document;
 
 import org.romppu.translit.document.TranslitDocument;
 import org.romppu.translit.dictionary.TranslitDictionary;
 import org.romppu.translit.TranslitDocumentException;
 
 import javax.swing.text.*;
 import java.awt.*;
 import java.beans.PropertyChangeListener;
 import java.beans.PropertyChangeSupport;
 
 /**
  * @author RP
  */
 public class TranslitDocumentFilter extends DocumentFilter {
 
     private PropertyChangeSupport changeSupport = new PropertyChangeSupport(this);
     private TranslitDocument translitDocument;
     private boolean translitMode;
     private Color translitForeground = Color.black;
     private Color textForeground = Color.gray;
 
     public TranslitDocumentFilter() {
     }
 
     public TranslitDocumentFilter(TranslitDocument translitDocument) {
         this.translitDocument = translitDocument;
 
     }
 
     @Override
     public void replace(DocumentFilter.FilterBypass fb, int offset, int length, String text, AttributeSet attrs)
             throws BadLocationException {
         try {
             if (isTranslitMode()) {
                 TranslitDocument.Mutation mutation = translitDocument.insertAt(offset, text, TranslitDictionary.Side.RIGHT);
                 String newString = translitDocument.getString(mutation.newElements(), TranslitDictionary.Side.LEFT);
                 int correction = newString.length() - text.length();
                 int startPos = mutation.getOffset() - correction;
                 int len = correction + (offset - mutation.getOffset());
                 fb.replace(startPos, len, newString, attrs);
                 if (fb.getDocument() instanceof StyledDocument) {
                     resetAttributes(startPos, len + 1, (StyledDocument) fb.getDocument());
                 }
             } else {
                 translitDocument.insertStringAt(offset, text, TranslitDictionary.Side.LEFT);
                 fb.replace(offset, length, text, attrs);
                resetAttributes(offset, length, (StyledDocument) fb.getDocument());
             }
         } catch (TranslitDocumentException e) {
             e.printStackTrace();
             throw new BadLocationException(text, offset);
         }
     }
 
     public void resetAttributes(StyledDocument document) throws TranslitDocumentException {
        resetAttributes(0, translitDocument.getSize(), document);
     }
 
     public void resetAttributes(int offset, int len, StyledDocument document) throws TranslitDocumentException {
         SimpleAttributeSet textAttrs = new SimpleAttributeSet();
         StyleConstants.setForeground(textAttrs, getTextForeground());
         SimpleAttributeSet translitAttrs = new SimpleAttributeSet();
         StyleConstants.setForeground(translitAttrs, getTranslitForeground());
         for (int i = offset; i < offset + len; i++) {
             if (i >= translitDocument.getSize()) break;
             TranslitDocument.Element element = translitDocument.getElement(i);
             document.setCharacterAttributes(i, 1, element.isTransliteration()?translitAttrs:textAttrs, true);
         }
     }
 
     public void remove(FilterBypass fb, int offset, int length) throws
             BadLocationException {
 
         translitDocument.removeElements(offset, length);
         fb.remove(offset, length);
     }
 
     public TranslitDocument getTranslitDocument() {
         return translitDocument;
     }
 
     public void setTranslitDocument(TranslitDocument translitDocument) {
         TranslitDocument old = getTranslitDocument();
         this.translitDocument = translitDocument;
         changeSupport.firePropertyChange("translitDocument", old, translitDocument);
     }
 
     public boolean isTranslitMode() {
         return translitMode;
     }
 
     public void setTranslitMode(boolean translitMode) {
         boolean old = isTranslitMode();
         this.translitMode = translitMode;
         changeSupport.firePropertyChange("translitMode", old, translitMode);
     }
 
     public Color getTranslitForeground() {
         return translitForeground;
     }
 
     public void setTranslitForeground(Color translitForeground) {
         Color old = getTranslitForeground();
         this.translitForeground = translitForeground;
         changeSupport.firePropertyChange("translitForeground", old, translitForeground);
     }
 
     public Color getTextForeground() {
         return textForeground;
     }
 
     public void setTextForeground(Color textForeground) {
         Color old = getTextForeground();
         this.textForeground = textForeground;
         changeSupport.firePropertyChange("textForeground", old, textForeground);
     }
 
     public void addPropertyChangeListener(String prop, PropertyChangeListener listener) {
         changeSupport.addPropertyChangeListener(prop, listener);
     }
 
     public void removePropertyChangeListener(String prop, PropertyChangeListener listener) {
         changeSupport.removePropertyChangeListener(prop, listener);
     }
 
 }
