 package rokon;
 
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.HashSet;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.graphics.Canvas;
 import android.graphics.Paint;
 
 /**
  * TextureAtlas is a way of optimizing the use of multiple textures in OpenGL.
  * OpenGL stores each of its textures as seperate images on the hardware, and to render
  * one of these requires a command to tell OpenGL to scrap the image it has loaded, and 
  * load up a new image into its immediate memory. It makes more sense to provide OpenGL
  * with one large texture, so it never has to change between which it has selected, and
  * for us to simply choose which parts of that texture we place onto a Sprite. This is
  * called texture mapping.
  * 
  * TextureAtlas uses a very basic, and rather inefficient, largest-first bin packing 
  * algorithm to squeeze all your textures and fonts on to one large image. This will be
  * improved for future versions.
  * 
  * TextureAtlas provides OpenGL with a power-of-two sized texture (2,4,8,16,32 etc.) as
  * this is needed for the hardware to work efficiently. It is possible for you however
  * to load Texture's of any dimenion into the atlas, and Rokon will clip it for you.
  */
 public class TextureAtlas {
 	
 	public final static int MAX_TEXTURES = 5;
 	
 	public static boolean ready = false;
 	public static boolean readyToLoad = false;
 	
 	public static HashMap<Integer, HashMap<Integer, Texture>> _textureSets = new HashMap<Integer, HashMap<Integer, Texture>>();;
 	public static HashMap<Integer, Texture> _texture = new HashMap<Integer, Texture>();;
 	private static int _textureCount = 0;
 	private static int _textureSetCount = 0;
 	private static Bitmap[] _bmp = new Bitmap[MAX_TEXTURES];
 	public static int[] texId = new int[MAX_TEXTURES];
 	
 	private static int _greatestWidth;
 	
 	private static int _width;
 	private static int[] _height = new int[MAX_TEXTURES];
 	
 	public static int currentAtlas = 0;
 	
 	public static String idString = "";
 	
 	public static Paint paint = new Paint();
 	
 	public static boolean reloadTextures = false;
 	public static HashSet<Integer> reloadTextureIndices = new HashSet<Integer>();
 	
 	/**
 	 * Resets the TextureAtlas ready to load another set of textures.
 	 */
 	public static void reset() {
 		_texture = new HashMap<Integer, Texture>();
 		_textureCount = 0;
 		readyToLoad = false;
 		ready = false;
 		idString = "";
 		System.gc();
 	}
 	
 	/**
 	 * @return the width of the texture atlas
 	 */
 	public static int getWidth() {
 		return _width;
 	}
 	
 	/**
 	 * @return the height of the texture atlas
 	 */
 	public static int getHeight(int tex) {
 		try {
 			return _height[tex];
 		} catch (Exception e ) {
 			e.printStackTrace();
 			Debug.print("EXCEPTION GETTING " + tex);
 			return 0;
 		}
 	}
 	
 	/**
 	 * Creates a Texture from the /assets/ folder
 	 * @param path path to the file in /assets/
 	 * @return Texture pointer
 	 */
 	public static Texture createTexture(String path) {
 		try {
 			Bitmap bmp = BitmapFactory.decodeStream(Rokon.getRokon().getActivity().getAssets().open(path));
 			Texture texture = new Texture(path, bmp);
 			if(bmp.getWidth() > _greatestWidth)
 				_greatestWidth = bmp.getWidth();
 			_texture.put(_textureCount, texture);
 			_textureCount++;
 			
 			idString += path + "/" + bmp.getWidth() + "/" + bmp.getHeight();
 			
 			bmp.recycle();
 			//System.gc();
 			return texture;
 		} catch (IOException e) {
 			Debug.print("CANNOT FIND " + path);
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	/**
 	 * Creates a Texture and puts it onto the atlas
 	 * @param resourceId reference to a drawable resource file, as described in R.java
 	 * @return NULL if failed
 	 */
 	public static Texture createTextureFromResource(int resourceId) {
 		Bitmap bmp = BitmapFactory.decodeResource(Rokon.getRokon().getActivity().getResources(), resourceId);
 		Texture t = createTextureFromBitmap(bmp);
 		return t;
 	}
 	
 	/**
 	 * Creates a Texture and puts it onto the atlas
 	 * @param bmp a Bitmap object to build the texture from
 	 * @return NLUL if failed
 	 */
 	public static Texture createTextureFromBitmap(Bitmap bmp) {
 		Texture texture = new Texture(bmp);
 		if(bmp.getWidth() > _greatestWidth)
 			_greatestWidth = bmp.getWidth();
 		_texture.put(_textureCount, texture);
 		_textureCount++;
 		return texture;
 	}
 	
 	/**
 	 * Adds a Texture to the atlas, note: this is done automatically if createTextureXXX functions are used
 	 * @param texture
 	 */
 	public static void addTexture(Texture texture) {
 		_texture.put(_textureCount, texture);
 		_textureCount++;
 	}
 	
 	/**
 	 * Calculates a TextureAtlas from all the loaded Texture's
 	 */
 	public static void compute() {
 		compute(1024);
 	}
 	
 	/**
 	 * Calculates a TextureAtlas from all the loaded Texture's
 	 * The Texture's are sorted into largest-first order, and a bin packing algorithm is used to squeeze 
 	 * into the atlas. There is a maximum total atlas size of 1024x1024 pixels, imposed by OpenGL.
 	 * If your Texture's and Font's do not fit into this space, an exception will be raised.
 	 * 
 	 * An improvement - to allow more atlases simulatenously, is being worked on.
 	 * @param initwidth the minimum width of the atlas
 	 */
 	@SuppressWarnings("unchecked")
 	public static void compute(int initwidth) {
 		boolean isNew;
 		//Debug.print("# COMPUTING TEXTURES IDSTR=" + idString);
		if(getLastIdString() == idString && !Rokon.getRokon().isForceTextureRefresh()) {
 			//Debug.print("## MATCHES LAST ID STRING");
 			isNew = false;
 		} else {
 			//Debug.print("## NEW ID STRING");
 			saveLastIdString();
 			isNew = true;
 		}
 		
 		if(isNew) {
 			_textureSets.put(_textureSetCount, (HashMap<Integer, Texture>)_texture.clone());
 			_height[0] = 0;
 			int i = 0;
 			int curK = 0;
 			_width = initwidth;
 			if(_width == 0)
 				while(_width < _greatestWidth)
 					_width = (int)Math.pow(2, i++);
 			for(i = 0; i < _textureSetCount; i++)
 				for(int h = 0; h < _textureSets.size(); h++)
 					_textureSets.get(h).get(i).inserted = false;
 			for(curK = 0; curK <= _textureSetCount; curK++) {
 				for(i = 0; i < _textureSets.get(curK).size(); i++) {
 					HashMap<Integer, Texture> textureSet = _textureSets.get(curK);
 					int greatestArea = 0;
 					int greatestIndex = -1;
 					for(int j = 0; j < _textureSets.get(curK).size(); j++) {
 						if(!textureSet.get(j).inserted && textureSet.get(j).getWidth() * textureSet.get(j).getHeight() > greatestArea) {
 							greatestIndex = j;
 							greatestArea = textureSet.get(j).getWidth() * textureSet.get(j).getHeight();
 						}
 					}
 					if(greatestIndex != -1) {
 						Texture texture = textureSet.get(greatestIndex);
 						int x = 0;
 						int y = 0;
 						boolean found = false;
 						while(!found) {
 							if(y + texture.getHeight() > 1024) {
 								Debug.print("Current atlas is full, moving on... x=" + x + " y=" + y + " w=" + texture.getWidth() + " h=" + texture.getHeight());
 								saveBitmap(_width);
 								x = 0;
 								y = 0;
 								currentAtlas++;
 								_height[currentAtlas] = 0;
 							}
 							int resX = isAnyoneWithinX(x, y, x + texture.getWidth(), y + texture.getHeight());
 							int resY = isAnyoneWithinY(x, y, x + texture.getWidth(), y + texture.getHeight());
 							if(resX == -1) {
 								texture.atlasX = x;
 								texture.atlasY = y;
 								texture.atlasIndex = currentAtlas;
 								texture.inserted = true;
 								found = true;
 								if(texture.atlasY + texture.getHeight() > _height[currentAtlas])
 									_height[currentAtlas] = texture.atlasY + texture.getHeight();
 							} else {
 								x = resX;
 								if(x + texture.getWidth() >= _width) {
 									x = 0;
 									y = resY;
 								}
 							}
 						}
 					} else
 						break;
 						
 				}
 				saveBitmap(_width);
 				currentAtlas++;
 				_height[currentAtlas] = 0;
 				Debug.print("reached split");
 			}
 			saveNewSettings();
 		} else {
 			//Debug.print("## LOADING OLD TEXTURES");
 			_textureSets.put(_textureSetCount, (HashMap<Integer, Texture>)_texture.clone());
 			loadOldSettings();
 			_height[0] = 0;
 			int i = 0;
 			int curK = 0;
 			_width = initwidth;
 			if(_width == 0)
 				while(_width < _greatestWidth)
 					_width = (int)Math.pow(2, i++);
 			for(i = 0; i < _textureSetCount; i++)
 				for(int h = 0; h < _textureSets.size(); h++)
 					_textureSets.get(h).get(i).inserted = false;
 			for(curK = 0; curK <= _textureSetCount; curK++) {
 				for(i = 0; i < _textureSets.get(curK).size(); i++) {
 					HashMap<Integer, Texture> textureSet = _textureSets.get(curK);
 					Texture texture = textureSet.get(i);
 					texture.atlasX = texture.suggestX;
 					texture.atlasY = texture.suggestY;
 					texture.atlasIndex = texture.suggestAtlas;
 					texture.inserted = true;
 					if(texture.atlasY + texture.getHeight() > _height[currentAtlas])
 						_height[currentAtlas] = texture.atlasY + texture.getHeight();
 				
 				}
 				saveBitmap(_width);
 				currentAtlas++;
 				_height[currentAtlas] = 0;
 				//Debug.print("reached split");
 			}
 		}
 		System.gc();
 	}
 	
 	public static void saveBitmap(int _width) {
 		int theight = _height[currentAtlas];
 		_height[currentAtlas] = 0;
 		int i = 0;
 		while(_height[currentAtlas] < theight)
 			_height[currentAtlas] = (int)Math.pow(2, i++);
 		_bmp[currentAtlas] = Bitmap.createBitmap(_width, _height[currentAtlas], Bitmap.Config.ARGB_8888);
 		Canvas canvas = new Canvas(_bmp[currentAtlas]);
 		for(int f = 0; f < _textureSets.size(); f++)
 			for(int h = 0; h < _textureSets.get(f).size(); h++) {
 				Texture texture = _textureSets.get(f).get(h);
 					if(texture.atlasIndex == currentAtlas) {
 						Bitmap bmp = null;
 						if(texture.isAsset) {
 							try {
 								bmp = BitmapFactory.decodeStream(Rokon.getRokon().getActivity().getAssets().open(texture.assetPath));
 							}
 							catch (Exception e) { 
 								Debug.print("CANNOT FIND saveBitmap");
 								System.exit(0);
 							}
 						} else {
 							bmp = texture.getBitmap();
 						}
 						canvas.drawBitmap(bmp, texture.atlasX, texture.atlasY, new Paint());
 						texture.cleanBitmap();
 						bmp.recycle();
 						Runtime.getRuntime().gc();
 					}
 				}
 		readyToLoad = true;
 	}
 	
 	public static Bitmap getBitmap(int index) {
 		return _bmp[index];
 	}
 	
 	private static int isAnyoneWithinX(int x, int y, int x2, int y2) {
 		for(int i = 0; i < _textureSets.size(); i++)
 			for(int h = 0; h < _textureSets.get(i).size(); h++) {
 				Texture texture = _textureSets.get(i).get(h);
 				if(texture.inserted && texture.atlasIndex == currentAtlas) {
 					boolean maybe = false;
 					
 					if(texture.atlasX >= x && texture.atlasX <= x2)
 						maybe = true;
 					
 					if(texture.atlasX <= x && texture.atlasX + texture.getWidth() > x)
 						maybe = true;
 					
 					if(maybe) {
 						if(texture.atlasY >= y && texture.atlasY <= y2)
 							return texture.atlasX + texture.getWidth();
 						if(texture.atlasY <= y && texture.atlasY + texture.getHeight() > y)
 							return texture.atlasX + texture.getWidth();
 					}
 			}
 		}
 		return -1;
 	}
 	
 	private static int isAnyoneWithinY(int x, int y, int x2, int y2) {
 		for(int i = 0; i < _textureSets.size(); i++)
 			for(int h = 0; h < _textureSets.get(i).size(); h++) {
 				Texture texture = _textureSets.get(i).get(h);
 				if(texture.inserted && texture.atlasIndex == currentAtlas) {
 					boolean maybe = false;
 					
 					if(texture.atlasX >= x && texture.atlasX <= x2)
 						maybe = true;
 					
 					if(texture.atlasX <= x && texture.atlasX + texture.getWidth() > x)
 						maybe = true;
 					
 					if(maybe) {
 						if(texture.atlasY >= y && texture.atlasY <= y2)
 							return texture.atlasY + texture.getHeight();
 						if(texture.atlasY <= y && texture.atlasY + texture.getHeight() > y)
 							return texture.atlasY + texture.getHeight();
 					}
 			}
 		}
 		return -1;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public static void textureSplit() {
 		_textureSets.put(_textureSetCount, (HashMap<Integer, Texture>)_texture.clone());
 		_textureSetCount++;
 		_textureCount = 0;
 		_texture.clear();
 	}
 	
 	public static void clearAll() {
 		//Debug.print("Clearing bitmaps");
 		for(HashMap<Integer, Texture> h : _textureSets.values()) {
 			for(Texture t : h.values()) {
 				try {
 					t.cleanBitmap();
 				} catch (Exception e) { }
 			}
 		}
 		for(int i = 0; i < _bmp.length; i++) {
 			try {
 				_bmp[i].recycle();
 			} catch (Exception e) { }
 		}
 	}
 	
 	private static String getLastIdString() {
 		try {
 			FileInputStream input = Rokon.getRokon().getActivity().openFileInput("textures");
 			byte[] buffer = new byte[input.available()];
 			input.read(buffer);
 			input.close();
 			return new String(buffer);
 		} catch (Exception e) { 
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	private static void saveLastIdString() {
 		try {
 			FileOutputStream output = Rokon.getRokon().getActivity().openFileOutput("textures", 0);
 			output.write(idString.getBytes());
 			output.close();
 		} catch (Exception e) { 
 			e.printStackTrace();
 		}
 	}
 	
 	private static void loadOldSettings() {
 		try {
 			FileInputStream input = Rokon.getRokon().getActivity().openFileInput("textures_info");
 			byte[] buffer = new byte[input.available()];
 			input.read(buffer);
 			input.close();
 			parseOldSettings(new String(buffer));
 			return;
 		} catch (Exception e) { 
 			e.printStackTrace();
 		}
 	}
 	
 	private static void parseOldSettings(String string) {
 		String line[] = string.split("\n");
 		for(int i = 0; i < line.length; i++) {
 			String info[] = line[i].split(",");
 			for(int curK = 0; curK <= _textureSetCount; curK++) {
 				HashMap<Integer, Texture> textureSet = _textureSets.get(curK);
 				for(int j = 0; j < _textureSets.get(curK).size(); j++) {
 					Texture texture = textureSet.get(j);
 					if(texture.assetPath.equals(info[0])) {
 						texture.suggestX = Integer.parseInt(info[1]);
 						texture.suggestY = Integer.parseInt(info[2]);
 						texture.suggestAtlas = Integer.parseInt(info[3]);
 						break;
 					}
 				}
 			}
 		}
 	}
 	
 	private static void saveNewSettings() {
 		String settings = "";
 		for(int curK = 0; curK <= _textureSetCount; curK++) {
 			for(int i = 0; i < _textureSets.get(curK).size(); i++) {
 				HashMap<Integer, Texture> textureSet = _textureSets.get(curK);
 				Texture texture = textureSet.get(i);
 				settings += texture.assetPath + "," + texture.atlasX + "," + texture.atlasY + "," + texture.atlasIndex + "\n";
 			}
 		}
 		try {
 			FileOutputStream output = Rokon.getRokon().getActivity().openFileOutput("textures_info", 0);
 			output.write(settings.getBytes());
 			output.close();
 		} catch (Exception e) { 
 			e.printStackTrace();
 		}
 	}
 	
 	public static void reloadTexture(int index, Bitmap bitmap) {
 		reloadTextures = true;
 		if(!reloadTextureIndices.contains(index))
 			reloadTextureIndices.add(index);
 		_bmp[index] = bitmap;
 	}
 }
