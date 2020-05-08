 /*-
  * Copyright © 2012 Diamond Light Source Ltd.
  *
  * This file is part of GDA.
  *
  * GDA is free software: you can redistribute it and/or modify it under the
  * terms of the GNU General Public License version 3 as published by the Free
  * Software Foundation.
  *
  * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
  * details.
  *
  * You should have received a copy of the GNU General Public License along
  * with GDA. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package gda.rcp.views;
 
 import gda.device.DeviceException;
 import gda.rcp.GDAClientActivator;
 
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import uk.ac.gda.ui.viewer.RotationViewer;
 
 public class StageCompositeFactory implements CompositeFactory {
 	private static final Logger logger = LoggerFactory.getLogger(StageCompositeFactory.class);
 	StageCompositeDefinition[] stageCompositeDefinitions;
 
 	public StageCompositeDefinition[] getStageCompositeDefinitions() {
 		return stageCompositeDefinitions;
 	}
 
 	public void setStageCompositeDefinitions(StageCompositeDefinition[] stageCompositeDefinitions) {
 		this.stageCompositeDefinitions = stageCompositeDefinitions;
 	}
 
 	String label;
	private int labelWidth;
 
 	public String getLabel() {
 		return label;
 	}
 
 	public void setLabel(String label) {
 		this.label = label;
 	}
 
 	public int getLabelWidth() {
 		return labelWidth;
 	}
 
 	public void setLabelWidth(int labelWidth) {
 		this.labelWidth = labelWidth;
 	}
 
 	public Control getTabControl(Composite parent) {
 		Composite cmp;
 		if (label != null) {
 			Group translationGroup = new Group(parent, SWT.SHADOW_NONE);
 			translationGroup.setText(label);
 			cmp = translationGroup;
 		} else {
 			cmp = new Composite(parent, SWT.NONE);
 		}
 		GridDataFactory.fillDefaults().applyTo(cmp);
 		GridLayoutFactory.fillDefaults().margins(1, 1).spacing(1, 1).applyTo(cmp);
 
 		Composite c1 = new Composite(cmp, SWT.NONE);
 		GridLayoutFactory.swtDefaults().margins(1,1).spacing(2,2).numColumns(2).applyTo(c1);
 		Label label2 = new Label(c1,SWT.NONE);
 		label2.setText("Stop all motors on this stage");
 		GridDataFactory.swtDefaults().applyTo(label2);
 		Button button = new Button(c1, SWT.PUSH);
 		button.setImage(GDAClientActivator.getImageDescriptor("icons/stop.png").createImage());
 		button.setToolTipText("Stop all the motors on this stage");
 		GridDataFactory.swtDefaults().applyTo(button);
 		button.addSelectionListener(new SelectionAdapter() {
 
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				super.widgetSelected(e);
 				for (StageCompositeDefinition s : stageCompositeDefinitions) {
 					try {
 						s.scannable.stop();
 					} catch (DeviceException e1) {
 						logger.error("Error stopping " + s.scannable.getName(), e1);
 					}
 				}
 			}
 
 		});
 		
 		GridLayoutFactory rotationGroupLayoutFactory = GridLayoutFactory.swtDefaults().numColumns(3).margins(0,0).spacing(0,0);
 		GridLayoutFactory layoutFactory = GridLayoutFactory.swtDefaults().numColumns(3).margins(0,0).spacing(0,0);
		GridDataFactory dataFactory = GridDataFactory.swtDefaults().hint(labelWidth, SWT.DEFAULT);
 
 
 		Label sep = new Label(cmp, SWT.SEPARATOR | SWT.HORIZONTAL);
 		GridDataFactory.fillDefaults().grab(true,false).applyTo(sep);
 		for (StageCompositeDefinition s : stageCompositeDefinitions) {
 			RotationViewer rotViewer = new RotationViewer(s.scannable, s.getLabel() != null ? s.getLabel()
 					: s.scannable.getName(), s.isResetToZero());
 			rotViewer.configureStandardStep(s.stepSize);
 			rotViewer.setNudgeSizeBoxDecimalPlaces(s.decimalPlaces);
 			if (s.isUseSteps()) {
 				rotViewer.configureFixedStepButtons(s.smallStep, s.bigStep);
 			}
 			rotViewer.createControls(cmp, s.isSingleLineNudge() ? SWT.SINGLE : SWT.NONE, s.isSingleLine(),
 					rotationGroupLayoutFactory.create(), layoutFactory.create(),
					dataFactory.create());
 			Label sep1 = new Label(cmp, SWT.SEPARATOR | SWT.HORIZONTAL);
 			GridDataFactory.fillDefaults().grab(true,false).applyTo(sep1);
 		}
 
 		return cmp;
 	}
 
 	@Override
 	public Composite createComposite(Composite parent, int style, IWorkbenchPartSite iWorkbenchPartSite) {
 		return (Composite) getTabControl(parent);
 	}
 
 }
