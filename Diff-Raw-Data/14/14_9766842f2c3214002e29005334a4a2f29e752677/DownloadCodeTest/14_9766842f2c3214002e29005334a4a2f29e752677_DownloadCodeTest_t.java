 package test.backend;
 
 import icloude.contents.FileContent;
 import icloude.request_handlers.DownloadCodeRequestHandler;
 import icloude.request_handlers.NewFileRequestHandler;
 import icloude.request_handlers.NewProjectRequestHandler;
 import icloude.request_handlers.UploadFileRequestHandler;
 import icloude.requests.DownloadCodeRequest;
 import icloude.requests.NewFileRequest;
 import icloude.requests.NewProjectRequest;
 import icloude.requests.UploadFileRequest;
 import icloude.responses.IDResponse;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.Reader;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.zip.ZipEntry;
 import java.util.zip.ZipInputStream;
 
 import storage.Database;
 import storage.DatabaseException;
 import storage.StoringType;
 import storage.file.File;
 import storage.project.Project;
 import storage.projectitem.ProjectItem;
 
 public class DownloadCodeTest extends Test {
 
 	public DownloadCodeTest() {
 		tests.add(new OneFileTest()); // All tests added here!!!
 	}
 
 	private abstract class InnerTest extends Test {
 		public List<TestResult> launch() {
 			String projectID = createProject();
 			List<TestResult> result = new ArrayList<TestResult>();
 			if (projectID != null) {
 				result.add(new TestResult(compareZIP(getZIP(projectID),
 						projectID), getName()));
 			} else {
 				result.add(new TestResult(false, getName()));
 			}
 			return result;
 		}
 
 		protected InputStream getZIP(String projectID) {
 			DownloadCodeRequestHandler dcrh = new DownloadCodeRequestHandler();
 			DownloadCodeRequest dcr = new DownloadCodeRequest(1,
 					"DownloadCodeRequest", "downloadcode", "UserIDZIP",
 					projectID);
 			return dcrh.get(GSON.toJson(dcr));
 		}
 
 		protected Boolean compareZIP(InputStream zip, String projectID) {
 			Boolean passed = true;
 			ZipInputStream zin = null;
 			ZipEntry entry;
 			try {
 				zin = new ZipInputStream(zip);
 				Project project = (Project) Database.get(StoringType.PROJECT,
 						projectID);
 				Map<String, ProjectItem> projectStructure = project
 						.getContent();
 				Map<String, String> pathsFromDB = new HashMap<String, String>();
 				for (String key : projectStructure.keySet()) {
 					pathsFromDB.put(getFullPath(key, projectStructure), key);
 				}
 				List<String> pathsFromZIP = new ArrayList<String>();
 				while ((entry = zin.getNextEntry()) != null) {
 					if (!entry.isDirectory()) {
 						if (pathsFromDB.containsKey(entry.getName())) {
 							Reader zipReader = new InputStreamReader(zin);
 							Reader fileReader = ((File) (projectStructure
 									.get(pathsFromDB.get(entry.getName()))))
 									.openForReading();
 							if (!isSame(zipReader, fileReader)) {
 								passed = false;
 								break;
 							}
 						} else {
 							passed = false;
 							break;
 						}
 					}
 					pathsFromZIP.add(entry.getName());
 				}
 				// All keys in pathsFromDB MUST be presented in pathsFromZIP
 			} catch (IOException e) {
 				passed = false;
 			} catch (DatabaseException e) {
 				passed = false;
 			} catch (Exception e) {
 				passed = false;
 			} finally {
 				if (zin != null) {
 					try {
 						zin.close();
 					} catch (IOException e) {
 						passed = false;
 					}
 				}
 			}
 			return passed;
 		}
 
 		/**
 		 * Compare two input stream
 		 * 
 		 * @param input1
 		 *            the first stream
 		 * @param input2
 		 *            the second stream
 		 * @return true if the streams contain the same content, or false
 		 *         otherwise
 		 * @throws IOException
 		 * @throws IllegalArgumentException
 		 *             if the stream is null
 		 */
 		private Boolean isSame(Reader input1, Reader input2) throws IOException {
 			boolean error = false;
 			try {
 				char[] buffer1 = new char[DEFAULT_BUFFER_SIZE];
 				char[] buffer2 = new char[DEFAULT_BUFFER_SIZE];
 				try {
 					int numRead1 = 0;
 					int numRead2 = 0;
 					while (true) {
 						numRead1 = input1.read(buffer1);
 						numRead2 = input2.read(buffer2);
 						if (numRead1 > -1) {
 							if (numRead2 != numRead1)
 								return false;
 							// Otherwise same number of bytes read
 							if (!Arrays.equals(buffer1, buffer2))
 								return false;
 							// Otherwise same bytes read, so continue ...
 						} else {
 							// Nothing more in stream 1 ...
 							return numRead2 < 0;
 						}
 					}
 				} finally {
 					// input1.close();
 				}
 			} catch (IOException e) {
 				error = true; // this error should be thrown, even if there is
 								// an error closing stream 2
 				throw e;
 			} catch (RuntimeException e) {
 				error = true; // this error should be thrown, even if there is
 								// an error closing stream 2
 				throw e;
 			} finally {
 				try {
 					input2.close();
 				} catch (IOException e) {
 					if (!error)
 						throw e;
 				}
 			}
 		}
 
 		private String getFullPath(String key, Map<String, ProjectItem> project) {
 			String currentItemKey = key;
 			ProjectItem currentItem = project.get(currentItemKey);
 			StringBuilder fullPath = new StringBuilder();
 			if (!(currentItem instanceof File)) {
 				fullPath.insert(0, '/');
 			}
 			while (currentItem.getParentKey() != null) {
 				fullPath.insert(0, currentItem.getName());
 				fullPath.insert(0, '/');
 				currentItemKey = currentItem.getParentKey();
 				currentItem = project.get(currentItemKey);
 			}
 			fullPath.insert(0, currentItem.getName());
 			return fullPath.toString();
 		}
 
 		protected abstract String createProject();
 	}
 
 	private class OneFileTest extends InnerTest {
 
 		protected String createProject() {
 			String json;
 
 			NewProjectRequestHandler nprh = new NewProjectRequestHandler();
 			NewFileRequestHandler nfrh = new NewFileRequestHandler();
 			UploadFileRequestHandler ufrh = new UploadFileRequestHandler();
 
 			NewProjectRequest npr = new NewProjectRequest(PROTOCOL_VERSION,
 					"NewProjectRequest", "newproject", "userIDZIP",
 					"projectZIP", "typeZIP");
 			json = nprh.post(GSON.toJson(npr));
 			IDResponse idrProj = GSON.fromJson(json, IDResponse.class);
 			NewFileRequest nfr = new NewFileRequest(PROTOCOL_VERSION,
 					"NewFileRequest", "newfile", "userIDZIP",
 					idrProj.getProjectID(), idrProj.getEntityID(), "fileZIP",
 					"typeZIP");
 			json = nfrh.post(GSON.toJson(nfr));
 			IDResponse idrFile = GSON.fromJson(json, IDResponse.class);
			FileContent content = new FileContent("file", "New name",
 					idrFile.getEntityID(), "Hello, I am text of this file!!!",
 					"textFile", "userIDZIP", "ZIPRevision",
 					(new Date()).getTime(), (new Date()).getTime());
 			UploadFileRequest ufr = new UploadFileRequest(PROTOCOL_VERSION,
 					"UploadFileRequest", "uploadfile", "userIDZIP",
 					idrProj.getProjectID(), content);
 			json = ufrh.post(GSON.toJson(ufr));
 
 			return idrProj.getProjectID();
 		}
 	}
 
 }
