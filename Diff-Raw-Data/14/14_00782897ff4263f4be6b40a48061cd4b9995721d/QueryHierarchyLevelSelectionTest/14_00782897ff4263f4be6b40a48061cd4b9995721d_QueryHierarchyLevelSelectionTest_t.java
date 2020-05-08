 package es.cgalesanco.olap4j.query;
 
 import java.util.Arrays;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.olap4j.OlapException;
 import org.olap4j.mdx.ParseTreeNode;
 import org.olap4j.metadata.Hierarchy;
 import org.olap4j.metadata.Level;
 import org.olap4j.metadata.Member;
 
 import es.cgalesanco.olap4j.query.Selection.Operator;
 
 public class QueryHierarchyLevelSelectionTest extends QueryTestBase {
 	private static Hierarchy baseHierarchy;
 	private static Member rootMember;
 	private static Member childMember;
 	private static Level rootLevel;
 	private static Level childLevel;
 	private static Level grandsonLevel;
 	private QueryHierarchy testHierarchy;
 	private HierarchyExpander expander;
 	
 
 	@BeforeClass
 	public static void setUpFixture() throws Exception {
 		QueryTestBase.setUpFixture();
 		baseHierarchy = getCube().getHierarchies().get("Time");
 		rootLevel = baseHierarchy.getLevels().get(0);
 		childLevel = baseHierarchy.getLevels().get(1);
 		grandsonLevel = baseHierarchy.getLevels().get(2);
 		rootMember = baseHierarchy.getRootMembers().get(0);
 		childMember = rootMember.getChildMembers().get("Q2");
 	}
 
 	@Before
 	public void setUp() throws OlapException {
 		Query query = new Query("test", getCube());
 		testHierarchy = query.getHierarchy(baseHierarchy.getName());
 		expander = new HierarchyExpander();
 	}
 	
 	@Test
 	public void testOneLevelSelection_rootLevel_noDrill() {
 		testHierarchy.include(rootLevel);
 		 
 		testHierarchy.toOlap4j();
 		assertMdx("%1$s.AllMembers", testHierarchy.toOlap4j(), rootLevel);
 	}
 
 	@Test
 	public void testOneLevelSelection_rootLevel_drillRoot() throws Exception {
 		testHierarchy.include(rootLevel);
 		expander.setDrills(Arrays.asList(rootMember));
 		ParseTreeNode exp = testHierarchy.toOlap4j(expander);
 		assertMdx("%1$s.AllMembers", exp, rootLevel);
 	}
 
 	@Test
 	public void testOneLevelSelection_rootLevel_drillChild() throws Exception {
 		testHierarchy.include(rootLevel);
 		
 		expander.setDrills(Arrays.asList(childMember));
 		ParseTreeNode exp = testHierarchy.toOlap4j(new HierarchyExpander());
 		assertMdx("%1$s.AllMembers", exp, rootLevel);
 	}
 
 	@Test
 	public void testTwoLevelSelection_rootLevel_childLevel_noDrill_expanded() throws Exception {
 		HierarchyExpander expander = new HierarchyExpander();
 		expander.expandHierarchy();
 		testHierarchy.getAxis().expandHierarchy(testHierarchy);
 		testHierarchy.include(rootLevel);
 		testHierarchy.include(childLevel);
 		
 		 
 		ParseTreeNode exp = testHierarchy.toOlap4j(expander);
 		assertMdx("Union(%1$s.AllMembers, %2$s.AllMembers)", exp, rootLevel, childLevel);
 	}
 	
 	@Test
 	public void testOneLevelSelection_rootLevel_noDrill_expanded() throws Exception {
 		expander.expandHierarchy();
 		testHierarchy.include(Operator.DESCENDANTS, rootMember);
 		testHierarchy.include(rootLevel);
 		
 		ParseTreeNode exp = testHierarchy.toOlap4j(expander);
 		assertMdx("Union(Year.AllMembers, Descendants(%1$s, 1, SELF_AND_AFTER))", exp, rootMember, rootLevel);
 	}
 	
 	@Test
 	public void testOneLevelSelection_rootLevel_excludedRootBefore_noDrill_expanded() throws Exception {
 		expander.expandHierarchy();
 		testHierarchy.exclude(Operator.DESCENDANTS, rootMember);
 		testHierarchy.include(rootLevel);
 		
 		 
 		ParseTreeNode exp = testHierarchy.toOlap4j(expander);
 		assertMdx("%2$s.AllMembers", exp, rootMember, rootLevel);
 	}
 
 	@Test
 	public void testOneLevelSelection_rootLevel_excludedRootAfter_noDrill_expanded() throws Exception {
 		expander.expandHierarchy();
 		testHierarchy.include(rootLevel);
 		testHierarchy.exclude(Operator.DESCENDANTS, rootMember);
 		
 		 
 		ParseTreeNode exp = testHierarchy.toOlap4j(expander);
 		assertMdx("Except(%2$s.AllMembers, {%1$s})", exp, rootMember, rootLevel);
 	}
 	
 	@Test
 	public void testOneLevelSelection_childLevel_excludedRootChildrenBefore_noDrill_expanded() throws Exception {
 		expander.expandHierarchy();
 		testHierarchy.exclude(Operator.CHILDREN, rootMember);
 		testHierarchy.include(childLevel);
 		
 		 
 		ParseTreeNode exp = testHierarchy.toOlap4j(expander);
 		assertMdx("%2$s.AllMembers", exp, rootMember, childLevel);
 	}
 
 	@Test
 	public void testOneLevelSelection_childLevel_excludedRootChildrenAfter_noDrill_expanded() throws Exception {
 		expander.expandHierarchy();
 		testHierarchy.include(childLevel);
 		testHierarchy.exclude(Operator.CHILDREN, rootMember);
 				
 		 
 		ParseTreeNode exp = testHierarchy.toOlap4j(expander);
 		assertMdx("Except(%2$s.AllMembers, [Time].[1997].Children)", exp, rootMember, childLevel);
 	}
 	
 	@Test
 	public void testOneLevelSelection_childLevel_includedRootDescendantsBefore_noDrill_expanded() throws Exception {
 		expander.expandHierarchy();
 		testHierarchy.include(Operator.DESCENDANTS, rootMember);
 		testHierarchy.exclude(childLevel);
 				
 		 
 		ParseTreeNode exp = testHierarchy.toOlap4j(expander);
 		assertMdx("Union({%1$s}, Descendants(%1$s, 2, SELF_AND_AFTER))", exp, rootMember);
 	}
 	
 	@Test
 	public void testOneLevelSelection_includeChildLevel_includedRootDescendatsBefore_nodDrill_epanded() throws Exception {
 		expander.expandHierarchy();
 		testHierarchy.include(Operator.DESCENDANTS, rootMember);
 		testHierarchy.include(childLevel);
 				
 		 
 		ParseTreeNode exp = testHierarchy.toOlap4j(expander);
 		assertMdx("Union({%1$s}, Union(%2$s.AllMembers, Descendants(%1$s, 2, SELF_AND_AFTER)))", exp, rootMember, childLevel);
 		
 	}
 	
 	@Test
 	public void testOneLevelSelection_includeGrandsonLevel_includedRootDescendatsBefore_nodDrill_epanded() throws Exception {
 		expander.expandHierarchy();
 		testHierarchy.include(Operator.DESCENDANTS, rootMember);
 		testHierarchy.include(grandsonLevel);
 				
 		 
 		ParseTreeNode exp = testHierarchy.toOlap4j(expander);
 		
 		assertMdx("Union(%2$s.AllMembers, Descendants(%1$s, 0, SELF_AND_AFTER))", exp, rootMember, grandsonLevel, rootLevel);
 	}
 	
 	@Test
 	public void testTwoConsecutiveLevelSelection_rootDrill() throws Exception {
 		HierarchyExpander expander = new HierarchyExpander();
 		testHierarchy.include(rootLevel);
 		testHierarchy.include(childLevel);
 	
 		expander.setDrills(Arrays.asList(rootMember));
 		ParseTreeNode exp = testHierarchy.toOlap4j(expander);
 		assertMdx("DrilldownMember(%1$s.AllMembers, {%2$s}, RECURSIVE)", exp, rootLevel, rootMember);
 	}
 	
 	@Test
 	public void testTwoNonConsecutiveLevelSelection_rootDrill() throws Exception {
 		testHierarchy.include(rootLevel);
 		testHierarchy.include(grandsonLevel);
 				
 		expander.setDrills(Arrays.asList(rootMember));
 		ParseTreeNode exp = testHierarchy.toOlap4j(expander);
		assertMdx("Union(Descendants({%2$s}, 2), %1$s.AllMembers)", exp, rootLevel, rootMember);
 	}
 
 	@Test
 	public void testAba_rootDrill() throws Exception {
 		testHierarchy.include(rootLevel);
 		testHierarchy.include(grandsonLevel);
 				
 		expander.setDrills(Arrays.asList(rootMember));
 		ParseTreeNode exp = testHierarchy.toOlap4j(expander);
		assertMdx("Union(Descendants({%2$s}, 2), %1$s.AllMembers)", exp, rootLevel, rootMember);
 	}
 	
 	@Test
 	public void testMixedDrill() throws Exception {
 		testHierarchy.include(rootLevel);
 		testHierarchy.include(grandsonLevel);
 		testHierarchy.include(Operator.MEMBER, childMember);
 
 		expander.setDrills(Arrays.asList(rootMember,childMember));
 		ParseTreeNode exp = testHierarchy.toOlap4j(expander);
 		
		assertMdx("DrilldownMember(Union({%3$s}, Union(Descendants(Except(%2$s.Children, {%3$s}), Month), %1$s.AllMembers)), {%3$s}, RECURSIVE)", exp, rootLevel, rootMember, childMember);
 	}
 }
