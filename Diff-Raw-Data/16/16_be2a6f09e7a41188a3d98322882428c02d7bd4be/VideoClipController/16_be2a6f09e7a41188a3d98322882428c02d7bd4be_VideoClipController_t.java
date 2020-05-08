 package com.lorent.vovo.controller;
 
 import it.sauronsoftware.ftp4j.FTPDataTransferListener;
 
 import java.awt.Component;
 import java.awt.EventQueue;
 import java.awt.image.BufferedImage;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.net.URL;
 import java.net.URLEncoder;
 import java.text.MessageFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.TreeSet;
 
 import javax.imageio.ImageIO;
 import javax.swing.JFileChooser;
 import javax.swing.JFrame;
 import javax.swing.JOptionPane;
 import javax.swing.SwingUtilities;
 import javax.swing.SwingWorker;
 import javax.swing.filechooser.FileNameExtensionFilter;
 
 import org.apache.log4j.Logger;
 import org.jdesktop.swingx.painter.ImagePainter;
 import org.jdesktop.swingx.painter.ImagePainter.ScaleType;
 
 import chrriis.dj.nativeswing.swtimpl.NativeInterface;
 import chrriis.dj.nativeswing.swtimpl.components.Credentials;
 import chrriis.dj.nativeswing.swtimpl.components.JWebBrowser;
 import chrriis.dj.nativeswing.swtimpl.components.WebBrowserAuthenticationHandler;
 
 import com.lorent.common.controller.BaseController;
 import com.lorent.common.dto.LCMVideoClip;
 import com.lorent.common.dto.VovoMyInfo;
 import com.lorent.common.util.FileUtil;
 import com.lorent.common.util.ProcessUtil;
 import com.lorent.common.util.StringUtil;
 import com.lorent.vovo.Vovo;
 import com.lorent.vovo.dto.LoginInfo;
 import com.lorent.vovo.ui.MainFrame;
 import com.lorent.vovo.ui.PlayMonitorFrame;
 import com.lorent.vovo.ui.UploadMonitorDialog;
 import com.lorent.vovo.ui.UploadVideoClipDialog;
 import com.lorent.vovo.ui.UploadYunLianVideoClipDialog;
 import com.lorent.vovo.ui.VideoClipInfoDialog;
 import com.lorent.vovo.ui.VideoClipItem;
 import com.lorent.vovo.ui.VideoClipListPanel;
 import com.lorent.vovo.ui.VideoClipPanel;
 import com.lorent.vovo.util.Constants;
 import com.lorent.vovo.util.MonitorsUtil;
 import com.lorent.vovo.util.MonitorsUtil.Monitor;
 import com.lorent.vovo.util.VovoStringUtil;
 
 public class VideoClipController extends BaseController {
 	private static Logger log  = Logger.getLogger(VideoClipController.class);
 	
 	private int pageSize = 4;
 	private int pageIndex = 0;
 	private int maxPageIndex = -1;
 	private int pageIndexM = 0;
 	private int maxPageIndexM = -1;
 	private String lastSelectPath = "";
 	private static SwingWorker<Object, Object> uploadSwingWorker = null;
 	
 	public void showUploadVideoClipDialog() throws Exception{
 		
 		int intProperty = Vovo.getConfigManager().getIntProperty("yunlian", 0);
 		if (intProperty == 0) {
 			UploadVideoClipDialog uploadVideoClipDialog = new UploadVideoClipDialog(null, true);
 			context.getViewManager().setWindowCenterLocation(uploadVideoClipDialog);
 			uploadVideoClipDialog.setVisible(true);
 		}
 		else{
 			UploadYunLianVideoClipDialog uploadYunLianVideoClipDialog = new UploadYunLianVideoClipDialog(null, true);
 			context.getViewManager().setWindowCenterLocation(uploadYunLianVideoClipDialog);
 			uploadYunLianVideoClipDialog.setVisible(true);
 		}
 	}
 	
 	public void showUploadMonitorDialog() throws Exception{
 		UploadMonitorDialog uploadMonitorDialog = new UploadMonitorDialog(null, true);
 		context.getViewManager().setWindowCenterLocation(uploadMonitorDialog);
 		uploadMonitorDialog.setVisible(true);
 	}
 	
 	public void selectedMonitorPictureFile(UploadMonitorDialog dialog) throws Exception{
 		JFileChooser jFileChooser = new JFileChooser();
 		if (lastSelectPath != "") {
 			jFileChooser.setCurrentDirectory(new File(lastSelectPath));
 		}
 		jFileChooser.setFileFilter(new FileNameExtensionFilter("*.jpg", new String[]{"jpg"}));
 //		SimpleDateFormat dateformat1=new SimpleDateFormat("yyyyMMddHHmmss_SSS");
 //		String currentTime=dateformat1.format(new Date(Vovo.getLcmUtil().getSystemTime()));
 		int showOpenDialog = jFileChooser.showOpenDialog(dialog);
 		if (showOpenDialog == JFileChooser.APPROVE_OPTION) {
 			File thefile = jFileChooser.getSelectedFile();
 			lastSelectPath = thefile.getAbsolutePath();
 //			dialog.getHightFilePathTextField().setText(jFileChooser.getSelectedFile().getAbsolutePath());
 			dialog.getThumbnailTextField().setText(thefile.getAbsolutePath());
 			
 //			String target = Constants.USER_HOME+"\\lorent\\vovo\\videoclip\\";
 //			String cacheFileName = target+currentTime+"_"+thefile.getName();
 			
 //			FileUtil.fileCopy(thefile.getAbsolutePath(), cacheFileName);
 //			FileUtil.localFileCopy(thefile.getAbsolutePath(), cacheFileName);
 			
 //			File cacheimagefile = new File(cacheFileName);
 			if (thefile.exists()&&thefile.isFile()&&thefile.length()>0) {
 				BufferedImage bufferimage =ImageIO.read(thefile);
 				ImagePainter ip = new ImagePainter(bufferimage);
 				ip.setScaleToFit(true);
 				ip.setScaleType(ScaleType.Distort);
 				dialog.getThumbnailXPanel1().setBackgroundPainter(ip);
 				dialog.setCacheFileName(thefile.getName());
 			}
 		}
 	}
 	
 	private LinkedHashMap<String, String> getVideoPrifilesMap(String target) throws Exception{
 		LinkedHashMap<String, String> resultsMap = new LinkedHashMap<String, String>();
 		String enter = "\r\n\r\n";  
         String separator = " : ";
         String mediainfo = StringUtil.convertFilePath2DOSCommandStr(Constants.USER_DIR+"\\mediainfo\\mediainfo.exe");
         String target1 = StringUtil.convertFilePath2DOSCommandStr(target);
         String cmdStr = "cmd /c "+StringUtil.convertFilePath2DOSCommandStr(mediainfo+" "+target1);
         log.info(cmdStr);
 		Process startProcess = ProcessUtil.getInstance().startProcess(cmdStr);
 		byte b[] = new byte[1024];
         int r = 0;
         StringBuffer resultBuffer = new StringBuffer();
 		while ((r = startProcess.getInputStream().read(b, 0, 1024)) > -1) {
             resultBuffer.append(new String(b, 0, r));
         }
 		startProcess.waitFor();
 		
 		String resultStr = resultBuffer.toString();
 		log.info(resultStr);
 		
 		if(resultStr.equals("") == false ){  
             String[] results = resultStr.split(enter);  
             for (String part  : results) {  
                 int index = part.indexOf("\r\n");  
                 String prefix = part.substring(0, index);  
                 String fragment = part.substring(index);  
                 String[] fragmentArray = fragment.split("\r\n");  
                   
                 for (String subFragment : fragmentArray) {  
                       
                     String[] stringArray = subFragment.split(separator);  
                     if (stringArray.length > 1) {  
                         resultsMap.put(prefix + "_"+stringArray[0].trim(), stringArray[1].trim());  
                     }  
                 }  
             }  
         }  
 		return resultsMap;
 	}
 	
 	private boolean checkVideoFile(LinkedHashMap<String, String> videoPrifilesMap,Constants.VideoDefinition definitaion) throws Exception{
 		//mpeg-4
 		String mpeg4 = videoPrifilesMap.get("General_Format");
 		if (mpeg4 == null || !mpeg4.equals("MPEG-4")) {
 			showErrorDialog(VovoStringUtil.getUIString("VideoClipController.infoTip"), VovoStringUtil.getUIString("VideoClipController.needselectMPEG4"));
 			return false;
 		}
 		//avc(h264) 
 		String avc = videoPrifilesMap.get("Video_Format");
 		if (avc == null || !avc.equals("AVC")) {
 			showErrorDialog(VovoStringUtil.getUIString("VideoClipController.infoTip"), VovoStringUtil.getUIString("VideoClipController.needselectH264"));
 			return false;
 		}
 		//分辨率
 		String width = videoPrifilesMap.get("Video_Width");
 		String height = videoPrifilesMap.get("Video_Height");
 		int nwidth = 0;
 		int nheight = 0;
 		if (width != null || height != null) {
 			String trim = width.substring(0, width.indexOf("pixels")-1).trim().replace(" ", "");
 			nwidth = Integer.parseInt(trim);
 			String trim1 = height.substring(0, height.indexOf("pixels")-1).trim().replace(" ", "");
 			nheight = Integer.parseInt(trim1);
 		}
 		
 		//码流
 		String bitrate = videoPrifilesMap.get("Video_Bit rate");
 		double nbitrate = 0;
 		if (bitrate != null) {
 			String trim = bitrate.substring(0, bitrate.indexOf("Kbps")-1).trim().replace(" ", "");
 			nbitrate = Double.parseDouble(trim);
 		}
 		
 		double BITTATE = Constants.MAXBITRATE_VIDEOHIGH;
 		if (definitaion.equals(Constants.VideoDefinition.Hyper)) {
 			BITTATE = Constants.MAXBITRATE_VIDEOHYPER;
 		}
 		else if(definitaion.equals(Constants.VideoDefinition.Other)){
 			BITTATE = Constants.MAXBITRATE_VIDEOHYPER;
 		}
 		if (nbitrate > BITTATE) {
 			String format = MessageFormat.format(VovoStringUtil.getUIString("VideoClipController.bitrateTooSmall"), BITTATE+"");
 			showErrorDialog(VovoStringUtil.getUIString("VideoClipController.infoTip"), format);
 			return false;
 		}
 		
 		if (definitaion.equals(Constants.VideoDefinition.High)) {
 			// 720P 1280 x 720
 			if (nwidth != 1280 && nheight != 720) {
 				showErrorDialog(VovoStringUtil.getUIString("VideoClipController.infoTip"), VovoStringUtil.getUIString("VideoClipController.needselect720P"));
 				return false; 
 			}
 		}
 		else if(definitaion.equals(Constants.VideoDefinition.Standard)){
 			if (nwidth > 640 || nheight > 480) {
 				showErrorDialog(VovoStringUtil.getUIString("VideoClipController.infoTip"), VovoStringUtil.getUIString("VideoClipController.needselect480Plow"));
 				return false;
 			}
 		}
 		else if(definitaion.equals(Constants.VideoDefinition.Hyper)){
 			if (nwidth != 1920 && nheight != 1080) {
 				showErrorDialog(VovoStringUtil.getUIString("VideoClipController.infoTip"), VovoStringUtil.getUIString("VideoClipController.needselect1080P"));
 				return false; 
 			}
 		}
 		else if(definitaion.equals(Constants.VideoDefinition.Other)){
 			if (nwidth > 1920 && nheight > 1080) {
 				showErrorDialog(VovoStringUtil.getUIString("VideoClipController.infoTip"), "请使用等于或低于1080P的视频文件");
 				return false;
 			}
 		}
 		return true;
 	}
 	
 	private String getDuration(String durationStr) throws Exception{
 		String duration = durationStr;
 		String shours = "";
 		String sminutes = "";
 		String sseconds = "";
 		if (duration.indexOf("h") != -1) {
 			String trim1 = duration.substring(0, duration.indexOf("h")).trim();
 			Integer hours = 0;
 			hours = Integer.parseInt(trim1);
 			if (hours < 10) {
 				shours = "0"+hours;
 			}
 			else{
 				shours = trim1;
 			}
 			duration = duration.substring(duration.indexOf("h")+1);
 		}
 		else{
 			shours = "00";
 		}
 		if (duration.indexOf("mn") != -1) {
 			String trim2 = duration.substring(0, duration.indexOf("mn")).trim();
 			Integer minutes = 0;
 			minutes = Integer.parseInt(trim2);
 			if (minutes < 10) {
 				sminutes = "0"+minutes;
 			}
 			else{
 				sminutes = trim2;
 			}
 			duration = duration.substring(duration.indexOf("mn")+2);
 		}
 		else{
 			sminutes = "00";
 		}
 		if (duration.indexOf("s") != -1) {
 			String trim3 = duration.substring(0,duration.indexOf("s")).trim();
 			Integer seconds = 0;
 			seconds = Integer.parseInt(trim3);
 			if (seconds < 10) {
 				sseconds = "0"+seconds;
 			}
 			else{
 				sseconds = trim3;
 			}
 		}
 		else{
 			sseconds = "00";
 		}
 		return shours+":"+sminutes+":"+sseconds;
 	}
 	
 	public void selectedYunLianVideoClipPicture(final UploadYunLianVideoClipDialog dialog) throws Exception{
 		SwingUtilities.invokeLater(new Runnable() {
 			
 			@Override
 			public void run() {
 				JFileChooser jFileChooser = new JFileChooser();
 				if (lastSelectPath != "") {
 					jFileChooser.setCurrentDirectory(new File(lastSelectPath));
 				}
 				jFileChooser.setFileFilter(new FileNameExtensionFilter("*.jpg", new String[]{"jpg"}));
 				if (dialog.getCurrentTime() == null || dialog.getCurrentTime().equals("")) {
 					JOptionPane.showMessageDialog(null, "请先选择超清文件");
 					return;
 				}
 				int showOpenDialog = jFileChooser.showOpenDialog(dialog);
 				if (showOpenDialog == JFileChooser.APPROVE_OPTION) {
 					File thefile = jFileChooser.getSelectedFile();
 					lastSelectPath = thefile.getAbsolutePath();
 					String target = Constants.USER_HOME+"\\lorent\\vovo\\videoclip\\";
 					File file = new File(target);
 					if (!file.exists()) {
 						file.mkdirs();
 					}
 					String cacheFileName = dialog.getThumbnailImageFilePath();
 					FileUtil.localFileCopy(jFileChooser.getSelectedFile().getAbsolutePath(), cacheFileName);
 					
 					File cacheimagefile = new File(cacheFileName);
 					if (cacheimagefile.exists()) {
 						try {
 							BufferedImage bufferimage =ImageIO.read(cacheimagefile);
 							ImagePainter ip = new ImagePainter(bufferimage);
 							ip.setScaleToFit(true);
 							ip.setScaleType(ScaleType.Distort);
 							dialog.getThumbnailXPanel1().setBackgroundPainter(ip);
 						} catch (Exception e) {
 							log.error("selectedVideoClipPicture", e);
 						}
 					}
 				}
 			}
 		});
 	}
 	
 	public void selectedVideoClipPicture(final UploadVideoClipDialog dialog) throws Exception{
 		
 		SwingUtilities.invokeLater(new Runnable() {
 			
 			@Override
 			public void run() {
 				JFileChooser jFileChooser = new JFileChooser();
 				if (lastSelectPath != "") {
 					jFileChooser.setCurrentDirectory(new File(lastSelectPath));
 				}
 				jFileChooser.setFileFilter(new FileNameExtensionFilter("*.jpg", new String[]{"jpg"}));
 				if (dialog.getCurrentTime() == null || dialog.getCurrentTime().equals("")) {
 					JOptionPane.showMessageDialog(null, "请先选择超清文件");
 					return;
 				}
 				int showOpenDialog = jFileChooser.showOpenDialog(dialog);
 				if (showOpenDialog == JFileChooser.APPROVE_OPTION) {
 					File thefile = jFileChooser.getSelectedFile();
 					lastSelectPath = thefile.getAbsolutePath();
 					String target = Constants.USER_HOME+"\\lorent\\vovo\\videoclip\\";
 					File file = new File(target);
 					if (!file.exists()) {
 						file.mkdirs();
 					}
 					String cacheFileName = dialog.getThumbnailImageFilePath();
 					FileUtil.localFileCopy(jFileChooser.getSelectedFile().getAbsolutePath(), cacheFileName);
 					
 					File cacheimagefile = new File(cacheFileName);
 					if (cacheimagefile.exists()) {
 						try {
 							BufferedImage bufferimage =ImageIO.read(cacheimagefile);
 							ImagePainter ip = new ImagePainter(bufferimage);
 							ip.setScaleToFit(true);
 							ip.setScaleType(ScaleType.Distort);
 							dialog.getThumbnailXPanel1().setBackgroundPainter(ip);
 						} catch (Exception e) {
 							log.error("selectedVideoClipPicture", e);
 						}
 					}
 				}
 			}
 		});
 	}
 	
 	public void selectedYunLianVideoClipFile(UploadYunLianVideoClipDialog dialog) throws Exception{
 		JFileChooser jFileChooser = new JFileChooser();
 		if (lastSelectPath != "") {
 			jFileChooser.setCurrentDirectory(new File(lastSelectPath));
 		}
 		jFileChooser.setFileFilter(new FileNameExtensionFilter("*.mp4", new String[]{"mp4"}));
 		//改为获得服务器时间
 		SimpleDateFormat dateformat1=new SimpleDateFormat("yyyyMMddHHmmss_SSS");
 		String currentTime=dateformat1.format(new Date(Vovo.getLcmUtil().getSystemTime()));
 		int showOpenDialog = jFileChooser.showOpenDialog(dialog);
 		if (showOpenDialog == JFileChooser.APPROVE_OPTION) {
 			File thefile = jFileChooser.getSelectedFile();
 			lastSelectPath = thefile.getAbsolutePath();
 			//检测文件
 			LinkedHashMap<String, String> videoPrifilesMap = getVideoPrifilesMap(thefile.getAbsolutePath());
 			boolean checkVideoFile = checkVideoFile(videoPrifilesMap,Constants.VideoDefinition.Other);
 			if (checkVideoFile == false) {
 				return;
 			}
 			String duration = videoPrifilesMap.get("General_Duration");
 			String duration2 = getDuration(duration);
 			dialog.getDurationLabel().setText("时长："+duration2);
 			dialog.setDuration(duration2);
 			//ffmpeg
 			String ffmpeg = StringUtil.convertFilePath2DOSCommandStr(Constants.USER_DIR+"\\ffmpeg\\ffmpeg.exe");
 			String target = Constants.USER_HOME+"\\lorent\\vovo\\videoclip\\";
 			File file = new File(target);
 			if (!file.exists()) {
 				file.mkdirs();
 			}
 			String selectedFile = StringUtil.convertFilePath2DOSCommandStr(jFileChooser.getSelectedFile().getAbsolutePath());
 			String cacheFileName = target+currentTime+"_"+""+".jpg";
 			dialog.setCurrentTime(currentTime);
 			String targetPath = StringUtil.convertFilePath2DOSCommandStr(cacheFileName);
 			String cmdStr ="cmd /c " + StringUtil.convertFilePath2DOSCommandStr(ffmpeg+" -i "+selectedFile+" -ss 3 -vframes 1 -r 1 -ac 1 -ab 2 -s 300x420 -f mjpeg "+targetPath);
 			log.info(cmdStr);
 			Process startProcess = ProcessUtil.getInstance().startProcess(cmdStr);
 			byte b[] = new byte[1024];
             int r = 0;
             StringBuffer resultBuffer = new StringBuffer();
 			while ((r = startProcess.getErrorStream().read(b, 0, 1024)) > -1) {
                 resultBuffer.append(new String(b, 0, r));
             }
 			startProcess.waitFor();
 			String resultStr = resultBuffer.toString();
 			log.info(resultStr);
 			
 //			mp4box(thefile.getAbsolutePath());
 			
 			File cacheimagefile = new File(cacheFileName);
 			if (cacheimagefile.exists()) {
 				BufferedImage bufferimage =ImageIO.read(cacheimagefile);
 				ImagePainter ip = new ImagePainter(bufferimage);
 				ip.setScaleToFit(true);
 				ip.setScaleType(ScaleType.Distort);
 				dialog.getThumbnailXPanel1().setBackgroundPainter(ip);
 				dialog.setThumbnailImageFilePath(cacheFileName);
 			}
 			dialog.getHyperFilePathTextField().setText(jFileChooser.getSelectedFile().getAbsolutePath());
 		}
 	}
 	
 	public void selectedVideoClipFile(UploadVideoClipDialog dialog,Constants.VideoDefinition definitaion) throws Exception{
 		JFileChooser jFileChooser = new JFileChooser();
 		if (lastSelectPath != "") {
 			jFileChooser.setCurrentDirectory(new File(lastSelectPath));
 		}
 		jFileChooser.setFileFilter(new FileNameExtensionFilter("*.mp4", new String[]{"mp4"}));
 		//改为获得服务器时间
 		SimpleDateFormat dateformat1=new SimpleDateFormat("yyyyMMddHHmmss_SSS");
 		String currentTime=dateformat1.format(new Date(Vovo.getLcmUtil().getSystemTime()));
 		int showOpenDialog = jFileChooser.showOpenDialog(dialog);
 		if (showOpenDialog == JFileChooser.APPROVE_OPTION) {
 			File thefile = jFileChooser.getSelectedFile();
 			lastSelectPath = thefile.getAbsolutePath();
 			//检查文件名是否有特殊字符
 			if (thefile.getName().indexOf(" ") != -1) {
 				JOptionPane.showMessageDialog(null, "文件名有不符合的字符。");
 				return;
 			}
 			//检查文件大小，不能超过4G
 			long filelength = thefile.length();
 			if (filelength >= Constants.MAXVIDEOFILESIZE) {
 				JOptionPane.showMessageDialog(null, "文件大小超出限制。");
 				return;
 			}
 			
 			//检测文件
 			LinkedHashMap<String, String> videoPrifilesMap = getVideoPrifilesMap(thefile.getAbsolutePath());
 			boolean checkVideoFile = checkVideoFile(videoPrifilesMap,definitaion);
 			if (checkVideoFile == false) {
 				return;
 			}
 			String duration = videoPrifilesMap.get("General_Duration");
 			String duration2 = getDuration(duration);
 			dialog.getDurationLabel().setText("时长："+duration2);
 			dialog.setDuration(duration2);
 			if (definitaion.equals(Constants.VideoDefinition.High)) {
 //				mp4box(thefile.getAbsolutePath());
 				dialog.setSelectedHighVideoFilePath(thefile.getAbsolutePath());
 				dialog.getHightFilePathTextField().setText(jFileChooser.getSelectedFile().getAbsolutePath());
 			}
 			else if(definitaion.equals(Constants.VideoDefinition.Standard)){
 //				mp4box(thefile.getAbsolutePath());
 				dialog.setSelectedStandardVideoFilePath(thefile.getAbsolutePath());
 				dialog.getStandardFilePathTextField().setText(jFileChooser.getSelectedFile().getAbsolutePath());
 			}
 			else if(definitaion.equals(Constants.VideoDefinition.Hyper)){
 				//ffmpeg
 				String ffmpeg = StringUtil.convertFilePath2DOSCommandStr(Constants.USER_DIR+"\\ffmpeg\\ffmpeg.exe");
 				//ffmpeg -i "flvplayer.flv" -y -f image2 -t 0.001 -s 352x240 %home%\lorent\vovo\videoclip\test.jpg
 				String target = Constants.USER_HOME+"\\lorent\\vovo\\videoclip\\";
 				File file = new File(target);
 				if (!file.exists()) {
 					file.mkdirs();
 				}
 				String selectedFile = StringUtil.convertFilePath2DOSCommandStr(jFileChooser.getSelectedFile().getAbsolutePath());
 //				String newTheFileName = thefile.getName().replace(".", "_").replace(" ", "_");
 //				newTheFileName = URLEncoder.encode(newTheFileName);
 				String cacheFileName = target+currentTime+"_"+""+".jpg";
 				dialog.setCurrentTime(currentTime);
 				String targetPath = StringUtil.convertFilePath2DOSCommandStr(cacheFileName);
 				String cmdStr ="cmd /c " + StringUtil.convertFilePath2DOSCommandStr(ffmpeg+" -i "+selectedFile+" -ss 3 -vframes 1 -r 1 -ac 1 -ab 2 -s 300x420 -f mjpeg "+targetPath);
 				log.info(cmdStr);
 				Process startProcess = ProcessUtil.getInstance().startProcess(cmdStr);
 				byte b[] = new byte[1024];
 	            int r = 0;
 	            StringBuffer resultBuffer = new StringBuffer();
 				while ((r = startProcess.getErrorStream().read(b, 0, 1024)) > -1) {
 	                resultBuffer.append(new String(b, 0, r));
 	            }
 				startProcess.waitFor();
 				String resultStr = resultBuffer.toString();
 				log.info(resultStr);
 				
 //				mp4box(thefile.getAbsolutePath());
 				
 				File cacheimagefile = new File(cacheFileName);
 				if (cacheimagefile.exists()) {
 					BufferedImage bufferimage =ImageIO.read(cacheimagefile);
 					ImagePainter ip = new ImagePainter(bufferimage);
 					ip.setScaleToFit(true);
 					ip.setScaleType(ScaleType.Distort);
 					dialog.getThumbnailXPanel1().setBackgroundPainter(ip);
 					dialog.setThumbnailImageFilePath(cacheFileName);
 				}
 				dialog.getHyperFilePathTextField().setText(jFileChooser.getSelectedFile().getAbsolutePath());
 			}
 		}
 	}
 	
 	private void mp4boxYunLian(String targetFilePath,UploadYunLianVideoClipDialog dialog) throws Exception{
 		//mp4box
 		byte b[] = new byte[1024];
         int r = 0;
 		String mp4box = StringUtil.convertFilePath2DOSCommandStr(Constants.USER_DIR+"\\GPAC\\mp4box.exe");
 		String targetPath = StringUtil.convertFilePath2DOSCommandStr(targetFilePath);
 		String cmdStr0 = "cmd /c "+StringUtil.convertFilePath2DOSCommandStr(mp4box +" -unhint "+targetPath);
 		String cmdStr = "cmd /c "+StringUtil.convertFilePath2DOSCommandStr(mp4box +" -hint "+targetPath);
 		System.out.println(cmdStr);
 		log.info(cmdStr);
 		Process startProcess1 = ProcessUtil.getInstance().startProcess(cmdStr0);
 		while ((r = startProcess1.getInputStream().read(b, 0, 1024)) > -1) {
 			String string = new String(b, 0, r);
 			dialog.getResultProgressBar().setString(string);
             log.info(string);
         }
 		startProcess1.waitFor();
 		
 		startProcess1 = ProcessUtil.getInstance().startProcess(cmdStr);
 		while ((r = startProcess1.getInputStream().read(b, 0, 1024)) > -1) {
 			String string = new String(b, 0, r);
 			dialog.getResultProgressBar().setString(string);
             log.info(string);
         }
 		startProcess1.waitFor();
 	}
 	
 	private void mp4box(String targetFilePath,UploadVideoClipDialog dialog) throws Exception{
 		//mp4box
 		byte b[] = new byte[1024];
         int r = 0;
 		String mp4box = StringUtil.convertFilePath2DOSCommandStr(Constants.USER_DIR+"\\GPAC\\mp4box.exe");
 		String targetPath = StringUtil.convertFilePath2DOSCommandStr(targetFilePath);
 		String cmdStr0 = "cmd /c "+StringUtil.convertFilePath2DOSCommandStr(mp4box +" -unhint "+targetPath);
 		String cmdStr = "cmd /c "+StringUtil.convertFilePath2DOSCommandStr(mp4box +" -hint "+targetPath);
 		System.out.println(cmdStr);
 		log.info(cmdStr);
 		Process startProcess1 = ProcessUtil.getInstance().startProcess(cmdStr0);
 		while ((r = startProcess1.getInputStream().read(b, 0, 1024)) > -1) {
 			String string = new String(b, 0, r);
 			dialog.getResultProgressBar().setString(string);
             log.info(string);
         }
 		startProcess1.waitFor();
 		
 		startProcess1 = ProcessUtil.getInstance().startProcess(cmdStr);
 		while ((r = startProcess1.getInputStream().read(b, 0, 1024)) > -1) {
 			String string = new String(b, 0, r);
 			dialog.getResultProgressBar().setString(string);
             log.info(string);
         }
 		startProcess1.waitFor();
 	}
 	
 	class FTPDataTransferAdater implements FTPDataTransferListener{
 		@Override
 		public void aborted() {
 		}
 		@Override
 		public void completed() {
 		}
 		@Override
 		public void failed() {
 		}
 		@Override
 		public void started() {
 		}
 		@Override
 		public void transferred(int transferedLength) {
 		}
 	}
 	
 	public void cancelUpLoadVideoClip(UploadVideoClipDialog dialog) throws Exception{
 		
 		if (uploadSwingWorker != null) {
 //			uploadSwingWorker.cancel(true);
 			uploadSwingWorker = null;
 			
 			synchronized (dialog.uploading) {
 				dialog.uploading = false;
 			}
 			Vovo.exeC("sharefile", "stopTransferAtFtpServer",dialog.getCurrentTime());
 			dialog.getCancelButton().setEnabled(false);
 			Thread.sleep(1000);
 			
 //			return;
 		}
 		
 		//删除超清文件
 		String filenameHyper = FileUtil.getFileNameFromFilePath(dialog.getNewHyperVideoFileName());
 		if (filenameHyper != null && !filenameHyper.equals("")) {
 			try {
 				Vovo.exeC("sharefile", "deleteFileAtFtpServer", "/VideoClips", filenameHyper);
 			} catch (Exception e) {
 				log.error("cancelUpLoadVideoClip", e);
 			}
 			String fileNameHyperFromFilePath = FileUtil.getFileNameFromFilePath(dialog.getHyperFilePathTextField().getText());
 			try {
 				Vovo.exeC("sharefile", "deleteFileAtFtpServer", "/VideoClips", fileNameHyperFromFilePath);
 			} catch (Exception e) {
 				log.error("cancelUpLoadVideoClip", e);
 			}
 		}
 		
 		//删除高清文件
 		String filenameHigh = FileUtil.getFileNameFromFilePath(dialog.getNewHighVideoFileName());
 		if (filenameHigh != null && !filenameHigh.equals("")) {
 			try {
 				Vovo.exeC("sharefile", "deleteFileAtFtpServer", "/VideoClips",filenameHigh);
 			} catch (Exception e) {
 				log.error("cancelUpLoadVideoClip", e);
 			}
 			String fileNameHighFromFilePath = FileUtil.getFileNameFromFilePath(dialog.getHightFilePathTextField().getText());
 			try {
 				Vovo.exeC("sharefile", "deleteFileAtFtpServer", "/VideoClips",fileNameHighFromFilePath);
 			} catch (Exception e) {
 				log.error("cancelUpLoadVideoClip", e);
 			}
 		}
 		
 		//删除标清文件
 		String filenameStandard = FileUtil.getFileNameFromFilePath(dialog.getNewStandardVideoFileName());
 		if (filenameStandard != null && !filenameStandard.equals("")) {
 			try {
 				Vovo.exeC("sharefile", "deleteFileAtFtpServer", "/VideoClips", filenameStandard);
 			} catch (Exception e) {
 				log.error("deleteVideoClip", e);
 			}
 			String fileNameStandardFromFilePath = FileUtil.getFileNameFromFilePath(dialog.getStandardFilePathTextField().getText());
 			try {
 				Vovo.exeC("sharefile", "deleteFileAtFtpServer", "/VideoClips", fileNameStandardFromFilePath);
 			} catch (Exception e) {
 				log.error("deleteVideoClip", e);
 			}
 		}
 		
 		//删除缩略图
 		String filenameThumbnail = FileUtil.getFileNameFromURL(dialog.getThumbnailImageFilePath());
 		if (filenameThumbnail != null && !filenameThumbnail.equals("")) {
 			try {
 				Vovo.exeC("sharefile", "deleteFileAtFtpServer", "/VideoClips", filenameThumbnail);
 			} catch (Exception e) {
 				log.error("deleteVideoClip", e);
 			}
 		}
 //		dialog.setUIEnable();
 		dialog.dispose();
 	}
 	
 	public void uploadYunLianVideoClip(final UploadYunLianVideoClipDialog dialog) throws Exception{
 		//检测输入
 		String title = dialog.getTitleTextField().getText();
 		if (title == null || title.equals("")) {
 			showErrorDialog(VovoStringUtil.getUIString("VideoClipController.infoTip"), VovoStringUtil.getUIString("VideoClipController.needTitle"));
 			return;
 		}
 		else if (title.length() >= 100) {
 			showErrorDialog(VovoStringUtil.getUIString("VideoClipController.infoTip"), VovoStringUtil.getUIString("VideoClipController.titleTooLong"));
 			return;
 		}
 		String description = dialog.getDescriptionTextArea().getText();
 		if (description == null || description.equals("")) {
 			showErrorDialog(VovoStringUtil.getUIString("VideoClipController.infoTip"), VovoStringUtil.getUIString("VideoClipController.needDescription"));
 			return;
 		}
 		else if (description.length() >= 255) {
 			showErrorDialog(VovoStringUtil.getUIString("VideoClipController.infoTip"), VovoStringUtil.getUIString("VideoClipController.descriptionTooLong"));
 			return;
 		}
 		
 		dialog.setUIDisable();
 		//上传文件至ftp
 		//缩略图
 		final File thumbailfile = new File(dialog.getThumbnailImageFilePath());
 		final long thumbailfilesize = thumbailfile.length();
 		final String uiString1 = "步骤2: "+ thumbailfile.getName() + "  {0} %";
 		final FTPDataTransferListener thumbnailDataTransferListener = new FTPDataTransferAdater(){
 			private long curentLength = 0;
 			@Override
 			public void transferred(int transferedLength) {
 				curentLength = curentLength + transferedLength;
 				double d = 1;
 				d = 100 * curentLength / thumbailfilesize;
 		        int  n = (int) d;
 		        dialog.getResultProgressBar().setValue(n);
 		        dialog.getResultProgressBar().setString(MessageFormat.format(uiString1, n));
 			}
 		};
 		//超清文件
 		final File fileHyper = new File(dialog.getHyperFilePathTextField().getText());
 		final String newFileNameHyper =  dialog.getCurrentTime()+"_1080P.mp4";//+fileHyper.getName();
 		final long filesizeHyper = fileHyper.length();
 		final String uiStrHyper = "步骤0: "+fileHyper.getName() +"  {0} %";
 		final FTPDataTransferListener ftpDataTransListenerHyper = new FTPDataTransferAdater(){
 			private long curentLength = 0;
 			@Override
 			public void transferred(int transferedLength) {
 				curentLength = curentLength + transferedLength;
 				double d = 1;
 				d = 100 * curentLength / filesizeHyper;
 		        int  n = (int) d;
 		        dialog.getResultProgressBar().setValue(n);
 		        dialog.getResultProgressBar().setString(MessageFormat.format(uiStrHyper, n));
 			}
 		};
 		
 		//检测ftp上是否已存在文件名
 		Boolean file2isexist = (Boolean) Vovo.exeC("sharefile", "checkFileExistInFtpServer","/VideoClips", fileHyper.getName());
 		if (file2isexist) {
 			JOptionPane.showMessageDialog(null, "服务器已存在文件名："+fileHyper.getName());
 			dialog.setUIEnable();
 			return;
 		}
 		//检测文件是否正在打开
 		boolean fileIsOpened = false;
 		fileIsOpened = FileUtil.fileIsOpened(fileHyper.getAbsolutePath());
 		if (fileIsOpened) {
 			JOptionPane.showMessageDialog(null, "文件已被打开："+fileHyper.getAbsolutePath());
 			dialog.setUIEnable();
 			return;
 		}
 		uploadSwingWorker = new SwingWorker<Object, Object>(){
             @Override
             protected Object doInBackground() throws Exception {
         		dialog.getResultProgressBar().setString(fileHyper.getAbsolutePath());
         		mp4boxYunLian(fileHyper.getAbsolutePath(),dialog);
         		Thread.sleep(2000);
             	
             	Vovo.exeC("sharefile", "upLoadFileToFtpServer", fileHyper,ftpDataTransListenerHyper,"/VideoClips",newFileNameHyper);
             	Vovo.exeC("sharefile", "upLoadFileToFtpServer", thumbailfile,thumbnailDataTransferListener,"/VideoClips","");
             	String ftpAddr = (String) Vovo.exeC("sharefile", "getFtpAddr");
             	Integer ftpPort = (Integer) Vovo.exeC("sharefile", "getFtpPort");
             	LoginInfo info = Vovo.getMyContext().getDataManager().getValue(Constants.DataKey.LOGGININFO.toString());
             	VovoMyInfo vovoinfo = Vovo.getLcmUtil().getVovoMyInfo(info.getUsername());
             	boolean uploadVideoClipInfo = Vovo.getLcmUtil().uploadVideoClipInfo(newFileNameHyper,newFileNameHyper,newFileNameHyper, thumbailfile.getName(), dialog.getTitleTextField().getText(),dialog.getDescriptionTextArea().getText(),ftpAddr,vovoinfo.getRealName(),vovoinfo.getUsername(),dialog.getDuration(),dialog.getCategoryComboBox().getSelectedItem().toString());
             	if (uploadVideoClipInfo) {
 //					System.out.println(uploadVideoClipInfo);
 					JOptionPane.showMessageDialog(null, VovoStringUtil.getUIString("VideoClipController.uploadSuccess"));
 					dialog.dispose();
 					reflashVideoClipPanel();
 				}
                 return new Object();
             }
         };
         uploadSwingWorker.execute();
 	}
 	
 	public void uploadVideoClip(final UploadVideoClipDialog dialog) throws Exception{
 		//检测输入
 		String highFilePath = dialog.getHightFilePathTextField().getText();
 		if (highFilePath ==  null || highFilePath.equals("")) {
 			showErrorDialog(VovoStringUtil.getUIString("VideoClipController.infoTip"), VovoStringUtil.getUIString("VideoClipController.needselectFileHigh"));
 			return;
 		}
 		String standardFilePath = dialog.getStandardFilePathTextField().getText();
 		if (standardFilePath == null || standardFilePath.equals("")) {
 			showErrorDialog(VovoStringUtil.getUIString("VideoClipController.infoTip"), VovoStringUtil.getUIString("VideoClipController.needselectFileStandard"));
 			return;
 		}
 		String title = dialog.getTitleTextField().getText();
 		if (title == null || title.equals("")) {
 			showErrorDialog(VovoStringUtil.getUIString("VideoClipController.infoTip"), VovoStringUtil.getUIString("VideoClipController.needTitle"));
 			return;
 		}
 		else if (title.length() >= 100) {
 			showErrorDialog(VovoStringUtil.getUIString("VideoClipController.infoTip"), VovoStringUtil.getUIString("VideoClipController.titleTooLong"));
 			return;
 		}
 		String description = dialog.getDescriptionTextArea().getText();
 		if (description == null || description.equals("")) {
 			showErrorDialog(VovoStringUtil.getUIString("VideoClipController.infoTip"), VovoStringUtil.getUIString("VideoClipController.needDescription"));
 			return;
 		}
 		else if (description.length() >= 255) {
 			showErrorDialog(VovoStringUtil.getUIString("VideoClipController.infoTip"), VovoStringUtil.getUIString("VideoClipController.descriptionTooLong"));
 			return;
 		}
 		
 		dialog.setUIDisable();
 		//上传文件至ftp
 		//缩略图
 		final File thumbailfile = new File(dialog.getThumbnailImageFilePath());
 		final long thumbailfilesize = thumbailfile.length();
 		final String uiString1 = "步骤2: "+ thumbailfile.getName() + "  {0} %";
 		final String thumbailsessionID = dialog.getCurrentTime()+"_thumbail";
 		final FTPDataTransferListener thumbnailDataTransferListener = new FTPDataTransferAdater(){
 			private long curentLength = 0;
 			private String sessionID = thumbailsessionID;
 			@Override
 			public void transferred(int transferedLength) {
 				curentLength = curentLength + transferedLength;
 				double d = 1;
 				d = 100 * curentLength / thumbailfilesize;
 		        int  n = (int) d;
 		        dialog.getResultProgressBar().setValue(n);
 		        dialog.getResultProgressBar().setString(MessageFormat.format(uiString1, n));
 			}
 			@Override
 			public void completed() {
 				Vovo.exeC("sharefile", "removeFtpTransferStatus", sessionID);
 			}
 			
 		};
 		//高清文件
 		final File fileHigh = new File(dialog.getSelectedHighVideoFilePath());
 		final String newFileNameHigh = dialog.getCurrentTime()+"_720P.mp4";//+ fileHigh.getName();
 		final long filesize = fileHigh.length();
 		final String uiString2 = "步骤1: "+fileHigh.getName() +"  {0} %";
 		final String fileHighsessionID = dialog.getCurrentTime()+"_fileHigh";
 		dialog.setNewHighVideoFileName(newFileNameHigh);
 		final FTPDataTransferListener ftpDataTransferListener = new FTPDataTransferAdater(){
 			private long curentLength = 0;
 			private String sessionID = fileHighsessionID;
 			@Override
 			public void transferred(int transferedLength) {
 				curentLength = curentLength + transferedLength;
 				double d = 1;
 				d = 100 * curentLength / filesize;
 		        int  n = (int) d;
 		        dialog.getResultProgressBar().setValue(n);
 		        dialog.getResultProgressBar().setString(MessageFormat.format(uiString2, n));
 			}
 			@Override
 			public void completed() {
 				Vovo.exeC("sharefile", "removeFtpTransferStatus", sessionID);
 			}
 		};
 		//标清文件
 		final File fileStandard = new File(dialog.getSelectedStandardVideoFilePath());
 		final String newFileNameStandard =dialog.getCurrentTime()+"_480P.mp4";//+ fileStandard.getName();
 		final long filesize1 = fileStandard.length();
 		final String uiString3 = "步骤3: "+fileStandard.getName() +"  {0} %";
 		final String fileStandardsessionID = dialog.getCurrentTime()+"_fileStandard";
 		dialog.setNewStandardVideoFileName(newFileNameStandard);
 		final FTPDataTransferListener ftpDataTransferListener1 = new FTPDataTransferAdater(){
 			private long curentLength = 0;
 			private String sessionID = fileStandardsessionID;
 			@Override
 			public void transferred(int transferedLength) {
 				curentLength = curentLength + transferedLength;
 				double d = 1;
 				d = 100 * curentLength / filesize1;
 		        int  n = (int) d;
 		        dialog.getResultProgressBar().setValue(n);
 		        dialog.getResultProgressBar().setString(MessageFormat.format(uiString3, n));
 			}
 			@Override
 			public void completed() {
 				Vovo.exeC("sharefile", "removeFtpTransferStatus", sessionID);
 			}
 		};
 		//超清文件
 		final File fileHyper = new File(dialog.getHyperFilePathTextField().getText());
 		final String newFileNameHyper =  dialog.getCurrentTime()+"_1080P.mp4";//+fileHyper.getName();
 		final long filesizeHyper = fileHyper.length();
 		final String uiStrHyper = "步骤0: "+fileHyper.getName() +"  {0} %";
 		final String fileHypersessionID = dialog.getCurrentTime()+"_fileHyper";
 		dialog.setNewHyperVideoFileName(newFileNameHyper);
 		final FTPDataTransferListener ftpDataTransListenerHyper = new FTPDataTransferAdater(){
 			private long curentLength = 0;
 			private String sessionID = fileHypersessionID;
 			@Override
 			public void transferred(int transferedLength) {
 				curentLength = curentLength + transferedLength;
 				double d = 1;
 				d = 100 * curentLength / filesizeHyper;
 		        int  n = (int) d;
 		        dialog.getResultProgressBar().setValue(n);
 		        dialog.getResultProgressBar().setString(MessageFormat.format(uiStrHyper, n));
 			}
 			@Override
 			public void completed() {
 				Vovo.exeC("sharefile", "removeFtpTransferStatus", sessionID);
 			}
 		};
 		
 		//检测ftp上是否已存在文件名
 		Boolean fileisexist = (Boolean) Vovo.exeC("sharefile", "checkFileExistInFtpServer","/VideoClips", fileHigh.getName());
 		if (fileisexist) {
 			JOptionPane.showMessageDialog(null, "服务器已存在文件名："+fileHigh.getName());
 			dialog.setUIEnable();
 			return;
 		}
 		Boolean file1isexist = (Boolean) Vovo.exeC("sharefile", "checkFileExistInFtpServer","/VideoClips", fileStandard.getName());
 		if (file1isexist) {
 			JOptionPane.showMessageDialog(null, "服务器已存在文件名："+fileStandard.getName());
 			dialog.setUIEnable();
 			return;
 		}
 		Boolean file2isexist = (Boolean) Vovo.exeC("sharefile", "checkFileExistInFtpServer","/VideoClips", fileHyper.getName());
 		if (file2isexist) {
 			JOptionPane.showMessageDialog(null, "服务器已存在文件名："+fileHyper.getName());
 			dialog.setUIEnable();
 			return;
 		}
 		
 		//检测文件是否正在打开
 		boolean fileIsOpened = false;
 		fileIsOpened = FileUtil.fileIsOpened(fileHigh.getAbsolutePath());
 		if (fileIsOpened) {
 			JOptionPane.showMessageDialog(null, "文件已被打开："+fileHigh.getAbsolutePath());
 			dialog.setUIEnable();
 			return;
 		}
 		fileIsOpened = FileUtil.fileIsOpened(fileStandard.getAbsolutePath());
 		if (fileIsOpened) {
 			JOptionPane.showMessageDialog(null, "文件已被打开："+fileStandard.getAbsolutePath());
 			dialog.setUIEnable();
 			return;
 		}
 		fileIsOpened = FileUtil.fileIsOpened(fileHyper.getAbsolutePath());
 		if (fileIsOpened) {
 			JOptionPane.showMessageDialog(null, "文件已被打开："+fileHyper.getAbsolutePath());
 			dialog.setUIEnable();
 			return;
 		}
 		synchronized (dialog.uploading) {
 			dialog.uploading = true;
 		}
 		uploadSwingWorker = new SwingWorker<Object, Object>(){
             @Override
             protected Object doInBackground() throws Exception {
             	dialog.getResultProgressBar().setString(fileHigh.getAbsolutePath());
             	synchronized (dialog.uploading) {
         			if (dialog.uploading == false) {
         				return new Object();
 					}
         		}
             	mp4box(fileHigh.getAbsolutePath(),dialog);
 				Thread.sleep(2000);
         		dialog.getResultProgressBar().setString(fileStandard.getAbsolutePath());
         		synchronized (dialog.uploading) {
         			if (dialog.uploading == false) {
         				return new Object();
 					}
         		}
         		mp4box(fileStandard.getAbsolutePath(),dialog);
         		Thread.sleep(2000);
         		dialog.getResultProgressBar().setString(fileHyper.getAbsolutePath());
         		synchronized (dialog.uploading) {
         			if (dialog.uploading == false) {
         				return new Object();
 					}
         		}
         		mp4box(fileHyper.getAbsolutePath(),dialog);
         		Thread.sleep(2000);
         		synchronized (dialog.uploading) {
         			if (dialog.uploading == false) {
         				return new Object();
 					}
         		}
         		Vovo.exeC("sharefile", "upLoadFileToFtpServer", thumbailsessionID,thumbailfile,thumbnailDataTransferListener,"/VideoClips","");
         		synchronized (dialog.uploading) {
         			if (dialog.uploading == false) {
         				return new Object();
         			}
         		}
         		Vovo.exeC("sharefile", "upLoadFileToFtpServer", fileHypersessionID,fileHyper,ftpDataTransListenerHyper,"/VideoClips",newFileNameHyper);
             	synchronized (dialog.uploading) {
             		if (dialog.uploading == false) {
         				return new Object();
 					}
         		}
             	Vovo.exeC("sharefile", "upLoadFileToFtpServer", fileHighsessionID,fileHigh,ftpDataTransferListener,"/VideoClips",newFileNameHigh);
             	synchronized (dialog.uploading) {
             		if (dialog.uploading == false) {
         				return new Object();
 					}
         		}
             	Vovo.exeC("sharefile", "upLoadFileToFtpServer", fileStandardsessionID,fileStandard,ftpDataTransferListener1,"/VideoClips",newFileNameStandard);
             	synchronized (dialog.uploading) {
             		if (dialog.uploading == false) {
         				return new Object();
 					}
         		}
             	String ftpAddr = (String) Vovo.exeC("sharefile", "getFtpAddr");
             	Integer ftpPort = (Integer) Vovo.exeC("sharefile", "getFtpPort");
             	LoginInfo info = Vovo.getMyContext().getDataManager().getValue(Constants.DataKey.LOGGININFO.toString());
             	VovoMyInfo vovoinfo = Vovo.getLcmUtil().getVovoMyInfo(info.getUsername());
             	boolean uploadVideoClipInfo = Vovo.getLcmUtil().uploadVideoClipInfo(newFileNameHyper,newFileNameHigh,newFileNameStandard, thumbailfile.getName(), dialog.getTitleTextField().getText(),dialog.getDescriptionTextArea().getText(),ftpAddr,vovoinfo.getRealName(),vovoinfo.getUsername(),dialog.getDuration(),dialog.getCategoryComboBox().getSelectedItem().toString());
             	if (uploadVideoClipInfo) {
             		JOptionPane.showMessageDialog(null, VovoStringUtil.getUIString("VideoClipController.uploadSuccess"));
             		dialog.dispose();
             		reflashVideoClipPanel();
             	}
                 return new Object();
             }
         };
         uploadSwingWorker.execute();
 	}
 	
 	public void uploadMonitor(final UploadMonitorDialog dialog) throws Exception{
 		//检查输入
 		String thumbnail = dialog.getThumbnailTextField().getText();
 		if (thumbnail == null || thumbnail.equals("")) {
 			showErrorDialog(VovoStringUtil.getUIString("VideoClipController.infoTip"), VovoStringUtil.getUIString("VideoClipController.needselectThumbnail"));
 			return;
 		}
 		String title = dialog.getTitleTextField().getText();
 		if (title == null || title.equals("")) {
 			showErrorDialog(VovoStringUtil.getUIString("VideoClipController.infoTip"), VovoStringUtil.getUIString("VideoClipController.needTitle"));
 			return;
 		}
 		else if(title.length() >= 100){
 			showErrorDialog(VovoStringUtil.getUIString("VideoClipController.infoTip"), VovoStringUtil.getUIString("VideoClipController.titleTooLong"));
 			return;
 		}
 		String description = dialog.getDescriptionTextArea().getText();
 		if (description == null || description.equals("")) {
 			showErrorDialog(VovoStringUtil.getUIString("VideoClipController.infoTip"), VovoStringUtil.getUIString("VideoClipController.needDescription"));
 			return;
 		}
 		else if(description.length()>=255){
 			showErrorDialog(VovoStringUtil.getUIString("VideoClipController.infoTip"), VovoStringUtil.getUIString("VideoClipController.descriptionTooLong"));
 			return;
 		}
 		String liveUrl = dialog.getLiveStreamURLTextField().getText();
 		if (liveUrl == null || liveUrl.equals("")) {
 			showErrorDialog(VovoStringUtil.getUIString("VideoClipController.infoTip"), VovoStringUtil.getUIString("VideoClipController.needLiveStreamUrl"));
 			return;
 		}
 		else if(liveUrl.length() >=2048){
 			showErrorDialog(VovoStringUtil.getUIString("VideoClipController.infoTip"), VovoStringUtil.getUIString("VideoClipController.liveStreamUrlTooLong"));
 			return;
 		}
 		dialog.getLiveStreamURLTextField().setEnabled(false);
 		dialog.getTitleTextField().setEnabled(false);
 		dialog.getDescriptionTextArea().setEnabled(false);
 		dialog.getUploadButton().setEnabled(false);
 		//上传文件至ftp
 		
 		File file = new File(thumbnail);
 		if(!file.exists()||file.length()<1)
 			return;
 		FileInputStream in = new FileInputStream(file);
 		byte[] data=new byte[(int) file.length()];
 		byte[] temp=new byte[1024*512];
 		int c,index=0;
 		while((c=in.read(temp))>0){
 			System.arraycopy(temp, 0, data, index, c);
 			index+=c;
 		}
 		Monitor monitor = new Monitor(data, liveUrl, title, description);
 		MonitorsUtil.add(monitor);
 //		final File thumbailfile = new File(dialog.getThumbnailTextField().getText());
 //		final String uiString = " "+ thumbailfile.getName() + "  {0} %";
 //		final long thumbailfilesize = thumbailfile.length();
 //		final FTPDataTransferListener thumbnailDataTransferListener = new FTPDataTransferAdater(){
 //			private long curentLength = 0;
 //			@Override
 //			public void transferred(int transferedLength) {
 //				curentLength = curentLength + transferedLength;
 //				double d = 1;
 //				d = 100 * curentLength / thumbailfilesize;
 //		        int  n = (int) d;
 //		        dialog.getResultProgressBar().setValue(n);
 //		        dialog.getResultProgressBar().setString(MessageFormat.format(uiString, n));
 //			}
 //		};
 //		new SwingWorker<Object, Object>(){
 //            @Override
 //            protected Object doInBackground() throws Exception {
 //            	Vovo.exeC("sharefile", "upLoadFileToFtpServer", thumbailfile,thumbnailDataTransferListener,"/Monitor",dialog.getCacheFileName());
 //            	String ftpAddr = (String) Vovo.exeC("sharefile", "getFtpAddr");
 //            	Integer ftpPort = (Integer) Vovo.exeC("sharefile", "getFtpPort");
 //            	LoginInfo info = Vovo.getMyContext().getDataManager().getValue(Constants.DataKey.LOGGININFO.toString());
 //            	VovoMyInfo vovoinfo = Vovo.getLcmUtil().getVovoMyInfo(info.getUsername());
 //            	String liveStreamUrl = dialog.getLiveStreamURLTextField().getText();
 ////            	String thumbnailFtpUrl = "ftp://"+ftpAddr+":"+ftpPort+"/Monitor/"+dialog.getCacheFileName();
 //            	String title = dialog.getTitleTextField().getText();
 //            	String description = dialog.getDescriptionTextArea().getText();
 //            	boolean uploadMonitorInfo = Vovo.getLcmUtil().uploadMonitorInfo(liveStreamUrl, dialog.getCacheFileName(), title, description, ftpAddr, vovoinfo.getRealName(),vovoinfo.getUsername());
 //            	if (uploadMonitorInfo) {
 //					JOptionPane.showMessageDialog(null, VovoStringUtil.getUIString("VideoClipController.uploadSuccess"));
 //					dialog.dispose();
 //					reflashMonitorPanel();
 //				}
 //            	
 //                return new Object();
 //            }
 //        }.execute();
 		SwingUtilities.invokeLater(new Runnable() {
 			public void run() {
 				dialog.dispose();
 				try {
 					reflashMonitorPanel();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 		
 	public void deleteVideoClip(VideoClipItem item) throws Exception{
 		int showConfirmDialog = JOptionPane.showConfirmDialog(null, VovoStringUtil.getUIString("VideoClipController.askConfirmDelete"), VovoStringUtil.getUIString("VideoClipController.infoTip"), JOptionPane.YES_NO_OPTION);
 		if (showConfirmDialog == JOptionPane.YES_OPTION) {
 			
 			if (item.getLcmVideoClip().getIsmonitor()) {
 //				String filenameHigh = FileUtil.getFileNameFromURL(item.getLcmVideoClip().getRtspVideoUrlHigh());
 //				if (filenameHigh != null && !filenameHigh.equals("")) {
 //					Vovo.exeC("sharefile", "deleteFileAtFtpServer", "/Monitor",filenameHigh);
 //				}
 //				Vovo.getLcmUtil().deleteVideoClip(item.getLcmVideoClip().getId());
 				MonitorsUtil.delete((Monitor) item.getLcmVideoClip());
 				reflashMonitorPanel();
 			}
 			else{
 				String filenameHigh = FileUtil.getFileNameFromURL(item.getLcmVideoClip().getRtspVideoUrlHigh());
 				if (filenameHigh != null && !filenameHigh.equals("")) {
 					try {
 						Vovo.exeC("sharefile", "deleteFileAtFtpServer", "/VideoClips",filenameHigh);
 					} catch (Exception e) {
 						log.error("deleteVideoClip", e);
 					}
 				}
 				String filenameStandard = FileUtil.getFileNameFromURL(item.getLcmVideoClip().getRtspVideoUrlStandard());
 				if (filenameStandard != null && !filenameStandard.equals("")) {
 					try {
 						Vovo.exeC("sharefile", "deleteFileAtFtpServer", "/VideoClips", filenameStandard);
 					} catch (Exception e) {
 						log.error("deleteVideoClip", e);
 					}
 				}
 				String filenameThumbnail = FileUtil.getFileNameFromURL(item.getLcmVideoClip().getThumbnailUrl());
 				if (filenameThumbnail != null && !filenameThumbnail.equals("")) {
 					try {
 						Vovo.exeC("sharefile", "deleteFileAtFtpServer", "/VideoClips", filenameThumbnail);
 					} catch (Exception e) {
 						log.error("deleteVideoClip", e);
 					}
 				}
 				String filenameHyper = FileUtil.getFileNameFromURL(item.getLcmVideoClip().getRtspVideoUrlHyper());
 				if (filenameHyper != null && !filenameHyper.equals("")) {
 					try {
 						Vovo.exeC("sharefile", "deleteFileAtFtpServer", "/VideoClips", filenameHyper);
 					} catch (Exception e) {
 						log.error("deleteVideoClip", e);
 					}
 				}
 				Vovo.getLcmUtil().deleteVideoClip(item.getLcmVideoClip().getId());
 				reflashVideoClipPanel();
 			}
 		}
 	}
 	
 	private void reflashMonitorPanel(final int pageIndex,final int pageSize)throws Exception{
 		SwingUtilities.invokeLater(new Runnable() {
 					
 			@Override
 			public void run() {
				final VideoClipPanel panel = Vovo.getViewManager().getView(Constants.ViewKey.VIDEOCLIPPANEL.toString());
 				try {
					//LoginInfo info = Vovo.getMyContext().getDataManager().getValue(Constants.DataKey.LOGGININFO.toString());
 //					LCMVideoClip[] monitorList = Vovo.getLcmUtil().getMonitorList(pageIndex, pageSize);
 					int index=pageIndex*pageSize;
 					int count=pageSize;
					panel.getMonitorPanel().removeAll();
 					Monitor[] monitors =MonitorsUtil.getArray();
 					if(null==monitors||monitors.length<index)
 						return;
 					if(monitors.length<index+count)
 						count=monitors.length-index;
 					Monitor[] monitorList =new Monitor[count];
 					System.arraycopy(monitors, index, monitorList, 0, count);
 					if (monitorList != null) {
 						log.info("MonitorList size: "+monitorList.length);
 						for (final Monitor lcmVideoClip : monitorList) {
 							if(lcmVideoClip==null)
 								return;
 //							System.out.println(lcmVideoClip);
 							final VideoClipItem videoClipItem = new VideoClipItem();
 							lcmVideoClip.setIsmonitor(true);
 							lcmVideoClip.setHttpVideoUrlHigh(lcmVideoClip.getUri());
 							videoClipItem.setLcmVideoClip(lcmVideoClip);
 //							videoClipItem.getPictureLabel().setIcon(new ImageIcon(new URL(lcmVideoClip.getThumbnailUrl())));
 							videoClipItem.getTitleLabel().setText(lcmVideoClip.getTitle());
 							videoClipItem.getDescriptionLabel().setText(lcmVideoClip.getDescription());
 							panel.getMonitorPanel().add(videoClipItem);
 							
 							new Thread(){
 
 								@Override
 								public void run() {
 									
 									ImagePainter imagePainter = null;
 									try {
 										imagePainter = new ImagePainter(ImageIO.read(new ByteArrayInputStream(lcmVideoClip.getData())));
 									} catch (Exception e) {
 										try {
 											imagePainter = new ImagePainter(ImageIO.read(getClass().getResource("/com/lorent/vovo/resource/images/video_no_pic.png")));
 										} catch (IOException e1) {
 											log.error("reflashVideoClipPanel ",e );
 											e1.printStackTrace();
 										}
 										log.error("reflashVideoClipPanel ",e );
 									}
 									imagePainter.setScaleToFit(true);
 									imagePainter.setScaleType(ScaleType.Distort);
 									videoClipItem.getPictureXPanel().setBackgroundPainter(imagePainter);
 								}
 								
 							}.start();
 							
 //							if (lcmVideoClip.getCreaterNo() == null || lcmVideoClip.getCreaterNo().equals("")) {
 //								videoClipItem.getDeleteButton().setVisible(false);
 //							}
 //							else if(lcmVideoClip.getCreaterNo().equals(lcmVideoClip.getCreaterNo())){
 //								videoClipItem.getDeleteButton().setVisible(true);
 //							}
 //							else{
 //								videoClipItem.getDeleteButton().setVisible(false);
 //							}
 							
 							videoClipItem.getDeleteButton().setVisible(true);
 						}
 					}
 				} catch (Exception e) {
 					log.error("reflashMonitorPanel", e);
				}finally{
					panel.getMonitorPanel().validate();
					panel.getMonitorPanel().repaint();
 				}
 			}
 		});
 	}
 	
 	private void reflashVideoClipPanel(final VideoClipListPanel listpanel ,final int pageIndex,final int pageSize) throws Exception{
 		SwingUtilities.invokeLater(new Runnable() {
 			
 			@Override
 			public void run() {
 				try {
 					LCMVideoClip[] videoClipList = Vovo.getLcmUtil().getVideoClipList(pageIndex, pageSize,listpanel.getCategory());
 					if (videoClipList != null) {
 						log.info("videoClipList size: "+videoClipList.length);
 //						VideoClipPanel panel = Vovo.getViewManager().getView(Constants.ViewKey.VIDEOCLIPPANEL.toString());
 //						panel.getVideoClipPanel().removeAll();
 //						listpanel.removeAll();
 						listpanel.getVideoClipPanel().removeAll();
 						for (final LCMVideoClip lcmVideoClip : videoClipList) {
 							final VideoClipItem videoClipItem = new VideoClipItem();
 							videoClipItem.setLcmVideoClip(lcmVideoClip);
 							videoClipItem.getTitleLabel().setText(lcmVideoClip.getTitle());
 							videoClipItem.getDescriptionLabel().setText(lcmVideoClip.getDescription());
 //							panel.getVideoClipPanel().add(videoClipItem);
 							listpanel.getVideoClipPanel().add(videoClipItem);
 //							listpanel.add(videoClipItem);
 							new Thread(){
 
 								@Override
 								public void run() {
 									
 									ImagePainter imagePainter = null;
 									try {
 //										String stemp1 = lcmVideoClip.getThumbnailUrl(); 
 //										int indexOf = stemp1.indexOf(":8800/");
 //										String endstr = stemp1.substring(indexOf+6);
 //										String beginstr = stemp1.substring(0, indexOf+6);
 //										String url = beginstr+ URLEncoder.encode(endstr);
 //										imagePainter = new ImagePainter(new URL(url));
 										imagePainter = new ImagePainter(ImageIO.read(new URL(lcmVideoClip.getThumbnailUrl())));
 									} catch (Exception e) {
 										try {
 											imagePainter = new ImagePainter(ImageIO.read(getClass().getResource("/com/lorent/vovo/resource/images/video_no_pic.png")));
 										} catch (IOException e1) {
 											log.error("reflashVideoClipPanel ",e );
 											e1.printStackTrace();
 										}
 										log.error("reflashVideoClipPanel ",e );
 									}
 									imagePainter.setScaleToFit(true);
 									imagePainter.setScaleType(ScaleType.Distort);
 									videoClipItem.getPictureXPanel().setBackgroundPainter(imagePainter);
 								}
 								
 							}.start();
 							
 							if (lcmVideoClip.getCreaterNo() == null || lcmVideoClip.getCreaterNo().equals("")) {
 								videoClipItem.getDeleteButton().setVisible(false);
 							}
 							else if(lcmVideoClip.getCreaterNo().equals(lcmVideoClip.getCreaterNo())){
 								videoClipItem.getDeleteButton().setVisible(true);
 							}
 							else{
 								videoClipItem.getDeleteButton().setVisible(false);
 							}
 							
 						}
 					}
 					else{
 						listpanel.getVideoClipPanel().removeAll();
 						log.info("videoClipList size: "+videoClipList);
 					}
 					listpanel.getVideoClipPanel().repaint();
 					listpanel.getVideoClipPanel().revalidate();
 					
 				} catch (Exception e) {
 					log.error("reflashVideoClipPanel", e);
 				}
 				if (listpanel.getMaxPageIndex()-1 <= listpanel.getPageIndex()) {
 					listpanel.getLastPageButton().setEnabled(false);
 				}
 				else{
 					listpanel.getLastPageButton().setEnabled(true);
 				}
 				if (1 <= listpanel.getPageIndex()) {
 					listpanel.getPrePageButton().setEnabled(true);
 				}
 				else{
 					listpanel.getPrePageButton().setEnabled(false);
 				}
 			}
 		});
 	}
 	
 	//刷新
 	public void reflashMonitorPanel() throws Exception{
 		int monitorListLength = MonitorsUtil.getLength();
 		int left = monitorListLength % pageSize;
 		if (left >0 ) {
 			maxPageIndexM = monitorListLength / pageSize + 1;
 		}
 		else{
 			maxPageIndexM = monitorListLength / pageSize;
 		}
 		
 		VideoClipPanel panel = Vovo.getViewManager().getView(Constants.ViewKey.VIDEOCLIPPANEL.toString());
 		reflashMonitorPanel(pageIndexM, pageSize);
 		if (maxPageIndexM-1 <= pageIndexM) {
 			panel.getLastPageMonitorButton().setEnabled(false);
 		}
 		else{
 			panel.getLastPageMonitorButton().setEnabled(true);
 		}
 		if (1 <= pageIndexM) {
 			panel.getPrePageMonitorButton().setEnabled(true);
 		}
 		else{
 			panel.getPrePageMonitorButton().setEnabled(false);
 		}
 	}
 	
 	public void reflashAllVideoClipPanel() throws Exception{
 		VideoClipPanel panel = Vovo.getViewManager().getView(Constants.ViewKey.VIDEOCLIPPANEL.toString());
 		Component[] components = panel.getVideoClipTabbedPane().getComponents();
 		for (Component component : components) {
 			if (component instanceof VideoClipListPanel) {
 				VideoClipListPanel listPanel = (VideoClipListPanel) component;
 				int videoListLength = Vovo.getLcmUtil().getVideoListLength(listPanel.getCategory());
 				int left = videoListLength % pageSize;
 				if (left >0 ) {
 					listPanel.setMaxPageIndex(videoListLength / pageSize + 1);
 				}
 				else{
 					listPanel.setMaxPageIndex(videoListLength / pageSize);
 				}
 				reflashVideoClipPanel(listPanel,listPanel.getPageIndex(),pageSize);
 				if (listPanel.getMaxPageIndex()-1 <= listPanel.getPageIndex()) {
 					listPanel.getLastPageButton().setEnabled(false);
 				}
 				else{
 					listPanel.getLastPageButton().setEnabled(true);
 				}
 				if (1 <= listPanel.getPageIndex()) {
 					listPanel.getPrePageButton().setEnabled(true);
 				}
 				else{
 					listPanel.getPrePageButton().setEnabled(false);
 				}
 			}
 		}
 	}
 	
 	//刷新
 	public void reflashVideoClipPanel() throws Exception{
 		VideoClipPanel panel = Vovo.getViewManager().getView(Constants.ViewKey.VIDEOCLIPPANEL.toString());
 		int selectedIndex = panel.getVideoClipTabbedPane().getSelectedIndex();
 		VideoClipListPanel listPanel = (VideoClipListPanel) panel.getVideoClipTabbedPane().getComponentAt(selectedIndex);
 		int videoListLength = Vovo.getLcmUtil().getVideoListLength(listPanel.getCategory());
 		int left = videoListLength % pageSize;
 		if (left >0 ) {
 			listPanel.setMaxPageIndex(videoListLength / pageSize + 1);
 		}
 		else{
 			listPanel.setMaxPageIndex(videoListLength / pageSize);
 		}
 		reflashVideoClipPanel(listPanel,listPanel.getPageIndex(),pageSize);
 		
 	}
 	
 	public void preVideoClipPage(VideoClipListPanel panel) throws Exception{
 		int pageIndex = panel.getPageIndex() -1;
 		panel.setPageIndex(pageIndex);
 //		pageIndex--;
 		reflashVideoClipPanel(panel,pageIndex,pageSize);
 	}
 	
 	public void lastVideoClipPage(VideoClipListPanel panel) throws Exception{
 		int pageIndex = panel.getPageIndex() +1;
 		panel.setPageIndex(pageIndex);
 //		pageIndex++;
 		reflashVideoClipPanel(panel,pageIndex,pageSize);
 	}
 	
 	public void preMonitorPage() throws Exception{
 		pageIndexM--;
 		reflashMonitorPanel();
 	}
 	
 	public void lastMonitorPage() throws Exception{
 		pageIndexM++;
 		reflashMonitorPanel();
 	}
 	
 	public void showVideoClipInfoDialog(VideoClipItem item) throws Exception{
 		MainFrame frame = Vovo.getViewManager().getView(Constants.ViewKey.MAINFRAME.toString());
 		VideoClipInfoDialog videoClipInfoDialog = new VideoClipInfoDialog(frame, true);
 		videoClipInfoDialog.getCreaterNameTextField().setText(item.getLcmVideoClip().getCreaterName());
 		videoClipInfoDialog.getTitleTextField().setText(item.getLcmVideoClip().getTitle());
 		videoClipInfoDialog.getDescriptionTextArea().setText(item.getLcmVideoClip().getDescription());
 		videoClipInfoDialog.getPictureXPanel().setBackgroundPainter(item.getPictureXPanel().getBackgroundPainter());
 		Vovo.getViewManager().setWindowCenterLocation(videoClipInfoDialog);
 		videoClipInfoDialog.setTitle("视频信息");
 		if (item.getLcmVideoClip().getIsmonitor()) {
 			videoClipInfoDialog.getCreaterNameTextField().setVisible(false);
 			videoClipInfoDialog.getUploadLabel().setVisible(false);
 			videoClipInfoDialog.getLiveStreamUrlLabel().setVisible(true);
 			videoClipInfoDialog.getLiveStreamUrlTextField().setVisible(true);
 			videoClipInfoDialog.getLiveStreamUrlTextField().setText(item.getLcmVideoClip().getRtspVideoUrlHigh());
 		}
 		else{
 			videoClipInfoDialog.getLiveStreamUrlLabel().setVisible(false);
 			videoClipInfoDialog.getLiveStreamUrlTextField().setVisible(false);
 		}
 		videoClipInfoDialog.setVisible(true);
 	}
 	
 	private static Process videoClipPlayerProcess = null;
 	
 	public void killPlayer() throws Exception{
 //		if (videoClipPlayerProcess != null) {
 //			System.out.println("kill ...");
 //			videoClipPlayerProcess.destroy();
 //		}
 		ProcessUtil.getInstance().killProcessByName("vlc.exe");
 	}
 	
 	public void playVideoClip(String url) throws Exception{
 		String vlc = StringUtil.convertFilePath2DOSCommandStr(Constants.USER_DIR+"\\vlc\\vlc.exe");
 		String newUrl = StringUtil.convertFilePath2DOSCommandStr(url);
 		final String cmdStr =  "cmd /c "+StringUtil.convertFilePath2DOSCommandStr(vlc+" --one-instance --fullscreen --no-video-title-show --play-and-exit  "+newUrl);
 		new Thread(){
 
 			@Override
 			public void run() {
 				try {
 					log.info(cmdStr);
 					videoClipPlayerProcess = ProcessUtil.getInstance().startProcess(cmdStr);
 //					startProcess.waitFor();
 //					videoClipPlayerProcess.destroy();
 					log.info("play video end");
 				} catch (Exception e) {
 					log.error("playVideoClip", e);
 				}
 			}
 		}.start();
 		
 	}
 	public void playMonitor(String url) throws Exception{
 		String vlc = StringUtil.convertFilePath2DOSCommandStr(Constants.USER_DIR+"\\vlc\\vlc.exe");
 		String newUrl = StringUtil.convertFilePath2DOSCommandStr(url);
 		final String cmdStr =  "cmd /c "+StringUtil.convertFilePath2DOSCommandStr(vlc+" --no-video-title-show "+newUrl);
 		new Thread(){
 			
 			@Override
 			public void run() {
 				try {
 					log.info(cmdStr);
 					Process startProcess = ProcessUtil.getInstance().startProcess(cmdStr);
 //					startProcess.waitFor();
 					log.info("play video end");
 				} catch (Exception e) {
 					log.error("playVideoClip", e);
 				}
 			}
 		}.start();
 		
 	}
 	
 	public void startLiveTv() throws Exception{
 		String liveTv = StringUtil.convertFilePath2DOSCommandStr(Constants.USER_DIR+"\\livetv\\tv.jar");
 		String javaExePath = StringUtil.convertFilePath2DOSCommandStr(Constants.USER_DIR+"\\jre\\bin\\java.exe");
 		final String cmdStr = "cmd /c "+StringUtil.convertFilePath2DOSCommandStr(javaExePath+" -jar "+liveTv);
 		new Thread(){
 
 			@Override
 			public void run() {
 				try {
 					log.info(cmdStr);
 					Process startProcess = ProcessUtil.getInstance().startProcess(cmdStr);
 					startProcess.waitFor();
 					log.info("start liveTv end");
 				} catch (Exception e) {
 					log.error("startLiveTv", e);
 				}
 			}
 		}.start();
 	}
 	
 	public void startLiveMonitor() throws Exception{
 		
 		EventQueue.invokeLater(new Runnable() {
 
 			@Override
 			public void run() {
 				PlayMonitorFrame frame = Vovo.getViewManager().getView(Constants.ViewKey.PLAYMONITORFRAME.toString());
 				if (frame != null && frame.isVisible()) {
 					frame.toFront();
 				}
 				else{
 					try {
 						frame = Vovo.getViewManager().createView(PlayMonitorFrame.class, Constants.ViewKey.PLAYMONITORFRAME.toString());
 						frame.setSize(840, 600);
 						JWebBrowser jWebBrowser = new JWebBrowser();
 						jWebBrowser.setBarsVisible(false);
 						jWebBrowser.setMenuBarVisible(false);
 						jWebBrowser.setAuthenticationHandler(new WebBrowserAuthenticationHandler() {
 							@Override
 							public Credentials getCredentials(JWebBrowser arg0, String arg1) {
 								System.out.println("admin:admin");
 								return new Credentials("admin", "admin");
 							}
 						});
 						String url = Vovo.getConfigManager().getProperty("monitorurl", "http://10.168.130.221/cnDefault_Login.asp");
 						jWebBrowser.navigate(url);
 						frame.add(jWebBrowser);
 						frame.setVisible(true);
 					} catch (Exception e) {
 						log.error("startLiveMonitor",e);
 						e.printStackTrace();
 					}
 				}
 				
 				try {
 					Vovo.getViewManager().setWindowCenterLocation(frame);
 				} catch (Exception e) {
 					log.error("startLiveMonitor", e);
 					e.printStackTrace();
 				}
 			}
 		});
 	}
 }
