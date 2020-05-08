 package sk.stuba.fiit.perconik.core.java.dom;
 
 import java.nio.charset.Charset;
 import java.util.Arrays;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import javax.annotation.Nullable;
 import org.eclipse.jdt.core.ITypeRoot;
 import org.eclipse.jdt.core.JavaModelException;
 import org.eclipse.jdt.core.dom.AST;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.ASTParser;
 import org.eclipse.jdt.core.dom.ASTVisitor;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.StructuralPropertyDescriptor;
 import sk.stuba.fiit.perconik.eclipse.jdt.core.dom.AstApiLevel;
 import sk.stuba.fiit.perconik.eclipse.jdt.core.dom.AstNodeType;
 import com.google.common.collect.Lists;
 
 // TODO add more helpers from org.eclipse.jdt.internal.corext.dom.ASTNodes
 
 public final class AstNodes
 {
 	private AstNodes()
 	{
 		throw new AssertionError();
 	}
 	
 	public static final AST newTree()
 	{
 		return newTree(AstApiLevel.latest());
 	}
 
 	public static final AST newTree(final AstApiLevel level)
 	{
 		return AST.newAST(level.getValue());
 	}
 	
 	public static final <N extends ASTNode> N newNode(final AST tree, final Class<N> type)
 	{
 		return type.cast(tree.createInstance(type));
 	}
 
 	public static final ASTNode create(final ASTParser parser, final byte source[], final Charset charset)
 	{
 		return create(parser, new String(source, charset));
 	}
 	
 	public static final ASTNode create(final ASTParser parser, final char[] source)
 	{
 		parser.setSource(source);
 		
 		return parser.createAST(null);
 	}
 	
 	public static final ASTNode create(final ASTParser parser, final CharSequence source)
 	{
 		return create(parser, source.toString().toCharArray());
 	}
 
 	public static final <N extends ASTNode> N copyOf(final N node)
 	{
 		if (node == null)
 		{
 			return null;
 		}
 		
 		return copyOf(node, AstApiLevel.valueOf(node.getAST().apiLevel()));
 	}
 	
 	public static final <N extends ASTNode> N copyOf(final N node, final AstApiLevel level)
 	{
 		return copyOf(node, newTree(level));
 	}
 
 	public static final <N extends ASTNode> N copyOf(final N node, final AST tree)
 	{
 		return (N) ASTNode.copySubtree(tree, node);
 	}
 	
 	public static final ASTNode root(@Nullable final ASTNode node)
 	{
 		return node != null ? node.getRoot() : null;
 	}
 
 	public static final ASTNode parent(@Nullable final ASTNode node)
 	{
 		return node != null ? node.getParent() : null;
 	}
 	
 	public static final LinkedList<ASTNode> children(@Nullable final ASTNode node)
 	{
 		final LinkedList<ASTNode> children = Lists.newLinkedList();
 
 		ASTVisitor visitor = new ASTVisitor()
 		{
 			@Override
 			public final boolean preVisit2(final ASTNode child)
 			{
 				if (isChild(node, child))
 				{
 					children.add(child);
 				}
 				
 				return false;
 			}
 		};
 		
 		node.accept(visitor);
 		
 		return children;
 	}
 	
 	public static final LinkedList<ASTNode> ancestors(@Nullable ASTNode node)
 	{
 		final LinkedList<ASTNode> ancestors = Lists.newLinkedList();
 		
 		while (node != null)
 		{
 			ancestors.add(node = node.getParent());
 		}
 		
 		return ancestors;
 	}
 
 	public static final LinkedList<ASTNode> descendants(@Nullable ASTNode node)
 	{
 		final LinkedList<ASTNode> descendants = Lists.newLinkedList();
 
 		if (node == null)
 		{
 			return descendants;
 		}
 		
 		ASTVisitor visitor = new ASTVisitor()
 		{
 			@Override
 			public final void preVisit(final ASTNode descendant)
 			{
 				descendants.add(descendant);
 			}
 		};
 		
 		node.accept(visitor);
 		
 		return descendants;
 	}
 
 	public static final LinkedList<ASTNode> upToRoot(@Nullable ASTNode node)
 	{
 		LinkedList<ASTNode> branch = Lists.newLinkedList();
 		
 		if (node != null)
 		{
 			do
 			{
				branch.addFirst(node);
 			}
 			while ((node = node.getParent()) != null);
 		}
 		
 		return branch;
 	}
 	
 	public static final LinkedList<ASTNode> downToLeaves(@Nullable ASTNode node)
 	{
 		LinkedList<ASTNode> branch = descendants(node);
 		
 		if (node != null)
 		{
 			branch.addFirst(node);
 		}
 		
 		return branch;
 	}
 	
 	public static final <N extends ASTNode> N firstUpToRoot(@Nullable ASTNode node, Class<N> type)
 	{
 		if (node != null)
 		{
 			do
 			{
 				if (isInstance(node, type))
 				{
 					return type.cast(node);
 				}
 			}
 			while ((node = node.getParent()) != null);
 		}
 		
 		return null;
 	}
 	
 	public static final <N extends ASTNode> N firstDownToLeaves(@Nullable ASTNode node, Class<N> type)
 	{
 		for (ASTNode other: downToLeaves(node))
 		{
 			if (isInstance(other, type))
 			{
 				return type.cast(other);
 			}
 		}
 		
 		return null;
 	}
 
 	@SafeVarargs
 	public static final ASTNode firstAncestor(@Nullable ASTNode node, final Class<? extends ASTNode> ... types)
 	{
 		return firstAncestor(node, Arrays.asList(types));
 	}
 
 	public static final ASTNode firstAncestor(@Nullable ASTNode node, final Iterable<Class<? extends ASTNode>> types)
 	{
 		while (node != null)
 		{
 			if (isInstance(node = node.getParent(), types))
 			{
 				return node;
 			}
 		}
 	
 		return null;
 	}
 
 	@SafeVarargs
 	public static final ASTNode firstDescendant(@Nullable ASTNode node, final Class<? extends ASTNode> ... types)
 	{
 		return firstDescendant(node, Arrays.asList(types));
 	}
 
 	public static final ASTNode firstDescendant(@Nullable ASTNode node, final Iterable<Class<? extends ASTNode>> types)
 	{
 		final MutableReference<ASTNode> descendant = new MutableReference<>();
 		
 		ASTVisitor visitor = new ASTVisitor()
 		{
 			@Override
 			public final boolean preVisit2(final ASTNode other)
 			{
 				if (isInstance(other, types))
 				{
 					descendant.value = other;
 				}
 				
 				return descendant.value == null;
 			}
 		};
 		
 		node.accept(visitor);
 		
 		return descendant.value;
 	}
 
 	public static final Map<String, Object> genericProperties(final ASTNode node)
 	{
 		return node.properties();
 	}
 
 	public static final List<StructuralPropertyDescriptor> structuralProperties(final ASTNode node)
 	{
 		return node.structuralPropertiesForType();
 	}
 
 	public static final String source(final ASTNode node, final AstRangeType range)
 	{
 		ASTNode root = node.getRoot();
 	
 		if (root instanceof CompilationUnit)
 		{
 			CompilationUnit unit = (CompilationUnit) root;
 			ITypeRoot       type = unit.getTypeRoot();
 
 			try
 			{
 				if (type != null && type.getBuffer() != null)
 				{
 					int offset = range.getOffset(unit, node);
 					int length = range.getLength(unit, node);
 					
 					return type.getBuffer().getText(offset, length);
 				}
 			}
 			catch (JavaModelException e)
 			{
 			}
 		}
 	
 		return null;
 	}
 	
 	public final static boolean hasSource(final ASTNode node)
 	{
 		return node.getStartPosition() != -1;
 	}
 
 	public static final boolean isRoot(@Nullable ASTNode node)
 	{
 		return node != null && node == node.getRoot();
 	}
 	
 	public static final boolean isParent(@Nullable ASTNode node, @Nullable final ASTNode parent)
 	{
 		return node != null && parent == node.getParent();
 	}
 	
 	public static final boolean isChild(@Nullable ASTNode node, @Nullable final ASTNode child)
 	{
 		return child != null && child.getParent() == node;
 	}
 	
 	public static final boolean isAncestor(@Nullable ASTNode node, @Nullable final ASTNode ancestor)
 	{
 		while (node != null)
 		{
 			if (ancestor == (node = node.getParent()))
 			{
 				return true;
 			}
 		}
 		
 		return false;
 	}
 	
 	public static final boolean isDescendant(@Nullable ASTNode node, @Nullable final ASTNode descendant)
 	{
 		if (node == null)
 		{
 			return false;
 		}
 		
 		final MutableBoolean visit = new MutableBoolean(true);
 		
 		ASTVisitor visitor = new ASTVisitor()
 		{
 			@Override
 			public final boolean preVisit2(final ASTNode other)
 			{
 				if (other == descendant)
 				{
 					visit.value = false;
 				}
 				
 				return visit.value;
 			}
 		};
 		
 		node.accept(visitor);
 		
 		return !visit.value;
 	}
 
 	public static final boolean isRecoveredOrMalformed(final ASTNode node)
 	{
 		return (node.getFlags() & ASTNode.RECOVERED) != 0 || (node.getFlags() & ASTNode.MALFORMED) != 0;
 	}
 	
 	public static final boolean isProblematicTree(final ASTNode node)
 	{
 		if (isRecoveredOrMalformed(node))
 		{
 			return true;
 		}
 		
 		for (ASTNode descendant: descendants(node))
 		{
 			if (isRecoveredOrMalformed(descendant))
 			{
 				return true;
 			}
 		}
 	
 		return false;
 	}
 
 	static class MutableReference<T>
 	{
 		T value;
 		
 		MutableReference()
 		{
 		}
 		
 		MutableReference(T value)
 		{
 			this.value = value;
 		}
 	}
 	
 	static class MutableBoolean
 	{
 		boolean value;
 		
 		MutableBoolean(boolean value)
 		{
 			this.value = value;
 		}
 	}
 
 	public static final boolean isInstance(@Nullable final ASTNode node, final Class<? extends ASTNode> type)
 	{
 		return node != null && type.isInstance(node);
 	}
 
 	public static final boolean isInstance(@Nullable final ASTNode node, final Class<? extends ASTNode> a, final Class<? extends ASTNode> b)
 	{
 		return node != null && (a.isInstance(node) || b.isInstance(node));
 	}
 
 	public static final boolean isInstance(@Nullable final ASTNode node, final Class<? extends ASTNode> a, final Class<? extends ASTNode> b, final Class<? extends ASTNode> c)
 	{
 		return node != null && (a.isInstance(node) || b.isInstance(node) || c.isInstance(node));
 	}
 
 	public static final boolean isInstance(@Nullable final ASTNode node, final Class<? extends ASTNode> a, final Class<? extends ASTNode> b, final Class<? extends ASTNode> c, final Class<? extends ASTNode> d)
 	{
 		return node != null && (a.isInstance(node) || b.isInstance(node) || c.isInstance(node) || d.isInstance(node));
 	}
 
 	@SafeVarargs
 	public static final boolean isInstance(@Nullable final ASTNode node, final Class<? extends ASTNode> a, final Class<? extends ASTNode> b, final Class<? extends ASTNode> c, final Class<? extends ASTNode> d, final Class<? extends ASTNode> ... rest)
 	{
 		return isInstance(node, a, b, c, d) || isInstance(node, Arrays.asList(rest));
 	}
 
 	public static final boolean isInstance(@Nullable final ASTNode node, final Iterable<Class<? extends ASTNode>> types)
 	{
 		if (node == null)
 		{
 			return false;
 		}
 		
 		for (Class<?> type: types)
 		{
 			if (type.isInstance(node))
 			{
 				return true;
 			}
 		}
 		
 		return false;
 	}
 
 	public static final Class<? extends ASTNode> typeAsClass(@Nullable final ASTNode node)
 	{
 		return node != null ? node.getClass() : null;
 	}
 
 	public static final AstNodeType typeAsConstant(@Nullable final ASTNode node)
 	{
 		return node != null ? AstNodeType.valueOf(node) : null;
 	}
 
 	public static final String typeAsString(@Nullable final ASTNode node)
 	{
 		return node != null ? node.getClass().getSimpleName() : null;
 	}
 }
