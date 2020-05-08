 package com.isocraft.tileentity.eridiumnet;
 
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraftforge.common.util.ForgeDirection;
 
 import com.isocraft.api.eridiumnet.EridiumNetwork;
 import com.isocraft.api.eridiumnet.IEridiumNetConnectionNode;
 import com.isocraft.api.eridiumnet.IEridiumNetMajorNode;
 import com.isocraft.core.helpers.LogHelper;
 import com.isocraft.entity.EntityEridiumFluxShell;
 
 public class TileEridiumNetHub extends TileEridiumMajorNode implements IEridiumNetConnectionNode {
 
 	private EridiumNetwork net;
 	private int ticker = 0;
 	
 	public EntityEridiumFluxShell entity;
 	private int entityLength = 0;
 	
 	private IEridiumNetMajorNode connectedTile;
 
 	public TileEridiumNetHub() {
 		super();
 		this.net = new EridiumNetwork();
 	}
 
 	public EridiumNetwork getNetwork() {
 		return this.net;
 	}
 
 	@Override
 	public void updateEntity() {
 		super.updateEntity();
 		
 		if (this.ticker == 0){
 			this.checkConnections();
 		}
 		else if (this.ticker == 10){
 			this.ticker = 0;
 		}
 		else {
 			++ ticker;
 		}
 			
 		if (!this.worldObj.isRemote) {		
 			this.net.tickNetwork();
 		}
 	}
 	
 	@Override
 	public void readFromNBT(NBTTagCompound nbt) {
 		super.readFromNBT(nbt);
 		this.net = this.net.readNetwork(nbt);
 	}
 
 	@Override
 	public void writeToNBT(NBTTagCompound nbt) {
 		super.writeToNBT(nbt);
 		this.net.saveNetwork(nbt);
 	}
 
 	//Node
 	@Override
 	public TileEridiumNetHub getHub() {
 		return this;
 	}
 
 	@Override
 	public void setHub(TileEridiumNetHub tile) {
 	}
 	
 	@Override
     public void updateSide() {
 		super.updateSide();
 		if (this.entity != null) {
 			this.worldObj.removeEntity(this.entity);
 			this.entity = null;
			this.entityLength = 0;
 		}
 		if (this.connectedSide != null){
 			this.entity = new EntityEridiumFluxShell(this.worldObj, this.xCoord, this.yCoord, this.zCoord, this.connectedSide, this);
 			this.worldObj.spawnEntityInWorld(this.entity);
 		}
 	}
 	
 	@Override
 	public void checkConnections() {
 		IEridiumNetMajorNode tile = EridiumNetwork.traceConnection(this.worldObj, this, this.xCoord, this.yCoord, this.zCoord, this.connectedSide);
 		if (tile != null){
 			this.connectedTile = tile;
 		}
 	}
 
 	@Override
     public void setConnectionsEntityLength(int l) {
 		if (l != this.entityLength){
 			this.entityLength = l;
 			this.entity.newLength(l);
 		}    
     }
 	
 	@Override
     public void setOverrideConnectionEntityLength(int l) {
 		if (this.entityLength > l){
 			this.entity.overrideLength(l);
 		}
     }
 	
 	@Override
     public IEridiumNetMajorNode[] getConnections() {
 		IEridiumNetMajorNode list[] = {this.connectedTile};
 	    return list;
     }
 
 	@Override
     public void connectionMade(ForgeDirection to) {
 		LogHelper.info("connection Made");
 	}
 }
