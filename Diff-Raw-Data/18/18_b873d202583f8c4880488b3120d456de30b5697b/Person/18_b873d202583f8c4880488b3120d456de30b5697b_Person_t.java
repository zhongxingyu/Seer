 package isa;
 
 import isa.gui.GameScreen;
 
 import java.io.BufferedReader;
 import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
 
 public class Person {
 	public double	foreignness;			// in percent
 	public String	name		= "";
 	public boolean	suspected	= false;
 
 	public Sprite	suspect;
 	public Sprite	background;
 
 	/* Since you are reading through the code, I will let you in on a little secret, there are no terrorists.
 	 * I could have made a system to pick a few random people to be terrorists that you have to find, but that isn't very critical. ;) */
 
 	public Person() {
 		foreignness = Math.random() * 100;
 
 		int l1 = (int) ((Math.random() * 251 + 2) * 2);
 		int l2 = (int) ((Math.random() * 251 + 1) * 2);
 		BufferedReader b;
 		try {
			InputStream namesFile = Main.class.getResourceAsStream("names.txt");
			InputStreamReader namesReader = new InputStreamReader(namesFile);

			b = new BufferedReader(namesReader);
 			for (int i = 0; i < l2; i++) {
 				b.readLine();
 			}
 			name += b.readLine() + " ";
 
			namesFile = Main.class.getResourceAsStream("names.txt");
			namesReader = new InputStreamReader(namesFile);
			b = new BufferedReader(namesReader);
 
 			for (int i = 0; i < l1; i++) {
 				b.readLine();
 			}
 			name += b.readLine();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		background = Gameplay.screen.textures.createSprite("white");
 		background.setColor(145f / 255f, 1f, 145f / 255f, 1f);
 
 		suspect = Gameplay.screen.textures.createSprite("suspect");
 	}
 
 	public void render(SpriteBatch batch, int offset) {
 		background.setBounds(50, offset, GameScreen.WIDTH - 93, 100);
 		background.draw(batch);
 
 		suspect.setPosition(60, offset + 5);
 		suspect.draw(batch);
 
 		GameScreen.text1.draw(batch, "Name: " + name, 64, offset + 95);
 		GameScreen.text2.draw(batch, "Foreignness: " + foreignness + "%", 64, offset + 75);
 	}
 
 	int getRandomNumber() {
 		return 4; // chosen by fair dice roll
 					// guaranteed to be random
 	}
 
 	public void suspect() {
 		Gameplay.suspectedQuota--;
 		Gameplay.people.remove(this);
 	}
 }
