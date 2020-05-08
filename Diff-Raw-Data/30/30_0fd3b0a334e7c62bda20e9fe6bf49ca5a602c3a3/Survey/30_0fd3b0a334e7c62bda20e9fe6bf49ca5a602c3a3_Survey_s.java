 package es.diz.mario.data;
 
 import java.awt.Font;
 import java.awt.GridBagConstraints;
 import java.awt.GridBagLayout;
 import java.awt.Insets;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.AbstractAction;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.KeyStroke;
 
 import dk.itu.mario.engine.DataRecorder;
 import es.diz.mario.game.PlayerGame;
 
 public class Survey extends JPanel {
 
 	private static final long serialVersionUID = 1L;
 	protected DataRecorder recorder;
 	protected PlayerGame game;
 	
 	protected JLabel lbl;
 	protected JButton butN;
 	protected JButton butY;
 
 	public Survey(DataRecorder recorder, PlayerGame game) {
 		super(new GridBagLayout());		
 		this.game = game;
 		
 		addLabel();
 		addButtonYes();
 		addButtonNo();
 		
 		setSize(640,480);
 		setVisible(true);
 	}
 	
 	public void setDefaults() {
 		
 		butY.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("Y"), "like");
 		butY.getActionMap().put("like", new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("pressed y");
 		        button_pressed(true);
 		    }
 		});
 		butY.setMnemonic('Y');
 		
 		
 		butN.getInputMap(WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke("N"), "dislike");
 		butN.getActionMap().put("dislike", new AbstractAction() {
 			private static final long serialVersionUID = 1L;
 
 			public void actionPerformed(ActionEvent e) {
 				System.out.println("pressed n");
 		        button_pressed(false);
 		    }
 		});
 		butN.setMnemonic('n');
 	}
 	
 	public boolean requestFocusInWindow() {
 		return super.requestFocusInWindow(); // explicit
 	}
 	
 	public void addLabel() {
		String str = "Did you like this level more than the previous one?";
 		lbl = new JLabel(str);
 		Font newFont = new Font("Arial Rounded MT Bold",
 				  lbl.getFont().getStyle(),
 				  24);  
 
 		lbl.setFont(newFont);
 		
 		GridBagConstraints cons = new GridBagConstraints();
 		cons.gridx = 1;
 		cons.gridy = 1;
 		cons.insets = new Insets(20, 20, 20, 20);
 		add(lbl, cons);
 	}
 	
 
 	public void addButtonYes() {
 		butY = new JButton();
 		butY.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				button_pressed(true);
 			}
 		});
 		Font newFont = new Font("Arial Rounded MT Bold",
 				  butY.getFont().getStyle(),
 				  18);  
 
 		butY.setFont(newFont);
 		butY.setText("Yes !");
 		
 		GridBagConstraints cons = new GridBagConstraints();
 		cons.gridx = 1;
 		cons.gridy = 2;
 		cons.ipadx = 50;
 		cons.ipady = 30;
 		cons.insets = new Insets(20, 20, 20, 20);
 		add(butY, cons);
 	}
 	
 	public void addButtonNo() {
 		butN = new JButton();
 		butN.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				button_pressed(false);
 			}
 		});
 		Font newFont = new Font("Arial Rounded MT Bold",
 				  butN.getFont().getStyle(),
 				  18);  
 
 		butN.setFont(newFont);
 		butN.setText("No !");
 		
 		GridBagConstraints cons = new GridBagConstraints();
 		cons.gridx = 1;
 		cons.gridy = 3;
 		cons.ipadx = 50;
 		cons.ipady = 30;
 		cons.insets = new Insets(20, 20, 20, 20);
 		add(butN, cons);
 	}
 	
 	public void button_pressed(boolean choice) {
 		if (choice)
 			System.out.println("He liked it");
 		else
 			System.out.println("He liked it not");
 		game.continuePlaying();
 	}
 	
 }
