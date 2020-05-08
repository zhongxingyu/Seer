 package de.enwida.web.service.implementation;
 
 import java.io.File;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.test.context.transaction.TransactionConfiguration;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.validation.BindingResult;
 import org.springframework.validation.ObjectError;
 
 import de.enwida.rl.dtos.DOUserLines;
 import de.enwida.web.dao.interfaces.IFileDao;
 import de.enwida.web.dao.interfaces.IUserDao;
 import de.enwida.web.dao.interfaces.IUserLinesDao;
 import de.enwida.web.db.model.UploadedFile;
 import de.enwida.web.model.Right;
 import de.enwida.web.model.User;
 import de.enwida.web.service.interfaces.IUploadFileService;
 import de.enwida.web.service.interfaces.IUserLinesService;
 import de.enwida.web.utils.Constants;
 import de.enwida.web.utils.EnwidaUtils;
 import de.enwida.web.validator.FileValidator;
 
 @Service("fileUploadService")
 @TransactionConfiguration(transactionManager = "jpaTransactionManager", defaultRollback = true)
 @Transactional(rollbackFor = Exception.class)
 public class UploadFileServiceImpl implements IUploadFileService {
 
 
 	@Autowired
 	private IUserLinesService userLineService;
 	
 	/**
 	 * File Data Access Object
 	 */
     @Autowired
     private IFileDao fileDao;  
     
     /**
 	 * User Data Access Object
 	 */
     @Autowired
     private IUserDao userDao;
     
     /**
 	 * User Lines Data Access Object
 	 */
     @Autowired
 	private IUserLinesDao userLinesDao;
  	    
 	/**
 	 * Caution: user should be persisted and in clean state! Dirty attributes
 	 * might be applied (i.e. committed to database, eventually).
 	 * 
 	 * @return the updated and managed user and file object
 	 * @throws Exception
 	 */
 	@Override
 	public User saveUserUploadedFile(User user, UploadedFile file)
 			throws Exception {
 		if (user.getUserId() == null) {
 			throw new IllegalArgumentException("user object is not persisted");
 		}
 		file.setRevision(1);
 		// file.setFileSetUniqueIdentifier(UUID.randomUUID().toString());
 
 		fileDao.create(file, true); // with flush
 		// if (previousFile != null) {
 		// previousFile.setActive(false);
 		// previousFile = fileDao.update(previousFile, true); // with flush
 		// }
 
 		// Refresh the user in order to reflect the changes
 		user = userDao.update(user);
 		userDao.refresh(user);
 		return user;
 	}
 	
 	/**
 	 * Caution: user should be persisted and in clean state! Dirty attributes
 	 * might be applied (i.e. committed to database, eventually).
 	 * 
 	 * @return the updated and managed user and file object
 	 * @throws Exception
 	 */
 	@Override
 	public User replaceUserUploadedFile(User user, UploadedFile newFile, UploadedFile oldFile) throws Exception {
 		if (user.getUserId() == null) {
 			throw new IllegalArgumentException("user object is not persisted");
 		}
 		// check for revision
 		newFile.setRevision(oldFile.getRevision() + 1);
 		newFile = fileDao.update(newFile, true); // with flush
 
 		oldFile.setActive(false);
 		oldFile = fileDao.update(oldFile, true); // with flush
 		
 		// Refresh the user in order to reflect the changes
 		user = userDao.update(user);
 		userDao.refresh(user);
 		return user;
 	}
     
 	/**
 	 * Caution: user should be persisted and in clean state! Dirty attributes
 	 * might be applied (i.e. committed to database, eventually).
 	 * 
 	 * @return the updated and managed user and file object
 	 * @throws Exception
 	 */
 	@Override
 	public User updateUserUploadedFile(User user, UploadedFile file) throws Exception {
 		if (user.getUserId() == null) {
 			throw new IllegalArgumentException("user object is not persisted");
 		}
 		if (file.getUploadedFileId().getId() > 0) {
 			file = fileDao.update(file, true); // with flush
 		} else {
 			fileDao.create(file, true); // with flush
 		}
 
 		// Refresh the user in order to reflect the changes
 		user = userDao.update(user);
 		userDao.refresh(user);
 		return user;
 	}
 
 	/**
 	 * deletes user uploaded file
 	 */
 	@Override
 	public void removeUserUploadedFile(User user, UploadedFile file)
 			throws Exception {
 		if (user.getUserId() == null) {
 			throw new IllegalArgumentException("user object is not persisted");
 		}
 		if (file.getUploadedFileId().getId() == 0) {
 			throw new IllegalArgumentException("user object is not persisted");
 		}
 		if (file.getUploadedFileId().getId() > 0) {
 			file.setUploader(null);
 			file.setMetaData(null);
 			// file.setPreviousFile(null);
 			fileDao.delete(file); // with flush
 		}
 
 		// Refresh the user in order to reflect the changes
 		// user = userDao.update(user);
 		// userDao.refresh(user);
 	}
 	
 	@Override
 	public UploadedFile getFile(long fileId, int revision) {
 		return fileDao.getFile(fileId, revision);
     }
 
     @Override
     public UploadedFile getFileByFilePath(String filePath) {
         return fileDao.getFileByFilePath(filePath);
     }
 
     @Override
     public int getUploadedFileVersion(UploadedFile file, User user) {
         return userDao.getUploadedFileVersion(file, user);
     }
     
     @Override
 	public List<UploadedFile> getUploadedFiles(User user) {
 		return userDao.getActiveUploadedFiles(user);
 	}
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public void makeFileActive(long fileId,int revision, User user, FileValidator fileValidator) throws Exception {
 		
 		UploadedFile fileToMakeActive = fileDao.getFile(fileId,revision);		
 		if (fileToMakeActive.getUploader().equals(user)) {
 			
 			//1. Get Active File of FileSet by FileSetUniqueIdentifier
 			UploadedFile fileAlreadyActive = fileDao
 					.fetchActiveFileByFileId(fileToMakeActive
 							.getUploadedFileId().getId());
 			
 			//2. Erase the Data of the Active File from Database
 			userLineService.eraseUserLines(fileId);
 			
 			//3. Insert the data of the Required Active File
 			BindingResult results = EnwidaUtils.validateFile(new File(fileToMakeActive.getFilePath()), fileValidator);
 			ObjectError status = results.getGlobalError();			
 			if (status.getCode().equalsIgnoreCase("file.upload.parse.success")) {
 				Map<String, Object> parsedData = (Map<String, Object>) status.getArguments()[0];
 				List<DOUserLines> userlines = (List<DOUserLines>) parsedData
 						.get(Constants.UPLOAD_LINES_KEY);
 				// UserLinesMetaData metaData = (UserLinesMetaData)
 				// parsedData.get(Constants.UPLOAD_LINES_METADATA_KEY);
 				userLineService.createUserLines(userlines,
 						fileToMakeActive.getUserLineId());
 			} else if (status.getCode().equalsIgnoreCase("file.upload.parse.error")) {
 				//TODO: FileUpload Fail: Parsing Error
 			}
 			
 			//4. Set the File Active
 			fileAlreadyActive.setActive(false);
 			fileDao.update(fileAlreadyActive, true);
 			fileToMakeActive.setActive(true);
 			fileDao.update(fileToMakeActive, true);
 		}	
 	}
 
 	@Override
 	public List<UploadedFile> getFileSetByFileId(long fileId) {
 		return fileDao.fetchFilesByFileId(fileId);
 	}
 
 	@Override
 	public Set<UploadedFile> getUploadedFilesUserHasAccessTo(User user) {
 		final Map<Integer, List<UploadedFile>> map = new HashMap<Integer, List<UploadedFile>>();
 
 		for (final Right right : user.getAllRights()) {
 			// Did we check this product already?
 			if (map.containsKey(right.getProduct())) {
 				continue;
 			}
 			// Try to get file
 			final List<UploadedFile> files = getFileSetByFileId(right.getProduct());
 			if (files == null || files.size() < 1) {
 				// No such file found
 				// Put a null entry into the map for speedup
 				map.put(right.getProduct(), null);
 				continue;
 			}
 			map.put(right.getProduct(), files);
 		}
 		final Set<UploadedFile> result = new HashSet<UploadedFile>();
 		for (final List<UploadedFile> files : map.values()) {
 			if (files != null) {
 				result.addAll(files);
 			}
 		}
 		return result;
 	}
 }
