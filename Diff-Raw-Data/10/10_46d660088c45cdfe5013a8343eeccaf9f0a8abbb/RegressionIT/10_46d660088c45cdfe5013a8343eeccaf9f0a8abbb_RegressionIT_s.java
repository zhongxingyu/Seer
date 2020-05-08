 package es.cgalesanco.olap4j.query;
 
 import static org.junit.Assert.assertEquals;
 
 import java.io.File;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.util.Arrays;
 
 import org.junit.AfterClass;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.olap4j.Axis;
 import org.olap4j.CellSet;
 import org.olap4j.CellSetAxis;
 import org.olap4j.OlapConnection;
 import org.olap4j.Position;
 import org.olap4j.metadata.Cube;
 import org.olap4j.metadata.Member;
 
 import es.cgalesanco.olap4j.query.Selection.Operator;
 
 public class RegressionIT {
 	/**
 	 * Helper class to parse an expected cellset position.
 	 * @author César García
 	 *
 	 */
 	private static class ExpectedMemberInfo {
 
 		private String name;
 		private boolean isLeaf;
 		private boolean isDrilled;
 
 		public ExpectedMemberInfo(String name, boolean leaf, boolean drilled) {
 			this.name = name;
 			this.isLeaf = leaf;
 			this.isDrilled = drilled;
 		}
 
 		public static ExpectedMemberInfo[] parse(String line) {
 			String members[] = line.split(",");
 			ExpectedMemberInfo[] result = new ExpectedMemberInfo[members.length];
 			for(int i = 0; i < members.length; ++i) {
 				String member = members[i].trim();
 				switch( member.charAt(0) ) {
 				case '+':
 					result[i] = new ExpectedMemberInfo(member.substring(1), false, false);
 					break;
 					
 				case '-':
 					result[i] = new ExpectedMemberInfo(member.substring(1), false, true);
 					break;
 					
 				default:
 					result[i] = new ExpectedMemberInfo(member, true, false);
 				}
 			}
 			return result;
 		}
 		
 		public String getMemberName() {
 			return name;
 		}
 		
 		public boolean isLeaf() {
 			return isLeaf;
 		}
 		
 		public boolean isDrilled() {
 			return isDrilled;
 		}
 	
 	}
 
 	private static Cube cube; 
 	private static OlapConnection cn;
 	
 	private Query query;
 	private QueryAxis testAxis;
 	private QueryHierarchy[] testHierarchies = new QueryHierarchy[5];
 	private QueryHierarchy measuresQueryHierarchy;
 	
 	@BeforeClass
 	public static void setUpFixture() throws Exception {
 			// Establish the connection with the provider and retrieves the "Sales" cube 
 			Class.forName("mondrian.olap4j.MondrianOlap4jDriver");
 
 			// Copy FoodMart catalog resource into a temporary file 
 			File catalogFile = File.createTempFile("foodmart", ".xml");
 			FixtureUtils.copyResource("FoodMart.xml", catalogFile.getParentFile(), catalogFile.getName());
 
 			// TODO change to retrieve connection info from the integration testing environment.
 			Connection jdbcCn = DriverManager.getConnection("jdbc:mondrian:"
 					+ "JdbcDrivers=com.mysql.jdbc.Driver;"
 					+ "Jdbc=jdbc:mysql://localhost/foodmart;"
 					+ "JdbcUser=root;JdbcPassword=root;"
 					+ "Catalog=file:"+catalogFile.getAbsolutePath());
 			cn = jdbcCn.unwrap(OlapConnection.class);
 			cube = cn.getOlapSchema().getCubes().get("Sales");
 
 	}
 	
 	@Before
 	public void setUp() throws Exception {
 		query = new Query("Test", cube);
 		
 
 		// Creates the test hierarchy (Time) 
 		testHierarchies[0] = query.getHierarchy("Time");
 		testAxis = query.getAxes().get(Axis.ROWS);
 		testAxis.addHierarchy(testHierarchies[0]);
 		
 		// Creates a COLUMNS for the test query
 		measuresQueryHierarchy = query.getHierarchy("Measures");
 		Member measure = measuresQueryHierarchy.getHierarchy().getRootMembers().get("Unit Sales");
 		measuresQueryHierarchy.include(Operator.MEMBER, measure);
 		query.getAxes().get(Axis.COLUMNS).addHierarchy(measuresQueryHierarchy);
 	}
 	
 	@AfterClass
 	public static void tearDownFixture() throws Exception {
 		if ( cn != null )
 			cn.close();
 	}
 	
 	@Test
 	public void testSingleHierarchyState() throws Exception {
 		// Adds another hierarchy to the test axis to avoid a Mondrian bug.
 		// Comment out this lines to reproduce the bug.
 		QueryHierarchy workaroundHierarchy = query.getHierarchy("Store Type");
 		workaroundHierarchy.include(Operator.MEMBER, workaroundHierarchy.getHierarchy().getRootMembers().get(0));
 		testAxis.addHierarchy(workaroundHierarchy);
 		
 		// Initial state for a QueryHierarchy: no member included. 
 		assertRowsMembers();
 
 		// Inclusion of DESCENDANTS of [Time].[1997].
 		// It's not drilled, so we get just just that member; it's not a leaf for 
 		// the query hierarchy, and it's not drilled.
 		Member root1997 = testHierarchies[0].getHierarchy().getRootMembers().get("1997");
 		Member root1998 = testHierarchies[0].getHierarchy().getRootMembers().get("1998");
 		testHierarchies[0].include(Operator.DESCENDANTS, root1997);
 		assertRowsMembers("+[Time].[1997]");
 		
 		// We execute a drill on [Time].[1997].
 		// Now the test hierarchy includes the set of children of this member;
 		testAxis.drill(root1997);
 		assertRowsMembers(
 				"-[Time].[1997]",
 				" +[Time].[1997].[Q1]",
 				" +[Time].[1997].[Q2]",
 				" +[Time].[1997].[Q3]",
 				" +[Time].[1997].[Q4]");
 		
 		// We drill down [Time].[1997].[Q2].
 		// The resulting cellset includes months for Q2. They are leafs
 		testAxis.drill(root1997.getChildMembers().get("Q2"));
 		assertRowsMembers(
 				"-[Time].[1997]",
 				" +[Time].[1997].[Q1]",
 				" -[Time].[1997].[Q2]",
 				"   [Time].[1997].[Q2].[4]",
 				"   [Time].[1997].[Q2].[5]",
 				"   [Time].[1997].[Q2].[6]",
 				" +[Time].[1997].[Q3]",
 				" +[Time].[1997].[Q4]");
 		
 		// Includes MEMBER [Time].[1998]
 		// We arent' including its descendants, so this member is a leaf for the query hierarchy
 		testHierarchies[0].include(Operator.MEMBER, root1998);
 		assertRowsMembers(
 				"-[Time].[1997]",
 				" +[Time].[1997].[Q1]",
 				" -[Time].[1997].[Q2]",
 				"   [Time].[1997].[Q2].[4]",
 				"   [Time].[1997].[Q2].[5]",
 				"   [Time].[1997].[Q2].[6]",
 				" +[Time].[1997].[Q3]",
 				" +[Time].[1997].[Q4]",
 				" [Time].[1998]");
 		
 		// Excludes MEMBER, [Time].[1997].[Q2]
 		// Children of Q2 become now children of 1997 for this query hierarchy
 		// TODO: Report olap4j error evaluating this MDX; JPivot evaluates it
 		//	correctly
 		testHierarchies[0].exclude(Operator.MEMBER, root1997.getChildMembers().get("Q2"));
 		assertRowsMembers(
 				"-[Time].[1997]",
 				" +[Time].[1997].[Q1]",
 				"   [Time].[1997].[Q2].[4]",
 				"   [Time].[1997].[Q2].[5]",
 				"   [Time].[1997].[Q2].[6]",
 				" +[Time].[1997].[Q3]",
 				" +[Time].[1997].[Q4]",
 				" [Time].[1998]");
 
 		// Undrill of [Time].[1997]
 		// It's descendants are colapsed
 		testAxis.undrill(root1997);
 		assertRowsMembers(
 				"+[Time].[1997]",
 				" [Time].[1998]");
 
 		// Drills down [Time].[1997].[Q3], a "hidden" member. 
 		// The resulting cellset doesn't changes, but...
 		testAxis.drill(root1997.getChildMembers().get("Q3"));
 		assertRowsMembers(
 				"+[Time].[1997]",
 				" [Time].[1998]");
 
 		// the drill of a hidden member is honored when it becames visible 
 		// again.
 		testAxis.drill(root1997);
 		assertRowsMembers(
 				"-[Time].[1997]",
 				" +[Time].[1997].[Q1]",
 				"   [Time].[1997].[Q2].[4]",
 				"   [Time].[1997].[Q2].[5]",
 				"   [Time].[1997].[Q2].[6]",
 				" -[Time].[1997].[Q3]",
 				"   [Time].[1997].[Q3].[7]",
 				"   [Time].[1997].[Q3].[8]",
 				"   [Time].[1997].[Q3].[9]",
 				" +[Time].[1997].[Q4]",
 				" [Time].[1998]");
 
 		// Excludes CHILDREN of [Time].[1997]. 
 		testHierarchies[0].exclude(Operator.CHILDREN, root1997);
 		assertRowsMembers(
 				"-[Time].[1997]",
 				"   [Time].[1997].[Q1].[1]",
 				"   [Time].[1997].[Q1].[2]",
 				"   [Time].[1997].[Q1].[3]",
 				"   [Time].[1997].[Q2].[4]",
 				"   [Time].[1997].[Q2].[5]",
 				"   [Time].[1997].[Q2].[6]",
 				"   [Time].[1997].[Q3].[7]",
 				"   [Time].[1997].[Q3].[8]",
 				"   [Time].[1997].[Q3].[9]",
 				"   [Time].[1997].[Q4].[10]",
 				"   [Time].[1997].[Q4].[11]",
 				"   [Time].[1997].[Q4].[12]",
 				" [Time].[1998]");
 		
 		// Includes CHILDREN of [Time].[1998]
 		// This member is not drilled, show its children doesn't show up. 
 		// Now it's not a leaf
 		testHierarchies[0].include(Operator.CHILDREN, root1998);
 		assertRowsMembers(
 				"-[Time].[1997]",
 				"   [Time].[1997].[Q1].[1]",
 				"   [Time].[1997].[Q1].[2]",
 				"   [Time].[1997].[Q1].[3]",
 				"   [Time].[1997].[Q2].[4]",
 				"   [Time].[1997].[Q2].[5]",
 				"   [Time].[1997].[Q2].[6]",
 				"   [Time].[1997].[Q3].[7]",
 				"   [Time].[1997].[Q3].[8]",
 				"   [Time].[1997].[Q3].[9]",
 				"   [Time].[1997].[Q4].[10]",
 				"   [Time].[1997].[Q4].[11]",
 				"   [Time].[1997].[Q4].[12]",
 				"+[Time].[1998]");
 		
 		// Finally we drilldwon [Time].[1998] to show up its children.
 		testAxis.drill(root1998);
 		assertRowsMembers(
 				"-[Time].[1997]",
 				"   [Time].[1997].[Q1].[1]",
 				"   [Time].[1997].[Q1].[2]",
 				"   [Time].[1997].[Q1].[3]",
 				"   [Time].[1997].[Q2].[4]",
 				"   [Time].[1997].[Q2].[5]",
 				"   [Time].[1997].[Q2].[6]",
 				"   [Time].[1997].[Q3].[7]",
 				"   [Time].[1997].[Q3].[8]",
 				"   [Time].[1997].[Q3].[9]",
 				"   [Time].[1997].[Q4].[10]",
 				"   [Time].[1997].[Q4].[11]",
 				"   [Time].[1997].[Q4].[12]",
 				"-[Time].[1998]",
 				"   [Time].[1998].[Q1]",
 				"   [Time].[1998].[Q2]",
 				"   [Time].[1998].[Q3]",
 				"   [Time].[1998].[Q4]"
 				);
 	}
 	
 	@Test
 	public void testTwoHierarchies() throws Exception {
 		testHierarchies[1] = query.getHierarchy("Store");
 		testHierarchies[1].include(Operator.MEMBER, testHierarchies[1].getHierarchy().getRootMembers().get(0));
 		testAxis.addHierarchy(testHierarchies[1]);
 		
 		Member allStores = testHierarchies[1].getHierarchy().getRootMembers().get(0);
 		Member root1997 = testHierarchies[0].getHierarchy().getRootMembers().get("1997");
 		Member root1998 = testHierarchies[0].getHierarchy().getRootMembers().get("1998");
 		
 		// Includes DESCENDANTS of all stores
 		// The [Time] query hierarchy includes no member, so the crossjoin produces an
 		// empty members set for the ROWS axis. 
 		testHierarchies[1].include(Operator.DESCENDANTS, allStores);
 		assertRowsMembers();
 
 		// Includes the full [Time] hierarchy.
 		testHierarchies[0].include(Operator.DESCENDANTS, root1997);
 		testHierarchies[0].include(Operator.DESCENDANTS, root1998);
 		assertRowsMembers(
 				"+[Time].[1997]               ,+[Store].[All Stores]",			
 				"+[Time].[1998]               ,+[Store].[All Stores]"			
 				);
 
 		// Drills ([Time].[1998],[Store].[All Stores])
 		testAxis.drill(root1998, allStores);
 		assertRowsMembers(
 				"+[Time].[1997]               ,+[Store].[All Stores]",			
 				"+[Time].[1998]               ,-[Store].[All Stores]",			
 				"+[Time].[1998]               , +[Store].[Canada]",			
 				"+[Time].[1998]               , +[Store].[Mexico]",			
 				"+[Time].[1998]               , +[Store].[USA]"			
 				);
 
 		// Drills ([Time].[1997])
 		testAxis.drill(root1997);
 		assertRowsMembers(
 				"-[Time].[1997]               ,+[Store].[All Stores]",			
 				" +[Time].[1997].[Q1]         ,+[Store].[All Stores]",			
 				" +[Time].[1997].[Q2]         ,+[Store].[All Stores]",			
 				" +[Time].[1997].[Q3]         ,+[Store].[All Stores]",			
 				" +[Time].[1997].[Q4]         ,+[Store].[All Stores]",			
 				"+[Time].[1998]               ,-[Store].[All Stores]",			
 				"+[Time].[1998]               , +[Store].[Canada]",			
 				"+[Time].[1998]               , +[Store].[Mexico]",			
 				"+[Time].[1998]               , +[Store].[USA]"			
 				);
 
 		// Drills ([Time].[1997].[Q2],[Store].[All Stores])
 		Member m1997_Q2 = root1997.getChildMembers().get("Q2");
 		testAxis.drill(m1997_Q2, allStores);
 		assertRowsMembers(
 				"-[Time].[1997]               ,+[Store].[All Stores]",			
 				" +[Time].[1997].[Q1]         ,+[Store].[All Stores]",			
 				" +[Time].[1997].[Q2]         ,-[Store].[All Stores]",			
 				" +[Time].[1997].[Q2]         , +[Store].[Canada]",			
 				" +[Time].[1997].[Q2]         , +[Store].[Mexico]",			
 				" +[Time].[1997].[Q2]         , +[Store].[USA]",			
 				" +[Time].[1997].[Q3]         ,+[Store].[All Stores]",			
 				" +[Time].[1997].[Q4]         ,+[Store].[All Stores]",			
 				"+[Time].[1998]               ,-[Store].[All Stores]",			
 				"+[Time].[1998]               , +[Store].[Canada]",			
 				"+[Time].[1998]               , +[Store].[Mexico]",			
 				"+[Time].[1998]               , +[Store].[USA]"			
 				);
 		
 		// Exclude stores in Canada.
 		testHierarchies[1].exclude(Operator.DESCENDANTS, allStores.getChildMembers().get("Canada"));
 		assertRowsMembers(
 				"-[Time].[1997]               ,+[Store].[All Stores]",			
 				" +[Time].[1997].[Q1]         ,+[Store].[All Stores]",			
 				" +[Time].[1997].[Q2]         ,-[Store].[All Stores]",			
 				" +[Time].[1997].[Q2]         , +[Store].[Mexico]",			
 				" +[Time].[1997].[Q2]         , +[Store].[USA]",			
 				" +[Time].[1997].[Q3]         ,+[Store].[All Stores]",			
 				" +[Time].[1997].[Q4]         ,+[Store].[All Stores]",			
 				"+[Time].[1998]               ,-[Store].[All Stores]",			
 				"+[Time].[1998]               , +[Store].[Mexico]",			
 				"+[Time].[1998]               , +[Store].[USA]"			
 				);
 		
 		// Drills Q2 in USA
 		Member mUSA = allStores.getChildMembers().get("USA");
 		testAxis.drill(m1997_Q2, mUSA);
 		assertRowsMembers(
 				"-[Time].[1997]               ,+[Store].[All Stores]",			
 				" +[Time].[1997].[Q1]         ,+[Store].[All Stores]",			
 				" +[Time].[1997].[Q2]         ,-[Store].[All Stores]",			
 				" +[Time].[1997].[Q2]         , +[Store].[Mexico]",			
 				" +[Time].[1997].[Q2]         , -[Store].[USA]",			
 				" +[Time].[1997].[Q2]         ,  +[Store].[USA].[CA]",			
 				" +[Time].[1997].[Q2]         ,  +[Store].[USA].[OR]",			
 				" +[Time].[1997].[Q2]         ,  +[Store].[USA].[WA]",			
 				" +[Time].[1997].[Q3]         ,+[Store].[All Stores]",			
 				" +[Time].[1997].[Q4]         ,+[Store].[All Stores]",			
 				"+[Time].[1998]               ,-[Store].[All Stores]",			
 				"+[Time].[1998]               , +[Store].[Mexico]",			
 				"+[Time].[1998]               , +[Store].[USA]"			
 				);
 
 		// Removes USA states from the query hierarchy
 		// State members are replaced by its children
 		testHierarchies[1].exclude(Operator.CHILDREN, mUSA);
 		assertRowsMembers(
 				"-[Time].[1997]               ,+[Store].[All Stores]",			
 				" +[Time].[1997].[Q1]         ,+[Store].[All Stores]",			
 				" +[Time].[1997].[Q2]         ,-[Store].[All Stores]",			
 				" +[Time].[1997].[Q2]         , +[Store].[Mexico]",			
 				" +[Time].[1997].[Q2]         , -[Store].[USA]",			
 //				" +[Time].[1997].[Q2]         ,  +[Store].[USA].[CA].[Alameda]", TODO Check why Alameda doesn't shows up 			
 				" +[Time].[1997].[Q2]         ,  +[Store].[USA].[CA].[Beverly Hills]",			
 				" +[Time].[1997].[Q2]         ,  +[Store].[USA].[CA].[Los Angeles]",			
 				" +[Time].[1997].[Q2]         ,  +[Store].[USA].[CA].[San Diego]",			
 				" +[Time].[1997].[Q2]         ,  +[Store].[USA].[CA].[San Francisco]",			
 				" +[Time].[1997].[Q2]         ,  +[Store].[USA].[OR].[Portland]",			
 				" +[Time].[1997].[Q2]         ,  +[Store].[USA].[OR].[Salem]",			
 				" +[Time].[1997].[Q2]         ,  +[Store].[USA].[WA].[Bellingham]",			
 				" +[Time].[1997].[Q2]         ,  +[Store].[USA].[WA].[Bremerton]",			
 				" +[Time].[1997].[Q2]         ,  +[Store].[USA].[WA].[Seattle]",			
 				" +[Time].[1997].[Q2]         ,  +[Store].[USA].[WA].[Spokane]",			
 				" +[Time].[1997].[Q2]         ,  +[Store].[USA].[WA].[Tacoma]",			
 				" +[Time].[1997].[Q2]         ,  +[Store].[USA].[WA].[Walla Walla]",			
 				" +[Time].[1997].[Q2]         ,  +[Store].[USA].[WA].[Yakima]",			
 				" +[Time].[1997].[Q3]         ,+[Store].[All Stores]",			
 				" +[Time].[1997].[Q4]         ,+[Store].[All Stores]",			
 				"+[Time].[1998]               ,-[Store].[All Stores]",			
 				"+[Time].[1998]               , +[Store].[Mexico]",			
 				"+[Time].[1998]               , +[Store].[USA]"			
 				);
 	}
 	
 	@Test
 	public void testSingleHierarchy_expanded() throws Exception {
 		// Adds another hierarchy to the test axis to avoid a Mondrian bug.
 		// Comment out this lines to reproduce the bug.
 		QueryHierarchy workaroundHierarchy = query.getHierarchy("Store Type");
 		workaroundHierarchy.include(Operator.MEMBER, workaroundHierarchy.getHierarchy().getRootMembers().get(0));
 		testAxis.addHierarchy(workaroundHierarchy);
 		testAxis.expandHierarchy(testHierarchies[0]);
 		
 		// Initial state for a QueryHierarchy: no member included. 
 		assertRowsMembers();
 
 		// Inclusion of DESCENDANTS of [Time].[1997].
 		// The hierarchy is expanded, so we get full hierarchy.
 		Member root1997 = testHierarchies[0].getHierarchy().getRootMembers().get("1997");
		Member root1998 = testHierarchies[0].getHierarchy().getRootMembers().get("1998");
 		testHierarchies[0].include(Operator.DESCENDANTS, root1997);
 		assertRowsMembers(
 				"-[Time].[1997]",
 				" -[Time].[1997].[Q1]",
 				"   [Time].[1997].[Q1].[1]",
 				"   [Time].[1997].[Q1].[2]",
 				"   [Time].[1997].[Q1].[3]",
 				" -[Time].[1997].[Q2]",
 				"   [Time].[1997].[Q2].[4]",
 				"   [Time].[1997].[Q2].[5]",
 				"   [Time].[1997].[Q2].[6]",
 				" -[Time].[1997].[Q3]",
 				"   [Time].[1997].[Q3].[7]",
 				"   [Time].[1997].[Q3].[8]",
 				"   [Time].[1997].[Q3].[9]",
 				" -[Time].[1997].[Q4]",
 				"   [Time].[1997].[Q4].[10]",
 				"   [Time].[1997].[Q4].[11]",
 				"   [Time].[1997].[Q4].[12]");
 
 		
 		// Then we undrill [Time].[1997].[Q3]
 		Member m1997_Q3 = root1997.getChildMembers().get(2);
 		testAxis.undrill(m1997_Q3);
 		assertRowsMembers(
 				"-[Time].[1997]",
 				" -[Time].[1997].[Q1]",
 				"   [Time].[1997].[Q1].[1]",
 				"   [Time].[1997].[Q1].[2]",
 				"   [Time].[1997].[Q1].[3]",
 				" -[Time].[1997].[Q2]",
 				"   [Time].[1997].[Q2].[4]",
 				"   [Time].[1997].[Q2].[5]",
 				"   [Time].[1997].[Q2].[6]",
 				" +[Time].[1997].[Q3]",
 				" -[Time].[1997].[Q4]",
 				"   [Time].[1997].[Q4].[10]",
 				"   [Time].[1997].[Q4].[11]",
 				"   [Time].[1997].[Q4].[12]");
 		
 		// After removing [Time].[1997].[Q3] from the query hierarchy, its descendants show up
 		testHierarchies[0].exclude(Operator.MEMBER, m1997_Q3);
 		assertRowsMembers(
 				"-[Time].[1997]",
 				" -[Time].[1997].[Q1]",
 				"   [Time].[1997].[Q1].[1]",
 				"   [Time].[1997].[Q1].[2]",
 				"   [Time].[1997].[Q1].[3]",
 				" -[Time].[1997].[Q2]",
 				"   [Time].[1997].[Q2].[4]",
 				"   [Time].[1997].[Q2].[5]",
 				"   [Time].[1997].[Q2].[6]",
 				"   [Time].[1997].[Q3].[7]",
 				"   [Time].[1997].[Q3].[8]",
 				"   [Time].[1997].[Q3].[9]",
 				" -[Time].[1997].[Q4]",
 				"   [Time].[1997].[Q4].[10]",
 				"   [Time].[1997].[Q4].[11]",
 				"   [Time].[1997].[Q4].[12]");
 
 		// Undrill [Time].[1997].[Q2]
 		testAxis.undrill(root1997.getChildMembers().get("Q2"));
 		assertRowsMembers(
 				"-[Time].[1997]",
 				" -[Time].[1997].[Q1]",
 				"   [Time].[1997].[Q1].[1]",
 				"   [Time].[1997].[Q1].[2]",
 				"   [Time].[1997].[Q1].[3]",
 				" +[Time].[1997].[Q2]",
 				"   [Time].[1997].[Q3].[7]",
 				"   [Time].[1997].[Q3].[8]",
 				"   [Time].[1997].[Q3].[9]",
 				" -[Time].[1997].[Q4]",
 				"   [Time].[1997].[Q4].[10]",
 				"   [Time].[1997].[Q4].[11]",
 				"   [Time].[1997].[Q4].[12]");
 
 		// Undrill [Time].[1997]
 		testAxis.undrill(root1997);
 		assertRowsMembers(
 				"+[Time].[1997]");
 
 
 		// Drilling down [Time].[1997] again keeps previous undrills
 		testAxis.drill(root1997);
 		assertRowsMembers(
 				"-[Time].[1997]",
 				" -[Time].[1997].[Q1]",
 				"   [Time].[1997].[Q1].[1]",
 				"   [Time].[1997].[Q1].[2]",
 				"   [Time].[1997].[Q1].[3]",
 				" +[Time].[1997].[Q2]",
 				"   [Time].[1997].[Q3].[7]",
 				"   [Time].[1997].[Q3].[8]",
 				"   [Time].[1997].[Q3].[9]",
 				" -[Time].[1997].[Q4]",
 				"   [Time].[1997].[Q4].[10]",
 				"   [Time].[1997].[Q4].[11]",
 				"   [Time].[1997].[Q4].[12]");
 				
 	}
 	
 	
 	private void assertRowsMembers(String... expectedPositions) throws Exception{
 		CellSet cs = query.execute();
 		CellSetAxis cellSetAxis = cs.getAxes().get(Axis.ROWS.axisOrdinal());
 		int rowsMemberCount = cellSetAxis.getPositionCount();
 		
 		assertEquals(expectedPositions.length, rowsMemberCount);
 
 		int i = 0;
 		for(Position pos : cellSetAxis.getPositions()) {
 			Member[] actualMembers = pos.getMembers().toArray(new Member[pos.getMembers().size()]);
 			ExpectedMemberInfo[] expectedMembers = ExpectedMemberInfo.parse(expectedPositions[i]);
 			int j = 0;
 			for(ExpectedMemberInfo expectedMember : expectedMembers) {
 				Member actualMember = pos.getMembers().get(j);
 				 
 				assertEquals(
 						String.format("Line %1$s, Column %2$s: Unexpected member %3$s, expected %4$s", i, j, actualMember.getUniqueName(), expectedMember.getMemberName()),
 						expectedMember.getMemberName(), 
 						actualMember.getUniqueName());
 				assertEquals(
 						String.format("Line %1$s: Member %2$s was expected %3$s to be a leaf", i, expectedMember.getMemberName(), expectedMember.isLeaf() ? "" : "not"),
 						expectedMember.isLeaf(), 
 						testHierarchies[j].isLeaf(actualMember));
 				if ( !expectedMember.isLeaf() ) {
 				assertEquals(
 						String.format("Line %1$s, Column %2$s: Member %3$s was expected to be %4$s drilled", i, j, 
 								expectedMember.getMemberName(), expectedMember.isDrilled() ? "" : "not"),
 						expectedMember.isDrilled(), 
 						testAxis.isDrilled(Arrays.copyOfRange(actualMembers, 0, j+1)));
 				}
 				++j;
 			}
 			++i;
 		}
 	}
 	
 	
 }
 
