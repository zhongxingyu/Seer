 package cn.cameron;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 
 /**
  * Created by IntelliJ IDEA.
  * User: Esther
  * Date: 12-1-6
  * Time: 下午9:42
  * To change this template use File | Settings | File Templates.
  */
 public class App {
 
     private final static String slashOrBackSlash = "/";
     private final static Encoder encoder = new Encoder();
 
     public static void main(String[] args) {
        if (args.length == 0) {
             System.out.println("Please specify path!");
             return;
         }
        System.out.println(args.length);
         File rootPath = new File(args[0]);
 
         if (!rootPath.exists()) {
            System.out.println("Path: " + args[0] + " doesn't exist!");
             return;
         }
 
         System.out.println("Starting generating...");
         doGenerate(rootPath);
         System.out.println("Done!");
     }
 
     public static void doGenerate(File root) {
         if (root.exists() && root.isDirectory()) {
             if (containsSubFolder(root)) {
                 for (File eachPath : root.listFiles(new FolderFilter())) {
                     doGenerate(eachPath);
                 }
 
                 writePlaylistForRoot(root);
             } else {
                 if (!containsPlaylist(root)) {
                     writePlaylistForLeaf(root);
                 }
             }
         }
     }
 
     public static void writePlaylistForLeaf(File path) {
         File playlist = new File(path.getAbsolutePath() + slashOrBackSlash + path.getName() + "_generated.m3u");
         Writer out = null;
         try {
             out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(playlist), "UTF8"));
             for (File song : path.listFiles(new MusicFileFilter())) {
                 out.append(encoder.toUtf8String(song.getName())).append("\r\n");
             }
             out.flush();
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } finally {
             if (out != null) {
                 try {
                     out.close();
                 } catch (IOException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
             }
         }
 
 
     }
 
     public static void writePlaylistForRoot(File path) {
         File playlist = new File(path.getAbsolutePath() + slashOrBackSlash + path.getName() + "_generated.m3u");
         Writer out = null;
 
         try {
             out = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(playlist), "UTF8"));
             //add all songs in the current folder
             for (File song : path.listFiles(new MusicFileFilter())) {
                 out.append(encoder.toUtf8String(song.getName())).append("\r\n");
             }
 
             //add all songs in sub folders, use relative path
             for (File subFolder : path.listFiles(new FolderFilter())) {
                 for (File eachPlaylist : subFolder.listFiles(new PlaylistFileFilter())) {
                     for (String eachSong : readPlaylist(eachPlaylist)) {
                         String line = formatSongPathForRoot(eachSong, subFolder.getName());
                         out.append(line);
                     }
                 }
             }
 
             out.flush();
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } finally {
             if (out != null) {
                 try {
                     out.close();
                 } catch (IOException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
             }
         }
     }
 
     public static boolean containsSubFolder(File path) {
         if (path.exists() && path.isDirectory()) {
             for (File eachChildPath : path.listFiles()) {
                 if (eachChildPath.isDirectory()) {
                     return true;
                 }
             }
             return false;
         }
         return false;
     }
 
     public static boolean containsPlaylist(File path) {
         if (path.exists() && path.isDirectory()) {
             File[] files = path.listFiles(new PlaylistFileFilter());
             List<File> fileList = Arrays.asList(files);
             return !fileList.isEmpty();
         }
         return false;
     }
 
     public static List<String> readPlaylist(File path) {
         List<String> songs = new ArrayList<String>();
         if (!path.exists() || path.isDirectory()) {
             return songs;
         }
 
         BufferedReader in = null;
         try {
             in = new BufferedReader(new InputStreamReader(new FileInputStream(path), "UTF8"));
             String str;
 
             while ((str = in.readLine()) != null) {
                 songs.add(str);
             }
         } catch (IOException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
         } finally {
 
             if (in != null) {
                 try {
                     in.close();
                 } catch (IOException e) {
                     e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
             }
         }
         return songs;
     }
 
     public static String formatSongPathForRoot(String song, String folderName) {
        if (song.startsWith("#EXT")) {
            return "";
        }

        String formattedSong = song.replaceFirst("./", "");
         formattedSong = "." + slashOrBackSlash + folderName + slashOrBackSlash + formattedSong + "\r\n";
         return encoder.toUtf8String(formattedSong);
     }
 
 }
