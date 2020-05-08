 package com.ionmarkgames.ys.client;
 
 import com.ionmarkgames.ys.client.objects.Enemy;
 import com.ionmarkgames.ys.client.objects.You;
 import com.ionmarkgames.ys.client.objects.enemies.Clown;
 import com.ionmarkgames.ys.client.objects.enemies.Coin;
 import com.ionmarkgames.ys.client.objects.enemies.Ghost;
 import com.ionmarkgames.ys.client.objects.enemies.Lips;
 import com.ionmarkgames.ys.client.objects.enemies.Person;
 import com.ionmarkgames.ys.client.objects.enemies.Snake;
 import com.ionmarkgames.ys.client.objects.enemies.Spider;
 import com.ionmarkgames.ys.client.objects.enemies.Tornado;
 
 public class EnemyFactory {
 
 	private GameController controller;
 	
 	public EnemyFactory(GameController controller) {
 		this.controller = controller;
 	}
 	
 	public Enemy getEnemy() {
 		YSPanel panel = controller.getCurrentPanel();
 		You player = panel.getPlayer();
 		switch (controller.getLevel()) {
 			case 0: // entomophobia
				return new Lips(panel, player);
 			case 1: // ophidiophobia
 				return new Snake(panel, player);
 			case 2: // arachnophobia
 				return new Spider(panel, player);
 			case 3: // phasmophobia
 				return new Ghost(panel, player);
 			case 4: // coulrophobia
 			    return new Clown(panel, player);
 			case 5: // sociaphobia
 				return new Person(panel, player);
 			case 6: // acrophobia
 				return new Tornado(panel, player);
 			case 7: // chrometophobia
 				return new Coin(panel, player);
 			case 8: // intimacy
 			    return new Lips(panel, player);
 			case 9: // commitment
 			case 10: // failure
 			case 11: // glossophobia
 			case 12: // thanathophobia
 		}
 		return null;
 	}
 }
