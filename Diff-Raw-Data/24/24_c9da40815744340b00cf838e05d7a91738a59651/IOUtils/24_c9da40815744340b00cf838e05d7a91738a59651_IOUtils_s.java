 package vazkii.codebase.common;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.src.AnvilChunkLoader;
 import net.minecraft.src.CompressedStreamTools;
 import net.minecraft.src.IChunkLoader;
 import net.minecraft.src.ISaveHandler;
 import net.minecraft.src.NBTTagCompound;
 import net.minecraft.src.World;
 import cpw.mods.fml.common.Side;
 import cpw.mods.fml.relauncher.ReflectionHelper;
 
 public class IOUtils {
 
 	public static void initFiles() {
 		File vazkiiFolder = getVazkiiFolder(getMcDir());
 		if (vazkiiFolder.exists()) return;
 
 		vazkiiFolder.mkdirs();
 	}
 
 	public static File getWorldDir(World world) {
 		try {
 			ISaveHandler worldsaver = world.getSaveHandler();
 			IChunkLoader loader = worldsaver.getChunkLoader(world.provider);
 			File file = ReflectionHelper.<File, AnvilChunkLoader> getPrivateValue(AnvilChunkLoader.class, (AnvilChunkLoader) loader, 3);
 			return file.getName().contains("DIM") ? file.getParentFile() : file;
 		} catch (Exception e) {
 			return null;
 		}
 	}
 
 	public static File createAndGetNBTFile(File f) {
 		try {
 			CompressedStreamTools.readCompressed(new FileInputStream(f));
 		} catch (Exception e) {
 			NBTTagCompound cmp = new NBTTagCompound();
 			try {
 				CompressedStreamTools.writeCompressed(cmp, new FileOutputStream(f));
 			} catch (IOException e1) {
 				e1.printStackTrace();
 			}
 		}
 
 		return f;
 	}
 
 	public static File createAndGetFile(File f) {
 		if (!f.exists()) try {
 			f.getParentFile().mkdirs();
 			f.createNewFile();
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 
 		return f;
 	}
 
 	public static File getMcDir() {
 		return getMcDir("");
 	}
 
 	public static File getMcDir(String dir) {
		CommonUtils.getMc();
 		return CommonUtils.getSide() == Side.CLIENT ? Minecraft.getAppDir("minecraft" + dir) : new File(dir.length() == 0 ? "." : dir.substring(1));
 	}
 
 	public static File getVazkiiFolder(File parent) {
 		return new File(parent, "vazkii");
 	}
 
 	public static File getCacheFile(EnumVazkiiMods mod) {
 		return createAndGetNBTFile(createAndGetFile(new File(new File(getVazkiiFolder(getMcDir()), "/cache"), String.format("%s.dat", mod.getFileName()))));
 	}
 
 	public static File getWorldCacheFile(EnumVazkiiMods mod, World world) {
 		return createAndGetNBTFile(createAndGetFile(new File(new File(getVazkiiFolder(getWorldDir(world)), "/cache"), String.format("%s.dat", mod.getFileName()))));
 	}
 
 	public static File getConfigFile(EnumVazkiiMods mod) {
 		return createAndGetFile(new File(new File(getVazkiiFolder(getMcDir()), "/config"), String.format("%s.cfg", mod.getFileName())));
 	}
 
 	public static NBTTagCompound getTagCompoundInFile(File f) {
 		try {
 			NBTTagCompound cmp = CompressedStreamTools.readCompressed(new FileInputStream(f));
 			return cmp;
 		} catch (IOException e) {
 			NBTTagCompound cmp = new NBTTagCompound();
 			try {
 				CompressedStreamTools.writeCompressed(cmp, new FileOutputStream(f));
 				return getTagCompoundInFile(f);
 			} catch (IOException e1) {
 				return null;
 			}
 		}
 	}
 
 	public static boolean injectNBTToFile(NBTTagCompound cmp, File f) {
 		try {
 			CompressedStreamTools.writeCompressed(cmp, new FileOutputStream(f));
 			return true;
 		} catch (IOException e) {
 			return false;
 		}
 	}
 
 }
