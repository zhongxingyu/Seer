 import java.awt.*;
 import java.awt.event.*;
 import javax.swing.*;
 
 public class Interface extends JFrame implements KeyListener, ActionListener {
 	static JButton single, multi, options, exit, sound, controls, backtomain, save, backtooptions;
 	static JLabel ctrlmenu, player1, player2, up1, down1, right1, left1, bomb1,
 			up2, down2, right2, left2, bomb2;
 	static JTextField getUp1, getUp2, getDown1, getDown2, getRight1, getRight2,
 			getLeft1, getLeft2, getBomb1, getBomb2;
 	static JPanel panel;
 
 	public static void MainMenu() {
 		GridBagConstraints c = new GridBagConstraints();
 		c.insets = new Insets(10, 10, 10, 10);
 		c.gridx = 1;
 		c.gridy = 0;
 		panel.add(single, c);
 		c.gridy = 1;
 		panel.add(multi, c);
 		c.gridy = 2;
 		panel.add(options, c);
 		c.gridy = 3;
 		panel.add(exit, c);
 	}
 
 	public static void OptionsMenu() {
 		GridBagConstraints c = new GridBagConstraints();
 		c.insets = new Insets(10, 10, 10, 10);
 		c.gridx = 1;
 		c.gridy = 0;
 		panel.add(sound, c);
 		c.gridy = 1;
 		panel.add(controls, c);
 		c.gridy = 2;
 		panel.add(backtomain, c);
 
 	}
 
 	public static void ControlMenu() {
 		GridBagConstraints c = new GridBagConstraints();
 		c.insets = new Insets(10, 10, 10, 10);
 		c.gridx = 0;
 		c.gridy = 0;
 		panel.add(player1, c);
 		c.gridy = 1;
 		panel.add(up1, c);
 		c.gridy = 2;
 		panel.add(down1, c);
 		c.gridy = 3;
 		panel.add(right1, c);
 		c.gridy = 4;
 		panel.add(left1, c);
 		c.gridy = 5;
 		panel.add(bomb1, c);
 		c.gridx = 1;
 		c.gridy = 1;
 		panel.add(getUp1, c);
 		c.gridy = 2;
 		panel.add(getDown1, c);
 		c.gridy = 3;
 		panel.add(getRight1, c);
 		c.gridy = 4;
 		panel.add(getLeft1, c);
 		c.gridy = 5;
 		panel.add(getBomb1, c);
 		c.gridx = 2;
 		c.gridy = 0;
 		panel.add(player2, c);
 		c.gridy = 1;
 		panel.add(up2, c);
 		c.gridy = 2;
 		panel.add(down2, c);
 		c.gridy = 3;
 		panel.add(right2, c);
 		c.gridy = 4;
 		panel.add(left2, c);
 		c.gridy = 5;
 		panel.add(bomb2, c);
 		c.gridx = 3;
 		c.gridy = 1;
 		panel.add(getUp2, c);
 		c.gridy = 2;
 		panel.add(getDown2, c);
 		c.gridy = 3;
 		panel.add(getRight2, c);
 		c.gridy = 4;
 		panel.add(getLeft2, c);
 		c.gridy = 5;
 		panel.add(getBomb2, c);
 		c.gridx = 1;
 		c.gridy = 6;
 		panel.add(save, c);
 		c.gridx = 2;
 		panel.add(backtooptions, c);
 
 	}
 
 	public Interface() {
 
 		this.setTitle("Bomberman!");
 		this.setSize(640, 480);
 		this.setResizable(false);
 		this.setLocationRelativeTo(null);
 		panel = new JPanel(new GridBagLayout());
 		single = new JButton("Singleplayer");
 		single.addActionListener(this);
 		multi = new JButton("  Multiplayer ");
 		multi.addActionListener(this);
 		options = new JButton("   Optionen    ");
 		options.addActionListener(this);
 		exit = new JButton("    Beenden    ");
 		exit.addActionListener(this);
 		controls = new JButton("               Steuerung               ");
 		controls.addActionListener(this);
 		sound = new JButton("                   Sound                   ");
 		backtomain = new JButton("Zurueck zum Hauptmenue");
 		backtomain.addActionListener(this);
 		save = new JButton("Speichern");
 		save.addActionListener(this);
 		backtooptions = new JButton("Zurueck");
 		backtooptions.addActionListener(this);
 		ctrlmenu = new JLabel("Steuerung");
 		player1 = new JLabel("Spieler 1");
 		player2 = new JLabel("Spieler 2");
 		up1 = new JLabel("Oben :");
 		down1 = new JLabel("Unten :");
 		right1 = new JLabel("Rechts :");
 		left1 = new JLabel("Links :");
 		bomb1 = new JLabel("Bombe legen :");
 		up2 = new JLabel("Oben :");
 		down2 = new JLabel("Unten :");
 		right2 = new JLabel("Rechts :");
 		left2 = new JLabel("Links :");
 		bomb2 = new JLabel("Bombe legen :");
 		getUp1 = new JTextField(6);
 		getDown1 = new JTextField(6);
 		getRight1 = new JTextField(6);
 		getLeft1 = new JTextField(6);
 		getBomb1 = new JTextField(6);
 		getUp2 = new JTextField(6);
 		getDown2 = new JTextField(6);
 		getRight2 = new JTextField(6);
 		getLeft2 = new JTextField(6);
 		getBomb2 = new JTextField(6);
 		panel.setFocusable(true); 
 		panel.addKeyListener(this);
 		
 		MainMenu();
 		this.add(panel);
 	}
 
 	public static void main(String[] args) {
 		
 		Eingabe.CtrlReader();
 		
 		Interface Menu = new Interface();
 		
 		Menu.setVisible(true);
 		
 	}
 
 	public void actionPerformed(ActionEvent e) {
 		GridBagConstraints c = new GridBagConstraints();
 		c.insets = new Insets(10, 10, 10, 10);
 		if (e.getSource() == this.exit) {
 			System.exit(0);
 		} else if (e.getSource() == this.options) {
 			panel.removeAll();
 			OptionsMenu();
 			panel.updateUI();
 		} else if (e.getSource() == this.backtomain) {
 			panel.removeAll();
 			MainMenu();
 			panel.updateUI();
 		} else if (e.getSource() == this.controls) {
 			panel.removeAll();
 			ControlMenu();
 			panel.updateUI();
 		} else if (e.getSource() == this.backtooptions) {
 			panel.removeAll();
 			OptionsMenu();
 			panel.updateUI();
 		}
 
 	}
 	
 	public void keyPressed(KeyEvent e) {
 		String Key = Eingabe.Ctrl(e);
 		System.out.println(Key);
 
 	}
 
 	public void keyReleased(KeyEvent e) {
 
 	}
 
 	public void keyTyped(KeyEvent e) {
 
 	}
 
 }
