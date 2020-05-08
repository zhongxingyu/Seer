 /*
  * Author: C.Williams
  * 
  * Copyright (c) 2004 RubyPeople.
  * 
  * This file is part of the Ruby Development Tools (RDT) plugin for eclipse. You
  * can get copy of the GPL along with further information about RubyPeople and
  * third party software bundled with RDT in the file
  * org.rubypeople.rdt.core_x.x.x/RDT.license or otherwise at
  * http://www.rubypeople.org/RDT.license.
  * 
  * RDT is free software; you can redistribute it and/or modify it under the
  * terms of the GNU General Public License as published by the Free Software
  * Foundation; either version 2 of the License, or (at your option) any later
  * version.
  * 
  * RDT is distributed in the hope that it will be useful, but WITHOUT ANY
  * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS FOR
  * A PARTICULAR PURPOSE. See the GNU General Public License for more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * RDT; if not, write to the Free Software Foundation, Inc., 59 Temple Place,
  * Suite 330, Boston, MA 02111-1307 USA
  */
 package org.rubypeople.rdt.internal.core;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.jruby.ast.AliasNode;
 import org.jruby.ast.AndNode;
 import org.jruby.ast.ArgsCatNode;
 import org.jruby.ast.ArgsNode;
 import org.jruby.ast.ArrayNode;
 import org.jruby.ast.BackRefNode;
 import org.jruby.ast.BeginNode;
 import org.jruby.ast.BignumNode;
 import org.jruby.ast.BlockArgNode;
 import org.jruby.ast.BlockNode;
 import org.jruby.ast.BlockPassNode;
 import org.jruby.ast.BreakNode;
 import org.jruby.ast.CallNode;
 import org.jruby.ast.CaseNode;
 import org.jruby.ast.ClassNode;
 import org.jruby.ast.ClassVarAsgnNode;
 import org.jruby.ast.ClassVarDeclNode;
 import org.jruby.ast.ClassVarNode;
 import org.jruby.ast.Colon2Node;
 import org.jruby.ast.Colon3Node;
 import org.jruby.ast.ConstDeclNode;
 import org.jruby.ast.ConstNode;
 import org.jruby.ast.DAsgnNode;
 import org.jruby.ast.DRegexpNode;
 import org.jruby.ast.DStrNode;
 import org.jruby.ast.DSymbolNode;
 import org.jruby.ast.DVarNode;
 import org.jruby.ast.DXStrNode;
 import org.jruby.ast.DefinedNode;
 import org.jruby.ast.DefnNode;
 import org.jruby.ast.DefsNode;
 import org.jruby.ast.DotNode;
 import org.jruby.ast.EnsureNode;
 import org.jruby.ast.EvStrNode;
 import org.jruby.ast.FCallNode;
 import org.jruby.ast.FalseNode;
 import org.jruby.ast.FixnumNode;
 import org.jruby.ast.FlipNode;
 import org.jruby.ast.FloatNode;
 import org.jruby.ast.ForNode;
 import org.jruby.ast.GlobalAsgnNode;
 import org.jruby.ast.GlobalVarNode;
 import org.jruby.ast.HashNode;
 import org.jruby.ast.IfNode;
 import org.jruby.ast.InstAsgnNode;
 import org.jruby.ast.InstVarNode;
 import org.jruby.ast.IterNode;
 import org.jruby.ast.LocalAsgnNode;
 import org.jruby.ast.LocalVarNode;
 import org.jruby.ast.Match2Node;
 import org.jruby.ast.Match3Node;
 import org.jruby.ast.MatchNode;
 import org.jruby.ast.ModuleNode;
 import org.jruby.ast.MultipleAsgnNode;
 import org.jruby.ast.NewlineNode;
 import org.jruby.ast.NextNode;
 import org.jruby.ast.NilNode;
 import org.jruby.ast.Node;
 import org.jruby.ast.NotNode;
 import org.jruby.ast.NthRefNode;
 import org.jruby.ast.OpAsgnAndNode;
 import org.jruby.ast.OpAsgnNode;
 import org.jruby.ast.OpAsgnOrNode;
 import org.jruby.ast.OpElementAsgnNode;
 import org.jruby.ast.OptNNode;
 import org.jruby.ast.OrNode;
 import org.jruby.ast.PostExeNode;
 import org.jruby.ast.RedoNode;
 import org.jruby.ast.RegexpNode;
 import org.jruby.ast.RescueBodyNode;
 import org.jruby.ast.RescueNode;
 import org.jruby.ast.RetryNode;
 import org.jruby.ast.ReturnNode;
 import org.jruby.ast.SClassNode;
 import org.jruby.ast.SValueNode;
 import org.jruby.ast.ScopeNode;
 import org.jruby.ast.SelfNode;
 import org.jruby.ast.SplatNode;
 import org.jruby.ast.StrNode;
 import org.jruby.ast.SuperNode;
 import org.jruby.ast.SymbolNode;
 import org.jruby.ast.ToAryNode;
 import org.jruby.ast.TrueNode;
 import org.jruby.ast.UndefNode;
 import org.jruby.ast.UntilNode;
 import org.jruby.ast.VAliasNode;
 import org.jruby.ast.VCallNode;
 import org.jruby.ast.WhenNode;
 import org.jruby.ast.WhileNode;
 import org.jruby.ast.XStrNode;
 import org.jruby.ast.YieldNode;
 import org.jruby.ast.ZArrayNode;
 import org.jruby.ast.ZSuperNode;
 import org.jruby.ast.visitor.NodeVisitor;
 import org.jruby.lexer.yacc.ISourcePosition;
 import org.jruby.runtime.Visibility;
 import org.rubypeople.rdt.core.IMethod;
 import org.rubypeople.rdt.core.IRubyElement;
 import org.rubypeople.rdt.core.IRubyScript;
 import org.rubypeople.rdt.internal.core.parser.RubyParser;
 
 /**
  * @author Chris
  * 
  */
 public class RubyScriptStructureBuilder implements NodeVisitor {
 
 	private InfoStack infoStack = new InfoStack();
 	private HandleStack modelStack = new HandleStack();
 	private RubyScriptElementInfo scriptInfo;
 	private IRubyScript script;
 	private Visibility currentVisibility = Visibility.PUBLIC;
 	private Map newElements;
 	private RubyElementInfo importContainerInfo;
 	private boolean DEBUG = false;
 
 	/**
 	 * 
 	 * @param script
 	 *            The RubyScript whose contents we're parsing
 	 * @param info
 	 *            The RubyElementInfo of the RubyScript
 	 * @param newElements
 	 *            a Map passed in. It is actually a temporarcy cache from the
 	 *            RubyModelManager. It holds elements below the level of a
 	 *            RubyScript in our hierarchy.
 	 */
 	public RubyScriptStructureBuilder(IRubyScript script, RubyScriptElementInfo info, Map newElements) {
 		this.script = script;
 		this.scriptInfo = info;
 		this.newElements = newElements;
 		modelStack.push(script);
 		infoStack.push(scriptInfo);
 		DEBUG = RubyParser.isDebugging();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitAliasNode(org.jruby.ast.AliasNode)
 	 */
 	public void visitAliasNode(AliasNode iVisited) {
		handleNode(iVisited);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitAndNode(org.jruby.ast.AndNode)
 	 */
 	public void visitAndNode(AndNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getFirstNode());
 		visitNode(iVisited.getFirstNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitArgsNode(org.jruby.ast.ArgsNode)
 	 */
 	public void visitArgsNode(ArgsNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getBlockArgNode());
 		if (iVisited.getOptArgs() != null) {
 			visitIter(iVisited.getOptArgs().iterator());
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitArgsCatNode(org.jruby.ast.ArgsCatNode)
 	 */
 	public void visitArgsCatNode(ArgsCatNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getFirstNode());
 		visitNode(iVisited.getSecondNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitArrayNode(org.jruby.ast.ArrayNode)
 	 */
 	public void visitArrayNode(ArrayNode iVisited) {
 		handleNode(iVisited);
 		visitIter(iVisited.iterator());
 	}
 
 	/**
 	 * @param iterator
 	 */
 	private void visitIter(Iterator iterator) {
 		while (iterator.hasNext()) {
 			visitNode((Node) iterator.next());
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitBackRefNode(org.jruby.ast.BackRefNode)
 	 */
 	public void visitBackRefNode(BackRefNode iVisited) {
 		handleNode(iVisited);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitBeginNode(org.jruby.ast.BeginNode)
 	 */
 	public void visitBeginNode(BeginNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getBodyNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitBignumNode(org.jruby.ast.BignumNode)
 	 */
 	public void visitBignumNode(BignumNode iVisited) {
 		handleNode(iVisited);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitBlockArgNode(org.jruby.ast.BlockArgNode)
 	 */
 	public void visitBlockArgNode(BlockArgNode iVisited) {
 		handleNode(iVisited);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitBlockNode(org.jruby.ast.BlockNode)
 	 */
 	public void visitBlockNode(BlockNode iVisited) {
 		handleNode(iVisited);
 		visitIter(iVisited.iterator());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitBlockPassNode(org.jruby.ast.BlockPassNode)
 	 */
 	public void visitBlockPassNode(BlockPassNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getArgsNode());
 		visitNode(iVisited.getBodyNode());
 		visitNode(iVisited.getIterNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitBreakNode(org.jruby.ast.BreakNode)
 	 */
 	public void visitBreakNode(BreakNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getValueNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitConstDeclNode(org.jruby.ast.ConstDeclNode)
 	 */
 	public void visitConstDeclNode(ConstDeclNode iVisited) {
 		handleNode(iVisited);
 		String name = iVisited.getName();
 		RubyElement type = getCurrentType();
 		RubyConstant handle = new RubyConstant(type, name);
 		modelStack.push(handle);
 		RubyElementInfo parentInfo = getCurrentTypeInfo();
 		parentInfo.addChild(handle);
 		RubyFieldElementInfo info = new RubyFieldElementInfo();
 		info.setTypeName(estimateValueType(iVisited.getValueNode()));
 		ISourcePosition pos = iVisited.getPosition();
 		setTokenRange(pos, info, name);
 		// TODO Add more information about the variable
 		infoStack.push(info);
 		newElements.put(handle, info);
 		visitNode(iVisited.getValueNode());
 		modelStack.pop();
 		infoStack.pop();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitClassVarAsgnNode(org.jruby.ast.ClassVarAsgnNode)
 	 */
 	public void visitClassVarAsgnNode(ClassVarAsgnNode iVisited) {
 		handleNode(iVisited);
 		String name = iVisited.getName();
 		RubyElement type = getCurrentType();
 		RubyClassVar handle = new RubyClassVar(type, name);
 		modelStack.push(handle);
 
 		RubyElementInfo parentInfo = getCurrentTypeInfo();
 		parentInfo.addChild(handle);
 
 		RubyFieldElementInfo info = new RubyFieldElementInfo();
 		info.setTypeName(estimateValueType(iVisited.getValueNode()));
 		ISourcePosition pos = iVisited.getPosition();
 		//setTokenRange(pos, info, name);
 		setClassVarRange( pos, info, name );
 		// TODO Add more information about the variable
 		infoStack.push(info);
 
 		newElements.put(handle, info);
 
 		visitNode(iVisited.getValueNode());
 
 		modelStack.pop();
 		infoStack.pop();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitClassVarDeclNode(org.jruby.ast.ClassVarDeclNode)
 	 */
 	public void visitClassVarDeclNode(ClassVarDeclNode iVisited) {
 		handleNode(iVisited);
 		String name = iVisited.getName();
 		RubyElement type = getCurrentType();
 		RubyClassVar var = new RubyClassVar(type, iVisited.getName());
 
 		RubyElementInfo parentInfo = infoStack.peek();		
 		RubyFieldElementInfo info = new RubyFieldElementInfo();
 		info.setTypeName(estimateValueType(iVisited.getValueNode()));
 		info.setTypeName(estimateValueType(iVisited.getValueNode()));
 		ISourcePosition pos = iVisited.getPosition();
 		//setTokenRange(pos, info, name);
 		setClassVarRange( pos, info, name );
 		
 		parentInfo.addChild(var);	
 
 		newElements.put(var, info);
 
 		visitNode(iVisited.getValueNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitClassVarNode(org.jruby.ast.ClassVarNode)
 	 */
 	public void visitClassVarNode(ClassVarNode iVisited) {
 		handleNode(iVisited);
 	}
 
 	/**
 	 * @return
 	 */
 	private RubyElementInfo getCurrentTypeInfo() {
 		List extras = new ArrayList();
 		RubyElementInfo element = infoStack.peek();
 		while (!(element instanceof RubyTypeElementInfo)) {
 			extras.add(infoStack.pop());
 			element = infoStack.peek();
 			if (element == null) break;
 		}
 		for (Iterator iter = extras.iterator(); iter.hasNext();) {
 			infoStack.push((RubyElementInfo) iter.next());
 		}
 		if (element == null) return scriptInfo;
 		return element;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitCallNode(org.jruby.ast.CallNode)
 	 */
 	public void visitCallNode(CallNode iVisited) {
 		handleNode(iVisited);
 		// FIXME Evaluate the receiver and check to see if the method exists!
 		if (DEBUG) System.out.println(iVisited.getName());
 		visitNode(iVisited.getReceiverNode());
 		visitNode(iVisited.getArgsNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitCaseNode(org.jruby.ast.CaseNode)
 	 */
 	public void visitCaseNode(CaseNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getCaseNode());
 		visitNode(iVisited.getFirstWhenNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitClassNode(org.jruby.ast.ClassNode)
 	 */
 	public void visitClassNode(ClassNode iVisited) {
 		handleNode(iVisited);
 		String name = iVisited.getCPath().getName();
 		RubyType handle = new RubyType(modelStack.peek(), name);
 		modelStack.push(handle);
 
 		RubyElementInfo parentInfo = infoStack.peek();
 		parentInfo.addChild(handle);
 
 		RubyTypeElementInfo info = new RubyTypeElementInfo();
 		info.setHandle(handle);
 		ISourcePosition pos = iVisited.getPosition();
 		setKeywordRange("class", pos, info, name);
 		
 		// TODO Set the superclass!
 		info.setIncludedModuleNames(new String[] {"Kernel"});
 		infoStack.push(info);
 
 		newElements.put(handle, info);
 
 		visitNode(iVisited.getSuperNode());
 		visitNode(iVisited.getBodyNode());
 
 		// TODO Collect the included modules and set them here!
 		modelStack.pop();
 		infoStack.pop();
 	}
 
 	private void setLocalVarRange( ISourcePosition pos, MemberElementInfo info, String name ){
 		int start = pos.getStartOffset()  - name.length();
 		setMemberElementPosition( 
 			info,
 			start,
 			pos.getStartOffset() - 1,
 			start,
 			pos.getEndOffset() );	
 	}
 	
 	private void setInstanceVarRange( ISourcePosition pos, MemberElementInfo info, String name ){
 		int start = pos.getStartOffset()  - name.length();
 		setMemberElementPosition( 
 			info,
 			start,
 			pos.getStartOffset()- 1,
 			start,
 			pos.getEndOffset() );	
 	}
 	
 	private void setClassVarRange( ISourcePosition pos, MemberElementInfo info, String name ){
 		int start = pos.getStartOffset() - name.length();
 		setMemberElementPosition( 
 			info,
 			start,
 			pos.getStartOffset() - 1,
 			start,
 			pos.getEndOffset() - 1);
 	}	
 	
 	/**
 	 * @param keyword
 	 * @param pos
 	 * @param info
 	 * @param name
 	 */
 	private void setKeywordRange(String keyword, ISourcePosition pos, MemberElementInfo info, String name) {
 		info.setNameSourceStart( pos.getStartOffset() + 1);
 		info.setNameSourceEnd(pos.getStartOffset() + name.length() );
 		info.setSourceRangeStart(pos.getStartOffset() + 1 );
 		info.setSourceRangeEnd( pos.getEndOffset() );	
 	}
 	
 	/**
 	 * Sets the position on a given MemberElementInfo object.
 	 * @param info
 	 * @param nameSourceStart
 	 * @param nameSourceEnd
 	 * @param sourceStart
 	 * @param sourceEnd
 	 */
 	private void setMemberElementPosition( MemberElementInfo info, int nameSourceStart, 
 			int nameSourceEnd, int sourceStart, int sourceEnd ){
 		info.setNameSourceStart( nameSourceStart );
 		info.setNameSourceEnd( nameSourceEnd );
 		info.setSourceRangeStart( sourceStart );
 		info.setSourceRangeEnd( sourceEnd );		
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitColon2Node(org.jruby.ast.Colon2Node)
 	 */
 	public void visitColon2Node(Colon2Node iVisited) {
 		handleNode(iVisited);
 		if (DEBUG) System.out.println(iVisited.getName());
 		visitNode(iVisited.getLeftNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitColon3Node(org.jruby.ast.Colon3Node)
 	 */
 	public void visitColon3Node(Colon3Node iVisited) {
 		handleNode(iVisited);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitConstNode(org.jruby.ast.ConstNode)
 	 */
 	public void visitConstNode(ConstNode iVisited) {
 		handleNode(iVisited);
 		if (DEBUG) System.out.println(iVisited.getName());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitDAsgnNode(org.jruby.ast.DAsgnNode)
 	 */
 	public void visitDAsgnNode(DAsgnNode iVisited) {
 		handleNode(iVisited);
 		RubyDynamicVar var = new RubyDynamicVar(modelStack.peek(), iVisited.getName());
 		modelStack.push(var);
 
 		RubyFieldElementInfo info = new RubyFieldElementInfo();
 		info.setTypeName(estimateValueType(iVisited.getValueNode()));
 		infoStack.push(info);
 
 		newElements.put(var, info);
 
 		if (DEBUG) System.out.println(iVisited.getName());
 		visitNode(iVisited.getValueNode());
 
 		modelStack.pop();
 		infoStack.pop();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitDRegxNode(org.jruby.ast.DRegexpNode)
 	 */
 	public void visitDRegxNode(DRegexpNode iVisited) {
 		handleNode(iVisited);
 		visitIter(iVisited.iterator());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitDStrNode(org.jruby.ast.DStrNode)
 	 */
 	public void visitDStrNode(DStrNode iVisited) {
 		handleNode(iVisited);
 		visitIter(iVisited.iterator());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitDSymbolNode(org.jruby.ast.DSymbolNode)
 	 */
 	public void visitDSymbolNode(DSymbolNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitDVarNode(org.jruby.ast.DVarNode)
 	 */
 	public void visitDVarNode(DVarNode iVisited) {
 		handleNode(iVisited);
 		if (DEBUG) System.out.println(iVisited.getName());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitDXStrNode(org.jruby.ast.DXStrNode)
 	 */
 	public void visitDXStrNode(DXStrNode iVisited) {
 		handleNode(iVisited);
 		visitIter(iVisited.iterator());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitDefinedNode(org.jruby.ast.DefinedNode)
 	 */
 	public void visitDefinedNode(DefinedNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getExpressionNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitDefnNode(org.jruby.ast.DefnNode)
 	 */
 	public void visitDefnNode(DefnNode iVisited) {
 		handleNode(iVisited);
 		// ArgsNode argsNode = (ArgsNode) iVisited.getArgsNode();
 		// boolean hasRest = false;
 		// if (argsNode.getRestArg() != -1) hasRest = true;
 
 		// boolean hasBlock = false;
 		// if (argsNode.getBlockArgNode() != null) hasBlock = true;
 
 		// int optArgCount = 0;
 		// if (argsNode.getOptArgs() != null) optArgCount =
 		// argsNode.getOptArgs().size();
 
 		String name = iVisited.getName();
 
 		Visibility visibility = currentVisibility;
 		if (name.equals("initialize")) visibility = Visibility.PROTECTED;
 
 		RubyElement type = getCurrentType();
 		RubyMethod method = new RubyMethod(type, name);
 		modelStack.push(method);
 
 		RubyElementInfo parentInfo = infoStack.peek();
 		parentInfo.addChild(method);
 
 		RubyMethodElementInfo info = new RubyMethodElementInfo();
 		info.setArgumentNames(getArgs(iVisited.getArgsNode()));
 		// TODO Set more information
 		info.setVisibility(convertVisibility(visibility));
 		ISourcePosition pos = iVisited.getPosition();
 		setKeywordRange("def", pos, info, name);
 		infoStack.push(info);
 
 		newElements.put(method, info);
 
 		visitNode(iVisited.getArgsNode());
 		visitNode(iVisited.getBodyNode());
 
 		modelStack.pop();
 		infoStack.pop();
 	}
 
 	/**
 	 * @param visibility
 	 * @return
 	 */
 	private int convertVisibility(Visibility visibility) {
 		// FIXME What about the module function and public-protected
 		// visibilities?
 		if (visibility == Visibility.PUBLIC) return IMethod.PUBLIC;
 		if (visibility == Visibility.PROTECTED) return IMethod.PROTECTED;
 		return IMethod.PRIVATE;
 	}
 
 	/**
 	 * @param argsNode
 	 * @return
 	 */
 	private String[] getArgs(Node argsNode) {
 		// TODO Auto-generated method stub
 		return new String[0];
 	}
 
 	/**
 	 * @return
 	 */
 	private RubyElement getCurrentType() {
 		List extras = new ArrayList();
 		IRubyElement element = modelStack.peek();
 		while (!element.isType(IRubyElement.TYPE)) {
 			extras.add(modelStack.pop());
 			element = modelStack.peek();
 			if (element == null) break;
 		}
 		for (Iterator iter = extras.iterator(); iter.hasNext();) {
 			modelStack.push((RubyElement) iter.next());
 		}
 		if (element == null) return (RubyScript) script;
 		return (RubyElement) element;
 	}
 
 
     /*
      * (non-Javadoc)
      * 
      * @see org.jruby.ast.visitor.NodeVisitor#visitDefsNode(org.jruby.ast.DefsNode)
      */
     public void visitDefsNode(DefsNode iVisited) {
         handleNode(iVisited);       
         
         // Get the information no the current parent of this method
         RubyElementInfo parentInfo = infoStack.peek();
         
         /* Get the name of the current static method and add the name of the
          * class or module to the beginning of it. This avoids instance method
          * naming conflicts. e.g.: 
          *    class A
          *      def self.method; end
          *      def method; end
          *    end
          * will give us:
          *    A.method
          *    method
          * in the Outline View.
          */ 
         String name;
         if( parentInfo instanceof RubyTypeElementInfo ){
             name = new String(((RubyTypeElementInfo)parentInfo).getName())
                 + "." + iVisited.getName(); }
         else{
             name = iVisited.getName(); }
         
         // Get the visibility of the current static method 
         Visibility visibility = currentVisibility;
         
         // Get the type of the current parent element
         RubyElement type = getCurrentType();
         RubyMethod method = new RubySingletonMethod(type, name);
         modelStack.push(method);
                 
         parentInfo.addChild( method );
 
         
         RubyMethodElementInfo info = new RubyMethodElementInfo();
         
         // TODO Set more info!
         infoStack.push(info);
         ISourcePosition pos = iVisited.getPosition();
         setKeywordRange("def", pos, info, name);
         
         info.setArgumentNames(getArgs(iVisited.getArgsNode()));
         info.setVisibility(convertVisibility(visibility));
 
         newElements.put(method, info);
 
         // FIXME Evaluate the receiver!
         visitNode(iVisited.getReceiverNode());
         visitNode(iVisited.getArgsNode());
         visitNode(iVisited.getBodyNode());
         
         modelStack.pop();
         infoStack.pop();
     }
 
     
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitDotNode(org.jruby.ast.DotNode)
 	 */
 	public void visitDotNode(DotNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getBeginNode());
 		visitNode(iVisited.getEndNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitEnsureNode(org.jruby.ast.EnsureNode)
 	 */
 	public void visitEnsureNode(EnsureNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getEnsureNode());
 		visitNode(iVisited.getBodyNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitEvStrNode(org.jruby.ast.EvStrNode)
 	 */
 	public void visitEvStrNode(EvStrNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getBody());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitFCallNode(org.jruby.ast.FCallNode)
 	 */
 	public void visitFCallNode(FCallNode iVisited) {
 		handleNode(iVisited);
 		// FIXME Evaluate self and check to see if the method exists!
 		if (DEBUG) System.out.println(iVisited.getName());
 		String functionName = iVisited.getName();
 		if (functionName.equals("require") || functionName.equals("load")) {
 			ArrayNode node = (ArrayNode) iVisited.getArgsNode();
 			String arg = getString(node);
 			if (arg != null) {
				RubyElementInfo parentInfo = scriptInfo;
 				ImportContainer importContainer = (ImportContainer) script.getImportContainer();
 				// create the import container and its info
 				if (this.importContainerInfo == null) {
 					this.importContainerInfo = new RubyElementInfo();
 					scriptInfo.addChild(importContainer);
 					this.newElements.put(importContainer, this.importContainerInfo);
 				}
 				RubyImport handle = new RubyImport(importContainer, arg);
 
 				ImportDeclarationElementInfo info = new ImportDeclarationElementInfo();
 				setKeywordRange(functionName, node.getPosition(), info, arg);
 				info.name = arg; // no trailing * if onDemand
 
 				this.importContainerInfo.addChild(handle);
 				this.newElements.put(handle, info);
 			}
 		}
 		visitNode(iVisited.getArgsNode());
 	}
 
 	/**
 	 * @param node
 	 * @return
 	 */
 	private String getString(ArrayNode node) {
 		Object tmp = node.iterator().next();
 		if (tmp instanceof DStrNode) {
 			DStrNode dstrNode = (DStrNode) tmp;
 			tmp = dstrNode.iterator().next();
 		}
 		if (tmp instanceof StrNode) {
 			StrNode strNode = (StrNode) tmp;
 			return strNode.getValue();
 		}
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitFalseNode(org.jruby.ast.FalseNode)
 	 */
 	public void visitFalseNode(FalseNode iVisited) {
 		handleNode(iVisited);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitFixnumNode(org.jruby.ast.FixnumNode)
 	 */
 	public void visitFixnumNode(FixnumNode iVisited) {
 		handleNode(iVisited);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitFlipNode(org.jruby.ast.FlipNode)
 	 */
 	public void visitFlipNode(FlipNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getBeginNode());
 		visitNode(iVisited.getEndNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitFloatNode(org.jruby.ast.FloatNode)
 	 */
 	public void visitFloatNode(FloatNode iVisited) {
 		handleNode(iVisited);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitForNode(org.jruby.ast.ForNode)
 	 */
 	public void visitForNode(ForNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getVarNode());
 		visitNode(iVisited.getIterNode());
 		visitNode(iVisited.getBodyNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitGlobalAsgnNode(org.jruby.ast.GlobalAsgnNode)
 	 */
 	public void visitGlobalAsgnNode(GlobalAsgnNode iVisited) {
 		handleNode(iVisited);
 
 		String name = iVisited.getName();
 		RubyGlobal global = new RubyGlobal(script, name);
 
 		RubyFieldElementInfo info = new RubyFieldElementInfo();
 		info.setTypeName(estimateValueType(iVisited.getValueNode()));
 		ISourcePosition pos = iVisited.getPosition();
 		setTokenRange(pos, info, name);
 		// TODO Set more info!
 
 		scriptInfo.addChild(global);
 
 		newElements.put(global, info);
 
 		visitNode(iVisited.getValueNode());
 	}
 
 	/**
 	 * @param pos
 	 * @param info
 	 */
 	private void setTokenRange(ISourcePosition pos, RubyFieldElementInfo info, String name) {
 		int realStart = pos.getStartOffset() - name.length();
 		info.setNameSourceStart(realStart);
 		info.setNameSourceEnd(pos.getStartOffset() - 1);
 		info.setSourceRangeStart(realStart);
 		info.setSourceRangeEnd(pos.getStartOffset() - 10);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitGlobalVarNode(org.jruby.ast.GlobalVarNode)
 	 */
 	public void visitGlobalVarNode(GlobalVarNode iVisited) {
 		handleNode(iVisited);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitHashNode(org.jruby.ast.HashNode)
 	 */
 	public void visitHashNode(HashNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getListNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitInstAsgnNode(org.jruby.ast.InstAsgnNode)
 	 */
 	public void visitInstAsgnNode(InstAsgnNode iVisited) {
 		handleNode(iVisited);
 
 		String name = iVisited.getName();
 		RubyElement type = getCurrentType();
 		RubyInstVar var = new RubyInstVar(type, name);
 
 		RubyElementInfo parentInfo = getCurrentTypeInfo();
 		parentInfo.addChild(var);
 
 		RubyFieldElementInfo info = new RubyFieldElementInfo();
 		// TODO Add more information to the info object!
 		ISourcePosition pos = iVisited.getPosition();
 		//setTokenRange(pos, info, name);
 		setInstanceVarRange( pos, info, name );
 		info.setTypeName(estimateValueType(iVisited.getValueNode()));
 
 		newElements.put(var, info);
 
 		visitNode(iVisited.getValueNode());
 	}
 
 	/**
 	 * @param value
 	 * @return
 	 */
 	private String estimateValueType(Node value) {
 		if (value instanceof CallNode) {
 			CallNode call = (CallNode) value;
 			if (call.getName().equals("new")) {
 				Node receiver = call.getReceiverNode();
 				if (receiver instanceof ConstNode) {
 					ConstNode constNode = (ConstNode) receiver;
 					return constNode.getName();
 				}
 			}
 		} else if (value instanceof DStrNode) {
 			return "String";
 
 		} else if (value instanceof FixnumNode) {
 			return "Fixnum";
 
 		} else if (value instanceof BignumNode) { return "Bignum"; }
 		return null;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitInstVarNode(org.jruby.ast.InstVarNode)
 	 */
 	public void visitInstVarNode(InstVarNode iVisited) {
 		handleNode(iVisited);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitIfNode(org.jruby.ast.IfNode)
 	 */
 	public void visitIfNode(IfNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getCondition());
 		visitNode(iVisited.getThenBody());
 		visitNode(iVisited.getElseBody());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitIterNode(org.jruby.ast.IterNode)
 	 */
 	public void visitIterNode(IterNode iVisited) {
 		handleNode(iVisited);
 
 		RubyBlock block = new RubyBlock(modelStack.peek());
 		modelStack.push(block);
 
 		visitNode(iVisited.getIterNode());
 		visitNode(iVisited.getVarNode());
 		visitNode(iVisited.getBodyNode());
 		if (DEBUG) System.out.println("Iter Node ended");
 
 		modelStack.pop();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitLocalAsgnNode(org.jruby.ast.LocalAsgnNode)
 	 */
 	public void visitLocalAsgnNode(LocalAsgnNode iVisited) {
 		handleNode(iVisited);
 		RubyLocalVar var = new RubyLocalVar(modelStack.peek(), iVisited.getName());
 		modelStack.push(var);
 
 		RubyElementInfo parentInfo = infoStack.peek();
 		parentInfo.addChild(var);
 
 		RubyFieldElementInfo info = new RubyFieldElementInfo();
 		info.setTypeName(estimateValueType(iVisited.getValueNode()));
 		
 		// TODO Set the info!
 		ISourcePosition pos = iVisited.getPosition();
 		setLocalVarRange( pos, info, iVisited.getName() );
 		infoStack.push(info);
 
 		newElements.put(var, info);
 
 		visitNode(iVisited.getValueNode());
 
 		modelStack.pop();
 		infoStack.pop();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitLocalVarNode(org.jruby.ast.LocalVarNode)
 	 */
 	public void visitLocalVarNode(LocalVarNode iVisited) {
 		handleNode(iVisited);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitMultipleAsgnNode(org.jruby.ast.MultipleAsgnNode)
 	 */
 	public void visitMultipleAsgnNode(MultipleAsgnNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getHeadNode());
 		visitNode(iVisited.getArgsNode());
 		visitNode(iVisited.getValueNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitMatch2Node(org.jruby.ast.Match2Node)
 	 */
 	public void visitMatch2Node(Match2Node iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getReceiverNode());
 		visitNode(iVisited.getValueNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitMatch3Node(org.jruby.ast.Match3Node)
 	 */
 	public void visitMatch3Node(Match3Node iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getReceiverNode());
 		visitNode(iVisited.getValueNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitMatchNode(org.jruby.ast.MatchNode)
 	 */
 	public void visitMatchNode(MatchNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getRegexpNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitModuleNode(org.jruby.ast.ModuleNode)
 	 */
 	public void visitModuleNode(ModuleNode iVisited) {
 		handleNode(iVisited);
 		String name = iVisited.getCPath().getName();
 		RubyModule module = new RubyModule(modelStack.peek(), name);
 		modelStack.push(module);
 
 		RubyElementInfo parentInfo = infoStack.peek();
 		parentInfo.addChild(module);
 
 		RubyTypeElementInfo info = new RubyTypeElementInfo();
 		info.setHandle(module);
 		ISourcePosition pos = iVisited.getPosition();
 		setKeywordRange("module", pos, info, name);
 		// TODO Set more info!
 		infoStack.push(info);
 
 		newElements.put(module, info);
 
 		visitNode(iVisited.getBodyNode());
 
 		modelStack.pop();
 		infoStack.pop();
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitNewlineNode(org.jruby.ast.NewlineNode)
 	 */
 	public void visitNewlineNode(NewlineNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getNextNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitNextNode(org.jruby.ast.NextNode)
 	 */
 	public void visitNextNode(NextNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getValueNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitNilNode(org.jruby.ast.NilNode)
 	 */
 	public void visitNilNode(NilNode iVisited) {
 		handleNode(iVisited);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitNotNode(org.jruby.ast.NotNode)
 	 */
 	public void visitNotNode(NotNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getConditionNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitNthRefNode(org.jruby.ast.NthRefNode)
 	 */
 	public void visitNthRefNode(NthRefNode iVisited) {
 		handleNode(iVisited);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitOpElementAsgnNode(org.jruby.ast.OpElementAsgnNode)
 	 */
 	public void visitOpElementAsgnNode(OpElementAsgnNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getReceiverNode());
 		visitNode(iVisited.getArgsNode());
 		visitNode(iVisited.getValueNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitOpAsgnNode(org.jruby.ast.OpAsgnNode)
 	 */
 	public void visitOpAsgnNode(OpAsgnNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getReceiverNode());
 		visitNode(iVisited.getValueNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitOpAsgnAndNode(org.jruby.ast.OpAsgnAndNode)
 	 */
 	public void visitOpAsgnAndNode(OpAsgnAndNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getFirstNode());
 		visitNode(iVisited.getSecondNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitOpAsgnOrNode(org.jruby.ast.OpAsgnOrNode)
 	 */
 	public void visitOpAsgnOrNode(OpAsgnOrNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getFirstNode());
 		visitNode(iVisited.getSecondNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitOptNNode(org.jruby.ast.OptNNode)
 	 */
 	public void visitOptNNode(OptNNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getBodyNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitOrNode(org.jruby.ast.OrNode)
 	 */
 	public void visitOrNode(OrNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getFirstNode());
 		visitNode(iVisited.getSecondNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitPostExeNode(org.jruby.ast.PostExeNode)
 	 */
 	public void visitPostExeNode(PostExeNode iVisited) {
 		handleNode(iVisited);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitRedoNode(org.jruby.ast.RedoNode)
 	 */
 	public void visitRedoNode(RedoNode iVisited) {
 		handleNode(iVisited);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitRegexpNode(org.jruby.ast.RegexpNode)
 	 */
 	public void visitRegexpNode(RegexpNode iVisited) {
 		handleNode(iVisited);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitRescueBodyNode(org.jruby.ast.RescueBodyNode)
 	 */
 	public void visitRescueBodyNode(RescueBodyNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getExceptionNodes());
 		visitNode(iVisited.getOptRescueNode());
 		visitNode(iVisited.getBodyNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitRescueNode(org.jruby.ast.RescueNode)
 	 */
 	public void visitRescueNode(RescueNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getRescueNode());
 		visitNode(iVisited.getBodyNode());
 		visitNode(iVisited.getElseNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitRetryNode(org.jruby.ast.RetryNode)
 	 */
 	public void visitRetryNode(RetryNode iVisited) {
 		handleNode(iVisited);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitReturnNode(org.jruby.ast.ReturnNode)
 	 */
 	public void visitReturnNode(ReturnNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getValueNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitSClassNode(org.jruby.ast.SClassNode)
 	 */
 	public void visitSClassNode(SClassNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getReceiverNode());
 		visitNode(iVisited.getBodyNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitScopeNode(org.jruby.ast.ScopeNode)
 	 */
 	public void visitScopeNode(ScopeNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getBodyNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitSelfNode(org.jruby.ast.SelfNode)
 	 */
 	public void visitSelfNode(SelfNode iVisited) {
 		handleNode(iVisited);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitSplatNode(org.jruby.ast.SplatNode)
 	 */
 	public void visitSplatNode(SplatNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getValue());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitStrNode(org.jruby.ast.StrNode)
 	 */
 	public void visitStrNode(StrNode iVisited) {
 		handleNode(iVisited);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitSuperNode(org.jruby.ast.SuperNode)
 	 */
 	public void visitSuperNode(SuperNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getArgsNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitSValueNode(org.jruby.ast.SValueNode)
 	 */
 	public void visitSValueNode(SValueNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getValue());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitSymbolNode(org.jruby.ast.SymbolNode)
 	 */
 	public void visitSymbolNode(SymbolNode iVisited) {
 		handleNode(iVisited);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitToAryNode(org.jruby.ast.ToAryNode)
 	 */
 	public void visitToAryNode(ToAryNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getValue());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitTrueNode(org.jruby.ast.TrueNode)
 	 */
 	public void visitTrueNode(TrueNode iVisited) {
 		handleNode(iVisited);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitUndefNode(org.jruby.ast.UndefNode)
 	 */
 	public void visitUndefNode(UndefNode iVisited) {
 		handleNode(iVisited);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitUntilNode(org.jruby.ast.UntilNode)
 	 */
 	public void visitUntilNode(UntilNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getConditionNode());
 		visitNode(iVisited.getBodyNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitVAliasNode(org.jruby.ast.VAliasNode)
 	 */
 	public void visitVAliasNode(VAliasNode iVisited) {
 		handleNode(iVisited);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitVCallNode(org.jruby.ast.VCallNode)
 	 */
 	public void visitVCallNode(VCallNode iVisited) {
 		handleNode(iVisited);
 		String functionName = iVisited.getMethodName();
 		if (functionName.equals("public")) {
 			currentVisibility = Visibility.PUBLIC;
 		} else if (functionName.equals("private")) {
 			currentVisibility = Visibility.PRIVATE;
 		} else if (functionName.equals("protected")) {
 			currentVisibility = Visibility.PROTECTED;
 		}
 		// TODO Set the method visibility for any arguments to the above
 		// methods!
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitWhenNode(org.jruby.ast.WhenNode)
 	 */
 	public void visitWhenNode(WhenNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getExpressionNodes());
 		visitNode(iVisited.getBodyNode());
 		visitNode(iVisited.getNextCase());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitWhileNode(org.jruby.ast.WhileNode)
 	 */
 	public void visitWhileNode(WhileNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getConditionNode());
 		visitNode(iVisited.getBodyNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitXStrNode(org.jruby.ast.XStrNode)
 	 */
 	public void visitXStrNode(XStrNode iVisited) {
 		handleNode(iVisited);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitYieldNode(org.jruby.ast.YieldNode)
 	 */
 	public void visitYieldNode(YieldNode iVisited) {
 		handleNode(iVisited);
 		visitNode(iVisited.getArgsNode());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitZArrayNode(org.jruby.ast.ZArrayNode)
 	 */
 	public void visitZArrayNode(ZArrayNode iVisited) {
 		handleNode(iVisited);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.jruby.ast.visitor.NodeVisitor#visitZSuperNode(org.jruby.ast.ZSuperNode)
 	 */
 	public void visitZSuperNode(ZSuperNode iVisited) {
 		handleNode(iVisited);
 	}
 
 	private void visitNode(Node iVisited) {
 		if (iVisited != null) iVisited.accept(this);
 	}
 
 	/**
 	 * @param visited
 	 */
 	private void handleNode(Node visited) {
 		// TODO Uncomment for logging?
 		if (DEBUG ) System.out.println(visited.toString() + ", position -> " + visited.getPosition());
 	}
 
 }
