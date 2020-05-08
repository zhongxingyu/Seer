 package ki.webgame;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.ResultSet;
 import java.sql.ResultSetMetaData;
 import java.sql.SQLException;
 import java.sql.Types;
 import java.util.List;
 import javax.naming.InitialContext;
 import javax.naming.NamingException;
 import javax.sql.DataSource;
 
 /**
  * All the STATIC methods for DB stuff.
  * Also the creation scripts that automatically check for tables.
  */
 public class DBManager
 {
     private static final String JDBC_NAME = "java:comp/env/jdbc/webgame";
     
     private static volatile DataSource dataSource;
     
     public static Connection getConnection() throws NamingException, SQLException
     {
         if (dataSource == null)
         {
             dataSource = (DataSource) new InitialContext().lookup(JDBC_NAME);
             checkTables();
         }
         
         return dataSource.getConnection();
     }
     
     public static void query(String query, List<Object> parameters, QueryDone querydone) throws Exception
     {
         query(null, query, parameters, querydone);
     }
     
     public static void query(Connection c, String query, List<Object> parameters, QueryDone querydone) throws Exception
     {
         boolean toclose = false;
         if (c == null)
         {
             c = getConnection();
             toclose = true;
         }
         
         try
         {
             try (PreparedStatement ps = c.prepareStatement(query))
             {
                 for (int i = 0; parameters != null && i < parameters.size(); i++)
                     ps.setObject(i+1, parameters.get(i));
                 
                 // With a querydone callback, we expect results
                 // otherwise is condidered to be an update
                 if (querydone != null)
                     try (ResultSet rs = ps.executeQuery())
                     {
                         querydone.done(rs);
                     }
                 else
                     ps.executeUpdate();
             }
         }
         finally
         {
             if (toclose)
                 c.close();
         }
     }
     
     public static String rs2json(ResultSet rs) throws SQLException
     {
         JSONBuilder jb = new JSONBuilder();
         jb.beginArray();
         ResultSetMetaData rsmd = rs.getMetaData();
         while (rs.next())
         {
             jb.beginObject();
             for (int i = 0; i < rsmd.getColumnCount(); i++)
             {
                 String label = rsmd.getColumnLabel(i+1);
                 int type = rsmd.getColumnType(i+1);
                 switch (type)
                 {
                     case Types.VARCHAR:
                     case Types.CHAR:
                     case Types.CLOB:
                         jb.property(label, rs.getString(i+1));
                         break;
                     default:
                         jb.property(label, rs.getObject(i+1));
                 }
             }
             jb.endObject();
         }
         jb.endArray();
         return jb.toString();
     }
     
     @FunctionalInterface
     public interface QueryDone
     {
         void done(ResultSet rs) throws Exception;
     }
     
     /**
      * Checks the database version and updates it accordingly.
      */
     private static void checkTables() throws SQLException
     {
         try (Connection c = dataSource.getConnection())
         {
             // Always execute the create/if for the version table
             try (PreparedStatement ps = c.prepareStatement(DB_VERSION_TABLE))
             {
                 ps.executeUpdate();
             }
             
             try (PreparedStatement ps = c.prepareStatement("select version from version"))
             {
                 try (ResultSet rs = ps.executeQuery())
                 {
                     int fromVersion = 0, toVersion = DB_VERSION;
                     boolean hadRecord = rs.next();
                     // when database is just a new one, fromVersion remains 0
                     if (hadRecord)
                         fromVersion = rs.getInt(1);
                     
                     // Update all the statements
                     for (int i = fromVersion; i < toVersion; i++)
                     {
                         String[] queries = DDL[i+1];
                         for (String query: queries)
                             try (PreparedStatement ps2 = c.prepareStatement(query))
                             {
                                 ps2.executeUpdate();
                             }
                     }
 
                     // Update the version number
                     if (!hadRecord)
                         try (PreparedStatement ps2 = c.prepareStatement("insert into version (version) values (?)"))
                         {
                             ps2.setInt(1, DB_VERSION);
                             ps2.executeUpdate();
                         }
                     else
                         try (PreparedStatement ps2 = c.prepareStatement("update version set version = ?"))
                         {
                             ps2.setInt(1, DB_VERSION);
                             ps2.executeUpdate();
                         }
                 }
             }
         }
     }
     
     private static final String DB_VERSION_TABLE = "create table if not exists version (version integer) ENGINE=InnoDB CHARSET=UTF8";
     
     private static final int DB_VERSION = 1;
     
     private static final String[][] DDL =
     {
         // Entry 0 - empty
         {},
         // v1
         {
             "create table if not exists users "
             + "(id integer not null auto_increment, "
             + "username VARCHAR(255) not null, "
             + "password VARCHAR(255) not null, "
             + "email varchar(255) not null, "
             + "registered integer not null default 0, "
             + "checkcode varchar(255), "
             + "race char(1) not null, "
             + "score bigint not null default 0,"
             + "strength double not null default 0.25, "
             + "land double not null default 0.25, "
             + "energy double not null default 0.5, "
             + "rage double not null default 0, "
             // S = strength, L = land
             + "task char(1) not null default 'S', "
            + "lastupdate datetime not null default now(), "
             + "constraint unique key (username), "
             + "constraint unique key (email), "
             + "primary key (id)) ENGINE=InnoDB CHARSET=UTF8",
             
             "create index users_username_idx on users (username)",
             "create index users_score_idx on users (score)",
             "create index users_strength_idx on users (strength)",
             "create index users_land_idx on users (land)",
             "create index users_energy_idx on users (energy)",
             "create index users_lastupdate_idx on users (lastupdate)",
             
             "create table if not exists attack_history "
             + "(id integer not null auto_increment, "
             + "attacking_userid integer not null, "
             + "defending_userid integer not null, "
             // algebric result: negative means a lost from the attacker
             + "result double not null, "
             // All the deltas resulting of the attack that have been applied to the stats
             + "attacker_strength_delta double not null, "
             + "attacker_land_delta double not null, "
             + "attacker_energy_delta double not null, "
             + "defender_strength_delta double not null, "
             + "defender_land_delta double not null, "
             + "defender_energy_delta double not null, "
            + "attack_time datetime not null, "
             + "constraint foreign key attack_history_attacker (attacking_userid) references users (id), "
             + "constraint foreign key attack_history_defender (defending_userid) references users (id), "
             + "primary key (id)) ENGINE=InnoDB CHARSET=UTF8",
             
             "create index attack_history_attacker_idx on attack_history (attacking_userid)",
             "create index attack_history_defender_idx on attack_history (defending_userid)",
         }
     };
 }
