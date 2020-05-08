 /*-
  * Copyright © 2011 Diamond Light Source Ltd.
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
 
 package uk.ac.gda.dls.client.views;
 
 import gda.device.DeviceException;
 import gda.device.EnumPositioner;
 import gda.device.enumpositioner.DummyPositioner;
 import gda.factory.FactoryException;
 import gda.observable.IObserver;
 import gda.rcp.views.CompositeFactory;
 
 import java.util.Arrays;
 
 import org.eclipse.jface.layout.GridDataFactory;
 import org.eclipse.jface.layout.GridLayoutFactory;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.events.SelectionAdapter;
 import org.eclipse.swt.events.SelectionEvent;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Combo;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Display;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Shell;
 import org.eclipse.ui.IWorkbenchPartSite;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.util.StringUtils;
 
 import swing2swt.layout.BorderLayout;
 import uk.ac.gda.common.rcp.util.EclipseWidgetUtils;
 import uk.ac.gda.ui.utils.SWTUtils;
 
 
 public class EnumPositionerCompositeFactory implements CompositeFactory, InitializingBean {
 
 	private String label;
 	private EnumPositioner positioner;
 	private Integer labelWidth;
 	private Integer contentWidth;
 
 	public String getLabel() {
 		return label;
 	}
 
 	public void setLabel(String label) {
 		this.label = label;
 	}
 
 	public EnumPositioner getPositioner() {
 		return positioner;
 	}
 
 	public void setPositioner(EnumPositioner positioner) {
 		this.positioner = positioner;
 	}
 	public Integer getLabelWidth() {
 		return labelWidth;
 	}
 
 	public Integer getContentWidth() {
 		return contentWidth;
 	}
 
 
 	public void setContentWidth(Integer contentWidth) {
 		this.contentWidth = contentWidth;
 	}
 
 
 	public void setLabelWidth(Integer labelWidth) {
 		this.labelWidth = labelWidth;
 	}
 
 
 	
 	@Override
 	public Composite createComposite(Composite parent, int style, IWorkbenchPartSite iWorkbenchPartSite) {
 		return new EnumPositionerComposite(parent, style, iWorkbenchPartSite.getShell().getDisplay(), positioner, label, labelWidth, contentWidth);
 	}
 
 	public static void main(String... args) {
 
 		Display display = new Display();
 		Shell shell = new Shell(display);
 		shell.setLayout(new BorderLayout());
 
 		DummyPositioner dummy = new DummyPositioner();
 		dummy.setName("dummy");
 			try {
 				dummy.configure();
 			} catch (FactoryException e1) {
 				// TODO Auto-generated catch block
 			}
 		
 		dummy.setPositions(new String[]{"position1", "position2", "position3"});
 		try {
 			dummy.moveTo(1);
 		} catch (DeviceException e) {
 			System.out.println("Can not move dummy to position 1");
 		}
 		
 		final EnumPositionerComposite comp = new EnumPositionerComposite(shell, SWT.NONE, display, dummy, "", new Integer(100), new Integer(200));
 		comp.setLayoutData(BorderLayout.NORTH);
 		comp.setVisible(true);
 		shell.pack();
 		shell.setSize(400, 400);
 		SWTUtils.showCenteredShell(shell);
 	}
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		if (positioner == null)
 			throw new IllegalArgumentException("positioner is null");
 
 	}
 }
 
 
 class EnumPositionerComposite extends Composite {
 	private static final Logger logger = LoggerFactory.getLogger(EnumPositionerComposite.class);
 	private Combo pcom;
 	EnumPositioner positioner;
 	IObserver observer;
 	int selectionIndex=-1;
 	String positions[];
 	Integer labelWidth;
 	Integer contentWidth;
 	Display display;
 	private Runnable setComboRunnable;
 	String [] formats;
 
 	EnumPositionerComposite(Composite parent, int style, final Display display, EnumPositioner positioner, String label, Integer labelWidth, Integer contentWidth ) {
 		super(parent, style);
 		this.display = display;
 		this.positioner = positioner;
 		this.labelWidth=labelWidth;
 		this.contentWidth=contentWidth;
 		
 		formats = positioner.getOutputFormat();
 		GridLayoutFactory.fillDefaults().numColumns(2).applyTo(this);
 		GridDataFactory.fillDefaults().applyTo(this);
 //		GridDataFactory.fillDefaults().align(GridData.FILL, SWT.FILL).applyTo(this);
 		
 //		Label lbl = new Label(this, SWT.RIGHT |SWT.WRAP | SWT.BORDER);
 		Label lbl = new Label(this, SWT.RIGHT |SWT.WRAP);
 		lbl.setText(StringUtils.hasLength(label) ? label : positioner.getName());
 		
         GridData labelGridData = new GridData(GridData.HORIZONTAL_ALIGN_END);
 		if(labelWidth != null)
 			labelGridData.widthHint = labelWidth.intValue();
         lbl.setLayoutData(labelGridData);
 		
 		try {
 		positions = this.positioner.getPositions();
 		} catch (DeviceException e) {
			logger.error("Error getting position for " + this.positioner.getName(), e);
 		}
 
 		pcom = new Combo(this, SWT.SINGLE|SWT.BORDER|SWT.CENTER|SWT.READ_ONLY);
 		pcom.setItems(positions);
 		
 //		pcom.setLayoutData(new GridData(GridData.FILL, GridData.CENTER, true, false));
 		GridData textGridData = new GridData(GridData.FILL_HORIZONTAL);
 		textGridData.horizontalAlignment = GridData.HORIZONTAL_ALIGN_BEGINNING;
 		if(contentWidth != null)
 			textGridData.widthHint = contentWidth.intValue();
 		pcom.setLayoutData(textGridData);
 		
 		pcom.addSelectionListener(new SelectionAdapter() {
 		      public void widgetSelected(SelectionEvent e) {
 		    	  valueChanged((Combo)e.widget);
 		        }
 		      });
 
 		setComboRunnable = new Runnable() {
 			@Override
 			public void run() {
 				if(selectionIndex != -1){
 					pcom.select(selectionIndex);
 				}
 				EclipseWidgetUtils.forceLayoutOfTopParent(EnumPositionerComposite.this);
 			}
 		};
 		
 		observer = new IObserver() {
 			@Override
 			public void update(Object source, Object arg) {
 				logger.info("Got the who knows what type event!");
 				displayValue();
 			}
 		};
 		
 		displayValue();
 		
 		positioner.addIObserver(observer);
 	}
 
 	void displayValue() {
 		try {
 			String a=(String) positioner.getPosition();
 			selectionIndex=Arrays.asList(this.positions).indexOf(a);
 		} catch (DeviceException e) {
 			selectionIndex=-1;
 			logger.error("Error getting position for " + positioner.getName(), e);
 		}
 		
 		if(!isDisposed()){
 			display.asyncExec(setComboRunnable);
 		}
 	}
 
 	/**
 	   * To change the positioner position when the input text fields changes.
 	   * 
 	   * @param c
 	   *            the event source
 	   */
 	public void valueChanged(Combo c) {
 //		if (!c.isFocusControl())
 //			return;
 
 		try {
 			int npi=c.getSelectionIndex();
 			this.positioner.asynchronousMoveTo( positions[npi] );
 			logger.info("New value '" + positions[npi] + "' send to " + positioner.getName() + ".");
 			selectionIndex = npi;
 		} catch (NumberFormatException e) {
 			logger.error("Invalid number format: " + c.getText());
 	      } catch (DeviceException e) {
 			logger.error("EnumPositioner device " + positioner.getName() + " move failed", e);
 		}
 	    
 	  }
 	
 	@Override
 	public void dispose() {
 		positioner.deleteIObserver(observer);
 		super.dispose();
 	}
 
 }
