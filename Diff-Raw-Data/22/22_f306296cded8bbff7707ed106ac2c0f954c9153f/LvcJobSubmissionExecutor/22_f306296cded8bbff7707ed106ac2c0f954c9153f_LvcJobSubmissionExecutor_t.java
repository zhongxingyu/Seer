 package org.ourgrid.portal.server.logic.executors.plugin.lvc;
 
 import java.awt.Graphics2D;
 import java.awt.image.BufferedImage;
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import javax.imageio.ImageIO;
 
 import org.ourgrid.common.specification.exception.JobSpecificationException;
 import org.ourgrid.common.specification.exception.TaskSpecificationException;
 import org.ourgrid.common.specification.job.IOBlock;
 import org.ourgrid.common.specification.job.IOEntry;
 import org.ourgrid.common.specification.job.JobSpecification;
 import org.ourgrid.common.specification.job.TaskSpecification;
 import org.ourgrid.portal.client.common.to.response.ResponseTO;
 import org.ourgrid.portal.client.common.to.service.ServiceTO;
 import org.ourgrid.portal.client.plugin.lvc.gui.model.LvcJobRequest;
 import org.ourgrid.portal.client.plugin.lvc.to.response.LvcJobSubmissionResponseTO;
 import org.ourgrid.portal.client.plugin.lvc.to.service.LvcJobSubmissionTO;
 import org.ourgrid.portal.server.exceptions.ExecutionException;
 import org.ourgrid.portal.server.logic.executors.JobSubmissionExecutor;
 import org.ourgrid.portal.server.logic.interfaces.OurGridPortalIF;
 import org.ourgrid.portal.server.messages.OurGridPortalServiceMessages;
 import org.ourgrid.portal.server.util.OurgridPortalProperties;
 
 public class LvcJobSubmissionExecutor extends JobSubmissionExecutor {
 
 	public LvcJobSubmissionExecutor(OurGridPortalIF portal) {
 		super(portal);
 	}
 
 	@Override
 	public ResponseTO execute(ServiceTO serviceTO) throws ExecutionException {
 
 		LvcJobSubmissionTO lvcJobSubmissionTO = (LvcJobSubmissionTO) serviceTO;
 		LvcJobSubmissionResponseTO lvcJobSubmissionResponseTO = new LvcJobSubmissionResponseTO();
 
 		Integer numberOfPartsOnHeight    = lvcJobSubmissionTO.getNumberOfPartsOnHeight();
 		Integer numberOfPartsOnWidth	 = lvcJobSubmissionTO.getNumberOfPartsOnWidth();
 		Long uploadId					 = lvcJobSubmissionTO.getUploadId();
 
 		initializeClient();
 
 		String userLogin = lvcJobSubmissionTO.getUserLogin();
 		boolean emailNotification = lvcJobSubmissionTO.isEmailNotification();
 		
 		String imageFilePath = "";
 		String filesPath = getFilesPath(uploadId);
 		
 		File dir = new File(filesPath);
 		if(dir.exists()){
 			for(String file : dir.list()){
 				if(file.toLowerCase().endsWith(".jpg") 
 						|| file.toLowerCase().endsWith(".png")){
 					imageFilePath = filesPath+System.getProperty("file.separator")+file;
 				}
 			}
 		}
 		
 		System.out.println(imageFilePath);
 
 		String outputDir = createOutputDir(userLogin);
 		lvcJobSubmissionResponseTO.setOutputDir(outputDir);
 
 		JobSpecification theJob = new JobSpecification();
 		try {
 			String filenames[] = splitImage(imageFilePath, numberOfPartsOnHeight, numberOfPartsOnWidth, outputDir);
 			theJob = compileInputs(filenames, outputDir);
 		} catch (JobSpecificationException e) {
 			throw new ExecutionException(OurGridPortalServiceMessages.PORTAL_SERVICE_UNAVAIABLE_MSG);
 		} catch (TaskSpecificationException e) {
 			throw new ExecutionException(OurGridPortalServiceMessages.PORTAL_SERVICE_UNAVAIABLE_MSG);
 		} catch (IOException e) {
 			throw new ExecutionException(OurGridPortalServiceMessages.PORTAL_SERVICE_UNAVAIABLE_MSG);
 		}
 
 		Integer jobId = addJob(lvcJobSubmissionTO.getUserLogin(), theJob);
 		lvcJobSubmissionResponseTO.getJobIDs().add(jobId);
 		LvcJobImageJoiner.getInstance().addOutputDir(jobId, outputDir);
 		Integer[] chunks = new Integer[2];
 		chunks[0] = numberOfPartsOnWidth;
 		chunks[1] = numberOfPartsOnHeight;
 		LvcJobImageJoiner.getInstance().addChunks(jobId, chunks);
 		addRequest(jobId, new LvcJobRequest(jobId ,0, userLogin, emailNotification));
 		reeschedule();
 
 		return lvcJobSubmissionResponseTO;
 	}
 	
 	private String[] splitImage(String imagePath, int numberOfPartsOnHeight, int numberOfPartsOnWidth, String outputDir) throws IOException{
 		File image = new File(imagePath);
 		FileInputStream fis = new FileInputStream(image);
 		BufferedImage bufferedImage = ImageIO.read(fis);
 		
 		int chunks = numberOfPartsOnHeight * numberOfPartsOnWidth;
 		
 		int chunkWidth = bufferedImage.getWidth() / numberOfPartsOnWidth;
 		int chunkHeight = bufferedImage.getHeight() / numberOfPartsOnHeight;
 		int count = 0;
 		BufferedImage imgs[] = new BufferedImage[chunks];
 		String filenames[] = new String[chunks];
 		
 		for(int x = 0; x < numberOfPartsOnWidth; x++){
 			for(int y = 0; y < numberOfPartsOnHeight; y++){
 				filenames[count] = "IN_"+x+"_"+y+".jpg";
 				imgs[count] = new BufferedImage(chunkWidth, chunkHeight, bufferedImage.getType());
 				
 				Graphics2D gr = imgs[count++].createGraphics();
 				gr.drawImage(bufferedImage, 0,0, chunkWidth, chunkHeight, chunkWidth * y, chunkHeight*x, chunkWidth*y+chunkWidth, chunkHeight*x+chunkHeight, null);
 				gr.dispose();
 			}
 		}
 		
 		String inputImages = outputDir + System.getProperty("file.separator") + "input";
 		File inputImagesDir = new File(inputImages);
 		inputImagesDir.mkdirs();
 		
 		for(int i = 0; i<imgs.length; i++){
 			ImageIO.write(imgs[i], "jpg", new File(inputImages+System.getProperty("file.separator")+filenames[i]));
 		}
 		
 		return filenames;
 	}
 	
 	private String getFilesPath(Long uploadId) {
 		return getPortal().getDAOManager().getUploadDirName(uploadId);
 	}
 
 	private JobSpecification compileInputs(String filenames[], String outputDir) throws JobSpecificationException, TaskSpecificationException, IOException {
 		List<TaskSpecification> tasksList = new ArrayList<TaskSpecification>();
 		
 		String resourcesDir = OurgridPortalProperties.getInstance().getProperty(OurgridPortalProperties.LVC_PATH);
 		
		for(String filename : filenames) {
 			String inputFilename = outputDir + File.separator + "input" + File.separator + filename;
 			//create initBlock
 			IOBlock initBlock = new IOBlock();
 			
 			IOEntry initEntry = new IOEntry("put", inputFilename, filename);
 			initBlock.putEntry(initEntry);
 			
			IOEntry initEntryIM = new IOEntry("put", resourcesDir + File.separator + "mogrify", "mogrify");
 			initBlock.putEntry(initEntryIM);
 			
			String remoteExe = "chmod +x mogrify; ./mogrify -paint 20 " + filename + ";"+ LINE_SEPARATOR;
 			
 			//create finalBlock
 			IOBlock finalBlock = new IOBlock();
 			
			IOEntry finalEntryMogrify = new IOEntry("get", filename, outputDir + File.separator + filename);
 			finalBlock.putEntry(finalEntryMogrify);
 
 			TaskSpecification taskSpec = new TaskSpecification(initBlock, remoteExe, finalBlock, null);
 			tasksList.add(taskSpec);
 		}
 
 
 		return new JobSpecification("LVC Job", "", tasksList);
 	}
 
 	private String createOutputDir(String userLogin) {
 
 		String outputDir = "";
 
 		outputDir  = getUploadDirectory();
 		outputDir += System.getProperty("file.separator");
 		outputDir += userLogin;
 		outputDir += System.getProperty("file.separator");
 		outputDir += "LVC Job Submission [" + longToDate() + "]";
 
 		File uploadDirFile = new File(outputDir);
 		uploadDirFile.mkdirs();
 
 		return outputDir;
 	}
 
 	private String getUploadDirectory() {
 		String uploadPath = OurgridPortalProperties.getInstance().
 				getProperty(OurgridPortalProperties.STORAGE_DIRECTORY);
 
 		File uploadDirectory = new File(uploadPath);
 
 		if(!uploadDirectory.exists()){
 			uploadDirectory.mkdirs();
 		}
 		return uploadPath;
 	}
 
 
 	private String longToDate() {
 		Date date = new Date();
 		DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy HH-mm-ss");
 		String dateString= dateFormat.format(date);
 		return dateString;
 	}
 
 
 	@Override
 	public void logTransaction(ServiceTO serviceTO) {
 		// TODO Auto-generated method stub
 
 	}
 
 }
