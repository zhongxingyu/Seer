 package pl.spaceshooters.gfx.texturepack;
 
 import java.io.BufferedReader;
 import java.io.Closeable;
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import pl.blackburn.graphics.Texture;
 
 public class CustomTexturePack implements ITexturePack, Closeable {
 	
 	private File file;
 	private ZipFile zip;
 	
 	public CustomTexturePack(File file) throws IOException {
 		if (zip == null) {
 			this.file = file;
 			zip = new ZipFile(file);
 		}
 	}
 	
 	@Override
 	public String getName() {
 		ZipEntry entry = zip.getEntry("name.txt");
 		if (entry != null) {
 			try (BufferedReader r = new BufferedReader(new InputStreamReader(zip.getInputStream(entry)))) {
 				return r.readLine();
 			} catch (IOException e) {
 				System.err.println("Error while reading the name of current texture pack!");
 			}
 		}
 		
 		return "Unknown pack";
 	}
 	
 	@Override
 	public Texture getTexture(String texturePath) {
 		return this.getTexture(texturePath, 1);
 	}
 	
 	@Override
 	public Texture getTexture(String texturePath, float scale) {
 		ZipEntry texture = zip.getEntry("data/textures/vanilla/" + texturePath);
 		if (texture != null) {
 			try {
				return new Texture(zip.getInputStream(texture), 1);
 			} catch (IOException e) {
 				// Shouldn't happen, but...
 				return DefaultTexturePack.getInstance().getTexture(texturePath);
 			}
 		} else
 			return DefaultTexturePack.getInstance().getTexture(texturePath);
 	}
 	
 	@Override
 	public File getFile() {
 		return file;
 	}
 	
 	@Override
 	public ZipFile getZipFile() {
 		return zip;
 	}
 	
 	@Override
 	public void close() throws IOException {
 		zip.close();
 	}
 }
