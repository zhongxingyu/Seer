 /*
  *   LogCap: Capture system logs to files.
  *   Copyright (C) 2012  Jason Tian
  *
  *   This program is free software: you can redistribute it and/or modify
  *   it under the terms of the GNU General Public License as published by
  *   the Free Software Foundation, either version 3 of the License, or
  *   (at your option) any later version.
  *
  *   This program is distributed in the hope that it will be useful,
  *   but WITHOUT ANY WARRANTY; without even the implied warranty of
  *   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *   GNU General Public License for more details.
  *
  *   You should have received a copy of the GNU General Public License
  *   along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package org.jasontian.logcap.util;
 
 import android.content.Context;
 import android.text.SpannableString;
 import android.text.style.ForegroundColorSpan;
 import android.util.Log;
 
 import org.jasontian.logcap.R;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 
 /**
  * @author Jason Tian
  */
 public class Util {
 
     public static final String TAG = "LOGCAP";
     
     public static final String EXTRA_START = "start";
     
     public static final String EXTRA_FORMAT = "format";
     
     public static final String EXTRA_BUFFER = "buffer";
 
     private static final String LOG_DIR = "/sdcard/log/";
 
     public static Process capture(String buffer, String format) throws IOException {
         File dir = new File(LOG_DIR);
         if (!dir.exists()) {
             if (!dir.mkdir()) {
                 throw new IOException("Can not mkdir: " + dir);
             }
         }
         return new ProcessBuilder(
                 new String[] {
                         "/system/bin/logcat",
                         "-v", format, "-b", buffer, "-f",
                         new File(dir, buffer + ".txt").getAbsolutePath()
                 }).start();
     }
 
     public static SpannableString getSpannedBufferInfo(String info) {
         SpannableString ss = new SpannableString(info);
         ss.setSpan(new ForegroundColorSpan(0xff009fe3), info.indexOf('('),
                 info.indexOf(')') + 1, 0);
         return ss;
     }
 
     public static String[] getAvailableBuffers() {
         String[] bufs = null;
         try {
             String out = isToString(new ProcessBuilder(new String[] {
                     "ls",
                     "/dev/log"
             }).start().getInputStream());
             if (out != null) {
                 bufs = out.split("\\s+");
             }
         } catch (IOException e) {
             throw new IllegalStateException("No available log buffer");
         }
         return bufs;
     }
 
     public static void clearBuffer(String buffer) throws IOException {
         new ProcessBuilder(new String[] {
                 "/system/bin/logcat", "-c", "-b", buffer
         }).start();
     }
 
     public static String getBufferInfo(String buffer) throws IOException {
         return isToString(new ProcessBuilder(new String[] {
                 "/system/bin/logcat",
                 "-g",
                 "-b",
                 buffer
         }).start().getInputStream());
     }
 
    public static void removeLogFile(File log) {
         if (log != null && log.isFile()) {
             log.delete();
         }
     }
 
     public static String getLogFileInfo(Context context) {
         int num = 0;
         float size = 0f;
         File[] list = getLogFiles();
         if (list != null) {
             num = list.length;
             for (File f : list) {
                 if (f.isFile()) {
                     size += f.length();
                 }
             }
         }
         size /= 1024 * 1024;
         return context.getString(R.string.file_number, num) + " "
                 + context.getString(R.string.file_size, size);
     }
 
     public static File[] getLogFiles() {
         return new File(LOG_DIR).listFiles();
     }
 
     private static String isToString(InputStream is) {
         String str = null;
         ByteArrayOutputStream bos = null;
         try {
             bos = new ByteArrayOutputStream();
             byte[] buf = new byte[1024];
             int len = -1;
             while ((len = is.read(buf)) != -1) {
                 bos.write(buf, 0, len);
             }
             str = bos.toString("utf-8");
         } catch (IOException e) {
             Log.d(TAG, "error writing bytes", e);
         } finally {
             if (bos != null) {
                 try {
                     bos.close();
                 } catch (IOException e) {
                     Log.i(TAG, "error closing bos", e);
                 }
             }
             if (is != null) {
                 try {
                     is.close();
                 } catch (IOException e) {
                     Log.i(TAG, "error closing is", e);
                 }
             }
         }
         return str;
     }
 }
