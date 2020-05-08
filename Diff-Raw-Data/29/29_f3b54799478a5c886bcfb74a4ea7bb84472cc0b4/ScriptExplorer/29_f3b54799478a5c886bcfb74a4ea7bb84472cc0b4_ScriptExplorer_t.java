 /**
  * 
  */
 package org.carnegiesciencecenter.buhl;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.FilenameFilter;
 import java.io.IOException;
 import java.io.StringReader;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.Scanner;
 
 import javax.swing.table.AbstractTableModel;
 
 /**
  * @author Anh Le
  *
  */
 public class ScriptExplorer {
 	
 	File buttonSetPath, spiceFile;
 	HashMap<String, DsScript> ds;
 	ScriptTableModel tableModel;
 	SpiceScript spiceScript;
 	ArrayList<DsCmd> allCmds;
 	String output;
 	String outputPath;
 	int countYes;
 	HashMap<String,String> namePairs;
 	ArrayList<String> usedNames;
 	File newSetPath;
 	String[] pageNames = {"1","2","3","4","5","6","7","8","9"};
 	HashMap<String,CmdNode> allNodes;
 	int buttonStartNum;
 	
 	DeviceManager dm;
 	
 	public static Settings globalConf;
 	
 	ScriptExplorer() {
 		ds = new HashMap<String, DsScript>();
 		tableModel = new ScriptTableModel();
 		globalConf = new Settings();
 		globalConf.loadSettings();
 	}
 	
 	int LoadDSFiles() {
 		if (buttonSetPath == null)
 			return 0;
 
 		// load page names
 		File cap = new File(buttonSetPath.getPath(), "Captions.txt");
 		Scanner in = null;
 		int page = 0;
 		try {
 			in = new Scanner(cap);
 			String line;
 			while (in.hasNextLine()) {
 				line = in.nextLine().trim();
 				pageNames[page++] = line;
 			}
 			//System.out.println(line);
 		}
 		catch (FileNotFoundException e) {
 			System.out.println("Cannot open file: Captions.txt");
 		} 
 		finally {
 			if (in != null)
 				in.close();
 		}
 
 		// load all script names
 		File list[] = buttonSetPath.listFiles(new DSFileFilter());
 		tableModel.data = new String[list.length][tableModel.getColumnCount()];
 		for (int i=0; i<list.length; i++) {
 			DsScript s = new DsScript(list[i]);
 			s.parseDS();
 			tableModel.setValueQuietly(s.id, i, 0);
 			tableModel.setValueQuietly(s.name, i, 1);
 			tableModel.setValueQuietly("No", i, 2);
 			tableModel.setValueQuietly(s.title, i, 3);
 			//System.out.println(s.id + "  " + Integer.toString(s.pageNum));
 			tableModel.setValueQuietly(pageNames[s.pageNum], i, 4);
 			tableModel.setValueQuietly(s.modified, i, 5);
 			tableModel.setValueQuietly(Integer.toString(s.stopPositions.size())+" / "+(s.loadNextButton ? "Y" : "N"), i, 6);
 			tableModel.setValueQuietly(s.uniqueCmds, i, 7);
 			//System.out.println(s.id);
 			ds.put(s.id, s);
 		}
 		tableModel.fireTableDataChanged();
 		return list.length;
 	}
 	
 	void removeUnused() {
 		Object[][] newTbl = new String[countYes][tableModel.getColumnCount()];
 		int i=0;
 		for (int k=0; k<tableModel.getRowCount(); k++) {
 			if (tableModel.data[k][2].equals("Yes")) {
 				newTbl[i] = tableModel.data[k];
 				i++;
 			}
 		}
 		tableModel.data = newTbl;
 		tableModel.fireTableDataChanged();
 	}
 	
 	int LoadSPICEfile() {
 		allNodes = new HashMap<String,CmdNode>();
 		spiceScript = new SpiceScript(spiceFile);
 		spiceScript.parseSPICE(allNodes);
 		updateUsage();
 		return 0;		
 	}
 	
 	void updateUsage() {
 		usedNames = new ArrayList<String>();
 		countYes = 0;
 		for (int k=0; k<tableModel.getRowCount(); k++) {
 			String button = (String) tableModel.getValueAt(k, 0);
 
 			while (button.startsWith("0"))
 					button = button.substring(1);
 			//System.out.println(button);
 			if (spiceScript.buttons.contains(button)) {
 				tableModel.data[k][2] = "Yes";
 				usedNames.add(button);
 				countYes++;
 			}
 			else
 				tableModel.data[k][2] = "No";
 		}
 		tableModel.fireTableDataChanged();
 	}
 	
 	void copyToNewSet(boolean newPage) {
 		String oldPath, newPath;
 		try {
 			oldPath = buttonSetPath.getCanonicalPath();
 			newPath = newSetPath.getCanonicalPath();
 		}
 		catch (Exception e) {
 			System.out.println("Cannot open soure/dest path.");
 			e.printStackTrace();
 			return;
 		}
 		
 		// find the last name in use
 		File list[] = newSetPath.listFiles(new DSFileFilter());
 		int max = -1;
 		for (int i=0; i<list.length; i++) {
 			String s = list[i].getName().substring(1).replace(".sct", "");
 			int num = Integer.parseInt(s);
 			if (max < num)
 				max = num;
 		}
 		int startNum;
 		
 		// pick a possible name
 		if (max == -1)
 			startNum = 1;
 		else {
 			if (newPage)
 				startNum = (int) (Math.floor(((double)max-1)/(8*12)) + 1)*(8*12) + 1;
 			else
 				startNum = max + 1;
 		}
 		
 		// sort used names to ensure that ShowNext will work
 		Collections.sort(usedNames, new Comparator<String>() {
 		    public int compare(String o1, String o2) {
 		        return Integer.parseInt(o1) - Integer.parseInt(o2);
 		    }
 		});
 		
 		// generate name pairs
 		namePairs = new HashMap<String,String>();
 		Iterator<String> it = usedNames.iterator();
 		while (it.hasNext()) {
 			String oldNum = it.next();
 			String newNum = Integer.toString(startNum++);
 			String oldName = oldPath + System.getProperty("file.separator") + "F" + oldNum + ".sct";
 			String newName = newPath + System.getProperty("file.separator") + "F" + newNum + ".sct";
 			namePairs.put(oldNum, newNum);
 			System.out.println(oldName + " --> " + newName);
 			copyFile(oldName, newName);
 		}
 		
 		// change Spice source
 		String newSpiceName = "";
 		try {
 			FileReader f1 = new FileReader(spiceFile);
 			newSpiceName = spiceFile.getAbsolutePath().replace(".SHOW","_COPIED.SHOW").trim();
 			System.out.println("Writing to " + newSpiceName);
 			FileWriter f2 = new FileWriter(new File(newSpiceName));
 			BufferedReader b1 = new BufferedReader(f1);
 			BufferedWriter b2 = new BufferedWriter(f2);
 			String s;
 			while((s = b1.readLine()) != null) {
 				//System.out.println(s);
 				if (s.toUpperCase().contains("RUNSCRIPT")) {
 					int i1 = s.toUpperCase().indexOf("RUNSCRIPT") + 10;
 					while (!"0123456789".contains(s.substring(i1, i1+1)))
 						i1++;
 					int i2 = i1;
 					while ("0123456789".contains(s.substring(i2, i2+1)))
 						i2++;
 					String num = s.substring(i1,i2);
 					String newNum = namePairs.get(num);
 					System.out.println("OLD:"+s);
 					s = s.substring(0,i1) + newNum + s.substring(i2,s.length());
 					System.out.println("NEW:"+s);
 					
 				}
 				b2.write(s + "\n");
 			}
 			b1.close();
 			b2.flush();
 			b2.close();
 			f1.close();
 			f2.close();
 		}
 		catch (Exception e) {
 			System.out.println("Cannot write to " + newSpiceName);
 		}
 	}
 	
 	void copyFile(String name1, String name2) {
 		try {
 			FileReader f1 = new FileReader(name1);
 			FileWriter f2 = new FileWriter(name2);
 			BufferedReader b1 = new BufferedReader(f1);
 			BufferedWriter b2 = new BufferedWriter(f2);
 			String s;
 			while((s = b1.readLine()) != null) {
 				b2.write(s + "\n");
 			}
 			b1.close();
 			b2.flush();
 			b2.close();
 			f1.close();
 			f2.close();
 		}
 		catch (Exception e) {
 			System.out.println("Cannot copy scripts.");
 		}
 	}
 	
 	String getOutFileName(int num) {
 		return "F" + Integer.toString(num) + ".sct";
 	}
 	
 	void saveToFile() {
 		String outFileName = getOutFileName(buttonStartNum);
 		try {
 			File f = new File(outputPath + File.separator + outFileName);
 			BufferedWriter writer = new BufferedWriter(new FileWriter(f));
 			// for Windows platform, change end-of-line to CR LF
 			writer.write(formatTitleBox(buttonStartNum,0).replaceAll("(\n)", "\r\n"));
 			writer.write(output.replaceAll("(\n)", "\r\n"));
 			writer.flush();
 			writer.close();
 			System.out.println("Written to:" + outputPath + File.separator + outFileName + "\n");
 		} catch (IOException e) {
 			System.out.println("Cannot write output!");
 			e.printStackTrace();
 		}	
 		
 	}
 	
 	void splitFile() {
 		int num = buttonStartNum;
 		try {
 			BufferedReader b1 = new BufferedReader(new StringReader(output));
 			while (true) {
 				String outFileName = getOutFileName(num);
 				File f = new File(outputPath + File.separator + outFileName);
 				BufferedWriter b2 = new BufferedWriter(new FileWriter(f));
 				b2.append(formatTitleBox(num,num-buttonStartNum+1).replaceAll("(\n)", "\r\n"));
 				if (num > buttonStartNum)
 					b2.append("\tSTOP\r\n");
 				String s;
 				while((s = b1.readLine()) != null) {
 					if (s.trim().startsWith("STOP")) {
 						b2.append("\tShow Next\r\n");
 						num++;
 						break;
 					}
 					else
 						b2.append(s+"\r\n");
 				}
 				b2.flush();
 				b2.close();
 				System.out.println(outputPath + File.separator + outFileName + "\n");
 				if (s == null)
 					break;
 			}
 			b1.close();
 		} 
 		catch (IOException e) {
 				System.out.println("Cannot write output!");
 				e.printStackTrace();
 		}	
 	}
 	
 	String formatTitleBox(int num, int subNum) {
 		SimpleDateFormat df = new SimpleDateFormat("E dd, MMM yyyy HH:mm");
 		String today = df.format(new Date());
 		String subNumStr;
 		if (subNum == 0)
 			subNumStr = "";
 		else
 			subNumStr = " - section " + Integer.toString(subNum);
 		return
 		";----------------------------------------------------------------------\n" +
 				"; Script number = F" + Integer.toString(num) + "\n" +
 				"; Title         = " + spiceScript.title + subNumStr + "\n" +
 				"; Color         = \"255,255,0\"\n" +
 				"; Created on    : " + today + "\n" +
 				"; Modified      : " + today + "\n" +
 				"; Version       : 2.1.2\n" +
 				"; Created by    : \n" +
 				"; Keywords      : \n" +
 				"; Description   : \n" +
 				";----------------------------------------------------------------------\n";		
 	}
 	
 	String formatWaitTime(double t) {
 		//return String.format("'\tWait %d minutes %4.2f seconds\n",(int)Math.floor(t/60),t-(Math.floor(t/60))*60) + String.format("+%4.2f\n", t);
 		return String.format("+%4.2f\n", t);
 	}
 	
 	public String getSoundFileName() {
 		String path = globalConf.getParam("COMMON", "SOUND_PATH");
 		if (path.length() == 0)
 			path = "SndPath\\DigitizedAudio\\";
 		String fname = globalConf.getParam("COMMON", "SOUND_FILE");
 		if (fname.length() == 0)
 			fname = spiceScript.title + ".ac3";
 		return path + fname;
 	}
 	
 	int convert() {
 		dm = new DeviceManager();
 		dm.initDevices();
 		String executionList = "VSRC ANIM PROJ VPRJ INTER ASKY";
 		String FS = System.getProperty("file.separator");
 		String SoundFileName = getSoundFileName();
 		allCmds = new ArrayList<DsCmd>();
 		allCmds.add(new DsCmd(0,	";======================================================================\n" + 
 									"\t;PUT ADDITIONAL \"Text Add\" HERE. MAY NEED SOME DELAY AFTER THAT.\n" +
 									"\t;----------------------------------------------------------------------\n" +
 									"\t Jbox1 Add \"audio\" \""+ SoundFileName +"\" \n" +
 									"\t Jbox1 Volume \"audio\" 0 100 \n" +
 									"+0.2\n" +
 									"\t;======================================================================\n\n\n",0));
 		Iterator<SpiceCmd> itr = spiceScript.commands.iterator();
 		boolean flag = false;
 	    while(itr.hasNext()) {
 	    	SpiceCmd c = itr.next();
 			
 	    	//for debug
 			System.out.println(String.format("sec=%d t=%d : %s", c.sectionNum, c.timeBegin, c.wholeLine));
 
 			c.translate();
 	    	if (c.type == SpiceCmdTypes.RUNSCRIPT) {
 	    		String scriptId = c.numericParam;
 	    		int scriptSection = c.sectionNum;
 	    		int loadPoint = c.timeBegin;
 	    		int tapePoint = c.currentTapeValue;
 	    		boolean tapeRunning = c.currentTapeRunning;
 	    		boolean recursive = true;
 	    		while (recursive) {
 		    		DsScript s = ds.get(DsScript.numberToId(scriptId));
 		    		if (s == null) {
 		    			System.out.println("Script not found: " + scriptId);
 		    			return 1;
 		    		}
 		    		int curSec = s.currentSection;
 		    		int totalSec = s.sectionNum;
 		    		allCmds.add(new DsCmd(loadPoint,	";======================================================================\n" + 
 		    											String.format("\t;BEGIN OF %s part %d/%d\n", scriptId, curSec+1, totalSec+1) +
 		    											"\t;----------------------------------------------------------------------\n", scriptSection));
 		    		allCmds.addAll(s.runNextAt(loadPoint,scriptSection));
 		    		DsCmd last = allCmds.get(allCmds.size()-1);
 		    		allCmds.add(new DsCmd(last.timeBegin,
 		    											";----------------------------------------------------------------------\n" + 
 		    											String.format("\t;END OF %s part %d/%d\n", scriptId,  curSec+1, totalSec+1) +
 														"\t;======================================================================\n", scriptSection));
 		    		if (s.loadNextButton) {
 		    			scriptId = Integer.toString(Integer.parseInt(scriptId)+1);
 		    			loadPoint = s.loadTime;
 		    		}
 		    		else
 		    			recursive = false;
 	    		}
 	    		flag = true; // just finished a RunScript call
 	    	}
 	    	else if (c.type == SpiceCmdTypes.OTHER &&
 	    				executionList.contains(c.deviceName.trim().toUpperCase()) )  
     		{
     			dm.resetEquivCmds();
     			DeviceManager.equivCmds.add(new DsCmd(c.sectionNum, c.timeBegin, "", "", DsCmdTypes.COMMENT, 
     					String.format("'###EXECUTED: %s\n", c.wholeLine)));
     			dm.executeCommand(c);
     			allCmds.addAll(DeviceManager.equivCmds);
     		}
 	    	else {
 	    		if (flag) {
 	    			if (c.dsEquiv.type == DsCmdTypes.COMMENT || c.dsEquiv.type == DsCmdTypes.EMPTY || c.dsEquiv.type == DsCmdTypes.SPICESTOP) {
 	    				DsCmd last = allCmds.get(allCmds.size()-1);
 	    				c.dsEquiv.setExecTime(last.timeBegin);	// force comments follow RunScript
 	    				c.dsEquiv.currentTapeValue = -1;		// force tape info to be updated
 	    			}
 	    			else
 	    				flag = false;
 	    		}
 	    		allCmds.add(c.dsEquiv);
 	    	}
 	    }
 
 	    
 	    // set order
 	    setOrder(allCmds);
 
 	    // sort by time
 	    sortByTime(allCmds);
 	    
 	    // adjust tapeValue
 	    adjustTapeValue(allCmds);
 
 	    // adjust timeBegin for cmds
 		ArrayList<DsCmd> dsOnlyCmds = new ArrayList<DsCmd>();
 	    for (int k=0; k<allCmds.size(); k++) {
 	    	DsCmd c = allCmds.get(k);
 	    	if (c.isNative)
 	    		dsOnlyCmds.add(c);
 	    }
 	    adjustTime(dsOnlyCmds);
 	    
 	    // sort by time
 	    sortByTime(allCmds);
 	    
 	    // adjust tapeValue
 	    for (int k=0; k<dsOnlyCmds.size(); k++) {
 	    	DsCmd c = dsOnlyCmds.get(k);
 	    	c.currentTapeValue = -1;	// reset to -1 then fix again
 	    }
 	    adjustTapeValue(allCmds);
 
 	    // generate output
 	    output = generateOutput(allCmds);
 	    return 0;
 	}
 	
 	public String generateOutput(ArrayList<DsCmd> list) {
 		Iterator<DsCmd> it = list.iterator();
 	    int oldTime = 0;
 		StringBuilder sb = new StringBuilder();
 		double queue = 0;
 	    while(it.hasNext()) {
 	    	DsCmd c = it.next();
 	    	if (c.type == DsCmdTypes.SPICESTOP)	// the STOP command was assigned a large exec time to make sure it happens at the end of block
 	    		c.timeBegin = oldTime;
 	    	double timeDiff = (c.timeBegin - oldTime);
 	    	if (timeDiff + queue > 0) {
 	    		if (c.type != DsCmdTypes.COMMENT && c.type != DsCmdTypes.EMPTY) {
 	    			timeDiff += queue;
 		    		timeDiff /= 100;
 		    		sb.append(formatWaitTime(timeDiff));
 			    	queue = 0;
 	    		}
 			    else
 			    	queue += timeDiff;
 	    	}
 
 	    	if (c.type != DsCmdTypes.WAIT) { // Will not output line with delay only
 	    		//uncomment this line to debug
 	    		sb.append(String.format("\t\t\t\t\t\t\t'BLOCK = %2d  t = %6d  # = %2d  iCLK = %s  TAPE = %s  RUN = %s\n",
 	    				c.superOrder, c.timeBegin, c.order,
 	    				SpiceScript.timeString(c.timeBegin), SpiceScript.timeString(c.currentTapeValue), (c.currentTapeRunning?"Y":"N")));
 	    		String finalText;
 	    		if (c.waitTime.length() != 0)
 	    			finalText = c.removeDelay().trim();
 	    		else
 	    			finalText = c.wholeLine.trim();
     			sb.append("\t" + (c.isNative ? "" : "\t\t") + finalText + "\n");
 	    	}
 	    	oldTime = c.timeBegin;
 	    }
 	    if (queue > 0)
 	    	sb.append(formatWaitTime(queue));
 	    return sb.toString();
 	}
 	
 	public ArrayList<DsCmd> cutSequence(int fromTime, int toTime, ArrayList<DsCmd> list) {
 		ArrayList<DsCmd> newList = new ArrayList<DsCmd>();
 		HashMap<String,Integer> bag = new HashMap<String,Integer>();
 		if (list.size() == 0)
 			return newList;
 		boolean flag = false;
 		DsCmd lastCmd = null;
 		for (int i=list.size()-1; i>0; i--) {
 	    	DsCmd c = list.get(i);
 	    	int currentAudio = c.currentTapeValue - c.currentTapeAudioDiff;
 	    	if (!flag && fromTime <= currentAudio && currentAudio <= toTime)
 	    		flag = true;
 	    	if (flag && currentAudio < fromTime)
 	    		flag = false;
     		if (flag || c.type == DsCmdTypes.SPICESTOP) {
     			newList.add(c);
    			if (flag)
    				lastCmd = c;
     		}
 	    	if (c.category.trim().startsWith("TEXT") && (flag || !bag.isEmpty())) {
     			String s = c.removeDelay().trim();
     			String pos[] = s.split("\\s");
     			String objName = pos[2];
     			while (objName.contains("\""))
     				objName = objName.replace("\"", "");
	    		if (c.action.trim().startsWith("VIEW") || c.action.trim().startsWith("PLAY")) {
 	    			if (flag && !bag.containsKey(objName))
 	    				bag.put(objName, 1);
 	    		}
	    		else if (c.action.trim().startsWith("LOCATE")) {
     				Integer f = bag.get(objName);
     				if (f != null) {
    					if (!flag && ((f & 2) == 0))	// first LOCATE on the way up
     						newList.add(c);
     					f = (f | 2);
     				}
     			}
	    		else if (c.action.trim().startsWith("ADD")) {
     				Integer f = bag.get(objName);
     				if (f != null && !flag) {
     					newList.add(c);
     					bag.remove(objName);
     				}
     			}
 	    	}
 		}
 		int insTime;
 		newList.add(DsCmd.cmdJboxAdd(0, 0, getSoundFileName()));
 		newList.add(DsCmd.cmdJboxVol(0, 100, 100));
 		if (lastCmd != null && lastCmd.currentTapeRunning) {
 			insTime = lastCmd.timeBegin - (lastCmd.currentTapeValue - lastCmd.currentTapeAudioDiff - fromTime);
 			newList.add(DsCmd.cmdJboxGoto(lastCmd.superOrder, insTime - 100, fromTime));
 			newList.add(DsCmd.cmdJboxPlay(lastCmd.superOrder, insTime)); 
 		}
 		else {
 			insTime = lastCmd.timeBegin;
 			newList.add(DsCmd.cmdJboxGoto(lastCmd.superOrder, insTime - 100, lastCmd.currentTapeValue - lastCmd.currentTapeAudioDiff));
 		}
 		sortByTime(newList);
 		return newList;
 	}
 
 	private void sortByTime(ArrayList<DsCmd> list) {
 	    Collections.sort(list);
 	}
 	
 	private void setOrder(ArrayList<DsCmd> list) {
 	    for (int k=0; k<list.size(); k++) {
 	    	DsCmd c = list.get(k);
 	    	c.setOrder(k);
 	    }
 	}
 	
 	private void adjustTapeValue(ArrayList<DsCmd> list) {
 	    int lastTapeValue = 0;
 	    boolean lastTapeRunning = false;
 	    int lastTimeBegin = 0;
 	    int lastDiff = 0;
 	    // now adjust tapeValue
 	    for (int k=0; k<list.size(); k++) {
 	    	DsCmd c = list.get(k);
 	    	int currentTimeBegin = c.timeBegin;
 	    	int timeSinceLastCmd = currentTimeBegin - lastTimeBegin;
 	    	if (c.currentTapeValue == -1) {	// need to fix
 	    		c.currentTapeRunning = lastTapeRunning;	// if the tapeValue is -1, this cmd must be DS-native, so it cannot change tape status
 	    		c.currentTapeAudioDiff = lastDiff;
 	    		if (lastTapeRunning)
 	    			c.currentTapeValue = lastTapeValue + timeSinceLastCmd;
 	    		else
 	    			c.currentTapeValue = lastTapeValue;
 	    		lastTapeValue = c.currentTapeValue;	// update lastTapeValue for next use
 	    	}
 	    	else {
 	    		lastTapeRunning = c.currentTapeRunning; // this is SPICE-native cmd, we trust the tape status stored in it
 	    		lastTapeValue = c.currentTapeValue;
 	    		lastDiff = c.currentTapeAudioDiff;
 	    	}
 	    	lastTimeBegin = c.timeBegin;
 	    }
 	}
 	
 	private void adjustTime(ArrayList<DsCmd> list) {
 	    // now adjust time
 	    int delta = 0;
 	    int prevExecTime = 0;
 	    int kExec = -1;
 	    int kFlex = -1;
 	    for (int k=0; k<list.size(); k++) {
 	    	DsCmd c = list.get(k);
 			if (delta != 0) {
 				c.addToExecTime(delta);
 				if (c.currentTapeRunning)
 					c.addToCurrentTapeValue(delta);
 			}
 	    	
 			if (c.type == DsCmdTypes.COMMENT || c.type == DsCmdTypes.EMPTY) {	// merge gaps between COMMENTs to give a chance to reduce gap later
 				int localDelta = c.timeBegin - prevExecTime;
 				if (localDelta > 0) {
 					c.addToExecTime(-localDelta);
 					if (c.currentTapeRunning)
 						c.addToCurrentTapeValue(-localDelta);
 				}
 	    	}
 	    	
 			if (c.type == DsCmdTypes.TIME_ADJUST) {
 				int timeTarget = SpiceScript.timeValue(c.action) + c.currentTapeAudioDiff;
 				int localDelta = timeTarget - c.currentTapeValue;
 				if (localDelta < 0) {
 					if (c.timeBegin + localDelta >= prevExecTime) {
 						c.wholeLine += String.format(" TARGET: %s, OLD TAPE VALUE: %s, REDUCED: %.2f\n", 
 								SpiceScript.timeString(timeTarget), 
 								SpiceScript.timeString(c.currentTapeValue), -((double)localDelta)/100);
 						delta += localDelta;
 						c.addToExecTime(localDelta);
 						if (c.currentTapeRunning)
 							c.addToCurrentTapeValue(localDelta);
 					}
 					else { // roll back to the last TIME_FLEX
 						int gap = (kFlex > 0 ? (list.get(kFlex).timeBegin - list.get(kFlex-1).timeBegin) : -1);
 						if (gap + localDelta >= 0) {
 							list.get(kFlex).wholeLine += String.format(" OLD GAP: %.2f, REDUCED: %.2f\n", 
 									((double)gap)/100, -((double)localDelta)/100);
 							c.wholeLine += String.format("  TARGET: %s, OLD TAPE VALUE: %s, REDUCED: %.2f, using TIME_FLEX\n",
 									SpiceScript.timeString(timeTarget), 
 									SpiceScript.timeString(c.currentTapeValue), -((double)localDelta)/100);
 							delta += localDelta;
 							for (int j=kFlex; j<=k; j++) {
 								DsCmd cmd = list.get(j);
 								cmd.addToExecTime(localDelta);
 								if (cmd.currentTapeRunning)
 									cmd.addToCurrentTapeValue(localDelta);
 							}
 						}
 						else
 							c.wholeLine += String.format("  TARGET: %s, OLD TAPE VALUE: %s, **FAILED** TO ADD: %.2f\n",
 									SpiceScript.timeString(timeTarget), 
 									SpiceScript.timeString(c.currentTapeValue), ((double)localDelta)/100);
 					}
 				}
 				else if (localDelta > 0) {
 					c.wholeLine += String.format("  TARGET: %s, OLD TAPE VALUE: %s, ADDED: %.2f\n",
 							SpiceScript.timeString(timeTarget), 
 							SpiceScript.timeString(c.currentTapeValue), ((double)localDelta)/100);
 					c.addToExecTime(localDelta);
 					if (c.currentTapeRunning)
 						c.addToCurrentTapeValue(localDelta);
 					delta += localDelta;
 				}
 			}
 			if (c.type == DsCmdTypes.TIME_FLEX) {
 				kFlex = k;
 			}
 			// adjust only cmds before a STOP
 			// reset all when hit a STOP
 	    	if (c.type == DsCmdTypes.SPICESTOP) {
 	    		delta = 0;
 	    		prevExecTime = 0;
 	    		kFlex = -1;
 	    	}
 	    	else
 	    		prevExecTime = c.timeBegin;
 	    }
 	}
 	
 }
 
 class DSFileFilter implements FilenameFilter {
 
 	@Override
 	public boolean accept(File f, String name) {
 		return name.toLowerCase().endsWith(".sct");
 
 	}
 
 }
 
 class ScriptTableModel extends AbstractTableModel {
 	String[] columnNames = {
 			"ID",
 			"File name",
 			"Used",
 			"Button name",
             "Button page",
             "Modified",
             "STOPs / ShowNext",
             "Commands"};
 	Object[][] data = {{"","","","","","","",""}};
 
     public int getColumnCount() {
         return columnNames.length;
     }
 
     public int getRowCount() {
         return data.length;
     }
 
     public String getColumnName(int col) {
         return columnNames[col];
     }
 
     public Object getValueAt(int row, int col) {
     	if (0<=row && row<=getRowCount() && 0<=col && col <=getColumnCount())
     		return data[row][col];
     	else
     		return null;
     }
 
     public Class<?> getColumnClass(int c) {
     	if (getValueAt(0, c) == null)
     		return Object.class;
     	else
     		return getValueAt(0, c).getClass();
     }
 
     public boolean isCellEditable(int row, int col) {
         return false;
     }
 
     public void setValueAt(Object value, int row, int col) {
     	if (0<=row && row<=getRowCount() && 0<=col && col <=getColumnCount()) {
     		data[row][col] = value;
     		fireTableCellUpdated(row, col);
     	}
     }
 
     public void setValueQuietly(Object value, int row, int col) {
    		data[row][col] = value;
     }
 }
