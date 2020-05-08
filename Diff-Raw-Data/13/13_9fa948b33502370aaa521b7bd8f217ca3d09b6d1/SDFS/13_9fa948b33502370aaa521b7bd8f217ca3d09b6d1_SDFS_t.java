 package filesystem;
 
 import communication.FileIdentifierFactory;
 import membership.Proc;
 import misc.MiscTool;
 import misc.TimeMachine;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PropertyConfigurator;
 
 import java.io.*;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Scanner;
 
 import static communication.Messages.*;
 
 public class SDFS {
 
     private Proc proc;
     private FileList fileList;
 
     private Map<String, Integer> timeStampMap;
     private Map<String, Long> localTimeMap;
     private Map<String, FileState> state;
 
     private static Logger logger = Logger.getLogger(SDFS.class);
     private String rootDirectory;
 
     public SDFS(String rootDirectory) {
         fileList = new FileList();
         timeStampMap = new HashMap<String, Integer>();
         localTimeMap = new HashMap<String, Long>();
         state = new HashMap<String, FileState>();
         this.rootDirectory = rootDirectory;
     }
 
     public void init() {
         File root = new File(rootDirectory);
         if(!root.exists()) {
             if(!root.mkdir()){
                 logger.fatal("Create root directory fails");
                 System.exit(-1);
             }
         } else {
             if(!root.isDirectory()) {
                 logger.fatal("sdfs is not directory!!!");
                 System.exit(-1);
             }
             loadFilesFromRootDirectory();
         }
     }
 
     private void loadFilesFromRootDirectory() {
         File root = new File(rootDirectory);
         File[] files = root.listFiles();
         if(files == null) {
             return;
         }
         for(File f : files) {
             addFileLocally(f);
         }
     }
 
     public File getFile(String fileName) {
         return new File(rootDirectory + fileName);
     }
 
     public File openFile(String fileName) {
         return new File(fileName);
     }
 
     public void setProc(Proc proc) {
         this.proc = proc;
     }
 
     public FileList getFileList() {
         return fileList;
     }
 
     public void addFileLocally(String filePath) {
         File file = new File(filePath);
         if(!file.exists()) {
             return;
         }
         String fileName = file.getName();
         FileIdentifier fileIdentifier = FileIdentifierFactory.generateFileIdentifier(proc.getIdentifier(), fileName);
 
         copyFile(file, rootDirectory + fileName);
         addToFileList(fileIdentifier, proc.getTimeStamp());
     }
 
     public void addFileLocally(File file) {
 
         String fileName = file.getName();
         FileIdentifier fileIdentifier = FileIdentifierFactory.generateFileIdentifier(proc.getIdentifier(), fileName);
 
         copyFile(file, rootDirectory + fileName);
         addToFileList(fileIdentifier, proc.getTimeStamp());
     }
 
     private void copyFile(File sourceFile, String destination) {
         copyFile(sourceFile, new File(destination));
     }
 
     private void copyFile(File sourceFile, File destFile) {
         if(sourceFile.getAbsolutePath().equals(destFile.getAbsolutePath())){
             return;
         }
 
         FileInputStream fis;
 
         try {
             fis = new FileInputStream(sourceFile);
         } catch (FileNotFoundException e) {
             logger.error("open source file error", e);
             return;
         }
 
         FileOutputStream fos;
         try {
             fos = new FileOutputStream(destFile);
         } catch (FileNotFoundException e) {
             logger.error("open dest file error", e);
             return;
         }
 
         int nextByte;
         try {
             while((nextByte = fis.read()) != -1) {
                 fos.write(nextByte);
             }
             fis.close();
             fos.close();
         } catch (IOException e) {
             logger.error("copy error", e);
         }
     }
 
     public void addToFileList(FileIdentifier fileIdentifier, Integer timeStamp) {
        if(fileList.find(fileIdentifier)!=-1){
            return;
        }
         fileList.addFile(fileIdentifier);
         String key = generateKey(fileIdentifier);
         timeStampMap.put(key, timeStamp);
         localTimeMap.put(key, TimeMachine.getTime());
         state.put(key, FileState.Available);
     }
 
     private String generateKey(FileIdentifier identifier) {
         return identifier.getFileStoringProcess().getIP()+":"+
                 identifier.getFileStoringProcess().getPort()+"/" +
                 identifier.getFilepath();
     }
 
     public void addToFileList(String fileName) {
         FileIdentifier fileIdentifier = FileIdentifierFactory.generateFileIdentifier(proc.getIdentifier(), fileName);
         addToFileList(fileIdentifier, proc.getTimeStamp());
     }
 
     public static void main(String[] args) {
         PropertyConfigurator.configure("log4j.properties");
         Scanner in = new Scanner(System.in);
 //        Proc proc = new Proc(MiscTool.inputPortNumber(in));
         Proc proc = new Proc(20000);
         proc.init();
 
         SDFS sdfs = new SDFS("sdfs/");
         sdfs.setProc(proc);
         sdfs.init();
 
         while(true) {
             String filename = MiscTool.inputFileName(in);
             sdfs.addFileLocally(filename);
         }
     }
 
     public Integer getFileTimeStamp(FileIdentifier fileIdentifier) {
         String key = generateKey(fileIdentifier);
         return timeStampMap.get(key);
     }
 
     public Long getFileLocalTime(FileIdentifier fileIdentifier) {
         String key = generateKey(fileIdentifier);
         return localTimeMap.get(key);
     }
 
     public boolean isAvailable(FileIdentifier fileIdentifier) {
         String key = generateKey(fileIdentifier);
         return state.containsKey(key) && state.get(key) == FileState.Available;
     }
 
     public void updateFileListEntry(FileIdentifier identifier, Integer timeStamp) {
         synchronized (this) {
             String key = generateKey(identifier);
             timeStampMap.put(key, timeStamp);
             localTimeMap.put(key, TimeMachine.getTime());
         }
     }
 }
