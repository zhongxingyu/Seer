 package org.codefaces.core.svn.internal.clientadaptor;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.StringWriter;
 import java.net.MalformedURLException;
 
 import javax.servlet.http.HttpSessionBindingEvent;
 import javax.servlet.http.HttpSessionBindingListener;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.codefaces.core.connectors.SCMIOException;
 import org.codefaces.core.connectors.SCMResponseException;
 import org.codefaces.core.connectors.SCMURLException;
 import org.codefaces.core.svn.internal.operations.SVNConstants;
 import org.eclipse.rwt.RWT;
 import org.eclipse.rwt.SessionSingletonBase;
 import org.tigris.subversion.clientadapter.Activator;
 import org.tigris.subversion.svnclientadapter.ISVNClientAdapter;
 import org.tigris.subversion.svnclientadapter.ISVNDirEntry;
 import org.tigris.subversion.svnclientadapter.ISVNInfo;
 import org.tigris.subversion.svnclientadapter.SVNClientException;
 import org.tigris.subversion.svnclientadapter.SVNNodeKind;
 import org.tigris.subversion.svnclientadapter.SVNRevision;
 import org.tigris.subversion.svnclientadapter.SVNUrl;
 
 /**
  * An abstract layer to adapt the underlining client provider
  */
 public class SVNClientAdaptor implements HttpSessionBindingListener {
 	private ISVNClientAdapter client;
 
 	private static final String ID = SVNClientAdaptor.class.toString();
 
 	/**
 	 * no parameter constructor for session singleton use
 	 */
 	protected SVNClientAdaptor() {
 		Activator activator = Activator.getDefault();
 		final ISVNClientAdapter client = activator
 				.getClientAdapter(SVNConstants.CLIENT_ADAPTOR_ID);
 		this.client = client;
 
 		RWT.getSessionStore().getHttpSession().setAttribute(ID, this);
 	}
 
 	/**
 	 * constructor for testing purpose
 	 */
 	protected SVNClientAdaptor(ISVNClientAdapter client) {
 		this.client = client;
 	}
 
 	/**
 	 * @return underlining client
 	 */
 	private ISVNClientAdapter getClient() {
 		return client;
 	}
 
 	/**
 	 * @return the info of the given svn repository
 	 * @param url
 	 *            the URL of the repository
 	 * @param username
 	 *            user name of the repository, can be null
 	 * @param password
 	 *            password of the repository, can be null
 	 */
 	public SVNRepoInfo getRepoInfo(String url, String username, String password) {
 		SVNRepoInfo svnInfo = null;
 		try {
 			SVNUrl svnUrl = new SVNUrl(url);
 			ISVNClientAdapter client = getClient();
 
 			String svnUsername = (username == null ? "" : username);
 			String svnPassword = (password == null ? "" : password);
 			client.setUsername(svnUsername);
 			client.setPassword(svnPassword);
 
 			ISVNInfo info = client.getInfo(svnUrl);
 			svnInfo = new SVNRepoInfo(info.getUrlString(), info.getUuid(), info
 					.getRevision().getNumber());
 		} catch (MalformedURLException e) {
 			throw new SCMURLException("Invalid repository url: " + url);
 		} catch (SVNClientException e) {
 			throw constructSCMResponseException(e);
 		}
 		return svnInfo;
 	}
 
 	/**
 	 * @return the directory entries in the given url
 	 * @param url
 	 *            the URL of the directory
 	 * @param username
 	 *            user name of the repository, can be null
 	 * @param password
 	 *            password of the repository, can be null
 	 */
 	public SVNDirectoryEntry[] getDirectoryEntries(String url, String username,
 			String password) {
 		SVNDirectoryEntry[] svnEntries = null;
 		try {
 			SVNUrl svnUrl = new SVNUrl(url);
 			ISVNClientAdapter client = getClient();
 
 			String svnUsername = (username == null ? "" : username);
 			String svnPassword = (password == null ? "" : password);
 			client.setUsername(svnUsername);
 			client.setPassword(svnPassword);
 
 			ISVNDirEntry[] entries = client.getList(svnUrl, SVNRevision.HEAD,
 					false);
 
 			svnEntries = new SVNDirectoryEntry[entries.length];
 			for (int i = 0; i < entries.length; i++) {
 				ISVNDirEntry entry = entries[i];
 				svnEntries[i] = new SVNDirectoryEntry(svnUrl + "/"
 						+ entry.getPath(), entry.getPath(),
 						getResourceKind(entry.getNodeKind()), entry.getSize(),
 						entry.getLastChangedDate(), entry
 								.getLastChangedRevision().getNumber());
 			}
 
 			return svnEntries;
 
 		} catch (MalformedURLException e) {
 			throw new SCMURLException("Invalid repository url: " + url);
 		} catch (SVNClientException e) {
 			throw constructSCMResponseException(e);
 		}
 	}
 
 	/**
 	 * @return checkout the resource of the given url
 	 */
 	public SVNResource getResource(String url, String username, String password) {
 		SVNResource resource = null;
 		InputStream stream = null;
 		StringWriter writer = null;
 
 		try {
 			SVNUrl svnUrl = new SVNUrl(url);
 			ISVNClientAdapter client = getClient();
 
 			String svnUsername = (username == null ? "" : username);
 			String svnPassword = (password == null ? "" : password);
 			client.setUsername(svnUsername);
 			client.setPassword(svnPassword);
 
 			ISVNDirEntry entry = client.getDirEntry(svnUrl, SVNRevision.HEAD);
 			stream = client.getContent(svnUrl, SVNRevision.HEAD);
 			writer = new StringWriter();
 			IOUtils.copy(stream, writer);
 
 			resource = new SVNResource(svnUrl + "/" + entry.getPath(),
 					entry.getPath(), null, entry.getSize(), writer.toString(),
 					entry.getLastChangedDate(), entry.getLastChangedRevision()
 							.getNumber());
 
 		} catch (MalformedURLException e) {
 			throw new SCMURLException("Invalid repository url: " + url);
 		} catch (SVNClientException e) {
 			throw constructSCMResponseException(e);
 		} catch (IOException e) {
 			throw new SCMIOException(e.getMessage(), e);
 		} finally {
 			try {
 				if (stream != null) {
 					stream.close();
 				}
 				if (writer != null) {
 					writer.close();
 				}
 			} catch (IOException e) {
 			}
 		}
 		return resource;
 	}
 
 	/**
	 * The orginal SVNClientException error message is not so meaningful. 
	 * We construct a new SCMResponseException
	 * 
 	 * @return SCMResponseException based on the original error message
 	 * @param e the original exception
 	 */
 	private SCMResponseException constructSCMResponseException(SVNClientException e) {
		String rawErrMsg = e.getMessage().toLowerCase();
		String errMsg;
		if(rawErrMsg.contains("authentication")){
			errMsg = "Authorization failed. Wrong username or password.";
		}
		else{
			errMsg = "Unable to connect to the repository.";
		}
 		return new SCMResponseException(errMsg, e);
 	}
 
 	/**
 	 * Map the nodekind of the underlining client to SVNResourceKind
 	 */
 	private SVNResourceKind getResourceKind(SVNNodeKind nodeKind) {
 		if (nodeKind == SVNNodeKind.DIR) {
 			return SVNResourceKind.DIRECTORY;
 		}
 		if (nodeKind == SVNNodeKind.FILE) {
 			return SVNResourceKind.FILE;
 		}
 		return SVNResourceKind.UNKNOWN;
 	}
 
 	/**
 	 * @return a session-based instance of SVNClinetAdaptor
 	 */
 	public static SVNClientAdaptor getCurrent() {
 		return (SVNClientAdaptor) SessionSingletonBase
 				.getInstance(SVNClientAdaptor.class);
 	}
 
 	@Override
 	public void valueBound(HttpSessionBindingEvent event) {
 
 	}
 
 	@Override
 	public void valueUnbound(HttpSessionBindingEvent event) {
 		client.dispose();
 	}
 }
