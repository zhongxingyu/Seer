 package de.propra12.gruppe04.dynamiteboy.Item;
 
 import de.propra12.gruppe04.dynamiteboy.Game.C;
 import de.propra12.gruppe04.dynamiteboy.Game.Player;
 import de.propra12.gruppe04.dynamiteboy.Map.DestroyableField;
 import de.propra12.gruppe04.dynamiteboy.Map.FloorField;
 import de.propra12.gruppe04.dynamiteboy.Map.Map;
 import de.propra12.gruppe04.dynamiteboy.Map.WallField;
 
 public class FunnyPill extends Item {
 	protected boolean trippin = true;
 
 	public FunnyPill() {
 		super(true);
 	}
 
 	public void beFunny(Map map, Player player) {
 		map.getField(player.getGridfieldXByMiddle(),
 				player.getGridfieldYByMiddle()).setItem(null);
 		Thread t = new Thread(new FunFun(map, player));
 		t.start();
 
 	}
 
 	public class FunFun implements Runnable {
 
 		private Map map;
 		private Player player;
 
 		public FunFun(Map map, Player player) {
 			this.map = map;
 			this.player = player;
 		}
 
 		private void setMapToFunny(Map map) {
 			for (int y = 0; y < 480; y += 32) {
 				for (int x = 0; x < 640; x += 32) {
 					if (map.getFieldByPixel(x, y) instanceof FloorField) {
 						if (map.getFieldByPixel(x, y).getItem() instanceof Teleporter) {
 							map.getFieldByPixel(x, y).setImage(
 									C.FUNNYPILL_TELEPORTER);
 						} else {
 							map.getFieldByPixel(x, y).setImage(
 									C.FUNNYPILL_FLOORFIELD);
 						}
 					}
 					if (map.getFieldByPixel(x, y) instanceof WallField) {
 						map.getFieldByPixel(x, y).setImage(
 								C.FUNNYPILL_WALLFIELD);
 					}
 					if (map.getFieldByPixel(x, y) instanceof DestroyableField) {
 						map.getFieldByPixel(x, y).setImage(
 								C.FUNNYPILL_DESTROYABLEFIELD);
 					}
 				}
 			}
 		}
 
 		private void setMapToNormal(Map map) {
 			for (int y = 0; y < 480; y += 32) {
 				for (int x = 0; x < 640; x += 32) {
 					if (map.getFieldByPixel(x, y) instanceof FloorField) {
						if (map.getFieldByPixel(x, y).getItem() instanceof Teleporter) {
							map.getFieldByPixel(x, y).setImage(
									C.TELEPORTFIELD_DEFAULT_PIC);
						} else {
							((FloorField) map.getFieldByPixel(x, y))
									.setRandImage();
						}

 					}

 					if (map.getFieldByPixel(x, y) instanceof WallField) {
 						map.getFieldByPixel(x, y).setImage(
 								C.WALLFIELD_DEFAULT_PIC);
 					}
 					if (map.getFieldByPixel(x, y) instanceof DestroyableField) {
 						map.getFieldByPixel(x, y).setImage(
 								C.DESTROYABLEFIELD_DEFAULT_PIC);
 					}
 				}
 			}
 		}
 
 		@Override
 		public void run() {
 			setMapToFunny(map);
 			player.setImage(C.FUNNYPILL_PLAYER);
 			C.funny = true;
 			int i = 0;
 			while (trippin) {
 				if (i == 30) {
 					trippin = false;
 				}
 				try {
 					Thread.sleep(1000);
 				} catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				i++;
 			}
 			setMapToNormal(map);
 			player.setImage("default");
 			C.funny = false;
 		}
 	}
 
 }
