 package org.linkstorage.repository;
 
 import java.util.Date;
 
 import org.linkstorage.model.Link;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.test.context.ContextConfiguration;
 import org.springframework.test.context.testng.AbstractTransactionalTestNGSpringContextTests;
 import org.springframework.transaction.annotation.Transactional;
 import org.testng.Assert;
 import org.testng.annotations.BeforeClass;
 import org.testng.annotations.Parameters;
 import org.testng.annotations.Test;
 
 @ContextConfiguration(locations = {"classpath:/META-INF/spring/integration-data.xml"})
 public class LinkBaseTest extends AbstractTransactionalTestNGSpringContextTests{
 
 	@Autowired
 	private LinkBase linkRepository;
 
 	private Link link;
 
 	/**
 	 * Preparing test data which will be initialized only once for @Test methods
 	 * of this class
 	 */
 	@BeforeClass
 	@Parameters({"url", "metaDescription", "createdAt", "updatedAt"})
 	public void init(String url, String metaDescription) {
 		Link link = new Link();
 		link.setUrl(url);
 		link.setMetaDescription(metaDescription);
 
 		//TODO convert String to Date object with Spring
 		link.setCreatedAt(new Date(1258466400));
 		link.setUpdatedAt(new Date(1258466400));
 
 		this.link = link;
 	}
 
 	/**
 	 * Transactional operation which will be rolled back by Spring after
 	 * the test method runs
 	 */
 	@Test
 	@Transactional
 	public void addLink() {
 		int actualLinks = linkRepository.getLinks().size();
 		linkRepository.addLink(link);
 		int incrementedLinks = linkRepository.getLinks().size();
 
 		Assert.assertNotNull(link.getId(),
 				"If link is persisted it must have been assigned an id");
 
 		Assert.assertEquals(incrementedLinks, actualLinks+1,
 				"After adding 1 link, total users' count must increment by 1");
 	}
 }
