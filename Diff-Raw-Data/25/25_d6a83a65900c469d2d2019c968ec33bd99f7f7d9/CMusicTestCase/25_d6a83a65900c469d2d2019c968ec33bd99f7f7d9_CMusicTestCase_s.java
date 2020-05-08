 package waazdoh.client;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.net.MalformedURLException;
 import java.nio.channels.FileChannel;
 import java.util.HashSet;
 import java.util.Set;
 
 import junit.framework.TestCase;
 
 import org.xml.sax.SAXException;
 
 import waazdoh.MEnvironmentImpl;
 import waazdoh.MEnvironment;
 import waazdoh.WaazdohMusicInfo;
 import waazdoh.client.rest.RestClient;
 import waazdoh.common.model.AudioInfo;
 import waazdoh.common.model.AudioSample;
 import waazdoh.common.model.Instrument;
 import waazdoh.common.model.InstrumentTrack;
 import waazdoh.common.model.MOutput;
 import waazdoh.common.model.Part;
 import waazdoh.common.model.Song;
 import waazdoh.common.model.Track;
 import waazdoh.common.model.TrackGroup;
 import waazdoh.cp2p.impl.P2PBinarySource;
 import waazdoh.cutils.JBeanResponse;
 import waazdoh.cutils.MLogger;
 import waazdoh.cutils.MPreferences;
 import waazdoh.cutils.MStringID;
 import waazdoh.service.CMService;
 import waazdoh.testing.ServiceMock;
 import waazdoh.testing.TestPWaveSource;
 
 public abstract class CMusicTestCase extends TestCase {
 	private Set<MEnvironment> clients = new HashSet<MEnvironment>();
 	MLogger log = MLogger.getLogger(this);
 	private int usercounter = 0;
 
 	@Override
 	protected synchronized void setUp() throws Exception {
 		log.info("**************************************** [[[   SETTING UP TEST   ]]] *********************************");
 		//
 		createCommonSource();
 		//
 		super.setUp();
 	}
 
 	@Override
 	protected void tearDown() throws Exception {
 		for (MEnvironment c : clients) {
 			c.stop();
 		}
 		clients = new HashSet<MEnvironment>();
 	}
 
 	public void sleep(int i) {
 		synchronized (this) {
 			try {
 				this.wait(i);
 			} catch (InterruptedException e) {
 				e.printStackTrace();
 			}
 		}
 	}
 
 	public MEnvironment getNewDumbClient() throws MalformedURLException {
 		TestPreferences p = new TestPreferences("test");
 		MEnvironment c = new MEnvironmentImpl(p, getDumbWaveSource(p));
 		return c;
 	}
 
 	private MBinarySource getDumbWaveSource(TestPreferences p) {
 		return new TestPWaveSource(p);
 	}
 
 	protected MEnvironment getNewClient() throws IOException, SAXException {
 		boolean bind = usercounter >= 0 ? true : false;
 		String username = "test_username_" + (usercounter++) + "@localhost";
 		return getNewClient(username, bind);
 	}
 
 	public MEnvironment getNewClient(final String email, boolean bind)
 			throws MalformedURLException, SAXException {
 		//
 		TestPreferences p = new TestPreferences(email);
 		MBinarySource waveSource = getWaveSource(p, bind);
 		MEnvironment c = new MEnvironmentImpl(p, waveSource, getTestService(p,
 				waveSource));
 		waveSource.setReportingService(c);
 
 		boolean setsession = c.getClient().setUsernameAndSession(email,
 				getSession(p));
 		if (setsession) {
 			clients.add(c);
 			return c;
 		} else {
 			String appidparam = "appid_" + email;
 			String sid = p.get(appidparam);
 			MStringID id;
 			if (sid != null && sid.length() > 0) {
 				id = new MStringID(sid);
 			} else {
 				id = new MStringID();
 				p.set(appidparam, id.toString());
 			}
 			String session = c.getService().requestAppLogin(email, "testapp",
 					id);
 			if (session != null) {
 				p.set("session", session);
 			}
 			return null;
 		}
 	}
 
 	private CMService getTestService(TestPreferences p, MBinarySource source)
 			throws SAXException {
 		try {
 			RestClient client = new RestClient(p.get(MPreferences.SERVICE_URL),
 					source);
 			if (client.isConnected()) {
 				return client;
 			}
 		} catch (MalformedURLException e) {
 			e.printStackTrace();
 		}
 		return new ServiceMock(source);
 	}
 
 	private String getSession(TestPreferences p) {
 		return p.get("session");
 	}
 
 	private void copyInstallationTo(final String path) throws IOException {
 		String inspath = "./target/cmclient-distribution/linux/";
 		File file = new File(inspath);
 		copyFilesTo(file, new File(path));
 	}
 
 	private void copyFilesTo(File file, File dstpath) throws IOException {
 		if (file.isDirectory()) {
 			file.mkdirs();
 			dstpath.mkdirs();
 			//
 			String[] list = file.list();
 			for (final String string : list) {
 				File src = new File(file.getAbsolutePath() + File.separator
 						+ string);
 				File dst = new File(dstpath + File.separator + string);
 				if (src.isDirectory()) {
 					dst.mkdirs();
 					copyFilesTo(src, dst);
 				} else {
 					FileChannel inchannel = new FileInputStream(src)
 							.getChannel();
 					FileChannel outchannel = new FileOutputStream(dst)
 							.getChannel();
 					inchannel.transferTo(0, inchannel.size(), outchannel);
 					if (dst.getName().equals("utserver")) {
 						dst.setExecutable(true);
 					}
 					inchannel.close();
 					outchannel.close();
 				}
 			}
 		}
 	}
 
 	public MBinarySource getWaveSource(MPreferences p, boolean bind) {
 		P2PBinarySource testsource = new P2PBinarySource(p, bind);
 		if (bind) {
 			testsource.setDownloadEverything(true);
 		}
 		// JXSEStreamSource testsource = new JXSEStreamSource(p);
 		return testsource;
 	}
 
 	public void createCommonSource() throws IOException {
 		// getNewClient("testdownloaderuser", true);
 	}
 
 	public void testTrue() {
 		assertTrue(true);
 	}
 
 	public void compareOutputs(MOutput outputwave, MOutput boutputwave) {
 		int index = 0;
 		AudioInfo audioInfo = outputwave.getAudioInfo();
 		AudioSample sa = new AudioSample();
 		AudioSample sb = new AudioSample();
 
 		while (index < audioInfo.getSampleCount()) {
 			sa.reset();
 			sb.reset();
 			//
 			boolean ba = outputwave.getSample(index, sa);
 			boolean bb = boutputwave.getSample(index, sb);
 			assert (ba && bb || (!ba && !bb));
 			//
 			Float a = sa.mix();
 			Float b = sb.mix();
 
 			float ab = b - a;
 			if (ab < 0) {
 				ab = -ab;
 			}
 			if (ab > WaazdohMusicInfo.MAX_RESOLUTION) {
 				index -= 100;
 				for (int count = 0; count < 200; count++) {
 					sa.reset();
 					sb.reset();
 					//
 					boolean saok = outputwave.getSample(index, sa);
 					boolean sbok = boutputwave.getSample(index, sb);
 					if (saok || sbok) {
 						a = sa.mix();
 						b = sb.mix();
 
 						String mes = "i:" + index + " a!=b " + a + " b:" + b;
 						log.info(mes);
 					}
 					//
 					index++;
 				}
 				String mes = "i:" + index + " a!=b a:" + a + " b:" + b + "ab:"
 						+ ab;
 				assertEquals("e", mes);
 			}
 
 			index++;
 		}
 	}
 
 	void doAssertEquals(Song s, Song bsong) {
 		for (int i = 0; i < s.getParts().size(); i++) {
 			doAssertEquals(s.getParts().get(i), bsong.getParts().get(i));
 		}
 		assertEquals(s.getBean().toXML(), bsong.getBean().toXML());
 	}
 
 	void doAssertEquals(Part part, Part part2) {
 		for (int i = 0; i < part.getTrackGroups().size(); i++) {
 			doAssertEquals(part.getTrackGroups().get(0), part2.getTrackGroups()
 					.get(i));
 		}
 		assertEquals(part.getBean().toXML(), part2.getBean().toXML());
 	}
 
 	void doAssertEquals(TrackGroup trackGroup, TrackGroup trackGroup2) {
 		for (int i = 0; i < trackGroup.getTracks().size(); i++) {
 			doAssertEquals(trackGroup.getTracks().get(0), trackGroup2
 					.getTracks().get(0));
 		}
 
 		assertEquals(trackGroup.getBean().toXML(), trackGroup2.getBean()
 				.toXML());
 		assertEquals(trackGroup.getID(), trackGroup2.getID());
 	}
 
 	private void doAssertEquals(Track track, Track track2) {
 		if (track instanceof InstrumentTrack) {
 			InstrumentTrack aitrack = (InstrumentTrack) track;
 			InstrumentTrack bitrack = (InstrumentTrack) track2;
 			assertEquals(aitrack.getInstrument().getBean().toText(), bitrack
 					.getInstrument().getBean().toText());
 
 		}
 		assertEquals(track.getBean().toXML(), track2.getBean().toXML());
 	}
 
 	protected void initSin(Instrument i) {
 		assertTrue(i
				.setScript("function info() { return \"TEST\"; } function get(v, rv) { return Math.sin(1000.0 * rv.getValue('t') * rv.getValue('f'))  }"));
 	}
 }
