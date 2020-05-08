 package es.cgalesanco.olap4j.query;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.StringWriter;
 import java.util.ArrayList;
 import java.util.Arrays;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.olap4j.mdx.ParseTreeNode;
 import org.olap4j.mdx.ParseTreeWriter;
 import org.olap4j.metadata.Cube;
 import org.olap4j.metadata.Member;
 
 import es.cgalesanco.olap4j.query.Selection.Operator;
 import es.cgalesanco.olap4j.query.Selection.Sign;
 
 public class ExpandedQueryHierarchyMethodicTest {
 	static private Cube cube;
 	private static QueryHierarchy qh;
 	private Member rootMember;
 	private Member childMember;
 	private HierarchyExpander expander;
 	private Member grandsonMember;
 
 	public enum State {
 		Ed_Ech_Em(Sign.EXCLUDE), Ed_Ech_Im(Sign.EXCLUDE, Sign.EXCLUDE,
 				Sign.INCLUDE), Ed_Ich_Em(Sign.EXCLUDE, Sign.INCLUDE), Ed_Ich_Im(
 				Sign.EXCLUDE, Sign.INCLUDE, Sign.INCLUDE), Id_Ech_Em(
 				Sign.INCLUDE, Sign.EXCLUDE, Sign.EXCLUDE), Id_Ech_Im(
 				Sign.INCLUDE, Sign.EXCLUDE), Id_Ich_Em(Sign.INCLUDE,
 				Sign.INCLUDE, Sign.EXCLUDE), Id_Ich_Im(Sign.INCLUDE);
 
 		private Sign[] signs;
 
 		State(Sign... signs) {
 			this.signs = signs;
 		}
 
 		void apply(Member m) {
 			qh.apply(new SelectionAction(m, signs[0], Operator.DESCENDANTS));
 
 			if (signs.length > 1)
 				qh.apply(new SelectionAction(m, signs[1], Operator.CHILDREN));
 
 			if (signs.length > 2)
 				qh.apply(new SelectionAction(m, signs[2], Operator.MEMBER));
 		}
 	}
 
 	@BeforeClass
 	static public void setUpFixture() throws Exception {
 		cube = MetadataFixture.createCube();
 	}
 
 	@Before
 	public void setUp() throws Exception {
 		Query q = new Query("Hierarchy Test", cube);
 		qh = q.getHierarchy("Time");
 		rootMember = qh.getHierarchy().getRootMembers().get(0);
 		childMember = rootMember.getChildMembers().get(0);
 		grandsonMember = childMember.getChildMembers().get(1);
 		expander = new HierarchyExpander();
 		expander.expandHierarchy();
 	}
 
 	@Test
 	public void testInitial_noDrill() {
 		String[] notDrilled = new String[] { 
 				"{}", 
 				"{%1$s}", 
 				"%1$s.Children",
 				"DrilldownMember({%1$s}, {%1$s}, RECURSIVE)", 
 				"Descendants(%1$s, 2, SELF_AND_AFTER)", 
 				"Union({%1$s}, Descendants(%1$s, 2, SELF_AND_AFTER))",
 				"Descendants(%1$s, 1, SELF_AND_AFTER)",
 				"Union({%1$s}, Descendants(%1$s, 1, SELF_AND_AFTER))"};
 
 		int i = 0;
 		for (State st : State.values()) {
 			System.out.println(st);
 			qh.clear();
 			st.apply(rootMember);
 			assertDrillExpression(notDrilled[i++], qh.toOlap4j(expander, new ArrayList<Member>()), rootMember);
 		}
 	}
 
 	@Test
 	public void testInitial_undrillRoot() {
 		String[] notDrilled = new String[] { 
 				"{}", 
 				"{%1$s}", 
 				"%1$s.Children",
 				"{%1$s}",
 				"Descendants(%1$s, 2, SELF_AND_AFTER)", 
 				"{%1$s}",
 				"Descendants(%1$s, 1, SELF_AND_AFTER)", 
 				"{%1$s}" };
 
 		int i = 0;
 		for (State st : State.values()) {
 			System.out.println(st);
 			qh.clear();
 			st.apply(rootMember);
 			assertDrillExpression(notDrilled[i++],
 					qh.toOlap4j(expander, Arrays.asList(rootMember)), rootMember);
 		}
 	}
 
 	@Test
 	public void testInitial_undrillChild() {
 		String[] notDrilled = new String[] { 
 				"{}", 
 				"{%1$s}", 
 				"%1$s.Children",
 				"DrilldownMember({%1$s}, {%1$s}, RECURSIVE)",
 				"Descendants(%1$s, 2, SELF_AND_AFTER)", 
 				"Union({%1$s}, Descendants(%1$s, 2, SELF_AND_AFTER))",
 				"Except(Descendants(%1$s, 1, SELF_AND_AFTER), Descendants({%2$s}, 0, AFTER))", 
 				"Except(Union({%1$s}, Descendants(%1$s, 1, SELF_AND_AFTER)), Descendants({%2$s}, 0, AFTER))" };
 
 		int i = 0;
 		for (State st : State.values()) {
 			System.out.println(st);
 			qh.clear();
 			st.apply(rootMember);
 			assertDrillExpression(notDrilled[i++],
 					qh.toOlap4j(expander, Arrays.asList(childMember)), rootMember, childMember);
 		}
 	}
 
 	@Test
 	public void testInitial_undrillGrandson() {
 		String[] notDrilled = new String[] { 
 				"{}", 
 				"{%1$s}", 
 				"%1$s.Children",
 				"DrilldownMember({%1$s}, {%1$s}, RECURSIVE)",
				"Except(Descendants(%1$s, 2, SELF_AND_AFTER), Descendants({%3$s}, 0, AFTER))", 
				"Except(Union({%1$s}, Descendants(%1$s, 2, SELF_AND_AFTER)), Descendants({%3$s}, 0, AFTER))",
 				"Except(Descendants(%1$s, 1, SELF_AND_AFTER), Descendants({%3$s}, 0, AFTER))", 
 				"Except(Union({%1$s}, Descendants(%1$s, 1, SELF_AND_AFTER)), Descendants({%3$s}, 0, AFTER))" };
 
 		int i = 0;
 		for (State st : State.values()) {
 			System.out.println(st);
 			qh.clear();
 			st.apply(rootMember);
 			assertDrillExpression(notDrilled[i++],
 					qh.toOlap4j(expander, Arrays.asList(grandsonMember)), rootMember, childMember, grandsonMember);
 		}
 	}
 	
 	@Test
 	public void testTwoLevels_noDrill() {
 		String[] notDrilled = new String[] {
 				// Root: Ed_Ech_Em
 				"{}", 
 				"{%2$s}", 
 				"%2$s.Children", 
 				"DrilldownMember({%2$s}, {%2$s}, RECURSIVE)",
 				"Descendants(%2$s, 2, SELF_AND_AFTER)", 
 				"Union({%2$s}, Descendants(%2$s, 2, SELF_AND_AFTER))", 
 				"Descendants(%2$s, 1, SELF_AND_AFTER)", 
 				"Union({%2$s}, Descendants(%2$s, 1, SELF_AND_AFTER))",
 
 				// Root: Ed_Ech_Im
 				"{%1$s}", 
 				"{%1$s, %2$s}", 
 				"Union({%1$s}, %2$s.Children)", 
 				"DrilldownMember({%1$s, %2$s}, {%2$s}, RECURSIVE)", 
 				"Union({%1$s}, Descendants(%2$s, 2, SELF_AND_AFTER))", 
 				"Union({%1$s, %2$s}, Descendants(%2$s, 2, SELF_AND_AFTER))",
 				"Union({%1$s}, Descendants(%2$s, 1, SELF_AND_AFTER))", 
 				"Union({%1$s, %2$s}, Descendants(%2$s, 1, SELF_AND_AFTER))",
 				
 				// Root: Ed_Ich_Em
 				"Except(%1$s.Children, {%2$s})",
 				"%1$s.Children",
 				"Except(Union(%2$s.Children, %1$s.Children), {%2$s})",
 				"DrilldownMember(%1$s.Children, {%2$s}, RECURSIVE)",
 				"Except(Union(Descendants(%2$s, 2, SELF_AND_AFTER), %1$s.Children), {%2$s})",
 				"Union(Descendants(%2$s, 2, SELF_AND_AFTER), %1$s.Children)",
 				"Except(Union(Descendants(%2$s, 1, SELF_AND_AFTER), %1$s.Children), {%2$s})",
 				"Union(Descendants(%2$s, 1, SELF_AND_AFTER), %1$s.Children)",
 				
 				// Root: Ed_Ich_Im
 				"Except(DrilldownMember({%1$s}, {%1$s}, RECURSIVE), {%2$s})", 
 				"DrilldownMember({%1$s}, {%1$s}, RECURSIVE)", 
 				"Except(DrilldownMember(Union({%1$s}, %2$s.Children), {%1$s}, RECURSIVE), {%2$s})", 
 				"DrilldownMember({%1$s}, {%2$s, %1$s}, RECURSIVE)",
 				"Except(DrilldownMember(Union({%1$s}, Descendants(%2$s, 2, SELF_AND_AFTER)), {%1$s}, RECURSIVE), {%2$s})", 
 				"DrilldownMember(Union({%1$s}, Descendants(%2$s, 2, SELF_AND_AFTER)), {%1$s}, RECURSIVE)", 
 				"Except(DrilldownMember(Union({%1$s}, Descendants(%2$s, 1, SELF_AND_AFTER)), {%1$s}, RECURSIVE), {%2$s})", 
 				"DrilldownMember(Union({%1$s}, Descendants(%2$s, 1, SELF_AND_AFTER)), {%1$s}, RECURSIVE)",
 
 				// Root: Id_Ech_Em
 				"Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER)",
 				"Union({%2$s}, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))",
 				"Union(%2$s.Children, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))", 
 				"DrilldownMember(Union({%2$s}, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER)), {%2$s}, RECURSIVE)",
 				"Union(Descendants(%2$s, 2, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))",
 				"Union({%2$s}, Union(Descendants(%2$s, 2, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER)))",
 				"Union(Descendants(%2$s, 1, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))", 
 				"Union({%2$s}, Union(Descendants(%2$s, 1, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER)))",
 				
 				// Root: Id_Ech_Im
 				"Union({%1$s}, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))", 
 				"Union({%1$s, %2$s}, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))", 
 				"Union({%1$s}, Union(%2$s.Children, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER)))", 
 				"DrilldownMember(Union({%1$s, %2$s}, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER)), {%2$s}, RECURSIVE)", 
 				"Union({%1$s}, Union(Descendants(%2$s, 2, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER)))", 
 				"Union({%1$s, %2$s}, Union(Descendants(%2$s, 2, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER)))", 
 				"Union({%1$s}, Union(Descendants(%2$s, 1, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER)))", 
 				"Union({%1$s, %2$s}, Union(Descendants(%2$s, 1, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER)))",
 
 				// Root: Id_Ich_Em
 				"Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER)", 
 				"Union({%2$s}, Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER))",
 				"Union(%2$s.Children, Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER))",
 				"DrilldownMember(Union({%2$s}, Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER)), {%2$s}, RECURSIVE)",
 				"Union(Descendants(%2$s, 2, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER))",
 				"Union({%2$s}, Union(Descendants(%2$s, 2, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER)))",
 				"Union(Descendants(%2$s, 1, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER))",
 				"Descendants(%1$s, 1, SELF_AND_AFTER)",
 				
 				// Root: Id_Ich_Im
 				"Union({%1$s}, Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER))",  
 				"Union({%1$s, %2$s}, Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER))",
 				"Union({%1$s}, Union(%2$s.Children, Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER)))", 
 				"DrilldownMember(Union({%1$s, %2$s}, Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER)), {%2$s}, RECURSIVE)", 
 				"Union({%1$s}, Union(Descendants(%2$s, 2, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER)))", 
 				"Union({%1$s, %2$s}, Union(Descendants(%2$s, 2, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER)))", 
 				"Union({%1$s}, Union(Descendants(%2$s, 1, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER)))", 
 				"Union({%1$s}, Descendants(%1$s, 1, SELF_AND_AFTER))",
 		};
 
 		int i = 0;
 		for (State st1 : State.values()) {
 			for (State st2 : State.values()) {
 				qh.clear();
 				st1.apply(rootMember);
 				st2.apply(childMember);
 				if (i < notDrilled.length) {
 					System.out.println(st1 + "/" + st2);
 					assertDrillExpression(notDrilled[i++], qh.toOlap4j(expander, new ArrayList<Member>()),
 							rootMember, childMember);
 				}
 			}
 		}
 	}
 
 	@Test
 	public void testTwoLevels_undrillRoot() {
 		String[] notDrilled = new String[] {
 				// Root: Ed_Ech_Em
 				"{}", 
 				"{%2$s}", 
 				"%2$s.Children", 
 				"DrilldownMember({%2$s}, {%2$s}, RECURSIVE)",
 				"Descendants(%2$s, 2, SELF_AND_AFTER)", 
 				"Union({%2$s}, Descendants(%2$s, 2, SELF_AND_AFTER))", 
 				"Descendants(%2$s, 1, SELF_AND_AFTER)", 
 				"Union({%2$s}, Descendants(%2$s, 1, SELF_AND_AFTER))",
 
 				// Root: Ed_Ech_Im
 				"{%1$s}", "{%1$s}", "{%1$s}", "{%1$s}", "{%1$s}", "{%1$s}", "{%1$s}", "{%1$s}",
 				
 				// Root: Ed_Ich_Em
 				"Except(%1$s.Children, {%2$s})",
 				"%1$s.Children",
 				"Except(Union(%2$s.Children, %1$s.Children), {%2$s})",
 				"DrilldownMember(%1$s.Children, {%2$s}, RECURSIVE)",
 				"Except(Union(Descendants(%2$s, 2, SELF_AND_AFTER), %1$s.Children), {%2$s})",
 				"Union(Descendants(%2$s, 2, SELF_AND_AFTER), %1$s.Children)",
 				"Except(Union(Descendants(%2$s, 1, SELF_AND_AFTER), %1$s.Children), {%2$s})",
 				"Union(Descendants(%2$s, 1, SELF_AND_AFTER), %1$s.Children)",
 				
 				// Root: Ed_Ich_Im
 				"{%1$s}", "{%1$s}", "{%1$s}", "{%1$s}", "{%1$s}", "{%1$s}", "{%1$s}", "{%1$s}",
 
 				// Root: Id_Ech_Em
 				"Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER)",
 				"Union({%2$s}, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))",
 				"Union(%2$s.Children, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))", 
 				"DrilldownMember(Union({%2$s}, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER)), {%2$s}, RECURSIVE)",
 				"Union(Descendants(%2$s, 2, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))",
 				"Union({%2$s}, Union(Descendants(%2$s, 2, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER)))",
 				"Union(Descendants(%2$s, 1, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))", 
 				"Union({%2$s}, Union(Descendants(%2$s, 1, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER)))",
 				
 				// Root: Id_Ech_Im
 				"{%1$s}", "{%1$s}", "{%1$s}", "{%1$s}", "{%1$s}", "{%1$s}", "{%1$s}", "{%1$s}",
 
 				// Root: Id_Ich_Em
 				"Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER)", 
 				"Union({%2$s}, Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER))",
 				"Union(%2$s.Children, Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER))",
 				"DrilldownMember(Union({%2$s}, Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER)), {%2$s}, RECURSIVE)",
 				"Union(Descendants(%2$s, 2, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER))",
 				"Union({%2$s}, Union(Descendants(%2$s, 2, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER)))",
 				"Union(Descendants(%2$s, 1, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER))",
 				"Descendants(%1$s, 1, SELF_AND_AFTER)",
 				
 				// Root: Id_Ich_Im
 				"{%1$s}", "{%1$s}", "{%1$s}", "{%1$s}", "{%1$s}", "{%1$s}", "{%1$s}", "{%1$s}",
 		};
 
 		int i = 0;
 		for (State st1 : State.values()) {
 			for (State st2 : State.values()) {
 				qh.clear();
 				st1.apply(rootMember);
 				st2.apply(childMember);
 				if (i < notDrilled.length) {
 					System.out.println(st1 + "/" + st2);
 					assertDrillExpression(notDrilled[i++], qh.toOlap4j(expander, Arrays.asList(rootMember)),
 							rootMember, childMember);
 				}
 			}
 		}
 	}
 
 	@Test
 	public void testTwoLevels_undrillChild() {
 		String[] notDrilled = new String[] {
 				// Root: Ed_Ech_Em
 				"{}", 
 				"{%2$s}", 
 				"%2$s.Children", 
 				"{%2$s}",
 				"Descendants(%2$s, 2, SELF_AND_AFTER)", 
 				"{%2$s}", 
 				"Descendants(%2$s, 1, SELF_AND_AFTER)", 
 				"{%2$s}",
 
 				// Root: Ed_Ech_Im
 				"{%1$s}", 
 				"{%1$s, %2$s}", 
 				"Union({%1$s}, %2$s.Children)", 
 				"{%1$s, %2$s}", 
 				"Union({%1$s}, Descendants(%2$s, 2, SELF_AND_AFTER))", 
 				"{%1$s, %2$s}",
 				"Union({%1$s}, Descendants(%2$s, 1, SELF_AND_AFTER))", 
 				"{%1$s, %2$s}",
 				
 				// Root: Ed_Ich_Em
 				"Except(%1$s.Children, {%2$s})",
 				"%1$s.Children",
 				"Except(Union(%2$s.Children, %1$s.Children), {%2$s})",
 				"%1$s.Children",
 				"Except(Union(Descendants(%2$s, 2, SELF_AND_AFTER), %1$s.Children), {%2$s})",
 				"%1$s.Children",
 				"Except(Union(Descendants(%2$s, 1, SELF_AND_AFTER), %1$s.Children), {%2$s})",
 				"%1$s.Children",
 				
 				// Root: Ed_Ich_Im
 				"Except(DrilldownMember({%1$s}, {%1$s}, RECURSIVE), {%2$s})", 
 				"DrilldownMember({%1$s}, {%1$s}, RECURSIVE)", 
 				"Except(DrilldownMember(Union({%1$s}, %2$s.Children), {%1$s}, RECURSIVE), {%2$s})", 
 				"DrilldownMember({%1$s}, {%1$s}, RECURSIVE)",
 				"Except(DrilldownMember(Union({%1$s}, Descendants(%2$s, 2, SELF_AND_AFTER)), {%1$s}, RECURSIVE), {%2$s})", 
 				"DrilldownMember({%1$s}, {%1$s}, RECURSIVE)", 
 				"Except(DrilldownMember(Union({%1$s}, Descendants(%2$s, 1, SELF_AND_AFTER)), {%1$s}, RECURSIVE), {%2$s})", 
 				"DrilldownMember({%1$s}, {%1$s}, RECURSIVE)",
 
 				// Root: Id_Ech_Em
 				"Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER)",
 				"Union({%2$s}, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))",
 				"Union(%2$s.Children, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))", 
 				"Union({%2$s}, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))",
 				"Union(Descendants(%2$s, 2, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))",
 				"Union({%2$s}, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))",
 				"Union(Descendants(%2$s, 1, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))", 
 				"Union({%2$s}, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))",
 				
 				// Root: Id_Ech_Im
 				"Union({%1$s}, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))", 
 				"Union({%1$s, %2$s}, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))", 
 				"Union({%1$s}, Union(%2$s.Children, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER)))", 
 				"Union({%1$s, %2$s}, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))", 
 				"Union({%1$s}, Union(Descendants(%2$s, 2, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER)))", 
 				"Union({%1$s, %2$s}, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))", 
 				"Union({%1$s}, Union(Descendants(%2$s, 1, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER)))", 
 				"Union({%1$s, %2$s}, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))",
 
 				// Root: Id_Ich_Em
 				"Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER)", 
 				"Union({%2$s}, Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER))",
 				"Union(%2$s.Children, Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER))",
 				"Union({%2$s}, Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER))",
 				"Union(Descendants(%2$s, 2, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER))",
 				"Union({%2$s}, Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER))",
 				"Union(Descendants(%2$s, 1, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER))",
 				"Except(Descendants(%1$s, 1, SELF_AND_AFTER), Descendants({%2$s}, 0, AFTER))",
 				
 				// Root: Id_Ich_Im
 				"Union({%1$s}, Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER))",  
 				"Union({%1$s, %2$s}, Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER))",
 				"Union({%1$s}, Union(%2$s.Children, Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER)))", 
 				"Union({%1$s, %2$s}, Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER))", 
 				"Union({%1$s}, Union(Descendants(%2$s, 2, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER)))", 
 				"Union({%1$s, %2$s}, Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER))", 
 				"Union({%1$s}, Union(Descendants(%2$s, 1, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER)))", 
 				"Except(Union({%1$s}, Descendants(%1$s, 1, SELF_AND_AFTER)), Descendants({%2$s}, 0, AFTER))",
 		};
 
 		int i = 0;
 		for (State st1 : State.values()) {
 			for (State st2 : State.values()) {
 				qh.clear();
 				st1.apply(rootMember);
 				st2.apply(childMember);
 				if (i < notDrilled.length) {
 					System.out.println(st1 + "/" + st2);
 					assertDrillExpression(notDrilled[i++], qh.toOlap4j(expander, Arrays.asList(childMember)),
 							rootMember, childMember);
 				}
 			}
 		}
 	}
 
 	@Test
 	public void testTwoLevels_undrillGrandson() {
 		String[] notDrilled = new String[] {
 				// Root: Ed_Ech_Em
 				"{}", 
 				"{%2$s}", 
 				"%2$s.Children", 
 				"DrilldownMember({%2$s}, {%2$s}, RECURSIVE)",
 				"Descendants(%2$s, 2, SELF_AND_AFTER)", 
 				"Union({%2$s}, Descendants(%2$s, 2, SELF_AND_AFTER))", 
 				"Except(Descendants(%2$s, 1, SELF_AND_AFTER), Descendants({%3$s}, 0, AFTER))", 
 				"Except(Union({%2$s}, Descendants(%2$s, 1, SELF_AND_AFTER)), Descendants({%3$s}, 0, AFTER))",
 
 				// Root: Ed_Ech_Im
 				"{%1$s}", 
 				"{%1$s, %2$s}", 
 				"Union({%1$s}, %2$s.Children)", 
 				"DrilldownMember({%1$s, %2$s}, {%2$s}, RECURSIVE)", 
 				"Union({%1$s}, Descendants(%2$s, 2, SELF_AND_AFTER))", 
 				"Union({%1$s, %2$s}, Descendants(%2$s, 2, SELF_AND_AFTER))",
 				"Except(Union({%1$s}, Descendants(%2$s, 1, SELF_AND_AFTER)), Descendants({%3$s}, 0, AFTER))", 
 				"Except(Union({%1$s, %2$s}, Descendants(%2$s, 1, SELF_AND_AFTER)), Descendants({%3$s}, 0, AFTER))",
 				
 				// Root: Ed_Ich_Em
 				"Except(%1$s.Children, {%2$s})",
 				"%1$s.Children",
 				"Except(Union(%2$s.Children, %1$s.Children), {%2$s})",
 				"DrilldownMember(%1$s.Children, {%2$s}, RECURSIVE)",
 				"Except(Union(Descendants(%2$s, 2, SELF_AND_AFTER), %1$s.Children), {%2$s})",
 				"Union(Descendants(%2$s, 2, SELF_AND_AFTER), %1$s.Children)",
 				"Except(Union(Descendants(%2$s, 1, SELF_AND_AFTER), %1$s.Children), Union({%2$s}, Descendants({%3$s}, 0, AFTER)))",
 				"Except(Union(Descendants(%2$s, 1, SELF_AND_AFTER), %1$s.Children), Descendants({%3$s}, 0, AFTER))",
 				
 				// Root: Ed_Ich_Im
 				"Except(DrilldownMember({%1$s}, {%1$s}, RECURSIVE), {%2$s})", 
 				"DrilldownMember({%1$s}, {%1$s}, RECURSIVE)", 
 				"Except(DrilldownMember(Union({%1$s}, %2$s.Children), {%1$s}, RECURSIVE), {%2$s})", 
 				"DrilldownMember({%1$s}, {%2$s, %1$s}, RECURSIVE)",
 				"Except(DrilldownMember(Union({%1$s}, Descendants(%2$s, 2, SELF_AND_AFTER)), {%1$s}, RECURSIVE), {%2$s})", 
 				"DrilldownMember(Union({%1$s}, Descendants(%2$s, 2, SELF_AND_AFTER)), {%1$s}, RECURSIVE)", 
 				"Except(DrilldownMember(Union({%1$s}, Descendants(%2$s, 1, SELF_AND_AFTER)), {%1$s}, RECURSIVE), Union({%2$s}, Descendants({%3$s}, 0, AFTER)))", 
 				"Except(DrilldownMember(Union({%1$s}, Descendants(%2$s, 1, SELF_AND_AFTER)), {%1$s}, RECURSIVE), Descendants({%3$s}, 0, AFTER))",
 
 				// Root: Id_Ech_Em
 				"Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER)",
 				"Union({%2$s}, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))",
 				"Union(%2$s.Children, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))", 
 				"DrilldownMember(Union({%2$s}, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER)), {%2$s}, RECURSIVE)",
 				"Union(Descendants(%2$s, 2, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))",
 				"Union({%2$s}, Union(Descendants(%2$s, 2, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER)))",
 				"Except(Union(Descendants(%2$s, 1, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER)), Descendants({%3$s}, 0, AFTER))", 
 				"Except(Union({%2$s}, Union(Descendants(%2$s, 1, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))), Descendants({%3$s}, 0, AFTER))",
 				
 				// Root: Id_Ech_Im
 				"Union({%1$s}, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))", 
 				"Union({%1$s, %2$s}, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))", 
 				"Union({%1$s}, Union(%2$s.Children, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER)))", 
 				"DrilldownMember(Union({%1$s, %2$s}, Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER)), {%2$s}, RECURSIVE)", 
 				"Union({%1$s}, Union(Descendants(%2$s, 2, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER)))", 
 				"Union({%1$s, %2$s}, Union(Descendants(%2$s, 2, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER)))", 
 				"Except(Union({%1$s}, Union(Descendants(%2$s, 1, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))), Descendants({%3$s}, 0, AFTER))", 
 				"Except(Union({%1$s, %2$s}, Union(Descendants(%2$s, 1, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 1, SELF_AND_AFTER))), Descendants({%3$s}, 0, AFTER))",
 
 				// Root: Id_Ich_Em
 				"Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER)", 
 				"Union({%2$s}, Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER))",
 				"Union(%2$s.Children, Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER))",
 				"DrilldownMember(Union({%2$s}, Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER)), {%2$s}, RECURSIVE)",
 				"Union(Descendants(%2$s, 2, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER))",
 				"Union({%2$s}, Union(Descendants(%2$s, 2, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER)))",
 				"Except(Union(Descendants(%2$s, 1, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER)), Descendants({%3$s}, 0, AFTER))",
 				"Except(Descendants(%1$s, 1, SELF_AND_AFTER), Descendants({%3$s}, 0, AFTER))",
 				
 				// Root: Id_Ich_Im
 				"Union({%1$s}, Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER))",  
 				"Union({%1$s, %2$s}, Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER))",
 				"Union({%1$s}, Union(%2$s.Children, Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER)))", 
 				"DrilldownMember(Union({%1$s, %2$s}, Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER)), {%2$s}, RECURSIVE)", 
 				"Union({%1$s}, Union(Descendants(%2$s, 2, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER)))", 
 				"Union({%1$s, %2$s}, Union(Descendants(%2$s, 2, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER)))", 
 				"Except(Union({%1$s}, Union(Descendants(%2$s, 1, SELF_AND_AFTER), Descendants(Except(%1$s.Children, {%2$s}), 0, SELF_AND_AFTER))), Descendants({%3$s}, 0, AFTER))", 
 				"Except(Union({%1$s}, Descendants(%1$s, 1, SELF_AND_AFTER)), Descendants({%3$s}, 0, AFTER))",
 		};
 
 		int i = 0;
 		for (State st1 : State.values()) {
 			for (State st2 : State.values()) {
 				qh.clear();
 				st1.apply(rootMember);
 				st2.apply(childMember);
 				if (i < notDrilled.length) {
 					System.out.println(st1 + "/" + st2);
 					assertDrillExpression(notDrilled[i++], qh.toOlap4j(expander, Arrays.asList(grandsonMember)),
 							rootMember, childMember, grandsonMember);
 				}
 			}
 		}
 	}
 	
 	private void assertMdx(String expected, ParseTreeNode actual) {
 		StringWriter swr = new StringWriter();
 		ParseTreeWriter wr = new ParseTreeWriter(swr);
 		actual.unparse(wr);
 		assertEquals(expected, swr.toString());
 	}
 
 	private void assertDrillExpression(String format, ParseTreeNode expression,
 			Member... args) {
 		assertMdx(String.format(format, (Object[]) args), expression);
 	}
 
 }
