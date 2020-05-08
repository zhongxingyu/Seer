 /*
  * Copyright (C) Bernhard Seybold. All rights reserved.
  *
  * This software is published under the terms of the LGPL Software License,
  * a copy of which has been included with this distribution in the LICENSE.txt
  * file.
  *
  * THIS SOFTWARE IS PROVIDED ``AS IS'' AND ANY EXPRESSED OR IMPLIED
  * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
  * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
  *
  * $Id: HTMLGameBrowser.java,v 1.3 2003/01/04 16:23:32 BerniMan Exp $
  */
 
 package chesspresso.game.view;
 
 import java.io.*;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.lang3.StringUtils;
 
 import freemarker.template.Configuration;
 import freemarker.template.Template;
 import freemarker.template.TemplateExceptionHandler;
 import freemarker.template.Version;
 
 import chesspresso.*;
 import chesspresso.game.*;
 import chesspresso.move.*;
 import chesspresso.position.*;
 
 /**
  * Producer for HTML pages displaying a game.
  *
  * @author Bernhard Seybold
  * @author Jeff Tsay
  * @version $Revision: 1.3 $
  */
 public class HTMLGameBrowser implements GameListener {
 
   //======================================================================
   // GameListener Methods
 
   @Override
   public void notifyLineStart(int level) {
     m_isLineStart = true;
     m_showMoveNumber = true;
     m_lasts[level + 1] = m_lasts[level];
   }
 
   @Override
   public void notifyLineEnd(int level) {
     m_isLineEnd = true;
     m_showMoveNumber = true;
   }
 
   @Override
   public void notifyMove(Move move, short[] nags, String comment, int plyNumber, int level) {
     ImmutablePosition pos = m_game.getPosition();
 
     final PlyModel plyModel = new PlyModel();
     plyModel.plyNumber = plyNumber;
     plyModel.lineStart = m_isLineStart;
     plyModel.lineEnd = m_isLineEnd;
 
     m_isLineStart = m_isLineEnd = false;
 
     plyModel.showMoveNumber = m_showMoveNumber;
     plyModel.moveNumber = m_moveNumber;
 
     m_showMoveNumber = Chess.isWhitePly(plyNumber + 1);
 
     plyModel.level = level;
     plyModel.move = move;
 
     if (nags != null) {
       for (int i = 0; i < nags.length; i++) {
         plyModel.nags.add(NAG.getShortString(nags[i]));
       }
       m_showMoveNumber = true;
     }
 
     plyModel.comment = comment;
 
     m_plyModels.add(plyModel);
 
     addPosData(pos);
     m_lastData.add(m_lasts[level]);
     m_lasts[level] = m_moveNumber;
 
     m_moveNumber++;
   }
 
 
   //======================================================================
 
   /**
    * Create a new HTMLGameBrowser with default settings.
    */
   public HTMLGameBrowser() {
     this("");
   }
 
 
   public HTMLGameBrowser(final String imagePrefix) {
     m_wimgs = new String[]{
      "wkw.gif", "wpw.gif", "wqw.gif", "wrw.gif", "wbw.gif", "wnw.gif", "now.gif",
      "bnw.gif", "bbw.gif", "brw.gif", "bqw.gif", "bpw.gif", "bkw.gif"
     };
     m_bimgs = new String[]{
      "wkb.gif", "wpb.gif", "wqb.gif", "wrb.gif", "wbb.gif", "wnb.gif", "nob.gif",
      "bnb.gif", "bbb.gif", "brb.gif", "bqb.gif", "bpb.gif", "bkb.gif"
     };
     m_imagePrefix = StringUtils.trimToEmpty(imagePrefix);
     m_styleFilename = null;
   }
 
   //======================================================================
 
   /**
    * Set the name of the style file. If name is set to null, inline style
    * definition will be used. Default is inline style.<br>
    * When using an external style file, the following styles are expected:
    * <ul>
    * <li>a.main: the anchor used for moves in the main line
    * <li>a.line: the anchor used for moves in side-lines
    * <li>span.comment: used for move comments
    * <li>table.content: the content table containing the board left and the moves on the right
    * </ul>
    *
    * @param styleFilename the name of the style file
    */
   private void setStyleFilename(String styleFilename) {
     m_styleFilename = styleFilename;
   }
 
   /**
    * Sets the name of an square image. The default names are set according to
    * the following scheme: First letter is the color of the stone (b, w), second
    * letter the piece (k, q, r, b, n, p) third letter the square color (b, w),
    * extension is gif. now.gif and nob.gif are used for empty squares.<br>
    * For instance: wkw.gif determines a white king on a white square,
    * bbb.gif is a black bishop on a black square.
    *
    * @param stone       the stone displayed
    * @param whiteSquare whether or not the square is white
    * @param name        the name of the corresponding image
    */
   private void setStoneImageName(int stone, boolean whiteSquare, String name) {
     if (whiteSquare) {
       m_wimgs[stone - Chess.MIN_STONE] = name;
     } else {
       m_bimgs[stone - Chess.MIN_STONE] = name;
     }
   }
 
   //======================================================================
 
   /**
    * Produces HTML to display a game.
    *
    * @param outStream where the HTML will be sent to
    * @param game      the game to display.
    */
   public void produceHTML(OutputStream outStream, Game game)
    throws Exception {
     produceHTML(outStream, game, false, false);
   }
 
   /**
    * Produces HTML to display a game.
    *
    * @param outStream   where the HTML will be sent to
    * @param game        the game to display.
    * @param contentOnly if true skip header and footer information, use this if you want to
    *                    produce your own header and footer
    */
   public synchronized void produceHTML(final OutputStream outStream,
                                        final Game game, final boolean contentOnly,
                                        final boolean debugMode)
    throws Exception {
     m_plyModels = new LinkedList<>();
     m_posData = new LinkedList<>();
 
     m_lastData = new LinkedList<>();
     m_game = game;
     m_moveNumber = 0;
     m_showMoveNumber = true;
     m_lasts = new int[100];
     m_lasts[0] = 0;
 
     m_lastData.add(0);
 
     m_game.gotoStart();
     addPosData(m_game.getPosition());
     m_moveNumber++;
 
     game.traverse(this, true);
 
     String styleHtml = null;
     List<String> imagePaths = null;
 
     if (!contentOnly) {
       if (m_styleFilename == null) {
         styleHtml = "<style type=\"text/css\">\n" +
          "   .chesspresso_main { text-decoration:none }\n" +
          "   .chesspresso_line { text-decoration:none }\n" +
          "  a.chesspresso_main { font-weight:bold; color:black; }\n" +
          "  a.chesspresso_line { color:black }\n" +
          "  span.chesspresso_comment {font-style:italic}\n" +
          "  .chesspresso_deselected_ply_link { background: white !important; color: black !important; }\n" +
          "  .chesspresso_selected_ply_link { background: black !important; color: white !important; }\n" +
          "</style>";
       } else {
         styleHtml = "<link rel=\"stylesheet\" href=\"" + m_styleFilename + "\" type=\"text/css\" />";
       }
 
       imagePaths = new LinkedList<String>();
       for (int stone = Chess.MIN_STONE; stone <= Chess.MAX_STONE; stone++) {
         imagePaths.add(getImageForStone(stone, true));
       }
 
       for (int stone = Chess.MIN_STONE; stone <= Chess.MAX_STONE; stone++) {
         imagePaths.add(getImageForStone(stone, false));
       }
     }
 
     final Position startPos = Position.createInitialPosition();
     final List<List<String>> imagePathsPerRow = new LinkedList<>();
     for (int row = Chess.NUM_OF_ROWS - 1; row >= 0; row--) {
       final List<String> imagePathsForRow = new LinkedList<>();
       for (int col = 0; col < Chess.NUM_OF_COLS; col++) {
         int sqi = Chess.coorToSqi(col, row);
         imagePathsForRow.add(getImageForStone(startPos.getStone(sqi), Chess.isWhiteSquare(sqi)));
       }
       imagePathsPerRow.add(imagePathsForRow);
     }
 
     final Map<String, Object> root = new HashMap<>();
 
     root.put("game", game);
     root.put("plys", m_plyModels);
     root.put("lastMoveNumber", m_moveNumber - 1);
     root.put("posData", m_posData);
     root.put("lastData", m_lastData);
     root.put("contentOnly", contentOnly);
     root.put("styleHtml", styleHtml);
     root.put("imagePaths", imagePaths);
     root.put("imagePathsPerRow", imagePathsPerRow);
 
     final Configuration config = makeConfiguration(debugMode);
     final Template template = config.getTemplate("game.ftl");
     template.process(root, new OutputStreamWriter(outStream));
   }
 
   private Configuration makeConfiguration(boolean debugMode) {
     final Configuration config = new Configuration();
 
     config.setIncompatibleImprovements(new Version(2, 3, 20));
     config.setClassForTemplateLoading(HTMLGameBrowser.class, "freemarker");
     config.setDefaultEncoding("UTF-8");
 
     if (debugMode) {
       config.setTemplateExceptionHandler(TemplateExceptionHandler.HTML_DEBUG_HANDLER);
     } else {
       config.setTemplateExceptionHandler(TemplateExceptionHandler.RETHROW_HANDLER);
     }
 
     return config;
   }
 
   public static void main(String[] args) {
     if (args.length < 1) {
       args = new String[]{ "fischer.pgn" };
 
       /*
       System.out.println("Usage: java " + HTMLGameBrowser.class.getName() +
        " <PGN filename>");
       System.exit(0); */
     }
 
     try {
       chesspresso.pgn.PGNReader pgn = new chesspresso.pgn.PGNReader(
        new FileReader(args[0]), "game");
       final Game game = pgn.parseGame();
 
       final HTMLGameBrowser html = new HTMLGameBrowser();
       html.produceHTML(System.out, game);
     } catch (Exception ex) {
       ex.printStackTrace();
     }
   }
 
   /**
    * Needs to be public for Freemarker.
    */
   public static class PlyModel {
     boolean lineStart = false;
     boolean lineEnd = false;
     int plyNumber = 0;
     boolean showMoveNumber = true;
     int moveNumber = 0;
     int level = 0;
     Move move;
     List<String> nags = new LinkedList<>();
     String comment;
 
     public boolean getLineStart() {
       return lineStart;
     }
 
     public void setLineStart(boolean lineStart) {
       this.lineStart = lineStart;
     }
 
     public boolean getLineEnd() {
       return lineEnd;
     }
 
     public void setLineEnd(boolean lineEnd) {
       this.lineEnd = lineEnd;
     }
 
     public int getPlyNumber() {
       return plyNumber;
     }
 
     public void setPlyNumber(int plyNumber) {
       this.plyNumber = plyNumber;
     }
 
 
     public boolean isShowMoveNumber() {
       return showMoveNumber;
     }
 
     public void setShowMoveNumber(boolean showMoveNumber) {
       this.showMoveNumber = showMoveNumber;
     }
 
     public int getMoveNumber() {
       return moveNumber;
     }
 
     public void setMoveNumber(int moveNumber) {
       this.moveNumber = moveNumber;
     }
 
     public int getLevel() {
       return level;
     }
 
     public void setLevel(int level) {
       this.level = level;
     }
 
     public Move getMove() {
       return move;
     }
 
     public void setMove(Move move) {
       this.move = move;
     }
 
     public List<String> getNags() {
       return nags;
     }
 
     public void setNags(List<String> nags) {
       this.nags = nags;
     }
 
     public String getComment() {
       return comment;
     }
 
     public void setComment(String comment) {
       this.comment = comment;
     }
 
     @Override
     public String toString() {
       return "PlyModel{" +
        "lineStart=" + lineStart +
        ", lineEnd=" + lineEnd +
        ", plyNumber=" + plyNumber +
        ", showMoveNumber=" + showMoveNumber +
        ", moveNumber=" + moveNumber +
        ", level=" + level +
        ", move=" + move +
        ", nags=" + nags +
        ", comment='" + comment + '\'' +
        '}';
     }
   }
 
   /**
    * Returns the name of the image.
    *
    * @param stone       the stone displayed
    * @param whiteSquare whether or not the square is white
    */
   private String getImageForStone(int stone, boolean isWhite) {
     return m_imagePrefix + (isWhite ? m_wimgs[stone - Chess.MIN_STONE] : m_bimgs[stone - Chess.MIN_STONE]);
   }
 
   private void addPosData(ImmutablePosition pos) {
     final int[] data = new int[Chess.NUM_OF_ROWS * Chess.NUM_OF_COLS];
     int j = 0;
     for (int row = Chess.NUM_OF_ROWS - 1; row >= 0; row--) {
       for (int col = 0; col < Chess.NUM_OF_COLS; col++) {
         int sqi = Chess.coorToSqi(col, row);
         data[j++] = pos.getStone(sqi) - Chess.MIN_STONE;
       }
     }
 
     m_posData.add(data);
   }
 
   private List<PlyModel> m_plyModels;
   private boolean m_isLineStart = false;
   private boolean m_isLineEnd = false;
   private List<int[]> m_posData;
   private List<Integer> m_lastData;
   private Game m_game;
   private int m_moveNumber;
   private boolean m_showMoveNumber;
   private int[] m_lasts;
   private String[] m_wimgs;
   private String[] m_bimgs;
   private final String m_imagePrefix;
   private String m_styleFilename;
 }
