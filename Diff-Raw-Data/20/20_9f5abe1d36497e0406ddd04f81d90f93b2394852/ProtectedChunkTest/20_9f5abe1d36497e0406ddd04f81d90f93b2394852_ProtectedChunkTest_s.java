 package tmc.BetterProtected.domain;
 
 import org.bukkit.Material;
 import org.junit.Before;
 import org.junit.Test;
 
 import static org.hamcrest.CoreMatchers.is;
 import static org.junit.Assert.assertThat;
 import static org.junit.matchers.JUnitMatchers.hasItem;
 
 public class ProtectedChunkTest {
 
     private ProtectedChunk protectedChunk;
 
     @Before
     public void setUp() throws Exception {
         protectedChunk = new ProtectedChunk(12, -13);
     }
 
     @Test
     public void shouldGenerateKeyWhenConstructed() {
         ProtectedChunkKey expectedKey = new ProtectedChunkKey(12, -13);
 
         assertThat(protectedChunk.getKey().getX(), is(expectedKey.getX()));
         assertThat(protectedChunk.getKey().getZ(), is(expectedKey.getZ()));
     }
 
     @Test
     public void shouldAddBlockToChunk() {
         ProtectedBlock jasonBlock = new ProtectedBlock(10, 10, 10, Material.DIRT, "Jason733i");
         protectedChunk.addBlock(jasonBlock);
         
         assertThat(protectedChunk.getBlocksAt(jasonBlock.getKey()), hasItem(jasonBlock));
     }
 
     @Test
    public void shouldGetNumberOfBlocksFromMap() {
         protectedChunk.addBlock(new ProtectedBlock(1, 2, 3, Material.getMaterial(1), "Bob"));
         protectedChunk.addBlock(new ProtectedBlock(1, 2, 3, Material.getMaterial(1), "Bob"));
        protectedChunk.addBlock(new ProtectedBlock(1, 4, 3, Material.getMaterial(1), "Bob"));
        protectedChunk.addBlock(new ProtectedBlock(1, 4, 3, Material.getMaterial(1), "Bob"));
        protectedChunk.addBlock(new ProtectedBlock(1, 4, 3, Material.getMaterial(1), "Bob"));
        protectedChunk.addBlock(new ProtectedBlock(1, 7, 3, Material.getMaterial(1), "Bob"));
         
         assertThat(protectedChunk.numberOfBlocks(), is(6));
     }
 
     @Test
     public void shouldGetZeroNumberOfBlocksIfChunkIsEmpty() {
         assertThat(protectedChunk.numberOfBlocks(), is(0));
     }
     
     
 }
