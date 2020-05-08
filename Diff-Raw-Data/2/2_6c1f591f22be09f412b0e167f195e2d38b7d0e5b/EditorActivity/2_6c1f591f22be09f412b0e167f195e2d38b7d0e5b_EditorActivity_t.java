 package editor;
 
 import java.io.BufferedReader;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import org.jbox2d.common.Vec2;
 import org.jbox2d.dynamics.World;
 
 import maps.Earth;
 import maps.EditorMap;
 import maps.Jupiter;
 import maps.Map;
 import maps.Moon;
 
 import com.example.escapeandroid.R;
 
 import entities.Entities;
 import entities.ships.Player;
 import entities.ships.enemies.EnemiesLoader;
 import entities.ships.enemies.EnemyDef;
 import factories.ShipFactory;
 import fr.umlv.android.GameActivity;
 import fr.umlv.android.GameGraphicsView;
 import game.Environnement;
 import game.Variables;
 
 import android.app.Activity;
 import android.content.ClipData;
 import android.content.Context;
 import android.content.Intent;
 import android.graphics.BitmapFactory;
 import android.graphics.Point;
 import android.graphics.drawable.Drawable;
 import android.os.Bundle;
 import android.util.Log;
 import android.view.Display;
 import android.view.DragEvent;
 import android.view.MotionEvent;
 import android.view.View;
 import android.view.View.DragShadowBuilder;
 import android.view.View.MeasureSpec;
 import android.view.View.OnDragListener;
 import android.view.View.OnTouchListener;
 import android.view.ViewGroup;
 import android.widget.ImageView;
 import android.widget.LinearLayout;
 import android.widget.Toast;
 
 public class EditorActivity extends Activity{
 	
 	private String scriptFile = "EnemyNew";
 	public static Environnement environnement;
 	public static Map map;
 	
 	
 	@Override
 	protected void onCreate(Bundle savedInstanceState) {
 		// TODO Auto-generated method stub
 		super.onCreate(savedInstanceState);
 		setContentView(R.layout.activity_editor);
 		setupMap();
 		setupViews();
 	}	
 	
 	private void setupMap(){
 		Intent intent= getIntent();
 		String level = intent.getStringExtra("level");
 		String[] res = level.split(" ");
 		scriptFile += res[1] + ".xml";
 		
 		
 		String mapName = intent.getStringExtra("map");
 		if(mapName.equals("Earth")){
 			map = new Earth();
 			EditorGraphicsView.mapDrawable = R.drawable.earth;
 		}
 		if(mapName.equals("Jupiter")){
 			map = new Jupiter();
 			EditorGraphicsView.mapDrawable = R.drawable.jupiter;
 		}
 		if(mapName.equals("Moon")){
 			map = new Moon();
 			EditorGraphicsView.mapDrawable = R.drawable.moon;
 		}
 		EditorGraphicsView.mapChanged = true;
 		
 		//OMG... I DONT KNOW WHAT I'M DOING... (BUT THE SOUND IS GOOD)
 		//Bon en vrai on va utiliser le "ennemyLoader" pour connaitre la position des Enemy
 		try {
 			World world = new World(new Vec2(Variables.WORLD_GRAVITY_X, Variables.WORLD_GRAVITY_Y), false);
 			world.setAllowSleep(false);
 			Entities entities = new Entities(world);
 			EnemiesLoader ennemyloader = new EnemiesLoader(entities, scriptFile, false);// xml of ennemies of the moon
 
 			for (EnemyDef enemyDef : ennemyloader.enemysDef) {
 				if (!enemyDef.isBoss())
 					EditorGraphicsView.vaisseaux.add(new Enemy(enemyDef.image,
 							enemyDef.name, enemyDef.x, enemyDef.y));
 			}
 		} catch (Exception e) {	
 			EditorGraphicsView.vaisseaux.clear();
 		} // Si sa plante ON S'EN FOU (on charge pas les ennemies)
 	}
 	
 	
 	
 	public void clean(View view){
 		EditorGraphicsView.vaisseaux.clear();
 		EditorGraphicsView.get().invalidate();
 	}
 	
 	public void play(View view) throws Exception{		
 		Display display = getWindowManager().getDefaultDisplay();
 		Point size = new Point();
 		display.getSize(size);
 		int width = size.x;
 		int height = size.y;
 		
 		Variables.SCREEN_WIDTH = width;
 		Variables.SCREEN_HEIGHT = height;		
 		
 		World world = new World(new Vec2(Variables.WORLD_GRAVITY_X, Variables.WORLD_GRAVITY_Y), false);
 		world.setAllowSleep(false);
 		
 		Entities entities = new Entities(world);
 		EnemiesLoader ennemyloader;
 		try{
 			ennemyloader = new EnemiesLoader(entities, scriptFile, false);//xml of ennemies of the moon
 		}
 		catch(Exception e){
			Toast.makeText(this, "Aucun ennemis n'a t sauvegard", Toast.LENGTH_LONG).show();
 			return;
 		}
 
 		ShipFactory factory = new ShipFactory(entities);
 		Player playerShip = factory.createPlayer();
 		
 		environnement = new Environnement(entities, map, playerShip, ennemyloader);		
 		
 		GameGraphicsView.game = null;
 		
 		Intent intent = new Intent(this, GameActivity.class);
 		startActivity(intent);
 	}
 	
 	public void save(View view) {
 		FileOutputStream fOut = null;
 		OutputStreamWriter osw = null;
 
 		try {
 			fOut = getBaseContext().openFileOutput(scriptFile, MODE_PRIVATE);
 			osw = new OutputStreamWriter(fOut);
 			
 			String head = "<?xml version=\"1.0\" encoding=\"UTF-8\"?> <enemies>";
 			osw.write(head);
 			
 			//Liste des actions (en XML) d'un enemy ( placer a la fin d'une liste d'apparition)
 			String action = "<actions repeattime=\"6\">	<move beg=\"1\" end=\"2\">	<angle>270</angle>	<velocity>100</velocity>  </move> <move beg=\"3\" end=\"4\"> <angle>300</angle> <velocity>100</velocity>	</move>	<move beg=\"5\" end=\"6\">	<angle>240</angle>	<velocity>100</velocity>	</move>	<fire beg=\"1\" end=\"2\">	<name>Fireball</name>	<angle>265</angle>	<velocity>200</velocity> </fire> <fire beg=\"5\" end=\"6\">	<name>Shiboleet</name>	<angle>275</angle>	<velocity>200</velocity></fire>	</actions> </enemy>";
 			
 			List<Enemy> enemies = EditorGraphicsView.vaisseaux;
 			Collections.sort(enemies);
 			
 			
 			
 			String first = "";
 			for(Enemy enemy : enemies){
 				String name = enemy.name;
 				/*
 				 * 2 cas:
 				 * _On change d'enemy (On remet les info: id, image et life)
 				 * _On continu avec le mme enemy (on change juste sa date d'apparition)
 				 */
 				if(!first.equals(name)){
 					if(first!="")
 						osw.write(action);
 					
 					first = name;
 					String info = "<enemy id=\"" + name + "\"> <life>35</life>	<image>" + name + "</image>";
 					osw.write(info);
 				}
 				
 				//TODO: time d'apparition (en fonction de Y)
 				//NOTE: posY plus pris en compte...
 				int appearTime = (int)(EditorGraphicsView.map.getHeight() - enemy.posY - Variables.SCREEN_HEIGHT )/15;
 				if(appearTime<=0)
 					appearTime = 1;
 				String apparition = " <appear time=\"" + appearTime + "\"> <posX>" + (int)enemy.posX + "</posX> <posY>" + (int)enemy.posY + "</posY> </appear>";
 				osw.write(apparition);
 			}				
 			osw.write(action);
 			
 			String boss = "<boss> <life>400</life> <image>boss</image>	<appear time=\"85\"> <posX>150</posX>	<posY>950</posY></appear><actions repeattime=\"10\">	<move beg=\"2\" end=\"3\"> <angle>-90</angle> <velocity>70</velocity></move><move beg=\"4\" end=\"5\">	<angle>0</angle><velocity>50</velocity> </move> <move beg=\"7\" end=\"8\">	<angle>90</angle> <velocity>70</velocity> </move> <move beg=\"9\" end=\"10\"> <angle>180</angle> <velocity>50</velocity> </move> <fire beg=\"4\" end=\"6\"> <name>ShiboleetExtended</name>	<angle>270</angle> <velocity>250</velocity>	</fire>	</actions></boss></enemies>";
 			osw.write(boss);
 			
 			osw.flush();
 		} catch (Exception e) {
 			e.printStackTrace();
 		} finally {
 			try {
 				osw.close();
 				fOut.close();
 			} catch (IOException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	
 	private String readTest() throws IOException{
 		FileInputStream fis = getBaseContext().openFileInput(scriptFile);			  
 		int content;
 		String text = "";
 		while ((content = fis.read()) != -1) {
 			text += (char) content;
 		}
 		return text;
 	}
 	
 	
 	/*
 	 * FOR DRAG AND DROP IN EDITOR...
 	 */	
 	private void setupViews() {		
 		
 		OnTouchListener touchListener = new View.OnTouchListener() {
 			@Override
 			public boolean onTouch(View view, MotionEvent motionEvent) {
 				if (motionEvent.getAction() == MotionEvent.ACTION_DOWN) {
 					ClipData data = ClipData.newPlainText("", "");
 					DragShadowBuilder shadowBuilder = new View.DragShadowBuilder(view);
 					view.startDrag(data, shadowBuilder, view, 0);
 					//view.setVisibility(View.INVISIBLE);
 					return true;
 				} else {
 					return false;
 				}
 			}
 		};
 
 		ImageView i1 = (ImageView) findViewById(R.id.imageView1);
 		ImageView i2 = (ImageView) findViewById(R.id.imageView2);
 		ImageView i3 = (ImageView) findViewById(R.id.imageView3);
 		ImageView i4 = (ImageView) findViewById(R.id.imageView4);
 		
 		i1.setOnTouchListener(touchListener);
 		i2.setOnTouchListener(touchListener);
 		i3.setOnTouchListener(touchListener);
 		i4.setOnTouchListener(touchListener);
 	}
 	
 	
 }
