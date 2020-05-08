 package org.esco.indicators.backend.jdbc;
 
 import java.sql.Connection;
 import java.sql.ResultSet;
 import java.sql.SQLException;
 import java.sql.Statement;
 import java.text.ParseException;
 import java.text.SimpleDateFormat;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.HashSet;
 import java.util.Iterator;
 import java.util.List;
 import java.util.ListIterator;
 import java.util.Map;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.sql.DataSource;
 
 import org.apache.commons.dbutils.QueryRunner;
 import org.apache.commons.dbutils.ResultSetHandler;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.time.DateUtils;
 import org.apache.commons.logging.Log;
 import org.apache.commons.logging.LogFactory;
 import org.esco.indicators.backend.exception.TransactionException;
 import org.esco.indicators.backend.jdbc.handler.ACommeProfilInEtabRSHandler;
 import org.esco.indicators.backend.jdbc.handler.ACommeProfilRSHandler;
 import org.esco.indicators.backend.jdbc.handler.EstActiveeRSHandler;
 import org.esco.indicators.backend.jdbc.handler.EtablissementRSHandler;
 import org.esco.indicators.backend.jdbc.handler.SeConnecteMoisRSHandler;
 import org.esco.indicators.backend.jdbc.handler.SeConnecteSemaineRSHandler;
 import org.esco.indicators.backend.jdbc.model.ACommeProfil;
 import org.esco.indicators.backend.jdbc.model.ConnexionProfilPeriode;
 import org.esco.indicators.backend.jdbc.model.ConnexionServiceJour;
 import org.esco.indicators.backend.jdbc.model.EstActivee;
 import org.esco.indicators.backend.jdbc.model.Etablissement;
 import org.esco.indicators.backend.jdbc.model.NombreDeVisiteurs;
 import org.esco.indicators.backend.jdbc.model.SeConnectePeriode;
 import org.esco.indicators.backend.service.Config;
 import org.esco.indicators.backend.utils.DateHelper;
 import org.postgresql.ds.PGPoolingDataSource;
 import org.postgresql.ds.jdbc23.AbstractJdbc23PoolingDataSource;
 import org.springframework.util.Assert;
 import org.springframework.util.CollectionUtils;
 
 
 
 public class JDBC {
 
 	/** Logger. */
 	private static final Log LOGGER = LogFactory.getLog(JDBC.class);
 
 	/** 1er janvier 2000. */
 	private static final Date JANUARY_FIRST_2000 = java.sql.Date.valueOf("2000-01-01");
 
 	/** Date format for day of year. */
 	private static final SimpleDateFormat dayOfYearFormat = new SimpleDateFormat("yyyy-MM-dd");
 
 	/** Date format for month of year. */
 	private static final SimpleDateFormat monthOfYearFormat = new SimpleDateFormat("yyyy-MM");
 
 	/** Number pf rows in a JDBC batch query. */
 	private static final int JDBC_BATCH_ROW_COUNT = 500;
 
 	/** Shoud we use caching ? */
 	private static final boolean USE_CACHE = true;
 
 	/** Configuration. */
 	private static final Config CONFIG = Config.getInstance();
 
 	/** Clé de stockage dans la BD pour la derniere date de traitement LDAP. */
 	private static final String LAST_LDAP_PROCESSING_DATE_CONFIG_KEY = "LAST_LDAP_PROCESSING_DATE";
 
 	/** SQL DataSource. */
 	private final DataSource dataSource;
 
 	/** Apache query runner. */
 	private final QueryRunner run = new QueryRunner();
 
 	/** Dates which was already processed by a previous treatement. */
 	private final Map<Long, Boolean> alreadyProcessedDate = new HashMap<Long, Boolean>(32);
 
 	/** Etablissement cache. */
 	private Map<String, Etablissement> etablissementCache = new HashMap<String, Etablissement>(128);
 
 	public JDBC() throws ClassNotFoundException, SQLException {
 		this(JDBC.buildDataSource());
 	}
 
 	public JDBC(final DataSource dataSource) throws ClassNotFoundException, SQLException {
 		Assert.notNull(dataSource, "DataSource wasn't provided !");
 		this.dataSource = dataSource;
 	}
 
 	protected static DataSource buildDataSource() throws ClassNotFoundException, SQLException {
 		// Load Postgre JDBC driver
 		Class.forName("org.postgresql.Driver");
 
 		final String dbUrl = JDBC.CONFIG.getConfigValue(Config.CONF_DB_URL);
 		final String dbUser = JDBC.CONFIG.getConfigValue(Config.CONF_DB_USER);
 		final String dbPassword = JDBC.CONFIG.getConfigValue(Config.CONF_DB_PASSWORD);
 
 		final int ldapThreadCount = Integer.valueOf(JDBC.CONFIG.getConfigValue(Config.CONF_LDAP_THREAD_COUNT));
 
 		final Pattern dbUrlPattern = Pattern.compile(".*://([^:]*):([0-9]*)/([^/]*)");
 		final Matcher dbUrlMatcher = dbUrlPattern.matcher(dbUrl);
 
 		Assert.isTrue(dbUrlMatcher.matches(), "DB URL in config is erroneous !");
 		final String dbName = dbUrlMatcher.group(3);
 		final String dbServerName = dbUrlMatcher.group(1);
 		final String dbPortNumber = dbUrlMatcher.group(2);
 
 		JDBC.LOGGER.debug(String.format(
 				"JDBC datasource will Connect to database: [%s] with user: [%s].",
 				dbUrl, dbUser));
 
 		PGPoolingDataSource poolDs = AbstractJdbc23PoolingDataSource.getDataSource("dataSource");
 
 		if (poolDs == null) {
 			// Init Pool DS
 			poolDs = new PGPoolingDataSource();
 			poolDs.setDatabaseName(dbName);
 			poolDs.setServerName(dbServerName);
 			poolDs.setPortNumber(Integer.valueOf(dbPortNumber));
 			poolDs.setUser(dbUser);
 			poolDs.setPassword(dbPassword);
 
 			poolDs.setDataSourceName("dataSource");
 			poolDs.setInitialConnections((ldapThreadCount / 2) + 5);
 			poolDs.setMaxConnections((ldapThreadCount / 2) + 5);
 
 			poolDs.setLoginTimeout(10);
 			poolDs.setSocketTimeout(10);
 
 			poolDs.initialize();
 		}
 
 		return poolDs;
 	}
 
 	/**
 	 * Commit & close connection.
 	 * 
 	 * @throws SQLException
 	 */
 	public boolean commitTransaction(final Connection connection) throws SQLException {
 		Assert.state(!connection.isClosed(), "Connection should not be closed here !");
 
 		boolean commitOk = false;
 
 		try {
 			connection.commit();
 			commitOk = true;
 		} catch (SQLException e) {
 			JDBC.LOGGER.error("An error occured while commiting in DB ! Processing will be rolled back !", e);
 			JDBC.rollOutSqlException(JDBC.LOGGER, e);
 
 			connection.rollback();
 		} finally {
 			connection.close();
 		}
 
 		return commitOk;
 	}
 
 	// Début méthodes pour le traitement Mensuel et hebdomadaire
 
 	/**
 	 * Recherche tous les jour traités du mois.
 	 * 
 	 * @param mois le mois
 	 * @return une map contenant les jours traités du mois comme clé avec true comme value
 	 * @throws SQLException
 	 * @throws ParseException si mauvais format pour mois
 	 */
 	public Map<Integer, Boolean> jourDuMois(final Date mois) throws SQLException {
 		Assert.notNull(mois, "Variable mois must be specified !");
 
 		String formattedMois = JDBC.monthOfYearFormat.format(mois);
 
 		final Map<Integer, Boolean> result = new HashMap<Integer, Boolean>();
 
 		final String query = "select distinct to_char(jour, 'DD') as j from nombredevisiteurs where to_char(jour, 'YYYY-MM') = ?";
 		final Connection connection = this.getConnection();
 		try {
 			this.run.query(connection, query, new ResultSetHandler<Object>() {
 				@Override
 				public Object handle(final ResultSet rs) throws SQLException {
 					while (rs.next()) {
 						result.put(Integer.parseInt(rs.getString("j")), true);
 					}
 					rs.close();
 
 					return null;
 				}
 			}, formattedMois);
 		} finally {
 			connection.close();
 		}
 
 		return result;
 	}
 
 	/**
 	 * Recherche tous les jour traités de la semaine commençant par un jour spécifique.
 	 * on veut tous les jours présents entre Semaine et Semaine +7 jours
 	 * on récupère Semaine +7 jours avec le Calendar et avec la requete SQl
 	 * on peut récuperer les jours entre les deux dates
 	 * 
 	 * @param jourDebutSemaine le jour du début de la semaine
 	 * @return une map contenant les jours traités de la semaine comme clé avec true comme value
 	 * @throws ParseException si mauvais format pour jourDebutSemaine
 	 * @throws SQLException
 	 */
 	public Map<Integer, Boolean> jourDeLaSemaine(final Date jourDebutSemaine) throws SQLException {
 		Assert.notNull(jourDebutSemaine, "Variable jourDebutSemaine must be specified !");
 
 		final String formattedJourDebut = JDBC.dayOfYearFormat.format(jourDebutSemaine);
 
 		final Calendar cal = Calendar.getInstance();
 		cal.setTime(jourDebutSemaine);
 		cal.add(Calendar.DATE, 7);
 		final String jourDebutSemaineSuivante = JDBC.dayOfYearFormat.format(cal.getTime());
 
 		final Map<Integer, Boolean> result = new HashMap<Integer, Boolean>();
 
 		final String query = "select distinct to_char(jour, 'DD') as j from nombredevisiteurs " +
 				"where jour >= to_date(?, 'YYYY-MM-DD') and jour < to_date(?, 'YYYY-MM-DD')";
 		final Connection connection = this.getConnection();
 		try {
 			this.run.query(connection, query, new ResultSetHandler<Object>() {
 				@Override
 				public Object handle(final ResultSet rs) throws SQLException {
 					while (rs.next()) {
 						result.put(Integer.parseInt(rs.getString("j")), true);
 					}
 					rs.close();
 
 					return null;
 				}
 			}, formattedJourDebut, jourDebutSemaineSuivante);
 		} finally {
 			connection.close();
 		}
 
 		return result;
 	}
 
 	/**
 	 * Recherche la liste des lignes des personnes simple pour ce mois.
 	 * Les personnes ne s'etant connectés que sous un profil et un etablissement lors de ce mois.
 	 * 
 	 * @param date la date ?
 	 * @return la liste des objets SeConnecteMois (can be null)
 	 * @throws SQLException
 	 * @throws ParseException si mauvais format pour date
 	 */
 	public List<SeConnectePeriode> getPersonneSimpleCoMois(final Date date) throws SQLException {
 		Assert.notNull(date, "Variable date must be specified !");
 
 		List<SeConnectePeriode> result = null;
 		final String formattedDate = JDBC.dayOfYearFormat.format(date);
 
 		final String query = "select uid, uai, nomprofil, mois, nbconnexionmois, moyennemois " +
 				"from seconnectemois natural join " +
 				"(select uid from seconnectemois where mois = to_date(?, 'YYYY-MM-DD') " +
 				"group by uid having count(uid) = 1) as sousreq " +
 				"where mois = to_date(?, 'YYYY-MM-DD')";
 		final Connection connection = this.getConnection();
 		try {
 			result = this.run.query(connection, query
 					, new SeConnecteMoisRSHandler(), formattedDate, formattedDate);
 			connection.close();
 		} finally {
 			connection.close();
 		}
 
 		return result;
 	}
 
 	/**
 	 * Supprime de la base les données qui ont été compréssés pour un mois.
 	 * 
 	 * @param scpColl compressed datas
 	 * @return count of deleted rows
 	 * @throws SQLException
 	 * @throws TransactionException
 	 */
 	public int deleteAfterMonthCompression(final Collection<SeConnectePeriode> scpColl, final Connection connection) throws SQLException {
 		// Delete from seconnectemois table.
 		final String query = "delete from seconnectemois " +
 				"where uai = ? and nomprofil = ? and uid = ? and mois = to_date(?, 'YYYY-MM-DD')";
 
 		return this.deleteAfterCompression(scpColl, query, "SeConnecteMois", connection);
 	}
 
 	/**
 	 * Supprime de la base les données qui ont été compréssés pour une semaine.
 	 * 
 	 * @param scpColl compressed datas
 	 * @return count of deleted rows
 	 * @throws SQLException
 	 * @throws TransactionException
 	 */
 	public int deleteAfterWeekCompression(final Collection<SeConnectePeriode> scpColl, final Connection connection) throws SQLException {
 		// Delete from seconnectemois table.
 		final String query = "delete from seconnectesemaine " +
 				"where uai = ? and nomprofil = ? and uid = ? and premierjoursemaine = to_date(?, 'YYYY-MM-DD')";
 
 		return this.deleteAfterCompression(scpColl, query, "SeConnecteSemaine", connection);
 	}
 
 	/**
 	 * Delete from DB compressed datas.
 	 * 
 	 * @param scpColl the compressed datas to delete
 	 * @param query the SQL query to execute
 	 * @param tableName the tbale name for logging
 	 * @param connection
 	 * @return count of deleted rows
 	 * @throws SQLException
 	 */
 	protected int deleteAfterCompression(final Collection<SeConnectePeriode> scpColl,
 			final String query, final String tableName, final Connection connection) throws SQLException {
 		Assert.notNull(scpColl, "SeConnectePeriode collection must be specified !");
 
 		JDBC.LOGGER.info(String.format("Preparing deletion of %1$d %2$s objects in DB...", scpColl.size(), tableName));
 
 		int count = 0;
 
 		// Split the collection in smaller collections for batch insert.
 		Iterator<SeConnectePeriode> itScp = scpColl.iterator();
 		while (itScp.hasNext()) {
 			Object[][] batchValues = new Object[JDBC.JDBC_BATCH_ROW_COUNT][4];
 
 			int k;
 
 			// Split
 			for (k = 0; (k < JDBC.JDBC_BATCH_ROW_COUNT) && itScp.hasNext(); k++) {
 				final SeConnectePeriode scp = itScp.next();
 
 				final String formattedFirstPeriodDay = JDBC.dayOfYearFormat.format(scp.getPremierJourPeriode());
 
 				batchValues[k][0] = scp.getUai();
 				batchValues[k][1] = scp.getNomProfil();
 				batchValues[k][2] = scp.getUid();
 				batchValues[k][3] = formattedFirstPeriodDay;
 
 				if (JDBC.LOGGER.isTraceEnabled()) {
 					JDBC.LOGGER.trace(String.format("Preparing deletion for %1$s.", scp));
 				}
 			}
 
 			if (k < JDBC.JDBC_BATCH_ROW_COUNT) {
 				// The values array is not full. Resize it to correct size.
 				batchValues = (Object[][]) ArrayUtils.subarray(batchValues, 0, k);
 			}
 
 			if (JDBC.LOGGER.isTraceEnabled()) {
 				JDBC.LOGGER.trace(String.format("Deletion of %1$d %2$s rows in DB...", k, tableName));
 			}
 
 			// Batch delete
 			int[] rowsCount = this.run.batch(connection, query, batchValues);
 			for (int deleteCount : rowsCount) {
 				count = count + deleteCount;
 			}
 		}
 
 		JDBC.LOGGER.info(String.format("%1$d %2$s rows were deleted", count, tableName));
 
 		return count;
 	}
 
 
 	/**
 	 * Insert dans la base les données qui ont été compréssés pour un mois.
 	 * 
 	 * @param scpColl compressed datas
 	 * @return count of deleted rows
 	 * @throws SQLException
 	 * @throws TransactionException
 	 */
 	public int insertMonthCompression(final Collection<ConnexionProfilPeriode> cppColl, final Connection connection) throws SQLException {
 		// Insert in connexionprofilmois table.
 		final String query = "insert into connexionprofilmois (uai, nomprofil, mois, nbconnexion, nbpersonne, moyenneconnexion) " +
 				"values (?, ?, to_date(?, 'YYYY-MM-DD'), ?, ?, ?)";
 
 		return this.insertCompressionPeriods(cppColl, query, "ConnexionProfilMois", connection);
 	}
 
 	/**
 	 * Insert dans la les données qui ont été compréssés pour une semaine.
 	 * 
 	 * @param scpColl compressed datas
 	 * @return count of deleted rows
 	 * @throws SQLException
 	 * @throws TransactionException
 	 */
 	public int insertWeekCompression(final Collection<ConnexionProfilPeriode> cppColl, final Connection connection) throws SQLException {
 		// Insert in connexionprofilsemaine table.
 		final String query = "insert into connexionprofilsemaine (uai, nomprofil, semaine, nbconnexion, nbpersonne, moyenneconnexion) " +
 				"values (?, ?, to_date(?, 'YYYY-MM-DD'), ?, ?, ?)";
 
 		return this.insertCompressionPeriods(cppColl, query, "ConnexionProfilSemaine", connection);
 	}
 
 	/**
 	 * Insertion dans les tables de compression.
 	 * 
 	 * @param scpColl the compressed datas to delete
 	 * @param query the SQL query to execute
 	 * @param tableName the tbale name for logging
 	 * @return count of inserted rows
 	 * @throws SQLException
 	 */
 	protected int insertCompressionPeriods(final Collection<ConnexionProfilPeriode> cppColl,
 			final String query, final String tableName, final Connection connection) throws SQLException {
 		Assert.notNull(cppColl, "ConnexionProfilPeriode collection must be specified !");
 
 		JDBC.LOGGER.info(String.format("Preparing insertion of %1$d %2$s rows in DB...", cppColl.size(), tableName));
 
 		int count = 0;
 
 		// Split the collection in smaller collections for batch insert.
 		Iterator<ConnexionProfilPeriode> itCpp = cppColl.iterator();
 		while (itCpp.hasNext()) {
 			Object[][] batchValues = new Object[JDBC.JDBC_BATCH_ROW_COUNT][6];
 
 			int k;
 
 			// Split
 			for (k = 0; (k < JDBC.JDBC_BATCH_ROW_COUNT) && itCpp.hasNext(); k++) {
 				final ConnexionProfilPeriode cpp = itCpp.next();
 
 				final String formattedFirstPeriodDay = JDBC.dayOfYearFormat.format(cpp.getDebutPeriode());
 
 				batchValues[k][0] = cpp.getUai();
 				batchValues[k][1] = cpp.getNomProfil();
 				batchValues[k][2] = formattedFirstPeriodDay;
 				batchValues[k][3] = cpp.getNbConnexion();
 				batchValues[k][4] = cpp.getNbPersonne();
 				batchValues[k][5] = cpp.getMoyenneConnexion();
 
 				if (JDBC.LOGGER.isTraceEnabled()) {
 					JDBC.LOGGER.trace(String.format("Preparing insertion for %1$s.", cpp));
 				}
 			}
 
 			if (k < JDBC.JDBC_BATCH_ROW_COUNT) {
 				// The values array is not full. Resize it to correct size.
 				batchValues = (Object[][]) ArrayUtils.subarray(batchValues, 0, k);
 			}
 
 			if (JDBC.LOGGER.isTraceEnabled()) {
 				JDBC.LOGGER.trace(String.format("Insertion of %1$d %2$s rows in DB...", k, tableName));
 			}
 
 			// Batch insert
 			int[] rowsCount = this.run.batch(connection, query, batchValues);
 			for (int insertCount : rowsCount) {
 				count = count + insertCount;
 			}
 		}
 
 		JDBC.LOGGER.info(String.format("%1$d %2$s rows were inserted", count, tableName));
 
 		return count;
 	}
 
 	/**
 	 * Recherche la liste des lignes des personnes simple pour la semaine.
 	 * Les personnes ne s'etant connectés que sous un profil et un etablissement pour cette semaine.
 	 * 
 	 * @param date la date representant la semaine ?
 	 * @return liste des objets SeConnecteSemaine (can be null)
 	 * @throws ParseException
 	 * @throws SQLException
 	 */
 	public List<SeConnectePeriode> getPersonneSimpleCoSemaine(final Date date) throws SQLException {
 		Assert.notNull(date, "Variable date must be specified !");
 
 		List<SeConnectePeriode> result = null;
 
 		final String formattedDate = JDBC.dayOfYearFormat.format(date);
 
 		final String query = "select uid, uai, nomprofil, premierjoursemaine, nbconnexionsemaine, moyennesemaine " +
 				"from seconnectesemaine  natural join " +
 				"(select uid from seconnectesemaine where premierjoursemaine=to_date(?, 'YYYY-MM-DD') " +
 				"group by uid having count(uid) = 1 ) as sousreq " +
 				"where premierjoursemaine=to_date(?, 'YYYY-MM-DD')";
 		final Connection connection = this.getConnection();
 		try {
 			result = this.run.query(connection, query
 					, new SeConnecteSemaineRSHandler(), formattedDate, formattedDate);
 		} finally {
 			connection.close();
 		}
 
 		return result;
 	}
 
 	/**
 	 * Recherche toutes les mois comprésses.
 	 * 
 	 * @return un set contenant les dates des premiers jour de chaque mois compressé
 	 * @throws SQLException
 	 */
 	public Set<Date> findAllCompressedMonth() throws SQLException {
 		final Set<Date>  result = new HashSet<Date> (256);
 
 		final String query = "select distinct mois as m from connexionprofilmois order by m";
 		final Connection connection = this.getConnection();
 		try {
 			this.run.query(connection, query, new ResultSetHandler<Object>() {
 				@Override
 				public Object handle(final ResultSet rs) throws SQLException {
 					while (rs.next()) {
 						java.sql.Date date = rs.getDate("m");
 						result.add(date);
 					}
 					rs.close();
 
 					return null;
 				}
 			});
 		} finally {
 			connection.close();
 		}
 
 		return result;
 	}
 
 	/**
 	 * Recherche toutes les semaine comprésses.
 	 * 
 	 * @return un set contenant les dates des premiers jour de chaque semaine compressée
 	 * @throws SQLException
 	 */
 	public Set<Date> findAllCompressedWeek() throws SQLException {
 		final Set<Date>  result = new HashSet<Date> (1024);
 
 		final String query = "select distinct semaine as j from connexionprofilsemaine order by j";
 		final Connection connection = this.getConnection();
 		try {
 			this.run.query(connection, query, new ResultSetHandler<Object>() {
 				@Override
 				public Object handle(final ResultSet rs) throws SQLException {
 					while (rs.next()) {
 						java.sql.Date date = rs.getDate("j");
 						result.add(date);
 					}
 					rs.close();
 
 					return null;
 				}
 			});
 		} finally {
 			connection.close();
 		}
 
 		return result;
 	}
 
 	/**
 	 * Recherche la date la plus ancienne à laquelle des statistiques ont été calculé.
 	 * 
 	 * @return la date la plus ancienne
 	 * @throws SQLException
 	 */
 	public Date findOldestStatDate() throws SQLException {
 		Date result = null;
 
 		final String query = "select least(minSemaine, minMois) as oldest from " +
 				"(select min(s.premierjoursemaine) as minSemaine from seconnectesemaine s) as semaine, " +
 				"(select min(m.mois) as minMois from seconnectemois m) as mois";
 		final Connection connection = this.getConnection();
 		try {
 			result = this.run.query(connection, query, new ResultSetHandler<Date>() {
 				@Override
 				public Date handle(final ResultSet rs) throws SQLException {
 					Date result = null;
 					if (rs.next()) {
 						final Date oldest = rs.getDate("oldest");
 
 						if ((oldest != null)) {
 							result = oldest;
 						}
 					}
 					rs.close();
 
 					return result;
 				}
 			});
 		} finally {
 			connection.close();
 		}
 
 		if (result == null) {
 			result = JDBC.JANUARY_FIRST_2000;
 		}
 
 		return result;
 	}
 
 	// Fin méthodes pour le traitement Mensuel et hebdomadaire
 
 	// Debut méthodes pour le traitement Ldap
 
 	/**
 	 * Return the last EstActivee object from db for a specific uid.
 	 * 
 	 * @param uid
 	 * @param connection2
 	 * @return the EstActivee object (can be null)
 	 * @throws SQLException
 	 */
 	public EstActivee getLastActivationState(final String uid,
 			final Connection connection) throws SQLException {
 		Assert.hasText(uid, "Variable uid must be supplied !");
 
 		EstActivee result = null;
 
 		final String query = "select datedebutactivation, datefinactivation from est_activee " +
 				"where uid = ? and datedebutactivation = " +
 				"(select max(datedebutactivation) from est_activee where uid = ? )";
 
 		result = this.run.query(connection, query, new EstActiveeRSHandler(uid), uid, uid);
 
 		return result;
 	}
 
 	/**
 	 * Try to activate an account in the DB at a specific date.
 	 * 
 	 * @param uid account Id
 	 * @param date activation date
 	 * @param connection
 	 * @return true if the account was activated
 	 * @throws SQLException
 	 * @throws TransactionException
 	 * @deprecated use updateAccountsActivation()
 	 */
 	@Deprecated
 	public void activateAccount(final String uid, final Date date, final Connection connection) throws SQLException, TransactionException {
 		Assert.hasText(uid, "Variable uid must be specified !");
 		Assert.notNull(date, "Variable dateDeb must be specified !");
 
 		final String formatedDate = JDBC.dayOfYearFormat.format(date);
 
 		final String query = "insert into est_activee (uid, datedebutactivation) values (?, to_date(?, 'YYYY-MM-DD'))";
 
 		int count = this.run.update(connection, query, uid, formatedDate);
 
 		if (count != 1) {
 			throw new TransactionException(String.format(
 					"Unable to activate account for uid: [%s] and activation date: [%s]", uid, formatedDate));
 		}
 	}
 
 	/**
 	 * Try to disactivate an account in the DB at a specific date.
 	 * 
 	 * @param uid account Id
 	 * @param date disactivation date
 	 * @return true if the account was activated
 	 * @throws SQLException
 	 * @throws TransactionException
 	 * @deprecated use updateAccountsActivation()
 	 */
 	@Deprecated
 	public void disactivateAccount(final EstActivee activationState, final Date date, final Connection connection) throws SQLException, TransactionException {
 		Assert.notNull(activationState, "Object EstActivee must be specified !");
 		final String uid = activationState.getUid();
 		Assert.hasText(activationState.getUid(), "Field EstActivee.uid must be specified !");
 		Assert.notNull(activationState.getDateDebutActivation(),
 				"Field EstActivee.dateDebutActivation must be specified !");
 		Assert.state(activationState.isActive(),
 				"Object EstActivee specified must be active !");
 		Assert.notNull(date, "Variable dateDeb must be specified !");
 
 		final String formatedDateDebut = JDBC.dayOfYearFormat.format(activationState.getDateDebutActivation());
 		final String formatedDateFin = JDBC.dayOfYearFormat.format(date);
 
 		final String query = "update est_activee set datefinactivation = to_date(?, 'YYYY-MM-DD') " +
 				"where uid = ? and datedebutactivation = to_date(?, 'YYYY-MM-DD')";
 
 		int count = this.run.update(connection, query,
 				formatedDateFin, uid, formatedDateDebut);
 
 		if (count != 1) {
 			throw new TransactionException(String.format(
 					"Unable to disactivate account for uid: [%s] ; activation date: [%s] and disactivation date: [%s]",
 					uid, formatedDateDebut, formatedDateFin));
 		}
 	}
 
 	/**
 	 * Try to disactivate an account in the DB at a specific date.
 	 * 
 	 * @param uid account Id
 	 * @param date disactivation date
 	 * @return true if the account was activated
 	 * @throws SQLException
 	 * @throws TransactionException
 	 */
 	public void updateAccountsActivation(final Collection<String> uidColl, final Date updateDate,
 			final Connection connection) throws SQLException, TransactionException {
 		Assert.notNull(uidColl, "Collection uid must be specified !");
 		Assert.notNull(updateDate, "Update account activation date must be specified !");
 
 		final String formatedUpdateDate = JDBC.dayOfYearFormat.format(updateDate);
 
 		final String disactivateQuery = "update est_activee set dateFinActivation = to_date(?, 'YYYY-MM-DD') " +
 				"where uid = ? and datefinactivation is null";
 
 		final String activateQuery = "insert into est_activee (datedebutactivation, uid) values (to_date(?, 'YYYY-MM-DD'), ?)";
 
 		int updatedRowsCount = 0;
 		int insertedRowsCount = 0;
 
 		// Split the collection in smaller collections for batch insert.
 		Iterator<String> itUid = uidColl.iterator();
 		while (itUid.hasNext()) {
 			Object[][] batchValues = new Object[JDBC.JDBC_BATCH_ROW_COUNT][2];
 
 			int k;
 
 			// Split
 			for (k = 0; (k < JDBC.JDBC_BATCH_ROW_COUNT) && itUid.hasNext(); k++) {
 				String uid = itUid.next();
 
 				batchValues[k][0] = formatedUpdateDate;
 				batchValues[k][1] = uid;
 
 				if (JDBC.LOGGER.isTraceEnabled()) {
 					JDBC.LOGGER.trace(String.format(
 							"Preparing insertion or update for est_activee row with UID: [%1$s].", uid));
 				}
 			}
 
 			if (k < JDBC.JDBC_BATCH_ROW_COUNT) {
 				// The values array is not full. Resize it to correct size.
 				batchValues = (Object[][]) ArrayUtils.subarray(batchValues, 0, k);
 			}
 
 			if (JDBC.LOGGER.isTraceEnabled()) {
 				JDBC.LOGGER.trace(String.format("Insertion or update of %1$d EstActivee rows in DB...", k));
 			}
 
 			final int[] updatedRows = this.run.batch(connection, disactivateQuery, batchValues);
 
 			// All the rows we could not update must be inserted
 			Object[][] valuesToInsert = new Object[JDBC.JDBC_BATCH_ROW_COUNT][2];
 			int i = 0;
 			for (k = 0; k < updatedRows.length; k++) {
 				if (updatedRows[k] == 0) {
 					// No rows updated, we need to insert it
 					valuesToInsert[i] = batchValues[k];
 					i++;
 				}
 			}
 
 			if (i < JDBC.JDBC_BATCH_ROW_COUNT) {
 				// The values array is not full. Resize it to correct size.
 				valuesToInsert = (Object[][]) ArrayUtils.subarray(valuesToInsert, 0, i);
 			}
 
 			final int[] insertedRows = this.run.batch(connection, activateQuery, valuesToInsert);
 
 			insertedRowsCount = insertedRowsCount + insertedRows.length;
 			updatedRowsCount = updatedRowsCount + (updatedRows.length - insertedRows.length);
 
 			if (JDBC.LOGGER.isTraceEnabled()) {
 				JDBC.LOGGER.trace(String.format("[%1$d] Accounts were activated and [%2$d] Accounts were disactivated."
 						, insertedRows.length, updatedRows.length - insertedRows.length));
 			}
 		}
 
 		if (JDBC.LOGGER.isDebugEnabled()) {
 			JDBC.LOGGER.debug(String.format("[%1$d] Accounts were activated and [%2$d] Accounts were disactivated."
 					, insertedRowsCount, updatedRowsCount));
 		}
 	}
 
 	/**
 	 * Update le profil.
 	 * => fin de profil, on change la date de fin de null à la date du traitement.
 	 * 
 	 * @param aCommeProfil
 	 * @param dateFinProfil
 	 * @throws SQLException
 	 * @throws TransactionException
 	 * @throws ParseException
 	 */
 	public int updateDateFinProfil(final ACommeProfil acp, final Date dateFinProfil, final Connection connection) throws SQLException, TransactionException {
 		Assert.notNull(acp, "Object ACommeProfil must be supplied !");
 		Assert.notNull(acp.getDateDebutProfil(), "Field ACommeProfil.dateDebutProfil must be supplied !");
 		Assert.notNull(dateFinProfil, "Variable dateFinProfil must be specified !");
 
 		final String formattedDateDebutProfil = JDBC.dayOfYearFormat.format(acp.getDateDebutProfil());
 		final String formattedDateFinProfil = JDBC.dayOfYearFormat.format(dateFinProfil);
 
 		final String query = "update  acommeprofil set datefinprofil = to_date(? , 'YYYY-MM-DD') " +
 				"where uid = ? and uai = ? and nomprofil = ? and datedebutprofil = to_date(?, 'YYYY-MM-DD')";
 		final int count = this.run.update(connection, query, formattedDateFinProfil,
 				acp.getUid(), acp.getNomProfil(), formattedDateDebutProfil);
 
 		if (count != 1) {
 			throw new TransactionException(String.format("ACommeProfil row: [%s] wasn't updated with dateFinProfil: [%s] !", acp, formattedDateFinProfil));
 		}
 
 		return count;
 	}
 
 	/**
 	 * Insert plusieurs profils.
 	 * => Date de fin vide et depart aujourd'hui
 	 * 
 	 * @param acp
 	 * @return
 	 * @throws SQLException
 	 * @throws TransactionException
 	 */
 	public int insertProfils(final Collection<ACommeProfil> acpColl, final Date insertDate, final Connection connection) throws SQLException, TransactionException {
 		Assert.notNull(acpColl, "Collection ACommeProfil must be supplied !");
 		Assert.notNull(insertDate, "Insertion date must be supplied !");
 
 		JDBC.LOGGER.debug(String.format("Preparing insertion of %1$d ACommeProfil rows in DB...", acpColl.size()));
 
 		final String dateDebutProfil = JDBC.dayOfYearFormat.format(insertDate);
 
 		final String query = "insert into  acommeprofil (uid, uai, nomprofil, datedebutprofil) " +
 				"values (?, ?, ?, to_date(?, 'YYYY-MM-DD'))";
 
 		int count = 0;
 
 		// Split the collection in smaller collections for batch insert.
 		Iterator<ACommeProfil> itAcp = acpColl.iterator();
 		while (itAcp.hasNext()) {
 			Object[][] batchValues = new Object[JDBC.JDBC_BATCH_ROW_COUNT][4];
 
 			int k;
 
 			// Split
 			for (k = 0; (k < JDBC.JDBC_BATCH_ROW_COUNT) && itAcp.hasNext(); k++) {
 				ACommeProfil acp = itAcp.next();
 
 				batchValues[k][0] = acp.getUid();
 				batchValues[k][1] = acp.getUai();
 				batchValues[k][2] = acp.getNomProfil();
 				batchValues[k][3] = dateDebutProfil;
 
 				if (JDBC.LOGGER.isTraceEnabled()) {
 					JDBC.LOGGER.trace(String.format("Preparing insertion for %1$s.", acp));
 				}
 			}
 
 			if (k < JDBC.JDBC_BATCH_ROW_COUNT) {
 				// The values array is not full. Resize it to correct size.
 				batchValues = (Object[][]) ArrayUtils.subarray(batchValues, 0, k);
 			}
 
 			if (JDBC.LOGGER.isTraceEnabled()) {
 				JDBC.LOGGER.trace(String.format("Insertion of %1$d ACommeProfil rows in DB...", k));
 			}
 
 			// Batch insert
 			int[] rowsCount = this.run.batch(connection, query, batchValues);
 			for (int rowsInserted : rowsCount) {
 				count += rowsInserted;
 			}
 
 		}
 
 		JDBC.LOGGER.debug(String.format("%1$d ACommeProfil rows were inserted", count));
 
 		return count;
 	}
 
 	/**
 	 * Désactive plusieurs profils.
 	 * => Date de fin aujourd'hui
 	 * 
 	 * @param acp
 	 * @return
 	 * @throws SQLException
 	 * @throws TransactionException
 	 */
 	public int deleteProfils(final Collection<ACommeProfil> acpColl, final Date deleteDate, final Connection connection) throws SQLException, TransactionException {
 		Assert.notNull(acpColl, "Collection ACommeProfil must be supplied !");
 		Assert.notNull(deleteDate, "Deletion date must be supplied !");
 
 		JDBC.LOGGER.debug(String.format("Preparing update of %1$d ACommeProfil rows in DB...", acpColl.size()));
 
 		final String dateFinProfil = JDBC.dayOfYearFormat.format(deleteDate);
 
 		final String query = "update  acommeprofil set datefinprofil = to_date(? , 'YYYY-MM-DD') " +
 				"where uid = ? and uai = ? and nomprofil = ? and datedebutprofil = to_date(?, 'YYYY-MM-DD')";
 
 		int count = 0;
 
 		// Split the collection in smaller collections for batch insert.
 		Iterator<ACommeProfil> itAcp = acpColl.iterator();
 		while (itAcp.hasNext()) {
 			Object[][] batchValues = new Object[JDBC.JDBC_BATCH_ROW_COUNT][5];
 
 			int k;
 
 			// Split
 			for (k = 0; (k < JDBC.JDBC_BATCH_ROW_COUNT) && itAcp.hasNext(); k++) {
 				ACommeProfil acp = itAcp.next();
 
 				final Date dateDebutProfil = acp.getDateDebutProfil();
 				Assert.notNull(dateDebutProfil , "Deletion date must be supplied !");
 
 				batchValues[k][0] = dateFinProfil;
 				batchValues[k][1] = acp.getUid();
 				batchValues[k][2] = acp.getUai();
 				batchValues[k][3] = acp.getNomProfil();
 				batchValues[k][4] = dateDebutProfil;
 
 				if (JDBC.LOGGER.isTraceEnabled()) {
 					JDBC.LOGGER.trace(String.format("Preparing update for %1$s.", acp));
 				}
 			}
 
 			if (k < JDBC.JDBC_BATCH_ROW_COUNT) {
 				// The values array is not full. Resize it to correct size.
 				batchValues = (Object[][]) ArrayUtils.subarray(batchValues, 0, k);
 			}
 
 			if (JDBC.LOGGER.isTraceEnabled()) {
 				JDBC.LOGGER.trace(String.format("update of %1$d ACommeProfil rows in DB...", k));
 			}
 
 			// Batch insert
 			int[] rowsCount = this.run.batch(connection, query, batchValues);
 			for (int rowsInserted : rowsCount) {
 				count += rowsInserted;
 			}
 		}
 
 		JDBC.LOGGER.debug(String.format("%1$d ACommeProfil rows were updated", count));
 
 		return count;
 	}
 
 	/**
 	 * Recherche la liste des profils (uid, uai ,nomprofil et datedebutprofil) d'une personne.
 	 * 
 	 * @param uid
 	 * @param connection
 	 * @return Set of ACommeProfil objects (can be null)
 	 * @throws SQLException
 	 */
 	public Set<ACommeProfil> getProfils(final String uid,
 			final Connection connection) throws SQLException {
 		Assert.hasText(uid, "Variable uid must be specified !");
 
 		Set<ACommeProfil> result = null;
 
 		final String query = "select uid, uai, nomprofil, datedebutprofil, datefinprofil from acommeprofil " +
 				"where uid = ? and datefinprofil is null";
 		result = this.run.query(connection, query, new ACommeProfilRSHandler(), uid);
 
 		return result;
 	}
 
 	/**
 	 * Recherche la liste des profils (uid, uai ,nomprofil et datedebutprofil)
 	 * de toutes les personnes dans un etablissements.
 	 * 
 	 * @param uai
 	 * @param connection
 	 * @return Set of ACommeProfil objects (can be null)
 	 * @throws SQLException
 	 */
 	public Map<String, Set<ACommeProfil>> getAllProfils() throws SQLException {
 		Map<String, Set<ACommeProfil>> result = null;
 
 		final String query = "select uid, uai, nomprofil, datedebutprofil, datefinprofil from acommeprofil " +
 				"where datefinprofil is null";
 		result = this.run.query(this.getConnection(), query, new ACommeProfilInEtabRSHandler());
 
 		return result;
 	}
 
 	/**
 	 * Insert or update an etablissement.
 	 * 
 	 * @param uai
 	 * @param departement
 	 * @param typeEtab
 	 * @return
 	 * @throws SQLException
 	 * @throws TransactionException
 	 */
 	public int insertOrUpdateEtablissement(final Etablissement etab, final Connection connection) throws SQLException, TransactionException {
 		Assert.notNull(etab, "Object Etablissement must be supplied !");
 		final String uai = etab.getUai();
 		Assert.hasText(uai, "Field Etablissement.uai must be specified !");
 
 		String countQuery = "select count(uai) from etablissement where uai = ?";
 
 		final int etabCount = this.run.query(connection, countQuery, new ResultSetHandler<Integer>() {
 			@Override
 			public Integer handle(final ResultSet rs) throws SQLException {
 				rs.next();
 				return rs.getInt(1);
 			}
 		}, uai);
 
 		final String query;
 		if (etabCount == 0) {
 			// Aucun établissement donc insertion
 			query = "insert into etablissement (departement, typeetablissement, siren, uai) " +
 					"values(?, ?, ?, ?)";
 		} else if (etabCount == 1){
 			// 1 établissement existe déjà donc update
 			query = "update etablissement set departement = ?, typeetablissement = ? , siren = ?" +
 					"where uai = ?";
 		} else {
 			// Impossible because uai is the unique Id !
 			throw new IllegalStateException("Impossible to have multiple etablissement rows with same uai !");
 		}
 
 		final int count = this.run.update(connection, query,
 				etab.getDepartement(), etab.getTypeEtablissement(), etab.getSiren(), uai);
 
 		if (count != 1) {
 			throw new TransactionException(String.format("Etablissement row: [%s] wasn't inserted nor updated !", etab));
 		}
 
 		return count;
 	}
 
 	// Fin méthodes pour le traitement Ldap
 
 	/**
 	 * Recherche le jour du dernier traitement des logs effectué.
 	 * Si c'est le premier, retourne le 1er janvier 2000 comme départ.
 	 * 
 	 * @return
 	 * @throws SQLException
 	 */
 	public Date dernierJourFait() {
 		Date bdLastDay = null;
 		try {
 			final String query = "select max(jour) as dernierjour from nombredevisiteurs";
 			final Connection connection = this.getConnection();
 			try {
 				bdLastDay = this.run.query(connection, query, new ResultSetHandler<Date>() {
 					@Override
 					public Date handle(final ResultSet rs) throws SQLException {
 						Date result = null;
 
 						if (rs.next()) {
 							result = rs.getDate(1);
 						}
 
 						return result;
 					}
 				});
 			} finally {
 				connection.close();
 			}
 		} catch (SQLException e) {
 			// Unable to retrieve last processing day in BD.
 			JDBC.LOGGER.error("Unable to retrieve last log Lecture day in BD", e);
 			JDBC.rollOutSqlException(JDBC.LOGGER, e);
 		}
 
 		final Date dernierJourFait;
 		if (bdLastDay == null) {
 			// Pas de dernier jour en BD : Premier traitement
 			dernierJourFait = JDBC.JANUARY_FIRST_2000;
 		} else {
 			// Récupération du dernier jour en BD
 			dernierJourFait = bdLastDay;
 		}
 
 		return dernierJourFait;
 	}
 
 	/**
 	 * Recherche le jour passé en paramètre à déjà été traité.
 	 * 
 	 * @param day
 	 * @return true si le jour à déjà été traité
 	 */
 	public boolean isDayAlreadyProcessed(final Date day) {
 		Assert.notNull(day, "Day must be supplied !");
 		Boolean test = null;
 
 
 		if (JDBC.USE_CACHE) {
 			test = this.alreadyProcessedDate.get(day.getTime());
 		}
 
 		if (test == null) {
 			try {
 				final String formatedDay = JDBC.dayOfYearFormat.format(day);
 				final String query = "select distinct(jour) as processedJour from nombredevisiteurs " +
 						"where jour = to_date(? , 'YYYY-MM-DD')";
 
 				final Connection connection = this.getConnection();
 				try {
 					test = this.run.query(connection, query, new ResultSetHandler<Boolean>() {
 						@Override
 						public Boolean handle(final ResultSet rs) throws SQLException {
 							final Boolean result = rs.next();
 
 							return result;
 						}
 					}, formatedDay);
 				} finally {
 					connection.close();
 				}
 
 				if (JDBC.USE_CACHE) {
 					this.alreadyProcessedDate.put(day.getTime(), test);
 				}
 			} catch (SQLException e) {
 				test = false;
 				// Unable to retrieve last processing day in BD.
 				JDBC.LOGGER.error("Unable to test if a log date was already processed !", e);
 				JDBC.rollOutSqlException(JDBC.LOGGER, e);
 			}
 		}
 
 		return test;
 	}
 
 	/**
 	 * Insert a collection of object NombreDeVisiteurs in DB.
 	 * 
 	 * @param ndvColl NombreDeVisiteurs objects collection
 	 * @throws SQLException
 	 */
 	public int insertConnexionsEtab(final Collection<NombreDeVisiteurs> ndvColl, final Connection connection) throws SQLException {
 		Assert.notNull(ndvColl, "Collection of NombreDeVisiteurs must be supplied !");
 
 		JDBC.LOGGER.info(String.format("Preparing insertion of %1$d NombreDeVisiteurs rows in DB...", ndvColl.size()));
 
 		final String query = "insert into NombreDeVisiteurs (jour, uai, nbvisites, nbvisiteurs, typeetab, typestat) " +
 				"values (to_date(?, 'YYYY-MM-DD'), ?, ?, ?, ?, ?)";
 
 		int count = 0;
 
 		// Split the collection in smaller collections for batch insert.
 		Iterator<NombreDeVisiteurs> itNdv = ndvColl.iterator();
 		while (itNdv.hasNext()) {
 			Object[][] batchValues = new Object[JDBC.JDBC_BATCH_ROW_COUNT][6];
 
 			int k;
 
 			// Split
 			for (k = 0; (k < JDBC.JDBC_BATCH_ROW_COUNT) && itNdv.hasNext(); k++) {
 				NombreDeVisiteurs ndv = itNdv.next();
 
 				Assert.notNull(ndv.getJour(), "Field NombreDeVisiteurs.jour must be supplied !");
 				final String jour = JDBC.dayOfYearFormat.format(ndv.getJour());
 
 				batchValues[k][0] = jour;
 				batchValues[k][1] = ndv.getUai();
 				batchValues[k][2] = ndv.getNbVisites();
 				batchValues[k][3] = ndv.getNbVisiteurs();
 				batchValues[k][4] = ndv.getTypeEtab();
 				batchValues[k][5] = ndv.getTypeStat();
 
 				if (JDBC.LOGGER.isTraceEnabled()) {
 					JDBC.LOGGER.trace(String.format("Preparing insertion for %1$s.", ndv));
 				}
 			}
 
 			if (k < JDBC.JDBC_BATCH_ROW_COUNT) {
 				// The values array is not full. Resize it to correct size.
 				batchValues = (Object[][]) ArrayUtils.subarray(batchValues, 0, k);
 			}
 
 			if (JDBC.LOGGER.isTraceEnabled()) {
 				JDBC.LOGGER.trace(String.format("Insertion of %1$d NombreDeVisiteurs rows in DB...", k));
 			}
 
 			// Batch insert
 			int[] rowsCount = this.run.batch(connection, query, batchValues);
 			for (int rowsInserted : rowsCount) {
 				count += rowsInserted;
 			}
 		}
 
 		JDBC.LOGGER.info(String.format("%1$d NombreDeVisiteurs rows were inserted", count));
 
 		return count;
 	}
 
 	/**
 	 * Insertion dans la table connexionservicejour de plusieurs tuples.
 	 * 
 	 * @param csjColl collection d'objets ConnexionServiceJour
 	 * @throws SQLException
 	 */
 	public int insertServices(final Collection<ConnexionServiceJour> csjColl, final Connection connection) throws SQLException {
 		Assert.notNull(csjColl, "Collection of ConnexionServiceJour must be supplied !");
 
 		JDBC.LOGGER.info(String.format("Preparing insertion of %1$d connexionServiceJour rows in DB...", csjColl.size()));
 
 		final String query = "insert into connexionservicejour (uid, nomprofil, uai, nomservice, truncatedfname, jour, nbconnexionservice) " +
 				"values (?, ?, ?, ?, ?, to_date(?, 'YYYY-MM-DD'), ?)";
 
 		int count = 0;
 
 		// Split the collection in smaller collections for batch insert.
 		Iterator<ConnexionServiceJour> itCsj = csjColl.iterator();
 		while (itCsj.hasNext()) {
 			Object[][] batchValues = new Object[JDBC.JDBC_BATCH_ROW_COUNT][7];
 
 			int k;
 
 			// Split
 			for (k = 0; (k < JDBC.JDBC_BATCH_ROW_COUNT) && itCsj.hasNext(); k++) {
 				ConnexionServiceJour csj = itCsj.next();
 
 				Assert.notNull(csj.getJour(), "Field ConnexionServiceJour.jour must be supplied !");
 				final String jour = JDBC.dayOfYearFormat.format(csj.getJour());
 
 				batchValues[k][0] = csj.getUid();
 				batchValues[k][1] = csj.getNomProfil();
 				batchValues[k][2] = csj.getUai();
 				batchValues[k][3] = csj.getNomService();
 				batchValues[k][4] = csj.getTruncatedFname();
 				batchValues[k][5] = jour;
 				batchValues[k][6] = csj.getNbConnexionService();
 
 				if (JDBC.LOGGER.isTraceEnabled()) {
 					JDBC.LOGGER.trace(String.format("Preparing insertion for %1$s.", csj));
 				}
 			}
 
 			if (k < JDBC.JDBC_BATCH_ROW_COUNT) {
 				// The values array is not full. Resize it to correct size.
 				batchValues = (Object[][]) ArrayUtils.subarray(batchValues, 0, k);
 			}
 
 			if (JDBC.LOGGER.isTraceEnabled()) {
 				JDBC.LOGGER.trace(String.format("Insertion of %1$d connexionServiceJour rows in DB...", k));
 			}
 
 			// Batch insert
 			int[] rowsCount = this.run.batch(connection, query, batchValues);
 			for (int rowsInserted : rowsCount) {
 				count += rowsInserted;
 			}
 		}
 
 		JDBC.LOGGER.info(String.format("%1$d connexionServiceJour rows were inserted", count));
 
 		return count;
 	}
 
 	/**
 	 * Recherche le departement de l'etablissement passé en parametre.
 	 * 
 	 * @param uai
 	 * @return la string representant le departement de l'etablissement
 	 * @throws SQLException
 	 */
 	public Etablissement getEtablissement(final String uai) throws SQLException {
 		Assert.hasText(uai, "Variable uai must be specified !");
 
 		Etablissement etab;
 
 		if (JDBC.USE_CACHE) {
 			etab = this.etablissementCache.get(uai);
 		}
 
 		if (etab == null) {
 			final String query = "select uai, siren, departement, typeetablissement from etablissement where uai = ?";
 			final Connection connection = this.getConnection();
 			try {
 				Iterator<Etablissement> itEtab =
 						this.run.query(connection, query, new EtablissementRSHandler(), uai).iterator();
 
 				if (itEtab.hasNext()) {
 					etab = itEtab.next();
 				}
 			} finally {
 				connection.close();
 			}
 
 			if (JDBC.USE_CACHE) {
 				this.etablissementCache.put(uai, etab);
 			}
 		}
 
 		return etab;
 	}
 
 	/**
 	 * Synchronize la BD avec LDAP pour désactiver les utilisateurs qui ne sont plus dans LDAP.
 	 * 
 	 * @param ldapUids la totalité des uids trouvés dans LDAP
 	 * @param deactivateDate
 	 * @return le nombre de compte désactivé dans la BD
 	 * @throws SQLException
 	 */
 	public int synchronizeDeletedLdapUser(final Collection<String> ldapUids, final Date deactivateDate)
 			throws SQLException {
 		Assert.notNull(deactivateDate, "Update account activation date must be specified !");
 
 		JDBC.LOGGER.info("Synchronization between LDAP and DB for LDAP deleted accounts...");
 
 		final String formatedDeactivateDate = JDBC.dayOfYearFormat.format(deactivateDate);
 
 		int deactivateAcountsCount = 0;
 		if (!CollectionUtils.isEmpty(ldapUids)) {
 			final Connection connection = this.getConnection();
 			final String tempTableName = this.buildLdapUidsTempTable(connection, ldapUids);
 
 			// Deactivate all user deleted from LDAP.
 			final String queryAccount = "update est_activee ea " +
 					"set datefinactivation = to_date(?, 'YYYY-MM-DD') " +
 					"where ea.datefinactivation is null and ea.uid not in " +
 					"(select t.uid from " + tempTableName + " t where t.uid = ea.uid);";
 
 			deactivateAcountsCount = this.run.update(connection, queryAccount, formatedDeactivateDate);
 			JDBC.LOGGER.debug(String.format("Deactivated Accounts count: [%1$d]", deactivateAcountsCount));
 
 			int deactivateProfilsCount = 0;
 
 			// Deactivate all user deleted from LDAP.
 			final String queryProfil = "update acommeprofil acp " +
 					"set datefinprofil = to_date(?, 'YYYY-MM-DD') " +
 					"where acp.datefinprofil is null and acp.uid not in " +
 					"(select t.uid from " + tempTableName + " t where t.uid = acp.uid);";
 
 			deactivateProfilsCount = this.run.update(connection, queryProfil, formatedDeactivateDate);
 			JDBC.LOGGER.debug(String.format("Deactivated Profils count: [%1$d]", deactivateProfilsCount));
 
			this.commitTransaction(connection);

 			JDBC.LOGGER.info(String.format(
 					"LDAP Sync : deactivated Accounts count: [%1$d] ; deactivated Profils count: [%2$d] !",
 					deactivateAcountsCount, deactivateProfilsCount));
 		}
 
 		return deactivateAcountsCount;
 	}
 
 	/**
 	 * Build a temporary table of all LDAP uids found.
 	 * 
 	 * @param connection the connection to DB
 	 * @param ldapProfils all the profils found in LDAP
 	 * @return the name of the temporary table
 	 * @throws SQLException
 	 */
 	protected String buildLdapUidsTempTable(final Connection connection, final Collection<String> ldapUids)
 			throws SQLException {
 		final String tempTableName = "ldap_uids_temp";
 
 		// Build temp table
 		final String createQuery = "create temporary table " + tempTableName +
 				" (uid varchar(32) NOT NULL, PRIMARY KEY (uid)) on commit drop;";
 		this.run.update(connection, createQuery);
 
 		// Insert uids in temp table
 		int count = 0;
 		final String insertQuery = "insert into " + tempTableName +
 				" (uid) values (?)";
 
 		// Split the collection in smaller collections for batch insert.
 		Iterator<String> itUids = ldapUids.iterator();
 		while (itUids.hasNext()) {
 			Object[][] batchValues = new Object[JDBC.JDBC_BATCH_ROW_COUNT][1];
 
 			int k;
 
 			// Split
 			for (k = 0; (k < JDBC.JDBC_BATCH_ROW_COUNT) && itUids.hasNext(); k++) {
 				String uid = itUids.next();
 
 				batchValues[k][0] = uid;
 
 				if (JDBC.LOGGER.isTraceEnabled()) {
 					JDBC.LOGGER.trace(String.format("Preparing insertion for %1$s.", uid));
 				}
 			}
 
 			if (k < JDBC.JDBC_BATCH_ROW_COUNT) {
 				// The values array is not full. Resize it to correct size.
 				batchValues = (Object[][]) ArrayUtils.subarray(batchValues, 0, k);
 			}
 
 			if (JDBC.LOGGER.isTraceEnabled()) {
 				JDBC.LOGGER.trace(String.format("Insertion of %1$d uids rows in temp table...", k));
 			}
 
 			// Batch insert
 			int[] rowsCount = this.run.batch(connection, insertQuery, batchValues);
 			for (int rowsInserted : rowsCount) {
 				count += rowsInserted;
 			}
 		}
 
 		// Analyze temp table
 		final String analyzeQuery = "analyze " + tempTableName + ";";
 		this.run.update(connection, analyzeQuery);
 
 		JDBC.LOGGER.info(String.format("%1$d uids were inserted in temp table.", count));
 
 		return tempTableName;
 	}
 
 	/**
 	 * Recherche tous les etablissements.
 	 * 
 	 * @return la liste des etablissements (can be null)
 	 * @throws SQLException
 	 */
 	public List<Etablissement> getAllEtablissements() throws SQLException {
 		List<Etablissement> etabs = null;
 
 		final String query = "select uai, siren, departement, typeetablissement from etablissement";
 		final Connection connection = this.getConnection();
 		try {
 			etabs = this.run.query(connection, query, new EtablissementRSHandler());
 		} finally {
 			connection.close();
 		}
 
 		return etabs;
 	}
 
 	/**
 	 * Insert ou update plusieurs rows dans les tables seconnectemois et seconnectesemaine.
 	 * 
 	 * @param scp les nouvelles donnees à prendre en compte.
 	 * @param parSemaine le set de type d'etablissement qui nécéssite un traitement par semaine
 	 * @throws SQLException
 	 * @throws ParseException
 	 */
 	public void updateConnexionsMoisVoirSemaine(final List<SeConnectePeriode> scpList,
 			final Set<String> parSemaine, final Connection connection) throws SQLException {
 		Assert.notNull(scpList, "SeConnectePeriode collection must be supplied !");
 		Assert.notNull(parSemaine, "Set parSemaine must be supplied !");
 
 		JDBC.LOGGER.info(String.format("Preparing insertion or update of %1$d SeConnecteMois and SeConnecteSemaine rows in DB...", scpList.size()));
 
 		final int[] insertedAndUpdatedMoisCount = new int[]{0, 0};
 		final int[] insertedAndUpdatedSemaineCount = new int[]{0, 0};
 
 		// Sort SeConnectePeriode list by premierJourPeriode
 		Collections.sort(scpList);
 
 		ListIterator<SeConnectePeriode> itScp = scpList.listIterator();
 		while (itScp.hasNext()) {
 
 			Object[][] monthValues = new Object[JDBC.JDBC_BATCH_ROW_COUNT][6];
 			Object[][] weekValues = new Object[JDBC.JDBC_BATCH_ROW_COUNT][6];
 
 			int k;
 			int i = 0;
 			Date currentProcessingDay = null;
 			Date premierJourPeriode = null;
 
 			// Split the collection in smaller collections for batch insert.
 			// Split by JDBC_BATCH_ROW_COUNT and by processing day
 			for (k = 0; (k < JDBC.JDBC_BATCH_ROW_COUNT) && itScp.hasNext(); k++) {
 				final SeConnectePeriode scp = itScp.next();
 
 				premierJourPeriode = scp.getPremierJourPeriode();
 				// Init currentProcessingDay on first looping
 				if (k == 0) {
 					currentProcessingDay = premierJourPeriode;
 				}
 
 				if (!DateUtils.isSameDay(currentProcessingDay, premierJourPeriode)) {
 					// Changing processing day so this is the end of the loop for this day.
 					itScp.previous();
 					break;
 				}
 
 				final String uai = scp.getUai();
 				final String nomProfil = scp.getNomProfil();
 				final String uid = scp.getUid();
 
 				Assert.hasText(uai, "Field SeConnectePeriode.uai must be supplied !");
 				Assert.hasText(nomProfil, "Field SeConnectePeriode.nomProfil must be supplied !");
 				Assert.hasText(uid, "Field SeConnectePeriode.uid must be supplied !");
 				Assert.notNull(premierJourPeriode, "Field SeConnectePeriode.premierJourPeriode must be supplied !");
 
 				// Premier jour du mois
 				final Date date = scp.getPremierJourPeriode();
 				final Date firstDayOfMonth = DateUtils.setDays(date, 1);
 				final String formattedFirstDayOfMonth = JDBC.dayOfYearFormat.format(firstDayOfMonth);
 
 				monthValues[k][0] = scp.getNbConnexion();
 				monthValues[k][1] = scp.getMoyenne();
 				monthValues[k][2] = uai;
 				monthValues[k][3] = nomProfil;
 				monthValues[k][4] = uid;
 				monthValues[k][5] = formattedFirstDayOfMonth;
 
 				// On recupere le type d'etablissement a partir de l'uai
 				//final Etablissement etab = this.getEtablissement(scp.getUai());
 				// Faut il effectuer un traitement par semaine ?
 				//boolean doSemaineProcessing = parSemaine.contains(etab.getTypeEtablissement());
 
 				//FIXME MBD: always do semaine processing ?
 				boolean doSemaineProcessing = true;
 				if (doSemaineProcessing) {
 					// Postitionnement du calendrier au début de la semaine
 					// la date du premier jour de la semaine
 					final Date firstDayOfWeek = DateHelper.getFirstDayOfWeek(date);
 					// Premier jour de la semaine
 					final String formattedFirstDayOfWeek = JDBC.dayOfYearFormat.format(firstDayOfWeek);
 
 					weekValues[i][0] = scp.getNbConnexion();
 					weekValues[i][1] = scp.getMoyenne();
 					weekValues[i][2] = uai;
 					weekValues[i][3] = nomProfil;
 					weekValues[i][4] = uid;
 					weekValues[i][5] = formattedFirstDayOfWeek;
 
 					i++;
 				}
 			}
 
 			if (k < JDBC.JDBC_BATCH_ROW_COUNT) {
 				// The values array is not full. Resize it to correct size.
 				monthValues = (Object[][]) ArrayUtils.subarray(monthValues, 0, k);
 			}
 
 			if (i < JDBC.JDBC_BATCH_ROW_COUNT) {
 				// The values array is not full. Resize it to correct size.
 				weekValues = (Object[][]) ArrayUtils.subarray(weekValues, 0, i);
 			}
 
 			final int[] countMois = this.insertOrUpdateSeConnectePeriodeRows(monthValues, false, connection);
 			insertedAndUpdatedMoisCount[0] += countMois[0];
 			insertedAndUpdatedMoisCount[1] += countMois[1];
 
 			final int[] countSemaine = this.insertOrUpdateSeConnectePeriodeRows(weekValues, true, connection);
 			insertedAndUpdatedSemaineCount[0] += countSemaine[0];
 			insertedAndUpdatedSemaineCount[1] += countSemaine[1];
 		}
 
 		if (JDBC.LOGGER.isInfoEnabled()) {
 			JDBC.LOGGER.info(String.format("[%1$d] SeConnecteMois rows were inserted and [%2$d] SeConnecteMois rows were updated.",
 					insertedAndUpdatedMoisCount[0], insertedAndUpdatedMoisCount[1]));
 			JDBC.LOGGER.info(String.format("[%1$d] SeConnecteSemaine rows were inserted and [%2$d] SeConnecteSemaine rows were updated.",
 					insertedAndUpdatedSemaineCount[0], insertedAndUpdatedSemaineCount[1]));
 		}
 	}
 
 
 	/**
 	 * @param inPartSelectQuery
 	 * @param values
 	 * @throws SQLException
 	 */
 	protected int[] insertOrUpdateSeConnectePeriodeRows(final Object[][] rowsValues, final boolean weekProcessingMode,
 			final Connection connection) throws SQLException {
 		if ((rowsValues == null) || (rowsValues.length == 0)) {
 			JDBC.LOGGER.warn("No SeConnectePeriode rows provided for insert nor update !");
 			return new int[] {0, 0};
 		}
 
 		final String insertRowQuery;
 		final String updateRowQuery;
 
 		if (weekProcessingMode) {
 			// Update seconnectesemaine rows
 			// Refresh nbconnexionseconnectesemaine by adding new connection count
 			// Refresh moyenneseconnectesemaine by recalculing the new moyenne
 			updateRowQuery = "update seconnectesemaine set " +
 					"moyennesemaine = (moyennesemaine * nbconnexionsemaine + mem.nbConnectionToAdd * mem.moyenneToAdd) " +
 					"/ (nbconnexionsemaine + mem.nbConnectionToAdd), " +
 					"nbconnexionsemaine = nbconnexionsemaine + mem.nbConnectionToAdd " +
 					"from (select cast(? as int) as nbConnectionToAdd, cast(? as double precision) as moyenneToAdd) as mem " +
 					"where uai = ? and nomprofil = ? and uid = ? and premierjoursemaine = to_date(?, 'YYYY-MM-DD')";
 
 			// Inesert seconnectemois rows
 			insertRowQuery = "insert into seconnectesemaine (nbconnexionsemaine, moyennesemaine, uai, nomprofil, uid, premierjoursemaine) " +
 					"values (?, ?, ?, ?, ?, to_date(?, 'YYYY-MM-DD'))";
 		}
 		else {
 			// Update seconnectemois rows
 			// Refresh nbconnexionmois by adding new connection count
 			// Refresh moyennemois by recalculing the new moyenne
 			updateRowQuery = "update seconnectemois set " +
 					"moyennemois = (moyennemois * nbconnexionmois + mem.nbConnectionToAdd * mem.moyenneToAdd) " +
 					"/ (nbconnexionmois + mem.nbConnectionToAdd), " +
 					"nbconnexionmois = nbconnexionmois + mem.nbConnectionToAdd " +
 					"from (select cast(? as int) as nbConnectionToAdd, cast(? as double precision) as moyenneToAdd) as mem " +
 					"where uai = ? and nomprofil = ? and uid = ? and mois = to_date(?, 'YYYY-MM-DD')";
 
 			// Inesert seconnectemois rows
 			insertRowQuery = "insert into seconnectemois (nbconnexionmois, moyennemois, uai, nomprofil, uid, mois) " +
 					"values (?, ?, ?, ?, ?, to_date(?, 'YYYY-MM-DD'))";
 		}
 
 		final int[] updatedRows = this.run.batch(connection, updateRowQuery, rowsValues);
 
 		Object[][] valuesToInsert = new Object[JDBC.JDBC_BATCH_ROW_COUNT][6];
 		int i = 0;
 		int updatedCount = 0;
 		for (int k = 0; k < updatedRows.length; k++) {
 			updatedCount += updatedRows[k];
 			if (updatedRows[k] == 0) {
 				// No rows updated, we need to insert it
 				valuesToInsert[i] = rowsValues[k];
 				i++;
 			}
 		}
 
 		if (i < JDBC.JDBC_BATCH_ROW_COUNT) {
 			// The values array is not full. Resize it to correct size.
 			valuesToInsert = (Object[][]) ArrayUtils.subarray(valuesToInsert, 0, i);
 		}
 
 		final int[] insertedRows = this.run.batch(connection, insertRowQuery, valuesToInsert);
 		int insertedCount = 0;
 		for (int rowsInserted : insertedRows) {
 			insertedCount += rowsInserted;
 		}
 
 		final int[] insertedAndUpdated = new int[] {insertedCount, updatedCount};
 
 		if (JDBC.LOGGER.isTraceEnabled()) {
 			final String table;
 			if (weekProcessingMode) {
 				table = "SeConnecteSemaine";
 			} else {
 				table = "SeConnecteMois";
 			}
 
 			JDBC.LOGGER.trace(String.format("Table: [%1$s] [%2$d] rows inserted. [%3$d] rows updated.",
 					table, insertedAndUpdated[0], insertedAndUpdated[1]));
 		}
 
 		return insertedAndUpdated;
 	}
 
 	/**
 	 * Find last LDAP processing in DB (specific table).
 	 * 
 	 * @return the last LDAP processing day of year.
 	 * @throws SQLException
 	 */
 	public Date findLastLdapProcessing() throws SQLException {
 		Date result = null;
 
 		final String query = "select valeur from configuration where cle = ?";
 		final Connection connection1 = this.getConnection();
 		try {
 			result = this.run.query(connection1, query, new ResultSetHandler<Date>() {
 				@Override
 				public Date handle(final ResultSet rs) throws SQLException {
 					Date result = null;
 					if (rs.next()) {
 						try {
 							result = JDBC.dayOfYearFormat.parse(rs.getString(1));
 						} catch (ParseException e) {
 							// Do nothing
 							JDBC.LOGGER.error("Error while parsing last LDAP processing date !", e);
 						}
 					}
 					rs.close();
 
 					return result;
 				}
 			}, JDBC.LAST_LDAP_PROCESSING_DATE_CONFIG_KEY);
 
 		} finally {
 			connection1.close();
 		}
 
 		if (result == null) {
 			final Connection connection2 = this.getConnection();
 			final String initQuery = "insert into configuration (cle, valeur) values (?, ?)";
 			this.run.update(connection2, initQuery, JDBC.LAST_LDAP_PROCESSING_DATE_CONFIG_KEY,
 					JDBC.dayOfYearFormat.format(JDBC.JANUARY_FIRST_2000));
 			this.commitTransaction(connection2);
 		}
 
 		if (result == null) {
 			result = JDBC.JANUARY_FIRST_2000;
 		}
 
 		return result;
 	}
 
 	public static void rollOutSqlException(final Log logger, final SQLException e) {
 		Assert.notNull(logger, "Logger cannot be null !");
 		Assert.notNull(e, "SQLException cannot be null !");
 
 		logger.error(String.format(
 				"SQL vendor specific error code: [%1$d] ; SQL state: [%1$s]",
 				e.getErrorCode(), e.getSQLState()));
 		SQLException next = e;
 		while ((next = next.getNextException()) != null) {
 			logger.error("Next SQL Exception is : ", next);
 		}
 	}
 
 	public void updateLastLdapProcessingDate(final Date processingDate) throws SQLException {
 		final String query = "update configuration set valeur = ? where cle = ?";
 		final String formattedDate = JDBC.dayOfYearFormat.format(processingDate);
 
 		final Connection connection = this.getConnection();
 		this.run.update(connection, query, formattedDate, JDBC.LAST_LDAP_PROCESSING_DATE_CONFIG_KEY);
 
 		this.commitTransaction(connection);
 	}
 
 	/**
 	 * Get SQL Connection from pool.
 	 * 
 	 * @return
 	 * @throws SQLException
 	 */
 	public Connection getConnection() throws SQLException {
 		final Connection connection = this.dataSource.getConnection();
 		connection.setAutoCommit(false);
 
 		JDBC.LOGGER.trace("Get one connection from the pool.");
 
 		return connection;
 	}
 
 	public static void viderBaseLog(final JDBC jdbc) throws ClassNotFoundException, SQLException {
 		Statement sql = jdbc.getConnection().createStatement();
 		String sqlText = "DELETE FROM seconnectemois";
 		System.out.println("Executing this command: " + sqlText + "\n");
 		sql.executeUpdate(sqlText);
 		String sqlText2 = "DELETE FROM seconnectesemaine";
 		System.out.println("Executing this command: " + sqlText2 + "\n");
 		sql.executeUpdate(sqlText2);
 		String sqlText3 = "DELETE FROM connexionservicejour";
 		System.out.println("Executing this command: " + sqlText3 + "\n");
 		sql.executeUpdate(sqlText3);
 		String sqlText4 = "DELETE FROM nombredevisiteurs";
 		System.out.println("Executing this command: " + sqlText4 + "\n");
 		sql.executeUpdate(sqlText4);
 
 	}
 
 	public static void viderBaseLdap(final JDBC jdbc) throws ClassNotFoundException, SQLException {
 
 		Statement sql = jdbc.getConnection().createStatement();
 		String sqlText = "DELETE FROM est_activee";
 		System.out.println("Executing this command: " + sqlText + "\n");
 		sql.executeUpdate(sqlText);
 		String sqlText1 = "DELETE FROM acommeprofil";
 		System.out.println("Executing this command: " + sqlText1 + "\n");
 		sql.executeUpdate(sqlText1);
 		String sqlText2 = "DELETE FROM etablissement";
 		System.out.println("Executing this command: " + sqlText2 + "\n");
 		sql.executeUpdate(sqlText2);
 
 	}
 
 	public static void viderBaseTraitementMensuelHebdomadaire(final JDBC jdbc) throws ClassNotFoundException, SQLException {
 
 		Statement sql = jdbc.getConnection().createStatement();
 		String sqlText = "DELETE FROM connexionprofilsemaine";
 		System.out.println("Executing this command: " + sqlText + "\n");
 		sql.executeUpdate(sqlText);
 		String sqlText1 = "DELETE FROM connexionprofilmois";
 		System.out.println("Executing this command: " + sqlText1 + "\n");
 		sql.executeUpdate(sqlText1);
 
 	}
 
 }
