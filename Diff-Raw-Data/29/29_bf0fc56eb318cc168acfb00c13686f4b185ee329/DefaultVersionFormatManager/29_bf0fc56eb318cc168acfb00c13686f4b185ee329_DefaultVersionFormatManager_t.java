 /**
  * Copyright (c) 2006-2009, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  */
 
 package org.eclipse.b3.versions;
 
 import java.util.Collections;
 import java.util.List;
 
 import org.eclipse.equinox.p2.metadata.Version;
 import org.eclipse.equinox.p2.metadata.VersionRange;
 import org.eclipse.xtext.util.Strings;
 
 import com.google.common.collect.HashBiMap;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Maps;
 import com.google.inject.Singleton;
 
 /**
  * A static implementation of some well known version formats.
  * 
  */
 @Singleton
 public class DefaultVersionFormatManager implements IVersionFormatManager {
 	private static class ProposalImpl implements IVersionFormatManager.VersionProposal {
 
 		private String format;
 
 		private String sample;
 
 		private ProposalImpl(String format, String sample) {
 			this.format = format;
 			this.sample = sample;
 		}
 
 		// @Override
 		public String getFormatDescription() {
 			return "sample in format '" + format + "' - " + sample;
 		}
 
 		// @Override
 		public String getFormatName() {
 			return format;
 		}
 
 		// @Override
 		public String getFormatSample() {
 			return '"' + sample + '"';
 		}
 
 	}
 
	// /**
	// * The OSGi version format.
	// */
	// public static final String VERSION_FORMAT__OSGI = "format(n[.n=0;[.n=0;[.S=[A-Za-z0-9_-];='';]]]):";

 	/**
 	 * Used by "maven" and many others, similar to OSGi, but with 1.0.0 < 1.0.0-beta
 	 */
 	public static final String VERSION_FORMAT__TRIPLET = "format(n[.n=0;[.n=0;]][d?S=M;]):";
 
 	/**
 	 * Separate format is required for snapshots as the trailing part after major.minor.micro is
 	 * numeric followed by string and ordering is different.
 	 */
 	public static final String VERSION_FORMAT__TRIPLET_SNAPSHOT = "format(n[.n=0;[.n=0;[-n=M;.S=m;]]]):";
 
 	/**
 	 * RPM has a auto (numerical, string) format scheme.
 	 */
 	public static final String VERSION_FORMAT__RPM = "format(<[n:]a(d?a)*>[-n[dS=!;]]):";
 
 	/**
 	 * Not known if this is the correct format for GEMs, documentation says three required numerical fields
 	 * separated by period. It is allowed to write less digits in version comparisons, but there are
 	 * special semantics associated with using 1,2 or 3 digits, but this does not affect b3 as osgi version
 	 * range semantics are used. Seems natural to allow missing position to mean "0".
 	 */
 	public static final String VERSION_FORMAT__GEM = "format(n[.n=0;[.n=0;]]):";
 
 	/**
 	 * Mozilla format has complex vectors.
 	 */
 	public static final String VERSION_FORMAT__MOZILLA = "format((<n=0;?s=m;?n=0;?s=m;?>(.<n=0;?s=m;?n=0;?s=m;?>)*)=p<0.m.0.m>;):";
 
 	/**
 	 * When nothing else makes sense, a String format captures everything.
 	 */
 	public static final String VERSION_FORMAT__STRING = "format(S):";
 
 	/**
 	 * An auto format for a sequence of numerical/alpha separated by some separate char.
 	 */
 	public static final String VERSION_FORMAT__AUTO = "format(a(d?a)*):";
 
 	HashBiMap<String, String> formats;
 
 	private List<VersionProposal> versionProposals;
 
 	private List<VersionProposal> versionRangeProposals;
 
 	DefaultVersionFormatManager() {
 		formats = Maps.newHashBiMap();
 		formats.put("string:", VERSION_FORMAT__STRING);
 		formats.put("rpm:", VERSION_FORMAT__RPM);
 		formats.put("triplet:", VERSION_FORMAT__TRIPLET);
 		formats.put("tripletSnapshot:", VERSION_FORMAT__TRIPLET_SNAPSHOT);
 		formats.put("gem:", VERSION_FORMAT__GEM);
 		formats.put("auto:", VERSION_FORMAT__AUTO);
		// formats.put("", VERSION_FORMAT__OSGI);
 
 		versionProposals = Lists.newArrayList();
 		versionProposals.add(new ProposalImpl("osgi", "1.2.3.qualifier"));
 		versionProposals.add(new ProposalImpl("auto", "auto:1.a.2"));
 		versionProposals.add(new ProposalImpl("triplet", "triplet:1.2.3.beta1"));
 		versionProposals.add(new ProposalImpl("tripletSnapshot", "tripletSnapshot:1.2.3-45.20081213:8765"));
 		versionProposals.add(new ProposalImpl("string", "string:jupiter"));
 		versionProposals.add(new ProposalImpl("gem", "gem:1.2.3"));
 		versionProposals.add(new ProposalImpl("rpm", "rpm:33:1.2.3a-23/i386"));
 		versionProposals = Collections.unmodifiableList(versionProposals);
 
 		versionRangeProposals = Lists.newArrayList();
 		versionRangeProposals.add(new ProposalImpl("osgi", "[1.2.3, 2.0.0]"));
 		versionRangeProposals.add(new ProposalImpl("auto", "auto:[1.a.2, 1.b.0]"));
 		versionRangeProposals.add(new ProposalImpl("triplet", "triplet:[1.2.2,1.2.3.beta1]"));
 		versionRangeProposals.add(new ProposalImpl(
 			"tripletSnapshot", "tripletSnapshot:[1.2.3-45.20081213:8765,1.2.3-45.20091231:7654]"));
 		versionRangeProposals.add(new ProposalImpl("string", "string:[jupiter,pluto]"));
 		versionRangeProposals.add(new ProposalImpl("gem", "gem:[1.2.3, 2.0.0]"));
 		versionRangeProposals.add(new ProposalImpl("rpm", "rpm:[33:1.2.3a-23/i386,33:2.0.0]"));
 		versionRangeProposals = Collections.unmodifiableList(versionRangeProposals);
 
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.b3.versions.IVersionFormatManager#getVersionProposals()
 	 */
 	// @Override
 	public List<VersionProposal> getVersionProposals() {
 		return Collections.unmodifiableList(versionProposals);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.eclipse.b3.versions.IVersionFormatManager#getVersionRangeProposals()
 	 */
 	// @Override
 	public List<VersionProposal> getVersionRangeProposals() {
 		return Collections.unmodifiableList(versionRangeProposals);
 	}
 
 	public String toString(Version v) {
		if(v.isOSGiCompatible())
			return v.toString();
		String fmt = v.getFormat() + ":";
		StringBuffer result = new StringBuffer();
		if(formats.inverse().containsKey(fmt)) {
			result.append(formats.inverse().get(fmt));
			result.append(v.getOriginal());
			return result.toString();
 		}
		// unknown format
		return v.toString();
 	}
 
 	public String toString(VersionRange r) {
		if(r.isOSGiCompatible())
			return r.toString();
 		// VersionRange's default "toString" does not produce the same as the input string
 		// format(s):[a,b] => raw:['a','b']/format(s):a,b
 		StringBuffer result = new StringBuffer();
 
 		String fmt = r.getFormat().toString() + ":";
 		if(formats.inverse().containsKey(fmt))
 			fmt = formats.inverse().get(fmt);
 		result.append(fmt);
 		result.append(r.getIncludeMinimum()
 				? '['
 				: '(');
 		result.append(r.getMinimum().getOriginal());
 		result.append(',');
 		result.append(r.getMaximum().getOriginal());
 		result.append(r.getIncludeMaximum()
 				? ']'
 				: ')');
 
 		return result.toString();
 	}
 
 	public VersionRange toVersionRangeValue(String s) {
 		if(Strings.isEmpty(s))
 			throw new IllegalArgumentException("A version range string can not be empty");
 
 		for(String prefix : formats.keySet()) {
 			if(s.startsWith(prefix)) {
 				s = s.replace(prefix, formats.get(prefix));
 				break;
 			}
 		}
 		return new VersionRange(s);
 	}
 
 	/**
 	 * Simplistic replacement of known format prefix. It will fail if the format name prefix e.g. "rpm:" appears in the
 	 * version string as in "rpm:1.0.0-rpm:3"
 	 * 
 	 * @param s
 	 * @return
 	 */
 	public Version toVersionValue(String s) {
 		if(Strings.isEmpty(s))
 			throw new IllegalArgumentException("A version string can not be empty");
 
 		for(String prefix : formats.keySet()) {
 			if(s.startsWith(prefix)) {
 				s = s.replace(prefix, formats.get(prefix));
 				break;
 			}
 		}
 		return Version.create(s);
 	}
 }
