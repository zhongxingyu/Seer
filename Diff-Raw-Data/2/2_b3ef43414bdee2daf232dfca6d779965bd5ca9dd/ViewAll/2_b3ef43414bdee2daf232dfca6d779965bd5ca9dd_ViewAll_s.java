 package ehe.insig.ui;
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Component;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.FontMetrics;
 import java.awt.Point;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.FocusAdapter;
 import java.awt.event.FocusEvent;
 import java.awt.event.InputEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 import java.util.ArrayList;
 import java.util.Comparator;
 import java.util.List;
 
 import javax.swing.BoxLayout;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JSplitPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.JToggleButton;
 import javax.swing.ListSelectionModel;
 import javax.swing.RowFilter;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableCellRenderer;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableColumnModel;
 import javax.swing.table.TableRowSorter;
 
 import org.jdesktop.application.Application;
 
 import ehe.insig.dataModel.HeisigItem;
 import ehe.insig.ui.datModel.KanjiTableModel;
 
 /**
  * This code was edited or generated using CloudGarden's Jigloo SWT/Swing GUI
  * Builder, which is free for non-commercial use. If Jigloo is being used
  * commercially (ie, by a corporation, company or business for any purpose
  * whatever) then you should purchase a license for each developer using Jigloo.
  * Please visit www.cloudgarden.com for details. Use of Jigloo implies
  * acceptance of these licensing terms. A COMMERCIAL LICENSE HAS NOT BEEN
  * PURCHASED FOR THIS MACHINE, SO JIGLOO OR THIS CODE CANNOT BE USED LEGALLY FOR
  * ANY CORPORATE OR COMMERCIAL PURPOSE.
  */
 public class ViewAll extends javax.swing.JFrame {
 
 	private JScrollPane mainScrollPanel;
 	private JTable dataTable;
 	private JTextField searchTextField;
 	private JPanel searchPanel;
 	private JPanel searchButton;
 	private JToggleButton lessonNumberToggleButton;
 	private JToggleButton strokeCountToggleButton;
 	private JToggleButton keywordToggleButton;
 	private JToggleButton kanjiToggleButton;
 	private JToggleButton indexToggleButton;
 	private TableRowSorter<KanjiTableModel> tableRowSorter;
 	private List<Integer> filterIndicies;
 	private boolean filterByAllFields;
 	Font kanjiFont = new Font("Hiragino Mincho Pro", Font.PLAIN, 26);
 	private JScrollPane detailsScrollPanel;
 	private JPanel detailsStrokeAndKanjiPanel;
 	private JTextField detailsStrokeCountLabel;
 	private JPanel detailsMainContentHeadingPanel;
 	private JTextField detailsHeisigNumberLabel;
 	private JTextField detailsKeywordLabel;
 	Font kanjiLargeFont = new Font("Hiragino Mincho Pro", Font.PLAIN, 132);
 	private JTextField detailsKanjiLabel;
 	private JPanel detailsMainContentPanel;
 	private JPanel detailsKanjiContentPanel;
 	private JPanel detailsScrollPane;
 	private JSplitPane summaryAndDetailsSplitPanel;
 	private JSplitPane mainSplitPanel;
 	private JPanel relativesPanel;
 	private JTextArea relativesTextArea;
 	Font romanFont = new Font("Helvetica", Font.PLAIN, 16);
 	Font searchTextFieldFont = new Font("Lucida Grande", Font.PLAIN, 19);
 	private KanjiTableModel kanjiTableModel;
 
 	public ViewAll(List<HeisigItem> items) {
 		super("insig");
 		kanjiTableModel = new KanjiTableModel(items);
 		filterByAllFields = true;
 		filterIndicies = new ArrayList<Integer>();
 		initGUI();
 		searchTextField.requestFocus();
 	}
 
 	private void initGUI() {
 		try {
 			{
 				this.setMinimumSize(new java.awt.Dimension(600, 500));
 				this.setSize(900, 800);
 			}
 			{
 				//////////////////////////////////////////////////////////
 				// The panel for the search bar and the search buttons.
 				//////////////////////////////////////////////////////////
 				searchPanel = new JPanel();
 				BorderLayout mainPanelLayout = new BorderLayout();
 				getContentPane().add(searchPanel, BorderLayout.NORTH);
 				searchPanel.setLayout(mainPanelLayout);
 				searchPanel.setPreferredSize(new java.awt.Dimension(900, 35));
 				{
 					searchTextField = new JTextField();
 					searchPanel.add(searchTextField, BorderLayout.CENTER);
 					searchTextField.setName("searchTextField");
 					searchTextField.setPreferredSize(new java.awt.Dimension(
 							461, 35));
 					searchTextField.addKeyListener(new KeyAdapter() {
 						public void keyReleased(KeyEvent evt) {
 							searchTextFieldKeyReleased(evt);
 						}
 					});
 
 					System.out.println(searchTextField.getFont().getName());
 					searchTextField.setFont(searchTextFieldFont);
 				}
 				{
 					searchButton = new JPanel();
 					FlowLayout searchButtonLayout = new FlowLayout();
 					searchButtonLayout.setAlignment(FlowLayout.LEFT);
 					searchButtonLayout.setHgap(1);
 					searchButton.setLayout(searchButtonLayout);
 					searchPanel.add(searchButton, BorderLayout.WEST);
 					searchButton.setPreferredSize(new java.awt.Dimension(418,
 							35));
 					{
 						kanjiToggleButton = new JToggleButton();
 						searchButton.add(kanjiToggleButton);
 						kanjiToggleButton.setName("kanjiToggleButton");
 						kanjiToggleButton
 								.addActionListener(new ActionListener() {
 									public void actionPerformed(ActionEvent evt) {
 										kanjiToggleButtonActionPerformed(evt);
 									}
 								});
 					}
 					{
 						indexToggleButton = new JToggleButton();
 						searchButton.add(indexToggleButton);
 						indexToggleButton.setName("indexToggleButton");
 						indexToggleButton
 								.addActionListener(new ActionListener() {
 									public void actionPerformed(ActionEvent evt) {
 										indexToggleButtonActionPerformed(evt);
 									}
 								});
 					}
 					{
 						keywordToggleButton = new JToggleButton();
 						searchButton.add(keywordToggleButton);
 						keywordToggleButton.setName("keywordToggleButton");
 						keywordToggleButton
 								.addActionListener(new ActionListener() {
 									public void actionPerformed(ActionEvent evt) {
 										keywordToggleButtonActionPerformed(evt);
 									}
 								});
 					}
 					{
 						strokeCountToggleButton = new JToggleButton();
 						searchButton.add(strokeCountToggleButton);
 						strokeCountToggleButton
 								.setName("strokeCountToggleButton");
 						strokeCountToggleButton
 								.addActionListener(new ActionListener() {
 									public void actionPerformed(ActionEvent evt) {
 										strokeCountToggleButtonActionPerformed(evt);
 									}
 								});
 					}
 					{
 						lessonNumberToggleButton = new JToggleButton();
 						searchButton.add(lessonNumberToggleButton);
 						lessonNumberToggleButton
 								.setName("lessonNumberToggleButton");
 						lessonNumberToggleButton
 								.addActionListener(new ActionListener() {
 									public void actionPerformed(ActionEvent evt) {
 										lessonNumberToggleButtonActionPerformed(evt);
 									}
 								});
 					}
 				}
 			}
 
 			//////////////////////////////////////////////////////////
 			// the right hand side of the main split panel (the relatives area)
 			//////////////////////////////////////////////////////////
 
 			{
 				{
 					relativesPanel = new JPanel();
 					relativesPanel.setVisible(false);
 					BorderLayout relativesPanelLayout = new BorderLayout();
 					relativesPanel.setLayout(relativesPanelLayout);
 					{
 						relativesTextArea = new JTextArea();
 						relativesPanel.add(relativesTextArea,
 								BorderLayout.CENTER);
 						//						relativesTextArea.setPreferredSize(new java.awt.Dimension(187, 719));
 						relativesTextArea.setEditable(false);
 						relativesTextArea.setText("This is the rel");
 					}
 				}
 			}
 
 			//////////////////////////////////////////////////////////
 			// details text area
 			//////////////////////////////////////////////////////////
 
 			//////////////////////////////////////////////////////////
 			// results area
 			//////////////////////////////////////////////////////////
 			{
 				mainScrollPanel = new JScrollPane();
 				{
 					dataTable = new JTable();
 					mainScrollPanel.setViewportView(dataTable);
 
 					dataTable.setName("dataTable");
 					dataTable.setModel(kanjiTableModel);
 					tableRowSorter = new TableRowSorter<KanjiTableModel>(
 							kanjiTableModel);
 					dataTable.setRowSorter(tableRowSorter);
 					dataTable
 							.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);//this can be changed in the future to show multiple entries in the details view (like the kanji dictionary does)
 					dataTable.addFocusListener(new FocusAdapter() {
 						public void focusGained(FocusEvent evt) {
 							if (dataTable.getSelectedRow() < 0) {
 								dataTable.setRowSelectionInterval(0, 0);
 								mainScrollPanel.getViewport().setViewPosition(
 										new Point(0, 0));
 							}
 						}
 					});
 					ListSelectionModel listSelectionModel = dataTable
 							.getSelectionModel();
 					listSelectionModel
 							.addListSelectionListener(new ListSelectionListener() {
 								@Override
 								public void valueChanged(ListSelectionEvent e) {
 									int viewRow = dataTable.getSelectedRow();
 									System.out.println("View row: " + viewRow);
 									if (viewRow < 0) {
 										//selection got filtered away...
 										//do nothing atm, but something in the future...
 									} else {
 										int modelRow = dataTable
 												.convertRowIndexToModel(viewRow);
 										String heisigIndex = (String) dataTable
 												.getModel()
 												.getValueAt(
 														modelRow,
 														KanjiTableModel.HEISIG_INDEX_COLUMN_INDEX);
 										setDetails(heisigIndex);
 									}
 								}
 							});
 					//comparing integers
 					Comparator<Integer> intComparator = new Comparator<Integer>() {
 						@Override
 						public int compare(Integer o1, Integer o2) {
 							if (o1 > o2) {
 								return 1;
 							} else if (o2 > o1) {
 								return -1;
 							}
 							return 0;
 						}
 					};
 					tableRowSorter.setComparator(1, intComparator);// TODO need to make this more generic
 					tableRowSorter.setComparator(3, intComparator);// TODO need to make this more generic
 					tableRowSorter.setComparator(4, intComparator);// TODO need to make this more generic
 
 					//setting the columns
 					TableColumnModel tableColumnModel = dataTable
 							.getColumnModel();
 					TableColumn column;
 
 					DefaultTableCellRenderer tableRomanCellRenderer = new DefaultTableCellRenderer() {
 						@Override
 						public Font getFont() {
 							return romanFont;
 						}
 					};
 					for (int i = 0; i < tableColumnModel.getColumnCount(); i++) {
 						if (i != KanjiTableModel.KANJI_COLUMN_INDEX) {
 							tableColumnModel.getColumn(i).setCellRenderer(
 									tableRomanCellRenderer);
 						}
 					}
 					for (int i = 0; i < tableColumnModel.getColumnCount(); i++) {
 						if (i != KanjiTableModel.KEYWORD_COLUMN_INDEX) {
 							tableColumnModel.getColumn(i).setPreferredWidth(25);
 						} else {
 							tableColumnModel.getColumn(i)
 									.setPreferredWidth(250);
 						}
 					}
 					FontMetrics fontMetrics = dataTable
 							.getFontMetrics(kanjiFont);
 					dataTable.setRowHeight(fontMetrics.getHeight()); // set row height to match font
 					DefaultTableCellRenderer tableKanjiCellRenderer = new DefaultTableCellRenderer() {
 						@Override
 						public Font getFont() {
 							return kanjiFont;
 						}
 					};
 					//					tableCellRenderer.setFont(customFont);
 					column = tableColumnModel
 							.getColumn(KanjiTableModel.KANJI_COLUMN_INDEX);
 					column.setCellRenderer(tableKanjiCellRenderer);
 				}
 			}
 			{
 				detailsScrollPanel = new JScrollPane();
 				detailsScrollPanel.setPreferredSize(new java.awt.Dimension(489,
 						329));
 				detailsScrollPanel.setBackground(null);
 			}
 
 			//////////////////////////////////////////////////////////
 			// The split panel that holds the results and details
 			//////////////////////////////////////////////////////////
 			summaryAndDetailsSplitPanel = new JSplitPane(
 					JSplitPane.VERTICAL_SPLIT, mainScrollPanel,
 					detailsScrollPane);
 			summaryAndDetailsSplitPanel.add(detailsScrollPanel,
 					JSplitPane.RIGHT);
 			detailsScrollPanel.setName("detailsScrollPanel");
 			{
 				detailsScrollPane = new JPanel();
 				detailsScrollPanel.setViewportView(detailsScrollPane);
 				BorderLayout detailsScrollPaneLayout = new BorderLayout();
 				detailsScrollPane.setLayout(detailsScrollPaneLayout);
 				{
 					detailsKanjiContentPanel = new JPanel();
 					detailsKanjiContentPanel.setBackground(Color.WHITE);
 					BorderLayout detailsKanjiContentPanelLayout = new BorderLayout();
 					detailsKanjiContentPanel
 							.setLayout(detailsKanjiContentPanelLayout);
 					detailsScrollPane.add(detailsKanjiContentPanel,
 							BorderLayout.WEST);
 					{
 						detailsStrokeAndKanjiPanel = new JPanel();
 						detailsStrokeAndKanjiPanel.setBackground(Color.WHITE);
 						BoxLayout detailsStrokeAndKanjiPanelLayout = new BoxLayout(
 								detailsStrokeAndKanjiPanel,
 								javax.swing.BoxLayout.Y_AXIS);
 						detailsStrokeAndKanjiPanel
 								.setLayout(detailsStrokeAndKanjiPanelLayout);
 						detailsKanjiContentPanel.add(
 								detailsStrokeAndKanjiPanel, BorderLayout.NORTH);
 						{
 							detailsKanjiLabel = new JTextField();
 							detailsStrokeAndKanjiPanel.add(detailsKanjiLabel);
 							detailsKanjiLabel.setFont(kanjiLargeFont);
 							detailsKanjiLabel
 									.setAlignmentX(Component.CENTER_ALIGNMENT);
 							makeTextFieldLookLikeLabel(detailsKanjiLabel);
 							detailsKanjiLabel.setName("detailKanjiLanel");
 						}
 						{
 							detailsStrokeCountLabel = new JTextField();
 							detailsStrokeCountLabel.setFont(romanFont);
 							detailsStrokeAndKanjiPanel
 									.add(detailsStrokeCountLabel);
 							detailsStrokeCountLabel
 									.setAlignmentX(Component.CENTER_ALIGNMENT);
 							detailsStrokeCountLabel
 									.setHorizontalAlignment(JTextField.CENTER);
 							detailsStrokeCountLabel
 									.setToolTipText("Stroke count");
 							makeTextFieldLookLikeLabel(detailsStrokeCountLabel);
 							detailsStrokeCountLabel
 									.setName("detailsStrokeCountLabel");
 						}
 					}
 				}
 				{
 					detailsMainContentPanel = new JPanel();
 					detailsMainContentPanel.setBackground(Color.WHITE);
 					BorderLayout detailsMainContentPanelLayout = new BorderLayout();
 					detailsMainContentPanel
 							.setLayout(detailsMainContentPanelLayout);
 					detailsScrollPane.add(detailsMainContentPanel,
 							BorderLayout.CENTER);
 					{
 						detailsMainContentHeadingPanel = new JPanel();
 						detailsMainContentHeadingPanel
 								.setBackground(Color.WHITE);
 						BorderLayout detailsMainContentHeadingPanelLayout = new BorderLayout();
 						detailsMainContentHeadingPanel
 								.setLayout(detailsMainContentHeadingPanelLayout);
 						detailsMainContentPanel.add(
 								detailsMainContentHeadingPanel,
 								BorderLayout.NORTH);
 						{
 							detailsKeywordLabel = new JTextField();
 							makeTextFieldLookLikeLabel(detailsKeywordLabel);
 							detailsMainContentHeadingPanel.add(
 									detailsKeywordLabel, BorderLayout.EAST);
 							Font keywordFont = new Font(romanFont.getName(),
 									romanFont.getStyle(), 60);
 							detailsKeywordLabel.setFont(keywordFont);
 							detailsKeywordLabel.setName("detailsKeywordLabel");
 						}
 						{
 							detailsHeisigNumberLabel = new JTextField();
 							makeTextFieldLookLikeLabel(detailsHeisigNumberLabel);
 							detailsMainContentHeadingPanel
 									.add(detailsHeisigNumberLabel,
 											BorderLayout.WEST);
 							Font heisigNumberFont = new Font(romanFont
 									.getName(), romanFont.getStyle(), 40);
 							detailsHeisigNumberLabel.setFont(heisigNumberFont);
 							detailsHeisigNumberLabel.setForeground(Color.GRAY);
 							detailsHeisigNumberLabel
 									.setName("detailsHeisigNumberLabel");
 						}
 					}
 				}
 			}
 			//////////////////////////////////////////////////////////
 			// The main split panel with the relos on the RHS and the others on the left
 			//////////////////////////////////////////////////////////
 			{
 				mainSplitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT,
 						summaryAndDetailsSplitPanel, relativesPanel);
 				mainSplitPanel.setDividerLocation(this.getWidth() - 250);
 
 				getContentPane().add(mainSplitPanel, BorderLayout.CENTER);
 			}
 			Application.getInstance().getContext().getResourceMap(getClass())
 					.injectComponents(getContentPane());
 			pack();
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 
 	private void makeTextFieldLookLikeLabel(JTextField textField) {
 		textField.setEditable(false);
 		textField.setBorder(null);
 		textField.setBackground(null);
 	}
 
 	private void searchTextFieldKeyReleased(KeyEvent evt) {
 		// add filter
 		updateFilter();
 		//check for shortcuts?
 		if (evt.getKeyCode() == KeyEvent.VK_ENTER) {
 			dataTable.requestFocus();
 		}
 	}
 
 	private void updateFilter() {
 		String searchText = searchTextField.getText();
 		String[] searchItems = searchText.split(" ");
 
 		List<RowFilter<Object, Object>> filters = new ArrayList<RowFilter<Object, Object>>();
 		for (String searchItem : searchItems) {
 			//TODO o something about bad characters (like parenthasis) when passed in.
 			if (filterByAllFields) { // if we should search all of the fields
 				filters.add(RowFilter.regexFilter(searchItem));
 			} else {// if we should only search some of them.
 				filters.add(RowFilter.regexFilter(searchItem,
 						getFilterIndiciesArray()));
 			}
 		}
 		RowFilter<Object, Object> rowFilter = RowFilter.andFilter(filters);
 
 		// RowFilter<KanjiTableModel, Object> rowFilter = RowFilter
 		// .regexFilter(searchText);
 		tableRowSorter.setRowFilter(rowFilter);
 		searchTextField.requestFocus();
 	}
 
 	private int[] getFilterIndiciesArray() {
 		int[] indicies = new int[filterIndicies.size()];
 		for (int i = 0; i < indicies.length; i++) {
 			indicies[i] = filterIndicies.get(i);
 		}
 		return indicies;
 	}
 
 	private void indexToggleButtonActionPerformed(ActionEvent evt) {
 		toggleButtonActionPerformed(KanjiTableModel.HEISIG_INDEX_COLUMN_INDEX,
 				indexToggleButton);
 	}
 
 	private void kanjiToggleButtonActionPerformed(ActionEvent evt) {
 		toggleButtonActionPerformed(KanjiTableModel.KANJI_COLUMN_INDEX,
 				kanjiToggleButton);
 	}
 
 	private void keywordToggleButtonActionPerformed(ActionEvent evt) {
 		toggleButtonActionPerformed(KanjiTableModel.KEYWORD_COLUMN_INDEX,
 				keywordToggleButton);
 	}
 
 	private void strokeCountToggleButtonActionPerformed(ActionEvent evt) {
 		toggleButtonActionPerformed(KanjiTableModel.STROKE_COUNT_COLUMN_INDEX,
 				strokeCountToggleButton);
 	}
 
 	private void lessonNumberToggleButtonActionPerformed(ActionEvent evt) {
 		toggleButtonActionPerformed(KanjiTableModel.LESSON_NUMBER_COLUMN_INDEX,
 				lessonNumberToggleButton);
 	}
 
 	private void toggleButtonActionPerformed(Integer columnIndex,
 			JToggleButton toggleButton) {
 		if (toggleButton.isSelected()) {
 			filterIndicies.add(columnIndex);
 		} else {
 			if (filterIndicies.contains(columnIndex)) {
 				filterIndicies.remove(filterIndicies.indexOf(columnIndex));
 			}
 		}
 		if (toggleButtonSelected()) {
 			filterByAllFields = false;
 		} else {
 			filterByAllFields = true;
 		}
 		updateFilter();
 	}
 
 	private boolean toggleButtonSelected() {
 		return indexToggleButton.isSelected() || kanjiToggleButton.isSelected()
 				|| keywordToggleButton.isSelected()
 				|| strokeCountToggleButton.isSelected()
 				|| lessonNumberToggleButton.isSelected();
 	}
 
 	private void setDetails(String heisigIndex) {
 		HeisigItem item = kanjiTableModel.get(heisigIndex);
 		detailsHeisigNumberLabel.setText("(#" + item.getHeisigIndex() + ")");
 		detailsHeisigNumberLabel.setToolTipText("Lesson number: "
 				+ item.getLessonNumber());
 		detailsKeywordLabel.setText(item.getKeywordsFormatted());
 		detailsKanjiLabel.setText(item.getKanji());
		detailsMainContentHeadingPanel.validate();
 		detailsStrokeCountLabel.setText("[ " + item.getKanjiStrokeCount()
 				+ " ]");
 	}
 
 }
