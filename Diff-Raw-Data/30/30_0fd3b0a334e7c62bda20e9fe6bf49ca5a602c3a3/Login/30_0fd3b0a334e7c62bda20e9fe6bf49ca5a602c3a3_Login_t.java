 package es.diz.mario.data;
 
 import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
import javax.swing.SwingConstants;
 
 import es.diz.mario.game.PlayerGame;
 
 public class Login extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 	protected PlayerGame game;
 	
 	protected JLabel lbl;
 	protected JTextField txt;
 	protected JButton but;
 
 	public Login(PlayerGame game) {
 		super(new GridBagLayout());		
 		this.game = game;
 		
 		addLabel();
 		addTextField();
 		addButton();
 		
 		setPreferredSize(new Dimension(640,480));
 		setVisible(true);
 	}
 	
 	public boolean requestFocusInWindow() {
		super.requestFocusInWindow();
		txt.requestFocusInWindow();
		// funnily, often they return both false but the textField gets focus anyway
		return true;
 	}
 	
 	public void setDefaults() {
 		getRootPane().setDefaultButton(but);
 	}
 	
 	public void addLabel() {
 		String str = "What's your name?";
 		lbl = new JLabel(str);
 		Font newFont = new Font("Arial Rounded MT Bold",
 				  lbl.getFont().getStyle(),
 				  24);  
 
		lbl.setHorizontalAlignment(SwingConstants.CENTER);
 		lbl.setFont(newFont);
 		
 		GridBagConstraints cons = new GridBagConstraints();
 		cons.gridx = 1;
 		cons.gridy = 1;
 		cons.insets = new Insets(20, 20, 20, 20);
 		add(lbl, cons);
 	}
 	
 	public void addTextField() {
 		txt = new JTextField(10);
 		Font newFont = new Font("Arial Rounded MT Bold",
 							  Font.BOLD,
 							  40);  
 		  
 		txt.setFont(newFont);
 		txt.setBorder(BorderFactory.createCompoundBorder(
 		        txt.getBorder(), 
 		        BorderFactory.createEmptyBorder(15, 15, 15, 15)));
 		
 		GridBagConstraints cons = new GridBagConstraints();
 		cons.gridx = 1;
 		cons.gridy = 2;
 		cons.ipady = 50;
 		cons.insets = new Insets(20, 10, 20, 10);
 		add(txt, cons);
 		txt.requestFocus();
 	}
 	
 	public void addButton() {
 		but = new JButton();
 		but.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				button_pressed(e);
 			}
 		});
 		Font newFont = new Font("Arial Rounded MT Bold",
 				  but.getFont().getStyle(),
 				  18);  
 
 		but.setFont(newFont);
 		but.setText("Play !");
 		
 		GridBagConstraints cons = new GridBagConstraints();
 		cons.gridx = 1;
 		cons.gridy = 3;
 		cons.ipadx = 50;
 		cons.ipady = 30;
 		cons.insets = new Insets(20, 20, 20, 20);
 		add(but, cons);
 	}
 	
 	public void button_pressed(ActionEvent e) {
 		game.setName(txt.getText());
 	}
 	
 }
