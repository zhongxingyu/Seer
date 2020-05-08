 package mainframe.tests;
 
 import static org.junit.Assert.*;
 import mainframe.logic.IMainframe;
 import mainframe.logic.IMainframe.IdentificationError;
 import mainframe.logic.IMainframe.VoterDoesNotExist;
 import mainframe.logic.IMainframe.VoterStartedVote;
 import mainframe.logic.Mainframe;
 import org.junit.Before;
 import org.junit.Ignore;
 import org.junit.Test;
 import partiesList.factories.IPartiesListFactory;
 import partiesList.factories.IPartyFactory;
 import partiesList.factories.PartiesListFactory;
 import partiesList.factories.PartyFactory;
 import partiesList.model.IPartiesList;
 import partiesList.model.IPartiesList.PartyDoesNotExist;
 import partiesList.model.IParty;
 import votersList.factories.IVoterDataFactory;
 import votersList.factories.IVotersListFactory;
 import votersList.factories.VoterDataFactory;
 import votersList.factories.VotersListFactory;
 import votersList.model.IVoterData;
 import votersList.model.IVoterData.AlreadyIdentified;
 import votersList.model.IVoterData.Unidentified;
 import votersList.model.IVotersList;
 import votersList.model.IVotersList.VoterDoesntExist;
 
 /**
  * 
  * unit test for the mainframe
  * 
  * we must note that we have a regular way to see if the mainframe is aware of some action:
  * we do shutDown and check the backup XML file that the mainframe does of its
  * voters list, parties list and unregistered voters list - if the expected changes are 
  * not seen then it is a failure - this is the only way to check the mainframe
  */
 public class MainframeUnitTest {
 	
 	/**
 	 * time for hot backup test
 	 */
 	private final int backupTimeIntervalSeconds = 2;	
 	
 	IPartyFactory partyFactory = new PartyFactory();
 	IVoterDataFactory voterDataFactory = new VoterDataFactory();
 	IVotersListFactory votersListFactory = new VotersListFactory();
 	IPartiesListFactory partiesListFactory = new PartiesListFactory(partyFactory);
 	
 	//stub factories
 	
 	BackupStubFactory backupStubFactory = new BackupStubFactory() ;
 	MainframeWindowStubFactory mainframeWindowStubFactory = new MainframeWindowStubFactory();
 	StationsControllerStubFactory stationsControllerStubFactory = new StationsControllerStubFactory();
 	ReadSuppliedXMLStubFactory readSuppliedXMLStubFactory = new ReadSuppliedXMLStubFactory(); 
 	
 	IMainframe mainframe;
 	IVotersList readVotersList;
 	IPartiesList readPartiesList;
 	
 	
 	/**
 	 * invokes the method 'markVoted' of the stub 'stationsControllerStub' 'numOfVotes' times
 	 * @param numOfVotes the number of times we want the station controller to know that we voted
 	 */
 	private void markVotesOnStationController(int numOfVotes){
 		for(int i=0; i<numOfVotes; i++){
 			try {
 				this.stationsControllerStubFactory.getStationsController().markVoted(-1);
 			} catch (VoterDoesNotExist e) {
 				e.printStackTrace();
 			}
 		}
 	}
 	
 	
 	
 	@Before
 	public void preprocessing(){
 		//we create a mainframe
 		mainframe = new Mainframe(backupTimeIntervalSeconds, backupStubFactory
 				, mainframeWindowStubFactory
 				, readSuppliedXMLStubFactory
 				, stationsControllerStubFactory
 				, voterDataFactory
 				, votersListFactory);
 		
 		
 		//create a little voters list
 		readVotersList = votersListFactory.createInstance();
 		IVoterData v1 = voterDataFactory.createInstance(111);
 		IVoterData v2 = voterDataFactory.createInstance(222);
 		IVoterData v3 = voterDataFactory.createInstance(333);
 		IVoterData v4 = voterDataFactory.createInstance(444);
 		IVoterData v5 = voterDataFactory.createInstance(555);
 		readVotersList.addVoter(v1);
 		readVotersList.addVoter(v2);
 		readVotersList.addVoter(v3);
 		readVotersList.addVoter(v4);
 		readVotersList.addVoter(v5);
 		
 		//create a little parties list
 		readPartiesList = partiesListFactory.createInstance();
 		IParty p1 = partyFactory.createInstance("Vive la France", "alors on dance!");
 		IParty p2 = partyFactory.createInstance(" ", "");
 		IParty p3 = partyFactory.createInstance(" ", "fuck you");
 		IParty p4 = partyFactory.createInstance("Liberte", "oui");
 		IParty p5 = partyFactory.createInstance("egalite", "non");
 		IParty p6 = partyFactory.createInstance("fraternite", "");
 		IParty p7 = partyFactory.createInstance("technion", "Ullman's metal dick");
 		readPartiesList.addParty(p1);
 		readPartiesList.addParty(p2);
 		readPartiesList.addParty(p3);
 		readPartiesList.addParty(p4);
 		readPartiesList.addParty(p5);
 		readPartiesList.addParty(p6);
 		readPartiesList.addParty(p7);		
 		
 		//we want to put these two into the object 
 		//that is returned by readSuppliedXMLStubFactory
 		readSuppliedXMLStubFactory.getReadSuppliedXMLStub().setReadPartiesList(readPartiesList);
 		readSuppliedXMLStubFactory.getReadSuppliedXMLStub().setReadVotersList(readVotersList);
 
 	}
 
 	/**
 	 * sanity check, we did this test before 'preprocessing' existed
 	 */
 	@Test
 	public void testMainframeBuild() {
 		
 		
 		new Mainframe(backupTimeIntervalSeconds, backupStubFactory
 									, mainframeWindowStubFactory
 									, readSuppliedXMLStubFactory
 									, stationsControllerStubFactory
 									, voterDataFactory
 									, votersListFactory);
 	}
 
 	/**
 	 * tests if the initialize works, again this is a sanity check 
 	 */
 	@Test
 	public void testInitialize(){
 		mainframe.initialize();
 	}
 
 	/**
 	 * tests if the function Restore works for all the three:
 	 * the parties list, the voters list and the unregistered voters list
 	 * @throws AlreadyIdentified
 	 * @throws VoterDoesntExist
 	 * @throws Unidentified
 	 */
 	@Test
 	public void testRestore() throws AlreadyIdentified, VoterDoesntExist, Unidentified {
 		mainframe.initialize();
 		backupStubFactory.getCreatedBackupStub().setBackupedPartiesList(readPartiesList);
 		backupStubFactory.getCreatedBackupStub().setBackupedVotersList(readVotersList);
 		IVotersList temp = votersListFactory.createInstance();
 		temp.addVoter(voterDataFactory.createInstance(123));
 		temp.findVoter(123).markIdentified();
 		temp.findVoter(123).markStartedVote();
 		backupStubFactory.getCreatedBackupStub().setBackupedUnregisteredVotersList(temp);
 		mainframe.restore();
 		
 		mainframe.shutDown();
 		IPartiesList p = backupStubFactory.getCreatedBackupStub().restoreParties();
 		IVotersList v = backupStubFactory.getCreatedBackupStub().restoreVoters();
 		IVotersList u = backupStubFactory.getCreatedBackupStub().restoreUnregisteredVoters();
 		assertEquals(readPartiesList, p);
 		assertEquals(readVotersList,v);
 		assertEquals(temp,u);
 		
 	}
 
 	/**
 	 * we will check if the count votes gives the method 'showHistogram' of
 	 * StationsControllerStub the right parties list
 	 * @throws IdentificationError 
 	 * @throws PartyDoesNotExist 
 	 */
 	@Test
 	public void testCountVotes() throws IdentificationError, PartyDoesNotExist {
 		backupStubFactory.getCreatedBackupStub().setBackupedPartiesList(readPartiesList);
 		backupStubFactory.getCreatedBackupStub().setBackupedVotersList(readVotersList);
 		mainframe.initialize();
 		mainframe.identification(111);
 		mainframe.identification(333);
 		markVotesOnStationController(2);
 		mainframe.countVotes();
 		IPartiesList p = readPartiesList.copy();
 		p.getPartyBySymbol("oui").increaseVoteNumber();
 		p.getPartyBySymbol("oui").increaseVoteNumber();
 		assertEquals(mainframeWindowStubFactory.getCreatedMainframeWindowStub().getWhatShowHistogramGot(), p);
 	}
 
 	/**
 	 * tests if after the shutDown all the backups are as they suppose to be
 	 * i.e. the mainframe does the backup before he shuts down completely
 	 */
 	@Test
 	public void testShutDown() {
 		assertNotNull(readSuppliedXMLStubFactory.getReadSuppliedXMLStub().readPartiesList());
 		assertNotNull(readSuppliedXMLStubFactory.getReadSuppliedXMLStub().readVotersList());
 		mainframe.initialize();
 		mainframe.shutDown();
 		//no check if the backup is correct
 		IPartiesList p = backupStubFactory.getCreatedBackupStub().restoreParties();
 		IVotersList v = backupStubFactory.getCreatedBackupStub().restoreVoters();
 		IVotersList u = backupStubFactory.getCreatedBackupStub().restoreUnregisteredVoters();
 		assertEquals(votersListFactory.createInstance(), u);
 		//votersListFactory.createInstance().peep();
 		assertEquals(readVotersList, v);
 		assertEquals(readPartiesList, p);
 	}
 	
 
 	/**
 	 * tests if after the identification of voter 111 the mainframe is written to the backup file
 	 * @throws IdentificationError
 	 * @throws AlreadyIdentified
 	 * @throws VoterDoesntExist
 	 */
 	@Test
 	public void testIdentification() throws IdentificationError, AlreadyIdentified, VoterDoesntExist {
 		mainframe.initialize();
 		mainframe.identification(111);
 		mainframe.shutDown();
 		IPartiesList p = backupStubFactory.getCreatedBackupStub().restoreParties();
 		IVotersList v = backupStubFactory.getCreatedBackupStub().restoreVoters();
 		IVotersList u = backupStubFactory.getCreatedBackupStub().restoreUnregisteredVoters();
 		assertEquals(votersListFactory.createInstance(), u);
 		assertTrue(!readVotersList.equals( v));
 		assertEquals(readPartiesList, p);
 		readVotersList.findVoter(111).markIdentified();
 		assertEquals(readVotersList, v);
 	}
 	
 	/**
 	 * test that if same voter identifies more than once then a matched exception is thrown
 	 * @throws IdentificationError
 	 * @throws AlreadyIdentified
 	 * @throws VoterDoesntExist
 	 */
 	@Test(expected = IMainframe.IdentificationError.class )
 	public void testIdentification2() throws IdentificationError, AlreadyIdentified, VoterDoesntExist {
 		mainframe.initialize();
 		mainframe.identification(111);
 		mainframe.identification(222);
 		mainframe.identification(111);
 	}
 	
 	
 	/**
 	 * check if an unregistered voter is written to the backup file
 	 * @throws IdentificationError
 	 * @throws AlreadyIdentified
 	 * @throws VoterDoesntExist
 	 */
 	@Test
 	public void testIdentification3() throws IdentificationError, AlreadyIdentified, VoterDoesntExist {
 		mainframe.initialize();
 		mainframe.identification(123);
 		mainframe.shutDown();
 		IPartiesList p = backupStubFactory.getCreatedBackupStub().restoreParties();
 		IVotersList v = backupStubFactory.getCreatedBackupStub().restoreVoters();
 		IVotersList u = backupStubFactory.getCreatedBackupStub().restoreUnregisteredVoters();
 		assertTrue(!votersListFactory.createInstance().equals( u));
 		assertTrue(!readVotersList.equals( v));
 		assertEquals(readPartiesList, p);
 		readVotersList.addVoter(voterDataFactory.createInstance(123));
 		readVotersList.findVoter(123).markIdentified();
 		assertEquals(readVotersList, v);
 		IVotersList temp = votersListFactory.createInstance();
 		temp.addVoter(voterDataFactory.createInstance(123));
 		temp.findVoter(123).markIdentified();
 		assertEquals(temp,u);
 	}
 
 	/**
 	 * there is nothing to test here
 	 */
 	@Ignore
 	public void testPeep() {
 		//always works
 	}
 
 	/**
 	 * tests that even after a voter identified and voted, a second voter cannot vote
 	 * without identifying first
 	 * @throws VoterDoesNotExist
 	 * @throws Unidentified
 	 * @throws VoterDoesntExist
 	 * @throws IdentificationError
 	 */
 	@Test(expected = IMainframe.VoterDoesNotExist.class)
 	public void testMarkVoted() throws VoterDoesNotExist, Unidentified, VoterDoesntExist, IdentificationError {
 		mainframe.initialize();
 		mainframe.identification(111);
 		mainframe.markVoted(111);
 		mainframe.markVoted(222);
 		
 	}
 	
 	/**
 	 * checks that after voting of two voters the backup files that mainframe does after it
 	 * shuts down are consistent
 	 * @throws VoterDoesNotExist
 	 * @throws Unidentified
 	 * @throws VoterDoesntExist
 	 * @throws IdentificationError
 	 * @throws AlreadyIdentified
 	 * @throws PartyDoesNotExist
 	 */
 	@Test
 	public void testMarkVoted2() throws VoterDoesNotExist, Unidentified, VoterDoesntExist, IdentificationError, AlreadyIdentified, PartyDoesNotExist {
 		mainframe.initialize();
 		mainframe.identification(111);
 		mainframe.identification(222);
 		mainframe.markVoted(111);
 		mainframe.markVoted(222);
 		markVotesOnStationController(2);
 		mainframe.shutDown();
 		IPartiesList p = backupStubFactory.getCreatedBackupStub().restoreParties();
 		IVotersList v = backupStubFactory.getCreatedBackupStub().restoreVoters();
 		IVotersList u = backupStubFactory.getCreatedBackupStub().restoreUnregisteredVoters();
 		assertEquals(votersListFactory.createInstance(), u);
 		assertTrue(!readPartiesList.equals( p));
 		assertTrue(!readVotersList.equals( v));
 		readPartiesList.getPartyBySymbol("oui").increaseVoteNumber();
 		readPartiesList.getPartyBySymbol("oui").increaseVoteNumber();
 		//readVotersList.peep();
 		assertEquals(readPartiesList, p);
 		readVotersList.findVoter(111).markIdentified();
 		readVotersList.findVoter(111).markVoted();
 		readVotersList.findVoter(222).markIdentified();
 		readVotersList.findVoter(222).markVoted();
		assertTrue(readVotersList.equals( v));
 		
 	}
 	
 	/**
 	 * checks that an unregistered voter cannot vote without identifying first
 	 * @throws IdentificationError
 	 * @throws VoterDoesNotExist
 	 * @throws Unidentified
 	 * @throws VoterDoesntExist
 	 */
 	@Test(expected = IMainframe.VoterDoesNotExist.class)
 	public void testMarkVoted3() throws IdentificationError, VoterDoesNotExist, Unidentified, VoterDoesntExist {
 		mainframe.initialize();
 		mainframe.markVoted(123);
 
 	}
 	
 	/**
 	 * checks that after a mixed voting of registered and unregistered users the backup files
 	 * in the end are consistent to what we have done in the test
 	 * @throws IdentificationError
 	 * @throws VoterDoesNotExist
 	 * @throws Unidentified
 	 * @throws VoterDoesntExist
 	 * @throws AlreadyIdentified
 	 * @throws PartyDoesNotExist
 	 */
 	@Test
 	public void testMarkVoted4() throws IdentificationError, VoterDoesNotExist, Unidentified, VoterDoesntExist, AlreadyIdentified, PartyDoesNotExist {
 		mainframe.initialize();
 		mainframe.identification(123);
 		mainframe.identification(222);
 		mainframe.markVoted(123);
 		mainframe.markVoted(222);
 		markVotesOnStationController(2);
 		mainframe.shutDown();
 		IPartiesList p = backupStubFactory.getCreatedBackupStub().restoreParties();
 		IVotersList v = backupStubFactory.getCreatedBackupStub().restoreVoters();
 		IVotersList u = backupStubFactory.getCreatedBackupStub().restoreUnregisteredVoters();
 		assertTrue(!votersListFactory.createInstance() .equals( u));
 		assertFalse(u.isEmpty());
 		assertTrue(!readPartiesList .equals( p));
 		readPartiesList.getPartyBySymbol("oui").increaseVoteNumber();
 		readPartiesList.getPartyBySymbol("oui").increaseVoteNumber();
 		assertEquals(readPartiesList, p);
 			
 		assertTrue(!readVotersList .equals( v));
 		IVotersList temp = votersListFactory.createInstance();
 		IVoterData newVoter = voterDataFactory.createInstance(123);
 		newVoter.markIdentified();
 		newVoter.markVoted();
 		temp.addVoter(newVoter);
 		assertEquals(temp, u);
 		readVotersList.addVoter(voterDataFactory.createInstance(123));
 		readVotersList.findVoter(123).markIdentified();
 		readVotersList.findVoter(123).markVoted();
 		readVotersList.findVoter(222).markIdentified();
 		readVotersList.findVoter(222).markVoted();
 		assertEquals(readVotersList, v);
 	}
 
 	/**
 	 * checks that if voters started voting, the mainframe is really aware of that
 	 * and does not ignore that
 	 * @throws VoterDoesNotExist
 	 * @throws VoterStartedVote
 	 * @throws IdentificationError
 	 * @throws PartyDoesNotExist
 	 * @throws Unidentified
 	 * @throws VoterDoesntExist
 	 * @throws AlreadyIdentified
 	 */
 	@Test
 	public void testMarkStartedVote() throws VoterDoesNotExist, VoterStartedVote, IdentificationError, PartyDoesNotExist, Unidentified, VoterDoesntExist, AlreadyIdentified {
 		mainframe.initialize();
 		mainframe.identification(111);
 		mainframe.markStartedVote(111);
 		mainframe.identification(222);
 		mainframe.markStartedVote(222);
 		mainframe.shutDown();
 		IPartiesList p = backupStubFactory.getCreatedBackupStub().restoreParties();
 		IVotersList v = backupStubFactory.getCreatedBackupStub().restoreVoters();
 		IVotersList u = backupStubFactory.getCreatedBackupStub().restoreUnregisteredVoters();
 		assertEquals(votersListFactory.createInstance(), u);
 		assertEquals(readPartiesList, p);
 		assertTrue(!readVotersList .equals( v));
 		readVotersList.findVoter(111).markIdentified();
 		readVotersList.findVoter(111).markStartedVote();
 		readVotersList.findVoter(222).markIdentified();
 		readVotersList.findVoter(222).markStartedVote();
 		assertEquals(readVotersList, v);	
 	}
 	
 	/**
 	 * if a voter is marked as 'StartedVote' twice a matched exception will be thrown
 	 * @throws VoterDoesNotExist
 	 * @throws VoterStartedVote
 	 * @throws IdentificationError
 	 */
 	@Test(expected = IMainframe.VoterStartedVote.class)
 	public void testMarkStartedVote2() throws VoterDoesNotExist, VoterStartedVote, IdentificationError {
 		mainframe.initialize();
 		mainframe.identification(111);
 		mainframe.markStartedVote(111);
 		mainframe.markStartedVote(111);
 	}
 	
 	/**
 	 * tests that an unregistered voter cannot be mark as someone who is started voting
 	 * before he is identified first 
 	 * @throws VoterDoesNotExist
 	 * @throws VoterStartedVote
 	 */
 	@Test(expected = IMainframe.VoterDoesNotExist.class)
 	public void testMarkStartedVote3() throws VoterDoesNotExist, VoterStartedVote {
 		mainframe.initialize();
 		mainframe.markStartedVote(123);
 	}
 	
 	/**
 	 * a test of two unregistered voters such that the first is identified, starting voting
 	 * and then finishing voting and then the second is identified and then starting voting
 	 * @throws VoterDoesNotExist
 	 * @throws VoterStartedVote
 	 * @throws IdentificationError
 	 * @throws PartyDoesNotExist
 	 * @throws Unidentified
 	 * @throws VoterDoesntExist
 	 * @throws AlreadyIdentified
 	 */
 	@Test
 	public void testMarkStartedVote4() throws VoterDoesNotExist, VoterStartedVote, IdentificationError, PartyDoesNotExist, Unidentified, VoterDoesntExist, AlreadyIdentified {
 		mainframe.initialize();
 		mainframe.identification(123);
 		mainframe.markStartedVote(123);
 		mainframe.markVoted(123);
 		markVotesOnStationController(1);
 		mainframe.identification(456);
 		mainframe.markStartedVote(456);
 		mainframe.shutDown();
 		IPartiesList p = backupStubFactory.getCreatedBackupStub().restoreParties();
 		IVotersList v = backupStubFactory.getCreatedBackupStub().restoreVoters();
 		IVotersList u = backupStubFactory.getCreatedBackupStub().restoreUnregisteredVoters();
 		assertTrue(!votersListFactory.createInstance().equals(u));
 		IVotersList temp = votersListFactory.createInstance();
 		temp.addVoter(voterDataFactory.createInstance(123));
 		temp.addVoter(voterDataFactory.createInstance(456));
 		temp.findVoter(123).markIdentified();
 		temp.findVoter(123).markVoted();
 		temp.findVoter(456).markIdentified();
 		temp.findVoter(456).markStartedVote();
 		assertEquals(temp, u);
 		assertTrue(!readPartiesList.equals(p));
 		readPartiesList.getPartyBySymbol("oui").increaseVoteNumber();
 		assertEquals(readPartiesList, p);
 		assertTrue(!readVotersList .equals( v));
 		readVotersList.addVoter(voterDataFactory.createInstance(123));
 		readVotersList.addVoter(voterDataFactory.createInstance(456));
 		readVotersList.findVoter(123).markIdentified();
 		readVotersList.findVoter(123).markStartedVote();
 		readVotersList.findVoter(123).markVoted();
 		readVotersList.findVoter(456).markIdentified();
 		readVotersList.findVoter(456).markStartedVote();
 		assertEquals(readVotersList, v);	
 	}
 	
 	/**
 	 * a more complex test of two registered voters who are starting to vote
 	 * @throws VoterDoesNotExist
 	 * @throws VoterStartedVote
 	 * @throws IdentificationError
 	 * @throws PartyDoesNotExist
 	 * @throws Unidentified
 	 * @throws VoterDoesntExist
 	 * @throws AlreadyIdentified
 	 */
 	@Test
 	public void testMarkStartedVote5() throws VoterDoesNotExist, VoterStartedVote, IdentificationError, PartyDoesNotExist, Unidentified, VoterDoesntExist, AlreadyIdentified {
 		mainframe.initialize();
 		mainframe.identification(111);
 		mainframe.markStartedVote(111);
 		mainframe.markVoted(111);
 		stationsControllerStubFactory.getStationsController().markVoted(111);
 		mainframe.identification(222);
 		mainframe.markStartedVote(222);
 		mainframe.shutDown();
 		IPartiesList p = backupStubFactory.getCreatedBackupStub().restoreParties();
 		IVotersList v = backupStubFactory.getCreatedBackupStub().restoreVoters();
 		IVotersList u = backupStubFactory.getCreatedBackupStub().restoreUnregisteredVoters();
 		assertEquals(votersListFactory.createInstance(), u);
 		assertTrue(!readPartiesList.equals(p));
 		readPartiesList.getPartyBySymbol("oui").increaseVoteNumber();
 		assertEquals(readPartiesList, p);
 		assertTrue(!readVotersList .equals( v));
 		readVotersList.findVoter(111).markIdentified();
 		//readVotersList.findVoter(111).markStartedVote();
 		readVotersList.findVoter(111).markVoted();
 		readVotersList.findVoter(222).markIdentified();
 		readVotersList.findVoter(222).markStartedVote();
 		assertEquals(readVotersList, v);	
 	}
 
 	/**
 	 * test that the method 'getVoterStatus' returns the right status for all the possible statuses
 	 * @throws IdentificationError
 	 * @throws VoterDoesNotExist
 	 * @throws VoterStartedVote
 	 */
 	@Test
 	public void testGetVoterStatus() throws IdentificationError, VoterDoesNotExist, VoterStartedVote {
 		mainframe.initialize();
 		IMainframe.VoterStatus stat;
 		stat = mainframe.getVoterStatus(111);
 		assertEquals(IMainframe.VoterStatus.unidentified, stat);
 		mainframe.identification(111);
 		stat = mainframe.getVoterStatus(111);
 		assertEquals(IMainframe.VoterStatus.identified, stat);
 		mainframe.markStartedVote(111);
 		stat = mainframe.getVoterStatus(111);
 		assertEquals(IMainframe.VoterStatus.startedVote, stat);
 		mainframe.markVoted(111);
 		stat = mainframe.getVoterStatus(111);
 		assertEquals(IMainframe.VoterStatus.voted, stat);
 		
 		stat = mainframe.getVoterStatus(123);
 		assertEquals(IMainframe.VoterStatus.unidentified, stat);
 	}
 	
 	/**
 	 * check that the hot backup automatic backup really works
 	 * @throws AlreadyIdentified
 	 * @throws VoterDoesntExist
 	 * @throws Unidentified
 	 * @throws InterruptedException 
 	 * @throws IdentificationError 
 	 * @throws VoterStartedVote 
 	 * @throws VoterDoesNotExist 
	 * @throws PartyDoesNotExist 
 	 */
 	@Test
	public void hotBackupWorks() throws AlreadyIdentified, VoterDoesntExist, Unidentified, InterruptedException, IdentificationError, VoterDoesNotExist, VoterStartedVote, PartyDoesNotExist{
 		mainframe.initialize();
 		IVotersList emptyList = new VotersListFactory().createInstance();
 		mainframe.identification(111);
 		mainframe.identification(222);
 		mainframe.markVoted(222);
 		markVotesOnStationController(1);
 		
 		//waiting for the backup
 		Thread.sleep((backupTimeIntervalSeconds)*1000+50);	
 		
 		IPartiesList p = backupStubFactory.getCreatedBackupStub().restoreParties();
 		IVotersList v = backupStubFactory.getCreatedBackupStub().restoreVoters();
 		IVotersList u = backupStubFactory.getCreatedBackupStub().restoreUnregisteredVoters();
		
		readPartiesList.getPartyBySymbol("oui").increaseVoteNumber();
		assertEquals(readPartiesList, p);
 		assertEquals(emptyList,u);
 		IVotersList newVotersList = readVotersList.copy();
 		newVotersList.findVoter(111).markIdentified();
 		newVotersList.findVoter(222).markIdentified();
 		newVotersList.findVoter(222).markVoted();		
 		assertEquals(newVotersList, v);
 		// TODO What about p? Is the test incomplete?
 	}
 
 }
