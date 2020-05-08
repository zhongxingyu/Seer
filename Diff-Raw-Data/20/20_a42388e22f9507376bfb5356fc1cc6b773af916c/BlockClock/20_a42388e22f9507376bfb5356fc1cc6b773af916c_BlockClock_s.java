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
 import net.fourbytes.shadow.blocks.BlockType.LogicType;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.utils.Array;
 
 public class BlockClock extends BlockType implements BlockLogic {
 	
 	boolean triggered = false;
 	int factor = 32;
 	int totime = 4;
 	int timer = 0;
 	
 	public BlockClock() {
 	}
 	
 	public BlockClock(int timer) {
 		this.timer = timer*factor;
 	}
 	
 	@Override 
 	public void tick() {
 		block.interactive = true;
 		block.solid = false;
 		block.alpha = 0f;
 		timer++;
 		if (timer >= totime*factor) {
 			timer = 0;
 			triggered = !triggered;
 			
 			for (Layer l : block.layer.level.layers.values()) {
 				for (int xo = -1; xo <= 1; xo++) {
 					for (int yo = -1; yo <= 1; yo++) {
 						Array<Block> v = l.get(Coord.get(block.pos.x + xo, block.pos.y + yo));
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
 	}
 	
 	@Override
 	public TextureRegion getTexture() {
 		TextureRegion[][] regs = TextureRegion.split(Images.getTexture("block_clock"), 16, 16);
 		TextureRegion reg = null;
 		reg = regs[0][0];
 		return reg;
 	}
 	
 	@Override
 	public void render() {
 	}
 	
 	@Override
 	public boolean triggered() {
 		return triggered;
 	}
 
 	@Override
 	public void handle(boolean triggered) {
 		//this.triggered = triggered;
 	}
 
 	@Override
 	public LogicType getType() {
 		return LogicType.OUTPUT;
 	}
 	
 }
