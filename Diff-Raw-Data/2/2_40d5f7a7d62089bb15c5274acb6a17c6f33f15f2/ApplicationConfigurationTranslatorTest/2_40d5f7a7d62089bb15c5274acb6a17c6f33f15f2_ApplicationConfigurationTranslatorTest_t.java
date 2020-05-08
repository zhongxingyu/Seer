 package org.atlasapi.application.v3;
 
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 import static org.junit.Assert.assertTrue;
 
 import org.atlasapi.application.v3.ApplicationConfiguration;
 import org.atlasapi.application.v3.ApplicationConfigurationTranslator;
 import org.atlasapi.media.entity.Publisher;
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableList;
 import com.google.common.collect.ImmutableSet;
 import com.mongodb.DBObject;
 
 public class ApplicationConfigurationTranslatorTest {
 
     private final ApplicationConfigurationTranslator codec = new ApplicationConfigurationTranslator();
     
     @Test
     public void testEncodesAndDecodesApplicationConfiguration() {
         
         ApplicationConfiguration config = ApplicationConfiguration.defaultConfiguration()
                 .agreeLicense(Publisher.BBC)
                 .enable(Publisher.BBC)
                 .request(Publisher.ITV)
                 .approve(Publisher.ITV)
                 .enable(Publisher.ITV)
                 .request(Publisher.NETFLIX)
                 .copyWithPrecedence(ImmutableList.of(Publisher.ITV, Publisher.BBC))
                 .copyWithWritableSources(ImmutableSet.of(Publisher.ITV));
         
         DBObject dbo = codec.toDBObject(config);
         
         ApplicationConfiguration decoded = codec.fromDBObject(dbo);
         
         assertTrue(decoded.isEnabled(Publisher.ITV));
         assertTrue(decoded.isEnabled(Publisher.BBC));
         assertThat(decoded.orderdPublishers().get(0), is(Publisher.ITV));
         assertThat(decoded.orderdPublishers().get(1), is(Publisher.BBC));
         assertTrue(decoded.canWrite(Publisher.ITV));
         assertEquals(SourceStatus.REQUESTED, decoded.statusOf(Publisher.NETFLIX));
        assertEquals(SourceStatus.AVAILABLE_DISABLED, decoded.statusOf(Publisher.PA));
     }
 
 }
