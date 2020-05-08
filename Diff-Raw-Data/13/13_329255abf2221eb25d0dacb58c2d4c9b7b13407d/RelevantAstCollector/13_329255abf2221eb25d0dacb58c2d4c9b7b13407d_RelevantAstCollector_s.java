 package com.gratex.perconik.activity.ide.listeners;
 
 import java.util.Collection;
 import java.util.List;
 import java.util.Set;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.AnnotationTypeDeclaration;
 import org.eclipse.jdt.core.dom.AnnotationTypeMemberDeclaration;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.EnumConstantDeclaration;
 import org.eclipse.jdt.core.dom.EnumDeclaration;
 import org.eclipse.jdt.core.dom.FieldDeclaration;
 import org.eclipse.jdt.core.dom.ImportDeclaration;
 import org.eclipse.jdt.core.dom.Initializer;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import org.eclipse.jdt.core.dom.PackageDeclaration;
 import org.eclipse.jdt.core.dom.TypeDeclaration;
 import sk.stuba.fiit.perconik.utilities.function.Collector;
 import com.google.common.annotations.Beta;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 
 @Beta
 final class RelevantAstCollector implements Collector<CompilationUnit, ASTNode>
 {
 	public RelevantAstCollector()
 	{
 	}
 	
	public final List<ASTNode> apply(final CompilationUnit unit)
 	{
 		return new Processor().apply(unit);
 	}
 
 	private static final class Processor extends ASTVisitor implements Collector<CompilationUnit, ASTNode>
 	{
 		private final Set<ASTNode> result;
 		
 		Processor()
 		{
 			this.result = Sets.newLinkedHashSet();
 		}
 		
		public final List<ASTNode> apply(final CompilationUnit unit)
 		{
 			unit.accept(this);
 
 			return Lists.newArrayList(this.result);
 		}
 		
 		private final void addNode(final ASTNode node)
 		{
 			if (node != null)
 			{
 				this.result.add(node);
 			}
 		}
 		
 		private final void addNodes(final Collection<? extends ASTNode> nodes)
 		{
 			this.result.addAll(nodes);
 		}
 
 		// compilation unit
 		
 		@Override
 		public final boolean visit(CompilationUnit node)
 		{
 			return true;
 		}
 
 		@Override
 		public final boolean visit(PackageDeclaration node)
 		{
 			this.addNode(node);
 			
 			return true;
 		}
 	
 		@Override
 		public final boolean visit(ImportDeclaration node)
 		{
 			this.addNode(node);
 			
 			return true;
 		}
 	
 		// abstract type declarations
 		
 		@Override
 		public final boolean visit(AnnotationTypeDeclaration node)
 		{
 			this.addNode(node.getJavadoc());
 			this.addNodes(node.modifiers());
 			this.addNode(node.getName());
 			
 			return true;
 		}
 	
 		@Override
 		public final boolean visit(EnumDeclaration node)
 		{
 			this.addNode(node.getJavadoc());
 			this.addNodes(node.modifiers());
 			this.addNode(node.getName());
 			this.addNodes(node.superInterfaceTypes());
 
 			return true;
 		}
 	
 		@Override
 		public final boolean visit(TypeDeclaration node)
 		{
 			this.addNode(node.getJavadoc());
 			this.addNodes(node.modifiers());
 			this.addNode(node.getName());
 			this.addNodes(node.typeParameters());
 			this.addNode(node.getSuperclassType());
 			this.addNodes(node.superInterfaceTypes());
 			
 			return true;
 		}
 	
 		// abstract type body declarations
 		
 		@Override
 		public final boolean visit(AnnotationTypeMemberDeclaration node)
 		{
 			this.addNode(node);
 			
 			return true;
 		}
 	
 		@Override
 		public final boolean visit(EnumConstantDeclaration node)
 		{
 			this.addNode(node);
 
 			return true;
 		}
 	
 		@Override
 		public final boolean visit(FieldDeclaration node)
 		{
 			this.addNode(node);
 
 			return true;
 		}
 	
 		@Override
 		public final boolean visit(Initializer node)
 		{
 			this.addNode(node);
 
 			return true;
 		}
 	
 		@Override
 		public final boolean visit(MethodDeclaration node)
 		{
 			this.addNode(node);
 
 			return true;
 		}
 	}
 }
