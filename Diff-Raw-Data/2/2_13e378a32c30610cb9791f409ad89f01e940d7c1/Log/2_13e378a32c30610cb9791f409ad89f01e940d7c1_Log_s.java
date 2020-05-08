 /*
 Copyright 2012 Kevin J. Jones (http://www.kevinjjones.co.uk)
 
 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at
 
 http://www.apache.org/licenses/LICENSE-2.0
 
 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
  */
 package uk.co.kevinjjones;
 
 import au.com.bytecode.opencsv.CSVReader;
 import java.io.*;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import uk.co.kevinjjones.model.*;
 import uk.co.kevinjjones.vehicle.*;
 
 /**
  * Wrapper for a single log file.
  * Responsible for loading the file and also setting up the "streams" that are
  * used to access the data.
  */
 public class Log extends View {
 
     private String _name;
 
     public final static String SESSION_STREAM="SESSION";
     public final static String TIME_STREAM="TIME";
     public final static String RPM_STREAM="RPM";
     public final static String THROT_STREAM="THROT";
     public final static String WATER_STREAM="WATER";
     public final static String AIR_STREAM="AIR";
     public final static String MAP_STREAM="MAP";
     public final static String LAMB_STREAM="LAMB";
     public final static String LUSP_STREAM="L U SP";
     public final static String RUSP_STREAM="R U SP";
     public final static String LDSP_STREAM="L D SP";
     public final static String RDSP_STREAM="R D SP";
     public final static String TURB_STREAM="TURB %";
     public final static String OILT_STREAM="OIL T";
     public final static String VOLTS_STREAM="VOLTS";
     public final static String SLIP_STREAM="SLIP%";
     public final static String ADV_STREAM="ADV";
     public final static String LAUCS_STREAM="LAUC S";
     public final static String SHIFS_STREAM="SHIF S";
     public final static String SHIFA_STREAM="SHIF A";
     public final static String INJ_STREAM="INJ %";
     public final static String A1VAL_STREAM="A1 VAL";
     public final static String A2VAL_STREAM="A2 VAL";
 
     /**
      * Metadata container for stream found in the files.
      */
     static class RawTraceData {
         public String _units;
         public String _desc;
         public String _axis;
         
         public RawTraceData(String units, String desc, String axis ) {
             assert(desc!=null && axis !=null);
             _units=units;
             _desc=desc;
             _axis=axis;
         }
     }
     
     /**
      * Metadata container for virtual streams that augment the log data.
      */
     static class VirtualTraceData {
         public boolean _rebase;
         public Class _streamClass;
         public Object _arg;
         
         public VirtualTraceData(boolean rebase, Class streamClass) {
             assert(streamClass!=null);
             _rebase=rebase;
             _streamClass=streamClass;
         }
         
         public VirtualTraceData(boolean rebase, Class streamClass, Object arg) {
             assert(streamClass!=null);
             _rebase=rebase;
             _streamClass=streamClass;
             _arg=arg;
         }
     }
     
     private final static Map<String,RawTraceData> _rawTraceData=new HashMap();
     private final static ArrayList<VirtualTraceData> _virtualTraceData=new ArrayList();
 
     /**
      * The metadata.
      */
     static {
         _rawTraceData.put(THROT_STREAM, new RawTraceData("%", "Throttle Position", "Throttle"));
         _rawTraceData.put(MAP_STREAM, new RawTraceData(null, "MAP", "Pressure"));
         _rawTraceData.put(RPM_STREAM, new RawTraceData("", "RPM", "RPM"));
         _rawTraceData.put(TURB_STREAM, new RawTraceData("%", "Turbo", "Turbo"));
         _rawTraceData.put(LAMB_STREAM, new RawTraceData("", "AFR", "AFR"));
         _rawTraceData.put(SLIP_STREAM, new RawTraceData("%", "Wheel Slip", "Slip"));
         _rawTraceData.put(WATER_STREAM, new RawTraceData(null, "Water Temp", "Temperature"));
         _rawTraceData.put(OILT_STREAM, new RawTraceData(null, "Oil Temp", "Temperature"));
         _rawTraceData.put(AIR_STREAM, new RawTraceData(null, "Air Temp", "Temperature"));
         _rawTraceData.put(LUSP_STREAM, new RawTraceData(null, "Left Undriven Speed", "Speed"));
         _rawTraceData.put(RUSP_STREAM, new RawTraceData(null, "Right Undriven Speed", "Speed"));
         _rawTraceData.put(LDSP_STREAM, new RawTraceData(null, "Left Driven Speed", "Speed"));
         _rawTraceData.put(RDSP_STREAM, new RawTraceData(null, "Right Driven Speed", "Speed"));
 
         _virtualTraceData.add(new VirtualTraceData(false, LowThrottleStream.class));
         _virtualTraceData.add(new VirtualTraceData(false, WheelStream.class,new Integer(0)));
         _virtualTraceData.add(new VirtualTraceData(false, WheelStream.class,new Integer(1)));
         _virtualTraceData.add(new VirtualTraceData(false, WheelStream.class,new Integer(2)));
         _virtualTraceData.add(new VirtualTraceData(false, WheelStream.class,new Integer(3)));
         _virtualTraceData.add(new VirtualTraceData(false, SpeedStream.class));
         _virtualTraceData.add(new VirtualTraceData(true, DistanceStream.class));
         _virtualTraceData.add(new VirtualTraceData(false, AFRStream.class, new Integer(1)));
         _virtualTraceData.add(new VirtualTraceData(false, AFRStream.class, new Integer(2)));
         _virtualTraceData.add(new VirtualTraceData(false, TempStream.class, new Integer(1)));
         _virtualTraceData.add(new VirtualTraceData(false, TempStream.class, new Integer(2)));
         _virtualTraceData.add(new VirtualTraceData(false, TempStream.class, new Integer(3)));
         _virtualTraceData.add(new VirtualTraceData(false, LongAccelStream.class));
     }
     
     // For rendering descriptions on the UI
     public static String getStreamDescription(String name) {
         RawTraceData rtd=_rawTraceData.get(name);
         if (rtd!=null && rtd._desc!=null)
             return rtd._desc;
         return name;
     }
     
     /**
      * File reader helper
      * @param fis The file to return
      * @return The file contents
      * @throws IOException 
      */
     static byte[] readFile(FileInputStream fis) throws IOException {
         InputStream in = null;
         byte[] out = new byte[0];
 
         try {
             in = new BufferedInputStream(fis);
 
             // the length of a buffer can vary
             int bufLen = 20000 * 1024;
             byte[] buf = new byte[bufLen];
             byte[] tmp;
             int len;
             while ((len = in.read(buf, 0, bufLen)) > 0) {
                 // extend array
                 tmp = new byte[out.length + len];
 
                 // copy data
                 System.arraycopy(out, 0, tmp, 0, out.length);
                 System.arraycopy(buf, 0, tmp, out.length, len);
 
                 out = tmp;
             }
 
         } finally {
             // always close the stream
             if (in != null) {
                 try {
                     in.close();
                 } catch (Exception e) {
                 }
             }
         }
         return out;
     }
 
     /**
      * Construct a new log file wrapper
      * @param f The log file
      * @param handler Handler for asking UI questions about data format
      * @param ok Error/warning handler
      * @throws IOException 
      */
     public Log(File f, ParamHandler handler, WithError<Boolean,BasicError> ok) throws IOException {
 
         // Check its readable
         if (!f.canRead()) {
             ok.addError(new BasicError("The data in the logfile " + 
                     f.getAbsolutePath()
                     + " can not be read, it may be protected against access."));
             ok.setValue(Boolean.FALSE);
             return;
         }
 
         // Now read the data stream
         FileInputStream fis;
         byte[] data;
         try {
             fis = new FileInputStream(f);
             data = readFile(fis);
         } catch (IOException e) {
             ok.addError(new BasicError("A file reading error occured reading the file "
                     + f.getAbsolutePath(),e));
             ok.setValue(Boolean.FALSE);
             return;
         }
         _name = f.getName();
 
         CSVReader parser = new CSVReader(new StringReader(new String(data)), ';');
         
         // First read the headers and create Streams in view
         String[] headers;
         headers = parser.readNext();
         if (headers.length==0) {
             ok.addError(new BasicError("The logfile "+f.getAbsolutePath()+
             " does not appear to have any header(s), it might be empty or not a DTA logfile."));
             ok.setValue(Boolean.FALSE);
             return;
         }
         
         RWStream[] streams=new RWStream[headers.length-1];
         for (int h=0; h<headers.length-1; h++) {
             String name=headers[h].trim();
             String desc=name;
             String axis=name;
             String units="unknown units";
             RawTraceData td=_rawTraceData.get(name);
             if (td!=null) {
                 desc=td._desc;
                 axis=td._axis;
                 units=td._units;
             }
             streams[h]=createStream(name,desc,axis,units);
         }
             
         // Next line should be blank
         String[] row=parser.readNext();
         if (row.length!=1 || row[0].length()!=0) {
             ok.addError(new BasicError("The logfile "+f.getAbsolutePath()+
               " appears to have data on the second line, it should be empty"));
             ok.setValue(Boolean.FALSE);
         }
         
         // Now read each row into the streams
         int line=3;
         while (true) {
             row=parser.readNext();
             if (row==null)
                 break;
             if (row.length-1!=streams.length) {
                 ok.addError(new BasicError("On line "+line+" of the logfile " + 
                         f.getAbsolutePath() + " there are only "+row.length+
                         " values, there should be "+streams.length+"."));
                 ok.setValue(Boolean.FALSE);                
             } else {
                for (int r=0;r<streams.length-1;r++) {
                     streams[r].addData(row[r].trim());
                 }
             }
         }
         
         // Reverse stream contents before we start mucking with them
         for (RWStream s : streams) {
             s.reverse();
         }
         
         // Create the virtual streams
         ArrayList<VStream> vs=new ArrayList();
         for (VirtualTraceData td : _virtualTraceData) {
             VStream s;
             try {
                 s = (VStream)td._streamClass.newInstance();
                 
                 WithError<Boolean,BasicError> vok=new WithError<Boolean,BasicError>(true);
                 s.setView(this,td._arg,handler,vok);
                 if (vok.value()) {
                     addVirtualStream(s.name(),s);
                     vs.add(s);
                     if (td!=null && td._rebase)
                         s.setMeta("rebase", "true");
                 }
                 ok.appendErrors(vok);
             } catch (InstantiationException ex) {
                 ok.addError(new BasicError(ex));
             } catch (IllegalAccessException ex) {
                 ok.addError(new BasicError(ex));
             }
         }
     }
 
     public String name() {
         return _name;
     }
 }
