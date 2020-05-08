 /*{{{ header
  * BufferListPlugin.java
  * Copyright (c) 2000-2002 Dirk Moebius
  *
  * :tabSize=4:indentSize=4:noTabs=false:maxLineLen=0:folding=explicit:collapseFolds=1:
  *
  * This program is free software; you can redistribute it and/or
  * modify it under the terms of the GNU General Public License
  * as published by the Free Software Foundation; either version 2
  * of the License, or any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  *}}}
  */
 package bufferlist;
 
 // {{{ imports
 import java.util.ArrayList;
 import java.util.List;
 
 import org.gjt.sp.jedit.EBMessage;
 import org.gjt.sp.jedit.EBPlugin;
 import org.gjt.sp.jedit.EditPane;
 import org.gjt.sp.jedit.ServiceManager;
 import org.gjt.sp.jedit.View;
 import org.gjt.sp.jedit.jEdit;
 import org.gjt.sp.jedit.msg.BufferUpdate;
 import org.gjt.sp.jedit.msg.EditPaneUpdate;
 import org.gjt.sp.util.Log;
// }}}
 
 /**
  * The BufferList plugin.
  * 
  * @author Dirk Moebius
  */
 public class BufferListPlugin extends EBPlugin
 {
 
 	public static final String MENU_SERVICE_TYPE = "bufferlist.MenuEntries";
 
 	private static List<MenuEntries> menuExtensions;
 
 	// {{{ +start() : void
 	public void start()
 	{
 		menuExtensions = new ArrayList<MenuEntries>();
 		loadPopupMenuExtensions();
 	} // }}}
 
 	// {{{ +stop() : void
 	public void stop()
 	{
 		menuExtensions = null;
 	} // }}}
 
 	// {{{ +handleMessage(EBMessage) : void
 	public void handleMessage(EBMessage message)
 	{
 		if (message instanceof BufferUpdate)
 		{
 			BufferUpdate bu = (BufferUpdate) message;
 			if (jEdit.getBooleanProperty("bufferlist.autoshow", false) && bu.getView() != null
 				&& (bu.getWhat() == BufferUpdate.CREATED || bu.getWhat() == BufferUpdate.CLOSED))
 			{
 				bu.getView().getDockableWindowManager().addDockableWindow("bufferlist");
 			}
 		}
 		else if (message instanceof EditPaneUpdate)
 		{
 			EditPaneUpdate epu = (EditPaneUpdate) message;
 			if (jEdit.getBooleanProperty("bufferlist.autoshow", false)
 				&& epu.getWhat() == EditPaneUpdate.BUFFER_CHANGED)
 			{
 				View view = ((EditPane) epu.getSource()).getView();
 				if (view != null)
 				{
 					view.getDockableWindowManager().addDockableWindow("bufferlist");
 				}
 			}
 		}
 	} // }}}
 
 	// {{{ -loadPopupMenuExtensions() : void
 	/**
 	 * loads all the services defined by other plugins of type
 	 * 'bufferlist.MenuEntries' These services must return objects implementing
 	 * the bufferlist.MenuEntries interface
 	 */
 	private void loadPopupMenuExtensions()
 	{
 		String[] serviceNames = ServiceManager.getServiceNames(MENU_SERVICE_TYPE);
 		for (int i = 0; i < serviceNames.length; ++i)
 		{
 			Object service = ServiceManager.getService(MENU_SERVICE_TYPE, serviceNames[i]);
 			if (service instanceof bufferlist.MenuEntries)
 			{
 				menuExtensions.add((MenuEntries) service);
 			}
 			else
 			{
 				Log.log(Log.WARNING, null, "Service " + serviceNames[i]
 					+ " is not a valid bufferlist.MenuEntries service");
 			}
 		}
 	}// }}}
 
 	// {{{ getMenuExtension() : List
 	/**
 	 * returns the List of MenuEntries objects to extend the popup menu.
 	 */
 	static List<MenuEntries> getMenuExtensions()
 	{
 		return menuExtensions;
 	}// }}}
 
 }
