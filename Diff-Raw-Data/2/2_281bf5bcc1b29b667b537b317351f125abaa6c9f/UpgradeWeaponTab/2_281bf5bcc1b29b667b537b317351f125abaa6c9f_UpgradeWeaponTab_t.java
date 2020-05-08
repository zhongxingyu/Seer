 package com.irr310.client.graphics.gui;
 
 import java.util.List;
 import java.util.Map;
 
 import org.fenggui.event.ButtonPressedEvent;
 import org.fenggui.event.IButtonPressedListener;
 
 import com.irr310.client.navigation.LoginManager;
 import com.irr310.common.Game;
 import com.irr310.common.event.BuyUpgradeRequestEvent;
 import com.irr310.common.event.QuitGameEvent;
 import com.irr310.common.world.upgrade.Upgrade;
 import com.irr310.common.world.upgrade.UpgradeOwnership;
 
 import fr.def.iss.vd2.lib_v3d.V3DColor;
 import fr.def.iss.vd2.lib_v3d.gui.V3DButton;
 import fr.def.iss.vd2.lib_v3d.gui.V3DContainer;
 import fr.def.iss.vd2.lib_v3d.gui.V3DGuiComponent;
 import fr.def.iss.vd2.lib_v3d.gui.V3DGuiComponent.GuiXAlignment;
 import fr.def.iss.vd2.lib_v3d.gui.V3DGuiRectangle;
 import fr.def.iss.vd2.lib_v3d.gui.V3DLabel;
 import fr.def.iss.vd2.lib_v3d.gui.V3DGuiComponent.GuiYAlignment;
 
 
 public class UpgradeWeaponTab extends UpgradeTab{
 
     private V3DContainer root;
     private List<UpgradeOwnership> upgradesOwnership;
 
     public UpgradeWeaponTab() {
         super("Weapon");
         
         upgradesOwnership = LoginManager.localPlayer.getUpgrades();
         Map<String, Upgrade> availableUpgrades = Game.getInstance().getWorld().getAvailableUpgrades();
         root = new V3DContainer();
         
         int y = 10;
         
         for(Upgrade upgrade: availableUpgrades.values()) {
             V3DGuiComponent upgradePane = generateUpgradePane(upgrade);
             upgradePane.setPosition(10, y);
             root.add(upgradePane);
             y += 160;
         }
         
     }
     
     private V3DGuiComponent generateUpgradePane(final Upgrade upgrade) {
         V3DContainer pane = new V3DContainer();
         pane.setSize(320, 150);
         
         V3DGuiRectangle upgradeRect = new V3DGuiRectangle();
         upgradeRect.setyAlignment(GuiYAlignment.TOP);
         upgradeRect.setPosition(0, 0);
         upgradeRect.setBorderWidth(2);
         upgradeRect.setSize(320, 150);
         upgradeRect.setFillColor(V3DColor.transparent);
         upgradeRect.setBorderColor(GuiConstants.irrGreen);
         pane.add(upgradeRect);
         
         V3DLabel upgradeName = new V3DLabel(upgrade.getName());
         upgradeName.setFontStyle("Ubuntu", "bold", 16);
         upgradeName.setColor(V3DColor.black, V3DColor.transparent);
         upgradeName.setPosition(5, 0);
         pane.add(upgradeName);
         
         int yPos = 20;
         
         V3DLabel upgradeDescription = new V3DLabel(upgrade.getGlobalDescription());
         upgradeDescription.setFontStyle("Ubuntu", "", 12);
         upgradeDescription.setColor(V3DColor.darkgrey, V3DColor.transparent);
         upgradeDescription.setPosition(5, yPos);
         upgradeDescription.setyAlignment(GuiYAlignment.TOP);
         upgradeDescription.setWordWarping(true, 150);
         pane.add(upgradeDescription);
         
         yPos += upgradeDescription.getSize().getY();
         
         
         UpgradeOwnership ownership  = LoginManager.localPlayer.getUpgradeState(upgrade);
         int currentRank = ownership.getRank();
         
         V3DLabel rankLabel = new V3DLabel(""+currentRank+" / "+upgrade.getMaxRank());
         rankLabel.setFontStyle("Ubuntu", "bold", 24);
         rankLabel.setColor(V3DColor.black, V3DColor.transparent);
         rankLabel.setxAlignment(GuiXAlignment.RIGHT);
         rankLabel.setPosition(5, 5);
         pane.add(rankLabel);
         
         
         if(currentRank > 0) {
             V3DLabel currentRankLabel = new V3DLabel("Current rank:");
             currentRankLabel.setFontStyle("Ubuntu", "bold", 12);
             currentRankLabel.setColor(V3DColor.black, V3DColor.transparent);
             currentRankLabel.setWordWarping(true, 150);
             currentRankLabel.setPosition(5, yPos);
             pane.add(currentRankLabel);
             
             yPos += currentRankLabel.getSize().getY();
             
            V3DLabel currentRankDescription = new V3DLabel(upgrade.getRankDescriptions().get(currentRank-1));
             currentRankDescription.setFontStyle("Ubuntu", "", 12);
             currentRankDescription.setColor(V3DColor.darkgrey, V3DColor.transparent);
             currentRankDescription.setWordWarping(true, 150);
             currentRankDescription.setPosition(5, yPos);
             pane.add(currentRankDescription);
             
             yPos += currentRankDescription.getSize().getY();
         }
         
         
         if(currentRank < upgrade.getMaxRank()) {
             V3DLabel nextRankLabel = new V3DLabel("Next rank:");
             nextRankLabel.setFontStyle("Ubuntu", "bold", 12);
             nextRankLabel.setColor(V3DColor.black, V3DColor.transparent);
             nextRankLabel.setWordWarping(true, 150);
             nextRankLabel.setPosition(5, yPos);
             pane.add(nextRankLabel);
             
             yPos += nextRankLabel.getSize().getY();
             
             V3DLabel nextRankDescription = new V3DLabel(upgrade.getRankDescriptions().get(currentRank));
             nextRankDescription.setFontStyle("Ubuntu", "", 12);
             nextRankDescription.setColor(V3DColor.darkgrey, V3DColor.transparent);
             nextRankDescription.setWordWarping(true, 150);
             nextRankDescription.setPosition(5, yPos);
             pane.add(nextRankDescription);
             
             yPos += nextRankDescription.getSize().getY();
             
             
             final V3DButton buyButton = new V3DButton("Buy");
             buyButton.setFontStyle("Ubuntu", "bold", 16);
             buyButton.setColor(V3DColor.white, GuiConstants.irrGreen);
             buyButton.setxAlignment(GuiXAlignment.RIGHT);
             buyButton.setyAlignment(GuiYAlignment.BOTTOM);
             buyButton.setPadding(5,30,30,5);
             buyButton.setPosition( 20,40);
             buyButton.getFenGUIWidget().addButtonPressedListener(new IButtonPressedListener() {
                 
                 @Override
                 public void buttonPressed(ButtonPressedEvent e) {
                     Game.getInstance().sendToAll(new BuyUpgradeRequestEvent(upgrade, LoginManager.localPlayer));
                 }
             });
             pane.add(buyButton);
             
             
             V3DLabel buyPrice = new V3DLabel(upgrade.getPrices().get(currentRank) +" $");
             buyPrice.setFontStyle("Ubuntu", "bold", 16);
             buyPrice.setColor(GuiConstants.irrGreen, V3DColor.transparent);
             buyPrice.setxAlignment(GuiXAlignment.RIGHT);
             buyPrice.setyAlignment(GuiYAlignment.BOTTOM);
             buyPrice.setPosition( 20,15);
             pane.add(buyPrice);
         }
         
         return pane;
     }
 
     @Override
     public V3DContainer getContentPane() {
         
         
         
         
         return root;
     }
 
     
 }
