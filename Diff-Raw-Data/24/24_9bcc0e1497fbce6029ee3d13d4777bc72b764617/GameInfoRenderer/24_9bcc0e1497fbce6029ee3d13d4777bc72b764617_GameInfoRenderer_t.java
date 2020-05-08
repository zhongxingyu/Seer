 package com.ftwinston.Killer;
 
 import java.util.Collection;
 import java.util.HashMap;
 
 import org.bukkit.Bukkit;
 import org.bukkit.Location;
 import org.bukkit.Material;
 import org.bukkit.entity.Entity;
 import org.bukkit.entity.ItemFrame;
 import org.bukkit.entity.Player;
 import org.bukkit.inventory.ItemStack;
 import org.bukkit.map.MapCanvas;
 import org.bukkit.map.MapFont;
 import org.bukkit.map.MapPalette;
 import org.bukkit.map.MapRenderer;
 import org.bukkit.map.MapView;
 import org.bukkit.map.MinecraftFont;
 
 public class GameInfoRenderer extends MapRenderer
 {
 	static GameInfoRenderer createForGame(Game game, Location loc, boolean gameModeOnly)
 	{
 		// first find an item frame at the listed location
 		ItemFrame frame = null;
 		
 		double radiusSq = 1.5;
 		Collection<ItemFrame> frames = loc.getWorld().getEntitiesByClass(ItemFrame.class);
 		for(Entity e : frames) {
             double distSq = e.getLocation().distanceSquared(loc);
             if(distSq <= radiusSq && e instanceof ItemFrame) {
             	frame = (ItemFrame)e;
             	break;
             }
 		}
 		
         if ( frame == null ) {
         	Killer.instance.log.warning("Cannot find ItemFrame at " + loc.getBlockX() + ", " + loc.getBlockY() + ", " + loc.getBlockZ());
         	return null;
         }
 
 		MapView mv = Bukkit.createMap(frame.getWorld());
 
 		// put map item into frame
 		ItemStack stack =  new ItemStack(Material.MAP, 1, mv.getId());
 		frame.setItem(stack); 
 		
 		// then set up the renderer
 		for(MapRenderer r : mv.getRenderers())
 			mv.removeRenderer(r);
 				
 		GameInfoRenderer renderer = new GameInfoRenderer(game, mv, gameModeOnly); 
 		mv.addRenderer(renderer);
 		return renderer;
 	}
 	
 	private Game game;
 	private MapView view;
 	private boolean forGameMode;
 	private GameInfoRenderer(Game game, MapView view, boolean gameModeOnly)
 	{
 		this.game = game;
 		this.view = view;
 		this.forGameMode = gameModeOnly;
 		playersDrawnFor = new HashMap<String, Boolean>();
 	}
 	
 	void allowForChanges() { playersDrawnFor.clear(); }
 	
 	MapView getView() { return view; }
 	
 	final int lineHeight = 12, separator = 8, canvasWidth = 128;
 	final byte background = MapPalette.TRANSPARENT;
 	HashMap<String, Boolean> playersDrawnFor;
 	
 	@Override
 	public void render(MapView view, MapCanvas canvas, Player player)
 	{
 		// called every tick for every player
 		// only redraw if we actually have changes!
 		if ( playersDrawnFor.containsKey(player.getName()) )
 			return;
 		playersDrawnFor.put(player.getName(), true);
 		
 		for ( int x=0; x<128; x++ )
 			for ( int y=0; y<128; y++ )
 				canvas.setPixel(x, y, background);
 		
 		MapFont font = MinecraftFont.Font;
 		int xpos = 7, ypos = 6;
 		
 		if ( forGameMode )
 		{
 			ypos = drawText(canvas, font, xpos, ypos, game.getGameMode().describe());
 			player.sendMap(view);
 			return;
 		}
 		
		if ( game.getGameMode().allowWorldGeneratorSelection() )
			ypos = drawText(canvas, font, xpos, ypos, game.getWorldGenerator().describe());
 		else
 			ypos = drawText(canvas, font, xpos, ypos, "Using a world specified by the game mode");
 		
 		ypos = 88; xpos = 12;
 		ypos = drawText(canvas, font, xpos, ypos, "Players:");
 		ypos = drawText(canvas, font, xpos, ypos, "Monsters:");
 		ypos = drawText(canvas, font, xpos, ypos, "Animals:");
 		
 		ypos = 88; xpos = 66;
 		ypos = drawText(canvas, font, xpos, ypos, game.usesPlayerLimit()
 				? game.getPlayers().size() + " / " + game.getPlayerLimit()
 				: "" + game.getPlayers().size()
 			);
 		ypos = drawText(canvas, font, xpos, ypos, getQuantityText(game.monsterNumbers));
 		ypos = drawText(canvas, font, xpos, ypos, getQuantityText(game.animalNumbers));
 		player.sendMap(view);
 	}
 
 	private int drawText(MapCanvas canvas, MapFont font, int xpos, int ypos, String text)
 	{
 		String[] lines = text.split("\n");
 		for ( String line : lines )
 			ypos = drawTextLine(canvas, font, xpos, ypos, line);
 		
 		return ypos;
 	}
 	
 	private int drawTextLine(MapCanvas canvas, MapFont font, int xpos, int ypos, String text)
 	{
 		if ( getWidth(font, text) >= canvasWidth-xpos )
 		{
 			String[] words = text.split(" ");
 			canvas.drawText(xpos, ypos, font, words[0]);
 			int x = xpos + getWidth(font, words[0]);
 			for ( int wordNum=1; wordNum<words.length; wordNum++ ) 
 			{
 				String word = " " + words[wordNum];
 				int width = getWidth(font, word);
 				
 				if ( x + width >= canvasWidth )
 				{
 					x = xpos;
 					ypos += lineHeight;
 				}
 				canvas.drawText(x, ypos, font, word);
 				x += width;
 			}
 		}
 		else
 			canvas.drawText(xpos, ypos, font, text);
 		return ypos + lineHeight;
 	}
 	
 	private int getWidth(MapFont font, String text)
 	{
 		return font.getWidth(text) + text.length();
 	}
 	
 	public static String getQuantityText(int num)
 	{
 		switch ( num )
 		{
 		case 0:
 			return "None";
 		case 1:
 			return "Few";
 		case 2:
 			return "Some";
 		case 3:
 			return "Many";
 		case 4:
 			return "Too Many";
 		default:
 			return "???";
 		}
 	}
 }
