 /*
  * 
  * Copyright (c) 2006-2010 Chris Smith, Shane Mc Cormack, Gregory Holmes
  * 
  * Permission is hereby granted, free of charge, to any person obtaining a copy
  * of this software and associated documentation files (the "Software"), to deal
  * in the Software without restriction, including without limitation the rights
  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the Software is
  * furnished to do so, subject to the following conditions:
  * 
  * The above copyright notice and this permission notice shall be included in
  * all copies or substantial portions of the Software.
  * 
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
  * SOFTWARE.
  */
 
 package com.dmdirc.addons.ui_swing.dialogs.channelsetting;
 
 import com.dmdirc.Channel;
 import com.dmdirc.Topic;
 import com.dmdirc.addons.ui_swing.components.text.OldTextLabel;
 
 import java.awt.Color;
 import java.util.Date;
 
 import javax.swing.JEditorPane;
 import javax.swing.JPanel;
 import javax.swing.JSeparator;
 import javax.swing.UIManager;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledDocument;
 import javax.swing.text.StyledEditorKit;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Topic Label for use in the topic history panel.
  */
 public class TopicLabel extends JPanel {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 1;
     /** Topic this label represents. */
     private final Topic topic;
     /** The channel to which this label belongs. */
     private final Channel channel;
     /** Topic field. */
     private JEditorPane pane;
     /** Empty Attrib set. */
     private SimpleAttributeSet as;
 
     /**
      * Instantiates a new topic label based on the specified topic.
      *
      * @param channel The channel to which this label belongs
      * @param topic Specified topic
      * @since 0.6.3
      */
     public TopicLabel(final Channel channel, final Topic topic) {
         if (topic == null) {
             throw new IllegalArgumentException();
         }
         
         this.topic = topic;
         this.channel = channel;
 
         super.setBackground(UIManager.getColor("Table.background"));
         super.setForeground(UIManager.getColor("Table.foreground"));
 
         init();
     }
 
     private void initTopicField() {
         pane = new JEditorPane();
         pane.setEditorKit(new StyledEditorKit());
 
         pane.setFocusable(false);
         pane.setEditable(false);
         pane.setOpaque(false);
 
         as = new SimpleAttributeSet();
         StyleConstants.setFontFamily(as, pane.getFont().getFamily());
         StyleConstants.setFontSize(as, pane.getFont().getSize());
         if (getBackground() == null) {
             StyleConstants.setBackground(as, UIManager.getColor(
                     "Table.background"));
         } else {
             StyleConstants.setBackground(as, getBackground());
         }
         if (getForeground() == null) {
             StyleConstants.setForeground(as, UIManager.getColor(
                     "Table.foreground"));
         } else {
             StyleConstants.setForeground(as, getForeground());
         }
         StyleConstants.setUnderline(as, false);
         StyleConstants.setBold(as, false);
         StyleConstants.setItalic(as, false);
     }
 
     private void init() {
         initTopicField();
         removeAll();
         setLayout(new MigLayout("fill, ins 0, debug", "[]0[]", "[]0[]"));
 
         if (!topic.getTopic().isEmpty()) {
             channel.getStyliser().addStyledString((StyledDocument) pane.getDocument(),
                     new String[]{topic.getTopic(),},
                     as);
             add(pane, "wmax 450, grow, push, wrap, gapleft 5, gapleft 5");
         }
 
         OldTextLabel label;
         if (topic.getTopic().isEmpty()) {
             label = new OldTextLabel("Topic unset by " + topic.getClient());
         } else {
             label = new OldTextLabel("Topic set by " + topic.getClient());
         }
         add(label, "wmax 450, grow, push, wrap, gapleft 5, pad 0");
 
         label = new OldTextLabel("on " + new Date(topic.getTime() * 1000).
                 toString());
         add(label, "wmax 450, grow, push, wrap, gapleft 5, pad 0");
 
         add(new JSeparator(), "newline, span, growx, pushx");
 
         super.validate();
     }
 
     /**
      * Returns the topic for this label.
      *
      * @return Topic
      */
     public Topic getTopic() {
         return topic;
     }
 
     /** {@inheritDoc} */
     @Override
     public void setBackground(final Color bg) {
         super.setBackground(bg);
         if (topic != null && ((bg == null && getBackground() != null) ||
                 !bg.equals(getBackground()))) {
             init();
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void setForeground(final Color fg) {
         super.setForeground(fg);
         if (topic != null && ((fg == null && getForeground() != null) ||
                 !fg.equals(getForeground()))) {
             init();
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void repaint() {
     }
 
     /** {@inheritDoc} */
     @Override
     public void firePropertyChange(String propertyName, Object oldValue,
             Object newValue) {
     }
 
     /** {@inheritDoc} */
     @Override
     public void firePropertyChange(String propertyName, boolean oldValue,
             boolean newValue) {
     }
 
     /** {@inheritDoc} */
     @Override
     public void firePropertyChange(String propertyName, int oldValue,
             int newValue) {
     }
 }
