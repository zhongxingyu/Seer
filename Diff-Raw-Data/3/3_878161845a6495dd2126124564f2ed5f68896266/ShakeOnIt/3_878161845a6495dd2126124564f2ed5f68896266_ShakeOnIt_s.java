 package co.sidhant.shakeonit;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.Input.TextInputListener;
 import com.badlogic.gdx.InputProcessor;
 import com.badlogic.gdx.Preferences;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 
 public class ShakeOnIt implements ApplicationListener {
 	private OrthographicCamera camera;
 	private SpriteBatch myBatch;
 	private float selectorPadding;
 	private float[] title;
 	private float[] selectors;
 	private float[] titleTarget;
 	private float[] selectorsTarget;
 	private Texture titleTex;
 	private Texture selecTex;
 	private Texture startTex;
 	private Texture settingsTex;
 	private TextureRegion titleReg;
 	private TextureRegion selecReg;
 	private int breathCount;
 	private float breathDir;
 	private boolean nameExists;
 	private String name;
 	private BitmapFont shakeFont;
 	private float textPosX;
 	private float textPosXMax;
 	private float nameChangePosX;
 	private float nameChangePosY;
 	private float nameChangeTargetX;
 	private boolean drawMenu;
 	private boolean drawSettings;
 	private Preferences shakePrefs;
 	private nameListener nl;
 	private boolean animate;
 	private RequestHandler dialogHandler;
 
 	public ShakeOnIt(RequestHandler requestHandler)
 	{
 		this.dialogHandler = requestHandler;
 	}
 
 	@Override
 	public void create() {		
 		float w = Gdx.graphics.getWidth();
 		float h = Gdx.graphics.getHeight();
 
 		camera = new OrthographicCamera(w, h);
 		camera.translate(w/2, h/2);
 
 		myBatch = new SpriteBatch();
 		titleTex = new Texture(Gdx.files.internal("title.png"));
 		selecTex = new Texture(Gdx.files.internal("selector.png"));
 		startTex = new Texture(Gdx.files.internal("start.png"));
 		settingsTex = new Texture(Gdx.files.internal("settings.png"));
 		titleReg = new TextureRegion(titleTex, 512, 256);
 		selecReg = new TextureRegion(selecTex, 512, 256);
 		shakeFont = new BitmapFont(Gdx.files.internal("shakeFont.fnt"), Gdx.files.internal("shakeFont0.png"), false);
 
 		//If this is the first run, get the name of the user
 		shakePrefs = Gdx.app.getPreferences("prefs");
 		nameExists = shakePrefs.contains("name");
 		name = "???";
 		if(nameExists)
 		{
 			name = shakePrefs.getString("name");
 			//			Gdx.app.log("name", name);
 		}
 
 		if(name.equals("???"))
 		{
 			nameExists = false;
 		}
 
 		// Init the coords for title and selector buttons, and padding
 		selectorPadding = h / 12f;
 		title = new float[4];
 		selectors = new float[6];
 
 		title[0] = w / 6f;			//x
 		title[1] = h; 				//y
 		title[2] = (w / 3f) * 2f;	//width
 		title[3] = h / 4f;			//height
 
 		selectors[1] = h * 7f / 18f;
 		selectors[2] = w * 2/ 3f;
 		selectors[0] = 0 - selectors[2];	//Start with the selectors off screen
 		selectors[3] = h * 5/ 36f;
 		selectors[4] = selectors[0] - (selectors[2] / 2); // settings button x
 		selectors[5] = selectors[1] - (selectorPadding + selectors[3]); // settings button y
 
 		//Targets for final positions of menu objects
 		titleTarget = new float[4];
 		selectorsTarget = new float[6];
 
 		titleTarget[0] = w / 6f;
 		titleTarget[1] = h - (h/9f + h/4f);
 		titleTarget[2] = (w / 3f) * 2f;
 		titleTarget[3] = h / 4f;
 
 		selectorsTarget[0] = 0;
 		selectorsTarget[1] = h * 7f / 18f;
 		selectorsTarget[2] = w * 2/ 3f;
 		selectorsTarget[3] = h * 5/ 36f;
 		selectorsTarget[4] = selectorsTarget[0]; //settings x
 		selectorsTarget[5] = selectorsTarget[1] - (selectorPadding + selectors[3]); // settings button y
 		// Counter to handle "breathing" of the title image
 		breathCount = 0;
 		// breathing direction
 		breathDir = 0.5f;
 		drawMenu = true;
 		nl = new nameListener();
 		MyInputProcessor input = new MyInputProcessor();
 		Gdx.input.setInputProcessor(input);
 		Gdx.input.setCatchBackKey(true);
 		textPosX = 0;
 		textPosXMax = w;
 		// Boolean that determines whether to animate, when changing screens
 		animate = true;
 		drawSettings = false;
 		nameChangePosX = w;
 		nameChangePosY = h * 9f / 10f;
 		nameChangeTargetX = w / 6f;
 	}
 
 	@Override
 	public void dispose() {
 		myBatch.dispose();
 		selecTex.dispose();
 		titleTex.dispose();
 		startTex.dispose();
 		settingsTex.dispose();
 		shakeFont.dispose();
 	}
 
 	@Override
 	public void render() 
 	{		
 		Gdx.gl.glClearColor(0.2f, 0.8f, 1, 1); //blue, e5bc10
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		camera.update();
 
 
 		myBatch.setProjectionMatrix(camera.combined);
 
 		//Draw the menu
 		if(drawMenu)
 		{
 			name = shakePrefs.getString("name");
 			myBatch.begin();
 			// This means the menu has just been accessed, animate the menu 
 			if(animate)
 			{
 				myBatch.draw(titleReg, title[0], title[1], title[2], title[3]);
 				myBatch.draw(selecReg, selectors[0], selectors[1], selectors[2], selectors[3]);
 				myBatch.draw(startTex, selectors[0], selectors[1], selectors[2], selectors[3]);
 				myBatch.draw(selecReg, selectors[4], selectors[5], selectors[2], selectors[3]);
 				myBatch.draw(settingsTex, selectors[4], selectors[5], selectors[2], selectors[3]);
 				float textPosY = shakeFont.getBounds("Hi, " + name + ".").height + selectorPadding / 3f;
 				// Set the scale based on screen res
 				shakeFont.setScale(selectorPadding / 18f);
 				// Text scrolling
 				float textPosXLoop =  0 - shakeFont.getBounds("Hi, " + name + ".").width;
 				if(textPosX < textPosXLoop)
 				{
 					textPosX = textPosXMax;
 				}
 				// Move the text by a fraction of the title width per frame
 				textPosX -= titleTarget[2] / 96f;
 				shakeFont.draw(myBatch, "Hi, " + name + ".", textPosX, textPosY);
 				// move the elements into place
 				boolean animDone = true;
 				if(title[1] > titleTarget[1])
 				{
 					title[1] -= title[3] / 25;
 					animDone = false;
 				}
 				else
 				{
 					title[1] = titleTarget[1];
 				}
 
 				if(selectors[0] < selectorsTarget[0])
 				{
 					selectors[0] += selectors[2] / 20;
 					animDone = false;
 				}
 				else
 				{
 					selectors[0] = selectorsTarget[0];
 				}
 
 				if(selectors[4] < selectorsTarget[4])
 				{
 					selectors[4] += selectors[2] / 20;
 					animDone = false;
 				}
 				else
 				{
 					selectors[4] = selectorsTarget[4];
 				}
 
 				if(animDone)
 				{
 					//					Gdx.app.log("animation", "done");
 					animate = false;
 					resetCoords();
 				}
 			}
 			else
 			{
				resetCoords();
 				if(breathCount == 15)
 				{
 					breathDir = -breathDir;
 					breathCount = 0;
 				}
 
 				for(int i = 0; i < 4; i++)
 				{
 					titleTarget[i] += breathDir;
 				}
 				breathCount++;
 				myBatch.draw(titleReg, titleTarget[0], titleTarget[1], titleTarget[2], titleTarget[3]);
 				myBatch.draw(selecReg, selectorsTarget[0], selectorsTarget[1], selectorsTarget[2], selectorsTarget[3]);
 				myBatch.draw(selecReg, selectorsTarget[4], selectorsTarget[5], selectorsTarget[2], selectorsTarget[3]);
 				myBatch.draw(startTex, selectorsTarget[0], selectorsTarget[1], selectorsTarget[2], selectorsTarget[3]);
 				myBatch.draw(settingsTex, selectorsTarget[4], selectorsTarget[5], selectorsTarget[2], selectorsTarget[3]);
 				float textPosY = shakeFont.getBounds("Hi, " + name + ".").height + selectorPadding / 3f;
 				// Set the scale based on screen res
 				shakeFont.setScale(selectorPadding / 18f);
 				// Make the greeting text scroll from right to left.
 				float textPosXLoop =  0 - shakeFont.getBounds("Hi, " + name + ".").width;
 				if(textPosX < textPosXLoop)
 				{
 					textPosX = textPosXMax;
 				}
 				// Move the text by a fraction of the title width per frame
 				textPosX -= titleTarget[2] / 96f;
 				shakeFont.draw(myBatch, "Hi, " + name + ".", textPosX, textPosY);
 			}
 			myBatch.end();
 		}
 		else if(drawSettings)
 		{
 			//bring settings in and menu out
 			if(animate)
 			{
 				myBatch.begin();
 				myBatch.draw(titleReg, titleTarget[0], titleTarget[1], titleTarget[2], titleTarget[3]);
 				myBatch.draw(selecReg, selectorsTarget[0], selectorsTarget[1], selectorsTarget[2], selectorsTarget[3]);
 				myBatch.draw(startTex, selectorsTarget[0], selectorsTarget[1], selectorsTarget[2], selectorsTarget[3]);
 				myBatch.draw(selecReg, selectorsTarget[4], selectorsTarget[5], selectorsTarget[2], selectorsTarget[3]);
 				myBatch.draw(settingsTex, selectorsTarget[4], selectorsTarget[5], selectorsTarget[2], selectorsTarget[3]);
 				shakeFont.setScale(selectorPadding / 18f);
 				float textPosY = shakeFont.getBounds("Hi, " + name + ".").height + selectorPadding / 3f;
 				// greeting text scrolling
 				float textPosXLoop =  0 - shakeFont.getBounds("Hi, " + name + ".").width;
 				if(textPosX < textPosXLoop)
 				{
 					textPosX = textPosXLoop;
 				}
 				else if(textPosX > textPosXLoop)
 				{
 					// Move the text by a fraction of the title width per frame
 					textPosX -= titleTarget[2] / 70f;
 				}
 				// Only draw greeting until it leaves the screen
 				if(textPosX != textPosXLoop)
 				{
 					shakeFont.draw(myBatch, "Hi, " + name + ".", textPosX, textPosY);
 				}
 				
 				shakeFont.setScale(selectorPadding / 40f);
 				shakeFont.draw(myBatch, "Change name?", nameChangePosX, nameChangePosY);
 				
 				// move the elements into place
 				boolean animDone = true;
 				if(titleTarget[1] < title[1])
 				{
 					titleTarget[1] += title[3] / 25;
 					animDone = false;
 				}
 				else
 				{
 					titleTarget[1] = title[1];
 				}
 
 				if(selectorsTarget[0] > selectors[0])
 				{
 					selectorsTarget[0] -= selectors[2] / 20;
 					animDone = false;
 				}
 				else
 				{
 					selectorsTarget[0] = selectors[0];
 				}
 
 				if(selectorsTarget[4] > selectors[4])
 				{
 					selectorsTarget[4] -= selectors[2] / 20;
 					animDone = false;
 				}
 				else
 				{
 					selectorsTarget[4] = selectors[4];
 				}
 
 				if(nameChangePosX > nameChangeTargetX)
 				{
 					animDone = false;
 					nameChangePosX -= titleTarget[2] / 40f;
 				}
 				else
 				{
 					nameChangePosX = nameChangeTargetX;
 				}
 
 				if(animDone)
 				{
 					//					Gdx.app.log("animation", "done");
 					animate = false;
 					resetCoords();
 				}
 				myBatch.end();
 			}
 			else
 			{
 				myBatch.begin();
 				
 				// Continue drawing greeting
 				shakeFont.setScale(selectorPadding / 18f);
 				float textPosY = shakeFont.getBounds("Hi, " + name + ".").height + selectorPadding / 3f;
 				float textPosXLoop =  0 - shakeFont.getBounds("Hi, " + name + ".").width;
 				if(textPosX < textPosXLoop)
 				{
 					textPosX = textPosXLoop;
 				}
 				else if(textPosX > textPosXLoop)
 				{
 					// Move the text by a fraction of the title width per frame
 					textPosX -= titleTarget[2] / 96f;
 				}
 				// Only draw greeting until it leaves the screen
 				if(textPosX != textPosXLoop)
 				{
 					shakeFont.draw(myBatch, "Hi, " + name + ".", textPosX, textPosY);
 				}
 				
 				shakeFont.setScale(selectorPadding / 40f);
 				shakeFont.draw(myBatch, "Change name?", nameChangeTargetX, nameChangePosY);
 				myBatch.end();
 			}
 		}
 		// Check for first run
 		if(!nameExists)
 		{
 			Gdx.input.getTextInput(nl, "Your name?", "???");
 			nameExists = true;
 		}
 	}
 
 	@Override
 	public void resize(int width, int height) {
 	}
 
 	@Override
 	public void pause() {
 	}
 
 	@Override
 	public void resume() 
 	{
 		resetCoords();
 		if(!drawSettings)
 		{
 			animate = true;
 		}
 	}
 
 	//Reset start positions of menu elements
 	private void resetCoords()
 	{
 		float w = Gdx.graphics.getWidth();
 		float h = Gdx.graphics.getHeight();
 		title[0] = w / 6f;			//x
 		title[1] = h; 				//y
 		title[2] = (w / 3f) * 2f;	//width
 		title[3] = h / 4f;			//height
 
 		selectors[1] = h * 7f / 18f;
 		selectors[2] = w * 2/ 3f;
 		selectors[0] = 0 - selectors[2];	//Start with the selectors off screen
 		selectors[3] = h * 5/ 36f;
 		selectors[4] = selectors[0] - (selectors[2] / 2); // settings button x
 		selectors[5] = selectors[1] - (selectorPadding + selectors[3]); // settings button y
 
 		titleTarget[0] = w / 6f;
 		titleTarget[1] = h - (h/9f + h/4f);
 		titleTarget[2] = (w / 3f) * 2f;
 		titleTarget[3] = h / 4f;
 
 		selectorsTarget[0] = 0;
 		selectorsTarget[1] = h * 7f / 18f;
 		selectorsTarget[2] = w * 2/ 3f;
 		selectorsTarget[3] = h * 5/ 36f;
 		selectorsTarget[4] = selectorsTarget[0]; //settings x
 		selectorsTarget[5] = selectorsTarget[1] - (selectorPadding + selectors[3]); // settings button y
 
 		nameChangePosX = w;
 	}
 
 	// Make sure the user wants to exit on back button press.
 	private void showConfirmDialog(){
 		dialogHandler.confirm(new ConfirmInterface(){
 			@Override
 			public void yes() {
 				Gdx.app.exit();
 			}
 
 
 			@Override
 			public void no() {
 				// The user clicked no!
 
 			}
 		});
 	}
 
 
 	public class nameListener  implements TextInputListener{
 		@Override
 		public void input(String text) 
 		{
 			shakePrefs.putString("name", text);
 			shakePrefs.flush();
 			//			Gdx.app.log("name", text);
 		}
 
 		@Override
 		public void canceled() 
 		{
 			if(!shakePrefs.contains("name"))
 			{
 				shakePrefs.putString("name", "???");
 				shakePrefs.flush();
 			}
 		}
 
 	}
 
 	public class MyInputProcessor implements InputProcessor {
 		@Override
 		public boolean keyDown (int keycode) {
 			return false;
 		}
 
 		@Override
 		public boolean keyUp (int keycode) 
 		{
 			if(keycode == 4)
 			{
 				if(drawMenu)
 				{
 					showConfirmDialog();
 				}
 				else if(drawSettings)
 				{
 					animate = true;
 					drawSettings = false;
 					drawMenu = true;
 				}
 			}
 			return false;
 		}
 
 		@Override
 		public boolean keyTyped (char character) {
 			return false;
 		}
 
 		@Override
 		public boolean touchDown (int x, int y, int pointer, int button) {
 			if(animate)
 			{
 				animate = false;
 			}
 			return false;
 		}
 
 		@Override
 		public boolean touchUp (int x, int y, int pointer, int button) 
 		{
 			float h = Gdx.graphics.getHeight();
 			// Touch coords are y down, but the drawing coords are y up, this is to make touch coords match drawing coords.
 			y = (int) (h - y);
 			//			Gdx.app.log("touchUp", Integer.toString(x) + ", " + Integer.toString(y));
 
 			if(drawMenu)
 			{
 				// These x coordinates map to both buttons
 				if(x > selectorsTarget[0] && x < (selectorsTarget[0] + selectorsTarget[2]))
 				{
 					// Start button pressed
 					if(y > selectorsTarget[1] && y < (selectorsTarget[1] + selectorsTarget[3]))
 					{
 						Gdx.app.log("button", "start");
 					}
 
 					// Settings button pressed
 					if(y > selectorsTarget[5] && y < (selectorsTarget[5] + selectorsTarget[3]))
 					{
 						Gdx.app.log("button", "settings");
 						drawSettings = true;
 						drawMenu = false;
 						animate = true;
 						resetCoords();
 					}
 				}
 			}
 			else if(drawSettings && !animate)
 			{
 				// Change Name? pressed
 				if(y >= (nameChangePosY - h / 22f))
 				{
 					Gdx.input.getTextInput(nl, "Your name?", shakePrefs.getString("name"));
 				}
 			}
 			return false;
 		}
 
 		@Override
 		public boolean touchDragged (int x, int y, int pointer) {
 			return false;
 		}
 
 		@Override
 		public boolean scrolled (int amount) {
 			return false;
 		}
 
 		@Override
 		public boolean mouseMoved(int screenX, int screenY) {
 			// TODO Auto-generated method stub
 			return false;
 		}
 	}
 }
