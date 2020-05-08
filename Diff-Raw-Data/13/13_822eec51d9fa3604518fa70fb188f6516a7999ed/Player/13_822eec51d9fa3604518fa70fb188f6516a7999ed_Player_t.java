 package fearlesscode;
 
 import fearlesscode.util.*;
import java.util.*;
 
 public class Player
 {
 	private Speed speed;
 	private ArrayList<Block> activeBlocks;
 
 	public Player()
 	{
 		Logger.reg(this, "player");
 		activeBlocks=new ArrayList<Block>();
 	}
 
 
 	public void addKey()
 	{
 		Logger.call(this, "addKey()");
 		Logger.ret(this, "addKey()");
 	}
 
 	/**
 	 * 
 	 * @param block
 	 */
 	public void enterBlock(Block block)
 	{
 		Logger.call(this, "enterBlock(Block)");
 		activeBlocks.add(block);
 		Logger.ret(this, "addKey(Block)");
 
 	}
 
 	public ArrayList<Block> getActiveBlocks()
 	{
 		Logger.call(this, "getActiveBlocks()");
 		Logger.ret(this, "getActiveBlocks()");
 		return activeBlocks;
 	}
 
 	public int getObtainedKeys()
 	{
 		Logger.call(this, "getObtainedKeys()");
 		Logger.ret(this, "getObtainedKeys()");
 		return 0;
 	}
 
 	/**
 	 * 
 	 * @param block
 	 */
 	public void leaveBlock(Block block)
 	{
 		Logger.call(this, "leaveBlock(Block)");
 		activeBlocks.remove(activeBlocks.indexOf(block));
 		Logger.ret(this, "leaveBlock(Block)");
 	}
 
 	/**
 	 * 
 	 * @param dir
 	 */
 	public void move(Speed newSpeed)
 	{
 		Logger.call(this, "move(dir)");
 
 		Logger.ret(this, "move(dir)");
 	}
 
 }
