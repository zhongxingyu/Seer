 package agent.test.mock;
 
 import Networking.Request;
 import Utils.Location;
 import agent.Agent;
 import agent.FeederAgent;
 import DeviceGraphics.BinGraphics;
 import DeviceGraphics.DeviceGraphics;
 import GraphicsInterfaces.FeederGraphics;
 
 public class MockFeederGraphics extends Agent implements DeviceGraphics, FeederGraphics {
 
 	FeederAgent feeder;
 	
 	@Override
 	public void receiveBin(BinGraphics bin) {
 		// TODO Auto-generated method stub
 		feeder.msgReceiveBinDone(bin.getBin());
 	}
 
 	@Override
 	public void purgeBin(BinGraphics bin) {
 		// TODO Auto-generated method stub
 		feeder.msgPurgeBinDone(bin.getBin());
 	}
 
 	@Override
 	public void flipDiverter() {
 		// TODO Auto-generated method stub
 		feeder.msgFlipDiverterDone();
 	}
 
 	@Override
 	public Location getLocation() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void receiveData(Request req) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public boolean pickAndExecuteAnAction() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public void setGraphicalRepresentation(DeviceGraphics dg) {
 		// TODO Auto-generated method stub
 		
 	}
 	public void setFeederAgent(FeederAgent feeder) {
 		this.feeder = feeder;
 	}
 	
 }
