 package de.ronnyfriedland.time.config;
 
 import junit.framework.Assert;
 
 import org.junit.Test;
 
 public class MessageTest {
 
    private static final Object[] TEXT_PROPERTIES = new Object[] { "test1", "test2" };
 
    @Test
    public void testMessageProperties() {
        for (Messages message : Messages.values()) {
            String formattedMessage = null;
            switch (message) {
            case EXPORT_DURATION:
                formattedMessage = message.getMessage(new Integer(1), "test");
                break;
            case EXPORT_SUMMARY:
                formattedMessage = message.getMessage(new Float(1), new Integer(2));
                break;
            default:
                formattedMessage = message.getMessage(TEXT_PROPERTIES);
                break;
            }
            Assert.assertNotNull(formattedMessage);
            Assert.assertFalse(formattedMessage.startsWith("!"));
            Assert.assertFalse(formattedMessage.endsWith("!"));
        }
 
    }
 }
