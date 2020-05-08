 package fr.odai.zerozeroduck;
 
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Label.LabelStyle;
 import com.badlogic.gdx.scenes.scene2d.utils.Align;
 import com.badlogic.gdx.utils.Array;
 
 import fr.odai.zerozeroduck.utils.TextureSetup;
 
 public class IntroRenderer {
 	private OrthographicCamera cam;
 	private Rectangle button = new Rectangle(4.5f, 3, 1, 1);
 	
 	private static final float CAMERA_WIDTH = 10f;
 	private static final float CAMERA_HEIGHT = 7f;
 
 	private SpriteBatch spriteBatch;
 
 	/* Nos textures */
 	private TextureRegion intro1Texture;
 	private TextureRegion intro2Texture;
 	private TextureRegion intro3Texture;
 	private TextureRegion intro4Texture;
 	
 	Array<TextureRegion> textures;
 
 	private float ppuX; // pixels per unit on the X axis
 	private float ppuY; // pixels per unit on the Y axis
 
 	private int width;
 	private int height;
 	
 	private float positionLastY;
 	private float positionPost2=0.f;
 	
 	int etape=1;
 	
 	boolean isEtapeFinished=true;
 	
 	public boolean getIsEtapeFinished(){
 		return isEtapeFinished;
 	}
 
 	public void setSize(int w, int h) {
 		this.width = w;
 		this.height = h;
 		ppuX = (float) width / CAMERA_WIDTH;
 		ppuY = (float) height / CAMERA_HEIGHT;
 	}
 
 	public IntroRenderer() {
 		positionLastY=0.f;
 		textures = new Array<TextureRegion>();
 		this.cam = new OrthographicCamera(CAMERA_WIDTH, CAMERA_HEIGHT);
 		this.cam.position.set(CAMERA_WIDTH / 2f, CAMERA_HEIGHT / 2f, 0);
 		this.cam.update();
 		spriteBatch = new SpriteBatch();
 		loadTextures();
 	}
 
 	public void render(float delta) {
 		spriteBatch.begin();
 		drawFond(delta);
 		spriteBatch.end();
 	}
 
 	private void drawFond(float delta) {
 		int i;
 		for(i=0;i<textures.size-1;i++){
 			TextureRegion texture=textures.get(i);
 			if(i<2)
				spriteBatch.draw(texture, 0.f, positionPost2,(int) (1000 * 0.8),(int) (700 * 0.8));
			else spriteBatch.draw(texture, 0.f, 0.f,(int) (1000 * 0.8),(int) (700 * 0.8));
 		}
 		TextureRegion texture=textures.get(i);
 		if(positionLastY<0){
 			positionLastY+=delta*500.f;
 		}
 		if(positionLastY>=0){
 			positionLastY=0;
 			isEtapeFinished=true;
 		} 
		if(etape>=3 && positionPost2<=(int)(700*0.8)){
 			positionPost2+=delta*500.f;
 		}
		spriteBatch.draw(texture, 0.f, positionLastY,(int) (1000 * 0.8),(int) (700 * 0.8));
 	}
 
 	private void loadTextures() {
		TextureSetup.main(null);
 		TextureAtlas atlas = new TextureAtlas(
 				Gdx.files.internal("images/textures.pack"));
 		intro1Texture = atlas.findRegion("Intro1");
 		intro2Texture = atlas.findRegion("Intro2");
 		intro3Texture = atlas.findRegion("Intro3");
 		intro4Texture = atlas.findRegion("Intro4");
 		textures.add(intro1Texture);
 	}
 	
 	public boolean click(int x, int y){
 		return button.contains(x/ppuX, y/ppuY);
 	}
 	
 	public void setEtape(int etape){
 		this.etape=etape;
 		if(positionLastY>=0){
 			if(etape==2){
 				textures.add(intro2Texture);
 			}
 			else if(etape==3){
 				textures.add(intro3Texture);
 			}
 			else if(etape==4){
 				textures.add(intro4Texture);
 			}
 			isEtapeFinished=false;
			positionLastY=-(int) (700 * 0.8);
 		}
 	}
 
 	
 	
 }
