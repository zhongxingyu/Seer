 package com.asevastyanov.torrentEater.files.process;
 
 import com.chilkatsoft.CkRar;
 import com.chilkatsoft.CkRarEntry;
 import com.asevastyanov.torrentEater.files.FileSearcher;
 import com.asevastyanov.torrentEater.files.model.TorrentFileName;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import java.io.File;
 import java.io.IOException;
 
 import static com.asevastyanov.torrentEater.utils.StringUtils.getFileNameFromPath;
 import static com.asevastyanov.torrentEater.utils.StringUtils.getFileNameFromPathShort;
 import static com.asevastyanov.torrentEater.utils.StringUtils.getRootDir;
 
 public class ArcAssFileProcessor implements FileProcessor {
     private static final Logger logger = LoggerFactory.getLogger(ArcAssFileProcessor.class.getSimpleName());
     private File[] videoFileList;
     private File path;
     private String message;
 
     public void process(File f) throws Exception {
         path = f;
         videoFileList = getVideoList();
 
         UnResult result = unrar();
         if (!result.isSucces()) {
             //result = unzip(); todo make unzip functional
         }
         message = result.getMessage();
         if (message == null) {
             message = "Extract error";
         }
     }
 
     public String getLastMessage() {
         return message;
     }
 
     private UnResult unrar() throws IOException {
         String message = "";
         String output = path.getParent();
         CkRar rar = new CkRar();
         boolean success = rar.Open(path.getPath());
         if (!success) {
             message = "ERROR - Unrar open file error:\n" + rar.lastErrorText();
             return new UnResult(false, message);
         }
 
         for (File videoFile : videoFileList) {
             CkRarEntry resRarEntry = null;
             int resRank = 0;
             TorrentFileName videoFileTorrent = TorrentFileName.parse(videoFile.getName());
 
             for (int i = 0; i < rar.get_NumEntries(); i++) {
                 CkRarEntry rarEntry = rar.GetEntryByIndex(i);
                 if (!rarEntry.get_IsDirectory()) {
                     String assFileName = getFileNameFromPath(rarEntry.filename());
                     TorrentFileName assFileTorrent = TorrentFileName.parse(assFileName);
                     int rank = assFileTorrent.getRankEquals(videoFileTorrent);
                     if (rank > resRank && rank >= TorrentFileName.NEED_RANK) { // name and episode equals
                         resRarEntry = rarEntry;
                     }
                 }
             }
 
             message += "\n" + videoFile.getName() + " - " + unrarFinal(output, videoFile, resRarEntry);
         }
 
         return new UnResult(true, message);
     }
 
     private String unrarFinal(String output, File videoFile, CkRarEntry rarEntry) {
         if (rarEntry == null) {
             return "Matching subtitles not found";
         }
 
         String name = getFileNameFromPathShort(videoFile.getPath());
         String unpackName = output + "\\" + rarEntry.filename();
         String assName = output + "\\" + name + ".ass";
 
         if (new File(assName).exists()) {
             return "WARNING - Unpack " + rarEntry.filename() + ", but file already EXIST: " + name + ".ass";
         }
 
         if (rarEntry.Unrar(output)) {
 
             // rename to video file name
             new File(unpackName).renameTo(new File(assName));
 
             // delete relative arch path
             String tmpDirName = getRootDir(rarEntry.filename());
             if (tmpDirName != null) {
                 new File(output + "\\" + tmpDirName).delete();
             }
 
            return "SUCCESS - Unpacked: " + rarEntry.filename();
         } else {
             return "ERROR - Unrar extract error:\n" + rarEntry.lastErrorText();
         }
     }
 
     private File[] getVideoList() {
         if (videoFileList == null) {
             return new FileSearcher(path.getParent(), ".*\\.avi$|.*\\.mp4$|.*\\.mkv$").search();
         }
         return videoFileList;
     }
 
     class UnResult {
         private boolean succes;
         private String message;
 
         UnResult(boolean succes, String message) {
             this.succes = succes;
             this.message = message;
         }
 
         public boolean isSucces() {
             return succes;
         }
 
         public String getMessage() {
             return message;
         }
     }
 
 }
