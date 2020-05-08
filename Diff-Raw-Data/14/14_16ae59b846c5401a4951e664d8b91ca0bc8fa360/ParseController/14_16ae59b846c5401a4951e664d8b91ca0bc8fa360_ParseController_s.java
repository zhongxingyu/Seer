 package org.meta_environment.eclipse.parser;
 
 import java.util.Iterator;
 
 import org.eclipse.core.resources.IFile;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.imp.language.ILanguageService;
 import org.eclipse.imp.language.Language;
 import org.eclipse.imp.language.LanguageRegistry;
 import org.eclipse.imp.model.ISourceProject;
 import org.eclipse.imp.parser.ILexer;
 import org.eclipse.imp.parser.IMessageHandler;
 import org.eclipse.imp.parser.IParseController;
 import org.eclipse.imp.parser.IParser;
 import org.eclipse.imp.parser.ISourcePositionLocator;
 import org.eclipse.imp.services.IAnnotationTypeInfo;
 import org.eclipse.imp.services.ILanguageSyntaxProperties;
 import org.eclipse.jface.text.IRegion;
 import org.meta_environment.eclipse.Tool;
 import org.meta_environment.eclipse.tokens.TokenIterator;
 import org.meta_environment.eclipse.tokens.TokenLocator;
 
 import aterm.ATerm;
 import aterm.ATermAppl;
 import aterm.ATermList;
 import errorapi.Factory;
 import errorapi.types.Area;
 import errorapi.types.Error;
 import errorapi.types.ErrorList;
 import errorapi.types.Subject;
 
 public class ParseController extends Tool implements IParseController,
 		ILanguageService {
 
 	private IPath filePath;
 
 	private String absPath;
 
 	private ISourceProject project;
 
 	private IMessageHandler handler;
 
 	private ErrorList errors;
 
 	public ATerm result;
 
 	Factory eFactory;
 
 	public ParseController() {
 		super("parse-controller");
 		eFactory = Factory.getInstance(factory);
 	}
 
 	public IAnnotationTypeInfo getAnnotationTypeInfo() {
 		return null;
 	}
 
 	public Object getCurrentAst() {
 		return result;
 	}
 
 	public ILexer getLexer() {
 		return null;
 	}
 
 	public ISourcePositionLocator getNodeLocator() {
 		return new TokenLocator();
 	}
 
 	public IParser getParser() {
 		return null;
 	}
 
 	public IPath getPath() {
 		return filePath;
 	}
 
 	public ISourceProject getProject() {
 		return project;
 	}
 
 	public ILanguageSyntaxProperties getSyntaxProperties() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	public void initialize(IPath filePath, ISourceProject project, IMessageHandler handler) {
 		this.filePath = filePath;
 		this.project = project;
 		this.handler = handler;
 		
 		connect(); // NOTE: This may only be called once.
 
 		// try to make path absolute
 		IFile file = project.getRawProject().getFile(filePath);
 		
 		if (file.exists()) {
 			absPath = file.getLocation().toOSString();
 		} else {
 			absPath = filePath.toOSString();
 		}
 	}
 
 	public Object parse(String input, boolean scanOnly, IProgressMonitor monitor) {
 		String l = this.getLanguage().getName();
 
 		ATerm res = sendRequest(factory.make("parse(<str>,<str>,<str>)", l, absPath, input));
 
 		if (res.getType() == ATerm.APPL) {
 			ATermAppl appl = (ATermAppl) res;
 			String name = appl.getName();
 
 			if (name.equals(TIME_OUT)) {
 				result = null;
 				errors = null;
 				return null;
 			} else if (name.equals("correct")) {
 				result = appl.getArgument(0);
 				errors = null;
 				return result;
 			} else if (name.equals("error")) {
 				result = null;
 				errors = eFactory.SummaryFromTerm(appl.getArgument(0))
 						.getList();
 				handleErrors(errors);
 				return null;
 			}
 		}
 
 		return result;
 	}
 
 	private void handleErrors(ErrorList list) {
 		for (; !list.isEmpty(); list = list.getTail()) {
 			Error error = list.getHead();
 			Subject subject = error.getList().getHead();
 			Area area;
 
 			if (subject.hasLocation()) {
 				area = subject.getLocation().getArea();
 			} else {
 				area = eFactory.makeArea_Area(0, 0, 0, 0, 0, 0);
 			}
 
 			handler.handleSimpleMessage(error.getDescription() + ": "
 					+ subject.getDescription(), area.getOffset(), area
 					.getOffset()
 					+ area.getLength(), area.getBeginColumn(), area
 					.getEndColumn(), area.getBeginLine(), area.getEndLine());
 		}
 	}
 
 	public Iterator<?> getTokenIterator(IRegion region) {
 		if (result != null) {
 			ATerm ack = sendRequest(factory.make("highlight(<term>)", result));
 
 			if (ack instanceof ATermList) {
 				final ATermList slices = (ATermList) ack;
 				return new TokenIterator(eFactory, slices);
 			}
 			// timeout happened
 		}
 
 		return new TokenIterator(eFactory, factory.makeList());
 	}
 
 	public Language getLanguage() {
 		return LanguageRegistry.findLanguage(filePath, null);
 	}
 }
