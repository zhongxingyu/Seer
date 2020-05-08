 /*
  * Copyright (c) 2011: Edmund Wagner, Wolfram Weidel, Lukas Gross
  * All rights reserved.
  * 
  * Redistribution and use in source and binary forms, with or without
  * modification, are permitted provided that the following conditions are met:
  * * Redistributions of source code must retain the above copyright
  * notice, this list of conditions and the following disclaimer.
  * * Redistributions in binary form must reproduce the above copyright
  * notice, this list of conditions and the following disclaimer in the
  * documentation and/or other materials provided with the distribution.
  * * Neither the name of the jeconfig nor the
  * names of its contributors may be used to endorse or promote products
  * derived from this software without specific prior written permission.
  * 
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
  * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
  * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE AUTHORS BE LIABLE FOR ANY
  * DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
  * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
  * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
  * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
  * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
  * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 
 package org.jeconfig.filepersister;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.Collection;
 import java.util.LinkedList;
 import java.util.Map;
 
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.filefilter.FileFilterUtils;
 import org.apache.commons.io.filefilter.TrueFileFilter;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.jeconfig.api.dto.ComplexConfigDTO;
 import org.jeconfig.api.exception.StaleConfigException;
 import org.jeconfig.api.exception.StoreConfigException;
 import org.jeconfig.api.persister.IConfigPersister;
 import org.jeconfig.api.persister.IScopePathGenerator;
 import org.jeconfig.api.scope.IScopePath;
 import org.jeconfig.api.util.Assert;
 import org.jeconfig.server.marshalling.IConfigMarshaller;
 import org.jeconfig.server.persister.DefaultScopePathGenerator;
 
 public final class FileConfigPersister implements IConfigPersister {
 
 	public static final String ID = FileConfigPersister.class.getName();
 	private static final Log LOG = LogFactory.getLog(FileConfigPersister.class);
 	private final IConfigMarshaller marshaller;
 	private final IScopePathGenerator gen;
 	private final String rootDirectory;
 	private final String fileExtension;
 
 	public FileConfigPersister(final IConfigMarshaller marshaller, final String rootDirectory, final String fileExtension) {
 		Assert.paramNotNull(rootDirectory, "rootDirectory"); //$NON-NLS-1$
 		Assert.paramNotNull(marshaller, "marshaller"); //$NON-NLS-1$
 		Assert.paramNotNull(fileExtension, "fileExtension"); //$NON-NLS-1$
 		this.gen = new DefaultScopePathGenerator(File.separator);
 		this.marshaller = marshaller;
 		this.fileExtension = fileExtension;
 		this.rootDirectory = rootDirectory;
 	}
 
 	@Override
 	public String getId() {
 		return ID;
 	}
 
 	@Override
 	public ComplexConfigDTO loadConfiguration(final IScopePath scopePath) {
 		Assert.paramNotNull(scopePath, "scopePath"); //$NON-NLS-1$
 
 		final File configFile = new File(rootDirectory
 			+ File.separator
 			+ gen.getPathFromScopePath(scopePath)
 			+ File.separator
 			+ gen.createName(scopePath)
 			+ fileExtension);
 		FileInputStream in = null;
 		try {
 			in = FileUtils.openInputStream(configFile);
 			final ComplexConfigDTO configDTO = marshaller.unmarshal(in);
 			if (configDTO.getVersion() < 1) {
 				throw new StoreConfigException(
 					"Illegal config version. Must be 1 or higher. Occures at scope Path '" + scopePath + "'!"); //$NON-NLS-1$ //$NON-NLS-2$
 			}
 			configDTO.setDefiningScopePath(scopePath);
 			return configDTO;
 		} catch (final FileNotFoundException e) {
 			return null;
 		} catch (final IOException e) {
 			throw new StoreConfigException("Error while loading configuration for the scope Path '" + scopePath + "'!", e); //$NON-NLS-1$ //$NON-NLS-2$
 		} finally {
 			try {
 				if (in != null) {
 					in.close();
 				}
 			} catch (final IOException e) {
 				LOG.warn("Error while closing inputStream for configuration with scopepath '" + scopePath + "'!", e); //$NON-NLS-1$//$NON-NLS-2$
 			}
 		}
 	}
 
 	@Override
 	public void updateConfiguration(final ComplexConfigDTO configDTO) {
 		Assert.paramNotNull(configDTO, "configuration"); //$NON-NLS-1$
 		final File configFile = new File(rootDirectory
 			+ File.separator
 			+ gen.getPathFromScopePath(configDTO.getDefiningScopePath())
 			+ File.separator
 			+ gen.createName(configDTO.getDefiningScopePath())
 			+ fileExtension);
 
 		if (!configFile.exists()) {
 			throw new StaleConfigException(
 				configDTO.getDefiningScopePath(),
 				"There exists no record for the scope path '" + configDTO.getDefiningScopePath() + "'!"); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		final long sourceVersion = loadConfiguration(configDTO.getDefiningScopePath()).getVersion();
 		final long destinationVersion = configDTO.getVersion();
 		if (sourceVersion >= destinationVersion) {
 			throw new StaleConfigException(
 				configDTO.getDefiningScopePath(),
 				"The saved Data has a newer or the same version. Occures at scope path '" + configDTO.getDefiningScopePath() + "'!"); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		if (sourceVersion < 1) {
 			throw new StoreConfigException(
 				"Illegal config version. Must be 1 or higher. Occures at scope Path '" + configDTO.getDefiningScopePath() + "'!"); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		final boolean deleted = configFile.delete();
 		if (!deleted) {
 			throw new StoreConfigException(
 				"Couldn't update configuration. Deleting old configuration failed. Occures at scope Path '" + configDTO.getDefiningScopePath() + "'!"); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		FileOutputStream out = null;
 		try {
 			out = FileUtils.openOutputStream(configFile);
 			marshaller.marshal(out, configDTO);
 		} catch (final FileNotFoundException e) {
 			throw new StoreConfigException(
 				"Error while updating configuration for scope Path '" + configDTO.getDefiningScopePath() + "'!", e); //$NON-NLS-1$//$NON-NLS-2$
 		} catch (final IOException e) {
 			throw new StoreConfigException(
 				"Error while updating configuration for scope Path '" + configDTO.getDefiningScopePath() + "'!", e); //$NON-NLS-1$ //$NON-NLS-2$
 		} finally {
 			if (out != null) {
 				try {
 					out.close();
 				} catch (final IOException e) {
 					LOG.warn(
 							"Error while closing outputStream for configuration with scopepath '" + configDTO.getDefiningScopePath() + "'!", e); //$NON-NLS-1$//$NON-NLS-2$
 				}
 			}
 		}
 	}
 
 	@Override
 	public void saveConfiguration(final ComplexConfigDTO configDTO) {
 		Assert.paramNotNull(configDTO, "configuration"); //$NON-NLS-1$
 
 		if (configDTO.getVersion() < 1) {
 			throw new StoreConfigException(
 				"Illegal config version. Must be 1 or higher. Occures at scope Path '" + configDTO.getDefiningScopePath() + "'!"); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		final File configFile = new File(rootDirectory
 			+ File.separator
 			+ gen.getPathFromScopePath(configDTO.getDefiningScopePath())
 			+ File.separator
 			+ gen.createName(configDTO.getDefiningScopePath())
 			+ fileExtension);
 
 		if (configFile.exists()) {
 			throw new StaleConfigException(
 				configDTO.getDefiningScopePath(),
 				"Can't save new configuration. There exists already a file for the scope path '" + configDTO.getDefiningScopePath() + "'!"); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		try {
 			configFile.getParentFile().mkdirs();
 			configFile.createNewFile();
 		} catch (final IOException e) {
 			throw new StoreConfigException(
 				"Error while creating new file. Occures at scope path '" + configDTO.getDefiningScopePath() + "'!", e); //$NON-NLS-1$ //$NON-NLS-2$
 		}
 		FileOutputStream out = null;
 		try {
 			out = FileUtils.openOutputStream(configFile);
 			marshaller.marshal(out, configDTO);
 		} catch (final FileNotFoundException e) {
 			throw new StoreConfigException(
 				"Error while saving configuration. Occures at scope path '" + configDTO.getDefiningScopePath() + "'!", e); //$NON-NLS-1$ //$NON-NLS-2$
 		} catch (final IOException e) {
 			throw new StoreConfigException("Error while saving configuration." + configDTO.getDefiningScopePath() + "'!", e); //$NON-NLS-1$ //$NON-NLS-2$
 		} finally {
 			if (out != null) {
 				try {
 					out.close();
 				} catch (final IOException e) {
 					LOG.warn(
 							"Error while closing outputStream for configuration with scopepath '" + configDTO.getDefiningScopePath() + "'!", e); //$NON-NLS-1$//$NON-NLS-2$
 				}
 			}
 		}
 	}
 
 	@Override
 	public void delete(final IScopePath scopePath, final boolean deleteChildren) {
 		Assert.paramNotNull(scopePath, "scopePath"); //$NON-NLS-1$
 
 		final File configFile = new File(rootDirectory, gen.getPathFromScopePath(scopePath)
 			+ File.separator
 			+ gen.createName(scopePath)
 			+ fileExtension);
 
 		if (deleteChildren == false) {
 			deleteConfigFileIfExists(configFile);
 		} else {
 			deleteConfigFileIfExists(configFile);
 			final Collection<?> configFiles = FileUtils.listFiles(
 					configFile.getParentFile(),
 					FileFilterUtils.nameFileFilter(gen.createName(scopePath) + fileExtension),
 					TrueFileFilter.INSTANCE);
 			for (final Object file : configFiles) {
 				deleteConfigFileIfExists((File) file);
 			}
 		}
 	}
 
 	@Override
 	public void deleteAllOccurences(final String scopeName, final Map<String, String> properties) {
 		Assert.paramNotNull(scopeName, "scopeName"); //$NON-NLS-1$
 		Assert.paramNotNull(properties, "properties"); //$NON-NLS-1$
 
 		final Collection<?> files = FileUtils.listFiles(
 				new File(rootDirectory),
 				FileFilterUtils.fileFileFilter(),
 				TrueFileFilter.INSTANCE);
 		final String searchedPathPart = gen.buildScopeWithProperty(scopeName, properties);
 		for (final Object file : files) {
 			final String path = ((File) file).getAbsolutePath();
 			if (path.contains(searchedPathPart)) {
 				deleteConfigFileIfExists(((File) file));
 			}
 		}
 	}
 
 	@Override
 	public Collection<IScopePath> listScopes(final String scopeName, final Map<String, String> properties) {
 		Assert.paramNotNull(scopeName, "scopeName"); //$NON-NLS-1$
 		Assert.paramNotNull(properties, "properties"); //$NON-NLS-1$
 
 		final Collection<?> files = FileUtils.listFiles(
 				new File(rootDirectory),
 				FileFilterUtils.fileFileFilter(),
 				TrueFileFilter.INSTANCE);
 		final Collection<String> paths = new LinkedList<String>();
 		for (final Object file : files) {
 			final String path = ((File) file).getAbsolutePath();
 			final String tmpPath = path.replace(rootDirectory, ""); //$NON-NLS-1$
 			paths.add(tmpPath);
 		}
 		return gen.createScopePaths(paths, scopeName, properties);
 	}
 
 	private void deleteConfigFileIfExists(final File file) {
 		if (file.exists()) {
 			final boolean deleted = file.delete();
 			if (deleted == false) {
 				throw new StoreConfigException("Error while deleting configuration."); //$NON-NLS-1$
 			}
 		}
 		final File[] dir = file.getParentFile().listFiles();
 		if (dir != null && dir.length == 0) {
 			try {
 				FileUtils.deleteDirectory(file.getParentFile());
 			} catch (final IOException e) {
 				throw new StoreConfigException("Error while deleting configuration.", e); //$NON-NLS-1$
 			}
 		}
 	}
 
 }
