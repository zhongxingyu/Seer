 package net.es.topology.common.converter.nml;
 
 import net.es.topology.common.records.ts.*;
 import net.es.topology.common.records.ts.NetworkObject;
 import net.es.topology.common.visitors.sls.Visitor;
 import org.ogf.schemas.nml._2013._05.base.*;
 import org.ogf.schemas.nsi._2013._09.topology.NSARelationType;
 import org.ogf.schemas.nsi._2013._09.topology.NSAType;
 import org.ogf.schemas.nsi._2013._09.topology.NsiServiceRelationType;
 import org.ogf.schemas.nsi._2013._09.topology.NsiServiceType;
 
 import javax.xml.bind.DatatypeConverter;
 import javax.xml.datatype.DatatypeConfigurationException;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.XMLGregorianCalendar;
 import java.util.*;
 
 /**
  * @author <a href="mailto:a.hassany@gmail.com">Ahmed El-Hassany</a>
  */
 public class SLSVisitor implements Visitor {
     private org.ogf.schemas.nsi._2013._09.topology.ObjectFactory nsiFactory = new org.ogf.schemas.nsi._2013._09.topology.ObjectFactory();
     private org.ogf.schemas.nml._2013._05.base.ObjectFactory nmlFactory = new org.ogf.schemas.nml._2013._05.base.ObjectFactory();
     private Map<String, NSAType> nsaTypeMap = new HashMap<String, NSAType>();
     private Map<String, NsiServiceType> nsiServiceTypeMap = new HashMap<String, NsiServiceType>();
     private Map<String, TopologyType> topologyTypeMap = new HashMap<String, TopologyType>();
     private Map<String, PortType> portTypeMap = new HashMap<String, PortType>();
     private Map<String, PortGroupType> portGroupTypeMap = new HashMap<String, PortGroupType>();
     private Map<String, LinkType> linkTypeMap = new HashMap<String, LinkType>();
     private Map<String, LinkGroupType> linkGroupTypeMap = new HashMap<String, LinkGroupType>();
     private Map<String, NodeType> nodeTypeMap = new HashMap<String, NodeType>();
     private Map<String, BidirectionalLinkType> bidirectionalLinkTypeMap = new HashMap<String, BidirectionalLinkType>();
     private Map<String, BidirectionalPortType> bidirectionalPortTypeMap = new HashMap<String, BidirectionalPortType>();
     private Map<String, SwitchingServiceType> switchingServiceTypeMap = new HashMap<String, SwitchingServiceType>();
     private Map<String, AdaptationServiceType> adaptationServiceTypeMap = new HashMap<String, AdaptationServiceType>();
     private Map<String, DeadaptationServiceType> deadaptationServiceTypeMap = new HashMap<String, DeadaptationServiceType>();
     /**
      * to make sure each object serialized once.
      */
     private List<String> serializedURNS = new ArrayList<String>();
 
     /**
      * Helper method to get specific NML NetworkObject
      *
      * @param urn
      * @return
      */
     public org.ogf.schemas.nml._2013._05.base.NetworkObject getNetworkObject(String urn) {
         if (getNsaTypeMap().containsKey(urn)) {
             return getNsaTypeMap().get(urn);
         } else if (getNsiServiceTypeMap().containsKey(urn)) {
             return getNsiServiceTypeMap().get(urn);
         } else if (getTopologyTypeMap().containsKey(urn)) {
             return getTopologyTypeMap().get(urn);
         } else if (getPortGroupTypeMap().containsKey(urn)) {
             return getPortGroupTypeMap().get(urn);
         } else if (getPortTypeMap().containsKey(urn)) {
             return getPortTypeMap().get(urn);
         } else if (getLinkTypeMap().containsKey(urn)) {
             return getLinkTypeMap().get(urn);
         } else if (getLinkGroupTypeMap().containsKey(urn)) {
             return getLinkGroupTypeMap().get(urn);
         } else if (getNodeTypeMap().containsKey(urn)) {
             return getNodeTypeMap().get(urn);
         } else if (getSwitchingServiceTypeMap().containsKey(urn)) {
             return getSwitchingServiceTypeMap().get(urn);
         } else if (getAdaptationServiceTypeMap().containsKey(urn)) {
             return getAdaptationServiceTypeMap().get(urn);
         } else if (getDeadaptationServiceTypeMap().containsKey(urn)) {
             return getDeadaptationServiceTypeMap().get(urn);
         } else {
             return null;
         }
     }
 
     public Map<String, NSAType> getNsaTypeMap() {
         return nsaTypeMap;
     }
 
     public Map<String, NsiServiceType> getNsiServiceTypeMap() {
         return nsiServiceTypeMap;
     }
 
     public Map<String, TopologyType> getTopologyTypeMap() {
         return topologyTypeMap;
     }
 
     public Map<String, PortType> getPortTypeMap() {
         return portTypeMap;
     }
 
     public Map<String, PortGroupType> getPortGroupTypeMap() {
         return portGroupTypeMap;
     }
 
     public Map<String, LinkType> getLinkTypeMap() {
         return linkTypeMap;
     }
 
     public Map<String, LinkGroupType> getLinkGroupTypeMap() {
         return this.linkGroupTypeMap;
     }
 
     public Map<String, NodeType> getNodeTypeMap() {
         return nodeTypeMap;
     }
 
     public Map<String, BidirectionalLinkType> getBidirectionalLinkTypeMap() {
         return bidirectionalLinkTypeMap;
     }
 
     public Map<String, BidirectionalPortType> getBidirectionalPortTypeMap() {
         return bidirectionalPortTypeMap;
     }
 
     public Map<String, SwitchingServiceType> getSwitchingServiceTypeMap() {
         return switchingServiceTypeMap;
     }
 
     public Map<String, AdaptationServiceType> getAdaptationServiceTypeMap() {
         return adaptationServiceTypeMap;
     }
 
     public Map<String, DeadaptationServiceType> getDeadaptationServiceTypeMap() {
         return deadaptationServiceTypeMap;
     }
 
     /**
      * A helper method to read the values from the sLS record and setup the NML network object values
      *
      * @param nmlNetObj
      * @param slsNetObj
      */
     protected void setNetworkObejctValues(org.ogf.schemas.nml._2013._05.base.NetworkObject nmlNetObj, NetworkObject slsNetObj) {
         if (slsNetObj.getId() != null) {
             nmlNetObj.setId(slsNetObj.getId());
         }
         if (slsNetObj.getName() != null) {
             nmlNetObj.setName(slsNetObj.getName());
         }
 
         // to check if the network object has location information
         boolean locationSet = false;
         LocationType nmlLocation = null;
         if (slsNetObj.getLocation() != null) {
             if (nmlNetObj.getLocation() == null) {
                 nmlLocation = nmlFactory.createLocationType();
             } else {
                 nmlLocation = nmlNetObj.getLocation();
             }
             Location slsLocation = slsNetObj.getLocation();
             if (slsLocation.getId() != null) {
                 nmlLocation.setId(slsLocation.getId());
                 locationSet = true;
             }
             if (slsLocation.getName() != null) {
                 nmlLocation.setName(slsLocation.getName());
                 locationSet = true;
             }
             if (slsLocation.getAltitude() != null) {
                 nmlLocation.setAlt(slsLocation.getAltitude());
                 locationSet = true;
             }
             if (slsLocation.getLongitude() != null) {
                 nmlLocation.setLong(slsLocation.getLongitude());
                 locationSet = true;
             }
             if (slsLocation.getLatitude() != null) {
                 nmlLocation.setLat(slsLocation.getLatitude());
                 locationSet = true;
             }
             if (slsLocation.getUnlocode() != null) {
                 nmlLocation.setUnlocode(slsLocation.getUnlocode());
                 locationSet = true;
             }
             // TODO (AH): set address
         }
         if (locationSet) {
             nmlNetObj.setLocation(nmlLocation);
         }
 
         if (slsNetObj.getLifetimeStart() != null) {
             Calendar calendar = DatatypeConverter.parseDateTime(slsNetObj.getLifetimeStart());
             GregorianCalendar gregorianCalendar = new GregorianCalendar();
             gregorianCalendar.setTime(calendar.getTime());
             if (nmlNetObj.getLifetime() == null) {
                 nmlNetObj.setLifetime(nmlFactory.createLifeTimeType());
             }
             try {
                 XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
                 nmlNetObj.getLifetime().setStart(xmlCalendar);
             } catch (DatatypeConfigurationException e) {
                 // TODO (AH): handle error in version date format
                 e.printStackTrace();
             }
         }
 
         if (slsNetObj.getLifetimeEnd() != null) {
             Calendar calendar = DatatypeConverter.parseDateTime(slsNetObj.getLifetimeEnd());
             GregorianCalendar gregorianCalendar = new GregorianCalendar();
             gregorianCalendar.setTime(calendar.getTime());
             if (nmlNetObj.getLifetime() == null) {
                 nmlNetObj.setLifetime(nmlFactory.createLifeTimeType());
             }
             try {
                 XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
                 nmlNetObj.getLifetime().setEnd(xmlCalendar);
             } catch (DatatypeConfigurationException e) {
                 // TODO (AH): handle error in version date format
                 e.printStackTrace();
             }
         }
 
         if (slsNetObj.getVersion() != null) {
             Calendar calendar = DatatypeConverter.parseDateTime(slsNetObj.getVersion());
             GregorianCalendar gregorianCalendar = new GregorianCalendar();
             gregorianCalendar.setTime(calendar.getTime());
 
             try {
                 XMLGregorianCalendar xmlCalendar = DatatypeFactory.newInstance().newXMLGregorianCalendar(gregorianCalendar);
                 nmlNetObj.setVersion(xmlCalendar);
             } catch (DatatypeConfigurationException e) {
                 // TODO (AH): handle error in version date format
                 e.printStackTrace();
             }
         }
 
     }
 
     @Override
     public void visit(NetworkObject record) {
 
     }
 
     @Override
     public void visit(SwitchingService record) {
         SwitchingServiceType obj = nmlFactory.createSwitchingServiceType();
         setNetworkObejctValues(obj, record);
 
         if (record.getEncoding() != null)
             obj.setEncoding(record.getEncoding());
         if (record.getLabelSwaping() != null) {
             obj.setLabelSwapping(record.getLabelSwaping());
         }
 
         if (record.getHasInboundPort() != null) {
             SwitchingServiceRelationType relation = nmlFactory.createSwitchingServiceRelationType();
             for (String urn : record.getHasInboundPort()) {
                 relation.setType(NMLVisitor.RELATION_HAS_INBOUND_PORT);
                 PortType objR = null;
                 if (getPortTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     objR = nmlFactory.createPortType();
                     objR.setId(urn);
                 } else {
                     objR = getPortTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getPort().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
         if (record.getHasOutboundPort() != null) {
             SwitchingServiceRelationType relation = nmlFactory.createSwitchingServiceRelationType();
             for (String urn : record.getHasOutboundPort()) {
                 relation.setType(NMLVisitor.RELATION_HAS_OUTBOUND_PORT);
                 PortType objR = null;
                 if (getPortTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     objR = nmlFactory.createPortType();
                     objR.setId(urn);
                 } else {
                     objR = getPortTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getPort().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
         if (record.getHasInboundPortGroup() != null) {
             SwitchingServiceRelationType relation = nmlFactory.createSwitchingServiceRelationType();
             for (String urn : record.getHasInboundPortGroup()) {
                 relation.setType(NMLVisitor.RELATION_HAS_INBOUND_PORT);
                 PortGroupType objR = null;
                 if (getPortGroupTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     objR = nmlFactory.createPortGroupType();
                     objR.setId(urn);
                 } else {
                     objR = getPortGroupTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getPortGroup().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
         if (record.getHasOutboundPortGroup() != null) {
             SwitchingServiceRelationType relation = nmlFactory.createSwitchingServiceRelationType();
             for (String urn : record.getHasOutboundPortGroup()) {
                 relation.setType(NMLVisitor.RELATION_HAS_OUTBOUND_PORT);
                 PortGroupType objR = null;
                 if (getPortGroupTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     objR = nmlFactory.createPortGroupType();
                     objR.setId(urn);
                 } else {
                     objR = getPortGroupTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getPortGroup().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
         if (record.getProvidesLink() != null) {
             SwitchingServiceRelationType relation = nmlFactory.createSwitchingServiceRelationType();
             for (String urn : record.getProvidesLink()) {
                 relation.setType(NMLVisitor.RELATION_PROVIDES_LINK);
                 LinkType objR = null;
                 if (getLinkTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     objR = nmlFactory.createLinkType();
                     objR.setId(urn);
                 } else {
                     objR = getLinkTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getLink().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
         if (record.getProvidesLinkGroup() != null) {
             SwitchingServiceRelationType relation = nmlFactory.createSwitchingServiceRelationType();
             for (String urn : record.getProvidesLinkGroup()) {
                 relation.setType(NMLVisitor.RELATION_PROVIDES_LINK);
                 LinkGroupType objR = null;
                 if (getLinkGroupTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     objR = nmlFactory.createLinkGroupType();
                     objR.setId(urn);
                 } else {
                     objR = getLinkGroupTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getLinkGroup().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
         getSwitchingServiceTypeMap().put(obj.getId(), obj);
 
         if (record.getIsAlias() != null) {
             SwitchingServiceRelationType relation = nmlFactory.createSwitchingServiceRelationType();
             for (String urn : record.getIsAlias()) {
                 relation.setType(NMLVisitor.RELATION_IS_ALIAS);
                 SwitchingServiceType objR = null;
                 if (getSwitchingServiceTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true || urn.equalsIgnoreCase(record.getId())) {
                     objR = nmlFactory.createSwitchingServiceType();
                     objR.setId(urn);
                 } else {
                     objR = getSwitchingServiceTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getSwitchingService().add(objR);
             }
             obj.getRelation().add(relation);
         }
     }
 

     @Override
     public void visit(AdaptationService record) {
         AdaptationServiceType obj = nmlFactory.createAdaptationServiceType();
         setNetworkObejctValues(obj, record);
 
         if (record.getAdaptationFunction() != null) {
             obj.setAdaptationFunction(record.getAdaptationFunction());
         }
 
         if (record.getCanProvidePort() != null) {
             AdaptationServiceRelationType relation = nmlFactory.createAdaptationServiceRelationType();
             for (String urn : record.getCanProvidePort()) {
                 relation.setType(NMLVisitor.RELATION_CAN_PROVIDE_PORT);
                 PortType objR = null;
                 if (getPortTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     objR = nmlFactory.createPortType();
                     objR.setId(urn);
                 } else {
                     objR = getPortTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getPort().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
         if (record.getCanProvidePortGroup() != null) {
             AdaptationServiceRelationType relation = nmlFactory.createAdaptationServiceRelationType();
             for (String urn : record.getCanProvidePortGroup()) {
                 relation.setType(NMLVisitor.RELATION_CAN_PROVIDE_PORT);
                 PortGroupType objR = null;
                 if (getPortGroupTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     objR = nmlFactory.createPortGroupType();
                     objR.setId(urn);
                 } else {
                     objR = getPortGroupTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getPortGroup().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
 
         if (record.getProvidesPort() != null) {
             AdaptationServiceRelationType relation = nmlFactory.createAdaptationServiceRelationType();
             for (String urn : record.getProvidesPort()) {
                 relation.setType(NMLVisitor.RELATION_PROVIDES_PORT);
                 PortType objR = null;
                 if (getPortTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     objR = nmlFactory.createPortType();
                     objR.setId(urn);
                 } else {
                     objR = getPortTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getPort().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
         if (record.getProvidesPortGroup() != null) {
             AdaptationServiceRelationType relation = nmlFactory.createAdaptationServiceRelationType();
             for (String urn : record.getProvidesPortGroup()) {
                 relation.setType(NMLVisitor.RELATION_PROVIDES_PORT);
                 PortGroupType objR = null;
                 if (getPortGroupTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     objR = nmlFactory.createPortGroupType();
                     objR.setId(urn);
                 } else {
                     objR = getPortGroupTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getPortGroup().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
         getAdaptationServiceTypeMap().put(obj.getId(), obj);
 
         if (record.getIsAlias() != null) {
             AdaptationServiceRelationType relation = nmlFactory.createAdaptationServiceRelationType();
             for (String urn : record.getIsAlias()) {
                 relation.setType(NMLVisitor.RELATION_IS_ALIAS);
                 AdaptationServiceType objR = null;
                 if (getAdaptationServiceTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true || urn.equalsIgnoreCase(record.getId())) {
                     objR = nmlFactory.createAdaptationServiceType();
                     objR.setId(urn);
                 } else {
                     objR = getAdaptationServiceTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getAdaptationService().add(objR);
             }
             obj.getRelation().add(relation);
         }
     }
 

     @Override
     public void visit(DeadaptationService record) {
         DeadaptationServiceType obj = nmlFactory.createDeadaptationServiceType();
         setNetworkObejctValues(obj, record);
 
         if (record.getAdaptationFunction() != null) {
             obj.setAdaptationFunction(record.getAdaptationFunction());
         }
 
         if (record.getCanProvidePort() != null) {
             DeadaptationServiceRelationType relation = nmlFactory.createDeadaptationServiceRelationType();
             for (String urn : record.getCanProvidePort()) {
                 relation.setType(NMLVisitor.RELATION_CAN_PROVIDE_PORT);
                 PortType objR = null;
                 if (getPortTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     objR = nmlFactory.createPortType();
                     objR.setId(urn);
                 } else {
                     objR = getPortTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getPort().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
         if (record.getCanProvidePortGroup() != null) {
             DeadaptationServiceRelationType relation = nmlFactory.createDeadaptationServiceRelationType();
             for (String urn : record.getCanProvidePortGroup()) {
                 relation.setType(NMLVisitor.RELATION_CAN_PROVIDE_PORT);
                 PortGroupType objR = null;
                 if (getPortGroupTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     objR = nmlFactory.createPortGroupType();
                     objR.setId(urn);
                 } else {
                     objR = getPortGroupTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getPortGroup().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
 
         if (record.getProvidesPort() != null) {
             DeadaptationServiceRelationType relation = nmlFactory.createDeadaptationServiceRelationType();
             for (String urn : record.getProvidesPort()) {
                 relation.setType(NMLVisitor.RELATION_PROVIDES_PORT);
                 PortType objR = null;
                 if (getPortTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     objR = nmlFactory.createPortType();
                     objR.setId(urn);
                 } else {
                     objR = getPortTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getPort().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
         if (record.getProvidesPortGroup() != null) {
             DeadaptationServiceRelationType relation = nmlFactory.createDeadaptationServiceRelationType();
             for (String urn : record.getProvidesPortGroup()) {
                 relation.setType(NMLVisitor.RELATION_PROVIDES_PORT);
                 PortGroupType objR = null;
                 if (getPortGroupTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     objR = nmlFactory.createPortGroupType();
                     objR.setId(urn);
                 } else {
                     objR = getPortGroupTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getPortGroup().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
         getDeadaptationServiceTypeMap().put(obj.getId(), obj);
 
         if (record.getIsAlias() != null) {
             DeadaptationServiceRelationType relation = nmlFactory.createDeadaptationServiceRelationType();
             for (String urn : record.getIsAlias()) {
                 relation.setType(NMLVisitor.RELATION_IS_ALIAS);
                 DeadaptationServiceType objR = null;
                 if (getDeadaptationServiceTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true || urn.equalsIgnoreCase(record.getId())) {
                     objR = nmlFactory.createDeadaptationServiceType();
                     objR.setId(urn);
                 } else {
                     objR = getDeadaptationServiceTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getDeadaptationService().add(objR);
             }
             obj.getRelation().add(relation);
         }
     }
 

     @Override
     public void visit(BidirectionalLink record) {
         BidirectionalLinkType obj = nmlFactory.createBidirectionalLinkType();
         setNetworkObejctValues(obj, record);
 
         if (record.getLinks() != null) {
             for (String urn : record.getLinks()) {
                 if (getLinkTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     LinkType obj2 = nmlFactory.createLinkType();
                     obj2.setId(urn);
                     obj.getRest().add(nmlFactory.createLink(obj2));
                 } else {
                     obj.getRest().add(nmlFactory.createLink(getLinkTypeMap().get(urn)));
                     serializedURNS.add(urn);
                 }
             }
         }
 
         if (record.getLinkGroups() != null) {
             for (String urn : record.getLinkGroups()) {
                 if (getLinkTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     LinkGroupType obj2 = nmlFactory.createLinkGroupType();
                     obj2.setId(urn);
                     obj.getRest().add(nmlFactory.createLinkGroup(obj2));
                 } else {
                     obj.getRest().add(nmlFactory.createLinkGroup(getLinkGroupTypeMap().get(urn)));
                     serializedURNS.add(urn);
                 }
             }
         }
 
         getBidirectionalLinkTypeMap().put(obj.getId(), obj);
     }
 
     @Override
     public void visit(BidirectionalPort record) {
         BidirectionalPortType obj = nmlFactory.createBidirectionalPortType();
         setNetworkObejctValues(obj, record);
 
         if (record.getPorts() != null) {
             for (String urn : record.getPorts()) {
                 if (getPortTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     PortType obj2 = nmlFactory.createPortType();
                     obj2.setId(urn);
                     obj.getRest().add(nmlFactory.createPort(obj2));
                 } else {
                     obj.getRest().add(nmlFactory.createPort(getPortTypeMap().get(urn)));
                     serializedURNS.add(urn);
                 }
             }
         }
 
         if (record.getPortGroups() != null) {
             for (String urn : record.getPortGroups()) {
                 if (getPortGroupTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     PortGroupType obj2 = nmlFactory.createPortGroupType();
                     obj2.setId(urn);
                     obj.getRest().add(nmlFactory.createPortGroup(obj2));
                 } else {
                     obj.getRest().add(nmlFactory.createPortGroup(getPortGroupTypeMap().get(urn)));
                     serializedURNS.add(urn);
                 }
             }
         }
 
         getBidirectionalPortTypeMap().put(obj.getId(), obj);
     }
 
     @Override
     public void visit(Link record) {
         LinkType obj = nmlFactory.createLinkType();
         setNetworkObejctValues(obj, record);
 
         if (record.getEncoding() != null)
             obj.setEncoding(record.getEncoding());
         if (record.getNoReturnTraffic() != null) {
             obj.setNoReturnTraffic(record.getNoReturnTraffic());
         }
 
         if (record.getLabelType() != null) {
             obj.setLabel(nmlFactory.createLabelType());
             obj.getLabel().setLabeltype(record.getLabelType());
             obj.getLabel().setValue(record.getLabel());
         }
 
 
         if (record.getIsSerialCompoundLink() != null) {
             LinkRelationType relation = nmlFactory.createLinkRelationType();
             for (String urn : record.getIsSerialCompoundLink()) {
                 relation.setType(NMLVisitor.RELATION_IS_SERIAL_COMPOUND_LINK);
                 LinkType objR = null;
                 if (getLinkTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     objR = nmlFactory.createLinkType();
                     objR.setId(urn);
                 } else {
                     objR = getLinkTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getLink().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
         if (record.getNext() != null) {
             LinkRelationType relation = nmlFactory.createLinkRelationType();
             for (String urn : record.getNext()) {
                 relation.setType(NMLVisitor.RELATION_NEXT);
                 LinkType objR = null;
                 if (getLinkTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     objR = nmlFactory.createLinkType();
                     objR.setId(urn);
                 } else {
                     objR = getLinkTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getLink().add(objR);
             }
             obj.getRelation().add(relation);
         }
         getLinkTypeMap().put(obj.getId(), obj);
 
         if (record.getIsAlias() != null) {
             LinkRelationType relation = nmlFactory.createLinkRelationType();
             for (String urn : record.getIsAlias()) {
                 relation.setType(NMLVisitor.RELATION_IS_ALIAS);
                 LinkType objR = null;
                 if (getLinkTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true || urn.equalsIgnoreCase(record.getId())) {
                     objR = nmlFactory.createLinkType();
                     objR.setId(urn);
                 } else {
                     objR = getLinkTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getLink().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
     }
 
     @Override
     public void visit(LinkGroup record) {
         LinkGroupType obj = nmlFactory.createLinkGroupType();
         setNetworkObejctValues(obj, record);
 
         if (record.getLabelGroup().getLabels() != null &&
                 record.getLabelGroup().getLabels().size() != 0) {
             for (List<String> label : record.getLabelGroup().getLabels()) {
                 LabelGroupType labelGroupType = (nmlFactory.createLabelGroupType());
                 labelGroupType.setLabeltype(label.get(0));
                 labelGroupType.setValue(label.get(1));
                 obj.getLabelGroup().add(labelGroupType);
             }
         }
 
         if (record.getLinks() != null) {
             for (String urn : record.getLinks()) {
                 if (getLinkTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     LinkType obj2 = nmlFactory.createLinkType();
                     obj.getLink().add(obj2);
                 } else {
                     obj.getLink().add(getLinkTypeMap().get(urn));
                     serializedURNS.add(urn);
                 }
             }
         }
 
         if (record.getLinkGroups() != null) {
             for (String urn : record.getLinkGroups()) {
                 if (getLinkGroupTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true || urn.equalsIgnoreCase(record.getId())) {
                     LinkGroupType obj2 = nmlFactory.createLinkGroupType();
                     obj.getLinkGroup().add(obj2);
                 } else {
                     obj.getLinkGroup().add(getLinkGroupTypeMap().get(urn));
                     serializedURNS.add(urn);
                 }
             }
         }
 
         if (record.getIsSerialCompoundLink() != null) {
             LinkGroupRelationType relation = nmlFactory.createLinkGroupRelationType();
             for (String urn : record.getIsSerialCompoundLink()) {
                 relation.setType(NMLVisitor.RELATION_IS_SERIAL_COMPOUND_LINK);
                 LinkGroupType objR = null;
                 if (getLinkGroupTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     objR = nmlFactory.createLinkGroupType();
                     objR.setId(urn);
                 } else {
                     objR = getLinkGroupTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getLinkGroup().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
 
         getLinkGroupTypeMap().put(obj.getId(), obj);
 
         if (record.getIsAlias() != null) {
             LinkGroupRelationType relation = nmlFactory.createLinkGroupRelationType();
             for (String urn : record.getIsAlias()) {
                 relation.setType(NMLVisitor.RELATION_IS_ALIAS);
                 LinkGroupType objR = null;
                 if (getLinkGroupTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true || urn.equalsIgnoreCase(record.getId())) {
                     objR = nmlFactory.createLinkGroupType();
                     objR.setId(urn);
                 } else {
                     objR = getLinkGroupTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getLinkGroup().add(objR);
             }
             obj.getRelation().add(relation);
         }
     }
 
     @Override
     public void visit(Node record) {
         NodeType obj = nmlFactory.createNodeType();
         setNetworkObejctValues(obj, record);
 
         if (record.getNodes() != null) {
             for (String urn : record.getNodes()) {
                 if (getNodeTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true || urn.equalsIgnoreCase(record.getId())) {
                     NodeType obj2 = nmlFactory.createNodeType();
                     obj.getNode().add(obj2);
                 } else {
                     obj.getNode().add(getNodeTypeMap().get(urn));
                     serializedURNS.add(urn);
                 }
             }
         }
 
         if (record.getHasOutboundPort() != null) {
             NodeRelationType relation = nmlFactory.createNodeRelationType();
             for (String urn : record.getHasOutboundPort()) {
                 relation.setType(NMLVisitor.RELATION_HAS_OUTBOUND_PORT);
                 PortType objR = null;
                 if (getPortTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     objR = nmlFactory.createPortType();
                     objR.setId(urn);
                 } else {
                     objR = getPortTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getPort().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
         if (record.getHasInboundPort() != null) {
             NodeRelationType relation = nmlFactory.createNodeRelationType();
             for (String urn : record.getHasInboundPort()) {
                 relation.setType(NMLVisitor.RELATION_HAS_INBOUND_PORT);
                 PortType objR = null;
                 if (getPortTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     objR = nmlFactory.createPortType();
                     objR.setId(urn);
                 } else {
                     objR = getPortTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getPort().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
         if (record.getHasOutboundPortGroup() != null) {
             NodeRelationType relation = nmlFactory.createNodeRelationType();
             for (String urn : record.getHasOutboundPortGroup()) {
                 relation.setType(NMLVisitor.RELATION_HAS_OUTBOUND_PORT);
                 PortGroupType objR = null;
                 if (getPortGroupTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     objR = nmlFactory.createPortGroupType();
                     objR.setId(urn);
                 } else {
                     objR = getPortGroupTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getPortGroup().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
         if (record.getHasInboundPortGroup() != null) {
             NodeRelationType relation = nmlFactory.createNodeRelationType();
             for (String urn : record.getHasInboundPortGroup()) {
                 relation.setType(NMLVisitor.RELATION_HAS_INBOUND_PORT);
                 PortGroupType objR = null;
                 if (getPortGroupTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     objR = nmlFactory.createPortGroupType();
                     objR.setId(urn);
                 } else {
                     objR = getPortGroupTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getPortGroup().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
         if (record.getHasService() != null) {
             NodeRelationType relation = nmlFactory.createNodeRelationType();
             for (String urn : record.getHasService()) {
                 relation.setType(NMLVisitor.RELATION_HAS_SERVICE);
                 SwitchingServiceType objR = null;
                 if (getSwitchingServiceTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     objR = nmlFactory.createSwitchingServiceType();
                     objR.setId(urn);
                 } else {
                     objR = getSwitchingServiceTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getSwitchingService().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
         getNodeTypeMap().put(obj.getId(), obj);
 
         if (record.getIsAlias() != null) {
             NodeRelationType relation = nmlFactory.createNodeRelationType();
             for (String urn : record.getIsAlias()) {
                 relation.setType(NMLVisitor.RELATION_IS_ALIAS);
                 NodeType objR = null;
                 if (getNodeTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true || urn.equalsIgnoreCase(record.getId())) {
                     objR = nmlFactory.createNodeType();
                     objR.setId(urn);
                 } else {
                     objR = getNodeTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getNode().add(objR);
             }
             obj.getRelation().add(relation);
         }
     }
 
     @Override
     public void visit(NSA record) {
         NSAType obj = nsiFactory.createNSAType();
         setNetworkObejctValues(obj, record);
 
         if (record.getNSIServices() != null) {
             for (String urn : record.getNSIServices()) {
                 if (getNsiServiceTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     NsiServiceType obj2 = nsiFactory.createNsiServiceType();
                     obj.getService().add(obj2);
                 } else {
                     obj.getService().add(getNsiServiceTypeMap().get(urn));
                     serializedURNS.add(urn);
                 }
             }
         }
 
         // TODO (AH): Parse admin contacts
 
         if (record.getPeersWith() != null) {
             NSARelationType relation = nsiFactory.createNSARelationType();
             for (String urn : record.getPeersWith()) {
                 relation.setType(NMLVisitor.RELATION_PEERS_WITH);
                 NSAType objR = null;
                 if (getNsaTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true || urn.equalsIgnoreCase(record.getId())) {
                     objR = nsiFactory.createNSAType();
                     objR.setId(urn);
                 } else {
                     objR = getNsaTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getNSA().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
         if (record.getManagedBy() != null) {
             NSARelationType relation = nsiFactory.createNSARelationType();
             for (String urn : record.getManagedBy()) {
                 relation.setType(NMLVisitor.RELATION_MANAGED_BY);
                 NSAType objR = null;
                 if (getNsaTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true || urn.equalsIgnoreCase(record.getId())) {
                     objR = nsiFactory.createNSAType();
                     objR.setId(urn);
                 } else {
                     objR = getNsaTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getNSA().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
 
         if (record.getTopologies() != null) {
             for (String urn : record.getTopologies()) {
                 if (getTopologyTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     TopologyType obj2 = nmlFactory.createTopologyType();
                     obj.getTopology().add(obj2);
                 } else {
                     obj.getTopology().add(getTopologyTypeMap().get(urn));
                     serializedURNS.add(urn);
                 }
             }
         }
 
         getNsaTypeMap().put(obj.getId(), obj);
     }
 
     @Override
     public void visit(NSIService record) {
         NsiServiceType obj = nsiFactory.createNsiServiceType();
         setNetworkObejctValues(obj, record);
 
         if (record.getLink() != null) {
             obj.setLink(record.getLink());
         }
 
         if (record.getDescribedBy() != null) {
             obj.setDescribedBy(record.getDescribedBy());
         }
 
         if (record.getType() != null) {
             obj.setType(record.getType());
         }
 
         if (record.getProvidedBy() != null) {
             NsiServiceRelationType relation = nsiFactory.createNsiServiceRelationType();
             for (String urn : record.getProvidedBy()) {
                 relation.setType(NMLVisitor.RELATION_PROVIDED_BY);
                 NSAType objR = null;
                 if (getNsaTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     objR = nsiFactory.createNSAType();
                     objR.setId(urn);
                 } else {
                     objR = getNsaTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getNSA().add(objR);
             }
             obj.getRelation().add(relation);
         }
         getNsiServiceTypeMap().put(obj.getId(), obj);
     }
 
     @Override
     public void visit(Port record) {
         PortType obj = nmlFactory.createPortType();
         setNetworkObejctValues(obj, record);
 
         if (record.getEncoding() != null)
             obj.setEncoding(record.getEncoding());
         if (record.getLabelType() != null) {
             obj.setLabel(nmlFactory.createLabelType());
             obj.getLabel().setLabeltype(record.getLabelType());
             obj.getLabel().setValue(record.getLabel());
         }
 
         if (record.getIsSink() != null) {
             PortRelationType relation = nmlFactory.createPortRelationType();
             for (String urn : record.getIsSink()) {
                 relation.setType(NMLVisitor.RELATION_IS_SINK);
                 LinkType objR = null;
                 if (getLinkTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
 
                     objR = nmlFactory.createLinkType();
                     objR.setId(urn);
                 } else {
                     objR = getLinkTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getLink().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
         if (record.getIsSource() != null) {
             PortRelationType relation = nmlFactory.createPortRelationType();
             for (String urn : record.getIsSource()) {
                 relation.setType(NMLVisitor.RELATION_IS_SOURCE);
                 LinkType objR = null;
                 if (getLinkTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
 
                     objR = nmlFactory.createLinkType();
                     objR.setId(urn);
                 } else {
                     objR = getLinkTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getLink().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
         getPortTypeMap().put(obj.getId(), obj);
 
         if (record.getIsAlias() != null) {
             PortRelationType relation = nmlFactory.createPortRelationType();
             for (String urn : record.getIsAlias()) {
                 relation.setType(NMLVisitor.RELATION_IS_ALIAS);
                 PortType objR = null;
                 if (getPortTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true || urn.equalsIgnoreCase(record.getId())) {
                     objR = nmlFactory.createPortType();
                     objR.setId(urn);
                 } else {
                     objR = getPortTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getPort().add(objR);
             }
             obj.getRelation().add(relation);
         }
     }
 
     @Override
     public void visit(PortGroup record) {
         PortGroupType obj = nmlFactory.createPortGroupType();
         setNetworkObejctValues(obj, record);
 
         if (record.getEncoding() != null)
             obj.setEncoding(record.getEncoding());
 
         if (record.getLabelGroup().getLabels() != null &&
                 record.getLabelGroup().getLabels().size() != 0) {
             for (List<String> label : record.getLabelGroup().getLabels()) {
                 LabelGroupType labelGroupType = (nmlFactory.createLabelGroupType());
                 labelGroupType.setLabeltype(label.get(0));
                 labelGroupType.setValue(label.get(1));
                 obj.getLabelGroup().add(labelGroupType);
             }
         }
 
         if (record.getPorts() != null) {
             for (String urn : record.getPorts()) {
                 if (getPortTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     PortType obj2 = nmlFactory.createPortType();
                     obj.getPort().add(obj2);
                 } else {
                     obj.getPort().add(getPortTypeMap().get(urn));
                     serializedURNS.add(urn);
                 }
             }
         }
 
         if (record.getPortGroups() != null) {
             for (String urn : record.getPortGroups()) {
                 if (getPortGroupTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true || urn.equalsIgnoreCase(record.getId())) {
                     PortGroupType obj2 = nmlFactory.createPortGroupType();
                     obj.getPortGroup().add(obj2);
                 } else {
                     obj.getPortGroup().add(getPortGroupTypeMap().get(urn));
                     serializedURNS.add(urn);
                 }
             }
         }
 
         if (record.getIsSink() != null) {
             PortGroupRelationType relation = nmlFactory.createPortGroupRelationType();
             for (String urn : record.getIsSink()) {
                 relation.setType(NMLVisitor.RELATION_IS_SINK);
                 LinkGroupType objR = null;
                 if (getLinkGroupTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     objR = nmlFactory.createLinkGroupType();
                     objR.setId(urn);
                 } else {
                     objR = getLinkGroupTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getLinkGroup().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
         if (record.getIsSource() != null) {
             PortGroupRelationType relation = nmlFactory.createPortGroupRelationType();
             for (String urn : record.getIsSource()) {
                 relation.setType(NMLVisitor.RELATION_IS_SOURCE);
                 LinkGroupType objR = null;
                 if (getLinkGroupTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
 
                     objR = nmlFactory.createLinkGroupType();
                     objR.setId(urn);
                 } else {
                     objR = getLinkGroupTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getLinkGroup().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
         getPortGroupTypeMap().put(obj.getId(), obj);
 
         if (record.getIsAlias() != null) {
             PortGroupRelationType relation = nmlFactory.createPortGroupRelationType();
             for (String urn : record.getIsAlias()) {
                 relation.setType(NMLVisitor.RELATION_IS_ALIAS);
                 PortGroupType objR = null;
                 if (getPortGroupTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true || urn.equalsIgnoreCase(record.getId())) {
                     objR = nmlFactory.createPortGroupType();
                     objR.setId(urn);
                 } else {
                     objR = getPortGroupTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getPortGroup().add(objR);
             }
             obj.getRelation().add(relation);
         }
     }
 
     @Override
     public void visit(Topology record) {
         TopologyType obj = nmlFactory.createTopologyType();
         setNetworkObejctValues(obj, record);
         System.out.println("Visiting topology: " + record.getId());
         if (record.getPorts() != null) {
             for (String urn : record.getPorts()) {
                 if (getPortTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     PortType obj2 = nmlFactory.createPortType();
                     obj.getPort().add(obj2);
                 } else {
                     obj.getPort().add(getPortTypeMap().get(urn));
                     serializedURNS.add(urn);
                 }
             }
         }
 
         if (record.getLinks() != null) {
             for (String urn : record.getLinks()) {
                 if (getLinkTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     LinkType obj2 = nmlFactory.createLinkType();
                     obj.getLink().add(obj2);
                 } else {
                     obj.getLink().add(getLinkTypeMap().get(urn));
                     serializedURNS.add(urn);
                 }
             }
         }
 
 
         if (record.getHasInboundPortGroup() != null) {
             TopologyRelationType relation = nmlFactory.createTopologyRelationType();
             for (String urn : record.getHasInboundPortGroup()) {
                 relation.setType(NMLVisitor.RELATION_HAS_INBOUND_PORT);
                 PortGroupType objR = null;
                 if (getPortGroupTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     objR = nmlFactory.createPortGroupType();
                     objR.setId(urn);
                 } else {
                     objR = getPortGroupTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getPortGroup().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
         if (record.getHasOutboundPortGroup() != null) {
             TopologyRelationType relation = nmlFactory.createTopologyRelationType();
             for (String urn : record.getHasOutboundPortGroup()) {
                 relation.setType(NMLVisitor.RELATION_HAS_OUTBOUND_PORT);
                 PortGroupType objR = null;
                 if (getPortGroupTypeMap().containsKey(urn) == false || serializedURNS.contains(urn) == true) {
                     objR = nmlFactory.createPortGroupType();
                     objR.setId(urn);
                 } else {
                     objR = getPortGroupTypeMap().get(urn);
                     serializedURNS.add(urn);
                 }
                 relation.getPortGroup().add(objR);
             }
             obj.getRelation().add(relation);
         }
 
         getTopologyTypeMap().put(obj.getId(), obj);
 
         if (record.getIsAlias() != null) {
             for (String urn : record.getIsAlias()) {
                 if (getTopologyTypeMap().containsKey(urn)) {
                     org.ogf.schemas.nml._2013._05.base.ObjectFactory factory = new org.ogf.schemas.nml._2013._05.base.ObjectFactory();
                     TopologyRelationType relation = factory.createTopologyRelationType();
                     relation.setType(NMLVisitor.RELATION_IS_ALIAS);
                     TopologyType topo = factory.createTopologyType();
                     topo.setId(urn);
                     relation.getTopology().add(topo);
                     obj.getRelation().add(relation);
                 }
             }
         }
     }
 }
