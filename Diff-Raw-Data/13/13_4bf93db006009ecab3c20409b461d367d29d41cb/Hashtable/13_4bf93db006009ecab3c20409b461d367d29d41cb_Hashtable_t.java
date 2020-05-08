 package dict;
 
import Constants.*;
 /**
  *  Hashtable extends the Dictionary abstract class.
  *  It implements hashing and chaining. What this means 
  *  is that if two entries either hash or compress to the 
  *  same array index, they will be stored in a list at that
  *  index.
  **/
 
 public class Hashtable extends Dictionary{
 
   /**
    *  Member Variables.
    *
    *  table the array that contains EntryList chains for hashed entries.
    *  collisions the number of collisions this hashtable has.
    **/
   private EntryList table[];
   private int collisions;
 
   /**
    *  Hashtable constructor creates a hashtable of a
    *  size close to the value specified.
    *
    *  @param sizeEstimate size estimated for this hashtable.
    **/
   public Hashtable(int sizeEstimate) {
     int size = firstPrimeOver(sizeEstimate);
     this.table = new EntryList[size];
     this.collisions = 0;
   }
 
   /**
    *  Hashtable creates an empty hashtable of a small size.
    **/
   public Hashtable() {
     this(100);
   }
 
   /**
    *  find() returns the corresponding value for a given key.
    *  If the given key is not in the dictionary, return null.
    *
    *  Running time: O(1) --if the load factor is low.
    *                O(n) --if the load factor is high. 
    *                       where n is the number of entries.
    *  @param key the key to lookup in this hashtable.
    *  @return the corresponding value to the given key.
    **/
   @Override
   public Object find(Object key) {
     int index = compress(key.hashCode());
     return (table[index] == null) ? null : table[index].find(key);
   }
 
   /**
    *  insert() inserts a key-value pair into this hashtable.
    *  The key will be hashed and compressed and fit into a chain in this table.
    *
    *  @param key the key to be stored in this hashtable.
    *  @param value the value that corresponds to the given key.
    **/
   @Override
   public void insert(Object key, Object value) {
     Entry e = new Entry(key,value);
     int index = compress(key.hashCode());
 
     if (this.table[index] == null) { //if bucket is empty-make new EntryList
       this.table[index] = new EntryList();
     } else { //otherwise-a collision has occurred
       this.collisions++;
     }
     this.table[index].push(e);
     this.size++;
 
     if(getLoadFactor() > Constants.MAX_LOAD) {
      Constants.print("resized");
       expand(2);
     }
   }
 
   /**
    *  remove() finds an entry in this hashtable given a key.
    *  The entry is then removed. If the given key is not in
    *  this hashtable, nothing happens.
    *
    *  @param key the key of the entry to be removed.
    **/
   @Override
   public void remove(Object key) {
     int index = compress(key.hashCode());
     if (this.table[index] != null) {
       //if this bucket has more than one entry, decrement number of collisions
       if(this.table[index].length() > 1) {
         this.collisions--;
       }
       this.table[index].remove(key);
       //if the chain's sizes goes to zero-save space by making it null
       if(this.table[index].length() == 0) {
         this.table[index] = null;
       }
     }
 
     if(getLoadFactor() < Constants.MIN_LOAD) {
       shrink(2);
     }
   }
 
   /**
    *  compress() compresses inputs in the integer range (Integer.MIN_VALUE
    *  to Integer.MAX_VALUE) to the range of array indicies of the size of
    *  this hashtable's table array.
    *
    *  @param code the hash code to compress.
    *  @return the array index where the given code compresses to.
    **/
   private int compress(int code) {
     int bigPrime = 16908799;
 
     if (this.table.length < bigPrime) {
       return ((code % bigPrime + bigPrime) % bigPrime) % this.table.length;
     } else {
       return (code % this.table.length + this.table.length) % this.table.length;
     }
   }
 
   /**
    *  expand() increases the size of this hashtable by a given factor.
    *  All items that are currently in this hashtable will be rehashed
    *  into the new hashtable. 
    *  ex: table size=100 ; expand(2.5) ; new table size = 250
    *
    *  @param expandFactor the factor by which to reduce the size of this table.
    **/
   private void expand(float expandFactor) {
     Hashtable newTable = new Hashtable((int)(this.table.length * expandFactor));
     for(EntryList l:this.table) {
       if (l != null) {
         for(Entry e:l) { //for each entry in each list
           //rehash each entry
           newTable.insert(e.getKey(),e.getValue());
         }
       }
     }
     //this table now owns the table field of the new table
    this.size = newTable.size;
     this.table = newTable.table;
     this.collisions = newTable.collisions;
   }
 
   /**
    *  shrink() reduces the size of this hashtable by a given factor.
    *  All items that are currently in this hashtable will be rehashed
    *  into the new hashtable.
    *  ex: table size=100 ; shrink(2) ; new table size = 50
    *
    *  @param shrinkFactor the factor by which to reduce the size of this table.
    **/
   private void shrink(float shrinkFactor) {
     expand(1/shrinkFactor);
   }
 
   /**
    *  firstPrimeOver() gives the next prime number greater than or equal 
    *  to the input number.
    *  ex: firstPrimeOver(20) == 23
    *      firstPrimeOver(31) == 31
    *  For the numbers in the positive integer range (0,2,147,483,647]
    *  the largest gap between any two prime numbers is 320. This means
    *  that the worst-case running time is O(319 * sqrt(n)) where n is
    *  the inputed number.
    *
    *  @param val the value for to look for the next sequential prime.
    *  @return the next prime number at or after the given input.
    **/
   private static int firstPrimeOver(int n){
     int out;
     for(out = n; !isPrime(out);out++){}
     return out;
   }
 
   /**
    *  getCollisions() returns the number of collisions this hashtable has.
    *  A collision is when a bucket of this table contains more than one entry.
    *
    *  @return the number of collisions in this hashtable.
    **/
   public int getCollisions() {
     return this.collisions;
   }
 
   /**
    *  getLoadFactor() returns the load factor of this hashtable.
    *  The load factor for a hashtable is defined as:
    *  the number of (entries / size of this table)
    *  A good load factor to acheive is 1.
    *
    *  @return the load factor of this hashtable.
    **/
   public float getLoadFactor() {
     return (float) size()/this.table.length;
   }
 
   /**
    *  isPrime() indicates if a number is prime.
    *
    *  @param val the number in question.
    *  @return whether the given number is prime or not.
    **/
   private static boolean isPrime(int n) {
     if ((n%2)==0){
       return false;
     }
     for (int i = 3; i < Math.sqrt(n); i+=2){
       if (n%i==0){
         return false;
       }
     }
     return true;
   }
 
   /**
    *  toString() gives the string representation of this hashtable.
    *  It follows the standards in the Java API.
    *
    *  @return the string representation of this hashtable.
    **/
   @Override
   public String toString() {
     String result = "";
     int r = 0;
     for(EntryList l:this.table) {
       if (l != null) {
         result = result +"\n"+r+": "+l.toString();
       }
       r++;
     }
     return result;
   }
 
   public static void main(String argv[]) {
     Hashtable h = new Hashtable(); //default constructor
 
     //insert
     for(int i=1;i<=1024;i=i<<1) {
       h.insert(i,i<<3);
     }
     System.out.println(h);
     //find
     for(int i=1;i<=1024;i=i<<1) {
       System.out.println("find "+i+": "+h.find(i));
     }
     //Collisions
     System.out.println("collisions:"+h.getCollisions());
     //remove
     for(int i=1;i<1024;i=i<<1) {
       System.out.println("remove() "+i);
       h.remove(i);
     }
     System.out.println(h);
     //invalid finds
     System.out.println("find(13) should be null: "+h.find(13));
     System.out.println("find(14) should be null: "+h.find(14));
     System.out.println("find(15) should be null: "+h.find(15));
     //invalid removes
     h.remove(100);
     h.remove(101);
     h.remove(102);
     System.out.println(h);
     //new table
     h = new Hashtable(1000);
     for(int i=0;i<1000;i++) {
       h.insert(Math.random(),i);
     }
     //Collisions
     System.out.println("new table, 1k entries: collisions:"+h.getCollisions());
     System.out.println("loadFactor: "+h.getLoadFactor());
     //System.out.println(h);
    //try to overload hashtable
    for(int i=0;i<1500;i++) {
      h.insert(Math.random(),i);
    }
    System.out.println("MAXLOAD:"+Constants.MAX_LOAD);
    System.out.println("loadFactor: "+h.getLoadFactor());
   }
 }
