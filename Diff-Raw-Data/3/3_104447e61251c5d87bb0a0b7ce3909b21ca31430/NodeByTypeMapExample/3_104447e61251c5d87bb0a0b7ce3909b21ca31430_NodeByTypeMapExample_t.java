 package sk.stuba.fiit.perconik.core.java.examples;
 
 import static java.lang.System.out;
 import java.nio.file.Paths;
 import java.util.List;
 import java.util.Set;
 import org.eclipse.jdt.core.dom.ASTNode;
 import org.eclipse.jdt.core.dom.CompilationUnit;
 import org.eclipse.jdt.core.dom.MethodDeclaration;
 import sk.stuba.fiit.perconik.core.java.dom.NodeCollectors;
 import sk.stuba.fiit.perconik.core.java.dom.NodeTypeDivider;
 import sk.stuba.fiit.perconik.core.java.dom.TreeParsers;
 import sk.stuba.fiit.perconik.eclipse.jdt.core.dom.NodeType;
 import sk.stuba.fiit.perconik.utilities.MoreStrings;
 import com.google.common.collect.Multimap;
 import com.google.common.collect.Sets;
 
 public class NodeByTypeMapExample
 {
 	public static void main(String[] args) throws Exception
 	{
 		CompilationUnit unit = (CompilationUnit) TreeParsers.parse(Paths.get("fixtures/HashMap.java"));
 		
 		NodeTypeDivider<ASTNode> d = NodeTypeDivider.create();
 		
 		Multimap<NodeType, ASTNode> m = d.apply(unit);
 		
 		Set<NodeType> k = Sets.newTreeSet(MoreStrings.toStringComparator());
 		
 		k.addAll(m.keySet());
 		
 		out.println(m.size() + " nodes");
		out.println(k.size() + " of " + NodeType.count() + " types");
		out.println();
 		
 		for (NodeType t: k)
 		{
 			out.printf("%-40s %4d%n", t, m.get(t).size());
 		}
 		
 		List<MethodDeclaration> methods = NodeCollectors.ofClass(MethodDeclaration.class).apply(unit);
 
 		out.println("%n" + methods.size() + " methods %n");
 
 		MethodDeclaration method = methods.get(10);
 		
 		out.println(method);
 		
 		m = d.apply(method);
 		
 		k = Sets.newTreeSet(MoreStrings.toStringComparator());
 		
 		k.addAll(m.keySet());
 		
 		out.println(m.size() + " nodes");
 		out.println(k.size() + " of " + NodeType.count() + " types%n");
 		
 		for (NodeType t: k)
 		{
 			out.printf("%-40s %4d -> %s%n", t, m.get(t).size(), m.get(t).toString().replaceAll("\r?%n|\r", ""));
 		}
 	}
 }
