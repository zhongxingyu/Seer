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
 import java.awt.*;
 import javax.swing.JLabel;
 import java.util.Random;
 
 /**
  * class representing a turing machine.
  */
 
 public class TuringMachine {
     private int num_visited_;
     private boolean single_step_;
 
     private InstructionList instruct_list_;
     private InstructionList possible_instructions_;
     private Robot robot_;
     private LabyrinthField lab_field_;
     private String message_ = "   Programm läuft...";
     private Component component_;
 
     public TuringMachine(Component component, LabyrinthField lab_field,
             Robot robot) {
         component_ = component;
         lab_field_ = lab_field;
         robot_ = robot;
         Direction initial_direction_ = new Direction(
                 lab_field_.getStartDirection());
         num_visited_ = 0; // sollte Roboter wissen
 
         lab_field_.setStartDirection(initial_direction_); // ????????????
         possible_instructions_ = new InstructionList();
         instruct_list_ = new InstructionList();
     }
 
     public void setupTuringMachine(Component component,
             LabyrinthField lab_field, Robot robot) {
         component_ = component;
         lab_field_ = lab_field;
         robot_ = robot;
         Direction initial_direction_ = new Direction(
                 lab_field_.getStartDirection());
         num_visited_ = 0; // sollte Roboter wissen
 
         lab_field_.setStartDirection(initial_direction_); // ????????????
         possible_instructions_ = new InstructionList();
         instruct_list_ = new InstructionList();
     }
 
     // -----------------------------------------------------------------------------
     /**
      * method returns the conditions of robot's environment
      * 
      * @return info about the current condition
      */
     protected Condition getFieldConditions() {
         int current_row = robot_.getPosRow();
         int current_col = robot_.getPosCol();
         int cell_mark = lab_field_.getCellMark(current_row, current_col);
         boolean wall_ahead = false;
         boolean wall_left = false;
         boolean wall_right = false;
         int state = robot_.getState();
         try {
             switch (robot_.getCurrentDirection().getDirection()) {
             case Direction.DIRECTION_UP:
                 wall_ahead = lab_field_.isWall(current_row - 1, current_col);
                 wall_left = lab_field_.isWall(current_row, current_col - 1);
                 wall_right = lab_field_.isWall(current_row, current_col + 1);
                 break;
             case Direction.DIRECTION_DOWN:
                 wall_ahead = lab_field_.isWall(current_row + 1, current_col);
                 wall_left = lab_field_.isWall(current_row, current_col + 1);
                 wall_right = lab_field_.isWall(current_row, current_col - 1);
                 break;
             case Direction.DIRECTION_LEFT:
                 wall_ahead = lab_field_.isWall(current_row, current_col - 1);
                 wall_left = lab_field_.isWall(current_row + 1, current_col);
                 wall_right = lab_field_.isWall(current_row - 1, current_col);
                 break;
             case Direction.DIRECTION_RIGHT:
                 wall_ahead = lab_field_.isWall(current_row, current_col + 1);
                 wall_left = lab_field_.isWall(current_row - 1, current_col);
                 wall_right = lab_field_.isWall(current_row + 1, current_col);
                 break;
             }
         } catch (IllegalDirectionException ex) {
             System.err.println(ex.getMessage());
         }
 
         Condition temp_condition = new Condition(state, cell_mark, wall_left,
                 wall_right, wall_ahead);
 
         return temp_condition;
     }
 
     // -----------------------------------------------------------------------------
     /**
      * method performes the given action and checks if the robot crashes
      * 
      * @param action
      *            specifies what to do
      * @return true if no problems occure during the action, false if the robot
      *         is crashed
      */
     protected boolean performAction(Action action) {
         int current_row = robot_.getPosRow();
         int current_col = robot_.getPosCol();
         boolean action_performed = false;
 
         try {
             switch (action.getMovingDirection()) {
             case Action.MOVE_AHEAD:
                 robot_.moveForward();
                 break;
             case Action.MOVE_BACK:
                 robot_.goBack();
                 break;
             case Action.MOVE_LEFT:
                 robot_.goLeft();
                 break;
             case Action.MOVE_RIGHT:
                 robot_.goRight();
                 break;
             }
 
         } catch (IllegalDirectionException ex) {
             System.err.println(ex.getMessage());
         }
         boolean is_wall = false;
         try {
             is_wall = lab_field_.isWall(robot_.getPosRow(), robot_.getPosCol());
         } catch (IllegalArgumentException ex) {
             System.err.println("ERROR: Robot's position is not valid!");
         }
         if (is_wall) {
             robot_.setPos(current_row, current_col);
 
         } else {
             robot_.setState(action.GetState());
             lab_field_.setCellMark(current_row, current_col,
                     action.getCellMark());
             action_performed = true;
         }
 
         return action_performed;
     }
 
     private boolean tryToPerformAction(Action action) {
         int current_row = robot_.getPosRow();
         int current_col = robot_.getPosCol();
         boolean action_ok = false;
 
         try {
             switch (action.getMovingDirection()) {
             case Action.MOVE_AHEAD:
                 robot_.moveForward();
                 break;
             case Action.MOVE_BACK:
                 robot_.goBack();
                 break;
             case Action.MOVE_LEFT:
                 robot_.goLeft();
                 break;
             case Action.MOVE_RIGHT:
                 robot_.goRight();
                 break;
             }
 
         } catch (IllegalDirectionException ex) {
             System.err.println(ex.getMessage());
         }
         boolean is_wall = false;
         try {
             is_wall = lab_field_.isWall(robot_.getPosRow(), robot_.getPosCol());
         } catch (IllegalArgumentException ex) {
             System.err.println("ERROR: Robot's position is not valid!");
         }
         if (is_wall) {
             action_ok = false;
         } else {
             action_ok = true;
         }
         robot_.restoreLastDirection();
         robot_.setPos(current_row, current_col);
 
         return action_ok;
     }
 
     // -----------------------------------------------------------------------------
     /**
  *
  */
     public void setInstructionList(InstructionList instruct_list) {
         instruct_list_ = instruct_list;
         possible_instructions_.removeAllInstructions();
     }
 
     public void newStep() {
         possible_instructions_.removeAllInstructions();
     }
 
     public void setVisited() {
         if ((lab_field_.isVisited(robot_.getPosRow(), robot_.getPosCol()) == false)
                 && (checkReturnedToStartPoint() == false)) {
             lab_field_.setVisited(robot_.getPosRow(), robot_.getPosCol());
             num_visited_++;
 
         }
     }
 
     public void takeAndCalcDiamonds() {
         boolean contains = false;
         try {
             contains = lab_field_.containsDiamond(robot_.getPosRow(),
                     robot_.getPosCol());
         } catch (IllegalArgumentException ex) {
             System.err.println("ERROR: Robot's position is not valid!");
         }
         if (contains) {
             robot_.pickUpThing();
             lab_field_.removeDiamond(robot_.getPosRow(), robot_.getPosCol());
         }
     }
 
     public int getNumVisited() {
         return num_visited_;
     }
 
     // -----------------------------------------------------------------------------
     public boolean runSingleStep() throws InterruptedException {
         Instruction current_instruction;
         Condition field_condition;
         field_condition = getFieldConditions();
         int num_instructions = instruct_list_.size();
         boolean made_step = false;
 
         for (int index = 0; index < num_instructions; index++) {
             current_instruction = instruct_list_.getInstructionAtPos(index);
 
             /*
              * System.out.println("instruct conditions: "+current_instruction.
              * getCondition().isWallLeft()
              * +current_instruction.getCondition().isWallRight()
              * +current_instruction.getCondition().isWallAhead()
              * +current_instruction.getCondition().getCellMark()
              * +current_instruction.getCondition().isNewArea());
              */
 
             if (field_condition.equals(current_instruction.getCondition())) {
                 if (!performAction(current_instruction.getAction())) {
                     message_ = "   Oje, der Roboter ist in die Wand gekracht!";
                     component_.repaint();
                     robot_.restoreLastDirection();
                 } else {
                     message_ = "   Programm läuft...";
                     takeAndCalcDiamonds();
                     made_step = true;
                 }
                 setVisited();
                 component_.repaint();
                 break;
             } else
                message_ = "   Keine Regel gefunden (Programm nicht vollständig)!";
         }
         return made_step;
     }
 
     public boolean runRandomSingleStep() {
         InstructionList possible = getPossibleInstructions();
         boolean made_step = makeSingleStepWithRandInstruction();
         return made_step;
     }
 
     public InstructionList getPossibleInstructions() {
         Instruction current_instruction;
         Condition field_condition;
         field_condition = getFieldConditions();
         int num_instructions = instruct_list_.size();
         boolean made_step = false;
         for (int index = 0; index < num_instructions; index++) {
             current_instruction = instruct_list_.getInstructionAtPos(index);
             if (field_condition.equals(current_instruction.getCondition())
                     && (current_instruction.isAktive() == true)) {
                 possible_instructions_.addInstruction(current_instruction);
             }
         }
         if (possible_instructions_.size() == 0)
            message_ = "   Keine Regel gefunden (Programm nicht vollständig)!";
         else if (possible_instructions_.size() == 1)
             message_ = "   " + possible_instructions_.size()
                     + " Regel gefunden";
         else
             message_ = "   ACHTUNG:  " + possible_instructions_.size()
                     + " Regel gefunden!";
         return possible_instructions_;
     }
 
     /*
      * public void selectPossibleInstructions() { Instruction
      * current_instruction; Condition field_condition; field_condition =
      * getFieldConditions(); int num_instructions = instruct_list_.size();
      * boolean made_step = false; for(int index=0; index < num_instructions;
      * index++) { current_instruction =
      * instruct_list_.getInstructionAtPos(index);
      * if(field_condition.equals(current_instruction.getCondition())) {
      * possible_instructions_.addInstruction(current_instruction); } }
      * if(possible_instructions_.size()==0)
      * message_="   Keine Regel gefunden, das Programm ist nicht vollständig!";
      * else if (possible_instructions_.size()==1) message_="   "+
      * possible_instructions_.size()+" Regel gefunden"; else
      * message_="   ACHTUNG:  "+
      * possible_instructions_.size()+" Regel gefunden!"; }
      */
 
     public boolean makeSingleStepWithRandInstruction() {
         boolean made_step = false;
         Random random_generator_ = new Random();
         if (possible_instructions_.size() > 0) {
             int rand = random_generator_.nextInt(possible_instructions_.size());
             Condition field_condition;
             field_condition = getFieldConditions();
             Instruction current_instruction;
             current_instruction = possible_instructions_
                     .getInstructionAtPos(rand);
 
             if (field_condition.equals(current_instruction.getCondition())) {
                 if (!performAction(current_instruction.getAction())) {
                     message_ = "   Oje, der Roboter ist in die Wand gekracht!";
                     component_.repaint();
                     robot_.restoreLastDirection();
                 } else {
                     message_ = "   Programm läuft...";
                     takeAndCalcDiamonds();
                     made_step = true;
                 }
                 setVisited();
                 component_.repaint();
             } else
                 System.err.println("Keine Regel gefunden!");
         } else
             // message_="   Keine Regel gefunden, das Programm ist nicht vollständig!";
             message_ = "   ";
         return made_step;
     }
 
     public String getMessage() {
         return message_;
     }
 
     /**
      * method checks if the robot returned to his startpoint.
      */
     public boolean checkReturnedToStartPoint() {
         boolean start_point = false;
         int current_row = robot_.getPosRow();
         int current_col = robot_.getPosCol();
         if (lab_field_.isStartpoint(current_row, current_col)
                 && (robot_.getNumSteps() > 0)) {
             start_point = true;
         }
         return start_point;
     }
 
     /**
      * method checks if the robot's position is the startpoint.
      */
     public boolean checkStartPoint() {
         boolean start_point = false;
         int current_row = robot_.getPosRow();
         int current_col = robot_.getPosCol();
         if (lab_field_.isStartpoint(current_row, current_col)) {
             start_point = true;
         }
         return start_point;
     }
 }
