 package coed.plugin.mocksfordebug;
 
 import coed.base.comm.ICoedCollaborator;
 import coed.base.comm.ICoedCommunicator;
 import coed.base.comm.ICoedVersioner;
 import coed.base.comm.ICollabStateObserver;
 import coed.base.data.CoedObject;
 import coed.base.data.ICoedObject;
 import coed.base.data.ICollabObject;
 import coed.base.data.IVersionedObject;
 import coed.base.data.exceptions.InvalidConfigFileException;
 import coed.base.data.exceptions.UnknownVersionerTypeException;
 
 public class MockCoedCollaborator implements coed.base.comm.ICoedCommunicator {
 
 	public MockCoedCollaborator() throws InvalidConfigFileException, UnknownVersionerTypeException {
 		
 	}
 	
 	@Override
 	public ICoedObject addObject(String path) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public ICoedObject getObject(String path) {
 		return new MockCoedObject(path);
 	}
 
 	@Override
 	public String[] getProjectList() {
 		// TODO Auto-generated method stub
 		String[] x= {"aaa","bbb"};
 		return x;
 	}
 
 	@Override
 	public String getVersionerType() {
 		// TODO Auto-generated method stub
 		return ICoedVersioner.STATIC;
 	}
 
 	@Override
 	public String getType() {
 		// TODO Auto-generated method stub
 		return ICoedCommunicator.STATIC;
 	}
 
 	@Override
 	public IVersionedObject makeVersionedObject(ICoedObject obj) {
 		// TODO Auto-generated method stub
		return new MockCoedObject(obj.getPath());
 	}
 
 	@Override
 	public void addStateListener(ICollabStateObserver stateObserver) {
 		// TODO Auto-generated method stub
 		
 	}
 
 	@Override
 	public String getState() {
 		// TODO Auto-generated method stub
 		return ICoedCollaborator.STATUS_CONNECTED;
 	}
 
 	@Override
 	public ICollabObject makeCollabObject(ICoedObject obj) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public void removeStateListener(ICollabStateObserver stateObserver) {
 		// TODO Auto-generated method stub
 		
 	}
 
 }
