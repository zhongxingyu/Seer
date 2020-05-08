 package nu.danielsundberg.yakutia.entity;
 
 import javax.persistence.*;
 import java.io.Serializable;
 import java.util.List;
 
 @Entity
 @NamedQueries({
         @NamedQuery(
                 name = "GamePlayer.getGamePlayersFromGameId",
                 query = "SELECT gp FROM GamePlayer gp WHERE gp.gameId =:gameId"
         ),
         @NamedQuery(
                 name = "GamePlayer.getGamePlayer",
                 query = "SELECT gp FROM GamePlayer gp WHERE gp.gameId =:gameId AND gp.playerId =:playerId"
         ),
         @NamedQuery(
                name = "GamePlayer.getGamePlayerFromPlayerId",
                 query = "SELECT gp FROM GamePlayer gp WHERE gp.playerId =:playerId"
         )
 })
 public class GamePlayer implements Serializable {
 
     @Id
     @GeneratedValue
     private long gamePlayerId;
 
     private long gameId;
     private long playerId;
 
     @Enumerated(EnumType.STRING)
     private GamePlayerStatus gamePlayerStatus;
 
     private long nextGamePlayerIdTurn;
 
     private boolean activePlayerTurn;
 
     @ManyToOne
     @JoinColumn(name="gameId", insertable = false, updatable = false)
     private Game game;
 
     @ManyToOne
     @JoinColumn(name = "playerId", insertable = false, updatable = false)
     private Player player;
 
     @OneToMany(mappedBy = "gamePlayer")
     private List<Unit> units;
 
     public long getGamePlayerId() {
         return gamePlayerId;
     }
 
     public void setGamePlayerId(long gamePlayerId) {
         this.gamePlayerId = gamePlayerId;
     }
 
     public long getGameId() {
         return gameId;
     }
 
     public void setGameId(long gameId) {
         this.gameId = gameId;
     }
 
     public long getPlayerId() {
         return playerId;
     }
 
     public void setPlayerId(long playerId) {
         this.playerId = playerId;
     }
 
     public GamePlayerStatus getGamePlayerStatus() {
         return gamePlayerStatus;
     }
 
     public void setGamePlayerStatus(GamePlayerStatus gamePlayerStatus) {
         this.gamePlayerStatus = gamePlayerStatus;
     }
 
     public long getNextGamePlayerIdTurn() {
         return nextGamePlayerIdTurn;
     }
 
     public void setNextGamePlayerIdTurn(long nextGamePlayerIdTurn) {
         this.nextGamePlayerIdTurn = nextGamePlayerIdTurn;
     }
 
     public boolean isActivePlayerTurn() {
         return activePlayerTurn;
     }
 
     public void setActivePlayerTurn(boolean activePlayerTurn) {
         this.activePlayerTurn = activePlayerTurn;
     }
 
     public Game getGame() {
         return game;
     }
 
     public void setGame(Game game) {
         this.game = game;
     }
 
     public Player getPlayer() {
         return player;
     }
 
     public void setPlayer(Player player) {
         this.player = player;
     }
 
     public List<Unit> getUnits() {
         return units;
     }
 
     public void setUnits(List<Unit> units) {
         this.units = units;
     }
 }
