 package tmc.BetterProtected.svc;
 
 import org.bukkit.Material;
 import org.bukkit.configuration.InvalidConfigurationException;
 import org.junit.Before;
 import org.junit.Test;
 import tmc.BetterProtected.domain.*;
 
 import java.io.File;
 import java.io.IOException;
 
 import static org.hamcrest.CoreMatchers.instanceOf;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.CoreMatchers.nullValue;
 import static org.junit.Assert.assertThat;
 import static org.junit.matchers.JUnitMatchers.hasItems;
 
 public class TransformationServiceTest {
     private final String FIXTURE_DIRECTORY = "test\\fixtures\\files";
     private TransformationService transformationService;
 
     @Before
     public void setUp() throws Exception {
         transformationService = new TransformationService();
     }
 
     @Test
    public void shouldParseEitherWindowsOrUnixDirectories() {
        String windowsFile = "random\\director\\in\\your\\file\\system\\-3.100.yml";
        String unixFile = "random/director/in/your/file/system/-3.100.yml";

        ProtectedChunkKey windowsKey = transformationService.parseChunkKeyFromFileName(windowsFile);
        ProtectedChunkKey unixKey = transformationService.parseChunkKeyFromFileName(unixFile);

        assertThat(windowsKey, is(new ProtectedChunkKey(-3, 100)));
        assertThat(unixKey, is(new ProtectedChunkKey(-3, 100)));

    }

    @Test
     public void shouldGetChunkKeyFromValidFileName() {
         String fileName = "random\\director\\in\\your\\file\\system\\-3.100.yml";
 
         ProtectedChunkKey key = transformationService.parseChunkKeyFromFileName(fileName);
 
         assertThat(key, is(new ProtectedChunkKey(-3, 100)));
     }
 
     @Test
     public void shouldGetNullValueFromInvalidFileName() {
         String fileName = "random\\director\\in\\your\\file\\system\\invalid.yml";
 
         ProtectedChunkKey key = transformationService.parseChunkKeyFromFileName(fileName);
 
         assertThat(key, is(nullValue()));
     }
 
     @Test
     public void shouldGetProtectedBlocksFromFile() throws IOException, InvalidConfigurationException {
         TimeFreezeService.freeze();
         String fileName = "test\\fixtures\\10.10.yml";
         ProtectedWorld world = new ProtectedWorld();
 
         transformationService.transformFile(fileName, world);
 
         ProtectedChunk actualChunk = world.getChunkFrom(new ProtectedChunkKey(10, 10));
         ProtectedBlock bobBlock = new ProtectedBlock(1, 1, 1, Material.getMaterial(58), "Bob");
 
         assertThat(actualChunk.getBlocksAt(new ProtectedBlockKey(1)), hasItems(bobBlock));
         TimeFreezeService.unfreeze();
     }
 
     @Test(expected = InvalidConfigurationException.class)
     public void shouldNotProcessFileWithNullChunkKey() throws IOException, InvalidConfigurationException {
         String fileName = "test\\fixtures\\nullChunkKey.yml";
         ProtectedWorld world = new ProtectedWorld();
 
         transformationService.transformFile(fileName, world);
 
         assertThat(world.numberOfChunks(), is(0));
     }
 
     @Test
     public void shouldProcessAllFilesOfAWorldFromOneDirectory() {
         String location = "test\\fixtures\\files";
         File file = new File(location);
         int numberOfFiles = file.list().length;
 
         ProtectedWorld world = transformationService.buildWorldFromFolder(location, "WorldName");
 
         assertThat(world.numberOfChunks(), is(numberOfFiles));
     }
 
     @Test
     public void shouldSkipFilesWithInvalidName() {
         String location = "test\\fixtures";
         File file = new File(location);
         int numberOfFiles = file.list().length;
 
         ProtectedWorld world = transformationService.buildWorldFromFolder(location, "WorldName");
         //Given that we have only 1 valid yml file in that directory,
         // the world should have 1 chunk.
         assertThat(world.numberOfChunks(), is(1));
     }
 
 }
