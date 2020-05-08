 /**
  * Copyright 2011-2012 Universite Joseph Fourier, LIG, ADELE team
  *   Licensed under the Apache License, Version 2.0 (the "License");
  *   you may not use this file except in compliance with the License.
  *   You may obtain a copy of the License at
  *
  *     http://www.apache.org/licenses/LICENSE-2.0
  *
  *   Unless required by applicable law or agreed to in writing, software
  *   distributed under the License is distributed on an "AS IS" BASIS,
  *   WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  *   See the License for the specific language governing permissions and
  *   limitations under the License.
  */
 package fr.imag.adele.apam.test.testcases;
 
 import static org.ops4j.pax.exam.CoreOptions.junitBundles;
 import static org.ops4j.pax.exam.CoreOptions.mavenBundle;
 import static org.ops4j.pax.exam.CoreOptions.vmOption;
 
 import java.io.EOFException;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import javax.inject.Inject;
 
 import org.apache.cxf.frontend.ClientProxyFactoryBean;
 import org.junit.Assert;
import org.junit.Ignore;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.ops4j.pax.exam.Option;
 import org.ops4j.pax.exam.junit.PaxExam;
 import org.ops4j.pax.exam.spi.reactors.ExamReactorStrategy;
 import org.ops4j.pax.exam.spi.reactors.PerMethod;
 import org.ops4j.pax.exam.util.Filter;
 import org.osgi.framework.BundleContext;
 
 import fr.imag.adele.apam.Implementation;
 import fr.imag.adele.apam.Instance;
 import fr.imag.adele.apam.distriman.DistrimanIface;
 import fr.imag.adele.apam.pax.distriman.test.iface.P2Spec;
 import fr.imag.adele.apam.test.support.distriman.DistrimanUtil;
 import fr.imag.adele.apam.tests.helpers.ExtensionAbstract;
 
