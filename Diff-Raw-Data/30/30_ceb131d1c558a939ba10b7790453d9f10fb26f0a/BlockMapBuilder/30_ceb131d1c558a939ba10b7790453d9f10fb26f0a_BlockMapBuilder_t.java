 package niktgar.tod.block;
 
 import java.util.Hashtable;
 
 import lombok.Data;
 import lombok.experimental.Accessors;
import niktgar.tod.block.special.FastBlock;
import niktgar.tod.block.special.NoJumpBlock;
import niktgar.tod.block.special.SlowBlock;
import niktgar.tod.block.special.SuperJumpBlock;
 import niktgar.tod.core.TODException;
 import niktgar.tod.sprite.Sprite;
 import niktgar.tod.sprite.SpriteLoader;
 
 @Accessors(fluent = true)
 @Data
 public class BlockMapBuilder {
 
     private static final int BLOCK_SIZE = 32;
 
     private final SpriteLoader spriteLoader;
     private final Hashtable<Integer, String> blockIdMapping;
 
     public BlockMapBuilder(final SpriteLoader spriteLoader) {
         this.spriteLoader = spriteLoader;
         this.blockIdMapping = new Hashtable<Integer, String>();
         blockIdMapping.put(0, "empty.png");
         blockIdMapping.put(1, "hollow.png");
         blockIdMapping.put(2, "black.png");
         blockIdMapping.put(3, "red.png");
         blockIdMapping.put(4, "green.png");
         blockIdMapping.put(5, "blue_7.png");
     }
 
     public BlockMap buildBlockMap(final int[][] blockIdMap, final BlockLayer currentBlockLayer) throws TODException {
         final Block[][] blockMap = new Block[blockIdMap.length][blockIdMap[0].length];
         for (int r = 0; r < blockMap.length; r++) {
             for (int c = 0; c < blockMap[0].length; c++) {
                 final int blockId = blockIdMap[r][c];
                 final String blockFileString = blockIdMapping.get(blockIdMap[r][c]);
                 final Sprite sprite = spriteLoader.loadMaskedSprite(String.format("blocks/%s", blockFileString), "blocks/_mask.gif");
                 Block block;
                 switch (blockId) {
                 case 0:
                     block = new EmptyBlock(sprite, r * BLOCK_SIZE, c * BLOCK_SIZE);
                     break;
                 case 1:
                     block = new NoJumpBlock(sprite, r * BLOCK_SIZE, c * BLOCK_SIZE);
                     currentBlockLayer.add(block);
                     break;
                 case 2:
                     block = new FastBlock(sprite, r * BLOCK_SIZE, c * BLOCK_SIZE);
                     currentBlockLayer.add(block);
                     break;
                 case 3:
                     block = new SlowBlock(sprite, r * BLOCK_SIZE, c * BLOCK_SIZE);
                     currentBlockLayer.add(block);
                     break;
                 case 4:
                     block = new Block(sprite, r * BLOCK_SIZE, c * BLOCK_SIZE);
                     currentBlockLayer.add(block);
                     break;
                 case 5:
                     block = new SuperJumpBlock(sprite, r * BLOCK_SIZE, c * BLOCK_SIZE);
                     currentBlockLayer.add(block);
                     break;
                 default:
                     block = new Block(sprite, r * BLOCK_SIZE, c * BLOCK_SIZE);
                     currentBlockLayer.add(block);
                 }
                 blockMap[r][c] = block;
             }
         }
         return new BlockMap(blockMap);
     }
 }
