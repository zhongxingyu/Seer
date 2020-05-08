 package org.vpac.grisu.model;
 
 import java.util.LinkedList;
 import java.util.List;
 
 import javax.persistence.Column;
 import javax.persistence.Entity;
 import javax.persistence.GeneratedValue;
 import javax.persistence.Id;
 import javax.persistence.Transient;
 import javax.xml.bind.annotation.XmlAccessType;
 import javax.xml.bind.annotation.XmlAccessorType;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 
 import org.apache.log4j.Logger;
 import org.vpac.grisu.model.dto.DtoProperty;
 
 /**
  * The concept of MountPoints is pretty important within grisu. A MountPoint is
  * basically a mapping of a "logical name" to an url. Much like mountpoints in a
  * unix filesystem. A logical name should contain the site where the filesystem
  * sits and the VO that has got access to this filesystem so that the user can
  * recognise which one is meant when looking at the name in a file browser.
  * 
  * @author Markus Binsteiner
  * 
  */
 @Entity
 @XmlRootElement(name = "mountpoint")
 @XmlAccessorType(XmlAccessType.NONE)
 public class MountPoint implements Comparable<MountPoint> {
 
 	static final Logger myLogger = Logger.getLogger(MountPoint.class.getName());
 
 	public static final String ALIAS_KEY = "label";
 	public static final String PATH_KEY = "path";
 	public static final String USER_SUBDIR_KEY = "user_subdir";
 
 	public static final String HIDDEN_KEY = "hidden";
 
 	private Long mountPointId = null;
 
 	/**
 	 * The dn of the user.
 	 */
 	private String dn = null;
 	/**
 	 * The fqan that is used to create a voms credential to access this
 	 * mountpoint.
 	 */
 	private String fqan = null;
 	/**
 	 * The alias of this mountpoint.
 	 */
 	private String alias = null;
 	/**
 	 * The url of the root of this mountpoint.
 	 */
 	private String rootUrl = null;
 
 	/**
 	 * The name of the site this mountpoint belongs to.
 	 */
 	private String site = null;
 
 	private List<DtoProperty> properties = new LinkedList<DtoProperty>();
 
 	private boolean automaticallyMounted = false;
 	private boolean disabled = false;
 
 	// for hibernate
 	public MountPoint() {
 	}
 
 	/**
 	 * This is used primarily to create a "dummy" mountpoint to be able to use
 	 * the {@link User#unmountFileSystem(String)} method.
 	 * 
 	 * @param dn
 	 *            the dn of the user
 	 * @param mountpoint
 	 *            the name of the mountpoint
 	 */
 	public MountPoint(final String dn, final String mountpoint) {
 		this.dn = dn;
 		this.alias = mountpoint;
 	}
 
 	/**
 	 * Creates a MountPoint. Sets automount property to false.
 	 * 
 	 * @param dn
 	 *            the dn of the user
 	 * @param fqan
 	 *            the fqan that is used to access this filesystem
 	 * @param url
 	 *            the root url
 	 * @param mountpoint
 	 *            the name of the mountpoint
 	 */
 	public MountPoint(final String dn, final String fqan, final String url,
 			final String mountpoint, final String site) {
 		this.dn = dn;
 		this.fqan = fqan;
 		this.rootUrl = url;
 		this.alias = mountpoint;
 		this.site = site;
 	}
 
 	/**
 	 * Creates a Mountpoint.
 	 * 
 	 * @param dn
 	 *            the dn of the user
 	 * @param fqan
 	 *            the fqan that is used to access this filesystem
 	 * @param url
 	 *            the root url
 	 * @param mountpoint
 	 *            the name of the mountpoint
 	 * @param automaticallyMounted
 	 *            whether this mountpoint was mounted automatically using mds
 	 *            information or manually by the user
 	 */
 	public MountPoint(final String dn, final String fqan, final String url,
 			final String mountpoint, final String site,
 			final boolean automaticallyMounted) {
 		this(dn, fqan, url, mountpoint, site);
 		this.automaticallyMounted = automaticallyMounted;
 	}
 
 	public void addProperty(String key, String value) {
 		this.properties.add(new DtoProperty(key, value));
 	}
 
 	public int compareTo(final MountPoint mp) {
		return getRootUrl().compareTo(mp.getRootUrl());
 	}
 
 	@Override
 	public boolean equals(final Object otherMountPoint) {
 
 		if (otherMountPoint instanceof MountPoint) {
 			final MountPoint other = (MountPoint) otherMountPoint;
 
 			return other.getAlias().equals(this.getAlias());
 
 			// if ( other.getDn().equals(this.getDn()) &&
 			// other.getRootUrl().equals(this.getRootUrl()) ) {
 			//
 			// if ( other.getFqan() == null ) {
 			// if ( this.getFqan() == null ) {
 			// return true;
 			// } else {
 			// return false;
 			// }
 			// } else {
 			// if ( this.getFqan() == null ) {
 			// return false;
 			// } else {
 			// return other.getFqan().equals(this.getFqan());
 			// }
 			// }
 			// } else {
 			// return false;
 			// }
 		} else {
 			return false;
 		}
 
 	}
 
 	@Column(nullable = false)
 	@XmlAttribute(name = "alias")
 	public String getAlias() {
 		return alias;
 	}
 
 	@Column(nullable = false)
 	@XmlElement(name = "dn")
 	public String getDn() {
 		return dn;
 	}
 
 	/**
 	 * The fqan that is used to create a voms proxy to access this mountpoint.
 	 * 
 	 * @return the fqan
 	 */
 	@XmlElement(name = "fqan")
 	public String getFqan() {
 		return fqan;
 	}
 
 	@Id
 	@GeneratedValue
 	public Long getMountPointId() {
 		return mountPointId;
 	}
 
 	@Transient
 	@XmlElement(name = "property")
 	public List<DtoProperty> getProperties() {
 		return properties;
 	}
 
 	@Transient
 	public String getProperty(String key) {
 		for (final DtoProperty prop : this.properties) {
 			if (prop.getKey().equals(key)) {
 				return prop.getValue();
 			}
 		}
 		return null;
 	}
 
 	/**
 	 * Returns the path of the specified file relative to the root of this
 	 * mountpoint.
 	 * 
 	 * @param url
 	 *            the file
 	 * @return the relative path or null if the file is not within the
 	 *         filesystem of the mountpoint
 	 */
 	public String getRelativePathToRoot(final String url) {
 
 		if (url.startsWith("/")) {
 			if (!url.startsWith(getAlias())) {
 				return null;
 			} else {
 				final String path = url.substring(getAlias().length());
 				if (path.startsWith("/")) {
 					return path.substring(1);
 				} else {
 					return path;
 				}
 			}
 		} else {
 			if (!url.startsWith(getRootUrl())) {
 				return null;
 			} else {
 				final String path = url.substring(getRootUrl().length());
 				if (path.startsWith("/")) {
 					return path.substring(1);
 				} else {
 					return path;
 				}
 			}
 		}
 
 	}
 
 	@Column(nullable = false)
 	@XmlAttribute(name = "url")
 	public String getRootUrl() {
 		return rootUrl;
 	}
 
 	@Column(nullable = false)
 	@XmlElement(name = "site")
 	public String getSite() {
 		return site;
 	}
 
 	@Override
 	public int hashCode() {
 		// return dn.hashCode() + mountpoint.hashCode();
 		return alias.hashCode();
 	}
 
 	@Column(nullable = false)
 	@XmlElement(name = "automounted")
 	public boolean isAutomaticallyMounted() {
 		return automaticallyMounted;
 	}
 
 	@Column(nullable = false)
 	@XmlElement(name = "disabled")
 	public boolean isDisabled() {
 		return disabled;
 	}
 
 	/**
 	 * Checks whether the "userspace" url (/ngdata.vpac/file.txt) contains the
 	 * file.
 	 * 
 	 * @param file
 	 *            the file
 	 * @return true - if it contains it; false - if not.
 	 */
 	public boolean isResponsibleForAbsoluteFile(final String file) {
 
 		if (file.startsWith(getRootUrl())) {
 			return true;
 		} else {
 			if (file.startsWith(getRootUrl().replace(":2811", ""))) {
 				// warning
 				myLogger.warn("Found mountpoint. Didn't compare port numbers though...");
 				return true;
 			}
 		}
 		return false;
 	}
 
 	/**
 	 * Checks whether the "userspace" url (/ngdata.vpac/file.txt) contains the
 	 * file.
 	 * 
 	 * @param file
 	 *            the file
 	 * @return true - if it contains it; false - if not.
 	 */
 	public boolean isResponsibleForUserSpaceFile(final String file) {
 
 		if (file.startsWith("gsiftp")) {
 			if (file.startsWith(getRootUrl())) {
 				return true;
 			} else {
 				return false;
 			}
 		}
 
 		if (file.startsWith(getAlias())) {
 			return true;
 		} else {
 			return false;
 		}
 	}
 
 	// public boolean equals(Object otherMountPoint) {
 	// if ( ! (otherMountPoint instanceof MountPoint) )
 	// return false;
 	// MountPoint other = (MountPoint)otherMountPoint;
 	// if ( other.dn.equals(this.dn) && other.mountpoint.equals(this.mountpoint)
 	// )
 	// return true;
 	// else return false;
 	// }
 
 	/**
 	 * Translates an absolute file url to a "mounted" file url.
 	 * 
 	 * @param file
 	 *            the absolute file
 	 *            (gsiftp://ngdata.vpac.org/home/sano4/markus/test.txt)
 	 * @return /ngdata.vpac.org/test.txt
 	 */
 	public String replaceAbsoluteRootUrlWithMountPoint(final String file) {
 
 		if (file.startsWith(getRootUrl())) {
 			return file.replaceFirst(getRootUrl(), getAlias());
 
 		} else {
 			return null;
 		}
 	}
 
 	public void setAlias(final String mountpoint) {
 		this.alias = mountpoint;
 	}
 
 	public void setAutomaticallyMounted(final boolean am) {
 		this.automaticallyMounted = am;
 	}
 
 	public void setDisabled(final boolean disabled) {
 		this.disabled = disabled;
 	}
 
 	public void setDn(final String dn) {
 		this.dn = dn;
 	}
 
 	public void setFqan(final String fqan) {
 		this.fqan = fqan;
 	}
 
 	public void setMountPointId(final Long id) {
 		this.mountPointId = id;
 	}
 
 	// public int compareTo(Object o) {
 	// // return ((MountPoint)o).getMountpoint().compareTo(getMountpoint());
 	// return getRootUrl().compareTo(((MountPoint)o).getRootUrl());
 	// }
 
 	public void setProperties(List<DtoProperty> properties) {
 		this.properties = properties;
 	}
 
 	public void setRootUrl(final String rootUrl) {
 		this.rootUrl = rootUrl;
 	}
 
 	public void setSite(final String site) {
 		this.site = site;
 	}
 
 	public void setUrl(final String url) {
 		this.rootUrl = url;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see java.lang.Object#toString()
 	 */
 	@Override
 	public String toString() {
 		return getAlias();
 	}
 
 }
