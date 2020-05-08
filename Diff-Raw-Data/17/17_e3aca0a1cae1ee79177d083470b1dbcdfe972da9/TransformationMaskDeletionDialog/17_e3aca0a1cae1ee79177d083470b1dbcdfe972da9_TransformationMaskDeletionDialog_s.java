 /**
  * Optimus, framework for Model Transformation
  *
  * Copyright (C) 2013 Worldline or third-party contributors as
  * indicated by the @author tags or express copyright attribution
  * statements applied by the authors.
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301 USA
  */
 package net.atos.optimus.m2m.engine.ui.prefs.dialog;
 
 import net.atos.optimus.common.tools.swt.FormDataBuilder;
 import net.atos.optimus.m2m.engine.core.masks.TransformationMaskDataSourceManager;
 import net.atos.optimus.m2m.engine.masks.EditableTransformationMaskReference;
 
 import org.eclipse.jface.dialogs.Dialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.swt.layout.FormLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 
 /**
  * Dialog window used to delete a user transformation mask
  * 
  * @author tnachtergaele <nachtergaele.thomas@gmail.com>
  * 
  *
  */
 
 public class TransformationMaskDeletionDialog extends Dialog {
 
 	/** The name of the mask to delete */
 	protected EditableTransformationMaskReference transformationMaskReference;
 
 	/**
 	 * Constructor
 	 * 
 	 * @param parentShell
 	 * @param transformationMaskReference
 	 *            the transformation mask reference to delete.
 	 */
 	public TransformationMaskDeletionDialog(Shell parentShell,
 			EditableTransformationMaskReference transformationMaskReference) {
 		super(parentShell);
 		this.setShellStyle(getShellStyle() | SWT.RESIZE);
 		this.transformationMaskReference = transformationMaskReference;
 	}
 
 	@Override
 	protected Control createDialogArea(Composite parent) {
 		Composite mainContainer = (Composite) super.createDialogArea(parent);
 		mainContainer.setLayout(new FormLayout());
 
 		Label deletionLabel = new Label(mainContainer, SWT.NONE);
 		deletionLabel.setText(TransformationMasksDialogMessages.DELETION_MESSAGE
 				.message(this.transformationMaskReference.getName()));
 
 		Label warningImage = new Label(mainContainer, SWT.ICON_WARNING);
 		warningImage.setImage(Display.getDefault().getSystemImage(SWT.ICON_WARNING));
 
 		Label warningPreferredMask = new Label(mainContainer, SWT.ICON_WARNING);
 		warningPreferredMask.setText(TransformationMasksDialogMessages.PREFERRED_MASK_WARNING
 				.message(TransformationMaskDataSourceManager.DEFAULT_MASK_NAME));
 
 		FormDataBuilder.on(deletionLabel).top().horizontal();
 		FormDataBuilder.on(warningImage).top(deletionLabel).left().right(warningPreferredMask);
 		FormDataBuilder.on(warningPreferredMask).top(deletionLabel).right();
 
 		// Show the warning only if necessary
 		if (!this.transformationMaskReference.equals(TransformationMaskDataSourceManager.INSTANCE
 				.getPreferredTransformationMask())) {
 			warningImage.setVisible(false);
 			warningPreferredMask.setVisible(false);
 		}
 
 		return mainContainer;
 	}
 
 	@Override
 	protected void okPressed() {
 		this.transformationMaskReference.suppressTransformationMaskReference();
 		super.okPressed();
 	}
 
 	@Override
 	protected void configureShell(Shell newShell) {
 		super.configureShell(newShell);
 		newShell.setText(TransformationMasksDialogMessages.DELETION_TITLE.message());
 	}
 
 	@Override
 	protected Point getInitialSize() {
 		return new Point(400, 150);
 	}
 
 }
