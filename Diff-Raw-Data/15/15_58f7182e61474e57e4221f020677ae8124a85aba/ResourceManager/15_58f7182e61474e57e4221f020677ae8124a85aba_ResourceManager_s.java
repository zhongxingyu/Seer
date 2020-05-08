 package game;
 
 import static org.lwjgl.opengl.GL11.*;
 import static org.lwjgl.opengl.GL20.*;
 
 import java.awt.Font;
 import java.io.BufferedInputStream;
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStreamWriter;
 import java.nio.FloatBuffer;
 
 import javax.xml.parsers.*;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 import javax.xml.transform.stream.StreamResult;
 
 import objects.Building;
 import objects.Buildings;
 import objects.Drawable;
 import objects.ObjectLoader;
 
 import org.lwjgl.BufferUtils;
 import org.lwjgl.opengl.Display;
 import org.newdawn.slick.SlickException;
 import org.newdawn.slick.UnicodeFont;
 import org.newdawn.slick.font.effects.ColorEffect;
 import org.newdawn.slick.openal.Audio;
 import org.newdawn.slick.openal.AudioLoader;
 import org.newdawn.slick.opengl.Texture;
 import org.newdawn.slick.opengl.TextureLoader;
 import org.w3c.dom.*;
 
 import objects.Entity;
 
 import animation.Animatable;
 
 /**
  * The resourcemanager provides static methods for accessing resources.
  * It also loads them when the game starts and maintains the list of objects.
  * @author Benedikt Ringlein
  */
 
 public class ResourceManager {
 	
 	/**
 	 * This font is used by buttons and labels in the gui
 	 */
 	public final static UnicodeFont Arial12 = addFont(new Font("Arial",0,12));
 	public final static UnicodeFont Arial15 = addFont(new Font("Arial",0,15));
 	public final static UnicodeFont Arial15B = addFont(new Font("Arial",Font.BOLD,15));
 	public final static UnicodeFont Arial30B = addFont(new Font("Arial",Font.BOLD,30));
 	
 	public final static String PLACEHOLDER1 = "%1%";
 	public final static String PLACEHOLDER2 = "%2%";
 	
 	//The shaders, currently not used
 	public static int shaderProgram, vertexShader, fragmentShader;
 	
 	//Some xml stuff that is only used internally
 	static Document langFile = null;
 	static Document settingsFile = null;
 	static DocumentBuilder builder = null;
 	
 	//The path of the settings file
 	static final String FILE_SETTINGS = "res/settings/settings.xml";
 	
 	public static final String objectspath = "/res/objects/";
 	public static final String soundspath = "/res/sounds/";
 	public static final String texturespath = "/res/textures/";
 	public static final String shaderpath = "/res/shader/";
 	
 	//The objects (Actually loads and pares a file and generates a displaylist)
 	public final static int[] OBJECT_HOUSE = addObject("house");
 	public final static int[] OBJECT_TERRAIN = addObject("terrain");
 	public final static int[] OBJECT_SKYBOX = addObject("skybox");
 	public final static int[] OBJECT_BIGHOUSE = addObject("bighouse");
 	public final static int[] OBJECT_STREET = addObject("streetsegment");
 	public final static int[] OBJECT_GRIDCELL = addObject("gridcell");
 	public final static int[] OBJECT_PLACEHOLDER = addObject("placeholder");
 	
 	//The audio files
 	public final static Audio SOUND_DROP = addSound("WAV", "drop.wav");
 	public final static Audio SOUND_DESTROY = addSound("WAV", "destroy.wav");
 	public final static Audio SOUND_SELECT = addSound("WAV", "select.wav");
 	
 	//Loads the textures
 	public final static Texture TEXTURE_ICON16 = addTexture("icon16.png");
 	public final static Texture TEXTURE_ICON32 = addTexture("icon32.png");
 	public final static Texture TEXTURE_ICON256 = addTexture("icon256.png");
 	public final static Texture TEXTURE_SKYBOX = addTexture("skybox.png");
 	public final static Texture TEXTURE_TERRAIN = addTexture("mars.png");
 	public final static Texture TEXTURE_STREET = addTexture("street.png");
 	public final static Texture TEXTURE_HOUSE = addTexture("housetexture.png");
 	public final static Texture TEXTURE_BIGHOUSE = addTexture("bighousetexture.png");
 	public final static Texture TEXTURE_EMPTY = addTexture("empty.png");
 	public final static Texture TEXTURE_MAINMENUBG = addTexture("mainmenubg.png");
 	public final static Texture TEXTURE_MAINMENUFF = addTexture("ForceField.png");
 	public final static Texture TEXTURE_MARSCITYLOGO = addTexture("marscitylogo.png");
 	public final static Texture TEXTURE_FORCEFIELDBG = addTexture("forcefieldbackground2.png");
 	public final static Texture TEXTURE_MSGBOX = addTexture("msgbox.png");
 	public final static Texture TEXTURE_GUITOOLSBG = addTexture("guitoolsBG.png");
 	public final static Texture TEXTURE_GUITOOLTIP = addTexture("guitooltip.png");
 	public final static Texture TEXTURE_GUIMENUBUTTON = addTexture("guimenubutton.png");
 	public final static Texture TEXTURE_GUIDELETE = addTexture("guidelete.png");
 	public final static Texture TEXTURE_GUITOOLBAR = addTexture("guitoolbar.png");
 	public final static Texture TEXTURE_GUIMENU = addTexture("guimenu.png");
 	public final static Texture TEXTURE_GUIBUTTON = addTexture("guibutton.png");
 	public final static Texture TEXTURE_GUIBUTTONDOWN = addTexture("guibuttondown.png");
 	public final static Texture TEXTURE_GUIBUTTON2 = addTexture("guibutton2.png");
 	public final static Texture TEXTURE_GUIBUTTON2DOWN = addTexture("guibutton2down.png");
 	public final static Texture TEXTURE_GUILABELBG = addTexture("guilabelbg.png");
 	public final static Texture TEXTURE_GUILABELBGL = addTexture("guilabelbgl.png");
 	public final static Texture TEXTURE_GUILABELBGR = addTexture("guilabelbgr.png");
 	public final static Texture TEXTURE_PARTICLEFOG = addTexture("fogparticle.png");
 	public final static Texture TEXTURE_GUIDELETEBORDER = addTexture("guideleteborder.png");
 	public final static Texture TEXTURE_GUICAMERAMOVE = addTexture("cameramove.png");
 	public final static Texture TEXTURE_GUICAMERAROTATE = addTexture("camerarotate.png");
 	public final static Texture TEXTURE_GUIBUILDINGSPANEL = addTexture("buildingspanel.png");
 	public final static Texture TEXTURE_GUIBUILDINGSPANELL = addTexture("buildingspanell.png");
 	public final static Texture TEXTURE_GUITHUMBSTREET = addTexture("thumbstreet.png");
 	public final static Texture TEXTURE_GUITHUMBHOUSE = addTexture("thumbhouse.png");
 	public final static Texture TEXTURE_GUITHUMBBIGHOUSE = addTexture("thumbbighouse.png");
 	public final static Texture TEXTURE_GUITEXTFIELD = addTexture("guitextfield.png");
 	public final static Texture TEXTURE_GUITEXTFIELDL = addTexture("guitextfieldl.png");
 	public final static Texture TEXTURE_GUITEXTFIELDR = addTexture("guitextfieldr.png");
 	public final static Texture TEXTURE_CPSHADOW = addTexture("cpshadow.png");
 	public final static Texture TEXTURE_SCROLLUP = addTexture("scrollup.png");
 	public final static Texture TEXTURE_SCROLLDOWN = addTexture("scrolldown.png");
 	public final static Texture TEXTURE_LOADABORT = addTexture("loadabort.png");
 	public final static Texture TEXTURE_CPSPEC = addTexture("cpspec.png");
 	public final static Texture TEXTURE_BUILDINGINFO = addTexture("buildinginfo.png");
 	public final static Texture TEXTURE_MONEYBG = addTexture("moneybg.png");
 	public final static Texture TEXTURE_GRAPHTRANSITION = addTexture("graphtransition.png");
 	public final static Texture TEXTURE_PLACEHOLDER = addTexture("placeholder.png");
 	public final static Texture TEXTURE_GUITHUMBPLACEHOLDER = addTexture("placeholderthumb.png");
 	public final static Texture TEXTURE_DATAVIEWBUTTONENERGY = addTexture("dataviewenergy.png");
 	public final static Texture TEXTURE_DATAVIEWBUTTONSECUTIRY = addTexture("dataviewsecurity.png");
 	public final static Texture TEXTURE_DATAVIEWBUTTONHEALTH = addTexture("dataviewhealth.png");
 	public final static Texture TEXTURE_DATAVIEWBUTTONGARBAGE = addTexture("dataviewgarbagecollection.png");
 	public final static Texture TEXTURE_DATAVIEWBUTTONINTERNET = addTexture("dataviewinternet.png");
 	public final static Texture TEXTURE_GUICHECKBOX = addTexture("guicheckbox.png");
 	public final static Texture TEXTURE_GUICHECKED = addTexture("guichecked.png");
 	public final static Texture TEXTURE_GUIRADIOBUTTON = addTexture("guiradiobutton.png");
 	public final static Texture TEXTURE_GUIRADIOCHECKED = addTexture("guiradiochecked.png");
 	public final static Texture TEXTURE_DATAVIEWBUTTONHAPPINESS = addTexture("dataviewhappiness.png");
 	
 	/**
 	 * Initializes the Resources that need to be initialized
 	 */
 	public static void init()
 	{
 		
 		//Set up the shader
 		Main.splashscreen.setInfo("Loading shader...");
 		setupShader("shader.v","shader.f");
 		
 		//Load and parse the Language file
 		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
 		try {
 			builder = factory.newDocumentBuilder();
 		} catch (ParserConfigurationException e) {
 			e.printStackTrace();
 		}
 		
 		Buildings.init();
 		
 		//create necessary folders and extract files
 		Main.splashscreen.setInfo("Creating folders...");
 		if(!(new File("res")).exists()||Main.debugMode){
 			(new File("res")).mkdir();
 			(new File("res/lang")).mkdir();
 			
 			(new File("res/settings")).mkdir();
 			(new File("res/cities")).mkdir();
 			Main.log("Created necessary folders.");
 			try {
 				(new File("res/lang/DE.xml")).createNewFile();
 				(new File(FILE_SETTINGS)).createNewFile();
 				
 				//settings.xml
 				BufferedReader input = new BufferedReader(new InputStreamReader(ResourceManager.class.getResourceAsStream("/res/settings/settings.xml")));
 				BufferedWriter output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("res/settings/settings.xml"))));
 			
 				String line;
 				while((line=input.readLine())!=null){
 					output.write(line+System.lineSeparator());
 				}
 				input.close();
 				output.close();
 				
 				//DE.xml
 				input = new BufferedReader(new InputStreamReader(ResourceManager.class.getResourceAsStream("/res/lang/DE.xml")));
 				output = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(new File("res/lang/DE.xml"))));
 			
 				while((line=input.readLine())!=null){
 					output.write(line+System.lineSeparator());
 				}
 				input.close();
 				output.close();
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 		}
 		
 		//make XML files available for the static methods
 		Main.splashscreen.setInfo("Loading xml files...");
 		settingsFile = addXML("res/settings/settings.xml");
 		langFile = addXML("res/lang/"+getSetting("lang")+".xml");
 	}
 
 	/**
 	 * Loads a unicode font
 	 * @param font Font to load
 	 * @return The unicode font ocject
 	 */
 	@SuppressWarnings("unchecked")
 	public static UnicodeFont addFont(Font font)
 	{
 		UnicodeFont ufont = new UnicodeFont(font);
 		ufont.getEffects().add(new ColorEffect());
 		ufont.addAsciiGlyphs();
 		try {
 			Main.splashscreen.setInfo("Loading Font: "+font.getFontName()+" "+font.getSize());
 			ufont.loadGlyphs();
 			return ufont;
 		} catch (SlickException e) {
 			e.printStackTrace();
 		}
 		return null;
 	}
 
 	/**
 	 * Loads a texture from the specified input stream
 	 * @param stream The imput stream
 	 * @return The loaded texture
 	 */
  	public static Texture LoadTexture(InputStream stream)
 	{
 		try {
 			return TextureLoader.getTexture("PNG", new BufferedInputStream(stream));
 		} catch (FileNotFoundException e) {
 			System.err.println("Texture file not found.");
 			e.printStackTrace();
 			Display.destroy();
 			System.exit(1);
 		} catch (IOException e) {
 			e.printStackTrace();
 			Display.destroy();
 			System.exit(1);
 		}
 		return null;
 	}
 	
 	/**
 	 * Deletes an animatable from the render list ()for compatibility
 	 * @param obj
 	 */
 	public static void deleteObject(Animatable obj)
 	{
 		Buildings.buildings.remove(obj);
 	}
 	
 	/**
 	 * Deletes an drawable from the render list ()for compatibility
 	 * @param obj
 	 */
 	public static void deleteObject(Drawable obj)
 	{
 		Buildings.buildings.remove(obj);
 	}
 	
 	/**
 	 * Deletes an object from the render list
 	 * @param obj The objects index
 	 */
 	public static void deleteObject(int obj)
 	{
 		Buildings.buildings.remove(obj);
 	}
 	
 	/**
 	 * Adds an object to the render list
 	 * @param path Path to the .obj file
 	 * @return An integer representing the displaylist
 	 */
 	public static int[] addObject(String path)
 	{
 		System.out.println(path);
 		try {
 			Main.log("Loading object: "+path);
 			Main.splashscreen.setInfo("Loading object: "+path);
 			int[] i = new int[]{-1,-1,-1};
			i[0]=ObjectLoader.createDisplayList(ObjectLoader.loadModel(path,0));
 			return i;
 		} catch (Exception e) {
 			e.printStackTrace();
 			Main.splashscreen.label2.setText("Error! Failed to load object: "+path);
 			Main.log("Failed to load object: "+path);
 		}
 		return (new int[]{-1,-1,-1});
 	}
 	
 	/**
 	 * Adds a sound file
 	 * @param format The files format, e.g. "WAV"
 	 * @param path The path of the sound file
 	 * @return The loaded Audio
 	 */
 	public static Audio addSound(String format,String path)
 	{
 		path = soundspath + path;
 		Main.log("Loading sound: "+path);
 		Main.splashscreen.setInfo("Loading sound: "+path);
 		try {
 			return AudioLoader.getAudio(format, new BufferedInputStream(ResourceManager.class.getResourceAsStream(path)));
 		} catch (Exception e) {
 			e.printStackTrace();
 			Main.splashscreen.setInfo("Error! Failed to load sound: "+path);
 			Main.log("Failed to load sound: "+path);
 		}
 		return null;
 	}
 	
 	/**
 	 * Loads a texture
 	 * @param path The path of the texture file (has to be .png!)
 	 * @return The loaded texture
 	 */
 	public static Texture addTexture(String path)
 	{
 		path = texturespath + path;
 		Main.log("Loading texture: "+path);
 		Main.splashscreen.setInfo("Loading texture: "+path);
 		try {
 			return LoadTexture(ResourceManager.class.getResourceAsStream(path));
 		} catch (Exception e) {
 			e.printStackTrace();
 			Main.splashscreen.label2.setText("Error! Failed to load texture: "+path);
 			Main.log("Failed to load texture: "+path);
 		}
 		return null;
 	}
 	
 	/**
 	 * Returns a the localized String from the language file
 	 * @param input
 	 * @return The localized strong or "String not found: #"
 	 */
 	public static String getString(String input)
 	{
 		String output =  null;
 		if(langFile==null)return "Unable to load language file for "+getSetting("lang");
 		try {
 			output =  langFile.getElementsByTagName(input).item(0).getTextContent();
 		} catch (Exception e) {
 		}
 		
 		if(output==null){
 			System.err.println("String not found: "+input);
 			return input;
 		}
 		return output;
 	}
 	
 	/**
 	 * Returns a the setting from the settings file
 	 * @param setting The setting that should be loaded
 	 * @return The value of the setting or "Setting not found: #"
 	 */
 	public static String getSetting(String setting)
 	{
 		String output =  null;
 		if(settingsFile==null){
 			System.err.println("Unable to load the settings file!");
 			Game.exit();
 		}
 		try {
 			output =  settingsFile.getElementsByTagName(setting).item(0).getTextContent();
 		} catch (Exception e) {
 		}
 		
 		if(output==null){
 			System.err.println("Setting not found: "+setting);
 			return "Setting not found: "+setting;
 		}
 		return output;
 	}
 	
 	/**
 	 * Writes a setting into the settings file
 	 * @param setting The setting that should be written
 	 * @param value The new value of the setting
 	 */
 	public static void setSetting(String setting, String value)
 	{
 		try {
 			settingsFile.getElementsByTagName(setting).item(0).setTextContent(value);
 		} catch (Exception e) {
 			e.printStackTrace();
 			System.err.println("Can't write setting "+setting);
 		}
 		Transformer transformer;
 		try {
 			transformer = TransformerFactory.newInstance().newTransformer();
 			 DOMSource        source = new DOMSource(settingsFile);
 	         FileOutputStream os     = new FileOutputStream(new File(FILE_SETTINGS));
 	         StreamResult     result = new StreamResult(os);
 	         transformer.transform(source, result);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
         
 	}
 	
 	/**
 	 * Loads an XML file
 	 * @param path Path of the file
 	 * @return A Document representing the file
 	 */
 	public static Document addXML(String path)
 	{
 		if(path==null){
 			return builder.newDocument();
 		}
 		Main.log("Loading XML: "+path);
 		try {
 			Document file = builder.parse(new File(path));
 			return file;
 		} catch ( Exception e) {
 			e.printStackTrace();
 			Main.log("Error! Unable to load the XML file: "+path);
 		}
 		return null;
 	}
 	
 	
 	/**
 	 * Plays a sound from the soundPool
 	 * @param sound The sound that should be played
 	 */
 	public static void playSoundRandom(Audio sound)
 	{
 		try {
 			sound.playAsSoundEffect((float)(Math.random()+0.5), 1f, false);
 		} catch (Exception e) {e.printStackTrace();}
 	}
 	
 	/**
 	 * Plays a sound from the soundPool
 	 * @param sound The sound that should be played
 	 */
 	public static void playSound(Audio sound)
 	{
 		try {
 			sound.playAsSoundEffect(1f, 1f, false);
 		} catch (Exception e) {e.printStackTrace();}
 	}
 	
 	/**
 	 * Return the object at the given index in the render list
 	 * @param index
 	 * @return
 	 */
 	public static Building getObject(int index)
 	{
 		if(index==-1)return new Building(-1);
 		return Buildings.buildings.get(index);
 	}
 	
 	/**
 	 * Sets up a shader
 	 * @param vert The vertex shader (path)
 	 * @param frag The fragment shader (path)
 	 */
 	public static void setupShader(String vert, String frag)
 	{
 		shaderProgram = glCreateProgram();
 		vertexShader = glCreateShader(GL_VERTEX_SHADER);
 		fragmentShader = glCreateShader(GL_FRAGMENT_SHADER);
 		StringBuilder vertexShaderSource = readFile(shaderpath+vert);
 		StringBuilder fragmentShaderSource = readFile(shaderpath+frag);
 		glShaderSource(vertexShader, vertexShaderSource);
 		glCompileShader(vertexShader);
 		if(glGetShaderi(vertexShader, GL_COMPILE_STATUS)==GL_FALSE){
 			System.err.println("Could not compile the vertex shader!");
 			Display.destroy();
 			System.exit(1);
 		}
 		glShaderSource(fragmentShader, fragmentShaderSource);
 		glCompileShader(fragmentShader);
 		if(glGetShaderi(fragmentShader, GL_COMPILE_STATUS)==GL_FALSE){
 			System.err.println("Could not compile the fragment shader!");
 			Display.destroy();
 			System.exit(1);
 		}
 		glAttachShader(shaderProgram, vertexShader);
 		glAttachShader(shaderProgram, fragmentShader);
 		glLinkProgram(shaderProgram);
 		glValidateProgram(shaderProgram);
 	}
 	
 	/**
 	 * Loads a file into a StringBuilder
 	 * @param path The Path to the File
 	 * @return a StringBuilder conatining hte Filecontents
 	 */
 	private static StringBuilder readFile(String path)
 	{
 		Main.log("Reading file: "+path);
 		StringBuilder string = new StringBuilder();
 		try {
 			BufferedReader reader = new BufferedReader(new InputStreamReader(ResourceManager.class.getResourceAsStream(path)));
 			
 			String line;
 			while((line=reader.readLine())!=null)
 			{
 				string.append(line);
 				string.append("\r\n");
 			}
 			reader.close();
 			return string;
 		} catch (Exception e) {
 			System.err.println("File not found: "+path);
 			Main.log("Error! Unable to read file: "+path);
 			e.printStackTrace();
 		}
 		return null;
 	}
 	
 	public static int getHoveredBuildingtype(int hoveredEntity)
 	{
 		if(hoveredEntity==-1)return -1;
 		return getObject(hoveredEntity).getBuildingType();
 	}
 
 	public static FloatBuffer toFlippedFloatBuffer(float... floats) {
 	    FloatBuffer b = BufferUtils.createFloatBuffer(floats.length);
 	    b.put(floats);
 	    b.flip();
 	    return b;
 	}
 	
 	public static String pathToCityname(String path)
 	{
 		return ((new File(path)).getName()).substring(0, ((new File(path)).getName()).length()-5);
 	}
 	
 	public static String getBtDescription(int bt)
 	{
 		return ResourceManager.getString("DESCRIPTION_"+Buildings.getBuildingType(bt).getName());
 	}
 	
 	public static String getBtDescription2(int bt)
 	{
 		return ResourceManager.getString("DESCRIPTION2_"+Buildings.getBuildingType(bt).getName());
 	}
 	
 	/**
 	 * Chosses the correct LoD model of the entity and draws it
 	 * @param e The entity to draw
 	 */
 	public static void callLODList(Entity e)
 	{
 		int dist = (int) Math.sqrt((e.getX()-Main.camera.getCx())*(e.getX()-Main.camera.getCx())+(e.getY()-Main.camera.getCy())*(e.getY()-Main.camera.getCy())+(e.getZ()-Main.camera.getCz())*(e.getZ()-Main.camera.getCz()));
 		int lod;
 		if(dist<Main.LOD1){
 			lod = 0;
 		}else if(dist<Main.LOD2){
 			lod = 1;
 		}else{
 			lod = 2;
 		}
 		if(e.getDisplayList()[lod]!=-1){
 			glCallList(e.getDisplayList()[lod]);
 		}else{
 			//If correct LoD model is not available, try to use a lower LoD model, or else a higher one
			for(int i=2;i>=0;i++)
 			{
 				if(e.getDisplayList()[i]!=-1){
 					glCallList(e.getDisplayList()[i]);
 					break;
 				}
 			}
 		}	
 	}
 	
 }
