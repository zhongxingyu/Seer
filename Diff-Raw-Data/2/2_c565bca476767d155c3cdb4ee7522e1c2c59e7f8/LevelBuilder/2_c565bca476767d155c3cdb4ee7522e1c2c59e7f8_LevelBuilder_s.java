 package levelBuilder;
 
 import game.Platformer;
 import game.PlatformGame;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.lang.reflect.Type;
 import java.util.ArrayList;
 import java.util.Scanner;
 
 import javax.imageio.ImageIO;
 
 import sprites.*;
 import sprites.Character;
 
 import levelEditor.GameFile;
 import levelEditor.SpriteInfo;
 
 import com.golden.gamedev.object.Background;
 import com.golden.gamedev.object.PlayField;
 import com.golden.gamedev.object.Sprite;
 import com.golden.gamedev.object.SpriteGroup;
 import com.golden.gamedev.object.background.ImageBackground;
 import com.google.gson.Gson;
 import com.google.gson.reflect.TypeToken;
 
 public class LevelBuilder {
 	public Platformer myGame=null;
 	GameFile myGameInfo;
 	public Background background;
 
 
 	public LevelBuilder(Platformer p) {
 		myGame=p;
 		GameFile myGameInfo=null;
 	}
 	
 	public PlayField createLevel(String jsonString) {
 		PlayField field= new PlayField();
 
 		Gson gson = new Gson();
 		Scanner scanner;
 		try {
 			scanner = new Scanner(new File(jsonString));
 
 			String wholeFile = scanner.useDelimiter("\\A").next();
 			Type collectionType = new TypeToken<GameFile>() {
 			}.getType();
 			myGameInfo = gson.fromJson(wholeFile, collectionType);
 
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		
 		if (myGameInfo==null) {
 			return null;
 		}
 
 
 
 		File backgroundPathFile= null; 
 		BufferedImage myBackground=null;		
 				try {
 		backgroundPathFile= new File(myGameInfo.getBackground());
 
 			myBackground = ImageIO.read(backgroundPathFile);
 		} catch (IOException e1) {
 			System.out.print("no background");
 		}
 		background = new ImageBackground(myBackground);
 		field = new PlayField(background);
 		// create groups
 		myGame.CHARACTER = field.addGroup(new SpriteGroup("Character"));
 		myGame.PROJECTILE = field.addGroup(new SpriteGroup("Projectile"));
 		myGame.POWER_UP = field.addGroup(new SpriteGroup("Power_Up"));
 		myGame.PLATFORM = field.addGroup(new SpriteGroup("Platform"));
 		myGame.SPAWNPOINT = field.addGroup(new SpriteGroup("SPAWNPOINT"));
 		myGame.COINS = field.addGroup(new SpriteGroup("COINS"));
 		myGame.BAD_GUYS = field.addGroup(new SpriteGroup("BAD_GUYS"));
		myGame.SPRINGS = field.addGroup(new SpriteGroup("SPRINGS"));
 
 
 		ArrayList<LESprite> SpriteList= new ArrayList<LESprite>();
 		SpriteList.add(new Bad_Guys());
 		SpriteList.add(new Character());
 		SpriteList.add(new Platform());
 		
 		for(int k=0;k<myGameInfo.getList().size();k++) {
 			ArrayList<String> LESpriteinfo= myGameInfo.getList().get(k);
 			for (int i=0;i<SpriteList.size();i++) {
 			    if(SpriteList.get(i).isInstanceOf(LESpriteinfo)) {
 				Sprite sprite= SpriteList.get(i).parse(LESpriteinfo, myGame);
 		    	}
 		    }
 		}
 	 
 
 //		
 //
 //		// set up collision groups
 //		field.addCollisionGroup(myGame.CHARACTER, myGame.PROJECTILE,
 //				new CharacterProjectileCollision());
 //		field.addCollisionGroup(myGame.PROJECTILE, myGame.BAD_GUYS,
 //				new EnemyProjectileCollision());
 //		field.addCollisionGroup(myGame.CHARACTER, myGame.PLATFORM,
 //				new SpritePlatformCollision());
 //		field.addCollisionGroup(myGame.CHARACTER, myGame.SPRINGS,
 //				new CharacterSpringCollision());
 //		// playfield.addCollisionGroup(CHARACTER, BAD_GUYS,
 //		// new CharacterEnemyCollision());
 //		// playfield.addCollisionGroup(CHARACTER, COINS, new CoinCollision());
 //		// playfield.addCollisionGroup(CHARACTER, POWER_UP,
 //		// new PowerUpCollision());
 //		// playfield.addCollisionGroup(CHARACTER, PLATFORM,
 //		// new PlatformCollision());
 //		// playfield.addCollisionGroup(CHARACTER, EXIT, new ExitCollision());
 //		// playfield.addCollisionGroup(BAD_GUYS, PLATFORM,
 //		// new PlatformCollision());
 
 		return field;
 	}
 	
 }
 		
