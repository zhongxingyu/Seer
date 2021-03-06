 package littleblocks.world;
 
 import littleblocks.core.LBCore;
 import littleblocks.core.LoggerLittleBlocks;
 import littleblocks.tileentities.TileEntityLittleBlocks;
 import net.minecraft.src.Block;
 import net.minecraft.src.Chunk;
 import net.minecraft.src.Entity;
 import net.minecraft.src.EnumSkyBlock;
 import net.minecraft.src.IChunkProvider;
 import net.minecraft.src.MovingObjectPosition;
 import net.minecraft.src.TileEntity;
 import net.minecraft.src.Vec3;
 import net.minecraft.src.World;
 import net.minecraft.src.WorldProvider;
 import net.minecraft.src.WorldSettings;
 import net.minecraftforge.common.ForgeDirection;
 import cpw.mods.fml.common.Side;
 import cpw.mods.fml.common.asm.SideOnly;
 
 public class LittleWorld extends World {
 
 	protected int ticksInWorld = 0;
 	protected static final int MAX_TICKS_IN_WORLD = 5;
 
 	protected World realWorld;
 
 	@SideOnly(Side.CLIENT)
 	public LittleWorld(World world) {
 		super(
 				world.getSaveHandler(),
 					"LittleBlocksWorld",
 					LBCore.littleProviderClient,
 					new WorldSettings(world.getWorldInfo().getSeed(), world
 							.getWorldInfo()
 								.getGameType(), world
 							.getWorldInfo()
 								.isMapFeaturesEnabled(), world
 							.getWorldInfo()
 								.isHardcoreModeEnabled(), world
 							.getWorldInfo()
 								.getTerrainType()), null);
 		this.realWorld = world;
 		LoggerLittleBlocks.getInstance(
 				LoggerLittleBlocks.filterClassName(
 						this.getClass().toString()
 				)
 		).write(
 				this.isRemote,
 				"LittleWorld[" + world.toString() + "].("+ 
 						world.getWorldInfo().getSeed() + ", " +
 						world.getWorldInfo().getGameType() + ", " +
 						world.getWorldInfo().isMapFeaturesEnabled() + ", " +
 						world.getWorldInfo().isHardcoreModeEnabled() + ", " +
 						world.getWorldInfo().getTerrainType() + ").realWorld[" + realWorld.toString() + "]",
 				LoggerLittleBlocks.LogLevel.DEBUG
 		);
 	}
 
 	public LittleWorld(World world, WorldProvider worldprovider) {
 		super(world.getSaveHandler(), "LittleBlocksWorld", new WorldSettings(
 				world.getWorldInfo().getSeed(),
 					world.getWorldInfo().getGameType(),
 					world.getWorldInfo().isMapFeaturesEnabled(),
 					world.getWorldInfo().isHardcoreModeEnabled(),
 					world.getWorldInfo().getTerrainType()), worldprovider, null);
 		this.realWorld = world;
 		LoggerLittleBlocks.getInstance(
 				LoggerLittleBlocks.filterClassName(
 						this.getClass().toString()
 				)
 		).write(
 				this.isRemote,
 				"LittleWorld[" + world.toString() + "].("+ 
 						world.getWorldInfo().getSeed() + ", " +
 						world.getWorldInfo().getGameType() + ", " +
 						world.getWorldInfo().isMapFeaturesEnabled() + ", " +
 						world.getWorldInfo().isHardcoreModeEnabled() + ", " +
 						world.getWorldInfo().getTerrainType() + ").realWorld[" + realWorld.toString() + "]",
 				LoggerLittleBlocks.LogLevel.DEBUG
 		);
 	}
 
 	@Override
 	public void tick() {
 		super.tick();
 		this.ticksInWorld++;
 		if (this.isRemote) {
 			this.setWorldTime(this.getWorldTime() + 1L);
 		}
 		if (this.ticksInWorld >= MAX_TICKS_IN_WORLD) {
 			this.ticksInWorld = 0;
 		}
 	}
 
 	/**
 	 * Runs through the list of updates to run and ticks them
 	 */
 	@Override
 	public boolean tickUpdates(boolean tick) {
 		LoggerLittleBlocks.getInstance(
 				LoggerLittleBlocks.filterClassName(
 						this.getClass().toString()
 				)
 		).write(
 				this.isRemote,
 				"tickUpdates(tick:" + tick + ")",
 				LoggerLittleBlocks.LogLevel.DEBUG
 		);
 		return false;
 	}
 
 	@Override
 	public int getSkyBlockTypeBrightness(EnumSkyBlock enumskyblock, int x, int y, int z) {
 		LoggerLittleBlocks.getInstance(
 				LoggerLittleBlocks.filterClassName(
 						this.getClass().toString()
 				)
 		).write(
 				this.isRemote,
 				"getSkyBlockTypeBrightness(" + enumskyblock + ", " + 
 				x + ", " + y + ", " + z + ")",
 				LoggerLittleBlocks.LogLevel.DEBUG
 		);
 		return super.getSkyBlockTypeBrightness(
 				enumskyblock,
 				x >> 3,
 				y >> 3,
 				z >> 3);
 	}
 
 	@Override
 	public int getLightBrightnessForSkyBlocks(int x, int y, int z, int l) {
 		LoggerLittleBlocks.getInstance(
 				LoggerLittleBlocks.filterClassName(
 						this.getClass().toString()
 				)
 		).write(
 				this.isRemote,
 				"getLightBrightnessForSkyBlocks(" + x + ", " + y + ", " + z + "[" + l + ")",
 				LoggerLittleBlocks.LogLevel.DEBUG
 		);
 		return realWorld.getLightBrightnessForSkyBlocks(
 				x >> 3,
 				y >> 3,
 				z >> 3,
 				l);
 	}
 
 	@SideOnly(Side.CLIENT)
 	@Override
 	public float getBrightness(int x, int y, int z, int l) {
 		if (realWorld != null) {
 			LoggerLittleBlocks.getInstance(
 					LoggerLittleBlocks.filterClassName(
 							this.getClass().toString()
 					)
 			).write(
 					this.isRemote,
 					"getBrightness" + x + ", " + y + ", " + z + "[" + l + "]).[" +
 					realWorld.getBrightness(x >> 3, y >> 3, z >> 3, l) + "]",
 					LoggerLittleBlocks.LogLevel.DEBUG
 			);
 			return realWorld.getBrightness(x >> 3, y >> 3, z >> 3, l);
 		}
 		LoggerLittleBlocks.getInstance(
 				LoggerLittleBlocks.filterClassName(
 						this.getClass().toString()
 				)
 		).write(
 				this.isRemote,
 				"getBrightness(null)",
 				LoggerLittleBlocks.LogLevel.DEBUG
 		);
 		return 0;
 	}
 
 	@SideOnly(Side.CLIENT)
 	@Override
 	public int getBlockLightValue(int x, int y, int z) {
 		if (realWorld != null) {
 			LoggerLittleBlocks.getInstance(
 					LoggerLittleBlocks.filterClassName(
 							this.getClass().toString()
 					)
 			).write(
 					this.isRemote,
 					"getBlockLightValue(" + x + ", " + y + ", " + z + ").[" +
 					realWorld.getBlockLightValue(x >> 3, y >> 3, z >> 3) + "]",
 					LoggerLittleBlocks.LogLevel.DEBUG
 			);
 			return realWorld.getBlockLightValue(x >> 3, y >> 3, z >> 3);
 		}
 		return 0;
 	}
 
 	@Override
 	public void setSpawnLocation() {
 	}
 
 	public boolean isOutdated(World world) {
 		//if (!world.isRemote && !this.isRemote) {
 			boolean outdated = !realWorld.equals(world);
 			if (outdated) {
 				LoggerLittleBlocks.getInstance(
 						LoggerLittleBlocks.filterClassName(
 								this.getClass().toString()
 						)
 				).write(
 						this.isRemote,
 						"isOutDated(" + world.toString() + ").[" +
 						outdated + "]",
 						LoggerLittleBlocks.LogLevel.DEBUG
 				);
 			}
 			return outdated;
 		//}
 		//return true;
 	}
 
 	@Override
 	public int getBlockId(int x, int y, int z) {
 		if (x < 0xfe363c80 || z < 0xfe363c80 || x >= 0x1c9c380 || z >= 0x1c9c380) {
 			LoggerLittleBlocks.getInstance(
 					LoggerLittleBlocks.filterClassName(
 							this.getClass().toString()
 					)
 			).write(
 					this.isRemote,
 					"getBlockId(" + x + ", " + y + ", " + z + ").[Out of bounds]",
 					LoggerLittleBlocks.LogLevel.DEBUG
 			);
 			return 0;
 		}
 		if (y < 0) {
 			LoggerLittleBlocks.getInstance(
 					LoggerLittleBlocks.filterClassName(
 							this.getClass().toString()
 					)
 			).write(
 					this.isRemote,
 					"getBlockId(" + x + ", " + y + ", " + z + ").[y < 0]",
 					LoggerLittleBlocks.LogLevel.DEBUG
 			);
 			return 0;
 		}
 		if (y >= this.getHeight()) {
 			LoggerLittleBlocks.getInstance(
 					LoggerLittleBlocks.filterClassName(
 							this.getClass().toString()
 					)
 			).write(
 					this.isRemote,
 					"getBlockId(" + x + ", " + y + ", " + z + ").[y >= "+ this.getHeight() + "]",
 					LoggerLittleBlocks.LogLevel.DEBUG
 			);
 			return 0;
 		} else {
 			int id = realWorld
 					.getChunkFromChunkCoords(x >> 7, z >> 7)
 						.getBlockID((x & 0x7f) >> 3, y >> 3, (z & 0x7f) >> 3);
 			if (id == LBCore.littleBlocksID) {
 				TileEntityLittleBlocks tile = (TileEntityLittleBlocks) realWorld
 						.getBlockTileEntity(x >> 3, y >> 3, z >> 3);
 				int littleBlockId = tile.getContent(x & 7, y & 7, z & 7);
 				LoggerLittleBlocks.getInstance(
 						LoggerLittleBlocks.filterClassName(
 								this.getClass().toString()
 						)
 				).write(
 						this.isRemote,
 						"getBlockId(" + x + ", " + y + ", " + z + ").[" + littleBlockId + "]",
 						LoggerLittleBlocks.LogLevel.DEBUG
 				);
 				return littleBlockId;
 			} else {
 				LoggerLittleBlocks.getInstance(
 						LoggerLittleBlocks.filterClassName(
 								this.getClass().toString()
 						)
 				).write(
 						this.isRemote,
 						"getBlockId(" + x + ", " + y + ", " + z + ").[" + id + "]",
 						LoggerLittleBlocks.LogLevel.DEBUG
 				);
 				return id;
 			}
 		}
 	}
 
 	@Override
 	public boolean spawnEntityInWorld(Entity entity) {
 		entity.setPosition(
 				entity.posX / LBCore.littleBlocksSize,
 				entity.posY / LBCore.littleBlocksSize,
 				entity.posZ / LBCore.littleBlocksSize);
 		entity.motionX /= LBCore.littleBlocksSize;
 		entity.motionY /= LBCore.littleBlocksSize;
 		entity.motionZ /= LBCore.littleBlocksSize;
 		entity.worldObj = realWorld;
 		LoggerLittleBlocks.getInstance(
 				LoggerLittleBlocks.filterClassName(
 						this.getClass().toString()
 				)
 		).write(
 				this.isRemote,
 				"spawnEntityInWorld(" + entity.entityId + ").[" +
 						entity.chunkCoordX + ", " +
 						entity.chunkCoordY + ", " +
 						entity.chunkCoordZ + "]",
 				LoggerLittleBlocks.LogLevel.DEBUG
 		);
 		return realWorld.spawnEntityInWorld(entity);
 	}
 
 	@Override
 	public int getBlockMetadata(int x, int y, int z) {
 		if (x < 0xfe363c80 || z < 0xfe363c80 || x >= 0x1c9c380 || z >= 0x1c9c380) {
 			LoggerLittleBlocks.getInstance(
 					LoggerLittleBlocks.filterClassName(
 							this.getClass().toString()
 					)
 			).write(
 					this.isRemote,
 					"getBlockMetadata(" + x + ", " + y + ", " + z + ").[Out of bounds]",
 					LoggerLittleBlocks.LogLevel.DEBUG
 			);
 			return 0;
 		}
 		if (y < 0) {
 			LoggerLittleBlocks.getInstance(
 					LoggerLittleBlocks.filterClassName(
 							this.getClass().toString()
 					)
 			).write(
 					this.isRemote,
 					"getBlockMetadata(" + x + ", " + y + ", " + z + ").[y < 0]",
 					LoggerLittleBlocks.LogLevel.DEBUG
 			);
 			return 0;
 		}
 		if (y >= this.getHeight()) {
 			LoggerLittleBlocks.getInstance(
 					LoggerLittleBlocks.filterClassName(
 							this.getClass().toString()
 					)
 			).write(
 					this.isRemote,
 					"getBlockMetadata(" + x + ", " + y + ", " + z + ").[y >= " + this.getHeight() + "]",
 					LoggerLittleBlocks.LogLevel.DEBUG
 			);
 			return 0;
 		} else {
 			int id = realWorld
 					.getChunkFromChunkCoords(x >> 7, z >> 7)
 						.getBlockID((x & 0x7f) >> 3, y >> 3, (z & 0x7f) >> 3);
 			int metadata = realWorld
 					.getChunkFromChunkCoords(x >> 7, z >> 7)
 						.getBlockMetadata(
 								(x & 0x7f) >> 3,
 								y >> 3,
 								(z & 0x7f) >> 3);
 			if (id == LBCore.littleBlocksID) {
 				TileEntityLittleBlocks tile = (TileEntityLittleBlocks) realWorld
 						.getBlockTileEntity(x >> 3, y >> 3, z >> 3);
 				int littleBlockId = tile.getContent(x & 7, y & 7, z & 7);
 				int littleMetaData = tile.getMetadata(x & 7, y & 7, z & 7); 
 				LoggerLittleBlocks.getInstance(
 						LoggerLittleBlocks.filterClassName(
 								this.getClass().toString()
 						)
 				).write(
 						this.isRemote,
 						"getBlockMetadata(" + x + ", " + y + ", " + z + ").[" + littleBlockId + ", " + littleMetaData + "]",
 						LoggerLittleBlocks.LogLevel.DEBUG
 				);
				return littleMetaData;
 			} else {
 				LoggerLittleBlocks.getInstance(
 						LoggerLittleBlocks.filterClassName(
 								this.getClass().toString()
 						)
 				).write(
 						this.isRemote,
 						"getBlockMetadata(" + x + ", " + y + ", " + z + ").[" + id + ", " + metadata + "]",
 						LoggerLittleBlocks.LogLevel.DEBUG
 				);
 				return metadata;
 			}
 		}
 	}
 
 	@Override
 	public int getHeight() {
 		return super.getHeight() * LBCore.littleBlocksSize;
 	}
 
 	@Override
 	public boolean setBlockAndMetadata(int x, int y, int z, int id, int metadata) {
 		if (x < 0xfe363c80 || z < 0xfe363c80 || x >= 0x1c9c380 || z >= 0x1c9c380) {
 			LoggerLittleBlocks.getInstance(
 					LoggerLittleBlocks.filterClassName(
 							this.getClass().toString()
 					)
 			).write(
 					this.isRemote,
 					"setBlockAndMetadata(" + x + ", " + y + ", " + z + ").[Out of bounds]",
 					LoggerLittleBlocks.LogLevel.DEBUG
 			);
 		}
 		if (y < 0) {
 			LoggerLittleBlocks.getInstance(
 					LoggerLittleBlocks.filterClassName(
 							this.getClass().toString()
 					)
 			).write(
 					this.isRemote,
 					"setBlockAndMetadata(" + x + ", " + y + ", " + z + ").[y < 0]",
 					LoggerLittleBlocks.LogLevel.DEBUG
 			);
 			return false;
 		}
 		if (y >= this.getHeight()) {
 			LoggerLittleBlocks.getInstance(
 					LoggerLittleBlocks.filterClassName(
 							this.getClass().toString()
 					)
 			).write(
 					this.isRemote,
 					"setBlockAndMetadata(" + x + ", " + y + ", " + z + ").[y >= " + this.getHeight() + "]",
 					LoggerLittleBlocks.LogLevel.DEBUG
 			);
 			return false;
 		} else {
 			boolean flag = false;
 			Chunk chunk = realWorld.getChunkFromChunkCoords(x >> 7, z >> 7);
 			if (chunk.getBlockID((x & 0x7f) >> 3, y >> 3, (z & 0x7f) >> 3) != LBCore.littleBlocksID) {
 				realWorld.setBlockWithNotify(
 						(x) >> 3,
 						y >> 3,
 						(z) >> 3,
 						LBCore.littleBlocksID);
 			}
 			TileEntityLittleBlocks tile = (TileEntityLittleBlocks) realWorld
 					.getBlockTileEntity(x >> 3, y >> 3, z >> 3);
 			int currentId = tile.getContent(x & 7, y & 7, z & 7);
 			int currentData = tile.getMetadata(x & 7, y & 7, z & 7);
 			if (currentId != id || currentData != metadata) {
 				LoggerLittleBlocks.getInstance(
 						LoggerLittleBlocks.filterClassName(
 								this.getClass().toString()
 						)
 				).write(
 						this.isRemote,
 						"setBlockAndMetadata(" + x + ", " + y + ", " + z + ").[" + id + ", " + metadata + "]",
 						LoggerLittleBlocks.LogLevel.DEBUG
 				);
 				tile.setContent(x & 7, y & 7, z & 7, id, metadata);
 				flag = true;
 			}
 			realWorld.updateAllLightTypes(x >> 3, y >> 3, z >> 3);
 			return flag;
 		}
 	}
 
 	/**
 	 * Sets the block ID and metadata of a block, optionally marking it as
 	 * needing update. Args: X,Y,Z, blockID, metadata, needsUpdate
 	 */
 	public boolean setBlockAndMetadataWithUpdate(int x, int y, int z, int blockId, int metadata, boolean needsUpdate) {
 		if (x < 0xfe363c80 || z < 0xfe363c80 || x >= 0x1c9c380 || z >= 0x1c9c380) {
 			LoggerLittleBlocks.getInstance(
 					LoggerLittleBlocks.filterClassName(
 							this.getClass().toString()
 					)
 			).write(
 					this.isRemote,
 					"setBlockAndMetadataWithUpdate(" + x + ", " + y + ", " + z + ").[Out of bounds]",
 					LoggerLittleBlocks.LogLevel.DEBUG
 			);
 			return false;
 		}
 		if (y < 0) {
 			LoggerLittleBlocks.getInstance(
 					LoggerLittleBlocks.filterClassName(
 							this.getClass().toString()
 					)
 			).write(
 					this.isRemote,
 					"setBlockAndMetadataWithUpdate(" + x + ", " + y + ", " + z + ").[y < 0]",
 					LoggerLittleBlocks.LogLevel.DEBUG
 			);
 			return false;
 		}
 		if (y >= this.getHeight()) {
 			LoggerLittleBlocks.getInstance(
 					LoggerLittleBlocks.filterClassName(
 							this.getClass().toString()
 					)
 			).write(
 					this.isRemote,
 					"setBlockAndMetadataWithUpdate(" + x + ", " + y + ", " + z + ").[y >= " + this.getHeight() + "]",
 					LoggerLittleBlocks.LogLevel.DEBUG
 			);
 			return false;
 		} else {
 			Chunk chunk = realWorld.getChunkFromChunkCoords(x >> 7, z >> 7);
 			boolean flag = false;
 			if (chunk.getBlockID((x & 0x7f) >> 3, y >> 3, (z & 0x7f) >> 3) != LBCore.littleBlocksID) {
 				realWorld.setBlockWithNotify(
 						(x) >> 3,
 						y >> 3,
 						(z) >> 3,
 						LBCore.littleBlocksID);
 			}
 			TileEntity tileentity = realWorld.getBlockTileEntity(
 					x >> 3,
 					y >> 3,
 					z >> 3);
 			if (tileentity != null && tileentity instanceof TileEntityLittleBlocks) {
 				TileEntityLittleBlocks tileentitylb = (TileEntityLittleBlocks) tileentity;
 				int currentId = tileentitylb.getContent(x & 7, y & 7, z & 7);
 				int currentData = tileentitylb.getMetadata(x & 7, y & 7, z & 7);
 				if (currentId != blockId || currentData != metadata) {
 					LoggerLittleBlocks.getInstance(
 							LoggerLittleBlocks.filterClassName(
 									this.getClass().toString()
 							)
 					).write(
 							this.isRemote,
 							"setBlockAndMetadataWithUpdate(" + x + ", " + y + ", " + z + ").[" + blockId + ", " + metadata + "]",
 							LoggerLittleBlocks.LogLevel.DEBUG
 					);
 					tileentitylb.setContent(
 							x & 7,
 							y & 7,
 							z & 7,
 							blockId,
 							metadata);
 					flag = true;
 				}
				this.markBlockForRenderUpdate(x >> 3, y >> 3, z >> 3);
 			}
 			return flag;
 		}
 	}
 
 	@Override
 	public boolean setBlock(int x, int y, int z, int id) {
 		if (x < 0xfe363c80 || z < 0xfe363c80 || x >= 0x1c9c380 || z >= 0x1c9c380) {
 			LoggerLittleBlocks.getInstance(
 					LoggerLittleBlocks.filterClassName(
 							this.getClass().toString()
 					)
 			).write(
 					this.isRemote,
 					"setBlock(" + x + ", " + y + ", " + z + ").[Out of bounds]",
 					LoggerLittleBlocks.LogLevel.DEBUG
 			);
 			return false;
 		}
 		if (y < 0) {
 			LoggerLittleBlocks.getInstance(
 					LoggerLittleBlocks.filterClassName(
 							this.getClass().toString()
 					)
 			).write(
 					this.isRemote,
 					"setBlock(" + x + ", " + y + ", " + z + ").[y < 0]",
 					LoggerLittleBlocks.LogLevel.DEBUG
 			);
 			return false;
 		}
 		if (y >= this.getHeight()) {
 			LoggerLittleBlocks.getInstance(
 					LoggerLittleBlocks.filterClassName(
 							this.getClass().toString()
 					)
 			).write(
 					this.isRemote,
 					"setBlock(" + x + ", " + y + ", " + z + ").[y >= " + this.getHeight() + "]",
 					LoggerLittleBlocks.LogLevel.DEBUG
 			);
 			return false;
 		} else {
 			boolean flag = false;
 			Chunk chunk = realWorld.getChunkFromChunkCoords(x >> 7, z >> 7);
 			if (chunk.getBlockID((x & 0x7f) >> 3, y >> 3, (z & 0x7f) >> 3) != LBCore.littleBlocksID) {
 				realWorld.setBlockWithNotify(
 						(x) >> 3,
 						y >> 3,
 						(z) >> 3,
 						LBCore.littleBlocksID);
 			}
 			TileEntityLittleBlocks tile = (TileEntityLittleBlocks) realWorld
 					.getBlockTileEntity(x >> 3, y >> 3, z >> 3);
 			int currentId = tile.getContent(x & 7, y & 7, z & 7);
 			if (currentId != id) {
 				LoggerLittleBlocks.getInstance(
 						LoggerLittleBlocks.filterClassName(
 								this.getClass().toString()
 						)
 				).write(
 						this.isRemote,
 						"setBlock(" + x + ", " + y + ", " + z + ").[" + id + "]",
 						LoggerLittleBlocks.LogLevel.DEBUG
 				);
 				tile.setContent(x & 7, y & 7, z & 7, id);
 				flag = true;
 			}
 			realWorld.updateAllLightTypes(x >> 3, y >> 3, z >> 3);
 			return flag;
 		}
 	}
 
 	@Override
 	public boolean setBlockMetadata(int x, int y, int z, int metadata) {
 		if (x < 0xfe363c80 || z < 0xfe363c80 || x >= 0x1c9c380 || z >= 0x1c9c380) {
 			LoggerLittleBlocks.getInstance(
 					LoggerLittleBlocks.filterClassName(
 							this.getClass().toString()
 					)
 			).write(
 					this.isRemote,
 					"setBlockMetadata(" + x + ", " + y + ", " + z + ").[Out of bounds]",
 					LoggerLittleBlocks.LogLevel.DEBUG
 			);
 			return false;
 		}
 		if (y < 0) {
 			LoggerLittleBlocks.getInstance(
 					LoggerLittleBlocks.filterClassName(
 							this.getClass().toString()
 					)
 			).write(
 					this.isRemote,
 					"setBlockMetadata(" + x + ", " + y + ", " + z + ").[y < 0]",
 					LoggerLittleBlocks.LogLevel.DEBUG
 			);
 			return false;
 		}
 		if (y >= this.getHeight()) {
 			LoggerLittleBlocks.getInstance(
 					LoggerLittleBlocks.filterClassName(
 							this.getClass().toString()
 					)
 			).write(
 					this.isRemote,
 					"setBlockMetadata(" + x + ", " + y + ", " + z + ").[y >= " + this.getHeight() + "]",
 					LoggerLittleBlocks.LogLevel.DEBUG
 			);
 			return false;
 		} else {
 			Chunk chunk = realWorld.getChunkFromChunkCoords(x >> 7, z >> 7);
 			if (chunk.getBlockID((x & 0x7f) >> 3, y >> 3, (z & 0x7f) >> 3) == LBCore.littleBlocksID) {
 				TileEntityLittleBlocks tile = (TileEntityLittleBlocks) realWorld
 						.getBlockTileEntity(x >> 3, y >> 3, z >> 3);
 				int currentData = tile.getMetadata(x & 7, y & 7, z & 7);
 				if (currentData != metadata) {
 					int id = tile.getContent(x & 7, y & 7, z & 7);
 					LoggerLittleBlocks.getInstance(
 							LoggerLittleBlocks.filterClassName(
 									this.getClass().toString()
 							)
 					).write(
 							this.isRemote,
 							"setBlockMetadata(" + x + ", " + y + ", " + z + ").[" + id + ", " + metadata + "]",
 							LoggerLittleBlocks.LogLevel.DEBUG
 					);
 					tile.setMetadata(x & 7, y & 7, z & 7, metadata);
 				}
 			} else {
 				LoggerLittleBlocks.getInstance(
 						LoggerLittleBlocks.filterClassName(
 								this.getClass().toString()
 						)
 				).write(
 						this.isRemote,
 						"setBlockMetadata(" + x + ", " + y + ", " + z + ").[chunkData]",
 						LoggerLittleBlocks.LogLevel.DEBUG
 				);
 				chunk.setBlockMetadata(
 						(x & 0x7f) >> 3,
 						y >> 3,
 						(z & 0x7f) >> 3,
 						metadata);
 			}
 			realWorld.updateAllLightTypes(x >> 3, y >> 3, z >> 3);
 			return true;
 		}
 	}
 
 	@Override
 	public boolean checkChunksExist(int minX, int minY, int minZ, int maxX, int maxY, int maxZ) {
 		int xDiff = (maxX - minX) >> 1, yDiff = (maxY - minY) >> 1, zDiff = (maxZ - minZ) >> 1;
 		int xMid = (minX + maxX) >> 1, yMid = (minY + maxY) >> 1, zMid = (minZ + maxZ) >> 1;
 
 		boolean flag = realWorld.checkChunksExist(
 				(xMid >> 3) - xDiff,
 				(yMid >> 3) - yDiff,
 				(zMid >> 3) - zDiff,
 				(xMid >> 3) + xDiff,
 				(yMid >> 3) + yDiff,
 				(zMid >> 3) + zDiff);
 		LoggerLittleBlocks.getInstance(
 				LoggerLittleBlocks.filterClassName(
 						this.getClass().toString()
 				)
 		).write(
 				this.isRemote,
 				"checkChunkExist(" + minX + ", " + minY + ", " + minZ + ", " + maxX + ", " + maxY + ", " + maxZ + ").[" + flag + "]",
 				LoggerLittleBlocks.LogLevel.DEBUG
 		);
 
 		return flag;
 	}
 
 	@Override
 	public void notifyBlocksOfNeighborChange(int x, int y, int z, int side) {
 		LoggerLittleBlocks.getInstance(
 				LoggerLittleBlocks.filterClassName(
 						this.getClass().toString()
 				)
 		).write(
 				this.isRemote,
				"notifyBlocksOfNeighborChange(" + x + ", " + y + ", " + z + ").[" + side + "]",
 				LoggerLittleBlocks.LogLevel.DEBUG
 		);
 		notifyBlockOfNeighborChange(x - 1, y, z, side);
 		notifyBlockOfNeighborChange(x + 1, y, z, side);
 		notifyBlockOfNeighborChange(x, y - 1, z, side);
 		notifyBlockOfNeighborChange(x, y + 1, z, side);
 		notifyBlockOfNeighborChange(x, y, z - 1, side);
 		notifyBlockOfNeighborChange(x, y, z + 1, side);
		this.markBlockForUpdate(x, y, z);
 	}
 
 	private void notifyBlockOfNeighborChange(int x, int y, int z, int side) {
 		World world;
 		int blockId = realWorld.getBlockId(x >> 3, y >> 3, z >> 3);
 		if (blockId == LBCore.littleBlocksID) {
 			LoggerLittleBlocks.getInstance(
 					LoggerLittleBlocks.filterClassName(
 							this.getClass().toString()
 					)
 			).write(
 					this.isRemote,
 					"notifyBlocksOfNeighbourChange(" + x + ", " + y + ", " + z + ").[" + side + "] | littleWorld",
 					LoggerLittleBlocks.LogLevel.DEBUG
 			);
 			world = this;
 		} else {
 			LoggerLittleBlocks.getInstance(
 					LoggerLittleBlocks.filterClassName(
 							this.getClass().toString()
 					)
 			).write(
 					this.isRemote,
 					"notifyBlocksOfNeighbourChange(" + x + ", " + y + ", " + z + ").[" + side + "] | realWorld",
 					LoggerLittleBlocks.LogLevel.DEBUG
 			);
 			x >>= 3;
 			y >>= 3;
 			z >>= 3;
 			world = realWorld;
 		}
		if (!realWorld.editingBlocks && !world.isRemote) {
 			Block block = Block.blocksList[world.getBlockId(x, y, z)];
 			if (block != null) {
 				LoggerLittleBlocks.getInstance(
 						LoggerLittleBlocks.filterClassName(
 								this.getClass().toString()
 						)
 				).write(
 						this.isRemote,
 						"notifyBlocksOfNeighbourChange(" + x + ", " + y + ", " + z + ").[" + side + "]",
 						LoggerLittleBlocks.LogLevel.DEBUG
 				);
 				block.onNeighborBlockChange(world, x, y, z, side);
 			}
 		}
		world.markBlockForRenderUpdate(x, y, z);
 	}
 
 	/*
 	 * public void idModified(BlockLittleBlocksBlock lbb, int lastId) {
 	 * realWorld.updateAllLightTypes( lbb.getParentX(), lbb.getParentY(),
 	 * lbb.getParentZ()); }
 	 */
 
 	public void idModified(int lastBlockId, int x, int y, int z, int side, int littleX, int littleY, int littleZ, int blockId, int metadata) {
 		realWorld.updateAllLightTypes(x, y, z);
 		LoggerLittleBlocks.getInstance(
 				LoggerLittleBlocks.filterClassName(
 						this.getClass().toString()
 				)
 		).write(
 				this.isRemote,
 				"idModified(" + x + ", " + y + ", " + z + ").[" + side + "].(" + littleX + ", " + littleY + ", " + littleZ + ", " + blockId + ", " + metadata + ")",
 				LoggerLittleBlocks.LogLevel.DEBUG
 		);
 	}
 
 	/*
 	 * public void metadataModified(BlockLittleBlocksBlock lbb) { }
 	 */
 
 	public void metadataModified(int x, int y, int z, int side, int littleX, int littleY, int littleZ, int blockId, int metadata) {
 		LoggerLittleBlocks.getInstance(
 				LoggerLittleBlocks.filterClassName(
 						this.getClass().toString()
 				)
 		).write(
 				this.isRemote,
 				"metadataModified(" + x + ", " + y + ", " + z + ").[" + side + "].(" + littleX + ", " + littleY + ", " + littleZ + ", " + blockId + ", " + metadata + ")",
 				LoggerLittleBlocks.LogLevel.DEBUG
 		);
 	}
 
 	@Override
 	public TileEntity getBlockTileEntity(int x, int y, int z) {
 		if (x < 0xfe363c80 || z < 0xfe363c80 || x >= 0x1c9c380 || z >= 0x1c9c380) {
 			LoggerLittleBlocks.getInstance(
 					LoggerLittleBlocks.filterClassName(
 							this.getClass().toString()
 					)
 			).write(
 					this.isRemote,
 					"getBlockTileEntity(" + x + ", " + y + ", " + z + ").[Out of bounds]",
 					LoggerLittleBlocks.LogLevel.DEBUG
 			);
 			return null;
 		}
 		if (y < 0) {
 			LoggerLittleBlocks.getInstance(
 					LoggerLittleBlocks.filterClassName(
 							this.getClass().toString()
 					)
 			).write(
 					this.isRemote,
 					"getBlockMetadata(" + x + ", " + y + ", " + z + ").[y < 0]",
 					LoggerLittleBlocks.LogLevel.DEBUG
 			);
 			return null;
 		}
 		if (y >= this.getHeight()) {
 			return null;
 		} else {
 			Chunk chunk = realWorld.getChunkFromChunkCoords(x >> 7, z >> 7);
 			if (chunk.getBlockID((x & 0x7f) >> 3, y >> 3, (z & 0x7f) >> 3) == LBCore.littleBlocksID) {
 				TileEntityLittleBlocks tile = (TileEntityLittleBlocks) realWorld
 						.getBlockTileEntity(x >> 3, y >> 3, z >> 3);
 				return tile.getTileEntity(x & 7, y & 7, z & 7);
 			} else {
 				return realWorld.getBlockTileEntity(x >> 3, y >> 3, z >> 3);
 			}
 		}
 	}
 
 	@Override
 	public void setBlockTileEntity(int x, int y, int z, TileEntity tileEntity) {
 		if (x < 0xfe363c80 || z < 0xfe363c80 || x >= 0x1c9c380 || z >= 0x1c9c380) {
 			return;
 		}
 		if (y < 0) {
 			return;
 		}
 		if (y >= this.getHeight()) {
 			return;
 		} else {
 			Chunk chunk = realWorld.getChunkFromChunkCoords(x >> 7, z >> 7);
 			if (chunk.getBlockID((x & 0x7f) >> 3, y >> 3, (z & 0x7f) >> 3) == LBCore.littleBlocksID) {
 				TileEntityLittleBlocks tile = (TileEntityLittleBlocks) realWorld
 						.getBlockTileEntity(x >> 3, y >> 3, z >> 3);
 				tile.setTileEntity(x & 7, y & 7, z & 7, tileEntity);
 			} else {
 				realWorld
 						.setBlockTileEntity(x >> 3, y >> 3, z >> 3, tileEntity);
 			}
 			return;
 		}
 	}
 
 	@Override
 	public void removeBlockTileEntity(int x, int y, int z) {
 		if (x < 0xfe363c80 || z < 0xfe363c80 || x >= 0x1c9c380 || z >= 0x1c9c380) {
 			return;
 		}
 		if (y < 0) {
 			return;
 		}
 		if (y >= this.getHeight()) {
 			return;
 		} else {
 			Chunk chunk = realWorld.getChunkFromChunkCoords(x >> 7, z >> 7);
 			if (chunk.getBlockID((x & 0x7f) >> 3, y >> 3, (z & 0x7f) >> 3) == LBCore.littleBlocksID) {
 				TileEntityLittleBlocks tile = (TileEntityLittleBlocks) realWorld
 						.getBlockTileEntity(x >> 3, y >> 3, z >> 3);
 				tile.removeTileEntity(x & 7, y & 7, z & 7);
 			}
 		}
 	}
 
 	@Override
 	public boolean isBlockSolidOnSide(int x, int y, int z, ForgeDirection side, boolean _default) {
 		if (x < 0xfe363c80 || z < 0xfe363c80 || x >= 0x1c9c380 || z >= 0x1c9c380) {
 			LoggerLittleBlocks.getInstance(
 					LoggerLittleBlocks.filterClassName(
 							this.getClass().toString()
 					)
 			).write(
 					this.isRemote,
 					"isBlockSolidOnSide(" + x + ", " + y + ", " + z + ").[" + side + "].(" + _default + ")",
 					LoggerLittleBlocks.LogLevel.DEBUG
 			);
 			return _default;
 		}
 
 		Chunk chunk = realWorld.getChunkFromChunkCoords(x >> 7, z >> 7);
 		if (chunk == null || chunk.isEmpty()) {
 			return _default;
 		}
 
 		Block block = Block.blocksList[getBlockId(x, y, z)];
 		if (block == null) {
 			return false;
 		}
 
 		return block.isBlockSolidOnSide(this, x, y, z, side);
 	}
 
 	@Override
 	public void markBlockForUpdate(int x, int y, int z) {
 		this.realWorld.markBlockForUpdate(x >> 3, y >> 3, z >> 3);
 	}
 
 	@Override
 	public void markBlockForRenderUpdate(int x, int y, int z) {
 		this.realWorld.markBlockForRenderUpdate(x >> 3, y >> 3, z >> 3);
 	}
 
 	@Override
 	public void playSoundEffect(double x, double y, double z, String s, float f, float f1) {
 		this.realWorld.playSoundEffect(
 				x / LBCore.littleBlocksSize,
 				y / LBCore.littleBlocksSize,
 				z / LBCore.littleBlocksSize,
 				s,
 				f,
 				f1);
 	}
 
 	@Override
 	public void playRecord(String s, int x, int y, int z) {
 		this.realWorld.playRecord(s, x, y, z);
 	}
 
 	@Override
 	public void playAuxSFX(int x, int y, int z, int l, int i1) {
 		this.realWorld.playAuxSFX(
 				x / LBCore.littleBlocksSize,
 				y / LBCore.littleBlocksSize,
 				z / LBCore.littleBlocksSize,
 				l,
 				i1);
 	}
 
 	@Override
 	public void spawnParticle(String s, double x, double y, double z, double d3, double d4, double d5) {
 		this.realWorld.spawnParticle(
 				s,
 				x / LBCore.littleBlocksSize,
 				y / LBCore.littleBlocksSize,
 				z / LBCore.littleBlocksSize,
 				d3,
 				d4,
 				d5);
 	}
 
 	@Override
 	public MovingObjectPosition rayTraceBlocks_do_do(Vec3 Vec3, Vec3 Vec31, boolean flag, boolean flag1) {
 		Vec3.xCoord *= LBCore.littleBlocksSize;
 		Vec3.yCoord *= LBCore.littleBlocksSize;
 		Vec3.zCoord *= LBCore.littleBlocksSize;
 
 		Vec31.xCoord *= LBCore.littleBlocksSize;
 		Vec31.yCoord *= LBCore.littleBlocksSize;
 		Vec31.zCoord *= LBCore.littleBlocksSize;
 		return super.rayTraceBlocks_do_do(Vec3, Vec31, flag, flag1);
 	}
 
 	@Override
 	protected IChunkProvider createChunkProvider() {
 		return new LittleChunkProvider(this);
 	}
 
 	public World getRealWorld() {
 		return this.realWorld;
 	}
 
 	@Override
 	public Entity getEntityByID(int entityId) {
 		return null;
 	}
 }
