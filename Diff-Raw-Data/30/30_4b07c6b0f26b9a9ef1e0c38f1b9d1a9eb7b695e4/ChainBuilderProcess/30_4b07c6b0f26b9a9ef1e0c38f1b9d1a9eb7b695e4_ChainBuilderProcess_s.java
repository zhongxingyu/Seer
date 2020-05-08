 package net.stickboyproductions.tetrisattack.processors;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Sets;
 import net.stickboyproductions.tetrisattack.constants.Directions;
 import net.stickboyproductions.tetrisattack.enums.BlockState;
 import net.stickboyproductions.tetrisattack.model.Block;
 import net.stickboyproductions.tetrisattack.model.Grid;
 import net.stickboyproductions.tetrisattack.model.Shape;
 
 import java.util.Set;
 
 /**
  * User: Pete
  * Date: 20/11/12
  * Time: 22:13
  */
 public class ChainBuilderProcess {
 
   public ChainBuilderProcess() {
   }
 
   public Set<Block> buildClearableChain(Block startBlock, Grid grid) {
     Set<Block> result = Sets.newHashSet(startBlock);
     if (startBlock != null) {
       result.addAll(expandChain(startBlock, grid, result));
     }
 
     trim(result, grid);
     return result;
   }
 
   private void trim(Set<Block> result, Grid grid) {
    for(Block block : ImmutableSet.copyOf(result)) {
      if(buildXChain(block, grid).size() >= 3) {
         continue;
       }
      if(buildYChain(block, grid).size() >= 3) {
         continue;
       }
       result.remove(block);
     }
   }
 
   private Set<Block> expandChain(Block startBlock, Grid grid, Set<Block> result) {
     Block blockAbove = grid.getBlockToTheDirection(startBlock, Directions.UP);
    if (checkBlockMatch(startBlock, blockAbove)) {
       result.add(blockAbove);
       expandChain(blockAbove, grid, result);
     }
 
     Block blockRight = grid.getBlockToTheDirection(startBlock, Directions.RIGHT);
    if (checkBlockMatch(startBlock, blockRight)) {
      if(!result.contains(blockRight)) {
        result.add(blockRight);
        expandChain(blockRight, grid, result);
      }
     }
 
     Block blockLeft = grid.getBlockToTheDirection(startBlock, Directions.LEFT);
    if (checkBlockMatch(startBlock, blockLeft)) {
      if(!result.contains(blockLeft)) {
        result.add(blockLeft);
        expandChain(blockLeft, grid, result);
      }
     }
 
     return result;
   }
 
   private boolean checkBlockMatch(Block startBlock, Block otherBlock) {
     return otherBlock != null && startBlock.getShape().equals(otherBlock.getShape()) &&
       BlockState.IDLE.equals(otherBlock.getBlockState()) && !otherBlock.getShape().equals(Shape.GREY);
   }
 
   public Set<Block> buildXChain(Block startBlock, Grid grid) {
     Set<Block> result = Sets.newHashSet();
     result.addAll(buildChainForDirection(startBlock, grid, Directions.LEFT));
     result.addAll(buildChainForDirection(startBlock, grid, Directions.RIGHT));
     return result;
   }
 
   public Set<Block> buildYChain(Block startBlock, Grid grid) {
     Set<Block> result = Sets.newHashSet();
     result.addAll(buildChainForDirection(startBlock, grid, Directions.UP));
     result.addAll(buildChainForDirection(startBlock, grid, Directions.DOWN));
     return result;
   }
 
   private Set<Block> buildChainForDirection(Block startBlock,
                                             Grid grid, int direction) {
     if (startBlock == null || startBlock.getShape() == null) {
       return ImmutableSet.of();
     }
 
     Set<Block> result = Sets.newHashSet();
     result.add(startBlock);
     Block block = grid.getBlockToTheDirection(startBlock, direction);
     while (block != null &&
       BlockState.IDLE.equals(block.getBlockState()) &&
       startBlock.getShape().equals(block.getShape())) {
       result.add(block);
       block = grid.getBlockToTheDirection(block, direction);
     }
     return ImmutableSet.copyOf(result);
   }
 }
