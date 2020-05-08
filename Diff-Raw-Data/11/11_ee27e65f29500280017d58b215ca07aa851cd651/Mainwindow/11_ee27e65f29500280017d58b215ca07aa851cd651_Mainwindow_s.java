 package gui;
 
 import gui.counter.ChooseGamemode;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 
 import java.awt.Dimension;
 import java.awt.Toolkit;
 
 import javax.swing.JButton;
 import java.awt.Font;
 import java.awt.event.ActionListener;
 import java.awt.event.ActionEvent;
 
 public class Mainwindow extends JFrame {
 	private static final long serialVersionUID = 1L;
 	private JButton btnTimer;
 	private JButton btnKonterchars;
 	private JButton btnLineup;
 	private JButton btnInfo;
 
 	public Mainwindow() {
 		getContentPane().setFont(new Font("GreekS", Font.BOLD, 14));
 		initGUI();
 
 		setLocationRelativeTo(null);
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 	}
 
 	private void initGUI() {
 		setSize(new Dimension(480, 165));
 		setResizable(false);
 		setTitle("LoL-Helper");
 		getContentPane().setLayout(null);
 
 		btnTimer = new JButton("Timer");
 		btnTimer.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				openModusChoose();
 			}
 		});
 		btnTimer.setFont(new Font("Centaur", Font.BOLD, 20));
 		btnTimer.setActionCommand("Timer");
 		btnTimer.setBounds(10, 43, 121, 49);
 		getContentPane().add(btnTimer);
 
 		btnKonterchars = new JButton("Counterchars");
 		btnKonterchars.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 				openModusChooseCounter();
 			}
 		});
 		btnKonterchars.setFont(new Font("Centaur", Font.BOLD, 20));
 		btnKonterchars.setBounds(157, 43, 171, 49);
 		getContentPane().add(btnKonterchars);
 
 		btnLineup = new JButton("Lineup");
 		btnLineup.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				openModusChooseLineup();
 			}
 		});
 		btnLineup.setFont(new Font("Centaur", Font.BOLD, 20));
 		btnLineup.setBounds(357, 43, 109, 49);
 		getContentPane().add(btnLineup);
 
 		btnInfo = new JButton("Info");
 		btnInfo.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent e) {
 				openInfo();
 			}
 		});
 		btnInfo.setFont(new Font("Dialog", Font.PLAIN, 8));
 		btnInfo.setBounds(428, 116, 46, 20);
 		getContentPane().add(btnInfo);
 	}
 
 	private void openModusChoose() {
 		ChooseGamemodeFrame choose = new ChooseGamemodeFrame();
 		choose.setVisible(true);
 	}
 	private void openModusChooseCounter(){
 		ChooseGamemode choose = new ChooseGamemode();
 		choose.setVisible(true);
 	}
 	private void openModusChooseLineup(){
 		Toolkit.getDefaultToolkit().beep();
 		JOptionPane.showMessageDialog(null, "Diese Option ist leider noch nicht verfgbar :(", "Nicht verfgbar", JOptionPane.OK_CANCEL_OPTION);
 	}
 	private void openInfo(){
 		InfoFrame info = new InfoFrame();
 		info.setVisible(true);
 	}
 }
