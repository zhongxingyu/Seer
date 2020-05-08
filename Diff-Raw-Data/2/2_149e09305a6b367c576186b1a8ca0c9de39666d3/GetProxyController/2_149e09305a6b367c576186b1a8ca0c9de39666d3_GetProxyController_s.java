 package portal.login.controller;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.security.cert.CertificateEncodingException;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Properties;
 
 import javax.portlet.ActionRequest;
 import javax.portlet.ActionResponse;
 import javax.portlet.PortletConfig;
 
 import org.apache.log4j.Logger;
 import org.globus.gsi.GlobusCredential;
 import org.globus.gsi.gssapi.GlobusGSSCredentialImpl;
 import org.globus.myproxy.MyProxy;
 import org.globus.myproxy.MyProxyException;
 import org.globus.util.Util;
 import org.ietf.jgss.GSSCredential;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.portlet.bind.annotation.ActionMapping;
 
 import portal.login.util.LoadProperties;
 
 /*import portal.login.domain.Certificate;
 import portal.login.domain.UserInfo;
 import portal.login.domain.Vo;
 import portal.login.services.CertificateService;
 import portal.login.services.UserInfoService;
 import portal.login.services.UserToVoService;
 import portal.login.services.VoService;*/
 
 import it.italiangrid.portal.dbapi.domain.Certificate;
 import it.italiangrid.portal.dbapi.domain.Notify;
 import it.italiangrid.portal.dbapi.domain.UserInfo;
 import it.italiangrid.portal.dbapi.domain.Vo;
 import it.italiangrid.portal.dbapi.services.CertificateService;
 import it.italiangrid.portal.dbapi.services.NotifyService;
 import it.italiangrid.portal.dbapi.services.UserInfoService;
 import it.italiangrid.portal.dbapi.services.UserToVoService;
 import it.italiangrid.portal.dbapi.services.VoService;
 
 import com.liferay.portal.kernel.servlet.SessionErrors;
 import com.liferay.portal.kernel.servlet.SessionMessages;
 import com.liferay.portal.kernel.util.JavaConstants;
 import com.liferay.portal.kernel.util.WebKeys;
 import com.liferay.portal.model.User;
 
 /**
  * This class is used for store proxy to the disk and destroy proxy from the
  * disk
  * 
  * @author dmichelotto
  * 
  */
 
 @Controller(value = "getProxyController")
 @RequestMapping(value = "VIEW")
 public class GetProxyController {
 
 	/**
 	 * Logger of the class DownloadCertificateController. TODO to substitute it
 	 * with slf4j.
 	 */
 	private static final Logger log = Logger
 			.getLogger(GetProxyController.class);
 
 	/**
 	 * Attribute for access to the PortalUser database.
 	 */
 	@Autowired
 	private UserInfoService userInfoService;
 	
 	/**
 	 * Attribute for access to the PortalUser database.
 	 */
 	@Autowired
 	private NotifyService notifyService;
 
 	/**
 	 * Attribute for access to the PortalUser database.
 	 */
 	@Autowired
 	private VoService voService;
 
 	/**
 	 * Attribute for access to the PortalUser database.
 	 */
 	@Autowired
 	private UserToVoService userToVoService;
 	
 	/**
 	 * Attribute for access to the PortalUser database.
 	 */
 	@Autowired
 	private CertificateService certificateService;
 
 	/**
 	 * Writes the specified globus credential to disk.
 	 * 
 	 * @param request
 	 *            : session parameter
 	 * @param response
 	 *            : session parameter
 	 * @throws IOException
 	 * @throws CertificateEncodingException
 	 */
 	@ActionMapping(params = "myaction=getProxy")
 	public void getProxy(ActionRequest request, ActionResponse response)
 			throws IOException, CertificateEncodingException {
 		log.debug("***** Pronto per scaricare il proxy *****");
 		
 		PortletConfig portletConfig = (PortletConfig)request.getAttribute(JavaConstants.JAVAX_PORTLET_CONFIG);
 		SessionMessages.add(request, portletConfig.getPortletName() + SessionMessages.KEY_SUFFIX_HIDE_DEFAULT_ERROR_MESSAGE);
 		
 		String contextPath = GetProxyController.class.getClassLoader().getResource("").getPath();
 		
 		log.debug("dove sono:" + contextPath);
 		
 		FileInputStream inStream =
 	    new FileInputStream(contextPath + "/content/MyProxy.properties");
 
 		Properties prop = new Properties();
 		prop.load(inStream);
 		inStream.close();
 		
 		String host = prop.getProperty("myproxy.storage");
 		String hostGrid = prop.getProperty("myproxy.grid");
 		String valid = prop.getProperty("valid");
 		
 		log.debug("Host = " + host);
 		log.debug("Valid = " + valid);
 
 		MyProxy mp = new MyProxy(host, 7512);
 		User user = (User) request.getAttribute(WebKeys.USER);
 
 		String dir = System.getProperty("java.io.tmpdir");
 		log.error("Directory = " + dir);
 
 		File location = new File(dir + "/users/" + user.getUserId() + "/");
 		if (!location.exists()) {
 			location.mkdirs();
 		}
 
 		OutputStream out = null;
 
 		int vo = Integer.parseInt(request.getParameter("vosId"));
 		String pass = (String) request.getParameter("proxyPass");
 		String role = (String) request.getParameter("fqan");
 		
 		if(role==null)
 			role="norole";
 		log.debug(" ");
 		log.debug("################################");
 		log.debug("#");
 		log.debug("# vo = " + vo);
 		log.debug("# role = " + role);
 		log.debug("#");
 		log.debug("################################");
 		log.debug(" ");
 
 		String username = user.getScreenName();
 
 		UserInfo userInfo = userInfoService.findByUsername(username);
 		
 		Notify n = notifyService.findByUserInfo(userInfo);
 		
 		if(n==null){
 			notifyService.save(new Notify(userInfo, "false"));
 			n = notifyService.findByUserInfo(userInfo);
 		}
 		
 		
 		Vo selectedVo =null;
 		if(vo == 0){
 			String tmp = userToVoService.findDefaultVo(userInfo.getUserId());
 			List<Vo> vos = userToVoService.findVoByUserId(userInfo.getUserId());
 			for (Iterator<Vo> iterator = vos.iterator(); iterator.hasNext();) {
 				Vo vo2 = (Vo) iterator.next();
 				if(tmp.equals(vo2.getVo())){
 					selectedVo = vo2;
 					break;
 				}
 			}
 		}else{
 			selectedVo = voService.findById(vo);
 		}
 		int idCert = userToVoService.findById(userInfo.getUserId(), selectedVo.getIdVo()).getCertificate().getIdCert();
 		
 		Certificate cert = certificateService.findByIdCert(idCert);
 		
 		
 
 		File proxyFile = new File(dir + "/users/" + user.getUserId()
 				+ "/x509up");
 		File proxyFileVO = new File(dir + "/users/" + user.getUserId()
 				+ "/x509up." + selectedVo.getVo());
 		
 		log.debug(" ");
 		log.debug("################################");
 		log.debug("#");
 		log.debug("# selectedVo = " + selectedVo.getVo());
 		log.debug("#");
 		log.debug("################################");
 		log.debug(" ");
 		
 
 		try {
 
 			log.debug("certificato: " + cert.getUsernameCert() + " password: "
 					+ pass);
 			GSSCredential proxy = mp.get(cert.getUsernameCert(), pass, 608400);
 			
 			log.debug("----- All ok -----");
 			log.debug("Proxy:" + proxy.toString());
 
 			GlobusCredential globusCred = null;
 			globusCred = ((GlobusGSSCredentialImpl) proxy)
 					.getGlobusCredential();
 			log.debug("----- Passo per il istanceof GlobusGSSCredentialImpl");
 
 			log.debug("Save proxy file: " + globusCred);
 			out = new FileOutputStream(proxyFile);
 			Util.setFilePermissions(proxyFile.toString(), 600);
 			globusCred.save(out);
 			
 			/*GlobusCredential myproxyCred = null;
 			myproxyCred = ((GlobusGSSCredentialImpl) myproxy)
 					.getGlobusCredential();
 			out = new FileOutputStream(myproxyFile);
 			Util.setFilePermissions(myproxyFile.toString(), 600);
 			myproxyCred.save(out);*/
 			
 			boolean proxyinit = myMyProxyInit(proxyFile.toString(), hostGrid, cert.getSubject(), pass, request);
 			
 			//myproxyFile.delete();
 			
 			//myVomsProxyInit(proxyFile.toString(), selectedVo.getVo(), role, request);
 
 			out = new FileOutputStream(proxyFileVO);
 			Util.setFilePermissions(proxyFileVO.toString(), 600);
 			globusCred.save(out);
 			
 			if(!n.getProxyExpireTime().equals("12:00"))
 				valid=n.getProxyExpireTime();
 			
 			log.error("Now Valid is: "+valid);
 			
 			boolean vomsproxyinit = myVomsProxyInit(proxyFileVO.toString(), selectedVo.getVo(), role, valid, request);
 
 
 			FileWriter fstream = new FileWriter(dir + "/users/"
 					+ user.getUserId() + "/.creds", true);
 			BufferedWriter outcred = new BufferedWriter(fstream);
 			outcred.write(System.currentTimeMillis()
 					+ ";"+host+";" + globusCred.getTimeLeft()
 					+ "; ;#" + selectedVo.getVo() + " ;\n");
 			// Close the output stream
 			outcred.close();
 			
 			if(proxyinit&&vomsproxyinit){
 				SessionMessages.add(request, "proxy-download-success");
 				
 				log.debug("@@@@ TEST @@@@");
 				
 				log.debug("@@@@" + n.getProxyExpire());
 				log.debug("@@@@ TEST @@@@");
 				
 				LoadProperties props = new LoadProperties("checkProxy.properties");
 				if(n.getProxyExpire().equals("true")){
 					log.debug("è richiesta la notifica");
 					
 					props.putValue(n.getIdNotify()+"."+selectedVo.getVo(), proxyFileVO.toString()+";"+60+";"+userInfo.getMail()+";"+userInfo.getFirstName()+";"+valid+";"+role);
 				}else{
 					log.debug("non è richiesta la notifica");
 					props.deleteValue(n.getIdNotify()+"."+selectedVo.getVo());
 				}
 			} else {
 				SessionErrors.add(request, "proxy-download-problem");
 			}
 
 		} catch (MyProxyException e) {
 			//e.printStackTrace();
 			
 			SessionErrors.add(request, "proxy-download-problem");
 			log.error("***** errore myproxy " + e.getMessage()
 					+ " MyProxyException *****");
 			response.setRenderParameter("myaction", "idps");
 
 		} catch (IllegalArgumentException e) {
 			
 			SessionErrors.add(request, "proxy-download-problem");
 			log.error("***** errore myproxy IllegalArgumentException *****");
 			response.setRenderParameter("myaction", "idps");
 
 		} catch (FileNotFoundException e) {
 			
 			log.error("Could not write credential to file "
 					+ proxyFile.getAbsolutePath() + ": " + e.getMessage());
 			throw new IOException(e.getMessage());
 
 		} finally {
 			if (out != null) {
 				try {
 					out.close();
 				} catch (IOException e) {
 					
 					log.error("Could not write credential to file "
 							+ proxyFile.getAbsolutePath() + ": "
 							+ e.getMessage());
 					throw e;
 				}
 			}
 		}
 	}
 	
 	private boolean myMyProxyInit(String proxyFile, String host, String dn, String pass, ActionRequest request){
 		
 		String[] commands = new String[]{"/usr/bin/myproxy-init", "-n", "-d", "-s", host, "-C", proxyFile, "-y", proxyFile};
 
 		for (int i = 0; i < commands.length; i++) {
 			log.debug("cmd = "+ commands[i]);
 		}
 		
 		Process p;
 		try {
 			p = Runtime.getRuntime().exec(commands);
 		
 			InputStream stdout = p.getInputStream();
 			InputStream stderr = p.getErrorStream();
 			
 			BufferedReader output = new BufferedReader(new InputStreamReader(
 					stdout));
 			String line = null;
 
 			while ((line = output.readLine()) != null) {
				log.error("[Stdout] " + line);
 			}
 			output.close();
 			
 			boolean error = false;
 
 			BufferedReader brCleanUp = new BufferedReader(
 					new InputStreamReader(stderr));
 			while ((line = brCleanUp.readLine()) != null) {
 				
 				if(!line.contains("....")){
 					log.error("[Stderr] " + line);
 					error= true;
 				}
 			}
 			if(error)
 				SessionErrors.add(request, "myMyProxyInit-problem");
 			brCleanUp.close();
 			
 			return true;
 		
 		} catch (IOException e) {
 			// TODO Auto-generated catch block
 			
 			SessionErrors.add(request, "myMyProxyInit-exception");
 			e.printStackTrace();
 			return false;
 		}
 		
 	}
 	
 	private boolean myVomsProxyInit(String proxyFile, String voms, String role, String valid, ActionRequest request){
 		try {
 			
 			User user = (User) request.getAttribute(WebKeys.USER);
 
 			String dir = System.getProperty("java.io.tmpdir");
 			log.debug("Directory = " + dir);
 
 			String proxy = dir + "/users/" + user.getUserId() + "/x509up";
 			
 			String cmd = "voms-proxy-init -noregen -cert " + proxy + " -key " + proxy + " -out " + proxyFile + " -valid " + valid  + " -voms " + voms;
 			log.debug(cmd);
 			if(!role.equals("norole")){
 				cmd += ":" + role;
 			}
 			log.debug("cmd = " + cmd);
 			Process p = Runtime.getRuntime().exec(cmd);
 			InputStream stdout = p.getInputStream();
 			InputStream stderr = p.getErrorStream();
 
 			BufferedReader output = new BufferedReader(new InputStreamReader(
 					stdout));
 			String line = null;
 
 			while ((line = output.readLine()) != null) {
 				log.debug("[Stdout] " + line);
 			}
 			output.close();
 			
 			//boolean error = false;
 
 			BufferedReader brCleanUp = new BufferedReader(
 					new InputStreamReader(stderr));
 			while ((line = brCleanUp.readLine()) != null) {
 				//error= true;
 				if(!line.contains("....")){
 					log.error("[Stderr] " + line);
 				}
 			}
 			/*if(error)
 				SessionErrors.add(request, "voms-proxy-init-problem");*/
 			brCleanUp.close();
 			
 			return true;
 
 		} catch (IOException e) {
 			
 			SessionErrors.add(request, "voms-proxy-init-exception");
 			e.printStackTrace();
 			return false;
 		}
 	}
 
 	/**
 	 * Destroy the specified globus credential to disk.
 	 * 
 	 * @throws CredentialException
 	 */
 	@ActionMapping(params = "myaction=destroyProxy")
 	public void destroyProxy(ActionRequest request){
 
 		User user = (User) request.getAttribute(WebKeys.USER);
 
 		String dir = System.getProperty("java.io.tmpdir");
 		log.debug("Directory = " + dir);
 
 		UserInfo userInfo = userInfoService
 				.findByUsername(user.getScreenName());
 		File credFile = new File(dir + "/users/" + user.getUserId() + "/creds");
 		File proxyVoFile = null;
 		File proxyFile = new File(dir + "/users/" + user.getUserId()
 				+ "/x509up");
 		proxyFile.delete();
 		credFile.delete();
 
 		List<Vo> vos = userToVoService.findVoByUserId(userInfo.getUserId());
 		for (Iterator<Vo> iterator = vos.iterator(); iterator.hasNext();) {
 			Vo vo = (Vo) iterator.next();
 			proxyVoFile = new File(dir + "/users/" + user.getUserId()
 					+ "/x509up." + vo.getVo());
 			proxyVoFile.delete();
 		}
 		
 		SessionMessages.add(request, "proxy-destroy-success");
 	}
 }
