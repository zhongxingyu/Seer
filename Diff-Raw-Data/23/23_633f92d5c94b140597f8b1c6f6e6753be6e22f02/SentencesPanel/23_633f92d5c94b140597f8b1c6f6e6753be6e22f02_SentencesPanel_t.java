 package org.pathwayeditor.curationtool.sentences;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dialog;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.swing.AbstractButton;
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.JTextField;
 import javax.swing.JTextPane;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 import javax.swing.table.DefaultTableColumnModel;
 import javax.swing.table.TableColumn;
 import javax.swing.table.TableColumnModel;
 
 import org.pathwayeditor.curationtool.dataviewer.DataViewEvent;
 import org.pathwayeditor.curationtool.dataviewer.DataViewListener;
 import org.pathwayeditor.curationtool.dataviewer.DataViewTableModel;
 import org.pathwayeditor.curationtool.dataviewer.IRowDefn;
 import org.pathwayeditor.notations.annotator.ndom.IEntityNode;
 import org.pathwayeditor.notations.annotator.ndom.ISentence;
 import org.pathwayeditor.notations.annotator.ndom.ISentenceStateChangeListener;
 import org.pathwayeditor.notations.annotator.ndom.impl.ISentenceStateChangeEvent;
 
 public class SentencesPanel extends JPanel {
 	private static final long serialVersionUID = 1L;
 	private static final int NAME_COLUMNS = 20;
 	private DataViewTableModel tableModel;
 	private JScrollPane dataViewScrollPane = new JScrollPane();
 	private TableColumnModel tableColumnModel;
 	private JTable dataViewTable;
 	private JPanel focusDetailPanel = new JPanel();
 	private JPanel previewPanel = new JPanel();
 	private javax.swing.JPanel actionPanel = new JPanel();
 	private JTextPane sentencePreviewer = new JTextPane();
 	private transient List<DataViewListener> dataViewListeners = new LinkedList<DataViewListener>();
 	private ListSelectionModel selectionModel;
 	private JButton prevButton;
 	private JButton nextButton;
 	private ISentence currentSentence;
 	private final List<ISentenceSelectionChangeListener> listeners;
 	private JCheckBox irrelevantCheckBox;
 	private JCheckBox focusCheckBox;
 	private JCheckBox interactingNodeCheckBox;
 	private ISentenceStateChangeListener sentenceListener;
 	private AbstractButton acceptButton;
 	private ActionListener nextActionListener;
 	private ActionListener prevActionListener;
 	private ChangeListener interactingNodeCheckBoxListener;
 	private ChangeListener focusCheckBoxListener;
 	private ChangeListener irrelevantCheckBoxListener;
 	private ListSelectionListener listSelectionListener;
 	private boolean isOpen = false;
 	private JTextField focusNameField;
 	private final Dialog synonymDialog;
 	private JButton synonymsButton;
 	private JButton intNodeSynonymsButton;
 	private JTextField intNodeNameField;
 	private JPanel intNodeDetailPanel = new JPanel();
 	private SynonymDialog focusNodeSynonymJDialog;
 	private SynonymDialog intNodeSynonymJDialog;
 	
 	public SentencesPanel(Dialog synonymDialog){
 		this.listeners = new LinkedList<ISentenceSelectionChangeListener>();
 		this.synonymDialog = synonymDialog;
 		
 		IRowDefn rowDefn = new SentenceRowDefinition();
 		tableModel = new DataViewTableModel(rowDefn);
 		tableColumnModel = new DefaultTableColumnModel();
 		dataViewTable = new JTable(tableModel, tableColumnModel);
 		dataViewTable.setAutoscrolls(true);
 		dataViewTable.setShowHorizontalLines(true);
 		dataViewTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
 
 		selectionModel = dataViewTable.getSelectionModel();
 		selectionModel.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 		
 		setColumnDefs();
 		
 		dataViewScrollPane.getViewport().add(dataViewTable);
 		setupActionPanel();
 		setUpFocusPanel();
 		setUpIntNodePanel();
 		JPanel sentenceSectionPanel = new JPanel();
 		sentenceSectionPanel.setLayout(new BorderLayout());
 		sentenceSectionPanel.add(this.focusDetailPanel, BorderLayout.PAGE_START);
 		sentenceSectionPanel.add(dataViewScrollPane, BorderLayout.CENTER);
 		this.previewPanel.setLayout(new BorderLayout());
 		this.previewPanel.add(this.intNodeDetailPanel, BorderLayout.PAGE_START);
 		this.previewPanel.add(this.sentencePreviewer, BorderLayout.CENTER);
 		this.previewPanel.add(this.actionPanel, BorderLayout.PAGE_END);
 		this.sentencePreviewer.setEditable(false);
 		
 	    this.setBorder(BorderFactory.createLineBorder(Color.BLACK));
 
 		this.setLayout(new GridLayout(1, 2));
 	    this.add(sentenceSectionPanel);
 		this.add(previewPanel);
 		focusNodeSynonymJDialog = new SynonymDialog(this.synonymDialog); 
		focusNodeSynonymJDialog.setSize(new Dimension(200, 300));
 		intNodeSynonymJDialog = new SynonymDialog(this.synonymDialog); 
		intNodeSynonymJDialog.setSize(new Dimension(200, 300));
 	    this.sentenceListener = new ISentenceStateChangeListener() {
 			@Override
 			public void stateChanged(ISentenceStateChangeEvent e) {
 				dataViewTable.updateUI();
 				updateButtons();
 			}
 		};
 		this.nextActionListener = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int currSelectionIdx = getCurrentSelection()+1;
 				selectionModel.setSelectionInterval(currSelectionIdx, currSelectionIdx);
 				int scrollOffset = dataViewTable.getRowHeight() * getCurrentSelection();
 				dataViewScrollPane.getVerticalScrollBar().setValue(scrollOffset);
 			}
 		};
 		this.prevActionListener = new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				int currSelectionIdx = getCurrentSelection()-1;
 				selectionModel.setSelectionInterval(currSelectionIdx, currSelectionIdx);
 				int scrollOffset = dataViewTable.getRowHeight() * getCurrentSelection();
 				dataViewScrollPane.getVerticalScrollBar().setValue(scrollOffset);
 			}
 		};
 		this.interactingNodeCheckBoxListener = new ChangeListener(){
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				currentSentence.setInteractingNodeValid(interactingNodeCheckBox.isSelected());
 			}
 		};
 		this.focusCheckBoxListener = new ChangeListener(){
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				currentSentence.setFocusNodeValid(focusCheckBox.isSelected());
 			}
 		};
 		this.irrelevantCheckBoxListener = new ChangeListener(){
 			@Override
 			public void stateChanged(ChangeEvent e) {
 				currentSentence.setSentenceRelevant(!irrelevantCheckBox.isSelected());
 			}
 			
 		};
 		this.listSelectionListener = new ListSelectionListener() {
 			@Override
 			public void valueChanged(ListSelectionEvent e) {
 				checkNavigatorButtonEnablement();
 				currentSentence.removeSentenceStateChangeListener(sentenceListener);
 				ISentence oldSentence = currentSentence;
 				currentSentence = ((SentenceRow)tableModel.getRow(getCurrentSelection())).getSentence();
 				notifySentenceSelectionChanged(getCurrentSelection(), oldSentence, currentSentence);
 				updateButtons();
 				sentencePreviewer.setText(currentSentence.getMarkedUpSentence());
 				intialiseIntNodeDetailPanel();
 				if(intNodeSynonymJDialog.isVisible()){
 					intNodeSynonymJDialog.setSynonyms(currentSentence.getArc().getInteractingNode());
 				}
 				currentSentence.addSentenceStateChangeListener(sentenceListener);
 			}
 		};
 	}
 
 	private void setUpFocusPanel(){
 		JLabel label = new JLabel("Focus Node:");
 		focusNameField = new JTextField(NAME_COLUMNS);
 		focusNameField.setEditable(false);
 		synonymsButton = new JButton("Synonyms");
 		synonymsButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				showFocusSynonymDialog();
 			}
 		});
 		this.focusDetailPanel.add(label);
 		this.focusDetailPanel.add(focusNameField);
 		this.focusDetailPanel.add(synonymsButton);
 		synonymsButton.setEnabled(false);
 //		this.focusDetailPanel.add(focusSynonymsField);
 	}
 	
 	private void setUpIntNodePanel(){
 		JLabel label = new JLabel("Interacting Node:");
 		intNodeNameField = new JTextField(NAME_COLUMNS);
 		intNodeNameField.setEditable(false);
 		intNodeSynonymsButton = new JButton("Synonyms");
 		intNodeSynonymsButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				showIntNodeSynonymDialog();
 			}
 		});
 		this.intNodeDetailPanel.add(label);
 		this.intNodeDetailPanel.add(intNodeNameField);
 		this.intNodeDetailPanel.add(intNodeSynonymsButton);
 		intNodeSynonymsButton.setEnabled(false);
 //		this.focusDetailPanel.add(focusSynonymsField);
 	}
 	
 	private void notifySentenceSelectionChanged(final int idx, final ISentence oldSentence, final ISentence currentSentence) {
 		ISentenceSelectionChangeEvent e = new ISentenceSelectionChangeEvent(){
 
 			@Override
 			public ISentence getSelectedSentence() {
 				return currentSentence;
 			}
 
 			@Override
 			public int getCurrentRowIdx() {
 				return idx;
 			}
 
 			@Override
 			public ISentence getOldSentence() {
 				return oldSentence;
 			}
 			
 		};
 		for(ISentenceSelectionChangeListener l : this.listeners){
 			l.sentenceSelectionChangeEvent(e);
 		}
 	}
 
 	public ISentence getCurrentlySelectedSentence(){
 		return this.currentSentence;
 	}
 	
 	public void loadData(Iterator<ISentence> sentenceIter){
 		if(isOpen){
 			reset();
 		}
 	    this.tableModel.deleteAllRows();
 	    while(sentenceIter.hasNext()){
 	    	final ISentence sentence = sentenceIter.next();
 	    	this.tableModel.appendRow(new SentenceRow(sentence));
 	    }
 	    this.tableModel.commitChanges();
 	    this.resetSelection();
 	    intialise();
 	    isOpen = true;
 	}
 	
 	public void close(){
 		if(isOpen){
 			reset();
 		}
 		isOpen = false;
 	}
 	
 	private void reset(){
 		this.currentSentence.removeSentenceStateChangeListener(sentenceListener);
 		this.selectionModel.removeListSelectionListener(this.listSelectionListener);
 		this.nextButton.removeActionListener(this.nextActionListener);
 		this.prevButton.removeActionListener(prevActionListener);
 		this.irrelevantCheckBox.removeChangeListener(this.irrelevantCheckBoxListener);
 		this.focusCheckBox.removeChangeListener(this.focusCheckBoxListener);
 		this.interactingNodeCheckBox.removeChangeListener(this.interactingNodeCheckBoxListener);
 	}
 	
 	private void intialiseFocusDetailPanel(){
 		IEntityNode focusNode = this.currentSentence.getArc().getFocusNode();
 		this.focusNameField.setText(focusNode.getName());
 		synonymsButton.setEnabled(true);
 //		this.focusSynonymsField.setText(buf.toString());
 	}
 	
 	private void intialiseIntNodeDetailPanel(){
 		IEntityNode intNode = this.currentSentence.getArc().getInteractingNode();
 		this.intNodeNameField.setText(intNode.getName());
 		intNodeSynonymsButton.setEnabled(true);
 //		this.focusSynonymsField.setText(buf.toString());
 	}
 	
 	private void showFocusSynonymDialog(){
 		focusNodeSynonymJDialog.setSynonyms(currentSentence.getArc().getFocusNode());
 		focusNodeSynonymJDialog.setVisible(true);
 	}
 	
 	private void showIntNodeSynonymDialog(){
 		intNodeSynonymJDialog.setSynonyms(currentSentence.getArc().getInteractingNode());
 		intNodeSynonymJDialog.setVisible(true);
 	}
 	
 	private void intialise() {
 		updateButtons();
 		this.sentencePreviewer.setContentType("text/html");
 		this.sentencePreviewer.setText("<html>" + this.currentSentence.getMarkedUpSentence() + "</html>");
 		intialiseFocusDetailPanel();
 		intialiseIntNodeDetailPanel();
 		this.currentSentence.addSentenceStateChangeListener(sentenceListener);
 		this.selectionModel.addListSelectionListener(this.listSelectionListener);
 		this.nextButton.addActionListener(this.nextActionListener);
 		this.prevButton.addActionListener(prevActionListener);
 	}
 
 	public void addSentenceSelectionChangeListener(ISentenceSelectionChangeListener l){
 		this.listeners.add(l);
 	}
 	
 	public void removeSentenceSelectionChangeListener(ISentenceSelectionChangeListener l){
 		this.listeners.remove(l);
 	}
 	
 	public List<ISentenceSelectionChangeListener> getSentenceSelectionChangeListeners(){
 		return new ArrayList<ISentenceSelectionChangeListener>(this.listeners);
 	}
 	
 	public ISentence getSelectedSentence(){
 		return currentSentence;
 	}
 	
 	private void setupActionPanel() {
 		this.actionPanel.setLayout(new GridBagLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		this.actionPanel.setPreferredSize(new Dimension(600, 100));
 		c.gridx = 0;
 		c.gridy = 0;
 		irrelevantCheckBox = addCheckBox("Sentence Irrelevant", c);
 		c.gridx = 1;
 		c.gridy = 0;
 		focusCheckBox = addCheckBox("Focus Node OK", c);
 		c.gridx = 0;
 		c.gridy = 1;
 		interactingNodeCheckBox = addCheckBox("Interacting Node OK", c);
 		irrelevantCheckBox.addChangeListener(this.irrelevantCheckBoxListener);
 		focusCheckBox.addChangeListener(this.focusCheckBoxListener);
 		interactingNodeCheckBox.addChangeListener(this.interactingNodeCheckBoxListener);
 		prevButton = new JButton("Previous");
 		c.gridx = 0;
 		c.gridy = 2;
 		this.actionPanel.add(prevButton, c);
 		prevButton.addActionListener(this.prevActionListener);
 		nextButton = new JButton("Next");
 		c.gridx = 1;
 		c.gridy = 2;
 		this.actionPanel.add(nextButton, c);
 		nextButton.addActionListener(this.nextActionListener);
 		acceptButton = new JButton("Accept");
 		c.gridx = 1;
 		c.gridy = 1;
 		this.actionPanel.add(acceptButton, c);
 		acceptButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				currentSentence.sentenceValidated();
 				int currSelectionIdx = getCurrentSelection();
 				if(getCurrentSelection() < tableModel.getRowCount()-1){
 					currSelectionIdx++;
 				}
 				selectionModel.setSelectionInterval(currSelectionIdx, currSelectionIdx);
 				int scrollOffset = dataViewTable.getRowHeight() * getCurrentSelection();
 				dataViewScrollPane.getVerticalScrollBar().setValue(scrollOffset);
 			}
 		});
 	}
 
 	private void checkNavigatorButtonEnablement(){
 		int currSelectionIdx = getCurrentSelection();
 		prevButton.setEnabled(currSelectionIdx > 0);
 		nextButton.setEnabled(currSelectionIdx < tableModel.getRowCount()-1);
 	}
 	
 	private JCheckBox addCheckBox(String labelText, GridBagConstraints c) {
 		JLabel label = new JLabel(labelText);
 		JCheckBox checkBox = new JCheckBox();
 		checkBox.setSelected(false);
 		JPanel buttonPanel = new JPanel();
 		buttonPanel.setLayout(new GridLayout(1, 2));
 		buttonPanel.add(label);
 		buttonPanel.add(checkBox);
 		this.actionPanel.add(buttonPanel, c);
 		return checkBox;
 	}
 
 	private void setColumnDefs() {
 		IRowDefn rowDefn = this.tableModel.getRowDefn();
 		for (int i = 0; i < rowDefn.getNumColumns(); i++) {
 			TableColumn colDefn = new TableColumn();
 			colDefn.setHeaderValue(i);
 			colDefn.setHeaderValue(rowDefn.getColumnHeader(i));
 			colDefn.setResizable(rowDefn.isColumnResizable(i));
 			colDefn.setPreferredWidth(rowDefn.getPreferredWidth(i));
 			if (rowDefn.getCustomRenderer(i) != null) {
 				colDefn.setCellRenderer(rowDefn.getCustomRenderer(i));
 			}
 			if (rowDefn.getCellEditor(i) != null) {
 				colDefn.setCellEditor(rowDefn.getCellEditor(i));
 			}
 			colDefn.setModelIndex(i);
 			tableColumnModel.addColumn(colDefn);
 		}
 	}
 
 	public void addDataViewListener(DataViewListener l) {
 		this.dataViewListeners.add(l);
 	}
 
 	public void removeDataViewListener(DataViewListener l) {
 		this.dataViewListeners.remove(l);
 	}
 
 	protected void fireRowInserted(DataViewEvent event) {
 		for (DataViewListener listener : dataViewListeners) {
 			listener.rowInserted(event);
 		}
 	}
 
 	protected void fireRowDeleted(DataViewEvent event) {
 		for (DataViewListener listener : dataViewListeners) {
 			listener.rowDeleted(event);
 		}
 	}
 
 	protected void fireViewSaved(DataViewEvent event) {
 		for (DataViewListener listener : dataViewListeners) {
 			listener.viewSaved(event);
 		}
 	}
 
 	protected void fireViewReset(DataViewEvent event) {
 		for (DataViewListener listener : dataViewListeners) {
 			listener.viewReset(event);
 		}
 	}
 
 	public DataViewTableModel getTableModel() {
 		return this.tableModel;
 	}
 
 	private int getCurrentSelection(){
 		return selectionModel.getAnchorSelectionIndex();
 	}
 	
 	public void resetSelection() {
 		selectionModel.setSelectionInterval(0, 0);
 		currentSentence = ((SentenceRow)tableModel.getRow(getCurrentSelection())).getSentence();
 	}
 
 	private void updateButtons() {
 		this.irrelevantCheckBox.removeChangeListener(this.irrelevantCheckBoxListener);
 		this.focusCheckBox.removeChangeListener(this.focusCheckBoxListener);
 		this.interactingNodeCheckBox.removeChangeListener(this.interactingNodeCheckBoxListener);
 		
 		this.irrelevantCheckBox.setSelected(!this.currentSentence.isSentenceRelevant());
 		this.focusCheckBox.setSelected(this.currentSentence.isFocusNodeValid());
 		this.interactingNodeCheckBox.setSelected(this.currentSentence.isInteractingNodeValid());
 		irrelevantCheckBox.setEnabled(focusCheckBox.isSelected() && interactingNodeCheckBox.isSelected());
 		
 		this.irrelevantCheckBox.addChangeListener(this.irrelevantCheckBoxListener);
 		this.focusCheckBox.addChangeListener(this.focusCheckBoxListener);
 		this.interactingNodeCheckBox.addChangeListener(this.interactingNodeCheckBoxListener);
 	}
 
 }
