 
 import java.util.*;
 import java.io.*;
 
 public class File extends Component implements Serializable{
 	
 	static final int BLOCK_SIZE = 256;
 	
 	private ArrayList<Integer> mDataPointer;
 	private int mBlockPosition;
 	
 	public File() {
 			
 		super("default");
 		mDataPointer = new ArrayList<Integer>();
 	}
 	
 	public File(String name) {
 	
 		super(name);
 		
 		mDataPointer = new ArrayList<Integer>();
 		int mBlockPosition = 0;
 	}
 	
 	public ArrayList<Integer> getDataPointer() {
 	
 		return mDataPointer;
 	}
 	
 	public int getCurrentBlockIndex() {
 	
 		int index = -1;
 		if(!mDataPointer.isEmpty()) {
			index = mDataPointer.get(mDataPointer.size()-1);
 		}
 		
 		return index;
 	}
 	
 	public void addBlockIndex(int index) {
 	
 		mDataPointer.add(index);
 	}
 	
 	public int getBlockPosition() {
 		
 		return mBlockPosition;
 	}
 	
 	public void setBlockPosition(int position) {
 	
 		mBlockPosition = position;
 	}
 }
