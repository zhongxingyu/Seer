 /**
  *
  * SIROCCO
  * Copyright (C) 2011 France Telecom
  * Contact: sirocco@ow2.org
  *
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
  * USA
  *
  * $Id$
  *
  */
 package org.ow2.sirocco.cimi.domain;
 
 import java.util.List;
 
 import org.ow2.sirocco.cimi.server.utils.ConstantsPath;
 
 /**
  * Enumeration of all the resource exchanged between the customer and the
  * producer.
  */
 public enum ExchangeType {
     /** */
     Action(),
     /** */
     Address(PathType.Address), AddressCollection(PathType.Address, false),
     /** */
     AddressCreate(PathType.Address),
     /** */
     AddressTemplate(PathType.AddressTemplate), AddressTemplateCollection(PathType.AddressTemplate, false),
     /** */
     CloudEntryPoint(PathType.CloudEntryPoint, false),
     /** */
     Credential(PathType.Credential), CredentialCollection(PathType.Credential, false), CredentialCreate(PathType.Credential),
     /** */
     CredentialTemplate(PathType.CredentialTemplate), CredentialTemplateCollection(PathType.CredentialTemplate, false),
     /** */
     Disk(PathType.MachineDisk), DiskCollection(PathType.MachineDisk, false),
     /** */
     Event(PathType.Event), EventCollection(PathType.Event, false),
     /** */
     EventLog(PathType.EventLog), EventLogCollection(PathType.EventLog, false),
     /** */
     EventLogCreate(PathType.EventLog),
     /** */
     EventLogTemplate(PathType.EventLogTemplate), EventLogTemplateCollection(PathType.EventLogTemplate, false),
     /** */
     ForwardingGroup(PathType.ForwardingGroup), ForwardingGroupCollection(PathType.ForwardingGroup, false),
     /** */
     ForwardingGroupCreate(PathType.ForwardingGroup),
     /** */
     ForwardingGroupNetwork(PathType.ForwardingGroupNetwork), ForwardingGroupNetworkCollection(PathType.ForwardingGroupNetwork,
         false),
     /** */
     ForwardingGroupTemplate(PathType.ForwardingGroupTemplate), ForwardingGroupTemplateCollection(
         PathType.ForwardingGroupTemplate, false),
     /** */
     Job(PathType.Job), JobCollection(PathType.Job, false),
     /** */
     Machine(PathType.Machine), MachineCollection(PathType.Machine, false),
     /** */
     MachineCreate(PathType.Machine),
     /** */
     MachineConfiguration(PathType.MachineConfiguration), MachineConfigurationCollection(PathType.MachineConfiguration, false),
     /** */
     MachineImage(PathType.MachineImage), MachineImageCollection(PathType.MachineImage, false),
     /** */
     MachineNetworkInterface(PathType.MachineNetworkInterface), MachineNetworkInterfaceCollection(
         PathType.MachineNetworkInterface, false),
     /** */
     MachineNetworkInterfaceAddress(PathType.MachineNetworkInterfaceAddress), MachineNetworkInterfaceAddressCollection(
         PathType.MachineNetworkInterfaceAddress, false),
     /** */
     MachineTemplate(PathType.MachineTemplate), MachineTemplateCollection(PathType.MachineTemplate, false),
     /** */
     MachineVolume(PathType.MachineVolume), MachineVolumeCollection(PathType.MachineVolume, false),
     /** */
     Network(PathType.Network), NetworkCollection(PathType.Network, false),
     /** */
     NetworkCreate(PathType.Network),
     /** */
     NetworkNetworkPort(PathType.NetworkNetworkPort), NetworkNetworkPortCollection(PathType.NetworkNetworkPort, false),
     /** */
     NetworkConfiguration(PathType.NetworkConfiguration), NetworkConfigurationCollection(PathType.NetworkConfiguration, false),
     /** */
     NetworkTemplate(PathType.NetworkTemplate), NetworkTemplateCollection(PathType.NetworkTemplate, false),
     /** */
     NetworkPort(PathType.NetworkPort), NetworkPortCollection(PathType.NetworkPort, false),
     /** */
     NetworkPortCreate(PathType.NetworkPort),
     /** */
     NetworkPortConfiguration(PathType.NetworkPortConfiguration), NetworkPortConfigurationCollection(
         PathType.NetworkPortConfiguration, false),
     /** */
     NetworkPortTemplate(PathType.NetworkPortTemplate), NetworkPortTemplateCollection(PathType.NetworkPortTemplate, false),
     /** */
     ResourceMetadata(PathType.ResourceMetadata), ResourceMetadataCollection(PathType.ResourceMetadata, false),
     /** */
     System(PathType.System), SystemCollection(PathType.System, false),
     /** */
     SystemCreate(PathType.System),
     /** */
     SystemCredential(PathType.SystemCredential), SystemCredentialCollection(PathType.SystemCredential, false),
     /** */
     SystemMachine(PathType.SystemMachine), SystemMachineCollection(PathType.SystemMachine, false),
     /** */
     SystemSystem(PathType.SystemSystem), SystemSystemCollection(PathType.SystemSystem, false),
     /** */
     SystemNetwork(PathType.SystemNetwork), SystemNetworkCollection(PathType.SystemNetwork, false),
     /** */
     SystemNetworkPort(PathType.SystemNetworkPort), SystemNetworkPortCollection(PathType.SystemNetworkPort, false),
     /** */
     SystemAddress(PathType.SystemAddress), SystemAddressCollection(PathType.SystemAddress, false),
     /** */
     SystemForwardingGroup(PathType.SystemForwardingGroup), SystemForwardingGroupCollection(PathType.SystemForwardingGroup,
         false),
     /** */
     SystemVolume(PathType.SystemVolume), SystemVolumeCollection(PathType.SystemVolume, false),
     /** */
     SystemTemplate(PathType.SystemTemplate), SystemTemplateCollection(PathType.SystemTemplate, false),
     /** */
     Volume(PathType.Volume), VolumeCollection(PathType.Volume, false),
     /** */
     VolumeCreate(PathType.Volume),
     /** */
     VolumeVolumeImage(PathType.VolumeVolumeImage), VolumeVolumeImageCollection(PathType.VolumeVolumeImage, false),
     /** */
     VolumeConfiguration(PathType.VolumeConfiguration), VolumeConfigurationCollection(PathType.VolumeConfiguration, false),
     /** */
     VolumeImage(PathType.VolumeImage), VolumeImageCollection(PathType.VolumeImage, false),
     /** */
     VolumeTemplate(PathType.VolumeTemplate), VolumeTemplateCollection(PathType.VolumeTemplate, false),
     /** */
     MachineTemplateVolume(Volume), MachineTemplateVolumeTemplate(VolumeTemplate);
 
     /** The path type of the resource. */
     PathType pathType;
 
     /** Flag ID in reference. */
     boolean idInReference;
 
     /**
      * The substitute resource : the Path Type and ResourceUri will be that of
      * substitute type.
      */
     ExchangeType substituteType;
 
     /** Constructor. */
     private ExchangeType() {
         this.idInReference = true;
     }
 
     /** Constructor. */
     private ExchangeType(final PathType pathType) {
         this.pathType = pathType;
         this.idInReference = true;
     }
 
     /** Constructor. */
     private ExchangeType(final PathType pathType, final boolean idInReference) {
         this.pathType = pathType;
         this.idInReference = idInReference;
     }
 
     /** Constructor. */
     private ExchangeType(final ExchangeType substituteType) {
         this.substituteType = substituteType;
         this.pathType = this.substituteType.getPathType();
         this.idInReference = this.substituteType.hasIdInReference();
     }
 
     /**
      * Get the URI of the resource.
      * 
      * @return The URI
      */
     public String getResourceURI() {
         StringBuilder sb = new StringBuilder();
         sb.append(ConstantsPath.CIMI_XML_NAMESPACE).append('/');
         if (null == this.substituteType) {
             sb.append(this.name());
         } else {
             sb.append(this.substituteType.name());
         }
         return sb.toString();
     }
 
     /**
      * Get the path type of the resource.
      * 
      * @return The path type
      */
     public PathType getPathType() {
         return this.pathType;
     }
 
     /**
      * Get the substitute type of the resource.
      * 
      * @return The substitute type or null if not defined
      */
     public ExchangeType getSubstituteType() {
         return this.substituteType;
     }
 
     /**
      * Get the pathname of the resource.
      * 
      * @return The path
      */
     public String getPathname() {
         return this.pathType.getPathname();
     }
 
     /**
      * Get the flag "ID in reference".
      * 
      * @return True if the type must be a ID in reference
      */
     public boolean hasIdInReference() {
         return this.idInReference;
     }
 
     /**
      * Get the flag {@link PathType} Parent.
      * 
      * @return True if the current PathType has a parent
      */
     public boolean hasParent() {
         return this.pathType.hasParent();
     }
 
     /**
      * Make a HREF for the current type.
      * 
      * @param baseUri The base URI
      * @param ids All ID necessary : the first is a ID parent, the last is
      *        current ID
      * @return
      */
     public String makeHref(final String baseUri, final String... ids) {
         StringBuilder sb = new StringBuilder();
         sb.append(baseUri);
         List<String> paths = this.getPathType().getPaths();
         for (int i = 0; i < paths.size(); i++) {
             if (i > 0) {
                 sb.append('/');
             }
             sb.append(paths.get(i));
             if ((i < (paths.size() - 1)) || (true == this.hasIdInReference())) {
                 sb.append('/');
                 if (i < ids.length) {
                     sb.append(ids[i]);
                 } else {
                     sb.append('*');
                 }
             }
         }
         return sb.toString();
     }
 
     /**
      * Make a HREF for the current type.
      * 
      * @param baseUri The base URI
      * @return
      */
     public String makeHrefPattern(final String baseUri) {
         StringBuilder sb = new StringBuilder();
         sb.append('^').append(baseUri);
         List<String> paths = this.getPathType().getPaths();
         for (int i = 0; i < paths.size(); i++) {
             if (i > 0) {
                 sb.append('/');
             }
             sb.append(paths.get(i));
             if ((i < (paths.size() - 1)) || (true == this.hasIdInReference())) {
                sb.append('/').append("([a-f0-9-]+){1}");
             }
         }
         sb.append('$');
         return sb.toString();
     }
 }
