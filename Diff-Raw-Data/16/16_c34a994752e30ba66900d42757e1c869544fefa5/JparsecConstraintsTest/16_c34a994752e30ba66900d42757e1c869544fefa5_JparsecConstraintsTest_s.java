 package com.lithium.ldn.starql.parsers;
 
 import static com.lithium.ldn.starql.models.QlConstraintOperator.EQUALS;
 import static com.lithium.ldn.starql.models.QlConstraintOperator.GREATER_THAN;
 import static com.lithium.ldn.starql.models.QlConstraintOperator.LESS_THAN;
 import static com.lithium.ldn.starql.models.QlConstraintOperator.NOT_EQUALS;
 import static com.lithium.ldn.starql.models.QlConstraintPairOperator.AND;
 import static com.lithium.ldn.starql.models.QlConstraintPairOperator.OR;
 import static org.junit.Assert.assertEquals;
 
 import org.codehaus.jparsec.error.ParserException;
 import org.joda.time.DateTime;
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.lithium.ldn.starql.exceptions.InvalidQueryException;
 import com.lithium.ldn.starql.exceptions.QueryValidationException;
 import com.lithium.ldn.starql.models.QlBooleanConstraintNode;
 import com.lithium.ldn.starql.models.QlConstraintPair;
 
 public class JparsecConstraintsTest extends JparsecTest {
 
 	@Test(expected = ParserException.class)
 	public final void test_constraints() {
 			String word = "";
 			inst.constraintsParser().parse(word);
 			Assert.fail("Constraints cannot be blank");
 	}
 	
 	@Test(expected = InvalidQueryException.class)
 	public final void test_constraints_a() throws InvalidQueryException, QueryValidationException {
 			String word = "";
 			inst.parseQlConstraintsClause(word);
 			Assert.fail("Constraints cannot be blank");
 	}
 	
 	@Test(expected = InvalidQueryException.class)
 	public final void test_constraints_b() throws InvalidQueryException, QueryValidationException {
 			String word = "WHERE id = 5";
 			inst.parseQlConstraintsClause(word);
 			Assert.fail("Constraints clause cannot begin with the \"WHERE\" keyword.");
 	}
 	
 	@Test(expected = InvalidQueryException.class)
 	public final void test_constraints_c() throws InvalidQueryException, QueryValidationException {
 			String word = "distinct id = 5 AND name = 'david'";
 			inst.parseQlConstraintsClause(word);
 			Assert.fail("Qualifiers cannot exist in the WHERE clause");
 	}
 	
 	@Test(expected = InvalidQueryException.class)
 	public final void test_constraints_d() throws InvalidQueryException, QueryValidationException {
 		String word = "id = 5 AND distinct name = 'david'";
 			inst.parseQlConstraintsClause(word);
 			Assert.fail("Qualifiers cannot exist in the WHERE clause");
 	}
 
 	@Test
 	public final void test_constraints1() {
 		QlBooleanConstraintNode expected = getStringConstraint("email", "david.esposito@lithium.com", NOT_EQUALS);
 		runConstraintsTest("email!='david.esposito@lithium.com'", expected);
 	}
 
 	@Test
 	public final void test_constraints2() {
 		QlBooleanConstraintNode expected = getNumberConstraint("age", 25, GREATER_THAN);
 		runConstraintsTest("age>25", expected);
 	}
 
 	@Test
 	public final void test_constraints2a() {
 		DateTime date = new DateTime();
 		QlBooleanConstraintNode expected = getDateConstraint("date", date, GREATER_THAN);
 		runConstraintsTest("date>" + getFormattedDateString(date), expected);
 	}
 
 	@Test
 	public final void test_constraints2b() {
 		DateTime date = new DateTime();
 		QlBooleanConstraintNode expected = getDateConstraint("date", date, LESS_THAN);
 		runConstraintsTest("date<" + getFormattedDateString(date), expected);
 	}
 
 	@Test
 	public final void test_constraints3() {
 		QlBooleanConstraintNode expected = new QlConstraintPair(
 				getNumberConstraint("a", 5, EQUALS), 
 				getStringConstraint("b", "c", EQUALS), 
 				AND);
 		runConstraintsTest("a=5 AND b='c'", expected);
 	}
 
 	@Test
 	public final void test_constraints4() {
 		QlBooleanConstraintNode temp = new QlConstraintPair(
 				getNumberConstraint("a", 5, EQUALS), 
 				getStringConstraint("b", "c", EQUALS),
 				AND);
 		QlBooleanConstraintNode expected = new QlConstraintPair(
 				temp, 
 				getNumberConstraint("d", 18, EQUALS), 
 				AND);
 		runConstraintsTest("a=5 AND b='c' AND d=18", expected);
 	}
 
 	@Test
 	public final void test_constraints5() {
 		QlBooleanConstraintNode temp = new QlConstraintPair(
 				getNumberConstraint("a", 5, EQUALS), 
 				getStringConstraint("b", "cdef", EQUALS),
 				AND);
 		QlBooleanConstraintNode expected = new QlConstraintPair(
 				temp, 
 				getNumberConstraint("g", 18, EQUALS), 
 				OR);
 		runConstraintsTest("a=5 AND b='cdef' OR g=18", expected);
 	}
 
 	@Test
 	public final void test_constraints6() {
 		QlBooleanConstraintNode temp1 = new QlConstraintPair(
 				getNumberConstraint("a", 5, EQUALS), 
 				getStringConstraint("b", "c", EQUALS),
 				AND);
 		QlBooleanConstraintNode temp2 = new QlConstraintPair(
 				temp1, 
 				getNumberConstraint("d", 18, EQUALS),
 				OR);
 		QlBooleanConstraintNode temp3 = new QlConstraintPair(
 				temp2, 
 				getNumberConstraint("w", 5, EQUALS),
 				AND);
 		QlBooleanConstraintNode temp4 = new QlConstraintPair(
 				temp3, 
 				getStringConstraint("x", "y", EQUALS),
 				AND);
 		QlBooleanConstraintNode expected = new QlConstraintPair(
 				temp4, 
 				getNumberConstraint("z", 18, EQUALS), 
 				OR);
 		runConstraintsTest("a=5 AND b='c' OR d=18 AND w=5 AND x='y' OR z=18", expected);
 	}
 	
 	private final void runConstraintsTest(String query, QlBooleanConstraintNode expected) {
 		// Test the actual parser
 		QlBooleanConstraintNode actual = inst.constraintsParser().parse(query);
 		assertEquals(expected, actual);
 		try {
 			// Test the public end point in the StarQL API
 			actual = inst.parseQlConstraintsClause(query).getRoot();
 			assertEquals(expected, actual);
 		} catch (InvalidQueryException e) {
 			e.printStackTrace();
 			Assert.fail();
		} catch (QueryValidationException e) {
			e.printStackTrace();
			Assert.fail();
 		}
 	}
 }
