 package com.android.thememanager.widget;
 
 import android.content.Context;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.drawable.AnimationDrawable;
 import android.graphics.drawable.BitmapDrawable;
 import android.util.AttributeSet;
 import android.widget.ImageView;
 
 import java.io.*;
 import java.util.ArrayList;
 import java.util.Enumeration;
 import java.util.List;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 public class BootanimationImageView extends ImageView {
     private List<AnimationPart> mAnimationParts;
     private int mFrameRateMillis;
     private BitmapFactory.Options mOpts;
     private AnimationDrawable mAnimation;
 
     public BootanimationImageView(Context context) {
         this(context, null);
     }
 
     public BootanimationImageView(Context context, AttributeSet attributeSet) {
         this(context, attributeSet, 0);
     }
 
     public BootanimationImageView(Context context, AttributeSet attributeSet, int i) {
         super(context, attributeSet, i);
         mOpts = new BitmapFactory.Options();
         mAnimation = new AnimationDrawable();
     }
 
     public void LoadAnimation(String path) throws IOException {
         ZipFile zip = new ZipFile(path);
         ZipEntry entry = zip.getEntry("desc.txt");
         BufferedReader reader = new BufferedReader(new InputStreamReader(zip.getInputStream(entry)));
         // first line, 3rd column has # of frames per second
         mFrameRateMillis = 1000 / Integer.parseInt(reader.readLine().split(" ")[2]);
         String line = "";
         mAnimationParts = new ArrayList<AnimationPart>();
         while ((line = reader.readLine()) != null) {
             String[] info = line.split(" ");
             if (info.length == 4 && info[0].equals("p")) {
                 int playCount = Integer.getInteger(info[1], 1);
                 int pause = Integer.getInteger(info[2], 0);
                 String name = info[3];
                 if (playCount == 0)
                     playCount = 10;
                 AnimationPart ap = new AnimationPart(playCount, pause, name);
                 mAnimationParts.add(ap);
             }
         }
         reader.close();
 
         mOpts.inSampleSize = 2;
         mOpts.inPreferredConfig = Bitmap.Config.RGB_565;
 
         for (AnimationPart a : mAnimationParts) {
             for (Enumeration<? extends ZipEntry> e = zip.entries();e.hasMoreElements();) {
                 ZipEntry ze = e.nextElement();
                 if (!ze.isDirectory() && ze.getName().contains(a.partName)) {
                     mAnimation.addFrame(loadFrame(zip.getInputStream(ze)), mFrameRateMillis);
                 }
             }
         }
         zip.close();
 
         mAnimation.setOneShot(false);
         setImageDrawable(mAnimation);
         mAnimation.start();
     }
 
     private BitmapDrawable loadFrame(InputStream is) throws FileNotFoundException {
         BitmapDrawable drawable = new BitmapDrawable(BitmapFactory.decodeStream(is, null, mOpts));
         drawable.setAntiAlias(true);
         drawable.setFilterBitmap(true);
         return drawable;
     }
 
     private class AnimationPart {
         public int playCount;
         public int pause;
         String partName;
         List<String> frames;
 
         public AnimationPart(int playCount, int pause, String partName) {
             this.playCount = playCount;
             this.pause = pause;
             this.partName = partName;
             frames = new ArrayList<String>();
         }
 
         public void addFrame(String relativePath) {
             frames.add(relativePath);
         }
     }
 }
