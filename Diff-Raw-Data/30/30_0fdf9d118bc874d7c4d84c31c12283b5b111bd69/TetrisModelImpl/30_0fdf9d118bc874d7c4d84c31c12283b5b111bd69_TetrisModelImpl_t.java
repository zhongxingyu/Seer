 package github.iruuunechka.model;
 
 import github.iruuunechka.view.TetrisView;
 
 import java.util.ArrayList;
 
 public class TetrisModelImpl implements TetrisModel, Runnable {
     private final TetrisView view;
     private volatile boolean isPaused;
     private final int delay;
     private final ArrayList<boolean[]> field;
     private Piece curPiece;
     private PieceFactory pieceFactory;
     private Thread curThread;
 
     public TetrisModelImpl(TetrisView view, int delay, PieceFactory pieceFactory) {
         this.view = view;
         this.delay = delay;
         this.pieceFactory = pieceFactory;
         this.pieceFactory.setStartPosition(view.getWidth() / 2, 0);
         field = new ArrayList<>();
         for (int i = 0; i < view.getHeight(); ++i) {
             field.add(new boolean[view.getWidth()]);
         }
         curPiece = this.pieceFactory.createPiece();
     }
 
     private void deleteLines() {
         int maxDeletedLine = 0;
         int countDeletedLines = 0;
         int size = field.size();
         for (int i = size - 1; i > 0; --i) {
             int j;
             for (j = 0; j < field.get(i).length; ++j) {
                 if (!field.get(i)[j]) {
                     break;
                 }
             }
             if (j == field.get(i).length) {
                 field.remove(i);
                 maxDeletedLine = maxDeletedLine == 0 ? i : maxDeletedLine;
                 countDeletedLines++;
             }
         }
         for (int i = 0; i < countDeletedLines; ++i) {
             field.add(0, new boolean[view.getWidth()]);
         }
         if (countDeletedLines > 0) {
             view.deleteLine(maxDeletedLine);
         }
     }
 
     private boolean check(int[][] blocks) {
         for (int[] block : blocks) {
             if (block[1] < 0 || block[1] >= field.size()) {
                 return false;
             }
             if (block[0] < 0 || block[0] >= field.get(block[1]).length) {
                 return false;
             }
             if (field.get(block[1])[block[0]]) {
                 return false;
             }
         }
         return true;
     }
 
     private void putFigure(int[][] blocks) {
         for (int[] block : blocks) {
             field.get(block[1])[block[0]] = true;
         }
     }
 
     @Override
     public void pause() {
         if (isPaused) {
             return;
         }
         isPaused = true;
         view.pause();
     }
 
     @Override
     public void play() {
         if (!isPaused) {
             return;
         }
         isPaused = false;
         view.play();
     }
 
     @Override
     public void start() {
         curThread = new Thread(this);
         curThread.start();
     }
 
     @Override
     public void gameOver() {
        if (!curThread.isInterrupted()) {
            curThread.interrupt();
        }
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
     public synchronized void moveDown() {
         int[][] curBlocks = curPiece.getDownBlocks();
         if (check(curBlocks)) {
             view.redraw(curPiece.getBlocks(), curBlocks);
             curPiece.down();
         } else {
             putFigure(curPiece.getBlocks());
             deleteLines();
             curPiece = pieceFactory.createPiece();
             if (check(curPiece.getBlocks())) {
                 view.redraw(curPiece.getBlocks(), curPiece.getBlocks());
             } else {
                 gameOver();
                 view.gameOver();
             }
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
         curPiece = pieceFactory.createPiece();
         while (!Thread.interrupted()) {
             if (!isPaused) {
                 moveDown();
                try {
                    Thread.sleep(delay);
                } catch (InterruptedException e) {
                    break;
                }
             }
         }
     }
     
     
 }
