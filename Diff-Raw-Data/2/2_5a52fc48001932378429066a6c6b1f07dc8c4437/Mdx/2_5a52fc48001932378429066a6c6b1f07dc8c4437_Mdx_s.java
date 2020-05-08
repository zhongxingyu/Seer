 package es.cgalesanco.olap4j.query.mdx;
 
 import java.math.BigDecimal;
 import java.util.List;
 
 import org.olap4j.mdx.CallNode;
 import org.olap4j.mdx.LevelNode;
 import org.olap4j.mdx.LiteralNode;
 import org.olap4j.mdx.MemberNode;
 import org.olap4j.mdx.ParseTreeNode;
 import org.olap4j.mdx.Syntax;
 import org.olap4j.metadata.Level;
 import org.olap4j.metadata.Member;
 
 import es.cgalesanco.olap4j.query.SortOrder;
 
 /**
  * Utility class to generate MDX tree expressions.
  * 
  * @author César García
  * 
  */
 public class Mdx {
 	public static MemberNode member(Member m) {
 		return new MemberNode(null, m);
 	}
 
 	public static CallNode children(Member member) {
 		return new CallNode(null, "Children", Syntax.Property, member(member));
 	}
 
 	public static ParseTreeNode except(ParseTreeNode from, ParseTreeNode except) {
 		if ( except == null )
 			return from;
 
 		ParseTreeNode exceptSet = Mdx.set(except);
 		return new CallNode(null, "Except", Syntax.Function, from, exceptSet);
 	}
 	public static ParseTreeNode drillDown(UnionBuilder inclusions,
 			UnionBuilder drillList) {
 		if (drillList.isEmpty())
 			return inclusions.getUnionNode();
 		return new CallNode(null, "DrilldownMember", Syntax.Function,
 				Mdx.set(inclusions.getUnionNode()), 
 				Mdx.set(drillList.getUnionNode()),
 				LiteralNode.createSymbol(null, "RECURSIVE"));
 	}
 
 	public static ParseTreeNode set(ParseTreeNode unionNode) {
 		if ( unionNode == null )
 			return null;
 		if ( unionNode instanceof MemberNode ) {
 			return new CallNode(null, "{}", Syntax.Braces, unionNode);
 		} else
 			return unionNode;
 	}
 
 	public static ParseTreeNode descendants(ParseTreeNode from, int level) {
 		if ( from == null )
 			return null;
 		if ( level == 0 )
 			return from;
 		return new CallNode(null, "Descendants", Syntax.Function,
 				from, LiteralNode.createNumeric(null,
 						new BigDecimal(level), false));
 	}
 
 	public static ParseTreeNode descendants(ParseTreeNode from, int level,
 			String flag) {
 		if ( from == null )
 			return null;
 		
 		if ( flag == null )
 			return descendants(from, level);
 		else
 			return new CallNode(null, "Descendants",
 					Syntax.Function,
 					from, 
 					LiteralNode.createNumeric(null, new BigDecimal(level), false),
 					LiteralNode.createSymbol(null, flag));
 	}
 
 	public static ParseTreeNode order(ParseTreeNode axisExpression,
 			List<Member> sortPosition, SortOrder sortOrder) {
 		MemberNode[] members = new MemberNode[sortPosition.size()];
 		int i = 0;
 		for(Member m : sortPosition) {
 			members[i++] = new MemberNode(null,m);
 		}
 		return new CallNode(
 				null, 
 				"Order", 
 				Syntax.Function,
 				axisExpression,
 				new CallNode(null, "()", Syntax.Parentheses, members),
 				LiteralNode.createSymbol(null, sortOrder.toString())
 			);
 	}
 
 	public static ParseTreeNode hierarchize(ParseTreeNode n) {
 		if ( n == null )
 			return null;
 		return new CallNode(null, "Hierarchize", Syntax.Function, n);
 	}
 
 	public static ParseTreeNode allMembers(Level level) {
		return new CallNode(null, "Members", Syntax.Property, new LevelNode(null, level));
 	}
 }
