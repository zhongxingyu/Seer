 /**
  * 
  */
 package hu.modembed.model.editor.properties;
 
 import hu.modembed.model.editor.IPropertyEditor;
 
 import java.util.Collection;
 import java.util.Collections;
 
 import org.eclipse.core.databinding.DataBindingContext;
 import org.eclipse.core.databinding.observable.value.IObservableValue;
 import org.eclipse.emf.databinding.edit.EMFEditObservables;
 import org.eclipse.emf.ecore.EAttribute;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.jface.databinding.swt.WidgetProperties;
 import org.eclipse.swt.SWT;
 import org.eclipse.swt.layout.GridData;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 import org.eclipse.swt.widgets.Label;
 import org.eclipse.swt.widgets.Text;
 
 /**
  * @author balage
  *
  */
 public class EAttributeEditor implements IPropertyEditor {
 
 	private final EAttribute attrib;
 	
 	public EAttributeEditor(EAttribute attrib) {
 		this.attrib = attrib;
 	}
 	
 	/* (non-Javadoc)
 	 * @see hu.modembed.model.editor.IPropertyEditor#createControls(org.eclipse.swt.widgets.Composite, org.eclipse.emf.ecore.EObject, org.eclipse.emf.edit.domain.EditingDomain)
 	 */
 	@Override
 	public Control[] createControls(Composite parent, final EObject eobject,
 			final EditingDomain edomain) {
 		
 		Label l = new Label(parent, SWT.NONE);
 		l.setText(attrib.getName()+": ");
 		final Text text = new Text(parent, SWT.BORDER);
 		l.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
 		text.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 2, 1));
 		text.setText(eobject.eGet(attrib)+"");
 		
 		DataBindingContext dbc = new DataBindingContext();
 		IObservableValue modelvalue = EMFEditObservables.observeValue(edomain, eobject, attrib);
 		
		dbc.bindValue(WidgetProperties.text(SWT.Modify).observe(text), modelvalue);
 		
 		return new Control[]{l, text};
 	}
 
 	/* (non-Javadoc)
 	 * @see hu.modembed.model.editor.IPropertyEditor#getChildren(org.eclipse.emf.ecore.EObject)
 	 */
 	@Override
 	public Collection<IPropertyEditor> getChildren(EObject eobject) {
 		return Collections.emptyList();
 	}
 
 }
