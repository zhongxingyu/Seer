 package simpledb.materialize;
 
 import simpledb.tx.Transaction;
 import simpledb.record.*;
 import simpledb.query.*;
 
 import java.util.*;
 
 /**
  * The Plan class for the <i>sort</i> operator.
  * @author Edward Sciore
  */
 public class SortPlan implements Plan {
    private Plan p;
    private Transaction tx;
    private Schema sch;
    private RecordComparator comp;
    private int k;
    private int recordsPerBlock = 5;
    private List<String> sortfields;
    
    /**
     * Creates a sort plan for the specified query.
     * @param p the plan for the underlying query
     * @param sortfields the fields to sort by
     * @param tx the calling transaction
     */
 	
 	public SortPlan(Plan p, List<String> sortfields, Transaction tx) {
       this(p, sortfields, tx, 2); // default to 2 runs
 	}
 	
 	public SortPlan(Plan p, List<String> sortfields, Transaction tx, int k) {
 	  this.sortfields = sortfields;
 	  this.k = k;
       this.p = p;
       this.tx = tx;
       sch = p.schema();
       comp = new RecordComparator(sortfields);
 	}
    
    /**
     * This method is where most of the action is.
     * Up to 2 sorted temporary tables are created,
     * and are passed into SortScan for final merging.
     * @see simpledb.query.Plan#open()
     */
    public Scan open() {
       UpdateScan src = (UpdateScan)p.open();
 	  src.beforeFirst();
       List<TempTable> runs = splitIntoRuns(src);
 	  System.out.println("Initial");
 	  System.out.println("-------------");
 	  printContents(runs);
       src.close();
       while (runs.size() > 1){
          runs = doAMergeIteration(runs);
 		 System.out.println("Merge Iteration");
 		 System.out.println("-------------");
 		 printContents(runs);
 	  }
 	  if(runs.size()<1){
 		return null;
 	  }
       return new SortScan(runs, comp);
    }
    
    /**
     * Returns the number of blocks in the sorted table,
     * which is the same as it would be in a
     * materialized table.
     * It does <i>not</i> include the one-time cost
     * of materializing and sorting the records.
     * @see simpledb.query.Plan#blocksAccessed()
     */
    public int blocksAccessed() {
       // does not include the one-time cost of sorting
       Plan mp = new MaterializePlan(p, tx); // not opened; just for analysis
       return mp.blocksAccessed();
    }
    
    /**
     * Returns the number of records in the sorted table,
     * which is the same as in the underlying query.
     * @see simpledb.query.Plan#recordsOutput()
     */
    public int recordsOutput() {
       return p.recordsOutput();
    }
    
    /**
     * Returns the number of distinct field values in
     * the sorted table, which is the same as in
     * the underlying query.
     * @see simpledb.query.Plan#distinctValues(java.lang.String)
     */
    public int distinctValues(String fldname) {
       return p.distinctValues(fldname);
    }
    
    /**
     * Returns the schema of the sorted table, which
     * is the same as in the underlying query.
     * @see simpledb.query.Plan#schema()
     */
    public Schema schema() {
       return sch;
    }
    
    
    
    private List<TempTable> splitIntoRuns(Scan src) {
       List<TempTable> temps = new ArrayList<TempTable>();
       src.beforeFirst();
       if (!src.next()){
          return temps;
 	  }
       TempTable currenttemp = new TempTable(sch, tx);
       UpdateScan currentscan = currenttemp.open();
       int recordsInserted = 0;
       while (copy(src, currentscan)){
         recordsInserted++;
         if(recordsInserted==recordsPerBlock){
           recordsInserted = 0;
           // Sort the records in the currentscan
 		  currentscan.close();
           currenttemp = sortRun(currenttemp);
 		  temps.add(currenttemp);
           currenttemp = new TempTable(sch, tx);
           currentscan = (UpdateScan) currenttemp.open();
         }
       }
       currentscan.close();
 	  currenttemp = sortRun(currenttemp);
 	  temps.add(currenttemp);
       return temps;
    }
    
    private List<TempTable> doAMergeIteration(List<TempTable> runs) {
 		int i;
 		int runsize = runs.size();
 		List<TempTable> toMerge;
 		List<TempTable> result = new ArrayList<TempTable>();
 		while(runs.size() > (k-1)) {
 			toMerge = new ArrayList<TempTable>();
 			for(i = 0; i<k; i++){
 				toMerge.add(runs.remove(0));
 			}
 			result.add(mergeRuns(toMerge));
 		}
		toMerge = new ArrayList<TempTable>();
 		//this will occur if (number of runs)%(k)!= 0. The remaining elements are merged just as the previous.
 		while(runs.size()>0){
			toMerge.add(runs.remove(0));
 		}
		result.add(mergeRuns(toMerge));
 		return result;
    }
    //creates one sorted run from a list of sorted runs
    private TempTable mergeRuns(List<TempTable> runs) {
 		List<Scan> scans = new ArrayList<Scan>();
 		boolean more, first;
 		int i;
 		int smallest=0;
 		int scanIndex=0;
 		TempTable result = new TempTable(sch,tx);
 		UpdateScan dest = result.open();
 		//open a scan for each run
 		for (TempTable run : runs){
 			Scan s = run.open();
 			//point to first element in the scan
 			s.next();
 			scans.add(s);
 		}
 		//runs until nothing remains to be added
 		while(scans.size()>0){
 			first = true;
 			for (i=0;i<scans.size(); i++){
 				//on the first encounted element
 				if(first){
 					smallest = scans.get(i).getInt(sortfields.get(0));
 					scanIndex = i;
 					first = false;
 				}
 				//on each remaining elements
 				else if(scans.get(i).getInt(sortfields.get(0))<smallest){
 					smallest = scans.get(i).getInt(sortfields.get(0));
 					scanIndex = i;
 				}
 			}
 			more = copy(scans.get(scanIndex),dest);
 			//if all of a run's elements have been added to the merge, close the associated scan and remove it from the list
 			if(!more){
 				scans.get(scanIndex).close();
 				scans.remove(scanIndex);
 			}
 		}
 		dest.close();
 		//close all scans
 		for (Scan s : scans){
 			s.close();
 		}
 		return result;
    }
    
    private TempTable mergeTwoRuns(TempTable p1, TempTable p2) {
       Scan src1 = p1.open();
       Scan src2 = p2.open();
       TempTable result = new TempTable(sch, tx);
       UpdateScan dest = result.open();
       
       boolean hasmore1 = src1.next();
       boolean hasmore2 = src2.next();
       while (hasmore1 && hasmore2)
          if (comp.compare(src1, src2) < 0)
          hasmore1 = copy(src1, dest);
       else
          hasmore2 = copy(src2, dest);
       
       if (hasmore1)
          while (hasmore1)
          hasmore1 = copy(src1, dest);
       else
          while (hasmore2)
          hasmore2 = copy(src2, dest);
       src1.close();
       src2.close();
       dest.close();
       return result;
    }
    
    private boolean copy(Scan src, UpdateScan dest) {
       dest.insert();
       for (String fldname : sch.fields()){
          dest.setVal(fldname, src.getVal(fldname));
 	  }
       return src.next();
    }
    
    // Sort the records in a given run
    private TempTable sortRun(TempTable t){
      // make temporary update scans to hold the sorted records
 	 boolean next = true;
 	 UpdateScan scan, s;
 	 TempTable temp;
 	 TempTable sorted = new TempTable(sch,tx);
 	 List<TempTable> runs = new ArrayList<TempTable>();
 	 scan = t.open();
 	 scan.next();
 	 while(next){
 		temp = new TempTable(sch,tx);
 		s = temp.open();
 		s.beforeFirst();
 		next = copy(scan,s);
 		s.close();
 		sorted = mergeTwoRuns(temp,sorted);
 	 }
 	 scan.close();
 	 return sorted;
 	}
    
    public void printContents(List<TempTable> runs){
 	int count;
 	int runCount = 0;
 	for(TempTable run : runs){
 		runCount++;
 		count = 0;
 		Scan s = run.open();
 		s.beforeFirst();
 		System.out.printf("Run %d:\n", runCount); 
 		while(s.next()){
 			count++;
 			int val = s.getInt(sortfields.get(0));
 			System.out.print(val);
 			if(count == recordsPerBlock){
 				System.out.print("\n");
 				count=0;
 			}
 			else{
 				System.out.print("\t");
 			}
 		}
 		s.close();
		System.out.print("\n");
    }
    }
 }
