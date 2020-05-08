 /*
  * (C) Copyright 2006-2010 Nuxeo SAS (http://nuxeo.com/) and contributors.
  *
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the GNU Lesser General Public License
  * (LGPL) version 2.1 which accompanies this distribution, and is available at
  * http://www.gnu.org/licenses/lgpl.html
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * Contributors:
  *     bstefanescu
  */
 package org.nuxeo.ide.connect.ui;
 
 import org.eclipse.swt.custom.BusyIndicator;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 import org.nuxeo.ide.common.FormPreferencePage;
 import org.nuxeo.ide.common.UI;
 import org.nuxeo.ide.common.forms.ActionHandler;
 import org.nuxeo.ide.common.forms.Form;
 import org.nuxeo.ide.common.forms.FormData;
 import org.nuxeo.ide.common.forms.UIObject;
 import org.nuxeo.ide.connect.ConnectPlugin;
 import org.nuxeo.ide.connect.ConnectPreferences;
 import org.nuxeo.ide.connect.Connector;
 import org.nuxeo.ide.connect.StudioProvider;
 import org.nuxeo.ide.connect.studio.StudioProject;
 
 /**
  * @author <a href="mailto:bs@nuxeo.com">Bogdan Stefanescu</a>
  * 
  */
 public class ConnectPreferencePage extends FormPreferencePage implements
         IWorkbenchPreferencePage, FormData {
 
     protected ConnectPreferences prefs;
     
     public ConnectPreferencePage() {
         super(ConnectPlugin.getDefault().getPreferences());
         setFormData(this);
     }
 
     @Override
     protected Form createForm() {
         Form form = new Form();
         form.addActionHandler("connect", new ActionHandler() {
             @Override
             public void handleAction(Form form, UIObject<?> obj, Object event) {
                 BusyIndicator.showWhile(Display.getCurrent(), new Runnable() {
                     @Override
                     public void run() {
                         try {
                             connect();
                         } catch (Exception e) {
                             UI.showError("Failed to connect to Nuxeo Studio", e);
                         }
                     }
                 });
             }
         });
         return form;
     }
 
     protected void connect() throws Exception {
         String host = form.getWidgetValueAsString("host").trim();
         String user = form.getWidgetValueAsString("username").trim();
         String password = form.getWidgetValueAsString("password");
 
         if (user.length() == 0) {
             return;
         }
 
         String v = null;
         v = form.getWidgetValueAsString("host").trim();
         prefs.setHost(v.length() > 0 ? v : null);
         v = form.getWidgetValueAsString("username");
         prefs.setUsername(v.length() > 0 ? v : null);
         v = form.getWidgetValueAsString("password");
         prefs.setPassword(v.length() > 0 ? v : null);
 
         StudioProvider provider = ConnectPlugin.getStudioProvider();
         provider.updateProjects(new Connector(host, user, password).getProjects());
 
         prefs.clear();
 
         org.eclipse.swt.widgets.List list = (org.eclipse.swt.widgets.List) form.getWidgetControl("projects");
         list.removeAll();
         for (StudioProject project : provider.getProjects()) {
            list.add(project.getId());
             prefs.addProject(project);
         }
     }
 
     protected void reset() {
         form.setWidgetValue("host", Connector.DEFAULT_URL);
         form.setWidgetValue("username", "");
         form.setWidgetValue("password", "");
         ((org.eclipse.swt.widgets.List) form.getWidgetControl("projects")).removeAll();
         prefs = new ConnectPreferences();
     }
 
     @Override
     public void load(Form form) throws Exception {
         prefs = ConnectPreferences.load();
         if (prefs.getHost() != null) {
             form.setWidgetValue("host", prefs.getHost());
         }
         if (prefs.getUsername() != null) {
             form.setWidgetValue("username", prefs.getUsername());
         }
         if (prefs.getPassword() != null) {
             form.setWidgetValue("password", prefs.getPassword());
         }
         org.eclipse.swt.widgets.List list = ((org.eclipse.swt.widgets.List) form.getWidgetControl("projects"));
         for (StudioProject project : prefs.getProjects()) {
            list.add(project.getId());
         }
     }
 
     @Override
     public void store(Form form) throws Exception {
         prefs.save();
         ConnectPlugin.getStudioProvider().fireStudioProjectsChanged();
     }
 
     @Override
     protected void performDefaults() {
         reset();
         super.performDefaults();
     }
 }
