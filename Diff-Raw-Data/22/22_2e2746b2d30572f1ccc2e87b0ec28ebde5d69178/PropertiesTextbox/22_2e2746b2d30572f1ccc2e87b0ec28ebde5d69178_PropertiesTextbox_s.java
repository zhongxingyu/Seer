 package gui.turing;
 
 import java.awt.BorderLayout;
 import java.awt.Dimension;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 
 import javax.swing.*;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 import com.mxgraph.model.mxCell;
 import com.mxgraph.model.mxGraphModel;
 import com.mxgraph.model.mxGraphModel.mxValueChange;
 import com.mxgraph.util.mxUndoableEdit;
 import com.mxgraph.view.mxGraph;
 
 import machine.turing.Textbox;
 
 /**
  * This class implements the textbox properties panel
  * @author David Wille
  *
  */
 
 public class PropertiesTextbox extends JPanel implements DocumentListener {
 	private static final long serialVersionUID = -4793677956080787446L;
 	private Textbox textbox;
 	private JPanel content;
 	private JLabel textLabel = new JLabel("Text:");
 	private JTextArea textArea;
 	private JScrollPane scrollPane;
 	private mxGraph graph;
 	private TuringMachineEditor turingMachineEditor;
 	private mxCell cell;
 
 	/**
 	 * This constructs a new textbox properties panel
 	 * @param textbox Textbox whose information should be displayed
 	 * @param graph Current graph
 	 */
 	public PropertiesTextbox(Textbox textbox, mxGraph graph, mxCell cell, TuringMachineEditor turingMachineEditor) {
 		this.turingMachineEditor = turingMachineEditor;
 		this.graph = graph;
 		this.textbox = textbox;
 		this.cell = cell;
 		
 		this.setMaximumSize(new Dimension(1000, 120));
 		this.setPreferredSize(new Dimension(250, 300));
 		this.setBorder(BorderFactory.createTitledBorder("Properties"));
 		this.setLayout(new BorderLayout());
 		GridBagConstraints c = new GridBagConstraints();
 		
 		content = new JPanel(new GridBagLayout());
 		textArea = new JTextArea(10,5);
 		textArea.setBorder(BorderFactory.createLoweredBevelBorder());
 		textArea.getDocument().addDocumentListener(this);
 		textArea.setText(textbox.getText());
 		textArea.setLineWrap(true);
 		textArea.setWrapStyleWord(true);
 		scrollPane = new JScrollPane(textArea);
 		scrollPane.setHorizontalScrollBarPolicy(ScrollPaneConstants.HORIZONTAL_SCROLLBAR_NEVER);
 		scrollPane.setPreferredSize(new Dimension(120, 150));
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weightx = 0.1;
 		c.fill = GridBagConstraints.BOTH;
 		c.insets = new Insets(10,10,10,10);
 		content.add(textLabel, c);
 		c.gridx = 1;
 		c.gridy = 0;
 		c.weightx = 0.9;
 		c.weighty = 1.0;
 		c.fill = GridBagConstraints.BOTH;
 		c.insets = new Insets(10,10,10,10);
 		content.add(scrollPane, c);
 		
 		this.add(content);
 	}
 
 	private void addUndoableEdit() {
 		mxValueChange change = new mxValueChange((mxGraphModel) graph.getModel(), cell, this.textbox);
 		change.setPrevious(this.textbox.clone());
 		mxUndoableEdit edit = new mxUndoableEdit(change);
 		edit.add(change);
 		turingMachineEditor.getUndoManager().undoableEditHappened(edit);
 	}
 	
 	@Override
 	public void insertUpdate(DocumentEvent e) {
 		addUndoableEdit();
 		this.textbox.setText(textArea.getText());
 		this.graph.refresh();
 		this.graph.repaint();
 	}
 
 	@Override
 	public void removeUpdate(DocumentEvent e) {
 		addUndoableEdit();
 		this.textbox.setText(textArea.getText());
 		this.graph.refresh();
 		this.graph.repaint();
 	}
 
 	@Override
 	public void changedUpdate(DocumentEvent e) {
 	}
 	
 }
