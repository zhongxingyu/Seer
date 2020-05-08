 package net.madz.download.engine.impl.metadata;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.net.HttpURLConnection;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.madz.download.engine.DownloadSegment;
 import net.madz.download.engine.DownloadTask;
 import net.madz.download.engine.IDownloadProcess.StateEnum;
 import net.madz.download.engine.impl.DownloadEngine;
 import net.madz.download.service.exception.ExceptionMessage;
 import net.madz.download.service.exception.ServiceException;
 import net.madz.download.service.requests.CreateTaskRequest;
 import net.madz.download.utils.LogUtils;
 
 public class MetaManager {
 
     public static HashMap<URL, DownloadTask> allTasks = new HashMap<URL, DownloadTask>();
 
     public static void serializeForPreparedState(DownloadTask task, File file) {
         RandomAccessFile randomAccessFile = null;
         try {
             randomAccessFile = new RandomAccessFile(file, "rw");
             randomAccessFile.seek(MetadataConsts.TOTAL_LENGTH_POSITION);
             randomAccessFile.writeLong(task.getTotalLength());
             randomAccessFile.seek(MetadataConsts.SEGMENTS_NUMBER_POSITION);
             randomAccessFile.writeInt(task.getSegmentsNumber());
             randomAccessFile.seek(MetadataConsts.RESUMABLE_FLAG_POSITION);
             randomAccessFile.writeBoolean(task.isResumable());
         } catch (FileNotFoundException e) {
             throw new IllegalStateException(e);
         } catch (IOException e) {
             throw new IllegalStateException(e);
         } finally {
             try {
                 if ( null != randomAccessFile ) {
                     randomAccessFile.close();
                 }
             } catch (IOException ignored) {
                 LogUtils.error(MetaManager.class, ignored);
             }
         }
     }
 
     public static DownloadTask createDownloadTask(CreateTaskRequest request) throws ServiceException {
         URL url = null;
         URL referURL = null;
         boolean resumable = false;
         try {
             url = new URL(request.getUrl());
             referURL = new URL(request.getReferURL());
         } catch (MalformedURLException ignored) {
             LogUtils.error(MetaManager.class, ignored);
         }
         DownloadTask task = new DownloadTask(url,referURL,new File(request.getFolder()), request.getFilename());
         task.setThreadNumber(new Integer(request.getThreadNumber()).byteValue());
         try {
             resumable = checkResumable(url);
             task.setResumable(resumable);
         } catch (IOException ex) {
             throw new ServiceException("Failed to connect to " + url.toString() + ". Please check your network.");
         }
         task.setTotalLength(getTotalLength(url));
         task.setState((byte) StateEnum.New.ordinal());
         if ( resumable ) {
             int segmentsNumber = (int) ( task.getTotalLength() / MetadataConsts.ONE_SEGEMENT );
             task.setSegmentsNumber(segmentsNumber <= 0 ? 1 : segmentsNumber);
         } else {
             task.setSegmentsNumber(1);
         }
         return task;
     }
 
     public static long getTotalLength(URL url) throws ServiceException {
         URLConnection openConnection = null;
         int totalLength = 0;
         try {
             openConnection = url.openConnection();
             openConnection.connect();
             totalLength = openConnection.getContentLength();
         } catch (IOException e) {
             throw new ServiceException("Failed to connect to " + url.toString() + ". Please check your network.");
         }
         return totalLength;
     }
 
     public static DownloadTask deserializeHeadInformation(File file) {
         DownloadTask task = null;
         RandomAccessFile randomAccessFile = null;
         StringBuilder headInfo = new StringBuilder();
         try {
             randomAccessFile = new RandomAccessFile(file, "rw");
             // Task ID
             //
             randomAccessFile.seek(MetadataConsts.ID_POSITION);
             final int id = randomAccessFile.readInt();
             task.setId(id);
             headInfo.append(" ID:" + id);
             // URL
             //
             randomAccessFile.seek(MetadataConsts.URL_SIZE_POSITION);
             final int urlLength = randomAccessFile.readInt();
             byte[] result = new byte[urlLength];
             randomAccessFile.seek(MetadataConsts.URL_POSITION);
             randomAccessFile.readFully(result, 0, urlLength);
             headInfo.append(" URL:");
             String urlStr = new String(result, "utf8");
             headInfo.append(urlStr);
             URL url = new URL(urlStr);
             task.setUrl(url);
             // Refer URL
             //
             randomAccessFile.seek(MetadataConsts.REFER_URL_SIZE_POSITION);
             final int referUrlLength = randomAccessFile.readInt();
             String referUrl = "";
             if ( 0 < referUrlLength ) {
                 result = new byte[referUrlLength];
                 randomAccessFile.seek(MetadataConsts.REFER_URL_POSITION);
                 randomAccessFile.readFully(result, 0, referUrlLength);
                 headInfo.append(" Refer URL:");
                 referUrl = new String(result, "utf8");
                 headInfo.append(referUrl);
                 task.setReferURL(new URL(referUrl));
             } else {
                 referUrl = "";
             }
             // Folder
             //
             randomAccessFile.seek(MetadataConsts.FOLDER_SIZE_POSITION);
             final int folderLength = randomAccessFile.readInt();
             if ( 0 < folderLength ) {
                 result = new byte[folderLength];
                 randomAccessFile.seek(MetadataConsts.FOLDER_POSITION);
                 randomAccessFile.readFully(result, 0, folderLength);
                 headInfo.append(" Folder:");
                 String folder = new String(result, "utf8");
                 headInfo.append(new String(result));
                 task.setFolder(new File(folder));
             }
             // File name
             //
             randomAccessFile.seek(MetadataConsts.FILE_NAME_SIZE_POSITION);
             final int filenameLength = randomAccessFile.readInt();
             if ( 0 < filenameLength ) {
                 result = new byte[filenameLength];
                 randomAccessFile.seek(MetadataConsts.FILE_NAME_POSITION);
                 randomAccessFile.readFully(result, 0, filenameLength);
                 headInfo.append(" File name:");
                 String filename = new String(result, "utf8");
                 headInfo.append(new String(result));
                 task.setFileName(filename);
             }
             // Total length
             //
             randomAccessFile.seek(MetadataConsts.TOTAL_LENGTH_POSITION);
             headInfo.append(" Total Length:");
             long totalLength = randomAccessFile.readLong();
             headInfo.append(totalLength);
             task.setTotalLength(totalLength);
             // Segments number
             //
             randomAccessFile.seek(MetadataConsts.SEGMENTS_NUMBER_POSITION);
             headInfo.append(" Segements Number:");
             int segmentNumber = randomAccessFile.readInt();
             headInfo.append(segmentNumber);
             task.setSegmentsNumber(segmentNumber);
             // Resumable
             //
             randomAccessFile.seek(MetadataConsts.RESUMABLE_FLAG_POSITION);
             headInfo.append(" Resumable:");
             byte resumableValue = randomAccessFile.readByte();
             if ( resumableValue == 0 ) {
                 task.setResumable(false);
                 headInfo.append(false);
             } else {
                 task.setResumable(true);
                 headInfo.append(true);
             }
             // Thread number
             //
             randomAccessFile.seek(MetadataConsts.THREAD_NUMBER_POSITION);
             headInfo.append(" Thread Number:");
             byte threadNumber = randomAccessFile.readByte();
             headInfo.append(threadNumber);
             task.setThreadNumber(threadNumber);
             // State
             //
             randomAccessFile.seek(MetadataConsts.STATE_POSTION);
             headInfo.append(" State:");
             byte stateValue = randomAccessFile.readByte();
             headInfo.append(StateEnum.valueof(stateValue));
             task.setState(stateValue);
             System.out.println("Task header Information:");
             System.out.println(headInfo.toString());
         } catch (FileNotFoundException ignored) {
             LogUtils.error(MetaManager.class, ignored);
         } catch (IOException ignored) {
             LogUtils.error(MetaManager.class, ignored);
         } finally {
             if ( null != randomAccessFile ) {
                 try {
                     randomAccessFile.close();
                 } catch (IOException ignored) {
                     LogUtils.error(MetaManager.class, ignored);
                 }
             }
         }
         return task;
     }
 
     public static boolean checkResumable(URL url) throws IOException {
         boolean resumable = false;
         final HttpURLConnection conn = (HttpURLConnection) url.openConnection();
         conn.setRequestProperty("RANGE", "bytes=" + 0 + "-" + 0);
         conn.connect();
         final int statusCode = conn.getResponseCode();
         if ( 206 == statusCode ) {
             resumable = true;
             return resumable;
         }
         final String acceptRanges = conn.getHeaderField("Accept-Ranges");
         if ( "bytes".equalsIgnoreCase(acceptRanges) ) {
             resumable = true;
         } else {
             System.out.println("Accept-Ranges" + acceptRanges);
         }
         return resumable;
     }
 
     public static void updateTaskState(DownloadTask task, File logFile) {
         RandomAccessFile raf = null;
         try {
             raf = new RandomAccessFile(logFile, "rw");
             raf.seek(MetadataConsts.STATE_POSTION);
             raf.writeByte(task.getState());
         } catch (Exception e) {
             LogUtils.error(MetaManager.class, e); // ignored the exception
         } finally {
             if ( null != raf ) {
                 try {
                     raf.close();
                 } catch (IOException ignored) {
                     LogUtils.error(MetaManager.class, ignored); // ignored the
                                                                 // exception
                 }
             }
         }
     }
 
     public static void deserializeSegmentsInformation(DownloadTask task, File logFile) throws ServiceException {
         int segments = task.getSegmentsNumber();
         StringBuilder segmentsInformation = new StringBuilder();
         RandomAccessFile raf = null;
         try {
             raf = new RandomAccessFile(logFile, "rw");
             long position = 0;
             for ( int i = 0; i < segments; i++ ) {
                 DownloadSegment item = new DownloadSegment();
                 // Segment Id
                 //
                 position = MetadataConsts.FIRST_SEGMENT_POSITION + i * MetadataConsts.SEGMENT_LENGTH;
                 raf.seek(position);
                 segmentsInformation.append("Segment Id:");
                 final int id = raf.readInt();
                 segmentsInformation.append(id);
                 item.setId(id);
                 // Segment Start bytes
                 //
                 position += MetadataConsts.SEGMENT_ID_LENGTH;
                 raf.seek(position);
                 segmentsInformation.append(" start bytes:");
                 final long startBytes = raf.readLong();
                 segmentsInformation.append(startBytes);
                 item.setStartBytes(startBytes);
                 // Segment End bytes
                 //
                 position += MetadataConsts.SEGMENT_START_BYTES_LENGTH;
                 segmentsInformation.append(" end bytes:");
                 final long endBytes = raf.readLong();
                 segmentsInformation.append(endBytes);
                 item.setEndBytes(endBytes);
                 // Segment current bytes
                 //
                 position += MetadataConsts.SEGMENT_END_BYTES_LENGTH;
                 segmentsInformation.append(" current bytes:");
                 final long currentBytes = raf.readLong();
                 segmentsInformation.append(currentBytes);
                 item.setCurrentBytes(currentBytes);
                 // Segment state
                 //
                 position += MetadataConsts.SEGMENT_CURRENT_BYTES_LENGTH;
                 segmentsInformation.append(" state:");
                 final byte state = raf.readByte();
                 segmentsInformation.append(StateEnum.valueof(state));
                 item.setState(state);
                 task.addSegment(item);
                 
                 segmentsInformation.append("\n");
             }
         } catch (FileNotFoundException e) {
             LogUtils.error(MetaManager.class, e);
             throw new ServiceException(ExceptionMessage.LOG_FILE_WAS_NOT_FOUND + logFile.getName());
         } catch (IOException e) {
             LogUtils.error(MetaManager.class, e);
             throw new ServiceException(ExceptionMessage.LOG_FILE_IS_NOT_COMPLETE + logFile.getName());
         } finally {
             if ( null != raf ) {
                 try {
                     raf.close();
                 } catch (IOException ignored) {
                     LogUtils.error(MetaManager.class, ignored);
                 }
             }
         }
         System.out.println("Segements Information:" + segmentsInformation.toString());
     }
 
     public static void serializeForNewState(DownloadTask task, File file) {
         RandomAccessFile randomAccessFile = null;
         try {
             randomAccessFile = new RandomAccessFile(file, "rw");
             // Task Id
             //
             int id = task.getId();
             randomAccessFile.seek(MetadataConsts.ID_POSITION);
             randomAccessFile.writeInt(id);
             // URL
             //
             byte[] urlBytes = task.getUrl().toString().getBytes("utf8");
             randomAccessFile.seek(MetadataConsts.URL_SIZE_POSITION);
             randomAccessFile.writeInt(urlBytes.length);
             randomAccessFile.seek(MetadataConsts.URL_POSITION);
             randomAccessFile.write(urlBytes);
             // REFER URL
             //
             randomAccessFile.seek(MetadataConsts.REFER_URL_SIZE_POSITION);
             byte[] referUrlBytes = task.getReferURL().toString().getBytes("utf8");
             randomAccessFile.writeInt(referUrlBytes.length);
             randomAccessFile.seek(MetadataConsts.REFER_URL_POSITION);
             randomAccessFile.write(referUrlBytes);
             // FOLDER
             //
             randomAccessFile.seek(MetadataConsts.FOLDER_SIZE_POSITION);
             byte[] folderBytes = task.getFolder().toString().getBytes("utf8");
             randomAccessFile.writeInt(folderBytes.length);
             randomAccessFile.seek(MetadataConsts.FOLDER_POSITION);
             randomAccessFile.write(folderBytes);
             // File name
             //
             randomAccessFile.seek(MetadataConsts.FILE_NAME_SIZE_POSITION);
             byte[] filenameBytes = task.getFileName().getBytes("utf8");
             randomAccessFile.writeInt(filenameBytes.length);
             randomAccessFile.seek(MetadataConsts.FILE_NAME_POSITION);
             randomAccessFile.write(filenameBytes);
             // Thread number
             //
             randomAccessFile.seek(MetadataConsts.THREAD_NUMBER_POSITION);
             randomAccessFile.writeByte(task.getThreadNumber());
             // State
             //
             randomAccessFile.seek(MetadataConsts.STATE_POSTION);
             randomAccessFile.writeByte(StateEnum.New.ordinal());
         } catch (FileNotFoundException e) {
             throw new IllegalStateException(e);
         } catch (IOException e) {
             throw new IllegalStateException(e);
         } finally {
             try {
                 if ( null != randomAccessFile ) {
                     randomAccessFile.close();
                 }
             } catch (IOException ignored) {
             }
         }
     }
 
     public static void serializeSegmentsInformation(DownloadTask task, File metadataFile) {
         RandomAccessFile raf = null;
         try {
             raf = new RandomAccessFile(metadataFile, "rw");
             for ( DownloadSegment segment : task.getSegments() ) {
                 int seq = segment.getId();
                 long position = MetadataConsts.FIRST_SEGMENT_POSITION + seq * MetadataConsts.SEGMENT_LENGTH;
                 raf.seek(position);
                 raf.writeInt(seq);
                 position += MetadataConsts.SEGMENT_ID_LENGTH;
                 raf.writeLong(segment.getStartBytes());
                 position += MetadataConsts.SEGMENT_START_BYTES_LENGTH;
                 raf.writeLong(segment.getEndBytes());
                 position += MetadataConsts.SEGMENT_END_BYTES_LENGTH;
                 raf.writeLong(segment.getCurrentBytes());
                 position += MetadataConsts.SEGMENT_CURRENT_BYTES_LENGTH;
                 raf.writeByte(StateEnum.Prepared.ordinal());
             }
         } catch (FileNotFoundException ignored) {
             LogUtils.error(MetaManager.class, ignored);
         } catch (IOException ignored) {
             LogUtils.error(MetaManager.class, ignored);
         } finally {
             if ( null != raf ) {
                 try {
                     raf.close();
                 } catch (IOException ignored) {
                     LogUtils.error(MetaManager.class, ignored);
                 }
             }
         }
     }
 
     public static void computeSegmentsInformation(DownloadTask task) {
         int segmentsNumber = task.getSegmentsNumber();
         long partLength = 0L;
         long totalLength = task.getTotalLength();
         partLength = totalLength / segmentsNumber;
         for ( int i = 0; i < segmentsNumber; i++ ) {
             DownloadSegment segment = new DownloadSegment();
             segment.setId(i);
             final int seq = i;
             final long finalPartLength = partLength;
             if ( i < segmentsNumber - 1 ) {
                 segment.setStartBytes(finalPartLength * seq);
                 segment.setEndBytes(finalPartLength * ( seq + 1 ) - 1);
                 segment.setCurrentBytes(finalPartLength * seq);
                 System.out.println("segment id:" + i);
                 System.out.println("segment start bytes:" + segment.getStartBytes());
                 System.out.println("segment end bytes:" + segment.getEndBytes());
             } else {
                 final long finalTotalLength = totalLength;
                 segment.setStartBytes(finalPartLength * seq);
                 segment.setCurrentBytes(finalPartLength * seq);
                 segment.setEndBytes(finalTotalLength - 1);
                 System.out.println("segment id:" + i);
                 System.out.println("segment start bytes:" + segment.getStartBytes());
                 System.out.println("segment end bytes:" + segment.getEndBytes());
             }
             task.addSegment(segment);
         }
     }
 
     public static void updateTaskState(File metadataFile, StateEnum state) throws FileNotFoundException, IOException {
         RandomAccessFile raf = null;
         try {
             raf = new RandomAccessFile(metadataFile, "rw");
             raf.seek(MetadataConsts.STATE_POSTION);
             raf.writeByte(state.ordinal());
         } finally {
             if ( null != raf ) {
                 raf.close();
             }
         }
     }
 
     public static void updateSegmentDownloadProgress(File metadataFile, int segmentId, long currentBytes) throws FileNotFoundException, IOException {
         RandomAccessFile raf = null;
         try {
             raf = new RandomAccessFile(metadataFile, "rw");
             final long segmentStartPosition = MetadataConsts.FIRST_SEGMENT_POSITION + MetadataConsts.SEGMENT_LENGTH * segmentId;
             final long endBytesPosition = segmentStartPosition + MetadataConsts.SEGMENT_ID_LENGTH + MetadataConsts.SEGMENT_START_BYTES_LENGTH;
             final long currentBytesPosition = segmentStartPosition + MetadataConsts.SEGMENT_ID_LENGTH + MetadataConsts.SEGMENT_START_BYTES_LENGTH
                     + MetadataConsts.SEGMENT_END_BYTES_LENGTH;
             final long statePosition = currentBytesPosition + MetadataConsts.SEGMENT_CURRENT_BYTES_LENGTH;
             // Log current bytes
             //
             raf.seek(currentBytesPosition);
             raf.writeLong(currentBytes);
             // Get end bytes
             //
             raf.seek(endBytesPosition);
             final long endBytes = raf.readLong();
             // Update segment state
             //
             if ( currentBytes < endBytes ) {
                 raf.seek(statePosition);
                 raf.writeByte(StateEnum.Started.ordinal());
             } else {
                 raf.seek(statePosition);
                 raf.writeByte(StateEnum.Finished.ordinal());
             }
         } finally {
             if ( null != raf ) {
                 raf.close();
             }
         }
     }
 
     public static void initiateMetadataDirs() {
         createFolder("./meta");
     }
 
     private static void createFolder(String path) {
         File folder = new File(path);
         if ( !folder.exists() ) {
             folder.mkdir();
         }
     }
 
     public synchronized static DownloadTask[] load(String root) {
         List<DownloadTask> results = new LinkedList<DownloadTask>();
         List<File> files = new LinkedList<File>();
         files = parseFolder(files, new File(root));
         for ( File file : files ) {
             DownloadTask task = MetaManager.deserializeHeadInformation(file);
             try {
                 MetaManager.deserializeSegmentsInformation(task, file);
             } catch (ServiceException ignored) {
                 LogUtils.error(MetaManager.class, ignored);
             }
             task.toString();
             results.add(task);
         }
         return results.toArray(new DownloadTask[results.size()]);
     }
 
     public synchronized static DownloadTask load(String root, int taskId) {
         DownloadTask result = null;
         List<File> files = new LinkedList<File>();
         files = parseFolder(files, new File(root));
         for ( File file : files ) {
             DownloadTask task = MetaManager.deserializeHeadInformation(file);
             try {
                 MetaManager.deserializeSegmentsInformation(task, file);
             } catch (ServiceException ignored) {
                 LogUtils.error(MetaManager.class, ignored);
             }
             if ( taskId == task.getId() ) {
                 result = task;
             }
         }
         return result;
     }
 
     private static List<File> parseFolder(List<File> result, File root) {
         File[] listFiles = root.listFiles();
         if ( null == listFiles ) {
             return null;
         }
         for ( File file : listFiles ) {
             if ( file.isFile() && file.getName().contains(DownloadEngine.META_SUFFIX) ) {
                 result.add(file);
             } else {
                 parseFolder(result, file);
             }
         }
         return result;
     }
 
     public static void updateSegmentState(File metadataFile, DownloadTask task, StateEnum state) throws FileNotFoundException, IOException {
         RandomAccessFile raf = null;
         List<DownloadSegment> segments = task.getSegments();
         try {
             raf = new RandomAccessFile(metadataFile, "rw");
             for ( int i = 0; i < task.getSegmentsNumber(); i++ ) {
                 final long segmentStartPosition = MetadataConsts.FIRST_SEGMENT_POSITION + MetadataConsts.SEGMENT_LENGTH * i;
                 final long statePosition = segmentStartPosition + MetadataConsts.SEGMENT_ID_LENGTH + MetadataConsts.SEGMENT_START_BYTES_LENGTH
                         + MetadataConsts.SEGMENT_END_BYTES_LENGTH + MetadataConsts.SEGMENT_CURRENT_BYTES_LENGTH;
                 // Update segment state
                 //
                 raf.seek(statePosition);
                 byte lastState = raf.readByte();
                 //
                 //
                 if ( StateEnum.Prepared.ordinal() == lastState || StateEnum.Started.ordinal() == lastState ) {
                     raf.seek(statePosition);
                     raf.writeByte(state.ordinal());
                 }
                 if ( StateEnum.Paused.ordinal() == lastState ) {
                     raf.seek(statePosition);
                     raf.writeByte(state.ordinal());
                     segments.get(i).setState((byte) state.ordinal());
                 }
             }
         } finally {
             if ( null != raf ) {
                 raf.close();
             }
         }
     }
 }
