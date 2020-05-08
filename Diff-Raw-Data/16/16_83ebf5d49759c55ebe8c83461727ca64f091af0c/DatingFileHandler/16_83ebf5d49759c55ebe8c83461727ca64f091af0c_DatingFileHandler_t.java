 package edu.cornell.dlrg.logging;
 
 import java.io.IOException;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.FileNotFoundException;
 import java.util.Calendar;
 import java.util.GregorianCalendar;
 import java.util.logging.ErrorManager;
 import java.util.logging.Formatter;
 import java.util.logging.LogRecord;
 import java.util.logging.StreamHandler;
 
 public class DatingFileHandler
         extends StreamHandler {
         
     private File m_logDir;
     private int m_maxSize;
     private String m_ext;
     private long m_maxMillis;
     private int m_maxFiles;
     private int m_initialSize;
     private int m_currentSize;
     private long m_startMillis;
     private String m_fnStart;
     private Formatter m_formatter;
     
     private final static int MILLIS_PER_DAY=1000 * 60 * 60 * 24;
     
     private String m_nowFormatted;
     private boolean m_settingOutputStream;
         
     public DatingFileHandler(File logDir, int maxSize, int maxDays, 
             int maxFiles, String ext, Formatter formatter) 
             throws IOException, FileNotFoundException, IllegalArgumentException {
         setFormatter(formatter);
         if (maxSize<0 || maxDays<0 || maxFiles<0) {
             throw new IllegalArgumentException();
         }
         m_logDir=logDir;
         m_maxSize=maxSize;
         m_maxMillis=maxDays * MILLIS_PER_DAY;
         m_maxFiles=maxFiles;
         m_ext=ext;
         
         // come up with starting string using current system time
         m_startMillis=System.currentTimeMillis();
         m_fnStart=getFormatted(new GregorianCalendar()) + "-";
         m_initialSize=formatter.getHead(this).length() 
                 + formatter.getTail(this).length(); 
         m_currentSize=m_initialSize;
         
         setOutputStream(new FileOutputStream(new File(m_logDir, m_fnStart 
                 + m_ext)));
     }
     
     public void setFormatter(Formatter formatter) {
         m_formatter=formatter;
         super.setFormatter(formatter);
     }
     
     public void publish(LogRecord record) {
         if (isLoggable(record) && m_formatter!=null) {
             m_currentSize+=m_formatter.format(record).length();
             boolean needNewFile=false;
             if (m_maxSize>0) {
                 if (m_currentSize>m_maxSize) {
                    // if finish would happen in the same second as
                    // start, wait till some more time has passed
                    if (m_startMillis + 1000 < System.currentTimeMillis()) {
                        needNewFile=true;
                    }
                 }
             }
             if (m_maxMillis>0) {
                 if (System.currentTimeMillis() - m_startMillis > m_maxMillis) {
                     needNewFile=true;
                 }
             }
             if (needNewFile) {
                 try {
                     // set the new one first, then rename the old one
                     m_nowFormatted=getFormatted(new GregorianCalendar());
                     m_settingOutputStream=true;
                     setOutputStream(new FileOutputStream(new File(m_logDir,
                             m_nowFormatted + "-" + m_ext)));
                    doRename();
                     m_settingOutputStream=false;
                     // set these for next time
                     m_fnStart=m_nowFormatted + "-";
                     m_currentSize=m_initialSize;
                     m_startMillis=System.currentTimeMillis(); 
                 } catch (FileNotFoundException fnfe) {
                 }
             }
             super.publish(record);
         }
     }
     
     public void close() {
         super.close();
        doRename();
    }
    
    private void doRename() {
         if (!m_settingOutputStream) {
             m_nowFormatted=getFormatted(new GregorianCalendar());
         }
        File oldFile=new File(m_logDir, m_fnStart + m_ext);
         oldFile.renameTo(new File(m_logDir, m_fnStart 
                 + m_nowFormatted + m_ext));
     }
     
     private String getFormatted(GregorianCalendar cal) {
         StringBuffer b=new StringBuffer();
         b.append(cal.get(Calendar.YEAR));
         int month=cal.get(Calendar.MONTH)-cal.getMinimum(Calendar.MONTH)+1;
         if (month<10) {
             b.append("0");
         }
         b.append(month);
         int day=cal.get(Calendar.DAY_OF_MONTH)
                 - cal.getMinimum(Calendar.DAY_OF_MONTH)+1;
         if (day<10) {
             b.append("0");
         }
         b.append(day);
         int hour=cal.get(Calendar.HOUR_OF_DAY)
                 - cal.getMinimum(Calendar.HOUR_OF_DAY)+1;
         if (hour<10) {
             b.append("0");
         }
         b.append(hour);
         int minute=cal.get(Calendar.MINUTE)-cal.getMinimum(Calendar.MINUTE)+1;
         if (minute<10) {
             b.append("0");
         }
         b.append(minute);
         int second=cal.get(Calendar.SECOND)-cal.getMinimum(Calendar.SECOND)+1;
         if (second<10) {
             b.append("0");
         }
         b.append(second);
         return b.toString();
     }
     
 }
