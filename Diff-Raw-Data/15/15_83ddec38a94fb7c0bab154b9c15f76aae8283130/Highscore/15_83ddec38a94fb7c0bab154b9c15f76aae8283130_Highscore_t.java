 package views;
 
 import java.awt.Color;
 import java.awt.EventQueue;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 
 import net.miginfocom.swing.MigLayout;
 import views.components.NicePanel;
 import controllers.Spel;
 
 public class Highscore extends NicePanel {
 	public static void main(String[] args) {
 		EventQueue.invokeLater(new Runnable() {
 			@Override
 			public void run() {
 				try {
 					JFrame frame = new JFrame();
 					frame.setBounds(100, 100, 800, 600);
 					frame.setVisible(true);
					Highscore ko = new Highscore(null);
 					frame.setContentPane(ko);
 					ko.updateUI();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 
 	private JTable	table;
 
 	/**
 	 * Create the frame.
 	 */
	public Highscore(final Spel spel) {
 		setBounds(0, 0, 800, 600);
 		setLayout(new MigLayout("", "[grow][][grow]", "[80.00][10.00,grow][][450.00,grow][40.00][10.00,grow]"));
 
 		JLabel lblNewLabel = new JLabel("Highscores");
 		lblNewLabel.setForeground(Color.WHITE);
 		lblNewLabel.setFont(new Font("Lucida Grande", Font.PLAIN, 40));
 		add(lblNewLabel, "cell 1 0,alignx center,aligny center");
 
 		JButton btnTerug = new JButton("Terug");
 		btnTerug.setFont(new Font("Lucida Grande", Font.PLAIN, 21));
 		btnTerug.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
				spel.backToMainMenu();
 				spel.openPanel(new JokerScherm(spel));
 			}
 		});
 
 		JLabel lblNewLabel_1 = new JLabel("Rang         Naam        Punten      Tijd");
 		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 15));
 		lblNewLabel_1.setForeground(Color.WHITE);
 		add(lblNewLabel_1, "cell 1 2");
 
 		table = new JTable();
 		table.setFont(new Font("Tahoma", Font.PLAIN, 16));
 		table.setModel(new DefaultTableModel(new Object[][] { { "1.", "Jodi", "7500", "5:00" },
 				{ "2.", "Nanne", "300", "0:10" }, { "3.", "Bas", "1", "0:01" }, }, new String[] { "Rang", "Naam",
 				"Prijzengeld", "Tijd over" }));
 		add(table, "cell 1 3,grow");
 		add(btnTerug, "cell 1 4,grow");
 
 	}
 }
