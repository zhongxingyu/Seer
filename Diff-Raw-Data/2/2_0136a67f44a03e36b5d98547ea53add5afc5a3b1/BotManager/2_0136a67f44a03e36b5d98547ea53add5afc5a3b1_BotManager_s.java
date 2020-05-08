 package pl.edu.agh.to1.dice.logic.players.ai;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import pl.edu.agh.to1.dice.logic.players.Player;
 import pl.edu.agh.to1.dice.logic.players.ai.figurechoosing.GreedyChoosingStrategy;
 import pl.edu.agh.to1.dice.logic.players.ai.freezing.GreedyFreezingStrategy;
 
 import java.util.ArrayList;
 import java.util.List;
 
 /**
  * @author Michal Partyka
  */
 @Service
 public class BotManager {
     private final double defaultCoef = 0.85;
 
     @Autowired
     private GreedyChoosingStrategy greedyChoosingStrategy;
 
     @Autowired
     private GreedyFreezingStrategy greedyFreezingStrategy;
 
     public List<Player> createBots(Integer amount) {
         return createBots(amount, defaultCoef);
     }
 
     public List<Player> createBots(Integer amount, double coef) {
         greedyChoosingStrategy.setWillTakeBest(coef);
         final List<Player> bots = new ArrayList<>();
        while ( amount > 0 ) {
             ModularBot modularBot = new ModularBot(amount.toString() + "bot");
             modularBot.setFigureChoosingStrategy(greedyChoosingStrategy);
             modularBot.setFreezingStrategy(greedyFreezingStrategy);
             bots.add(modularBot);
         }
         return bots;
     }
 }
