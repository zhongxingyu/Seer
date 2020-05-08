 /* Class:
  *   TTable
  * Description:
  *   A Transposition Table which stores critical information about previously seen
  *   states to save computation time.
  */
 public class TTable {
 
 	public TTableEntry[] entries;
 	private int numEntries;
 	
 	public TTable() {
 		numEntries = 256;
 		entries = new TTableEntry[numEntries];
 	}
 	
 	/* Takes a hash and gives the TTable entry that it corresponds to,
 	 * if it exists. Returns null if the entry is not valid. */
 	public TTableEntry getEntry(long tgtHash) {
 		int tgtIndex = getIndex(tgtHash);
 		if (entries[tgtIndex].valid)
 			return entries[tgtIndex];
 		
 		return null;
 	}
 	
 	/* Stores a new TTable Entry into the TTable. */
 	public void storeEntry(TTableEntry newEntry) {
 		newEntry.valid = true;
 		int tgtIndex = getIndex(newEntry.hash);
 		entries[tgtIndex] = newEntry;
 	}
 	
 	/* Takes a hash and returns the array index in the TTable
 	 * that it corresponds to. */
 	private int getIndex(long tgtHash) {
 		// 256 is 2^8 (8 bits), long is 64 bits, mask 56 bits.
 		for (int i = 0; i < 56; i++)
			tgtHash = tgtHash >> 1;
		return (int)tgtHash;
 	}
 }
