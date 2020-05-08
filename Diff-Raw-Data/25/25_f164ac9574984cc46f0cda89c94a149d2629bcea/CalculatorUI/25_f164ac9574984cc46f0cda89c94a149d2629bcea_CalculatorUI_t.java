 package at.ac.prog.calculator.ui;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.WindowEvent;
 import java.awt.event.WindowListener;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import at.ac.prog.calculator.engine.CalcExecutor;
 import at.ac.prog.calculator.engine.exception.CalcParsingException;
 import at.ac.prog.calculator.engine.util.CalcExecutorListener;
 
 public class CalculatorUI extends JFrame implements WindowListener,
 		CalcExecutorListener {
 
 	private static final long serialVersionUID = 986427844864093227L;
 	private JTextField inputTextField;
 	private JTextArea inputTextArea;
 	private JTextArea outputTextArea;
 	private JTextArea stackTextArea;
 	private JTextArea inputListTextArea;
 	private CalcExecutor executor;
 
 	private JButton debugStepButton, runButton, debugButton;
 	private JTextArea textArea;
 	private boolean isQuestionMarkOperator = false;
 
 	public CalculatorUI() {
 		this.setTitle("Programmable Calculator");
 		this.addWindowListener(this);
 		this.setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
 
 		GridBagLayout gridBagLayout = new GridBagLayout();
 		gridBagLayout.columnWidths = new int[] { 0, 0, 0, 0, 0 };
 		gridBagLayout.rowWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0 };
 		gridBagLayout.rowHeights = new int[] { 0, 0, 0, 0, 0, 0, 50 };
 		gridBagLayout.columnWeights = new double[] { 0.0, 0.0, 0.0, 0.0, 1.0 };
 		getContentPane().setLayout(gridBagLayout);
 		this.setMinimumSize(new Dimension(920, 480));
 
 		this.initalizeMenu();
 		this.initializeComponents();
 		this.setVisible(true);
 	}
 
 	public void init(CalcExecutor executor) {
 		this.executor = executor;
 	}
 
 	private void initalizeMenu() {
 
 		JMenuBar menuBar = new JMenuBar();
 		JMenu fileMenu = new JMenu("File");
 
 		JMenuItem enableDebugMItem = new JMenuItem("Toggle Debug");
 		enableDebugMItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				CalculatorUI.this.executor.toggleDebug();
 				CalculatorUI.this.debugStepButton
 						.setEnabled(!CalculatorUI.this.debugStepButton
 								.isEnabled());
 			}
 		});
 
 		JMenuItem clearStackMItem = new JMenuItem("Clear Stack");
 		clearStackMItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				CalculatorUI.this.executor.clearStack();
 				CalculatorUI.this.stackTextArea.setText("");
 			}
 		});
 
 		JMenuItem clearInputAreaMItem = new JMenuItem("Clear Input Area");
 		clearInputAreaMItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				CalculatorUI.this.inputTextArea.setText("");
 			}
 		});
 
 		JMenuItem clearOutputAreaMItem = new JMenuItem("Clear Output Area");
 		clearOutputAreaMItem.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent ae) {
 				CalculatorUI.this.outputTextArea.setText("");
 			}
 		});
 
 		fileMenu.add(enableDebugMItem);
 		fileMenu.add(clearStackMItem);
 		fileMenu.add(clearInputAreaMItem);
 		fileMenu.add(clearOutputAreaMItem);
 		menuBar.add(fileMenu);
 
 		this.setJMenuBar(menuBar);
 	}
 
 	private void initializeComponents() {
 
 		this.inputTextArea = new JTextArea();
 		this.inputTextArea.setEditable(false);
 		JScrollPane inputTextAreaScrollPane = new JScrollPane(
 				this.inputTextArea);
 
 		JLabel inputTextLabel = new JLabel("Input: ");
 
 		this.inputTextField = new JTextField();
 		this.inputTextField.setEditable(true);
 		this.inputTextField.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				runInput();
 			}
 		});
 
 		this.initializeDebugStepButton();
 
 		this.outputTextArea = new JTextArea();
 		this.outputTextArea.setEditable(false);
 		JScrollPane outputTextAreaScrollPane = new JScrollPane(
 				this.outputTextArea);
 
 		GridBagConstraints c, d, e, g, h;
 
 		JPanel panel = new JPanel();
 		FlowLayout flowLayout = (FlowLayout) panel.getLayout();
 		flowLayout.setAlignment(FlowLayout.LEFT);
 		GridBagConstraints gbc_panel = new GridBagConstraints();
 		gbc_panel.fill = GridBagConstraints.HORIZONTAL;
 		gbc_panel.anchor = GridBagConstraints.NORTH;
 		gbc_panel.gridwidth = 5;
 		gbc_panel.insets = new Insets(0, 0, 5, 0);
 		gbc_panel.gridx = 0;
 		gbc_panel.gridy = 0;
 		getContentPane().add(panel, gbc_panel);
 
 		JButton btnClearAll = new JButton("Clear All");
 		btnClearAll.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				resetCalculator();
 			}
 		});
 		panel.add(btnClearAll);
 
 		JButton btnLoadPrimeTest = new JButton("Load Prime Test");
 		btnLoadPrimeTest.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				CalculatorUI.this.inputTextField
 						.setText("[[1+ 2! 4! % 0= 6+!@][3!'\\ \" = 2+ # \" \\n \" 2# 2#][Prime][Not Prime] 33 1 7! @] 2! @");
 			}
 		});
 		panel.add(btnLoadPrimeTest);
 
 		c = new GridBagConstraints();
 		c.anchor = GridBagConstraints.LINE_START;
 		c.fill = GridBagConstraints.BOTH;
 		c.insets = new Insets(10, 10, 5, 10);
 		c.gridx = 0;
 		c.gridy = 1;
 		c.weightx = 1;
 		c.weighty = 0.4;
 		c.gridwidth = 4;
 		getContentPane().add(inputTextAreaScrollPane, c);
 
 		d = new GridBagConstraints();
 		d.fill = GridBagConstraints.NONE;
 		d.insets = new Insets(0, 10, 5, 5);
 		d.gridx = 0;
 		d.gridy = 2;
 		d.weightx = 0.0;
 		d.weighty = 0.1;
 		d.gridwidth = 1;
 		getContentPane().add(inputTextLabel, d);
 
 		e = new GridBagConstraints();
 		e.fill = GridBagConstraints.HORIZONTAL;
 		e.insets = new Insets(0, 5, 5, 10);
 		e.gridx = 1;
 		e.gridy = 2;
 		e.weightx = 0.9;
 		e.weighty = 0.1;
 		e.gridwidth = 3;
 		getContentPane().add(inputTextField, e);
 
 		runButton = new JButton("Run");
 		runButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(CalculatorUI.this.executor.isbDebug()) {
 					// in the special case, that we were in debug mode before and now want to
 					// finish the calculation, we have to leave debug mode, disable the nextStep
 					// button and then run execute()
 					CalculatorUI.this.executor.setbDebug(false);
 					CalculatorUI.this.debugStepButton.setEnabled(false);
 					try {
 						CalculatorUI.this.executor.execute();
 					} catch (CalcParsingException e1) {
 						setErrorState(e1.getLocalizedMessage());
 					} catch (IllegalArgumentException e1) {
 						setErrorState(e1.getLocalizedMessage());
					} catch (Exception e1) {
						setErrorState(e1.getLocalizedMessage());
 					}
 				} else {
 					runInput();
 				}
 			}
 		});
 		GridBagConstraints gbc_btnRun = new GridBagConstraints();
 		gbc_btnRun.insets = new Insets(0, 0, 5, 5);
 		gbc_btnRun.gridx = 1;
 		gbc_btnRun.gridy = 3;
 		getContentPane().add(runButton, gbc_btnRun);
 
 		debugButton = new JButton("Debug");
 		debugButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				CalculatorUI.this.executor.setbDebug(true);
 				CalculatorUI.this.debugStepButton.setEnabled(true);
 				runInput();
 			}
 		});
 		GridBagConstraints gbc_btnDebug = new GridBagConstraints();
 		gbc_btnDebug.insets = new Insets(0, 0, 5, 5);
 		gbc_btnDebug.gridx = 2;
 		gbc_btnDebug.gridy = 3;
 		getContentPane().add(debugButton, gbc_btnDebug);
 
 		g = new GridBagConstraints();
 		g.weightx = 1.0;
 		g.fill = GridBagConstraints.BOTH;
 		g.insets = new Insets(5, 10, 10, 10);
 		g.gridx = 0;
 		g.gridy = 4;
 		g.weighty = 0.5;
 		g.gridwidth = 4;
 		getContentPane().add(outputTextAreaScrollPane, g);
 
 		h = new GridBagConstraints();
 		h.fill = GridBagConstraints.BOTH;
 		h.insets = new Insets(10, 10, 10, 10);
 		h.gridx = 4;
 		h.gridy = 1;
 		h.weightx = 0.3;
 		h.weighty = 1;
 		h.gridwidth = 1;
 		h.gridheight = 4;
 		getContentPane().add(this.createExecutorLoggingPanel(), h);
 
 		JLabel lblStatus = new JLabel("Status:");
 		GridBagConstraints gbc_lblStatus = new GridBagConstraints();
 		gbc_lblStatus.anchor = GridBagConstraints.SOUTHWEST;
 		gbc_lblStatus.insets = new Insets(0, 10, 5, 0);
 		gbc_lblStatus.gridx = 0;
 		gbc_lblStatus.gridy = 5;
 		getContentPane().add(lblStatus, gbc_lblStatus);
 
 		textArea = new JTextArea();
 		textArea.setFont(new Font("Dialog", Font.BOLD, 12));
 		textArea.setMargin(new Insets(6, 6, 6, 6));
 		textArea.setEditable(false);
 		textArea.setDoubleBuffered(true);
 		GridBagConstraints gbc_textArea = new GridBagConstraints();
 		gbc_textArea.gridwidth = 5;
 		gbc_textArea.insets = new Insets(0, 10, 10, 10);
 		gbc_textArea.fill = GridBagConstraints.BOTH;
 		gbc_textArea.gridx = 0;
 		gbc_textArea.gridy = 6;
 		getContentPane().add(textArea, gbc_textArea);
 	}
 
 	private JPanel createExecutorLoggingPanel() {
 		JPanel panel = new JPanel();
 		panel.setMinimumSize(new Dimension(300, 10));
 		panel.setLayout(new GridBagLayout());
 
 		this.inputListTextArea = new JTextArea();
 		inputListTextArea.setMinimumSize(new Dimension(200, 15));
 		this.inputListTextArea.setEditable(false);
 		JScrollPane inputListTextAreaScrollPane = new JScrollPane(
 				this.inputListTextArea);
 
 		this.stackTextArea = new JTextArea();
 		stackTextArea.setMinimumSize(new Dimension(200, 15));
 		this.stackTextArea.setEditable(false);
 		JScrollPane stackTextAreaScrollPane = new JScrollPane(
 				this.stackTextArea);
 
 		GridBagConstraints i, j;
 
 		i = new GridBagConstraints();
 		i.fill = GridBagConstraints.BOTH;
 		i.insets = new Insets(0, 0, 0, 0);
 		i.gridx = 0;
 		i.gridy = 0;
 		i.weightx = 1;
 		i.weighty = 1;
 		i.gridwidth = 1;
 		panel.add(inputListTextAreaScrollPane, i);
 
 		j = new GridBagConstraints();
 		j.fill = GridBagConstraints.BOTH;
 		j.insets = new Insets(10, 0, 0, 0);
 		j.gridx = 0;
 		j.gridy = 1;
 		j.weightx = 1;
 		j.weighty = 1;
 		j.gridwidth = 1;
 		panel.add(stackTextAreaScrollPane, j);
 
 		return panel;
 	}
 
 	private void initializeDebugStepButton() {
 		this.debugStepButton = new JButton("Next Step");
 		this.debugStepButton.setEnabled(false);
 		debugStepButton.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				CalculatorUI.this.nextProcessStep();
 			}
 		});
 
 		GridBagConstraints gbc_debugStepButton = new GridBagConstraints();
 		gbc_debugStepButton.anchor = GridBagConstraints.EAST;
 		gbc_debugStepButton.insets = new Insets(0, 0, 5, 5);
 		gbc_debugStepButton.gridx = 3;
 		gbc_debugStepButton.gridy = 3;
 		getContentPane().add(debugStepButton, gbc_debugStepButton);
 	}
 
 	private void nextProcessStep() {
 		try {
 			this.executor.execute();
 		} catch (CalcParsingException e) {
 			setErrorState(e.getLocalizedMessage());
 		} catch (IllegalArgumentException e) {
 			setErrorState(e.getLocalizedMessage());
		} catch (Exception e1) {
			setErrorState(e1.getLocalizedMessage());
 		}
 	}
 
 	private void addOutputMessage(String message) {
 		this.outputTextArea.append(message);
 	}
 
 	private int getValidationDialogResult() {
 		return (JOptionPane.showConfirmDialog(this, "Really Quit?", "GUI",
 				JOptionPane.OK_CANCEL_OPTION));
 	}
 
 	private void initializeShutdown() {
 		if (JOptionPane.OK_OPTION == getValidationDialogResult()) {
 			// TODO: close APP
 			System.exit(0);
 		}
 	}
 
 	private void runInput() {
 		String input = CalculatorUI.this.inputTextField.getText();
 		if (!input.equals("")) {
 			CalculatorUI.this.processNewParseInput(input);
 		}
 	}
 
 	private void processNewParseInput(String input) {
 		this.inputTextField.setText("");
 		this.inputTextField.setEnabled(false);
 		this.inputTextArea.append(input + "\n");
 		this.inputTextArea.setEditable(false);
 		if (isQuestionMarkOperator) {
 			input = "[" + input + "]";
 			isQuestionMarkOperator = false;
 		}
 		try {
 			this.executor.parse(input);
 			this.executor.execute();
 		} catch (CalcParsingException e) {
 			setErrorState(e.getLocalizedMessage());
 		} catch (IllegalArgumentException e) {
 			setErrorState(e.getLocalizedMessage());
		} catch (Exception e1) {
			setErrorState(e1.getLocalizedMessage());
 		}
 	}
 
 	@Override
 	public void windowClosing(WindowEvent e) {
 		this.initializeShutdown();
 	}
 
 	@Override
 	public void windowDeactivated(WindowEvent e) {
 	}
 
 	@Override
 	public void windowDeiconified(WindowEvent e) {
 	}
 
 	@Override
 	public void windowIconified(WindowEvent e) {
 	}
 
 	@Override
 	public void windowOpened(WindowEvent e) {
 	}
 
 	@Override
 	public void windowActivated(WindowEvent e) {
 	}
 
 	@Override
 	public void windowClosed(WindowEvent e) {
 	}
 
 	@Override
 	public void notifyStackChange(List<String> stack) {
 		this.stackTextArea.setText("");
 		for (Object item : stack) {
 			this.stackTextArea.append(item + "\n");
 		}
 	}
 
 	@Override
 	public void notifyInputListChange(List<Object> inputList) {
 		this.inputListTextArea.setText("");
 		for (Object item : inputList) {
 			this.inputListTextArea.append(item + "\n");
 		}
 	}
 
 	@Override
 	public void notifyOutput(String output) {
 		this.addOutputMessage(output);
 	}
 
 	@Override
 	public void notifyNewInput() {
 		this.inputTextField.setEnabled(true);
 	}
 
 	@Override
 	public void notifyNewInput(boolean questionmark) {
 		this.isQuestionMarkOperator = true;
 		this.inputTextField.setEnabled(true);
 	}
 
 	private void resetCalculator() {
 		CalculatorUI.this.executor.clearStack();
 		CalculatorUI.this.stackTextArea.setText("");
 		CalculatorUI.this.outputTextArea.setText("");
 		CalculatorUI.this.inputTextArea.setText("");
 		CalculatorUI.this.inputListTextArea.setText("");
 		CalculatorUI.this.inputTextField.setEnabled(true);
 		this.textArea.setText("");
 		this.textArea.setBackground(new JTextArea().getBackground());
		this.debugStepButton.setEnabled(true);
		this.runButton.setEnabled(true);
		this.debugButton.setEnabled(true);
 	}
 
 	private void setErrorState(String message) {
		final String ERROR = "\nYou can inspect the current stack and input list state to analyse your error. Click 'Clear all' to start again.";
		final String GENERICERROR = "A generic error occured in the execution engine. Please click 'Clear all' to try another expression.";
		if(message != null) {
			this.textArea.setText(message + ERROR);
		} else {
			this.textArea.setText(GENERICERROR);
		}
 		this.textArea.setBackground(new Color(255, 160, 160));
		this.debugStepButton.setEnabled(false);
		this.runButton.setEnabled(false);
		this.debugButton.setEnabled(false);
 	}
 }
