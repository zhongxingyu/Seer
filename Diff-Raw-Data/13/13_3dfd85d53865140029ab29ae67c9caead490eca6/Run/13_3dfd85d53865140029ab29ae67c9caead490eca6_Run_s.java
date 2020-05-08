 package edu.drexel.cs544.mcmuc;
 
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 
 import edu.drexel.cs544.mcmuc.actions.Message;
 import edu.drexel.cs544.mcmuc.actions.Presence.Status;
 import edu.drexel.cs544.mcmuc.channels.Controller;
 import edu.drexel.cs544.mcmuc.ui.CLI;
 import edu.drexel.cs544.mcmuc.ui.CLICommand;
 import edu.drexel.cs544.mcmuc.util.Certificate;
 
 /**
  * Run exists to exercise a simple command-line interface to the Multicast Multi-User Chat protocol.
  * 
  * The Multicast Multi-User Chat is a distributed, non-reliable application-layer protocol that uses UDP
  * at the transport layer and provides multi-user chat room creation, discovery, and the exchange of
  * user presence information and messaging without a central server. A client can join a chat room
  * by name, which is hashed to a specific multicast channel within a given range of channels, excluding
  * those already in use. The client then sends messages intended for that multi-user chat on the
  * channel produced by the hashing algorithm. The protocol is distributed and decentralized by using a
  * classical flooding algorithm to forward non-duplicate messages.
  * 
  * @author Brian Balderston
  * @author Dustin Ingram
  * @author Clint Kirberger
  * @author Ben Schilke
  */
 public class Run {
     public static final int CONTROL_PORT = 31941;
 
     /**
      * Create and start a new command-line interface. Create a new Controller and set the user interface
      * to be the previously created command-line interface. Continuously receive and process commands from
      * the user until the received command is 'exit'.
      * 
      * @param args String command-line arguments to program, which are ignored
      */
     public static void main(String[] args) {
 
         CLI cli = new CLI();
         cli.start();
 
         Controller controller = Controller.getInstance();
         controller.setUI(cli);
 
         while (true) {
             CLICommand command = cli.getNextCommand();
             if (command.getCommand() == CLICommand.Command.EXIT) { // Stop and kill this program
                 System.err.println("Exit command received, shutting down...");
                 System.exit(0);
             } else if (command.getCommand() == CLICommand.Command.USEROOM) {
                 controller.useRoom(command.getArg(1), command.getArg(0));
             } else if (command.getCommand() == CLICommand.Command.PRESENCE) {
                 Status s;
                 if (command.getArg(1).equalsIgnoreCase("Online"))
                     s = Status.Online;
                 else if (command.getArg(1).equalsIgnoreCase("Offline"))
                     s = Status.Offline;
                 else {
                     System.err.println("Unknown status");
                     continue;
                 }
                 controller.setRoomStatus(command.getArg(0), s);
             } else if (command.getCommand() == CLICommand.Command.MESSAGE) {
                 controller.messageRoom(command.getArg(0), new Message(controller.getUserName(command.getArg(0)), command.getArg(1)));
             } else if (command.getCommand() == CLICommand.Command.PVTMESSAGE) {
                 controller.messageRoom(command.getArg(1), new Message(controller.getUserName(command.getArg(1)), command.getArg(2), command.getArg(0)));
             } else if (command.getCommand() == CLICommand.Command.ADDKEY) {
             	try {
 					FileInputStream publicKeyFile = new FileInputStream(command.getArg(1));
 					byte publicKey[] = new byte[(int)publicKeyFile.available()];
 					publicKeyFile.read(publicKey);
 					
 					FileInputStream privateKeyFile = new FileInputStream(command.getArg(2));
 					byte privateKey[] = new byte[(int)privateKeyFile.available()];
 					privateKeyFile.read(privateKey);
 					
 					controller.addKeyPair(command.getArg(0), new Certificate("X.509",publicKey), new Certificate("X.509",privateKey));
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
             } else if (command.getCommand() == CLICommand.Command.REMOVEKEY) {
             	try {
 					FileInputStream publicKeyFile = new FileInputStream(command.getArg(1));
 					byte publicKey[] = new byte[(int)publicKeyFile.available()];
 					publicKeyFile.read(publicKey);
 						
 					controller.removeKeyPair(command.getArg(0), new Certificate("X.509",publicKey));
 				} catch (FileNotFoundException e) {
 					e.printStackTrace();
 				} catch (IOException e) {
 					e.printStackTrace();
 				}
             } else if (command.getCommand() == CLICommand.Command.SECUREMESSAGE) {
               	try {
              		FileInputStream publicKeyFile = new FileInputStream(command.getArg(1));
     				byte publicKey[] = new byte[(int)publicKeyFile.available()];
     				publicKeyFile.read(publicKey);
     				
     				Certificate cert = new Certificate("X.509",publicKey);
     				controller.messageRoom(command.getArg(2), new Message(controller.getUserName(command.getArg(2)), command.getArg(3), command.getArg(1), cert));
     			} catch (FileNotFoundException e) {
     				e.printStackTrace();
     			} catch (IOException e) {
     				e.printStackTrace();
     			}
             }
         }
     }
 }
