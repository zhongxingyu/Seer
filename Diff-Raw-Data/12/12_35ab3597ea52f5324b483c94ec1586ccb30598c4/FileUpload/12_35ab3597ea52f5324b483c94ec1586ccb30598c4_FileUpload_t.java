 package net.kurochenko.ispub.upload;
 
 import org.springframework.web.multipart.commons.CommonsMultipartFile;
 
 /**
  * User: kurochenko
  * Date: 7/9/11
  * Time: 9:56 PM
  */
 public class FileUpload {
     private String name;
    private CommonsMultipartFile csvFile;
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
    public CommonsMultipartFile getCsvFile() {
        return csvFile;
     }
 
    public void setCsvFile(CommonsMultipartFile csvFile) {
        this.csvFile = csvFile;
     }
 }
