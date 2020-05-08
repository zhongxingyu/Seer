 package com.dgrid.helpers.impl;
 
 import java.io.IOException;
 import java.util.List;
 import java.util.Map;
 
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 
 import com.dgrid.errors.TransportException;
 import com.dgrid.gen.InvalidApiKey;
 import com.dgrid.helpers.AWSConstants;
 import com.dgrid.helpers.EC2Helper;
 import com.dgrid.helpers.AWSConstants.EC2InstanceType;
 import com.dgrid.service.DGridClient;
 import com.xerox.amazonws.ec2.EC2Exception;
 import com.xerox.amazonws.ec2.EC2Utils;
 import com.xerox.amazonws.ec2.InstanceType;
 import com.xerox.amazonws.ec2.Jec2;
 
 public class EC2HelperImpl implements EC2Helper {
 
 	private Log log = LogFactory.getLog(getClass());
 
 	private DGridClient gridClient;
 
	public void setGridClient(DGridClient gridClient) {
 		this.gridClient = gridClient;
 	}
 
 	public String getInstanceUserdata() throws IOException {
 		log.trace("getInstanceUserdata");
 		return EC2Utils.getInstanceUserdata();
 	}
 
 	public Map<String, String> getInstanceMetadata() {
 		log.trace("getInstanceMetadata()");
 		return EC2Utils.getInstanceMetadata();
 	}
 
 	public String getInstanceMetadata(String name) throws IOException {
 		log.trace("getInstanceMetadata");
 		return EC2Utils.getInstanceMetadata(name);
 	}
 
 	public void runInstances(String imageId, int minCount, int maxCount,
 			List<String> groupSet, String userData, String keyName,
 			EC2InstanceType type) throws TransportException, InvalidApiKey,
 			com.dgrid.errors.EC2Exception {
 		log.trace("runInstances()");
 		Jec2 jec2 = getJec2();
 		InstanceType instanceType = getInstanceType(type);
 		try {
 			jec2.runInstances(imageId, minCount, maxCount, groupSet, userData,
 					keyName, instanceType);
 		} catch (EC2Exception e) {
 			log.error("EC2Exception in runInstances()", e);
 			throw new com.dgrid.errors.EC2Exception(e);
 		} finally {
 		}
 	}
 
 	public void rebootInstances(String[] instanceIds)
 			throws TransportException, InvalidApiKey,
 			com.dgrid.errors.EC2Exception {
 		log.trace("rebootInstances()");
 		Jec2 jec2 = getJec2();
 		try {
 			jec2.rebootInstances(instanceIds);
 		} catch (EC2Exception e) {
 			log.error("EC2Exception in rebootInstances()", e);
 			throw new com.dgrid.errors.EC2Exception(e);
 		}
 	}
 
 	public void terminateInstances(String[] instanceIds)
 			throws TransportException, InvalidApiKey,
 			com.dgrid.errors.EC2Exception {
 		log.trace("terminateInstances()");
 		Jec2 jec2 = getJec2();
 		try {
 			jec2.terminateInstances(instanceIds);
 		} catch (EC2Exception e) {
 			log.error("EC2Exception in terminateInstances()", e);
 			throw new com.dgrid.errors.EC2Exception(e);
 		}
 	}
 
 	public String allocateAddress() throws TransportException, InvalidApiKey,
 			com.dgrid.errors.EC2Exception {
 		log.trace("allocateAddress()");
 		Jec2 jec2 = getJec2();
 		try {
 			return jec2.allocateAddress();
 		} catch (EC2Exception e) {
 			log.error("EC2Exception in terminateInstances()", e);
 			throw new com.dgrid.errors.EC2Exception(e);
 		}
 	}
 
 	public void associateAddress(String instanceId, String publicIp)
 			throws TransportException, InvalidApiKey,
 			com.dgrid.errors.EC2Exception {
 		log.trace("associateAddress()");
 		Jec2 jec2 = getJec2();
 		try {
 			jec2.associateAddress(instanceId, publicIp);
 		} catch (EC2Exception e) {
 			log.error("EC2Exception in terminateInstances()", e);
 			throw new com.dgrid.errors.EC2Exception(e);
 		}
 	}
 
 	public void disassociateAddress(String publicIp) throws TransportException,
 			InvalidApiKey, com.dgrid.errors.EC2Exception {
 		log.trace("disassociateAddress()");
 		Jec2 jec2 = getJec2();
 		try {
 			jec2.disassociateAddress(publicIp);
 		} catch (EC2Exception e) {
 			log.error("EC2Exception in terminateInstances()", e);
 			throw new com.dgrid.errors.EC2Exception(e);
 		}
 	}
 
 	public Jec2 getJec2() throws TransportException, InvalidApiKey {
 		log.trace("getJec2()");
 		String awsAccessId = gridClient.getSetting(
 				AWSConstants.AWS_ACCESS_KEY_SETTING, "");
 		String awsSecretKey = gridClient.getSetting(
 				AWSConstants.AWS_SECRET_KEY_SETTING, "");
 		boolean secure = Boolean.parseBoolean(gridClient.getSetting(
 				AWSConstants.AWS_SECURE, Boolean.toString(false)));
 		Jec2 jec2 = new Jec2(awsAccessId, awsSecretKey, secure);
 		return jec2;
 	}
 
 	private InstanceType getInstanceType(EC2InstanceType localType) {
 		InstanceType t = null;
 		switch (localType) {
 		case DEFAULT:
 			t = InstanceType.DEFAULT;
 			break;
 		case LARGE:
 			t = InstanceType.LARGE;
 			break;
 		case MEDIUM_HCPU:
 			t = InstanceType.MEDIUM_HCPU;
 			break;
 		case XLARGE:
 			t = InstanceType.XLARGE;
 			break;
 		case XLARGE_HCPU:
 			t = InstanceType.XLARGE_HCPU;
 			break;
 		default:
 			t = InstanceType.DEFAULT;
 		}
 		return t;
 	}
 
 }
