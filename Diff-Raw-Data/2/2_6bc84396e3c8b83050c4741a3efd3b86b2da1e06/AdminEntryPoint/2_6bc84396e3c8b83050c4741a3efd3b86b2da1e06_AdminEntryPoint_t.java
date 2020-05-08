 /**
  *  This file is part of Daxplore Presenter.
  *
  *  Daxplore Presenter is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 2.1 of the License, or
  *  (at your option) any later version.
  *
  *  Daxplore Presenter is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with Daxplore Presenter.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.daxplore.presenter.admin;
 
 import org.daxplore.presenter.admin.inject.AdminInjector;
 
 import com.google.gwt.core.client.EntryPoint;
 import com.google.gwt.core.client.GWT;
 import com.google.gwt.user.client.ui.RootPanel;
 
 public class AdminEntryPoint implements EntryPoint {
 	/**
 	 * This is the entry point method. It is automatically called by GWT to
 	 * create the admin web page.
 	 */
 	@Override
 	public void onModuleLoad() {
 		AdminInjector injector = GWT.create(AdminInjector.class);
 		AdminController adminController = injector.getAdminController();
		adminController.go(RootPanel.get("ID-AdminPanel"));
 	}
 }
