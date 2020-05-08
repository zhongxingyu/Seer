 package com.zarcode.data.model;
 
 import java.util.logging.Logger;
 
 import javax.xml.bind.annotation.XmlRootElement;
 
 import com.zarcode.common.ApplicationProps;
import com.zarcode.data.resources.Lake;
 import com.zarcode.platform.model.AppPropDO;
 import com.zarcode.security.BlockTea;
 
 @XmlRootElement(name = "SecurityToken") 
 public class SecurityTokenDO {
 	
 	private Logger logger = Logger.getLogger(SecurityTokenDO.class.getName());
 
 	private String nickname = null;
 	
 	private String logoutUrl = null;
 
 	private String emailAddr = null;
 	
 	private String deviceId = null;
 	
 	private String llId = null;
 	
 	private String userId = null;
 	
 	private String authDomain = null;
 	
 	private String picasaUser = null;
 	
 	private String picasaPassword = null;
 	
 	private String fbKey = null;
 	
 	private String fbSecret = null;
 	
 	private String serverSecret = null;
 	
 	private int status = 0;
 	
 	private String errorMsg = null;
 	
 	public SecurityTokenDO() {
 		AppPropDO p1 = ApplicationProps.getInstance().getProp("SERVER_TO_CLIENT_SECRET");
 		AppPropDO p2 = ApplicationProps.getInstance().getProp("CLIENT_TO_SERVER_SECRET");
 		BlockTea.BIG_ENDIAN = false;
 		String cipherText = BlockTea.encrypt(p2.getStringValue(), p1.getStringValue());
 		this.serverSecret = cipherText;
 	}
 	
 	public SecurityTokenDO(int status, String errorMsg) {
 		this.status = status;
 		this.errorMsg = errorMsg;
 	}
 	
 	public void postCreation() {
 	}
 	
 	public String getFbKey() {
 		return fbKey;
 	}
 	
 	public void setFbKey(String fbKey) {
 		this.fbKey = fbKey;
 	}
 	
 	public void encryptThenSetFbKey(String key) {
 		AppPropDO prop = ApplicationProps.getInstance().getProp("SERVER_TO_CLIENT_SECRET");
 		BlockTea.BIG_ENDIAN = false;
 		String cipherText = BlockTea.encrypt(key, prop.getStringValue());
 		this.fbKey = cipherText;
 	}
 	
 	public String getFbSecret() {
 		return fbSecret;
 	}
 	
 	public void setFbSecret(String fbSecret) {
 		this.fbSecret = fbSecret;
 	}
 	
 	public void encryptThenSetFbSecret(String secret) {
 		AppPropDO prop = ApplicationProps.getInstance().getProp("SERVER_TO_CLIENT_SECRET");
 		BlockTea.BIG_ENDIAN = false;
 		String cipherText = BlockTea.encrypt(secret, prop.getStringValue());
 		this.fbSecret = cipherText;
 	}
 	
 	public String getPicasaUser() {
 		return picasaUser;
 	}
 	
 	public void setPicasaUser(String picasaUser) {
 		this.picasaUser = picasaUser;
 	}
 	
 	public void encryptThenSetPicasaUser(String pUser) {
 		AppPropDO prop = ApplicationProps.getInstance().getProp("SERVER_TO_CLIENT_SECRET");
 		BlockTea.BIG_ENDIAN = false;
 		String cipherText = BlockTea.encrypt(pUser, prop.getStringValue());
 		this.picasaUser = cipherText;
 	}
 	
 	
 	public String getPicasaPassword() {
 		return picasaPassword;
 	}
 	
 	public void setPicasaPassword(String picasaPassword) {
 		this.picasaPassword = picasaPassword;
 	}
 	
 	public void encryptThenSetPicasaPassword(String pw) {
 		AppPropDO prop = ApplicationProps.getInstance().getProp("SERVER_TO_CLIENT_SECRET");
 		BlockTea.BIG_ENDIAN = false;
 		String cipherText = BlockTea.encrypt(pw, prop.getStringValue());
 		this.picasaPassword = cipherText;
 	}
 	
 	public String getNickname() {
 		return nickname;
 	}
 	
 	public void setNickname(String nickname) {
 		this.nickname = nickname;
 	}
 	
 	public String getAuthDomain() {
 		return authDomain;
 	}
 	
 	public void setAuthDomain(String authDomain) {
 		this.authDomain = authDomain;
 	}
 	
 	public String getEmailAddr() {
 		return emailAddr;
 	}
 	
 	public void setEmailAddr(String emailAddr) {
 		this.emailAddr = emailAddr;
 	}
 	
 	public void encryptThenSetEmailAddr(String emailAddr) {
 		AppPropDO prop = ApplicationProps.getInstance().getProp("SERVER_TO_CLIENT_SECRET");
 		BlockTea.BIG_ENDIAN = false;
 		String cipherText = BlockTea.encrypt(emailAddr, prop.getStringValue());
 		this.emailAddr = cipherText;
 	}
 	
 	public String getLogoutUrl() {
 		return logoutUrl;
 	}
 	
 	public void setLogoutUrl(String logoutUrl) {
 		this.logoutUrl = logoutUrl;
 	}
 	
 	public void encryptThenSetLogoutUrl(String logoutUrl) {
 		AppPropDO prop = ApplicationProps.getInstance().getProp("SERVER_TO_CLIENT_SECRET");
 		BlockTea.BIG_ENDIAN = false;
 		String cipherText = BlockTea.encrypt(logoutUrl, prop.getStringValue());
 		this.logoutUrl = cipherText;
 	}
 	
 	public String getLlId() {
 		return llId;
 	}
 	
 	public void setLlId(String llId) {
 		this.llId = llId;
 	}
 	
 	public void encryptThenSetLLId(String llId) {
 		AppPropDO prop = ApplicationProps.getInstance().getProp("SERVER_TO_CLIENT_SECRET");
 		BlockTea.BIG_ENDIAN = false;
 		String cipherText = BlockTea.encrypt(llId, prop.getStringValue());
 		this.llId = cipherText;
 		AppPropDO p2 = ApplicationProps.getInstance().getProp("CLIENT_TO_SERVER_SECRET");
 		String c2s = BlockTea.encrypt(llId, p2.getStringValue());
 		logger.info("C-to-S encrypted ---> " + c2s);
 	}
 	
 	public String getUserId() {
 		return userId;
 	}
 	
 	public void setServer(String serverSecret) {
 		this.serverSecret = serverSecret;
 	}
 	
 	public String getServerSecret() {
 		return serverSecret;
 	}
 	
 	public void setUserId(String userId) {
 		this.userId = userId;
 	}
 	
 	public void encryptThenSetUserId(String userId) {
 		AppPropDO prop = ApplicationProps.getInstance().getProp("SERVER_TO_CLIENT_SECRET");
 		BlockTea.BIG_ENDIAN = false;
 		String cipherText = BlockTea.encrypt(userId, prop.getStringValue());
 		this.userId = cipherText;
 	}
 	
 	public int getStatus() {
 		return status;
 	}
 	
 	public void setStatus(int s) {
 		this.status = s;
 	}
 	
 	public String getErrorMsg() {
 		return errorMsg;
 	}
 	
 	public void setErrorMsg(String errorMsg) {
 		this.errorMsg = errorMsg;
 	}
 	
 }
