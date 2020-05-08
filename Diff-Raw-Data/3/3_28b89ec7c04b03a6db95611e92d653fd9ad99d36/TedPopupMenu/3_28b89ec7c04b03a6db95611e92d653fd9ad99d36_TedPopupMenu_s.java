 package ted;
 
 import java.awt.event.ActionEvent;
 import java.awt.event.ActionListener;
 import java.io.IOException;
 import java.util.Vector;
 
 import javax.swing.JMenuItem;
 import javax.swing.JOptionPane;
 import javax.swing.JPopupMenu;
 /**
  * TED: Torrent Episode Downloader (2005 - 2006)
  * 
  * This is the mainwindow of ted
  * It shows all the shows with their urls, status and more and includes menus
  * and buttons for the user to interact with ted.
  * 
  * @author Roel
  * @author Joost
  *
  * ted License:
  * This file is part of ted. ted and all of it's parts are licensed
  * under GNU General Public License (GPL) version 2.0
  * 
  * for more details see: http://en.wikipedia.org/wiki/GNU_General_Public_License
  *
  */
 
 import ted.ui.editshowdialog.EditShowDialog;
 
 /**
  * @author Joost
  * The TedPopupMenu receives a Vector of TedPopupItems. These menu items are seperated by
  * type and shown in the popup menu. After selecting an item the action that has to be
  * done is determined by the type the menu item has.
  *
  */
 public class TedPopupMenu extends JPopupMenu implements ActionListener
 {
 	private static final long serialVersionUID = 1L;
 	
 	private Vector allItems = new Vector();
 	private Vector search   = new Vector();
 	private Vector category = new Vector();
 	private Vector general  = new Vector();
 	private TedPopupItem help;
 	private TedPopupItem empty;
 
 	private EditShowDialog dialog;
 
 	/**
 	 * Constructs a new TedPopupMenu
 	 * @param v The vector containing the JPopupItems
 	 * @param dialog2 The Episode Dialog that initialized this menu
 	 */
 	public TedPopupMenu(EditShowDialog dialog2, Vector v)
 	{
 		dialog = dialog2;
 		
 		// make a copy determine which action has to be done later on
 		this.allItems = v;
 		
 		// divide the vector in groups
 		this.divideMenu(allItems);
 		
 		// add the "add empty" item
 		String s = Lang.getString("TedEpisodeDialog.FeedsTable.UserDefined");
 		JMenuItem item = new JMenuItem(s);
 		item.addActionListener(this);
 		item.setActionCommand(s);
 		this.add(item);
 		this.addSeparator();
 		
 		// add the groups to the menu (divided by a seperator)
 		this.setMenu(search);
 		this.setMenu(category);
 		this.setMenu(general);
 		
 		try
 		{
 			// retrieve help item text
 			s = Lang.getString("TedEpisodeDialog.Help");
 		}
 		catch(Exception e)
 		{
 			// no help item was available in the vector
 			s = Lang.getString("TedEpisodeDialog.NoHelp");
 		}
 		
 		// add help item to menu
 		item = new JMenuItem(s);
 		item.addActionListener(this);
 		item.setActionCommand(item.getName());
 		this.add(item);
 	}
 	
 	/**
 	 * Divides the given vector in groups based on the type of the 
 	 * JPopupItem 
 	 */
 	private void divideMenu(Vector v)
 	{
 		TedPopupItem item;
 		for(int i=0; i<v.size(); i++)
 		{
 			item = (TedPopupItem)v.get(i);
 			
 			if(item.getType()==TedPopupItem.IS_SEARCH_BASED)
 				search.add(item);
 			else if(item.getType()==TedPopupItem.IS_CATEGORY_BASED)
 				category.add(item);
 			else if(item.getType()==TedPopupItem.IS_GENERAL_FEED)
 				general.add(item);
 			else if(item.getType()==TedPopupItem.IS_HELP)
 				help = item;
 			else if(item.getType()==TedPopupItem.IS_EMPTY)
 				empty = item;
 		}
 	}
 	
 	/**
 	 * Add the items from the vector to the menu
 	 */
 	private void setMenu(Vector v)
 	{
 		if(v.size()!=0)
 		{
 			JMenuItem item;
 			for(int i=0; i<v.size(); i++)
 			{
 				TedPopupItem pi = (TedPopupItem)v.get(i);
 				item = new JMenuItem(pi.getName());
 				item.addActionListener(this);
 				item.setActionCommand(pi.getName());
 				this.add(item);
 			}
 			this.addSeparator();
 		}
 	}
 
 	public void actionPerformed(ActionEvent arg0) 
 	{
 		TedPopupItem item;
 		String action = arg0.getActionCommand();
 		
 		for(int i=0; i<allItems.size(); i++)
 		{
 			item = (TedPopupItem)allItems.get(i);
 			
 			if(item.getName().equals(action))
 			{
 				if(item.getType()==TedPopupItem.IS_SEARCH_BASED)
 					this.openOptionDialog(item.getUrl(), item.getWebsite(), item.getType());
 				else if(item.getType()==TedPopupItem.IS_CATEGORY_BASED)
 					this.openUrl(item.getUrl());
 				else if(item.getType()==TedPopupItem.IS_GENERAL_FEED)
 					this.openOptionDialog(item.getUrl(), item.getWebsite(), item.getType());
 				else if(item.getType()==TedPopupItem.IS_HELP)
 					this.openUrl(item.getUrl());
 				else if(item.getType()==TedPopupItem.IS_EMPTY)
 					dialog.addFeed();
 				
 				return;
 			}
 		}
 	}
 	
 	private void openUrl(String url)
 	{
 		try
 		{
 			BrowserLauncher.openURL(url);
 		} 
 		catch (IOException e) 
 		{
 			e.printStackTrace();
 		}
 	}
 	
 	private void openOptionDialog(String url, String website, int type)
 	{
 		String question;
 		
 		// decide which question has to be shown
 		if(type==TedPopupItem.IS_SEARCH_BASED)
 			question = Lang.getString("TedEpisodeDialog.DialogFindSearch");
 		else
 			question = Lang.getString("TedEpisodeDialog.DialogFindGeneral");
 		
 		// show question dialog
 		 Object[] options = { Lang.getString("TedGeneral.Yes"), Lang.getString("TedGeneral.No"), Lang.getString("TedGeneral.Website") };
 		 int selectedValue = JOptionPane.showOptionDialog(null, 
 				 	question, Lang.getString("TedGeneral.Question"),
 		             JOptionPane.YES_NO_CANCEL_OPTION, JOptionPane.QUESTION_MESSAGE,
 		             null, options, options[0]);
 		 
 		 if(selectedValue == 0) //Yes
 		 {
 			 String name = dialog.getShowName();
 			 if(!name.equals(""))
 			 {
 				 // do action based on type
 				 if(type==TedPopupItem.IS_SEARCH_BASED)
 					 url = url.replace("#NAME#", name); // add name to rss query
 				 else if(type==TedPopupItem.IS_GENERAL_FEED)
 					 dialog.addKeywords(name); // use general feeds combined with keywords
 					 
 				 dialog.addFeed(url);
 			 }
 			 else
 			 {
 				 JOptionPane.showMessageDialog(null, Lang.getString("TedEpisodeDialog.DialogFindError"));
 			 }
 		 }
 		 else if(selectedValue == 1) //No
 		 { }
 		 else if(selectedValue == 2) //Go to website
 		 { 
 			 this.openUrl(website);
 		 }
 	}
 }
