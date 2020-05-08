 /*******************************************************************************
  * Copyright (c) May 18, 2011 Zend Technologies Ltd. 
  * All rights reserved. This program and the accompanying materials 
  * are made available under the terms of the Eclipse Public License v1.0 
  * which accompanies this distribution, and is available at 
  * http://www.eclipse.org/legal/epl-v10.html  
  *******************************************************************************/
 package org.zend.sdklib.internal.target;
 
 import java.io.File;
 import java.io.FileFilter;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.Properties;
 
import org.zend.sdklib.internal.utils.EnvironmentUtils;
import org.zend.sdklib.logger.Log;
 import org.zend.sdklib.target.ITargetLoader;
 import org.zend.sdklib.target.IZendTarget;
 
 /**
  * Default persistence layer for targets
  * 
  * @author Roy, 2011
  */
 public class UserBasedTargetLoader implements ITargetLoader {
 
 	private static final String PROPERTY = "target";
 	private static final String INI_EXTENSION = ".ini";
 	private static final String CONF_FILENAME = "conf";
 	private final File baseDir;
 
 	public UserBasedTargetLoader() {
 		this(getDefaultTargetsDirectory());
 	}
 
 	public UserBasedTargetLoader(File baseDir) {
 		this.baseDir = baseDir;
 
 		if (!baseDir.exists()) {
 			throw new IllegalStateException("error finding user home directory");
 		}
 	}
 
 	public static File getDefaultTargetsDirectory() {
 		final String property = System.getProperty("user.home");
 		final File user = new File(property);
 		if (user.exists()) {
 			final File targetsDir = new File(user, ".zend" + File.separator
 					+ "targets");
 			if (!targetsDir.isDirectory()) {
 				targetsDir.mkdirs();
 			}
 			return targetsDir;
 		} else {
 			throw new IllegalStateException("error finding user home directory");
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.zend.sdklib.target.ITargetLoader#add(org.zend.sdklib.target.IZendTarget
 	 * )
 	 */
 	@Override
 	public IZendTarget add(IZendTarget target) {
 		if (target == null) {
 			throw new IllegalArgumentException("target is null");
 		}
 
 		final File df = getDescriptorFile(target.getId());
 		TargetDescriptor descriptor = loadTargetDescriptor(df);
 		if (descriptor != null) {
 			throw new IllegalArgumentException("target already exists");
 		}
 
 		// create descriptor
 		descriptor = storeTargetDescriptor(target);
 		if (null == descriptor) {
 			return null;
 		}
 
 		File confFile = new File(descriptor.path, CONF_FILENAME);
 		try {
 			confFile.createNewFile();
			grantPermissions(confFile.getAbsolutePath(), "600");
 			FileOutputStream fos = new FileOutputStream(confFile);
 			target.store(fos);
 			fos.close();
 		} catch (IOException e1) {
 			return null;
 		}
 
 		return target;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.zend.sdklib.target.ITargetLoader#remove(org.zend.sdklib.target.
 	 * IZendTarget)
 	 */
 	@Override
 	public IZendTarget remove(IZendTarget target) {
 		final File df = getDescriptorFile(target.getId());
 		TargetDescriptor d = loadTargetDescriptor(df);
 		if (null == d) {
 			throw new IllegalArgumentException("cannot find target"
 					+ target.getId());
 		}
 		final File descriptorFile = df;
 
 		d.path.deleteOnExit();
 		final boolean delete2 = descriptorFile.delete();
 
 		if (!delete2) {
 			throw new IllegalArgumentException("error deleting data");
 		}
 
 		return target;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.zend.sdklib.target.ITargetLoader#update(org.zend.sdklib.target.
 	 * IZendTarget)
 	 */
 	@Override
 	public IZendTarget update(IZendTarget target) {
 		if (target == null) {
 			throw new IllegalArgumentException("target is null");
 		}
 
 		final File df = getDescriptorFile(target.getId());
 		TargetDescriptor descriptor = loadTargetDescriptor(df);
 		if (descriptor == null) {
 			throw new IllegalArgumentException("target does not exists");
 		}
 
 		File confFile = new File(descriptor.path, CONF_FILENAME);
 		if (confFile.exists()) {
 			confFile.delete();
 		}
 		try {
 			FileOutputStream fos = new FileOutputStream(confFile);
 			target.store(fos);
 			fos.close();
 		} catch (IOException e1) {
 			return null;
 		}
 
 		return target;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.zend.sdklib.target.ITargetLoader#loadAll()
 	 */
 	@Override
 	public IZendTarget[] loadAll() {
 		final File[] targets = baseDir.listFiles(new FileFilter() {
 
 			@Override
 			public boolean accept(File file) {
 				return file.getName().endsWith(INI_EXTENSION) && file.isFile();
 			}
 
 		});
 
 		final ArrayList<IZendTarget> arrayList = new ArrayList<IZendTarget>(
 				targets.length);
 		for (File file : targets) {
 			final TargetDescriptor d = loadTargetDescriptor(file);
 			if (d.isValid()) {
 				File confFile = new File(d.path, CONF_FILENAME);
 				try {
 					InputStream is = new FileInputStream(confFile);
 					final ZendTarget target = new ZendTarget();
 					target.load(is);
 					arrayList.add(target);
 				} catch (IOException e1) {
 					// skip target loading
 				}
 			}
 		}
 
 		return (IZendTarget[]) arrayList.toArray(new IZendTarget[arrayList
 				.size()]);
 	}
 
 	private TargetDescriptor storeTargetDescriptor(IZendTarget target) {
 		try {
 			final File file = getDescriptorFile(target.getId());
 			if (!file.createNewFile()) {
 				return null;
 			}
 			final TargetDescriptor targetDescriptor = new TargetDescriptor();
 			targetDescriptor.target = target.getId();
 			targetDescriptor.path = new File(baseDir, targetDescriptor.target);
 
 			final Properties properties = new Properties();
 			properties.put(PROPERTY, targetDescriptor.target);
 			properties.put("path", targetDescriptor.path.getAbsolutePath());
 
 			final FileOutputStream fileOutputStream = new FileOutputStream(file);
 			properties.store(fileOutputStream, "descriptor for target "
 					+ target.getId());
 
 			fileOutputStream.close();
 			targetDescriptor.path.mkdir();
			grantPermissions(targetDescriptor.path.getAbsolutePath(), "700");
 
 			return targetDescriptor.isValid() ? targetDescriptor : null;
 		} catch (IOException e) {
 			// can't be identified as valid target - ignore
 			return null;
 		}
 	}
 
 	public TargetDescriptor loadTargetDescriptor(File file) {
 		try {
 			if (!file.exists()) {
 				return null;
 			}
 
 			final Properties properties = new Properties();
 			final FileInputStream fileInputStream = new FileInputStream(file);
 			properties.load(fileInputStream);
 
 			final TargetDescriptor targetDescriptor = new TargetDescriptor();
 			targetDescriptor.target = properties.getProperty(PROPERTY);
 			targetDescriptor.path = new File(properties.getProperty("path"));
 
 			fileInputStream.close();
 			return targetDescriptor.isValid() ? targetDescriptor : null;
 		} catch (IOException e) {
 			// can't be identified as valid target - ignore
 			return null;
 		}
 	}
 
 	public File getDescriptorFile(String target) {
 		if (!target.endsWith(INI_EXTENSION)) {
 			target = target + INI_EXTENSION;
 		}
 		final File file = new File(baseDir, target);
 		return file;
 	}
 
 	/**
 	 * Holds the name and path of a target
 	 */
 	public class TargetDescriptor {
 
 		/**
 		 * Name of the target
 		 */
 		public String target;
 
 		/**
 		 * Path of the target directory
 		 */
 		public File path;
 
 		public boolean isValid() {
 			return this.target != null && this.path.exists();
 		}
 	}
 
	private boolean grantPermissions(String path, String permissions) {
		if (EnvironmentUtils.isUnderLinux() || EnvironmentUtils.isUnderMaxOSX()) {
			try {
				Runtime.getRuntime().exec("chmod " + permissions + " " + path);
			} catch (IOException e) {
				Log.getInstance().getLogger(this.getClass().getName()).error(e);
				return false;
			}
		}
		return true;
	}

 }
