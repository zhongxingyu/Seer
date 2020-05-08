 package models;
 
 import java.io.BufferedInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Map;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.persistence.Entity;
 import javax.persistence.EnumType;
 import javax.persistence.Enumerated;
 import javax.persistence.ManyToOne;
 import javax.persistence.OneToMany;
 import javax.persistence.OrderBy;
 import javax.persistence.Transient;
 
 import org.apache.commons.codec.binary.Hex;
 import org.apache.commons.io.FileUtils;
 
 import play.Logger;
 import play.Play;
 import play.db.jpa.Model;
 import play.exceptions.UnexpectedException;
 import play.i18n.Messages;
 import play.libs.Codec;
 import play.libs.IO;
 import play.vfs.VirtualFile;
 
 @Entity
 public class Import extends Model {
 
     @ManyToOne
     public Referential referential;
 
     @Enumerated(EnumType.STRING)
     public Type type;
 
     @OneToMany(mappedBy="importEntity")
     @OrderBy("logDate desc")
     public List<ImportLog> logs = new ArrayList<ImportLog>();
 
     public String originalFileName;
     public String fileName;
     public Long fileSize;
     public String fileHash;
 
     @Transient
     public File file;
 
     public Date depositDate = new Date();
     public Date importStart = null;
     public Date importStop = null;
 
     public Long importLine = 0L;
 
     public static String path = "/data/imports/";
 
     public File getFile() {
         return Play.getFile(path + this.fileName);
     }
 
     public void setFile(File file) throws IOException, NoSuchAlgorithmException {
         String storedFileName = Codec.UUID();
         Play.getFile(path).mkdirs();
         File storedFile = Play.getFile(path + storedFileName);
         storedFile.createNewFile();
         FileUtils.copyFile(file, storedFile);
         this.originalFileName = file.getName();
         this.fileSize = file.length();
         this.fileName = storedFileName;
         this.fileHash = hashFile(file);
     }
 
     public static Import queue(Referential referential, File csvFile, Type type) throws IOException {
         Import currentImport = new Import();
         currentImport.type = type;
         currentImport.referential = referential;
         currentImport.file = csvFile;
         currentImport.save();
         return currentImport;
     }
 
     public static String hashFile(File file) throws NoSuchAlgorithmException, IOException {
         MessageDigest md = MessageDigest.getInstance("SHA-1");
         BufferedInputStream in = new BufferedInputStream(new FileInputStream(file));
         int theByte = 0;
         while ((theByte = in.read()) != -1) {
             md.update((byte) theByte);
         }
         in.close();
         return String.valueOf(Hex.encodeHex(md.digest()));
     }
 
     public void log(ImportLog.Error error, String message, String jsonObject) {
        new ImportLog(this, error, message, jsonObject).save();
     }
 
     public static Import findFirstInQueue() {
         return find("importStop is null order by importStart").first();
     }
 
     public static boolean fileExists(File file) throws NoSuchAlgorithmException, IOException {
         return find("fileHash = ?", hashFile(file)).first() != null;
     }
 
     public void alreadyExists(String jsonObject) {
         this.log(ImportLog.Error.ALREADY_EXISTS, Messages.get("alreadyExists"), jsonObject);
     }
 
     public void badFormat(String keyMessage, String jsonObject) {
         this.log(ImportLog.Error.FORMAT, Messages.get(keyMessage), jsonObject);
     }
 
     public static enum Type {
         CITY("Cities"), WAY("Ways");
 
         String label;
 
         Type(String label) {
             this.label = label;
         }
 
         @Override
         public String toString() {
             return this.label;
         }
     }
 
 }
