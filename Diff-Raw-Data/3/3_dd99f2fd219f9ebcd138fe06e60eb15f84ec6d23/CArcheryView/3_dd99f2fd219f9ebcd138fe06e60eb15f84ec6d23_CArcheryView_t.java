 package com.example.archery.archeryView;
 
 import java.io.*;
 import java.util.Vector;
 import android.content.Context;
 import android.graphics.Color;
 import android.graphics.Paint;
 import android.preference.PreferenceManager;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 import com.example.archery.CArrow;
 import com.example.archery.CShot;
 import com.example.archery.database.CMySQLiteOpenHelper;
 import com.example.archery.target.CRing;
 import com.example.archery.target.CTarget;
 import com.example.archery.target.CTargetView;
 
 public  class CArcheryView extends LinearLayout {
 	public Paint pen;
 	public int numberOfSeries;
     public int arrowsInSeries;
     private Context context;
     private CDistance currentDistance = null;
     private CDistance previousDistance = null;
     
 	public CArcheryView(Context context,int a, int b) {
 		super(context);
         this.context = context;
 		pen = new Paint();
 		numberOfSeries = a;
 		arrowsInSeries = b;
 
        //context.deleteFile("Archery");
         try
         {
             ObjectOutputStream oos =
                     new ObjectOutputStream(context.openFileOutput("Targets",Context.MODE_PRIVATE));
             Vector<CRing> rings = new Vector<CRing>();
             rings.add(new CRing(10,1, Color.YELLOW));
             rings.add(new CRing(9,2,Color.YELLOW));
             rings.add(new CRing(8,3,Color.RED));
             rings.add(new CRing(7,4,Color.RED));
             rings.add(new CRing(6,5,Color.BLUE));
             rings.add(new CRing(5,6,Color.BLUE));
             rings.add(new CRing(0,7,Color.WHITE));
             CTarget target = new CTarget("default_target",rings);
             CTarget targets [] = {target};
             oos.writeObject(targets);
             oos.close();
         }
         catch (Exception e)
         {
             Toast toast = Toast.makeText(context, e.getMessage(), 3000);
             toast.show();
         }
         try
         {
             ObjectOutputStream oos =
                     new ObjectOutputStream(context.openFileOutput("Arrows",Context.MODE_PRIVATE));
             CArrow arrows [] = {new CArrow("defaultArrow","люминь",0.02f)};
             oos.writeObject(arrows);
             oos.close();
         }
         catch (Exception e)
         {
             Toast toast = Toast.makeText(context, e.getMessage(), 3000);
             toast.show();
         }
         this.setOrientation(VERTICAL);
         LinearLayout layout = new LinearLayout(context);
         layout.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 0,0.9f));
         layout.setOrientation(HORIZONTAL);
         CTargetView mTargetView = new CTargetView(context,this);
         mTargetView.setLayoutParams(new LayoutParams(0, LayoutParams.MATCH_PARENT,15f));
         layout.addView(mTargetView);
         СSeriesCounterView mSeriesCounterView = new СSeriesCounterView(context,this);
         mSeriesCounterView.setLayoutParams(new LayoutParams(0, LayoutParams.MATCH_PARENT,1f));
         layout.addView(mSeriesCounterView);
         this.addView(layout);
         CCurrentSeriesView mCurrentSeriesView = new CCurrentSeriesView(context,this);
         mCurrentSeriesView.setLayoutParams(new LayoutParams(LayoutParams.MATCH_PARENT, 0, 0.1f));
         this.addView(mCurrentSeriesView);
     	}
 
     public void saveDistances()  {
         if (currentDistance!=null)
         {
         if (!currentDistance.isEmpty())
         {
             CMySQLiteOpenHelper helper = new CMySQLiteOpenHelper(context);
             try
             {
             helper.addDistance(currentDistance);
             }
             catch (Exception e)
             {
                 Toast toast = Toast.makeText(context, e.getMessage(), 3000);
                 toast.show();
             }
         }
         }
     }
 
     public void loadDistances()  {
         CMySQLiteOpenHelper helper = new CMySQLiteOpenHelper(context);
         try
         {
             currentDistance = helper.getUnfinishedDistance();
         }
         catch (Exception e)
         {
             Toast toast = Toast.makeText(context, e.getMessage(), 3000);
             toast.show();
         }
         }
 
 	public void deleteLastShot()    {
        if (currentDistance!=null)
		    currentDistance.deleteLastShot();
 	}
 
     public void endCurrentDistance() {
         if (currentDistance!=null)
         {
             if (!currentDistance.isEmpty())
             {
                 CMySQLiteOpenHelper helper = new CMySQLiteOpenHelper(context);
                 helper.deleteDistance(currentDistance);
                 currentDistance.isFinished = true;
             }
             else
                 currentDistance = null;
         }
     }
 
     public void addShot(CShot shot) {
         if (currentDistance == null)
         {
             currentDistance = new CDistance(numberOfSeries, arrowsInSeries,getCurrentArrow());
             currentDistance.addShot(shot);
         }
         else
         if (currentDistance.addShot(shot) == 0)
         {
             endCurrentDistance();
             saveDistances();
             previousDistance = currentDistance;
             currentDistance = null;
         }
     }
 	
     public CArrow getCurrentArrow() {
         String arrowName = PreferenceManager.getDefaultSharedPreferences(context).getString("arrowName","defaultArrow");
         try
         {
             ObjectInputStream oos =
                     new ObjectInputStream(context.openFileInput("Arrows"));
             CArrow arrows[] = (CArrow[]) oos.readObject();
             for (CArrow arrow : arrows)
                 if (arrow.name.equals(arrowName))
                     return arrow;
             oos.close();
         }
         catch (Exception e)
         {
             Toast toast = Toast.makeText(context, e.getMessage(), 3000);
             toast.show();
         }
         return null;
     }
 
     public CDistance getCurrentDistance()   {
         if ((currentDistance==null)&&(previousDistance==null))
             return new CDistance(numberOfSeries,arrowsInSeries,null);
         if (currentDistance==null)
             return previousDistance;
         if ((currentDistance.isEmpty()&&(previousDistance!=null)))
             return previousDistance;
         else
             return currentDistance;
     }
 }
