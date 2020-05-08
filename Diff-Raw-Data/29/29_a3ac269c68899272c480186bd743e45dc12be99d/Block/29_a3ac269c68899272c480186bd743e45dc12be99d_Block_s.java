 package demo.client.local.game.gui;
 
 import com.google.gwt.canvas.dom.client.Context2d;
 
 import demo.client.local.game.tools.ColorMapper;
 import demo.client.local.game.tools.Size;
 import demo.client.local.game.tools.Size.SizeCategory;
 import demo.client.shared.model.BlockModel;
 import demo.client.shared.model.SquareModel;
 
 /* A class for drawing Block Drop blocks on an HTML5 canvas. */
 public class Block {
 
   /* The BlockModel associated with this instance. */
   private BlockModel model;
   private SizeCategory sizeCategory;
 
   /*
    * Create a Block instance.
    */
   public Block(BlockModel blockModel, SizeCategory sizeCategory) {
     model = blockModel;
     this.sizeCategory = sizeCategory;
   }
 
   /*
    * Draw this block on the given context.
    * 
    * @param x The x coordinate of the position at which to draw this block.
    * 
    * @param y The y coordinate of the position at which to draw this block.
    * 
    * @param context2d The context on which to draw this block.
    */
   public void draw(double x, double y, Context2d context2d) {
     Square square = new Square(sizeCategory);
     // Get an iterator of square coordinates based around the given coordinate (x,y).
     for (SquareModel squareModel : model.getIterator()) {
       square.setInteriorColour(ColorMapper.codeToColour(squareModel.getCode()));
       square.draw(x + indexToCoord(squareModel.getCol(), sizeCategory), y
               + indexToCoord(squareModel.getRow(), sizeCategory), context2d);
     }
   }
 
   /*
    * Check if a given BlockModel is the model for this Block type.
    * 
    * @param activeBlock The block model to compare against the the model stored in this Block.
    * 
    * @return True iff the given BlockModel is the same as the model of this Block instance.
    */
   public boolean isModel(BlockModel activeBlock) {
     return model.equals(activeBlock);
   }
 
   /*
    * Get a path on the given context surrounding all the squares in this block.
    * 
    * @param x The x coordinate of this block's position.
    * 
    * @param y The y coordinate of this block's position.
    * 
    * @param context2d The context on which to create this path.
    */
   void getPath(int x, int y, Context2d context2d) {
     Square square = new Square(sizeCategory);
     for (SquareModel squareModel : model.getIterator()) {
       square.addSquareToCanvasPath(x + indexToCoord(squareModel.getCol(), sizeCategory),
               y + indexToCoord(squareModel.getRow(), sizeCategory), context2d);
     }
   }
 
   void rotate() {
     model.rotate();
   }
 
   public double getCentreRowDiff() {
     return model.getCentreRowDiff();
   }
 
   public double getCentreColDiff() {
     return model.getCentreColDiff();
   }
 
   /*
    * Convert an index (used to locate BlockModels on the BoardModel) to a coordinate (used to draw
    * Blocks on the BoardPage canvas).
    * 
    * @param index The index of a BlockModel on a BoardModel to be converted to a coordinate.
    * 
    * @return The coordinate to pass to the Block.draw method for drawing a block in the correct
    * position.
    */
   public static int indexToCoord(Integer index, SizeCategory sizeCategory) {
     return index * Size.getSize(sizeCategory).BLOCK_SIZE;
   }
 }
