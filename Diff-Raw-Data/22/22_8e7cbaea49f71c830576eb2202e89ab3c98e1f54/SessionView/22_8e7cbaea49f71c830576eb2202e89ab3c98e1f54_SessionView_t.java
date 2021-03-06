 /*
  * DPP - Serious Distributed Pair Programming
  * (c) Freie Universitaet Berlin - Fachbereich Mathematik und Informatik - 2006
  * (c) Riad Djemili - 2006
  * 
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 1, or (at your option)
  * any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 675 Mass Ave, Cambridge, MA 02139, USA.
  */
 package de.fu_berlin.inf.dpp.ui;
 
 import org.eclipse.jface.action.IMenuListener;
 import org.eclipse.jface.action.IMenuManager;
 import org.eclipse.jface.action.IToolBarManager;
 import org.eclipse.jface.action.MenuManager;
 import org.eclipse.jface.action.Separator;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.PreferenceConverter;
 import org.eclipse.jface.util.IPropertyChangeListener;
 import org.eclipse.jface.util.PropertyChangeEvent;
 import org.eclipse.jface.viewers.IColorProvider;
 import org.eclipse.jface.viewers.IStructuredContentProvider;
 import org.eclipse.jface.viewers.ITableFontProvider;
 import org.eclipse.jface.viewers.ITableLabelProvider;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Color;
 import org.eclipse.swt.graphics.Font;
 import org.eclipse.swt.graphics.FontData;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.swt.graphics.RGB;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Menu;
 import org.eclipse.ui.IActionBars;
 import org.eclipse.ui.IWorkbenchActionConstants;
 import org.eclipse.ui.editors.text.EditorsUI;
 import org.eclipse.ui.part.ViewPart;
 import org.eclipse.ui.texteditor.AnnotationPreference;
 import org.eclipse.ui.texteditor.AnnotationPreferenceLookup;
 
 import de.fu_berlin.inf.dpp.Saros;
 import de.fu_berlin.inf.dpp.User;
 import de.fu_berlin.inf.dpp.editor.annotations.SelectionAnnotation;
 import de.fu_berlin.inf.dpp.invitation.IIncomingInvitationProcess;
 import de.fu_berlin.inf.dpp.net.JID;
 import de.fu_berlin.inf.dpp.project.ISessionListener;
 import de.fu_berlin.inf.dpp.project.ISessionManager;
 import de.fu_berlin.inf.dpp.project.ISharedProject;
 import de.fu_berlin.inf.dpp.project.ISharedProjectListener;
 import de.fu_berlin.inf.dpp.ui.actions.ConsistencyAction;
 import de.fu_berlin.inf.dpp.ui.actions.FollowModeAction;
 import de.fu_berlin.inf.dpp.ui.actions.GiveDriverRoleAction;
 import de.fu_berlin.inf.dpp.ui.actions.LeaveSessionAction;
 import de.fu_berlin.inf.dpp.ui.actions.OpenInviteInterface;
 import de.fu_berlin.inf.dpp.ui.actions.RemoveAllDriverRoleAction;
 import de.fu_berlin.inf.dpp.ui.actions.TakeDriverRoleAction;
 
 public class SessionView extends ViewPart implements ISessionListener,
         IPropertyChangeListener {
 
     private TableViewer viewer;
 
     private ISharedProject sharedProject;
 
     private GiveDriverRoleAction giveDriverRoleAction;
 
     private TakeDriverRoleAction takeDriverRoleAction;
 
     private IPreferenceStore store = null;
 
    private FollowModeAction followModeAction;

     private class SessionContentProvider implements IStructuredContentProvider,
             ISharedProjectListener {
 
         private TableViewer tableViewer;
 
         public void inputChanged(Viewer v, Object oldInput, Object newInput) {
             if (oldInput != null) {
                 ISharedProject oldProject = (ISharedProject) oldInput;
                 oldProject.removeListener(this);
             }
 
             SessionView.this.sharedProject = (ISharedProject) newInput;
             if (SessionView.this.sharedProject != null) {
                 SessionView.this.sharedProject.addListener(this);
             }
 
             this.tableViewer = (TableViewer) v;
             this.tableViewer.refresh();
 
             updateEnablement();
         }
 
         public Object[] getElements(Object parent) {
             if (SessionView.this.sharedProject != null) {
                 return SessionView.this.sharedProject.getParticipants()
                         .toArray();
             }
 
             return new Object[] {};
         }
 
         public void driverChanged(JID driver, boolean replicated) {
            User participant = SessionView.this.sharedProject
                    .getParticipant(driver);

            // if the local host become driver leave follow mode
            if (participant.getJid().equals(Saros.getDefault().getMyJID())) {
                if (SessionView.this.sharedProject.isDriver(participant))
                    followModeAction.setFollowMode(false);
            }
             refreshTable();
         }
 
         public void userJoined(JID user) {
             refreshTable();
         }
 
         public void userLeft(JID user) {
             refreshTable();
         }
 
         public void dispose() {
         }
 
         private void refreshTable() {
             Display.getDefault().asyncExec(new Runnable() {
                 public void run() {
                     SessionContentProvider.this.tableViewer.refresh();
                 }
             });
         }
     }
 
     private class SessionLabelProvider extends LabelProvider implements
             ITableLabelProvider, IColorProvider, ITableFontProvider {
 
         private final Image userImage = SarosUI.getImage("icons/user.png");
         private final Image driverImage = SarosUI
                 .getImage("icons/user_edit.png");
         private Font boldFont = null;
 
         public String getColumnText(Object obj, int index) {
             User participant = (User) obj;
 
             StringBuffer sb = new StringBuffer(participant.getJid().getName());
             // if (participant.equals(sharedProject.getDriver())) {
             if (SessionView.this.sharedProject.isDriver(participant)) {
 
                 sb.append(" (Driver)");
             }
 
             return sb.toString();
         }
 
         @Override
         public Image getImage(Object obj) {
             User user = (User) obj;
             if (SessionView.this.sharedProject.isDriver(user)) {
                 return this.driverImage;
             }
             // return user.equals(sharedProject.getDriver()) ? driverImage :
             // userImage;
             return this.userImage;
         }
 
         public Image getColumnImage(Object obj, int index) {
             return getImage(obj);
         }
 
         // TODO getting current color doesnt uses when default was changed.
         public Color getBackground(Object element) {
             User user = (User) element;
 
             if (user.getJid().equals(Saros.getDefault().getMyJID())) {
                 return null;
             }
 
             int colorid = user.getColorID();
             String mytype = SelectionAnnotation.TYPE + "."
                     + new Integer(colorid + 1).toString();
 
             AnnotationPreferenceLookup lookup = org.eclipse.ui.editors.text.EditorsUI
                     .getAnnotationPreferenceLookup();
             AnnotationPreference ap = lookup.getAnnotationPreference(mytype);
             if (ap == null) {
                 return null;
             }
 
             IPreferenceStore store = EditorsUI.getPreferenceStore();
             RGB rgb = PreferenceConverter.getColor(store, ap
                     .getColorPreferenceKey());
 
             return new Color(Display.getDefault(), rgb);
 
         }
 
         public Color getForeground(Object element) {
             return null;
         }
 
         public Font getFont(Object element, int columnIndex) {
             if (this.boldFont == null) {
                 Display disp = SessionView.this.viewer.getControl()
                         .getDisplay();
                 FontData[] data = disp.getSystemFont().getFontData();
                 for (FontData fontData : data) {
                     fontData.setStyle(SWT.BOLD);
                 }
                 this.boldFont = new Font(disp, data);
             }
 
             User user = (User) element;
             if (user.getJid().equals(Saros.getDefault().getMyJID())) {
                 return this.boldFont;
             }
             return null;
         }
 
         @Override
         public void dispose() {
             if (this.boldFont != null) {
                 this.boldFont.dispose();
                 this.boldFont = null;
             }
 
             super.dispose();
         }
     }
 
     /**
      * The constructor.
      */
     public SessionView() {
         this.store = EditorsUI.getPreferenceStore();
         this.store.addPropertyChangeListener(this);
     }
 
     @Override
     protected void finalize() throws Throwable {
         this.store.removePropertyChangeListener(this);
         super.finalize();
     }
 
     public void sessionStarted(final ISharedProject session) {
         Display.getDefault().asyncExec(new Runnable() {
             public void run() {
                 SessionView.this.viewer.setInput(session);
             }
         });
     }
 
     public void sessionEnded(ISharedProject session) {
         Display.getDefault().asyncExec(new Runnable() {
             public void run() {
                 SessionView.this.viewer.setInput(null);
             }
         });
         this.sharedProject = null;
     }
 
     public void invitationReceived(IIncomingInvitationProcess process) {
         // ignore
     }
 
     /**
      * This is a callback that will allow us to create the viewer and initialize
      * it.
      */
     @Override
     public void createPartControl(Composite parent) {
         this.viewer = new TableViewer(parent, SWT.MULTI | SWT.H_SCROLL
                 | SWT.V_SCROLL);
         this.viewer.setContentProvider(new SessionContentProvider());
         this.viewer.setLabelProvider(new SessionLabelProvider());
         this.viewer.setInput(null);
 
         this.giveDriverRoleAction = new GiveDriverRoleAction(this.viewer);
         this.takeDriverRoleAction = new TakeDriverRoleAction(this.viewer);
 
         contributeToActionBars();
         hookContextMenu();
         attachSessionListener();
         updateEnablement();
 
         setPartName("Shared Project Session");
     }
 
     /**
      * Passing the focus request to the viewer's control.
      */
     @Override
     public void setFocus() {
         this.viewer.getControl().setFocus();
     }
 
     /**
      * Needs to called from the UI thread.
      */
     private void updateEnablement() {
         this.viewer.getControl().setEnabled(this.sharedProject != null);
     }
 
     private void attachSessionListener() {
         ISessionManager sessionManager = Saros.getDefault().getSessionManager();
 
         sessionManager.addSessionListener(this);
         if (sessionManager.getSharedProject() != null) {
             this.viewer.setInput(sessionManager.getSharedProject());
         }
     }
 
     private void contributeToActionBars() {
         IActionBars bars = getViewSite().getActionBars();
         IToolBarManager toolBar = bars.getToolBarManager();
 
        this.followModeAction = new FollowModeAction();
         toolBar.add(new ConsistencyAction());
         toolBar.add(new OpenInviteInterface());
         toolBar.add(new RemoveAllDriverRoleAction());
        toolBar.add(followModeAction);
         toolBar.add(new LeaveSessionAction());
     }
 
     private void hookContextMenu() {
         MenuManager menuMgr = new MenuManager("#PopupMenu");
         menuMgr.setRemoveAllWhenShown(true);
         menuMgr.addMenuListener(new IMenuListener() {
             public void menuAboutToShow(IMenuManager manager) {
                 fillContextMenu(manager);
             }
         });
 
         Menu menu = menuMgr.createContextMenu(this.viewer.getControl());
 
         this.viewer.getControl().setMenu(menu);
         getSite().registerContextMenu(menuMgr, this.viewer);
     }
 
     private void fillContextMenu(IMenuManager manager) {
         manager.add(this.giveDriverRoleAction);
         manager.add(this.takeDriverRoleAction);
 
         // Other plug-ins can contribute there actions here
         manager.add(new Separator(IWorkbenchActionConstants.MB_ADDITIONS));
     }
 
     public void propertyChange(PropertyChangeEvent event) {
         this.viewer.refresh();
 
     }
 }
