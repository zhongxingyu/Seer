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
 import java.net.URL;
 import java.net.MalformedURLException;
 import java.io.IOException;
 import javax.xml.parsers.SAXParserFactory;
 import javax.xml.parsers.SAXParser;
 import javax.xml.parsers.ParserConfigurationException;
 import org.xml.sax.XMLReader;
 import org.xml.sax.ErrorHandler;
 import org.xml.sax.SAXException;
 import org.xml.sax.SAXParseException;
 
 /**
  * class representing an action
  */
 public class InstructionTxtWriter {
     private String line_;
 
     private String condition_start_string_;
     private String condition_end_string_;
     private String action_start_string_;
     private String action_end_string_;
     private String condition_action_separator_string_;
     private String separator_string_;
     private String comment_out_line_string_;
     private String comment_out_start_string_;
     private String comment_out_end_string_;
     private String deactivate_instruction_string_;
     private String wall_string_;
     private String way_string_;
     private String direction_left_string_;
     private String direction_ahead_string_;
     private String direction_right_string_;
     private String direction_back_string_;
     private URL url_ = null;
     private String path_ = null;
     private InstructionTXTXMLContentHandler handler_;
     private IntStringMap mark_array_, state_array_;
 
     // -----------------------------------------------------------------------------
     /**
      * Standard constructor parses the config xml file to get the settings
      * 
      * @param url
      */
     public InstructionTxtWriter(URL url) {
         InstructionTXTXMLContentHandler handler_ = new InstructionTXTXMLContentHandler();
         try {
             path_ = (new URL(url, "config/InstructionTXT.xml")).toString();
         } catch (MalformedURLException ex) {
             System.err.println(ex.getMessage());
         }
         try {
             SAXParserFactory factory = SAXParserFactory.newInstance();
             factory.setValidating(true); // neu
             SAXParser saxParser = factory.newSAXParser();
             XMLReader xmlReader = saxParser.getXMLReader();
             handler_ = new InstructionTXTXMLContentHandler();
             xmlReader.setContentHandler(handler_);
             XMLErrorHandler xml_error_handler = new XMLErrorHandler();
             xmlReader.setErrorHandler(xml_error_handler);
             xmlReader.parse(path_);
         } catch (SAXParseException exc) {
            System.out.println("SAXParseException");
             System.out.println("In File " + exc.getSystemId());
             System.out.println(exc.getMessage());
         } catch (SAXException exc) {
             System.out.println("SAXException");
             System.err.println(exc.getMessage());
             System.err.println(exc.toString());
         } catch (ParserConfigurationException exc) {
             System.out.println("ParserConfigurationException");
             System.err.println(exc.getMessage());
         } catch (IOException exc) {
             System.out.println("IOException");
             System.err.println(exc.getMessage());
         }
 
         condition_start_string_ = handler_.ConditionStartString();
         condition_end_string_ = handler_.ConditionEndString();
         action_start_string_ = handler_.ActionStartString();
         action_end_string_ = handler_.ActionEndString();
         condition_action_separator_string_ = handler_
                 .ConditionActionSeparatorString();
         separator_string_ = handler_.SeparatorString();
         comment_out_line_string_ = handler_.CommentOutLineString();
         comment_out_start_string_ = handler_.CommentOutStartString();
         comment_out_end_string_ = handler_.CommentOutEndString();
         deactivate_instruction_string_ = handler_.DeactivateInstructionString();
         wall_string_ = handler_.WallString();
         way_string_ = handler_.WayString();
         direction_left_string_ = handler_.DirectionLeftString();
         direction_ahead_string_ = handler_.DirectionAheadString();
         direction_right_string_ = handler_.DirectionRightString();
         direction_back_string_ = handler_.DirectionBackString();
 
     }
 
     // -----------------------------------------------------------------------------
     /**
      * set a mark array to map the input strings to integers
      * 
      * @param mark_array
      */
     public void setMarkArray(IntStringMap mark_array) {
         mark_array_ = mark_array;
     }
 
     // -----------------------------------------------------------------------------
     /**
      * set a state array to map the input strings to integers
      * 
      * @param mark_array
      */
     public void setStateArray(IntStringMap state_array) {
         state_array_ = state_array;
     }
 
     // -----------------------------------------------------------------------------
     /**
      * generate a string with the informations of the instruction list
      * 
      * @param instruction_list
      * @return string
      */
     public String writeText(InstructionList list) {
         InstructionList instructions;
         instructions = list;
         String code;
         code = new String();
         try {
             for (int count = 1; count <= instructions.size(); count++) {
                 Instruction tmp_instruction = instructions
                         .getInstructionAtPos(instructions.size() - count);
                 try {
                     String tmp = writeLine(tmp_instruction);
                     code = code.concat(tmp).concat("\n");
                 } catch (IllegalDirectionException exc) {
                     System.err.println(exc.getMessage());
                 }
             }
         } catch (IllegalArgumentException exc) {
             System.err.println(exc.getMessage());
         }
         return code;
     }
 
     // -----------------------------------------------------------------------------
     /**
      * generate a string taking informations from a instruction
      * 
      * @param instruction
      * @return string
      * @exception IllegalDirectionException
      *                thrown when a syntax error is detected
      */
     private String writeLine(Instruction instruction)
             throws IllegalDirectionException {
         String left, ahead, right, direction;
         Condition tmp_condition = instruction.getCondition();
 
         String condition_state = state_array_.getFromInt(tmp_condition
                 .getState());
         String condition_cell_mark = mark_array_.getFromInt(tmp_condition
                 .getCellMark());
 
         boolean wall_left = tmp_condition.isWallLeft();
         if (wall_left == true)
             left = wall_string_;
         else
             left = way_string_;
 
         boolean wall_ahead = tmp_condition.isWallAhead();
         if (wall_ahead == true)
             ahead = wall_string_;
         else
             ahead = way_string_;
 
         boolean wall_right = tmp_condition.isWallRight();
         if (wall_right == true)
             right = wall_string_;
         else
             right = way_string_;
 
         Action tmp_action = instruction.getAction();
 
         int dir = tmp_action.getMovingDirection();
         if (dir == 1)
             direction = direction_ahead_string_;
         else if (dir == 2)
             direction = direction_right_string_;
         else if (dir == 3)
             direction = direction_back_string_;
         else if (dir == 4)
             direction = direction_left_string_;
         else
             throw (new IllegalDirectionException("Illegal direction"));
 
         String action_cellmark = mark_array_.getFromInt(tmp_action
                 .getCellMark());
         String action_state = state_array_.getFromInt(tmp_action.GetState());
 
         String line;
         if (instruction.isAktive() == true) {
             line = condition_start_string_.concat(left).concat(ahead)
                     .concat(right).concat(separator_string_)
                     .concat(condition_state).concat(separator_string_)
                     .concat(condition_cell_mark).concat(condition_end_string_)
                     .concat(condition_action_separator_string_)
                     .concat(action_start_string_).concat(action_cellmark)
                     .concat(separator_string_).concat(direction)
                     .concat(separator_string_).concat(action_state)
                     .concat(action_end_string_);
         } else {
             line = deactivate_instruction_string_
                     .concat(condition_start_string_).concat(left).concat(ahead)
                     .concat(right).concat(separator_string_)
                     .concat(condition_state).concat(separator_string_)
                     .concat(condition_cell_mark).concat(condition_end_string_)
                     .concat(condition_action_separator_string_)
                     .concat(action_start_string_).concat(action_cellmark)
                     .concat(separator_string_).concat(direction)
                     .concat(separator_string_).concat(action_state)
                     .concat(action_end_string_);
         }
         return line;
     }
}
