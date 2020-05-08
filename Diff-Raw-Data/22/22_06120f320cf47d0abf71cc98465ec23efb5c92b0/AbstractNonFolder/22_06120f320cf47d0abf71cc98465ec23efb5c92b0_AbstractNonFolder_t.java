 package net.bioclipse.brunn.ui.explorer.model.nonFolders;
 
 import java.util.ArrayList;
 
 import org.eclipse.swt.graphics.Image;
 
 import net.bioclipse.brunn.pojos.ILISObject;
 import net.bioclipse.brunn.ui.explorer.model.ITreeObject;
 import net.bioclipse.brunn.ui.explorer.model.Model;
 
 public abstract class AbstractNonFolder extends Model 
                                        implements ITreeObject {
 
 	protected ITreeObject parent;
 	protected ILISObject object;
 
 	public AbstractNonFolder(ITreeObject parent, ILISObject object) {
 		super();
 		this.parent = parent;
 		this.object = object;
 	}
 	
 	public ArrayList<ITreeObject> getChildren() {
 		return new ArrayList<ITreeObject>();
 	}
 
 	public String getName() {
 		return object.getName();
 	}
 	
 	public ITreeObject getParent() {
 		return parent;
 	}
 	
 	public ILISObject getPOJO() {
 		return object;
 	}
 	
 	public ITreeObject getFolder() {
 		return parent.getFolder();
 	}
 	
 	public Image getIconImage() {
 		return null;
 	}
 	
 	public ITreeObject getUniqueFolder() {
 		return parent.getUniqueFolder();
 	}
 
 	@Override
 	public int hashCode() {
 		final int prime = 31;
 		int result = 1;
 		result = prime * result + ((object == null) ? 0 : object.hashCode());
 		return result;
 	}
 
 	@Override
 	public boolean equals(Object obj) {
 		if (this == obj)
 			return true;
 		if (obj == null)
 			return false;
 		if (getClass() != obj.getClass())
 			return false;
 		final AbstractNonFolder other = (AbstractNonFolder) obj;
 		if (object == null) {
 			if (other.object != null)
 				return false;
 		} 
 		else {
 			return other.object.getId() == this.object.getId();
 		}
 		return false;
 	}
 }
