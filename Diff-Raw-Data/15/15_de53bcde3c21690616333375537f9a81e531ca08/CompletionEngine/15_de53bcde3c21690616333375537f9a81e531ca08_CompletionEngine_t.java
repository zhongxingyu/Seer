 package org.rubypeople.rdt.internal.codeassist;
 
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import org.eclipse.core.resources.IResource;
 import org.eclipse.core.runtime.IPath;
 import org.jruby.ast.ClassNode;
 import org.jruby.ast.ClassVarAsgnNode;
 import org.jruby.ast.ClassVarDeclNode;
 import org.jruby.ast.ClassVarNode;
 import org.jruby.ast.Colon2Node;
 import org.jruby.ast.ConstNode;
 import org.jruby.ast.DefnNode;
 import org.jruby.ast.DefsNode;
 import org.jruby.ast.InstAsgnNode;
 import org.jruby.ast.InstVarNode;
 import org.jruby.ast.MethodDefNode;
 import org.jruby.ast.ModuleNode;
 import org.jruby.ast.Node;
 import org.jruby.ast.types.INameNode;
 import org.jruby.lexer.yacc.SyntaxException;
 import org.jruby.parser.StaticScope;
 import org.rubypeople.rdt.core.CompletionProposal;
 import org.rubypeople.rdt.core.CompletionRequestor;
 import org.rubypeople.rdt.core.Flags;
 import org.rubypeople.rdt.core.IMethod;
 import org.rubypeople.rdt.core.IOpenable;
 import org.rubypeople.rdt.core.IRubyElement;
 import org.rubypeople.rdt.core.IRubyModel;
 import org.rubypeople.rdt.core.IRubyProject;
 import org.rubypeople.rdt.core.IRubyScript;
 import org.rubypeople.rdt.core.ISourceRange;
 import org.rubypeople.rdt.core.IType;
 import org.rubypeople.rdt.core.RubyCore;
 import org.rubypeople.rdt.core.RubyModelException;
 import org.rubypeople.rdt.internal.core.RubyElement;
 import org.rubypeople.rdt.internal.core.RubyType;
 import org.rubypeople.rdt.internal.core.parser.RubyParser;
 import org.rubypeople.rdt.internal.core.search.ExperimentalIndex;
 import org.rubypeople.rdt.internal.core.util.ASTUtil;
 import org.rubypeople.rdt.internal.ti.DefaultTypeInferrer;
 import org.rubypeople.rdt.internal.ti.ITypeGuess;
 import org.rubypeople.rdt.internal.ti.ITypeInferrer;
 import org.rubypeople.rdt.internal.ti.util.AttributeLocator;
 import org.rubypeople.rdt.internal.ti.util.ClosestSpanningNodeLocator;
 import org.rubypeople.rdt.internal.ti.util.INodeAcceptor;
 import org.rubypeople.rdt.internal.ti.util.ScopedNodeLocator;
 
 public class CompletionEngine {
 	private CompletionRequestor requestor;
 	private CompletionContext context;
 
 	public CompletionEngine(CompletionRequestor requestor) {
 		this.requestor = requestor;
 	}
 
 	public void complete(IRubyScript script, int offset) throws RubyModelException {
 		this.requestor.beginReporting();		
 		context = new CompletionContext(script, offset);
 		if (context.emptyPrefix()) { // no prefix, so we could suggest anything
 			suggestTypeNames();
 			suggestConstantNames();
 			suggestGlobals();
 			getDocumentsRubyElementsInScope();
 		} else {
 			if (context.isConstant()) { // type or constant
 				suggestTypeNames();
 				suggestConstantNames();
 			} 
 			if (context.isMethodInvokation()) {
 				ITypeInferrer inferrer = new DefaultTypeInferrer();
				List<ITypeGuess> guesses = inferrer.infer(context.getCorrectedSource(), context.getOffset());
 				RubyElementRequestor requestor = new RubyElementRequestor(script);
 				for (ITypeGuess guess : guesses) {
 					String name = guess.getType();
					IType[] types = requestor.findType(name);  // FIXME When syntax is broken, grabbing type that is defined in same script like this just doesn't work!
 					for (int i = 0; i < types.length; i++) {
 						suggestMethods(guess.getConfidence(), types[i]);
 					}
 				}
 			} else {
 				// FIXME Traverse the IRubyElement model, not nodes (and don't reparse)?
 				getDocumentsRubyElementsInScope();
 			}
 			if (context.isGlobal()) { // looks like a global
 				suggestGlobals();
 			}
 		}
 		this.requestor.endReporting();
 		context = null;
 	}
 	
 	private void suggestGlobals() {
 		Set<String> globals = ExperimentalIndex.getGlobalNames();
 		// TODO Sort?
 		for (String name : globals) {
 			if (!context.prefixStartsWith(name))
 				continue;
 			CompletionProposal proposal = createProposal(context.getReplaceStart(), CompletionProposal.FIELD_REF, name);
 			requestor.accept(proposal);
 		}
 	}
 
 	private void suggestTypeNames() {
 		Set<String> types = ExperimentalIndex.getTypeNames();
 		// TODO Sort?
 		for (String name : types) {
 			if (!context.prefixStartsWith(name))
 				continue;
 			CompletionProposal proposal = createProposal(context.getReplaceStart(), CompletionProposal.TYPE_REF, name);
 			proposal.setType(name);
 			requestor.accept(proposal);
 		}
 	}
 
 	private CompletionProposal createProposal(int replaceStart, int type, String name) {
 		CompletionProposal proposal = new CompletionProposal(type, name, 100);
 		proposal.setReplaceRange(replaceStart, replaceStart + name.length());
 		return proposal;
 	}
 
 	private void suggestConstantNames() {
 		Set<String> types = ExperimentalIndex.getConstantNames();
 		// TODO Sort?
 		for (String name : types) {
 			if (!context.prefixStartsWith(name))
 				continue;
 			CompletionProposal proposal = createProposal(context.getReplaceStart(), CompletionProposal.FIELD_REF, name);
 			requestor.accept(proposal);
 		}
 	}
 
 	private void suggestMethods(int confidence, IType type) throws RubyModelException {
 		if (type == null)
 			return;		
 		IMethod[] methods = type.getMethods();
 		for (int k = 0; k < methods.length; k++) {
 			suggestMethod(methods[k], type.getElementName(), confidence);
 		}
 	}
 
 	private void suggestMethod(IMethod method, String typeName, int confidence) {
 		int start = context.getReplaceStart();
 		String name = method.getElementName();
 		int flags = Flags.AccDefault;
 		if (method.isSingleton()) {
 			flags |= Flags.AccStatic;
 			name = name.substring(typeName.length() + 1);
 		} else {
 //			 FIXME Don't show instance methods if the thing we're working on is a constant (class name)!
 		}
 		if (!context.prefixStartsWith(name))
 			return;
 		
 		try {
 			switch (method.getVisibility()) {
 			case IMethod.PRIVATE:
 				flags |= Flags.AccPrivate;
 				break;
 			case IMethod.PUBLIC:
 				flags |= Flags.AccPublic;
 				break;
 			case IMethod.PROTECTED:
 				flags |= Flags.AccProtected;
 				break;
 			default:
 				break;
 			}
 		} catch (RubyModelException e) {
 			RubyCore.log(e);
 			flags |= Flags.AccPublic;
 		}
 		CompletionProposal proposal = new CompletionProposal(CompletionProposal.METHOD_REF, name, confidence);
 		proposal.setReplaceRange(start, start + name.length());
 		proposal.setFlags(flags);
 		proposal.setName(name);
 		proposal.setDeclaringType(typeName);
 		requestor.accept(proposal);
 		
 	}
 
 	/**
 	 * Gets all the distinct elements in the current RubyScript
 	 * 
 	 * @param offset
 	 * @param replaceStart
 	 * 
 	 * @return a List of the names of all the elements in the current RubyScript
 	 */
 	private void getDocumentsRubyElementsInScope() {
 		try {
 			// FIXME Try to stop all the multiple re-parsing of the source! Can
 			// we parse once and pass the root node around?
 			// Parse
 			Node rootNode = (new RubyParser()).parse(context.getCorrectedSource());
 			if (rootNode == null) {
 				return;
 			}
 
 			// Find the enclosing method to get locals and args
 			Node enclosingMethodNode = ClosestSpanningNodeLocator.Instance().findClosestSpanner(rootNode, context.getOffset(), new INodeAcceptor() {
 				public boolean doesAccept(Node node) {
 					return (node instanceof DefnNode || node instanceof DefsNode);
 				}
 			});
 
 			addLocalVariablesAndArguments(enclosingMethodNode);
 
 			// Find the enclosing type (class or module) to get instance and
 			// classvars from
 			Node enclosingTypeNode = ClosestSpanningNodeLocator.Instance().findClosestSpanner(rootNode, context.getOffset(), new INodeAcceptor() {
 				public boolean doesAccept(Node node) {
 					return (node instanceof ClassNode || node instanceof ModuleNode);
 				}
 			});
 
 			// Add members from enclosing type
 			if (enclosingTypeNode != null) {
 				getMembersAvailableInsideType(enclosingTypeNode);
 			}
 		} catch (RubyModelException rme) {
 			RubyCore.log(rme);
 			RubyCore.log("RubyModelException in CompletionEngine::getElementsInScope()");
 		} catch (SyntaxException se) {
 			RubyCore.log(se);
 			RubyCore.log("SyntaxError in CompletionEngine::getElementsInScope()");
 		}
 	}
 
 	private void addLocalVariablesAndArguments(Node enclosingMethodNode) {
 		// Add local vars and arguments
 		if (enclosingMethodNode != null && enclosingMethodNode instanceof MethodDefNode) {
 			Set<String> matches = new HashSet<String>();
 			StaticScope scope = ((MethodDefNode) enclosingMethodNode).getScope();
 			if (scope != null && scope.getVariables().length > 0) {
 				List locals = Arrays.asList(scope.getVariables());
 				for (Iterator iter = locals.iterator(); iter.hasNext();) {
 					String local = (String) iter.next();
 					if (!context.prefixStartsWith(local))
 						continue;
 					matches.add(local);
 				}
 			}
 			for (String local : matches) { // Avoid duplicates
 				CompletionProposal proposal = new CompletionProposal(CompletionProposal.LOCAL_VARIABLE_REF, local, 100);
 				proposal.setReplaceRange(context.getReplaceStart(), context.getReplaceStart() + local.length());
 				requestor.accept(proposal);
 			}
 		}
 	}
 
 	/**
 	 * Gets the members available inside a type node (ModuleNode, ClassNode): -
 	 * Instance variables - Class variables - Methods
 	 * 
 	 * @param typeNode
 	 * @return
 	 */
 	private void getMembersAvailableInsideType(Node typeNode) throws RubyModelException {
 		if (typeNode == null) {
 			return;
 		}
 
 		String typeName = getTypeName(typeNode);
 		if (typeName == null) {
 			return;
 		}
 
 		// XXX rubyType may not be in script, but rather be defined in another
 		// script
 		// IType rubyType = new RubyType( (RubyElement)script, typeName );
 		// Better method:
 		// Find the named type
 		// IType rubyType = findTypeFromAllProjects(typeName, script);
 
 		// System.out.println(" -- Located RubyType info.");
 		// System.out.println(" -- Superclass: " + rubyType.getSuperclassName()
 		// );
 
 		// if ( rubyType != null ) {
 		// String[] includedModuleNames = rubyType.getIncludedModuleNames();
 		// if ( includedModuleNames != null ) {
 		// for ( String moduleName : rubyType.getIncludedModuleNames() ) {
 		// System.out.println(" -- Includes module: " + moduleName);
 		// }
 		// }
 		// }
 
 		// Get superclass and add its public members
 		List<Node> superclassNodes = getSuperclassNodes(typeNode);
 		for (Node superclassNode : superclassNodes) {
 			getMembersAvailableInsideType(superclassNode);
 		}
 
 		// Get public members of mixins
 		List<String> mixinNames = getIncludedMixinNames(typeName);
 		for (String mixinName : mixinNames) {
 			List<Node> mixinDeclarations = getTypeDeclarationNodes(mixinName);
 			for (Node mixinDeclaration : mixinDeclarations) {
 				getMembersAvailableInsideType(mixinDeclaration);
 			}
 		}
 
 		// Get method names defined by DefnNodes and DefsNodes
 		List<Node> methodDefinitions = ScopedNodeLocator.Instance().findNodesInScope(typeNode, new INodeAcceptor() {
 			public boolean doesAccept(Node node) {
 				return (node instanceof DefnNode) || (node instanceof DefsNode);
 			}
 		});
 		for (Node methodDefinition : methodDefinitions) {
 			String name = null;
 			if (methodDefinition instanceof DefnNode) {
 				name = ((DefnNode) methodDefinition).getName();
 			}
 			if (methodDefinition instanceof DefsNode) {
 				name = ((DefsNode) methodDefinition).getName();
 			}
 			if (!context.prefixStartsWith(name))
 				continue;
 			NodeMethod method = new NodeMethod(methodDefinition);
 			suggestMethod(method, typeName, 100);
 		}
 		addTypesVariables(typeNode);
 	}
 
 	private String getTypeName(Node typeNode) {
 		// Get type name
 		String typeName = null;
 		if (typeNode instanceof ClassNode) {
 			typeName = ((Colon2Node) ((ClassNode) typeNode).getCPath()).getName();
 		}
 		if (typeNode instanceof ModuleNode) {
 			typeName = ((Colon2Node) ((ModuleNode) typeNode).getCPath()).getName();
 		}
 		return typeName;
 	}
 
 	private void addTypesVariables(Node typeNode) {
 		// Get instance and class variables available in the enclosing type
 		List<Node> instanceAndClassVars = ScopedNodeLocator.Instance().findNodesInScope(typeNode, new INodeAcceptor() {
 			public boolean doesAccept(Node node) {
 				return (node instanceof InstVarNode || node instanceof InstAsgnNode || node instanceof ClassVarNode || node instanceof ClassVarDeclNode || node instanceof ClassVarAsgnNode);
 			}
 		});
 		Set<String> fields = new HashSet<String>();
 		if (instanceAndClassVars != null) {
 			// Get the unique names of instance and class variables
 			for (Node varNode : instanceAndClassVars) {
 				String name = ASTUtil.getNameReflectively(varNode);
 				if (!context.prefixStartsWith(name))
 					continue;
 				fields.add(name);
 			}
 		}
 		// Get instance and class vars defined by [c]attr_* calls
 		List<String> attrs = AttributeLocator.Instance().findInstanceAttributesInScope(typeNode);
 		for (Iterator iter = attrs.iterator(); iter.hasNext();) {
 			String attr = (String) iter.next();
 			if (!context.prefixStartsWith(attr))
 				continue;
 			fields.add(attr);
 		}
 		for (String field : fields) {
 			CompletionProposal proposal = new CompletionProposal(CompletionProposal.FIELD_REF, field, 100);
 			proposal.setReplaceRange(context.getReplaceStart(), context.getReplaceStart() + field.length());
 			requestor.accept(proposal);
 		}
 	}
 
 	/**
 	 * Finds all nodes that declare a type that is a superclass of the specified
 	 * node. Example:
 	 * 
 	 * """ class Klass;def meth_1;1;end;end class Klass;def meth_2;2;end;end
 	 * 
 	 * class SubKlass < Klass;end """
 	 * 
 	 * Issuing getSuperClassNodes() on the ClassNode declaring SubKlass would
 	 * return two ClassNodes; one for each definition of Klass.
 	 * 
 	 * @param typeNode
 	 *            Node to find superclass nodes of
 	 * @return List of ClassNode or ModuleNode
 	 */
 	private List<Node> getSuperclassNodes(Node typeNode) {
 		if (typeNode instanceof ClassNode) {
 			Node superNode = ((ClassNode) typeNode).getSuperNode();
 			if (superNode instanceof ConstNode) {
 				String superclassName = ((ConstNode) superNode).getName();
 				return getTypeDeclarationNodes(superclassName);
 			}
 		}
 		return new ArrayList<Node>();
 	}
 
 	/** Lookup type declaration nodes */
 	private List<Node> getTypeDeclarationNodes(String typeName) {
 		System.out.println("Being asked for the type decl node for " + typeName);
 
 		// Find the named type
 		RubyElementRequestor requestor = new RubyElementRequestor(context.getScript());
 		IType[] types = requestor.findType(typeName);
 		IType type = types[0];
 
 		try {
 			if (type instanceof RubyType) {
 
 				// FIXME This feels a little hacky and backwards -
 				// RubyType.getSource() and then parse... consider reworking the
 				// clients to this method to accept RubyTypes or something
 				// similar?
 				// Find source and parse
 				RubyType rubyType = (RubyType) type;
 				String source = rubyType.getSource();
 
 				// FIXME Why does the parser balk on \r chars?
 				source = source.replace('\r', ' ');
 				Node rootNode = (new RubyParser()).parse(source);
 
 				// Bail if the parse fails
 				if (rootNode == null) {
 					return new ArrayList<Node>();
 				}
 
 				// Return any type declaration nodes in included source
 				return ScopedNodeLocator.Instance().findNodesInScope(rootNode, new INodeAcceptor() {
 					public boolean doesAccept(Node node) {
 						return (node instanceof ClassNode) || (node instanceof ModuleNode);
 					}
 				});
 			}
 
 		} catch (RubyModelException rme) {
 			rme.printStackTrace();
 		}
 
 		return new ArrayList<Node>(0);
 	}
 
 	private List<String> getIncludedMixinNames(String typeName) {
 		IType rubyType = new RubyType((RubyElement)context.getScript(), typeName);
 
 		try {
 			String[] includedModuleNames = rubyType.getIncludedModuleNames();
 			if (includedModuleNames != null) {
 				return Arrays.asList(rubyType.getIncludedModuleNames());
 			} 
 			return new ArrayList<String>(0);
 		} catch (RubyModelException e) {
 			return new ArrayList<String>(0);
 		}
 	}
 	
 	private class NodeMethod implements IMethod {
 
 		private Node node;
 
 		public NodeMethod(Node methodDefinition) {
 			this.node = methodDefinition;
 		}
 
 		public String[] getParameterNames() throws RubyModelException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public int getVisibility() throws RubyModelException {
 			// TODO Auto-generated method stub
 			return IMethod.PUBLIC;
 		}
 
 		public boolean isConstructor() {
 			if (node instanceof DefnNode) {
 				return ((DefnNode)node).getName().equals("initialize");
 			}
 			return false;
 		}
 
 		public boolean isSingleton() {
 			return node instanceof DefsNode;
 		}
 
 		public boolean exists() {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		public IRubyElement getAncestor(int ancestorType) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IResource getCorrespondingResource() throws RubyModelException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public String getElementName() {
 			// TODO Auto-generated method stub
 			if (node instanceof INameNode) {
 				return ((INameNode)node).getName();
 			}
 			return null;
 		}
 
 		public int getElementType() {
 			return IRubyElement.METHOD;
 		}
 
 		public IOpenable getOpenable() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IRubyElement getParent() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IPath getPath() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IRubyElement getPrimaryElement() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IResource getResource() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IRubyModel getRubyModel() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IRubyProject getRubyProject() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IResource getUnderlyingResource() throws RubyModelException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public boolean isReadOnly() {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		public boolean isStructureKnown() throws RubyModelException {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 		public boolean isType(int type) {
 			return type == IRubyElement.METHOD;
 		}
 
 		public Object getAdapter(Class adapter) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IType getDeclaringType() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public ISourceRange getNameRange() throws RubyModelException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IRubyScript getRubyScript() {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IType getType(String name, int occurrenceCount) {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public String getSource() throws RubyModelException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public ISourceRange getSourceRange() throws RubyModelException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public IRubyElement[] getChildren() throws RubyModelException {
 			// TODO Auto-generated method stub
 			return null;
 		}
 
 		public boolean hasChildren() throws RubyModelException {
 			// TODO Auto-generated method stub
 			return false;
 		}
 
 	}
 }
