 //
 // typica - A client library for Amazon Web Services
 // Copyright (C) 2007,2008,2009 Xerox Corporation
 // 
 // Licensed under the Apache License, Version 2.0 (the "License");
 // you may not use this file except in compliance with the License.
 // You may obtain a copy of the License at
 //
 //     http://www.apache.org/licenses/LICENSE-2.0
 //
 // Unless required by applicable law or agreed to in writing, software
 // distributed under the License is distributed on an "AS IS" BASIS,
 // WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 // See the License for the specific language governing permissions and
 // limitations under the License.
 //
 
 package com.xerox.amazonws.ec2;
 
 import java.io.BufferedReader;
 import java.io.ByteArrayInputStream;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.io.OutputStream;
 import java.io.StringWriter;
 import java.net.MalformedURLException;
 import java.net.URL;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.HashMap;
 import java.util.Iterator;
 import java.util.List;
 import java.util.Map;
 
 import javax.xml.bind.JAXBException;
 
 import org.apache.commons.codec.binary.Base64;
 import org.apache.commons.httpclient.HttpException;
 import org.apache.commons.httpclient.HttpMethodBase;
 import org.apache.commons.httpclient.methods.GetMethod;
 import org.apache.commons.httpclient.methods.PostMethod;
 import org.apache.commons.httpclient.methods.StringRequestEntity;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.xerox.amazonws.common.AWSException;
 import com.xerox.amazonws.common.AWSQueryConnection;
 import com.xerox.amazonws.typica.loadbalance.jaxb.EnableAvailabilityZonesForLoadBalancerResponse;
 import com.xerox.amazonws.typica.loadbalance.jaxb.ConfigureHealthCheckResponse;
 import com.xerox.amazonws.typica.loadbalance.jaxb.CreateLoadBalancerResponse;
 import com.xerox.amazonws.typica.loadbalance.jaxb.DeleteLoadBalancerResponse;
 import com.xerox.amazonws.typica.loadbalance.jaxb.DeregisterInstancesFromLoadBalancerResponse;
 import com.xerox.amazonws.typica.loadbalance.jaxb.DescribeLoadBalancersResponse;
 import com.xerox.amazonws.typica.loadbalance.jaxb.DescribeInstanceHealthResponse;
 import com.xerox.amazonws.typica.loadbalance.jaxb.RegisterInstancesWithLoadBalancerResponse;
 import com.xerox.amazonws.typica.loadbalance.jaxb.DisableAvailabilityZonesForLoadBalancerResponse;
 //import com.xerox.amazonws.typica.loadbalance.jaxb.;
 
 /**
  * A Java wrapper for the EC2 web services API
  */
 public class LoadBalancing extends AWSQueryConnection {
 
     private static Log logger = LogFactory.getLog(LoadBalancing.class);
 
 	/**
 	 * Initializes the ec2 service with your AWS login information.
 	 *
      * @param awsAccessId The your user key into AWS
      * @param awsSecretKey The secret string used to generate signatures for authentication.
 	 */
     public LoadBalancing(String awsAccessId, String awsSecretKey) {
         this(awsAccessId, awsSecretKey, true);
     }
 
 	/**
 	 * Initializes the ec2 service with your AWS login information.
 	 *
      * @param awsAccessId The your user key into AWS
      * @param awsSecretKey The secret string used to generate signatures for authentication.
      * @param isSecure True if the data should be encrypted on the wire on the way to or from EC2.
 	 */
     public LoadBalancing(String awsAccessId, String awsSecretKey, boolean isSecure) {
        this(awsAccessId, awsSecretKey, isSecure, "lbs.amazonaws.com");
     }
 
 	/**
 	 * Initializes the ec2 service with your AWS login information.
 	 *
      * @param awsAccessId The your user key into AWS
      * @param awsSecretKey The secret string used to generate signatures for authentication.
      * @param isSecure True if the data should be encrypted on the wire on the way to or from EC2.
      * @param server Which host to connect to.  Usually, this will be lbs.amazonaws.com
 	 */
     public LoadBalancing(String awsAccessId, String awsSecretKey, boolean isSecure,
                              String server)
     {
         this(awsAccessId, awsSecretKey, isSecure, server,
              isSecure ? 443 : 80);
     }
 
     /**
 	 * Initializes the ec2 service with your AWS login information.
 	 *
      * @param awsAccessId The your user key into AWS
      * @param awsSecretKey The secret string used to generate signatures for authentication.
      * @param isSecure True if the data should be encrypted on the wire on the way to or from EC2.
      * @param server Which host to connect to.  Usually, this will be lbs.amazonaws.com
      * @param port Which port to use.
      */
     public LoadBalancing(String awsAccessId, String awsSecretKey, boolean isSecure,
                              String server, int port)
     {
 		super(awsAccessId, awsSecretKey, isSecure, server, port);
 		ArrayList<String> vals = new ArrayList<String>();
 		vals.add("2009-05-15");
 		super.headers.put("Version", vals);
     }
 
 	/**
 	 * Add availability zones.
 	 * 
 	 * @param loadBalancerName the name of the load balancer
 	 * @param availabilityZones a list of availability zones to add
 	 * @throws LoadBalancingException wraps checked exceptions
 	 */
 	public List<String> enableAvailabilityZonesForLoadBalancer(String loadBalancerName,
 								List<String> availabilityZones) throws LoadBalancingException {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("LoadBalancerName", loadBalancerName);
 		int i=0;
 		for (String zone : availabilityZones) {
 			params.put("AvailabilityZones.member."+(i+1), zone);
 			i++;
 		}
 		GetMethod method = new GetMethod();
 		try {
 			EnableAvailabilityZonesForLoadBalancerResponse response =
 					makeRequestInt(method, "EnableAvailabilityZonesForLoadBalancer", params, EnableAvailabilityZonesForLoadBalancerResponse.class);
 			return response.getEnableAvailabilityZonesForLoadBalancerResult().getAvailabilityZones().getMembers();
 		} finally {
 			method.releaseConnection();
 		}
 	}
 
 	/**
 	 * Create load balancer.
 	 * 
 	 * @param loadBalancerName the name of the load balancer
 	 * @param listeners the definition of protocol and ports
 	 * @param availabilityZones a list of availability zones
 	 * @return dns the DNS name for the load balancer
 	 * @throws LoadBalancingException wraps checked exceptions
 	 */
 	public String createLoadBalancer(String loadBalancerName, List<Listener> listeners,
 								List<String> availabilityZones) throws LoadBalancingException {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("LoadBalancerName", loadBalancerName);
 		int i=1;
 		for (Listener l : listeners) {
 			params.put("Listeners.member."+i+".Protocol", l.getProtocol());
 			params.put("Listeners.member."+i+".LoadBalancerPort", ""+l.getLoadBalancerPort());
 			params.put("Listeners.member."+i+".InstancePort", ""+l.getInstancePort());
 		}
 		i=1;
 		for (String zone : availabilityZones) {
 			params.put("AvailabilityZones.member."+i, zone);
 			i++;
 		}
 		GetMethod method = new GetMethod();
 		try {
 			CreateLoadBalancerResponse response =
 					makeRequestInt(method, "CreateLoadBalancer", params, CreateLoadBalancerResponse.class);
 			return response.getCreateLoadBalancerResult().getDNSName();
 		} finally {
 			method.releaseConnection();
 		}
 	}
 
 	/**
 	 * Configure health check.
 	 * 
 	 * @param loadBalancerName the name of the load balancer
 	 * @param healthCheck the details of the healthcheck
 	 * @throws LoadBalancingException wraps checked exceptions
 	 */
 	public HealthCheck configureHealthCheck(String loadBalancerName, HealthCheck healthCheck) throws LoadBalancingException {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("LoadBalancerName", loadBalancerName);
 		params.put("HealthCheck.Target", ""+healthCheck.getTarget());
 		params.put("HealthCheck.Interval", ""+healthCheck.getInterval());
 		params.put("HealthCheck.Timeout", ""+healthCheck.getTimeout());
 		params.put("HealthCheck.UnhealthyThreshold", ""+healthCheck.getUnhealthyThreshold());
 		params.put("HealthCheck.HealthyThreshold", ""+healthCheck.getHealthyThreshold());
 		GetMethod method = new GetMethod();
 		try {
 			ConfigureHealthCheckResponse response =
 					makeRequestInt(method, "ConfigureHealthCheck", params, ConfigureHealthCheckResponse.class);
 			com.xerox.amazonws.typica.loadbalance.jaxb.HealthCheck hc =
 						response.getConfigureHealthCheckResult().getHealthCheck();
 			return new HealthCheck(hc.getTarget(), hc.getInterval().intValue(),
 							hc.getTimeout().intValue(),
 							hc.getUnhealthyThreshold().intValue(),
 							hc.getHealthyThreshold().intValue());
 		} finally {
 			method.releaseConnection();
 		}
 	}
 
 	/**
 	 * Delete load balancer.
 	 * 
 	 * @param loadBalancerName the name of the load balancer
 	 * @throws LoadBalancingException wraps checked exceptions
 	 */
 	public void deleteLoadBalancer(String loadBalancerName) throws LoadBalancingException {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("LoadBalancerName", loadBalancerName);
 		GetMethod method = new GetMethod();
 		try {
 //			DeleteLoadBalancerResponse response =
 					makeRequestInt(method, "DeleteLoadBalancer", params, DeleteLoadBalancerResponse.class);
 		} finally {
 			method.releaseConnection();
 		}
 	}
 
 	/**
 	 * Deregister instances from load balancer.
 	 * 
 	 * @param loadBalancerName the name of the load balancer
 	 * @param instances a list of instances to deregister from the load balancer
      * @return the updated list of instances registered with the load balancer
 	 * @throws LoadBalancingException wraps checked exceptions
 	 */
 	public List<String> deregisterInstancesFromLoadBalancer(String loadBalancerName,
 								List<String> instances) throws LoadBalancingException {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("LoadBalancerName", loadBalancerName);
 		int i=1;
 		for (String inst : instances) {
 			params.put("Instances.member."+i, inst);
 			i++;
 		}
 		GetMethod method = new GetMethod();
 		try {
 			DeregisterInstancesFromLoadBalancerResponse response =
 					makeRequestInt(method, "DeregisterInstancesFromLoadBalancer", params, DeregisterInstancesFromLoadBalancerResponse.class);
 			List<com.xerox.amazonws.typica.loadbalance.jaxb.Instance> instList = response.getDeregisterInstancesFromLoadBalancerResult().getInstances().getMembers();
 			List<String> ret = new ArrayList<String>();
 			for (com.xerox.amazonws.typica.loadbalance.jaxb.Instance inst : instList) {
 				ret.add(inst.getInstanceId());
 			}
 			return ret;
 		} finally {
 			method.releaseConnection();
 		}
 	}
 
 
 
 
 	/**
 	 * Describe all load balancers.
 	 *
 	 * @throws LoadBalancingException wraps checked exceptions
 	 */
 	public List<LoadBalancer> describeLoadBalancers() throws LoadBalancingException {
         return describeLoadBalancers(null);
     }
 
 
 	/**
 	 * Describe load balancers.
 	 * 
 	 * @param loadBalancerNames a list of load balancers to describe
 	 * @throws LoadBalancingException wraps checked exceptions
 	 */
 	public List<LoadBalancer> describeLoadBalancers(List<String> loadBalancerNames) throws LoadBalancingException {
 		Map<String, String> params = new HashMap<String, String>();
 		if (loadBalancerNames != null && loadBalancerNames.size() > 0) {
 			int i = 0;
 			for (String name : loadBalancerNames) {
 				params.put("LoadBalancerNames.member."+(i+1), name);
 				i++;
 			}
 		}
 		GetMethod method = new GetMethod();
 		try {
 			DescribeLoadBalancersResponse response =
 					makeRequestInt(method, "DescribeLoadBalancers", params, DescribeLoadBalancersResponse.class);
 			List<com.xerox.amazonws.typica.loadbalance.jaxb.LoadBalancerDescription> result =
 						response.getDescribeLoadBalancersResult().getLoadBalancerDescriptions().getMembers();
 			List<LoadBalancer> ret = new ArrayList<LoadBalancer>();
 			for (com.xerox.amazonws.typica.loadbalance.jaxb.LoadBalancerDescription lb : result) {
 				List<com.xerox.amazonws.typica.loadbalance.jaxb.Instance> instList = lb.getInstances().getMembers();
 				List<String> instances = new ArrayList<String>();
 				for (com.xerox.amazonws.typica.loadbalance.jaxb.Instance inst : instList) {
 					instances.add(inst.getInstanceId());
 				}
 				List<com.xerox.amazonws.typica.loadbalance.jaxb.Listener> listenerList = lb.getListeners().getMembers();
 				List<Listener> listeners = new ArrayList<Listener>();
 				for (com.xerox.amazonws.typica.loadbalance.jaxb.Listener listnr : listenerList) {
 					listeners.add(new Listener(listnr.getProtocol(),
 												listnr.getLoadBalancerPort().intValue(),
 												listnr.getInstancePort().intValue()));
 				}
 				com.xerox.amazonws.typica.loadbalance.jaxb.HealthCheck hc = lb.getHealthCheck();
 				HealthCheck healthCheck = new HealthCheck(hc.getTarget(),
 									hc.getInterval().intValue(),
 									hc.getTimeout().intValue(),
 									hc.getUnhealthyThreshold().intValue(),
 									hc.getHealthyThreshold().intValue());
 				LoadBalancer newPoint = new LoadBalancer(lb.getLoadBalancerName(),
 								lb.getDNSName(),
 								listeners,
 								lb.getAvailabilityZones().getMembers(),
 								instances, healthCheck,
 								lb.getCreatedTime().toGregorianCalendar());
 				ret.add(newPoint);
 			}
 			return ret;
 		} finally {
 			method.releaseConnection();
 		}
 	}
 
     /**
 	 * Describe the current state of the instances registered with the load balancer.
 	 *
 	 * @param loadBalancerName the name of the load balancer
 	 * @throws LoadBalancingException wraps checked exceptions
 	 */
 	public List<InstanceState> describeInstanceHealth(String loadBalancerName) throws LoadBalancingException {
         return describeInstanceHealth(loadBalancerName, null);
     }
 
 	/**
 	 * Describe the current state of the instances specified
 	 * 
 	 * @param loadBalancerName the name of the load balancer
 	 * @param instances a list of instances to describe (null for all)
 	 * @throws LoadBalancingException wraps checked exceptions
 	 */
 	public List<InstanceState> describeInstanceHealth(String loadBalancerName, List<String> instances) throws LoadBalancingException {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("LoadBalancerName", loadBalancerName);
 		if (instances != null && instances.size() > 0) {
 			int i = 0;
 			for (String name : instances) {
 				params.put("Instances.member."+(i+1), name);
 				i++;
 			}
 		}
 		GetMethod method = new GetMethod();
 		try {
 			DescribeInstanceHealthResponse response =
 					makeRequestInt(method, "DescribeInstanceHealth", params, DescribeInstanceHealthResponse.class);
 			List<com.xerox.amazonws.typica.loadbalance.jaxb.InstanceState> result =
 						response.getDescribeInstanceHealthResult().getInstanceStates().getMembers();
 			List<InstanceState> ret = new ArrayList<InstanceState>();
 			for (com.xerox.amazonws.typica.loadbalance.jaxb.InstanceState state : result) {
 				InstanceState newState = new InstanceState(state.getInstanceId(),
 								state.getState(),
 								state.getReasonCode(),
 								state.getDescription());
 				ret.add(newState);
 			}
 			return ret;
 		} finally {
 			method.releaseConnection();
 		}
 	}
 
 	/**
 	 * Register instance(s) with a load balancer.
 	 * 
 	 * @param loadBalancerName the name of the load balancer
 	 * @param instances a list of instance IDs registered with the load balancer
 	 * @throws LoadBalancingException wraps checked exceptions
 	 */
 	public List<String> registerInstancesWithLoadBalancer(String loadBalancerName,
 								List<String> instances) throws LoadBalancingException {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("LoadBalancerName", loadBalancerName);
 		int i=1;
 		for (String inst : instances) {
 			params.put("Instances.member."+i, inst);
 			i++;
 		}
 		GetMethod method = new GetMethod();
 		try {
 			RegisterInstancesWithLoadBalancerResponse response =
 					makeRequestInt(method, "RegisterInstancesWithLoadBalancer", params, RegisterInstancesWithLoadBalancerResponse.class);
 			List<com.xerox.amazonws.typica.loadbalance.jaxb.Instance> instList = response.getRegisterInstancesWithLoadBalancerResult().getInstances().getMembers();
 			List<String> ret = new ArrayList<String>();
 			for (com.xerox.amazonws.typica.loadbalance.jaxb.Instance inst : instList) {
 				ret.add(inst.getInstanceId());
 			}
 			return ret;
 		} finally {
 			method.releaseConnection();
 		}
 	}
 
 	/**
 	 * Disable availability zones.
 	 * 
 	 * @param loadBalancerName the name of the load balancer
 	 * @param availabilityZones a list of availability zones to disable
 	 * @throws LoadBalancingException wraps checked exceptions
 	 */
 	public List<String> disableAvailabilityZonesForLoadBalancer(String loadBalancerName,
 								List<String> availabilityZones) throws LoadBalancingException {
 		Map<String, String> params = new HashMap<String, String>();
 		params.put("LoadBalancerName", loadBalancerName);
 		int i=0;
 		for (String zone : availabilityZones) {
 			params.put("AvailabilityZones.member."+(i+1), zone);
 			i++;
 		}
 		GetMethod method = new GetMethod();
 		try {
 			DisableAvailabilityZonesForLoadBalancerResponse response =
 					makeRequestInt(method, "DisableAvailabilityZonesForLoadBalancer", params, DisableAvailabilityZonesForLoadBalancerResponse.class);
 			return response.getDisableAvailabilityZonesForLoadBalancerResult().getAvailabilityZones().getMembers();
 		} finally {
 			method.releaseConnection();
 		}
 	}
 
 	protected <T> T makeRequestInt(HttpMethodBase method, String action, Map<String, String> params, Class<T> respType)
 		throws LoadBalancingException {
 		try {
 			return makeRequest(method, action, params, respType);
 		} catch (AWSException ex) {
 			throw new LoadBalancingException(ex);
 		} catch (JAXBException ex) {
 			throw new LoadBalancingException("Problem parsing returned message.", ex);
 		} catch (MalformedURLException ex) {
 			throw new LoadBalancingException(ex.getMessage(), ex);
 		} catch (IOException ex) {
 			throw new LoadBalancingException(ex.getMessage(), ex);
 		}
 	}
 }
