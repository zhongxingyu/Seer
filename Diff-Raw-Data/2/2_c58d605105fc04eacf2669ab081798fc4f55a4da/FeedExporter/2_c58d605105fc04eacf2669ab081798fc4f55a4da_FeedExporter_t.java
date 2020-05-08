 package nmd.rss.collector.exporter;
 
 import nmd.rss.collector.feed.FeedHeader;
 import nmd.rss.collector.feed.FeedItem;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import java.io.StringWriter;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Locale;
 
 import static nmd.rss.collector.util.Assert.assertNotNull;
 
 /**
  * Author : Igor Usenko ( igors48@gmail.com )
  * Date : 16.05.13
  */
 public final class FeedExporter {
 
     private static final String FEED_HEADER = "<rss version=\"2.0\">";
     private static final String FEED_FOOTER = "</rss>";
     private static final DateFormat DATE_FORMAT = new SimpleDateFormat("E, d MMM yyyy HH:mm:ss Z", Locale.US);
 
     public static String export(final FeedHeader header, final List<FeedItem> items) throws FeedExporterException {
         assertNotNull(header);
         assertNotNull(items);
 
         try {
             final List<Item> channelItems = new ArrayList<>();
 
             for (final FeedItem current : items) {
                 final Item item = new Item();
 
                 item.setPubDate(DATE_FORMAT.format(current.date));
                 item.setDescription(current.description);
                 item.setLink(current.link);
                item.setGuid(current.guid);
                 item.setTitle(current.title);
 
                 channelItems.add(item);
             }
 
             Channel channel = new Channel();
 
             channel.setTitle(header.title);
             channel.setLink(header.link);
             channel.setDescription(header.description);
             channel.setItems(channelItems);
 
             final JAXBContext JAXB_CONTEXT = JAXBContext.newInstance(Channel.class, Item.class);
             final Marshaller marshaller = JAXB_CONTEXT.createMarshaller();
             marshaller.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
 
             final StringWriter stringWriter = new StringWriter();
             marshaller.marshal(channel, stringWriter);
 
             return FEED_HEADER + stringWriter.toString() + FEED_FOOTER;
         } catch (JAXBException exception) {
             throw new FeedExporterException(exception);
         }
     }
 
     private FeedExporter() {
         // empty
     }
 
 }
