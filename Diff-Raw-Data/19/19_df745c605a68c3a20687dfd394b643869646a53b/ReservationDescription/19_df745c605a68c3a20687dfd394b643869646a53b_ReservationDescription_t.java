 //
 // typica - A client library for Amazon Web Services
 // Copyright (C) 2007 Xerox Corporation
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
 
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Iterator;
 import java.util.List;
 
 import com.xerox.amazonws.typica.jaxb.EbsInstanceBlockDeviceMappingResponseType;
 import com.xerox.amazonws.typica.jaxb.GroupItemType;
 import com.xerox.amazonws.typica.jaxb.GroupSetType;
 import com.xerox.amazonws.typica.jaxb.InstanceBlockDeviceMappingItemType;
import com.xerox.amazonws.typica.jaxb.InstanceBlockDeviceMappingResponseType;
 import com.xerox.amazonws.typica.jaxb.InstanceBlockDeviceMappingResponseItemType;
 import com.xerox.amazonws.typica.jaxb.InstanceStateType;
 import com.xerox.amazonws.typica.jaxb.ProductCodesSetItemType;
 import com.xerox.amazonws.typica.jaxb.ProductCodeType;
 import com.xerox.amazonws.typica.jaxb.RunningInstancesItemType;
 import com.xerox.amazonws.typica.jaxb.RunningInstancesSetType;
 
 /**
  * An instance of this class represents an EC2 instance slot reservation.
  * <p>
  * Instances are returned by calls to
  * {@link com.xerox.amazonws.ec2.Jec2#runInstances(String, int, int, List, String, String)},
  * {@link com.xerox.amazonws.ec2.Jec2#describeInstances(List)} and
  * {@link com.xerox.amazonws.ec2.Jec2#describeInstances(String[])}.
  */
 public class ReservationDescription {
 	private String requestId;
 	private String owner;
 	private String resId;
 	private String requesterId;
 	private List<Instance> instances = new ArrayList<Instance>();
 	private List<String> groups = new ArrayList<String>();
 
 	public ReservationDescription(String requestId, String owner, String resId, String requesterId) {
 		this.requestId = requestId;
 		this.owner = owner;
 		this.resId = resId;
 		this.requesterId = requesterId;
 	}
 
 	ReservationDescription(String requestId, String ownerId, String reservationId, String requesterId,
 					GroupSetType groupSet, RunningInstancesSetType instSet) {
 		this(requestId, ownerId, reservationId, requesterId);
 		Iterator groups_iter = groupSet.getItems().iterator();
 		while (groups_iter.hasNext()) {
 			GroupItemType rsp_item = (GroupItemType) groups_iter.next();
 			groups.add(rsp_item.getGroupId());
 		}
 		Iterator instances_iter = instSet.getItems().iterator();
 		while (instances_iter.hasNext()) {
 			RunningInstancesItemType rsp_item = (RunningInstancesItemType) instances_iter.next();
 			instances.add(new Instance(rsp_item));
 		}
 	}
 
 	public String getRequestId() {
 		return requestId;
 	}
 
 	public String getOwner() {
 		return owner;
 	}
 
 	public String getReservationId() {
 		return resId;
 	}
 
 	public String getRequesterId() {
 		return requesterId;
 	}
 
 	public Instance addInstance(String imageId, String instanceId,
 			String privateDnsName, String dnsName, InstanceStateType state,
 			String reason, String keyName, String launchIndex, List<String> productCodes,
 			Calendar launchTime, InstanceType instanceType,
 			String availabilityZone, String kernelId, String ramdiskId, String platform,
 			boolean monitoring, String subnetId, String privateIpAddress, String ipAddress,
 			String architecture, String rootDeviceType, String rootDeviceName,
 			List<InstanceBlockDeviceMapping> blockDeviceMapping, String instanceLifecycle,
 			String spotInstanceRequestId) {
 		Instance instance = new Instance(imageId, instanceId, privateDnsName,
 				dnsName, state.getName(), ""+state.getCode(), reason, 
 				keyName, launchIndex, productCodes,
 				instanceType, launchTime,
 				availabilityZone, kernelId, ramdiskId, platform,
 				monitoring, subnetId, privateIpAddress, ipAddress,
 				architecture, rootDeviceType, rootDeviceName,
 				blockDeviceMapping, instanceLifecycle, spotInstanceRequestId);
 		instances.add(instance);
 		return instance;
 	}
 
 	public List<Instance> getInstances() {
 		return instances;
 	}
 
 	public String addGroup(String groupId) {
 		groups.add(groupId);
 		return groupId;
 	}
 
 	public List<String> getGroups() {
 		return groups;
 	}
 
 	/**
 	 * Encapsulates information about an EC2 instance within a
 	 * {@link ReservationDescription}.
 	 */
 	public class Instance {
 		private String imageId;
 		private String instanceId;
 		private String privateDnsName;
 		private String dnsName;
 		private String reason;
 		private String keyName;
 		private String launchIndex;
 		private List<String> productCodes;
 		private InstanceType instanceType;
 		private Calendar launchTime;
 		private String availabilityZone;
 		private String kernelId;
 		private String ramdiskId;
 		private String platform;
 
 		/**
 		 * An EC2 instance may be in one of four states:
 		 * <ol>
 		 * <li><b>pending</b> - the instance is in the process of being
 		 * launched.</li>
 		 * <li><b>running</b> - the has been launched. It may be in the
 		 * process of booting and is not yet guaranteed to be reachable.</li>
 		 * <li><b>shutting-down</b> - the instance is in the process of
 		 * shutting down.</li>
 		 * <li><b>terminated</b> - the instance is no longer running.</li>
 		 * </ol>
 		 */
 		private String state;
 		private String stateCode;
 		private boolean monitoring;
 		private String subnetId;
 		private String privateIpAddress;
 		private String ipAddress;
 		private String architecture;
 		private String rootDeviceType;
 		private String rootDeviceName;
 		private List<InstanceBlockDeviceMapping> blockDeviceMapping;
 		private String instanceLifecycle;
 		private String spotInstanceRequestId;
 
 		public Instance(String imageId, String instanceId, String privateDnsName,
 				String dnsName, String stateName, String stateCode, String reason,
 				String keyName, String launchIndex, List<String> productCodes,
 				InstanceType instanceType, Calendar launchTime,
 				String availabilityZone, String kernelId, String ramdiskId, String platform,
 				boolean monitoring, String subnetId, String privateIpAddress, String ipAddress,
 				String architecture, String rootDeviceType, String rootDeviceName,
 				List<InstanceBlockDeviceMapping> blockDeviceMapping, String instanceLifecycle,
 				String spotInstanceRequestId) {
 			this.imageId = imageId;
 			this.instanceId = instanceId;
 			this.privateDnsName = privateDnsName;
 			this.dnsName = dnsName;
 			this.state = stateName;
 			this.stateCode = stateCode;
 			this.reason = reason;
 			this.keyName = keyName;
 			this.launchIndex = launchIndex;
 			this.productCodes = productCodes;
 			this.instanceType = instanceType;
 			this.launchTime = launchTime;
 			this.availabilityZone = availabilityZone;
 			this.kernelId = kernelId;
 			this.ramdiskId = ramdiskId;
 			this.platform = platform;
 			this.monitoring = monitoring;
 			this.subnetId = subnetId;
 			this.privateIpAddress = privateIpAddress;
 			this.ipAddress = ipAddress;
 			this.architecture = architecture;
 			this.rootDeviceType = rootDeviceType;
 			this.rootDeviceName = rootDeviceName;
 			this.blockDeviceMapping = blockDeviceMapping;
 			this.instanceLifecycle = instanceLifecycle;
 			this.spotInstanceRequestId = spotInstanceRequestId;
 		}
 
 		Instance(RunningInstancesItemType rsp_item) {
 			this.imageId = rsp_item.getImageId();
 			this.instanceId = rsp_item.getInstanceId();
 			this.privateDnsName = rsp_item.getPrivateDnsName();
 			this.dnsName = rsp_item.getDnsName();
 			this.state = rsp_item.getInstanceState().getName();
 			this.stateCode = ""+rsp_item.getInstanceState().getCode();
 			if (rsp_item.getStateReason() != null) {
 				this.reason = rsp_item.getStateReason().getMessage();
 			}
 			else {
 				this.reason = "";
 			}
 			this.keyName = rsp_item.getKeyName();
 			this.launchIndex = rsp_item.getAmiLaunchIndex();
 			ArrayList<String> codes = new ArrayList<String>();
 			for (ProductCodesSetItemType type : rsp_item.getProductCodes().getItems()) {
 				codes.add(type.getProductCode());
 			}
 			this.productCodes = codes;
 			this.instanceType = InstanceType.getTypeFromString(rsp_item.getInstanceType());
 			this.launchTime = rsp_item.getLaunchTime().toGregorianCalendar();
 			this.availabilityZone = rsp_item.getPlacement().getAvailabilityZone();
 			this.kernelId = rsp_item.getKernelId();
 			this.ramdiskId = rsp_item.getRamdiskId();
 			this.platform = rsp_item.getPlatform();
 			this.monitoring = rsp_item.getMonitoring().getState().equals("enabled");
 			this.subnetId = rsp_item.getSubnetId();
 			this.privateIpAddress = rsp_item.getPrivateIpAddress();
 			this.ipAddress = rsp_item.getIpAddress();
 			this.architecture = rsp_item.getArchitecture();
 			this.rootDeviceType = rsp_item.getRootDeviceType();
 			this.rootDeviceName = rsp_item.getRootDeviceName();
 			this.blockDeviceMapping = new ArrayList<InstanceBlockDeviceMapping>();
			InstanceBlockDeviceMappingResponseType bdmType = rsp_item.getBlockDeviceMapping();
			if (bdmType != null) {
				List<InstanceBlockDeviceMappingResponseItemType> bdmSet = bdmType.getItems();
				if (bdmSet != null) {
					for (InstanceBlockDeviceMappingResponseItemType mapping : bdmSet) {
						EbsInstanceBlockDeviceMappingResponseType ebs = mapping.getEbs();
						this.blockDeviceMapping.add(new InstanceBlockDeviceMapping(mapping.getDeviceName(), ebs.getVolumeId(),
										ebs.getStatus(), ebs.getAttachTime().toGregorianCalendar(),
										ebs.isDeleteOnTermination()));
					}
 				}
 			}
 			this.instanceLifecycle = rsp_item.getInstanceLifecycle();
 			this.spotInstanceRequestId = rsp_item.getSpotInstanceRequestId();
 		}
 
 		public String getImageId() {
 			return imageId;
 		}
 
 		public String getInstanceId() {
 			return instanceId;
 		}
 
 		public String getPrivateDnsName() {
 			return privateDnsName;
 		}
 
 		public String getDnsName() {
 			return dnsName;
 		}
 
 		public String getReason() {
 			return reason;
 		}
 
 		public String getKeyName() {
 			return keyName;
 		}
 
 		public String getLaunchIndex() {
 			return launchIndex;
 		}
 
 		public List<String> getProductCodes() {
 			return productCodes;
 		}
 
 		public String getState() {
 			return state;
 		}
 
 		public boolean isRunning() {
 			return this.state.equals("running");
 		}
 
 		public boolean isPending() {
 			return this.state.equals("pending");
 		}
 
 		public boolean isShuttingDown() {
 			return this.state.equals("shutting-down");
 		}
 
 		public boolean isTerminated() {
 			return this.state.equals("terminated");
 		}
 
 		public String getStateCode() {
 			return stateCode;
 		}
 
 		public InstanceType getInstanceType() {
 			return this.instanceType;
 		}
 
 		public Calendar getLaunchTime() {
 			return this.launchTime;
 		}
 
 		public String getAvailabilityZone() {
 			return availabilityZone;
 		}
 
 		public String getKernelId() {
 			return kernelId;
 		}
 
 		public String getRamdiskId() {
 			return ramdiskId;
 		}
 
 		public String getPlatform() {
 			return platform;
 		}
 
 		public boolean isMonitoring() {
 			return monitoring;
 		}
 
 		public String getSubnetId() {
 			return subnetId;
 		}
 
 		public String getPrivateIpAddress() {
 			return privateIpAddress;
 		}
 
 		public String getIpAddress() {
 			return ipAddress;
 		}
 
 		public String getArchitecture() {
 			return architecture;
 		}
 
 		public String getRootDeviceType() {
 			return rootDeviceType;
 		}
 
 		public String getRootDeviceName() {
 			return rootDeviceName;
 		}
 
 		public List<InstanceBlockDeviceMapping> getBlockDeviceMappings() {
 			return blockDeviceMapping;
 		}
 
 		public String getInstanceLifecycle() {
 			return instanceLifecycle;
 		}
 
 		public String getSpotInstanceRequestId() {
 			return spotInstanceRequestId;
 		}
 
 		public String toString() {
 			return "[img=" + this.imageId + ", instance=" + this.instanceId
 					+ ", privateDns=" + this.privateDnsName
 					+ ", dns=" + this.dnsName + ", loc=" + this.availabilityZone + ", state="
 					+ this.state + "(" + this.stateCode + ") reason="
 					+ this.reason + ", monitoring=" + this.monitoring
 					+ ", subnetId=" + this.subnetId + "]";
 		}
 	}
 
 	public String toString() {
 		return "Reservation[id=" + this.resId + ", Loc=" + ", instances="
 				+ this.instances + ", groups=" + this.groups + "]";
 	}
 }
 
