 package org.eclipse.dltk.core;
 
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.ast.parser.ISourceParser;
 import org.eclipse.dltk.ast.parser.ISourceParserConstants;
 import org.eclipse.dltk.ast.parser.ISourceParserExtension;
 import org.eclipse.dltk.compiler.env.CompilerSourceCode;
 import org.eclipse.dltk.compiler.problem.IProblemReporter;
 import org.eclipse.dltk.compiler.problem.ProblemCollector;
 import org.eclipse.dltk.core.ISourceModuleInfoCache.ISourceModuleInfo;
 import org.eclipse.dltk.internal.core.ModelManager;
 
 public class SourceParserUtil {
 	private static final String AST = "ast"; //$NON-NLS-1$
 	private static final String ERRORS = "errors"; //$NON-NLS-1$
 
 	public static interface IContentAction {
 		void run(ISourceModule module, char[] content);
 	}
 
 	public static ModuleDeclaration getModuleDeclaration(ISourceModule module) {
 		return getModuleDeclaration(module, null,
 				ISourceParserConstants.DEFAULT, null);
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
 		final String errorKey;
 		final String astKey;
 		if (mifo != null) {
 			errorKey = getKey(ERRORS, flags);
 			astKey = getKey(AST, flags);
 			moduleDeclaration = (ModuleDeclaration) mifo.get(astKey);
 			if (moduleDeclaration != null) {
 				if (reporter != null) {
 					final ProblemCollector collector = (ProblemCollector) mifo
 							.get(errorKey);
 					if (collector != null) {
 						collector.copyTo(reporter);
 					}
 				}
 			}
 		} else {
 			errorKey = null;
 			astKey = null;
 		}
 		if (moduleDeclaration == null) {
 			ISourceParser sourceParser = null;
 			sourceParser = DLTKLanguageManager.getSourceParser(toolkit
 					.getNatureId());
 			if (sourceParser != null) {
 				if (sourceParser instanceof ISourceParserExtension) {
 					((ISourceParserExtension) sourceParser).setFlags(flags);
 				}
 				final ProblemCollector collector = mifo != null ? new ProblemCollector()
 						: null;
 				try {
 					char[] sourceAsCharArray = module.getSourceAsCharArray();
 					moduleDeclaration = sourceParser.parse(module.getPath()
 							.toString().toCharArray(), sourceAsCharArray,
 							collector != null ? collector : reporter);
 					if (collector != null && reporter != null) {
 						collector.copyTo(reporter);
 					}
 					if (action != null) {
 						action.run(module, sourceAsCharArray);
 					}
 				} catch (ModelException e) {
 					final String msg = Messages.SourceParserUtil_errorRetrievingContent;
 					DLTKCore.error(msg, e);
 				}
 				if (moduleDeclaration != null && mifo != null) {
 					mifo.put(astKey, moduleDeclaration);
 					if (collector != null && !collector.isEmpty()) {
 						mifo.put(errorKey, collector);
 					} else {
 						mifo.remove(errorKey);
 					}
 				}
 			}
 		}
 		return moduleDeclaration;
 	}
 
 	/**
 	 * @param baseKey
 	 * @param flags
 	 * @return
 	 */
 	private static String getKey(String baseKey, int flags) {
 		return flags == 0 ? baseKey : baseKey + flags;
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
 		ModuleDeclaration moduleDeclaration = getModuleFromCache(mifo, flags);
 		if (moduleDeclaration == null) {
 			final ProblemCollector collector = mifo != null ? new ProblemCollector()
 					: null;
 			moduleDeclaration = sourceParser.parse(filename, content,
 					collector != null ? collector : reporter);
 			if (collector != null && reporter != null) {
 				collector.copyTo(reporter);
 			}
 			putModuleToCache(mifo, moduleDeclaration, flags, collector);
 		} else if (reporter != null) {
 			final ProblemCollector collector = (ProblemCollector) mifo
 					.get(getKey(ERRORS, flags));
 			if (collector != null) {
 				collector.copyTo(reporter);
 			}
 		}
 		return moduleDeclaration;
 	}
 
 	/**
 	 * This is for use in parsers.
 	 */
 	public static ModuleDeclaration getModuleFromCache(ISourceModuleInfo mifo,
 			int flags) {
 		if (mifo != null) {
 			return (ModuleDeclaration) mifo.get(getKey(AST, flags));
 		}
 		return null;
 	}
 
 	public static void putModuleToCache(ISourceModuleInfo info,
 			ModuleDeclaration module, int flags, ProblemCollector collector) {
 		if (info != null) {
 			info.put(getKey(AST, flags), module);
 			final String errorKey = getKey(ERRORS, flags);
 			if (collector != null && !collector.isEmpty()) {
 				info.put(errorKey, collector);
 			} else {
 				info.remove(errorKey);
 			}
 		}
 	}
 
 	public static void parseSourceModule(final ISourceModule module,
 			ISourceElementParser parser) {
 		ISourceModuleInfoCache sourceModuleInfoCache = ModelManager
 				.getModelManager().getSourceModuleInfoCache();
 		ISourceModuleInfo mifo = sourceModuleInfoCache.get(module);
 		if (module instanceof org.eclipse.dltk.compiler.env.ISourceModule) {
 			parser.parseSourceModule(
 					(org.eclipse.dltk.compiler.env.ISourceModule) module, mifo);
 		} else {
 			try {
 				parser.parseSourceModule(new CompilerSourceCode(module
 						.getSource()), mifo);
 			} catch (ModelException ex) {
				DLTKCore.error("Error parsing source module: " + module, ex);
 			}
 		}
 	}
 }
