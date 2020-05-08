 package spaceshooters.gfx.texturepacks;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.zip.ZipFile;
 
 import org.newdawn.slick.Image;
 import org.newdawn.slick.SlickException;
 
 public final class DefaultTexturePack implements ITexturePack {
 	
 	private static final DefaultTexturePack instance = new DefaultTexturePack();
 	
 	private File file;
 	private ZipFile zip;
 	
 	private DefaultTexturePack() {
 		try {
			file = new File(this.getClass().getProtectionDomain().getCodeSource().getLocation().toString().substring(6).replace("%20", " "));
 			zip = new ZipFile(file);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 	}
 	
 	@Override
 	public Image getTexture(String textureName) {
 		try {
 			return new Image("textures/vanilla/" + textureName);
 		} catch (Exception e) {
 			try {
 				return new Image("textures/vanilla_new/" + textureName);
 			} catch (SlickException | RuntimeException e1) {
 				// Technically this one can happen only if none of vanilla packs have the requested texture.
 				e1.printStackTrace();
 			}
 		}
 		
 		return null;
 	}
 	
 	@Override
 	public String getName() {
 		return "Default";
 	}
 	
 	@Override
 	public File getFile() {
 		return file;
 	}
 	
 	@Override
 	public ZipFile getZipFile() {
 		return zip;
 	}
 	
 	public static DefaultTexturePack getInstance() {
 		return instance;
 	}
 }
