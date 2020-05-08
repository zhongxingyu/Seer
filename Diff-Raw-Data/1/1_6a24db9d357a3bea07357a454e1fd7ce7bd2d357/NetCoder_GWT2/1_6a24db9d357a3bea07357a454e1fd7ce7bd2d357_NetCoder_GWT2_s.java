 // NetCoder - a web-based pedagogical programming environment
 // Copyright (C) 2011, Jaime Spacco <jspacco@knox.edu>
 // Copyright (C) 2011, David H. Hovemeyer <dhovemey@ycp.edu>
 //
 // This program is free software: you can redistribute it and/or modify
 // it under the terms of the GNU Affero General Public License as published by
 // the Free Software Foundation, either version 3 of the License, or
 // (at your option) any later version.
 //
 // This program is distributed in the hope that it will be useful,
 // but WITHOUT ANY WARRANTY; without even the implied warranty of
 // MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 // GNU Affero General Public License for more details.
 //
 // You should have received a copy of the GNU Affero General Public License
 // along with this program.  If not, see <http://www.gnu.org/licenses/>.
 
 package edu.ycp.cs.netcoder.client;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.user.client.ui.RootLayoutPanel;
 import com.google.gwt.user.client.ui.Widget;
 
 import edu.ycp.cs.netcoder.client.logchange.ChangeList;
 import edu.ycp.cs.netcoder.shared.affect.AffectEvent;
 import edu.ycp.cs.netcoder.shared.problems.User;
 import edu.ycp.cs.netcoder.shared.util.Observable;
 import edu.ycp.cs.netcoder.shared.util.Observer;
 
 /**
  * Entry point classes define <code>onModuleLoad()</code>.
  */
 public class NetCoder_GWT2 implements EntryPoint, Observer {
 	// Client session data.
 	private Session session;
 	
 	private Widget currentView;
 	
 	/**
 	 * This is the entry point method.
 	 */
 	public void onModuleLoad() {
 		// Create session
 		session = new Session();
 		session.add(new ChangeList());
 		session.add(new AffectEvent());
 		
 		// Observe Session changes so we're notified of successful login
 		session.addObserver(this);
 		
 		changeView(new LoginView(session));
 	}
 	
 	public void changeView(Widget view) {
 		if (currentView != null) {
 			RootLayoutPanel.get().remove(currentView);
 		}
 		RootLayoutPanel.get().add(view);
 		currentView = view;
 	}
 	
 	@Override
 	public void update(Observable obj, Object hint) {
 		if (currentView.getClass() == LoginView.class && session.get(User.class) != null) {
 			// User just successfully logged in - switch to development view
 			DevelopmentView developmentView = new DevelopmentView(session);
 			changeView(developmentView);
 			developmentView.startEditor();
 		} else if (currentView.getClass() == DevelopmentView.class && session.get(User.class) == null) {
 			changeView(new LoginView(session));
 		}
 	}
 }
