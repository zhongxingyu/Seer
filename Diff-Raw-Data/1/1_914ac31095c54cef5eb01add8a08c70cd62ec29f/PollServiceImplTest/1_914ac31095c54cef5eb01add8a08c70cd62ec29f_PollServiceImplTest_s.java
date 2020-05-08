 package de.uniluebeck.itm.ep5.poll.service;
 
 //import static org.hamcrest.CoreMatchers.equalTo;
 //import static org.junit.Assert.assertThat;
 import java.util.GregorianCalendar;
 import java.util.List;
 
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.Test;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.context.ApplicationContext;
 import org.springframework.context.support.ClassPathXmlApplicationContext;
 
 import de.uniluebeck.itm.ep5.poll.domain.IOption;
 import de.uniluebeck.itm.ep5.poll.domain.XODateOption;
 import de.uniluebeck.itm.ep5.poll.domain.XOLocalizedString;
 import de.uniluebeck.itm.ep5.poll.domain.XOOptionList;
 import de.uniluebeck.itm.ep5.poll.domain.XOTextOption;
 import de.uniluebeck.itm.ep5.poll.domain.xoPoll;
 import de.uniluebeck.itm.ep5.poll.service.PollService;
 import de.uniluebeck.itm.ep5.util.InactiveExcepiton;
 import java.util.Locale;
 
 public class PollServiceImplTest {
 
     final static Logger logger = LoggerFactory.getLogger(PollServiceImplTest.class);
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
 
     /*
      * nutzer kann abstimmung anlegen
      */
     @Test
     public void addPoll() {
         xoPoll poll = new xoPoll("createpoll");
         pollService.addPoll(poll);
 
         List<xoPoll> list = pollService.getPolls();
         Assert.assertEquals(1, list.size());
         Assert.assertEquals("createpoll", list.get(0).getTitle());
 /*
         // Print all polls and options
         for (xoPoll p : pollService.getPolls()) {
             logger.info(p.toString());
         }*/
     }
 
     /*
      * beim anlegen einer abstimmung wird eine eindeutige ID angeleget
      */
     @Test
     public void createPollId() {
         xoPoll poll = new xoPoll("identity");
         pollService.addPoll(poll);
 
         poll = new xoPoll("identity1");
         pollService.addPoll(poll);
         List<xoPoll> list = pollService.getPolls();
         Assert.assertEquals(2, list.size());
 
         int id = list.get(0).getId();
         int id1 = list.get(1).getId();
         Assert.assertFalse(id == id1);
 
         // Print all polls and options
         for (xoPoll p : pollService.getPolls()) {
             logger.info(p.toString());
         }
     }
 
     /*
      * nutzer kann poll ändern
      * nutzer kann bestimmen ob die abstimmung public
      */
     @Test
     public void changePoll() {
         // add poll
         xoPoll poll = new xoPoll("changepoll", false);
         pollService.addPoll(poll);
         Assert.assertEquals("changepoll", poll.getTitle());
         Assert.assertEquals(false, poll.isPublic());
         Assert.assertEquals(true, poll.isActive());
 
         // change it
         poll.setTitle("blubb");
         poll.setPublic(true);
 
         GregorianCalendar yesterday = new GregorianCalendar();
         yesterday.add(GregorianCalendar.DAY_OF_MONTH, -1);
         
 
         GregorianCalendar inTwoDays = new GregorianCalendar();
         inTwoDays.add(GregorianCalendar.DAY_OF_MONTH, 2);
         poll.setActiveTimeSpan(yesterday.getTime(), inTwoDays.getTime());
         // save changes
         pollService.updatePoll(poll);
 
         List<xoPoll> list = pollService.getPolls();
         Assert.assertEquals(1, list.size());
         poll = list.get(0);
         Assert.assertEquals("blubb", poll.getTitle());
         Assert.assertEquals(true, poll.isPublic());
         Assert.assertEquals(true, poll.isActive());
     }
 
     /*
      * nutzer kann angbeben wie lange die abstimmung aktiv ist
      * interaktive abstimmungen können eingesehen werden
      */
     @Test
     public void setActiveDatePoll() {
         // add poll
         xoPoll poll = new xoPoll("setActiveDatePoll");
         Assert.assertEquals(true, poll.isActive());
 
         GregorianCalendar tomorrow = new GregorianCalendar();
         tomorrow.add(GregorianCalendar.DAY_OF_MONTH, 1);
 
         GregorianCalendar inTwoDays = new GregorianCalendar();
         inTwoDays.add(GregorianCalendar.DAY_OF_MONTH, 2);
         poll.setActiveTimeSpan(tomorrow.getTime(), inTwoDays.getTime());
         // save changes
         pollService.addPoll(poll);
 
         // interactive poll must be in list with other polls
         List<xoPoll> list = pollService.getPolls();
         Assert.assertEquals(1, list.size());
         poll = list.get(0);
         Assert.assertEquals(false, poll.isActive());
 
         // test other date cases
         GregorianCalendar yesterday = new GregorianCalendar();
         yesterday.add(GregorianCalendar.DAY_OF_MONTH, -1);
         poll.setActiveTimeSpan(yesterday.getTime(), inTwoDays.getTime());
         Assert.assertEquals(true, poll.isActive());
 
         GregorianCalendar beforeTwoDays = new GregorianCalendar();
         beforeTwoDays.add(GregorianCalendar.DAY_OF_MONTH, -2);
         poll.setActiveTimeSpan(beforeTwoDays.getTime(), yesterday.getTime());
         Assert.assertEquals(false, poll.isActive());
     }
 
     /*
      * abstimmung kann beliebigviele optionlisten enthalten
      */
     @Test
     public void setOptionList() {
         XOTextOption text = new XOTextOption();
         XODateOption date = new XODateOption();
 
         XOOptionList olist = new XOOptionList();
         olist.addOption(date);
         olist.addOption(text);
 
         xoPoll poll = new xoPoll("poll");
         poll.addOptionList(olist);
 
         // save changes
         pollService.addPoll(poll);
         List<xoPoll> list = pollService.getPolls();
         Assert.assertEquals(1, list.size());
 
         poll = list.get(0);
         List<XOOptionList> listOfOptionLists = poll.getOptionLists();
         Assert.assertEquals(1, list.size());
 
         olist = listOfOptionLists.get(0);
         List<IOption> options = olist.getOptions();
         Assert.assertEquals(2, options.size());
     }
 
 	/*
 	 * abstimmung kann beliebigviele optionen enthalten
 	 * nutzer kann datums und frei text option anlegen
 	 */
     @Test
     public void setDateOptions() {
         XODateOption date = null;
 		XOOptionList olist = new XOOptionList();
 
 		GregorianCalendar yesterday = new GregorianCalendar();
         yesterday.add(GregorianCalendar.DAY_OF_MONTH, -1);
         GregorianCalendar beforeTwoDays = new GregorianCalendar();
         beforeTwoDays.add(GregorianCalendar.DAY_OF_MONTH, -2);
 		GregorianCalendar tomorrow = new GregorianCalendar();
         tomorrow.add(GregorianCalendar.DAY_OF_MONTH, 1);
         GregorianCalendar inTwoDays = new GregorianCalendar();
         inTwoDays.add(GregorianCalendar.DAY_OF_MONTH, 2);
 
 		date = new XODateOption(yesterday.getTime());
         olist.addOption(date);
 		date = new XODateOption(beforeTwoDays.getTime());
         olist.addOption(date);
 		date = new XODateOption(tomorrow.getTime());
         olist.addOption(date);
 		date = new XODateOption(inTwoDays.getTime());
         olist.addOption(date);
 
         xoPoll poll = new xoPoll("poll");
         poll.addOptionList(olist);
 
         // save changes
         pollService.addPoll(poll);
         List<xoPoll> list = pollService.getPolls();
         Assert.assertEquals(1, list.size());
 
         poll = list.get(0);
         List<XOOptionList> listOfOptionLists = poll.getOptionLists();
         Assert.assertEquals(1, list.size());
 
         olist = listOfOptionLists.get(0);
         List<IOption> options = olist.getOptions();
         Assert.assertEquals(4, options.size());
 
 		for (IOption io : options) {
 			Assert.assertTrue(io instanceof XODateOption);
 		}
         
     }
 
 	/*
 	 * abstimmung kann beliebigviele optionen enthalten
 	 * nutzer kann datums und frei text option anlegen
 	 * optionen können in verschiedenen sprachen eingeben un angezeigt werden
 	 */
     @Test
     public void setTextOptions() {
         XOTextOption text = new XOTextOption();
         text.addString("hello", Locale.ENGLISH.toString());
         text.addString("hallo", Locale.GERMAN.toString());
 
         XOOptionList olist = new XOOptionList();
         olist.addOption(text);
 
         xoPoll poll = new xoPoll("poll");
         poll.addOptionList(olist);
 
         // save changes
         pollService.addPoll(poll);
         List<xoPoll> list = pollService.getPolls();
         Assert.assertEquals(1, list.size());
 
         poll = list.get(0);
         List<XOOptionList> listOfOptionLists = poll.getOptionLists();
         Assert.assertEquals(1, list.size());
 
         olist = listOfOptionLists.get(0);
         List<IOption> options = olist.getOptions();
         Assert.assertEquals(1, options.size());
 
         Assert.assertTrue(options.get(0) instanceof XOTextOption);
         XOTextOption t = (XOTextOption) options.get(0);
         Assert.assertEquals(2, t.getStrings().size());
     }
 
     /**
      * Nutzer kann abstimmen in dem er seinen namen angibt und seine gewaehlten optionen
      */
     @Test
     public void vote() {
     	String person1 = "person1";	// votes for option1a
     	String person2 = "person2"; // votes for option1b
     	String person3 = "person3"; // votes for option1a
     	
     	xoPoll poll = new xoPoll("somepoll");
     	
     	XOOptionList optionList1 = new XOOptionList();
     	XOTextOption option1a = new XOTextOption();
     	option1a.addString("option1a", Locale.ENGLISH.toString());
     	option1a.addVote(person1);
     	option1a.addVote(person3);
     	optionList1.addOption(option1a);
     	
     	XOTextOption option1b = new XOTextOption();
     	option1b.addString("option1b", Locale.ENGLISH.toString());
     	option1b.addVote(person2);
     	optionList1.addOption(option1b);
     	
     	poll.addOptionList(optionList1);
     	
     	pollService.addPoll(poll);
     	
     	List<xoPoll> polls = pollService.search("somepoll");
     	poll = polls.get(0);
     	optionList1 = poll.getOptionLists().get(0);
     	option1a = (XOTextOption)findOption(optionList1.getOptions(), "option1a", Locale.ENGLISH.toString());
     	Assert.assertTrue(option1a.getVotes().contains(person1));
     	Assert.assertTrue(option1a.getVotes().contains(person3));
     	
     	option1b = (XOTextOption)findOption(optionList1.getOptions(), "option1b", Locale.ENGLISH.toString());
     	Assert.assertTrue(option1b.getVotes().contains(person2));
     	
     }
     
     private IOption findOption(List<IOption> options, String text, String locale) {
     	for (IOption option : options) {
     		List<XOLocalizedString> strings = null;
     		if (option instanceof XOTextOption) {
     			strings = ((XOTextOption)option).getStrings();
     		}
     		if (strings != null) {
     			for (XOLocalizedString string : strings) {
     				if (string.getLocale().equals(locale) && string.getText().equals(text)) {
     					return option;
     				}
     			}
     		}
     	}
     	return null;
     }
     
     /*
      * interaktive abstimmungen können nicht mehr verändert werden
      */
     @Test(expected=InactiveExcepiton.class)
     public void dontUpdateInactivePolls() {
         // add poll
         xoPoll poll = new xoPoll("changepoll", false);
         pollService.addPoll(poll);
         List<xoPoll> list = pollService.getPolls();
         Assert.assertEquals(1, list.size());
         poll = list.get(0);
 
         // set inactive
         GregorianCalendar yesterday = new GregorianCalendar();
         yesterday.add(GregorianCalendar.DAY_OF_MONTH, -1);
         GregorianCalendar beforeTwoDays = new GregorianCalendar();
         beforeTwoDays.add(GregorianCalendar.DAY_OF_MONTH, -2);
         poll.setActiveTimeSpan(beforeTwoDays.getTime(), yesterday.getTime());
 
         // if poll is not active -> don't allow an update
         Assert.assertEquals(false, poll.isActive());
         Assert.assertEquals("changepoll", poll.getTitle());
         Assert.assertEquals(false, poll.isPublic());
 
         // change it
         poll.setTitle("blubb");
         poll.setPublic(true);
         pollService.updatePoll(poll);
     }
 
     /*
      * nutzer kann abstimmungen nach titel suchen mit wildcards
      */
     @Test
     public void searchTest() {
         xoPoll poll = new xoPoll("poll");
         pollService.addPoll(poll);
         poll = new xoPoll("poll1");
         pollService.addPoll(poll);
         poll = new xoPoll("poll2");
         pollService.addPoll(poll);
 
         // find our poll
         List<xoPoll> list = this.pollService.search("poll");
         Assert.assertEquals(1, list.size());
         poll = list.get(0);
         Assert.assertEquals("poll", poll.getTitle());
 
         list = this.pollService.search("poll*");
         Assert.assertEquals(3, list.size());
         poll = list.get(0);
         Assert.assertEquals("poll", poll.getTitle());
         poll = list.get(1);
         Assert.assertEquals("poll1", poll.getTitle());
         poll = list.get(2);
         Assert.assertEquals("poll2", poll.getTitle());
 
         list = this.pollService.search("foo");
         Assert.assertNotNull(list);
         Assert.assertEquals(0, list.size());
     }
 
 	/*
      * das wichtigste auf einen blick für sönke und florian
      */
     @Test
     public void milestoneOneHighligtTest() {
 		// create strings
 		XOTextOption text = new XOTextOption();
         text.addString("hello", Locale.ENGLISH.toString());
         text.addString("hallo", Locale.GERMAN.toString());
 
 		XOTextOption text1 = new XOTextOption();
         text1.addString("in the morning", Locale.ENGLISH.toString());
         text1.addString("morgens", Locale.GERMAN.toString());
 
 		// add vote
 		text.addVote("hoschi");
 		text1.addVote("jacob");
 
 		// save string options
         XOOptionList olist = new XOOptionList();
 		olist.setTitle("strings");
         olist.addOption(text);
 		olist.addOption(text1);
 
         xoPoll poll = new xoPoll("poll");
         poll.addOptionList(olist);
 
 
 		// create dates
 		XODateOption date = null;
 		olist = new XOOptionList();
 		olist.setTitle("dates");
 
 		GregorianCalendar yesterday = new GregorianCalendar();
         yesterday.add(GregorianCalendar.DAY_OF_MONTH, -1);
         GregorianCalendar beforeTwoDays = new GregorianCalendar();
         beforeTwoDays.add(GregorianCalendar.DAY_OF_MONTH, -2);
 		GregorianCalendar tomorrow = new GregorianCalendar();
         tomorrow.add(GregorianCalendar.DAY_OF_MONTH, 1);
         GregorianCalendar inTwoDays = new GregorianCalendar();
         inTwoDays.add(GregorianCalendar.DAY_OF_MONTH, 2);
 
 		date = new XODateOption(yesterday.getTime());
         olist.addOption(date);
 		date = new XODateOption(beforeTwoDays.getTime());
         olist.addOption(date);
 		date = new XODateOption(tomorrow.getTime());
         olist.addOption(date);
 		date = new XODateOption(inTwoDays.getTime());
         olist.addOption(date);
 
         poll.addOptionList(olist);
 
 		// set active date
 		poll.setActiveTimeSpan(yesterday.getTime(), tomorrow.getTime());
 
         // save changes
         pollService.addPoll(poll);
 
 	}
 }
