 /*
  * Copyright (c) 2006-2012 DMDirc Developers
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
 
 package com.dmdirc.addons.ui_swing.components;
 
 import com.dmdirc.Channel;
 import com.dmdirc.Topic;
 import com.dmdirc.addons.ui_swing.SwingController;
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.addons.ui_swing.actions.ReplacePasteAction;
 import com.dmdirc.addons.ui_swing.components.frames.ChannelFrame;
 import com.dmdirc.addons.ui_swing.components.inputfields.SwingInputHandler;
 import com.dmdirc.addons.ui_swing.components.inputfields.TextPaneInputField;
 import com.dmdirc.addons.ui_swing.components.text.WrapEditorKit;
 import com.dmdirc.interfaces.ConfigChangeListener;
 import com.dmdirc.interfaces.TopicChangeListener;
 import com.dmdirc.parser.common.ChannelJoinRequest;
 import com.dmdirc.ui.messages.Styliser;
 
 import java.awt.Color;
 import java.awt.Window;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 
 import javax.swing.AbstractAction;
 import javax.swing.JButton;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JScrollPane;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import javax.swing.text.DefaultStyledDocument;
 import javax.swing.text.SimpleAttributeSet;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledDocument;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Component to show and edit topics for a channel.
  */
 public class TopicBar extends JComponent implements ActionListener,
         ConfigChangeListener, HyperlinkListener, MouseListener,
         DocumentListener, TopicChangeListener {
 
     /** Serial version UID. */
     private static final long serialVersionUID = 1;
     /** Topic text. */
     private final TextPaneInputField topicText;
     /** Edit button. */
     private final JButton topicEdit;
     /** Cancel button. */
     private final JButton topicCancel;
     /** Associated channel. */
     private final Channel channel;
     /** Controller. */
     private final SwingController controller;
     /** the maximum length allowed for a topic. */
     private final int topicLengthMax;
     /** Empty Attribute set. */
     private SimpleAttributeSet as;
     /** Foreground Colour. */
     private Color foregroundColour;
     /** Background Colour. */
     private Color backgroundColour;
     /** Error icon. */
     private final JLabel errorIcon;
 
     /**
      * Instantiates a new topic bar.
      *
      * @param parentWindow Parent window
      * @param channelFrame Parent channel frame
      */
     public TopicBar(final Window parentWindow,
             final ChannelFrame channelFrame) {
         super();
 
         channel = (Channel) channelFrame.getContainer();
         controller = channelFrame.getController();
         topicText = new TextPaneInputField(channelFrame.getController(),
                 parentWindow);
         topicLengthMax = channel.getMaxTopicLength();
         errorIcon = new JLabel(channelFrame.getIconManager()
                 .getIcon("input-error"));
         topicText.setEditorKit(new WrapEditorKit(channel.getConfigManager()
                 .getOptionBool(controller.getDomain(), "showfulltopic")));
         ((DefaultStyledDocument) topicText.getDocument()).setDocumentFilter(
                 new NewlinesDocumentFilter());
 
         topicText.getActionMap().put("paste-from-clipboard",
                 new ReplacePasteAction("(\r\n|\n|\r)", " "));
         topicEdit = new ImageButton<Object>("edit",
                 channelFrame.getIconManager().getIcon("edit-inactive"),
                 channelFrame.getContainer().getIconManager().getIcon("edit"));
         topicCancel = new ImageButton<Object>("cancel",
                 channelFrame.getIconManager().getIcon("close"),
                 channelFrame.getIconManager().getIcon("close-active"));
 
         final SwingInputHandler handler = new SwingInputHandler(topicText,
                 channelFrame.getContainer().getCommandParser(),
                 channelFrame.getContainer());
         handler.setTypes(true, false, true, false);
         handler.setTabCompleter(channel.getTabCompleter());
 
         final JScrollPane sp = new JScrollPane(topicText);
         sp.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);
         sp.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_NEVER);
 
         setLayout(new MigLayout("fillx, ins 0, hidemode 3"));
         add(sp, "growx, pushx");
         add(errorIcon, "");
         add(topicCancel, "");
         add(topicEdit, "");
         //Fix broken layout manager
         invalidate();
         validate();
         invalidate();
 
         channel.addTopicChangeListener(this);
         topicText.addActionListener(this);
         topicEdit.addActionListener(this);
         topicCancel.addActionListener(this);
         topicText.getInputMap().put(KeyStroke.getKeyStroke("ENTER"),
                 "enterButton");
         topicText.getActionMap().put("enterButton", new AbstractAction(
                 "enterButton") {
 
             /**
              * A version number for this class. It should be changed whenever the class
              * structure is changed (or anything else that would prevent serialized
              * objects being unserialized with the new class).
              */
             private static final long serialVersionUID = 1;
 
             /** {@inheritDoc} */
             @Override
             public void actionPerformed(final ActionEvent e) {
                 commitTopicEdit();
             }
         });
         topicText.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"),
                 "escapeButton");
         topicText.getActionMap().put("escapeButton", new AbstractAction(
                 "escapeButton") {
 
             private static final long serialVersionUID = 1;
 
             /** {@inheritDoc} */
             @Override
             public void actionPerformed(final ActionEvent e) {
                 cancelTopicEdit();
             }
         });
         topicText.addHyperlinkListener(this);
         topicText.addMouseListener(this);
         topicText.getDocument().addDocumentListener(this);
         controller.getGlobalConfig().addChangeListener(
                 "ui", "backgroundcolour", this);
         controller.getGlobalConfig().addChangeListener(
                 "ui", "foregroundcolour", this);
         controller.getGlobalConfig().addChangeListener(
                 "ui", "inputbackgroundcolour", this);
         controller.getGlobalConfig().addChangeListener(
                 "ui", "inputforegroundcolour", this);
         controller.getGlobalConfig().addChangeListener(
                 controller.getDomain(), "showfulltopic", this);
         controller.getGlobalConfig().addChangeListener(
                 controller.getDomain(), "hideEmptyTopicBar", this);
 
         topicText.setFocusable(false);
         topicText.setEditable(false);
         topicCancel.setVisible(false);
         setColours();
         topicChanged(channel, channel.getCurrentTopic());
     }
 
     /** {@inheritDoc} */
     @Override
     public final void topicChanged(final Channel channel, final Topic topic) {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 if (topicText.isEditable()) {
                     return;
                 }
                 topicText.setText("");
                 if (channel.getCurrentTopic() != null) {
                     channel.getStyliser().addStyledString(
                             (StyledDocument) topicText.getDocument(),
                             new String[]{Styliser.CODE_HEXCOLOUR
                                     + UIUtilities.getHex(foregroundColour)
                                     + channel.getCurrentTopic().getTopic(), },
                             as);
                 }
                 if (channel.getConfigManager().getOptionBool(controller.
                         getDomain(),
                         "hideEmptyTopicBar")) {
                     setVisible(topicText.getDocument().getLength() != 0);
                 }
                 topicText.setCaretPosition(0);
                 validateTopic();
                 setVisible(false);
                 setVisible(true);
             }
         });
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Action event
      */
     @Override
     public void actionPerformed(final ActionEvent e) {
         if (!channel.isOnChannel()) {
             return;
         }
         if (e.getSource() == topicEdit) {
             if (topicText.isEditable()) {
                 commitTopicEdit();
             } else {
                 setupTopicEdit();
             }
         } else if (e.getSource() == topicCancel) {
             cancelTopicEdit();
         }
     }
 
     /**
      * Commits a topic edit to the parent channel.
      */
     private void commitTopicEdit() {
         if ((channel.getCurrentTopic() == null
                 && !topicText.getText().isEmpty())
                 || (channel.getCurrentTopic() != null
                 && !channel.getCurrentTopic().getTopic()
                 .equals(topicText.getText()))) {
             channel.setTopic(topicText.getText());
         }
         ((ChannelFrame) controller.getWindowFactory().getSwingWindow(channel))
                 .getInputField().requestFocusInWindow();
        topicChanged(channel, null);
         topicText.setFocusable(false);
         topicText.setEditable(false);
         topicCancel.setVisible(false);
     }
 
     /**
      * Sets the topic ready to be edited, changing attributes and focus.
      */
     private void setupTopicEdit() {
         topicText.setVisible(false);
         topicText.setText("");
         if (channel.getCurrentTopic() != null) {
             topicText.setText(channel.getCurrentTopic().getTopic());
         }
         applyAttributes();
         topicText.setCaretPosition(0);
         topicText.setFocusable(true);
         topicText.setEditable(true);
         topicText.setVisible(true);
         topicText.requestFocusInWindow();
         topicCancel.setVisible(true);
     }
 
     /**
      * Cancels a topic edit, resetting focus and button states.
      */
     private void cancelTopicEdit() {
         topicText.setFocusable(false);
         topicText.setEditable(false);
         topicCancel.setVisible(false);
         ((ChannelFrame) controller.getWindowFactory().getSwingWindow(channel))
                 .getInputField().requestFocusInWindow();
         topicChanged(channel, null);
     }
 
     /** {@inheritDoc} */
     @Override
     public void hyperlinkUpdate(final HyperlinkEvent e) {
         if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
             final String url = e.getDescription();
             if (url == null) {
                 return;
             }
             if (url.charAt(0) == '#') {
                 channel.getServer().join(new ChannelJoinRequest(url));
             } else if (url.contains("://")) {
                 controller.getURLHandler().launchApp(e.getDescription());
             } else {
                 controller.requestWindowFocus(controller.getWindowFactory()
                         .getSwingWindow(channel.getServer().getQuery(url)));
             }
         }
     }
 
     /**
      * Load and set colours.
      */
     private void setColours() {
         backgroundColour = UIUtilities.convertColour(
                 channel.getConfigManager().getOptionColour(
                 "ui", "inputbackgroundcolour", "ui", "backgroundcolour"));
         foregroundColour = UIUtilities.convertColour(
                 channel.getConfigManager().getOptionColour(
                 "ui", "inputforegroundcolour", "ui", "foregroundcolour"));
         setBackground(backgroundColour);
         setForeground(foregroundColour);
         setDisabledTextColour(foregroundColour);
         setCaretColor(foregroundColour);
         setAttributes();
     }
 
     /**
      * Sets sensible attributes.
      */
     private void setAttributes() {
         as = new SimpleAttributeSet();
         StyleConstants.setFontFamily(as, topicText.getFont().getFamily());
         StyleConstants.setFontSize(as, topicText.getFont().getSize());
         StyleConstants.setBackground(as, backgroundColour);
         StyleConstants.setForeground(as, foregroundColour);
         StyleConstants.setUnderline(as, false);
         StyleConstants.setBold(as, false);
         StyleConstants.setItalic(as, false);
     }
 
     /**
      * Applies predefined attributes to the topic bar.
      */
     private void applyAttributes() {
         setAttributes();
         ((DefaultStyledDocument) topicText.getDocument())
                 .setCharacterAttributes(0, Integer.MAX_VALUE, as, true);
     }
 
     /**
      * Sets the caret position in this topic bar.
      *
      * @param position New position
      */
     public void setCaretPosition(final int position) {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 topicText.setCaretPosition(position);
             }
         });
     }
 
     /**
      * Sets the caret colour to the specified coloour.
      *
      * @param optionColour Colour for the caret
      */
     public void setCaretColor(final Color optionColour) {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 topicText.setCaretColor(optionColour);
             }
         });
     }
 
     /**
      * Sets the foreground colour to the specified coloour.
      *
      * @param optionColour Colour for the foreground
      */
     @Override
     public void setForeground(final Color optionColour) {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 topicText.setForeground(optionColour);
             }
         });
     }
 
     /**
      * Sets the disabled text colour to the specified coloour.
      *
      * @param optionColour Colour for the disabled text
      */
     public void setDisabledTextColour(final Color optionColour) {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 topicText.setDisabledTextColor(optionColour);
             }
         });
     }
 
     /**
      * Sets the background colour to the specified coloour.
      *
      * @param optionColour Colour for the caret
      */
     @Override
     public void setBackground(final Color optionColour) {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 topicText.setBackground(optionColour);
             }
         });
     }
 
     /** {@inheritDoc} */
     @Override
     public void configChanged(final String domain, final String key) {
         if ("showfulltopic".equals(key)) {
             topicText.setEditorKit(new WrapEditorKit(channel.getConfigManager()
                 .getOptionBool(controller.getDomain(), "showfulltopic")));
             ((DefaultStyledDocument) topicText.getDocument()).setDocumentFilter(
                     new NewlinesDocumentFilter());
             topicChanged(channel, null);
         }
         setColours();
         if ("hideEmptyTopicBar".equals(key)) {
             setVisible(true);
             if (channel.getConfigManager().getOptionBool(controller.getDomain(),
                     "hideEmptyTopicBar")) {
                 setVisible(topicText.getDocument().getLength() != 0);
             }
         }
     }
 
     /**
      * Closes this topic bar.
      */
     public void close() {
         channel.removeTopicChangeListener(this);
     }
 
     /**
      * Validates the topic text and shows errors as appropriate.
      */
     public void validateTopic() {
         UIUtilities.invokeLater(new Runnable() {
 
             /** {@inheritDoc} */
             @Override
             public void run() {
                 if (topicText.isEditable()) {
                     final int charsLeft = topicLengthMax - topicText.getText().
                             length();
                     if (charsLeft < 0) {
                         errorIcon.setVisible(true);
                         errorIcon.setToolTipText("Topic too long: " + topicText.
                                 getText().length() + " of " + topicLengthMax);
                     } else {
                         errorIcon.setVisible(false);
                         errorIcon.setToolTipText(null);
                     }
                 } else {
                     errorIcon.setVisible(false);
                 }
             }
         });
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mouseClicked(final MouseEvent e) {
         if (e.getClickCount() == 2 && !topicText.isEditable()) {
             topicEdit.doClick();
         }
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mousePressed(final MouseEvent e) {
         //Ignore
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mouseReleased(final MouseEvent e) {
         //Ignore
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mouseEntered(final MouseEvent e) {
         //Ignore
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Mouse event
      */
     @Override
     public void mouseExited(final MouseEvent e) {
         //Ignore
     }
 
     /** {@inheritDoc} */
     @Override
     public void insertUpdate(final DocumentEvent e) {
         validateTopic();
         if (topicText.isEditable()) {
             SwingUtilities.invokeLater(new Runnable() {
 
                 @Override
                 public void run() {
                     applyAttributes();
                 }
             });
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void removeUpdate(final DocumentEvent e) {
         validateTopic();
     }
 
     /** {@inheritDoc} */
     @Override
     public void changedUpdate(final DocumentEvent e) {
         validateTopic();
     }
 }
