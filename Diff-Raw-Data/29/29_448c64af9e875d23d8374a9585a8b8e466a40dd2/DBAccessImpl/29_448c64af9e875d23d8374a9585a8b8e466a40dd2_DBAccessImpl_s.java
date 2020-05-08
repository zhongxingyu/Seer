 package suncertify.db;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.util.ArrayList;
import java.util.Arrays;
 import java.util.List;
 
import suncertify.constants.StringPool;

import suncertify.parser.DBRecord;

 import suncertify.parser.DBPresenter;
 import suncertify.parser.DBReaderWriter;
 import suncertify.parser.DBRecordHelper;
 
 public class DBAccessImpl implements DBAccess {
   private static final Logger log = LoggerFactory.getLogger(DBAccessImpl.class);
 
   @Override
   public String[] readRecord(long recNo) throws RecordNotFoundException {
     DBPresenter presenter = DBPresenter.getInstance();
     DBRecord record = presenter.getRecord(recNo);
     return DBRecordHelper.getDBRecordAsStringArray(record);
   }
 
   @Override
   public void updateRecord(long recNo, String[] data, long lockCookie) throws RecordNotFoundException, SecurityException {
     DBPresenter presenter = DBPresenter.getInstance();
     DBRecord oldRecord = presenter.getRecord(recNo);
     if (oldRecord.isLocked() && oldRecord.getCookie() != lockCookie) {
       throw new SecurityException("The record " + recNo + " with cookie " + oldRecord.getCookie() + " can't be updated with cookie " + lockCookie);
     }
     DBRecord newRecord = DBRecordHelper.createDBRecord(data);
     try {
       DBReaderWriter.updateRecord(oldRecord, newRecord);
     } catch (Exception e) {
       log.info(e.getMessage(), e);
     }
   }
 
   @Override
   public void deleteRecord(long recNo, long lockCookie) throws RecordNotFoundException, SecurityException {
     DBPresenter presenter = DBPresenter.getInstance();
     DBRecord record = presenter.getRecord(recNo);
     if (record.isLocked() && record.getCookie() != lockCookie) {
       throw new SecurityException("The record " + recNo + " with cookie " + record.getCookie() + " can't be deleted with cookie " + lockCookie);
     }
     try {
       DBReaderWriter.deleteRecord(record);
     } catch (Exception e) {
       log.info(e.getMessage(), e);
     }
   }
 
   @Override
   public long[] findByCriteria(String[] criteria) {
     if (criteria == null) {
       return null;
     }
     DBPresenter presenter = DBPresenter.getInstance();
    if (Arrays.asList(criteria).contains(null)) { // return all records if some criteria is null
      long[] findedNumbers = new long[presenter.getRecords().size()];
      int count = 0;
      for (DBRecord record : presenter.getRecords()) {
        findedNumbers[count++] = record.getPosition();
      }
      return findedNumbers;
    }
     List<DBRecord> findedRecords = new ArrayList<DBRecord>();
     for (DBRecord record : presenter.getRecords()) {
      for (String crit : criteria) {
        if (StringPool.BLANK.equals(crit)) {
          break;
        }
        if (record.getValid().startsWith(crit) || record.getName().startsWith(crit) || record.getLocation().startsWith(crit) || record.getSpecialties().startsWith(crit)
            || record.getNumberOfWorkers().startsWith(crit) || record.getRate().startsWith(crit) || record.getOwner().startsWith(crit)) {
           findedRecords.add(record);
         }
       }
     }
     long[] findedNumbers = new long[findedRecords.size()];
     int count = 0;
     for (DBRecord record : findedRecords) {
       findedNumbers[count++] = record.getPosition();
     }
     return findedNumbers;
   }
 
   @Override
   public long createRecord(String[] data) throws DuplicateKeyException {
     DBPresenter presenter = DBPresenter.getInstance();
     DBRecord record = DBRecordHelper.createDBRecord(data);
     if (presenter.getRecords().contains(record)) {
       throw new DuplicateKeyException("Database contains: " + record);
     }
     try {
       DBReaderWriter.addRecord(data);
     } catch (Exception e) {
       log.info(e.getMessage(), e);
     }
     return record.getPosition();
   }
 
   @Override
   public long lockRecord(long recNo) throws RecordNotFoundException {
     DBPresenter presenter = DBPresenter.getInstance();
     DBRecord record = presenter.getRecord(recNo);
     if (!record.isLocked()) {
       long cookie = System.currentTimeMillis();
       record.setCookie(cookie);
       return cookie;
     }
     return 0;
   }
 
   @Override
   public void unlock(long recNo, long cookie) throws SecurityException {
     DBPresenter presenter = DBPresenter.getInstance();
     DBRecord record = null;
     try {
       record = presenter.getRecord(recNo);
     } catch (RecordNotFoundException e) {
       log.error(e.getMessage(), e);
       return;
     }
     if (record.isLocked() && record.getCookie() != cookie) {
       throw new SecurityException("The record " + recNo + " is locked with cookie: " + record.getCookie() + ". You cant' unlock this record with cookie: " + cookie);
     }
     record.setCookie(0);
   }
 }
