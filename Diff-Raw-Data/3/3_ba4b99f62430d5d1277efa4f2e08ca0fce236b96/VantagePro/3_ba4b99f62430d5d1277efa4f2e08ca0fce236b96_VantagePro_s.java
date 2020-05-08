 /*
  *
  */
 package org.tom.weather.comm;
 
 import java.io.IOException;
 import java.rmi.RemoteException;
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.Iterator;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import javax.comm.NoSuchPortException;
 import javax.comm.PortInUseException;
 
 import org.tom.weather.ArchiveEntry;
 import org.tom.weather.Cacheable;
 import org.tom.weather.WeatherStation;
 import org.tom.weather.upload.DataUploader;
 import org.tom.weather.ws.client.WxWsClient;
 
 import org.tom.weather.davis.vp.DmpRecord;
 import org.tom.weather.davis.vp.LoopPacket;
 import org.tom.weather.posting.DataPoster;
 import uk.me.jstott.jweatherstation.util.Process;
 import uk.me.jstott.jweatherstation.util.UnsignedByte;
 
 /**
  * 
  * 
  * @author Jonathan Stott
  * @version 1.0
  * @since 1.0
  */
 public class VantagePro extends Station implements WeatherStation {
   static protected final int RECORD_SIZE = 52;
   static protected final int BUFFER_SIZE = 266;
   static protected final int LOOP_SIZE = 99;
   private static final Logger LOGGER = Logger.getLogger(VantagePro.class);
   private static final Logger DATA_PROBLEMS_LOGGER = Logger.getLogger("DATA_PROBLEMS_LOGGER");
   
   public VantagePro(String portName, int baudRate, int rainGauge) throws PortInUseException,
       NoSuchPortException, IOException {
     super(portName, baudRate, rainGauge);
     LOGGER.debug("rainGauge: " + rainGauge);
   }
 
   private void processDmpAftPacket(byte[] page, int pageOffset) throws Exception {
     List dmpRecords = new ArrayList();
     for (int i = pageOffset; i < 5; i++) {
       byte[] rawData = new byte[RECORD_SIZE];
       int byteOffset = RECORD_SIZE * i;
       for (int k = 0, j = byteOffset; j < byteOffset + RECORD_SIZE; j++, k++) {
         // unsignedBytes[k] = new UnsignedByte(page[j]);
         rawData[k] = page[j];
       }
       DmpRecord dmpRecord = new DmpRecord(rawData);
       if (dmpRecord.getDate().after(getLastDate().getTime())) {
       }
       if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("DMP Record: " + dmpRecord);
       }
       if (dmpRecord.getDate().after(getLastDate().getTime())) {
         if (LOGGER.isInfoEnabled() && dmpRecord.getDate().before(new Date())) {
           LOGGER.info("new DmpRecord = " + dmpRecord.getDate().toString());
         }
         dmpRecords.add(dmpRecord);
         setLastDate(getLastDate());
       } else {
         if (LOGGER.isDebugEnabled()) {
           LOGGER.debug(dmpRecord.getDate() + " older than "
               + getLastDate().getTime());
         }
       }
     }
     uploadDmpRecords(dmpRecords);
   }
   
   private void wakeup() throws IOException {
     clearInputBuffer();
     for (int i = 1; i <= 3; i++) {
       sendByte((byte)0x0d);
       delay(1200);
       int bytes = getInputStream().available();
       if (bytes == 2) {
         byte[] crlf = new byte[2];
         int bytesRead = getInputStream().read(crlf);
         if (bytesRead == 2 && crlf[0] == LF && crlf[1] == CR) {
           LOGGER.debug("Wakeup attempt " + i + " was successful");
           return;
         }
       } else {
         LOGGER.debug(bytes + " bytes available after wakeup, s/b 2");
       }
     }
     LOGGER.error("Station failed to wake up - exiting");
     System.exit(1);
   }
   
   public boolean test() throws IOException {
     LOGGER.debug("about to send TEST");
     sendString("TEST\n");
     // wait HALF A SECOND!
     delay(500);
     boolean ok = false;
     byte tmp[] = new byte[10];
     int bytesRead = this.getInputStream().read(tmp);
     StringBuffer sb = new StringBuffer();
     for (int i = 0; i < bytesRead; i++) {
       if (tmp[i] != 10 && tmp[i] != 13) {
         sb.append((char) tmp[i]);
       }
     }
     if (LOGGER.isDebugEnabled()) {
       LOGGER.debug("station responded: " + sb.toString());
     }
     int index = sb.indexOf("TEST");
     if (index != -1) {
       ok = true;
     }
     return ok;
   }
   protected void sendLoopCommand(int i) throws IOException {
     sendString("LOOP " + i + "\n");
     getAck();
   }
   protected LoopPacket readLoopData() throws IOException {
     LoopPacket packet = null;
     int bytes = 0;
     byte[] localBuffer = new byte[LOOP_SIZE];
     try {
       sendLoopCommand(1);
       readBytes(localBuffer);
         packet = new LoopPacket(localBuffer);
         if (LOGGER.isInfoEnabled()) {
           LOGGER.info(packet.shortToString());
         }
     } catch (IOException ex) {
       LOGGER.warn(ex);
       throw ex;
     }
     return packet;
   }
   /**
    * 
    * 
    * @return
    * @throws IOException
    * @since 1.0
    */
   public void dmpaft() throws Exception {
     UnsignedByte[] datetime = null;
     datetime = getlLastArchiveRecord();
     LOGGER.debug(datetime);
     // t lastDate
     sendString("DMPAFT\n");
     getAck();
     // Send date/time
     crc.reset();
     LOGGER.debug("Sending date/time "
         + Process.printUnsignedByteArray(datetime));
     sendUnsignedBytes(datetime);
     // Send CRC and get number of pages to be received
     UnsignedByte[] check = crc.getUnsignedBytes();
     LOGGER.debug("sending CRC " + Process.printUnsignedByteArray(check));
     sendUnsignedBytes(check);
     // sendBytes(zeros);
     if (!getAck()) {
       LOGGER.error("Aborting dmpaft");
       return;
     }
 
     delay(500);
     int bytes = getInputStream().available();
     if (LOGGER.isDebugEnabled()) {
       LOGGER.debug(bytes + " bytes available on stream");
     }
 
     if (bytes == 0) {
         if (LOGGER.isDebugEnabled()) {
             LOGGER.debug("zero bytes available - trturning from method");
         }
         return;
     }
   
     byte[] header = new byte[bytes];
     int bytesRead = getInputStream().read(header);
     if (bytesRead == 0) { // header must be null - give up and try again
       LOGGER.warn("got zero bytes from serial stream when more were expected");
       return;
     }
  
     if (LOGGER.isDebugEnabled()) {
       LOGGER.debug(bytesRead + " bytes actually read from stream");
     }
   
     UnsignedByte[] data = UnsignedByte.getUnsignedBytes(header);
     if (LOGGER.isDebugEnabled()) {
       LOGGER.debug(data.length + ": size of data array");
       LOGGER.debug("Data: " + Process.printUnsignedByteArray(data));
     }
   
     int pages = (data[1].getByte() << 8) | data[0].getByte();
     int startRecord = (data[3].getByte() << 8) | data[2].getByte();
   
     if (LOGGER.isDebugEnabled()) {
       LOGGER.debug("Expecting " + pages + " pages; first record: " + startRecord);
     }
 
     if (pages == 513) { // we asked for too much data
         pages = 512;
     }
     
     if (pages > 512) {
       LOGGER.warn("unexpected number of data pages: " + pages);
       return;
     }
   
     for (int i = 0; i < pages; i++) {
       if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("sending ack");
       }
       sendBytes(new byte[]{(byte)0x06 });
       delay(500);
   
       if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("sent ack");
       }
       
       byte[] myByte = new byte[1];
       getInputStream().read(myByte);
       int sequenceNumber = (int)myByte[0];
       if (LOGGER.isDebugEnabled()) {
         LOGGER.debug("got sequence number: " + sequenceNumber);
         LOGGER.debug("index: " + i + " and sequence number: " + sequenceNumber);
       }
        if (i == 0) {
         readDmpData(startRecord);
       } else {
         readDmpData();
       }
     }
     if (pages > 2) {
         resetCache();
     }
    }
   private void uploadDmpRecords(List dmpRecords2)  throws Exception {
     
     if (getUploaderList().size() > 0) {
       ArchiveEntry[] entries = new ArchiveEntry[dmpRecords2.size()];
       Iterator iterator = dmpRecords2.iterator();
       int i = 0;
       while (iterator.hasNext()) {
         entries[i++] = (ArchiveEntry)iterator.next();
       }
       for (Iterator iter = getUploaderList().iterator(); iter.hasNext();) {
         DataUploader myUploader = (DataUploader)iter.next();
         myUploader.upload(entries);
       }
     }
   }
   private void readDmpData() throws Exception {
     readDmpData(0);
   }
   private void readDmpData(int offset) throws Exception {
   
     if (LOGGER.isDebugEnabled()) {
       LOGGER.debug("reading dmp data at offset: " + offset);
     }
   
     byte[] localBuffer = new byte[BUFFER_SIZE];
     try {
       int available = getInputStream().available();
       if (LOGGER.isDebugEnabled()) {
         LOGGER.debug(available + " bytes available");
       }
       if (available > 0) {
         int bytes = getInputStream().read(localBuffer);
         if (LOGGER.isDebugEnabled()) {
           LOGGER.debug("looking for: " + localBuffer.length + " retrieved: "
               + bytes + " bytes");
         }
         processDmpAftPacket(localBuffer, offset);
       }
       delay(500);
     } catch (IOException ex) {
       LOGGER.error("Cannot read input stream", ex);
       throw ex;
     }
   }
   private UnsignedByte[] getlLastArchiveRecord() {
     UnsignedByte[] datetime;
     Timestamp dbLastDate = null;
     try {
       dbLastDate = new Timestamp(WxWsClient.getLatestArchiveRecordDate(getLocation()));
     } catch (RemoteException e) {
       LOGGER.warn(e);
       dbLastDate = new Timestamp(new Date().getTime() - 172800); // use two days ago
     }
     getLastDate().setTime(dbLastDate);
     datetime = Process.dmpTimeStamp(// 26, 1, 2004, 15, 0);
         dbLastDate.getDate(), 
         dbLastDate.getMonth() + 1, 
         dbLastDate.getYear() + 1900, 
         dbLastDate.getHours(), 
         dbLastDate.getMinutes());
     LOGGER.debug("returning: " + lastDate.getTime());
     return datetime;
   }
   private void setLastDate(Calendar lastDate) {
     this.lastDate = lastDate;
   }
   private Calendar getLastDate() {
     return lastDate;
   }
   protected void sendString(String str) throws IOException {
     wakeup();
     sendBytes(str.getBytes());
   }
 
   public void setLocation(String location) {
     this.location = location;
   }
   
   public String getLocation() {
     return location;
   }
 
   public void readCurrentConditions() throws Exception {
     LoopPacket loop = readLoopData();
     if (!loop.isValid()) {
       DATA_PROBLEMS_LOGGER.warn(loop.toString());
     }
     if (loop != null && loop.isValid()) {
      if (LOGGER.isDebugEnabled()) {
        LOGGER.debug(loop.to_json());
      }
       post(loop);
     }
   }
 
   public void readArchiveMemory() throws Exception {
     dmpaft();
   }
 
   private void post(LoopPacket loop) throws RemoteException {
     for (Iterator iter = getPosterList().iterator(); iter.hasNext();) {
       DataPoster poster = (DataPoster)iter.next();
       poster.post(loop);
     }
   }
 
   private void resetCache() throws Exception {
     for (Iterator iter = getUploaderList().iterator(); iter.hasNext();) {
       DataUploader myUploader = (DataUploader)iter.next();
       if (myUploader instanceof Cacheable) {
         Cacheable c = (Cacheable)myUploader;
         c.resetCache();
       }
     }
   }
 
 
 }
