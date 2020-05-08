 package yang.graphics.translator;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Map.Entry;
 
 import yang.graphics.font.BitmapFont;
 import yang.graphics.model.material.YangMaterialProvider;
 import yang.graphics.model.material.YangMaterialSet;
 import yang.graphics.textures.TextureData;
 import yang.graphics.textures.TextureProperties;
 import yang.graphics.textures.enums.TextureFilter;
 import yang.graphics.textures.enums.TextureWrap;
 import yang.model.DebugYang;
 import yang.systemdependent.AbstractResourceManager;
 
 
 
 public abstract class AbstractGFXLoader implements YangMaterialProvider{
 	
 	public static int MAX_TEXTURES = 512;
 	public static final String[] IMAGE_EXT	= new String[]{".png",".jpg"};
 	public static final String SHADER_EXT	= ".txt";
 
 	protected String SHADER_PATH	= "shaders" + File.separatorChar;
 	protected String[] IMAGE_PATH		= new String[]{"","textures"+File.separatorChar,"models"+File.separatorChar};
 	protected String MATERIAL_PATH  = "models" + File.separatorChar;
 	
 	public HashMap<String, Texture> mTextures;
 	protected HashMap<String, String> mShaders;
 	protected HashMap<String, YangMaterialSet> mMaterials;
 	protected GraphicsTranslator mGraphics;
 	
 	public String[] mTexQueue;
 	protected int mTexQueueId = 0;
 	public boolean mEnqueueMode = false;
 	protected int mQueueBytes = 0;
 	public int mMaxQueueLoadingBytes = -1;
 	
 	public int mDefaultApproxTextureSize = 256;
 	
 	public AbstractResourceManager mResources;
 
 	protected abstract TextureData derivedLoadImageData(String filename,boolean forceRGBA);
 	
 	public AbstractGFXLoader(GraphicsTranslator graphics,AbstractResourceManager resources) {
 		mTexQueue = new String[MAX_TEXTURES];
 		mTexQueueId = 0;
 		mTextures = new HashMap<String, Texture>();
 		mShaders = new HashMap<String, String>();
 		mMaterials = new HashMap<String, YangMaterialSet>();
 		mGraphics = graphics;
 		mResources = resources;
 	}
 	
 	public YangMaterialSet getMaterialSet(String name) {
 		if(name.startsWith("./"))
 			name = name.substring(2);
 		YangMaterialSet result = mMaterials.get(name);
 		if(result!=null)
 			return result;
 		result = new YangMaterialSet(this);
 		String filename = MATERIAL_PATH+name;
 		if(!mResources.fileExists(filename))
 			return null;
 		try {
 			result.loadFromStream(mResources.getInputStream(filename));
 		} catch (IOException e) {
 			return null;
 		}
 		mMaterials.put(name, result);
 		return result;
 	}
 	
 	public TextureData loadImageData(String path,String name,String ext,boolean forceRGBA) {
 		String filename = path + File.separatorChar+name+"."+ext;
 		if(!mResources.fileExists(filename))
 			return null;
 		else
 			return derivedLoadImageData(filename,forceRGBA);
 	}
 	
 	public String createExistingFilename(String name) {
 		for(String path:IMAGE_PATH) {
 			if(mResources.fileExists(path+name))
 				return path+name;
 			for(String ext:IMAGE_EXT) {
 				if(mResources.fileExists(path+name+ext))
 					return path+name+ext;
 			}
 		}
 
 		throw new RuntimeException("Image not found: "+name);
 	}
 	
 	protected TextureData loadImageData(String name,boolean forceRGBA) {
 		String filename = createExistingFilename(name);
 		if(filename==null)
 			return null;
 		return derivedLoadImageData(filename,forceRGBA);
 	}
 
 	public TextureData loadImageData(String name) {
 		return loadImageData(name,false);
 	}
 
 	private void enqueue(String key,Texture tex) {
 		mTexQueue[mTexQueueId++] = key;
 		mQueueBytes += tex.getByteCount();
 	}
 	
 	private String pollQueue() {
 		if(mTexQueueId<=0)
 			return null;
 		String tex = mTexQueue[--mTexQueueId];
 		mQueueBytes -= mTextures.get(tex).getByteCount();
 		return tex;
 	}
 	
 	public void loadEnqueuedTextures() {
 		mEnqueueMode = false;
 		String texKey;
 		int minBytes = mMaxQueueLoadingBytes>0?mQueueBytes - mMaxQueueLoadingBytes:-1;
 		while(mQueueBytes>minBytes && (texKey=pollQueue())!=null) {
 			Texture tex = mTextures.get(texKey);
 			TextureData data = loadImageData(texKey,tex.mProperties.mChannels>3);
			if(data==null)
				System.err.println("Image not found: "+texKey);
 			if(tex.mIsAlphaMap)
 				data.redToAlpha();
			tex.generate();
 			tex.update(data);
 		}
 	}
 	
 	private Texture loadTexture(String filename,TextureProperties textureProperties,int approxWidth,int approxHeight,boolean alphaMap) {
 		if(mEnqueueMode) {
 			Texture result = new Texture(mGraphics,textureProperties).generate();
 			result.mWidth = approxWidth;
 			result.mHeight = approxHeight;
 			if(alphaMap)
 				result.mIsAlphaMap = true;
 			enqueue(filename,result);
 			return result;
 		}else{
			TextureData data = derivedLoadImageData(filename,alphaMap);
 			if(alphaMap)
 				data.redToAlpha();
			Texture result = mGraphics.createTexture(data,textureProperties);
			result.mIsAlphaMap = alphaMap;
			return result.finish();
 		}
 	}
 	
 	public synchronized Texture getImage(String name,TextureProperties textureProperties,int approxWidth,int approxHeight,boolean alphaMap) {
 		String filename = createExistingFilename(name);
 		Texture texture = mTextures.get(filename);
 		
 		if (texture != null) {
 			if((textureProperties==null || texture.mProperties.equals(textureProperties)))
 				return texture;
 			else{
 				System.out.println("double loaded texture: "+filename+" "+textureProperties+" != "+texture.mProperties);
 				//DebugYang.showStackTrace();
 			}
 		}
 		if(textureProperties==null)
 			textureProperties = new TextureProperties();
 
 		texture = loadTexture(filename, textureProperties,approxWidth,approxHeight,alphaMap);
 		mTextures.put(filename, texture);
 		mGraphics.rebindTexture(0);
 		
 		return texture;
 	}
 	
 	public synchronized Texture getImage(String name,TextureProperties textureProperties,int approxWidth,int approxHeight) {
 		return getImage(name,textureProperties,approxWidth,approxHeight,false);
 	}
 	
 	public synchronized Texture getImage(String name,TextureProperties textureProperties) {
 		return getImage(name,textureProperties,mDefaultApproxTextureSize,mDefaultApproxTextureSize,false);
 	}
 	
 	public void freeTexture(String name) {
 		String filename = createExistingFilename(name);
 		Texture tex = mTextures.remove(filename);
 		if(tex!=null)
 			tex.free();
 	}
 	
 	public Texture getImage(String name, TextureWrap wrapX, TextureWrap wrapY) {
 		return getImage(name,new TextureProperties(wrapX,wrapY));
 	}
 	
 	public Texture getImage(String name, TextureFilter textureFilter) {
 		return getImage(name,new TextureProperties(textureFilter));
 	}
 	
 	public Texture getImage(String name) {
 		Texture texture = mTextures.get(name);
 		if (texture != null)
 			return texture;
 		else
 			return getImage(name,new TextureProperties());
 	}
 	
 	public Texture getAlphaMap(String name,TextureProperties textureProperties) {
 		return getImage(name,textureProperties,mDefaultApproxTextureSize,mDefaultApproxTextureSize,true);
 	}
 	
 	public Texture getAlphaMap(String name) {
 		return getAlphaMap(name,new TextureProperties());
 	}
 	
 	public Texture getAlphaMap(String name, TextureWrap wrapX, TextureWrap wrapY) {
 		return getAlphaMap(name,new TextureProperties(wrapX,wrapY));
 	}
 	
 	public Texture getAlphaMap(String name, TextureWrap wrapX, TextureWrap wrapY, TextureFilter textureFilter) {
 		return getAlphaMap(name,new TextureProperties(wrapX,wrapY,textureFilter));
 	}
 
 	public String getShader(String name) {
 		String shader = mShaders.get(name);
 		if (shader != null)
 			return shader;
 		if(!name.endsWith(SHADER_EXT))
 			name += SHADER_EXT;
 		shader = mResources.textFileToString(SHADER_PATH+name);
 		mShaders.put(name, shader);
 		return shader;
 	}
 
 	public void clear() {
 		for(Entry<String,Texture> entry:mTextures.entrySet()) {
 			entry.getValue().free();
 		}
 		mTextures.clear();
 		mQueueBytes = 0;
 	}
 
 	public BitmapFont loadFont(String texAndDatafilename) {
 		BitmapFont result = new BitmapFont();
 		return result.init(texAndDatafilename,this);
 	}
 	
 	public BitmapFont loadFont(Texture texture,String fontDataFilename) {
 		BitmapFont result = new BitmapFont();
 		return result.init(texture,fontDataFilename,mResources);
 	}
 
 
 	
 	public void deleteTextures() {
 		for(Entry<String,Texture> entry:mTextures.entrySet()) {
 			entry.getValue().free();
 		}
 	}
 
 	public void startEnqueuing() {
 		mEnqueueMode = true;
 	}
 
 	public void divideQueueLoading(int steps) {
 		if(steps<=0 || mQueueBytes<=0)
 			mMaxQueueLoadingBytes = -1;
 		else
 			mMaxQueueLoadingBytes = mQueueBytes/steps;
 	}
 	
 	//TODO handling same filenames with different texture settings
 	public void reloadTextures() {
 		for(Entry<String,Texture> entry:mTextures.entrySet()) {
 			TextureData data = loadImageData(entry.getKey());
 			Texture tex = entry.getValue();
 			if(tex.mIsAlphaMap)
 				data.redToAlpha();
 			tex.update(data.mData);
 			data = null;
 		}
 	}
 
 	public void reenqueueTextures() {
 		for(Entry<String,Texture> entry:mTextures.entrySet()) {
 			enqueue(entry.getKey(),entry.getValue());
 		}
 	}
 	
 }
