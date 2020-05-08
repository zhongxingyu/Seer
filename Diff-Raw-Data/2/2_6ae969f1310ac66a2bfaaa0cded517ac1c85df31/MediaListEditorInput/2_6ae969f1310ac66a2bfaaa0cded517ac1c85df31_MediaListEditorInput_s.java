 package net.sparktank.morrigan.editors;
 
 import net.sparktank.morrigan.helpers.EqualHelper;
 import net.sparktank.morrigan.model.media.MediaList;
 
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.ui.IEditorInput;
 import org.eclipse.ui.IPersistableElement;
 
 public class MediaListEditorInput<T extends MediaList> implements IEditorInput {
 
 	private final T editedMediaList;
 
 	public MediaListEditorInput (T mediaList) {
 		editedMediaList = mediaList;
 	}
 	
 	public T getEditedMediaList() {
 		return editedMediaList;
 	}
 	
 	@Override
 	public boolean exists() {
 		return false;
 	}
 	
 	@Override
 	public ImageDescriptor getImageDescriptor() {
 		return null;
 	}
 	
 	@Override
 	public String getName() {
 		return editedMediaList.toString();
 	}
 	
 	@Override
 	public IPersistableElement getPersistable() {
 		return null;
 	}
 	
 	@Override
 	public String getToolTipText() {
 		return editedMediaList.toString();
 	}
 	
 	@SuppressWarnings("unchecked")
 	@Override
 	public Object getAdapter(Class adapter) {
 		return null;
 	}
 	
 	@Override
 	public boolean equals(Object aThat) {
 		if ( aThat == null ) return false;
 		if ( this == aThat ) return true;
 		if ( !(aThat instanceof MediaListEditorInput<?>) ) return false;
 		MediaListEditorInput<?> that = (MediaListEditorInput<?>)aThat;
 		
 		return EqualHelper.areEqual(editedMediaList.getListId(), that.getEditedMediaList().getListId());
 	}
 	
 	@Override
 	public int hashCode() {
		return editedMediaList.hashCode();
 	}
 }
