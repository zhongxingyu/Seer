 package com.gratex.perconik.activity.listeners;
 
 import java.util.Collection;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Objects;
 import org.eclipse.jdt.core.dom.ASTMatcher;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.AbstractTypeDeclaration;
 import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
 import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
 import org.eclipse.jdt.core.dom.EnumDeclaration;
 import org.eclipse.jdt.core.dom.FieldDeclaration;
 import org.eclipse.jdt.core.dom.Initializer;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 import org.eclipse.jdt.core.dom.VariableDeclarationFragment;
 import sk.stuba.fiit.perconik.core.java.dom.difference.AstDifference;
 import sk.stuba.fiit.perconik.eclipse.jdt.core.dom.AstApiLevel;
 import sk.stuba.fiit.perconik.eclipse.jdt.core.dom.AstNodeType;
 import com.google.common.annotations.Beta;
 import com.google.common.collect.Lists;
 import difflib.Delta;
 import difflib.DiffUtils;
 import difflib.Patch;
 
 @Beta
 class CompilationUnitDifferencer
 {
 	static final ASTMatcher matcher = new ASTMatcher(true);
 	
 	private final RelevantAstCollector collector;
 	
	private AstDifference.Builder builder;
 	
 	public CompilationUnitDifferencer()
 	{
 		this.collector = new RelevantAstCollector();
 	}
 	
	public final AstDifference difference(final CompilationUnit original, final CompilationUnit revised)
 	{
		this.builder = new AstDifference.Builder();
 		
 		if (original != null || revised != null)
 		{
 			List<ASTNode> originalNodes = this.collector.collect(original);
 			List<ASTNode> revisedNodes  = this.collector.collect(revised);
 			
 			this.compute(originalNodes, revisedNodes);
 		}
 		
 		return this.builder.build();
 	}
 		
 	private final void compute(final Collection<?> original, final Collection<?> revised)
 	{
 		final Patch patch = DiffUtils.diff(wrap(original), wrap(revised));
 
 		for (final Delta delta: patch.getDeltas())
 		{
 			switch (delta.getType())
 			{
 				case DELETE:
 					for (ASTNode node: unwrap(delta.getOriginal().getLines()))
 					{
 						this.builder.delete(node);
 					}
 					
 					break;
 				case INSERT:
 					for (ASTNode node: unwrap(delta.getRevised().getLines()))
 					{
 						this.builder.add(node);
 					}
 					
 					break;
 				case CHANGE:
 					List<ASTNode> originalNodes = unwrap(delta.getOriginal().getLines());
 					List<ASTNode> revisedNodes  = unwrap(delta.getRevised().getLines());
 					
 					List<ASTNode> unmatchedOriginalNodes = Lists.newLinkedList();
 					
 					main:
 					for (ASTNode originalNode: originalNodes)
 					{
 						int revisedNodesSize = revisedNodes.size();
 						
 						for (int k = 0; k < revisedNodesSize; k ++)
 						{
 							ASTNode revisedNode = revisedNodes.get(k);
 							
 							if (isSimilar(originalNode, revisedNode))
 							{
 								this.builder.modify(originalNode, revisedNode);
 								
 								revisedNodes.remove(k);
 								
 								continue main;
 							}
 						}
 						
 						unmatchedOriginalNodes.add(originalNode);
 					}
 
 					for (ASTNode originalNode: unmatchedOriginalNodes)
 					{
 						this.builder.delete(originalNode);
 					}
 
 					for (ASTNode revisedNode: revisedNodes)
 					{
 						this.builder.add(revisedNode);
 					}
 
 					break;
 				default:
 					throw new AssertionError();
 			}
 		}
 	}
 
 	private static final class AstNodeEqualsWrapper
 	{
 		final ASTNode node;
 		
 		AstNodeEqualsWrapper(ASTNode node)
 		{
 			this.node = node;
 		}
 
 		@Override
 		public final boolean equals(Object o)
 		{
 			if (this == o)
 			{
 				return true;
 			}
 			
 			if (!(o instanceof AstNodeEqualsWrapper))
 			{
 				return false;
 			}
 			
 			AstNodeEqualsWrapper other = (AstNodeEqualsWrapper) o;
 			
 			return matcher.safeSubtreeMatch(this.node, other.node);
 		}
 
 		@Override
 		public final int hashCode()
 		{
 			// TODO better hash code? now it is inconsistent with equals
 			return Objects.hashCode(this.node);
 		}
 	}
 	
 	private final static List<AstNodeEqualsWrapper> wrap(final Collection<?> objects)
 	{
 		final List<AstNodeEqualsWrapper> wrapped = Lists.newArrayListWithExpectedSize(objects.size());
 		
 		for (final Object o: objects)
 		{
 			wrapped.add(new AstNodeEqualsWrapper((ASTNode) o));
 		}
 		
 		return wrapped;
 	}
 	
 	private final static List<ASTNode> unwrap(final Collection<?> objects)
 	{
 		final List<ASTNode> nodes = Lists.newArrayListWithExpectedSize(objects.size());
 		
 		for (final Object o: objects)
 		{
 			nodes.add(((AstNodeEqualsWrapper) o).node);
 		}
 		
 		return nodes;
 	}
 	
 	// body declaration routers
 	
 	private static final boolean isSimilar(final ASTNode original, final ASTNode revised)
 	{
 		if (original instanceof AbstractTypeDeclaration)
 		{
 			return isSimilar((AbstractTypeDeclaration) original, revised);
 		}
 		
 		switch (AstNodeType.valueOf(original))
 		{
 			case ANNOTATION_TYPE_MEMBER_DECLARATION:
 				return isSimilar((AnnotationTypeMemberDeclaration) original, revised);
 			case ENUM_CONSTANT_DECLARATION:
 				return isSimilar((EnumConstantDeclaration) original, revised);
 			case FIELD_DECLARATION:
 				return isSimilar((FieldDeclaration) original, revised);
 			case INITIALIZER:
 				return isSimilar((Initializer) original, revised);
 			case METHOD_DECLARATION:
 				return isSimilar((MethodDeclaration) original, revised);
 			default:
 				return false;
 		}
 	}
 	
 	private static final boolean isSimilar(final AbstractTypeDeclaration original, final ASTNode revised)
 	{
 		switch (AstNodeType.valueOf(original))
 		{
 			case ANNOTATION_TYPE_DECLARATION:
 				return isSimilar((AnnotationTypeDeclaration) original, revised);
 			case ENUM_DECLARATION:
 				return isSimilar((EnumDeclaration) original, revised);
 			case TYPE_DECLARATION:
 				return isSimilar((TypeDeclaration) original, revised);
 			default:
 				return false;
 		}
 	}
 	
 	// abstract type declarations
 
 	private static final boolean isSimilar(final AnnotationTypeDeclaration original, final ASTNode revised)
 	{
 		if (!(revised instanceof AnnotationTypeDeclaration))
 		{
 			return false;
 		}
 		
 		AnnotationTypeDeclaration other = (AnnotationTypeDeclaration) revised;
 
 		boolean nameMatches = matcher.safeSubtreeMatch(original.getName(), other.getName());
 		
 		if (nameMatches)
 		{
 			return true;
 		}
 		
 		boolean restMatches = matcher.safeSubtreeMatch(original.getJavadoc(), other.getJavadoc())
 		&& matcher.safeSubtreeListMatch(original.modifiers(), other.modifiers())
 		&& areSimilar(original.bodyDeclarations(), other.bodyDeclarations());
 		
 		return restMatches;
 	}
 
 	private static final boolean isSimilar(final EnumDeclaration original, final ASTNode revised)
 	{
 		if (!(revised instanceof EnumDeclaration))
 		{
 			return false;
 		}
 		
 		EnumDeclaration other = (EnumDeclaration) revised;
 	
 		boolean nameMatches = matcher.safeSubtreeMatch(original.getName(), other.getName());
 		
 		if (nameMatches)
 		{
 			return true;
 		}
 		
 		boolean restMatches = matcher.safeSubtreeMatch(original.getJavadoc(), other.getJavadoc())
 		&& matcher.safeSubtreeListMatch(original.modifiers(), other.modifiers())
 		&& matcher.safeSubtreeListMatch(original.superInterfaceTypes(), other.superInterfaceTypes())
 		&& matcher.safeSubtreeListMatch(original.enumConstants(), other.enumConstants())
 		&& areSimilar(original.bodyDeclarations(), other.bodyDeclarations());
 		
 		return restMatches;
 	}
 
 	private static final boolean isSimilar(final TypeDeclaration original, final ASTNode revised)
 	{
 		if (!(revised instanceof TypeDeclaration))
 		{
 			return false;
 		}
 		
 		TypeDeclaration other = (TypeDeclaration) revised;
 	
 		boolean nameMatches = matcher.safeSubtreeMatch(original.getName(), other.getName());
 		
 		if (nameMatches)
 		{
 			return true;
 		}
 		
 		boolean restMatches = true;
 		
 		switch (AstApiLevel.valueOf(original))
 		{
 			case JLS2:
 				throw new UnsupportedOperationException();
 			case JLS3:
 			case JLS4:
 				restMatches = restMatches
 				&& matcher.safeSubtreeListMatch(original.modifiers(), other.modifiers())
 				&& matcher.safeSubtreeListMatch(original.typeParameters(), other.typeParameters())
 				&& matcher.safeSubtreeMatch(original.getSuperclassType(), other.getSuperclassType())
 				&& matcher.safeSubtreeListMatch(original.superInterfaceTypes(), other.superInterfaceTypes());
 				break;
 			default:
 				throw new AssertionError();
 		}
 	
 		restMatches = restMatches
 		&& original.isInterface() == other.isInterface()
 		&& matcher.safeSubtreeMatch(original.getJavadoc(), other.getJavadoc())
 		&& matcher.safeSubtreeMatch(original.getName(), other.getName())
 		&& areSimilar(original.bodyDeclarations(), other.bodyDeclarations());
 		
 		return restMatches;
 	}
 	
 	// body declarations except abstract type declarations
 
 	private static final boolean isSimilar(final AnnotationTypeMemberDeclaration original, final ASTNode revised)
 	{
 		if (!(revised instanceof AnnotationTypeMemberDeclaration))
 		{
 			return false;
 		}
 		
 		AnnotationTypeMemberDeclaration other = (AnnotationTypeMemberDeclaration) revised;
 
 		boolean nameMatches = matcher.safeSubtreeMatch(original.getName(), other.getName());
 		
 		if (nameMatches)
 		{
 			return true;
 		}
 		
 		boolean restMatches = matcher.safeSubtreeMatch(original.getJavadoc(), other.getJavadoc())
 		&& matcher.safeSubtreeListMatch(original.modifiers(), other.modifiers())
 		&& matcher.safeSubtreeMatch(original.getType(), other.getType())
 		&& matcher.safeSubtreeMatch(original.getDefault(), other.getDefault());
 		
 		return restMatches;
 	}
 
 	private static final boolean isSimilar(final EnumConstantDeclaration original, final ASTNode revised)
 	{
 		if (!(revised instanceof EnumConstantDeclaration))
 		{
 			return false;
 		}
 		
 		EnumConstantDeclaration other = (EnumConstantDeclaration) revised;
 
 		boolean nameMatches = matcher.safeSubtreeMatch(original.getName(), other.getName());
 		
 		if (nameMatches)
 		{
 			return true;
 		}
 		
 		boolean restMatches = matcher.safeSubtreeMatch(original.getJavadoc(), other.getJavadoc())
 		&& matcher.safeSubtreeListMatch(original.modifiers(), other.modifiers())
 		&& matcher.safeSubtreeListMatch(original.arguments(), other.arguments())
 		&& matcher.safeSubtreeMatch(original.getAnonymousClassDeclaration(), other.getAnonymousClassDeclaration());
 
 		return restMatches;
 	}
 	
 	private static final boolean isSimilar(final FieldDeclaration original, final ASTNode revised)
 	{
 		if (!(revised instanceof FieldDeclaration))
 		{
 			return false;
 		}
 		
 		FieldDeclaration other = (FieldDeclaration) revised;
 		
 		boolean fragmentMatches = false;
 		boolean restMatches     = true;
 
 		for (Object originalFragment: original.fragments())
 		{
 			if (!(originalFragment instanceof VariableDeclarationFragment))
 			{
 				continue;
 			}
 			
 			for (Object otherFragment: other.fragments())
 			{
 				if (!(otherFragment instanceof ASTNode))
 				{
 					continue;
 				}
 				
 				if (isSimilar((VariableDeclarationFragment) originalFragment, (ASTNode) otherFragment))
 				{
 					fragmentMatches = true;
 					break;
 				}
 			}
 		}
 		
 		if (fragmentMatches)
 		{
 			return true;
 		}
 		
 		switch (AstApiLevel.valueOf(original))
 		{
 			case JLS2:
 				throw new UnsupportedOperationException();
 			case JLS3:
 			case JLS4:
 				restMatches = restMatches
 				&& matcher.safeSubtreeListMatch(original.modifiers(), other.modifiers());
 				break;
 			default:
 				throw new AssertionError();
 		}
 		
 		restMatches = restMatches
 		&& matcher.safeSubtreeMatch(original.getJavadoc(), other.getJavadoc())
 		&& matcher.safeSubtreeMatch(original.getType(), other.getType());
 		
 		// TODO rm
 //		System.out.println("TYPE : "+original.getType());
 //		System.out.println("TYPE : "+other.getType());
 //		System.out.println("FIELD CMP name : "+(fragmentMatches));
 //		System.out.println("FIELD CMP rest : "+(restMatches));
 //		System.out.println("FIELD CMP : "+(fragmentMatches || restMatches));
 
 		return restMatches;
 	}
 
 	@SuppressWarnings("unused")
 	private static final boolean isSimilar(final Initializer original, final ASTNode revised)
 	{
 		if (!(revised instanceof Initializer))
 		{
 			return false;
 		}
 		
 		return true;
 	}
 
 	private static final boolean isSimilar(final MethodDeclaration original, final ASTNode revised)
 	{
 		if (!(revised instanceof MethodDeclaration))
 		{
 			return false;
 		}
 		
 		MethodDeclaration other = (MethodDeclaration) revised;
 		
 		boolean nameMatches = matcher.safeSubtreeMatch(original.getName(), other.getName());
 		
 		if (nameMatches)
 		{
 			return true;
 		}
 		
 		boolean restMatches = true;
 		
 		switch (AstApiLevel.valueOf(original))
 		{
 			case JLS2:
 				throw new UnsupportedOperationException();
 			case JLS3:
 			case JLS4:
 				restMatches = restMatches
 				&& matcher.safeSubtreeListMatch(original.modifiers(), other.modifiers())
 				&& matcher.safeSubtreeMatch(original.getReturnType2(), other.getReturnType2())
 				&& matcher.safeSubtreeListMatch(original.typeParameters(), other.typeParameters());
 				break;
 			default:
 				throw new AssertionError();
 		}
 		
 		restMatches = restMatches
 		&& original.isConstructor() == other.isConstructor()
 		&& matcher.safeSubtreeMatch(original.getJavadoc(), other.getJavadoc())
 		&& matcher.safeSubtreeListMatch(original.parameters(), other.parameters())
 		&& original.getExtraDimensions() == other.getExtraDimensions()
 		&& matcher.safeSubtreeListMatch(original.thrownExceptions(), other.thrownExceptions())
 		&& matcher.safeSubtreeMatch(original.getBody(), other.getBody());
 		
 		return restMatches;
 	}
 	
 	// body declaration helpers
 	
 	private static final boolean isSimilar(final VariableDeclarationFragment original, final ASTNode revised)
 	{
 		if (!(revised instanceof VariableDeclarationFragment))
 		{
 			return false;
 		}
 		
 		VariableDeclarationFragment other = (VariableDeclarationFragment) revised;
 
 		// TODO rm
 //		System.out.println("--------------------------------");
 //		System.out.println("ORIGINAL NAME: " +original.getName());
 //		System.out.println("ORIGINAL: " +original);
 //		System.out.println("OTHER NAME: " +other.getName());
 //		System.out.println("OTHER: " +other);
 //		System.out.println("--------------------------------");
 		
 		boolean nameMatches = matcher.safeSubtreeMatch(original.getName(), other.getName());
 		
 		if (nameMatches)
 		{
 			return true;
 		}
 		
 		boolean restMatches = original.getExtraDimensions() == other.getExtraDimensions()
 		&& matcher.safeSubtreeMatch(original.getInitializer(), other.getInitializer());
 
 		// TODO rm
 //		System.out.println("CMP name : "+(nameMatches));
 //		System.out.println("CMP rest : "+(restMatches));
 //		System.out.println("CMP : "+(nameMatches || restMatches));
 		
 		return restMatches;
 	}
 	
 	private static final boolean areSimilar(final List<ASTNode> original, final List<ASTNode> revised)
 	{
 		if (original.size() != revised.size())
 		{
 			return false;
 		}
 		
 		Iterator<ASTNode> revisedIterator = revised.iterator();
 		
 		for (ASTNode originalNode: original)
 		{
 			if (!isSimilar(originalNode, revisedIterator.next()))
 			{
 				return false;
 			}
 		}
 		
 		return true;
 	}
 }
