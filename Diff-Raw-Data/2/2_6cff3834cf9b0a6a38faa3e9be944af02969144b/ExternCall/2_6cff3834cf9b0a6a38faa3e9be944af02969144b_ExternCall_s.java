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
 import java.util.*;
 
 import javax.microedition.lcdui.Display;
 import javax.microedition.lcdui.Font;
 import javax.microedition.rms.RecordStore;
 
 //#ifdef api.mm
 import javax.microedition.media.Manager;
 import javax.microedition.midlet.MIDlet;
 //#endif
 
 //#ifdef jsr.amms
 import javax.microedition.amms.*;
 import javax.microedition.amms.control.*;
 import javax.microedition.amms.control.imageeffect.*;
 //#endif
 
 //#ifdef jsr.75
 import java.io.*;
 import javax.microedition.io.*;
 import javax.microedition.io.file.*;
 //#endif
 
 //#ifdef jsr.179
 import javax.microedition.location.Coordinates;
 import javax.microedition.location.Criteria;
 import javax.microedition.location.Location;
 import javax.microedition.location.LocationListener;
 import javax.microedition.location.LocationProvider;
 //#endif
 
 class ExternCall {
     static Random s_rnd = new Random (System.currentTimeMillis());
     static String [] s_mimeTypes;
 //#ifdef api.mm
     static {
         //MCP: Prevent classNotFoundException when MMAPI is not supported
         try { s_mimeTypes = Manager.getSupportedContentTypes (null); } catch (Throwable e) {}
     }
 //#endif
 
 //#ifdef api.im
     static IMHelper imHelper = new IMHelper();
     static IMObserver imObserver = new IMObserver();
 //#endif
     
 //#ifdef api.pushlet
     static Pushlet pushlet = new Pushlet();
 //#endif
 
 //#ifdef api.socket
     static final int MAX_SOCKETS = 4;
     static Socket s_socket[];
 //#endif
 
 
     static int getFreeSlot(Object[] array) {
         int i;
         for(i=0; i<array.length; i++) {
             if (array[i] == null) {
                 return i;
             }
         }
         return -1;
     }
 
     static void doCall (Machine mc, Context c, int o, int m, Register [] registers, int r, int nbParams) {
         switch (o) {
 
         case 0: doBrowser (mc, c, m, registers, r, nbParams); break;
         case 1: doMath (c, m, registers, r, nbParams); break;
         case 2: doString (c, m, registers, r, nbParams); break;
         case 3: doDate (c, m, registers, r, nbParams); break;
         case 4: doFile (c, m, registers, r, nbParams); break;
 
 //#ifdef api.jsonrpc
         case 5: TaskQueue.doJsonRpc (mc, c, m, registers, r, nbParams); break;
 //#endif
 
 //#ifdef api.persist
         case 6: JSPersist.doPersist (c, m, registers, r, nbParams); break;
 //#endif
 
 //#ifdef api.array
         case 7: JSArray.doArray (c, m, registers, r, nbParams); break;
         case 8: JSArray.doEnumeration (c, m, registers, r, nbParams); break;
 //#endif
 
         case 9: doMessaging (c, m, registers, r, nbParams); break;
 
         case 10: doXml (c, m, registers, r, nbParams); break;
 //#ifdef api.xparse
         case 11: doJson (c, m, registers, r, nbParams); break;
 //#endif
 
         case 12: doContact (c, m, registers, r, nbParams); break;
         case 13: doLocation (c, m, registers, r, nbParams); break;
 
 //#ifdef api.im
         case 14: doIM (c, m, registers, r, nbParams); break;
 //#endif
 
 //#ifdef api.pushlet
         case 15: doPushlet (c, m, registers, r, nbParams); break;
 //#endif
 
 //#ifdef api.socket
         case 16: doSocket (c, m, registers, r, nbParams); break;
 //#endif
         
 //#ifdef api.ad
         case 17: doAd (c, m, registers, r, nbParams); break;
 //#endif
 
         case 18: doHttp (c, m, registers, r, nbParams); break;
         case 19: doStyle (c, m, registers, r, nbParams); break;
         case 20: doGPS (c, m, registers, r, nbParams); break;
         case 21: doAppWidget (mc, c, m, registers, r, nbParams); break;
         default:
             Logger.println("No API: "+o);
         }
     }
 
     static void doBrowser (Machine mc, Context c, int m, Register [] registers, int r, int nbParams) {
 //#ifdef MM.namespace
         if ((m==1 || m==15 || m==16 || m==17 || m==19) && Namespace.getName() != "") {
             Logger.println("Browser API: Access to some methods are unauthorize from the current Namespace.");
             return;
         }
 //#endif
         switch (m) {
         case 0: // print
             Logger.timePrintln (registers[r].getString());
             return;
         case 1: // exit
             c.again = false; return;
         case 2: // setCooky
             CookyManager.set (registers[r].toString(), registers[r+1].toString());
             return;
         case 3: // getCooky
             registers[r].setString (CookyManager.get (registers[r].toString()));
             return;
         case 4: // getIntCooky
             try {
                 registers[r].setInt (Integer.parseInt (CookyManager.get (registers[r].toString())));
             } catch (NumberFormatException e) {
                 registers[r].setInt(-1);
             }
             return;
         case 5: // getWidth
             registers[r].setInt (c.width);
             return;
         case 6: // getHeight
             registers[r].setInt (c.height);
             return;
 
         case 7: // getNbMimeTypes () => Int
             registers[r].setInt (s_mimeTypes != null ? s_mimeTypes.length : 0);
             return;
         case 8: // getMimeType (Int index) -> string
             registers[r].setString (s_mimeTypes != null ? s_mimeTypes[registers[r].getInt ()] : null);
             return;
         case 9: // getFps -> int
             registers[r].setInt (c.fps);
             return;
         case 10: // getIdle -> int
             registers[r].setInt (c.cpu);
             return;
         case 11: // getRecord (String name) -> String
             registers[r].setString (CacheManager.getManager().getRecord (registers[r].getString()));
             return;
         case 12: // setRecord (String name, String value)
             registers[r].setBool (CacheManager.getManager().setRecord (registers[r].getString(), registers[r+1].getString()));
             return;
 //#ifdef api.traffic
         case 13: // getSessionTraffic ()
             registers[r].setInt(Traffic.getSession());
             return;
         case 14: // getTotalTraffic ()
             registers[r].setInt(Traffic.getTotal());
             return;
         case 15: // resetTraffic ()
             Traffic.reset();
             return;
 //#endif
         case 16: // deleteCooky (String name)
             CookyManager.remove(registers[r].toString());
             return;
         case 17: // cleanCookies ()
             CookyManager.clean();
             return;
         case 18: // getJadProperty ()
             registers[r].setString(MiniPlayer.getJadProperty(registers[r].getString()));
             return;
         case 19: // cleanRMS()
 //#ifdef api.traffic
             Traffic.reset();
 //#endif
             CacheManager.deleteAll();
             return;
 //#ifdef api.lcdui
         case 20: // displayTextBox(String title, String text, int size, int type, String okLabel, String cancelLabel, [callback])
         {
             boolean bRet = LcdUI.displayTextBox(
                     registers[r].getString(),
                     registers[r+1].getString(),
                     registers[r+2].getInt(),
                     registers[r+3].getInt(),
                     registers[r+4].getString(),
                     registers[r+5].getString());
             if (nbParams == 7) { // 7 parameters => return value by callback
                 int cb = registers[r+6].getInt ();
                 Register[] params = new Register[] { new Register(), new Register() };
                 params[0].setBool (bRet);
                 params[1].setString (LcdUI.getTextInput ());
                 c.script.addCallback (cb, params);
                 c.script.releaseMachineOnInit = false;
                 MyCanvas.composeAgain = true;
             }
             registers[r].setString (LcdUI.getTextInput ());
             return;
         }
         case 21: // displayAlert(String title, String text)
             LcdUI.displayAlert(
                 registers[r].getString(),
                 registers[r+1].getString(),
                 null,
                 javax.microedition.lcdui.AlertType.INFO,
                 javax.microedition.lcdui.Alert.FOREVER);
             return;
 //#endif
         case 22: // getVersion
             registers[r].setString(Logger.s_version);
             return;
         case 23: // vibrate (duration in seconds)
             MiniPlayer.vibrate (FixFloat.fixMul(registers[r].getFloat(), 1000));
             return;
         case 24: // logCapability
 //#ifdef api.mm
             MediaObject.printInfo ();
             MediaObject.printCapabilities ();
 //#endif
             return;
         //case 25: // UNUSED (DEPRECATED wakeup call)
         //    return;
         case 26: //transformImage (String in, String out, int width, int height, boolean rotate)
 //#ifdef jsr.amms            
             try {
                 MediaProcessor mp = GlobalManager.createMediaProcessor("image/jpeg");
                 Logger.println ("opening input: "+registers[r+0].getString ());
                 FileConnection fileIn  = (FileConnection) Connector.open(registers[r+0].getString ());
 
                 Logger.println ("opening output: "+registers[r+1].getString ());
                 FileConnection fileOut = (FileConnection) Connector.open(registers[r+1].getString ());
                 if (!fileOut.exists()) {
                     fileOut.create();
                 }
                 InputStream inputStream   = fileIn.openDataInputStream ();   // create a InputStream that contains the source image
                 OutputStream outputStream = fileOut.openDataOutputStream (); // create a OutputStream that will receive the resulting image
                 Logger.println ("setting MP intput for: "+registers[r+0].getString ());
                 mp.setInput (inputStream, MediaProcessor.UNKNOWN);
                 Logger.println ("setting MP output for:"+registers[r+1].getString ());
                 mp.setOutput(outputStream);
                 
                 // Define effects to be applied during processing
                 ImageTransformControl imageTrs = 
                     (ImageTransformControl)mp.getControl("javax.microedition.amms.control.imageeffect.ImageTransformControl");
                 imageTrs.setEnforced (true);
                 imageTrs.setEnabled (true);
                 Logger.println ("setting transform: w="+registers[r+2].getInt ()+", h="+registers[r+3].getInt ()+", r="+registers[r+4].getInt ());
                 imageTrs.setTargetSize (registers[r+2].getInt (), registers[r+3].getInt (), registers[r+4].getInt ());
                 
                 // Set output format
                 ImageFormatControl fc = (ImageFormatControl)mp.getControl("javax.microedition.amms.control.ImageFormatControl");
                 fc.setFormat("image/jpeg");
                 fc.setParameter("quality", 80);
                 
                 // Do the actual processing. If you do not want to use a blocking call, 
                 // use start() and MediaProcessorListener.
                 Logger.println ("starting transformation");
                 mp.complete();
                 fileIn.close ();
                 fileOut.close ();
                 Logger.println ("transformation done");
             } catch (Exception e) {
                 Logger.println  ("Exception during transform image: "+e);
             }
 //#endif
             return;
         case 27: //sendMessage (String messageURL, String dataToSend [, String moreData]*)
             if (nbParams == 2) {
                 Message.sendMessage(registers[r].getString(), registers[r+1].getString());
             } else if (nbParams > 2) {
                 MFString mess = new MFString(--nbParams);
                 for (int i=0; i<nbParams; i++) {
                     mess.setValue(i, registers[r+i+1].getString());
                 }
                 Message.sendMessage(registers[r].getString(), mess);
             }
             return;
         case 28: // getProperty (String property)
 //#ifndef BlackBerry
             registers[r].setString(System.getProperty(registers[r].getString()));
 //#else
             String property = registers[r].getString();
             String value = System.getProperty(registers[r].getString());
             if (property.compareTo("microedition.platform")==0) {
               value = value+" "+net.rim.device.api.system.DeviceInfo.getDeviceName();
             }
             registers[r].setString(value);
 //#endif
             return;
         case 29: // setTimeout (method, timeout, [args]+)
             final int timeout = registers[r+1].getInt ();
             if (timeout > 0) {
                 final Script script = c.script;
                 final int cb = registers[r].getInt ();
                 nbParams -= 2; r += 2;
                 final Register[] params = new Register[nbParams];
                 for (int i=0; i<nbParams; i++) {
                     (params[i] = new Register()).set (registers[r+i]);
                 }
                 script.releaseMachineOnInit = false; // prevent release of Machine on Script start
                 script.addCallback (cb, params, c.time + timeout); // add delayed callback
             }
             return;
         case 30: // getCountryCode
             registers[r].setString(Inline.getMobileCountry ());
             return;
         case 31: // deleteRecord
             CacheManager.getManager().deleteRecord (registers[r].getString());
             return;
         case 32: // deleteAllRecords
             CacheManager.getManager().erase ();
             return;
         case 33: // getLanguage
             registers[r].setString(Inline.getMobileLanguage ());
             return;
         case 34: // getFreeMemory
             registers[r].setInt ((int) (Runtime.getRuntime().freeMemory () / 1024));
             return;
         case 35: // getTotalMemory
             Runtime.getRuntime().gc ();
             registers[r].setInt ((int) (Runtime.getRuntime().totalMemory () / 1024));
             return;
         case 36: // forceBacklight (bool force)
 //#ifdef jsr.nokia-ui
             if (BackLight.isAvailable()) {
                 if (registers[r].getBool ()) {
                     BackLight.start ();
                 } else {
                     BackLight.stop ();
                 }
                 registers[r].setBool (true);
             } else {
                 registers[r].setBool (false);
             }
 //#else
             registers[r].setBool (false);
 //#endif
             return;
         case 37: // deleteStore (String storeName)
 //#ifdef MM.namespace
             if (Namespace.getName () != "") return;
 //#endif
             CacheManager.delete (registers[r].getString());
             return;
         case 38: // setStore (String storeName)
 //#ifdef MM.namespace
             if (Namespace.getName () != "") return;
 //#endif
             CacheManager.setStore (registers[r].getString());
             return;
         case 39: // hasRecord (String name)
             registers[r].setBool (CacheManager.getManager().hasRecord (registers[r].getString()));
             return;
         default:
             System.err.println ("doBrowser(m:"+m+")Static call: Invalid method");
             return;
         }
     }
 
     static void doMath (Context c, int m, Register [] registers, int r, int nbParams) {
         int n;
         //  0:cos 1:sin 2:floor 3:ceil  4:sqrt  5:rand  6:abs   7:acos  :8:asin  9:atan
         // 10:PI 11:E  12:dadd 13:dsub 14:dmul 15:ddiv 16:dsin 17:dasin 18:dcos 19:dacos
         // 20:dtan 21:datan 22:datan2 23:dfloor 24:dsup 25:dsupeq 26:dinf 27:dinfeq 28:dabs
 
         switch (m) {
         case 0: // cos
             n = registers[r].getFloat ();
             registers[r].setFloat (FixFloat.fixCos(n));
             return;
         case 1: // sin
             n = registers[r].getFloat ();
             registers[r].setFloat (FixFloat.fixSin(n));
             return;
         case 2: // floor
             n = registers[r].getFloat ();
             registers[r].setInt (FixFloat.fix2int(n & 0xFFFF0000));
             return;
         case 3: // ceil
             n = registers[r].getFloat ();
             registers[r].setInt (FixFloat.fix2int((n + 65536) & 0xFFFF0000));
             return;
         case 4: // sqrt
             n = registers[r].getFloat ();
             registers[r].setFloat (FixFloat.sqrt (n));
             return;
         case 5: // rand
             //long l = (System.currentTimeMillis ()*65) & 0x0000FFFF;
             registers[r].setFloat (s_rnd.nextInt () & 0x0000FFFF);
             return;
         case 6: // abs
             n = registers[r].getFloat ();
             registers[r].setFloat (n > 0 ? n : -n);
             return;
         case 7: // acos
             n = registers[r].getFloat ();
             registers[r].setFloat (FixFloat.arcCos(n));
             return;
         case 8: // asin
             n = registers[r].getFloat ();
             registers[r].setFloat (FixFloat.arcSin(n));
             return;
         case 9: // atan
             n = registers[r].getFloat ();
             registers[r].setFloat (FixFloat.arcTan(n));
             return;
         case 10: // PI
            registers[r].setFloat (205587);
             return;
         case 11: // E
             registers[r].setFloat (178145);
             return;
         default:
             try {
                 double d1 = Double.parseDouble(registers[r].getString());
                 double d2 = 0.0;
                 if (nbParams == 2) {
                     d2 = Double.parseDouble(registers[r + 1].getString());
                 }
                 switch (m) {
                 case 12: // dadd
                     d1 += d2;
                     registers[r].setString(String.valueOf(d1));
                     return;
                 case 13: // dsub
                     d1 -= d2;
                     registers[r].setString(String.valueOf(d1));
                     return;
                 case 14: // dmul
                     d1 *= d2;
                     registers[r].setString(String.valueOf(d1));
                     return;
                 case 15: // ddiv
                     if (Math.abs(d2) < Double.MIN_VALUE) {
                         registers[r].setString("NaN");
                     } else {
                         d1 /= d2;
                         registers[r].setString(String.valueOf(d1));
                     }
                     return;
                 case 16: // dsin
                     registers[r].setString(String.valueOf(Math.sin(d1)));
                     return;
                 case 17: // dasin
                     registers[r].setString(String.valueOf(asin(d1)));
                     return;
                 case 18: // dcos
                     registers[r].setString(String.valueOf(Math.cos(d1)));
                     return;
                 case 19: // dacos
                     registers[r].setString(String.valueOf(acos(d1)));
                     return;
                 case 20: // dtan
                     registers[r].setString(String.valueOf(Math.tan(d1)));
                     return;
                 case 21: // datan
                     registers[r].setString(String.valueOf(atan(d1)));
                     return;
                 case 22: // datan2
                     registers[r].setString(String.valueOf(atan2(d1, d2)));
                     return;
                 case 23: // dfloor
                     registers[r].setString(String.valueOf(Math.floor(d1)));
                     return;
                 case 24: // dsup
                     registers[r].setBool(d1 > d2);
                     return;
                 case 25: // dsupeq
                     registers[r].setBool(d1 >= d2);
                     return;
                 case 26: // dinf
                     registers[r].setBool(d1 < d2);
                     return;
                 case 27: // dinfeq
                     registers[r].setBool(d1 <= d2);
                     return;
                 case 28: // dabs
                     registers[r].setString(String.valueOf(Math.abs(d1)));
                     return;
                 case 29: // dLog
                     registers[r].setString(String.valueOf(dlog(d1)));
                     return;
                 default:
                     Logger.println("doMath (m:"+m+") Static call: Invalid method");
                 }
             } catch (Exception e) {
                 Logger.println("doMath error: " + e);
             }
         }
     }
 
 //#ifdef api.xparse
     static JsonReader [] s_readers;
     final static int MAX_READERS = 12;
 
     static void doJson (Context c, int m, Register [] registers, int r, int nbParams) {
         int id, res;
         JsonReader jr = null;
         switch (m) {
         case 0: // int open (String s, mode)
             if (s_readers == null) {
                 s_readers = new JsonReader[MAX_READERS];
                 id = 0;
             } else {
                 id = getFreeSlot (s_readers);
             }
             if (id >= 0) {
                 s_readers[id] = new JsonReader (registers[r].getString(), registers[r+1].getInt() );
             }
             registers[r].setInt (id);
             return;
         case 1: // void close (int id)
             id = registers[r].getInt ();
             if (id >= 0 && id < MAX_READERS && s_readers[id] != null) {
                 s_readers[id].close ();
                 s_readers[id] = null;
             }
             return;
         case 2: //isNewData (id)
             id = registers[r].getInt ();
             if (id >= 0 && id < MAX_READERS && (jr = s_readers[id]) != null) {
                 registers[r].setBool (jr.isNewData ());
             } else {
                 registers[r].setBool (false);
             }
             return;
         case 3: //find (int id, String s)
             id = registers[r].getInt ();
             res = 0;
             if (id >= 0 && id < MAX_READERS && (jr = s_readers[id]) != null) {
                 res = jr.find (registers[r+1].getString()) ? 1 : 0;
             }
             registers[r].setInt (res);
             return;
         case 4: // getType (int id)
             id = registers[r].getInt ();
             res = JsonReader.ERROR;
             if (id >= 0 && id < MAX_READERS && (jr = s_readers[id]) != null) {
                 res = jr.getType ();
             }
             registers[r].setInt (res);
             return;
         case 5: // getSize (int id)
             id = registers[r].getInt ();
             res = JsonReader.ERROR;
             if (id >= 0 && id < MAX_READERS && (jr = s_readers[id]) != null) {
                 res = jr.getSize ();
             }
             registers[r].setInt (res);
             return;
         case 6: // getValue (int id)
             id = registers[r].getInt ();
             if (id >= 0 && id < MAX_READERS && (jr = s_readers[id]) != null) {
                 switch (jr.getType ()) {
                 case JsonReader.NUMBER: registers[r].setFloat (jr.getNumberValue()); return;
                 case JsonReader.STRING: registers[r].setString (jr.getStringValue()); return;
                 case JsonReader.TRUE: registers[r].setInt (1); return;
                 case JsonReader.FALSE: registers[r].setInt (0); return;
                 case JsonReader.NULL: registers[r].setInt (0); return;
                 }
             }
             registers[r].setInt (0);
             return;
         case 7: // STRING
             registers[r].setInt(JsonReader.STRING);
             return;
         case 8: // NUMBER
             registers[r].setInt(JsonReader.NUMBER);
             return;
         case 9: // OBJECT
             registers[r].setInt(JsonReader.OBJECT);
             return;
         case 10: // ARRAY
             registers[r].setInt(JsonReader.ARRAY);
             return;
         case 11: // TRUE
             registers[r].setInt(JsonReader.TRUE);
             return;
         case 12: // TRUE
             registers[r].setInt(JsonReader.FALSE);
             return;
         case 13: // NULL
             registers[r].setInt(JsonReader.NULL);
             return;
         case 14: // ERROR
             registers[r].setInt(JsonReader.ERROR);
             return;
         case 15: // BUFFER 
             registers[r].setInt (BaseReader.BUFFER);
             return;
         case 16: // URL 
             registers[r].setInt (BaseReader.URL);
             return;
         case 18: // DEBUG
             registers[r].setInt (BaseReader.DEBUG);
             return;
         case 19: // ASYNC
             registers[r].setInt (BaseReader.ASYNC);
             return;
         }
     }
 //#endif
 
     static XmlDom s_xmlDoms[];
     final static int MAX_XML_DOMS = 12;
     
     static XmlDom getXmlDom (int id) {
         return (id >= 0 && id < MAX_XML_DOMS && s_xmlDoms != null && s_xmlDoms[id] != null) ? s_xmlDoms[id] : null;
     }
     
     static void doXml (Context c, int m, Register [] registers, int r, int nbParams) {
         XmlDom xd = null;
         int id = -1;
         switch (m) {
         case 0: //open (String s, int mode, [Function callback])
             if (s_xmlDoms == null) {
                 s_xmlDoms = new XmlDom[MAX_XML_DOMS];
                 id = 0;
             } else {
                 id = getFreeSlot(s_xmlDoms);
             }
             if (id >= 0) {
                 final String buffer = registers[r].getString ();
                 final int mode = registers[r+1].getInt();
                 final int cb = nbParams > 2 ? registers[r+2].getInt() : -1;
                 s_xmlDoms[id] = new XmlDom (); // reserve slot with empty DOM
                 if (cb > 0 || (mode & XmlDom.ASYNC) == XmlDom.ASYNC) {
                     final XmlDom dom = s_xmlDoms[id];
                     final int xid = id;
                     final Script script = c.script;
                     if (cb > 0) {
                         script.releaseMachineOnInit = false; // prevent release of Machine on Script start
                     }
                     new Thread () {
                         public void run () {
                             int id = xid;
                             int retCode = dom.populate (buffer, mode);
                             if (dom.hasData() == false) { // error
                                 s_xmlDoms[id] = null;
                                 id = -1;
                             }
                             if (cb > 0) { // call callback
                                 Register[] params = new Register[] { new Register(), new Register(), new Register() };
                                 params[0].setString (buffer);
                                 params[1].setInt (retCode);
                                 params[2].setInt (id);
                                 script.addCallback (cb, params);
                                 MiniPlayer.wakeUpCanvas ();
                             }
                         }
                     }.start();
                 } else { // synchronous
                     s_xmlDoms[id].populate (buffer, mode);
                     if (s_xmlDoms[id].hasData() == false) { // error
                         s_xmlDoms[id] = null;
                         id = -1;
                     }
                 }
             }
             registers[r].setInt (id);
             return;
         case 1: //close (int id)
             id = registers[r].getInt ();
             if ((xd = getXmlDom (id)) != null) {
                 s_xmlDoms[id] = null;
             }
             return;
         case 2: //isNewData (id)
             if ((xd = getXmlDom (registers[r].getInt ())) != null) {
                 registers[r].setBool (xd.hasData());
             } else {
                 registers[r].setBool (false);
             }
             return;
         case 3: //find (id, String path)
             if ((xd = getXmlDom (registers[r].getInt ())) != null) {
                 registers[r].setBool (xd.find (registers[r+1].getString()));
             } else {
                 registers[r].setBool (false);
             }
             return;
 
         case 4: //goto (id, String path)
             if ((xd = getXmlDom (registers[r].getInt ())) != null) {
                 registers[r].setBool (xd.goTo (registers[r+1].getString()));
             } else {
                 registers[r].setBool (false);
             }
             return;
 
         case 5: //int getValue (id)
             if ((xd = getXmlDom (registers[r].getInt ())) != null) {
                 registers[r].setString (xd.getValue ());
             } else {
                 registers[r].setString ("");
             }
             return;
         case 6: //int getNbAttributes (id)
             if ((xd = getXmlDom (registers[r].getInt ())) != null) {
                 registers[r].setInt (xd.getNbAttributes ());
             } else {
                 registers[r].setInt (0);
             }
             return;
         case 7: //String getAttributeName (int id, int index)
             if ((xd = getXmlDom (registers[r].getInt ())) != null) {
                 registers[r].setString (xd.getAttributeName (registers[r+1].getInt()));
             } else {
                 registers[r].setString ("");
             }
             return;
         case 8: //String getAttributeValue (int id, int index | String name)
             if ((xd = getXmlDom (registers[r].getInt ())) != null) {
                 if (registers[r+1].m_type == Register.TYPE_STRING) {
                     registers[r].setString (xd.getAttributeValue (registers[r+1].getString()));
                 } else {
                     registers[r].setString (xd.getAttributeValue (registers[r+1].getInt()));
                 }
             } else {
                 registers[r].setString ("");
             }
             return;
         case 9: //int getNbChildren  (int id) 
             if ((xd = getXmlDom (registers[r].getInt ())) != null) {
                 registers[r].setInt (xd.getNbChildren ());
             } else {
                 registers[r].setInt (0);
             }
 
             return;
         case 10: //bool isTextChild  (int id, int index) 
             if ((xd = getXmlDom (registers[r].getInt ())) != null) {
                 registers[r].setBool (xd.isTextChild (registers[r+1].getInt()));
             } else {
                 registers[r].setBool (false);
             }
             return;
         case 11: //String getChildValue  (int id, int index) 
             if ((xd = getXmlDom (registers[r].getInt ())) != null) {
                 registers[r].setString (xd.getChildValue (registers[r+1].getInt()));
             } else {
                 registers[r].setString ("");
             }
             return;
         case 12: // BUFFER 
             registers[r].setInt (XmlDom.BUFFER);
             return;
         case 13: // URL 
             registers[r].setInt (XmlDom.URL);
             return;
         case 14: // BML_ENCODING (DEPRECATED)
             registers[r].setInt (XmlDom.BML);
             return;
         case 15: // DEBUG
             registers[r].setInt (XmlDom.DEBUG);
             return;
         case 16: // ASYNC
             registers[r].setInt (XmlDom.ASYNC);
             return;
         case 17: // HTML
             registers[r].setInt (XmlDom.HTML);
             return;
         case 18: //bool isSelfClosing (int id) 
             if ((xd = getXmlDom (registers[r].getInt ())) != null) {
                 registers[r].setBool (xd.isSelfClosing ());
             } else {
                 registers[r].setBool (false);
             }
             return;
         case 19: //bool isSelfClosingChild (int id, int index) 
             if ((xd = getXmlDom (registers[r].getInt ())) != null) {
                 registers[r].setBool (xd.isSelfClosingChild (registers[r+1].getInt()));
             } else {
                 registers[r].setBool (false);
             }
             return;
 //#ifdef api.domEdition
         case 20: //bool setValue (int id, String value)
             if ((xd = getXmlDom (registers[r].getInt ())) != null) {
                 registers[r].setBool (xd.setValue (registers[r+1].getString()));
             } else {
                 registers[r].setBool (false);
             }
             return;
         case 21: //bool setChildValue (int id, int index, String value)
             if ((xd = getXmlDom (registers[r].getInt ())) != null) {
                 registers[r].setBool (xd.setChildValue (registers[r+1].getInt(), registers[r+2].getString()));
             } else {
                 registers[r].setBool (false);
             }
             return;
         case 22: //bool setAttributeValue (int id, String name, String value)
             if ((xd = getXmlDom (registers[r].getInt ())) != null) {
                 registers[r].setBool (xd.setAttributeValue (registers[r+1].getString(), registers[r+2].getString()));
             } else {
                 registers[r].setBool (false);
             }
             return;
         case 23: //String serialize (int id)
             if ((xd = getXmlDom (registers[r].getInt ())) != null) {
                 registers[r].setString (xd.serialize ());
             } else {
                 registers[r].setString ("");
             }
             return;
         case 24: //bool save (int id, String record)
             if ((xd = getXmlDom (registers[r].getInt ())) != null) {
                 registers[r].setBool (xd.serializeToRms (registers[r+1].getString ()));
             } else {
                 registers[r].setBool (false);
             }
             return;
 //#endif
         default:
             System.err.println ("doXML (m:"+m+") Static call: Invalid method");
             return;
         }
     }
     
     // Used by String.encodeUrl ()
     private static void encodeByte (int v, StringBuffer sb) {
         sb.append('%');
         if (v <= 0xF) sb.append('0');
         sb.append(Integer.toHexString (v));
     }
     
     static void doString (Context c, int m, Register [] registers, int r, int nbParams) {
         String s = registers[r].getString();
         int i, j;
         StringBuffer sb;
         switch (m) {
         case 0: //length (String s)
             registers[r].setInt (s.length());
             return;
         case 1: //charAt (String s, int idx)
             i = registers[r+1].getInt();
             try { registers[r].setInt (s.charAt(i)); } catch (Exception e) { registers[r].setInt (0); }
             return;
         case 2: //substring (String s, int idx, int len)
             i = registers[r+1].getInt();
             j = registers[r+2].getInt();
             try {
                 if (j > -1) {
                     registers[r].setString (s.substring (i, j));
                 } else {
                     registers[r].setString (s.substring(i));
                 }
             } catch (Exception e) { registers[r].setString (""); }
             return;
         case 3: //startsWith (String src, String car)
             registers[r].setBool (s.startsWith(registers[r+1].getString()));
             return;
         case 4: //endsWith (String s)
             registers[r].setBool (s.endsWith(registers[r+1].getString()));
             return;
         case 5: //indexOf (String s, int char, int index)
             i = registers[r+1].getString().charAt (0);
             j = registers[r+2].getInt();
             registers[r].setInt (s.indexOf((char) i, j));
             return;
         case 6: //lastIndexOf (String s, int char, int index)
             i = registers[r+1].getString().charAt (0);
             j = (nbParams == 3) ? registers[r+2].getInt() : s.length();
             registers[r].setInt (s.lastIndexOf((char) i, j));
             return;
         case 7: //toLower (String s)
             registers[r].setString (s.toLowerCase ());
             return;
         case 8: //toUpper (String s)
             registers[r].setString (s.toUpperCase ());
             return;
         case 9: //strIndexOf (String s1, String s2, int index)
             String s2 = registers[r+1].getString();
             j = registers[r+2].getInt();
             registers[r].setInt (s.indexOf(s2, j));
             return;
         case 10: //toInt (String s)
             try {
                 registers[r].setInt((int)Double.parseDouble(s));
             } catch (NumberFormatException e) {
                 registers[r].setInt(-1);
             }
             return;
         case 11: //toFloat (String s)
             try {
                 registers[r].setFloat ((int)(Double.parseDouble(s)*65536));
             } catch (NumberFormatException e) {
                 registers[r].setInt(-1);
             }
             return;
         case 12: //trim (String s)
             registers[r].setString (s.trim());
             return;
         case 13: // width(String, Fontstyle)
             Node node = registers[r+1].getNode();
             if ( (node != null) && (node instanceof FontStyle) ) {
                 ExternFont font = ((FontStyle)node).getExternFont (c);
                 registers[r].setInt (font != null ? font.stringWidth(s) : 0);
             }
             return;
         case 14: // height(Fontstyle)
             node = registers[r].getNode();
             if (node instanceof FontStyle) {
                 ExternFont font = ((FontStyle)node).getExternFont (c);
                 registers[r].setInt (font != null ? font.getHeight() : 0);
             }
             return;
         case 17: //decodeUrl
         case 15: //URLdecode
             try {
                 if (s.length() != 0) s = new String (s.getBytes(), "UTF-8"); // getBytes() on empty string throws ArrayOutOfBoundsException on Samsung F480
                 StringBuffer out = new StringBuffer(s.length());
                 int a = 0;
                 int b = 0;
                 while (a < s.length()){
                     char ch = s.charAt(a);
                     a++;
                     if (ch == '+') {
                         ch = ' ';
                     } else if (ch == '%'){
                         ch = (char)Integer.parseInt(s.substring(a,a+2), 16);
                         a+=2;
                     }
                     out.append(ch);
                     b++;
                 }
                 registers[r].setString (new String(out));
             } catch (Exception e) { e.printStackTrace(); }
             return;
         case 16: // makeAscii (int)
             registers[r].setString (String.valueOf((char)registers[r].getInt()));
             return;
         case 18: //encodeUrl (string)
             sb = new StringBuffer(s.length() * 3);
             final String unreservedChars = "!()*'-._~"; // see RFC2396 section 2.3 Unreserved Characters
             char v;
             for (i = 0; i < s.length(); i++) {
                 v = s.charAt(i);
                 if (v == ' ') {
                     sb.append ('+');
                 } else if ( (v >= 'a' && v <= 'z') || (v >= 'A' && v <= 'Z') || (v >= '0' && v <= '9') || unreservedChars.indexOf(v) != -1) {
                     sb.append (v);
                 } else { // see http://www.w3.org/International/O-URL-code.html
                     if (v <= 0x007f) { // other ASCII
                         encodeByte (v, sb);
                     } else if (v <= 0x07FF) { // non-ASCII <= 0x7FF
                         encodeByte (0xc0 | (v >> 6), sb);
                         encodeByte (0x80 | (v & 0x3F), sb);
                     } else { // 0x7FF < v <= 0xFFFF
                         encodeByte (0xe0 | (v >> 12), sb);
                         encodeByte (0x80 | ((v >> 6) & 0x3F), sb);
                         encodeByte (0x80 | (v & 0x3F), sb);
                     }
                 }
             }
             registers[r].setString (sb.toString ());
             return;
 //#ifdef MM.base64
         case 19: // encodeBase64
             registers[r].setString(JSBase64Coder.encode(s.getBytes()));
             return;
         case 20: // decodeBase64
             try {
               registers[r].setString(JSBase64Coder.decode(s));
             }
             catch (Exception e) {
               registers[r].setString("");
             }
             return;
 //#endif
         case 21: //int split (string orig, MFString dest, char sep)
             s2 = registers[r+2].getString();
             char sep = nbParams == 2 || s2.length() < 1 ? ',' : s2.charAt (0);
             if (registers[r+1].getField() instanceof MFString) {
                 MFString f = (MFString)registers[r+1].getField();
                 int cnt = 0, p = 0, l = s.length();
                 i = s.indexOf (sep);
                 if (i != -1) {
                     if (i == 0) { // sep is first caracter of string
                         f.setValue(cnt++, "");
                         p = 1;
                     }
                     while ((p < l) && (i = s.indexOf (sep, p)) != -1) {
                         f.setValue(cnt++,s.substring(p, i));
                         p = i + 1;
                     }
                     if (p < l) { // rest of string
                         f.setValue(cnt++, s.substring(p));
                     } else if (p == l) { // sep is last caracter of string
                         f.setValue(cnt++, "");
                     }
                     f.resize(cnt);
                 } else {
                     f.resize(1);
                     f.setValue(cnt++, s);
                 }
                 registers[r].setInt (cnt);
             } else {
                 registers[r].setInt (0);
             }
             return;
         case 22: //String join (MFString orig, char sep, int max)
             s = registers[r+1].getString();
             sep = nbParams == 1 || s.length() < 1 ? ',' : s.charAt (0);
             if (registers[r].getField() instanceof MFString) {
                 MFString f = (MFString)registers[r].getField();
                 int max = registers[r+2].getInt();
                 max = nbParams == 3  && max < f.m_size ? max : f.m_size;
                 sb = new StringBuffer (f.getValue (0));
                 for (i=1; i<max; i++) {
                     sb.append (sep);
                     sb.append (f.getValue(i));
                 }
                 registers[r].setString (sb.toString());
             } else {
                 registers[r].setString ("");
             }
             return;
         case 23: //String replace (String s, String tok (s2), String rep)
             s2 = registers[r+1].getString ();
             if (s.indexOf (s2) != -1) { // at least one match...
                 String rep = registers[r+2].getString ();
                 int[] occ = new int[30];
                 int cnt = 0; j = 0; 
                 while ((i = s.indexOf (s2, j)) != -1) { // find all occurences
                     occ[cnt++] = i;
                     j = i + 1;
                     if (cnt == occ.length) { // realloc ooc array
                         int[] nocc = new int[occ.length * 2];
                         System.arraycopy (occ, 0, nocc, 0, occ.length);
                         occ = nocc;
                     }
                 }
                 char[] sc = s.toCharArray (); s = null; // convert s to char array
                 int tokl = s2.length ();
                 sb = new StringBuffer (sc.length + cnt * (rep.length() - tokl));
                 for (i = 0, j = 0; i < cnt; i++) { // replace occurences
                     sb.append (sc, j, occ[i] - j);
                     sb.append (rep);
                     j = occ[i] + tokl;
                 }
                 if (sc.length-j > 0) {
                     sb.append (sc, j, sc.length-j);
                 }
                 registers[r].setString (sb.toString ());
             } else {
                 registers[r].setString (s);
             }
             return;
         default:
             System.err.println ("doString (m:"+m+")Static call: Invalid method");
             return;
         }
     }
     
     private static void appendZeroedInt (StringBuffer sb, int i) {
         if (i < 10) {
             sb.append('0');
         }
         sb.append(i);
     }
     
     private static boolean isLeapYear (int y) {
         return (y % 4 == 0 && y % 100 != 0) || (y % 400 == 0);
     }
     
     private static int dayOfYear (int d, int m, int y) {
         int days[] = { 0, 31, 59, 90, 120, 151, 181, 212, 243, 273, 304, 334 };
         try {
             return d + days[m] + (isLeapYear (y) && m>2 ? 1 : 0); // Add one day on leap years after feb.
         } catch (ArrayIndexOutOfBoundsException e) {
             return -1; // bad month number
         }
     }
 
     static void doDate (Context c, int m, Register [] registers, int r, int nbParams) {
         switch (m) {
         case 6: //time() for backward compatibility
         case 8: //getTime (year, month, day, hour, min, sec, timezone)
             if (nbParams == 0) {
                 registers[r].setInt ((int)(System.currentTimeMillis () / 1000));
                 return;
             }
             try {
                 Calendar cal = Calendar.getInstance ();
                 cal.set (Calendar.YEAR,         nbParams > 0 ? registers[r+0].getInt () : 1970);
                 cal.set (Calendar.MONTH,        nbParams > 1 ? registers[r+1].getInt ()-1 : 0);
                 cal.set (Calendar.DAY_OF_MONTH, nbParams > 2 ? registers[r+2].getInt () : 1);
                 cal.set (Calendar.HOUR_OF_DAY,  nbParams > 3 ? registers[r+3].getInt () : 0);
                 cal.set (Calendar.MINUTE,       nbParams > 4 ? registers[r+4].getInt () : 0);
                 cal.set (Calendar.SECOND,       nbParams > 5 ? registers[r+5].getInt () : 0);
                 cal.set (Calendar.MILLISECOND, 0);
                 if (nbParams > 6) {
                     cal.setTimeZone (TimeZone.getTimeZone (registers[r+6].getString ()));
                 }
                 registers[r].setInt ((int)(cal.getTime().getTime() / 1000));
             } catch (Exception e) { // IllegalArgumentException on wrong/negative numbers
                 registers[r].setInt (0);
             }
             return;
         case 10: // MINUTE
             registers[r].setInt (60);
             return;
         case 11: // HOUR
             registers[r].setInt (3600);
             return;
         case 12: // DAY
             registers[r].setInt (24*3600);
             return;
         case 13: // WEEK
             registers[r].setInt (7*24*3600);
             return;
         case 14: // parseDate (string, timezone) - ISO-8601 extented format 
             // YYYY-MM-DDTHH:MM:SSZ
             // YYYY-MM-DDTHH:MM:SS∓XX:XX
             // 0    5    0    5    0    5
             try {
                 String s = registers[r].getString ();
                 Calendar cal = Calendar.getInstance ();
                 cal.set (Calendar.YEAR, Integer.parseInt (s.substring (0, 4)));
                 cal.set (Calendar.MONTH, Integer.parseInt (s.substring (5, 7)) - 1);
                 cal.set (Calendar.DAY_OF_MONTH, Integer.parseInt (s.substring (8, 10)));
                 cal.set (Calendar.HOUR_OF_DAY, Integer.parseInt (s.substring (11, 13)));
                 cal.set (Calendar.MINUTE, Integer.parseInt (s.substring (14, 16)));
                 cal.set (Calendar.SECOND, Integer.parseInt (s.substring (17, 19)));
                 cal.set (Calendar.MILLISECOND, 0);
                 int time = 0; // in seconds
                 int offsetToGMT = cal.getTimeZone().getRawOffset () / 1000;
                 if (s.length() > 19) { // timezone defined
                     cal.setTimeZone (TimeZone.getTimeZone ("GMT")); // offset == 0
                     time = (int) (cal.getTime().getTime() / 1000l);
                     if (s.charAt(19) != 'Z') { // +HH:MM or -HH:MM
                         int dir = s.charAt (19) == '+' ? -1 : 1;
                         int hour = Integer.parseInt (s.substring (20, 22));
                         int min = Integer.parseInt (s.substring (23, 25));
                         time += dir * (hour * 3600 + min * 60);
                     }
                     time += offsetToGMT;
                 } else { // using local time
                     time = (int) (cal.getTime().getTime() / 1000l);
                 }
                 if (nbParams > 1) { // convert time to given timezone
                     int gmtTime = time - offsetToGMT;
                     String timezone = registers[r+1].getString();
                     int offsetToTimezone = TimeZone.getTimeZone(timezone).getRawOffset() / 1000;
                     if (offsetToTimezone == 0 && !timezone.equalsIgnoreCase("GMT")) {
                         Logger.println("parseDate: Could not convert to unsupported timezone: "+timezone);
                     }
                     time = gmtTime + offsetToTimezone;
                 }
                 registers[r].setInt(time);
             } catch (Exception e) { // NumberFormat or ArrayOutOfBound exceptions
                 registers[r].setInt(0);
             }
             return;
         }
         // Other methods can use a timestamp and a timezone
         Calendar cal = Calendar.getInstance ();
         if (nbParams > 0) {
             cal.setTime (new Date (registers[r].getInt () * 1000l));
             if (nbParams == 2) {
                 cal.setTimeZone (TimeZone.getTimeZone (registers[r+1].getString ()));
             }
         }
         switch (m) {
         case 0: //getYear
             registers[r].setInt (cal.get (Calendar.YEAR));
             return;
         case 1: //getMonth
             registers[r].setInt (cal.get (Calendar.MONTH) + 1);
             return;
         case 2: //getDay
             registers[r].setInt (cal.get (Calendar.DATE));
             return;
         case 3: //getHour
             registers[r].setInt (cal.get (Calendar.HOUR_OF_DAY));
             return;
         case 4: //getMinute
             registers[r].setInt (cal.get (Calendar.MINUTE));
             return;
         case 5: //getsecond
             registers[r].setInt (cal.get (Calendar.SECOND));
             return;
         case 7: //getDayOfWeek
         {
             int d = cal.get (Calendar.DAY_OF_WEEK);
             registers[r].setInt (d == 1 ? 7 : d-1); // convert to ISO-8601 (monday=1)
             return;
         }
         case 9: //getWeek
         {
             // Convertion from date to ISO-8601 Week Numbers
             // http://personal.ecu.edu/mccartyr/ISOwdALG.txt
             
             // Get Y M D for date
             int Y = cal.get (Calendar.YEAR);
             int M = cal.get (Calendar.MONTH);
             int D = cal.get (Calendar.DAY_OF_MONTH);
             int dayOfYearNumber = dayOfYear (D,M, Y);
             // Find the jan1Weekday for Y (Monday=1, Sunday=7)
             int YY = (Y-1) % 100;
             int C = (Y-1) - YY;
             int G = YY + YY/4;
             int jan1Weekday = 1 + (((((C / 100) % 4) * 5) + G) % 7);
             // If Y M D falls in YearNumber Y-1, WeekNumber 52 or 53
             if (dayOfYearNumber <= (8-jan1Weekday) && jan1Weekday > 4) {
                 if (jan1Weekday == 5 || (jan1Weekday == 6 && isLeapYear (Y-1))) {
                     registers[r].setInt (53); // CASE 1
                 } else {
                     registers[r].setInt (52); // CASE 2
                 }
                 return;
             }
             // Find the weekDay for Y M D
             int weekDay = 1 + ((dayOfYearNumber + (jan1Weekday - 1) - 1) % 7);
             // If Y M D falls in YearNumber Y+1, WeekNumber 1
             if (((isLeapYear (Y) ? 366 : 365) - dayOfYearNumber) < (4 - weekDay)) {
                 registers[r].setInt (1); // CASE 3
                 return;
             }
             // Y M D falls in YearNumber Y, WeekNumber 1 through 53
             int weekNumber = (dayOfYearNumber + (7 - weekDay) + (jan1Weekday - 1)) / 7;
             if (jan1Weekday > 4) {
                   weekNumber -= 1;
             }
             registers[r].setInt (weekNumber); // CASE 4
             return;
         }
         case 15: //toString (time, [timezone]) - ISO-8601 extented format
             {
             StringBuffer sb = new StringBuffer ();
             sb.append (cal.get (Calendar.YEAR));
             sb.append ('-');
             appendZeroedInt (sb, cal.get (Calendar.MONTH) + 1);
             sb.append ('-');
             appendZeroedInt (sb, cal.get (Calendar.DAY_OF_MONTH));
             sb.append ('T');
             appendZeroedInt (sb, cal.get (Calendar.HOUR_OF_DAY));
             sb.append (':');
             appendZeroedInt (sb, cal.get (Calendar.MINUTE));
             sb.append (':');
             appendZeroedInt (sb, cal.get (Calendar.SECOND));
             // Timezone
             if (cal.getTimeZone().getRawOffset() == 0) {
                 sb.append('Z');
             } else {
                 int offset = cal.getTimeZone().getRawOffset() / 1000;
                 sb.append (offset > 0 ? '+':'-');
                 appendZeroedInt (sb, offset / 3600);
                 sb.append (':');
                 appendZeroedInt (sb, (offset % 3600) / 60);
             }
             registers[r].setString (sb.toString ());
             return;
             }
         }
     }
 
     static void doFile (Context c, int m, Register [] registers, int r, int nbParams) {
         int i = registers[r].getInt ();
         switch (m) {
 //#ifdef jsr.75
         case 0: // list
             registers[r].setInt (JSFile.list (registers[r].getString ()));
             return;
         case 1: //getName
             registers[r].setString (JSFile.getName (i));
             return;
         case 2: //isDir
             registers[r].setBool (JSFile.isDir (i));
             return;
         case 3: //getFullPath
             registers[r].setString (JSFile.getFullPath (i));
             return;
 //#endif
         case 4: //open
             //if (nbParams == 2) {
                 registers[r].setInt (JSFile.open (c, registers[r].getString(), registers[r+1].getInt()));
                 //} else if (nbParams == 3) {
                 //registers[r].setInt (JSFile.open (c, registers[r].getString(), registers[r+1].getBool(), registers[r+2].getString()));
                 //}
             return;
         case 5: //close
             JSFile.close (c, i);
             return;
         case 6: //getLine
             registers[r].setString (JSFile.getLine (i));
             return;
         case 7: //eof
             registers[r].setBool (JSFile.eof (i));
             return;
         case 8: //getData
             registers[r].setString (JSFile.getData (i, registers[r+1].getBool(), nbParams==3 ? registers[r+2].getString() : "UTF-8"));
             return;
         case 9: //getStatus
             registers[r].setInt (JSFile.getStatus (i));
             return;
         case 10: // isAvailable
 //#ifdef jsr.75
             registers[r].setBool(true);
 //#else
             registers[r].setBool(false);
 //#endif
             return;
         case 11: //ERROR
             registers[r].setInt(Loadable.ERROR);
             return;
         case 12: //READY
             registers[r].setInt(Loadable.READY);
             return;
         case 13: //OPENING
             registers[r].setInt(Loadable.OPENING);
             return;
         case 14: //LOADING
             registers[r].setInt(Loadable.LOADING);
             return;
         case 15: //LOADED
             registers[r].setInt(Loadable.LOADED);
             return;
         case 16: //CLOSED
             registers[r].setInt(Loadable.CLOSED);
             return;
         case 17: //STOPPED
             registers[r].setInt(Loadable.STOPPED);
             return;
         case 18: //clean
             JSFile.clean(c);
             return;
         case 19: //getLength
             registers[r].setInt(JSFile.getLength(i));
             return;
         case 20: //write (int id, String data, boolean async=false, String encoding=UTF-8)
             registers[r].setBool(JSFile.writeData(i, registers[r+1].getString(), 
                     nbParams>2  ? registers[r+2].getBool() : false,
                     nbParams==4 ? registers[r+3].getString() : "UTF-8"));
             return;
         case 21: //WRITE
             registers[r].setInt(File.MODE_WRITE);
             return;
         case 22: //SYNC
             registers[r].setInt(File.MODE_SYNC);
             return;
         case 23: //ASYNC
             registers[r].setInt(File.MODE_ASYNC);
             return;
         default:
             System.err.println ("doFile (m:"+m+")Static call: Invalid method");
             return;
         }
     }
 
     static void doContact  (Context c, int m, Register [] registers, int r, int nbParams) {
         switch (m) {
         case 0: // isAvailable()
 //#ifdef api.pim
             registers[r].setBool(true);
 //#else
             registers[r].setBool(false);
 //#endif
             return;
         case 1: // int open ()
             registers[r].setInt (JSContact.openContacts()); return;
         case 2: // void close ()
             JSContact.closeContacts(); return;
         case 3: // String getInfo (int id, int property)
             registers[r].setString (JSContact.getContactInfo (registers[r].getInt(), registers[r+1].getInt())); return; 
         case 4: // FULL_NAME
             registers[r].setInt (JSContact.PIM_FULL_NAME); return;
         case 5: // FIRST_NAME
             registers[r].setInt (JSContact.PIM_FIRST_NAME); return;
         case 6: // LAST_NAME
             registers[r].setInt (JSContact.PIM_LAST_NAME); return;
         case 7: //ADDRESS
             registers[r].setInt (JSContact.PIM_ADDRESS); return;
         case 8: //MOBILE
             registers[r].setInt (JSContact.PIM_MOBILE); return;
         case 9: //MOBILE_HOME
             registers[r].setInt (JSContact.PIM_MOBILE_HOME); return;
         case 10: //MOBILE_WORK
             registers[r].setInt (JSContact.PIM_MOBILE_WORK); return;
         case 11: //HOME
             registers[r].setInt (JSContact.PIM_HOME); return;
         case 12: //WORK
             registers[r].setInt (JSContact.PIM_WORK); return;
         case 13: //FAX
             registers[r].setInt (JSContact.PIM_FAX); return;
         case 14: //OTHER
             registers[r].setInt (JSContact.PIM_OTHER); return;
         case 15: //PREFERRED
             registers[r].setInt (JSContact.PIM_PREFERRED); return;
         case 16: //EMAIL
             registers[r].setInt (JSContact.PIM_EMAIL); return;       
         case 17: //PHOTO_URL
             registers[r].setInt (JSContact.PIM_PHOTO_URL); return;    
         case 18: // int createContact()
             registers[r].setInt (JSContact.createContact()); return;
         case 19: // int deleteContact(int contactId)
             registers[r].setInt (JSContact.deleteContact (registers[r].getInt())); return;     
         case 20: // void setInfo(int contactId, int info_constant, String info_string)
             JSContact.setInfo (registers[r].getInt(), registers[r+1].getInt(), registers[r+2].getString()); return;
         case 21: // void addInfo(int contactId, int info_constant, String info_string)
             JSContact.addInfo (registers[r].getInt(), registers[r+1].getInt(), registers[r+2].getString()); return;           
         default:
             System.err.println ("doContact (m:"+m+")Static call: Invalid method");
             return;
         }
     }
 
     static void doMessaging (Context c, int m, Register [] registers, int r, int nbParams) {
         boolean success=false;
         switch (m) {
         case 0: // isAvailable()
 //#ifndef jsr.wma
             registers[r].setBool(false);
 //#else
             registers[r].setBool(true);
             return;
         case 1: // sendSMS(phoneNumber, msgToSend)
             success = MessagingHelper.sendSMS(registers[r].getString(), registers[r+1].getString());
             registers[r].setBool(success);
             return;
         case 2: // send MMS(phoneNumber, subject, msgToSend, imagePath)
 //#ifdef jsr.wma2
             success = MessagingHelper.sendMMS(registers[r].getString(), registers[r+1].getString(), registers[r+2].getString(), registers[r+3].getString(), c.decoder);
             registers[r].setBool(success);
 //#else
             registers[r].setBool(false);
 //#endif
             return;
         case 3: // startListeningSMS(port,messageName)
             if (nbParams==1) {
                 success = MessagingHelper.startListenSMS(registers[r].getInt(),null);
             } else if (nbParams==2) {
                 success = MessagingHelper.startListenSMS(registers[r].getInt(),registers[r+1].getString());
             }
             registers[r].setBool(success);
             return;
         case 4: // stopListeningSMS(port)
             success = MessagingHelper.stopListenSMS(registers[r].getInt());
             registers[r].setBool(success);
             return;
 //#ifdef MM.pushSMS
         case 5: // registerPushSMS(port)
             success = MessagingHelper.registerConnection(registers[r].getInt());
             registers[r].setBool(success);
             return;
         case 6: // unregisterPushSMS(port)
             success = MessagingHelper.unregisterConnection(registers[r].getInt());
             registers[r].setBool(success);
             return;
         case 7: // isRegisteredPushSMS(port)
         	registers[r].setInt(MessagingHelper.isRegisteredConnection(registers[r].getInt()));
             return;
 //#endif
         case 8: // isPushSMSAvailable()
 //#ifdef MM.pushSMS
             registers[r].setBool(true);
 //#else
             registers[r].setBool(false);
 //#endif
             return;
 //#endif
         default:
             System.err.println ("doMessaging (m:"+m+")Static call: Invalid method");
             return;
 
         }
     }
 
 //#ifdef api.im
     static void doIM (Context c, int m, Register [] registers, int r, int nbParams) {
         String email, pwd, clientID, contact, msg, alias;
         int status;
         boolean accept,result;
         switch (m) {
         case 0: // login(email, password, clientID, status)
             email = registers[r].getString();
             pwd = registers[r+1].getString();
             clientID = registers[r+2].getString();
             status = registers[r+3].getInt();
             result = imHelper.login(email,pwd,clientID,status,imObserver);
             // set return value
             registers[r].setBool(result);
             return;
         case 1: // logout()
             result = imHelper.logout();
             // set return value
             registers[r].setBool(result);
             return;
         case 2: // updateAlias(alias)
             alias = registers[r].getString();
             result = imHelper.updateAlias(alias);
             // set return value
             registers[r].setBool(result);
             return;
         case 3: // updatePresence(status)
             status = registers[r].getInt();
             result = imHelper.updatePresence(status);
             // set return value
             registers[r].setBool(result);
             return;
         case 4: // sendMessage(contact, msg)
             contact = registers[r].getString();
             msg = registers[r+1].getString();
             result = imHelper.sendMessage(contact, msg);
             // set return value
             registers[r].setBool(result);
             return;
         case 5: // blockContact(contact)
             contact = registers[r].getString();
             result = imHelper.blockContact(contact);
             // set return value
             registers[r].setBool(result);
             return;
         case 6: // unblockContact(contact)
             contact = registers[r].getString();
             result = imHelper.unblockContact(contact);
             // set return value
             registers[r].setBool(result);
             return;
         case 7: // addContact(contact)
             contact = registers[r].getString();
             result = imHelper.addContact(contact);
             // set return value
             registers[r].setBool(result);
             return;
         case 8: // removeContact(contact)
             contact = registers[r].getString();
             result = imHelper.removeContact(contact);
             // set return value
             registers[r].setBool(result);
             return;
         case 9: // respondInvitation(contact, accept)
             contact = registers[r].getString();
             accept = registers[r+1].getBool();
             result = imHelper.respondInvitation(contact, accept);
             // set return value
             registers[r].setBool(result);
             return;
         default:
             System.err.println ("doIM (m:"+m+")Static call: Invalid method");
             return;
 
         }
     }
 //#endif
 
     static void doLocation (Context c, int m, Register [] registers, int r, int nbParams) {
         switch (m) {
 
         case 0: // isAvailable()
 //#ifndef api.location
             registers[r].setBool(false);
 //#else
             registers[r].setBool(true);
             return;
         case 1: // getDistance(latSrc, longSrc, latDest, longDest)
         {
             int result = JSLocation.getDistance(registers[r].getInt(), registers[r+1].getInt(), registers[r+2].getInt(), registers[r+3].getInt());
             registers[r].setInt(result);
             return;
         }
 //#endif
         default:
             System.err.println ("doLocation (m:"+m+")Static call: Invalid method");
             return;
 
         }
     }
 
 //#ifdef api.pushlet
     static void doPushlet (Context c, int m, Register [] registers, int r, int nbParams) {
         switch (m) {
         case 0: // join()
             pushlet.join();
             return;
         case 1: // leave()
             pushlet.leave();
             return;
         case 2: // listen(subject)
             pushlet.listen(registers[r].getString());
             return;
         case 3: // stopListen()
             pushlet.stopListen();
             return;
         case 4: // publish(subject,key,value)
             pushlet.publish(registers[r].getString(),registers[r+1].getString(),registers[r+2].getString());
             return;
         default:
             System.err.println ("doPushlet (m:"+m+") Static call: Invalid method");
             return;
         }
     }
 //#endif
 
 //#ifdef api.socket
     static int getFreeSocketSlot () {
         if (s_socket == null) {
             s_socket = new Socket [MAX_SOCKETS];
             for (int i = 0; i < MAX_SOCKETS; i++) {
                 s_socket[i] = null;
             }
             return 0;
         }
         return getFreeSlot (s_socket);
     }
     
     static boolean socketExist (int i) {
         return (i > -1) && (i < MAX_SOCKETS) && (s_socket[i] != null);
     }
     
     static void doSocket (Context c, int m, Register [] registers, int r, int nbParams) {
         int id = registers[r].getInt();
         switch (m) {
         case 0: // newSocket
             id = getFreeSocketSlot();
             if (id > -1) s_socket[id] = new Socket(registers[r].getString(),registers[r+1].getInt());
             else Logger.println("ERROR: Max sockets ("+MAX_SOCKETS+") reached !");
             registers[r].setInt(id);
             return;
         case 1: // free
             if (socketExist(id)) s_socket[id] = null;
             else Logger.println("ERROR: socket ("+id+") does not exist !");
             return;
         case 2: // connect
             if (socketExist(id)) s_socket[id].connect();
             else Logger.println("ERROR: socket ("+id+") does not exist !");
             return;
         case 3: // disconnect
             if (socketExist(id)) s_socket[id].disconnect(nbParams == 2 ? registers[r+1].getBool() : false);
             else Logger.println("ERROR: socket ("+id+") does not exist !");
             return;
         case 4: // isConnected
             if (socketExist(id)) registers[r].setBool(s_socket[id].isConnected());
             else {
                 Logger.println("ERROR: socket ("+id+") does not exist !");
                 registers[r].setBool(false);
             }
             return;
         case 5: // send
             if (socketExist(id)) s_socket[id].send(registers[r+1].getString(), nbParams == 3 ? registers[r+2].getBool() : false);
             else Logger.println("ERROR: socket ("+id+") does not exist !");
             return;
         case 6: // sendFile
             if (socketExist(id)) s_socket[id].sendFile(registers[r+1].getInt(), nbParams == 3 ? registers[r+2].getBool() : false);
             else Logger.println("ERROR: socket ("+id+") does not exist !");
             return;
         case 7: // sendToBuffer
             if (socketExist(id)) s_socket[id].sendToBuffer(registers[r+1].getString(), nbParams == 3 ? registers[r+2].getBool() : false);
             else Logger.println("ERROR: socket ("+id+") does not exist !");
             return;
         case 8: // sendFileToBuffer
             if (socketExist(id)) s_socket[id].sendFileToBuffer(registers[r+1].getInt(), nbParams == 3 ? registers[r+2].getBool() : false);
             else Logger.println("ERROR: socket ("+id+") does not exist !");
             return;
         case 9: // sendBuffer
             if (socketExist(id)) s_socket[id].sendBuffer(nbParams == 2 ? registers[r+1].getBool() : false);
             else Logger.println("ERROR: socket ("+id+") does not exist !");
             return;
         default:
             System.err.println ("doSocket (m:"+m+") Static call: Invalid method");
             return;
         }
     }
 //#endif
     
     static void doAd (Context c, int m, Register [] registers, int r, int nbParams) {
         switch (m) {
         case 0: // isAvailable()
 //#ifndef api.ad
             registers[r].setBool(false);
 //#else
             registers[r].setBool(true);
             return;
         case 1: // clickTo(email)
             String adLinkURL = AdHelper.getAdClient().clickToUrl(AdHelper.AD_CLICK_URL, registers[r].getString());            
             if (adLinkURL != null) MiniPlayer.openUrl(adLinkURL);
             return;
 //#endif
         default:
             System.err.println ("doAd (m:"+m+")Static call: Invalid method");
             return;
 
         }
     }
     
     static void doHttp (Context c, int m, Register [] registers, int r, int nbParams) {
         switch (m) {
         case 0: // get (url, callback, [encoding]) => callback (responseCode, data)
         case 1: // post (url, data, callback, [encoding]) => callback (responseCode, data)
             final Script script = c.script;
             final Context context = c;
             final String url = registers[r].getString();
             final String data = registers[r+1].getString();
             final int cb = registers[r+m+1].getInt();
             final String encoding = nbParams==m+3 ? registers[r+m+2].getString() : "UTF-8";
             final int mode = m == 1 ? File.MODE_WRITE : File.MODE_READ;
             script.releaseMachineOnInit = false; // prevent release of Machine on Script start
             new Thread () {
                 public void run () {
                     File f = new File (url, mode);
                     context.addLoadable (f);
                     if (mode == File.MODE_WRITE) f.startWriteAll(data, false, encoding);
                     String rdata = f.startReadAll (false, encoding);
                     int response = f.getHttpResponseCode();
                     context.removeLoadable (f);
                     Register[] params = new Register[] { new Register(), new Register(), new Register() };
                     params[0].setString(url);
                     params[1].setInt(response);
                     params[2].setString(rdata);
                     script.addCallback(cb, params);
                     MiniPlayer.wakeUpCanvas();
                 }
             }.start();
             return;
         default:
             System.err.println ("doHttp (m:"+m+")Static call: Invalid method");
             return;
         }
     }
 
     static void doStyle (Context c, int m, Register [] registers, int r, int nbParams) {
 //#ifdef api.richText
         Field f = registers[r].getField();
         String tagName = registers[r+1].getString();
         CSSProp p = null;
         //Logger.println ("---- doStyle.getProperty value for "+tagName);
         if (m != 6) { // getFontStyle is a speacial case
             if (f != null && f instanceof MFString) {
                 p = Style.getProperty ((MFString)f, tagName);
             } else {
                 p = Style.getProperty (registers[r].getString(), tagName);
             }
         }
 //         if (p != null) {
 //             Logger.println ("doStyle.getXYZ prop value is "+p.m_val);
 //             p.print ();
 //         }
         switch (m) {
         case 0: // void getColor (String, String String, SFColor);
             f = registers[r+2].getField ();
             if (p != null && p.m_type == CSSProp.TYPE_COLOR && f != null && f instanceof SFColor) {
                 ((SFColor) f).setRgb (p.m_value);
                 registers[r].setInt (1);
             } else {
                 registers[r].setInt (0);
             }
             break;
         case 1: // void getCoord (MSFsting|String tag, String attr, SFvec2f, int lx, int ly);
             f = registers[r+2].getField ();
             if (p != null && f != null && f instanceof SFVec2f) {
                 ((SFVec2f) f).setValue ((Style.getFixFloat  (p, registers[r+3].getInt())),
                                         (Style.getFixFloat2 (p, registers[r+4].getInt())));
                 registers[r].setInt (1);
             } else {
                 registers[r].setInt (0);
             }
             break;
         case 2: // int getInt (MFString | String, String, int l)
             if (p != null) {
                 registers[r].setInt (Style.getInteger (p, registers[r+2].getInt()));
             } else {
                 registers[r].setInt (0);
             }
             break;
         case 3: // int getFloat (String)
             if (p != null) {
                 registers[r].setFloat (Style.getFixFloat (p, registers[r+2].getInt()));
             } else {
                 registers[r].setFloat (0);
             }
             break;
         case 4: // int getString (String)
             if (p != null) {
                 registers[r].setString (p.m_val);
             } else {
                 registers[r].setString ("##");
             }
             break;
         case 5: // int getBool (String)
             if (p != null && p.m_val.equalsIgnoreCase ("true")) {
                 registers[r].setInt (1);
             } else {
                 registers[r].setInt (0);
             }
             break;
         case 6: // int getFontStyle (String)
             Node fontStyle = registers[r+1].getNode ();
             if (fontStyle != null && fontStyle instanceof FontStyle) {
                 if (f != null && f instanceof MFString) {
                     if ( (p = Style.getProperty ((MFString)f, "font-family")) != null) {
                         ((MFString)((FontStyle)fontStyle).m_field[3]).setValue (0, p.m_val);
                     }
                     if ( (p = Style.getProperty ((MFString)f, "font-size")) != null) {
                         ((SFFloat)((FontStyle)fontStyle).m_field[0]).setValue (Style.getFixFloat (p, 1));
                     }
                     if ( (p = Style.getProperty ((MFString)f, "font-style")) != null) {
                         ((SFString)((FontStyle)fontStyle).m_field[1]).setValue (p.m_val);
                     }
                     if ( (p = Style.getProperty ((MFString)f, "horizontal-align")) != null) {
                         ((MFString)((FontStyle)fontStyle).m_field[2]).setValue (0, p.m_val);
                     }
                     if ( (p = Style.getProperty ((MFString)f, "vertical-align")) != null) {
                         ((MFString)((FontStyle)fontStyle).m_field[2]).setValue (1, p.m_val);
                     }
                     if ( (p = Style.getProperty ((MFString)f, "font-filter")) != null) {
                         ((SFBool)((FontStyle)fontStyle).m_field[4]).setValue (p.m_val.equalsIgnoreCase("true"));
                     }
                 } else {
                     String s = registers[r].getString();
                     if ( (p = Style.getProperty (s, "font-family")) != null) {
                         ((MFString)((FontStyle)fontStyle).m_field[3]).setValue (0, p.m_val);
                     }
                     if ( (p = Style.getProperty (s, "font-size")) != null) {
                         ((SFFloat)((FontStyle)fontStyle).m_field[0]).setValue (Style.getFixFloat (p, 1));
                     }
                     if ( (p = Style.getProperty (s, "font-style")) != null) {
                         ((SFString)((FontStyle)fontStyle).m_field[1]).setValue (p.m_val);
                     }
                     if ( (p = Style.getProperty (s, "horizontal-align")) != null) {
                         ((MFString)((FontStyle)fontStyle).m_field[2]).setValue (0, p.m_val);
                     }
                     if ( (p = Style.getProperty (s, "vertical-align")) != null) {
                         ((MFString)((FontStyle)fontStyle).m_field[2]).setValue (1, p.m_val);
                     }
                     if ( (p = Style.getProperty (s, "font-filter")) != null) {
                         ((SFBool)((FontStyle)fontStyle).m_field[4]).setValue (p.m_val.equalsIgnoreCase("true"));
                     }
                 }
                 registers[r].setInt (1);
             } else {
                 Logger.println ("Style.getFontStyle: invalid parameter #2 (should be a FontStyle node)");
                 registers[r].setInt (0);
             }
             break;
         default:
             Logger.println ("doStyle (m:"+m+")Static call: Invalid method");
             return;
         }
 //#else 
         Logger.println ("Style not supported in this version");
 //#endif
     }
     
 //#ifdef jsr.179
 
 	static boolean isJSR179Available() {
 		// check if GPS is enabled
         String gpsEnable = MiniPlayer.getJadProperty ("MEMO-ENABLE_GPS");
         if ( (gpsEnable == null) || (gpsEnable.equalsIgnoreCase ("true")==false) ) {
         	return false;
         }
 
 		// check if JSR 179 is present
     	Class locationClass = null;
     	try {
     		locationClass = Class.forName("javax.microedition.location.Location");
     	} catch (ClassNotFoundException e) {}
 
 		if (locationClass == null) {
 			// JSR 179 not available on this device 
 	        Logger.println ("GPS location class not available");
             return false;
 		}
 
 		Criteria criteria = new Criteria();
     	if( criteria == null ) {
 	        Logger.println ("GPS criteria not available");
 			return false;
 		}
 
 		LocationProvider provider = null;
 		try {
 			provider = javax.microedition.location.LocationProvider.getInstance(criteria);
 		} catch (Exception e) {}
 
 		if (provider == null) {
 			// JSR 179 available but no provider on this device 
 	        Logger.println ("GPS provider not available");
             return false;
 		}
 
 		// JSR179 available
 		return true;
 	}
 	
 	static boolean s_jsr179Available = isJSR179Available();
 	
 	static class MemoLocation implements LocationListener {
 		LocationProvider lp;
 		
 		MemoLocation() {
             try { 
 	            Criteria criteria = new Criteria();
 	            // Get an instance of the provider
 	            lp = LocationProvider.getInstance(criteria);
             } catch (Exception e) {};
 		}
 		
 		public boolean init(int period) {
 			try {
 				lp.setLocationListener(this, period, -1, -1);
 				return true;
 			} catch (Exception e) {};
 			return false;
 		}
 		
 		public void reset() {
 			lp.setLocationListener(null, -1, -1, -1);
 		}
 		
 		public void locationUpdated(LocationProvider provider, Location location) {
             if( location.isValid() ) {
             	// if coordinates are valid, send message
                 Coordinates c = location.getQualifiedCoordinates();
                 MFString mess = new MFString(4);
                 mess.setValue(0, ""+c.getLatitude());
                 mess.setValue(1, ""+c.getLongitude());
                 mess.setValue(2, ""+c.getAltitude());
                 mess.setValue(3, ""+location.getSpeed());
                 mess.setValue(4, ""+(location.getTimestamp()/1000));
             	Message.sendMessage ("GPS_POSITION", mess);
                 MiniPlayer.wakeUpCanvas ();
             }
 		}
 		
 		public void providerStateChanged(LocationProvider provider, int newState) {
 		}
 	}
 	
 	static MemoLocation m_MemoLocation;
 //#endif
 
 	
 	static void doGPS (Context c, int m, Register [] registers, int r, int nbParams) {
 //#ifdef jsr.179
 
         switch (m) {
         case 0: // isAvailable
             // no error starting thread
             registers[r].setBool (s_jsr179Available);
         	break;
         case 1: // start
         	// check if available
 			if(s_jsr179Available==false) {
 	            registers[r].setBool (false);
 	            break;
 			}
 
 			// stop previous thread if running
         	if( m_MemoLocation == null ) {
         		m_MemoLocation = new MemoLocation();
         	}
         	
         	if( m_MemoLocation != null ) {
 	            // no error starting thread
 	            registers[r].setBool (m_MemoLocation.init(registers[r].getInt ()));
         	} else {
 	            registers[r].setBool (false);
         	}
         	break;
         case 2: // stop
         	if( m_MemoLocation != null ) {
         		m_MemoLocation.reset();
         		m_MemoLocation=null;
         	}
         	break;
         default:
             Logger.println ("doGPS (m:"+m+")Static call: Invalid method");
             return;
         }
 //#else 
         Logger.println ("GPS not supported in this version");
         // return always false
         registers[r].setBool (false);
 //#endif
     }
 
     static void doAppWidget (Machine mc, Context c, int m, Register [] registers, int r, int nbParams) {
 //#ifdef platform.android
         MIDlet ml = MiniPlayer.self;
         switch (m) {
         case 0: // bool isAppWidget ()
             registers[r].setBool (Display.getDisplay (ml).isAppWidget ());
             return;
         case 1: // bool displayWidget ()
             registers[r].setBool (Display.getDisplay (ml).displayWidget ());
             return;
         case 2: // bool displayLoader (bool display, string message)
             registers[r].setBool (Display.getDisplay (ml).displayLoader (registers[r].getString()));
             return;
        case 3: // bool displayError (string message)
            registers[r].setBool (Display.getDisplay (ml).displayAlert (registers[r].getString()));
            return;
 
        default:
            Logger.println ("doAppWidget (m:"+m+"): Invalid static method call");
        }
 //#else
        Logger.println ("AppWidget class not supported in this version");
        registers[r].setBool (false);
 //#endif
     }
 
 
     // As J2ME CLDC 1.1 does not have asin, acos, atan, atan2 functions
     // here is a quick implementation
 
     // constants
     static final double sq2p1 = 2.414213562373095048802e0;
     static final double sq2m1 = 0.414213562373095048802e0;
     static final double p4 = 0.161536412982230228262e2;
     static final double p3 = 0.26842548195503973794141e3;
     static final double p2 = 0.11530293515404850115428136e4;
     static final double p1 = 0.178040631643319697105464587e4;
     static final double p0 = 0.89678597403663861959987488e3;
     static final double q4 = 0.5895697050844462222791e2;
     static final double q3 = 0.536265374031215315104235e3;
     static final double q2 = 0.16667838148816337184521798e4;
     static final double q1 = 0.207933497444540981287275926e4;
     static final double q0 = 0.89678597403663861962481162e3;
     static final double PIO2 = 1.5707963267948966135E0;
     static final double nan = (0.0 / 0.0);
 
     // reduce
     private static double mxatan(double arg) {
 
         double argsq = arg * arg;
         double value = ((((p4 * argsq + p3) * argsq + p2) * argsq + p1) * argsq + p0);
         value = value / (((((argsq + q4) * argsq + q3) * argsq + q2) * argsq + q1) * argsq + q0);
         return (value * arg);
     }
 
     // reduce
     private static double msatan(double arg) {
         if (arg < sq2m1) {
             return mxatan(arg);
         }
         if (arg > sq2p1) {
             return (PIO2 - mxatan(1 / arg));
         }
         return ( (PIO2 / 2) + mxatan((arg - 1) / (arg + 1)));
     }
 
     // implementation of atan
     public static double atan(double arg) {
         if (arg > 0) {
             return msatan(arg);
         }
         return (-msatan(-arg));
     }
 
     // implementation of atan2
     public static double atan2(double arg1, double arg2) {
         if (arg1 + arg2 == arg1) {
             if (arg1 >= 0) {
                 return PIO2;
             }
             return -PIO2;
         }
         arg1 = atan(arg1 / arg2);
         if (arg2 < 0) {
             if (arg1 <= 0) {
                 return (arg1 + Math.PI);
             }
             return arg1 - Math.PI;
         }
         return arg1;
     }
 
     // implementation of asin
     public static double asin(double arg) {
         int sign = 0;
         if (arg < 0) {
             arg = -arg;
             sign++;
         }
         if (arg > 1) {
             return nan;
         }
         double temp = Math.sqrt(1 - arg * arg);
         if (arg > 0.7) {
             temp = (PIO2 - atan(temp / arg));
         } else {
             temp = atan(arg / temp);
         }
         if (sign > 0) {
             temp = -temp;
         }
         return temp;
     }
 
     // implementation of acos
     public static double acos(double arg) {
         if (arg > 1 || arg < -1) {
             return nan;
         }
         return (PIO2 - asin(arg));
     }
     
     public static double pow(double base, int exp){
         if(exp == 0) return 1;
         double res = base;
         for(;exp > 1; --exp) {
             res *= base;
         }
         return res;
     }
 
     public static double dlog(double x) {
         long l = Double.doubleToLongBits(x);
         long exp = ((0x7ff0000000000000L & l) >> 52) - 1023;
         double man = (0x000fffffffffffffL & l) / (double)0x10000000000000L + 1.0;
         double lnm = 0.0;
         double a = (man - 1) / (man + 1);
         for( int n = 1; n < 7; n += 2) {
             lnm += pow(a, n) / n;
         }
         return 2 * lnm + exp * 0.69314718055994530941723212145818;
     }
 }
