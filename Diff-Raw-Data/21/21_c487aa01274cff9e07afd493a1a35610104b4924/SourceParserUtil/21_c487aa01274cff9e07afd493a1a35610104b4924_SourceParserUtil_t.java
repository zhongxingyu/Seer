 package org.eclipse.dltk.core;
 
 import org.eclipse.dltk.ast.declarations.ModuleDeclaration;
 import org.eclipse.dltk.ast.declarations.ModuleDeclarationWrapper;
 import org.eclipse.dltk.ast.parser.ASTCacheManager;
 import org.eclipse.dltk.ast.parser.IASTCache;
 import org.eclipse.dltk.ast.parser.IASTCache.ASTCacheEntry;
 import org.eclipse.dltk.ast.parser.IModuleDeclaration;
 import org.eclipse.dltk.ast.parser.ISourceParser;
 import org.eclipse.dltk.compiler.CharOperation;
 import org.eclipse.dltk.compiler.env.IModuleSource;
 import org.eclipse.dltk.compiler.problem.IProblemReporter;
 import org.eclipse.dltk.compiler.problem.ProblemCollector;
 import org.eclipse.dltk.compiler.util.Util;
 import org.eclipse.dltk.core.ISourceModuleInfoCache.ISourceModuleInfo;
 import org.eclipse.dltk.core.RuntimePerformanceMonitor.PerformanceNode;
 import org.eclipse.dltk.internal.core.ModelManager;
 
 public class SourceParserUtil {
 	private static final String AST = "ast"; //$NON-NLS-1$
 	private static final String ERRORS = "errors"; //$NON-NLS-1$
 
 	private static boolean useASTCaching = true;
 	private static boolean useASTPersistenceCaching = true;
 
 	public static IModuleDeclaration parse(IModuleSource module,
 			String natureId, IProblemReporter reporter) {
 		final IModelElement element = module.getModelElement();
 		final ISourceParser parser = DLTKLanguageManager.getSourceParser(
 				element != null ? element.getScriptProject().getProject()
 						: null, natureId);
 		return parser.parse(module, reporter);
 	}
 
 	public static IModuleDeclaration parse(final ISourceModule module,
 			IProblemReporter reporter) {
 		final IDLTKLanguageToolkit toolkit = DLTKLanguageManager
 				.getLanguageToolkit(module);
 		if (toolkit == null) {
 			return null;
 		}
 		IModuleDeclaration moduleDeclaration = null;
 		PerformanceNode p1 = RuntimePerformanceMonitor.begin();
 
 		final String errorKey;
 		final String astKey;
 		final ISourceModuleInfo mifo = getCache().get(module);
 		if (mifo != null && useASTCaching) {
 			errorKey = getKey(ERRORS, 0);
 			astKey = getKey(AST, 0);
 			moduleDeclaration = (IModuleDeclaration) mifo.get(astKey);
 			if (moduleDeclaration != null) {
 				if (reporter != null) {
 					final ProblemCollector collector = (ProblemCollector) mifo
 							.get(errorKey);
 					if (collector != null) {
 						collector.copyTo(reporter);
 					}
 				}
 			}
 			if (moduleDeclaration == null && useASTPersistenceCaching) {
 				// Try to retrieve information from persistence cache.
 				IASTCache[] providers = ASTCacheManager.getProviders(toolkit
 						.getNatureId());
 				if (providers != null) {
 					for (IASTCache provider : providers) {
 						ASTCacheEntry restored = provider.restoreModule(module);
 						if (restored != null) {
 							if (reporter != null) {
 								if (restored.problems != null) {
 									restored.problems.copyTo(reporter);
 								}
 							}
 							// Store to local cache.
 							mifo.put(astKey, restored.module);
 							if (restored.problems != null
 									&& !restored.problems.isEmpty()) {
 								mifo.put(errorKey, restored.problems);
 							} else {
 								mifo.remove(errorKey);
 							}
 							moduleDeclaration = restored.module;
 							break;
 						}
 					}
 				}
 			}
 		} else {
 			errorKey = null;
 			astKey = null;
 		}
 		p1.done(toolkit.getNatureId(), "Retrive AST from cache", 0);
 		if (moduleDeclaration == null) {
 			p1.renew();
 			ISourceParser sourceParser = DLTKLanguageManager.getSourceParser(
 					module.getScriptProject().getProject(),
 					toolkit.getNatureId());
 			if (sourceParser != null) {
 				// if (sourceParser instanceof ISourceParserExtension) {
 				// ((ISourceParserExtension) sourceParser).setFlags(flags);
 				// }
 				final ProblemCollector collector = mifo != null ? new ProblemCollector()
 						: null;
 				final IModuleSource source;
 				if (module instanceof IModuleSource) {
 					source = (IModuleSource) module;
 				} else {
 					source = new IModuleSource() {
 
 						public String getFileName() {
 							return module.getPath().toString();
 						}
 
 						public String getSourceContents() {
 							try {
 								return module.getSource();
 							} catch (ModelException e) {
 								return Util.EMPTY_STRING;
 							}
 						}
 
 						public IModelElement getModelElement() {
 							return module;
 						}
 
 						public char[] getContentsAsCharArray() {
 							try {
 								return module.getSourceAsCharArray();
 							} catch (ModelException e) {
 								return CharOperation.NO_CHAR;
 							}
 						}
 					};
 				}
 				moduleDeclaration = sourceParser.parse(source,
 						collector != null ? collector : reporter);
 				if (collector != null && reporter != null) {
 					collector.copyTo(reporter);
 				}
 				p1.done(toolkit.getNatureId(), "AST parse time", 0);
 				if (moduleDeclaration != null && mifo != null && useASTCaching) {
 					mifo.put(astKey, moduleDeclaration);
 					if (useASTPersistenceCaching) {
 						// Store to persistence cache
 						IASTCache[] providers = ASTCacheManager
 								.getProviders(toolkit.getNatureId());
 						if (providers != null) {
 							for (IASTCache provider : providers) {
 								provider.storeModule(module, moduleDeclaration,
 										collector);
 							}
 						}
 					}
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
 
 	public static ModuleDeclaration getModuleDeclaration(ISourceModule module) {
		return getModuleDeclaration(module, null);
 	}
 
 	public static ModuleDeclaration getModuleDeclaration(ISourceModule module,
 			IProblemReporter reporter) {
		return wrap(parse(module, reporter));
 	}
 
 	@Deprecated
 	public static ModuleDeclaration getModuleDeclaration(ISourceModule module,
 			IProblemReporter reporter, int flags) {
		return getModuleDeclaration(module, reporter);
 	}
 
 	@Deprecated
 	public static ModuleDeclaration getModuleDeclaration(ISourceModule module,
 			IProblemReporter reporter, ISourceModuleInfo mifo) {
		return getModuleDeclaration(module, reporter);
 	}
 
 	@Deprecated
 	public static ModuleDeclaration getModuleDeclaration(ISourceModule module,
 			IProblemReporter reporter, ISourceModuleInfo mifo, int flags) {
		return getModuleDeclaration(module, reporter);
 	}
 
 	/**
 	 * @param baseKey
 	 * @param flags
 	 * @return
 	 */
 	private static String getKey(String baseKey, int flags) {
 		return flags == 0 ? baseKey : baseKey + flags;
 	}
 
 	/**
 	 * Parse the specified source
 	 * 
 	 * @param module
 	 * @param nature
 	 * @param reporter
 	 * @return
 	 */
 	public static ModuleDeclaration getModuleDeclaration(IModuleSource module,
 			String natureId, IProblemReporter reporter) {
 		final IModuleDeclaration result = parse(module, natureId, reporter);
 		return wrap(result);
 	}
 
 	private static ModuleDeclaration wrap(final IModuleDeclaration result) {
 		if (result != null) {
 			if (result instanceof ModuleDeclaration) {
 				return (ModuleDeclaration) result;
 			}
 			return new ModuleDeclarationWrapper(result);
 		} else {
 			return null;
 		}
 	}
 
 	/**
 	 * This is for use in parsers.
 	 */
 	public static IModuleDeclaration getModuleFromCache(ISourceModuleInfo mifo,
 			IProblemReporter reporter) {
 		final int flags = 0;// FIXME remove later
 		if (mifo != null && useASTCaching) {
 			final IModuleDeclaration moduleDeclaration = (IModuleDeclaration) mifo
 					.get(getKey(AST, flags));
 			if (moduleDeclaration != null && reporter != null) {
 				final ProblemCollector collector = (ProblemCollector) mifo
 						.get(getKey(ERRORS, flags));
 				if (collector != null) {
 					collector.copyTo(reporter);
 				}
 			}
 			return moduleDeclaration;
 		}
 		return null;
 	}
 
 	public static void putModuleToCache(ISourceModuleInfo info,
 			IModuleDeclaration module, ProblemCollector collector) {
 		final int flags = 0; // TODO remove later
 		if (info != null && useASTCaching) {
 			info.put(getKey(AST, flags), module);
 			final String errorKey = getKey(ERRORS, flags);
 			if (collector != null && !collector.isEmpty()) {
 				info.put(errorKey, collector);
 			} else {
 				info.remove(errorKey);
 			}
 		}
 	}
 
 	/**
 	 * Performance testing only
 	 */
 	public static void disableCache() {
 		useASTCaching = false;
 	}
 
 	public static void enableCache() {
 		useASTCaching = true;
 	}
 
 	public static void clearCache() {
 		getCache().clear();
 	}
 
 	public static ISourceModuleInfoCache getCache() {
 		return ModelManager.getModelManager().getSourceModuleInfoCache();
 	}
 }
