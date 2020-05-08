 package tmc.BetterProtected.svc;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.bukkit.configuration.file.YamlConfiguration;
 import org.bukkit.craftbukkit.LoggerOutputStream;
 import tmc.BetterProtected.domain.ProtectedBlock;
 import tmc.BetterProtected.domain.ProtectedChunk;
 import tmc.BetterProtected.domain.ProtectedChunkKey;
 import tmc.BetterProtected.domain.ProtectedWorld;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.logging.Logger;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class TransformationService {
    public static final String FILE_REGEX = ".*\\\\(-*\\d{0,5}).(-*\\d{0,5})\\.yml";
     private final Pattern pattern;
     private Logger minecraftLog = Logger.getLogger("Minecraft");
 
     public TransformationService() {
         pattern = Pattern.compile(FILE_REGEX);
     }
 
     public ProtectedWorld buildWorldFromFolder(String location, String worldName) {
         ProtectedWorld world = new ProtectedWorld();
 
         File folder = new File(location);
 
         File[] files = folder.listFiles();
         for (File file : files) {
             if(file.isFile())    {
                 try {
                     transformFile(file.getAbsolutePath(), world);
                 } catch (IOException e) {
                     minecraftLog.severe("java.io.IOException: File Stream failed to parse filename.");
                     e.printStackTrace();
                 } catch (InvalidConfigurationException e) {
                     minecraftLog.warning("Unable to parse chunk data from file: " + file.getAbsolutePath());
                 }
             }
         }
 
         return world;
     }
 
     public void transformFile(String fileName, ProtectedWorld world) throws IOException, InvalidConfigurationException {
         ProtectedChunkKey key = parseChunkKeyFromFileName(fileName);
         if (null == key) {
             throw new InvalidConfigurationException();
         }
 
         minecraftLog.info("Parsing Chunk at coordinates x: " + key.getX() + ", z: " + key.getZ() + ".");
 
         ProtectedChunk chunk = world.getChunkFrom(key);
 
         YamlConfiguration configuration = new YamlConfiguration();
         configuration.load(fileName);
 
         for (String path : configuration.getKeys(false)) {
             String[] coordinates = path.split(",");
             int x = Integer.parseInt(coordinates[0]);
             int y = Integer.parseInt(coordinates[1]);
             int z = Integer.parseInt(coordinates[2]);
             chunk.addBlock(new ProtectedBlock(x, y, z,
                     Material.getMaterial(configuration.getInt(path + ".type")),
                     configuration.getString(path + ".player")));
         }
     }
 
     public ProtectedChunkKey parseChunkKeyFromFileName(String fileName) {
         Matcher matcher = pattern.matcher(fileName);
 
         if (matcher.matches()) {
             Integer x = Integer.parseInt(matcher.group(1));
             Integer z = Integer.parseInt(matcher.group(2));
 
             return new ProtectedChunkKey(x, z);
         }
 
         return null;
     }
 }
