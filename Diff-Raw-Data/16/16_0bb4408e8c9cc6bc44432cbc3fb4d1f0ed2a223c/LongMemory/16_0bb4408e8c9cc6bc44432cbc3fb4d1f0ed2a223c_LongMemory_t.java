 package structure;
 
 public class LongMemory implements Memory {
 	private LongWord[] mem;
 	private int size;
 
 	public LongMemory(int a) {
 		mem = new LongWord[a];
 		size = a;
 	}
 
 	public void clear() {
 		mem = new LongWord[size];
 	}
 
 	public Word get(int index) {
 		return mem[index];
 	}
 	
 	public void set(int index, Word word) {
 		mem[index] = (LongWord) word;
 	}
 }
