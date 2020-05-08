 package org.eclipse.dltk.ui.viewsupport;
 
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 import org.eclipse.core.resources.IStorage;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.dltk.core.DLTKLanguageManager;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.jface.resource.ImageDescriptor;
 import org.eclipse.jface.viewers.LabelProvider;
 import org.eclipse.swt.graphics.Image;
 import org.eclipse.ui.IEditorDescriptor;
 import org.eclipse.ui.IEditorRegistry;
 import org.eclipse.ui.IFileEditorMapping;
 import org.eclipse.ui.ISharedImages;
 import org.eclipse.ui.PlatformUI;
 
 
 public class StorageLabelProvider extends LabelProvider
 {
 	private IEditorRegistry fEditorRegistry = null;
 
 	private Map fImageMap = new HashMap(10);
 
 	private Image fDefaultImage;
 
 	private IEditorRegistry getEditorRegistry()
 	{
 		if( fEditorRegistry == null )
 			fEditorRegistry = PlatformUI.getWorkbench().getEditorRegistry();
 		return fEditorRegistry;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see ILabelProvider#getImage
 	 */
 	public Image getImage(Object element)
 	{
 		if( element instanceof ISourceModule) {
 			return getImageForEntry((ISourceModule)element);
 		} else if( element instanceof IStorage ) {
 			return getImageForEntry((IStorage) element);		
 		}
 		
 
 		return super.getImage(element);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see ILabelProvider#getText
 	 */
 	public String getText(Object element)
 	{
 		if( element instanceof IStorage )
 			return ((IStorage) element).getName();
 
 		return super.getText(element);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see IBaseLabelProvider#dispose
 	 */
 	public void dispose()
 	{
 		if( fImageMap != null ) {
 			Iterator each = fImageMap.values().iterator();
 			while( each.hasNext() ) {
 				Image image = (Image) each.next();
 				image.dispose();
 			}
 			fImageMap = null;
 		}
 		fDefaultImage = null;
 	}
 
 	/*
 	 * Gets and caches an image for a ArchiveEntryFile. The image for a ArchiveEntryFile
 	 * is retrieved from the EditorRegistry.
 	 */
 	private Image getImageForEntry(IStorage element)
 	{				
 		if( fImageMap == null )
 			return getDefaultImage();
 
 		if( element == null || element.getName() == null )
 			return getDefaultImage();
 
 		// Try to find icon for full name
 		String name = element.getName();
 		Image image = (Image) fImageMap.get(name);
 		if( image != null )
 			return image;
 		IFileEditorMapping[] mappings = getEditorRegistry().getFileEditorMappings();
 		int i = 0;
 		while( i < mappings.length ) {
 			if( mappings[i].getLabel().equals(name) )
 				break;
 			i++;
 		}
 		String key = name;
 		if( i == mappings.length ) {
 			// Try to find icon for extension
 			IPath path = element.getFullPath();
 			if( path == null )
 				return getDefaultImage();
 			key = path.getFileExtension();
 //			if( key == null )
 //				return getDefaultImage();
 			if( key != null ) {
 				image = (Image) fImageMap.get(key);
 				if( image != null )
 					return image;
 			}
 		}
 
 		// Get the image from the editor registry
 		ImageDescriptor desc = null;
 		
 		// Use DLTK Based editor images for all editors.
 		IEditorDescriptor[] descs = getEditorRegistry().getEditors(name);
 		for(int e = 0; e < descs.length; ++e ) {
 			String id = descs[e].getId();
 			if( id.indexOf("dltk") > 0 ) {
 				desc = descs[e].getImageDescriptor();
 			}
 		}
 		
 		if( desc == null ) {		
 			desc = getEditorRegistry().getImageDescriptor(name);
 		}
 		
 		image = desc.createImage();
 
 		fImageMap.put(key, image);
 
 		return image;
 	}
 	
 	private Image getImageForEntry(ISourceModule element)
 	{
 		if( fImageMap == null )
 			return getDefaultImage();
 
 		if( element == null || element.getElementName() == null )
 			return getDefaultImage();
 
 		// Try to find icon for full name
 		String name = element.getElementName();
 		Image image = (Image) fImageMap.get(name);
 		if( image != null )
 			return image;
 		IFileEditorMapping[] mappings = getEditorRegistry().getFileEditorMappings();
 		int i = 0;
 		while( i < mappings.length ) {
 			if( mappings[i].getLabel().equals(name) )
 				break;
 			i++;
 		}
 		String key = name;
 		if( i == mappings.length ) {
 			// Try to find icon for extension
 			IPath path = element.getPath();
 			if( path == null )
 				return getDefaultImage();
 			key = path.getFileExtension();
 //			if( key == null )
 //				return getDefaultImage();
 			if( key != null ) {
 				image = (Image) fImageMap.get(key);
 				if( image != null )
 					return image;
 			}			
 		}
 
 		// Get the image from the editor registry
 		//		ImageDescriptor desc = getEditorRegistry().getImageDescriptor(name);
 		//		 Get the image from the editor registry
 		ImageDescriptor desc = null;
 		// Use DLTK Based editor images for all editors.
 		try {
 			IDLTKLanguageToolkit toolkit = DLTKLanguageManager.getLanguageToolkit(element);
			if( toolkit == null ) {
				return null;
			}
 			String editorID = toolkit.getEditorID(element);
 			IEditorDescriptor ed = getEditorRegistry().findEditor(editorID);
 			if (ed != null) {
 				desc = ed.getImageDescriptor();
 			}
 		} catch (CoreException e1) {
 		}
 		
 		if( desc == null ) {		
 			desc = getEditorRegistry().getImageDescriptor(name);
 		}
 		image = desc.createImage();
 
 		fImageMap.put(key, image);
 
 		return image;
 	}
 
 	private Image getDefaultImage()
 	{
 		if( fDefaultImage == null )
 			fDefaultImage = PlatformUI.getWorkbench().getSharedImages().getImage(ISharedImages.IMG_OBJ_FILE);
 		return fDefaultImage;
 	}
 }
