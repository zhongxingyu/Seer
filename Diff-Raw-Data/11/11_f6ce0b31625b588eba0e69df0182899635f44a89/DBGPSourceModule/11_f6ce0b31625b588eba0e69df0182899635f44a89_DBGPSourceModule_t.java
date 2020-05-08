 /**
  * 
  */
 package org.eclipse.dltk.launching.sourcelookup;
 
 import java.io.ByteArrayInputStream;
 import java.io.InputStream;
 import java.net.URI;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.core.DLTKCore;
 import org.eclipse.dltk.core.IDLTKLanguageToolkit;
 import org.eclipse.dltk.core.IModelStatus;
 import org.eclipse.dltk.core.ISourceModule;
 import org.eclipse.dltk.core.ModelException;
 import org.eclipse.dltk.core.WorkingCopyOwner;
 import org.eclipse.dltk.dbgp.exceptions.DbgpException;
 import org.eclipse.dltk.internal.core.AbstractExternalSourceModule;
 import org.eclipse.dltk.internal.core.DefaultWorkingCopyOwner;
 import org.eclipse.dltk.internal.core.ScriptProject;
 import org.eclipse.dltk.internal.debug.core.model.ScriptStackFrame;
 
 /**
  * This is DBGP source module.
  * 
  */
 public class DBGPSourceModule extends AbstractExternalSourceModule {
 
 	private ScriptStackFrame frame;
 
 	public DBGPSourceModule(ScriptProject parent, String name,
 			WorkingCopyOwner owner, ScriptStackFrame frame) {
 		super(parent, name, owner);
 
 		this.frame = frame;
 	}
 
 	/*
 	 * @see org.eclipse.dltk.internal.core.AbstractSourceModule#equals(java.lang.Object)
 	 */
 	public boolean equals(Object obj) {
 		if (!(obj instanceof DBGPSourceModule)) {
 			return false;
 		}
 
 		return super.equals(obj);
 	}
 
 	protected IStatus validateSourceModule(IResource resource) {
 		/*
 		 * XXX: is there a way to validate a remote resource?
 		 */
 		return IModelStatus.VERIFIED_OK;
 	}
 
 	/*
 	 * @see org.eclipse.core.resources.IStorage#getContents()
 	 */
 	public InputStream getContents() throws CoreException {
 		try {
 			byte[] contents = lookupSource().getBytes();
 			return new ByteArrayInputStream(contents);
 		} catch (DbgpException e) {
 			throw new CoreException(new Status(IStatus.ERROR,
 					DLTKCore.PLUGIN_ID, Messages.DBGPSourceModule_dbgpSourceLookup, e));
 		}
 	}
 
 	/*
 	 * @see org.eclipse.dltk.compiler.env.IDependent#getFileName()
 	 */
 	public char[] getFileName() {
 		/*
 		 * XXX: remote source should not be touched by compiler
 		 * 
 		 * remove this and just make the other sub-classes implement
 		 * org.eclipse.dltk.compiler.env.IDependent directly?
 		 */
 		return new char[0];
 	}
 
 	/*
 	 * @see org.eclipse.core.resources.IStorage#getFullPath()
 	 */
 	public IPath getFullPath() {
 		return null;
 	}
 
 	/*
 	 * @see org.eclipse.core.resources.IStorage#getName()
 	 */
 	public String getName() {
 		Path path = new Path(frame.getFileName().getPath());
 		return path.lastSegment();
 	}
 
 	/*
 	 * @see org.eclipse.dltk.internal.core.AbstractSourceModule#getBufferContent()
 	 */
 	protected char[] getBufferContent() throws ModelException {
 		try {
 			return lookupSource().toCharArray();
 		} catch (DbgpException e) {
 			throw new ModelException(e, IModelStatus.ERROR);
 		}
 	}
 
 	private String lookupSource() throws DbgpException {
 		/*
 		 * XXX: this has problems if the encodings on both hosts don't match -
 		 * see getBufferContents/getContents
 		 */
 		URI uri = frame.getFileName();
 		return frame.getScriptThread().getDbgpSession().getCoreCommands()
 				.getSource(uri);
 	}
 
 	/*
 	 * @see org.eclipse.dltk.internal.core.AbstractExternalSourceModule#getModuleType()
 	 */
 	protected String getModuleType() {
 		return "DLTK Remote Source Moule: "; //$NON-NLS-1$
 	}
 
 	/*
 	 * @see org.eclipse.dltk.internal.core.AbstractSourceModule#getNatureId()
 	 */
 	protected String getNatureId() throws CoreException {
		IDLTKLanguageToolkit toolkit = lookupLanguageToolkit(getParent());
 		if (toolkit == null) 
 			return null;
 		
 		return toolkit.getNatureId();
 	}
 
 	/*
 	 * @see org.eclipse.dltk.internal.core.AbstractSourceModule#getOriginalSourceModule()
 	 */
 	protected ISourceModule getOriginalSourceModule() {
 		return new DBGPSourceModule((ScriptProject) getParent(),
 				getElementName(), DefaultWorkingCopyOwner.PRIMARY, frame);
 	}
 }
