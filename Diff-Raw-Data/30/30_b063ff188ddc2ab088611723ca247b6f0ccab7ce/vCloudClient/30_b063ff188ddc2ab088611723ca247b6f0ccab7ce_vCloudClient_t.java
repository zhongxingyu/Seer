 package nl.cyso.vcloud.client;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashMap;
 import java.util.List;
 import java.util.logging.Level;
 
 import javax.xml.bind.JAXBElement;
 
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
 import com.vmware.vcloud.api.rest.schema.ovf.MsgType;
 import com.vmware.vcloud.api.rest.schema.ovf.SectionType;
 import com.vmware.vcloud.sdk.Catalog;
 import com.vmware.vcloud.sdk.CatalogItem;
 import com.vmware.vcloud.sdk.OrgNetwork;
 import com.vmware.vcloud.sdk.Organization;
 import com.vmware.vcloud.sdk.VCloudException;
 import com.vmware.vcloud.sdk.VM;
 import com.vmware.vcloud.sdk.Vapp;
 import com.vmware.vcloud.sdk.VappTemplate;
 import com.vmware.vcloud.sdk.VcloudClient;
 import com.vmware.vcloud.sdk.Vdc;
 import com.vmware.vcloud.sdk.VirtualDisk;
 import com.vmware.vcloud.sdk.VirtualNetworkCard;
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
 			System.err.println("Unexpected error");
 			System.err.println(e.getStackTrace());
 			System.exit(1);
 		}
 
 		HashMap<String, ReferenceType> organizationsMap = null;
 		try {
 			this.vcc.login(username, password);
 			organizationsMap = this.vcc.getOrgRefsByName();
 		} catch (VCloudException ve) {
 			System.err.println("An error occurred while logging in:\n\n");
 			System.err.println(ve.getLocalizedMessage());
 			System.exit(1);
 		}
 
 		if (organizationsMap.isEmpty()) {
 			System.err.println("Invalid login for user " + username);
 			System.exit(1);
 		}
 	}
 
 	public void listOrganizations() {
 		this.vccPreCheck();
 
 		try {
 			Collection<ReferenceType> orgs = this.vcc.getOrgRefs();
 
 			System.out.println("Organizations:\n");
 			for (ReferenceType org : orgs) {
 				System.out.println(String.format("%s", org.getName()));
 			}
 		} catch (VCloudException e) {
 			System.err.println("An error occured while retrieving organizations");
			System.err.println(e.getLocalizedMessage());
 			System.exit(1);
 		}
 	}
 
 	public void listVDCs(String org) {
 		this.vccPreCheck();
 
 		try {
 			System.out.println("Virtual Data Centers:\n");
 			ReferenceType orgRef = this.vcc.getOrgRefsByName().get(org);
 			for (ReferenceType vdcRef : Organization.getOrganizationByReference(this.vcc, orgRef).getVdcRefs()) {
 				Vdc vdc = Vdc.getVdcByReference(this.vcc, vdcRef);
 				System.out.println(String.format("%-20s - %s", vdcRef.getName(), vdc.getResource().getDescription()));
 
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
 
 					System.out.println(String.format("\t%-10s (%s)", net.getResource().getName(), i.toString()));
 				}
 			}
 		} catch (VCloudException e) {
 			System.err.println("An error occured while retrieving virtual data centers");
			System.err.println(e.getLocalizedMessage());
 			System.exit(1);
 		}
 	}
 
 	public void listVApps(String org, String vdc) {
 		this.vccPreCheck();
 
 		Vdc vdcObj = this.getVDC(org, vdc);
 
 		try {
 			System.out.println("vApps:\n");
 			for (ReferenceType vappRef : vdcObj.getVappRefs()) {
 				Vapp vapp = Vapp.getVappByReference(this.vcc, vappRef);
 				System.out.println(String.format("%-20s - %s", vappRef.getName(), vapp.getResource().getDescription()));
 
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
 					System.out.println(String.format("\t%-10s (%s)", vn.getNetworkName(), i.toString()));
 				}
 			}
 		} catch (VCloudException e) {
 			System.err.println("An error occured while retrieving vApps");
			System.err.println(e.getLocalizedMessage());
 			System.exit(1);
 		}
 	}
 
 	public void listVMs(String org, String vdc, String vapp) {
 		this.vccPreCheck();
 
 		Vapp vappObj = this.getVApp(org, vdc, vapp);
 
 		try {
 			System.out.println("VMs:");
 			List<VM> vms = vappObj.getChildrenVms();
 
 			for (VM vm : vms) {
 				System.out.println(String.format("----\n%-20s - %s", vm.getReference().getName(), vm.getResource().getDescription()));
 				System.out.println(String.format("\tCPUs: %s, RAM: %s MB", vm.getCpu().getNoOfCpus(), vm.getMemory().getMemorySize()));
 				System.out.println(String.format("\tOS: %s", vm.getOperatingSystemSection().getDescription().getValue()));
 				try {
 					System.out.println(String.format("\tVMware Tools: %s", vm.getRuntimeInfoSection().getVMWareTools().getVersion()));
 				} catch (NullPointerException ne) {
 					System.out.println(String.format("\tVMware Tools: No"));
 				}
 
				try {
					System.out.println(String.format("\tConsole Link: http://vcloud.localhost/console.html?%s", vm.acquireTicket().getValue()));
				} catch (VCloudException e) {
					System.out.println(String.format("\tConsole Link: %s", e.getLocalizedMessage()));
				}

 				System.out.println("\tDisks:");
 				for (VirtualDisk disk : vm.getDisks()) {
 					if (disk.isHardDisk()) {
 						System.out.println(String.format("\t\t%-10s - %s MB", disk.getItemResource().getElementName().getValue(), disk.getHardDiskSize()));
 					} else {
 						System.out.println(String.format("\t\t%-10s", disk.getItemResource().getElementName().getValue()));
 					}
 				}
 
 				System.out.println("\tNICs:");
 				for (VirtualNetworkCard net : vm.getNetworkCards()) {
 					System.out.println(String.format("\t\t%-14s - %15s - %s - %s", net.getIpAddress(), net.getItemResource().getAddress().getValue(), net.getItemResource().getConnection().get(0).getValue(), net.getItemResource().getDescription().getValue()));
 				}
 			}
 		} catch (VCloudException e) {
 			System.err.println("An error occured while retrieving VMs");
			System.err.println(e.getLocalizedMessage());
 			System.exit(1);
 		}
 	}
 
 	public void listCatalogs(String org) {
 		this.vccPreCheck();
 
 		Organization orgObj = this.getOrganization(org);
 
 		try {
 			System.out.println("Catalogs:\n");
 			for (ReferenceType catalogRef : orgObj.getCatalogRefs()) {
 				Catalog catalog = Catalog.getCatalogByReference(this.vcc, catalogRef);
 				System.out.println(String.format("----\n%-20s - %s", catalogRef.getName(), catalog.getResource().getDescription()));
 
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
 
 				System.out.println("\tvApps:");
 				for (CatalogItem item : vapps) {
 					System.out.println(String.format("\t\t%-20s - %s", item.getReference().getName(), item.getResource().getDescription().replace("\n", ", ")));
 				}
 				System.out.println("\tvMedia:");
 				for (CatalogItem item : media) {
 					System.out.println(String.format("\t\t%-20s - %s", item.getReference().getName(), item.getResource().getDescription().replace("\n", ", ")));
 				}
 			}
 		} catch (VCloudException e) {
 			System.err.println("An error occured while retrieving Catalogs");
			System.err.println(e.getLocalizedMessage());
 			System.exit(1);
 		}
 	}
 
 	private Organization getOrganization(String org) {
 		this.vccPreCheck();
 
 		Organization orgObj = null;
 		try {
 			ReferenceType orgRef = this.vcc.getOrgRefByName(org);
 			orgObj = Organization.getOrganizationByReference(this.vcc, orgRef);
 		} catch (VCloudException e) {
 			System.err.println("An error occured while selecting the organization");
			System.err.println(e.getLocalizedMessage());
 			System.exit(1);
 		} catch (NullPointerException ne) {
 			System.err.println("Organization does not exist");
 			System.exit(1);
 		}
 
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
 			System.err.println("An error occured while selecting the virtual data center");
			System.err.println(e.getLocalizedMessage());
 			System.exit(1);
 		} catch (NullPointerException ne) {
 			System.err.println("Virtual data center does not exist");
 			System.exit(1);
 		}
 
 		return vdcObj;
 	}
 
 	private Vapp getVApp(String org, String vdc, String vapp) {
 		this.vccPreCheck();
 
 		Vapp vappObj = null;
 		try {
 			Vdc vdcObj = this.getVDC(org, vdc);
 
 			vappObj = Vapp.getVappByReference(this.vcc, vdcObj.getVappRefByName(vapp));
 		} catch (VCloudException e) {
 			System.err.println("An error occured while retrieving vApp");
 			System.exit(1);
 		} catch (NullPointerException ne) {
 			System.err.println("vApp does not exist");
 			System.exit(1);
 		}
 
 		return vappObj;
 	}
 
 	private CatalogItem getCatalogItem(String org, String catalog, String item, String type) {
 		this.vccPreCheck();
 
 		Catalog cat = null;
 		try {
 			Organization orgObj = this.getOrganization(org);
 
 			for (ReferenceType catalogRef : orgObj.getCatalogRefs()) {
 				if (catalogRef.getName().equals(catalog)) {
 					cat = Catalog.getCatalogByReference(this.vcc, catalogRef);
 				}
 			}
 		} catch (VCloudException e) {
 			System.err.println("An error occured while retrieving Catalog");
			System.err.println(e.getLocalizedMessage());
 			System.exit(1);
 		}
 
 		if (cat == null) {
 			System.err.println("Catalog not found");
 			System.exit(1);
 		}
 
 		CatalogItem itemObj = null;
 		try {
 			itemObj = CatalogItem.getCatalogItemByReference(this.vcc, cat.getCatalogItemRefByName(item));
 
 			if (!itemObj.getEntityReference().getType().equals(type)) {
 				System.err.println("Catalog item was found, but was not of the requested type");
 				System.exit(1);
 			}
 		} catch (VCloudException e) {
 			System.err.println("An error occured while retrieving vApp");
			System.err.println(e.getLocalizedMessage());
 			System.exit(1);
 		} catch (NullPointerException ne) {
 			System.err.println("Catalog item not found");
 			System.exit(1);
 		}
 
 		return itemObj;
 	}
 
 	public void recomposeVApp(String org, String vdc, String vapp, String catalog, String template, String fqdn, String description, String ip, String network) {
 		this.vccPreCheck();
 
 		Vapp vappObj = this.getVApp(org, vdc, vapp);
 		CatalogItem itemObj = this.getCatalogItem(org, catalog, template, vCloudConstants.MediaType.VAPP_TEMPLATE);
 		VappTemplate templateObj = null;
 		VappTemplate vmObj = null;
 		try {
 			templateObj = VappTemplate.getVappTemplateByReference(this.vcc, itemObj.getEntityReference());
 			for (VappTemplate child : templateObj.getChildren()) {
 				if (child.isVm()) {
 					vmObj = child;
 				}
 			}
 		} catch (VCloudException e) {
 			System.err.println("Unexpected error");
 			e.printStackTrace();
 			System.exit(1);
 		}
 
 		if (vmObj == null) {
 			System.err.println("Could not find VM in specified vApp");
 			System.exit(1);
 		}
 
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
 
 		// Do it
 		try {
 			vappObj.recomposeVapp(recomp);
 		} catch (VCloudException e) {
			System.err.println("An error occured while recomposing vApp");
			System.err.println(e.getLocalizedMessage());
			System.exit(1);
 		}
 	}
 }
