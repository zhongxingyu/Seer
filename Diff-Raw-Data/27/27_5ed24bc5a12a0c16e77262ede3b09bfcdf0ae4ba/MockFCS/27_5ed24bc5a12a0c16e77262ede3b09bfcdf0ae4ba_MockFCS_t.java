 package factory.test.mock;
 
import factory.BinConfig;
import factory.Kit;
 import factory.interfaces.FCS;
 
 public class MockFCS extends MockAgent implements FCS {
 
 	public MockFCS(String name) {
 		super(name);
 		// TODO Auto-generated constructor stub
 	}

	@Override
	public void msgInitialize(BinConfig binConfig) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgProduceKit(int quantity, String name) {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void kitIsFinished() {
		// TODO Auto-generated method stub
		
	}

	@Override
	public void msgKitIsExported(Kit kit) {
		// TODO Auto-generated method stub
		
	}
 	
 	
 
 }
