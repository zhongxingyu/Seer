 /*-
  * Copyright (c) 2008, Derek Konigsberg
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer. 
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in the
  *    documentation and/or other materials provided with the distribution. 
  * 3. Neither the name of the project nor the names of its
  *    contributors may be used to endorse or promote products derived
  *    from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS
  * "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT
  * LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS
  * FOR A PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE
  * COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT,
  * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING,
  * BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
  * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT,
  * STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE)
  * ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED
  * OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package org.logicprobe.LogicMail.ui;
 
 import java.util.Hashtable;
 import java.util.Vector;
 import java.util.Enumeration;
 
 import net.rim.device.api.system.Bitmap;
 import net.rim.device.api.ui.Font;
 import net.rim.device.api.ui.Graphics;
 import net.rim.device.api.ui.Keypad;
 import net.rim.device.api.ui.MenuItem;
 import net.rim.device.api.ui.UiApplication;
 import net.rim.device.api.ui.component.Menu;
 import net.rim.device.api.ui.component.TreeField;
 import net.rim.device.api.ui.component.TreeFieldCallback;
 import net.rim.device.api.ui.Field;
 
 import org.logicprobe.LogicMail.LogicMailResource;
 import org.logicprobe.LogicMail.conf.AccountConfig;
 import org.logicprobe.LogicMail.model.AccountNode;
 import org.logicprobe.LogicMail.model.AccountNodeEvent;
 import org.logicprobe.LogicMail.model.AccountNodeListener;
 import org.logicprobe.LogicMail.model.MailManagerEvent;
 import org.logicprobe.LogicMail.model.MailboxNode;
 import org.logicprobe.LogicMail.model.MailboxNodeEvent;
 import org.logicprobe.LogicMail.model.MailboxNodeListener;
 import org.logicprobe.LogicMail.model.Node;
 import org.logicprobe.LogicMail.model.MailRootNode;
 import org.logicprobe.LogicMail.model.MailManager;
 import org.logicprobe.LogicMail.model.MailManagerListener;
 import org.logicprobe.LogicMail.util.EventObjectRunnable;
 
 /**
  * Main screen for the application, providing a unified
  * view of accounts and folders.
  */
 public class MailHomeScreen extends BaseScreen {
 	private TreeField treeField;
 	private MailManager mailManager;
 	private MailRootNode mailRootNode;
 	private Hashtable nodeIdMap;
 	private MailManagerListener mailManagerListener;
 	private boolean firstVisible = true;
 	
 	/** Tree node data class */
 	private static class TreeNode {
 		public Node node;
 		
 		public TreeNode(Node node) {
 			this.node = node;
 		}
 	}
 	
 	public MailHomeScreen() {
 		super(resources.getString(LogicMailResource.MAILHOME_TITLE));
 	
 		initFields();
 		nodeIdMap = new Hashtable();
 		mailManager = MailManager.getInstance();
 		mailRootNode = mailManager.getMailRootNode();
 		
 		mailManagerListener = new MailManagerListener() {
 			public void mailConfigurationChanged(MailManagerEvent e) {
 				mailManager_MailConfigurationChanged(e);
 			}
 		};
 	}
 
 	private void initFields() {
 		treeField = new TreeField(new TreeFieldCallback() {
 			public void drawTreeItem(TreeField treeField, Graphics graphics,
 					int node, int y, int width, int indent) {
 				treeField_DrawTreeItem(treeField, graphics, node, y, width, indent);
 			}
 		}, Field.FOCUSABLE);
 		treeField.setEmptyString(resources.getString(LogicMailResource.MAILHOME_NOACCOUNTS), 0);
 		treeField.setDefaultExpanded(true);
 		treeField.setIndentWidth(20);
 		
 		add(treeField);
 	}
 	
 	private AccountNodeListener accountNodeListener = new AccountNodeListener() {
 		public void accountStatusChanged(AccountNodeEvent e) {
 			UiApplication.getUiApplication().invokeLater(new EventObjectRunnable(e) {
 				public void run() {
 					accountNodeListener_AccountStatusChanged((AccountNodeEvent)getEvent());
 				}
 			});
 		}
 	};
 	
 	private void accountNodeListener_AccountStatusChanged(AccountNodeEvent e) {
 		UiApplication.getUiApplication().invokeLater(new EventObjectRunnable(e) {
 			public void run() {
 				AccountNodeEvent e = (AccountNodeEvent)getEvent();
 				if(nodeIdMap.containsKey(e.getSource())) {
 					if(e.getType() == AccountNodeEvent.TYPE_CONNECTION) {
 						treeField.invalidateNode(((Integer)nodeIdMap.get(e.getSource())).intValue());
 					}
 					else if(e.getType() == AccountNodeEvent.TYPE_MAILBOX_TREE) {
 						refreshAccountFolders((AccountNode)e.getSource());
 					}
 				}
 			}
 		});
 	}
 	
     private MenuItem selectFolderItem = new MenuItem(resources.getString(LogicMailResource.MENUITEM_SELECT), 100, 10) {
         public void run() {
         	openSelectedItem();
         }
     };
     private MenuItem refreshStatusItem = new MenuItem(resources.getString(LogicMailResource.MENUITEM_REFRESH_STATUS), 110, 10) {
         public void run() {
         	// Assume this will only be called when a valid AccountNode is selected.
         	// Later we need to modify it to deal with being called from MailboxNodes.
       		AccountNode accountNode = (AccountNode)((TreeNode)treeField.getCookie(treeField.getCurrentNode())).node;
       		accountNode.refreshMailboxStatus();
         }
     };
     private MenuItem refreshItem = new MenuItem(resources.getString(LogicMailResource.MENUITEM_REFRESH_FOLDERS), 111, 10) {
         public void run() {
         	// Assume this will only be called when a valid AccountNode is selected.
         	// Later we need to modify it to deal with being called from MailboxNodes.
       		AccountNode accountNode = (AccountNode)((TreeNode)treeField.getCookie(treeField.getCurrentNode())).node;
       		accountNode.refreshMailboxes();
         }
     };
     private MenuItem compositionItem = new MenuItem(resources.getString(LogicMailResource.MENUITEM_COMPOSE_EMAIL), 120, 10) {
         public void run() {
         	int id = treeField.getCurrentNode();
         	if(id != -1) {
         		Node node = ((TreeNode)treeField.getCookie(id)).node;
         		AccountNode accountNode;
         		if(node instanceof MailboxNode) {
         			accountNode = ((MailboxNode)node).getParentAccount();
         		}
         		else if(node instanceof AccountNode) {
         			accountNode = (AccountNode)node; 
         		}
         		else {
         			accountNode = null;
         		}
         		if(accountNode != null) {
                     UiApplication.getUiApplication().pushScreen(new CompositionScreen(accountNode));
         		}
         	}
         }
     };
     private MenuItem disconnectItem = new MenuItem(resources.getString(LogicMailResource.MENUITEM_DISCONNECT), 200000, 9) {
         public void run() {
             disconnectSelectedAccount();
         }
     };
 
     private MailboxNodeListener mailboxNodeListener = new MailboxNodeListener() {
 		public void mailboxStatusChanged(MailboxNodeEvent e) {
 			if(nodeIdMap.containsKey(e.getSource())) {
 				treeField.invalidateNode(((Integer)nodeIdMap.get(e.getSource())).intValue());
 			}
 		}
 	};
 	
 	protected void onDisplay() {
 		populateTreeField();
 		mailManager.addMailManagerListener(mailManagerListener);
 		super.onDisplay();
 	}
 	
 	protected void onUndisplay() {
 		clearTreeField();
 		mailManager.removeMailManagerListener(mailManagerListener);
 		super.onUndisplay();
 	}
 	
 	public boolean onClose() {
 		tryShutdownApplication();
 		return false;
 	}
 
 	protected void onVisibilityChange(boolean visible) {
 		super.onVisibilityChange(visible);
 		if(visible && firstVisible) {
 			firstVisible = false;
 			// Check to see if there are no configured accounts
 			if(mailRootNode.getAccounts().length <= 1) {
 				UiApplication.getUiApplication().invokeLater(new Runnable() {
 					public void run() {
 						// Start the new account configuration wizard
 						AccountConfigWizard wizard = new AccountConfigWizard();
 						if(wizard.start()) {
 							AccountConfig newAccountConfig = wizard.getAccountConfig();
 							
 							// Find the newly created account, and trigger a folder refresh (if applicable)
 							AccountNode[] accounts = mailRootNode.getAccounts();
 							for(int i=0; i<accounts.length; i++) {
 								if(accounts[i].getAccountConfig() == newAccountConfig) {
 									if(accounts[i].hasFolders()) {
 										accounts[i].refreshMailboxes();
 									}
 									break;
 								}
 							}
 						}
 					}
 				});
 			}
 		}
 	}
 	
     protected void makeMenu(Menu menu, int instance) {
     	int id = treeField.getCurrentNode();
     	if(id != -1) {
     		Node node = ((TreeNode)treeField.getCookie(id)).node;
     		if(node instanceof MailboxNode) {
     			MailboxNode mailboxNode = (MailboxNode)node;
     			menu.add(selectFolderItem);
     			if(mailboxNode.getParentAccount().hasMailSender()) {
     				menu.add(compositionItem);
     			}
     		}
     		else if(node instanceof AccountNode) {
     			AccountNode accountNode = (AccountNode)node; 
     			if(accountNode.getRootMailbox() != null) {
     				menu.add(refreshStatusItem);
     			}
     			if(accountNode.hasFolders()) {
     				menu.add(refreshItem);
     			}
     			if(accountNode.hasMailSender()) {
     				menu.add(compositionItem);
     			}
     			if(accountNode.getStatus() == AccountNode.STATUS_ONLINE) {
     				menu.add(disconnectItem);
     			}
     		}
     	}
         super.makeMenu(menu, instance);
     }
 
     protected boolean onClick() {
     	return openSelectedItem();
     }
     
     public boolean keyChar(char key, int status, int time) {
 		boolean retval = false;
 		switch (key) {
 		case Keypad.KEY_ENTER:
 			retval = openSelectedItem();
 			break;
 		}
 		return retval;
 	}
     
 	/**
 	 * Handles the configuration change event from the mail manager.
 	 * 
 	 * @param e Event data.
 	 */
 	private void mailManager_MailConfigurationChanged(MailManagerEvent e) {
 		// This really should take a more efficient approach,
 		// but since this screen usually isn't visible when
 		// this event is fired, it shouldn't be a problem.
 		clearTreeField();
 		populateTreeField();
 	}
 	
 	/**
 	 * Reads the mail data model tree, and creates the initial structure
 	 * of the tree field.
 	 */
 	private synchronized void populateTreeField() {
 		int firstNode = -1;
 		AccountNode[] accounts = mailRootNode.getAccounts();
 		for(int i=accounts.length - 1; i >= 0; --i) {
 			int id = treeField.addChildNode(0, new TreeNode(accounts[i]));
 			if(i == 0) { firstNode = id; }
 			nodeIdMap.put(accounts[i], new Integer(id));
 			
 			MailboxNode rootMailbox = accounts[i].getRootMailbox();
 			if(rootMailbox != null) {
 				MailboxNode[] mailboxNodes = rootMailbox.getMailboxes();
 				for(int j=mailboxNodes.length - 1; j>=0; --j) {
 					populateTreeFieldMailbox(id, mailboxNodes[j]);
 				}
 			}
 			accounts[i].addAccountNodeListener(accountNodeListener);
 		}
 		if(firstNode != -1) {
 			treeField.setCurrentNode(firstNode);
 		}
 	}
 	
 	private void populateTreeFieldMailbox(int parent, MailboxNode mailboxNode) {
 		int id = treeField.addChildNode(parent, new TreeNode(mailboxNode));
 		nodeIdMap.put(mailboxNode, new Integer(id));
 		
 		MailboxNode[] mailboxes = mailboxNode.getMailboxes();
 		for(int i=mailboxes.length - 1; i >= 0; --i) {
 			populateTreeFieldMailbox(id, mailboxes[i]);
 		}
 		mailboxNode.addMailboxNodeListener(mailboxNodeListener);
 	}
 	
 	/**
 	 * Clears the structure of the tree field.
 	 */
 	private synchronized void clearTreeField() {
 		for (Enumeration e = nodeIdMap.keys(); e.hasMoreElements() ;) {
 			Node node = (Node)e.nextElement();
 			if(node instanceof AccountNode) {
 				((AccountNode)node).removeAccountNodeListener(accountNodeListener);
 			}
 			else if(node instanceof MailboxNode) {
 				((MailboxNode)node).removeMailboxNodeListener(mailboxNodeListener);
 			}
 	    }
 		treeField.deleteAll();
 		nodeIdMap.clear();
 	}
 
 	/**
 	 * Refreshes the mailbox nodes for a specific account.
 	 * 
 	 * @param account The account node.
 	 */
 	private synchronized void refreshAccountFolders(AccountNode account) {
 		if(nodeIdMap.containsKey(account)) {
 			int accountId = ((Integer)nodeIdMap.get(account)).intValue();
 			
 			// Unsubscribe from all existing mailbox nodes, remove them
 			// from the node-id map, and remove them from the tree.
 			Vector mailboxes = new Vector();
 			getMailboxNodes(mailboxes, accountId);
 			int size = mailboxes.size();
 			for(int i=0; i<size; i++) {
 				MailboxNode mailboxNode = (MailboxNode)mailboxes.elementAt(i);
 				if(nodeIdMap.containsKey(mailboxNode)) {
 					mailboxNode.removeMailboxNodeListener(mailboxNodeListener);
 					nodeIdMap.remove(mailboxNode);
 				}
 			}
 			int nextId = treeField.getFirstChild(accountId);
 			while(nextId != -1) {
 				int id = nextId;
 				nextId = treeField.getNextSibling(nextId);
 				treeField.deleteSubtree(id);
 			}
 			
 			// Now get the new mailbox list for the account
 			// and repopulate the tree.
 			MailboxNode rootMailbox = account.getRootMailbox();
 			if(rootMailbox != null) {
 				MailboxNode[] mailboxNodes = rootMailbox.getMailboxes();
 				for(int i=mailboxNodes.length - 1; i>=0; --i) {
 					populateTreeFieldMailbox(accountId, mailboxNodes[i]);
 				}
 			}
 		}
 	}
 	
 	/**
 	 * Recursively traverse the TreeField from a node and build a
 	 * linear vector of all the mailboxes contained within.
 	 * 
 	 * @param result Result vector to add to.
 	 * @param nodeId Starting node.
 	 */
 	private void getMailboxNodes(Vector result, int nodeId) {
 		int id = treeField.getFirstChild(nodeId);
 		while(id != -1) {
 			TreeNode treeNode = (TreeNode)treeField.getCookie(id);
 			if(treeNode.node instanceof MailboxNode) {
 				result.addElement(treeNode.node);
 			}
 			getMailboxNodes(result, id);
 			id = treeField.getNextSibling(id);
 		}
 	}
 
 	private void treeField_DrawTreeItem(
 			TreeField treeField,
 			Graphics graphics,
 			int node, int y, int width, int indent) {
 		TreeNode treeNode = (TreeNode)treeField.getCookie(node);
		int height = treeField.getRowHeight();
 		
 		Bitmap icon = NodeIcons.getIcon(treeNode.node);
		
		graphics.drawBitmap(indent, y+((height-icon.getHeight())/2), height-2, height-2, icon, 0, 0);
 		
 		Font origFont = graphics.getFont();
 		StringBuffer buf = new StringBuffer();
 		buf.append(treeNode.node.toString());
 		
 		if(treeNode.node instanceof AccountNode) {
 			graphics.setFont(origFont.derive(Font.BOLD));
 		}
 		else if(treeNode.node instanceof MailboxNode) {
 			MailboxNode mailboxNode = (MailboxNode)treeNode.node;
 			
 			if(!mailboxNode.isSelectable()) {
                 graphics.setFont(origFont.derive(Font.ITALIC));
 			}
 			
 			int unseenCount = mailboxNode.getUnseenMessageCount();
 			if(unseenCount > 0) {
                 buf.append(" (");
                 buf.append(unseenCount);
                 buf.append(")");
                 graphics.setFont(origFont.derive(Font.BOLD));
 			}
 			else {
                 graphics.setFont(origFont.derive(Font.PLAIN));
 			}
 		}
		graphics.drawText(buf.toString(), indent + height, y, Graphics.ELLIPSIS, width);
 	}
 	
 	/**
 	 * Opens the selected item in the tree.
 	 * 
 	 * @return True if the item was opened, false otherwise.
 	 */
 	private boolean openSelectedItem() {
 		int id = treeField.getCurrentNode();
 		if(id != -1) {
 			Node node = ((TreeNode)treeField.getCookie(id)).node;
 			if(node instanceof MailboxNode) {
 				MailboxNode mailboxNode = (MailboxNode)node;
 				MailboxScreen mailboxScreen = new MailboxScreen(mailboxNode);
 				UiApplication.getUiApplication().pushScreen(mailboxScreen);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	/**
 	 * Disconnect from the mail connection associated with
 	 * the selected account.
 	 */
 	private void disconnectSelectedAccount() {
 		int id = treeField.getCurrentNode();
 		if(id != -1) {
 			Node node = ((TreeNode)treeField.getCookie(id)).node;
 			if(node instanceof AccountNode) {
 				((AccountNode)node).requestDisconnect(false);
 			}
 		}
 	}
 }
