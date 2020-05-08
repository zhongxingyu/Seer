 package net.ostis.confman.ui.common.component.conftree;
 
 import java.util.List;
 
 import net.ostis.confman.services.common.model.Conference;
 import net.ostis.confman.services.common.model.Report;
 import net.ostis.confman.services.common.model.Section;
 
 import org.eclipse.jface.viewers.ITreeContentProvider;
 import org.eclipse.jface.viewers.Viewer;
 
 public class ConfTreeContentProvider implements ITreeContentProvider {
 
     private List<Conference> model;
 
     @Override
     public void dispose() {
 
     }
 
    @SuppressWarnings("unchecked")
     @Override
     public void inputChanged(final Viewer viewer, final Object oldInput,
             final Object newInput) {
 
         this.model = (List<Conference>) newInput;
     }
 
     @Override
     public Object[] getElements(final Object inputElement) {
 
         return this.model.toArray();
     }
 
     @Override
     public Object[] getChildren(final Object parentElement) {
 
         if (parentElement instanceof Conference) {
             final Conference conference = (Conference) parentElement;
             return conference.getSections().toArray();
         }
         if (parentElement instanceof Section) {
             final Section section = (Section) parentElement;
             return section.getReports().toArray();
         }
         return null;
     }
 
     @Override
     public Object getParent(final Object element) {
 
         return null;
     }
 
     @Override
     public boolean hasChildren(final Object element) {
 
         if (element instanceof Report) {
             return false;
         }
         return true;
     }
 }
