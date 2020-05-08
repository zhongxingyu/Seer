 package gov.nih.nci.ncicb.cadsr.loader;
 
 import gov.nih.nci.ncicb.cadsr.loader.util.BeansAccessor;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 
 import javax.sql.DataSource;
 
 /**
  * <code>Unclassifier</code> is a convenience script for unclassifying all
  * AC's, alt names and definitions from a previous model load.  
  *
  * @author <a href="mailto:rokickik@mail.nih.gov">Konrad Rokicki</a>
  */
 public class Unclassifier {
 
   private static final String DEL_DEFINITIONS_SQL = 
       "delete from definitions where defin_idseq in (" +
       "  select defin_idseq from definitions ATT " + 
       "   join AC_ATT_CSCSI_EXT att_csi on att.defin_idseq = att_csi.att_idseq " + 
       "   join CS_CSI csCsi on att_csi.cs_csi_idseq = csCsi.cs_csi_idseq " + 
       "   join classification_schemes cs on cs.cs_idseq = csCsi.cs_idseq " + 
      "  where cs.long_name = ? and cs.Version = ? " + 
       "  and 2 = (select count(*) from AC_ATT_CSCSI_EXT ACC " +
       "           where ACC.ATT_IDSEQ = ATT.defin_idseq)" +
       ")";
   
   private static final String DEL_ALTNAMES_SQL = 
       "delete from designations where desig_idseq in (" + 
       "  select desig_idseq from designations ATT " + 
       "   join AC_ATT_CSCSI_EXT att_csi on att.desig_idseq = att_csi.att_idseq " + 
       "   join CS_CSI csCsi on att_csi.cs_csi_idseq = csCsi.cs_csi_idseq " + 
       "   join classification_schemes cs on cs.cs_idseq = csCsi.cs_idseq " + 
      "  where cs.long_name = ? and cs.Version = ? " + 
       "  and 2 = (select count(*) from AC_ATT_CSCSI_EXT ACC " +
       "           where ACC.ATT_IDSEQ = ATT.desig_idseq) " + 
       ")";
 
   private static final String DEL_ATTRS_SQL = 
       "delete from AC_ATT_CSCSI_EXT where CS_CSI_IDSEQ in ( " + 
       "  select CS_CSI_IDSEQ from CS_CSI where CS_IDSEQ in ( " + 
       "   select CS_IDSEQ from Classification_schemes " +
      "   where long_name = ? and Version = ? " + 
       "  ) " + 
       ")";
   
   private static final String DEL_ACS_SQL = 
       "delete from AC_CSI where CS_CSI_IDSEQ in ( " + 
       "  select CS_CSI_IDSEQ from CS_CSI where CS_IDSEQ in ( " + 
       "   select CS_IDSEQ from Classification_schemes " +
      "   where long_name = ? and Version = ? " + 
       "  ) " + 
       ")";
 
   private DataSource dataSource;
   
   /**
    *
    * @param args a <code>String[]</code> value
    * @exception Exception if an error occurs
    */
   public static void main(String[] args) throws Exception {
 
     if(args.length != 2) {
       System.err.println("unclassify [CS name] [CS version]");
       System.exit(1);
     }
 
     Float projectVersion = null;
     try {
       projectVersion = new Float(args[1]);
     } catch (NumberFormatException ex) {
       System.err.println("Parameter projectVersion must be a number");
       System.exit(1);
     }
 
     Unclassifier loader = BeansAccessor.getUnclassifier();
     loader.run(args[0], projectVersion);
   }
 
   private void run(String csName, Float csVersion) throws Exception {
 
     // Refresh the views
     Connection conn = null;
     PreparedStatement stmt = null;
     try {
         System.out.println("Starting unclassification");
       
         conn = dataSource.getConnection();
         conn.setAutoCommit(false);
 
         System.out.println("Deleting definitions");
         stmt = conn.prepareStatement(DEL_DEFINITIONS_SQL);
         stmt.setString(1, csName);
        stmt.setFloat(2, csVersion);
         int definitionsDeleted = stmt.executeUpdate();
         System.out.println("  "+definitionsDeleted+" deleted");
 
         System.out.println("Deleting alt names");
         stmt = conn.prepareStatement(DEL_ALTNAMES_SQL);
         stmt.setString(1, csName);
        stmt.setFloat(2, csVersion);
         int altNamesDeleted = stmt.executeUpdate();
         System.out.println("  "+altNamesDeleted+" deleted");
         
         System.out.println("Deleting attribute classifications");
         stmt = conn.prepareStatement(DEL_ATTRS_SQL);
         stmt.setString(1, csName);
        stmt.setFloat(2, csVersion);
         int attrDeleted = stmt.executeUpdate();
         System.out.println("  "+attrDeleted+" deleted");
 
         System.out.println("Deleting AC classifications");
         stmt = conn.prepareStatement(DEL_ACS_SQL);
         stmt.setString(1, csName);
        stmt.setFloat(2, csVersion);
         int acDeleted = stmt.executeUpdate();
         System.out.println("  "+acDeleted+" deleted");
         
         conn.commit();
         System.out.println("Unclassification complete");
     }
     catch (SQLException e) {
         if (conn != null) {
           System.err.println("Error encountered, rolling back changes.");
           conn.rollback();
         }
         e.printStackTrace();
         System.exit(1);
     }
     finally {
         if (conn != null) {
             conn.close();
         }
     }
     
     System.exit(0);
   }
 
   public void setDataSource(DataSource dataSource) {
     this.dataSource = dataSource;
   }
 }
