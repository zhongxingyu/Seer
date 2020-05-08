 package nl.cyso.vcloud.client;
 
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 
 import javax.xml.bind.JAXBElement;
 import javax.xml.namespace.QName;
 
 import nl.cyso.vcloud.client.constants.vCloudConstants;
 import nl.cyso.vcloud.client.types.ManipulateType;
 
 import org.apache.commons.lang.NotImplementedException;
 
 import com.vmware.vcloud.api.rest.schema.GuestCustomizationSectionType;
 import com.vmware.vcloud.api.rest.schema.InstantiationParamsType;
 import com.vmware.vcloud.api.rest.schema.IpRangeType;
 import com.vmware.vcloud.api.rest.schema.NetworkConnectionSectionType;
 import com.vmware.vcloud.api.rest.schema.NetworkConnectionType;
 import com.vmware.vcloud.api.rest.schema.ObjectFactory;
 import com.vmware.vcloud.api.rest.schema.RecomposeVAppParamsType;
 import com.vmware.vcloud.api.rest.schema.ReferenceType;
 import com.vmware.vcloud.api.rest.schema.SourcedCompositionItemParamType;
 import com.vmware.vcloud.api.rest.schema.VAppNetworkConfigurationType;
 import com.vmware.vcloud.api.rest.schema.ovf.CimString;
 import com.vmware.vcloud.api.rest.schema.ovf.MsgType;
 import com.vmware.vcloud.api.rest.schema.ovf.RASDType;
 import com.vmware.vcloud.api.rest.schema.ovf.SectionType;
 import com.vmware.vcloud.sdk.Catalog;
 import com.vmware.vcloud.sdk.CatalogItem;
 import com.vmware.vcloud.sdk.FakeSSLSocketFactory;
 import com.vmware.vcloud.sdk.OrgNetwork;
 import com.vmware.vcloud.sdk.Organization;
 import com.vmware.vcloud.sdk.Task;
 import com.vmware.vcloud.sdk.VCloudException;
 import com.vmware.vcloud.sdk.VM;
 import com.vmware.vcloud.sdk.Vapp;
 import com.vmware.vcloud.sdk.VappTemplate;
 import com.vmware.vcloud.sdk.VcloudClient;
 import com.vmware.vcloud.sdk.Vdc;
 import com.vmware.vcloud.sdk.VirtualDisk;
 import com.vmware.vcloud.sdk.VirtualNetworkCard;
 import com.vmware.vcloud.sdk.constants.UndeployPowerActionType;
 import com.vmware.vcloud.sdk.constants.Version;
 
 public class vCloudClient {
 
 	private VcloudClient vcc = null;
 
 	protected VcloudClient getVcc() {
 		return vcc;
 	}
 
 	private void vccPreCheck() {
 		if (this.vcc == null) {
 			throw new IllegalStateException("vCloudClient was not logged in");
 		}
 	}
 
 	public void login(String uri, String username, String password) {
 		VcloudClient.setLogLevel(Level.OFF);
 		this.vcc = new VcloudClient(uri, Version.V1_5);
 		try {
 			this.vcc.registerScheme("https", 443, FakeSSLSocketFactory.getInstance());
 		} catch (Exception e) {
 			Formatter.printErrorLine("Unexpected error");
 			Formatter.printStackTrace(e);
 			System.exit(1);
 		}
 
 		HashMap<String, ReferenceType> organizationsMap = null;
 		try {
 			this.vcc.login(username, password);
 			organizationsMap = this.vcc.getOrgRefsByName();
 		} catch (VCloudException ve) {
 			Formatter.printErrorLine("An error occurred while logging in:\n");
 			Formatter.printErrorLine(ve.getLocalizedMessage());
 			System.exit(1);
 		}
 
 		if (organizationsMap.isEmpty()) {
 			Formatter.printErrorLine("Invalid login for user " + username);
 			System.exit(1);
 		}
 	}
 
 	public void listOrganizations() {
 		this.vccPreCheck();
 
 		try {
 			Formatter.printInfoLine("Retrieving Organizations...");
 			Collection<ReferenceType> orgs = this.vcc.getOrgRefs();
 
 			List<String> o = new ArrayList<String>(orgs.size());
 			for (ReferenceType org : orgs) {
 				o.add(org.getName());
 			}
 			Collections.sort(o, String.CASE_INSENSITIVE_ORDER);
 
 			Formatter.printInfoLine("Organizations:");
 			for (String p : o) {
 				Formatter.printInfoLine("\t" + p);
 			}
 
 		} catch (VCloudException e) {
 			Formatter.printErrorLine("An error occured while retrieving organizations");
 			Formatter.printErrorLine(e.getLocalizedMessage());
 			System.exit(1);
 		}
 	}
 
 	public void listVDCs(String org) {
 		this.vccPreCheck();
 
 		Organization orgObj = this.getOrganization(org);
 
 		try {
 			Formatter.printInfoLine("This organization contains:\n\n");
 			for (ReferenceType vdcRef : orgObj.getVdcRefs()) {
 				Vdc vdc = Vdc.getVdcByReference(this.vcc, vdcRef);
 				Formatter.printInfoLine(String.format("%-20s - %s", vdcRef.getName(), vdc.getResource().getDescription()));
 
 				for (ReferenceType netRef : vdc.getAvailableNetworkRefs()) {
 					OrgNetwork net = OrgNetwork.getOrgNetworkByReference(this.vcc, netRef);
 
 					StringBuilder i = new StringBuilder();
 					try {
 						List<IpRangeType> ips = net.getResource().getConfiguration().getIpScope().getIpRanges().getIpRange();
 						for (IpRangeType ip : ips) {
 							i.append(String.format(" %s - %s ", ip.getStartAddress(), ip.getEndAddress()));
 						}
 					} catch (NullPointerException e) {
 						i.append("?");
 					}
 
 					Formatter.printInfoLine(String.format("\t%-10s (%s)", net.getResource().getName(), i.toString()));
 				}
 			}
 		} catch (VCloudException e) {
 			Formatter.printErrorLine("An error occured while retrieving virtual data centers");
 			Formatter.printErrorLine(e.getLocalizedMessage());
 			System.exit(1);
 		}
 	}
 
 	public void listVApps(String org, String vdc) {
 		this.vccPreCheck();
 
 		Vdc vdcObj = this.getVDC(org, vdc);
 
 		try {
 			Formatter.printInfoLine("This virtual data center contains:\n");
 			for (ReferenceType vappRef : vdcObj.getVappRefs()) {
 				Vapp vapp = Vapp.getVappByReference(this.vcc, vappRef);
 				Formatter.printInfoLine(String.format("%-20s - %s", vappRef.getName(), vapp.getResource().getDescription()));
 
 				for (VAppNetworkConfigurationType vn : vapp.getVappNetworkConfigurations()) {
 					StringBuilder i = new StringBuilder();
 
 					try {
 						List<IpRangeType> vips = vn.getConfiguration().getIpScope().getIpRanges().getIpRange();
 
 						for (IpRangeType ip : vips) {
 							i.append(String.format(" %s - %s ", ip.getStartAddress(), ip.getEndAddress()));
 						}
 					} catch (NullPointerException e) {
 						i.append("?");
 					}
 					Formatter.printInfoLine(String.format("\t%-10s (%s)", vn.getNetworkName(), i.toString()));
 				}
 			}
 		} catch (VCloudException e) {
 			Formatter.printErrorLine("An error occured while retrieving vApps");
 			Formatter.printErrorLine(e.getLocalizedMessage());
 			System.exit(1);
 		}
 	}
 
 	public void listVMs(String org, String vdc, String vapp) {
 		this.vccPreCheck();
 
 		Vapp vappObj = this.getVApp(org, vdc, vapp);
 
 		try {
 			Formatter.printInfoLine("This vApp contains:");
 			List<VM> vms = vappObj.getChildrenVms();
 
 			for (VM vm : vms) {
 				Formatter.printInfoLine(String.format("----\n%-20s - %s", vm.getReference().getName(), vm.getResource().getDescription()));
				Formatter.printInfoLine(String.format("\tID: %s", vm.getReference().getHref()));
 				Formatter.printInfoLine(String.format("\tCPUs: %s, RAM: %s MB", vm.getCpu().getNoOfCpus(), vm.getMemory().getMemorySize()));
 				Formatter.printInfoLine(String.format("\tOS: %s", vm.getOperatingSystemSection().getDescription().getValue()));
 
 				try {
 					Formatter.printInfoLine(String.format("\tVMware Tools: Version %s", vm.getRuntimeInfoSection().getVMWareTools().getVersion()));
 				} catch (NullPointerException ne) {
 					Formatter.printInfoLine(String.format("\tVMware Tools: No"));
 				}
 
 				try {
 					Formatter.printInfoLine(String.format("\tConsole Link: http://vcloud.localhost/console.html?%s", vm.acquireTicket().getValue()));
 				} catch (VCloudException e) {
 					Formatter.printInfoLine(String.format("\tConsole Link: %s", e.getLocalizedMessage()));
 				}
 
 				try {
 					int length = vm.getVMDiskChainLength();
 					Formatter.printInfoLine(String.format("\tDisk Chain Length: %s %s", String.valueOf(length), (length == 0 ? "" : length == 1 ? "(Flat)" : "(Chained)")));
 				} catch (NullPointerException ne) {
 					Formatter.printInfoLine(String.format("\tDisk Chain Length: Unknown"));
 				}
 
 				Formatter.printInfoLine("\tDisks:");
 				for (VirtualDisk disk : vm.getDisks()) {
 					if (disk.isHardDisk()) {
 						Formatter.printInfoLine(String.format("\t\t%-10s - %s MB", disk.getItemResource().getElementName().getValue(), disk.getHardDiskSize()));
 					} else {
 						Formatter.printInfoLine(String.format("\t\t%-10s", disk.getItemResource().getElementName().getValue()));
 					}
 				}
 
 				Formatter.printInfoLine("\tNICs:");
 				for (VirtualNetworkCard net : vm.getNetworkCards()) {
 					String ip = null;
 					try {
 						ip = net.getIpAddress();
 					} catch (VCloudException e) {
 						ip = "?";
 					}
 					Formatter.printInfoLine(String.format("\t\t%-14s - %15s - %s - %s", ip, net.getItemResource().getAddress().getValue(), net.getItemResource().getConnection().get(0).getValue(), net.getItemResource().getDescription().getValue()));
 				}
 			}
 		} catch (VCloudException e) {
 			Formatter.printErrorLine("An error occured while retrieving VMs");
 			Formatter.printErrorLine(e.getLocalizedMessage());
 			System.exit(1);
 		}
 	}
 
 	public void listCatalogs(String org) {
 		this.vccPreCheck();
 
 		Organization orgObj = this.getOrganization(org);
 
 		try {
 			Formatter.printInfoLine("Catalogs:\n");
 			for (ReferenceType catalogRef : orgObj.getCatalogRefs()) {
 				Catalog catalog = Catalog.getCatalogByReference(this.vcc, catalogRef);
 				Formatter.printInfoLine(String.format("----\n%-20s - %s", catalogRef.getName(), catalog.getResource().getDescription()));
 
 				List<CatalogItem> vapps = new ArrayList<CatalogItem>();
 				List<CatalogItem> media = new ArrayList<CatalogItem>();
 				for (ReferenceType itemRef : catalog.getCatalogItemReferences()) {
 					CatalogItem item = CatalogItem.getCatalogItemByReference(this.vcc, itemRef);
 					ReferenceType ref = item.getEntityReference();
 
 					if (ref.getType().equals(vCloudConstants.MediaType.VAPP_TEMPLATE)) {
 						vapps.add(item);
 					} else if (ref.getType().equals(vCloudConstants.MediaType.MEDIA)) {
 						media.add(item);
 					}
 				}
 
 				Formatter.printInfoLine("\tvApps:");
 				for (CatalogItem item : vapps) {
 					Formatter.printInfoLine(String.format("\t\t%-20s - %s", item.getReference().getName(), item.getResource().getDescription().replace("\n", ", ")));
 				}
 				Formatter.printInfoLine("\tvMedia:");
 				for (CatalogItem item : media) {
 					Formatter.printInfoLine(String.format("\t\t%-20s - %s", item.getReference().getName(), item.getResource().getDescription().replace("\n", ", ")));
 				}
 			}
 		} catch (VCloudException e) {
 			Formatter.printErrorLine("An error occured while retrieving Catalogs");
 			Formatter.printErrorLine(e.getLocalizedMessage());
 			System.exit(1);
 		}
 	}
 
 	private Organization getOrganization(String org) {
 		this.vccPreCheck();
 
 		Formatter.printInfoLine("Retrieving organization: " + org);
 
 		Organization orgObj = null;
 		try {
 			ReferenceType orgRef = this.vcc.getOrgRefByName(org);
 			orgObj = Organization.getOrganizationByReference(this.vcc, orgRef);
 		} catch (VCloudException e) {
 			Formatter.printErrorLine("An error occured while selecting the organization");
 			Formatter.printErrorLine(e.getLocalizedMessage());
 			System.exit(1);
 		} catch (NullPointerException ne) {
 			Formatter.printErrorLine("Organization does not exist");
 			System.exit(1);
 		}
 
 		Formatter.printInfoLine("Retrieved organization");
 
 		return orgObj;
 	}
 
 	private Vdc getVDC(String org, String vdc) {
 		this.vccPreCheck();
 
 		Vdc vdcObj = null;
 		try {
 			Organization o = this.getOrganization(org);
 			ReferenceType vdcRef = o.getVdcRefByName(vdc);
 			vdcObj = Vdc.getVdcByReference(this.vcc, vdcRef);
 		} catch (VCloudException e) {
 			Formatter.printErrorLine("An error occured while selecting the virtual data center");
 			Formatter.printErrorLine(e.getLocalizedMessage());
 			System.exit(1);
 		} catch (NullPointerException ne) {
 			Formatter.printErrorLine("Virtual data center does not exist");
 			System.exit(1);
 		}
 
 		Formatter.printInfoLine("Retrieved virtual data center: " + vdc);
 
 		return vdcObj;
 	}
 
 	private Vapp getVApp(String org, String vdc, String vapp) {
 		this.vccPreCheck();
 
 		Vapp vappObj = null;
 		try {
 			Vdc vdcObj = this.getVDC(org, vdc);
 
 			vappObj = Vapp.getVappByReference(this.vcc, vdcObj.getVappRefByName(vapp));
 		} catch (VCloudException e) {
 			Formatter.printErrorLine("An error occured while retrieving vApp");
 			Formatter.printErrorLine(e.getLocalizedMessage());
 			System.exit(1);
 		} catch (NullPointerException ne) {
 			Formatter.printErrorLine("vApp does not exist");
 			System.exit(1);
 		}
 
 		Formatter.printInfoLine("Retrieved vApp: " + vapp);
 
 		return vappObj;
 	}
 
 	private VM getVM(String org, String vdc, String vapp, String vm) {
 		this.vccPreCheck();
 
 		VM vmObj = null;
 		try {
 			Vapp vappObj = this.getVApp(org, vdc, vapp);
 
 			for (VM v : vappObj.getChildrenVms()) {
 				if (v.getReference().getName().equals(vm)) {
 					vmObj = v;
 				}
 			}
 		} catch (VCloudException e) {
 			Formatter.printErrorLine("An error occured while retrieving VM");
 			Formatter.printErrorLine(e.getLocalizedMessage());
 			System.exit(1);
 		}
 
 		if (vmObj == null) {
 			Formatter.printErrorLine("VM does not exist");
 			System.exit(1);
 		}
 
 		Formatter.printInfoLine("Retrieved VM: " + vm);
 
 		return vmObj;
 	}
 
 	private CatalogItem getCatalogItem(String org, String catalog, String item, String type) {
 		this.vccPreCheck();
 
 		Formatter.printInfoLine("Retrieving catalog: " + catalog);
 
 		Catalog cat = null;
 		try {
 			Organization orgObj = this.getOrganization(org);
 
 			for (ReferenceType catalogRef : orgObj.getCatalogRefs()) {
 				if (catalogRef.getName().equals(catalog)) {
 					cat = Catalog.getCatalogByReference(this.vcc, catalogRef);
 				}
 			}
 		} catch (VCloudException e) {
 			Formatter.printErrorLine("An error occured while retrieving Catalog");
 			Formatter.printErrorLine(e.getLocalizedMessage());
 			System.exit(1);
 		}
 
 		if (cat == null) {
 			Formatter.printErrorLine("Catalog not found");
 			System.exit(1);
 		}
 
 		Formatter.printInfoLine("Retrieving catalog item: " + item);
 
 		CatalogItem itemObj = null;
 		try {
 			itemObj = CatalogItem.getCatalogItemByReference(this.vcc, cat.getCatalogItemRefByName(item));
 
 			if (!itemObj.getEntityReference().getType().equals(type)) {
 				Formatter.printErrorLine("Catalog item was found, but was not of the requested type");
 				System.exit(1);
 			}
 		} catch (VCloudException e) {
 			Formatter.printErrorLine("An error occured while retrieving vApp");
 			Formatter.printErrorLine(e.getLocalizedMessage());
 			System.exit(1);
 		} catch (NullPointerException ne) {
 			Formatter.printErrorLine("Catalog item not found");
 			System.exit(1);
 		}
 
 		return itemObj;
 	}
 
 	private Task manipulateVM(String org, String vdc, String vapp, String vm, ManipulateType action) {
 		this.vccPreCheck();
 
 		VM vmObj = this.getVM(org, vdc, vapp, vm);
 
 		Task t = null;
 		try {
 			switch (action) {
 			case POWERON:
 				Formatter.printInfoLine("Powering on VM: " + vm);
 				t = vmObj.powerOn();
 				break;
 			case POWEROFF:
 				Formatter.printInfoLine("Powering off VM: " + vm);
 				t = vmObj.undeploy(UndeployPowerActionType.POWEROFF);
 				break;
 			case SHUTDOWN:
 				Formatter.printInfoLine("Shutting down VM: " + vm);
 				t = vmObj.undeploy(UndeployPowerActionType.SHUTDOWN);
 				break;
 			case REMOVE:
 				Formatter.printInfoLine("Removing VM: " + vm);
 				t = vmObj.delete();
 				break;
 			default:
 				throw new NotImplementedException("Manipulation type not implemented: " + action.toString());
 			}
 		} catch (VCloudException e) {
 			Formatter.printErrorLine("An error occured while manipulating VM");
 			Formatter.printErrorLine(e.getLocalizedMessage());
 			System.exit(1);
 		}
 
 		return t;
 	}
 
 	public Task addVM(String org, String vdc, String vapp, String catalog, String template, String fqdn, String description, String ip, String network) {
 		this.vccPreCheck();
 
 		Vapp vappObj = this.getVApp(org, vdc, vapp);
 		CatalogItem itemObj = this.getCatalogItem(org, catalog, template, vCloudConstants.MediaType.VAPP_TEMPLATE);
 		VappTemplate templateObj = null;
 		VappTemplate vmObj = null;
 
 		Formatter.printInfoLine("Retrieving requested VM template: " + template);
 		try {
 			templateObj = VappTemplate.getVappTemplateByReference(this.vcc, itemObj.getEntityReference());
 
 			for (VappTemplate child : templateObj.getChildren()) {
 				if (child.isVm()) {
 					vmObj = child;
 				}
 			}
 		} catch (VCloudException e) {
 			Formatter.printErrorLine("Unexpected error");
 			Formatter.printStackTrace(e);
 			System.exit(1);
 		}
 
 		if (vmObj == null) {
 			Formatter.printErrorLine("Could not find template VM");
 			System.exit(1);
 		}
 
 		Formatter.printInfoLine("Building vApp recompose request");
 
 		// Change vApp settings
 		RecomposeVAppParamsType recomp = new RecomposeVAppParamsType();
 		recomp.setName(vappObj.getReference().getName());
 		List<SourcedCompositionItemParamType> sources = recomp.getSourcedItem();
 
 		// Change new VM network settings
 		NetworkConnectionType nw = new NetworkConnectionType();
 		nw.setIpAddress(ip);
 		nw.setMACAddress(null);
 		nw.setIpAddressAllocationMode("MANUAL");
 		nw.setNetwork(network);
 		nw.setIsConnected(true);
 
 		NetworkConnectionSectionType networkObject = new NetworkConnectionSectionType();
 		networkObject.setInfo(new MsgType());
 		networkObject.getNetworkConnection().add(nw);
 
 		InstantiationParamsType instant = new InstantiationParamsType();
 		List<JAXBElement<? extends SectionType>> sections = instant.getSection();
 		sections.add(new ObjectFactory().createNetworkConnectionSection(networkObject));
 
 		String[] fqdnParts = fqdn.split("\\.");
 
 		GuestCustomizationSectionType guest = new GuestCustomizationSectionType();
 		guest.setInfo(new MsgType());
 		guest.setComputerName(fqdnParts[0]);
 		sections.add(new ObjectFactory().createGuestCustomizationSection(guest));
 
 		// Whip it all up
 		SourcedCompositionItemParamType s = new SourcedCompositionItemParamType();
 		s.setSource(vmObj.getReference());
 		s.getSource().setName(fqdn);
 		s.setSourceDelete(false);
 		s.setInstantiationParams(instant);
 		sources.add(s);
 
 		Formatter.printInfoLine("Recomposing vApp");
 		// Do it
 		Task t = null;
 		try {
 			t = vappObj.recomposeVapp(recomp);
 		} catch (VCloudException e) {
 			Formatter.printErrorLine("An error occured while recomposing vApp");
 			Formatter.printErrorLine(e.getLocalizedMessage());
 			System.exit(1);
 		}
 
 		return t;
 	}
 
 	public Task removeVM(String org, String vdc, String vapp, String vm) {
 		return this.manipulateVM(org, vdc, vapp, vm, ManipulateType.REMOVE);
 	}
 
 	public Task powerOnVM(String org, String vdc, String vapp, String vm) {
 		return this.manipulateVM(org, vdc, vapp, vm, ManipulateType.POWERON);
 	}
 
 	public Task powerOffVM(String org, String vdc, String vapp, String vm) {
 		return this.manipulateVM(org, vdc, vapp, vm, ManipulateType.POWEROFF);
 	}
 
 	public Task shutdownVM(String org, String vdc, String vapp, String vm) {
 		return this.manipulateVM(org, vdc, vapp, vm, ManipulateType.SHUTDOWN);
 	}
 
 	public Task resizeVMDisks(String org, String vdc, String vapp, String vm, String diskname, BigInteger disksize) {
 		this.vccPreCheck();
 
 		Task t = null;
 		try {
 			VM vmObj = this.getVM(org, vdc, vapp, vm);
 
 			int length = 0;
 			try {
 				length = vmObj.getVMDiskChainLength();
 			} catch (NullPointerException ne) {
 				Formatter.printErrorLine("Could not retrieve VM disk chain length. Operation will continue, but may fail.");
 			}
 
 			if (length > 1) {
 				Formatter.printErrorLine("VM has a disk chain length larger than one. This VM needs to be consolidated before the disk can be extended.");
 				System.exit(1);
 			}
 
 			List<VirtualDisk> disks = vmObj.getDisks();
 			List<VirtualDisk> newDisks = new ArrayList<VirtualDisk>(disks.size());
 			for (VirtualDisk disk : disks) {
 				if (disk.isHardDisk()) {
 					RASDType d = new RASDType();
 					d.setElementName(disk.getItemResource().getElementName());
 					d.setResourceType(disk.getItemResource().getResourceType());
 					d.setInstanceID(disk.getItemResource().getInstanceID());
 
 					for (int i = 0; i < disk.getItemResource().getHostResource().size(); i++) {
 						CimString resource = disk.getItemResource().getHostResource().get(i);
 						d.getHostResource().add(resource);
 						if (disk.getItemResource().getElementName().getValue().equals(diskname)) {
 							if (disk.getHardDiskSize().compareTo(disksize) == 1) {
 								throw new VCloudException("Failed to resize disk, shrinking disks is not supported");
 							}
 							for (QName key : resource.getOtherAttributes().keySet()) {
 								if (key.getLocalPart().equals("capacity")) {
 									resource.getOtherAttributes().put(key, disksize.toString());
 								}
 							}
 						}
 					}
 					newDisks.add(new VirtualDisk(d));
 				}
 			}
 			t = vmObj.updateDisks(newDisks);
 
 		} catch (VCloudException e) {
 			Formatter.printErrorLine("An error occured while resizing disks");
 			Formatter.printErrorLine(e.getLocalizedMessage());
 			Formatter.printStackTrace(e);
 			System.exit(1);
 		}
 
 		return t;
 	}
 
 	public Task consolidateVM(String org, String vdc, String vapp, String vm) {
 		this.vccPreCheck();
 
 		Task t = null;
 		try {
 			VM vmObj = this.getVM(org, vdc, vapp, vm);
 
 			Formatter.printInfoLine("Consolidating VM: " + vm);
 			t = vmObj.consolidate();
 		} catch (VCloudException e) {
 			Formatter.printErrorLine("An error occured while consolidating");
 			Formatter.printErrorLine(e.getLocalizedMessage());
 			Formatter.printStackTrace(e);
 			System.exit(1);
 		}
 		return t;
 	}
 }
