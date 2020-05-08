 package spleefmanager.misc;
 
 import net.minecraft.command.CommandBase;
 import net.minecraft.command.ICommandSender;
 import net.minecraft.command.WrongUsageException;
 import net.minecraft.entity.player.EntityPlayer;
 import net.minecraft.world.World;
 import spleefmanager.SpfManager;
 
 
 public class AreaInformation {
 	private static Area arr[];
 	private static int areaCount=0;
 	
 	
 	public AreaInformation() {
 		readAreaInformationFromFile();
 		// TODO Auto-generated constructor stub
 	}
 	
 	public boolean readAreaInformationFromFile()
 	{
 		//mks jiang jiao gei ni le =v=
 		//int x1 = SpfManager.nbt.getInteger(name +"x1");
 		//int x2 = SpfManager.nbt.getInteger(name +"x2");
 		//int z1 = SpfManager.nbt.getInteger(name +"z1");
 		//int z2 = SpfManager.nbt.getInteger(name +"z2");
 		//int y = SpfManager.nbt.getInteger(name +"y");
 		return true;
 	}
 	//Command uses this
 	public boolean saveAreaInformation(ICommandSender ics, int x1,int z1,int x2,int z2,int y,int BlockID,String name)
 	{
 		for(int i=0;i<areaCount;i++)
 		{
 			//detect name conflict
 			if(arr[i].sAreaName == name)
 				ics.sendChatToPlayer("Area Existing , delete it first plz ... ");
 			return false;
 		}
 		areaCount++;
		arr[areaCount]=new Area(x1,z1,x2,z2,y,BlockID,name);
 		SpfManager.nbt.setInteger(name + "x1", x1);
 		SpfManager.nbt.setInteger(name + "z1", z1);
 		SpfManager.nbt.setInteger(name + "x2", x2);
 		SpfManager.nbt.setInteger(name + "z2", z2);
 		SpfManager.nbt.setInteger(name + "y", y);
 		SpfManager.nbt.setInteger(name + "BlockID" , BlockID);
	//	SpfManager.nbt.setString(name,name);
 		ics.sendChatToPlayer("Sucessfully create the area <" + name + ">");
 		return true;
 	}
 	//Item uses this
 	public boolean saveAreaInformation(EntityPlayer par , int x1,int z1,int x2,int z2,int y,int BlockID,String name)
 	{
 		SpfManager.nbt.setInteger(name + "x1", x1);
 		SpfManager.nbt.setInteger(name + "z1", z1);
 		SpfManager.nbt.setInteger(name + "x2", x2);
 		SpfManager.nbt.setInteger(name + "z2", z2);
 		SpfManager.nbt.setInteger(name + "y", y);
 		SpfManager.nbt.setInteger(name + "BlockID",BlockID);
 	//	SpfManager.nbt.setString(name,name);
 		par.sendChatToPlayer("Sucessfully create the area <" + name + ">");
 		return true;
 	}
 	
 	public boolean restoreArea(ICommandSender par,String name){
 		for(int i=0;i<areaCount;i++){
 			if(name == arr[i].sAreaName){
 				SetSpleefBlock(CommandBase.getCommandSenderAsPlayer(par).worldObj, arr[i].x1,arr[i].z1, arr[i].x2,arr[i].z2, arr[i].y, arr[i].BlockID);
 				par.sendChatToPlayer("Area sucessfully restored. ");
 			}
 		}
 		par.sendChatToPlayer("Area not found, it has not been restored.");
 		return false; //par1 AreaID
 	}
 
 	private void SetSpleefBlock(World world,int x1,int z1,int x2,int z2,int y,int blockID) {
 		int bx; 
 		int sx;
 		int bz;
 		int sz;
 			
 		bx = (x1>x2) ? x1 : x2;
 		sx = (x1>x2) ? x2 : x1; 
 			
 		bz = (z1 > z2) ? z1 : z2;
 		sz = z1 = (z1 > z2) ? z2 : z1;
 
 		for (int i = sx; i < bx; i++)
 			for (int j = sz; j < bz; j++)
 				world.setBlock(i, y, j, blockID);
 		return;
 	}
 	
 	public boolean deleteArea(String name , ICommandSender par){
		/*		
		SpfManager.nbt.setInteger(name + "x1" , 0 );
		SpfManager.nbt.setInteger(name + "z1", z1);
 		SpfManager.nbt.setInteger(name + "x2", x2);
		SpfManager.nbt.setInteger(name + "z2", z2);
		SpfManager.nbt.setInteger(name + "y", y);
		*/
 		for(int i=0;i<areaCount;i++)
 		{
 			if(arr[i].sAreaName == name){
 				arr[i]=arr[areaCount];
 				par.sendChatToPlayer("Area " + name + " sucessfully deleted.");
 				return true;
 			}
 		}
 		par.sendChatToPlayer("Not found,area was not deleted.");
 		return false;
 	}
 	public void showAreaInformation(EntityPlayer par,String name){
 		for(int i=0;i<areaCount;i++){
 			if(arr[i].sAreaName == name){
 				par.sendChatToPlayer("-----------------Area Information-------------");
 				par.sendChatToPlayer("Area Name : " + name);
 				par.sendChatToPlayer("Area range : " + arr[i].x1 + " " + arr[i].z1);
 				par.sendChatToPlayer("      To   : " + arr[i].x2 + " " + arr[i].z2);
 				par.sendChatToPlayer("----------------------------------------------");
 				return;
 			}
 		}
 		par.sendChatToPlayer("No required area found.");
 	}
 	public void showAreaInformation(EntityPlayer par){
 		for(int i=0;i<areaCount;i++){
 			par.sendChatToPlayer("-----------------Area Information-------------");
 			par.sendChatToPlayer("Area Name : " + arr[i].sAreaName);
 			par.sendChatToPlayer("Area range : " + arr[i].x1 + " " + arr[i].z1);
 			par.sendChatToPlayer("      To   : " + arr[i].x2 + " " + arr[i].z2);
 			par.sendChatToPlayer("----------------------------------------------");
 		}
 	}
 }
