 package org.geworkbench.builtin.projects;
 
 import java.awt.BorderLayout;
 import java.awt.GridLayout;
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.awt.event.MouseEvent;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.File;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.sql.Timestamp;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.concurrent.ExecutionException;
 
 import javax.swing.DefaultComboBoxModel;
 import javax.swing.JButton;
 import javax.swing.JComboBox;
 import javax.swing.JDialog;
 import javax.swing.JLabel;
 import javax.swing.JOptionPane;
 import javax.swing.JPanel;
 import javax.swing.JPasswordField;
 import javax.swing.JScrollPane;
 import javax.swing.JTabbedPane;
 import javax.swing.JTable;
 import javax.swing.JTextArea;
 import javax.swing.JTextField;
 import javax.swing.ListSelectionModel;
 import javax.swing.event.ListSelectionEvent;
 import javax.swing.event.ListSelectionListener;
 
 import org.apache.commons.lang.StringUtils;
 import org.geworkbench.builtin.projects.WorkspaceHandler.OpenTask;
 import org.geworkbench.builtin.projects.WorkspaceHandler.SaveTask;
 import org.geworkbench.engine.config.rules.GeawConfigObject;
 import org.geworkbench.engine.preferences.GlobalPreferences;
 import org.geworkbench.util.FilePathnameUtils;
 import org.geworkbench.util.ProgressDialog;
 import org.geworkbench.util.ProgressItem;
 import org.geworkbench.util.ProgressTask;

 import com.jgoodies.forms.builder.DefaultFormBuilder;
 import com.jgoodies.forms.layout.FormLayout;
 
 /**
  * Remote Workspace Handler
  * @author mw2518
  * $Id$
  */
 public class RWspHandler {
 	protected static final String USER_INFO_DELIMIETER = "==";
 	protected static final String USER_INFO = "userinfo";
 	protected static String userInfo = null;
 	private JDialog loginDialog;
 	private JTextField usernameField;
 	private JPasswordField passwordField;
 	private static final String wsproot = FilePathnameUtils
 			.getTemporaryFilesDirectoryPath()
 			+ "wsp" + FilePathnameUtils.FILE_SEPARATOR;
 	protected static String wspdir = wsproot;
 	private static ProgressDialog pdmodal = ProgressDialog.create(ProgressDialog.MODAL_TYPE);
 	private static ProgressDialog pdnonmodal = ProgressDialog.create(ProgressDialog.NONMODAL_TYPE);
 	private WorkspaceHandler ws = new WorkspaceHandler();
 
 	private JTextArea jtaDesc = new JTextArea();
 	private JTextArea jtaAnno = new JTextArea();
 	private JButton descbtn = new JButton("Update");
 	private JButton addannobtn = new JButton("Add Annotation");
 	private JButton adduserbtn = new JButton("Add User Access");
 	private JButton addgroupbtn = new JButton("Add Group Access");
 	private JTextField jtfUsername = new JTextField();
 	private JComboBox grpnames = new JComboBox();
 	private JComboBox grpjcb = new JComboBox(new String[]{"remove", "read", "write"});
 	private JComboBox jcb = new JComboBox(new String[]{"remove", "read", "write", "admin"});
 	private JTable jthist = new JTable();
 	private JTable jtuser = new JTable();
 	private JTable jtanno = new JTable();
 	private RegPanel rp= new RegPanel();
 	private JTable jtgroup = new JTable();
 	private JTextField groupName = new JTextField(20);
 	private JTextField groupUser = new JTextField(20);
 	protected static final int LocalID = 0, IdID = 1, AccessID=4, LkUsrID=5, DirtyID=6, SyncID=7, LastSyncID=8, LastChangeID=9;
 	protected static final int LDWidth = 790, LDHeight = 330, SLDHeight = 300; 
 
 	protected static String checkoutstr = "";
 	protected static String lastchange = "";
 	protected static boolean dirty = false;
 	protected static int wspId = 0;
 	private JTable jt;
 	private JTextField name;
 	private JTextField desc;
 	private JDialog newDialog;
 	protected static JDialog listDialog;
 	private static String savedUserInfo = null;
 
 	protected void getUserInfo() {
 		FormLayout layout = new FormLayout("left:max(25dlu;pref), 5dlu, 78dlu");
 		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
 		builder.setDefaultDialogBorder();
 		loginDialog = new JDialog();
 		usernameField = new JTextField(20);
 		passwordField = new JPasswordField(20);
 		builder.append("Username", usernameField);
 		builder.append("Password", passwordField);
 		JButton login = new JButton("Login");
 		login.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				String username = usernameField.getText();
 				String passwd = new String(passwordField.getPassword());
 				if (username.trim().equals("")) {
 					userInfo = "";
 				} else {
 					userInfo = username + USER_INFO_DELIMIETER + RegPanel.getEncodedChars(passwordField.getPassword());
 					savedUserInfo = username + USER_INFO_DELIMIETER + passwd;
 				}
 				loginDialog.dispose();
 			}
 		});
 		JButton cancel = new JButton("Cancel");
 		cancel.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e ){
 				userInfo = null;
 				loginDialog.dispose();
 			}
 		});
 		JPanel buttons = new JPanel();
 		buttons.add(login);
 		buttons.add(cancel);
 		JButton register = new JButton("Register");
 		register.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				userInfo = null;
 				loginDialog.dispose();
 				RegPanel reg = new RegPanel();
 				reg.initFrame();
 				reg.showFrame();
 			}
 		});
 		builder.append(register, buttons);
 		
 		JButton skip = new JButton("Skip Authentication");
 		skip.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				userInfo = "local";
 				loginDialog.dispose();
 			}
 		});
 		builder.append("", skip);
 		
 		if (!StringUtils.isEmpty(savedUserInfo)) {
 			String s[] = savedUserInfo.split(USER_INFO_DELIMIETER, 2);
 			if (s.length >= 2) {
 				usernameField.setText(s[0]);
 				passwordField.setText(s[1]);
 			}
 		}
 		loginDialog.addWindowListener(new WindowAdapter(){
 			public void windowClosing(WindowEvent we){
 				userInfo = null;
 				loginDialog.dispose();
 			}
 		});
 		
 		loginDialog.getRootPane().setDefaultButton(login);
 		loginDialog.add(builder.getPanel());
 		loginDialog.setTitle("User Login");
 		loginDialog.setModal(true);
 		loginDialog.pack();
 		loginDialog.setLocationRelativeTo(null);
 		loginDialog.setVisible(true);
 	}
 
 	public void listWsp(boolean listOnly) {
 		if (userInfo == null || !listOnly) getUserInfo();
 		if (userInfo == null) return;
 		if (userInfo == "") {
 			JOptionPane
 					.showMessageDialog(
 							null,
 							"Please make sure you entered valid username and password",
 							"Invalid User Account",
 							JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 
 		ListRemoteTask profileTask = new ListRemoteTask(ProgressItem.INDETERMINATE_TYPE, "Remote workspace list is being retrieved.", listOnly);
 		pdnonmodal.executeTask(profileTask);
 	}
 
 	private class ListRemoteTask extends ProgressTask<HashMap<String, String[][]>, Void> {
 		private boolean listOnly = false;
 
 		ListRemoteTask(int pbtype, String message, boolean lo) {
 			super(pbtype, message);
 			listOnly = lo;
 		}
 		@Override
 		protected HashMap<String, String[][]> doInBackground() throws Exception {
 			HashMap<String, String[][]> hm = new HashMap<String, String[][]>();
 
 			try {
 				hm = DownloadClient.getSavedWorkspaceList(userInfo);
 			} catch (Exception e1) {
 				JOptionPane.showMessageDialog(null, e1.getMessage()+".\n\n"+
     					"GeWorkbench cannot retrieve remote workspace info from axis2 web service.\n" +
     					"Please try again later or report the problem to geWorkbench support team.\n",
     					"Database connection/data transfer error", JOptionPane.ERROR_MESSAGE);
     			return null;
 			}
 			return hm;
 		}
 		@Override
 		protected void done(){
 			if (isCancelled()){
 				pdnonmodal.removeTask(this);
 				return;
 			}
 
 			HashMap<String, String[][]> hm = new HashMap<String, String[][]>();
 			String[][] profiles = null, groups = null, dldwspname = null;
 			try {
 				hm = get();
 			} catch (ExecutionException e) {
 				e.getCause().printStackTrace();
 				JOptionPane.showMessageDialog(null,
 						"Exception: could not retrieve remote workspace.",
 						"List Remote Workspace Error", JOptionPane.ERROR_MESSAGE);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			} catch (Exception e) {
 				e.printStackTrace();
 				JOptionPane.showMessageDialog(null,
 						"Exception: could not retrieve remote workspace.\n"+e,
 						"List Remote Workspace Error", JOptionPane.ERROR_MESSAGE);
 			} finally {
 				pdnonmodal.removeTask(this);
 			}
 			if (hm == null || isCancelled()) return;
 
 			profiles = hm.get("PROFILE");
 			if (profiles != null){
 				int i=0;
 				for (java.awt.Component c: rp.getComponents()){
 					if(c instanceof JTextField && !(c instanceof JPasswordField) && i<profiles[0].length){
 						JTextField t = (JTextField)c;
 						t.setText(profiles[0][++i]);
 					} else if (c instanceof JPasswordField){
 						JPasswordField p = (JPasswordField)c;
 						p.setText("");
 
 					}
 				}
 			}
 
 			groups = hm.get("GROUP");
 			if (groups != null){
 				jtgroup.setModel(new RWspHelper.DetailTableModel(groups, DownloadClient.colgroup));
 				String[] names=new String[groups.length];
 				for (int i=0; i<groups.length; i++)
 					names[i]=groups[i][0];
 				grpnames.setModel(new DefaultComboBoxModel(names));
 			}
 
 			dldwspname = hm.get("LIST");
 			if (isCancelled()) return;
 
 			if (dldwspname != null){// && dldwspname.length > 0)
 				listWspDialog(dldwspname, listOnly);
 			}else 
     			JOptionPane.showMessageDialog(null, "No remote workspace available");
 			//insert else newWspDialog();
 		}
 	}
 
 	private void addUserGroup(){
 		String usr = groupUser.getText();
 		if (usr.equals("")) return;
 		int grpId = jtgroup.getSelectedRow();
 		if (grpId < 0) return;
 		String grp = (String)jtgroup.getModel().getValueAt(grpId, 0);
 		String res = "";
 		try{
 			getUserInfo();
 			if (userInfo == null) return;
 			if (userInfo == "") {
 				JOptionPane
 						.showMessageDialog(
 								null,
 								"Please make sure you entered valid username and password",
 								"Invalid User Account",
 								JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 
 			res = DownloadClient.modifySavedWorkspace("USERGROUP", null, usr, grp, userInfo);
 		}catch(Exception e1){
 			JOptionPane.showMessageDialog(null, e1.getMessage()+".\n\n"+
 	    			"GeWorkbench cannot add user to group for remote workspace via axis2 web service.\n" +
 	    			"Please try again later or report the problem to geWorkbench support team.\n",
 	    			"Database connection/data transfer error", JOptionPane.ERROR_MESSAGE);
 		}
 		if (res!=null && res.contains("success")){
 			JOptionPane.showMessageDialog(null, res);
 			listDialog.dispose();
 		} else
 			JOptionPane.showMessageDialog(null, "Could not add user to group to remote workspace "+usr);
 
 	}
 
 	private void addGroup(){
 		String grp = groupName.getText();
 		if (grp.equals("")) return;
 		String res = "";
 		try{
 			getUserInfo();
 			if (userInfo == null) return;
 			if (userInfo == "") {
 				JOptionPane
 						.showMessageDialog(
 								null,
 								"Please make sure you entered valid username and password",
 								"Invalid User Account",
 								JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			res = DownloadClient.modifySavedWorkspace("GROUP", null, null, grp, userInfo);
 		}catch(Exception e1){
 			JOptionPane.showMessageDialog(null, e1.getMessage()+".\n\n"+
 	    			"GeWorkbench cannot add user group for remote workspace via axis2 web service.\n" +
 	    			"Please try again later or report the problem to geWorkbench support team.\n",
 	    			"Database connection/data transfer error", JOptionPane.ERROR_MESSAGE);
 		}
 		if (res!=null && res.contains("success")){
 			JOptionPane.showMessageDialog(null, res);
 			listDialog.dispose();
 		} else
 			JOptionPane.showMessageDialog(null, "Could not add user group to remote workspace "+grp);
 
 	}
 
 	private JButton downloadBtn = new JButton("Open remote");
 	private JButton openLocalBtn = new JButton("Open local");
 	private JButton renameLocalBtn = new JButton("Rename local");
 	private JButton releaselockBtn = new JButton("Release lock");
 	private JButton breaklockBtn = new JButton("Break lock");
 	private JButton removeBtn = new JButton("Remove");
 	private void listWspDialog(String[][] dldwspname, boolean listOnly){
 		listDialog = new JDialog();
 		listDialog.setTitle("Remote workspace List ("+userInfo.split(USER_INFO_DELIMIETER)[0]+")");
 		JPanel jpw = new JPanel(new GridLayout(2, 0));
 
 		if (listOnly)
 			listDialog.add(jpw);
 		else{
 			JTabbedPane tp = new JTabbedPane();
 			listDialog.add(tp);
 
 			rp.changeForUpdate();
 			tp.addTab("Profile", rp);
 			tp.addTab("Workspaces", jpw);
 
 			JPanel mgp = new JPanel(new GridLayout(2, 0));
 			JButton createBtn = new JButton("Create Group");
 			createBtn.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					addGroup();
 				}
 			});
 			JButton addBtn = new JButton("Add Group User");
 			addBtn.addActionListener(new ActionListener() {
 				public void actionPerformed(ActionEvent e) {
 					addUserGroup();
 				}
 			});
 			FormLayout layout = new FormLayout("left:max(30dlu;pref), 10dlu, left:max(30dlu;pref), 10dlu, left:max(30dlu;pref)");
 			DefaultFormBuilder builder = new DefaultFormBuilder(layout);
 			builder.setDefaultDialogBorder();
 			builder.append("Group Name:", groupName, createBtn);
 			builder.append("User Name:", groupUser, addBtn);
 			mgp.add(builder.getPanel());
 	
 			jtgroup.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 			jtgroup.setPreferredScrollableViewportSize(jtgroup.getPreferredSize());
 			mgp.add(new JScrollPane(jtgroup));
 			tp.addTab("Manage Groups", mgp);
 
 		}
 
 		JPanel jp = new JPanel(new BorderLayout());
 		jt = new JTable(new RWspHelper.WspTableModel(dldwspname, DownloadClient.colnames)){
 			private static final long serialVersionUID = -477589541697891130L;
 
 			public String getToolTipText(MouseEvent e) {
 	             java.awt.Point p = e.getPoint();
 	             int rowIndex = rowAtPoint(p);
 	             int colIndex = columnAtPoint(p);
 	             int realColumnIndex = convertColumnIndexToModel(colIndex);
 
 	             if (realColumnIndex != IdID &&
 	            		 realColumnIndex != DirtyID && realColumnIndex != SyncID)
 	            	 return (String)getModel().getValueAt(rowIndex, realColumnIndex);
 	             else	return null;
 			 }
 		};
 		jt.setDefaultRenderer(Object.class, new RWspHelper.ColorRenderer());
 		jt.removeColumn(jt.getColumnModel().getColumn(jt.getColumnCount()-1));
 	    jt.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
 
 	    jt.getColumnModel().getColumn(IdID).setMaxWidth(30);
 	    jt.getColumnModel().getColumn(AccessID).setMaxWidth(50);
 	    jt.getColumnModel().getColumn(DirtyID).setMaxWidth(40);
 	    jt.getColumnModel().getColumn(SyncID).setMaxWidth(40);
 	    jt.getColumnModel().getColumn(LastSyncID).setPreferredWidth(140);
 	    jt.getColumnModel().getColumn(LastChangeID).setPreferredWidth(140);
 
 	    jt.setPreferredScrollableViewportSize(jt.getPreferredSize());
 	    ListSelectionListener listener = new ListSelectionListener(){
 			public void valueChanged(ListSelectionEvent e) {
 				if (!e.getValueIsAdjusting()) {
 					int selectedRow = jt.getSelectedRow();
 					if (selectedRow > -1)
 						selectionChanged(selectedRow);
 				}
 			}	
 	    };
 	    jt.getSelectionModel().addListSelectionListener(listener);
 	    JScrollPane jsp = new JScrollPane(jt);
 		jp.add(jsp, BorderLayout.CENTER);
 
 		JPanel bp = new JPanel();
 		downloadBtn.setEnabled(false);
 		downloadBtn.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 				int selectedRow = jt.getSelectedRow();
 				String remoteId = jt.getModel().getValueAt(selectedRow, IdID).toString();
 				String selectedco = jt.getModel().getValueAt(selectedRow, LastSyncID).toString();
 				listDialog.dispose();
 
 				saveLocalwsp(false);
 
 				openWsp(remoteId, selectedco);
 			}
 		});
 		bp.add(downloadBtn);
 		openLocalBtn.setEnabled(false);
 		openLocalBtn.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				int selectedRow = jt.getSelectedRow();
 				String localwspname = jt.getModel().getValueAt(selectedRow, LocalID).toString();
 				String wsFilename = wspdir+localwspname;
 				listDialog.dispose();
 
 				saveLocalwsp(false);
 
 				OpenTask openTask = ws.new OpenTask(ProgressItem.INDETERMINATE_TYPE, "Workspace is being loaded.", wsFilename);
 				pdnonmodal.executeTask(openTask);
 			}
 		});
 		bp.add(openLocalBtn);
 		renameLocalBtn.setEnabled(false);
 		renameLocalBtn.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				int selectedRow = jt.getSelectedRow();
 				String localwspname = jt.getValueAt(selectedRow, LocalID).toString();
 				String wsFilename = wspdir+localwspname;				
 
 				File f = new File(wsFilename);
 				String newName = JOptionPane.showInputDialog("Enter new local workspace name:");
 				if (newName!=null){
 					if (!newName.endsWith(".wsp"))
 						newName = newName+".wsp";
 					if (localwspname.equals(newName)){
 						JOptionPane.showMessageDialog(null, "Cannot rename to the same workspace name!");
 						return;
 					}
 					File nf = new File(wspdir+newName);
 					if (nf.exists()){
 						int n = JOptionPane.showConfirmDialog(null, 
 								"This local workspace name already exists. Do you want to overwrite it?",
 								"Confirm", JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE);
 						if (n == JOptionPane.CANCEL_OPTION || n == JOptionPane.NO_OPTION)
 							return;
 						if (!nf.delete()){
 							JOptionPane.showMessageDialog(null, "Failed to delete old file "+newName);
 							return;
 						}
 					}
 					if (!f.renameTo(nf)){
 						JOptionPane.showMessageDialog(null, "Failed to rename file to "+newName);
 						return;
 					}
 					listDialog.dispose();
 					int id = Integer.valueOf(jt.getValueAt(selectedRow, IdID).toString());
 					DownloadClient.wsprenames.put(id, newName);
 				}
 			}
 		});
 		bp.add(renameLocalBtn);
 		//release lock
 		releaselockBtn.setEnabled(false);
 		releaselockBtn.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				releaseLock("RELEASE");
 			}
 		});
 		bp.add(releaselockBtn);
 		//break lock
 		breaklockBtn.setEnabled(false);
 		breaklockBtn.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				releaseLock("BREAK");
 			}
 		});
 		bp.add(breaklockBtn);
 		//delete
 		removeBtn.setEnabled(false);
 		removeBtn.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e) {
 						removeWsp();
 				
 			}
 		});
 		bp.add(removeBtn);
 		JButton cancel = new JButton("Cancel");
 		cancel.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				listDialog.dispose();
 			}
 		});
 		bp.add(cancel);
 		jp.add(bp, BorderLayout.SOUTH);
 		jpw.add(jp);
 
 		JTabbedPane jtp = new JTabbedPane();
 		JPanel descpanel = new JPanel(new BorderLayout());
 		descpanel.add(jtaDesc, BorderLayout.CENTER);
 		descbtn.setEnabled(false);
 		descbtn.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				addDescription();
 			}
 		});
 		descpanel.add(descbtn, BorderLayout.LINE_END);
 		jtp.addTab("Description", descpanel);
 		jtanno.setPreferredScrollableViewportSize(jtanno.getPreferredSize());
 		jtp.addTab("Annotation", new JScrollPane(jtanno));
 		jthist.setPreferredScrollableViewportSize(jthist.getPreferredSize());
 		jtp.addTab("History", new JScrollPane(jthist));
 		jtuser.setPreferredScrollableViewportSize(jtuser.getPreferredSize());
 		jtp.addTab("Users", new JScrollPane(jtuser));
 		JPanel addannopanel = new JPanel(new BorderLayout());
 		addannopanel.add(jtaAnno, BorderLayout.CENTER);
 		addannobtn.setEnabled(false);
 		addannobtn.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				addAnnotation();
 			}
 		});
 		addannopanel.add(addannobtn, BorderLayout.LINE_END);
 		jtp.addTab("Add Annotation", addannopanel);
 		JPanel adduserpanel = new JPanel(new GridLayout(3, 2));
 		adduserpanel.add(new JLabel("Enter username: "));
 		adduserpanel.add(jtfUsername);
 		adduserpanel.add(new JLabel("Choose user access priviledge: "));
 		adduserpanel.add(jcb);
 		adduserpanel.add(new JLabel("Give user access to this workspace: "));
 		adduserbtn.setEnabled(false);
 		adduserbtn.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				addUser();
 			}
 		});
 		adduserpanel.add(adduserbtn);
 		jtp.addTab("Add User Access", adduserpanel);
 		JPanel addgrouppanel = new JPanel(new GridLayout(3, 2));
 		addgrouppanel.add(new JLabel("Choose group: "));
 		addgrouppanel.add(grpnames);
 		addgrouppanel.add(new JLabel("Choose group access priviledge: "));
 		addgrouppanel.add(grpjcb);
 		addgrouppanel.add(new JLabel("Give group access to this workspace: "));
 		addgroupbtn.setEnabled(false);
 		addgroupbtn.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				addGroupAccess();
 			}
 		});
 		addgrouppanel.add(addgroupbtn);
 		jtp.addTab("Add Group Access", addgrouppanel);
 		jpw.add(jtp);
 
 		// else JOptionPane.showMessageDialog(null, "No user profiles for remote workspace available");
 
 		if (wspId > 0){
 			for (int i = 0; i < jt.getRowCount(); i++){
 				if (Integer.valueOf(jt.getValueAt(i, IdID).toString()) == wspId){
 					jt.getSelectionModel().setSelectionInterval(i, i);
 				}
 			}
 		}
 
 		listDialog.pack();
 		listDialog.setSize(LDWidth, LDHeight);
 		if (listOnly) listDialog.setSize(LDWidth, SLDHeight);
 		listDialog.setLocationRelativeTo(null);
 		listDialog.setVisible(true);
 	}
 
 	private void selectionChanged(int selectedRow){
 		String localwspname = (String)jt.getValueAt(selectedRow, LocalID);
 		if (localwspname!=null && !localwspname.equals("")){
 			openLocalBtn.setEnabled(true);
 			renameLocalBtn.setEnabled(true);
 		} else {
 			openLocalBtn.setEnabled(false);
 			renameLocalBtn.setEnabled(false);
 		}
 		String lkusr = (String)jt.getValueAt(selectedRow, LkUsrID);
 		if (lkusr!=null && !lkusr.equals("")){
 			releaselockBtn.setEnabled(true);
 			breaklockBtn.setEnabled(true);
 			removeBtn.setEnabled(false);
 		} else {
 			releaselockBtn.setEnabled(false);
 			breaklockBtn.setEnabled(false);
 			removeBtn.setEnabled(true);
 		}
 
 		String access = (String)jt.getValueAt(selectedRow, AccessID);
 		if (access!=null && !access.equals("")){
 			downloadBtn.setEnabled(true);
 			descbtn.setEnabled(true);
 			addannobtn.setEnabled(true);
 			adduserbtn.setEnabled(true);
 			addgroupbtn.setEnabled(true);
 			String desc = (String) jt.getModel().getValueAt(selectedRow, jt.getColumnCount());
 			jtaDesc.setText(desc);
 	
 			String wspid = (String) jt.getValueAt(selectedRow, IdID);
 			
 			HashMap<String, String[][]> hm = new HashMap<String, String[][]>();
 			String[][] listhist = null, listuser = null, listanno = null;
 			try {
 				hm = DownloadClient.getSavedWorkspaceInfo(wspid);
 			} catch (Exception e1) {
 				JOptionPane.showMessageDialog(null, e1.getMessage()+"\n\n"+
 						"Exception: could not retrieve information for remote workspace "+ wspid,
 						"Database connection/data transfer error", JOptionPane.ERROR_MESSAGE);
 			}
 			if (hm!=null){
 				listhist = hm.get("HIST");
 				if (listhist != null){
 					jthist.setModel(new RWspHelper.DetailTableModel(listhist, DownloadClient.colhist));
 				}
 				listuser = hm.get("GETUSER");
 				if (listuser!=null){
 					jtuser.setModel(new RWspHelper.DetailTableModel(listuser, DownloadClient.coluser));
 				}
 				listanno = hm.get("GETANNO");
 				if (listanno != null){
 					jtanno.setModel(new RWspHelper.DetailTableModel(listanno, DownloadClient.colanno));
 				}
 			}
 		}
 	}
 
 	//save current local wsp and release lock before opening another remote wsp
 	protected static void saveLocalwsp(boolean terminating){
 		if (!doSaveLocal(terminating)) return;
 
 		//if workspace being closed is locked by current user
 		if (userInfo.equals("local")){
 			RWspHelper.CheckReleaseRemoteTask releaseTask = new RWspHelper.CheckReleaseRemoteTask(ProgressItem.INDETERMINATE_TYPE, 
 					"Checking remote workspace "+wspId+" lock before release.", wspId);
 			pdnonmodal.executeTask(releaseTask);
 		}
 		//reset wspId
 		resetWsp();
 	}
 	protected static boolean doSaveLocal(boolean terminating){
 		if (wspId == 0) return false;
 		String fname = wspId+".wsp";
 		String rename = DownloadClient.wsprenames.get(wspId); 
 		if (rename!=null) fname=rename;
 		if (fname!=null && !fname.equals("") && dirty) {
 			String wsFilename = wspdir+fname;
 			File file = new File(wsFilename);
 			if (file.exists()){
 				Timestamp filetime = new Timestamp(file.lastModified());
 				filetime.setNanos(0);
 				//System.out.println(wsFilename+": "+Timestamp.valueOf(lastchange)+"; "+filetime);
 				if (Timestamp.valueOf(lastchange).after(filetime)){
 					SaveTask task = new WorkspaceHandler().new SaveTask(ProgressItem.INDETERMINATE_TYPE, "Workspace is being saved.", wsFilename, terminating);
 					pdmodal.executeTask(task);
 				}
 			}
 		}
 		if(terminating) {
 			GeawConfigObject.getGuiWindow().dispose();
 			System.exit(0);
 		}
 		return true;
 	}
 
 	protected static void resetWsp(){
 		wspId = 0;
 		dirty = false;
 		checkoutstr = "";
 		lastchange = "";
 	}
 
 	private void releaseLock(String type){
 		int selectedRow = jt.getSelectedRow();
 		String remoteId = jt.getModel().getValueAt(selectedRow, IdID).toString();
 		String res = "";
 		try{
 			getUserInfo();
 			if (userInfo == null) return;
 			if (userInfo == "") {
 				JOptionPane
 						.showMessageDialog(
 								null,
 								"Please make sure you entered valid username and password",
 								"Invalid User Account",
 								JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 
 			res = DownloadClient.modifySavedWorkspace(type, remoteId, null, null, userInfo);
 		}catch(Exception e1){
 			JOptionPane.showMessageDialog(null, e1.getMessage()+".\n\n"+
 	    			"GeWorkbench cannot release lock for remote workspace via axis2 web service.\n" +
 	    			"Please try again later or report the problem to geWorkbench support team.\n",
 	    			"Database connection/data transfer error", JOptionPane.ERROR_MESSAGE);
 		}
 		if (res!=null && res.contains("success")){
 			JOptionPane.showMessageDialog(null, res);
 			listDialog.dispose();
 		} else
 			JOptionPane.showMessageDialog(null, "Could not release remote workspace "+remoteId);
 	}
 	
 	private void addDescription(){
 		int selectedRow = jt.getSelectedRow();
 		String remoteId = jt.getModel().getValueAt(selectedRow, IdID).toString();
 		String res = "";
 		try{
 			getUserInfo();
 			if (userInfo == null) return;
 			if (userInfo == "") {
 				JOptionPane
 						.showMessageDialog(
 								null,
 								"Please make sure you entered valid username and password",
 								"Invalid User Account",
 								JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			String desc = jtaDesc.getText();
 			res = DownloadClient.modifySavedWorkspace("DESC", remoteId, desc, null, userInfo);
 		}catch(Exception e1){
 			JOptionPane.showMessageDialog(null, e1.getMessage()+".\n\n"+
 					"GeWorkbench cannot change description for remote workspace via axis2 web service.\n" +
     				"Please try again later or report the problem to geWorkbench support team.\n",
    					"Database connection/data transfer error", JOptionPane.ERROR_MESSAGE);
 		}
 		if (res!=null && res.contains("success")){
 			JOptionPane.showMessageDialog(null, res);
 			listDialog.dispose();
 		} else
 			JOptionPane.showMessageDialog(null, "Could not change description for remote workspace "+remoteId);
 	}
 
 	private void addAnnotation(){
 		int selectedRow = jt.getSelectedRow();
 		String remoteId = jt.getModel().getValueAt(selectedRow, IdID).toString();
 		String res = "";
 		try{
 			getUserInfo();
 			if (userInfo == null) return;
 			if (userInfo == "") {
 				JOptionPane
 						.showMessageDialog(
 								null,
 								"Please make sure you entered valid username and password",
 								"Invalid User Account",
 								JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			String anno = jtaAnno.getText();
 			res = DownloadClient.modifySavedWorkspace("ADDANNO", remoteId, anno, null, userInfo);
 		}catch(Exception e1){
 			JOptionPane.showMessageDialog(null, e1.getMessage()+".\n\n"+
 	   				"GeWorkbench cannot add annotation for remote workspace via axis2 web service.\n" +
 	   				"Please try again later or report the problem to geWorkbench support team.\n",
 	   				"Database connection/data transfer error", JOptionPane.ERROR_MESSAGE);
 		}
 		if (res!=null && res.contains("success")){
 			JOptionPane.showMessageDialog(null, res);
 			listDialog.dispose();
 		} else
 			JOptionPane.showMessageDialog(null, "Could not add annotation for remote workspace "+remoteId);
 	}
 
 	private void addUser(){
 		int selectedRow = jt.getSelectedRow();
 		String remoteId = jt.getModel().getValueAt(selectedRow, IdID).toString();
 		String res = "";
 		try{
 			getUserInfo();
 			if (userInfo == null) return;
 			if (userInfo == "") {
 				JOptionPane
 						.showMessageDialog(
 								null,
 								"Please make sure you entered valid username and password",
 								"Invalid User Account",
 								JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			String uname = jtfUsername.getText();
 			String group = (String)jcb.getSelectedItem();
 			res = DownloadClient.modifySavedWorkspace("ADDUSER", remoteId, uname, group, userInfo);
 		}catch(Exception e1){
 			JOptionPane.showMessageDialog(null, e1.getMessage()+".\n\n"+
 					"GeWorkbench cannot give user access to remote workspace via axis2 web service.\n" +
 					"Please try again later or report the problem to geWorkbench support team.\n",
 					"Database connection/data transfer error", JOptionPane.ERROR_MESSAGE);
 		}
 		if (res!=null && res.contains("success")) {
 			JOptionPane.showMessageDialog(null, res);
 			listDialog.dispose();
 		} else
 			JOptionPane.showMessageDialog(null, "Could not give user access to remote workspace "+remoteId);
 	}
 
 	private void addGroupAccess(){
 		int selectedRow = jt.getSelectedRow();
 		String remoteId = jt.getModel().getValueAt(selectedRow, IdID).toString();
 		String res = "";
 		try{
 			getUserInfo();
 			if (userInfo == null) return;
 			if (userInfo == "") {
 				JOptionPane
 						.showMessageDialog(
 								null,
 								"Please make sure you entered valid username and password",
 								"Invalid User Account",
 								JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 			String uname = (String)grpnames.getSelectedItem();
 			String group = (String)grpjcb.getSelectedItem();
 			res = DownloadClient.modifySavedWorkspace("ADDGROUP", remoteId, uname, group, userInfo);
 		}catch(Exception e1){
 			JOptionPane.showMessageDialog(null, e1.getMessage()+".\n\n"+
 					"GeWorkbench cannot give group access to remote workspace via axis2 web service.\n" +
 					"Please try again later or report the problem to geWorkbench support team.\n",
 					"Database connection/data transfer error", JOptionPane.ERROR_MESSAGE);
 		}
 		if (res!=null && res.contains("success")) {
 			JOptionPane.showMessageDialog(null, res);
 			listDialog.dispose();
 		} else
 			JOptionPane.showMessageDialog(null, "Could not give group access to remote workspace "+remoteId);
 	}
 
 
 	private void newWspDialog(){
 
 		FormLayout layout = new FormLayout("left:max(25dlu;pref), 10dlu, left:max(50dlu;pref)");
 		DefaultFormBuilder builder = new DefaultFormBuilder(layout);
 		builder.setDefaultDialogBorder();
 		newDialog = new JDialog();
 		name = new JTextField(20);
 		desc = new JTextField(20);
 		builder.append("Workspace title", name);
 		builder.append("Description", desc);
 		JButton upload = new JButton("Upload");
 		upload.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e){
 				String wsFilename = name.getText();
 				if( wsFilename==null || wsFilename.length()==0 ){
 					JOptionPane.showMessageDialog(newDialog, "Workspace name cannot be blank.");
 					return;
 				}
 				newDialog.dispose();
 
 				String description = desc.getText();
 
 				uploadWsp(wsFilename, description, false);
 			}
 		});
 		JButton cancel = new JButton("Cancel");
 		cancel.addActionListener(new ActionListener(){
 			public void actionPerformed(ActionEvent e ){
 				newDialog.dispose();
 			}
 		});
 		JPanel buttons = new JPanel();
 		buttons.add(upload);
 		buttons.add(cancel);
 		builder.append("", buttons);
 		newDialog.getRootPane().setDefaultButton(upload);
 		newDialog.add(builder.getPanel());
 		newDialog.setTitle("Upload New Workspace");
 		newDialog.pack();
 		newDialog.setVisible(true);
 		newDialog.setLocationRelativeTo(null);
 	}
 
 	private void openWsp(String wsFilename, String selectedco) {
 		int id = Integer.valueOf(wsFilename);
 		wsFilename = wsFilename+".wsp";
 		RWspHelper.CheckOpenRemoteTask openTask = new RWspHelper.CheckOpenRemoteTask(ProgressItem.INDETERMINATE_TYPE, 
 				"Checking remote workspace "+wsFilename+" access before downloading.", wsFilename, selectedco, id);
 		pdnonmodal.executeTask(openTask);
 	}
 
 	protected static boolean checkWspdir(){
 		File dir = new File(wsproot);
 		if (!dir.exists()){
 			if (!dir.mkdir()){
 				JOptionPane.showMessageDialog(null, "Cannot create wsp dir "+wsproot);
 				return false;
 			}
 		}
 
 		URL url = null;
 		try{
 			url = new URL(GlobalPreferences.getInstance().getRWSP_URL());
 		}catch(MalformedURLException e){
 			JOptionPane.showMessageDialog(null, "Malformed RWSP URL detected -- replaced by default RWSP URL.");
 			try{
 				url = new URL(GlobalPreferences.DEFAULT_RWSP_URL);
 			}catch(Exception e1){
 				e1.printStackTrace();
 				return false;
 			}
 		}
 		wspdir = wsproot + url.getHost() + url.getPort() + FilePathnameUtils.FILE_SEPARATOR;
 		dir = new File(wspdir);
 		if (!dir.exists()){
 			if (!dir.mkdir()){
 				JOptionPane.showMessageDialog(null, "Cannot create wsp dir "+wspdir);
 				return false;
 			}
 		}
 		
 		DownloadClient.cachedir = wspdir+"axis2cache";
 		return true;
 	}
 
 	protected void uploadWsp(){
 		if(!checkWspdir()) return;
 
 		if (wspId == 0) //insert
 			newWspDialog();
 		else //update
 			updateWsp(wspId, false);
 	}
 	
 	private void uploadWsp(String wsFilename, String desc, boolean terminating) {
 		getUserInfo();
 		if (userInfo == null) return;
 		if (userInfo == "") {
 			JOptionPane
 					.showMessageDialog(
 							null,
 							"Please make sure you entered valid username and password",
 							"Invalid User Account",
 							JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 		
 		RWspHelper.UploadNewTask saveTask = new RWspHelper.UploadNewTask(ProgressItem.INDETERMINATE_TYPE, 
 				"Remote workspace "+wsFilename+" is being saved and uploaded.", 
 				wsFilename, desc, terminating);
 		pdmodal.executeTask(saveTask);
 	}
 
 	private void updateWsp(int id, boolean terminating){
 		if (!dirty)
 			JOptionPane.showMessageDialog(null, "The workspace hasn't been changed");
 		else{
 			getUserInfo();
 			if (userInfo == null) return;
 			if (userInfo == "") {
 				JOptionPane
 						.showMessageDialog(
 								null,
 								"Please make sure you entered valid username and password",
 								"Invalid User Account",
 								JOptionPane.ERROR_MESSAGE);
 				return;
 			}
 
 			String fname = id+".wsp";
 			String rename = DownloadClient.wsprenames.get(id); 
 			if (rename!=null) fname=rename;
 			RWspHelper.CheckUpdateRemoteTask saveTask = new RWspHelper.CheckUpdateRemoteTask(ProgressItem.INDETERMINATE_TYPE, 
 					"Checking remote workspace "+id+" access before updating.", wspdir+fname, 
 					terminating, id);
 			pdnonmodal.executeTask(saveTask);
 		}
 	}
 
 	private void removeWsp(){
 		int selectedRow = jt.getSelectedRow();
 		String wsFilename = jt.getModel().getValueAt(selectedRow, IdID).toString();
 		String localwspname = jt.getModel().getValueAt(selectedRow, LocalID).toString();
 
 		getUserInfo();
 		if (userInfo == null) return;
 		if (userInfo == "") {
 			JOptionPane
 					.showMessageDialog(
 							null,
 							"Please make sure you entered valid username and password",
 							"Invalid User Account",
 							JOptionPane.ERROR_MESSAGE);
 			return;
 		}
 
 		RWspHelper.RemoveRemoteTask removeTask = new RWspHelper.RemoveRemoteTask(ProgressItem.INDETERMINATE_TYPE,
 				"Remote workspace "+wsFilename+" is being removed.", wsFilename, localwspname);
 		pdnonmodal.executeTask(removeTask);
 	}
 
 	protected static void treeModified(){
 		if (wspId > 0){
 			Timestamp now = new Timestamp(new Date().getTime());
 			now.setNanos(0);
 			lastchange = now.toString();
 			//System.out.println("treemodified:"+wspId+"; "+dirty);
 			if (dirty == false){
 				dirty = true;
 
 				RWspHelper.AccessRemoteTask accessTask = new RWspHelper.AccessRemoteTask(ProgressItem.INDETERMINATE_TYPE,
 				"Remote workspace status is being retrieved.", wspId);
 				pdnonmodal.executeTask(accessTask);
 			}
 		}
 	}
 }
