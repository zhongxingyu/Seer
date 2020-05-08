 package systemHibernateManagers;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 //import org.apache.tools.ant.BuildException;
 //import org.apache.tools.ant.Task;
 
 /**
  * generates the hibernate configfile for Charity
  */
 public class GenerateConfig{
 
     //String generateDir;
      
     public static void execute(String DB_Name,String Username,String Password) throws Exception {
     	             
     	//Configuration.class.getResourceAsStream();
     	String FilePath = charityHibernateEntities.User.class.getResource("").getFile()+DB_Name +".cfg.xml";
     	System.out.println("The file path is:"+FilePath);
     	File file = new File(FilePath);
     	//File file = new File("../CharityHibernateConfigurations/"+ DB_Name +".cfg.xml");
         //File file = new File(outputDir + "/" + DB_Name +".cfg.xml");
        
         try {
             BufferedWriter writer = new BufferedWriter(new FileWriter(file));
             
             writer.write("<!DOCTYPE hibernate-configuration PUBLIC \"-//Hibernate/Hibernate Configuration DTD 3.0//EN\" \"http://hibernate.sourceforge.net/hibernate-configuration-3.0.dtd\">\n");
             writer.write("<hibernate-configuration>\n");
             writer.write("<session-factory>\n");
             
             /** Set Properties **/
             writer.write("<property name=\"hibernate.hbm2ddl.auto\">update</property>\n");
             writer.write("<property name=\"hibernate.dialect\">org.hibernate.dialect.MySQLDialect</property>\n");
             writer.write("<property name=\"log4j.logger.org.hibernate.type\">TRACE</property>\n");
             writer.write("<property name=\"hibernate.max_fetch_depth\">3</property>\n");
             writer.write("<property name=\"hibernate.connection.driver_class\">com.mysql.jdbc.Driver</property>\n");
             writer.write("<property name=\"hibernate.ejb.event.post-insert\">org.hibernate.ejb.event.EJB3PostInsertEventListener,org.hibernate.envers.event.AuditEventListener</property>\n");
             writer.write("<property name=\"hibernate.show_sql\" >true</property>\n");
             writer.write("<property name=\"hibernate.ejb.event.post-update\">org.hibernate.ejb.event.EJB3PostUpdateEventListener,org.hibernate.envers.event.AuditEventListener</property>\n");
             writer.write("<property name=\"hibernate.ejb.event.post-delete\">org.hibernate.ejb.event.EJB3PostDeleteEventListener,org.hibernate.envers.event.AuditEventListener</property>\n");   
             writer.write("<property name=\"hibernate.ejb.event.pre-collection-update\">org.hibernate.envers.event.AuditEventListener</property>\n");
             writer.write("<property name=\"hibernate.ejb.event.pre-collection-remove\">org.hibernate.envers.event.AuditEventListener</property>\n");
             writer.write("<property name=\"hibernate.ejb.event.post-collection-recreate\">org.hibernate.envers.event.AuditEventListener</property>\n");
             writer.write("<property name=\"org.hibernate.envers.audit_table_suffix\" >_VER</property>\n");
             writer.write("<property name=\"org.hibernate.envers.revision_field_name\">REV</property>\n");
             writer.write("<property name=\"org.hibernate.envers.revision_type_field_name\">REVTYPE</property>\n");  
             writer.write("<property name=\"org.hibernate.envers.default_schema\">"+DB_Name+"</property>\n");   
             writer.write("<property name=\"org.hibernate.envers.revision_on_collection_change\">false</property>\n");              
             
             /** Set DB Connection Properties **/
             writer.write("<property name=\"hibernate.connection.url\">jdbc:mysql://localhost/"+DB_Name+"</property>\n");
             writer.write("<property name=\"hibernate.connection.username\">"+ Username +"</property>\n");
             writer.write("<property name=\"hibernate.connection.password\">"+ Password +"</property>\n");
             
            /** Bean problem fix **/
            writer.write("<property name=\"javax.persistence.validation.mode\">none</property>");
            
             /** Set Mapping Resources **/
            writer.write("<mapping resource=\"charityHibernateEntities/User.hbm.xml\"/>\n");
            writer.write("<mapping resource=\"charityHibernateEntities/AccessLog.hbm.xml\"/>\n");
            writer.write("<mapping resource=\"charityHibernateEntities/FormFields.hbm.xml\"/>\n");
            writer.write("<mapping resource=\"charityHibernateEntities/MailingGroup.hbm.xml\"/>\n");
            writer.write("<mapping resource=\"charityHibernateEntities/MailingList.hbm.xml\"/>\n");
            writer.write("<mapping resource=\"charityHibernateEntities/Event.hbm.xml\"/>\n");
            writer.write("<mapping resource=\"charityHibernateEntities/FilledForm.hbm.xml\"/>\n");	
            writer.write("<mapping resource=\"charityHibernateEntities/FormType.hbm.xml\"/>\n");
            writer.write("<mapping resource=\"charityHibernateEntities/FieldType.hbm.xml\"/>\n");
            writer.write("<mapping resource=\"charityHibernateEntities/FieldSelection.hbm.xml\"/>\n");
            writer.write("<mapping resource=\"charityHibernateEntities/Form.hbm.xml\"/>\n");
            writer.write("<mapping resource=\"charityHibernateEntities/FormPermissions.hbm.xml\"/>\n");
            writer.write("<mapping resource=\"charityHibernateEntities/UserType.hbm.xml\"/>\n"); 
             writer.write("</session-factory>\n");
             writer.write("</hibernate-configuration>\n");
             writer.flush();
             writer.close();
             
         } catch (Exception e) {
             throw new Exception(e);
         }
     }
 
 
     
 
 }
 
