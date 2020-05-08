 package dungeon.ui.screens;
 
 import dungeon.load.messages.LevelLoadedEvent;
 import dungeon.messages.Message;
 import dungeon.messages.MessageHandler;
 import dungeon.models.*;
 import dungeon.models.messages.Transform;
 
 import javax.swing.*;
 import java.awt.Color;
 import java.awt.Font;
 import java.awt.Graphics;
 import java.awt.Rectangle;
 import java.util.logging.Logger;
 
 public class Canvas extends JPanel implements MessageHandler {
   private final static Logger LOGGER = Logger.getLogger(Canvas.class.getName());
 
   /**
    * How long to show dialogs in milliseconds.
    */
   private static final int DIALOG_TIME = 3000;
 
   private final Color blockingTile = new Color(181, 125, 147);
 
   private final Color passableTile = new Color(139, 108, 217);
 
   private final Color victoryTile = new Color(255, 244, 25);
 
   private final Color teleporterTile = new Color(0, 0, 0);
 
   private final Color playerColor = new Color(101, 202, 227);
 
   private final Color npcColor = new Color(28, 31, 255);
 
   private final Color merchantColor = new Color(255, 124, 235);
 
   private final Color enemyColor = new Color(33, 237, 60);
 
   private final Color savePointColor = new Color(50, 122, 88);
 
   private final Color hpColor = new Color(235, 58, 58);
 
   private final Color lifeColor = new Color(60, 179, 113);
 
   private final Color manaColor = new Color(0, 160, 255);
 
   private final Color white = new Color(255, 255, 255);
 
   private final Color moneyColor = new Color(253, 225, 54);
 
   private final Color itemColor = new Color(151, 151, 151);
 
   private final Color healthPotionColor = new Color(200, 0, 0);
 
   private final Color manaPotionColor = new Color(0, 170, 255);
 
   private final Color projectileColor = new Color(118, 77, 0);
 
   private final Color iceBoltProjectileColor = new Color(0, 200, 255);
 
   private final Font font = new Font("Arial", Font.PLAIN, 20);
 
   private World world;
 
   private long dialogTimeout;
 
   private NPC dialogNpc;
 
   /**
    * The unit to pixel conversion factors for the current room.
    */
   private double xPixelPerUnit;
 
   private double yPixelPerUnit;
 
   public Canvas () {
     this.setFocusable(true);
   }
 
   @Override
   public void handleMessage (Message message) {
     if (message instanceof Transform) {
       this.world = this.world.apply((Transform)message);
 
       if (message instanceof NPC.InteractTransform) {
         this.dialogTimeout = System.currentTimeMillis() + DIALOG_TIME;
         this.dialogNpc = ((NPC.InteractTransform)message).getNpc();
       }
     } else if (message instanceof LevelLoadedEvent) {
       this.world = ((LevelLoadedEvent)message).getWorld();
     }
 
     repaint();
   }
 
   @Override
   protected void paintComponent (Graphics g) {
     super.paintComponent(g);
 
     if (this.world == null) {
       return;
     }
 
     Room room = this.world.getCurrentRoom();
 
     this.xPixelPerUnit = (double)g.getClipBounds().width / room.getXSize();
     this.yPixelPerUnit = (double)g.getClipBounds().height / room.getYSize();
 
     this.drawTiles(g, room);
     this.drawDrops(g, room);
     this.drawNPCs(g, room);
     this.drawMerchants(g, room);
     this.drawEnemies(g, room);
     this.drawSavepoints(g, room);
     this.drawPlayer(g, this.world.getPlayer());
     this.drawProjectiles(g, room);
     this.drawHpIndicator(g);
     this.drawMoneyIndicator(g);
     this.drawLifeIndicator(g);
     this.drawManaIndicator(g);
     this.drawWeaponIndicator(g);
     this.drawDialog(g);
   }
 
   private void drawTiles (Graphics g, Room room) {
     for (Tile tile : room.getTiles()) {
       if (tile instanceof TeleporterTile) {
         g.setColor(this.teleporterTile);
       } else if (tile instanceof VictoryTile) {
         g.setColor(this.victoryTile);
       } else if (tile.isBlocking()) {
         g.setColor(this.blockingTile);
       } else {
         g.setColor(this.passableTile);
       }
 
       this.drawSquare(g, tile.getPosition(), Tile.SIZE);
     }
   }
 
   private void drawDrops (Graphics g, Room room) {
     for (Drop drop : room.getDrops()) {
       if (drop.isMoney()) {
         g.setColor(this.moneyColor);
       } else if (drop.getItem().getType() == ItemType.HEALTH_POTION) {
         g.setColor(this.healthPotionColor);
       } else if (drop.getItem().getType() == ItemType.MANA_POTION) {
         g.setColor(this.manaPotionColor);
       }
 
       this.drawSquare(g, drop.getPosition(), Drop.SIZE);
     }
   }
 
   private void drawNPCs (Graphics g, Room room) {
     for (NPC npc : room.getNpcs()) {
       g.setColor(this.npcColor);
 
       this.drawSquare(g, npc.getPosition(), NPC.SIZE);
     }
   }
 
   private void drawMerchants (Graphics g, Room room) {
     for (Merchant merchant : room.getMerchants()) {
       g.setColor(this.merchantColor);
 
       this.drawSquare(g, merchant.getPosition(), Merchant.SIZE);
     }
   }
 
   private void drawEnemies (Graphics g, Room room) {
     for (Enemy enemy : room.getEnemies()) {
       g.setColor(this.enemyColor);
 
       this.drawSquare(g, enemy.getPosition(), Enemy.SIZE);
     }
   }
 
   private void drawSavepoints (Graphics g, Room room) {
     for (SavePoint savePoint : room.getSavePoints()) {
       g.setColor(this.savePointColor);
 
       this.drawSquare(g, savePoint.getPosition(), savePoint.SIZE);
     }
   }
 
   private void drawPlayer (Graphics g, Player player) {
     g.setColor(this.playerColor);
 
     this.drawSquare(g, player.getPosition(), Player.SIZE);
   }
 
   private void drawProjectiles (Graphics g, Room room) {
     for (Projectile projectile : room.getProjectiles()) {
       if (projectile.getType() == DamageType.NORMAL) {
         g.setColor(this.projectileColor);
       } else if (projectile.getType() == DamageType.ICE) {
         g.setColor(this.iceBoltProjectileColor);
       }
 
       this.drawSquare(g, projectile.getPosition(), Projectile.SIZE);
     }
   }
 
   private void drawHpIndicator (Graphics g) {
     g.setColor(this.hpColor);
     g.fillRect(20, 20, 20, 20);
 
     g.setColor(this.white);
     g.setFont(this.font);
     g.drawString(String.format("%d / %d", this.world.getPlayer().getHitPoints(), this.world.getPlayer().getMaxHitPoints()), 60, 38);
   }
 
   private void drawLifeIndicator (Graphics g) {
     g.setColor(this.lifeColor);
     g.fillRect(20, 100, 20, 20);
 
     g.setColor(this.white);
     g.setFont(this.font);
     g.drawString(String.format("%d", this.world.getPlayer().getLives()), 60, 118);
   }
 
   private void drawMoneyIndicator (Graphics g) {
     g.setColor(this.moneyColor);
     g.fillRect(20, 60, 20, 20);
 
     g.setColor(this.white);
     g.setFont(this.font);
     g.drawString(String.format("%d", this.world.getPlayer().getMoney()), 60, 78);
   }
 
   private void drawManaIndicator (Graphics g) {
     g.setColor(this.manaColor);
     g.fillRect(20, 140, 20, 20);
 
     g.setColor(this.white);
     g.setFont(this.font);
     g.drawString(String.format("%d / %d", this.world.getPlayer().getMana(), this.world.getPlayer().getMaxMana()), 60, 158);
   }
 
   private void drawWeaponIndicator (Graphics g) {
     Item weapon = this.world.getPlayer().getWeapon();
     String weaponName = "Keine Waffe";
 
     if (weapon != null) {
       weaponName = weapon.getType().getName();
     }
 
     g.setColor(this.white);
     g.setFont(this.font);
     g.drawString(weaponName, 120, 38);
   }
 
   private void drawDialog (Graphics g) {
     if (System.currentTimeMillis() < this.dialogTimeout) {
       Rectangle bounds = g.getClipBounds();
 
       g.setColor(Color.BLACK);
       g.fillRect(10, 10, bounds.width - 20, 200);
 
       g.setFont(this.font);
 
       g.setColor(Color.YELLOW);
       g.drawString(this.dialogNpc.getName(), 20, 40);
 
       g.setColor(Color.WHITE);
       g.drawString(this.dialogNpc.getSaying(), 20, 80);
     }
   }
 
   /**
    * Draw a square with the positions converted from our abstract unit to pixels.
   *
   * The squares bounds are rounded up to prevent gaps between squares.
    */
   private void drawSquare (Graphics g, Position position, int widthUnits) {
    g.fillRect(
      (int)Math.ceil(position.getX() * this.xPixelPerUnit),
      (int)Math.ceil(position.getY() * this.yPixelPerUnit),
      (int)Math.ceil(widthUnits * this.xPixelPerUnit),
      (int)Math.ceil(widthUnits * this.yPixelPerUnit)
    );
   }
 }
