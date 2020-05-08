 package coed.versioning.client;
 
 import coed.base.common.ICoedVersioner;
 //import coed.collab.client.*;
 import coed.base.data.CoedFile;
 import coed.base.data.CoedProject;
 
 public class NullVersioner implements ICoedVersioner{
 
 	@Override
 	public boolean checkoutFile(CoedFile file) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean checkoutFiles(CoedFile[] files) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean checkoutProject(CoedProject project) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean commitFile(CoedFile file) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean commitFiles(CoedFile[] files) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public boolean commitProject(CoedProject project) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
	public CoedFile getFileInfo() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
 	public CoedProject getProjectInfo(String name) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String getProjectList() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String getState() {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public String getType() {
 		return null;
 	}
 
 }
