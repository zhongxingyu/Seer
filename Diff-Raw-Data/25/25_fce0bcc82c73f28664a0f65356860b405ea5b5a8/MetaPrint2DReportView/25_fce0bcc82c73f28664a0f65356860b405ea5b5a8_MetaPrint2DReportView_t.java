 /*******************************************************************************
  * Copyright (c) 2009 Ola Spjuth.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  * 
  * Contributors:
  *     Ola Spjuth - initial API and implementation
  ******************************************************************************/
 package net.bioclipse.metaprint2d.ui.views;
 
import net.bioclipse.cdk.jchempaint.editor.JChemPaintEditor;
import net.bioclipse.cdk.ui.sdfeditor.editor.MultiPageMoleculesEditorPart;
 import net.bioclipse.metaprint2d.ui.Activator;
 import net.bioclipse.metaprint2d.ui.business.IMetaPrint2DManager;
 import net.bioclipse.metaprint2d.ui.model.MetaPrint2DCalculation;
 
 import org.eclipse.jface.dialogs.MessageDialog;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.layout.GridLayout;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Widget;
 import org.eclipse.ui.IEditorPart;
 import org.eclipse.ui.IPartListener;
 import org.eclipse.ui.IPartListener2;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.IWorkbenchPartReference;
 import org.eclipse.ui.PlatformUI;
 import org.eclipse.ui.forms.events.HyperlinkAdapter;
 import org.eclipse.ui.forms.events.HyperlinkEvent;
 import org.eclipse.ui.forms.widgets.AbstractHyperlink;
 import org.eclipse.ui.forms.widgets.FormToolkit;
 import org.eclipse.ui.forms.widgets.Hyperlink;
 import org.eclipse.ui.forms.widgets.ScrolledForm;
 import org.eclipse.ui.internal.WorkbenchPartReference;
 import org.eclipse.ui.part.*;
 
 /**
  * 
  * @author ola
  *
  */
 public class MetaPrint2DReportView extends ViewPart implements IPartListener2{
 
 	public static final String VIEW_ID = "net.bioclipse.metaprint2d.ui.views.MetaPrint2DReportView";
 
 	private FormToolkit toolkit;
 	private ScrolledForm form;
 
 	private Label lblTime;
 
 	private Label lblDB;
 
 	private Label lblOperator;
 
 	private IMetaPrint2DManager m2d;
 
 	private Label lblStatus;
 	/**
 	 * The constructor.
 	 */
 	public MetaPrint2DReportView() {
 		m2d=Activator.getDefault().getMetaPrint2DManager();
 	}
 	/**
 	 * This is a callback that will allow us to create the viewer and
 	 * initialize it.
 	 */
 	public void createPartControl(Composite parent) {
 		toolkit = new FormToolkit(parent.getDisplay());
 		form = toolkit.createScrolledForm(parent);
 		form.setText("Last MetaPrint2D run");
 
 		GridLayout layout = new GridLayout();
 		form.getBody().setLayout(layout);
 
 		layout.numColumns = 2;
 		GridData gdl = new GridData();
 		gdl.widthHint=100;
 		GridData gdr = new GridData(GridData.GRAB_HORIZONTAL);
 		gdr.widthHint=200;
 
 		Label label4 = toolkit.createLabel(form.getBody(), "Status", SWT.NULL);
 		label4.setLayoutData(gdl);
 		lblStatus = toolkit.createLabel(form.getBody(), "Not run", SWT.NULL);
 		lblStatus.setLayoutData(gdr);
 
 		Label label3 = toolkit.createLabel(form.getBody(), "Database", SWT.NULL);
 		label3.setLayoutData(gdl);
 		lblDB = toolkit.createLabel(form.getBody(), "", SWT.NULL);
 		lblDB.setLayoutData(gdr);
 
 		Label label2 = toolkit.createLabel(form.getBody(), "Operator", SWT.NULL);
 		label2.setLayoutData(gdl);
 		lblOperator = toolkit.createLabel(form.getBody(), "", SWT.NULL);
 		lblOperator.setLayoutData(gdr);
 
 		Label label = toolkit.createLabel(form.getBody(), "Calculation time", SWT.NULL);
 		label.setLayoutData(gdl);
 		lblTime = toolkit.createLabel(form.getBody(), "", SWT.NULL);
 		lblTime.setLayoutData(gdr);
 
 		refresh();
 
 	}
 	/**
 	 * Passing the focus request to the form.
 	 */
 	public void setFocus() {
 		form.setFocus();
 	}
 	/**
 	 * Disposes the toolkit
 	 */
 	public void dispose() {
 		toolkit.dispose();
 		super.dispose();
 	}
 
 	private void resreshValues(MetaPrint2DCalculation m2calc) {
 		// TODO Auto-generated method stub
 
 	}
 
 
 	/*========================
 	 * For PART listener below
 	 ========================*/
 
 
 	private void handlePartChange(IWorkbenchPartReference partRef) {
 
 		IWorkbenchPart part=partRef.getPart(false);
 
 		if (part==null) return;
 		if (!(part instanceof IEditorPart))return;
 
 		//OK, so it is an editor
 		IEditorPart editor = (IEditorPart) part;
 
 		visitEditor(editor);
 	}
 
 	
 	private void visitEditor(IEditorPart part) {
 	
 		//Look up in manager the m2dcalculation
 		MetaPrint2DCalculation m2calc =null;
		if ( part instanceof MultiPageMoleculesEditorPart ) {
		    MultiPageMoleculesEditorPart jcpmulti = (MultiPageMoleculesEditorPart) part;
        if (jcpmulti.isJCPVisible()){
            Object obj = jcpmulti.getAdapter(JChemPaintEditor.class);
            if (obj!= null){
                part = (JChemPaintEditor)obj;
            }
        }
    }
 		if (m2d.getCalculationMap().containsKey(part)){
 			m2calc = m2d.getCalculationMap().get(part);
 		}else{
 			//See what active editor is
 			IEditorPart ePart=PlatformUI.getWorkbench().
 			getActiveWorkbenchWindow().getActivePage().getActiveEditor();
 			if (m2d.getCalculationMap().containsKey(ePart)){
 				m2calc = m2d.getCalculationMap().get(ePart);
 			}
 		}
 		if (m2calc!=null)
 			refreshValues(m2calc);
 
 	}
 
 
 	private void refreshValues(final MetaPrint2DCalculation m2calc) {
 		lblTime.setText(m2calc.getCalculationTime());
 		lblOperator.setText(m2calc.getOperator());
 		lblDB.setText(m2calc.getDatabase());
 		lblStatus.setText(m2calc.getStatus());
 //		if (m2calc.getStatus().equalsIgnoreCase("ok")){
 //			//Create a simple label
 //			lblStatus=toolkit.createLabel(form.getBody(), m2calc.getStatus());
 //			form.update();
 //		}else{
 //			//Add hyperlink to review what went wrong
 //			lblStatus = toolkit.createHyperlink(form.getBody(), 
 //					"Error", SWT.WRAP);
 //			((AbstractHyperlink) lblStatus).addHyperlinkListener(new HyperlinkAdapter() {
 //				public void linkActivated(HyperlinkEvent e) {
 //					MessageDialog.openError(getSite().getShell(), "MetaPrint2D error", m2calc.getStatus());
 //				}
 //			});
 //
 //		}
 	}
 	
 	public void refresh(){
	    if (PlatformUI.getWorkbench()==null) return;
      if (PlatformUI.getWorkbench().getActiveWorkbenchWindow()==null) return;
      if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
              ==null) return;
      if (PlatformUI.getWorkbench().getActiveWorkbenchWindow().getActivePage()
              .getActiveEditor()==null) return;
      IEditorPart ePart=PlatformUI.getWorkbench().
      getActiveWorkbenchWindow().getActivePage().getActiveEditor();
      visitEditor(ePart);
 	}
 
 	public void partActivated(IWorkbenchPartReference partRef) {
 		handlePartChange(partRef);
 	}
 	public void partBroughtToTop(IWorkbenchPartReference partRef) {
 		handlePartChange(partRef);
 	}
 	public void partClosed(IWorkbenchPartReference partRef) {
 		// TODO Auto-generated method stub
 
 	}
 	public void partDeactivated(IWorkbenchPartReference partRef) {
 		// TODO Auto-generated method stub
 
 	}
 	public void partHidden(IWorkbenchPartReference partRef) {
 		// TODO Auto-generated method stub
 
 	}
 	public void partInputChanged(IWorkbenchPartReference partRef) {
 		// TODO Auto-generated method stub
 
 	}
 	public void partOpened(IWorkbenchPartReference partRef) {
 		handlePartChange(partRef);
 
 	}
 	public void partVisible(IWorkbenchPartReference partRef) {
 		handlePartChange(partRef);
 
 	}
 
 }
 
