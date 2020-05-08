 /**
  *Copyright (C) 2013  Wikimedia Foundation
  *
  *This program is free software; you can redistribute it and/or
  *modify it under the terms of the GNU General Public License
  *as published by the Free Software Foundation; either version 2
  *of the License, or (at your option) any later version.
  *
  *This program is distributed in the hope that it will be useful,
  *but WITHOUT ANY WARRANTY; without even the implied warranty of
  *MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *GNU General Public License for more details.
  *
  *You should have received a copy of the GNU General Public License
  *along with this program; if not, write to the Free Software
  *Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301, USA.
 
  */
 
 package org.wikimedia.analytics.varnishkafka;
 
 import org.apache.avro.Schema;
 import org.apache.avro.file.DataFileWriter;
 import org.apache.avro.generic.GenericData;
 import org.apache.avro.generic.GenericDatumWriter;
 import org.apache.avro.generic.GenericRecord;
 import org.apache.avro.io.DatumWriter;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.LineIterator;
 import org.apache.log4j.Logger;
 import org.codehaus.jackson.JsonEncoding;
 import org.codehaus.jackson.JsonFactory;
 import org.codehaus.jackson.JsonGenerationException;
 import org.codehaus.jackson.JsonGenerator;
 import org.codehaus.jackson.map.JsonMappingException;
 import org.xerial.snappy.SnappyOutputStream;
 
 import java.io.*;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Map;
 
 
 public class Cli {
 
     private File inputFile;
 
     private String format;
 
     private boolean compress;
 
     private long start;
 
     private long end;
 
     private double linesPerSec;
 
     private double timeElapsed;
 
     private long fileSize;
 
     private File cwd = new File(System.getProperty("user.dir"));
 
     private Map<String, Results> results = new HashMap<String, Results>();
 
     static Logger log = Logger.getLogger(Cli.class.getName());
 
 
     public long getStart() {
         return start;
     }
 
     public void setStart(long start) {
         this.start = start;
     }
 
     public long getEnd() {
         return end;
     }
 
     public void setEnd(long end) {
         this.end = end;
     }
 
     public double getLinesPerSec() {
         return linesPerSec;
     }
 
     public void setLinesPerSec(double linesPerSec) {
         this.linesPerSec = linesPerSec;
     }
 
     public double getTimeElapsed() {
         return timeElapsed;
     }
 
     public void setTimeElapsed(double timeElapsed) {
         this.timeElapsed = timeElapsed;
     }
 
     public long getFileSize() {
         return fileSize;
     }
 
     public void setFileSize(long fileSize) {
         this.fileSize = fileSize;
     }
 
     public String getFormat() {
         return format;
     }
 
     public void setFormat(String format) {
         this.format = format;
     }
 
     public static void main(final String[] args) {
         org.apache.log4j.BasicConfigurator.configure();
 
         if (args.length != 2 && args.length != 3) {
             System.out.println("Please specify either three parameters:\n " +
                     "1) full input path to raw ncsa logging format\n " +
                     "2) the output format such as 'json', 'protobufs', 'avro', or 'tsv'\n " +
                     "3) whether to use snappy compression true/false\n" +
                     " or \n" +
                     "specify 'suite' as command to run all the benchmarks.");
             System.exit(-1);
         }
 
         Cli cli = new Cli();
 
         cli.inputFile = new File(args[0]);
         if (!cli.inputFile.exists()) {
             log.error("Input path to file does not exist");
             System.exit(-1);
         } else {
             log.info("Input file: " + cli.inputFile.toString());
         }
 
 
         if (args.length == 3) {
             cli.compress = args[2].equals("true");
             if (cli.compress) {
                 cli.setFormat(args[1] + ".snappy");
             } else {
                 cli.setFormat(args[1]);
             }
             cli.runBenchmark();
         } else {
             cli.runSuiteBenchmark();
         }
     }
 
     public void runSuiteBenchmark() {
         String[] formats = {"tsv", "avro", "protobufs", "json", "json.snappy"};
         Results result = null;
         for (String format : formats) {
             setFormat(format);
 
             for (int i = 0; i < 10; i++) {
                 runBenchmark();
                 determineFileSize();
                 result = new Results();
                 result.setFormat(format);
                 result.setFileSize(getFileSize());
                 result.setLinesPerSec(getLinesPerSec());
                 result.setTimeElapsed(getTimeElapsed());
                 if ("json.snappy".equals(format)) {
                     result.setCompressed(true);
                 } else {
                     result.setCompressed(false);
                 }
                 results.put(format, result);
             }
         }
         printResults();
     }
 
     private void printResults() {
         Iterator it = results.entrySet().iterator();
         System.out.println("Format \t Filesize \t Avg. Time \t Lines/Sec \t Compressed");
         while (it.hasNext()) {
             Map.Entry pairs = (Map.Entry) it.next();
             Results result =  (Results) pairs.getValue();
 
             System.out.print(result.getFormat() + "\t");
             System.out.print(result.getFileSize() / (1024 * 1024) + "Mb\t");
             System.out.print(result.getAverageTimeElapsed() + "\t");
             System.out.print(result.getLinesPerSec() + "\t");
             System.out.print(result.isCompressed());
             System.out.println();
         }
     }
 
     private void runBenchmark() {
         int n = 0;
         if (format.contains("json")) {
             if (format.contains("snappy")) {
                 compress = true;
             }
             n = writeJsonOutput();
         }  else if ("protobufs".equals(format)) {
             n = writeProtobufOutput();
         } else if ("avro".equals(format)) {
             n = writeAvroOutput();
         } else if ("tsv".equals(format)) {
             n = writeEscapedOutput();
         }  else {
             log.error("Format is not 'json', 'protobufs', 'avro' or 'tsv'.");
             System.exit(-1);
         }
 
         long elapsedTime =  (getEnd() - getStart());
         double seconds = (double) elapsedTime / 1000000000.0;
 
         double avg = n / seconds;
         setLinesPerSec(avg);
         setTimeElapsed(seconds);
         log.info("Elapsed time (secs): " + seconds + " lines/sec: " + avg);
     }
 
     private void determineFileSize() {
         File file = new File(cwd.getPath(), "test." + getFormat());
         setFileSize(file.length());
     }
 
     private Integer parseBytesSent(final String bytesSent) {
         try {
             return Integer.parseInt(bytesSent);
         }  catch (NumberFormatException e) {
             return 0;
         }
     }
 
     private Integer writeEscapedOutput() {
         int n = 0;
         OutputStream out = null;
         BufferedOutputStream bos = null;
         try {
             LineIterator it = FileUtils.lineIterator(inputFile, "UTF-8");
             File outputFile = new File(cwd.getPath(), "test." + getFormat());
             outputFile.delete();
             log.info("Output file path: " + outputFile.toString());
             out = new FileOutputStream(outputFile);
             bos = new BufferedOutputStream(out);
             setStart(System.nanoTime());
             while (it.hasNext()) {
                 n++;
                 String line = it.nextLine();
                 String[] fields = line.split("\\t");
                 String ua = fields[14].replace("\\t"," ").replace("\\n", " ");
                 fields[14] = ua;
 
                 for (String field : fields) {
                     bos.write(field.getBytes());
                     bos.write("\t".getBytes());
                 }
                 bos.write("\n".getBytes());
             }
             setEnd(System.nanoTime());
         } catch (IOException e) {
             e.printStackTrace();
         } finally {
             try {
                 out.close();
                 bos.close();
             } catch (IOException e) {
                 e.printStackTrace();
             }
         }
         return n;
     }
 
     private Integer writeJsonOutput() {
         int n = 0;
         JsonFactory jfactory = new JsonFactory();
 
         /*** write to file ***/
         try {
             JsonGenerator jGenerator;
 
             SnappyOutputStream snappyOutputStream = null;
             File outputFile = new File(cwd.getPath(), "test." + getFormat());
             OutputStream out = new FileOutputStream(outputFile);
             BufferedOutputStream bos = new BufferedOutputStream(out);
 
             if (compress) {
                 log.info("Snappy compression is enabled.");
 
                 out = new FileOutputStream(outputFile);
                 snappyOutputStream = new SnappyOutputStream(out);
                 jGenerator = jfactory.createJsonGenerator(snappyOutputStream, JsonEncoding.UTF8);
             } else {
                 jGenerator = jfactory.createJsonGenerator(bos, JsonEncoding.UTF8);
             }
 
             log.info("Output file path: " + outputFile.toString());
 
 
             LineIterator it = FileUtils.lineIterator(inputFile, "UTF-8");
 
             try {
                 setStart(System.nanoTime());
                 while (it.hasNext()) {
                     n++;
                     String line = it.nextLine();
                     String[] fields = line.split("\\t");
 
                     jGenerator.writeStartObject();
 
                     jGenerator.writeNumberField("kafka_offset", Long.parseLong(fields[0]));
                     jGenerator.writeStringField("host", fields[1]);
                     jGenerator.writeNumberField("seq_num", Long.parseLong(fields[2]));
                     jGenerator.writeStringField("timestamp", fields[3]);
                     jGenerator.writeNumberField("response", Float.parseFloat(fields[4]));
                     jGenerator.writeStringField("ip", fields[5]);
                     jGenerator.writeStringField("http_status", fields[6]);
                     jGenerator.writeNumberField("bytes_sent", parseBytesSent(fields[7]));
                     jGenerator.writeStringField("request_method", fields[8]);
                     jGenerator.writeStringField("uri", fields[9]);
                     jGenerator.writeStringField("proxy_host", fields[10]);
                     jGenerator.writeStringField("mime_type", fields[11]);
                     jGenerator.writeStringField("referer", fields[12]);
                     jGenerator.writeStringField("x_forwarded_for", fields[13]);
                     jGenerator.writeStringField("user_agent", fields[14]);
                     jGenerator.writeStringField("accept_language", fields[15]);
                     jGenerator.writeStringField("x_analytics", fields[16]);
 
                     jGenerator.writeEndObject();
                 }
                 setEnd(System.nanoTime());
             } finally {
                 it.close();
                 jGenerator.close();
                 if (compress)  {
                     snappyOutputStream.close();
                 } else {
                     out.close();
                     bos.close();
                 }
             }
         } catch (JsonGenerationException e) {
             e.printStackTrace();
         } catch (JsonMappingException e) {
             e.printStackTrace();
         } catch (IOException e) {
             e.printStackTrace();
         }
         return n;
     }
 
     private Integer writeProtobufOutput() {
         int n = 0;
         try {
             LineIterator it = FileUtils.lineIterator(inputFile, "UTF-8");
             File outputFile = new File(cwd.getPath(), "test." + getFormat());
             OutputStream out = new FileOutputStream(outputFile);
             BufferedOutputStream bos = new BufferedOutputStream(out);
             log.info("Output file path: " + outputFile.toString());
             try {
                 setStart(System.nanoTime());
                 while (it.hasNext()) {
                     n++;
                     String line = it.nextLine();
                     String[] fields = line.split("\\t");
                     Logline.LogLine logline = Logline.LogLine.newBuilder()
                             .setKafkaOffset(Long.parseLong(fields[0]))
                             .setHost(fields[1])
                             .setSeqNum(Long.parseLong(fields[2]))
                             .setTimestamp(fields[3])
                             .setResponse(Float.parseFloat(fields[4]))
                             .setIp(fields[5])
                             .setHttpStatus(fields[6])
                             .setBytesSent(parseBytesSent(fields[7]))
                             .setRequestMethod(fields[8])
                             .setUri(fields[9])
                             .setProxyHost(fields[10])
                             .setMimeType(fields[11])
                             .setReferer(fields[12])
                             .setXForwardedFor(fields[13])
                             .setUserAgent(fields[14])
                             .setAcceptLanguage(fields[15])
                             .setXAnalytics(fields[16])
                             .build();
 
                     bos.write(logline.toByteArray());
                 }
                 setEnd(System.nanoTime());
             } finally {
                 try {
                     out.close();
                     bos.close();
                 } catch (IOException e) {
                     e.printStackTrace();
                 }
             }
         } catch (IOException e) {
             e.printStackTrace();
         } catch (Exception e) {
             e.printStackTrace();
         }
         return n;
     }
 
     private Integer writeAvroOutput() {
         Schema schema = null;
         int n = 0;
 
         try {
            InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream("WebRequest.avro.json");
                     //Cli.class.getResourceAsStream("/");
             schema = new Schema.Parser().parse(inputStream);
             //schema = Schema.parse(inputStream);
             inputStream.close();
 
             File file = new File(cwd.getPath(), "test." + getFormat());
             log.info("Output file path: " + file.toString());
             file.delete();
             DatumWriter<GenericRecord> writer = new GenericDatumWriter<GenericRecord>(
                     schema);
             DataFileWriter<GenericRecord> dataFileWriter = new DataFileWriter<GenericRecord>(
                     writer);
             dataFileWriter.create(schema, file);
 
             try {
                 LineIterator it = FileUtils.lineIterator(inputFile, "UTF-8");
 
                 try {
                     setStart(System.nanoTime());
                     while (it.hasNext()) {
                         n++;
                         String line = it.nextLine();
                         String[] fields = line.split("\\t");
 
                         // Populate data
                         GenericRecord r = new GenericData.Record(schema);
                         r.put("kafka_offset", Long.parseLong(fields[0]));
                         r.put("host", fields[1]);
                         r.put("seq_num", Long.parseLong(fields[2]));
                         r.put("timestamp", fields[3]);
                         r.put("response", Float.parseFloat(fields[4]));
                         r.put("ip", fields[5]);
                         r.put("http_status", fields[6]);
                         r.put("bytes_sent", parseBytesSent(fields[7]));
                         r.put("request_method", fields[8]);
                         r.put("uri", fields[9]);
                         r.put("proxy_host", fields[10]);
                         r.put("mime_type", fields[11]);
                         r.put("referer", fields[12]);
                         r.put("x_forwarded_for", fields[13]);
                         r.put("user_agent", fields[14]);
                         r.put("accept_language", fields[15]);
                         r.put("x_analytics", fields[16]);
                         dataFileWriter.append(r);
                     }
 
                     setEnd(System.nanoTime());
                 } finally {
                     dataFileWriter.close();
                 }
             } catch (IOException ex) {
                 ex.printStackTrace();
             }
         } catch (IOException e) {
             e.printStackTrace();
         }
         return n;
     }
 }
