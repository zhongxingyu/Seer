 package org.jpaqueryfier;
 
 import java.util.regex.Pattern;
 
 class SQLGrammar {
 
 	static final String SPACE = " ";
 	static final String OPTIONAL_SPACE = "\\s*";
 	static final String CLAUSE = "(where|WHERE|and|AND|or|OR)";
	static final String PARAMETER_NAME = "[a-zA-Z0-9_.]+";
 	static final String OPERATOR = "(=|<|>|<=|>=|is|IS|between|BETWEEN|like|LIKE)";
 	static final String PARAMETER_VALUE = ":([a-zA-Z][a-zA-Z0-9_]+)";
 	static final String PARAMETER_WITH_CLAUSE = CLAUSE + SPACE + PARAMETER_NAME + OPTIONAL_SPACE + OPERATOR
 			+ OPTIONAL_SPACE + PARAMETER_VALUE;
 	static final String PARAMETER_VALUE_WITH_CLAUSE = CLAUSE + SPACE + PARAMETER_VALUE;
 
 	static final Pattern PARAMETER_WITH_CLAUSE_PATTERN = Pattern.compile(PARAMETER_WITH_CLAUSE);
 	static final Pattern PARAMETER_VALUE_WITH_CLAUSE_PATTERN = Pattern.compile(PARAMETER_VALUE_WITH_CLAUSE);
 	static final Pattern PARAMETER_VALUE_PATTERN = Pattern.compile(PARAMETER_VALUE);
 
 }
