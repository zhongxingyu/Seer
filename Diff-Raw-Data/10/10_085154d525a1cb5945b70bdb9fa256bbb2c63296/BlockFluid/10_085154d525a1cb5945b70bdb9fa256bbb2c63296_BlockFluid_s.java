 package net.fourbytes.shadow.blocks;
 
 import java.util.Random;
 import java.util.Vector;
 
 import net.fourbytes.shadow.Block;
 import net.fourbytes.shadow.Coord;
 import net.fourbytes.shadow.Entity;
 import net.fourbytes.shadow.Garbage;
 import net.fourbytes.shadow.Images;
 import net.fourbytes.shadow.Input;
 import net.fourbytes.shadow.Player;
 import net.fourbytes.shadow.TypeBlock;
 
 import com.badlogic.gdx.graphics.Texture;
 import com.badlogic.gdx.graphics.g2d.Sprite;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.utils.Array;
 
 public abstract class BlockFluid extends BlockType {
 	
 	/*Conor-ian water system. Powered by Conor's redstone brain!*/
 	static boolean conor = true;
 	public BlockFluid source;
 	
 	int subframe = -1;
 	int frame = 0;
 	
 	int gframe = 24;
 	int pframe = 20;
 	
 	public int height = 16;
 	
 	boolean topblock = true;
 	
 	public BlockFluid() {
 	}
 	
 	public static Random rand = new Random();
 	
 	@Override
 	public void tick() {
 		if (subframe == -1) {
 			subframe++;
 		}
 		
 		block.interactive = true;
 		block.solid = false;
 		block.alpha = 0.75f;
 		block.rendertop = true;
 		
 		subframe += rand.nextInt(6);
 		block.solid = false;
 		if (subframe > 24) {
 			frame++;
 			subframe = 0;
 			if (source != null && conor) {
 				if (!source.block.layer.blocks.contains(source.block, true)) {
 					height-=3;
 					if (source.block.pos.y < block.pos.y) {
 						height-=3;
 					}
 				}
 			}
 			if (height <= 2) {
 				block.layer.remove(block);
 			}
 			imgupdate = true;
 		}
 		if (frame >= 4) {
 			frame = 0;
 			block.pixdur = rand.nextInt(20)+20;
 			imgupdate = true;
 		}
 		
 		topblock = true;
 		Array<Block> al = block.layer.get(Coord.get(block.pos.x, block.pos.y-1));
 		if (al != null) {
 			for (Block b : al) {
 				if (b instanceof TypeBlock && ((TypeBlock)b).type instanceof BlockFluid) {
 					height = 16;
 					imgupdate = true;
 				}
 				topblock = false;
 				break;
 			}
 		}
 		
 		gframe--;
 		if (gframe <= 0) {
 			boolean free = update(0f, 1f, 16, true);
 			if (free) {
 				gframe = 24;
 			}
 		}
 		
 		pframe--;
 		
 		boolean onground = false;
 		al = block.layer.get(Coord.get(block.pos.x, block.pos.y+1));
 		if (al != null) {
 			for (Block b : al) {
 				if (b instanceof TypeBlock && ((TypeBlock)b).type instanceof BlockFluid) {
 					onground = false;
 					imgupdate = true;
 					break;
 				}
 				if (b.solid) {
 					onground = true;
 					imgupdate = true;
 					break;
 				}
 				//if (!free) break;
 			}
 		}
 		
 		if (pframe <= 0 && height > 4 && onground) {
			boolean free1 = update(-1f, 0f, height-2, false);
			boolean free2 = update(1f, 0f, height-2, false);
 			if (free1 && free2) {
 				pframe = 20;
 			}
 		}
 		
 		block.rec.height = height/16f;
 		block.colloffs.y = (16-height)/16f;
 		block.colloffs.height = -block.colloffs.y;
 	}
 	
 	public boolean update(float xo, float yo, int height, boolean hupdate) {
 		boolean free = true;
 		
 		if (height <= 3) {
 			return !hupdate;
 		}
 		
		if (conor) {
 			height++;
		}
 		
 		Array<Block> al = block.layer.get(Coord.get(block.pos.x+xo, block.pos.y+yo));
 		if (al != null) {
 			for (Block b : al) {
 				if (b == block) {
 					continue;
 				}
 				if (b instanceof TypeBlock && ((TypeBlock)b).type instanceof BlockPush) {
 					((BlockPush)((TypeBlock)b).type).push((int) xo, 0);
 				}
 				if (b instanceof TypeBlock && ((TypeBlock)b).type instanceof BlockFluid) {
 					if (isSameType(((TypeBlock)b).type)) {
 						if (hupdate) {
 							((BlockFluid)((TypeBlock)b).type).height = height;
 							b.imgupdate = true;
 							((BlockFluid)((TypeBlock)b).type).source = this;
 						} else {
 							BlockFluid fluid = ((BlockFluid)((TypeBlock)b).type);
 							if (fluid.height < height) {
 								fluid.height = height;
 								fluid.block.imgupdate = true;
 								fluid.source = this;
 							}
 						}
 						free = false;
 						break;
 					} else {
 						boolean handled = !handleMix((BlockFluid)((TypeBlock)b).type);
 						if (handled) {
 							free = false;
 							break;
 						}
 					}
 				}
 				if (!b.solid) continue;
 				free = false;
 				break;
 				//if (!free) break;
 			}
 		}
 		
 		if (free) {
 			Block b = create(block.pos.x + xo, block.pos.y + yo, height);
 			block.layer.add(b);
 		}
 		return free;
 	}
 	
 	public Block create(float x, float y, int height) {
 		TypeBlock b = (TypeBlock) BlockType.getInstance(subtype, x, y, block.layer);
 		b.interactive = true;
 		b.solid = false;
 		BlockFluid type = (BlockFluid) b.type;
 		type.height = height;
 		type.source = this;
 		return b;
 	}
 	
 	@Override
 	public TextureRegion getTexture() {
 		int height = this.height;
 		//if (height <= 0) height = 1;
 		//if (height > 16) height = 16;
 		if (!topblock && height == 16) {
 			TextureRegion[][] regs = TextureRegion.split(getTexture0(), 16, height);
 			TextureRegion reg = null;
 			if (regs.length > 0) {
 				reg = regs[0][0];
 				return reg;
 			} else {
 				return new TextureRegion(Images.getTexture("white"));
 			}
 		} else {
 			TextureRegion[][] regs = TextureRegion.split(getTexture1(), 16, height);
 			TextureRegion reg = null;
 			if (regs.length > 0) {
 				reg = regs[0][frame];
 				return reg;
 			} else {
 				return new TextureRegion(Images.getTexture("white"));
 			}
 		}
 	}
 
 	public abstract Texture getTexture0();
 	public abstract Texture getTexture1();
 	
 	@Override
 	public void preRender() {
 		block.tmpimg = block.getImage();
 		if (block.tmpimg != null) {
 			block.tmpimg.setScaleY(-1f * (height/16f));
 			//i.setPosition(pos.x * Shadow.dispw/Shadow.vieww, pos.y * Shadow.disph/Shadow.viewh);
 			block.tmpimg.setPosition(block.pos.x, block.pos.y + 1f);
 			//block.tmpimg.setSize(block.rec.width, 1f*(height*(1f/16f)));
 			//block.tmpimg.setSize(block.rec.width, block.rec.height);
 			block.tmpimg.setSize(block.rec.width, 1f);
 		} else {
 			System.out.println("I: null; S: "+toString());
 		}
 	}
 	
 	boolean isSameType(Object o) {
 		return getClass().isInstance(o);
 	}
 	
 	public abstract boolean handleMix(BlockFluid type);
 	
 	@Override
 	public void collide(Entity e) {
 		e.movement.scl(0.8f);
 		e.movement.y -= 0.02f;
 		if (e instanceof Player) {
 			((Player)e).canJump = ((Player)e).maxJump;
 		}
 		if (e instanceof Player && Input.down.isDown) {
 			e.movement.y += 0.03f;
 		}
 		if (e instanceof Player && Input.up.isDown) {
 			e.movement.y -= 0.02f;
 		}
 	}
 	
 }
