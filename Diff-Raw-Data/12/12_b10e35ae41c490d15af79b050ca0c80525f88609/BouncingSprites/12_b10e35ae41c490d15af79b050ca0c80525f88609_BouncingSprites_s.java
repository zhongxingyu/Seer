 package chum.examples;
 
 import chum.engine.GameActivity;
 import chum.engine.GameEvent;
 import chum.engine.GameNode;
 import chum.engine.GameSequence;
 import chum.engine.common.FPSNode;
 import chum.f.Vec3;
 import chum.gl.Color;
 import chum.gl.Font;
 import chum.gl.RenderNode;
 import chum.gl.SpriteBatch;
 import chum.gl.SpriteBatchBuilder;
 import chum.gl.SpriteSheet;
 import chum.gl.render.ClearNode;
 import chum.gl.render.SaveMatrixNode;
 import chum.gl.render.Sprite;
 import chum.gl.render.Standard2DNode;
 import chum.gl.render.TextNode;
 import chum.input.TouchInputNode;
 import chum.sound.SoundManager;
 
 import android.graphics.Typeface;
 import android.os.Bundle;
 import android.view.MotionEvent;
 
 import javax.microedition.khronos.opengles.GL10;
 
 
 
 /**
    Draw a pyramid and spin it around various axes
 */
 public class BouncingSprites extends GameActivity
 {
     SpriteSheet sheet;
     SpriteBatch batch;
 
     SoundManager sound;
     RenderNode spritesNode;
     TextNode text;
     
     int number = 0;
     
     static final int TOUCH = 1;
     static final int BOUNCE = 2;
     
 
     /** Keep track of the original title string, so it can be updated (appended) */
     CharSequence origTitle;
 
 
     @Override
     public void setViewOptions() {
     	this.hideTitlebar = false;
     }
     
     
     @Override
     public void onCreate(Bundle savedInstanceState) {
         super.onCreate(savedInstanceState);
         origTitle = getTitle();
 
         sound = new SoundManager(this);
         sound.loadSound(R.raw.bonk,R.raw.bonk);
         
         number = 0;
     }
 
 
     // The logic tree consists of two nodes:
     // - one to move the sprites
     // - one to periodically display the FPS
     @Override
     protected GameNode createLogicTree() {
         GameNode node = new GameNode();
         
         node.addNode(new GameNode(){
             @Override
             public boolean update(long millis) {
                 move(millis);
                 return true;
             }
         });
         
         node.addNode(new FPSNode(){
             @Override
             public void showFPS() {
                 super.showFPS();
                 showFPSInTitle();
             }
         });
         
         node.addNode(new TouchInputNode());
 
         return node;
     }
 
     /**
        The render tree consists of:
        - the root node is a 2D projection
        - ClearNode to clear the scene
        - Parent node for all the sprites
        - A text node to display the number when sprites are added
      */
     @Override
     protected RenderNode createRenderTree(GameNode logic) {
         Standard2DNode base = new chum.gl.render.Standard2DNode();
         base.addNode(new ClearNode(Color.WHITE));
                                         
         spritesNode = new RenderNode();
         base.addNode(spritesNode.setName("sprites"));
         
         SaveMatrixNode save = new SaveMatrixNode();
         
         text = new TextNode();
         text.setColor(Color.BLACK);
         text.setPosition(new Vec3());
         text.visible = false;
         save.addNode(text);
         base.addNode(save);
         
         return base;
     }
 
 
     @Override
     public void onSurfaceChanged(int width, int height) {
         super.onSurfaceChanged(width,height);
         
         loadSprites();
 
         Font font = new Font(renderContext,Typeface.DEFAULT_BOLD,30);
         text.setText(font.buildText("#####"));
 
         addSprite(Color.RED,renderContext.width/2f,renderContext.height/2f);
     }
 
         
     @Override
     public boolean onGameEvent(GameEvent event) {
         switch(event.type) {
         case GameEvent.INPUT_TOUCH:
             MotionEvent ev = (MotionEvent)event.object;
             if ( ev.getAction() == MotionEvent.ACTION_DOWN ) {
                 Color color = BouncySprite.colors
                    [gameController.random.nextInt(BouncySprite.colors.length)];
                 addSprite(color,ev.getX(),renderContext.height - ev.getY());
             }   
             return true;
         
             
         case BOUNCE:
             sound.playSound(R.raw.bonk);
             ((BouncySprite)event.object).bounce();
             return true;
             
         }
         
         return false;
     }
 
     
     protected void loadSprites() {
         BouncySprite.setMargin(renderContext.width,renderContext.height);
 
         SpriteSheet.Loader loader = new SpriteSheet.Loader(renderContext);
         sheet = loader.loadFromAssets("sprites/sprites.png",
                                       "sprites/sprites.xml");
         
         batch = new SpriteBatch(sheet,sheet.data.length);
         SpriteBatchBuilder builder = new SpriteBatchBuilder(batch);
         for(int i=0; i<sheet.data.length; ++i) {
             builder.add(sheet.data[i], 0, 0);
         }
         builder.build();
         batch.adjustOrigin(0,sheet.data.length,SpriteBatch.ADJUST_CENTER); // TODO: need an anchor option in builder.add()
     }
 
     
     protected void addSprite(Color color, float x, float y) {
        int shape = gameController.random.nextInt(batch.maxSprites);
         BouncySprite sprite = new BouncySprite(batch,color,shape);
         sprite.setPosition(new Vec3(x,y,0));
 
         while ( spritesNode.num_children >= 5 ) {
             BouncySprite old = (BouncySprite)spritesNode.children[0];
             old.remove();
             //old.recycle();
         }
 
         spritesNode.addNode(sprite);
         
         // Choose random direction:
        float speed = 0.08f + gameController.random.nextFloat() * 0.15f;        
        double angle = gameController.random.nextDouble() * 3.1416;
 
         sprite.velocity.set(speed * (float)Math.cos(angle),
                             speed * (float)Math.sin(angle),
                             0);
 
         number++;
         
         //chum.util.Log.d("sprite %s: angle=%d, speed=%.2f, velo=%s", sprite,
         //                (int)angle, speed, sprite.velocity);
         
         // Also add text:
         text.text.setString("#"+number);
         text.visible = true;
 
         GameSequence anim = new GameSequence.Parallel(){
             @Override
             protected void postEnd() { text.visible = false; } 
         };
 
         Vec3 to = new Vec3(sprite.position);
         to.x += 10;
         to.y += 30;
         
         anim.addNode(text.animatePosition(sprite.position,to,2000));
         anim.addNode(text.animateScale(1f,2f,2000));
         anim.addNode(text.animateAlpha(1f,0f,2000));
         text.addNode(anim);
     }
     
 
     protected void move(long millis) {
         for (int i=0,num=spritesNode.num_children;i<num;++i) {
             BouncySprite bs = (BouncySprite)spritesNode.children[i];
             if ( bs.move(millis) )
                 tree.postUp(GameEvent.obtain(BOUNCE,bs));
         }
     }
 
 
 
     Runnable updateFPS = new Runnable(){
         public void run() {
             setTitle(""+origTitle + " -- "+gameController.getFPS()+"fps");
         }
     };
     
 
     protected void showFPSInTitle() {
         gameController.uiHandler.post(updateFPS);
     }
 
     
     
     public static class BouncySprite extends Sprite {
 
         static final float MARGIN = 20f;
         static float minx, miny, maxx, maxy;
         
         public Color color;
         public Vec3 velocity = new Vec3();
         
         public BouncySprite(SpriteBatch batch, Color color,int shape) {
             super(batch);
             this.color = color;
             setImage(shape,1,false);
         }
         
         
         public boolean move(long millis) {
             boolean bounce = false;
             
             position.x += velocity.x * millis;
             position.y += velocity.y * millis;
 
             if ( position.x < minx||
                  position.x > maxx||
                  position.y < miny ||
                  position.y > maxy ) {
                 bounce = true;
             }
 
             angle += (velocity.x + velocity.y) * millis;
             
             return bounce;
         }
         
         public void bounce() {
             if ( position.x < minx ) {
                 position.x = minx;
                 if ( velocity.x < 0 ) velocity.x *= -1;
             }
             
             if ( position.x > maxx) {
                 position.x = maxx;
                 if ( velocity.x > 0 ) velocity.x *= -1;
             }
 
             if ( position.y < miny ) {
                 position.y = miny;
                 if ( velocity.y < 0 ) velocity.y *= -1;
             }
 
             if ( position.y > maxy ) {
                 position.y = maxy;
                 if ( velocity.y > 0 ) velocity.y *= -1;
             }
         }
 
 
         public static void setMargin(int width,int height) {
             minx = miny = MARGIN;
             maxx = width - MARGIN;
             maxy = height - MARGIN;
         }
 
         
         @Override
         public void renderPrefix(GL10 gl) {
             gl.glColor4f(color.red,color.green,color.blue,color.alpha);
             super.renderPrefix(gl);
         }
 
         
         public static Color[] colors = {
             Color.RED,
             Color.GREEN,
             Color.BLUE,
             Color.BLACK,
             new Color("#ffff00"),
             new Color("#ff00ff"),
             new Color("#00ffff")
         };
     }
 
 }
 
