 /**
  * Copyright (c) 2012, SURFnet BV
  * All rights reserved.
  *
  * Redistribution and use in source and binary forms, with or without modification, are permitted provided that the
  * following conditions are met:
  *
  *   * Redistributions of source code must retain the above copyright notice, this list of conditions and the following
  *     disclaimer.
  *   * Redistributions in binary form must reproduce the above copyright notice, this list of conditions and the following
  *     disclaimer in the documentation and/or other materials provided with the distribution.
  *   * Neither the name of the SURFnet BV nor the names of its contributors may be used to endorse or promote products
  *     derived from this software without specific prior written permission.
  *
  * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED WARRANTIES,
  * INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
  * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT HOLDER OR CONTRIBUTORS BE LIABLE FOR ANY DIRECT, INDIRECT, INCIDENTAL,
  * SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR
  * SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY,
  * WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
  * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
  */
 package nl.surfnet.bod.nbi.mtosi;
 
 import static nl.surfnet.bod.nbi.mtosi.MtosiUtils.createNamingAttrib;
 import static nl.surfnet.bod.nbi.mtosi.MtosiUtils.createNamingAttributeType;
 import static nl.surfnet.bod.nbi.mtosi.MtosiUtils.createRdn;
 import static nl.surfnet.bod.nbi.mtosi.MtosiUtils.createServiceCharacteristicsAndAddToList;
 
 import java.util.List;
 
 import javax.xml.bind.JAXBElement;
 
 import nl.surfnet.bod.domain.PhysicalPort;
 import nl.surfnet.bod.domain.ProtectionType;
 import nl.surfnet.bod.domain.Reservation;
 import nl.surfnet.bod.util.XmlUtils;
 
 import org.joda.time.DateTime;
 import org.tmforum.mtop.fmw.xsd.gen.v1.AnyListType;
 import org.tmforum.mtop.fmw.xsd.nam.v1.NamingAttributeType;
 import org.tmforum.mtop.fmw.xsd.nam.v1.RelativeDistinguishNameType;
 import org.tmforum.mtop.sa.xsd.scai.v1.ReserveRequest;
 import org.tmforum.mtop.sb.xsd.svc.v1.*;
 
 import com.ciena.mtop.tmw.xsd.coi.v1.Nvs;
 import com.google.common.annotations.VisibleForTesting;
 import com.google.common.base.Optional;
 
 public class ReserveRequestBuilder {
 
   @VisibleForTesting
   static final String SSC = "SSC";
   static final String MANAGING_DOMAIN = "CIENA/OneControl";
   static final String TRAFFIC_MAPPING_TABLECOUNT = "1";
   static final String TRAFFIC_MAPPING_FROM_TABLE_PRIORITY = "all";
   static final String TRAFFIC_MAPPING_TO_TABLE_TRAFFICCLASS = "5";
 
   public ReserveRequest createReservationRequest(Reservation reservation, boolean autoProvision, long sequence) {
     ReserveRequest reserveRequest = createReserveRequest(reservation.getEndDateTime());
 
     ResourceFacingServiceType rfsCreateData = createRfsCreateData(reservation, sequence);
 
     createDescribedByList(rfsCreateData.getDescribedByList(), reservation.getStartDateTime());
 
     // Source port
     PhysicalPort sourcePhysicalPort = reservation.getSourcePort().getPhysicalPort();
     ServiceAccessPointType sourceSAP = addStaticCharacteristicsTo(rfsCreateData.getSapList(), sourcePhysicalPort, sequence);
     addDynamicCharacteristicsTo(Optional.<Integer> fromNullable(reservation.getSourcePort().getVlanId()), reservation
         .getProtectionType(), reservation.getBandwidth(), sourcePhysicalPort.getPortType(), sourceSAP
         .getDescribedByList());
 
     // Destination port
     PhysicalPort destinationPhysicalPort = reservation.getDestinationPort().getPhysicalPort();
     ServiceAccessPointType destinationSAP = addStaticCharacteristicsTo(rfsCreateData.getSapList(),
         destinationPhysicalPort, sequence);
     addDynamicCharacteristicsTo(Optional.<Integer> fromNullable(reservation.getDestinationPort().getVlanId()),
         reservation.getProtectionType(), reservation.getBandwidth(), destinationPhysicalPort.getPortType(),
         destinationSAP.getDescribedByList());
 
     // Vendor extensions
     createVendorExtensionsAndAdd(rfsCreateData, reservation.getStartDateTime());
     reserveRequest.setRfsCreateData(rfsCreateData);
 
     return reserveRequest;
   }
 
   private void createVendorExtensionsAndAdd(ResourceFacingServiceType rfsCreateData, DateTime startDateTime) {
     AnyListType anyListType = new org.tmforum.mtop.fmw.xsd.gen.v1.ObjectFactory().createAnyListType();
     List<Object> anyList = anyListType.getAny();
 
     Nvs nvs = new Nvs();
     nvs.setName("startTime");
     nvs.setValue(convertToXml(startDateTime));
     anyList.add(nvs);
 
     JAXBElement<AnyListType> vendorExtensions = new org.tmforum.mtop.fmw.xsd.coi.v1.ObjectFactory()
         .createCommonObjectInfoTypeVendorExtensions(anyListType);
 
     rfsCreateData.setVendorExtensions(vendorExtensions);
   }
 
   @VisibleForTesting
   ServiceAccessPointType addStaticCharacteristicsTo(List<ServiceAccessPointType> sapList,
       PhysicalPort physicalPort, long sequence) {
    
     ServiceAccessPointType serviceAccessPoint = createServiceAccessPoint(physicalPort, sequence);
     sapList.add(serviceAccessPoint);
 
     createServiceCharacteristicsAndAddToList(TRAFFIC_MAPPING_TABLECOUNT, createNamingAttrib(SSC,
         "TrafficMappingTableCount"), serviceAccessPoint.getDescribedByList());
 
     createServiceCharacteristicsAndAddToList(TRAFFIC_MAPPING_FROM_TABLE_PRIORITY, createNamingAttrib(SSC,
         "TrafficMappingFrom_Table_Priority"), serviceAccessPoint.getDescribedByList());
 
     createServiceCharacteristicsAndAddToList(TRAFFIC_MAPPING_TO_TABLE_TRAFFICCLASS, createNamingAttrib(SSC,
         "TrafficMappingTo_Table_TrafficClass"), serviceAccessPoint.getDescribedByList());
 
     return serviceAccessPoint;
   }
 
   @VisibleForTesting
   void addDynamicCharacteristicsTo(Optional<Integer> vlandId, ProtectionType protectionType, Integer bandwidth,
       String portType, List<ServiceCharacteristicValueType> describedByList) {
 
     if (vlandId.isPresent()) {
       createServiceCharacteristicsAndAddToList("EVPL", createNamingAttrib(SSC, "ServiceType"), describedByList);
       createServiceCharacteristicsAndAddToList(String.valueOf(vlandId.get()), createNamingAttrib(SSC,
           "TrafficMappingFrom_Table_VID"), describedByList);
     }
     else {
       createServiceCharacteristicsAndAddToList("EPL", createNamingAttrib(SSC, "ServiceType"), describedByList);
       createServiceCharacteristicsAndAddToList("all", createNamingAttrib(SSC, "TrafficMappingFrom_Table_VID"),
           describedByList);
     }
 
     createServiceCharacteristicsAndAddToList(portType, createNamingAttrib(SSC, "InterfaceType"), describedByList);
 
     createServiceCharacteristicsAndAddToList(bandwidth.toString(), createNamingAttrib(SSC,
         "TrafficMappingTo_Table_IngressCIR"), describedByList);
 
     createServiceCharacteristicsAndAddToList(protectionType.getMtosiName(), createNamingAttrib(SSC, "ProtectionLevel"),
         describedByList);
   }
 
   private org.tmforum.mtop.sa.xsd.scai.v1.ReserveRequest createReserveRequest(DateTime endDateTime) {
     ReserveRequest reserveRequest = new org.tmforum.mtop.sa.xsd.scai.v1.ObjectFactory().createReserveRequest();
 
     reserveRequest.setExpiringTime(XmlUtils.getXmlTimeStampFromDateTime(endDateTime).get());
 
     return reserveRequest;
   }
 
   @VisibleForTesting
   ResourceFacingServiceType createRfsCreateData(Reservation reservation, long sequence) {
     ResourceFacingServiceType rfsData = new org.tmforum.mtop.sb.xsd.svc.v1.ObjectFactory()
         .createResourceFacingServiceType();
 
    rfsData.setName(createNamingAttributeType("RFS", reservation.getReservationId()+":"+sequence));
     rfsData.setIsMandatory(true);
     rfsData.setIsStateful(true);
     rfsData.setAdminState(AdminStateType.UNLOCKED);
     rfsData.setServiceState(ServiceStateType.RESERVED);
 
     return rfsData;
   }
 
   private void createDescribedByList(List<ServiceCharacteristicValueType> describedByList, DateTime timeStamp) {
     createServiceCharacteristicsAndAddToList(convertToXml(timeStamp), createNamingAttrib(SSC, "startTime"),
         describedByList);
 
     createServiceCharacteristicsAndAddToList("Strict", createNamingAttrib(SSC, "AdmissionControl"), describedByList);
   }
 
   public static String convertToXml(DateTime timeStamp) {
     return XmlUtils.getXmlTimeStampFromDateTime(timeStamp).get().toXMLFormat();
   }
 
   
   @VisibleForTesting
   ServiceAccessPointType createServiceAccessPoint(PhysicalPort port, final long sequence) {
     NamingAttributeType resourceRef = createNamingAttrib();
     List<RelativeDistinguishNameType> resourceRefList = resourceRef.getRdn();
 
     resourceRefList.add(createRdn("MD", MANAGING_DOMAIN));
     resourceRefList.add(createRdn("ME", port.getNmsNeId()));
     resourceRefList.add(createRdn("PTP", MtosiUtils.extractPTPFromNmsPortId(port.getNmsPortId())));
    
    
     // TODO: Where to get the CTP value?
    resourceRefList.add(createRdn("CTP", "/dummy-"+sequence));
 
     ServiceAccessPointType serviceAccessPoint = new org.tmforum.mtop.sb.xsd.svc.v1.ObjectFactory()
         .createServiceAccessPointType();
    serviceAccessPoint.setName(createNamingAttributeType("SAP", port.getNmsSapName()+"-"+sequence));
     serviceAccessPoint.setResourceRef(resourceRef);
 
     return serviceAccessPoint;
   }
 
   
}
