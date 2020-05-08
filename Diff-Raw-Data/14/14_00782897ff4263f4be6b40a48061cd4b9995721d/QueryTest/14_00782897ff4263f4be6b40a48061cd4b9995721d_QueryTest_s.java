 package es.cgalesanco.olap4j.query;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertNotNull;
 import static org.junit.Assert.assertNull;
 import static org.junit.Assert.assertSame;
 import static org.junit.Assert.assertTrue;
 
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Ignore;
 import org.junit.Test;
 import org.olap4j.Axis;
 import org.olap4j.mdx.CubeNode;
 import org.olap4j.mdx.SelectNode;
 import org.olap4j.metadata.Cube;
 import org.olap4j.metadata.Hierarchy;
 import org.olap4j.metadata.Member;
 import org.olap4j.metadata.NamedList;
 
 import es.cgalesanco.olap4j.query.Selection.Operator;
 
 public class QueryTest {
 	private static CubeMock cube;
 	private Query query;
 
 	@BeforeClass
 	public static void setupFixture() throws Exception {
 		cube = MetadataFixture.createCube();
 	}
 
 	private static final String QUERY_NAME = "test query";
 
 	@Before
 	public void setUp() {
 		query = new Query(QUERY_NAME, cube);
 	}
 
 	/**
 	 * Tests {@link Query#Query(String, org.olap4j.metadata.Cube)}
 	 */
 	@Test
 	public void testConstructor() {
 
 		assertSame(cube, query.getCube());
 		assertEquals(QUERY_NAME, query.getName());
 		for (Entry<Axis, QueryAxis> e : query.getAxes().entrySet()) {
 			if (e.getKey() != null) {
 				// Initially every proper QueryAxis is empty
 				assertTrue(e.getKey().toString(), e.getValue().getHierarchies()
 						.isEmpty());
 			} else {
 				// The unused axis contains every hierarchy in the cube
 				QueryAxis unusedAxis = e.getValue();
 				NamedList<Hierarchy> cubeHierarchies = cube.getHierarchies();
 				List<QueryHierarchy> queryHierarchies = unusedAxis
 						.getHierarchies();
 				assertEquals(cubeHierarchies.size(), queryHierarchies.size());
 				for (QueryHierarchy qh : queryHierarchies) {
 					Hierarchy expected = cubeHierarchies.get(qh.getHierarchy()
 							.getName());
 					assertEquals(expected, qh.getHierarchy());
 				}
 			}
 		}
 	}
 	
 	@Test
 	public void testGetSelect_oneAxis() throws Exception {
 		Cube c = query.getCube();
 		QueryHierarchy hMeasures = query.getHierarchy("Measures");
 		for(Member m : hMeasures.getHierarchy().getRootMembers()) {
 			hMeasures.include(Operator.MEMBER, m);
 		}
 		query.getAxis(Axis.COLUMNS).addHierarchy(hMeasures);
 		
 		SelectNode select = query.getSelect();
 		
 		assertTrue(select.getFrom() instanceof CubeNode);
 		assertEquals(c, ((CubeNode)(select.getFrom())).getCube());
 		assertEquals(1, select.getAxisList().size());
 		assertNull(select.getFilterAxis().getExpression());
 	}
 
 	@Test
 	public void testGetSelect_twoAxes() throws Exception {
 		Cube c = query.getCube();
 		QueryHierarchy hMeasures = query.getHierarchy("Measures");
 		for(Member m : hMeasures.getHierarchy().getRootMembers()) {
 			hMeasures.include(Operator.MEMBER, m);
 		}
 		query.getAxis(Axis.COLUMNS).addHierarchy(hMeasures);
 		
 		QueryHierarchy hTime = query.getHierarchy("Time");
 		for(Member m : hTime.getHierarchy().getRootMembers()) {
 			hTime.include(Operator.MEMBER, m);
 		}
 		query.getAxis(Axis.ROWS).addHierarchy(hTime);
 		
 		SelectNode select = query.getSelect();
 		
 		assertTrue(select.getFrom() instanceof CubeNode);
 		assertEquals(c, ((CubeNode)(select.getFrom())).getCube());
 		assertEquals(2, select.getAxisList().size());
 		assertNull(select.getFilterAxis().getExpression());
 	}
 	
 	@Test
	@Ignore
 	public void testGetSelect_twoAxesAndFilter() throws Exception {
 		Cube c = query.getCube();
 		QueryHierarchy hMeasures = query.getHierarchy("Measures");
 		for(Member m : hMeasures.getHierarchy().getRootMembers()) {
 			hMeasures.include(Operator.MEMBER, m);
 		}
 		query.getAxis(Axis.COLUMNS).addHierarchy(hMeasures);
 		
 		QueryHierarchy hTime = query.getHierarchy("Time");
 		for(Member m : hTime.getHierarchy().getRootMembers()) {
 			hTime.include(Operator.MEMBER, m);
 		}
 		query.getAxis(Axis.ROWS).addHierarchy(hTime);
 		
 		QueryHierarchy hGender = query.getHierarchy("Gender");
 		Member male = hGender.getHierarchy().getRootMembers().get(0);
 		hGender.include(Operator.MEMBER, male);
 		query.getAxis(Axis.FILTER).addHierarchy(hGender);
 		
 		SelectNode select = query.getSelect();
 		
 		assertTrue(select.getFrom() instanceof CubeNode);
 		assertEquals(c, ((CubeNode)(select.getFrom())).getCube());
 		assertEquals(2, select.getAxisList().size());
 		assertNotNull(select.getFilterAxis().getExpression());
 	}
 	
 	/**
 	 * Tests that {@link Query#getAxis(Axis)} is congruent with @{link
 	 * {@link Query#getAxes()} and {@link Query#getUnusedAxis()}.
 	 */
 	@Test
 	public void testGetAxis() {
 		Map<Axis, QueryAxis> axes = query.getAxes();
 		assertSame(axes.get(Axis.ROWS), query.getAxis(Axis.ROWS));
 		assertSame(axes.get(Axis.COLUMNS), query.getAxis(Axis.COLUMNS));
 		assertSame(axes.get(Axis.FILTER), query.getAxis(Axis.FILTER));
 		assertSame(axes.get(null), query.getAxis(null));
 		assertSame(axes.get(null), query.getUnusedAxis());
 	}
 
 	/**
 	 * Tests {@link Query#getHierarchy(String)}
 	 */
 	@Test
 	public void testGetHierarchy() {
 		for(Hierarchy h : cube.getHierarchies()) {
 			QueryHierarchy qh = query.getHierarchy(h.getName());
 			assertEquals(h, qh.getHierarchy());
 		}
 	}
 }
