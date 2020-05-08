 package com.tnicoll.apps.bookworm.gui;
 
 import java.awt.BorderLayout;
 import java.awt.FlowLayout;
 import java.awt.GraphicsConfiguration;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.Insets;
 import java.awt.Rectangle;
 import java.awt.Toolkit;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
import java.util.regex.Pattern;
 
 import javax.swing.BorderFactory;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.RowFilter;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableModel;
 import javax.swing.table.TableCellRenderer;
 import javax.swing.table.TableModel;
 import javax.swing.table.TableRowSorter;
 
 import com.tnicoll.apps.bookworm.BookModel;
 
 public class BookPanel extends JPanel{
 
 	/**
 	 * A panel showing all the details for a single book
 	 */
 	private static final long serialVersionUID = -5675560527929949571L;
 	private JPanel menuPanel;
 	private JPanel optionPanel;
 	private JTextField filterBox;
 	private JPanel searchPanel;
 	private JScrollPane wordScroll;
 	private JTable wordTable;
 	private BookModel model;
 	private JTextArea console;
 	private JScrollPane consolePane;
 	private JTextField paragraphCnt;
 	private JTextField wordCnt;
 	private JTextField avgParagraphWordCnt;
 	private JTextField sentenceCnt;
 	private JTextField speechCnt;
 	private JTextField filesize;
 	private JTextField characterCnt;
 	private JTextField spellingErrorCnt;
 	private JTextField lineCnt;
 
 	public JTextField getParagraphCnt() {
 		return paragraphCnt;
 	}
 
 
 
 	public void setParagraphCnt(int paragraphCnt) {
 		this.paragraphCnt.setText(Integer.toString(paragraphCnt));
 	}
 
 	public void setWordCnt(int wordCnt) {
 		this.wordCnt.setText(Integer.toString(wordCnt));
 	}
 	public void setAvgParagraphWordCnt(int avgParagraphWordCnt) {
 		this.avgParagraphWordCnt.setText(Integer.toString(avgParagraphWordCnt));
 	}
 	public void setSentenceCnt(int sentenceCnt) {
 		this.sentenceCnt.setText(Integer.toString(sentenceCnt));
 	}
 	public void setSpeechCnt(int speechCnt) {
 		this.speechCnt.setText(Integer.toString(speechCnt));
 	}
 	public void setFilesize(long filesize) {
 		this.filesize.setText(Long.toString(filesize/1024) + "K");
 	}
 	public void setCharacterCnt(int characterCnt) {
 		this.characterCnt.setText(Integer.toString(characterCnt));
 	}
 	public void setSpellingErrorCnt(int spellingErrorCnt) {
 		this.spellingErrorCnt.setText(Integer.toString(spellingErrorCnt));
 	}
 	public void setLineCnt(int lineCnt) {
 		this.lineCnt.setText(Integer.toString(lineCnt));
 	}
 
 	public BookPanel(int width, int height)
 	{
 		searchPanel = new JPanel();
 		searchPanel.setPreferredSize(new java.awt.Dimension(width/2, height));
 		JLabel filterLabel = new JLabel("Filter: ");
 		filterBox = new JTextField();
 		filterBox.setEditable(true);
 		filterBox.setColumns(50);
 		filterBox.setSize((width/2)-50, 10);
 		searchPanel.add(filterLabel, BorderLayout.NORTH);
 		searchPanel.add(filterBox, BorderLayout.NORTH);
 		add(searchPanel, BorderLayout.WEST);
 
 		///Add Word Table
 		Object []columnNames = {"Word", "Count","Recognised"};
 		Object [][] data = {{"",new Integer(0),new Boolean(false)}};;
 		model = new BookModel(data, columnNames);
 		System.out.println(data[0][0]);
 		wordTable = new JTable(model);
 		wordTable.setRowSelectionAllowed(true);
 		ListSelectionModel cellSelectionModel = wordTable.getSelectionModel();
 		cellSelectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
 		cellSelectionModel.addListSelectionListener(new ListSelectionListener() {
 			public void valueChanged(ListSelectionEvent e) {
 				String selectedData = null;
 
 				int[] selectedRow = wordTable.getSelectedRows();
 
 				selectedData = (String) wordTable.getValueAt(selectedRow[0], 0);
 
 				System.out.println("Selected: " + selectedData);
 			}
 
 		});
 
 
 		wordTable.setAutoCreateRowSorter(true);
 		final TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
 		wordTable.setRowSorter(sorter);
 		filterBox.getDocument().addDocumentListener(new DocumentListener() {
 			@Override
 			public void insertUpdate(DocumentEvent e) {
 				filterText();
 			}
 			@Override
 			public void removeUpdate(DocumentEvent e) {
 				filterText();
 			}
 			@Override
 			public void changedUpdate(DocumentEvent e) {
 				filterText();
 			}
 			public void filterText() {
 				String text = filterBox.getText();
 				if (text.length() == 0) {
 					sorter.setRowFilter(null);
 				} else {
					sorter.setRowFilter(RowFilter.regexFilter("(?i)" + Pattern.quote(text)));
 				}
 			}
 
 		});
 		wordScroll = new JScrollPane(wordTable);
 		wordScroll.setPreferredSize(new java.awt.Dimension(width/2, height-90));
 		searchPanel.add(wordScroll, BorderLayout.SOUTH);
 
 
 		///Add Right Hand Panel
 		menuPanel = new JPanel();
 		add(menuPanel, BorderLayout.EAST);
 		FlowLayout menuPanelLayout = new FlowLayout();
 		menuPanelLayout.setAlignment(FlowLayout.RIGHT);
 		menuPanel.setLayout(menuPanelLayout);
 		menuPanel.setPreferredSize(new java.awt.Dimension(width/2, height));
 
 		//Add Right Hand Top Option Panel
 		optionPanel = new JPanel();
 		optionPanel.setBorder(BorderFactory.createEmptyBorder(30, 30, 30, 30));
 		optionPanel.setPreferredSize(new java.awt.Dimension((width/2)-10, (height/2)) );
 
 		int textbox_x=100, textbox_y=30;
 		JLabel paragraphCntLabel = new JLabel("No of Paragraphs:");
 		paragraphCntLabel.setPreferredSize(new java.awt.Dimension(textbox_x, textbox_y));
 		paragraphCnt = new JTextField(6);
 		paragraphCnt.setPreferredSize(new java.awt.Dimension(50, 30));
 		paragraphCnt.setBounds(textbox_x, textbox_y, 20, 5);
 		optionPanel.add(paragraphCntLabel);
 		optionPanel.add(paragraphCnt);
 		
 		JLabel wordCntLabel = new JLabel("Word Count:");
 		wordCntLabel.setPreferredSize(new java.awt.Dimension(80, 30));
 
 		wordCnt = new JTextField(6);
 		wordCnt.setPreferredSize(new java.awt.Dimension(50, 30));
 		optionPanel.add(wordCntLabel);
 		optionPanel.add(wordCnt);
 		
 		JLabel avgParagraphWordCntLabel = new JLabel("Average Word Count per Paragraph:");
 		avgParagraphWordCntLabel.setPreferredSize(new java.awt.Dimension(180, 30));
 
 		avgParagraphWordCnt = new JTextField(6);
 		avgParagraphWordCnt.setPreferredSize(new java.awt.Dimension(50, 30));
 		optionPanel.add(avgParagraphWordCntLabel);
 		optionPanel.add(avgParagraphWordCnt);
 		
 		JLabel sentenceCntLabel = new JLabel("No of Sentences:");
 		sentenceCntLabel.setPreferredSize(new java.awt.Dimension(130, 30));
 
 		sentenceCnt = new JTextField(6);
 		sentenceCnt.setPreferredSize(new java.awt.Dimension(50, 30));
 		optionPanel.add(sentenceCntLabel);
 		optionPanel.add(sentenceCnt);
 		
 		JLabel speechCntLabel = new JLabel("No of lines of Dialogue:");
 		speechCntLabel.setPreferredSize(new java.awt.Dimension(150, 30));
 
 		speechCnt = new JTextField(6);
 		speechCnt.setPreferredSize(new java.awt.Dimension(50, 30));
 		optionPanel.add(speechCntLabel);
 		optionPanel.add(speechCnt);
 		
 		JLabel filesizeLabel = new JLabel("Filesize:");
 		filesizeLabel.setPreferredSize(new java.awt.Dimension(70, 30));
 
 		filesize = new JTextField(6);
 		filesize.setPreferredSize(new java.awt.Dimension(50, 30));
 		optionPanel.add(filesizeLabel);
 		optionPanel.add(filesize);
 		
 		JLabel characterCntLabel = new JLabel("No of characters:");
 		characterCntLabel.setPreferredSize(new java.awt.Dimension(120, 30));
 
 		characterCnt = new JTextField(6);
 		characterCnt.setPreferredSize(new java.awt.Dimension(50, 30));
 		optionPanel.add(characterCntLabel);
 		optionPanel.add(characterCnt);
 		
 		JLabel spellingErrorCntLabel = new JLabel("No of Spelling Errors:");
 		spellingErrorCntLabel.setPreferredSize(new java.awt.Dimension(150, 30));
 
 		spellingErrorCnt = new JTextField(6);
 		spellingErrorCnt.setPreferredSize(new java.awt.Dimension(50, 30));
 		optionPanel.add(spellingErrorCntLabel);
 		optionPanel.add(spellingErrorCnt);
 		
 		JLabel lineCntLabel = new JLabel("No of lines:");
 		lineCntLabel.setPreferredSize(new java.awt.Dimension(130, 30));
 
 		lineCnt = new JTextField(6);
 		lineCnt.setPreferredSize(new java.awt.Dimension(50, 30));
 		optionPanel.add(lineCntLabel);
 		optionPanel.add(lineCnt);
 		
 		menuPanel.add(optionPanel, BorderLayout.NORTH);
 
 		//Add text console
 		console = new JTextArea();
 		console.setVisible(true);
 		console.setEditable(false);
 		console.setLineWrap(true);
 		console.setWrapStyleWord(true);
 
 		consolePane = new JScrollPane(console);
 		consolePane.setVisible(true);
 		consolePane.setPreferredSize(new java.awt.Dimension((width/2)-20, (height/2)-70) );
 		menuPanel.add(consolePane, BorderLayout.SOUTH);
 		console.setText("");
 	}
 
 
 
 	public JPanel getMenuPanel() {
 		return menuPanel;
 	}
 
 	public void setMenuPanel(JPanel menuPanel) {
 		this.menuPanel = menuPanel;
 	}
 
 	public JPanel getOptionPanel() {
 		return optionPanel;
 	}
 
 	public void setOptionPanel(JPanel optionPanel) {
 		this.optionPanel = optionPanel;
 	}
 
 	public JTextField getFilterBox() {
 		return filterBox;
 	}
 
 	public void setFilterBox(JTextField filterBox) {
 		this.filterBox = filterBox;
 	}
 
 	public JPanel getSearchPanel() {
 		return searchPanel;
 	}
 
 	public void setSearchPanel(JPanel searchPanel) {
 		this.searchPanel = searchPanel;
 	}
 
 	public JScrollPane getWordScroll() {
 		return wordScroll;
 	}
 
 	public void setWordScroll(JScrollPane wordScroll) {
 		this.wordScroll = wordScroll;
 	}
 
 	public JTable getWordTable() {
 		return wordTable;
 	}
 
 	public void setWordTable(JTable wordTable) {
 		this.wordTable = wordTable;
 	}
 
 	public BookModel getModel() {
 		return model;
 	}
 
 	public void setModel(BookModel model) {
 		this.model = model;
 	}
 
 	public JTextArea getConsole() {
 		return console;
 	}
 
 	public void setConsole(JTextArea console) {
 		this.console = console;
 	}
 	public void setConsoleText(String text){
 		this.console.setText(text);
 	}
 
 	public JScrollPane getConsolePane() {
 		return consolePane;
 	}
 
 	public void setConsolePane(JScrollPane consolePane) {
 		this.consolePane = consolePane;
 	}
 
 
 
 	public static long getSerialversionuid() {
 		return serialVersionUID;
 	}
 
 
 }
