 package com.me.mygdxgame;
 
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.BitmapFont;
 import com.badlogic.gdx.graphics.g2d.TextureAtlas;
 import com.badlogic.gdx.scenes.scene2d.Group;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.Touchable;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Slider;
 import com.badlogic.gdx.scenes.scene2d.ui.TextButton;
 import com.badlogic.gdx.scenes.scene2d.utils.SpriteDrawable;
 
 public class HeadsUpDisplay
 {
 	Group yellowBoxGroup;
 	Label yellowBoxLabel;
 	int yellowBoxYPadding = 10;
 	int yellowBoxXPadding = 15;
 	ExtendedActor yellowBox;
 	ExtendedActor targetYellowBoxActor = null;
 	int yellowBoxLines = 0;
 	
 	Group consoleGroup;
 	Label consoleLabel;
 	ExtendedActor consoleBackground;
 	boolean showConsole = false;
 	int consoleLines = 0;
 	int consoleLinesPadding = 5;
 	List<String> consoleStrings = new ArrayList<String>();
 	int consoleStates = 0;
 	
 	List<String> towerKeys = new ArrayList<String>();
 	List<Label> towerCostLabels = new ArrayList<Label>();
 	Label wallCostLabel, sellPriceLabel, upgradeCostLabel;
 	Slider healthSlider, speedSlider, damageSlider;
 	
 	TextButton livesButton, goldButton;
 	
 	BitmapFont font;
 	
 	public HeadsUpDisplay(BitmapFont font)
 	{
 		this.font = font;
 	}
 	
 	public void createUI(TextureAtlas miscAtlas, TextureAtlas towersAtlas, HashMap<String, TowerStats> towerInfo, Stage stage, ButtonGenerator buttonGenerator, ListenerGenerator listenerGenerator, int startGold)
 	{
 		yellowBoxGroup = new Group();
 		yellowBox = new ExtendedActor(miscAtlas.createSprite("YellowBox"));
 		yellowBoxGroup.addActor(yellowBox);
 
 		towerKeys.addAll(towerInfo.keySet());
 
 		Label.LabelStyle labelStyle = new Label.LabelStyle(font, Color.BLACK);
 		yellowBoxLabel = new Label("", labelStyle);
 		yellowBoxLabel.setPosition(800, GameConstants.screenHeight - 40);
 		yellowBoxGroup.addActor(yellowBoxLabel);
 
 		consoleLabel = new Label("", labelStyle);
 
 		consoleGroup = new Group();
 		consoleBackground = new ExtendedActor(miscAtlas.createSprite("YellowBox"));
 		consoleBackground.setColor(Color.YELLOW.r, Color.YELLOW.g, Color.YELLOW.b, 50);
 		consoleGroup.addActor(consoleBackground);
 		consoleGroup.addActor(consoleLabel);
 		stage.addActor(consoleGroup);
 
 		consoleGroup.setVisible(showConsole);
 
 		yellowBoxGroup.setVisible(false);
 
 		yellowBoxGroup.setTouchable(Touchable.disabled);
 		consoleGroup.setTouchable(Touchable.disabled);
 		stage.addActor(yellowBoxGroup);
 
 		TextButton settingsButton = buttonGenerator.createButton(miscAtlas.createSprite("settingsButton"), font);
 		settingsButton.addListener(listenerGenerator.createCleanButtonListener());
 		settingsButton.setPosition(GameConstants.screenWidth - 2 * 64, GameConstants.screenHeight - 100);
 		stage.addActor(settingsButton);
 
 		TextButton sellButton = buttonGenerator.createButton(miscAtlas.createSprite("sellTowerButton"), font);
 		sellButton.addListener(listenerGenerator.createSellButtonListener());
 		sellButton.setPosition(GameConstants.screenWidth - 3 * 64, GameConstants.screenHeight - 100);
 		stage.addActor(sellButton);
 
 		sellPriceLabel = new Label("", labelStyle);
 		sellPriceLabel.setVisible(true);
 		sellPriceLabel.setPosition(sellButton.getX(), sellButton.getY() - sellButton.getHeight()/9);
 		sellPriceLabel.setText("");
 		stage.addActor(sellPriceLabel);
 		
 		TextButton upgradeButton = buttonGenerator.createButton(miscAtlas.createSprite("upgradeTowerButton"), font);
 		upgradeButton.addListener(listenerGenerator.createUpgradeButtonListener());
 		upgradeButton.setPosition(GameConstants.screenWidth - 4 * 64, GameConstants.screenHeight - 100);
 		stage.addActor(upgradeButton);
 
 		upgradeCostLabel = new Label("", labelStyle);
 		upgradeCostLabel.setVisible(true);
 		upgradeCostLabel.setPosition(upgradeButton.getX(), upgradeButton.getY() - upgradeButton.getHeight()/9);
 		upgradeCostLabel.setText("");
 		stage.addActor(upgradeCostLabel);
 		
 		TextButton wallButton = buttonGenerator.createButton(miscAtlas.createSprite("wallButton"), font);
 		wallButton.addListener(listenerGenerator.createWallButtonListener());
 		wallButton.setPosition(GameConstants.screenWidth - 5 * 64, GameConstants.screenHeight - 100);
 		stage.addActor(wallButton);
 		
 		wallCostLabel = new Label("", labelStyle);
 		wallCostLabel.setVisible(true);
 		wallCostLabel.setPosition(wallButton.getX(), wallButton.getY() - wallButton.getHeight()/9);
 		wallCostLabel.setText("");
 		stage.addActor(wallCostLabel);
 		
 		for (int i = 0; i < towerKeys.size(); i++)
 		{
 			if (!towerInfo.get(towerKeys.get(i)).buildable)
 			{
 				towerKeys.remove(i);
 				i--;
 				continue;
 			}
 
 			TextButton eachTowerButton = buttonGenerator.createButton(towersAtlas.createSprite(towerInfo.get(towerKeys.get(i)).towerTexture), font);
 			final String currentKey = towerKeys.get(i);
 			eachTowerButton.addListener(listenerGenerator.createTowerButtonListener(currentKey));
 			eachTowerButton.setPosition(10 + 10 * i + 64 * i, GameConstants.screenHeight - 100);
 			stage.addActor(eachTowerButton);
 
 			Label towerCostLabel = new Label("", labelStyle);
 			towerCostLabel.setText("" + towerInfo.get(towerKeys.get(i)).buildCost);
 			towerCostLabel.setVisible(true);
 			towerCostLabel.setPosition(55 + 10 * i + 64 * i, GameConstants.screenHeight - 125);
 			stage.addActor(towerCostLabel);
 			towerCostLabels.add(towerCostLabel);
 		}
 
 		livesButton = buttonGenerator.createButton(miscAtlas.createSprite("heart"), font, "" + GameConstants.startLives);
 		livesButton.setPosition(10 * 64, GameConstants.screenHeight - 100);
 		stage.addActor(livesButton);
 
 		goldButton = buttonGenerator.createButton(miscAtlas.createSprite("gold"), font, "        " + startGold);
 		goldButton.setPosition(12 * 64, GameConstants.screenHeight - 100);
 		stage.addActor(goldButton);
 		
 		healthSlider = new Slider(1f, 10f, 1f, false, new Slider.SliderStyle(new SpriteDrawable(miscAtlas.createSprite("sliderBar")), new SpriteDrawable(miscAtlas.createSprite("healthKnob"))));
 		healthSlider.setPosition(450, GameConstants.screenHeight - 50);
 		healthSlider.setVisible(true);
 		healthSlider.setTouchable(Touchable.disabled);
 		stage.addActor(healthSlider);
 		
 		speedSlider = new Slider(1f, 6f, 1f, false, new Slider.SliderStyle(new SpriteDrawable(miscAtlas.createSprite("sliderBar")), new SpriteDrawable(miscAtlas.createSprite("speedKnob"))));
 		speedSlider.setPosition(450, GameConstants.screenHeight - 85);
 		speedSlider.setVisible(true);
 		speedSlider.setTouchable(Touchable.disabled);
 		stage.addActor(speedSlider);
 		
 		damageSlider = new Slider(1f, 10f, 1f, false, new Slider.SliderStyle(new SpriteDrawable(miscAtlas.createSprite("sliderBar")), new SpriteDrawable(miscAtlas.createSprite("damageKnob"))));
 		damageSlider.setPosition(450, GameConstants.screenHeight - 125);
 		damageSlider.setVisible(true);
 		damageSlider.setTouchable(Touchable.disabled);
 		stage.addActor(damageSlider);
 	}
 	
 	public void updateYellowBoxPosition()
 	{
 		if (targetYellowBoxActor != null)
 		{
 			float x = targetYellowBoxActor.getX() + targetYellowBoxActor.getWidth() / 2 - yellowBox.getWidth() / 2;
 			float y = targetYellowBoxActor.getY() + targetYellowBoxActor.getHeight();
 			if (y >= GameConstants.screenHeight - yellowBox.getHeight())
 			{
 				y = targetYellowBoxActor.getY() - yellowBox.getHeight();
 			}
 			yellowBox.setPosition(x, y);
 			yellowBoxLabel.setPosition(x + yellowBoxXPadding, y + (yellowBox.getHeight() - yellowBoxLines * font.getBounds(yellowBoxLabel.getText()).height));
 		}
 	}
 	public void fadeInYellowBox(ExtendedActor targetActor, List<String> strings)
 	{
 		yellowBoxLines = strings.size();
 		int height = 2 * yellowBoxYPadding;
 		int width = 0;
 		yellowBoxLabel.setText("");
 		for (String s : strings)
 		{
 			height += 2 * (int) font.getBounds(s).height;
 			yellowBoxLabel.setText(yellowBoxLabel.getText() + s + "\n");
 			if (font.getBounds(s).width > width)
 				width = (int) font.getBounds(s).width;
 		}
 		width += 2 * yellowBoxXPadding;
 		yellowBox.setSize(width, height);
 		targetYellowBoxActor = targetActor;
 		yellowBoxGroup.setVisible(true);
 		yellowBoxGroup.setColor(0, 0, 0, 60);
 		yellowBoxGroup.setZIndex(1000); // Random high value, to keep it above
 										// anything.
 		yellowBoxGroup.setTouchable(Touchable.disabled);
 	}
 
 	public void updateConsole() // This must be called after console strings
 	// are changed for visually reflecting
 	// changes.
 	{
 		consoleLines = consoleStrings.size();
 		int height = 2 * consoleLinesPadding;
 		int width = 0;
 		consoleLabel.setText("");
 		for (String s : consoleStrings)
 		{
 			height += 2 * (int) font.getBounds(s).height;
 			consoleLabel.setText(consoleLabel.getText() + s + "\n");
 			if (font.getBounds(s).width > width)
 				width = (int) font.getBounds(s).width;
 		}
 		width += 2 * consoleLinesPadding;
 		consoleBackground.setSize(width, height);
 		consoleGroup.setZIndex(1001); // Random high value, to keep it
 											// above
 		// anything.
 		int consoleX = 0;
 		int consoleY = 0;
 		consoleBackground.setPosition(consoleX, consoleY);
 		consoleLabel
 				.setPosition(
 						consoleX + consoleLinesPadding,
 						consoleY
 								+ (height - consoleLines
 										* font.getBounds(consoleLabel
 												.getText()).height));
 	}
 
 	public void updateConsoleState(boolean goNextState, HashMap<String, Parameter> parameters, ThinkTankInfo thinkTankInfo)
 	{
 		consoleStrings.clear();
 		if (goNextState)
 			consoleStates++;
 		switch (consoleStates)
 		{
 			case 1:
 				consoleStrings.add("Somewhat Normalized Parameters");
 				consoleStrings.add("Global Monster HP Multiplier: " + parameters.get("GlobalMonsterHP").value);
 				consoleStrings.add("Global Reload Time Multiplier: " + parameters.get("GlobalReloadTime").value);
 				consoleStrings.add("Global Tower Range Multiplier: " + parameters.get("GlobalTowerRange").value);
 				consoleStrings.add("TE Dot Damage Multiplier: " + parameters.get("TEDotDamage").value);
 				consoleStrings.add("TE Damage Multiplier: " + parameters.get("TEDamage").value);
 				consoleStrings.add("Global Monster Gold Yield Multiplier: " + parameters.get("GlobalMonsterGoldYield").value);
 				consoleStrings.add("Global Monster Speed: " + parameters.get("GlobalMonsterSpeed").value);
 				consoleStrings.add("TE Slow Duration Multiplier: " + parameters.get("TESlowDuration").value);
 				consoleStrings.add("TE Slow Percentage Multiplier: " + parameters.get("TESlowPercentage").value);
 				consoleStrings.add("Global Build Cost Multiplier: " + parameters.get("GlobalBuildCost").value);
 				consoleStrings.add("TE Dot Ticks Multiplier: " + parameters.get("TEDotTicks").value);
				consoleStrings.add("Super Enemy Chance (Norm.): " + parameters.get("SuperChance").value);
 				consoleGroup.setVisible(true);
 				break;
 			case 2:
 				consoleStrings.add("Player Level: " + thinkTankInfo.playerLevel);
 				consoleStrings.add("Last Metric: " + thinkTankInfo.lastMetric);
 				consoleStrings.add("Current Metric: " + thinkTankInfo.currentMetric);
 				consoleStrings.add("Challenger Metric: " + thinkTankInfo.challengerMetric);
 				consoleStrings.add("Game Length Multiplier: " + thinkTankInfo.gameLengthMultiplier);
 				consoleStrings.add("Digger Chance: " + parameters.get("DiggerChance").value);
 				consoleStrings.add("Super Enemy Chance: " + thinkTankInfo.superEnemyChance);
 				consoleStrings.add("Earthquake On Chance: " + parameters.get("EarthquakeChance").value);
 				consoleStrings.add("Earthquake Chance: " + parameters.get("EarthquakeChanceInGame").value);
 				consoleStrings.add("Max Jump Distance: " + thinkTankInfo.maxJumpDistance);
 
 				consoleGroup.setVisible(true);
 				break;
 			default:
 				consoleStates = 0;
 				break;
 		}
 		if (consoleStates > 0)
 			consoleGroup.setVisible(true);
 		else
 			consoleGroup.setVisible(false);
 		updateConsole();
 	}
 
 	public void fadeOutYellowBox()
 	{
 		targetYellowBoxActor = null;
 		yellowBoxGroup.setVisible(false);
 	}
 	public void updateCostLabels(String text)
 	{
 		wallCostLabel.setText("");
 		upgradeCostLabel.setText("");
 		sellPriceLabel.setText("");
 	}
 	public void updateCostLabels(Tower selectedTower)
 	{
 		if (selectedTower.wall)
 			wallCostLabel.setText("wall");
 		else
 			wallCostLabel.setText("" + selectedTower.towerStats.buildCost*2);
 		
 		if (selectedTower.towerStats.upgradesTo.equals("null"))
 			upgradeCostLabel.setText("max");
 		else
 			upgradeCostLabel.setText("" + selectedTower.towerStats.upgradeCost);
 			
 		sellPriceLabel.setText("" + selectedTower.towerStats.sellPrice);
 	}
 }
