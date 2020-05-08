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
 
 package com.dmdirc.addons.ui_swing.components;
 
 import com.dmdirc.Channel;
 import com.dmdirc.Topic;
 import com.dmdirc.addons.ui_swing.SwingController;
 import com.dmdirc.addons.ui_swing.UIUtilities;
 import com.dmdirc.addons.ui_swing.actions.NoNewlinesPasteAction;
 import com.dmdirc.addons.ui_swing.components.frames.ChannelFrame;
 import com.dmdirc.addons.ui_swing.components.text.WrapEditorKit;
 import com.dmdirc.config.IdentityManager;
 import com.dmdirc.interfaces.ConfigChangeListener;
 import com.dmdirc.interfaces.TopicChangeListener;
 import com.dmdirc.ui.IconManager;
 import com.dmdirc.ui.messages.ColourManager;
 import com.dmdirc.ui.messages.Styliser;
 import com.dmdirc.ui.core.util.URLHandler;
 
 import java.awt.Color;
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
 import javax.swing.text.StyledEditorKit;
 
 import net.miginfocom.swing.MigLayout;
 
 /**
  * Component to show and edit topics for a channel.
  */
 public class TopicBar extends JComponent implements ActionListener,
         ConfigChangeListener, HyperlinkListener, MouseListener,
         DocumentListener, TopicChangeListener {
 
     /**
      * A version number for this class. It should be changed whenever the class
      * structure is changed (or anything else that would prevent serialized
      * objects being unserialized with the new class).
      */
     private static final long serialVersionUID = 1;
     /** Topic text. */
     private final TextPaneInputField topicText;
     /** Edit button. */
     private final JButton topicEdit;
     /** Cancel button. */
     private final JButton topicCancel;
     /** Associated channel. */
     private Channel channel;
     /** Controller. */
     private SwingController controller;
     /** Empty Attrib set. */
     private SimpleAttributeSet as;
     /** Foreground Colour. */
     private Color foregroundColour;
     /** Background Colour. */
     private Color backgroundColour;
     /** the maximum length allowed for a topic. */
     private int topicLengthMax;
     /** Error icon. */
     private final JLabel errorIcon;
 
     /**
      * Instantiates a new topic bar.
      *
      * @param channelFrame Parent channel frame
      */
     public TopicBar(final ChannelFrame channelFrame) {
         this.channel = channelFrame.getChannel();
         controller = channelFrame.getController();
         topicText = new TextPaneInputField();
         topicLengthMax = channel.getMaxTopicLength();
         errorIcon =
                 new JLabel(IconManager.getIconManager().getIcon("input-error"));
         if (channelFrame.getConfigManager().getOptionBool(controller.
                 getDomain(), "showfulltopic")) {
             topicText.setEditorKit(new StyledEditorKit());
         } else {
             topicText.setEditorKit(new WrapEditorKit());
         }
         ((DefaultStyledDocument) topicText.getDocument()).setDocumentFilter(
                 new NewlinesDocumentFilter());
 
         topicText.getActionMap().put("paste-from-clipboard",
                 new NoNewlinesPasteAction());
         topicEdit = new ImageButton("edit", IconManager.getIconManager().
                 getIcon("edit-inactive"), IconManager.getIconManager().
                 getIcon("edit"));
         topicCancel = new ImageButton("cancel", IconManager.getIconManager().
                 getIcon("close"), IconManager.getIconManager().
                 getIcon("close-active"));
 
         final SwingInputHandler handler = new SwingInputHandler(topicText,
                 channelFrame.getCommandParser(), channelFrame);
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
 
         channel.addTopicChangeListener(this);
         topicText.addActionListener(this);
         topicEdit.addActionListener(this);
         topicCancel.addActionListener(this);
         topicText.getInputMap().put(KeyStroke.getKeyStroke("ENTER"),
                 "enterButton");
         topicText.getActionMap().put("enterButton", new AbstractAction(
                 "enterButton") {
 
             private static final long serialVersionUID = 1;
 
             /** {@inheritDoc} */
             @Override
             public void actionPerformed(ActionEvent e) {
                 TopicBar.this.actionPerformed(e);
             }
         });
         topicText.getInputMap().put(KeyStroke.getKeyStroke("ESCAPE"),
                 "escapeButton");
         topicText.getActionMap().put("escapeButton", new AbstractAction(
                 "escapeButton") {
 
             private static final long serialVersionUID = 1;
 
             /** {@inheritDoc} */
             @Override
             public void actionPerformed(ActionEvent e) {
                 e.setSource(topicCancel);
                 TopicBar.this.actionPerformed(e);
             }
         });
         topicText.addHyperlinkListener(this);
         topicText.addMouseListener(this);
         topicText.getDocument().addDocumentListener(this);
         IdentityManager.getGlobalConfig().addChangeListener(
                 "ui", "backgroundcolour", this);
         IdentityManager.getGlobalConfig().addChangeListener(
                 "ui", "foregroundcolour", this);
         IdentityManager.getGlobalConfig().addChangeListener(
                 "ui", "inputbackgroundcolour", this);
         IdentityManager.getGlobalConfig().addChangeListener(
                 "ui", "inputforegroundcolour", this);
         IdentityManager.getGlobalConfig().addChangeListener(
                 controller.getDomain(), "showfulltopic", this);
         IdentityManager.getGlobalConfig().addChangeListener(
                 controller.getDomain(), "hideEmptyTopicBar", this);
 
         topicText.setFocusable(false);
         topicText.setEditable(false);
         topicCancel.setVisible(false);
         setColours();
         validateTopic();
     }
 
     /** {@inheritDoc} */
     @Override
     public void topicChanged(final Channel channel, final Topic topic) {
         if (topicText.isEditable()) {
             return;
         }
         topicText.setText("");
         if (channel.getCurrentTopic() != null) {
             channel.getStyliser().addStyledString((StyledDocument) topicText.getDocument(),
                     new String[]{Styliser.CODE_HEXCOLOUR + ColourManager.getHex(
                         foregroundColour) + channel.getCurrentTopic().getTopic(),},
                     as);
         }
         if (channel.getConfigManager().getOptionBool(controller.getDomain(),
                 "hideEmptyTopicBar")) {
             setVisible(topicText.getDocument().getLength() != 0);
         }
         topicText.setCaretPosition(0);
         validateTopic();
         setVisible(false);
         setVisible(true);
     }
 
     /**
      * {@inheritDoc}
      *
      * @param e Action event
      */
     @Override
     public void actionPerformed(final ActionEvent e) {
         if (e.getSource() == topicEdit || e.getSource() == topicText) {
             if (topicText.isEditable()) {
                 if ((channel.getCurrentTopic() == null && !topicText.getText().isEmpty())
                         || (channel.getCurrentTopic() != null &&
                         !channel.getCurrentTopic().getTopic().equals(topicText.getText()))) {
                     channel.setTopic(topicText.getText());
                 }
                 ((ChannelFrame) channel.getFrame()).getInputField().
                         requestFocusInWindow();
                 topicChanged(channel, null);
                 topicText.setFocusable(false);
                 topicText.setEditable(false);
                 topicCancel.setVisible(false);
             } else {
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
         } else if (e.getSource() == topicCancel) {
             topicText.setFocusable(false);
             topicText.setEditable(false);
             topicCancel.setVisible(false);
             ((ChannelFrame) channel.getFrame()).getInputField().
                     requestFocusInWindow();
             topicChanged(channel, null);
         }
     }
 
     /** {@inheritDoc} */
     @Override
     public void hyperlinkUpdate(final HyperlinkEvent e) {
         if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
             URLHandler.getURLHander().launchApp(e.getDescription());
         }
     }
 
     private void setColours() {
         backgroundColour = channel.getConfigManager().getOptionColour(
                 "ui", "inputbackgroundcolour", "ui", "backgroundcolour");
         foregroundColour = channel.getConfigManager().getOptionColour(
                 "ui", "inputforegroundcolour", "ui", "foregroundcolour");
         setBackground(backgroundColour);
         setForeground(foregroundColour);
         setDisabledTextColour(foregroundColour);
         setCaretColor(foregroundColour);
         setAttributes();
     }
 
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
 
     private void applyAttributes() {
         setAttributes();
         ((DefaultStyledDocument) topicText.getDocument()).setCharacterAttributes(
                 0, Integer.MAX_VALUE, as, true);
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
     public void configChanged(String domain, String key) {
         if ("showfulltopic".equals(key)) {
             if (channel.getConfigManager().getOptionBool(controller.getDomain(),
                     "showfulltopic")) {
                 topicText.setEditorKit(new StyledEditorKit());
             } else {
                 topicText.setEditorKit(new WrapEditorKit());
             }
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
     public void mouseClicked(MouseEvent e) {
         if (e.getClickCount() == 2) {
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
        
