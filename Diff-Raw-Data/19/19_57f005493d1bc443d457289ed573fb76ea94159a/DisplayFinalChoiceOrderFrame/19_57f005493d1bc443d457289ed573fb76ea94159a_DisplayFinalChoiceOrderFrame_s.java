 package view;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 
 import model.DecisionLinesEvent;
 import model.Model;
 import controller.ButtonController;
 
 /**
  * This class displays the final choice once the event is finished
  * @author Martti Peltola and Trevor Hodde
  */
 public class DisplayFinalChoiceOrderFrame extends JFrame{
 
 	private static final long serialVersionUID = 1L;
 	JPanel contentPane;
 	Model model;
 
 	/**
 	 * Constructor to set up the final choice form
 	 * @param m : Model object
 	 */
 	public DisplayFinalChoiceOrderFrame(Model m){
 		this.model = m;
 		setTitle("Final Choice Order");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setSize(700, 500);
 		setResizable(false);
 		Toolkit toolkit = Toolkit.getDefaultToolkit();
 	    Dimension screenSize = toolkit.getScreenSize();
 	    //Following three lines make the form centered
 	    int x = (screenSize.width - this.getWidth())/2;
 	    int y = (screenSize.height - this.getHeight())/2;
 	    this.setLocation(x, y);
 
 	    contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 
 		//show the question panel from the event
 		JPanel questionPanel = new JPanel();
 		questionPanel.setBounds(10, 10, 690, 100);
 		questionPanel.setBorder(BorderFactory.createTitledBorder("Event Question"));
 		this.add(questionPanel);
 
 		//display the question that was asked in the event
 		JLabel questionLabel = new JLabel();
 		questionLabel.setText("" + DecisionLinesEvent.getInstance().getQuestion());
 		questionPanel.add(questionLabel);
 
 		JPanel choicesPanel = new JPanel();
 		choicesPanel.setBounds(150, 120, 350, 250);
 		//show the final choice order
 		choicesPanel.setBorder(BorderFactory.createTitledBorder("Final Choice Order"));
 		this.add(choicesPanel);
 
		JLabel tmp = new JLabel();
 	    for(int i = 0; i < DecisionLinesEvent.getInstance().getNumChoices(); i++) {
	    	tmp.setBounds(50, 200+(25*i), 150, 150);
	    	choicesPanel.add(tmp);
 	    	tmp.setText(DecisionLinesEvent.getInstance().getChoiceOrderPosition(i));
 	    }
 
 	    //create an exit button
 		JButton btnExit = new JButton("Exit");
 		btnExit.setBounds(300, 400, 90, 25);
 		this.add(btnExit);
 		//set the exit button listener
 		btnExit.addActionListener(new ButtonController(3, this));
 	}
 }
