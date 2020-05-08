 package controller;
 
 /*Copyright 2004-2005 Univ.Prof. Dipl.-Ing. Dr.techn. Wolfgang SLANY,
  Andreas Augustin, Sandra Durasiewicz, Bojan Hrnkas, Markus Köberl,
  Bernhard Kornberger, Susanne Schöberl
 
  This file is part of Neptune-Robot-Simulation.
 
  Neptune-Robot-Simulation is free software; you can redistribute it and/or modify
  it under the terms of the GNU General Public License as published by
  the Free Software Foundation; either version 2 of the License, or
  (at your option) any later version.
 
  Neptune-Robot-Simulation is distributed in the hope that it will be useful,
  but WITHOUT ANY WARRANTY; without even the implied warranty of
  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  GNU General Public License for more details.
 
  You should have received a copy of the GNU General Public License
  along with Neptune-Robot-Simulation; if not, write to the Free Software
  Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA  02110-1301  US
  */
 import org.xml.sax.*;
 import java.lang.Integer;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.net.URI;
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import javax.xml.parsers.*;
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.HashMap;
 import controller.LabyrinthField;
 import controller.TUGInformations;
 import controller.IntStringMap;
 import controller.TagName;
 
 /**
  * class representing a labyrinth xpert mode
  */
 public class LabyrinthXpertMode {
     private URL url_ = TUGInformations.getCodeBase();
     private String file_ = TUGInformations.getGameFileName();
     private GameXMLContentHandler game_xml_content_handler_;
     private boolean is_error_;
     private int id_;
     private String level_file_;
     private LabyrinthField labyrinth_field_;
     private LabyrinthXMLContentHandler handler_;
 
     private IntStringMap mark_array_;
     private String start_symbol_, wall_symbol_, diamond_symbol_, way_symbol_,
             zero_symbol_, one_symbol_, two_symbol_, three_symbol_, possible_;
     private String symbol_string_, field_;
     HashMap char_map_, string_map_, used_string_map_;
 
     // -----------------------------------------------------------------------------
     /**
      * Standard constructor
      */
     public LabyrinthXpertMode(IntStringMap mark_array) {
         mark_array_ = mark_array;
         is_error_ = true;
         id_ = 0;
         used_string_map_ = new HashMap();
     }
 
     // -----------------------------------------------------------------------------
     /**
      * allows to set a labyrinth field, to complete xml file
      */
     public void setLabyrinthField(LabyrinthField labyrinth_field) {
         labyrinth_field_ = labyrinth_field;
     }
 
     // -----------------------------------------------------------------------------
     /**
      * load the labyrint from file
      */
     private void loadGameXmlFile() {
         try {
             SAXParserFactory factory = SAXParserFactory.newInstance();
             factory.setValidating(true);
             SAXParser saxParser = factory.newSAXParser();
             XMLReader xmlReader = saxParser.getXMLReader();
 
             game_xml_content_handler_ = new GameXMLContentHandler();
             game_xml_content_handler_.setDocumentBase(url_);
             xmlReader.setContentHandler(game_xml_content_handler_);
             XMLErrorHandler xml_error_handler = new XMLErrorHandler();
             xmlReader.setErrorHandler(xml_error_handler);
             xmlReader.parse(file_);
             is_error_ = xml_error_handler.isXMLError();
             if (is_error_ == false) {
                 int num_levels = game_xml_content_handler_.getNumLevels();
                 if ((id_ > 0) && (id_ <= num_levels))
                     level_file_ = game_xml_content_handler_.getFile(id_);
                 else
                     System.out.println("Wrong parameter for loading level...");
             }
         } catch (SAXException exc) {
             System.err.println(exc.getMessage());
         } catch (ParserConfigurationException exc) {
             System.err.println(exc.getMessage());
         } catch (IOException exc) {
             System.err.println(exc.getMessage());
         }
     }
 
     // -----------------------------------------------------------------------------
     /**
      * read the labyrinth from txt editor
      */
     private String getLevelString() {
         int max_characters_ = 20000;
         BufferedReader in;
         try {
             in = new BufferedReader(new InputStreamReader(
                     new URL(level_file_).openStream()));
             in.mark(max_characters_);
         } catch (Exception e) {
             System.out.println("Open File Error");
             in = null;
         }
         String tmp;
         String level = "";
         if (in != null) {
             try {
                 in.reset();
                 while ((tmp = in.readLine()) != null)
                     level = level.concat(tmp).concat("\n");
             } catch (Exception e) {
                 System.out.println("while:" + e.getMessage());
             }
         }
         return level;
     }
 
     // -----------------------------------------------------------------------------
     /**
      * read the labyrinth from txt editor
      */
     private String getLabyrinthString() {
         String labyrinth_string = new String("");
         labyrinth_string = labyrinth_string.concat("  <")
                 .concat(TagName.LABYRINTH).concat(">\n").concat(symbol_string_)
                 .concat(getStartingDirectionString()).concat(field_)
                 .concat("  </").concat(TagName.LABYRINTH).concat(">\n");
         return labyrinth_string;
     }
 
     private String getStartingDirectionString() {
         String start_direction_string = new String("");
         String start = handler_.getStartingDirection().toString();
         if (start == "no valid direction")
             start = labyrinth_field_.getStartDirection().toString();
         start_direction_string = start_direction_string.concat("    <")
                 .concat(TagName.STARTDIRECTION).concat("> ").concat(start)
                 .concat(" </").concat(TagName.STARTDIRECTION).concat(">\n");
         return start_direction_string;
     }
 
     private String getDimensionString() {
         String string = new String("");
         // int width=handler_.getWidth();
         // int height=handler_.getHeight();
         int height = labyrinth_field_.getNumRows();
         int width = labyrinth_field_.getNumCols();
         string = string.concat("  <").concat(TagName.DIMENSION).concat("> \n");
         string = string.concat("    <").concat(TagName.WIDTH).concat("> ")
                 .concat(String.valueOf(width)).concat(" </")
                 .concat(TagName.WIDTH).concat("> \n");
         string = string.concat("    <").concat(TagName.HEIGHT).concat("> ")
                 .concat(String.valueOf(height)).concat(" </")
                 .concat(TagName.HEIGHT).concat("> \n");
         string = string.concat("  </").concat(TagName.DIMENSION).concat(">\n");
         return string;
     }
 
     private String getNameString() {
         String name_string = new String("");
         name_string = name_string.concat("  <").concat(TagName.NAME)
                 .concat("> ").concat(handler_.getLevelName()).concat(" </")
                 .concat(TagName.NAME).concat(">\n");
         return name_string;
     }
 
     private void initSymbols() {
         possible_ = new String(
                 "0123456789abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ");
 
         symbol_string_ = new String("");
 
         Character ch = handler_.getStartSymbol();
         if (ch != '\n')
             start_symbol_ = Character.toString(ch);
         else
             start_symbol_ = "S";
         possible_ = possible_.replaceFirst(start_symbol_, "");
         symbol_string_ = symbol_string_.concat("    <")
                 .concat(TagName.STARTSYMBOL).concat("> ").concat(start_symbol_)
                 .concat(" </").concat(TagName.STARTSYMBOL).concat(">\n");
         ch = handler_.getWallSymbol();
         if (ch != '\n')
             wall_symbol_ = Character.toString(ch);
         else
             wall_symbol_ = "W";
         possible_ = possible_.replaceFirst(wall_symbol_, "");
         symbol_string_ = symbol_string_.concat("    <")
                 .concat(TagName.WALLSYMBOL).concat("> ").concat(wall_symbol_)
                 .concat(" </").concat(TagName.WALLSYMBOL).concat(">\n");
         ch = handler_.getDiamondSymbol();
         if (ch != '\n')
             diamond_symbol_ = Character.toString(ch);
         else
             diamond_symbol_ = "D";
         possible_ = possible_.replaceFirst(diamond_symbol_, "");
         symbol_string_ = symbol_string_.concat("    <")
                 .concat(TagName.DIAMONDSYMBOL).concat("> ")
                 .concat(diamond_symbol_).concat(" </")
                 .concat(TagName.DIAMONDSYMBOL).concat(">\n");
         ch = handler_.getWaySymbol();
         if (ch != '\n')
             way_symbol_ = Character.toString(ch);
         else
             way_symbol_ = ".";
         possible_ = possible_.replaceFirst(way_symbol_, "");
         symbol_string_ = symbol_string_.concat("    <")
                 .concat(TagName.WAYSYMBOL).concat("> ").concat(way_symbol_)
                 .concat(" </").concat(TagName.WAYSYMBOL).concat(">\n");
         ch = handler_.getZeroSymbol();
         if (ch != '\n')
             zero_symbol_ = Character.toString(ch);
         else
             zero_symbol_ = "0";
         possible_ = possible_.replaceFirst(zero_symbol_, "");
         symbol_string_ = symbol_string_.concat("    <")
                 .concat(TagName.ZEROSYMBOL).concat("> ").concat(zero_symbol_)
                 .concat(" </").concat(TagName.ZEROSYMBOL).concat(">\n");
         ch = handler_.getOneSymbol();
         if (ch != '\n')
             one_symbol_ = Character.toString(ch);
         else
             one_symbol_ = "1";
         possible_ = possible_.replaceFirst(one_symbol_, "");
         symbol_string_ = symbol_string_.concat("    <")
                 .concat(TagName.ONESYMBOL).concat("> ").concat(one_symbol_)
                 .concat(" </").concat(TagName.ONESYMBOL).concat(">\n");
         ch = handler_.getTwoSymbol();
         if (ch != '\n')
             two_symbol_ = Character.toString(ch);
         else
             two_symbol_ = "2";
         possible_ = possible_.replaceFirst(two_symbol_, "");
         symbol_string_ = symbol_string_.concat("    <")
                 .concat(TagName.TWOSYMBOL).concat("> ").concat(two_symbol_)
                 .concat(" </").concat(TagName.TWOSYMBOL).concat(">\n");
         ch = handler_.getThreeSymbol();
         if (ch != '\n')
             three_symbol_ = Character.toString(ch);
         else
             three_symbol_ = "3";
         possible_ = possible_.replaceFirst(three_symbol_, "");
         symbol_string_ = symbol_string_.concat("    <")
                 .concat(TagName.THREESYMBOL).concat("> ").concat(three_symbol_)
                 .concat(" </").concat(TagName.THREESYMBOL).concat(">\n");
 
         char_map_ = handler_.getSymboleMap();
         string_map_ = handler_.getStringSymboleMap();
 
     }
 
     private String getCellMarkSymbol(int number) {
         String tmp = new String("");
 
         if (number == 0)
             tmp = zero_symbol_;
         else if (number == 1)
             tmp = one_symbol_;
         else if (number == 2)
             tmp = two_symbol_;
         else if (number == 3)
             tmp = three_symbol_;
         else {
             if (used_string_map_.containsKey(mark_array_.getFromInt(number)) == true)
                 tmp = (String) string_map_.get(mark_array_.getFromInt(number));
             else
                 tmp = newSymbol(mark_array_.getFromInt(number));
         }
 
         return tmp;
     }
 
     private String newSymbol(String mark_string) {
         symbol_string_ = symbol_string_.concat("    <")
                 .concat(TagName.SYMBOLLIST).concat("> \n");
         symbol_string_ = symbol_string_.concat("      <")
                 .concat(TagName.SYMBOL).concat("> \n");
         String c = "";
         if (string_map_.containsKey(mark_string) == true) {
             c = (String) string_map_.get(mark_string);
             symbol_string_ = symbol_string_.concat("        <")
                     .concat(TagName.CHARSYMBOL).concat("> ").concat(c)
                     .concat(" </").concat(TagName.CHARSYMBOL).concat("> \n");
             possible_ = possible_.replaceFirst(c, "");
             used_string_map_.put(new String(mark_string), new String(c));
         } else {
             c = possible_.substring(0, 1);
             symbol_string_ = symbol_string_.concat("        <")
                     .concat(TagName.CHARSYMBOL).concat("> ").concat(c)
                     .concat(" </").concat(TagName.CHARSYMBOL).concat("> \n");
             possible_ = possible_.replaceFirst(c, "");
             char_map_.put(new String(c), new String(mark_string));
             string_map_.put(new String(mark_string), new String(c));
             used_string_map_.put(new String(mark_string), new String(c));
         }
         symbol_string_ = symbol_string_.concat("        <")
                 .concat(TagName.STRINGSYMBOL).concat("> ").concat(mark_string)
                 .concat(" </").concat(TagName.STRINGSYMBOL).concat("> \n");
         symbol_string_ = symbol_string_.concat("      </")
                 .concat(TagName.SYMBOL).concat("> \n");
         symbol_string_ = symbol_string_.concat("    </")
                 .concat(TagName.SYMBOLLIST).concat("> \n");
         return c;
     }
 
     private String createCurrentLevelXMLString() {
         try {
             SAXParserFactory factory = SAXParserFactory.newInstance();
             factory.setValidating(true);
             SAXParser saxParser = factory.newSAXParser();
             XMLReader xmlReader = saxParser.getXMLReader();
 
             handler_ = new LabyrinthXMLContentHandler();
             handler_.setDocumentBase(url_);
             handler_.setDocument(level_file_);
 
             xmlReader.setContentHandler(handler_);
             XMLErrorHandler xml_error_handler = new XMLErrorHandler();
             xmlReader.setErrorHandler(xml_error_handler);
             xmlReader.parse(level_file_);
             is_error_ = xml_error_handler.isXMLError();
            if (is_error_)
                System.out.println("Error in XML handler for current level!");
         } catch (SAXException exc) {
             System.err.println(exc.getMessage());
         } catch (ParserConfigurationException exc) {
             System.err.println(exc.getMessage());
         } catch (IOException exc) {
             System.err.println(exc.getMessage());
         }
 
         initSymbols();
         String file = "";
         if (labyrinth_field_ != null) {
             field_ = new String("");
             int start_point_row = labyrinth_field_.getStartpointRow();
             int start_point_col = labyrinth_field_.getStartpointCol();
             field_ = field_.concat("    <").concat(TagName.LABYRINTHFIELD)
                     .concat(">\n");
             for (int i = 0; i < labyrinth_field_.getNumRows(); i++) {
                 for (int j = 0; j < labyrinth_field_.getNumCols(); j++) {
                     boolean way = labyrinth_field_.isWay(i, j);
                     if (way == true) {
                         int tmp = labyrinth_field_.getCellMark(i, j);
                         if (tmp > -1)
                             field_ = field_.concat(getCellMarkSymbol(tmp));
                         else if (labyrinth_field_.containsDiamond(i, j))
                             field_ = field_.concat(diamond_symbol_);
                         else if (i == start_point_row && j == start_point_col)
                             field_ = field_.concat(start_symbol_);
                         else
                             field_ = field_.concat(way_symbol_);
                     } else
                         field_ = field_.concat(wall_symbol_);
                 }
                 field_ = field_.concat("\n");
             }
             field_ = field_.concat("    </").concat(TagName.LABYRINTHFIELD)
                     .concat(">\n");
 
             String name = getNameString();
             String dim = getDimensionString();
             String labyrinth = getLabyrinthString();
 
             file = "<?xml version=\"1.0\" encoding=\"iso-8859-1\"?>\n"
                     .concat("<!DOCTYPE LabyrinthLevel SYSTEM \"LabyrinthLevel.dtd\">\n")
                     .concat("<").concat(TagName.LABYRINTHLEVEL).concat(">\n")
                     .concat(name).concat(dim).concat(labyrinth).concat("</")
                     .concat(TagName.LABYRINTHLEVEL).concat(">\n");
         }
 
         return file;
     }
 
     // -----------------------------------------------------------------------------
     /**
      * prints the labyrinth level to the txt editor
      */
     public String getLabyrinth(int id) {
         id_ = id;
         loadGameXmlFile();
         String labyrinth = getLevelString();
         return labyrinth;
     }
 
     // -----------------------------------------------------------------------------
     /**
      * generates a XML-File from the labyrinth field and returns a String
      * representation of this xml file
      */
     public String getCurrentLabyrinthXML(int id) {
         id_ = id;
         loadGameXmlFile();
         String labyrinth = createCurrentLevelXMLString();
         return labyrinth;
     }
 
     // -----------------------------------------------------------------------------
     /**
      * generates a String representation of the labyrinth field like
      * <LabyrinthField> WWWWW W.D.W WSWWW </LabyrinthField>
      */
     public String getCurrentLabyrinth(int id) {
         id_ = id;
         loadGameXmlFile();
         String labyrinth = createCurrentLevelXMLString();
         // System.out.println ("LAB: \n"+ field_);
         return field_;
     }
 
     public String getCurrentLabyrinth(String source) {
         String start = "<".concat(TagName.LABYRINTHFIELD).concat(">");
         String end = "</".concat(TagName.LABYRINTHFIELD).concat(">");
         String tmp = "    ".concat(source.substring(source.indexOf(start),
                 source.indexOf(end) + end.length()));
         return tmp;
     }
}
