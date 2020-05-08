 // Decompiled by Jad v1.5.8g. Copyright 2001 Pavel Kouznetsov.
 // Jad home page: http://www.kpdus.com/jad.html
 // Decompiler options: packimports(3) braces deadcode 
 
 package MCP.mod_finiteliquids;
 
 import java.util.List;
 import java.util.Random;
 
 import net.minecraft.src.AxisAlignedBB;
 import net.minecraft.src.Block;
 import net.minecraft.src.Entity;
 import net.minecraft.src.EntityItem;
 import net.minecraft.src.IBlockAccess;
 import net.minecraft.src.Item;
 import net.minecraft.src.Material;
 import net.minecraft.src.Tessellator;
 import net.minecraft.src.Vec3D;
 import net.minecraft.src.World;
 import MCP.ApiController;
 import MCP.api.CustomRenderedBlockBase;
 
 // Referenced classes of package net.minecraft.src:
 //            Block, mod_NWater, World, EntityItem, 
 //            AxisAlignedBB, ItemStack, Item, BlockNWater_Ocean, 
 //            Material, BlockFire, IBlockAccess, Entity, 
 //            Vec3D
 
 public class BlockNWater extends CustomRenderedBlockBase {
 
 	protected BlockNWater(ApiController api, int textureID, Material material) {
		super(api, api.getBlockID(BlockNWater.class), textureID, material);
 		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F, 0.0725F, 1.0F);
 		setTickOnLoad(true);
 		blockResistance = 100F;
 		setLightOpacity(3);
 		setHardness(100F);
 		setBlockName("nwater");
 	}
 
 	public int getBlockTexture(IBlockAccess iblockaccess, int i, int j, int k,
 			int l) {
 		return mod_NWater.texx[5];
 	}
 
 	public boolean canBlockBePlacedAt(int i, int j, int k, int l, boolean flag) {
 		return true;
 	}
 
 	public int tickRate() {
 		return 6;
 	}
 
 	public boolean canCollideCheck(int i, boolean flag) {
 		return (i > 4) & flag;
 	}
 
 	public int idDropped(int i, Random random) {
 		return 0;
 	}
 
 	public void velocityToAddToEntity(World world, int i, int j, int k,
 			Entity entity, Vec3D vec3d) {
 		mod_NWater.waterPush(world, i, j, k, entity, vec3d);
 	}
 
 	public int quantityDropped(Random random) {
 		return 0;
 	}
 
 	public boolean isOpaqueCube() {
 		return false;
 	}
 
 	public boolean renderAsNormalBlock() {
 		return false;
 	}
 
 	public boolean canPlaceBlockAt(World world, int i, int j, int k) {
 		return true;
 	}
 
 	public void onBlockAdded(World world, int i, int j, int k) {
 		world.scheduleBlockUpdate(i, j, k, blockID, tickRate());
 	}
 
 	public void onNeighborBlockChange(World world, int i, int j, int k, int l) {
 	}
 
 	public boolean isWater(World world, int i, int j, int k) {
 		if (world.getBlockId(i, j, k) == mod_NWater.nwater.blockID) {
 			return true;
 		}
 		if (world.getBlockId(i, j, k) == mod_NWater.nwater_still.blockID) {
 			return true;
 		}
 		if (world.getBlockId(i, j, k) == mod_NWater.nlava.blockID) {
 			return true;
 		}
 		if (world.getBlockId(i, j, k) == mod_NWater.nlava_still.blockID) {
 			return true;
 		}
 		if (world.getBlockId(i, j, k) == mod_NWater.noil.blockID) {
 			return true;
 		}
 		if (world.getBlockId(i, j, k) == mod_NWater.noil_still.blockID) {
 			return true;
 		}
 		if (world.getBlockId(i, j, k) == mod_NWater.nqsand.blockID) {
 			return true;
 		}
 		if (world.getBlockId(i, j, k) == mod_NWater.nqsand_still.blockID) {
 			return true;
 		}
 		return (world.getBlockId(i, j, k) == mod_NWater.grate.blockID)
 				& (world.getBlockMetadata(i, j, k) != 10);
 	}
 
 	public boolean isRWater(World world, int i, int j, int k) {
 		if (world.getBlockId(i, j, k) == mod_NWater.nwater.blockID) {
 			return true;
 		}
 		if (world.getBlockId(i, j, k) == mod_NWater.nwater_still.blockID) {
 			return true;
 		}
 		if (world.getBlockId(i, j, k) == mod_NWater.nlava.blockID) {
 			return true;
 		}
 		if (world.getBlockId(i, j, k) == mod_NWater.nlava_still.blockID) {
 			return true;
 		}
 		if (world.getBlockId(i, j, k) == mod_NWater.noil.blockID) {
 			return true;
 		}
 		if (world.getBlockId(i, j, k) == mod_NWater.noil_still.blockID) {
 			return true;
 		}
 		if (world.getBlockId(i, j, k) == mod_NWater.nqsand.blockID) {
 			return true;
 		}
 		return world.getBlockId(i, j, k) == mod_NWater.nqsand_still.blockID;
 	}
 
 	public boolean checkGrab(int i, int j, int k) {
 		if (k == -1) {
 			return true;
 		}
 		if ((k == 1) & (i > 0)) {
 			return false;
 		}
 		if ((k == 2) & (i < 0)) {
 			return false;
 		}
 		if ((k == 3) & (j > 0)) {
 			return false;
 		}
 		return !((k == 4) & (j < 0));
 	}
 
 	public int getGrab(int i, int j) {
 		if (i < 0) {
 			return 1;
 		}
 		if (i > 0) {
 			return 2;
 		}
 		if (j < 0) {
 			return 3;
 		}
 		return j <= 0 ? 0 : 4;
 	}
 
 	public void placeWater(World world, int i, int j, int k, int l, int i1) {
 		int j1 = world.getBlockMetadata(i, j, k);
 		int k1 = world.getBlockId(i, j, k);
 		int l1 = blockID;
 		int i2 = mod_NWater.convertToActive(k1);
 		if (i2 != l1) {
 			l -= 2;
 			if (l < 1) {
 				l = 0;
 			}
 		}
 		Random random = new Random();
 		if (random.nextInt(2) == 1 && i2 > -1) {
 			l1 = i2;
 		}
 		if (k1 == mod_NWater.grate.blockID) {
 			l1 = mod_NWater.grate.blockID;
 		}
 		if ((k1 == mod_NWater.grate.blockID) & (l == 10)) {
 			Random random1 = new Random();
 			if (random1.nextInt(3) == 1) {
 				l = 11;
 			} else {
 				l = 9;
 			}
 			world.setBlockMetadataWithNotify(i, j, k, l);
 			return;
 		}
 		if ((k1 != 0) & (!isWater(world, i, j, k))) {
 			Block.blocksList[k1].dropBlockAsItem(world, i, j, k,
 					world.getBlockMetadata(i, j, k));
 		}
 		if ((j1 > l) & isRWater(world, i, j + 1, k)
 				&& j1 - l < world.getBlockMetadata(i, j + 1, k)) {
 			world.setBlockMetadata(i, j + 1, k,
 					world.getBlockMetadata(i, j + 1, k) - (j1 - l));
 			l = j1;
 		}
 		if (i1 == 0) {
 			world.setBlockAndMetadata(i, j, k, l1, l);
 		} else {
 			world.setBlockAndMetadataWithNotify(i, j, k, l1, l);
 		}
 	}
 
 	public int getThreshold() {
 		return 2;
 	}
 
 	public void grabWater(World world, int i, int j, int k, int l, int i1,
 			int j1, int k1, int l1) {
 		if (world.getBlockId(i, j - 1, k) == Block.blockDiamond.blockID) {
 			placeWater(world, i, j, k, 15, 0);
 		} else if (!borderOcean(world, i, j, k)) {
 			world.setBlockAndMetadata(i, j, k, 0, 0);
 		}
 		byte byte0 = 1;
 		byte byte1 = 1;
 		Random random = new Random();
 		if (random.nextInt(2) == 0) {
 			byte0 = -1;
 		}
 		if (random.nextInt(2) == 0) {
 			byte1 = -1;
 		}
 		boolean flag = false;
 		int i2 = getThreshold();
 		for (int j2 = 0; j2 <= 1; j2++) {
 			for (int k2 = -1; k2 <= 1; k2++) {
 				for (int l2 = -1; l2 <= 1; l2++) {
 					int i3 = k2 * byte0;
 					int j3 = l2 * byte1;
 					if (!checkGrab(i3, j3, l1)) {
 						continue;
 					}
 					if ((!flag) & (k2 == 0 || l2 == 0) & (j2 == 1)) {
 						world.getBlockId(i + i3, j + j2, k + j3);
 						int i4 = world.getBlockMetadata(i + i3, j + j2, k + j3);
 						if (isRWater(world, i + i3, j + j2, k + j3)) {
 							flag = true;
 							placeWater(world, i, j, k, i4, 0);
 							if (k1 < 0
 									|| (k1 < 15)
 									& (world.getBlockMetadata(i, j + j2, k) < i2 + 1)) {
 								if (l1 == -1) {
 									l1 = getGrab(i3, j3);
 								}
 								int k4 = k1 / 2;
 								if (k1 == k4 * 2) {
 									l1 = getGrab(i3, j3);
 								}
 								grabWater(world, i + i3, j + j2, k + j3, i, j,
 										k, k1 + 1, l1);
 							} else if ((!borderOcean(world, i, j, k))
 									& (world.getBlockId(i, j - 1, k) != Block.blockDiamond.blockID)) {
 								tryToSave(world, i, j, k);
 							}
 						}
 					}
 					if (!((!flag) & (k2 != 0 || l2 != 0) & (k2 == 0 || l2 == 0) & (j2 == 0))
 							|| i + i3 == l && k + j3 == j1) {
 						continue;
 					}
 					world.getBlockId(i + i3, j, k + j3);
 					int j4 = world.getBlockMetadata(i + i3, j, k + j3);
 					if (!isRWater(world, i + i3, j, k + j3)) {
 						continue;
 					}
 					flag = true;
 					placeWater(world, i, j, k, j4, 0);
 					if (k1 < 0 || (k1 < 15)
 							& (world.getBlockMetadata(i, j, k) < i2 + 1)) {
 						if (l1 == -1) {
 							l1 = getGrab(i3, j3);
 						}
 						int l4 = k1 / 2;
 						if (k1 == l4 * 2) {
 							l1 = getGrab(i3, j3);
 						}
 						grabWater(world, i + i3, j, k + j3, i, j, k, k1 + 1, l1);
 						continue;
 					}
 					if ((!borderOcean(world, i, j, k))
 							& (world.getBlockId(i, j - 1, k) != Block.blockDiamond.blockID)) {
 						tryToSave(world, i, j, k);
 					}
 				}
 
 			}
 
 		}
 
 		if (world.getBlockId(i, j, k) == 0) {
 			tryToSave(world, i, j, k);
 		}
 	}
 
 	private void tryToSave(World world, int i, int j, int k) {
 		boolean flag = false;
 		int l1 = j;
 		for (int i2 = 0; i2 < 1; i2++) {
 			int i1 = i + 1;
 			int j1 = l1 + i2;
 			int k1 = k;
 			int l = world.getBlockMetadata(i1, j1, k1);
 			if ((!flag)
 					& isRWater(world, i1, j1, k1)
 					& (l > 0 || world.getBlockId(i1, j1, k1) == mod_NWater.nwater_ocean.blockID)) {
 				world.setBlockMetadataWithNotify(i1, j1, k1, l - 1);
 				flag = true;
 			}
 			i1 = i - 1;
 			j1 = l1 + i2;
 			k1 = k;
 			l = world.getBlockMetadata(i1, j1, k1);
 			if ((!flag)
 					& isRWater(world, i1, j1, k1)
 					& (l > 0 || world.getBlockId(i1, j1, k1) == mod_NWater.nwater_ocean.blockID)) {
 				world.setBlockMetadataWithNotify(i1, j1, k1, l - 1);
 				flag = true;
 			}
 			i1 = i;
 			j1 = l1 + i2;
 			k1 = k + 1;
 			l = world.getBlockMetadata(i1, j1, k1);
 			if ((!flag)
 					& isRWater(world, i1, j1, k1)
 					& (l > 0 || world.getBlockId(i1, j1, k1) == mod_NWater.nwater_ocean.blockID)) {
 				world.setBlockMetadataWithNotify(i1, j1, k1, l - 1);
 				flag = true;
 			}
 			i1 = i;
 			j1 = l1 + i2;
 			k1 = k - 1;
 			l = world.getBlockMetadata(i1, j1, k1);
 			if ((!flag)
 					& isRWater(world, i1, j1, k1)
 					& (l > 0 || world.getBlockId(i1, j1, k1) == mod_NWater.nwater_ocean.blockID)) {
 				world.setBlockMetadataWithNotify(i1, j1, k1, l - 1);
 				flag = true;
 			}
 			if (i2 == 0) {
 				continue;
 			}
 			i1 = i;
 			j1 = l1 + i2;
 			k1 = k;
 			l = world.getBlockMetadata(i1, j1, k1);
 			if ((!flag)
 					& isRWater(world, i1, j1, k1)
 					& (l > 0 || world.getBlockId(i1, j1, k1) == mod_NWater.nwater_ocean.blockID)) {
 				world.setBlockMetadataWithNotify(i1, j1, k1, l - 1);
 				flag = true;
 			}
 		}
 
 		if (!flag) {
 			world.setBlockAndMetadata(i, j, k, 0, 0);
 		} else {
 			world.setBlockAndMetadataWithNotify(i, j, k, blockID, 0);
 		}
 	}
 
 	public boolean checkRecipe(World world, int i, int j, int k, int l) {
 		if ((blockID != mod_NWater.nwater.blockID)
 				& (blockID != mod_NWater.nwater_still.blockID)) {
 			return false;
 		}
 		List<Entity> list = null;
 		list = world.getEntitiesWithinAABB(net.minecraft.src.EntityItem.class,
 				AxisAlignedBB.getBoundingBoxFromPool((float) i - 1.2F,
 						(float) j - 1.2F, (float) k - 1.2F, (float) i + 2.2F,
 						(float) j + 2.2F, (float) k + 2.2F));
 		if (list.size() > 0) {
 			int i1 = world.getBlockMetadata(i, j, k);
 			boolean flag = (world.getBlockId(i, j - 1, k) == Block.blockSteel.blockID)
 					& (world.getBlockMetadata(i, j - 1, k) > 8);
 			boolean flag1 = false;
 			int j1 = 0;
 			int k1 = 0;
 			int l1 = 0;
 			int i2 = 0;
 			for (int j2 = 0; j2 < list.size(); j2++) {
 				EntityItem entityitem = (EntityItem) list.get(j2);
 				if (entityitem.item.itemID == Item.redstone.shiftedIndex) {
 					j1++;
 				}
 				if (entityitem.item.itemID == Item.ingotGold.shiftedIndex) {
 					k1++;
 				}
 				if (entityitem.item.itemID == Item.diamond.shiftedIndex) {
 					l1++;
 				}
 				if (entityitem.item.itemID == Block.sand.blockID) {
 					i2++;
 				}
 			}
 
 			int k2 = 0;
 			int l2 = 0;
 			int i3 = 0;
 			int j3 = 0;
 			if ((i1 > 10) & (k1 >= 1) & (j1 >= 3)) {
 				l2 = 1;
 				k2 = 3;
 				setArea(world, i, j, k, Block.waterStill.blockID, 3, false);
 				flag1 = true;
 			}
 			if ((i1 > 10) & (l1 >= 1) & (j1 >= 3) & flag) {
 				i3 = 1;
 				k2 = 3;
 				setArea(world, i, j, k, Block.lavaStill.blockID, 3, false);
 				flag1 = true;
 			}
 			if ((i1 > 10) & (i2 >= 5)) {
 				j3 = 5;
 				setArea(world, i, j, k, mod_NWater.nqsand.blockID, 3, true);
 				flag1 = true;
 			}
 			if (flag1) {
 				for (int k3 = 0; k3 < list.size(); k3++) {
 					EntityItem entityitem1 = (EntityItem) list.get(k3);
 					if ((k2 > 0)
 							& (entityitem1.item.itemID == Item.redstone.shiftedIndex)) {
 						entityitem1.setEntityDead();
 						k2--;
 					}
 					if ((l2 > 0)
 							& (entityitem1.item.itemID == Item.ingotGold.shiftedIndex)) {
 						entityitem1.setEntityDead();
 						l2--;
 					}
 					if ((i3 > 0)
 							& (entityitem1.item.itemID == Item.diamond.shiftedIndex)) {
 						entityitem1.setEntityDead();
 						i3--;
 					}
 					if ((j3 > 0)
 							& (entityitem1.item.itemID == Block.sand.blockID)) {
 						entityitem1.setEntityDead();
 						j3--;
 					}
 				}
 
 				return l == 0;
 			}
 		}
 		if (isRWater(world, i, j - 1, k) & (l < 4)) {
 			return checkRecipe(world, i, j - 1, k, l + 1);
 		} else {
 			return false;
 		}
 	}
 
 	public void setArea(World world, int i, int j, int k, int l, int i1,
 			boolean flag) {
 		int j1 = world.getBlockMetadata(i, j, k);
 		if (!flag) {
 			j1 = 0;
 		}
 		world.setBlockAndMetadataWithNotify(i, j, k, l, j1);
 		if (i1 > 0) {
 			if (isRWater(world, i + 1, j, k)) {
 				setArea(world, i + 1, j, k, l, i1 - 1, flag);
 			}
 			if (isRWater(world, i - 1, j, k)) {
 				setArea(world, i - 1, j, k, l, i1 - 1, flag);
 			}
 			if (isRWater(world, i, j + 1, k)) {
 				setArea(world, i, j + 1, k, l, i1 - 1, flag);
 			}
 			if (isRWater(world, i, j - 1, k)) {
 				setArea(world, i, j - 1, k, l, i1 - 1, flag);
 			}
 			if (isRWater(world, i, j, k + 1)) {
 				setArea(world, i, j, k + 1, l, i1 - 1, flag);
 			}
 			if (isRWater(world, i, j, k - 1)) {
 				setArea(world, i, j, k - 1, l, i1 - 1, flag);
 			}
 		}
 	}
 
 	public int getMetathresh() {
 		return 0;
 	}
 
 	public boolean checkSponge(World world, int i, int j, int k, int l, int i1,
 			int j1) {
 		if ((world.getBlockId(i, j, k) == Block.sponge.blockID)
 				& (world.getBlockMetadata(i, j, k) < 15)) {
 			Random random = new Random();
 			if (world.getBlockMetadata(l, i1, j1) > 2
 					|| world.isBlockOpaqueCube(l, i1 - 1, j1)) {
 				if (random.nextInt(5) == 0) {
 					world.setBlockMetadataWithNotify(i, j, k,
 							world.getBlockMetadata(i, j, k) + 1);
 				}
 				int k1 = world.getBlockMetadata(l, i1, j1) - 2;
 				if (k1 < 0) {
 					grabWater(world, l, i1, j1, l, i1, j1, 0, -3);
 				} else {
 					world.setBlockMetadataWithNotify(l, i1, j1, k1);
 				}
 				return true;
 			}
 		}
 		return false;
 	}
 
 	public float getSurfaceMull() {
 		return 1.0F;
 	}
 
 	public void updateTick(World world, int i, int j, int k, Random random) {
 		if (world.multiplayerWorld) {
 			return;
 		}
 		if (random.nextInt(30) == 0 && checkRecipe(world, i, j, k, 0)) {
 			return;
 		}
 		int l = world.getBlockMetadata(i, j, k);
 		boolean flag = false;
 		if ((!BlockNWater_Ocean.noOcean)
 				& (world.getBlockId(i, j - 1, k) == mod_NWater.nwater_ocean.blockID)
 				& (l < 4)) {
 			grabWater(world, i, j, k, i, j, k, 0, -3);
 			world.setBlockAndMetadata(i, j, k, 0, 0);
 			flag = true;
 		}
 		if ((!flag) & mod_NWater.canMoveAmt(world, i, j - 1, k, l)) {
 			if (l < 2) {
 				if (!borderOcean(world, i, j, k)) {
 					grabWater(world, i, j, k, i, j, k, 0, -1);
 				}
 				placeWater(world, i, j - 1, k, l, 1);
 			} else {
 				if (!borderOcean(world, i, j, k)) {
 					placeWater(world, i, j - 1, k, l - 1, 1);
 				}
 				placeWater(world, i, j - 1, k, 0, 1);
 			}
 			flag = true;
 		}
 		if (!flag) {
 			if (checkSponge(world, i - 1, j, k, i, j, k)) {
 				flag = true;
 			}
 			if (checkSponge(world, i + 1, j, k, i, j, k)) {
 				flag = true;
 			}
 			if (checkSponge(world, i, j - 1, k, i, j, k)) {
 				flag = true;
 			}
 			if (checkSponge(world, i, j, k - 1, i, j, k)) {
 				flag = true;
 			}
 			if (checkSponge(world, i, j, k + 1, i, j, k)) {
 				flag = true;
 			}
 		}
 		if ((!flag)
 				& (world.getBlockId(i, j - 1, k) == Block.lavaStill.blockID)) {
 			world.setBlock(i, j, k, 0);
 			return;
 		}
 		if ((!flag) & isWater(world, i, j - 1, k)) {
 			int j1 = world.getBlockMetadata(i, j - 1, k);
 			if (j1 < 15) {
 				if (j1 + (1 + l) < 16) {
 					grabWater(world, i, j, k, i, j, k, 0, -1);
 					placeWater(world, i, j - 1, k, 1 + l + j1, 1);
 					flag = true;
 				} else {
 					int l1 = (l + j1) - 15;
 					placeWater(world, i, j, k, l1, 1);
 					placeWater(world, i, j - 1, k, 15, 1);
 					flag = true;
 				}
 			}
 		}
 		if (!flag) {
 			byte byte0 = 1;
 			byte byte1 = 1;
 			if (random.nextInt(2) == 0) {
 				byte0 = -1;
 			}
 			if (random.nextInt(2) == 0) {
 				byte1 = -1;
 			}
 			for (int i2 = -1; i2 <= 1; i2++) {
 				for (int j2 = -1; j2 <= 1; j2++) {
 					int l2 = i2 * byte0;
 					int j3 = j2 * byte1;
 					if (!((!flag) & (i2 != 0 || j2 != 0) & (i2 == 0 || j2 == 0))) {
 						continue;
 					}
 					world.getBlockId(i + l2, j, k + j3);
 					world.getBlockMetadata(i + l2, j, k + j3);
 					boolean flag1 = mod_NWater.canMoveAmt(world, i + l2, j, k
 							+ j3, l);
 					if (!((!flag) & flag1)) {
 						continue;
 					}
 					world.getBlockId(i + l2, j - 1, k + j3);
 					world.getBlockMetadata(i + l2, j - 1, k + j3);
 					flag1 = mod_NWater.canMoveAmt(world, i + l2, j - 1, k + j3,
 							l);
 					if ((!flag) & flag1) {
 						if (l == 0) {
 							grabWater(world, i, j, k, i, j, k, 0, -1);
 							placeWater(world, i + l2, j - 1, k + j3, l, 1);
 						} else {
 							world.setBlockAndMetadataWithNotify(i, j, k,
 									blockID, 0);
 							placeWater(world, i + l2, j - 1, k + j3, l - 1, 1);
 							l = 0;
 						}
 						flag = true;
 						i2 = 5;
 						j2 = 5;
 					}
 					if (!((!flag) & isWater(world, i + l2, j - 1, k + j3))) {
 						continue;
 					}
 					new Random();
 					if (blockMaterial == Material.lava
 							&& (world.getBlockId(i + l2, j - 1, k + j3) == mod_NWater.nwater_ocean.blockID
 									|| world.getBlockId(i + l2, j - 1, k + j3) == mod_NWater.nwater.blockID || world
 									.getBlockId(i + l2, j - 1, k + j3) == mod_NWater.nwater_still.blockID)) {
 						int i5 = world.getBlockMetadata(i, j, k);
 						world.setBlock(i, j, k, 0);
 						if (i5 > 5) {
 							world.setBlockWithNotify(i + l2, j - 1, k + j3,
 									Block.cobblestone.blockID);
 						}
 						flag = true;
 						i2 = 5;
 						j2 = 5;
 					}
 					if (blockMaterial == Material.water
 							&& (world.getBlockId(i + l2, j - 1, k + j3) == mod_NWater.nlava.blockID || world
 									.getBlockId(i + l2, j - 1, k + j3) == mod_NWater.nlava_still.blockID)) {
 						int j5 = world.getBlockMetadata(i, j, k);
 						world.setBlock(i, j, k, 0);
 						hardenFX(world, i + l2, j - 1, k + j3);
 						if (j5 > 5) {
 							world.setBlockWithNotify(i + l2, j - 1, k + j3,
 									Block.obsidian.blockID);
 						}
 						flag = true;
 						i2 = 5;
 						j2 = 5;
 					}
 					if (flag) {
 						continue;
 					}
 					int k5 = world.getBlockMetadata(i + l2, j - 1, k + j3);
 					if (k5 >= 15) {
 						continue;
 					}
 					if (k5 + (1 + l) < 16) {
 						grabWater(world, i, j, k, i, j, k, 0, -1);
 						placeWater(world, i + l2, j - 1, k + j3, 1 + l + k5, 1);
 						flag = true;
 						i2 = 5;
 						j2 = 5;
 					} else {
 						int i6 = (l + k5) - 15;
 						placeWater(world, i, j, k, i6, 1);
 						placeWater(world, i + l2, j - 1, k + j3, 15, 1);
 						flag = true;
 						i2 = 5;
 						j2 = 5;
 					}
 				}
 
 			}
 
 		}
 		int k1 = getMetathresh();
 		if ((!flag) & (l > k1)) {
 			byte byte2 = 1;
 			byte byte3 = 1;
 			if (random.nextInt(2) == 0) {
 				byte2 = -1;
 			}
 			if (random.nextInt(2) == 0) {
 				byte3 = -1;
 			}
 			for (int k2 = -1; k2 <= 1; k2++) {
 				for (int i3 = -1; i3 <= 1; i3++) {
 					int k3 = k2 * byte2;
 					int i4 = i3 * byte3;
 					if (!((!flag) & (k2 != 0 || i3 != 0) & (k2 == 0 || i3 == 0))) {
 						continue;
 					}
 					world.getBlockId(i + k3, j, k + i4);
 					int l4 = world.getBlockMetadata(i + k3, j, k + i4);
 					boolean flag2 = mod_NWater.canMoveAmt(world, i + k3, j, k
 							+ i4, l);
 					if ((!flag) & flag2) {
 						l4 = l / 2;
 						l -= l4;
 						placeWater(world, i + k3, j, k + i4, l - 1, 1);
 						placeWater(world, i, j, k, l4, 1);
 						flag = true;
 						k2 = 5;
 						i3 = 5;
 					}
 					if (!((!flag) & isWater(world, i + k3, j, k + i4))) {
 						continue;
 					}
 					Random random2 = new Random();
 					if (blockMaterial == Material.lava
 							&& (world.getBlockId(i + k3, j, k + i4) == mod_NWater.nwater_ocean.blockID
 									|| world.getBlockId(i + k3, j, k + i4) == mod_NWater.nwater.blockID || world
 									.getBlockId(i + k3, j, k + i4) == mod_NWater.nwater_still.blockID)) {
 						if (random2.nextInt(4) == 0) {
 							world.setBlock(i, j, k, Block.cobblestone.blockID);
 							hardenFX(world, i, j, k);
 							world.setBlockWithNotify(i + k3, j, k + i4, 0);
 						} else {
 							world.setBlock(i, j, k, 0);
 							hardenFX(world, i + k3, j, k + i4);
 							world.setBlockWithNotify(i + k3, j, k + i4,
 									Block.cobblestone.blockID);
 						}
 						flag = true;
 						k2 = 5;
 						i3 = 5;
 					}
 					if (blockMaterial == Material.water
 							&& (world.getBlockId(i + k3, j, k + i4) == mod_NWater.nlava.blockID || world
 									.getBlockId(i + k3, j, k + i4) == mod_NWater.nlava_still.blockID)) {
 						if (random2.nextInt(4) == 0) {
 							world.setBlock(i, j, k, Block.obsidian.blockID);
 							world.setBlockWithNotify(i + k3, j, k + i4, 0);
 						} else {
 							world.setBlock(i, j, k, 0);
 							world.setBlockWithNotify(i + k3, j, k + i4,
 									Block.obsidian.blockID);
 						}
 						flag = true;
 						k2 = 5;
 						i3 = 5;
 					}
 					if ((!flag) & (l4 + 2 < l)) {
 						int j6 = (l + l4 + 1) / 2;
 						int k6 = (l + l4) / 2;
 						placeWater(world, i + k3, j, k + i4, j6, 1);
 						placeWater(world, i, j, k, k6, 1);
 						flag = true;
 						k2 = 5;
 						i3 = 5;
 					}
 					float f = getSurfaceMull();
 					if (!((!flag) & (Math
 							.pow(mod_NWater.getMetaAvg2(world, i + k3, j, k
 									+ i4), f) < (double) mod_NWater
 							.getMetaAvg2(world, i, j, k)))) {
 						continue;
 					}
 					int l6 = l;
 					int i7 = (l + l4 + 1) / 2;
 					int j7 = (l + l4) / 2;
 					if (l - l4 > 1) {
 						i7 += (l - l4) / 4;
 						j7 -= (l - l4) / 4;
 					}
 					if (l6 != j7) {
 						placeWater(world, i + k3, j, k + i4, i7, 1);
 						placeWater(world, i, j, k, j7, 1);
 						flag = true;
 						k2 = 5;
 						i3 = 5;
 					}
 				}
 
 			}
 
 		}
 		if (borderOcean(world, i, j, k)) {
 			l = (int) ((float) l * 1.25F) + 2;
 			if (l > 15) {
 				l = 15;
 			}
 			world.setBlockMetadataWithNotify(i, j, k, l);
 			if (l > 13) {
 				world.setBlockAndMetadataWithNotify(i, j, k,
 						mod_NWater.nwater_ocean.blockID, 0);
 				return;
 			} else {
 				world.scheduleBlockUpdate(i, j, k, blockID, tickRate());
 				return;
 			}
 		}
 		if (!flag) {
 			world.setBlockAndMetadata(i, j, k, getStill(), l);
 			setToStill(world, i, j, k);
 		} else {
 			world.scheduleBlockUpdate(i, j, k, blockID, tickRate());
 		}
 	}
 
 	public void setToStill(World world, int i, int j, int k) {
 	}
 
 	public int getStill() {
 		return mod_NWater.nwater_still.blockID;
 	}
 
 	public AxisAlignedBB getCollisionBoundingBoxFromPool(World world, int i,
 			int j, int k) {
 		if (world.multiplayerWorld) {
 			return null;
 		}
 		if (world.getBlockId(i, j + 1, k) != 0) {
 			setBlockBounds(-0.01F, 0.01F, 0.01F, 1.01F, 1.01F, 1.01F);
 		}
 		return null;
 	}
 
 	public void burnArea(World world, int i, int j, int k) {
 		int l = i + 1;
 		int i1 = j;
 		int j1 = k;
 		if (world.getBlockMaterial(l, i1, j1).getBurning()) {
 			world.setBlockWithNotify(l, i1, j1, Block.fire.blockID);
 		}
 		l = i - 1;
 		i1 = j;
 		j1 = k;
 		if (world.getBlockMaterial(l, i1, j1).getBurning()) {
 			world.setBlockWithNotify(l, i1, j1, Block.fire.blockID);
 		}
 		l = i;
 		i1 = j;
 		j1 = k + 1;
 		if (world.getBlockMaterial(l, i1, j1).getBurning()) {
 			world.setBlockWithNotify(l, i1, j1, Block.fire.blockID);
 		}
 		l = i;
 		i1 = j;
 		j1 = k - 1;
 		if (world.getBlockMaterial(l, i1, j1).getBurning()) {
 			world.setBlockWithNotify(l, i1, j1, Block.fire.blockID);
 		}
 		l = i;
 		i1 = j + 1;
 		j1 = k;
 		if (world.getBlockMaterial(l, i1, j1).getBurning()) {
 			world.setBlockWithNotify(l, i1, j1, Block.fire.blockID);
 		}
 		l = i;
 		i1 = j - 1;
 		j1 = k;
 		if (world.getBlockMaterial(l, i1, j1).getBurning()) {
 			world.setBlockWithNotify(l, i1, j1, Block.fire.blockID);
 		}
 	}
 
 	public boolean borderOcean(World world, int i, int j, int k) {
 		if (world.getBlockId(i + 1, j, k) == mod_NWater.nwater_ocean.blockID) {
 			return true;
 		}
 		if (world.getBlockId(i - 1, j, k) == mod_NWater.nwater_ocean.blockID) {
 			return true;
 		}
 		if (world.getBlockId(i, j, k + 1) == mod_NWater.nwater_ocean.blockID) {
 			return true;
 		}
 		if (world.getBlockId(i, j, k - 1) == mod_NWater.nwater_ocean.blockID) {
 			return true;
 		}
 		return world.getBlockId(i, j + 1, k) == mod_NWater.nwater_ocean.blockID;
 	}
 
 	protected void hardenFX(World world, int i, int j, int k) {
 		world.playSoundEffect((float) i + 0.5F, (float) j + 0.5F,
 				(float) k + 0.5F, "random.fizz", 0.5F,
 				2.6F + (world.rand.nextFloat() - world.rand.nextFloat()) * 0.8F);
 		for (int l = 0; l < 8; l++) {
 			world.spawnParticle("largesmoke", (double) i + Math.random(),
 					(double) j + 1.2D, (double) k + Math.random(), 0.0D, 0.0D,
 					0.0D);
 		}
 
 	}
 
 	public void randomDisplayTick(World world, int i, int j, int k,
 			Random random) {
 		if (random.nextInt(4) == 1 && mod_NWater.isBoiling(world, i, j, k)) {
 			world.spawnParticle("bubble", (float) i + random.nextFloat(),
 					(float) j + random.nextFloat(),
 					(float) k + random.nextFloat(),
 					(random.nextFloat() - 0.5F) / 10F, random.nextFloat() / 5F,
 					(random.nextFloat() - 0.5F) / 10F);
 		}
 	}
 
 	public void onEntityCollidedWithBlock(World world, int i, int j, int k,
 			Entity entity) {
 		if (world.multiplayerWorld) {
 			return;
 		} else {
 			mod_NWater.boilWater(world, i, j, k, entity);
 			return;
 		}
 	}
 
 	public void onBlockRemoval(World world, int i, int j, int k) {
 		int l = world.getBlockMetadata(i, j, k);
 		if (l > 0) {
 			world.notifyBlocksOfNeighborChange(i, j, k, blockID);
 			world.notifyBlocksOfNeighborChange(i, j - 1, k, blockID);
 		}
 		super.onBlockRemoval(world, i, j, k);
 	}
 
 	public void setBlockBoundsBasedOnState(IBlockAccess iblockaccess, int i,
 			int j, int k) {
 		setBlockBounds(0.0F, 0.0F, 0.0F, 1.0F,
 				0.01F + (float) iblockaccess.getBlockMetadata(i, j, k) / 16F,
 				1.0F);
 	}
 
 	public boolean isPoweringTo(IBlockAccess iblockaccess, int i, int j, int k,
 			int l) {
 		return false;
 	}
 
 	public boolean isIndirectlyPoweringTo(World world, int i, int j, int k,
 			int l) {
 		return false;
 	}
 
 	public boolean canProvidePower() {
 		return false;
 	}
 
 	public float getBlockBrightness(IBlockAccess iblockaccess, int i, int j,
 			int k) {
 		float f = iblockaccess.getLightBrightness(i, j, k);
 		float f1 = iblockaccess.getLightBrightness(i, j + 1, k);
 		return f > f1 ? f : f1;
 	}
 
 	public int getRenderBlockPass() {
 		return 1;
 	}
 
 	public void setBlockBoundsForItemRender() {
 		float f = 0.5F;
 		float f1 = 0.125F;
 		float f2 = 0.5F;
 		setBlockBounds(0.5F - f, 0.5F - f1, 0.5F - f2, 0.5F + f, 0.5F + f1,
 				0.5F + f2);
 	}
 
 	public boolean canDrawWater(IBlockAccess myBlockAccess, int i, int j, int k) {
 		if (myBlockAccess.isBlockOpaqueCube(i, j, k)) {
 			return false;
 		}
 		return !isWater(myBlockAccess, i, j, k);
 	}
 
 	public boolean canDrawWater2(IBlockAccess myBlockAccess, int i, int j, int k) {
 		return !isWater(myBlockAccess, i, j, k);
 	}
 
 	public boolean isWater(IBlockAccess myBlockAccess, int i, int j, int k) {
 		if (myBlockAccess.getBlockId(i, j, k) == mod_NWater.nwater.blockID) {
 			return true;
 		}
 		if (myBlockAccess.getBlockId(i, j, k) == mod_NWater.nwater_still.blockID) {
 			return true;
 		}
 		if (myBlockAccess.getBlockId(i, j, k) == mod_NWater.nlava.blockID) {
 			return true;
 		}
 		if (myBlockAccess.getBlockId(i, j, k) == mod_NWater.nlava_still.blockID) {
 			return true;
 		}
 		if (myBlockAccess.getBlockId(i, j, k) == mod_NWater.noil.blockID) {
 			return true;
 		}
 		if (myBlockAccess.getBlockId(i, j, k) == mod_NWater.noil_still.blockID) {
 			return true;
 		}
 		if (myBlockAccess.getBlockId(i, j, k) == mod_NWater.nqsand.blockID) {
 			return true;
 		}
 		if (myBlockAccess.getBlockId(i, j, k) == mod_NWater.nqsand_still.blockID) {
 			return true;
 		}
 		return myBlockAccess.getBlockId(i, j, k) == mod_NWater.nwater_ocean.blockID;
 	}
 
 	public static boolean isWater(IBlockAccess myBlockAccess, World world,
 			int i, int j, int k) {
 		if (world.getBlockId(i, j, k) == mod_NWater.nwater.blockID) {
 			return true;
 		}
 		if (world.getBlockId(i, j, k) == mod_NWater.nwater_still.blockID) {
 			return true;
 		}
 		if (world.getBlockId(i, j, k) == mod_NWater.nlava.blockID) {
 			return true;
 		}
 		if (world.getBlockId(i, j, k) == mod_NWater.nlava_still.blockID) {
 			return true;
 		}
 		if (world.getBlockId(i, j, k) == mod_NWater.noil.blockID) {
 			return true;
 		}
 		if (world.getBlockId(i, j, k) == mod_NWater.noil_still.blockID) {
 			return true;
 		}
 		if (world.getBlockId(i, j, k) == mod_NWater.nqsand.blockID) {
 			return true;
 		}
 		if (world.getBlockId(i, j, k) == mod_NWater.nqsand_still.blockID) {
 			return true;
 		}
 		return world.getBlockId(i, j, k) == mod_NWater.nwater_ocean.blockID;
 	}
 
 	public boolean isFWater(IBlockAccess myBlockAccess, int i, int j, int k) {
 		if (myBlockAccess.getBlockId(i, j, k) == mod_NWater.nwater.blockID) {
 			return true;
 		}
 		if (myBlockAccess.getBlockId(i, j, k) == mod_NWater.nlava.blockID) {
 			return true;
 		}
 		if (myBlockAccess.getBlockId(i, j, k) == mod_NWater.noil.blockID) {
 			return true;
 		}
 		return myBlockAccess.getBlockId(i, j, k) == mod_NWater.nqsand.blockID;
 	}
 
 	public float getHeight(IBlockAccess myBlockAccess, int i, int j, int k,
 			int l, int i1) {
 		if (myBlockAccess.getBlockId(i, j, k) == mod_NWater.nwater_ocean.blockID) {
 			return 1.0F;
 		}
 		float f = myBlockAccess.getBlockMetadata(i, j, k) + 1;
 		myBlockAccess.getBlockMetadata(i, j, k);
 		if (isWater(myBlockAccess, i, j + 1, k)) {
 			return 1.0F;
 		}
 		if ((myBlockAccess.getBlockId(i + 1, j, k) == 0)
 				& (myBlockAccess.getBlockId(i - 1, j, k) == 0)
 				& (myBlockAccess.getBlockId(i, j, k + 1) == 0)
 				& (myBlockAccess.getBlockId(i, j, k - 1) == 0)) {
 		}
 		if ((l == 1) & (i1 == 1)) {
 			int j1 = 1;
 			if (myBlockAccess.getBlockId(i + 1, j, k) == mod_NWater.nwater_ocean.blockID) {
 				return 1.0F;
 			}
 			if (isWater(myBlockAccess, i + 1, j, k)) {
 				j1++;
 				f = f + (float) myBlockAccess.getBlockMetadata(i + 1, j, k)
 						+ 1.0F;
 			}
 			if (isFWater(myBlockAccess, i + 1, j + 1, k)) {
 				return 1.0F;
 			}
 			if (isLDWater(myBlockAccess, i + 1, j, k)) {
 				return 0.0F;
 			}
 			if (myBlockAccess.getBlockId(i + 1, j, k + 1) == mod_NWater.nwater_ocean.blockID) {
 				return 1.0F;
 			}
 			if (isWater(myBlockAccess, i + 1, j, k + 1)) {
 				j1++;
 				f = f + (float) myBlockAccess.getBlockMetadata(i + 1, j, k + 1)
 						+ 1.0F;
 			}
 			if (isFWater(myBlockAccess, i + 1, j + 1, k + 1)) {
 				return 1.0F;
 			}
 			if (myBlockAccess.getBlockId(i, j, k + 1) == mod_NWater.nwater_ocean.blockID) {
 				return 1.0F;
 			}
 			if (isWater(myBlockAccess, i, j, k + 1)) {
 				j1++;
 				f = f + (float) myBlockAccess.getBlockMetadata(i, j, k + 1)
 						+ 1.0F;
 			}
 			if (isFWater(myBlockAccess, i, j + 1, k + 1)) {
 				return 1.0F;
 			}
 			if (isLDWater(myBlockAccess, i, j, k + 1)) {
 				return 0.0F;
 			} else {
 				return f / 16F / (float) j1;
 			}
 		}
 		if ((l == -1) & (i1 == -1)) {
 			int k1 = 1;
 			if (myBlockAccess.getBlockId(i - 1, j, k) == mod_NWater.nwater_ocean.blockID) {
 				return 1.0F;
 			}
 			if (isWater(myBlockAccess, i - 1, j, k)) {
 				k1++;
 				f = f + (float) myBlockAccess.getBlockMetadata(i - 1, j, k)
 						+ 1.0F;
 			}
 			if (isFWater(myBlockAccess, i - 1, j + 1, k)) {
 				return 1.0F;
 			}
 			if (isLDWater(myBlockAccess, i - 1, j, k)) {
 				return 0.0F;
 			}
 			if (myBlockAccess.getBlockId(i - 1, j, k - 1) == mod_NWater.nwater_ocean.blockID) {
 				return 1.0F;
 			}
 			if (isWater(myBlockAccess, i - 1, j, k - 1)) {
 				k1++;
 				f = f + (float) myBlockAccess.getBlockMetadata(i - 1, j, k - 1)
 						+ 1.0F;
 			}
 			if (isFWater(myBlockAccess, i - 1, j + 1, k - 1)) {
 				return 1.0F;
 			}
 			if (myBlockAccess.getBlockId(i, j, k - 1) == mod_NWater.nwater_ocean.blockID) {
 				return 1.0F;
 			}
 			if (isWater(myBlockAccess, i, j, k - 1)) {
 				k1++;
 				f = f + (float) myBlockAccess.getBlockMetadata(i, j, k - 1)
 						+ 1.0F;
 			}
 			if (isFWater(myBlockAccess, i, j + 1, k - 1)) {
 				return 1.0F;
 			}
 			if (isLDWater(myBlockAccess, i, j, k - 1)) {
 				return 0.0F;
 			} else {
 				return f / 16F / (float) k1;
 			}
 		}
 		if ((l == 1) & (i1 == -1)) {
 			int l1 = 1;
 			if (myBlockAccess.getBlockId(i + 1, j, k) == mod_NWater.nwater_ocean.blockID) {
 				return 1.0F;
 			}
 			if (isWater(myBlockAccess, i + 1, j, k)) {
 				l1++;
 				f = f + (float) myBlockAccess.getBlockMetadata(i + 1, j, k)
 						+ 1.0F;
 			}
 			if (isFWater(myBlockAccess, i + 1, j + 1, k)) {
 				return 1.0F;
 			}
 			if (isLDWater(myBlockAccess, i + 1, j, k)) {
 				return 0.0F;
 			}
 			if (myBlockAccess.getBlockId(i + 1, j, k - 1) == mod_NWater.nwater_ocean.blockID) {
 				return 1.0F;
 			}
 			if (isWater(myBlockAccess, i + 1, j, k - 1)) {
 				l1++;
 				f = f + (float) myBlockAccess.getBlockMetadata(i + 1, j, k - 1)
 						+ 1.0F;
 			}
 			if (isFWater(myBlockAccess, i + 1, j + 1, k - 1)) {
 				return 1.0F;
 			}
 			if (myBlockAccess.getBlockId(i, j, k - 1) == mod_NWater.nwater_ocean.blockID) {
 				return 1.0F;
 			}
 			if (isWater(myBlockAccess, i, j, k - 1)) {
 				l1++;
 				f = f + (float) myBlockAccess.getBlockMetadata(i, j, k - 1)
 						+ 1.0F;
 			}
 			if (isFWater(myBlockAccess, i, j + 1, k - 1)) {
 				return 1.0F;
 			}
 			if (isLDWater(myBlockAccess, i, j, k - 1)) {
 				return 0.0F;
 			} else {
 				return f / 16F / (float) l1;
 			}
 		}
 		if ((l == -1) & (i1 == 1)) {
 			int i2 = 1;
 			if (myBlockAccess.getBlockId(i - 1, j, k) == mod_NWater.nwater_ocean.blockID) {
 				return 1.0F;
 			}
 			if (isWater(myBlockAccess, i - 1, j, k)) {
 				i2++;
 				f = f + (float) myBlockAccess.getBlockMetadata(i - 1, j, k)
 						+ 1.0F;
 			}
 			if (isFWater(myBlockAccess, i - 1, j + 1, k)) {
 				return 1.0F;
 			}
 			if (isLDWater(myBlockAccess, i - 1, j, k)) {
 				return 0.0F;
 			}
 			if (myBlockAccess.getBlockId(i - 1, j, k + 1) == mod_NWater.nwater_ocean.blockID) {
 				return 1.0F;
 			}
 			if (isWater(myBlockAccess, i - 1, j, k + 1)) {
 				i2++;
 				f = f + (float) myBlockAccess.getBlockMetadata(i - 1, j, k + 1)
 						+ 1.0F;
 			}
 			if (isFWater(myBlockAccess, i - 1, j + 1, k + 1)) {
 				return 1.0F;
 			}
 			if (myBlockAccess.getBlockId(i, j, k + 1) == mod_NWater.nwater_ocean.blockID) {
 				return 1.0F;
 			}
 			if (isWater(myBlockAccess, i, j, k + 1)) {
 				i2++;
 				f = f + (float) myBlockAccess.getBlockMetadata(i, j, k + 1)
 						+ 1.0F;
 			}
 			if (isFWater(myBlockAccess, i, j + 1, k + 1)) {
 				return 1.0F;
 			}
 			if (isLDWater(myBlockAccess, i, j, k + 1)) {
 				return 0.0F;
 			} else {
 				return f / 16F / (float) i2;
 			}
 		} else {
 			return f / 16F;
 		}
 	}
 
 	@Override
 	protected void renderBlock(IBlockAccess myBlockAccess, int i, int j, int k) {
 		Tessellator tessellator = Tessellator.instance;
 		int l = getBlockTextureFromSide(0);
 		float f = getBlockBrightness(myBlockAccess, i, j, k);
 		tessellator.setColorOpaque_F(f, f, f);
 		int i1 = (l & 0xf) << 4;
 		int j1 = l & 0xf0;
 		double d = (float) i1 / 256F;
 		double d1 = ((float) i1 + 15.99F) / 256F;
 		double d2 = (float) j1 / 256F;
 		double d3 = ((float) j1 + 15.99F) / 256F;
 		int k1 = myBlockAccess.getBlockMetadata(i, j, k);
 		if (myBlockAccess.getBlockId(i, j, k) == mod_NWater.nwater_ocean.blockID) {
 			k1 = 15;
 		}
 		float f2 = 0.0F;
 		if (canDrawWater2(myBlockAccess, i, j + 1, k)) {
 			tessellator.addVertexWithUV((float) i + 1.0F, (float) j
 					+ getHeight(myBlockAccess, i, j, k, 1, 1), (float) (k + 1),
 					d, d2);
 			tessellator.addVertexWithUV((float) i + 1.0F, (float) j
 					+ getHeight(myBlockAccess, i, j, k, 1, -1),
 					(float) (k + 0), d, d3);
 			tessellator.addVertexWithUV((float) i + 0.0F, (float) j
 					+ getHeight(myBlockAccess, i, j, k, -1, -1),
 					(float) (k + 0), d1, d3);
 			tessellator.addVertexWithUV((float) i + 0.0F, (float) j
 					+ getHeight(myBlockAccess, i, j, k, -1, 1),
 					(float) (k + 1), d1, d2);
 		}
 		if (canDrawWater(myBlockAccess, i, j - 1, k)) {
 			tessellator.addVertexWithUV((float) i + 1.0F, (float) j,
 					(float) (k + 1), d, d2);
 			tessellator.addVertexWithUV((float) i + 1.0F, (float) j,
 					(float) (k + 0), d, d3);
 			tessellator.addVertexWithUV((float) i + 0.0F, (float) j,
 					(float) (k + 0), d1, d3);
 			tessellator.addVertexWithUV((float) i + 0.0F, (float) j,
 					(float) (k + 1), d1, d2);
 		}
 		if (canDrawWater(myBlockAccess, i - 1, j, k)) {
 			tessellator.addVertexWithUV((float) i,
 					(float) j + getHeight(myBlockAccess, i, j, k, -1, 1) + f2,
 					(float) (k + 1), d, d2);
 			tessellator.addVertexWithUV((float) i, (float) (j + 0) - f2,
 					(float) (k + 1), d, d3);
 			tessellator.addVertexWithUV((float) i, (float) (j + 0) - f2,
 					(float) (k + 0), d1, d3);
 			tessellator.addVertexWithUV((float) i,
 					(float) j + getHeight(myBlockAccess, i, j, k, -1, -1) + f2,
 					(float) (k + 0), d1, d2);
 		}
 		if (canDrawWater(myBlockAccess, i + 1, j, k)) {
 			tessellator.addVertexWithUV((float) i + 1.0F, (float) j
 					+ getHeight(myBlockAccess, i, j, k, 1, 1) + f2,
 					(float) (k + 1), d, d2);
 			tessellator.addVertexWithUV((float) i + 1.0F, (float) (j + 0) - f2,
 					(float) (k + 1), d, d3);
 			tessellator.addVertexWithUV((float) i + 1.0F, (float) (j + 0) - f2,
 					(float) (k + 0), d1, d3);
 			tessellator.addVertexWithUV((float) i + 1.0F, (float) j
 					+ getHeight(myBlockAccess, i, j, k, 1, -1) + f2,
 					(float) (k + 0), d1, d2);
 		}
 		if (canDrawWater(myBlockAccess, i, j, k - 1)) {
 			tessellator.addVertexWithUV((float) i + 1.0F, (float) j
 					+ getHeight(myBlockAccess, i, j, k, 1, -1) + f2,
 					(float) (k + 0), d, d2);
 			tessellator.addVertexWithUV((float) i + 1.0F, (float) (j + 0) - f2,
 					(float) (k + 0), d, d3);
 			tessellator.addVertexWithUV((float) i, (float) (j + 0) - f2,
 					(float) (k + 0), d1, d3);
 			tessellator.addVertexWithUV((float) i,
 					(float) j + getHeight(myBlockAccess, i, j, k, -1, -1) + f2,
 					(float) (k + 0), d1, d2);
 		}
 		if (canDrawWater(myBlockAccess, i, j, k + 1)) {
 			tessellator.addVertexWithUV((float) i + 1.0F, (float) j
 					+ getHeight(myBlockAccess, i, j, k, 1, 1) + f2,
 					(float) (k + 1), d, d2);
 			tessellator.addVertexWithUV((float) i + 1.0F, (float) (j + 0) - f2,
 					(float) (k + 1), d, d3);
 			tessellator.addVertexWithUV((float) i, (float) (j + 0) - f2,
 					(float) (k + 1), d1, d3);
 			tessellator.addVertexWithUV((float) i,
 					(float) j + getHeight(myBlockAccess, i, j, k, -1, 1) + f2,
 					(float) (k + 1), d1, d2);
 		}
 	}
 
 	public boolean isLDWater(IBlockAccess myBlockAccess, int i, int j, int k) {
 		return (myBlockAccess.getBlockId(i, j, k) == 0)
 				& isWater(myBlockAccess, i, j - 1, k)
 				&& (!isWater(myBlockAccess, i - 1, j, k)
 						&& !isWater(myBlockAccess, i + 1, j, k)
 						&& !isWater(myBlockAccess, i, j, k + 1) && !isWater(
 						myBlockAccess, i, j, k - 1));
 	}
 
 	@Override
 	public void renderEntity(int arg0) {
 		// TODO Auto-generated method stub
 
 	}
 }
