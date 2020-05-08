 package simpledb.index.hash;
 
 import simpledb.tx.Transaction;
 import simpledb.record.*;
 import simpledb.query.*;
 import simpledb.index.Index;
 
 import java.util.ArrayList;
 
 public class LinearHash implements Index{
   public int numBuckets = 1;
   private int overflowCount = 0;
   private int maxOverflow = 1;
   private int blockSize = 5; //max records per block
   private int splitPointer = 0; // next bucket to be split
   private boolean redistributing = false; //additional expands will not be performed mid-expand
   private ArrayList<Integer> records = new ArrayList<Integer>(); //number of records in each bucket including overflow
   private int level = 0;
   private String idxname;
   private Schema sch;
   private Transaction tx;
   private Constant searchkey = null;
   private TableScan ts = null;
 
   public LinearHash(String idxname, Schema sch, Transaction tx){
     this.idxname = idxname;
     this.sch = sch;
     this.tx = tx;
   }
 
   public void beforeFirst(Constant searchkey){
     close();
     this.searchkey = searchkey;
     int bucket = address(searchkey);
     String tblname = idxname + bucket;
     TableInfo ti = new TableInfo(tblname, sch);
     ts = new TableScan(ti, tx);
   }
   
   //sets the current tablescan to the bucket that will be split
   public void setSplitBucket(){
 	  String tblname = idxname + splitPointer;
 	  TableInfo ti = new TableInfo(tblname,sch);
 	  ts = new TableScan(ti,tx);  
   }
 
   public boolean next(){
     while(ts.next()){
       if(ts.getVal("dataval").equals(searchkey)){
         return true;
       }
     }
     return false;
   }
 
   public RID getDataRid(){
     int blknum = ts.getInt("block");
     int id = ts.getInt("id");
     return new RID(blknum, id);
   }
 
   public void insert(Constant val, RID rid){
 	close();
 	if(records.size()==0){
 		growArray(100);
 	}
 	System.out.println("--");
 	int bucket = address(val);
 	int numRecords = records.get(bucket);
     beforeFirst(val); 
     ts.insert();
     ts.setInt("block", rid.blockNumber());
     ts.setInt("id", rid.id());
     ts.setVal("dataval", val);
     records.set(bucket,numRecords+1);
     System.out.println(bucket);
     System.out.println(val);
     System.out.println(records.get(bucket));
     if(((numRecords+1)>(blockSize))&&(!redistributing)){
     	overflowCount++;
     	if(overflowCount==maxOverflow){
    		records.set(bucket, 0);
     		expand();
     	}
     }
   }
 
   // Expands the bucket pointed to by the splitPointer
   public void expand(){
 	close();
 	System.out.println("expand");
 	overflowCount=0;
     //printIndex();
     setSplitBucket();
     ArrayList<RID> rids = new ArrayList<RID>();
     ArrayList<Constant> vals = new ArrayList<Constant>();
 
     //clear the bucket; contents are saved to be redistributed after
     while(ts.next()){
       int block = ts.getInt("block");
       int id = ts.getInt("id");
       RID rid = new RID(block, id);
       Constant dataval = ts.getVal("dataval");
       rids.add(rid);
       vals.add(dataval); 
       ts.delete();
     }
     
     incrementSplitPointer();
     
     //rehash contents of split bucket
     redistributing = true;
     for(int i = 0; i<rids.size(); i++){
     	RID r = rids.get(i);
     	Constant dv = vals.get(i);
     	insert(dv,r);
     }
     redistributing = false;
     
     System.out.println("end expand");
     
     //printIndex();
   }
 
   // Closes the index by closing the current table scan
   public void close(){
     if (ts != null){
       ts.close();
     }
   }
 
   public int address(Constant key){
     double bucket = (key.hashCode() % (Math.pow(2,level)*numBuckets));
     if (bucket<splitPointer){
     	bucket = (key.hashCode() % ((int)(Math.pow(2,level+1))*numBuckets));
     }
     return (int)bucket;
   }
   
   public void growArray(int i){
 	  int l = records.size();
 	  while(l<i+1){
 		  records.add(0);
 		  l++;
 	  }
   }
  
   
 
   public void delete(Constant key, RID rid){
 	  return;
   }
 
   
   public void printIndex(){
     close();
     System.out.printf("Level: %d \t Next: %d", level, splitPointer);
     int len = numBuckets;
     int i;
     int block;
     boolean overflow = false;
     for(i=0; i<len; i++){
       String tblname = idxname + i;
       TableInfo ti = new TableInfo(tblname, sch);
       ts = new TableScan(ti, tx);
       ts.beforeFirst();
       System.out.printf("Bucket #%d\n", i);
       while(ts.next()){
         block = ts.currentBlock();
         if(block==0){
           // Not in the overflow blocks
           System.out.printf("%d \t", ts.getInt("id"));
         }
         else{
           if(!overflow){
             System.out.println();
             System.out.printf("Bucket #%d overflow\n", i);
             overflow = true;
           }
           System.out.printf("%d \t", ts.getInt("id"));
         }
       }
       System.out.println("\n"); // white space between buckets
     }
   }
 
   private void incrementSplitPointer(){
     splitPointer = (int)((splitPointer+1)%(Math.pow(2,level)));
     if (splitPointer == 0){
       level++;
     }
   }
 }
 
