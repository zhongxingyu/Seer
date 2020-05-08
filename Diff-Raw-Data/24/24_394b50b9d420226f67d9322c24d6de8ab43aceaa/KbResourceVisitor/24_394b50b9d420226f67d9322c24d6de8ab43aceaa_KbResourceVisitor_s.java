 package org.jboss.tools.jst.web.kb.internal;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.jdt.core.IClasspathEntry;
 import org.eclipse.jdt.core.IJavaProject;
 import org.jboss.tools.common.model.XModel;
 import org.jboss.tools.common.model.XModelObject;
 import org.jboss.tools.common.model.filesystems.FileSystemsHelper;
 import org.jboss.tools.common.model.plugin.ModelPlugin;
 import org.jboss.tools.common.model.util.EclipseResourceUtil;
 import org.jboss.tools.jst.web.kb.WebKbPlugin;
 import org.jboss.tools.jst.web.kb.internal.scanner.IFileScanner;
 import org.jboss.tools.jst.web.kb.internal.scanner.JSF2ResourcesScanner;
 import org.jboss.tools.jst.web.kb.internal.scanner.LoadedDeclarations;
 import org.jboss.tools.jst.web.kb.internal.scanner.MyFacesScanner;
 import org.jboss.tools.jst.web.kb.internal.scanner.ScannerException;
 import org.jboss.tools.jst.web.kb.internal.scanner.XMLScanner;
 import org.jboss.tools.jst.web.model.helpers.InnerModelHelper;
 
 public class KbResourceVisitor implements IResourceVisitor {
 	static IFileScanner[] FILE_SCANNERS = {
 		new MyFacesScanner(),
 		new XMLScanner(),
 	};
 	JSF2ResourcesScanner jsf2scanner = new JSF2ResourcesScanner();
 	
 	KbProject p;
 
 	IPath[] outs = new IPath[0];
 	IPath[] srcs = new IPath[0];
 	IPath webinf = null;
 	IPath jsf2resources = null; //JSF 2
 	IResource jsf2resourcesFolder = null;
 	boolean jsf2resourcesProcessed = false;
 
 	public KbResourceVisitor(KbProject p) {
 		this.p = p;
 
 		if(p.getProject() != null && p.getProject().isOpen()) {
 			getJavaSourceRoots(p.getProject());
 
 			XModel model = InnerModelHelper.createXModel(p.getProject());
 			if(model != null) {
 				XModelObject wio = FileSystemsHelper.getWebInf(model);
 				if(wio != null) {
 					IResource wir = (IResource)wio.getAdapter(IResource.class);
 					if(wir != null) {
 						webinf = wir.getFullPath();
 						jsf2resources = webinf.removeLastSegments(1).append("resources"); //$NON-NLS-1$
 						IResource rf = ResourcesPlugin.getWorkspace().getRoot().getFolder(jsf2resources);
 						if(rf == null || !rf.exists()) {
 							jsf2resources = null;
 						} else {
 							jsf2resourcesFolder = rf;
 						}
 					}
 				}
 			}
 		}
 	}
 
 	public IResourceVisitor getVisitor() {
 		return this;
 	}
 
 	public void init() {
 		jsf2resourcesProcessed = false;
 	}
 
 	public boolean visit(IResource resource) {
 		if(resource instanceof IFile) {
 			IFile f = (IFile)resource;
 			for (int i = 0; i < outs.length; i++) {
 				if(outs[i].isPrefixOf(resource.getFullPath())) {
 					return false;
 				}
 			}
 			for (int i = 0; i < FILE_SCANNERS.length; i++) {
 				IFileScanner scanner = FILE_SCANNERS[i];
 				if(scanner.isRelevant(f)) {
 					long t = System.currentTimeMillis();
 					if(!scanner.isLikelyComponentSource(f)) {
 						p.pathRemoved(f.getFullPath());
 						return false;
 					}
 					LoadedDeclarations c = null;
 					try {
 						c = scanner.parse(f, p);
 					} catch (ScannerException e) {
 						WebKbPlugin.getDefault().logError(e);
 					}
 					if(c != null) componentsLoaded(c, f);
 					long dt = System.currentTimeMillis() - t;
 //					timeUsed += dt;
 //					System.out.println("Time=" + timeUsed);
 					break;
 				}
 			}
 			if(jsf2resources != null && jsf2resources.isPrefixOf(f.getFullPath()) && jsf2scanner.isLikelyComponentSource(f)) {
 				processJSF2Resources();
 			}
 		}
 		if(resource instanceof IFolder) {
 			IPath path = resource.getFullPath();
 			for (int i = 0; i < outs.length; i++) {
 				if(outs[i].isPrefixOf(path)) {
 					return false;
 				}
 			}
 			for (int i = 0; i < srcs.length; i++) {
 				if(srcs[i].isPrefixOf(path) || path.isPrefixOf(srcs[i])) {
 					return true;
 				}
 			}
 			if(jsf2resources != null) {
 				if (jsf2resources.isPrefixOf(path)) {
 					processJSF2Resources();
 					return false;
 				}
 				if(path.isPrefixOf(jsf2resources)) {
 					return true;
 				}
 			}
 			if(webinf != null) {
 				if(webinf.isPrefixOf(path) || path.isPrefixOf(webinf)
 						|| webinf.removeLastSegments(1).isPrefixOf(path) //Webroot
 				) {
 					return true;
 				}
 			}
 			
 			if(resource == resource.getProject()) {
 				return true;
 			}
 			return false;
 		}
 		//return true to continue visiting children.
 		return true;
 	}
 
 	void processJSF2Resources() {
 		if (jsf2resourcesFolder == null || jsf2resourcesProcessed) return;
 		jsf2resourcesProcessed = true;
 		JSF2ResourcesScanner scanner = new JSF2ResourcesScanner();
 		Map<IPath,LoadedDeclarations> result = null;
 		try {
 			result = scanner.parse((IFolder) jsf2resourcesFolder, p);
 		} catch (ScannerException e) {
 			WebKbPlugin.getDefault().logError(e);
 		}
 		if (result != null) {
 			for (IPath path: result.keySet()) {
 				LoadedDeclarations c = result.get(path);
 				p.registerComponents(c, path);
 			}
 		}
 	}
 	
 	void componentsLoaded(LoadedDeclarations c, IResource resource) {
 		if(c == null || c.getLibraries().size() == 0) return;
 		p.registerComponents(c, resource.getFullPath());
 	}
 
 	void getJavaSourceRoots(IProject project) {
 		IJavaProject javaProject = EclipseResourceUtil.getJavaProject(project);
 		if(javaProject == null) return;
 		List<IPath> ps = new ArrayList<IPath>();
 		List<IPath> os = new ArrayList<IPath>();
 		try {
 			IPath output = javaProject.getOutputLocation();
 			if(output != null) os.add(output);
 			IClasspathEntry[] es = javaProject.getResolvedClasspath(true);
 			for (int i = 0; i < es.length; i++) {
 				if(es[i].getEntryKind() == IClasspathEntry.CPE_SOURCE) {
 					IResource findMember = ModelPlugin.getWorkspace().getRoot().findMember(es[i].getPath());
 					if(findMember != null && findMember.exists()) {
 						ps.add(findMember.getFullPath());
 					}
 					IPath out = es[i].getOutputLocation();
 					if(out != null && !os.contains(out)) {
 						os.add(out);
 					}
 				} 
 			}
 			srcs = ps.toArray(new IPath[0]);
 			outs = os.toArray(new IPath[0]);
 		} catch(CoreException ce) {
 			ModelPlugin.getPluginLog().logError("Error while locating java source roots for " + project, ce); //$NON-NLS-1$
 		}
 	}
 
 }
