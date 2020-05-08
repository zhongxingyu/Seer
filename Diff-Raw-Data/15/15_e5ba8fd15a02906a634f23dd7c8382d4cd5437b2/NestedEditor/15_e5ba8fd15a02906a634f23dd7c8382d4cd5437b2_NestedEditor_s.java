 package org.jboss.tools.drools.reddeer.editor;
 
 import java.util.List;
 
 import org.eclipse.ui.IEditorPart;
import org.jboss.reddeer.eclipse.jface.text.contentassist.ContentAssistant;
 import org.jboss.reddeer.workbench.api.Editor;
 import org.jboss.reddeer.workbench.impl.editor.Marker;
 
 public class NestedEditor implements Editor {
     private IEditorPart editor;
     private Editor parent;
 
     public NestedEditor(MultiPageEditor parent, String title) {
         this.parent = parent;
         editor = parent.getEditorByTitle(title);
     }
 
     protected IEditorPart getEditorPart() {
         return editor;
     }
 
     public String getTitle() {
         return parent.getTitle();
     }
 
     public String getTitleToolTip() {
         return parent.getTitleToolTip();
     }
 
     public boolean isDirty() {
         return parent.isDirty();
     }
 
     public void save() {
         parent.save();
     }
 
     public void close(boolean save) {
         parent.close(save);
     }
 
     public boolean isActive() {
         return parent.isActive();
     }
 
     public void close() {
         parent.close();
     }
 
     public void maximize() {
         parent.maximize();
     }
 
     public void minimize() {
         parent.minimize();
     }
 
     // TODO The following methods were added in Red Deer 0.6.0
     
 	@Override
 	public void activate() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public void restore() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ContentAssistant openContentAssistant() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ContentAssistant openQuickFixContentAssistant() {
 		throw new UnsupportedOperationException();
 	}
 
 	@Override
 	public ContentAssistant openOpenOnAssistant() {
		// TODO Auto-generated method stub
		return null;
 	}
 
 	@Override
 	public List<Marker> getMarkers() {
		// TODO Auto-generated method stub
		return null;
 	}
 
 }
