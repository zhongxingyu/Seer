 import java.util.ArrayList;
 
 public class Items implements Runnable {
 
 	private ArrayList<Item> itemList = new ArrayList<Item>();
 	public static ArrayList<Item> ITEM_POOL = new ArrayList<Item>();
 	public static int MAXIMUM_ITEM = 10;
 	public static int ITEM_INTERVAL = 4000;
 
 	public Items() {
 		// TODO Auto-generated constructor stub
 		Items.ITEM_POOL.add(new ItemSplit());
 		Items.ITEM_POOL.add(new ItemDash());
 		Items.ITEM_POOL.add(new ItemBetray());
 		Items.ITEM_POOL.add(new ItemRandom());
 		Items.ITEM_POOL.add(new ItemFast());
 		Items.ITEM_POOL.add(new ItemBigPad());
 		Items.ITEM_POOL.add(new ItemSmallPad());
 		Items.ITEM_POOL.add(new ItemGhost());
 		Items.ITEM_POOL.add(new ItemMagnet());
 		Items.ITEM_POOL.add(new ItemShock());
 		
 	}
 
 	@Override
 	public void run() {
 		while (true) {
 			// Check if Paused
 			synchronized (Game.lockPause) {
 				if(Game.isPaused())
 					try {
 						Game.lockPause.wait();
 					} catch (InterruptedException e) {
 						e.printStackTrace();
 					}
 			}
 			if (itemList.size() < MAXIMUM_ITEM) {
 					int i = (int) (Math.random() * ITEM_POOL.size());
 					Item t = ITEM_POOL.get(i).clone();
 					System.out.println("Place " + t.getName() + " at "
 							+ t.getX() + "," + t.getY());
 					itemList.add(t);
 					Sound.playItem();
 			}
			try {
				Thread.sleep(ITEM_INTERVAL);
				ITEM_INTERVAL = (int)(Math.random()*5000+1000);
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
 		}
 	}
 	
 	public ArrayList<Item> getItemList()
 	{
 		return itemList;
 	}
 }
