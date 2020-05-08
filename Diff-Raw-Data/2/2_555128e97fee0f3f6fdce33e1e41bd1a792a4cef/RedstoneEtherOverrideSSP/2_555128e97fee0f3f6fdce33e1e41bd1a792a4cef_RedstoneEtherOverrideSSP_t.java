 package net.minecraft.src.wifi;
 
 import net.minecraft.src.ModLoader;
 import net.minecraft.src.World;
 
 public class RedstoneEtherOverrideSSP implements RedstoneEtherOverride {
 	@Override
 	public boolean beforeAddTransmitter(World world, int i, int j, int k,
 			String freq) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void afterAddTransmitter(World world, int i, int j, int k,
 			String freq) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean beforeRemTransmitter(World world, int i, int j, int k,
 			String freq) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void afterRemTransmitter(World world, int i, int j, int k,
 			String freq) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean beforeSetTransmitterState(World world, int i, int j, int k,
 			String freq, boolean state) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void afterSetTransmitterState(World world, int i, int j, int k,
 			String freq, boolean state) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean beforeAddReceiver(World world, int i, int j, int k,
 			String freq) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void afterAddReceiver(World world, int i, int j, int k, String freq) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean beforeRemReceiver(World world, int i, int j, int k,
 			String freq) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void afterRemReceiver(World world, int i, int j, int k, String freq) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean beforeGetFreqState(World world, String freq) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean afterGetFreqState(World world, String freq,
 			boolean returnState) {
 		// TODO Auto-generated method stub
		return returnState;
 	}
 
 	@Override
 	public boolean beforeIsLoaded(World world, int i, int j, int k) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean afterIsLoaded(World world, int i, int j, int k, boolean returnState) {
 		LoggerRedstoneWireless.getInstance("RedstoneEtherOverrideSSP").write("isLoaded(world, "+i+", "+j+", "+k+")", LoggerRedstoneWireless.LogLevel.DEBUG);
 		if ( ModLoader.getMinecraftInstance() == null || ModLoader.getMinecraftInstance().thePlayer == null )
 			return false;
 
 		int[] a = {i,j,k};
 		int[] b = {
 				(int) ModLoader.getMinecraftInstance().thePlayer.posX,
 				(int) ModLoader.getMinecraftInstance().thePlayer.posY,
 				(int) ModLoader.getMinecraftInstance().thePlayer.posZ
 		};
 		if ( RedstoneEther.pythagoras(a, b) < 1 ) // Is player
 			return true;
 
 		return returnState;
 	}
 }
