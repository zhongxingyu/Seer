 /*******************************************************************************
  * Copyright (c) 2005, 2007 IBM Corporation and others.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  
  *******************************************************************************/
 package org.eclipse.vjet.eclipse.internal.ui.text;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Iterator;
 import java.util.List;
 
 import org.eclipse.dltk.mod.core.ISourceModule;
 import org.eclipse.dltk.mod.internal.core.VjoSourceModule;
 import org.eclipse.dltk.mod.ui.text.IColorManager;
 import org.eclipse.jface.preference.IPreferenceStore;
 import org.eclipse.jface.text.BadLocationException;
 import org.eclipse.jface.text.IDocument;
 import org.eclipse.jface.text.rules.ICharacterScanner;
 import org.eclipse.jface.text.rules.IRule;
 import org.eclipse.jface.text.rules.IToken;
 import org.eclipse.jface.text.rules.SingleLineRule;
 import org.eclipse.jface.text.rules.Token;
 import org.eclipse.swt.graphics.Point;
 import org.eclipse.vjet.dsf.jst.IJstNode;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.JstSource;
 import org.eclipse.vjet.dsf.jst.declaration.JstBlock;
 import org.eclipse.vjet.dsf.jst.declaration.JstMethod;
 import org.eclipse.vjet.dsf.jst.declaration.JstPackage;
 import org.eclipse.vjet.dsf.jst.reserved.JsCoreKeywords;
 import org.eclipse.vjet.dsf.jst.term.JstIdentifier;
 import org.eclipse.vjet.eclipse.core.VjetPlugin;
 import org.eclipse.vjet.eclipse.core.parser.VjoParserToJstAndIType;
 import org.eclipse.vjet.vjo.meta.VjoKeywords;
 
 public class VjoCodeScanner extends JavascriptCodeScanner {
 	private static String fgTokenProperties[] = new String[] {
 			VjetColorConstants.VJET_STRING,
 			VjetColorConstants.VJET_REGEXP_CORE,
 			VjetColorConstants.VJET_KEYWORD };
 
 	private final static String[] VJO_KEYWORDS = {
 			JsCoreKeywords.THIS + "." + VjoKeywords.BASE,
 			JsCoreKeywords.THIS + "." + VjoKeywords.VJ$ + "."
 					+ VjoKeywords.PARENT,
 			JsCoreKeywords.THIS + "." + VjoKeywords.VJ$ + "."
 					+ VjoKeywords.OUTER,
 			JsCoreKeywords.THIS + "." + vjo(VjoKeywords.NEEDS_IMPL) };
 
 	private final static String[] VJO_FUNCTION_COMPOSITE_KEYWORDS = {
 			dot(VjoKeywords.NEEDSLIB),dot(VjoKeywords.NEEDS), vjo(VjoKeywords.OTYPE),
 			vjo(VjoKeywords.TYPE), dot(VjoKeywords.PROPS),
 			dot(VjoKeywords.PROTOS), dot(VjoKeywords.DEFS),
 			dot(VjoKeywords.INHERITS), dot(VjoKeywords.INITS),
 			dot(VjoKeywords.SATISFIES), vjo(VjoKeywords.ITYPE),
 			vjo(VjoKeywords.ETYPE), 
 			vjo(VjoKeywords.LTYPE),
 			vjo(VjoKeywords.MTYPE),
 			vjo(VjoKeywords.CTYPE),
 			vjo(VjoKeywords.FTYPE),
 		    dot(VjoKeywords.MIXIN),
 		    dot(VjoKeywords.OPTIONS),
 //			dot(VjoKeywords.MIXINPROPS), dot(VjoKeywords.EXPECTS),
 			dot(VjoKeywords.GLOBALS),
 			dot(VjoKeywords.VALUES), dot(VjoKeywords.ENDTYPE) };
 
 	private final static List<String> VJO_FUNCTION_KEYWORDS = new ArrayList<String>();
 	static {
 		VJO_FUNCTION_KEYWORDS.add(VjoKeywords.NEEDS);
 		VJO_FUNCTION_KEYWORDS.add(VjoKeywords.GLOBALS);
 		VJO_FUNCTION_KEYWORDS.add(VjoKeywords.OTYPE);
 		VJO_FUNCTION_KEYWORDS.add(VjoKeywords.TYPE);
 		VJO_FUNCTION_KEYWORDS.add(VjoKeywords.PROPS);
 		VJO_FUNCTION_KEYWORDS.add(VjoKeywords.PROTOS);
 		VJO_FUNCTION_KEYWORDS.add(VjoKeywords.OPTIONS);
 		VJO_FUNCTION_KEYWORDS.add(VjoKeywords.DEFS);
 		VJO_FUNCTION_KEYWORDS.add(VjoKeywords.INHERITS);
 		VJO_FUNCTION_KEYWORDS.add(VjoKeywords.INITS);
 		VJO_FUNCTION_KEYWORDS.add(VjoKeywords.SATISFIES);
 		VJO_FUNCTION_KEYWORDS.add(VjoKeywords.ITYPE);
 		VJO_FUNCTION_KEYWORDS.add(VjoKeywords.ETYPE);
 		VJO_FUNCTION_KEYWORDS.add(VjoKeywords.LTYPE);
 		VJO_FUNCTION_KEYWORDS.add(VjoKeywords.MTYPE);
 		VJO_FUNCTION_KEYWORDS.add(VjoKeywords.CTYPE);
 		VJO_FUNCTION_KEYWORDS.add(VjoKeywords.FTYPE);
 		VJO_FUNCTION_KEYWORDS.add(VjoKeywords.MAKE_FINAL);
 		VJO_FUNCTION_KEYWORDS.add(VjoKeywords.MIXIN);
 //		VJO_FUNCTION_KEYWORDS.add(VjoKeywords.MIXINPROPS);
 		VJO_FUNCTION_KEYWORDS.add(VjoKeywords.EXPECTS);
 		VJO_FUNCTION_KEYWORDS.add(VjoKeywords.VALUES);
 		VJO_FUNCTION_KEYWORDS.add(VjoKeywords.ENDTYPE);
 		VJO_FUNCTION_KEYWORDS.add(VjoKeywords.VJO);
 	}
 
 	private static String dot(String keyword) {
 		return "." + keyword;
 	}
 
 	private static String vjo(String keyword) {
 		return VjoKeywords.VJO + "." + keyword;
 	}
 
 	private List<Point> hightPoints = new ArrayList<Point>();
 
 	private PointComparator pointComparator = new PointComparator();
 
 	private String fContent;
 
 	private ISourceModule m_module;
 
 	public void setModule(ISourceModule mModule) {
 		m_module = mModule;
 	}
 
 	public VjoCodeScanner(IColorManager manager, IPreferenceStore store) {
 		super(manager, store);
 	}
 
 	protected String[] getTokenProperties() {
 		String[] tokens = super.getTokenProperties();
 		String[] newTokens = new String[tokens.length + 4];
 		System.arraycopy(tokens, 0, newTokens, 0, tokens.length);
 		System.arraycopy(fgTokenProperties, 0, newTokens, tokens.length,
 				fgTokenProperties.length);
 		return newTokens;
 	}
 
 	@Override
 	@SuppressWarnings(value = { "unchecked" })
 	protected List createRules() {
 		List rules = super.createRules();
 		IToken keyword = getToken(VjetColorConstants.VJET_KEYWORD);
 
 		IRule rule = new VjoKeywordRule(VJO_KEYWORDS, keyword);
 		rules.add(0, rule);
 		// for (String word : VJO_KEYWORDS) {
 		// IRule rule = new VjoKeywordRule(word, keyword);
 		// rules.add(0, rule);
 		// }
 
 		rule = new VjoFunctionKeywordRule(VJO_FUNCTION_COMPOSITE_KEYWORDS,
 				keyword);
 		rules.add(0, rule);
 		// for (String word : VJO_FUNCTION_KEYWORDS) {
 		// IRule rule = new VjoFunctionKeywordRule(word, keyword);
 		// rules.add(0, rule);
 		// }
 
 		// exclude vjo.needs("")
 		rules.add(0, new SingleLineRule(vjo(VjoKeywords.NEEDS), "v", keyword,
 				'.'));
 
 		// new RegExp("s$"); or /( [' "] ) [^ ' "] * \1/
 		IToken reg = getToken(VjetColorConstants.VJET_REGEXP_CORE);
 		rules.add(new RegexRule(reg, '\\'));
 
 		return rules;
 	}
 
 	private IDocument getDocument() {
 		return fDocument;
 	}
 
 	class RegexRule extends SingleLineRule {
 
 		/*
 		 * @see SingleLineRule
 		 */
 		public RegexRule(IToken token) {
 			super("/", "/", token, (char) 0); //$NON-NLS-2$ //$NON-NLS-1$
 		}
 
 		/*
 		 * @see SingleLineRule
 		 */
 		public RegexRule(IToken token, char escapeCharacter) {
 			super("/", "/", token, escapeCharacter); //$NON-NLS-2$ //$NON-NLS-1$
 		}
 
 		private IToken evaluateToken(ICharacterScanner scanner) {
 			try {
 				final String token = getDocument().get(getTokenOffset(),
 						getTokenLength())
 						+ "."; //$NON-NLS-1$
 
 				int offset = 0;
 				char character = token.charAt(++offset);
 
 				while (Character.isWhitespace(character))
 					character = token.charAt(++offset);
 
 				while (character != '/' && offset < token.length() - 1)
 					character = token.charAt(++offset);
 
 				while (Character.isWhitespace(character))
 					character = token.charAt(++offset);
 
 				if (offset >= 2 && token.charAt(offset) == fEndSequence[0]) {
 					int c = scanner.read();
 					if (c != 'g' && c != 'i') {
 						scanner.unread();
 					} else {
 						// read second char if read first char
 						int secondChar = scanner.read();
 						if ((secondChar == 'g' || secondChar == 'i')
 								&& c != secondChar) {
 
 						} else {
 							scanner.unread();
 						}
 					}
 
 					return fToken;
 				}
 
 			} catch (BadLocationException exception) {
 				// Do nothing
 			}
 			return Token.UNDEFINED;
 		}
 
 		/*
 		 * @see PatternRule#evaluate(ICharacterScanner)
 		 */
 		public IToken evaluate(ICharacterScanner scanner) {
 			IToken result = super.evaluate(scanner);
 			if (result == fToken)
 				return evaluateToken(scanner);
 			return result;
 		}
 	}
 
 	public Point[] getHighlightPoints() {
 		Point[] result = new Point[hightPoints.size()];
 		hightPoints.toArray(result);
 		return result;
 	}
 
 	@Override
 	public void setRange(IDocument document, int offset, int length) {
 		if (documentChanged(document)) {
 			hightPoints.clear();
 			initHighLightPoints(document);
 		}
 		super.setRange(document, offset, length);
 	}
 
 	private boolean documentChanged(IDocument document) {
 		String content = document.get();
 		boolean bol;
 		if (fContent == null) {
 			bol = true;
 		} else {
 			bol = !fContent.equals(content);
 		}
 		if (bol) {
 			fContent = content;
 		}
 		return bol;
 	}
 
 	private void initHighLightPoints(IDocument document) {
 		String content = document.get();
 		
 		String groupName = null;
 		String typeName = null;
 		if(m_module instanceof VjoSourceModule){
 			VjoSourceModule module = ((VjoSourceModule) m_module);
			IJstType jstType = module.getJstType();
 			if(jstType==null){
 				return;
 			}
 			JstPackage package1 = jstType.getPackage();
 			if(package1==null){
 				return;
 			}			
 			groupName = package1.getGroupName();
 			typeName =  jstType.getName();
 		}else{
 			
 			return;
 			
 		}
 	
		VjoParserToJstAndIType parser = new VjoParserToJstAndIType();
		if(VjetPlugin.TRACE_PARSER){
			System.out.println("parsing for " + getClass().getName());
		}
		IJstType scriptUnit = parser.parse(groupName, typeName, content);
		List<JstIdentifier> identifiers = getJstIdentifierFromScriptUnit(scriptUnit);
 		if (identifiers.isEmpty()) {
 			return;
 		}
 		Iterator<JstIdentifier> it = identifiers.iterator();
 		while (it.hasNext()) {
 			JstIdentifier identifer = it.next();
 			String name = identifer.getName();
 			if (!VJO_FUNCTION_KEYWORDS.contains(name)) {
 				continue;
 			}
 			IJstNode node = identifer.getJstBinding();
 			if(node instanceof JstMethod){
 				node = ((JstMethod)node).getOwnerType();
 			}
 			
 			if(node instanceof IJstType){
 				IJstType t = (IJstType) node;
 				String typename = t.getName();
 				if(!typename.startsWith("vjo.") && !typename.equals("vjo") && !typename.equals("type::vjo")){
 					continue;
 				}
 			}
 			
 			JstSource source = identifer.getSource();
 			if (source != null) {
 				hightPoints.add(new Point(source.getStartOffSet(), source
 						.getEndOffSet()));
 			}
 		}
 		Collections.sort(hightPoints, pointComparator);
 	}
 
 	private List<JstIdentifier> getJstIdentifierFromScriptUnit(IJstType unit) {
 		List<JstBlock> blocks = unit.getJstBlockList();
 		if (blocks == null) {
 			return Collections.emptyList();
 		}
 		List<JstIdentifier> result = new ArrayList<JstIdentifier>();
 		Iterator<JstBlock> it = blocks.iterator();
 		while (it.hasNext()) {
 			JstBlock block = it.next();
 			innerSFindIdentifer(block, result);
 		}
 		return result;
 	}
 
 	private void innerSFindIdentifer(IJstNode node, List<JstIdentifier> result) {
 		if (node == null) {
 			return;
 		}
 		if (node instanceof JstIdentifier) {
 			JstIdentifier identifer = (JstIdentifier) node;
 			if (!result.contains(identifer)) {
 				result.add(identifer);
 			}
 		}
 		List<? extends IJstNode> nodes = node.getChildren();
 		if (nodes.isEmpty()) {
 			return;
 		}
 		Iterator<? extends IJstNode> it = nodes.iterator();
 		while (it.hasNext()) {
 			IJstNode temp = it.next();
 			innerSFindIdentifer(temp, result);
 		}
 
 	}
 
 	class PointComparator implements Comparator<Point> {
 
 		@Override
 		public int compare(Point object1, Point object2) {
 			return object1.x - object2.x;
 		}
 
 	}
 
 	public boolean isValidVjo() {
 		return !hightPoints.isEmpty();
 	}
 
 }
