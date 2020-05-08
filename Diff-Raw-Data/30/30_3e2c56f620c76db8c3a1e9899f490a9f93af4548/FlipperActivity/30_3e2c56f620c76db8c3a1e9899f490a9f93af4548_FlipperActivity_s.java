 package cz.minig;
 
 import android.app.Activity;
 import android.app.AlertDialog;
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.os.Bundle;
 import android.view.Gravity;
 import android.view.Menu;
 import android.view.MenuItem;
 import android.view.MenuItem.OnMenuItemClickListener;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.LinearLayout;
 import android.widget.LinearLayout.LayoutParams;
 
 public class FlipperActivity extends Activity implements OnClickListener {
 
     private int cols = 4;
     private int rows = 3;
     private LinearLayout[] rowLayouts;
     private Flipper[] flippers;
     private int[][] flips;
     private Bitmap picture;
     private AlertDialog finishDialog;
 
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         // Create central layout
         LinearLayout mainLayout = new LinearLayout(this);
         mainLayout.setOrientation(LinearLayout.VERTICAL);
         mainLayout.setGravity(Gravity.FILL);
         setContentView(mainLayout);
         // Generate row layouts
         rowLayouts = new LinearLayout[rows];
         for (int i = 0; i < rowLayouts.length; i++) {
             rowLayouts[i] = new LinearLayout(this);
             rowLayouts[i].setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT, 1));
             rowLayouts[i].setGravity(Gravity.FILL);
             mainLayout.addView(rowLayouts[i]);
         }
         // Prepare image drawable
         picture = BitmapFactory.decodeResource(getResources(), R.drawable.flip_image_01);
         // Prepare buttons
         flippers = new Flipper[rows * cols];
         for (int i = 0; i < flippers.length; i++) {
             flippers[i] = new Flipper(this);
             flippers[i].setPosition(rows, cols, i);
             flippers[i].setPicture(picture);
             flippers[i].setOnClickListener(this);
             flippers[i].setLayoutParams(new LayoutParams(LayoutParams.WRAP_CONTENT, LayoutParams.MATCH_PARENT, 1));
             rowLayouts[i / cols].addView(flippers[i]);
         }
         // Prepare finish dialog
         finishDialog = new AlertDialog.Builder(this).setMessage(R.string.finish_msg).create();
         // Start the game
         restart();
     }
 
     private void generateFlips() {
         flips = new int[flippers.length][];
         for (int index = 0; index < flips.length; index++) {
             // How many tiles will be flipped
             int count = 1;
             while (Math.random() < 0.6 && count < flippers.length - 1) {
                 count++;
             }
             // Generate permutation field
             int[] perm = new int[flippers.length];
             for (int i = 0; i < perm.length; i++) {
                 perm[i] = i;
             }
             // Generate tile indexes
             flips[index] = new int[count];
             for (int i = 0; i < flips[index].length; i++) {
                 int trans = (int) (Math.random() * (flippers.length - i)) + i;
                 flips[index][i] = perm[trans];
                 perm[trans] = perm[i];
             }
         }
     }
 
     private void randomizeFlippers() {
         int min = 5;
         while (min > 0 || Math.random() < 0.8) {
             min--;
            onClick(flippers[(int) (Math.random() * flippers.length)]);
         }
     }
 
     @Override
     public void onClick(View v) {
        Flipper flipper = ((Flipper) v);
         // Do the flipping
        for (int i = 0; i < flips[flipper.getIndex()].length; i++) {
            flippers[flips[flipper.getIndex()][i]].reverseState();
        }
         // Check for finish state
         boolean finished = true;
         for (int i = 0; i < flippers.length; i++) {
             if (flippers[i].isFlipped()) {
                 finished = false;
                 break;
             }
         }
         if (finished) {
             finishDialog.show();
         }
     }
 
     private void restart() {
         for (int i = 0; i < flippers.length; i++) {
             flippers[i].resetState();
         }
         generateFlips();
         randomizeFlippers();
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         getMenuInflater().inflate(R.menu.activity_flipper, menu);
         menu.findItem(R.id.menu_restart).setOnMenuItemClickListener(new OnMenuItemClickListener() {
             @Override
             public boolean onMenuItemClick(MenuItem item) {
                 restart();
                 return true;
             }
         });
         return true;
     }
 
 }
