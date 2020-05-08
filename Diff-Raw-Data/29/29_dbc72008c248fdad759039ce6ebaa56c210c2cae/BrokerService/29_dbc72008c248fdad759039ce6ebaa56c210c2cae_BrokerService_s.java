 /*
  * This file is part of aion-unique <aion-unique.org>.
  *
  *  aion-unique is free software: you can redistribute it and/or modify
  *  it under the terms of the GNU General Public License as published by
  *  the Free Software Foundation, either version 3 of the License, or
  *  (at your option) any later version.
  *
  *  aion-unique is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU General Public License for more details.
  *
  *  You should have received a copy of the GNU General Public License
  *  along with aion-unique.  If not, see <http://www.gnu.org/licenses/>.
  */
 package com.aionemu.gameserver.services;
 
 import java.sql.Timestamp;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Calendar;
 import java.util.Collections;
 import java.util.List;
 import java.util.Map;
 
 import javolution.util.FastMap;
 
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.log4j.Logger;
 
 import com.aionemu.commons.callbacks.EnhancedObject;
 import com.aionemu.commons.database.dao.DAOManager;
 import com.aionemu.gameserver.dao.BrokerDAO;
 import com.aionemu.gameserver.dao.InventoryDAO;
 import com.aionemu.gameserver.model.Race;
 import com.aionemu.gameserver.model.broker.BrokerItemMask;
 import com.aionemu.gameserver.model.broker.BrokerMessages;
 import com.aionemu.gameserver.model.broker.BrokerPlayerCache;
 import com.aionemu.gameserver.model.broker.BrokerRace;
 import com.aionemu.gameserver.model.gameobjects.BrokerItem;
 import com.aionemu.gameserver.model.gameobjects.Item;
 import com.aionemu.gameserver.model.gameobjects.PersistentState;
 import com.aionemu.gameserver.model.gameobjects.player.Player;
 import com.aionemu.gameserver.model.gameobjects.player.listeners.PlayerLoggedOutListener;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_BROKER_ITEMS;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_BROKER_REGISTERED_LIST;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_BROKER_REGISTRATION_SERVICE;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_BROKER_SETTLED_LIST;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_DELETE_ITEM;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_INVENTORY_UPDATE;
 import com.aionemu.gameserver.network.aion.serverpackets.SM_SYSTEM_MESSAGE;
 import com.aionemu.gameserver.taskmanager.AbstractFIFOPeriodicTaskManager;
 import com.aionemu.gameserver.utils.PacketSendUtility;
 import com.aionemu.gameserver.utils.ThreadPoolManager;
 import com.aionemu.gameserver.world.World;
 import com.google.inject.Inject;
 
 /**
  * @author kosyachok
  * @author ATracer
  * 
  */
 public class BrokerService
 {
 	private Map<Integer, BrokerItem>		elyosBrokerItems		= new FastMap<Integer, BrokerItem>().shared();
 	private Map<Integer, BrokerItem>		elyosSettledItems		= new FastMap<Integer, BrokerItem>().shared();
 	private Map<Integer, BrokerItem>		asmodianBrokerItems		= new FastMap<Integer, BrokerItem>().shared();
 	private Map<Integer, BrokerItem>		asmodianSettledItems	= new FastMap<Integer, BrokerItem>().shared();
 
 	private static final Logger				log						= Logger.getLogger(BrokerService.class);
 
 	private final int						DELAY_BROKER_SAVE		= 6000;
 	private final int						DELAY_BROKER_CHECK		= 60000;
 
 	private BrokerPeriodicTaskManager		saveManager;
 
 	@Inject
 	private World							world;
 
 	private Map<Integer, BrokerPlayerCache>	playerBrokerCache		= new FastMap<Integer, BrokerPlayerCache>().shared();
 
 	public BrokerService()
 	{
 		saveManager = new BrokerPeriodicTaskManager(DELAY_BROKER_SAVE);
 		ThreadPoolManager.getInstance().scheduleAtFixedRate(new Runnable(){
 
 			@Override
 			public void run()
 			{
 				checkExpiredItems();
 			}
 		}, DELAY_BROKER_CHECK, DELAY_BROKER_CHECK);
 	}
 
 	public void initBrokerService()
 	{
 		log.info("Loading broker...");
 		int loadedBrokerItemsCount = 0;
 		int loadedSettledItemsCount = 0;
 
 		List<BrokerItem> brokerItems = DAOManager.getDAO(BrokerDAO.class).loadBroker();
 
 		for(BrokerItem item : brokerItems)
 		{
 			if(item.getItemBrokerRace() == BrokerRace.ASMODIAN)
 			{
 				if(item.isSettled())
 				{
 					asmodianSettledItems.put(item.getItemUniqueId(), item);
 					loadedSettledItemsCount++;
 				}
 				else
 				{
 					asmodianBrokerItems.put(item.getItemUniqueId(), item);
 					loadedBrokerItemsCount++;
 				}
 			}
 			else if(item.getItemBrokerRace() == BrokerRace.ELYOS)
 			{
 				if(item.isSettled())
 				{
 					elyosSettledItems.put(item.getItemUniqueId(), item);
 					loadedSettledItemsCount++;
 				}
 				else
 				{
 					elyosBrokerItems.put(item.getItemUniqueId(), item);
 					loadedBrokerItemsCount++;
 				}
 			}
 		}
 
 		log.info("Broker loaded with " + loadedBrokerItemsCount + " broker items, " + loadedSettledItemsCount
 			+ " settled items.");
 	}
 
 	/**
 	 * 
 	 * @param player
 	 * @param clientMask
 	 * @param sortType
 	 * @param startPage
 	 */
 	public void showRequestedItems(Player player, int clientMask, int sortType, int startPage)
 	{
 		BrokerItem[] searchItems = null;
 		int playerBrokerMaskCache = getPlayerMask(player);
 		BrokerItemMask brokerMaskById = BrokerItemMask.getBrokerMaskById(clientMask);
 		boolean isChidrenMask = brokerMaskById.isChildrenMask(playerBrokerMaskCache);
 		if(getFilteredItems(player).length == 0 || !isChidrenMask)
 		{
 			searchItems = getItemsByMask(player, clientMask, false);
 		}
 		else if(isChidrenMask)
 		{
 			searchItems = getItemsByMask(player, clientMask, true);
 		}
 		else
 			searchItems = getFilteredItems(player);
 
 		if(searchItems == null || searchItems.length < 0)
 			return;
 
 		int totalSearchItemsCount = searchItems.length;
 
 		getPlayerCache(player).setBrokerSortTypeCache(sortType);
 		getPlayerCache(player).setBrokerStartPageCache(startPage);
 
 		sortBrokerItems(searchItems, sortType);
 		searchItems = getRequestedPage(searchItems, startPage);
 
 		PacketSendUtility.sendPacket(player, new SM_BROKER_ITEMS(searchItems, totalSearchItemsCount, startPage));
 	}
 
 	/**
 	 * 
 	 * @param player
 	 * @param clientMask
 	 * @return
 	 */
 	private BrokerItem[] getItemsByMask(Player player, int clientMask, boolean cached)
 	{
 		List<BrokerItem> searchItems = new ArrayList<BrokerItem>();
 
 		BrokerItemMask brokerMask = BrokerItemMask.getBrokerMaskById(clientMask);
 
 		if(cached)
 		{
 			BrokerItem[] brokerItems = getFilteredItems(player);
 			if(brokerItems == null)
 				return null;
 
 			for(BrokerItem item : brokerItems)
 			{
 				if(item == null || item.getItem() == null)
 					continue;
 
 				if(brokerMask.isMatches(item.getItem()))
 				{
 					searchItems.add(item);
 				}
 			}
 		}
 		else
 		{
 			Map<Integer, BrokerItem> brokerItems = getRaceBrokerItems(player.getCommonData().getRace());
 			if(brokerItems == null)
 				return null;
 			for(BrokerItem item : brokerItems.values())
 			{
 				if(item == null || item.getItem() == null)
 					continue;
 
 				if(brokerMask.isMatches(item.getItem()))
 				{
 					searchItems.add(item);
 				}
 			}
 		}
 
 		BrokerItem[] items = searchItems.toArray(new BrokerItem[searchItems.size()]);
 		getPlayerCache(player).setBrokerListCache(items);
 		getPlayerCache(player).setBrokerMaskCache(clientMask);
 
 		return items;
 	}
 
 	/**
 	 * Perform sorting according to sort type
 	 * 
 	 * @param brokerItems
 	 * @param sortType
 	 */
 	private void sortBrokerItems(BrokerItem[] brokerItems, int sortType)
 	{
 		Arrays.sort(brokerItems, BrokerItem.getComparatoryByType(sortType));
 	}
 
 	/**
 	 * 
 	 * @param brokerItems
 	 * @param startPage
 	 * @return
 	 */
 	private BrokerItem[] getRequestedPage(BrokerItem[] brokerItems, int startPage)
 	{
 		List<BrokerItem> page = new ArrayList<BrokerItem>();
 		int startingElement = startPage * 9;
 
 		for(int i = startingElement, limit = 0; i < brokerItems.length && limit < 45; i++, limit++)
 		{
 			page.add(brokerItems[i]);
 		}
 
 		return page.toArray(new BrokerItem[page.size()]);
 	}
 
 	/**
 	 * 
 	 * @param race
 	 * @return
 	 */
 	private Map<Integer, BrokerItem> getRaceBrokerItems(Race race)
 	{
 		switch(race)
 		{
 			case ELYOS:
 				return elyosBrokerItems;
 			case ASMODIANS:
 				return asmodianBrokerItems;
 			default:
 				return null;
 		}
 	}
 
 	/**
 	 * 
 	 * @param race
 	 * @return
 	 */
 	private Map<Integer, BrokerItem> getRaceBrokerSettledItems(Race race)
 	{
 		switch(race)
 		{
 			case ELYOS:
 				return elyosSettledItems;
 			case ASMODIANS:
 				return asmodianSettledItems;
 			default:
 				return null;
 		}
 	}
 
 	/**
 	 * 
 	 * @param player
 	 * @param itemUniqueId
 	 */
 	public void buyBrokerItem(Player player, int itemUniqueId)
 	{
 		if(player.getInventory().isFull())
 		{
 			PacketSendUtility.sendPacket(player, SM_SYSTEM_MESSAGE.MSG_FULL_INVENTORY);
 			return;
 		}
 
 		boolean isEmptyCache = getFilteredItems(player).length == 0;
 		Race playerRace = player.getCommonData().getRace();
 
 		BrokerItem buyingItem = getRaceBrokerItems(playerRace).get(itemUniqueId);
 
 		if(buyingItem == null)
 			return; // TODO: Message "this item has already been bought, refresh page please."
 
 		Item item = buyingItem.getItem();
 		int price = buyingItem.getPrice();
 
 		if(player.getInventory().getKinahItem().getItemCount() < price)
 			return;
 
 		getRaceBrokerItems(playerRace).remove(itemUniqueId);
 		putToSettled(playerRace, buyingItem, true);
 
 		if(!isEmptyCache)
 		{
 			BrokerItem[] newCache = (BrokerItem[]) ArrayUtils.removeElement(getFilteredItems(player), buyingItem);
 			getPlayerCache(player).setBrokerListCache(newCache);
 		}
 
 		player.getInventory().decreaseKinah(price);
 		Item boughtItem = player.getInventory().putToBag(item);
 
 		// create save task
 		BrokerOpSaveTask bost = new BrokerOpSaveTask(buyingItem, boughtItem, player.getInventory().getKinahItem(),
 			player.getObjectId());
 		saveManager.add(bost);
 
 		PacketSendUtility.sendPacket(player, new SM_INVENTORY_UPDATE(Collections.singletonList(boughtItem)));
 
 		showRequestedItems(player, getPlayerCache(player).getBrokerMaskCache(), getPlayerCache(player)
 			.getBrokerSortTypeCache(), getPlayerCache(player).getBrokerStartPageCache());
 	}
 
 	/**
 	 * 
 	 * @param race
 	 * @param brokerItem
 	 * @param isSold
 	 */
 	private void putToSettled(Race race, BrokerItem brokerItem, boolean isSold)
 	{
 		if(isSold)
 			brokerItem.removeItem();
 		else
 			brokerItem.setSettled();
 
 		brokerItem.setPersistentState(PersistentState.UPDATE_REQUIRED);
 
 		switch(race)
 		{
 			case ASMODIANS:
 				asmodianSettledItems.put(brokerItem.getItemUniqueId(), brokerItem);
 				break;
 
 			case ELYOS:
 				elyosSettledItems.put(brokerItem.getItemUniqueId(), brokerItem);
 				break;
 		}
 
 		Player seller = world.findPlayer(brokerItem.getSellerId());
 
 		saveManager.add(new BrokerOpSaveTask(brokerItem));
 
 		if(seller != null)
 		{
 			PacketSendUtility.sendPacket(seller, new SM_BROKER_SETTLED_LIST(true));
 			// TODO: Retail system message
 		}
 	}
 
 	/**
 	 * 
 	 * @param player
 	 * @param itemUniqueId
 	 * @param price
 	 */
 	public void registerItem(Player player, int itemUniqueId, int price)
 	{
 		Item itemToRegister = player.getInventory().getItemByObjId(itemUniqueId);
 		Race playerRace = player.getCommonData().getRace();
 
 		if(itemToRegister == null)
 			return;
 
 		BrokerRace brRace;
 
 		if(playerRace == Race.ASMODIANS)
 			brRace = BrokerRace.ASMODIAN;
 		else if(playerRace == Race.ELYOS)
 			brRace = BrokerRace.ELYOS;
 		else
 			return;
 
 		int registrationCommition = Math.round(price * 0.02f);
 
 		if(registrationCommition < 10)
 			registrationCommition = 10;
 
 		if(player.getInventory().getKinahItem().getItemCount() < registrationCommition)
 		{
 			PacketSendUtility.sendPacket(player, new SM_BROKER_REGISTRATION_SERVICE(BrokerMessages.NO_ENOUGHT_KINAH
 				.getId()));
 		}
 
 		player.getInventory().decreaseKinah(registrationCommition);
 
 		player.getInventory().removeFromBag(itemToRegister, false);
 		PacketSendUtility.sendPacket(player, new SM_DELETE_ITEM(itemToRegister.getObjectId()));
 
 		itemToRegister.setItemLocation(126);
 
 		BrokerItem newBrokerItem = new BrokerItem(itemToRegister, price, player.getName(), player.getObjectId(), brRace);
 
 		switch(brRace)
 		{
 			case ASMODIAN:
 				asmodianBrokerItems.put(newBrokerItem.getItemUniqueId(), newBrokerItem);
 				break;
 
 			case ELYOS:
 				elyosBrokerItems.put(newBrokerItem.getItemUniqueId(), newBrokerItem);
 				break;
 		}
 
 		BrokerOpSaveTask bost = new BrokerOpSaveTask(newBrokerItem, itemToRegister, player.getInventory()
 			.getKinahItem(), player.getObjectId());
 		saveManager.add(bost);
 
 		PacketSendUtility.sendPacket(player, new SM_BROKER_REGISTRATION_SERVICE(newBrokerItem));
 	}
 
 	/**
 	 * 
 	 * @param player
 	 */
 	public void showRegisteredItems(Player player)
 	{
 		Map<Integer, BrokerItem> brokerItems = getRaceBrokerItems(player.getCommonData().getRace());
 
 		List<BrokerItem> registeredItems = new ArrayList<BrokerItem>();
 		int playerId = player.getObjectId();
 
 		for(BrokerItem item : brokerItems.values())
 		{
 			if(item != null && item.getItem() != null && playerId == item.getSellerId())
 				registeredItems.add(item);
 		}
 
 		PacketSendUtility.sendPacket(player, new SM_BROKER_REGISTERED_LIST(registeredItems));
 	}
 
 	/**
 	 * 
 	 * @param player
 	 * @param brokerItemId
 	 */
 	public void cancelRegisteredItem(Player player, int brokerItemId)
 	{
 		Map<Integer, BrokerItem> brokerItems = getRaceBrokerItems(player.getCommonData().getRace());
 		BrokerItem brokerItem = brokerItems.get(brokerItemId);
 
 		if(brokerItem != null)
 		{
 			Item item = player.getInventory().putToBag(brokerItem.getItem());
 			PacketSendUtility.sendPacket(player, new SM_INVENTORY_UPDATE(Collections.singletonList(item)));
 			brokerItem.setPersistentState(PersistentState.DELETED);
 			saveManager.add(new BrokerOpSaveTask(brokerItem));
 			brokerItems.remove(brokerItemId);
 		}
 		showRegisteredItems(player);
 	}
 
 	/**
 	 * 
 	 * @param player
 	 */
 	public void showSettledItems(Player player)
 	{
 		Map<Integer, BrokerItem> brokerSettledItems = getRaceBrokerSettledItems(player.getCommonData().getRace());
 
 		List<BrokerItem> settledItems = new ArrayList<BrokerItem>();
 
 		int playerId = player.getObjectId();
 		int totalKinah = 0;
 
 		for(BrokerItem item : brokerSettledItems.values())
 		{
 			if(item != null && playerId == item.getSellerId())
 			{
 				settledItems.add(item);
 
 				if(item.isSold())
 					totalKinah += item.getPrice();
 			}
 		}
 
 		PacketSendUtility.sendPacket(player, new SM_BROKER_SETTLED_LIST(settledItems, totalKinah));
 	}
 
 	/**
 	 * 
 	 * @param player
 	 */
 	public void settleAccount(Player player)
 	{
 		Race playerRace = player.getCommonData().getRace();
 		Map<Integer, BrokerItem> brokerSettledItems = getRaceBrokerSettledItems(playerRace);
 		List<BrokerItem> collectedItems = new ArrayList<BrokerItem>();
 		int playerId = player.getObjectId();
 		int kinahCollect = 0;
 		boolean itemsLeft = false;
 
 		for(BrokerItem item : brokerSettledItems.values())
 		{
 			if(item.getSellerId() == playerId)
 				collectedItems.add(item);
 		}
 
 		for(BrokerItem item : collectedItems)
 		{
 			if(item.isSold())
 			{
 				boolean result = false;
 				switch(playerRace)
 				{
 					case ASMODIANS:
 						result = asmodianSettledItems.remove(item.getItemUniqueId()) != null;
 						break;
 					case ELYOS:
 						result = elyosSettledItems.remove(item.getItemUniqueId()) != null;
 						break;
 				}
 
 				if(result)
 				{
 					item.setPersistentState(PersistentState.DELETED);
 					saveManager.add(new BrokerOpSaveTask(item));
 					kinahCollect += item.getPrice();
 				}
 			}
 			else
 			{
 				if(item.getItem() != null)
 				{
 					Item resultItem = player.getInventory().putToBag(item.getItem());
 					if(resultItem != null)
 					{
 						switch(playerRace)
 						{
 							case ASMODIANS:
								asmodianSettledItems.remove(item.getItemUniqueId());
 								break;
 							case ELYOS:
								elyosSettledItems.remove(item.getItemUniqueId());
 								break;
 						}
						saveManager.add(new BrokerOpSaveTask(item));
						PacketSendUtility.sendPacket(player, new SM_INVENTORY_UPDATE(Collections
							.singletonList(resultItem)));
 					}
 					else
 						itemsLeft = true;
 
 				}
 				else
 					log.warn("Broker settled item missed. ObjID: " + item.getItemUniqueId());
 			}
 		}
 
 		player.getInventory().increaseKinah(kinahCollect);
 
 		showSettledItems(player);
 
 		if(!itemsLeft)
 			PacketSendUtility.sendPacket(player, new SM_BROKER_SETTLED_LIST(false));
 
 	}
 
 	private void checkExpiredItems()
 	{
 		Map<Integer, BrokerItem> asmoBrokerItems = getRaceBrokerItems(Race.ASMODIANS);
 		Map<Integer, BrokerItem> elyosBrokerItems = getRaceBrokerItems(Race.ELYOS);
 
 		Timestamp currentTime = new Timestamp(Calendar.getInstance().getTimeInMillis());
 
		for(int i = 0; i < asmoBrokerItems.size(); i++)
 		{
			BrokerItem item = asmoBrokerItems.get(i);
 			if(item != null && item.getExpireTime().getTime() <= currentTime.getTime())
 			{
 				putToSettled(Race.ASMODIANS, item, false);
 				asmodianBrokerItems.remove(item.getItemUniqueId());
 			}
 		}
 
		for(int i = 0; i < elyosBrokerItems.size(); i++)
 		{
			BrokerItem item = elyosBrokerItems.get(i);
 			if(item != null && item.getExpireTime().getTime() <= currentTime.getTime())
 			{
 				putToSettled(Race.ELYOS, item, false);
 				this.elyosBrokerItems.remove(item.getItemUniqueId());
 			}
 		}
 	}
 
 	/**
 	 * 
 	 * @param player
 	 */
 	public void onPlayerLogin(Player player)
 	{
 		Map<Integer, BrokerItem> brokerSettledItems = getRaceBrokerSettledItems(player.getCommonData().getRace());
 
 		int playerId = player.getObjectId();
 
 		for(BrokerItem item : brokerSettledItems.values())
 		{
 			if(item != null && playerId == item.getSellerId())
 			{
 				PacketSendUtility.sendPacket(player, new SM_BROKER_SETTLED_LIST(true));
 				break;
 			}
 		}
 	}
 	
 	/**
 	 * 
 	 * @param player
 	 * @return
 	 */
 	private BrokerPlayerCache getPlayerCache(Player player)
 	{
 		BrokerPlayerCache cacheEntry = playerBrokerCache.get(player.getObjectId());
 		if(cacheEntry == null)
 		{
 			cacheEntry = new BrokerPlayerCache();
 			playerBrokerCache.put(player.getObjectId(), cacheEntry);
 			((EnhancedObject)player).addCallback(new PlayerLoggedOutListener(){
 				
 				@Override
 				protected void onLoggedOut(Player loggedOutPlayer)
 				{
 					playerBrokerCache.remove(loggedOutPlayer.getObjectId());
 					
 				}
 			});
 		}
 		return cacheEntry;
 	}
 
 	/**
 	 * 
 	 * @param player
 	 * @return
 	 */
 	private int getPlayerMask(Player player)
 	{
 		return getPlayerCache(player).getBrokerMaskCache();
 	}
 
 	/**
 	 * 
 	 * @param player
 	 * @return
 	 */
 	private BrokerItem[] getFilteredItems(Player player)
 	{
 		return getPlayerCache(player).getBrokerListCache();
 	}
 
 	/**
 	 * Frequent running save task
 	 */
 	public static final class BrokerPeriodicTaskManager extends AbstractFIFOPeriodicTaskManager<BrokerOpSaveTask>
 	{
 		private static final String	CALLED_METHOD_NAME	= "brokerOperation()";
 
 		/**
 		 * @param period
 		 */
 		public BrokerPeriodicTaskManager(int period)
 		{
 			super(period);
 		}
 
 		@Override
 		protected void callTask(BrokerOpSaveTask task)
 		{
 			task.run();
 		}
 
 		@Override
 		protected String getCalledMethodName()
 		{
 			return CALLED_METHOD_NAME;
 		}
 	}
 
 	/**
 	 * This class is used for storing all items in one shot after any broker operation
 	 */
 	public static final class BrokerOpSaveTask implements Runnable
 	{
 		private BrokerItem	brokerItem;
 		private Item		item;
 		private Item		kinahItem;
 		private int			playerId;
 
 		/**
 		 * 
 		 * @param brokerItem
 		 * @param item
 		 * @param kinahItem
 		 * @param playerId
 		 */
 		private BrokerOpSaveTask(BrokerItem brokerItem, Item item, Item kinahItem, int playerId)
 		{
 			this.brokerItem = brokerItem;
 			this.item = item;
 			this.kinahItem = kinahItem;
 			this.playerId = playerId;
 		}
 
 		/**
 		 * @param brokerItem
 		 */
 		public BrokerOpSaveTask(BrokerItem brokerItem)
 		{
 			this.brokerItem = brokerItem;
 		}
 
 		@Override
 		public void run()
 		{
 			if(brokerItem != null)
 				DAOManager.getDAO(BrokerDAO.class).store(brokerItem);
 			if(item != null)
 				DAOManager.getDAO(InventoryDAO.class).store(item, playerId);
 			if(kinahItem != null)
 				DAOManager.getDAO(InventoryDAO.class).store(kinahItem, playerId);
 		}
 	}
 }
