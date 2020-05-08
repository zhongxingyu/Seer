 package us.praefectus.scorebored;
 
 import java.util.ArrayList;
 import java.util.LinkedList;
 import java.util.List;
 import org.apache.log4j.Logger;
 import us.praefectus.scorebored.Team.Side;
 import us.praefectus.scorebored.talker.*;
 import us.praefectus.scorebored.util.Check;
 
 public class Match {
     
     private static final Logger log = Logger.getLogger(Match.class);
     
     private SwingTalker talker;
     private Style style; 
     private boolean active = false;
     private GameLength gameLength;
     private MatchLength matchLength;
     private Team leftTeam = new Team(Team.Side.LEFT);
     private Team rightTeam = new Team(Team.Side.RIGHT);
     private Team.Side server;
     private List<MatchListener> listeners = new LinkedList<MatchListener>();
     private boolean subtitles = false;
     private boolean fullScreen = true;
     
     private List<PointHistory> histories = new ArrayList<PointHistory>();
     
     static { 
         //log.setLevel(Level.TRACE);
     }
     
     public Match(SwingTalker talker) {        
 
         this.talker = talker;
         gameLength = GameLength.TWENTY_ONE;
         matchLength = MatchLength.ONE; 
         
         style = Style.LED; 
 
         leftTeam.setName(new Speech("Home Team"));
         leftTeam.setColor(TeamColor.LED_RED);
         rightTeam.setName(new Speech("Away Team"));
         rightTeam.setColor(TeamColor.LED_CYAN);
     }
     
     public void reset() {
         leftTeam.setScore(0);
         rightTeam.setScore(0);
         leftTeam.setWins(0);
         rightTeam.setWins(0);
         server = null;
         setActive(false);
 
         histories.clear();
     }
     
     public void switchSides() {
         Team temp = leftTeam;
         leftTeam = rightTeam;
         rightTeam = temp;
         leftTeam.switchSides();
         rightTeam.switchSides();
         if ( server == Side.LEFT ) { 
             server = Side.RIGHT;
         } else if ( server == Side.RIGHT ) { 
             server = Side.LEFT;
         }
         histories.clear();
     }
     
     public void clearHistory() {
         histories.clear();
     }
     
     public boolean isOvertime() {        
         return (leftTeam.getScore() >= gameLength.getPoints() ||
                rightTeam.getScore() >= gameLength.getPoints()) && 
                Math.abs(leftTeam.getScore() - rightTeam.getScore()) < 2;
     }
     
     public boolean isEndOfGame() {
         return (leftTeam.getScore() >= gameLength.getPoints() || 
                 rightTeam.getScore() >= gameLength.getPoints()) &&
                !isOvertime();
     }
     
     public Team getWinner() {
         if ( !isEndOfGame() ) {
             return null;
             
         }
         return leftTeam.getScore() > rightTeam.getScore() 
                 ? leftTeam : rightTeam;
     }
 
     public Team getLoser() {
         if ( !isEndOfGame() ) {
             return null;
             
         }
         return leftTeam.getScore() > rightTeam.getScore() 
                 ? rightTeam : leftTeam;
     }
         
     public boolean isEndOfMatch() {
         if ( !isEndOfGame() ) {
             return false;
         }
         return leftTeam.getWins() == matchLength.getMinGames() ||
                rightTeam.getWins() == matchLength.getMinGames(); 
     }
     
     public boolean isServerChange() {
         int totalScore = leftTeam.getScore() + rightTeam.getScore();
         if ( totalScore == 0 ) { 
             return false;
         }
         switch ( gameLength ) {
             case ELEVEN:
                 return totalScore % 2 == 0 || isOvertime();
             case TWENTY_ONE:
                 return totalScore % 5 == 0 || isOvertime();
         }
         throw new IllegalStateException("Invalid game length: " + gameLength);
     }
     
     public void switchServers() {
         if ( server == Team.Side.LEFT ) {
             server = Team.Side.RIGHT;  
         } else if ( server == Team.Side.RIGHT ) {
             server = Team.Side.LEFT;
         }
     }
     
     public void incrementTeamVictory(Team.Side side) {
         Team team = getTeam(side);
         if(team.getWins() < matchLength.getMinGames() - 1 ) {
             team.setWins(team.getWins() + 1);
         }
     }
 
     public void decrementTeamVictory(Team.Side side) {
        Team team = getTeam(side);
        if(team.getWins() > 0) {
         team.setWins(team.getWins() - 1);
        }
     }
 
     public void decrementTeamScore(Team.Side side) {
         Team team = getTeam(side);
         log.trace("Decrement team: " + team);
 
         if ( team.getScore() == 0 ) {
             return;
         }
         if ( isEndOfGame() ) {
             team.setWins(team.getWins() - 1);
         }
         if ( isServerChange() ) {
             this.switchServers();
         }
         histories.add(0, new PointHistory(side, PointHistory.Type.DECREMENT));
         team.setScore(team.getScore() - 1);
     }
     
     public void incrementTeamScore(Team.Side side) {
         Team team = getTeam(side);
         log.trace("Increment team: " + team);
         Commentary commentary = new Commentary();
         
         if ( isEndOfMatch() ) { 
             return;
         }
         if ( isEndOfGame() ) { 
             Team winner = getWinner();
             Team loser = getLoser();
             leftTeam.setScore(0);
             rightTeam.setScore(0);
             server = loser.getSide();
             switchSides();
             return;
         }
 
         if ( server == null ) {
             server = side;
             commentary.add(team.getName()).add(" serves first");
         } else {                
             team.setScore(team.getScore() + 1);
             histories.add(0, new PointHistory(side, PointHistory.Type.INCREMENT));
             if ( !isOvertime() && !isEndOfGame() ) {
                 commentary.add("Point ").add(team.getName());
             }
             if ( isEndOfGame() ) {
                 Team winner = getWinner();
                 Team loser  = getLoser();
 
                 winner.setWins(winner.getWins() + 1);
                 
                 if(leftTeam.getWins() == matchLength.getMinGames() ||
                    rightTeam.getWins() == matchLength.getMinGames()) {
 
                     commentary.next("Congratulations ").add(winner.getName()) 
                               .next("You have defeated ").add(loser.getName());  
                 }
                 else {
                      commentary.add(winner.getName()).add(" wins the game")
                                .next("Switch sides")
                                .next("Losers serve first");
                 }
                 if(loser.getScore() == 0) {
                     commentary.next("Perfect game!");
                } else if(loser.getScore() <= 12) {
                     commentary.next("Sorry ").add(loser.getName())
                               .add(", Jacob is not impressed!");
                 }              
             }
             else {
                 int totalScore = leftTeam.getScore() + rightTeam.getScore();
                 //check for o-fer and add to commentary
                 if ( !isOvertime() && totalScore != 0 && isServerChange() ) {
                     log.trace("Check for ofer");
                     commentary = checkForOfer(team, commentary);
                 }
 
                 if ( isServerChange() ) {
                     if(!isOvertime()) {
                         commentary.next("Change servers", "Change servers!");
                     }
                     switchServers();
                     //String name = getTeam(server).getName();
                 }
                 //announce score
                 if(!isOvertime()) {
                     AnnounceScore announceScore = new AnnounceScore(this);
                     commentary.next(announceScore.getScore());
                 }
                 else {
                     if(leftTeam.getScore() == rightTeam.getScore()) {
                         commentary.next("Deuce", "Deuce!");
                     }
                 }
 
                 //check for gamepoint or matchpoint
                 commentary = checkForGamePoint(commentary);
             }
         }
 
         log.trace("Score: " + leftTeam.getName() + " " + leftTeam.getScore() + 
                 " to " + rightTeam.getName() + " " + rightTeam.getScore());
         talker.say(commentary);
     }
 
     public Commentary checkForOfer(Team team, Commentary commentary) {
         int runCount  = PointHistory.getRunCount(histories);
         log.trace("Point run count: " + runCount);
         if (gameLength == GameLength.TWENTY_ONE) {
             int ofers = runCount / 5;
             if (ofers == 1) {
                 commentary.next("Ofer!", "Oh fer!");
             } else if (ofers == 2) {
                 commentary.next("Kenfer!", "Kenfer!");
             } else if (ofers == 3) {
                 commentary.next("Turkey!");
             } else if (ofers == 4) {
                 commentary.next("Double Kenfer!", "Double Kenfer!");
             }
         }
         if (gameLength == GameLength.ELEVEN) {
             int ofers = runCount / 4;
             if ( ofers == 1 ) {
                 commentary.next("Ofer!", "Oh fer!");
             } else if ( ofers == 2 ) {
                 commentary.next("Double Ofer!", "Double Oh fer!");
             }
         }
         return commentary;
     }
 
     public Commentary checkForGamePoint(Commentary commentary) {    
         if(leftTeam.getScore() >= 20 && leftTeam.getScore() - rightTeam.getScore() >= 1) {
             if(isOvertime()) {
                 commentary.next("Advantage ").add(leftTeam.getName());
             }
             else if(leftTeam.getWins() == matchLength.getMinGames() - 1) {
                 commentary.next("Match point");
             }
             else {
                 commentary.next("Game point");
             }
         }
         if(rightTeam.getScore() >= 20 && rightTeam.getScore() - leftTeam.getScore() >= 1) {
             if(isOvertime()) {
                 commentary.next("Advantage ").add(rightTeam.getName());
             }
             else if(rightTeam.getWins() == matchLength.getMinGames() - 1) {
                 commentary.next("Match point");
             }
             else {
                 commentary.next("Game point");
             }
         }
         
         return commentary;
     }
     
     public void addListener(MatchListener listener) { 
         listeners.add(Check.notNull(listener));
     }
     
     public boolean removeListener(MatchListener listener) { 
         return listeners.remove(listener);
     }
     
     /**
      * @return the active
      */
     public boolean isActive() {
         return active;
     }
 
     public void introductionCommentary() {
         talker.say(new Commentary()
             .add("Today's matchup")
             .next(getTeam(Team.Side.LEFT).getName())
             .add(" versus ")
             .add(getTeam(Team.Side.RIGHT).getName())
             .next("Volley for serve"));
     }
     
     /**
      * @param active the active to set
      */
     public void setActive(boolean active) {
         this.active = active;
         if ( active ) {
             for ( MatchListener listener: listeners ) { 
                 listener.matchStarted();
             }
         } else { 
             for ( MatchListener listener: listeners ) { 
                 listener.matchEnded();
             }
         }
     }
 
     /**
      * @return the gameLength
      */
     public GameLength getGameLength() {
         return gameLength;
     }
 
     /**
      * @param gameLength the gameLength to set
      */
     public void setGameLength(GameLength gameLength) {
         this.gameLength = gameLength;
     }
 
     /**
      * @return the matchLength
      */
     public MatchLength getMatchLength() {
         return matchLength;
     }
 
     /**
      * @param matchLength the matchLength to set
      */
     public void setMatchLength(MatchLength matchLength) {
         this.matchLength = matchLength;
     }
 
     public Team getTeam(Team.Side side) { 
         switch ( side ) {
             case LEFT:
                 return leftTeam;
             case RIGHT:
                 return rightTeam;
         }
         throw new IllegalStateException("Unknown side: " + side);
     }
 
     /**
      * @return the service
      */
     public Team.Side getServer() {
         return server;
     }
 
     /**
      * @param server the service to set
      */
     public void setServer(Team.Side server) {
         this.server = server;
     }
 
     /**
      * @return the style
      */
     public Style getStyle() {
         return style;
     }
 
     /**
      * @param style the style to set
      */
     public void setStyle(Style style) {
         this.style = style;
     }
     
     public boolean isSubtitled() {
         return subtitles;
     }
     
     public void setSubtitled(boolean subtitles) { 
         this.subtitles = subtitles;
     }
     
     public SwingTalker getTalker() {
         return talker;
     }
     
     public boolean isFullScreen() {
         return fullScreen;
     }
     
     public void setFullScreen(boolean fullScreen) {
         this.fullScreen = fullScreen;
     }
     
 }
