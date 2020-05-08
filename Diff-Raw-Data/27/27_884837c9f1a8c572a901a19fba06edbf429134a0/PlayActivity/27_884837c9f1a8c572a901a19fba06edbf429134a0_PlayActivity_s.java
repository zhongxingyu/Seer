 package com.example.simonsays;
 
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import com.example.simonsays.R.layout;
 import android.os.Bundle;
 import android.os.Handler;
 import android.R.integer;
 import android.app.Activity;
 import android.content.Context;
 import android.content.res.Resources;
 import android.graphics.Color;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import android.widget.AdapterView;
 import android.widget.AdapterView.OnItemClickListener;
 import android.widget.ArrayAdapter;
 import android.widget.GridLayout;
 import android.widget.GridView;
 import android.widget.ImageButton;
 import android.widget.ImageView;
 import android.widget.TableLayout.LayoutParams;
 import android.widget.TableLayout;
 import android.widget.TableRow;
 import android.widget.TextView;
 import android.widget.Toast;
 
 public class PlayActivity extends Activity
 {
     TableLayout tableLay;
    private List<Objects> pickedobjects = new ArrayList<Objects>();
    
    static final String[] numbers = new String[] { 
        "A", "B", "C", "D", "E",
        "F", "G", "H", "I", "J",
        "K", "L", "M", "N", "O",
        "P", "Q", "R", "S", "T",
        "U", "V", "W", "X", "Y", "Z",
        "AA","BB","CC","DD","EE" };
    
 
     @Override
     protected void onCreate(Bundle savedInstanceState)
     {
         super.onCreate(savedInstanceState);
         setContentView(R.layout.activity_play);
         
         //index for keys from config
         //getIntent().getExtras().getString("user");
         //getIntent().getExtras().getString("numOfObjects");
         //getIntent().getExtras().getString("square");
         //getIntent().getExtras().getString("triangle");
         //getIntent().getExtras().getString("circle");
         //getIntent().getExtras().getString("red");
         //getIntent().getExtras().getString("blue");
         //getIntent().getExtras().getString("green");
         //getIntent().getExtras().getString("purple");
         //getIntent().getExtras().getString("orange");
         //getIntent().getExtras().getString("yellow");
         //getIntent().getExtras().getString("layout");
         //getIntent().getExtras().getString("objectSize");             
         
         
         int numOfObjects = Integer.parseInt(getIntent().getExtras().getString("numOfObjects"));
         String[] color = getColors();
         String size = getIntent().getExtras().getString("objectSize");
         String[] shape = getShapes();
         populateList(numOfObjects, color, size, shape);
         Score score = new Score(numOfObjects);
         Player player = new Player(getIntent().getExtras().getString("user"), score);
 
         AI testAI = new AI(pickedobjects);
         
         //TextView;
         
 //        testAI.addNewShapeToList();
 //        testAI.addNewShapeToList();
 //        testAI.addNewShapeToList();      
         
         if(getIntent().getExtras().getString("layout") == "grid")
         {
             Grid g = new Grid();
         }
         if(getIntent().getExtras().getString("layout") == "diamond")
         {
             Diamond d = new Diamond();
         }
 //        4 = 2x2
 //        5 -> 9 = 3x3
 //        10 -> 16 = 4x4
 //        17 -> 25 = 5x5
         
         
         tableLay = (TableLayout)findViewById(R.id.tableLayout1); 
                 
         //diamond 4 objects = 3x3
         // 5 -> 8 = 5x5
         // 9 -> 12 = 7x7
         // 13 -> 16 = 9x9
         // 17 -> 20 = 11x11
         // 21 -> 24 = 13x13
         // 25 = special case 13x13 1 in middle
        
         //grid                 
         if(numOfObjects == 4)
         {
                 String colorString = "";
                 String shapeString = "";                
                 String objectString = "";
                 int drawableID;
                Random random = new Random();
             
                TableRow tr1 = new TableRow(this);   
                tr1.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
                
                TableRow tr2= new TableRow(this);   
                tr2.setLayoutParams(new LayoutParams(LayoutParams.FILL_PARENT,LayoutParams.WRAP_CONTENT));
                                            
                ImageButton button1 = new ImageButton(this);
                ImageButton button2 = new ImageButton(this);
                ImageButton button3 = new ImageButton(this);
                ImageButton button4 = new ImageButton(this);
                
                
                colorString = pickedobjects.get(0).getColor();
                shapeString = pickedobjects.get(0).getShape();              
                                       
                objectString = colorString + "_" + shapeString;
 
                drawableID = getResources().getIdentifier(objectString, "drawable", getPackageName());
                button1.setImageResource(drawableID);
                
               // button1.setImageResource(resID);    
                
             
               
               
                
                colorString = pickedobjects.get(1).getColor();
                shapeString = pickedobjects.get(1).getShape();
                objectString = colorString + "_" + shapeString;
                
               drawableID = getResources().getIdentifier(objectString, "drawable", getPackageName());
                button2.setImageResource(drawableID);
                
                
 
                colorString = pickedobjects.get(2).getColor();
                shapeString = pickedobjects.get(2).getShape();
              
                objectString = colorString + "_" + shapeString;
 
                drawableID = getResources().getIdentifier(objectString, "drawable", getPackageName());
                button3.setImageResource(drawableID);
                
                colorString = pickedobjects.get(3).getColor();
                shapeString = pickedobjects.get(3).getShape();
                objectString = colorString + "_" + shapeString;
 
 
                drawableID = getResources().getIdentifier(objectString, "drawable", getPackageName());
                button4.setImageResource(drawableID);
                
               
                
                tr1.addView(button1); 
                tr1.addView(button2); 
                tr2.addView(button3); 
                tr2.addView(button4); 
                
                
                tableLay.addView(tr1);
                tableLay.addView(tr2);
                
         }
         else if(numOfObjects >= 5 || numOfObjects <= 9 )
         {
            
         }
         else if(numOfObjects >= 10 || numOfObjects <= 16)
         {
                   
         }
         else
         {
                     
         }
 
 //        button1.setOnClickListener(new OnClickListener()
 //        {
 //            
 //            @Override
 //            public void onClick(View v) 
 //            {
 //                //next line is the color / shape you want to change to immediatly after click
 //                button1.setImageResource(R.drawable.black_circle);
 //                // SLEEP 2 SECONDS HERE ...
 //                Handler handler = new Handler(); 
 //                handler.postDelayed(new Runnable() 
 //                { 
 //                     public void run() 
 //                     { 
 //                         //this is what the image will return to after the seconds has executed
 //                         button1.setImageResource(R.drawable.blue_circle); 
 //                     } 
 //                }, 500); 
 //            }
 //        }        
 //                );     
         
     }// end of onCreate
     public static int getImageId(Context context, String imageName) 
     {        
         return context.getResources().getIdentifier("R.drawable." + imageName, null, context.getPackageName());
     }
 
     @Override
     public boolean onCreateOptionsMenu(Menu menu)
     {
         // Inflate the menu; this adds items to the action bar if it is present.
         getMenuInflater().inflate(R.menu.play, menu);
         return true;
     }
     
     private String[] getColors()
     {
     	String[] result;
     	int size = 0;
     	int currentIndex = 0;
     	if(getIntent().getExtras().getString("red") != null)
     	{
     		size++;
     	}
         if(getIntent().getExtras().getString("blue") != null)
         {
         	size++;
         }
         if(getIntent().getExtras().getString("green") != null);
         {
         	size++;
         }
         if(getIntent().getExtras().getString("purple") != null)
         {
         	size++;
         }
         if(getIntent().getExtras().getString("orange") != null)
         {
         	size++;
         }
         if(getIntent().getExtras().getString("yellow") != null)
         {
         	size++;
         }
         
         result = new String[size];
        
         if(getIntent().getExtras().getString("red") != null)
     	{
         	result[currentIndex] = "red";
     		currentIndex++;
     	}
         if(getIntent().getExtras().getString("blue") != null)
         {
         	result[currentIndex] = "blue";
         	currentIndex++;
         }
        if(getIntent().getExtras().getString("green") != null);
        {
        	result[currentIndex] = "green";
        	currentIndex++;
        }
         if(getIntent().getExtras().getString("purple") != null)
         {
         	result[currentIndex] = "purple";
         	currentIndex++;
         }
         if(getIntent().getExtras().getString("orange") != null)
         {
         	result[currentIndex] = "orange";
         	currentIndex++;
         }
         if(getIntent().getExtras().getString("yellow") != null)
         {
         	result[currentIndex] = "yellow";
         	currentIndex++;
         }
         
     	return result;
     }
     
     private String[] getShapes()
     {
     	String[] result;
     	int size = 0;
     	int currentIndex = 0;
     	if(getIntent().getExtras().getString("square") != null)
     	{
     		size++;
     	}
     	
     	if(getIntent().getExtras().getString("triangle") != null)
     	{
     		size++;
     	}
     	
     	if(getIntent().getExtras().getString("circle") != null)
     	{
     		size++;
     	}
     	
     	result = new String[size];
         
         if(getIntent().getExtras().getString("square") != null)
     	{
         	result[currentIndex] = "square";
     		currentIndex++;
     	}
         
         if(getIntent().getExtras().getString("triangle") != null)
     	{
         	result[currentIndex] = "triangle";
     		currentIndex++;
     	}
         
         if(getIntent().getExtras().getString("circle") != null)
     	{
         	result[currentIndex] = "circle";
     		currentIndex++;
     	}
         
         return result;
     }
     
     public void populateList(int num, String[] color, String size, String[] shape)
     {
     	Random r = new Random();
     	for(int i = 0; i < num; i++)
     	{    	  
     		int j = r.nextInt(color.length);
     		int k = r.nextInt(shape.length);
     		pickedobjects.add(new Objects(shape[k], color[j], size, i));
     	}    	
     }
     
     
 
 }
