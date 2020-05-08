 package Enderware.modsupport.computercraft.peripheral;
 
 import net.minecraft.command.ICommandSender;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.nbt.NBTTagCompound;
 import net.minecraft.network.packet.Packet3Chat;
 import net.minecraft.server.MinecraftServer;
 import net.minecraft.src.ModLoader;
 import net.minecraft.tileentity.TileEntity;
 import net.minecraft.util.ChunkCoordinates;
 import Enderware.config.Config;
 import Enderware.listener.Eventhandler;
 import Enderware.permissions.PermissionManager;
 import cpw.mods.fml.common.FMLCommonHandler;
 import dan200.computer.api.IComputerAccess;
 import dan200.computer.api.IPeripheral;
 
 
 public class CommandPeripheral extends TileEntity implements IPeripheral , ICommandSender{
     public String placedby;
     public double code = -1;
     public boolean messagetoplayer = false;
 public CommandPeripheral(){
     super();
    
    
 }
 public void onPlaced(EntityPlayer player,int code){
     if(worldObj.isRemote)return;
     this.code = code;
     this.placedby = player.username;
 }
     @Override
     public void attach(IComputerAccess arg0) {
     }
     @Override
     public void writeToNBT(NBTTagCompound par1nbtTagCompound) {
         if(worldObj.isRemote)return;
         par1nbtTagCompound.setString("placedBy", placedby);
         par1nbtTagCompound.setInteger("code", (int)code);
         super.writeToNBT(par1nbtTagCompound);
     }
     @Override
     public void readFromNBT(NBTTagCompound par1nbtTagCompound) {
         if(worldObj.isRemote)return;
         this.placedby = par1nbtTagCompound.getString("placedBy");
         this.code = par1nbtTagCompound.getInteger("code");
         super.readFromNBT(par1nbtTagCompound);
     }
     @Override
     public Object[] callMethod(IComputerAccess access, int method, Object[] params)
             throws Exception {
         if(this.worldObj.isRemote)return null;
         double id = 0;
         switch(method){
             case 0:
         Object[] temp = {placedby};
                 return temp;
                
             case 1:
                
                 try {
                     id = (Double)params[0];
                 } catch (Exception e) {
                     e.printStackTrace();
                     throw new Exception("Parameter 1 : Number expexted");
                 }
                 if(!(params[1]instanceof String))throw new Exception("Parmeter 2 : String expected!");
                 
                 if(id != code)throw new Exception("WRONG PASSWORD");
                 try{
                 if((FMLCommonHandler.instance().getEffectiveSide().isClient()||(PermissionManager.instance.hasPlayerPermission(placedby, "Colors")&&((Boolean)(Config.configs.get("ChatColors")))))){
                     String message = "";
                     
                     
                     int i = 0;
               
                     while(i < ((String)params[1]).length()){
                         if(((String)params[1]).charAt(i) == '#'&&((String)params[1]).length() > i) {
                    
                             message = message+"\u00a7"+((String)params[1]).charAt(++i);
                         }else{
                 
                         message = message+((String)params[1]).charAt(i);
                         }
                         i++;
                         }
                     params[1] = message;
                   
                    
                     
                 }
                 }catch(Exception e){
                     e.printStackTrace();
                 }
                
                 
                 new Eventhandler().sendCHatPackages(this.xCoord, this.yCoord, this.zCoord, 30, 0, new Packet3Chat(((String)params[1])+(String)params[2]));
                 return new Object[]{params[1]};
             case 2:
                id = 0;
                 try {
                     id = (Double)params[0];
                 } catch (Exception e) {
                     e.printStackTrace();
                     throw new Exception("Parameter 1 : Number expexted");
                 }
                 if(!(params[1]instanceof String))throw new Exception("Parmeter 2 : String expected!");
                 
                 if(id != code)throw new Exception("WRONG PASSWORD");
                 MinecraftServer.getServer().getCommandManager().executeCommand(this, (String) params[1]);
                 return new Object[]{params[0]};
                 
         }
         return null;
     }
     @Override
     public boolean canAttachToSide(int arg0) {
         return true;
     }
 
     @Override
     public void detach(IComputerAccess arg0) {
     }
 
     @Override
     public String[] getMethodNames() {
         String[] temp  = {"getPlayer","say","executeCommand"};
         return temp;
     }
 
     @Override
     public String getType() {
     return "PlayerCommandBlock";
     }
     @Override
     public String getCommandSenderName() {
         return placedby;
     }
     @Override
     public void sendChatToPlayer(String var1) {
         if(worldObj.isRemote)return;
         if(messagetoplayer){
             EntityPlayer player = MinecraftServer.getServerConfigurationManager(ModLoader.getMinecraftServerInstance()).getPlayerForUsername(placedby);
             if( player != null)player.sendChatToPlayer(var1);
         }
     }
     @Override
     public boolean canCommandSenderUseCommand(int var1, String var2) {
         boolean op = false;
         if(worldObj.isRemote)return false;
        if(FMLCommonHandler.instance().getEffectiveSide().isClient())return true;
         @SuppressWarnings("rawtypes")
         java.util.Iterator i = MinecraftServer.getServerConfigurationManager(MinecraftServer.getServer()).getOps().iterator();
         while(!op && i.hasNext()){
             op = op || i.next().equals(placedby);
         }
         
         return op;
     }
     @Override
     public String translateString(String var1, Object... var2) {
         return null;
     }
     @Override
     public ChunkCoordinates getPlayerCoordinates() {
         return null;
     }
 
 }
