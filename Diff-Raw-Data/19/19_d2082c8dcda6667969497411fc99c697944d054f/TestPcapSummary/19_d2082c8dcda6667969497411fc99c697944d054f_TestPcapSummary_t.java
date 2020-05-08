 package com.dsa.pcapneo.domain;
 
 import static org.hamcrest.CoreMatchers.hasItems;
 import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import junit.framework.TestCase;
 
 import org.junit.Assert;
 import org.junit.Test;
 import org.junit.runner.RunWith;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.data.neo4j.support.Neo4jTemplate;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.dsa.pcapneo.domain.graph.Device;
 import com.dsa.pcapneo.domain.graph.DeviceType;
 import com.dsa.pcapneo.domain.graph.HttpSession;
 import com.dsa.pcapneo.domain.graph.IpAddress;
 import com.dsa.pcapneo.domain.graph.IpSession;
 import com.dsa.pcapneo.domain.graph.Protocol;
 import com.dsa.pcapneo.domain.graph.Session;
 import com.dsa.pcapneo.domain.graph.User;
 import com.dsa.pcapneo.domain.graph.WebSite;
 import com.dsa.pcapneo.domain.session.PcapSummary;
 import com.dsa.pcapneo.domain.session.SessionFactory;
 import com.dsa.pcapneo.graph.repositories.DeviceRepository;
 import com.dsa.pcapneo.graph.repositories.SessionArtefactFactory;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(("/testContext.xml"))
 public class TestPcapSummary extends TestCase {
 	@Autowired Neo4jTemplate template;
 	@Autowired SessionArtefactFactory factory;
 	@Autowired SessionFactory sessionFactory;
 	@Autowired DeviceRepository devRepo;
 
 	@Test
 	@Transactional
 	public void parseCsvString() {
 		String pcapStr = "1367956351.145376000,eth:ip:tcp:http,192.168.1.79,31.13.72.33,433,6,38314,80,,,http://search.maven.org/,";
 		PcapSummary pcap = null;
 		try {
 			pcap = new PcapSummary(pcapStr);
 		} catch (Exception e) {
 			Assert.fail("Failed to parse pcap str: " + e);
 		}
 		Session session = sessionFactory.createSession(pcap);
 		//Persist session
 		Long id = template.save(session).getSessionId();
 		//Check is a http session
 		assertEquals(session.getClass(), HttpSession.class);
 		HttpSession ret = template.findOne(id, HttpSession.class);
 		assertThat(ret.getDestPort().getPort(), is(80));
 		assertThat(ret.getSrcPort().getPort(), is(38314));
 		assertThat(ret.getDtoi(), is(1367956351L));
 		assertThat(ret.getIpDest().getIpAddr(), is("31.13.72.33"));
 		assertThat(ret.getIpSrc().getIpAddr(), is("192.168.1.79"));
 		assertThat(ret.getLength(), is(433));
 		//Check web site set
 		WebSite site = template.fetch(ret.getWebSite());
 		assertNotNull(site);
 		assertThat(site.getAddress(), is("search.maven.org"));
 		Device device = template.fetch(ret.getToDevice());
 		assertNotNull(device);
		assertTrue(device.getHostName(), device.getHostName().contains("31.13.72.33") || device.getHostName().contains("facebook.com"));
 		device = template.fetch(ret.getFromDevice());
 		assertNotNull(device);
 		assertThat(device.getHostName(), device.getHostName(), is("192.168.1.79"));
 	}
 
 	@Test
 	@Transactional
 	public void checkSessionRelationships() {
 		Device device = new Device("dans-laptop", template.save(new DeviceType("laptop")), template.save(new User("user1")));
 		String ip1 = "192.168.1.79";
 		IpAddress ip = template.save(new IpAddress(ip1));
 		device.addIpAddr(ip);
 		template.save(device);
 		String pcapStr = "1367871080.730573000,eth:ip:tcp:http,192.168.1.79,31.13.72.33,900,6,54819,80,,,http://www.facebook.com/,";
 		PcapSummary pcap = null;
 		try {
 			pcap = new PcapSummary(pcapStr);
 		} catch (Exception e) {
 			Assert.fail("Failed to parse pcap str: " + e);
 		}
 		Session session = sessionFactory.createSession(pcap);
 		//Persist session
 		Long id = template.save(session).getSessionId();
 		HttpSession ret = template.findOne(id, HttpSession.class);
 		//Check relationships
 		Iterable<WebSite> sites = devRepo.getAllWebSitesVisitedByDevice("dans-laptop");
 		assertNotNull(sites);
 		List<String> webSites = new ArrayList<String>();
 		for (WebSite site : sites) {
 			webSites.add(site.getAddress());
 		}
 		assertThat(webSites.size(), is(1));
 		assertThat(webSites, hasItems("www.facebook.com"));
 	}
 	
 	@Test
 	@Transactional
 	public void parseUdpSession() {
 		String pcapStr = "1367871079.363044000,eth:ip:udp:db-lsp-disc,192.168.1.82,192.168.1.255,140,17,,17500,17500,,,";
 		PcapSummary pcap = null;
 		try {
 			pcap = new PcapSummary(pcapStr);
 		} catch (Exception e) {
 			Assert.fail("Failed to parse pcap str: " + e);
 		}
 		Session session = sessionFactory.createSession(pcap);
 		//Persist session
 		Long id = template.save(session).getSessionId();
 		//Check is a ip session
 		assertEquals(IpSession.class, session.getClass());
 		IpSession ret = template.findOne(id, IpSession.class);
 		assertThat(ret.getDestPort().getPort(), is(17500));
 		assertThat(ret.getSrcPort().getPort(), is(17500));
 		assertThat(ret.getDtoi(), is(1367871079L));
 		assertThat(ret.getIpDest().getIpAddr(), is("192.168.1.255"));
 		assertThat(ret.getIpSrc().getIpAddr(), is("192.168.1.82"));
 		assertThat(ret.getLength(), is(140));
 		assertThat(ret.getProtocolNumber(), is(17));
 		List<String> protos = new ArrayList<String>();
 		for (Protocol proto : template.fetch(ret.getProtocols())) {
 			protos.add(proto.getName());
 		}
 		assertThat(protos, hasItems("eth","ip","udp","db-lsp-disc"));
 	}
 
 	@Test
 	@Transactional
 	public void parseIp6Session() {
 		String pcapStr = "1367871237.870682000,eth:ipv6:udp:http,,,,,51670,1900,,,";
 
 		PcapSummary pcap = null;
 		try {
 			pcap = new PcapSummary(pcapStr);
 		} catch (Exception e) {
 			Assert.fail("Failed to parse pcap str: " + e);
 		}
 		Session session = sessionFactory.createSession(pcap);
 		//Persist session
 		Long id = template.save(session).getSessionId();
 		assertEquals(IpSession.class, session.getClass());
 		IpSession ret = template.findOne(id, IpSession.class);
 		assertThat(ret.getDestPort().getPort(), is(1900));
 		assertThat(ret.getSrcPort().getPort(), is(51670));
 		assertThat(ret.getDtoi(), is(1367871237L));
 		List<String> protos = new ArrayList<String>();
 		for (Protocol proto : template.fetch(ret.getProtocols())) {
 			protos.add(proto.getName());
 		}
 		assertThat(protos, hasItems("eth","ipv6","udp","http"));
 	}
 
 	@Test
 	@Transactional
 	public void parseMulticastSession() {
 		String pcapStr = "1367871239.404734000,eth:ip:igmp,192.168.1.72,224.0.0.22,40,2,,,,,";
 		
 		PcapSummary pcap = null;
 		try {
 			pcap = new PcapSummary(pcapStr);
 		} catch (Exception e) {
 			Assert.fail("Failed to parse pcap str: " + e);
 		}
 		Session session = sessionFactory.createSession(pcap);
 		//Persist session
 		Long id = template.save(session).getSessionId();
 		assertEquals(session.getClass(), IpSession.class);
 		IpSession ret = template.findOne(id, IpSession.class);
 		assertThat(ret.getIpSrc().getIpAddr(), is("192.168.1.72"));
 		assertThat(ret.getIpDest().getIpAddr(), is("224.0.0.22"));
 		assertThat(ret.getLength(), is(40));
 		assertThat(ret.getDtoi(), is(1367871239L));
 		List<String> protos = new ArrayList<String>();
 		for (Protocol proto : template.fetch(ret.getProtocols())) {
 			protos.add(proto.getName());
 		}
 		assertThat(protos, hasItems("eth","ip","igmp"));
 	}
 	
 	@Test
 	@Transactional
 	public void parseVeryShortSession() {
 		String pcapStr = "1367871304.019532000,eth:ipv6:icmpv6,,,,,,,,";
 		
 		PcapSummary pcap = null;
 		try {
 			pcap = new PcapSummary(pcapStr);
 		} catch (Exception e) {
 			Assert.fail("Failed to parse pcap str: " + e);
 		}
 		Session session = sessionFactory.createSession(pcap);
 		//Persist session
 		Long id = template.save(session).getSessionId();
 		assertEquals(session.getClass(), IpSession.class);
 		IpSession ret = template.findOne(id, IpSession.class);
 		assertThat(ret.getDtoi(), is(1367871304L));
 		List<String> protos = new ArrayList<String>();
 		for (Protocol proto : template.fetch(ret.getProtocols())) {
 			protos.add(proto.getName());
 		}
 		assertThat(protos, hasItems("eth","ipv6","icmpv6"));
 	}
 	
 }
