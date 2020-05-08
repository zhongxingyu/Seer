 package uk.ac.ebi.arrayexpress.utils.db;
 
 /*
  * Copyright 2009-2010 European Molecular Biology Laboratory
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *   http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  *
  */
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.util.ArrayList;
 import java.util.List;
 
 public class ExperimentListDatabaseRetriever extends SqlStatementExecutor
 {
     // logging facility
     private final Logger logger = LoggerFactory.getLogger(getClass());
     // sql to get a list of experiments from the database
     // (the parameter is either 0 for all experiments and 1 for public only)
     private final static String getExperimentListSql = "select distinct e.id" +
             " from tt_experiment e" +
// test :)  "  where e.id in (2200649523, 13850163, 1673780805, 352682122, 366346722)" +
             " order by" +
             "  e.id asc";
 
     // experiment list
     private List<Long> experimentList;
 
     public ExperimentListDatabaseRetriever( IConnectionSource connSource )
     {
         super(connSource, getExperimentListSql);
         experimentList = new ArrayList<Long>();
     }
 
     public List<Long> getExperimentList()
     {
         if (!execute(false)) {
             logger.error("There was a problem retrieving the list of experiments, check log for errors or exceptions");
         }
         return experimentList;
     }
 
     protected void setParameters( PreparedStatement stmt ) throws SQLException
     {
     }
 
     protected void processResultSet( ResultSet resultSet ) throws SQLException
     {
         while ( resultSet.next() ) {
             experimentList.add(resultSet.getLong(1));
         }
     }
 }
 
