 package pl.dagguh.soccerbackend.game.boundary;
 
import java.util.List;
 import javax.ejb.EJB;
 import javax.ejb.Stateless;
 import javax.persistence.*;
 import javax.ws.rs.*;
 import javax.ws.rs.core.MediaType;
 import org.apache.log4j.Logger;
 import pl.dagguh.soccerbackend.game.control.GameNotFoundException;
 import pl.dagguh.soccerbackend.game.entity.Game;
 import pl.dagguh.soccerbackend.game.entity.GameField;
 import pl.dagguh.soccerbackend.player.entity.Player;
 import pl.dagguh.soccerbackend.player.boundary.PlayerService;
 import pl.dagguh.soccerbackend.player.control.PlayerNotFoundException;
 import pl.dagguh.soccerbackend.player.control.TicketMismatchException;
 
 /**
  * @author Maciej Kwidziński <maciek.kwidzinski@gmail.com>
  */
 @Stateless
 @Path("/game")
 @Table()
 public class GameService {
 
 	private static Logger log = Logger.getLogger(GameService.class);
 	public static final String emptyPlayerNick = "";
 	@PersistenceContext
 	private EntityManager em;
 	@EJB
 	private PlayerService playerService;
 
 	@PUT
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.TEXT_PLAIN)
 	public String hostGame(Player firstPlayer) {
 		try {
 			log.info("Creating new game with first player: " + firstPlayer);
 			playerService.validateTicket(firstPlayer);
 			Game mergedGame = em.merge(createNewGame(firstPlayer.getNick()));
 			log.info("New game created " + mergedGame);
 			return Long.toString(mergedGame.getId());
 		} catch (PlayerNotFoundException e) {
 			return "-1";
 		} catch (TicketMismatchException e) {
 			return "-1";
 		}
 	}
 
 	public Game createNewGame(String redPlayerNick) {
 		Game game = new Game();
 		game.setRedPlayerNick(redPlayerNick);
 		game.setBluePlayerNick(emptyPlayerNick);
 		game.setIsItRedsTurn(true);
 		game.setGameField(new GameField());
 		return game;
 	}
 
 	@POST
 	@Consumes(MediaType.APPLICATION_JSON)
 	@Produces(MediaType.TEXT_PLAIN)
 	@Path("/join/{gameId}")
 	public void join(Player bluePlayer, @PathParam("gameId") String gameId) {
 		try {
 			Game game = find(gameId);
 			game.setBluePlayerNick(bluePlayer.getNick());
 			em.merge(game);
 		} catch (GameNotFoundException e) {
 			throw new WebApplicationException(404);
 		}
 	}
 
 	private Game find(String gameId) throws GameNotFoundException {
 		return find(Long.parseLong(gameId));
 	}
 
 	private Game find(long gameId) throws GameNotFoundException {
 		Game game = em.find(Game.class, gameId);
 		if (null == game) {
 			throw new GameNotFoundException();
 		}
 		return game;
 	}
 
 	@GET
 	@Path("/list")
 	@Produces(MediaType.TEXT_PLAIN)
 	public String findOpenGame() {
 		try {
 			TypedQuery<Long> query = em.createQuery("SELECT e.id FROM Game e WHERE e.bluePlayerNick = :blueNick", Long.class);
 			query.setParameter("blueNick", "");
			List<Long> gameIds = query.getResultList();
			log.info("Found open game ids " + gameIds);
			return Long.toString(gameIds.get(0));
		} catch (IndexOutOfBoundsException e) {
 			log.info("No open games found");
 			return "-1";
 		}
 	}
 }
