 package com.github.gotos.rpgfilenamechanger;
 
 import java.io.File;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.naming.event.EventContext;
 
 import com.github.gotos.rpgreader.engine.DataReader;
 import com.github.gotos.rpgreader.engine.LuciferCommonEvent;
 import com.github.gotos.rpgreader.engine.LuciferDatabase;
 import com.github.gotos.rpgreader.engine.LuciferEventCommand;
 import com.github.gotos.rpgreader.engine.LuciferHeroUnit;
 import com.github.gotos.rpgreader.engine.LuciferMapEvent;
 import com.github.gotos.rpgreader.engine.LuciferMapEventPage;
 import com.github.gotos.rpgreader.engine.LuciferMapUnit;
 import com.github.gotos.rpgreader.engine.LuciferMoveCommand;
 
 /**
  * Main Class, the class that will be started.
  * 
  * @author alina
  *
  */
 public class Main {
 
 	
 	private Main() { }
 	
 	/**
 	 * This will start everything, eventually. For now, it takes two parameters, as stated below, and renames only charsets.
 	 * 
 	 * @param args pathToProject oldFilename, newFilename
 	 */
 	public static void main(String[] args) {
 		if (args.length != 3) {
 			System.out.println("Usage: java Main pathToProject oldFilename newFilename");
 			System.exit(1);
 		}
 		String path = null;
 		for (String folder : new File(args[0]).list()) {
 			if (folder.equalsIgnoreCase("charset")) {
 				path = args[0] + "/" + folder;
 			}
 		}
 		if (path == null) {
 			System.out.println("No Charset-folder found in project.");
 			System.exit(2);
 		}
 		File oldFile = null;
 		for (String filename : new File(path).list()) {
			if (filename.toLowerCase().startsWith(args[1].toLowerCase()) && filename.length() == args[0].length() + 4) {
 				oldFile = new File(path + "/" + filename);
 			}
 		}
 		if (oldFile == null) {
 			System.out.println("oldFile does not exist!");
 			System.exit(3);
 		}
 		String extension = oldFile.getPath().substring(oldFile.getPath().lastIndexOf("."));
 		File newFile = new File(path + "/" + args[2] + extension);
 		if (newFile.exists()) {
 			System.out.println("newFile already exist! Exiting...");
 			System.exit(4);
 		}
 		DataReader dr;
 		
 		//get db
 		/*for (String filename : new File(args[0]).list()) {
 			if (filename.equalsIgnoreCase("rpg_rt.ldb")) {
 				dr = DataReader.parseFile(args[0] + "/" + filename);
 				try {
 					dr.nextUnitZeroID();
 					LuciferDatabase db = new LuciferDatabase(dr);
 					for (LuciferHeroUnit hero : db.getHeroes()) {
 						if (hero != null) {
 							if (hero.getGraphicFile().equalsIgnoreCase(args[1])) {
 								hero.setGraphicFile(args[2]);
 							}
 						}
 					}
 					if (db.getSystem().getAirship().equalsIgnoreCase(args[1])) {
 						db.getSystem().setAirship(args[2]);
 					}
 					if (db.getSystem().getShip().equalsIgnoreCase(args[1])) {
 						db.getSystem().setShip(args[2]);
 					}
 					if (db.getSystem().getBoat().equalsIgnoreCase(args[1])) {
 						db.getSystem().setBoat(args[2]);
 					}
 					for (LuciferCommonEvent ce : db.getCommonEvents()) {
 						if (ce != null) {
 							renameInCommands(args[1], args[2], ce.getCommands());
 						}
 					}
 
 					FileOutputStream fos = new FileOutputStream(args[0] + "/" + filename);
 					fos.write(db.write());
 					fos.close();
 					
 				} catch (IOException e) {
 					System.out.println("Database broken!");
 					System.exit(5);
 				}
 				
 			}
 		}*/
 		
 		//get Maps
 		for (String filename : new File(args[0]).list()) {
 			if (filename.toLowerCase().startsWith("map")) {
 				dr = DataReader.parseFile(args[0] + "/" + filename);
 				try {
 					dr.nextUnitZeroID();
 					LuciferMapUnit map = new LuciferMapUnit(dr);
 					
 					for (LuciferMapEvent event : map.getEvents()) {
 						if (event != null) {
 							for (LuciferMapEventPage page : event.getPages()) {
 								if (page != null) {
 									if (page.getCharset().equalsIgnoreCase(args[1])) {
 										page.setCharset(args[2]);
 									}
 									
 									renameInCommands(args[1], args[2], page.getCommands());
 									
 									for (int i = 0; i < page.getRoute().getCommands().size(); i++) {
 										LuciferMoveCommand move = page.getRoute().getCommands().get(i);
 										if (move.type == 0x22 && move.filename.equalsIgnoreCase(args[1])) {
 											move = new LuciferMoveCommand(move.type, move.data, args[2]);
 											page.getRoute().getCommands().set(i, move);
 										}
 									}
 								}
 							}
 						}
 					}
 					
 					FileOutputStream fos = new FileOutputStream(args[0] + "/" + filename);
 					fos.write(map.write());
 					fos.close();
 					
 				} catch (IOException e) {
 					System.out.print("Map broken! ");
 					System.out.println(filename);
 				}
 			}
 		}
 		
 		//rename file
 		oldFile.renameTo(newFile);
 	}
 	
 	private static void renameInCommands(String oldname, String newname, List<LuciferEventCommand> commands)
 			throws UnsupportedEncodingException, IllegalArgumentException {
 		for (int k = 0; k < commands.size(); k++) {
 			LuciferEventCommand command = commands.get(k);
 			if (command.type == LuciferEventCommand.CHANGE_HERO_GRAPHIC || command.type == LuciferEventCommand.CHANGE_VEHICLE_GRAPHIC) {
 				if (command.string.equalsIgnoreCase(oldname)) {
 					command.string = newname;
 				}
 			} else if (command.type == LuciferEventCommand.MOVE_EVENT) {
 				List<LuciferMoveCommand> list = LuciferMoveCommand.assembleMoveCommands(command);
 				for (int i = 0; i < list.size(); i++) {
 					LuciferMoveCommand move = list.get(i);
 					if (move.type == 0x22 && move.filename.equalsIgnoreCase(oldname)) {
 						move = new LuciferMoveCommand(move.type, move.data, newname);
 						list.set(i, move);
 					}
 				}
 				command = LuciferMoveCommand.disassembleMoveCommands(
 						list, command.data[0], command.data[1], command.data[2], command.data[3], command.depth);
 				commands.set(k, command);
 			}
 		}
 	}
 }
