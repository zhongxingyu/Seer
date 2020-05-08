 package com.finalyear.controlrobot;
 import java.util.ArrayList;
 
 import com.example.controlrobot.R;
 
 import android.os.Bundle;
 import android.app.Activity;
 import android.view.Menu;
 import android.view.View;
 import android.view.View.OnClickListener;
 import at.abraxas.amarino.Amarino;
 import android.widget.Button;
 import android.widget.ImageButton;
 import android.widget.TextView;
 
 public class MainActivity extends Activity {
 	
 	private static final String DEVICE_ADDRESS = "00:12:12:04:32:51";
 	ImageButton forward;
 	ImageButton left;
 	ImageButton right;
 	ImageButton backward;
 	TextView Display1;
 	TextView Display2;
 	TextView Display3;
 	TextView Display4;
 	TextView Display5;
 	TextView Display6;
 	TextView Display7;
 	TextView Display8;
 	TextView Display9;
 	
 	TextView Display10;
 	TextView Display11;
 	TextView Display12;
 	TextView Display13;
 	TextView Display14;
 	TextView Display15;
 	TextView Display16;
 	TextView Display17;
 	TextView Display18;
 	
 	Button button;
 	Button button2;
 	
 	static char Maze[][] ={
 		{'#','0','#','#','#','#','#','#','#'},
 		{'#','0','0','0','#','0','0','0','#'},
 	    {'#','0','#','#','#','0','#','0','#'},
 	    {'#','0','#','0','0','0','#','0','#'},
 	    {'#','0','#','0','#','0','#','#','#'},
 	    {'#','0','0','0','#','0','#','0','#'},
 	    {'#','0','#','#','#','0','#','0','#'},
 	    {'#','0','0','0','#','0','0','0','#'},
 	    {'#','#','#','#','#','#','#','0','#'}
 	};
 	
 	static char Wall = '#';
 	static char Free = '0';
 	static char Path = 'X';
 	
 	static int Height = 9;
     static int Width = 9;
     
     static Coordinate Start = new Coordinate(7,8);
     static Coordinate End = new Coordinate(1,0);
     
     static char CurrentDirection = 'n';
     
     static int previousX = 0;
     static int previousY = 0;
     
     static int currentX = 7;
     static int currentY = 8;
     
     static ArrayList<String> instructions = new ArrayList<String>();
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_main);
 		Amarino.connect(this, DEVICE_ADDRESS);
 		
 		/*forward = (ImageButton) findViewById(R.id.imageButton1);
 		right = (ImageButton) findViewById(R.id.imageButton2);
 		left = (ImageButton) findViewById(R.id.imageButton4);
 		backward = (ImageButton) findViewById(R.id.imageButton3);*/
 		
 		Display1 = (TextView) findViewById(R.id.textView2);
 		Display2 = (TextView) findViewById(R.id.textView3);
 		Display3 = (TextView) findViewById(R.id.textView4);
 		Display4 = (TextView) findViewById(R.id.textView5);
 		Display5 = (TextView) findViewById(R.id.textView6);
 		Display6 = (TextView) findViewById(R.id.textView7);
 		Display7 = (TextView) findViewById(R.id.textView8);
 		Display8 = (TextView) findViewById(R.id.textView9);
 		Display9 = (TextView) findViewById(R.id.textView10);
 		
 		Display1.setText(new String(Maze[0]));
 		Display2.setText(new String(Maze[1]));
 		Display3.setText(new String(Maze[2]));
 		Display4.setText(new String(Maze[3]));
 		Display5.setText(new String(Maze[4]));
 		Display6.setText(new String(Maze[5]));
 		Display7.setText(new String(Maze[6]));
 		Display8.setText(new String(Maze[7]));
 		Display9.setText(new String(Maze[8]));
 		
 		Display10 = (TextView) findViewById(R.id.textView12);
 		Display11 = (TextView) findViewById(R.id.textView13);
 		Display12 = (TextView) findViewById(R.id.textView14);
 		Display13 = (TextView) findViewById(R.id.textView15);
 		Display14 = (TextView) findViewById(R.id.textView16);
 		Display15 = (TextView) findViewById(R.id.textView17);
 		Display16 = (TextView) findViewById(R.id.textView18);
 		Display17 = (TextView) findViewById(R.id.textView19);
 		Display18 = (TextView) findViewById(R.id.textView20);
 		
 		button = (Button) findViewById(R.id.button1);
 		
 		button.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View arg0) {
 					if(solve(Start.getX(), Start.getY())){
 						findpath();
 						Display10.setText(new String(Maze[0]));
 						Display11.setText(new String(Maze[1]));
 						Display12.setText(new String(Maze[2]));
 						Display13.setText(new String(Maze[3]));
 						Display14.setText(new String(Maze[4]));
 						Display15.setText(new String(Maze[5]));
 						Display16.setText(new String(Maze[6]));
 						Display17.setText(new String(Maze[7]));
 						Display18.setText(new String(Maze[8]));		
 						button.setVisibility(View.INVISIBLE);
 						button2.setVisibility(0);
 					}	
 				}
 			});
 		
 		button2 = (Button) findViewById(R.id.button2);
 		button2.setVisibility(View.INVISIBLE);
 		
 		button2.setOnClickListener(new OnClickListener(){
 
 			@Override
 			public void onClick(View v) {
 				for(String x: instructions){
 					if(x.equals("Forward")){
 						forward();
 					} else if (x.equals("Left")){
 						left();
 					} else if(x.equals("Right")){
 						right();
 					}
 				}
 				
 			}
 			
 		});
 		
 
 	}
 	
 	public static void findpath(){
 		while(currentX != End.getX() && currentY != End.getY()){
 				switch(CurrentDirection){
 				case 'n': 
 							if(currentY - 1 >= 0 && (Maze[currentY - 1][currentX] == Path) || (Maze[currentY - 1][currentX] == 'E')){ 
 								currentY = currentY - 1;
 								instructions.add("Forward");
 								break;
 							}
 							if(currentX - 1 >= 0 && (Maze[currentY][currentX - 1] == Path) || (Maze[currentY][currentX - 1] == 'E')){
 								currentX = currentX - 1;
 								instructions.add("Left");
 								CurrentDirection = 'w';
 								break;
 							}
 							if(currentX + 1 < Width && (Maze[currentY][currentX + 1] == Path) || (Maze[currentY][currentX + 1] == 'E')){
 								currentX = currentX + 1;
 								instructions.add("Right");
 								CurrentDirection = 'e';
 								break;
 							}
 				case 'e':
 							if(currentX + 1  < Width && (Maze[currentY][currentX + 1] == Path) || (Maze[currentY][currentX + 1] == 'E')){
 								currentX = currentX - 1;
 								CurrentDirection = 'e';
 								instructions.add("Forward");
 								break;
 							}
 							if(currentY - 1 >= 0 && (Maze[currentY - 1][currentX] == Path) || (Maze[currentY - 1][currentX] == 'E')){
 								currentY = currentY - 1;
 								instructions.add("Left");
 								CurrentDirection = 'n';
 								break;
 							}
 							if(currentY + 1 < Height && (Maze[currentY + 1][currentX] == Path) || (Maze[currentY + 1][currentX] == 'E')){
 								currentY = currentY + 1;
 								instructions.add("Right");
 								CurrentDirection = 's';
 								break;
 							}
 				case 's':	
 							if(currentY + 1 < Height && (Maze[currentY + 1][currentX] == Path) || (Maze[currentY + 1][currentX] == 'E')){
 								currentY = currentY + 1;
 								instructions.add("Forward");
 								break;
 							}
 							if(currentX + 1 < Width && (Maze[currentY][currentX + 1] == Path) || (Maze[currentY][currentX + 1] == 'E')){
 								currentX = currentX + 1;
 								instructions.add("Left");
 								CurrentDirection = 'e';
 								break;
 							}
 							if(currentX - 1 >= 0 && (Maze[currentY][currentX - 1] == Path) || (Maze[currentY][currentX - 1] == 'E')){
 								currentX = currentX - 1;
 								instructions.add("Right");
 								CurrentDirection = 'w';
 								break;
 							}
 				case 'w':
 							if(currentY - 1 >= 0 && (Maze[currentY - 1][currentX] == Path) || (Maze[currentY - 1][currentX] == 'E')){
 								currentY = currentY - 1;
 								instructions.add("Right");
 								CurrentDirection = 'n';
 								break;
 							}
 							if(currentX - 1 >= 0 && (Maze[currentY][currentX - 1] == Path) || (Maze[currentY][currentX - 1] == 'E')){
 								currentX = currentX - 1;
 								instructions.add("Forward");
 								break;
 							}
 							if(currentY + 1 < Height && (Maze[currentY+1][currentX] == Path) || (Maze[currentY+1][currentX] == 'E')){
 								currentY = currentY + 1;
 								instructions.add("Left");
 								CurrentDirection = 's';
 								break;
 							}	
 				}
 		}
 	} 
 	
 	public static boolean solve(int x, int y){
 		if(x == Start.getX() && y == Start.getY()){
 			Maze[y][x] = 'S';
 		} else if (x == End.getX() && y == End.getY()){
 			Maze[y][x] = 'E';
 		}else{
 		Maze[y][x] = Path;
 		}
 		
 		if(x == End.getX() && y == End.getY()){
 			return true;
 		}
 		
 		if(x > 0 && Maze[y][x - 1] == Free && solve(x - 1, y)){
 			return true;
 		}
 		if(x < Width && Maze[y][x+1] == Free && solve(x+1,y)){
 			return true;
 		}
 		if(y > 0 && Maze[y - 1][x] == Free && solve(x,y - 1)){
 			return true;
 		}
 		if(y < Height && Maze[y + 1][x] == Free && solve(x,y+1)){
 			return true;
 		}
 		
 		Maze[y][x] = Free;
 		
 		return false;
 	}
 	
 	private void forward(){
 		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'A', 0);
 	}
 	
 	private void backward(){
 		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'B', 0);
 	}
 	
 	private void right(){
 		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'C', 0);
 	}
 	
 	private void left(){
 		Amarino.sendDataToArduino(this, DEVICE_ADDRESS, 'D', 0);
 	}
 
 	@Override
 	public boolean onCreateOptionsMenu(Menu menu) {
 		// Inflate the menu; this adds items to the action bar if it is present.
 		getMenuInflater().inflate(R.menu.main, menu);
 		return true;
 	}
 
 }
