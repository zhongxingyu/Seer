 /*
  * Copyright (C) 2013 Felix Wiemuth
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package nipgm.command;
 
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 import nipgm.control.Game;
 import nipgm.data.impl.GameStatus.State;
 
 /**
  * Base class for commands from the user to the game. Subclasses define concrete
  * commands. Every subclass should define the command state permissions in a
  * static part using 'putStatePermissions(Class commandClass, CommandPermissions
  * p)'.
  *
  * @author Felix Wiemuth
  */
 public abstract class AbstractCommand {
 
     /**
      * Contains the states in which the command is allowed to be executed,
      * optional special exceptions to be thrown in special disallowed states and
      * preconditions that have to be met to allow the execution.
      */
     protected static class CommandPermissions {
 
         private final Set<State> allowedStates = new HashSet<>();
         private final Map<State, CommandStateException> disallowedStateExceptions = new HashMap<>();
         private final List<CommandPrecondition> preconditions = new LinkedList<>();
 
         public CommandPermissions(State... allowedStates) {
             this.allowedStates.addAll(Arrays.asList(allowedStates));
         }
 
         public void putDisallowedStateException(State state, CommandStateException ex) {
             disallowedStateExceptions.put(state, ex);
         }
 
         public void addPrecondition(CommandPrecondition precondition) {
             preconditions.add(precondition);
         }
     }
     private final static Map<Class, CommandPermissions> statePermissions = new HashMap<>();
 
     /**
      * Execute the command.
      *
      * @return Information on the successful execution of the command. If no
      * information is provided, 'null' is returned.
      * @throws CommandExecuteException - if something during the execution went
      * wrong
      * @throws CommandStateException - if the game is currently in a state not
      * allowed for this command
     * @throws CommandPreconditionException - if speicific preconditions for the
      * execution are not met
      */
     public CommandFeedback execute() throws CommandExecuteException, CommandStateException, CommandPreconditionException {
         CommandPermissions p = statePermissions.get(this.getClass());
         try {
             checkState(p);
             checkPreconditions(p);
             return exec();
         } catch (CommandExecuteException ex) { //TODO need to catch CommandStateException?
             //TODO log exception
             throw ex;
         }
     }
 
     private void checkState(CommandPermissions p) throws CommandStateException {
         if (!p.allowedStates.contains(Game.getStatus().getState())) {
             CommandStateException ex = p.disallowedStateExceptions.get(Game.getStatus().getState());
             if (ex == null) { //build standard exception text
                 StringBuilder sb = new StringBuilder();
                 for (State state : p.allowedStates) {
                     sb.append("'");
                     sb.append(Game.getStatus().getStateName(state));
                     sb.append("', ");
                 }
                //delete last seperator
                 if (sb.length() >= 2) {
                     sb.delete(sb.length() - 2, sb.length());
                 }
                 ex = new CommandStateException(
                         Game.getFormattedText("ex_CommandNotAllowedInCurrentState",
                         Game.getStatus().getStateName(),
                         sb.toString()));
             }
             throw ex;
         }
     }
 
     private void checkPreconditions(CommandPermissions p) throws CommandPreconditionException {
         StringBuilder sb = new StringBuilder(Game.getText("ex_CommandPreconditionNotMet"));
         sb.append('\n');
         boolean ok = true;
         for (CommandPrecondition precondition : p.preconditions) {
             if (!precondition.check()) {
                 ok = false;
                 sb.append(precondition.getName()).append(": ")
                         .append(Game.getText("cmdP_" + precondition.getName()))
                         .append("\n");
             }
         }
         if (!ok) {
             throw new CommandPreconditionException(sb.toString());
         }
     }
 
     /**
      * Same as 'execute()' with an additional exception handler handling
      * exceptions.
      *
      * @param exceptionHandler - the exception handler to use
      * @return Information on the successful execution of the command. If no
      * information is provided, 'null' is returned.
      */
     public CommandFeedback execute(ExceptionHandler exceptionHandler) {
         try {
             return execute();
         } catch (CommandExecuteException ex) {
             CommandFeedback feedback = new CommandFeedback(ex);
             exceptionHandler.handleException(ex);
             return feedback;
         }
     }
 
     /**
      * Same as 'execute(ExceptionHandler exceptionHandler)' with an additional
      * feedback handler handling the feedback.
      *
      * @param exceptionHandler - the exception handler to use
      * @param feedbackHandler - the feedback handler to use
      * @return Information on the successful execution of the command (which in
      * this case is also handled by the feedback handler).
      */
     public CommandFeedback execute(ExceptionHandler exceptionHandler, FeedbackHandler feedbackHandler) {
         CommandFeedback feedback = execute(exceptionHandler);
         if (feedback != null) {
             feedbackHandler.handleFeedback(feedback);
         }
         return feedback;
     }
 
     /**
      * Carry out the command execution.
      *
      * @return Information on the successful execution of the command.
      * @throws CommandExecuteException - indicates that
      */
     protected abstract CommandFeedback exec() throws CommandExecuteException;
 
     protected static void putStatePermissions(Class commandClass, CommandPermissions p) {
         statePermissions.put(commandClass, p);
     }
 }
