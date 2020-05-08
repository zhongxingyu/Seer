 package toldea.romecraft.tileentity;
 
 import net.minecraft.tileentity.TileEntity;
 import toldea.romecraft.block.BlockHelper;
 import toldea.romecraft.managers.PacketManager;
 
 public class TileEntityBellows extends TileEntity {
 	public static final float MAX_ROTATION = (float) (30f * Math.PI / 180f);
 	public static final float ROTATION_SPEED = .02f;
 	public static final int ROTATION_TIME = (int) (MAX_ROTATION / ROTATION_SPEED * 2);
 
 	private float bellowsRotation = 30f;
 	private boolean contracting = false;
 	private boolean recentlyPushed = false;
 	private TileEntityBloomery facingBloomery = null;
 
 	@Override
 	public void updateEntity() {
 		if (worldObj.isRemote) {
 			if (contracting) {
 				bellowsRotation = Math.max(0f, bellowsRotation - ROTATION_SPEED);
 				if (bellowsRotation <= 0f) {
 					contracting = false;
 				}
 			} else if (bellowsRotation != MAX_ROTATION) {
 				bellowsRotation = Math.min(MAX_ROTATION, bellowsRotation + ROTATION_SPEED);
 				if (bellowsRotation >= MAX_ROTATION) {
 					recentlyPushed = false;
 				}
 			}
 		}
 	}
 
 	public boolean pushBellows() {
 		if (worldObj.isRemote) {
 			if (!recentlyPushed) {
 				contracting = true;
 				recentlyPushed = true;
 
 				// Check if there is a Bloomery in front of us and if so, notify it a bellows was activated.
 				TileEntityBloomery bloomery = getFacingBloomery();
 				if (bloomery != null && bloomery.getIsValid() && bloomery.getIsMaster()) {
 					bloomery.applyBellowsBoost(this.worldObj);
 				}
 
 				return true;
 			}
 		} else {
 			PacketManager.sendPushBellowsPacketToAllPlayers(this);
 			return true;
 		}
 		return false;
 	}
 
 	public void setFacingBloomery(TileEntityBloomery bloomery) {
 		this.facingBloomery = bloomery;
 	}
 
 	public TileEntityBloomery getFacingBloomery() {
		if (facingBloomery == null || !facingBloomery.getIsValid() || !facingBloomery.getIsMaster()) {
 			TileEntity tileEntity = TileEntityHelper.getNeighbouringTileEntityForDirection(getBlockMetadata() & BlockHelper.MASK_DIR, this.worldObj, xCoord,
 					yCoord, zCoord);
 			if (tileEntity != null && tileEntity instanceof TileEntityBloomery) {
 				TileEntityBloomery bloomery = (TileEntityBloomery) tileEntity;
 				if (bloomery.getIsValid() && bloomery.getIsMaster()) {
 					setFacingBloomery(bloomery);
 				}
 			}
 		}
 		return this.facingBloomery;
 	}
 
 	public TileEntityBloomery faceAdjacentBloomery() {
 		// Look for any adjacent bloomery 'master' tile entities and face one if found.
 		for (int i = 0; i < 4; i++) {
 			TileEntity te = TileEntityHelper.getNeighbouringTileEntityForDirection(i, this.worldObj, xCoord, yCoord, zCoord);
 			if (te != null && te instanceof TileEntityBloomery) {
 				TileEntityBloomery bloomery = (TileEntityBloomery) te;
 				if (bloomery.getIsValid() && bloomery.getIsMaster()) {
 					this.blockMetadata = BlockHelper.getOppositeDirectionByteForInt(i);
 					this.worldObj.setBlockMetadataWithNotify(xCoord, yCoord, zCoord, this.blockMetadata, 2);
 					setFacingBloomery(bloomery);
 					break;
 				}
 			}
 		}
 		return getFacingBloomery();
 	}
 
 	public float getBellowsRotation() {
 		return bellowsRotation;
 	}
 }
