 package quickex3;
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.ButtonGroup;
 import javax.swing.JComboBox;
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.JMenu;
 import javax.swing.JMenuBar;
 import javax.swing.JMenuItem;
 import javax.swing.JPanel;
 import javax.swing.JRadioButton;
 
import quickex3.PanelPlay.HometownPanel;
import quickex3.PanelPlay.MajorPanel;

 
 public class PanelPlay extends JFrame implements ActionListener {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 	private JMenu menu;
 	private JMenuBar menubar;
 	private JMenuItem quit;
 	private MajorPanel major;
 	private HometownPanel hometown;
 
 	class MajorPanel extends JPanel {
 		private JRadioButton math;
 		private JRadioButton cs;
 		private ButtonGroup majors;
 
 		MajorPanel() {
 			this.setBorder(BorderFactory.createTitledBorder("College Major"));
 			
 			this.math = new JRadioButton("Math");
 			this.cs = new JRadioButton("Computer Science");
 			
 			this.majors = new ButtonGroup();
 			majors.add(cs);
 			majors.add(math);
 			
 			this.add(math);
 			this.add(cs);
 		}
 	}
 	
 	class HometownPanel extends JPanel {
 		private JLabel townlabel;
		private JComboBox town;
 
 		HometownPanel() {
 			String[] towns = {"Golden", "Longmont", "Dubai"};
 			this.townlabel = new JLabel("Home town: ");
			this.town = new JComboBox(towns);
 			this.add(townlabel);
 			this.add(town);
 		}
 	}
 	
 	PanelPlay() {
 		this.setTitle("Panel Play");
 		
 		this.setResizable( false ); 
 		this.setLocationRelativeTo(getRootPane()); 
 		
 		this.menubar = new JMenuBar();
 		this.menu = new JMenu("Program");
 		menubar.add(menu);
 		
 		quit = new JMenuItem("Quit");
 		quit.addActionListener(this);
 		menu.add(quit);
 		
 		this.setJMenuBar(menubar);
 		
 		this.hometown = new HometownPanel();
 		add(hometown, BorderLayout.NORTH);
 		
 		this.major = new MajorPanel();
 		add(major);
 		
 		this.pack();
 	}
 	
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		if( quit.equals(e.getSource())) {
 			this.dispose();
 		}
 	}
 
 	/**
 	 * @param args
 	 */
 	public static void main(String[] args) {
 		PanelPlay p = new PanelPlay();
 		p.setVisible(true);
 	}
 
 }
