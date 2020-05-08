 package jp.shuri.android.SDCardSender;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.PrintWriter;
 
 import org.acra.collector.CrashReportData;
 import org.acra.sender.ReportSender;
 import org.acra.sender.ReportSenderException;
 
 import android.os.Environment;
 
 public class SDCardSender implements ReportSender {
 
 	@Override
 	public void send(CrashReportData report) throws ReportSenderException {
 		String sdcard = Environment.getExternalStorageDirectory().getPath();
 	    String path = sdcard + File.separator + "bug.txt";
 	    File file = new File(path);
 	    
 	    PrintWriter pw = null;
 	    try {
 	    	pw = new PrintWriter(new FileOutputStream(file));
 	    } catch (FileNotFoundException e) {
 	    	throw new ReportSenderException("Error file not found " + path, e);
 	    }
 
         for (final Object key : report.keySet()) {
            //final Object preliminaryValue = finalReport.get(key);
         	final Object preliminaryValue = report.get(key);
             final Object value = (preliminaryValue == null) ? "" : preliminaryValue;
             pw.println(key.toString() + " : " + value.toString());
         }
         pw.close();
 	}
 }
