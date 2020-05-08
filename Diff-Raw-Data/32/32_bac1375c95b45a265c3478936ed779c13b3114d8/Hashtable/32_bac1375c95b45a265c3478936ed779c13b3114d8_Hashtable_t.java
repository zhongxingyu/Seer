 package dict;
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
     int size = nextPrime(sizeEstimate);
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
       this.table[index].push(e);
     } else { //otherwise-a collision has occurred
       this.table[index].push(e);
       this.collisions++;
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
    *  nextPrime() gives the next prime number greater than or equal 
    *  to the input number.
    *  ex: nextPrime(20) == 23
    *      nextPrime(31) == 31
    *
    *  @param val the value for to look for the next sequential prime.
    *  @return the next prime number at or after the given input.
    **/
   private static int nextPrime(int val) {
     //320 is the largest gap between prime numbers in the range of int32
     int maxSearch = 320 + val;
   }
 
   private static int first_prime_over(int n){
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
 }
