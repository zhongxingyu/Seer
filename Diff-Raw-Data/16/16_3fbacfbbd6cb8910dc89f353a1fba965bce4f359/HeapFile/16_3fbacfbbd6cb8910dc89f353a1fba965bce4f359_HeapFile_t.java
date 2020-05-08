 package simpledb;
 
 import java.io.*;
 import java.util.*;
 
 /**
  * HeapFile is an implementation of a DbFile that stores a collection of tuples
  * in no particular order. Tuples are stored on pages, each of which is a fixed
  * size, and the file is simply a collection of those pages. HeapFile works
  * closely with HeapPage. The format of HeapPages is described in the HeapPage
  * constructor.
  * 
  * @see simpledb.HeapPage#HeapPage
  * @author Sam Madden
  */
 public class HeapFile implements DbFile {
     public class MyIterator implements DbFileIterator {
         HeapFile holder;
         Iterator<Tuple> hasNext;
         Iterator<Tuple> next;
         TransactionId tid;
         int hasNextcounter;
         int nextCounter;
         int tableId;
         int numPage;
         HeapPage temp;
         public MyIterator(HeapFile f,TransactionId id){
             holder=f;
             tid=id;
             hasNextcounter=0;
             tableId=f.getId();
             numPage=f.numPages();
 
         }
         public void open(){
             try{
                 temp=(HeapPage)Database.getBufferPool().getPage(tid,new HeapPageId(tableId,0),Permissions.READ_WRITE);
                 hasNext=temp.iterator();
             }catch (TransactionAbortedException e){
             }catch (DbException e1){
             }
             }
 
         public boolean hasNext(){
             if(hasNext==null){
                 return false;
             }
             if(hasNext.hasNext()){
                 return hasNext.hasNext();
             }
             try{
                 while(hasNextcounter<(numPage-1)){
                  hasNextcounter++;
                  temp=(HeapPage)Database.getBufferPool().getPage(tid,new HeapPageId(tableId,hasNextcounter),Permissions.READ_WRITE);
                  hasNext=temp.iterator();
                  if(hasNext.hasNext()){
                      return true;
                  }
                 }
                 }catch(TransactionAbortedException e){
                     return false;
                 } catch(DbException e1){
                     return false;
                 }
                 return false;
         }
         public Tuple next(){
             if(hasNext==null){
                 throw new NoSuchElementException();
             }
             return hasNext.next();
 
         }
         public void close(){
             hasNextcounter=0;
             hasNext=null;
         }
         public void rewind(){
             close();
             open();
         }
 
 
 
     }
 
     File fileHolder;
     TupleDesc schemeHolder;
     HashMap<Integer,Boolean> pageHolder;
     /**
      * Constructs a heap file backed by the specified file.
      * 
      * @param f
      *            the file that stores the on-disk backing store for this heap
      *            file.
      */
     public HeapFile(File f, TupleDesc td) {
         // some code goes here
         fileHolder=f;
         schemeHolder=td;
         pageHolder=new HashMap<Integer,Boolean>();
         
     }
 
     /**
      * Returns the File backing this HeapFile on disk.
      * 
      * @return the File backing this HeapFile on disk.
      */
     public File getFile() {
         // some code goes here
         return fileHolder;
     }
 
     /**
      * Returns an ID uniquely identifying this HeapFile. Implementation note:
      * you will need to generate this tableid somewhere ensure that each
      * HeapFile has a "unique id," and that you always return the same value for
      * a particular HeapFile. We suggest hashing the absolute file name of the
      * file underlying the heapfile, i.e. f.getAbsoluteFile().hashCode().
      * 
      * @return an ID uniquely identifying this HeapFile.
      */
     public int getId() {
         // some code goes here
        return fileHolder.getAbsoluteFile().hashCode();
     }
 
     /**
      * Returns the TupleDesc of the table stored in this DbFile.
      * 
      * @return TupleDesc of this DbFile.
      */
     public TupleDesc getTupleDesc() {
         // some code goes here
         return schemeHolder;
     }
 
     // see DbFile.java for javadocs
     public Page readPage(PageId pid) {
         // some code goes here
         try{
             if(pid.getTableId()==getId()){
                 RandomAccessFile temp=new RandomAccessFile(fileHolder,"r");
                 long offset= pid.pageNumber()*BufferPool.PAGE_SIZE;
                 byte[] data=new byte[BufferPool.PAGE_SIZE];
                 temp.seek(offset);
                 temp.read(data);
                temp.close();
                 HeapPage result=new HeapPage((HeapPageId)pid,data);
                 return result;
             } else {
                 throw new IllegalArgumentException();
             }
         
         }catch (IOException e){
              System.out.println("heyyy");
              e.printStackTrace();
              throw new RuntimeException();
         }
     }
 
     // see DbFile.java for javadocs
     public void writePage(Page page) throws IOException {
         // some code goes here
         // not necessary for proj1
         RandomAccessFile temp=new RandomAccessFile(fileHolder,"rw");
         long offset= page.getId().pageNumber()*BufferPool.PAGE_SIZE;
         byte[] data=page.getPageData();
         temp.seek(offset);
         temp.write(data);
         page.markDirty(false,null);
 
     }
 
     /**
      * Returns the number of pages in this HeapFile.
      */
     public int numPages() {
         // some code goes here
         return (int)Math.ceil((double)fileHolder.length()/(double)BufferPool.PAGE_SIZE);
     }
 
     // see DbFile.java for javadocs
     public ArrayList<Page> insertTuple(TransactionId tid, Tuple t)
             throws DbException, IOException, TransactionAbortedException {
         // some code goes here
         // not necessary for proj1
         BufferPool holder=Database.getBufferPool();
         int bound=numPages();
         for(int i=0;i<bound;i++){
             HeapPageId temp=new HeapPageId(getId(),i);
             HeapPage candidate=(HeapPage)holder.getPage(tid,temp,Permissions.READ_WRITE);
             if(candidate.getNumEmptySlots()>0){
                 candidate.insertTuple(t);
                 candidate.markDirty(true,tid);
                 ArrayList<Page> result=new ArrayList<Page>();
                 result.add(candidate);
                 return result;
             }
         }
         byte[] add= HeapPage.createEmptyPageData();
         HeapPageId temp2=new HeapPageId(getId(),bound);
         HeapPage candidate2= new HeapPage(temp2,add);
         writePage(candidate2);
         HeapPage candidate3=(HeapPage)holder.getPage(tid,temp2,Permissions.READ_WRITE);
         candidate3.insertTuple(t);
         candidate3.markDirty(true,tid);
         ArrayList<Page> result=new ArrayList<Page>();
         result.add(candidate3);
         return result;
 
 
     }
 
     // see DbFile.java for javadocs
     public Page deleteTuple(TransactionId tid, Tuple t) throws DbException,
             TransactionAbortedException {
         // some code goes here
         // not necessary for proj1
         if(t.getRecordId()!=null && t.getRecordId().getPageId().getTableId()==getId()){
             BufferPool holder=Database.getBufferPool();
             HeapPage dest=(HeapPage)holder.getPage(tid,t.getRecordId().getPageId(),Permissions.READ_WRITE);
             dest.deleteTuple(t);
             dest.markDirty(true,tid);
             return dest;
         } else {
             throw new DbException("Tuple not in file");
         }
             
     }
 
     // see DbFile.java for javadocs
     public DbFileIterator iterator(TransactionId tid) {
         // some code goes here
         MyIterator result=new MyIterator(this,tid);
         return result;
 
 
     }
 
 }
 
