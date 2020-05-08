 package fr.liglab.adele.rondo.parser.impl;
 
 import java.io.File;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Unmarshaller;
 
 import org.apache.felix.ipojo.annotations.Component;
 import org.apache.felix.ipojo.annotations.Instantiate;
 import org.apache.felix.ipojo.annotations.Provides;
 
 import fr.liglab.adele.rondo.RondoParser;
 import fr.liglab.adele.rondo.exception.RondoParserException;
 import fr.liglab.adele.rondo.model.ObjectFactory;
 import fr.liglab.adele.rondo.model.Rondo;
 
 @Component(immediate = true)
 @Provides
 @Instantiate
 public class RondoJAXBParserImpl implements RondoParser {
 
 	private final String NAMESPACE = "fr.liglab.adele.rondo.model";
 
 	JAXBContext jc;
 
 	public RondoJAXBParserImpl() {
 		try {
 			ClassLoader cl = ObjectFactory.class.getClassLoader();
 			jc = JAXBContext.newInstance(NAMESPACE, cl);
 		} catch (JAXBException e) {
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 
 	@Override
 	public Rondo parse(File file) throws RondoParserException {
 		try {
 			Unmarshaller unmarshaller = jc.createUnmarshaller();
 			Rondo rondo = (Rondo) unmarshaller.unmarshal(file);
 			return rondo;
 		} catch (JAXBException e) {
			e.printStackTrace();
 			throw new RondoParserException(e.getMessage());
 
 		}
 	}
 
 }
