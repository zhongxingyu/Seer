 package edu.kgu.aeon.logic;
 
 import edu.kgu.QrCode.QrcodeEncode;
 import edu.kgu.aeon.access.userInfoAccess;
 import edu.kgu.aeon.bean.*;
 import edu.kgu.log.LogLogger;
 import edu.kgu.util.StringProcess;
 
 public class loginLogic extends BaseLogic {
 	private userInfoAccess access = new userInfoAccess();
 	
 	private loginFormBean bean;
 	private registerConfirmFormBean registerConfirmBean = new registerConfirmFormBean();
 	
 	private userInfoBean resultBean;
 	
 	public registerConfirmFormBean getRegisterConfirmBean() {
 		return registerConfirmBean;
 	}
 	
 	@Override
 	public boolean CheckFormBean() {
 		// ユーザ名入力チェック
 		if (bean.getUserName().length() <= 0) {
 			this.messagebean.setErrorMsg("ユーザ名入力してください");
 			return false;
 		}
 		
 		// パスワード入力チェック
 		if (bean.getPassword().length() <= 0) {
 			this.messagebean.setErrorMsg("パスワード入力してください");
 			return false;
 		}
 		
 		// パスワード正しいチェック
 		resultBean = access.getUserInfoByUserName(bean.getUserName());
 		if (!bean.getPassword().equals(resultBean.getPassword())) {
 			this.messagebean.setErrorMsg("ユーザ名とパスワードが不一致です。");
 			return false;
 		}
 		
 
 		return true;
 	}
 	
 	public boolean execute(loginFormBean value) {
 		boolean rtn = false;
 		bean = value;
 		
 		// form value check
 		if (CheckFormBean()) {
			
 			// create LoginQrImage
 			registerLogic.createQrCode(resultBean.getUserID());
	
 			// set return value
 			String path = "QRtmp/";
 			// userID
 			this.registerConfirmBean.setUserID(resultBean.getUserID());
 			this.registerConfirmBean.setUserName(resultBean.getUserName());
 			this.registerConfirmBean.setFirstName(resultBean.getFirstname());
 			this.registerConfirmBean.setQrImage(path + resultBean.getUserID() + ".png");
 
 			rtn = true;
 		}
 		
 		return rtn;
 	}
 }
