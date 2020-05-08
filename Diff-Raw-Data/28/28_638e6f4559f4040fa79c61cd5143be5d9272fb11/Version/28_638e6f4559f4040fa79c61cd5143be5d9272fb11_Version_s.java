 package com.umansoor.util.Version;
 
/* 
  * A class to represent version information. This class cannot be inherited.
 * This class is thread-safe as it is immutable and can be freely shared 
 * between threads without any external synchronization. 
  * 
  * A version object represented by this class is of the following format:
  * X.Y.Z.b
  * where,
  * X = Major component of the version.
  * Y = Minor component of the version.
  * Z = Revision component of the version.
  * b = Build component of the version.
  * 
 * This is inspired from the Version class in .NET Framework 4.0.
  * 
 * Umer Mansoor <umermk3@gmail.com>
  */
 public final class Version implements Comparable<Version> {
 	
	// Represents the value of the major component of the version object.
 	private final Integer major;
 	
	// Represents the value of the minor component of the version object.
 	private final Integer minor;
 	
	// Represents the value of the revision component of the version object.
 	private final Integer revision;
 	
	// Represents the value of the build component of the version object.
 	private final Integer build;
 	
	/*
 	 * Private constructor, cannot be called directly.
 	 */
 	private Version (Integer major, Integer minor, Integer revision, Integer build) {
 		if (major == null || minor == null || revision == null || build == null)
 			throw new IllegalArgumentException("version number cannot contain null values");
 		this.major = major;
 		this.minor = minor;
 		this.revision = revision;
 		this.build = build;
 	}
 	
 	/**
 	 * Returns the major component for the current version object.
 	 * @return
 	 */
 	public final Integer getMajor() {
 		return major;
 	}
 	
 	/**
 	 * Returns the minor component for the current version object.
 	 * @return
 	 */
 	public final Integer getMinor() {
 		return minor;
 	}
 	
 	/**
 	 * Returns the revision component for the current version object.
 	 * @return
 	 */
 	public final Integer getRevision() {
 		return revision;
 	}
 	
 	/**
 	 * Returns the build component for the current version object.
 	 * @return
 	 */
 	public final Integer getBuild() {
 		return build;
 	}
 	
 	/**
 	 * Initializes a new object of this class with the specified `major` and
 	 * `minor` components and returns the instance. An object of this class 
 	 * requires at least `major` and `minor` components.
 	 * @param major
 	 * @param minor
 	 * @return
 	 */
 	public static Version valueOf(Integer major, Integer minor) {
 		return new Version(major, minor, 0, 0);
 	}
 	
 	/**
 	 * Initializes a new object of this class with the specified `major`,
 	 * `minor`, and `build` components and returns the instance. The
 	 * `revision` is set to 0.
 	 * @param major
 	 * @param minor
 	 * @param build
 	 * @return
 	 */
 	public static Version valueOf(Integer major, Integer minor, Integer build) {
 		return new Version(major, minor, 0, build);
 	}
 	
 	/**
 	 *  Initializes a new object of this class with the specified `major`,
 	 * `minor`, `build` and `revision` and returns the instance.
 	 * @param major
 	 * @param minor
 	 * @param revision
 	 * @param build
 	 * @return
 	 */
 	public static Version valueOf(Integer major, Integer minor, Integer revision, Integer build) {
 		return new Version(major, minor, revision, build);
 	}
 	
 	/**
 	 * Initializes a new object of this class using the specified String.
 	 * @param value
 	 * @return
 	 * @throws IllegalArgumentException
 	 */
 	public static Version valueOf(String value) throws IllegalArgumentException {
 		if (value == null) {
 			throw new IllegalArgumentException("version string cannot be null");
 		}
 		
 		// Split the String at '.' character
 		String[] info = value.split("\\.");
 		
 		// A valid version object must have at least 2 components (major and 
 		// minor) i.e, X.Y and at most 4 components i.e. X.Y.Z.b (revision and 
 		// build).
 		if (info.length < 2 || info.length > 4) {
 			throw new IllegalArgumentException("invalid version string");
 		}
 		
 		Integer[] versionInfo = new Integer[info.length];
 		for (int i=0; i<info.length; i++) {
 			try {
 				versionInfo[i] = Integer.valueOf(info[i]);
 			} catch (NumberFormatException ne) {
 				throw new IllegalArgumentException("invalid version string.", ne);
 			}
 		}
 		
 		if (info.length == 2) {
 			return new Version(versionInfo[0], versionInfo[1], 0, 0);
 		} else if (info.length == 3) {
 			return new Version(versionInfo[0], versionInfo[1], 0, versionInfo[2]);
 		} else {
 			return new Version(versionInfo[0], versionInfo[1], versionInfo[2], versionInfo[3]);
 		}
 		      
 	}
 	
 	/**
 	 * Returns a String representation of this object.
 	 */
 	@Override
 	public String toString() {
 		return major + "." + minor + "." + revision + "." + build;
 	}
 	
 	
 	/**
 	 * Used for testing if the current Version is equal to another Version 
 	 * object passed as an argument. 
 	 */
 	@Override
 	public boolean equals(Object o){
 		if (o == null || o instanceof Version == false) {
 			return false;
 		} 
 		
 		if (o == this ) {
 			return true;
 		}
 		
 		Version v = (Version)o;
 		if (major.equals(v.getMajor()) && minor.equals(v.getMinor()) && revision.equals(v.getRevision()) && build.equals(v.getBuild())) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 	
 	/**
 	 * Used for comparing the current versiob object with the one passed in the
 	 * argument. Inherited and uses the protocol of Comparable interface: 
 	 * @see <a href="http://docs.oracle.com/javase/7/docs/api/java/lang/Comparable.html">http://docs.oracle.com/javase/7/docs/api/java/lang/Comparable.html</a>
 	 * 
 	 */
 	@Override
 	public int compareTo(Version v) {
 		if (major.compareTo(v.getMajor()) > 0) {
 			return 1;
 		} else if (minor.compareTo(v.getMinor()) > 0) {
 			return 1;
 		} else if (revision.compareTo(v.getRevision()) > 0) {
 			return 1;
 		} else if (build.compareTo(v.getBuild()) > 0) {
 			return 1;
 		} else if (major.compareTo(v.getMajor()) == 0 && minor.compareTo(v.getMinor()) == 0 
 						&& revision.compareTo(v.getRevision()) == 0 && build.compareTo(v.getBuild()) == 0) {
 			return 0;
 		} else {
 			return -1;
 		}
 	}
 	
 	/**
 	 * When overiding equals, always override hashCode as well.
 	 * @see <a href="https://github.com/starscriber/coding-standards/wiki/When-Overiding-equals,-Always-Override-hashCode-As-Well">https://github.com/starscriber/coding-standards/wiki/When-Overiding-equals,-Always-Override-hashCode-As-Well</a>
 	 */
 	@Override
 	public int hashCode() {
 		int hash = 0;
 		hash += 17 + major.hashCode();
 		hash += minor.hashCode();
 		hash += 13 + (revision == null ? 0 : revision.hashCode());
 		hash += (build == null ? 0 : build.hashCode());
 		
 		return 31 + hash;
 	}
 }
