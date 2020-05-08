 package no.runsafe.runsafebank;
 
import no.runsafe.framework.api.IDebug;
 import no.runsafe.framework.api.IScheduler;
 import no.runsafe.framework.api.event.plugin.IPluginDisabled;
 import no.runsafe.framework.api.player.IPlayer;
 import no.runsafe.framework.minecraft.inventory.RunsafeInventory;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 public class BankHandler implements IPluginDisabled
 {
 	public BankHandler(BankRepository bankRepository, IDebug output, IScheduler scheduler)
 	{
 		this.bankRepository = bankRepository;
 		this.debugger = output;
 
 		scheduler.startAsyncRepeatingTask(new Runnable()
 		{
 			@Override
 			public void run()
 			{
 				saveLoadedBanks();
 			}
 		}, 60, 60);
 	}
 
 	public void openBank(IPlayer viewer, IPlayer owner)
 	{
 		String ownerName = owner.getName();
 		if (!this.loadedBanks.containsKey(ownerName))
 			this.loadBank(ownerName);
 
 		viewer.openInventory(this.loadedBanks.get(ownerName));
 		debugger.debugFine(String.format("Opening %s's bank for %s", ownerName, viewer.getName()));
 	}
 
 	private void loadBank(String ownerName)
 	{
 		loadedBanks.put(ownerName, bankRepository.get(ownerName));
 		debugger.debugFine("Loaded bank from database for " + ownerName);
 	}
 
 	private void saveLoadedBanks()
 	{
 		List<String> oldBanks = new ArrayList<String>();
 		for (Map.Entry<String, RunsafeInventory> bank : this.loadedBanks.entrySet())
 		{
 			RunsafeInventory bankInventory = bank.getValue();
 			String ownerName = bank.getKey();
 			this.bankRepository.update(ownerName, bankInventory);
 			this.debugger.debugFine("Saved bank to database: " + ownerName);
 
 			if (bankInventory.getViewers().isEmpty())
 				oldBanks.add(ownerName);
 		}
 
 		for (String ownerName : oldBanks)
 		{
 			this.loadedBanks.remove(ownerName);
 			this.debugger.debugFine("Removing silent bank reference for GC: " + ownerName);
 		}
 	}
 
 	private void forceBanksShut()
 	{
 		for (Map.Entry<String, RunsafeInventory> bank : this.loadedBanks.entrySet())
 		{
 			for (IPlayer viewer : bank.getValue().getViewers())
 			{
 				viewer.sendColouredMessage("&cServer restarting, you have been forced out of your bank.");
 				viewer.closeInventory();
 			}
 		}
 	}
 
 	@Override
 	public void OnPluginDisabled()
 	{
 		this.debugger.logInformation("Shutdown detected, forcing save of all loaded banks.");
 		this.forceBanksShut();
 		this.saveLoadedBanks();
 	}
 
 	private HashMap<String, RunsafeInventory> loadedBanks = new HashMap<String, RunsafeInventory>();
 	private BankRepository bankRepository;
 	private IDebug debugger;
 }
