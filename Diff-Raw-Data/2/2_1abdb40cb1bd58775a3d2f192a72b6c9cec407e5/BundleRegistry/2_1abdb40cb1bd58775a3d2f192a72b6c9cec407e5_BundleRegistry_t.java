 package bundle;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import models.AppProps;
 import models.Bundle;
 import models.Service;
 
 import org.apache.commons.io.FileUtils;
 
 import play.Logger;
 import util.Check;
 import util.Utils;
 
 /**
  * Manager for {@link Bundles}s objects 
  * 
  * @author Paolo Di Tommaso
  *
  */
 public class BundleRegistry {
 	
 	/** There'se just one singleton instance of this object */
 	private static final BundleRegistry INSTANCE;
 	
 	static {
 		/* create an instance and force loading bundles */
 		INSTANCE = new BundleRegistry();
 		INSTANCE.detectChanges();
 	}
 	
 	/** contains errors raised on bundle loading mechanism */
 	public final List<String> errors = new ArrayList<String>();
 	
 	/** Accessor method to the singleton instance */
 	public static BundleRegistry instance() { return INSTANCE; }
 
 	
 	private final Map<String,Bundle> bundles = new HashMap<String, Bundle>();
 	
 	private final Map<File,Bundle> roots = new HashMap<File,Bundle>();
 	
 	private long lastScanTime = 0;
 	
 	/** Marked as 'protected' method to prevent direct instantiation 
 	 * @see #instance()
 	 */
 	protected BundleRegistry() { } 
 	
 	/**
 	 * Accessor method to retrieve {@link Bundle} instance by name 
 	 * @param name the unique bundle name 
 	 * @return a {@link Bundle} instance if exists with the specified name, or <code>null</code> otherwise
 	 */
 	public Bundle get( String name ) { 
 		detectChanges();
 		
 		return bundles.get(name);
 	}
 	
 
     public Bundle load( File file ) { 
     	Bundle result = Bundle.read(file);
     	result.verify();
     	
     	load(result);
     	
     	return result;
     }
 	
 	public synchronized void load( Bundle bundle ) {
 		/* 
 		 * if already exists with the same name 
 		 * unload the old version 
 		 */
 		
 		Bundle old = bundles.get(bundle.name);
 		if( old != null ) { 
 			unload(old);
 		}
 
 		/* 
 		 * register the new 
 		 */
 		bundles.put(bundle.name, bundle);
 		roots.put(bundle.root, bundle);
 		
 		bundle.contentHash = getBundleHash(bundle);
 		
 	}
 	
 	
 	public void unload( Bundle bundle ) { 
 		
 		/* 
 		 * remove from the lists
 		 */
 		bundles.remove(bundle.name);
 		roots.remove(bundle.root);
 		
 		/* clean template cache */
 		BundleTemplateLoader.cleanCacheForBundle(bundle);
 	}
 	
 	/**
 	 * Uninstall and delete bundle from the bundles directory 
 	 * 
 	 * @param bundle
 	 */
 	public void drop( Bundle bundle ) { 
 		Check.notNull(bundle, "Argument 'bundle' cannot be null");
 		try {
 			File root = bundle.root;
 			unload(bundle);
 			FileUtils.deleteDirectory(root);
 			/* double check */
 			if( root.exists() ) { 
 				throw new BundleException("Unable to remove bundle: '%s'", bundle.root);
 			}
 		} 
 		catch (IOException e) {
 			throw new BundleException(e, "Unable to drop bundle '%s'", bundle.root);
 		}
 	}
 	
 	public List<String> getNames() { 
 		detectChanges();
 		
 		return new ArrayList<String>(bundles.keySet());
 	}
 	
 	public List<Bundle> getBundles() { 
 		detectChanges();
 		
 		return new ArrayList<Bundle>(bundles.values());
 	}
 
 	public void detectChanges() {
 		
 		if( System.currentTimeMillis() - lastScanTime < 5000 ) { 
 			return;
 		}
 
 		errors.clear();
 
 		/* create a set with all bundles root currently */
 		File[] files = AppProps.BUNDLES_FOLDER.listFiles();
 		Set<File> foundBundlesOnFileSystem = new HashSet( (files!=null) ? files.length : 0);
 		if( files != null ) { 
 			for( File file : files ) { 
 				if( isBundlePath(file)) { 
 					foundBundlesOnFileSystem.add(file);
 				}
 			}			
 		}
 		
 		Set<File> installed = new HashSet<File>( roots.keySet() );
 		
 		/* 
 		 * remove from the list of path the set of installed bundles 
 		 * so we get the set of new path 
 		 */
 		Set<File> setOfNewBudles = BundleHelper.getNewBundlesPath(installed,foundBundlesOnFileSystem);
 		
 		for( File path : setOfNewBudles ) { 
 
 			/*
 			 * try to install this bundle
 			 */
 			Bundle bundle=null;
 			try { 
 				Logger.info("[bundle] Discovering '%s'", path);
 				bundle = Bundle.read(path);
 				bundle.verify();
 				
 				/* check if already exists with the same name */
 				Bundle other = bundles.get( bundle.name );
 				if( other != null ) { 
 					if( bundle.version.compareTo( other.version ) > 0 ) { 
 						Logger.info("[bundle] Unloading '%s' ", other.root);
 						INSTANCE.unload(other);
 					}
 					else { 
 						Logger.warn("[bundle] Skipping installation of '%s' because a newer version it is already installed", path);
 						continue;
 					}
 				}
 				
 				Logger.info("[bundle] Loading '%s'", path);
 				INSTANCE.load(bundle);
 			}
 			catch( Exception e ) { 
 				Logger.error(e, "[bundle] Error loading  '%s'", path);
 				// append this exception on the errors list
 				errors.add(String.format("Error loading  '%s'", path));
 			}
 		}
 		
 		
 		
 		/*
 		 * get the list of removed bundle path from the current installed 
 		 */
 		Set<File> setOfDroppedBundle = BundleHelper.getDroppedBundlesPath(installed, foundBundlesOnFileSystem);
 		
 		for( File path : setOfDroppedBundle ) { 
 			Bundle bundle = roots.get(path);
 			if( bundle == null ) { 
 				Logger.warn("[bundle] Missing '%s'", path);
 				continue;
 			}
 			
 			Logger.info("[bundle] Unloading '%s'", bundle.root );
 			INSTANCE.unload(bundle);
 		}
 		
 
 		/*
 		 * check if for the existing something has changed .. if so reload it
 		 */
 		Set<File> setOfMatchingBundle = BundleHelper.getExistingBundlesPath(installed, foundBundlesOnFileSystem);
 		
 		
 		for( File path : setOfMatchingBundle ) { 
 			/* check if this bundle has already been installed */
 			Bundle bundle = roots.get(path);
 			
 			if( bundle.contentHash == getBundleHash(bundle) ) { 
 				/* the bundle in installed and BUT content is NOT changed, just skip it */
 				continue;
 			}
 		
 			/*
 			 * try to install this bundle
 			 */
 			Logger.info("[bundle] Reloading '%s'", path);
 
 			INSTANCE.unload(bundle);
 
 			try { 
 				bundle = Bundle.read(path);
 				bundle.verify();
 				INSTANCE.load(bundle);
 			}
 			catch( Exception e ) { 
 				Logger.error(e, "[bundle] Error loading '%s'", path);
 				// append this exception on the errors list
 				errors.add(String.format("Error loading  '%s'", path));
 			}
 			
 		}
 
 		// set the current time  before exit 
 		lastScanTime = System.currentTimeMillis();
 	}
 	
 
 
 	/**
 	 * Defines if the specified path contains a bundle or not 
 	 * 
 	 * @param file the path to check 
 	 * @return <code>true</code> if the path contains a bundle, <code>false</code> otherwise 
 	 */
 	public static boolean isBundlePath( File file ) { 
 		if( !file.isDirectory() ) { 
 			return false;
 		}
 		
 		return new File(file,"conf/bundle.xml").exists(); 
 	}
 	
 	/**
 	 * Calculate the 'deep' hash code for all bundles files getting values from file 
 	 * 'last modified time' and file length
 	 *   
 	 * @return the hash code for bundle content 
 	 */
 	public int getBundleHash( Bundle bundle ) {
 		
 		List<String> hotfiles = java.util.Arrays.asList("bundle.xml", "bundle.properties","bundle.environment","bundle.env"); 
 		
 		int hash = Utils.hash();
 		File[] files = new File(bundle.root, "conf").listFiles();
 		
 		for( File file : files ) { 
 			
 			hash = Utils.hash(hash, file.getName());
 			
 			/* for hot files check also file size and last update date */
 			if( hotfiles.contains( file.getName() ) ) { 
 				hash = Utils.hash(hash, file.lastModified());
 				hash = Utils.hash(hash, file.length());
 			}
 		}
 
 		
 		
 		return hash;
 	} 	
 	
 	
 	/**
 	 * @return map containing bundle <name, title> pairs
 	 */
 	public Map<String,String> getTitlesMap() { 
 		Map<String,String> result = new HashMap<String, String>();
 		
 		for( String name : getNames() ) { 
 			result.put(name, get(name).title );
 		}
 		
 		return result;
 	}
 	
 	/**
 	 * @param bundle the name of an existing {@link Bundle} instance
 	 * @return the title string for the specified <code>bundle</code> name or null if not exists 
 	 */
 	public String getBundleTitle( String bundle ) { 
 		Bundle oBundle = get(bundle);
 		return oBundle != null ? oBundle.title : null;
 	}
 
 	/**
 	 * 
 	 * @param bundle the name of an existing {@link Bundle} instance
 	 * @param service the name of an existing {@link Service} instance for that bundle 
 	 * @return the title string for the specified <code>bundle,serice</code> pair or null if not exists 
 	 */
 	public String getServiceTitle( String bundle, String service ) { 
 		Bundle oBundle = get(bundle);
		Service oService = oBundle != null && service != null ? oBundle.getService(service) : null;
 		return oService != null ? oService.title : null;
 	}
 	
 	/**
 	 * Quick access to bundle/service name using a composed string notation 
 	 * 
 	 * @param fullyQualifiedName it could be the name of a {@link Bundle} or a service name prefixed 
 	 * by the bundle name, using the format <code>bundle-name:service-name</code>
 	 * @return
 	 */
 	public String getTitle( String fullyQualifiedName ) { 
 		String result;
 		
 		String[] parts = fullyQualifiedName.split("\\:");
 		if( parts == null || parts.length==0 ) { 
 			result = null;
 		}
 		else if( parts.length == 1 ) { 
 			result = getBundleTitle(parts[0]);
 		}
 		else { 
 			result = getServiceTitle(parts[0], parts[1]);
 		}
 		
 		return Utils.isEmpty(result) ? fullyQualifiedName : result;
 	}
 
 }
