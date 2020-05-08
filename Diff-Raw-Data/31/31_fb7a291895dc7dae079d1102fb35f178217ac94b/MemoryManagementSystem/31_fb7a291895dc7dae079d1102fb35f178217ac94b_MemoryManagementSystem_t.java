 import java.util.Arrays;
 
 
 /**
  * Implements a memory management system.
  *
  * @author Ory Band
  * @version 1.0
  */
 public class MemoryManagementSystem {
     private RAM      ram;
     private String[] hd;
 	private boolean  lru;
 
 
     /**
      * @param lru Use LRU implementation.
      *
      * @return a new initialized MemoryManagementSystem object.
      */
 	public MemoryManagementSystem(boolean lru) {
         this.ram = new RAM(1000, 50);
 		this.hd  = new String[1000];
 		this.lru = lru;
 
         for (int i=0; i<1000; i++) {
             this.hd[i] = "";
         }
 	}
 
 
 	/** @see java.lang.Object#toString() */
 	public String toString() {
 		return "secondaryMemory=" + Arrays.toString(this.hd);
 	}
 
 
     /**
      * Loads page into RAM, and flushes old head Page.
      * Distinguishes between FIFO/LRU.
      *
      * @param key Page's key in hard-disk.
      */
     public void load(int key) {
         Page p       = this.ram.getPage(key),
              oldHead = null;
 
         // Load data to RAM if not present, and return old head Page.
         if (p.prev == null && p.next == null) {
             p.setData(this.hd[key]);  // Load data from hard-disk.
            this.ram.enqueue(p);
            oldHead = this.ram.dequeue();
         // If data already in RAM and LRU is on,
         // Relocate data to RAM's end of the line, and get old
         // head Page.
         } else if (lru) {
            if (p.prev == null) {  // This is the head Page.
                this.ram.dequeue();
                this.ram.enqueue(p);
            } else if (p.next != null) {  // This is NOT the tail Page.
                this.ram.remove(p);
                this.ram.enqueue(p);
            }
         }
 
         // Update (flush) data on hard-disk if page was thrown out of RAM.
         if (oldHead != null) {
             this.flush(oldHead);
         }
     }
 
 
     /**
      * Updates Page's data to hard-disk.
      *
      * @param p Page whose data is to be flushed.
      */
     private void flush(Page p) {
         this.hd[p.getIndex()] = p.getData();
     }
 
 
     /**
      * Reads Page from RAM, and updates old head page in hard-disk, if thrown out of RAM.
      *
      * @param key Page key in hard-disk.
      *
      * @return Page's data.
      */
 	public String read(int key) {
         this.load(key);
         return this.ram.getPage(key).getData();
     }
 
 
     /**
      * Appends a character to the Page in RAM.
      *
      * @param key Page's key in hard-disk.
      * @param c Character to append to Page.
      */
     public void write(int key, char c) {
         this.load(key);
         this.ram.getPage(key).appendChar(c);
 	}
 }
 
