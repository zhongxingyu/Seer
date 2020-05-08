 package net.sourceforge.eclipseccase.ui.compare;
 
 import java.io.IOException;
 
 import net.sourceforge.eclipseccase.ClearCaseProvider;
 
 import java.io.File;
 
 import net.sourceforge.eclipseccase.StateCache;
 
 import net.sourceforge.eclipseccase.StateCacheFactory;
 
 import java.io.BufferedInputStream;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.InputStream;
 
 import org.eclipse.compare.BufferedContent;
 import org.eclipse.compare.CompareUI;
 import org.eclipse.compare.ITypedElement;
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.swt.graphics.Image;
 
 /**
  * Implements resource node for comparing ClearCase element versions.
  * 
  * <p>
  * Limitation: Do not handle directories.
  * </p>
  * 
  * @author eplatti
  */
 
 public class ClearCaseResourceNode extends BufferedContent implements ITypedElement {
 
 	private final IResource resource;
 
 	private final String vextPath;
 
 	private final ClearCaseProvider provider;
 
 	public ClearCaseResourceNode(IResource resource, String version, ClearCaseProvider provider) {
 		this.resource = resource;
 		this.vextPath = resource.getLocation().toOSString() + "@@" + version;
 		this.provider = provider;
 	}
 
 	
 	
 	@Override
 	public InputStream createStream() throws CoreException{
 		InputStream contents = null;
 		try
 		{
 			StateCache cache = StateCacheFactory.getInstance().get(resource);
 			if (cache.isSnapShot())
 			{
 				final File tempFile = File.createTempFile("eclipseccase", null);
 				tempFile.delete();
 				tempFile.deleteOnExit();
 				provider.copyVersionIntoSnapShot(tempFile.getPath(),vextPath);
 				//now we should have the snapshot version.
 				contents = new BufferedInputStream(new FileInputStream(tempFile.getPath()));
 				
 			}else{	
 				//dynamic
 				contents = new FileInputStream(vextPath);
 			}
 		}catch (FileNotFoundException e){
			throw new CoreException(new Status(IStatus.WARNING, "net.sourceforge.eclipseccase.ui.compare", "Internal, could not find file to compare with "+vextPath, e));
 		}
 		catch (IOException e){	
 			throw new CoreException(new Status(IStatus.WARNING, "net.sourceforge.eclipseccase.ui.compare", "Internal, Could not create temp file for predecessor: "+vextPath, e));
 		}
 		
 		return contents;
 	}
 
 	public String getName() {
 		return vextPath;
 	}
 
 	public Image getImage() {
 		return CompareUI.getImage(resource);
 	}
 
 	public String getType() {
 		if (resource instanceof IContainer)
 			return ITypedElement.FOLDER_TYPE;
 		if (resource != null) {
 			String s = resource.getFileExtension();
 			if (s != null) {
 				return s;
 			}
 		}
 		return ITypedElement.UNKNOWN_TYPE;
 	}
 }
