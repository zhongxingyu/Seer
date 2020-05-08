 package org.atlasapi.media.channel;
 
 import static org.hamcrest.Matchers.equalTo;
 import static org.hamcrest.Matchers.is;
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertThat;
 
 import java.util.List;
 
 import org.atlasapi.media.entity.Publisher;
 import org.junit.Test;
 
 import com.google.common.collect.ImmutableSet;
 import com.google.common.collect.Lists;
 import com.metabroadcast.common.intl.Countries;
 import com.mongodb.DBObject;
 
 public class ChannelGroupTranslatorTest {
 
     private final ChannelGroupTranslator channelGroupTranslator = new ChannelGroupTranslator();
     
     @Test
     public void testEncodesAndDecodedChannelGroup() {
         
         ChannelGroup channelGroup = new Platform();
         channelGroup.setId(5678L);
         channelGroup.setAvailableCountries(ImmutableSet.of(Countries.US,Countries.GB));
         channelGroup.addTitle("Title");
         channelGroup.setPublisher(Publisher.BBC);
        channelGroup.setChannels(Lists.newArrayList(1234L, 1235L, 1236L));
         
         ChannelNumbering numbering1 = ChannelNumbering.builder()
                 .withChannel(1234L)
                 .withChannelNumber("1")
                 .withChannelGroup(channelGroup)
                 .build();
         
         ChannelNumbering numbering2 = ChannelNumbering.builder()
                 .withChannel(1235L)
                 .withChannelNumber("7")
                 .withChannelGroup(channelGroup)
                 .build();
         
         ChannelNumbering numbering3 = ChannelNumbering.builder()
                 .withChannel(1236L)
                 .withChannelNumber("89")
                 .withChannelGroup(channelGroup)
                 .build();
         
         List<ChannelNumbering> channelNumberings = Lists.newArrayList(numbering1, numbering2, numbering3);
         
         channelGroup.setChannelNumberings(channelNumberings);
         
         DBObject encoded = channelGroupTranslator.toDBObject(null, channelGroup);
         
         ChannelGroup decoded = channelGroupTranslator.fromDBObject(encoded, null);
         
        // add channelgroups constructed from channels
        channelGroup.addChannelNumbering(ChannelNumbering.builder()
                .withChannel(1234L)
                .withChannelGroup(5678L)
                .build());
        channelGroup.addChannelNumbering(ChannelNumbering.builder()
                .withChannel(1235L)
                .withChannelGroup(5678L)
                .build());
        channelGroup.addChannelNumbering(ChannelNumbering.builder()
                .withChannel(1236L)
                .withChannelGroup(5678L)
                .build());
        
         assertThat(decoded.getAvailableCountries(), is(equalTo(channelGroup.getAvailableCountries())));
         assertThat(decoded.getPublisher(), is(equalTo(channelGroup.getPublisher())));
         assertThat(decoded.getTitle(), is(equalTo(channelGroup.getTitle())));
         assertThat(decoded.getChannels(), is(equalTo(channelGroup.getChannels())));
         assertEquals(channelGroup.getChannelNumberings(), decoded.getChannelNumberings());
     }
 
 }
