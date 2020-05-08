 package org.gwaspi.cli;
 
 import java.io.IOException;
 import org.gwaspi.global.Text;
 import org.gwaspi.gui.StartGWASpi;
 import org.gwaspi.model.StudyList;
 
 /**
  * Parses, prepares and executes one command read from a script file.
  *
  * @author Fernando Muñiz Fernandez
  * IBE, Institute of Evolutionary Biology (UPF-CSIC)
  * CEXS-UPF-PRBB
  */
 abstract class AbstractScriptCommand implements ScriptCommand {
 
 	private final String commandName;
 
 	AbstractScriptCommand(String commandName) {
 		this.commandName = commandName;
 	}
 
 	@Override
 	public String getCommandName() {
 		return commandName;
 	}
 
 	/**
 	 * parses the study ID, and eventually creates a new study, if requested.
 	 * @param studyIdStr the study ID parameter as given on the command-line
 	 * @param allowNew if true, then we create a new study,
 	 *   if <code>studyIdStr.contains("New Study")</code>
 	 * @return the study ID or Integer.MIN_VALUE, in case of a problem.
 	 */
 	protected static int prepareStudy(String studyIdStr, boolean allowNew) throws IOException {
 
 		int studyId = Integer.MIN_VALUE;
 
 		try {
 			studyId = Integer.parseInt(studyIdStr); // Study Id
 		} catch (Exception ex) {
 			if (allowNew) {
 				if (studyIdStr.contains("New Study")) {
 					studyId = addStudy(studyIdStr/*.substring(10)*/,
 							"Study created by command-line interface");
 				}
 			} else {
 				System.out.println("The Study-id must be an integer value of an existing Study, \""+studyIdStr+"\" is not so!");
 			}
 		}
 
 		return studyId;
 	}
 
 	protected static boolean checkStudy(int studyId) throws IOException {
 
 		boolean studyExists = false;
 
 		Object[][] studyTable = StudyList.getStudyTable();
 		if (studyId != Integer.MIN_VALUE) {
 			for (int i = 0; i < studyTable.length; i++) {
 				if ((Integer) studyTable[i][0] == studyId) {
 					studyExists = true;
 				}
 			}
 		}
 
 		if (!studyExists) {
 			System.out.println("\n" + Text.Cli.studyNotExist);
 			System.out.println(Text.Cli.availableStudies);
 			for (int i = 0; i < studyTable.length; i++) {
 				System.out.println("Study ID: " + studyTable[i][0]);
 				System.out.println("Name: " + studyTable[i][1]);
 				System.out.println("Description: " + studyTable[i][2]);
 				System.out.println("\n");
 			}

			StartGWASpi.exit(); // FIXME remove this!
 		}
 
 		return studyExists;
 	}
 
 	protected static int addStudy(String newStudyName, String description) throws IOException {
 
 		int newStudyId;
 
 		StudyList.insertNewStudy(newStudyName, description);
 
 		Object[][] studyTable = StudyList.getStudyTable();
 
 		newStudyId = (Integer) studyTable[studyTable.length - 1][0];
 
 		return newStudyId;
 	}
 }
