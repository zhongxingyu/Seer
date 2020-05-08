 /*
  * JparsecQlParserTest.java
  * Created on Jul 16, 2013
  *
  * Copyright 2013 Lithium Technologies, Inc. 
  * Emeryville, California, U.S.A.  All Rights Reserved.
  *
  * This software is the  confidential and proprietary information
  * of  Lithium  Technologies,  Inc.  ("Confidential Information")
  * You shall not disclose such Confidential Information and shall 
  * use  it  only in  accordance  with  the terms of  the  license 
  * agreement you entered into with Lithium.
  */
 
 package com.lithium.ldn.starql.parsers;
 
 import static com.lithium.ldn.starql.models.QlConstraintOperator.EQUALS;
 import static com.lithium.ldn.starql.models.QlConstraintOperator.GREATER_THAN_EQUAL;
 import static com.lithium.ldn.starql.models.QlConstraintOperator.LESS_THAN;
 import static com.lithium.ldn.starql.models.QlConstraintOperator.LESS_THAN_EQUAL;
 import static com.lithium.ldn.starql.models.QlConstraintOperator.NOT_EQUALS;
 import static com.lithium.ldn.starql.models.QlConstraintPairOperator.AND;
 import static com.lithium.ldn.starql.models.QlConstraintPairOperator.OR;
 import static com.lithium.ldn.starql.models.QlSortOrderType.ASC;
 import static com.lithium.ldn.starql.models.QlSortOrderType.DESC;
 import static org.junit.Assert.assertEquals;
 
 import java.util.Date;
 import java.util.List;
 
 import org.codehaus.jparsec.error.ParserException;
 import org.joda.time.DateTime;
 import org.junit.Assert;
 import org.junit.Test;
 
 import com.google.common.collect.Lists;
 import com.lithium.ldn.starql.exceptions.InvalidQueryException;
 import com.lithium.ldn.starql.exceptions.QueryValidationException;
 import com.lithium.ldn.starql.models.QlBooleanConstraintNode;
 import com.lithium.ldn.starql.models.QlConstraint;
 import com.lithium.ldn.starql.models.QlConstraintOperator;
 import com.lithium.ldn.starql.models.QlConstraintPair;
 import com.lithium.ldn.starql.models.QlConstraintValueNumber;
 import com.lithium.ldn.starql.models.QlField;
 import com.lithium.ldn.starql.models.QlPageConstraints;
 import com.lithium.ldn.starql.models.QlSelectStatement;
 import com.lithium.ldn.starql.models.QlSortClause;
 
 /**
  * @author David Esposito
  */
 
 public class JparsecSelectStatementTest extends JparsecTest {
 
 	/*
 	 * ===================================================
 	 * 		*QL SELECT STATEMENTS
 	 * ===================================================
 	 */
 	@Test(expected = ParserException.class)
 	public final void test_qlSelectStatement() {
 			String word = "";
 			inst.qlSelectParser().parse(word);
 			Assert.fail("*QL SelectStatement cannot be blank");
 	}
 
 	@Test
 	public final void test_qlSelectStatement_simple() {
 		String query = "SELECT * FROM users";
 		List<QlField> fields = getFields();
 		String table = "users";
 		QlBooleanConstraintNode constraints = null;
 		List<QlSortClause> sortConstraint = Lists.newArrayList();
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraints(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 
 	@Test
 	public final void test_qlSelectStatement_simple1() {
 		String query = "select * from users";
 		List<QlField> fields = getFields();
 		String table = "users";
 		QlBooleanConstraintNode constraints = null;
 		List<QlSortClause> sortConstraint = Lists.newArrayList();
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraints(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 	
 	private List<QlField> getFields(String...fieldNames) {
 		if (fieldNames.length == 0) {
 			return Lists.newArrayList(QlField.create("*"));
 		}
 		List<QlField> fields = Lists.newArrayList();
 		for (String name : fieldNames) {
 			fields.add(QlField.create(name));
 		}
 		return fields;
 	}
 	
 	private List<QlField> getFields(String[] fieldNames, String[] fieldQualifiers) {
 		if (fieldNames.length == 0) {
 			return Lists.newArrayList(QlField.create("*"));
 		}
 		List<QlField> fields = Lists.newArrayList();
 		for (int i=0;i<fieldNames.length;i++) {
 			String name = fieldNames[i];
 			String qualifier = fieldQualifiers[i];
 			fields.add(QlField.create(qualifier, name));
 		}
 		return fields;
 	}
 
 	@Test
 	public final void test_qlSelectStatement_fields() {
 		String query = "SELECT uid,login, email,firstName FROM users";
 		List<QlField> fields = getFields("uid", "login", "email", "firstName");
 		String table = "users";
 		QlBooleanConstraintNode constraints = null;
 		List<QlSortClause> sortConstraint = Lists.newArrayList();
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraints(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 	
 	/*
 	 * ===================================================
 	 * 		SORTED *QL SELECT STATEMENTS
 	 * ===================================================
 	 */
 	@Test(expected = InvalidQueryException.class)
 	public final void test_qlSelectStatementOrderByDistinctFailure() throws InvalidQueryException, 
 			QueryValidationException {
 		String query = "SELECT * FROM users ORDER BY distinct date ASC";
 		inst.parseQlSelect(query);
 		Assert.fail("The distinc keyword cannot be used in the ORDER BY clause.");
 	}
 	
 	@Test
 	public final void test_qlSelectStatement_simpleOrderBy() {
 		String query = "SELECT * FROM users ORDER BY date ASC";
 		List<QlField> fields = getFields();
 		String table = "users";
 		QlBooleanConstraintNode constraints = null;
 		List<QlSortClause> sortConstraint = Lists.newArrayList(getSortClause("date", ASC));
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraints(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 
 	@Test
 	public final void test_qlSelectStatement_fieldsOrderBy() {
 		String query = "SELECT uid,login, email,firstName FROM users ORDER BY email DESC";
 		List<QlField> fields = getFields("uid", "login", "email", "firstName");
 		String table = "users";
 		QlBooleanConstraintNode constraints = null;
 		List<QlSortClause> sortConstraint = Lists.newArrayList(getSortClause("email", DESC));
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraints(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 
 	@Test
 	public final void test_qlSelectStatement_fieldsOrderBy1() {
 		String query = "SELECT DISTINCT uid,login, email,firstName FROM users ORDER BY email DESC";
 		List<QlField> fields = getFields(new String[]{"uid", "login", "email", "firstName"},
 				new String[]{"DISTINCT", null, null, null});
 		String table = "users";
 		QlBooleanConstraintNode constraints = null;
 		List<QlSortClause> sortConstraint = Lists.newArrayList(getSortClause("email", DESC));
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraints(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 
 	@Test
 	public final void test_qlSelectStatement_fieldsOrderBy2() {
 		String query = "SELECT uid,login, email,DISTINCT firstName FROM users ORDER BY email DESC";
 		List<QlField> fields = getFields(new String[]{"uid", "login", "email", "firstName"},
 				new String[]{null, null, null, "DISTINCT"});
 		String table = "users";
 		QlBooleanConstraintNode constraints = null;
 		List<QlSortClause> sortConstraint = Lists.newArrayList(getSortClause("email", DESC));
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraints(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 
 	/*
 	 * ===================================================
 	 * 		*QL SELECT STATEMENTS WITH LIMIT/OFFSET
 	 * ===================================================
 	 */
 	@Test
 	public final void test_qlSelectStatement_simpleLimit() {
 		String query = "SELECT * FROM users LIMIT 10";
 		List<QlField> fields = getFields();
 		String table = "users";
 		QlBooleanConstraintNode constraints = null;
 		List<QlSortClause> sortConstraint = Lists.newArrayList();
 		QlPageConstraints pageConstraints = defaultLimit;
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraints(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 
 	@Test
 	public final void test_qlSelectStatement_fieldsLimitOffset() {
 		String query = "SELECT uid,login, email,firstName FROM users LIMIT 30 OFFSET 10";
 		List<QlField> fields = getFields("uid", "login", "email", "firstName");
 		String table = "users";
 		QlBooleanConstraintNode constraints = null;
 		List<QlSortClause> sortConstraint = Lists.newArrayList();
 		QlPageConstraints pageConstraints = new QlPageConstraints(30, 10);
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraints(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 
 	@Test
 	public final void test_qlSelectStatement_simpleSortLimit() {
 		String query = "SELECT * FROM users ORDER BY date ASC LIMIT 100";
 		List<QlField> fields = getFields();
 		String table = "users";
 		QlBooleanConstraintNode constraints = null;
 		List<QlSortClause> sortConstraint = Lists.newArrayList(getSortClause("date", ASC));
 		QlPageConstraints pageConstraints = new QlPageConstraints(100, -1);
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraints(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 
 	@Test
 	public final void test_qlSelectStatement_fieldsSortLimitOffset() {
 		String query = "SELECT uid,login, email,firstName FROM users ORDER BY email DESC LIMIT 100 OFFSET 1000";
 		List<QlField> fields = getFields("uid", "login", "email", "firstName");
 		String table = "users";
 		QlBooleanConstraintNode constraints = null;
 		List<QlSortClause> sortConstraint = Lists.newArrayList(getSortClause("email", DESC));
 		QlPageConstraints pageConstraints = new QlPageConstraints(100, 1000);
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraints(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 	
 	/*
 	 * ===================================================
 	 * 		*QL SELECT STATEMENTS WITH BOOLEAN CONSTRAINTS
 	 * ===================================================
 	 */
 	@Test
 	public final void test_qlSelectStatement_simpleSingleWhere() {
 		String query = "SELECT * FROM users WHERE email='david.esposito@lithium.com'";
 		List<QlField> fields = getFields();
 		String table = "users";
 		QlBooleanConstraintNode constraints = getStringConstraint("email", "david.esposito@lithium.com", EQUALS);
 		List<QlSortClause> sortConstraint = Lists.newArrayList();		
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraints(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 
 	@Test
 	public final void test_qlSelectStatement_fieldsTwoWhere() {
 		String query = "SELECT uid,login, email,firstName FROM users WHERE name='david' AND age<50";
 		List<QlField> fields = getFields("uid", "login", "email", "firstName");
 		String table = "users";
 		QlBooleanConstraintNode constraints = new QlConstraintPair(
 				getStringConstraint("name", "david", EQUALS),
 				getNumberConstraint("age", 50, LESS_THAN),
 				AND);
 		List<QlSortClause> sortConstraint = Lists.newArrayList();
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraints(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 
 	@Test
 	public final void test_qlSelectStatement_simpleDateWhere() {
 		DateTime start = new DateTime(new Date().getTime() - (60 * 60 * 24));
 		DateTime end = new DateTime();
 		String startString = getFormattedDateString(start);
 		String endString = getFormattedDateString(end);
 		String query = String.format("SELECT * FROM users WHERE date >= %1$s AND date <= %2$s", 
 				startString, endString);
 		List<QlField> fields = getFields();
 		String table = "users";
 		QlBooleanConstraintNode constraints = new QlConstraintPair(
 				getDateConstraint("date", start, GREATER_THAN_EQUAL),
 				getDateConstraint("date", end, LESS_THAN_EQUAL),
 				AND);
 		List<QlSortClause> sortConstraint = Lists.newArrayList();
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraints(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		try {
 			QlSelectStatement parseQlSelect = inst.parseQlSelect(query);
 			assertEquals(expected, parseQlSelect);
 		} catch (InvalidQueryException e) {
 			Assert.fail(e.getMessage() + "\n" + e.getStackTrace());
 		}
 	}
 
 	@Test
 	public final void test_qlSelectStatement_fieldsDateWhere() {
 		DateTime start = new DateTime(new Date().getTime() - (60 * 60 * 24));
 		DateTime end = new DateTime();
 		String startString = getFormattedDateString(start);
 		String endString = getFormattedDateString(end);
 		String query = String.format("SELECT uid,login, email,firstName FROM users WHERE registerDate >= %1$s AND registerDate <= %2$s", 
 				startString, endString);
 		List<QlField> fields = getFields("uid", "login", "email", "firstName");
 		String table = "users";
 		QlBooleanConstraintNode constraints = new QlConstraintPair(
 				getDateConstraint("registerDate", start, GREATER_THAN_EQUAL),
 				getDateConstraint("registerDate", end, LESS_THAN_EQUAL),
 				AND);
 		List<QlSortClause> sortConstraint = Lists.newArrayList();
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraints(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		try {
 			QlSelectStatement parseQlSelect = inst.parseQlSelect(query);
 			assertEquals(expected, parseQlSelect);
 		} catch (InvalidQueryException e) {
 			Assert.fail(e.getMessage() + "\n" + e.getStackTrace());
 		}
 	}
 	
 	@Test
 	public final void test_qlSelectStatement_simpleSingleWhereSort() {
 		String query = "SELECT * FROM users WHERE email='david.esposito@lithium.com' ORDER BY date ASC";
 		List<QlField> fields = getFields();
 		String table = "users";
 		QlBooleanConstraintNode constraints = getStringConstraint("email", "david.esposito@lithium.com", EQUALS);
 		List<QlSortClause> sortConstraint = Lists.newArrayList(getSortClause("date", ASC));
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraints(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 	
 	@Test
 	public final void test_qlSelectStatement_fieldsTwoWhereSort() {
 		String query = "SELECT uid,login, email,firstName FROM users WHERE name='david' AND age<50 ORDER BY email DESC";
 		List<QlField> fields = getFields("uid", "login", "email", "firstName");
 		String table = "users";
 		QlBooleanConstraintNode constraints = new QlConstraintPair(
 				getStringConstraint("name", "david", EQUALS),
 				getNumberConstraint("age", 50, LESS_THAN),
 				AND);
 		List<QlSortClause> sortConstraint = Lists.newArrayList(getSortClause("email", DESC));
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraints(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 	
 	@Test
 	public final void test_qlSelectStatement_fieldsTwoWhereSort1() {
 		String query = "SELECT uid,login, email,firstName FROM users WHERE name='david' AND age<50 ORDER BY email DESC, login ASC";
 		List<QlField> fields = getFields("uid", "login", "email", "firstName");
 		String table = "users";
 		QlBooleanConstraintNode constraints = new QlConstraintPair(
 				getStringConstraint("name", "david", EQUALS),
 				getNumberConstraint("age", 50, LESS_THAN),
 				AND);
 		List<QlSortClause> sortConstraint = Lists.newArrayList(getSortClause("email", DESC),
 				getSortClause("login", ASC));
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraints(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 	
 	@Test
 	public final void test_qlSelectStatement_fieldsTwoWhereSort2() {
 		String query = "SELECT uid,login, email,firstName FROM users WHERE name='david' AND age<50 ORDER BY email DESC, login ASC, registration_date desc";
 		List<QlField> fields = getFields("uid", "login", "email", "firstName");
 		String table = "users";
 		QlBooleanConstraintNode constraints = new QlConstraintPair(
 				getStringConstraint("name", "david", EQUALS),
 				getNumberConstraint("age", 50, LESS_THAN),
 				AND);
 		List<QlSortClause> sortConstraint = Lists.newArrayList(getSortClause("email", DESC),
 				getSortClause("login", ASC), getSortClause("registration_date", DESC));
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraints(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 
 	@Test
 	public final void test_qlSelectStatement_fieldsTwoWhereLimit() {
 		String query = "SELECT uid,login, email,firstName FROM users WHERE name='david' AND age<50 LIMIT 50";
 		List<QlField> fields = getFields("uid", "login", "email", "firstName");
 		String table = "users";
 		QlBooleanConstraintNode constraints = new QlConstraintPair(
 				getStringConstraint("name", "david", EQUALS),
 				getNumberConstraint("age", 50, LESS_THAN),
 				AND);
 		List<QlSortClause> sortConstraint = Lists.newArrayList();
 		QlPageConstraints pageConstraints = new QlPageConstraints(50, -1);
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraints(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 
 	@Test
 	public final void test_qlSelectStatement_simpleThreeWhereLimit() {
 		String query = "SELECT * FROM users WHERE name='david' AND age<50 OR login!='davidE' LIMIT 10";
 		List<QlField> fields = getFields();
 		String table = "users";
 		QlBooleanConstraintNode constraints = new QlConstraintPair(
 				new QlConstraintPair(
 						getStringConstraint("name", "david", EQUALS), 
 						getNumberConstraint("age", 50, LESS_THAN), 
 						AND), 
 				getStringConstraint("login", "davidE", NOT_EQUALS), 
 				OR);
 		List<QlSortClause> sortConstraint = Lists.newArrayList();
 		QlPageConstraints pageConstraints = new QlPageConstraints(10, -1);
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraints(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 
 	@Test
 	public final void test_qlSelectStatement_fieldsThreeLimitOffset() {
 		String query = "SELECT uid,login, email,firstName FROM users WHERE name='david' AND age<50 OR login!='davidE' LIMIT 30 OFFSET 10";
 		List<QlField> fields = getFields("uid", "login", "email", "firstName");
 		String table = "users";
 		QlBooleanConstraintNode constraints = new QlConstraintPair(
 				new QlConstraintPair(
 						getStringConstraint("name", "david", EQUALS), 
 						getNumberConstraint("age", 50, LESS_THAN), 
 						AND), 
 				getStringConstraint("login", "davidE", NOT_EQUALS), 
 				OR);
 		List<QlSortClause> sortConstraint = Lists.newArrayList();
 		QlPageConstraints pageConstraints = new QlPageConstraints(30, 10);
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraints(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 	
 	@Test
 	public final void test_qlSelectStatement_fieldsThreeSort() {
 		String query = "SELECT uid,login, email,firstName FROM users WHERE name='david' AND age<50 OR login!='davidE' ORDER BY age DESC";
 		List<QlField> fields = getFields("uid", "login", "email", "firstName");
 		String table = "users";
 		QlBooleanConstraintNode constraints = new QlConstraintPair(
 				new QlConstraintPair(
 						getStringConstraint("name", "david", EQUALS), 
 						getNumberConstraint("age", 50, LESS_THAN), 
 						AND), 
 				getStringConstraint("login", "davidE", NOT_EQUALS), 
 				OR);
 		List<QlSortClause> sortConstraint = Lists.newArrayList(getSortClause("age", DESC));
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(fields)
 				.setCollection(table)
 				.setConstraints(constraints)
 				.setSortConstraints(sortConstraint)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 	
 	@Test
 	public final void test_qlSelectStatement_simpleFunctionInFields() {
 		String query = "SELECT count( * ) FROM messages";
 		QlField field = QlField.create(null, "count", QlField.create("*"), true);
 		String table = "messages";
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(Lists.newArrayList(field))
 				.setCollection(table)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 	
 	@Test
 	public final void test_qlSelectStatement_objectFunctionInFields() {
 		String query = "SELECT kudos.sum(weight) FROM messages";
 		QlField field = QlField.create(null, "kudos", QlField.create(null, "sum", QlField.create("weight"), true), false);
 		String table = "messages";
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(Lists.newArrayList(field))
 				.setCollection(table)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 	
 	@Test
 	public final void test_qlSelectStatement_functionInFunctionInFields() {
 		String query = "SELECT sum(kudos.count(*)) FROM messages";
 		QlField star = QlField.create("*");
 		QlField count = QlField.create(null, "count", star, true);
 		QlField kudos = QlField.create(null, "kudos", count, false);
 		QlField field = QlField.create(null, "sum", kudos, true);
 		
 		String table = "messages";
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 		
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(Lists.newArrayList(field))
 				.setCollection(table)
 				.setPageConstraints(pageConstraints)
 				.build();
 		verify(query, expected);
 	}
 	
 	@Test
 	public final void test_qlSelectStatement_FunctionInWhere() {
 		String query = "SELECT * FROM messages WHERE kudos.count(*) > 0";
 		QlField star = QlField.create("*");
 		QlField count = QlField.create(null, "count", star, true);
 		QlField kudos = QlField.create(null, "kudos", count, false);
 
 		QlBooleanConstraintNode constraint = new QlConstraint(kudos,
 				new QlConstraintValueNumber(0),
 				QlConstraintOperator.GREATER_THAN);
 		String table = "messages";
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(Lists.newArrayList(star))
 				.setCollection(table)
 				.setPageConstraints(pageConstraints)
 				.setConstraints(constraint)
 				.build();
 		verify(query, expected);
 	}
 	
 	@Test
 	public final void test_qlSelectStatement_FunctionInWhereAndOrderBy() {
 		String query = "SELECT * FROM messages WHERE kudos.count(*) > 0 ORDER BY kudos.count(*) DESC";
 		QlField star = QlField.create("*");
 		QlField count = QlField.create(null, "count", star, true);
 		QlField kudos = QlField.create(null, "kudos", count, false);
 
 		QlBooleanConstraintNode constraint = new QlConstraint(kudos,
 				new QlConstraintValueNumber(0),
 				QlConstraintOperator.GREATER_THAN);
 		List<QlSortClause> sortConstraint = Lists.newArrayList(new QlSortClause(kudos, DESC));
 		String table = "messages";
 		QlPageConstraints pageConstraints = QlPageConstraints.ALL;
 
 		QlSelectStatement expected = QlSelectStatement.builder()
 				.setFields(Lists.newArrayList(star))
 				.setCollection(table)
 				.setPageConstraints(pageConstraints)
 				.setConstraints(constraint)
 				.setSortConstraints(sortConstraint)
 				.build();
 		verify(query, expected);
 	}
 
 	private void verify(String query, QlSelectStatement expected) {
 		try {
 			assertEquals(expected, inst.parseQlSelect(query));
 		} catch (InvalidQueryException e) {
 			throw new RuntimeException(e);
 		}
 	}
 }
