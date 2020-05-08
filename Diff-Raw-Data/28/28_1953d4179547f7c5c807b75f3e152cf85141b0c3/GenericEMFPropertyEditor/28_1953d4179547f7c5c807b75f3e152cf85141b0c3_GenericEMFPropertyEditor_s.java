 /**
  * 
  */
 package hu.modembed.model.editor.properties;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 import org.eclipse.emf.ecore.EClass;
 import org.eclipse.emf.ecore.EObject;
 import org.eclipse.emf.edit.domain.EditingDomain;
 import org.eclipse.swt.widgets.Composite;
 import org.eclipse.swt.widgets.Control;
 
 import hu.modembed.model.editor.IPropertyEditor;
 
 /**
  * @author balage
  *
  */
 public class GenericEMFPropertyEditor implements IPropertyEditor {
 
 	/* (non-Javadoc)
 	 * @see hu.modembed.model.editor.IPropertyEditor#createControls(org.eclipse.swt.widgets.Composite, org.eclipse.emf.ecore.EObject)
 	 */
 	@Override
 	public Control[] createControls(Composite parent, EObject eobject, EditingDomain edomain) {
 		return new Control[0];
 	}
 
 	/* (non-Javadoc)
 	 * @see hu.modembed.model.editor.IPropertyEditor#getChildren()
 	 */
 	@Override
 	public Collection<IPropertyEditor> getChildren(EObject eobject) {
 		List<IPropertyEditor> editors = new ArrayList<IPropertyEditor>();
 		Queue<EClass> q = new LinkedList<EClass>();
 		q.add(eobject.eClass());
 		while(!q.isEmpty()){
 			EClass ec = q.poll();
			q.addAll(ec.getESuperTypes());
			editors.add(new EClassPropertyEditor(ec));
 		}
 		return editors;
 	}
 
 }
