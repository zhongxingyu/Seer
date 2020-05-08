 package pl.remadag.mendeley.bookparser.runme;
 
 import com.mendeley.oapi.schema.Author;
 import com.mendeley.oapi.schema.Document;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 /**
  * Generates files after searching.
  */
 public class FileGeneratorUtil {
 
     protected static String createFilesLocationPrefix() {
         return "files/" + RunMe.DOC_DIR + "/";
     }
 
 
     public void createRelatedDocsFile(Map<String, Set<String>> relatedDocMap, String filesLocationPrefix) throws IOException {
         String fileName = "relatedDocs.txt";
         final String pathName = filesLocationPrefix + fileName;
         File relatedDocsFile = new File(pathName);
         boolean exist = relatedDocsFile.createNewFile();
         if (exist) {
             FileWriter fstream = new FileWriter(pathName);
             BufferedWriter out = new BufferedWriter(fstream);
             for (String docTitle : relatedDocMap.keySet()) {
                 out.newLine();
                 out.write(docTitle);
                 for (String relatedDocTitle : relatedDocMap.get(docTitle)) {
                     out.newLine();
                     out.write("> " + relatedDocTitle);
                 }
                out.close();
             }
             System.out.println("File " + fileName + " in location " + relatedDocsFile.getAbsolutePath() + " created successfully.");
         } else {
             System.out.println("EXCEPTION File for related docs already exists.");
         }
     }
 
     public void createFilesForTerm(String term, Map<String, Set<Document>> sortedDocMap, String filesLocationPrefix) throws IOException {
         for (String counter : sortedDocMap.keySet()) {
             String fileNameTerm;
             if (!term.contains("r_")) {
                 fileNameTerm = "a_" + term;
             } else {
                 fileNameTerm = term;
             }
             String fileName = fileNameTerm + "_" + counter + ".txt";
             final String pathName = filesLocationPrefix + fileName;
             File file = new File(pathName);
             Set<Document> sortedDoc = sortedDocMap.get(counter);
             boolean exist = file.createNewFile();
             if (exist) {
                 FileWriter fstream = new FileWriter(pathName);
                 BufferedWriter out = new BufferedWriter(fstream);
                 for (Document d : sortedDoc) {
                     out.write(d.getTitle() + " | " + getAuthors(d.getAuthors()));
                     out.newLine();
                     out.write("\to czestotliwosci " + counter);
                     out.newLine();
                 }
                 out.close();
                 System.out.println("File " + fileName + " in location " + file.getAbsolutePath() + " created successfully.");
             } else {
                 System.out.println("EXCEPTION File already exists.");
             }
         }
     }
 
     private String getAuthors(final List<Author> authors) {
         StringBuilder builder = new StringBuilder();
         for (Author author : authors) {
             builder.append("Autor: ").append(author.getSurname()).append(" ").append(author.getForename()).append(", ");
         }
         return builder.toString();
     }
 
 
     public void createDocsWithFitsAllCriteria(Map<String, Integer> fitsCriteriaDocsMap, String filesLocationPrefix) throws IOException {
 
         if (fitsCriteriaDocsMap.isEmpty()) {
             fitsCriteriaDocsMap.put("Test title", 5);
         }
 
         String fileName = "correctDocs.txt";
         final String pathName = filesLocationPrefix + fileName;
         File docsFile = new File(pathName);
         boolean exist = docsFile.createNewFile();
         if (exist) {
             FileWriter fstream = new FileWriter(pathName);
             BufferedWriter out = new BufferedWriter(fstream);
             out.write("Liczba wszystkich ksiazek: " + fitsCriteriaDocsMap.size());
             out.newLine();
 
             for (String title : fitsCriteriaDocsMap.keySet()) {
                 out.write(title + " | Częstotliwość: " + fitsCriteriaDocsMap.get(title));
                 out.newLine();
             }
 
             out.close();
             System.out.println("File " + fileName + " in location " + docsFile.getAbsolutePath() + " created successfully.");
         } else {
             System.out.println("EXCEPTION File for related docs already exists.");
         }
     }
 }
