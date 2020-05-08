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
 package org.ow2.sirocco.apis.rest.cimi.converter;
 
 import org.ow2.sirocco.apis.rest.cimi.domain.CimiResource;
 import org.ow2.sirocco.apis.rest.cimi.domain.ExchangeType;
 import org.ow2.sirocco.apis.rest.cimi.domain.Operation;
 import org.ow2.sirocco.apis.rest.cimi.domain.TargetResource;
 import org.ow2.sirocco.apis.rest.cimi.request.CimiContext;
 import org.ow2.sirocco.cloudmanager.model.cimi.CloudEntity;
 import org.ow2.sirocco.cloudmanager.model.cimi.CloudResource;
 import org.ow2.sirocco.cloudmanager.model.cimi.Job;
 import org.ow2.sirocco.cloudmanager.model.cimi.Machine;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineImage;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineNetworkInterface;
 import org.ow2.sirocco.cloudmanager.model.cimi.MachineTemplateNetworkInterface;
 import org.ow2.sirocco.cloudmanager.model.cimi.Network;
 import org.ow2.sirocco.cloudmanager.model.cimi.Network.Type;
 import org.ow2.sirocco.cloudmanager.model.cimi.NetworkPort;
 import org.ow2.sirocco.cloudmanager.model.cimi.Resource;
 import org.ow2.sirocco.cloudmanager.model.cimi.Volume;
 import org.ow2.sirocco.cloudmanager.model.cimi.VolumeImage;
 import org.ow2.sirocco.cloudmanager.model.cimi.event.Event;
 import org.ow2.sirocco.cloudmanager.model.cimi.event.EventLogTemplate;
 import org.ow2.sirocco.cloudmanager.model.cimi.system.ComponentDescriptor.ComponentType;
 import org.ow2.sirocco.cloudmanager.model.cimi.system.System;
 
 /**
  * 
  */
 public class ConverterHelper {
 
     public static Float toFloat(final Integer value) {
         Float converted = null;
         if (null != value) {
             converted = value.floatValue();
         }
         return converted;
     }
 
     public static Integer toInteger(final Float value) {
         Integer converted = null;
         if (null != value) {
             converted = value.intValue();
         }
         return converted;
     }
 
     public static String toString(final Job.Status value) {
         String converted = null;
         if (null != value) {
             converted = value.toString();
         }
         return converted;
     }
 
     public static Machine.State toMachineState(final String value) {
         Machine.State converted = null;
         if (null != value) {
             Machine.State[] allEnums = Machine.State.values();
             for (Machine.State myEnum : allEnums) {
                 if (true == value.equalsIgnoreCase(myEnum.toString())) {
                     converted = myEnum;
                     break;
                 }
             }
         }
         return converted;
     }
 
     public static String toString(final Machine.State value) {
         String converted = null;
         if (null != value) {
             converted = value.toString();
         }
         return converted;
     }
 
     public static String toString(final MachineImage.State value) {
         String converted = null;
         if (null != value) {
             converted = value.toString();
         }
         return converted;
     }
 
     public static String toString(final MachineImage.Type value) {
         String converted = null;
         if (null != value) {
             converted = value.toString();
         }
         return converted;
     }
 
     public static String toString(final MachineNetworkInterface.InterfaceState value) {
         String converted = null;
         if (null != value) {
            converted = value.toString();
         }
         return converted;
     }
 
     public static MachineNetworkInterface.InterfaceState toMachineNetworkInterfaceState(final String value) {
         MachineNetworkInterface.InterfaceState converted = null;
         if (null != value) {
             MachineNetworkInterface.InterfaceState[] allEnums = MachineNetworkInterface.InterfaceState.values();
             for (MachineNetworkInterface.InterfaceState myEnum : allEnums) {
                 if (true == value.equalsIgnoreCase(myEnum.toString())) {
                     converted = myEnum;
                     break;
                 }
             }
         }
         return converted;
     }
 
     public static String toString(final MachineTemplateNetworkInterface.InterfaceState value) {
         String converted = null;
         if (null != value) {
             converted = value.toString();
         }
         return converted;
     }
 
     public static MachineTemplateNetworkInterface.InterfaceState toMachineTemplateNetworkInterfaceState(final String value) {
         MachineTemplateNetworkInterface.InterfaceState converted = null;
         if (null != value) {
             MachineTemplateNetworkInterface.InterfaceState[] allEnums = MachineTemplateNetworkInterface.InterfaceState.values();
             for (MachineTemplateNetworkInterface.InterfaceState myEnum : allEnums) {
                 if (true == value.equalsIgnoreCase(myEnum.toString())) {
                     converted = myEnum;
                     break;
                 }
             }
         }
         return converted;
     }
 
     public static String toString(final Network.State value) {
         String converted = null;
         if (null != value) {
             converted = value.toString();
         }
         return converted;
     }
 
     public static String toString(final Network.Type value) {
         String converted = null;
         if (null != value) {
             converted = value.toString();
         }
         return converted;
     }
 
     public static Network.Type toNetworkType(final String value) {
         Network.Type converted = null;
         if (null != value) {
             Network.Type[] allEnums = Network.Type.values();
             for (Type myEnum : allEnums) {
                 if (true == value.equalsIgnoreCase(myEnum.toString())) {
                     converted = myEnum;
                     break;
                 }
             }
         }
         return converted;
     }
 
     public static String toString(final NetworkPort.State value) {
         String converted = null;
         if (null != value) {
             converted = value.toString();
         }
         return converted;
     }
 
     public static String toString(final Volume.State value) {
         String converted = null;
         if (null != value) {
             converted = value.toString();
         }
         return converted;
     }
 
     public static String toString(final VolumeImage.State value) {
         String converted = null;
         if (null != value) {
             converted = value.toString();
         }
         return converted;
     }
 
     public static String toString(final System.State value) {
         String converted = null;
         if (null != value) {
             converted = value.toString();
         }
         return converted;
     }
 
     public static String toString(final Event.Outcome value) {
         String converted = null;
         if (null != value) {
             converted = value.toString();
         }
         return converted;
     }
 
     public static String toString(final Event.Severity value) {
         String converted = null;
         if (null != value) {
             converted = value.toString();
         }
         return converted;
     }
 
     public static String toString(final EventLogTemplate.Persistence value) {
         String converted = null;
         if (null != value) {
             converted = value.toString();
         }
         return converted;
     }
 
     public static EventLogTemplate.Persistence toEventLogTemplatePersistence(final String value) {
         EventLogTemplate.Persistence converted = null;
         if (null != value) {
             EventLogTemplate.Persistence[] allEnums = EventLogTemplate.Persistence.values();
             for (EventLogTemplate.Persistence myEnum : allEnums) {
                 if (true == value.equalsIgnoreCase(myEnum.toString())) {
                     converted = myEnum;
                     break;
                 }
             }
         }
         return converted;
     }
 
     public static Operation toOperation(final String value) {
         Operation converted = null;
         if (null != value) {
             Operation[] allEnums = Operation.values();
             for (Operation myEnum : allEnums) {
                 if (true == value.equalsIgnoreCase(myEnum.toString())) {
                     converted = myEnum;
                     break;
                 }
             }
         }
         return converted;
     }
 
     /**
      * Convert a component type into CIMI type URI.
      * 
      * @param value The component type
      * @return The CIMI type URI
      * @throws InvalidConversionException If unable to convert the component
      *         type into CIMI type URI
      */
     public static String toString(final ComponentType value) {
         String converted = null;
         if (null != value) {
             if (true == value.equals(ComponentType.CREDENTIALS)) {
                 converted = ExchangeType.Credential.getResourceURI();
             } else if (true == value.equals(ComponentType.MACHINE)) {
                 converted = ExchangeType.Machine.getResourceURI();
             } else if (true == value.equals(ComponentType.NETWORK)) {
                 converted = ExchangeType.Network.getResourceURI();
             } else if (true == value.equals(ComponentType.SYSTEM)) {
                 converted = ExchangeType.System.getResourceURI();
             } else if (true == value.equals(ComponentType.VOLUME)) {
                 converted = ExchangeType.Volume.getResourceURI();
             } else {
                 throw new InvalidConversionException(
                     "Unable to convert the component type into CIMI type URI: Component Type = " + value.toString());
             }
         }
         return converted;
     }
 
     /**
      * Convert a CIMI type URI into component type.
      * 
      * @param typeURI The CIMI type URI
      * @return The component type
      * @throws InvalidConversionException If unable to convert the URI into
      *         component type
      */
     public static ComponentType toComponentType(final String typeURI) {
         ComponentType converted = null;
         if (null != typeURI) {
             if (true == typeURI.equalsIgnoreCase(ExchangeType.Credential.getResourceURI())) {
                 converted = ComponentType.CREDENTIALS;
             } else if (true == typeURI.equalsIgnoreCase(ExchangeType.Machine.getResourceURI())) {
                 converted = ComponentType.MACHINE;
             } else if (true == typeURI.equalsIgnoreCase(ExchangeType.Network.getResourceURI())) {
                 converted = ComponentType.NETWORK;
             } else if (true == typeURI.equalsIgnoreCase(ExchangeType.System.getResourceURI())) {
                 converted = ComponentType.SYSTEM;
             } else if (true == typeURI.equalsIgnoreCase(ExchangeType.Volume.getResourceURI())) {
                 converted = ComponentType.VOLUME;
             } else {
                 throw new InvalidConversionException("Unable to convert the URI into component type: URI = " + typeURI);
             }
         }
         return converted;
     }
 
     /**
      * Find the associated CIMI class in the configuration to build a target
      * resource with the ID of an unknown service resource.
      * 
      * @param context The current context
      * @param targetDataService The service resource
      * @return The CIMI target
      */
     public static TargetResource buildTargetResource(final CimiContext context, final Resource targetDataService) {
         TargetResource target = null;
         if (null != targetDataService) {
             target = new TargetResource(ConverterHelper.buildHrefTargetResource(context, targetDataService));
         }
         return target;
     }
 
     /**
      * Find the associated CIMI class in the configuration to build a reference
      * HREF with the ID of an unknown service resource.
      * 
      * @param context The current context
      * @param targetDataService The service resource
      * @return The CIMI HREF
      */
     protected static String buildHrefTargetResource(final CimiContext context, final Resource targetDataService) {
         String href = null;
         if (null != targetDataService) {
             Class<? extends CimiResource> targetType = context.findAssociate(targetDataService.getClass());
             href = context.makeHref(targetType, ConverterHelper.getTargetId(targetDataService).toString());
         }
         return href;
     }
 
     /**
      * Get ID of an unknown service resource.
      * 
      * @param targetDataService The service resource
      * @return Th service resource ID
      */
     protected static Integer getTargetId(final Resource targetDataService) {
         Integer id = null;
         if (true == CloudResource.class.isAssignableFrom(targetDataService.getClass())) {
             id = ((CloudResource) targetDataService).getId();
         } else {
             id = ((CloudEntity) targetDataService).getId();
         }
         return id;
     }
 
     /**
      * Build a resource URI for an unknown service resource.
      * 
      * @param context The current context
      * @param targetDataService The service resource
      * @return The CIMI resource URI
      */
     public static String buildResourceUri(final CimiContext context, final Resource targetDataService) {
         String uri = null;
         if (null != targetDataService) {
             Class<? extends CimiResource> targetType = context.findAssociate(targetDataService.getClass());
             ExchangeType type = context.getType(targetType);
             if (null != type) {
                 uri = type.getResourceURI();
             }
         }
         return uri;
     }
 }
