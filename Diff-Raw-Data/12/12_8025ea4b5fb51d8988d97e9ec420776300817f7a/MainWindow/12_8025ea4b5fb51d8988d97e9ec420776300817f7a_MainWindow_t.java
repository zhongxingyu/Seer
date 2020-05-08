 package org.windom.story.ui.impl;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.io.IOException;
 import java.util.List;
 
 import javax.swing.ImageIcon;
 import javax.swing.JComponent;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTextPane;
 import javax.swing.border.EmptyBorder;
 import javax.swing.text.BadLocationException;
 import javax.swing.text.html.HTMLDocument;
 import javax.swing.text.html.HTMLEditorKit;
 
 import org.windom.story.game.Engine;
 import org.windom.story.game.action.Action;
 import org.windom.story.game.action.StartAction;
 import org.windom.story.ui.PlayerInfo;
 import org.windom.story.ui.Ui;
 import org.windom.story.utils.UiUtils;
 
 @SuppressWarnings("serial")
 public class MainWindow extends JFrame implements Ui,ActionHandler {
 	
 	private final Engine engine;
 	private final PlayerInfoRenderer playerInfoRenderer = new PlayerInfoRenderer();
 	
 	private final JTextPane contentPane;
 	private final JScrollPane choiceScroll;
 	private final ActionButtonList choicePane;
 	private final JLabel nameLabel;
 	private final JLabel classLevelLabel;
 	private final JLabel strLabel;
 	private final JLabel intLabel;
 	private final JLabel dexLabel;
 	private final JLabel hpLabel;
 	private final JLabel mpLabel;
 	private final JLabel goldLabel;
 	private final JTextPane descriptionPane;
 	
 	private final Action newAction;
 	private final Action loadAction;
 	private final Action saveAction;
 	private final Action clearAction;
 	
 	private String oldDescription = null;
 	
 	public MainWindow(Engine engine) {
 		this.engine = engine;
 		
 		setTitle("Story");
 		setSize(Constants.SIZE);
 		setMinimumSize(Constants.MIN_SIZE);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setLocationRelativeTo(null);
 		getContentPane().setBackground(Constants.BACKGROUND);
 		
 		JSplitPane splitPane = new JSplitPane();
 		splitPane.setOpaque(false);
 		splitPane.setBorder(new EmptyBorder(5, 6, 6, 6));
 		splitPane.setResizeWeight(1.0);
 		splitPane.setOrientation(JSplitPane.VERTICAL_SPLIT);
 		getContentPane().add(splitPane, BorderLayout.CENTER);
 		UiUtils.flattenSplitPane(splitPane);
 
 		JScrollPane contentScroll = new JScrollPane();
 		contentScroll.setBorder(Constants.BORDER);
 		contentScroll.setViewportBorder(null);
 		splitPane.setLeftComponent(contentScroll);
 
 		contentPane = new JTextPane();
 		contentPane.setContentType("text/html");
 		contentPane.setEditable(false);
 		contentPane.setBackground(Color.WHITE);
 		contentPane.setMargin(new Insets(7,9,7,9));
 		initTextPaneStyle(contentPane);
 		contentScroll.setViewportView(contentPane);
 		
 		choiceScroll = new JScrollPane();
 		choiceScroll.setViewportBorder(null);
 		choiceScroll.setBorder(Constants.BORDER);
 		choiceScroll.getVerticalScrollBar().setUnitIncrement(10);
 		splitPane.setRightComponent(choiceScroll);
 
 		choicePane = new ActionButtonList(this);
 		choicePane.setBackground(Color.WHITE);
 		choicePane.setBorder(new EmptyBorder(5,5,5,5));
 		choiceScroll.setViewportView(choicePane);
 
 		JPanel sidePanel = new JPanel();
 		sidePanel.setOpaque(false);
 		sidePanel.setBorder(new EmptyBorder(5, 0, 6, 5));
 		sidePanel.setPreferredSize(new Dimension(180,0));
 		getContentPane().add(sidePanel, BorderLayout.EAST);
 		sidePanel.setLayout(new BorderLayout(0, 5));
 		
 		JLabel playerInfoPanel = new JLabel();
 		playerInfoPanel.setIcon(new ImageIcon(Constants.PLAYER_INFO_BKG));
 		playerInfoPanel.setBorder(Constants.BORDER);
 		playerInfoPanel.setBackground(Color.WHITE);
 		playerInfoPanel.setPreferredSize(new Dimension(0, 200));
 		playerInfoPanel.setMinimumSize(new Dimension(0, 200));
 		sidePanel.add(playerInfoPanel, BorderLayout.NORTH);
 		GridBagLayout gbl_playerInfoPanel = new GridBagLayout();
 		gbl_playerInfoPanel.columnWidths = new int[] { 0, 0 };
 		gbl_playerInfoPanel.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 0, 0, 0 };
 		gbl_playerInfoPanel.columnWeights = new double[] { 1.0, Double.MIN_VALUE };
 		gbl_playerInfoPanel.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE };
 		playerInfoPanel.setLayout(gbl_playerInfoPanel);
 
 		nameLabel = new JLabel();
 		nameLabel.setFont(Constants.FONT_STRONG);
 		GridBagConstraints gbc_nameLabel = new GridBagConstraints();
 		gbc_nameLabel.insets = new Insets(15, 0, 5, 0);
 		gbc_nameLabel.gridx = 0;
 		gbc_nameLabel.gridy = 0;
 		playerInfoPanel.add(nameLabel, gbc_nameLabel);
 
 		classLevelLabel = new JLabel();
 		classLevelLabel.setFont(Constants.FONT);
 		GridBagConstraints gbc_classLevelLabel = new GridBagConstraints();
 		gbc_classLevelLabel.insets = new Insets(0, 0, 8, 0);
 		gbc_classLevelLabel.gridx = 0;
 		gbc_classLevelLabel.gridy = 1;
 		playerInfoPanel.add(classLevelLabel, gbc_classLevelLabel);
 
 		strLabel = new JLabel();
 		strLabel.setFont(Constants.FONT);
 		GridBagConstraints gbc_strLabel = new GridBagConstraints();
 		gbc_strLabel.insets = new Insets(0, 50, 8, 0);
 		gbc_strLabel.anchor = GridBagConstraints.WEST;
 		gbc_strLabel.gridx = 0;
 		gbc_strLabel.gridy = 2;
 		playerInfoPanel.add(strLabel, gbc_strLabel);
 
 		intLabel = new JLabel();
 		intLabel.setFont(Constants.FONT);
 		GridBagConstraints gbc_intLabel = new GridBagConstraints();
 		gbc_intLabel.anchor = GridBagConstraints.WEST;
 		gbc_intLabel.insets = new Insets(0, 50, 7, 0);
 		gbc_intLabel.gridx = 0;
 		gbc_intLabel.gridy = 3;
 		playerInfoPanel.add(intLabel, gbc_intLabel);
 
 		dexLabel = new JLabel();
 		dexLabel.setFont(Constants.FONT);
 		GridBagConstraints gbc_dexLabel = new GridBagConstraints();
 		gbc_dexLabel.insets = new Insets(0, 50, 7, 0);
 		gbc_dexLabel.anchor = GridBagConstraints.WEST;
 		gbc_dexLabel.gridx = 0;
 		gbc_dexLabel.gridy = 4;
 		playerInfoPanel.add(dexLabel, gbc_dexLabel);
 
 		hpLabel = new JLabel();
 		hpLabel.setFont(Constants.FONT);
 		GridBagConstraints gbc_hpLabel = new GridBagConstraints();
 		gbc_hpLabel.anchor = GridBagConstraints.WEST;
 		gbc_hpLabel.insets = new Insets(0, 50, 6, 0);
 		gbc_hpLabel.gridx = 0;
 		gbc_hpLabel.gridy = 5;
 		playerInfoPanel.add(hpLabel, gbc_hpLabel);
 
 		mpLabel = new JLabel();
 		mpLabel.setFont(Constants.FONT);
 		GridBagConstraints gbc_mpLabel = new GridBagConstraints();
 		gbc_mpLabel.anchor = GridBagConstraints.WEST;
 		gbc_mpLabel.insets = new Insets(0, 50, 5, 0);
 		gbc_mpLabel.gridx = 0;
 		gbc_mpLabel.gridy = 6;
 		playerInfoPanel.add(mpLabel, gbc_mpLabel);
 
 		goldLabel = new JLabel();
 		goldLabel.setFont(Constants.FONT);
 		GridBagConstraints gbc_goldLabel = new GridBagConstraints();
 		gbc_goldLabel.anchor = GridBagConstraints.WEST;
 		gbc_goldLabel.insets = new Insets(0, 50, 5, 0);
 		gbc_goldLabel.gridx = 0;
 		gbc_goldLabel.gridy = 7;
 		playerInfoPanel.add(goldLabel, gbc_goldLabel);
 
 		JScrollPane descriptionScroll = new JScrollPane();
 		descriptionScroll.setBorder(Constants.BORDER);
 		descriptionScroll.setViewportBorder(null);
 		sidePanel.add(descriptionScroll);
 
 		descriptionPane = new JTextPane();
 		descriptionPane.setContentType("text/html");
 		descriptionPane.setEditable(false);
 		descriptionPane.setText("Ez egy szoveg<br/>salalalalala\r\n\r\ntralalalala\r\nababababa");
 		descriptionPane.setBackground(Color.WHITE);
 		descriptionPane.setMargin(new Insets(10, 10, 10, 10));
 		initTextPaneStyle(descriptionPane);
 		descriptionScroll.setViewportView(descriptionPane);
 		
 		JPanel menuPanel = new JPanel();
 		menuPanel.setBorder(Constants.BORDER);
 		menuPanel.setBackground(Color.WHITE);
 		menuPanel.setLayout(new FlowLayout(FlowLayout.CENTER));
 		sidePanel.add(menuPanel,BorderLayout.SOUTH);
 		
 		newAction = new Action(null,"uj","<i>Hova gondolsz? Majd kesobb...</i>");
 		loadAction = new Action(null,"betolt","<i>Hova gondolsz? Majd kesobb...</i>");
 		saveAction = new Action(null,"ment","<i>Hova gondolsz? Majd kesobb...</i>");
 		clearAction = new Action(null,"takarit","Torli az egyre csak novekvo naplot.");
 		
 		menuPanel.add(newMenuItem(newAction));
 		menuPanel.add(newMenuItem(loadAction));
 		menuPanel.add(newMenuItem(saveAction));
 		menuPanel.add(newMenuItem(clearAction));
 	}
 	
 	public void launch() {
 		engine.handleAction(this,new StartAction());
 		setVisible(true);
 	}
 	
 	@Override
 	public void render(PlayerInfo playerInfo) {
 		nameLabel.setText(playerInfoRenderer.renderName(playerInfo));
 		classLevelLabel.setText(playerInfoRenderer.renderClassLevel(playerInfo));
 		strLabel.setText(playerInfoRenderer.renderStr(playerInfo));
 		intLabel.setText(playerInfoRenderer.renderInt(playerInfo));
 		dexLabel.setText(playerInfoRenderer.renderDex(playerInfo));
 		hpLabel.setText(playerInfoRenderer.renderHp(playerInfo));
 		mpLabel.setText(playerInfoRenderer.renderMp(playerInfo));
 		goldLabel.setText(playerInfoRenderer.renderGold(playerInfo));
 		if ("Harcos".equals(playerInfo.getClazz())) {
 			classLevelLabel.setIcon(Constants.WARRIOR_ICO);
 		} else if ("Varazslo".equals(playerInfo.getClazz())) {
 			classLevelLabel.setIcon(Constants.MAGE_ICO);
 		} else if ("Zsivany".equals(playerInfo.getClazz())) {
 			classLevelLabel.setIcon(Constants.ROGUE_ICO);
 		}
 	}
 	
 	@Override
 	public void render(List<Action> actions) {
 		choicePane.setActions(actions);
 	}
 	
 	@Override
 	public void renderDescription(String description) {
		if (oldDescription == null) {
			descriptionPane.setText(description);
		} else {
			oldDescription = description;
		}
 	}
 	
 	private void replaceDescription(String newDescription) {
 		oldDescription = descriptionPane.getText();
 		descriptionPane.setText(newDescription);
 	}
 	
 	private void restoreDescription() {
 		descriptionPane.setText(oldDescription);
 		oldDescription = null;
 	}
 	
 	private JComponent newMenuItem(Action menuAction) {
 		return new ActionButton(-1,menuAction,this);
 	}
 	
 	@Override
 	public void actionEntered(Action action) {
 		if (action.getDescription() != null) {
 			replaceDescription(action.getDescription());
 		}
 	}
 	
 	@Override
 	public void actionExited(Action action) {
 		if (action.getDescription() != null && oldDescription != null) {
 			restoreDescription();
 		}
 	}
 	
 	@Override
 	public void actionPressed(Action action) {
 		if (newAction == action)
 			doNew();
 		else if (loadAction == action)
 			doLoad();
 		else if (saveAction == action)
 			doSave();
 		else if (clearAction == action)
 			doClear();
 		else
 			engine.handleAction(this,action);
 	}
 	
 	@Override
 	public void appendContent(String content) {
 		HTMLDocument doc = (HTMLDocument) contentPane.getDocument();
 		HTMLEditorKit kit = new HTMLEditorKit();
 		try {
 			kit.insertHTML(doc,doc.getLength(),content,0,0,null);			
 		} catch (BadLocationException e) {
 			e.printStackTrace();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		contentPane.setCaretPosition(doc.getLength());
 	}
 	
 	private void doNew() {
 		//TODO implement
 	}
 	
 	private void doLoad() {
 		//TODO implement
 	}
 	
 	private void doSave() {
 		//TODO implement
 	}
 	
 	private void doClear() {
 		contentPane.setText("");
 	}
 
 	private static void initTextPaneStyle(JTextPane textPane) {
 		((HTMLDocument)textPane.getDocument()).getStyleSheet().addRule(
 			String.format(
 				"body { font-family: %s; font-size:%dpt; } " +
 				"p { margin: 3px 0px 3px 0px } " +
 				".number { font-style: italic } ",
 				Constants.FONT.getFamily(),
 				Constants.FONT.getSize()
 			)
 		);
 	}
 
 }
