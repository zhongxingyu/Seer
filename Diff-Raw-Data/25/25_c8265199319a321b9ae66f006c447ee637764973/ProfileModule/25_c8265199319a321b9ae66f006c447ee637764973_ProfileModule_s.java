 package crescendo.base.GUI;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 
 import javax.swing.BoxLayout;
 import javax.swing.GroupLayout;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JTabbedPane;
 import javax.swing.JTextField;
 import javax.swing.SwingConstants;
 import javax.swing.GroupLayout.Group;
 
 import crescendo.base.ErrorHandler;
 import crescendo.base.profile.Profile;
 import crescendo.base.profile.ProfileManager;
 import crescendo.base.module.Module;
 
 /**
  * ProfileModule
  * 
  * This module is used to modify the attributes of a profile,
  * the preferences of a profile, and manage scores data for that
  * profile.
  * 
  * @author groszc
  *
  */
 public class ProfileModule extends Module implements ActionListener{
 	
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = -505754189840407725L;
 	
 	//general app pref elements
 	JTextField rename;
 	JButton renameProfileButton;
 	JButton deleteProfileButton;
 	
 	//sheet music pref elements
 	JButton deleteSheetMusicScoresButton;
 	
	ProfileModule(int width, int height)
 	{
 		super();
 		
 		//set the size of this module
		this.setSize(width, height);
 		this.setLayout(null);
 		
 		//set up the different tabs for the profile/prefs manager
 		JTabbedPane tabbedPane = new JTabbedPane();
 		tabbedPane.setBounds(0, 0, 1024, 768);
 		tabbedPane.setTabPlacement(JTabbedPane.LEFT);
 
 		//**********   General Profile Preferences   ***************//
 		JPanel generalPanel = new JPanel();
 		
 		GroupLayout layout=new GroupLayout(generalPanel);
 		generalPanel.setLayout(layout);
 		layout.setAutoCreateGaps(true);
 		layout.setAutoCreateContainerGaps(true);
 		
 		JLabel renameLabel=new JLabel("Rename Profile");
 		renameLabel.setVerticalAlignment(SwingConstants.CENTER);
 		rename=new JTextField(ProfileManager.getInstance().getActiveProfile().getName(),16);
 		rename.addActionListener(this);
 		renameProfileButton=new JButton("Rename");
 		renameProfileButton.addActionListener(this);
 		
 		deleteProfileButton = new JButton("Delete Profile");
 		deleteProfileButton.addActionListener(this);
 		
 		Group rows=layout.createSequentialGroup();
 		rows.addGroup(layout.createParallelGroup().addComponent(renameLabel).addComponent(rename,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE).addComponent(renameProfileButton));
 		rows.addGroup(layout.createParallelGroup().addComponent(deleteProfileButton));
 		Group cols=layout.createSequentialGroup();
		cols.addGroup(layout.createParallelGroup().addComponent(renameLabel).addComponent(deleteProfileButton));
		cols.addGroup(layout.createParallelGroup().addComponent(rename,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE,GroupLayout.PREFERRED_SIZE));
 		cols.addGroup(layout.createParallelGroup().addComponent(renameProfileButton));
 		layout.setHorizontalGroup(cols);
 		layout.setVerticalGroup(rows);
 		
 		tabbedPane.addTab("<html><body leftmargin=20 topmargin=8 marginwidth=20 marginheight=5>General</body></html>", null, generalPanel,"General Program Preferences");
 		
 		//**********   Sheet Music Profile Preferences   ***************//
 		JPanel sheetMusicPanel = new JPanel();
 		deleteSheetMusicScoresButton = new JButton("Delete sheet music scores");
 		deleteSheetMusicScoresButton.addActionListener(this);
 		sheetMusicPanel.add(deleteSheetMusicScoresButton);
 		sheetMusicPanel.setLayout(new BoxLayout(sheetMusicPanel, BoxLayout.Y_AXIS));
 		tabbedPane.addTab("<html><body leftmargin=20 topmargin=8 marginwidth=20 marginheight=5>Sheet Music</body></html>", null, sheetMusicPanel,"Sheet Music Preferences");
 		
 		
 		//**********   Lesson Profile Preferences   ***************//
 		//JPanel lessonPanel = new JPanel();
 		//renameProfileButton = new JButton("Rename Profile");
 		//renameProfileButton.addActionListener(al);
 		//lessonPanel(renameProfileButton);
 		//lessonPanel.setLayout(new BoxLayout(lessonPanel, BoxLayout.Y_AXIS));
 		//tabbedPane.addTab("<html><body leftmargin=20 topmargin=8 marginwidth=20 marginheight=5>Lessons</body></html>", null, lessonPanel,"Lesson Preferences");
 		
 		
 		
 		//**********   Game Profile Preferences   ***************//
 		JPanel gamePanel = new JPanel();
 		//renameProfileButton = new JButton("Rename Profile");
 		//renameProfileButton.addActionListener(al);
 		//gamePanel(renameProfileButton);
 		gamePanel.setLayout(new BoxLayout(gamePanel, BoxLayout.Y_AXIS));
 		tabbedPane.addTab("<html><body leftmargin=20 topmargin=8 marginwidth=20 marginheight=5>Game</body></html>", null, gamePanel,"Game Preferences");
 		
 		//add the tabbed pane to ourself
 		add(tabbedPane);
 	}
 	
 	/**
 	 * Profile/prefs module action listener
 	 */
 	public void actionPerformed(ActionEvent e) {
 		//if they chose to rename their profile
 		if(e.getSource()==renameProfileButton || e.getSource()==rename)
 		{
 			String name = rename.getText();
 			
 			if(name!=null)
 			{
 				ProfileManager.getInstance().renameProfile(ProfileManager.getInstance().getActiveProfile(), name);
 			}
 		
 			PianoTrainerApplication.getInstance().updateProfileMenu();
 		}
 		
 		
 		//if they chose to delete their profile
 		if(e.getSource().equals(deleteProfileButton))
 		{
 			if(ProfileManager.getInstance().getProfiles().size()<=0)
 			{
 				JOptionPane.showMessageDialog(null, "You cannot delete this profile unless another profile exists");
 			}
 			else
 			{
 				int response = JOptionPane.showConfirmDialog(null, "Delete this profile?", "Warning", JOptionPane.YES_NO_OPTION);
 				if(response == JOptionPane.YES_OPTION)
 				{
 					Profile toDelete = ProfileManager.getInstance().getActiveProfile();
 					ProfileManager.getInstance().switchProfile(ProfileManager.getInstance().getProfiles().get(0));
 					ProfileManager.getInstance().removeProfile(toDelete);
 				
 					PianoTrainerApplication.getInstance().updateProfileMenu();
 				}
 			}
 		}
 	
 	
 		//if they chose to delete the sheet music scores
 		if(e.getSource().equals(deleteSheetMusicScoresButton))
 		{
 			if(ErrorHandler.showYesNo("Delete Scores","Really delete scores for sheet music?\nThis cannot be undone.")==ErrorHandler.Response.YES)
 			{	
 				ProfileManager.getInstance().getActiveProfile().getSongPreferences().clear();
 			}
 
 		}
 	}
 
 	@Override
 	public String saveState() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void cleanUp() {
 		
 	}
 	
 }
