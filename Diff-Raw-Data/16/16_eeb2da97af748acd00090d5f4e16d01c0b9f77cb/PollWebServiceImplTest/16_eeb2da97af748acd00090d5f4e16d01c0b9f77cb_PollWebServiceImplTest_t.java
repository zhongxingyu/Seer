 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package de.uniluebeck.itm.ep5.poll.service;
 
 import de.uniluebeck.itm.ep5.poll.domain.XODateOption;
 import de.uniluebeck.itm.ep5.poll.domain.XOOptionList;
 import de.uniluebeck.itm.ep5.poll.domain.XOTextOption;
 import de.uniluebeck.itm.ep5.poll.domain.xoPoll;
 import de.uniluebeck.itm.pollservice.XsOption;
 import de.uniluebeck.itm.pollservice.XsOptionList;
 import de.uniluebeck.itm.pollservice.XsPoll;
 import de.uniluebeck.itm.pollservice.XsPollInfo;
 import de.uniluebeck.itm.pollservice.XsVote;
 import java.util.GregorianCalendar;
 import java.util.List;
 import java.util.Locale;
 import org.junit.*;
 import static org.junit.Assert.*;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 /**
  *
  * @author hoschi
  */
 public class PollWebServiceImplTest {
 // <editor-fold defaultstate="collapsed" desc="generated stuff">
 
 	public PollWebServiceImplTest() {
 	}
 
 	@BeforeClass
 	public static void setUpClass() throws Exception {
 	}
 
 	@AfterClass
 	public static void tearDownClass() throws Exception {
 	}
 
 	@After
 	public void tearDown() {
 	}// </editor-fold>
 	final static Logger logger =
 			LoggerFactory.getLogger(PollWebServiceImplTest.class);
 	ApplicationContext ctx;
 	PollService pollService;
 
 	@Before
 	public void setUp() {
 		// Create the spring container using the XML configuration in
 		// application-context.xml
 		ctx = new ClassPathXmlApplicationContext(
 				"application-context.xml");
 
 		// Retrieve the beans we'll use during testing
 		pollService = ctx.getBean(PollService.class);
 	}
 
 	/**
 	 * Test of getPolls method, of class PollWebServiceImpl.
 	 */
 	@Test
 	@Ignore
 	public void testGetPolls() {
 		logger.info("getPolls");
 		PollWebServiceImpl instance = new PollWebServiceImpl();
 		List<XsPollInfo> result;
 		XsPollInfo info;
 		xoPoll poll;
 
 		logger.info("- insert polls into DB");
 		// create strings
 		XOTextOption text = new XOTextOption();
 		text.addString("hello", Locale.ENGLISH.toString());
 		text.addString("hallo", Locale.GERMAN.toString());
 
 		XOTextOption text1 = new XOTextOption();
 		text1.addString("in the morning", Locale.ENGLISH.toString());
 		text1.addString("morgens", Locale.GERMAN.toString());
 
 		// save string options
 		XOOptionList olist = new XOOptionList();
 		olist.setTitle("strings");
 		olist.addOption(text);
 		olist.addOption(text1);
 
 		poll = new xoPoll("poll1");
 		poll.addOptionList(olist);
 		pollService.addPoll(poll);
 
 		poll = new xoPoll("poll2");
 		pollService.addPoll(poll);
 		poll = new xoPoll("poll3");
 		pollService.addPoll(poll);
 
 
 		logger.info("- get poll infos");
 		result = instance.getPolls(Locale.ENGLISH.toString());
 		assertEquals(3, result.size());
 		assertEquals("poll1", result.get(0).getTitle());
 		assertEquals("poll2", result.get(1).getTitle());
 		assertEquals("poll3", result.get(2).getTitle());
 	}
 
 	/**
 	 * Test of getPoll method, of class PollWebServiceImpl.
 	 */
 	@Test
 	@Ignore
 	public void testGetPoll() throws Exception {
 		logger.info("getPoll");
 		PollWebServiceImpl instance = new PollWebServiceImpl();
 		XsPoll result;
 		XsOptionList optionList;
 
 		logger.info("- insert a multi lingual poll into DB");
 		// create strings
 		XOTextOption text = new XOTextOption();
 		text.addString("hello", Locale.ENGLISH.toString());
 		text.addString("hallo", Locale.GERMAN.toString());
 		text.addVote("hoschi");
 
 		GregorianCalendar yesterday = new GregorianCalendar();
 		yesterday.add(GregorianCalendar.DAY_OF_MONTH, -1);
 		XODateOption date = new XODateOption(yesterday.getTime());
 
 		// save string options
 		XOOptionList olist = new XOOptionList();
 		olist.setTitle("stuff");
 		olist.addOption(text);
 		olist.addOption(date);
 
 		xoPoll poll = new xoPoll("poll");
 		poll.addOptionList(olist);
 
 		// save changes and generate ID
 		pollService.addPoll(poll);
 
 		logger.info("- get the poll - english locale");
 		result = instance.getPoll(poll.getId().toString(),
 				Locale.ENGLISH.toString());
 		assertEquals(poll.getTitle(), result.getTitle());
 		assertEquals(1, result.getOptionList());
 		optionList = result.getOptionList().get(0);
 		assertEquals(olist.getTitle(), optionList.getTitle());
 		assertEquals(2, optionList.getOption());
 		for (XsOption option : optionList.getOption()) {
 			if (option.getDateTime() == null) {
 				// text option
 				assertEquals("hello", option.getValue());
 				assertEquals(1, option.getVotes().getVoter().size());
 				assertEquals("hoschi", option.getVotes().getVoter().get(0));
 			} else if (option.getValue() == null) {
 				// date option
 				assertEquals(date.getDate(),
 						XODateOption.parseString(option.getDateTime()));
 				assertEquals(0, option.getVotes().getVoter().size());
 			}
 		}
 
 		logger.info("- get the poll - german locale");
 		result = instance.getPoll(poll.getId().toString(),
 				Locale.GERMAN.toString());
 		assertEquals(poll.getTitle(), result.getTitle());
 		assertEquals(1, result.getOptionList());
 		optionList = result.getOptionList().get(0);
 		assertEquals(olist.getTitle(), optionList.getTitle());
 		assertEquals(2, optionList.getOption());
 		for (XsOption option : optionList.getOption()) {
 			if (option.getDateTime() == null) {
 				// text option
 				assertEquals("hallo", option.getValue());
 				assertEquals(1, option.getVotes().getVoter().size());
 				assertEquals("hoschi", option.getVotes().getVoter().get(0));
 			} else if (option.getValue() == null) {
 				// date option
 				assertEquals(date.getDate(),
 						XODateOption.parseString(option.getDateTime()));
 				assertEquals(0, option.getVotes().getVoter().size());
 			}
 		}
 	}
 
 	/**
 	 * Test of voteForOptions method, of class PollWebServiceImpl.
 	 */
 	@Test
	//@Ignore
 	public void testVoteForOptions() {
 		logger.info("voteForOptions");
 		XsVote voteForOptions = null;
 		PollWebServiceImpl instance = new PollWebServiceImpl();
 
 		logger.info(" - create poll with two options to vote for");
 		// create strings
 		XOTextOption text = new XOTextOption();
 		text.addString("hello", Locale.ENGLISH.toString());
 		text.addString("hallo", Locale.GERMAN.toString());
 		text.addVote("hoschi");
 
 		GregorianCalendar yesterday = new GregorianCalendar();
 		yesterday.add(GregorianCalendar.DAY_OF_MONTH, -1);
 		XODateOption date = new XODateOption(yesterday.getTime());
 
 		// save string options
 		XOOptionList olist = new XOOptionList();
 		olist.setTitle("stuff");
 		olist.addOption(text);
 		olist.addOption(date);
 
 		xoPoll poll = new xoPoll("poll");
 		poll.addOptionList(olist);
 
 		// save changes and generate ID
 		pollService.addPoll(poll);
		olist = poll.getOptionLists().get(0);
 		assertNotNull(olist.getId());
 
 		logger.info(" - vote for one");
 
 		logger.info(" - vote for both, with same voter");
 		logger.info(" - vote for both, with another voter");
 		logger.info(" - vote for no one");
 
 		instance.voteForOptions(voteForOptions);
 	}
 }
