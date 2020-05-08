 import java.beans.*;
 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 public class InfoBox extends JPanel{
 	private Image background;
 	private ImageIcon agreeIcon = new ImageIcon(this.getClass().getResource("resources/agree_OFF.png"));
 	private ImageIcon agreeIconOn = new ImageIcon(this.getClass().getResource("resources/agree_ON.png"));
 	private ImageIcon disagreeIcon = new ImageIcon(this.getClass().getResource("resources/disagree_OFF.png"));
 	private ImageIcon disagreeIconOn = new ImageIcon("resources/disagree_ON.png");
 	private ImageIcon yesIcon = new ImageIcon(this.getClass().getResource("resources/yes_OFF.png"));
 	private ImageIcon yesIconOn = new ImageIcon(this.getClass().getResource("resources/yes_ON.png"));
 	private Dimension size;
 	private String question = "";
 	private String answer = "";
 	private String dialogText = "";
 	private JButton agree = new JButton();
 	private JButton disagree = new JButton();
 	private JButton playAgain = new JButton("Yes");
 	private GameControl control;
 	//private GameControl control;
 	public InfoBox(){
 		setBackground(new Color(17,45,164));
 	    setFont(new Font("Dialog", Font.BOLD, 15));
 		size = new Dimension();
 		background = new ImageIcon(this.getClass().getResource("resources/bottom.png")).getImage();
 		size.width = background.getWidth(null);
 		size.height = background.getHeight(null);
 		setPreferredSize(size);
 		agree.setBorder(BorderFactory.createEmptyBorder());
 		agree.setContentAreaFilled(false);
 		disagree.setBorder(BorderFactory.createEmptyBorder());
 		disagree.setContentAreaFilled(false);
 		agree.setIcon(agreeIcon);
 		disagree.setIcon(disagreeIcon);
 		agree.setRolloverIcon(agreeIconOn);
 		disagree.setRolloverIcon(disagreeIconOn);
 		playAgain.setIcon(yesIcon);
 		playAgain.setRolloverIcon(yesIconOn);
 		playAgain.setBorder(BorderFactory.createEmptyBorder());
 		playAgain.setContentAreaFilled(false);
 		agree.addActionListener(new ActionListener() {
 			// all the buttons do is call methods of the control
 		    public void actionPerformed(ActionEvent e) {
 		    	if (control != null) {
 		    		control.answerAgree(e); 
 		    	}
 			}
 		});
 		disagree.addActionListener(new ActionListener() {
 			// all the buttons do is call methods of the control
 		    public void actionPerformed(ActionEvent e) {
 		    	if (control != null) {
 		    		control.answerDisagree(e); 
 		    	}
 			}
 		});
 		playAgain.addActionListener(new ActionListener() {
 			// all the buttons do is call methods of the control
 		    public void actionPerformed(ActionEvent e) {
 		    	if (control != null) {
 		    		question="";
 					answer="";
 					control.playAgain(e); 
 		    	}
 			}
 		});
 	}
 	public void setGuiControl(GameControl control) {
       	this.control = control;
    	}
 	public void setQuestion(String question){
 		setFont(new Font("Dialog", Font.BOLD, 15));
 		this.question=question;
 	}
 	public void setAnswer(String answer){
 		add(agree);
 		add(disagree);
 		remove(playAgain);
 		this.answer=answer;
 		repaint();
 		revalidate();
 	}
 	public void setDialog(String dialog){
 		remove(agree);
 		remove(disagree);
 		remove(playAgain);
 		setFont(new Font("Dialog", Font.BOLD, 45));
 		this.answer=dialog;
 		this.question="";
 		repaint();
 		revalidate();
 	}
 	public void clear(){
 		remove(agree);
 		remove(disagree);
 		remove(playAgain);
 		this.answer="";
 		this.question="";
 		repaint();
 		revalidate();
 	}
 	public void setWinner(){
 		remove(agree);
 		remove(disagree);
 		add(playAgain);
 		setFont(new Font("Dialog", Font.BOLD, 35));
 		this.answer="DO YOU WANT TO PLAY AGAIN?";
 		this.question="";
 		repaint();
 		revalidate();
 	}
 	
 	public void paintComponent( Graphics g ){
 		super.paintComponent( g );
		int x = (getWidth()/2)-background.getWidth(null)/2; 
	    Graphics2D g2d = (Graphics2D) g;
	  	g2d.drawImage(background, x, 0, null);
 		g2d.setPaint(Color.black);
		g2d.drawString(question, 160, 80); 
		g2d.drawString(answer, 160, 110); 
 	}
 }
