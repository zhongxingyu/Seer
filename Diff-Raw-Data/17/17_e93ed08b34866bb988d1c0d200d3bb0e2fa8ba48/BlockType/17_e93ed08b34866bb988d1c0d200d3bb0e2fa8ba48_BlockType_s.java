 package net.fourbytes.shadow.blocks;
 
 import java.lang.reflect.InvocationTargetException;
 
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.graphics.g2d.TextureRegion;
 import com.badlogic.gdx.math.Vector2;
 import com.badlogic.gdx.scenes.scene2d.ui.Image;
 import com.badlogic.gdx.scenes.scene2d.utils.TextureRegionDrawable;
 
 import net.fourbytes.shadow.Block;
 import net.fourbytes.shadow.Entity;
 import net.fourbytes.shadow.Layer;
 import net.fourbytes.shadow.Level;
 import net.fourbytes.shadow.Shadow;
 import net.fourbytes.shadow.TypeBlock;
 import net.fourbytes.shadow.mod.ModLoader;
 
 public abstract class BlockType {
 	
 	public static enum LogicType {
 		INPUT,
 		OUTPUT,
 		PUTPUT;
 	}
 	
 	public String[] attr;
 	public transient TypeBlock block;
 	public String subtype;
 	
 	public BlockType() {
 	}
 	
 	public static Block getInstance(String subtype, float x, float y, Layer layer) {
 		Block b = ModLoader.getTypeBlock(subtype, x, y, layer);
 		if (b != null) {
 			return b;
 		}
 		String bsubtype = subtype;
 		try {
 			String classname = subtype;
 			Object[] args = new Object[0];
 			Class[] argtypes = new Class[0];
 			if (subtype.contains(".")) {
 				String[] split = subtype.split("\\.");
 				classname = split[0];
 				args = new Object[split.length-1];
 				System.arraycopy(split, 1, args, 0, args.length);
 				argtypes = new Class[args.length];
 				for (int i = 0; i < args.length; i++) {
 					try {
 						args[i] = Integer.parseInt((String) args[i]);
 						argtypes[i] = int.class;
 					} catch (Throwable e) {
 						try {
 							args[i] = Float.parseFloat((String) args[i]);
 							argtypes[i] = float.class;
 						} catch (Throwable e1) {
 							try {
 								args[i] = ((String) args[i]).replace("\\|", "\\.");
 								argtypes[i] = String.class;
 							} catch (Throwable e2) {
 							}
 						}
 					}
 					if (argtypes[i] == null) {
 						argtypes[i] = args[i].getClass();
 					}
 				}
 			}
 			Class c = BlockType.class.getClassLoader().loadClass("net.fourbytes.shadow.blocks."+classname);
 			/*String tmp = "";
 			for (Object o : args) {
 				tmp += o+" ";
 			}
 			if (!tmp.isEmpty()) {
 				System.out.println(tmp);
 			}*/
 			BlockType instance = (BlockType) c.getConstructor(argtypes).newInstance(args);
 			instance.subtype = bsubtype;
 			Block block = new TypeBlock(new Vector2(x, y), layer, instance);
 			return block;
 		} catch (Throwable t) {
 			String imgname = subtype.toLowerCase();
 			if (imgname.startsWith("block")) {
 				imgname = imgname.substring(5);
 			}
 			imgname = "block_"+imgname;
 			BlockType instance = new BlockImage(imgname);
 			instance.subtype = bsubtype;
 			Block block = new TypeBlock(new Vector2(x, y), layer, instance);
 			return block;
 		}
 	}
 	
 	public boolean imgupdate = true;
 	public boolean cantint = false;
 	Image ii;
 	TextureRegionDrawable trd;
 	public Image getImage() {
 		if (imgupdate || ii == null) {
 			if (!block.reuseImage) {
 				Image img = new Image(getTexture());
 				ii = img;
 			} else {
 				if (ii == null) {
 					trd = new TextureRegionDrawable(getTexture());
 					ii = new Image(trd);
 				} else {
 					trd.setRegion(getTexture());
 					ii.setDrawable(trd);
 				}
 			}
 			imgupdate = false;
 			cantint = true;
 			return ii;
 		} else {
 			return ii;
 		}
 	}
 	public abstract TextureRegion getTexture();
 	
 	public void tick() {
 	}
 	
 	public void collide(Entity e) {
 	}
 	
 	public void preRender() {
 		block.tmpimg = block.getImage();
 		if (block.tmpimg != null) {
 			block.tmpimg.setScaleY(-1f);
 			//i.setPosition(pos.x * Shadow.dispw/Shadow.vieww, pos.y * Shadow.disph/Shadow.viewh);
 			block.tmpimg.setPosition(block.pos.x + block.renderoffs.x, block.pos.y + block.rec.height + block.renderoffs.y);
 			block.tmpimg.setSize(block.rec.width + block.renderoffs.width, block.rec.height + block.renderoffs.height);
 		} else {
 			System.out.println("I: null; S: "+toString());
 		}
 	}
 	
 	public void render() {
 		if (block.tmpimg != null) {
 			block.tmpimg.draw(Shadow.spriteBatch, block.alpha);
 		} else {
 			//System.out.println("I: null; S: "+toString());
 		}
 	}
 	
 }
