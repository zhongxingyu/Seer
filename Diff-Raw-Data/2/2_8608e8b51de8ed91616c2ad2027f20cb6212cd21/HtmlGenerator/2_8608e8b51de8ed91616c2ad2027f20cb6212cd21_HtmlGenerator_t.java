 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package hexadoku;
 
 import java.io.PrintStream;
 
 /**
  * Generates HTML for a board.
  *
  * @author Sam Fredrickson <kinghajj@gmail.com>
  */
 public class HtmlGenerator {
     private static String[] header = {
         "<?xml version=\"1.0\" encoding=\"UTF-8\"?>",
         "<!DOCTYPE html PUBLIC \"-//W3C//DTD XHTML 1.0 Strict//EN\" \"http://www.w3.org/TR/xhtml1/DTD/xhtml1-strict.dtd\">",
         "<html xmlns=\"http://www.w3.org/1999/xhtml\">",
         "<head><meta http-equiv=\"Content-Type\" content=\"text/html; charset=UTF-8\"/>",
         "<title>Hexadoku</title><style type=\"text/css\">",
         "*{margin:0;padding:0;}",
         "body{margin-left:auto;margin-right:auto;text-align:center;width:50em;}",
         "table#board{border:2px solid black;border-collapse:collapse;font-family:\"Courier New\";font-size:16pt;margin-left:auto;margin-right:auto;}",
         "table#board tbody tr td{border:1px solid black;height:32px;min-width:32px;}",
         "table#board tbody tr.sb{border-bottom:2px solid black;}table#board tbody tr td.sr {border-right:2px solid black;}",
         "</style></head><body>",
         "<h1>Hexadoku</h1><h2>By Sam Fredrickson</h2><table id=\"board\"><tbody>",
     };
     private static String footer = "</tbody></table></body></html>";
 
     private static void writeRow(Board board, PrintStream stream, int row)
     {
         char cell;
         stream.print("<tr");
         if(row == 3 || row == 7 || row == 11)
             stream.print(" class=\"sb\"");
         stream.print('>');
         for(int i = 0; i < Board.NUM_DIGITS; ++i)
         {
             stream.print("<td");
             if(i == 3 || i == 7 || i == 11)
                stream.print(" class=\"sr\"");
             stream.print('>');
             cell = board.getCellValue(row * Board.NUM_DIGITS + i);
             stream.print(cell == '\0' ? ' ' : cell);
             stream.print("</td>");
         }
         stream.print("</tr>");
     }
 
     public static void generate(Board board, PrintStream stream)
     {
         for(String line : header)
             stream.print(line);
 
         for(int row = 0; row < Board.NUM_DIGITS; ++row)
             writeRow(board, stream, row);
 
         stream.print(footer);
     }
 }
