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
 
 package gda.simplescan;
 
 import gda.jython.JythonServerFacade;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.derby.impl.store.raw.data.DecryptInputStream;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.core.runtime.jobs.Job;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.KeyAdapter;
 import org.eclipse.swt.events.KeyEvent;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.events.SelectionListener;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Button;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Group;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.wb.swt.SWTResourceManager;
 
 import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
 import uk.ac.gda.richbeans.components.wrappers.ComboWrapperWithGetCombo;
 import org.eclipse.swt.events.SelectionAdapter;
 
 public class PosComposite extends Composite {
 
 	ComboWrapperWithGetCombo scannableName;
 	SimpleScan bean;
 	Text incrementVal;
 	ScaleBox textTo;
 	Button btnIncrement;
 	Button btnDecrement;
 	Label lblReadbackVal;
 	Job updateReadbackJob;
 	String scannable;
 	
 	public PosComposite(Composite parent, int style, Object editingBean) {
 		super(parent, style);
 		GridLayout gridLayout = new GridLayout(1, false);
 		gridLayout.verticalSpacing = 0;
 		gridLayout.marginWidth = 0;
 		gridLayout.marginHeight = 0;
 		gridLayout.horizontalSpacing = 0;
 		setLayout(gridLayout);
 		bean = (SimpleScan) editingBean;
 
 		Group grpPos = new Group(this, SWT.NONE);
 		GridData gd_grpPos = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
 		gd_grpPos.widthHint = 231;
 		grpPos.setLayoutData(gd_grpPos);
 		grpPos.setText("Pos");
 		grpPos.setLayout(new GridLayout(1, false));
 
 		Composite posComposite = new Composite(grpPos, SWT.NONE);
 		GridData gd_posComposite = new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1);
 		gd_posComposite.widthHint = 197;
 		posComposite.setLayoutData(gd_posComposite);
 		posComposite.setLayout(new GridLayout(2, false));
 
 		Label lblScannable = new Label(posComposite, SWT.NONE);
 		lblScannable.setText("Scannable");
 
 		createScannables(posComposite);
 		Composite composite_2 = new Composite(grpPos, SWT.NONE);
 		GridData gd_composite_2 = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
 		gd_composite_2.widthHint = 218;
 		composite_2.setLayoutData(gd_composite_2);
 		composite_2.setLayout(new GridLayout(3, false));
 
 		Label lblTo = new Label(composite_2, SWT.NONE);
 		lblTo.setText("Demand");
 		textTo = new ScaleBox(composite_2, SWT.NONE);
 
 		textTo.getControl().addKeyListener(new KeyAdapter() {
 			@Override
 			public void keyPressed(KeyEvent e) {
 				if (e.keyCode == 13)
 					performPos();
 				else{
 					bean.setScannableName(scannableName.getItem(scannableName.getSelectionIndex()));
 					try {
 						setMotorLimits(bean.getScannableName(), textTo);
 					} catch (Exception e1) {
 					}
 				}
 			}
 		});
 
 		((GridData) textTo.getControl().getLayoutData()).widthHint = 75;
 		textTo.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 		new Label(textTo, SWT.NONE);
 
 		Label lblStatus = new Label(composite_2, SWT.NONE);
 		lblStatus.setLayoutData(new GridData(SWT.CENTER, SWT.CENTER, false, false, 1, 1));
 		lblStatus.setText("Idle");
 
 		Label lblReadback = new Label(composite_2, SWT.NONE);
 		lblReadback.setText("Readback");
 		lblReadbackVal = new Label(composite_2, SWT.NONE);
 		GridData gd_lblReadbackVal = new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1);
 		gd_lblReadbackVal.widthHint = 75;
 		lblReadbackVal.setLayoutData(gd_lblReadbackVal);
 		lblReadbackVal.setText("2.335mm");
 
 		Button btnStop = new Button(composite_2, SWT.NONE);
 		btnStop.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				performStop();
 			}
 		});
 		btnStop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 		btnStop.setText("Stop");
 
 		Label lblIncrement = new Label(composite_2, SWT.NONE);
 		lblIncrement.setText("Increment");
 
 		incrementVal = new Text(composite_2, SWT.BORDER);
 		incrementVal.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false, 1, 1));
 		incrementVal.setText("1");
 
 		Composite composite = new Composite(composite_2, SWT.NONE);
 		GridLayout gl_composite = new GridLayout(2, false);
 		gl_composite.marginHeight = 0;
 		gl_composite.horizontalSpacing = 0;
 		gl_composite.marginWidth = 0;
 		composite.setLayout(gl_composite);
 
 		btnDecrement = new Button(composite, SWT.NONE);
 		btnDecrement.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				performDecrement();
 			}
 		});
 		btnDecrement.setText("-");
 		GridData gd_btnDecrement = new GridData(SWT.FILL, SWT.FILL, false, false, 1, 1);
 		gd_btnDecrement.widthHint = 30;
 		btnDecrement.setLayoutData(gd_btnDecrement);
 		btnDecrement.setFont(SWTResourceManager.getFont("Sans", 12, SWT.BOLD));
 
 		btnIncrement = new Button(composite, SWT.NONE);
 		btnIncrement.addSelectionListener(new SelectionAdapter() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				performIncrement();
 			}
 		});
 		btnIncrement.setText("+");
 		GridData gd_btnIncrement = new GridData(SWT.FILL, SWT.FILL, true, true, 1, 1);
 		gd_btnIncrement.widthHint = 30;
 		btnIncrement.setLayoutData(gd_btnIncrement);
 		btnIncrement.setFont(SWTResourceManager.getFont("Sans", 12, SWT.BOLD));
 
 		updateScannables();
 
 		scannableName.getCombo().addSelectionListener(new SelectionListener() {
 			@Override
 			public void widgetSelected(SelectionEvent e) {
 				try {
 					bean.setScannableName(scannableName.getItem(scannableName.getSelectionIndex()));
 					setMotorLimits(bean.getScannableName(), textTo);
 					updateReadback();
 				} catch (Exception e1) {
 				}
 			}
 			@Override
 			public void widgetDefaultSelected(SelectionEvent e) {
 			}
 		});
 		
 		updateReadbackJob = new Job("updateReadback") {
 			@Override
 			protected IStatus run(IProgressMonitor monitor) {
 				boolean moving = true;
 				while (moving) {
 					String status = JythonServerFacade.getInstance().evaluateCommand(scannable + ".isBusy()");
 					if (status.equals("False"))
 						moving=false;
 						try {
							Thread.sleep(100);
 						} catch (InterruptedException e) {
 						}
 					Display.getDefault().asyncExec(new Runnable() {
 						@Override
 						public void run() {
 							updateReadback();
 						}
 					});
 				}
 				return Status.OK_STATUS;
 			}
 		};
 	}
 
 	private void performPos() {
 		scannable = scannableName.getItem(scannableName.getSelectionIndex());
 		String demand = textTo.getValue().toString();
 		String command = scannable + "(" + demand + ")";
 		JythonServerFacade.getInstance().runCommand(command);
 		updateReadbackJob.schedule();
 	}
 
 	private void performIncrement() {
 		scannable = scannableName.getItem(scannableName.getSelectionIndex());
 		double increment = Double.parseDouble(incrementVal.getText());
 		double scannablePos = Double.parseDouble(JythonServerFacade.getInstance().evaluateCommand(
 				bean.getScannableName() + "()"));
 		double demand = scannablePos + increment;
 		String command = scannable + "(" + demand + ")";
 		JythonServerFacade.getInstance().runCommand(command);
 		updateReadbackJob.schedule();
 	}
 
 	private void performDecrement() {
 		scannable = scannableName.getItem(scannableName.getSelectionIndex());
 		double decrement = Double.parseDouble(incrementVal.getText());
 		double scannablePos = Double.parseDouble(JythonServerFacade.getInstance().evaluateCommand(
 				bean.getScannableName() + "()"));
 		double demand = scannablePos - decrement;
 		String command = scannable + "(" + demand + ")";
 		JythonServerFacade.getInstance().runCommand(command);
 		updateReadbackJob.schedule();
 	}
 	
 	private void performStop(){
 		scannable = scannableName.getItem(scannableName.getSelectionIndex());
 		String command = scannable + ".getMotor().stop()";
 		JythonServerFacade.getInstance().runCommand(command);
 	}
 
 	public void createScannables(Composite comp) {
 		scannableName = new ComboWrapperWithGetCombo(comp, SWT.NONE);
 		scannableName.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
 	}
 
 	public void updateScannables() {
 		List<String> names = new ArrayList<String>(bean.getScannables().size());
 		String[] comboNames = new String[bean.getScannables().size() + 1];
 		comboNames[0] = "";
 		for (int i = 1; i < bean.getScannables().size() + 1; i++) {
 			names.add(bean.getScannables().get(i - 1).getScannableName());
 			comboNames[i] = bean.getScannables().get(i - 1).getScannableName();
 		}
 		scannableName.setItems(comboNames);
 
 		List<ScannableManagerBean> scannables = bean.getScannables();
 		boolean found = false;
 
 		for (int i = 0; i < scannables.size(); i++) {
 			if (scannables.get(i).getScannableName().equals(bean.getScannableName())) {
 				scannableName.select(i + 1);
 				found = true;
 				updateReadback();
 			}
 		}
 		if (!found)
 			scannableName.select(0);
 
 	}
 
 	public void updateReadback() {
 		double scannablePos = Double.parseDouble(JythonServerFacade.getInstance().evaluateCommand(
 				bean.getScannableName() + "()"));
 		lblReadbackVal.setText(String.valueOf(scannablePos));
 	}
 
 	public void setMotorLimits(String motorName, ScaleBox box) throws Exception {
 		String lowerLimit = JythonServerFacade.getInstance().evaluateCommand(motorName + ".getLowerInnerLimit()");
 		String upperLimit = JythonServerFacade.getInstance().evaluateCommand(motorName + ".getUpperInnerLimit()");
 		if (!lowerLimit.equals("None"))
 			box.setMinimum(Double.parseDouble(lowerLimit));
 		else
 			box.setMinimum(-99999);
 		if (!upperLimit.equals("None"))
 			box.setMaximum(Double.parseDouble(upperLimit));
 		else
 			box.setMaximum(99999);
 	}
 
 	public void setBean(SimpleScan bean) {
 		this.bean = bean;
 	}
 }
