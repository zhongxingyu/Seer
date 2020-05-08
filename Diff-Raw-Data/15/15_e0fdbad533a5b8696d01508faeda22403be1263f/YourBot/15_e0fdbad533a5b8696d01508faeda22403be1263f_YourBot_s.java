 package com.videoplaza.poker.bot;
 
 import java.util.Random;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.videoplaza.poker.game.model.Card;
 import com.videoplaza.poker.game.model.Game;
 import com.videoplaza.poker.game.model.Player;
 
 public class YourBot extends AbstractTournamentBot {
 
    private static final Random rnd = new Random(System.currentTimeMillis());
 
    @Override
    public String getAvatarImageUrl() {
       return "http://disturbingauctions.com/thumbnails/froggolf.jpg";
    }
 
    @Override
    public String getCreator() {
       return "You";
    }
 
    @Override
    public String getName(HttpServletRequest req) {
       return "You";
    }
 
    @Override
    public Bet play(Game game, Player me, HttpServletRequest req) {
       try {
          if (game.getCards().size() == 0) {
             return preFlop(game, me);
          }
          float handrank = getHandRank(game, me);
          System.out.println("Handrank " + handrank);
          if (handrank > 0.5f) {
             return raise(game, me, game.getHighestBet() * 2, "Good postflop handrank");
          }
         return checkOrfold("Bad postflop handrank");
       } catch (Exception e) {
          e.printStackTrace();
          return new Bet(0, "Exception");
       }
    }
 
    private Bet preFlop(Game game, Player me) throws Exception {
       Card[] cards = getMyCards(me);
       if (cards[0].getRank() == cards[1].getRank()) {
          return allIn(me, "Pocket pairs");
       }
       float handrank = (cards[0].getRank() + cards[1].getRank()) / 28f;
       System.out.println("Preflop handrank " + handrank);
       if (handrank > 0.2f) {
          return call(game, me, "Good preflop hand");
       }
      return checkOrfold("Bad preflop hand");
    }
 }
