 /*******************************************************************
  * Copyright (c) 2010 Rohit Kumbhar
  *
  * Permission is hereby granted, free of charge, to any person
  * obtaining a copy of this software and associated documentation
  * files (the "Software"), to deal in the Software without
  * restriction, including without limitation the rights to use,
  * copy, modify, merge, publish, distribute, sublicense, and/or sell
  * copies of the Software, and to permit persons to whom the
  * Software is furnished to do so, subject to the following
  * conditions:
  *
  * The above copyright notice and this permission notice shall be
  * included in all copies or substantial portions of the Software.
  *
  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND,
  * EXPRESS OR IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES
  * OF MERCHANTABILITY, FITNESS FOR A PARTICULAR PURPOSE AND
  * NONINFRINGEMENT. IN NO EVENT SHALL THE AUTHORS OR COPYRIGHT
  * HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER LIABILITY,
  * WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING
  * FROM, OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR
  * OTHER DEALINGS IN THE SOFTWARE.
  *******************************************************************/
 package net.yama.android.views.contentfactory;
 
 import java.util.List;
 
 import net.yama.android.Rendezvous;
 import net.yama.android.managers.DataManager;
 import net.yama.android.managers.config.ConfigurationManager;
 import net.yama.android.managers.connection.ApplicationException;
 import net.yama.android.util.Constants;
 import net.yama.android.views.adapter.EventListAdapter;
 import net.yama.android.views.adapter.GroupsListAdapter;
 import net.yama.android.views.components.LoadingView;
 import android.view.View;
 import android.widget.ListView;
 
 public class MainContentFactory extends AbstractContentFactory {
 
 	Rendezvous context;
 	
 	public MainContentFactory(Rendezvous ctx) {
 		super(ctx);
 		this.context = ctx;
 	}
 
 	public View createTabContent(String tag) {
 		
 		View contentView = null;
 		try {
 			
 			// Do I have access?
 			if(!ConfigurationManager.instance.haveAcess())
 				return this.context.authorizeView();
 			
 			// Is my member info available
 			if(ConfigurationManager.instance.getMemberId() == null)
 				DataManager.getMemberInformation();
 			
 			if(Constants.GROUPS_TAB_ID.equalsIgnoreCase(tag))
 				contentView = getGroupsView();
 			else if (Constants.MEETUPS_TAB_ID.equalsIgnoreCase(tag))
 				contentView = getMeetupsView();
 			else
 				contentView = getNullView();
 		} catch (Exception e) {
 			contentView = getExceptionView(e);
 		}
 		
 		return contentView;
 	}
 	
 	private View getGroupsView() throws ApplicationException {
 
 		LoadingView view = new LoadingView(context) {
 			@Override
 			public View getResultsView() throws ApplicationException {
 				List groupsList = DataManager.getAllGroups();
 				ListView groupsView = new ListView(context);
 				GroupsListAdapter adapter = new GroupsListAdapter(groupsList,context);
 				groupsView.setAdapter(adapter);
 				return groupsView;
 			}
 		};
 
 		return view;
 
 	}
 
 	private View getMeetupsView() throws ApplicationException {
 		
 		LoadingView view = new LoadingView(context) {
 			
 			@Override
 			public View getResultsView() throws ApplicationException {
 				List eventsList = DataManager.getAllEvents();
 				ListView eventsView  = new ListView(context);
 				EventListAdapter adapter = new EventListAdapter(eventsList,context);
 				eventsView.setAdapter(adapter);
 				return eventsView;
 			}
 		};
 		
 		return view;
 		
 	}
 
 }
