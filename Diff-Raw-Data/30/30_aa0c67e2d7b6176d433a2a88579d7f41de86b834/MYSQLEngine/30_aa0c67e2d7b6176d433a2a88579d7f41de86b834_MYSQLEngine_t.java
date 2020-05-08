 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package io;
 
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Iterator;
 import org.joda.time.DateTime;
 import system.Ticket;
 import system.User;
 
 /**
  * A class to encapsulate the connectivity to the MYSQL database.
  *
  * @author jonathanrainer
  */
 public class MYSQLEngine
 {
 
     // JDBC driver name and database URL
     private String JDBC_DRIVER;
     private String DB_URL;
     //  Database credentials
     private String USER;
     private String PASS;
     private static final String MYSQLTIMEFORMAT = "YYYY-MM-dd HH:mm:ss";
 
     /**
      * Constructor to create the object, requiring the host, database, username
      * and password. Root access is not required but it would be good to use a
      * dedicated user with a fairly high level of privilege but root is not
      * required at the moment.
      *
      * @param mysqlhost URL of the MYSQL server.
      * @param mysqldatabase Name of the MYSQL database.
      * @param mysqluser Username for the MYSQL database.
      * @param mysqlpassword Password for the above use on the MYSQL database.
      */
     public MYSQLEngine(String mysqlhost, String mysqldatabase, String mysqluser, String mysqlpassword)
     {
         JDBC_DRIVER = "com.mysql.jdbc.Driver";
         DB_URL = "jdbc:mysql://" + mysqlhost + ":3306/" + mysqldatabase;
         USER = mysqluser;
         PASS = mysqlpassword;
     }
 
     /**
      * Method to look at the attached database and extract, from the information
      * schema, the team names stored in the ENUM of the same name in the
      * database. In short, when someone puts a user into the Users table in the
      * database they have to be assigned a team based on an ENUM embedded in the
      * team field, this method extracts all the choices so they can be put into
      * a combo box.
      *
      * @return An ArrayList of the team names as dictated by the database.
      */
     public ArrayList<String> enumerateTeamNames()
     {
         // Set up the initial connection and statement objects
         Connection conn = null;
         Statement stmt = null;
         // Create a new ArrayList to store the results of the MYSQL query.
         ArrayList<String> results = new ArrayList<>();
         // Begin try block so SQL Exceptions can be handled later
         try
         {
             // Register JDBC driver
             Class.forName("com.mysql.jdbc.Driver");
             // Open a connection
             conn = DriverManager.getConnection(DB_URL, USER, PASS);
 
             // Create and Execute the SQL Query
             stmt = conn.createStatement();
             /**
              * Query states: Look in the information_schema table and extract,
              * to a substring, the entire contents of the ENUM called team.
              */
             String sql = "SELECT SUBSTRING( COLUMN_TYPE, 5 ) FROM "
                     + "information_schema.COLUMNS WHERE TABLE_SCHEMA = 'Nexus' "
                     + "AND TABLE_NAME = 'users' AND COLUMN_NAME = 'team'";
             ResultSet rs = stmt.executeQuery(sql);
 
             // Extract the resulting data from the ResultSet
             while (rs.next())
             {
                 String stringToSplit = rs.getString(1);
                 // Remove junk characters
                 stringToSplit = stringToSplit.replaceAll("[(\"\')]", "");
                 // Split the string using the comma as a delimiter
                 String[] argumentArray = stringToSplit.split(",");
                 Arrays.sort(argumentArray);
                 int i = 0;
                 /**
                  * Remove Super Users from the list since login in as a Super
                  * User will be dealt with separately.
                  */
                 while (i < argumentArray.length)
                 {
                     if (argumentArray[i].equals("Super Users"))
                     {
                         i++;
                     } else
                     {
                         results.add(argumentArray[i]);
                         i++;
                     }
                 }
             }
             // Close all the statements, result set and connection.
             rs.close();
             stmt.close();
             conn.close();
         } catch (SQLException se)
         {
             //Handle errors for JDBC
         } catch (Exception e)
         {
             //Handle errors for Class.forName
         } finally
         {
             //finally block used to close resources should all else fail
             try
             {
                 if (stmt != null)
                 {
                     stmt.close();
                 }
             } catch (SQLException se2)
             {
             }// nothing we can do
 
             try
             {
                 if (conn != null)
                 {
                     conn.close();
                 }
             } catch (SQLException se)
             {
             }
         }
         return results;
     }
 
     public ArrayList<String> enumerateTeamMembers(String team)
     {
         // Set up the initial connection and statement objects
         Connection conn = null;
         Statement stmt = null;
         // Create a new ArrayList to store the results of the MYSQL query.
         ArrayList<String> results = new ArrayList<>();
         // Begin try block so SQL Exceptions can be handled later
         try
         {
             // Register JDBC driver
             Class.forName("com.mysql.jdbc.Driver");
             // Open a connection
             conn = DriverManager.getConnection(DB_URL, USER, PASS);
 
             // Create and Execute the SQL Query
             stmt = conn.createStatement();
             /**
              * Query states: Select all the users in a particular team
              */
             String sql;
             sql = "SELECT `name` FROM `users` WHERE `team` = '" + team + "'";
             try (ResultSet rs = stmt.executeQuery(sql))
             {
                 while (rs.next())
                 {
                     results.add(rs.getString(1));
                 }
             }
             stmt.close();
             conn.close();
         } catch (SQLException se)
         {
             //Handle errors for JDBC
         } catch (Exception e)
         {
             //Handle errors for Class.forName
         } finally
         {
             //finally block used to close resources should all else fail
             try
             {
                 if (stmt != null)
                 {
                     stmt.close();
                 }
             } catch (SQLException se2)
             {
             }// nothing we can do
 
             try
             {
                 if (conn != null)
                 {
                     conn.close();
                 }
             } catch (SQLException se)
             {
             }
         }
         return results;
     }
 
     public boolean submitTicket(Ticket ticket, boolean duplicate)
     {
         // Set up the initial connection and statement objects
         Connection conn = null;
         Statement stmt1 = null;
         Statement stmt2 = null;
         boolean success = false;
         // Begin try block so SQL Exceptions can be handled later
         try
         {
             // Register JDBC driver
             Class.forName("com.mysql.jdbc.Driver");
             // Open a connection
             conn = DriverManager.getConnection(DB_URL, USER, PASS);
 
             // Create and Execute the SQL Query
             stmt1 = conn.createStatement();
             stmt2 = conn.createStatement();
             /**
              * Query states: Select all the users in a particular team
              */
             String table = "";
             if(duplicate)
             {
                 table = "`duplicateQueue`";
             }
             else
             {
                 table = "`tickets`";
             }
             String insertionQuery;
             insertionQuery = "INSERT INTO " + table +  " VALUES (NULL, NOW(), '" + 
                     ticket.getTicketRaisedBy().getTeam() + "', "
                     + "'" + ticket.getTicketRaisedBy().getName() + "', " + "'" 
                     + ticket.getProblemLocation() + "', " 
                     + "'" + ticket.getProblemDescription() + "', " + "'" + 
                     ticket.getCISKeywordsAsString() + "', "
                     + "'" + ticket.getReportedBy() + "', " + "'" + 
                     ticket.getWhoIsA()+ "', " + "'" + ticket.getContactVia() + 
                     "', '" + ticket.getContactNumber() + "', " + "'" + 
                     ticket.getLocationVenueVillage() + "', '" + 
                     ticket.getDelegateImpact() + "'";
             
             if(ticket.getTicketRaisedBy().getTeam().equals("Control Office"))
             {
                 int showOnCIS = ticket.isShowOnCIS() ? 1 : 0;
                 String ticketAllocatedTo = ticket.getTicketAllocatedTo();
                 String jobProgress = ticket.getJobProgress();
                 DateTime asAt = ticket.getAsAt();
                 insertionQuery = insertionQuery + ", " + showOnCIS + ", '" + 
                         ticketAllocatedTo + "', '" + jobProgress + "', '" 
                         + asAt.toString(MYSQLTIMEFORMAT) 
                         + "', NULL, NULL, NULL, NULL, NULL, "
                     + "NULL, NULL, NULL, NULL, NULL, NULL);";
                 
             }
             else
             {
                 insertionQuery = insertionQuery + ",'0', NULL,"
                     + " 'Issue Reported', NOW(), NULL, NULL, NULL, NULL, NULL, "
                     + "NULL, NULL, NULL, NULL, NULL, NULL);";
             }
             stmt1.executeUpdate(insertionQuery);
            String query2 = "SELECT * FROM `" + table + "` WHERE `CISKeywords` LIKE '" + 
                     ticket.getCISKeywordsAsString() + "' AND `problemLocation`"
                     + "LIKE '" + ticket.getProblemLocation() + "' AND `reportedBy`"
                     + " LIKE '" + ticket.getReportedBy() + "';";
             ResultSet rs = stmt2.executeQuery(query2);
             while(rs.next())
             {
                 ticket.setJobRefId(rs.getInt("jobRefId"));
                 String timeStamp = rs.getString("dateTime");
                 ticket.setDateTime(timeStampToDateTime(timeStamp));
             }
             success = true;
             stmt1.close();
             stmt2.close();
             conn.close();
         } catch (SQLException se)
         {
             System.out.println(se.getSQLState());
             //Handle errors for JDBC
         } catch (Exception e)
         {
             //Handle errors for Class.forName
         } finally
         {
             //finally block used to close resources should all else fail
             try
             {
                 if (stmt1 != null || stmt2 != null)
                 {
                     stmt1.close();
                     stmt2.close();
                 }
             } catch (SQLException se2)
             {
             }// nothing we can do
 
             try
             {
                 if (conn != null)
                 {
                     conn.close();
                 }
             } catch (SQLException se)
             {
             }
         }
         return success;
     }
     
     public boolean isTicketDuplicate(Ticket ticket)
     {
         // Set up the initial connection and statement objects
         Connection conn = null;
         Statement stmt = null;
         boolean success = false;
         // Begin try block so SQL Exceptions can be handled later
         try
         {
             // Register JDBC driver
             Class.forName("com.mysql.jdbc.Driver");
             // Open a connection
             conn = DriverManager.getConnection(DB_URL, USER, PASS);
 
             // Create and Execute the SQL Query
             stmt = conn.createStatement();
             /**
              * Query states: Select all the users in a particular team
              */
             String sql;
             sql = "SELECT * FROM `tickets` WHERE `CISKeywords` LIKE '" + 
                     ticket.getCISKeywordsAsString() + "' AND `problemLocation`"
                     + "LIKE '" + ticket.getProblemLocation() + "' AND `reportedBy`"
                     + " LIKE '" + ticket.getReportedBy() + "';";
             ResultSet rs = stmt.executeQuery(sql);
             if (!rs.isBeforeFirst())
             {
             }
             else
             {
                 success = true;
             }
             stmt.close();
             conn.close();
         } catch (SQLException se)
         {
             System.out.println(se.getSQLState());
             //Handle errors for JDBC
         } catch (Exception e)
         {
             //Handle errors for Class.forName
         } finally
         {
             //finally block used to close resources should all else fail
             try
             {
                 if (stmt != null)
                 {
                     stmt.close();
                 }
             } catch (SQLException se2)
             {
             }// nothing we can do
 
             try
             {
                 if (conn != null)
                 {
                     conn.close();
                 }
             } catch (SQLException se)
             {
             }
         }
         return success;
     }
     
     public Ticket[] getTicketsThisTicketDuplicates(Ticket ticket)
     {
         // Set up the initial connection and statement objects
         Connection conn = null;
         Statement stmt = null;
         ArrayList<Ticket> duplicatesFromMainDB = new ArrayList<>();
         // Begin try block so SQL Exceptions can be handled later
         try
         {
             // Register JDBC driver
             Class.forName("com.mysql.jdbc.Driver");
             // Open a connection
             conn = DriverManager.getConnection(DB_URL, USER, PASS);
 
             // Create and Execute the SQL Query
             stmt = conn.createStatement();
             /**
              * Query states: Select all the users in a particular team
              */
             String sql;
             sql = "SELECT * FROM `tickets` WHERE `CISKeywords` LIKE '" + 
                     ticket.getCISKeywordsAsString() + "' AND `problemLocation`"
                     + "LIKE '" + ticket.getProblemLocation() + "' AND `reportedBy`"
                     + " LIKE '" + ticket.getReportedBy() + "';";
             ResultSet rs = stmt.executeQuery(sql);
             while(rs.next())
             {
                 Ticket duplicateTicket = retrieveTicket(rs.getString("jobRefId"), "tickets");
                 duplicatesFromMainDB.add(duplicateTicket);
             }
             stmt.close();
             conn.close();
         } catch (SQLException se)
         {
             System.out.println(se.getSQLState());
             //Handle errors for JDBC
         } catch (Exception e)
         {
             //Handle errors for Class.forName
         } finally
         {
             //finally block used to close resources should all else fail
             try
             {
                 if (stmt != null)
                 {
                     stmt.close();
                 }
             } catch (SQLException se2)
             {
             }// nothing we can do
 
             try
             {
                 if (conn != null)
                 {
                     conn.close();
                 }
             } catch (SQLException se)
             {
             }
         }
         int i = 0;
         Ticket[] duplicateMainDBArray = new Ticket[duplicatesFromMainDB.size()];
         while(i < duplicatesFromMainDB.size())
         {
             duplicateMainDBArray[i] = duplicatesFromMainDB.get(i);
             i++;
         }
         return duplicateMainDBArray;
     }
     
     public Ticket retrieveTicket(String ticketID, String table)
     {
         // Set up the initial connection and statement objects
         Connection conn = null;
         Statement stmt = null;
         Ticket ticket = null;
         // Begin try block so SQL Exceptions can be handled later
         try
         {
             // Register JDBC driver
             Class.forName("com.mysql.jdbc.Driver");
             // Open a connection
             conn = DriverManager.getConnection(DB_URL, USER, PASS);
 
             // Create and Execute the SQL Query
             stmt = conn.createStatement();
             /**
              * Query states: Look in the information_schema table and extract,
              * to a substring, the entire contents of the ENUM called team.
              */
             String sql = "SELECT * FROM `" + table + "` WHERE `jobRefId` = " + ticketID + ";";
             ResultSet rs = stmt.executeQuery(sql);
 
             // Extract the resulting data from the ResultSet
             while (rs.next())
             {
                 int jobRefID = rs.getInt("jobRefId");
                 DateTime dateTime = timeStampToDateTime(rs.getString("dateTime"));
                 User user = new User(rs.getString("ticketRaisedByTeam"));
                 user.setName(rs.getString("ticketRaisedByMember"));
                 String problemLocation = rs.getString("problemLocation");
                 String problemDescription = rs.getString("problemDescription");
                 String[] CISKeywordsArray = rs.getString("CISKeywords").split(";");
                 int i = 0;
                 ArrayList<String> CISKeywords = new ArrayList<>();
                 while (i < CISKeywordsArray.length)
                 {
                     CISKeywords.add(CISKeywordsArray[i]);
                     i++;
                 }
                 String reportedBy = rs.getString("reportedBy");
                 String whoIsA = rs.getString("whoIsA");
                 String contactVia = rs.getString("contactVia");
                 String contactNumber = rs.getString("contactNumber");
                 String locationVenueVillage = rs.getString("locationVenueVillage");
                 String delegateImpact = rs.getString("delegateImpact");
                 boolean showOnCIS = rs.getBoolean("showOnCIS");
                 String ticketAllocatedTo = rs.getString("ticketAllocatedTo");
                 String jobProgress = rs.getString("jobProgress");
                 DateTime asAt = timeStampToDateTime(rs.getString("asat"));
                 ArrayList<String> updateDescriptions = new ArrayList<>();
                 updateDescriptions.add(rs.getString("update1Description"));
                 updateDescriptions.add(rs.getString("update2Description"));
                 updateDescriptions.add(rs.getString("update3Description"));
                 ArrayList<DateTime> estimatedCompletions = new ArrayList<>();
                 estimatedCompletions.add(timeStampToDateTime(rs.getString("update1EstimatedCompletion")));
                 estimatedCompletions.add(timeStampToDateTime(rs.getString("update2EstimatedCompletion")));
                 estimatedCompletions.add(timeStampToDateTime(rs.getString("update3EstimatedCompletion")));
                 ArrayList<DateTime> updatedAt = new ArrayList<>();
                 updatedAt.add(timeStampToDateTime(rs.getString("update1UpdatedAt")));
                 updatedAt.add(timeStampToDateTime(rs.getString("update2UpdatedAt")));
                 updatedAt.add(timeStampToDateTime(rs.getString("update3UpdatedAt")));
                 DateTime jobClosed = timeStampToDateTime(rs.getString("jobClosed"));
                 DateTime nextUpdateDue = timeStampToDateTime(rs.getString("nextUpdateDue"));
                 ticket = new Ticket(jobRefID, dateTime, user, problemLocation,
                         problemDescription, CISKeywords, reportedBy, whoIsA,
                         contactVia, contactNumber, locationVenueVillage, delegateImpact, showOnCIS,
                         ticketAllocatedTo, jobProgress, asAt, updateDescriptions,
                         estimatedCompletions, updatedAt, jobClosed, nextUpdateDue);
             }
             // Close all the statements, result set and connection.
             rs.close();
             stmt.close();
             conn.close();
         } catch (SQLException se)
         {
             //Handle errors for JDBC
         } catch (Exception e)
         {
             //Handle errors for Class.forName
         } finally
         {
             //finally block used to close resources should all else fail
             try
             {
                 if (stmt != null)
                 {
                     stmt.close();
                 }
             } catch (SQLException se2)
             {
             }// nothing we can do
 
             try
             {
                 if (conn != null)
                 {
                     conn.close();
                 }
             } catch (SQLException se)
             {
             }
         }
         return ticket;
     }
     
     public Ticket[] getDuplicates()
     {
         // Set up the initial connection and statement objects
         Connection conn = null;
         Statement stmt = null;
         ArrayList<Ticket> duplicates = new ArrayList<>();
         // Begin try block so SQL Exceptions can be handled later
         try
         {
             // Register JDBC driver
             Class.forName("com.mysql.jdbc.Driver");
             // Open a connection
             conn = DriverManager.getConnection(DB_URL, USER, PASS);
 
             // Create and Execute the SQL Query
             stmt = conn.createStatement();
             /**
              * Query states: Select all the users in a particular team
              */
             String sql = "SELECT * FROM duplicateQueue";
             ResultSet rs = stmt.executeQuery(sql);
             while(rs.next())
             {
                Ticket ticket = retrieveTicket(rs.getString("jobRefId"), "duplicateQueue");
                 duplicates.add(ticket);
             }
             stmt.close();
             conn.close();
         } catch (SQLException se)
         {
 ;            System.out.println(se.getSQLState());
             //Handle errors for JDBC
         } catch (Exception e)
         {
             //Handle errors for Class.forName
         } finally
         {
             //finally block used to close resources should all else fail
             try
             {
                 if (stmt != null )
                 {
                     stmt.close();
                 }
             } catch (SQLException se2)
             {
             }// nothing we can do
 
             try
             {
                 if (conn != null)
                 {
                     conn.close();
                 }
             } catch (SQLException se)
             {
             }
         }
         int i = 0;
         Ticket[] duplicateArray = new Ticket[duplicates.size()];
         while(i < duplicates.size())
         {
             duplicateArray[i] = duplicates.get(i);
             i++;
         }
         return duplicateArray;
     }
     
     public boolean updateTicket(Ticket ticket)
     {
          // Set up the initial connection and statement objects
         Connection conn = null;
         Statement stmt = null;
         Boolean success = false;
         // Begin try block so SQL Exceptions can be handled later
         try
         {
             // Register JDBC driver
             Class.forName("com.mysql.jdbc.Driver");
             // Open a connection
             conn = DriverManager.getConnection(DB_URL, USER, PASS);
 
             // Create and Execute the SQL Query
             stmt = conn.createStatement();
             /**
              * Query states: Look in the information_schema table and extract,
              * to a substring, the entire contents of the ENUM called team.
              */
             int showOnCIS = ticket.isShowOnCIS() ? 1 : 0;
             String sql = "UPDATE `tickets` SET delegateImpact = '" + ticket.getDelegateImpact() + 
                     "', showOnCIS = " + showOnCIS + ", ticketAllocatedTo = '" + ticket.getTicketAllocatedTo() +
                     "', jobProgress = '" + ticket.getJobProgress() + "', asAt = '" + ticket.getAsAt().toString(MYSQLTIMEFORMAT) +
                     "',";
             int i = 1;
             while (i <= ticket.getUpdateDescriptions().size())
             {
                 if(!(ticket.getUpdateDescriptions().get(i-1) == null))
                 {
                     sql = sql + " update" + i + "Description = '" + 
                         ticket.getUpdateDescriptions().get(i - 1) + "', update" + i +
                         "EstimatedCompletion = '" + ticket.getEstimatedCompletions().get(i-1).toString(MYSQLTIMEFORMAT) +
                         "', update" + i + "UpdatedAt = '" + ticket.getUpdatedAt().get(i-1).toString(MYSQLTIMEFORMAT) + "',";
                     i++;
                 }
                 else
                 {
                     sql = sql + " update" + i + "Description = '" + 
                         ticket.getUpdateDescriptions().get(i - 1) + "', update" + i +
                         "EstimatedCompletion = '" + ticket.getEstimatedCompletions().get(i-1) +
                         "', update" + i + "UpdatedAt = '" + ticket.getUpdatedAt().get(i-1) + "',";
                     i++;
                 }
                 
             }
             if(!(ticket.getJobClosed() == null))
             {
                 sql = sql + " jobClosed = '" + ticket.getJobClosed().toString(MYSQLTIMEFORMAT) +
                     "', nextUpdateDue = '" + ticket.getNextUpdateDue().toString(MYSQLTIMEFORMAT) + "' "
                     + "WHERE jobRefId = " + ticket.getJobRefId() + " ;";
             }
             else
             {
                 sql = sql + " nextUpdateDue = '" + ticket.getNextUpdateDue().toString(MYSQLTIMEFORMAT) + "' "
                     + "WHERE jobRefId = " + ticket.getJobRefId() + " ;";
             }
             
             sql = sql.replace("'null'", "NULL"); 
             
            stmt.executeUpdate(sql);
            success = true;
            stmt.close();
            conn.close();
         } catch (SQLException se)
         {
             //Handle errors for JDBC
         } catch (Exception e)
         {
             //Handle errors for Class.forName
         } finally
         {
             //finally block used to close resources should all else fail
             try
             {
                 if (stmt != null)
                 {
                     stmt.close();
                 }
             } catch (SQLException se2)
             {
             }// nothing we can do
 
             try
             {
                 if (conn != null)
                 {
                     conn.close();
                 }
             } catch (SQLException se)
             {
             }
         }
         return success;
     }
     
     public Ticket[] getUnprinted()
     {
         // Set up the initial connection and statement objects
         Connection conn = null;
         Statement stmt = null;
         ArrayList<Ticket> unprinted = new ArrayList<>();
         // Begin try block so SQL Exceptions can be handled later
         try
         {
             // Register JDBC driver
             Class.forName("com.mysql.jdbc.Driver");
             // Open a connection
             conn = DriverManager.getConnection(DB_URL, USER, PASS);
 
             // Create and Execute the SQL Query
             stmt = conn.createStatement();
             /**
              * Query states: Select all the users in a particular team
              */
             String sql = "SELECT * FROM tickets WHERE jobProgress = 'Issue Reported'";
             ResultSet rs = stmt.executeQuery(sql);
             while(rs.next())
             {
                 Ticket ticket = retrieveTicket(rs.getString("jobRefId"), "tickets");
                 unprinted.add(ticket);
             }
             stmt.close();
             conn.close();
         } catch (SQLException se)
         {
 ;            System.out.println(se.getSQLState());
             //Handle errors for JDBC
         } catch (Exception e)
         {
             //Handle errors for Class.forName
         } finally
         {
             //finally block used to close resources should all else fail
             try
             {
                 if (stmt != null )
                 {
                     stmt.close();
                 }
             } catch (SQLException se2)
             {
             }// nothing we can do
 
             try
             {
                 if (conn != null)
                 {
                     conn.close();
                 }
             } catch (SQLException se)
             {
             }
         }
         int i = 0;
         Ticket[] unprintedArray = new Ticket[unprinted.size()];
         while(i < unprinted.size())
         {
             unprintedArray[i] = unprinted.get(i);
             i++;
         }
         return unprintedArray;
     }
     
     public ArrayList<Ticket> getIssueReportedTickets()
     {
         // Set up the initial connection and statement objects
         Connection conn = null;
         Statement stmt = null;
         ArrayList<Ticket> issueReportedTickets = new ArrayList<>();
         // Begin try block so SQL Exceptions can be handled later
         try
         {
             // Register JDBC driver
             Class.forName("com.mysql.jdbc.Driver");
             // Open a connection
             conn = DriverManager.getConnection(DB_URL, USER, PASS);
 
             // Create and Execute the SQL Query
             stmt = conn.createStatement();
             /**
              * Query states: Select all the users in a particular team
              */
             String sql = "SELECT * FROM tickets WHERE jobProgress = 'Issue Reported'";
             ResultSet rs = stmt.executeQuery(sql);
             while(rs.next())
             {
                 Ticket ticket = retrieveTicket(rs.getString("jobRefId"), "tickets");
                 issueReportedTickets.add(ticket);
             }
             stmt.close();
             conn.close();
         } catch (SQLException se)
         {
 ;            System.out.println(se.getSQLState());
             //Handle errors for JDBC
         } catch (Exception e)
         {
             //Handle errors for Class.forName
         } finally
         {
             //finally block used to close resources should all else fail
             try
             {
                 if (stmt != null )
                 {
                     stmt.close();
                 }
             } catch (SQLException se2)
             {
             }// nothing we can do
 
             try
             {
                 if (conn != null)
                 {
                     conn.close();
                 }
             } catch (SQLException se)
             {
             }
         }
         return issueReportedTickets;
     }
     
     public ArrayList<Ticket> getTicketPrintedTickets()
     {
         // Set up the initial connection and statement objects
         Connection conn = null;
         Statement stmt = null;
         ArrayList<Ticket> ticketPrintedTickets = new ArrayList<>();
         // Begin try block so SQL Exceptions can be handled later
         try
         {
             // Register JDBC driver
             Class.forName("com.mysql.jdbc.Driver");
             // Open a connection
             conn = DriverManager.getConnection(DB_URL, USER, PASS);
 
             // Create and Execute the SQL Query
             stmt = conn.createStatement();
             /**
              * Query states: Select all the users in a particular team
              */
             String sql = "SELECT * FROM tickets WHERE jobProgress = 'Ticket Printed'";
             ResultSet rs = stmt.executeQuery(sql);
             while(rs.next())
             {
                 Ticket ticket = retrieveTicket(rs.getString("jobRefId"), "tickets");
                 ticketPrintedTickets.add(ticket);
             }
             stmt.close();
             conn.close();
         } catch (SQLException se)
         {
 ;            System.out.println(se.getSQLState());
             //Handle errors for JDBC
         } catch (Exception e)
         {
             //Handle errors for Class.forName
         } finally
         {
             //finally block used to close resources should all else fail
             try
             {
                 if (stmt != null )
                 {
                     stmt.close();
                 }
             } catch (SQLException se2)
             {
             }// nothing we can do
 
             try
             {
                 if (conn != null)
                 {
                     conn.close();
                 }
             } catch (SQLException se)
             {
             }
         }
         return ticketPrintedTickets;
     }
     
      public ArrayList<Ticket> getjobInProgressTickets()
     {
         // Set up the initial connection and statement objects
         Connection conn = null;
         Statement stmt = null;
         ArrayList<Ticket> jobInProgressTickets = new ArrayList<>();
         // Begin try block so SQL Exceptions can be handled later
         try
         {
             // Register JDBC driver
             Class.forName("com.mysql.jdbc.Driver");
             // Open a connection
             conn = DriverManager.getConnection(DB_URL, USER, PASS);
 
             // Create and Execute the SQL Query
             stmt = conn.createStatement();
             /**
              * Query states: Select all the users in a particular team
              */
             String sql = "SELECT * FROM tickets WHERE jobProgress = 'Job In Progress'";
             ResultSet rs = stmt.executeQuery(sql);
             while(rs.next())
             {
                 Ticket ticket = retrieveTicket(rs.getString("jobRefId"), "tickets");
                 jobInProgressTickets.add(ticket);
             }
             stmt.close();
             conn.close();
         } catch (SQLException se)
         {
 ;            System.out.println(se.getSQLState());
             //Handle errors for JDBC
         } catch (Exception e)
         {
             //Handle errors for Class.forName
         } finally
         {
             //finally block used to close resources should all else fail
             try
             {
                 if (stmt != null )
                 {
                     stmt.close();
                 }
             } catch (SQLException se2)
             {
             }// nothing we can do
 
             try
             {
                 if (conn != null)
                 {
                     conn.close();
                 }
             } catch (SQLException se)
             {
             }
         }
         return jobInProgressTickets;
     }
     
     public void markTicketPrinted(Ticket ticket)
     {
         // Set up the initial connection and statement objects
         Connection conn = null;
         Statement stmt = null;
         // Begin try block so SQL Exceptions can be handled later
         try
         {
             // Register JDBC driver
             Class.forName("com.mysql.jdbc.Driver");
             // Open a connection
             conn = DriverManager.getConnection(DB_URL, USER, PASS);
 
             // Create and Execute the SQL Query
             stmt = conn.createStatement();
             /**
              * Query states: Select all the users in a particular team
              */
             if(ticket.getJobProgress().equals("Issue Reported"))
             {
                 String sql = "UPDATE tickets SET jobProgress = 'Ticket Printed'"
                     + "WHERE jobrefId = ' " + ticket.getJobRefId() + "';";
             stmt.executeUpdate(sql);
             }
             stmt.close();
             conn.close();
         } catch (SQLException se)
         {
 ;            System.out.println(se.getSQLState());
             //Handle errors for JDBC
         } catch (Exception e)
         {
             //Handle errors for Class.forName
         } finally
         {
             //finally block used to close resources should all else fail
             try
             {
                 if (stmt != null )
                 {
                     stmt.close();
                 }
             } catch (SQLException se2)
             {
             }// nothing we can do
 
             try
             {
                 if (conn != null)
                 {
                     conn.close();
                 }
             } catch (SQLException se)
             {
             }
         }
     }
     
     public void markTicketsDuplicate(ArrayList<Ticket> duplicates)
     {
         Iterator<Ticket> it1 = duplicates.iterator();
         while(it1.hasNext())
         {
             Ticket ticketToConsider = it1.next();
             removeTicketDuplicateQueue(ticketToConsider);
             submitTicket(ticketToConsider, false);
             ticketToConsider.setJobProgress("Duplicate Job");
             ticketToConsider.setJobClosed(new DateTime());
             updateTicket(ticketToConsider);
         }
     }
     
     public void markTicketsNonDuplicates(ArrayList<Ticket> duplicates)
     {
         Iterator<Ticket> it1 = duplicates.iterator();
         while(it1.hasNext())
         {
             Ticket ticketToConsider = it1.next();
             submitTicket(ticketToConsider, false);
             removeTicketDuplicateQueue(ticketToConsider);
         }
     }
     
     public void removeTicketDuplicateQueue(Ticket ticket)
     {
         // Set up the initial connection and statement objects
         Connection conn = null;
         Statement stmt = null;
         // Begin try block so SQL Exceptions can be handled later
         try
         {
             // Register JDBC driver
             Class.forName("com.mysql.jdbc.Driver");
             // Open a connection
             conn = DriverManager.getConnection(DB_URL, USER, PASS);
 
             // Create and Execute the SQL Query
             stmt = conn.createStatement();
             /**
              * Query states: Select all the users in a particular team
              */
             String sql = "DELETE FROM duplicateQueue WHERE jobRefId = " + ticket.getJobRefId();
             stmt.executeUpdate(sql);
             stmt.close();
             conn.close();
         } catch (SQLException se)
         {
 ;            System.out.println(se.getSQLState());
             //Handle errors for JDBC
         } catch (Exception e)
         {
             //Handle errors for Class.forName
         } finally
         {
             //finally block used to close resources should all else fail
             try
             {
                 if (stmt != null )
                 {
                     stmt.close();
                 }
             } catch (SQLException se2)
             {
             }// nothing we can do
 
             try
             {
                 if (conn != null)
                 {
                     conn.close();
                 }
             } catch (SQLException se)
             {
             }
         }
     }
     
     private DateTime timeStampToDateTime(String timeStamp)
     {
         if (timeStamp == null)
         {
             return null;
         }
         else
         {
             int year = Integer.parseInt(timeStamp.substring(0, 4));
             int month = Integer.parseInt(timeStamp.substring(5, 7));
             int dayOfMonth = Integer.parseInt(timeStamp.substring(8, 10));
             int hourOfDay = Integer.parseInt(timeStamp.substring(11, 13));
             int minuteOfDay = Integer.parseInt(timeStamp.substring(14, 16));
             return new DateTime(year, month, dayOfMonth, hourOfDay, minuteOfDay);
             }
     }  
 }
