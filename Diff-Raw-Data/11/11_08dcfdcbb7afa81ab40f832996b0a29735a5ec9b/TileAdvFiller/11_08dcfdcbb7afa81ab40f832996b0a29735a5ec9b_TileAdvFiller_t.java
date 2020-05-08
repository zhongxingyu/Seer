 package mods.firstspring.advfiller;
 
 import java.io.ByteArrayOutputStream;
 import java.io.DataOutputStream;
 import java.util.ArrayList;
 import java.util.HashSet;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Set;
 
 import mods.firstspring.advfiller.lib.BlockLib;
 import mods.firstspring.advfiller.lib.BuildCraftProxy;
 import net.minecraft.block.Block;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.inventory.IInventory;
 import net.minecraft.item.ItemStack;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.network.packet.Packet;
 import net.minecraft.network.packet.Packet250CustomPayload;
 import net.minecraft.network.packet.Packet3Chat;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.world.ChunkCoordIntPair;
 import net.minecraftforge.common.FakePlayerFactory;
 import net.minecraftforge.common.ForgeChunkManager;
 import net.minecraftforge.common.ForgeChunkManager.Ticket;
 import net.minecraftforge.common.ForgeDirection;
 import buildcraft.api.core.IAreaProvider;
 import buildcraft.api.power.IPowerProvider;
 import buildcraft.api.power.IPowerReceptor;
 import buildcraft.api.power.PowerFramework;
 
 import com.google.common.collect.Sets;
 
 import cpw.mods.fml.common.network.PacketDispatcher;
 import cpw.mods.fml.common.network.Player;
 
 public class TileAdvFiller extends TileEntity implements IPowerReceptor {
 	Thread initializeThread;
 	IPowerProvider powerProvider;
 	public int dim;
 	boolean bcLoaded = BuildCraftProxy.loaded;
 	Ticket chunkTicket;
 	//used on chunkloading message
 	EntityPlayer player;
 	boolean doRender = false;
 	int left,right,up,down,forward;
 	int type;//0 : Quarry Mode 1 : Remove Mode 2 : Filling Mode 3 : Flatten Mode 4 : Exclusive Remove Mode 5 : TofuBuild Mode
 	int fromX,fromY,fromZ,toX,toY,toZ;
 	int tick = 0;
 	int rate;
 	boolean initialized = false, disabled = false, finished = false;
 	boolean loopMode = false;
 	boolean doLoop = false;
 	boolean removeModeDrop = false;
 	boolean removeModeIteration = false;//false:descend true:ascend
 	Position from,to;
 	List<Position> removeList;
 	ListIterator removeListIterator;
 	List<Position> fillList;
 	ListIterator fillListIterator;
 	//Quarry Mode
 	boolean frameCreated = false;
 	List<Position> frameBuildList;
 	ListIterator frameBuildListIterator;
 	List<Position> quarryList;
 	ListIterator quarryListIterator;
 	//used Quarry Mode and Flatten Mode
 	HashSet<Coord> ignoreCoordSet;
 	ForgeDirection orient;
 	
 	public TileAdvFiller() {
 		rate = AdvFiller.energyRate;
 		powerProvider = PowerFramework.currentFramework.createPowerProvider();
 		powerProvider.configure(20, 1, 1000, 25, 1000);
 		powerProvider.configurePowerPerdition(0, 100);
 		//configure for quarry mode
 		left = 5;
 		right = 5;
 		up = 0;
 		down = 0;
 		forward = 10;
 		type = 0;
 	}
 	
 	public void placed(){
 		if(!worldObj.isRemote){
 			IAreaProvider a = null;
 			Position pos = new Position(xCoord,yCoord,zCoord,orient);
 			pos.moveForwards(1);
 			TileEntity tile = worldObj.getBlockTileEntity(pos.x, pos.y, pos.z);
 			if(tile instanceof IAreaProvider)
 				a = (IAreaProvider)tile;
 			if (a != null) {
 				calculateMarker(a);
 				if(bcLoaded)
 					BuildCraftProxy.removeMarker(a);
 			}
 		}
 	}
 	
 	@SuppressWarnings("incomplete-switch")
 	public void calculateMarker(IAreaProvider a){
 		switch(orient){
 		case SOUTH:
 			break;
 		case NORTH:
 			break;
 		case EAST:
 			break;
 		case WEST:
 		}
 		Position pos = new Position(xCoord,yCoord,zCoord,orient);
 		pos.moveForwards(1);
 		//System.out.println(pos.toString());
 		//System.out.println(a.xMin() + "," + a.yMin() + "," + a.zMin() + "," + a.xMax() + "," + a.yMax() + "," + a.zMax());
 		int x,y,z;
 		//minX
 		if(a.xMin() < a.xMax())
 			x = pos.x - a.xMin();
 		else
 			x = pos.x - a.xMax();
 		switch(orient){
 		case SOUTH:
 			right = x;
 			break;
 		case NORTH:
 			left = x;
 			break;
 		case EAST:
 			break;
 		case WEST:
 			forward = x;
 		}
 		
 		//maxX
 		if(a.xMin() < a.xMax())
 			x = a.xMax() - pos.x;
 		else
 			x = a.xMin() - pos.x;
 		switch(orient){
 		case SOUTH:
 			left = x;
 			break;
 		case NORTH:
 			right = x;
 			break;
 		case EAST:
 			forward = x;
 			break;
 		case WEST:
 		}
 		
 		//minZ
 		if(a.zMin() < a.zMax())
 			z = pos.z - a.zMin();
 		else
 			z = pos.z - a.zMax();
 		switch(orient){
 		case SOUTH:
 			break;
 		case NORTH:
 			forward = z;
 			break;
 		case EAST:
 			left = z;
 			break;
 		case WEST:
 			right = z;
 		}
 		
 		//maxZ
 		if(a.zMin() < a.zMax())
 			z = a.zMax() - pos.z;
 		else
 			z = a.zMin() - pos.z;
 		switch(orient){
 		case SOUTH:
 			forward = z;
 			break;
 		case NORTH:
 			break;
 		case EAST:
 			right = z;
 			break;
 		case WEST:
 			left = z;
 		}
 		
 		//minY
 		down = pos.y - a.yMin();
 		
 		//maxY
 		up = a.yMax() - pos.y;
 	}
 	
 	public void preInit(){
 		setDisable();
 		dim = worldObj.getWorldInfo().getDimension();
 		orient = ForgeDirection.values()[worldObj.getBlockMetadata(xCoord, yCoord, zCoord)].getOpposite();
 		if(orient == ForgeDirection.UP || orient == ForgeDirection.DOWN || orient == ForgeDirection.UNKNOWN){
 			return;
 		}
 		setArea();
 		setBox();
 		initializeThread = new Thread(new AdvFillerInitializeThread(this));
 		initializeThread.start();
 		initialized = true;
 	}
 	
 	public void init(){
 		this.tick = 0;
 		switch(type){
 		case 0:
 			frameCreated = false;
 			ignoreCoordSet = new HashSet();
 			calculateFrame();
 			createQuarryList();
 			break;
 		case 1:
 			createRemoveList();
 			break;
 		case 2:
 			createFillList();
 			break;
 		case 3:
 			createFlattenList();
 			break;
 		case 4:
 			createExclusiveRemoveList();
 			break;
 		case 5:
 			createTofuBuildList();
 		}
 		if(doLoop)
 			return;
 		//フロントの表示の更新に使用
 		//鯖側で実行するとブロックの情報(IDやらメタデータやら)と、
 		//TileEntityの情報(getDescriptionPacketで作ったパケット)が送られる模様
 		//蔵側で実行すると表示の更新が行われる(結構重いので注意)
 		worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
 	}
 	
 
 	public void setDisable() {
 		this.disabled = true;
 	}
 	
 	public void setEnable(){
 		this.disabled = false;
 	}
 	
 	public void setBox() {
 		fromX = (int)from.x;
 		fromY = (int)from.y;
 		fromZ = (int)from.z;
 		toX = (int)to.x;
 		toY = (int)to.y;
 		toZ = (int)to.z;
 		if(bcLoaded)
 			BuildCraftProxy.proxy.getBox(this).initialize(fromX, fromY, fromZ, toX, toY, toZ);
 	}
 
 	public void setArea(){
 		if(bcLoaded)
 			BuildCraftProxy.proxy.getBox(this).reset();
 		Position pos1 = new Position(xCoord,yCoord,zCoord,orient);
 		pos1.moveForwards(1);
 		pos1.moveLeft(left);
 		pos1.moveDown(down);
 		if(pos1.y <= 0)
 			pos1.y = 1;
 		Position pos2 = new Position(xCoord,yCoord,zCoord,orient);
 		pos2.moveForwards(1 + forward);
 		pos2.moveRight(right);
 		pos2.moveUp(up);
 		if(pos2.y > 255)
 			pos2.y = 255;
 		from = pos1.min(pos2);
 		to = pos1.max(pos2);
 	}
 	
 	//パケットで使用
 	public void setArea(int left, int right, int up, int down,
 			int forward, int type) {
 		this.left = left;
 		this.right = right;
 		this.up = up;
 		this.down = down;
 		this.forward = forward;
 		this.type = type;
 	}
 
 	@Override
 	public void setPowerProvider(IPowerProvider provider) {
 		this.powerProvider = provider;
 	}
 
 	@Override
 	public IPowerProvider getPowerProvider() {
 		return this.powerProvider;
 	}
 
 	@Override
 	public void doWork() {}
 	
 	public void processEnergy(){
 		if(worldObj.isBlockIndirectlyGettingPowered(xCoord, yCoord, zCoord))
 			this.powerProvider.receiveEnergy(Float.MAX_VALUE, ForgeDirection.UP);
 	}
 
 	
 	@Override
 	public void updateEntity() {
 		super.updateEntity();
 		if(worldObj.isRemote && !AdvFiller.bcFrameRenderer){
 			//描画用エンティティがスポーンしてない場合はスポーンさせる
 			if(!this.doRender){
 				//描画フラグと共有
 				this.doRender = true;
 				EntityRendererFiller e = new EntityRendererFiller(worldObj, this);
 				//天候エフェクトのふり
 				worldObj.addWeatherEffect(e);
 				//e.setPosition());
 			}
 			return;
 		}
 		if(worldObj.isRemote)
 			return;
 		processEnergy();
 		if(!initialized)
 			preInit();
 		if(disabled)
 			return;
 		this.tick++;
 		if(loopMode && this.tick > AdvFiller.loopTick){
 			this.doLoop = true;
 			setDisable();
 			player = null;
 			initializeThread = new Thread(new AdvFillerInitializeThread(this));
 			initializeThread.start();
 			return;
 		}
 		if(finished)
 			return;
 		if(type == 0)
 			doQuarryMode();
 		if(type == 1 || type == 4)
 			doRemoveMode();
 		if(type == 2 || type == 5)
 			doFillMode();
 		if(type == 3)
 			doFlattenMode();
 	}
 	
 	//Quarry Mode
 	
 	public void doQuarryMode(){
 		if(!frameCreated){
 			buildFrame();
 			return;
 		}
 		dig();
 	}
 	
 	public boolean checkFrame(int x, int y, int z){
 		if(y == fromY || y == toY)
 			return (x == fromX || x == toX) || (z == fromZ || z == toZ);
 		else
 			return (x == fromX || x == toX) && (z == fromZ || z == toZ);
 	}
 	
 	void calculateFrame(){
 		frameBuildList = new ArrayList();
 		removeList = new ArrayList();
 		fromY = (int)yCoord;
 		toY = (int)yCoord + 4;
 		if(toY > 255){
 			finished = true;
 			return;
 		}
 		if(toX - fromX < 2 || toZ - fromZ < 2){
 			finished = true;
 			return;
 		}
 		if(bcLoaded)
 			BuildCraftProxy.proxy.getBox(this).initialize(fromX, fromY, fromZ, toX, toY, toZ);
 		for(int y = fromY; y <= toY; y++){
 			for(int x = fromX; x <= toX; x++){
 				for(int z = fromZ; z <= toZ; z++){
 					if(checkFrame(x,y,z)){
 						if(worldObj.getBlockId(x, y, z) != BuildCraftProxy.getFrameBlockId()){
 							if(!AdvFiller.fillingSet.contains(worldObj.getBlockId(x, y, z)))
 								removeList.add(new Position(x,y,z));
 							frameBuildList.add(new Position(x,y,z));
 						}
 					}else{
 						if(!AdvFiller.fillingSet.contains(worldObj.getBlockId(x, y, z)))
 							removeList.add(new Position(x,y,z));
 					}
 				}
 			}
 		}
 		frameBuildListIterator = frameBuildList.listIterator();
 		removeListIterator = removeList.listIterator();
 	}
 	
 	public void buildFrame(){
 		if(powerProvider.useEnergy(rate, rate, false) != rate)
 			return;
 		powerProvider.useEnergy(rate, rate, true);
 		if(removeListIterator.hasNext()){
 			Position pos = (Position)removeListIterator.next();
 			worldObj.setBlock(pos.x, pos.y, pos.z, 0);
 			return;
 		}
 		if(frameBuildListIterator.hasNext()){
 			Position pos = (Position)frameBuildListIterator.next();
 			worldObj.setBlock(pos.x, pos.y, pos.z, BuildCraftProxy.getFrameBlockId(), 0, 3);
 			return;
 		}
 		calculateFrame();
 		if(removeListIterator.hasNext() || frameBuildListIterator.hasNext())
 			return;
 		frameCreated = true;
 	}
 	
 	public void createQuarryList(){
 		quarryList = new ArrayList();
 		for(int y = yCoord -1 ; y >= 1 ; y--){
 			for(int x = fromX + 1 ; x <= toX - 1 ; x++){
 				for(int z = fromZ + 1 ; z <= toZ - 1 ; z++){
 					if(checkBreakable(x,y,z))
 						if(!AdvFiller.fillingSet.contains(worldObj.getBlockId(x, y, z)))
 							quarryList.add(new Position(x,y,z));
 				}
 			}
 		}
 		quarryListIterator = quarryList.listIterator();
 	}
 	
 	public boolean checkBreakable(int x, int y, int z){
 		if(ignoreCoordSet.contains(new Coord(x, z)))
 			return false;
 		if(!BlockLib.canChangeBlock(worldObj, x, y, z)){
 			ignoreCoordSet.add(new Coord(x, z));
 			return false;
 		}
 		return true;
 	}
 	
 	public void dig(){
 		
 		if(powerProvider.useEnergy(rate * 4, rate * 4, false) != rate * 4)
 			return;
 		powerProvider.useEnergy(rate * 4, rate * 4, true);
 		if(quarryListIterator.hasNext()){
 			Position pos = (Position)quarryListIterator.next();
 			List<ItemStack> stacks = BlockLib.getBlockDropped(worldObj, pos.x, pos.y, pos.z);
 			if(AdvFiller.breakEffect)
 				//クァーリーよりコピペ
 				worldObj.playAuxSFXAtEntity(null, 2001, pos.x, pos.y, pos.z, (worldObj.getBlockId(pos.x, pos.y, pos.z) + (worldObj.getBlockMetadata(pos.x, pos.y, pos.z) << 12)));
 			worldObj.setBlock(pos.x, pos.y, pos.z, 0);
 			if (stacks == null || stacks.isEmpty())
 				return;
 			for (ItemStack stack : stacks) {
 				if(bcLoaded){
 					ItemStack added = BuildCraftProxy.addToRandomInventory(stack, worldObj, xCoord, yCoord, zCoord, ForgeDirection.UNKNOWN);
 					stack.stackSize -= added.stackSize;
 				}
 				else
 					stack = BlockLib.insertStackToNearInventory(stack, this);
 				if (stack == null || stack.stackSize <= 0) {
 					continue;
 				}
 				BuildCraftProxy.addToRandomPipeEntry(this, ForgeDirection.UNKNOWN, stack);
 			}
 		}else{
 			finished = true;
 			worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
 		}
 	}
 	
 	//RemoveMode
 	
 	public void createRemoveList(){
 		removeList = new ArrayList();
 		for(int y = fromY; y <= toY; y++)
 			for(int x = fromX; x <= toX; x++)
 				for(int z = fromZ; z <= toZ; z++)
 					if(BlockLib.canChangeBlock(worldObj, x, y, z) && worldObj.getBlockId(x, y, z) != 0)
 						removeList.add(new Position(x,y,z));
 		if(this.removeModeIteration)
 			removeListIterator = removeList.listIterator();
 		else
 			removeListIterator = removeList.listIterator(removeList.size());
 	}
 	
 	public void doRemoveMode(){
 		Position pos;
 		if(this.removeModeIteration){
 			for(int i = 0; i < 4; i++){
 				if(removeListIterator.hasNext()){
 					if(type == 1){
 						if(powerProvider.useEnergy(rate, rate, false) != rate)
 							return;
 					}
 					if(type == 4){
 						if(powerProvider.useEnergy(rate * 6, rate * 6, false) != rate * 6)
 							return;
 					}
 					if(type == 1)
 						powerProvider.useEnergy(rate, rate, true);
 					if(type == 4){
 						powerProvider.useEnergy(rate * 6, rate * 6, true);
 						i = 3;
 					}
 					pos = (Position)removeListIterator.next();
 					doRemove(pos.x, pos.y, pos.z);
 
 				}else if(!loopMode){
 					finished = true;
 					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
 				}
 			}
 			return;
 		}else{
 			for(int i = 0; i < 4; i++){
 				if(removeListIterator.hasPrevious()){
 					if(type == 1){
 						if(powerProvider.useEnergy(rate, rate, false) != rate)
 							return;
 					}
 					if(type == 4){
 						if(powerProvider.useEnergy(rate * 6, rate * 6, false) != rate * 6)
 							return;
 					}
 					if(type == 1)
 						powerProvider.useEnergy(rate, rate, true);
 					if(type == 4){
 						powerProvider.useEnergy(rate * 6, rate * 6, true);
 						i = 3;
 					}
 					pos = (Position)removeListIterator.previous();
 					doRemove(pos.x, pos.y, pos.z);
 				}else if(!loopMode){
 					finished = true;
 					worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
 				}
 			}
 			return;
 		}
 	}
 	
 	public void doRemove(int x, int y, int z){
 		if(AdvFiller.breakEffect)
 			//クァーリーよりコピペ
 			worldObj.playAuxSFXAtEntity(null, 2001, x, y, z, (worldObj.getBlockId(x, y, z) + (worldObj.getBlockMetadata(x, y, z) << 12)));
 		if(!(CommonProxy.proxy.isServer() && !AdvFiller.removeModeDrop) && removeModeDrop){
 			int meta = worldObj.getBlockMetadata(x, y, z);
 			int id = worldObj.getBlockId(x, y, z);
 			if(Block.blocksList[id] == null)
 				return;
 			Block.blocksList[id].dropBlockAsItem(worldObj, x, y, z, meta, 0);
 		}
 		worldObj.setBlock(x, y, z, 0);
 	}
 	
 	//FillingMode
 	public void createFillList(){
 		fillList = new ArrayList();
 		for(int y = fromY; y <= toY; y++)
 			for(int x = fromX; x <= toX; x++)
 				for(int z = fromZ; z <= toZ; z++)
 					if(AdvFiller.fillingSet.contains(worldObj.getBlockId(x, y, z)))
 						fillList.add(new Position(x,y,z));
 		fillListIterator = fillList.listIterator();
 	}
 	
 	public void doFillMode(){
 		Position pos;
 		for(int i = 0; i < 4; i++){
 			if(fillListIterator.hasNext()){
 				if(powerProvider.useEnergy(rate, rate, false) != rate)
 					return;
 				pos = (Position)fillListIterator.next();
 				if(doFill(pos.x, pos.y, pos.z))
 						powerProvider.useEnergy(rate, rate, true);
 				else if(!loopMode){
 					fillListIterator.previous();
 					return;
 				}
 			}else if(!loopMode){
 				finished = true;
 				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
 			}
 		}
 	}
 	
 	public boolean doFill(int x, int y, int z){
 		TileEntity tile = worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord);
 		if(!(tile instanceof IInventory))
 			return false;
 		IInventory inv = (IInventory)tile;
 		ItemStack is = null;
 		int stackslot = 0;
 		for(int i = 0;i< inv.getSizeInventory(); i++){
 			is = inv.getStackInSlot(i);
 			stackslot = i;
 			if(is != null)
 				break;
 		}
 		if(is == null)
 			return false;
 		boolean success = is.getItem().onItemUse(is, FakePlayerFactory.getMinecraft(worldObj), worldObj, x, y - 1, z, 1, 0.0f, 0.0f, 0.0f);
 		if(is.stackSize < 1)
 			inv.setInventorySlotContents(stackslot, null);
 		return success;
 	}
 	
 	ItemStack getStackFromUpperInventory(){
 		TileEntity tile = worldObj.getBlockTileEntity(xCoord, yCoord + 1, zCoord);
 		if(!(tile instanceof IInventory))
 			return null;
 		IInventory inv = (IInventory)tile;
 		ItemStack is = null;
 		for(int i = 0;i< inv.getSizeInventory(); i++){
 			is = inv.getStackInSlot(i);
 			if(is != null)
 				break;
 		}
 		return is;
 	}
 	
 	//FlattenMode
 	
 	public void createFlattenList(){
 		long time = System.currentTimeMillis();
 		removeList = new ArrayList();
 		fillList = new ArrayList();
 		for(int y = yCoord; y <= 255; y++)
 			for(int x = fromX; x <= toX; x++)
 				for(int z = fromZ; z <= toZ; z++){
 					int blockID = worldObj.getBlockId(x, y, z);
 					if(BlockLib.canChangeBlock(blockID, worldObj, x, y, z) && blockID != 0)
 						removeList.add(new Position(x,y,z));
 				}
 		System.out.println(System.currentTimeMillis() - time);
 		removeListIterator = removeList.listIterator();
 		ignoreCoordSet = new HashSet();
 		for(int y = yCoord - 1; y > 0; y--)
 			for(int x = fromX; x <= toX; x++)
 				for(int z = fromZ; z <= toZ; z++)
 					if(true){
 						if(AdvFiller.fillingSet.contains(worldObj.getBlockId(x, y, z)))
 							fillList.add(new Position(x,y,z));
 						else
 							ignoreCoordSet.add(new Coord(x,z));
 					}
 		fillListIterator = fillList.listIterator(fillList.size());
 		System.out.println(System.currentTimeMillis() - time);
 	}
 	
 	public boolean isIgnoreCoord(int x, int z){
 		if(ignoreCoordSet.contains(new Coord(x,z)))
 			return true;
 		return false;
 	}
 	
 	public void doFlattenMode(){
 		Position pos;
 		for(int i = 0; i < 4; i++){
 			if(removeListIterator.hasNext()){
 				if(powerProvider.useEnergy(rate, rate, false) != rate)
 					return;
 				powerProvider.useEnergy(rate, rate, true);
 				pos = (Position)removeListIterator.next();
 				doRemove(pos.x, pos.y, pos.z);
 				if(i == 3)//下に制御が行かないように
 					return;
 			}
 		}
 		for(int i = 0; i < 4; i++){
 			if(fillListIterator.hasPrevious()){
 				if(powerProvider.useEnergy(rate, rate, false) != rate)
 					return;
 				pos = (Position)fillListIterator.previous();
 				if(doFill(pos.x, pos.y, pos.z))
 					powerProvider.useEnergy(rate, rate, true);
 				else{
 					if(!loopMode){
 						fillListIterator.next();
 						return;
 					}
 				}
 			}else if(!loopMode){
 				finished = true;
 				worldObj.markBlockForUpdate(xCoord, yCoord, zCoord);
 			}
 		}
 
 	}
 	
 	//Exclusive Remove Mode
 	
 	public void createExclusiveRemoveList(){
 		removeList = new ArrayList();
 		int removeID = worldObj.getBlockId(xCoord, yCoord + 1, zCoord);
 		if(removeID == 0){
 			finished = true;
 			return;
 		}
 		for(int y = fromY; y <= toY; y++)
 			for(int x = fromX; x <= toX; x++)
 				for(int z = fromZ; z <= toZ; z++)
 					if(worldObj.getBlockId(x, y, z) == removeID)
 						removeList.add(new Position(x,y,z));
 		if(this.removeModeIteration)
 			removeListIterator = removeList.listIterator();
 		else
 			removeListIterator = removeList.listIterator(removeList.size());
 	}
 	
 	//TofuBuild Mode
 	
 	public void createTofuBuildList(){
 		fillList = new ArrayList();
 		for(int y = fromY; y <= toY; y++){
 			if(y == fromY || y == toY){
 				for(int x = fromX; x <= toX; x++){
 					for(int z = fromZ; z <= toZ; z++){
 						if(worldObj.getBlockId(x, y, z) == 0)
 							fillList.add(new Position(x,y,z));
 					}
 				}
 			}else
 				for(int x = fromX; x <= toX; x++)
 					for(int z = fromZ; z <= toZ; z++){
 						if(((x == fromX || x == toX) || (z == fromZ || z == toZ)) && worldObj.getBlockId(x, y, z) == 0)
 							fillList.add(new Position(x,y,z));
 					}
 		}
 		fillListIterator = fillList.listIterator();
 	}
 	
 	@Override
 	public void readFromNBT(NBTTagCompound nbt) {
 		super.readFromNBT(nbt);
 		left = nbt.getInteger("left");
 		right = nbt.getInteger("right");
 		up = nbt.getInteger("up");
 		down = nbt.getInteger("down");
 		forward = nbt.getInteger("forward");
 		type = nbt.getInteger("type");
 		loopMode = nbt.getBoolean("loop");
 		removeModeIteration = nbt.getBoolean("iterate");
 		removeModeDrop = nbt.getBoolean("drop");
 		PowerFramework.currentFramework.loadPowerProvider(this, nbt);
 		powerProvider.configure(20, 1, 1000, 25, 1000);
 		powerProvider.configurePowerPerdition(0, 100);
 		
 	}
 
 	@Override
 	public void writeToNBT(NBTTagCompound nbt) {
 		super.writeToNBT(nbt);
 		nbt.setInteger("left", left);
 		nbt.setInteger("right", right);
 		nbt.setInteger("up", up);
 		nbt.setInteger("down", down);
 		nbt.setInteger("forward", forward);
 		nbt.setInteger("type", type);
 		nbt.setBoolean("loop", loopMode);
 		nbt.setBoolean("iterate", removeModeIteration);
 		nbt.setBoolean("drop", removeModeDrop);
 		PowerFramework.currentFramework.savePowerProvider(this, nbt);
 	}
 
 	public boolean isActive() {
 		return !this.disabled && !finished;
 	}
 	
 	@Override
 	public Packet getDescriptionPacket() {
 		ByteArrayOutputStream bos = new ByteArrayOutputStream();
 		DataOutputStream dos = new DataOutputStream(bos);
 	 
 		try
 		{
 			dos.writeInt(fromX);
 			dos.writeInt(fromY);
 			dos.writeInt(fromZ);
 			dos.writeInt(toX);
 			dos.writeInt(toY);
 			dos.writeInt(toZ);
 			dos.writeInt(xCoord);
 			dos.writeInt(yCoord);
 			dos.writeInt(zCoord);
 			dos.writeInt(left);
 			dos.writeInt(right);
 			dos.writeInt(up);
 			dos.writeInt(down);
 			dos.writeInt(forward);
 			dos.writeInt(type);
 			dos.writeBoolean(loopMode);
 			dos.writeBoolean(finished);
 			dos.writeBoolean(disabled);
 			dos.writeBoolean(removeModeIteration);
 			dos.writeBoolean(removeModeDrop);
 		}
 		catch (Exception e)
 		{
 			e.printStackTrace();
 		}
 	 
 		Packet250CustomPayload packet = new Packet250CustomPayload();
 		packet.channel = "advfiller_client";
 		packet.data    = bos.toByteArray();
 		packet.length  = bos.size();
 		packet.isChunkDataPacket = true;
 		return packet;
 	}
 
 	//クライアント用
 	@Override
 	public void invalidate() {
 		if(!worldObj.isRemote){
 			//止めても問題ないはず
			if(initializeThread != null)
				initializeThread.stop();
 		}
 		//クライアント用
 		if(bcLoaded)
 			BuildCraftProxy.proxy.getBox(this).deleteLasers();
 		doRender = false;
 		super.invalidate();
 	}
 	
 	//クァーリーよりコピペ
 	public void setLoadingChunks(){
 		chunkTicket.getModData().setInteger("xCoord", xCoord);
 		chunkTicket.getModData().setInteger("yCoord", yCoord);
 		chunkTicket.getModData().setInteger("zCoord", zCoord);
 		Set<ChunkCoordIntPair> chunks = Sets.newHashSet();
 		ChunkCoordIntPair myChunk = new ChunkCoordIntPair(xCoord >> 4, zCoord >> 4);
 		chunks.add(myChunk);
 		ForgeChunkManager.forceChunk(this.chunkTicket, myChunk);
 		for (int chunkX = fromX >> 4; chunkX <= toZ >> 4; chunkX++) {
 			for (int chunkZ = fromZ >> 4; chunkZ <= toZ >> 4; chunkZ++) {
 				ChunkCoordIntPair chunk = new ChunkCoordIntPair(chunkX, chunkZ);
 				ForgeChunkManager.forceChunk(this.chunkTicket, chunk);
 				chunks.add(chunk);
 			}
 		}
 		if (player != null) {
 			PacketDispatcher.sendPacketToPlayer(
 					new Packet3Chat(String.format("[BUILDCRAFT ADDON : ADVFILLER] The filler at %d %d %d will keep %d chunks loaded", xCoord, yCoord, zCoord, chunks.size())),
 					(Player) player);
 		}
 	}
 	
 	@Override
 	public int powerRequest(ForgeDirection from) {
 		if (isActive())
 			return (int) Math.ceil(Math.min(getPowerProvider().getMaxEnergyReceived(), getPowerProvider().getMaxEnergyStored()
 					- getPowerProvider().getEnergyStored()));
 		else
 			return 0;
 	}
 }
