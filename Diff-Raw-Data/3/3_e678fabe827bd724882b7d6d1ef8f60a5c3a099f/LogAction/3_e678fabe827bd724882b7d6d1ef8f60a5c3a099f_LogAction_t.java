 package com.tp.action.log;
 
 import static com.tp.utils.Constants.ENCODE_UTF_8;
 import static com.tp.utils.Constants.PARA_APP_NAME;
 import static com.tp.utils.Constants.PARA_AUTO_SWITCH;
 import static com.tp.utils.Constants.PARA_BCID;
 import static com.tp.utils.Constants.PARA_CLIENT_TYPE;
 import static com.tp.utils.Constants.PARA_CLIENT_VERSION;
 import static com.tp.utils.Constants.PARA_CONTENT_VERSION;
 import static com.tp.utils.Constants.PARA_DOWNLOAD_METHOD;
 import static com.tp.utils.Constants.PARA_DO_TYPE;
 import static com.tp.utils.Constants.PARA_FROM;
 import static com.tp.utils.Constants.PARA_FROM_MARKET;
 import static com.tp.utils.Constants.PARA_IMEI;
 import static com.tp.utils.Constants.PARA_IMSI;
 import static com.tp.utils.Constants.PARA_LANGUAGE;
 import static com.tp.utils.Constants.PARA_MACHINE_MODEL;
 import static com.tp.utils.Constants.PARA_NET_ENVIRONMENT;
 import static com.tp.utils.Constants.PARA_OPERATORS;
 import static com.tp.utils.Constants.PARA_RESOLUTION;
 import static com.tp.utils.Constants.PARA_SAFETYLOCK;
 import static com.tp.utils.Constants.PARA_STORE_TYPE;
 import static com.tp.utils.Constants.PARA_URL;
 
 import java.net.URLDecoder;
 import java.util.Date;
 
 import com.tp.utils.DateUtil;
 import org.apache.commons.lang3.StringUtils;
 import org.apache.struts2.convention.annotation.Namespace;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.opensymphony.xwork2.ActionSupport;
 import com.tp.dto.LogDTO;
 import com.tp.entity.ClientFile;
 import com.tp.entity.log.LogForContent;
 import com.tp.entity.log.LogForPoll;
 import com.tp.entity.log.LogForRedirect;
 import com.tp.entity.log.LogFromClient;
 import com.tp.entity.log.LogInHome;
 import com.tp.mapper.BeanMapper;
 import com.tp.service.ClientFileManager;
 import com.tp.service.LogService;
 import com.tp.utils.Constants;
 import com.tp.utils.Struts2Utils;
 
 @Namespace("/log")
 public class LogAction extends ActionSupport {
 
 	private static final long serialVersionUID = 1L;
 
 	private LogService logService;
 	private ClientFileManager clientFileManager;
 
 	@Override
 	public String execute() throws Exception {
 
 		return save();
 	}
 
 	public String save() throws Exception {
 
 		LogDTO logDTO = getLog();
 		LogFromClient entity = BeanMapper.map(logDTO, LogFromClient.class);
 		entity.setCreateTime(DateUtil.convert(new Date()));
 		logService.saveLogFromClient(entity);
 
 		String clientVersion = entity.getClientVersion();
 		ClientFile client = clientFileManager.getByVersion(clientVersion);
 		if (client == null || clientVersion.equals("2.6.0")) { //兼容客户端无法升级的bug
 			Struts2Utils.renderText("");
 			return null;
 		}
 
 		String version = clientFileManager.getNewestClient(clientVersion, client.getDtype());
         ClientFile clientFile= clientFileManager.getByVersion(version);
         if(clientFile!=null&&StringUtils.isNotBlank(clientFile.getDescription())){
             version=version+"{"+clientFile.getDescription()+"}";
         }
 		Struts2Utils.renderText(version);
 		return null;
 	}
 
 	public String saveDownload() throws Exception {
 
 		String queryStr = Struts2Utils.getParameter(Constants.QUERY_STRING);
 		if (StringUtils.isNotBlank(queryStr) && queryStr.contains("UMS"))
 			queryStr = StringUtils.substringAfterLast(queryStr, "UMS/");
 		String clientStr = Struts2Utils.getParameter("cs");
 		LogInHome log = splitClientStr(clientStr);
 		int index = StringUtils.indexOf(queryStr, "&inputPath");
 		int ques = StringUtils.indexOf(queryStr, "?");
 		if (ques != -1) {
 			log.setRequestMethod(StringUtils.substring(queryStr, 0, ques));
 		} else {
 			log.setRequestMethod("d_download");
 		}
 		if (index != -1) {
 			queryStr = StringUtils.substring(queryStr, ques + 1, index);
 			String[] qs = StringUtils.split(queryStr, "=");
 			log.setRequestParams(qs[0] + ":" + qs[1]);
 		} else {
 			log.setRequestParams(queryStr);
 		}
 		log.setCreateTime(DateUtil.convert(new Date()));
        if(log.getAppName()==null){
            log.setAppName("");
        }
 		logService.saveLogInHome(log);
 		Struts2Utils.renderText("success");
 		return null;
 	}
 
 	private LogInHome splitClientStr(String requetParam) {
 		LogInHome log = new LogInHome();
 		if (StringUtils.isNotBlank(requetParam)) {
 
 			String[] params = StringUtils.split(requetParam, "&");
 
 			for (int i = 0; i < params.length; i++) {
 				String param = params[i];
 				String[] keyValue = StringUtils.split(param, "=");
 				if (keyValue.length > 1) {
 					String key = keyValue[0];
 					String value = keyValue[1];
 					if (key.equals(Constants.PARA_CLIENT_VERSION)) {
 						log.setClientVersion(value);
 					}
 					if (key.equals(Constants.PARA_DOWNLOAD_METHOD)) {
 						log.setDownType(value);
 					}
 					if (key.equals(Constants.PARA_FROM_MARKET)) {
 						log.setFromMarket(value);
 					}
 					if (key.equals(Constants.PARA_IMEI)) {
 						log.setImei(value);
 					}
 					if (key.equals(Constants.PARA_IMSI)) {
 						log.setImsi(value);
 					}
 					if (key.equals(Constants.PARA_LANGUAGE)) {
 						log.setLanguage(value);
 					}
 					if (key.equals(Constants.PARA_RESOLUTION)) {
 						log.setResolution(value);
 					}
 					if (key.equals(Constants.PARA_STORE_TYPE)) {
 						log.setStoreType(value);
 					}
 				}
 			}
 		}
 		return log;
 
 	}
 
 	@Deprecated
 	public String client() throws Exception {
 
 		return null;
 	}
 
 	public String content() throws Exception {
 		LogDTO logDTO = getLog();
 		LogForContent entity = BeanMapper.map(logDTO, LogForContent.class);
 		entity.setCreateTime(DateUtil.convert(new Date()));
 		logService.saveLogContent(entity);
 		return null;
 	}
 
 	public String redirect() throws Exception {
 		LogDTO log = getLog();
 		String url = log.getUrl();
 		String app = log.getAppName();
 		if (StringUtils.isNotBlank(app)) {
 			app = URLDecoder.decode(app, ENCODE_UTF_8);
 		}
 		LogForRedirect redire = new LogForRedirect();
 		redire.setAppName(app);
 		redire.setLinkAddr(url);
 		logService.saveRedirect(redire);
 		Struts2Utils.getResponse().sendRedirect(url);
 		return null;
 	}
 
 	public String poll() throws Exception {
 
 		LogDTO logDTO = getLog();
 
 		LogForPoll entity = BeanMapper.map(logDTO, LogForPoll.class);
 		logService.savePoll(entity);
 		Struts2Utils.renderText("success");
 		return null;
 	}
 
 	private LogDTO getLog() {
 		String imei = Struts2Utils.getParameter(PARA_IMEI);
 		String imsi = Struts2Utils.getParameter(PARA_IMSI);
 		String storeType = Struts2Utils.getParameter(PARA_STORE_TYPE);
 		String ct = Struts2Utils.getParameter(PARA_CLIENT_TYPE);
 		String downType = Struts2Utils.getParameter(PARA_DOWNLOAD_METHOD);
 		String language = Struts2Utils.getParameter(PARA_LANGUAGE);
 		String clientVersion = Struts2Utils.getParameter(PARA_CLIENT_VERSION);
 		String contentVersion = Struts2Utils.getParameter(PARA_CONTENT_VERSION);
 		String resolution = Struts2Utils.getParameter(PARA_RESOLUTION);
 		String fromMarket = Struts2Utils.getParameter(PARA_FROM_MARKET);
 		String autoSwitch = Struts2Utils.getParameter(PARA_AUTO_SWITCH);
 		String safetyLock = Struts2Utils.getParameter(PARA_SAFETYLOCK);
 		String netEnv = Struts2Utils.getParameter(PARA_NET_ENVIRONMENT);
 		String op = Struts2Utils.getParameter(PARA_OPERATORS);
 		String model = Struts2Utils.getParameter(PARA_MACHINE_MODEL);
 		String doType = Struts2Utils.getParameter(PARA_DO_TYPE);
 		String bcid = Struts2Utils.getParameter(PARA_BCID);
 		String app = Struts2Utils.getParameter(PARA_APP_NAME);
 		String url = Struts2Utils.getParameter(PARA_URL);
 		String from = Struts2Utils.getParameter(PARA_FROM);
 
 		LogDTO dto = new LogDTO();
 		dto.setAppName(app);
 		dto.setAutoSwitch(autoSwitch);
 		dto.setBcid(bcid);
 		dto.setClientVersion(clientVersion);
 		dto.setContentVersion(contentVersion);
 		dto.setClientType(ct);
 		dto.setDownType(downType);
 		dto.setDoType(doType);
 		dto.setFromMarket(fromMarket);
 		dto.setFrom(from);
 		dto.setImei(imei);
 		dto.setImsi(imsi);
 		dto.setLanguage(language);
 		dto.setModel(model);
 		dto.setNetEnv(netEnv);
 		dto.setOperators(op);
 		dto.setResolution(resolution);
 		dto.setSafetyLock(safetyLock);
 		dto.setStoreType(storeType);
 		dto.setUrl(url);
 		return dto;
 	}
 
 	@Autowired
 	public void setLogService(LogService logService) {
 		this.logService = logService;
 	}
 
 	@Autowired
 	public void setClientFileManager(ClientFileManager clientFileManager) {
 		this.clientFileManager = clientFileManager;
 	}
 }
