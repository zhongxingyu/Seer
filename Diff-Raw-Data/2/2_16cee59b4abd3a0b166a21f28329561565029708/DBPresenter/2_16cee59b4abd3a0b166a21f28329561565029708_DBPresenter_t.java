 package suncertify.parser;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import suncertify.constants.Variables;
 import suncertify.db.RecordNotFoundException;
 
 public class DBPresenter {
   private static final Logger log = LoggerFactory.getLogger(DBPresenter.class);
   private static DBPresenter instance;
 
   private String dbPath;
   private int magicCookie;
   private byte[] fileHeader;
   private int fieldsNumber;
   private List<DBRecord> records;
   private long newRecordNumber;
 
   private DBPresenter(String dbPath) {
     this.dbPath = dbPath;
   }
 
   public static synchronized DBPresenter getInstance() {
     if (instance == null || !instance.getDbPath().equals(PropertiesLoader.getInstance().getDBLocation())) {
       instance = new DBPresenter(PropertiesLoader.getInstance().getDBLocation());
       DBReaderWriter.createDatabasePresenter();
       log.info("worked with: " + Variables.getWorkedFilePath());
     }
     return instance;
   }
 
   public long getMagicCookie() {
     return magicCookie;
   }
 
   public void setMagicCookie(int magicCookie) {
     this.magicCookie = magicCookie;
   }
 
   public void setFileHeader(byte[] fileHeader) {
     this.fileHeader = fileHeader;
   }
 
   public byte[] getFileHeader() {
     return fileHeader;
   }
 
   public long getFieldsNumber() {
     return fieldsNumber;
   }
 
   public void setFieldsNumber(int fieldsNumber) {
     this.fieldsNumber = fieldsNumber;
   }
 
   public List<DBRecord> getRecords() {
     if (records == null) {
       records = new ArrayList<DBRecord>();
     }
     return records;
   }
 
   public void setRecords(List<DBRecord> records) {
     this.records = records;
   }
 
   public String getDbPath() {
     return dbPath;
   }
 
   public void setNewRecordNumber(long newRecordNumber) {
     this.newRecordNumber = newRecordNumber;
   }
 
   public long getNewRecordNumber() {
     return newRecordNumber;
   }
 
   public void addRecord(DBRecord record) {
     record.setPosition(newRecordNumber++);
     getRecords().add(record);
   }
 
   public void updateRecord(DBRecord record) {
     getRecords().add(record);
   }
 
   public void deleteRecord(DBRecord record) {
     getRecords().remove(record);
   }
 
   public DBRecord getRecord(long recNo) throws RecordNotFoundException {
     for (DBRecord record : getRecords()) {
      if (record.getPosition() == recNo && record.isValid()) {
         return record;
       }
     }
     throw new RecordNotFoundException("No record found with number: " + recNo);
   }
 
   @Override
   public String toString() {
     StringBuffer sb = new StringBuffer();
     sb.append("DBPresenter[");
     sb.append("dbPath=").append(dbPath);
     sb.append(", magicCookie=").append(magicCookie);
     sb.append(", fieldsNumber=").append(fieldsNumber).append("\n");
     for (DBRecord record : getRecords()) {
       sb.append(", ").append(record).append("\n");
     }
     return sb.toString();
   }
 }
