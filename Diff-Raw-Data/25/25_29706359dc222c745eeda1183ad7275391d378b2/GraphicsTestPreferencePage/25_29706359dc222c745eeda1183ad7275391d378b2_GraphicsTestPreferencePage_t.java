 /*******************************************************************************
  * <copyright>
  *
  * Copyright (c) 2005, 2010 SAP AG.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  * Contributors:
  *    SAP AG - initial API, implementation and documentation
  *
  * </copyright>
  *
  *******************************************************************************/
 package org.eclipse.graphiti.examples.common.preference;
 
 import org.eclipse.core.runtime.preferences.InstanceScope;
 import org.eclipse.draw2d.ColorConstants;
 import org.eclipse.graphiti.examples.common.Messages;
 import org.eclipse.graphiti.internal.GraphitiPlugin;
 import org.eclipse.graphiti.internal.pref.GFPreferences;
import org.eclipse.graphiti.internal.util.AbstractTracer;
 import org.eclipse.jface.preference.BooleanFieldEditor;
 import org.eclipse.jface.preference.FieldEditorPreferencePage;
 import org.eclipse.jface.preference.IntegerFieldEditor;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.IWorkbench;
 import org.eclipse.ui.IWorkbenchPreferencePage;
 import org.eclipse.ui.preferences.ScopedPreferenceStore;
 
 /**
  * The workbench-preference-page of this Plugin.
  */
 @SuppressWarnings("restriction")
 // The Test preference page is allowed to use the preference settings
 // defined and used in the framework
 public class GraphicsTestPreferencePage extends FieldEditorPreferencePage implements IWorkbenchPreferencePage {
 
 	/**
 	 * Creates and returns the text editor preference page.
 	 */
 	public GraphicsTestPreferencePage() {
 		super(GRID);
 		setDescription(Messages.GraphicsTestPreferencePage_PageDescription);
 		setPreferenceStore(new ScopedPreferenceStore(new InstanceScope(), GraphitiPlugin.PLUGIN_ID));
 	}
 
 	@Override
 	public void createFieldEditors() {
 
 		addLabel(""); //$NON-NLS-1$
 		addLabel(Messages.GraphicsTestPreferencePage_VisualPlayingTitle);
 		addField(new IntegerFieldEditor(GFPreferences.VISUAL_STATE_RENDERING,
 				Messages.GraphicsTestPreferencePage_VisualStateRenderingField, getFieldEditorParent()));
 		addField(new IntegerFieldEditor(GFPreferences.CONTEXT_BUTTON_PAD_DECLARATION,
 				Messages.GraphicsTestPreferencePage_ContextButtonPadField, getFieldEditorParent()));
 		addField(new IntegerFieldEditor(GFPreferences.ZOOM_ANIMATION_STEPS, Messages.GraphicsTestPreferencePage_AnimationStepsField,
 				getFieldEditorParent()));
 		addField(new BooleanFieldEditor(GFPreferences.PROGRESS_DIALOG_ACTIVE, Messages.GraphicsTestPreferencePage_ProgressDialogField,
 				getFieldEditorParent()));
 		addField(new IntegerFieldEditor(GFPreferences.POLYLINE_ROUNDING, Messages.GraphicsTestPreferencePage_PolylineRoundingField,
 				getFieldEditorParent()));
 
 		addLabel(""); //$NON-NLS-1$
 		addLabel(Messages.GraphicsTestPreferencePage_DebuggingTitle);
 		addField(new BooleanFieldEditor(GFPreferences.DEBUG_ACTIONS_ACTIVE, Messages.GraphicsTestPreferencePage_DebugActionsField,
 				getFieldEditorParent()));
 		addField(new BooleanFieldEditor(GFPreferences.CPU_PROFILING_TRACE_ACTIVE,
 				Messages.GraphicsTestPreferencePage_CpuDurationField, getFieldEditorParent()));
 		addField(new BooleanFieldEditor(GFPreferences.INVISIBLE_RECTANGLES_SHOWN, Messages.GraphicsTestPreferencePage_InvisibleRectangleField,
 				getFieldEditorParent()));
 		addField(new BooleanFieldEditor(GFPreferences.GENERIC_OUTLINE_ACTIVE, Messages.GraphicsTestPreferencePage_GenericOutlineField, getFieldEditorParent()));
 		addField(new BooleanFieldEditor(GFPreferences.GENERIC_PROPERTY_SHEET_ACTIVE, Messages.GraphicsTestPreferencePage_GenericPropertyField, getFieldEditorParent()));
 
 		addLabel(""); //$NON-NLS-1$
 		addLabel(Messages.GraphicsTestPreferencePage_InternalTitle);
 		addField(new BooleanFieldEditor(GFPreferences.RECURSIVE_CHECK_FOR_UPDATE_ACTIVE, Messages.GraphicsTestPreferencePage_RecursiveUpdateCheckField,
 				getFieldEditorParent()));
		
		addLabel(""); //$NON-NLS-1$
		addLabel(Messages.GraphicsTestPreferencePage_GraphitiTracingTitle);
		addField(new BooleanFieldEditor(GFPreferences.INFO_LEVEL_TRACING_ACTIVE, Messages.GraphicsTestPreferencePage_ActivateInfoLevelField, getFieldEditorParent()) {
			@Override
			protected void valueChanged(boolean oldValue, boolean newValue) {
				AbstractTracer.setInfoLogging(newValue);
			}
		});
		addField(new BooleanFieldEditor(GFPreferences.DEBUG__LEVEL_TRACING_ACTIVE, Messages.GraphicsTestPreferencePage_ActivateDebugLevelField, getFieldEditorParent()) {
			@Override
			protected void valueChanged(boolean oldValue, boolean newValue) {
				AbstractTracer.setDebugLogging(newValue);
			}
		});
 	}
 
 	private void addLabel(String message) {
 		Label label = new Label(getFieldEditorParent(), SWT.None);
 		GridData data = new GridData(SWT.BEGINNING, SWT.BEGINNING, true, false, 2, 1);
 		label.setLayoutData(data);
 		label.setForeground(ColorConstants.darkGreen);
 		if (message != null)
 			label.setText(message);
 	}
 
 	@Override
 	public void init(IWorkbench workbench) {
 		// nothing to do
 	}
 
 }
