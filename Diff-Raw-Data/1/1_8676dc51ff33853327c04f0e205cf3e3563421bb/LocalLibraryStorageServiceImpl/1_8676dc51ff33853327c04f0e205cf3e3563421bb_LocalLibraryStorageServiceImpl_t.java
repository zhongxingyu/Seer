 /**
  * 
  */
 package com.senselessweb.soundcloud.storage.mongodb.service;
 
 import java.io.File;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Map;
 import java.util.Set;
 import java.util.StringTokenizer;
 import java.util.regex.Pattern;
 
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.mongodb.core.MongoTemplate;
 import org.springframework.data.mongodb.core.query.Criteria;
 import org.springframework.data.mongodb.core.query.Query;
 import org.springframework.stereotype.Service;
 
 import com.senselessweb.soundcloud.domain.library.LocalFile;
 import com.senselessweb.soundcloud.domain.sources.MediaSource;
 import com.senselessweb.soundcloud.util.FileInformationsReader;
 import com.senselessweb.storage.library.LocalLibraryStorageService;
 
 /**
  * Default implementation of the LocalLibraryStorageService.
  *
  * @author thomas
  */
 @Service
 public class LocalLibraryStorageServiceImpl implements LocalLibraryStorageService
 {
 	
 	/**
 	 * The collection used by this service.
 	 */
 	private static final String collectionName = "localLibraryCacheCollection";
 	
 	/**
 	 * The mongoTemplate
 	 */
 	@Autowired MongoTemplate mongoTemplate;
 	
 	/**
 	 * Caches the keywords 
 	 * 
 	 * TODO Move cache to db
 	 */
 	private final Map<String, Set<String>> cachedKeywords = Collections.synchronizedMap(new HashMap<String, Set<String>>());
 
 	/**
 	 * @see com.senselessweb.storage.library.LocalLibraryStorageService#getOrCreate(java.io.File)
 	 */
 	@Override
 	public synchronized LocalFile getOrCreate(final File input)
 	{
 		final LocalFile localFile = this.mongoTemplate.findOne(
 				new Query(Criteria.where("path").is(input.getAbsolutePath())), LocalFile.class, collectionName);
 
 		if (localFile != null && input.lastModified() <= localFile.getLastModified())
 			return localFile;
 		
 		final LocalFile newLocalFile = LocalFile.create(input.getAbsolutePath(), FileInformationsReader.read(input));
		this.mongoTemplate.remove(new Query(Criteria.where("path").is(input.getAbsolutePath())), collectionName);
 		this.mongoTemplate.insert(newLocalFile, collectionName);
 		this.cachedKeywords.clear();
 		return this.mongoTemplate.findOne(
 				new Query(Criteria.where("path").is(input.getAbsolutePath())), LocalFile.class, collectionName);
 	}
 	
 	/**
 	 * @see com.senselessweb.storage.library.LocalLibraryStorageService#getKeywords(java.lang.String, java.lang.String)
 	 */
 	@Override
 	public synchronized Set<String> getKeywords(final String basePath, final String path)
 	{
 		// Return an empty set if keywords for the root path are requested as they are way too many
 		if (StringUtils.isBlank(path)) return Collections.emptySet();
 		
 		if (this.cachedKeywords.containsKey(path)) return this.cachedKeywords.get(path);
 		
 		final Set<String> keywords = new HashSet<String>();
 		keywords.addAll(parseKeywords(path));
 		
 		final String regex = Pattern.quote(FilenameUtils.normalize(basePath + File.separator + path)) + "*";
 		for (final LocalFile localFile : this.mongoTemplate.find(new Query(
 				Criteria.where("path").regex(regex)), LocalFile.class, collectionName))
 		{
 			for (final String keyword : localFile.getKeywords()) keywords.addAll(parseKeywords(keyword));
 			keywords.addAll(parseKeywords(localFile.getLongTitle()));
 			keywords.addAll(parseKeywords(localFile.getShortTitle()));
 			keywords.addAll(parseKeywords(localFile.getLongTitle().trim().toLowerCase()));
 		}
 		
 		this.cachedKeywords.put(path, keywords);
 		return keywords;
 	}
 
 	/**
 	 * Parses the keywords out of a string
 	 * 
 	 * @param string The string to parse.
 	 * 
 	 * @return The keywords for that string.
 	 */
 	private static Set<String> parseKeywords(final String string)
 	{
 		final Set<String> result = new HashSet<String>();
 		final StringTokenizer st = new StringTokenizer(string, "-_./()");
 		while (st.hasMoreTokens())
 		{
 			final String s = st.nextToken();
 			
 			// Do not add string that only contain of numbers
 			try { Double.parseDouble(s); } catch (NumberFormatException e) { 
 				if (s.trim().length() >= 3) result.add(s.trim().toLowerCase()); 
 			}
 		}
 		return result;
 	}
 	
 	/**
 	 * @see com.senselessweb.storage.library.LocalLibraryStorageService#get(java.lang.String)
 	 */
 	@Override
 	public LocalFile get(final String id)
 	{
 		return this.mongoTemplate.findById(id, LocalFile.class, collectionName);
 	}
 
 	/**
 	 * @see com.senselessweb.storage.library.LocalLibraryStorageService#get(com.senselessweb.soundcloud.domain.sources.MediaSource)
 	 */
 	@Override
 	public LocalFile get(final MediaSource mediaSource)
 	{
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 }
