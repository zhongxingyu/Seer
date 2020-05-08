 package varviewer.server.sampleSource;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.LinkedList;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import varviewer.server.variant.VariantCollection;
 import varviewer.shared.HasVariants;
 import varviewer.shared.SampleInfo;
 import varviewer.shared.SampleTreeNode;
 
 /**
  * A VariantServer that just wraps the most recently requested variants in memory
  * for faster response times. 
  * @author brendan
  *
  */
 public class CachingSampleSource implements SampleSource {
 
	int samplesToCache = 4;
 	private SampleSource source;
 	
 	private List<CachedSample> cache = new LinkedList<CachedSample>();
 	
 	public CachingSampleSource(SampleSource sampleSource) {
 		this.source = sampleSource;
 	}
 	
 	@Override
 	public VariantCollection getVariantsForSample(SampleInfo info) {
 		CachedSample sample = getCachedSample(info);
 		
 		if (sample != null) {
 			//Sweet, cache hit
 			bumpToFront(info);
 			return sample.vars;
 		}
 		
 		Logger.getLogger(CachingSampleSource.class).info("Cache miss for sample " + info.getSampleID() + ", loading new set of variants");
 		
 		try {
 			//Force re-loading of sample info
 			source.initialize();
 			addToCache(info, source.getVariantsForSample(info));
 		} catch (IOException e) {
 			Logger.getLogger(CachingSampleSource.class).warn("IOError re-loading variants: " + e.getMessage());
 			e.printStackTrace();
 		}
 		
 		sample = getCachedSample(info);
 		if (sample != null) {
 			return sample.vars;
 		}
 		else {
 			return null;
 		}
 	}
 
 
 
 	@Override
 	public HasVariants getHasVariantsForSample(SampleInfo info) {
 		return getVariantsForSample(info);
 	}
 
 	@Override
 	public File getBAMFileForSample(SampleInfo info) {
 		return source.getBAMFileForSample(info);
 	}
 	
 	@Override
 	public void initialize() throws IOException {
 		source.initialize();
 	}
 
 
 	@Override
 	public boolean containsSample(SampleInfo info) {
 		return source.containsSample(info);
 	}
 
 
 	@Override
 	public List<SampleInfo> getAllSamples() {
 		return source.getAllSamples();
 	}
 
 
 	@Override
 	public SampleTreeNode getSampleTreeRoot() {
 		return source.getSampleTreeRoot();
 	}
 
 
 	@Override
 	public SampleInfo getInfoForSample(SampleInfo info) {
 		return source.getInfoForSample(info);
 	}
 
 	/**
 	 * Returns true if a sample with the given id is current in the cache
 	 * @param sampleID
 	 * @return
 	 */
 	private boolean isInCache(SampleInfo info) {
 		return getCachedSample(info) != null;
 	}
 	
 	private CachedSample getCachedSample(SampleInfo info) {
 		int infoKey = info.getUniqueKey();
 		for(CachedSample sample : cache) {
 			if (sample.info.getUniqueKey() == infoKey ) {
 				return sample;
 			}
 		}
 		return null;
 	}
 	
 	/**
 	 * 
 	 * @param sampleID
 	 */
 	private void bumpToFront(SampleInfo info) {
 		CachedSample cs = getCachedSample(info);
 		if (cs != null) {
 			cache.remove(cs);
 			cache.add(cs);
 		}
 	}
 	
 	private void addToCache(SampleInfo info, VariantCollection vars) {
 		CachedSample cs = new CachedSample(info, vars);
 		cache.add(cs);
 		if (cache.size() > samplesToCache) {
 			CachedSample removed = cache.remove(0);
 			//Logger.getLogger(CachingSampleSource.class).info("Added sample " + sampleID + " to cache, bumped " + removed.sampleID + " from cache since it was full");
 		}
 	}
 	
 	class CachedSample {
 		final SampleInfo info;
 		final VariantCollection vars;
 		
 		public CachedSample(SampleInfo info, VariantCollection vars) {
 			this.info = info;
 			this.vars = vars;
 		}
 	}
 
 	
 
 }
