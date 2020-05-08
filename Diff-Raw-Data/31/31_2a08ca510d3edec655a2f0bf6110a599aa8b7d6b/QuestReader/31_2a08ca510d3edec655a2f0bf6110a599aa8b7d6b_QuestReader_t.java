 package net.teamwraith.wraithquest.files;
 
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Scanner;
 
 import net.teamwraith.wraithquest.files.WraithFile.FileType;
 import net.teamwraith.wraithquest.files.WraithFile.State;
 import net.teamwraith.wraithquest.files.link.Task;
 import net.teamwraith.wraithquest.files.link.Character;
 
 
 public class QuestReader {
 
 	private static Scanner reader;
 
 	public static Task[] getTaskArray(WraithFile questObject) {
 		if (questObject.getFileType() == FileType.CUTSCENE) {
 			return new Task[] {questObject.getTasks()[0]};
 		}
 		else return questObject.getTasks();
 	}
 	
 	public static Task[] getPasswordMatches(String pass) {
 		List<Task> taskMatches = new ArrayList<Task>();
 		for (WraithFile file : FileReader.getWraithFileArray()) {
 			for (int i=0; i < getTaskArray(file).length; i++) {
 				
 				if (file.getFileType() == FileType.CHARACTER && file.getState() != State.DISABLED) {
 					if (file.getPassword().equals(pass) && 
 						getTaskArray(file)[i].getState() != State.DISABLED) {
 						
						taskMatches.add((Task) getTaskArray(file)[i]);
 						
 						System.out.println("Task: "+ getTaskArray(file)[i].getTaskPoint() +" was added.");
 						System.out.println(getTaskArray(file)[i].getState());
 					}
 					
				} else if (file.getState() != State.DISABLED) {
 					if (getTaskArray(file)[i].getPassword().equals(pass) && 
 						getTaskArray(file)[i].getState() != State.DISABLED) {
 							
						taskMatches.add((Task) getTaskArray(file)[i]);
 						
 						System.out.println("Task: "+ getTaskArray(file)[i].getTaskPoint() +" was added.");
 						System.out.println(getTaskArray(file)[i].getState());
 					}
 				}
 			}
 		}
 		return taskMatches.toArray(new Task[taskMatches.size()]);
 	}
 }
