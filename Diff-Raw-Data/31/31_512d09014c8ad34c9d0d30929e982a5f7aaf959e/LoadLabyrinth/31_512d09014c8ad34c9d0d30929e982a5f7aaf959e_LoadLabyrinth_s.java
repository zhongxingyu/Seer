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
 import javax.xml.parsers.SAXParserFactory;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.ParserConfigurationException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 import java.io.File;
 import java.io.IOException;
 import java.net.URL;
 import java.util.HashMap;
 import org.xml.sax.InputSource;
 import org.xml.sax.EntityResolver;
 import javax.swing.JOptionPane;
 import java.io.ByteArrayInputStream;
 
 //----------------------------------------------------------------------
 /**
  * class for loading a Labyrinth
  */
 
 public class LoadLabyrinth {
 
     /**
      * the max number of rows max_row_=rows-1
      */
     protected int max_row_;
     /**
      * the max number of cols max_col_=cols-1
      */
     protected int max_col_;
 
     /**
      * document base
      */
     private URL document_base_;
 
     /**
      * labyrinth field
      */
     protected LabyrinthField labyrinth_field_;
 
     /**
      * create labyrinth
      */
     protected CreateLabyrinth create_labyrinth_;
 
     /**
      * labyrinth XML content handler
      */
     protected LabyrinthXMLContentHandler labyrinth_xml_content_handler_;
 
     /**
      * true if style is defined
      */
     private boolean is_style_;
 
     /**
      * path to wall picture
      */
     private String wall_picture_;
 
     /**
      * path to way picture
      */
     private String way_picture_;
 
     /**
      * path to robot picture
      */
     private String robot_picture_;
 
     /**
      * path to diamond picture
      */
     private String diamond_picture_;
 
     /**
      * path to start picture
      */
     private String start_picture_;
 
     /**
      * filname of the XML file
      */
     private String filename_;
 
     /**
      * int string map for the cell mark
      */
     private IntStringMap mark_array_;
 
     /**
      * source of the XML file
      */
     private InputSource input_;
 
     /**
      * constant for useing the filname
      */
     private final static int FILENAME = 1;
 
     /**
      * constant for useing the input source
      */
     private final static int INPUTSOURCE = 2;
 
     // --------------------------------------------------------------------------
 
     /**
      * CopyConstructor
      * 
      * @param lab
      */
     public LoadLabyrinth(LoadLabyrinth lab) {
         this.max_row_ = lab.max_row_;
         this.max_col_ = lab.max_col_;
         this.document_base_ = lab.document_base_;
         this.labyrinth_field_ = (LabyrinthField) lab.labyrinth_field_.clone();
         this.create_labyrinth_ = lab.create_labyrinth_;
         this.labyrinth_xml_content_handler_ = lab.labyrinth_xml_content_handler_;
         this.is_style_ = lab.is_style_;
         this.wall_picture_ = lab.wall_picture_;
         this.way_picture_ = lab.way_picture_;
         this.robot_picture_ = lab.robot_picture_;
         this.diamond_picture_ = lab.diamond_picture_;
         this.start_picture_ = lab.start_picture_;
         this.filename_ = lab.filename_;
     }
 
     // ------------------------------------------------------------------------------
     /**
      * Standard constructor for using the input source string
      * 
      * @param input
      *            input source string
      * @param document_base
      *            document base
      * @param mark_array
      *            int string map for the cell mark
      * @param source
      *            , to mark the string as source
      * @exception IllegalArgumentException
      *                if the specified input source can't be loaded
      */
     public LoadLabyrinth(String input, URL document_base,
             IntStringMap mark_array, boolean source)
             throws IllegalArgumentException {
         mark_array_ = mark_array;
         document_base_ = document_base;
         StringBuilder propertiesEncoded = new StringBuilder();
         for (int i = 0; i < input.length(); i++) {
             char c = input.charAt(i);
             if (c <= 0x7e)
                 propertiesEncoded.append((char) c);
             else
                 propertiesEncoded.append(String.format("\\u%04x", (int) c));
         }
         try {
             input_ = new InputSource(new ByteArrayInputStream(propertiesEncoded
                     .toString().getBytes("ISO-8859-1")));
         } catch (Exception e) {
 
         }
 
         parse(INPUTSOURCE);
     }
 
     // ------------------------------------------------------------------------------
     /**
      * Standard constructor for using the file
      * 
      * @param filename
      *            name of the XML file to load
      * @param document_base
      *            document base
      * @param mark_array
      *            int string map for the cell mark
      * @exception IllegalArgumentException
      *                if the specified file can't be found
      */
     public LoadLabyrinth(String filename, URL document_base,
             IntStringMap mark_array) throws IllegalArgumentException {
         mark_array_ = mark_array;
         document_base_ = document_base;
         filename_ = filename;
         labyrinth_field_ = null;
         parse(FILENAME);
     }
 
     /**
      * starting parsing with the given input type
      * 
      * @param input_type
      *            FILENAME or INPUTSOURCE
      * @exception IllegalArgumentException
      */
     private void parse(int input_type) throws IllegalArgumentException {
         try {
             // Load the ContentHandler
             // ********************************************
             SAXParserFactory factory = SAXParserFactory.newInstance();
             factory.setValidating(true);
             SAXParser saxParser = factory.newSAXParser();
             XMLReader xmlReader = saxParser.getXMLReader();
             labyrinth_xml_content_handler_ = new LabyrinthXMLContentHandler();
             labyrinth_xml_content_handler_.setDocumentBase(document_base_);
             if (input_type == FILENAME)
                 labyrinth_xml_content_handler_.setDocument(filename_);
             xmlReader.setContentHandler(labyrinth_xml_content_handler_);
             if (input_type == INPUTSOURCE)
                 xmlReader.setEntityResolver(new LabyrinthEntityResolver());
             XMLErrorHandler xml_error_handler = new XMLErrorHandler();
             xmlReader.setErrorHandler(xml_error_handler);
             if (input_type == FILENAME)
                 xmlReader.parse(filename_);
             else if (input_type == INPUTSOURCE)
                 xmlReader.parse(input_);
 
             // (END OF) Load the ContentHandler
             // ********************************************
 
             // Just some variables
             // ***********************************************************
             boolean is_load_labyrinth = false;
             char start_symbol;
             char wall_symbol;
             char diamond_symbol;
             char way_symbol;
             HashMap symbols_map;
             int set_num_diamonds;
             int set_num_way_cells;
             int set_min_way_lenght;
             int set_max_way_lenght;
             int set_build_angles;
             int set_build_trees;
             int set_build_places;
             int set_build_loops;
             int rows = labyrinth_xml_content_handler_.getHeight();
             int cols = labyrinth_xml_content_handler_.getWidth();
             // (END OF) Just some variables
             // *************************************************
 
             if (xml_error_handler.isXMLError() == false
                     && labyrinth_xml_content_handler_.isXMLError() == false) {
                 // This point IS reached at normal operation
                 // ************************************
                 try {
                     if (cols <= 0)
                         throw (new IllegalArgumentException("\"<"
                                 + TagName.WIDTH + ">\"" + "must be > 0"));
                     if (rows <= 0)
                         throw (new IllegalArgumentException("\"<"
                                 + TagName.HEIGHT + ">\"" + "must be > 0"));
                     labyrinth_field_ = new LabyrinthField(rows, cols);
 
                     max_row_ = rows - 1;
                     max_col_ = cols - 1;
 
                     String name = labyrinth_xml_content_handler_.getLevelName();
                     labyrinth_field_.setName(name);
 
                     if (labyrinth_xml_content_handler_.isInstructionList() == true) {
                         /*
                          * SAXParserFactory factory =
                          * SAXParserFactory.newInstance(); SAXParser saxParser =
                          * factory.newSAXParser(); XMLReader xmlReader =
                          * saxParser.getXMLReader(); String
                          * instruction_file_name =
                          * labyrinth_xml_content_handler_
                          * .getInstructionFileName();
                          * instruction_listxml_content_handler = new
                          * InstructionListXMLContentHandler();
                          * xmlReader.setContentHandler
                          * (instruction_listxml_content_handler);
                          * xmlReader.parse(instruction_file_name);
                          * labyrinth_field_.setInstructionList();
                          */
                     }
 
                     if (labyrinth_xml_content_handler_.isLoadLabyrinth() == true) {
                         // Is not executed at normal operation,
                         // isLoadLabyrinth=false
                        System.out.println("isLoadLabyrinth()");
                         is_load_labyrinth = true;
                         start_symbol = labyrinth_xml_content_handler_
                                 .getStartSymbol();
                         wall_symbol = labyrinth_xml_content_handler_
                                 .getWallSymbol();
                         diamond_symbol = labyrinth_xml_content_handler_
                                 .getDiamondSymbol();
                         way_symbol = labyrinth_xml_content_handler_
                                 .getWaySymbol();
                         symbols_map = labyrinth_xml_content_handler_
                                 .getSymboleMap();
                         char[][] labyrinth = labyrinth_xml_content_handler_
                                 .getLabyrinth();
                         Direction start_direction = labyrinth_xml_content_handler_
                                 .getStartingDirection();
                         labyrinth_field_.setStartDirection(start_direction);
 
                         char current_symbol;
                         for (int row_count = 0; row_count < rows; row_count++) {
                             for (int col_count = 0; col_count < cols; col_count++) {
                                 current_symbol = labyrinth[row_count][col_count];
                                 if (current_symbol == start_symbol) {
                                     labyrinth_field_.convertToStartpoint(
                                             row_count, col_count);
                                 } else if (current_symbol == wall_symbol) {
                                     // current_cell.convertToWall();
                                     // labyrinth_field_.convertToWall(row_count,col_count);
                                 } else if (current_symbol == diamond_symbol) {
                                     labyrinth_field_.convertToWay(row_count,
                                             col_count);
                                     labyrinth_field_.setDiamond(row_count,
                                             col_count);
                                 } else if (current_symbol == way_symbol) {
                                     labyrinth_field_.convertToWay(row_count,
                                             col_count);
                                 } else {
                                     char[] char_sympol = new char[1];
                                     char_sympol[0] = current_symbol;
                                     String sympol = (String) symbols_map
                                             .get(new String(char_sympol));
                                     labyrinth_field_.convertToWay(row_count,
                                             col_count);
                                     labyrinth_field_.setCellMark(row_count,
                                             col_count,
                                             mark_array_.setString(sympol));
                                 }
                             }
                         }
                     }
 
                     if (labyrinth_xml_content_handler_.isCreateLabyrinth() == true) {
                         // This piece of code is executed at normal operation,
                         // isCreateLabyrinth=true
                        System.out.println("isCreateLabyrinth()");
                         set_num_diamonds = labyrinth_xml_content_handler_
                                 .getNumDiamonds();
                         set_num_way_cells = labyrinth_xml_content_handler_
                                 .getNumWayCells();
                         set_min_way_lenght = labyrinth_xml_content_handler_
                                 .getMinWayLenght();
                         set_max_way_lenght = labyrinth_xml_content_handler_
                                 .getMaxWayLenght();
                         set_build_angles = labyrinth_xml_content_handler_
                                 .getBuildAngles();
                         set_build_trees = labyrinth_xml_content_handler_
                                 .getBuildTrees();
                         set_build_places = labyrinth_xml_content_handler_
                                 .getBuildPlaces();
                         set_build_loops = labyrinth_xml_content_handler_
                                 .getBuildLoops();
 
                         create_labyrinth_ = new CreateLabyrinth(
                                 labyrinth_field_);
                         create_labyrinth_.setNumDiamonds(set_num_diamonds);
                         create_labyrinth_.setNumWayCells(set_num_way_cells);
                         create_labyrinth_.setMinWayLenght(set_min_way_lenght);
                         create_labyrinth_.setMaxWayLenght(set_max_way_lenght);
                         create_labyrinth_.setBuildAngles(set_build_angles);
                         create_labyrinth_.setBuildTrees(set_build_trees);
                         create_labyrinth_.setBuildPlaces(set_build_places);
                         create_labyrinth_.setBuildLoops(set_build_loops);
 
                         create_labyrinth_.calculateWays();
 
                         if (set_num_diamonds != 0) {
                             create_labyrinth_.setDiamonds();
                         }
                         labyrinth_field_ = create_labyrinth_
                                 .getLabyrinthField();
                     }
 
                     is_style_ = labyrinth_xml_content_handler_.isStyle();
                 } catch (IllegalArgumentException exc) // SECOND TRY ENDS HERE
                 {
                     labyrinth_field_ = null;
                    System.out.println("ERROR");
                     if (filename_ != null) {
                         URL new_url = new URL(document_base_, filename_);
                         System.out.println("In File " + new_url.toString());
                     }
                     System.out.println(exc.getMessage());
                     JOptionPane error;
                     error = new JOptionPane();
                     error.showMessageDialog(null, exc.getMessage(),
                             "Marvin-10 Programmfehler", error.ERROR_MESSAGE);
                 }
             }
         } // FIRST TRY ENDS HERE
         catch (SAXParseException exc) {
             JOptionPane error;
             error = new JOptionPane();
             error.showMessageDialog(null, exc.getMessage(),
                     "Marvin-10 Programmfehler", error.ERROR_MESSAGE);
         } catch (SAXException exc) {
             JOptionPane error;
             error = new JOptionPane();
             error.showMessageDialog(null, exc.getMessage(),
                     "Marvin-10 Programmfehler", error.ERROR_MESSAGE);
         } catch (ParserConfigurationException exc) {
             JOptionPane error;
             error = new JOptionPane();
             error.showMessageDialog(null, exc.getMessage(),
                     "Marvin-10 Programmfehler", error.ERROR_MESSAGE);
         } catch (IOException exc) {
             JOptionPane error;
             error = new JOptionPane();
             error.showMessageDialog(null, exc.getMessage(),
                     "Marvin-10 Programmfehler", error.ERROR_MESSAGE);
         }
 
     }
 
     // ------------------------------------------------------------------------------
     /**
      * returns the labyrinth field
      * 
      * @return labyrinth_field_
      */
     public LabyrinthField getLabyrinthField() {
         return labyrinth_field_;
     }
 
     // ------------------------------------------------------------------------------
     /**
      * returne the int string map for the cell mark
      * 
      * @return mark_array_
      */
 
     public IntStringMap getMarkArray() {
         return mark_array_;
     }
 
     // ------------------------------------------------------------------------------
     /**
      * standard toString method for debugging
      * 
      * @return info about the labyrinth field in string-format
      */
 
     public String toString() {
         return ("LoadLabyrinth: " + super.toString());
     }
 
     // ----------------------------------------------------------------------------
 
     /**
      * returns if a style is set or not
      * 
      * @return is_style_
      */
     public boolean isStyle() {
         return is_style_;
     }
 
     // ----------------------------------------------------------------------------
 
     /**
      * returns the path to the wall picture
      * 
      * @return wall picture
      */
     public String getWallPicture() {
         return labyrinth_xml_content_handler_.getWallPicture();
     }
 
     // ----------------------------------------------------------------------------
 
     /**
      * returns the path to the way picture
      * 
      * @return way picture
      */
     public String getWayPicture() {
         return way_picture_ = labyrinth_xml_content_handler_.getWayPicture();
     }
 
     // ----------------------------------------------------------------------------
 
     /**
      * returns the path to the robot picture
      * 
      * @return robot picture
      */
     public String getRobotPicture() {
         return robot_picture_ = labyrinth_xml_content_handler_
                 .getRobotPicture();
     }
 
     // ----------------------------------------------------------------------------
 
     /**
      * returns the path to the diamond picture
      * 
      * @return diamond picture
      */
     public String getDiamondPicture() {
         return diamond_picture_ = labyrinth_xml_content_handler_
                 .getDiamondPicture();
     }
 
     // ----------------------------------------------------------------------------
 
     /**
      * returns the path to the start picture
      * 
      * @return start picture
      */
     public String getStartPicture() {
         return start_picture_ = labyrinth_xml_content_handler_
                 .getStartPicture();
     }
 
 }
