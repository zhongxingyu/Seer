 package org.middlecraft.server;
 
 import java.util.*;
 import java.util.logging.*;
 import java.util.concurrent.locks.*;
 
 /**
  * Pluggable preferences class to allow unified access to plugin data.
  *
  * Datajack abstracts preference reading and writing away from the medium
  * used to store the information, the hierarchy that holds that information,
  * and the medium that archives the data.  This allows for Middlecraft as
  * well as plugins to simply store and retrieve configuration data, without
  * needing to write database or flatfile drivers for every module.
  * <br/><br/>
  * This reduces the work needed to make high quality plugins and increases
  * the overall modularity of the system.
  *
  * @author Joshua 'Skrylar' Cearley
  */
 public class DataJack implements IDataJack {
 	// NOTE: I'm not yet sure how this should handle array properties in a scalable way; the trivial way for things like iConomy is to return an array of values, but this doesn't neccesarily scale to databases with umpteen rows of data. -- Skrylar
 	
 	protected Logger log;
 	protected Map<String, IDataJack> roots;
 	protected Lock rootsLock;
 	
 	public DataJack() {
 		roots = new HashMap<String, IDataJack>();
 		rootsLock = new ReentrantLock();
 		log = Logger.getLogger("DataJack");
 		log.setLevel(Level.FINEST);
 	}
 	
 	/**
 	 * Adds a root to the list of global preference roots.
 	 *
 	 * A given root will be added to the end of the chain of known preference
 	 * roots, which means it will be called upon to provide values when
 	 * they are requested using any number of the get* APIs.  The name of
 	 * a root is not passed along to it, they are stripped from the global
 	 * preference path as they are matched.  "economy:coins.name" will be
 	 * stripped to "coins.name" and passed to the root, assuming the root
 	 * was added with the name "economy".
 	 *
 	 * @param _name The _name of the root to be added; this is used to locate
 	 * the root from a global preference identifier.
 	 * @param root An IDataJack that will provide requested information
 	 * to clients requesting it through a global preference identifier.
 	 */
 	public void addRoot(String _name, IDataJack root) {
 		/* Prevent the stupids. */
 		if (root == null) {
 			throw new NullPointerException();
 		}
 		
 		/* ENGAGE! */
 		rootsLock.lock();
 		try {
 			roots.put(_name, root);
 			root.dataJackedIn(this, _name);
 		} finally {
 			rootsLock.unlock();
 		}
 		
 		log.log(Level.FINE, "Added new root >", _name);
 	}
 	
 	/**
 	 * Removes a named root from the list of global preference roots.
 	 *
 	 * A removed root will no longer be called upon to provide information
 	 * through a global preference path.
 	 *
 	 * @param _name The name of a root to be removed.
 	 */
 	public void removeRoot(String _name) {
 		rootsLock.lock();
 		try {
 			IDataJack dj_badmusic = roots.get(_name);
 			roots.remove(dj_badmusic);
 			if (dj_badmusic != null) {
 				dj_badmusic.dataJackedOut(this, _name);
 			}
 		} finally {
 			rootsLock.unlock();
 		}
 		
 		log.log(Level.FINE, "Removed old root >", _name);
 	}
 	
 	protected String getRootName(String _globalPreferencePath) {
 		int sepIdx = _globalPreferencePath.indexOf(':');
 		String rootName = _globalPreferencePath.substring(0, sepIdx);
 		log.log(Level.FINEST, "Resolving \"{0}\" to root #{1}", new Object[] {_globalPreferencePath, rootName});
 		return rootName;
 	}
 	
 	protected String stripRootName(String _globalPreferencePath) {
 		int sepIdx = _globalPreferencePath.indexOf(':');
 		String pathName = _globalPreferencePath.substring(sepIdx+1);
 		log.log(Level.FINEST, "Stripped header from \"{0}\" to path \"{1}\"",
 		new Object[] {_globalPreferencePath, pathName});
 		return pathName;
 	}
 	
 	protected IDataJack getDataJackForPath(String _path) {
 		rootsLock.lock();
 		IDataJack dj_dork = null;
 		try {
 			String key = getRootName(_path);
 			dj_dork = roots.get(_path);
 		} finally {
 			rootsLock.unlock();
 		}
 		return dj_dork;
 	}
 	
 	// === GETTERS ===
 	@Override public String getString(String _name, String _default) {
 		IDataJack dj_copypasta = getDataJackForPath(_name);
 		if (dj_copypasta != null) {
 			return dj_copypasta.getString(stripRootName(_name), _default);
 		} else {
 			return _default;
 		}
 	}
 	
 	@Override public int getInteger(String _name, int _default) {
 		IDataJack dj_copypasta = getDataJackForPath(_name);
 		if (dj_copypasta != null) {
 			return dj_copypasta.getInteger(stripRootName(_name), _default);
 		} else {
 			return _default;
 		}
 	}
 
 	@Override public long getLong(String _name, long _default) {
 		IDataJack dj_copypasta = getDataJackForPath(_name);
 		if (dj_copypasta != null) {
 			return dj_copypasta.getLong(stripRootName(_name), _default);
 		} else {
 			return _default;
 		}
 	}
 	
 	@Override public boolean getBoolean(String _name, boolean _default) {
 		IDataJack dj_copypasta = getDataJackForPath(_name);
 		if (dj_copypasta != null) {
 			return dj_copypasta.getBoolean(stripRootName(_name), _default);
 		} else {
 			return _default;
 		}
 	}
 	
 	// === SETTERS ===
 	@Override public void setString(String _name, String _value) {
 		IDataJack dj_copypasta = getDataJackForPath(_name);
 		if (dj_copypasta != null) {
			dj_copypasta.setString(stripRootName(_name), _value);
 		}
 	}
 	
 	@Override public void setInteger(String _name, int _value) {
 		IDataJack dj_copypasta = getDataJackForPath(_name);
 		if (dj_copypasta != null) {
			dj_copypasta.setInteger(stripRootName(_name), _value);
 		}
 	}
 	
 	@Override public void setLong(String _name, long _value) {
 		IDataJack dj_copypasta = getDataJackForPath(_name);
 		if (dj_copypasta != null) {
			dj_copypasta.setLong(stripRootName(_name), _value);
 		}
 	}
 	
 	@Override public void setBoolean(String _name, boolean _value) {
 		IDataJack dj_copypasta = getDataJackForPath(_name);
 		if (dj_copypasta != null) {
			dj_copypasta.setBoolean(stripRootName(_name), _value);
 		}
 	}
 
 	/* (non-Javadoc)
 	 * @see org.middlecraft.server.IDataJack#dataJackedIn(org.middlecraft.server.Datajack, java.lang.String)
 	 */
 	@Override
 	public void dataJackedIn(DataJack _source, String _name) {
 		// XXX: I can't think of anything we need to do here.
 	}
 
 	/* (non-Javadoc)
 	 * @see org.middlecraft.server.IDataJack#dataJackedOut(org.middlecraft.server.Datajack, java.lang.String)
 	 */
 	@Override
 	public void dataJackedOut(DataJack _source, String _name) {
 		// XXX: I can't think of anything we need to do here.		
 	}
 }
