 package gui.turing;
 
 import java.awt.*;
 import java.awt.event.ItemEvent;
 import java.awt.event.ItemListener;
 import javax.swing.*;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 
 import com.mxgraph.model.mxCell;
 import com.mxgraph.model.mxGraphModel;
 import com.mxgraph.model.mxGraphModel.mxValueChange;
 import com.mxgraph.util.mxUndoableEdit;
 import com.mxgraph.view.mxCellState;
 import com.mxgraph.view.mxGraph;
 
 import machine.turing.State;
 
 /**
  * This class implements the state properties panel
  * @author Sven Schuster
  *
  */
 
 public class PropertiesState extends JPanel implements ItemListener, DocumentListener {
 	
 	private static final long serialVersionUID = -5483231033293338765L;
 
 	private JLabel name;
 	private JTextField inputName;
 	private JCheckBox startState;
 	private JCheckBox finalState;
 	private JPanel propertiesPanel;
 	private State state;
 	private mxGraph graph;
 	private mxCellState vertex;
 	private TuringMachineEditor turingMachineEditor;
 	
 	public PropertiesState(State state, mxGraph graph, mxCellState vertex, TuringMachineEditor turingMachineEditor) {
 		this.turingMachineEditor = turingMachineEditor;
 		this.graph = graph;
 		this.vertex = vertex;
 		this.state = state;
 		name = new JLabel("Name");
 		inputName = new JTextField(this.state.getName(),10);
 		inputName.getDocument().addDocumentListener(this);
 		startState = new JCheckBox("Start");
 		startState.setSelected(this.state.isStartState());
 		startState.addItemListener(this);
 		finalState = new JCheckBox("Final");
 		finalState.setSelected(this.state.isFinalState());
 		finalState.addItemListener(this);
 		propertiesPanel = new JPanel();
 		propertiesPanel.setLayout(new GridBagLayout());
 		
 		this.setMaximumSize(new Dimension(1000, 120));
 		this.setPreferredSize(new Dimension(250, 120));
 		this.setBorder(BorderFactory.createTitledBorder("Properties"));
 		this.setLayout(new BorderLayout());
 		
 		GridBagConstraints c = new GridBagConstraints();
 		
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = 0;
 		c.weightx = 1.0;
 		c.gridwidth = 2;
 		c.insets = new Insets(5,5,5,5);
 		propertiesPanel.add(name, c);
 		
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = 1;
 		c.weightx = 1.0;
 		c.gridwidth = 2;
 		c.insets = new Insets(5,5,5,5);
 		propertiesPanel.add(inputName, c);
 		
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 0;
 		c.gridy = 2;
 		c.weightx = 0.5;
 		c.gridwidth = 1;
 		c.insets = new Insets(5,5,5,5);
 		propertiesPanel.add(startState, c);
 		
 		c.fill = GridBagConstraints.HORIZONTAL;
 		c.gridx = 1;
 		c.gridy = 2;
 		c.weightx = 0.5;
 		c.gridwidth = 1;
 		c.insets = new Insets(5,5,5,5);
 		propertiesPanel.add(finalState, c);
 		
 		this.add(propertiesPanel, BorderLayout.CENTER);
 	}
 
 	public void setStyle(mxCell cell) {
 		if(startState.isSelected() && finalState.isSelected())
 			cell.setStyle("FINALSTART");
 		else if(startState.isSelected())
 			cell.setStyle("START");
 		else if(finalState.isSelected())
 			cell.setStyle("FINAL");
 		else
 			cell.setStyle("CIRCLE");
 	}
 	
 	private void addUndoableEdit() {
 		mxValueChange change = new mxValueChange((mxGraphModel) graph.getModel(), vertex.getCell(), this.state);
 		change.setPrevious(this.state.clone());
 		mxUndoableEdit edit = new mxUndoableEdit(change);
 		edit.add(change);
 		turingMachineEditor.getUndoManager().undoableEditHappened(edit);
		turingMachineEditor.updateUndoRedoMenu();
 	}
 	
 	@Override
 	public void itemStateChanged(ItemEvent e) {
 		addUndoableEdit();
 		if(e.getSource().equals(finalState)) {
 			this.state.setFinalState(finalState.isSelected());
 			mxCell cell = (mxCell) vertex.getCell();
 			setStyle(cell);
 			graph.refresh();			
 			graph.repaint();
 			this.vertex = graph.getView().getState(cell);
 		}
 		else if(e.getSource().equals(startState)) {
 			this.state.setStartState(startState.isSelected());
 			mxCell cell = (mxCell) vertex.getCell();
 			setStyle(cell);
 			graph.refresh();			
 			graph.repaint();
 			this.vertex = graph.getView().getState(cell);
 		}
 	}
 	
 	@Override
 	public void changedUpdate(DocumentEvent e) {}
 
 	@Override
 	public void insertUpdate(DocumentEvent e) {
 		addUndoableEdit();
 		this.state.setName(this.inputName.getText());
 		graph.refresh();
 		graph.repaint();
 	}
 
 	@Override
 	public void removeUpdate(DocumentEvent e) {
 		addUndoableEdit();
 		this.state.setName(this.inputName.getText());
 		graph.refresh();
 		graph.repaint();
 	}
 }
