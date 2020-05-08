 /*
  * Copyright 2014 Mikael Svensson
  *
  *    Licensed under the Apache License, Version 2.0 (the "License");
  *    you may not use this file except in compliance with the License.
  *    You may obtain a copy of the License at
  *
  *        http://www.apache.org/licenses/LICENSE-2.0
  *
  *    Unless required by applicable law or agreed to in writing, software
  *    distributed under the License is distributed on an "AS IS" BASIS,
  *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *    See the License for the specific language governing permissions and
  *    limitations under the License.
  */
 
 package info.mikaelsvensson.devtools.doclet.xml.documentcreator.db2.parser;
 
 import java.util.LinkedList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public abstract class AbstractCommandHandler implements CommandHandler {
     protected static final String REGEXP_QUOTED_NAME = "\"([a-zA-Z0-9_-]+)\"";
     protected static final String REGEXP_QUOTED_NAMES_IN_PARENTHESIS = "\\(((" + REGEXP_QUOTED_NAME + "[\\s,ASCDEascde]*)+)\\)";
     protected static final Pattern QUOTED_NAME_PATTERN = Pattern.compile(REGEXP_QUOTED_NAME);
 
     public static String getAffectedTableName(String sql) {
         int posDot = sql.indexOf('.');
         int posSpace = sql.indexOf(' ', posDot);
        posSpace = posSpace == -1 ? sql.indexOf('\n', posDot) : posSpace;
        posSpace = posSpace == -1 ? sql.indexOf('\r', posDot) : posSpace;
        posSpace = posSpace == -1 ? sql.indexOf('\t', posDot) : posSpace;
         return sql.substring(posDot + 1 + 1, posSpace - 1);
     }
 
     protected List<String> getColumns(String columnListSql) {
         List<String> cols = new LinkedList<String>();
         Matcher colMatcher = QUOTED_NAME_PATTERN.matcher(columnListSql);
         while (colMatcher.find()) {
             cols.add(colMatcher.group(1));
         }
         return cols;
     }
 
     protected String fixSQL(String sql) {
         return sql.replaceAll("\n", "");
     }
 }
