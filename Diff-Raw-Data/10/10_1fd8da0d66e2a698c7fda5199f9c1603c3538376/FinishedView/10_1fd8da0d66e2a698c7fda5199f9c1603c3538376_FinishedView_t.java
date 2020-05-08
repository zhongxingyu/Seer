 package se.chalmers.kangaroo.view;
 
 import java.awt.BorderLayout;
import java.awt.Dimension;
 import java.awt.Font;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseAdapter;
 import java.awt.event.MouseEvent;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.SwingConstants;
 
 public class FinishedView extends JPanel{
 	public FinishedView() {
		
		setSize(new Dimension(1024,576));
		setPreferredSize(new Dimension(1024,576));
		setLayout(new BorderLayout(0, 0));
 		
 		JLabel lblCongratulations = new JLabel("Congratulations!");
 		lblCongratulations.setFont(new Font("Tahoma", Font.PLAIN, 36));
 		lblCongratulations.setHorizontalAlignment(SwingConstants.CENTER);
 		add(lblCongratulations, BorderLayout.NORTH);
 		
 		JTextArea txtrByFinishingThis = new JTextArea();
 		txtrByFinishingThis.setWrapStyleWord(true);
 		txtrByFinishingThis.setLineWrap(true);
 		txtrByFinishingThis.setEditable(false);
		txtrByFinishingThis.setText("By finishing this game you establish yourself as a true nerd and a pr0 g4m3r. Now the question is, can you do it faster? (hardcore mode activated)");
 		add(txtrByFinishingThis, BorderLayout.CENTER);
 		
 		JPanel panel = new JPanel();
 		add(panel, BorderLayout.EAST);
 		panel.setLayout(new BoxLayout(panel, BoxLayout.Y_AXIS));
 		JLabel lblCreators = new JLabel("Creators:");
 		panel.add(lblCreators);
 		
 		JLabel lblSeanPavlov = new JLabel("Sean Pavlov");
 		panel.add(lblSeanPavlov);
 		
 		JLabel lblHenrikAlburg = new JLabel("Henrik Alburg");
 		panel.add(lblHenrikAlburg);
 		
 		JLabel lblSimonAlmgren = new JLabel("Simon Almgren");
 		panel.add(lblSimonAlmgren);
 		
 		JLabel lblArvidKarlsson = new JLabel("Arvid Karlsson");
 		panel.add(lblArvidKarlsson);
 		
 		JPanel panel_1 = new JPanel();
 		add(panel_1, BorderLayout.SOUTH);
 		
 		JButton btnNewGame = new JButton("New Game");
 		btnNewGame.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent arg0) {
 			}
 		});
 		panel_1.add(btnNewGame);
 		
 		JButton btnNewButton = new JButton("Exit Game");
 		btnNewButton.addMouseListener(new MouseAdapter() {
 			@Override
 			public void mouseClicked(MouseEvent e) {
 			}
 		});
 		btnNewButton.addActionListener(new ActionListener() {
 			public void actionPerformed(ActionEvent arg0) {
 			}
 		});
 		panel_1.add(btnNewButton);
 	}
 }
