 package org.kingsteff.passwordsave;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.InputStream;
 import java.io.OutputStream;
 import java.security.InvalidKeyException;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.crypto.Cipher;
 import javax.crypto.CipherInputStream;
 import javax.crypto.CipherOutputStream;
 import javax.crypto.NoSuchPaddingException;
 import javax.crypto.SecretKey;
 import javax.crypto.spec.SecretKeySpec;
 
 import org.apache.commons.io.FileDeleteStrategy;
 
 import com.vaadin.terminal.StreamResource;
 
 public class FileArchiveController {
 
 	public ArrayList<FileInStore> metaInfos = new ArrayList<FileInStore>();
 
 	public void addFileToList(String cipherFileName) {
 		// ist noch im upLoadHandler
 	}
 
 	public void saveMetaDataForFile(FileInStore fileInStore) {
 		try {
 			deleteAllMetaDataIfExistentForFile(fileInStore);
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		ObjectMarshaller objectMarshaller = new ObjectMarshaller();
 		String xml = objectMarshaller.toXmlWithXStream(fileInStore);
 		writeFileToEncrytedFileInMetaDataFolder(xml);
 	}
 
 	private void deleteAllMetaDataIfExistentForFile(FileInStore fileInStore)
 			throws Exception {
 		File fileHandleForFileInStoreObject = getFileHandleForFileInStoreObject(fileInStore);
 		System.out.println("Trying to delete object:"
 				+ fileHandleForFileInStoreObject);
 		fileInStore = null;
 		FileDeleteStrategy.FORCE.delete(fileHandleForFileInStoreObject);
 	}
 
 	private void writeFileToEncrytedFileInMetaDataFolder(String incoming) {
 		File dir = new File(PersonalPassConstants.MAINDIR
 				+ PersonalPassConstants.FILES_METADATADIR
 				+ PasswordManager.getMd5Hash(PersonalpasssaveApplication
 						.getInstance().getBaseController().getCurrentUser()));
 		if (!dir.exists()) {
 			dir.mkdirs();
 		}
 		try {
 			SecretKey key;
 			Cipher cipher;
 			cipher = Cipher.getInstance("DESede");
 			key = new SecretKeySpec("kgl51um6ad6598pbjnbz6xln".getBytes(),
 					"DESede");
 			cipher.init(Cipher.ENCRYPT_MODE, key);
 			CipherOutputStream cip = new CipherOutputStream(
 					new FileOutputStream(
 							PersonalPassConstants.MAINDIR
 									+ PersonalPassConstants.FILES_METADATADIR
 									+ PasswordManager
 											.getMd5Hash(PersonalpasssaveApplication
 													.getInstance()
 													.getBaseController()
 													.getCurrentUser())
 									+ "/"
 									+ System.currentTimeMillis()
 									+ PersonalPassConstants.FILEMETAINFOS_FILENAME_SUFFIX),
 					cipher);
 			cip.write(incoming.getBytes());
 			cip.close();
 		} catch (Exception e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	public void removeFileFromArchive(String filePath, String realFIleName) {
 		File file = new File(filePath + "/" + realFIleName);
 		file.delete();
 	}
 
 	public ArrayList<FileInStore> getAllFilesForUser() {
 		ArrayList<FileInStore> userFiles = new ArrayList<FileInStore>();
 		File dir = new File(PersonalPassConstants.MAINDIR
 				+ PersonalPassConstants.FILESTORE_SUBDIR
 				+ PasswordManager.getMd5Hash(PersonalpasssaveApplication
 						.getInstance().getBaseController().getCurrentUser())
 				+ "/");
 		File[] listFiles = dir.listFiles();
 		if (listFiles != null) {
 			for (File file : listFiles) {
 				FileInStore newFileInStore = null;
 				try {
 					newFileInStore = getFileMetaInfosByCipheredFileName(file
 							.getName());
 				} catch (Exception e) {
 					// TODO Auto-generated catch block
 					e.printStackTrace();
 				}
 				if (newFileInStore == null) {
 					newFileInStore = new FileInStore();
 				}
 
 				newFileInStore.setFileName(PasswordManager
 						.baseDeCryptString(file.getName()));
 				newFileInStore.setFilePath(file.getAbsolutePath());
 				newFileInStore.setFileSize(file.length() / 1000 + " kilobytes");
 				newFileInStore.setCipheredFileName(file.getName());
 				userFiles.add(newFileInStore);
 
 			}
 		}
 		return userFiles;
 	}
 
 	private FileInStore getFileMetaInfosByCipheredFileName(
 			String cipheredFileName) throws Exception {
 		File dir = new File(PersonalPassConstants.MAINDIR
 				+ PersonalPassConstants.FILES_METADATADIR
 				+ PasswordManager.getMd5Hash(PersonalpasssaveApplication
 						.getInstance().getBaseController().getCurrentUser()));
 		if (dir.exists()) {
 			File[] listFiles = dir.listFiles();
 			if (listFiles != null) {
 				for (File file : listFiles) {
 					FileInStore newFileInStore = new FileInStore();
 					CipherInputStream in;
 					OutputStream out;
 					Cipher cipher;
 					SecretKey key;
 					byte[] byteBuffer;
 					cipher = Cipher
 							.getInstance(PersonalPassConstants.ENCRYPTION_MODE);
 					key = new SecretKeySpec(
 							PersonalPassConstants.MAIN_SCHLUSSEL.getBytes(),
 							PersonalPassConstants.ENCRYPTION_MODE);
 					cipher.init(Cipher.DECRYPT_MODE, key);
 					in = new CipherInputStream(new FileInputStream(dir + "/"
 							+ file.getName()), cipher);
 					int read = 0;
 					StringBuffer buff = new StringBuffer();
 					while ((read = in.read()) != -1) {
 						buff.append((char) read);
 					}
 					ObjectMarshaller marshaller = new ObjectMarshaller();
 					Object fromXml = marshaller.fromXmlWithXStream(buff
 							.toString());
 					newFileInStore = (FileInStore) fromXml;
 					in.close();
 					if (newFileInStore.getCipheredFileName().equals(
 							cipheredFileName)) {
 						return newFileInStore;
 					}
 
 				}
 			}
 		}
 		FileInStore fis = new FileInStore();
 		fis.setCipheredFileName(cipheredFileName);
 		return fis;
 	}
 
 	private File getFileHandleForFileInStoreObject(
 			FileInStore incomingFileInStrore) throws Exception {
 		File dir = new File(PersonalPassConstants.MAINDIR
 				+ PersonalPassConstants.FILES_METADATADIR
 				+ PasswordManager.getMd5Hash(PersonalpasssaveApplication
 						.getInstance().getBaseController().getCurrentUser()));
 		String cipheredFileName = incomingFileInStrore.getCipheredFileName();
 		if (dir.exists()) {
 			File[] listFiles = dir.listFiles();
 			if (listFiles != null) {
 				for (File file : listFiles) {
 					FileInStore newFileInStore = new FileInStore();
 					CipherInputStream in;
 					OutputStream out;
 					Cipher cipher;
 					SecretKey key;
 					byte[] byteBuffer;
 					cipher = Cipher
 							.getInstance(PersonalPassConstants.ENCRYPTION_MODE);
 					key = new SecretKeySpec(
 							PersonalPassConstants.MAIN_SCHLUSSEL.getBytes(),
 							PersonalPassConstants.ENCRYPTION_MODE);
 					cipher.init(Cipher.DECRYPT_MODE, key);
 					in = new CipherInputStream(new FileInputStream(dir + "/"
 							+ file.getName()), cipher);
 					int read = 0;
 					StringBuffer buff = new StringBuffer();
 					while ((read = in.read()) != -1) {
 						buff.append((char) read);
 					}
 					ObjectMarshaller marshaller = new ObjectMarshaller();
 					Object fromXml = marshaller.fromXmlWithXStream(buff
 							.toString());
 					newFileInStore = (FileInStore) fromXml;
 					in.close();
 					if (newFileInStore.getCipheredFileName().equals(
 							cipheredFileName)) {
 						return file;
 					}
 				}
 			}
 		}
 
 		return null;
 	}
 
 	public void openFileFromArchive(String filePath, String realFIleName) {
 
 		// Create an instance of our stream source.
 		StreamResource.StreamSource cipherResource = new MyCipherSource(
 				filePath);
 
 		// Create a resource that uses the stream source and give it a name.
 		// The constructor will automatically register the resource in
 		// the application.
 		StreamResource cipherresource = new StreamResource(cipherResource,
 				realFIleName, PersonalpasssaveApplication.getInstance());
 
 		PersonalpasssaveApplication.getInstance().getMainWindow()
 				.open(cipherresource, "_blank");
 	}
 
 	private CipherInputStream getCipherInputputStream(File file) {
 		CipherInputStream in = null;
 		try {
 			Cipher cipher;
 			SecretKey key;
 			cipher = Cipher.getInstance(PersonalPassConstants.ENCRYPTION_MODE);
 			key = new SecretKeySpec(
 					PersonalPassConstants.MAIN_SCHLUSSEL.getBytes(),
 					PersonalPassConstants.ENCRYPTION_MODE);
 			cipher.init(Cipher.DECRYPT_MODE, key);
 			in = new CipherInputStream(new FileInputStream(
 					file.getAbsolutePath()), cipher);
 		} catch (InvalidKeyException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchAlgorithmException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (NoSuchPaddingException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		} catch (FileNotFoundException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 		return in;
 
 	}
 
 	class MyCipherSource implements StreamResource.StreamSource {
 
 		private String filePath;
 
 		public MyCipherSource(String filePath) {
 			this.filePath = filePath;
 		}
 
 		@Override
 		public InputStream getStream() {
 			return getCipherInputputStream(new File(filePath));
 		}
 
 	}
 
 	public List<String> getAllRootFolderNames() throws Exception {
 		ArrayList<String> folderList = new ArrayList<String>();
 		File dir = new File(PersonalPassConstants.MAINDIR
 				+ PersonalPassConstants.FILES_METADATADIR
 				+ PasswordManager.getMd5Hash(PersonalpasssaveApplication
 						.getInstance().getBaseController().getCurrentUser()));
 		if (dir.exists()) {
 			File[] listFiles = dir.listFiles();
 			if (listFiles != null) {
 				for (File file : listFiles) {
 					FileInStore newFileInStore = new FileInStore();
 					CipherInputStream in;
 					OutputStream out;
 					Cipher cipher;
 					SecretKey key;
 					byte[] byteBuffer;
 					cipher = Cipher
 							.getInstance(PersonalPassConstants.ENCRYPTION_MODE);
 					key = new SecretKeySpec(
 							PersonalPassConstants.MAIN_SCHLUSSEL.getBytes(),
 							PersonalPassConstants.ENCRYPTION_MODE);
 					cipher.init(Cipher.DECRYPT_MODE, key);
 					in = new CipherInputStream(new FileInputStream(dir + "/"
 							+ file.getName()), cipher);
 					int read = 0;
 					StringBuffer buff = new StringBuffer();
 					while ((read = in.read()) != -1) {
 						buff.append((char) read);
 					}
 					ObjectMarshaller marshaller = new ObjectMarshaller();
 					Object fromXml = marshaller.fromXmlWithXStream(buff
 							.toString());
 					newFileInStore = (FileInStore) fromXml;
 					in.close();
 					System.out.println("folder search found:" + newFileInStore);
 					if (newFileInStore.getParentFoldername() != null) {
 						if (newFileInStore.getParentFoldername().trim()
 								.equals("")) {
 							folderList.add(newFileInStore.getFoldername());
 						}
 					}
 				}
 			}
 		}
 
 		return folderList;
 
 	}
 
 	public List<FileInStore> getAllFilesForSpecifiedForlder(String value)
 			throws Exception {
 		ArrayList<FileInStore> allFilesForUser = getAllFilesForUser();
 		ArrayList<FileInStore> resultList = new ArrayList<FileInStore>();
 
 		if (!PersonalPassConstants.FILE_ROOT_NAME.equals(value)) {
 			for (FileInStore fileInStore : allFilesForUser) {
 				if (fileInStore.getFoldername() != null) {
 					System.out.println("File " + fileInStore.getFileName()
 							+ " folder:" + fileInStore.getFoldername());
					if (fileInStore.getFoldername().equals(value)) {
 						resultList.add(fileInStore);
 					}
 				}
 			}
 			return resultList;
 		}
 		return allFilesForUser;
 	}
 
 }
