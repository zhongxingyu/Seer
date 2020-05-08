 package ui;
 
 import java.awt.HeadlessException;
 import java.sql.SQLException;
 
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.DefaultTableModel;
 
 import domain.VoterDB;
 
 public class ResultUI {
 	private JFrame frame;
 	private VoterDB voterDB;
 	public ResultUI(VoterDB voterDB) {
 		frame = new JFrame("Vote Result");
 		this.voterDB = voterDB;
 		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		initComponents();
 		frame.pack();
 		frame.setSize(800, 500);
 		frame.setLocation(280, 100);
 	}
 	public void initComponents() {
 		try {
 			JOptionPane.showMessageDialog(frame,new JScrollPane(new JTable(voterDB.voteResult(null))),"Vote Result",JOptionPane.INFORMATION_MESSAGE);
 		} catch (HeadlessException | SQLException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 	public void run() {
 		frame.setVisible(true);
 	}
 }
