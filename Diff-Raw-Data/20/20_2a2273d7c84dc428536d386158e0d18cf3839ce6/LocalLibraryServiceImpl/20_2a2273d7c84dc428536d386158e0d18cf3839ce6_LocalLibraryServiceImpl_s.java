 package com.senselessweb.soundcloud.library.service.local.impl;
 
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.List;
 
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 import com.senselessweb.soundcloud.domain.library.LocalFile;
 import com.senselessweb.soundcloud.domain.library.LocalFolder;
 import com.senselessweb.soundcloud.domain.library.LocalSubfolder;
 import com.senselessweb.soundcloud.domain.sources.MediaSource;
 import com.senselessweb.soundcloud.library.service.local.LocalLibraryService;
 import com.senselessweb.soundcloud.util.FileFilters;
 import com.senselessweb.storage.library.LocalLibraryStorageService;
 
 /**
  * Default implementation of the {@link LocalLibraryService}
  *
  * @author thomas
  */
 @Service
 public class LocalLibraryServiceImpl implements LocalLibraryService
 {
 	
 	/**
 	 * The root folder
 	 * 
 	 * TODO Make this configurable
 	 */
 	static final String root = System.getProperty("soundcloud.library");
 	
 	
 	/**
 	 * The localLibraryStorageService
 	 */
 	@Autowired LocalLibraryStorageService localLibraryStorageService;
 	
 	/**
 	 * @see com.senselessweb.soundcloud.library.service.local.LocalLibraryService#getFolder(java.lang.String)
 	 */
 	@Override
 	public LocalFolder getFolder(final String folder)
 	{
 		final File dir = StringUtils.isBlank(folder) ? new File(root) : new File(root, folder);
 		
 		final List<LocalSubfolder> subfolders = Lists.newArrayList(Collections2.transform(Lists.newArrayList(dir.listFiles(FileFilters.directoryFilter)), new Function<File, LocalSubfolder>() {
 			/** @see com.google.common.base.Function#apply(java.lang.Object) */
 			@Override public LocalSubfolder apply(final File input) 
 			{ 
 				return new LocalSubfolder(input.getName(), 
 						LocalLibraryServiceImpl.this.localLibraryStorageService.getKeywords(root, 
 								(StringUtils.isBlank(folder) ? "" : (folder + File.separator)) + input.getName())); 
 			}
 		}));
 		Collections.sort(subfolders);
 		
 		final List<LocalFile> files = Lists.newArrayList(Collections2.transform(Lists.newArrayList(dir.listFiles(FileFilters.mediaFileFilter)), new Function<File, LocalFile>() {
 			/** @see com.google.common.base.Function#apply(java.lang.Object) */
 			@Override public LocalFile apply(final File input) 
 			{
 				return LocalLibraryServiceImpl.this.localLibraryStorageService.getOrCreate(input);
 			}
 		}));
 		Collections.sort(files);
 		
		return new LocalFolder(dir.getName(), dir.getAbsolutePath().substring(root.length()), 
 				subfolders, files, this.localLibraryStorageService.getKeywords(root, folder));
 	}
 	
 	/**
 	 * @see com.senselessweb.soundcloud.library.service.local.LocalLibraryService#getFiles(java.lang.String)
 	 */
 	@Override
 	public Collection<LocalFile> getFiles(final String folder)
 	{
 		final List<LocalFile> result = new ArrayList<LocalFile>();
 		final LocalFolder localFolder = this.getFolder(folder);
 		
 		result.addAll(localFolder.getFiles());
 		for (final LocalSubfolder subfolder : localFolder.getSubfolders())
 			result.addAll(this.getFiles(folder + "/" + subfolder.getName()));
 		
 		return result;
 	}
 	
 	/**
 	 * @see com.senselessweb.soundcloud.library.service.local.LocalLibraryService#getFile(java.lang.String)
 	 */
 	@Override
 	public LocalFile getFile(final String id) 
 	{
 		return this.localLibraryStorageService.get(id);
 	}
 	
 	/**
 	 * @see com.senselessweb.soundcloud.library.service.local.LocalLibraryService#getFile(com.senselessweb.soundcloud.domain.sources.MediaSource)
 	 */
 	@Override
 	public LocalFile getFile(final MediaSource mediaSource)
 	{
 		return this.localLibraryStorageService.get(mediaSource);
 	}
 	
 }
