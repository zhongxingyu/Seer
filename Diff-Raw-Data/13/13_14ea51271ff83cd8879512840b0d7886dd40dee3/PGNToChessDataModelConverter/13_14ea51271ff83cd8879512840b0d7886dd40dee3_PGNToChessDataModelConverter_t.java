 /**
  * PGNToChessDataModelConverter.java
  */
 
 package de.uni_leipzig.informatik.swp13_sc.converter;
 
 import java.io.BufferedReader;
 import java.io.DataInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.nio.charset.Charset;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipOutputStream;
 
 import de.uni_leipzig.informatik.swp13_sc.converter.ChessDataModelToRDFConverter.OutputFormats;
 import de.uni_leipzig.informatik.swp13_sc.datamodel.ChessGame;
 import de.uni_leipzig.informatik.swp13_sc.datamodel.ChessMove;
 import de.uni_leipzig.informatik.swp13_sc.datamodel.ChessPlayer;
 import de.uni_leipzig.informatik.swp13_sc.datamodel.pgn.ChessPGNVocabulary;
 
 /**
  * Converter class.<br />
  * Converts PGN Files (text input streams) into the internal chess data format
  * and then into RDF.<br />
  * 
  *
  * @author Erik
  * @see PGNToRDFConverterAllAtOnce
  */
 public class PGNToChessDataModelConverter
 {
     
     /**
      * The inputFilename.
      */
     private String inputFilename;
     /**
      * The outputFilename.
      */
     private String outputFilename;
     /**
      * isConverting
      */
     private volatile boolean isConverting;
     /**
      * isParsing
      */
     private volatile boolean isParsing;
     /**
      * finishedConverting
      */
     private volatile boolean finishedConverting;
     /**
      * finishedParsing
      */
     private volatile boolean finishedParsing;
     /**
      * Converter used to convert ChessDataModel to RDF.
      */
     private ChessDataModelToRDFConverter chessToRDF;
     /**
      * List of all converted chess games. Internal chess data format.
      */
     private List<ChessGame> games;
     
     // ------------------------------------------------------------------------
     
     /**
      * Internal. Empty String. Not null.
      */
     private final static String EMPTY = "";
     
     // regex strings
     /**
      * Regex for the figure types (without pawn).
      */
     private final static String regex_move_figure = "[RNBQK]";
     /**
      * Regex for the row field part notation.
      */
     private final static String regex_move_source_row = "[1-8]";
     /**
      * Regex for the column field notation.
      */
     private final static String regex_move_source_column = "[a-h]";
     /**
      * Regex for partial source field notation.
      */
     private final static String regex_move_source =
             regex_move_source_column + "?" + regex_move_source_row + "?";
     /**
      * Regex for a capture move.
      */
     private final static String regex_move_captured = "x";
     /**
      * Regex for the destination field in a move.
      */
     private final static String regex_move_destination =
             regex_move_source_column + regex_move_source_row;
     
     /**
      * Regex for promotion. e. g. a pawn was transformed into a queen -> "=Q"
      */
     private final static String regex_move_promotion = "=" + regex_move_figure;
     
     /**
      * Regex for check modifier.
      */
     private final static String regex_move_check = "\\+";
     /**
      * Regex for checkmate.
      */
     private final static String regex_move_checkmate = "#"; // ?
     /**
      * Regex for check or checkmate.
      */
     private final static String regex_move_checkCheckmate =
             "(" + regex_move_check + "|" + regex_move_checkmate + ")?";
     
     /**
      * Regex for the round number.
      */
     private final static String regex_move_number = "\\b" + "[1-9][0-9]*\\."; // checked!
     /**
      * Regex for a comment between moves.
      */
     private final static String regex_move_comment = "(" + "\\{[^\\}]*\\}" + ")?"; // ungreedy
     
     /**
      * Regex for queenside castling move. Not used.
      */
     private final static String regex_move_castling_queen = "O\\-O\\-O";
     /**
      * Regex for queenside castling move. Not used.
      */
     private final static String regex_move_castling_king = "O\\-O";
     //(O\\-O(\\-O)?)   <->   (O\\-O\\-O)|(O\\-O)   ???
     //O\-O(\-O)?
     /**
      * Regex for a castling move.
      */
     private final static String regex_move_castling =
             //regex_move_castling_queen + "|" + regex_move_castling_king;
             // checked!? -> needs greedy
             "O\\-O(\\-O)?";
     
     //([a-h]x)?[a-h][1-8](=[RNBQK])?
     /**
      * Regex for a pawn move.
      */
     private final static String regex_move_pawn =
             "(" + regex_move_source_column + regex_move_captured + ")?" + 
                     regex_move_destination +
                     "(" + regex_move_promotion + ")?"; // checked!
     //[RNBQK][a-h]?[1-8]?x?[a-h][1-8]
     /**
      * Regex for a move other than pawn.
      */
     private final static String regex_move_otherfigure =
             regex_move_figure + regex_move_source + regex_move_captured
                     + "?" + regex_move_destination;
     
     // castling -> check ? maybe possible ...
     //\b(((([a-h]x)?[a-h][1-8](=[RNBQK])?)|([RNBQK][a-h]?[1-8]?x?[a-h][1-8])|(O\-O(\-O)?))\b(\+|#)?)\s*(\{.*?\})?
     /**
      * Regex for a single move notation. Most important and mostly used.
      */
     private final static String regex_move_single =
             "\\b" + "(((" + regex_move_pawn + ")|(" + regex_move_otherfigure +
                     ")|(" + regex_move_castling + "))" + "\\b" + regex_move_checkCheckmate +
                     ")" + "\\s*" + regex_move_comment + "\\s*"; // checked !!
     
     //(\b[1-9][0-9]*\.\s*)\b((([a-h]x)?[a-h][1-8](=[RNBQK])?)|([RNBQK][a-h]?[1-8]?x?[a-h][1-8])|(O\-O(\-O)?))\b(\+|#)?\s*(\{.*?\})?(\b((([a-h]x)?[a-h][1-8](=[RNBQK])?)|([RNBQK][a-h]?[1-8]?x?[a-h][1-8])|(O\-O(\-O)?))\b(\+|#)?\s*(\{.*?\})?)?
     /**
      * Regex for a round (round number, first move and if present the second move).
      */
     private final static String regex_move_double =
             "(" + regex_move_number + "\\s*" + ")" + regex_move_single + "(" +
                     regex_move_single + ")?"; // checked !!
     
     //\b(1\-0)|(0\-1)|(1\/2\-1\/2)\b
     /**
      * Regex for pgn move part - the result at the end.
      */
     private final static String regex_result =
             "\\b" + "(1\\-0)|(0\\-1)|(1\\/2\\-1\\/2)" + "\\b"; // checked!
     
     
     /**
      * Regex for meta key name.
      */
     private final static String regex_meta_key = "([A-Z]\\w*)";
     /**
      * Regex for meta key value part.
      */
     private final static String regex_meta_value = "\\\"([^\\\"]*)\\\"";
     /**
      * Regex for separator between key and value.
      */
     private final static String regex_meta_separator = "\\s+";
     /**
      * Regex for meta data entry start char.
      */
     private final static String regex_meta_start = "\\[";
     /**
      * Regex for meta data entry end.
      */
     private final static String regex_meta_end = "\\]";
     /**
      * Regex for meta data entry element.
      */
     private final static String regex_meta =
             regex_meta_start + regex_meta_key + regex_meta_separator +
                     regex_meta_value + regex_meta_end;
     // needs multi-line modifier if ^PATTERN$
     // check for multi-line modifier ... below
     /**
      * Regex for meta data entry start. Used to check if there is one.
      */
     private final static String regex_meta_start_line =
             "^" + regex_meta_start;
     
     
     // ------------------------------------------------------------------------
     
     /**
      * Regex Pattern for a single chess move in algebraic notation.
      */
     private static Pattern pattern_single_move = Pattern.compile(regex_move_single);
     // Group 0: complete (pos, check, comment)
     // Group 1: move without comment
     // Group 2: move only (source, dest. pos, NO check!)
     // Group 3: pawn moves only (source, capture, dest., promo, figure)
     // Group 4: pawn move (capture + source column)
     // Group 5: promotion (promo, dest. figure)
     // Group 6: move of other figures (figure, source, capture, dest.)
     // Group 7: castlings (king, queen)
     // Group 8: queen side castlings (queen part ? -O)
     // Group 9: check (check, checkmate (?))
     // Group 10: comments (-> {...})
     
     ///**
     // * Regex Pattern for a complete round (-> 2 moves with number).
     // */
     //private static Pattern pattern_double_move = Pattern.compile(regex_move_double);
     
     /**
      * Regex Pattern for the start of a meta data entry line.
      */
     private static Pattern pattern_meta_start_line =
             Pattern.compile(regex_meta_start_line);//, Pattern.MULTILINE);
     
     /**
      * Regex Pattern for a single meta data entry line.
      */
     private static Pattern pattern_meta = Pattern.compile(regex_meta);
     // Group 0: complete
     // Group 1: meta key
     // Group 2: meta value
     
     // ------------------------------------------------------------------------
     
     /**
      * Creates a new Converter. Nothing more.
      * 
      * @param   inputFilename   Input filename used later while parsing.
      * @param   outputFilename  Output filename used later while writing RDF to a file.
      */
     public PGNToChessDataModelConverter(String inputFilename, String outputFilename)
     {
         // no validation!
         this.inputFilename = inputFilename;
         this.outputFilename = outputFilename;
         
         this.isConverting = false;
         this.isParsing = false;
         this.finishedConverting = false;
         this.finishedParsing = false;
         
         this.games = null;
     }
     
     /**
      * Parses the previously given input file to the internal chess data format.
      * 
      * @return  true if successful, false for error
      */
     public boolean parse()
     {
         this.isParsing = true;
         this.games = new ArrayList<ChessGame>();        
         BufferedReader reader = openReader(this.inputFilename);
         if (reader == null)
         {
             return false;
         }
         
         String line = null;
         int nr = 0;
         boolean inMoves = false;
         boolean outside = true;
         ChessGame.Builder cgb = new ChessGame.Builder();
         
         // start parse
         while (true)
         {
             try
             {
                 line = reader.readLine();
             }
             catch (IOException e)
             {
                 // log
                 // break;
                 return false;
             }
             if (line == null)
             {
                 break;
             }
             
             if (EMPTY.equals(line))
             {
                 // -- separator lines --
                 if (outside)
                 {
                     continue;
                 }
                 else
                 {
                     outside = true;
                     /*inMeta = false;*/
                     
                     // finish game and add to list
                     if (inMoves)
                     {
                         this.games.add(cgb.build());
                         cgb = new ChessGame.Builder();
                     }
                     inMoves = false;
                     nr = 0;
                 }
             }
             //else if (line.charAt(0) == '[')
             else if (pattern_meta_start_line.matcher(line).lookingAt())
             {
                 // -- meta data --
                 Matcher meta = pattern_meta.matcher(line);
                 // get values
                 if (! meta.find())
                 {
                     // log line
                     continue;
                 }
                 try
                 {
                     String key = meta.group(1);
                     String value = meta.group(2);
                     
                     // switch(key) only with JRE 1.7 !
                     if (ChessPGNVocabulary.Meta_Key_White.equals(key))
                     {
                         cgb.setWhitePlayer(new ChessPlayer.Builder().setName(value).build());
                     }
                     else if (ChessPGNVocabulary.Meta_Key_Black.equals(key))
                     {
                         cgb.setBlackPlayer(new ChessPlayer.Builder().setName(value).build());
                     }
                     else if (ChessPGNVocabulary.Meta_Key_Date.equals(key))
                     {
                         cgb.setDate(value);
                     }
                     else if (ChessPGNVocabulary.Meta_Key_Round.equals(key))
                     {
                         cgb.setRound(value);
                     }
                     else if (ChessPGNVocabulary.Meta_Key_Result.equals(key))
                     {
                         cgb.setResult(value);
                     }
                     else if (ChessPGNVocabulary.Meta_Key_Site.equals(key))
                     {
                         cgb.setSite(value);
                     }
                     else if (ChessPGNVocabulary.Meta_Key_Event.equals(key))
                     {
                         cgb.setEvent(value);
                     }
                     else
                     {
                         cgb.addMetaData(key, value);
                     }         
                 }
                 catch (IllegalStateException e)
                 {
                     e.printStackTrace();
                 }
                 catch (IndexOutOfBoundsException e)
                 {
                     e.printStackTrace();
                 }
                 
             }
             else
             {
                 if (outside)
                 {
                     outside = false;
                     inMoves = true;
                 }
                 // -- moves --
                 Matcher move = pattern_single_move.matcher(line);
                 while (move.find())
                 {
                     nr ++;
                     ChessMove.Builder cmb = new ChessMove.Builder();
                     try
                     {
                         String m = move.group(1);
                         String comment = move.group(9);
                         
                         cgb.addMove(cmb.setNr(nr).setMove(m).setComment(comment).build());
                     }
                     catch (IllegalStateException e)
                     {
                         e.printStackTrace();
                     }
                     catch (IndexOutOfBoundsException e)
                     {
                         e.printStackTrace();
                     }
                 }
             }
             
         }
         // end parse
         try
         {
             reader.close();
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
         
         this.isParsing = false;
         this.finishedParsing = true;
         return true;
     }
     
     /**
      * Converts the internal chess data format into RDF tripel/statements.
      * 
      * @return  true if successful, false if error
      */
     public boolean convert()
     {
         this.isConverting = true;
         if (this.games == null || this.games.size() == 0)
         {
             return false;
         }
         this.chessToRDF = new ChessDataModelToRDFConverter();
         
         //convert
         this.chessToRDF.convert(games);
         
         this.isConverting = false;
         this.finishedConverting = true;
         return true;
     }
     
     /**
      * Writes the RDF data to the previously given output file.
      * 
      * @return  true if successful else false
      */
     public boolean write()
     {        
         return write(this.outputFilename);
     }
     
     /**
      * Writes the RDF data to the specified outputFilename. Old file data will
      * be overwritten and therefore lost.
      * 
      * @param   outputFilename  File to write into.
      * @return  true if successful else false
      */
     public boolean write(String outputFilename)
     {
         //OutputStream os = openOutputStream(outputFilename);
         OutputStream os = openZipOutputStream(outputFilename);
         
         if (os == null)
         {
             return false;
         }        
         if (this.chessToRDF == null)
         {
             return false;
         }
         
         // flush all
         this.chessToRDF.flushToStream(os, OutputFormats.TURTLE);
         try
         {
             os.close();
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
         
         return true;
     }
     
     /**
      * Opens a new FileInputStream.
      * 
      * @param   inputFilename   File to open for reading.
      * @return  FileInputStream or null if error
      */
     protected FileInputStream openInputStream(String inputFilename)
     {
         if (inputFilename == null)
         {
             return null;
         }
         // try opening the input
         try
         {
             return new FileInputStream(inputFilename);
         }
         catch (FileNotFoundException e)
         {
             e.printStackTrace();
         }
         catch (SecurityException e)
         {
             e.printStackTrace();
         }
         return null;
     }
     
     /**
      * Opens a new FileOutputStream.
      * 
      * @param   outputFilename  File to write into. Old content will be
      *                          overwritten
      * @return  FileOutputStream or null if error
      */
     protected FileOutputStream openOutputStream(String outputFilename)
     {
         if (outputFilename == null)
         {
             return null;
         }
         // try opening output file
         try
         {
             return new FileOutputStream(outputFilename);
         }
         catch (FileNotFoundException e)
         {
             e.printStackTrace();
         }
         catch (SecurityException e)
         {
             e.printStackTrace();
         }
         return null;
     }
     
     /**
      * Opens a new ZipOutputStream for compressed output.
      * 
      * @param   outputFilename  File to write into.
      * @return  ZipOutputStream or null if error.
      */
     protected ZipOutputStream openZipOutputStream(String outputFilename)
     {
         if (outputFilename == null)
         {
             return null;
         }
         
         // zip archive file
         FileOutputStream fos = openOutputStream(outputFilename + ".zip");
         if (fos == null)
         {
             return null;
         }
         
         // add normal file to zip archive
         ZipOutputStream zos = new ZipOutputStream(fos);
         zos.setLevel(9); // highest level
         
         try
         {
            File foc = new File(outputFilename);
            File fop = foc.getParentFile();
            String entryname = outputFilename;
            if (fop != null)
            {
                entryname = fop.toURI().relativize(foc.toURI()).getPath();
            }            
            
             ZipEntry ze = new ZipEntry(entryname);
         
             zos.putNextEntry(ze);
             
             return zos;
         }
         catch (NullPointerException e)
         {
             e.printStackTrace();
         }
         catch (IllegalArgumentException e)
         {
             e.printStackTrace();
         }
         catch (IOException e)
         {
             e.printStackTrace();
         }
         
         return null;
     }
     
 
     /**
      * Opens a text file reader with the given inputFilename.
      * 
      * @param   inputFilename   File to read from.
      * @return  BufferedReader or null if error
      */
     protected BufferedReader openReader(String inputFilename)
     {
         FileInputStream fis = openInputStream(inputFilename);
         if (fis == null)
         {
             return null;
         }
         
         DataInputStream dis = new DataInputStream(fis);
         // default charset?
         InputStreamReader isr = new InputStreamReader(dis, Charset.defaultCharset());
         BufferedReader br = new BufferedReader(isr);
         
         return br;
     }
     
 }
