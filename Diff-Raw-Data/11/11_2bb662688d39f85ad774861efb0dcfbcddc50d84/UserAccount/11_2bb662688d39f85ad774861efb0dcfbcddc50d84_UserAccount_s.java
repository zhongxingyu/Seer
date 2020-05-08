 package nl.giantit.minecraft.GiantBanks.Bank;
 
 import nl.giantit.minecraft.GiantBanks.GiantBanks;
 import nl.giantit.minecraft.GiantBanks.core.Items.ItemID;
 import nl.giantit.minecraft.GiantBanks.core.Tools.db.dbType;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class UserAccount {
 
 	private static HashMap<String, UserAccount> accounts = new HashMap<String, UserAccount>();
 	
 	private HashMap<String, ArrayList<Integer>> cache = new HashMap<String, ArrayList<Integer>>();
 	private ArrayList<BankSlot> slots = new ArrayList<BankSlot>();
 	
 	private Integer aID;
 	private String owner;
 	private String rawData;
 	private Boolean isUpdated = false;
 	private AccountType type;
 	
 	private void fillNewSlot(int usedSlots, String item, int amount, ItemID iID) {
 		BankSlot bS = new BankSlot(usedSlots, item, amount, iID);
 		this.slots.add(bS);
 		ArrayList<Integer> slots = new ArrayList<Integer>();
 		slots.add(usedSlots);
 		
 		this.cache.put(item, slots);
 		this.isUpdated = true;
 	}
 	
 	private void parseData() {
 		String[] d = this.rawData.split("-;-");
 		for(String bS : d) {
 			if(bS.length() <= 0)
 				continue;
 			
 			int u = this.slots.size();
 			this.slots.add(new BankSlot(bS));
 			if(!this.cache.containsKey(this.slots.get(u).getItem())) {
 				ArrayList<Integer> slots = new ArrayList<Integer>();
 				slots.add(u);
 				
 				this.cache.put(this.slots.get(u).getItem(), slots);
 			}else{
 				ArrayList<Integer> slots = this.cache.remove(this.slots.get(u).getItem());;
 				slots.add(u);
 				
 				this.cache.put(this.slots.get(u).getItem(), slots);
 			}
 		}
 		
 	}
 	
 	private String rawData() {
 		String d = "";
 		
 		for(BankSlot slot : this.slots) {
 			d += slot + "-;-";
 		}
 		
 		return d;
 	}
 	
 	private UserAccount(String p, String data) {
 		this(null, p, data);
 		this.isUpdated = true;
 	}
 	
 	private UserAccount(Integer id, String p, String data) {
 		this(id, p, data, null);
 	}
 	
 	private UserAccount(Integer id, String p, String data, AccountType type) {
 		if(null == type)
 			type = AccountType.getType(0);
 			
 		this.aID = id;
 		this.owner = p;
 		this.rawData = data;
 		this.type = type;
 		
 		if(null != this.rawData)
 			this.parseData();
 	}
 	
 	public void setAccountID(int id) {
 		this.aID = id;
 	}
 	
 	public Integer getAccountID() {
 		return this.aID;
 	}
 	
 	public String getOwner() {
 		return this.owner;
 	}
 	
 	public String getRawData() {
 		return this.rawData();
 	}
 	
 	public AccountType getType() {
 		return this.type;
 	}
 	
 	public Boolean isUpdated() {
 		return this.isUpdated;
 	}
 	
 	public Boolean setUpdated(Boolean updated) {
 		this.isUpdated = updated;
 		return this.isUpdated;
 	}
 	
 	public String add(ItemID iID, int amount) {
 		if(iID == null)
 			return "";
 		
 		String item = GiantBanks.getPlugin().getItemHandler().getItemNameByID(iID.getId(), iID.getType());
 		int mS = this.type.getMaxSlots();
 		int amt = amount;
 		
 		if(this.cache.containsKey(item)) {
 			ArrayList<Integer> slotIDs = this.cache.remove(item);
 			for(int i = 0; i < slotIDs.size(); i++) {
 				Integer slotID = slotIDs.get(i);
 				BankSlot bS = this.slots.get(slotID);
 				if(!bS.contains(iID)) {
 					//Durability missmatch? Or perhaps a misscache?
 					//What ever the case, we need to remove that entry from the cache!
 					slotIDs.remove(i);
 					continue;
 				}
 				
 				int a = bS.getAmount() + amt;
 				int d = this.type.getMaxPerSlot() - bS.getAmount();
 				
 				if(this.type.getMaxPerSlot() <= 0 || a <= this.type.getMaxPerSlot()) {
 					amt = 0;
 					bS.setAmount(a);
 					break;
 				}else if(d > 0) {
 					amt -= d;
 					bS.setAmount(d, true);
 					if(amt <= 0)
 						break;
 				}
 			}
 			
 			if(amt > 0){
 				while(amt > 0) {
 					int usedSlots = this.slots.size();
 					if(mS <= 0 || mS > usedSlots) {
 						if(amt - this.type.getMaxPerSlot() > 0) {
 							amt -= this.type.getMaxPerSlot();
 							this.fillNewSlot(usedSlots, item, this.type.getMaxPerSlot(), iID);
 						}else{
 							this.fillNewSlot(usedSlots, item, amt, iID);
 							amt = 0;
 							break;
 						}
 					}else{
 						break;
 					}
 				}
 			}
 			
 			if(amt == 0) {
 				//Call to Sync in case caching is off for bank accounts
 				this.isUpdated = true;
 				GiantBanks.getPlugin().getSync().callUpdate(dbType.ACCOUNTS);
 				return null;
 			}else{
				//message saying x items have not been added
				return "";
 			}
 			
			//return ""; //Return message saying no available slots.
 		}else{
 			while(amt > 0) {
 				int usedSlots = this.slots.size();
 				if(mS <= 0 || mS > usedSlots) {
 					if(amt - this.type.getMaxPerSlot() > 0) {
 						amt -= this.type.getMaxPerSlot();
 						this.fillNewSlot(usedSlots, item, this.type.getMaxPerSlot(), iID);
 					}else{
 						this.fillNewSlot(usedSlots, item, amt, iID);
 						amt = 0;
 						break;
 					}
 				}else{
 					break;
 				}
 			}			
 			
 			if(amt == 0) {
 				//Call to Sync in case caching is off for bank accounts
 				GiantBanks.getPlugin().getSync().callUpdate(dbType.ACCOUNTS);
 				return null;
 			}else{
 				//message saying x items have not been added
 				return "";
 			}
 		}
 	}
 	
 	public void get(ItemID iID, int amount) {
 		if(iID == null)
 			return;
 		
 		String item = GiantBanks.getPlugin().getItemHandler().getItemNameByID(iID.getId(), iID.getType());
 		
 		//Should probably return an ItemStack I think
 	}
 	
 	public void getAll(ItemID iID) {
 		//Should probably return an ArrayList<ItemStack>?
 	}
 	
 	public static UserAccount getUserAccount(String p) {
 		if(accounts.containsKey(p))
 			return accounts.get(p);
 		
 		return null;
 	}
 	
 	public static UserAccount createUserAccount(String p) {
 		return createUserAccount(p, null);
 	}
 	
 	public static UserAccount createUserAccount(String p, String data) {
 		if(!accounts.containsKey(p)) {
 			UserAccount UA = new UserAccount(p, data);				
 			accounts.put(p, UA);
 			return UA;
 		}
 		
 		return accounts.get(p);
 	}
 	
 	public static UserAccount createUserAccount(int id, String p, String data) {
 		if(!accounts.containsKey(p)) {
 			UserAccount UA = new UserAccount(id, p, data);				
 			accounts.put(p, UA);
 			return UA;
 		}
 		
 		return accounts.get(p);
 	}
 	
 	public static UserAccount createUserAccount(int id, String p, String data, AccountType type) {
 		if(!accounts.containsKey(p)) {
 			UserAccount UA = new UserAccount(id, p, data, type);
 			accounts.put(p, UA);
 			return UA;
 		}
 		
 		return accounts.get(p);
 	}
 	
 	public static HashMap<String, UserAccount> getAllAccounts() {
 		return accounts;
 	}
 	
 }
 ///b s -i:cookie -a 5
