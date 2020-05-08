 /*
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License. 
  */
 package net.rptools.maptool.client.ui.commandpanel;
 
 import javax.swing.AbstractAction;
 import javax.swing.ActionMap;
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.ImageIcon;
 import javax.swing.InputMap;
 import javax.swing.JButton;
 import javax.swing.JColorChooser;
 import javax.swing.JComponent;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JPopupMenu;
 import javax.swing.JScrollPane;
 import javax.swing.JTextPane;
 import javax.swing.JToggleButton;
 import javax.swing.KeyStroke;
 import javax.swing.border.BevelBorder;
 import javax.swing.plaf.basic.BasicToggleButtonUI;
 import javax.swing.text.html.StyleSheet;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Graphics2D;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Image;
 import java.awt.Insets;
 import java.awt.Rectangle;
 import java.awt.RenderingHints;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.image.BufferedImage;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Observable;
 import java.util.Observer;
 import java.util.regex.Pattern;
 
 import net.rptools.lib.AppEvent;
 import net.rptools.lib.AppEventListener;
 import net.rptools.lib.image.ImageUtil;
 import net.rptools.lib.swing.SwingUtil;
 import net.rptools.maptool.client.AppActions;
 import net.rptools.maptool.client.AppPreferences;
 import net.rptools.maptool.client.AppStyle;
 import net.rptools.maptool.client.MapTool;
 import net.rptools.maptool.client.MapToolMacroContext;
 import net.rptools.maptool.client.macro.MacroManager;
 import net.rptools.maptool.client.ui.chat.ChatProcessor;
 import net.rptools.maptool.client.ui.chat.SmileyChatTranslationRuleGroup;
 import net.rptools.maptool.client.ui.htmlframe.HTMLFrameFactory;
 import net.rptools.maptool.model.AssetManager;
 import net.rptools.maptool.model.ObservableList;
 import net.rptools.maptool.model.TextMessage;
 import net.rptools.maptool.model.Token;
 import net.rptools.maptool.util.ImageManager;
 import net.rptools.maptool.util.StringUtil;
 
 public class CommandPanel extends JPanel implements Observer {
 
 	private JLabel characterLabel;
 	private JTextPane commandTextArea;
 	private MessagePanel messagePanel;
 	private List<String> commandHistory = new LinkedList<String>();
 	private int commandHistoryIndex;
 	private TextColorWell textColorWell;
 	private JToggleButton scrollLockButton;
 	private AvatarPanel avatarPanel;
 	private JPopupMenu emotePopup;
 	private JButton emotePopupButton;
 	private String typedCommandBuffer;
 	
 	private ChatProcessor chatProcessor;
 	
 	private String identity;
 	
 	public CommandPanel() {
 		setLayout(new BorderLayout());
 		setBorder(BorderFactory.createLineBorder(Color.gray));
 		
 		add(BorderLayout.SOUTH, createSouthPanel());
 		add(BorderLayout.CENTER, getMessagePanel());
 		
 		initializeSmilies();
 		addFocusHotKey();
 	}
 	
 	public ChatProcessor getChatProcessor() {
 		return chatProcessor;
 	}
 	
 	private void initializeSmilies() {
 
 		SmileyChatTranslationRuleGroup smileyRuleGroup = new SmileyChatTranslationRuleGroup();
 		emotePopup = smileyRuleGroup.getEmotePopup();
 
 		chatProcessor = new ChatProcessor();
 		chatProcessor.install(smileyRuleGroup);
 		
 	}
 	
	/**
	 * Whether the player is currently impersonating a token
	 */
	public boolean isImpersonating() {
		return identity != null;
	}

	/**
	 * The identity currently in use, if the player is not impersonating a token, this will return the player's name
	 */
     public String getIdentity() {
     	return ( identity == null ? MapTool.getPlayer().getName() : identity );
     }
     
     public void setIdentity( String identity ) {
     	this.identity = identity;
    	if (identity == null) {
     		setCharacterLabel("");
     		avatarPanel.setImage(null);
     	} else {
     		setCharacterLabel("Speaking as: " + getIdentity() );
     		
     		if (MapTool.getFrame().getCurrentZoneRenderer() != null) {
     			Token token = MapTool.getFrame().getCurrentZoneRenderer().getZone().getTokenByName(identity);
     			if (token != null) {
     				avatarPanel.setImage(ImageManager.getImageAndWait(AssetManager.getAsset(token.getImageAssetId())));
     			} else {
     				avatarPanel.setImage(null);
     			}
     		}
     	}
 		HTMLFrameFactory.impersonateToken();
     }
     
     public JButton getEmotePopupButton() {
     	if (emotePopupButton == null) {
     		
     		try {
     			emotePopupButton = new JButton(new ImageIcon(ImageUtil.getImage("net/rptools/maptool/client/image/smiley/emsmile.png")));
     			emotePopupButton.setMargin(new Insets(0, 0, 0, 0));
     			emotePopupButton.setContentAreaFilled(false);
     			emotePopupButton.setBorderPainted(false);
     			emotePopupButton.setFocusPainted(false);
     			emotePopupButton.setOpaque(false);
     			emotePopupButton.addActionListener(new ActionListener() {
     				public void actionPerformed(ActionEvent e) {
     					emotePopup.show(emotePopupButton, 0, 0);
     				}
     			});
     		} catch (IOException ioe) {
     			ioe.printStackTrace();
     		}
     	}
     	return emotePopupButton;
     }
 	
     public JToggleButton getScrollLockButton() {
     	if (scrollLockButton == null) {
     		scrollLockButton = new JToggleButton();
     		scrollLockButton.setIcon(new ImageIcon(AppStyle.chatScrollImage));
     		scrollLockButton.setSelectedIcon(new ImageIcon(AppStyle.chatScrollLockImage));
     		scrollLockButton.setToolTipText("Scroll lock");
     		scrollLockButton.setUI(new BasicToggleButtonUI());
     		scrollLockButton.setBorderPainted(false);
     		scrollLockButton.setFocusPainted(false);
     		scrollLockButton.setPreferredSize(new Dimension(16, 16));
     	}
     	
     	return scrollLockButton;
     }
     
 	private JComponent createSouthPanel() {
 		
 		JPanel panel = new JPanel (new BorderLayout());
 
 		JPanel subPanel = new JPanel (new BorderLayout());
 		subPanel.add(BorderLayout.EAST, createTextPropertiesPanel());
 		subPanel.add(BorderLayout.NORTH, createCharacterLabel());
 		subPanel.add(BorderLayout.CENTER, createCommandPanel());
 		
 		panel.add(BorderLayout.WEST, createAvatarPanel());
 		panel.add(BorderLayout.CENTER, subPanel);
 		
 		return panel;
 	}
 
 	private JComponent createAvatarPanel() {
 		
 		avatarPanel = new AvatarPanel(new Dimension(60, 60));
 		
 		return avatarPanel;
 	}
 	
 	private JComponent createCommandPanel() {
 		
 		JScrollPane pane = new JScrollPane(getCommandTextArea(), JScrollPane.VERTICAL_SCROLLBAR_AS_NEEDED, JScrollPane.HORIZONTAL_SCROLLBAR_NEVER);		
 		return pane;
 	}
 	
 	private JComponent createTextPropertiesPanel() {
 		JPanel panel = new JPanel(new GridBagLayout());
 		panel.setBorder(BorderFactory.createEmptyBorder(0, 3, 0, 3));
 
 		GridBagConstraints constraints = new GridBagConstraints();
 		constraints.gridx = 0;
 		constraints.gridy = 0;
 		
 		panel.add(getTextColorWell(), constraints);
 		
 		constraints.gridy++;
 		panel.add(Box.createVerticalStrut(2), constraints);
 		
 		constraints.gridy++;
 		panel.add(getScrollLockButton(), constraints);
 
 		constraints.gridx = 1;
 		constraints.gridy = 0;
 		
 		panel.add(getEmotePopupButton(), constraints);
 		
 		return panel;
 	}
 	
 	public String getMessageHistory() {
 		return messagePanel.getMessagesText();
 	}
 	
 	public void setCharacterLabel( String label ) {
 		characterLabel.setText( label );
 	}
 
 	@Override
 	public Dimension getPreferredSize() {
 		return new Dimension(50, 50);
 	}
 
 	@Override
 	public Dimension getMinimumSize() {
 		return getPreferredSize();
 	}
 	
 	public JLabel createCharacterLabel() {
 		
 		characterLabel = new JLabel("", JLabel.LEFT);
 		characterLabel.setText("");
 		characterLabel.setBorder(BorderFactory.createEmptyBorder(1,5,1,5));
 		
 		return characterLabel;
 	}
 	
 	public JTextPane getCommandTextArea() {
 		if (commandTextArea == null) {
 			commandTextArea = new JTextPane(){
 				@Override
 				protected void paintComponent(Graphics g) {
 					super.paintComponent(g);
 					
 					Dimension size = getSize();
 					g.setColor(Color.gray);
 					g.drawLine(0, 0, size.width, 0);
 				}
 			};
 			commandTextArea.setBorder(BorderFactory.createEmptyBorder(0, 5, 0, 5));
 			commandTextArea.setPreferredSize(new Dimension(50, 40));
 			commandTextArea.setFont(new Font("sans-serif", 0, AppPreferences.getFontSize()));
 			SwingUtil.useAntiAliasing(commandTextArea);
 			
 			ActionMap actions = commandTextArea.getActionMap();
 			actions.put(AppActions.COMMIT_COMMAND_ID,
 							AppActions.COMMIT_COMMAND);
 			actions.put(AppActions.ENTER_COMMAND_ID, AppActions.ENTER_COMMAND);
 			actions.put(AppActions.CANCEL_COMMAND_ID, AppActions.CANCEL_COMMAND);
 			actions.put(AppActions.COMMAND_UP_ID, new CommandHistoryUpAction());
 			actions.put(AppActions.COMMAND_DOWN_ID, new CommandHistoryDownAction());
 			
 			InputMap inputs = commandTextArea.getInputMap();
 			inputs.put(KeyStroke.getKeyStroke("ESCAPE"),
 					AppActions.CANCEL_COMMAND_ID);
 			inputs.put(KeyStroke.getKeyStroke("ENTER"),
 					AppActions.COMMIT_COMMAND_ID);
 			inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_UP, 0), AppActions.COMMAND_UP_ID);
 			inputs.put(KeyStroke.getKeyStroke(KeyEvent.VK_DOWN, 0), AppActions.COMMAND_DOWN_ID);
 			
 			// Resize on demand
 			MapTool.getEventDispatcher().addListener(MapTool.PreferencesEvent.Changed, new AppEventListener() {
 				public void handleAppEvent(AppEvent event) {
 					commandTextArea.setFont(commandTextArea.getFont().deriveFont((float)AppPreferences.getFontSize()));
 					doLayout();
 				}
 			});
 		}
 
 		return commandTextArea;
 	}
 
 	/* FIXME: this is insufficient for stopping faked rolls; the user can still
 	 * do something like &{"laquo;"}.
 	 */
     private static final Pattern CHEATER_PATTERN = Pattern.compile("«|»|&#171;|&#187;|&laquo;|&raquo;|\036|\037");
 
     /**
 	 * Execute the command in the command field.
 	 */
 	public void commitCommand() {
 		commitCommand(null);
 	}
 	
 	/**
 	 * Execute the command in the command field.
 	 * @param macroContext The context we are calling the macro in.
 	 */
     public void commitCommand(MapToolMacroContext macroContext) {
 		String text = commandTextArea.getText().trim();
 		if (text.length() == 0) {
 			return;
 		}
 
 		// Command history
 		// Don't store up a bunch of repeats
 		if (commandHistory.size() == 0 || !text.equals(commandHistory.get(commandHistory.size()-1))) {
 			commandHistory.add(text);
 			typedCommandBuffer = null;
 		}
 		commandHistoryIndex = commandHistory.size();
 
 		// Detect whether the person is attempting to fake rolls. 
     	if (CHEATER_PATTERN.matcher(text).find()) {
     		MapTool.addServerMessage(TextMessage.me(null, "Cheater. You have been reported."));
     		MapTool.serverCommand().message(TextMessage.gm(null, MapTool.getPlayer().getName() + " was caught <i>cheating</i>: " + text));
     		commandTextArea.setText("");
     		return;
     	}
 
     	// Make sure they aren't trying to break out of the div
     	// FIXME: as above, </{"div"}> can be used to get around this
     	int divCount = StringUtil.countOccurances(text, "<div");
     	int closeDivCount = StringUtil.countOccurances(text, "</div>");
     	while (closeDivCount < divCount) {
     		text += "</div>";
     		closeDivCount ++;
     	}
     	if (closeDivCount > divCount) {
     		MapTool.addServerMessage(TextMessage.me(null, "You have too many &lt;/div&gt;."));
     		commandTextArea.setText("");
     		return;
     	}
     	
 		if (text.charAt(0) != '/') {
 			// Assume a "SAY"
 			text = "/s " + text;
 		}
 		MacroManager.executeMacro(text, macroContext);
 		commandTextArea.setText("");
 	}
 	
 	public void clearMessagePanel() {
 		messagePanel.clearMessages();
 	}
 
 	/**
 	 * Cancel the current command in the command field.
 	 */
 	public void cancelCommand() {
 		commandTextArea.setText("");
 		validate();
 		
 		MapTool.getFrame().hideCommandPanel();
 	}
 	
 	public void startMacro() {
 		MapTool.getFrame().showCommandPanel();
 		
 		commandTextArea.requestFocusInWindow();
 		commandTextArea.setText("/");
 	}
 
 	public void startChat() {
 		MapTool.getFrame().showCommandPanel();
 		
 		commandTextArea.requestFocusInWindow();
 	}
 
 	public TextColorWell getTextColorWell() {
 		if (textColorWell == null) {
 			textColorWell = new TextColorWell();
 		}
 		
 		return textColorWell;
 	}
 	
 	private class CommandHistoryUpAction extends AbstractAction {
 		
 		public void actionPerformed(ActionEvent e) {
 			if (commandHistory.size() == 0) {
 				return;
 			}
 			
 			if (commandHistoryIndex == commandHistory.size()) {
 				typedCommandBuffer = getCommandTextArea().getText();
 			}
 			
 			commandHistoryIndex --;
 			if (commandHistoryIndex < 0) {
 				commandHistoryIndex = 0;
 			}
 
 			commandTextArea.setText(commandHistory.get(commandHistoryIndex));
 		}
 	}
 	
 	private class CommandHistoryDownAction extends AbstractAction {
 		
 		public void actionPerformed(ActionEvent e) {
 			if (commandHistory.size() == 0) {
 				return;
 			}
 			commandHistoryIndex ++;
 			if (commandHistoryIndex == commandHistory.size()) {
 				commandTextArea.setText(typedCommandBuffer != null ? typedCommandBuffer : "");
 				commandHistoryIndex = commandHistory.size();
 			} else if (commandHistoryIndex >= commandHistory.size()) {
 				commandHistoryIndex--;
 			} else {
 				commandTextArea.setText(commandHistory.get(commandHistoryIndex));
 			}
 		}
 	}
 	
 	@Override
 	public void requestFocus() {
 		commandTextArea.requestFocus();
 	}
 	
 	private MessagePanel getMessagePanel() {
 		if (messagePanel == null) {
 			messagePanel = new MessagePanel();
 			
 			// Update whenever the preferences change
 			MapTool.getEventDispatcher().addListener(MapTool.PreferencesEvent.Changed, new AppEventListener() {
 				public void handleAppEvent(AppEvent event) {
 					messagePanel.refreshRenderer();
 				}
 			});
 		}
 
 		return messagePanel;
 	}
 
 	public void addMessage(TextMessage message) {
 		messagePanel.addMessage(message);
 	}
 	
 	
 	public void setTrustedMacroPrefixColors(Color foreground, Color background) {
 		getMessagePanel().setTrustedMacroPrefixColors(foreground, background);
 	}
 	
 
 	
 	public static class TextColorWell extends JPanel {
 
 		//Set the Color from the saved chat color from AppPreferences
 		private Color color = AppPreferences.getChatColor();
 				
 		public TextColorWell() {
 			setMinimumSize(new Dimension(15, 15));
 			setPreferredSize(new Dimension(15, 15));
 			setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
 			
 			addMouseListener(new MouseAdapter() {
 				public void mouseClicked(MouseEvent e) {
 					Color newColor = JColorChooser.showDialog(TextColorWell.this, "Text Color", color);
 					if (newColor != null) {
 						setColor(newColor);
 					}
 				}
 			});
 			
 			setToolTipText("Set the color of your speech text");
 		}
 		
 		public void setColor(Color newColor){
 	    	color = newColor;
 			repaint();
 			AppPreferences.setChatColor(color); //Set the Chat Color in AppPreferences
 	    }
 	
 		public Color getColor() {
 			return color;
 		}
 		
 		@Override
 		protected void paintComponent(Graphics g) {
 			g.setColor(color);
 			g.fillRect(0, 0, getSize().width, getSize().height);
 		}
 	}
 	
 	private class AvatarPanel extends JComponent {
 
 		private static final int PADDING = 5;
 		
 		private Image image;
 		private Dimension preferredSize;
 		private Rectangle cancelBounds;
 		
 		public AvatarPanel(Dimension preferredSize) {
 			this.preferredSize = preferredSize;
 			
 			setImage(null);
 			
 			addMouseListener(new MouseAdapter() {
 				@Override
 				public void mouseClicked(MouseEvent e) {
 					if (cancelBounds != null && cancelBounds.contains(e.getPoint())) {
 						JTextPane commandArea = getCommandTextArea();
 
 						commandArea.setText("/im");
 						MapTool.getFrame().getCommandPanel().commitCommand();
 					}
 				}
 			});
 		}
 
 		public void setImage(Image image) {
 			this.image = image;
 			setPreferredSize(image != null ? preferredSize : new Dimension(0, 0));
 			
 			invalidate();
 			repaint();
 		}
 		
 		@Override
 		protected void paintComponent(Graphics g) {
 			Dimension size = getSize();
 			g.setColor(getBackground());
 			g.fillRect(0, 0, size.width, size.height);
 			
 			cancelBounds = null;
 			if (image == null) {
 				return;
 			}
 			
 			Dimension imgSize = new Dimension(image.getWidth(null), image.getHeight(null));
 			SwingUtil.constrainTo(imgSize, size.width-PADDING*2, size.height-PADDING*2);
 
 			((Graphics2D)g).setRenderingHint(RenderingHints.KEY_RENDERING, RenderingHints.VALUE_RENDER_QUALITY);
 			g.drawImage(image, (size.width-imgSize.width)/2, (size.height-imgSize.height)/2, imgSize.width, imgSize.height, this);
 			
 			// Cancel
 			BufferedImage cancelButton = AppStyle.cancelButton;
 			int x = size.width - cancelButton.getWidth();
 			int y = 2;
 			g.drawImage(cancelButton, x, y, this);
 			cancelBounds = new Rectangle(x, y, cancelButton.getWidth(), cancelButton.getHeight());
 		}
 	}
 	
 	////
 	// OBSERVER
 	public void update(Observable o, Object arg) {
 	    ObservableList<TextMessage> textList = MapTool.getMessageList();   
 	    ObservableList.Event event = (ObservableList.Event)arg; 
 	    switch (event) {
 	    case append:
 	      addMessage(textList.get(textList.size() - 1));
 	      break;
 	    case add:
 	    case remove:
 	      //resetMessagePanel();
 	      break;
 	    case clear:
 	      clearMessagePanel();
 	      break;
 	    default:
 	      throw new IllegalArgumentException("Unknown event: " + event);
 	    } 
 	}	
 	
 	private void addFocusHotKey() {
 		KeyStroke ks = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, 0);
 		Object actionObject = new Object();
 		getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(ks, actionObject);
 		getActionMap().put(actionObject, new AbstractAction() {
 			public void actionPerformed(ActionEvent event) {
 				requestFocus();
 }
 		});
 	}
 
 	/**
 	 * Convienence method to run a command with ability to preserve the text
 	 * in the commandTextArea. 
 	 * @param command Command string
 	 * @param preserveOldText true for preserving, false to remove the old text 
 	 */
 	public void quickCommit(String command, boolean preserveOldText) {
 		String oldText = commandTextArea.getText();
 		commandTextArea.setText(command);
 		commitCommand();
 		if (preserveOldText) {
 			commandTextArea.setText(oldText);
 		}
 	}
 
 	public void quickCommit(String command) {
 		quickCommit(command, true);
 	}
 }
