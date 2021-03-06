 /**********************************************************************
  * $Source: /cvsroot/syntax/syntax/src/de/willuhn/jameica/fibu/server/DBSupportMySqlImpl.java,v $
 * $Revision: 1.5 $
 * $Date: 2006/11/17 00:11:20 $
  * $Author: willuhn $
  * $Locker:  $
  * $State: Exp $
  *
  * Copyright (c) by willuhn.webdesign
  * All rights reserved
  *
  **********************************************************************/
 
 package de.willuhn.jameica.fibu.server;
 
 import java.io.File;
 import java.io.FileReader;
 import java.rmi.RemoteException;
 import java.sql.Connection;
 import java.sql.DriverManager;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 
 import de.willuhn.jameica.fibu.Fibu;
 import de.willuhn.jameica.fibu.rmi.DBSupport;
 import de.willuhn.jameica.messaging.StatusBarMessage;
 import de.willuhn.jameica.plugin.PluginResources;
 import de.willuhn.jameica.system.Application;
 import de.willuhn.logging.Logger;
 import de.willuhn.sql.ScriptExecutor;
 import de.willuhn.util.ApplicationException;
 import de.willuhn.util.ProgressMonitor;
 
 /**
  * Implementierung des MySQL-Supports.
  */
 public class DBSupportMySqlImpl extends AbstractDBSupportImpl implements
     DBSupport
 {
 
   /**
    * @throws RemoteException
    */
   public DBSupportMySqlImpl() throws RemoteException
   {
     super();
   }
 
   /**
    * @see de.willuhn.jameica.fibu.rmi.DBSupport#create(de.willuhn.util.ProgressMonitor)
    */
   public void create(ProgressMonitor monitor) throws RemoteException, ApplicationException
   {
     String dbname   = getDatabaseName();
     String username = getUsername();
     String hostname = getHostname();
     int port        = getTcpPort();
 
     if (dbname == null || dbname.length() == 0)
       throw new ApplicationException(i18n.tr("Bitte geben Sie den Namen der Datenbank an"));
 
     if (username == null || username.length() == 0)
       throw new ApplicationException(i18n.tr("Bitte geben Sie einen Benutzernamen an"));
 
     if (hostname == null || hostname.length() == 0)
       throw new ApplicationException(i18n.tr("Bitte geben Sie einen Hostnamen fr die Datenbank an"));
 
     if (port <= 0 || port > 65535)
       throw new ApplicationException(i18n.tr("Bitte geben Sie einen gltigen TCP-Port ein"));
 
 
     PluginResources res = Application.getPluginLoader().getPlugin(Fibu.class).getResources();
     File create = new File(res.getPath() + File.separator + "sql" + File.separator + "create_mysql.sql");
     File init   = new File(res.getPath() + File.separator + "sql" + File.separator + "init.sql");
 
     Connection conn = null;
     ResultSet rs    = null;
     try
     {
       try
       {
         Class.forName(getJdbcDriver());
       }
       catch (Throwable t)
       {
         Logger.error("unable to load jdbc driver",t);
         throw new ApplicationException(i18n.tr("Fehler beim Laden des JDBC-Treibers. {0}",t.getLocalizedMessage()));
       }
       
       String jdbcUrl = getJdbcUrl();
       Logger.info("using jdbc url: " + jdbcUrl);
 
       try
       {
         conn = DriverManager.getConnection(jdbcUrl,username,getPassword());
       }
       catch (SQLException se)
       {
         Logger.error("unable to open sql connection",se);
         throw new ApplicationException(i18n.tr("Fehler beim Aufbau der Datenbankverbindung. {0}",se.getLocalizedMessage()));
       }
       
       // Wir schauen mal, ob vielleicht schon Tabellen existieren
       rs = conn.getMetaData().getTables(null,null,null,null);
       if (rs.next())
       {
         Logger.warn("database seems to exist, skip database creation");
         String msg = i18n.tr("Datenbank existiert bereits. berspringe Erstellung");
         monitor.setStatusText(msg);
         monitor.setPercentComplete(100);
         Application.getMessagingFactory().sendMessage(new StatusBarMessage(msg, StatusBarMessage.TYPE_SUCCESS));
       }
       else
       {
        ScriptExecutor.execute(new FileReader(create),conn, monitor);
         monitor.setPercentComplete(0);
        ScriptExecutor.execute(new FileReader(init),conn, monitor);
         monitor.setStatusText(i18n.tr("Datenbank erfolgreich eingerichtet"));
       }
 
     }
     catch (Throwable t)
     {
       Logger.error("unable to execute sql scripts",t);
       throw new ApplicationException(i18n.tr("Fehler beim Initialisieren der Datenbank. {0}", t.getLocalizedMessage()),t);
     }
     finally
     {
       if (rs != null)
       {
         try
         {
           rs.close();
         }
         catch (Throwable t)
         {
           Logger.error("unable to close resultset",t);
         }
       }
       if (conn != null)
       {
         try
         {
           conn.close();
         }
         catch (Throwable t)
         {
           Logger.error("unable to close connection",t);
         }
       }
     }
   }
 
   /**
    * @see de.willuhn.jameica.fibu.rmi.DBSupport#executeSQLScript(java.io.File)
    */
   public void executeSQLScript(File f) throws RemoteException, ApplicationException
   {
 //    VelocityContext context = new VelocityContext();
 //
 //    context.put("sql",new Math());
 //
 //    BufferedWriter writer = null;
 //    try
 //    {
 //      writer = new BufferedWriter(new OutputStreamWriter(export.getTarget()));
 //
 //      Template t = Velocity.getTemplate("template.vm");
 //      t.merge(context,writer);
 //    }
 //    catch (Exception e)
 //    {
 //      Logger.error("error while writing into velocity file " + template,e);
 //      throw new ApplicationException(i18n.tr("Fehler beim Schreiben in die Export-Datei"));
 //    }
 //    finally
 //    {
 //      if (writer != null)
 //      {
 //        try
 //        {
 //          writer.close();
 //        }
 //        catch (Exception e)
 //        {
 //          Logger.error("error while closing outputstream",e);
 //        }
 //      }
 //    }
 //    
 //    ScriptExecutor.execute(new FileReader(f),conn, monitor);
   }
 
 
   /**
    * @see de.willuhn.jameica.fibu.rmi.DBSupport#getName()
    */
   public String getName() throws RemoteException
   {
     return i18n.tr("MySQL 4.0 oder hher");
   }
 
   /**
    * @see de.willuhn.jameica.fibu.rmi.DBSupport#needsDatabaseName()
    */
   public boolean needsDatabaseName() throws RemoteException
   {
     return true;
   }
   
   /**
    * @see de.willuhn.jameica.fibu.rmi.DBSupport#needsHostname()
    */
   public boolean needsHostname() throws RemoteException
   {
     return true;
   }
   
   /**
    * @see de.willuhn.jameica.fibu.rmi.DBSupport#needsPassword()
    */
   public boolean needsPassword() throws RemoteException
   {
     return true;
   }
   
   /**
    * @see de.willuhn.jameica.fibu.rmi.DBSupport#needsTcpPort()
    */
   public boolean needsTcpPort() throws RemoteException
   {
     return true;
   }
   
   /**
    * @see de.willuhn.jameica.fibu.rmi.DBSupport#needsUsername()
    */
   public boolean needsUsername() throws RemoteException
   {
     return true;
   }
 
   /**
    * @see de.willuhn.jameica.fibu.rmi.DBSupport#getJdbcUrl()
    */
   public String getJdbcUrl() throws RemoteException
   {
     return "jdbc:mysql://" + getHostname() + ":" + getTcpPort() + "/" + getDatabaseName() + "?dumpQueriesOnException=true&amp;useUnicode=true&amp;characterEncoding=ISO8859_1";
   }
 
   /**
    * @see de.willuhn.jameica.fibu.rmi.DBSupport#getJdbcDriver()
    */
   public String getJdbcDriver() throws RemoteException
   {
     return "com.mysql.jdbc.Driver";
   }
 
   /**
    * @see de.willuhn.jameica.fibu.rmi.DBSupport#getSQLTimestamp(java.lang.String)
    */
   public String getSQLTimestamp(String content) throws RemoteException
   {
     return "(UNIX_TIMESTAMP({0})*1000)".replaceAll("\\{0\\}",content);
   }
 }
 
 
 /*********************************************************************
  * $Log: DBSupportMySqlImpl.java,v $
  * Revision 1.5  2006/11/17 00:11:20  willuhn
  * *** empty log message ***
  *
  * Revision 1.4  2006/06/29 15:11:31  willuhn
  * @N Setup-Wizard fertig
  * @N Auswahl des Geschaeftsjahres
  *
  * Revision 1.3  2006/06/19 16:25:42  willuhn
  * *** empty log message ***
  *
  * Revision 1.2  2006/06/13 22:52:10  willuhn
  * @N Setup wizard redesign and code cleanup
  *
  * Revision 1.1  2006/06/12 23:05:47  willuhn
  * *** empty log message ***
  *
  **********************************************************************/
