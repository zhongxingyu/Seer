 package twp.application;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.net.InetAddress;
 import java.net.UnknownHostException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 
 import twp.core.Container;
 import twp.generated.CalculatorError;
 import twp.generated.CalculatorHandler;
 import twp.generated.CalculatorProtocol;
 import twp.generated.CalculatorReply;
 import twp.generated.CalculatorRequest;
 import twp.generated.Expression;
 import twp.generated.ThreadID;
 
 public class CalculatorClient extends JFrame implements CalculatorHandler {
 	
 	private static final long serialVersionUID = 1L;
 
 	private class CalculatorListener implements ActionListener {
 		private CalculatorClient client;
 		
 		public CalculatorListener(CalculatorClient client) {
 			this.client = client;
 		}
 		
 		@Override
 		public void actionPerformed(ActionEvent arg0) {
 			client.run();	
 		}
 	}
 	
 	CalculatorFormulaGenerator generator;
 	
 	private static int regId = 0;
 	
 	private JPanel panel;
 	private JTextField input;
 	private JButton submit;
 	private JTextArea out;
 	
 	public static synchronized int getRegId() {
 		return regId++;
 	}
 	
 	public CalculatorClient() {
 		buildInterface();
 		registerOperations();
 	}
 	
 	private void buildInterface() {
 		setTitle("Calculator");
 		setLocationRelativeTo(null);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize(200, 156);
 		panel = new JPanel();
 		panel.setLayout(null);
 		getContentPane().add(panel);
 		input = new JTextField();
 		input.setBounds(10, 10, 180, 24);
 		panel.add(input);
 		submit = new JButton("Calculate!");
 		submit.setBounds(10, 44, 180, 24);
 		submit.addActionListener(new CalculatorListener(this));
 		panel.add(submit);
 		out = new JTextArea();
 		out.setBounds(10, 78, 180, 48);
 		out.setEditable(false);
 		out.setLineWrap(true);
 		out.setWrapStyleWord(true);
 		panel.add(out);
 		setVisible(true);
 	}
 	
 	private void run() {
 		run(input.getText());
 	}
 	
 	public void run(String formula) {
 		Expression expr = null;
 		try {
 			expr = generator.generate(formula);
 		} catch (Exception e) {
 			out.setText("Formula is not correct.\n" + e.getMessage());
 			e.printStackTrace();
 		}
 		if (expr != null) {
 			if (expr.getHost() != null)
 				try {
 					InetAddress addr = InetAddress.getByAddress(expr.getHost());
 					CalculatorProtocol prot = new CalculatorProtocol(addr.getHostAddress(), expr.getPort(), this);
					prot.sendRequest(getRegId(), expr.getArguments());//, createThreadExtension(prot));
 					submit.setEnabled(false);
 				} catch (UnknownHostException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
 			else
 				// shortcut in case the user entered just a number
 				out.setText("Result:\n" + expr.getArguments().getElements().get(0).getValue().getValue());
 		}
 	}
 	
 	private List<Container> createThreadExtension(CalculatorProtocol prot) {
 		ArrayList<Container> exts = new ArrayList<Container>();
 		exts.add(new ThreadID(prot.getLocalAddress(), prot.getLocalPort()));
 		return exts;
 	}
 	
 	private void registerOperations() {
 		generator = new CalculatorFormulaGenerator();
 		try {
			/*generator.register(new CalculatorOperation("localhost", 12347, "+", CalculatorOperation.BINARY_OP, CalculatorOperation.INFIX));
 			generator.register(new CalculatorOperation("localhost", 12348, "*", CalculatorOperation.BINARY_OP, CalculatorOperation.INFIX));
 			generator.register(new CalculatorOperation("localhost", 12349, "!", CalculatorOperation.UNARY_OP, CalculatorOperation.POSTFIX));
 			generator.register(new CalculatorOperation("localhost", 12350, "sin", CalculatorOperation.UNARY_OP, CalculatorOperation.PREFIX));
 			generator.register(new CalculatorOperation("localhost", 12351, "PI", CalculatorOperation.CONSTANT, CalculatorOperation.PREFIX));
 			generator.register(new CalculatorOperation("localhost", 12352, "/", CalculatorOperation.BINARY_OP, CalculatorOperation.INFIX));
			generator.register(new CalculatorOperation("localhost", 12353, "-", CalculatorOperation.BINARY_OP, CalculatorOperation.INFIX));*/
			generator.register(new CalculatorOperation("172.16.57.117", 7001, "+", CalculatorOperation.BINARY_OP, CalculatorOperation.INFIX));
			generator.register(new CalculatorOperation("172.16.57.117", 7002, "*", CalculatorOperation.BINARY_OP, CalculatorOperation.INFIX));
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 	}
 	
 	public static void main(String[] args) {
 		new CalculatorClient();
 	}
 
 	@Override
 	public void onCalculatorError(CalculatorError message) {
		out.setText("Error from Server:\n" + message.getText());
 		submit.setEnabled(true);
 	}
 
 	@Override
 	public void onCalculatorReply(CalculatorReply message) {
 		out.setText("Result:\n" + message.getResult().getValue());
 		submit.setEnabled(true);
 	}
 
 	@Override
 	public void onCalculatorRequest(CalculatorRequest message) {
 		// do nothing	
 	}
 }
