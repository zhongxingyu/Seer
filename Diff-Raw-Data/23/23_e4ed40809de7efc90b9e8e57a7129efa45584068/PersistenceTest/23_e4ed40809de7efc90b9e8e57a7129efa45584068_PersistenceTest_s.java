 package org.oregami.test;
 
 import java.util.Iterator;
 import java.util.List;
 import java.util.Set;
 
 import javax.persistence.EntityManager;
 
 import org.joda.time.LocalDate;
 import org.junit.After;
 import org.junit.Assert;
 import org.junit.Before;
 import org.junit.BeforeClass;
 import org.junit.Test;
 import org.oregami.data.DatabaseFiller;
 import org.oregami.data.GameDao;
 import org.oregami.data.GameEntryTypeDao;
 import org.oregami.data.GameTitleDao;
 import org.oregami.data.LanguageDao;
 import org.oregami.data.PublicationFranchiseDao;
 import org.oregami.data.RegionDao;
 import org.oregami.data.TitleTypeDao;
 import org.oregami.dropwizard.OregamiService;
 import org.oregami.entities.Game;
 import org.oregami.entities.GameTitle;
 import org.oregami.entities.GameToGameTitleConnection;
 import org.oregami.entities.Language;
 import org.oregami.entities.Publication;
 import org.oregami.entities.PublicationFranchise;
 import org.oregami.entities.PublicationIssue;
 import org.oregami.entities.Region;
 import org.oregami.entities.ReleaseGroup;
 import org.oregami.entities.datalist.GameEntryType;
 import org.oregami.entities.datalist.ReleaseType;
 import org.oregami.entities.datalist.TitleType;
 
 import com.google.inject.Guice;
 import com.google.inject.Injector;
 import com.google.inject.persist.PersistService;
 import com.google.inject.persist.jpa.JpaPersistModule;
 
 public class PersistenceTest {
 
 	private static Injector injector;
 	
 	EntityManager entityManager = null;
 	
 	public PersistenceTest() {
 	}
 	
 	@BeforeClass
 	public static void init() {
 		JpaPersistModule jpaPersistModule = new JpaPersistModule(OregamiService.JPA_UNIT);
 		injector = Guice.createInjector(jpaPersistModule);
 		injector.getInstance(PersistenceTest.class);
 		PersistService persistService = injector.getInstance(PersistService.class);
 		persistService.start();
 	}
 	
 	@Before
 	public void startTx() {
 		if (entityManager==null) {
 			entityManager = injector.getInstance(EntityManager.class);
 		}
 		entityManager.getTransaction().begin();
 		
 		//deletes are not necessary, because of the Rollback after every Test
 //		DatabaseFiller.getInstance().deleteGameData();
 //		DatabaseFiller.getInstance().deleteBaseListData();
 	}
 	
 	@After
 	public void rollbackTx() {
 		entityManager.getTransaction().rollback();
 	}
 	
 	private <T> T getInstance(Class<T> c) {
 		return injector.getInstance(c);
 	}
 	
 	@Test
 	public void testSaveGame() {
 		Game game = new Game();
 		game.connectGameTitle(new GameTitle("The Secret of Monkey Island"), new TitleType(TitleType.ORIGINAL_TITLE));
 		
 		GameDao gameDao = getInstance(GameDao.class);
 		Long gameId = gameDao.save(game);
 		Assert.assertNotNull(gameId);
 		
 		Game loadedGame = gameDao.findOne(gameId);
 		Assert.assertNotNull(loadedGame);
 		Assert.assertEquals(game.getGameEntryType(), loadedGame.getGameEntryType());
 	}
 	
 	@Test
 	public void testSaveGameEntryType() {
 		GameEntryType gameEntryType = new GameEntryType(GameEntryType.GAME);
 		GameEntryTypeDao gameEntryTypeDao = getInstance(GameEntryTypeDao.class);
 		gameEntryTypeDao.save(gameEntryType);
 		
 		GameEntryType loadedGameEntryType = gameEntryTypeDao.findOne(gameEntryType.getId());
 		Assert.assertNotNull(loadedGameEntryType);
 		Assert.assertEquals(loadedGameEntryType.getValue(), gameEntryType.getValue());
 	}
 	
 	
 	
 	@Test
 	public void testSaveMultipleGameEntryTypes() {
 		GameEntryTypeDao gameEntryTypeDao = getInstance(GameEntryTypeDao.class);
 
 		GameEntryType gameEntryType = new GameEntryType(GameEntryType.GAME);
 		gameEntryTypeDao.save(gameEntryType);
 		
 		GameEntryType gameEntryType2 = new GameEntryType(GameEntryType.COMPILATION);
 		gameEntryTypeDao.save(gameEntryType2);
 		
 		GameEntryType gameEntryType3 = new GameEntryType(GameEntryType.ADD_ON_SIGNIFICANT);
 		gameEntryTypeDao.save(gameEntryType3);
 		
 		List<GameEntryType> all = gameEntryTypeDao.findAll();
 		Assert.assertNotNull(all);
 		Assert.assertEquals(all.size(), 3);
 	}	
 	
 	
 	/**
 	 * if a Game is deleted, does the associated GameEntryType stay in the database?
 	 * This works because the Game.GameEntryType field does NOT have CascadeType.ALL.
 	 */
 	@Test
 	public void testSaveAndDeleteGameWithMultipleGameEntryTypes() {
 		GameEntryTypeDao gameEntryTypeDao = getInstance(GameEntryTypeDao.class);
 		GameDao gameDao = getInstance(GameDao.class);
 		GameTitleDao gameTitleDao = getInstance(GameTitleDao.class);
 		
 		GameTitle gameTitle = new GameTitle("The Secret of Monkey Island");
 		gameTitleDao.save(gameTitle);
 		
 		TitleTypeDao titleTypeDao = getInstance(TitleTypeDao.class);
 		TitleType titleType = new TitleType(TitleType.ORIGINAL_TITLE);
 		titleTypeDao.save(titleType);
 		
 		GameEntryType gameEntryType = new GameEntryType(GameEntryType.GAME);
 		gameEntryTypeDao.save(gameEntryType);
 		
 		GameEntryType gameEntryType2 = new GameEntryType(GameEntryType.COMPILATION);
 		gameEntryTypeDao.save(gameEntryType2);
 		
 		GameEntryType gameEntryType3 = new GameEntryType(GameEntryType.ADD_ON_SIGNIFICANT);
 		gameEntryTypeDao.save(gameEntryType3);
 		
 		Game game = new Game();
 		game.connectGameTitle(gameTitle, titleType);
 		game.setGameEntryType(gameEntryType);
 		Long gameId = gameDao.save(game);
 		Assert.assertNotNull(gameId);
 		
 		List<GameEntryType> allGameEntryTypes = gameEntryTypeDao.findAll();
 		Assert.assertNotNull(allGameEntryTypes);
 		Assert.assertEquals(allGameEntryTypes.size(), 3);
 		
 		List<Game> allGames = gameDao.findAll();
 		Assert.assertNotNull(allGames);
 		Assert.assertEquals(allGames.size(), 1);
 		
 		gameDao.delete(game);
 		
 		allGames = gameDao.findAll();
 		Assert.assertNotNull(allGames);
 		Assert.assertEquals(allGames.size(), 0);
 		
 		allGameEntryTypes = gameEntryTypeDao.findAll();
 		Assert.assertNotNull(allGameEntryTypes);
 		Assert.assertEquals(allGameEntryTypes.size(), 3);		
 		
 	}		
 	
 	@Test
 	public void testSaveGameWithGameEntryType() {
 		
 		GameEntryType gameEntryType = new GameEntryType(GameEntryType.GAME);
 		GameEntryTypeDao gameEntryTypeDao = getInstance(GameEntryTypeDao.class);
 		gameEntryTypeDao.save(gameEntryType);
 		
 		GameEntryType loadedGameEntryType = gameEntryTypeDao.findOne(gameEntryType.getId());
 		Assert.assertNotNull(loadedGameEntryType);
 		Assert.assertEquals(loadedGameEntryType.getValue(), gameEntryType.getValue());
 		
 		GameDao gameDao = getInstance(GameDao.class);
 		
 		Game game = new Game();
 		game.setGameEntryType(gameEntryType);
 		Long gameId = gameDao.save(game);
 		Assert.assertNotNull(gameId);
 
 		Game game2 = new Game();
 		game2.setGameEntryType(gameEntryType);
 		Long gameId2 = gameDao.save(game2);
 		Assert.assertNotNull(gameId2);
 		
 		Game loadedGame = gameDao.findOne(gameId);
 		Assert.assertNotNull(loadedGame);
 		Assert.assertEquals(game.getGameEntryType(), loadedGame.getGameEntryType());
 
 		Game loadedGame2 = gameDao.findOne(gameId2);
 		Assert.assertNotNull(loadedGame2);
 		Assert.assertEquals(game2.getGameEntryType(), loadedGame2.getGameEntryType());
 		
 		List<Game> allGames = gameDao.findAll();
 		Assert.assertEquals(2, allGames.size());
 		
 		
 	}
 	
 	
 	@Test
 	public void testSaveReleaseGroup() {
 		
 		ReleaseType releaseType = new ReleaseType(ReleaseType.NATIVE_DEVELOPMENT);
 		entityManager.persist(releaseType);
 		
 		ReleaseGroup releaseGroup = new ReleaseGroup();
 		releaseGroup.setReleaseType(releaseType);
 		entityManager.persist(releaseGroup);
 		Long id = releaseGroup.getId();
 		Assert.assertNotNull(id);
 		
 		ReleaseGroup rgLoaded = entityManager.find(ReleaseGroup.class, id);
 		Assert.assertNotNull(rgLoaded);
 		Assert.assertEquals(rgLoaded.getReleaseType(), releaseGroup.getReleaseType());
 		System.out.println(rgLoaded);
 		
 	}
 	
 	@Test
 	public void testSaveGameTitle() {
 		
 		DatabaseFiller.getInstance().addLanguages();
 		
 		LanguageDao languageDao = getInstance(LanguageDao.class);
 		Language languageEn = languageDao.findByExactName(Language.ENGLISH);
 		
 		GameTitleDao titleDao = getInstance(GameTitleDao.class);
 		GameTitle title = new GameTitle("The Secret of Monkey Island");
 		title.setLanguage(languageEn);
 		Long long1 = titleDao.save(title);
 		
 		Assert.assertEquals(titleDao.findAll().size(), 1);
 		
 		GameTitle title2 = new GameTitle("Le Secret de L'Ile aux Singes");
 		titleDao.save(title2);
 		
 		Assert.assertEquals(titleDao.findAll().size(), 2);
 		
 		GameTitle loadedGameTitle = titleDao.findOne(long1);
 		Assert.assertEquals(loadedGameTitle.getLanguage(), languageEn);
 		
 		System.out.println(loadedGameTitle);
 		
 	}
 	
 	
 	@Test
 	public void testSaveGameWithGameTitle() {
 		GameTitleDao titleDao = getInstance(GameTitleDao.class);
 		GameTitle title = new GameTitle("The Secret of Monkey Island");
 		titleDao.save(title);
 		GameTitle title2 = new GameTitle("Monkey Island");
 		titleDao.save(title2);
 		Assert.assertEquals(titleDao.findAll().size(), 2);
 		
 		TitleTypeDao titleTypeDao = getInstance(TitleTypeDao.class);
 		TitleType titleType = new TitleType(TitleType.ORIGINAL_TITLE);
 		TitleType titleType2 = new TitleType(TitleType.ABBREVIATION);
 		titleTypeDao.save(titleType);
 		titleTypeDao.save(titleType2);
 		
 		Assert.assertEquals(titleTypeDao.findAll().size(), 2);
 
 		GameDao gameDao = getInstance(GameDao.class);
 		Game game = new Game();
 		game.connectGameTitle(title, titleType);
 		game.connectGameTitle(title2, titleType2);
 		Long gameId = gameDao.save(game);
 		
 		Game gameLoaded = gameDao.findOne(gameId);
 		Set<GameToGameTitleConnection> connectionList = gameLoaded.getGameToGameTitleConnectionList();
 		Assert.assertEquals(connectionList.size(), 2);
 		
 		GameToGameTitleConnection gameTitleConnectionLoaded = connectionList.iterator().next();
 		Assert.assertNotNull(gameTitleConnectionLoaded);
 		
 		Assert.assertEquals(titleTypeDao.findAll().size(), 2);
 		
 		//delete Game an test if GameTitle objects stay in the database
 		gameDao.delete(game);
 		List<Game> allGames = gameDao.findAll();
 		Assert.assertNotNull(allGames);
 		Assert.assertEquals(allGames.size(), 0);
 		Assert.assertEquals(titleTypeDao.findAll().size(), 2);
 		
 	}
 	
 	@Test
 	public void testSaveGameWithGameTitle2() {
 		GameTitleDao titleDao = getInstance(GameTitleDao.class);
 		GameTitle title = new GameTitle("The Secret of Monkey Island");
 		titleDao.save(title);
 		GameTitle title2 = new GameTitle("Monkey Island");
 		titleDao.save(title2);
 		Assert.assertEquals(titleDao.findAll().size(), 2);
 		
 		TitleTypeDao titleTypeDao = getInstance(TitleTypeDao.class);
 		TitleType titleType = new TitleType(TitleType.ORIGINAL_TITLE);
 		TitleType titleType2 = new TitleType(TitleType.ABBREVIATION);
 		titleTypeDao.save(titleType);
 		titleTypeDao.save(titleType2);
 		
 		Assert.assertEquals(titleTypeDao.findAll().size(), 2);
 
 		GameDao gameDao = getInstance(GameDao.class);
 		Game game = new Game();
 		game.connectGameTitle(title, titleType);
 		game.connectGameTitle(title2, titleType2);
 		Long gameId = gameDao.save(game);
 		
 		Game gameLoaded = gameDao.findOne(gameId);
 		Set<GameToGameTitleConnection> connectionList = gameLoaded.getGameToGameTitleConnectionList();
 		Assert.assertEquals(connectionList.size(), 2);
 		
 		GameToGameTitleConnection gameTitleConnectionLoaded = connectionList.iterator().next();
 		Assert.assertNotNull(gameTitleConnectionLoaded);
 		//Assert.assertEquals(gameTitleConnectionLoaded.getTitleType(), titleType2);
 		
 		Assert.assertEquals(titleTypeDao.findAll().size(), 2);
 		
 	}	
 	
 	
 	@Test
 	public void testLanguage() {
 		LanguageDao languageDao = injector.getInstance(LanguageDao.class);
 		
 		Language german = new Language(Language.GERMAN);
 		Long langId = languageDao.save(german);
 		Assert.assertNotNull("ID expected", langId);
 		
 		List<Language> all = languageDao.findAll();
 		Assert.assertTrue("1 language expected", all.size()==1);
 		
 		Language english = new Language(Language.ENGLISH);
 		Long langId2 = languageDao.save(english);
 		
 		all = languageDao.findAll();
 		Assert.assertTrue("2 languages expected", all.size()==2);
 		
 		Language loadedGerman = languageDao.findByExactName(Language.GERMAN);
 		Assert.assertNotNull(loadedGerman);
 		Assert.assertEquals(loadedGerman.getId(), langId);
 		Assert.assertEquals(loadedGerman, german);
 		
 		Language loadedEnglish = languageDao.findByExactName(Language.ENGLISH);
 		Assert.assertNotNull(loadedEnglish);
 		Assert.assertEquals(loadedEnglish.getId(), langId2);
 		Assert.assertEquals(loadedEnglish, english);
 		
 	}
 	
 	
 	@Test
 	public void testRegion() {
 		RegionDao regionDao = injector.getInstance(RegionDao.class);
 		
 		Region germany = new Region(Region.GERMANY);
 		Long long1 = regionDao.save(germany);
 		Assert.assertNotNull("ID expected", long1);
 		
 		List<Region> all = regionDao.findAll();
 		Assert.assertTrue("1 Region expected", all.size()==1);
 		
 		Region europe = new Region(Region.EUROPE);
 		Long long2 = regionDao.save(europe);
 		
 		all = regionDao.findAll();
 		Assert.assertTrue("2 Regions expected", all.size()==2);
 		
 		Region loadedGermany = regionDao.findByExactName(Region.GERMANY);
 		Assert.assertNotNull(loadedGermany);
 		Assert.assertEquals(loadedGermany.getId(), long1);
 		Assert.assertEquals(loadedGermany, germany);
 		
 		Region loadedEurope = regionDao.findByExactName(Region.EUROPE);
 		Assert.assertNotNull(loadedEurope);
 		Assert.assertEquals(loadedEurope.getId(), long2);
 		Assert.assertEquals(loadedEurope, europe);		
 	}
 	
 	@Test
 	public void testPublication() {
 		PublicationFranchiseDao publicationFranchiseDao = injector.getInstance(PublicationFranchiseDao.class);
 		PublicationFranchise pf = new PublicationFranchise("Power Play");
 		
 		Publication publicationPowerPlay = new Publication("Power Play");
 		pf.getPublicationList().add(publicationPowerPlay);
 		
 		PublicationIssue issue = new PublicationIssue();
 		issue.setIssueNameYear(1990);
 		issue.setIssueNameNumber(1);
 		publicationPowerPlay.getPublicationIssueList().add(issue);
 		PublicationIssue issue2 = new PublicationIssue();
 		issue2.setIssueNameYear(1990);
 		issue2.setIssueNameNumber(2);
 		issue2.setReleaseDate(new LocalDate(1990,3,16));
 		
 		
 		publicationPowerPlay.getPublicationIssueList().add(issue2);		
 		
 		Publication publicationChipPowerPlay = new Publication("CHIP Power Play");
 		pf.getPublicationList().add(publicationChipPowerPlay);
 		
 		publicationFranchiseDao.save(pf);
 		
 		List<PublicationFranchise> findAll = publicationFranchiseDao.findAll();
 		Assert.assertNotNull(findAll);
 		Assert.assertTrue(findAll.size()==1);
 		
 		PublicationFranchise franchiseLoaded = findAll.get(0);
 		Assert.assertEquals(franchiseLoaded.getId(), pf.getId());
 		Assert.assertEquals(franchiseLoaded.getName(), pf.getName());
		
		Publication publication = franchiseLoaded.getPublicationList().iterator().next();
		Iterator<PublicationIssue> issueIterator = publication.getPublicationIssueList().iterator();
 		boolean releaseDateFound = false;
 		LocalDate localDate = new LocalDate(1990,3,16);
		while (issueIterator.hasNext()) {
			PublicationIssue publicationIssue = issueIterator.next();
			if (localDate.equals(publicationIssue.getReleaseDate())) {
				releaseDateFound=true;
 			}
 		}
 		Assert.assertTrue("release date not found", releaseDateFound);
 		
 		
 		System.out.println(pf);
 	}
 	
 }
