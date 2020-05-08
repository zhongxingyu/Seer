 package utils;
 
 import java.awt.Dimension;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.ImageIcon;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 
 import display.LangEditor;
 import display.Start;
 import display.Window;
 
 public class TabPanel extends JPanel implements ActionListener {
 
 	private static final long	serialVersionUID	= 8155818731609154350L;
 	private String				title				= null;
 	private JButton				button				= null;
 	private JTabbedPane			tabs				= null;
 	private String				filePath;
 
 	public TabPanel(String title, String filePath, JTabbedPane tabs)
 	{
 		this.filePath = filePath;
 		this.title = title;
 		JLabel label = new JLabel(title);
 		this.button = new JButton(new ImageIcon("img/x-icon.png"));
 		this.button.setPreferredSize(new Dimension(20, 20));
 		this.button.setContentAreaFilled(false);
 		this.tabs = tabs;
 		button.addActionListener(this);
 		this.add(label);
 		this.add(button);
 		this.setOpaque(false);
 	}
 
 	@Override
 	public void actionPerformed(final ActionEvent e)
 	{
 		removeTab(title);
 	}
 
 	public boolean removeTab(String title)
 	{
 		Start st = (Start) Window.getInstance().getContentPane();
 		boolean aux = false;
 		int i = 0;
 		while ( ! aux && i < tabs.getTabCount())
 		{
 			if (tabs.getTitleAt(i).equals(title))
 			{
 				LangEditor langEditor = st.getLangEditors().get(i);
 				if ( ! langEditor.isSaved())
 				{
 					int selection = JOptionPane
 					.showOptionDialog(
 					Window.getInstance(),
 					"The file has not been saved. Are you sure you want to close this file without saving?",
 					"Not Saved", JOptionPane.YES_NO_OPTION,
 					JOptionPane.WARNING_MESSAGE, new ImageIcon(
 					"img/warning.png"), new Object[] {"Yes", "No"}, "No");
 					if (selection == 0)
 					{
 						tabs.removeTabAt(i);
 						st.getLangEditors().remove(i);
 					}
 					aux = true;
 				}
 				else
 				{
 					tabs.removeTabAt(i);
 					aux = true;
 					st.getLangEditors().remove(i);
 				}
 			}
 		}
 		return false;
 	}
 }
