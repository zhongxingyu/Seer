 package de.uniluebeck.itm.ep5.poll;
 
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
 import de.uniluebeck.itm.ep5.poll.domain.XOOptionList;
 import de.uniluebeck.itm.ep5.poll.domain.XOTextOption;
 import de.uniluebeck.itm.ep5.poll.domain.xoPoll;
 import de.uniluebeck.itm.ep5.poll.service.PollService;
 import de.uniluebeck.itm.ep5.util.InactiveExcepiton;
 import org.junit.Ignore;
 
 public class PollServiceTest {
 
     final static Logger logger = LoggerFactory.getLogger(PollServiceTest.class);
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
      * TODO abstimmung kann beliebigviele optionlisten enthalten
      */
     @Test
	@Ignore
     public void setOptionList() {
         XOTextOption text = new XOTextOption("test");
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
     /////////////////////////////////////////////////////
     // TODO abstimmung kann beliebigviele optionen enthalten
     /////////////////////////////////////////////////////
     /////////////////////////////////////////////////////
     // TODO nutzer kann datums und frei text option anlegen
     /////////////////////////////////////////////////////
     /////////////////////////////////////////////////////
     // TODO optionen können in verschiedenen sprachen eingeben un angezeigt werden
     /////////////////////////////////////////////////////
     /////////////////////////////////////////////////////
     // TODO nutzer kann abstimmen in dem er seinen namen angbibt und seine gewählten optionen
     /////////////////////////////////////////////////////
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
 }
