 /*
  * Copyright (c) 2009-2011 Daniel Oom, see licence.txt for more info.
  */
 
 package entities;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import loader.data.DataException;
 import loader.data.json.PlayersData;
 import loader.parser.ParserException;
 import main.Launcher;
 
 import org.newdawn.slick.Graphics;
 import org.newdawn.slick.SlickException;
 
 import other.CacheTool;
 import other.GameTime;
 import basics.Rectangle;
 import basics.Vector2;
 import factories.PlayerFactory;
 
 public class Players implements Iterable<Player> {
   private static final float      STARING_X = 0.1f;
   private final int               count;
   private final ArrayList<Player> players;
 
  public Players(final int count)
    throws ParserException, DataException, IOException, SlickException {
     super();
 
     players = new ArrayList<Player>();
     this.count = count;
 
     final PlayersData data = CacheTool.getPlayers(Launcher.cache);
     int j = 0;
     for (int i = 0; i < count; i++) {
       players.add(PlayerFactory.Make(0, 0, data.players.get(i)));
       j = (j + 1) % data.players.size();
     }
   }
 
   public void update(final GameTime time) {
     for (final Player p : players) {
       p.update(time);
     }
   }
 
   public void render(final Graphics g) {
     for (final Player p : players) {
       p.render(g);
     }
   }
 
   public void reposition(final Rectangle rect) {
     final float dy = rect.getHeight() / (count * 2);
     final Vector2 v = new Vector2(rect.getWidth() * Players.STARING_X, dy);
     for (final Player p : players) {
       p.getBody().setPosition(v);
       v.y += dy;
     }
   }
 
   @Override
   public Iterator<Player> iterator() {
     return players.iterator();
   }
 }
