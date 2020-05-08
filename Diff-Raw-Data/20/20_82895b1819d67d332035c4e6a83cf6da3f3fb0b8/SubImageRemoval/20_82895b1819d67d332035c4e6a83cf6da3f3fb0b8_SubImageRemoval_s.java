 package image;
 
 import image.pnm.Pbm;
 
 import java.util.concurrent.ThreadPoolExecutor;
 import java.util.concurrent.TimeUnit;
 import java.util.concurrent.LinkedBlockingQueue;
 
 public class SubImageRemoval
 {
     public static final int NUM_PROCESSORS = Runtime.getRuntime().availableProcessors();
 
     public static void blankRectangle(Pbm image, int rowStart, int colStart, int rows, int cols)
     {
         System.out.println("Blanking "+rows+"x"+cols+" rectangle at: ("+rowStart+", "+colStart+")");
         for(int row=0; row<rows; ++row)
         {
             for(int col=0; col<cols; ++col)
             {
                 image.set(rowStart+row, colStart+col, Pbm.WHITE);
             }
         }
     }
 
     public static void invertSubImage(Pbm image, Pbm sub, int rowStart, int colStart)
     {
         System.out.println("Inverting logo at: ("+rowStart+", "+colStart+")");
         int rows = sub.getRows();
         int cols = sub.getCols();
 
         for(int row=0; row<rows; ++row)
         {
             for(int col=0; col<cols; ++col)
             {
                 if(sub.get(row, col) == Pbm.BLACK)
                 {
                     image.set(rowStart+row, colStart+col, Pbm.WHITE);
                 }
             }
         }
     }
 
     public static int firstNonBlankRow(Pbm image)
     {
         int rows = image.getRows();
         int cols = image.getCols();
         for(int row=0; row<rows; ++row)
         {
             for(int col=0; col<cols; ++col)
             {
                 if(image.get(row, col) == Pbm.BLACK)
                 {
                     return row;
                 }
             }
         }
         return rows;
     }
 
     public static int scoreSection(Pbm main, Pbm sub, int rowStart, int colStart, int cutoff)
     {
         int rows = sub.getRows();
         int cols = sub.getCols();
         int maxRows = main.getRows();
         int maxCols = main.getCols();
 
         int score = 0;
 
         if(rowStart+rows >= maxRows || colStart+cols >= maxCols)
         {
             //System.out.println("Sub image goes off main image at ("+rowStart+", "+colStart+")");
             return Integer.MAX_VALUE;
         }
 
         for(int row=0; row<rows; ++row)
         {
             for(int col=0; col<cols; ++col)
             {
                 if(score >= cutoff)
                 {
                     //System.out.println("cutting it off score: "+score);
                     return Integer.MAX_VALUE;
                 }
                 //System.out.println("Add to score: "+(main.get(rowStart+row, colStart+col) ^ sub.get(row, col)));
                 score += (main.get(rowStart+row, colStart+col) ^ sub.get(row, col));
             }
         }
 
         return score;
     }
 
     public static int[] findImage(final Pbm main, final Pbm sub, int startRowInput, final int rowCount, final int startCol, final int colCount, final int cutoff)
     {
         //System.out.println("Finding subimage...");
         //skip a bunch of rows.
         final int startRow = Math.max(firstNonBlankRow(main), startRowInput);
 
         final int rows = main.getRows();
         final int cols = main.getCols();
         final int subRows = sub.getRows();
         final int subCols = sub.getCols();
 
         int bestRow = -1;
         int bestCol = -1;
         int bestScore = cutoff;
         //System.out.println("Cutoff score: "+bestScore);
         //Lame. Gets around 'final' restriction when using inner classes.
         final int[] best = new int[]{bestRow, bestCol, bestScore};
 
         final int goodEnough = 100;
 
         ThreadPoolExecutor pool = new ThreadPoolExecutor(NUM_PROCESSORS, NUM_PROCESSORS, 1000, TimeUnit.MILLISECONDS, new LinkedBlockingQueue<Runnable>());
         final Object lock = new Object();
 
         //Lame. See above.
         final boolean[] looping = new boolean[]{true};
 
         for(int rowc=startRow; rowc<startRow+rowCount && rowc<rows-subRows && looping[0]; ++rowc)
         {
             final int row = rowc;
             pool.execute(new Runnable()
             {
                 public void run()
                 {
                     for(int col=startCol; col<startCol+colCount && col<cols-subCols && looping[0]; ++col)
                     {
                         int score = scoreSection(main, sub, row, col, best[2]);
                         //System.out.println("curr score: "+score);
                         if(score < best[2])
                         {
                             //System.out.println("New best score: "+score+" - ("+row+", "+col+")");
                             synchronized(lock)
                             {
                                 best[0] = row;
                                 best[1] = col;
                                 best[2] = score;
                             }
                         }
                         if(score < goodEnough)
                         {
                             looping[0] = false;
                         }
                     }
                 }
             });
         }
         try
         {
             pool.shutdown();
             pool.awaitTermination(1000, TimeUnit.SECONDS);
         }
         catch(InterruptedException e)
         {
             e.printStackTrace();
         }
 
         if(best[0] == -1)
         {
             throw new RuntimeException("Unable to find subimage");
         }
 
         return new int[] {best[0], best[1]};
     }
 
     public static void main(String args[])
         throws Exception
     {
         if(args.length < 9)
         {
             System.out.println("Usage: image.SubImageRemoval <main image filename> <sub-image filename> <output filename> <percent close> <start row> <rows to search> <start column> <columns to search> <removal method>");
             System.exit(-1);
         }
 
         String mainFilename = args[0];
         String subFilename = args[1];
         String outFilename = args[2];
         double cutoffPercentage = Double.parseDouble(args[3]);
         int startRow = Integer.parseInt(args[4]);
         int rowCount = Integer.parseInt(args[5]);
         int startCol = Integer.parseInt(args[6]);
         int colCount = Integer.parseInt(args[7]);
         String removalMethod = args[8];
 
         Pbm main = new Pbm(mainFilename);
         Pbm sub = new Pbm(subFilename);
 
         //-1 indicated full range or default for these arguments.
         rowCount = (rowCount == -1) ? main.getRows() : rowCount;
         colCount = (colCount == -1) ? main.getCols() : colCount;
 
         int cutoff = (int) ( (cutoffPercentage/100.0) * sub.getRows() * sub.getCols());
 
         //main.write(mainFilename+"test.pbm");
 
        int[] pos = findImage(main, sub, startRow, rowCount, startCol, colCount, cutoff);
        if("invert_logo".equals(removalMethod))
         {
            invertSubImage(main, sub, pos[0], pos[1]);
         }
        else if("blank_rectangle".equals(removalMethod))
         {
            blankRectangle(main, pos[0], pos[1], sub.getRows(), sub.getCols());
         }
         main.write(outFilename);
     }
 }
 
