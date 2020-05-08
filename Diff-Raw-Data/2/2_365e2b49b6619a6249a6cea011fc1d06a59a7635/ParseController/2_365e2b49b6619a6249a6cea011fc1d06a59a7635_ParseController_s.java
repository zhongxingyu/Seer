 package toolbus_ide.editor;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.StringReader;
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 
 import java_cup.runtime.Symbol;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IFolder;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.imp.model.ISourceProject;
 import org.eclipse.imp.parser.IMessageHandler;
 import org.eclipse.imp.parser.IParseController;
 import org.eclipse.imp.parser.ISourcePositionLocator;
 import org.eclipse.imp.services.IAnnotationTypeInfo;
 import org.eclipse.imp.services.ILanguageSyntaxProperties;
 import org.eclipse.jface.text.IRegion;
 
 import toolbus.ToolBus;
 import toolbus.exceptions.ToolBusException;
 import toolbus.parsercup.Lexer;
 import toolbus.parsercup.sym;
 import toolbus.parsercup.parser.SyntaxErrorException;
 import toolbus.parsercup.parser.UndeclaredVariableException;
 
 public class ParseController implements IParseController {
 	private volatile IMessageHandler handler;
 	private volatile ISourceProject project;
 	private volatile String absPath;
 	
 	private volatile Lexer lexer;
 
 	public IAnnotationTypeInfo getAnnotationTypeInfo() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 	
 	public static class SymbolHolder{
 		public final Symbol symbol;
 		public final int startOffset;
 		public final int endOffset;
 		
 		public SymbolHolder(Symbol symbol, int startOffset, int endOffset){
 			this.symbol = symbol;
 			this.startOffset = startOffset;
 			this.endOffset = endOffset;
 		}
 	}
 	
     public Iterator<?> getTokenIterator(IRegion region){
     	class TokenIterator implements Iterator<SymbolHolder>{
     		private int currentOffset;
     		private Symbol nextSymbol;
     		
     		public TokenIterator(){
     			super();
     			prepareNext();
     		}
     		
     		public void prepareNext(){
     			try{
     				nextSymbol = lexer.next_token();
     				currentOffset = lexer.getPosition();
 				}catch(IOException ioex){
 					// Ignore this, since it can't happen.
 				}catch(Error e){
 					// This doesn't matter. it'll just generate error symbols.
 				}
     		}
 
 			public boolean hasNext(){
 				return !(nextSymbol.sym == sym.EOF || nextSymbol.sym == sym.error);
 			}
 
 			public SymbolHolder next(){
 				if(!hasNext()) return null;
 				
 				int offset = currentOffset;
 				Symbol symbol = nextSymbol;
 				
 				prepareNext();
 				
 				return new SymbolHolder(symbol, offset, currentOffset);
 			}
 
 			public void remove(){
 				throw new UnsupportedOperationException("Removing is not supported by this iterator.");
 			}
     	}
     	
     	return new TokenIterator();
     }
 
 	public Object getCurrentAst() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public ISourcePositionLocator getNodeLocator() {
 		return new TokenLocator();
 	}
 
 	public IPath getPath() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public ISourceProject getProject() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public ILanguageSyntaxProperties getSyntaxProperties() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void initialize(IPath filePath, ISourceProject project, IMessageHandler handler) {
 		this.handler = handler;
 		this.project = project;
 		
 		// Try to make the path absolute
 		IFile file = project.getRawProject().getFile(filePath);
 		
 		if(file.exists()){
 			absPath = file.getLocation().toOSString();
 		}else{
 			absPath = filePath.toOSString();
 		}
 	}
 	
 	private String[] buildIncludePath(){
 		List<String> includes = new ArrayList<String>();
 		
 		IProject[] projects = project.getRawProject().getWorkspace().getRoot().getProjects();
 		for(int i = projects.length - 1; i >= 0; i--){
 			IProject project = projects[i];
 			try{
 				IResource[] resources = project.members();
 				for(int j = resources.length -1; j >= 0; j--){
 					IResource resource = resources[j];
 					if(resource instanceof IFolder){
 						IFolder folder = (IFolder) resource;
 						IPath path = folder.getLocation();
 						File file = path.toFile();
 						includes.add(" -I"+file.getAbsolutePath());
 					}
 				}
 			}catch(CoreException cex){
 				// Ignore; we don't want to know about this atm.
 			}
 			
 			IPath path = project.getLocation();
 			File file = path.toFile();
 			includes.add("-I"+file.getAbsolutePath());
 		}
 		
 		return includes.toArray(new String[includes.size()]);
 	}
 
 	public Object parse(String input, boolean scanOnly, IProgressMonitor monitor) {
 		lexer = new Lexer(new StringReader(input));
 		
 		String[] includePath = buildIncludePath();
 		
 		ToolBus toolbus = new ToolBus(includePath);
 		try{
 			toolbus.parsecupString(absPath, input);
 			return toolbus;
		}catch(ToolBusException tex) {
			handler.handleSimpleMessage(tex.getMessage(), 0, 0, 0, 0, 1, 1);
 	    }catch(SyntaxErrorException see){ // Parser.
 	    	System.err.println("parse error");
 	    	// TODO assuming the input is the whole file, this fix is correct.
 	    	// needs to be fixed in IMP
 	    	int pos = (see.position >= input.length()) ? input.length() - 1 : see.position;
 	    	handler.handleSimpleMessage(see.getMessage(), pos, pos, see.column, see.column, see.line , see.line);
 		}catch(UndeclaredVariableException uvex){ // Parser.
 			int pos = (uvex.position >= input.length()) ? input.length() - 1 : uvex.position;
 			handler.handleSimpleMessage(uvex.getMessage(), pos, pos, uvex.column, uvex.column, uvex.line, uvex.line);
 		}catch(Error e){ // Scanner.
 			System.err.println("hiero!");
 			e.printStackTrace();	
 	    }catch(Exception ex){ // Something else.
 	    	ex.printStackTrace();
 	    }
 		
 		return null;
 	}
 }
