 package loecraftpack.ponies.abilities;
 
 import java.util.HashMap;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.entity.player.EntityPlayer;
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.relauncher.Side;
 import cpw.mods.fml.relauncher.SideOnly;
 
 public class AbilityPlayerData
 {
 	@SideOnly(Side.CLIENT)
 	public static AbilityPlayerData clientData = new AbilityPlayerData();
 	
 	@SideOnly(Side.CLIENT)
 	public static HashMap<Integer, Integer> useAttemptsClient = new HashMap<Integer, Integer>();
 	@SideOnly(Side.CLIENT)
 	public static HashMap<Integer, Integer> useAttemptsClientTimeStamp = new HashMap<Integer, Integer>();
 	@SideOnly(Side.CLIENT)
 	public static int nextAttemptID = 0;
 	@SideOnly(Side.CLIENT)
 	public static int energyAttemptOffset = 0;
 	@SideOnly(Side.CLIENT)
 	public static HashMap<Integer, Float> afterImageClient = new HashMap<Integer, Float>();
 	@SideOnly(Side.CLIENT)
 	public static HashMap<Integer, Integer> afterImageClientTimeStamp = new HashMap<Integer, Integer>();
 	@SideOnly(Side.CLIENT)
 	public static int nextAfterImageID = 0;
 	@SideOnly(Side.CLIENT)
 	public static float energyAfterImageOffset = 0;
 	
 	
 	public String playerName;
 	private EntityPlayer player = null;
 	public float energyRegenNatural = 5;//Multiples of 5, for max accuracy
 	public int energyMax = 500;
 	public float energy;
 	
 	public float chargeMax = 100.0f;
 	public float charge;
 	
 	public final ActiveAbility[] activeAbilities;
 	public final PassiveAbility[] passiveAbilities;
 	
 	private static HashMap<String, AbilityPlayerData> map = new HashMap<String, AbilityPlayerData>();
 	
 	@SideOnly(Side.CLIENT)
 	public AbilityPlayerData()
 	{
 		this.activeAbilities = ActiveAbility.NewAbilityArray();
 		this.passiveAbilities = PassiveAbility.NewAbilityArray();
 		playerName = "";
 		//Debug: full energy on load
 		energy = energyMax;
 	}
 	
 	public AbilityPlayerData(String player, ActiveAbility[] activeAbilities, PassiveAbility[] passiveAbilities)
 	{
 		this.activeAbilities = activeAbilities;
 		this.passiveAbilities = passiveAbilities;
 		playerName = player;
 		//Debug: full energy on load
 		energy = energyMax;
 	}
 	
 	public void setEnergy(float newEnergy)
 	{
 		energy = Math.min(energyMax, Math.max(0, newEnergy));
 	}
 	
 	public void addEnergy(float energyDifference)
 	{
 		energy = Math.min(energyMax, Math.max(0, energy + energyDifference));
 	}
 	
 	@SideOnly(Side.CLIENT)
 	public void setEnergyWithOffset(float newEnergy)
 	{
 		energy = Math.min(energyMax + energyAttemptOffset, Math.max(0, newEnergy));
 	}
 	
 	@SideOnly(Side.CLIENT)
 	public void addEnergyWithOffset(float energyDifference)
 	{
 		energy = Math.min(energyMax + energyAttemptOffset, Math.max(0, energy + energyDifference));
 	}
 	
 	public static AbilityPlayerData Get(String player)
 	{
 		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT)
 			return clientData;
 		else
 			return map.get(player);
 	}
 	
 	public static boolean HasPlayer(String player)
 	{
 		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT && player == Minecraft.getMinecraft().thePlayer.username)
 			return true;
 		else
 			return map.containsKey(player);
 	}
 	
 	public static AbilityPlayerData RegisterPlayer(String player)
 	{	
 		AbilityPlayerData playerData = new AbilityPlayerData(player, ActiveAbility.NewAbilityArray(), PassiveAbility.NewAbilityArray());
 
 		if (FMLCommonHandler.instance().getEffectiveSide() == Side.CLIENT && Minecraft.getMinecraft().thePlayer.username.equals(player))
 		{
 			clientData = new AbilityPlayerData(player, ActiveAbility.NewAbilityArray(), PassiveAbility.NewAbilityArray());
 			
 			for(AbilityBase ability : clientData.activeAbilities)
 				ability.SetPlayer(player, clientData);
 			
 			for(AbilityBase ability : clientData.passiveAbilities)
 				ability.SetPlayer(player, clientData);
 		}
 		else
 			map.put(player, playerData);
 		
 		return playerData;
 	}
 	
 	public static void UnregisterPlayer(String player)
 	{
 		map.remove(player);
 	}
 	
 	public void onUpdateSERVER(EntityPlayer player)
 	{
 		for(ActiveAbility ability : activeAbilities)
 			ability.onUpdate(player);
 		
 		for(PassiveAbility ability : passiveAbilities)
 			ability.onTick(player);
 	}
 	
 	public void onUpdateCLIENT(EntityPlayer player)
 	{
 
 		energyAttemptOffset = 0;
 		
 		int currentTime = (int)(System.currentTimeMillis()/100L);
 		for (int id : useAttemptsClientTimeStamp.keySet())
 		{
 			if (currentTime - useAttemptsClientTimeStamp.get(id) > 80)
 			{
 				cleanUse(id, 0);
 			}
 			else
 			{
 				energyAttemptOffset += useAttemptsClient.get(id);
 			}
 		}
 		
 		//trims energy with offset, to keep it from going to high.
 		setEnergyWithOffset(energy);
 		
 		energyAfterImageOffset = 0;
 		
 		for (int id : afterImageClientTimeStamp.keySet())
 		{
 			if (id >= 0 && currentTime - afterImageClientTimeStamp.get(id) > 10)
 			{
 				cleanAfterImage(id);
 			}
 			else
 			{
 				energyAfterImageOffset += afterImageClient.get(id);
 			}
 		}
 	}
 	
 	public static float getClientEnergyAfterImageRatio()
 	{
 		if (clientData == null)
 			return 0;
 		
 		if (clientData.energy + energyAfterImageOffset >= clientData.energyMax)
 			return 1;
 		else if (clientData.energy + energyAfterImageOffset <= 0)
 			return 0;
 		else
 			return (clientData.energy + energyAfterImageOffset) / (float) clientData.energyMax;
 	}
 	
 	public static float getClientEffectiveEnergyRatio()
 	{
 		if (clientData == null)
 			return 0;
 		
 		if (clientData.energy - (float)energyAttemptOffset >= clientData.energyMax)
 			return 1;
 		else if (clientData.energy - (float)energyAttemptOffset<= 0)
 			return 0;
 		else
 			return (clientData.energy  - (float)energyAttemptOffset) / (float) clientData.energyMax;
 	}
 
 	public static float getClientCastTimeRatio()
 	{
 		if (clientData == null)
 			return 0;
 		
 		if (clientData.charge >= clientData.chargeMax)
 			return 1;
 		else if (clientData.charge <= 0)
 			return 0;
 		else
 			return clientData.charge / clientData.chargeMax;
 	}
 	
 	public static int attemptUse(int cost)
 	{
 		int id = nextAttemptID;
 		nextAttemptID = (id+1)%256;
 		useAttemptsClient.put(id, cost);
 		useAttemptsClientTimeStamp.put(id, (int)(System.currentTimeMillis()/100L));
 		energyAttemptOffset += cost;
 		return id;
 	}
 	
 	public static void cleanUse(int id, int cost)
 	{
 		if (cost>0)
 		{
 			int imageId = nextAfterImageID;
 			nextAfterImageID = (imageId+1)%256;
 			afterImageClient.put(imageId, (float)cost);
 			if (useAttemptsClientTimeStamp.containsKey(id))
 				afterImageClientTimeStamp.put(imageId, useAttemptsClientTimeStamp.get(id));
 			else
 				afterImageClientTimeStamp.put(id, (int)(System.currentTimeMillis()/100L));;
 			clientData.addEnergy(-cost);
 		}
 		useAttemptsClient.remove(id);
 		useAttemptsClientTimeStamp.remove(id);
 	}
 	
 	public static void addToggleAfterImage(int id, float cost, float costMax)
 	{
 		if (afterImageClient.containsKey(id))
 		{
 			cost+=afterImageClient.get(id);
 			if (cost > costMax)
 				cost = costMax;
 		}
 		afterImageClient.put(id, cost);
 		afterImageClientTimeStamp.put(id, (int)(System.currentTimeMillis()/100L));
 	}
 	
 	public static void cleanAfterImage(int id)
 	{
 		afterImageClient.remove(id);
 		afterImageClientTimeStamp.remove(id);
 	}
 }
