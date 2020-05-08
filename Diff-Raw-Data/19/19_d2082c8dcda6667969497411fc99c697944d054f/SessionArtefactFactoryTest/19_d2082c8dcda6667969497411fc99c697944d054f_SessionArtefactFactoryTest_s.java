 package com.dsa.pcapneo.domain.session;
 
import static org.hamcrest.CoreMatchers.is;
 import static org.hamcrest.MatcherAssert.assertThat;
 
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
 import com.dsa.pcapneo.domain.graph.IpAddress;
 import com.dsa.pcapneo.domain.graph.Protocol;
 import com.dsa.pcapneo.domain.graph.User;
 import com.dsa.pcapneo.graph.repositories.SessionArtefactFactory;
 
 @RunWith(SpringJUnit4ClassRunner.class)
 @ContextConfiguration(("/testContext.xml"))
 
 public class SessionArtefactFactoryTest {
 	@Autowired Neo4jTemplate template;
 	@Autowired SessionArtefactFactory factory;
 
 	@Test
 	@Transactional
 	public void getIpAddress() {
 		IpAddress ip = factory.getIpAddress("192.168.1.1");
 		assertThat(ip.getIpAddr(), is ("192.168.1.1"));
 		IpAddress ret = template.findOne(ip.getId(), IpAddress.class);
 		Assert.assertNotNull(ret);
 		assertThat(ret.getIpAddr(), is("192.168.1.1"));
 	}
 
 	@Test
 	@Transactional
 	public void getIpSavedIpAddress() {
 		IpAddress ip = new IpAddress("192.168.1.1");
 		Long id = template.save(ip).getId();
 		ip = factory.getIpAddress("192.168.1.1");
 		assertThat(ip.getIpAddr(), is ("192.168.1.1"));
 		assertThat(ip.getId(), is(id));
 	}
 
 	@Test
 	@Transactional
 	public void getDevice() {
 		String ip1 = "127.0.0.1";
 		template.save(new IpAddress(ip1));
 
 		Device retrieved = factory.getDeviceFromIpAddr(factory.getIpAddress(ip1));
 		Assert.assertNotNull(retrieved);
		assertThat(retrieved.getHostName(), is("localhost.localdomain"));
 	}
 
 }
