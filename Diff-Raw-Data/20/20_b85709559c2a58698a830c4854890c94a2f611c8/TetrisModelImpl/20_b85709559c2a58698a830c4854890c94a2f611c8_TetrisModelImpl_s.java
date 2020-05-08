 package github.iruuunechka.model;
 
 import github.iruuunechka.view.TetrisView;
 
 import java.util.ArrayList;
 
 public class TetrisModelImpl implements TetrisModel, Runnable {
     private final TetrisView view;
    private boolean isPaused;
     private final int delay;
     private final ArrayList<boolean[]> field;
     private Piece curPiece;
     private PieceFactory pieceFactory;
 
     public TetrisModelImpl(TetrisView view, int delay, PieceFactory pieceFactory) {
         this.view = view;
         this.delay = delay;
         this.pieceFactory = pieceFactory;
         field = new ArrayList<>();
         for (int i = 0; i < view.getHeight(); ++i) {
             field.add(new boolean[view.getWidth()]);
         }
         curPiece = this.pieceFactory.createPiece();
     }
 
     private void delay() {
         try {
             Thread.sleep(delay);
         } catch (InterruptedException e) {
             e.printStackTrace();
         }
     }
 
     private void deleteLines() {
         int maxDeletedLine = 0;
         int countDeletedLines = 0;
         for (int i = field.size(); i > 0; --i) {
             int j;
             for (j = 0; j < field.get(i).length; ++j) {
                 if (!field.get(i)[j]) {
                     break;
                 }
             }
             if (j == field.get(i).length) {
                 field.remove(maxDeletedLine);
                 maxDeletedLine = maxDeletedLine == 0 ? i : maxDeletedLine;
                 countDeletedLines++;
             }
         }
         for (int i = 0; i < countDeletedLines; ++i) {
             field.add(0, new boolean[view.getWidth()]);
         }
         view.deleteLine(maxDeletedLine);
     }
 
     private boolean check(int[][] blocks) {
         for (int i = 0; i < blocks.length; ++i) {
             for (int j = 0; j < blocks[0].length; ++j) {
                 if (field.get(i)[j]) {
                     return false;
                 }
             }
         }
         return true;
     }
 
     @Override
     public void pause() {
         isPaused = true;
         view.pause();
     }
 
     @Override
     public void start() {
         isPaused = false;
         view.play();
     }
 
     @Override
     public void moveLeft() {
         int[][] curBlocks = curPiece.getLeftBlocks();
         if (check(curBlocks)) {
             view.redraw(curPiece.getBlocks(), curBlocks);
             curPiece.left();
         }
     }
 
     @Override
     public void moveRight() {
         int[][] curBlocks = curPiece.getRightBlocks();
         if (check(curBlocks)) {
             view.redraw(curPiece.getBlocks(), curBlocks);
             curPiece.right();
         }
     }
 
     @Override
    public void moveDown() {
         int[][] curBlocks = curPiece.getDownBlocks();
         if (check(curBlocks)) {
             view.redraw(curPiece.getBlocks(), curBlocks);
             curPiece.down();
         } else {
             deleteLines();
             curPiece = pieceFactory.createPiece();
         }
     }
 
     @Override
     public void rotate() {
         int[][] curBlocks = curPiece.getRotatedBlocks();
         if (check(curBlocks)) {
             view.redraw(curPiece.getBlocks(), curBlocks);
             curPiece.rotate();
         }
     }
 
     @Override
     public void run() {
        while (!isPaused) {
            moveDown();
            delay();
         }
     }
 }
