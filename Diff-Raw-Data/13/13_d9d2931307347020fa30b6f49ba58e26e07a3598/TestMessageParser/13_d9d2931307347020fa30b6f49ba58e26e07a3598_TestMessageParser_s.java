 package fr.ribesg.alix.message;
 
 import fr.ribesg.alix.api.enums.Command;
 import fr.ribesg.alix.api.message.Message;
 import fr.ribesg.alix.api.message.PongMessage;
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.junit.runners.Parameterized;
 import org.junit.runners.Parameterized.Parameters;
 
 import java.util.ArrayList;
 import java.util.Collection;
 
 @RunWith(Parameterized.class)
 public class TestMessageParser {
 
 	private final Message awaitedMessage;
 	private final String  message;
 
 	public TestMessageParser(final Message awaitedMessage, final String message) {
 		this.awaitedMessage = awaitedMessage;
 		this.message = message;
 	}
 
 	@Test
 	public void testParseMessage() {
 		final Message message = Message.parseMessage(this.message);
 		Assert.assertEquals("Prefix doesn't match", awaitedMessage.getPrefix(), message.getPrefix());
 		Assert.assertEquals("Command doesn't match", awaitedMessage.getRawCommandString(), message.getRawCommandString());
 		Assert.assertEquals("Trail doesn't match", awaitedMessage.getTrail(), message.getTrail());
 		Assert.assertArrayEquals("Parameters doesn't match", awaitedMessage.getParameters(), message.getParameters());
 	}
 
 	@Parameters
 	public static Collection<Object[]> data() {
 		final Collection<Object[]> data = new ArrayList<>();
 		data.add(new Object[] {
 				new PongMessage("Test"),
 				"PONG :Test"
 		});
 		data.add(new Object[] {
 				new Message("DSH105!~DSH@2607:5300:60:2464::1", Command.PRIVMSG.name(), "sgtcaze :o?", "#drtshock"),
 				":DSH105!~DSH@2607:5300:60:2464::1 PRIVMSG #drtshock :sgtcaze :o?"
 		});
 		data.add(new Object[] {
 				new Message("Ribesg", Command.USER.name(), "Ribesg", "Ribesg", "0", "*"),
 				":Ribesg USER Ribesg 0 * :Ribesg"
 		});
 		return data;
 	}
 }
