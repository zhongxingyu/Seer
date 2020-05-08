 package basis.migration.service;
 
 import java.text.ParseException;
 import java.text.ParsePosition;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 /**
  * Created with IntelliJ IDEA.
  * User: gregorysebert
  * Date: 14/12/12
  * Time: 11:11
  * To change this template use File | Settings | File Templates.
  */
 public class Gbdo {
 
     public static BasisDocument getBasisDoc(String BO, Mapping mapping, String BOCountPattern)  {
         BasisDocument basisDoc= new BasisDocument();
 
         String dosNum = MigrationUtil.checkDosNum(mapping.getDOSNUM(), mapping.getDOCIDT(), BOCountPattern);
 
         basisDoc.setDocId(BO+"."+dosNum+"-"+mapping.getDOCNUM());
 
        SimpleDateFormat formatter = new SimpleDateFormat("yyyyddMM");
         ParsePosition pos = new ParsePosition(0);
         Date sysdate = null;
         try {
             sysdate = formatter.parse(mapping.getSYSDAT().trim());
         } catch (ParseException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
         basisDoc.setDocReference(mapping.getZAAK());
         basisDoc.setSysDate(sysdate);
         basisDoc.setDocDate(sysdate);
         basisDoc.setDocExternSenderAdress(mapping.getAFSENDER());
 
         String comment ="";
         if (mapping.getKLASSEMENT()!=null)
             comment =  "Klassement :" + mapping.getKLASSEMENT()+ "<BR>";
         if ( mapping.getONDERWERP()!=null)
             comment =  "Onderwerp :" + mapping.getONDERWERP()+ "<BR>";
         if (mapping.getREFERENCIE()!=null)
             comment =  "Referentie :" + mapping.getREFERENCIE()+ "<BR>";
         if (mapping.getBESTEMMELING()!=null)
             comment =  "Bestemmeling :" + mapping.getBESTEMMELING()+ "<BR>";
         if (mapping.getOPSTELER()!=null)
             comment =  "Opsteller :" + mapping.getOPSTELER()+ "<BR>";
         if (mapping.getTYPEDOC()!=null)
             comment =  "Type document :" + mapping.getTYPEDOC()+ "<BR>";
         if (mapping.getDOCIDT()!=null)
             comment =  "Document IDT :" + mapping.getDOCIDT()+ "<BR>";
 
         basisDoc.setDocComments(comment);
 
         basisDoc.setDocKeywords(mapping.getSLEUTELWORDEN());
 
         return basisDoc;
     }
 
     public static BasisFolder getBasisFolder(String BO,Mapping mapping, String BOCountPattern)
     {
         BasisFolder basisFolder= new BasisFolder();
         String dosNum = MigrationUtil.checkDosNum(mapping.getDOSNUM(), mapping.getDOCIDT(), BOCountPattern);
 
         basisFolder.setFolderId(BO+"."+dosNum);
 
         String comment ="";
         if (mapping.getKLASSEMENT()!=null)
             comment =  "Klassement :" + mapping.getKLASSEMENT()+ "<BR>";
         if ( mapping.getONDERWERP()!=null)
             comment =  "Onderwerp :" + mapping.getONDERWERP()+ "<BR>";
         if (mapping.getREFERENCIE()!=null)
             comment =  "Referentie :" + mapping.getREFERENCIE()+ "<BR>";
         if (mapping.getBESTEMMELING()!=null)
             comment =  "Bestemmeling :" + mapping.getBESTEMMELING()+ "<BR>";
         if (mapping.getOPSTELER()!=null)
             comment =  "Opsteller :" + mapping.getOPSTELER()+ "<BR>";
         if (mapping.getTYPEDOC()!=null)
             comment =  "Type document :" + mapping.getTYPEDOC()+ "<BR>";
         if (mapping.getDOCIDT()!=null)
             comment =  "Document IDT :" + mapping.getDOCIDT()+ "<BR>";
 
         basisFolder.setFolderComments(comment);
 
         return basisFolder;
     }
 
     public static BasisFiche getBasisFiche(String BO,Mapping mapping)
     {
         BasisFiche basisFiche= new BasisFiche();
         basisFiche.setFicheId("FU-000");
         basisFiche.setFollowRequiredAction("Import Basis");
         SimpleDateFormat formatter = new SimpleDateFormat("yyyyddMM");
         Date sysdate = null;
         try {
             sysdate = formatter.parse(mapping.getSYSDAT());
         } catch (ParseException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         }
 
         basisFiche.setFollowAnswerByDate(sysdate);
         return basisFiche;
     }
 
 }
