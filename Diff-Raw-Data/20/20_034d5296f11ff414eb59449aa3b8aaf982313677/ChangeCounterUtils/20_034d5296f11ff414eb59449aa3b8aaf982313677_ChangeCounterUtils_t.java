 package pcc.analysis;
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.LinkedList;
 import java.util.Vector;
 
 import pcc.io.IOUtils;
 import pcc.test.integration.IOUtilsStub;
 import pcc.vercon.ProjectVersion;
 import pcc.vercon.SourceFileRecord;
 
 
 public class ChangeCounterUtils{
 	private static LinkedList<Change> getChanges(String[] old, String[] cur){
 		//TODO make more efficient
 		
 		//Count duplicate lines in both files
 		//line -> instances
 		HashMap<String, LinkedList<Integer>> oldLines = new HashMap<String, LinkedList<Integer>>();
 		HashMap<String, LinkedList<Integer>> curLines = new HashMap<String, LinkedList<Integer>>();
 		LinkedList<String> uniqueLines = new LinkedList<String>();
 		int i = 0;
 		for(String line:old){
 			LinkedList<Integer> instances = oldLines.get(line);
 			if(instances==null){
 				instances = new LinkedList<Integer>();
 				instances.add(i);
 				uniqueLines.add(line);
 				oldLines.put(line, instances);
 			}else
 				instances.add(i);
 			i++;
 		}
 		i=0;
 		for(String line:cur){
 			LinkedList<Integer> instances = curLines.get(line);
 			if(instances==null){
 				instances = new LinkedList<Integer>();
 				instances.add(i);
 				if(!oldLines.containsKey(line))
 					uniqueLines.add(line);
 				curLines.put(line, instances);
 			}else
 				instances.add(i);
 			i++;
 		}
 		
 		//count changes
 		LinkedList<Change> added = new LinkedList<Change>();
 		Vector<Change> removed = new Vector<Change>();
 		for(String line:uniqueLines){
 			LinkedList<Integer> oldInstances = oldLines.get(line);
 			LinkedList<Integer> curInstances = curLines.get(line);
 			if(oldInstances==null)
 				oldInstances = new LinkedList<Integer>();
 			if(curInstances==null)
 				curInstances = new LinkedList<Integer>();
 			//count removed changes
 			//TODO: make more efficient
 			while(oldInstances.size()>curInstances.size()){
 				//find the old instance that is farthest from any current instance
 				i = getFurthestInstance(oldInstances, curInstances);
 				int lineNumber = oldInstances.remove(i);
 				removed.add(new Change(ChangeType.REMOVED, line, lineNumber));
 			}
 			//count added changes
 			//TODO: make more efficient
 			while(oldInstances.size()<curInstances.size()){
 				//find the old instance that is farthest from any current instance
 				i = getFurthestInstance(curInstances, oldInstances);
 				int lineNumber = curInstances.remove(i);
 				added.add(new Change(ChangeType.ADDED, line, lineNumber));
 			}
 		}
 		
 		//combine added & removed change pairs with similar strings
 		final double MAX_DIFF = .8;
 		LinkedList<Change> changes = new LinkedList<Change>();
 	
 		for(int a = 0; a < added.size(); a++){
 			Change addition = added.get(a);
 			if(removed.isEmpty())
 				break;
 			int best = 0;
 			double bestDiff =  StringDistanceUtil.getDiff(
 					removed.get(0).line, addition.line);
 			for(i = 1; i < removed.size(); i++){
 				double diff = StringDistanceUtil.getDiff(
 						removed.get(i).line, addition.line);
 				if(diff<bestDiff){
 					best = i;
 					bestDiff = diff;
 				}
 			}
 			if(bestDiff<MAX_DIFF){
 				changes.add(new Change(ChangeType.CHANGED, addition.line, addition.lineNumber));
 				removed.remove(best);
 				added.remove(a--);
 			}
 		}
 		for(Change change:added)
 			changes.add(change);
 		for(Change change:removed)
 			changes.add(change);
 		
 		return changes;
 	}
 	
 	//find the instance in listA that is farthest from any instance in listB
 	private static int getFurthestInstance(LinkedList<Integer> listA, LinkedList<Integer> listB){
 		int i = 0, minDist, maxDist=0, maxIndex=-1,dist;
 		for(Integer a:listA){
 			//find the instance in listB that is closest to 'a'
 			minDist = Integer.MAX_VALUE;
 			for(Integer b:listB){
 				dist = Math.abs(a-b);
 				if(dist<minDist){
 					minDist = dist;
 				}
 			}
 			//if the instance of 'a' that is farthest from any instance in listB
 			if(minDist>maxDist){
 				maxDist = minDist;
 				maxIndex = i;
 			}
 			i++;
 		}
 		return maxIndex;
 	}
 	
 	private static int countLLOCType(LinkedList<Change> changes, ChangeType type){
 		//TODO make more efficient
 		int count = 0;
 		for(Change change:changes){
 			if(isLLOC(change.line)){
 				if(change.type==type)
 					count++;
 			}
 		}
 		return count;
 	}
 	private static void exportChangeLabels(String dir, String version1Meta, String version2Meta, String[] old, String[] cur, String labelFileName) throws IOException{
 		LinkedList<Change> changes = getChanges(old,cur);
 		StringBuilder output = new StringBuilder();
 		int added = countLLOCType(changes, ChangeType.ADDED);
 		int removed = countLLOCType(changes, ChangeType.REMOVED);
 		int changed = countLLOCType(changes, ChangeType.CHANGED);
 		
 		//write change label header
 		output.append("/**\n");
 		output.append(" * From: "+version1Meta+"\n");
 		output.append(" * To:   "+version2Meta+"\n");
 		output.append(" * Added LLOC:   " + added +"\n");
 		output.append(" * Removed LLOC: " + removed +"\n");
 		output.append(" * Changed LLOC: " + changed +"\n");
 		output.append(" **/\n");
 		
 		//sort changes by line number
 		Collections.sort(changes);
 		
 		//write each line & change label to the output
 		Iterator<Change> iterator = changes.iterator();
 		Change currentChange = null;
 		for(int i = 0; i < cur.length || iterator.hasNext();){
 			if(iterator.hasNext() && currentChange==null)
 				currentChange = iterator.next();
 			//write changed lines
 			if(currentChange!=null && currentChange.lineNumber<=i){
 				output.append(currentChange.getLineLabel()+"\n");
 				//skip the next line from cur if it was an added line
 				if(currentChange.type==ChangeType.ADDED ||
 						currentChange.type==ChangeType.CHANGED)
 					i++;
 				currentChange = null;
 			}
 			//write unchanged lines
 			else if(i<cur.length){
 				
 				output.append(cur[i]+"\n");
 				i++;
 			}
 		}
 		IOUtils.writeSourceFile(dir, labelFileName, output.toString());
 	}
 	public static void exportChangeLabels(String toDirName, ProjectVersion v1, ProjectVersion v2) throws IOException{
 		IOUtils.createFolder(toDirName);
 		IOUtils.writeSourceFile(toDirName,"FILE_CHANGES.txt", getLLOCChanges(v1,v2,false));
 		LinkedList<String> v1Files = getFileNames(v1);
 		LinkedList<String> v2Files = getFileNames(v2);
 		for(String file:v1Files){
 			if(v2Files.remove(file)){
 				SourceFileRecord v1File = v1.getFile(file);
 				SourceFileRecord v2File = v2.getFile(file);
 				exportChangeLabels(toDirName, v1.getMetaData(), v2.getMetaData(),
 						v1File.getLines(),v2File.getLines(), toDirName+File.separator+file);
 			}
 		}
 	}
 	private static String getLineChangesReport(SourceFileRecord f1, SourceFileRecord f2){
 		LinkedList<Change> changes = getChanges(f1.getLines(), f2.getLines());
 		StringBuilder output = new StringBuilder();
 		int added = countLLOCType(changes, ChangeType.ADDED);
 		int removed = countLLOCType(changes, ChangeType.REMOVED);
 		int changed = countLLOCType(changes, ChangeType.CHANGED);
 		output.append("### "+f1.getName()+": "+added + "LLOC added, "+removed+"LLOC removed, "+changed+"LLOC changed ###<br>");
 		for(Change change:changes){
 			if(isLLOC(change.line))
 				output.append(change.toString()+"<br>");
 		}
 		return output.toString();
 	}
 	public static String getLineChangesReport(ProjectVersion v1, ProjectVersion v2){
 		StringBuilder output = new StringBuilder();
 		LinkedList<String> v1Files = getFileNames(v1);
 		LinkedList<String> v2Files = getFileNames(v2);
 		for(String file:v1Files){
 			if(v2Files.remove(file)){
 				SourceFileRecord v1File = v1.getFile(file);
 				SourceFileRecord v2File = v2.getFile(file);
 				output.append(getLineChangesReport(v1File,v2File));
 			}
 			else
 				output.append("### "+ file + ": file removed ###<br>");
 		}
 		for(String file:v2Files)
 			output.append("### "+ file+": file added ###<br>");
 		return output.toString().trim();
 	}
 	private static boolean isLLOC(String line){
 		return line.trim().length()>0;
 	}
 	private static int getLLOC(String[] lines){
 		int count = 0;
 		for(String line:lines){
 			if(isLLOC(line))
 				count++;
 		}
 		return count;
 	}
 	public static int getLLOC(ProjectVersion version){
 		int count = 0;
 		for(SourceFileRecord file:version.getFiles())
 			count+=getLLOC(file.getLines());
 		return count;
 	}
 	
 	private static LinkedList<String> getFileNames(ProjectVersion v){
 		ArrayList<SourceFileRecord> files = v.getFiles();
 		LinkedList<String> names = new LinkedList<String>();
 		for(SourceFileRecord file:files)
 			names.add(file.getName());
 		return names;
 	}
 	//returns a report of the file changes between two versions
	private static String getFileChanges(ProjectVersion v1, ProjectVersion v2, boolean html){
		String lineEnd = html?"<br>":"\n";
 		StringBuilder output = new StringBuilder();
 		LinkedList<String> v1Files = getFileNames(v1);
 		LinkedList<String> v2Files = getFileNames(v2);
 		int totalAdded = 0;
 		int totalRemoved = 0;
 		int totalChanged = 0;
 		for(String file:v1Files){
 			output.append(file + ": ");
 			if(v2Files.remove(file)){
 				SourceFileRecord v1File = v1.getFile(file);
 				SourceFileRecord v2File = v2.getFile(file);
 				
 				LinkedList<Change> changes = getChanges(v1File.getLines(), v2File.getLines());
 				int added = countLLOCType(changes, ChangeType.ADDED);
 				int removed = countLLOCType(changes, ChangeType.REMOVED);
 				int changed = countLLOCType(changes, ChangeType.CHANGED);
 				totalAdded+=added;
 				totalRemoved+=removed;
 				totalChanged+=changed;
				output.append(added + "LLOC added, "+removed+"LLOC removed, "+changed+"LLOC changed"+lineEnd);
 			}
 			else{
 				int loc = getLLOC(v1.getFile(file).getLines());
 				totalRemoved+=loc;
				output.append("file removed, " +loc+"LLOC"+lineEnd);
 			}
 		}
 		for(String file:v2Files){
 			int loc = getLLOC(v2.getFile(file).getLines());
 			totalAdded+=loc;
			output.append(file+": file added, "+loc+"LLOC"+lineEnd);
 		}
 		//write total added and removed
		output.append("Total LLOC Added:   " + totalAdded + lineEnd);
		output.append("Total LLOC Removed: " + totalRemoved+ lineEnd);
 		output.append("Total LLOC Changed: " + totalChanged);
 		return output.toString();
 	}
 	public static String getLLOCChanges(ProjectVersion v1, ProjectVersion v2, boolean html){
 		String newLine = html?"<br>":"\n";
 		StringBuilder output = new StringBuilder();
 		output.append("Version "+v1.getNumber()+": "+getLLOC(v1)+"LLOC"+newLine);
 		output.append("Version "+v2.getNumber()+": "+getLLOC(v2)+"LLOC"+newLine);
		output.append(getFileChanges(v1,v2,html));
 		return output.toString();
 	}
 }
