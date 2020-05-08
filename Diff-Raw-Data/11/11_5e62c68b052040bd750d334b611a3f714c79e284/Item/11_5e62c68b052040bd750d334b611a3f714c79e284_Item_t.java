 package de.futjikato.mrwhiz.game;
 
 import de.futjikato.mrwhiz.game.inventory.Tool;
 import de.futjikato.mrwhiz.xml.Trigger;
 
 public class Item extends Block {
 
 	private int score;
 
 	private Tool tool;
 
 	public Item(int bx, int by, char type, BlockDefinitions defines) {
 		super(bx, by, type, defines);
 
 		score = defines.getBlockAttributeAsInt(type, "score", 0);
 
 		String toolName = defines.getBlockAttributeAsString(type, "tool");
 		if (toolName != null) {
 			tool = Tool.getTool(toolName);
 		}
 	}
 
 	public int getScore() {
 		return score;
 	}
 
 	public Tool getTool() {
 		return tool;
 	}
 
 	@Override
 	public void triggerTouch() {
 		for ( Trigger trigger : touchListener ) {
 			trigger.trigger();
 		}
 	}
 }
