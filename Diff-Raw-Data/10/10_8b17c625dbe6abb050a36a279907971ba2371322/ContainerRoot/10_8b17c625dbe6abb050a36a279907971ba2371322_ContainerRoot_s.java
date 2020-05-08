 // Copyright (c) 2011, David J. Pearce (djp@ecs.vuw.ac.nz)
 // All rights reserved.
 //
 // Redistribution and use in source and binary forms, with or without
 // modification, are permitted provided that the following conditions are met:
 //    * Redistributions of source code must retain the above copyright
 //      notice, this list of conditions and the following disclaimer.
 //    * Redistributions in binary form must reproduce the above copyright
 //      notice, this list of conditions and the following disclaimer in the
 //      documentation and/or other materials provided with the distribution.
 //    * Neither the name of the <organization> nor the
 //      names of its contributors may be used to endorse or promote products
 //      derived from this software without specific prior written permission.
 //
 // THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 // ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 // WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 // DISCLAIMED. IN NO EVENT SHALL DAVID J. PEARCE BE LIABLE FOR ANY
 // DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 // (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 // LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 // ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 // (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 // SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 
 package wyclipse.core.builder;
 
 import java.io.*;
 import java.util.*;
 
 import org.eclipse.core.resources.IContainer;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.Path;
 
 import wybs.lang.Content;
 import wybs.lang.Path.*;
 import wybs.util.AbstractFolder;
 import wybs.util.AbstractRoot;
 import wybs.util.AbstractEntry;
 import wybs.util.Trie;
 
 /**
  * An implementation of <code>Path.Root</code> which is backed by an Eclipse
  * <code>IContainer</code>.
  * 
  * @author David J. Pearce
  * 
  */
 public class ContainerRoot extends AbstractRoot<ContainerRoot.IFolderEntry> {	
 	private final IContainer dir;	
 		
 	/**
 	 * Construct a directory root from a given directory and file filter.
 	 * 
 	 * @param file
 	 *            --- location of directory on filesystem.
 	 */
 	public ContainerRoot(IContainer dir, Content.Registry contentTypes) {
 		super(contentTypes);
 		this.dir = dir;
 		// the following line is necessary because the AbstractFolder
 		// constructor calls root(), all of which will happen before this.dir is
 		// assigned above.
 		root.dir = dir;
 	}
 
 	public IContainer getContainer() {
 		return dir;
 	}
 	
 	public IFileEntry<?> getResource(IResource file) throws CoreException {		
 		try {			
 			// instead of calling getAll, I could just do the traversal right
 			// here. That would save creating lots of objects to hold the
 			// intermediate results from getAll.
 			for(Entry<?> e : root.getAll()) {	
 				if (e instanceof IFileEntry) {
 					IFileEntry ife = (IFileEntry) e;
 					if (ife.file.equals(file)) {
 						return ife;
 					}
 				}
 			}			
 		} catch(RuntimeException e) {
 			throw e;
 		} catch(Exception other) {
 			// hmmm, obviously I don't like doing this probably the best way
 			// around it is to not extend abstract root.
 		}
 		return null;
 	}
 	
 	/**
 	 * Create an entry for the given resource (if appropriate). If the entry is
 	 * of no interest to this root, then nothing is created and it returned
 	 * null.
 	 * 
 	 * @param resource
 	 * @return
 	 * @throws CoreException
 	 */
 	public IFileEntry create(IResource resource) throws CoreException {
		IPath path = resource.getFullPath();
		IPath fullPath = dir.getFullPath();
 		try {
 			if (fullPath.isPrefixOf(path)
 					&& resource instanceof IFile) {
 				IFile file = (IFile) resource;				
 				ID id = path2ID(path.makeRelativeTo(fullPath));			
 				
 				String suffix = file.getFileExtension();
 				if (suffix != null
 						&& (suffix.equals("class") || suffix.equals("whiley"))) {
 					// FIXME: file types should not be hard coded above	
 					return (IFileEntry) root.create(id, file);
 				}
 			}
 		} catch(CoreIOException e) {
 			throw e.payload;
 		} catch(RuntimeException e) {
 			throw e;
 		} catch(Exception e) {
 			// hmmm, obviously I don't like doing this probably the best way
 			// around it is to not extend abstract root. 
 		}
 		return null;
 	}
 	
 	public IFolderEntry root() {
 		return new IFolderEntry(Trie.ROOT,null);
 	}
 	
 	public String toString() {
 		return dir.toString();
 	}
 	
 	public void refresh() {
 		// TODO
 	}
 	
 	public ID path2ID(IPath path) {		
 		path = path.removeFileExtension();
 		Trie id = Trie.ROOT;
 		for(int i=0;i!=path.segmentCount();++i) {
 			id = id.append(path.segment(i));
 		}
 		return id;
 	}
 	
 	public class IFolderEntry extends AbstractFolder {	
 		private IContainer dir;
 		
 		public IFolderEntry(ID id, IContainer dir) {
 			super(id);
 			this.dir = dir;
 		}
 		
 		protected Item[] contents() throws IOException {
 			try {
 				ArrayList<Item> contents = new ArrayList<Item>();			
 				for (IResource file : dir.members()) {			
 					if(file instanceof IFile) {
 						String suffix = file.getFileExtension();
 						if (suffix != null
 								&& (suffix.equals("class") || suffix.equals("whiley"))) {							
 							String filename = file.getName();
 							String name = filename.substring(0, filename.lastIndexOf('.'));
 							ID eid = id.append(name);							
 							IFileEntry entry = new IFileEntry(eid, (IFile) file);
 							contents.add(entry);
 							contentTypes.associate(entry);
 						}
 					} else if(file instanceof IFolder) {
 						IFolder folder = (IFolder) file;		
 						ID fid = id.append(folder.getName());
 						contents.add(new IFolderEntry(fid,folder));
 					}
 				}
 				
 				return contents.toArray(new Item[contents.size()]);
 
 			} catch (CoreException e) {
 				throw new CoreIOException(e);
 			}
 		}
 				
 		public <T> Entry<T> create(ID nid, Content.Type<T> ct,
 				Entry<?>... sources) throws IOException {
 			String root = nid.get(0);
 			if (nid.size() == 1) {
 				// attempting to create an entry in this folder
 				Entry<T> e = super.get(nid, ct);
 				if (e == null) {
 					// Entry doesn't already exist, so create it
 					Path path = new Path(nid.toString() + "."
 							+ contentTypes.suffix(ct));
 					IFile file = dir.getFile(path);
 					e = new IFileEntry<T>(id.append(root), file);
 					e.associate(ct, null);
 					super.insert(e);
 				}
 				return e;
 			} else {
 				// attempting to create entry in subfolder.
 				IFolderEntry folder = (IFolderEntry) getFolder(root);
 				if (folder == null) {
 					// Folder doesn't already exist, so create it.
 					ID fid = id.append(root); 							
					IPath fpath = dir.getFullPath().append(root);
 					folder = new IFolderEntry(fid,dir.getFolder(fpath));
 					super.insert(folder);
 				}
 				return folder.create(nid.subpath(1, nid.size()), ct, sources);
 			}
 		}
 		
 		public <T> Entry<T> create(ID nid, IFile file) throws IOException {
 			String root = nid.get(0);
 			if (nid.size() == 1) {
 				// attempting to create an entry in this folder
 				Entry<T> e = new IFileEntry<T>(id.append(root), file);
 				contentTypes.associate(e);
 				super.insert(e);				
 				return e;
 			} else {
 				// attempting to create entry in subfolder.
 				IFolderEntry folder = (IFolderEntry) getFolder(root);
 				if (folder == null) {
 					// Folder doesn't already exist, so create it.
 					ID fid = id.append(root); 		
					IPath fpath = dir.getFullPath().append(root);
 					folder = new IFolderEntry(fid,dir.getFolder(fpath));
 					super.insert(folder);
 				}
 				return folder.create(nid.subpath(1, nid.size()), file);
 			}
 		}
 	}
 	
 	/**
 	 * An IFileEntry is a file on the file system which represents a Whiley
 	 * module. The file may be encoded in a range of different formats. For
 	 * example, it may be a source file and/or a binary wyil file.
 	 * 
 	 * @author David J. Pearce
 	 * 
 	 */
 	public static class IFileEntry<T> extends AbstractEntry<T> {		
 		private final IFile file;		
 		
 		public IFileEntry(ID mid, IFile file) {
 			super(mid);			
 			this.file = file;
 		}
 		
 		public IFile getFile() {
 			return file;
 		}
 	
 		public String location() {
 			return file.getLocation().toFile().toString();			
 		}
 		
 		public long lastModified() {
 			return file.getModificationStamp();
 		}
 		
 		public String suffix() {
 			String filename = file.getName();
 			String suffix = "";
 			int pos = filename.lastIndexOf('.');
 			if (pos > 0) {
 				suffix = filename.substring(pos + 1);
 			}
 			return suffix;
 		}
 				
 		public void write(T contents) throws IOException {
 			super.write(contents);			
 			ByteArrayOutputStream out = new ByteArrayOutputStream();
 			contentType().write(out,contents);
 			byte[] bytes = out.toByteArray();
 			ByteArrayInputStream input = new ByteArrayInputStream(bytes);
 			try {
 				if (file.exists()) {
 					// File already exists, so update contents.
 					file.setContents(input,
 							IResource.FORCE | IResource.DERIVED, null);
 				} else {
 					// first, ensure containing folder exists
 					create(file.getParent());
 					// finally, create file
 					file.create(input, IResource.FORCE | IResource.DERIVED,
 							null);
 				}
 			} catch (CoreException e) {
 				throw new CoreIOException(e);
 			}
 		}
 		
 		public void refresh() {
 			if(!modified) {
 				contents = null; // reset contents
 			}
 		}
 		
 		public InputStream inputStream() throws IOException {
 			try {
 				return file.getContents();
 			} catch (CoreException e) {
 				throw new CoreIOException(e);
 			}
 		}
 		
 		public OutputStream outputStream() throws IOException {
 			return new IFileOutputStream(file);		
 		}
 		
 		/**
 		 * The following method traverses the folder hierarchy from a given
 		 * point and creates all IFolders that it encounters.  
 		 * 
 		 * @param container
 		 * @throws CoreException
 		 */
 		private void create(IContainer container) throws CoreException {			
 			if(container.exists()) {
 				return;
 			}
 			IContainer parent = container.getParent();
 			if(parent instanceof IFolder) {
 				create((IFolder)parent);
 			} 
 			if(container instanceof IFolder) {
 				IFolder folder = (IFolder) container;
 				folder.create(IResource.FORCE | IResource.DERIVED, true, null);
 			}
 		}
 	}
 	
 	/**
 	 * An adapator to simplify writing of data to <code>IFile</code> instances.
 	 * 
 	 * @author David J. Pearce
 	 * 
 	 */
 	private final static class IFileOutputStream extends OutputStream {
 		private final IFile file;
 		private final ByteArrayOutputStream out = new ByteArrayOutputStream();
 
 		public IFileOutputStream(IFile file) {
 			this.file = file;
 		}
 
 		public void close() throws IOException {
 			flush();
 			out.close();
 		}
 
 		public void flush() throws IOException {
 			out.flush();
 			InputStream inputStream = new ByteArrayInputStream(
 					out.toByteArray());
 			// update the resource content
 			try {
 				if (file.exists()) {
 					file.setContents(inputStream, IResource.FORCE, null);
 				} else {
 					file.create(inputStream, IResource.FORCE, null);
 				}
 			} catch (CoreException e) {
 				throw new CoreIOException(e);
 			}
 		}
 
 		public void write(byte[] bytes) throws IOException {
 			out.write(bytes);
 		}
 
 		public void write(byte[] bytes, int off, int len) throws IOException {
 			out.write(bytes, off, len);
 		}
 
 		public void write(int byt) throws IOException {
 			out.write(byt);
 		}
 	}
 }
