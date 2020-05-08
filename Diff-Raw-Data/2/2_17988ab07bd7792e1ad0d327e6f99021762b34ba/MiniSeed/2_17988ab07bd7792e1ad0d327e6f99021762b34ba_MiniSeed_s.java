 /*
  * MiniSeed.java
  *
  * Created on May 24, 2005, 2:00 PM
  *
  *
  */
 
 package seed;
 import java.nio.ByteBuffer;
 import java.nio.ByteOrder;
 import java.text.DecimalFormat;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 
 /** This class represents a mini-seed packet.  It can translate binary data in
  * a byte array and break apart the fixed data header and other data blockettes
  * and represent them as separate internal structures.
  *
  * @author davidketchum
  */
 public class MiniSeed  implements MiniSeedOutputHandler {
   public final static int ACTIVITY_CAL_ON=1;
   public final static int ACTIVITY_TIME_CORRECTION_APPLIED=2;
   public final static int ACTIVITY_BEGIN_EVENT=4;
   public final static int ACTIVITY_END_EVENT=8;
   public final static int ACTIVITY_POSITIVE_LEAP=16;
   public final static int ACTIVITY_NEGATIVE_LEAP=32;
   public final static int ACTIVITY_EVENT_IN_PROGRESS=64;
   public final static int IOCLOCK_PARITY_ERROR=1;
   public final static int IOCLOCK_LONG_RECORD=2;
   public final static int IOCLOCK_SHORT_RECORD=4;
   public final static int IOCLOCK_START_SERIES=8;
   public final static int IOCLOCK_END_SERIES=16;
   public final static int IOCLOCK_LOCKED=32;
   public final static int QUALITY_AMP_SATURATED=1;
   public final static int QUALITY_CLIPPED=1;
   public final static int QUALITY_SPIKES=1;
   public final static int QUALITY_GLITCHES=1;
   public final static int QUALITY_MISSING_DATA=1;
   public final static int QUALITY_TELEMETRY_ERROR=1;
   public final static int QUALITY_CHARGING=1;
   public final static int QUALITY_QUESTIONABLE_TIME=1;
   private ByteBuffer ms;
   private byte [] buf;              // our copy of the input data wrapped by ms
   private boolean cracked;
   private boolean cleared;          // This one was last cleared
   private int length;
   private static DecimalFormat int5;
   private static int recordCount; // COunter as MiniSeed records are created.
   private int recordNumber;       // The serial number assigned this record
 
   // components filled out by the crack() routine
   private byte [] seed;             // The 12 charname in fixed header order SSSSSLLCCCNN
   private byte [] seq;              // 6 character with ascii of sequence
   private byte [] indicator;        // two character indicator normally "D " or "Q "
   private byte [] startTime;        // Bytes with raw fixed header time
   private short nsamp;              // Number of samples
   private short rateFactor;         // Rate factor from fixed header
   private short rateMultiplier;     // Rate multiplier from fixed header
   private byte activityFlags;       // activity flags byte from fixed header
   private byte ioClockFlags;        // iod flags from fixed header
   private byte dataQualityFlags;    // Data quality flags from fixed header
   private byte nblockettes;         // number of "data blockettes" in this record
   private int timeCorrection;       // Time Correction from fixed header
   private short dataOffset;         // Offset in buffer of first byte of data
   private short blocketteOffset;    // Offset in bytes to first byte of first data blockette
   private boolean hasBlk1000;       // derived flag on whether a blockette 1000 was decoded
   private boolean hasBlk1001;       // derived flag on whether a blockette 1001 was decoded
   private short year,day,husec;     // Portions of time broken out from fixed header
   private byte hour,minute,sec;     // The byte portions of the time from fixed header
   private GregorianCalendar time;   // This is the Java Gregorian representation of the time
   private GregorianCalendar timeTruncated;// This does not round the ms
   private int julian;               // julian day from year and doy
   private int forward;              // forward integration constart (from first frame)
   private int reverse;              // reverse or ending integeration constant from end
   
   // These contain information about the "data blockettes" really meta-data
   private ByteBuffer [] blockettes;   // these wrap the bufnnn below for each blockette found
                                       // in same order as the blocketteList
   private int [] blocketteOffsets;
   private short [] blocketteList;     // List of the blockett types found
   private byte [] buf100;             // These bufnnn contain data from the various
   private byte [] buf200;             // possible "data blockettes" found. They are
   private byte [] buf201;             // never all defined.
   private byte [] buf300;
   private byte [] buf310;
   private byte [] buf320;
   private byte [] buf390;
   private byte [] buf395;
   private byte [] buf400;
   private byte [] buf405;
   private byte [] buf500;
   private byte [] buf1000;
   private byte [] buf1001;
   private ByteBuffer bb100;             // These bufnnn contain data from the various
   private ByteBuffer bb200;             // possible "data blockettes" found. They are
   private ByteBuffer bb201;             // never all defined.
   private ByteBuffer bb300;
   private ByteBuffer bb310;
   private ByteBuffer bb320;
   private ByteBuffer bb390;
   private ByteBuffer bb395;
   private ByteBuffer bb400;
   private ByteBuffer bb405;
   private ByteBuffer bb500;
   private ByteBuffer bb1000;
   private ByteBuffer bb1001;
   private Blockette1000 b1000;
   private Blockette1001 b1001;
   
   // Data we need from the type 1000 and 1001
   private byte order;         // 0=little endian, 1 = big endian
   private boolean swap;            // If set, this MiniSeed needs to be swapped.
   private int recLength;      // in bytes
   private byte encoding;      // 10=Steim1, 11=Steim2, 15=NSN
   private byte timingQuality;   // 1001 - from 0 to 100 %
   private byte microSecOffset;  // offset from the 100 of USecond in time code
   private byte nframes;         // in compressed data (Steim method only)
 
   private static boolean dbg=false;
   /** If this mini-seed block is a duplicate of the one passed, return true
    * @param ms The mini-seed block for comparison
    * @return true if the blocks have the same time, data rate, number of samples, and compression payload
    *
    */
   public boolean isDuplicate (MiniSeed ms) {
     crack();
     if(ms == null)
       Util.prt("Null");
     if(time.getTimeInMillis() - ms.getTimeInMillis() < 500./getRate() &&
         getNsamp() == ms.getNsamp() && buf.length == ms.getBuf().length) {
       byte [] obuf = ms.getBuf();
       for(int i=64; i<buf.length; i++) if(buf[i] != obuf[i]) return false;
       return true;
     }
     return false;
   }
   public void clear() {
     cracked=false;
     for(int i=0; i<buf.length; i++) buf[i]=0;
     for(int i=0; i<12;i++) seed[i]='Z';
     
     cleared=true;
   }
   public boolean hasBlk1000() {crack(); return hasBlk1000;}
   /** if true, this MiniSeed object is cleared and presumably available for reuse
    */
   public boolean isClear() {return cleared;}
   /** set the debug output flag
    *@param t True for lots of output
    */
   public static  void setDebug(boolean t) {dbg=t;}
   /** set time to given GregorianCalendar
    *@param g The time to set
    *@param hund The hundreds of microseconds to set (adds to Millisecons in Gregorian Calendar)
    */
   public void setTime(GregorianCalendar g, int hund) {
     year = (short) g.get(Calendar.YEAR);
     day = (short) g.get(Calendar.DAY_OF_YEAR);
     hour = (byte) g.get(Calendar.HOUR_OF_DAY);
     minute = (byte) g.get(Calendar.MINUTE);
     sec = (byte) g.get(Calendar.SECOND);
     husec = (short) (g.get(Calendar.MILLISECOND)*10+ hund);
     ms.position(20);
     ms.putShort(year);
     ms.putShort(day);
     ms.put(hour);
     ms.put(minute);
     ms.put(sec);
     ms.put((byte) 0);
     ms.putShort(husec);
     ms.position(20);
     ms.get(startTime);
   }
   /** ms set the number of samples.  Normally used to truncate a time series say
    * to make it just long enough to fill the day!
    *@param ns The number of samples
    */
   public void setNsamp(int ns) {
     nsamp=(short) ns;
     ms.position(30);
     ms.putShort(nsamp);
   }
   public void fixLocationCode() {
     if(seed[5] != ' ' && !Character.isUpperCase(seed[5]) && !Character.isDigit(seed[5])) seed[5] = ' ';
     if(seed[6] != ' ' && !Character.isUpperCase(seed[6]) && !Character.isDigit(seed[6])) seed[6] = ' ';
   }
   public void fixHusecondsQ330() {
     if( husec % 10 == 0) 
       if( (husec/10) % 10 == 4 || (husec/10) %10 == 9) {
         //Util.prt("Fix ms="+toString());
         husec += 9;
         ByteBuffer timebuf = ByteBuffer.wrap(startTime);
         if(swap) timebuf.order(ByteOrder.LITTLE_ENDIAN);
         timebuf.position(8);    // fix it in the startTime buffer
         timebuf.putShort(husec);
         ms.position(28);          
         ms.putShort(husec);        // fix it in the main buffer
         //Util.prt("aft ms="+toString());
         //time.add(Calendar.MILLISECOND, 1);  // add the extra millisecond to the gregorian timeCorrection
       }
     if(husec == 9 && sec == 0 && minute == 0 && hour == 0) husec = 0;
   }
   public void prt(String s) { Util.prt(s);}
   public void prta(String s) { Util.prta(s); }
   public int getRecordNumber() {return recordNumber;}
   /** Creates a new instance of MiniSeed 
    * @param inbuf An array of binary miniseed data
    *@throws IllegalSeednameException if the name does not pass muster
    */
   public MiniSeed(byte [] inbuf) throws IllegalSeednameException {
     buf = new byte[inbuf.length];
     System.arraycopy(inbuf, 0, buf, 0, inbuf.length);
     ms = ByteBuffer.wrap(buf);
     blockettes = new ByteBuffer[4];
     blocketteList = new short[4];
     blocketteOffsets = new int[4];
     init();   // init will set swapping of ms
     recordNumber = recordCount++;
   }
   /** Creates a new instance of MiniSeed 
    * @param inbuf An array of binary miniseed data
    * @param off the offset into inbuf to start
    * @param len The length of the inbuf to convert (the payload length)
    *@throws IllegalSeednameException if the name does not pass muster
    */
   public MiniSeed(byte [] inbuf, int off, int len) throws IllegalSeednameException {
     buf = new byte[len];
     System.arraycopy(inbuf, off, buf, 0, len);
     ms = ByteBuffer.wrap(buf);
     blockettes = new ByteBuffer[4];
     blocketteList = new short[4];
     blocketteOffsets = new int[4];
     init();   // init will set swapping of ms
     recordNumber = recordCount++;
   }
   public void load(byte [] inbuf) throws IllegalSeednameException {
     if(inbuf.length != buf.length) {
       Util.prt("MiniSeed.load() change buffer length from "+buf.length+" to "+inbuf.length);
       buf = new byte[inbuf.length];
       ms = ByteBuffer.wrap(buf);      // order will be set by init()
     }
     System.arraycopy(inbuf, 0, buf, 0, inbuf.length);
     init();
   }
   public void load(byte [] inbuf, int off, int len) throws IllegalSeednameException {
     if(buf.length != len) {
       Util.prt("MiniSeed.load : change buffer length from "+buf.length+" to "+len);
       buf = new byte[len];
       ms = ByteBuffer.wrap(buf);      // order will be set by init()
     }
     System.arraycopy(inbuf, off, buf, 0, len);
     init();
   }
   private void init() throws IllegalSeednameException {
     length=buf.length;
     cracked=false;
     cleared=false;
     encoding=0;
     recLength=buf.length;   // this will be overridden by blockette 1000 if present
     order=-100;
     nframes=0;
     microSecOffset=0;
     timingQuality=101;  
     if(int5 == null) int5 = new DecimalFormat("00000");
     if(seed == null) seed = new byte[12];
     if(seq == null) seq = new byte[6];
     if(indicator == null) indicator = new byte[2];
     if(startTime == null) startTime = new byte[10];
     if(isHeartBeat()) return;
     swap = swapNeeded(buf, ms);
     if(swap) ms.order(ByteOrder.LITTLE_ENDIAN);
     
     
     // crack the seed name so we can check its legality
     ms.clear();
     ms.get(seq).get(indicator).get(seed).get(startTime);
     //MasterBlock.checkSeedName(getSeedName());
     nsamp=ms.getShort();
     rateFactor=ms.getShort();
     rateMultiplier=ms.getShort();
     activityFlags=ms.get();
     ioClockFlags=ms.get();
     dataQualityFlags=ms.get();
     nblockettes=ms.get();
     timeCorrection = ms.getInt();
     dataOffset=ms.getShort();
     blocketteOffset=ms.getShort();
     ms.position(68);
     forward = ms.getInt();
     reverse = ms.getInt();
     fixLocationCode();
     if(swap && dbg) Util.prt("   *************Swap is needed! ************* "+ getSeedName());
   }
   public static boolean crackIsHeartBeat(byte [] buf) {
     boolean is = true;
     for(int i=0; i<6; i++) if(buf[i] != 48 || buf[i+6] != 32 || buf[i+12] != 32) 
       {is = false; break;}
     return is;
   }
   /** Is this mini-seed a heart beat.  These packets have all zero sequence # and
    * all spaces in the net/station/location/channel
    *@return true if sequences is all zero and first 12 chars are blanks
    */
   public boolean isHeartBeat() {
     boolean is = true;
     for(int i=0; i<6; i++) if(buf[i] != 48 || buf[i+6] != 32 || buf[i+12] != 32) 
       {is = false; break;}
     return is;
   }
   
   /** This returns the Julian data from a raw miniseed buffer in buf.
    * This routine would be used to extract a bit of data from a raw buffer without
    *going to the full effort of creating a MiniSeed object from it.
    *@param buf A array with a miniseed block in raw form
    *@return The Julian day
    * @throws IllegalSeednameException if this is clearly not a miniseed buffer
    */
   public static int crackJulian(byte [] buf) throws IllegalSeednameException {
     ByteBuffer bb = ByteBuffer.wrap(buf);
     if(swapNeeded(buf)) bb.order(ByteOrder.LITTLE_ENDIAN);
     bb.position(20);
     short year = bb.getShort();
     short day = bb.getShort();
     if(day <= 0 || day > 366) Util.prt("MiniSeed.crackJulian bad day "+MiniSeed.crackSeedname(buf)+
         " yr="+year+" day="+day);
     if(year <= 2000 || year > 2030) Util.prt("MiniSeed.crackJulian bad year "+MiniSeed.crackSeedname(buf)+
         " yr="+year+" day="+day);
     //short hour = bb.get();
     //short minute = bb.get();
     //short sec = bb.get();
     //bb.get();
     //husec=bb.getShort();
     return SeedUtil.toJulian(year,day);
   }
   /** This returns the time data as a 4 element array with hour,minute, sec, and hsec
    * from a raw miniseed buffer in buf.
    * This routine would be used to extract a bit of data from a raw buffer without
    *going to the full effort of creating a MiniSeed object from it.
    *@param buf A array with a miniseed block in raw form
    *@return The time in a 4 integer array
    * @throws IllegalSeednameException if the buf is clearly not miniseed
    */
   public static int [] crackTime(byte [] buf) throws IllegalSeednameException {
     ByteBuffer bb = ByteBuffer.wrap(buf);
     if(swapNeeded(buf)) bb.order(ByteOrder.LITTLE_ENDIAN);
    bb.position(20);
     //short year = bb.getShort();
     //short day = bb.getShort();
     int [] time = new int[4];
     time[0] = bb.get();   // hour
     time[1] = bb.get();   // minute
     time[2] = bb.get();
     bb.get();
     time[3] =bb.getShort();
     return time;
   }
   /** Return the year from an uncracked miniseedbuf
    *
    * @param buf Buffer with miniseed header
    * @return The year
    * @throws IllegalSeednameException if the buffer clearly is not mini-seed
    */
   public static int crackYear(byte [] buf) throws IllegalSeednameException {
     ByteBuffer bb = ByteBuffer.wrap(buf);
     if(swapNeeded(buf))  bb.order(ByteOrder.LITTLE_ENDIAN);
     bb.position(20);
     return (int) bb.getShort();
   }
   /** Return the day of year from an uncracked miniseedbuf
    *
    * @param buf Buffer with miniseed header
    * @return The day of year
    * @throws IllegalSeednameException if the buffer clearly is not mini-seed
 
    */
   public static int crackDOY(byte [] buf)throws IllegalSeednameException  {
     ByteBuffer bb = ByteBuffer.wrap(buf);
     if(swapNeeded(buf))  bb.order(ByteOrder.LITTLE_ENDIAN);
     bb.position(22);
     return (int) bb.getShort();
   }
   /** This returns the number of samples of data from a raw miniseed buffer in buf.
    * This routine would be used to extract a bit of data from a raw buffer without
    *going to the full effort of creating a MiniSeed object from it.
    *@param buf A array with a miniseed block in raw form
    *@return The number of samples
    * @throws IllegalSeednameException If the buffer is not miniseed
    */
   public static int crackNsamp( byte [] buf) throws IllegalSeednameException{
     ByteBuffer bb = ByteBuffer.wrap(buf);
     if(swapNeeded(buf)) bb.order(ByteOrder.LITTLE_ENDIAN);
     bb.position(30);
     return (int) bb.getShort();
   }
   /** This returns the digitizing rate from a raw miniseed buffer in buf.
    * This routine would be used to extract a bit of data from a raw buffer without
    *going to the full effort of creating a MiniSeed object from it.
    *@param buf A array with a miniseed block in raw form
    *@return The digitizing rate as a double.  0. if the block factor and multipler are invalid.
    * @throws IllegalSeednameException if the buffer clearly is not mini-seed
    */
   public static double  crackRate( byte [] buf)  throws IllegalSeednameException {
     ByteBuffer bb = ByteBuffer.wrap(buf);
     if(swapNeeded(buf)) bb.order(ByteOrder.LITTLE_ENDIAN);
     bb.position(32);
     short rateFactor=bb.getShort();
     short rateMultiplier=bb.getShort();
     double rate=rateFactor;
     // if rate > 0 its in hz, < 0 its period.
     // if multiplier > 0 it multiplies, if < 0 it divides.
     if(rateFactor == 0 || rateMultiplier == 0) return 0;
     if(rate >= 0) {
       if(rateMultiplier > 0) rate *= rateMultiplier;
       else rate /= -rateMultiplier;
     }
     else {
       if(rateMultiplier > 0)  rate = -rateMultiplier/rate;
       else rate = -1./(-rateMultiplier)/rate;
     }
     return rate;
   }
   /** This returns the seedname in NSCL order from a raw miniseed buffer in buf.
    * This routine would be used to extract a bit of data from a raw buffer without
    *going to the full effort of creating a MiniSeed object from it.
    *@param buf A array with a miniseed block in raw form
    *@return The seedname in NSCL order
    */
   public static String crackSeedname( byte [] buf) {
     ByteBuffer bb = ByteBuffer.wrap(buf);
     bb.position(8);
     byte [] seed = new byte[12];
     bb.get(seed);
     String s = new String(seed);
     return s.substring(10,12)+s.substring(0,5)+s.substring(7,10)+s.substring(5,7);
   }
   public static String safeLetter(byte b) {
     char c = (char) b;
     return Character.isLetterOrDigit(c) || c == ' ' ? ""+c : Util.toHex((byte) c);
   }
   public static String toStringRaw(byte [] buf) {
     ByteBuffer bb = ByteBuffer.wrap(buf);
     StringBuilder tmp = new StringBuilder(100);
     bb.position(0);
     for(int i=0; i<6; i++)  tmp.append(safeLetter(bb.get()));
     tmp.append(" ");
     bb.position(18);
     for(int i=0; i<2; i++) tmp.append(safeLetter( bb.get()));
     bb.position(8);
     for(int i=0; i<5; i++) tmp.append(safeLetter( bb.get()));
     bb.position(15);
     for(int i=0; i<3; i++) tmp.append(safeLetter( bb.get()));
     bb.position(13);
     for(int i=0; i<2; i++) tmp.append(safeLetter( bb.get()));
     bb.position(20);
     short i2 = bb.getShort();
     tmp.append(" "+i2+" "+Util.toHex(i2));
     i2 = bb.getShort();
     tmp.append(" "+i2+" "+Util.toHex(i2));
     tmp.append(" "+bb.get()+":"+bb.get()+":"+bb.get());
     bb.get();
     i2 = bb.getShort();
     tmp.append("."+i2+" "+Util.toHex(i2));
     i2 = bb.getShort();
     tmp.append(" ns="+i2);
     i2 = bb.getShort();
     tmp.append(" rt="+i2);
     i2 = bb.getShort();
     tmp.append("*"+i2);
     bb.position(39);
     tmp.append(" nb="+bb.get());
     bb.position(44);
     i2 = bb.getShort();
     tmp.append(" d="+i2);
      i2 = bb.getShort();
     tmp.append(" b="+i2);
     return tmp.toString();
   }
   public static boolean swapNeeded(byte [] buf) throws IllegalSeednameException {
     ByteBuffer bb = ByteBuffer.wrap(buf);
     return swapNeeded(buf,bb);
   }
   public static boolean swapNeeded(byte [] buf, ByteBuffer bb) throws IllegalSeednameException {
     boolean swap=false;
     if( buf[0] <'0' || buf[0] > '9' || buf[1] <'0' ||  buf[1] > '9' ||
         buf[2] <'0' || buf[2] > '9' || buf[3] <'0' ||  buf[3] > '9' ||
         buf[4] <'0' || buf[4] > '9' || buf[5] <'0' ||  buf[5] > '9' ||
         (buf[6] != 'D' && buf[6] != 'R' && buf[6] !='Q') || buf[7] != ' ' ) {
       throw new IllegalSeednameException("Bad seq # or [DQR] "+toStringRaw(buf));
     }
     bb.position(39);          // position # of blockettes that follow
     int nblks=bb.get();       // get it
     int offset = 0;
     if(nblks > 0) {
       bb.position(46);         // position offset to first blockette
       offset = bb.getShort();
       if(offset > 64 || offset < 48) {       // This looks like swap is needed
         bb.order(ByteOrder.LITTLE_ENDIAN);
         bb.position(46);
         offset = bb.getShort(); // get byte swapped version
         if(offset > 200 || offset < 0) {
           Util.prt("MiniSEED: cannot figure out if this is swapped or not!!! Assume not. offset="+offset+" "+toStringRaw(buf));
           new RuntimeException("Cannot figure swap from offset ").printStackTrace();
         }
         else swap=true;
       }
       for(int i=0; i<nblks; i++) {
         if(offset < 48 || offset >64) {
           Util.prta("Illegal offset trying to figure swapping off="+Util.toHex(offset)+" nblks="+nblks+" seedname="+Util.toAllPrintable(crackSeedname(buf))+" "+toStringRaw(buf));
           break;
         }
         bb.position(offset);
         int type = bb.getShort();
         int oldoffset=offset;
         offset = bb.getShort();
         //ByteOrder order=null;
         if(type == 1000) {
           bb.position(oldoffset+5);   // this should be word order
           if(bb.get() == 0) {
             if(swap) return swap;
             Util.prt("Offset said swap but order byte in b1000 said not to! "+toStringRaw(buf));
             return false;
           }
           else return false;
         }
       }
     }
     else {    // This block does not have blockette 1000, so make decision based on where the data starts!
       bb.position(44);
       offset = bb.getShort();
       if(offset < 0 || offset > 512) return true;
       return false;
     }
     return swap;
   }
   public static int crackBlockSize(byte [] buf) throws IllegalSeednameException {
     ByteBuffer bb = ByteBuffer.wrap(buf);
     if(swapNeeded(buf)) bb.order(ByteOrder.LITTLE_ENDIAN);
     bb.position(39);          // position # of blockettes that follow
     int nblks=bb.get();       // get it
     bb.position(46);         // position offset to first blockette
     int offset = bb.getShort();
     for(int i=0; i<nblks; i++) {
       if(offset < 48 || offset >=64) {
         Util.prta("Illegal offset trying to crackBlockSize() off="+offset+" nblks="+nblks+" seedname="+crackSeedname(buf));
         break;
       }
       bb.position(offset);
       int type = bb.getShort();
       int oldoffset=offset;
       offset = bb.getShort();
       if(type == 1000) {
         bb.position(oldoffset+6);
         return 1 << bb.get();
       }
     }
     return 0;
     
   }
 
   /** this routine takes a binary buf and breaks it into the fixed data header
    *and any other data blockettes that are present.  Note : creating a miniseed
    *object does nothing but store the data.  Anything that needs to use the 
    *"cracked" data structures should call crack first.  If the record has been
    *previously cracked, no processing is done.
    */
   private  void crack() {
     if(cracked) return;
     synchronized(this) {
 
       //timeCorrection=new byte[4];
       if(isHeartBeat()) return;
 
       // Bust up the time and convert to parts and to a GregorianCalendar
       ByteBuffer timebuf=ByteBuffer.wrap(startTime);
       if(swap) timebuf.order(ByteOrder.LITTLE_ENDIAN);
 
       year=timebuf.getShort();
       day=timebuf.getShort();
       hour=timebuf.get();
       minute=timebuf.get();
       sec=timebuf.get();
       timebuf.get();
       husec=timebuf.getShort();
       julian = SeedUtil.toJulian(year,day);
 
       // Note : jan 1, 1970 is time zero so 1 must be subtracted from the day
       long millis = (year -1970)*365L*86400000L+(day-1)*86400000L+((long) hour)*3600000L+
           ((long) minute)*60000L+((long)sec)*1000L;
       millis += ((year - 1969)/4)*86400000L;      // Leap years past but not this one!
 
       if(time == null) time = new GregorianCalendar();
       if(timeTruncated == null) timeTruncated = new GregorianCalendar();
       time.setTimeInMillis(millis+(husec+5)/10);
       timeTruncated.setTimeInMillis(millis+husec/10);
       
       hasBlk1000=false;
       hasBlk1001=false;
       /*prt("sq="+new String(seq)+" ind="+new String(indicator)+
           " name="+new String(seed)+" ns="+nsamp+" nblk="+nblockettes+
           " blkoff="+blocketteOffset+" dataOff="+dataOffset);*/
       // This is the "terminator" blocks for rerequests for the GSN, most LOGS have
       // nsamp set to number of characters in buffer!
       if(seed[7] == 'L' && seed[8] == 'O' && seed[9] == 'G' && nsamp == 0) nblockettes=0;
 
       // If data blockettes are present, process them
       // blockettes is array with ByteBuffer of each type of blockette.  BlocketteList is
       // the blockette type of each in the same order
       if(nblockettes > 0 ) {
         if(blockettes.length < nblockettes) {     // if number of blockettes is bigger than reserved.
           if(nsamp >0)
             Util.prt("Unusual expansion of blockette space in MiniSeed nblks="+
                   nblockettes+" length="+blockettes.length+" "+getSeedName()+" "+
                   ""+year+" "+int5.format(day).substring(2,5)+":"+
         int5.format(hour).substring(3,5)+
         ":"+int5.format(minute).substring(3,5)+":"+int5.format(sec).substring(3,5)+"."+
         int5.format(husec).substring(1,5));
           blockettes = new ByteBuffer[nblockettes];
           blocketteList = new short[nblockettes];
           blocketteOffsets = new int[nblockettes];
         }
         ms.position(blocketteOffset);
         short  next=blocketteOffset;
         for(int blk=0; blk<nblockettes; blk++) {
           blocketteOffsets[blk] = next;
           short type =ms.getShort();
           // This is the problem when blockette 1001 was not swapped for a shor ttime 2009,128-133
           if(type == -5885) {
             if(time.get(Calendar.YEAR) == 2009 &&
                     time.get(Calendar.DAY_OF_YEAR) >= 128 && time.get(Calendar.DAY_OF_YEAR) <= 133) {
               ms.position(ms.position() -2);
               ms.putShort((short) 1001);
               type = 1001;
             }
           }
 
           if(dbg) prt(blk+"                                           **** MS: Blockette type="+type+" off="+next);
           blocketteList[blk]=type;
           if(next < 48 || next >= recLength)  {
             if(nsamp > 0)
               Util.prt("Bad position in blockettes next2="+next);
             nblockettes=(byte) blk;
             break;
           }
           ms.position((int) next);
           switch(type) {
             case 100:     // Sample Rate Blockette
               if(buf100 == null) {
                 buf100=new byte[12];
                 bb100=ByteBuffer.wrap(buf100);
               }
               blockettes[blk]=bb100;
               ms.get(buf100);
               if(swap) blockettes[blk].order(ByteOrder.LITTLE_ENDIAN);
               blockettes[blk].clear();
               blockettes[blk].getShort();    // type = 100
               next=blockettes[blk].getShort();
               break;
             case 200:     // Generic Event Detection
               if(buf200 == null ) {
                 buf200=new byte[28];
                 bb200=ByteBuffer.wrap(buf200);
               }
               blockettes[blk]=bb200;
               ms.get(buf200);
               if(swap) blockettes[blk].order(ByteOrder.LITTLE_ENDIAN);
               blockettes[blk].clear();
               blockettes[blk].getShort();    // type = 200
               next=blockettes[blk].getShort();
               break;
             case 201:     // Murdock Event Detection
               if(buf201 == null) {
                 buf201=new byte[60];
                 bb201=ByteBuffer.wrap(buf201);
               }
               blockettes[blk]=bb201;
               ms.get(buf201);
               if(swap) blockettes[blk].order(ByteOrder.LITTLE_ENDIAN);
               blockettes[blk].clear();
               blockettes[blk].getShort();    // type = 201
               next=blockettes[blk].getShort();
               break;
             case 300:     // Step Calibration
               if(buf300 == null) {
                 buf300=new byte[32];
                 bb300=ByteBuffer.wrap(buf300);
               }
               blockettes[blk]=bb300;
               ms.get(buf300);
               if(swap) blockettes[blk].order(ByteOrder.LITTLE_ENDIAN);
               blockettes[blk].clear();
               blockettes[blk].getShort();    // type = 300
               next=blockettes[blk].getShort();
               break;
             case 310:     // Sine Calbration
               if(buf310 == null) {
                 buf310=new byte[32];
                 bb310=ByteBuffer.wrap(buf310);
               }
               ms.get(buf310);
               blockettes[blk]=bb310;
               if(swap) blockettes[blk].order(ByteOrder.LITTLE_ENDIAN);
               blockettes[blk].clear();
               blockettes[blk].getShort();    // type = 310
               next=blockettes[blk].getShort();
               break;
             case 320:     // Pseudo-random calibration
               if(buf320 == null) {
                 buf320=new byte[32];
                 bb320=ByteBuffer.wrap(buf320);
               }
               ms.get(buf320);
               blockettes[blk]=bb320;
               if(swap) blockettes[blk].order(ByteOrder.LITTLE_ENDIAN);
               blockettes[blk].clear();
               blockettes[blk].getShort();    // type = 320
               next=blockettes[blk].getShort();
               break;
             case 390:     // Generic Calibration
               if(buf390 == null) {
                 buf390=new byte[28];
                 bb390=ByteBuffer.wrap(buf390);
               }
               ms.get(buf390);
               blockettes[blk]=bb390;
               if(swap) blockettes[blk].order(ByteOrder.LITTLE_ENDIAN);
               blockettes[blk].clear();
               blockettes[blk].getShort();    // type = 330
               next=blockettes[blk].getShort();
               break;
             case 395:     // Calibration abort
               if(buf395 == null) {
                 buf395=new byte[16];
                 bb395 = ByteBuffer.wrap(buf395);
               }
               blockettes[blk]=bb395;
               ms.get(buf395);
               if(swap) blockettes[blk].order(ByteOrder.LITTLE_ENDIAN);
               blockettes[blk].clear();
               blockettes[blk].getShort();    // type = 395
               next=blockettes[blk].getShort();
               break;
             case 400:     // Beam blockette
               if(buf400 == null) {
                 buf400=new byte[16];
                 bb400=ByteBuffer.wrap(buf400);
               }
               ms.get(buf400);
               blockettes[blk]=bb400;
               if(swap) blockettes[blk].order(ByteOrder.LITTLE_ENDIAN);
               blockettes[blk].clear();
               blockettes[blk].getShort();    // type = 400
               next=blockettes[blk].getShort();
               break;
             case 405:     // Beam Delay 
               if(buf405 == null) {
                 buf405=new byte[6];
                 bb405=ByteBuffer.wrap(buf405);
               }
               ms.get(buf405);
               blockettes[blk]=bb405;
               if(swap) blockettes[blk].order(ByteOrder.LITTLE_ENDIAN);
               blockettes[blk].clear();
               blockettes[blk].getShort();    // type = 405
               next=blockettes[blk].getShort();
               break;
             case 500:     // Timing BLockette
               if(buf500 == null) {
                 buf500=new byte[200];
                 bb500=ByteBuffer.wrap(buf500);
               }
               ms.get(buf500);
               blockettes[blk]=bb500;
               if(swap) blockettes[blk].order(ByteOrder.LITTLE_ENDIAN);
               blockettes[blk].clear();
               blockettes[blk].getShort();    // type = 500
               next=blockettes[blk].getShort();
               break;
             case 1000:
               if(buf1000 == null) {
                 buf1000=new byte[8];
                 bb1000=ByteBuffer.wrap(buf1000);            
                 b1000 = new Blockette1000(buf1000);
               } 
               ms.get(buf1000);
               blockettes[blk] = bb1000;
               b1000.reload(buf1000);
               if(swap) blockettes[blk].order(ByteOrder.LITTLE_ENDIAN);
               blockettes[blk].clear();
               blockettes[blk].getShort();    // type = 1000
               next=blockettes[blk].getShort();
               encoding=blockettes[blk].get();
               order=blockettes[blk].get();
               recLength=1;
               recLength = recLength << blockettes[blk].get();
               if(dbg) prt("MS: Blk 1000 length**2="+recLength+" order="+order+
                   " next="+next+" encoding="+encoding);
               hasBlk1000=true;
               break;
             case 1001:      // data extension (Quanterra only?) 
               if(buf1001 == null) {
                 buf1001=new byte[8];
                 bb1001=ByteBuffer.wrap(buf1001);
                 b1001 = new Blockette1001(buf1001);
               }
               ms.get(buf1001);
               blockettes[blk]=bb1001;
               b1001.reload(buf1001);
               if(swap) blockettes[blk].order(ByteOrder.LITTLE_ENDIAN);
               blockettes[blk].clear();
               blockettes[blk].getShort();    // type = 1001
               next=blockettes[blk].getShort();
               timingQuality=blockettes[blk].get();
               microSecOffset=blockettes[blk].get();
               blockettes[blk].get();        // reserved
               nframes=blockettes[blk].get();
               if(dbg) prt("MS: Blk 1001 next="+next+" timing="+timingQuality+" uSecOff="+microSecOffset+
                       " nframes="+nframes);
               hasBlk1001=true;
               break;
 
             default:
               if(dbg) prt("MS: - unknown blockette type="+type);
               short ty = ms.getShort();
               next = ms.getShort();
               int len = (next == 0 ? recLength:next) - blocketteOffsets[blk];
               byte [] tmp=new byte[len];
               ms.position(blocketteOffsets[blk]);
               ms.get(tmp);
               blockettes[blk]=ByteBuffer.wrap(tmp);
               if(swap) blockettes[blk].order(ByteOrder.LITTLE_ENDIAN);
               blockettes[blk].clear();
               blockettes[blk].getShort();    // type
               ms.position(next);
               break;
           }
         }
       }
 
       cracked=true;
     }     // end of synchronized on this!
   }
   public boolean deleteBlockette(int type) {
     for(int i=0; i<nblockettes; i++) {
       if(blocketteList[i] == type) {
         // found it, take it out of the list and adjust the offsets
         if(i == 0) {
           ms.position(46);
           if(nblockettes == 1) ms.putShort((short) 0);
           else ms.putShort((short) blocketteOffsets[1]); // make 2nd one the first one
           ms.putInt(0);            // Wipe out the offset and type bytes
         }
         else {
           ms.position(blocketteOffsets[i-1]+2);
           if(i+1 < nblockettes) ms.putShort((short) blocketteOffsets[i+1]);
           else ms.putShort((short) 0);      // End of chain
         }
         ms.position(39);
         ms.put((byte) (nblockettes-1));
         cracked=false;
         crack();          // recrack it with the block missing
         return true;
       }
     }
     return false;
   }
   /** create a basic string from the fixed data header representing this packet.
    *any other data blockettes are added at the end to indicate their presence
    *@return A representative string 
    */
   @Override
   public String toString() {
     crack();
     String rt=getRate()+"     ";
     
     rt = getSeedName()+" "+new String(seq)+
         " "+new String(indicator)+getTimeString()+
         " n="+(nsamp+"    ").substring(0,5)+"rt="+rt.substring(0,5)+
         " dt="+(dataOffset+"  ").substring(0,3)+
         //" ln="+(recLength+"   ").substring(0,4)+
         //" en="+(encoding+" ").substring(0,2)+
         " off="+(blocketteOffset+"   ").substring(0,3)+
         " #f="+getUsedFrameCount()+ 
         " nb="+nblockettes+(swap?"S":"");
     for(int i=0; i<nblockettes; i++) {
       if(blocketteList[i] == 1000) rt += " "+b1000.toString();
       else if(blocketteList[i] == 1001) rt += b1001.toString();
       else rt += " ("+blocketteList[i]+")";
     }
     if(nblockettes < 2) rt+=" bsiz="+recLength;
     String f="";
     if(order == 0) f +="S";
     if(swap) f +="S";
     if((activityFlags & ACTIVITY_CAL_ON)!=0) f +="C";
     if((activityFlags & ACTIVITY_TIME_CORRECTION_APPLIED)!=0) f +="*";
     if((activityFlags & ACTIVITY_BEGIN_EVENT)!=0) f +="T";
     if((activityFlags & ACTIVITY_END_EVENT)!=0) f +="t";
     if((activityFlags & ACTIVITY_POSITIVE_LEAP)!=0) f +="+";
     if((activityFlags & ACTIVITY_NEGATIVE_LEAP)!=0) f +="-";
     if((activityFlags & ACTIVITY_EVENT_IN_PROGRESS)!=0) f +="E";
     if((ioClockFlags & IOCLOCK_PARITY_ERROR)!=0) f+="P";
     if((ioClockFlags & IOCLOCK_LONG_RECORD)!=0) f+="L";
     if((ioClockFlags & IOCLOCK_SHORT_RECORD)!=0) f+="s";
     if((ioClockFlags & IOCLOCK_START_SERIES)!=0) f+="O";
     if((ioClockFlags & IOCLOCK_END_SERIES)!=0) f+="F";
     if((ioClockFlags & IOCLOCK_LOCKED)==0 ) f+="U";
     if((dataQualityFlags & QUALITY_AMP_SATURATED) !=0) f+="A";
     if((dataQualityFlags & QUALITY_CLIPPED) !=0) f+="c";
     if((dataQualityFlags & QUALITY_SPIKES) !=0) f+="G";
     if((dataQualityFlags & QUALITY_GLITCHES) !=0) f+="g";
     if((dataQualityFlags & QUALITY_MISSING_DATA) !=0) f+="M";
     if((dataQualityFlags & QUALITY_TELEMETRY_ERROR) !=0) f+="e";
     if((dataQualityFlags & QUALITY_CHARGING) !=0) f+="d";
     if((dataQualityFlags & QUALITY_QUESTIONABLE_TIME) !=0) f+="q";
 
     return rt+f;
   }
 
   /** Compare this MiniSeed object to another. First the two objects' SEED names
    * are compared, and if they are equal, their starting dates are compared.
    * @param o the other MiniSeed object
    * @return -1 if this object is less than other, 0 if the two objects are
    *         equal, and +1 if this object is greater than other.
    */
   public int compareTo(MiniSeed o) {
     MiniSeed other;
     int cmp;
     if(o.isClear()) return -1;    // Cleared MiniSeeds are always at end
     if(isClear()) return 1;
     other =  o;
     crack();
     other.crack();
     cmp = getSeedName().compareTo(other.getSeedName());
     if (cmp != 0)
       return cmp;
     if (getGregorianCalendar().before(other.getGregorianCalendar()))
       return -1;
     else if (getGregorianCalendar().after(other.getGregorianCalendar()))
       return 1;
     else
       return 0;
   }
   /** return number of used data frames.  We figure this by looking for all zero frames
    * from the end!
    *@return Number of used frames based on examining for zeroed out frames rather than b1001
    */
   public int getUsedFrameCount() {
     crack();
     int i;
     for(i=Math.min(buf.length,recLength)-1; i>= dataOffset; i--) {
       if(buf[i] != 0) break;
     }
     return  (i -dataOffset+64) / 64;      // This is the data frame # used
   }
   /** return state of swap as required by the Steim decompression routines and our internal convention.  We both assume
    *  bytes are in BIG_ENDIAN order and swap bytes if the records is not.  Stated another way, we swap whenever
    * the compressed data indicates it is is little endian order.
    *@return True if bytes need to be swapped */
   public boolean isSwapBytes() {return swap;}
   /**
    * return the blocksize or record length of this mini-seed
    * @return the blocksize of record length 
    */
   public int getBlockSize() {crack(); return recLength;}
   /** retun number of samples in packet 
    * @return # of samples */
   public int getNsamp() { return (int) nsamp;}
   /** return number of data blockettes 
    * @return # of data blockettes */
   public int getNBlockettes() { return (int) nblockettes;}
   /** return the offset  of the ith blockette
    *@param i The index of the block (should be less than return of getNBlockettes
    *@return The offset of the ith blockette or -1 if i is out or range */
   public int getBlocketteOffset(int i) {crack(); if(i < blocketteList.length) return blocketteOffsets[i]; return -1;}
   /** return the type of the ith blockette
    *@param i The index of the block (should be less than return of getNBlockettes
    *@return The type of the ith blockette or -1 if i is out or range */
   public int getBlocketteType(int i) {crack(); if(i < blocketteList.length) return (int) blocketteList[i]; return -1;}
   /** return a ByteBuffer to the ith blockett
    * @param i The index of the blockette desired
    * @return A byte buffer with the data for this blockett
    */
   public ByteBuffer getBlockette(int i) {return blockettes[i];}
   /** return the seed name of this component in nnssssscccll order 
    * @return the nnssssscccll*/
   public String getSeedName() {
     String s = new String(seed);
     return s.substring(10,12)+s.substring(0,5)+s.substring(7,10)+s.substring(5,7);
   }
   /** return the two char data block type, normally "D ", "Q ", etc 
    * @return the indicator*/
   public String getIndicator(){ return new String(indicator);}
   /** return the encoding 
    * @return the encoding */
   public boolean isBigEndian(){ return (order != 0);}
   /** return the encoding 
    * @return the encoding */
   public int getEncoding(){ crack(); return encoding;}
   /** return year from the first sample time 
    * @return the year */
   public int getYear() {crack(); return year;}
   /** return the day-of-year from the first sample time 
    * @return the day-of-year of first sample */
   public int getDay() {crack(); return day;}
 
   /** return the day-of-year from the first sample time 
    * @return the day-of-year of first sample */
   public int getDoy() {crack(); return day;}
   /** return the hours 
    * @return the hours of first sample */
   public int getHour() {crack(); return hour;}
   /** return the minutes 
    * @return the minutes of first sample */
   public int getMinute() {crack(); return minute;}
   /** return the seconds 
    * @return the seconds of first sample */
   public int getSeconds() {crack(); return sec;}
   /** return the 100s of uSeconds 
    * @return 100s of uSeconds of first sample */
   public int getHuseconds() {crack(); return husec;}
   /** return Julian day (a big integer) of the first sample's year and day
    * @return The 1st sample time*/
   public int getJulian() { crack(); return julian;}
   /** return the activity flags from field 12 of the SEED fixed header
    * <p> 
    * Bit   Description
    *<p>  0     Calibration signals present
    *<p>  1     Time Correction applied.
    *<p>  2     Beginning of an event, station trigger
    *<p>  3     End of an event, station detriggers
    *<p>  4     Positive leap second occurred in packet
    *<p>  5     Negative leap second occurred in packet
    *<p>  6     Event in Progress
    *<p>
    *@return The activity flags
    */  
   public byte getActivityFlags() {crack();return activityFlags;}
   /** return the data qualit flags from field 13 of the Seed fixed header
    *<p> bit   Description
    *<p>  0    Amplifier staturation detected
    *<p>  1    Digitizer clipping detected
    *<p>  2    Spikes detected
    *<p>  3    Glitches detected
    *<p>  4    Missing/padded data present
    *<p>  5    Telemetry synchronization error
    *<p>  6    A digial filter may be "charging"
    *<p>  7    Time tag is questionable
    *@return The data quality flags
    */
   public byte getDataQualityFlags(){crack();return dataQualityFlags;}
   /** Return the IO and clock flags from field 13 of fixed header
    *<p>Bit Description
    *<p> 0  Station volume parity error possibly present
    *<p> 1  Long record read( possibly no problem)
    *<p> 2  Short record read(record padded)
    *<p> 3  Start of time series
    *<p> 4  End of time series
    *<p> 5  Clock locked
    *@return The IO and clock flags
    */
   public byte getIOClockFlags() {crack();return ioClockFlags;}
   /** return the raw time bytes in an array
    *@return The raw time bytes in byte array 
    */
   public byte [] getRawTimeBuf() {
     crack();
     byte [] b = new byte[startTime.length];
     System.arraycopy(startTime, 0, b, 0, startTime.length);
     return b;   // return copy of time bytes.
   }
   /** return the time in gregroian calendar millis
    *@return GregorianCalendar millis for start time
    */
   public long getTimeInMillis() {crack(); return time.getTimeInMillis();}
   /** return a GregorianCalendar set to the 1st sample time, it is a copy just so users
    * do not do calculations with it and end up changing the mini-seed record time.  This time is rounded in millis
    * @return The 1st sample time*/
   public GregorianCalendar getGregorianCalendar() { 
     crack();
     GregorianCalendar e = new GregorianCalendar();
     e.setTimeInMillis(time.getTimeInMillis());
     return e;
   }
   /** return the time in gregroian calendar millis, this time is truncated
    *@return GregorianCalendar millis for start time
    */
   public long getTimeInMillisTruncated() {crack(); return timeTruncated.getTimeInMillis();}
   /** return a GregorianCalendar set to the 1st sample time, it is a copy just so users
    * do not do calculations with it and end up changing the mini-seed record time.  This time is truncated in millis
    * @return The 1st sample time*/
   public GregorianCalendar getGregorianCalendarTruncated() { 
     crack();
     GregorianCalendar e = new GregorianCalendar();
     e.setTimeInMillis(timeTruncated.getTimeInMillis());
     return e;
   }
   /** give a standard time string for the 1st sample time 
    * @return the time string yyyy ddd:hh:mm:ss.hhhh */
   public String getTimeString() {
     crack(); 
     return ""+year+" "+int5.format(day).substring(2,5)+":"+
         int5.format(hour).substring(3,5)+
         ":"+int5.format(minute).substring(3,5)+":"+int5.format(sec).substring(3,5)+"."+
         int5.format(husec).substring(1,5);
   }
   public GregorianCalendar getEndTime() {
     crack();
     GregorianCalendar end = new GregorianCalendar();
     end.setTimeInMillis(time.getTimeInMillis() + (long) ((nsamp-1)/getRate()*1000.+0.5)); 
     return end;
   }
   public long getNextExpectedTimeInMillis() {
     crack();
     return time.getTimeInMillis() + (long) ((nsamp)/getRate()*1000.+0.5); 
   }
   public String getEndTimeString() {
     crack();
     GregorianCalendar end = new GregorianCalendar();
     if(getRate() < 0.0001) end.setTimeInMillis(time.getTimeInMillis());
     else end.setTimeInMillis(time.getTimeInMillis() + (long) ((nsamp-1)/getRate()*1000.+0.5));
     int iy = end.get(Calendar.YEAR);
     int doy = end.get(Calendar.DAY_OF_YEAR);
     int hr = end.get(Calendar.HOUR_OF_DAY);
     int min = end.get(Calendar.MINUTE);
     int sc = end.get(Calendar.SECOND);
     int msec = end.get(Calendar.MILLISECOND);
     return ""+iy+" "+int5.format(doy).substring(2,5)+":"+
         int5.format(hr).substring(3,5)+
         ":"+int5.format(min).substring(3,5)+":"+int5.format(sc).substring(3,5)+"."+
         int5.format(msec).substring(2,5);
   }
   /** return the rate factor from the seed header
    * @return The factor
    */
   public short getRateFactor() {return rateFactor;}
   /** return the rate multiplier from the seed header
    * 
    * @return The multiplier
    */
   public short getRateMultiplier() {return rateMultiplier;}
   
   /** return the sample rate 
    * @return the sample rate */
   public double getRate() {
     crack();
     double rate=rateFactor;
     // if rate > 0 its in hz, < 0 its period.
     // if multiplier > 0 it multiplies, if < 0 it divides.
     if(rateFactor == 0 || rateMultiplier == 0) return 0;
     if(rate >= 0) {
       if(rateMultiplier > 0) rate *= rateMultiplier;
       else rate /= -rateMultiplier;
     }
     else {
       if(rateMultiplier > 0)  rate = -rateMultiplier/rate;
       else rate = -1./(-rateMultiplier)/rate;
     }
     return rate;
   }
 
   /**
    * Return the duration of this packet.
    *
    * @return the duration in seconds.
    */
   public double getDuration() {
     crack();
     return getNsamp() / getRate();
   }
 
   /** return the buffer with the data portion of the packet of length.
    * @param len The number of bytes to return
    * @return An array with the bytes
    */
   public byte [] getData(int len) { 
     ms.position(dataOffset); 
     byte [] tmp=new byte[len]; 
     ms.get(tmp); 
     return tmp;
   }
   /** get the raw buffer representing this Miniseed-record. Beware you are getting
    *the actual buffer so changing it will change the underlying data of this MiniSeed
    * object.  If this is done after crack, some of the internals will not reflect it.
    *@return the raw data byte buffer */
   public byte [] getBuf() {return buf;}
   /** return the record length from the blockette 1000.  Note the length might also
    * be determined by getBuf().length as to how this mini-seed was created (buf is a
    * copy of the input buffer).
    *@return the length if blockette 1000 present, zero if it is not
    */
   public int getRecLength() {crack();return recLength;}
   /** return a blockette 100
    *@return the blockette or null if this blockette is not in mini-seed record
    */
   public byte [] getBlockette100() {crack();return buf100;}             // These getBlockettennn contain data from the various
   /** return a blockette 200
    *@return the blockette or null if this blockette is not in mini-seed record
    */
   public byte [] getBlockette200() {crack();return buf200;}             // possible "data blockettes" found. They are
   /** return a blockette 201
    *@return the blockette or null if this blockette is not in mini-seed record
    */
   public byte [] getBlockette201() {crack();return buf201;}             // never all defined.
   /** return a blockette 300
    *@return the blockette or null if this blockette is not in mini-seed record
    */
   public byte [] getBlockette300() {crack();return buf300;}
   /** return a blockette 310
    *@return the blockette or null if this blockette is not in mini-seed record
    */
   public byte [] getBlockette310() {crack();return buf310;}
   /** return a blockette 320
    *@return the blockette or null if this blockette is not in mini-seed record
    */
   public byte [] getBlockette320() {crack();return buf320;}
   /** return a blockette 390
    *@return the blockette or null if this blockette is not in mini-seed record
    */
   public byte [] getBlockette390() {crack();return buf390;}
   /** return a blockette 395
    *@return the blockette or null if this blockette is not in mini-seed record
    */
   public byte [] getBlockette395() {crack();return buf395;}
   /** return a blockette 400
    *@return the blockette or null if this blockette is not in mini-seed record
    */
   public byte [] getBlockette400() {crack();return buf400;}
   /** return a blockette 405
    *@return the blockette or null if this blockette is not in mini-seed record
    */
   public byte [] getBlockette405() {crack();return buf405;}
   /** return a blockette 500
    *@return the blockette or null if this blockette is not in mini-seed record
    */
   public byte [] getBlockette500() {crack();return buf500;}
   /** return a blockette 1000
    *@return the blockette or null if this blockette is not in mini-seed record
    */
   public byte [] getBlockette1000() {crack();return buf1000;}
   /** return a blockette 1001
    *@return the blockette or null if this blockette is not in mini-seed record
    */
   public byte [] getBlockette1001() {crack();return buf1001;}
   /** return the forward integration constant
    *@return the forward integration constant*/
   public int getForward() {crack();return forward;}
   /** return the offset to the data
    *@return the offset to the data in bytes */
   public int getDataOffset() {return (int) dataOffset;}
   /** return reverse integration constant
    *@return The reverse integration constant*/
   public int getReverse() {crack();return reverse;}
   /** get the sequence number as an int!
    *@return the sequence number */
   public int getSequence() {
     crack();
     return Integer.parseInt(new String(seq));
   }
   /** return the timing quality byte from blockette 1000
    *@return the timing quality byte from blockette 1001 or -1 if it does not exist
    */
   public int getTimingQuality() {
     crack();
     if(b1001 == null) return -1;
     return b1001.getTimingQuality();
   }
   /** return the number of used frames from from blockette 1000
    *@return the # used frames from blockette 1001 or -1 if it does not exist
    */
   public int getB1001FrameCount() {
     crack();
     if(b1001 == null) return -1;
     return b1001.getFrameCount();
   }
   /** return the number of used frames from from blockette 1000
    *@return the # used frames from blockette 1001 or -1 if it does not exist
    */
   public int getB1001USec() {
     crack();
     if(b1001 == null) return -1;
     return b1001.getUSecs();
   }
 
   /** the unit test main
    *@param args COmmand line arguments
    */
   /** 
    * Set the length of the packet to n in the Blockette 1000
    * @param n the record length, anything but 512 or 4096 will generate some warning output
    */
   public void setB1000Length(int n) {
     crack();
     if(n != 512 && n != 4096) Util.prt("Unusual B1000 len="+n+" "+toString());
     // Change length of  record in Blockette1000 and change it in buf1000 and in data buffer.
     if(b1000 != null) {
       b1000.setRecordLength(n);
       System.arraycopy(b1000.getBytes(),0, buf1000, 0,8);
       for(int j=0; j<blocketteList.length; j++) {
         if(blocketteList[j] == 1000) {
           System.arraycopy(b1000.getBytes(),0, buf, blocketteOffsets[j],8);
           break;
         }
       } 
       cracked=false;
     }
     return;
   }
   public void setB1001FrameCount(int n) {
     // Change #frames in framce count in B1ockette 1001 and store in buf1001 and data buffer
     crack();
     if(b1001 != null) {
       b1001.setFrameCount(n);
       System.arraycopy(b1001.getBytes(), 0, buf1001, 0, 8);
       for(int j=0; j<blocketteList.length; j++) {
         if(blocketteList[j] == 1001) {
           System.arraycopy(b1001.getBytes(),0, buf, blocketteOffsets[j],8);
           break;
         }
       } 
       cracked=false;
     }
   }
   public void setB1001Usec(int n) {
     // Change #frames in framce count in B1ockette 1001 and store in buf1001 and data buffer
     crack();
     if(b1001 != null) {
       b1001.setUSecs(n);
       System.arraycopy(b1001.getBytes(), 0, buf1001, 0, 8);
       for(int j=0; j<blocketteList.length; j++) {
         if(blocketteList[j] == 1001) {
           System.arraycopy(b1001.getBytes(),0, buf, blocketteOffsets[j],8);
           break;
         }
       } 
       cracked=false;
     }
   }
   public int [] decomp() throws SteimException {
     int rev=0;
     byte [] frames = new byte[getBlockSize()-dataOffset];
     System.arraycopy(buf,dataOffset, frames,0,getBlockSize()-dataOffset); 
     int [] samples=null;
     if(getEncoding() == 10) samples = Steim1.decode(frames, getNsamp(), swap, rev);
     if(getEncoding() == 11) samples = Steim2.decode(frames, getNsamp(), swap, rev);
 
     // Would adding this block "as is" cause a reverse constant error (or steim error)?  If so, restore block
     // to state before adding this one, write it out, and make this block the beginning of next output block
     if(Steim2.hadReverseError() || Steim2.hadSampleCountError()) {
       if(Steim2.hadReverseError()) Util.prta("Decomp  "+Steim2.getReverseError()+" "+toString());
       if(Steim2.hadSampleCountError()) Util.prta("decomp "+Steim2.getSampleCountError()+" "+toString());
       return null;
     }
     return samples;
   }
   public void fixReverseIntegration() {
    try {
       byte [] frames = new byte[getBlockSize()-dataOffset];
       System.arraycopy(buf,dataOffset, frames,0,getBlockSize()-dataOffset); 
       int [] samples=null;
       int rev=0;
       if(getEncoding() == 10) samples = Steim1.decode(frames, getNsamp(), swap, rev);
       if(getEncoding() == 11) samples = Steim2.decode(frames, getNsamp(), swap, rev);
 
       // Would adding this block "as is" cause a reverse constant error (or steim error)?  If so, set reverse
       // integration constant from the decompressed dta.
       if(Steim2.hadReverseError()) {
 
         ms.position(dataOffset+4);            // position forward integration constant
         //Util.prt("FixReverseIntegration: fwd="+forward+" "+samples[0]+" rev="+reverse+" "+samples[samples.length-1]);
         if(reverse != samples[samples.length-1]) {
           ms.position(dataOffset+8);
           ms.putInt(samples[samples.length-1]);
           reverse = samples[samples.length-1];      // set new reverse in data section
           samples = decomp();
           if(samples == null) Util.prt("MS.fixReverseIntegration() failed decomp returned null");
           //else Util.prt("FixReverseIntegration: new rev="+samples[samples.length-1]);
         } 
       }
     }
     catch(SteimException e) {}
   }
   /** this routine splits a bigger miniseed block into multiple 512.  It has two algorithms
    * depending on whether the input block is a agreegate of 512s or not.  If it is, the blocks
    * are just disaggregated.  If not, the block is decompressed and recompressed into a series of
    * blocks.  In general the block number of the output blocks is 
    * 500000+(orig sequ *10) + 512 block number.  So block number 1234 has 9 output sequences from
    * 512340 to 512348.
    * @throws IllegalSeednameException if the seedname does not pass muster
    *@return An array with the 512 byte blocks.*/
   public MiniSeed [] toMiniSeed512() throws IllegalSeednameException {
     crack();
     if(this.getBlockSize() == 512) {
       MiniSeed [] mss = new MiniSeed[1];
       mss[0] = this;
       Util.prt("Attempt to put 512 byte miniseed into 512 byte miniseed!");
       return mss;
     }
      int nout=0;
      MiniSeed mshdr = null;
     // Build a separate block with only the blockettes in them if the exceed the 16 bytes between 48 and 64
     if(dataOffset > 64) {
       byte [] bf= new byte[512];
       System.arraycopy(buf, 0, bf, 0, 512);
       ByteBuffer bb = ByteBuffer.wrap(bf);
       bb.order(ms.order());
       bb.position(30);
       bb.putShort((short) 0);   // zap the number of samples
       bb.putShort((short) 0);   // set the data rate to zero
       for(int i=dataOffset; i<512; i++) bf[i]=0;    // zero remainder of buffer
       mshdr = new MiniSeed(bf);
       mshdr.setB1000Length(512);
       mshdr.setB1001FrameCount(mshdr.getUsedFrameCount());
     }
 
     // Now prepare the new header that will fit in 64 bytes with B1000 and B1001
     byte [] hdr = new byte[64];
     System.arraycopy(buf, 0, hdr, 0, 64);     // Save the header!
     hdr[0]='9';
     for(int j=1;j<5; j++) hdr[j]=hdr[j+1];    // new seq = 900000+(oldseq*10)+blk#
     hdr[5]='0';
 
     // We only want a B1000 and B1001 (if present), fix up this header
     ByteBuffer bh = ByteBuffer.wrap(hdr);
     bh.position(39);
     bh.put((byte) 1);             // Assume only B1000 will be put back
     bh.position(44);
     bh.putShort((short) 64);      // data offset must be 64
     bh.putShort((short) 48);      // Blockettes now must start at 48
     if(hasBlk1000) {
       bh.put(buf1000);
       bh.position(50);
       bh.putShort((short) 0);   // assume no b1001
     }
     else {
       Util.prta("Attempt to break up a block without a B1000 "+toString());
       //SendEvent.debugSMEEvent("MS512Err", toString(), "MiniSeed");
       return null;
     }
     if(hasBlk1001) {
       bh.position(39);
       bh.put((byte) 2);            // now 2 blockettes B1000 and B1001
       bh.position(50);
       bh.putShort((short) 56);     // link this to the B1000
       bh.position(56);
       bh.put(buf1001);
       bh.position(58);
       bh.putShort((short) 0);
     }
       
       // There are two types of 4096 point miniseed in the world so far, those built up from 512
     // and those compressed whole as 4096.  We can save the 512s by spliting them up if we find this
     boolean ok=true;
     for(int i=dataOffset; i<getBlockSize(); i=i+7*64) {   // for each key in each 7th frame
       ms.position(i);
       int key = ms.getInt();      // this is a Steim frame key for 1st block of 512
       //int v = key & 0xF0000000;   // if this is the start of an original frame, then 1st 2 keys are zero for ICs
       //Util.prt(i+" "+Util.toHex(v));
       if( (key & 0xF0000000) != 0) {
         ok=false;
         break;                    // no need to check more
       }
     }
     if(ok && getSeedName().substring(0,2).equals("IU")) {                      // This can be broken up into groups of 7 frames modifying the headers
       Util.prta("toMS512 DO IU ms="+toString());
       int nfrs = 100;
       if(b1001 != null) nfrs = b1001.getFrameCount();
 
       MiniSeed [] mss = new MiniSeed[(getBlockSize()/(512-dataOffset))+1];
       if(mshdr != null) mss[nout++] = mshdr;              // Add the hdr only block
       int totsamp=0;
  
       // Now build up blocks 7 frames long blocks long
       int hund = husec %10;
       int i = dataOffset;
       int nleft = 512 - 64;
       int cntFrames=0;
       // how many bytes are left for data frames
       while (i < getBlockSize()) {
       //for(int i=64; i<getBlockSize(); i=i+7*64) {
         byte [] bf = new byte[512];
         //ByteBuffer bb = ByteBuffer.wrap(bf);
         if(getBlockSize() -i < nleft) 
           for(int j=0; j<512; j++) bf[j] = 0;   // on the last short block add prezero
         System.arraycopy(buf, i, bf, 64, Math.min(nleft, getBlockSize() - i));   // Put the 7 or less frames in position
         System.arraycopy(hdr, 0, bf, 0, 64);      // Add the incorrect header
         MiniSeed msrec=null;
         int nsampbuf = MiniSeed.getNsampFromBuf(bf, getEncoding());          // get number of samples in this compression frame
         if(nsampbuf > 0) {                  // sometimes there really is no more data, do not build zero blocks
           if(nfrs <= cntFrames) Util.prt(" ****** nfrs="+nfrs+" count="+cntFrames+" but more data="+nsampbuf);
           msrec = new MiniSeed(bf);           // create a 512 miniseed for this
           msrec.setNsamp(nsampbuf);
           GregorianCalendar g = new GregorianCalendar();
           g.setTimeInMillis(time.getTimeInMillis()+ ((long) (totsamp/getRate()*1000.+0.0001)));
           //Util.prt("nsb="+nsampbuf+" totsamp="+totsamp+" rate="+getRate()+" totms="+(totsamp/getRate()*1000.)+
           //        " time="+Util.asctime2(g)+" nout="+nout+" i="+i+" nleft="+nleft+" frms="+cntFrames+" of "+nfrs);
           msrec.setTime(g, hund);
           totsamp += nsampbuf;
           msrec.setB1000Length(512);
           msrec.setB1001FrameCount(msrec.getUsedFrameCount());
           msrec.fixReverseIntegration();
           mss[nout++] = msrec;
           hdr[5]++;
         }
         cntFrames += 7;
         i += nleft;
       }
       if(totsamp != getNsamp()) {
         Util.prt("Suspect break of 4096 miniseed to 512 nsamps do not agree. Use recompress method. nsamp="+getNsamp()+" found "+totsamp);
         Util.prt(toString());
       }
       else {
         if(nout < mss.length) {
           MiniSeed [] tmp = new MiniSeed[nout];
           for(i=0; i<nout; i++) tmp[i] = mss[i];
           //for(i=0; i<tmp.length; i++) Util.prt("ms["+i+"]="+tmp[i].toString());
           return tmp;
         }
         //for(i=0; i<mss.length; i++) Util.prt("ms["+i+"]="+mss[i].toString());
         return mss;
       }
     }
 
     // Decompress record to make new blocks.
     try {
       Util.prt("toMS512 decomp/comp method input="+toString().substring(0,60));
       int rev=0;
       byte [] frames = new byte[getBlockSize()-dataOffset];
       System.arraycopy(buf,dataOffset, frames,0,getBlockSize()-dataOffset);
       int [] samples=null;
       if(getEncoding() == 10) samples = Steim1.decode(frames, getNsamp(), swap, rev);
       if(getEncoding() == 11) samples = Steim2.decode(frames, getNsamp(), swap, rev);
 
       if(Steim2.hadReverseError() || Steim2.hadSampleCountError()) {
         if(Steim2.hadReverseError()) Util.prta("make512()  "+Steim2.getReverseError());
         if(Steim2.hadSampleCountError()) Util.prta("make512() "+Steim2.getSampleCountError());
       }
 
       // we now need to recompress the samples, we need to use a putbuf of our own.
       GregorianCalendar sss = getGregorianCalendar();
       int sq=0;
       for(int i=1; i<6; i++) sq = sq*10 + (buf[i]-'0');
       sq = (sq * 10)%100000;
       sq += 910000;
       RawToMiniSeed rtms = new RawToMiniSeed(getSeedName() , getRate(), 7,
           sss.get(Calendar.YEAR),sss.get(Calendar.DAY_OF_YEAR),
             sss.get(Calendar.HOUR_OF_DAY)*3600+sss.get(Calendar.MINUTE)*60+sss.get(Calendar.SECOND),
             sss.get(Calendar.MILLISECOND)*1000+(husec % 10) * 100,
             sq);
       rtms.setOutputHandler(this);        // This registers our putbuf
 
       // if a blockette 1001 is present, update the useconds portion and clock quality.
       int usec = 0;
       int tq=0;
       if(b1001 != null ) {
         tq = b1001.getTimingQuality();
         usec = b1001.getUSecs();
       }
       ms512=null;       // Insure this call to process makes a new array of 512s
       rtms.process(samples, samples.length,sss.get(Calendar.YEAR),sss.get(Calendar.DAY_OF_YEAR),
           sss.get(Calendar.HOUR_OF_DAY)*3600+sss.get(Calendar.MINUTE)*60+sss.get(Calendar.SECOND),
           sss.get(Calendar.MILLISECOND)*1000, 0, 0, 0, tq, 0, false);
       rtms.forceOut();
       int totsamp=0;
       for(int i=0; i<ms512.length; i++) {
         ms512[i].setB1001Usec(usec);
         totsamp+= ms512[i].getNsamp();
         //Util.prt("ms512["+i+"]="+ms512[i].toString());
       }
       if(totsamp != getNsamp()) Util.prt("Suspicious 512 break up nsamp mismatch orig="+getNsamp()+" 512="+totsamp);
 
       // If there was a blockette only hdr record created, add it to the beginning.
       if(mshdr != null) {
         MiniSeed [] tmp = new MiniSeed[ms512.length+1];
         tmp[0] = mshdr;
         for(int i=0; i<ms512.length; i++) tmp[i+1]=ms512[i];
         ms512 = tmp;
       }
       //for(int i=0; i<ms512.length; i++)  Util.prt("ms512["+(i+1)+"] "+ms512[i].toString());
       return ms512;
     }
     catch (SteimException e) {
       Util.prt("**** block gave steim decode error. "+e.getMessage());
       return null;
     }
         
   }
   public void close(){}   // needed to implement a miniseedoutputhandler
   private MiniSeed [] ms512;
   public void putbuf(byte [] b, int size) {
     try {
       MiniSeed msin = new MiniSeed(b);
       if(ms512 == null) ms512 = new MiniSeed[1];
       else {
         MiniSeed[] tmp = new MiniSeed[ms512.length+1];
         for(int i=0; i<ms512.length; i++) tmp[i] = ms512[i];
         ms512=tmp;
       }
       ms512[ms512.length-1]=msin;
     }
     catch(IllegalSeednameException e ) {
       Util.prt("putbuf building ms512 has IllegalSeednameException "+e.getMessage());
     }
 
   }
   public static int getNsampFromBuf(byte [] bf, int encoding) throws IllegalSeednameException {
     ByteBuffer bb = ByteBuffer.wrap(bf);
     bb.position(30);
     byte b1 = bb.get();
     byte b2 = bb.get();
     if(b1 == 0 && b2 == 0) return 0;     // no samples in this - must be ACE or LOG etc
     b1 = bb.get();
     b2 = bb.get();
     if(b1 == 0 && b2 == 0) return 0;     // The rate is zero - must be ACE or LOG
     if(swapNeeded(bf)) bb.order(ByteOrder.LITTLE_ENDIAN);
     int nsamp=0;
     bb.position(44);
     int datoff = bb.getShort();     // start at the data offset length
     if(encoding == 11) {
       for(int offset=datoff; offset<bf.length; offset+= 64) {
         bb.position(offset);
         int keys=bb.getInt();         // Get "nibbles" or key word
         for(int word=1; word < 16; word++) {
           int type = (keys >>(15-word)*2) & 3;     // put the 2 bit nibble for 1st working word at bottom
           int diffwork = bb.getInt();
           int dnib=0;                   // This is the upper two bits of working word for certain "type" values
           // The number of bits in the difference is a determinted by the "nibble" in the key
           // for this work (the type here) and possible the "dnib" from top two bits of differences
           // word.  Figure out how many bits from these two data and how much to left shift the working
           // word to put the first difference sign bit in the 32 bit word sign bit.
           switch (type) {
             case 0:
               if(dbg) Util.prt("non data!");    // This should never happen'
               continue;
             case 1:     // 4 one byte differences
               nsamp +=4;
               continue;
             case 2: 
               dnib=diffwork>>30 & 3;
               switch (dnib) {
                 case 1:
                   nsamp++;
                   continue;
                 case 2: 
                   nsamp += 2;
                   continue;
                 case 3: 
                   nsamp += 3;
                   continue;
               }               // end of case on dnib for type=2
               continue;
             case 3:
               dnib=diffwork>>30 & 3;
               switch (dnib) {
                 case 0:
                   nsamp += 5;
                   continue;
                 case 1:
                   nsamp += 6;
                   continue;
                 case 2: 
                   nsamp += 7;
                   continue;
 
               }   // end of case on dnib for type=3;
           }       // end of case on type    
         }     // end loop on each word in frame
         
       }       // end loop on ach frame
     }         // end of encoding 11
     else if(encoding == 10) {       // Steim I
       for(int offset=datoff; offset<bf.length; offset+= 64) {
         bb.position(offset);
         int keys=bb.getInt();         // Get "nibbles" or key word
         for(int word=1; word < 16; word++) {
           int type = (keys >>(15-word)*2) & 3;     // put the 2 bit nibble for 1st working word at bottom
           //int diffwork = bb.getInt();
           //int dnib=0;                   // This is the upper two bits of working word for certain "type" values
           switch(type) {
             case 0:
               continue;                 // this is overhead words like integration constants
             case 1:
               nsamp += 4;
               continue;
             case 2:
               nsamp += 2;
               continue;
             case 3:
               nsamp++;
           }
         }
         //Util.prt("at offset="+offset+" nsamp="+nsamp);
 
       }
     }
     else Util.prt("Encoding is unknown in getNsampFromBuf() ="+encoding+" "+toStringRaw(bf));
     return nsamp;
   }
   
   public static void simpleFixes(byte [] buf)  throws IllegalSeednameException{
     // If any nulls are fouind in the seedname, change them to spaces
     if(buf[15] == 'L' && buf[16] == 'O' && buf[17] == 'G') return;
     for(int i=8; i<20; i++) 
       if(buf[i] == 0) {
          buf[i] =' ';
          Util.prt("Replace 0 with space in seedname at pos="+i+" "+(new String(buf, 8,12)));
       }
     for(int i=0; i<6; i++) if(buf[i] == ' ') buf[i] = '0';
     
     // Look for a blockette 1000 and see if the swap agrees.
     ByteBuffer bb = ByteBuffer.wrap(buf);
     boolean swap = swapNeeded(buf);
     if(swap) bb.order(ByteOrder.LITTLE_ENDIAN);
     bb.position(39);          // position # of blockettes that follow
     int nblks=bb.get();       // get it
     int offset = 0;
     if(nblks > 0) {
       bb.position(46);         // position offset to first blockette
       offset = bb.getShort();
       for(int i=0; i<nblks; i++) {
         if(offset <= 0 || offset >512) {
           Util.prt("Problem with blockette "+i+" of "+nblks+" offset bad="+offset+" skip rest ");
           break;
         }
         bb.position(offset);
         int type = bb.getShort();
         int oldoffset=offset;
         offset = bb.getShort();
         if(type == 1000) {
           bb.position(oldoffset+5);   // this should be word order
           if(bb.get() == 0) {
             if(!swap) {
               bb.position(oldoffset+5);
               bb.put((byte) 1);
               Util.prt("B1000 say little endian, but swapNeeded() say its big endian. fixed");
             }
           }
           else {      // blocket 1000 says big endian
             if(swap) {
               bb.position(oldoffset+5);
               bb.put((byte) 0);
               Util.prt("B1000 say big endian, but swapNeeded() say its little endian.fixed");
             }
           }
         }
       }      
     }
   }
   public static boolean isQ680Header(byte [] buf) {
     // Some Q680 "mini-seed" have a header.  From 31-75 this is a time-span in ascii, look for this and skip it if found
 
     boolean hdr=true;
     for(int ii=31; ii<75; ii++) 
       if(!((buf[ii] >= 48 && buf[ii] <=57) || buf[ii] == '.' || buf[ii] == ',' || buf[ii] == ':' || buf[ii] == 126)) {
         hdr=false; 
         break;
       }
     return hdr;
   }
 
   /** static method that insures a seedname makes some sense.
    * 1)  Name is 12 characters long nnssssscccll.
    * 2) All characters are characters,  digits, spaces, question marks or dashes
    * 3) Network code contain blanks
    * 4) Station code must be at least 3 characters long
    * 5) Channel codes must be characters in first two places
    *@param name A seed string to check
    *@throws IllegalSeednameException if any of the above rules are violated.
    */
   public static void checkSeedName(String name) throws IllegalSeednameException {
     if(name.length() != 12 )
       throw new IllegalSeednameException("Length not 12 is "+name.length()+
           " in ["+Util.toAllPrintable(name)+"]");
 
     char ch;
     //char [] ch = name.toCharArray();
     for(int i=0; i<12; i++) {
       ch = name.charAt(i);
       if( !(Character.isLetterOrDigit(ch) || ch == ' ' || ch == '?' || ch == '_' ||
               ch == '-'))
         throw new IllegalSeednameException(
           "A seedname character is not letter, digit, space or [?-] ("+
             Util.toAllPrintable(name)+") at "+i);
     }
     if(name.charAt(0) == ' ' /*|| name.charAt(1) == ' '*/)  // GEOS is network 'G'
       throw new IllegalSeednameException(" network code blank ("+name+")");
     if(name.charAt(2) == ' ' || name.charAt(3) == ' ' || name.charAt(4) == ' ')
       throw new IllegalSeednameException("Station code too short ("+name+")");
     if( !(Character.isLetter(name.charAt(7)) && Character.isLetter(name.charAt(8)) &&
         Character.isLetterOrDigit(name.charAt(9))))
       throw new IllegalSeednameException("Channel code not Letter, Letter, LetterOrDigit ("+name+")");
 
 
   }
 }
