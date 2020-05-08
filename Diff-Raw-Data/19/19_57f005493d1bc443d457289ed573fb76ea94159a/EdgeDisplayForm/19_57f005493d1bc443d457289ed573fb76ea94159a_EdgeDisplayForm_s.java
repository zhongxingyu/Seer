 package view;
 
 import java.awt.Dimension;
 import java.awt.Graphics;
 import java.awt.Toolkit;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.border.EmptyBorder;
 
 import controller.AddEdgeController;
 import controller.AddEdgeResponseXMLController;
 import controller.ButtonController;
 import model.DecisionLinesEvent;
 import model.Edge;
 import model.Line;
 import model.Model;
 import javax.swing.JLabel;
 
 /**
  * This class displays edges that are added between two decision lines
  * @author Trevor Hodde
  */
 public class EdgeDisplayForm extends JFrame {
 	private static final long serialVersionUID = 1L;
 	Model model;
 	AddEdgeResponseXMLController edgeController; 
 	public int nLastXClick;
 	public int nLastYClick;
 	public static final int CHOICEWIDTH = 100;
 	public static final int CHOICEHEIGHT = 55;
 	int[] xCoords;
 	JPanel contentPane;
 	JLabel lblConnectedUsers, lblEventTypeLabel;
 
 	/**
 	 * Constructor for setting up the edge display form
 	 * @param m : Model object
 	 */
 	public EdgeDisplayForm() {
 		model = Model.getModel();
		//creates the form
		setTitle("Decision Lines Event");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		setResizable(false);
 		setSize(700, 500);
 		
 		Toolkit toolkit = Toolkit.getDefaultToolkit();
 	    Dimension screenSize = toolkit.getScreenSize();
 	    //Following three lines make the form centered
 	    int x = (screenSize.width - this.getWidth())/2;
 	    int y = (screenSize.height - this.getHeight())/2;
 	    this.setLocation(x, y);
 		
 		getContentPane().setLayout(null);
 		contentPane = new JPanel();
 		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
 		setContentPane(contentPane);
 		contentPane.setLayout(null);
 		
 		//sets the exit button up
 		JButton btnExitButton = new JButton("Exit");
 		btnExitButton.setBounds(300, 400, 90, 25);
 		btnExitButton.addActionListener(new ButtonController(3, this));
 		btnExitButton.setVisible(true);
 		contentPane.add(btnExitButton);
 		
 		lblEventTypeLabel = new JLabel("Event Type:");
 		lblEventTypeLabel.setBounds(30, 417, 218, 14);
 		contentPane.add(lblEventTypeLabel);
 		
 		lblConnectedUsers = new JLabel("Connected Users: ");
 		lblConnectedUsers.setBounds(30, 442, 637, 25);
 		contentPane.add(lblConnectedUsers);
 		contentPane.addMouseListener(new AddEdgeController(this));
 		
 		model.setJFrame(this);
 	}
 	
 	/**
 	 * This method repaints the frame on the current form
 	 */
 	public void redraw(){
 		this.repaint();
 		//model.setJFrame(this);
 	}
 	
 	/**
 	 * Returns the position of the current choice line
 	 * @param choicePosition int
 	 * @return int position of choice line
 	 */
 	public int getChoiceXLocation(int choicePosition) {
 		return choicePosition * CHOICEWIDTH + 20;
 	}
 	
 	/**
 	 * Return the length of the current choice line
 	 * @param choicePosition int
 	 * @return int height of choice line
 	 */
 	public int getChoiceYLocation(int choicePosition) {
 		return CHOICEHEIGHT;
 	}
 	
 	/**
 	 * This method paints the edges, lines and choices in the correct places
 	 */
 	@Override
 	public void paint(Graphics g) {
 		super.paint(g);
 		Edge currentEdge;
 		
 		//This loop paints the choices above the lines and the decision lines
 		DecisionLinesEvent event = model.getDecisionLinesEvent();
 		for (int i = 0; i < event.getNumChoices(); i++) {
 			Line tempChoice = event.getChoice(i);
 			if (tempChoice != null) {
 				g.drawString(tempChoice.getChoice(), getChoiceXLocation(i), getChoiceYLocation(i));
 				g.drawLine(getChoiceXLocation(i) + 20, getChoiceYLocation(i) + 10, getChoiceXLocation(i) + 20, getChoiceYLocation(i) + 350);
 			}
 		}
 		
 		//this loops paints the edges between the correct lines
 		for(int i = 0; i < event.getEdges().size(); i++) {
 			currentEdge = event.getEdges().get(i);
 			g.drawLine(getChoiceXLocation(currentEdge.getLeft()) + 20, currentEdge.getHeight() + 10, getChoiceXLocation(currentEdge.getRight()) + 20, currentEdge.getHeight() + 10);
 		}
 		
 		lblEventTypeLabel.setText("Event Type: " + event.getType());
 		String conUsers = "Connected Users:";
 		for (int i = 0; i < model.connectedUsers.size(); i++) {
 			conUsers += " " + model.connectedUsers.get(i);
 		}
 		lblConnectedUsers.setText(conUsers);
 	}
 }
