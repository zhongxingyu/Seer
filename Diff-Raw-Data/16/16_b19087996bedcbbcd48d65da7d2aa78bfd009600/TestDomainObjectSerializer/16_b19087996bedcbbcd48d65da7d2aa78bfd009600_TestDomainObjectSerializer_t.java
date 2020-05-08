 
 package unit.basic;
 
 import java.io.IOException;
 import javax.xml.stream.XMLStreamException;
 import javax.xml.stream.XMLStreamReader;
 import com.cedarsoft.Version;
 import com.cedarsoft.VersionException;
 import com.cedarsoft.VersionRange;
 import com.cedarsoft.serialization.stax.AbstractStaxMateSerializer;
 import org.codehaus.staxmate.out.SMOutputElement;
 
 public class TestDomainObjectSerializer
     extends AbstractStaxMateSerializer<TestDomainObject>
 {
 
     public final static String ELEMENT_DESCRIPTION = "description";
     public final static String ATTRIBUTE_ID = "id";
 
     public TestDomainObjectSerializer() {
        super("testdomainobject", "http://basic.unit/test-domain-object", VersionRange.from(1, 0, 0).to(1, 0, 0));
     }
 
     @Override
     public void serialize(SMOutputElement serializeTo, TestDomainObject object)
         throws IOException, XMLStreamException
     {
         //description
         serializeTo.addElementWithCharacters(serializeTo.getNamespace(), ELEMENT_DESCRIPTION, object.getDescription());
         //id
         serializeTo.addAttribute(ATTRIBUTE_ID, String.valueOf(object.getId()));
     }
 
     @Override
     public TestDomainObject deserialize(XMLStreamReader deserializeFrom, Version formatVersion)
         throws VersionException, IOException, XMLStreamException
     {
         //description
         String description = getChildText(deserializeFrom, ELEMENT_DESCRIPTION);
         //id
         int id = Integer.parseInt(deserializeFrom.getAttributeValue(null, ATTRIBUTE_ID));
         //Finally closing element
         closeTag(deserializeFrom);
         //Constructing the deserialized object
         TestDomainObject object = new TestDomainObject(description, id);
         return object;
     }
 
 }
