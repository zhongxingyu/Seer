 package nl.giantit.minecraft.GiantBanks.Bank;
 
 import nl.giantit.minecraft.GiantBanks.GiantBanks;
 import nl.giantit.minecraft.GiantBanks.core.Items.ItemID;
 import nl.giantit.minecraft.GiantBanks.core.Items.Items;
 import nl.giantit.minecraft.GiantBanks.core.Tools.db.dbType;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 
 public class AccountType {
 
 	private static HashMap<Integer, String> typeByID = new HashMap<Integer, String>();
 	private static HashMap<String, AccountType> types = new HashMap<String, AccountType>();
 	private static AccountType last;
 	
 	private HashMap<String, Integer> data = new HashMap<String, Integer>();
 	private Items iH = GiantBanks.getPlugin().getItemHandler();
 	
 	private Integer typeID;
 	private String name;
 	private int maxSlots = 20;
 	private int maxPerSlot = 20;
 	private Boolean isUpdated = false;
 	private Boolean isNew = false;
 
 	private ArrayList<String> allowed = new ArrayList<String>();
 	private ArrayList<String> disallowed = new ArrayList<String>(); 
 
 	private AccountType(Integer id, String name) {
 		this(id, name, 20, 20);
 	}
 	
 	private AccountType(Integer id, String name, int maxSlot) {
 		this(id, name, maxSlot, 20);
 	}
 	
 	private AccountType(Integer id, String name, int maxSlot, int maxPerSlot) {
 		this(id, name, maxSlot, maxPerSlot, null);
 	}
 	
 	private AccountType(Integer id, String name, int maxSlot, int maxPerSlot, String item) {
 		this.typeID = id;
 		this.name = name;
 		this.maxSlots = maxSlot;
 		this.maxPerSlot = maxPerSlot;
 		
 		if(null != item) {
 			String[] items = item.split(";");
 			for(String i : items) {
 				if(i.startsWith("+")) {
 					i = i.replaceFirst("\\+", "");
 					allowed.add(i.toLowerCase());
 				}else{
					if(i.equals(""))
						continue;
					
 					i = i.replace("^-", "");
 					disallowed.add(i.toLowerCase());
 				}
 			}
 		}
 	}
 	
 	public Integer getTypeID() {
 		return this.typeID;
 	}
 	
 	public String getName() {
 		return this.name;
 	}
 	
 	public Boolean isNew() {
 		return this.isNew;
 	}
 	
 	public Boolean setNew(Boolean n) {
 		this.isNew = n;
 		return this.isNew;
 	}
 	
 	public Boolean isUpdated() {
 		return this.isUpdated;
 	}
 	
 	public Boolean setUpdated(Boolean updated) {
 		this.isUpdated = updated;
 		return this.isUpdated;
 	}
 	
 	public String setName(String n) {
 		this.isUpdated = true;
 		this.name = n;
 		
 		GiantBanks.getPlugin().getSync().callUpdate(dbType.TYPES);
 		return this.name;
 	}
 	
 	public int setMaxSlots(int ms) {
 		this.isUpdated = true;
 		this.maxSlots = ms;
 
 		GiantBanks.getPlugin().getSync().callUpdate(dbType.TYPES);
 		return this.maxSlots;
 	}
 	
 	public int setMaxPerSlots(int mps) {
 		this.isUpdated = true;
 		this.maxPerSlot = mps;
 		
 		GiantBanks.getPlugin().getSync().callUpdate(dbType.TYPES);
 		return this.maxPerSlot;
 	}
 	
 	public int setStorable(Boolean allow, ItemID item) {
 		if(item == null)
 			return -1;
 		
 		return this.setStorable(allow, iH.getItemNameByID(item.getId(), (null == item.getType() ? null : item.getType())));
 	}
 	
 	public int setStorable(Boolean allow, String item) {
 		if(item == null)
 			return -1;
 		
 		item = item.toLowerCase();
 		
 		if(allow) {
 			if(this.allowed.contains(item))
 				return -2;
 
 			if(this.disallowed.contains(item))
 				this.disallowed.remove(item);
 			
 			this.allowed.add(item);
 		}else{
 			if(this.disallowed.contains(item))
 				return -2;
 
 			if(this.allowed.contains(item))
 				this.allowed.remove(item);
 			
 			this.disallowed.add(item);
 		}
 		
 		this.isUpdated = true;
 		GiantBanks.getPlugin().getSync().callUpdate(dbType.TYPES);
 		return 0;
 	}
 	
 	public int getMaxSlots() {
 		return this.maxSlots;
 	}
 	
 	public int getMaxPerSlot() {
 		return this.maxPerSlot;
 	}
 	
 	public String getStorable() {
 		String s = "";
 		if(this.allowed.size() > 0) {
 			for(String al : this.allowed) {
				if(al.equals(""))
					continue;
				
 				s += "+" + al + ";";
 			}
 		}
 		
 		if(this.disallowed.size() > 0) {
 			for(String dl : this.disallowed) {
				if(dl.equals(""))
					continue;
				
 				s += "-" + dl + ";";
 			}
 		}
 		
 		return s;
 	}
 	
 	public Boolean isStorable(ItemID item) {
 		if(item == null)
 			return false;
 		
 		return this.isStorable(iH.getItemNameByID(item.getId(), (null == item.getType() ? null : item.getType())));
 	}
 	
 	public Boolean isStorable(String item) {
 		if(item == null)
 			return false;
 		
 		if(allowed.contains(item.toLowerCase())) {
 			return true;
 		}
 		
 		if(allowed.size() > 0)
 			return false;
 		
 		return !disallowed.contains(item.toLowerCase());
 	}
 	
 	public static AccountType createAccountType(String n) {
 		return createAccountType(n, 20, 20);
 	}
 	
 	public static AccountType createAccountType(String n, int maxSlot) {
 		return createAccountType(n, maxSlot, 20);
 	}
 	
 	public static AccountType createAccountType(String n, int maxSlot, int maxPerSlot) {
 		return createAccountType(n, maxSlot, maxPerSlot, null);
 	}
 	
 	public static AccountType createAccountType(String n, int maxSlot, int maxPerSlot, String item) {
 		int id = last.getTypeID() + 1;
 		
 		AccountType aT = createAccountType(id, n, maxSlot, maxPerSlot, item);
 		aT.setNew(true);
 		GiantBanks.getPlugin().getSync().callUpdate(dbType.TYPES);
 		return aT;
 	}
 	
 	public static AccountType createAccountType(int id, String n) {
 		return createAccountType(id, n, 20, 20);
 	}
 	
 	public static AccountType createAccountType(int id, String n, int maxSlot) {
 		return createAccountType(id, n, maxSlot, 20);
 	}
 	
 	public static AccountType createAccountType(int id, String n, int maxSlot, int maxPerSlot) {
 		return createAccountType(id, n, maxSlot, maxPerSlot, null);
 	}
 	
 	public static AccountType createAccountType(int id, String n, int maxSlot, int maxPerSlot, String item) {
 		if(!types.containsKey(n)) {
 			AccountType AT = new AccountType(id, n, maxSlot, maxPerSlot, item);
 			types.put(n, AT);
 			typeByID.put(id, n);
 			last = AT;
 			return AT;
 		}
 		
 		return types.get(n);
 	}
 	
 	public static AccountType getType(int id) {
 		if(typeByID.containsKey(id)) {
 			String n = typeByID.get(id);
 			if(types.containsKey(n)) {
 				return types.get(n);
 			}else
 				typeByID.remove(id);
 		}
 		
 		return null;
 	}
 	
 	public static AccountType getType(String name) {
 		if(types.containsKey(name)) {
 			return types.get(name);
 		}
 		
 		return null;
 	}
 	
 	public static HashMap<String, AccountType> getAllTypes() {
 		return types;
 	}
 }
