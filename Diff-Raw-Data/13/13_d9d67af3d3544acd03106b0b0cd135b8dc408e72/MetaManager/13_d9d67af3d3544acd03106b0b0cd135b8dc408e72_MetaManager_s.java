 package net.madz.download.service.metadata;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.RandomAccessFile;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 
 import net.madz.download.LogUtils;
 import net.madz.download.engine.IDownloadProcess.StateEnum;
 import net.madz.download.service.exception.ErrorException;
 import net.madz.download.service.exception.ErrorMessage;
 import net.madz.download.service.requests.CreateTaskRequest;
 
 public class MetaManager {
 
     public static HashMap<URL, DownloadTask> allTasks = new HashMap<URL, DownloadTask>();
 
     public static void serializeForPreparedState(DownloadTask task, File file) {
         RandomAccessFile randomAccessFile = null;
         try {
             randomAccessFile = new RandomAccessFile(file, "rw");
             randomAccessFile.seek(Consts.TOTAL_LENGTH_POSITION);
             randomAccessFile.writeLong(task.getTotalLength());
             randomAccessFile.seek(Consts.SEGMENTS_NUMBER_POSITION);
             randomAccessFile.writeInt(task.getSegmentsNumber());
             randomAccessFile.seek(Consts.RESUMABLE_FLAG_POSITION);
             randomAccessFile.writeBoolean(task.isResumable());
             randomAccessFile.seek(Consts.STATE_POSTION);
             randomAccessFile.writeByte(StateEnum.Prepared.ordinal());
         } catch (FileNotFoundException e) {
            e.printStackTrace();
         } catch (IOException e) {
            e.printStackTrace();
         } finally {
             try {
                randomAccessFile.close();
            } catch (IOException e) {
                e.printStackTrace();
             }
         }
     }
 
     public static DownloadTask createDownloadTask(CreateTaskRequest request) {
         DownloadTask task = new DownloadTask();
         URL url = null;
         URL referURL = null;
         boolean resumable = false;
         try {
             url = new URL(request.getUrl());
             referURL = new URL(request.getReferURL());
         } catch (MalformedURLException ignored) {
             LogUtils.error(MetaManager.class, ignored);
         }
         task.setUrl(url);
         task.setReferURL(referURL);
         task.setFolder(new File(request.getFolder()));
         task.setFileName(request.getFilename());
         task.setThreadNumber(new Integer(request.getThreadNumber()).byteValue());
         try {
             resumable = checkResumable(url);
             task.setResumable(resumable);
         } catch (IOException ignored) {
             LogUtils.error(MetaManager.class, ignored);
         }
         task.setTotalLength(getTotalLength(url));
         task.setState((byte) StateEnum.New.ordinal());
         if ( resumable ) {
             int segmentsNumber = (int) ( task.getTotalLength() / Consts.ONE_SEGEMENT );
             task.setSegmentsNumber(segmentsNumber <= 0 ? 1 : segmentsNumber);
         } else {
             task.setSegmentsNumber(1);
         }
         return task;
     }
 
     public static long getTotalLength(URL url) {
         URLConnection openConnection = null;
         int totalLength = 0;
         try {
             openConnection = url.openConnection();
             openConnection.connect();
             totalLength = openConnection.getContentLength();
         } catch (IOException ignored) {
             LogUtils.error(MetaManager.class, ignored);
         }
         return totalLength;
     }
 
     public static DownloadTask deserializeHeadInformation(File file) {
         DownloadTask task = new DownloadTask();
         RandomAccessFile randomAccessFile = null;
         StringBuilder headInfo = new StringBuilder();
         try {
             randomAccessFile = new RandomAccessFile(file, "rw");
             // URL
             //
             randomAccessFile.seek(Consts.URL_SIZE_POSITION);
             final int urlLength = randomAccessFile.readInt();
             byte[] result = new byte[urlLength];
             randomAccessFile.seek(Consts.URL_POSITION);
             randomAccessFile.readFully(result, 0, urlLength);
             headInfo.append(" URL:");
             String urlStr = new String(result, "utf8");
             headInfo.append(urlStr);
             URL url = new URL(urlStr);
             task.setUrl(url);
             // Refer URL
             //
             randomAccessFile.seek(Consts.REFER_URL_SIZE_POSITION);
             final int referUrlLength = randomAccessFile.readInt();
             if ( 0 < referUrlLength ) {
                 result = new byte[referUrlLength];
                 randomAccessFile.seek(Consts.REFER_URL_POSITION);
                 randomAccessFile.readFully(result, 0, referUrlLength);
                 headInfo.append(" Refer URL:");
                 String referUrl = new String(result, "utf8");
                 headInfo.append(referUrl);
                 task.setReferURL(new URL(referUrl));
             }
             // Folder
             //
             result = new byte[Consts.FOLDER_LENGTH];
             randomAccessFile.seek(Consts.FOLDER_POSITION);
             randomAccessFile.readFully(result, 0, Consts.FOLDER_LENGTH);
             headInfo.append(" Folder:");
             headInfo.append(new String(result));
             task.setFolder(new File(new String(result)));
             // File name
             //
             result = new byte[Consts.FILE_NAME_LENGTH];
             randomAccessFile.seek(Consts.FILE_NAME_POSITION);
             randomAccessFile.readFully(result, 0, Consts.FILE_NAME_LENGTH);
             headInfo.append(" File name:");
             headInfo.append(new String(result));
             task.setFileName(new String(result));
             // Total length
             //
             randomAccessFile.seek(Consts.TOTAL_LENGTH_POSITION);
             headInfo.append(" Total Length:");
             headInfo.append(randomAccessFile.readLong());
             task.setTotalLength(randomAccessFile.readLong());
             // Segments number
             //
             randomAccessFile.seek(Consts.SEGMENTS_NUMBER_POSITION);
             headInfo.append(" Segements Number:");
             headInfo.append(randomAccessFile.readInt());
             task.setSegmentsNumber(randomAccessFile.readInt());
             // Resumable
             //
             randomAccessFile.seek(Consts.RESUMABLE_FLAG_POSITION);
             headInfo.append(" Resumable:");
             headInfo.append(randomAccessFile.readByte());
             if ( randomAccessFile.readByte() == 0 ) {
                 task.setResumable(false);
             } else {
                 task.setResumable(true);
             }
             // Thread number
             //
             randomAccessFile.seek(Consts.THREAD_NUMBER_POSITION);
             headInfo.append(" Thread Number:");
             headInfo.append(randomAccessFile.readByte());
             task.setThreadNumber(randomAccessFile.readByte());
             // State
             //
             randomAccessFile.seek(Consts.STATE_POSTION);
             headInfo.append(" State:");
             headInfo.append(randomAccessFile.readByte());
             task.setState(randomAccessFile.readByte());
             System.out.println("Task header Information:");
             System.out.println(headInfo.toString());
         } catch (FileNotFoundException ignored) {
             LogUtils.error(MetaManager.class, ignored);
         } catch (IOException ignored2) {
             System.out.println(ignored2.getMessage());
             LogUtils.error(MetaManager.class, ignored2);
         }
         return task;
     }
 
     private static boolean checkResumable(URL url) throws IOException {
         boolean resumable = false;
         URLConnection openConnection;
         openConnection = url.openConnection();
         openConnection.setRequestProperty("RANGE", "bytes=" + 0 + "-" + 0);
         openConnection.connect();
         String acceptRanges = openConnection.getHeaderField("Accept-Ranges");
         if ( "bytes".equalsIgnoreCase(acceptRanges) ) {
             resumable = true;
         }
         return resumable;
     }
 
     public static void updateTaskState(DownloadTask task, File logFile) {
         try {
             RandomAccessFile raf = new RandomAccessFile(logFile, "rw");
             raf.seek(Consts.STATE_POSTION);
             raf.writeByte(task.getState());
         } catch (Exception e) {
             LogUtils.error(MetaManager.class, e); // ignored the exception
         }
     }
 
     public static void deserializeSegmentsInformation(DownloadTask task, File logFile) throws ErrorException {
         int segments = task.getSegmentsNumber();
         StringBuilder segmentsInformation = new StringBuilder();
         try {
             RandomAccessFile raf = new RandomAccessFile(logFile, "rw");
             long position = 0;
             for ( int i = 0; i < segments; i++ ) {
                 Segment item = new Segment();
                 // Segment Id
                 //
                 position = Consts.FIRST_SEGMENT_POSITION + i * Consts.SEGMENT_LENGTH;
                 raf.seek(position);
                 segmentsInformation.append("Segment Id:");
                 segmentsInformation.append(raf.readInt());
                 item.setId(raf.readInt());
                 // Segment Start bytes
                 //
                 position += Consts.SEGMENT_ID_LENGTH;
                 raf.seek(position);
                 segmentsInformation.append("Segment start bytes:");
                 segmentsInformation.append(raf.readLong());
                 item.setStartBytes(raf.readLong());
                 // Segment End bytes
                 //
                 position += Consts.SEGMENT_START_BYTES_LENGTH;
                 segmentsInformation.append("Segment end bytes:");
                 segmentsInformation.append(raf.readLong());
                 item.setEndBytes(raf.readLong());
                 // Segment state
                 //
                 position += Consts.SEGMENT_END_BYTES_LENGTH;
                 segmentsInformation.append("Segment state:");
                 segmentsInformation.append(raf.readByte());
                 item.setState(raf.readByte());
                 // Segment current bytes
                 //
                 position += Consts.SEGMENT_STATE_LENGTH;
                 segmentsInformation.append("Segment current bytes:");
                 segmentsInformation.append(raf.readLong());
                 item.setCurrentBytes(raf.readLong());
                 task.addSegment(item);
             }
         } catch (FileNotFoundException e) {
             LogUtils.error(MetaManager.class, e);
             throw new ErrorException(ErrorMessage.LOG_FILE_WAS_NOT_FOUND + logFile.getName());
         } catch (IOException e) {
             LogUtils.error(MetaManager.class, e);
             throw new ErrorException(ErrorMessage.LOG_FILE_IS_NOT_COMPLETE + logFile.getName());
         }
         System.out.println("Segements Information:" + segmentsInformation.toString());
     }
 
     public static void serializeForNewState(CreateTaskRequest request, File file) {
         RandomAccessFile randomAccessFile = null;
         try {
             randomAccessFile = new RandomAccessFile(file, "rw");
             byte[] urlBytes = request.getUrl().getBytes("utf8");
             randomAccessFile.seek(Consts.URL_SIZE_POSITION);
             randomAccessFile.writeInt(urlBytes.length);
             randomAccessFile.seek(Consts.URL_POSITION);
             randomAccessFile.write(urlBytes);
             randomAccessFile.seek(Consts.REFER_URL_SIZE_POSITION);
             byte[] referUrlBytes = request.getReferURL().getBytes("utf8");
             randomAccessFile.write(referUrlBytes.length);
             randomAccessFile.seek(Consts.REFER_URL_LENGTH);
             randomAccessFile.write(referUrlBytes);
             randomAccessFile.seek(Consts.FOLDER_POSITION);
             randomAccessFile.writeChars(request.getFolder());
             randomAccessFile.seek(Consts.FILE_NAME_POSITION);
             randomAccessFile.writeChars(request.getFilename());
             randomAccessFile.seek(Consts.THREAD_NUMBER_POSITION);
             randomAccessFile.writeByte(request.getThreadNumber());
             randomAccessFile.seek(Consts.STATE_POSTION);
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
 
     public static void recover() {
         File downloadingFolder = new File("./meta/downloading");
         File finishedFolder = new File("./meta/finished");
         new File("./meta/deleted").mkdirs();
         new File("./meta/paused").mkdirs();
     }
 
     public static void serializeSegmentsInformation(DownloadTask task, File metadataFile) {
         RandomAccessFile raf = null;
         try {
             raf = new RandomAccessFile(metadataFile, "rw");
             for ( Segment segment : task.getSegments() ) {
                 int seq = segment.getId();
                 long position = Consts.FIRST_SEGMENT_POSITION + seq * Consts.SEGMENT_LENGTH;
                 raf.seek(position);
                 raf.writeInt(seq);
                 position += Consts.SEGMENT_ID_LENGTH;
                 raf.writeLong(segment.getStartBytes());
                 position += Consts.SEGMENT_START_BYTES_LENGTH;
                 raf.writeLong(segment.getEndBytes());
                 position += Consts.SEGMENT_END_BYTES_LENGTH;
                 raf.writeByte(StateEnum.Prepared.ordinal());
                 position += Consts.SEGMENT_STATE_LENGTH;
                 raf.writeLong(segment.getStartBytes());
             }
         } catch (FileNotFoundException ignored) {
             LogUtils.error(MetaManager.class, ignored);
         } catch (IOException ignored) {
             LogUtils.error(MetaManager.class, ignored);
         }
     }
 
     public static void computeSegmentsInformation(DownloadTask task) {
         int segmentsNumber = task.getSegmentsNumber();
         long partLength = 0L;
         long totalLength = task.getTotalLength();
         partLength = totalLength / segmentsNumber;
         for ( int i = 0; i < segmentsNumber; i++ ) {
             Segment segment = new Segment();
             segment.setId(i);
             final int seq = i;
             final long finalPartLength = partLength;
             if ( i < segmentsNumber - 1 ) {
                 segment.setStartBytes(finalPartLength * seq);
                 segment.setEndBytes(finalPartLength * ( seq + 1 ) - 1);
                 System.out.println("segment id:" + i);
                 System.out.println("segment start bytes:" + segment.getStartBytes());
                 System.out.println("segment end bytes:" + segment.getEndBytes());
             } else {
                 final long finalTotalLength = totalLength;
                 segment.setStartBytes(finalPartLength * seq);
                 segment.setEndBytes(finalTotalLength - 1);
                 System.out.println("segment id:" + i);
                 System.out.println("segment start bytes:" + segment.getStartBytes());
                 System.out.println("segment end bytes:" + segment.getEndBytes());
             }
             task.addSegment(segment);
         }
     }
 
     private static File copy(File resFile, File objFolderFile) {
         if ( !resFile.exists() || !resFile.isFile() ) throw new IllegalStateException("resFile only accept file existed.");
         if ( !objFolderFile.exists() ) objFolderFile.mkdirs();
         File objFile = new File(objFolderFile.getPath() + File.separator + resFile.getName());
         InputStream ins = null;
         FileOutputStream outs = null;
         try {
             ins = new FileInputStream(resFile);
             outs = new FileOutputStream(objFile);
             byte[] buffer = new byte[1024 * 512];
             int length;
             while ( ( length = ins.read(buffer) ) != -1 ) {
                 outs.write(buffer, 0, length);
             }
         } catch (FileNotFoundException ignored) {
             LogUtils.error(MetaManager.class, ignored);
         } catch (IOException ignored) {
             LogUtils.error(MetaManager.class, ignored);
         } finally {
             try {
                 if ( null != ins ) {
                     ins.close();
                 }
                 if ( null != outs ) {
                     outs.flush();
                     outs.close();
                 }
             } catch (IOException ignored) {
                 LogUtils.error(MetaManager.class, ignored);
             }
         }
         return objFile;
     }
 
     public static File move(File srcFile, File objFolderFile) {
         File targetFile = copy(srcFile, objFolderFile);
         delete(srcFile);
         return targetFile;
     }
 
     private static void delete(File file) {
         if ( !file.exists() ) return;
         if ( file.isFile() ) {
             file.delete();
         } else {
             for ( File f : file.listFiles() ) {
                 delete(f);
             }
             file.delete();
         }
     }
 
     public static void updateTaskState(File metadataFile, StateEnum state) throws FileNotFoundException, IOException {
         RandomAccessFile raf = new RandomAccessFile(metadataFile, "rw");
         raf.seek(Consts.STATE_POSTION);
         raf.writeByte(state.ordinal());
     }
 
     public static void updateSegmentDownloadProgress(File metadataFile, int segmentId, long currentBytes) throws FileNotFoundException, IOException {
         RandomAccessFile raf = new RandomAccessFile(metadataFile, "rw");
         long segmentStartPosition = Consts.FIRST_SEGMENT_POSITION + Consts.SEGMENT_LENGTH * segmentId;
         long currentBytesPosition = segmentStartPosition + Consts.SEGMENT_ID_LENGTH + Consts.SEGMENT_START_BYTES_LENGTH + Consts.SEGMENT_END_BYTES_LENGTH;
         raf.seek(currentBytesPosition);
         raf.writeLong(currentBytes);
     }
 
     public static void initiateMetadataDirs() {
         createFolder("./meta/new");
         createFolder("./meta/prepared");
         createFolder("./meta/started");
         createFolder("./meta/failed");
         createFolder("./meta/finished");
         createFolder("./meta/paused");
     }
 
     private static void createFolder(String path) {
         File folder = new File(path);
         if ( !folder.exists() ) {
             folder.mkdir();
         }
     }
 
     public static List<DownloadTask> load(String root) {
         List<DownloadTask> results = new LinkedList<DownloadTask>();
         List<File> files = new LinkedList<File>();
         files = parseFolder(files, new File(root));
         for ( File file : files ) {
             DownloadTask task = MetaManager.deserializeHeadInformation(file);
             try {
                 MetaManager.deserializeSegmentsInformation(task, file);
             } catch (ErrorException ignored) {
                 LogUtils.error(MetaManager.class, ignored);
             }
             task.toString();
         }
         return results;
     }
 
     private static List<File> parseFolder(List<File> result, File root) {
         File[] listFiles = root.listFiles();
         if ( null == listFiles ) {
             return null;
         }
         for ( File file : listFiles ) {
             if ( file.isFile() && file.getName().contains("_log") ) {
                 result.add(file);
             } else {
                 parseFolder(result, file);
             }
         }
         return result;
     }
 }
