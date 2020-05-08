 package util;
 
  
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.RandomAccessFile;
 import java.util.ArrayList;
 
 import util.Condition;
 import util.HeapFile;
 import util.Record;
 import compare.Comparer;
 
 public class Query {
 	
 	public HeapFile heapFile;
 	private String[] args;
 	public ArrayList<ArrayList<Condition>> dummyRecord;
 	private int argIndex;
 	public ArrayList<String> projectionList = new ArrayList<String>();
 	public ArrayList<Integer> matchingRecords;
 	public ArrayList<Long> hashRecords;
 	
 	public Query(HeapFile inputHeap, String[] arguments) {
 		this.heapFile = inputHeap;
 		this.args = arguments;		
 	}
 
 	public void processQuery (){
 		// check if there is a query in the command line
 		int hasQuery = this.hasQuery();
 		// if not, program terminates
 		if(hasQuery == 0){
 			return;
 		}
 		this.dummyRecord = new ArrayList<ArrayList<Condition>>();
 		this.projectionList = new ArrayList<String>();
		this.argIndex = 1;
 		this.addConditions();
		this.argIndex = 1;
 		this.addProjections();
 		this.findMatchingRecords();
 		Output output = new Output(this);
 	}
 	private int hasQuery() {
 		// scan the arguments to see if there is a condition or projection
 		// if there is one
 		int x = 1;
 		while(x < this.args.length){
 			if(args[x].contains("-s") || args[x].contains("-p")){
 				return 1;
 			}
 			x++;
 		}
 		return 0;
 	}
 
 	private void addConditions(){
 
 		int argCount = 0;
 		int multiCondition = 0;
 	
 		ArrayList<Condition> first = new ArrayList<Condition>();
 		this.dummyRecord.add(first);
 		
 		// traverse command line and add condition(s) to conditionList
 		while(this.argIndex <= (args.length - 1)){
 			// if argument contains an s, then we create a new condition, 
 			//and advance 3 spots in the index
 			if(this.args[this.argIndex].contains("s")){
 				argCount++;
 				int columnNumber = Integer.parseInt(this.args[this.argIndex].substring(2));
 				// check that this is a valid column
 				if(columnNumber > heapFile.numberOfFields  || columnNumber == 0){
 					System.out.println("Sorry. That column for query does not exist.");
 					return;
 				}
 				
 				// if this isnt the first condition added, and this condition is equal to the previous
 				// condition, then it is a multicondition. so we added it to the arraylist that already
 				// exists for that column number
 				if(argCount > 1 && this.args[this.argIndex].equals(this.args[this.argIndex - 3])){
 					Condition condition = new Condition(this.args[this.argIndex], this.args[this.argIndex + 1], this.args[this.argIndex + 2], columnNumber);
 					//	multiList.add(condition);
 					multiCondition++;
 					if(multiCondition >= dummyRecord.size()){
 						ArrayList<Condition> next = new ArrayList<Condition>();
 						dummyRecord.add(next);
 						dummyRecord.get(multiCondition).add(condition);
 					}
 					else{
 						dummyRecord.get(multiCondition).add(condition);
 					}
 					this.argIndex = this.argIndex + 3;
 
 				}
 				// this is a totally new column to be queried
 				else{
 					multiCondition = 0;
 					Condition condition = new Condition(this.args[this.argIndex], this.args[this.argIndex + 1], this.args[this.argIndex + 2], columnNumber);
 					dummyRecord.get(multiCondition).add(condition);
 					this.argIndex = this.argIndex + 3;
 
 				}
 			}
 			else
 			   argIndex++;
 		
 	}
 		
 		
 	}
 	
 	
 	private void addProjections() {
		
 		// if argument contains a p, as in -p1, add this arg to projections and advance to next index
		while(this.argIndex <= (this.args.length - 1) && this.args[this.argIndex].contains("p")){
 			int columnNumber = Integer.parseInt(this.args[this.argIndex].substring(2));
 			if(columnNumber > heapFile.numberOfFields  || columnNumber == 0){
 				System.out.println("Sorry. That column for projection does not exist.");
 				return;
 			}
 			this.projectionList.add(this.args[this.argIndex]);
 			this.argIndex++;
 		}
 	}
 
 
 
 	public void findMatchingRecords() {
 		
 		// this.dummyRecord now is an arraylist of arraylists
 		// each index in dummyrecord corresponds to a different column
 		// each column may have one or more conditions
 		// for example: this.dummyRecord = [0][condition on column 1, different condition on column 1]
 		//                                 [1][condition on column 3]
 		
 		// first. we scan through the first condition in each list to find the column of that list
 		// if this column has a hash index, we see if any of the conditions in that list 
 		// test for equality. if they do, we then use the index file to find the RIDs of the 
 		// value to be tested for equality. 
 		ArrayList<Long> allRIDs = new ArrayList<Long>();   
 		int hashes = 0;
 		int x = 0;
 		int advance = 1;
 		while(x < this.dummyRecord.size()){
 			// get column of this condition list
 				int y = 0;
 				int column = this.dummyRecord.get(x).get(y).column;
 			// check if there is a hash index on this column
 				if(this.heapFile.indexExistsOnColumn(column)){
 					
 					while(y < this.dummyRecord.get(x).size()){
 				// if current condition's parameter is equality, then we get the RIDs for the value
 					String param = this.dummyRecord.get(x).get(y).parameter;
 					if(param.equals("=")){
 						// increase hashes
 						hashes++;
 						// get RIDs
 						ArrayList<Long> equalityRIDs = this.heapFile.getListOfRidsForSelectionCondition(column, this.dummyRecord.get(x).get(y).value);
 						// add RIDs to list
 						int a = 0;
 						while(a < equalityRIDs.size()){
 							allRIDs.add(equalityRIDs.get(a));
 							a++;
 						}
 						// remove this condition from this.dummyRecord
 						this.dummyRecord.get(x).remove(y);
 						// since we removed a condition, we dont want to skip the next condition
 						// the slid down as a result of the delete
 						advance = 0;
 					}
 					if(advance == 1){
 						y++;
 					}
 					advance = 1;
 					
 				}
 				// advance to next condition if there is one
 				
 			}
 			x++;
 		}
 		
 		// if 'hashes' > 1, then we can reduce the RID set by only keeping an RID 
 		// if it appears in the list 'hashes' amount of time
 		if(hashes > 1){							
 			
 			ArrayList<Long> matchRIDs = new ArrayList<Long>();
 			int e = 0;
 			int matchesNeeded = hashes;
 
 			while(e < allRIDs.size()){
 				int f = e + 1;
 				int matched = 0;
 				int matches = 0;
 				while(matched == 0 && f < allRIDs.size()){
 					if(allRIDs.get(e).equals(allRIDs.get(f))){
 						matches++;
 					}
 					if(matches == matchesNeeded){
 						matchRIDs.add(allRIDs.get(e));
 						matched = 1;
 					}
 
 					f++;
 				}
 				e++;
 			}
 			if(matchRIDs.size() == 0){
 				// no matches in the file so we return null
 				this.matchingRecords = null;
 				return;
 			}
 			// else switch allRIDs to the new set of matches
 			allRIDs = matchRIDs;
 			
 		}
 		
 		
 		
 		// if allRIDs.size() > 0, then we only want to compare the rest of the conditions with 
 		if(allRIDs.size() > 0){
 			Comparer comparer1 = new Comparer();
 			this.hashRecords = new ArrayList<Long>();
 		
 		
 			try {
 		
 				RandomAccessFile heap = new RandomAccessFile(new File(this.heapFile.path), "r");
 				Record dummyRec1 = new Record();
 				int[] offsetList = this.heapFile.getOffsetList();
 				int[] lengthList = this.heapFile.getListOfLengths();
 				int z =  0;	
 				// go through each RID in allRIDs
 			while(z < allRIDs.size()){
 				// seek to first record in allRIDs
 				// compare each condition in this.dummyRecord
 				// seek to RID spot
 				heap.seek(allRIDs.get(z));
 				// reach record at this point in heap
 				byte[] heapRec = new byte[this.heapFile.numberOfBytesPerRecord];
 				heap.read(heapRec);
 				
 				/// compare with each condition list in this.dummyrecord
 				int a = 0;
 				int reject = 0;
 				while(a < this.dummyRecord.size()){
 					RandomAccessFile dum = new RandomAccessFile(new File("dummy"), "rw");
 					// write to dummyrec1
 					int[] compareList = new int[this.heapFile.schemaArray.length];
 					compareList = dummyRec1.writeDummyFile(this.dummyRecord.get(a), compareList, this.heapFile);
 					// read the record
 					dum.seek(0);
 					byte[] dumRec = new byte[this.heapFile.numberOfBytesPerRecord];
 					dum.read(dumRec);
 					
 
 					Record results = new Record();
 					int index = 0;
 					int match;
 					int condIndex1 = 0;
 					while(index < compareList.length && reject == 0){
 						int answer;
 						if(compareList[index] == 1){
 							answer = comparer1.compare_functions[this.heapFile.schemaArray[index]].compare(dumRec, offsetList[index], heapRec, offsetList[index],lengthList[index]);
 							match = results.checkCompareResult(this.dummyRecord.get(a).get(condIndex1).operator, answer);
 							if(match == 0){
 								reject = 1;
 							}
 							condIndex1++;
 						}
 
 						index++;
 					}
 					dum.close();
 					File dummy1 = new File("dummy");
 					dummy1.delete();
 					a++;
 			}
 				if(reject == 0){
 					// match found, add to matchRecords list
 					this.hashRecords.add(allRIDs.get(z));
 				}
 				
 				z++;
 			
 		}
 			} // end of try
 			catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} // end of catchers
 			
 			return;
 		}
 		
 			// end of try
 		// if allRIDs.size() == 0 that must mean that we didnt have an index on any of the 
 		// of the columns that the query is conditioning
 		// so we traverse the heapfile as usual
 		if(allRIDs.size() == 0){
 		// below is how we find matching records for a column that doesnt have an index
 		Comparer comparer = new Comparer();
 		this.matchingRecords = new ArrayList<Integer>();
 		int m =  0;
 		int[] offsetList = this.heapFile.getOffsetList();
 
 		//int firstListCheck = 0;
 		while(m < this.dummyRecord.size()){
 
 			int[] compareList = new int[this.heapFile.schemaArray.length];
 			Record dummyRec = new Record();
 
 			compareList = dummyRec.writeDummyFile(this.dummyRecord.get(m), compareList, this.heapFile);
 
 
 			// create RAF to read heapFile
 
 			int[] lengthList = this.heapFile.getListOfLengths();
 			RandomAccessFile dummy;
 			try {
 				dummy = new RandomAccessFile(new File("dummy"), "rw");
 				RandomAccessFile raf1 = new RandomAccessFile(new File(this.heapFile.path), "r");
 
 				int currentRecord = 0;
 
 
 				while(currentRecord < this.heapFile.numberOfRecords){
 
 					raf1.seek(this.heapFile.currentFileOffset + (this.heapFile.numberOfBytesPerRecord * currentRecord));
 					byte[] heapRec = new byte[this.heapFile.numberOfBytesPerRecord];
 					raf1.read(heapRec);
 
 					dummy.seek(0);
 					byte[] dumRec = new byte[this.heapFile.numberOfBytesPerRecord];
 					dummy.read(dumRec);
 
 					Record results = new Record();
 					int index = 0;
 					int reject = 0;
 					int match;
 					int condIndex1 = 0;
 					while(index < compareList.length && reject == 0){
 						int answer;
 						if(compareList[index] == 1){
 							answer = comparer.compare_functions[this.heapFile.schemaArray[index]].compare(dumRec, offsetList[index], heapRec, offsetList[index],lengthList[index]);
 							match = results.checkCompareResult(this.dummyRecord.get(m).get(condIndex1).operator, answer);
 							if(match == 0){
 								reject = 1;
 							}
 							condIndex1++;
 						}
 
 						index++;
 					}
 
 					if(reject == 0){
 						// match found, add to matchRecords list
 						this.matchingRecords.add(currentRecord);
 					}
 
 					currentRecord++;
 				} // end of scanning all records
 
 				dummy.close();
 				File dummy1 = new File("dummy");
 				dummy1.delete();
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} // end of catchers
 			m++;
 		} // end of m > 0 loop
 		
 		return;
 	}
 	}
 		
 }
 
