 package com.zhongying.datadapter.service.issue;
 
 import java.io.File;
 import java.util.Date;
 
 import org.apache.commons.lang3.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.util.Assert;
 
 import com.zhongying.datadapter.constant.IssueType;
 import com.zhongying.datadapter.dao.issue.IssueDao;
 import com.zhongying.datadapter.service.AbstractDownloadService;
 import com.zhongying.datadapter.utils.DateUtils;
 
 /**
  * ·issueصķ
  * 
  * @author bbn
  */
 @Component
 @Transactional
 public class IssueService extends AbstractDownloadService {
 
 	private static final Logger logger = LoggerFactory.getLogger(IssueService.class);
 
 	@Autowired
 	private IssueDao dao;
 
 	private ThreadLocal<IssueType> typeHolder = new ThreadLocal<IssueType>();
 
 	/**
 	 * ؼ·IssueļӦĿ¼
 	 */
 	@Override
 	public void download() {
 		IssueType type = typeHolder.get();
 		Date date = dateHolder.get();
 		Assert.notNull(type);
 		Assert.notNull(date);
 
 		// ڸʽ
 		String datestr = DateUtils.toString(date, "yyyyMMdd");
 
 		// ½FTP
 		logger.info("ؼissueļ,Ϊ{},Ϊ{}...", type, datestr);
 		if (!ftpUtils.connect()) {
 			logger.error("ؼissueļ,Ϊ{},Ϊ{},ʧ!", type, datestr);
 			return;
 		}
 
 		// FTPعĿ¼(ͬĿ¼ṹ)
 		File localWorkDir = new File(ftpUtils.getDownloadDir(), "issue_data" + File.separator + type + File.separator + datestr);
 		if (! localWorkDir.exists()) {
 			logger.info("FTPĿ¼{},֮...", localWorkDir);
 			if (! localWorkDir.mkdirs()) {
 				logger.error("FTPعĿ¼{},ʧ!", localWorkDir);
 			}
 		}
 		logger.info("FTPعĿ¼Ϊ:{}", localWorkDir);
 		
 		// FTPԶ̹Ŀ¼
 		String remoteWorkDir = "/issue_data/" + type + "/" + datestr;
 		logger.info("FTPԶ̹Ŀ¼Ϊ:{}", remoteWorkDir);
 
 		// data_upload_begin.txtdata_upload_end.txt־ļǷ
 		if (ftpUtils.hasFiles(remoteWorkDir, "data_upload_begin.txt", "data_upload_end.txt")) {
 			logger.info("data_upload_begin.txtdata_upload_end.txt־ļ,׼...");
 		} else {
			logger.warn("δdata_upload_begin.txtdata_upload_end.txt־ļ,ȡ!");
 			return;
 		}
 
 		// ԶĿ¼µļعĿ¼
 		if (ftpUtils.downloadDirToDir(remoteWorkDir, localWorkDir)) {
 			logger.info("ؼissueļ,Ϊ{},Ϊ{},OK!", type, datestr);
 		} else {
 			logger.error("ؼissueļ,Ϊ{},Ϊ{},ʧ!", type, datestr);
 		}
 
 		// رFTP
 		ftpUtils.disconnect();
 		logger.info("رFTP");
 	}
 
 	/**
 	 * <pre>
 	 * غõIssueļ뵽ݿ,:
 	 * 1.ֱӵ뵽DA_ORI_RES_ͷı
 	 * 2.ȫ,ȫƵDA_RES_ͷı;,仯ͬDA_RES_ͷı
 	 * 3.DA_RES_ͷı,ӳǼŵǰԴ
 	 * </pre>
 	 */
 	@Override
 	public void load() {
 		IssueType type = typeHolder.get();
 		Date date = dateHolder.get();
 		Assert.notNull(type);
 		Assert.notNull(date);
 
 		// ڸʽ
 		String datestr = DateUtils.toString(date, "yyyyMMdd");
 
 		// IssueļĿ¼(ͬĿ¼ṹ)
 		File localWorkDir = new File(ftpUtils.getDownloadDir(), File.separator + "issue_data" + File.separator + type + File.separator + datestr);
 		if (! localWorkDir.exists()) {
 			logger.warn("IssueļĿ¼{},ȡ!", localWorkDir);
 			return;
 		}
 		logger.info("IssueļĿ¼Ϊ:{}", localWorkDir);
 
 		// Ᵽ
 		for (File file : localWorkDir.listFiles()) {
 			dao.insertFromFile(file, type);
 			logger.info("Issueļ{} OK!", file.getName());
 		}
 		
 		// ִͬ
 		dao.syncData(date, type);
 		logger.info("Issueļͬ OK!");
 	}
 
 	/**
 	 * ɾݿissue
 	 */
 	@Override
 	public void delete() {
 		IssueType type = typeHolder.get();
 		Date date = dateHolder.get();
 		Assert.notNull(type);
 		Assert.notNull(date);
 
 		logger.info("ɾݿΪ{}issue", DateUtils.toString(date, "yyyyMMdd"));
 		dao.deleteIssue(date, type);
 	}
 
 	public void setType(IssueType type) {
 		typeHolder.set(type);
 	}
 
 	public void setType(String typeValue) {
 		for (IssueType v : IssueType.values()) {
 			if (StringUtils.equals(typeValue, v.getValue())) {
 				setType(IssueType.valueOf(typeValue));
 				return;
 			}
 		}
 	}
 
 }
