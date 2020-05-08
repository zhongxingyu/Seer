 import java.util.List;
 import java.util.Scanner;
 import java.util.StringTokenizer;
 import java.util.Vector;
 import java.io.*;
 
 public class Storage {
 
 	private List<AbstractTask> loadTaskList = new Vector<AbstractTask>();
 
 	public Storage() {
 	}
 
 	public List<AbstractTask> loadTaskList() throws IOException {
 		return readMoustaskFile();
 	}
 
 	public List<AbstractTask> readMoustaskFile() throws IOException {
 		try {
 			File moustaskFile = new File("moustask.txt");
 			Scanner readMoustaskFile = new Scanner(moustaskFile);
 			// if moustask.txt has data, import into taskList
 			// else return empty taskList
 			while (readMoustaskFile.hasNextLine()) {
 				String line = readMoustaskFile.nextLine();
 				StringTokenizer taskToken = new StringTokenizer(line, "|");
 				int numberOfTaskToken = taskToken.countTokens();
 				String taskCategory = taskToken.nextToken().trim();
 				String taskDescription = taskToken.nextToken().trim();
 
 				if (taskCategory.equalsIgnoreCase("Floating")) {
 					FloatingTask floatingTask = new FloatingTask(
 							taskDescription);
 					if (numberOfTaskToken == 3) {
 						String taskStatus = taskToken.nextToken().trim();
 						if (taskStatus.equalsIgnoreCase("Done")) {
 							floatingTask.setStatus(AbstractTask.Status.DONE);
 						} else if (taskStatus.equalsIgnoreCase("Impossible")) {
 							floatingTask
 									.setStatus(AbstractTask.Status.IMPOSSIBLE);
 						}
 					} else {
 						String taskVenue = taskToken.nextToken().trim();
 						String taskStatus = taskToken.nextToken().trim();
 						floatingTask.setVenue(taskVenue);
 						if (taskStatus.equalsIgnoreCase("Done")) {
 							floatingTask.setStatus(AbstractTask.Status.DONE);
 						} else if (taskStatus.equalsIgnoreCase("Impossible")) {
 							floatingTask.setStatus(AbstractTask.Status.IMPOSSIBLE);
 						}
 					}
 					loadTaskList.add(floatingTask);
 				}
 
 				else if (taskCategory.equalsIgnoreCase("Deadline")) {
 					String taskEndDate = taskToken.nextToken().trim();
 					DeadlineTask deadlineTask = new DeadlineTask(
 							taskDescription, taskEndDate);
 					if (numberOfTaskToken == 4) {
 						String taskStatus = taskToken.nextToken().trim();
 						if (taskStatus.equalsIgnoreCase("Done")) {
 							deadlineTask.setStatus(AbstractTask.Status.DONE);
 						} else if (taskStatus.equalsIgnoreCase("Impossible")) {
 							deadlineTask
 									.setStatus(AbstractTask.Status.IMPOSSIBLE);
 						}
 					} else {
 						String taskVenue = taskToken.nextToken().trim();
 						String taskStatus = taskToken.nextToken().trim();
 						deadlineTask.setEndDate(taskEndDate);
 						deadlineTask.setVenue(taskVenue);
 						if (taskStatus.equalsIgnoreCase("Done")) {
 							deadlineTask.setStatus(AbstractTask.Status.DONE);
 						} else if (taskStatus.equalsIgnoreCase("Impossible")) {
 							deadlineTask
 									.setStatus(AbstractTask.Status.IMPOSSIBLE);
 						}
 					}
 					loadTaskList.add(deadlineTask);
 				}
 
 				else {
 					String taskStartDate = taskToken.nextToken().trim();
 					String taskEndDate = taskToken.nextToken().trim();
 					TimedTask timedTask = new TimedTask(taskDescription,
 							taskStartDate, taskEndDate);
 					if (numberOfTaskToken == 5) {
 						String taskStatus = taskToken.nextToken().trim();
 						if (taskStatus.equalsIgnoreCase("Done")) {
 							timedTask.setStatus(AbstractTask.Status.DONE);
 						} else if (taskStatus.equalsIgnoreCase("Impossible")) {
 							timedTask.setStatus(AbstractTask.Status.IMPOSSIBLE);
 						}
 					} else {
 						String taskVenue = taskToken.nextToken().trim();
 						String taskStatus = taskToken.nextToken().trim();
 						timedTask.setVenue(taskVenue);
 						if (taskStatus.equalsIgnoreCase("Done")) {
 							timedTask.setStatus(AbstractTask.Status.DONE);
 						} else if (taskStatus.equalsIgnoreCase("Impossible")) {
 							timedTask.setStatus(AbstractTask.Status.IMPOSSIBLE);
 						}
 					}
 					loadTaskList.add(timedTask);
 				}
 			}
 			readMoustaskFile.close();
 			return loadTaskList;
 		} catch (Exception moustaskFileNotFound) {
 			Writer createNewMoustaskFile = new BufferedWriter(new FileWriter(
 					"moustask.txt"));
 			createNewMoustaskFile.close();
 		}
 		return loadTaskList;
 	}
 
 	public void writeTaskList(List<AbstractTask> taskList) throws IOException {
 		BufferedWriter writeMoustaskFile = new BufferedWriter(new FileWriter(
 				"moustask.txt", false));
 		for (int i = 0; i < taskList.size(); i++) {
 			Enum taskType = taskList.get(i).getType();
			if (taskType == AbstractTask.Type.FLOATING) {
 				if (((FloatingTask) taskList.get(i)).getVenue()
 						.equalsIgnoreCase("")) {
 					writeMoustaskFile.write("Floating | "
 							+ ((FloatingTask) taskList.get(i)).getDescription()
 							+ " | "
 							+ ((FloatingTask) taskList.get(i)).getStatus()
 									.toString());
 					writeMoustaskFile.newLine();
 				} else {
 					writeMoustaskFile.write("Floating | "
 							+ ((FloatingTask) taskList.get(i)).getDescription()
 							+ " | "
 							+ ((FloatingTask) taskList.get(i)).getVenue()
 							+ " | " + taskList.get(i).getStatus().toString());
 					writeMoustaskFile.newLine();
 				}
 			} else if (taskType == AbstractTask.Type.DEADLINE) {
 				if (((DeadlineTask) taskList.get(i)).getVenue()
 						.equalsIgnoreCase("")) {
 					writeMoustaskFile.write("Deadline | "
 							+ ((DeadlineTask) taskList.get(i)).getDescription()
 							+ " | "
 							+ ((DeadlineTask) taskList.get(i)).getEndDate()
 							+ " | "
 							+ ((DeadlineTask) taskList.get(i)).getStatus()
 									.toString());
 					writeMoustaskFile.newLine();
 				} else {
 					writeMoustaskFile.write("Deadline | "
 							+ ((DeadlineTask) taskList.get(i)).getDescription()
 							+ " | "
 							+ ((DeadlineTask) taskList.get(i)).getEndDate()
 							+ " | "
 							+ ((DeadlineTask) taskList.get(i)).getVenue()
 							+ " | "
 							+ ((DeadlineTask) taskList.get(i)).getStatus()
 									.toString());
 					writeMoustaskFile.newLine();
 				}
 			} else {
 				if (((TimedTask) taskList.get(i)).getVenue().equalsIgnoreCase(
 						"")) {
 					writeMoustaskFile.write("Timed | "
 							+ ((TimedTask) taskList.get(i)).getDescription()
 							+ " | "
 							+ ((TimedTask) taskList.get(i)).getStartDate()
 							+ " | "
 							+ ((TimedTask) taskList.get(i)).getEndDate()
 							+ " | "
 							+ ((TimedTask) taskList.get(i)).getStatus()
 									.toString());
 					writeMoustaskFile.newLine();
 				} else {
 					writeMoustaskFile.write("Timed | "
 							+ ((TimedTask) taskList.get(i)).getDescription()
 							+ " | "
 							+ ((TimedTask) taskList.get(i)).getStartDate()
 							+ " | "
 							+ ((TimedTask) taskList.get(i)).getEndDate()
 							+ " | "
 							+ ((TimedTask) taskList.get(i)).getVenue()
 							+ " | "
 							+ ((TimedTask) taskList.get(i)).getStatus()
 									.toString());
 					writeMoustaskFile.newLine();
 				}
 			}
 		}
 		writeMoustaskFile.close();
 	}
 
 }
