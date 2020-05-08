 package com.marklogic.ant.tasks;
 
 import static com.marklogic.xcc.ContentFactory.newContent;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.tools.ant.BuildException;
 import org.apache.tools.ant.types.resources.FileResource;
 
 import com.google.common.base.Throwables;
 import com.marklogic.ant.types.Permission;
 import com.marklogic.ant.types.ResourceFileSet;
 import com.marklogic.xcc.AdhocQuery;
 import com.marklogic.xcc.Content;
 import com.marklogic.xcc.ContentCreateOptions;
 import com.marklogic.xcc.ContentPermission;
 import com.marklogic.xcc.Session;
 import com.marklogic.xcc.exceptions.RequestException;
 
 /**
  * @author Bob Browning <bob.browning@pressassociation.com>
  */
 public abstract class AbstractInstallTask extends AbstractDeploymentTask {
 
 	protected static final String ACTION_INSTALL_CONTENT = "install-content";
 	protected static final String ACTION_INSTALL_CPF = "install-cpf";
 	protected static final String ACTION_INSTALL_DATABASES = "install-databases";
     protected static final String ACTION_UPDATE_DATABASES = "update-databases";
     protected static final String ACTION_INSTALL_INDICES = "install-indices";
 	protected static final String ACTION_INSTALL_FIELDS = "install-fields";
 	protected static final String ACTION_INSTALL_TRIGGERS = "install-triggers";
 	protected static final String ACTION_INSTALL_SERVERS = "install-servers";
     protected static final String ACTION_UPDATE_SERVERS = "update-servers";
     protected static final String ACTION_INSTALL_TASKS = "install-tasks";
 
 	protected void installContent() throws BuildException {
 		/* Install content resources from maven project */
 		installResources(getEnvironment().getResources());
 	}
 
 	protected void installCPF() throws BuildException {
 		/* Install pipeline resources from maven project */
 		installPipeline(getEnvironment().getPipelineResources());
 
 		executeAction(ACTION_INSTALL_CPF);
 	}
 
 	protected void installDatabases() throws BuildException {
 		executeAction(ACTION_INSTALL_DATABASES);
 	}
 
     protected void updateDatabases() throws BuildException {
         executeAction(ACTION_UPDATE_DATABASES);
     }
 
 	protected void installIndices() throws BuildException {
 		executeAction(ACTION_INSTALL_INDICES);
 	}
 
 	protected void installFields() throws BuildException {
 		executeAction(ACTION_INSTALL_FIELDS);
 	}
 
 	protected void installTriggers() throws BuildException {
 		executeAction(ACTION_INSTALL_TRIGGERS);
 	}
 
 	protected void installTasks() throws BuildException {
 		executeAction(ACTION_INSTALL_TASKS);
 	}
 
 	protected void installServers() throws BuildException {
 		executeAction(ACTION_INSTALL_SERVERS);
 	}
 
     protected void updateServers() throws BuildException {
         executeAction(ACTION_UPDATE_SERVERS);
     }
 
 	private void installPipeline(List<ResourceFileSet> resources) {
 		try {
             String applicationName = getEnvironment().getApplicationName();
 			for (final ResourceFileSet resource : resources) {
 				final String targetDatabase =
                         applicationName != null ? applicationName + "-" + resource.getDatabase() : resource.getDatabase();
 				System.out
 						.println(" -- ".concat(targetDatabase).concat(" -- "));
 
 				/*
 				 * Get connection to database for uploading content
 				 */
 				Session session = getSession(targetDatabase);
 
 				AdhocQuery query = session
 						.newAdhocQuery("(::)\n"
 								+ "xquery version \"1.0-ml\";\n"
 								+ "import module namespace p = \"http://marklogic.com/cpf/pipelines\" at \"/MarkLogic/cpf/pipelines.xqy\";\n"
 								+ "declare variable $file as xs:string external; \n"
 								+ "p:insert( xdmp:unquote($file)/* )\n"
 								+ "(::)");
 
 				Iterator it = resource.iterator();
 				while (it.hasNext()) {
 					String f = (String) it.next();
 					File sourceFile = new File(resource.getDir(), f);
 					System.out.println(String.format(
 							"Loading pipeline configuration %s",
 							sourceFile.getPath()));
 					try {
 						query.setNewStringVariable("file",
 								getFileAsString(sourceFile));
 						session.submitRequest(query);
 					} catch (IOException e) {
 						System.out.println("Failed to read pipeline file "
 								.concat(f));
 						throw Throwables.propagate(e);
 					} catch (RequestException e) {
 						System.out.println("Failed to insert pipeline file "
 								.concat(f));
 						throw Throwables.propagate(e);
 					}
 				}
 			}
 		} finally {
 			for (Map.Entry<String, Session> e : sessions.entrySet()) {
 				e.getValue().close();
 			}
 			sessions = new HashMap<String, Session>();
 		}
 	}
 
 	private void installResources(List<ResourceFileSet> resources) {
 		try {
             String applicationName = getEnvironment().getApplicationName();
 			for (ResourceFileSet resource : resources) {
 				final String targetDatabase =
                         applicationName != null ? applicationName + "-" + resource.getDatabase() : resource.getDatabase();
 				System.out
 						.println(" -- ".concat(targetDatabase).concat(" -- "));
 
 				/*
 				 * Get connection to database for uploading content
 				 */
 				Session session = getSession(targetDatabase);
 
 				ContentCreateOptions options;
 				options = new ContentCreateOptions();
 
 				String[] collections = resource.getCollections();
 				if (collections != null && collections.length > 0) {
 					options.setCollections(collections);
 				}
 
 				Permission[] permissions = resource.getPermissions();
 				if (permissions != null && permissions.length > 0) {
 					ContentPermission[] contentPermissions = new ContentPermission[permissions.length];
 					int i = 0;
 					for (Permission permission : permissions) {
 						ContentPermission contentPermission = new ContentPermission(
 								permission.getCapability(),
 								permission.getRole());
 						contentPermissions[i++] = contentPermission;
 					}
 					options.setPermissions(contentPermissions);
 				}
 
 				options.setFormat(resource.getFormat());
 
 				Iterator it = resource.iterator();
 				while (it.hasNext()) {
 					FileResource fileResource = (FileResource) it.next();
 					String f = fileResource.getName();
 					File sourceFile = new File(resource.getDir(), f);
 					File destinationFile = new File(
 							resource.getOutputDirectory(), f);
 
 					String destinationPath = destinationFile.getPath().replace(
 							File.separatorChar, '/');
 					System.out.println(String.format("Deploying %s to %s",
 							sourceFile.getPath(), destinationPath));
					Content c = null;
 					try {
						c = newContent(destinationPath,
								sourceFile, options);
 						session.insertContent(c);
 					} catch (RequestException e) {
 						throw new BuildException(
 								"Failed to insert content file ".concat(f), e);
					} finally {
					    if(c != null)
					        c.close();
 					}
 				}
 			}
 		} finally {
 			for (Map.Entry<String, Session> e : sessions.entrySet()) {
 				e.getValue().close();
 			}
 			sessions = new HashMap<String, Session>();
 		}
 	}
 
 	// /**
 	// * Invoke server modules.
 	// * <p/>
 	// * TODO: Create configuration object instead of using PlexusConfiguration
 	// - ModuleExecution perhaps.
 	// */
 	// protected void invokeModules() {
 	// PlexusConfiguration invokes = getEnvironment().getModuleInvokes();
 	//
 	// if (invokes == null || invokes.getChildCount() == 0) {
 	// return;
 	// }
 	//
 	// String appName = getEnvironment().getApplicationName();
 	//
 	// for (PlexusConfiguration config : invokes.getChildren()) {
 	// try {
 	// String modulePath = config.getChild("module").getValue();
 	// String serverName = config.getChild("server").getValue();
 	//
 	// PlexusConfiguration server = getServer(serverName);
 	//
 	// String database = appName + "-" + server.getAttribute("database");
 	// int port = Integer.parseInt(server.getAttribute("port"));
 	//
 	// System.out.println("-------------------------------------------------------------------- ");
 	// System.out.println("Invoking module on " + database);
 	// System.out.println("-------------------------------------------------------------------- ");
 	// System.out.println("Connecting to : " + host + " : " + port);
 	// System.out.println("  Module Path : " + modulePath);
 	//
 	// Session session = getXccSession(database, port);
 	// session.submitRequest(session.newModuleInvoke(modulePath));
 	// } catch (PlexusConfigurationException e) {
 	// getLog().error(e);
 	// } catch (RequestException e) {
 	// getLog().error(e);
 	// }
 	// }
 	// }
 
 }
