 package com.ibm.watson.safari.xform.search;
 
 import java.util.Iterator;
 import java.util.Set;
import lpg.lpgjavaruntime.IMessageHandler;
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.resources.IProject;
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.resources.IResourceVisitor;
 import org.eclipse.core.resources.ResourcesPlugin;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.search.ui.ISearchQuery;
 import org.eclipse.search.ui.ISearchResult;
 import org.eclipse.search.ui.text.Match;
 import org.eclipse.uide.core.Language;
 import org.eclipse.uide.core.LanguageRegistry;
 import org.eclipse.uide.parser.IParseController;
 import org.eclipse.uide.runtime.RuntimePlugin;
 import org.eclipse.uide.utils.ExtensionPointFactory;
 import org.eclipse.uide.utils.StreamUtils;
 import com.ibm.watson.safari.xform.XformPlugin;
 import com.ibm.watson.safari.xform.pattern.matching.IASTAdapter;
 import com.ibm.watson.safari.xform.pattern.matching.MatchResult;
 import com.ibm.watson.safari.xform.pattern.matching.Matcher;
 import com.ibm.watson.safari.xform.pattern.parser.ASTPatternLexer;
 import com.ibm.watson.safari.xform.pattern.parser.ASTPatternParser;
 import com.ibm.watson.safari.xform.pattern.parser.Ast.Pattern;
 
 public class ASTSearchQuery implements ISearchQuery {
     private String fASTPatternString;
 
     private String fLanguageName;
 
     private Language fLanguage;
 
     private Pattern fASTPattern;
 
     private IASTAdapter fASTAdapter;
 
     private ASTSearchScope fScope;
 
     private ASTSearchResult fResult;
 
     public ASTSearchQuery(String astPattern, String languageName, ASTSearchScope scope) {
 	fASTPatternString= astPattern;
 	fLanguageName= languageName;
 	fLanguage= LanguageRegistry.findLanguage(fLanguageName);
 	fScope= scope;
         fResult= new ASTSearchResult(this);
 
 	fASTAdapter= (IASTAdapter) ExtensionPointFactory.createExtensionPoint(fLanguage, XformPlugin.kPluginID, "astAdapter");
 	ASTPatternParser.setASTAdapter(fASTAdapter);
 
 	ASTPatternLexer lexer= new ASTPatternLexer(fASTPatternString.toCharArray(), "__PATTERN__");
         ASTPatternParser parser= new ASTPatternParser(lexer.getLexStream());
     
         lexer.lexer(parser); // Why wasn't this done by the parser ctor?
 	fASTPattern= (Pattern) parser.parser();
     }
 
     class SystemOutMessageHandler implements IMessageHandler {
 	public void handleMessage(int offset, int length, String message) {
 	    System.out.println("[" + offset + ":" + length + "] " + message);
 	}
     }
 
     public IStatus run(final IProgressMonitor monitor) throws OperationCanceledException {
 	final IMessageHandler msgHandler= new SystemOutMessageHandler();
 
 	for(Iterator iter= fScope.getProjects().iterator(); iter.hasNext(); ) {
             final IProject p= (IProject) iter.next();
 
             try {
                 p.accept(new IResourceVisitor() {
                     public boolean visit(IResource resource) throws CoreException {
                         if (resource instanceof IFile) {
                             IFile file= (IFile) resource;
                             String exten= file.getFileExtension();
 
                             if (resource.isDerived())
                         	System.out.println("Skipping derived resource " + resource.getFullPath());
 
                             if (exten != null && fLanguage.hasExtension(exten)) {
                         	monitor.subTask("Searching " + file.getFullPath());
                                 String contents= StreamUtils.readStreamContents(file.getContents(), ResourcesPlugin.getEncoding());
                                 IParseController parseController= (IParseController) ExtensionPointFactory.createExtensionPoint(fLanguage, RuntimePlugin.UIDE_RUNTIME, "parser");
 
                                 if (parseController == null)
                                     return false;
                                 parseController.initialize(resource.getProjectRelativePath().toString(), p, msgHandler);
 
                                 Object astRoot= parseController.parse(contents, false, monitor);
 
                                 if (astRoot == null)
                                     return false;
 
                                 Matcher m= new Matcher(fASTPattern);
                                 Set/*MatchResult*/ matches= fASTAdapter.findAllMatches(m, astRoot);
 
                                 for(Iterator iterator= matches.iterator(); iterator.hasNext(); ) {
 				    MatchResult match= (MatchResult) iterator.next();
 				    Object matchNode= match.getMatchNode();
                                     Match textMatch= new Match(file, fASTAdapter.getOffset(matchNode), fASTAdapter.getLength(matchNode));
 
                                     fResult.addMatch(textMatch);
 				}
                             }
                             return false;
                         }
                         return true;
                     }
                 });
             } catch (CoreException e) {
                 e.printStackTrace();
             }
         }
         return new Status(IStatus.OK, "com.ibm.watson.safari.xform", 0, "Search complete", null);
     }
 
     public String getLabel() {
 	return "<AST " + fASTPatternString + ">";
     }
 
     public boolean canRerun() {
 	return false;
     }
 
     public boolean canRunInBackground() {
 	return true;
     }
 
     public ISearchResult getSearchResult() {
 	return fResult;
     }
 }
