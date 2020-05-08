 package eu32k.ludumdare.ld26.gameplay;
 
 import eu32k.ludumdare.ld26.effects.FadeComplete;
 import eu32k.ludumdare.ld26.events.IEvent;
 import eu32k.ludumdare.ld26.events.IEventHandler;
 import eu32k.ludumdare.ld26.level.Tile;
 import eu32k.ludumdare.ld26.state.GameState;
 import eu32k.ludumdare.ld26.state.GlobalState;
 import eu32k.ludumdare.ld26.state.LevelLosingState;
 import eu32k.ludumdare.ld26.state.LevelLostState;
 import eu32k.ludumdare.ld26.state.LevelPauseState;
 import eu32k.ludumdare.ld26.state.LevelState;
 import eu32k.ludumdare.ld26.state.LevelWinningState;
 import eu32k.ludumdare.ld26.state.LevelWonState;
 import eu32k.ludumdare.ld26.state.MenuState;
 import eu32k.ludumdare.ld26.state.StateMachine;
 
 public class GameEventHandler implements IEventHandler {
 
    private GlobalState globalState;
    private LevelState levelState;
 
    public GameEventHandler() {
       globalState = StateMachine.instance().getState(GlobalState.class);
       levelState = StateMachine.instance().getState(LevelState.class);
       globalState.getEvents().addHandler(this);
       levelState.getEvents().addHandler(this);
    }
 
    @Override
    public void handle(IEvent ev) {
       if (ev instanceof GameplayEvent) {
          handleGameplayEvent((GameplayEvent) ev);
       } else if (ev instanceof FadeComplete) {
          handleFadeComplete((FadeComplete) ev);
       }
    }
 
    private void handleFadeComplete(FadeComplete ev) {
       levelState.log("Popped tile");
       Tile t = ev.fade.getTile();
       t.setDead(true);
       levelState.getLevel().getTiles().remove(t);
       if (levelState.playerTile == t) {
          levelState.playerTile = null;
       }
       if (levelState.goalTile == t) {
          levelState.goalTile = null;
       }
    }
 
    private void handleGameplayEvent(GameplayEvent event) {
      GameState state = StateMachine.instance().getCurrentState();
       switch (event.getType()) {
       case PAUSE:
          levelState.setPaused(true);
          levelState.log("Paused");
          StateMachine.instance().enterState(LevelPauseState.class);
          break;
       case RESUME:
          levelState.setPaused(false);
          levelState.log("Resumed");
          StateMachine.instance().enterState(LevelState.class);
          break;
       case NEXTLEVEL:
          if (state instanceof LevelWinningState || state instanceof LevelWonState) {
             if (levelState.getLevels().nextLevel()) {
                levelState.initLevel();
                StateMachine.instance().enterState(LevelState.class);
             } else {
                StateMachine.instance().enterState(MenuState.class);
             }
          }
          break;
       case WIN:
          levelState.log("WON");
          levelState.getEvents().clear();
          StateMachine.instance().enterState(LevelWinningState.class);
          break;
       case LOSE:
          String death = "LOST";
          switch (event.getParam()) {
          case GameplayEvent.PARAM_LOSE_FALLOFFBOARD:
            levelState.getEvents().clear();
             death = "Player has fallen off the board";
            levelState.log(death);
            StateMachine.instance().enterState(LevelLosingState.class);
             break;
          case GameplayEvent.PARAM_LOSE_SQUASHED:
            levelState.getEvents().clear();
             death = "Player has been squashed to death";
            levelState.log(death);
            StateMachine.instance().enterState(LevelLosingState.class);
             break;
          case GameplayEvent.PARAM_LOSE_TIMEOUT:
            levelState.getEvents().clear();
             death = "Time is up";
            levelState.log(death);
            StateMachine.instance().enterState(LevelLosingState.class);
             break;
          case GameplayEvent.PARAM_LOSE_TOLOST:
            if (state instanceof LevelLosingState) {
               StateMachine.instance().enterState(LevelLostState.class);
            }
             return;
          }
 
          // StateMachine.instance().createState(new LevelState());
          // StateMachine.instance().getState(LevelState.class).setStage(new GameStage());
          break;
       }
    }
 
 }
