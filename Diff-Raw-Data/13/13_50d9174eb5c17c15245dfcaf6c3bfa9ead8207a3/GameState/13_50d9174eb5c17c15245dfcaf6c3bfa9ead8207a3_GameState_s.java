 package breakout;
 
 import static org.lwjgl.opengl.GL11.GL_COLOR_BUFFER_BIT;
 import static org.lwjgl.opengl.GL11.GL_DEPTH_BUFFER_BIT;
 import static org.lwjgl.opengl.GL11.GL_QUADS;
 import static org.lwjgl.opengl.GL11.glBegin;
 import static org.lwjgl.opengl.GL11.glClear;
 import static org.lwjgl.opengl.GL11.glEnd;
 import static org.lwjgl.opengl.GL11.glLoadIdentity;
 import static org.lwjgl.opengl.GL11.glTexCoord2f;
 import static org.lwjgl.opengl.GL11.glVertex2f;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.lwjgl.opengl.Display;
 import org.newdawn.slick.Color;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 import org.newdawn.slick.util.ResourceLoader;
 
 import particles.Particles;
 import particles.SimpleExplosion;
 import util.LevelLoader;
 import breakout.Block.BlockState;
 import breakout.Block.BlockType;
 import engine.IGameState;
 import engine.Texts;
 
 public class GameState implements IGameState {
 
 	public static final String name = "STATE_GAME";
 
 	private Texture bgTexture;
 	private Paddle paddle;
 	private List<Ball> balls;
 	private List<Block> blocks;
 	private List<Particles> particles;
 	private List<Coin> coins;
 	private Texture[] coinTextures;
 	private Texts texts;
 
 	@Override
 	public String getName() {
 		return name;
 	}
 
 	@Override
 	public void init() {
 		try {
 			loadTextures();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		paddle = new Paddle(100, 10, Display.getWidth() / 6,
 				Display.getHeight() / 20);
 		balls = new ArrayList<Ball>();
 		blocks = LevelLoader.load("resources/levels/level1-1.txt", Hud.height);
 		particles = new ArrayList<Particles>();
 		coins = new ArrayList<Coin>();
 		texts = new Texts();
 		spawnBall();
 	}
 
 	private void loadTextures() throws IOException {
 		bgTexture = TextureLoader.getTexture("PNG", ResourceLoader
 				.getResourceAsStream("resources/images/cloudsinthedesert.png"));
 		coinTextures = new Texture[4];
 		coinTextures[0] = TextureLoader.getTexture("PNG", ResourceLoader
 				.getResourceAsStream("resources/images/coinBlue.png"));
 		coinTextures[1] = TextureLoader.getTexture("PNG", ResourceLoader
 				.getResourceAsStream("resources/images/coinRed.png"));
 		coinTextures[2] = TextureLoader.getTexture("PNG", ResourceLoader
 				.getResourceAsStream("resources/images/coinGreen.png"));
 		coinTextures[3] = TextureLoader.getTexture("PNG", ResourceLoader
 				.getResourceAsStream("resources/images/coinGrey.png"));
 	}
 
 	@Override
 	public void start() {
 
 	}
 
 	@Override
 	public void stop() {
 
 	}
 
 	@Override
 	public void render(int delta) {
 		glLoadIdentity();
 		glClear(GL_COLOR_BUFFER_BIT | GL_DEPTH_BUFFER_BIT);
 
 		renderBackground(delta);
 
 		paddle.render();
 
 		for (Ball ball : balls) {
 			ball.render();
 		}
 
 		for (Block block : blocks) {
 			block.render();
 		}
 
 		for (Particles p : particles) {
 			p.render();
 		}
 
 		for (Coin c : coins) {
 			c.render();
 		}
 
 		texts.render();
 
 		Hud.get().render();
 	}
 
 	private void renderBackground(float delta) {
 		bgTexture.bind();
 		Color.white.bind();
 		glBegin(GL_QUADS);
 		glTexCoord2f(0.0f, bgTexture.getHeight());
 		glVertex2f(0.0f, 0.0f);
 		glTexCoord2f(bgTexture.getWidth(), bgTexture.getHeight());
 		glVertex2f(Display.getWidth(), 0.0f);
 		glTexCoord2f(bgTexture.getWidth(), 0.0f);
 		glVertex2f(Display.getWidth(), Display.getHeight());
 		glTexCoord2f(0.0f, 0.0f);
 		glVertex2f(0.0f, Display.getHeight());
 		glEnd();
 	}
 
 	@Override
 	public void update(int delta) {
 		// update paddle
 		paddle.update(delta);
 
 		// update balls
 		for (int i = 0; i < balls.size(); i++) {
 			Ball ball = balls.get(i);
 			if (ball.isAlive()) {
 				ball.update(delta, paddle);
 			} else {
 				balls.remove(i);
 				i--;
 				if (balls.isEmpty()) {
 					Hud.get().addLives(-1);
 					spawnBall();
 				}
 			}
 		}
 
 		// update blocks
 		for (int i = 0; i < blocks.size(); i++) {
 			Block block = blocks.get(i);
 			if (block.getState().equals(BlockState.ALIVE) && block.isHit(balls)) {
 				block.onHit();
 				spawnParticles(block);
 				spawnCoin(block);
 			} else if (block.getState().equals(BlockState.DEAD)) {
 				blocks.remove(block);
 				i--;
 				break;
 			} else {
 				block.update(delta);
 			}
 		}
 
 		// update particles
 		for (int i = 0; i < particles.size(); i++) {
 			Particles p = particles.get(i);
 			if (p.isAlive()) {
 				p.update(delta, paddle, texts);
 			} else {
 				particles.remove(i);
 				i--;
 			}
 		}
 
 		// update coins
 		for (int i = 0; i < coins.size(); i++) {
 			Coin c = coins.get(i);
 			if (c.isAlive()) {
 				if (c.isHit(paddle)) {
 					Hud.get().addPoints(10);
 					texts.add("+10", c.getX(), c.getY(), 30, true);
 					doPowerup(c);
 					coins.remove(i);
 					i--;
 				} else {
 					c.update(delta);
 				}
 			} else {
 				coins.remove(i);
 				i--;
 			}
 		}
 
 		// update texts
 		texts.update(delta);
 
 		// update HUD
 		Hud.get().update(delta);
 	}
 
 	private void doPowerup(Coin coin) {
 		switch (coin.getType()) {
 		case GREY_FACE:
 			spawnBall();
 			break;
 		case RED_FACE:
 			Hud.get().addLives(1);
 			break;
 		case BLUE_FACE:
 			paddle.setWidth(paddle.getWidth() + Display.getWidth() / 60.0f);
 			break;
 		case GREEN_FACE:
 			spawnParticles(coin);
 			break;
 		default:
 			break;
 		}
 	}
 
 	private void spawnBall() {
 		balls.add(new Ball(Display.getWidth() / 2, Display.getHeight() / 2,
 				Display.getWidth() / 30, 0.0f, -Display.getHeight() / 6));
 	}
 
 	private void spawnParticles(Block block) {
 		particles.add(new SimpleExplosion(5, block.getTexture(), block.getX()
 				+ 0.5f * Block.width, block.getY() - 0.5f * Block.height, 0.4f,
 				0.4f, 0.4f, 1.0f, 1.0f, 1.0f));
 	}
 	
 	private void spawnParticles(Coin coin) {
 		particles.add(new SimpleExplosion(100, coin.getTexture(), coin.getX()
 				+ 0.5f * Block.width, coin.getY() - 0.5f * Block.height, 0.4f,
 				0.4f, 0.4f, 1.0f, 1.0f, 1.0f));
 	}
 
 	private void spawnCoin(Block block) {
 		BlockType type = block.getType();
 		switch (type) {
 		case BLUE_FACE:
 			coins.add(new Coin(coinTextures[0], type, block.getX(), block
 					.getY(), Block.width, Block.height, 0.0f, -40.0f));
 			break;
 		case RED_FACE:
 			coins.add(new Coin(coinTextures[1], type, block.getX(), block
 					.getY(), Block.width, Block.height, 0.0f, -40.0f));
 			break;
 		case GREEN_FACE:
 			coins.add(new Coin(coinTextures[2], type, block.getX(), block
 					.getY(), Block.width, Block.height, 0.0f, -40.0f));
 			break;
 		case GREY_FACE:
 			coins.add(new Coin(coinTextures[3], type, block.getX(), block
 					.getY(), Block.width, Block.height, 0.0f, -40.0f));
 			break;
 		default:
 			break;
 		}
 	}
 
 }
