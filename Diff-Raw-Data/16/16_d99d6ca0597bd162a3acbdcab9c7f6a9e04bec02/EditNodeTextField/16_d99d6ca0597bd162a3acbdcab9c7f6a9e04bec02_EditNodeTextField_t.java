 /*
  *  Freeplane - mind map editor
  *  Copyright (C) 2008 Joerg Mueller, Daniel Polansky, Christian Foltin, Dimitry Polivaev
  *
  *  This file is modified by Dimitry Polivaev in 2008.
  *
  *  This program is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 2 of the License, or
  *  (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.freeplane.view.swing.map.mindmapmode;
 
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.Frame;
 import java.awt.Toolkit;
 import java.awt.datatransfer.DataFlavor;
 import java.awt.datatransfer.Transferable;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.IOException;
 import java.io.Writer;
 
 import javax.swing.Action;
 import javax.swing.ActionMap;
 import javax.swing.InputMap;
 import javax.swing.JComponent;
 import javax.swing.JEditorPane;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JPopupMenu;
 import javax.swing.KeyStroke;
 import javax.swing.SwingUtilities;
 import javax.swing.border.MatteBorder;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.Document;
 import javax.swing.text.StyleConstants;
 import javax.swing.text.StyledDocument;
 import javax.swing.text.StyledEditorKit;
 import javax.swing.text.StyledEditorKit.BoldAction;
 import javax.swing.text.StyledEditorKit.ForegroundAction;
 import javax.swing.text.StyledEditorKit.ItalicAction;
 import javax.swing.text.StyledEditorKit.StyledTextAction;
 import javax.swing.text.StyledEditorKit.UnderlineAction;
 import javax.swing.text.html.HTMLDocument;
 import javax.swing.text.html.HTMLEditorKit;
 import javax.swing.text.html.HTMLWriter;
 import javax.swing.text.html.MinimalHTMLWriter;
 import javax.swing.text.html.StyleSheet;
 
 import org.freeplane.core.controller.Controller;
 import org.freeplane.core.frame.ViewController;
 import org.freeplane.core.resources.ResourceController;
 import org.freeplane.core.util.ColorUtils;
 import org.freeplane.core.util.LogUtils;
 import org.freeplane.core.util.TextUtils;
 import org.freeplane.features.common.map.ModeController;
 import org.freeplane.features.common.map.NodeModel;
 import org.freeplane.features.common.styles.MapStyleModel;
 import org.freeplane.features.common.text.TextController;
 
 import org.freeplane.features.mindmapmode.ortho.SpellCheckerController;
 import org.freeplane.features.mindmapmode.text.AbstractEditNodeTextField;
 import org.freeplane.view.swing.map.MainView;
 import org.freeplane.view.swing.map.MapView;
 import org.freeplane.view.swing.map.NodeView;
 
 import com.lightdev.app.shtm.SHTMLWriter;
 
 /**
  * @author foltin
  */
 class EditNodeTextField extends AbstractEditNodeTextField {
 	private int extraWidth;
 
 	private final class MyDocumentListener implements DocumentListener {
 		private boolean updateRunning = false;
 		public void changedUpdate(final DocumentEvent e) {
 			onUpdate();
 		}
 
 		private void onUpdate() {
 			if(updateRunning){
 				return;
 			}
 			EventQueue.invokeLater(new Runnable() {
 				public void run() {
 					updateRunning = true;
 					layout();
 					updateRunning = false;
 				}
 			});
 		}
 
 		public void insertUpdate(final DocumentEvent e) {
 			onUpdate();
 		}
 
 		public void removeUpdate(final DocumentEvent e) {
 			onUpdate();
 		}
 	}
 
 	private void layout() {
 		if (textfield == null) {
 			return;
 		}
 		final int lastWidth = textfield.getWidth();
 		final int lastHeight = textfield.getHeight();
 		final boolean lineWrap = lastWidth == maxWidth;
 		Dimension preferredSize = textfield.getPreferredSize();
 		if (!lineWrap) {
 			preferredSize.width ++;
 			if (preferredSize.width > maxWidth) {
 				setLineWrap();
 				preferredSize = textfield.getPreferredSize();
 			}
 			else {
 				if (preferredSize.width < lastWidth) {
 					preferredSize.width = lastWidth;
 				}
 				else {
 					preferredSize.width = Math.min(preferredSize.width + extraWidth, maxWidth);
 					if (preferredSize.width == maxWidth) {
 						setLineWrap();
 					}
 				}
 			}
 		}
 		else {
 			preferredSize.width = Math.max(maxWidth, preferredSize.width); 
 		}
 		if(preferredSize.width != lastWidth){
 			preferredSize.height = lastHeight;
 			SwingUtilities.invokeLater(new Runnable() {
 				public void run() {
 					layout();
 				}
 			});
 		}
 		else{
 			preferredSize.height = Math.max(preferredSize.height, lastHeight);
 		}
 		if (preferredSize.width == lastWidth && preferredSize.height == lastHeight) {
 			textfield.repaint();
 			return;
 		}
 		textfield.setSize(preferredSize);
 		final JComponent mainView = (JComponent) textfield.getParent();
 		mainView.setPreferredSize(new Dimension(preferredSize.width + horizontalSpace + iconWidth, preferredSize.height
 		        + verticalSpace));
 		textfield.revalidate();
 		final NodeView nodeView = (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class, mainView);
 		final MapView mapView = (MapView) SwingUtilities.getAncestorOfClass(MapView.class, nodeView);
 		mapView.scrollNodeToVisible(nodeView);
 	}
 
 	private void setLineWrap() {
 		if(null != textfield.getClientProperty("EditNodeTextField.linewrap")){
 			return;
 		}
 	    final HTMLDocument document = (HTMLDocument) textfield.getDocument();
 	    document.getStyleSheet().addRule("body { width: " + (maxWidth - 1) + "}");
 	    textfield.setText(getNewText());
 	    textfield.putClientProperty("EditNodeTextField.linewrap", true);
     }
 
 	class TextFieldListener implements KeyListener, FocusListener, MouseListener {
 		final int CANCEL = 2;
 		final int EDIT = 1;
 		Integer eventSource = EDIT;
 		private boolean popupShown;
 
 		public TextFieldListener() {
 		}
 
 		private void conditionallyShowPopup(final MouseEvent e) {
 			if (e.isPopupTrigger()) {
 				final JPopupMenu popupMenu = createPopupMenu();
 				popupShown = true;
 				popupMenu.show(e.getComponent(), e.getX(), e.getY());
 				e.consume();
 			}
 		}
 		
 		
 
 		public void focusGained(final FocusEvent e) {
 			popupShown = false;
 		}
 
 		public void focusLost(final FocusEvent e) {
 			if (textfield == null || !textfield.isVisible() || eventSource == CANCEL || popupShown) {
 				return;
 			}
 			if (e == null) {
 				submitText();
 				hideMe();
 				eventSource = CANCEL;
 				return;
 			}
 			if (e.isTemporary() && e.getOppositeComponent() == null) {
 				return;
 			}
 			submitText();
 			hideMe();
 		}
 
 		private void submitText() {
 	        submitText(getNewText());
         }
 
 		private void submitText(final String output) {
 			getEditControl().ok(output);
         }
 
 		public void keyPressed(final KeyEvent e) {
 			if (e.isControlDown() || e.isMetaDown() || eventSource == CANCEL) {
 				return;
 			}
 			switch (e.getKeyCode()) {
 				case KeyEvent.VK_ESCAPE:
 					eventSource = CANCEL;
 					hideMe();
 					getEditControl().cancel();
 					nodeView.requestFocus();
 					e.consume();
 					break;
 				case KeyEvent.VK_ENTER: {
 					final boolean enterConfirms = ResourceController.getResourceController().getBooleanProperty(
 					    "il__enter_confirms_by_default");
 					if (enterConfirms == e.isAltDown() || e.isShiftDown()) {
 						e.consume();
 						final Component component = e.getComponent();
 						final KeyEvent keyEvent = new KeyEvent(component, e.getID(), e.getWhen(), 0, e.getKeyCode(), e
 						    .getKeyChar(), e.getKeyLocation());
 						SwingUtilities.processKeyBindings(keyEvent);
 						break;
 					}
 				}
 				final String output = getNewText();
 				e.consume();
 				eventSource = CANCEL;
 				hideMe();
 				submitText(output);
 				nodeView.requestFocus();
 				break;
 				case KeyEvent.VK_TAB:
 					textfield.replaceSelection("    ");
 				case KeyEvent.VK_SPACE:
 					e.consume();
 					break;
 			}
 		}
 
 		public void keyReleased(final KeyEvent e) {
 		}
 
 		public void keyTyped(final KeyEvent e) {
 		}
 
 		public void mouseClicked(final MouseEvent e) {
 		}
 
 		public void mouseEntered(final MouseEvent e) {
 		}
 
 		public void mouseExited(final MouseEvent e) {
 		}
 
 		public void mousePressed(final MouseEvent e) {
 			conditionallyShowPopup(e);
 		}
 
 		public void mouseReleased(final MouseEvent e) {
 			conditionallyShowPopup(e);
 		}
 	}
 
 	final private KeyEvent firstEvent;
 	private JEditorPane textfield;
 	private final DocumentListener documentListener;
 	private int maxWidth;
 
 	public EditNodeTextField(final NodeModel node, final String text, final KeyEvent firstEvent,
 	                         final IEditControl editControl) {
 		super(node, text, editControl);
 		this.firstEvent = firstEvent;
 		documentListener = new MyDocumentListener();
 		boldAction = new StyledEditorKit.BoldAction();
 		boldAction.putValue(Action.NAME, TextUtils.getText("BoldAction.text"));
 		boldAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control B"));
 	
 		italicAction = new StyledEditorKit.ItalicAction();
 		italicAction.putValue(Action.NAME, TextUtils.getText("ItalicAction.text"));
 		italicAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control I"));
 		
 		underlineAction = new StyledEditorKit.UnderlineAction();
 		underlineAction.putValue(Action.NAME, TextUtils.getText("UnderlineAction.text"));
 		underlineAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control U"));
 		
 		redAction = new ForegroundAction(TextUtils.getText("red"), Color.RED);
 		redAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control R"));
 		
 		greenAction = new ForegroundAction(TextUtils.getText("green"), Color.GREEN);
 		greenAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control G"));
 		
 		blueAction = new ForegroundAction(TextUtils.getText("blue"), Color.BLUE);
 		blueAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control E"));
 		
 		blackAction = new ForegroundAction(TextUtils.getText("black"), Color.BLACK);
 		blackAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control K"));
 		
 		defaultColorAction = new ExtendedEditorKit.RemoveStyleAttributeAction(StyleConstants.Foreground, TextUtils.getText("DefaultColorAction.text"));
 		defaultColorAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control D"));
 		
 		removeFormattingAction = new ExtendedEditorKit.RemoveStyleAttributeAction(null, TextUtils.getText("simplyhtml.clearFormatLabel"));
 		removeFormattingAction.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke("control T"));
 	}
 
 	public String getNewText() {
 		final SHTMLWriter shtmlWriter = new SHTMLWriter((HTMLDocument) textfield.getDocument());
 		try {
 	        shtmlWriter.write();
         }
         catch (Exception e) {
 	        LogUtils.severe(e);
         }
 		return shtmlWriter.toString();
     }
 
 	private void hideMe() {
 		if (textfield == null) {
 			return;
 		}
 		textfield.getDocument().removeDocumentListener(documentListener);
 		final MainView mainView = (MainView) textfield.getParent();
 		textfield = null;
 		mainView.setPreferredSize(null);
 		mainView.updateText(getNode());
 		mainView.setHorizontalAlignment(JLabel.CENTER);
 		mainView.remove(0);
 		mainView.revalidate();
 		mainView.repaint();
 	}
 
 	private NodeView nodeView;
 	private Font font;
 	private float zoom;
 	private int iconWidth;
 	private int horizontalSpace;
 	private int verticalSpace;
 	private final BoldAction boldAction;
 	private final ItalicAction italicAction;
 	private final UnderlineAction underlineAction;
 	private final ForegroundAction redAction;
 	private final ForegroundAction greenAction;
 	private final ForegroundAction blueAction;
 	private final ForegroundAction blackAction;
 	private StyledTextAction defaultColorAction;
 	private StyledTextAction removeFormattingAction;
 
 	@Override
     protected JPopupMenu createPopupMenu() {
 		JPopupMenu menu = super.createPopupMenu();
 	    JMenu formatMenu = new JMenu(TextUtils.getText("simplyhtml.formatLabel")); 
 	    menu.add(formatMenu);
 		if (textfield.getSelectionStart() == textfield.getSelectionEnd()){
 			formatMenu.setEnabled(false);
 			return menu;
 		}
 	    formatMenu.add(boldAction);
 	    formatMenu.add(italicAction);
 	    formatMenu.add(underlineAction);
 	    formatMenu.add(redAction);
 	    formatMenu.add(greenAction);
 	    formatMenu.add(blueAction);
 	    formatMenu.add(blackAction);
 	    formatMenu.add(defaultColorAction);
 	    formatMenu.add(removeFormattingAction);
 		return menu;
     }
 
 	/* (non-Javadoc)
 	 * @see org.freeplane.view.swing.map.INodeTextField#show()
 	 */
 	@SuppressWarnings("serial")
     @Override
 	public void show(final Frame frame) {
 		final ModeController modeController = Controller.getCurrentModeController();
 		final ViewController viewController = modeController.getController().getViewController();
 		final TextController textController = TextController.getController(modeController);
 		final Component component = viewController.getComponent(getNode());
 		nodeView = (NodeView) SwingUtilities.getAncestorOfClass(NodeView.class, component);
 		font = nodeView.getTextFont();
 		zoom = viewController.getZoom();
 		if (zoom != 1F) {
 			final float fontSize = (int) (Math.rint(font.getSize() * zoom));
 			font = font.deriveFont(fontSize);
 		}
 		textfield = new JEditorPane(){
 
 			@Override
             public void paste() {
 				final Transferable contents = Toolkit.getDefaultToolkit().getSystemClipboard().getContents(this);
 				if(contents.isDataFlavorSupported(DataFlavor.stringFlavor)){
 					try {
 	                    String text = (String) contents.getTransferData(DataFlavor.stringFlavor);
 	                    replaceSelection(text);
                     }
                     catch (Exception e) {
                     }
 				}
             }
 			
 		};
 		textfield.setEditorKit(new HTMLEditorKit(){
 
 			@Override
             public void write(Writer out, Document doc, int pos, int len) throws IOException, BadLocationException {
 	            if (doc instanceof HTMLDocument) {
                     HTMLWriter w = new SHTMLWriter(out, (HTMLDocument)doc, pos, len);
                     w.write();
                 } else {
                     super.write(out, doc, pos, len);
                 }
             }
 			
 		});
 
 		final InputMap inputMap = textfield.getInputMap();
 		final ActionMap actionMap = textfield.getActionMap();
 		
 		inputMap.put((KeyStroke) boldAction.getValue(Action.ACCELERATOR_KEY), "boldAction");
 		actionMap.put("boldAction",boldAction);
 		
 		inputMap.put((KeyStroke) italicAction.getValue(Action.ACCELERATOR_KEY), "italicAction");
 		actionMap.put("italicAction", italicAction);
 		
 		inputMap.put((KeyStroke) underlineAction.getValue(Action.ACCELERATOR_KEY), "underlineAction");
 		actionMap.put("underlineAction", underlineAction);
 		
 		inputMap.put((KeyStroke) redAction.getValue(Action.ACCELERATOR_KEY), "redAction");
 		actionMap.put("redAction", redAction);
 		
 		inputMap.put((KeyStroke) greenAction.getValue(Action.ACCELERATOR_KEY), "greenAction");
 		actionMap.put("greenAction", greenAction);
 		
 		inputMap.put((KeyStroke) blueAction.getValue(Action.ACCELERATOR_KEY), "blueAction");
 		actionMap.put("blueAction", blueAction);
 		
 		inputMap.put((KeyStroke) blackAction.getValue(Action.ACCELERATOR_KEY), "blackAction");
 		actionMap.put("blackAction", blackAction);
 		
 		inputMap.put((KeyStroke) defaultColorAction.getValue(Action.ACCELERATOR_KEY), "defaultColorAction");
 		actionMap.put("defaultColorAction", defaultColorAction);
 		
 		inputMap.put((KeyStroke) removeFormattingAction.getValue(Action.ACCELERATOR_KEY), "removeFormattingAction");
 		actionMap.put("removeFormattingAction", removeFormattingAction);
 		
 		final Color nodeTextColor = nodeView.getTextColor();
 		final Color nodeTextBackground = nodeView.getTextBackground();
 		textfield.setCaretColor(nodeTextColor);
 		textfield.setBackground(nodeTextBackground);
 		final StringBuilder ruleBuilder = new StringBuilder(100);
 		ruleBuilder.append("body {");
 		ruleBuilder.append("font-family: ").append(font.getFamily()).append(";");
 		ruleBuilder.append("font-size: ").append(font.getSize()).append("pt;");
 		if (font.isItalic()) {
 			ruleBuilder.append("font-style: italic; ");
 		}
 		if (font.isBold()) {
 			ruleBuilder.append("font-weight: bold; ");
 		}
 		ruleBuilder.append("color: ").append(ColorUtils.colorToString(nodeTextColor)).append(";");
 		ruleBuilder.append("}\n");
 		ruleBuilder.append("p {margin-top:0;}\n");
 		final HTMLDocument document = (HTMLDocument) textfield.getDocument();
 		final StyleSheet styleSheet = document.getStyleSheet();
 		styleSheet.removeStyle("p");
 		styleSheet.removeStyle("body");
 		styleSheet.addRule(ruleBuilder.toString());
 		textfield.setText(text);
 		final MapView mapView = (MapView) viewController.getMapView();
 		maxWidth = MapStyleModel.getExtension(mapView.getModel()).getMaxNodeWidth();
 		maxWidth = mapView.getZoomed(maxWidth) + 1;
 		extraWidth = ResourceController.getResourceController().getIntProperty("editor_extra_width", 80);
 		extraWidth = mapView.getZoomed(extraWidth);
 		final TextFieldListener textFieldListener = new TextFieldListener();
 		this.textFieldListener = textFieldListener;
 		textfield.addFocusListener(textFieldListener);
 		textfield.addKeyListener(textFieldListener);
 		textfield.addMouseListener(textFieldListener);
 		SpellCheckerController.getController().enableAutoSpell(textfield, true);
 		mapView.scrollNodeToVisible(nodeView);
 		final MainView mainView = nodeView.getMainView();
 		final int nodeWidth = mainView.getWidth();
 		final int nodeHeight = mainView.getHeight();
 		final Dimension textFieldSize;
 		textfield.setBorder(new MatteBorder(2, 2, 2, 2, nodeView.getSelectedColor()));
 		textFieldSize = textfield.getPreferredSize();
 		textFieldSize.width += 1;
 		if (textFieldSize.width > maxWidth) {
 			textFieldSize.width = maxWidth;
 			setLineWrap();
 			textFieldSize.height = textfield.getPreferredSize().height;
 			horizontalSpace = nodeWidth - textFieldSize.width;
 			verticalSpace = nodeHeight - textFieldSize.height;
 		}
 		else {
 			horizontalSpace = nodeWidth - textFieldSize.width;
 			verticalSpace = nodeHeight - textFieldSize.height;
 		}
 		if (horizontalSpace < 0) {
 			horizontalSpace = 0;
 		}
 		if (verticalSpace < 0) {
 			verticalSpace = 0;
 		}
 		textfield.setSize(textFieldSize.width, textFieldSize.height);
 		mainView.setPreferredSize(new Dimension(textFieldSize.width + horizontalSpace, textFieldSize.height
 		        + verticalSpace));
 		iconWidth = mainView.getIconWidth();
 		if (iconWidth != 0) {
 			iconWidth += mapView.getZoomed(mainView.getIconTextGap());
 			horizontalSpace -= iconWidth;
 		}

 		final int x = (horizontalSpace + 1) / 2;
 		final int y = (verticalSpace + 1) / 2;
		textfield.setBounds(x + iconWidth, y, textFieldSize.width, textFieldSize.height);
		mainView.setText("");
		mainView.setHorizontalAlignment(JLabel.LEFT);

 		mainView.add(textfield, 0);
 		textfield.setCaretPosition(document.getLength());
 		if (firstEvent != null) {
 			redispatchKeyEvents(textfield, firstEvent);
 		}
 		document.addDocumentListener(documentListener);
 		if(textController.getIsShortened(node)){
 			layout();
 		}
 		textfield.repaint();
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				if(textfield != null){
 					textfield.requestFocus();
 				}
 			}
 		});
 	}
 }
