 /*******************************************************************************
  * Copyright 2011 See AUTHORS file.
  * 
  * Licensed under the Apache License, Version 2.0 (the "License");
  * you may not use this file except in compliance with the License.
  * You may obtain a copy of the License at
  * 
  *   http://www.apache.org/licenses/LICENSE-2.0
  * 
  * Unless required by applicable law or agreed to in writing, software
  * distributed under the License is distributed on an "AS IS" BASIS,
  * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and
  * limitations under the License.
  ******************************************************************************/
 
 package com.game;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.audio.Music;
 import com.badlogic.gdx.audio.Sound;
 import com.badlogic.gdx.graphics.Camera;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.math.Vector3;
 
 public class HelloWorld implements ApplicationListener, InputProcessor {
 	SpriteBatch spriteBatch;
 	Texture texture;
 	BitmapFont font;
 	Vector2 textPosition = new Vector2(100, 100);
 	Vector2 textDirection = new Vector2(1, 1);
 	RenderTree mRenderTree;
 	
 
     private Texture mTexture;
 
 	
 	//Audio
 	Music music;
 	Sound sound;
 	
 	//Camera orthographic
 	OrthographicCamera mCam;
 	private Rectangle glViewport;
 
 	static final int WIDTH  = 480;
     static final int HEIGHT = 320;
 
 	final Vector3 curr = new Vector3();
 	final Vector3 last = new Vector3(-1, -1, -1);
 	final Vector3 delta = new Vector3();
 	
 	// for pinch-to-zoom
 	int mNumberOfFingers = 0;
 	int mFingerOnePointer;
 	int mFingerTwoPointer;
 	float mLastDistance = 0;
 	Vector3 mFingerOne = new Vector3();
 	Vector3 mFingerTwo = new Vector3();
 
 	
 
 	@Override
 	public void create () {
 		Gdx.input.setInputProcessor(this);
 		font = new BitmapFont();
 		font.setColor(Color.RED);
 		texture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
 		spriteBatch = new SpriteBatch();
 		
 		mRenderTree = new RenderTree();
 		
 		//Define the audio source
 		music = Gdx.audio.newMusic(Gdx.files.internal("data/music.mp3"));
 		sound = Gdx.audio.newSound(Gdx.files.internal("data/sound.ogg"));
 		music.setLooping(true);
 		music.setVolume(0.2f);
 		music.play();
 		
 		//Define the orthographic cam
 		mCam = new OrthographicCamera(WIDTH,HEIGHT);
 		mCam.position.set(WIDTH / 2, HEIGHT / 2, 0);
 		
 		glViewport = new Rectangle(0, 0, WIDTH, HEIGHT);
 		
 		//Texture
 		mTexture = new Texture(Gdx.files.internal("data/badlogic.jpg"));
 
 
 
 		
 	}
 
 	@Override
 	public void render () {
 		mRenderTree.draw();
 		/*
 		int centerX = Gdx.graphics.getWidth() / 2;
 		int centerY = Gdx.graphics.getHeight() / 2;
 		
 		//Get the gl version instance
 		GL10 gl = Gdx.graphics.getGL10();
 			
 		//Update the cam
 		mCam.update();
         mCam.apply(gl);
 		gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		
 		
 		
         
         // Texturing --------------------- /
         gl.glActiveTexture(GL10.GL_TEXTURE0);
         gl.glEnable(GL10.GL_TEXTURE_2D);
         texture.bind();
 
 
 		// more fun but confusing :)
 		// textPosition.add(textDirection.tmp().mul(Gdx.graphics.getDeltaTime()).mul(60));
 		textPosition.x += textDirection.x * Gdx.graphics.getDeltaTime() * 60;
 		textPosition.y += textDirection.y * Gdx.graphics.getDeltaTime() * 60;
 
 		if (textPosition.x < 0) {
 			textDirection.x = -textDirection.x;
 			textPosition.x = 0;
 		}
 		if (textPosition.x > Gdx.graphics.getWidth()) {
 			textDirection.x = -textDirection.x;
 			textPosition.x = Gdx.graphics.getWidth();
 		}
 		if (textPosition.y < 0) {
 			textDirection.y = -textDirection.y;
 			textPosition.y = 0;
 		}
 		if (textPosition.y > Gdx.graphics.getHeight()) {
 			textDirection.y = -textDirection.y;
 			textPosition.y = Gdx.graphics.getHeight();
 		}
 
 		spriteBatch.begin();
 		spriteBatch.setColor(Color.WHITE);
 		/*spriteBatch.draw(texture, centerX - texture.getWidth() / 2, centerY - texture.getHeight() / 2, 0, 0, texture.getWidth(),
 			texture.getHeight());
 		
 		spriteBatch.draw(texture, 10, 10);
 		font.draw(spriteBatch, "hello", (int)textPosition.x, (int)textPosition.y);
 		spriteBatch.end();
 		*/
 		
 	}
 
 	@Override
 	public void resize (int width, int height) {
 		spriteBatch.getProjectionMatrix().setToOrtho2D(0, 0, width, height);
 		textPosition.set(0, 0);
 	}
 
 	@Override
 	public void pause () {
 		
 		//Remove number of fingers
 		mNumberOfFingers = 0;
 		
 		//Close the music and sound
 		music.dispose();
 		sound.dispose();
 
 	}
 
 	@Override
 	public void resume () {
 		create();
 		
 		//Play music and sound
 		music.play();
 		sound.play();
 
 	}
 
 	@Override
 	public void dispose () {
 		
 		//Close the music and sound
 		music.dispose();
 		sound.dispose();
 
 	}
 
 	@Override
 	public boolean keyDown(int arg0) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean keyTyped(char arg0) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean keyUp(int arg0) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean scrolled(int arg0) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchDown(int x, int y, int pointer, int button) {
 		Vector2 touchPosition = new Vector2(x, y);
 		//Play sound effect
 		
 		
 		//For pinch-to-zoom
 		mNumberOfFingers++;
 		if(mNumberOfFingers == 1)
 		{
 		       mFingerOnePointer = pointer;
 		       mFingerOne.set(x, y, 0);
 		}
 		else if(mNumberOfFingers == 2)
 		{
 		       mFingerTwoPointer = pointer;
 		       mFingerTwo.set(x, y, 0);
 		
 		       float distance = mFingerOne.dst(mFingerTwo);
 		       mLastDistance = distance;
 		       
 		       //Play sound because second finger is pressed
 		       sound.play();
 		       
 		 }
 	
 		return false;
 	}
 
 	@Override
 	public boolean touchDragged(int x, int y, int pointer) {
 		
		mCam.zoom = -3f;
 		return false;
 	}
 
 	@Override
 	public boolean touchMoved(int arg0, int arg1) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean touchUp(int x, int y, int pointer, int button) {
 		
 		//For pinch-to-zoom           
 		if(mNumberOfFingers == 1)
 		{
 		       Vector3 touchPoint = new Vector3(x, y, 0);
 		       //cam.unproject(touchPoint);
 		}
 		mNumberOfFingers--;
 		
 		// Remove number of fingers on the screen... clamping number of fingers (ouch! :-)
 		if(mNumberOfFingers < 0){
 		       mNumberOfFingers = 0;
 		}
 		
 		//Put back the last position for the orthographic cam
 		last.set(-1, -1, -1);
 		
 		return false;
 	}
 	
 	
 
 }
