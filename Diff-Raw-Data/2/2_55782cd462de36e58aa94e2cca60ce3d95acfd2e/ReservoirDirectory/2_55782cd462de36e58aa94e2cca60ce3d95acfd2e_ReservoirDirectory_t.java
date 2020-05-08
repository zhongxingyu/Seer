 /*
  * Claudia Project
  * http://claudia.morfeo-project.org
  *
  * (C) Copyright 2010 Telefonica Investigacion y Desarrollo
  * S.A.Unipersonal (Telefonica I+D)
  *
  * See CREDITS file for info about members and contributors.
  *
  * This program is free software; you can redistribute it and/or modify
  * it under the terms of the Affero GNU General Public License (AGPL) as 
  * published by the Free Software Foundation; either version 3 of the License, 
  * or (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the Affero GNU General Public License
  * along with this program; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA 02111-1307, USA.
  *
  * If you want to use this software an plan to distribute a
  * proprietary application in any way, and you are not licensing and
  * distributing your source code under AGPL, you probably need to
  * purchase a commercial license of the product. Please contact
  * claudia-support@lists.morfeo-project.org for more information.
  */
 package com.telefonica.claudia.slm.naming;
 
 import java.util.Iterator;
 import java.util.Set;
 
 import org.apache.log4j.ConsoleAppender;
 import org.apache.log4j.Level;
 import org.apache.log4j.Logger;
 import org.apache.log4j.PatternLayout;
 
 import com.telefonica.claudia.slm.deployment.Customer;
 import com.telefonica.claudia.slm.deployment.ProbeKPI;
 import com.telefonica.claudia.slm.deployment.Rule;
 import com.telefonica.claudia.slm.deployment.ServiceApplication;
 import com.telefonica.claudia.slm.deployment.ServiceKPI;
 import com.telefonica.claudia.slm.deployment.VEE;
 import com.telefonica.claudia.slm.deployment.VEEReplica;
 import com.telefonica.claudia.slm.deployment.hwItems.CPU;
 import com.telefonica.claudia.slm.deployment.hwItems.Disk;
 import com.telefonica.claudia.slm.deployment.hwItems.Memory;
 import com.telefonica.claudia.slm.deployment.hwItems.NIC;
 import com.telefonica.claudia.slm.deployment.hwItems.Network;
 import com.telefonica.claudia.slm.maniParser.Parser;
 
 @SuppressWarnings("unchecked")
 public class ReservoirDirectory extends Directory {
 
 	/* Class logger */
 	private static Logger logger = Logger.getLogger(Parser.class);
 
 
 	private static ReservoirDirectory resDirInstance = null;
 
 	// FIXME: make the following line work and avoid the hardwiring (quick fix during London F2F :)
 	//public static final String ROOT_NAME_SPACE = SMConfiguration.getInstance().getSiteRoot();
 	public static String ROOT_NAME_SPACE = "es.tid";
 	public static final String COSTUMERS_NAME_SPACE = "customers";
 	public static final String SERVICES_NAME_SPACE = "services";
 	public static final String NETWORKS_NAME_SPACE= "networks";
 	public static final String VEES_NAME_SPACE = "vees";
 	public static final String VEE_REPLICAS_NAME_SPACE = "replicas";
 	public static final String CPUS_NAME_SPACE = "cpus";
 	public static final String DISKS_NAME_SPACE = "disks";
 	public static final String MEMORY_NAME_SPACE = "memory";
 	public static final String NICS_NAME_SPACE = "networks";
 	public static final String SERVICE_KPIS_NAME_SPACE = "kpis";
 	public static final String RULES_KPIS_NAME_SPACE = "rules";
 
 	private ReservoirDirectory() {
 		super(new FQN(ROOT_NAME_SPACE));
 
 
 	}
 
 	public static ReservoirDirectory getInstance() {
 		if(resDirInstance == null)
 			resDirInstance = new ReservoirDirectory();
 		return resDirInstance;
 	}
 
 	public FQN buildFQN(Customer customer) {
 		return new FQN(ROOT_NAME_SPACE + FQN.CONTEXT_SEPARATOR + COSTUMERS_NAME_SPACE + FQN.CONTEXT_SEPARATOR + customer.getCustomerName());       
 	}
 
 	/**
 	 * Generate the ServiceApplication FQN. 
 	 * 
 	 * @param service
 	 * 		The service whose FQN is to be build.		
 	 * 
 	 * @return
 	 */
 	public FQN buildFQN(ServiceApplication service) {
 		return new FQN(buildFQN(service.getCustomer()) + FQN.CONTEXT_SEPARATOR + SERVICES_NAME_SPACE  + FQN.CONTEXT_SEPARATOR + service.getSerAppName());
 	}
 
 	public FQN buildFQN(VEE vee) {
 		return new FQN(buildFQN(vee.getServiceApplication()) + FQN.CONTEXT_SEPARATOR + VEES_NAME_SPACE + FQN.CONTEXT_SEPARATOR + vee.getVEEName());
 	}
 
 	public FQN buildFQN(VEEReplica veeReplica) {
 		return new FQN(buildFQN(veeReplica.getVEE()) + FQN.CONTEXT_SEPARATOR + VEE_REPLICAS_NAME_SPACE + FQN.CONTEXT_SEPARATOR + veeReplica.getId());
 	}
 
 	public FQN buildFQN(CPU cpu) {
 		return new FQN(buildFQN(cpu.getVEEReplica()) + FQN.CONTEXT_SEPARATOR + CPUS_NAME_SPACE + FQN.CONTEXT_SEPARATOR + cpu.getId());
 	}
 
 	public FQN buildFQN(Disk disk) {
 		return new FQN(buildFQN(disk.getVEEReplica()) + FQN.CONTEXT_SEPARATOR + DISKS_NAME_SPACE + FQN.CONTEXT_SEPARATOR + disk.getDiskConf().getFileSystem().getName());
 	}
 
 	public FQN buildFQN(Memory memory) {
 		return new FQN(buildFQN(memory.getVEEReplica()) + FQN.CONTEXT_SEPARATOR + MEMORY_NAME_SPACE); 
 	}
 
 	public FQN buildFQN(NIC nic) {
 		return new FQN(buildFQN(nic.getVEEReplica()) + FQN.CONTEXT_SEPARATOR + NICS_NAME_SPACE + FQN.CONTEXT_SEPARATOR +  nic.getId()); 
 	}
 
 	public FQN buildFQN(Network network) {
 		return new FQN(buildFQN(network.getServiceApplication()) + FQN.CONTEXT_SEPARATOR + NETWORKS_NAME_SPACE + FQN.CONTEXT_SEPARATOR + network.getName());
 	}
 
 	public FQN buildFQN(ServiceKPI serviceKPI) {
 
 		logger.info("PONG serviceKPI.getKPIType() " + serviceKPI.getKPIType());
 		logger.info("PONG serviceKPI.getKPIVmname() " + serviceKPI.getKPIVmname());
 
		if (serviceKPI.getKPIType().equals("VEEHW") && !(serviceKPI.getKPIType().equals("null")) && !(serviceKPI.getKPIVmname().equals("null"))){
 			logger.info("PONG buildFQN = " + serviceKPI.getServiceApplication()+ ".vees." + serviceKPI.getKPIVmname()+ ".kpis." +  serviceKPI.getKPIName());
 			return new FQN(buildFQN(serviceKPI.getServiceApplication())+ ".vees." + serviceKPI.getKPIVmname() + ".kpis." +  serviceKPI.getKPIName());
 		}
 		else {
 			logger.info("PONG buildFQN = " + serviceKPI.getServiceApplication() + FQN.CONTEXT_SEPARATOR + SERVICE_KPIS_NAME_SPACE + FQN.CONTEXT_SEPARATOR + serviceKPI.getKPIName());
 			return new FQN(buildFQN(serviceKPI.getServiceApplication()) + FQN.CONTEXT_SEPARATOR + SERVICE_KPIS_NAME_SPACE + FQN.CONTEXT_SEPARATOR + serviceKPI.getKPIName());
 		}
 	}
 
 	public FQN buildFQN(ProbeKPI probeKPI) {
 		return new FQN(buildFQN(probeKPI.getServiceApplication()) + FQN.CONTEXT_SEPARATOR + SERVICE_KPIS_NAME_SPACE + FQN.CONTEXT_SEPARATOR + probeKPI.getKPIName());
 	}
 
 	public FQN buildFQN(Rule rule) {
 		return new FQN(buildFQN(rule.getServiceApplication()) + FQN.CONTEXT_SEPARATOR + RULES_KPIS_NAME_SPACE + FQN.CONTEXT_SEPARATOR + rule.getName());
 	}
 }
