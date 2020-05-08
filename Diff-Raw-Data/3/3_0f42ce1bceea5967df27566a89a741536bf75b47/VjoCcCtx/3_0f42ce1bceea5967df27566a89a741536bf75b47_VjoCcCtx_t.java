 /*******************************************************************************
  * Copyright (c) 2005, 2012 eBay Inc.
  * All rights reserved. This program and the accompanying materials
  * are made available under the terms of the Eclipse Public License v1.0
  * which accompanies this distribution, and is available at
  * http://www.eclipse.org/legal/epl-v10.html
  *
  *******************************************************************************/
 package org.eclipse.vjet.vjo.tool.codecompletion;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 
 import org.eclipse.vjet.dsf.jsgen.shared.ids.ScopeIds;
 import org.eclipse.vjet.dsf.jsgen.shared.validation.common.ScopeId;
 import org.eclipse.vjet.dsf.jst.BaseJstNode;
 import org.eclipse.vjet.dsf.jst.IJstMethod;
 import org.eclipse.vjet.dsf.jst.IJstNode;
 import org.eclipse.vjet.dsf.jst.IJstProperty;
 import org.eclipse.vjet.dsf.jst.IJstRefType;
 import org.eclipse.vjet.dsf.jst.IJstType;
 import org.eclipse.vjet.dsf.jst.IJstTypeReference;
 import org.eclipse.vjet.dsf.jst.JstSource;
 import org.eclipse.vjet.dsf.jst.declaration.JstArg;
 import org.eclipse.vjet.dsf.jst.declaration.JstBlock;
 import org.eclipse.vjet.dsf.jst.declaration.JstTypeReference;
 import org.eclipse.vjet.dsf.jst.declaration.JstVars;
 import org.eclipse.vjet.dsf.jst.expr.FieldAccessExpr;
 import org.eclipse.vjet.dsf.jst.expr.MtdInvocationExpr;
 import org.eclipse.vjet.dsf.jst.term.JstIdentifier;
 import org.eclipse.vjet.dsf.jst.term.ObjLiteral;
 import org.eclipse.vjet.dsf.jst.term.SimpleLiteral;
 import org.eclipse.vjet.dsf.jst.token.IExpr;
 import org.eclipse.vjet.dsf.jst.ts.JstTypeSpaceMgr;
 import org.eclipse.vjet.dsf.jstojava.translator.JstUtil;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.IJstCompletion;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.JstComletionOnMessageSend;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.JstCommentCompletion;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.JstCompletion;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.JstCompletionOnQualifiedNameReference;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.JstCompletionOnSingleNameReference;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.JstFieldOrMethodCompletion;
 import org.eclipse.vjet.dsf.jstojava.translator.robust.completion.JstKeywordCompletion;
 import org.eclipse.vjet.dsf.ts.type.TypeName;
 import org.eclipse.vjet.vjo.lib.LibManager;
 import org.eclipse.vjet.vjo.meta.VjoKeywords;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcConstructorGenProposalAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcDerivedPropMethodAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcFunctionGenProposalAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcOverrideProposalAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcPropMethodProposalAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.advisor.VjoCcStaticPropMethodProposalAdvisor;
 import org.eclipse.vjet.vjo.tool.codecompletion.comment.VjoCcCommentUtil;
 import org.eclipse.vjet.vjo.tool.codecompletion.handler.VjoCcAdvisorSorter;
 import org.eclipse.vjet.vjo.tool.codecompletion.reporter.VjoCcReporter;
 import org.eclipse.vjet.vjo.tool.typespace.TypeSpaceMgr;
 
 /**
  * Extend VjoValidationCtx, to support special requirement of vjo cc.
  * 
  * 
  * 
  */
 public class VjoCcCtx {
 
 	public static final int POSITION_UNKNOWN = 0;
 	public static final int POSITION_AFTER_THIS = 1;
 	public static final int POSITION_AFTER_THISVJO = 2;
 	public static final int POSITION_AFTER_THISVJOTYPE = 3;
 	public static final String INFO_KEY_IN_TYPE_SCOPE = "TYPE_SHOW_FULLNAME";
 	public static final String INFO_KEY_PARAMETER_HINT = "PARAMETER_HINT_MTDINVOEXPR";
 	public static final String INFO_KEY_ARGUMENT = "ARGUMENT_MTDINVOEXPR";
 
 	private List<String> matchAdvisorList = new ArrayList<String>();
 
 	private JstCompletion completion;
 
 	private IJstType actingType;
 
 	private int offset;
 
 	private String m_content;
 
 	private TypeName m_typeName;
 
 	private Map<String, Object> infoMap = new HashMap<String, Object>();
 
 	private IVjoCcReporter reporter = new VjoCcReporter();
 
 	private String actingToken;
 	/**
 	 * com.ebay.a.b.c.<cursor>, when propose the package, the full name is
 	 * token
 	 * 
 	 */
 	private String actingPackageToken;
 
 	private JstTypeSpaceMgr m_jstTypeSpaceMgr;
 
 	private IJstType calledType;
 
 	private IJstType m_scriptUnit;
 
 	private List<IJstNode> m_scriptNodes = new ArrayList<IJstNode>();
 	private List<JstIdentifier> m_identifers = new ArrayList<JstIdentifier>();
 
 	// cache two frequently used types
 	private IJstType m_objType = null;
 	private IJstType m_globalType = null;
 
 	// this is cached "vjo" type
 	private IJstType vjoType;
 
 	private int positionType = POSITION_UNKNOWN;
 
 	private boolean mtypeEabled = false;
 	/**
 	 * Store the result advisors
 	 */
 	private List<String> advisors;
 	
 	/**
 	 * Mark current content is a common js instead of vjo type
 	 */
 	private boolean commonJsCtx = false;
 
 	public boolean isCommonJsCtx() {
 		return commonJsCtx;
 	}
 
 	public void setCommonJsCtx(boolean commonJsCtx) {
 		this.commonJsCtx = commonJsCtx;
 	}
 
 	public VjoCcCtx(JstTypeSpaceMgr jstTypeSpaceMgr, TypeName name) {
 		this.m_jstTypeSpaceMgr = jstTypeSpaceMgr;
 		this.m_typeName = name;
 	}
 	
 
 	public IJstNode getJstNode() {
 		return ((JstCompletion) completion).getParentNode();
 	}
 
 	/**
 	 * Opened to advisor and handler, to enable advisor transfer their special
 	 * information to the caller
 	 * 
 	 * @param key
 	 * @param info
 	 */
 	public void putInfo(String key, Object info) {
 		infoMap.put(key, info);
 	}
 
 	public IVjoCcReporter getReporter() {
 		return reporter;
 	}
 
 	public List<IVjoCcProposalData> getProposals() {
 		List<IVjoCcProposalData> datas = reporter.getProposalData();
 		if (!datas.isEmpty()) {
 			Collections.sort(datas, new VjoCcAdvisorSorter(getActingToken()));
 		}
 		return datas;
 	}
 
 	public IJstType getJstType() {
 		if (actingType != null) {
 			return actingType;
 		} else {
 			IJstType type = completion.getOwnerType();
 			if (type == null) {
 				type = completion.getParentNode();
 			}
 			return type;
 		}
 	}
 
 	public JstCompletion getCompletion() {
 		return completion;
 	}
 
 	public boolean isInStaticMethod() {
 		return isInStatic() && completion.inScope(ScopeIds.METHOD);
 	}
 
 	public void setCompletion(IJstCompletion completion) {
 		this.completion = (JstCompletion) completion;
 	}
 
 	public String getToken() {
 		return getActingToken();
 	}
 
 	public JstTypeSpaceMgr getJstTypeSpaceMgr() {
 		return m_jstTypeSpaceMgr;
 	}
 
 	/**
 	 * Calculate the JstMethod that contains the selected node,it can be used to
 	 * calculate the method argument and variable
 	 * 
 	 * @return
 	 */
 	public IJstMethod getContaindMethod() {
 		IJstNode node = getJstNode();
 		while (!(node instanceof IJstMethod) && !(node instanceof IJstType)) {
 			node = node.getParentNode();
 		}
 		if (node instanceof IJstMethod) {
 			return (IJstMethod) node;
 		} else {
 			return null;
 		}
 	}
 
 	public boolean isInStatic() {
 		// To support inner type, get the recent scopeId
 		Stack<ScopeId> ss = completion.getScopeStack();
 		Iterator<ScopeId> it = ss.iterator();
 		ScopeId lastScope = null;
 		while (it.hasNext()) {
 			ScopeId scope = it.next();
 			if (ifConsider(scope)) {
 				lastScope = scope;
 			}
 		}
 		return lastScope == null || lastScope != ScopeIds.PROTOS;
 
 	}
 
 	private boolean ifConsider(ScopeId scope) {
 		if (scope == ScopeIds.PROPS || scope == ScopeIds.PROTOS
 				|| scope == ScopeIds.DEFS || scope == ScopeIds.INITS) {
 			return true;
 		}
 		return false;
 	}
 
 	/**
 	 * used to analyse current class, to get the right class or instance, which
 	 * used to fullfil JstCompletionOnQualifiedNameReference
 	 * 
 	 * @return
 	 */
 	public IJstType getCalledType() {
 		if (calledType != null) {
 			return calledType;
 		}
 		if (completion == null) {
 			return getActingType();
 		}
 		String token = completion.getToken();
 		if (StringUtils.isBlankOrEmpty(token)) {
 			return getActingType();
 		}
 		IJstNode node = getJstNode();
 		IJstType type = getTypeFromNode(node);
 		return type;
 	}
 
 	public IJstType getActingType() {
 		if (actingType == null) {
 			if(completion == null){
 				return null;
 			}
 			else if (completion.getOwnerType() != null) {
 				return completion.getOwnerType();
 			} else {
 				return completion.getParentNode();
 			}
 		}
 		return actingType;
 	}
 
 	public void setActingType(IJstType actingType) {
 		this.actingType = actingType;
 	}
 
 	public void setActingToken(String string) {
 		this.actingToken = string;
 	}
 
 	public String getActingToken() {
 		if (actingToken == null) {
 			if (completion != null) {
 				if (completion instanceof JstFieldOrMethodCompletion) {
 					return completion.getCompositeToken();
 				} else {
 					return completion.getToken();
 				}
 			} else {
 				return calculateToken();
 			}
 		}
 		return actingToken;
 	}
 
 	public String getActingPackageToken() {
 		if (actingPackageToken == null) {
 			return getActingToken();
 		}
 		return actingPackageToken;
 	}
 
 	/**
 	 * TODO add logic here to calculate token
 	 * 
 	 * @return
 	 */
 	public String calculateToken() {
 		int i = offset - 1;
 		while (i >= 0) {
 			char c = m_content.charAt(i);
 			if (StringUtils.isLetter(c)) {
 				i--;
 				continue;
 			} else {
 				break;
 			}
 		}
 		if (i == offset - 1) {
 			return "";
 		} else {
 			return m_content.substring(i + 1, offset);
 		}
 	}
 
 	public void setCalledType(IJstType calledType) {
 		this.calledType = calledType;
 	}
 
 	protected IJstType getTypeFromNode(IJstNode jstNode) {
 		if (jstNode instanceof IJstType) {
 			return (IJstType) jstNode;
 		} else if (jstNode instanceof JstIdentifier) {
 			JstIdentifier ji = (JstIdentifier) jstNode;
 			return ji.getType();
 		}
 		return null;
 	}
 
 	public List<IJstNode> getSelectedJstNodes() {
 		List<IJstNode> result = new ArrayList<IJstNode>();
 		innerGetSelectedNode(getActingType(), offset, result);
 		return result;
 	}
 
 	/**
 	 * Return the first fit JstMethod(if overloaded, the count may be over 1)
 	 * 
 	 * @return
 	 */
 	public IJstMethod getSelectedJstMethod() {
 		List<IJstNode> nodes = getSelectedJstNodes();
 		if (nodes.isEmpty()) {
 			return null;
 		}
 		Iterator<IJstNode> it = nodes.iterator();
 		while (it.hasNext()) {
 			IJstNode node = it.next();
 			if (node instanceof IJstMethod) {
 				return (IJstMethod) node;
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * return all the fit jst methods (all the overloaded methods)
 	 * 
 	 * @return
 	 */
 	public List<IJstMethod> getSelectedJstMethods() {
 		List<IJstNode> nodes = getSelectedJstNodes();
 		if (nodes.isEmpty()) {
 			return Collections.emptyList();
 		}
 		List<IJstMethod> methods = new ArrayList<IJstMethod>();
 		Iterator<IJstNode> it = nodes.iterator();
 		while (it.hasNext()) {
 			IJstNode node = it.next();
 			if (node instanceof IJstMethod) {
 				methods.add((IJstMethod) node);
 			}
			if(node instanceof JstBlock && node.getParentNode() instanceof IJstMethod){
				methods.add((IJstMethod)node.getParentNode());
			}
 		}
 		return methods;
 	}
 
 	private void innerGetSelectedNode(IJstNode pnode, int i,
 			List<IJstNode> result) {
 		if (pnode == null) {
 			return;
 		}
 		List<? extends IJstNode> nodes = pnode.getChildren();
 		Iterator<? extends IJstNode> it = nodes.iterator();
 		while (it.hasNext()) {
 			IJstNode node = it.next();
 			if (node != null) {
 				if (isInNode(node, i)) {
 					result.add(node);
 				}
 				innerGetSelectedNode(node, i, result);
 			}
 		}
 	}
 
 	private boolean isInNode(IJstNode jnode, int i) {
 		if (jnode instanceof JstCompletion) {
 			return false;
 		}
 		JstSource source = jnode.getSource();
 		if (source != null
 				&& (source.getStartOffSet() < i && source.getEndOffSet() >= i)) {
 			return true;
 		}
 		return false;
 	}
 
 	public void setOffset(int position) {
 		this.offset = position;
 	}
 
 	public List<JstVars> getJstVars(IJstMethod method) {
 		List<JstVars> result = new ArrayList<JstVars>();
 		innerGetJstVars(method, result);
 		return result;
 	}
 
 	public static List<String> getArgStringList(IJstMethod method) {
 		List<JstArg> list = method.getArgs();
 		List<String> argList = new ArrayList<String>();
 		Iterator<JstArg> ait = list.iterator();
 		while (ait.hasNext()) {
 			JstArg arg = ait.next();
 			argList.add(arg.getName());
 		}
 		return argList;
 
 	}
 
 	private void innerGetJstVars(IJstNode pnode, List<JstVars> result) {
 		List<? extends IJstNode> nodes = pnode.getChildren();
 		Iterator<? extends IJstNode> it = nodes.iterator();
 		while (it.hasNext()) {
 			IJstNode node = it.next();
 			if (node != null && node instanceof JstVars) {
 				JstVars jnode = (JstVars) node;
 				if (isVariableBeforeOffset(jnode, offset)) {
 					result.add(jnode);
 				}
 			}
 			innerGetJstVars(node, result);
 		}
 	}
 
 	private boolean isVariableBeforeOffset(JstVars jstVar, int i) {
 		// JstSource source = jstVar.getSource();
 		// TODO here can not get JstSource
 		// if (source != null && (source.getEndOffSet() < i)) {
 		// return true;
 		// }
 		return true;
 
 	}
 
 	public void setScriptUnit(IJstType scriptUnit) {
 		this.m_scriptUnit = scriptUnit;
 		if (m_scriptUnit == null) {
 			return;
 		}
 		m_scriptNodes = getJstNodeFromScriptUnit(m_scriptUnit, offset);
 		m_identifers = getJstIdentifierFromScriptUnit(m_scriptUnit, offset);
 		Collections.reverse(m_scriptNodes);
 
 	}
 
 	public boolean isInSciptUnitArea() {
 		if (completion != null && !(completion instanceof JstKeywordCompletion)) {
 			return false;
 		}
 		Iterator<IJstNode> it = m_scriptNodes.iterator();
 		while (it.hasNext()) {
 			IJstNode child = it.next();
 			if (child instanceof ObjLiteral) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public boolean isEmptyScriptNodes() {
 		return m_scriptNodes.isEmpty();
 	}
 
 	private List<IJstNode> getJstNodeFromScriptUnit(IJstType unit,
 			int position) {
 		List<IJstNode> result = new ArrayList<IJstNode>();
 		JstBlock block = VjoCcCtxUtil.getExactBlock(unit, position);
 		if (block != null) {
 			innerSFindNode(block, position, result);
 		}
 		return result;
 	}
 
 	private void innerSFindNode(IJstNode node, int position,
 			List<IJstNode> result) {
 		if (node == null) {
 			return;
 		}
 		JstSource source = node.getSource();
 		if (source != null) {
 
 			int endPos = source.getEndOffSet();
 			if (node instanceof JstIdentifier) {
 				endPos = endPos + 1;
 			}
 			if (source.getStartOffSet() <= position
 					&& source.getEndOffSet() + 1 >= position) {
 				result.add(node);
 			}
 		}
 		List<? extends IJstNode> nodes = node.getChildren();
 		if (nodes.isEmpty()) {
 			return;
 		}
 		Iterator<? extends IJstNode> it = nodes.iterator();
 		while (it.hasNext()) {
 			IJstNode temp = it.next();
 			innerSFindNode(temp, position, result);
 		}
 
 	}
 
 	private void innerSFindIdentifer(IJstNode node, int position,
 			List<JstIdentifier> result) {
 		if (node == null) {
 			return;
 		}
 		if (node instanceof JstIdentifier) {
 			JstSource source = node.getSource();
 			if (source != null && source.getEndOffSet() + 1 < position) {
 				// Make sure the position is after "xxx.", or can not count in
 				// this identifer
 				result.add((JstIdentifier) node);
 			}
 		}
 		List<? extends IJstNode> nodes = node.getChildren();
 		if (nodes.isEmpty()) {
 			return;
 		}
 		Iterator<? extends IJstNode> it = nodes.iterator();
 		while (it.hasNext()) {
 			IJstNode temp = it.next();
 			innerSFindIdentifer(temp, position, result);
 		}
 
 	}
 
 	private List<JstIdentifier> getJstIdentifierFromScriptUnit(
 			IJstType unit, int position) {
 		List<JstIdentifier> result = new ArrayList<JstIdentifier>();
 		JstBlock block = VjoCcCtxUtil.getExactBlock(unit, position);
 		if (block != null) {
 			innerSFindIdentifer(block, position, result);
 		}
 		return result;
 	}
 
 	public MtdInvocationExpr getSMtdInvo() {
 		if (m_scriptNodes.isEmpty()) {
 			return null;
 		}
 		Iterator<IJstNode> it = m_scriptNodes.iterator();
 		while (it.hasNext()) {
 			IJstNode child = it.next();
 			if (child instanceof MtdInvocationExpr) {
 				return (MtdInvocationExpr) child;
 			} else if (child instanceof FieldAccessExpr) {
 				MtdInvocationExpr mtd = getMtdFromFieldAccExpr((FieldAccessExpr) child);
 				if (mtd != null) {
 					return mtd;
 				}
 			}
 		}
 		return null;
 	}
 
 	private MtdInvocationExpr getMtdFromFieldAccExpr(
 			FieldAccessExpr fieldAccExpr) {
 		List<BaseJstNode> nodes = fieldAccExpr.getChildren();
 		if (nodes == null) {
 			return null;
 		}
 		Iterator<BaseJstNode> it = nodes.iterator();
 		while (it.hasNext()) {
 			BaseJstNode node = it.next();
 			if (node instanceof MtdInvocationExpr) {
 				return (MtdInvocationExpr) node;
 			}
 		}
 		return null;
 	}
 
 	public JstIdentifier getSIdentifer() {
 		if (m_identifers.isEmpty()) {
 			return null;
 		} else {
 			return m_identifers.get(0);
 		}
 	}
 	
 	public JstIdentifier getSFirstIdentifer() {
 		if (m_identifers.isEmpty()) {
 			return null;
 		} else {
 			return m_identifers.get(m_identifers.size() - 1);
 		}
 	}
 
 	public String[] getSKeywordStack() {
 		Iterator<JstIdentifier> it = m_identifers.iterator();
 		List<String> list = new ArrayList<String>();
 		while (it.hasNext()) {
 			list.add(it.next().getName());
 		}
 		String[] ss = new String[list.size()];
 		list.toArray(ss);
 		return ss;
 	}
 
 	public String getSSimpleToken() {
 		SimpleLiteral sl = getSSimpleLiteral();
 		String str = sl.getValue();
 		JstSource source = sl.getSource();
 		if (StringUtils.isBlankOrEmpty(str) || source == null) {
 			return "";
 		} else {
 			int subp = offset - source.getStartOffSet();
 			if (subp <= 0 || subp > str.length() + 1) {
 				return null;
 			} else {
 				return str.substring(0, subp - 1);
 			}
 		}
 	}
 
 	/**
 	 * calculate the last token, for xxx.<cursor1>endType().<cursor2>, at
 	 * cursor1, proposal should be supplied; at cursor2, proposal should not be
 	 * supplied. This method to see
 	 * 
 	 * @return
 	 */
 	public String getSLastToken() {
 		SimpleLiteral sl = getSSimpleLiteral();
 		String str = sl.getValue();
 		JstSource source = sl.getSource();
 		if (StringUtils.isBlankOrEmpty(str) || source == null) {
 			return "";
 		} else {
 			int subp = offset - source.getStartOffSet();
 			if (subp <= 0 || subp > str.length() + 1) {
 				return null;
 			} else {
 				return str.substring(0, subp - 1);
 			}
 		}
 	}
 
 	private void innerGetStack(MtdInvocationExpr mtd, List<String> list) {
 		List<BaseJstNode> nodes = mtd.getChildren();
 		if (nodes == null || nodes.size() == 0) {
 			return;
 		}
 		Iterator<BaseJstNode> it = nodes.iterator();
 		while (it.hasNext()) {
 			IJstNode node = it.next();
 			if (node != null && node instanceof JstIdentifier) {
 				JstIdentifier jid = (JstIdentifier) node;
 				list.add(jid.getName());
 			}
 		}
 		it = nodes.iterator();
 		while (it.hasNext()) {
 			IJstNode node = it.next();
 			if (node != null && node instanceof MtdInvocationExpr) {
 				MtdInvocationExpr mtdEx = (MtdInvocationExpr) node;
 				innerGetStack(mtdEx, list);
 			}
 		}
 
 	}
 
 	public SimpleLiteral getSSimpleLiteral() {
 
 		if (m_scriptNodes.isEmpty()) {
 			return null;
 		}
 		Iterator<IJstNode> it = m_scriptNodes.iterator();
 		while (it.hasNext()) {
 			IJstNode child = it.next();
 			if (child instanceof SimpleLiteral) {
 				return (SimpleLiteral) child;
 			}
 		}
 		return null;
 	}
 	
 	public TypeName getTypeNameObj() {
 		return m_typeName;
 	}
 
 	public String getTypeName() {
 		return m_typeName.typeName();
 	}
 	
 	public String getGroupName() {
 		return m_typeName.groupName();
 	}
 
 	public String getReplacedToken() {
 		return getActingToken();
 	}
 
 	public String getReplacedPackageToken() {
 		if (StringUtils.isBlankOrEmpty(this.actingPackageToken)) {
 			return getReplacedToken();
 		} else {
 			return this.actingPackageToken;
 		}
 	}
 
 	public void setActingPackageToken(String token) {
 		this.actingPackageToken = token;
 	}
 
 	public String getTypeStr() {
 		String[] ss = getSKeywordStack();
 		for (int i = 0; i < ss.length; i++) {
 			String s = ss[i];
 			if (CodeCompletionUtils.isTypeDeclare(s)) {
 				return s;
 			}
 		}
 		return "";
 	}
 
 	/**
 	 * check what is the currrent type, if the end key word is type, for CC, it
 	 * should be ignored. so skip the first token
 	 * 
 	 * @return
 	 */
 	public String getSTypeStrForCC() {
 		String[] ss = getSKeywordStack();
 		if (ss.length < 2) {
 			return "";
 		}
 		int i = 0;
 		if (!ifSContainKeyword(ss[0])) {
 			i = 1;
 		}
 		for (; i < ss.length; i++) {
 			String s = ss[i];
 			if (CodeCompletionUtils.isTypeDeclare(s)) {
 				return s;
 			}
 		}
 		return "";
 	}
 
 	public boolean ifSContainKeyword(String vjoKeyword) {
 		String[] ss = getSKeywordStack();
 		if (ss.length < 2) {
 			return false;
 		} else {
 			if (!(vjoKeyword).equals(ss[0])) {
 				return false;
 			} else {
 				String t = m_content.substring(0, offset - 1);
 				if (t.contains(vjoKeyword)) {
 					return true;
 				} else {
 					return false;
 				}
 			}
 		}
 	}
 
 	/**
 	 * get informatins injected during proposalling, by handler or advisor
 	 * 
 	 * @param key
 	 * @return
 	 */
 	public Object getInfo(String key) {
 		return infoMap.get(key);
 	}
 
 	public IJstType findBaseType(String name) {
 		if (name == null)
 			return null;
 
 		IJstType type = getVjoType();
 		if ("vjo".equals(name)) {
 			return type;
 		}
 		List<? extends IJstNode> nodes = type.getChildren();
 		Iterator<? extends IJstNode> it = nodes.iterator();
 		while (it.hasNext()) {
 			IJstNode node = it.next();
 			if (node instanceof IJstType) {
 				IJstType ttype = (IJstType) node;
 				if (name.equals(ttype.getSimpleName())) {
 					return ttype;
 				}
 			} else if (node instanceof JstTypeReference) {
 				JstTypeReference rtype = (JstTypeReference) node;
 				IJstType ttype = rtype.getReferencedType();
 				if (ttype != null && name.equals(ttype.getSimpleName())) {
 					return ttype;
 				}
 			}
 		}
 		return null;
 
 	}
 
 	private IJstType getVjoType() {
 		if (this.vjoType == null) {
 			this.vjoType = getJstTypeSpaceMgr().getQueryExecutor().findType(
 					new TypeName(LibManager.VJO_SELF_DESCRIBED, "vjo"));
 		}
 		return vjoType;
 	}
 
 	public boolean callFromDifferentType(IJstMethod method) {
 		IJstType callingType = getActingType();
 		IJstType calledType = method.getOwnerType();
 		return callingType != calledType;
 	}
 
 	public boolean callFromDifferentType(IJstProperty property) {
 		IJstType callingType = getActingType();
 		IJstType calledType = property.getOwnerType();
 		return callingType != calledType;
 	}
 
 	public IJstType getGlobalType() {
 		if (m_globalType == null) {
 			m_globalType = getJstTypeSpaceMgr().getQueryExecutor().findType(
 					new TypeName(JstTypeSpaceMgr.JS_NATIVE_GRP,
 							TypeSpaceMgr.GLOBAL));
 		}
 		return m_globalType;
 	}
 
 	public IJstType getObjectType() {
 		if (m_objType == null) {
 			m_objType = getJstTypeSpaceMgr().getQueryExecutor().findType(
 					new TypeName(JstTypeSpaceMgr.JS_NATIVE_GRP,
 							TypeSpaceMgr.OBJECT));
 		}
 		return m_objType;
 	}
 
 	public void setContent(String content) {
 		this.m_content = content;
 	}
 
 	public String getOriginalToken() {
 		if (completion == null) {
 			return "";
 		}
 		String t = completion.getToken();
 		if (StringUtils.isBlankOrEmpty(t)) {
 			return "";
 		} else {
 			return t;
 		}
 	}
 
 	public int getPositionType() {
 		return positionType;
 	}
 
 	public void setPositionType(int positionType) {
 		this.positionType = positionType;
 	}
 
 	/**
 	 * Check if the method come from vjo or vjo.ctype...., the replace string is
 	 * different from the common method true: vjo.ctype() false:vjo.creatArray()
 	 * 
 	 * @param method
 	 * @return
 	 */
 	public boolean isVjoMethod(IJstMethod method) {
 		IJstType type = method.getOwnerType();
 		IJstType rtype = method.getRtnType();
 		return isSelfDescibedType(type) && isSelfDescibedType(rtype);
 	}
 
 	public boolean isSelfDescibedType(IJstType type) {
 		if (type == null || type.getName() == null) {
 			return false;
 		}
 		String typeName = type.getName();
 		if (typeName.startsWith("vjo.") || typeName.equals("vjo")) {
 			if (type.getPackage() != null
 					&& LibManager.VJO_SELF_DESCRIBED.equals(type.getPackage()
 							.getGroupName())) {
 				return true;
 			} else {
 				return false;
 			}
 		} else {
 			return false;
 		}
 
 	}
 
 	/**
 	 * A temporate way to judge if there is a call from Class and not instance
 	 * 
 	 * @param jstCompl
 	 * @param jstType
 	 * @return
 	 */
 	public boolean callFromClass(JstCompletion jstCompl, IJstType jstType) {
 		if (jstType instanceof IJstRefType) {
 			return true;
 		}
 		// if (!(jstCompl instanceof JstCompletionOnQualifiedNameReference)) {
 		// return false;
 		// }
 		// if (jstType == null) {
 		// return false;
 		// }
 		// String typeName = jstType.getName();
 		// String token = jstCompl.getToken();
 		// if (nameCheck(token, typeName)) {
 		// return true;
 		// }
 		// String sTypeName = jstType.getSimpleName();
 		// if (nameCheck(token, sTypeName)) {
 		// return true;
 		// }
 		return false;
 	}
 
 	private boolean nameCheck(String token, String name) {
 		if (name == null) {
 			return false;
 		}
 		if (!token.startsWith(name)) {
 			return false;
 		}
 		String subStr = token.substring(name.length());
 		if (subStr.startsWith(".")) {
 			subStr = subStr.substring(1, subStr.length());
 		}
 		return !subStr.contains(".");
 	}
 
 	/**
 	 * case: var v = xxx; v.c<cursor> v is the prefix. JstCompletion is
 	 * JstCompletionOnQualifiedNameReference
 	 * 
 	 * case2: c<cursor> there is no prefix. JstCompletion is
 	 * JstCompletionOnSingleNameReference
 	 * 
 	 * @return
 	 */
 	public boolean hasNoPrefix() {
 		return completion != null
 				&& (completion instanceof JstCompletionOnSingleNameReference || completion instanceof JstComletionOnMessageSend);
 	}
 
 	/**
 	 * The advisor wit the name "advisorId" has advised an exactly matched
 	 * proposal
 	 * 
 	 * @param advisorId
 	 */
 	public void exactMatch(String advisorId) {
 		if (!matchAdvisorList.contains(advisorId)) {
 			matchAdvisorList.add(advisorId);
 		}
 	}
 
 	/**
 	 * @param advisorId
 	 * @return true, The advisor wit the name "advisorId" has advised an exactly
 	 *         matched proposal
 	 */
 	private boolean hasExactmatch(String advisorId) {
 		return matchAdvisorList.contains(advisorId);
 	}
 
 	public boolean needAdvisor(String id) {
 		if (VjoCcFunctionGenProposalAdvisor.ID.equals(id)) {
 			return !(hasExactmatch(VjoCcConstructorGenProposalAdvisor.ID) || hasExactmatch(VjoCcOverrideProposalAdvisor.ID));
 		}
 		return true;
 	}
 
 	public static boolean isVoidType(IJstType type) {
 		return type == null || "void".equals(type.getName());
 	}
 
 	public boolean isInObjectCreateExpr() {
 
 		int i = offset - 1;
 		while (i >= 0) {
 			char c = m_content.charAt(i);
 			if (StringUtils.isLetter(c)) {
 				i--;
 				continue;
 			} else {
 				break;
 			}
 		}
 		String s = m_content.substring(0, i);
 		if (s.trim().endsWith("new")) {
 			return !StringUtils.isLetter(s.charAt(s.length() - 4));
 		}
 		return false;
 	}
 
 	public IJstNode findNearestNode() {
 		IJstType type = completion.getOwnerType();
 		if (type == null) {
 			return null;
 		}
 		IJstNode obj = JstUtil.getLeafNode(type, offset, offset);
 		return obj;
 	}
 
 	public boolean isMtypeEabled() {
 		return mtypeEabled;
 	}
 
 	public void setMtypeEabled(boolean mtypeEabled) {
 		this.mtypeEabled = mtypeEabled;
 	}
 
 	public boolean isInMtdCall() {
 		Stack<ScopeId> ss = completion.getScopeStack();
 		if (ss.isEmpty()) {
 			return false;
 		} else {
 			return ScopeIds.METHOD_CALL == ss.peek();
 		}
 	}
 
 	/**
 	 * xx(a, b, <cursor> c) return the cursor's relative position, the above
 	 * code will return 2;
 	 * 
 	 * @param mtd
 	 * @return
 	 */
 	public int getRelativePosInMtdCall(MtdInvocationExpr mtd) {
 		List<IExpr> args = mtd.getArgs();
 		if (args == null || args.isEmpty()) {
 			return 0;
 		}
 		int pos = 0;
 		Iterator<IExpr> it = args.iterator();
 		while (it.hasNext()) {
 			IExpr node = it.next();
 			JstSource source = node.getSource();
 			if (source == null) {
 				pos++;
 				continue;
 			} else {
 				if (source.getEndOffSet() >= offset) {
 					return pos;
 				} else {
 					pos++;
 				}
 			}
 		}
 		return pos;
 	}
 
 	public boolean ifContainsFullNameCheck() {
 		return actingPackageToken != null;
 	}
 
 	/**
 	 * Return the last "needs" position, if there is no the keyword, return the
 	 * position after the ctype(or etype ...)
 	 * 
 	 * @return
 	 */
 	public int getNeedsPosition() {
 		if (m_identifers == null || m_identifers.isEmpty()) {
 			return -1;
 		}
 		Iterator<JstIdentifier> it = m_identifers.iterator();
 		int start = -1;
 		JstIdentifier preIdentifier = null;
 		boolean posFound = false;
 		while (it.hasNext()) {
 			JstIdentifier identifier = it.next();
 			String exprText = identifier.toExprText();
 			if (VjoKeywords.NEEDS.equals(exprText)
 					|| CodeCompletionUtils.isTypeDeclare(identifier
 							.toExprText())) {
 				posFound = true;
 				break;
 			} else {
 				preIdentifier = identifier;
 			}
 		}
 		if (preIdentifier == null || !posFound) {
 			return -1;
 		}
 		IJstNode node = preIdentifier.getParentNode();
 		if (node instanceof MtdInvocationExpr) {
 			JstSource source = preIdentifier.getSource();
 			start = source.getStartOffSet();
 		}
 		if (start == -1) {
 			return start;
 		}
 		for (; start > 0; start--) {
 			if (m_content.charAt(start) == '.') {
 				return start + 1;
 			}
 		}
 		return -1;
 
 	}
 
 	private boolean needInsertNeedsExpr(IJstType type, String typeName) {
 		// check needs
 		List<? extends IJstType> etypes = type.getImports();
 		Iterator<? extends IJstType> it = etypes.iterator();
 		while (it.hasNext()) {
 			IJstType temp = it.next();
 			if (typeName.equals(temp.getName())) {
 				return false;
 			}
 		}
 		// check inherites
 		IJstType inheritType = type.getExtend();
 		if (inheritType != null && typeName.equals(inheritType.getName())) {
 			return false;
 		}
 		// check satifies
 		List<? extends IJstType> stypes = type.getSatisfies();
 		it = stypes.iterator();
 		while (it.hasNext()) {
 			IJstType temp = it.next();
 			if (typeName.equals(temp.getName())) {
 				return false;
 			}
 		}
 		// Embeded Types
 		List<? extends IJstType> innerTypes = type.getEmbededTypes();
 		it = innerTypes.iterator();
 		while (it.hasNext()) {
 			IJstType temp = it.next();
 			if (typeName.equals(temp.getName())) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	/**
 	 * Condition: Has no needs
 	 * @param insertedType
 	 * @return If need to add "needs" expression for the inserted type
 	 */
 	public boolean needInsertNeedsExpr(IJstType insertedType) {
 		//If is common js content, won't inject needs expr.
 		if (isCommonJsCtx()) {
 			return false;
 		}
 		String typeName = insertedType.getName();
 		if (insertedType.isImpliedImport()) {
 			return false;
 		}
 		IJstType type = getActingType();
 		// get parent type if having
 		IJstNode node = type.getParentNode();
 		while (node != null && (node instanceof IJstType)) {
 			type = (IJstType) node;
 			node = node.getParentNode();
 		}
 		if (typeName.equals(type.getName())) {
 			return false;
 		}
 		IJstType insertedOuterType = insertedType.getOuterType();
 		if (isSelfDescibedType(insertedOuterType)) {
 			return false;
 		}
 		if (!needInsertNeedsExpr(type, typeName)) {
 			return false;
 		}
 		// check child type if having
 		List<? extends IJstType> list = type.getEmbededTypes();
 		if (list != null && !list.isEmpty()) {
 			Iterator<? extends IJstType> it = list.iterator();
 			while (it.hasNext()) {
 				IJstType temp = it.next();
 				if (!needInsertNeedsExpr(temp, typeName)) {
 					return false;
 				}
 			}
 		}
 		return true;
 
 	}
 	/**
 	 * Condition: 1. Has no needs 2. Has no inactive needs. 
 	 * @param insertedType
 	 * @return If need to add "needs" expression for the inserted type
 	 */
 	public boolean needInsertInactiveNeedsExpr(IJstType insertedType) {
 		if (!needInsertNeedsExpr(insertedType)) {
 			return false;
 		}
 		//TODO: Need clarify the requirement to skip some native types
 		//The "isNativeType" method is not accurate enough
 		if (CodeCompletionUtils.isNativeType(insertedType)) {
 			return false;
 		}
 		String typeName = insertedType.getName();
 		IJstType type = getActingType();
 		List<? extends IJstTypeReference> typeRefs = type.getInactiveImportsRef();
 		if (typeRefs.isEmpty()) {
 			return true;
 		}
 		Iterator<? extends IJstTypeReference> iterator = typeRefs.iterator();
 		while (iterator.hasNext()) {
 			IJstTypeReference typeRef = iterator.next();
 			if (typeName.equals(typeRef.getReferencedType().getName())) {
 				return false;
 			}
 		}
 		return true;
 	}
 
 	public void setAdvisors(String[] advisors) {
 		this.advisors = Arrays.asList(advisors);
 	}
 
 	public boolean containsFieldAdvisors() {
 		return advisors != null
 				&& (advisors.contains(VjoCcStaticPropMethodProposalAdvisor.ID) || advisors
 						.contains(VjoCcPropMethodProposalAdvisor.ID) || advisors.contains(VjoCcDerivedPropMethodAdvisor.ID));
 	}
 
 	public boolean isInSimpeLiteral() {
 		IJstNode node = JstUtil.getLeafNode(getActingType(), offset, offset);
 		if (node != null && node instanceof SimpleLiteral) {
 			SimpleLiteral sl = (SimpleLiteral) node;
 			return sl.getSource().getStartOffSet() != offset;
 		} else {
 			return false;
 		}
 	}
 
 	public boolean isCalledFromInnerType() {
 		IJstType callingType = getCalledType();
 		IJstType calledType = getCalledType();
 		if (callingType == null) {
 			return false;
 		}
 		if (calledType != null && callingType != calledType) {
 			return false;
 		}
 		IJstNode outer = callingType.getParentNode();
 		if (outer == null || !(outer instanceof IJstType)) {
 			return false;
 		}
 		return true;
 	}
 
 	public boolean isAfterFieldAccess() {
 		return completion != null
 				&& (completion instanceof JstCompletionOnQualifiedNameReference);
 	}
 
 	public boolean isInSyntaxScope() {
 		Object info = getInfo(VjoCcCtx.INFO_KEY_IN_TYPE_SCOPE);
 		if (info != null && (info instanceof Boolean)) {
 			return (Boolean) info;
 		}
 		return false;
 	}
 
 	public boolean isInCommentArea() {
 		return (getCompletion() instanceof JstCommentCompletion);
 	}
 
 	public boolean isInInactiveArea() {
 		if (!isInCommentArea()) {
 			return false;
 		}
 		JstCommentCompletion commentCompletion = (JstCommentCompletion) this.completion;
 		return VjoCcCommentUtil.isInactiveNeed(commentCompletion);
 	}
 
 }
