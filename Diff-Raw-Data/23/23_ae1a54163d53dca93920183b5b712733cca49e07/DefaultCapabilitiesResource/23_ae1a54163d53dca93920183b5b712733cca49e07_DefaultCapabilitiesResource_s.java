 /*
  * Copyright © 2010 Red Hat, Inc.
  *
  * This is free software; you can redistribute it and/or modify it
  * under the terms of the GNU Lesser General Public License as
  * published by the Free Software Foundation; either version 2.1 of
  * the License, or (at your option) any later version.
  *
  * This software is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this software; if not, write to the Free
  * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
  * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
  */
 package com.redhat.rhevm.api.common.resource;
 
 import com.redhat.rhevm.api.model.Capabilities;
 import com.redhat.rhevm.api.model.CPU;
 import com.redhat.rhevm.api.model.CPUs;
 import com.redhat.rhevm.api.model.PowerManagement;
 import com.redhat.rhevm.api.model.PowerManagementOption;
 import com.redhat.rhevm.api.model.PowerManagementOptions;
 import com.redhat.rhevm.api.model.PowerManagers;
 import com.redhat.rhevm.api.model.VersionCaps;
 import com.redhat.rhevm.api.resource.CapabilitiesResource;
 
 public class DefaultCapabilitiesResource implements CapabilitiesResource {
 
     private final VersionCaps VERSION22 = buildVersion(2, 2, true);
     private final VersionCaps VERSION21 = buildVersion(2, 1, false);
 
     private VersionCaps buildVersion(int major, int minor, boolean current) {
         VersionCaps version = new VersionCaps();
         version.setMajor(major);
         version.setMinor(minor);
         version.setCPUs(new CPUs());
         if (current) {
             version.setCurrent(true);
         }
         return version;
     }
 
     private CPUs cpus = new CPUs();
 
     {
         addCpu(VERSION21, "Intel Xeon w/o XD/NX",  2);
         addCpu(VERSION21, "Intel Xeon",            3);
         addCpu(VERSION22, "Intel Xeon Core2",      4);
         addCpu(VERSION22, "Intel Xeon 45nm Core2", 5);
         addCpu(VERSION22, "Intel Xeon Core i7",    6);
 
         addCpu(VERSION21, "AMD Opteron G1 w/o NX", 2);
         addCpu(VERSION21, "AMD Opteron G1",        3);
         addCpu(VERSION22, "AMD Opteron G2",        4);
         addCpu(VERSION22, "AMD Opteron G3",        5);
     }
 
     private void addCpu(VersionCaps version, String id, int level) {
         CPU cpu = new CPU();
 
         cpu.setId(id);
         cpu.setLevel(level);
 
         version.getCPUs().getCPUs().add(cpu);
 
         if (version == VERSION21) {
             addCpu(VERSION22, id, level);
         }
     }
 
     private PowerManagers powerManagers = new PowerManagers();
 
     {
        addPowerManager("alom", "secure=secure,port=ipport");
        addPowerManager("apc", "secure=secure,port=ipport,slot=port");
        addPowerManager("bladecenter", "secure=secure,port=ipport,slot=port");
        addPowerManager("drac5", "secure=secure,port=ipport");
        addPowerManager("eps", "slot=port");
        addPowerManager("ilo", "secure=ssl,port=ipport");
         addPowerManager("ipmilan", "");
        addPowerManager("rsa", "secure=secure,port=ipport");
         addPowerManager("rsb", "");
        addPowerManager("wti", "secure=secure,port=ipport,slot=port");
     }
 
     private void addPowerManager(String type, String options) {
         addPowerManager(VERSION21, type, options);
         addPowerManager(VERSION22, type, options);
     }
 
     private void addPowerManager(VersionCaps version, String type, String options) {
         PowerManagement powerManagement = new PowerManagement();
 
         powerManagement.setType(type);
 
         powerManagement.setOptions(new PowerManagementOptions());
 
         String[] opts = options.split(",");
         for (int i = 0; i < opts.length; i++) {
             if (opts[i].isEmpty()) {
                 continue;
             }
 
             String[] parts = opts[i].split("=");
 
             PowerManagementOption option = new PowerManagementOption();
             option.setName(parts[0]);
            option.setValue(parts[1]);
             powerManagement.getOptions().getOptions().add(option);
         }
 
         if (!version.isSetPowerManagers()) {
             version.setPowerManagers(new PowerManagers());
         }
         version.getPowerManagers().getPowerManagers().add(powerManagement);
     }
 
     public Capabilities get() {
         Capabilities caps = new Capabilities();
         caps.getVersions().add(VERSION22);
         caps.getVersions().add(VERSION21);
         return caps;
     }
 }
