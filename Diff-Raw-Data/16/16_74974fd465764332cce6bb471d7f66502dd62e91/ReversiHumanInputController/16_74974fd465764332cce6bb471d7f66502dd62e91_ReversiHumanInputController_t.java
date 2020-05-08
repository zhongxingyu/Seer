 package reversi.game.controller.input;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Scanner;
 
 import reversi.models.ReversiPlayer;
 import reversi.models.game.ReversiInput;
 import reversi.parser.ReversiParser;
 import reversi.server.ReversiServerResponse;
 import reversi.server.commands.ReversiCommand;
 import reversi.server.controllers.exceptions.ExitException;
 import base.game.controllers.input.HumanInputController;
 import base.models.Position;
 
 /**
  * Input controller used for local games run through the Console.
  * 
  * @author dereekb
  * 
  */
 public class ReversiHumanInputController implements
 		HumanInputController<ReversiPlayer, ReversiInput> {
 
 	private final ReversiParser parser = new ReversiParser();
 
 	@Override
 	public ReversiInput inputForHuman(ReversiPlayer human) throws IOException {
 
 		ReversiInput reversiInput = null;
 		Scanner scanner = human.getInputScanner();
 
 		boolean validMove = false;
 		boolean validInput = false;
 
 		while (!validMove) {
 			
 			String input = scanner.nextLine();
 			ReversiCommand command = parser.parseCommand(input);
 
 			switch (command.getType()) {
 				case Move: {
 					
 					List<String> parameters = command.getParameters();
 					String column = parameters.get(0);
 					String row = parameters.get(1);
 					
					try{
						Integer rowInteger = new Integer(row);
						Position position = new Position(column, rowInteger);
						reversiInput = new ReversiInput(human, position);
						validMove = true;
						validInput = true;
					}catch(NumberFormatException e){
						validMove = false;
						validInput = false;
					}
 				}
 					break;
 				case Display: {
 					human.toggleAsciiDisplay();
 					validInput = true;
 					validMove = false;
 					ReversiServerResponse.sendOk(human);
 				} break;
 				case Undo:
 				case Redo: {
 					reversiInput = new ReversiInput(human, command);
 					validInput = true;
 					validMove = true;
 				} break;
 				case Exit: {
 					throw new ExitException();
 				}
 				default: break;
 			}
 			
 			if(validInput == false){
 				ReversiServerResponse.sendIllegal(human);
 			}
 		}
 		
 		return reversiInput;
 	}
 
 }
