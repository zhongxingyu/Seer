 package com.parc.ccn.library.profiles;
 
 import java.math.BigInteger;
 import java.sql.Timestamp;
 
 import com.parc.ccn.data.ContentName;
 import com.parc.ccn.data.security.SignedInfo;
 
 /**
  * Versions, when present, occupy the penultimate component of the CCN name, 
  * not counting the digest component. They may be chosen based on time.
  * The first byte of the version component is 0xFD. The remaining bytes are a
  * big-endian binary number. If based on time they are expressed in units of
  * 2**(-12) seconds since the start of Unix time, using the minimum number of
  * bytes. The time portion will thus take 48 bits until quite a few centuries
  * from now (Sun, 20 Aug 4147 07:32:16 GMT). With 12 bits of precision, it allows 
  * for sub-millisecond resolution. The client generating the version stamp 
  * should try to avoid using a stamp earlier than (or the same as) any 
  * version of the file, to the extent that it knows about it. It should 
  * also avoid generating stamps that are unreasonably far in the future.
  */
 public class VersioningProfile implements CCNProfile {
 
 	public static final byte VERSION_MARKER = (byte)0xFD;
 	public static final byte [] FIRST_VERSION_MARKER = new byte []{VERSION_MARKER};
 
 	/**
 	 * Add a version field to a ContentName.
 	 * @return ContentName with any previous version field and sequence number removed and new version field added.
 	 */
 	public static ContentName versionName(ContentName name, long version) {
 		// Need a minimum-bytes big-endian representation of version.
 		ContentName baseName = name;
 		if (isVersioned(name)) {
 			baseName = versionRoot(name);
 		}
 		byte [] vcomp = null;
 		if (0 == version) {
 			vcomp = FIRST_VERSION_MARKER;
 		} else {
 			byte [] varr = BigInteger.valueOf(version).toByteArray();
 			vcomp = new byte[varr.length + 1];
 			vcomp[0] = VERSION_MARKER;
 			System.arraycopy(varr, 0, vcomp, 1, varr.length);
 		}
 		return new ContentName(baseName, vcomp);
 	}
 	
 	/**
 	 * Converts a timestamp into a fixed point representation, with 12 bits in the fractional
 	 * component, and adds this to the ContentName as a version field. The timestamp is rounded
 	 * to the nearest value in the fixed point representation.
 	 * <p>
 	 * This allows versions to be recorded as a timestamp with a 1/4096 second accuracy.
 	 * @see #versionName(ContentName, long)
 	 */
 	public static ContentName versionName(ContentName name, Timestamp version) {
 		return versionName(name, (version.getTime() / 1000 ) * 4096L + (version.getNanos() * 4096L + 500000000L) / 1000000000L);
 	}
 	
 	/**
 	 * Add a version field based on the current time, accurate to 1/4096 second.
 	 * @see #versionName(ContentName, Timestamp)
 	 */
 	public static ContentName versionName(ContentName name) {
 		return versionName(name, SignedInfo.now());
 	}
 	
 	/**
 	 * Checks to see if this name has a validly formatted version field.
 	 */
 	public static boolean isVersioned(ContentName name) {
 		byte [] vm = null;
 		if (SegmentationProfile.isSegment(name)) {
 			vm = name.component(name.count()-2);
 		} else {
 			vm = name.lastComponent(); // no fragment number, unusual
 		}
		return (null != vm) && (0 != vm.length) && (VERSION_MARKER == vm[0]) && ((vm.length == 1) || (vm[1] != 0));
 	}
 
 	/**
 	 * Take a name which may have a segment component and a version component
 	 * and strip them if present.
 	 */
 	public static ContentName versionRoot(ContentName name) {
 		int offset = 0;
 		if (SegmentationProfile.isSegment(name)) {
 			if (isVersioned(name)) {
 				offset = name.count()-2;
 			} else {
 				// is a fragment, but not versioned, remove fragment marker
 				offset = name.count()-1;
 			}
 		} else {
 			// no fragment number, unusual
 			if (isVersioned(name)) {
 				offset = name.count()-1;
 			} else {
 				// already there
 				return name;
 			}
 		}
 		return new ContentName(offset, name.components());
 	}
 
 	/**
 	 * Does this name represent a version of the given parent?
 	 * DKS TODO -- do we need a tighter definition? e.g. is this a data block of
 	 * this version, versus metadata, etc...
 	 * @param version
 	 * @param parent
 	 * @return
 	 */
 	public static boolean isVersionOf(ContentName version, ContentName parent) {
 		if (!isVersioned(version))
 			return false;
 		
 		if (isVersioned(parent))
 			parent = versionRoot(parent);
 		
 		return parent.isPrefixOf(version);
 	}
 	
 	/**
 	 * @param name
 	 * @return the contents of the version field as a long
 	 * @throws VersionMissingException
 	 */
 	public static long getVersionAsLong(ContentName name) throws VersionMissingException {
 		byte [] vm = null;
 		if (SegmentationProfile.isSegment(name)) {
 			vm = name.component(name.count()-2);
 		} else {
 			vm = name.lastComponent(); // no fragment number, unusual
 		}
 		if ((null == vm) || (0 == vm.length) || (VERSION_MARKER != vm[0]))
 			throw new VersionMissingException();
 		
 		byte [] versionData = new byte[vm.length - 1];
 		System.arraycopy(vm, 1, versionData, 0, vm.length - 1);
 		return new BigInteger(versionData).longValue();
 	}
 
 	/**
 	 * Extract the version from this name as a Timestamp.
 	 * @throws VersionMissingException 
 	 */
 	public static Timestamp getVersionAsTimestamp(ContentName name) throws VersionMissingException {
 		long time = getVersionAsLong(name);
 		Timestamp ts = new Timestamp((time / 4096L) * 1000L);
 		ts.setNanos((int)(((time % 4096L) * 1000000000L) / 4096L));
 		return ts;
 	}
 
 	/**
 	 * Control whether versions start at 0 or 1.
 	 * @return
 	 */
 	public static final int baseVersion() { return 0; }
 }
