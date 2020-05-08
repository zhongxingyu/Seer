 import java.awt.Graphics2D;
 import java.awt.event.KeyEvent;
 import java.awt.image.BufferedImage;
 
 
 import com.javagroup.game.graphics.BasicGame;
 import com.javagroup.game.graphics.image.Art;
 import com.javagroup.game.graphics.image.Font;
 import com.javagroup.game.graphics.particle.Effect;
 import com.javagroup.game.graphics.particle.Effect.EffectType;
 import com.javagroup.game.graphics.particle.Emitter;
 import com.javagroup.game.graphics.particle.ParticleSystem;
 import com.javagroup.game.input.Input;
 import com.javagroup.game.map.Map;
 import com.javagroup.game.sound.Sound;
 
 public class Game extends BasicGame {
 
 	Map testMap;
 	float x = 450;
 	float y;
 
 	ParticleSystem particleSystem;
 	Emitter emitter, emitter2, emitter3;
 	Font font = new Font();
	BufferedImage testFont;
 	Sound sound ;
 
 
 
 	testEnt testent;
 
 	
 
 	public void initiate() {
 		Art.init();
 		testFont = Font.getFont().getLetters("PARTICLES");
 		
		sound = new Sound("/echelonfadeout.wav");
 		sound.load();
		sound.setGain(1.0f);
 		sound.loop();
 
 		// Testing
 		{
 			testent = new testEnt();
			testMap = new Map("Test",200, 200);
 
 			particleSystem = new ParticleSystem();
 			emitter = new Emitter(1000, 2.5F, 100);
 			emitter.setLocation(-300, 250);
 			emitter.setImage(Art.getTileSheet().getImage(64, 32, 8, 8));
 			particleSystem.addEmitter(emitter);
 			emitter2 = new Emitter(1000, 2.0F, 100);
 			emitter2.setLocation(-300, 250);
 			emitter2.setImage(Art.getTileSheet().getImage(72, 32, 8, 8));
 			particleSystem.addEmitter(emitter2);
 			emitter3 = new Emitter(1000, 1.5F, 100);
 			emitter3.setLocation(-300, 250);
 			emitter3.setImage(Art.getTileSheet().getImage(80, 32, 8, 8));
 			particleSystem.addEmitter(emitter3);
 
 
 			//Effect.playEffect(EffectType.BloodEffect, 100, 200);
 			//Effect.genericParticleEffect();
 			Effect.playEffect(EffectType.BloodEffect, 500, 50);
 
 
 		}
 
 	}
 
 	@Override
 	public void render(Graphics2D g) {
 		testMap.render(g, Math.round(x), Math.round(y));
 		particleSystem.render(g);
 		g.drawImage(testFont, Math.round(x) + (-350), (int) y, testFont.getWidth(), testFont.getHeight(), null);
 
 		testent.render(g);
 		Effect.getEffect().render(g);
 
 	}
 
 	@Override
 	public void update(int delta) {
 		if (Input.getInput().isKeyDown(KeyEvent.VK_F)) {
 			toggleFullScreen();
 		}
 
 		if (Input.getInput().isKeyDown(KeyEvent.VK_A)) {
 			x += 0.1F * delta;
 		}
 
 		if (Input.getInput().isKeyDown(KeyEvent.VK_D)) {
 			x -= 0.1F * delta;
 		}
 		if (Input.getInput().isKeyDown(KeyEvent.VK_W)) {
 			y += 0.1F * delta;
 		}
 
 		if (Input.getInput().isKeyDown(KeyEvent.VK_S)) {
 			y -= 0.1F * delta;
 		}
 
 
 		particleSystem.update(x, y);
 
 
 		testent.update(delta);
 		Effect.getEffect().update(x, y);
 		Effect.getEffect().update(500, y);
 
 	}
 
 	public static void main(String[] args) {
 		Game game = new Game();
 		game.createWindow();
 	}
 
 }
