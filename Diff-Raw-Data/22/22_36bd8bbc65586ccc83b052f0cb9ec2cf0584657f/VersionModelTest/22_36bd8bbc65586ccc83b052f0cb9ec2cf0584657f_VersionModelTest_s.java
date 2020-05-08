 package se.findout.tempo.client;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 
 public class VersionModelTest {
 	private VersionModel model;
 
 	@Before
 	public void setup() {
 		model = new VersionModel();
 	}
 	
 	@Test
 	public void testInitialState_oneNullHead() {
 		Assert.assertEquals(1, model.getHeads().size());
 		Assert.assertNull(model.getHeads().get(0).getBase());
 		Assert.assertNull(model.getHeads().get(0).getChange());
 	}
 
 	@Test
 	public void testAddVersion_fromEmptyNoBranch() {
 		model.addVersion(model.getHeads().get(0), new NUllChange());
 		Assert.assertEquals(1, model.getHeads().size());
 	}
 
 	@Test
 	public void testAddVersion_makeBranch() {
 		Version v1 = model.getHeads().get(0);
 		Version v2 = model.addVersion(v1, new NUllChange());
 		Version v11 = model.addVersion(v1, new NUllChange());
 		Version v12 = model.addVersion(v11, new NUllChange());
 		Assert.assertEquals(2, model.getHeads().size());
 		Assert.assertEquals(v1, v2.getBase());
 		Assert.assertEquals(v1, v11.getBase());
 		
 		Assert.assertEquals("1", v1.getName());
 		Assert.assertEquals("2", v2.getName());
 		Assert.assertEquals("1.1", v11.getName());
 	}
 	
 	@Test
 	public void testGetInitialVersion() {
 		Version initialVersion = model.getInitialVersion();
 		Assert.assertNotNull(initialVersion);
 		Assert.assertNull(initialVersion.getBase());
 		Assert.assertNull(initialVersion.getChange());
 	}	
 
 	@Test
 	public void testIncVersion() {
 		Assert.assertEquals("2", model.incVersion("1"));
 		Assert.assertEquals("1.2", model.incVersion("1.1"));
 		
 	}
 	
 	@Test
 	public void testSwitchVersion_straight_up() {
 		StringBuilder log = new StringBuilder();
 		Version v1 = model.getHeads().get(0);
 		Version v2 = model.addVersion(v1, new LogChange(log, "v2"));
 		Version v3 = model.addVersion(v2, new LogChange(log, "v3"));
 		Version v4 = model.addVersion(v3, new LogChange(log, "v4"));
 
 		model.switchVersion(v1,  v4, changeIterator);
 		Assert.assertEquals("ev2 ev3 ev4 ", log.toString());
 	}
 	
 	@Test
 	public void testSwitchVersion_straight_down() {
 		StringBuilder log = new StringBuilder();
 		Version v1 = model.getHeads().get(0);
 		Version v2 = model.addVersion(v1, new LogChange(log, "v2"));
 		Version v3 = model.addVersion(v2, new LogChange(log, "v3"));
 		Version v4 = model.addVersion(v3, new LogChange(log, "v4"));
 
 		model.switchVersion(v4,  v1, changeIterator);
 		Assert.assertEquals("uv4 uv3 uv2 ", log.toString());
 	}
 	
 	@Test
 	public void testSwitchVersion_same() {
 		StringBuilder log = new StringBuilder();
 		Version v1 = model.getHeads().get(0);
 		Version v2 = model.addVersion(v1, new LogChange(log, "v2"));
 
 		model.switchVersion(v2,  v2, changeIterator);
 		Assert.assertEquals("", log.toString());
 	}
 	
 	@Test
 	public void testGetVersionById() {
 		StringBuilder log = new StringBuilder();
 		Version v1 = model.getHeads().get(0);
 		Version v2 = model.addVersion(v1, new LogChange(log, "v2"));
 		Version v3 = model.addVersion(v2, new LogChange(log, "v3"));
 		Version v21 = model.addVersion(v2, new LogChange(log, "v21"));
 		Version v22 = model.addVersion(v21, new LogChange(log, "v22"));
 
 		Assert.assertEquals(v1, model.getVersionById("1"));
 		Assert.assertEquals(v22, model.getVersionById("2.2"));
 	}
 	
 	@Test
 	public void testSwitchVersion_branch() {
 		StringBuilder log = new StringBuilder();
 		Version v1 = model.getHeads().get(0);
 		Version v2 = model.addVersion(v1, new LogChange(log, "v2"));
 		Version v3 = model.addVersion(v2, new LogChange(log, "v3"));
 		Version v21 = model.addVersion(v2, new LogChange(log, "v21"));
 		Version v22 = model.addVersion(v21, new LogChange(log, "v22"));
 
 		model.switchVersion(v3,  v22, changeIterator);
 		Assert.assertEquals("uv3 ev21 ev22 ", log.toString());
 	}
 	
 	VersionModel.ChangeIterator changeIterator = new VersionModel.ChangeIterator() {
 		
 		@Override
 		public void undo(Command change) {
 			change.undo(null);
 		}
 		
 		@Override
 		public void execute(Command change) {
 			change.execute(null);
 		}
 	};
 
 	public class LogChange implements Command {

 		private StringBuilder log;
 		private String name;
 
 		public LogChange(StringBuilder log, String name) {
 			this.log = log;
 			this.name = name;
 		}
 
 		@Override
 		public void execute(ModelModel modelModel) {
 			log.append("e" + name + " ");
 		}
 
 		@Override
 		public void undo(ModelModel modelModel) {
 			log.append("u" + name + " ");
 		}
 
 		@Override
 		public String getDescription() {
 			return "Log change";
 		}
 
 	}
 
 }
