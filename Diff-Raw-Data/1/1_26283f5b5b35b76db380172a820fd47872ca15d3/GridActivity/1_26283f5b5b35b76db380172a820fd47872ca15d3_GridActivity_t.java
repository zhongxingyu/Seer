 package ch.unibe.scg.team3.wordfinder;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import android.os.Bundle;
 import android.annotation.SuppressLint;
 import android.app.Activity;
 import android.graphics.Color;
 import android.graphics.Rect;
 import android.view.Menu;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.OnTouchListener;
 import android.widget.TableLayout;
 
 /**
 @author nfaerber
 */
 
 @SuppressLint("NewApi") public class GridActivity extends Activity implements OnTouchListener {
 
 	List<View> walked;
 	GameManager manager;
     @Override
     protected void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_grid);
         findViewById(R.id.tableBoard).setOnTouchListener(this);
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu) {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.grid, menu);
         return true;
     }
     
     public Rect calculateBoundary(int id) {
     	Rect re = new Rect();
     	findViewById(id).getGlobalVisibleRect(re);
     	return re;
     }
     
     @Override
     public boolean onTouch(View v, MotionEvent event) {  
     	
     	boolean isInList = false;
     	
     	switch (event.getAction()) {
     	case MotionEvent.ACTION_DOWN:
     		this.walked = new ArrayList<View>();
     	case MotionEvent.ACTION_MOVE:
     		break;
     	case MotionEvent.ACTION_UP:
     		System.out.println(this.walked);
     		for (int i=0;i<this.walked.size();i++) {
     			this.walked.get(i).setBackgroundColor(Color.WHITE);
     		}
     		break;
     	default:
     		return false;
     	}
     	
         TableLayout layout = (TableLayout)v;
         for(int i =0; i< layout.getChildCount(); i++)
         {
             View rview = layout.getChildAt(i);
             Rect rrect = new Rect(rview.getLeft(), rview.getTop(), rview.getRight(), rview.getBottom());
             if(rrect.contains((int)event.getX(), (int)event.getY()))
             {
                 SquareRow srow = (SquareRow)rview;
                 for(int j =0; j< srow.getChildCount(); j++)
                 {
                     View fview = srow.getChildAt(j);
                     Rect frect;
                     if (i==0) {
                     	frect = new Rect(fview.getLeft()+10, fview.getTop()+10, fview.getRight()-10, fview.getBottom()-10);
                     } else {
                     	frect = new Rect(fview.getLeft()+10, fview.getTop()+rview.getTop()+10, fview.getRight()-10, fview.getBottom()+rview.getBottom()-10);
                     }
                     if(frect.contains((int)event.getX(), (int)event.getY()))
                     {
                     	for (int k=0; k<this.walked.size();k++) {
                     		if (this.walked.get(k).equals(fview)) {
                     			isInList = true;
                     		}
                     	}
                 		if (!isInList) {
                 			this.walked.add(fview);
                 			fview.setBackgroundColor(Color.GRAY);
                 		}
                 		break;
                     }
                 }
                 break;
             }
         }
         return true;
     }
 }
 
