 package pt.utl.ist.fenix.tools.file.dspace;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
 import org.apache.commons.httpclient.HttpClient;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpStatus;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.multipart.FilePart;
 import org.apache.commons.httpclient.methods.multipart.MultipartRequestEntity;
 import org.apache.commons.httpclient.methods.multipart.Part;
 import org.apache.commons.httpclient.methods.multipart.StringPart;
 
 import pt.utl.ist.fenix.tools.file.FileDescriptor;
 import pt.utl.ist.fenix.tools.file.FileSearchCriteria;
 import pt.utl.ist.fenix.tools.file.FileSearchResult;
 import pt.utl.ist.fenix.tools.file.FileSet;
 import pt.utl.ist.fenix.tools.file.FileSetDescriptor;
 import pt.utl.ist.fenix.tools.file.FileSetQueryResults;
 import pt.utl.ist.fenix.tools.file.FilesetMetadataQuery;
 import pt.utl.ist.fenix.tools.file.VirtualPath;
 import pt.utl.ist.fenix.tools.file.XMLSerializable;
 import pt.utl.ist.fenix.tools.file.utils.FileUtils;
 
 public class DSpaceHttpClient implements IDSpaceClient {
 
 	private static final String DSPACE_ENCODING = "UTF-8";
 
 	private static final String SUCCESS_CODE = "SUCCESS";
 
 	private static final String UNEXPECTED_ERROR_CODE = "UNEXPECTED_ERROR";
 
 	private static final String DSPACE_REMOTE_MANAGER_SERVLET = "/DSpaceHTTPRemoteManagerServlet";
 
 	private static final String DSPACE_REMOTE_DOWNLOAD_SERVLET = "/DSpaceFileSetDownloadServlet";
 
 	private String remoteInterfaceUrl;
 
 	private String remoteDownloadInterfaceUrl;
 
 	private String username;
 
 	private String password;
 
 	// private DSpaceFileManager fileManager=null;
 
 	public DSpaceHttpClient() {
 	}
 
 	public void init(DSpaceFileManager fileManager) {
 		// this.fileManager=fileManager;
 		this.remoteInterfaceUrl = fileManager.getProperty("dspace.serverUrl") + DSPACE_REMOTE_MANAGER_SERVLET;
 		this.remoteDownloadInterfaceUrl = fileManager.getProperty("dspace.serverUrl")
 				+ DSPACE_REMOTE_DOWNLOAD_SERVLET;
 		this.username = fileManager.getProperty("dspace.username");
 		this.password = fileManager.getProperty("dspace.password");
 	}
 
 	public FileSetDescriptor uploadFileSet(VirtualPath path, String originalFilename, FileSet fs,
 			boolean privateFile) throws DSpaceClientException {
 
 		FileSetUploadRequest request = new FileSetUploadRequest(path, originalFilename, privateFile, fs);
 		Collection<File> allFiles = request.getFileSet().getAllFiles();
 
 		Part[] additionalParts = new Part[allFiles.size()];
 
 		int i = 0;
 		for (File f : allFiles)
 			try {
 				additionalParts[i++] = new FilePart(f.getAbsolutePath(), f);
 			} catch (FileNotFoundException e) {
 				throw new DSpaceClientException(e);
 			}
 
 		return ((FileSetUploadResponse) executeRemoteMethod("uploadFileSet", request, username, password,
 				FileSetUploadResponse.class, additionalParts)).getFileSetDescriptor();
 	}
 
 	public void deleteFileSet(FileSetDescriptor descriptor) throws DSpaceClientException {
 
 		FileSetDeleteRequest request = new FileSetDeleteRequest(descriptor);
 		FileSetDeleteResponse response = ((FileSetDeleteResponse) executeRemoteMethod("deleteFileSet",
 				request, username, password, FileSetDeleteResponse.class, new Part[0]));
 		if (response.getError() != null)
 			throw new DSpaceClientException(response.getError());
 
 	}
 
 	public void changeFileSetPermissions(FileSetDescriptor descriptor, boolean privateFile)
 			throws DSpaceClientException {
 
 		FileSetPermissionChangeRequest request = new FileSetPermissionChangeRequest(descriptor, privateFile);
 		FileSetPermissionChangeResponse response = ((FileSetPermissionChangeResponse) executeRemoteMethod(
 				"changeFileSetPermissions", request, username, password,
 				FileSetPermissionChangeResponse.class, new Part[0]));
 		if (response.getError() != null)
 			throw new DSpaceClientException(response.getError());
 
 	}
 
 	public FileSet retrieveFileSet(FileSetDescriptor descriptor) throws DSpaceClientException {
 
 		Collection<FileDescriptor> allDescriptorsRecursive = descriptor.recursiveListAllFileDescriptors();
 
 		File dirForTempDownload;
 		try {
 			dirForTempDownload = pt.utl.ist.fenix.tools.file.utils.FileUtils.createTemporaryDir(
 					"DownloadDSpace", ".tmp");
 		} catch (IOException e1) {
 			throw new DSpaceClientException(e1);
 		}
 
 		// get the base root directory from the first entrance in the fileset
 		String absoluteParentPath = descriptor.getContentFileDescriptor(0).getOriginalAbsoluteFilePath();
 
 		FileSet fsRetVal = descriptor.createRecursiveFileSet();
 
 		// by now all the descriptors recursively should be in
 		// allDescriptorsRecursive...
 		// now download them all via http
 		HttpClient client = new HttpClient();
 		for (FileDescriptor desc : allDescriptorsRecursive) {
 			String downloadUrl = remoteDownloadInterfaceUrl + "?username=" + username + "&password="
 					+ password + "&uniqueId=" + desc.getUniqueId();
 
 			GetMethod gm = new GetMethod(downloadUrl);
 			try {
 				int result = client.executeMethod(gm);
 
 				if (result == HttpStatus.SC_OK) {
 					File f = new File(dirForTempDownload, FileUtils.makeRelativePath(absoluteParentPath, desc
 							.getOriginalAbsoluteFilePath(), "download"));
 					FileOutputStream fos;
 					try {
 						fos = new FileOutputStream(f);
 						FileUtils.copyInputStreamToOutputStream(gm.getResponseBodyAsStream(), fos);
 						//fsRetVal.addContentFile(f);
 						fsRetVal.replaceFileWithAbsolutePath(desc.getOriginalAbsoluteFilePath(), f);
 					} catch (FileNotFoundException e) {
 						throw new DSpaceClientException(e);
 					} catch (IOException e) {
 						throw new DSpaceClientException(e);
 					}
 				} else {
 					throw new DSpaceClientException("Unable to download file " + desc.getFilename()
 							+ " with unique id " + desc.getUniqueId() + " because of an http error: "
 							+ result);
 				}
 
 			} catch (HttpException e) {
 				throw new DSpaceClientException(e);
 			} catch (IOException e) {
 				throw new DSpaceClientException(e);
 			} finally {
 				gm.releaseConnection();
 			}
 		}
 		return fsRetVal;
 	}
 
 	public FileSetDescriptor listAllDescriptorsFromRoot(FileSetDescriptor rootFileSetDescriptor)
 			throws DSpaceClientException {
 		FileSetListRecursiveRequest request = new FileSetListRecursiveRequest(rootFileSetDescriptor);
 		FileSetListRecursiveResponse response = ((FileSetListRecursiveResponse) executeRemoteMethod(
 				"listRecursiveFileSet", request, username, password, FileSetListRecursiveResponse.class,
 				new Part[0]));
 		if (response.getError() != null)
 			throw new DSpaceClientException(response.getError());
 
 		return response.getFileSetDescriptor();
 
 	}
 
 	public XMLSerializable executeRemoteMethod(String methodName, XMLSerializable request, String username,
 			String password, Class<? extends XMLSerializable> responseClass, Part... additionalParts)
 			throws DSpaceClientException {
 		PostMethod post = new PostMethod(remoteInterfaceUrl);
 		DspaceResponse response;
 		try {
 			HttpClient client = new HttpClient();
 			Part[] parts = new Part[4 + (additionalParts != null ? additionalParts.length : 0)];
 			parts[0] = new StringPart("username", username, DSPACE_ENCODING);
 			parts[1] = new StringPart("password", password, DSPACE_ENCODING);
 			parts[2] = new StringPart("method", methodName, DSPACE_ENCODING);
 			parts[3] = new StringPart("message", request.toXMLString(), DSPACE_ENCODING);
 			if (additionalParts != null)
 				System.arraycopy(additionalParts, 0, parts, 4, additionalParts.length);
 
 			post.setRequestEntity(new MultipartRequestEntity(parts, post.getParams()));
 			client.executeMethod(post);
 			response = getDspaceResponse(post.getResponseBodyAsString());
 
 		} catch (HttpException e) {
 			throw new DSpaceClientException(e);
 		} catch (IOException e) {
 			throw new DSpaceClientException(e);
 		} finally {
 			post.releaseConnection();
 		}
 
 		if (!response.responseCode.equals(SUCCESS_CODE)) {
			throw new DSpaceClientException(response.responseCode);
 		}
 
 		XMLSerializable responseObject;
 		try {
 			responseObject = responseClass.newInstance();
 			responseObject.fromXMLString(response.responseMessage);
 			return responseObject;
 		} catch (InstantiationException e) {
 			throw new DSpaceClientException(e);
 		} catch (IllegalAccessException e) {
 			throw new DSpaceClientException(e);
 		}
 	}
 
 	private static class DspaceResponse {
 		public String responseCode;
 
 		public String responseMessage;
 
 		public DspaceResponse(String responseCode, String responseMessage) {
 			this.responseCode = responseCode;
 			this.responseMessage = responseMessage;
 		}
 	}
 
 	private static DspaceResponse getDspaceResponse(String rawResponse) throws DSpaceClientException {
		if (rawResponse.length() == 0 || rawResponse.contains("<error>")) {
			throw new DSpaceClientException(UNEXPECTED_ERROR_CODE + "\nBody:" + rawResponse);
 		}
 
 		int firstIndexOfNewLine = rawResponse.indexOf('\n');
 		String responseCode = rawResponse.substring(0, firstIndexOfNewLine);
 		String responseMessage = rawResponse.substring(firstIndexOfNewLine + 1);
 
 		return new DspaceResponse(responseCode, responseMessage);
 	}
 
 	public FileSetDescriptor getRootDescriptor(FileSetDescriptor innerChildDescriptor)
 			throws DSpaceClientException {
 		FileSetRootDescriptorRequest request = new FileSetRootDescriptorRequest(innerChildDescriptor);
 		FileSetRootDescriptorResponse response = ((FileSetRootDescriptorResponse) executeRemoteMethod(
 				"getRootDescriptor", request, username, password, FileSetRootDescriptorResponse.class,
 				new Part[0]));
 		if (response.getError() != null)
 			throw new DSpaceClientException(response.getError());
 
 		return response.getFileSetDescriptor();
 	}
 
 	public FileSetQueryResults searchFileSets(FilesetMetadataQuery query,
 			VirtualPath optionalPathToRestrictSearch) throws DSpaceClientException {
 		FileSetMetadataSearchRequest request = new FileSetMetadataSearchRequest(query,
 				optionalPathToRestrictSearch);
 		FileSetMetadataSearchResponse response = ((FileSetMetadataSearchResponse) executeRemoteMethod(
 				"searchFileSets", request, username, password, FileSetMetadataSearchResponse.class,
 				new Part[0]));
 		if (response.getError() != null)
 			throw new DSpaceClientException(response.getError());
 
 		return response.getResults();
 	}
 
 	public FileSearchResult searchFiles(FileSearchCriteria criteria) throws DSpaceClientException {
 		return searchFiles(criteria, null);
 	}
 
 	public FileSearchResult searchFiles(FileSearchCriteria criteria, VirtualPath optionalPathToRestrictSearch)
 			throws DSpaceClientException {
 		List<FileDescriptor> descriptors = new ArrayList<FileDescriptor>();
 		FilesetMetadataQuery query = criteria.getQuery();
 
 		FileSetQueryResults results = searchFileSets(query, optionalPathToRestrictSearch);
 		for (FileSetDescriptor descriptor : results.getResults()) {
 			descriptors.add(getRootDescriptor(descriptor).getContentFileDescriptor(0));
 		}
 
 		return new FileSearchResult(descriptors, query.getStart(), query.getPageSize(), results
 				.getHitsCount());
 	}
 }
