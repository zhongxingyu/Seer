 /**
  * Copyright (c) 2000-2012 Liferay, Inc. All rights reserved.
  *
  * This library is free software; you can redistribute it and/or modify it under
  * the terms of the GNU Lesser General Public License as published by the Free
  * Software Foundation; either version 2.1 of the License, or (at your option)
  * any later version.
  *
  * This library is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
  * FOR A PARTICULAR PURPOSE. See the GNU Lesser General Public License for more
  * details.
  */
 
 package com.liferay.hadoop.store;
 
 import com.liferay.hadoop.util.HadoopManager;
 import com.liferay.portal.kernel.exception.PortalException;
 import com.liferay.portal.kernel.exception.SystemException;
 import com.liferay.portal.kernel.util.StreamUtil;
 import com.liferay.portal.kernel.util.StringBundler;
 import com.liferay.portal.kernel.util.StringPool;
 import com.liferay.portal.kernel.util.Validator;
 import com.liferay.portlet.documentlibrary.DuplicateFileException;
 import com.liferay.portlet.documentlibrary.store.BaseStore;
 
 import java.io.IOException;
 import java.io.InputStream;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.hadoop.fs.FSDataOutputStream;
 import org.apache.hadoop.fs.FileStatus;
 import org.apache.hadoop.fs.FileSystem;
 import org.apache.hadoop.fs.Path;
 import org.apache.hadoop.fs.permission.FsPermission;
 
 /**
  * @author Raymond Augé
  */
 public class HDFSStore extends BaseStore {
 
 	@Override
 	public void addDirectory(long companyId, long repositoryId, String dirName)
 		throws PortalException, SystemException {
 
 		Path fullPath = getFullDirPath(companyId, repositoryId, dirName);
 
 		FileSystem fileSystem = HadoopManager.getFileSystem();
 
 		try {
 			fileSystem.mkdirs(fullPath, FsPermission.getDefault());
 		}
 		catch (IOException ioe) {
 			throw new SystemException(ioe);
 		}
 	}
 
 	@Override
 	public void addFile(
 			long companyId, long repositoryId, String fileName, InputStream is)
 		throws PortalException, SystemException {
 
 		Path fullPath = getFullVersionFilePath(
 			companyId, repositoryId, fileName, VERSION_DEFAULT);
 
 		FileSystem fileSystem = HadoopManager.getFileSystem();
 
 		FSDataOutputStream outputStream = null;
 
 		try {
 			outputStream = fileSystem.create(fullPath);
 
 			StreamUtil.transfer(is, outputStream, false);
 		}
 		catch (IOException ioe) {
 			throw new SystemException(ioe);
 		}
 		finally {
 			StreamUtil.cleanUp(outputStream);
 		}
 	}
 
 	@Override
 	public void checkRoot(long companyId) throws SystemException {
 	}
 
 	@Override
 	public void deleteDirectory(
 			long companyId, long repositoryId, String dirName)
 		throws PortalException, SystemException {
 
 		Path fullPath = getFullDirPath(companyId, repositoryId, dirName);
 
 		FileSystem fileSystem = HadoopManager.getFileSystem();
 
 		try {
 			fileSystem.delete(fullPath, true);
 		}
 		catch (IOException ioe) {
 			throw new SystemException(ioe);
 		}
 	}
 
 	@Override
 	public void deleteFile(
 			long companyId, long repositoryId, String fileName)
 		throws PortalException, SystemException {
 
 		deleteFile(companyId, repositoryId, fileName, StringPool.BLANK);
 	}
 
 	@Override
 	public void deleteFile(
 			long companyId, long repositoryId, String fileName,
 			String versionLabel)
 		throws PortalException, SystemException {
 
 		Path fullPath = getFullVersionFilePath(
 			companyId, repositoryId, fileName, versionLabel);
 
 		FileSystem fileSystem = HadoopManager.getFileSystem();
 
 		try {
 			fileSystem.delete(fullPath, true);
 		}
 		catch (IOException ioe) {
 			throw new SystemException(ioe);
 		}
 	}
 
 	@Override
 	public InputStream getFileAsStream(
 			long companyId, long repositoryId, String fileName,
 			String versionLabel)
 		throws PortalException, SystemException {
 
 		Path fullPath = getFullVersionFilePath(
 			companyId, repositoryId, fileName, versionLabel);
 
 		FileSystem fileSystem = HadoopManager.getFileSystem();
 
 		try {
 			if (!fileSystem.exists(fullPath)) {
 				throw new PortalException(
 					"File " + fullPath.toUri().toString() + " does not exist");
 			}
 
 			return fileSystem.open(fullPath);
 		}
 		catch (IOException ioe) {
 			throw new SystemException(ioe);
 		}
 	}
 
 	public String[] getFileNames(long companyId, long repositoryId)
 		throws SystemException {
 
 		return getFileNames(companyId, repositoryId);
 	}
 
 	@Override
 	public String[] getFileNames(
 			long companyId, long repositoryId, String dirName)
 		throws SystemException {
 
 		Path fullPath = getFullDirPath(companyId, repositoryId, dirName);
 
 		FileSystem fileSystem = HadoopManager.getFileSystem();
 
 		try {
 			FileStatus[] listStatus = fileSystem.listStatus(fullPath);
 
 			if ((listStatus == null) || (listStatus.length < 1)) {
 				return new String[0];
 			}
 
 			List<String> fileNameList = new ArrayList<String>(
 				listStatus.length);
 
 			for (FileStatus fileStatus : listStatus) {
 
 				// TODO omit folders?
 
 				//if (fileStatus.isDir()) {
 				//	continue;
 				//}
 
 				String fileStatusPathString = fileStatus.getPath().toString();
 
 				int pos = fileStatusPathString.indexOf(dirName);
 
 				if (pos != -1) {
 					fileStatusPathString = fileStatusPathString.substring(pos);
 				}
 
 				fileNameList.add(fileStatusPathString);
 			}
 
 			return fileNameList.toArray(new String[fileNameList.size()]);
 		}
 		catch (IOException ioe) {
 			throw new SystemException(ioe);
 		}
 	}
 
 	@Override
 	public long getFileSize(long companyId, long repositoryId, String fileName)
 		throws PortalException, SystemException {
 
 		Path fullPath = getFullVersionFilePath(
 			companyId, repositoryId, fileName, VERSION_DEFAULT);
 
 		FileSystem fileSystem = HadoopManager.getFileSystem();
 
 		try {
 			if (!fileSystem.exists(fullPath)) {
 				throw new PortalException(
 					"File " + fullPath.toUri().toString() + " does not exist");
 			}
 
 			FileStatus fileStatus = fileSystem.getFileStatus(fullPath);
 
			return fileStatus.getBlockSize();
 		}
 		catch (IOException ioe) {
 			throw new SystemException(ioe);
 		}
 	}
 
 	@Override
 	public boolean hasDirectory(
 			long companyId, long repositoryId, String dirName)
 		throws PortalException, SystemException {
 
 		Path fullPath = getFullDirPath(companyId, repositoryId, dirName);
 
 		FileSystem fileSystem = HadoopManager.getFileSystem();
 
 		try {
 			return fileSystem.exists(fullPath);
 		}
 		catch (IOException ioe) {
 			throw new SystemException(ioe);
 		}
 	}
 
 	@Override
 	public boolean hasFile(
 			long companyId, long repositoryId, String fileName,
 			String versionLabel)
 		throws PortalException, SystemException {
 
 		Path fullPath = getFullVersionFilePath(
 			companyId, repositoryId, fileName, versionLabel);
 
 		FileSystem fileSystem = HadoopManager.getFileSystem();
 
 		try {
 			return fileSystem.exists(fullPath);
 		}
 		catch (IOException ioe) {
 			throw new SystemException(ioe);
 		}
 	}
 
 	@Override
 	public void move(String srcDir, String destDir) throws SystemException {
 	}
 
 	@Override
 	public void updateFile(
 			long companyId, long repositoryId, long newRepositoryId,
 			String fileName)
 		throws PortalException, SystemException {
 
 		Path sourcePath = getFullVersionFilePath(
 			companyId, repositoryId, fileName, VERSION_DEFAULT);
 		Path targetPath = getFullVersionFilePath(
 			companyId, newRepositoryId, fileName, VERSION_DEFAULT);
 
 		FileSystem fileSystem = HadoopManager.getFileSystem();
 
 		try {
 			if (fileSystem.exists(targetPath)) {
 				throw new DuplicateFileException(fileName);
 			}
 
 			if (!fileSystem.exists(sourcePath)) {
 				throw new PortalException(
 					"File " + sourcePath.toUri().toString() + " does not exist");
 			}
 
 			boolean renamed = fileSystem.rename(sourcePath, targetPath);
 
 			if (!renamed) {
 				throw new SystemException(
 					"File name directory was not renamed from " +
 						sourcePath.toUri().toString() + " to " +
 							targetPath.toUri().toString());
 			}
 		}
 		catch (IOException ioe) {
 			throw new SystemException(ioe);
 		}
 	}
 
 	public void updateFile(
 			long companyId, long repositoryId, String fileName,
 			String newFileName)
 		throws PortalException, SystemException {
 
 		Path sourcePath = getFullVersionFilePath(
 			companyId, repositoryId, fileName, VERSION_DEFAULT);
 		Path targetPath = getFullVersionFilePath(
 			companyId, repositoryId, newFileName, VERSION_DEFAULT);
 
 		FileSystem fileSystem = HadoopManager.getFileSystem();
 
 		try {
 			if (fileSystem.exists(targetPath)) {
 				throw new DuplicateFileException(fileName);
 			}
 
 			if (!fileSystem.exists(sourcePath)) {
 				throw new PortalException(
 					"File " + sourcePath.toUri().toString() + " does not exist");
 			}
 
 			boolean renamed = fileSystem.rename(sourcePath, targetPath);
 
 			if (!renamed) {
 				throw new SystemException(
 					"File name directory was not renamed from " +
 						sourcePath.toUri().toString() + " to " +
 							targetPath.toUri().toString());
 			}
 		}
 		catch (IOException ioe) {
 			throw new SystemException(ioe);
 		}
 	}
 
 	@Override
 	public void updateFile(
 			long companyId, long repositoryId, String fileName,
 			String versionLabel, InputStream inputStream)
 		throws PortalException, SystemException {
 
 		Path fullPath = getFullVersionFilePath(
 			companyId, repositoryId, fileName, versionLabel);
 
 		FileSystem fileSystem = HadoopManager.getFileSystem();
 
 		FSDataOutputStream outputStream = null;
 
 		try {
 			outputStream = fileSystem.create(fullPath);
 
 			StreamUtil.transfer(inputStream, outputStream, false);
 		}
 		catch (IOException ioe) {
 			throw new SystemException(ioe);
 		}
 		finally {
 			StreamUtil.cleanUp(outputStream);
 		}
 	}
 
 	private String getFullDirName(
 		long companyId, long repositoryId, String dirName) {
 
 		StringBundler sb = new StringBundler(5);
 
 		sb.append(StringPool.SLASH);
 		sb.append(companyId);
 		sb.append(StringPool.SLASH);
 		sb.append(repositoryId);
 
 		if (Validator.isNotNull(dirName)) {
 			sb.append(StringPool.SLASH);
 			sb.append(dirName);
 		}
 
 		return sb.toString();
 	}
 
 	private Path getFullDirPath(
 		long companyId, long repositoryId, String dirName) {
 
 		return new Path(getFullDirName(companyId, repositoryId, dirName));
 	}
 
 	private String getFullVersionFileName(
 		long companyId, long repositoryId, String fileName, String version) {
 
 		StringBundler sb = new StringBundler(3);
 
 		sb.append(getFullDirName(companyId, repositoryId, fileName));
 		sb.append(StringPool.SLASH);
 
 		if (Validator.isNull(version)) {
 			sb.append(VERSION_DEFAULT);
 		}
 		else {
 			sb.append(version);
 		}
 
 		return sb.toString();
 	}
 
 	private Path getFullVersionFilePath(
 		long companyId, long repositoryId, String fileName, String version) {
 
 		return new Path(
 			getFullVersionFileName(companyId, repositoryId, fileName, version));
 	}
 
 }