@Ignore
 @RunWith(PaxExam.class)
 @ExamReactorStrategy(PerMethod.class)
 public class DistriManTest extends ExtensionAbstract {
 
     @Inject
     private BundleContext bc;
 
     @Inject
     @Filter(timeout = 60000)
     private DistrimanIface distriman;
 
     String clienturl = "http://127.0.0.1:8280";
     String serverurl = "http://127.0.0.1:8280/apam/machine";
 
     // CoreOptions.systemProperty("org.osgi.framework.system.packages.extra").value("org.ops4j.pax.url.mvn");
     // CoreOptions.frameworkProperty("org.osgi.framework.system.packages.extra").value("org.ops4j.pax.url.mvn");
     @Override
     public List<Option> config() {
 	List<Option> config = new ArrayList<Option>();// super.config();
 
 	config.add(packInitialConfig());
 	config.add(packOSGi());
 	config.add(packPax());
 	config.add(packApamCore());
 	config.add(packApamObrMan());
 	config.add(packApamShell());
 	config.add(packLog());
 	config.add(junitBundles());
 	config.add(packDebugConfiguration());
 	config.add(vmOption("-ea"));
 	config.add(packApamDistriMan());
 
 	config.add(mavenBundle().groupId("fr.imag.adele.apam.tests.services")
 		.artifactId("apam-pax-distriman-iface").versionAsInProject());
 	config.add(mavenBundle().groupId("fr.imag.adele.apam.tests.services")
 		.artifactId("apam-pax-distriman-P2").versionAsInProject());
 
 	return config;
 
     }
 
     @Test
     public void ProviderDependencyConstraintRespected_tc096()
 	    throws MalformedURLException, IOException {
 
 	boolean validInstanceAvailable = false;
 
 	final String constraint = "(rule=one)";
 
 	final String jsonPayload = DistrimanUtil.httpRequestDependency("p2",
 		"specification", "P2-spec-constraint", "P2", false, clienturl,
 		new ArrayList<String>() {
 		    {
 			add(constraint);
 		    }
 		}, new ArrayList<String>());
 
 	Map<String, String> parameters = new HashMap<String, String>() {
 	    {
 		put("content", jsonPayload);
 	    }
 	};
 
 	try {
 
 	    Implementation p1Impl = waitForImplByName(null, "P2-constraint2");
 
 	    Instance p1Inst = p1Impl.createInstance(null, null);
 
 	    DistrimanUtil.curl(parameters, serverurl);
 
 	    // An exception should be raised since there is no instance that can
 	    // meet the constraints
 
 	} catch (EOFException e) {
 	    validInstanceAvailable = false;
 	}
 
 	Assert.assertTrue(
 		"A remote instance that do not respect the constraint was injected",
 		!validInstanceAvailable);
 
 	try {
 
 	    Implementation p1Impl = waitForImplByName(null, "P2-constraint");
 
 	    Instance p1Inst = p1Impl.createInstance(null, null);
 
 	    String response = DistrimanUtil.curl(parameters, serverurl);
 
 	    Map<String, String> properties = DistrimanUtil
 		    .propertyGet(response);
 
 	    Map<String, String> endpoints = DistrimanUtil.endpointGet(response);
 
 	    DistrimanUtil.endpointConnect(endpoints);
 
 	    Assert.assertTrue(
 		    String.format(
 			    "remote object do not respect the instance constraints specified <%s> instead the value for rule was %s.",
 			    constraint, properties.get("rule")), properties
 			    .get("rule").equals("one"));
 
 	} catch (EOFException e) {
 	    e.printStackTrace();
 	    Assert.fail("inespected exception while injecting the remote field, with the message:"
 		    + e.getMessage());
 	}
 
     }
 
     @Test
     public void ProviderDependencyInterface_tc086()
 	    throws MalformedURLException, IOException {
 
 	Implementation p2Impl = waitForImplByName(null, "P2-singleinterface");
 
 	Instance p2Inst = p2Impl.createInstance(null, null);
 
 	final String jsonPayload = DistrimanUtil.httpRequestDependency("p2",
 		"specification", "P2-spec-singleinterface",
 		"P2-singleinterface", false, clienturl);
 
 	Map<String, String> parameters = new HashMap<String, String>() {
 	    {
 		put("content", jsonPayload);
 	    }
 	};
 
 	String response = DistrimanUtil.curl(parameters, serverurl);
 
 	System.err.println("<" + response + ">");
 
 	Map<String, String> endpoints = DistrimanUtil.endpointGet(response);
 
 	System.out.println("Class\tURL");
 	for (Map.Entry<String, String> entry : endpoints.entrySet()) {
 	    System.out.println(String.format("%s\t%s", entry.getKey(),
 		    entry.getValue()));
 	}
 
 	Assert.assertTrue(
 		String.format(
 			"distriman(provider host) did not create an endpoint, or not the right number of endpoints. Expected 1 but %s were provided",
 			endpoints.size()), endpoints.size() == 1);
 
 	try {
 
 	    ClientProxyFactoryBean factory = new ClientProxyFactoryBean();
 	    factory.setServiceClass(P2Spec.class);
 	    factory.setAddress(endpoints
 		    .get("fr.imag.adele.apam.pax.distriman.test.iface.P2Spec"));
 	    P2Spec proxy = (P2Spec) factory.create();
 	    System.err.println(proxy.getName());
 
 	} catch (Exception e) {
 	    Assert.fail(String
 		    .format("distriman(provider host) created an endpoint but was not possible to connect to it, failed with the message %s",
 			    e.getMessage()));
 	}
 
     }
 
     @Test
     public void ProviderDependencySpecificationMultipleInterface_tc087()
 	    throws MalformedURLException, IOException {
 
 	Implementation p2Impl = waitForImplByName(null, "P2-multipleinterface");
 
 	Instance p2Inst = p2Impl.createInstance(null, null);
 
 	final String jsonPayload = DistrimanUtil.httpRequestDependency("p2",
 		"specification", "P2-spec-multipleinterface", "P2", false,
 		clienturl);
 
 	Map<String, String> parameters = new HashMap<String, String>() {
 	    {
 		put("content", jsonPayload);
 	    }
 	};
 
 	String response = DistrimanUtil.curl(parameters, serverurl);
 
 	System.err.println(response);
 
 	Map<String, String> endpoints = DistrimanUtil.endpointGet(response);
 
 	System.err.println("Class\tURL");
 	for (Map.Entry<String, String> entry : endpoints.entrySet()) {
 	    System.err.println(String.format("%s\t%s", entry.getKey(),
 		    entry.getValue()));
 	}
 
 	Assert.assertTrue(
 		String.format(
 			"distriman(provider host) did not create an endpoint, or not the right number of endpoints. Expected 2 but %s were provided",
 			endpoints.size()), endpoints.size() == 2);
 
 	DistrimanUtil.endpointConnect(endpoints);
 
     }
 
     @Test
     public void ProviderDependencySpecificationSingleInterface_tc088()
 	    throws MalformedURLException, IOException {
 
 	Implementation p2Impl = waitForImplByName(null, "P2-singleinterface");
 
 	Instance p2Inst = p2Impl.createInstance(null, null);
 
 	final String jsonPayload = DistrimanUtil.httpRequestDependency("p2",
 		"specification", "P2-spec-singleinterface", "P2", false,
 		clienturl);
 
 	Map<String, String> parameters = new HashMap<String, String>() {
 	    {
 		put("content", jsonPayload);
 	    }
 	};
 
 	String response = DistrimanUtil.curl(parameters, serverurl);
 
 	System.err.println(response);
 
 	Map<String, String> endpoints = DistrimanUtil.endpointGet(response);
 
 	System.err.println("Class\tURL");
 	for (Map.Entry<String, String> entry : endpoints.entrySet()) {
 	    System.err.println(String.format("%s\t%s", entry.getKey(),
 		    entry.getValue()));
 	}
 
 	Assert.assertTrue(
 		String.format(
 			"distriman(provider host) did not create an endpoint, or not the right number of endpoints. Expected 1 but %s were provided",
 			endpoints.size()), endpoints.size() == 1);
 
 	DistrimanUtil.endpointConnect(endpoints);
 
     }
 
 }
