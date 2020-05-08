 package fi.helsinki.cs.okkopa.mail.writeToDisk;
 
 import fi.helsinki.cs.okkopa.exampaper.ExamPaper;
 import fi.helsinki.cs.okkopa.exception.DocumentException;
 import fi.helsinki.cs.okkopa.pdfprocessor.PDFSplitter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Comparator;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.commons.io.IOUtils;
 import org.apache.pdfbox.exceptions.COSVisitorException;
 import org.jpedal.exception.PdfException;
 
 /**
  *Create a folder per day(if we run it everyday) and save files to folder. lists  all files by date
  *and delete file.
  */
 public class Save implements Saver {
 
     File saveFile;
     //File openFile;
     String folderName;
     private String fileName;
     private Calendar mydate;
 
     public Save() {
         folderName();
         File folder = new File("/cs/fs/home/anttkaik/NetBeansProjects/OKKoPa/" + folderName);
         this.fileName = "" + mydate.get(Calendar.HOUR_OF_DAY) + ":" + mydate.get(Calendar.MINUTE) + ":" + mydate.get(Calendar.SECOND) + ":" + mydate.get(Calendar.MILLISECOND);
         if (!folder.exists()) {
             folder.mkdir();
         }
        //saveFile = new File("/cs/fs/home/anttkaik/NetBeansProjects/OKKoPa/" + folderName + "/" + fileName + ".pdf");
         //openFile = new File("/cs/fs/home/anttkaik/NetBeansProjects/OKKoPa/src/test/resources/pdf/all.pdf");
 
     }
 
     /**
      *delete the youngest file.
      */
     @Override
     public void delete() {
        saveFile = new File("/cs/fs/home/anttkaik/NetBeansProjects/OKKoPa/" + folderName + "/" + fileName + ".pdf");
         if (saveFile.delete()) {
             System.out.println(saveFile.getName() + " is deleted!");
         } else {
             System.out.println("Delete operation is failed.");
         }
 
 
     }
 
     /**
      * Sort all of files in folder by lastModified date..
      *
      * @return sorted list of files in folder
      */
     @Override
     public ArrayList<File> list() {
         File f = new File("/cs/fs/home/anttkaik/NetBeansProjects/OKKoPa/" + folderName + "/");
         File[] files = f.listFiles();
         Arrays.sort(files, new Comparator() {
             @Override
             public int compare(Object o1, Object o2) {
 
                 if (((File) o2).lastModified() > ((File) o1).lastModified()) {
                     return -1;
                 } else if (((File) o2).lastModified() < ((File) o1).lastModified()) {
                     return +1;
                 } else {
                     return 0;
                 }
             }
         });
         ArrayList<File> list = new ArrayList();
         for (int i = 0; i < files.length; i++) {
 
             if (files[i].isFile()) {
                 list.add(files[i]);
                
             }
         }
         
         System.out.println(list);
         return list;
       
     }
 
     /**
      *Save ExamPapers to local disk. 
      *
      * @param examPaper,
      */
     @Override
     public void saveExamPaper(ExamPaper examPaper) {
 
 
 //           saveFile = new File("/cs/fs/home/tirna/OKKoPa/OKKoPa/"+ folderName+"/"+fileName+".pdf");
 
         // saveFile = new File("/cs/fs/home/tirna/OKKoPa/OKKoPa "+ folderName + fileName);
 //        String fileName = ""+System.currentTimeMillis();
 //        File saveFile = new File("/cs/fs/home/tirna/OKKoPa/OKKoPa/" + folderName() + "/" + fileName + ".pdf");
 
         FileOutputStream outputStream = null;
         try {
             outputStream = new FileOutputStream(saveFile);
             IOUtils.copy(examPaper.getPdf(), outputStream);       
             outputStream.close();
             examPaper.getPdf().close();
         } catch (IOException ex) {
             Logger.getLogger(Save.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private String folderName() {
         mydate = Calendar.getInstance();
         mydate.setTimeInMillis(System.currentTimeMillis());
         folderName = "fails/"+mydate.get(Calendar.DAY_OF_MONTH) + "." + mydate.get(Calendar.MONTH) + "." + mydate.get(Calendar.YEAR);
         return folderName;
     }
 
     //testiä
 //    public static void main(String[] args) throws FileNotFoundException, IOException {
 //        Save save = new Save();
 //        PDFSplitter splitter = new PDFSplitter();
 //        List<ExamPaper> papers = null;
 //        try {
 //            papers = splitter.splitToExamPapersWithPDFStreams(new FileInputStream(save.openFile));
 //        } catch (DocumentException ex) {
 //            Logger.getLogger(Save.class.getName()).log(Level.SEVERE, null, ex);
 //        } catch (PdfException ex) {
 //            Logger.getLogger(Save.class.getName()).log(Level.SEVERE, null, ex);
 //        } catch (COSVisitorException ex) {
 //            Logger.getLogger(Save.class.getName()).log(Level.SEVERE, null, ex);
 //        }
 //        save.saveExamPaper(papers.get(0));
 //        Save save2 = new Save();
 //        save2.saveExamPaper(papers.get(1));
 //        Save save3 = new Save();
 //        save3.saveExamPaper(papers.get(5));
 //
 //        save.list();
 //        save.delete();
 //
 //    }
 }
