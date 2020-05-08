 /*******************************************************************************
  * Copyright (c) 2012 Wind River Systems and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *     Wind River Systems - initial API and implementation
  *******************************************************************************/
 package org.eclipse.tcf.internal.cdt.ui.breakpoints;
 
 import java.util.Map;
 
 import org.eclipse.cdt.debug.core.model.ICBreakpointType;
 import org.eclipse.core.runtime.IAdaptable;
 import org.eclipse.debug.ui.DebugUITools;
 import org.eclipse.debug.ui.contexts.IDebugContextProvider;
 import org.eclipse.jface.dialogs.DialogPage;
 import org.eclipse.jface.preference.FieldEditor;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.DisposeEvent;
 import org.eclipse.swt.events.DisposeListener;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Label;
 import org.eclipse.tcf.debug.ui.ITCFObject;
 import org.eclipse.tcf.protocol.IChannel;
 import org.eclipse.tcf.protocol.IToken;
 import org.eclipse.tcf.services.IBreakpoints;
 import org.eclipse.tcf.util.TCFTask;
 import org.eclipse.ui.IWorkbenchPropertyPage;
 
 public class HardwareFieldEditor extends FieldEditor {
 
     private Composite fParent;
 
     /**
      * The previously selected, or "before", value.
      */
     private boolean wasSelected;
 
     /**
      * The checkbox control, or <code>null</code> if none.
      */
     private Button checkBox;
 
     public HardwareFieldEditor(Composite parent) {
        super(ICBreakpointType.TYPE, "Hardware", parent);
         fParent = parent;
     }
 
     @Override
     public void setPage(DialogPage dialogPage) {
         super.setPage(dialogPage);
         updateEnablement();
     }
 
     @Override
     public void dispose() {
         fParent = null;
         super.dispose();
     }
 
     protected void updateEnablement() {
 
         // enable/disable this feature according to TCF agent capabilities.
         final IChannel channel = getActiveChannel();
         Boolean enabled;
 
         if ((channel == null) || (channel.getState() != IChannel.STATE_OPEN)) {
             enabled = Boolean.FALSE;
         }
         else {
             // determine if "Physical Address" capability is supported and
             // populate the cache.
 
             enabled = checkChannelCapabilities(channel);
         }
         setEnabled(enabled, fParent);
     }
 
     /**
      * determine if the channel is able to manage "Physical Address" capability
      *
      * @param channel
      *            the channel to check
      * @return TRUE if "Physical Address" is supported by the channel, else
      *         false
      */
     protected Boolean checkChannelCapabilities(final IChannel channel) {
         Boolean result = Boolean.FALSE;
 
         result = new TCFTask<Boolean>() {
             public void run() {
                 IBreakpoints service = channel.getRemoteService(IBreakpoints.class);
                 service.getCapabilities(null, new IBreakpoints.DoneGetCapabilities() {
                     public void doneGetCapabilities(IToken token, Exception error, Map<String, Object> capabilities) {
                         done( Boolean.TRUE.equals(capabilities.get(IBreakpoints.CAPABILITY_BREAKPOINT_TYPE)) );
                     }
                 });
             }
         }.getE();
 
         return result;
     }
 
     protected IChannel getActiveChannel() {
         Object debugContext = getDebugContext();
         if (debugContext instanceof ITCFObject) {
             return ((ITCFObject)debugContext).getChannel();
         }
         return null;
     }
 
     protected Object getDebugContext() {
         IWorkbenchPropertyPage page = (IWorkbenchPropertyPage)getPage();
         if (page != null) {
             IAdaptable element = page.getElement();
             IDebugContextProvider provider = (IDebugContextProvider)element.getAdapter(IDebugContextProvider.class);
             if (provider != null) {
                 ISelection selection = provider.getActiveContext();
                 if (selection instanceof IStructuredSelection) {
                     return ((IStructuredSelection) selection).getFirstElement();
                 }
                 return null;
             }
             return DebugUITools.getDebugContext();
         }
         return null;
     }
 
     /*
      * (non-Javadoc) Method declared on FieldEditor.
      */
     protected void adjustForNumColumns(int numColumns) {
         ((GridData)checkBox.getLayoutData()).horizontalSpan = numColumns - 1;
     }
 
     /*
      * (non-Javadoc) Method declared on FieldEditor.
      */
     protected void doFillIntoGrid(Composite parent, int numColumns) {
        getLabelControl(parent);
        numColumns--;
         checkBox = getChangeControl(parent);
         GridData gd = new GridData();
         gd.horizontalSpan = numColumns;
         checkBox.setLayoutData(gd);
     }
 
     /**
      * Returns the control responsible for displaying this field editor's label.
      * This method can be used to set a tooltip for a
      * <code>BooleanFieldEditor</code>. Note that the normal pattern of
      * <code>getLabelControl(parent).setToolTipText(tooltipText)</code> does not
      * work for boolean field editors, as it can lead to duplicate text (see bug
      * 259952).
      *
      * @param parent
      *            the parent composite
      * @return the control responsible for displaying the label
      *
      * @since 3.5
      */
     public Control getDescriptionControl(Composite parent) {
         return getLabelControl(parent);
     }
 
     /*
      * (non-Javadoc) Method declared on FieldEditor. Loads the value from the
      * preference store and sets it to the check box.
      */
     protected void doLoad() {
         if (checkBox != null) {
             int type = getPreferenceStore().getInt(getPreferenceName());
             boolean value = (type & ICBreakpointType.HARDWARE) != 0;
             checkBox.setSelection(value);
             wasSelected = value;
         }
     }
 
     /*
      * (non-Javadoc) Method declared on FieldEditor. Loads the default value
      * from the preference store and sets it to the check box.
      */
     protected void doLoadDefault() {
         if (checkBox != null) {
             int type = getPreferenceStore().getDefaultInt(getPreferenceName());
             boolean value = (type & ICBreakpointType.HARDWARE) != 0;
             checkBox.setSelection(value);
             wasSelected = value;
         }
     }
 
     /*
      * (non-Javadoc) Method declared on FieldEditor.
      */
     protected void doStore() {
         int type = getPreferenceStore().getInt(getPreferenceName());
         boolean selection = checkBox.getSelection();
         if (selection) {
             type = type | ICBreakpointType.HARDWARE;
         } else {
             type = type & ~ICBreakpointType.HARDWARE;
         }
         getPreferenceStore().setValue(getPreferenceName(), type);
     }
 
     /**
      * Returns this field editor's current value.
      *
      * @return the value
      */
     public boolean getBooleanValue() {
         return checkBox.getSelection();
     }
 
     /**
      * Returns the change button for this field editor.
      *
      * @param parent
      *            The Composite to create the receiver in.
      *
      * @return the change button
      */
     protected Button getChangeControl(Composite parent) {
         if (checkBox == null) {
             checkBox = new Button(parent, SWT.CHECK | SWT.LEFT);
             checkBox.setFont(parent.getFont());
             checkBox.addSelectionListener(new SelectionAdapter() {
                 public void widgetSelected(SelectionEvent e) {
                     boolean isSelected = checkBox.getSelection();
                     valueChanged(wasSelected, isSelected);
                     wasSelected = isSelected;
                 }
             });
             checkBox.addDisposeListener(new DisposeListener() {
                 public void widgetDisposed(DisposeEvent event) {
                     checkBox = null;
                 }
             });
         }
         else {
             checkParent(checkBox, parent);
         }
         return checkBox;
     }
 
     /*
      * (non-Javadoc) Method declared on FieldEditor.
      */
     public int getNumberOfControls() {
        return 2;
     }
 
     /*
      * (non-Javadoc) Method declared on FieldEditor.
      */
     public void setFocus() {
         if (checkBox != null) {
             checkBox.setFocus();
         }
     }
 
     /*
      * (non-Javadoc) Method declared on FieldEditor.
      */
     public void setLabelText(String text) {
         super.setLabelText(text);
        Label label = getLabelControl();
        if (label == null && checkBox != null) {
            checkBox.setText(text);
        }
     }
 
     /**
      * Informs this field editor's listener, if it has one, about a change to
      * the value (<code>VALUE</code> property) provided that the old and new
      * values are different.
      *
      * @param oldValue
      *            the old value
      * @param newValue
      *            the new value
      */
     protected void valueChanged(boolean oldValue, boolean newValue) {
         setPresentsDefaultValue(false);
         if (oldValue != newValue) {
             fireStateChanged(VALUE, oldValue, newValue);
         }
     }
 
     /*
      * @see FieldEditor.setEnabled
      */
     public void setEnabled(boolean enabled, Composite parent) {
         super.setEnabled(enabled, parent);
         getChangeControl(parent).setEnabled(enabled);
     }
 }
