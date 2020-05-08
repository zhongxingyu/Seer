 /*
  * $Id$
  * (c) Copyright 2000 wingS development team.
  *
  * This file is part of the wingS demo (http://wings.mercatis.de).
  *
  * The wingS demo is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or (at your option) any later version.
  *
  * Please see COPYING for the complete licence.
  */
  
 package ldap;
  
 import java.awt.event.*;
 import java.io.*;
 import java.util.Hashtable;
 import java.util.Enumeration;
 import java.util.ArrayList;
  
 import javax.servlet.*;
 import javax.servlet.http.*;
 
 import javax.swing.*;
 import javax.swing.tree.*;
 import javax.swing.event.TreeSelectionListener;
 import javax.swing.event.TreeSelectionEvent;
 
  
 import org.wings.*;
 import org.wings.io.*;
 import org.wings.plaf.*;
 import org.wings.servlet.*;
 import org.wings.session.*;
 import org.wings.util.*;
 import java.awt.Color;
 
  
 public class LdapBrowserSession 
     extends SessionServlet
     implements SConstants, TreeSelectionListener, ActionListener
 {
     SLabel timeMeasure = new SLabel();
     LdapClient client = null;
     STabbedPane tabs = null;
     STree ldapTree;
     DefaultTreeModel treeModel;
     Hashtable attrHTable;
     Hashtable attrHLTable;
     SPanel attrPanel;
     SPanel treePanel;
     STextField text;
     SLabel attrLabel;
     final ArrayList textList = new ArrayList();
     Hashtable componentTable = new Hashtable();
     String dn;
     LdapTreeNode node;
 
     public LdapBrowserSession(Session session) {
         super(session);
         System.out.println("LdapBrowser starting ...");  
     }
     
     
     public void postInit(ServletConfig config) {
 	SContainer contentPane = getFrame().getContentPane();
 
 	tabs = new STabbedPane();
 	contentPane.add(tabs);
 
 	SForm settingsForm = new SForm(new SGridLayout(2));
 	tabs.add(settingsForm, "connection settings");
 
 	SLabel descServer = new SLabel("sever:port");
 	final STextField server = new STextField("");
 	server.setColumns(30);
 	server.setText(((PropertyService)getSession()).getProperty("ldap.server.host"));
 	settingsForm.add(descServer);
 	settingsForm.add(server);
 
 	SLabel descBaseDN = new SLabel("base DN");
 	final STextField baseDN = new STextField("");
 	baseDN.setColumns(30);
 	baseDN.setText(((PropertyService)getSession()).getProperty("ldap.server.basedn"));
 	settingsForm.add(descBaseDN);
 	settingsForm.add(baseDN);
 
 	SLabel descBindDN = new SLabel("bind DN");
 	final STextField bindDN= new STextField("");
 	bindDN.setColumns(30);
 	settingsForm.add(descBindDN);
 	settingsForm.add(bindDN);
 
 	SLabel descBindDNPassword = new SLabel("password");
 	final SPasswordField bindDNPassword= new SPasswordField();
 	bindDNPassword.setColumns(30);
 	settingsForm.add(descBindDNPassword);
 	settingsForm.add(bindDNPassword);
 
 	final SButton connectButton = new SButton("connect");
 	final SButton disconnectButton = new SButton("disconnect");
 	disconnectButton.setVisible(false);
 	settingsForm.add(connectButton);
 	settingsForm.add(disconnectButton);
 
 	SPanel panel = new SPanel(new SGridLayout(2));
 	tabs.add(panel, "browser");
 
 	treePanel = new SPanel();
 	panel.add(treePanel);
 
 	ldapTree = createTree();
 	treePanel.add(ldapTree);
 
 	attrPanel = new SPanel();
 	panel.add(attrPanel);
 
 	connectButton.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    client = new LdapClient(server.getText(),
 					    baseDN.getText(),
 					    bindDN.getText(),
 					    bindDNPassword.getText());
 
 		    boolean success = client.getSuccess();
 		    if (success) {
 			connectButton.setVisible(false);
 			disconnectButton.setVisible(true);
 			tabs.setSelectedIndex(1);
 		    }
 		    else {
 			connectButton.setVisible(true);
 			disconnectButton.setVisible(false);
 		    }
 
 		    if (success) {
 			setLdapClient(client);
 			ldapTree = createTree(client);
 			treePanel.removeAll();
 			treePanel.add(ldapTree);
 		    }
 		}
 	    });
 
 	disconnectButton.addActionListener(new ActionListener() {
 		public void actionPerformed(ActionEvent e) {
 		    LdapClient c;
 		    c = getLdapClient();
 		    if (c != null) {
 			c.close();
 			System.out.println("client is non null");
 		    }
 		    treePanel.removeAll();
 		    attrPanel.removeAll();
 
 		    bindDNPassword.setText("");
 
 		    disconnectButton.setVisible(false);
 		    connectButton.setVisible(true);
 		}
 	    });
     }
     
 
 
     private LdapClient getLdapClient() {
 	return client;
     }
 
     private void setLdapClient(LdapClient c) {
 	this.client = c;
     }
     
     private STree createTree() {
 	STree tree = new STree(new DefaultTreeModel(new DefaultMutableTreeNode("empty")));
 	return tree;
     }
 
     private STree createTree(LdapClient client) {
 	LdapTreeNode root = null;
 	if (client != null) {
 	    root = new LdapTreeNode(client, null, client.getBaseDN());
 	}	
 	else {
 	    root = new LdapTreeNode("not connected");
 	}
 
 	final STree tree;
 	tree = new STree(new DefaultTreeModel(root));	
         tree.getSelectionModel().setSelectionMode(TreeSelectionModel.SINGLE_TREE_SELECTION);
         tree.addTreeSelectionListener(this);
 
 	return tree;
     }
 
     public void actionPerformed(ActionEvent evt) {
 	String newValue;
 	String oldValue;
 	
 	System.out.println("commit wurde gedrueckt");
 	
 	Enumeration le;
 	le = componentTable.keys();
 	int ii = 0;
 	while (le !=null && le.hasMoreElements()) {
 	    System.out.println("ist ja was drin");
 	    Object key = le.nextElement();
 	    System.out.println("da ist ja ein "+ key.getClass().getName());
 	    Object component = componentTable.get(key);
 	    if (component==null) System.out.println("null");
 	    
 	    Hashtable valuesToChange = new Hashtable();
 
 	    if (component.getClass().getName().equals("org.wings.STextField")) {
 		STextField t = (STextField)component;
 		newValue = t.getText();
 		System.out.println("ja ja ");
 		if (newValue == null) System.out.println("new value null");
 		System.out.println("new value " +newValue);
 		oldValue = (String)(textList.get(ii));
 		System.out.println("oldvalue " + oldValue);
 		ii++;
 		if (!oldValue.equals(newValue)) {
 		    valuesToChange.put(((SLabel)key).getText(),newValue);
 		}
 	    }
 	    
 	    if (!valuesToChange.isEmpty()) {
 		LdapClient c = getLdapClient();
 		System.out.println("einige attr. zu aendern");
 		c.modifyAttributes(getDN(), valuesToChange);
 		LdapTreeNode newNode = new LdapTreeNode(c,(LdapTreeNode)node.getParent(),getDN());
 		//c.modifyAttributes(valuesToChange);
 	    }
 	    
 	    if (key.getClass().getName().equals("org.wings.SLabel"))
 		System.out.println(key.getClass().getName());
 	}
     }
 
     private void setDN(String dn) {
 	this.dn = dn;
     }
 
     public String getDN() {
 	return dn;
     }
    
     public LdapTreeNode getNode() {
 	return node;
     }
 
     private void setNode(LdapTreeNode node) {
 	this.node =node;
     }
 
     public void valueChanged(TreeSelectionEvent e) {
 	//final ArrayList textList = new ArrayList();
 	//final Hashtable componentTable = new Hashtable();
 
 	LdapTreeNode node = (LdapTreeNode)ldapTree.getLastSelectedPathComponent();
 	if (node == null) {
 	    System.out.println("nothing's selected!");
 	    return;
 	}
 	System.err.println("selected node: " + node.getCompleteDN());
 
 	if (node != null)
 	    setDN(node.getCompleteDN());
 	setNode((LdapTreeNode)node);
 
 	LdapClient c = getLdapClient();
 	Hashtable attributes = new Hashtable();
 	SForm attrForm = null;
 	SButton commit = new SButton("commit changes");
 	attrPanel.removeAll();
 
 	attrForm = new SForm();
 	SGridLayout sgl = new SGridLayout(0,2);
 	attrForm.setLayout(sgl);
 
 	attrHTable = new Hashtable();
 	attrHLTable = new Hashtable();
 	componentTable = new Hashtable();
 
 	attributes = c.getDNAttributes(node.getCompleteDN());
 	Enumeration enum = attributes.keys();
 	while (enum != null && enum.hasMoreElements()) {
 	    Object key = enum.nextElement();
 	    SLabel label = new SLabel((String)key);
 
 	    boolean wAccess
 		= c.getWAccess(node.getCompleteDN(), (String)key, (String)attributes.get(key));
 
 	    if (wAccess) {
 		text = new STextField((String)attributes.get(key));
 		text.setColumns(20);
 		componentTable.put(label, text);
 		textList.add(text.getText());
 		System.out.println("put " + text.getClass().getName());
 	    }
 	    else {
 		attrLabel = new SLabel((String)attributes.get(key));
 		componentTable.put(label, attrLabel);
 		textList.add(attrLabel.getText());
 		System.out.println("put " + attrLabel.getClass().getName());
 	    }
 	}
 	
 	Enumeration compEnum;
 	compEnum = componentTable.keys();
 	while (compEnum!=null && compEnum.hasMoreElements()) {
 	    Object key = compEnum.nextElement();
 	    attrForm.add((SLabel)key);
 	    attrForm.add((SComponent)componentTable.get(key));
 	}
 
 	attrForm.add(commit);
 	attrPanel.add(attrForm);
 
 	commit.addActionListener(this);
     }
 
     private void createNodes(DefaultMutableTreeNode root)
     {
 	//return new STree();
     }
     
     public void processRequest(HttpServletRequest req,
                                HttpServletResponse res)
         throws ServletException, IOException
     {
 	measure.start("time to generate HTML Code ");
 	// then write the data of the response
 	// Dies ist ein Dummy-Aufruf, um die Zeit zu messen; es wird
 	// sozusagen die Seite zweimal aufgebaut, denn die Ausgabe der
 	// Zeit ist ja selbst wieder eine Component.
 	String erg = getFrame().show();
 	measure.stop();
 	timeMeasure.setText(measure.print());
             measure.reset();
     }
     
     /**
      * Servletinfo
      */
     public String getServletInfo() {
         return "LdapTest";
     }              
 }
