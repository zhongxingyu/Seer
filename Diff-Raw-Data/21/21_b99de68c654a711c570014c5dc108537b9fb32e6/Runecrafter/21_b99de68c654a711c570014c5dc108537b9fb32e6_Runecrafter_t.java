 package com.battleguard.scripts.f2prunecrafter;
 
 import java.awt.Color;
 import java.awt.Graphics;
 import java.util.Arrays;
 import java.util.Queue;
 import java.util.concurrent.ConcurrentLinkedQueue;
 
 import org.powerbot.event.PaintListener;
 import org.powerbot.script.Manifest;
 import org.powerbot.script.PollingScript;
 import org.powerbot.script.wrappers.GameObject;
 import org.powerbot.script.wrappers.Tile;
 import org.powerbot.script.wrappers.TileMatrix;
 
 import com.battleguard.scripts.f2prunecrafter.data.Master;
 import com.battleguard.scripts.f2prunecrafter.nodes.Node;
 import com.battleguard.scripts.f2prunecrafter.nodes.impl.Banking;
 import com.battleguard.scripts.f2prunecrafter.nodes.impl.Craft;
 import com.battleguard.scripts.f2prunecrafter.nodes.impl.Doors;
 import com.battleguard.scripts.f2prunecrafter.nodes.impl.Walk;
 
 @Manifest(authors = { "Battleguard" }, description = "AIO free to play runecrafter", name = "AIO F2P Runecrafter")
 public class Runecrafter extends PollingScript implements PaintListener {
 	
     private final Queue<Node> bankNodes = new ConcurrentLinkedQueue<>();
     private final Queue<Node> craftNodes = new ConcurrentLinkedQueue<>();
 	
 	private String currentNodeName = "null";
 	private Master master = Master.AIR;	
 	
 	public Runecrafter() {
 		getExecQueue(State.START).offer(new Runnable() {
 			 
             @Override
             public void run() {
             	master = Master.FIRE;	
         		
         		Node toAlter = Walk.alterPathInstance(master, ctx);        		
         		Node enterAlter = Doors.enterAlterInstance(master, ctx);
         		Node crafting = new Craft(master, ctx);        		
         		
         		Node bank = new Banking(master, ctx);
         		Node toBank = Walk.bankPathInstance(master, ctx);
         		Node exitAlter = Doors.exitAlterInstance(master, ctx);
         		
         		craftNodes.addAll(Arrays.asList(crafting, enterAlter, toAlter));
         		bankNodes.addAll(Arrays.asList(bank, exitAlter, toBank));
             }
         });		
 	}
 	
 	@Override
 	public int poll() {		
 		final Queue<Node> tree = ctx.backpack.select().id(master.rune().essenceId()).isEmpty() ? bankNodes : craftNodes;
 		for (Node node : tree) {
 			if(node.activate()) {
 				currentNodeName = node.getClass().getName();
 				node.execute();
 				return 50;
 			}
 		}
 		return 50;
 	}
 
 
 	@Override
 	public void repaint(Graphics g) {		
 		g.drawString("Current Node: " + currentNodeName, 10, 10);		
 		
 		final int[] ids = {master.alter().alterId(), master.alter().insideDoorId(), master.alter().outsideDoorId()};
 //		final Area[] areas = {master.area().bank(), master.area().insiderAlter(), master.area().outsideAlter()};
		final Tile[] path = master.path().toBank(ctx).toArray();
 				
 		for (GameObject sceneObject : ctx.objects.select().id(ids)) {			
 			sceneObject.draw(g);
 		}
 //		for (Area area : areas) {
 //			for (Tile tile : area.getTileArray()) {
 //				final TileMatrix matrix = tile.getMatrix(ctx);
 //				if(matrix.isOnScreen()) {		
 //					g.setColor(Color.GREEN.brighter());
 //					g.drawPolygon(matrix.getBounds());
 //				}							
 //			}
 //		}
 		
 		for (Tile tile : path) {
 			final TileMatrix matrix = tile.getMatrix(ctx);
 			if(matrix.isOnScreen()) {
 				g.setColor(Color.RED);
 				g.drawPolygon(matrix.getBounds());
 			}	
 		}
 	}
 
 }
