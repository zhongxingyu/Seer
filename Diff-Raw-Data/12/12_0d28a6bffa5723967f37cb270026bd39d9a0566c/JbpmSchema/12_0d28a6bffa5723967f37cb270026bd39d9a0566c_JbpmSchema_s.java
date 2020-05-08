 /*
  * JBoss, Home of Professional Open Source
  * Copyright 2005, JBoss Inc., and individual contributors as indicated
  * by the @authors tag. See the copyright.txt in the distribution for a
  * full listing of individual contributors.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package org.jbpm.db;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.PrintStream;
 import java.io.Serializable;
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.hibernate.cfg.Configuration;
 import org.hibernate.cfg.Settings;
 import org.hibernate.connection.ConnectionProvider;
 import org.hibernate.dialect.Dialect;
 import org.hibernate.engine.Mapping;
 import org.hibernate.mapping.ForeignKey;
 import org.hibernate.mapping.Table;
 import org.hibernate.tool.hbm2ddl.SchemaExport;
 import org.hibernate.util.JDBCExceptionReporter;
 import org.jbpm.JbpmException;
 
 /**
  * utilities for the jBPM database schema.
  */
 public class JbpmSchema implements Serializable
 {
 
   private static final long serialVersionUID = 1L;
 
   Configuration configuration = null;
   Settings settings;
   Mapping mapping = null;
   String[] createSql = null;
   String[] dropSql = null;
   String[] cleanSql = null;
 
   ConnectionProvider connectionProvider = null;
   Connection connection = null;
   Statement statement = null;
 
   public JbpmSchema(Configuration configuration)
   {
     this.configuration = configuration;
     this.settings = configuration.buildSettings();
     this.mapping = configuration.buildMapping();
   }
 
   public String[] getCreateSql()
   {
     if (createSql == null)
     {
       createSql = configuration.generateSchemaCreationScript(settings.getDialect());
     }
     return createSql;
   }
 
   public String[] getDropSql()
   {
     if (dropSql == null)
     {
       dropSql = configuration.generateDropSchemaScript(settings.getDialect());
     }
     return dropSql;
   }
 
   public String[] getCleanSql()
   {
     if (cleanSql == null)
     {
       new SchemaExport(configuration);
 
       Dialect dialect = settings.getDialect();
       String catalog = settings.getDefaultCatalogName();
       String schema = settings.getDefaultSchemaName();
 
       // loop over all foreign key constraints
       List dropForeignKeysSql = new ArrayList();
       List createForeignKeysSql = new ArrayList();
       Iterator iter = configuration.getTableMappings();
       while (iter.hasNext())
       {
         Table table = (Table)iter.next();
         if (table.isPhysicalTable())
         {
           Iterator subIter = table.getForeignKeyIterator();
           while (subIter.hasNext())
           {
             ForeignKey fk = (ForeignKey)subIter.next();
 
             if (fk.isPhysicalConstraint())
             {
               // collect the drop foreign key constraint sql
               String sqlDropString = fk.sqlDropString(dialect, catalog, schema);
               dropForeignKeysSql.add(sqlDropString);
 
               // and collect the create foreign key constraint sql
               String sqlCreateString = fk.sqlCreateString(dialect, mapping, catalog, schema);
               createForeignKeysSql.add(sqlCreateString);
             }
           }
         }
       }
 
       List deleteSql = new ArrayList();
       iter = configuration.getTableMappings();
       while (iter.hasNext())
       {
         Table table = (Table)iter.next();
         deleteSql.add("delete from " + table.getName());
       }
 
       // glue
       // - drop foreign key constraints
       // - delete contents of all tables
       // - create foreign key constraints
       // together to form the clean script
       List cleanSqlList = new ArrayList();
       cleanSqlList.addAll(dropForeignKeysSql);
       cleanSqlList.addAll(deleteSql);
       cleanSqlList.addAll(createForeignKeysSql);
 
       cleanSql = (String[])cleanSqlList.toArray(new String[cleanSqlList.size()]);
     }
     return cleanSql;
   }
 
   public boolean hasJbpmTables()
   {
     return (getJbpmTables().size() > 0);
   }
 
   public List getJbpmTables()
   {
     // delete all the data in the jbpm tables
     List jbpmTableNames = new ArrayList();
     Iterator iter = configuration.getTableMappings();
     while (iter.hasNext())
     {
       Table table = (Table)iter.next();
       if (table.isPhysicalTable())
       {
         jbpmTableNames.add(table.getName());
       }
     }
     return jbpmTableNames;
   }
   
   public Map getJbpmTablesRecordCount() {
     Map recordCounts = new HashMap();
     
     String sql = null;
     
     try
     {
       Iterator iter = getJbpmTables().iterator();
 
       createConnection();
       while (iter.hasNext()) {
         statement = connection.createStatement();
         String tableName = (String) iter.next();
         sql = "SELECT COUNT(*) FROM "+tableName;
         ResultSet resultSet = statement.executeQuery(sql);
         resultSet.next();
         int count = resultSet.getInt(1);
         resultSet.close();
         statement.close();
         recordCounts.put(tableName, count);
       }
     }
     catch (SQLException e)
     {
       throw new JbpmException("couldn't execute sql '" + sql + "'", e);
     }
     finally
     {
       closeConnection();
     }
 
     return recordCounts;
   }
 
   public void dropSchema()
   {
     execute(getDropSql());
   }
 
   public void createSchema()
   {
     execute(getCreateSql());
   }
 
   public void cleanSchema()
   {
     if (getJbpmTables().size() > 0)
       execute(getCleanSql());
   }
 
   public void saveSqlScripts(String dir, String prefix)
   {
     try
     {
       new File(dir).mkdirs();
       saveSqlScript(dir + "/" + prefix + ".drop.sql", getDropSql());
       saveSqlScript(dir + "/" + prefix + ".create.sql", getCreateSql());
       saveSqlScript(dir + "/" + prefix + ".clean.sql", getCleanSql());
       new SchemaExport(configuration).setDelimiter(getSqlDelimiter()).setOutputFile(dir + "/" + prefix + ".drop.create.sql").create(true, false);
     }
     catch (IOException e)
     {
       throw new JbpmException("couldn't generate scripts", e);
     }
   }
 
   public static void main(String[] args)
   {
     if ((args == null) || (args.length == 0))
     {
       syntax();
     }
     else if ("create".equalsIgnoreCase(args[0]) && args.length <= 3)
     {
       Configuration configuration = createConfiguration(args, 1);
       new JbpmSchema(configuration).createSchema();
     }
     else if ("drop".equalsIgnoreCase(args[0]) && args.length <= 3)
     {
       Configuration configuration = createConfiguration(args, 1);
       new JbpmSchema(configuration).dropSchema();
     }
     else if ("clean".equalsIgnoreCase(args[0]) && args.length <= 3)
     {
       Configuration configuration = createConfiguration(args, 1);
       new JbpmSchema(configuration).cleanSchema();
     }
     else if ("scripts".equalsIgnoreCase(args[0]) && args.length >= 3 && args.length <= 5)
     {
       Configuration configuration = createConfiguration(args, 3);
       new JbpmSchema(configuration).saveSqlScripts(args[1], args[2]);
     }
     else
     {
       syntax();
     }
   }
 
   private static void syntax()
   {
     System.err.println("syntax:");
     System.err.println("JbpmSchema create [<hibernate.cfg.xml> [<hibernate.properties>]]");
     System.err.println("JbpmSchema drop [<hibernate.cfg.xml> [<hibernate.properties>]]");
     System.err.println("JbpmSchema clean [<hibernate.cfg.xml> [<hibernate.properties>]]");
     System.err.println("JbpmSchema scripts <dir> <prefix> [<hibernate.cfg.xml> [<hibernate.properties>]]");
   }
 
   static Configuration createConfiguration(String[] args, int index)
   {
     String hibernateCfgXml = (args.length > index ? args[index] : "hibernate.cfg.xml");
     String hibernateProperties = (args.length > (index + 1) ? args[index + 1] : null);
 
     Configuration configuration = new Configuration();
     configuration.configure(new File(hibernateCfgXml));
     if (hibernateProperties != null)
     {
       try
       {
         Properties properties = new Properties();
         InputStream inputStream = new FileInputStream(hibernateProperties);
         properties.load(inputStream);
         configuration.setProperties(properties);
       }
       catch (IOException e)
       {
         throw new JbpmException("couldn't load hibernate configuration", e);
       }
     }
 
     return configuration;
   }
 
   void saveSqlScript(String fileName, String[] sql) throws FileNotFoundException
   {
     FileOutputStream fileOutputStream = new FileOutputStream(fileName);
     try
     {
       PrintStream printStream = new PrintStream(fileOutputStream);
       for (int i = 0; i < sql.length; i++)
       {
         printStream.println(sql[i] + getSqlDelimiter());
       }
     }
     finally
     {
       try
       {
         fileOutputStream.close();
       }
       catch (IOException e)
       {
         log.debug("failed to close file", e);
       }
     }
   }
 
   public void execute(String[] sqls)
   {
     String sql = null;
    boolean showSql = settings.isShowSqlEnabled();
 
     try
     {
       createConnection();
       statement = connection.createStatement();
 
       for (int i = 0; i < sqls.length; i++)
       {
         sql = sqls[i];
 
        if (showSql)
           log.debug(sql);
         statement.executeUpdate(sql);
       }
 
     }
     catch (SQLException e)
     {
       throw new JbpmException("couldn't execute sql '" + sql + "'", e);
     }
     finally
     {
       closeConnection();
     }
   }
 
   void closeConnection()
   {
     if (statement != null)
     {
       try
       {
         statement.close();
       }
       catch (SQLException e)
       {
         log.debug("could not close jdbc statement", e);
       }
     }
     if (connection != null)
     {
       try
       {
         JDBCExceptionReporter.logWarnings(connection.getWarnings());
         connection.clearWarnings();
         connectionProvider.closeConnection(connection);
         connectionProvider.close();
       }
       catch (SQLException e)
       {
         log.debug("could not close jdbc connection", e);
       }
     }
   }
 
   void createConnection() throws SQLException
   {
     connectionProvider = settings.getConnectionProvider();
     connection = connectionProvider.getConnection();
     if (!connection.getAutoCommit())
     {
       connection.commit();
       connection.setAutoCommit(true);
     }
   }
 
   public Properties getProperties()
   {
     return configuration.getProperties();
   }
 
   // sql delimiter ////////////////////////////////////////////////////////////
 
   static String sqlDelimiter = null;
 
   synchronized String getSqlDelimiter()
   {
     if (sqlDelimiter == null)
     {
       sqlDelimiter = getProperties().getProperty("jbpm.sql.delimiter", ";");
     }
     return sqlDelimiter;
   }
 
   // logger ///////////////////////////////////////////////////////////////////
 
   private static final Log log = LogFactory.getLog(JbpmSchema.class);
 }
