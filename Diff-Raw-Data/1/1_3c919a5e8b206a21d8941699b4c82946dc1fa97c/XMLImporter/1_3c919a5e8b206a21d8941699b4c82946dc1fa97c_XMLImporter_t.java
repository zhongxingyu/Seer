 package dk.frv.eavdam.io;
 
 import java.io.File;
 import java.net.MalformedURLException;
 import java.util.List;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBElement;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import org.w3c.dom.Element;
 
 import dk.frv.eavdam.data.AISFixedStationStatus;
 import dk.frv.eavdam.data.AISFixedStationType;
 import dk.frv.eavdam.data.AntennaType;
 import dk.frv.eavdam.data.EAVDAMData;
 import dk.frv.eavdam.io.jaxb.Address;
 import dk.frv.eavdam.io.jaxb.AisFixedStationCoverage;
 import dk.frv.eavdam.io.jaxb.AisFixedStationData;
 import dk.frv.eavdam.io.jaxb.Antenna;
 import dk.frv.eavdam.io.jaxb.EavdamData;
 import dk.frv.eavdam.io.jaxb.EavdamUser;
 import dk.frv.eavdam.io.jaxb.FatdmaSlotAllocation;
 import dk.frv.eavdam.io.jaxb.Person;
 
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
 			data.setCoverage(convert(xData.getCoverage()));
 			data.setAntenna(convert(xData.getAntenna()));
 			data.setFatdmaAllocation(convert(xData.getFatdmaAllocation()));
 			if (xData.getStationType() != null) {
 				data.setStationType(AISFixedStationType.valueOf(xData
 						.getStationType().toString()));
 			}
 			if (xData.getStatus() != null) {
 				data.setStatus(AISFixedStationStatus.valueOf(xData.getStatus()
 						.toString()));
 			}
 			List<Element> anything = xData.getAny();
 			data.setAnything(anything);
 			
 			return data;
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
 			List<AisFixedStationCoverage> xDataList) {
 		if (xDataList != null) {
 
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
