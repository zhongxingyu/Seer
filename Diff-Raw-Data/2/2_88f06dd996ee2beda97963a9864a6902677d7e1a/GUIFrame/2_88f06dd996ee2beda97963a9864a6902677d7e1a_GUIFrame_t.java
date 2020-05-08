 package org.mathrick.swingsandbox;
 
 import static org.fluentjava.FluentUtils.irange;
 
 import java.awt.CardLayout;
 import java.awt.Component;
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.ButtonGroup;
 import javax.swing.JButton;
 import javax.swing.JCheckBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 import javax.swing.JScrollPane;
 import javax.swing.JSpinner;
 import javax.swing.JTextPane;
 import javax.swing.SpringLayout;
 import javax.swing.UIManager;
 import javax.swing.UnsupportedLookAndFeelException;
 import javax.swing.border.EmptyBorder;
 import javax.swing.event.ChangeEvent;
 import javax.swing.event.ChangeListener;
 import java.awt.event.ItemListener;
 import java.awt.event.ItemEvent;
 
 public class GUIFrame extends JFrame {
 	private static class State {
 		public SeqType seqType;
 		public boolean fullSeq;
 		public int seqArg;
 		public int seqFrom;
 		public int seqTo;
 
 		public State(SeqType seqType, boolean fullSeq, int seqArg, int seqFrom,
 				int seqTo) {
 			this.seqType = seqType;
 			this.fullSeq = fullSeq;
 			this.seqArg = seqArg;
 			this.seqFrom = seqFrom;
 			this.seqTo = seqTo;
 		}
 	}
 
 	private static final ButtonGroup buttonGroup = new ButtonGroup();
 	private static JSpinner spinFrom;
 	private static JSpinner spinTo;
 	private static JSpinner spinArg;
 	
 	private static enum SeqType
 	{
 		SEQ_FACT,
 		SEQ_FIB
 	}
 	
 	public GUIFrame()
 	{
 		super("Factonacci");
 		initGUI();
 	}
 	
 	private void initGUI() {
 
 		final State state = new State(SeqType.SEQ_FACT, false, 0, 0, 10);
 		
 		this.setPreferredSize(new Dimension(500, 450));
 		
 		this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		SpringLayout springLayout = new SpringLayout();
 		this.getContentPane().setLayout(springLayout);
 		
 		JPanel panel1 = new JPanel();
 		springLayout.putConstraint(SpringLayout.NORTH, panel1, 12, SpringLayout.NORTH, this.getContentPane());
 		springLayout.putConstraint(SpringLayout.WEST, panel1, 12, SpringLayout.WEST, this.getContentPane());
 		panel1.setAlignmentY(Component.TOP_ALIGNMENT);
 		panel1.setAlignmentX(Component.LEFT_ALIGNMENT);
 		this.getContentPane().add(panel1);
 		panel1.setLayout(new BoxLayout(panel1, BoxLayout.Y_AXIS));
 		
 		JLabel labelSeqType = new JLabel("Sequence type");
 		
 		Font boldFont = labelSeqType.getFont().deriveFont(Font.BOLD); 
 		labelSeqType.setFont(boldFont);
 		labelSeqType.setAlignmentY(Component.TOP_ALIGNMENT);
 		panel1.add(labelSeqType);
 		
 		JPanel panel2 = new JPanel();
 		panel2.setAlignmentY(Component.TOP_ALIGNMENT);
 		panel2.setAlignmentX(Component.LEFT_ALIGNMENT);
 		panel1.add(panel2);
 		panel2.setBorder(new EmptyBorder(0, 12, 0, 0));
 		panel2.setLayout(new BoxLayout(panel2, BoxLayout.Y_AXIS));
 		
 		Component verticalStrut_1 = Box.createVerticalStrut(6);
 		panel2.add(verticalStrut_1);
 		
 		JRadioButton radioFact = new JRadioButton("Factorial");
 		radioFact.setName("factorial");
 		radioFact.setSelected(true);
 		buttonGroup.add(radioFact);
 		radioFact.setAlignmentY(Component.TOP_ALIGNMENT);
 		panel2.add(radioFact);
 
 		JRadioButton radioFib = new JRadioButton("Fibonacci");
 		radioFib.setName("fibonacci");
 		radioFib.setAlignmentY(Component.TOP_ALIGNMENT);
 		panel2.add(radioFib);
 		buttonGroup.add(radioFib);
 		
 		radioFact.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent event) {
 				if(event.getStateChange() == ItemEvent.SELECTED)
 				{
 					state.seqType = SeqType.SEQ_FACT;
 				}
 			}
 		});
 
 		radioFib.addItemListener(new ItemListener() {
 			public void itemStateChanged(ItemEvent event) {
 				if(event.getStateChange() == ItemEvent.SELECTED)
 				{
					state.seqType = SeqType.SEQ_FIB;
 				}
 			}
 		});
 
 		final JCheckBox checkFullSeq = new JCheckBox("Generate full sequence");
 		checkFullSeq.setName("full");
 		checkFullSeq.setAlignmentY(Component.TOP_ALIGNMENT);
 		panel2.add(checkFullSeq);
 		
 		JPanel panel3 = new JPanel();
 		springLayout.putConstraint(SpringLayout.NORTH, panel3, 6, SpringLayout.SOUTH, panel1);
 		springLayout.putConstraint(SpringLayout.SOUTH, panel3, -6, SpringLayout.SOUTH, this.getContentPane());
 		springLayout.putConstraint(SpringLayout.WEST, panel3, 10, SpringLayout.WEST, this.getContentPane());
 		springLayout.putConstraint(SpringLayout.EAST, panel3, -10, SpringLayout.EAST, this.getContentPane());
 		panel3.setAlignmentY(Component.TOP_ALIGNMENT);
 		panel3.setAlignmentX(Component.LEFT_ALIGNMENT);
 		panel3.setBorder(null);
 		this.getContentPane().add(panel3);
 		panel3.setLayout(new BoxLayout(panel3, BoxLayout.Y_AXIS));
 		
 		JLabel labelResult = new JLabel("Result");
 		labelResult.setFont(boldFont);
 		labelResult.setAlignmentY(Component.TOP_ALIGNMENT);
 		panel3.add(labelResult);
 		
 		Component verticalStrut = Box.createVerticalStrut(6);
 		panel3.add(verticalStrut);
 		
 		JPanel panel = new JPanel();
 		panel.setAlignmentY(Component.TOP_ALIGNMENT);
 		panel.setAlignmentX(Component.LEFT_ALIGNMENT);
 		panel.setBorder(new EmptyBorder(0, 12, 0, 0));
 		panel3.add(panel);
 		panel.setLayout(new BoxLayout(panel, BoxLayout.X_AXIS));
 		
 		final JTextPane textResult = new JTextPane();
 		textResult.setName("result");
 		JScrollPane scroll = new JScrollPane(textResult);
 		panel.add(scroll);
 		textResult.setBorder(null);
 		textResult.setAlignmentY(Component.TOP_ALIGNMENT);
 		textResult.setAlignmentX(Component.LEFT_ALIGNMENT);
 		
 		
 		
 		JButton btnGenerate = new JButton("Generate");
 		btnGenerate.setName("generate");
 		springLayout.putConstraint(SpringLayout.SOUTH, btnGenerate, 0, SpringLayout.SOUTH, panel1);
 		this.getContentPane().add(btnGenerate);
 		btnGenerate.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				showSequence(textResult, state);
 			}
 		});
 		
 		final JPanel panel5 = new JPanel();
 		springLayout.putConstraint(SpringLayout.WEST, btnGenerate, 0, SpringLayout.WEST, panel5);
 		springLayout.putConstraint(SpringLayout.EAST, btnGenerate, 0, SpringLayout.EAST, panel5);
 		this.getContentPane().add(panel5);
 		final CardLayout argLayout = new CardLayout(0, 0);
 		panel5.setLayout(argLayout);
 		
 		JPanel panelFullSeq = new JPanel();
 		panel5.add(panelFullSeq, "fullSeq");
 		panelFullSeq.setLayout(new BoxLayout(panelFullSeq, BoxLayout.X_AXIS));
 		
 		JLabel lblFrom = new JLabel("From");
 		panelFullSeq.add(lblFrom);
 		
 		Component horizontalStrut_1 = Box.createHorizontalStrut(6);
 		panelFullSeq.add(horizontalStrut_1);
 		
 		spinFrom = new JSpinner();
 		spinFrom.setName("from");
 		spinFrom.setValue((Integer)state.seqFrom);
 		((JSpinner.DefaultEditor)spinFrom.getEditor()).getTextField().setColumns(3);
 		spinFrom.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent arg0) {
 				state.seqFrom = ((Integer)spinFrom.getValue());
 			}
 		});
 		panelFullSeq.add(spinFrom);
 		
 		Component horizontalStrut_2 = Box.createHorizontalStrut(6);
 		panelFullSeq.add(horizontalStrut_2);
 		
 		JLabel lblTo = new JLabel("to");
 		panelFullSeq.add(lblTo);
 		
 		Component horizontalStrut = Box.createHorizontalStrut(6);
 		panelFullSeq.add(horizontalStrut);
 		
 		spinTo = new JSpinner();
 		spinTo.setName("to");
 		spinTo.setValue((Integer)state.seqTo);
 		((JSpinner.DefaultEditor)spinTo.getEditor()).getTextField().setColumns(3);
 		spinTo.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent arg0) {
 				state.seqTo = ((Integer)spinTo.getValue());
 			}
 		});
 		panelFullSeq.add(spinTo);
 		
 		Component glue = Box.createGlue();
 		panelFullSeq.add(glue);
 		
 		JPanel panelShortSeq = new JPanel();
 		panel5.add(panelShortSeq, "shortSeq");
 		panelShortSeq.setLayout(new BoxLayout(panelShortSeq, BoxLayout.X_AXIS));
 		
 		JLabel lblArgument = new JLabel("Argument");
 		panelShortSeq.add(lblArgument);
 		
 		spinArg = new JSpinner();
 		spinArg.setName("arg");
 		spinArg.addChangeListener(new ChangeListener() {
 			public void stateChanged(ChangeEvent arg0) {
 				state.seqArg = ((Integer)spinArg.getValue());
 			}
 		});
 		
 		Component horizontalStrut_3 = Box.createHorizontalStrut(6);
 		panelShortSeq.add(horizontalStrut_3);
 		panelShortSeq.add(spinArg);
 		
 		checkFullSeq.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				state.fullSeq = (checkFullSeq.isSelected());
 				if (state.fullSeq)
 				{
 					argLayout.show(panel5, "fullSeq");
 				} else
 				{
 					argLayout.show(panel5, "shortSeq");
 				}
 				
 			}
 		});
 		
 		JLabel lblParameters = new JLabel("Parameters");
 		lblParameters.setFont(boldFont);
 		springLayout.putConstraint(SpringLayout.NORTH, lblParameters, 12, SpringLayout.NORTH, this.getContentPane());
 		springLayout.putConstraint(SpringLayout.WEST, lblParameters, 6, SpringLayout.EAST, panel1);
 		springLayout.putConstraint(SpringLayout.NORTH, panel5, 6, SpringLayout.SOUTH, lblParameters);
 		springLayout.putConstraint(SpringLayout.WEST, panel5, 12, SpringLayout.WEST, lblParameters);
 		
 		// Swing is stupid and doesn't let you fire events yourself; there's no way
 		// to set a checkbox to a known state while ensuring change listeners will be
 		// fired, which means we can get out of sync with the logic, unless we do the
 		// horrible hack with setting the state to the opposite of intended value and
 		// then doing doClick()
 		checkFullSeq.getModel().setSelected(!state.fullSeq);
 		checkFullSeq.doClick();
 		
 		
 		this.getContentPane().add(lblParameters);
 		this.pack();
 	}
 
 	protected static void showSequence(JTextPane text, State state) {
 
 		StringBuilder str = new StringBuilder();
 		String fmtFact = "%d! = %d\n";
 		String fmtFib = "fib(%d) = %d\n";
 		
 		int from, to;
 		if(state.fullSeq)
 		{
 			from = state.seqFrom;
 			to = state.seqTo;
 		} else
 		{
 			from = to = state.seqArg;
 		}
 		
 		switch (state.seqType) {
 		case SEQ_FACT:
 			for (Integer i : irange(from, to + 1)) {
 				str.append(String.format(fmtFact, i, MathOps.factorial(i)));
 			}
 			break;
 		case SEQ_FIB:
 			for (Integer i : irange(from, to + 1)) {
 				str.append(String.format(fmtFib, i, MathOps.fibonacci(i)));
 			}
 			break;
 		}
 		text.setText(str.toString());
 	}
 
 	public static void main(String[] args) {
 		try {
 			// Set System L&F
 			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
 		} 
 		catch (UnsupportedLookAndFeelException e) {} 
 		catch (ClassNotFoundException e) {} 
 		catch (InstantiationException e) {} 
 		catch (IllegalAccessException e) {}
 
 		java.awt.EventQueue.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				new GUIFrame().setVisible(true);
 			}
 		});
 	}
 }
