 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 /*
  * MemberListItem.java
  *
  * Created on 2011-12-13, 14:17:41
  */
 package com.lorent.lvmc.ui;
 
 import java.awt.Component;
 import java.awt.Point;
 import java.awt.Rectangle;
 
 import javax.swing.JFrame;
 import javax.swing.JLabel;
 import javax.swing.SwingUtilities;
 
 import org.apache.log4j.Logger;
 
 import com.lorent.common.dto.LCMRoleDto;
 import com.lorent.lvmc.controller.ControllerFacade;
 import com.lorent.lvmc.dto.MemberDto;
 import com.lorent.lvmc.util.Constants;
 import com.lorent.lvmc.util.LvmcUtil;
 
 /**
  *
  * @author jack
  */
 public class MemberListItem extends javax.swing.JPanel {
 
 	private static Logger log = Logger.getLogger(MemberListItem.class);
 	private MemberDto data;
 	private int shareDesktopState = Constants.MEMBER_STATUS_SHAREDESKTOP_FREE;
 
 	public int getShareDesktopState() {
 		return shareDesktopState;
 	}
 
 	public MemberDto getData() {
 		return data;
 	}
 
 	/** Creates new form MemberListItem */
 	public MemberListItem() {
 		initComponents();
 		shareDesktopStatusButton.setVisible(false);
 	}
 
 	public void setData(MemberDto data, boolean showPermission) {
 		this.data = data;
 		if (data.isOnline()) {
 			memberImg.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 					"/com/lorent/lvmc/resource/images/state_ready.png")));
 		} else {
 			memberImg.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 					"/com/lorent/lvmc/resource/images/state_offline.png")));
 		}
 		if (LvmcUtil.isUCSAPP()) {
 			memberName.setText(data.getName());
 		} else {
 			memberName.setText(data.getNickname());
 		}
 		//		memberRole.setText("");
 		StringBuilder roleText = new StringBuilder();
 		roleText.append("(");
 		LCMRoleDto role = data.getRole();
 		if (role != null) {
 			if (role.getNames().size() == 0) {
 				roleText.append(Constants.PARTICIPANT_STR);
 			} else {
 				for (String roleName : role.getNames()) {
 					roleText.append(roleName).append(",");
 				}
 				roleText.deleteCharAt(roleText.length() - 1);
 			}
 			roleText.append(")");
 			memberRole.setText(roleText.toString());
 		}
 		showPermission = true;//TODO暂时所有人都可以看到
 		if (showPermission) {
 			enableVideo.setVisible(true);
 			enableSound.setVisible(true);
 		} else {
 			enableVideo.setVisible(false);
 			enableSound.setVisible(false);
 		}
 		updateVideoSoundStatus(data);
 	}
 	
 	public void updateVideoSoundStatus(MemberDto data){
 		this.data.setEnableAudio(data.isEnableAudio());
 		this.data.setEnableVideo(data.isEnableVideo());
 		if(data.isEnableAudio()){
 			enableSound.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 			"/com/lorent/lvmc/resource/images/sound-on.png")));
 		}else{
 			enableSound.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 			"/com/lorent/lvmc/resource/images/sound-off.png")));
 		}
 		if(data.isEnableVideo()){
 			enableVideo.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 			"/com/lorent/lvmc/resource/images/camera-on.png")));
 		}else{
 			enableVideo.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 			"/com/lorent/lvmc/resource/images/camera-off.png")));
 		}
 	}
 
 	public void setShareDesktopState(int status) throws Exception {
 		shareDesktopState = status;
 		switch (status) {
 		case Constants.MEMBER_STATUS_SHAREDESKTOP_FREE:
 			shareDesktopStatusButton.setVisible(false);
 			shareDesktopStatusButton
 					.setIcon(new javax.swing.ImageIcon(
 							getClass()
 									.getResource(
 											"/com/lorent/lvmc/resource/images/video-display-3.png")));
 			break;
 
 		case Constants.MEMBER_STATUS_SHAREDESKTOP_VIEW:
 			shareDesktopStatusButton.setVisible(true);
 			shareDesktopStatusButton
 					.setIcon(new javax.swing.ImageIcon(
 							getClass()
 									.getResource(
 											"/com/lorent/lvmc/resource/images/video-display-3.png")));
 			break;
 		case Constants.MEMBER_STATUS_SHAREDESKTOP_CONTROL:
 			shareDesktopStatusButton.setVisible(true);
 			shareDesktopStatusButton
 					.setIcon(new javax.swing.ImageIcon(
 							getClass()
 									.getResource(
 											"/com/lorent/lvmc/resource/images/video-display-31.png")));
 			break;
 		default:
 			break;
 		}
 	}
 
 	//GEN-BEGIN:initComponents
 	// <editor-fold defaultstate="collapsed" desc="Generated Code">
 	private void initComponents() {
 
 		baseInfoPanel = new javax.swing.JPanel();
 		memberImg = new javax.swing.JLabel();
 		memberName = new javax.swing.JLabel();
 		memberRole = new javax.swing.JLabel();
 		permissionPanel = new javax.swing.JPanel();
 		enableVideo = new javax.swing.JLabel();
 		enableSound = new javax.swing.JLabel();
 		shareDesktopStatusButton = new javax.swing.JButton();
 
 		setOpaque(false);
 		setLayout(new javax.swing.BoxLayout(this,
 				javax.swing.BoxLayout.LINE_AXIS));
 
 		baseInfoPanel.setOpaque(false);
 		baseInfoPanel.setLayout(new java.awt.FlowLayout(
 				java.awt.FlowLayout.LEFT));
 
 		memberImg.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/com/lorent/lvmc/resource/images/state_ready.png"))); // NOI18N
 		baseInfoPanel.add(memberImg);
 
 		memberName.setText("name");
 		baseInfoPanel.add(memberName);
 
 		memberRole.setText("(role,role)");
 		baseInfoPanel.add(memberRole);
 
 		add(baseInfoPanel);
 
 		permissionPanel.setOpaque(false);
 		permissionPanel.setLayout(new java.awt.FlowLayout(
 				java.awt.FlowLayout.RIGHT));
 
 		enableVideo.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/com/lorent/lvmc/resource/images/camera-on.png"))); // NOI18N
 		enableVideo.setName("enableVideo");
 		permissionPanel.add(enableVideo);
 
 		enableSound.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 				"/com/lorent/lvmc/resource/images/sound-on.png"))); // NOI18N
 		enableSound.setName("enableSound");
 		permissionPanel.add(enableSound);
 
 		shareDesktopStatusButton
 				.setIcon(new javax.swing.ImageIcon(getClass().getResource(
 						"/com/lorent/lvmc/resource/images/video-display-3.png"))); // NOI18N
 		shareDesktopStatusButton.setBorder(javax.swing.BorderFactory
 				.createEmptyBorder(1, 1, 1, 1));
 		shareDesktopStatusButton.setBorderPainted(false);
 		shareDesktopStatusButton
 				.setHorizontalTextPosition(javax.swing.SwingConstants.CENTER);
 		shareDesktopStatusButton
 				.setVerticalTextPosition(javax.swing.SwingConstants.BOTTOM);
 		permissionPanel.add(shareDesktopStatusButton);
 
 		add(permissionPanel);
 	}// </editor-fold>
 	//GEN-END:initComponents
 
 	public void handleClick(Point p, Rectangle cellBounds) {
 		if (isClick(enableSound, p, cellBounds)) {
 			ControllerFacade.execute("mainController", "enableUserAudio", !data.isEnableAudio(), data.getName());
 		} else if (isClick(enableVideo, p, cellBounds)) {
			ControllerFacade.execute("mainController", "enableUserSound", !data.isEnableVideo(), data.getName());
 		}
 	}
 
 	//判断在permissionPanel里面的Label是否被选中
 	private boolean isClick(JLabel label, Point p, Rectangle cellBounds) {
 		Rectangle rect = new Rectangle(permissionPanel.getX() + label.getX(),
 				cellBounds.y + label.getY(), label.getWidth(), label
 						.getHeight());
 		if (rect.contains(p)) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	//GEN-BEGIN:variables
 	// Variables declaration - do not modify
 	private javax.swing.JPanel baseInfoPanel;
 	private javax.swing.JLabel enableSound;
 	private javax.swing.JLabel enableVideo;
 	private javax.swing.JLabel memberImg;
 	private javax.swing.JLabel memberName;
 	private javax.swing.JLabel memberRole;
 	private javax.swing.JPanel permissionPanel;
 	private javax.swing.JButton shareDesktopStatusButton;
 
 	// End of variables declaration//GEN-END:variables
 
 	public javax.swing.JLabel getMemberName() {
 		return memberName;
 	}
 
 	public void setMemberName(javax.swing.JLabel memberName) {
 		this.memberName = memberName;
 	}
 
 	public javax.swing.JLabel getMemberRole() {
 		return memberRole;
 	}
 
 	public void setMemberRole(javax.swing.JLabel memberRole) {
 		this.memberRole = memberRole;
 	}
 
 	public void setTextForeground(java.awt.Color color) {
 		memberName.setForeground(color);
 		memberRole.setForeground(color);
 	}
 
 	public static void main(String[] args) {
 		SwingUtilities.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				JFrame j = new JFrame();
 				j.setSize(300, 400);
 				j.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
 				j.add(new MemberListItem());
 				j.setVisible(true);
 			}
 		});
 	}
 }
