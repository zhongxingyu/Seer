 package toolbus_ide;
 
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.HashSet;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IMarker;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.imp.builder.BuilderBase;
 import org.eclipse.imp.language.Language;
 import org.eclipse.imp.language.LanguageRegistry;
 import org.eclipse.imp.runtime.PluginBase;
 
 import toolbus.ToolBus;
 import toolbus.exceptions.ToolBusException;
 import toolbus.exceptions.ToolBusExecutionException;
 import toolbus.parsercup.SyntaxErrorException;
 import toolbus.parsercup.parser;
 import toolbus.parsercup.parser.UndeclaredVariableException;
 import toolbus_ide.editor.ParseController;
import aterm.ATerm;

public class Builder extends BuilderBase{
    public static final String BUILDER_ID = Activator.kPluginID + ".toolbus_ide.builder";
 
    public static final String PROBLEM_MARKER_ID = Activator.kPluginID + ".toolbus_ide.builder.problem";
 
    public static final String LANGUAGE_NAME = "tscript";
 
     public static final Language LANGUAGE = LanguageRegistry.findLanguage(LANGUAGE_NAME);
 
     protected String getErrorMarkerID(){
         return PROBLEM_MARKER_ID;
     }
 
     protected String getWarningMarkerID(){
         return PROBLEM_MARKER_ID;
     }
 
     protected String getInfoMarkerID(){
         return PROBLEM_MARKER_ID;
     }
 
     protected boolean isSourceFile(IFile file){
         IPath path = file.getRawLocation();
         if (path == null) return false;
 
         String pathString = path.toString();
         if (pathString.indexOf("/bin/") != -1) return false;
 
         return LANGUAGE.hasExtension(path.getFileExtension());
     }
 
     protected boolean isNonRootSourceFile(IFile resource){
         return false;
     }
 
     protected void collectDependencies(IFile file){
     }
 
     protected boolean isOutputFolder(IResource resource){
         return resource.getFullPath().lastSegment().equals("bin");
     }
 
     protected void compile(final IFile file, IProgressMonitor monitor){
         try{
         	IMarker marker = file.createMarker(PROBLEM_MARKER_ID);
         	// TODO see ErrorViewer.java on how to create error markers
         	marker.setAttribute(IMarker.MESSAGE, "there is no implementation of a builder yet");
         	
         	String filename = file.getName();
         	String[] includePath = ParseController.buildIncludePath();
         	ToolBus toolbus = new ToolBus(includePath);
     		try{
     			parser parser_obj = new parser(new HashSet<String>(), new ArrayList<ATerm>(), filename, new FileReader(filename), toolbus);
     			parser_obj.parse();
     		}catch(SyntaxErrorException see){ // Parser.
     			// TODO assuming the input is the whole file, this fix is correct.
     			// needs to be fixed in IMP
     			//int pos = (see.position >= input.length()) ? input.length() - 1 : see.position;
     			//handler.handleSimpleMessage(see.getMessage(), pos, pos, see.column, see.column, see.line, see.line);
     		}catch(UndeclaredVariableException uvex){ // Parser.
     			//int pos = (uvex.position >= input.length()) ? input.length() - 1 : uvex.position;
     			//handler.handleSimpleMessage(uvex.getMessage(), pos, pos, uvex.column, uvex.column, uvex.line, uvex.line);
     		}catch(ToolBusExecutionException e){
     			//PositionInformation p = e.getPositionInformation();
     			//handler.handleSimpleMessage(e.getMessage(), p.getOffset(), p.getOffset(), 0, 0, 1, 1);
     		}catch(ToolBusException e){
     			//handler.handleSimpleMessage(e.getMessage(), 0, 0, 0, 0, 1, 1);
     			e.printStackTrace();
     		}catch(Error e){ // Scanner.
     			//handler.handleSimpleMessage(e.getMessage(), 0, 0, 0, 0, 1, 1);
     		}catch(Exception ex){ // Something else.
     			//handler.handleSimpleMessage(ex.getMessage(), 0, 0, 0, 0, 1, 1);
     		}
         }catch(Exception e){
             getPlugin().logException(e.getMessage(), e);
         }
     }
     
 	protected PluginBase getPlugin(){
 		return Activator.getInstance();
 	}
 }
