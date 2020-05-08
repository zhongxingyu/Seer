 package pleocmd.pipe;
 
 import static org.junit.Assert.assertEquals;
 import static org.junit.Assert.assertTrue;
 import static org.junit.Assert.fail;
 
 import java.io.IOException;
 
 import org.junit.Test;
 
 import pleocmd.Log;
 import pleocmd.Testcases;
 import pleocmd.cfg.ConfigString;
 import pleocmd.exc.ConfigurationException;
 import pleocmd.exc.PipeException;
 
 public final class PipePartTest extends Testcases {
 
 	private final ConfigString cfg0 = new ConfigString("Config A", false);
 	private final ConfigString cfg1 = new ConfigString("Config B", true);
 
 	@SuppressWarnings("synthetic-access")
 	private PipePart createPipePart() {
 		return new PipePart() {
 			{
 				addConfig(cfg0);
 				addConfig(cfg1);
 				constructed();
 			}
 
 			@Override
 			protected void init0() throws PipeException, IOException {
 				throw new UnsupportedOperationException();
 			}
 
 			@Override
 			protected void configure0() throws PipeException, IOException {
 				// just ignore
 			}
 
 			@Override
 			protected void close0() throws PipeException, IOException {
 				throw new UnsupportedOperationException();
 			}

			@Override
			public String getInputDescription() {
				return "";
			}

			@Override
			public String getOutputDescription() {
				return "";
			}

 		};
 	}
 
 	@Test
 	public void testPipePart() throws ConfigurationException {
 		final PipePart pp = createPipePart();
 		Log.consoleOut("Constructed new PipePart '%s'", pp);
 
 		try {
 			pp.addConfig(new ConfigString("CanNeverBeAdded", false));
 			fail("addConfig() allows adding after construction");
 		} catch (final IllegalStateException e) {
 			assertTrue(e.toString(), e.getMessage().startsWith("Cannot add"));
 		}
 		Log.consoleOut("Checked owner management of PipePart '%s'", pp);
 
 		assertEquals(2, pp.getGroup().getSize());
 		cfg0.setContent("foo");
 		cfg1.setContent("foo\nbar");
 		assertEquals("ConfigValue not referenced", cfg0.getContent(),
 				((ConfigString) pp.getGroup().get("Config A")).getContent());
 		Log.consoleOut("Checked Group of PipePart '%s'", pp);
 	}
 }
