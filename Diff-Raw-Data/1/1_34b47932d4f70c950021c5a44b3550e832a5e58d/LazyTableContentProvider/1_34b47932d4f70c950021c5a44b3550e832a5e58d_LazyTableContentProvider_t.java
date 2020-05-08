 package hu.ppke.itk.swt.demo;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.eclipse.jface.viewers.ILazyContentProvider;
 import org.eclipse.jface.viewers.TableViewer;
 import org.eclipse.jface.viewers.Viewer;
 
 /**
  * @author akitta <akitta@b2international.com>
  *
  */
 public class LazyTableContentProvider implements ILazyContentProvider {
 
 	private List<?> input;
 	private TableViewer viewer;
 	
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.IContentProvider#dispose()
 	 */
 	@Override
 	public void dispose() {
 
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.IContentProvider#inputChanged(org.eclipse.jface.viewers.Viewer, java.lang.Object, java.lang.Object)
 	 */
 	@Override
 	public void inputChanged(Viewer viewer, Object oldInput, Object newInput) {
 		if (viewer instanceof TableViewer && newInput instanceof Collection) {
 			this.viewer = (TableViewer) viewer;
 			input = new ArrayList<Object>((Collection<?>) newInput);
			this.viewer.setItemCount(input == null ? 0 : input.size());
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.eclipse.jface.viewers.ILazyContentProvider#updateElement(int)
 	 */
 	@Override
 	public void updateElement(int index) {
 		viewer.replace(input == null ? null : input.get(index), index);
 	}
 
 }
