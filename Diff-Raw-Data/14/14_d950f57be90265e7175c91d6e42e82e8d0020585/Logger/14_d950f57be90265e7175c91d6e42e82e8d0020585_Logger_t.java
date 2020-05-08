 /*
  * Copyright (C) 2010 France Telecom
  *
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  *
  *      http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  */
 
 package memoplayer;
 //#ifndef BlackBerry
 import javax.microedition.lcdui.Font;
 import javax.microedition.lcdui.Graphics;
 //#endif
import javax.microedition.rms.RecordEnumeration;
 import javax.microedition.rms.RecordStore;
 
 public class Logger {
     public final static String s_version = "1.5.2";
 //#ifdef debug.console
     private final static int MAX_MESSAGES = 100;
     private final static int MENU_MAIN = 0;
     private final static int MENU_DEBUG = 1;
     private final static int MENU_RMS = 2;
 
     private static String [] s_messages;
     private static int s_count, s_ch, s_offset, s_startRaw; // , s_cw;
     private static Font s_font;
     private static int s_maxLines;
     private static int s_firstLine = 0;
     private static int s_nbLines = 0;
     private static int s_touchBar = -1; // -1: first time, display help
     private static boolean s_fastScroll = false;
     private static int s_menu = 0;
     private static int s_rmsTableIndex = 0;
     private static String s_rmsTableName = "";
 
 //#ifdef profiling
     private static long s_startTime = System.currentTimeMillis() ;
     private static long [] s_timeStack;
     private static int s_currentSlot = 0;
 
     public synchronized static void profile (String s) {
         println ("[T:"+(System.currentTimeMillis() - s_startTime)+"]"+s);
     }
 
     public synchronized static void profileStart () {
         s_timeStack[s_currentSlot++] = System.currentTimeMillis();
     }
 
     public synchronized static void profileEnd (String s) {
         println ("[D:"+(System.currentTimeMillis()-s_timeStack[--s_currentSlot])+"] "+s);
     }
 //#endif
 
     static {
         s_messages = new String [MAX_MESSAGES+1];
         s_font = Font.getFont (Font.FACE_MONOSPACE, Font.STYLE_PLAIN, FontStyle.s_fontSmall);
         // s_cw = s_font.charWidth ('W');
         s_ch = s_font.getHeight ();
         println ("MeMo player V"+s_version);
 //#ifdef profiling
         s_timeStack = new long [128];
 //#endif
     }
 
     public static void setKey (int c) {
         switch (s_menu) {
         case MENU_MAIN:
             dispatchMainMenu(c);
             break;
         case MENU_RMS:
             dispatchRmsMenu(c);
             break;
         case MENU_DEBUG:
             dispatchDebugMenu(c);
             break;
         }
     }
 
     private static void dispatchNavigation (int c) {
         int step;
         switch (c) {
         case 'U' :
             s_offset += s_fastScroll ? 10 : 1;// -= s_offset > 0 ? 1 : 0;
             break;
         case 'D' :
             step = s_fastScroll ? 10 : 1;
             s_offset -= s_offset < step ? s_offset : step;
             break;
         case 'L' :
             step = s_fastScroll ? 20 : 5;
             s_startRaw -= s_startRaw > step ? step : s_startRaw;
             break;
         case 'R' :
             s_startRaw += s_fastScroll ? 20 : 5;
             break;
         case '7' :
             s_startRaw = 0;
             break;
         case '9' :
             int maxi = 0;
             for (int i = 0; i < s_nbLines; i++) {
                 int l = s_messages[i+s_firstLine].length ();
                 if (l > maxi) { maxi = l; }
             }
             s_startRaw = maxi - 20;
             break;
         case 'E' :
             s_startRaw = s_offset = 0;
             break;
         case 'Z' :
             s_startRaw = s_offset = 0;
             for (int i = 0; i < s_count; i++) {
                 s_messages[i] = null;
             }
             s_count = 0;
             break;
         default:
             Logger.println("Sorry, unknown command.");
         }
     }
 
     public static void dispatchMainMenu (int c) {
         switch (c) {
         case '1':
             CookyManager cm = CookyManager.s_protected;
             while (cm != null) {
                 Logger.println(cm.m_key+":"+cm.m_value);
                 cm = cm.m_next;
             }
             cm = CookyManager.s_root;
             while (cm != null) {
                 Logger.println(cm.m_key+":"+cm.m_value);
                 cm = cm.m_next;
             }
             break;
         case '2':
             Logger.println ("Running gc...");
             System.gc ();
             try { Thread.sleep(100); } catch (InterruptedException e) {}
             Runtime rt = Runtime.getRuntime ();
             Logger.println ("Mem: used: "+(rt.totalMemory()-rt.freeMemory())/1024+"ko free: "+rt.freeMemory()/1024+"ko, total: "+rt.totalMemory()/1024+"ko");
             break;
         case '3':
 //#ifdef api.mm
             MediaObject.printInfo ();
             MediaObject.printCapabilities ();
 //#endif
             break;
         case '4':
             s_fastScroll = ! s_fastScroll;
             Logger.println("Fast scrolling: "+(s_fastScroll ? "enabled" : "disabled"));
             break;
         case '5':
             s_menu = MENU_DEBUG;
             setKey('#');
             break;
         case '6':
             s_menu = MENU_RMS;
             setKey('#');
             break;
         case '#':
             Logger.println ("MeMo player "+s_version);
 //#ifdef BlackBerry
             Logger.println ("BlackBerry device: "+net.rim.device.api.system.DeviceInfo.getDeviceName());
 //#endif
             Logger.println ("Main menu:");
             Logger.println ("  1: display cookies");
             Logger.println ("  2: run gc & print free mem");
             Logger.println ("  3: Print media infos");
             Logger.println ("  4: Enable/disable fast scroll");
             Logger.println ("  5: Debug sub menu");
             Logger.println ("  6: RMS sub menu");
             Logger.println ("Navigation: ");
             Logger.println ("  7/9: start/end of lines");
             Logger.println ("  Pad: scroll");
             Logger.println ("  C: Clear console");
             Logger.println ("  Fire: go to top");
             Logger.println ("  *: Exit console");
             Logger.println ("  #: Display help");
             break;
         case 'C' :
             System.exit (1);
             break;
         default:
             dispatchNavigation(c);
         }
     }
     private static void dispatchDebugMenu (int c) {
         switch (c) {
         case '0':
             s_menu = MENU_MAIN;
             Logger.println("Back to main menu");
             break;
         case '1':
             MyCanvas.s_pauseIndicator = ! MyCanvas.s_pauseIndicator;
             Logger.println("Pause indicator: "+(MyCanvas.s_pauseIndicator ? "enabled" : "disabled"));
             break;
         case '2':
             if (MyCanvas.s_memBar>0) {
                 Logger.println("Membar: disabled");
                 MyCanvas.s_memBar=-1;
             } else {
                 Logger.println("Membar: enabled");
                 MyCanvas.s_memBar=1;
             }
             break;
         case '3':
             MyCanvas.s_debugClip = ! MyCanvas.s_debugClip;
             Logger.println("Clip debug: "+(MyCanvas.s_debugClip ? "enabled" : "disabled"));
             break;
         case '#':
             Logger.println("Debug menu:");
             Logger.println ("  0: Back to main menu");
             Logger.println ("  1: Enable/disable pause indicator");
             Logger.println ("  2: Enable/disable memory bar");
             Logger.println ("  3: Enable/disable clip debug");
             break;
         default:
             dispatchNavigation(c);
         }
     }
     private static void dispatchRmsMenu(int c) {
         switch (c) {
         case '0':
             s_menu = MENU_MAIN;
             Logger.println("Back to main menu");
             break;
         case '1':
             Logger.println ("RMS ------------------");
             Logger.println ("name: nbRecords / size");
             String[] rms = RecordStore.listRecordStores();
             if (rms != null && rms.length > 0) {
                 int totalSize = 0, available = -1;
                 StringBuffer sb = new StringBuffer ();
                 RecordStore rs = null;
                 for (int i = 0; i < rms.length; i++) {
                     sb.append(rms[i]);
                     sb.append(": ");
                     try {
                         rs = RecordStore.openRecordStore(rms[i], false);
                         if (rs != null) {
                             int size = rs.getSize();
                             int nbRecords = rs.getNumRecords();
                             if (available == -1) {
                                 available = rs.getSizeAvailable();
                             }
                             totalSize += size;
                             sb.append(nbRecords).append(" / ").append (size);
                             rs.closeRecordStore();
                         }
                     } catch (Exception e) {
                         sb.append("NULL");
                     }
                     rs = null;
                     Logger.println (sb.toString());
                     sb.setLength(0);
                 }
                 Logger.println ("Total size: "+totalSize);
                 Logger.println ("Available size: "+available);
                 s_rmsTableIndex = 0;
                 Logger.println("Current table: "+rms[s_rmsTableIndex]);
             }
             break;
         case '2':
             rms = RecordStore.listRecordStores();
             if (rms != null && s_rmsTableIndex > 0 && s_rmsTableIndex < rms.length) {
                 s_rmsTableName = rms[--s_rmsTableIndex];
                 Logger.println("Current table: "+s_rmsTableName);
             }
             break;
         case '3':
             rms = RecordStore.listRecordStores();
             if (rms != null && s_rmsTableIndex >= 0 && s_rmsTableIndex < rms.length-1) {
                 s_rmsTableName = rms[++s_rmsTableIndex];
                 Logger.println("Current table: "+s_rmsTableName);
             }
             break;
         case '4':
             if (s_rmsTableName != null) {
                 StringBuffer sb = new StringBuffer ();
                 try {
                     RecordStore rs = RecordStore.openRecordStore(s_rmsTableName, false);
                     if (rs != null) {
                        RecordEnumeration re = rs.enumerateRecords(null, null, false);
                        while (re.hasNextElement()) {
                            int id = re.nextRecordId();
                            int s = rs.getRecordSize(id);
                            sb.append(id).append(':').append(s);
                             if (s < 512) {
                                try { sb.append(" DATA:").append(new String(rs.getRecord(id), "UTF-8")); } catch (Exception e) {}
                             }
                             Logger.println (sb.toString());
                             sb.setLength(0);
                         }
                         rs.closeRecordStore();
                     }
                 } catch (Exception e) {
                     Logger.println(s_rmsTableName+": "+e+":"+e.getMessage());
                 }
             }
             break;
         case '#':
             Logger.println ("RMS menu:");
             Logger.println ("  0: Back to main menu");
             Logger.println ("  1: List tables");
             Logger.println ("  2: Switch to previous table");
             Logger.println ("  3: Switch to next table");
             Logger.println ("  4: Display current table");
             break;
         default:
             dispatchNavigation(c);
         }
     }
 
     public static boolean setPointer (int x, int y, int w, int h) {
         int H = h/10;
         int h3 = h/3, w3 = w/3;
         if (s_touchBar>0) {
             if (y>h-H) {
                 Logger.setKey('0'+(x*10/w));
             } else if (y<h3) {
                 Logger.setKey ('U');
             } else if (y>h-h3-H) {
                 Logger.setKey ('D');
             } else if (x < w3) {
                 Logger.setKey ('L');
             } else if (x > w-w3) {
                 Logger.setKey ('R');
             } else {
                 s_touchBar = 0; // disable
                 return false;
             }
         } else {
             if (s_touchBar == -1) { // first time, display help
                 Logger.setKey('#');
             }
             s_touchBar = 1; // enable
         }
         return true;
     }
 
     public synchronized static void println (String s) {
         if (s_count == MAX_MESSAGES) {
             System.arraycopy (s_messages, 1, s_messages, 0, MAX_MESSAGES-1);
             s_messages[--s_count] = null;
         }
         print (s);
         System.out.println ();
         s_count++;
     }
 
     public synchronized static void timePrintln (String s) {
 //#ifdef profiling
         println (">>["+(System.currentTimeMillis() - s_startTime)+"] "+s);
 //#else
         println (">> "+s);
 //#endif
     }
 
     public synchronized static void print (String s) {
         if (s_messages[s_count] == null) {
             s_messages[s_count] = s;
         } else {
             s_messages[s_count] += s;
         }
         System.out.print (s);
     }
 
     public static void show (Graphics g, int w, int h) {
         if (g == null) return;
         g.setColor (0);
         g.fillRect (0, 0, w, h);
         if (s_touchBar>0) {
             int h2 = h/2, w2 = w/2, H = h/10, y = h - H, W = w/10, W2 = W/2, yH2 = y + H/2;
             g.setColor(0x440000);
             g.fillTriangle(0, h2, 10, h2+10, 10, h2-10);
             g.fillTriangle(w, h2, w-10, h2+10, w-10, h2-10);
             g.fillTriangle(w2, 0, w2-10, 10, w2+10, 10);
             g.fillTriangle(w2, y, w2-10, y-10, w2+10, y-10);
             g.setColor(0xaa0000);
             g.setFont(s_font);
             for (int i=0; i<10;i++) {
                 int x = i*W+1;
                 g.drawRect(x, y, W-2, H);
                 g.drawChar((char)('0'+i), x+W2, yH2, g.BASELINE|g.HCENTER);
             }
             h -= H;
         }
         g.setColor (0X00AA00);
         g.setFont (s_font);
         s_maxLines = h / s_ch;
         s_firstLine = 0;
         if (s_count > s_maxLines) {
             s_firstLine = s_count - s_maxLines - s_offset;
         } else {
             s_firstLine = s_offset;
         }
         if (s_firstLine < 0) {
             s_firstLine = 0;
         }
         s_nbLines = Math.min (s_maxLines, s_count-s_firstLine);
         for (int i = 0; i < s_nbLines; i++) {
             String s = s_messages[i+s_firstLine];
             if (s != null) {
                 int l = s.length ()-s_startRaw;
                 if (l > 0) {
                     g.drawSubstring (s, s_startRaw, l, 0, i*s_ch, g.TOP|g.LEFT);
                 }
             }
         }
     }
 //#else
     public static void println (String s) { }
     public static void print (String s) { }
     public static void timePrintln (String s) { }
 //#endif
 }
