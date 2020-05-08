 package jk_5.nailed.blocks.tileentity;
 
 import cpw.mods.fml.common.FMLCommonHandler;
 import cpw.mods.fml.common.network.ByteBufUtils;
 import io.netty.buffer.ByteBuf;
 import jk_5.nailed.api.NailedAPI;
 import jk_5.nailed.api.map.stat.IStatTileEntity;
 import jk_5.nailed.api.map.stat.Stat;
 import jk_5.nailed.api.map.stat.StatManager;
import jk_5.nailed.api.map.stat.StatMode;
 import jk_5.nailed.api.player.Player;
 import jk_5.nailed.blocks.NailedBlocks;
 import jk_5.nailed.gui.IGuiReturnHandler;
 import lombok.Getter;
 import lombok.NoArgsConstructor;
 import lombok.Setter;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.util.ChatComponentTranslation;
 import net.minecraft.util.EnumChatFormatting;
 import net.minecraftforge.permissions.api.PermissionsManager;
 
 /**
  * No description given
  *
  * @author jk-5
  */
 @NoArgsConstructor
 public class TileEntityStatEmitter extends NailedTileEntity implements IStatTileEntity, IGuiTileEntity, IGuiReturnHandler {
 
     public static final String PERMNODE = "nailed.statEmitter.edit";
 
     @Getter private String programmedName = "";
     @Getter private boolean signalEnabled = false;
     @Getter @Setter private StatMode mode = StatMode.NORMAL;
     @Getter private Stat stat;
     private boolean needsUpdate = false;
     private boolean isLoaded = false;
     private int redstonePulseTicks = -1;
     private int pulseLength = 4;
 
     public void setStatName(String statName){
         this.programmedName = statName;
         if(this.worldObj == null) this.needsUpdate = true;
         else if(!this.worldObj.isRemote){
             StatManager manager = NailedAPI.getMapLoader().getMap(this.worldObj).getStatManager();
             this.stat = manager.getStat(this.programmedName);
             manager.registerStatTile(this);
             this.isLoaded = true;
             if(this.stat == null){
                 this.disable();
             }else{
                 if(this.stat.isEnabled()) this.enable();
                 else this.disable();
             }
         }
     }
 
     @Override
     public void updateEntity(){
         if(this.needsUpdate){
             StatManager manager = NailedAPI.getMapLoader().getMap(this.worldObj).getStatManager();
             this.stat = manager.getStat(this.programmedName);
             manager.registerStatTile(this);
             this.isLoaded = true;
             if(this.stat == null){
                 this.disable();
             }else{
                 if(this.stat.isEnabled()) this.enable();
                 else this.disable();
             }
             this.needsUpdate = false;
         }
         if(!this.isLoaded){
             NailedAPI.getMapLoader().getMap(this.worldObj).getStatManager().registerStatTile(this);
             this.isLoaded = true;
         }
         if(this.redstonePulseTicks != -1){
             if(this.redstonePulseTicks == this.pulseLength){
                 this.signalEnabled = false;
                 this.scheduleRedstoneUpdate();
                 this.redstonePulseTicks = -1;
             }else{
                 this.redstonePulseTicks++;
             }
         }
     }
 
     @Override
     public void invalidate(){
         super.invalidate();
         if(this.isLoaded){
             NailedAPI.getMapLoader().getMap(this.worldObj).getStatManager().unloadStatTile(this);
             this.isLoaded = false;
         }
     }
 
     @Override
     public void onChunkUnload(){
         super.onChunkUnload();
         if(this.isLoaded){
             NailedAPI.getMapLoader().getMap(this.worldObj).getStatManager().unloadStatTile(this);
             this.isLoaded = false;
         }
     }
 
     @Override
     public boolean canUpdate(){
         return FMLCommonHandler.instance().getEffectiveSide().isServer();
     }
 
     @Override
     public void readFromNBT(NBTTagCompound tag){
         super.readFromNBT(tag);
         this.setStatName(tag.getString("name"));
         this.mode = StatMode.values()[tag.getByte("mode")];
         this.pulseLength = tag.getInteger("pulseLength");
     }
 
     @Override
     public void writeToNBT(NBTTagCompound tag){
         super.writeToNBT(tag);
         tag.setString("name", this.programmedName);
         tag.setByte("mode", (byte) this.mode.ordinal());
         tag.setInteger("pulseLength", this.pulseLength);
     }
 
     @Override
     public void enable(){
         if(this.stat == null && this.signalEnabled){
             this.signalEnabled = false;
             this.scheduleRedstoneUpdate();
             return;
         }
         if(this.mode == StatMode.NORMAL){
             this.signalEnabled = true;
             this.scheduleRedstoneUpdate();
         }else if(this.mode == StatMode.INVERSED){
             this.signalEnabled = false;
             this.scheduleRedstoneUpdate();
         }else if(this.mode == StatMode.PULSE_ON || this.mode == StatMode.PULSE_BOTH){
             this.signalEnabled = true;
             this.scheduleRedstoneUpdate();
             this.redstonePulseTicks = 0;
         }
     }
 
     @Override
     public void disable(){
         if(this.stat == null && this.signalEnabled){
             this.signalEnabled = false;
             this.scheduleRedstoneUpdate();
             return;
         }
         if(this.mode == StatMode.NORMAL){
             this.signalEnabled = false;
             this.scheduleRedstoneUpdate();
         }else if(this.mode == StatMode.INVERSED){
             this.signalEnabled = true;
             this.scheduleRedstoneUpdate();
         }else if(this.mode == StatMode.PULSE_OFF || this.mode == StatMode.PULSE_BOTH){
             this.signalEnabled = true;
             this.scheduleRedstoneUpdate();
             this.redstonePulseTicks = 0;
         }
     }
 
     @Override
     public boolean canPlayerOpenGui(Player player){
         if(!PermissionsManager.getPerm(player.getUsername(), PERMNODE, this).check()){
             ChatComponentTranslation message = new ChatComponentTranslation("nailed.noPermission");
             message.getChatStyle().setColor(EnumChatFormatting.RED);
             player.sendChat(message);
             return false;
         }
         return true;
     }
 
     @Override
     public void writeGuiData(ByteBuf buffer){
         ByteBufUtils.writeUTF8String(buffer, this.programmedName);
         buffer.writeByte(this.mode.ordinal());
         buffer.writeByte(this.pulseLength);
     }
 
     @Override
     public void readGuiCloseData(ByteBuf buffer){
         this.setStatName(ByteBufUtils.readUTF8String(buffer));
         this.setMode(StatMode.values()[buffer.readByte()]);
         this.pulseLength = buffer.readByte();
     }
 
     public void scheduleRedstoneUpdate(){
         //this.worldObj.notifyBlockChange(this.xCoord, this.yCoord, this.zCoord, NailedBlocks.stat);
         this.worldObj.notifyBlocksOfNeighborChange(this.xCoord, this.yCoord, this.zCoord, NailedBlocks.stat);
     }
 }
