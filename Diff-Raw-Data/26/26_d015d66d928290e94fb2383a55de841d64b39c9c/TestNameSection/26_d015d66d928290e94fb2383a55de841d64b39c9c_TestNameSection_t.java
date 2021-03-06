 /*
  * Created on 14.may.2006
  * 
  * This software is licensed under the terms of the GNU GENERAL PUBLIC LICENSE
  * Version 2, which can be found at http://www.gnu.org/copyleft/gpl.html
  *
  */
 package org.cubictest.ui.sections;
 
 import java.beans.PropertyChangeEvent;
 import java.beans.PropertyChangeListener;
 
 import org.cubictest.model.Test;
 import org.cubictest.ui.gef.command.ChangeTestNameCommand;
 import org.cubictest.ui.gef.controller.TestEditPart;
 import org.cubictest.ui.gef.interfaces.exported.ITestEditor;
 import org.eclipse.core.runtime.Assert;
 import org.eclipse.jface.viewers.ISelection;
 import org.eclipse.jface.viewers.IStructuredSelection;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.custom.CLabel;
 import org.eclipse.swt.events.FocusEvent;
 import org.eclipse.swt.events.FocusListener;
 import org.eclipse.swt.layout.FormAttachment;
 import org.eclipse.swt.layout.FormData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Text;
 import org.eclipse.ui.IWorkbenchPart;
 import org.eclipse.ui.views.properties.tabbed.AbstractPropertySection;
 import org.eclipse.ui.views.properties.tabbed.ITabbedPropertyConstants;
 import org.eclipse.ui.views.properties.tabbed.TabbedPropertySheetPage;
 
 
 public class TestNameSection extends AbstractPropertySection implements PropertyChangeListener {
 
 	private Text labelText;
 	private Test test;
 
 	private FocusListener listener = new FocusListener(){
 		public void focusLost(FocusEvent e) {
 			ChangeTestNameCommand cmd = new ChangeTestNameCommand();
 			cmd.setTest(test);
 			cmd.setOldName(test.getName());
 			cmd.setNewName(labelText.getText());
 			ITestEditor editor = (ITestEditor) getPart();
 			editor.getCommandStack().execute(cmd);
 		}
 		public void focusGained(FocusEvent e) {}
 	};
 		
 	@Override
 	public void createControls(Composite parent, TabbedPropertySheetPage aTabbedPropertySheetPage) {
 		super.createControls(parent, aTabbedPropertySheetPage);
 		Composite composite = getWidgetFactory().createFlatFormComposite(parent);
 		FormData data;
 
 		labelText = getWidgetFactory().createText(composite, ""); 
 		data = new FormData();
		data.left = new FormAttachment(0, STANDARD_LABEL_WIDTH + 10);
 		data.right = new FormAttachment(100, 0);
 		data.top = new FormAttachment(0, ITabbedPropertyConstants.VSPACE);
 		labelText.setLayoutData(data);
 		labelText.addFocusListener(listener);
 
 		CLabel labelLabel = getWidgetFactory().createCLabel(composite, "Name:");
 		data = new FormData();
 		data.left = new FormAttachment(0, 0);
 		data.right = new FormAttachment(labelText, -ITabbedPropertyConstants.HSPACE);
 		data.top = new FormAttachment(labelText, 0, SWT.CENTER);
 		labelLabel.setLayoutData(data);
 	}
 	
 	@Override
 	public void setInput(IWorkbenchPart part, ISelection selection) {
 		super.setInput(part, selection);
 		Assert.isTrue(selection instanceof IStructuredSelection);
 		Object input = ((IStructuredSelection) selection).getFirstElement();
 		Assert.isTrue(input instanceof TestEditPart);
 		this.test = (Test) ((TestEditPart) input).getModel();
 	}
 	
 	@Override
 	public void aboutToBeShown() {
 		test.addPropertyChangeListener(this);
 	}
 	
 	@Override
 	public void aboutToBeHidden() {
 		test.removePropertyChangeListener(this);
 	}
 	
 	@Override
 	public void dispose() {
 		super.dispose();
 		if(test != null)
 			test.removePropertyChangeListener(this);
 	}
 	
 	@Override
 	public void refresh() {
 		super.refresh();
 		labelText.removeFocusListener(listener);
 		labelText.setText(test.getName());
 		labelText.addFocusListener(listener);
 	}
 
 	public void propertyChange(PropertyChangeEvent evt) {
 		refresh();
 	}
 
 }
