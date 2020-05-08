 package bstramke.NetherStuffsCore;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipFile;
 
 import javax.swing.plaf.metal.MetalIconFactory.FolderIcon16;
 
 import net.minecraft.client.Minecraft;
 import net.minecraftforge.common.Configuration;
 import cpw.mods.fml.relauncher.IClassTransformer;
 
 public class NetherStuffsASM implements IClassTransformer {
 
 	public static HashMap<String, String> override = new HashMap<String, String>();
 	public static HashMap<String, String> obfStrings = new HashMap<String, String>();
 
 	public static void addClassOverride(String name, String description) {
 		override.put(name, description);
 	}
 
 	public NetherStuffsASM() {
 		try {
 			File configDir = new File(".", "config");
 			File config = new File(configDir.getCanonicalPath() + File.separator + "NetherStuffs.cfg");
 			if (config.isFile()) {
 				FileReader input = new FileReader(config);
 				BufferedReader bufRead = new BufferedReader(input);
 				String line;
 				int count = 0;
 				line = bufRead.readLine();
 				count++;
 				while (line != null) {
 					if (line.indexOf("OverrideChunkClass") >= 0) {
 						if (line.indexOf("=true") >= 0)
 							CoreModContainer.bOverrideChunk = true;
 						else
 							CoreModContainer.bOverrideChunk = false;
 					} else if (line.indexOf("OverrideBlockPaneClass") >= 0) {
 						if (line.indexOf("=true") >= 0)
 							CoreModContainer.bOverrideBlockPane = true;
 						else
 							CoreModContainer.bOverrideBlockPane = false;
 					} else if (line.indexOf("OverrideBlockBreakableClass") >= 0) {
 						if (line.indexOf("=true") >= 0)
 							CoreModContainer.bOverrideBlockBreakable = true;
 						else
 							CoreModContainer.bOverrideBlockBreakable = false;
					} 
 					line = bufRead.readLine();
 					count++;
 				}
 				bufRead.close();
 
 			} else {
 				CoreModContainer.bOverrideBlockBreakable = true;
 				CoreModContainer.bOverrideBlockPane = true;
 				CoreModContainer.bOverrideChunk = true;
 			}
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		if (CoreModContainer.bOverrideBlockBreakable)
 			NetherStuffsASM.addClassOverride("akm", "net/minecraft/src/BlockBreakable.java");
 		if (CoreModContainer.bOverrideBlockPane)
 			NetherStuffsASM.addClassOverride("amp", "net/minecraft/src/BlockPane.java");
 		if (CoreModContainer.bOverrideChunk)
 			NetherStuffsASM.addClassOverride("zz", "net.minecraft.world.chunk.Chunk");
 	}
 
 	@Override
 	public byte[] transform(String name, byte[] bytes) {
 		// System.out.println("try override: " + name);
 		if (override.containsKey(name)) {
 			System.out.println("attempting override of " + name + " from " + NetherStuffsCorePlugin.myLocation.getName());
 			bytes = overrideBytes(name, bytes, NetherStuffsCorePlugin.myLocation);
 		}
 		return bytes;
 	}
 
 	public static byte[] overrideBytes(String name, byte[] bytes, File location) {
 		try {
 			ZipFile zip = new ZipFile(location);
 			ZipEntry entry = zip.getEntry(name.replace('.', '/') + ".class");
 			if (entry == null)
 				System.out.println(name + " not found in " + location.getName());
 			else {
 				DataInputStream zin = new DataInputStream(zip.getInputStream(entry));
 				bytes = new byte[(int) entry.getSize()];
 				zin.readFully(bytes);
 				zin.close();
 				System.out.println(name + " was overriden from " + location.getName());
 			}
 			zip.close();
 		} catch (Exception e) {
 			throw new RuntimeException("Error overriding " + name + " from " + location.getName(), e);
 		}
 		return bytes;
 	}
 }
