 package com.teammetallurgy.agriculture;
 
 import codechicken.nei.ItemList;
 import net.minecraft.item.Item;
 import net.minecraft.item.crafting.FurnaceRecipes;
 import net.minecraft.util.WeightedRandomChestContent;
 import net.minecraft.world.MinecraftException;
 import net.minecraftforge.common.ChestGenHooks;
 import cpw.mods.fml.common.registry.GameRegistry;
 import cpw.mods.fml.common.registry.LanguageRegistry;
 
 public class AgricultureItems
 {
     public static SubItem ovenRack;
     public static SubItem flour;
     public static SubItem dough;
 
     public static SubItem candiedApple;
     public static SubItem water;
     public static SubItem hotCocoa;
     public static SubItem vinegar;
     public static SubItem beer;
     public static SubItem milk;
     public static SubItem rawHamburgerPatty;
     public static SubItem appleJellyToast;
     public static SubItem cinnamonToast;
     public static SubItem strawberryJellyToast;
     public static SubItem caramelAppleWithNuts;
     public static SubItem appleJelly;
     public static SubItem mashedPotatos;
     public static SubItem carrotCakeBatter;
     public static SubItem rawFrenchToast;
     public static SubItem batter;
     public static SubItem macaroniAndCheese;
     public static SubItem strawberryJelly;
     public static SubItem rawApplePie;
     public static SubItem rawStrawberryPie;
     public static SubItem strawberryShortcake;
     public static SubItem hamburger;
     public static SubItem pbjSandwichApple;
     public static SubItem pbjSandwichStrawberry;
     public static SubItem cheeseSandwich;
     public static SubItem butteredToast;
     public static SubItem baconCheeseFries;
     public static SubItem baconCheeseburger;
     public static SubItem peanutButter;
     public static SubItem chocolate;
     public static SubItem cheese;
     public static SubItem butter;
     public static SubItem whippedCream;
     // public static SubItem dough;
     public static SubItem appleGelatin;
     public static SubItem strawberryGelatin;
     public static SubItem marshmellows;
     public static SubItem pastaDough;
     public static SubItem cheeseFries;
     public static SubItem rawChickenNuggets;
     public static SubItem cheeseburger;
     public static SubItem deluxeHotCocoa;
     public static SubItem saltedBeef;
     public static SubItem breadedChicken;
     public static SubItem saltedPork;
     public static SubItem caramelApple;
     public static SubItem chocolateCoveredStrawberries;
     public static SubItem cinnamonAndSugar;
     public static SubItem sliceOfCheese;
     public static SubItem sliceOfBread;
     public static SubItem friedChicken;
     public static SubItem frenchFries;
     public static SubItem chickenNuggets;
     public static SubItem shortcake;
     public static SubItem carrotCake;
     public static SubItem grilledCheese;
     public static SubItem loafOfBread;
     public static SubItem pasta;
     public static SubItem toastedPbjSandwichApple;
     public static SubItem toastedPbjSandwichStrawberry;
     public static SubItem roastedPeanuts;
     public static SubItem applePie;
     public static SubItem frenchToast;
     public static SubItem hamburgerPatty;
     public static SubItem strawberryPie;
     public static SubItem beefJerkey;
     public static SubItem bacon;
     public static SubItem toast;
     public static SubItem caramel;
     public static SubItem appleMush;
     public static SubItem gelatin;
     public static SubItem breadCrumbs;
     public static SubItem groundBeef;
     public static SubItem groundChicken;
     public static SubItem groundPork;
     public static SubItem crushedPeanuts;
     public static SubItem strawberryMush;
     // public static SubItem flour;
     public static SubItem cookingOil;
     public static SubItem strawberry;
     public static SubItem salt;
     public static SubItem groundCinnamon;
     public static SubItem cinnamon;
     public static SubItem peanuts;
 
     public static SubItem appleCinnamonCookieDough;
     public static SubItem butterCookieDough;
     public static SubItem bowlOfRawPasta;
     public static SubItem cheesyBaconPotatoes;
     public static SubItem cheesyPotatoes;
     public static SubItem chocolateChipCookieDough;
     public static SubItem chocolateIceCreamChocolateSauce;
     public static SubItem chocolateIceCreamMix;
     public static SubItem dicedPotatoes;
     public static SubItem doubleBaconCheeseburger;
     public static SubItem iceCreamMix;
     public static SubItem pbSandwich;
     public static SubItem peanutButterCookieDough;
     public static SubItem pumpkinCookieDough;
     public static SubItem snickerDoodleDough;
     public static SubItem strawberryIceCreamChocolateSauce;
     public static SubItem strawberryIceCreamMix;
     public static SubItem strawberryJellyCookieDough;
     public static SubItem sugarCookieDough;
     public static SubItem vanillaIceCreamChocolateSauce;
     public static SubItem vanillaIceCreamMix;
     // IceBox
     public static SubItem chocolateIceCream;
     public static SubItem strawberryIceCream;
     public static SubItem vanillaIceCream;
     // Oven
     public static SubItem appleCinnamonCookie;
     public static SubItem butterCookie;
     public static SubItem chocolateChipCookie;
     public static SubItem chocolateSauce;
     public static SubItem peanutButterCookie;
     public static SubItem pumpkinCookie;
     public static SubItem roastedMarshmellows;
     public static SubItem snickerDoodle;
     public static SubItem strawberryJellyCookie;
     public static SubItem sugarCookie;
     public static SubItem toastedPBSandwich;
     // Processor
 
     public static SubItem appleCinnamonCookieBurned;
     public static SubItem applePieBurned;
     public static SubItem beefJerkeyBurned;
     public static SubItem baconBurned;
     public static SubItem butterCookieBurned;
     public static SubItem caramelBurned;
     public static SubItem carrotCakeBurned;
     public static SubItem chocolateChipCookieBurned;
     public static SubItem chocolateSauceBurned;
     public static SubItem frenchToastBurned;
     public static SubItem grilledCheeseBurned;
     public static SubItem hamburgerPattyBurned;
     public static SubItem loafOfBreadBurned;
     public static SubItem pastaBurned;
     public static SubItem peanutButterCookieBurned;
     public static SubItem pumpkinCookieBurned;
     public static SubItem roastedMarshmellowsBurned;
     public static SubItem roastedPeanutsBurned;
     public static SubItem snickerDoodleBurned;
     public static SubItem strawberryJellyCookieBurned;
     public static SubItem strawberryPieBurned;
     public static SubItem shortcakeBurned;
     public static SubItem sugarCookieBurned;
     public static SubItem toastBurned;
     public static SubItem toastedPBJSandwichAppleBurned;
     public static SubItem toastedPBJSandwichStrawberryBurned;
     public static SubItem toastedPBSandwichBurned;
 
     public static SubItem clayBowl;
     public static SubItem clayPlate;
     public static SubItem clayCup;
     public static SubItem ceramicBowl;
     public static SubItem ceramicPlate;
     public static SubItem ceramicCup;
     public static SubItem vanilla;
 
     public static int dishID = ConfigHandler.getItemId("Dish", 19999);
     public static int foodID = ConfigHandler.getItemId("Food", 20000);
 
     public static void addNames()
     {
         LanguageRegistry.addName(ovenRack.getItemStack(), "Oven Rack");
 
         LanguageRegistry.addName(clayBowl.getItemStack(), "Clay Bowl");
         LanguageRegistry.addName(clayPlate.getItemStack(), "Clay Plate");
         LanguageRegistry.addName(clayCup.getItemStack(), "Clay Cup");
         LanguageRegistry.addName(ceramicBowl.getItemStack(), "Ceramic Bowl");
         LanguageRegistry.addName(ceramicPlate.getItemStack(), "Ceramic Plate");
         LanguageRegistry.addName(ceramicCup.getItemStack(), "Ceramic Cup");
 
         LanguageRegistry.addName(candiedApple.getItemStack(), "Candied Apple");
         LanguageRegistry.addName(water.getItemStack(), "Cup of Water");
         LanguageRegistry.addName(milk.getItemStack(), "Cup of Milk");
         LanguageRegistry.addName(hotCocoa.getItemStack(), "Hot Cocoa");
         LanguageRegistry.addName(vinegar.getItemStack(), "Vinegar");
         LanguageRegistry.addName(beer.getItemStack(), "Beer");
         LanguageRegistry.addName(rawHamburgerPatty.getItemStack(), "Raw Hamburger Patty");
         LanguageRegistry.addName(appleJellyToast.getItemStack(), "Apple Jelly Toast");
         LanguageRegistry.addName(cinnamonToast.getItemStack(), "Cinnamon Toast");
         LanguageRegistry.addName(strawberryJellyToast.getItemStack(), "Strawberry Jelly Toast");
         LanguageRegistry.addName(caramelAppleWithNuts.getItemStack(), "Caramel Apple with Nuts");
         LanguageRegistry.addName(appleJelly.getItemStack(), "Apple Jelly");
         LanguageRegistry.addName(mashedPotatos.getItemStack(), "Mashed Potatos");
         LanguageRegistry.addName(carrotCakeBatter.getItemStack(), "Carrot Cake Batter");
         LanguageRegistry.addName(rawFrenchToast.getItemStack(), "Raw French Toast");
         LanguageRegistry.addName(batter.getItemStack(), "Batter");
         LanguageRegistry.addName(macaroniAndCheese.getItemStack(), "Macaroni and Cheese");
         LanguageRegistry.addName(strawberryJelly.getItemStack(), "Strawberry Jelly");
         LanguageRegistry.addName(rawApplePie.getItemStack(), "Raw Apple Pie");
         LanguageRegistry.addName(rawStrawberryPie.getItemStack(), "Raw Strawberry Pie");
         LanguageRegistry.addName(strawberryShortcake.getItemStack(), "Strawberry Shortcake");
         LanguageRegistry.addName(hamburger.getItemStack(), "Hamburger");
         LanguageRegistry.addName(pbjSandwichApple.getItemStack(), "PB&J Sandwich (Apple)");
         LanguageRegistry.addName(pbjSandwichStrawberry.getItemStack(), "PB&J Sandwich (Strawberry)");
         LanguageRegistry.addName(cheeseSandwich.getItemStack(), "Cheese Sandwich");
         LanguageRegistry.addName(butteredToast.getItemStack(), "Buttered Toast");
         LanguageRegistry.addName(baconCheeseFries.getItemStack(), "Bacon Cheese Fries");
         LanguageRegistry.addName(baconCheeseburger.getItemStack(), "Bacon Cheeseburger");
         LanguageRegistry.addName(chocolate.getItemStack(), "Chocolate");
         LanguageRegistry.addName(cheese.getItemStack(), "Cheese");
         LanguageRegistry.addName(butter.getItemStack(), "Butter");
         LanguageRegistry.addName(whippedCream.getItemStack(), "Whipped Cream");
         LanguageRegistry.addName(dough.getItemStack(), "Dough");
         LanguageRegistry.addName(appleGelatin.getItemStack(), "Apple Gelatin");
         LanguageRegistry.addName(strawberryGelatin.getItemStack(), "Strawberry Gelatin");
         LanguageRegistry.addName(marshmellows.getItemStack(), "Marshmellows");
         LanguageRegistry.addName(pastaDough.getItemStack(), "Pasta Dough");
         LanguageRegistry.addName(cheeseFries.getItemStack(), "Cheese Fries");
         LanguageRegistry.addName(rawChickenNuggets.getItemStack(), "Raw Chicken Nuggets");
         LanguageRegistry.addName(cheeseburger.getItemStack(), "Cheeseburger");
         LanguageRegistry.addName(deluxeHotCocoa.getItemStack(), "Deluxe Hot Cocoa");
         LanguageRegistry.addName(saltedBeef.getItemStack(), "Salted Beef");
         LanguageRegistry.addName(breadedChicken.getItemStack(), "Breaded Chicken");
         LanguageRegistry.addName(saltedPork.getItemStack(), "Salted Pork");
         LanguageRegistry.addName(caramelApple.getItemStack(), "Caramel Apple");
         LanguageRegistry.addName(chocolateCoveredStrawberries.getItemStack(), "Chocolate-Covered Strawberries");
         LanguageRegistry.addName(cinnamonAndSugar.getItemStack(), "Cinnamon and Sugar");
         LanguageRegistry.addName(sliceOfCheese.getItemStack(), "Slice of Cheese");
         LanguageRegistry.addName(sliceOfBread.getItemStack(), "Slice of Bread");
         LanguageRegistry.addName(friedChicken.getItemStack(), "Fried Chicken");
         LanguageRegistry.addName(frenchFries.getItemStack(), "French Fries");
         LanguageRegistry.addName(chickenNuggets.getItemStack(), "Chicken Nuggets");
         LanguageRegistry.addName(shortcake.getItemStack(), "Shortcake");
         LanguageRegistry.addName(carrotCake.getItemStack(), "Carrot Cake");
         LanguageRegistry.addName(grilledCheese.getItemStack(), "Grilled Cheese");
         LanguageRegistry.addName(loafOfBread.getItemStack(), "Loaf of Bread");
         LanguageRegistry.addName(pasta.getItemStack(), "Pasta");
         LanguageRegistry.addName(toastedPbjSandwichApple.getItemStack(), "Toasted PB&J Sandwich (Apple)");
         LanguageRegistry.addName(toastedPbjSandwichStrawberry.getItemStack(), "Toasted PB&J Sandwich (Strawberry)");
         LanguageRegistry.addName(roastedPeanuts.getItemStack(), "Roasted Peanuts");
         LanguageRegistry.addName(applePie.getItemStack(), "Apple Pie");
         LanguageRegistry.addName(frenchToast.getItemStack(), "French Toast");
         LanguageRegistry.addName(hamburgerPatty.getItemStack(), "Hamburger Patty");
         LanguageRegistry.addName(strawberryPie.getItemStack(), "Strawberry Pie");
         LanguageRegistry.addName(beefJerkey.getItemStack(), "Beef Jerkey");
         LanguageRegistry.addName(bacon.getItemStack(), "Bacon");
         LanguageRegistry.addName(toast.getItemStack(), "Toast");
         LanguageRegistry.addName(caramel.getItemStack(), "Caramel");
         LanguageRegistry.addName(appleMush.getItemStack(), "Apple Mush");
         LanguageRegistry.addName(gelatin.getItemStack(), "Gelatin");
         LanguageRegistry.addName(breadCrumbs.getItemStack(), "Bread Crumbs");
         LanguageRegistry.addName(groundBeef.getItemStack(), "Ground Beef");
         LanguageRegistry.addName(groundChicken.getItemStack(), "Ground Chicken");
         LanguageRegistry.addName(groundPork.getItemStack(), "Ground Pork");
         LanguageRegistry.addName(crushedPeanuts.getItemStack(), "Crushed Peanuts");
         LanguageRegistry.addName(strawberryMush.getItemStack(), "Strawberry Mush");
         LanguageRegistry.addName(flour.getItemStack(), "Flour");
         LanguageRegistry.addName(peanutButter.getItemStack(), "Peanut Butter");
         LanguageRegistry.addName(cookingOil.getItemStack(), "Cooking Oil");
         LanguageRegistry.addName(salt.getItemStack(), "Salt");
         LanguageRegistry.addName(cinnamon.getItemStack(), "Cinnamon");
         LanguageRegistry.addName(groundCinnamon.getItemStack(), "Ground Cinnamon");
         LanguageRegistry.addName(peanuts.getItemStack(), "Peanuts");
 
         LanguageRegistry.addName(appleCinnamonCookieDough.getItemStack(), "Apple Cinnamon Cookie Dough");
         LanguageRegistry.addName(bowlOfRawPasta.getItemStack(), "Bowl of Raw Pasta");
         LanguageRegistry.addName(butterCookieDough.getItemStack(), "Butter Cookie Dough");
         LanguageRegistry.addName(cheesyBaconPotatoes.getItemStack(), "Cheesy Bacon Potatoes");
         LanguageRegistry.addName(cheesyPotatoes.getItemStack(), "Cheesy Potatoes");
         LanguageRegistry.addName(chocolateChipCookieDough.getItemStack(), "Chocolate Chip Cookie Dough");
         LanguageRegistry.addName(chocolateIceCreamChocolateSauce.getItemStack(), "Chocolate Ice Cream (Chocolate Sauce)");
         LanguageRegistry.addName(chocolateIceCreamMix.getItemStack(), "Chocolate Ice Cream Mix");
         LanguageRegistry.addName(dicedPotatoes.getItemStack(), "Diced Potatoes");
         LanguageRegistry.addName(doubleBaconCheeseburger.getItemStack(), "Double Bacon Cheeseburger");
         LanguageRegistry.addName(iceCreamMix.getItemStack(), "Ice Cream Mix");
         LanguageRegistry.addName(pbSandwich.getItemStack(), "PB Sandwich");
         LanguageRegistry.addName(peanutButterCookieDough.getItemStack(), "Peanut Butter Cooke Dough");
         LanguageRegistry.addName(pumpkinCookieDough.getItemStack(), "Pumpkin Cookie Dough");
         LanguageRegistry.addName(snickerDoodleDough.getItemStack(), "Snickerdoodle Dough");
         LanguageRegistry.addName(strawberryIceCreamChocolateSauce.getItemStack(), "Strawberry Ice Cream (Chocolate Sauce)");
         LanguageRegistry.addName(strawberryIceCreamMix.getItemStack(), "Strawberry Ice Cream Mix");
         LanguageRegistry.addName(strawberryJellyCookieDough.getItemStack(), "Strawberry Jelly Cookie Dough");
         LanguageRegistry.addName(sugarCookieDough.getItemStack(), "Sugar Cookie Dough");
         LanguageRegistry.addName(vanillaIceCreamChocolateSauce.getItemStack(), "Vanilla Ice Cream (Chocolate Sauce)");
         LanguageRegistry.addName(vanillaIceCreamMix.getItemStack(), "Vanilla Ice Cream Mix");
         LanguageRegistry.addName(chocolateIceCream.getItemStack(), "Chocolate Ice Cream");
         LanguageRegistry.addName(strawberryIceCream.getItemStack(), "Strawberry Ice Cream");
         LanguageRegistry.addName(vanillaIceCream.getItemStack(), "Vanilla Ice Cream");
         LanguageRegistry.addName(appleCinnamonCookie.getItemStack(), "Apple Cinnamon Cookie");
         LanguageRegistry.addName(butterCookie.getItemStack(), "Butter Cookie");
         LanguageRegistry.addName(chocolateChipCookie.getItemStack(), "Chocolate Chip Cookie");
         LanguageRegistry.addName(chocolateSauce.getItemStack(), "Chocolate Sauce");
         LanguageRegistry.addName(peanutButterCookie.getItemStack(), "Peanut Butter Cookie");
         LanguageRegistry.addName(pumpkinCookie.getItemStack(), "Pumpkin Cookie");
         LanguageRegistry.addName(roastedMarshmellows.getItemStack(), "Roasted Marshmellows");
         LanguageRegistry.addName(snickerDoodle.getItemStack(), "Snickerdoodle");
         LanguageRegistry.addName(strawberryJellyCookie.getItemStack(), "Strawberry Jelly Cookie");
         LanguageRegistry.addName(sugarCookie.getItemStack(), "Sugar Cookie");
         LanguageRegistry.addName(toastedPBSandwich.getItemStack(), "Toasted PB Sandwich");
         LanguageRegistry.addName(vanilla.getItemStack(), "Vanilla");
 
         LanguageRegistry.addName(appleCinnamonCookieBurned.getItemStack(), "Apple Cinnamon Cooke (Burned)");
         LanguageRegistry.addName(applePieBurned.getItemStack(), "Apple Pie (Burned)");
         LanguageRegistry.addName(baconBurned.getItemStack(), "Bacon (Burned)");
         LanguageRegistry.addName(beefJerkeyBurned.getItemStack(), "Beef Jerkey (Burned)");
         LanguageRegistry.addName(butterCookieBurned.getItemStack(), "Butter Cookie (Burned)");
         LanguageRegistry.addName(caramelBurned.getItemStack(), "Caramel (Burned)");
         LanguageRegistry.addName(carrotCake.getItemStack(), "Carrot Cake (Burned)");
         LanguageRegistry.addName(chocolateChipCookieBurned.getItemStack(), "Chocolate Chip Cookie (Burned)");
         LanguageRegistry.addName(chocolateSauceBurned.getItemStack(), "Chocolate Sauce (Burned)");
         LanguageRegistry.addName(frenchToastBurned.getItemStack(), "French Toast (Burned)");
         LanguageRegistry.addName(grilledCheeseBurned.getItemStack(), "Grilled Cheese (Burned)");
         LanguageRegistry.addName(hamburgerPattyBurned.getItemStack(), "Hambuerger Patty (Burned)");
         LanguageRegistry.addName(loafOfBreadBurned.getItemStack(), "Loaf of Bread (Burned)");
         LanguageRegistry.addName(pastaBurned.getItemStack(), "Pasta (Burned)");
         LanguageRegistry.addName(peanutButterCookieBurned.getItemStack(), "Peanut Butter Cookie (Burned)");
         LanguageRegistry.addName(pumpkinCookieBurned.getItemStack(), "Pumpkin Cookie (Burned)");
         LanguageRegistry.addName(roastedMarshmellowsBurned.getItemStack(), "Roasted Marshmellows (Burned)");
         LanguageRegistry.addName(shortcakeBurned.getItemStack(), "Shortcake (Burned)");
         LanguageRegistry.addName(snickerDoodleBurned.getItemStack(), "Snickerdoodle (Burned)");
         LanguageRegistry.addName(strawberryJellyCookieBurned.getItemStack(), "Strawberry Jelly Cookie (Burned)");
         LanguageRegistry.addName(roastedPeanutsBurned.getItemStack(), "Roasted Peanuts (Burned)");
         LanguageRegistry.addName(strawberryPieBurned.getItemStack(), "Strawberry Pie (Burned)");
         LanguageRegistry.addName(sugarCookieBurned.getItemStack(), "Sugar Cookie (Burned)");
         LanguageRegistry.addName(toastBurned.getItemStack(), "Toast (Burned)");
         LanguageRegistry.addName(toastedPBSandwichBurned.getItemStack(), "Toasted PB Sandwich (Burned)");
         LanguageRegistry.addName(toastedPBJSandwichAppleBurned.getItemStack(), "Toasted PB&J Sandwich (Apple) (Burned)");
         LanguageRegistry.addName(toastedPBJSandwichStrawberryBurned.getItemStack(), "Toasted PB&J Sabdwich (Strawberry) (Burned)");
 
     }
 
     public static void addRecipes()
     {
         GameRegistry.addShapelessRecipe(sliceOfCheese.getItemStack(4), cheese.getItemStack());
         GameRegistry.addShapelessRecipe(sliceOfBread.getItemStack(4), loafOfBread.getItemStack());
 
         GameRegistry.addRecipe(clayBowl.getItemStack(12), "X X", " X ", 'X', Item.clay);
         GameRegistry.addRecipe(clayPlate.getItemStack(12), "XXX", 'X', Item.clay);
         GameRegistry.addRecipe(clayCup.getItemStack(12), "X", "X", 'X', Item.clay);
 
         GameRegistry.addShapelessRecipe(water.getItemStack(), ceramicCup.getItemStack(), Item.bucketWater);
         GameRegistry.addShapelessRecipe(milk.getItemStack(), ceramicCup.getItemStack(), Item.bucketMilk);
 
         GameRegistry.addShapelessRecipe(candiedApple.getItemStack(), Item.appleRed, Item.stick);
 
         FurnaceRecipes.smelting().addSmelting(clayBowl.getItemStack().itemID, clayBowl.getItemStack().getItemDamage(), ceramicBowl.getItemStack(), 0.1f);
         FurnaceRecipes.smelting().addSmelting(clayPlate.getItemStack().itemID, clayPlate.getItemStack().getItemDamage(), ceramicPlate.getItemStack(), 0.1f);
         FurnaceRecipes.smelting().addSmelting(clayCup.getItemStack().itemID, clayCup.getItemStack().getItemDamage(), ceramicCup.getItemStack(), 0.1f);
 
     }
 
     public static void init()
     {
         // ovenRack = new SubItem(dishID,
         // 6).setUnlocalizedName("OvenRack").setTextureName("OvenRack").setCreativeTab(Agriculture.tab);
 
         int damage = 0;
         clayBowl = createSubItem(dishID, damage++).setUnlocalizedName("ClayBowl").setTextureName("ClayBowl").setCreativeTab(Agriculture.tab);
         clayPlate = createSubItem(dishID, damage++).setUnlocalizedName("ClayPlate").setTextureName("ClayPlate").setCreativeTab(Agriculture.tab);
         clayCup = createSubItem(dishID, damage++).setUnlocalizedName("ClayCup").setTextureName("ClayCup").setCreativeTab(Agriculture.tab);
         ceramicBowl = createSubItem(dishID, damage++).setUnlocalizedName("CeramicBowl").setTextureName("CeramicBowl").setCreativeTab(Agriculture.tab);
         ceramicPlate = createSubItem(dishID, damage++).setUnlocalizedName("CeramicPlate").setTextureName("CeramicPlate").setCreativeTab(Agriculture.tab);
         ceramicCup = createSubItem(dishID, damage++).setUnlocalizedName("CeramicCup").setTextureName("CeramicCup").setCreativeTab(Agriculture.tab);
         ovenRack = createSubItem(dishID, damage++).setUnlocalizedName("OvenRack").setTextureName("OvenRack").setCreativeTab(Agriculture.tab);
         
         damage = 1;
         candiedApple = createSubFood(foodID, damage++, 1).setUnlocalizedName("CandiedApple").setCreativeTab(Agriculture.tab);
         frenchFries = createSubFood(foodID, damage++, 3).setUnlocalizedName("FrenchFries").setTextureName("FrenchFries").setCreativeTab(Agriculture.tab);
         water = createSubFood(foodID, damage++, 1).setUnlocalizedName("Water").setTextureName("Water").setCreativeTab(Agriculture.tab);
         milk = createSubFood(foodID, damage++, 1).setUnlocalizedName("Milk").setTextureName("Milk").setCreativeTab(Agriculture.tab);
         hotCocoa = createSubFood(foodID, damage++, 2, 0.1f).setUnlocalizedName("HotCocoa").setTextureName("HotCocoa").setCreativeTab(Agriculture.tab);
         vinegar = createSubItem(foodID, damage++).setUnlocalizedName("Vinegar").setTextureName("Vinegar").setCreativeTab(Agriculture.tab);
         beer = createSubFood(foodID, damage++, 2).setUnlocalizedName("Beer").setTextureName("Beer").setCreativeTab(Agriculture.tab);
         rawHamburgerPatty = createSubItem(foodID, damage++).setUnlocalizedName("RawHamburgerPatty").setTextureName("RawHamburgerPatty").setCreativeTab(Agriculture.tab);
         appleJellyToast = createSubFood(foodID, damage++, 10).setUnlocalizedName("AppleJellyToast").setTextureName("AppleJellyToast").setCreativeTab(Agriculture.tab);
         cinnamonToast = createSubFood(foodID, damage++, 9).setUnlocalizedName("CinnamonToast").setTextureName("CinnamonToast").setCreativeTab(Agriculture.tab);
         strawberryJellyToast = createSubFood(foodID, damage++, 10).setUnlocalizedName("StrawberryJellyToast").setTextureName("StrawberryJellyToast").setCreativeTab(Agriculture.tab);
         caramelAppleWithNuts = createSubFood(foodID, damage++, 7).setUnlocalizedName("CaramelAppleWithNuts").setTextureName("CaramelAppleWithNuts").setCreativeTab(Agriculture.tab);
         appleJelly = createSubItem(foodID, damage++).setUnlocalizedName("AppleJelly").setTextureName("AppleJelly").setCreativeTab(Agriculture.tab);
         mashedPotatos = createSubFood(foodID, damage++, 5).setUnlocalizedName("MashedPotatos").setTextureName("MashedPotatos").setCreativeTab(Agriculture.tab);
         carrotCakeBatter = createSubItem(foodID, damage++).setUnlocalizedName("CarrotCakeBatter").setTextureName("CarrotCakeBatter").setCreativeTab(Agriculture.tab);
         rawFrenchToast = createSubItem(foodID, damage++).setUnlocalizedName("RawFrenchToast").setTextureName("RawFrenchToast").setCreativeTab(Agriculture.tab);
         batter = createSubItem(foodID, damage++).setUnlocalizedName("Batter").setTextureName("Batter").setCreativeTab(Agriculture.tab);
         macaroniAndCheese = createSubFood(foodID, damage++, 8).setUnlocalizedName("MacaroniAndCheese").setTextureName("MacaroniAndCheese").setCreativeTab(Agriculture.tab);
         strawberryJelly = createSubItem(foodID, damage++).setUnlocalizedName("StrawberryJelly").setTextureName("StrawberryJelly").setCreativeTab(Agriculture.tab);
         rawApplePie = createSubItem(foodID, damage++).setUnlocalizedName("RawApplePie").setTextureName("RawApplePie").setCreativeTab(Agriculture.tab);
         rawStrawberryPie = createSubItem(foodID, damage++).setUnlocalizedName("RawStrawberryPie").setTextureName("RawStrawberryPie").setCreativeTab(Agriculture.tab);
         strawberryShortcake = createSubFood(foodID, damage++, 16).setUnlocalizedName("StrawberryShortcake").setTextureName("StrawberryShortcake").setCreativeTab(Agriculture.tab);
         hamburger = createSubFood(foodID, damage++, 6).setUnlocalizedName("Hamburger").setTextureName("Hamburger").setCreativeTab(Agriculture.tab);
         pbjSandwichApple = createSubFood(foodID, damage++, 12).setUnlocalizedName("PBJSandwichApple").setTextureName("PBJSandwichApple").setCreativeTab(Agriculture.tab);
         pbjSandwichStrawberry = createSubFood(foodID, damage++, 12).setUnlocalizedName("PBJSandwichStrawberry").setTextureName("PBJSandwichStrawberry").setCreativeTab(Agriculture.tab);
         cheeseSandwich = createSubFood(foodID, damage++, 5).setUnlocalizedName("CheeseSandwich").setTextureName("CheeseSandwich").setCreativeTab(Agriculture.tab);
         butteredToast = createSubFood(foodID, damage++, 6).setUnlocalizedName("ButteredToast").setTextureName("ButteredToast").setCreativeTab(Agriculture.tab);
         baconCheeseFries = createSubFood(foodID, damage++, 6).setUnlocalizedName("BaconCheeseFries").setTextureName("BaconCheeseFries").setCreativeTab(Agriculture.tab);
         baconCheeseburger = createSubFood(foodID, damage++, 9).setUnlocalizedName("BaconCheeseburger").setTextureName("BaconCheeseburger").setCreativeTab(Agriculture.tab);
         peanutButter = createSubItem(foodID, damage++).setUnlocalizedName("PeanutButter").setTextureName("PeanutButter").setCreativeTab(Agriculture.tab);
         chocolate = createSubFood(foodID, damage++, 3).setUnlocalizedName("Chocolate").setTextureName("Chocolate").setCreativeTab(Agriculture.tab);
         cheese = createSubItem(foodID, damage++).setUnlocalizedName("Cheese").setTextureName("Cheese").setCreativeTab(Agriculture.tab);
         butter = createSubItem(foodID, damage++).setUnlocalizedName("Butter").setTextureName("Butter").setCreativeTab(Agriculture.tab);
         whippedCream = createSubItem(foodID, damage++).setUnlocalizedName("WhippedCream").setTextureName("WhippedCream").setCreativeTab(Agriculture.tab);
         dough = createSubItem(foodID, damage++).setUnlocalizedName("Dough").setTextureName("Dough").setCreativeTab(Agriculture.tab);
         appleGelatin = createSubItem(foodID, damage++).setUnlocalizedName("AppleGelatin").setTextureName("Gelatin").setCreativeTab(Agriculture.tab);
         strawberryGelatin = createSubItem(foodID, damage++).setUnlocalizedName("StrawberryGelatin").setTextureName("StrawberryGelatin").setCreativeTab(Agriculture.tab);
         marshmellows = createSubFood(foodID, damage++, 3).setUnlocalizedName("Marshmellows").setTextureName("Marshmellows").setCreativeTab(Agriculture.tab);
         pastaDough = createSubItem(foodID, damage++).setUnlocalizedName("PastaDough").setTextureName("PastaDough").setCreativeTab(Agriculture.tab);
         cheeseFries = createSubFood(foodID, damage++, 5).setUnlocalizedName("CheeseFries").setTextureName("CheeseFries").setCreativeTab(Agriculture.tab);
         rawChickenNuggets = createSubItem(foodID, damage++).setUnlocalizedName("RawChickenNuggets").setTextureName("RawChickenNuggets").setCreativeTab(Agriculture.tab);
         cheeseburger = createSubFood(foodID, damage++, 8).setUnlocalizedName("Cheeseburger").setTextureName("Cheeseburger").setCreativeTab(Agriculture.tab);
         deluxeHotCocoa = createSubFood(foodID, damage++, 11).setUnlocalizedName("DeluxeHotCocoa").setTextureName("DeluxeHotCocoa").setCreativeTab(Agriculture.tab);
         saltedBeef = createSubItem(foodID, damage++).setUnlocalizedName("SaltedBeef").setTextureName("SaltedBeef").setCreativeTab(Agriculture.tab);
         breadedChicken = createSubItem(foodID, damage++).setUnlocalizedName("BreadedChicken").setTextureName("BreadedChicken").setCreativeTab(Agriculture.tab);
         saltedPork = createSubItem(foodID, damage++).setUnlocalizedName("SaltedPork").setTextureName("SaltedPork").setCreativeTab(Agriculture.tab);
         caramelApple = createSubFood(foodID, damage++, 4).setUnlocalizedName("CaramelApple").setTextureName("CaramelApple").setCreativeTab(Agriculture.tab);
         chocolateCoveredStrawberries = createSubFood(foodID, damage++, 4).setUnlocalizedName("ChocolateCoveredStrawberries").setTextureName("ChocolateCoveredStrawberries")
                 .setCreativeTab(Agriculture.tab);
         cinnamonAndSugar = createSubItem(foodID, damage++).setUnlocalizedName("CinnamonAndSugar").setTextureName("CinnamonAndSugar").setCreativeTab(Agriculture.tab);
         sliceOfCheese = createSubItem(foodID, damage++).setUnlocalizedName("SliceOfCheese").setTextureName("SliceOfCheese").setCreativeTab(Agriculture.tab);
         sliceOfBread = createSubFood(foodID, damage++, 1).setUnlocalizedName("SliceOfBread").setTextureName("SliceOfBread").setCreativeTab(Agriculture.tab);
         friedChicken = createSubFood(foodID, damage++, 3).setUnlocalizedName("FriedChicken").setTextureName("FriedChicken").setCreativeTab(Agriculture.tab);
         chickenNuggets = createSubFood(foodID, damage++, 4).setUnlocalizedName("ChickenNuggets").setTextureName("ChickenNuggets").setCreativeTab(Agriculture.tab);
         shortcake = createSubFood(foodID, damage++, 8).setUnlocalizedName("Shortcake").setTextureName("Shortcake").setCreativeTab(Agriculture.tab);
         carrotCake = createSubFood(foodID, damage++, 10).setUnlocalizedName("CarrotCake").setTextureName("CarrotCake").setCreativeTab(Agriculture.tab);
         grilledCheese = createSubFood(foodID, damage++, 6).setUnlocalizedName("GrilledCheese").setTextureName("GrilledCheese").setCreativeTab(Agriculture.tab);
         loafOfBread = createSubFood(foodID, damage++, 4).setUnlocalizedName("LoafOfBread").setTextureName("LoafOfBread").setCreativeTab(Agriculture.tab);
         pasta = createSubItem(foodID, damage++).setUnlocalizedName("Pasta").setTextureName("Pasta").setCreativeTab(Agriculture.tab);
         toastedPbjSandwichApple = createSubFood(foodID, damage++, 13).setUnlocalizedName("ToastedPBJSandwichApple").setTextureName("ToastedPBJSandwichApple")
                 .setCreativeTab(Agriculture.tab);
         toastedPbjSandwichStrawberry = createSubFood(foodID, damage++, 13).setUnlocalizedName("ToastedPBJSandwichStrawberry").setTextureName("ToastedPBJSandwichStrawberry")
                 .setCreativeTab(Agriculture.tab);
         roastedPeanuts = createSubFood(foodID, damage++, 2).setUnlocalizedName("RoastedPeanuts").setTextureName("RoastedPeanuts").setCreativeTab(Agriculture.tab);
         applePie = createSubFood(foodID, damage++, 10).setUnlocalizedName("ApplePie").setTextureName("ApplePie").setCreativeTab(Agriculture.tab);
         frenchToast = createSubFood(foodID, damage++, 8).setUnlocalizedName("FrenchToast").setTextureName("FrenchToast").setCreativeTab(Agriculture.tab);
         hamburgerPatty = createSubItem(foodID, damage++).setUnlocalizedName("HamburgerPatty").setTextureName("HamburgerPatty").setCreativeTab(Agriculture.tab);
         strawberryPie = createSubFood(foodID, damage++, 7).setUnlocalizedName("StrawberryPie").setTextureName("StrawberryPie").setCreativeTab(Agriculture.tab);
         beefJerkey = createSubFood(foodID, damage++, 2).setUnlocalizedName("BeefJerkey").setTextureName("BeefJerkey").setCreativeTab(Agriculture.tab);
         bacon = createSubFood(foodID, damage++, 2).setUnlocalizedName("Bacon").setTextureName("Bacon").setCreativeTab(Agriculture.tab);
         toast = createSubFood(foodID, damage++, 2).setUnlocalizedName("Toast").setTextureName("Toast").setCreativeTab(Agriculture.tab);
         caramel = createSubItem(foodID, damage++).setUnlocalizedName("Caramel").setTextureName("Caramel").setCreativeTab(Agriculture.tab);
         appleMush = createSubItem(foodID, damage++).setUnlocalizedName("AppleMush").setTextureName("AppleMush").setCreativeTab(Agriculture.tab);
         gelatin = createSubItem(foodID, damage++).setUnlocalizedName("gelatin").setTextureName("gelatin").setCreativeTab(Agriculture.tab);
         breadCrumbs = createSubItem(foodID, damage++).setUnlocalizedName("BreadCrumbs").setTextureName("BreadCrumbs").setCreativeTab(Agriculture.tab);
         groundBeef = createSubItem(foodID, damage++).setUnlocalizedName("GroundBeef").setTextureName("GroundBeef").setCreativeTab(Agriculture.tab);
         groundChicken = createSubItem(foodID, damage++).setUnlocalizedName("GroundChicken").setTextureName("GroundChicken").setCreativeTab(Agriculture.tab);
         groundPork = createSubItem(foodID, damage++).setUnlocalizedName("GroundPork").setTextureName("GroundPork").setCreativeTab(Agriculture.tab);
         crushedPeanuts = createSubItem(foodID, damage++).setUnlocalizedName("CrushedPeanuts").setTextureName("CrushedPeanuts").setCreativeTab(Agriculture.tab);
         strawberryMush = createSubItem(foodID, damage++).setUnlocalizedName("StrawberryMush").setTextureName("Shortcake").setCreativeTab(Agriculture.tab);
         flour = createSubItem(foodID, damage++).setUnlocalizedName("Flour").setTextureName("Flour").setCreativeTab(Agriculture.tab);
         cookingOil = createSubItem(foodID, damage++).setUnlocalizedName("CookingOil").setTextureName("CookingOil").setCreativeTab(Agriculture.tab);
         strawberry = createSubItemSeed(foodID, damage++, 1).setUnlocalizedName("Strawberry").setTextureName("Strawberry").setCreativeTab(Agriculture.tab);
         salt = createSubItem(foodID, damage++).setUnlocalizedName("Salt").setTextureName("Salt").setCreativeTab(Agriculture.tab);
         cinnamon = createSubItem(foodID, damage++).setUnlocalizedName("Cinnamon").setTextureName("Cinnamon").setCreativeTab(Agriculture.tab);
         groundCinnamon = createSubItem(foodID, damage++).setUnlocalizedName("GroundCinnamon").setTextureName("GroundCinnamon").setCreativeTab(Agriculture.tab);
         peanuts = createSubItemSeed(foodID, damage++, 1).setUnlocalizedName("Peanuts").setTextureName("Peanuts").setCreativeTab(Agriculture.tab);
 
         appleCinnamonCookieDough = createSubItem(foodID, damage++).setUnlocalizedName("AppleCinnamonCookieDough").setTextureName("AppleCinnamonCookieDough")
                 .setCreativeTab(Agriculture.tab);
         bowlOfRawPasta = createSubItem(foodID, damage++).setUnlocalizedName("BowlOfRawPasta").setTextureName("BowlOfRawPasta").setCreativeTab(Agriculture.tab);
         butterCookieDough = createSubItem(foodID, damage++).setUnlocalizedName("ButterCookieDough").setTextureName("ButterCookieDough").setCreativeTab(Agriculture.tab);
         cheesyBaconPotatoes = createSubFood(foodID, damage++, 9).setUnlocalizedName("BaconCheesyPotatoes").setTextureName("BaconCheesyPotatoes").setCreativeTab(Agriculture.tab);
         cheesyPotatoes = createSubFood(foodID, damage++, 7).setUnlocalizedName("CheesyPotatoes").setTextureName("CheesyPotatoes").setCreativeTab(Agriculture.tab);
         chocolateChipCookieDough = createSubItem(foodID, damage++).setUnlocalizedName("ChocolateChipCookieDough").setTextureName("ChocolateChipCookieDough")
                 .setCreativeTab(Agriculture.tab);
         chocolateIceCreamChocolateSauce = createSubItem(foodID, damage++).setUnlocalizedName("ChocolateIceCreamChocolateSauce").setTextureName("ChocolateIceCreamChocolateSauce")
                 .setCreativeTab(Agriculture.tab);
         chocolateIceCreamMix = createSubItem(foodID, damage++).setUnlocalizedName("ChocolateIceCreamMix").setTextureName("ChocolateIceCreamMix").setCreativeTab(Agriculture.tab);
         dicedPotatoes = createSubItem(foodID, damage++).setUnlocalizedName("DicedPotatoes").setTextureName("DicedPotatoes").setCreativeTab(Agriculture.tab);
         doubleBaconCheeseburger = createSubFood(foodID, damage++, 18).setUnlocalizedName("DoubleBaconCheeseburger").setTextureName("DoubleBaconCheeseburger")
                 .setCreativeTab(Agriculture.tab);
         iceCreamMix = createSubItem(foodID, damage++).setUnlocalizedName("IceCreamMix").setTextureName("IceCreamMix").setCreativeTab(Agriculture.tab);
         pbSandwich = createSubFood(foodID, damage++, 8).setUnlocalizedName("PBSandwich").setTextureName("PBSandwich").setCreativeTab(Agriculture.tab);
         peanutButterCookieDough = createSubItem(foodID, damage++).setUnlocalizedName("PeanutButterCookieDough").setTextureName("PeanutButterCookieDough").setCreativeTab(Agriculture.tab);
         pumpkinCookieDough = createSubItem(foodID, damage++).setUnlocalizedName("PumpkinCookieDough").setTextureName("PumpkinCookieDough").setCreativeTab(Agriculture.tab);
         snickerDoodleDough = createSubItem(foodID, damage++).setUnlocalizedName("SnickerDoodleDough").setTextureName("SnickerDoodleDough").setCreativeTab(Agriculture.tab);
         strawberryIceCreamChocolateSauce = createSubItem(foodID, damage++).setUnlocalizedName("StrawberryIceCreamChocolateSauce").setTextureName("StrawberryIceCreamChocolateSauce")
                 .setCreativeTab(Agriculture.tab);
         strawberryIceCreamMix = createSubItem(foodID, damage++).setUnlocalizedName("StrawberryIceCreamMix").setTextureName("StrawberryIceCreamMix").setCreativeTab(Agriculture.tab);
         strawberryJellyCookieDough = createSubItem(foodID, damage++).setUnlocalizedName("StrawberryJellyCookieDough").setTextureName("StrawberryJellyCookieDough")
                 .setCreativeTab(Agriculture.tab);
         sugarCookieDough = createSubItem(foodID, damage++).setUnlocalizedName("SugarCookieDough").setTextureName("SugarCookieDough").setCreativeTab(Agriculture.tab);
         vanillaIceCreamChocolateSauce = createSubItem(foodID, damage++).setUnlocalizedName("VanillaIceCreamChocolateSauce").setTextureName("VanillaIceCreamChocolateSauce")
                 .setCreativeTab(Agriculture.tab);
         vanillaIceCreamMix = createSubItem(foodID, damage++).setUnlocalizedName("VanillaIceCreamMix").setTextureName("VanillaIceCreamMix").setCreativeTab(Agriculture.tab);
         // IceBox
         chocolateIceCream = createSubFood(foodID, damage++, 7).setUnlocalizedName("ChocolateIceCream").setTextureName("ChocolateIceCream").setCreativeTab(Agriculture.tab);
         strawberryIceCream = createSubFood(foodID, damage++, 5).setUnlocalizedName("StrawberryIceCream").setTextureName("StrawberryIceCream").setCreativeTab(Agriculture.tab);
         vanillaIceCream = createSubFood(foodID, damage++, 5).setUnlocalizedName("VanillaIceCream").setTextureName("VanillaIceCream").setCreativeTab(Agriculture.tab);
         // Oven
         appleCinnamonCookie = createSubFood(foodID, damage++, 2).setUnlocalizedName("AppleCinnamonCookie").setTextureName("AppleCinnamonCookie").setCreativeTab(Agriculture.tab);
         butterCookie = createSubFood(foodID, damage++, 2).setUnlocalizedName("ButterCookie").setTextureName("ButterCookie").setCreativeTab(Agriculture.tab);
         chocolateChipCookie = createSubFood(foodID, damage++, 2).setUnlocalizedName("ChocolateChipCookie").setTextureName("ChocolateChipCookie").setCreativeTab(Agriculture.tab);
         chocolateSauce = createSubFood(foodID, damage++, 4).setUnlocalizedName("ChocolateSauce").setTextureName("ChocolateSauce").setCreativeTab(Agriculture.tab);
         peanutButterCookie = createSubFood(foodID, damage++, 2).setUnlocalizedName("PeanutButterCookie").setTextureName("PeanutButterCookie").setCreativeTab(Agriculture.tab);
         pumpkinCookie = createSubFood(foodID, damage++, 2).setUnlocalizedName("PumpkinCookie").setTextureName("PumpkinCookie").setCreativeTab(Agriculture.tab);
         roastedMarshmellows = createSubFood(foodID, damage++, 4).setUnlocalizedName("RoastedMarshmellows").setTextureName("RoastedMarshmellows").setCreativeTab(Agriculture.tab);
         snickerDoodle = createSubFood(foodID, damage++, 2).setUnlocalizedName("SnickerDoodle").setTextureName("SnickerDoodle").setCreativeTab(Agriculture.tab);
         strawberryJellyCookie = createSubFood(foodID, damage++, 2).setUnlocalizedName("StrawberryJellyCookie").setTextureName("StrawberryJellyCookie").setCreativeTab(Agriculture.tab);
         sugarCookie = createSubFood(foodID, damage++, 2).setUnlocalizedName("SugarCookie").setTextureName("SugarCookie").setCreativeTab(Agriculture.tab);
         toastedPBSandwich = createSubFood(foodID, damage++, 11).setUnlocalizedName("ToastedPBSandwich").setTextureName("ToastedPBSandwich").setCreativeTab(Agriculture.tab);
         vanilla = createSubItem(foodID, damage++).setUnlocalizedName("Vanilla").setCreativeTab(Agriculture.tab);
 
         appleCinnamonCookieBurned = createSubItem(foodID, damage++).setUnlocalizedName("AppleCinnamonCookieBurned").setCreativeTab(Agriculture.tab);
         applePieBurned = createSubItem(foodID, damage++).setUnlocalizedName("ApplePieBurned").setCreativeTab(Agriculture.tab);
         baconBurned = createSubItem(foodID, damage++).setUnlocalizedName("BaconBurned").setCreativeTab(Agriculture.tab);
         beefJerkeyBurned = createSubItem(foodID, damage++).setUnlocalizedName("BeefJerkeyBurned").setCreativeTab(Agriculture.tab);
         butterCookieBurned = createSubItem(foodID, damage++).setUnlocalizedName("ButterCookieBurned").setCreativeTab(Agriculture.tab);
         caramelBurned = createSubItem(foodID, damage++).setUnlocalizedName("CaramelBurned").setCreativeTab(Agriculture.tab);
         carrotCakeBurned = createSubItem(foodID, damage++).setUnlocalizedName("CarrotCakeBurned").setCreativeTab(Agriculture.tab);
         chocolateChipCookieBurned = createSubItem(foodID, damage++).setUnlocalizedName("ChocolateChipCookieBurned").setCreativeTab(Agriculture.tab);
         chocolateSauceBurned = createSubItem(foodID, damage++).setUnlocalizedName("ChocolateSauceBurned").setCreativeTab(Agriculture.tab);
         frenchToastBurned = createSubItem(foodID, damage++).setUnlocalizedName("FrenchToastBurned").setCreativeTab(Agriculture.tab);
         grilledCheeseBurned = createSubItem(foodID, damage++).setUnlocalizedName("GrilledCheeseBurned").setCreativeTab(Agriculture.tab);
         hamburgerPattyBurned = createSubItem(foodID, damage++).setUnlocalizedName("HamburgerPattyBurned").setCreativeTab(Agriculture.tab);
         loafOfBreadBurned = createSubItem(foodID, damage++).setUnlocalizedName("LoafOfBreadBurned").setCreativeTab(Agriculture.tab);
         pastaBurned = createSubItem(foodID, damage++).setUnlocalizedName("PastaBurned").setCreativeTab(Agriculture.tab);
         peanutButterCookieBurned = createSubItem(foodID, damage++).setUnlocalizedName("PeanutButterCookieBurned").setCreativeTab(Agriculture.tab);
         pumpkinCookieBurned = createSubItem(foodID, damage++).setUnlocalizedName("PumpkinCookieBurned").setCreativeTab(Agriculture.tab);
         roastedMarshmellowsBurned = createSubItem(foodID, damage++).setUnlocalizedName("RoastedMarshmellowsBurned").setCreativeTab(Agriculture.tab);
         roastedPeanutsBurned = createSubItem(foodID, damage++).setUnlocalizedName("RoastedPeanutsBurned").setCreativeTab(Agriculture.tab);
         shortcakeBurned = createSubItem(foodID, damage++).setUnlocalizedName("ShortcakeBurned").setCreativeTab(Agriculture.tab);
         snickerDoodleBurned = createSubItem(foodID, damage++).setUnlocalizedName("SnickerDoodleBurned").setCreativeTab(Agriculture.tab);
         strawberryJellyCookieBurned = createSubItem(foodID, damage++).setUnlocalizedName("StrawberryJellyCookieBurned").setCreativeTab(Agriculture.tab);
         strawberryPieBurned = createSubItem(foodID, damage++).setUnlocalizedName("StrawberryPieBurned").setCreativeTab(Agriculture.tab);
         sugarCookieBurned = createSubItem(foodID, damage++).setUnlocalizedName("SugarCookieBurned").setCreativeTab(Agriculture.tab);
         toastBurned = createSubItem(foodID, damage++).setUnlocalizedName("ToastBurned").setCreativeTab(Agriculture.tab);
         toastedPBJSandwichAppleBurned = createSubItem(foodID, damage++).setUnlocalizedName("ToastedPBJSandwichAppleBurned").setCreativeTab(Agriculture.tab);
         toastedPBJSandwichStrawberryBurned = createSubItem(foodID, damage++).setUnlocalizedName("ToastedPBJSandwichStrawberryBurned").setCreativeTab(Agriculture.tab);
         toastedPBSandwichBurned = createSubItem(foodID, damage++).setUnlocalizedName("ToastedPBSandwichBurned").setCreativeTab(Agriculture.tab);
 
     }
 
     private static SubItem createSubItemSeed(int id, int damage, int j)
     {
         int start = id;
 
         SubItem ret = null;
         while (ret == null)
         {
             if (id > Item.itemsList.length)
             {
                 break;
             }
 
             try
             {
                 ret = new SubItemSeed(id, damage, j);
             } catch (NullPointerException ex)
             {
                 id++;
             }
         }
 
         if (AgricultureItems.foodID == start && id != start)
         {
            AgricultureItems.foodID = id;
             ConfigHandler.set("Food", id);
         }
         else if (AgricultureItems.dishID == start  && id != start)
         {
            AgricultureItems.dishID = id;
             ConfigHandler.set("Dish", id);
         }
 
         if (ret == null)
         {
             throw new IllegalArgumentException("Slot " + id + " is already occupied by " + Item.itemsList[256 + id]);
         }
         return ret;
     }
 
     private static SubItem createSubFood(int id, int damage, int j, float f)
     {
         int start = id;
 
         SubItem ret = null;
         while (ret == null)
         {
             if (id > Item.itemsList.length)
             {
                 break;
             }
 
             try
             {
                 ret = new SubItemFood(id, damage, j, f);
             } catch (Throwable ex)
             {
                 id++;
             }
         }
 

         if (AgricultureItems.foodID == start && id != start)
         {
            AgricultureItems.foodID = id;
             ConfigHandler.set("Food", id);
         }
         else if (AgricultureItems.dishID == start  && id != start)
         {
            AgricultureItems.dishID = id;
             ConfigHandler.set("Dish", id);
         }
 
         if (ret == null)
         {
             throw new IllegalArgumentException("Slot " + id + " is already occupied by " + Item.itemsList[256 + id]);
         }
         return ret;
     }
 
     private static SubItem createSubFood(int id, int damage, int j)
     {
         int start = id;
 
         SubItem ret = null;
         while (ret == null)
         {
             if (id > Item.itemsList.length)
             {
                 break;
             }
 
             try
             {
                 ret = new SubItemFood(id, damage, j);
             } catch (Throwable ex)
             {
                 id++;
             }
         }
 

         if (AgricultureItems.foodID == start && id != start)
         {
             AgricultureItems.foodID = id;
             ConfigHandler.set("Food", id);
         }
         else if (AgricultureItems.dishID == start  && id != start)
         {
             AgricultureItems.dishID = id;
             ConfigHandler.set("Dish", id);
         }
 
         if (ret == null)
         {
             throw new IllegalArgumentException("Slot " + id + " is already occupied by " + Item.itemsList[256 + id]);
         }
         return ret;
     }
 
     private static SubItem createSubItem(int id, int damage)
     {
         int start = id;
 
         SubItem ret = null;
         while (ret == null)
         {
             if (id > Item.itemsList.length)
             {
                 break;
             }
 
             try
             {
                 ret = new SubItem(id, damage);
             } catch (Throwable ex)
             {
                 id++;
             }
         }
 
         if (AgricultureItems.foodID == start && id != start)
         {
            AgricultureItems.foodID = id;
             ConfigHandler.set("Food", id);
         }
         else if (AgricultureItems.dishID == start  && id != start)
         {
            AgricultureItems.dishID = id;
             ConfigHandler.set("Dish", id);
         }
 
         if (ret == null)
         {
             throw new IllegalArgumentException("Slot " + id + " is already occupied by " + Item.itemsList[256 + id]);
         }
         return ret;
     }
 
     public static void setupItems()
     {
         ((SubItemSeed) peanuts).setPlantBlock(AgricultureBlocks.peanut);
         ((SubItemSeed) strawberry).setPlantBlock(AgricultureBlocks.strawberry);
 
         ChestGenHooks.addItem("dungeonChest", new WeightedRandomChestContent(AgricultureItems.strawberry.getItemStack(), 1, 3, 5));
         ChestGenHooks.addItem("villageBlacksmithChestContents", new WeightedRandomChestContent(AgricultureItems.strawberry.getItemStack(), 1, 3, 5));
         ChestGenHooks.addItem("dungeonChest", new WeightedRandomChestContent(AgricultureItems.peanuts.getItemStack(), 1, 3, 5));
         ChestGenHooks.addItem("villageBlacksmithChestContents", new WeightedRandomChestContent(AgricultureItems.peanuts.getItemStack(), 1, 3, 5));
 
         addRecipes();
         addNames();
     }
 }
