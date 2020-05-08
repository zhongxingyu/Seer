 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.dsf.jstojava.parser;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.eclipse.mod.wst.jsdt.core.ast.IProgramElement;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.CompilationUnitDeclaration;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.FieldReference;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.MessageSend;
 import org.eclipse.mod.wst.jsdt.internal.compiler.ast.SingleNameReference;
 import org.eclipse.vjet.af.common.error.ErrorList;
 import org.eclipse.vjet.af.common.error.ErrorObject;
 import org.eclipse.vjet.dsf.common.exceptions.DsfRuntimeException;
 import org.eclipse.vjet.dsf.jsgen.shared.ids.ScopeIds;
 import org.eclipse.vjet.dsf.jsgen.shared.jstvalidator.DefaultJstProblem;
 import org.eclipse.vjet.dsf.jst.FileBinding;
 import org.eclipse.vjet.dsf.jst.IJstLib;
 import org.eclipse.vjet.dsf.jst.IJstParser;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.IScriptProblem;
 import org.eclipse.vjet.dsf.jst.ProblemSeverity;
 import org.eclipse.vjet.dsf.jst.SimpleBinding;
 import org.eclipse.vjet.dsf.jst.declaration.JstBlock;
 import org.eclipse.vjet.dsf.jst.declaration.JstCache;
 import org.eclipse.vjet.dsf.jst.declaration.JstFactory;
 import org.eclipse.vjet.dsf.jst.declaration.JstPackage;
 import org.eclipse.vjet.dsf.jst.declaration.JstType;
 import org.eclipse.vjet.dsf.jstojava.report.DefaultErrorReporter;
 import org.eclipse.vjet.dsf.jstojava.report.ErrorReporter;
 import org.eclipse.vjet.dsf.jstojava.resolver.TypeConstructorRegistry;
 import org.eclipse.vjet.dsf.jstojava.translator.BlockTranslator;
 import org.eclipse.vjet.dsf.jstojava.translator.TranslateConfig;
 import org.eclipse.vjet.dsf.jstojava.translator.TranslateCtx;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.JstCompletion;
 import org.eclipse.vjet.vjo.meta.VjoKeywords;
 
 public class VjoParser implements IJstParser {
 
 	@SuppressWarnings("unchecked")
 	public static final IJstType UNKNOWNUNIT =  JstFactory.getInstance().createJstType("UNKNOWN_TYPE", false);
 	private TranslateConfig m_config;
 
 	private boolean s_debug = false;
 
 	public VjoParser() {
 		m_config = new TranslateConfig();
 	}
 
 	public VjoParser(TranslateConfig cfg) {
 		m_config = cfg;
 	}
 
 	public IJstType parse(String groupName, String fileName, String source) {
 		return parseInternal(groupName, fileName, source, new TranslateCtx(
 				m_config), null);
 	}
 	
 	public IJstType parse(String groupName, String fileName, String source, TranslateCtx ctx) {
 		return parseInternal(groupName, fileName, source, ctx, null);
 	}
 
 	public IJstType parse(String groupName, URL url) {
 		return parse(groupName, url.getFile(), getContent(url));
 	}
 
 	public IJstType parse(String groupName, URL url,
 			boolean skiptImplementation) {
 		return parse(groupName, url.getFile(), getContent(url),
 				skiptImplementation);
 	}
 
 	public IJstType parse(String groupName, File file) {
 		return parseInternal(groupName, file.getAbsolutePath(),
 				getContent(file), file);
 	}
 
 	public IJstType parse(String groupName, File file, TranslateCtx ctx) {
 		return parseInternal(groupName, file.getAbsolutePath(),
 				getContent(file), ctx, file);
 	}
 
 	public IJstType parse(String groupName, File file,
 			boolean skiptImplementation) {
 		return parseInternal(groupName, file.getAbsolutePath(),
 				getContent(file), skiptImplementation, file);
 	}
 
 	public IJstType parse(String groupName, String fileName, File file,
 			TranslateCtx ctx) {
 		return parseInternal(groupName, fileName, getContent(file), ctx, file);
 	}
 
 	public IJstType parse(String groupName, String fileName, String source,
 			boolean skiptImplementation) {
 		return parseInternal(groupName, fileName, source, skiptImplementation,
 				null);
 	}
 
 //	public IScriptUnit parse(final String groupName, final String fileName,
 //			final String source, final TranslateCtx ctx) {
 //		return parseInternal(groupName, fileName, source, ctx, null);
 //	}
 
 	private IJstType parseInternal(String groupName, String fileName,
 			String source, File file) {
 		return parseInternal(groupName, fileName, source, new TranslateCtx(
 				m_config), file);
 	}
 
 	private IJstType parseInternal(String groupName, String fileName,
 			String source, boolean skiptImplementation, File file) {
 		TranslateConfig config = new TranslateConfig();
 		config.setSkiptImplementation(skiptImplementation);
 
 		return parseInternal(groupName, fileName, source, new TranslateCtx(
 				config), file);
 	}
 
 	private IJstType parseInternal(final String groupName,
 			final String fileName, final String source, final TranslateCtx ctx,
 			final File file) {
 		if (s_debug) {
 			printJstCacheTypes();
 		}
 
 		ctx.setGroup(groupName);
 	
 		Map<?, ?> settings = Collections.EMPTY_MAP;
 		final String mysource = convertHTMLCommentsToJsComments(source);
 		char[] charsource = mysource.toCharArray();
 
 		String encoding = null;
 		AstCompilationResult astResult = SyntaxTreeFactory2
 				.createASTCompilationResult(settings, charsource, fileName,
 						encoding);
 		CompilationUnitDeclaration ast = astResult
 				.getCompilationUnitDeclaration();
 		ctx.setAST(ast);
 		// Add errors to translate context -- making default if AST and JST are
 		// done together
 		SyntaxTreeFactory2.addAstErrorMessagesToCtx(mysource,
 				ast.compilationResult.getAllProblems(), ctx.getErrorReporter());
 		if (astResult.hasNonFakeTokenInsertionError()) {
 			ast = SyntaxTreeFactory2.fixedProblems(settings, charsource,
 					fileName, encoding, ast);
 			// ctx.setAST(ast);
 		}
 
 		JstBlock block = null;
 		List<JstBlock> createJstBlock = createJstBlock(ctx, ast);
 //		if (createJstBlock != null && createJstBlock.size() > 0) {
 //			block = createJstBlock.get(0);
 //		}
 
 		ctx.setScriptUnitBlockList(createJstBlock);
 		final IJstType type = SyntaxTreeFactory2.createJST(ast, ctx);
 
 		if (type == null) {
 			return null;
 		}
 		if (type.getPackage() == null && type instanceof JstType) {
 			((JstType) type).setPackage(new JstPackage());
 		}
 		final List<IScriptProblem> probs = createProblems(ctx);
 		type.setProblems(probs);
 		type.setJstBlockList(createJstBlock);
 		
 		// TODO use SimpleBinding when working on file/not saved
 		// when using source this could be an issue with memory.
 		
 		if (type.getSource() != null && type.getSource().getBinding() == null) {
 			if (file == null) { // binding simple source
 				type.getSource()
 						.setBinding(new SimpleBinding(fileName, source));
 			} else { // binding file
 				type.getSource().setBinding(new FileBinding(file));
 			}
		}else if(type.getSource()!=null && type.getSource().getBinding() instanceof SimpleBinding){
			type.getSource().setBinding(new SimpleBinding(fileName, source));
 		}
 		type.getPackage().setGroupName(groupName);
 		return type;
 
 	}
 
 	private void printJstCacheTypes() {
 		// TODO Auto-generated method stub
 		JstCache.getInstance().printTypes(System.out);
 	}
 
 //	private IWritableScriptUnit createScriptUnit(final JstBlock block,
 //			final List<JstBlock> blockList, final IJstType type,
 //			final List<IScriptProblem> probs) {
 //		IWritableScriptUnit unit = new WorkableScriptUnit(block,blockList,type,probs);
 //		postParse(unit);
 //		return unit;
 //	}
 
 	private List<JstBlock> createJstBlock(TranslateCtx ctx,
 			CompilationUnitDeclaration ast) {
 		IProgramElement[] statements = ast.getStatements();
 		List<JstBlock> blocks = new ArrayList<JstBlock>(statements.length);
 		for (IProgramElement elem : statements) {
 			if (isTypeDelaration(elem)) {
 				TranslateConfig cfg = new TranslateConfig();
 				cfg.setSkiptImplementation(true);
 				cfg.setSkipJsExtSyntaxArgs(true);
 				TranslateCtx ctx2 = new TranslateCtx(cfg);
 //				ctx2.setCreatedCompletion(true);
 				ctx2.setCompletionPos(ctx.getCompletionPos());
 				ctx2.setAST(ctx.getAST());
 				blocks.add(BlockTranslator.createJstBlock(ctx2, elem));
 				for(JstCompletion cmp : ctx2.getJstErrors()){
 					if(cmp.inScope(ScopeIds.METHOD_CALL)){
 						ctx.addBlockCompletion(cmp);
 					}
 //					ctx.addSyntaxError(cmp);
 				}
 			}
 		}
 		return blocks;
 	}
 	
 	private static boolean isTypeDelaration(IProgramElement pElement) {
 		
 		IProgramElement receiver = pElement;
 		IProgramElement pre = null;
 		while (receiver != null) {
 			if (receiver instanceof MessageSend) {
 				pre = receiver;
 				receiver = ((MessageSend) receiver).receiver;
 			} else if (receiver instanceof FieldReference) {
 				pre = receiver;
 				receiver = ((FieldReference) receiver).receiver;
 			} else if (receiver instanceof SingleNameReference) {
 				String name = null;
 				if (pre instanceof MessageSend) {
 					name = new String(((MessageSend)pre).getSelector());
 				}
 				else if (pre instanceof FieldReference) {
 					name = new String(((FieldReference)pre).getToken());
 				}
 				String receiverStr = ((SingleNameReference)receiver).toString();
 				String key = receiverStr +":" + name;
 				
 				boolean hasContructorResolver = false;
 				
 				hasContructorResolver = TypeConstructorRegistry.getInstance().hasResolver(key);
 				if(!hasContructorResolver){
 					String key2 = receiverStr +"::" + name;
 					hasContructorResolver = TypeConstructorRegistry.getInstance().hasResolver(key);
 					
 				}
 				
 				if(hasContructorResolver){
 					return true;
 				}
 				
 				// older way check
 				if (VjoKeywords.VJO.equals(receiverStr)) {
 					if (name != null && isValidTypeDef(name)) {
 						return true;
 					}
 				}
 				break;
 			} else {
 				break;
 			}
 		}
 		return false;
 	}
 
 	private List<IScriptProblem> createProblems(TranslateCtx ctx) {
 		ErrorReporter errorReporter = ctx.getErrorReporter();
 		List<IScriptProblem> problems = new ArrayList<IScriptProblem>();
 
 		ErrorList errList = errorReporter.getErrors();
 		ErrorList warnList = errorReporter.getWarnings();
 
 		addProblems(ctx, problems, errList, ProblemSeverity.error);
 		addProblems(ctx, problems, warnList, ProblemSeverity.warning);
 
 		return problems;
 	}
 
 	private void addProblems(TranslateCtx ctx,
 			List<IScriptProblem> problemList, ErrorList errList,
 			ProblemSeverity severity) {
 
 		@SuppressWarnings("unchecked")
 		Iterator<ErrorObject> itr = errList.listIterator();
 
 		while (itr.hasNext()) {
 			ErrorObject eo = itr.next();
 			int begin = getIntValue(eo, DefaultErrorReporter.BEGIN);
 			int end = getIntValue(eo, DefaultErrorReporter.END);
 			int line = getIntValue(eo, "line");
 			int col = getIntValue(eo, "column");
 			String message = eo.getParameters().getValueByName("message");
 			char[] fileName = ctx.getAST().getFileName();
 
 			IScriptProblem prblm = new DefaultJstProblem(null, null, message,
 					fileName, begin, end, line, col, severity);
 			problemList.add(prblm);
 		}
 	}
 
 	private static int getIntValue(ErrorObject errorObject, String name) {
 		String value = errorObject.getParameters().getValueByName(name);
 		return value == null ? 0 : Integer.parseInt(value);
 	}
 
 	private String convertHTMLCommentsToJsComments(String source) {
 		if (source.contains("<!--")) {
 			source = source.replace("<!--", "//--");
 		}
 		if (source.contains("-->")) {
 			source = source.replace("-->", "///");
 		}
 		return source;
 
 	}
 
 	public VjoParser addLib(IJstLib lib) {
 		JstCache.getInstance().addLib(lib);
 		return this;
 	}
 
 	public static String getContent(File file) {
 		InputStream inputStream;
 		try {
 			inputStream = new FileInputStream(file);
 		} catch (FileNotFoundException e) {
 			throw new DsfRuntimeException("can not open '"
 					+ file.getAbsolutePath() + "'", e);
 		}
 		return load(inputStream, file.getAbsolutePath());
 	}
 
 	public static String getContent(final URL url) {
 		final InputStream inputStream;
 		try {
 			inputStream = url.openStream();
 		} catch (IOException e) {
 			throw new DsfRuntimeException(url.toExternalForm(), e);
 		}
 		return load(inputStream, url.toExternalForm());
 	}
 
 	public static String load(InputStream inputStream, String source) {
 		final ByteArrayOutputStream os = new ByteArrayOutputStream(4096);
 		final byte[] buffer = new byte[4096];
 		try {
 			try {
 				do {
 					final int numBytesXferred = inputStream.read(buffer);
 					if (numBytesXferred == -1) {
 						break; // EOF
 					}
 					os.write(buffer, 0, numBytesXferred);
 				} while (true);
 			} finally {
 				inputStream.close();
 			}
 			return os.toString("utf-8");
 		} catch (IOException e) {
 			throw new DsfRuntimeException("can not load '" + source + "'", e);
 		}
 	}
 
 	//
 	// public List<IJstProblem> getErrors() {
 	// return convertErrorsToProblems(m_ctx.getAST().compilationResult
 	// .getAllProblems());
 	//
 	// }
 	//
 	// private List<IJstProblem> convertErrorsToProblems(CategorizedProblem[]
 	// categorizedProblems) {
 	// List<IJstProblem> problems = new ArrayList<IJstProblem>();
 	// for (CategorizedProblem cp : categorizedProblems) {
 	//
 	// ProblemTypeEnum type = ProblemTypeEnum.warning;
 	// if (cp.isError()) {
 	// type = ProblemTypeEnum.error;
 	// }
 	//			
 	// problems.add(new DefaultJstProblem(type, cp.getMessage(), m_charsource,
 	// cp
 	// .getSourceStart(), cp.getSourceEnd()));
 	// }
 	//		
 	// return problems;
 	//		
 	//		
 	// }
 
 	public TranslateCtx getDefaultTranslateCtx() {
 		return new TranslateCtx(m_config);
 	}
 
 	public void setDebug(boolean debug) {
 		this.s_debug = debug;
 	}
 	
 	private static Set<String> VJO_TYPE_DEF_KEYS = new HashSet<String>();
 	static {
 		VJO_TYPE_DEF_KEYS.add(VjoKeywords.TYPE);
 		VJO_TYPE_DEF_KEYS.add(VjoKeywords.CTYPE);
 		VJO_TYPE_DEF_KEYS.add(VjoKeywords.ITYPE);
 		VJO_TYPE_DEF_KEYS.add(VjoKeywords.FTYPE);
 		VJO_TYPE_DEF_KEYS.add(VjoKeywords.MTYPE);
 		VJO_TYPE_DEF_KEYS.add(VjoKeywords.OTYPE);
 		VJO_TYPE_DEF_KEYS.add(VjoKeywords.ETYPE);
 		VJO_TYPE_DEF_KEYS.add(VjoKeywords.LTYPE);
 		VJO_TYPE_DEF_KEYS.add(VjoKeywords.NEEDS);
 	}
 	
 	private static boolean isValidTypeDef(String name) {
 		return VJO_TYPE_DEF_KEYS.contains(name);
 	}
 
 }
