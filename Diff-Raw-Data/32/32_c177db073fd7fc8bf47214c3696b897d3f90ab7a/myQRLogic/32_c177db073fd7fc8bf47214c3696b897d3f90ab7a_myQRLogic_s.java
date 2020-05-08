 package edu.kgu.aeon.logic;
 
 import edu.kgu.aeon.access.userInfoAccess;
 import edu.kgu.aeon.bean.registerConfirmFormBean;
 import edu.kgu.aeon.bean.userInfoBean;
 
 public class myQRLogic extends BaseLogic {
 
 	private userInfoAccess access = new userInfoAccess();
 	@Override
 	public boolean CheckFormBean() {
 		// TODO Auto-generated method stub
 		return false;
 	}
 	
 	public registerConfirmFormBean execute(String userName) {
 		registerConfirmFormBean rtn = new registerConfirmFormBean();
 		userInfoBean bean = access.getUserInfoByUserName(userName);
 		
 		// set return value
 		String path = "QRtmp/";
 		
 		rtn.setFirstName(bean.getFirstname());
 		rtn.setQrImage(path + bean.getUserID() + ".png");
 		rtn.setUserID(bean.getUserID());
 		rtn.setUserName(bean.getUserName());
 		
 		return rtn;
 	}
 
 }
