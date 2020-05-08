 package org.eclipse.dltk.core;
 
 import org.eclipse.core.runtime.ILog;
 import org.eclipse.core.runtime.IStatus;
 import org.eclipse.core.runtime.Status;
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.ast.parser.ISourceParser;
 import org.eclipse.dltk.ast.parser.ISourceParserConstants;
 import org.eclipse.dltk.ast.parser.ISourceParserExtension;
 import org.eclipse.dltk.compiler.problem.IProblemReporter;
 import org.eclipse.dltk.core.ISourceModuleInfoCache.ISourceModuleInfo;
 import org.eclipse.dltk.internal.core.ModelManager;
 
 public class SourceParserUtil {
 	private static final Object AST = "ast"; //$NON-NLS-1$
 	private static final Object FLAGS = "flags"; //$NON-NLS-1$
 
 	public static interface IContentAction {
 		void run(ISourceModule module, char[] content);
 	}
 
 	public static ModuleDeclaration getModuleDeclaration(ISourceModule module,
 			IProblemReporter reporter) {
 		return getModuleDeclaration(module, reporter,
 				ISourceParserConstants.DEFAULT, null);
 	}
 
 	public static ModuleDeclaration getModuleDeclaration(ISourceModule module,
 			IProblemReporter reporter, IContentAction action) {
 		return getModuleDeclaration(module, reporter,
 				ISourceParserConstants.DEFAULT, action);
 	}
 
 	public static ModuleDeclaration getModuleDeclaration(ISourceModule module,
 			IProblemReporter reporter, int flags) {
 		ISourceModuleInfoCache sourceModuleInfoCache = ModelManager
 				.getModelManager().getSourceModuleInfoCache();
 		ISourceModuleInfo sourceModuleInfo = sourceModuleInfoCache.get(module);
 		return getModuleDeclaration(module, reporter, sourceModuleInfo, flags,
 				null);
 	}
 
 	public static ModuleDeclaration getModuleDeclaration(ISourceModule module,
 			IProblemReporter reporter, int flags, IContentAction action) {
 		ISourceModuleInfoCache sourceModuleInfoCache = ModelManager
 				.getModelManager().getSourceModuleInfoCache();
 		ISourceModuleInfo sourceModuleInfo = sourceModuleInfoCache.get(module);
 		return getModuleDeclaration(module, reporter, sourceModuleInfo, flags,
 				action);
 	}
 
 	public static ModuleDeclaration getModuleDeclaration(ISourceModule module,
 			IProblemReporter reporter, ISourceModuleInfo mifo) {
 		return getModuleDeclaration(module, reporter, mifo,
 				ISourceParserConstants.DEFAULT, null);
 	}
 
 	public static ModuleDeclaration getModuleDeclaration(ISourceModule module,
 			IProblemReporter reporter, ISourceModuleInfo mifo,
 			IContentAction action) {
 		return getModuleDeclaration(module, reporter, mifo,
 				ISourceParserConstants.DEFAULT, action);
 	}
 
 	public static ModuleDeclaration getModuleDeclaration(ISourceModule module,
 			IProblemReporter reporter, ISourceModuleInfo mifo, int flags,
 			IContentAction action) {
 
 		IDLTKLanguageToolkit toolkit;
 		toolkit = DLTKLanguageManager.getLanguageToolkit(module);
		if (toolkit == null) {
			return null;
		}
 		ModuleDeclaration moduleDeclaration = null;
 		Integer flag;
 		if (mifo != null) {
 			moduleDeclaration = (ModuleDeclaration) mifo.get(AST);
 			flag = (Integer) mifo.get(FLAGS);
 			if (flag != null && flag.intValue() != flags) {
 				moduleDeclaration = null;
 			}
 		}
 		if (moduleDeclaration == null) {
 			if (reporter != null) {
 				reporter.clearMarkers();
 			}
 			ISourceParser sourceParser = null;
 			sourceParser = DLTKLanguageManager.getSourceParser(toolkit
 					.getNatureId());
 			if (sourceParser != null) {
 				if (sourceParser instanceof ISourceParserExtension) {
 					((ISourceParserExtension) sourceParser).setFlags(flags);
 				}
 				try {
 					char[] sourceAsCharArray = module.getSourceAsCharArray();
 					moduleDeclaration = sourceParser.parse(module.getPath()
 							.toString().toCharArray(), sourceAsCharArray,
 							reporter);
 					if (action != null) {
 						action.run(module, sourceAsCharArray);
 					}
 				} catch (ModelException e) {
 					ILog log = DLTKCore.getDefault().getLog();
 					IStatus status = new Status(IStatus.ERROR,
 							DLTKCore.PLUGIN_ID,
 							"Error to get source module content", e);
 					log.log(status);
 				}
 				if (moduleDeclaration != null && mifo != null) {
 					mifo.put(AST, moduleDeclaration);
 					mifo.put(FLAGS, new Integer(flags));
 				}
 			}
 		}
 		return moduleDeclaration;
 	}
 
 	public static ModuleDeclaration getModuleDeclaration(char[] filename,
 			char[] content, String nature, IProblemReporter reporter,
 			ISourceModuleInfo mifo) {
 		return getModuleDeclaration(filename, content, nature, reporter, mifo,
 				ISourceParserConstants.DEFAULT);
 	}
 
 	public static ModuleDeclaration getModuleDeclaration(char[] filename,
 			char[] content, String nature, IProblemReporter reporter,
 			ISourceModuleInfo mifo, int flags) {
 		ISourceParser sourceParser;// = new SourceParser(this.fReporter);
 		sourceParser = DLTKLanguageManager.getSourceParser(nature);
 		if (sourceParser instanceof ISourceParserExtension) {
 			((ISourceParserExtension) sourceParser).setFlags(flags);
 		}
 		ModuleDeclaration moduleDeclaration = SourceParserUtil
 				.getModuleFromCache(mifo, flags);
 		if (moduleDeclaration == null) {
 			if (reporter != null) {
 				reporter.clearMarkers();
 			}
 			moduleDeclaration = sourceParser.parse(filename, content, reporter);
 			SourceParserUtil.putModuleToCache(mifo, moduleDeclaration, flags);
 		}
 		return moduleDeclaration;
 	}
 
 	/**
 	 * This is for use in parsers.
 	 */
 	public static ModuleDeclaration getModuleFromCache(ISourceModuleInfo mifo,
 			int flags) {
 		if (mifo != null) {
 			Integer flag = (Integer) mifo.get(FLAGS);
 			if (flag != null && flag.intValue() != flags) {
 				return null;
 			}
 			return (ModuleDeclaration) mifo.get(AST);
 		}
 		return null;
 	}
 
 	public static void putModuleToCache(ISourceModuleInfo info,
 			ModuleDeclaration module, int flags) {
 		if (info != null) {
 			info.put(AST, module);
 			info.put(FLAGS, new Integer(flags));
 		}
 	}
 
 	public static void parseSourceModule(ISourceModule module,
 			ISourceElementParser parser) {
 		char[] contents;
 		try {
 			contents = module.getSourceAsCharArray();
 			ISourceModuleInfoCache sourceModuleInfoCache = ModelManager
 					.getModelManager().getSourceModuleInfoCache();
 			ISourceModuleInfo mifo = sourceModuleInfoCache.get(module);
 			parser.parseSourceModule(contents, mifo, module.getPath()
 					.toString().toCharArray());
 		} catch (ModelException e) {
 			if (DLTKCore.DEBUG) {
 				e.printStackTrace();
 			}
 		}
 	}
 }
