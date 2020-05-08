 package gui;
 
 
 import java.awt.BorderLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BorderFactory;
 import javax.swing.Box;
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JPanel;
 import javax.swing.JTextField;
 
 public class ParsingTab extends JPanel implements ActionListener {
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -8617942858027114616L;
 	private JButton buttonParse;
 	
 	public ParsingTab() {
 		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 		setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
 		/* Source File Choosing */
 		JTextField textFieldFileName = new JTextField("./sample/WoWCombatLog.txt",30);
		textFieldFileName.setMaximumSize( textFieldFileName.getPreferredSize() );
 		JButton buttonBrowse = new JButton("Browse...");
 		JPanel sourcePanel = new JPanel();
 		
 		buttonParse = new JButton("Parse");
 		buttonParse.setEnabled(false);
 		buttonBrowse.addActionListener(this);
 		sourcePanel.setLayout(new BoxLayout(sourcePanel, BoxLayout.LINE_AXIS));
 		sourcePanel.setBorder(BorderFactory.createTitledBorder("Choose source file"));
 		sourcePanel.add(textFieldFileName);
 		sourcePanel.add(buttonBrowse);
 		this.add(sourcePanel,BorderLayout.PAGE_START);
 		this.add(buttonParse);
 	}
 
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		buttonParse.setEnabled(true);
 	}
 }
