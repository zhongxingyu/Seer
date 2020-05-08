 /*
  * ====================================================================
  *
  * Frame2 Open Source License
  *
  * Copyright (c) 2004 Megatome Technologies.  All rights
  * reserved.
  *
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions
  * are met:
  *
  * 1. Redistributions of source code must retain the above copyright
  *    notice, this list of conditions and the following disclaimer.
  *
  * 2. Redistributions in binary form must reproduce the above copyright
  *    notice, this list of conditions and the following disclaimer in
  *    the documentation and/or other materials provided with the
  *    distribution.
  *
  * 3. The end-user documentation included with the redistribution, if
  *    any, must include the following acknowlegement:
  *       "This product includes software developed by
  *        Megatome Technologies."
  *    Alternately, this acknowlegement may appear in the software itself,
  *    if and wherever such third-party acknowlegements normally appear.
  *
  * 4. The names "The Frame2 Project", and "Frame2", 
  *    must not be used to endorse or promote products derived
  *    from this software without prior written permission. For written
  *    permission, please contact iamthechad@sourceforge.net.
  *
  * 5. Products derived from this software may not be called "Frame2"
  *    nor may "Frame2" appear in their names without prior written
  *    permission of Megatome Technologies.
  *
  * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED.  IN NO EVENT SHALL MEGATOME TECHNOLOGIES OR
  * ITS CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT
  * LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF
  * USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY,
  * OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT
  * OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF
  * SUCH DAMAGE.
  * ====================================================================
  */
 /*******************************************************************************
  * Copyright (c) 2000, 2003 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Common Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/cpl-v10.html
  * 
  * Contributors:
  *     IBM Corporation - initial API and implementation
  *******************************************************************************/
 package org.megatome.frame2.wizards;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.wizard.WizardPage;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.TableEditor;
 import org.eclipse.swt.events.ModifyEvent;
 import org.eclipse.swt.events.ModifyListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.graphics.Rectangle;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Event;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Listener;
 import org.eclipse.swt.widgets.Table;
 import org.eclipse.swt.widgets.TableColumn;
 import org.eclipse.swt.widgets.TableItem;
 import org.eclipse.swt.widgets.Text;
 import org.megatome.frame2.model.Forward;
 import org.megatome.frame2.model.Frame2Model;
 import org.megatome.frame2.Frame2Plugin;
 
 public class EventMappingWizardPage3 extends WizardPage {
 	private Combo htmlViewCombo;
 	private Combo xmlViewCombo;
     private Table rolesTable;
     private TableEditor editor;
     private Button addRowButton;
     private Button removeRowButton;
 	private ISelection selection;
     //private IProject rootProject;
     private boolean badModel = false;
     
     private boolean handlersSelected = false;
     
     private final String noneString = Frame2Plugin.getResourceString("EventMappingWizardPage3.noneString"); //$NON-NLS-1$
     private static int roleIndex = 1;
 
 	public EventMappingWizardPage3(ISelection selection) {
 		super(Frame2Plugin.getResourceString("EventMappingWizardPage3.wizardName")); //$NON-NLS-1$
 		setTitle(Frame2Plugin.getResourceString("EventMappingWizardPage3.pageTitle")); //$NON-NLS-1$
 		setDescription(Frame2Plugin.getResourceString("EventMappingWizardPage3.pageDescription")); //$NON-NLS-1$
 		this.selection = selection;
 	}
 
 	public void createControl(Composite parent) {
 		Composite container = new Composite(parent, SWT.NULL);
 		GridLayout layout = new GridLayout();
 		container.setLayout(layout);
 		layout.numColumns = 3;
 		layout.verticalSpacing = 9;
 		Label label = new Label(container, SWT.NULL);
 		label.setText(Frame2Plugin.getResourceString("EventMappingWizardPage3.htmlViewLabel")); //$NON-NLS-1$
 
 		htmlViewCombo = new Combo(container, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
 		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
         gd.horizontalSpan = 2;
         htmlViewCombo.setLayoutData(gd);
 
 		label = new Label(container, SWT.NULL);
 		label.setText(Frame2Plugin.getResourceString("EventMappingWizardPage3.xmlViewLabel")); //$NON-NLS-1$
 
 		xmlViewCombo = new Combo(container, SWT.BORDER | SWT.SINGLE | SWT.READ_ONLY);
 		gd = new GridData(GridData.FILL_HORIZONTAL);
         gd.horizontalSpan = 2;
         xmlViewCombo.setLayoutData(gd);
         
         // Sep
         label = new Label(container, SWT.SEPARATOR | SWT.HORIZONTAL);
         gd = new GridData(GridData.FILL_HORIZONTAL);
         gd.horizontalSpan = 3;
         label.setLayoutData(gd);
 
         rolesTable = new Table(container, SWT.SINGLE | SWT.FULL_SELECTION);
         rolesTable.setHeaderVisible(true);
         gd = new GridData(GridData.FILL_BOTH);
         gd.horizontalSpan = 3;
         rolesTable.setLayoutData(gd);
         
         TableColumn tc = new TableColumn(rolesTable, SWT.NULL);
         tc.setText(Frame2Plugin.getResourceString("EventMappingWizardPage3.userRolesColumn")); //$NON-NLS-1$
         tc.setWidth(200);
         
         editor = new TableEditor(rolesTable);
         editor.horizontalAlignment = SWT.LEFT;
         editor.grabHorizontal = true;
         rolesTable.addListener(SWT.MouseDown, new Listener() {
             public void handleEvent(Event event) {
                 Rectangle clientArea = rolesTable.getClientArea();
                 Point pt = new Point(event.x, event.y);
                 int index = rolesTable.getTopIndex();
                 while (index < rolesTable.getItemCount()) {
                     boolean visible = false;
                     final TableItem item = rolesTable.getItem(index);
                     for (int i = 0; i < rolesTable.getColumnCount(); i++) {
                         Rectangle rect = item.getBounds(i);
                         if (rect.contains(pt)) {
                             final int column = i;
                             final Text text =
                                 new Text(rolesTable, SWT.NONE);
                             Listener textListener = new Listener() {
                                 public void handleEvent(final Event e) {
                                     switch (e.type) {
                                         case SWT.FocusOut :
                                             item.setText(
                                                 column,
                                                 text.getText());
                                             text.dispose();
                                             break;
                                         case SWT.Traverse :
                                             switch (e.detail) {
                                                 case SWT.TRAVERSE_RETURN :
                                                     item.setText(
                                                         column,
                                                         text.getText());
                                                     //FALL THROUGH
                                                 case SWT.TRAVERSE_ESCAPE :
                                                     text.dispose();
                                                     e.doit = false;
                                             }
                                             break;
                                     }
                                     dialogChanged();
                                 }
                             };
                             text.addListener(SWT.FocusOut, textListener);
                             text.addListener(SWT.Traverse, textListener);
                             editor.setEditor(text, item, i);
                             text.setText(item.getText(i));
                             text.selectAll();
                             text.setFocus();
                             removeRowButton.setEnabled(true);
                             return;
                         }
                         if (!visible && rect.intersects(clientArea)) {
                             visible = true;
                         }
                     }
                     if (!visible)
                         return;
                     index++;
                 }
             }
         });
         
         addRowButton = new Button(container, SWT.PUSH);
         addRowButton.setText(Frame2Plugin.getResourceString("EventMappingWizardPage3.addRowCtl")); //$NON-NLS-1$
         gd = new GridData(GridData.HORIZONTAL_ALIGN_BEGINNING);
         gd.horizontalSpan = 2;
         addRowButton.setLayoutData(gd);
         addRowButton.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent e) {
                 TableItem item = new TableItem(rolesTable, SWT.NULL);
                 item.setText(Frame2Plugin.getResourceString("EventMappingWizardPage3.dummyRole") + roleIndex++); //$NON-NLS-1$
             }
         });
         
         removeRowButton = new Button(container, SWT.PUSH);
         removeRowButton.setText(Frame2Plugin.getResourceString("EventMappingWizardPage3.removeRowCtl")); //$NON-NLS-1$
         gd = new GridData(GridData.HORIZONTAL_ALIGN_END);
         gd.horizontalSpan = 1;
         removeRowButton.setLayoutData(gd);
         removeRowButton.addSelectionListener(new SelectionAdapter() {
             public void widgetSelected(SelectionEvent e) {
                 int selIndex = rolesTable.getSelectionIndex();
                 if (selIndex != -1) {
                     rolesTable.remove(selIndex);
                 }
                 
                 removeRowButton.setEnabled(false);
             }
         });
         removeRowButton.setEnabled(false);
                         
 		initialize();
         
         htmlViewCombo.addModifyListener(new ModifyListener() {
             public void modifyText(ModifyEvent e) {
                 dialogChanged();
             }
         });
         
         xmlViewCombo.addModifyListener(new ModifyListener() {
             public void modifyText(ModifyEvent e) {
                 dialogChanged();
             }
         });
         
         setPageComplete(handlersSelected);
         dialogChanged();
 		setControl(container);
 	}
     
 	
 	private void initialize() {
         roleIndex = 1;
         Frame2Model model = ((EventMappingWizard)getWizard()).getFrame2Model();
         
         if (model != null) {
             htmlViewCombo.add(noneString);
             xmlViewCombo.add(noneString);
             
             Forward[] forwards = model.getGlobalForwards();
             for (int i =0; i < forwards.length; i++) {
                 String forwardType = forwards[i].getType();
                 if (forwardType.equals(Frame2Plugin.getResourceString("EventMappingWizardPage3.htmlResource_type"))) { //$NON-NLS-1$
                     htmlViewCombo.add(forwards[i].getName());
                 } else if (forwardType.equals(Frame2Plugin.getResourceString("EventMappingWizardPage3.xmlResource_type")) || //$NON-NLS-1$
                            forwardType.equals(Frame2Plugin.getResourceString("EventMappingWizardPage3.xmlResponse_type"))) { //$NON-NLS-1$
                     xmlViewCombo.add(forwards[i].getName());
                 } else if (forwardType.equals(Frame2Plugin.getResourceString("EventMappingWizardPage3.event_internal_type"))) { //$NON-NLS-1$
                     htmlViewCombo.add(forwards[i].getName());
                     xmlViewCombo.add(forwards[i].getName());
                 }
             }
             
             htmlViewCombo.setText(noneString);
             xmlViewCombo.setText(noneString);
         } else {
             setPageComplete(false);
             badModel = true;
             dialogChanged();
         }
 	}
 	
 	private void dialogChanged() {
         if (badModel) {
             updateStatus(Frame2Plugin.getResourceString("EventMappingWizardPage3.errorConfig")); //$NON-NLS-1$
             return;
         }
         
 		String htmlView = getHTMLView();
         String xmlView = getXMLView();
 
         if ((htmlView.length() == 0) &&
             (xmlView.length() == 0)  && 
             (!handlersSelected)) {
 			updateStatus(Frame2Plugin.getResourceString("EventMappingWizardPage3.errorMissingInformation")); //$NON-NLS-1$
 			return;
 		}
         
 		updateStatus(null);
 	}
 
 	private void updateStatus(String message) {
 		setErrorMessage(message);
 		setPageComplete(message == null);
 	}
 
 	public String getHTMLView() {
         String htmlView = htmlViewCombo.getText();
         if (htmlView.equals(noneString)) {
             return ""; //$NON-NLS-1$
         }
         
 		return htmlView;
 	}
     public String getXMLView() {
         String xmlView = xmlViewCombo.getText();
         if (xmlView.equals(noneString)) {
             return ""; //$NON-NLS-1$
         }
         
         return xmlView;
     }
     public List getSecurityRoles() {
         List roles = new ArrayList();
         
         int roleCount = rolesTable.getItemCount();
         for (int i = 0; i < roleCount; i++) {
             TableItem item = rolesTable.getItem(i);
             roles.add(item.getText());
         }
         
         return roles;
     }
     public void setHandlersSelected(boolean selected) {
         handlersSelected = selected;
         dialogChanged();
     }
 
     public void dispose() {
         super.dispose();
         
         htmlViewCombo.dispose();
         xmlViewCombo.dispose();
        rolesTable.dispose();
         editor.dispose();
         addRowButton.dispose();
         removeRowButton.dispose();
     }
 
 }
