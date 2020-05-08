 package org.zend.webapi.test.server;
 
 import java.io.ByteArrayInputStream;
 import java.io.StringWriter;
 
 import javax.xml.transform.Result;
 import javax.xml.transform.Source;
 import javax.xml.transform.Transformer;
 import javax.xml.transform.TransformerConfigurationException;
 import javax.xml.transform.TransformerException;
 import javax.xml.transform.TransformerFactory;
 import javax.xml.transform.dom.DOMSource;
 
 import junit.framework.Assert;
 
 import org.restlet.Application;
 import org.restlet.Request;
 import org.restlet.Response;
 import org.restlet.Restlet;
 import org.restlet.data.Disposition;
 import org.restlet.data.MediaType;
 import org.restlet.ext.xml.DomRepresentation;
 import org.restlet.representation.InputRepresentation;
 import org.restlet.representation.Representation;
 import org.restlet.routing.Router;
 import org.w3c.dom.Document;
 import org.zend.webapi.test.server.response.FileResponse;
 import org.zend.webapi.test.server.response.ServerResponse;
 import org.zend.webapi.test.server.response.ServiceResponse;
 
 public class ServerApplication extends Application {
 
 	@Override
 	public synchronized Restlet createRoot() {
 		Router router = new Router(getContext());
 
 		Restlet getSystemInfo = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				ServerResponse serverResponse = ZendSystem.getInstance()
 						.getSystemInfo();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		Restlet clusterGetServerStatus = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				ServerResponse serverResponse = ZendSystem.getInstance()
 						.clusterGetServerStatus();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		Restlet clusterAddServer = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				String postParams = request.getEntityAsText();
 				Assert.assertNotNull(postParams);
 				ServerResponse serverResponse = ZendSystem.getInstance()
 						.clusterAddServer();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		Restlet clusterRemoveServer = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				String postParams = request.getEntityAsText();
 				Assert.assertNotNull(postParams);
 				ServerResponse serverResponse = ZendSystem.getInstance()
 						.clusterRemoveServer();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		Restlet clusterDisableServer = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				String postParams = request.getEntityAsText();
 				Assert.assertNotNull(postParams);
 				ServerResponse serverResponse = ZendSystem.getInstance()
 						.clusterDisableServer();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		Restlet clusterEnableServer = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				String postParams = request.getEntityAsText();
 				Assert.assertNotNull(postParams);
 				ServerResponse serverResponse = ZendSystem.getInstance()
 						.clusterEnableServer();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		Restlet restartPhp = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				ServerResponse serverResponse = ZendSystem.getInstance()
 						.restartPhp();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		Restlet configurationExport = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				FileResponse serverResponse = (FileResponse) ZendSystem
 						.getInstance().configurationExport();
 				InputRepresentation representation = new InputRepresentation(
 						new ByteArrayInputStream(serverResponse.getContent()),
 						MediaType.valueOf("application/vnd.zend.serverconfig"));
 				Disposition disposition = new Disposition(
 						Disposition.TYPE_ATTACHMENT);
 				disposition.setFilename(serverResponse.getFileName());
 				representation.setDisposition(disposition);
 				representation.setSize(serverResponse.getFileSize());
 				response.setEntity(representation);
 				response.setStatus(serverResponse.getStatus());
 			}
 		};
 
 		Restlet configurationImport = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				final ServerResponse serverResponse = ZendSystem.getInstance()
 						.configurationImport();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		Restlet applicationGetStatus = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				ServerResponse serverResponse = ZendSystem.getInstance()
 						.applicationGetStatus();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		Restlet applicationDeploy = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				ServerResponse serverResponse = ZendSystem.getInstance()
 						.applicationDeploy();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		Restlet applicationUpdate = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				ServerResponse serverResponse = ZendSystem.getInstance()
 						.applicationUpdate();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		Restlet applicationRemove = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				ServerResponse serverResponse = ZendSystem.getInstance()
 						.applicationRemove();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		Restlet applicationRedeploy = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				ServerResponse serverResponse = ZendSystem.getInstance()
 						.applicationRedeploy();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		Restlet clusterReconfigureServer = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				ServerResponse serverResponse = ZendSystem.getInstance()
 						.clusterReconfigureServer();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		Restlet applicationRollback = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				ServerResponse serverResponse = ZendSystem.getInstance()
 						.applicationRollback();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		Restlet codeTracingDisable = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				ServerResponse serverResponse = ZendSystem.getInstance()
 						.codeTracingDisable();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		Restlet codeTracingEnable = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				ServerResponse serverResponse = ZendSystem.getInstance()
 						.codeTracingEnable();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		Restlet codeTracingIsEnabled = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				ServerResponse serverResponse = ZendSystem.getInstance()
 						.codeTracingIsEnabled();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		Restlet codeTracingCreate = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				ServerResponse serverResponse = ZendSystem.getInstance()
 						.codeTracingCreate();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		Restlet codeTracingList = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				ServerResponse serverResponse = ZendSystem.getInstance()
 						.codeTracingList();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		Restlet codeTracingDelete = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				ServerResponse serverResponse = ZendSystem.getInstance()
 						.codeTracingDelete();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		Restlet codetracingDownloadTraceFile = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				FileResponse serverResponse = (FileResponse) ZendSystem
 						.getInstance().codetracingDownloadTraceFile();
 				InputRepresentation representation = new InputRepresentation(
 						new ByteArrayInputStream(serverResponse.getContent()),
 						MediaType.valueOf("application/vnd.zend.serverconfig"));
 				Disposition disposition = new Disposition(
 						Disposition.TYPE_ATTACHMENT);
 				disposition.setFilename(serverResponse.getFileName());
 				representation.setDisposition(disposition);
 				representation.setSize(serverResponse.getFileSize());
 				response.setEntity(representation);
 				response.setStatus(serverResponse.getStatus());
 			}
 		};
 
 		Restlet monitorGetRequestSummary = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				ServerResponse serverResponse = ZendSystem.getInstance()
 						.monitorGetRequestSummary();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		Restlet monitorGetIssuesListPredefinedFilter = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				ServerResponse serverResponse = ZendSystem.getInstance()
 						.monitorGetIssuesListByPredefinedFilter();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		Restlet monitorGetIssueDetails = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				ServerResponse serverResponse = ZendSystem.getInstance()
 						.monitorGetIssueDetails();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		Restlet monitorGetEventGroupDetails = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				ServerResponse serverResponse = ZendSystem.getInstance()
 						.monitorGetEventGroupDetails();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		Restlet monitorExportIssueByEventsGroup = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				FileResponse serverResponse = (FileResponse) ZendSystem
 						.getInstance().monitorExportIssueByEventsGroup();
 				InputRepresentation representation = new InputRepresentation(
 						new ByteArrayInputStream(serverResponse.getContent()),
 						MediaType.valueOf("application/vnd.zend.eventexport"));
 				Disposition disposition = new Disposition(
 						Disposition.TYPE_ATTACHMENT);
 				disposition.setFilename(serverResponse.getFileName());
 				representation.setDisposition(disposition);
 				representation.setSize(serverResponse.getFileSize());
 				response.setEntity(representation);
 				response.setStatus(serverResponse.getStatus());
 			}
 		};
 
 		Restlet monitorChangeIssueStatus = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				ServerResponse serverResponse = ZendSystem.getInstance()
 						.monitorChangeIssueStatus();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		Restlet studioStartDebug = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				ServerResponse serverResponse = ZendSystem.getInstance()
 						.studioStartDebug();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		Restlet studioStartProfile = new Restlet() {
 			@Override
 			public void handle(Request request, Response response) {
 				ServerResponse serverResponse = ZendSystem.getInstance()
 						.studioStartProfile();
 				prepareResponse(response, serverResponse);
 			}
 		};
 
 		router.attach("/ZendServerManager/Api/getSystemInfo", getSystemInfo);
 		router.attach("/ZendServerManager/Api/clusterGetServerStatus",
 				clusterGetServerStatus);
 		router.attach("/ZendServerManager/Api/clusterAddServer",
 				clusterAddServer);
 		router.attach("/ZendServerManager/Api/clusterRemoveServer",
 				clusterRemoveServer);
 		router.attach("/ZendServerManager/Api/clusterDisableServer",
 				clusterDisableServer);
 		router.attach("/ZendServerManager/Api/clusterEnableServer",
 				clusterEnableServer);
 		router.attach("/ZendServerManager/Api/clusterReconfigureServer",
 				clusterReconfigureServer);
 		router.attach("/ZendServerManager/Api/restartPhp", restartPhp);
 		router.attach("/ZendServerManager/Api/configurationExport",
 				configurationExport);
 		router.attach("/ZendServerManager/Api/configurationImport",
 				configurationImport);
 		router.attach("/ZendServerManager/Api/applicationGetStatus",
 				applicationGetStatus);
 		router.attach("/ZendServerManager/Api/applicationDeploy",
 				applicationDeploy);
 		router.attach("/ZendServerManager/Api/applicationUpdate",
 				applicationUpdate);
 		router.attach("/ZendServerManager/Api/applicationRemove",
 				applicationRemove);
 		router.attach("/ZendServerManager/Api/applicationSynchronize",
 				applicationRedeploy);
 		router.attach("/ZendServerManager/Api/applicationRollback",
 				applicationRollback);
 		router.attach("/ZendServerManager/Api/codetracingDisable",
 				codeTracingDisable);
 		router.attach("/ZendServerManager/Api/codetracingEnable",
 				codeTracingEnable);
 		router.attach("/ZendServerManager/Api/codetracingIsEnabled",
 				codeTracingIsEnabled);
 		router.attach("/ZendServerManager/Api/codetracingCreate",
 				codeTracingCreate);
 		router.attach("/ZendServerManager/Api/codetracingDelete",
 				codeTracingDelete);
 		router.attach("/ZendServerManager/Api/codetracingList", codeTracingList);
 		router.attach("/ZendServerManager/Api/codetracingDownloadTraceFile",
 				codetracingDownloadTraceFile);
 		router.attach("/ZendServerManager/Api/monitorGetRequestSummary",
 				monitorGetRequestSummary);
 		router.attach(
 				"/ZendServerManager/Api/monitorGetIssuesListPredefinedFilter",
 				monitorGetIssuesListPredefinedFilter);
 		router.attach("/ZendServerManager/Api/monitorGetIssueDetails",
 				monitorGetIssueDetails);
 		router.attach("/ZendServerManager/Api/monitorGetEventGroupDetails",
 				monitorGetEventGroupDetails);
 		router.attach("/ZendServerManager/Api/monitorExportIssueByEventsGroup",
 				monitorExportIssueByEventsGroup);
 		router.attach("/ZendServerManager/Api/monitorChangeIssueStatus",
 				monitorChangeIssueStatus);
 		router.attach("/ZendServerManager/Api/studioStartDebug",
 				studioStartDebug);
 		router.attach("/ZendServerManager/Api/studioStartProfile",
 				studioStartProfile);
 		return router;
 	}
 
 	private void prepareResponse(Response response,
 			ServerResponse serverResponse) {
 		Document doc = ((ServiceResponse) serverResponse).getData();
 		Representation representation = new DomRepresentation(
				MediaType.register("application/vnd.zend.serverapi",
						"version=1.1"), doc);
 
 		TransformerFactory factory = TransformerFactory.newInstance();
 		try {
 			Transformer xform = factory.newTransformer();
 			Source src = new DOMSource(doc);
 			StringWriter writer = new StringWriter();
 			Result result = new javax.xml.transform.stream.StreamResult(writer);
 			xform.transform(src, result);
 			representation.setSize(writer.toString().length());
 		} catch (TransformerConfigurationException e) {
 			// ignore
 		} catch (TransformerException e) {
 			// ignore
 		}
 		response.setEntity(representation);
 		response.setStatus(serverResponse.getStatus());
 	}
 }
