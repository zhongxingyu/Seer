 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package slotgame;
 
 import java.util.AbstractList;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 import slotgame.config.SlotConfig;
 import slotgame.helper.SlotPrinter;
 
 /**
  *
  * @author vitalispiridonov
  */
 public class SlotMachine {
     
     private SlotConfig master;
     
     private List<Line> lines;
     private List<Payline> winningLines;
     private int win;
     
     private Random rnd;
     
     private int paylinesCount;
     private int betForLine = 1;
 
     public SlotMachine(SlotConfig master, int paylinesCount) {
         this.master = master;
         this.paylinesCount = paylinesCount;
         
         lines = new ArrayList<Line>();
         rnd = new Random(System.nanoTime());
     }
     
    public SlotConfig getMasterConfig() {
    	return master;
    }
    
     public void spin() {
         reset();
         
         for (int lineNumber = 0; lineNumber < master.countLines(); lineNumber++) {
             Line line = new Line();
             for (int reelNumber = 0; reelNumber < master.countReels(); reelNumber++) {
                 Reel reel = master.getReel(reelNumber);
                 
                 spinReal(reel, line);
             }
             
             lines.add(line);
         }
         
         findPaylines();
     }
     
    public List<Reel> getReels() {
    	List<Reel> reels= new ArrayList<Reel>();
    	for (int reelNumber = 0; reelNumber < master.countReels(); reelNumber++) {
    		reels.add(master.getReel(reelNumber));
    	}
    	
    	return reels;
    }
    
     private void reset() {
         lines = new ArrayList<Line>();
         
         for (int reelNumber = 0; reelNumber < master.countReels(); reelNumber++) {
             Reel reel = master.getReel(reelNumber);
             reel.reset();
         }
     }
     
     private void spinReal(Reel reel, Line line) {
         int attampCount = 0;
         while (true) {
             if (attampCount >= 100) {
                 break;
             }
             
             reel.shuffle();
             for (Symbol symbol: reel.getSymbols()) {
                 double r = rnd.nextDouble();
                 
                 if (!reel.isDropped(symbol) && r < symbol.getChance()) {
                     reel.drop(symbol);
                     
                     line.addSymbol(symbol);
                     
                     return;
                 }
                 
             }
             
             attampCount++;
         }
     }
     
     public List<Payline> findPaylines() {
         win = 0;
         winningLines = new ArrayList<Payline>();
         
         List<Payline> paylines = new ArrayList<Payline>();
         
         for (int lineIndex = 1; lineIndex <= paylinesCount; lineIndex++) {
             Payline pl = new Payline();
             pl.addSymbols(paylineSymbols(lineIndex));
             
             
             LineAward lineAward = master.getPaytable().winForLine(pl);
             pl.setLineAward(lineAward);
             
             int winForLine = lineAward.getAward();
             if (winForLine > 0) {
                 win += winForLine;
                 winningLines.add(pl);
                 
             }
             
             paylines.add(pl);
         }
 
         return paylines;
     }
     
     public int totalBet() {
         return betForLine * paylinesCount;
     }
     
     private List<Symbol> paylineSymbols(int lineIndex) {
         List<Symbol> symbols = new ArrayList<Symbol>();
         if (lineIndex == 1) {
             symbols.addAll(lines.get(Line.LINE_MIDDLE).getSymbols());
         }
         
         if (lineIndex == 2) {
             symbols.addAll(lines.get(Line.LINE_TOP).getSymbols());
         }
         
         if (lineIndex == 3) {
             symbols.addAll(lines.get(Line.LINE_BOTTOM).getSymbols());
         }
         
         
         //int[] l = {};
         if (lineIndex == 4) {
             /*
              * x 0 0 0 x
              * 0 x 0 x 0
              * 0 0 x 0 0
              */
             
             //= {0,1,2,1,0};
                    
             symbols.add(getSymbol(Line.LINE_TOP, 0));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 1));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 2));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 3));
             symbols.add(getSymbol(Line.LINE_TOP, 4));
         }
         
         if (lineIndex == 5) {
             
             /*
              * 0 0 x 0 0
              * 0 x 0 x 0
              * x 0 0 0 x
              */
             
             symbols.add(getSymbol(Line.LINE_BOTTOM, 0));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 1));
             symbols.add(getSymbol(Line.LINE_TOP, 2));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 3));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 4));
         }
         
         if (lineIndex == 6) {
             
             /*
              * 0 x x x 0
              * x 0 0 0 x
              * 0 0 0 0 0
              */
             
             symbols.add(getSymbol(Line.LINE_MIDDLE, 0));
             symbols.add(getSymbol(Line.LINE_TOP, 1));
             symbols.add(getSymbol(Line.LINE_TOP, 2));
             symbols.add(getSymbol(Line.LINE_TOP, 3));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 4));
         }
         
         if (lineIndex == 7) {
             
             /*
              * 0 0 0 0 0
              * x 0 0 0 x
              * 0 x x x 0
              */
             
             symbols.add(getSymbol(Line.LINE_MIDDLE, 0));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 1));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 2));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 3));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 4));
         }
         
         if (lineIndex == 8) {
             
             /*
              * x x 0 0 0
              * 0 0 x 0 0
              * 0 0 0 x x
              */
             
             symbols.add(getSymbol(Line.LINE_TOP, 0));
             symbols.add(getSymbol(Line.LINE_TOP, 1));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 2));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 3));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 4));
         }
         
         if (lineIndex == 9) {
             
             /*
              * - - - x x
              * - - x - -
              * x x - - -
              */
             
             symbols.add(getSymbol(Line.LINE_BOTTOM, 0));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 1));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 2));
             symbols.add(getSymbol(Line.LINE_TOP, 3));
             symbols.add(getSymbol(Line.LINE_TOP, 4));
         }
         
         if (lineIndex == 10) {
             
             /*
              * - x - x -
              * x - x - x
              * - - - - -
              */
             
             symbols.add(getSymbol(Line.LINE_MIDDLE, 0));
             symbols.add(getSymbol(Line.LINE_TOP, 1));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 2));
             symbols.add(getSymbol(Line.LINE_TOP, 3));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 4));
         }
         
         if (lineIndex == 11) {
             
             /*
              * - - - - -
              * x - x - x
              * - x - x - 
              */
             
             symbols.add(getSymbol(Line.LINE_MIDDLE, 0));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 1));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 2));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 3));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 4));
         }
         
         if (lineIndex == 12) {
             
             /*
              * x - - - x
              * - x x x -
              * - - - - - 
              */
             
             symbols.add(getSymbol(Line.LINE_TOP, 0));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 1));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 2));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 3));
             symbols.add(getSymbol(Line.LINE_TOP, 4));
         }
         
         if (lineIndex == 13) {
             
             /*
              * - - - - -
              * - x x x -
              * x - - - x 
              */
             
             symbols.add(getSymbol(Line.LINE_BOTTOM, 0));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 1));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 2));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 3));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 4));
         }
         
         if (lineIndex == 14) {
             
             /*
              * x - x - x
              * - x - x -
              * - - - - - 
              */
             
             symbols.add(getSymbol(Line.LINE_TOP, 0));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 1));
             symbols.add(getSymbol(Line.LINE_TOP, 2));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 3));
             symbols.add(getSymbol(Line.LINE_TOP, 4));
         }
         
         if (lineIndex == 15) {
             
             /*
              * - - - - -
              * - x - x -
              * x - x - x 
              */
             
             symbols.add(getSymbol(Line.LINE_BOTTOM, 0));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 1));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 2));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 3));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 4));
         }
         
         if (lineIndex == 16) {
             
             /*
              * - - x - -
              * x x - x x
              * - - - - - 
              */
             
             symbols.add(getSymbol(Line.LINE_MIDDLE, 0));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 1));
             symbols.add(getSymbol(Line.LINE_TOP, 2));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 3));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 4));
         }
         
         if (lineIndex == 17) {
             
             /*
              * - - - - -
              * x x - x x
              * - - x - - 
              */
             
             symbols.add(getSymbol(Line.LINE_MIDDLE, 0));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 1));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 2));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 3));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 4));
         }
         
         if (lineIndex == 18) {
             
             /*
              * x x - x x
              * - - - - -
              * - - x - - 
              */
             
             symbols.add(getSymbol(Line.LINE_TOP, 0));
             symbols.add(getSymbol(Line.LINE_TOP, 1));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 2));
             symbols.add(getSymbol(Line.LINE_TOP, 3));
             symbols.add(getSymbol(Line.LINE_TOP, 4));
         }
         
         if (lineIndex == 19) {
             
             /*
              * - - x - - 
              * - - - - -
              * x x - x x
              */
             
             symbols.add(getSymbol(Line.LINE_BOTTOM, 0));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 1));
             symbols.add(getSymbol(Line.LINE_TOP, 2));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 3));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 4));
         }
         
         if (lineIndex == 20) {
             
             /*
              * x - - - x
              * - - - - -
              * - x x x -
              */
             
             symbols.add(getSymbol(Line.LINE_TOP, 0));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 1));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 2));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 3));
             symbols.add(getSymbol(Line.LINE_TOP, 4));
         }
         
         if (lineIndex == 21) {
             
             /*
              * - x x x -
              * - - - - -
              * x - - - x
              */
             
             symbols.add(getSymbol(Line.LINE_BOTTOM, 0));
             symbols.add(getSymbol(Line.LINE_TOP, 1));
             symbols.add(getSymbol(Line.LINE_TOP, 2));
             symbols.add(getSymbol(Line.LINE_TOP, 3));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 4));
         }
         
         if (lineIndex == 22) {
             
             /*
              * - x - x -
              * x - - - x
              * - - x - -
              */
             
             symbols.add(getSymbol(Line.LINE_MIDDLE, 0));
             symbols.add(getSymbol(Line.LINE_TOP, 1));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 2));
             symbols.add(getSymbol(Line.LINE_TOP, 3));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 4));
         }
         
         if (lineIndex == 23) {
             
             /*
              * - - x - -
              * x - - - x
              * - x - x -
              */
             
             symbols.add(getSymbol(Line.LINE_MIDDLE, 0));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 1));
             symbols.add(getSymbol(Line.LINE_TOP, 2));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 3));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 4));
         }
         
         if (lineIndex == 24) {
             
             /*
              * x - x - x
              * - - - - -
              * - x - x -
              */
             
             symbols.add(getSymbol(Line.LINE_TOP, 0));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 1));
             symbols.add(getSymbol(Line.LINE_TOP, 2));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 3));
             symbols.add(getSymbol(Line.LINE_TOP, 4));
         }
         
         
         if (lineIndex == 25) {
             
             /*
              * - x - x -
              * - - - - -
              * x - x - x
              */
             
             symbols.add(getSymbol(Line.LINE_BOTTOM, 0));
             symbols.add(getSymbol(Line.LINE_TOP, 1));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 2));
             symbols.add(getSymbol(Line.LINE_TOP, 3));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 4));
         }
         
         if (lineIndex == 26) {
             
             /*
              * x - - x -
              * - - x - -
              * - x - - x
              */
             
             symbols.add(getSymbol(Line.LINE_BOTTOM, 0));
             symbols.add(getSymbol(Line.LINE_TOP, 1));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 2));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 3));
             symbols.add(getSymbol(Line.LINE_TOP, 4));
         }
         
         if (lineIndex == 27) {
             
             /*
              * - x - - x
              * - - x - -
              * x - - x -
              */
             
             symbols.add(getSymbol(Line.LINE_BOTTOM, 0));
             symbols.add(getSymbol(Line.LINE_TOP, 1));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 2));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 3));
             symbols.add(getSymbol(Line.LINE_TOP, 4));
         }
         
         if (lineIndex == 28) {
             
             /*
              * x - - - -
              * - x - - x
              * - - x x -
              */
             
             symbols.add(getSymbol(Line.LINE_TOP, 0));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 1));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 2));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 3));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 4));
         }
         
         if (lineIndex == 29) {
             
             /*
              * - - x x -
              * - x - - x
              * x - - - -
              */
             
             symbols.add(getSymbol(Line.LINE_BOTTOM, 0));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 1));
             symbols.add(getSymbol(Line.LINE_TOP, 2));
             symbols.add(getSymbol(Line.LINE_TOP, 3));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 4));
         }
         
         if (lineIndex == 30) {
             
             /*
              * - - - x -
              * - x - - x
              * x - x - -
              */
             
             symbols.add(getSymbol(Line.LINE_BOTTOM, 0));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 1));
             symbols.add(getSymbol(Line.LINE_BOTTOM, 2));
             symbols.add(getSymbol(Line.LINE_TOP, 3));
             symbols.add(getSymbol(Line.LINE_MIDDLE, 4));
         }
         
         
         
         //for (int lineNo = 0; lineNo < 5; lineNo++) {
         //    symbols.add(getSymbol(l[lineNo], lineNo));
         //}
         
         return symbols;
     }
     
     private Symbol getSymbol(int lineNo, int symbolNo) {
         return lines.get(lineNo).getSymbols().get(symbolNo);
     }
 
     @Override
     public String toString() {
         SlotPrinter printer = new SlotPrinter(this);
         
         return printer.toString();
     }
 
     public List<Line> getLines() {
         return lines;
     }
 
     public int getBetForLine() {
         return betForLine;
     }
 
     public void setBetForLine(int betForLine) {
         this.betForLine = betForLine;
     }
 
     public List<Payline> getWinningLines() {
         return winningLines;
     }
 
     public void setWinningLines(List<Payline> winningLines) {
         this.winningLines = winningLines;
     }
 
     public int getWin() {
         return win;
     }
 
     public void setWin(int win) {
         this.win = win;
     }
     
 }
