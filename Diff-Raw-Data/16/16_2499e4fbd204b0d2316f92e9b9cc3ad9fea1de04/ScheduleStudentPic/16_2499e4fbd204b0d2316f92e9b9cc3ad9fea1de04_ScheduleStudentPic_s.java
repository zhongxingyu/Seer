 package com.cj.lion.web;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.InputStream;
 import java.net.URL;
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.StandardCopyOption;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.List;
 
 import lombok.extern.slf4j.Slf4j;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.domain.Page;
 import org.springframework.data.domain.PageRequest;
 import org.springframework.data.domain.Pageable;
 import org.springframework.scheduling.annotation.EnableAsync;
 import org.springframework.scheduling.annotation.EnableScheduling;
 import org.springframework.scheduling.annotation.Scheduled;
 import org.springframework.stereotype.Component;
 
 import com.cj.domain.received.ImageReceivedMessage;
 import com.cj.lion.domain.StudentInfo;
 import com.cj.lion.repository.StudentInfoRepository;
 import com.cj.repository.received.ImageReceivedMessageRepository;
 
 @Slf4j
 @EnableAsync
 @EnableScheduling
 @Component
 public class ScheduleStudentPic {
 
 	private static final String FOLDER_NAME="studentPic";
 	
 	@Autowired
 	private StudentInfoRepository studentInfoRepository;
 	
     @Autowired
     private ImageReceivedMessageRepository imageReceivedMessageRepository;
 
 	@Scheduled(cron = "0 0 1-4 * * *")
 	public void syncStudentPic() {
 		log.info("sync Student's pictures");
 		Pageable pageable=new PageRequest(0,20);
		Page<StudentInfo> studentInfos=studentInfoRepository.findByStoredIsFalse(pageable);
		if(studentInfos.getNumberOfElements()==0){
			return;
		}
		for(StudentInfo studentInfo:studentInfos){
			this.syncPic(studentInfo);
		}
 	}
 	
 	
 	
 	private void syncPic(StudentInfo studentInfo) {
 		List<ImageReceivedMessage> imageReceivedMessages=imageReceivedMessageRepository.findByOtherOrderByCreateTimeDesc(studentInfo.getWechatId());
 		studentInfo.setStored(true);
 		studentInfoRepository.save(studentInfo);
 		if(imageReceivedMessages.size()>0){
 			String url = imageReceivedMessages.get(0).getPicUrl();
 			try {
 				InputStream inputStream=new URL(url).openStream();
 				Path path=getStoredLink(studentInfo.getWechatId());
 				Files.copy(inputStream, path, StandardCopyOption.REPLACE_EXISTING);
 			} catch (IOException e) {
 				log.info(e.getMessage());
 			}
 		}
 		
 	}
 	
 	  private Path getStoredLink(String wechatId) throws IOException{
 			SimpleDateFormat dateformat= new SimpleDateFormat("yyyyMMdd");
 			String dateString = dateformat.format(new Date());
 			String pathString=System.getProperty("user.home")+File.separator+FOLDER_NAME;
 			File folder=new File(pathString);
 			if(!folder.exists()){
 				folder.mkdir();
 			}
 			pathString=pathString+File.separator+dateString;
 			folder=new File(pathString);
 			if(!folder.exists()){
 				folder.mkdir();
 			}
 			pathString=pathString+File.separator+wechatId+".jpg";
 			File file=new File(pathString);
 			if(!file.exists()){
 				file.createNewFile();
 			}
 			
 			Path path=file.toPath();
 			return path;
 		}
 
 }
