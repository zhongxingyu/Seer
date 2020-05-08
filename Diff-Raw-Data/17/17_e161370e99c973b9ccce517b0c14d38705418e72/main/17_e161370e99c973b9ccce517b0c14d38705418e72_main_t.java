 package levelBuilder;
 
 import hudDisplay.NumberStat;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStreamWriter;
 import java.util.ArrayList;
 
 import javax.imageio.ImageIO;
 
 import org.jdom2.Document;
 import org.jdom2.Element;
 import org.jdom2.output.Format;
 import org.jdom2.output.XMLOutputter;
 
 import SpriteAction.JetPack;
 
 import sprites.*;
 import sprites.Character;
 
 public class main {
 	public static void main(String[] args) throws IOException {
 
 		Document doc = new Document();
 		Element root = new Element("root").setText("This is the root");
 		doc.addContent(root);
 		Element background = new Element("background")
				.setText("src/images/background.jpg");
 		root.addContent(background);
 
 		ArrayList<LevelEditable> spritelist = new ArrayList<LevelEditable>();
 		GeneralSprite s1 = new MainCharacter();
 		// BufferedImage[] images = new BufferedImage[1];
 		// images[0] = ;
 		s1.setImage(ImageIO.read(new File("src/images/mario1.png")));
 		s1.setLocation(100, 100);
 		s1.setPath("src/images/mario1.png");
 		s1.createStat("lives", new NumberStat(5));
 		s1.createStat("score", new NumberStat(0));
 		s1.createStat("coinMult", new NumberStat(0));
 		spritelist.add(s1);
 
 		BufferedImage image = ImageIO.read(new File("src/images/bricks1.png"));
 		for (int i = 0; i < 1990; i += 32) {
 			if (i < 300 | i > 400) {
 				GeneralSprite p= new Platform(image, i, 400);
 				p.setPath("src/images/bricks1.png");
 				spritelist.add(p);
 				
 			}
 		}
 
 		GeneralSprite mush = new LifeMushroom(ImageIO.read(new File(
 				"src/images/mushroom.png")), 1350, 406);
 		mush.setPath("src/images/mushroom.png");
 		spritelist.add(mush);
 		
 		GeneralSprite flag = new Flag(ImageIO.read(new File(
 				"src/images/finalflag.png")), 1750, 106);
 		flag.setPath("src/images/finalflag.png");
 		spritelist.add(flag);
 
 		GeneralSprite castle = new GeneralSprite(ImageIO.read(new File(
 				"src/images/castle.gif")), 1800, 300);
 		castle.setPath("src/images/castle.gif");
 		spritelist.add(castle);
 		
 
 //		HUD = new HeadsUpDisplay(0, 0);
 //
 //		GameFont BigFont = fontManager.getFont(getImages("images/Font.png", 8,
 //				12));
 //		GameFont SmallFont = fontManager.getFont(getImages(
 //				"images/SmallFont.png", 8, 12));
 //
 //		TextItem lives = new TextItem(BigFont, 10, 10,
 //				mainChar.getStat("lives"), "Mario x ");
 //		HUD.addItem(lives);
 //
 //		timer = new NumberStat(0);
 //		timer.incrementWithTimer(500, 1);
 //		TextItem timerScore = new TextItem(BigFont, 300, 10, timer, "Time: ");
 //		HUD.addItem(timerScore);
 //
 //		TextItem CoinMult = new TextItem(SmallFont, 500, 10,
 //				mainChar.getStat("coinMult"), "Coins x ");
 //		HUD.addItem(CoinMult);
 //
 //		TextItem score = new TextItem(SmallFont, 500, 20,
 //				mainChar.getStat("score"));
 //		HUD.addItem(score);
 
 		GeneralSprite jet = new Jetpack(ImageIO.read(new File("src/images/rocket.png")), 400, 400);
 		jet.setPath("src/images/rocket.png");
 		spritelist.add(jet);
 		
 		GeneralSprite enemy1 = new HomingEnemy();
 		enemy1.setImage(ImageIO.read(new File("src/images/boo.jpg")));
 		enemy1.setPath("src/images/boo.jpg");
 		enemy1.setLocation(300, 300);
 
 		GeneralSprite enemy2 = new HomingEnemy();
 		enemy2.setImage(ImageIO.read(new File("src/images/boo.jpg")));
 		enemy2.setLocation(700, 300);
 		enemy2.setPath("src/images/boo.jpg");
 
 		GeneralSprite enemy3 = new HomingEnemy();
 		enemy3.setImage(ImageIO.read(new File("src/images/boo.jpg")));
 		enemy3.setLocation(1200, 300);
 		enemy3.setPath("src/images/boo.jpg");
 
 		GeneralSprite enemy4 = new HomingEnemy();
 		enemy4.setImage(ImageIO.read(new File("src/images/boo.jpg")));
 		enemy4.setLocation(1700, 300);
 		enemy4.setPath("src/images/boo.jpg");
 
 		spritelist.add(enemy1);
 		spritelist.add(enemy2);
 		spritelist.add(enemy3);
 		spritelist.add(enemy4);
 		Element sprites = new Element("sprites");
 		root.addContent(sprites);
 
 		for (int i = 0; i < spritelist.size(); i++) {
 			Element sprite = spritelist.get(i).writeElement();
 			sprites.addContent(sprite);
 		}
 		// serialize it onto System.out
 		try {
 			XMLOutputter outputter = new XMLOutputter();
 			Format oFormat = Format.getPrettyFormat();
 			outputter.setFormat(oFormat);
 			FileOutputStream FileOS = new FileOutputStream("level1.xml");
 			OutputStreamWriter OSWriter = new OutputStreamWriter(FileOS);
 			outputter.output(doc, OSWriter);
 			OSWriter.close();
 			FileOS.close();
 
 		} catch (IOException e) {
 			System.err.println(e);
 		}
 
 	}
 
 }
