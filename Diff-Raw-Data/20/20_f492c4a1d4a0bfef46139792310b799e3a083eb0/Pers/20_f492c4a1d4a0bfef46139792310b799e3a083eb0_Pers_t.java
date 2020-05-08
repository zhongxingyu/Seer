 package basis.migration.service;
 
 import java.text.ParseException;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * Created with IntelliJ IDEA.
  * User: gregorysebert
  * Date: 14/12/12
  * Time: 11:07
  * To change this template use File | Settings | File Templates.
  */
 public class Pers {
 
     public static BasisDocument getBasisDoc(String BO, Mapping mapping, String BOCountPattern)  {
         BasisDocument basisDoc= new BasisDocument();
 
         String dosNum = MigrationUtil.checkDosNum(mapping.getDOSNUM(), mapping.getDOCIDT(), BOCountPattern);
 
         basisDoc.setDocId(BO+"."+dosNum+"-"+mapping.getDOCNUM());
 
         SimpleDateFormat formatter = new SimpleDateFormat("yyyyMMdd");
         ParsePosition pos = new ParsePosition(0);
         Date sysdate = null;
         try {
             sysdate = formatter.parse(mapping.getSYSDAT().trim());
         } catch (ParseException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         if (mapping.getSTAMNUMMER()!=null) basisDoc.setDocReference(mapping.getSTAMNUMMER());
         else basisDoc.setDocReference("");
 
         String comment = "";
 
         if (mapping.getOPSTELER()!=null)
         {
             comment = comment + "Opsteller :" +  mapping.getOPSTELER() + "<BR>";
         }
 
         if (mapping.getAFSENDER()!=null)
         {
            comment = comment + "Afzender :" +  mapping.getAFSENDER() + "<BR>";
         }
 
         if (mapping.getSTAMNUMMER()!=null)
         {
             comment = comment + "Stamnummer/matricule :" +  mapping.getSTAMNUMMER() + "<BR>";
         }
 
 
         basisDoc.setDocComments(comment);
 
         basisDoc.setSysDate(sysdate);
         return basisDoc;
     }
 
     public static BasisFolder getBasisFolder(String BO,Mapping mapping,String BOCountPattern)
     {
         BasisFolder basisFolder= new BasisFolder();
         String dosNum = MigrationUtil.checkDosNum(mapping.getDOSNUM(), mapping.getDOCIDT(), BOCountPattern);
 
         basisFolder.setFolderId(BO+"."+dosNum);
         basisFolder.setFolderExternalReference(mapping.getZAAK());
 
         String comment = "";
 
         if (mapping.getOPSTELER()!=null)
         {
             comment = comment + "Opsteller :" +  mapping.getOPSTELER() + "<BR>";
         }
 
         if (mapping.getAFSENDER()!=null)
         {
            comment = comment + "Afzender :" +  mapping.getAFSENDER() + "<BR>";
         }
 
         if (mapping.getSTAMNUMMER()!=null)
         {
             comment = comment + "Stamnummer/matricule :" +  mapping.getSTAMNUMMER() + "<BR>";
         }
 
 
         basisFolder.setFolderComments(comment);
 
         return basisFolder;
     }
 
     public static BasisFiche getBasisFiche(String BO,Mapping mapping)
     {
         BasisFiche basisFiche= new BasisFiche();
         basisFiche.setFicheId("FU-000");
         basisFiche.setFollowRequiredAction("Import Basis");
         SimpleDateFormat formatter = new SimpleDateFormat("yyyyddMM");
         ParsePosition pos = new ParsePosition(0);
         Date sysdate = null;
         try {
             sysdate = formatter.parse(mapping.getSYSDAT().trim());
         } catch (ParseException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         basisFiche.setFollowAnswerByDate(sysdate);
         return basisFiche;
     }
 
 }
