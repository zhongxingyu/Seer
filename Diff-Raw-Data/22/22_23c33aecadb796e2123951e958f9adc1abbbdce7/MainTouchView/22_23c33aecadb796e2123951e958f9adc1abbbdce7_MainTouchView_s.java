 package com.dobrynland.secretspad;
 
 import android.app.Activity;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.Canvas;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.graphics.Path;
 import android.view.Gravity;
 import android.widget.ImageView;
 import android.view.MotionEvent;
 import android.widget.Toast;
 import android.graphics.PathMeasure;
 import android.util.DisplayMetrics;
 
 import java.io.*;
 import java.lang.Math;
 import java.util.Random;
 
 public class MainTouchView extends ImageView
 {
     private Paint paint = new Paint();
     private Path path = new Path();
     private Path comp_path = new Path();
     private boolean needCheck = false;
     private int counter = 0;
     private int rad = 50;
     private String pathFile;
     int screen_width;
     int screen_height;
     final Random myRandom;
 
     public MainTouchView(Context context)
     {
         super(context);
 
         paint.setAntiAlias(true);
         paint.setStrokeWidth(6f);
         paint.setColor(Color.BLUE);
         paint.setStyle(Paint.Style.STROKE);
         paint.setStrokeJoin(Paint.Join.ROUND);
 
         DisplayMetrics displaymetrics = new DisplayMetrics();
         ((Activity) getContext()).getWindowManager().getDefaultDisplay().getMetrics(displaymetrics);
         screen_width = displaymetrics.widthPixels;
         screen_height = displaymetrics.heightPixels;
         ToastMsg("Screen size: " + Integer.toString(screen_width) + "x" + Integer.toString(screen_height));
         myRandom = new Random();
         //drawThreeRandomCircles();
        pathFile  =  context.getFilesDir() + "circles.xml";
        File file = getContext().getFileStreamPath(pathFile);
         if(file.exists())
         {
             needCheck = true;
         }
         else
         {
             ToastMsg("Tap triple to create new key");
         }
     }
 
     void drawThreeRandomCircles()
     {
 
         comp_path.reset();
         rad = 40;
         comp_path.addCircle(myRandom.nextInt(screen_width - rad * 2), myRandom.nextInt(screen_height - rad * 2), rad, Path.Direction.CW);
         comp_path.addCircle(myRandom.nextInt (screen_width - (rad - 5) * 2), myRandom.nextInt (screen_height - (rad - 5) * 2), rad + 5, Path.Direction.CW);
         comp_path.addCircle(myRandom.nextInt (screen_width - (rad - 10) * 2),myRandom.nextInt (screen_height -(rad - 10) * 2), rad + 10, Path.Direction.CW);
         /*comp_path.addCircle((int)Math.random() * (screen_width - rad * 2), (int)Math.random() * (screen_height - rad * 2), rad, Path.Direction.CW);
         comp_path.addCircle((int)Math.random() * (screen_width - (rad - 5) * 2), (int)Math.random() * (screen_height - (rad - 5) * 2), rad + 5, Path.Direction.CW);
         comp_path.addCircle((int)Math.random() * (screen_width - (rad - 10) * 2), (int)Math.random() * (screen_height -(rad - 10) * 2), rad + 10, Path.Direction.CW);*/
     }
 
     @Override
     public void onDraw(Canvas canvas)
     {
         super.onDraw(canvas);
         canvas.drawPath(path, paint);
        //canvas.drawPath(comp_path, paint);
     }
 
     @Override
     public boolean onTouchEvent(MotionEvent event)
     {
         float eventX = event.getX();
         float eventY = event.getY();
 
         switch (event.getAction()) {
             case MotionEvent.ACTION_DOWN:
                 if(counter < 3)
                 {
                     path.moveTo(eventX, eventY);
                     path.addCircle(eventX, eventY, rad, Path.Direction.CW);
                     //rad += 5;
                     counter++;
                 }
                 return true;
             case MotionEvent.ACTION_UP:
                 if(counter >= 3)
                 {
                     if(needCheck)
                     {
                         comp_path =  loadPath();
                         if(comparePaths(path, comp_path))
                         {
                             ToastMsg("Auth OK");
                            Context context = getContext();
                            Intent i = new Intent(context, Notepad.class);
                            context.startActivity(i);
                         }
                         else
                         {
                             ToastMsg("Auth NOK");
                             counter = 0;
                             path.reset();
                             //drawThreeRandomCircles();
                         }
                     }
                     else
                     {
                         savePath(path);
                         ToastMsg("New key created");
                     }
                 }
                 break;
             default:
                 return false;
         }
 
         invalidate();
         return true;
     }
 
     public Path loadPath()
     {
         Path path = new Path();
         float x, y;
         try
         {
             FileInputStream fis = new FileInputStream(pathFile);
             DataInputStream dis = new DataInputStream(fis);
             for(int i = 0; i < 3; i++)
             {
                 x = dis.readFloat();
                 y = dis.readFloat();
                 path.addCircle(x, y, rad, Path.Direction.CW);
             }
             dis.close();
         }
         catch (FileNotFoundException e)
         {
             e.printStackTrace();
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
 
 
         return path;
     }
 
     public void savePath(Path cPath)
     {
         PathMeasure pm = new PathMeasure(cPath, false);
         float coords[] = {0f, 0f};
         try
         {
             FileOutputStream fos = new FileOutputStream(pathFile);
             DataOutputStream dos = new DataOutputStream(fos);
             do
             {
                 pm.getPosTan(pm.getLength() * 0.5f, coords, null);
                 dos.writeFloat(coords[0]);
                 dos.writeFloat(coords[1]);
             }
             while(pm.nextContour());
             dos.close();
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
     }
 
     public void ToastMsg(String str)
     {
         Toast msg = Toast.makeText(MainTouchView.this.getContext(), str, Toast.LENGTH_SHORT);
         msg.setGravity(Gravity.BOTTOM, msg.getXOffset() / 2, msg.getYOffset() / 2);
         msg.show();
     }
 
     boolean comparePaths(Path path1, Path path2)
     {
         PathMeasure pm = new PathMeasure(path1, false);
         PathMeasure pm2 = new PathMeasure(path2, false);
         float aCoordinates[] = {0f, 0f};
         float bCoordinates[] = {0f, 0f};
         do
         {
             pm.getPosTan(pm.getLength() * 0.5f, aCoordinates, null);
             pm2.getPosTan(pm2.getLength() * 0.5f, bCoordinates, null);
             double dist = distance(aCoordinates, bCoordinates);
             if(dist > rad / 2)
                 return false;
             pm2.nextContour();
         }
         while(pm.nextContour());
 
         return true;
     }
 
     static float[] computeCentroid(float[] points)
     {
         float centerX = 0;
         float centerY = 0;
         int count = points.length;
         for (int i = 0; i < count; i++) {
             centerX += points[i];
             i++;
             centerY += points[i];
         }
         float[] center = new float[2];
         center[0] = 2 * centerX / count;
         center[1] = 2 * centerY / count;
 
         return center;
     }
 
     public double distance(float[] p, float[] q)
     {
         double dx   = p[0] - q[0];         //horizontal difference
         double dy   = p[1] - q[1];         //vertical difference
         double dist = Math.sqrt( dx*dx + dy*dy ); //distance using Pythagoras theorem
         return dist;
     }
 }
