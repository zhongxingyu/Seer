 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * MemberListPanel.java
  *
  * Created on 2011-12-13, 9:08:39
  */
 package com.lorent.lvmc.ui;
 
 import java.awt.Component;
 import java.awt.dnd.DnDConstants;
 import java.awt.dnd.DragSource;
 import java.awt.event.ActionEvent;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.swing.BorderFactory;
 import javax.swing.DefaultListCellRenderer;
 import javax.swing.DefaultListModel;
 import javax.swing.JFrame;
 import javax.swing.JList;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import javax.swing.UIManager;
 
 import org.apache.log4j.Logger;
 
 import com.lorent.common.dnd.MyDragGestureListener;
 import com.lorent.lvmc.controller.ControllerFacade;
 import com.lorent.lvmc.controller.ViewManager;
 import com.lorent.lvmc.dto.LoginInfo;
 import com.lorent.lvmc.dto.MemberDto;
 import com.lorent.lvmc.util.Constants;
 import com.lorent.lvmc.util.DataUtil;
 import com.lorent.lvmc.util.PermissionUtil;
 import com.lorent.lvmc.util.StringUtil;
 
 /**
  *
  * @author jack
  */
 public class MemberListPanel extends javax.swing.JPanel {
 
 	private Logger log = Logger.getLogger(MemberListPanel.class);
 	private List<MemberDto> members = new ArrayList<MemberDto>();
 	private boolean showPermission;
 	private javax.swing.JPopupMenu menu;
 	private javax.swing.JMenuItem grantNarratorMenuItem;
 	private javax.swing.JMenuItem revokeNarratorMenuItem;
 	private javax.swing.JMenuItem grantCompereMenuItem;
 
 	/** Creates new form MemberListPanel */
 	public MemberListPanel() {
 		initComponents();
 		memberList.setModel(new DefaultListModel());
 		memberList.setCellRenderer(new MyListCellRenderer());
 		DefaultListModel model = (DefaultListModel) memberList.getModel();
 		model.removeAllElements();
 		this.operatePanel.setVisible(false);
 		this.inviteButton.setVisible(false);
 		this.kickButton.setVisible(false);
 		if (PermissionUtil.hasPermission(PermissionUtil.INVITE_JOIN_CONFERENCE)) {
 			this.operatePanel.setVisible(true);
 			this.inviteButton.setVisible(true);
 		}
 		if (PermissionUtil.hasPermission(PermissionUtil.KICK_FROM_CONFERENCE)) {
 			this.operatePanel.setVisible(true);
 			this.kickButton.setVisible(true);
 		}
 		menu = new javax.swing.JPopupMenu();
 		grantNarratorMenuItem = new javax.swing.JMenuItem("授予主讲人权限");
 		revokeNarratorMenuItem = new javax.swing.JMenuItem("取消主讲人权限");
 		grantCompereMenuItem = new javax.swing.JMenuItem("授予主持人权限");
 		menu.add(grantNarratorMenuItem);
 		menu.add(revokeNarratorMenuItem);
 		menu.add(grantCompereMenuItem);
 		grantNarratorMenuItem.addActionListener(new java.awt.event.ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				grantOrRevokeAuthority(Constants.GRANT_AUTHORITY,Constants.NARRATOR_STR);//授予主讲人权限
 			}
 			
 		});
 		revokeNarratorMenuItem.addActionListener(new java.awt.event.ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				grantOrRevokeAuthority(Constants.REVOKE_AUTHORITY,Constants.NARRATOR_STR);//取消主讲人权限
 			}
 			
 		});
 		grantCompereMenuItem.addActionListener(new java.awt.event.ActionListener(){
 			@Override
 			public void actionPerformed(ActionEvent e) {
 				grantOrRevokeAuthority(Constants.GRANT_AUTHORITY,Constants.COMPERE_STR);//取消主持人权限
 			}
 			
 		});
 	}
 	
 	
 	public void grantOrRevokeAuthority(int flag,String roleName){
 		this.menu.setVisible(false);
 		MemberListItem item = (MemberListItem) memberList
 		.getSelectedValue();
 		ControllerFacade.execute("authorityController", "grantOrRevokeConfAuthority", item , flag, roleName);
 	}
 
 	private class MyListCellRenderer extends DefaultListCellRenderer {
 
 		@Override
 		public Component getListCellRendererComponent(JList list, Object value,
 				int index, boolean isSelected, boolean cellHasFocus) {
 			//            MemberDto data = (MemberDto) value;
 			//            System.out.println(data.getName());
 
 			MemberListItem item = (MemberListItem) value;
 			//            item.setData(data, showPermission);
 			item.setTextForeground(UIManager.getColor("List.foreground"));
 			if (isSelected || cellHasFocus) {
 				//                item.setBackground(UIManager.getColor("List.selectionBackground"));
 				//                item.setBorder(BorderFactory.createLineBorder(UIManager.getColor("List.selectionForeground")));
 				item.setBorder(BorderFactory.createEtchedBorder());
 				//                item.setBackground(Color.WHITE);
 			} else {
 				//            	item.setBorder(BorderFactory.createEmptyBorder());
 				item.setBorder(null);
 				//                item.setBackground(UIManager.getColor("List.background"));
 			}
 			item.repaint();
 			return item;
 		}
 	}
 
 	public void setShowPermission(boolean showPermission) {
 		this.showPermission = showPermission;
 		DefaultListModel model = (DefaultListModel) memberList.getModel();
 		for (int i = 0; i < model.size(); i++) {
 			MemberListItem item = (MemberListItem) model.get(i);
 			MemberDto data = item.getData();
 			item.setData(data, showPermission);
 		}
 	}
 
 	public void clear() {
 		DefaultListModel model = (DefaultListModel) memberList.getModel();
 		model.removeAllElements();
 	}
 
 	public void showAllMembers(List<MemberDto> members) {
 		if (members != null && members.size() > 0) {
 			DefaultListModel model = (DefaultListModel) memberList.getModel();
 			model.removeAllElements();
 			for (MemberDto member : members) {
 				MemberListItem memberListItem = new MemberListItem();
 				memberListItem.setData(member, showPermission);
 				model.addElement(memberListItem);
 			}
 			this.revalidate();
 			this.repaint();
 			this.members = members;
 		}
 		MyDragGestureListener myDragGestureListener = new MyDragGestureListener();
 		myDragGestureListener.setPropery("object", memberList);
 		new DragSource().createDefaultDragGestureRecognizer(memberList,
 				DnDConstants.ACTION_MOVE, myDragGestureListener);
 	}
 
 	public void joinOneMember(MemberDto member) {
 		DefaultListModel model = (DefaultListModel) memberList.getModel();
 		MemberListItem memberListItem = getMemberListItemByName(member
 				.getName());
 		if (memberListItem == null) {
 			memberListItem = new MemberListItem();
 			model.addElement(memberListItem);
 			this.members.add(member);
 		} else {
 			log.info("已有" + member.getName() + "号码，不需要加入");
 		}
 		memberListItem.setData(member, showPermission);
 	}
 
 	public void removeOneMember(String member) {
 		DefaultListModel model = (DefaultListModel) memberList.getModel();
 		MemberDto temp = getMemberByName(member);
 		if (temp == null) {
 			return;
 		} else {
 			log.info("已有" + member + "号码，将要删除");
 		}
 		MemberListItem memberListItem = getMemberListItemByName(member);
 		model.removeElement(memberListItem);
 		this.members.remove(temp);
 	}
 
 	public MemberListItem getMemberListItemByName(String name) {
 		DefaultListModel model = (DefaultListModel) memberList.getModel();
 		for (int i = 0; i < model.size(); i++) {
 			MemberListItem item = (MemberListItem) model.get(i);
 			MemberDto data = item.getData();
 			if (data.getName().equals(name)) {
 				return item;
 			}
 		}
 		return null;
 	}
 
 	public MemberDto getMemberByName(String name) {
 		for (MemberDto member : members) {
 			if (member.getName().equals(name)) {
 				return member;
 			}
 		}
 		return null;
 	}
 
 	public static void main1(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				MemberListPanel memberListPanel = new MemberListPanel();
 				List<MemberDto> temp = new ArrayList<MemberDto>();
 
 				memberListPanel.showAllMembers(temp);
 				memberListPanel.setShowPermission(true);
 
 				JFrame frame = new JFrame();
 				frame.setSize(300, 600);
 				frame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
 				frame.add(memberListPanel);
 				frame.setVisible(true);
 
 			}
 		});
 	}
 
 	/** This method is called from within the constructor to
 	 * initialize the form.
 	 * WARNING: Do NOT modify this code. The content of this method is
 	 * always regenerated by the Form Editor.
 	 */
 	@SuppressWarnings("unchecked")
 	//GEN-BEGIN:initComponents
 	// <editor-fold defaultstate="collapsed" desc="Generated Code">
 	private void initComponents() {
 
 		jScrollPane1 = new javax.swing.JScrollPane();
 		memberList = new javax.swing.JList();
 		operatePanel = new javax.swing.JPanel();
 		inviteButton = new javax.swing.JButton();
 		kickButton = new javax.swing.JButton();
 
 		setLayout(new java.awt.BorderLayout());
 
 		memberList
 				.setSelectionMode(javax.swing.ListSelectionModel.SINGLE_SELECTION);
 		memberList.addMouseListener(new java.awt.event.MouseAdapter() {
 			public void mouseClicked(java.awt.event.MouseEvent evt) {
 				memberListMouseClicked(evt);
 			}
 		});
 		jScrollPane1.setViewportView(memberList);
 
 		add(jScrollPane1, java.awt.BorderLayout.CENTER);
 
 		operatePanel
 				.setLayout(new java.awt.FlowLayout(java.awt.FlowLayout.LEFT));
 
 		inviteButton.setText(StringUtil
 				.getUIString("MemberListPanel.inviteButton.txt"));
 		inviteButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				inviteButtonActionPerformed(evt);
 			}
 		});
 		operatePanel.add(inviteButton);
 
 		kickButton.setText(StringUtil
 				.getUIString("MemberListPanel.kickButton.txt"));
 		kickButton.addActionListener(new java.awt.event.ActionListener() {
 			public void actionPerformed(java.awt.event.ActionEvent evt) {
 				kickButtonActionPerformed(evt);
 			}
 		});
 		operatePanel.add(kickButton);
 
 		add(operatePanel, java.awt.BorderLayout.SOUTH);
 	}// </editor-fold>
 	//GEN-END:initComponents
 
 	private void kickButtonActionPerformed(java.awt.event.ActionEvent evt) {
 		MemberListItem item = (MemberListItem) this.memberList
 				.getSelectedValue();
 		if (item != null) {
 			MemberDto data = item.getData();
 			//			LoginInfo loginInfo = DataUtil.getValue(DataUtil.Key.LoginInfo);
 			//			ControllerFacade.execute("mainController", "kickOneFromConference",data);
 			String lccno = item.getData().getName();
 			ControllerFacade.execute("mainController", "removeUser", lccno);
 		} else {
 			JOptionPane.showMessageDialog(null, StringUtil
 					.getErrorString("MemberListPanel.notSelected"));
 		}
 	}
 
 	private void inviteButtonActionPerformed(java.awt.event.ActionEvent evt) {
 		ControllerFacade.execute("mainController", "showInviteDialog");
 	}
 
 	private void memberListMouseClicked(java.awt.event.MouseEvent evt) {//GEN-FIRST:event_memberListMouseClicked
 		//        Object elementAt = memberList.getModel().getElementAt(memberList.locationToIndex(evt.getPoint()));
 		//        Object selectedValue = memberList.getSelectedValue();
 		//        ListSelectionModel selectionModel = memberList.getSelectionModel();
 
 		//        System.out.println(selectionModel);
 		//        System.out.println(selectedValue);
 		//        if(selectComponent.getName() != null && selectComponent.getName().startsWith("enable")){
 		////            System.out.println(selectComponent.getName());
 		//            
 		//        }
 		if (evt.getButton() == java.awt.event.MouseEvent.BUTTON1
 				&& evt.getClickCount() == 2) {
 			int locationToIndex = memberList.locationToIndex(evt.getPoint());
 			memberList.setSelectedIndex(locationToIndex);
 			Object selectedValue = memberList.getSelectedValue();
 			MemberListItem item = (MemberListItem) selectedValue;
 			ControllerFacade.execute("videoViewsController", "showMemberVideo",
 					item);
 		} else if (evt.getButton() == java.awt.event.MouseEvent.BUTTON3) {
 			int locationToIndex = memberList.locationToIndex(evt.getPoint());
 			memberList.setSelectedIndex(locationToIndex);
 			MemberListItem item = (MemberListItem) memberList
 					.getSelectedValue();
 			if (item != null) {
 				LoginInfo info = DataUtil.getLoginInfo();
 				MemberDto myInfo = this.getMemberByName(info.getUsername());
 				if (myInfo.getRole().getPermissions().containsKey(
 						PermissionUtil.AUTHORITY_OPERATE)) {
 					String[] memberinfo = (String[])ControllerFacade.execute("phoneController", "getMemberInfoByUserName", item.getData().getName());
 					if(memberinfo == null || ! memberinfo[5].equals("1")){//不是我们的客户端不能更改权限
 						return;
 					}
 					if (item.getData().getRole().getNames().contains(
 							Constants.NARRATOR_STR)) {
 						this.grantNarratorMenuItem.setEnabled(false);
 						this.revokeNarratorMenuItem.setEnabled(true);
 					} else {
 						this.grantNarratorMenuItem.setEnabled(true);
 						this.revokeNarratorMenuItem.setEnabled(false);
 					}
 					if(!item.getData().getName().equals(info.getUsername())){
 						this.grantCompereMenuItem.setEnabled(true);
 					}else{
 						this.grantCompereMenuItem.setEnabled(false);
 					}
 					this.menu.show(this, evt.getX(), evt.getY());
 				}
 			}
 		}
 	}//GEN-LAST:event_memberListMouseClicked
 
 	//GEN-BEGIN:variables
 	// Variables declaration - do not modify
 	private javax.swing.JButton inviteButton;
 	private javax.swing.JScrollPane jScrollPane1;
 	private javax.swing.JButton kickButton;
 	private javax.swing.JList memberList;
 	private javax.swing.JPanel operatePanel;
 
 	// End of variables declaration//GEN-END:variables
 
 	public JList getMemberList() {
 		return memberList;
 	}
 	
 	public void setVisibleOfInviteButton(boolean f){
 		this.operatePanel.setVisible(f);
 		this.inviteButton.setVisible(f);
 	}
 	
 	public void setVisibleOfKickButton(boolean f){
 		this.operatePanel.setVisible(f);
 		this.kickButton.setVisible(f);
 	}
 	
 	public static void main(String[] args){
 		List<String> list = new ArrayList<String>(); 
 		list.add("gg");
 		String str = new String("gg");
 		list.remove(str);
 		System.out.println(list.contains(str));
 	}
 	
 	public List<MemberListItem> getAllMemberListItem() {
 		DefaultListModel model = (DefaultListModel) memberList.getModel();
 		List<MemberListItem> items = new ArrayList<MemberListItem>();
 		for (int i = 0; i < model.size(); i++) {
 			MemberListItem item = (MemberListItem) model.get(i);
 			items.add(item);
 		}
 		return items;
 	}
 
 }
