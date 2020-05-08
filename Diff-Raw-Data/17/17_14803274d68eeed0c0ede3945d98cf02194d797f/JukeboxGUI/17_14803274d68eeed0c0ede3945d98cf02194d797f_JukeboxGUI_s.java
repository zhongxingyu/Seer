 package view;
 
 import java.awt.BorderLayout;
 import java.awt.Color;
 import java.awt.Dimension;
 
 import javax.swing.JButton;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JScrollPane;
 import javax.swing.JTable;
 import javax.swing.table.TableModel;
 
 import model.SongList;
 import model.StudentList;
 
 public class JukeboxGUI extends JFrame{
 
 
 	public static void main(String[] args)
 	{
		new JukeboxGUI().setVisible(true);
 	}
 	
 	private TableModel modelOfSongs;
 	private JTable tableOfSongs;
 	
 	public JukeboxGUI()
 	{
		String username = JOptionPane.showInputDialog("Please enter username");
		
		JOptionPane.showInputDialog("Please enter password for user " + username);
		
		if(true)
		{
			this.setVisible(true);
 			layoutGUI();
		}
 	}
 	
 	private void layoutGUI()
 	{
 		setTitle("University of Arizona Student Jukebox");
 		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 		
 //		setBackground(Color.BLUE);
 
 		this.setSize(500, 500);
 		this.setLocation(200, 200);
 		
 		modelOfSongs = new SongList();
 		tableOfSongs = new JTable(modelOfSongs);
 		
 		JScrollPane scrollPane = new JScrollPane(tableOfSongs);
 		
 		add(scrollPane, BorderLayout.CENTER);
 	}
 
 }
