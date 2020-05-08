 package net.minecraft.src.mystlinkingbook;
 
 import net.minecraft.client.Minecraft;
 import net.minecraft.src.BlockCloth;
 import net.minecraft.src.ChunkProviderLoadOrGenerate;
 import net.minecraft.src.EntityPlayer;
 import net.minecraft.src.EntityPlayerSP;
 import net.minecraft.src.IChunkProvider;
 import net.minecraft.src.ModLoader;
 import net.minecraft.src.NBTTagCompound;
 import net.minecraft.src.World;
 import net.minecraft.src.WorldProvider;
 
import org.lwjgl.input.Keyboard;
import org.lwjgl.input.Mouse;

 /**
  * Contains the methods to interact with the datas of the Linking Books.<br>
  * <br>
  * The datas of the Linking Books are stored in a format called NBT (Named Binary Tag), like any other map datas in Minecraft.<br>
  * <br>
  * There is only 1 instance of this class.
  * 
  * @author ziliss
  * @since 0.1a
  */
 public class LinkingBook {
 	
 	public LinkPreloader linkPreloader = new LinkPreloader(ModLoader.getMinecraftInstance());;
 	
 	public AgesManager agesManager = new AgesManager();
 	
 	public LinkingBook() {
 	}
 	
 	public boolean isWritten(NBTTagCompound nbttagcompound) {
 		return nbttagcompound.getBoolean("dest");
 	}
 	
 	public boolean write(NBTTagCompound nbttagcompound, EntityPlayer entityplayer, int nbPages, boolean unstable) {
 		if (nbttagcompound.getBoolean("dest")) return false;
 		nbttagcompound.setBoolean("dest", true);
 		nbttagcompound.setDouble("destX", entityplayer.posX);
 		nbttagcompound.setDouble("destY", entityplayer.posY);
 		nbttagcompound.setDouble("destZ", entityplayer.posZ);
 		nbttagcompound.setFloat("destRotYaw", entityplayer.rotationYaw);
 		nbttagcompound.setFloat("destRotPitch", entityplayer.rotationPitch);
 		nbttagcompound.setInteger("destDim", entityplayer.dimension);
 		nbttagcompound.setInteger("nbPages", nbPages);
 		nbttagcompound.setInteger("maxPages", nbPages);
 		nbttagcompound.setBoolean("unstable", unstable);
 		return true;
 	}
 	
 	public String getName(NBTTagCompound nbttagcompound) {
 		if (nbttagcompound == null) return "";
 		else return nbttagcompound.getString("name");
 	}
 	
 	public void setName(NBTTagCompound nbttagcompound, String name) {
 		nbttagcompound.setString("name", name);
 	}
 	
 	public int getNbPages(NBTTagCompound nbttagcompound) {
 		if (nbttagcompound == null) return 0;
 		else return nbttagcompound.getInteger("nbPages");
 	}
 	
 	public int getMaxPages(NBTTagCompound nbttagcompound) {
 		if (nbttagcompound == null) return 0;
 		else return nbttagcompound.getInteger("maxPages");
 	}
 	
 	public int addPages(NBTTagCompound nbttagcompound, int nb) {
 		if (nb <= 0) return 0;
 		else if (nbttagcompound == null) return nb;
 		else {
 			int nbPages = nbttagcompound.getInteger("nbPages");
 			int available = nbttagcompound.getInteger("maxPages") - nbPages;
 			int added = nb;
 			if (added > available) {
 				added = available;
 			}
 			nbttagcompound.setInteger("nbPages", nbPages + added);
 			return nb - added;
 		}
 	}
 	
 	public int removePages(NBTTagCompound nbttagcompound, int nb) {
 		if (nb <= 0 || nbttagcompound == null) return 0;
 		else {
 			int available = nbttagcompound.getInteger("nbPages");
 			int removed = nb;
 			if (removed > available) {
 				removed = available;
 			}
 			nbttagcompound.setInteger("nbPages", available - removed);
 			return removed;
 		}
 	}
 	
 	public int getPagesColor(NBTTagCompound nbttagcompound) {
 		return nbttagcompound.getInteger("color");
 	}
 	
 	public void setPagesColorFromDye(NBTTagCompound nbttagcompound, int dyeColor) {
 		int color = BlockCloth.getBlockFromDye(dyeColor);
 		nbttagcompound.setInteger("color", color);
 	}
 	
 	public boolean isUnstable(NBTTagCompound nbttagcompound) {
 		return nbttagcompound.getBoolean("unstable");
 	}
 	
 	public boolean doLinkToDifferentAge(TileEntityLinkingBook tileEntityLinkingBook, EntityPlayer entityplayer) {
 		NBTTagCompound nbttagcompound = tileEntityLinkingBook.nbttagcompound_linkingBook;
 		if (!nbttagcompound.getBoolean("dest")) return false;
 		int destX = (int)nbttagcompound.getDouble("destX");
 		int destY = (int)(nbttagcompound.getDouble("destY") - entityplayer.yOffset); // yOffset: prevent the tiny jump when teleporting
 		int destZ = (int)nbttagcompound.getDouble("destZ");
 		int destDim = nbttagcompound.getInteger("destDim");
 		int bookX = tileEntityLinkingBook.xCoord;
 		int bookY = tileEntityLinkingBook.yCoord;
 		int bookZ = tileEntityLinkingBook.zCoord;
 		int bookDim = PrivateAccesses.World_worldInfo.getFrom(tileEntityLinkingBook.worldObj).getDimension();
 		return agesManager.linksToDifferentAge(bookX, bookY, bookZ, bookDim, destX, destY, destZ, destDim);
 	}
 	
 	public boolean doLinkChangesDimension(NBTTagCompound nbttagcompound, EntityPlayer entityplayer) {
 		return nbttagcompound.getBoolean("dest") && nbttagcompound.getInteger("destDim") != entityplayer.dimension;
 	}
 	
 	public void prepareLinking(NBTTagCompound nbttagcompound, EntityPlayer entityplayer) {
 		if (!nbttagcompound.getBoolean("dest")) return;
 		int destX = (int)nbttagcompound.getDouble("destX");
 		int destY = (int)(nbttagcompound.getDouble("destY") - entityplayer.yOffset); // yOffset: prevent the tiny jump when teleporting
 		int destZ = (int)nbttagcompound.getDouble("destZ");
 		int destDim = nbttagcompound.getInteger("destDim");
 		if (destDim == entityplayer.dimension) {
 			linkPreloader.preloadDestination(destX, destY, destZ);
 		}
 		else {
 			linkPreloader.preloadDestination(destX, destY, destZ, destDim);
 		}
 	}
 	
 	public boolean link(NBTTagCompound nbttagcompound, EntityPlayer entityplayer) {
 		if (nbttagcompound.getBoolean("dest")) {
 			double destX = nbttagcompound.getDouble("destX");
 			double destY = nbttagcompound.getDouble("destY") - entityplayer.yOffset; // yOffset: prevent the tiny jump when teleporting
 			double destZ = nbttagcompound.getDouble("destZ");
 			float destRotYaw = nbttagcompound.getFloat("destRotYaw");
 			float destRotPitch = nbttagcompound.getFloat("destRotPitch");
 			int destDim = nbttagcompound.getInteger("destDim");
 			String bookName = getName(nbttagcompound);
 			
 			if (destDim == entityplayer.dimension) {
 				teleport(destX, destY, destZ, destRotYaw, destRotPitch, bookName, entityplayer);
 				return true;
 			}
 			else {
 				teleport(destX, destY, destZ, destRotYaw, destRotPitch, destDim, bookName, entityplayer);
 				return true;
 			}
 		}
 		return false;
 	}
 	
 	public void teleport(double destX, double destY, double destZ, float destRotYaw, float destRotPitch, String bookName, EntityPlayer entityplayer) {
 		entityplayer.setLocationAndAngles(destX, destY, destZ, destRotYaw, destRotPitch);
 	}
 	
 	public void teleport(double destX, double destY, double destZ, float destRotYaw, float destRotPitch, int destDim, String bookName, EntityPlayer entityplayer) {
 		Minecraft mc = ModLoader.getMinecraftInstance();
 		World theWorld = mc.theWorld;
 		EntityPlayerSP thePlayer = mc.thePlayer;
 		
 		// Inspired by Minecraft.usePortal(int i):
 		int curDim = thePlayer.dimension;
 		thePlayer.dimension = destDim;
 		theWorld.setEntityDead(thePlayer);
 		thePlayer.isDead = false;
 		
 		World newWorld = linkPreloader.getWorld();
 		if (newWorld != null && newWorld.worldProvider.worldType != destDim) {
 			newWorld = null;
 		}
 		if (newWorld == null) {
 			newWorld = new World(theWorld, WorldProvider.getProviderForDimension(destDim));
 		}
 		
 		thePlayer.setLocationAndAngles(destX, destY, destZ, destRotYaw, destRotPitch);
 		if (thePlayer.isEntityAlive()) {
 			// Is the following not useful ?
 			newWorld.updateEntityWithOptionalForce(thePlayer, false);
 		}
 		
		// This is a workaround for a weird bug I called the "speed bug".
		// I don't know why, but sometimes after teleporting quickly multiple times to the same dimension, the player is in this list.
		// Maybe a chunkloader cache bug ?
		if (newWorld.loadedEntityList.contains(thePlayer)) {
			newWorld.loadedEntityList.remove(thePlayer);
		}
		
 		mc.changeWorld(newWorld, "Linking to " + bookName, thePlayer);
 		
 		thePlayer = mc.thePlayer; // Just in case.
 		thePlayer.worldObj = newWorld;
 		System.out.println("Teleported to " + newWorld.worldProvider.worldType);
 		if (thePlayer.isEntityAlive()) {
 			thePlayer.setLocationAndAngles(destX, destY, destZ, destRotYaw, destRotPitch);
 			newWorld.updateEntityWithOptionalForce(thePlayer, false);
 		}
		
		while (Keyboard.next()) {
			// KeyBinding.setKeyBindState(Keyboard.getEventKey(), Keyboard.getEventKeyState());
		}
		while (Mouse.next()) {
		}
 	}
 	
 	class LinkPreloader {
 		
 		public PreloadThread preloadThread = null;
 		
 		public Minecraft mc;
 		
 		public class PreloadThread extends Thread {
 			public World curWorld;
 			public int destX;
 			public int destY;
 			public int destZ;
 			public int destDim;
 			public World destWorld;
 			
 			public boolean aborted = false;
 			
 			public PreloadThread(World curWorld, double destX, double destY, double destZ) {
 				this.curWorld = curWorld;
 				this.destX = (int)Math.floor(destX);
 				this.destY = (int)Math.floor(destY);
 				this.destZ = (int)Math.floor(destZ);
 				destWorld = curWorld;
 			}
 			
 			public PreloadThread(World curWorld, double destX, double destY, double destZ, int destDim, World destWorld) {
 				this.curWorld = curWorld;
 				this.destX = (int)Math.floor(destX);
 				this.destY = (int)Math.floor(destY);
 				this.destZ = (int)Math.floor(destZ);
 				this.destDim = destDim;
 				this.destWorld = destWorld;
 			}
 			
 			@Override
 			// Insppired by Minecraft.preloadWorld():
 			public void run() {
 				int c = '\200';
 				
 				if (destWorld == null) {
 					destWorld = new World(curWorld, WorldProvider.getProviderForDimension(destDim));
 				}
 				
 				if (aborted) return;
 				if (destWorld != curWorld) {
 					IChunkProvider ichunkprovider = destWorld.getChunkProvider();
 					if (ichunkprovider instanceof ChunkProviderLoadOrGenerate) {
 						ChunkProviderLoadOrGenerate chunkproviderloadorgenerate = (ChunkProviderLoadOrGenerate)ichunkprovider;
 						chunkproviderloadorgenerate.setCurrentChunkOver(destX >> 4, destZ >> 4);
 					}
 				}
 				for (int k = -c; k <= c; k += 16) {
 					if (aborted) return;
 					for (int l = -c; l <= c; l += 16) {
 						destWorld.getBlockId(destX + k, 64, destZ + l);
 						while (destWorld.updatingLighting()) {
 						}
 					}
 				}
 				
 				if (aborted) return;
 				// mc.statFileWriter.func_27175_b();
 				mc.statFileWriter.syncStats();
 				
 				if (aborted) return;
 				curWorld.quickSaveWorld(0);
 				
 				if (aborted) return;
 				mc.getSaveLoader().flushCache();
 			}
 			
 			public void abort() {
 				aborted = true;
 			}
 		};
 		
 		public LinkPreloader(Minecraft mc) {
 			this.mc = mc;
 		}
 		
 		public void preloadDestination(double destX, double destY, double destZ) {
 			if (preloadThread != null) {
 				preloadThread.abort();
 			}
 			preloadThread = new PreloadThread(mc.theWorld, destX, destY, destZ);
 			preloadThread.setPriority(Thread.MIN_PRIORITY);
 			preloadThread.start();
 		}
 		
 		public void preloadDestination(double destX, double destY, double destZ, int destDim) {
 			World destWorld = null;
 			if (preloadThread != null) {
 				preloadThread.abort();
 				if (preloadThread.destDim == destDim) {
 					destWorld = getWorld();
 				}
 			}
 			preloadThread = new PreloadThread(mc.theWorld, destX, destY, destZ, destDim, destWorld);
 			preloadThread.setPriority(Thread.MIN_PRIORITY);
 			preloadThread.start();
 		}
 		
 		public World getWorld() {
 			if (preloadThread != null) {
 				preloadThread.abort();
 				preloadThread.setPriority(Thread.NORM_PRIORITY + 1);
 				try {
 					preloadThread.join();
 				}
 				catch (InterruptedException e) {
 					e.printStackTrace();
 				}
 				return preloadThread.destWorld;
 			}
 			else return null;
 		}
 	}
 }
