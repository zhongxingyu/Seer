 package it.more.konakart.util;
 
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.Writer;
 import java.net.URLDecoder;
 import java.security.InvalidParameterException;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.apache.torque.TorqueException;
 import org.apache.velocity.VelocityContext;
 import org.apache.velocity.app.VelocityEngine;
 import org.apache.velocity.tools.generic.DateTool;
 import org.apache.velocity.tools.generic.ListTool;
 
import com.konakart.al.KKAppEng;
 import com.konakart.app.KKException;
 import com.konakart.appif.KKEngIf;
 import com.konakart.blif.ConfigurationMgrIf;
 import com.konakart.util.FileUtils;
import com.konakart.util.KKConstants;
 import com.konakart.util.PropertyFileFinder;
 import com.konakartadmin.app.KKAdminException;
 import com.konakartadmin.appif.KKAdminIf;
 import com.konakartadmin.blif.AdminConfigurationMgrIf;
import com.konakartadmin.ws.KKAdminEngineMgr;
 import com.workingdogs.village.DataSetException;
 
 public class InvoiceUtils {
 	public String velocityLogLocn /*
 								 * = KKEng.getKonakartConfig().getString(
 								 * "velocity.logfile")
 								 */;
 	private String conversionCommand;
 	private String baseDir;
 	private String templateBaseDir;
 	private KKEngIf eng;
 	private KKAdminIf adminEng;
 
 	public InvoiceUtils(KKEngIf eng2) {
 		this.eng = eng2;
 	}
 
 	public InvoiceUtils(KKAdminIf adminEng) {
 		this.adminEng = adminEng;
 	}
 
 	public void setVelocityLogLocn(String velocityLogLocn) {
 		this.velocityLogLocn = velocityLogLocn;
 	}
 
 	private static Log log = LogFactory.getLog(InvoiceUtils.class);
 
 	private static final String TEMPLATE_BASE_DIRECTORY = "TEMPLATE_BASE_DIRECTORY";
 	private static final String ORDER_PDF_COMMAND = "ORDER_PDF_COMMAND";
 
 	protected VelocityEngine getVelocityEngine() throws Exception {
 		VelocityEngine localVelocityEngine = new VelocityEngine();
 		localVelocityEngine.setProperty("file.resource.loader.path", "");
 		String str = PropertyFileFinder
 				.findProperties("konakart_velocity.properties");
 		if (log.isDebugEnabled())
 			log.debug("Velocity Properties file: " + str);
 		localVelocityEngine.init(URLDecoder.decode(str, "UTF-8"));
 		return localVelocityEngine;
 	}
 
 	/*
 	 * public VelocityEngine getVelocityEngine() throws Exception { Properties
 	 * localProperties = new Properties(); if (getVelocityLogLocn() != null) if
 	 * (getVelocityLogLocn().trim().length() == 0) {
 	 * log.debug("Velocity log is disabled");
 	 * localProperties.setProperty("runtime.log.logsystem.class",
 	 * "org.apache.velocity.runtime.log.NullLogSystem"); } else {
 	 * log.debug("Velocity log = " + getVelocityLogLocn()); localProperties
 	 * .setProperty("runtime.log", getVelocityLogLocn()); } else
 	 * log.debug("Using default velocity log");
 	 * localProperties.setProperty("input.encoding", "UTF-8");
 	 * localProperties.setProperty("output.encoding", "UTF-8");
 	 * localProperties.setProperty("resource.loader", "class"); localProperties
 	 * .setProperty("class.resource.loader.class",
 	 * "org.apache.velocity.runtime.resource.loader.ClasspathResourceLoader");
 	 * VelocityEngine localVelocityEngine = new VelocityEngine();
 	 * localVelocityEngine.init(localProperties); return localVelocityEngine; }
 	 */
 	protected String getVelocityLogLocn() {
 		return this.velocityLogLocn;
 	}
 
 	public String createInvoice(int orderId,
 			VelocityContext localVelocityContext, String locale)
 			throws TorqueException, KKException, DataSetException, Exception {
 
 		if (templateBaseDir == null) {
 			throw new InvalidParameterException(
 					"You must initialize the template base directory.");
 		}
 		// order.calculateTotals();
 
 		VelocityEngine localVelocityEngine = getVelocityEngine();
 
 		localVelocityContext.put("dateTool", new DateTool());
 		localVelocityContext.put("listTool", new ListTool());
 
 		if (baseDir == null) {
 			baseDir = "/tmp";
 		}
 		File fnameIn = new File(baseDir, "/orderInvoice_" + orderId + ".html");
 		File fnameOut = new File(baseDir, "/orderInvoice_" + orderId + ".pdf");
 		Writer s = new BufferedWriter(new FileWriter(fnameIn));
 
 		localVelocityEngine.mergeTemplate(templateBaseDir + "OrderInvoice_"
 				+ locale + ".vm", "ISO-8859-1", localVelocityContext, s);
 		s.close();
 
 		String command = String.format(conversionCommand,
 				fnameIn.getAbsoluteFile(), fnameOut.getAbsoluteFile());
 		CommandLineExecuter.execute(command);
		return fnameOut.getName();
 	}
 
 	public void setConversionCommand(AdminConfigurationMgrIf adminConfigMgr)
 			throws TorqueException, KKException, DataSetException,
 			KKAdminException, Exception {
 		conversionCommand = getConfigParameter(adminConfigMgr,
 				ORDER_PDF_COMMAND);
 	}
 
 	public void setBaseDir(AdminConfigurationMgrIf adminConfigMgr)
 			throws TorqueException, KKException, DataSetException,
 			KKAdminException, Exception {
		baseDir = getConfigParameter(adminConfigMgr,
				KKConstants.CONF_KEY_PDF_BASE_DIRECTORY)
				+ FileUtils.FILE_SEPARATOR + getStoreId();
 	}
 
 	private String getConfigParameter(AdminConfigurationMgrIf adminConfigMgr,
 			String paramKey) throws TorqueException, DataSetException,
 			KKAdminException, Exception, KKException {
 		String paramValue = null;
 		try {
 			paramValue = adminConfigMgr.getConfiguration(paramKey)
 					.getConfigurationValue();
 
 		} catch (NullPointerException e) {
 			final String errMsg = String.format("You must configure  %s",
 					paramKey);
 			log.error(errMsg);
 			throw new KKException(errMsg);
 		}
 		return paramValue;
 	}
 
 	private String getConfigParameter(ConfigurationMgrIf configMgr,
 			String paramKey) throws TorqueException, KKException,
 			DataSetException {
 		String paramValue = null;
 		try {
 			paramValue = configMgr.getConfiguration(paramKey).getValue();
 
 		} catch (NullPointerException e) {
 			final String errMsg = String.format("You must configure  %s",
 					paramKey);
 			log.error(errMsg);
 			throw new KKException(errMsg);
 		}
 		return paramValue;
 	}
 
 	public void setBaseDir(ConfigurationMgrIf configMgr)
			throws TorqueException, KKException, DataSetException,
			KKAdminException {
		baseDir = getConfigParameter(configMgr,
				KKConstants.CONF_KEY_PDF_BASE_DIRECTORY)
				+ FileUtils.FILE_SEPARATOR + getStoreId();
 	}
 
 	public void setConversionCommand(ConfigurationMgrIf configMgr)
 			throws TorqueException, KKException, DataSetException {
 		conversionCommand = getConfigParameter(configMgr, ORDER_PDF_COMMAND);
 	}
 
 	public void setTemplateBaseDirectory(AdminConfigurationMgrIf adminConfigMgr)
 			throws TorqueException, DataSetException, KKAdminException,
 			KKException {
 		this.templateBaseDir = getTemplateRoot(adminConfigMgr
 				.getConfigurationValue(TEMPLATE_BASE_DIRECTORY));
 	}
 
 	public void setTemplateBaseDirectory(ConfigurationMgrIf configMgr)
 			throws KKException, KKAdminException {
 		this.templateBaseDir = getTemplateRoot(configMgr
 				.getConfigurationValue(TEMPLATE_BASE_DIRECTORY));
 	}
 
 	protected String getTemplateRoot(String str2) throws KKException,
 			KKAdminException {
 		String str1 = FileUtils.FILE_SEPARATOR;
 		if (str2 != null) {
 			this.templateBaseDir = (str2 + str1 + getStoreId() + str1);
 			return this.templateBaseDir;
 		} else {
 			return null;
 		}
 	}
 
 	protected String getStoreId() throws KKException, KKAdminException {
 
 		if (this.eng == null && this.adminEng == null)
 			throw new KKException(
 					"This manager has been instantiated with KKEng set to null");
 
 		String storeId = "store1";
 		if (this.adminEng != null && this.adminEng.getEngConf() != null) {
 			storeId = this.adminEng.getEngConf().getStoreId();
 		}
 
 		if (this.eng != null && this.eng.getEngConf() != null) {
 			storeId = this.eng.getEngConf().getStoreId();
 		}
 
 		return storeId;
 
 	}
 }
