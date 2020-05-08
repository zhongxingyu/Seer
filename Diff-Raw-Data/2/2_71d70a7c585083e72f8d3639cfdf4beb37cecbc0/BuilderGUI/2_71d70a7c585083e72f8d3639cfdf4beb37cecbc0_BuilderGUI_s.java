 /**
  * @file BuilderGUI.java
  * @author Jia Chen
  * @date 09/06/2011
  * @description 
  * 		BuilderGUI.java displays the deck building client. Allowing 
  * 		users to save and load decks as well as customize them
  */
 
 /**
  * drag and drop (not possible currently, need research)
  * print deck with card translation and image (implementation)
  * select and copy card info
  */
 
 package DeckBuilder;
 
 import java.awt.*;
 /*import java.awt.datatransfer.DataFlavor;
  import java.awt.datatransfer.Transferable;
  import java.awt.datatransfer.UnsupportedFlavorException;*/
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusEvent;
 import java.awt.event.FocusListener;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 import java.awt.event.MouseListener;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.ObjectInputStream;
 //import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URISyntaxException;
 import java.text.NumberFormat;
 import java.text.ParseException;
 import java.util.ArrayList;
 import java.util.HashMap;
 
 //import javax.imageio.ImageIO;
 import javax.swing.*;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.HyperlinkEvent;
 import javax.swing.event.HyperlinkListener;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableModel;
 import javax.swing.table.TableRowSorter;
 import javax.swing.UIManager.*;
 
 import CardAssociation.*;
 
 public class BuilderGUI extends JFrame {
 
 	private static final long serialVersionUID = -6401235618421175285L;
 
 	// private properties of BuilderGUI
 
 	private Box searchBox;
 	private Box listBox;
 	private JMenuBar menu;
 	private JPanel cardInfo;
 	private Box deckList;
 
 	// private JMenuItem newD;
 	// private JMenuItem save;
 	// private JMenuItem load;
 	// private JMenuItem exit;
 
 	private JButton newdb;
 	private JButton saveb;
 	private JButton loadb;
 	private JButton exitb;
 
 	private Deck currentDeck;
 	private boolean changes;
 	private Card selectedCard;
 
 	private final int OFFSET = 35;
 	private final int DECKPERLINE = 15;
 	private final int RESULTPERLINE = 10;
 	private final int MAXIMUMRESULTSHOWN = 500;
 
 	private JFileChooser fc;
 	private ArrayList<Card> completeList;
 	private HashMap<String, Card> cardHolder;
 
 	private ArrayList<Card> resultList;
 	private File file;
 
 	// private static String datafile;
 
 	// UI Components (Panes)
 	private JTabbedPane resultArea;
 	private JScrollPane resultPane;
 	private JScrollPane resultThumbPane;
 
 	private JTabbedPane deckArea;
 	private JScrollPane deckPane;
 
 	// Header for result area
 	private JLabel resultHeader;
 
 	// Components for tables
 	private TableCellRenderer cardIDRenderer;
 	private TableCellRenderer numberRenderer;
 	private JTable resultListTable;
 	private ResultListTableModel resultListModel;
 	private JTable deckListTable;
 	private DeckListTableModel deckListModel;
 
 	// Text in stats box
 	private JLabel cardCountText;
 	private JLabel climaxCountText;
 	private JLabel lv0CountText;
 	private JLabel lv1CountText;
 	private JLabel lv2CountText;
 	private JLabel lv3CountText;
 	private JLabel soulCountText;
 
 	private JLabel eventCountText;
 	private JLabel yellowCountText;
 	private JLabel greenCountText;
 	private JLabel redCountText;
 	private JLabel blueCountText;
 
 	private JComponent previousFocus;
 
 	private JScrollPane deckThumbPane;
 
 	/**
 	 * Start the GUI client
 	 */
 	public BuilderGUI() {
 		super("Weiss Schwarz Deck Builder");
 
 		resultList = new ArrayList<Card>();
 
 		searchBox = Box.createVerticalBox();
 		listBox = Box.createVerticalBox();
 		listBox.setBorder(BorderFactory.createCompoundBorder(
 				BorderFactory.createTitledBorder("Search Result"), null));
 		listBox.setPreferredSize(new Dimension(520, 500));
 		menu = new JMenuBar();
 		cardInfo = new JPanel();
 		deckList = Box.createHorizontalBox();
 		deckList.setBorder(BorderFactory.createCompoundBorder(
 				BorderFactory.createTitledBorder("Deck List"), null));
 
 		fc = new JFileChooser();
 
 		currentDeck = new Deck();
 		selectedCard = null;
 		file = null;
 		changes = false;
 
 		if (!System.getProperty("os.name").toLowerCase().contains("mac")) {
 			try {
 				for (LookAndFeelInfo info : UIManager
 						.getInstalledLookAndFeels()) {
 					if ("Nimbus".equals(info.getName())) {
 						UIManager.setLookAndFeel(info.getClassName());
 						break;
 					}
 				}
 			} catch (Exception e) {
 				System.err.println("Nimbus not available");
 			}
 		}
 
 		deserializer();
 
 		setSize(1000, 720);
 		setResizable(false);
 	}
 
 	// ///////////////////////
 	//
 	// GUI Builders
 	//
 	// //////////////////////
 
 	/**
 	 * Building search box
 	 */
 	@SuppressWarnings({ "rawtypes", "unchecked" })
 	private void buildSearchBox() {
 		Box row1 = Box.createHorizontalBox();
 		Box row2 = Box.createHorizontalBox();
 		Box row3 = Box.createHorizontalBox();
 		final NumberFormat numberInput = NumberFormat.getInstance();
 		numberInput.setParseIntegerOnly(true);
 
 		final JTextField idSearch = new JTextField();
 		final JTextField nameSearch = new JTextField();
 		final JTextField traitSearch = new JTextField();
 		// final JTextField typeSearch = new JTextField();
 		// final JTextField colorSearch = new JTextField();
 		final JTextField abilitySearch = new JTextField();
 		final JTextField powerSearch = new JTextField();
 		final JTextField costSearch = new JTextField();
 		// final JTextField levelSearch = new JTextField();
 		// final JTextField soulSearch = new JTextField();
 		// final JTextField triggerSearch = new JTextField();
 
 		CCode[] colorSelections = null;
 		colorSelections = CCode.values();
 		final JComboBox colorList = new JComboBox(colorSelections);
 		colorList.setSelectedItem(null);
 
 		CardAssociation.Type[] classifications = null;
 		classifications = CardAssociation.Type.values();
 		final JComboBox typeList = new JComboBox(classifications);
 		typeList.setSelectedItem(null);
 
 		final String[] levelSelections = {"","0","1","2","3"};
 		final JComboBox levelList = new JComboBox(levelSelections);
 		levelList.setSelectedItem(levelSelections[0]);
 		
 		final String[] soulSelections = {"","1","2","3"};
 		final JComboBox soulList = new JComboBox(soulSelections);
 		soulList.setSelectedItem(soulSelections[0]);
 		
 		Trigger[] triggerSelections = null;
 		triggerSelections = Trigger.values();
 		final JComboBox triggerList = new JComboBox(triggerSelections);
 		triggerList.setSelectedItem(null);
 
 		final KeyListener searchFieldListener = new KeyListener() {
 
 			@Override
 			public void keyPressed(KeyEvent e) {
 				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
 					e.consume();
 				}
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
 					resultList.clear();
 
 					/*
 					 * System.out.println(idSearch.getText() +
 					 * nameSearch.getText() + triggerSearch.getText() +
 					 * powerSearch.getText() + costSearch.getText() +
 					 * colorSearch.getText() + levelSearch.getText() +
 					 * traitSearch.getText() + typeSearch.getText() +
 					 * soulSearch.getText());
 					 */
 
 					String cardID = idSearch.getText();
 
 					String name = nameSearch.getText();
 
 					CCode sColor = (CCode) colorList.getSelectedItem();
 					/*
 					 * String color = colorSearch.getText(); if
 					 * (color.equalsIgnoreCase("RED")) { sColor = CCode.RED; }
 					 * else if (color.equalsIgnoreCase("BLUE")) { sColor =
 					 * CCode.BLUE; } else if (color.equalsIgnoreCase("YELLOW"))
 					 * { sColor = CCode.YELLOW; } else if
 					 * (color.equalsIgnoreCase("GREEN")) { sColor = CCode.GREEN;
 					 * } else { sColor = null; }
 					 */
 
 					CardAssociation.Type sType = (CardAssociation.Type) typeList
 							.getSelectedItem();
 					/*
 					 * String type = typeSearch.getText(); if
 					 * (type.equalsIgnoreCase("CHARACTER")) { sType =
 					 * CardAssociation.Type.CHARACTER; } else if
 					 * (type.equalsIgnoreCase("CLIMAX")) { sType =
 					 * CardAssociation.Type.CLIMAX; } else if
 					 * (type.equalsIgnoreCase("EVENT")) { sType =
 					 * CardAssociation.Type.EVENT; } else { sType = null; }
 					 */
 
 					//String level = levelSearch.getText();
 					String level = (String) levelList.getSelectedItem();
 					int sLevel;
 					try {
 						sLevel = numberInput.parse(level).intValue();
 					} catch (ParseException e1) {
 						sLevel = -1;
 						// sLevel = (level.isEmpty()) ? -1 : 0;
 					}
 
 					String cost = costSearch.getText();
 					int sCost;
 					try {
 						sCost = numberInput.parse(cost).intValue();
 					} catch (ParseException e1) {
 						sCost = -1;
 						// sCost = (cost.isEmpty()) ? -1 : 0;
 					}
 
 					Trigger sTrigger = (Trigger) triggerList.getSelectedItem();
 					/*
 					 * String trigger = triggerSearch.getText(); sTrigger =
 					 * Trigger.convertString(trigger);
 					 */
 
 					String power = powerSearch.getText();
 					int sPower;
 					try {
 						sPower = numberInput.parse(power).intValue();
 					} catch (ParseException e1) {
 						sPower = -1;
 						// sPower = (power.isEmpty()) ? -1 : 0;
 					}
 					// String soul = soulSearch.getText();
 					String soul = (String) soulList.getSelectedItem();
 					int sSoul;
 					try {
 						sSoul = numberInput.parse(soul).intValue();
 					} catch (ParseException e1) {
 						sSoul = -1;
 						// sSoul = (soul.isEmpty()) ? -1 : 0;
 					}
 					String trait = traitSearch.getText();
 
 					String sAbility = abilitySearch.getText();
 
 					for (Card c : completeList) {
 						if (c.meetsRequirement(cardID, name, sColor, sType,
 								sLevel, sCost, sTrigger, sPower, sSoul, trait,
 								sAbility))
 							resultList.add(c);
 					}
 
 					refresh("search");
 				}
 			}
 
 			@Override
 			public void keyTyped(KeyEvent arg0) {
 
 			}
 
 		};
 
 		/*
 		 * final JFormattedTextField powerSearch = new JFormattedTextField(
 		 * numberInput); final JFormattedTextField costSearch = new
 		 * JFormattedTextField( numberInput); final JFormattedTextField
 		 * levelSearch = new JFormattedTextField( numberInput); final
 		 * JFormattedTextField soulSearch = new JFormattedTextField(
 		 * numberInput);
 		 */
 
 		JButton submitButton = new JButton("Submit");
 		// submitButton.setPreferredSize(new Dimension(100, 25));
 		JButton clearButton = new JButton("Clear All");
 		// submitButton.setPreferredSize(new Dimension(100, 25));
 
 		// nameSearch.setMaximumSize(new Dimension(255, 20));
 
 		JLabel idLabel = new JLabel("Card ID");
 		JLabel nameLabel = new JLabel("Card Name");
 		JLabel triggerLabel = new JLabel("Trigger");
 		JLabel powerLabel = new JLabel("Power");
 		JLabel costLabel = new JLabel("Cost");
 		JLabel colorLabel = new JLabel("Color");
 		JLabel levelLabel = new JLabel("Level");
 		JLabel traitLabel = new JLabel("Trait");
 		JLabel typeLabel = new JLabel("Type");
 		JLabel soulLabel = new JLabel("Soul");
 		JLabel abilityLabel = new JLabel("Ability");
 
 		idLabel.setLabelFor(idSearch);
 		nameLabel.setLabelFor(nameSearch);
 		// triggerLabel.setLabelFor(triggerSearch);
 		powerLabel.setLabelFor(powerSearch);
 		costLabel.setLabelFor(costSearch);
 		// colorLabel.setLabelFor(colorSearch);
 		// levelLabel.setLabelFor(levelSearch);
 		traitLabel.setLabelFor(traitSearch);
 		// typeLabel.setLabelFor(typeSearch);
 		// soulLabel.setLabelFor(soulSearch);
 		abilityLabel.setLabelFor(abilitySearch);
 
 		// searchBox.setLayout(new GridLayout(0, 2));
 
 		idSearch.addKeyListener(searchFieldListener);
 		nameSearch.addKeyListener(searchFieldListener);
 		traitSearch.addKeyListener(searchFieldListener);
 		// typeSearch.addKeyListener(searchFieldListener);
 		// colorSearch.addKeyListener(searchFieldListener);
 		abilitySearch.addKeyListener(searchFieldListener);
 		powerSearch.addKeyListener(searchFieldListener);
 		costSearch.addKeyListener(searchFieldListener);
 		// levelSearch.addKeyListener(searchFieldListener);
 		// soulSearch.addKeyListener(searchFieldListener);
 		// triggerSearch.addKeyListener(searchFieldListener);
 
 		submitButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 
 				resultList.clear();
 
 				String cardID = idSearch.getText();
 
 				String name = nameSearch.getText();
 
 				CCode sColor = (CCode) colorList.getSelectedItem();
 
 				CardAssociation.Type sType = (CardAssociation.Type) typeList
 						.getSelectedItem();
 
 				//String level = levelSearch.getText();
 				String level = (String) levelList.getSelectedItem();
 				int sLevel;
 				try {
 					sLevel = numberInput.parse(level).intValue();
 				} catch (ParseException e1) {
 					sLevel = -1;
 				}
 
 				String cost = costSearch.getText();
 				int sCost;
 				try {
 					sCost = numberInput.parse(cost).intValue();
 				} catch (ParseException e1) {
 					sCost = -1;
 				}
 
 				Trigger sTrigger = (Trigger) triggerList.getSelectedItem();
 
 				String power = powerSearch.getText();
 				int sPower;
 				try {
 					sPower = numberInput.parse(power).intValue();
 				} catch (ParseException e1) {
 					sPower = -1;
 				}
 				//String soul = soulSearch.getText();
 				String soul = (String) soulList.getSelectedItem();
 				int sSoul;
 				try {
 					sSoul = numberInput.parse(soul).intValue();
 				} catch (ParseException e1) {
 					sSoul = -1;
 				}
 				String trait = traitSearch.getText();
 
 				String sAbility = abilitySearch.getText();
 
 				for (Card c : completeList) {
 					if (c.meetsRequirement(cardID, name, sColor, sType, sLevel,
 							sCost, sTrigger, sPower, sSoul, trait, sAbility))
 						resultList.add(c);
 				}
 
 				refresh("search");
 			}
 		});
 
 		clearButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				idSearch.setText("");
 				nameSearch.setText("");
 				traitSearch.setText("");
 				abilitySearch.setText("");
 				powerSearch.setText("");
 				costSearch.setText("");
 				//soulSearch.setText("");
 				//levelSearch.setText("");
 				soulList.setSelectedItem(soulSelections[0]);
 				levelList.setSelectedItem(levelSelections[0]);
 				
 				colorList.setSelectedItem(null);
 				typeList.setSelectedItem(null);
 				triggerList.setSelectedItem(null);
 			}
 		});
 
 		row1.add(Box.createHorizontalStrut(5));
 		row1.add(idLabel);
 		row1.add(Box.createHorizontalStrut(5));
 		row1.add(idSearch);
 		row1.add(Box.createHorizontalStrut(5));
 		row1.add(nameLabel);
 		row1.add(Box.createHorizontalStrut(5));
 		row1.add(nameSearch);
 		row1.add(Box.createHorizontalStrut(5));
 		row1.add(colorLabel);
 		row1.add(Box.createHorizontalStrut(5));
 		row1.add(colorList);
 		row1.add(Box.createHorizontalStrut(5));
 		row1.add(typeLabel);
 		row1.add(Box.createHorizontalStrut(5));
 		row1.add(typeList);
 		row1.add(Box.createHorizontalStrut(5));
 		row1.add(submitButton);
 		row1.add(Box.createHorizontalStrut(5));
 
 		row2.add(Box.createHorizontalStrut(5));
 		row2.add(triggerLabel);
 		row2.add(Box.createHorizontalStrut(5));
 		row2.add(triggerList);
 		row2.add(Box.createHorizontalStrut(5));
 		row2.add(powerLabel);
 		row2.add(Box.createHorizontalStrut(5));
 		row2.add(powerSearch);
 		row2.add(Box.createHorizontalStrut(5));
 		row2.add(costLabel);
 		row2.add(Box.createHorizontalStrut(5));
 		row2.add(costSearch);
 		row2.add(Box.createHorizontalStrut(5));
 		row2.add(soulLabel);
 		row2.add(Box.createHorizontalStrut(5));
 		row2.add(soulList);
 		row2.add(Box.createHorizontalStrut(5));
 		row2.add(levelLabel);
 		row2.add(Box.createHorizontalStrut(5));
 		row2.add(levelList);
 		row2.add(Box.createHorizontalStrut(5));
 		row2.add(traitLabel);
 		row2.add(Box.createHorizontalStrut(5));
 		row2.add(traitSearch);
 		row2.add(Box.createHorizontalStrut(5));
 		row2.add(clearButton);
 		row2.add(Box.createHorizontalStrut(5));
 
 		row3.add(Box.createHorizontalStrut(5));
 		row3.add(abilityLabel);
 		row3.add(Box.createHorizontalStrut(5));
 		row3.add(abilitySearch);
 		row3.add(Box.createHorizontalStrut(5));
 
 		searchBox.add(row1);
 		searchBox.add(row2);
 		searchBox.add(row3);
 	}
 
 	/**
 	 * Building the menu bar
 	 */
 	/*
 	 * private void buildMenu() {
 	 * 
 	 * JMenu firstTab = new JMenu("File");
 	 * 
 	 * newD = new JMenuItem("New"); save = new JMenuItem("Save"); load = new
 	 * JMenuItem("Load"); exit = new JMenuItem("Exit");
 	 * 
 	 * newD.addActionListener(new ActionListener() {
 	 * 
 	 * @SuppressWarnings("unchecked")
 	 * 
 	 * @Override public void actionPerformed(ActionEvent e) { if (changes) {
 	 * saveOption(); } file = null; currentDeck = new Deck();
 	 * 
 	 * resultList = (ArrayList<Card>) completeList.clone();
 	 * 
 	 * refresh("new"); } });
 	 * 
 	 * save.addActionListener(new ActionListener() {
 	 * 
 	 * @Override public void actionPerformed(ActionEvent e) { changes = false;
 	 * action(e); } });
 	 * 
 	 * load.addActionListener(new ActionListener() {
 	 * 
 	 * @Override public void actionPerformed(ActionEvent e) { currentDeck = new
 	 * Deck(); action(e); changes = false; } });
 	 * 
 	 * exit.addActionListener(new ActionListener() {
 	 * 
 	 * @Override public void actionPerformed(ActionEvent e) { saveOption();
 	 * System.exit(1); } });
 	 * 
 	 * firstTab.add(newD); firstTab.add(save); firstTab.add(load);
 	 * firstTab.add(exit);
 	 * 
 	 * menu.add(firstTab);
 	 * 
 	 * }
 	 */
 
 	/**
 	 * Building the selection pane
 	 * 
 	 * @return Box populated with the option buttons
 	 */
 	private Box buildOption() {
 
 		Box box = Box.createHorizontalBox();
 
 		newdb = new JButton("New");
 		saveb = new JButton("Save");
 		loadb = new JButton("Load");
 		exitb = new JButton("Exit");
 
 		newdb.addActionListener(new ActionListener() {
 			@SuppressWarnings("unchecked")
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (changes) {
 					saveOption();
 				}
 				file = null;
 				currentDeck = new Deck();
 
 				resultList = (ArrayList<Card>) completeList.clone();
 
 				refresh("new");
 			}
 		});
 
 		saveb.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				changes = false;
 				action(e);
 			}
 		});
 
 		loadb.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				// currentDeck = new Deck();
 				action(e);
 				changes = false;
 			}
 		});
 
 		exitb.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				saveOption();
 				System.exit(0);
 			}
 		});
 
 		box.add(newdb);
 		box.add(saveb);
 		box.add(loadb);
 		box.add(exitb);
 
 		return box;
 	}
 
 	/**
 	 * Building the card information pane
 	 * 
 	 * @param c
 	 *            Display the information of the selected Card c
 	 */
 	private Box buildCardInfo(Card c) {
 		Box splitter = Box.createHorizontalBox();
 
 		JEditorPane link = new JEditorPane(
 				"text/html",
 				"Special thanks to <a href = 'http://littleakiba.com/tcg/weiss-schwarz'>littleakiba</a> for the translations.");
 		link.setEditable(false);
 		link.setOpaque(true);
 		link.addHyperlinkListener(new HyperlinkListener() {
 			public void hyperlinkUpdate(HyperlinkEvent hle) {
 				if (HyperlinkEvent.EventType.ACTIVATED.equals(hle
 						.getEventType())) {
 					Desktop desktop = Desktop.getDesktop();
 					try {
 						desktop.browse(new URI(
 								"http://littleakiba.com/tcg/weiss-schwarz"));
 					} catch (IOException e) {
 						e.printStackTrace();
 					} catch (URISyntaxException e) {
 						e.printStackTrace();
 					}
 				}
 			}
 		});
 
 		Box optionBox = buildOption();
 
 		int widthM = getWidth() / 2 - OFFSET;
 		int heightM = listBox.getPreferredSize().height - link.getHeight()
 				- optionBox.getHeight();
 		heightM = 250;
 
 		if (getPreferredSize().width / 2 - OFFSET > widthM)
 			widthM = getPreferredSize().width / 2 - OFFSET;
 		else
 			widthM = getWidth() / 2 - OFFSET;
 
 		splitter.setPreferredSize(new Dimension(widthM, heightM));
 		splitter.setMaximumSize(new Dimension(widthM, heightM));
 		// splitter2.setPreferredSize(new Dimension(widthM, 300));
 		// splitter2.setMaximumSize(new Dimension(widthM, 300));
 
 		if (c == null) {
 		} else {
 			splitter.add(c.displayImage(
 					(int) (splitter.getPreferredSize().width * 0.22),
 					splitter.getPreferredSize().height));
 			splitter.add(c.getInfoPane(
 					(int) (splitter.getPreferredSize().width * 0.78),
 					splitter.getPreferredSize().height));
 			// splitter2.add(splitter);
 
 		}
 
 		Box splitter3 = Box.createVerticalBox();
 
 		splitter3.add(splitter);
 		splitter3.add(link);
 		splitter3.add(optionBox);
 		
 		splitter3.revalidate();
 
 		// cardInfo.add(splitter3);
 
 		// System.out.println("splitter has components: " +
 		// splitter.getComponentCount());
 
 		return splitter3;
 	}
 
 	/**
 	 * Building search result list
 	 * 
 	 * @return JScrollPane populated with information of the cards in the result
 	 *         list
 	 */
 	private JScrollPane buildResultList() {
 
 		resultListModel = new ResultListTableModel(resultList);
 		resultListTable = new JTable(resultListModel) {
 
 			private static final long serialVersionUID = 3570425890676389430L;
 
 			public boolean isCellEditable(int rowIndex, int colIndex) {
 				return false;
 			}
 			
 			public TableCellRenderer getCellRenderer(int row, int column) {
 				if (column == 0) return cardIDRenderer;
 				else if (column >= 4) return numberRenderer;
 				else return super.getCellRenderer(row, column);
 			}
 		};
 
 		// Allocate column widths
 		int widthM = getPreferredSize().width / 2 - OFFSET;
 		if (getWidth() / 2 - OFFSET > widthM)
 			widthM = getWidth() / 2 - OFFSET;
 		else
 			widthM = getPreferredSize().width / 2 - OFFSET;
 
 		/*
 		 * resultListTable.setPreferredScrollableViewportSize(new Dimension(
 		 * widthM, 70));
 		 */
 		resultListTable.setFillsViewportHeight(true);
 		// resultListTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 		resultListTable.setRowSorter(new TableRowSorter<TableModel>(
 				resultListTable.getModel()));
 
 		// Handles right click selection
 		resultListTable.addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent e) {
 				// Left mouse click
 				if (SwingUtilities.isLeftMouseButton(e)) {
 					// Do something
 				}
 				// Right mouse click
 				else if (SwingUtilities.isRightMouseButton(e)) {
 					// Get the coordinates of the mouse click
 					Point p = e.getPoint();
 
 					// Get the row index that contains that coordinate
 					int rowNumber = resultListTable.rowAtPoint(p);
 
 					// Get the ListSelectionModel of the JTable
 					ListSelectionModel model = resultListTable
 							.getSelectionModel();
 
 					// Set the selected interval of rows. Using the "rowNumber"
 					// variable for the beginning and end selects only that one
 					// row.
 					model.setSelectionInterval(rowNumber, rowNumber);
 
 					int row = resultListTable.getSelectedRow();
 					if (row > -1) {
 						selectedCard = cardHolder.get(resultListTable
 								.getValueAt(row, 0));
 						// selectedCard = allCards.get(row);
 					}
 					refresh("listBox");
 				}
 
 			}
 		});
 
 		// Handles click action
 		resultListTable.addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent e) {
 				int row = resultListTable.getSelectedRow();
 				if (row > -1) {
 					selectedCard = cardHolder.get(resultListTable.getValueAt(
 							row, 0));
 					// selectedCard = allCards.get(row);
 				}
 				if (e.getClickCount() == 1
 						&& e.getButton() == MouseEvent.BUTTON1) {
 					refresh("listBox");
 				} /*
 				 * else if ((e.getClickCount() == 2 && e.getButton() ==
 				 * MouseEvent.BUTTON1) || (e.getClickCount() == 1 &&
 				 * e.getButton() == MouseEvent.BUTTON3) && row > -1) {
 				 * 
 				 * currentDeck.addCard(selectedCard); refresh("listBox"); }
 				 */
 			}
 
 			public void mouseReleased(MouseEvent e) {
 				int row = resultListTable.getSelectedRow();
 				if (row > -1) {
 					selectedCard = cardHolder.get(resultListTable.getValueAt(
 							row, 0));
 					// selectedCard = allCards.get(row);
 				}
 				if ((e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
 						|| (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3)
 						&& row > -1) {
 					if (currentDeck.addCard(selectedCard, true))
 						refresh("addToDeck");
 				}
 			}
 		});
 
 		// Handles keyboard inputs
 		resultListTable.addKeyListener(new KeyListener() {
 
 			@Override
 			public void keyPressed(KeyEvent e) {
 				if (e.getKeyCode() == KeyEvent.VK_ENTER) {
 					e.consume();
 				}
 			}
 
 			@Override
 			public void keyReleased(KeyEvent e) {
 				int row = resultListTable.getSelectedRow();
 				if (row > -1) {
 					selectedCard = cardHolder.get(resultListTable.getValueAt(
 							row, 0));
 				}
 				if (e.getKeyCode() == KeyEvent.VK_DELETE
 						|| e.getKeyCode() == KeyEvent.VK_MINUS) {
 					if (currentDeck.removeCard(selectedCard))
 						refresh("removeFromDeck");
 				} else if (e.getKeyCode() == KeyEvent.VK_EQUALS
 						|| e.getKeyCode() == KeyEvent.VK_ADD
 						|| e.getKeyCode() == KeyEvent.VK_ENTER) {
 					if (currentDeck.addCard(selectedCard, true))
 						refresh("addToDeck");
 				} else if (e.getKeyCode() == KeyEvent.VK_UP
 						|| e.getKeyCode() == KeyEvent.VK_DOWN) {
 					refresh("listBox");
 				}
 			}
 
 			@Override
 			public void keyTyped(KeyEvent arg0) {
 
 			}
 
 		});
 
 		TableColumn indCol = resultListTable.getColumnModel().getColumn(0);
 		indCol.setPreferredWidth(110);
 		TableColumn namCol = resultListTable.getColumnModel().getColumn(1);
 		namCol.setPreferredWidth(widthM - 110 - 55 - 60 - 35 - 35 - 35 - 50);
 		TableColumn colCol = resultListTable.getColumnModel().getColumn(2);
 		colCol.setPreferredWidth(55);
 		TableColumn typCol = resultListTable.getColumnModel().getColumn(3);
 		typCol.setPreferredWidth(60);
 		TableColumn lvlCol = resultListTable.getColumnModel().getColumn(4);
 		lvlCol.setPreferredWidth(35);
 		TableColumn cstCol = resultListTable.getColumnModel().getColumn(5);
 		cstCol.setPreferredWidth(35);
 		TableColumn solCol = resultListTable.getColumnModel().getColumn(6);
 		solCol.setPreferredWidth(35);
 		TableColumn powCol = resultListTable.getColumnModel().getColumn(7);
 		powCol.setPreferredWidth(50);
 
 		resultListTable.addFocusListener(new FocusListener() {
 			@Override
 			public void focusGained(FocusEvent arg0) {
 			}
 
 			@Override
 			public void focusLost(FocusEvent e) {
 				previousFocus = resultListTable;
 			}
 
 		});
 
 		resultPane = new JScrollPane(resultListTable);
 		resultPane
 				.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 
 		return resultPane;
 	}
 
 	private void refreshResultList() {
 		resultListModel.setCardList(resultList);
 		resultListTable.scrollRectToVisible(resultListTable.getCellRect(0, 0,
 				true));
 
 	}
 
 	/**
 	 * Building search result thumbnail
 	 * 
 	 * @param listPane
 	 *            The ResultList JScrollPane
 	 * @return JScrollPane populated with thumbnail of the cards in the result
 	 *         list
 	 */
 	private JScrollPane buildResultThumbPane(JScrollPane listPane) {
 		JPanel panel = new JPanel();
 		Box box = Box.createHorizontalBox();
 		box.setAlignmentX(Box.LEFT_ALIGNMENT);
 		Box vbox = Box.createVerticalBox();
 		vbox.setAlignmentX(Box.LEFT_ALIGNMENT);
 
 		if (resultList.size() > MAXIMUMRESULTSHOWN) {
 
 		} else {
 
 			for (int i = 0; i < resultList.size(); i++) {
 				if (i % RESULTPERLINE == 0 && i > 0) {
 					vbox.add(box);
 					box = Box.createHorizontalBox();
 					box.setAlignmentX(Box.LEFT_ALIGNMENT);
 				}
 				final Card thisCard = resultList.get(i);
 				JLabel tempLab = thisCard.initiateImage();
 				MouseListener listener = new MouseAdapter() {
 					public void mouseReleased(MouseEvent e) {
 						// JComponent comp = (JComponent) e.getSource();
 						// TransferHandler handler = comp.getTransferHandler();
 						// handler.exportAsDrag(comp, e, TransferHandler.COPY);
 
 						selectedCard = thisCard;
 						System.out.println("selected "
 								+ selectedCard.getCardName());
 						refresh("listBox2");
 
 						if ((e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
 								|| (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3)) {
 							selectedCard = thisCard;
 							if (currentDeck.addCard(selectedCard, true))
 								refresh("addToDeck");
 						}
 					}
 				};
 
 				tempLab.addMouseListener(listener);
 				box.add(tempLab);
 
 			}
 			vbox.add(box);
 
 			panel.add(vbox);
 		}
 		JScrollPane jsp = new JScrollPane(panel);
 		jsp.setPreferredSize(new Dimension(listPane.getPreferredSize()));
 		jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 
 		return jsp;
 	}
 
 	/**
 	 * Building the search result viewer area
 	 * 
 	 * @return JTabbedPane with different tabs for thumbnail and list views of
 	 *         the result list
 	 */
 	private JTabbedPane buildResultArea() {
 
 		resultPane = buildResultList();
 		// resultThumbPane = buildResultThumbPane(resultPane);
 		resultArea = new JTabbedPane();
 		resultArea.add("List View", resultPane);
 		resultArea.add("Thumbnail View", resultThumbPane);
 		resultArea.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent e) {
 				if (resultArea.getSelectedIndex() == resultArea
 						.indexOfComponent(resultThumbPane)) {
 					// resultThumbPane = buildResultThumbPane(resultPane);
 					refreshResultArea();
 				}
 			}
 		});
 		return resultArea;
 	}
 
 	private void refreshResultArea() {
 		int resultThumbIndex = resultArea.indexOfComponent(resultThumbPane);
 		resultHeader.setText("Result count: " + resultList.size());
 		refreshResultList();
 		resultThumbPane = buildResultThumbPane(resultPane);
 		resultArea.setComponentAt(resultThumbIndex, resultThumbPane);
 	}
 
 	private Box buildAddRemoveButtonBox() {
 		Box box = Box.createHorizontalBox();
 
 		JButton plusOne = new JButton("+1");
 		JButton plusFour = new JButton("+4");
 		JButton minusOne = new JButton("-1");
 		JButton minusFour = new JButton("-4");
 
 		plusOne.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (selectedCard != null
 						&& currentDeck.addCard(selectedCard, true)) {
 					refresh("addToDeck");
 				}
 				previousFocus.requestFocus();
 			}
 		});
 
 		plusFour.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (selectedCard != null) {
 					currentDeck.addCard(selectedCard, true);
 					for (int i = 1; i < 4
 							&& currentDeck.addCard(selectedCard, false); ++i)
 						;
 					refresh("addToDeck");
 				}
 				previousFocus.requestFocus();
 			}
 		});
 
 		minusOne.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (selectedCard != null
 						&& currentDeck.removeCard(selectedCard)) {
 					refresh("removeFromDeck");
 				}
 				previousFocus.requestFocus();
 			}
 		});
 
 		minusFour.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (selectedCard != null) {
 					for (int i = 0; i < 4
 							&& currentDeck.removeCard(selectedCard); ++i)
 						;
 					refresh("removeFromDeck");
 				}
 				previousFocus.requestFocus();
 			}
 		});
 
 		box.add(Box.createHorizontalGlue());
 		box.add(plusOne);
 		box.add(Box.createRigidArea(new Dimension(20, 0)));
 		box.add(plusFour);
 		box.add(Box.createRigidArea(new Dimension(20, 0)));
 		box.add(minusOne);
 		box.add(Box.createRigidArea(new Dimension(20, 0)));
 		box.add(minusFour);
 		box.add(Box.createHorizontalGlue());
 		box.setPreferredSize(new Dimension(300, 50));
 		return box;
 	}
 
 	/**
 	 * Building the deck list pane
 	 * 
 	 * @return JScrollPane populated with information of the cards in the deck
 	 */
 	private JScrollPane buildDeckList() {
 
 		deckListModel = new DeckListTableModel(currentDeck.getUnique());
 		deckListTable = new JTable(deckListModel) {
 
 			private static final long serialVersionUID = 3570425890676389430L;
 
 			public boolean isCellEditable(int rowIndex, int colIndex) {
 				return false;
 			}
 			public TableCellRenderer getCellRenderer(int row, int column) {
 				switch(column) {
 					case 1:
 						return cardIDRenderer;
 					case 5:
 					case 6:
 					case 8:
 					case 9:
 						return numberRenderer;
 					default:
 						return super.getCellRenderer(row, column);
 				}
 			}
 			
 		};
 
 		deckListTable
 				.setPreferredScrollableViewportSize(new Dimension(600, 175));
 		deckListTable.setFillsViewportHeight(true);
 		deckListTable.setRowSorter(new TableRowSorter<TableModel>(deckListTable
 				.getModel()));
 
 		// Handles right click selection
 		deckListTable.addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent e) {
 				// Left mouse click
 				if (SwingUtilities.isLeftMouseButton(e)) {
 					// Do something
 				}
 				// Right mouse click
 				else if (SwingUtilities.isRightMouseButton(e)) {
 					// get the coordinates of the mouse click
 					Point p = e.getPoint();
 
 					// get the row index that contains that coordinate
 					int rowNumber = deckListTable.rowAtPoint(p);
 
 					// Get the ListSelectionModel of the JTable
 					ListSelectionModel model = deckListTable
 							.getSelectionModel();
 
 					// Set the selected interval of rows. Using the "rowNumber"
 					// variable for the beginning and end selects only that one
 					// row.
 					model.setSelectionInterval(rowNumber, rowNumber);
 
 					int row = deckListTable.getSelectedRow();
 					if (row > -1) {
 						selectedCard = cardHolder.get(deckListTable.getValueAt(
 								row, 1));
 						// selectedCard = allCards.get(row);
 					}
 					refresh("deckListSelect");
 				}
 
 			}
 		});
 
 		// Handles click action
 		deckListTable.addMouseListener(new MouseAdapter() {
 			public void mousePressed(MouseEvent e) {
 				int row = deckListTable.getSelectedRow();
 				if (row > -1)
 					selectedCard = cardHolder.get(deckListTable.getValueAt(row,
 							1));
 				if (e.getClickCount() == 1
 						&& e.getButton() == MouseEvent.BUTTON1) {
 					refresh("deckListSelect");
 				} /*
 				 * else if ((e.getClickCount() == 2 && e.getButton() ==
 				 * MouseEvent.BUTTON1) || (e.getClickCount() == 1 &&
 				 * e.getButton() == MouseEvent.BUTTON3) && row > -1) {
 				 * currentDeck.removeCard(selectedCard); refresh("deckList2"); }
 				 */
 			}
 
 			public void mouseReleased(MouseEvent e) {
 				int row = deckListTable.getSelectedRow();
 				if (row > -1)
 					selectedCard = cardHolder.get(deckListTable.getValueAt(row,
 							1));
 				if ((e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
 						|| (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3)
 						&& row > -1) {
 					if (currentDeck.removeCard(selectedCard))
 						refresh("removeFromDeck");
 				}
 			}
 		});
 
 		deckListTable.addFocusListener(new FocusListener() {
 
 			@Override
 			public void focusGained(FocusEvent f) {
 			}
 
 			@Override
 			public void focusLost(FocusEvent f) {
 			}
 		});
 
 		// Keyboard controls to operate the builder
 		deckListTable.addKeyListener(new KeyListener() {
 
 			@Override
 			public void keyPressed(KeyEvent e) {
 
 			}
 
 			/*
 			 * Key Explanations DELETE remove a card - remove a card = add a
 			 * card + add a card ENTER add a card
 			 * 
 			 * @see
 			 * java.awt.event.KeyListener#keyReleased(java.awt.event.KeyEvent)
 			 */
 			@Override
 			public void keyReleased(KeyEvent e) {
 				int row = deckListTable.getSelectedRow();
 				if (row > -1)
 					selectedCard = cardHolder.get(deckListTable.getValueAt(row,
 							1));
 				if (e.getKeyCode() == KeyEvent.VK_UP
 						|| e.getKeyCode() == KeyEvent.VK_DOWN)
 					refresh("deckListSelect");
 				if (e.getKeyCode() == KeyEvent.VK_DELETE
 						|| e.getKeyCode() == KeyEvent.VK_MINUS) {
 					if (currentDeck.removeCard(selectedCard))
 						refresh("removeFromDeck");
 				} else if (e.getKeyCode() == KeyEvent.VK_EQUALS
 						|| e.getKeyCode() == KeyEvent.VK_ADD
 						|| e.getKeyCode() == KeyEvent.VK_ENTER) {
 					if (currentDeck.addCard(selectedCard, true))
 						refresh("addToDeck");
 				}
 
 			}
 
 			@Override
 			public void keyTyped(KeyEvent e) {
 
 			}
 
 		});
 
 		// allocating size for columns
 		int widthM = getPreferredSize().width;
 
 		if (getWidth() / 2 - OFFSET > widthM)
 			widthM = getWidth();
 		else
 			widthM = getPreferredSize().width;
 
 		int cntW = 40;
 		int indW = 130;
 		int colW = 75;
 		int typW = 80;
 		int lvlW = 25;
 		int cstW = 25;
 		int trgW = 100;
 		int solW = 25;
 		int powW = 60;
 
 		int usedSpace = cntW + indW + colW + typW + lvlW + cstW + trgW + solW
 				+ powW;
 
 		TableColumn cntCol = deckListTable.getColumnModel().getColumn(0);
 		cntCol.setPreferredWidth(cntW);
 		TableColumn indCol = deckListTable.getColumnModel().getColumn(1);
 		indCol.setPreferredWidth(indW);
 		TableColumn namCol = deckListTable.getColumnModel().getColumn(2);
 		namCol.setPreferredWidth(widthM - usedSpace);
 		TableColumn colCol = deckListTable.getColumnModel().getColumn(3);
 		colCol.setPreferredWidth(colW);
 		TableColumn typCol = deckListTable.getColumnModel().getColumn(4);
 		typCol.setPreferredWidth(typW);
 		TableColumn lvlCol = deckListTable.getColumnModel().getColumn(5);
 		lvlCol.setPreferredWidth(lvlW);
 		TableColumn cstCol = deckListTable.getColumnModel().getColumn(6);
 		cstCol.setPreferredWidth(cstW);
 		TableColumn trgCol = deckListTable.getColumnModel().getColumn(7);
 		trgCol.setPreferredWidth(trgW);
 		TableColumn solCol = deckListTable.getColumnModel().getColumn(8);
 		solCol.setPreferredWidth(solW);
 		TableColumn powCol = deckListTable.getColumnModel().getColumn(9);
 		powCol.setPreferredWidth(powW);
 
 		deckListTable.addFocusListener(new FocusListener() {
 			@Override
 			public void focusGained(FocusEvent arg0) {
 			}
 
 			@Override
 			public void focusLost(FocusEvent e) {
 				previousFocus = deckListTable;
 			}
 
 		});
 
 		deckPane = new JScrollPane(deckListTable);
 		deckPane.setVerticalScrollBarPolicy(JScrollPane.VERTICAL_SCROLLBAR_ALWAYS);
 
 		return deckPane;
 	}
 
 	private void refreshDeckList() {
 		deckListModel.setDeckList(currentDeck.getUnique());
 		int i;
 		for (i = 0; i < deckListTable.getRowCount(); ++i) {
 			if (cardHolder.get(deckListTable.getValueAt(i, 1)).equals(
 					selectedCard))
 				break;
 		}
 		if (i < deckListTable.getRowCount()) {
 			deckListTable.getSelectionModel().setSelectionInterval(i, i);
 			deckListTable.scrollRectToVisible(deckListTable.getCellRect(i, 0,
 					true));
 		}
 	}
 
 	private void refreshStats() {
 		if (currentDeck.getNumClimax() < 8)
 			climaxCountText.setForeground(Color.RED);
 		else
 			climaxCountText.setForeground(Color.BLACK);
 
 		if (currentDeck.getNumLevel0() < 14 || currentDeck.getNumLevel0() > 18)
 			lv0CountText.setForeground(Color.RED);
 		else
 			lv0CountText.setForeground(Color.BLACK);
 
 		if (currentDeck.getNumLevel1() < 10 || currentDeck.getNumLevel1() > 14)
 			lv1CountText.setForeground(Color.RED);
 		else
 			lv1CountText.setForeground(Color.BLACK);
 
 		if (currentDeck.getNumLevel2() < 8 || currentDeck.getNumLevel2() > 12)
 			lv2CountText.setForeground(Color.RED);
 		else
 			lv2CountText.setForeground(Color.BLACK);
 
 		if (currentDeck.getNumLevel3() < 4 || currentDeck.getNumLevel3() > 8)
 			lv3CountText.setForeground(Color.RED);
 		else
 			lv3CountText.setForeground(Color.BLACK);
 
 		cardCountText.setText(String.valueOf(currentDeck.getCards().size()));
 		climaxCountText.setText(String.valueOf(currentDeck.getNumClimax()));
 		lv0CountText.setText(String.valueOf(currentDeck.getNumLevel0()));
 		lv1CountText.setText(String.valueOf(currentDeck.getNumLevel1()));
 		lv2CountText.setText(String.valueOf(currentDeck.getNumLevel2()));
 		lv3CountText.setText(String.valueOf(currentDeck.getNumLevel3()));
 		soulCountText.setText(String.valueOf(currentDeck.getNumSoul()));
 
 		eventCountText.setText(String.valueOf(currentDeck.getNumEvent()));
 		yellowCountText.setText(String.valueOf(currentDeck.getNumYellow()));
 		greenCountText.setText(String.valueOf(currentDeck.getNumGreen()));
 		redCountText.setText(String.valueOf(currentDeck.getNumRed()));
 		blueCountText.setText(String.valueOf(currentDeck.getNumBlue()));
 	}
 
 	/**
 	 * Building deck statistic analyzer
 	 * 
 	 * @return Box with the statistical information of the deck displayed
 	 */
 	private Box buildStatsZone() {
 		Box leftPanel = Box.createVerticalBox();
 		Box analyzerBox = Box.createHorizontalBox();
 
 		// 16 Lv0
 		// 12-14 Lv1
 		// 6-8 Lv2
 		// 4-6 Lv3
 		// 8 CX
 
 		Box newVert = Box.createVerticalBox();
 		newVert.add(new JLabel("Card count: "));
 		newVert.add(new JLabel("Climax: "));
 		newVert.add(new JLabel("Level 0: "));
 		newVert.add(new JLabel("Level 1: "));
 		newVert.add(new JLabel("Level 2: "));
 		newVert.add(new JLabel("Level 3: "));
 		newVert.add(new JLabel("Soul Triggers: "));
 		Box newVert2 = Box.createVerticalBox();
 
 		cardCountText = new JLabel(
 				String.valueOf(currentDeck.getCards().size()));
 		climaxCountText = new JLabel(String.valueOf(currentDeck.getNumClimax()));
 		lv0CountText = new JLabel(String.valueOf(currentDeck.getNumLevel0()));
 		lv1CountText = new JLabel(String.valueOf(currentDeck.getNumLevel1()));
 		lv2CountText = new JLabel(String.valueOf(currentDeck.getNumLevel2()));
 		lv3CountText = new JLabel(String.valueOf(currentDeck.getNumLevel3()));
 
 		soulCountText = new JLabel(String.valueOf(currentDeck.getNumSoul()));
 
 		newVert2.add(cardCountText);
 		newVert2.add(climaxCountText);
 		newVert2.add(lv0CountText);
 		newVert2.add(lv1CountText);
 		newVert2.add(lv2CountText);
 		newVert2.add(lv3CountText);
 		newVert2.add(soulCountText);
 		newVert2.setPreferredSize(new Dimension(20, 100));
 
 		Box newVert3 = Box.createVerticalBox();
 		newVert3.add(new JLabel("Event: "));
 		newVert3.add(new JLabel("Yellow: "));
 		newVert3.add(new JLabel("Green: "));
 		newVert3.add(new JLabel("Red: "));
 		newVert3.add(new JLabel("Blue: "));
 		// newVert3.add(new JLabel("------"));
 		// newVert3.add(new JLabel("Damage: "));
 		Box newVert4 = Box.createVerticalBox();
 
 		eventCountText = new JLabel(String.valueOf(currentDeck.getNumEvent()));
 		yellowCountText = new JLabel(String.valueOf(currentDeck.getNumYellow()));
 		greenCountText = new JLabel(String.valueOf(currentDeck.getNumGreen()));
 		redCountText = new JLabel(String.valueOf(currentDeck.getNumRed()));
 		blueCountText = new JLabel(String.valueOf(currentDeck.getNumBlue()));
 
 		newVert4.add(eventCountText);
 		newVert4.add(yellowCountText);
 		newVert4.add(greenCountText);
 		newVert4.add(redCountText);
 		newVert4.add(blueCountText);
 		// newVert4.add(new JLabel("------"));
 		// newVert4.add(new JLabel(String.valueOf(currentDeck.getDamage())));
 		newVert4.setPreferredSize(new Dimension(20, 100));
 
 		analyzerBox.add(newVert);
 		// analyzerBox.add(Box.createHorizontalGlue());
 		analyzerBox.add(Box.createRigidArea(new Dimension(5, 0)));
 		analyzerBox.add(newVert2);
 		analyzerBox.add(Box.createHorizontalGlue());
 		analyzerBox.add(newVert3);
 		analyzerBox.add(Box.createRigidArea(new Dimension(5, 0)));
 		// analyzerBox.add(Box.createHorizontalGlue());
 		analyzerBox.add(newVert4);
 		// analyzerBox.add(Box.createHorizontalStrut(5));
 		analyzerBox.setPreferredSize(new Dimension(80, 100));
 
 		JButton clearDeck = new JButton("Clear Deck");
 		clearDeck.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if (changes) {
 					saveOption();
 				}
 				file = null;
 				currentDeck = new Deck();
 
 				refresh("deckList2");
 			}
 		});
 
 		leftPanel.add(analyzerBox);
 		Box boxtemp = Box.createHorizontalBox();
 		boxtemp.add(Box.createHorizontalGlue());
 		boxtemp.add(clearDeck);
 		leftPanel.add(boxtemp);
 		leftPanel.setAlignmentX(CENTER_ALIGNMENT);
 		leftPanel.setPreferredSize(new Dimension(60, 100));
 		return leftPanel;
 	}
 
 	/**
 	 * Building deck thumbnail
 	 * 
 	 * @param deckListPane
 	 *            The ResultList JScrollPane
 	 * @return JScrollPane populated with thumbnail of the cards in the deck
 	 */
 	private JScrollPane buildDeckThumbPane(JScrollPane deckListPane) {
 
 		JPanel panel = new JPanel();
 		Box box = Box.createHorizontalBox();
 		box.setAlignmentX(Box.LEFT_ALIGNMENT);
 		Box vbox = Box.createVerticalBox();
 		vbox.setAlignmentX(Box.LEFT_ALIGNMENT);
 
 		for (int i = 0; i < currentDeck.getCards().size(); i++) {
 			if (i % DECKPERLINE == 0 && i > 0) {
 				vbox.add(box);
 				box = Box.createHorizontalBox();
 				box.setAlignmentX(Box.LEFT_ALIGNMENT);
 			}
 			final Card thisCard = currentDeck.getCards().get(i);
 			JLabel tempLab = thisCard.initiateImage();
 			MouseListener listener = new MouseAdapter() {
 				public void mouseReleased(MouseEvent e) {
 					selectedCard = thisCard;
 					System.out.println(thisCard.getCardName() + " has "
 							+ thisCard.getCardCount() + " copies");
 					refresh("deckListSelect");
 
 					if ((e.getClickCount() == 2 && e.getButton() == MouseEvent.BUTTON1)
 							|| (e.getClickCount() == 1 && e.getButton() == MouseEvent.BUTTON3)) {
 						currentDeck.removeCard(selectedCard);
 						refresh("removeFromDeck");
 					}
 				}
 			};
 
 			tempLab.addMouseListener(listener);
 			box.add(tempLab);
 
 		}
 		vbox.add(box);
 		panel.add(vbox);
 
 		JScrollPane jsp = new JScrollPane(panel);
 		jsp.setPreferredSize(new Dimension(deckListPane.getPreferredSize()));
 		jsp.setVerticalScrollBarPolicy(ScrollPaneConstants.VERTICAL_SCROLLBAR_ALWAYS);
 
 		return jsp;
 	}
 
 	/**
 	 * Building deck viewer area
 	 * 
 	 * @return JTabbedPane with different tabs for thumbnail and list views of
 	 *         the deck
 	 */
 	private JTabbedPane buildDeckArea() {
 		deckArea = new JTabbedPane();
 
 		deckPane = buildDeckList();
 		deckThumbPane = buildDeckThumbPane(deckPane);
 		deckArea = new JTabbedPane();
 		deckArea.addTab("List View", deckPane);
 		deckArea.addTab("Thumbnail View", deckThumbPane);
 
 		return deckArea;
 	}
 
 	private void refreshDeckArea() {
 		int resultThumbIndex = deckArea.indexOfComponent(deckThumbPane);
 		refreshDeckList();
 		deckThumbPane = buildDeckThumbPane(deckPane);
 		deckArea.setComponentAt(resultThumbIndex, deckThumbPane);
 	}
 
 	// ///////////////////////
 	//
 	// Public Methods
 	//
 	// //////////////////////
 
 	/**
 	 * Initialize the client
 	 */
 	public void init() {
 		numberRenderer = new NumberCellRenderer();
 		cardIDRenderer = new CardIDCellRenderer();
 		buildUI();
 		// pack();
 	}
 
 	/**
 	 * Action events that are necessary
 	 * 
 	 * @param e
 	 *            ActionEvent e
 	 */
 	public void action(ActionEvent e) {
 		fc.setCurrentDirectory(new File("Deck"));
 
 		if (e.getSource() == loadb) {
 			// saveOption();
 			int returnVal = fc.showOpenDialog(this.getParent());
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				file = fc.getSelectedFile();
 				currentDeck = new Deck();
 				currentDeck.loadRaw(file, cardHolder);
 				refresh("load");
 			}
 		} else if (e.getSource() == saveb) {
 			File directoryMaker = new File("Deck");
 			if (!directoryMaker.exists())
 				directoryMaker.mkdir();
 			fc.setCurrentDirectory(directoryMaker);
 			int returnVal = fc.showSaveDialog(this.getParent());
 			if (returnVal == JFileChooser.APPROVE_OPTION) {
 				// if (file == null)
 				file = fc.getSelectedFile();
 				if (file.exists()) {
 					// System.err.println(file.getCardName());
 				} else {
 
 					file = new File(directoryMaker, file.getName());
 					System.out.println(file.getName() + " was created");
 				}
 				currentDeck.saveRaw(file);
 			}
 		}
 	}
 
 	/**
 	 * Refresh view
 	 * 
 	 * @param source
 	 *            The string representation of the source called refresh
 	 */
 	public void refresh(String source) {
 		// getContentPane().removeAll();
 		// validate();
 
 		if (source.equalsIgnoreCase("load")
 				|| source.equalsIgnoreCase("listBox")
 				|| source.equalsIgnoreCase("deckListSelect")
 				|| source.equalsIgnoreCase("search")
 				|| source.equalsIgnoreCase("new")
 				|| source.equalsIgnoreCase("listBox2")) {
 			cardInfo.removeAll();
 			cardInfo.revalidate();
 			cardInfo.add(buildCardInfo(selectedCard));
 		}
 
 		if (source.equalsIgnoreCase("search") || source.equalsIgnoreCase("new")) {
 			refreshResultArea();
 		}
 
 		if (source.equalsIgnoreCase("load")) {
 			changes = true;
 		}
 
 		if (source.equalsIgnoreCase("addToDeck")
 				|| source.equalsIgnoreCase("removeFromDeck")
 				|| source.equalsIgnoreCase("deckList2")
 				|| source.equalsIgnoreCase("load")
 				|| source.equalsIgnoreCase("new")) {
 			refreshStats();
 			refreshDeckArea();
 			// changes = true;
 		}
 
 		// BorderLayout layout = new BorderLayout();
 		// getContentPane().setLayout(layout);
 		//
 		// add(BorderLayout.NORTH, searchBox);
 		// add(BorderLayout.WEST, cardInfo);
 		// add(BorderLayout.EAST, listBox);
 		// add(BorderLayout.SOUTH, deckList);
 		// setJMenuBar(menu);
 
 		// pack();
 		// System.out.println(getWidth() + " * " + getHeight());
 
 		resizeSearchBox(searchBox);
 		resizeSearchBox(cardInfo);
 		// getContentPane().setVisible(true);
 	}
 
 	/**
 	 * Prompt for save
 	 */
 	private void saveOption() {
 		if (changes) {
 			if (file == null) {
 				int returnVal = fc.showSaveDialog(this);
 				if (returnVal == JFileChooser.APPROVE_OPTION) {
 					file = fc.getSelectedFile();
 				}
 			}
 
 			if (file != null) {
 				currentDeck.saveRaw(file);
 				currentDeck.save(file);
 			}
 		}
 	}
 
 	/**
 	 * Resize the search box components
 	 */
 	private void resizeSearchBox(JComponent comp) {
 		for (Component jc : comp.getComponents()) {
 			((JComponent) jc).putClientProperty("JComponent.sizeVariant",
 					"mini");
 		}
 	}
 
 	/**
 	 * Build the internal elements of the UI
 	 */
 	public void buildUI() {
 		buildSearchBox();
 		resizeSearchBox(searchBox);
 		// buildMenu();
 
 		resultHeader = new JLabel("Result count: " + resultList.size());
 		Box headerBox = Box.createHorizontalBox();
 		headerBox.add(Box.createRigidArea(new Dimension(10, 0)));
 		headerBox.add(resultHeader);
 		headerBox.add(Box.createHorizontalGlue());
 
 		listBox.add(headerBox);
 		listBox.add(buildResultArea());
 		listBox.add(buildAddRemoveButtonBox());
 
 		cardInfo.add(buildCardInfo(selectedCard));
 		resizeSearchBox(cardInfo);
 
 		deckList.add(buildStatsZone());
 		deckList.add(buildDeckArea());
 
 		setJMenuBar(menu);
 
 		BorderLayout layout = new BorderLayout();
 		getContentPane().setLayout(layout);
 
 		add(BorderLayout.NORTH, searchBox);
 		add(BorderLayout.WEST, cardInfo);
 		add(BorderLayout.EAST, listBox);
 		add(BorderLayout.SOUTH, deckList);
 	}
 
 	/**
 	 * used by Player.java to load a selected deck
 	 * 
 	 * @param selectedDeck
 	 *            Name of the selected deck
 	 */
 	public void loadDefaultDeck(String selectedDeck) {
 		currentDeck.load(new File("Deck/" + selectedDeck), cardHolder);
 		refresh("load");
 	}
 
 	/**
 	 * De-serialize the data given
 	 */
 	@SuppressWarnings("unchecked")
 	private void deserializer() {
 
 		// FileInputStream fileInput;
 		InputStream fileInput;
 		ObjectInputStream objectInput;
 
 		try {
 			// fileInput = new FileInputStream();
 			System.out.println("Opening data");
 			fileInput = getClass().getResourceAsStream("/resources/CardDatav2");
 			objectInput = new ObjectInputStream(fileInput);
 
 			completeList = (ArrayList<Card>) objectInput.readObject();
 			resultList = (ArrayList<Card>) completeList.clone();
 			cardHolder = (HashMap<String, Card>) objectInput.readObject();
 
 		} catch (IOException e) {
 			e.printStackTrace();
 		} catch (ClassNotFoundException e) {
 			e.printStackTrace();
 		}
 
 	}
 
 	// main
 	/*public static void main(String[] args) {
 		BuilderGUI builderGui = new BuilderGUI();
 		builderGui.init();
 
 		builderGui.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		builderGui.setLocationRelativeTo(null);
 		builderGui.setVisible(true);
 	}*/
 
 }
