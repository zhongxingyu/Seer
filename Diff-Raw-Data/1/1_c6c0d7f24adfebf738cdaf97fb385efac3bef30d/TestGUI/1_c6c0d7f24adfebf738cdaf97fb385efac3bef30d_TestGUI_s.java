 package munk.graph.gui;
 
 import java.awt.EventQueue;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.ComponentAdapter;
 import java.awt.event.ComponentEvent;
 import java.awt.event.KeyAdapter;
 import java.awt.event.KeyEvent;
 
 import javax.swing.BoxLayout;
 import javax.swing.Icon;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 import javax.swing.UIManager;
 import javax.vecmath.Color3f;
 
 import munk.graph.appearance.Colors;
 import munk.graph.function.Function;
 import munk.graph.function.FunctionList;
 import munk.graph.function.FunctionUtil;
 import munk.graph.plot.Plotter3D;
 
 import com.graphbuilder.math.ExpressionParseException;
 
 
 /**
  * A simple GUI for the 3DPlotter application.
  * @author xXx
  *
  */
 public class TestGUI {
 	
 	private static final int CANVAS_INITIAL_WIDTH = 600;
 	private static final int CANVAS_INITIAL_HEIGTH = 600;
 	private static final float DEFAULT_STEPSIZE = (float) 0.1;
 	private static final float[] DEFAULT_BOUNDS = {-1,1,-1,1,-1,1};
 	
 	// GUI Variables.
 	private static TestGUI window;
 	private JFrame frame;
 	private JTextField function;
 	private JLabel lblFunctions;
 	private JButton btnEdit;
 	private JButton btnDelete;
 	private JButton btnPlot;
 	private JPanel functionPanel;
 
 	// Non-GUI variables.
 	private Plotter3D plotter;
 	private int controlsWidth;
 	private int controlsHeight;
 	private FunctionList<Function> functionList; 
 	private FunctionList<Function> paramFunctionList; 
 	private int noOfFunctions;
 	private boolean maximized;
 	
 	// Option variables
 	private static final float STEP_SIZE = (float) 1; 
 	
 	/**
 	 * Launch the application.
 	 */
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			public void run() {
 				// Set look-and-feel to OS default.
 				try {
 					UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 				} 
 				catch (Exception e) {
 					System.out.println("Unable to load native look and feel: " + e.toString());
 				}
 				window = new TestGUI();
 				window.frame.setVisible(true);
 			}
 		});
 	}
 	
 	/**
 	 * Create the application.
 	 */
 	public TestGUI() {
 		frame = new JFrame("Ultra Mega Epic Xtreme Plotter 3D");
 		functionList = new FunctionList<Function>();
 		paramFunctionList = new FunctionList<Function>();
 		noOfFunctions = 0;
 		maximized = false;
 		initialize();
 	}
 
 	/**
 	 * Initialize;
 	 */
 	private void initialize(){
 		// Layout definition.
 		frame.setBounds(100, 100, 1000, 1000);
      	GridBagLayout gbl = new GridBagLayout();
      	gbl.columnWidths = new int[]{5, 0, 50, 50, 5, 0};
      	gbl.rowHeights = new int[]{5, 5, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0};
      	gbl.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
      	gbl.rowWeights = new double[]{0.0, 1.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, 0.0, Double.MIN_VALUE};
      	frame.getContentPane().setLayout(gbl);
      	
 		// The 3D plotter.
      	plotter = new Plotter3D();
     	GridBagConstraints gbc_plotter = new GridBagConstraints();
     	gbc_plotter.gridheight = 11;
      	gbc_plotter.insets = new Insets(0, 0, 5, 5);
      	gbc_plotter.gridx = 1;
      	gbc_plotter.gridy = 1;
      	frame.getContentPane().add(plotter, gbc_plotter);
      	
      	// The list of plotted functions.
      	functionPanel = new JPanel();
      	functionPanel.setLayout(new BoxLayout(functionPanel,BoxLayout.Y_AXIS));
      	GridBagConstraints gbc_list = new GridBagConstraints();
      	gbc_list.anchor = GridBagConstraints.NORTH;
      	gbc_list.gridwidth = 2;
      	gbc_list.insets = new Insets(0, 0, 5, 5);
      	gbc_list.fill = GridBagConstraints.HORIZONTAL;
      	gbc_list.gridx = 2;
      	gbc_list.gridy = 7;
      	frame.getContentPane().add(functionPanel, gbc_list);    	
      	
      	// Auto update List according to the function list.
      	functionList.addActionListener(new ActionListener() {
 
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(e.getActionCommand().equals("ADD")){
 					functionPanel.add(new FunctionLabel((Function) e.getSource(), new ActionListener() {
 						public void actionPerformed(ActionEvent e) {
 							Function oldFunc = (Function) e.getSource();
 							String newExpr = e.getActionCommand();
 							updatePlot(oldFunc, newExpr, oldFunc.getColor(), oldFunc.getBounds(), oldFunc.getStepsize());
 						}
 					}));
 				}
 				else if(e.getActionCommand().equals("REMOVE")){
 					functionPanel.remove(e.getID());
 				}
 				else if(e.getActionCommand().equals("SET")){
 					FunctionLabel label = (FunctionLabel) functionPanel.getComponent(e.getID());
 					label.setMother((Function) e.getSource());
 				}
 			}
 		});
      	
      	// Heading.
      	lblFunctions = new JLabel("Equations");
      	GridBagConstraints gbc_lblFunctions = new GridBagConstraints();
      	gbc_lblFunctions.gridwidth = 2;
      	gbc_lblFunctions.insets = new Insets(0, 0, 5, 5);
      	gbc_lblFunctions.gridx = 2;
      	gbc_lblFunctions.gridy = 6;
      	frame.getContentPane().add(lblFunctions, gbc_lblFunctions);
      	
      	// Delete button.
      	btnDelete = new JButton("Delete");
      	GridBagConstraints gbc_btnDelete = new GridBagConstraints();
      	gbc_btnDelete.insets = new Insets(0, 0, 5, 5);
      	gbc_btnDelete.gridx = 2;
      	gbc_btnDelete.gridy = 8;
      	frame.getContentPane().add(btnDelete, gbc_btnDelete);
      	btnDelete.addActionListener(new ActionListener() {
      		// Remove the graph.
      		@Override
      		public void actionPerformed(ActionEvent arg0) {   			
      			deleteSelectedFunctions(); 
      			frame.pack();
      		}
      	});
      	
      	// Edit button.
      	btnEdit = new JButton("Edit");
      	GridBagConstraints gbc_btnEdit = new GridBagConstraints();
      	gbc_btnEdit.insets = new Insets(0, 0, 5, 5);
      	gbc_btnEdit.gridx = 3;
      	gbc_btnEdit.gridy = 8;
      	frame.getContentPane().add(btnEdit, gbc_btnEdit);
      	btnEdit.addActionListener(new ActionListener() {
      		// Edit the graph.
      		@Override
      		public void actionPerformed(ActionEvent arg0) {
      			for(Function f : functionList){
      				if(f.isSelected()) spawnEditDialog(f);
      			}
      		}
      	});
      	
      	// Function input.
      	function = new JTextField();
      	function.setText("Input equation here.");
      	GridBagConstraints gbc_txtInputFunctionExpression = new GridBagConstraints();
      	gbc_txtInputFunctionExpression.gridwidth = 2;
      	gbc_txtInputFunctionExpression.insets = new Insets(0, 0, 5, 5);
      	gbc_txtInputFunctionExpression.fill = GridBagConstraints.HORIZONTAL;
      	gbc_txtInputFunctionExpression.gridx = 2;
      	gbc_txtInputFunctionExpression.gridy = 9;
      	frame.getContentPane().add(function, gbc_txtInputFunctionExpression);
      	function.setColumns(10);
      	function.addKeyListener(new KeyAdapter() {
      		// Plot the graph.
      		@Override
      		public void keyReleased(KeyEvent e) {
      			if (e.getKeyCode() == KeyEvent.VK_ENTER) {
      				// Limit deleted.
      				addPlot(function.getText());
      			}
      		}
      	});
 
      	// Plot button.
      	btnPlot = new JButton("Plot");
      	GridBagConstraints gbc_btnNewButton = new GridBagConstraints();
      	gbc_btnNewButton.gridwidth = 2;
      	gbc_btnNewButton.insets = new Insets(0, 0, 5, 5);
      	gbc_btnNewButton.gridx = 2;
      	gbc_btnNewButton.gridy = 10;
      	frame.getContentPane().add(btnPlot, gbc_btnNewButton);
      	btnPlot.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				addPlot(function.getText());
 			}
 		});
 
      	// Finish up.
      	frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      	frame.setVisible(true);
 		frame.pack();
 		controlsWidth = frame.getWidth() - CANVAS_INITIAL_WIDTH;
 		controlsHeight = frame.getHeight() - CANVAS_INITIAL_HEIGTH;
 		
 		// Auto resize.
 		canvasResize();
 	}
 
 	/*
 	 * Resize the plot canvas according to window resize.
 	 */
 	private void canvasResize() {
 		frame.addComponentListener(new ComponentAdapter() {
 			public void componentResized(ComponentEvent e) {
 				plotter.updateSize(frame.getWidth()- controlsWidth,frame.getHeight()- controlsHeight);
 				
 				// Er nedenstende ndvendigt ?
 				// XXX Ja, med mindre du har en bedre lsning. 
 				// Problemet er, at frame.pack() resizer vinduet. 
 				// Dvs. uden dette check opns et uendeligt loop.
 				if(!e.getSource().equals(frame) || maximized){
 					frame.pack();
 					maximized = false;
 				}
 				if(e.getSource().toString().contains("maximized")){
 					frame.pack();
 					maximized = true;
 				}
				frame.pack();
 			}
 		});
 	}
 	
 	// Just for now (custom colors will be available later?)
 	private void addPlot(String expr) {
 		addPlot(expr, Colors.BLUE);
 	}
 	
 	/*
 	 * Add new plot.
 	 */
 	private void addPlot(String expr, Color3f color) {
 		// Create the function.
 		try{
 		Function newFunc = FunctionUtil.createFunction(expr,color,DEFAULT_BOUNDS,DEFAULT_STEPSIZE);
 		newFunc.addActionListener(FunctionUtil.createActionListener(plotter));
 		functionList.add(newFunc);
 		plotter.plotFunction(newFunc);
 		noOfFunctions++;
 		frame.pack();
 		}
 		catch(ExpressionParseException e){
 			String message = e.getMessage();
 			JLabel label = new JLabel(message,JLabel.CENTER);
 			JOptionPane.showMessageDialog(frame,label);
 		}
 	}
 	
 	/*
 	 * Remove a function from the plot.
 	 */
 	private void removePlot(Function function) {
 		plotter.removePlot(function);
 	}
 
 	/*
 	 * Update a function.
 	 */
 	private void updatePlot(Function oldFunc, String newExpr, Color3f newColor, float[] bounds, float stepsize) {
 		// Try evaluating the function.
 		try {
 			Function newFunc = FunctionUtil.createFunction(newExpr, newColor, bounds, stepsize);
 			functionList.set(functionList.indexOf(oldFunc),newFunc);
 			plotter.removePlot(oldFunc);
 			plotter.plotFunction(newFunc);
 			frame.pack();
 		} 
 		// Catch error.
 		catch (ExpressionParseException e) {
 			// TODO Hvis der trykkes enter fanges den ogs af plotfeltet.
 			String message = e.getMessage();
 			JLabel label = new JLabel(message,JLabel.CENTER);
 			JOptionPane.showMessageDialog(frame,label);
 		}
 	}
 
 	/*
 	 * Delete all selected functions.
 	 */
 	private void deleteSelectedFunctions() {
 		for (int i = 0; i < functionList.size(); i++) {
 			Function f = functionList.get(i);
 			if (f.isSelected()) {
 				noOfFunctions--;
 				removePlot(f);
 				functionList.remove(i);
 				i--;
 			}
 		}
 		frame.pack();
 	}
 
 	/*
 	 * Spawn an edit dialog and process the input.
 	 */
 	private void spawnEditDialog(Function f) {
 		String curExpr = f.getExpression()[0];
 		
 		// Set up dialog.
 		JPanel inputPanel = new JPanel();
 		GridBagLayout gbl_inputPanel = new GridBagLayout();
 		gbl_inputPanel.columnWidths = new int[]{5, 193, 20, 5, 0};
 		gbl_inputPanel.rowHeights = new int[]{5, 20, 0};
 		gbl_inputPanel.columnWeights = new double[]{0.0, 0.0, 0.0, 0.0, Double.MIN_VALUE};
 		gbl_inputPanel.rowWeights = new double[]{0.0, 0.0, Double.MIN_VALUE};
 		inputPanel.setLayout(gbl_inputPanel);
 		
 		JTextField equation = new JTextField(curExpr);
 		GridBagConstraints gbc_equation = new GridBagConstraints();
 		gbc_equation.fill = GridBagConstraints.HORIZONTAL;
 		gbc_equation.anchor = GridBagConstraints.NORTH;
 		gbc_equation.insets = new Insets(0, 0, 0, 5);
 		gbc_equation.gridx = 1;
 		gbc_equation.gridy = 1;
 		inputPanel.add(equation, gbc_equation);
 		
 		JComboBox<Icon> colors = new JComboBox<Icon>(Colors.getAllColors());
 		GridBagConstraints gbc_colors = new GridBagConstraints();
 		gbc_colors.insets = new Insets(0, 0, 0, 5);
 		gbc_colors.anchor = GridBagConstraints.NORTHWEST;
 		gbc_colors.gridx = 2;
 		gbc_colors.gridy = 1;
 		inputPanel.add(colors, gbc_colors);
 		
 		JOptionPane.showMessageDialog(frame, inputPanel, "Edit Function", JOptionPane.PLAIN_MESSAGE, null);
 		
 		// Update function in case of changes.
 		ColorIcon selectedIcon = (ColorIcon) colors.getSelectedItem();
 		String newExpr = equation.getText();
 		Color3f newColor = selectedIcon.getColor();
 		// TODO: Implement the option to change bounds and stepsize.
 		if (!curExpr.equals(newExpr)) {
 			updatePlot(f, newExpr, newColor, DEFAULT_BOUNDS, DEFAULT_STEPSIZE);
 		} else if (newColor != null && !f.getColor().equals(newColor)) {
 			functionList.get(functionList.indexOf(f)).setColor(newColor);
 		}
 	}
 }
