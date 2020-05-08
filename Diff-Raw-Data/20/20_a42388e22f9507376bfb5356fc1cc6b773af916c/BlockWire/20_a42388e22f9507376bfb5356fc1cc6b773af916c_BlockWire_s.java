 package net.fourbytes.shadow.blocks;
 
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Vector;
 
 import net.fourbytes.shadow.Block;
 import net.fourbytes.shadow.Coord;
 import net.fourbytes.shadow.Entity;
 import net.fourbytes.shadow.Garbage;
 import net.fourbytes.shadow.Images;
 import net.fourbytes.shadow.Layer;
 import net.fourbytes.shadow.Player;
 import net.fourbytes.shadow.TypeBlock;
 import net.fourbytes.shadow.map.Saveable;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.utils.Array;
 
 public class BlockWire extends BlockType implements BlockLogic {
 	
 	@Saveable
 	boolean triggered = false;
 	@Saveable
 	int send = 0;
 	
 	public BlockWire() {
 	}
 	
 	@Override 
 	public void tick() {
 		block.interactive = true;
 		block.solid = false;
 		
 		if (send == -6) {
 			for (Layer l : block.layer.level.layers.values()) {
 				for (int xo = -1; xo <= 1; xo++) {
 					for (int yo = -1; yo <= 1; yo++) {
 						Array<Block> v = l.get(Coord.get(block.pos.x+xo, block.pos.y+yo));
 						if (v != null) {
 							for (Block b : v) {
 								if (b instanceof TypeBlock) {
 									TypeBlock tb = (TypeBlock) b;
 									BlockType type = tb.type;
 									if (type instanceof BlockLogic) {
 										BlockLogic bl = (BlockLogic) type;
 										bl.handle(triggered);
 									}
 								}
 							}
 						}
 					}
 				}
 			}
 		}
 		send++;
 	}
 	
 	@Override
 	public TextureRegion getTexture() {
 		return new TextureRegion(Images.getTexture("block_wire"));
 	}
 	
 	@Override
 	public void render() {
		if (send < 0) {
 			super.render();
		}
 	}
 
 	@Override
 	public boolean triggered() {
 		return triggered;
 	}
 
 	@Override
 	public void handle(boolean triggered) {
 		if (send > 0) {
 			this.triggered = triggered;
 			send = -6;
 		}
 	}
 
 	@Override
 	public LogicType getType() {
 		return LogicType.PUTPUT;
 	}
 	
 }
