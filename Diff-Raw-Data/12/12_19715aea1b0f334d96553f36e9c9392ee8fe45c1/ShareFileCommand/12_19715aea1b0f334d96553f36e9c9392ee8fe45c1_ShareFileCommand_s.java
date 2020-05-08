 package com.lorent.sharefile;
 
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.lang.reflect.Constructor;
 import java.lang.reflect.Method;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 
 import javax.imageio.ImageIO;
 
 import org.apache.mina.core.session.IoSession;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.lorent.commonconfig.ConfigUtil;
 import com.lorent.sharefile.bean.UserInfoBean;
 import com.lorent.util.FileListConfigUtilFactory;
 import com.lorent.util.UniqueIntIDCreator;
 import com.lorent.whiteboard.command.MeetingCommandAdaptor;
 
 public class ShareFileCommand extends MeetingCommandAdaptor {
 
 	public ShareFileCommand(String meetingId) {
 		super(meetingId);
 	}
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	private static final Logger logger = LoggerFactory
 			.getLogger(ShareFileCommand.class);
 	
 	public synchronized void execute(IoSession session){
 		if (operation.equals("ClientUpFileToServer")) {
 			//上穿文件至服务器
 			
 			String fileName = (String) getParameter("FilePath");
 			String uniqueFileID = (String) getParameter("UniqueFileID");
 			int buffersize = (Integer) getParameter("BufferSize");
 			byte[] buffer = (byte[]) getParameter("Buffer");
 			byte[] bufferwrite = new byte[buffersize];
 			System.arraycopy(buffer, 0, bufferwrite, 0, buffersize);
 //			bufferwrite = buffer;
 			
 			int lastIndexOf = fileName.lastIndexOf("\\");
 			String substring = fileName.substring(lastIndexOf+1)+"."+uniqueFileID+".tmp";
 			String apppath = (String) session.getAttribute("AppPath");
 			
 			File file = new File(apppath+"/tmp/"+substring);
 			try {
 				FileOutputStream fileOutputStream = new FileOutputStream(file, true);
 				
 				fileOutputStream.write(bufferwrite);
 				fileOutputStream.close();
 			} catch (Exception e) {
 				logger.error("ClientUpFileToServer error:",e);
 			}
 			ShareFileCommandResult shareFileCommandResult = new ShareFileCommandResult();
 			shareFileCommandResult.setValue("MSG", apppath+substring+","+buffer.length);
 			session.write(shareFileCommandResult);
 		}
 		else if(operation.equals("ClientUpFileToServerFinished")){
 			//上传文件至服务器完毕
 			//判断会议文件夹是否存在
 			String apppath = (String) session.getAttribute("AppPath");
 			String fileName = (String) getParameter("FilePath");
 			String uniqueFileID = (String) getParameter("UniqueFileID");
 			int lastIndexOf = fileName.lastIndexOf("\\");
 			String substring = fileName.substring(lastIndexOf+1)+"."+uniqueFileID+".tmp";
 			File file = new File(apppath+"/files/"+getMeetingId());
 			if (!file.exists()) {
 				file.mkdirs();
 			}
			String realFileName = uniqueFileID + "_" + fileName.substring(lastIndexOf+1);
 			String filePath = apppath + "/files/" + getMeetingId() + "/" + realFileName;
 			try {
 				ConfigUtil configUtil = FileListConfigUtilFactory.getFileListConfUtilByMeetingID(getMeetingId(), session);
 				String oldFileName = configUtil.getProperty(fileName.substring(lastIndexOf+1));
 				File oldFile = new File(apppath + "/files/" + getMeetingId() + "/" + oldFileName);
 				if(oldFile.exists()){
 //					oldFile.setLastModified(System.currentTimeMillis());
 //					logger.error("upload file:" +oldFile.getAbsolutePath() + ";file time:" + oldFile.lastModified());
 					ConfigUtil deleteConfigUtil = FileListConfigUtilFactory.getDeleteFileListConfUtilByMeetingID(getMeetingId(), session);
 					deleteConfigUtil.setProperty(oldFileName, String.valueOf(System.currentTimeMillis()));
 				}
 				
 				FileUtil.copyFile(apppath+"/tmp/"+substring, filePath);
 				FileUtil.deleteFile(apppath+"/tmp/"+substring);
 				configUtil.setProperty(fileName.substring(lastIndexOf+1), realFileName);
 			} catch (Exception e) {
 				logger.error("ClientUpFileToServerFinished error:",e);
 			}
 			
 			ArrayList<UserInfoBean> userInfos = ShareFileDto.getUserInfos(getMeetingId());
 			for (UserInfoBean userInfoBean : userInfos) {
 				ShareFileCommand shareFileCommand = new ShareFileCommand(getMeetingId());
 				shareFileCommand.setOperation("ServerCallClientGetFileList");
 				
 				userInfoBean.getAliveSession().write(shareFileCommand);
 			}
 			
 			session.write("finished");
 			if(getParameter("loadToWhiteBoard")!=null&&(Boolean)getParameter("loadToWhiteBoard")){
 				
 				try {
 					Class<?> clazz = Class.forName("com.lorent.fileserver.convertor.FileConvertors");
 					Method method = clazz.getDeclaredMethod("convert", File.class,String.class);
 					method.invoke(null, new File(filePath),meetingId);
 				} catch (Exception e) {
 					logger.warn("convert file {} failed",filePath);
 					logger.warn("failed by",e);
 				}
 			}
 		}
 		else if(operation.equals("ClientDownLoadFileFromServer")){
 			//从服务器下载文件
 			
 			ShareFileCommandResult shareFileCommandResult = new ShareFileCommandResult();
 			
 			
 			String apppath = (String) session.getAttribute("AppPath");
 		
 			String filename = (String) getParameter("FileName");
 			String realFileName = (String) getParameter("RealFileName");
 			
 			int buffersize = (Integer) getParameter("readBufferSize");
 			long fileposition = (Long) getParameter("filePosition");
 			long time = (Long) getParameter("time");
 			File file = new File(apppath+"/files/"+getMeetingId()+"/"+realFileName);
 			if (!file.exists()) {
 				shareFileCommandResult.setValue("State", "ERROR");
 				shareFileCommandResult.setValue("MSG", "文件不存在");
 			}
 			int offset =  0;
 			try {
 				FileInputStream fileInputStream = new FileInputStream(file);
 				byte[] buffer =  new byte[(int) buffersize];
 				fileInputStream.skip(fileposition);
 				offset = fileInputStream.read(buffer);
 				
 				shareFileCommandResult.setValue("buffer", buffer);
 				shareFileCommandResult.setValue("offset", offset);
 				
 				fileInputStream.close();
 				
 			} catch (FileNotFoundException e) {
 				logger.error("ClientDownLoadFileFromServer error:",e);
 			} catch (IOException e) {
 				logger.error("ClientDownLoadFileFromServer error:",e);
 			}
 			session.write(shareFileCommandResult);
 			logger.error("====="+time + " :download realFileName =============" + realFileName + offset);
 		}
 		else if(operation.equals("GetServerFileSize")){
 			//获得服务器文件大小
 			ShareFileCommandResult shareFileCommandResult = new ShareFileCommandResult();
 			
 			String apppath = (String) session.getAttribute("AppPath");
 			String filename1 = (String) getParameter("FileName");
 			ConfigUtil configUtil = FileListConfigUtilFactory.getFileListConfUtilByMeetingID(getMeetingId(), session);
 			String realFileName = configUtil.getProperty(filename1);
 			String filename = apppath+"/files/"+getMeetingId()+"/"+realFileName;
 			File file = new File(filename);
 			long length = -1;
 			if (!file.exists()) {
 				shareFileCommandResult.setValue("State", "ERROR");
 				shareFileCommandResult.setValue("MSG", "文件不存在");
 			}
 			else{
 				length = file.length();
 			}
 			logger.info("GetServerFileSize {}",filename1);
 			shareFileCommandResult.setValue("FileSize", length);
 			shareFileCommandResult.setValue("FileName", filename);
 			shareFileCommandResult.setValue("AppPath", apppath);
 			shareFileCommandResult.setValue("RealFileName", realFileName);
 			session.write(shareFileCommandResult);
 			
 		}
 		else if(operation.equals("GetFileList")){
 			//获得该会议可以下载的文件列表
 			logger.info("GetFileList");
 			ShareFileCommandResult shareFileCommandResult = new ShareFileCommandResult();
 			
 			String apppath = (String) session.getAttribute("AppPath");
 			String filepath = apppath +"/files/"+getMeetingId();
 			ConfigUtil configUtil = FileListConfigUtilFactory.getFileListConfUtilByMeetingID(getMeetingId(), session);
 			Properties properties = configUtil.getProperties();
 			
 			ArrayList<String> arrayList = new ArrayList<String>();
 			Set set = properties.keySet();
 
 			for(Object obj:set){
 				logger.error("GetFileList====" + (String)obj);
 				arrayList.add((String)obj);
 			}
 			Object[] array = arrayList.toArray();
 			shareFileCommandResult.setValue("FileNameArray", array);
 			session.write(shareFileCommandResult);
 		}
 		else if(operation.equals("ClientKeepAliveConnect")){
 			//客户端心跳机制，保持连接
 			String username = (String) getParameter("UserName");
 			UserInfoBean userInfoBean = new UserInfoBean();
 			userInfoBean.setIp(session.getRemoteAddress().toString());
 			userInfoBean.setUserName(username);
 			userInfoBean.setAliveSession(session);
 //			System.out.println(session.getRemoteAddress().toString());
 			ShareFileDto.setUserInfo(getMeetingId(), userInfoBean);
 			logger.info("ClientKeepAliveConnect: {}",username);
 		}
 		else if(operation.equals("RemoveConferenceFiles")){
 			//删除会议的所有文件
 			String apppath = (String) session.getAttribute("AppPath");
 			String filepath = apppath +"/files/"+getMeetingId();
 			ShareFileCommandResult shareFileCommandResult = new ShareFileCommandResult();
 			logger.info("RemoveConferenceFiles {}",filepath);
 			try {
 				FileUtil.deletePath(filepath);
 				ConfigUtil configUtil = FileListConfigUtilFactory.getFileListConfUtilByMeetingID(getMeetingId(), session);
 				Properties properties = configUtil.getProperties();
 				properties.clear();
 				shareFileCommandResult.setValue("State", "OK");
 			} catch (Exception e) {
 				shareFileCommandResult.setValue("State", "ERROR");
 //				e.printStackTrace();
 				logger.error("RemoveConferenceFiles error:",e);
 			}
 			session.write(shareFileCommandResult);
 		}
 		else if(operation.equals("LoadToWhiteBoard")){
 			//加载到白板
 			
 			String apppath = (String) session.getAttribute("AppPath");
 			String filename = (String) getParameter("FileName");
 			ConfigUtil configUtil = FileListConfigUtilFactory.getFileListConfUtilByMeetingID(getMeetingId(), session);
 			String realFileName = configUtil.getProperty(filename);
 			String filePath=apppath+"/files/"+getMeetingId()+"/"+realFileName;
 			logger.info("LoadToWhiteBoard {}",filePath);
 			try {
 				Class<?> clazz = Class.forName("com.lorent.fileserver.convertor.FileConvertors");
 				Method method = clazz.getDeclaredMethod("convert", File.class,String.class);
 				method.invoke(null, new File(filePath),meetingId);
 			} catch (Exception e) {
 				logger.warn("convert file {} failed",filePath);
 				logger.warn("failed by",e);
 			}
 			
 		}else if(operation.equals("startScreenShare")){
 			String ip = (String)getParameter("ip");
 	        int port = (Integer)getParameter("port");
 	        String passwd = (String)getParameter("passwd");
 	        String username = (String)getParameter("username");
 	        Map<String, Integer> repeaterids = (Map<String, Integer>) getParameter("repeaterids");
 	        logger.info("startScreenShare:ip=" + ip + "&port=" + port + "&passwd=" + passwd + "&username=" + username);
 			ArrayList<UserInfoBean> userInfos = ShareFileDto.getUserInfos(getMeetingId());
 			for (UserInfoBean userInfoBean : userInfos) {
 				logger.info("userInfoBean:"+userInfoBean);
 				ShareFileCommand shareFileCommand = new ShareFileCommand(getMeetingId());
 				if(username.equals(userInfoBean.getUserName())){
 					;
 				}else{
 					shareFileCommand.setOperation("startScreenShare");
 					shareFileCommand.setParameter("ip", ip);
 					shareFileCommand.setParameter("port", port);
 					shareFileCommand.setParameter("passwd", passwd);
 					shareFileCommand.setParameter("username", username);
 					shareFileCommand.setParameter("repeaterids", repeaterids);
 					userInfoBean.getAliveSession().write(shareFileCommand);
 					logger.info("startScreenShare send target:"+userInfoBean.getAliveSession());
 				}
 			}
 		}else if(operation.equals("stopScreenShare")){
 	        String username = (String)getParameter("username");
 	        logger.info("stopScreenShare:username=" + username);
 			ArrayList<UserInfoBean> userInfos = ShareFileDto.getUserInfos(getMeetingId());
 			for (UserInfoBean userInfoBean : userInfos) {
 				ShareFileCommand shareFileCommand = new ShareFileCommand(getMeetingId());
 				if(username.equals(userInfoBean.getUserName())){
 					;
 				}else{
 					shareFileCommand.setOperation("stopScreenShare");
 					shareFileCommand.setParameter("username", username);					
 					userInfoBean.getAliveSession().write(shareFileCommand);
 					logger.info("stopScreenShare send target:"+userInfoBean.getAliveSession());
 				}
 			}
 		} else if (operation.equals("GetPage")) {
 			String apppath = (String) session.getAttribute("AppPath");
 			String fileName = (String) getParameter("fileName");
 			Integer page = (Integer) getParameter("page");
 			Integer pageCount = (Integer) getParameter("pageCount");
 			StringBuilder filePath = new StringBuilder(apppath)
 					.append(File.separator).append("files")
 					.append(File.separator).append(getMeetingId())
 					.append(File.separator).append(fileName);
 			logger.debug("fileName {} page {}", fileName, page);
 			int index = fileName.lastIndexOf('.');
 			
 			//拼接转换后的图片文件地址
 			//eg:/opt/lcp/lvmcserver/files/426893/abc.pdf.jpgs/abc.pdf
 			//eg:/opt/lcp/lvmcserver/files/426893/abc.ppt.pdf/converted.pdf.jpgs/converted.pdf
 			if (".pdf".equals(fileName.substring(index))) {
 				filePath.append(".jpgs").append(File.separator)
 						.append(fileName);
 			} else {
 				filePath.append(".pdf").append(File.separator)
 						.append("converted.pdf.jpgs").append(File.separator)
 						.append("converted.pdf");
 			}
 			filePath.append('_').append(pageCount).append('_').append(page)
 					.append(".jpg");
 			try {
 				BufferedImage image = ImageIO.read(new File(filePath.toString()));
 				Class<?> clazz = Class.forName("org.jhotdraw.samples.svg.figures.SVGImage");
 				Constructor<?> constructor = clazz.getConstructor(BufferedImage.class);
 				Object updater = constructor.newInstance(image);
 				session.write(updater);
 			} catch (IOException e) {
 				logger.error("Read File {} error",filePath.toString());
 				logger.error("GetPage Error!",e);
 			} catch (Exception e) {
 				logger.error("GetPage Error!",e);
 			}
 
 		}else if(operation.equals("GetRepeaterIDs")){
 	        String username = (String)getParameter("username");
 	        String[] targetusers = (String[]) getParameter("targetusers");
 	        logger.info("GetRepeaterID username="+username+" targetusers:"+targetusers);
 	        HashMap<String, Integer> repeaterids = new HashMap<String, Integer>();
 	        
 	        for (String name : targetusers) {
 	        	String key =  username+"-"+name;
 				int repeaterID = UniqueIntIDCreator.create();//RepeaterIDDto.getRepeaterID(key);
 				repeaterids.put(key, repeaterID);
 			}
 	        ShareFileCommandResult shareFileCommandResult = new ShareFileCommandResult();
 	        shareFileCommandResult.setValue("OriginalCommand", this);
 	        shareFileCommandResult.setValue("RepeaterIDs", repeaterids);
 	        shareFileCommandResult.setValue("ResultID", "GetRepeaterID");
 	        session.write(shareFileCommandResult);
 		}else if("deleteFile".equals(operation)){
 			ShareFileCommandResult shareFileCommandResult = new ShareFileCommandResult();
 			String filename = (String) getParameter("FileName");
 			ConfigUtil configUtil = FileListConfigUtilFactory.getFileListConfUtilByMeetingID(getMeetingId(), session);
 			try {
 				String realFileName = configUtil.getProperty(filename);
 //				String apppath = (String) session.getAttribute("AppPath");
 //				File file = new File(apppath + "/" + getMeetingId() + "/" + realFileName);
 //				file.setLastModified(System.currentTimeMillis());
 //				logger.error("delete file:" +file.getAbsolutePath() + ";file time:" + file.lastModified());
 				ConfigUtil deleteConfigUtil = FileListConfigUtilFactory.getDeleteFileListConfUtilByMeetingID(getMeetingId(), session);
 				deleteConfigUtil.setProperty(realFileName, String.valueOf(System.currentTimeMillis()));
 				configUtil.removeProperty(filename);
 			} catch (Exception e) {
 //				e.printStackTrace();
 				logger.error("deleteFile error:",e);
 			}
 //			session.write(shareFileCommandResult);
 			ArrayList<UserInfoBean> userInfos = ShareFileDto.getUserInfos(getMeetingId());
 			for (UserInfoBean userInfoBean : userInfos) {
 				ShareFileCommand shareFileCommand = new ShareFileCommand(getMeetingId());
 				shareFileCommand.setOperation("ServerCallClientGetFileList");
 				
 				userInfoBean.getAliveSession().write(shareFileCommand);
 			}
 			
 //			session.write("finished");
 		}else if("getRealFileName".equals(operation)){
 			String filename = (String) getParameter("FileName");
 			ConfigUtil configUtil = FileListConfigUtilFactory.getFileListConfUtilByMeetingID(getMeetingId(), session);
 			try {
 				String realFileName = configUtil.getProperty(filename);
 				logger.info("getRealFileName: "+filename+" , "+realFileName);
 				ShareFileCommandResult shareFileCommandResult = new ShareFileCommandResult();
 		        shareFileCommandResult.setValue("OriginalCommand", this);
 		        shareFileCommandResult.setValue("RealFileName", realFileName);
 		        shareFileCommandResult.setValue("ResultID", "getRealFileName");
 		        session.write(shareFileCommandResult);
 			} catch (Exception e) {
 				logger.error("getRealFileName error:",e);
 			}
 		}
 		
 	}
 	
 	/*public synchronized void execute(IoSession session){
 		if (operation.equals("ClientUpFileToServer")) {
 			//上穿文件至服务器
 			
 			String fileName = (String) getParameter("FilePath");
 			String uniqueFileID = (String) getParameter("UniqueFileID");
 			int buffersize = (Integer) getParameter("BufferSize");
 			byte[] buffer = (byte[]) getParameter("Buffer");
 			byte[] bufferwrite = new byte[buffersize];
 			System.arraycopy(buffer, 0, bufferwrite, 0, buffersize);
 //			bufferwrite = buffer;
 			
 			int lastIndexOf = fileName.lastIndexOf("\\");
 			String substring = fileName.substring(lastIndexOf+1)+"."+uniqueFileID+".tmp";
 			String apppath = (String) session.getAttribute("AppPath");
 			
 			File file = new File(apppath+"/tmp/"+substring);
 			try {
 				FileOutputStream fileOutputStream = new FileOutputStream(file, true);
 				
 				fileOutputStream.write(bufferwrite);
 				fileOutputStream.close();
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			ShareFileCommandResult shareFileCommandResult = new ShareFileCommandResult();
 			shareFileCommandResult.setValue("MSG", apppath+substring+","+buffer.length);
 			session.write(shareFileCommandResult);
 		}
 		else if(operation.equals("ClientUpFileToServerFinished")){
 			//上传文件至服务器完毕
 			//判断会议文件夹是否存在
 			String apppath = (String) session.getAttribute("AppPath");
 			String fileName = (String) getParameter("FilePath");
 			String uniqueFileID = (String) getParameter("UniqueFileID");
 			int lastIndexOf = fileName.lastIndexOf("\\");
 			String substring = fileName.substring(lastIndexOf+1)+"."+uniqueFileID+".tmp";
 			File file = new File(apppath+"/files/"+getMeetingId());
 			if (!file.exists()) {
 				file.mkdirs();
 			}
 			String filePath=apppath+"/files/"+getMeetingId()+"/"+fileName.substring(lastIndexOf+1);
 			try {
 				FileUtil.copyFile(apppath+"/tmp/"+substring, filePath);
 				FileUtil.deleteFile(apppath+"/tmp/"+substring);
 			} catch (Exception e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			
 			ArrayList<UserInfoBean> userInfos = ShareFileDto.getUserInfos(getMeetingId());
 			for (UserInfoBean userInfoBean : userInfos) {
 				ShareFileCommand shareFileCommand = new ShareFileCommand(getMeetingId());
 				shareFileCommand.setOperation("ServerCallClientGetFileList");
 				
 				userInfoBean.getAliveSession().write(shareFileCommand);
 			}
 			
 			session.write("finished");
 			if(getParameter("loadToWhiteBoard")!=null&&(Boolean)getParameter("loadToWhiteBoard")){
 				
 				try {
 					Class<?> clazz = Class.forName("com.lorent.fileserver.convertor.FileConvertors");
 					Method method = clazz.getDeclaredMethod("convert", File.class,String.class);
 					method.invoke(null, new File(filePath),meetingId);
 				} catch (Exception e) {
 					logger.warn("convert file {} failed",filePath);
 					logger.warn("failed by",e);
 				}
 			}
 		}
 		else if(operation.equals("ClientDownLoadFileFromServer")){
 			//从服务器下载文件
 			
 			ShareFileCommandResult shareFileCommandResult = new ShareFileCommandResult();
 			
 			
 			String apppath = (String) session.getAttribute("AppPath");
 		
 			String filename = (String) getParameter("FileName");
 			
 			int buffersize = (Integer) getParameter("readBufferSize");
 			long fileposition = (Long) getParameter("filePosition");
 			
 			File file = new File(apppath+"/files/"+getMeetingId()+"/"+filename);
 			if (!file.exists()) {
 				shareFileCommandResult.setValue("State", "ERROR");
 				shareFileCommandResult.setValue("MSG", "文件不存在");
 			}
 			try {
 				FileInputStream fileInputStream = new FileInputStream(file);
 				byte[] buffer =  new byte[(int) buffersize];
 				fileInputStream.skip(fileposition);
 				int offset = fileInputStream.read(buffer);
 				
 				shareFileCommandResult.setValue("buffer", buffer);
 				shareFileCommandResult.setValue("offset", offset);
 				
 				fileInputStream.close();
 				
 			} catch (FileNotFoundException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			} catch (IOException e) {
 				// TODO Auto-generated catch block
 				e.printStackTrace();
 			}
 			session.write(shareFileCommandResult);
 			
 		}
 		else if(operation.equals("GetServerFileSize")){
 			//获得服务器文件大小
 			ShareFileCommandResult shareFileCommandResult = new ShareFileCommandResult();
 			
 			String apppath = (String) session.getAttribute("AppPath");
 			String filename1 = (String) getParameter("FileName");
 			String filename = apppath+"/files/"+getMeetingId()+"/"+filename1;
 			File file = new File(filename);
 			long length = -1;
 			if (!file.exists()) {
 				shareFileCommandResult.setValue("State", "ERROR");
 				shareFileCommandResult.setValue("MSG", "文件不存在");
 			}
 			else{
 				length = file.length();
 			}
 			
 			shareFileCommandResult.setValue("FileSize", length);
 			shareFileCommandResult.setValue("FileName", filename);
 			shareFileCommandResult.setValue("AppPath", apppath);
 			session.write(shareFileCommandResult);
 			
 		}
 		else if(operation.equals("GetFileList")){
 			//获得该会议可以下载的文件列表
 			ShareFileCommandResult shareFileCommandResult = new ShareFileCommandResult();
 			
 			String apppath = (String) session.getAttribute("AppPath");
 			String filepath = apppath +"/files/"+getMeetingId();
 			
 			ArrayList<String> arrayList = new ArrayList<String>();
 			File file = new File(filepath);
 			if (file.exists() && file.isDirectory()) {
 				File[] listFiles = file.listFiles();
 				for (File file2 : listFiles) {
 					if (file2.isFile()) {
 						arrayList.add(file2.getName());
 					}
 				}
 			}
 			
 			Object[] array = arrayList.toArray();
 			shareFileCommandResult.setValue("FileNameArray", array);
 			session.write(shareFileCommandResult);
 		}
 		else if(operation.equals("ClientKeepAliveConnect")){
 			//客户端心跳机制，保持连接
 			String username = (String) getParameter("UserName");
 			UserInfoBean userInfoBean = new UserInfoBean();
 			userInfoBean.setIp(session.getRemoteAddress().toString());
 			userInfoBean.setUserName(username);
 			userInfoBean.setAliveSession(session);
 //			System.out.println(session.getRemoteAddress().toString());
 			ShareFileDto.setUserInfo(getMeetingId(), userInfoBean);
 			logger.info("ClientKeepAliveConnect: {}",username);
 		}
 		else if(operation.equals("RemoveConferenceFiles")){
 			//删除会议的所有文件
 			String apppath = (String) session.getAttribute("AppPath");
 			String filepath = apppath +"/files/"+getMeetingId();
 			ShareFileCommandResult shareFileCommandResult = new ShareFileCommandResult();
 			
 			try {
 				FileUtil.deletePath(filepath);
 				shareFileCommandResult.setValue("State", "OK");
 			} catch (Exception e) {
 				shareFileCommandResult.setValue("State", "ERROR");
 				e.printStackTrace();
 			}
 			session.write(shareFileCommandResult);
 		}
 		else if(operation.equals("LoadToWhiteBoard")){
 			//加载到白板
 			
 			String apppath = (String) session.getAttribute("AppPath");
 			String filename = (String) getParameter("FileName");
 			String filePath=apppath+"/files/"+getMeetingId()+"/"+filename;
 			try {
 				Class<?> clazz = Class.forName("com.lorent.fileserver.convertor.FileConvertors");
 				Method method = clazz.getDeclaredMethod("convert", File.class,String.class);
 				method.invoke(null, new File(filePath),meetingId);
 			} catch (Exception e) {
 				logger.warn("convert file {} failed",filePath);
 				logger.warn("failed by",e);
 			}
 			
 		}else if(operation.equals("startScreenShare")){
 			String ip = (String)getParameter("ip");
 	        int port = (Integer)getParameter("port");
 	        String passwd = (String)getParameter("passwd");
 	        String username = (String)getParameter("username");
 	        Map<String, Integer> repeaterids = (Map<String, Integer>) getParameter("repeaterids");
 	        logger.info("startScreenShare:ip=" + ip + "&port=" + port + "&passwd=" + passwd + "&username=" + username);
 			ArrayList<UserInfoBean> userInfos = ShareFileDto.getUserInfos(getMeetingId());
 			for (UserInfoBean userInfoBean : userInfos) {
 				logger.info("userInfoBean:"+userInfoBean);
 				ShareFileCommand shareFileCommand = new ShareFileCommand(getMeetingId());
 				if(username.equals(userInfoBean.getUserName())){
 					;
 				}else{
 					shareFileCommand.setOperation("startScreenShare");
 					shareFileCommand.setParameter("ip", ip);
 					shareFileCommand.setParameter("port", port);
 					shareFileCommand.setParameter("passwd", passwd);
 					shareFileCommand.setParameter("username", username);
 					shareFileCommand.setParameter("repeaterids", repeaterids);
 					userInfoBean.getAliveSession().write(shareFileCommand);
 					logger.info("startScreenShare send target:"+userInfoBean.getAliveSession());
 				}
 			}
 		}else if(operation.equals("stopScreenShare")){
 	        String username = (String)getParameter("username");
 	        logger.info("stopScreenShare:username=" + username);
 			ArrayList<UserInfoBean> userInfos = ShareFileDto.getUserInfos(getMeetingId());
 			for (UserInfoBean userInfoBean : userInfos) {
 				ShareFileCommand shareFileCommand = new ShareFileCommand(getMeetingId());
 				if(username.equals(userInfoBean.getUserName())){
 					;
 				}else{
 					shareFileCommand.setOperation("stopScreenShare");
 					shareFileCommand.setParameter("username", username);					
 					userInfoBean.getAliveSession().write(shareFileCommand);
 					logger.info("stopScreenShare send target:"+userInfoBean.getAliveSession());
 				}
 			}
 		} else if (operation.equals("GetPage")) {
 			String apppath = (String) session.getAttribute("AppPath");
 			String fileName = (String) getParameter("fileName");
 			Integer page = (Integer) getParameter("page");
 			Integer pageCount = (Integer) getParameter("pageCount");
 			StringBuilder filePath = new StringBuilder(apppath)
 					.append(File.separator).append("files")
 					.append(File.separator).append(getMeetingId())
 					.append(File.separator).append(fileName);
 			logger.debug("fileName {} page {}", fileName, page);
 			int index = fileName.lastIndexOf('.');
 			
 			//拼接转换后的图片文件地址
 			//eg:/opt/lcp/lvmcserver/files/426893/abc.pdf.jpgs/abc.pdf
 			//eg:/opt/lcp/lvmcserver/files/426893/abc.ppt.pdf/converted.pdf.jpgs/converted.pdf
 			if (".pdf".equals(fileName.substring(index))) {
 				filePath.append(".jpgs").append(File.separator)
 						.append(fileName);
 			} else {
 				filePath.append(".pdf").append(File.separator)
 						.append("converted.pdf.jpgs").append(File.separator)
 						.append("converted.pdf");
 			}
 			filePath.append('_').append(pageCount).append('_').append(page)
 					.append(".jpg");
 			try {
 				BufferedImage image = ImageIO.read(new File(filePath.toString()));
 				Class<?> clazz = Class.forName("org.jhotdraw.samples.svg.figures.SVGImage");
 				Constructor<?> constructor = clazz.getConstructor(BufferedImage.class);
 				Object updater = constructor.newInstance(image);
 				session.write(updater);
 			} catch (IOException e) {
 				logger.error("Read File {} error",filePath.toString());
 				logger.error("GetPage Error!",e);
 			} catch (Exception e) {
 				logger.error("GetPage Error!",e);
 			}
 
 		}else if(operation.equals("GetRepeaterIDs")){
 	        String username = (String)getParameter("username");
 	        String[] targetusers = (String[]) getParameter("targetusers");
 	        logger.info("GetRepeaterID username="+username+" targetusers:"+targetusers);
 	        HashMap<String, Integer> repeaterids = new HashMap<String, Integer>();
 	        
 	        for (String name : targetusers) {
 	        	String key =  username+"-"+name;
 				int repeaterID = UniqueIntIDCreator.create();//RepeaterIDDto.getRepeaterID(key);
 				repeaterids.put(key, repeaterID);
 			}
 	        ShareFileCommandResult shareFileCommandResult = new ShareFileCommandResult();
 	        shareFileCommandResult.setValue("OriginalCommand", this);
 	        shareFileCommandResult.setValue("RepeaterIDs", repeaterids);
 	        shareFileCommandResult.setValue("ResultID", "GetRepeaterID");
 	        session.write(shareFileCommandResult);
 		}else if("deleteFile".equals(operation)){
 			ShareFileCommandResult shareFileCommandResult = new ShareFileCommandResult();
 			String filename = (String) getParameter("FileName");
 			String apppath = (String) session.getAttribute("AppPath");
 			String filePath = apppath+"/files/"+getMeetingId()+"/"+filename;
 			try {
 				FileUtil.deletePath(filePath);
 				shareFileCommandResult.setValue("State", "OK");
 			} catch (Exception e) {
 				shareFileCommandResult.setValue("State", "ERROR");
 				e.printStackTrace();
 			}
 //			session.write(shareFileCommandResult);
 			ArrayList<UserInfoBean> userInfos = ShareFileDto.getUserInfos(getMeetingId());
 			for (UserInfoBean userInfoBean : userInfos) {
 				ShareFileCommand shareFileCommand = new ShareFileCommand(getMeetingId());
 				shareFileCommand.setOperation("ServerCallClientGetFileList");
 				
 				userInfoBean.getAliveSession().write(shareFileCommand);
 			}
 			
 //			session.write("finished");
 		}
 		
 	}*/
 	
 	private String operation;
 
 	private Map<String, Object> parameters = new HashMap<String, Object>();
 	
 	
 	public void setParameter(String key,Object value){
 		parameters.put(key, value);
 	}
 	
 	
 	public Object getParameter(String key){
 		return parameters.get(key);
 	}
 
 
 	/**
 	 * @return the operation
 	 */
 	public String getOperation() {
 		return operation;
 	}
 	/**
 	 * @param operation the operation to set
 	 */
 	public void setOperation(String operation) {
 		this.operation = operation;
 	}
 }
