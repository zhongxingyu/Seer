 package com.musicgame.Demo;
 
 
 import java.util.Iterator;
 
 //import com.badlogic.gdx.Application.ApplicationType;
 import com.badlogic.gdx.Gdx;
 //import com.badlogic.gdx.Input.Keys;
 //import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Screen;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.VertexAttributes.Usage;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.glutils.ShaderProgram;
 import com.badlogic.gdx.math.MathUtils;
 import com.badlogic.gdx.math.Polygon;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.utils.Array;
 import com.badlogic.gdx.utils.TimeUtils;
 
 import com.badlogic.gdx.audio.io.*;
 
 import com.badlogic.gdx.graphics.*;
 import com.musicgame.PumpAndJump.Player;
 
 public class GameScreen implements Screen, InputProcessor {
 	//private World world;
 
 	Texture dropImage;
 	Texture notedudeImage;
 	Sound dropSound;
 	Music rainMusic;
 	SpriteBatch batch;
 	OrthographicCamera camera;
 	Rectangle bucket;
 	Array<Rectangle> raindrops;
 	long lastDropTime;
 	boolean isjumping=false;
 	boolean isfalling=false;
 	int bucket_initialY=20;
 	int bucket_initialX=20;
 
 	Decoder mysounddecoder;
 	short[] mysoundsamples;
 	int soundbuffersize;
 	boolean ismoresoundsamples;
 
 	private Mesh waveFormMesh;
 	private float[] waveFormVertices;
 
 	@Override
 	public void show()
 	{
 			//world = new World();
 
 		      // load the images for the droplet and the bucket, 48x48 pixels each
 		      dropImage = new Texture(Gdx.files.internal("droplet.png"));
 		      notedudeImage = new Texture(Gdx.files.internal("notedude_walk_animation.png"));
 
 		      // load the drop sound effect and the rain background "music"
 		     //dropSound = Gdx.audio.newSound(Gdx.files.internal("drop.wav"));
 		      dropSound = Gdx.audio.newSound(Gdx.files.internal("kendrum.mp3"));
 		      rainMusic = Gdx.audio.newMusic(Gdx.files.internal("rain.mp3"));
 
 		      // start the playback of the background music immediately
 		      rainMusic.setLooping(true);
 		      rainMusic.play();
 
 		      mysounddecoder = new WavDecoder(Gdx.files.internal("drop.wav"));
 
 		      // create the camera and the SpriteBatch
 		      camera = new OrthographicCamera();
 		      camera.setToOrtho(false, 800, 480);
 		      //camera.update();
 
 		      batch = new SpriteBatch();
 
 		      // create a Rectangle to logically represent the bucket
 		      bucket = new Rectangle();
 		      bucket.x = bucket_initialX; // center the bucket horizontally
 		      bucket.y = bucket_initialY; // bottom left corner of the bucket is 20 pixels above the bottom screen edge
 		      bucket.width = 32;
 		      bucket.height = 32;
 
 		      // create the raindrops array and spawn the first raindrop
 		      raindrops = new Array<Rectangle>();
 
 		      makeTrack();
 		// TODO Auto-generated method stub
 	}
 	void printWaveFormVertices()
 	{
 		   Gdx.app.log("waveFormVertices: ", waveFormVertices.toString()+" len: "+waveFormVertices.length);
 		   for(int i=0; i<waveFormVertices.length;i+=3){
 			   Gdx.app.log("\t"+i, "("+waveFormVertices[i]+", "+waveFormVertices[i+1]+", "+waveFormVertices[i+2]+")");
 		   }
 	}
 
 	private void makePlayer()
 	{
		Player testPlayer = new Player(new Polygon(new float[]{0,0,10,10,20,20,10,20,20,10}));
 	}
 
 	   private void makeTrack() {
 
 		   mysoundsamples = mysounddecoder.readAllSamples();
 		   int startx=10;
 		   int numsamples=mysoundsamples.length;
 		   waveFormVertices = new float[numsamples*3*2];
 		   for(int i=0; i < numsamples;i++)
 		   {
 			   waveFormVertices[6*i] = i+startx; //x
 			   waveFormVertices[6*i+1] = 200;//y
 			   waveFormVertices[6*i+2] = 0;
 
 			   waveFormVertices[6*i+3] = i+startx; //x
 			   waveFormVertices[6*i+4] = 200+(mysoundsamples[i]/256f);//y
 			   waveFormVertices[6*i+5] = 0;
 		   }
 		   //printWaveFormVertices();
 
            waveFormMesh = new Mesh(true, waveFormVertices.length, 0,
         		   new VertexAttribute( Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE));
 
            waveFormMesh.setVertices(waveFormVertices);
 
 		   spawnRaindropAt(100,20);
 		   spawnRaindropAt(200,20);
 		   spawnRaindropAt(300,20);
 		   spawnRaindropAt(350,20);
 		   spawnRaindropAt(400,20);
 		   spawnRaindropAt(500,20);
 		   spawnRaindropAt(600,20);
 		   spawnRaindropAt(700,20);
 		   spawnRaindropAt(800,20);
 		   spawnRaindropAt(900,20);
 		   spawnRaindropAt(1000,20);
 
 	   }
 	   public Mesh createFullScreenQuad() {
 
 		   float[] verts = new float[20];
 		   int i = 0;
 
 		   verts[i++] = -1; // x1
 		   verts[i++] = -1; // y1
 		   verts[i++] = 0;
 		   verts[i++] = 0f; // u1
 		   verts[i++] = 0f; // v1
 
 		   verts[i++] = 1f; // x2
 		   verts[i++] = -1; // y2
 		   verts[i++] = 0;
 		   verts[i++] = 1f; // u2
 		   verts[i++] = 0f; // v2
 
 		   verts[i++] = 1f; // x3
 		   verts[i++] = 1f; // y2
 		   verts[i++] = 0;
 		   verts[i++] = 1f; // u3
 		   verts[i++] = 1f; // v3
 
 		   verts[i++] = -1; // x4
 		   verts[i++] = 1f; // y4
 		   verts[i++] = 0;
 		   verts[i++] = 0f; // u4
 		   verts[i++] = 1f; // v4
 
 		   Mesh mesh = new Mesh( true, 4, 0,
 		     new VertexAttribute( Usage.Position, 3, ShaderProgram.POSITION_ATTRIBUTE ),
 		     new VertexAttribute( Usage.TextureCoordinates, 2, ShaderProgram.TEXCOORD_ATTRIBUTE+"0" ) );
 
 		   mesh.setVertices( verts );
 		   return mesh;
 		 }
 	   @SuppressWarnings("unused")
 	   private void spawnRaindrop() {
 	         Rectangle raindrop = new Rectangle();
 	         raindrop.x = MathUtils.random(0, 800-32);
 	         raindrop.y = 20;
 	         raindrop.width = 32;
 	         raindrop.height = 32;
 	         raindrops.add(raindrop);
 	         lastDropTime = TimeUtils.nanoTime();
 	      }
 
 	      private void spawnRaindropAt(int x, int y) {
 	   	      Rectangle raindrop = new Rectangle();
 	   	      raindrop.x = x;
 	   	      raindrop.y = y;
 	   	      raindrop.width = 32;
 	   	      raindrop.height = 32;
 	   	      raindrops.add(raindrop);
 	   	      lastDropTime = TimeUtils.nanoTime();
 	   	   }
 	@Override
 	public boolean keyDown(int keycode) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean keyUp(int keycode) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char character) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int screenX, int screenY, int pointer, int button) {
 		// TODO Auto-generated method stub
    	 	if(!this.isjumping && bucket.y <= this.bucket_initialY)
    	 		this.isjumping=true;
 
 		return false;
 	}
 
 	@Override
 	public boolean touchUp(int screenX, int screenY, int pointer, int button) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchDragged(int screenX, int screenY, int pointer) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean mouseMoved(int screenX, int screenY) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int amount) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void render(float delta) {
 		//Gdx.app.log( "hi", "GameScreen.render\n" );
 
 		// TODO Auto-generated method stub
 	      // clear the screen with a dark blue color. The
 	      // arguments to glClearColor are the red, green
 	      // blue and alpha component in the range [0,1]
 	      // of the color to be used to clear the screen.
 	      Gdx.gl.glClearColor(0, 0, 0.2f, 1);
 	      Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 
 	      // tell the camera to update its matrices.
 	      if(bucket.x > camera.position.x)
 	    	  camera.position.x = bucket.x;
 	      camera.update();
 
 	      // tell the SpriteBatch to render in the
 	      // coordinate system specified by the camera.
 	      batch.setProjectionMatrix(camera.combined);
 
 	      waveFormMesh.render(GL10.GL_LINES);
 
 	      // begin a new batch and draw the bucket and
 	      // all drops
 	      batch.begin();
 	      batch.draw(notedudeImage, bucket.x, bucket.y);
 	      for(Rectangle raindrop: raindrops) {
 	         batch.draw(dropImage, raindrop.x, raindrop.y);
 	      }
 	      batch.end();
 
 
 
 
 	      if(this.isjumping)
 	      {
 	    	  bucket.y += 100 * Gdx.graphics.getDeltaTime();
 	    	  if(bucket.y > (2*bucket.height+this.bucket_initialY) )
 	    	  {
 	    		  	this.isfalling=true;
 	    	  		this.isjumping=false;
 	    	  }
 	      }
 	      else{
 	    	  if(bucket.y > this.bucket_initialY)
 			  	bucket.y -= 100 * Gdx.graphics.getDeltaTime();
 		  	else if(bucket.y < this.bucket_initialY)
 		  		bucket.y = this.bucket_initialY;
 	      }
 
 
 	      bucket.x += 200 * Gdx.graphics.getDeltaTime();
 
 
 	      //make sure the bucket stays within the screen bounds
 	      //if(bucket.x < 0) bucket.x = 0;
 	      //if(bucket.x > 800 - 32) bucket.x = 800 - 32;
 	      //move bucket back to start when it reaches the end
 	      /*if(bucket.x > 450 - 32){
 	    	  bucket.x = 0;
 	    	  makeTrack();
 	      }*/
 	      // check if we need to create a new raindrop
 	      //if(TimeUtils.nanoTime() - lastDropTime > 1000000000) spawnRaindrop();
 
 	      // move the raindrops, remove any that are beneath the bottom edge of
 	      // the screen or that hit the bucket. In the later case we play back
 	      // a sound effect as well.
 	      Iterator<Rectangle> iter = raindrops.iterator();
 	      while(iter.hasNext()) {
 	         Rectangle raindrop = iter.next();
 	         //raindrop.y -= 200 * Gdx.graphics.getDeltaTime();
 	         //if(raindrop.y + 32 < 0) iter.remove();
 	         if(raindrop.overlaps(bucket)) {
 	            dropSound.play();
 	            iter.remove();
 	         }
 	      }
 
 
 	}
 
 	@Override
 	public void resize(int width, int height) {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void hide() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void pause() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void resume() {
 		// TODO Auto-generated method stub
 
 	}
 
 	@Override
 	public void dispose() {
 		// TODO Auto-generated method stub
 	      // dispose of all the native resources
 	      dropImage.dispose();
 	      notedudeImage.dispose();
 	      dropSound.dispose();
 	      rainMusic.dispose();
 	      batch.dispose();
 	}
 
 }
