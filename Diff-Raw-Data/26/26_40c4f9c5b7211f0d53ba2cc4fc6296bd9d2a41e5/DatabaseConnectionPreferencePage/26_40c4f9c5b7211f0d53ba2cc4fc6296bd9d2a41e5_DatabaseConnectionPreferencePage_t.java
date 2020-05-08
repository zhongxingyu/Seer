 /*******************************************************************************
  * Copyright (c) 2004, 2010 BREDEX GmbH.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     BREDEX GmbH - initial API and implementation and/or initial documentation
  *******************************************************************************/
 package org.eclipse.jubula.client.ui.preferences;
 
 import java.lang.reflect.InvocationTargetException;
import java.util.List;
 
 import org.eclipse.core.databinding.beans.BeanProperties;
 import org.eclipse.core.databinding.observable.list.IObservableList;
 import org.eclipse.core.databinding.observable.list.WritableList;
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.jface.databinding.viewers.ViewerSupport;
 import org.eclipse.jface.dialogs.IDialogConstants;
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.preference.PreferencePage;
 import org.eclipse.jface.viewers.DoubleClickEvent;
 import org.eclipse.jface.viewers.IDoubleClickListener;
 import org.eclipse.jface.viewers.ISelectionChangedListener;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.jface.viewers.ListViewer;
 import org.eclipse.jface.viewers.SelectionChangedEvent;
 import org.eclipse.jface.window.Window;
 import org.eclipse.jface.wizard.WizardDialog;
 import org.eclipse.jubula.client.core.Activator;
 import org.eclipse.jubula.client.core.preferences.database.DatabaseConnection;
 import org.eclipse.jubula.client.core.preferences.database.DatabaseConnectionConverter;
 import org.eclipse.jubula.client.ui.Plugin;
 import org.eclipse.jubula.client.ui.dialogs.DatabaseConnectionDialog;
 import org.eclipse.jubula.client.ui.i18n.Messages;
 import org.eclipse.jubula.client.ui.widgets.JBText;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 import org.eclipse.ui.preferences.ScopedPreferenceStore;
 
 /**
  * Preference page for configuring Database Connections.
  * 
  * @author BREDEX GmbH
  * @created 10.01.2011
  */
 public class DatabaseConnectionPreferencePage extends PreferencePage 
     implements IWorkbenchPreferencePage {
 
     /** 
      * pre-configured factory for creating grid data for the buttons on the 
      * side of the database list 
      */
     private static final GridDataFactory BUTTON_DATA_FACTORY =
         GridDataFactory.fillDefaults().align(SWT.FILL, SWT.BEGINNING);
     
     /** 
      * global listener to select/deselect text in text fields during widget 
      * traversal 
      */
     private static final Listener SELECT_ALL_LISTENER = 
         new Listener() {
             private boolean m_isTraversing = false;
         
             public void handleEvent(Event event) {
                 switch (event.type) {
                     case SWT.FocusIn:
                         if (m_isTraversing) {
                             m_isTraversing = false;
                             if (event.widget instanceof JBText) {
                                 ((JBText)event.widget).selectAll();
                             }
                         }
                         break;
                     case SWT.FocusOut:
                         if (event.widget instanceof Text) {
                             ((Text)event.widget).clearSelection();
                         }
                         break;
                     case SWT.Traverse:
                         if (event.doit) {
                             m_isTraversing = true;
                         }
                         break;
     
                     default:
                         break;
                 }
             }
         };
     
     /** list of managed connections */
     private IObservableList m_connectionList;
 
     /**
      * 
      * {@inheritDoc}
      */
     public void init(IWorkbench workbench) {
         setPreferenceStore(new ScopedPreferenceStore(
                 new InstanceScope(), Activator.PLUGIN_ID));
     }
 
     @Override
     protected Control createContents(Composite parent) {
         Composite composite = new Composite(parent, SWT.NONE);
         GridDataFactory.fillDefaults().grab(true, true).applyTo(composite);
         GridLayoutFactory.fillDefaults().numColumns(2).applyTo(composite);
 
        m_connectionList = new WritableList(
                parsePreferences(getPreferenceStore()),
                DatabaseConnection.class);
         final ListViewer connectionViewer = new ListViewer(composite);
         Control listControl = connectionViewer.getControl();
         GridDataFactory.fillDefaults().grab(true, true).span(1, 3)
             .hint(SWT.DEFAULT, SWT.DEFAULT).applyTo(listControl);
         ViewerSupport.bind(connectionViewer, m_connectionList, 
                 BeanProperties.value(DatabaseConnection.PROP_NAME_NAME));
         
         createAddButton(composite, m_connectionList);
         
         createRemoveButton(composite, m_connectionList, connectionViewer);
         
         createEditButton(composite, connectionViewer);
         
         return composite;
     }
 
     /**
      * 
      * @param parent The parent composite.
      * @param connectionViewer The viewer containing the elements affected by 
      *                         pressing the created button.
      */
     private void createEditButton(Composite parent,
             final ListViewer connectionViewer) {
         
         final IDoubleClickListener editListener = new IDoubleClickListener() {
             public void doubleClick(DoubleClickEvent event) {
                 Object selectedObj = 
                     ((IStructuredSelection)event.getSelection())
                         .getFirstElement();
                 if (selectedObj instanceof DatabaseConnection) {
                     DatabaseConnection selectedConn = 
                         (DatabaseConnection)selectedObj;
 
                     try {
                         DatabaseConnectionDialog databaseConnectionWizard = 
                             new DatabaseConnectionDialog(
                                     new DatabaseConnection(selectedConn));
                             
                         if (showDialog(databaseConnectionWizard, 
                                 event.getViewer().getControl().getDisplay()) 
                                     == Window.OK) {
                             DatabaseConnection modifiedConn = 
                                 databaseConnectionWizard
                                     .getEditedConnection();
                             selectedConn.setName(modifiedConn.getName());
                             selectedConn.setConnectionInfo(
                                     modifiedConn.getConnectionInfo());
                         }
                     } catch (IllegalAccessException e) {
                         throw new RuntimeException(e);
                     } catch (InstantiationException e) {
                         throw new RuntimeException(e);
                     } catch (InvocationTargetException e) {
                         throw new RuntimeException(e);
                     } catch (NoSuchMethodException e) {
                         throw new RuntimeException(e);
                     }
                 } else if (selectedObj != null) {
                     throw new RuntimeException(Messages.
                         DatabaseConnectionPrefPageSelecObjIsOfIncorrectType);
                 }
             }
         }; 
 
         connectionViewer.addDoubleClickListener(editListener);
         
         final Button editButton = new Button(parent, SWT.NONE);
         BUTTON_DATA_FACTORY.applyTo(editButton);
         editButton.setEnabled(false);
         connectionViewer.addSelectionChangedListener(
                 new ISelectionChangedListener() {
                     public void selectionChanged(SelectionChangedEvent event) {
                         IStructuredSelection sel =
                             (IStructuredSelection)event.getSelection();
                         editButton.setEnabled(sel.size() == 1);
                     }
                 });
         editButton.setText(
                 Messages.DatabaseConnectionPreferencePageEditButtonLabel);
         editButton.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent event) {
                 editListener.doubleClick(new DoubleClickEvent(
                         connectionViewer, connectionViewer.getSelection()));
             }
 
         });
         
     }
 
     /**
      * Displays a dialog for the given wizard.
      * 
      * @param databaseConnectionWizard The wizard to display in the dialog.
      * @param display Used for registering/deregistering global listeners.
      * @return the result of {@link Window#open()}.
      */
     private static int showDialog(
             DatabaseConnectionDialog databaseConnectionWizard,
             Display display) {
         WizardDialog dialog = 
             new WizardDialog(Plugin.getShell(), 
                     databaseConnectionWizard) {
                 protected void createButtonsForButtonBar(
                         Composite parent) {
                     super.createButtonsForButtonBar(parent);
                     Button finishButton = 
                         getButton(IDialogConstants.FINISH_ID);
                     finishButton.setText(
                             IDialogConstants.OK_LABEL);
                 }
             };
         databaseConnectionWizard.setWindowTitle(
                 Messages.DatabaseConnectionDialogTitle);
         dialog.setHelpAvailable(true);
         
         
         display.addFilter(
                 SWT.FocusIn, SELECT_ALL_LISTENER);
         display.addFilter(
                 SWT.FocusOut, SELECT_ALL_LISTENER);
         display.addFilter(
                 SWT.Traverse, SELECT_ALL_LISTENER);
 
         try {
             return dialog.open();
         } finally {
             display.removeFilter(
                     SWT.FocusIn, SELECT_ALL_LISTENER);
             display.removeFilter(
                     SWT.FocusOut, SELECT_ALL_LISTENER);
             display.removeFilter(
                     SWT.Traverse, SELECT_ALL_LISTENER);
         }
     }
 
     /**
      * 
      * @param parent The parent composite.
      * @param existingConnections List of connections contained in the viewer.
      * @param connectionViewer The viewer containing the elements affected by 
      *                         pressing the created button.
      */
     private void createRemoveButton(Composite parent,
             final IObservableList existingConnections,
             final ListViewer connectionViewer) {
         final Button removeButton = new Button(parent, SWT.NONE);
         BUTTON_DATA_FACTORY.applyTo(removeButton);
         removeButton.setEnabled(false);
         connectionViewer.addSelectionChangedListener(
                 new ISelectionChangedListener() {
                     public void selectionChanged(SelectionChangedEvent event) {
                         removeButton.setEnabled(
                                 !event.getSelection().isEmpty());
                     }
                 });
         removeButton.setText(
                 Messages.DatabaseConnectionPreferencePageRemoveButtonLabel);
         removeButton.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent e) {
                 existingConnections.removeAll(
                         ((IStructuredSelection)connectionViewer.getSelection())
                             .toList());
             }
         });
     }
 
     /**
      * 
      * @param parent The parent composite.
      * @param existingConnections List of connections contained in the viewer.
      */
     private void createAddButton(Composite parent,
             final IObservableList existingConnections) {
         Button addButton = new Button(parent, SWT.NONE);
         BUTTON_DATA_FACTORY.applyTo(addButton);
         addButton.setText(
                 Messages.DatabaseConnectionPreferencePageAddButtonLabel);
         addButton.addSelectionListener(new SelectionAdapter() {
             @Override
             public void widgetSelected(SelectionEvent event) {
                 DatabaseConnectionDialog databaseConnectionWizard = 
                     new DatabaseConnectionDialog();
 
                 if (showDialog(databaseConnectionWizard, event.display) 
                         == Window.OK) {
                     existingConnections.add(
                             databaseConnectionWizard.getEditedConnection());
                 }
             }
         });
     }
 
     @Override
     public boolean performOk() {
         getPreferenceStore().setValue(
             DatabaseConnectionConverter.PREF_DATABASE_CONNECTIONS, 
             DatabaseConnectionConverter.convert(
                     (DatabaseConnection[])m_connectionList.toArray(
                             new DatabaseConnection[m_connectionList.size()])));
         return super.performOk();
     }
 
    @Override
    protected void performDefaults() {
        getPreferenceStore().setToDefault(
                DatabaseConnectionConverter.PREF_DATABASE_CONNECTIONS);
        m_connectionList.clear();
        m_connectionList.addAll(parsePreferences(getPreferenceStore()));
        super.performDefaults();
    }
    
     /**
      * Parses the configured database connections from the information 
      * contained in the given preference store.
      * 
      * @param prefStore The preference store that contains the database 
      *                  connection information.
      * @return the list of configured database connections found in the given
      *         preference store.
      */
    private static List<DatabaseConnection> parsePreferences(
             IPreferenceStore prefStore) {
 
        return DatabaseConnectionConverter.convert(prefStore.getString(
                DatabaseConnectionConverter.PREF_DATABASE_CONNECTIONS));
     }
 
 }
