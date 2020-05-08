 package dk.nsi.sdm2.core.parser;
 
 import dk.nsi.sdm2.core.domain.AbstractRecord;
 import dk.nsi.sdm2.core.persist.RecordPersister;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.mockito.ArgumentCaptor;
 import org.mockito.InjectMocks;
 import org.mockito.Mock;
 import org.mockito.runners.MockitoJUnitRunner;
 import org.springframework.oxm.Unmarshaller;
 
 import javax.xml.transform.stream.StreamSource;
 import java.io.File;
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.List;
 
import static java.util.Arrays.asList;
 import static org.junit.Assert.assertEquals;
 import static org.mockito.Matchers.any;
 import static org.mockito.Mockito.verify;
 import static org.mockito.Mockito.when;
 
 @RunWith(MockitoJUnitRunner.class)
 public class SimpleParserTest {
     @Mock
     Unmarshaller unmarshaller;
 
     @Mock
     RecordPersister recordPersister;
 
     @InjectMocks
     Parser thingParser = new SimpleParser<ThingRecord, Things, Thing>() {
         @Override
         protected Collection<Thing> getTypes(Things type) {
             return type.getThingList();
         }
 
         @Override
         public ThingRecord transform(final Thing thing) {
             return new ThingRecord() {{
                 content = thing.content;
             }};
         }
     };
 
     static class ThingRecord extends AbstractRecord {
         String content;
     }
 
     static class Things {
         List<Thing> thingList = new ArrayList<Thing>();
 
         public List<Thing> getThingList() {
             return thingList;
         }
     }
 
     static class Thing {
         String content;
     }
 
     @Test
     public void canProcess() throws Exception {
         File dataSet = new File("/dev/null");
         Things things = new Things() {{
             thingList.add(new Thing() {{
                 content = "TEST";
             }});
         }};
         when(unmarshaller.unmarshal(any(StreamSource.class))).thenReturn(things);
 
         thingParser.process(dataSet, recordPersister);
 
         ArgumentCaptor<Collection> recordCollectionCaptor = ArgumentCaptor.forClass(Collection.class);
         verify(recordPersister).persist(recordCollectionCaptor.capture());
         List<ThingRecord> collection = new ArrayList<ThingRecord>(recordCollectionCaptor.getValue());
         assertEquals(1, collection.size());
         assertEquals("TEST", collection.get(0).content);
     }
 }
