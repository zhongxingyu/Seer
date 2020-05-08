 /*
  * Copyright (c) 2009-2012 Daniel Oom, see license.txt for more info.
  */
 
 package game.misc;
 
 import game.CacheTool;
 import game.components.holdables.weapons.Weapon;
 import game.factories.WeaponFactory;
 import game.types.Orientation;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import loader.data.json.ShopData;
 import loader.data.json.types.ShopItemData;
 import loader.parser.ParserException;
 import main.Locator;
 
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.Image;
 
 public class Shop {
   private class ShopItem {
     public boolean bought;
     public final int price;
     public final Image icon, iconGray;
     public final Weapon weapon;
 
     public ShopItem(int price, Image icon, Image iconGray, Weapon weapon) {
       this.bought   = false;
       this.price    = price;
       this.icon     = icon;
       this.iconGray = iconGray;
       this.weapon   = weapon;
     }
   }
 
   private final ArrayList<ShopItem> items;
   private final Iterator<ShopItem> it;
   private ShopItem next;
 
  public Shop(ShopData shop, WeaponFactory factory)
       throws ParserException, IOException {
     items = new ArrayList<>();
 
     for (ShopItemData item : shop.items) {
       Image icon     = CacheTool.getImage(Locator.getCache(), item.icon);
       Image iconGray = CacheTool.getImage(Locator.getCache(), item.iconGray);
 
       // These weapons are for players, hence right orientation
      Weapon weapon  = factory.makeWeapon(item.weapon, Orientation.RIGHT);
 
       ShopItem tmp = new ShopItem(item.price, icon, iconGray, weapon);
       items.add(tmp);
     }
 
     it   = items.iterator();
     next = it.next();
   }
 
   public void render(Graphics g, int spacing) {
     int x = spacing;
     for (ShopItem item : items) {
       if (item.bought)
         g.drawImage(item.icon, x, spacing);
       else
         g.drawImage(item.iconGray, x, spacing);
 
       x += spacing + item.icon.getWidth();
     }
   }
 
   public boolean canAffordNext(Wallet wallet) {
     return next != null && wallet.getMoney() >= next.price;
   }
 
   public Weapon buyNext(Wallet wallet) {
     if (next == null)
       return null;
 
     if (wallet.takeMoney(next.price)) {
       next.bought = true;
       Weapon tmp = next.weapon;
 
       if (it.hasNext()) {
         next = it.next();
       } else {
         next = null;
       }
 
       return tmp;
     }
 
     return null;
   }
 }
