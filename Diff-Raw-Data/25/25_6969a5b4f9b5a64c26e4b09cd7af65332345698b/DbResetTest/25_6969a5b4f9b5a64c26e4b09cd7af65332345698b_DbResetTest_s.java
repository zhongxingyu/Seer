 package edu.wheaton.hymnal.data;
 
 import java.sql.SQLException;
 import java.util.List;
 
 import com.j256.ormlite.dao.Dao;
 import com.j256.ormlite.dao.DaoManager;
 import com.j256.ormlite.jdbc.JdbcConnectionSource;
 import com.j256.ormlite.support.ConnectionSource;
 import com.j256.ormlite.table.TableUtils;
 
 public class DbResetTest {
 	private ConnectionSource connectionSource;
 	private Dao<Meter, String> meterDao;
 	private Dao<Text, String> textDao;
 	private Dao<Tune, String> tuneDao;
 	private Dao<Stanza, String> stanzaDao;
 	private Dao<Voice, String> voiceDao;
 
 	public DbResetTest() throws SQLException {
 //		// sqlite
 //		String sqliteDbPath = this.getClass().getResource("/hymnal.sqlite").getPath();
 //		String sqliteDatabaseUrl = "jdbc:sqlite:" + sqliteDbPath;
 //		// create a connection source to our database
 //		connectionSource = new JdbcConnectionSource(sqliteDatabaseUrl);
 		
 		// h2
 		String h2DbPath = this.getClass().getResource("/hymnal.h2").getPath();
 		String h2DbUrl = "jdbc:h2:" + h2DbPath;
 		// create a connection source to our database
 		connectionSource = new JdbcConnectionSource(h2DbUrl);
 
 		// instantiate the Dao's
		meterDao = DaoManager.createDao(connectionSource, Meter.class);
		textDao = DaoManager.createDao(connectionSource, Text.class);
		tuneDao = DaoManager.createDao(connectionSource, Tune.class);
		stanzaDao = DaoManager.createDao(connectionSource, Stanza.class);
		voiceDao = DaoManager.createDao(connectionSource, Voice.class);
 	}
 
 	public void setupDb() throws SQLException {
 		// Meter
 		TableUtils.dropTable(connectionSource, Meter.class, true);
 		TableUtils.createTable(connectionSource, Meter.class);
 		Meter commonMeter = new Meter("8 6 8 6", "Common Meter");
 		meterDao.create(commonMeter);
 
 		// Text & Tune
 		TableUtils.dropTable(connectionSource, Text.class, true);
 		TableUtils.createTable(connectionSource, Text.class);
 		TableUtils.dropTable(connectionSource, Tune.class, true);
 		TableUtils.createTable(connectionSource, Tune.class);
 		Text amazingGrace = new Text(
 				"Amazing Grace",
 				commonMeter,
 				"John Newton (1725 - 1807), 1779 (poet of the seventh verse is unknown, 1829)",
 				null);
 		Tune newBritain = new Tune("New Britain", commonMeter,
 				"James P. Carrell (1787 - 1854) and David S. Clayton, 1831",
 				"g \\major", "3/4", "4 = 90", "4", amazingGrace);
 		// set default Tune
 		amazingGrace.setDefaultTune(newBritain);
 		textDao.create(amazingGrace);
 		tuneDao.create(newBritain);
 		// revisit Text to set defaultTune properly, now that we have its id
 		textDao.update(amazingGrace);
 
 		// Stanza
 		TableUtils.dropTable(connectionSource, Stanza.class, true);
 		TableUtils.createTable(connectionSource, Stanza.class);
 		Stanza stanzaOne = new Stanza(1,
 				"	A -- maz -- ing grace! How sweet the sound\n"
 						+ "	That saved a wretch like me!\n"
 						+ "	I once was lost, but now am found;\n"
 						+ "	Was blind, but now I see.", amazingGrace);
 		stanzaDao.create(stanzaOne);
 		Stanza stanzaTwo = new Stanza(2,
 				"	’Twas grace that taught my heart to fear,\n"
 						+ "	And grace my fears re -- lieved;\n"
 						+ "	How pre -- cious did that grace ap -- pear\n"
 						+ "	The hour I first be -- lieved!", amazingGrace);
 		stanzaDao.create(stanzaTwo);
 		Stanza stanzaThree = new Stanza(3,
 				"	Through ma -- ny dan -- gers, toils and snares,\n"
 						+ "	I have al -- rea -- dy come;\n"
 						+ "	’Tis grace hath brought me safe thus far,\n"
 						+ "	And grace will lead me home.", amazingGrace);
 		stanzaDao.create(stanzaThree);
 		Stanza stanzaFour = new Stanza(4,
 				"	The Lord has prom -- ised good to me,\n"
 						+ "	His Word my hope se -- cures;\n"
 						+ "	He will my Shield and Por -- tion be,\n"
 						+ "	As long as life en -- dures.", amazingGrace);
 		stanzaDao.create(stanzaFour);
 		Stanza stanzaFive = new Stanza(5,
 				"	Yea, when this flesh and heart shall fail,\n"
 						+ "	And mor -- tal life shall cease,\n"
 						+ "	I shall pos -- sess, with -- in the veil,\n"
 						+ "	A life of joy and peace.", amazingGrace);
 		stanzaDao.create(stanzaFive);
 		Stanza stanzaSix = new Stanza(6,
 				"	The earth shall soon dis -- solve like snow,\n"
 						+ "	The sun for -- bear to shine;\n"
 						+ "	But God, who called me here be -- low,\n"
 						+ "	Will be for -- e -- ver mine.", amazingGrace);
 		stanzaDao.create(stanzaSix);
 		Stanza stanzaSeven = new Stanza(7,
 				"	When we’ve been there ten thou -- sand years,\n"
 						+ "	Bright shin -- ing as the sun,\n"
 						+ "	We’ve no less days to sing God’s praise\n"
 						+ "	Than when we’d first be -- gun.", amazingGrace);
 		stanzaDao.create(stanzaSeven);
 
 		// Voice
 		TableUtils.dropTable(connectionSource, Voice.class, true);
 		TableUtils.createTable(connectionSource, Voice.class);
 		Voice soprano = new Voice(Voice.Part.SOPRANO, "	d'4 g'2 b'8 (g') b'2\n"
 				+ "	a'4 g'2 e'4 d'2 \\bar \"\" \\break\n"
 				+ "	d'4 g'2 b'8 (g') b'2 a'4 d''2.\n"
 				+ "	b'4 d''4. (b'8) d''8 (b') g'2 \\break\n"
 				+ "	d'4 e'4. (g'8) g'8 (e') d'2\n"
 				+ "	d'4 g'2 b'8 (g') b'2 a'4 g'2\n" + "	\\bar \"|.\"",
 				newBritain);
 		voiceDao.create(soprano);
 		Voice alto = new Voice(Voice.Part.ALTO, "	b4 b2 d'4 d'2\n"
 				+ "	c'4 b2 c'4 b2\n" + "	b4 b2 b4 d'2 d'4 d'2.\n"
 				+ "	d'4 d'2 d'4 d'2\n" + "	d'4 c'4. (d'8) c'4 b2\n"
 				+ "	d'4 b2 d'4 d'2 c'4 b2", newBritain);
 		voiceDao.create(alto);
 		Voice tenor = new Voice(Voice.Part.TENOR, "	g4 d2 g4 g2\n"
 				+ "	fis4 g2 g4 g2\n" + "	g4 d2 g4 g2 fis4 g2.\n"
 				+ "	g4 b4. (g8) b8 (g) g2\n" + "	g4 g2 e8 (g) g2\n"
 				+ "	g4 g2 g8 (b) g2 fis4 g2", newBritain);
 		voiceDao.create(tenor);
 		Voice bass = new Voice(Voice.Part.BASS, "	g,4 g,2 g,8 (b,) d2\n"
 				+ "	d4 e2 c4 g,2\n" + "	g,4 g,2 g,8 (b,) d2 c4 b,2.\n"
 				+ "	g,4 g2 g4 b,2\n" + "	b,4 c4. (b,8) c4 g,2\n"
 				+ "	b,4 e2 d4 d2 d4 g,2", newBritain);
 		voiceDao.create(bass);
 	}
 
 	public static void main(String[] args) throws SQLException {
 		DbResetTest dbt = new DbResetTest();
 		dbt.setupDb();
 		
 		dbt.meterDao.create(new Meter("10 12 10 12", "Uncommon Meter"));
 		List<Meter> meters = dbt.meterDao.queryForAll();
 		for (Meter m : meters)
 			System.out.println("Meter: " + m);
 
 		List<Text> texts = dbt.textDao.queryForAll();
 		for (Text t : texts) {
 			System.out.println("Text: " + t);
 			System.out.println("\tMeter: " + t.getMeter());
 			System.out.println("\tDefaultTune: "
 					+ H2Db.getInstance().selectDefaultTuneFor(t));
 			for (Stanza s : t.getStanzas())
 				System.out.println("Stanza " + s.getNumber() + ":\n"
 						+ s.getWords());
 		}
 
 		List<Tune> tunes = dbt.tuneDao.queryForAll();
 		for (Tune t : tunes) {
 			System.out.println("Tune: " + t);
 			System.out.println("\tMeter: " + t.getMeter());
 			System.out.println("\tDefaultText: "
 					+ H2Db.getInstance().selectDefaultTextFor(t));
 			for (Voice v : t.getVoices())
 				System.out.println("Voice " + v.getPart() + ":\n"
 						+ v.getNotes());
 		}
 	
 		dbt.connectionSource.close();
 	}
 }
