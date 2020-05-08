 package yang.samples.statesystem.states;
 
 import yang.graphics.defaults.programs.TextureTileRepeatProgram;
 import yang.graphics.textures.TextureCoordBounds;
 import yang.graphics.textures.TextureData;
 import yang.graphics.textures.TextureProperties;
 import yang.graphics.textures.enums.TextureFilter;
 import yang.graphics.textures.enums.TextureWrap;
 import yang.graphics.translator.Texture;
 import yang.math.objects.Quadruple;
 import yang.samples.statesystem.SampleState;
 
 public class TexAtlasSampleState extends SampleState {
 
 	private Texture mAtlasTex;
 	private TextureCoordBounds mGrassBounds;
 	private TextureCoordBounds mSkyBounds;
 	private TextureTileRepeatProgram mTileProgram;
 	
 	@Override
 	public void initGraphics() {
 		
 		mTileProgram = mGraphics.addProgram(TextureTileRepeatProgram.class);
 		
 		TextureData atlasData = mGFXLoader.loadImageData("atlas");
 		mGrassBounds = atlasData.createBiasBorder(0, 0, 128, 128, 8, TextureWrap.REPEAT, TextureWrap.REPEAT);
 		atlasData.createBiasBorder(128, 0, 128, 128, 4, TextureWrap.CLAMP, TextureWrap.CLAMP);
 		atlasData.createBiasBorder(0, 128, 128, 128, 8, TextureWrap.MIRROR, TextureWrap.MIRROR);
 		mSkyBounds = atlasData.copyWithMargin(128,128, 128,128, mGFXLoader.loadImageData("sky_small"), 2, TextureWrap.REPEAT, TextureWrap.CLAMP);
 		mAtlasTex = mGraphics.createTexture(atlasData, new TextureProperties(TextureFilter.LINEAR));
 	}
 	
 	@Override
 	protected void step(float deltaTime) {
 		
 	}
 
 	@Override
 	protected void draw() {
 		mGraphics.clear(0,0,0);
 		mGraphics2D.activate();
 		mGraphics2D.setShaderProgram(mTileProgram);
		//mGraphics2D.setDefaultProgram();
 		mGraphics.bindTexture(mAtlasTex);
 		//mGraphics.bindTexture(mGraphics.createTexture(mGFXLoader.loadImageData("sky_small"), new TextureSettings()));
 		mGraphics2D.setWhite();
 		
 		
 		mGraphics2D.setSuppData(mSkyBounds);
 		mGraphics2D.drawRect(mGraphics2D.getScreenLeft(),-1,mGraphics2D.getScreenRight(),1, 0,mSkyBounds.getHeight(),mSkyBounds.getWidth()*2,0);
 		
 		mGraphics2D.setSuppData(mGrassBounds);
 		final float SIZE = 0.5f;
 		mGraphics2D.drawRect(-SIZE,-SIZE,SIZE,SIZE, 0,2,2,0);
 		
 		mGraphics2D.setSuppData(Quadruple.Q0011);
 		mGraphics2D.drawRect(mGraphics2D.getScreenRight()-0.7f,mGraphics2D.getScreenTop()-0.7f,mGraphics2D.getScreenRight()-0.03f,mGraphics2D.getScreenTop()-0.03f);
 	}
 	
 	@Override
 	public void stop() {
 		mGraphics2D.setDefaultProgram();
 	}
 
 }
