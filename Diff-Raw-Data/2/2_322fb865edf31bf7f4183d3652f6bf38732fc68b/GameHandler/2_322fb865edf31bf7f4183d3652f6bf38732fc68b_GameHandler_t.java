 package net.littlecoder.core;
 
 import static playn.core.PlayN.*;
 
 import static net.littlecoder.core.ImageHelper.*;
 
 import playn.core.*;
 
// TODO: Move rendering of dynamic texts to ImageHelper
 // TODO: Move Game Over part to single class
 class GameHandler implements Keyboard.Listener, ImmediateLayer.Renderer {
 
     private static final float SMALL_FONT_SIZE = 15f;
     private static final float LARGE_FONT_SIZE = 45f;
     private static final float TIME_BETWEEN_LEVELS = 3000f;
     private static final float BLINK_INTERVAL = 2000f;
 
     private int width;
     private  int height;
 
     private Ship ship;
     private BulletManager bulletManager;
     private AsteroidManager asteroidManager;
 
     private boolean shooting = false;
 
     private byte lifes = 0;
     private int score = 0;
     private int level = 0;
 
     private TextFormat smallTextFormat;
     private CanvasImage scoreImage;
     private CanvasImage levelImage;
     private CanvasImage nextLevelImage;
     private CanvasImage gameOverImage;
     private CanvasImage pressFireImage;
 
     private Polyline shipPolyline;
 
     private float timeToNextLevel = TIME_BETWEEN_LEVELS;
 
     private float blinkTime = 0f;
 
     public GameHandler(int width, int height) {
         this.width = width;
         this.height = height;
 
         initTexts();
 
         ship = new Ship(width, height);
         bulletManager = new BulletManager(width, height);
         asteroidManager = new AsteroidManager(width, height);
 
         shipPolyline = Ship.shipPolyline.clone();
 
         keyboard().setListener(this);
     }
 
     @Override
     public void render(Surface surface) {
         surface.clear();
 
         if (!isGameOver()) {
             ship.paint(surface);
 
             bulletManager.paint(surface);
             asteroidManager.paint(surface);
 
             for (int l = 0; l < lifes; l++)
                 shipPolyline.transform(0f, 20f + l * 20f, 20f).paint(surface);
 
         } else
             paintGameOver(surface);
 
         paintScore(surface);
         paintLevel(surface);
     }
 
     public void update(float delta) {
         if (!isGameOver()) {
             ship.update(delta);
             updateBullets(delta);
             asteroidManager.update(delta);
             detectCollisions(delta);
             advanceLevel(delta);
         } else
             blinkTime = (blinkTime + delta) % BLINK_INTERVAL;
     }
 
     public void onKeyDown(Keyboard.Event event) {
         if (event.key() == Key.UP)
             ship.accelerate(true);
         if (event.key() == Key.RIGHT)
             ship.steerRight(true);
         if (event.key() == Key.LEFT)
             ship.steerLeft(true);
 
         if (event.key() == Key.SPACE)
             if (isGameOver())
                 initLevel(1);
             else if (!ship.isDead())
                 shooting = !ship.isDisabled() && true;
             else
                 ship.reinitialize(width, height);
 
         if (event.key() == Key.ESCAPE)
             System.exit(0);
     }
 
     public void onKeyUp(Keyboard.Event event) {
         if (event.key() == Key.UP)
             ship.accelerate(false);
         if (event.key() == Key.RIGHT)
             ship.steerRight(false);
         if (event.key() == Key.LEFT)
             ship.steerLeft(false);
     }
 
     public void onKeyTyped(Keyboard.TypedEvent event) {
     }
 
     private void updateBullets(float delta) {
         bulletManager.update(delta);
         if (shooting) {
             bulletManager.addBullet(ship);
             shooting = false;
         }
     }
 
     private void detectCollisions(float delta) {
         score += bulletManager.detectCollisions(asteroidManager);
 
         if (asteroidManager.isCollidingWith(ship)) {
             ship.die();
             lifes--;
         }
     }
 
     private void paintScore(Surface surface) {
         String scoreText = String.valueOf(score);
         if (score < 10)
             scoreText = "0" + scoreText;
         if (score < 100)
             scoreText = "0" + scoreText;
         if (score < 1000)
             scoreText = "0" + scoreText;
         if (score < 10000)
             scoreText = "0" + scoreText;
 
         TextLayout layout = graphics().layoutText(scoreText, smallTextFormat);
 
         scoreImage.canvas().clear();
         scoreImage.canvas().drawText(layout, 0, 0);
         surface.drawImage(scoreImage, surface.width() - layout.width() - 10, 10f);
     }
 
     private void paintLevel(Surface surface) {
         String text = String.valueOf(level);
         if (level < 10)
             text = "0" + text;
 
         TextLayout layout = graphics().layoutText(text, smallTextFormat);
         levelImage.canvas().clear();
         levelImage.canvas().drawText(layout, 0, 0);
         surface.drawImage(levelImage, surface.width() / 2f - layout.width(), 10f);
 
         if (asteroidManager.isEmpty() && !isGameOver()) {
             text = "Next Level in\n\n" + (int) Math.ceil(timeToNextLevel / 1000) + " Sec";
             layout = graphics().layoutText(text, smallTextFormat);
             nextLevelImage.canvas().clear();
             nextLevelImage.canvas().drawText(layout, 0, 0);
             float y = surface.height() / 4f;
             if (ship.y < surface.height() / 2f)
                 y += surface.height() / 2f;
             surface.drawImage(
                     nextLevelImage, (surface.width() - layout.width()) / 2f, y
             );
         }
     }
 
     private void paintGameOver(Surface surface) {
         surface.drawImage(
                 gameOverImage,
                 (surface.width() - gameOverImage.width()) / 2f,
                 (surface.height() / 3f - gameOverImage.height() / 2f)
         );
 
         if (blinkTime < BLINK_INTERVAL / 2f)
             surface.drawImage(
                     pressFireImage,
                     (surface.width() - pressFireImage.width()) / 2f,
                     (surface.height() / 3f * 2f - pressFireImage.height() / 2f)
             );
     }
 
     private void initTexts() {
         Font smallFont = graphics().createFont(
                 "Vector Battle", Font.Style.PLAIN, SMALL_FONT_SIZE
         );
         Font largeFont = graphics().createFont(
                 "Vector Battle", Font.Style.BOLD, LARGE_FONT_SIZE
         );
         TextFormat.Alignment a = TextFormat.Alignment.CENTER;
         int c = 0xFFFFFFFF;
 
         smallTextFormat = new TextFormat().
                 withFont(smallFont).
                 withAlignment(a).
                 withTextColor(c);
 
         levelImage = createTextImage("00", smallFont, a, c);
         scoreImage = createTextImage("00000", smallFont, a, c);
         nextLevelImage = createTextImage("Next Level in\n\n0 Sec", smallFont, a, c);
         pressFireImage = createTextImage("Press Fire to Start", smallFont, a, c);
         gameOverImage = createTextImage("Game Over", largeFont, a, c);
     }
 
     private void initLevel(int level) {
         this.level = level;
         if (level == 1) {
             lifes = 3;
             score = 0;
         }
 
         asteroidManager.initLevel(level);
     }
 
     private void advanceLevel(float delta) {
         if (asteroidManager.isEmpty()) {
             timeToNextLevel -= delta;
             if (timeToNextLevel < 0f) {
                 level++;
                 initLevel(level);
                 timeToNextLevel = TIME_BETWEEN_LEVELS;
             }
         }
     }
 
     private boolean isGameOver() {
         return level == 0 || lifes == 0 && ship.isDead();
     }
 }
