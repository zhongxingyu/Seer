 /**
  * 
  */
 
 /**
  * @author Dvir
  *
  */
 
 public class RAM implements Queue {
 	private int size;
 	private Link last; // a circular linked list
 	private Link[] ram; // a list indexing the circular linked list
 	private boolean useLRU;
 	
	public RAM(boolean useLRU) {
 		this.size = 0;
		this.ram = new Link[PHYSICAL_MEMORY_SIZE]; // initialize the RAM array, in the size of the ROM, so the indexes will correspond to the Page in the rom.
		for (int i = 0; i < PHYSICAL_MEMORY_SIZE; i++) {
 			this.ram[i] = null; // null = page is NOT in the RAM, otherwise - the page in the RAM
 		}
 		
 		this.last = null; // initialize the RAM queue
 		this.useLRU = useLRU; // determines whether we handle the RAM's queue by FIFO or LRU
 	}
 	
 	/**
 	 * Reads data from the RAM
 	 * @param key The memory slot to read from
 	 * @return The data
 	 */
 	public String read(int key) {
 		if (this.useLRU) {
 			this.pushToTop(key);
 		}
 		
 		return this.ram[key].getPage().getData();
 	}
 	
 	/**
 	 * Write data into the RAM 
 	 * @param key The memory slot to write to
 	 * @param c The data to append to the memory slot
 	 */
 	public void write(int key, char c) {
 		if (this.useLRU) {
 			this.pushToTop(key);
 		}
 		
 		this.ram[key].getPage().appendChar(c);		
 	}
 	
 	/**
 	 * Push a page in the RAM to the top of the queue
 	 * @param key The page index in the RAM
 	 */
 	private void pushToTop(int key) {
 		// the page is already in the list, and we are using LRU (Least Recently Used), 
 		// which means we should "push" the page up
 //System.out.print("Current top: ["+this.last.getNext().getPage().getIndex()+"] "+this.last.getNext().getPage().getData()+"...");
 		/*int startingTop = this.last.getNext().getPage().getIndex();
 		
 		Link currLink = this.ram[key];
 		Link prevLink = currLink.getPrev();
 		Link nextLink = currLink.getNext();
 		Link first = this.last.getNext();
 
 		// pop the link outside of the queue
 		prevLink.setNext(nextLink);
 		nextLink.setPrev(prevLink);
 		
 		// prepare the link with prev and next as last and first respectively
 		currLink.setPrev(this.last);
 		currLink.setNext(first);
 		
 		// push the link back in the top of the queue
 		first.setPrev(currLink);
 		this.last.setNext(currLink);
 		
 		if (startingTop == this.last.getNext().getPage().getIndex()) {
 			// this is the point everything fucks up 
 			// (well not exactly, as two calls in a row to the same key are possible, but it's quite obvious looking at the debugging data)
 			//System.out.print("==" + this.size + "==");
 		}
 		//System.out.println("New top: ["+this.last.getNext().getPage().getIndex()+"] "+this.last.getNext().getPage().getData()+"...");
 		*/
 		
 		//OR's VERSION:
 		Link first = this.last.getNext();
 		
		if (!this.ram[key] == first) {
 			Link temp = this.ram[key];
 		
 			temp.getPrev().setNext(temp.getNext());
 			temp.getNext().setPrev(temp.getPrev());
 		
 			if (this.ram[key] == this.last) {
 				this.last = this.last.getPrev();
 			}
 			
 			temp.setNext(first);
 			this.last.setNext(temp);
 			temp.setPrev(this.last);
 			first.setPrev(temp);
 		}
 	}	
 	
 	/**
 	 * Checks if a page is in the RAM
 	 * @param key The memory slot index
 	 * @return True if the page is in the RAM, false otherwise
 	 */
 	public boolean exists(int key) {
 		return (this.ram[key] != null);
 	}
 	
 	/**
 	 * Checks if the RAM queue is empty
 	 * @return True if empty, false otherwise
 	 */
 	public boolean isEmpty() {
 		return (this.size == 0);
 	}
 	
 	/**
 	 * Checks if the RAM queue is full
 	 * @return True if it's full, false otherwise
 	 */
 	public boolean isFull() {
		return (this.size == RAM_SIZE);
 	}
 	
 	/**
 	 * Enqueues a new page to the RAM queue
 	 * @param page The page to enqueue
 	 */
 	public void enqueue(Page page) {
 		if (this.isFull()) {
 			// the queue is full! can't add any more pages to the RAM.
 			// throw an exception.
 			// NOTE: we could've just dequeued and then enqueued, but in the RAM we need to save the dequeued pages,
 			// so we can't just pop it.			
 			throw new RuntimeException("QueueOverflow in RAM.enqueue()");
 		}
 		
 		// push the page to the start of the queue		
 		Link newLink = new Link(new Page(page.getIndex(), page.getData()));
 		
 		// if the queue is empty, initialize it with the new link
 		if (this.last == null) {
 			this.last = newLink;
 		}
 		else {
 			newLink.setPrev(this.last);
 			newLink.setNext(this.last.getNext());
 			this.last.getNext().setPrev(newLink);
 			this.last.setNext(newLink);
 		}
 		
 		this.ram[newLink.getPage().getIndex()] = newLink;
 		
 		this.size++;		
 	}
 	
 	/**
 	 * Dequeue's the last element of the RAM queue
 	 * @return page The page we dequeued from the RAM
 	 */
 	public Page dequeue() {		
 		if (this.isEmpty()) {
 			// the queue is empty! nothing to dequeue.
 			// throw an exception.
 			throw new RuntimeException("QueueUnderFlow in RAM.dequeue()");
 		}
 		
 		Link toRemove = this.last; // the last Link in the RAM queue
 		Link newLast = toRemove.getPrev(); // the link before the last in the queue
 		Link first = toRemove.getNext(); // the first link in the queue
 		
 		// remove the current last link from the end of the queue
 		first.setPrev(newLast);
 		newLast.setNext(first);
 		
 		// update the last link in the queue to the link before it
 		this.last = newLast;
 		
 		// get the page of the link we are about to dequeue
 		Page page = toRemove.getPage();
 
 		// remove the link from our RAM index
 		this.ram[page.getIndex()] = null;
 		
 		// decrease the size of the queue
 		this.size--;		
 		return page;
 	}
 }
