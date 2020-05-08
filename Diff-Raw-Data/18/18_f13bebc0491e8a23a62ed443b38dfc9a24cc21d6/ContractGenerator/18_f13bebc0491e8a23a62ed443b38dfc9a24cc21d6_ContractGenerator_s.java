 package com.icbat.game.tradesong;
 
 import com.badlogic.gdx.Gdx;
 import com.icbat.game.tradesong.gameObjects.Contract;
 import com.icbat.game.tradesong.gameObjects.Item;
 import com.icbat.game.tradesong.gameObjects.Rarity;
 
 import java.util.*;
 
 /***/
 public class ContractGenerator {
     Map<Rarity, List<Item>> prototypes = new HashMap<Rarity, List<Item>>();
     private final Random random = new Random();
 
     public ContractGenerator(Set<Item> allItemPrototypes) {
         for (Rarity rarity : Rarity.values()) {
             List<Item> itemsAtThisRarity = new ArrayList<Item>();
             for (Item item : allItemPrototypes) {
                 if (item.getRarity().equals(rarity)){
                     itemsAtThisRarity.add(item);
                 }
             }
             prototypes.put(rarity, itemsAtThisRarity);
             Gdx.app.debug("Prototype mapping, rarity: " + rarity.toString(), itemsAtThisRarity.toString());
         }
     }
 
     public Contract generateContract() {
         Rarity[] rarities = Rarity.values();
         int rarity = random.nextInt(rarities.length);
         return generateContract(rarities[rarity]);
     }
 
     public Contract generateContract(Rarity rarity) {
         List<Item> requirements = getRequirements(rarity);
         List<Item> rewards = getRewards(rarity);
         int moneyReward = getMoneyReward(rarity);
         return new Contract(rarity, requirements, rewards, moneyReward);
     }
 
     private int getMoneyReward(Rarity rarity) {
         int reward = random.nextInt(150) + 50;
         switch (rarity) {
             case COMMON:
                 return reward;
             case UNCOMMON:
                 return reward << 1;
             case RARE:
                 return reward << 2;
             case ULTRA_RARE:
                 return reward << 3;
             default:
                 return reward;
         }
     }
 
     private List<Item> getRequirements(Rarity rarity) {
         int itemsToFind = 2 + random.nextInt(2);
         return getRandomItemList(rarity, itemsToFind);
     }
 
     private List<Item> getRewards(Rarity rarity) {
         int itemsToFind = random.nextInt(2);
         if (itemsToFind > 0) {
            return getRandomItemList(rarity, itemsToFind);
         } else {
             return new ArrayList<Item>();
         }
     }
 
     private List<Item> getRandomItemList(Rarity rarity, int numberOfItems) {
         List<Item> items = new ArrayList<Item>();
 
         for (int i=0; i < numberOfItems; ++i) {
             items.add(getRandomItem(rarity));
         }
 
         return items;
     }
 
     private Item getRandomItem(Rarity rarity) {
         List<Item> itemsAtRarity = prototypes.get(rarity);
         int randomIndex = random.nextInt(itemsAtRarity.size());
         return itemsAtRarity.get(randomIndex);
     }
 
     public void makeDailyContracts() {
         List<Contract> contracts = new ArrayList<Contract>();
         int contractsToday = 3 + random.nextInt(4);
         for (int i=0; i < contractsToday; ++i) {
             Contract contract = generateContract();
             contracts.add(contract);
             Gdx.app.debug("contract generated", contract.toString());
         }
         Gdx.app.debug("Contracts for today", contracts.size() + "");
         Tradesong.contractList = contracts;
     }
 }
