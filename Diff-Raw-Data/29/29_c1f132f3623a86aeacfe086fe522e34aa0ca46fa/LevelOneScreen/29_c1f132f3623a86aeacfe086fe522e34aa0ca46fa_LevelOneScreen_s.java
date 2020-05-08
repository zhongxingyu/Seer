 package cs2114.speedrider1;
 
 import java.nio.charset.Charset;
 import java.nio.MappedByteBuffer;
 import java.nio.channels.FileChannel;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.FileOutputStream;
 import android.content.Context;
 import android.graphics.Canvas;
 import android.view.ScaleGestureDetector;
 import android.view.MotionEvent;
 import android.view.MenuItem;
 import android.view.Menu;
 import android.view.MenuInflater;
 import cs2114.speedrider1.DrawableLine;
 import cs2114.speedrider1.Goal;
 import cs2114.speedrider1.Ground;
 import cs2114.speedrider1.Rider;
 import sofia.graphics.LineShape;
 import sofia.app.ShapeScreen;
 import sofia.graphics.Color;
 
 // -------------------------------------------------------------------------
 /**
  * Level One Screen.
  *
  * @author Chris Conley
  * @author Dan Moore
  * @author Harjas Singh
  * @version 2013.04.24
  */
 public class LevelOneScreen
     extends ShapeScreen
 // implements LevelInterface
 {
     private Rider                rider;
     private Goal                 goal;
     private StopWatch            timer;
 
     private float                x1;
     private float                y1;
     // keeps track of whether or not the player is drawing, erasing, adding
     // a speed booster, or started the animation
     private boolean              draw;
     private boolean              erase;
     private boolean              booster;
     private boolean              started;
 
     // listener for pinch zoom
     private ScaleGestureDetector mScaleDetector;
     private float                mScaleFactor = 1.f;
 
     private String               FILENAME;
 
 
     // ~ Public methods ........................................................
     private class ScaleListener
         extends ScaleGestureDetector.SimpleOnScaleGestureListener
     {
 
         @Override
         public boolean onScale(ScaleGestureDetector detector)
         {
             mScaleFactor = detector.getScaleFactor();
 
             // Don't let the object get too small or too large.
             mScaleFactor = Math.max(0.1f, Math.min(mScaleFactor, 5.0f));
 
             return true;
         }
     }
 
 
     /**
      * Initializes the state of the screen: its background color, the coordinate
      * system, gravity, and the shapes in the field. Automatically defines the
      * amount of line segments that can be drawn as 500
      */
     public void initialize()
     {
         draw = false;
         booster = false;
         started = false;
         timer = new StopWatch();
 
         mScaleDetector =
             new ScaleGestureDetector(this.getBaseContext(), new ScaleListener());
         enableScaleGestures();
 
         BackgroundPaper back =
             new BackgroundPaper(0, 0, getWidth(), getHeight());
         back.setSensor(true);
         add(back);
 
         // create bounds so the player does not fall off the screen
         Ground bound1 = new Ground(0, 0, this.getWidth(), 0);
         this.add(bound1);
 
         Ground bound2 = new Ground(0, 0, 0, this.getHeight());
         this.add(bound2);
 
         Ground bound3 =
             new Ground(0, this.getHeight(), this.getWidth(), this.getHeight());
         this.add(bound3);
 
         Ground bound4 =
             new Ground(this.getWidth(), 0, this.getWidth(), this.getHeight());
         this.add(bound4);
 
         // create a goal for the level
         goal = new Goal(this.getWidth() - 50, this.getHeight() - 50, 30);
         this.add(goal);
 
         // set the gravity level for the course
         this.setGravity(0, 20f);
 
         // create a new rider
         rider = new Rider(10, 10);
         this.add(rider);
         rider.setGravityScale(0);
 
         Shooter shoot =
             new Shooter(
                 getHeight() / 2,
                 getWidth() / 2,
                 getHeight() / 2 + 10,
                 getWidth() / 2 + 10,
                 true);
         add(shoot);
 
     }
 
 
     /**
      * finish the rider
      */
     public void afterInitialize()
     {
         rider.finishRider();
         FILENAME = "listOfTimes";
     }
 
 
     /**
      * touch down for pinch zoom
      *
      * @param ev
      *            for multi-touch
      */
     public void onTouchDown(MotionEvent ev)
     {
         mScaleDetector.onTouchEvent(ev);
 
     }
 
 
     /**
      * Place a description of your method here.
      *
      * @param canvas
      *            to be drawn on
      */
     public void onDraw(Canvas canvas)
     {
         getShapeView().draw(canvas);
 
         canvas.save();
         canvas.scale(mScaleFactor, mScaleFactor);
     }
 
 
     public boolean onCreateOptionsMenu(Menu menu)
     {
         MenuInflater inflater = getMenuInflater();
         inflater.inflate(R.menu.options_menu, menu);
         return true;
     }
 
 
     public boolean onOptionsItemSelected(MenuItem item)
     {
         switch (item.getItemId())
         {
             case R.id.draw:
                 this.draw();
                 return true;
             case R.id.booster:
                 this.speedBoost();
                 return true;
             case R.id.erase:
                 this.erase();
                 return true;
             case R.id.start:
                 this.start();
             case R.id.undo:
                 this.getScores();
             default:
                 return super.onOptionsItemSelected(item);
         }
     }
 
 
     /**
      * create a line segment when player touches down
      *
      * @param newx1
      *            the first x location
      * @param newy1
      *            the first y location
      */
     public void onTouchDown(float newx1, float newy1)
     {
         this.x1 = newx1;
         this.y1 = newy1;
 
         // if booster is true add a speed booster at location
         if (booster == true)
         {
             SpeedBooster boost = new SpeedBooster(newx1, newy1);
             this.add(boost);
         }
 
         Rider rider1 =
             getShapes().locatedAt(newx1, newy1).withClass(Rider.class).front();
 
         // make sure a rider was found to start
         if (rider1 != null)
         {
             this.start();
             timer.start();
         }
 
         if (rider.getRemoved())
         {
             timer.stop();
             String time =
                 String.valueOf(timer.getElapsedTimeSecs() + " seconds\n");
            byte[] timeInBytes = time.getBytes();
             FileOutputStream fos;
             try
             {
                 fos = openFileOutput(FILENAME, Context.MODE_PRIVATE);
                fos.write(timeInBytes);
                 fos.close();
             }
             catch (IOException e)
             {
                 e.printStackTrace();
             }
             boolean x = true;
             this.finish(x);
 
         }
     }
 
 
     /**
      * When touch is released, the x and y coordinates at the end of the line
      * are drawn
      *
      * @param newX
      *            the second x location
      * @param newY
      *            the second y location
      */
     public void onTouchMove(float newX, float newY)
     {
         this.processTouch(x1, y1, newX, newY);
         x1 = newX;
         y1 = newY;
     }
 
 
     /**
      * when a user touches screen create line segment or erase a line segment.
      * Only creates line if user has not used up all of the available segments
      * or draw is true. If erase is true then line segments will be erased
      *
      * @param newx1
      *            the first x location
      * @param newy1
      *            the first y location
      * @param newx2
      *            the second x location
      * @param newy2
      *            the second y location
      */
     public void processTouch(float newx1, float newy1, float newx2, float newy2)
     {
         // if draw is true create lines
         if (draw == true)
         {
             DrawableLine segment = new DrawableLine(newx1, newy1, newx2, newy2);
             segment.setColor(Color.black);
             this.add(segment);
         }
         // if draw is false erase lines
         else if (erase == true)
         {
             // get line at location
             LineShape segment =
                 getShapes().intersecting(newx1, newy1, newx2, newy2)
                     .withClass(LineShape.class).front();
 
             // get booster at location
             SpeedBooster booster1 =
                 getShapes().locatedAt(newx1, newy1)
                     .withClass(SpeedBooster.class).front();
 
             // make sure an item was found to remove
             if (segment != null)
             {
                 segment.remove();
             }
 
             // make sure booster is present
             if (booster1 != null)
             {
                 booster1.remove();
             }
         }
     }
 
 
     /**
      * set draw field to true when draw button is clicked. set all others to
      * false
      */
     public void draw()
     {
         draw = true;
         booster = false;
         erase = false;
     }
 
 
     /**
      * set erase field to true when erase button is clicked. set all others to
      * false
      */
     public void erase()
     {
         draw = false;
         booster = false;
         erase = true;
     }
 
 
     /**
      * set booster field to true when booster button is clicked. set all others
      * to false
      */
     public void speedBoost()
     {
         draw = false;
         booster = true;
         erase = false;
     }
 
 
     /**
      * This lets the player control when the animation starts
      */
     public void start()
     {
         while (!started)
         {
             // apply a force to get the rider moving
             rider.setGravityScale(1);
             rider.applyLinearImpulse(0, 20000);
             started = true;
         }
     }
 
 
     public void getScores()
     {
         FileInputStream fis;
         String result = "";
         try
         {
             fis = openFileInput(FILENAME);
             FileChannel fc = fis.getChannel();
             MappedByteBuffer bb =
                 fc.map(FileChannel.MapMode.READ_ONLY, 0, fc.size());
             result = Charset.defaultCharset().decode(bb).toString();
             fis.close();
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
 
         showAlertDialog("Previous Times", result);
     }
 }
