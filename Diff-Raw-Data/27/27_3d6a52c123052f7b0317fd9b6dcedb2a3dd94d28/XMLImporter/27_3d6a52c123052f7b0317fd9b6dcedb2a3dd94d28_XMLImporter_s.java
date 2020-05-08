 package dk.frv.eavdam.io;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.sql.Date;
 import java.util.List;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.datatype.XMLGregorianCalendar;
 
 import org.w3c.dom.Element;
 
 import dk.frv.eavdam.data.AISFixedStationCoverage;
 import dk.frv.eavdam.data.AISFixedStationStatus;
 import dk.frv.eavdam.data.AISFixedStationType;
 import dk.frv.eavdam.data.AntennaType;
 import dk.frv.eavdam.data.EAVDAMData;
 import dk.frv.eavdam.io.derby.DerbyDBInterface;
 import dk.frv.eavdam.io.jaxb.Address;
 import dk.frv.eavdam.io.jaxb.AisFixedStationCoverage;
 import dk.frv.eavdam.io.jaxb.AisFixedStationCoverageType;
 import dk.frv.eavdam.io.jaxb.AisFixedStationData;
 import dk.frv.eavdam.io.jaxb.Antenna;
 import dk.frv.eavdam.io.jaxb.CoveragePoint;
 import dk.frv.eavdam.io.jaxb.EavdamData;
 import dk.frv.eavdam.io.jaxb.EavdamUser;
 import dk.frv.eavdam.io.jaxb.FatdmaSlotAllocation;
 import dk.frv.eavdam.io.jaxb.Person;
 import dk.frv.eavdam.io.jaxb.Status;
 
 public class XMLImporter {
 
 	private static dk.frv.eavdam.data.EAVDAMData convert(EavdamData xData)
 			throws MalformedURLException {
 		if (xData != null) {
 			dk.frv.eavdam.data.EAVDAMData data = new dk.frv.eavdam.data.EAVDAMData();
 			dk.frv.eavdam.data.EAVDAMUser user = convert(xData.getUser());
 			data.setUser(user);
 			for (AisFixedStationData xd : xData.getStation()) {
 				dk.frv.eavdam.data.AISFixedStationData d = convert(xd);
 				d.setOperator(user);
 				d.setStationDBID(-1);
 				data.addStation(d);
 			}
 			return data;
 		}
 		return null;
 	}
 
 	private static dk.frv.eavdam.data.EAVDAMUser convert(EavdamUser xUser)
 			throws MalformedURLException {
 		if (xUser != null) {
 			dk.frv.eavdam.data.EAVDAMUser user = new dk.frv.eavdam.data.EAVDAMUser();
 			user.setOrganizationName(xUser.getOrganizationName());
 			user.setCountryID(xUser.getCountryID());
 			user.setPhone(xUser.getPhone());
 			user.setFax(xUser.getFax());
 			if (xUser.getWww() != null) {
 				user.setWww(new java.net.URL(xUser.getWww()));
 			}
 			user.setDescription(xUser.getDescription());
 			user.setContact(convert(xUser.getContact()));
 			user.setTechnicalContact(convert(xUser.getTechnicalContact()));
 			user.setVisitingAddress(convert(xUser.getVisitingAddress()));
 			user.setPostalAddress(convert(xUser.getPostalAddress()));
 			List<Element> anything = xUser.getAny();
 			user.setAnything(anything);
 
 			return user;
 		}
 		return null;
 	}
 
 	private static dk.frv.eavdam.data.Person convert(Person xPerson) {
 		if (xPerson != null) {
 			dk.frv.eavdam.data.Person person = new dk.frv.eavdam.data.Person();
 			person.setName(xPerson.getName());
 			person.setEmail(xPerson.getEmail());
 			person.setPhone(xPerson.getPhone());
 			person.setFax(xPerson.getFax());
 			person.setDescription(xPerson.getDescription());
 			person.setVisitingAddress(convert(xPerson.getVisitingAddress()));
 			person.setPostalAddress(convert(xPerson.getPostalAddress()));
 			return person;
 		}
 		return null;
 	}
 
 	private static dk.frv.eavdam.data.Address convert(Address xAddress) {
 		if (xAddress != null) {
 			dk.frv.eavdam.data.Address address = new dk.frv.eavdam.data.Address();
 			address.setAddressline1(xAddress.getAddressline1());
 			address.setAddressline2(xAddress.getAddressline2());
 			address.setZip(xAddress.getZip());
 			address.setCity(xAddress.getCity());
 			address.setCountry(xAddress.getCountry());
 			return address;
 		}
 		return null;
 	}
 
 	private static dk.frv.eavdam.data.AISFixedStationData convert(
 			AisFixedStationData xData) {
 		if (xData != null) {
 			dk.frv.eavdam.data.AISFixedStationData data = new dk.frv.eavdam.data.AISFixedStationData();
 			data.setStationName(xData.getStationName());
 			data.setLat(xData.getLat());
 			data.setLon(xData.getLon());
 			data.setMmsi(xData.getMmsi());
 			data.setTransmissionPower(xData.getTransmissionPower());
 			data.setDescription(xData.getDescription());
 			
 			data.setAntenna(convert(xData.getAntenna()));
 			data.setFatdmaAllocation(convert(xData.getFatdmaAllocation()));
 			
 			data.setInterferenceCoverage(convert(xData.getCoverage(), DerbyDBInterface.COVERAGE_INTERFERENCE));
 			data.setTransmissionCoverage(convert(xData.getCoverage(), DerbyDBInterface.COVERAGE_TRANSMIT));
 			data.setReceiveCoverage(convert(xData.getCoverage(),DerbyDBInterface.COVERAGE_RECEIVE));
 			
 			if (xData.getStationType() != null) {
 				data.setStationType(AISFixedStationType.valueOf(xData
 						.getStationType().toString()));
 			}
 			if (xData.getStatus() != null) {
 				data.setStatus(convert(xData.getStatus()));
 			}
 			
 			List<Element> anything = xData.getAny();
 			data.setAnything(anything);
 			
 			return data;
 		}
 		return null;
 	}
 
 	private static dk.frv.eavdam.data.AISFixedStationStatus convert(Status xStatus){
 		if(xStatus != null){
 			AISFixedStationStatus s = new AISFixedStationStatus();
 
 			XMLGregorianCalendar start_xcal = xStatus.getStartDate();
 			if(start_xcal != null){
 				long s_dt = start_xcal.toGregorianCalendar().getTime().getTime();
 				s.setStartDate(new Date(s_dt));
 			}
 			
 			XMLGregorianCalendar end_xcal = xStatus.getEndDate();
 			
 			if(end_xcal != null){
 				long e_dt = end_xcal.toGregorianCalendar().getTime().getTime();
 				s.setEndDate(new Date(e_dt));
 			}
 			
 			s.setStatusName(xStatus.getStatus().name());
 			
 			switch(xStatus.getStatus()){
 				case OLD: s.setStatusID(DerbyDBInterface.STATUS_OLD); break;
 				case OPERATIVE: s.setStatusID(DerbyDBInterface.STATUS_ACTIVE); break;
 				case PLANNED: s.setStatusID(DerbyDBInterface.STATUS_PLANNED); break;
 				case PROPOSED: s.setStatusID(DerbyDBInterface.STATUS_PROPOSED); break;
 				case SIMULATED: s.setStatusID(DerbyDBInterface.STATUS_SIMULATED); break;
 			}
 			
 			return s;
 		}
 		return null;
 	}
 	
 	private static dk.frv.eavdam.data.Antenna convert(Antenna xData) {
 		if (xData != null) {
 			dk.frv.eavdam.data.Antenna data = new dk.frv.eavdam.data.Antenna();
 			data.setAntennaHeight(xData.getAntennaHeight());
 			data.setTerrainHeight(xData.getTerrainHeight());
 			if (xData.getOmnidirectionalAntenna() != null) {
 				data.setAntennaType(AntennaType.OMNIDIRECTIONAL);
 			} else {
 				data.setAntennaType(AntennaType.DIRECTIONAL);
 				data.setHeading(xData.getDirectionalAntenna().getHeading());
 				data.setFieldOfViewAngle(xData.getDirectionalAntenna()
 						.getFieldOfViewAngle());
 				data.setGain(xData.getDirectionalAntenna().getGain());
 			}
 			return data;
 		}
 		return null;
 	}
 
 	// TODO
 	private static dk.frv.eavdam.data.AISFixedStationCoverage convert(
 			List<AisFixedStationCoverage> xDataList, int coverageType) {
		if (xDataList != null) {
 			dk.frv.eavdam.data.AISFixedStationCoverage coverage = new AISFixedStationCoverage();
 			for(AisFixedStationCoverage c : xDataList){
 				if(c.getCoverageType() == AisFixedStationCoverageType.TRANSMISSION && coverageType == DerbyDBInterface.COVERAGE_TRANSMIT){
 					for(CoveragePoint p : c.getPoint()){
 						coverage.addCoveragePoint(p.getLat(), p.getLon());
 					}
 				}else if(c.getCoverageType() == AisFixedStationCoverageType.RECEIVE && coverageType == DerbyDBInterface.COVERAGE_RECEIVE){
 					for(CoveragePoint p : c.getPoint()){
 						coverage.addCoveragePoint(p.getLat(), p.getLon());
 					}
 				} else if(c.getCoverageType() == AisFixedStationCoverageType.INTERFERENCE && coverageType == DerbyDBInterface.COVERAGE_INTERFERENCE){
 					for(CoveragePoint p : c.getPoint()){
 						coverage.addCoveragePoint(p.getLat(), p.getLon());
 					}
 				}
 			}
 			
 			return coverage;
 		}
 		return null;
 	}
 
 	// TODO
 	private static dk.frv.eavdam.data.FATDMASlotAllocation convert(
 			FatdmaSlotAllocation xData) {
 		if (xData != null) {
 
 		}
 		return null;
 	}
 
 	public static EAVDAMData readXML(File xml) throws JAXBException,
 			MalformedURLException {
 		JAXBContext jc = JAXBContext.newInstance("dk.frv.eavdam.io.jaxb");
 		Unmarshaller unmarshaller = jc.createUnmarshaller();
 		JAXBElement o = (JAXBElement) unmarshaller.unmarshal(xml);
 		if (o != null && o.getValue() instanceof EavdamData) {
 			return convert((EavdamData) o.getValue());
 		} else {
 			throw new RuntimeException("Invalid file");
 		}
 	}
 }
