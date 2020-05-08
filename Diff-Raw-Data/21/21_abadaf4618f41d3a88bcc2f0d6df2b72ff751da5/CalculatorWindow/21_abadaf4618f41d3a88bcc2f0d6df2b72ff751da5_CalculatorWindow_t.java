 /*
  * Copyright 2012 Justin Wilcox
  * 
  * This file is part of GraphingCalculator.
  *
  * GraphingCalculator is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * GraphingCalculator is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with GraphingCalculator.  If not, see <http://www.gnu.org/licenses/>.
  */
 package gui;
 
 import java.awt.BorderLayout;
 import java.awt.EventQueue;
 
 import javax.swing.JFrame;
 import javax.swing.JComboBox;
 import javax.swing.JTextField;
 import javax.swing.JLabel;
 import javax.swing.JButton;
 import javax.swing.JTabbedPane;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.BoxLayout;
 import javax.swing.UIManager;
 import java.awt.Font;
 import java.awt.Color;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.util.ArrayList;
 import java.util.StringTokenizer;
 
 import javax.swing.JScrollPane;
import javax.swing.plaf.metal.MetalLookAndFeel;
 
 import calculus.Derivatives;
 import calculus.Integration;
 
 import threads.BisectionThread;
 import threads.CalculationThread;
 import threads.FalsePositionThread;
 import threads.ModifiedFalsePositionThread;
 import threads.ModifiedSecantThread;
 import threads.SecantThread;
 
 import functions.Function;
 import functions.FunctionArguments;
 import functions.FunctionStore;
 import functions.MalformedFunctionException;
 import functions.TokenizedFunctionFactory;
 
 public class CalculatorWindow {
     /** GUI Elements */
     private JFrame frame;
     private JTextField expressionBox;
     private JTextField input2Box;
     private JTextField input3Box;
     private JTextField input4Box;
     private JTextArea textOutput;
     private JComboBox actionBox;
     private JLabel input1Label;
     private JComboBox input1Box;
     private JLabel input2Label;
     private JLabel input3Label;
     private JLabel input4Label;
     private JButton btnEvaluate;
     private JButton btnClearConsole;
     private JButton btnOptions;
     private JPanel graphPanel;
     private JTabbedPane tabbedPane;
     private JButton btnContinue;
     private Options optionFrame;
 
     /** Action listener */
     private CalculatorWindowListener listen;
 
     /** The graph */
     private Graph graph;
 
     /** Separate thread for iterative calculations */
     private CalculationThread otherThread;
 
     /**
      * Launch the application.
      */
     public static void main(String[] args) {
         EventQueue.invokeLater(new Runnable() {
             public void run() {
                 try {
                    UIManager.put("Button.font", new Font(Font.SANS_SERIF, 0, 10));
                    UIManager.put("Label.font", new Font(Font.SANS_SERIF, 0, 10));
                    UIManager.setLookAndFeel(new MetalLookAndFeel());
 
                     CalculatorWindow window = new CalculatorWindow();
                     window.frame.setVisible(true);
 		    
                 } catch (Exception e) {
                     e.printStackTrace();
                     System.out.println("Crashed");
                 }
             }
         });
     }
 
     /**
      * Create the application.
      */
     public CalculatorWindow() {
         initialize();
     }
 
     /**
      * Initialize the contents of the frame.
      */
     private void initialize() {
         frame = new JFrame();
         frame.setResizable(false);
         frame.setBounds(100, 100, 751, 567);
         frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
         frame.getContentPane().setLayout(null);
 
         actionBox = new JComboBox();
         actionBox.setBounds(55, 9, 103, 20);
         frame.getContentPane().add(actionBox);
 
         expressionBox = new JTextField();
         expressionBox.setText("Expression");
         expressionBox.setBounds(245, 9, 489, 20);
         frame.getContentPane().add(expressionBox);
         expressionBox.setColumns(10);
 
         JLabel lblAction = new JLabel("Action:");
         lblAction.setBounds(10, 11, 41, 16);
         frame.getContentPane().add(lblAction);
 
         JLabel lblExpression = new JLabel("Expression:");
         lblExpression.setBounds(168, 11, 67, 16);
         frame.getContentPane().add(lblExpression);
 
         input1Label = new JLabel("Input 1:");
         input1Label.setBounds(10, 40, 41, 16);
         frame.getContentPane().add(input1Label);
 
         input1Box = new JComboBox();
         input1Box.setBounds(55, 38, 103, 20);
         frame.getContentPane().add(input1Box);
 
         input2Label = new JLabel("Input 2:");
         input2Label.setBounds(168, 40, 41, 16);
         frame.getContentPane().add(input2Label);
 
         input2Box = new JTextField();
         input2Box.setText("Input2");
         input2Box.setBounds(216, 38, 142, 20);
         frame.getContentPane().add(input2Box);
         input2Box.setColumns(10);
 
         input3Label = new JLabel("Input 3:");
         input3Label.setBounds(363, 39, 41, 16);
         frame.getContentPane().add(input3Label);
 
         input3Box = new JTextField();
         input3Box.setText("Input3");
         input3Box.setBounds(411, 38, 135, 20);
         frame.getContentPane().add(input3Box);
         input3Box.setColumns(10);
 
         input4Label = new JLabel("Input 4:");
         input4Label.setBounds(549, 40, 47, 16);
         frame.getContentPane().add(input4Label);
 
         input4Box = new JTextField();
         input4Box.setText("Input4");
         input4Box.setBounds(599, 38, 135, 20);
         frame.getContentPane().add(input4Box);
         input4Box.setColumns(10);
 
         btnEvaluate = new JButton("Evaluate");
         btnEvaluate.setBounds(637, 63, 98, 20);
         frame.getContentPane().add(btnEvaluate);
 
         btnClearConsole = new JButton("Clear");
         btnClearConsole.setBounds(516, 63, 117, 20);
         frame.getContentPane().add(btnClearConsole);
 
         btnOptions = new JButton("Options");
         btnOptions.setActionCommand("EXTERMINATE");
         btnOptions.setBounds(421, 63, 87, 20);
         frame.getContentPane().add(btnOptions);
 
         btnContinue = new JButton("Continue");
         btnContinue.setActionCommand("EXTERMINATE");
         btnContinue.setBounds(327, 63, 87, 20);
         frame.getContentPane().add(btnContinue);
 
         tabbedPane = new JTabbedPane(JTabbedPane.TOP);
         tabbedPane.setBounds(10, 67, 724, 464);
         frame.getContentPane().add(tabbedPane);
 
         JPanel panel = new JPanel();
         tabbedPane.addTab("Output", null, panel, null);
         panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
 
         JScrollPane scrollPane = new JScrollPane();
         panel.add(scrollPane);
 
         textOutput = new JTextArea();
         scrollPane.setViewportView(textOutput);
         textOutput.setEditable(false);
         textOutput.setText("Text output");
 
         graphPanel = new JPanel();
         graphPanel.setBackground(Color.WHITE);
         tabbedPane.addTab("Graph", null, graphPanel, null);
 
         myInitialize();
     }
 
     /**
      * Init my components
      */
     private void myInitialize() {
         optionFrame = new Options(this);
 
         frame.setTitle("Graphing Calculator");
         graph = new Graph();
 
         graphPanel.setLayout(new BorderLayout(0, 0));
         graphPanel.add(graph, BorderLayout.CENTER);
         btnEvaluate.addActionListener(graph);
         btnContinue.addActionListener(graph);
         btnClearConsole.addActionListener(graph);
 
         populateActionBox();
         disableInputs();
         clearInputs();
         attachListener();
         clearOutput();
         displayOutput("Initialized\n");
         graph.setDefaultWindow(new GraphWindow(-10.0, 10.0, -10.0, 10.0, 719,
                 436));
         graph.defaultWindow();
     }
 
     /**
      * Sets the action commands for the GUI components
      */
     private void attachListener() {
         listen = new CalculatorWindowListener();
 
         actionBox.setActionCommand("ACTION");
         actionBox.addActionListener(listen);
 
         btnEvaluate.setActionCommand("EVALUATE");
         btnEvaluate.addActionListener(listen);
 
         btnClearConsole.setActionCommand("EXTERMINATE");
         btnClearConsole.addActionListener(listen);
 
         btnContinue.setActionCommand("CONTINUE");
         btnContinue.addActionListener(listen);
         btnContinue.setEnabled(false);
 
         btnOptions.setActionCommand("OPTIONS");
         btnOptions.addActionListener(listen);
 
         input1Box.setActionCommand("INPUT1");
         input1Box.addActionListener(listen);
     }
 
     private void populateActionBox() {
         actionBox.removeAllItems();
         actionBox.addItem("Evaluate");
         actionBox.addItem("Def. Function");
         actionBox.addItem("Graph");
         actionBox.addItem("Zero");
         actionBox.addItem("Derivative");
         actionBox.addItem("Integral");
     }
 
     private void disableInputs() {
         input1Box.setVisible(false);
         input2Box.setVisible(false);
         input3Box.setVisible(false);
         input4Box.setVisible(false);
 
         input1Label.setVisible(false);
         input2Label.setVisible(false);
         input3Label.setVisible(false);
         input4Label.setVisible(false);
     }
 
     private void clearInputs() {
         input1Box.removeAllItems();
         input1Box.setToolTipText("");
         input2Box.setText("");
         input2Box.setToolTipText("");
         input3Box.setText("");
         input3Box.setToolTipText("");
         input4Box.setText("");
         input4Box.setToolTipText("");
 
         input1Label.setText("");
         input1Label.setToolTipText("");
         input2Label.setText("");
         input2Label.setToolTipText("");
         input3Label.setText("");
         input3Label.setToolTipText("");
         input4Label.setText("");
         input4Label.setToolTipText("");
 
         // expressionBox.setText("");
     }
 
     private void clearOutput() {
         textOutput.setText("");
     }
 
     public void displayOutput(String output) {
         textOutput.append(output);
     }
 
     public void updateOptions() {
         graph.setPlotResolution((int) optionFrame.getParam("PlotResolution"));
         GraphWindow gw = new GraphWindow(optionFrame.getParam("Xmin"),
                 optionFrame.getParam("Xmax"), optionFrame.getParam("Ymin"),
                 optionFrame.getParam("Ymax"), graph.getWidth(),
                 graph.getHeight());
         graph.setDefaultWindow(gw);
         graph.scheduleRepaint();
     }
 
     private class CalculatorWindowListener implements ActionListener {
         /** What action state are we in */
         private String state;
 
         public CalculatorWindowListener() {
             state = "Evaluate";
         }
 
         @SuppressWarnings("deprecation")
         /**
          * Perform command
          */
         public void actionPerformed(ActionEvent arg0) {
             String cmd = arg0.getActionCommand();
             if (cmd.equals("CONTINUE")) {
                 if (otherThread != null) {
                     if (otherThread.hasResults() == false) {
                         otherThread.resume();
                     } else {
                         finishZerosRun();
                     }
                 }
             } else if (cmd.equals("EVALUATE")) {
                 if (state.equals("Zero")) {
                     evalZeroState();
                 } else if (state.equals("Evaluate")) {
                     evalEvaluateState();
                 } else if (state.equals("Graph")) {
                     evalGraphState();
                 } else if (state.equals("Def. Function")) {
                     evalDefState();
                 } else if (state.equals("Derivative")) {
                     evalDerivativeState();
                 } else if (state.equals("Integral")) {
                     evalIntegralState();
                 }
             } else if (cmd.equals("EXTERMINATE")) {
                 clearOutput();
             } else if (cmd.equals("ACTION")) {
                 String action = (String) actionBox.getSelectedItem();
                 if (action.equals("Evaluate")) {
                     enterEvaluateState();
                 } else if (action.equals("Zero")) {
                     enterZeroState();
                 } else if (action.equals("Def. Function")) {
                     enterDefState();
                 } else if (action.equals("Graph")) {
                     enterGraphState();
                 } else if (action.equals("Derivative")) {
                     enterDerivativeState();
                 } else if (action.equals("Integral")) {
                     enterIntegralState();
                 }
             } else if (cmd.equals("INPUT1")) {
                 if (state.equals("Zero")) {
                     reprocessZeroState();
                 }
             } else if (cmd.equals("OPTIONS")) {
                 optionFrame.setVisible(true);
             }
         }
 
         private void evalIntegralState() {
             try {
                 graph.defaultWindow();
                 ArrayList<String> vars = new ArrayList<String>();
 
                 vars.add(input2Box.getText().trim());
                 Function f = TokenizedFunctionFactory.createFunction(
                         expressionBox.getText(), vars);
 
                 double val = 0.0;
                 String meth = (String) input1Box.getSelectedItem();
                 if (meth.equals("Midpoint")) {
                     val = Integration.midpointRectangle(f,
                             Double.parseDouble(input3Box.getText()),
                             Double.parseDouble(input4Box.getText()),
                             (int) optionFrame.getParam("Division"), graph);
                 } else if (meth.equals("Trapezoid")) {
                     val = Integration.trapezoidRule(f,
                             Double.parseDouble(input3Box.getText()),
                             Double.parseDouble(input4Box.getText()),
                             (int) optionFrame.getParam("Division"), graph);
                 } else if (meth.equals("Romberg")) {
                     val = Integration.rombergsRule(f,
                             Double.parseDouble(input3Box.getText()),
                             Double.parseDouble(input4Box.getText()),
                             (int) optionFrame.getParam("Romberg"));
                 } else if (meth.equals("Simpson")) {
                     val = Integration.simpsonsRule(f,
                             Double.parseDouble(input3Box.getText()),
                             Double.parseDouble(input4Box.getText()),
                             (int) optionFrame.getParam("Division"));
                 }
 
                 displayOutput("Integral: " + val + "\n");
             } catch (MalformedFunctionException e) {
                 displayOutput("Malformed expression: " + e.getMessage() + "\n");
             } catch (NumberFormatException e) {
                 displayOutput("Invalid numerical input\n");
             }
         }
 
         private void evalDerivativeState() {
             try {
                 ArrayList<String> vars = new ArrayList<String>();
                 vars.add(input2Box.getText().trim());
                 Function f = TokenizedFunctionFactory.createFunction(
                         expressionBox.getText(), vars);
                 double diff = 0.0;
                 String meth = (String) input1Box.getSelectedItem();
                 if (meth.equals("Forward Diff.")) {
                     diff = Derivatives.forwardDifference(f,
                             Double.parseDouble(input3Box.getText()),
                             optionFrame.getParam("DerivStep"));
                 } else if (meth.equals("Backward Diff.")) {
                     diff = Derivatives.backwardDifference(f,
                             Double.parseDouble(input3Box.getText()),
                             optionFrame.getParam("DerivStep"));
                 } else if (meth.equals("Centered Diff.")) {
                     diff = Derivatives.centeredDifference(f,
                             Double.parseDouble(input3Box.getText()),
                             optionFrame.getParam("DerivStep"));
                 }
                 displayOutput("Derivative: " + diff + "\n");
             } catch (MalformedFunctionException e) {
                 displayOutput("Could not evaluate: " + e.getMessage() + "\n");
             } catch (NumberFormatException e) {
                 displayOutput("Invalid numerical input\n");
             }
         }
 
         private void evalDefState() {
             try {
                 ArrayList<String> vars = new ArrayList<String>();
                 String type = (String) input1Box.getSelectedItem();
                 if (type.equals("Single")) {
                     vars.add(input3Box.getText().trim());
                 } else if (type.equals("Multiple")) {
                     StringTokenizer tok = new StringTokenizer(input3Box
                             .getText().trim(), ",", false);
                     while (tok.hasMoreTokens()) {
                         vars.add(tok.nextToken());
                     }
                     if(vars.size() < 2) {
                         displayOutput("Invalid variable list " + input3Box.getText() + "\n");
                         return;
                     }
                 }
 
                 String name = input2Box.getText().trim();
                 if (name.length() == 0
                         || FunctionStore.getStore().hasFunction(name)) {
                     displayOutput("Invalid Function name.");
                     return;
                 }
                 FunctionStore.getStore().storeFunction(
                         name,
                         TokenizedFunctionFactory.createFunction(
                                 expressionBox.getText(), vars));
                 displayOutput("Function " + name + " stored with definition "
                         + expressionBox.getText() + "\n");
             } catch (MalformedFunctionException e) {
                 displayOutput("Malformed expression: " + e.getMessage() + "\n");
             }
         }
 
         private void enterIntegralState() {
             disableInputs();
             clearInputs();
 
             input1Box.setVisible(true);
             input1Box.addItem("Midpoint");
             input1Box.addItem("Trapezoid");
             input1Box.addItem("Simpson");
             input1Box.addItem("Romberg");
             input1Label.setVisible(true);
             input1Label.setText("Meth.:");
 
             input2Box.setVisible(true);
             input2Label.setVisible(true);
             input2Box.setText("x");
             input2Label.setText("Var.:");
 
             input3Box.setVisible(true);
             input3Label.setVisible(true);
             input3Label.setText("Low:");
 
             input4Box.setVisible(true);
             input4Label.setVisible(true);
             input4Label.setText("High:");
 
             state = "Integral";
         }
 
         private void enterDerivativeState() {
             disableInputs();
             clearInputs();
 
             input1Box.setVisible(true);
             input1Box.addItem("Forward Diff.");
             input1Box.addItem("Backward Diff.");
             input1Box.addItem("Centered Diff.");
             input1Label.setVisible(true);
             input1Label.setText("Meth.:");
 
             input2Box.setVisible(true);
             input2Label.setVisible(true);
             input2Box.setText("x");
             input2Label.setText("Vars.:");
 
             input3Box.setVisible(true);
             input3Label.setVisible(true);
             input3Label.setText("Point:");
 
             state = "Derivative";
         }
 
         private void enterGraphState() {
             disableInputs();
             clearInputs();
 
             input2Label.setVisible(true);
             input2Label.setText("Var.:");
             input2Box.setVisible(true);
             input2Box.setText("x");
 
             state = "Graph";
         }
 
         private void evalGraphState() {
             graph.defaultWindow();
             graph.clearFunctions();
             graph.clearLines();
             graph.clearPoints();
             graph.setNotification("");
 
             Function f = null;
             try {
                 ArrayList<String> vars = new ArrayList<String>();
                 vars.add(input2Box.getText().trim());
                 f = TokenizedFunctionFactory.createFunction(
                         expressionBox.getText(), vars);
             } catch (MalformedFunctionException e) {
                 displayOutput("Could not evaluate function: " + e.getMessage());
                 return;
             }
 
             graph.addPlot(f, Color.BLACK);
         }
 
         private void evalEvaluateState() {
             try {
                 Function f = TokenizedFunctionFactory.createFunction(
                         expressionBox.getText(), null);
                 displayOutput("Evaluation: "
                         + f.evaluate(new FunctionArguments(null)) + "\n");
             } catch (MalformedFunctionException e) {
                 displayOutput(e.getMessage() + "\n");
             }
         }
 
         @SuppressWarnings("deprecation")
         private void evalZeroState() {
             if (otherThread == null) {
                 graph.defaultWindow();
 
                 graph.setNotification("");
 
                 ArrayList<String> vars = new ArrayList<String>();
                 vars.add(input4Box.getText());
 
                 Function f = TokenizedFunctionFactory.createFunction(
                         expressionBox.getText(), vars);
                 String method = (String) input1Box.getSelectedItem();
                 CalculationThread t = null;
 
                 try {
                     if (method.equals("Bisection")) {
                         t = new BisectionThread(f, Double.parseDouble(input3Box
                                 .getText()), Double.parseDouble(input2Box
                                 .getText()),
                                 optionFrame.getParam("StoppingThreshold"),
                                 (int) optionFrame.getParam("MaxIterations"),
                                 graph);
                     } else if (method.equals("False Position")) {
                         t = new FalsePositionThread(f,
                                 Double.parseDouble(input3Box.getText()),
                                 Double.parseDouble(input2Box.getText()),
                                 optionFrame.getParam("StoppingThreshold"),
                                 (int) optionFrame.getParam("MaxIterations"),
                                 graph);
                     } else if (method.equals("Secant")) {
                         t = new SecantThread(f, Double.parseDouble(input3Box
                                 .getText()), Double.parseDouble(input2Box
                                 .getText()),
                                 optionFrame.getParam("StoppingThreshold"),
                                 (int) optionFrame.getParam("MaxIterations"),
                                 graph);
                     } else if (method.equals("Modified Secant")) {
                         t = new ModifiedSecantThread(f,
                                 Double.parseDouble(input2Box.getText()),
                                 optionFrame.getParam("ModifiedSecantStep"),
                                 optionFrame.getParam("StoppingThreshold"),
                                 (int) optionFrame.getParam("MaxIterations"),
                                 graph);
                     } else if (method.equals("Modified F.P.")) {
                         t = new ModifiedFalsePositionThread(f,
                                 Double.parseDouble(input3Box.getText()),
                                 Double.parseDouble(input2Box.getText()),
                                 optionFrame.getParam("StoppingThreshold"),
                                 (int) optionFrame.getParam("MaxIterations"),
                                 graph);
                     }
                     displayOutput("Method: " + method + "\n");
                 } catch (NumberFormatException e) {
                     displayOutput("You must enter valid constraints\n");
                     return;
                 }
 
                 if (t == null) {
                     displayOutput("There was an error starting the zero computation\n");
                     return;
                 }
 
                 t.start();
                 otherThread = t;
                 btnEvaluate.setText("Finish");
                 btnContinue.setEnabled(true);
 
                 disableInputs();
                 actionBox.setEnabled(false);
             } else {
                 while (!otherThread.hasResults()) {
                     otherThread.resume();
                 }
                 finishZerosRun();
             }
         }
 
         private void finishZerosRun() {
             displayOutput(otherThread.getResultsString());
             otherThread = null;
             btnEvaluate.setText("Evaluate");
             btnContinue.setEnabled(false);
 
             enterZeroState();
             actionBox.setEnabled(true);
         }
 
         private void enterDefState() {
             clearInputs();
             disableInputs();
 
             input1Box.addItem("Single");
             input1Box.addItem("Multiple");
             input1Label.setText("Vars.:");
             input1Label.setVisible(true);
             input1Box.setVisible(true);
 
             input2Label.setText("Name:");
             input2Box.setText("");
             input2Label.setVisible(true);
             input2Box.setVisible(true);
 
             input3Label.setText("Var(s):");
             input3Label.setVisible(true);
             input3Box.setVisible(true);
             state = "Def. Function";
         }
 
         private void enterZeroState() {
             clearInputs();
             disableInputs();
 
             input1Box.addItem("Bisection");
             input1Box.addItem("False Position");
             input1Box.addItem("Modified F.P.");
             input1Box.addItem("Secant");
             input1Box.addItem("Modified Secant");
             input1Box.setVisible(true);
             input1Label.setText("Meth.:");
             input1Label.setVisible(true);
 
             input2Box.setVisible(true);
             input2Label.setText("Low:");
             input2Label.setVisible(true);
 
             input3Label.setVisible(true);
             input3Label.setText("High:");
             input3Box.setVisible(true);
 
             input4Label.setVisible(true);
             input4Label.setText("Var.:");
             input4Box.setText("x");
             input4Box.setVisible(true);
 
             state = "Zero";
         }
 
         private void reprocessZeroState() {
             String selected = (String) input1Box.getSelectedItem();
             if (selected == null) {
                 return;
             }
             if (selected.equals("Modified Secant")) {
                 input2Label.setText("Start:");
 
                 input3Label.setVisible(false);
                 input3Box.setVisible(false);
             } else if (selected.equals("Secant")) {
                 input2Label.setText("X1:");
                 input2Label.setVisible(true);
 
                 input3Label.setVisible(true);
                 input3Label.setText("X2:");
                 input3Box.setVisible(true);
             } else {
                 input2Label.setText("Low:");
                 input2Label.setVisible(true);
 
                 input3Label.setVisible(true);
                 input3Label.setText("High:");
                 input3Box.setVisible(true);
             }
         }
 
         private void enterEvaluateState() {
             clearInputs();
             disableInputs();
             state = "Evaluate";
         }
 
     }
 }
