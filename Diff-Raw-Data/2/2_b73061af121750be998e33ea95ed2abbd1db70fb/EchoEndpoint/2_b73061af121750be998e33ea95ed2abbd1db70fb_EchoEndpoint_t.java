 package org.kalibro.service;
 
 import javax.jws.WebMethod;
 import javax.jws.WebParam;
 import javax.jws.WebResult;
 import javax.jws.WebService;
 
 import org.kalibro.core.model.*;
 import org.kalibro.core.model.enums.Granularity;
 import org.kalibro.service.entities.*;
 
 @WebService
 public class EchoEndpoint {
 
 	@WebMethod
 	@WebResult(name = "baseTool")
 	public BaseToolXml echoBaseTool(@WebParam(name = "baseTool") BaseToolXml baseTool) {
 		BaseTool entity = baseTool.convert();
 		entity.setName("echo " + entity.getName());
 		return new BaseToolXml(entity);
 	}
 
 	@WebMethod
 	@WebResult(name = "configuration")
 	public ConfigurationXml echoConfiguration(@WebParam(name = "configuration") ConfigurationXml configuration) {
 		Configuration entity = configuration.convert();
 		entity.setName("echo " + entity.getName());
 		return new ConfigurationXml(entity);
 	}
 
 	@WebMethod
 	@WebResult(name = "metricConfiguration")
 	public MetricConfigurationXml echoMetricConfiguration(
 		@WebParam(name = "metricConfiguration") MetricConfigurationXml metricConfiguration) {
 		MetricConfiguration entity = metricConfiguration.convert();
 		entity.setCode("echo_" + entity.getCode());
 		return new MetricConfigurationXml(entity);
 	}
 
 	@WebMethod
 	@WebResult(name = "moduleResult")
 	public ModuleResultXml echoModuleResult(@WebParam(name = "moduleResult") ModuleResultXml moduleResult) {
 		ModuleResult entity = moduleResult.convert();
 		Module module = entity.getModule();
 		module.setName("echo." + module.getName());
 		return new ModuleResultXml(entity);
 	}
 
 	@WebMethod
 	@WebResult(name = "project")
 	public ProjectXml echoProject(@WebParam(name = "project") ProjectXml project) {
 		Project entity = project.convert();
 		entity.setName("echo " + entity.getName());
 		return new ProjectXml(entity);
 	}
 
 	@WebMethod
 	@WebResult(name = "projectResult")
 	public ProjectResultXml echoProjectResult(@WebParam(name = "projectResult") ProjectResultXml projectResult) {
 		ProjectResult entity = projectResult.convert();
 		Project project = entity.getProject();
 		project.setName("echo " + project.getName());
 		return new ProjectResultXml(entity);
 	}
 
 	@WebMethod
 	@WebResult(name = "project")
 	public RawProjectXml echoRawProject(@WebParam(name = "project") RawProjectXml project) {
 		Project entity = project.convert();
 		entity.setName("echo " + entity.getName());
 		return new RawProjectXml(entity);
 	}
 
 	@WebMethod
 	@WebResult(name = "parentGranularity")
	public Granularity inferParentGranularity(@WebParam(name = "granularity") Granularity granularity) {
 		return granularity.inferParentGranularity();
 	}
 }
