 package dk.frv.eavdam.io;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.math.BigDecimal;
 import java.math.BigInteger;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Locale;
 import java.util.TimeZone;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.datatype.DatatypeFactory;
 import javax.xml.datatype.Duration;
 import javax.xml.datatype.XMLGregorianCalendar;
 import javax.xml.namespace.QName;
 
 import dk.frv.eavdam.data.AISFixedStationData;
 import dk.frv.eavdam.data.AISFixedStationStatus;
 import dk.frv.eavdam.data.AntennaType;
 import dk.frv.eavdam.data.FATDMASlotAllocation;
 import dk.frv.eavdam.io.derby.DerbyDBInterface;
 import dk.frv.eavdam.io.jaxb.Address;
 import dk.frv.eavdam.io.jaxb.AisFixedStationCoverage;
 import dk.frv.eavdam.io.jaxb.AisFixedStationCoverageType;
 import dk.frv.eavdam.io.jaxb.AisFixedStationData;
 import dk.frv.eavdam.io.jaxb.AisFixedStationStatus;
 import dk.frv.eavdam.io.jaxb.AisFixedStationType;
 import dk.frv.eavdam.io.jaxb.Antenna;
 import dk.frv.eavdam.io.jaxb.CoveragePoint;
 import dk.frv.eavdam.io.jaxb.DirectionalAntenna;
 import dk.frv.eavdam.io.jaxb.EavdamData;
 import dk.frv.eavdam.io.jaxb.EavdamUser;
 import dk.frv.eavdam.io.jaxb.FatdmaSlotAllocation;
 import dk.frv.eavdam.io.jaxb.ObjectFactory;
 import dk.frv.eavdam.io.jaxb.OmnidirectionalAntenna;
 import dk.frv.eavdam.io.jaxb.Person;
 import dk.frv.eavdam.io.jaxb.Status;
 
 /**
  * Class that is responsible for converting AIS base station network data
  * (station and coverage details) to common XML structure from EAVDAM internal
  * Java objects.
  * 
  * @author ttesei
  * @version 1.0
  * @created 26-elo-2011 13:27:25
  */
 public class XMLExporter {
 
 	/**
 	 * 
 	 * @param data
 	 * @param file
 	 * @throws JAXBException
 	 * @throws FileNotFoundException
 	 */
 	public static void writeXML(dk.frv.eavdam.data.EAVDAMData data, File file)
 			throws JAXBException, FileNotFoundException {
 		ObjectFactory objFactory = new ObjectFactory();
 		// Root level
 		EavdamData xData = objFactory.createEavdamData();
 		// User part
 		dk.frv.eavdam.data.EAVDAMUser user = data.getUser();
 		EavdamUser xUser = objFactory.createEavdamUser();
 		xUser.setOrganizationName(user.getOrganizationName());
 		xUser.setCountryID(user.getCountryID());
 		xUser.setPhone(user.getPhone());
 		xUser.setFax(user.getFax());
 		if (user.getWww() != null) {
 			xUser.setWww(user.getWww().toString());
 		}
 		xUser.setDescription(user.getDescription());
 		xUser.setContact(convert(user.getContact()));
 		xUser.setTechnicalContact(convert(user.getTechnicalContact()));
 		xUser.setVisitingAddress(convert(user.getVisitingAddress()));
 		xUser.setPostalAddress(convert(user.getPostalAddress()));
 		if (user.getAnything() != null) {
 			xUser.getAny().addAll(user.getAnything());
 		}
 		xData.setUser(xUser);
 		
 		try{
 			GregorianCalendar c = new GregorianCalendar();
 			c.setTime(new Date(System.currentTimeMillis()));
 			XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
 			xData.setTimestamp(date2);
 		}catch(Exception e){
 			e.printStackTrace();
 		}
 		
 		//TODO convert active etc. stations?!?!?
 		
 		// Stations
 		List<AisFixedStationData> xStations = xData.getStation();
 		for (AISFixedStationData station : data.getStations()) {
 			xStations.add(convert(station));
 		}
 		// Create file
 		JAXBContext jc = JAXBContext.newInstance("dk.frv.eavdam.io.jaxb");
 		Marshaller marshaller = jc.createMarshaller();
 		//marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, new Boolean(
 		//		true));
 		
 		ObjectFactory factory = new ObjectFactory();
 		JAXBElement<EavdamData> element = factory.createEavdamData(xData);
  		
 		marshaller.marshal(element, new FileOutputStream(file));
 		
 
 	}
 
 	private static Person convert(dk.frv.eavdam.data.Person person) {
 		if (person != null) {
 			Person xPerson = new Person();
 			xPerson.setName(person.getName());
 			xPerson.setEmail(person.getEmail());
 			xPerson.setPhone(person.getPhone());
 			xPerson.setFax(person.getFax());
 			xPerson.setDescription(person.getDescription());
 			xPerson.setVisitingAddress(convert(person.getVisitingAddress()));
 			xPerson.setPostalAddress(convert(person.getPostalAddress()));
 			return xPerson;
 		}
 		return null;
 	}
 
 	private static Address convert(dk.frv.eavdam.data.Address address) {
 		if (address != null) {
 			Address xAddress = new Address();
 			xAddress.setAddressline1(address.getAddressline1());
 			xAddress.setAddressline2(address.getAddressline2());
 			xAddress.setZip(address.getZip());
 			xAddress.setCity(address.getCity());
 			xAddress.setCountry(address.getCountry());
 			return xAddress;
 		}
 		return null;
 	}
 
 	private static AisFixedStationData convert(AISFixedStationData data) {
 		if (data != null) {
 			AisFixedStationData xData = new AisFixedStationData();
 			xData.setStationName(data.getStationName());
 			xData.setLat(data.getLat());
 			xData.setLon(data.getLon());
 			xData.setMmsi(data.getMmsi());
 			xData.setTransmissionPower(data.getTransmissionPower());
 			xData.setDescription(data.getDescription());
 			if (data.getTransmissionCoverage() != null) {
				xData.getCoverage().addAll(convert(data.getTransmissionCoverage(), DerbyDBInterface.COVERAGE_TRANSMIT));
 			}
 
 			if (data.getReceiveCoverage() != null) {
				xData.getCoverage().addAll(convert(data.getReceiveCoverage(), DerbyDBInterface.COVERAGE_RECEIVE));
 			}
 			
 			if (data.getInterferenceCoverage() != null) {
				xData.getCoverage().addAll(convert(data.getInterferenceCoverage(), DerbyDBInterface.COVERAGE_INTERFERENCE));
 			}
 			
 			xData.setAntenna(convert(data.getAntenna()));
 			
 			xData.setFatdmaAllocation(convert(data.getFatdmaAllocation()));
 			if (data.getStationType() != null) {
 				xData.setStationType(AisFixedStationType.valueOf(data
 						.getStationType().toString()));
 			}
 			if (data.getStatus() != null) {
 				xData.setStatus(convert(data.getStatus()));
 			}
 			if (data.getAnything() != null) {
 				xData.getAny().addAll(data.getAnything());
 			}
 
 			return xData;
 		}
 		return null;
 	}
 
 	private static List<AisFixedStationCoverage> convert(
 			dk.frv.eavdam.data.AISFixedStationCoverage coverage, int coverageType) {
		if (coverage != null) {
 			List<AisFixedStationCoverage> xCoverage = new ArrayList<AisFixedStationCoverage>();
 
 			AisFixedStationCoverage c = new AisFixedStationCoverage();
 			switch(coverageType){
 				case DerbyDBInterface.COVERAGE_INTERFERENCE: c.setCoverageType(AisFixedStationCoverageType.INTERFERENCE); break;
 				case DerbyDBInterface.COVERAGE_TRANSMIT: c.setCoverageType(AisFixedStationCoverageType.TRANSMISSION); break;
 				case DerbyDBInterface.COVERAGE_RECEIVE: c.setCoverageType(AisFixedStationCoverageType.RECEIVE); break;
 			}
 			
 			for(double[] p : coverage.getCoveragePoints()){
 				CoveragePoint cp = new CoveragePoint();
 				cp.setLat(p[0]);
 				cp.setLon(p[1]);
 				c.getPoint().add(cp);
 			}
 			
 			xCoverage.add(c);
 			// TODO: coverage
 			return xCoverage;
 		}
 		return null;
 	}
 
 	private static Status convert(dk.frv.eavdam.data.AISFixedStationStatus status){
 		if(status != null){
 			Status xStatus = new Status();
 			if(status.getStatusID() == DerbyDBInterface.STATUS_ACTIVE){
 				xStatus.setStatus(AisFixedStationStatus.OPERATIVE);
 			}else if(status.getStatusID() == DerbyDBInterface.STATUS_OLD){
 				xStatus.setStatus(AisFixedStationStatus.OLD);
 			} else if(status.getStatusID() == DerbyDBInterface.STATUS_SIMULATED){
 				xStatus.setStatus(AisFixedStationStatus.SIMULATED);
 			} else if(status.getStatusID() == DerbyDBInterface.STATUS_PLANNED){
 				xStatus.setStatus(AisFixedStationStatus.PLANNED);
 			} else if(status.getStatusID() == DerbyDBInterface.STATUS_PROPOSED){
 				xStatus.setStatus(AisFixedStationStatus.PROPOSED);
 			}  
 			
 			if(status.getStartDate() != null){
 				try{
 					GregorianCalendar c = new GregorianCalendar();
 					c.setTime(status.getStartDate());
 					XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
 					xStatus.setStartDate(date2);
 				}catch(Exception e){
 					e.printStackTrace();
 				}
 			}	
 			
 			if(status.getEndDate() != null){
 				try{
 					GregorianCalendar c = new GregorianCalendar();
 					
 					c.setTime(status.getEndDate());
 					XMLGregorianCalendar date2 = DatatypeFactory.newInstance().newXMLGregorianCalendar(c);
 					xStatus.setEndDate(date2);
 				}catch(Exception e){
 					e.printStackTrace();
 				}
 			}
 			
 			return xStatus;
 		}
 		
 		
 		return null;
 	}
 	
 	private static Antenna convert(dk.frv.eavdam.data.Antenna antenna) {
 		if (antenna != null) {
 			Antenna xAntenna = new Antenna();
 			if (antenna.getAntennaType() == AntennaType.OMNIDIRECTIONAL) {
 				xAntenna.setOmnidirectionalAntenna(new OmnidirectionalAntenna());
 			} else {
 				DirectionalAntenna da = new DirectionalAntenna();
 				da.setHeading(antenna.getHeading());
 				da.setFieldOfViewAngle(antenna.getFieldOfViewAngle());
 				da.setGain(antenna.getGain());
 				xAntenna.setDirectionalAntenna(da);
 			}
 			xAntenna.setAntennaHeight(antenna.getAntennaHeight());
 			xAntenna.setTerrainHeight(antenna.getTerrainHeight());
 			return xAntenna;
 		}
 		return null;
 	}
 
 	private static FatdmaSlotAllocation convert(dk.frv.eavdam.data.FATDMASlotAllocation allocation) {
 		if (allocation != null && allocation.getAllocations() != null) {
 			FatdmaSlotAllocation xAllocation = new FatdmaSlotAllocation();
 			for(Integer f : allocation.getAllocations()){
 				xAllocation.getFatdmaAllocationID().add(f.intValue()+"");
 			}
 			
 			
 			return xAllocation;
 		}
 		return null;
 	}
 }
