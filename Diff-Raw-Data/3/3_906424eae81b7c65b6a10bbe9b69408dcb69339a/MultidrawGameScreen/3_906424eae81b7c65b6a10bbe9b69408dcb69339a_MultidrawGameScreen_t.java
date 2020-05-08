 package org.i52jianr.multidraw;
 
 import java.util.ArrayList;
 
 import com.badlogic.gdx.ApplicationListener;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.assets.AssetManager;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.GL10;
 import com.badlogic.gdx.graphics.OrthographicCamera;
 import com.badlogic.gdx.graphics.Pixmap;
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.Texture.TextureFilter;
 import com.badlogic.gdx.graphics.g2d.NinePatch;
 import com.badlogic.gdx.graphics.g2d.SpriteBatch;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Button;
 import com.badlogic.gdx.scenes.scene2d.ui.Button.ButtonStyle;
 import com.badlogic.gdx.scenes.scene2d.ui.ClickListener;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Skin;
 import com.badlogic.gdx.scenes.scene2d.ui.Slider;
 import com.badlogic.gdx.scenes.scene2d.ui.Slider.SliderStyle;
 import com.badlogic.gdx.utils.Scaling;
 
 class BrushButtonDescriptor {
 	private Brush brush;
 	private int x;
 	private int y;
 	
 	public BrushButtonDescriptor(Brush brush, int x, int y) {
 		super();
 		this.brush = brush;
 		this.x = x;
 		this.y = y;
 	}
 	
 	public int getX() {
 		return x;
 	}
 	
 	public int getY() {
 		return y;
 	}
 	
 	public Brush getBrush() {
 		return brush;
 	}
 }
 
 public class MultidrawGameScreen implements ApplicationListener {
 
     private OrthographicCamera cam;
 	private DrawingArea drawingArea;
 	private SpriteBatch batch;
 	
 	private final int OFFSET_X = 31;
 	private final int OFFSET_Y = 16;
 	private final int ORIGINAL_WIDTH = 320;
 	private final int ORIGINAL_HEIGHT = 480;
 	
 	private int red_x = 25;
 	private int red_y = 80;
 	
 	private int green_x = 25;
 	private int green_y = 50;
 	
 	private int blue_x = 25;
 	private int blue_y = 20;
 	
 	private float scaleX = 1.0f;
 	private float scaleY = 1.0f;
 	
 	/* UI Stuff */
 	private Stage stage;
 	private Slider red_slider;
 	private Slider blue_slider;
 	private Slider green_slider;
 	private Pixmap colorPreview;
 	private Texture colorPreviewTexture;
 	private AssetManager manager;
 	
 	private ArrayList<BrushButtonDescriptor> brushButtonsDesc;
 	private ArrayList<Button> brushButtons;
 	private int canvasSize;
 	
 	private Button activeBrushButton;
 	
 	public MultidrawGameScreen() {
 		brushButtonsDesc = new ArrayList<BrushButtonDescriptor>();
 		brushButtons = new ArrayList<Button>();
 		
 		brushButtonsDesc.add(new BrushButtonDescriptor(Brushes.pixel, red_x, red_y + 80));
 		brushButtonsDesc.add(new BrushButtonDescriptor(Brushes.round, red_x + 45, red_y + 80));
 		brushButtonsDesc.add(new BrushButtonDescriptor(Brushes.round7, red_x + 90, red_y + 80));
 		brushButtonsDesc.add(new BrushButtonDescriptor(Brushes.round11, red_x + 135, red_y + 80));
 		brushButtonsDesc.add(new BrushButtonDescriptor(Brushes.crossSmooth, red_x + 180, red_y + 80));
 		brushButtonsDesc.add(new BrushButtonDescriptor(Brushes.smooth5, red_x + 225, red_y + 80));
 		brushButtonsDesc.add(new BrushButtonDescriptor(Brushes.smooth7, red_x, red_y + 35));
 		brushButtonsDesc.add(new BrushButtonDescriptor(Brushes.smooth11, red_x + 45, red_y + 35));
 		brushButtonsDesc.add(new BrushButtonDescriptor(Brushes.square3, red_x + 90, red_y + 35));
 		brushButtonsDesc.add(new BrushButtonDescriptor(Brushes.square5, red_x + 135, red_y + 35));
 		brushButtonsDesc.add(new BrushButtonDescriptor(Brushes.square11, red_x + 180, red_y + 35));
 		brushButtonsDesc.add(new BrushButtonDescriptor(Brushes.square15, red_x + 225, red_y + 35));
 		
 		canvasSize = 256; // Default
 	}
 	
 	public MultidrawGameScreen(ArrayList<BrushButtonDescriptor> list, int size) {
 		brushButtonsDesc = list;
 		canvasSize = size;
 		brushButtons = new ArrayList<Button>();
 	
 	}
 	
 	@Override
 	public void create() {
 		cam = new OrthographicCamera(Gdx.graphics.getWidth(), Gdx.graphics.getHeight());
 		cam.setToOrtho(true, ORIGINAL_WIDTH, ORIGINAL_HEIGHT);																																					
 		drawingArea = new DrawingArea(canvasSize);
 		batch = new SpriteBatch();
 		stage = new Stage(ORIGINAL_WIDTH, ORIGINAL_HEIGHT, true, batch);
 		
 		Gdx.input.setInputProcessor(stage);
 		manager = new AssetManager();
 		manager.load("data/uiskin.json", Skin.class);
 		manager.load("draw-brush.png", Texture.class);
 		manager.load("draw-eraser-2.png", Texture.class);
 		manager.load("edit-clear-2.png", Texture.class);
 		
 		// While things yet to be loaded...
 		while(!manager.update());
 		
 		setupUI(manager.get("data/uiskin.json", Skin.class));
 	
 		Gdx.input.setInputProcessor(stage);
 		// Weird alpha if not called wtf
 		drawingArea.clearArea();
 	}
 
 	@Override
 	public void render() {
 		Gdx.gl.glClearColor(0.5f, 0.5f, 0.5f, 1.0f);
 		Gdx.gl.glClear(GL10.GL_COLOR_BUFFER_BIT);
 		
 		// We could use event handlers in case of performance drop 
 		setSelectedColor();
 		handleInput();	
 		
 		Texture texture = new Texture(drawingArea.getPixmap());
 		texture.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
 		texture.bind();
 		
 		Texture colortext = new Texture(colorPreview);
 		colortext.bind();
 		
 		cam.update();
 		batch.setProjectionMatrix(cam.combined);
 		batch.begin();
 		batch.draw(texture, OFFSET_X, OFFSET_Y);
 		batch.draw(colortext, 200, 390);
 		batch.end();
 		
 		stage.act(Gdx.graphics.getDeltaTime());
 		stage.draw();
 		
 		texture.dispose();
 		colortext.dispose();
 	}
 	
 
 	private void handleInput() {
 		if (Gdx.input.isTouched()) {
 			int touchx = Gdx.input.getX();
 			int touchy = Gdx.input.getY();
 			
 			touchx = Math.round(touchx * scaleX);
 			touchy = Math.round(touchy * scaleY);
 			
 			drawingArea.normDraw(touchx - OFFSET_X, touchy - OFFSET_Y);
 		} else {
 			 drawingArea.removeLast();
 		}
 	}
 
 	@Override
 	public void resize(int width, int height) {
         scaleX = ((float)ORIGINAL_WIDTH) / width;
         scaleY = ((float)ORIGINAL_HEIGHT) / height;
 	}
 
 	private void setupUI(final Skin skin) {
 		Label red_label = new Label(skin);
 		red_label.setText("R");
 		Label green_label = new Label(skin);
 		green_label.setText("G");
 		Label blue_label = new Label(skin);
 		blue_label.setText("B");
 		
 		for(BrushButtonDescriptor desc : brushButtonsDesc) {
 			Button tmp = brushButtonFactory(desc.getBrush(), skin, desc.getX(), desc.getY());
 			brushButtons.add(tmp);
 			stage.addActor(tmp);
 		}
 		
 		// Setting up initial state, not my best work but hey
 		activeBrushButton = brushButtons.get(0);
 		activeBrushButton.setStyle(skin.getStyle("checked", ButtonStyle.class));
 
 		red_slider = new Slider(0, 255, 1, skin.getStyle(SliderStyle.class), "slider");
 		green_slider = new Slider(0, 255, 1, skin.getStyle(SliderStyle.class), "slider");
 		blue_slider = new Slider(0, 255, 1, skin.getStyle(SliderStyle.class), "slider");
 		
 		stage.addActor(red_slider);
 		stage.addActor(green_slider);
 		stage.addActor(blue_slider);
 		
 		stage.addActor(red_label);
 		stage.addActor(green_label);
 		stage.addActor(blue_label);
 		
 		red_slider.y = red_y;
 		red_slider.x = red_x;
 		red_label.x = red_x - 15;
 		red_label.y = red_y + 6;
 		
 		green_slider.y = green_y;
 		green_slider.x = green_x;
 		green_label.x = green_x - 15;
 		green_label.y = green_y + 6;
 		
 		blue_slider.y = blue_y;
 		blue_slider.x = blue_x;
 		blue_label.x = blue_x - 15;
 		blue_label.y = blue_y + 6;
 		
 		// Random Initial values
 		red_slider.setValue(192);
 		green_slider.setValue(168);
 		blue_slider.setValue(172);
 		
 		// Maybe it's not necessary to use RGBA?
 		colorPreview = new Pixmap(64, 64, Pixmap.Format.RGBA8888);
 		colorPreviewTexture = new Texture(colorPreview);
 		colorPreviewTexture.bind();
 
 		Label drawThis = new Label(manager.get("data/uiskin.json", Skin.class));
 		drawThis.setText("Dibuja...La Dignidad");
 		drawThis.x = (ORIGINAL_WIDTH / 2) - (drawThis.getTextBounds().width / 2);
 		drawThis.y = ORIGINAL_HEIGHT - OFFSET_Y + 5;
 		
 		stage.addActor(drawThis);
 		
 		// Brush, eraser, clear buttons
 		final Button brush_button = new Button(new Image(manager.get("draw-brush.png", Texture.class), Scaling.fill), manager.get("data/uiskin.json", Skin.class));
 		final Button erase_button = new Button(new Image(manager.get("draw-eraser-2.png", Texture.class), Scaling.fill), manager.get("data/uiskin.json", Skin.class));
 		Button clear_button = new Button(new Image(manager.get("edit-clear-2.png", Texture.class), Scaling.fill), manager.get("data/uiskin.json", Skin.class));
 		
 		brush_button.setClickListener(new ClickListener() {
 			
 			@Override
 			public void click(Actor actor, float x, float y) {
 				drawingArea.setDrawMode();
 				// Toggle the state (same on the other button's method)
 				erase_button.setStyle(skin.getStyle("unchecked", ButtonStyle.class));
 				brush_button.setStyle(skin.getStyle("checked", ButtonStyle.class));
 			}
 		});
 		
 		erase_button.setClickListener(new ClickListener() {
 			
 			@Override
 			public void click(Actor actor, float x, float y) {
 				drawingArea.setEraseMode();
 				brush_button.setStyle(skin.getStyle("unchecked", ButtonStyle.class));
 				erase_button.setStyle(skin.getStyle("checked", ButtonStyle.class));
 			}
 		});
 		
 		clear_button.setClickListener(new ClickListener() {
 			
 			@Override
 			public void click(Actor actor, float x, float y) {
 				drawingArea.clearArea();
 			}
 		});
 		
 		// Initial style state
 		brush_button.click(0, 0);
 		
 		// Setup Position
 		clear_button.x = ORIGINAL_WIDTH - clear_button.width - 10;
 		clear_button.y = blue_slider.y;
 		
 		erase_button.x = ORIGINAL_WIDTH - erase_button.width - 10;
 		erase_button.y = green_slider.y;
 		
 		brush_button.x = ORIGINAL_WIDTH - brush_button.width - 10;
 		brush_button.y = red_slider.y;
 		
 		stage.addActor(brush_button);
 		stage.addActor(erase_button);
 		stage.addActor(clear_button);
 
 	}
 
 	@Override
 	public void pause() {
 		Gdx.app.log("INFO", "Pausing...");
 	}
 
 	@Override
 	public void resume() {
 		Gdx.app.log("INFO", "Resuming...");
 		
 		for(Button button : brushButtons) {
 			stage.removeActor(button);
 		}
 		
 		brushButtons.clear();
 		
 		for(BrushButtonDescriptor desc : brushButtonsDesc) {
 			Button tmp = brushButtonFactory(desc.getBrush(), manager.get("data/uiskin.json", Skin.class), desc.getX(), desc.getY());
 			brushButtons.add(tmp);
 			stage.addActor(tmp);
 		}
 	}
 
 	@Override
 	public void dispose() {
 		// drawingArea.dispose();
 		batch.dispose();
 	}
 	
 	// Overloading is cool
 	private void setSelectedColor() {
 		setSelectedColor(new Color(red_slider.getValue() / 255.0f, green_slider.getValue()  / 255.0f, blue_slider.getValue() / 255.0f, 1.0f));
 	}
 	
 	private void setSelectedColor(Color color) {
 		colorPreview.setColor(color);
 		colorPreview.fill();
 		drawingArea.setColor(color);
 	}
 	
 	private Button brushButtonFactory(final Brush brush, final Skin skin, int x, int y) {
 		Texture text = new Texture(brush.getPixmap());
 		text.setFilter(TextureFilter.Nearest, TextureFilter.Nearest);
 		text.bind();
 		Image image = new Image(text, Scaling.fill);
 		final Button button = new Button(image, skin.getStyle(ButtonStyle.class));
 		button.setClickListener(new ClickListener() {
 			@Override
 			public void click(Actor actor, float x, float y) {
 				drawingArea.setBrush(brush);
 				if (!activeBrushButton.equals(button)) {
 					button.setStyle(skin.getStyle("checked", ButtonStyle.class));
 					activeBrushButton.setStyle(skin.getStyle("unchecked", ButtonStyle.class));
 					activeBrushButton = button;	
 				}
 			}
 		});
 		
 		button.x = x;
 		button.y = y;
 		
 		return button;
 	}
 
 }
