 package fdt.builder;
 
 import java.io.File;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceDelta;
 import org.eclipse.core.resources.IResourceDeltaVisitor;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.ProjectScope;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.preferences.IScopeContext;
 import org.eclipse.ui.console.ConsolePlugin;
 import org.eclipse.ui.console.IConsole;
 import org.eclipse.ui.console.IConsoleManager;
 import org.eclipse.ui.console.MessageConsole;
 import org.eclipse.ui.console.MessageConsoleStream;
 import org.osgi.service.prefs.Preferences;
 
 import fdk.cfg.CFGFile;
 import fdt.builder.dependency.DependencyGraph;
 import fdt.compilers.CompilersList;
 import fdt.compilers.Mark;
 import fdt.preferences.PreferenceConstants;
 
 public class SSLCResourceVisitor implements IResourceVisitor, IResourceDeltaVisitor {
 
     class Compiler {
         private File   outDir;
         private File   cpPath;
         private String cpType;
 
         public Compiler(File outD, File cpP, String cpT) {
             outDir = outD;
             cpPath = cpP;
             cpType = cpT;
 
             outDir.mkdirs();
         }
 
         public Mark[] compile(IFile file, MessageConsoleStream msg) throws Exception {
             File outFile = new File(outDir, file.getName().replaceAll("\\.[Ss][Ss][Ll]$", ".int"));
             return CompilersList.getInstance().getCompiler(cpType).compile(cpPath, file, outFile, msg);
         }
     }
 
     private Compiler            m_compiler;
    private static final String MARKER_TYPE = "SSL.sslcProblem";
     private MessageConsole      m_console   = createConsole("SSLC Console");
     private IProgressMonitor    m_monitor;
     private IProject            m_project;
     private DependencyGraph     m_graph     = new DependencyGraph();
 
     public SSLCResourceVisitor(IProject project, IProgressMonitor monitor, int files) {
         m_project = project;
 
         try {
             IScopeContext projectScope = new ProjectScope(project);
             Preferences projectNode = projectScope.getNode("SSL");
             String cpPath = projectNode.get(PreferenceConstants.P_COMPILER, "");
             String cpType = projectNode.get(PreferenceConstants.C_COMPILER_TYPE, "");
             String cfgPath = projectNode.get(PreferenceConstants.C_CFG, "");
             projectNode.flush();
 
             File fCfg = new File(cfgPath);
             CFGFile cfg = new CFGFile(fCfg);
             File outDir = new File(cfg.getPath("system.master_patches"), "scripts");
 
             try {
                 m_graph.buildGraph(m_project.getLocation().toFile());
             } catch (Exception e) {}
             m_compiler = new Compiler(outDir, new File(cpPath), cpType);
             m_console.clearConsole();
 
             m_monitor = monitor;
             if (m_monitor == null) {
                 m_monitor = new NullProgressMonitor();
             }
             m_monitor.beginTask("Building scripts", files);
         } catch (Exception e) {
             e.printStackTrace();
             m_monitor = new NullProgressMonitor();
         }
     }
 
     public boolean visit(IResource resource) {
         if (m_monitor.isCanceled()) {
             return false;
         }
         m_monitor.subTask("Building script: " + resource.getFullPath().toString());
 
         boolean ret = compile(resource);
 
         if (ret) {
             m_monitor.worked(1);
         }
         return true;
     }
 
     @Override
     public boolean visit(IResourceDelta delta) throws CoreException {
         if (m_monitor.isCanceled()) {
             return false;
         }
         IResource resource = delta.getResource();
         m_monitor.subTask("Building script: " + resource.getFullPath().toString());
 
         switch (delta.getKind()) {
         case IResourceDelta.ADDED:
             compile(resource);
             break;
         case IResourceDelta.REMOVED:
             break;
         case IResourceDelta.CHANGED:
             compile(resource);
             break;
         }
 
         m_monitor.worked(1);
         return true;
     }
 
     private MessageConsole createConsole(String name) {
         ConsolePlugin plugin = ConsolePlugin.getDefault();
         IConsoleManager conMan = plugin.getConsoleManager();
         IConsole[] existing = conMan.getConsoles();
         for (int i = 0; i < existing.length; i++)
             if (name.equals(existing[i].getName())) return (MessageConsole) existing[i];
         MessageConsole myConsole = new MessageConsole(name, null);
         conMan.addConsoles(new IConsole[] { myConsole });
         return myConsole;
     }
 
     protected boolean compile(IResource resource) {
         deleteMarkers(resource);
         if (resource instanceof IFile && SSLBuilder.checkSSLName(resource.getName())) {
             try {
                 IFile file = (IFile) resource;
                 File root = file.getLocation().removeLastSegments(1).toFile();
 
                 Mark[] marks = m_compiler.compile(file, m_console.newMessageStream());
 
                 for (Mark mark : marks) {
                     FileSearch fs = new FileSearch(mark.getCanonicalFile(root));
                     m_project.accept(fs);
                     IFile dstFile = (IFile) fs.getRes();
                     dstFile = dstFile == null ? file : dstFile;
 
                     switch (mark.getSeverity()) {
                     case Mark.WARNING:
                         addMarker(dstFile, mark.getMessage(), mark.getLine(), IMarker.SEVERITY_WARNING);
                         break;
 
                     case Mark.ERROR:
                         addMarker(dstFile, mark.getMessage(), mark.getLine(), IMarker.SEVERITY_ERROR);
                         break;
                     }
                 }
 
                 return true;
             } catch (Exception e) {
                 return false;
             }
         }
         return false;
     }
 
     private void addMarker(IResource file, String message, int lineNumber, int severity) {
         try {
             for (IMarker m : file.findMarkers(MARKER_TYPE, true, IResource.DEPTH_ZERO)) {
                 if (m.getAttribute(IMarker.MESSAGE).equals(message)) {
                     return;
                 }
             }
 
             IMarker marker = file.createMarker(MARKER_TYPE);
             marker.setAttribute(IMarker.MESSAGE, message);
             marker.setAttribute(IMarker.SEVERITY, severity);
             if (lineNumber == -1) {
                 lineNumber = 1;
             }
             marker.setAttribute(IMarker.LINE_NUMBER, lineNumber);
         } catch (CoreException e) {}
     }
 
     private void deleteMarkers(IResource file) {
         try {
             file.deleteMarkers(MARKER_TYPE, false, IResource.DEPTH_ZERO);
         } catch (CoreException ce) {}
     }
 }
