 package com.dedaulus.cinematty.framework;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Pair;
 import com.dedaulus.cinematty.framework.tools.DefaultComparator;
 import com.dedaulus.cinematty.framework.tools.PictureReceiver;
 import com.dedaulus.cinematty.framework.tools.PictureType;
 import com.dedaulus.cinematty.framework.tools.UniqueSortedList;
 
 import java.io.*;
 import java.net.URL;
 import java.net.URLConnection;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Queue;
 
 /**
  * User: Dedaulus
  * Date: 08.05.11
  * Time: 18:40
  */
 public class HttpPictureRetriever implements PictureRetriever, Runnable {
 
     private class PictureWrapper implements Comparable<PictureWrapper> {
         public String picId = null;
         public Integer pictureType = 0;
         public Bitmap picture = null;
 
         public PictureWrapper(String picId, int pictureType, Bitmap picture) {
             this.picId = picId;
             this.pictureType = pictureType;
             this.picture = picture;
         }
 
         public int compareTo(PictureWrapper o) {
             if (this == o) return 0;
             else {
                 int cmp = picId.compareTo(o.picId);
                 if (cmp == 0) {
                     return pictureType.compareTo(o.pictureType);
                 } else if (cmp > 0) return 1;
                 else return -1;
             }
         }
     }
 
     private static final int APROX_PICS_COUNT = 80; // This is a nearly count of moscow movies at once
 
     Context mContext;
     String mRemotePictureFolder;
     String mLocalPictureFolder;
 
     Queue<Pair<Pair<String, Integer>, PictureReceiver>> mTaskQueue = new LinkedList<Pair<Pair<String, Integer>, PictureReceiver>>();
     List<PictureWrapper> mReadyPictures = new UniqueSortedList<PictureWrapper>(APROX_PICS_COUNT, new DefaultComparator<PictureWrapper>());
 
     public HttpPictureRetriever(Context context, String remotePictureFolder, String localPictureFolder) {
         mContext = context;
         mRemotePictureFolder = remotePictureFolder;
         mLocalPictureFolder = localPictureFolder;
 
         restoreState();
 
         new Thread(this).start();
     }
 
     public void setRemotePictureFolder(String remotePictureFolder) {
         synchronized (mRemotePictureFolder) {
             mRemotePictureFolder = remotePictureFolder;
         }
     }
 
     public boolean hasPicture(String picId, int pictureType) {
         synchronized (mReadyPictures) {
            return mReadyPictures.contains(new Pair<String, Integer>(picId, pictureType));
         }
     }
 
     public Bitmap getPicture(String picId, int pictureType) {
         if (pictureType == PictureType.ORIGINAL_WITH_URL) {
             picId = getPicIdFromFullPath(picId);
         }
 
         synchronized (mReadyPictures) {
             int id = mReadyPictures.indexOf(new PictureWrapper(picId, pictureType, null));
             if (id == -1) return null;
 
             Bitmap picture = mReadyPictures.get(id).picture;
             if (picture != null) return picture;
             else {
                 picture = loadPicture(picId, pictureType);
                 if (picture != null) return picture;
                 else {
                     mReadyPictures.remove(id);
                     return null;
                 }
             }
         }
     }
 
     public synchronized void addRequest(String picId, int pictureType, PictureReceiver receiver) {
         synchronized (mTaskQueue) {
             mTaskQueue.add(new Pair<Pair<String, Integer>, PictureReceiver>(new Pair<String, Integer>(picId, pictureType), receiver));
             notify();
         }
     }
 
     public synchronized void addRequest(String remotePicturePath, PictureReceiver receiver) {
         synchronized (mTaskQueue) {
             mTaskQueue.add(new Pair<Pair<String, Integer>, PictureReceiver>(new Pair<String, Integer>(remotePicturePath, PictureType.ORIGINAL_WITH_URL), receiver));
             notify();
         }
     }
 
     public Bitmap downloadPicture(String picId, int pictureType) {
         Bitmap picture = loadPicture(picId, pictureType);
         if (picture != null) return picture;
 
         String from = getRemotePicturePath(picId, pictureType);
         File to = getLocalPicturePath(picId, pictureType);
         if (downloadPicture(from, to)) {
             picture = loadPicture(picId, pictureType);
             synchronized (mReadyPictures) {
                 mReadyPictures.add(new PictureWrapper(picId, pictureType, null));
             }
             return picture;
 
         } else return null;
     }
 
     public Bitmap downloadPicture(String remotePicturePath) {
         String picId = getPicIdFromFullPath(remotePicturePath);
         Bitmap picture = loadPicture(picId, PictureType.ORIGINAL);
         if (picture != null) return picture;
 
         File to = getLocalPicturePath(picId, PictureType.ORIGINAL);
         if (downloadPicture(remotePicturePath, to)) {
             picture = loadPicture(picId, PictureType.ORIGINAL);
             synchronized (mReadyPictures) {
                 mReadyPictures.add(new PictureWrapper(picId, PictureType.ORIGINAL_WITH_URL, picture));
             }
             return picture;
 
         } else return null;
     }
 
     private void restoreState() {
         String[] fileNames = mContext.getCacheDir().list(new FilenameFilter() {
             public boolean accept(File dir, String name) {
                 if (name.startsWith("pic_")) return true;
                 return false;
             }
         });
 
         for (String name : fileNames) {
             int picIdStart = "pic_".length();
             int picIdEnd = name.indexOf("_", picIdStart + 1);
 
             String picId = name.substring(picIdStart, picIdEnd);
             int pictureType = postfixToPictureType(name.substring(picIdEnd + 1, name.indexOf(".", picIdEnd)));
 
             Bitmap picture = null;
             if (pictureType == PictureType.LIST_BIG
                     || pictureType == PictureType.LIST_MEDIUM
                     || pictureType == PictureType.LIST_SMALL) {
                 picture = loadPicture(picId, pictureType);
             }
 
             mReadyPictures.add(new PictureWrapper(picId, pictureType, picture));
         }
     }
 
     private int postfixToPictureType(String postfix) {
         if (postfix.compareTo("h") == 0)      return PictureType.LIST_BIG;
         else if (postfix.compareTo("m") == 0) return PictureType.LIST_MEDIUM;
         else if (postfix.compareTo("l") == 0) return PictureType.LIST_SMALL;
         else return PictureType.ORIGINAL;
     }
 
     private String pictureTypeToPostfix(int pictureType) {
         String postfix = null;
         switch (pictureType) {
         case PictureType.LIST_SMALL:
             postfix = "s";
             break;
         case PictureType.LIST_MEDIUM:
             postfix = "m";
             break;
         case PictureType.LIST_BIG:
             postfix = "h";
             break;
         case PictureType.ORIGINAL:
         case PictureType.ORIGINAL_WITH_URL:
         default:
             postfix = "";
             break;
         }
 
         return postfix;
     }
 
     private String getRemotePicturePath(String picId, int pictureType) {
         if (pictureType != PictureType.ORIGINAL_WITH_URL) {
             String name = "pic_" + pictureTypeToPostfix(pictureType) + ".jpg";
 
             synchronized (mRemotePictureFolder) {
                 StringBuffer buffer = new StringBuffer(mRemotePictureFolder.length() + 1 + picId.length() + 1 + name.length());
                 return (buffer.append(mRemotePictureFolder).append("/").append(picId).append("/").append(name)).toString();
             }
         } else {
             return picId;
         }
     }
 
     private File getLocalPicturePath(String picId, int pictureType) {
         File path = mContext.getCacheDir();
         if (pictureType == PictureType.ORIGINAL_WITH_URL) {
             picId = getPicIdFromFullPath(picId);
         }
         return new File(path, "pic_" + picId + "_" + pictureTypeToPostfix(pictureType) + ".jpg");
     }
 
     private boolean downloadPicture(String loadFrom, File saveTo) {
         try {
             URL url = new URL(loadFrom);
             URLConnection connection = url.openConnection();
             connection.connect();
 
             InputStream input = new BufferedInputStream(url.openStream());
             OutputStream output = new FileOutputStream(saveTo);
 
             byte data[] = new byte[1024];
 
             int count = 0;
             while ((count = input.read(data)) != -1) {
                 output.write(data, 0, count);
             }
 
             output.flush();
             output.close();
             input.close();
         } catch (Exception e) {
             return false;
         }
 
         return true;
     }
 
 
 
     private Bitmap loadPicture(String picId, int pictureType) {
         File picturePath = getLocalPicturePath(picId, pictureType);
         try {
             InputStream is = new FileInputStream(picturePath);
             return BitmapFactory.decodeStream(is);
         } catch (FileNotFoundException e) {
             return null;
         }
     }
 
     public void run() {
         while (true) {
             Pair<Pair<String, Integer>, PictureReceiver> task = null;
             synchronized (mTaskQueue) {
                 task = mTaskQueue.poll();
             }
 
             if (task == null) {
                 try {
                     synchronized (this) {
                         wait();
                     }
                 } catch (InterruptedException e) {
                     // TODO: shit, but what to do?
                 }
             } else {
                 String picId = task.first.first;
                 int pictureType = task.first.second;
                 PictureReceiver receiver = task.second;
 
                 String from = getRemotePicturePath(picId, pictureType);
                 File to = getLocalPicturePath(picId, pictureType);
 
                 if (hasPicture(picId, pictureType)) {
                     if (receiver != null) {
                         receiver.onPictureReceive(picId, pictureType, true);
                     }
                 } else {
                     boolean downloaded = downloadPicture(from, to);
                     if (downloaded) {
                         Bitmap picture = null;
                         if (pictureType == PictureType.ORIGINAL_WITH_URL
                                 || pictureType == PictureType.LIST_BIG
                                 || pictureType == PictureType.LIST_MEDIUM
                                 || pictureType == PictureType.LIST_SMALL) {
                             picture = loadPicture(picId, pictureType);
                         }
 
                         if (pictureType == PictureType.ORIGINAL_WITH_URL) {
                             picId = getPicIdFromFullPath(picId);
                         }
 
                         synchronized (mReadyPictures) {
                             mReadyPictures.add(new PictureWrapper(picId, pictureType, picture));
                         }
                     }
 
                     if (receiver != null) {
                         receiver.onPictureReceive(picId, pictureType, downloaded);
                     }
                 }
             }
         }
     }
 
     private String getPicIdFromFullPath(String fullPath) {
         return fullPath.substring(fullPath.lastIndexOf("/") + 1, fullPath.lastIndexOf("."));
     }
 }
