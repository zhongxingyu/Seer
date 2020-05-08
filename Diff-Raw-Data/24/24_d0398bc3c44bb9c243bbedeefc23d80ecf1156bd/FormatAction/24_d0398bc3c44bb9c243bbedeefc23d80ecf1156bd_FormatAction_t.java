 /**
  * Metaphase Editor - WYSIWYG HTML Editor Component
  * Copyright (C) 2010  Rudolf Visagie
  * Full text of license can be found in com/metaphaseeditor/LICENSE.txt
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 3 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
  *
  * The author can be contacted at metaphase.editor@gmail.com.
  */
 
 package dmh.swing.huxley;
 
 import java.awt.event.ActionEvent;
 import java.util.Map;
 import java.util.Observable;
 import java.util.Observer;
 
 import javax.swing.text.Element;
 import javax.swing.text.JTextComponent;
 import javax.swing.text.MutableAttributeSet;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledEditorKit.StyledTextAction;
 import javax.swing.text.html.HTML;
 import javax.swing.text.html.HTMLDocument;
 
 import dmh.swing.html.SwingHtmlUtil;
 
 @Deprecated
 public class FormatAction extends StyledTextAction implements Observer {
     // adapted from metaphase editor.
     
     private static final long serialVersionUID = 1L;
 
     private final HTML.Tag htmlTag;
     private final Map<String, String> attributes;
     private final JTextComponent textComponent;
 
     public FormatAction(JTextComponent textComponent, String actionName, HTML.Tag htmlTag) {
         this(textComponent, actionName, htmlTag, null);
     }
 
     public FormatAction(JTextComponent textComponent, String actionName, Map<String, String> attributes) {
         this(textComponent, actionName, null, attributes);
     }
 
     public FormatAction(JTextComponent editorPanel, String actionName, HTML.Tag htmlTag, Map<String, String> attributes) {
             super(actionName);
             this.textComponent = editorPanel;
             this.htmlTag = htmlTag;
             this.attributes = attributes;
     }
 
 
     @Override
     public void actionPerformed(ActionEvent ae) {
         HTMLDocument htmlDocument = (HTMLDocument) textComponent.getDocument();
 
         int start = textComponent.getSelectionStart();
         int end = textComponent.getSelectionEnd();
 
         Element element = htmlDocument.getParagraphElement(start);
         MutableAttributeSet newAttrs = new SimpleAttributeSet(element.getAttributes());
         if (htmlTag != null) {
             newAttrs.addAttribute(StyleConstants.NameAttribute, htmlTag);
         }
         if (attributes != null) {
             for (Map.Entry<String, String> entry: attributes.entrySet()) {
                 newAttrs.addAttribute(entry.getKey(), entry.getValue());
             }
         }
 
         htmlDocument.setParagraphAttributes(start, end - start, newAttrs, true);
         SwingHtmlUtil.refreshJTextComponent(textComponent);
     }
 
     @Override
    public void update(Observable o, Object arg) { /* Do nothing by default. */ }
 }
