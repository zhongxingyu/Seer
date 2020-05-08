 package mainGUI;
 
 import java.awt.Color;
 import java.awt.Dimension;
 import java.awt.FlowLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.File;
 import java.util.Collection;
import java.util.LinkedList;
 import java.util.Vector;
 
 import javax.swing.BoxLayout;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JPanel;
 import javax.swing.JTextArea;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import dataStructures.AbstractDocument;
 import dataStructures.Attribute;
 import dataStructures.Member;
 import dataStructures.Role;
 import dataStructures.maps.EdgeStatementMap;
 import dataStructures.maps.MemberMap;
 import dataStructures.maps.RoleMap;
 import edu.uci.ics.jung.graph.Graph;
 
 public class SetupPreferencesPane extends UpdatePane implements ActionListener {
 
 	JFrame parent;
 	AbstractDocument document;
 	
 	boolean isMultipleStakeholder;
 	boolean isTCPPref;
 	
 	JComboBox stakeholderBox;
 	JPanel stakeholderControls;
 	
 	PreferencePane preferencesPanel;
 	Member curMember;
 	
 	JButton save;
 	JButton load;
 	JButton clear;
 	
 	JTextArea noMembers;
 	
 	public SetupPreferencesPane(JFrame parent, AbstractDocument document,
 			boolean isTCPPref){
 		this.parent = parent;
 		this.document = document;
 		this.isMultipleStakeholder = document.getRoleMap().isMultipleStakeholder();
 		this.isTCPPref = isTCPPref;
 		createGUI();
 	}
 	
 	private void setupStakeholderBox() {
 		RoleMap rm = document.getRoleMap();
 		Role[] roles = (Role[]) rm.values().toArray(new Role[0]);
 		Vector<Member> allMembers = new Vector<Member>();
 		for(Role role: roles) {
 			MemberMap members = role.getObject();
 			if (members != null) {
 				Collection<Member> roleMembers = members.values();
 				allMembers.addAll(roleMembers);
 
 			}
 		}
 		
 		stakeholderBox = new JComboBox<Member>(allMembers);
 		stakeholderBox.addActionListener(this);
 		stakeholderBox.invalidate();
 		if(allMembers.size() > 0){
 			stakeholderBox.setSelectedIndex(0);
 		} else {
 			curMember = null;
 		}
 	}
 	
 	@Override
 	public void update() {
 		preferencesPanel.update();
 		
 		stakeholderControls.removeAll();
 		
 		if(isMultipleStakeholder) {	
 			setupStakeholderBox();
 			stakeholderControls.add(stakeholderBox);
 		}
 		
 		stakeholderControls.add(save); 
 		stakeholderControls.add(load);
 		stakeholderControls.add(clear);
 		
 		if(curMember == null) {
 			stakeholderControls.setVisible(false);
 			preferencesPanel.setVisible(false);
 			noMembers.setVisible(true);
 		} else {
 			stakeholderControls.setVisible(true);
 			preferencesPanel.setVisible(true);
 			noMembers.setVisible(false);
 		}
 	}
 	
 	private void createGUI(){
 		setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
 				
 		initializePreferencePanel();
 		
 		save = new JButton("Save");
 		save.addActionListener(new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				if(curMember == null) {
 					System.out.println("SetupPreferencesPane -- member == null!!!");
 				} else if (curMember.getPreferenceFilePath() == null) {
 					savePreferencesAs();
 				} else {
 					savePreferences();
 				}
 				
 			}
 		});
 		
 		load = new JButton("Load Existing File");
 		load.addActionListener( new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent arg0) {
 				selectPreferencesFile();				
 			}
 		});
 		
 		clear = new JButton("Clear");
 		clear.addActionListener( new ActionListener() {
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				clearMemberPreferences();				
 			}
 		});
 		
 		stakeholderControls = new JPanel();
 		stakeholderControls.setLayout(new FlowLayout());
 				
 		if( !isMultipleStakeholder) {
 			RoleMap rm = document.getRoleMap();
 			Role defaultRole = rm.get(rm.firstKey());
 			MemberMap mMap = defaultRole.getObject();
 			Member defaultMember = mMap.get(0); // default member is always key 0
 			curMember = defaultMember;
 			loadMemberPreferences();
 		}
 		
 		noMembers = new JTextArea("There are currently no stakeholders in your project.\n"+
 				"Please create a stakeholder to input preferences.");
 		noMembers.setEditable(false);
 		noMembers.setPreferredSize(new Dimension(300, 40));
 		noMembers.setMaximumSize(noMembers.getPreferredSize());
 		noMembers.setBackground(new Color(255,255,255,0));
 		
 		update();
 		add(stakeholderControls);
 		add(preferencesPanel);
 		add(noMembers);
 		
 	}
 	
 	private void initializePreferencePanel(){
 		if(document.isCINetworkType()) {
 			preferencesPanel = new ImportancePane(document.getAttributeMap(), parent);
 		} else {
 			preferencesPanel = new SetupGraphPane(document, parent);
 		}
 	}
 	
	private PreferencePane getPreferencePanel() {
		return preferencesPanel;
	}
	
 	public Graph<Attribute, EdgeStatementMap> getGraph(){
 		if(isTCPPref)
 			return ((SetupGraphPane) preferencesPanel).getGraph();
 		else return null;
 	}
 
 	private void clearMemberPreferences() {
 		// sets attributes to unused
 		preferencesPanel.clearPane();
 		remove(preferencesPanel);
 			
 		initializePreferencePanel();
 		add(preferencesPanel);
 		revalidate();
 	}
 	
 	private void loadMemberPreferences() {
 		// Clear previous user's preferences					
 		clearMemberPreferences();
 		
 		if(curMember != null && curMember.getPreferenceFilePath() != null) {
 			File file = new File(curMember.getPreferenceFilePath());
 			preferencesPanel.loadMemberPreferences(file);
 		}
 		preferencesPanel.update();
 		revalidate();
 	}
 	
 	private void savePreferences() {
 		File memberFile = new File(curMember.getPreferenceFilePath());
 		preferencesPanel.saveMemberPreferences(memberFile);
 	}
 	
 	private void savePreferencesAs() {
 		JFileChooser chooser = new JFileChooser();
 		int option = chooser.showSaveDialog(this);
 		if (option == JFileChooser.APPROVE_OPTION) {
 			File file = chooser.getSelectedFile();
 			curMember.setPreferenceFilePath(file.getAbsolutePath());
 			savePreferences();
 		}
 	}
 	
 	private void selectPreferencesFile() {
 		//use a chooser to get the file to open
 		JFileChooser chooser = new JFileChooser();
 	    FileNameExtensionFilter filter = new FileNameExtensionFilter(
 	    		"XML (*.xml)","xml");
 	    chooser.setFileFilter(filter);
 	    int option = chooser.showOpenDialog(this);
 		if (option == JFileChooser.APPROVE_OPTION) {
 			File file = chooser.getSelectedFile();
 			loadExistingPreferences(file);
 		}
 	}
 	
 	private void loadExistingPreferences(File file) {
 		preferencesPanel.loadMemberPreferences(file);
 		curMember.setPreferenceFilePath(file.getAbsolutePath());
 	}
 	
 	@Override
 	public void actionPerformed(ActionEvent e) {
 		// TODO Auto-generated method stub
 		if( e.getSource() == stakeholderBox && curMember != stakeholderBox.getSelectedItem()) {
 			curMember = (Member) stakeholderBox.getSelectedItem();
 			loadMemberPreferences();
 		}
 	}
 
 }
