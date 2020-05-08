 package net.fourbytes.shadow.blocks;
 
 import java.util.ArrayList;
 import java.util.Random;
 import java.util.Vector;
 
 import net.fourbytes.shadow.Block;
 import net.fourbytes.shadow.Coord;
 import net.fourbytes.shadow.Entity;
 import net.fourbytes.shadow.Garbage;
 import net.fourbytes.shadow.Images;
 import net.fourbytes.shadow.Player;
 import net.fourbytes.shadow.Shadow;
 import net.fourbytes.shadow.Sounds;
 import net.fourbytes.shadow.TypeBlock;
 import net.fourbytes.shadow.map.Saveable;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Rectangle;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.utils.Array;
 
 public class BlockDissolve extends BlockType implements BlockLogic {
 	
 	@Saveable
 	boolean triggered = false;
 	boolean inverted = false;
 	
 	public BlockDissolve() {
 	}
 	
 	public BlockDissolve(int inverted) {
 		this.inverted = inverted==1?true:false;
 	}
 	
 	@Override 
 	public void tick() {
 		block.interactive = true;
 		if (!inverted) {
 			block.solid = !triggered;
 		} else {
 			block.solid = triggered;
 		}
 		block.alpha = block.solid?1f:0f;
 	}
 	
 	@Override
 	public TextureRegion getTexture() {
 		TextureRegion[][] regs = TextureRegion.split(Images.getTexture("block_dissolve"), 16, 16);
 		TextureRegion reg = null;
 		reg = regs[0][0];
 		return reg;
 	}
 
 	@Override
 	public boolean triggered() {
 		return triggered;
 	}
 
 	@Override
 	public void handle(boolean triggered) {
 		if (triggered != this.triggered) {
 			Sounds.getSound("disappear").play(Sounds.calcVolume(block.pos), Sounds.calcPitch(1f, 0.15f), 0f);
 			this.triggered = triggered;
 			
 			if ((!inverted && !triggered) || (inverted && triggered)) {
 				Array<Block> v = block.layer.get(Coord.get(block.pos.x, block.pos.y));
 				for (Block b : v) {
 					if (b != block) {
 						b.layer.remove(b);
 					}
 				}
 			}
 		}
 	}
 
 	@Override
 	public LogicType getType() {
 		return LogicType.INPUT;
 	}
 	
 	@Override
 	public void render() {
 		super.render();
 	}
 	
 }
