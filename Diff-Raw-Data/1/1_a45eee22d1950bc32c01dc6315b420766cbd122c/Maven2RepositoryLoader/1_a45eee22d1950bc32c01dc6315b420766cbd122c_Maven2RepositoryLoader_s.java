 /*******************************************************************************
  * Copyright (c) 2006-2007, Cloudsmith Inc.
  * The code, documentation and other materials contained herein have been
  * licensed under the Eclipse Public License - v 1.0 by the copyright holder
  * listed above, as the Initial Contributor under such license. The text of
  * such license is available at www.eclipse.org.
  ******************************************************************************/
 package org.eclipse.b3.p2.maven.loader;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.UnsupportedEncodingException;
 import java.math.BigInteger;
 import java.net.ConnectException;
 import java.net.MalformedURLException;
 import java.net.URI;
 import java.net.URL;
 import java.net.URLConnection;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 import java.util.Stack;
 import java.util.StringTokenizer;
 import java.util.regex.Pattern;
 
 import org.apache.commons.httpclient.Header;
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpMethod;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.HeadMethod;
 import org.eclipse.b3.p2.InstallableUnit;
import org.eclipse.b3.p2.MetadataRepository;
 import org.eclipse.b3.p2.P2Factory;
 import org.eclipse.b3.p2.impl.ArtifactKeyImpl;
 import org.eclipse.b3.p2.impl.CopyrightImpl;
 import org.eclipse.b3.p2.impl.InstallableUnitImpl;
 import org.eclipse.b3.p2.impl.LicenseImpl;
 import org.eclipse.b3.p2.impl.MetadataRepositoryImpl;
 import org.eclipse.b3.p2.impl.ProvidedCapabilityImpl;
 import org.eclipse.b3.p2.impl.TouchpointTypeImpl;
 import org.eclipse.b3.p2.loader.IRepositoryLoader;
 import org.eclipse.b3.p2.maven.MavenActivator;
 import org.eclipse.b3.p2.maven.MavenMetadata;
 import org.eclipse.b3.p2.maven.POM;
 import org.eclipse.b3.p2.maven.indexer.IMaven2Indexer;
 import org.eclipse.b3.p2.maven.indexer.IndexNotFoundException;
 import org.eclipse.b3.p2.maven.indexer.IndexerUtils;
 import org.eclipse.b3.p2.maven.pom.Dependency;
 import org.eclipse.b3.p2.maven.pom.License;
 import org.eclipse.b3.p2.maven.pom.Model;
 import org.eclipse.b3.p2.maven.util.UriIterator;
 import org.eclipse.b3.p2.maven.util.VersionUtil;
 import org.eclipse.b3.p2.util.P2Bridge;
 import org.eclipse.b3.p2.util.P2Utils;
 import org.eclipse.b3.p2.util.RepositoryLoaderUtils;
 import org.eclipse.b3.util.ExceptionUtils;
 import org.eclipse.b3.util.LogUtils;
 import org.eclipse.b3.util.StringUtils;
 import org.eclipse.core.runtime.CoreException;
 import org.eclipse.core.runtime.IConfigurationElement;
 import org.eclipse.core.runtime.IPath;
 import org.eclipse.core.runtime.IProgressMonitor;
 import org.eclipse.core.runtime.NullProgressMonitor;
 import org.eclipse.core.runtime.OperationCanceledException;
 import org.eclipse.core.runtime.Path;
 import org.eclipse.core.runtime.SubMonitor;
 import org.eclipse.core.runtime.SubProgressMonitor;
 import org.eclipse.equinox.internal.p2.artifact.repository.simple.SimpleArtifactDescriptor;
 import org.eclipse.equinox.internal.p2.artifact.repository.simple.SimpleArtifactRepository;
 import org.eclipse.equinox.p2.core.IProvisioningAgent;
 import org.eclipse.equinox.p2.metadata.IArtifactKey;
 import org.eclipse.equinox.p2.metadata.IInstallableUnit;
 import org.eclipse.equinox.p2.metadata.IRequirement;
 import org.eclipse.equinox.p2.metadata.ITouchpointType;
 import org.eclipse.equinox.p2.metadata.MetadataFactory;
 import org.eclipse.equinox.p2.metadata.MetadataFactory.InstallableUnitDescription;
 import org.eclipse.equinox.p2.metadata.Version;
 import org.eclipse.equinox.p2.metadata.VersionRange;
 import org.eclipse.equinox.p2.query.IQuery;
 import org.eclipse.equinox.p2.query.IQueryResult;
 import org.eclipse.equinox.p2.query.QueryUtil;
 import org.eclipse.equinox.p2.repository.IRepository;
 import org.eclipse.equinox.p2.repository.artifact.IArtifactRepository;
 import org.eclipse.equinox.p2.repository.metadata.IMetadataRepository;
 import org.eclipse.equinox.p2.repository.metadata.IMetadataRepositoryManager;
 
 public class Maven2RepositoryLoader implements IRepositoryLoader {
 	private class LicenseHelper {
 		URI location;
 
 		String body;
 
 		BigInteger getDigest() {
 			String message = normalize(body);
 			try {
 				MessageDigest algorithm = MessageDigest.getInstance("MD5"); //$NON-NLS-1$
 				algorithm.reset();
 				algorithm.update(message.getBytes("UTF-8")); //$NON-NLS-1$
 				byte[] digestBytes = algorithm.digest();
 				return new BigInteger(1, digestBytes);
 			}
 			catch(NoSuchAlgorithmException e) {
 				throw new RuntimeException(e);
 			}
 			catch(UnsupportedEncodingException e) {
 				throw new RuntimeException(e);
 			}
 		}
 
 		/**
 		 * Replace all sequences of whitespace with a single whitespace character. (copied from
 		 * org.eclipse.equinox.internal.p2.metadata.License)
 		 */
 		private String normalize(String license) {
 			String text = license.trim();
 			StringBuffer result = new StringBuffer();
 			int length = text.length();
 			for(int i = 0; i < length; i++) {
 				char c = text.charAt(i);
 				boolean foundWhitespace = false;
 				while(Character.isWhitespace(c) && i < length) {
 					foundWhitespace = true;
 					c = text.charAt(++i);
 				}
 				if(foundWhitespace)
 					result.append(' ');
 				if(i < length)
 					result.append(c);
 			}
 			return result.toString();
 		}
 	}
 
 	// We are not interested in folder names that starts with a digit and a dot since
 	// that's the version folders.
 	//
 	private static final Pattern folderExcludePattern = Pattern.compile("^.*/[0-9]+\\.[^/]*/?$");
 
 	private static final String MAVEN_METADATA = "maven-metadata.xml";
 
 	private static final String MAVEN_METADATA_LOCAL = "maven-metadata-local.xml";
 
 	private static final IQuery<IInstallableUnit> QUERY_ALL_IUS = QueryUtil.createIUAnyQuery();
 
 	public static final String SIMPLE_METADATA_TYPE = org.eclipse.equinox.internal.p2.metadata.repository.Activator.ID +
 			".simpleRepository"; //$NON-NLS-1$
 
 	private Stack<UriIterator> iteratorStack;
 
 	private Iterator<VersionEntry> versionEntryItor;
 
 	private IMaven2Indexer indexer;
 
 	private URI location;
 
 	private IProvisioningAgent agent;
 
 	private MetadataRepositoryImpl repository;
 
 	private Map<String, IInstallableUnit> cachedIUs;
 
 	private static final String MAVEN_EMPTY_RANGE_STRING = "0.0.0";
 
 	private static final String REPOSITORY_CANCELLED_MESSAGE = "Repository loading was cancelled";
 
 	private static final String PROP_MAVEN_ID = "maven.artifactId";
 
 	private static final String PROP_MAVEN_GROUP = "maven.groupId";
 
 	private static final String PROP_POM_MD5 = "maven.pom.md5";
 
 	private static final String PROP_POM_SHA1 = "maven.pom.sha1";
 
 	private static final String PROP_POM_TIMESTAMP = "maven.pom.timestamp";
 
 	private static final String PROP_INDEX_TIMESTAMP = "maven.index.timestamp";
 
 	public void close() {
 		cachedIUs = null;
 	}
 
 	public IArtifactRepository getArtifactRepository(IMetadataRepository mdr, IProgressMonitor monitor)
 			throws CoreException {
 		monitor.beginTask("Generating artifact repository", 100);
 		SubMonitor subMon = SubMonitor.convert(monitor);
 		SimpleArtifactRepository ar = new SimpleArtifactRepository(
 			mdr.getProvisioningAgent(), mdr.getName(), mdr.getLocation(), null) {
 			@Override
 			public void save() {
 				// no-op
 			}
 		};
 
 		IQueryResult<IInstallableUnit> result = mdr.query(QUERY_ALL_IUS, subMon.newChild(40));
 
 		IProgressMonitor fetchMon = new SubProgressMonitor(subMon, 20);
 		fetchMon.beginTask("Collecting all IUs", 1);
 		Iterator<IInstallableUnit> itor = result.iterator();
 		ArrayList<InstallableUnit> ius = new ArrayList<InstallableUnit>();
 		while(itor.hasNext())
 			ius.add(P2Bridge.importToModel(itor.next()));
 		fetchMon.worked(1);
 		Collections.sort(ius);
 		subMon.worked(20);
 
 		IProgressMonitor targetMon = new SubProgressMonitor(subMon, 20);
 		targetMon.beginTask("Collecting all IUs", ius.size());
 		for(IInstallableUnit iu : ius) {
 			for(IArtifactKey key : iu.getArtifacts()) {
 				SimpleArtifactDescriptor ad = new SimpleArtifactDescriptor(key);
 				String groupPath = iu.getProperty(PROP_MAVEN_GROUP);
 				if(groupPath != null)
 					groupPath = groupPath.replace('.', '/');
 				String id = iu.getProperty(PROP_MAVEN_ID);
 
 				String version = VersionUtil.getVersionString(iu.getVersion());
 				ad.setRepositoryProperty(SimpleArtifactDescriptor.ARTIFACT_REFERENCE, mdr.getLocation().toString() +
 						'/' + groupPath + '/' + id + '/' + version + '/' + id + '-' + version + '.' +
 						key.getClassifier());
 				ar.addDescriptor(ad);
 			}
 			targetMon.worked(1);
 		}
 
 		return ar;
 	}
 
 	public void load(IProgressMonitor monitor) throws CoreException {
 		load(monitor, false);
 	}
 
 	public void open(URI location, IProvisioningAgent agent, MetadataRepositoryImpl mdr) throws CoreException {
 		this.location = location;
 		this.agent = agent;
 		indexer = IndexerUtils.getIndexer("nexus");
 
 		// get nexus index timestamp without using nexus tools for now
 		long remoteIndexTimestamp = getRemoteIndexTimestamp();
 
 		// if no indexer is available or no index is available, check if the repository is allowed to be crawled
 		if(indexer == null || remoteIndexTimestamp == 0L)
 			if(!robotSafe(location.toString(), "/")) {
 				StringBuilder message = new StringBuilder("Crawling of %1$s is discouraged (see %1$s/robots.txt)");
 				if(remoteIndexTimestamp != 0L)
 					message.append(". Hint: The repository is indexed. Install an index reader to map this repository.");
 
 				throw ExceptionUtils.fromMessage(message.toString(), location.toString());
 			}
 
 		repository = mdr;
 
 		iteratorStack = new Stack<UriIterator>();
 		versionEntryItor = Collections.<VersionEntry> emptyList().iterator();
 	}
 
 	public void reload(IProgressMonitor monitor) throws CoreException {
 		load(monitor, true);
 	}
 
 	private void append(StringBuilder sb, String name, boolean newLines) {
 		name = StringUtils.trimmedOrNull(name);
 		if(name != null) {
 			sb.append(name);
 			if(newLines) {
 				sb.append('\n');
 				sb.append('\n');
 			}
 		}
 	}
 
 	private LicenseHelper buildLicense(List<License> licenses) {
 		URI location = null;
 		StringBuilder body = new StringBuilder();
 		int cnt = licenses.size();
 
 		if(cnt == 1) {
 			License license = licenses.get(0);
 			try {
 				if(StringUtils.trimmedOrNull(license.getUrl()) != null)
 					location = URI.create(license.getUrl());
 				append(body, license.getName(), true);
 				append(body, license.getComments(), true);
 				append(body, "Distribution: " + license.getDistribution(), false);
 
 				LicenseHelper helper = new LicenseHelper();
 				helper.body = body.toString();
 				helper.location = location;
 				return helper;
 			}
 			catch(IllegalArgumentException e) {
 				// bad location - put location in the body as text
 			}
 		}
 
 		int i = 0;
 		for(License license : licenses) {
 			append(body, license.getName(), true);
 			append(body, license.getComments(), true);
 			append(body, license.getUrl(), true);
 			append(body, "Distribution: " + license.getDistribution(), i++ < cnt);
 		}
 
 		LicenseHelper helper = new LicenseHelper();
 		helper.body = body.toString();
 		helper.location = location;
 		return helper;
 	}
 
 	private IRepositoryLoader checkCache() throws CoreException {
 		File p2content = null;
 		try {
 			p2content = getCacheFile();
 		}
 		catch(MalformedURLException e) {
 			throw ExceptionUtils.wrap(e);
 		}
 
 		if(p2content.exists()) {
 			IConfigurationElement config = RepositoryLoaderUtils.getLoaderFor("p2");
 			return (IRepositoryLoader) config.createExecutableExtension("class");
 		}
 
 		return null;
 	}
 
 	private InstallableUnit createIU(VersionEntry versionEntry, IProgressMonitor monitor) throws IOException {
 		InstallableUnitImpl iu = (InstallableUnitImpl) P2Factory.eINSTANCE.createInstallableUnit();
 
 		iu.setId(createP2Id(versionEntry.groupId, versionEntry.artifactId));
 		iu.setVersion(versionEntry.version);
 		iu.getPropertyMap().put(PROP_MAVEN_ID, versionEntry.artifactId);
 		iu.getPropertyMap().put(PROP_MAVEN_GROUP, versionEntry.groupId);
 		iu.getPropertyMap().put(PROP_ORIGINAL_PATH, versionEntry.groupId.replace('.', '/'));
 		iu.getPropertyMap().put(PROP_ORIGINAL_ID, versionEntry.artifactId);
 		iu.setFilter(null);
 
 		POM pom;
 		Model model;
 		try {
 			pom = POM.getPOM(
 				location.toString(), versionEntry.groupId, versionEntry.artifactId, versionEntry.version.getOriginal());
 
 			String md5 = pom.getMd5();
 			String sha1 = pom.getSha1();
 			Long timestamp = pom.getTimestamp();
 
 			if(md5 != null)
 				iu.getPropertyMap().put(PROP_POM_MD5, md5);
 			if(sha1 != null)
 				iu.getPropertyMap().put(PROP_POM_SHA1, sha1);
 			if(timestamp != null)
 				iu.getPropertyMap().put(PROP_POM_TIMESTAMP, timestamp.toString());
 
 			if(!versionEntry.groupId.equals(pom.getGroupId()))
 				throw new IOException(String.format(
 					"Bad groupId in POM: expected %s, found %s", versionEntry.groupId, pom.getGroupId()));
 			if(!versionEntry.artifactId.equals(pom.getArtifactId()))
 				throw new IOException(String.format(
 					"Bad artifactId in POM: expected %s, found %s", versionEntry.artifactId, pom.getArtifactId()));
 
 			model = pom.getProject();
 
 			if(model.getDependencies() != null) {
 				for(Dependency dependency : model.getDependencies().getDependency()) {
 					// TODO What about the namespace ?
 					String namespace = dependency.isSetType()
 							? POM.expandProperties(dependency.getType(), pom.getProperties())
 							: "jar";
 
 					// TODO What about the groupId ?
 					// Yes, include: good for native maven, but not for "mavenized" p2 since artifactId is full
 					// No, don't include: good for "mavenized" p2, but not for native maven (may lead to duplicities)
 					// For now: include if artifactId is not equals to groupId or does not start with groupId followed
 					// by a dot
 					String groupId = POM.expandProperties(dependency.getGroupId(), pom.getProperties());
 					String artifactId = POM.expandProperties(dependency.getArtifactId(), pom.getProperties());
 					String versionRange = POM.expandProperties(dependency.getVersion(), pom.getProperties());
 					if(versionRange == null)
 						versionRange = MAVEN_EMPTY_RANGE_STRING;
 					VersionRange vr = VersionUtil.createVersionRange(versionRange);
 
 					IRequirement rc = P2Bridge.importToModel(MetadataFactory.createRequirement(namespace, createP2Id(
 						groupId, artifactId), vr, null, dependency.isSetOptional(), false, true));
 
 					iu.getRequirements().add(rc);
 				}
 			}
 
 			// Add 2 provided capabilities - one for an IU, another one for packaging
 			ProvidedCapabilityImpl pc = (ProvidedCapabilityImpl) P2Factory.eINSTANCE.createProvidedCapability();
 			String version = pom.getVersion();
 
 			pc.setNamespace(IInstallableUnit.NAMESPACE_IU_ID);
 			pc.setName(iu.getId());
 
 			pc.setVersion(VersionUtil.createVersion(version));
 			iu.getProvidedCapabilities().add(pc);
 
 			pc = (ProvidedCapabilityImpl) P2Factory.eINSTANCE.createProvidedCapability();
 			// TODO Namespace? See discussion above (regarding dependencies)
 			pc.setNamespace(model.getPackaging());
 			pc.setName(iu.getId());
 
 			pc.setVersion(VersionUtil.createVersion(version));
 			iu.getProvidedCapabilities().add(pc);
 
 			if(model.getLicenses() != null) {
 				List<License> toLicense = new ArrayList<License>();
 				List<License> toCopyright = new ArrayList<License>();
 
 				for(License license : model.getLicenses().getLicense()) {
 					String match = "copyright";
 					String name = license.getName();
 					String comments = license.getComments();
 
 					if(name != null && name.toLowerCase().contains(match) || comments != null &&
 							comments.toLowerCase().contains(match))
 						toCopyright.add(license);
 					else
 						toLicense.add(license);
 				}
 
 				if(toCopyright.size() > 0) {
 					LicenseHelper copyrightHelper = buildLicense(toCopyright);
 					CopyrightImpl copyright = (CopyrightImpl) P2Factory.eINSTANCE.createCopyright();
 					copyright.setBody(copyrightHelper.body);
 					copyright.setLocation(copyrightHelper.location);
 					iu.setCopyright(copyright);
 				}
 
 				if(toLicense.size() > 0) {
 					for(License license : toLicense) {
 						LicenseHelper licenseHelper = buildLicense(Collections.singletonList(license));
 						LicenseImpl p2License = (LicenseImpl) P2Factory.eINSTANCE.createLicense();
 						p2License.setBody(licenseHelper.body);
 						p2License.setLocation(licenseHelper.location);
 						p2License.setUUID(licenseHelper.getDigest().toString());
 						iu.getLicenses().add(p2License);
 					}
 				}
 			}
 
 			if(!"pom".equals(model.getPackaging())) {
 				ArtifactKeyImpl artifact = (ArtifactKeyImpl) P2Factory.eINSTANCE.createArtifactKey();
 				artifact.setId(iu.getId());
 				artifact.setVersion(iu.getVersion());
 				artifact.setClassifier(model.getPackaging());
 				iu.getArtifacts().add(artifact);
 			}
 		}
 		catch(CoreException e) {
 			IOException ioe = new IOException(e.getMessage());
 			ioe.initCause(e);
 			throw ioe;
 		}
 
 		TouchpointTypeImpl touchpointType = (TouchpointTypeImpl) P2Factory.eINSTANCE.createTouchpointType();
 
 		// TODO Set up a touchpoint! What is an installation of a maven artifact supposed to do?
 		touchpointType.setId(ITouchpointType.NONE.getId());
 		touchpointType.setVersion(ITouchpointType.NONE.getVersion());
 		iu.setTouchpointType(touchpointType);
 
 		LogUtils.debug("Adding IU: %s#%s", iu.getId(), VersionUtil.getVersionString(iu.getVersion()));
 		return iu;
 	}
 
 	private String createKey(IInstallableUnit iu) {
 		return iu.getId() + '#' + iu.getVersion().toString();
 	}
 
 	private String createKey(VersionEntry ve) {
 		return createP2Id(ve.groupId, ve.artifactId) + '#' + ve.version.toString();
 	}
 
 	private String createP2Id(String groupId, String artifactId) {
 		return (artifactId.equals(groupId) || artifactId.startsWith(groupId + '.'))
 				? artifactId
 				: (groupId + '/' + artifactId);
 	}
 
 	private boolean deleteTree(File root) {
 		boolean result = true;
 
 		if(root.isDirectory())
 			for(File f : root.listFiles()) {
 				if(f.isDirectory()) {
 					if(!deleteTree(f))
 						result = false;
 				}
 				else {
 					if(!f.delete())
 						result = false;
 				}
 			}
 
 		if(!root.delete())
 			result = false;
 
 		return result;
 	}
 
 	private MavenMetadata findNextComponent(IProgressMonitor monitor) throws CoreException {
 		if(iteratorStack.isEmpty())
 			//
 			// All iterators exhausted
 			//
 			return null;
 
 		UriIterator itor = iteratorStack.peek();
 		outer: while(itor.hasNext()) {
 			URI uri = itor.next();
 			if(isFolder(uri)) {
 				// This was a folder. Push it on the stack and
 				// scan it.
 				//
 				for(UriIterator prev : iteratorStack) {
 					if(uri.equals(prev.getRoot()))
 						//
 						// Circular reference detected. This iteration
 						// cannot be used.
 						//
 						continue outer;
 				}
 
 				UriIterator subItor;
 				try {
 					subItor = new UriIterator(uri, folderExcludePattern, monitor);
 				}
 				catch(CoreException e) {
 					LogUtils.warning(e.getMessage());
 					continue;
 				}
 
 				URI[] uris = subItor.getURIs();
 				int idx = uris.length;
 				URI mavenMetadataURI = null;
 				while(--idx >= 0) {
 					URI subUri = uris[idx];
 					IPath subPath = Path.fromPortableString(subUri.getPath());
 					String name = subPath.lastSegment();
 					if(MAVEN_METADATA.equals(name) || MAVEN_METADATA_LOCAL.equals(name)) {
 						mavenMetadataURI = subUri;
 						break;
 					}
 				}
 
 				if(mavenMetadataURI != null) {
 					// This folder has a maven-metadata.xml document. Let's filter out
 					// all sub folders that just reflect versions of that document since
 					// we will visit them anyway in due course if needed.
 					//
 					List<String> versions;
 					try {
 						MavenMetadata md = new MavenMetadata(
 							org.eclipse.emf.common.util.URI.createURI(mavenMetadataURI.toString()));
 						versions = md.getMetaData().getVersioning().getVersions().getVersion();
 					}
 					catch(Exception e) {
 						LogUtils.warning(e.getMessage());
 						continue;
 					}
 					int top = uris.length;
 					List<URI> uriList = new ArrayList<URI>();
 					for(idx = 0; idx < top; ++idx) {
 						URI subUri = uris[idx];
 						IPath subPath = Path.fromPortableString(subUri.getPath());
 						String file = subPath.lastSegment();
 						if(!versions.contains(file))
 							uriList.add(subUri);
 					}
 					if(uriList.size() < top)
 						subItor = new UriIterator(uri, folderExcludePattern, uriList.toArray(new URI[uriList.size()]));
 				}
 
 				iteratorStack.push(subItor);
 				return findNextComponent(monitor);
 			}
 
 			try {
 				IPath path = Path.fromPortableString(uri.getPath());
 				String file = path.lastSegment();
 				if(MAVEN_METADATA.equals(file) || MAVEN_METADATA_LOCAL.equals(file))
 					return new MavenMetadata(org.eclipse.emf.common.util.URI.createURI(uri.toString()));
 			}
 			catch(Exception e) {
 				LogUtils.warning(e.getMessage());
 			}
 		}
 
 		// This iterator is exhausted. Pop it from the stack
 		//
 		iteratorStack.pop();
 		return findNextComponent(monitor);
 	}
 
 	private InstallableUnit findNextIU(IProgressMonitor monitor) throws CoreException {
 		while(true) {
 			if(indexer != null) {
 				// if an indexer is used, finish loading when the iterator is exhausted
 				if(!versionEntryItor.hasNext())
 					return null;
 			}
 			else {
 				// if no indexer is used, try to obtain versions by crawling the folders
 				while(!versionEntryItor.hasNext()) {
 					if(monitor.isCanceled())
 						throw new OperationCanceledException(REPOSITORY_CANCELLED_MESSAGE);
 
 					MavenMetadata md = findNextComponent(monitor);
 					if(md == null)
 						return null;
 					versionEntryItor = getVersions(md).iterator();
 				}
 			}
 
 			if(monitor.isCanceled())
 				throw new OperationCanceledException(REPOSITORY_CANCELLED_MESSAGE);
 
 			VersionEntry ve = versionEntryItor.next();
 
 			if(cachedIUs != null) {
 				InstallableUnit iu = (InstallableUnit) cachedIUs.get(createKey(ve));
 				if(iu != null)
 					return iu;
 			}
 
 			try {
 				return createIU(ve, monitor);
 			}
 			catch(Exception e) {
 				LogUtils.warning("Skipping component " + ve.toString() + ": " + e.getMessage());
 			}
 		}
 	}
 
 	private File getCacheFile() throws MalformedURLException {
 		return new File(getCacheLocation(), "content.jar");
 	}
 
 	private File getCacheLocation() throws MalformedURLException {
 		return MavenActivator.getPlugin().getCacheDirectory(location);
 	}
 
 	private long getRemoteIndexTimestamp() throws CoreException {
 		try {
 			BufferedReader reader = null;
 			String indexPropertiesFile = "/.index/nexus-maven-repository-index.properties";
 
 			if("http".equals(location.getScheme()) || "https".equals(location.getScheme())) {
 				HttpClient httpClient = new HttpClient();
 				GetMethod method = new GetMethod(location.toString() + indexPropertiesFile);
 				method.setRequestHeader("user-agent", "");
 				int status = httpClient.executeMethod(method);
 				if(status == HttpStatus.SC_OK)
 					reader = new BufferedReader(new InputStreamReader(method.getResponseBodyAsStream()));
 			}
 			else {
 				URL url = new URL(location.toString() + indexPropertiesFile);
 				try {
 					URLConnection conn = url.openConnection();
 					reader = new BufferedReader(new InputStreamReader(conn.getInputStream()));
 				}
 				catch(FileNotFoundException e) {
 					// no index exists (very likely)
 				}
 				catch(ConnectException e) {
 					// no index exists (very likely)
 				}
 			}
 
 			if(reader != null) {
 				try {
 					String line;
 					String[] timePrefixes = new String[] { "nexus.index.timestamp=", "nexus.index.time=" };
 					while((line = reader.readLine()) != null) {
 						for(String timePrefix : timePrefixes)
 							if(line.startsWith(timePrefix)) {
 								String timeStr = line.substring(timePrefix.length());
 								SimpleDateFormat dateFormat = new SimpleDateFormat("yyyyMMddHHmmss.SSS Z");
 								return dateFormat.parse(timeStr).getTime();
 							}
 					}
 				}
 				finally {
 					reader.close();
 				}
 			}
 		}
 		catch(Exception e) {
 			throw ExceptionUtils.fromMessage(e, "Unable to contact repository %s", location.toString());
 		}
 
 		return 0L;
 	}
 
 	private List<VersionEntry> getVersions(MavenMetadata md) throws CoreException {
 		List<String> versions = md.getMetaData().getVersioning().getVersions().getVersion();
 		List<VersionEntry> versionEntries = new ArrayList<VersionEntry>(versions.size());
 		String groupId = md.getMetaData().getGroupId();
 		String artifactId = md.getMetaData().getArtifactId();
 		for(String versionString : versions) {
 			try {
 				versionEntries.add(new VersionEntry(groupId, artifactId, VersionUtil.createVersion(versionString)));
 			}
 			catch(IllegalArgumentException e) {
 				LogUtils.warning("Skipping component " + groupId + '/' + artifactId + '#' + versionString + ": " +
 						e.getMessage());
 			}
 		}
 
 		return versionEntries;
 	}
 
 	private boolean isFolder(URI uri) {
 		IPath path = Path.fromPortableString(uri.getPath());
 
 		if(path.hasTrailingSeparator())
 			return true;
 
 		String scheme = uri.getScheme();
 		if("http".equals(scheme) || "https".equals(scheme)) {
 			HttpMethod method = new HeadMethod(uri.toString());
 			HttpClient httpClient = new HttpClient();
 			try {
 				method.setFollowRedirects(false);
 				int status;
 				if((status = httpClient.executeMethod(method)) == HttpStatus.SC_MOVED_PERMANENTLY ||
 						status == HttpStatus.SC_MOVED_TEMPORARILY) {
 					Header target = method.getResponseHeader("location");
 					if(target != null && target.getValue() != null && target.getValue().endsWith("/"))
 						return true;
 				}
 
 				return false;
 			}
 			catch(Exception e) {
 				LogUtils.warning(e, "Unable to check if %s is folder: %s", uri.toString(), e.getMessage());
 				return false;
 			}
 		}
 
 		return false;
 	}
 
 	private void load(IProgressMonitor monitor, boolean avoidCache) throws CoreException {
 		IRepositoryLoader cacheLoader = null;
 		cachedIUs = null;
 		long remoteTime = getRemoteIndexTimestamp();
 
 		// if remote index is not available, get rid of the indexer
 		if(remoteTime == 0L)
 			indexer = null;
 
 		if(avoidCache)
 			removeCache();
 		else
 			cacheLoader = checkCache();
 
 		if(cacheLoader != null) {
 			LogUtils.debug("Opening cache for %s", location.toString());
 
 			try {
 				cacheLoader.open(getCacheLocation().toURI(), agent, repository);
 			}
 			catch(MalformedURLException e) {
 				throw ExceptionUtils.wrap(e);
 			}
 			cacheLoader.load(new NullProgressMonitor());
 			cacheLoader.close();
 
 			// reset location to the real location rather than the local cache file
 			repository.setLocation(location);
 
 			// remove compression property (cache is compressed but the maven original was not)
 			repository.getPropertyMap().remove(IRepository.PROP_COMPRESSED);
 
 			// remove timestamp property (cache has the timestamp of storing in the cache which is irrelevant)
 			repository.getPropertyMap().remove(IRepository.PROP_TIMESTAMP);
 
 			String cacheTimeString = repository.getPropertyMap().get(PROP_INDEX_TIMESTAMP);
 			long cacheTime = cacheTimeString != null
 					? Long.parseLong(cacheTimeString)
 					: 0L;
 
 			if(remoteTime == 0L || remoteTime > cacheTime) {
 				if(remoteTime == 0L)
 					LogUtils.debug(
 						"Unable to check if cache for %s is obsolete, repository will be scanned again",
 						location.toString());
 				else
 					LogUtils.debug("Cache for %s is obsolete, repository will be scanned again", location.toString());
 
 				// the cache is obsolete, remove it
 				removeCache();
 
 				// save a map of IUs in the cached repository - existing IUs will be reused from cache
 				cachedIUs = new HashMap<String, IInstallableUnit>(repository.getInstallableUnits().size());
 				for(IInstallableUnit iu : repository.getInstallableUnits())
 					cachedIUs.put(createKey(iu), iu);
 
 				// finally, re-initialize the target repository to empty
 				repository.removeAll();
 				repository.getPropertyMap().clear();
 			}
 			else {
 				LogUtils.debug("Cache for %s is up-to-date", location.toString());
 
 				monitor.done();
 				return;
 			}
 		}
 
 		SubMonitor subMon = SubMonitor.convert(monitor, 100);
 
 		try {
 			if(indexer != null) {
 				try {
 					indexer.openRemoteIndex(location, avoidCache);
 					versionEntryItor = indexer.getArtifacts();
 				}
 				catch(IndexNotFoundException e) {
 					LogUtils.debug("Indexer: Remote index not found at %s", location.toString());
 
 					indexer.closeRemoteIndex();
 					// set indexer to null to force standard crawling
 					indexer = null;
 				}
 			}
 
 			repository.setName("Maven2@" + location.toString());
 			repository.setLocation(location);
 			repository.setProvider(null);
 			repository.setType("maven2");
 			repository.setVersion(null);
 			repository.getPropertyMap().put(PROP_INDEX_TIMESTAMP, Long.valueOf(getRemoteIndexTimestamp()).toString());
 			// This will set a property named 'description' after saving.
 			repository.setDescription(location.toString() + " converted to p2 format");
 			// To appear is immediately in the model we also set the property directly
 			repository.getPropertyMap().put(
 				IRepository.PROP_DESCRIPTION, location.toString() + " converted to p2 format");
 
 			SubMonitor crawlMon;
 			if(indexer != null) {
 				crawlMon = subMon;
 				crawlMon.beginTask("Scanning repository", indexer.getNumberOfEntries());
 			}
 			else {
 				UriIterator itor;
 				final SubMonitor[] crawlMonRef = new SubMonitor[1];
 				itor = new UriIterator(location, folderExcludePattern, subMon.newChild(1)) {
 					@Override
 					public URI next() {
 						URI nextURI = super.next();
 						if(nextURI != null)
 							crawlMonRef[0].worked(1);
 
 						return nextURI;
 					}
 				};
 				crawlMon = subMon.newChild(99);
 				crawlMon.beginTask("Scanning repository", itor.size());
 				crawlMonRef[0] = crawlMon;
 
 				iteratorStack.add(itor);
 			}
 
 			List<IInstallableUnit> ius = repository.getInstallableUnits();
 			Map<String, IInstallableUnit> categoryMap = new HashMap<String, IInstallableUnit>();
 
 			InstallableUnit iu;
 			IProgressMonitor cancellationOnlyMonitor = new SubProgressMonitor(monitor, 0) {
 				@Override
 				public void beginTask(String name, int work) {
 					// no-op
 				}
 
 				@Override
 				public void done() {
 					// no-op
 				}
 
 				@Override
 				public void internalWorked(double work) {
 					// no-op
 				}
 
 				@Override
 				public void setTaskName(String name) {
 					// no-op
 				}
 
 				@Override
 				public void subTask(String name) {
 					// no-op
 				}
 
 				@Override
 				public void worked(int work) {
 					// no-op
 				}
 			};
 			while((iu = findNextIU(cancellationOnlyMonitor)) != null) {
 				if(crawlMon.isCanceled())
 					throw new OperationCanceledException(REPOSITORY_CANCELLED_MESSAGE);
 
 				if(indexer != null)
 					crawlMon.worked(1);
 
 				String groupId = iu.getProperty(PROP_MAVEN_GROUP);
 				InstallableUnitImpl category = (InstallableUnitImpl) categoryMap.get(groupId);
 				if(category == null) {
 					category = (InstallableUnitImpl) P2Factory.eINSTANCE.createInstallableUnit();
 					category.setId(groupId);
 					category.setVersion(Version.emptyVersion);
 					category.getPropertyMap().put(InstallableUnitDescription.PROP_TYPE_CATEGORY, "true");
 					category.getPropertyMap().put(IInstallableUnit.PROP_NAME, "Group " + groupId);
 					ius.add(category);
 					categoryMap.put(groupId, category);
 				}
 
 				IRequirement rc = P2Bridge.importToModel(MetadataFactory.createRequirement(
 					IInstallableUnit.NAMESPACE_IU_ID, iu.getId(), new VersionRange(
 						iu.getVersion(), true, iu.getVersion(), true), null, false, false, true));
 				List<IRequirement> rcList = category.getRequirements();
 				rcList.add(rc);
 
 				ius.add(iu);
 			}
 
 			storeCache();
 		}
 		finally {
 			if(indexer != null)
 				indexer.closeRemoteIndex();
 			subMon.done();
 		}
 	}
 
 	private void removeCache() throws CoreException {
 		IMetadataRepositoryManager mdrMgr = null;
 		mdrMgr = P2Utils.getRepositoryManager(agent, IMetadataRepositoryManager.class);
 		try {
 			mdrMgr.removeRepository(getCacheLocation().toURI());
 			deleteTree(getCacheLocation());
 		}
 		catch(MalformedURLException e) {
 			throw ExceptionUtils.wrap(e);
 		}
 		finally {
 			P2Utils.ungetRepositoryManager(agent, mdrMgr);
 		}
 	}
 
 	private boolean robotSafe(String baseURL, String path) throws CoreException {
 		String robotStr = baseURL + "/robots.txt";
 		URL robotURL;
 		try {
 			robotURL = new URL(robotStr);
 		}
 		catch(MalformedURLException e) {
 			throw ExceptionUtils.fromMessage(e.getMessage());
 		}
 
 		StringBuilder commands = new StringBuilder();
 		InputStream robotStream = null;
 
 		try {
 			robotStream = robotURL.openStream();
 			byte buffer[] = new byte[1024];
 			int read;
 			while((read = robotStream.read(buffer)) != -1)
 				commands.append(new String(buffer, 0, read));
 		}
 		catch(IOException e) {
 			// no robots.txt file => safe to crawl
 			return true;
 		}
 		finally {
 			if(robotStream != null)
 				try {
 					robotStream.close();
 				}
 				catch(IOException e) {
 					// ignore
 				}
 		}
 
 		// search for "Disallow:" commands.
 		int index = 0;
 		String disallow = "Disallow:";
 		while((index = commands.indexOf(disallow, index)) != -1) {
 			index += disallow.length();
 			String commandPath = commands.substring(index);
 			StringTokenizer tokenizer = new StringTokenizer(commandPath);
 
 			if(!tokenizer.hasMoreTokens())
 				break;
 
 			String disallowedPath = tokenizer.nextToken();
 
 			// if the URL starts with a disallowed path, it is not safe
 			if(path.indexOf(disallowedPath) == 0)
 				return false;
 		}
 
 		return true;
 	}
 
 	private void storeCache() throws CoreException {
 		IMetadataRepositoryManager mdrMgr = null;
 		mdrMgr = P2Utils.getRepositoryManager(agent, IMetadataRepositoryManager.class);
 		try {
 			Map<String, String> properties = new HashMap<String, String>(2);
 			properties.put(IRepository.PROP_COMPRESSED, "true");
 			properties.put(PROP_INDEX_TIMESTAMP, repository.getPropertyMap().get(PROP_INDEX_TIMESTAMP));
 			properties.put(IRepository.PROP_DESCRIPTION, repository.getDescription());
 			IMetadataRepository mdr = null;
 			try {
 				mdr = mdrMgr.createRepository(
 					getCacheLocation().toURI(), repository.getName(), SIMPLE_METADATA_TYPE, properties);
 			}
 			catch(MalformedURLException e) {
 				throw ExceptionUtils.wrap(e);
 			}
 			mdr.addInstallableUnits(repository.getInstallableUnits());
 		}
 		finally {
 			P2Utils.ungetRepositoryManager(agent, mdrMgr);
 		}
 	}
 }
