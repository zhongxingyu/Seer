 package pl.edu.agh.to1.dice.logic.players.ai;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import pl.edu.agh.to1.dice.TUI.ReadingUserInputException;
 import pl.edu.agh.to1.dice.logic.*;
 import pl.edu.agh.to1.dice.logic.players.AbstractPlayer;
 import pl.edu.agh.to1.dice.logic.players.ai.figurechoosing.IFigureChoosingStrategy;
 import pl.edu.agh.to1.dice.logic.players.ai.freezing.IFreezingStrategy;
 
 /**
  * This is a basic modular bot that utilizes strategy pattern to change it's behaviour. There are two kinds of
  * strategies: freezing strategies and figure-choosing strategies. Both can be adjusted during runtime
  *
  * Author: Piotr Turek
  */
 public class ModularBot extends AbstractPlayer {
     private static final Logger LOGGER = LoggerFactory.getLogger(ModularBot.class);
 
     private IFreezingStrategy freezingStrategy;
     private IFigureChoosingStrategy figureChoosingStrategy;
 
 
     public ModularBot(String name) {
        this.name = name;
     }
 
     public void sparePoints(DiceBox diceBox) throws ReadingUserInputException {
         score.add(figureChoosingStrategy.chooseFigure(score, diceBox), diceBox);
     }
 
     public void manageDices(DiceBox diceBox) throws ReadingUserInputException {
         diceBox.freeze(freezingStrategy.getToFreeze(score, diceBox));
     }
 
     public IFreezingStrategy getFreezingStrategy() {
         return freezingStrategy;
     }
 
     public void setFreezingStrategy(IFreezingStrategy freezingStrategy) {
         this.freezingStrategy = freezingStrategy;
     }
 
     public IFigureChoosingStrategy getFigureChoosingStrategy() {
         return figureChoosingStrategy;
     }
 
     public void setFigureChoosingStrategy(IFigureChoosingStrategy figureChoosingStrategy) {
         this.figureChoosingStrategy = figureChoosingStrategy;
     }
 }
