 /**
  * Implements a linked-list array, to be used as runtime-efficient RAM.
  *
  * @author Ory Band
  * @Version 1.0
  */
 public class RAM {
     private Page[] ram;
    private Page   first, last;
 
     /**
      * @param hdSize Hard-disk size.
      * @param ramSize RAM size.
      *
      * @return a new initialized RAM object.
      */
     public RAM(int hdSize, int ramSize) {
         if (ramSize <= 1) {
             throw new RuntimeException("RAM size too small.");
         } else if (hdSize <= 1) {
             throw new RuntimeException("HD size too small.");
         } else if (hdSize < ramSize) {
             throw new RuntimeException("HD size smaller than RAM size.");
         }
 
         this.ram = new Page[hdSize];
 
         // Init data.
         for (int i=0; i<hdSize; i++) {
             this.ram[i] = new Page("", null, null);
         }
 
         for (int i=0; i<ramSize -1; i++) {
             // Set previous order.
             if (i >= 1) {
                 this.ram[i].prev = this.ram[i-1];
             }
 
             // Set advancing order.
             if (i <= ramSize -2) {
                 this.ram[i].next = this.ram[i+1];
             }
         }
 
        // Set first and last pages.
        this.first = this.ram[0];
        this.last  = this.ram[ramSize];
     }
 
     /**
      * Removes Page received as argument from list.
      *
      * @param key Page key in hard-disk.
      */
     public void remove(Page p) {
         if (p.prev != null) {
             p.prev.next = p.next;
         }
         if (p.next != null) {
             p.next.prev = p.prev;
         }
     }
 
 
     /**
      * Dequeues and returns the RAM's head page.
      *
      * @return RAM's head page. */
     public Page dequeue() {
        Page oldHead = this.first;
        this.first = this.first.next;
         this.remove(oldHead);
         return oldHead;
     }
 
 
     /**
      * Adds page to end of list, if not already present.
      *
      * @param key Page key in hard-disk.
      *
      * @return Oldest page (Page in head).
      */
     public Page enqueue(Page p) {
         if (p.prev != null || p.next != null) {  // Poll if Page is already in RAM.
             throw new RuntimeException("Page already in RAM.");
         } else {
            // Enqueue as last Page.
            p.prev = this.last;
             p.next = null;
 
            // Set as last in queue.
            this.last.next = p;
            this.last = p;
 
             return this.dequeue();
         }
     }
 }
 
