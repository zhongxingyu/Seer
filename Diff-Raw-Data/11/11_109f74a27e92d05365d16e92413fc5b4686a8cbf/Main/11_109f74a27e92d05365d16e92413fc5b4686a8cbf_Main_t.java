package ecv;
 
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.io.StringReader;
 
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 
 @SuppressWarnings("serial")
 public class Main extends JFrame implements ActionListener {
 	private JPanel result = new JPanel();
 	private JComboBox choice = new JComboBox(new String[]{"Evaluation", "Infix Print", "Postfix print", "Graph"});
 	private JTextArea expr = new JTextArea("0 == 0", 3, 20);
 
 	public static void main(String args[]) {
 		new Main();
 	}
 
 	private Main() {
 		super("ECV");
 		setLayout(new FlowLayout());
 		add(expr);
 		JButton go = new JButton("GO"); 
 		go.addActionListener(this);
 		add(go);
 		add(result);
 		pack();
 		setVisible(true);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent arg) {
 		PropertiesContext context = new PropertiesContext ();
 		Expression expression;
 		try {
 			expression = parse(expr.getText());
 			switch(choice.getSelectedIndex()) {
 			case 0:	EvaluatingVisitor visitor = new EvaluatingVisitor (context);
 				visitor.valueOf (expression);
 				break;
 			case 1:
 			case 2:
 			case 3:
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 
 	private Expression parse(String expression) throws IOException
 	{
 		StringReader reader = new StringReader(expression);
 		StreamTokenizerAdapter sta = new StreamTokenizerAdapter(reader);
 		ExpressionBuilder builder = new ExpressionBuilder();
 		Parser parser = new Parser(sta, builder);
 		parser.parse();
 		return builder.getResult();
 	}
 }
