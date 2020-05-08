 package org.opendarts.core.player.internal.service;
 
 import java.io.File;
 import java.text.MessageFormat;
 import java.util.List;
 import java.util.UUID;
 import java.util.concurrent.atomic.AtomicReference;
 
 import javax.persistence.EntityManager;
 import javax.persistence.EntityManagerFactory;
 import javax.persistence.EntityTransaction;
 import javax.persistence.NoResultException;
 import javax.persistence.PersistenceException;
 import javax.persistence.TypedQuery;
 
 import org.opendarts.core.model.player.IComputerPlayer;
 import org.opendarts.core.model.player.IPlayer;
 import org.opendarts.core.player.model.ComputerPlayer;
 import org.opendarts.core.player.model.Player;
 import org.opendarts.core.service.dart.IDartService;
 import org.opendarts.core.service.player.IPlayerService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 /**
  * The Class PlayerService.
  */
 public class PlayerService implements IPlayerService {
 
 	/** The logger. */
 	private static final Logger LOG = LoggerFactory
 			.getLogger(PlayerService.class);
 
 	/** The dart service. */
 	private final AtomicReference<IDartService> dartService;
 
 	/** The EntityManagerFactory. */
 	private final AtomicReference<EntityManagerFactory> emf;
 
 	/** The EntityManager. */
 	private EntityManager em;
 
 	/**
 	 * Instantiates a new player service.
 	 */
 	public PlayerService() {
 		super();
 		this.dartService = new AtomicReference<IDartService>();
 		this.emf = new AtomicReference<EntityManagerFactory>();
 	}
 
 	/**
 	 * Startup.
 	 */
 	public void startup() {
 		this.em = this.emf.get().createEntityManager();
 		LOG.debug("Using Database: {}",
 				new File("OpenDartsPlayers").getAbsoluteFile());
 
 		// Create local user (if needed)
 		String name = System.getenv("USER");
 		if ((name == null) || "".equals(name)) {
 			name = System.getenv("USERNAME");
 			name = Character.toUpperCase(name.charAt(0)) + name.substring(1);
 		}
 		try {
 			TypedQuery<IPlayer> query = this.em.createNamedQuery(
 					"Player.byName", IPlayer.class);
 			query.setParameter("name", name);
 			query.getSingleResult();
 		} catch (NoResultException nre) {
 			LOG.warn("Missing local user, gonna create it");
 			this.createPlayer(name);
 		}
 
 		// Create computers (if needed)
 		if (this.getAllComputerPlayers().isEmpty()) {
 			LOG.warn("Missing comupters players, gonna create then");
 			for (int lvl = 0; lvl < 16; lvl++) {
 				this.createComputer(lvl);
 			}
 		}
 	}
 
 	/**
 	 * Shutdown.
 	 */
 	public void shutdown() {
 		try {
 			this.em.close();
 		} finally {
 			this.emf.get().close();
 		}
 	}
 
 	/**
 	 * Creates the player.
 	 *
 	 * @param name the name
 	 * @return the player
 	 */
 	@Override
 	public IPlayer createPlayer(String name) {
 		// Create entity
 		Player player = new Player();
 		player.setUuid(UUID.randomUUID().toString());
 
 		if ((name == null) || "".equals(name)) {
 			player.setName("The misterious player");
 		} else {
 			player.setName(name);
 		}
 
 		// JPA persistence
 		EntityTransaction tx = this.em.getTransaction();
 		tx.begin();
 		try {
 			this.em.persist(player);
 			tx.commit();
 			LOG.info("New player: {}", player);
 		} catch (PersistenceException e) {
 			LOG.error("Could not save the player: " + player, e);
 			tx.rollback();
 		}
 
 		return player;
 	}
 
 	/**
 	 * Creates the computer.
 	 *
 	 * @param level the level
 	 * @return the computer player
 	 */
 	@Override
 	public IComputerPlayer createComputer(int level) {
 		// Create entity
 		ComputerPlayer player = new ComputerPlayer();
 		player.setUuid(UUID.randomUUID().toString());
 		player.setName(MessageFormat.format("Com_{0,number,00}", level));
 		player.setLevel(level);
 
 		// JPA persistence
 		EntityTransaction tx = this.em.getTransaction();
 		tx.begin();
 		try {
 			this.em.persist(player);
 			tx.commit();
 			LOG.info("New Computer player: {}", player);
 		} catch (PersistenceException e) {
 			LOG.error("Could not save the player: " + player, e);
 			tx.rollback();
 		}
 		return player;
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.core.service.player.IPlayerService#getPlayer(java.lang.String)
 	 */
 	@Override
 	public IPlayer getPlayer(String uuid) {
 		TypedQuery<IPlayer> query = this.em.createNamedQuery("Player.byUuid",
 				IPlayer.class);
 		query.setParameter("uuid", uuid);
		return query.getSingleResult();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.core.service.player.IPlayerService#getComputerPlayer(int)
 	 */
 	@Override
 	public IComputerPlayer getComputerPlayer(int lvl) {
 		TypedQuery<IComputerPlayer> query = this.em.createNamedQuery(
 				"ComputerPlayer.byLevel", IComputerPlayer.class);
 		query.setParameter("level", lvl);
 		return query.getSingleResult();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.core.service.player.IPlayerService#getAllComputerPlayers()
 	 */
 	@Override
 	public List<IComputerPlayer> getAllComputerPlayers() {
 		TypedQuery<IComputerPlayer> query = this.em.createNamedQuery(
 				"ComputerPlayer.all", IComputerPlayer.class);
 		return query.getResultList();
 	}
 
 	/* (non-Javadoc)
 	 * @see org.opendarts.core.service.player.IPlayerService#getAllPlayers()
 	 */
 	@Override
 	public List<IPlayer> getAllPlayers() {
 		TypedQuery<IPlayer> query = this.em.createNamedQuery("Player.all",
 				IPlayer.class);
 		return query.getResultList();
 	}
 
 	// Services
 	/**
 	 * Sets the dart service.
 	 *
 	 * @param dartService the new dart service
 	 */
 	public void setDartService(IDartService dartService) {
 		this.dartService.set(dartService);
 	}
 
 	/**
 	 * Unset dart service.
 	 *
 	 * @param dartService the dart service
 	 */
 	public void unsetDartService(IDartService dartService) {
 		this.dartService.compareAndSet(dartService, null);
 	}
 
 	/**
 	 * Sets the entity manager factory.
 	 *
 	 * @param emf the new entity manager factory
 	 */
 	public void setEntityManagerFactory(EntityManagerFactory emf) {
 		this.emf.set(emf);
 	}
 
 	/**
 	 * Unset entity manager factory.
 	 *
 	 * @param emf the emf
 	 */
 	public void unsetEntityManagerFactory(EntityManagerFactory emf) {
 		this.emf.compareAndSet(emf, null);
 	}
 
 }
