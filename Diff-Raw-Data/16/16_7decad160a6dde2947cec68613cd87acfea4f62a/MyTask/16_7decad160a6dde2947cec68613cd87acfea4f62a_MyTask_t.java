 /**
  * 
  */
 package com.haozileung.scau.server.common.quartz;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Random;
 
 import org.apache.commons.io.FileUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Service;
 
 import com.haozileung.scau.server.common.context.SpringApplicationContextHolder;
 import com.haozileung.scau.server.common.dto.RestDataSourceResponse;
 import com.haozileung.scau.server.common.dto.TempMap;
 import com.haozileung.scau.server.common.listener.WebApplicationInitListener;
 import com.haozileung.scau.server.common.utility.DateUtil;
 import com.haozileung.scau.server.common.utility.GsonUtil;
 import com.haozileung.scau.server.common.websocket.MyWebSocket;
 import com.haozileung.scau.server.domain.Equipment;
 import com.haozileung.scau.server.dto.SportDataInfo;
 import com.haozileung.scau.server.repository.IEquipmentRepository;
 import com.haozileung.scau.server.service.ISportDataService;
 
 /**
  * @author haozi
  * 
  */
 @Service("myTask")
 public class MyTask {
 	@Autowired
 	private ISportDataService sportDataService;
 
 	@Autowired
 	private IEquipmentRepository equipmentRespository;
 
 	private static final Logger logger = LoggerFactory.getLogger(MyTask.class);
 	private static List<String> fileList = null;
 	private static boolean isRunning = false;
 	private static String dataPath = System.getProperty("ssh.root")
 			+ "/new_data";
 
 	@Scheduled(cron = "0 * * * * ?")
 	public void execute() {
 		if (isRunning == false) {
 			if (logger.isDebugEnabled()) {
 				logger.debug("定时任务开始...");
 			}
 			isRunning = true;
 			fileList = getFileList(dataPath);
			TempMap tmpMap = (TempMap) SpringApplicationContextHolder
					.getBean("tmpMap");
			tmpMap.getMap().clear();
			tmpMap.setStatus(false);
 			processFiles();
			tmpMap.setStatus(true);
 			for (MyWebSocket socket : WebApplicationInitListener
 					.getSocketList()) {
 				try {
 					RestDataSourceResponse<SportDataInfo> response = new RestDataSourceResponse<SportDataInfo>();
 					if (tmpMap != null && tmpMap.isStatus() == true) {
 						tmpMap.setStatus(false);
 						for (String equipmentId : tmpMap.getMap().keySet()) {
 							List<SportDataInfo> sportDatas = sportDataService
 									.getSportDataByEquipmentId(equipmentId,
 											new Date());
 							response.getData().addAll(sportDatas);
 						}
 					}
 					String json = GsonUtil.bean2json(response);
 					socket.getConn().sendMessage(json);
 				} catch (IOException e) {
 					logger.error(e.getMessage());
 				}
 			}
 			isRunning = false;
 		}
 	}
 
 	@Scheduled(cron = "30 * * * * ?")
 	public void addNewDataFile() {
 		Random r = new Random(new Date().getTime());
 		int t = r.nextInt(30) + 1;
 		List<Equipment> equipment = equipmentRespository.findAll();
 		for (int num = 0; num < 2; num++) {
 			String cowId = equipment
 					.get(equipment.size() <= 0 ? 0
 							: r.nextInt(equipment.size())).getId().toString();
 			StringBuffer dataBuffer = new StringBuffer();
 			for (int times = 0; times < t; times++) {
 
 				dataBuffer
 						.append(cowId)
 						.append(',')
 						.append(DateUtil.format(
 								DateUtil.addDays(new Date(), 0 - r.nextInt(30)),
 								DateUtil.defaultDatePatternStr)).append(',');
 				for (int i = 0; i < 24; i++) {
 					dataBuffer.append(r.nextInt(500));
 					if (i != 23) {
 						dataBuffer.append(',');
 					} else {
 						dataBuffer.append('\n');
 					}
 				}
 			}
 			try {
 				FileUtils
 						.writeStringToFile(
 								new File(dataPath + '/' + new Date().getTime()),
 								dataBuffer.toString(), "UTF-8");
 			} catch (IOException e) {
 				logger.error("新建数据文件失败！" + e.getMessage());
 			}
 			dataBuffer = null;
 		}
 	}
 
 	public void processFiles() {
 		if (null != fileList) {
 			for (String file : fileList) {
 				if (null != file) {
 					BufferedReader br = null;
 					String data = null;
 					try {
 						br = new BufferedReader(new FileReader(file));
 					} catch (FileNotFoundException e) {
 						logger.error("读取文件失败：文件没有找到！" + e.getMessage());
 						continue;
 					}
 					try {
 						data = br.readLine();
 					} catch (IOException e) {
 						logger.error("读取文件失败：文件读取出错！" + e.getMessage());
 						continue;
 					}
 					while (data != null) {
 						processLine(data);
 						try {
 							data = br.readLine();
 						} catch (IOException e) {
 							logger.error("读取文件失败：文件读取出错！" + e.getMessage());
 							continue;
 						}
 					}
 					try {
 						br.close();
 					} catch (IOException e) {
 						logger.error("文件关闭失败！" + e.getMessage());
 					}
 					moveFile(file);
 				}
 			}
 		}
 	}
 
 	public void moveFile(String file) {
 		File srcFile = new File(file);
 		File destDir = new File(srcFile.getParent().replaceAll("new_data",
 				"backup_data"));
 		try {
 			FileUtils.moveToDirectory(srcFile, destDir, true);
 		} catch (IOException e) {
 			logger.error("文件备份失败：" + file + "\n" + e.getMessage());
 		}
 	}
 
 	public List<String> getFileList(String path) {
 		if (null == path) {
 			return null;
 		}
 		List<String> fileLst = new ArrayList<String>();
 		File file = new File(path);
 		File[] files = file.listFiles();
 		if (null != files) {
 			for (int i = 0; i < files.length; i++) {
 				if (!files[i].isDirectory()) {
 					fileLst.add(files[i].getAbsolutePath());
 				} else {
 					fileLst.addAll(getFileList(files[i].getAbsolutePath()));
 				}
 			}
 		}
 		return fileLst;
 	}
 
 	public boolean processLine(String line) {
 		TempMap tmpMap = (TempMap) SpringApplicationContextHolder
 				.getBean("tmpMap");
 		SportDataInfo sportDataInfo = new SportDataInfo();
 		String[] dataArray = line.split(",");
 		sportDataInfo.setEquipmentId(dataArray[0]);
 		sportDataInfo.setCurrentDate(dataArray[1]);
 		StringBuffer data = new StringBuffer();
 		for (int i = 2; i < 26; i++) {
 			data.append(Float.valueOf(dataArray[i]));
 			if (i != 25) {
 				data.append(',');
 			}
 		}
 		sportDataInfo.setData(data.toString());
 		tmpMap.getMap().put(sportDataInfo.getEquipmentId(),
 				sportDataInfo.getEquipmentId());
 		return sportDataService.saveSportData(sportDataInfo);
 	}
 }
