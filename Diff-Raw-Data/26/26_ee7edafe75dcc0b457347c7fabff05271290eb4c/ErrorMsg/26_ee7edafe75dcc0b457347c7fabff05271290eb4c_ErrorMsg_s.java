 /**
  * Licensed to the Apache Software Foundation (ASF) under one
  * or more contributor license agreements.  See the NOTICE file
  * distributed with this work for additional information
  * regarding copyright ownership.  The ASF licenses this file
  * to you under the Apache License, Version 2.0 (the
  * "License"); you may not use this file except in compliance
  * with the License.  You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package org.apache.hadoop.hive.ql.parse;
 
 import org.antlr.runtime.tree.*;
 
 import java.util.Map;
 import java.util.HashMap;
 import java.util.regex.Pattern;
 import java.util.regex.Matcher;
 
 /**
  * List of error messages thrown by the parser
  **/
 
 public enum ErrorMsg {
   //SQLStates are taken from Section 12.5 of ISO-9075.
   //See http://www.contrib.andrew.cmu.edu/~shadow/sql/sql1992.txt
   //Most will just rollup to the generic syntax error state of 42000, but
   //specific errors can override the that state.
   //See this page for how MySQL uses SQLState codes:
   //http://dev.mysql.com/doc/refman/5.0/en/connector-j-reference-error-sqlstates.html
 
   GENERIC_ERROR("Exception while processing"),
   INVALID_TABLE("Table not found", "42S02"),
   INVALID_COLUMN("Invalid Column Reference"),
   INVALID_TABLE_OR_COLUMN("Invalid Table Alias or Column Reference"),
   AMBIGUOUS_TABLE_OR_COLUMN("Ambiguous Table Alias or Column Reference"),
   INVALID_PARTITION("Partition not found"),
   AMBIGUOUS_COLUMN("Ambiguous Column Reference"),
   AMBIGUOUS_TABLE_ALIAS("Ambiguous Table Alias"),
   INVALID_TABLE_ALIAS("Invalid Table Alias"),
   NO_TABLE_ALIAS("No Table Alias"),
   INVALID_FUNCTION("Invalid Function"),
   INVALID_FUNCTION_SIGNATURE("Function Argument Type Mismatch"),
   INVALID_OPERATOR_SIGNATURE("Operator Argument Type Mismatch"),
   INVALID_ARGUMENT("Wrong Arguments"),
   INVALID_ARGUMENT_LENGTH("Arguments Length Mismatch", "21000"),
   INVALID_ARGUMENT_TYPE("Argument Type Mismatch"),
   INVALID_JOIN_CONDITION_1("Both Left and Right Aliases Encountered in Join"),
   INVALID_JOIN_CONDITION_2("Neither Left nor Right Aliases Encountered in Join"),
   INVALID_JOIN_CONDITION_3("OR not supported in Join currently"),
   INVALID_TRANSFORM("TRANSFORM with Other Select Columns not Supported"),
   DUPLICATE_GROUPBY_KEY("Repeated Key in Group By"),
   UNSUPPORTED_MULTIPLE_DISTINCTS("DISTINCT on Different Columns not Supported"),
   NO_SUBQUERY_ALIAS("No Alias For Subquery"),
   NO_INSERT_INSUBQUERY("Cannot insert in a Subquery. Inserting to table "),
   NON_KEY_EXPR_IN_GROUPBY("Expression Not In Group By Key"),
   INVALID_XPATH("General . and [] Operators are Not Supported"),
   INVALID_PATH("Invalid Path"),
   ILLEGAL_PATH("Path is not legal"),
   INVALID_NUMERICAL_CONSTANT("Invalid Numerical Constant"),
   INVALID_ARRAYINDEX_CONSTANT("Non Constant Expressions for Array Indexes not Supported"),
   INVALID_MAPINDEX_CONSTANT("Non Constant Expression for Map Indexes not Supported"),
   INVALID_MAPINDEX_TYPE("Map Key Type does not Match Index Expression Type"),
   NON_COLLECTION_TYPE("[] not Valid on Non Collection Types"),
   SELECT_DISTINCT_WITH_GROUPBY("SELECT DISTINCT and GROUP BY can not be in the same query"),
   COLUMN_REPEATED_IN_PARTITIONING_COLS("Column repeated in partitioning columns"),
   DUPLICATE_COLUMN_NAMES("Duplicate column names"),
   COLUMN_REPEATED_IN_CLUSTER_SORT("Same column cannot appear in cluster and sort by"),
   SAMPLE_RESTRICTION("Cannot Sample on More Than Two Columns"),
   SAMPLE_COLUMN_NOT_FOUND("Sample Column Not Found"),
   NO_PARTITION_PREDICATE("No Partition Predicate Found"),
   INVALID_DOT(". operator is only supported on struct or list of struct types"),
   INVALID_TBL_DDL_SERDE("Either list of columns or a custom serializer should be specified"),
   TARGET_TABLE_COLUMN_MISMATCH("Cannot insert into target table because column number/types are different"),
   TABLE_ALIAS_NOT_ALLOWED("Table Alias not Allowed in Sampling Clause"),
   CLUSTERBY_DISTRIBUTEBY_CONFLICT("Cannot have both Cluster By and Distribute By Clauses"),
   ORDERBY_DISTRIBUTEBY_CONFLICT("Cannot have both Order By and Distribute By Clauses"),
   CLUSTERBY_SORTBY_CONFLICT("Cannot have both Cluster By and Sort By Clauses"),
   ORDERBY_SORTBY_CONFLICT("Cannot have both Order By and Sort By Clauses"),
   CLUSTERBY_ORDERBY_CONFLICT("Cannot have both Cluster By and Order By Clauses"),
   NO_LIMIT_WITH_ORDERBY("In strict mode, limit must be specified if ORDER BY is present"),
   NO_CARTESIAN_PRODUCT("In strict mode, cartesian product is not allowed. If you really want to perform the operation, set hive.mapred.mode=nonstrict"),
   UNION_NOTIN_SUBQ("Top level Union is not supported currently; use a subquery for the union"),
   INVALID_INPUT_FORMAT_TYPE("Input Format must implement InputFormat"),
   INVALID_OUTPUT_FORMAT_TYPE("Output Format must implement HiveOutputFormat, otherwise it should be either IgnoreKeyTextOutputFormat or SequenceFileOutputFormat"),
   NO_VALID_PARTN("The query does not reference any valid partition. To run this query, set hive.mapred.mode=nonstrict"),
   NO_OUTER_MAPJOIN("Map Join cannot be performed with Outer join"),
   INVALID_MAPJOIN_HINT("neither table specified as map-table"),
   INVALID_MAPJOIN_TABLE("result of a union cannot be a map table"),
   NON_BUCKETED_TABLE("Sampling Expression Needed for Non-Bucketed Table"),
   NEED_PARTITION_ERROR("need to specify partition columns because the destination table is partitioned."),
   CTAS_CTLT_COEXISTENCE("Create table command does not allow LIKE and AS-SELECT in the same command"),
   CTAS_COLLST_COEXISTENCE("Create table as select command cannot specify the list of columns for the target table."),
   CTLT_COLLST_COEXISTENCE("Create table like command cannot specify the list of columns for the target table."),
   INVALID_SELECT_SCHEMA("Cannot derive schema from the select-clause."),
   CTAS_PARCOL_COEXISTENCE("CREATE-TABLE-AS-SELECT does not support partitioning in the target table."),
   CTAS_MULTI_LOADFILE("CREATE-TABLE-AS-SELECT results in multiple file load."),
   CTAS_EXTTBL_COEXISTENCE("CREATE-TABLE-AS-SELECT cannot create external table."),
  TABLE_ALREADY_EXISTS("Table already exists:", "42S02");
   
   private String mesg;
   private String SQLState;
 
   private static char SPACE = ' ';
   private static Pattern ERROR_MESSAGE_PATTERN = Pattern.compile(".*line [0-9]+:[0-9]+ (.*)");
   private static Map<String, ErrorMsg> mesgToErrorMsgMap = new HashMap<String, ErrorMsg>();
   private static int minMesgLength = -1;
 
   static {
     for (ErrorMsg errorMsg : values()) {
       mesgToErrorMsgMap.put(errorMsg.getMsg().trim(), errorMsg);
 
       int length = errorMsg.getMsg().trim().length();
       if (minMesgLength == -1 || length < minMesgLength)
         minMesgLength = length;
     }
   }
 
   /**
    * For a given error message string, searches for a <code>ErrorMsg</code>
    * enum that appears to be a match. If an match is found, returns the
    * <code>SQLState</code> associated with the <code>ErrorMsg</code>. If a match
    * is not found or <code>ErrorMsg</code> has no <code>SQLState</code>, returns
    * the <code>SQLState</code> bound to the <code>GENERIC_ERROR</code>
    * <code>ErrorMsg</code>.
    *
    * @param mesg An error message string
    * @return SQLState
    */
   public static String findSQLState(String mesg) {
 
     //first see if there is a direct match
     ErrorMsg errorMsg = mesgToErrorMsgMap.get(mesg);
     if (errorMsg != null) {
       if (errorMsg.getSQLState() != null)
         return errorMsg.getSQLState();
       else
         return GENERIC_ERROR.getSQLState();
     }
 
     //if not see if the mesg follows type of format, which is typically the case:
     //line 1:14 Table not found table_name
     String truncatedMesg = mesg.trim();
     Matcher match = ERROR_MESSAGE_PATTERN.matcher(mesg);
     if (match.matches()) truncatedMesg = match.group(1);
 
     //appends might exist after the root message, so strip tokens off until we match
     while (truncatedMesg.length() > minMesgLength) {
       errorMsg = mesgToErrorMsgMap.get(truncatedMesg.trim());
       if (errorMsg != null) {
         if (errorMsg.getSQLState() != null)
           return errorMsg.getSQLState();
         else
           return GENERIC_ERROR.getSQLState();
       }
 
       int lastSpace = truncatedMesg.lastIndexOf(SPACE);
       if (lastSpace == -1) break;
 
       // hack off the last word and try again
       truncatedMesg = truncatedMesg.substring(0, lastSpace).trim();
     }
 
     return GENERIC_ERROR.getSQLState();
   }
 
   ErrorMsg(String mesg) {
     //42000 is the generic SQLState for syntax error.
     this(mesg, "42000");
   }
 
   ErrorMsg(String mesg, String SQLState) {
     this.mesg = mesg;
     this.SQLState = SQLState;
   }
 
   private int getLine(ASTNode tree) {
     if (tree.getChildCount() == 0) {
       return tree.getToken().getLine();
     }
 
     return getLine((ASTNode)tree.getChild(0));
   }
 
   private int getCharPositionInLine(ASTNode tree) {
     if (tree.getChildCount() == 0) {
       return tree.getToken().getCharPositionInLine();
     }
 
     return getCharPositionInLine((ASTNode)tree.getChild(0));
   }
 
   // Dirty hack as this will throw away spaces and other things - find a better way!
   private String getText(ASTNode tree) {
     if (tree.getChildCount() == 0) {
       return tree.getText();
     }
 
     return getText((ASTNode)tree.getChild(tree.getChildCount() - 1));
   }
 
   public String getMsg(ASTNode tree) {
     return "line " + getLine(tree) + ":" + getCharPositionInLine(tree) + " " + mesg + " " + getText(tree);
   }
 
   String getMsg(Tree tree) {
     return getMsg((ASTNode)tree);
   }
 
   String getMsg(ASTNode tree, String reason) {
     return "line " + getLine(tree) + ":" + getCharPositionInLine(tree) + " " + mesg + " " + getText(tree) + ": " + reason;
   }
 
   String getMsg(Tree tree, String reason) {
     return getMsg((ASTNode)tree, reason);
   }
 
   public String getMsg(String reason) {
     return mesg + " " + reason;
   }
 
   public String getMsg() {
     return mesg;
   }
 
   public String getSQLState() {
     return SQLState;
   }
 }
