 package com.rom.client.gui;
 
 
 import com.rom.client.RoMClient;
 import com.rom.client.eventlisteners.*;
 import com.rom.client.eventobjects.*;
 import com.rom.client.history.RoMClientHistory;
 import com.rom.common.commands.GetGroupsClientCommand;
 import com.rom.common.dataObjects.User;
 import com.rom.common.logging.Logger;
 import java.awt.Color;
 import java.awt.event.KeyEvent;
 import java.awt.event.KeyListener;
 import java.awt.event.WindowAdapter;
 import java.awt.event.WindowEvent;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.logging.Level;
 import javax.swing.DefaultListModel;
 import javax.swing.ImageIcon;
 import javax.swing.SwingUtilities;
 import javax.swing.event.DocumentEvent;
 import javax.swing.event.DocumentListener;
 import javax.swing.text.*;
 /**
  *
  * @author Robert
  */
 public class MainFrame extends javax.swing.JFrame implements IReceivedMessageListener, IReceivedGroupsListener, IJoinGroupListener, KeyListener, IDeleteGroupListener{
 
 	private RoMClient client;
     private Document doc;
     private MutableAttributeSet black;
     private MutableAttributeSet red;
 	private MutableAttributeSet blue;
 	private MutableAttributeSet gray;
 	private MutableAttributeSet smilie;
     private AttributeSet attribute;
 	public AdminPanelFrame adminPanelFrame;
    
     /**
      * Creates new form MainFrame
 	 * @param client RoMClient which was generated in LoginForm
      */
     public MainFrame(RoMClient client) throws IOException, BadLocationException {
         initComponents();
         
 		this.client = client;
 		
 		adminPanelFrame = new AdminPanelFrame(this, client);
 		adminPanelFrame.setVisible(false);
 		
 		black = new SimpleAttributeSet();
         StyleConstants.setForeground(black, Color.BLACK);
         red = new SimpleAttributeSet();
         StyleConstants.setForeground(red, Color.RED);
 		blue = new SimpleAttributeSet();
         StyleConstants.setForeground(blue, Color.BLUE);
 		gray = new SimpleAttributeSet();
         StyleConstants.setForeground(gray, Color.GRAY);
 		
 		smilie = new SimpleAttributeSet();
 		StyleConstants.setIcon(smilie, new ImageIcon("build\\classes\\com\\rom\\client\\gui\\smilies\\wink.gif"));
 		
         txtHistory.setEditorKit(new StyledEditorKit());
         doc = txtHistory.getDocument();		
 
                 
                 //register the listeners
                 client.addReceivedMessageListener(this);
 				client.addJoinGroupListener(this);
 				client.addGetGroupListener(this);
 				client.addDeleteGroupListener(this);
 				
 				
                 //loads the history	
 				//loadHistory();
                 
 								
                //loads the groupList                
                 GetGroupsClientCommand cmd = new GetGroupsClientCommand(client.getClientID());
 				client.getObjectOutputStream().writeObject(cmd);
                 
                 
                 //inner class for the frame closing
                 this.addWindowListener(new WindowAdapter(){
                     @Override
                     public void windowClosing(WindowEvent e){
                         System.exit(0);
                     }
                 });
 				
 				//sets focus on the txtMessage field
 				txtMessage.requestFocus();
 				txtMessage.addKeyListener(this);
				
				int i = 0;
				while (i < lsGroups.getItemCount()-1){
					if (lsGroups.getItemAt(i).toString().equals("Allgemein")){
						lsGroups.setSelectedIndex(i);
					}
					i++;	
				}
				client.joinGroup(lsGroups.getSelectedItem().toString());
     }
 	
 	public void loadHistory() throws IOException, BadLocationException {
 		
 		//loads the history	
 			txtHistory.setText(RoMClientHistory.loadHistory(client.getUser()).toString());
 			
 		//inserts smilies into history		
 			StyledDocument styledDoc=(StyledDocument)txtHistory.getDocument();
 			int start = 0;
             int end = txtHistory.getText().length();
 			
 			//inserting the smile smilie
 			String text=styledDoc.getText(start, end-start);                   
 			int i=text.indexOf(":)");
                 while(i>=0) {
 					final SimpleAttributeSet attrs=new SimpleAttributeSet(
 					styledDoc.getCharacterElement(start+i).getAttributes());
 					if (StyleConstants.getIcon(attrs)==null) {
 						StyleConstants.setIcon(attrs, new ImageIcon("build\\classes\\com\\rom\\client\\gui\\smilies\\happy.gif"));
 						styledDoc.remove(start+i, 2);
 						styledDoc.insertString(start+i,":)", attrs);
 					}
 					i=text.indexOf(":)", i+2);
                 }
 			
 			//inserting the wink smilie
 			text=styledDoc.getText(start, end-start);                    
 			int j=text.indexOf(";)");
                 while(j>=0) {
 					final SimpleAttributeSet attrs=new SimpleAttributeSet(
 					styledDoc.getCharacterElement(start+j).getAttributes());
 					if (StyleConstants.getIcon(attrs)==null) {
 						StyleConstants.setIcon(attrs, new ImageIcon("build\\classes\\com\\rom\\client\\gui\\smilies\\wink.gif"));
 						styledDoc.remove(start+j, 2);
 						styledDoc.insertString(start+j,";)", attrs);
 					}
 					j=text.indexOf(";)", j+2);
                 }
 				
 			//inserting the laugh smilie
 			text=styledDoc.getText(start, end-start);                    
 			int k=text.indexOf(":D");
                 while(k>=0) {
 					final SimpleAttributeSet attrs=new SimpleAttributeSet(
 					styledDoc.getCharacterElement(start+k).getAttributes());
 					if (StyleConstants.getIcon(attrs)==null) {
 						StyleConstants.setIcon(attrs, new ImageIcon("build\\classes\\com\\rom\\client\\gui\\smilies\\laugh.gif"));
 						styledDoc.remove(start+k, 2);
 						styledDoc.insertString(start+k,":D", attrs);
 					}
 					k=text.indexOf(":D", k+2);
                 }
 				
 			//inserting the angry smilie
 			text=styledDoc.getText(start, end-start);                    
 			int l=text.indexOf("-.-");
                 while(l>=0) {
 					final SimpleAttributeSet attrs=new SimpleAttributeSet(
 					styledDoc.getCharacterElement(start+l).getAttributes());
 					if (StyleConstants.getIcon(attrs)==null) {
 						StyleConstants.setIcon(attrs, new ImageIcon("build\\classes\\com\\rom\\client\\gui\\smilies\\angry.gif"));
 						styledDoc.remove(start+l, 3);
 						styledDoc.insertString(start+l,"-.-", attrs);
 					}
 					l=text.indexOf("-.-", l+3);
                 }
 				
 		//scrolls down		
 		txtHistory.setCaretPosition(doc.getLength());
 	}
 
 	private void appendHistory(String historyText){
         try {
 			int start = doc.getLength();
             doc.insertString(doc.getLength(), historyText, attribute);
             int end = doc.getLength();
 			
 			StyledDocument styledDoc=(StyledDocument)txtHistory.getDocument();
 			
 			//inserting the smile smilie
 			String text=styledDoc.getText(start, end-start);                   
 			int i=text.indexOf(":)");
                 while(i>=0) {
 					final SimpleAttributeSet attrs=new SimpleAttributeSet(
 					styledDoc.getCharacterElement(start+i).getAttributes());
 					if (StyleConstants.getIcon(attrs)==null) {
 						StyleConstants.setIcon(attrs, new ImageIcon("build\\classes\\com\\rom\\client\\gui\\smilies\\happy.gif"));
 						styledDoc.remove(start+i, 2);
 						styledDoc.insertString(start+i,":)", attrs);
 					}
 					i=text.indexOf(":)", i+2);
                 }
 				
 			//inserting the smile wink
 			text=styledDoc.getText(start, end-start);                   
 			int j=text.indexOf(";)");
                 while(j>=0) {
 					final SimpleAttributeSet attrs=new SimpleAttributeSet(
 					styledDoc.getCharacterElement(start+j).getAttributes());
 					if (StyleConstants.getIcon(attrs)==null) {
 						StyleConstants.setIcon(attrs, new ImageIcon("build\\classes\\com\\rom\\client\\gui\\smilies\\wink.gif"));
 						styledDoc.remove(start+j, 2);
 						styledDoc.insertString(start+j,";)", attrs);
 					}
 					j=text.indexOf(";)", j+2);
                 }
 				
 			//inserting the laugh smilie
 			text=styledDoc.getText(start, end-start);                    
 			int k=text.indexOf(":D");
                 while(k>=0) {
 					final SimpleAttributeSet attrs=new SimpleAttributeSet(
 					styledDoc.getCharacterElement(start+k).getAttributes());
 					if (StyleConstants.getIcon(attrs)==null) {
 						StyleConstants.setIcon(attrs, new ImageIcon("build\\classes\\com\\rom\\client\\gui\\smilies\\laugh.gif"));
 						styledDoc.remove(start+k, 2);
 						styledDoc.insertString(start+k,":D", attrs);
 					}
 					k=text.indexOf(":D", k+2);
                 }
 				
 			//inserting the angry smilie
 			text=styledDoc.getText(start, end-start);                    
 			int l=text.indexOf("-.-");
                 while(l>=0) {
 					final SimpleAttributeSet attrs=new SimpleAttributeSet(
 					styledDoc.getCharacterElement(start+l).getAttributes());
 					if (StyleConstants.getIcon(attrs)==null) {
 						StyleConstants.setIcon(attrs, new ImageIcon("build\\classes\\com\\rom\\client\\gui\\smilies\\angry.gif"));
 						styledDoc.remove(start+l, 2);
 						styledDoc.insertString(start+l,"-.-", attrs);
 					}
 					l=text.indexOf("-.-", l+2);
                 }
 			
         } catch (BadLocationException ex) {
             ex.printStackTrace();
         }
 		
 		//scrolls down
 		txtHistory.setCaretPosition(doc.getLength());
     }
 	
 	
     /**
      * This method is called from within the constructor to initialize the form.
      * WARNING: Do NOT modify this code. The content of this method is always
      * regenerated by the Form Editor.
      */
     @SuppressWarnings("unchecked")
     // <editor-fold defaultstate="collapsed" desc="Generated Code">//GEN-BEGIN:initComponents
     private void initComponents() {
 
         txtMessage = new javax.swing.JTextField();
         btnSend = new javax.swing.JButton();
         btnClose = new javax.swing.JButton();
         pnlUsers = new javax.swing.JScrollPane();
         lsUsers = new javax.swing.JList();
         lblUsers = new javax.swing.JLabel();
         lblGroups = new javax.swing.JLabel();
         btnLoginPic = new javax.swing.JButton();
         jLabel1 = new javax.swing.JLabel();
         jLabel2 = new javax.swing.JLabel();
         jScrollPane1 = new javax.swing.JScrollPane();
         txtHistory = new javax.swing.JEditorPane();
         lsGroups = new javax.swing.JComboBox();
         mnuMain = new javax.swing.JMenuBar();
         mnuCommon = new javax.swing.JMenu();
         mnuCommonDeleteHistory = new javax.swing.JMenuItem();
         mnuCommonClose = new javax.swing.JMenuItem();
         mnuAdmin = new javax.swing.JMenu();
         mnuAdminPanel = new javax.swing.JMenuItem();
 
         setDefaultCloseOperation(javax.swing.WindowConstants.EXIT_ON_CLOSE);
         setMinimumSize(new java.awt.Dimension(713, 354));
         setResizable(false);
         addKeyListener(new java.awt.event.KeyAdapter() {
             public void keyPressed(java.awt.event.KeyEvent evt) {
                 formKeyPressed(evt);
             }
             public void keyReleased(java.awt.event.KeyEvent evt) {
                 formKeyReleased(evt);
             }
             public void keyTyped(java.awt.event.KeyEvent evt) {
                 formKeyTyped(evt);
             }
         });
 
         btnSend.setText("Senden");
         btnSend.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnSendActionPerformed(evt);
             }
         });
 
         btnClose.setText("Schließen");
         btnClose.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 btnCloseActionPerformed(evt);
             }
         });
 
         pnlUsers.setViewportView(lsUsers);
 
         lblUsers.setText("Users online");
 
         lblGroups.setText("Gruppe");
 
         btnLoginPic.setIcon(new javax.swing.ImageIcon(getClass().getResource("/com/rom/client/gui/romLogo.jpg"))); // NOI18N
         btnLoginPic.setBorder(javax.swing.BorderFactory.createEtchedBorder());
 
         jLabel1.setFont(new java.awt.Font("Verdana", 1, 30)); // NOI18N
         jLabel1.setForeground(new java.awt.Color(51, 153, 255));
         jLabel1.setText("RoM");
         jLabel1.setToolTipText("");
 
         jLabel2.setFont(new java.awt.Font("Tahoma", 0, 8)); // NOI18N
         jLabel2.setText("Rosenheim Messenger");
 
         jScrollPane1.setPreferredSize(new java.awt.Dimension(250, 80));
 
         txtHistory.setEditable(false);
         txtHistory.setMinimumSize(new java.awt.Dimension(250, 80));
         jScrollPane1.setViewportView(txtHistory);
 
         lsGroups.addItemListener(new java.awt.event.ItemListener() {
             public void itemStateChanged(java.awt.event.ItemEvent evt) {
                 lsGroupsItemStateChanged(evt);
             }
         });
 
         mnuCommon.setText("Datei");
 
         mnuCommonDeleteHistory.setText("History löschen");
         mnuCommonDeleteHistory.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mnuCommonDeleteHistoryActionPerformed(evt);
             }
         });
         mnuCommon.add(mnuCommonDeleteHistory);
 
         mnuCommonClose.setText("Schließen");
         mnuCommonClose.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mnuCommonCloseActionPerformed(evt);
             }
         });
         mnuCommon.add(mnuCommonClose);
 
         mnuMain.add(mnuCommon);
 
         mnuAdmin.setText("Admin");
 
         mnuAdminPanel.setText("Adminpanel");
         mnuAdminPanel.addActionListener(new java.awt.event.ActionListener() {
             public void actionPerformed(java.awt.event.ActionEvent evt) {
                 mnuAdminPanelActionPerformed(evt);
             }
         });
         mnuAdmin.add(mnuAdminPanel);
 
         mnuMain.add(mnuAdmin);
 
         setJMenuBar(mnuMain);
 
         javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
         getContentPane().setLayout(layout);
         layout.setHorizontalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createSequentialGroup()
                         .addComponent(txtMessage)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                         .addComponent(btnSend, javax.swing.GroupLayout.PREFERRED_SIZE, 82, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 462, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.TRAILING)
                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                         .addComponent(lblUsers)
                         .addGroup(layout.createSequentialGroup()
                             .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                                 .addComponent(jLabel1)
                                 .addComponent(jLabel2)
                                 .addComponent(lblGroups))
                             .addGap(18, 18, 18)
                             .addComponent(btnLoginPic, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
                         .addComponent(lsGroups, 0, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(pnlUsers, javax.swing.GroupLayout.PREFERRED_SIZE, 0, Short.MAX_VALUE))
                     .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 88, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addContainerGap())
         );
         layout.setVerticalGroup(
             layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
             .addGroup(layout.createSequentialGroup()
                 .addContainerGap()
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING, false)
                     .addGroup(layout.createSequentialGroup()
                         .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                             .addGroup(layout.createSequentialGroup()
                                 .addComponent(jLabel1)
                                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                                 .addComponent(jLabel2))
                             .addComponent(btnLoginPic, javax.swing.GroupLayout.PREFERRED_SIZE, 71, javax.swing.GroupLayout.PREFERRED_SIZE))
                         .addGap(9, 9, 9)
                         .addComponent(lblGroups, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                         .addComponent(lsGroups, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addGap(18, 18, 18)
                         .addComponent(lblUsers, javax.swing.GroupLayout.PREFERRED_SIZE, 14, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)
                         .addComponent(pnlUsers, javax.swing.GroupLayout.PREFERRED_SIZE, 111, javax.swing.GroupLayout.PREFERRED_SIZE))
                     .addComponent(jScrollPane1, javax.swing.GroupLayout.PREFERRED_SIZE, 269, javax.swing.GroupLayout.PREFERRED_SIZE))
                 .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.UNRELATED)
                 .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                     .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                         .addComponent(txtMessage, javax.swing.GroupLayout.PREFERRED_SIZE, 26, javax.swing.GroupLayout.PREFERRED_SIZE)
                         .addComponent(btnSend))
                     .addGroup(layout.createSequentialGroup()
                         .addGap(2, 2, 2)
                         .addComponent(btnClose, javax.swing.GroupLayout.PREFERRED_SIZE, 24, javax.swing.GroupLayout.PREFERRED_SIZE)))
                 .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
         );
 
         pack();
     }// </editor-fold>//GEN-END:initComponents
 
     private void mnuCommonCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuCommonCloseActionPerformed
         btnCloseActionPerformed(evt);
     }//GEN-LAST:event_mnuCommonCloseActionPerformed
 
     private void btnCloseActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnCloseActionPerformed
         
         // disconnect from server
         try {
             client.disconnect();
         } catch (IOException ex) {
             Logger.logException("Exception thrown:", ex);
         }
         
         // destroys the frame
         this.dispose();
         
         // completly closes the programm
         System.exit(0);
         
     }//GEN-LAST:event_btnCloseActionPerformed
 
     private void btnSendActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_btnSendActionPerformed
 		try {
 			sendMessage();
 		} catch (BadLocationException ex) {
 			Logger.logException("Fehler beim senden der Nachricht", ex);
 		}
     }//GEN-LAST:event_btnSendActionPerformed
 
 	
 	public void sendMessage() throws BadLocationException {
 
 				try {
 					client.sendMessage(txtMessage.getText());
 					txtMessage.setText("");
 				} catch (IOException ex) {
 						Logger.logException("Exception thrown: ", ex);
 				}
 	}
 	
 	@Override
 	public void keyPressed(java.awt.event.KeyEvent evt) {
 		if (evt.getKeyCode()==KeyEvent.VK_ENTER)
 			try {
 			sendMessage();
 		} catch (BadLocationException ex) {
 			Logger.logException("Fehler beim senden der Nachricht", ex);
 		}
 	}
 	@Override
 	public void keyReleased(java.awt.event.KeyEvent evt) {
 		
 	}
 	@Override
 	public void keyTyped(java.awt.event.KeyEvent evt) {
 		
 	}
 	
 	private void formKeyPressed(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyPressed
 		if (evt.getKeyCode()==KeyEvent.VK_ENTER)
 			btnSend.doClick();
 	}//GEN-LAST:event_formKeyPressed
 
 	private void formKeyReleased(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyReleased
 		// TODO add your handling code here:
 	}//GEN-LAST:event_formKeyReleased
 
 	private void formKeyTyped(java.awt.event.KeyEvent evt) {//GEN-FIRST:event_formKeyTyped
 		// TODO add your handling code here:
 	}//GEN-LAST:event_formKeyTyped
 
 	private void mnuAdminPanelActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuAdminPanelActionPerformed
 		adminPanelFrame.setVisible(true);
 	}//GEN-LAST:event_mnuAdminPanelActionPerformed
 
 	private void mnuCommonDeleteHistoryActionPerformed(java.awt.event.ActionEvent evt) {//GEN-FIRST:event_mnuCommonDeleteHistoryActionPerformed
 		
 		txtHistory.setText("");
 		try {
 			RoMClientHistory.deleteHistory(this.client.getUser().toString());
 		} catch (IOException ex) {
 			java.util.logging.Logger.getLogger(MainFrame.class.getName()).log(Level.SEVERE, null, ex);
 		}
 	}//GEN-LAST:event_mnuCommonDeleteHistoryActionPerformed
 
 	private void lsGroupsItemStateChanged(java.awt.event.ItemEvent evt) {//GEN-FIRST:event_lsGroupsItemStateChanged
 		try {
 			client.joinGroup(lsGroups.getSelectedItem().toString());
 			Logger.logMessage("Join Group Command gesendet");
 			txtMessage.requestFocus();
 		} catch (IOException ex) {
 			Logger.logException("Exception thrown: ", ex);
 		}
 	}//GEN-LAST:event_lsGroupsItemStateChanged
 
 
     // Variables declaration - do not modify//GEN-BEGIN:variables
     private javax.swing.JButton btnClose;
     private javax.swing.JButton btnLoginPic;
     private javax.swing.JButton btnSend;
     private javax.swing.JLabel jLabel1;
     private javax.swing.JLabel jLabel2;
     private javax.swing.JScrollPane jScrollPane1;
     private javax.swing.JLabel lblGroups;
     private javax.swing.JLabel lblUsers;
     private javax.swing.JComboBox lsGroups;
     private javax.swing.JList lsUsers;
     private javax.swing.JMenu mnuAdmin;
     private javax.swing.JMenuItem mnuAdminPanel;
     private javax.swing.JMenu mnuCommon;
     private javax.swing.JMenuItem mnuCommonClose;
     private javax.swing.JMenuItem mnuCommonDeleteHistory;
     private javax.swing.JMenuBar mnuMain;
     private javax.swing.JScrollPane pnlUsers;
     private javax.swing.JEditorPane txtHistory;
     private javax.swing.JTextField txtMessage;
     // End of variables declaration//GEN-END:variables
 
     
     @Override
     public void receivedMessage(ReceivedMessageEvent event) {
         
         Date currDate = new Date();
         DateFormat dateFormat = new SimpleDateFormat("dd.MM.yyyy hh:mm:ss");
                 
         appendHistory("[" + dateFormat.format(currDate) + "] ");
 		attribute = blue;
 		appendHistory(event.getFromUser());
 		attribute = black;
 		appendHistory(": " + event.getMessage());
         appendHistory(System.getProperty("line.separator"));
         
     }
 	
 	@Override
 	public void receivedGroups(ReceivedGroupsEvent event) {
 		
 		lsGroups.removeAllItems();
 		for (String group : event.getGroups()) {
 			lsGroups.addItem(group);
 		}
 	}
 	
 	@Override
     public void joinGroup(JoinGroupEvent event) {
 		DefaultListModel model = new DefaultListModel();
 		for (User user : event.getUsers()) {
 			String username = user.getUsername();
 			
 			if (user.getIsAdmin()) {
 				username = "@" + username;
 			}
 			
 			model.addElement(username);
 		}
 		lsUsers.setModel(model);
 		attribute = red;
 		appendHistory("Du befindest dich nun in der Gruppe " + lsGroups.getSelectedItem().toString());
 		attribute = black;
     }
 	
 	@Override
 	public void deleteGroup(DeleteGroupEvent event) {
 		
 		lsGroups.removeAllItems();
 		for (String group : event.getGroups()) {
 			lsGroups.addItem(group);
 		}
 	}
 	
 	
 
 }
