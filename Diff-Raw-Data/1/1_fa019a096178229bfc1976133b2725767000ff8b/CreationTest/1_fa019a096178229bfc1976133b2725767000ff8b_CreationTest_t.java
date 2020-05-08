 /**
  *
  * Copyright (c) 2012, PetalsLink
  * 
  * This library is free software; you can redistribute it and/or
  * modify it under the terms of the GNU Lesser General Public
  * License as published by the Free Software Foundation; either
  * version 2.1 of the License, or (at your option) any later version.
  *
  * This library is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
  * Lesser General Public License for more details.
  *
  * You should have received a copy of the GNU Lesser General Public
  * License along with this library; if not, write to the Free Software
  * Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA 
  *
  */
 package eu.playproject.platform.service.bootstrap;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 
 import javax.xml.namespace.QName;
 import javax.xml.ws.wsaddressing.W3CEndpointReference;
 
 import junit.framework.TestCase;
 
 import org.junit.Assert;
 import org.junit.Test;
 
 import eu.playproject.governance.api.GovernanceExeption;
 import eu.playproject.governance.api.bean.Metadata;
 import eu.playproject.governance.api.bean.Topic;
 import eu.playproject.platform.service.bootstrap.api.GovernanceClient;
 import eu.playproject.platform.service.bootstrap.api.Subscription;
 
 public class CreationTest extends TestCase {
 
 	@Test
 	public void testCreate() throws Exception {
 
 		Topic t1 = new Topic();
 		t1.setName("T1");
 		t1.setNs("http://foo");
 		t1.setPrefix("pre");
 
 		Topic t2 = new Topic();
 		t2.setName("T2");
 		t2.setNs("http://foo");
 		t2.setPrefix("pre");
 
 		final List<Topic> topics = new ArrayList<Topic>();
 		topics.add(t1);
 		topics.add(t2);
 
 		final List<Topic> filtered = new ArrayList<Topic>();
 		filtered.add(t1);
 
 		String ecEndpoint = "http://localhost:4568/EventCloudService";
 		String subscriberEndpoint = "http://localhost:4569/SubscriberService";
 
 		DSBSubscribesToECBootstrapServiceImpl service = new DSBSubscribesToECBootstrapServiceImpl();
 		service.setEventCloudClientFactory(new EventCloudClientFactoryMock(
 				ecEndpoint));
 		service.setTopicManager(new TopicManagerMock());
		service.setSubscriptionRegistry(new SubscriptionRegistryServiceImpl());
 		service.setGovernanceClient(new GovernanceClient() {
 
 			@Override
 			public void loadResources(InputStream arg0)
 					throws GovernanceExeption {
 			}
 
 			@Override
 			public List<Topic> getTopics() {
 				return topics;
 			}
 
 			@Override
 			public List<QName> findTopicsByElement(QName arg0)
 					throws GovernanceExeption {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			@Override
 			public List<W3CEndpointReference> findEventProducersByTopics(
 					List<QName> arg0) throws GovernanceExeption {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			@Override
 			public List<W3CEndpointReference> findEventProducersByElements(
 					List<QName> arg0) throws GovernanceExeption {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			@Override
 			public void createTopic(Topic arg0) throws GovernanceExeption {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public void removeMetadata(Topic arg0, Metadata arg1)
 					throws GovernanceExeption {
 				// TODO Auto-generated method stub
 
 			}
 
 			@Override
 			public List<Topic> getTopicsWithMeta(List<Metadata> arg0)
 					throws GovernanceExeption {
 				System.out.println("Get topics with meta " + arg0);
 				return filtered;
 			}
 
 			@Override
 			public Metadata getMetadataValue(Topic arg0, String arg1)
 					throws GovernanceExeption {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			@Override
 			public List<Metadata> getMetaData(Topic arg0)
 					throws GovernanceExeption {
 				// TODO Auto-generated method stub
 				return null;
 			}
 
 			@Override
 			public boolean deleteMetaData(Topic arg0) throws GovernanceExeption {
 				// TODO Auto-generated method stub
 				return false;
 			}
 
 			@Override
 			public void addMetadata(Topic arg0, Metadata arg1)
 					throws GovernanceExeption {
 				// TODO Auto-generated method stub
 
 			}
 		});
 
 		List<Subscription> result = service.bootstrap(ecEndpoint,
 				subscriberEndpoint);
 
 		System.out.println(result);
 		Assert.assertTrue(result.size() == 1);
 	}
 
 }
