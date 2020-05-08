 package aigilas.states;
 
 import aigilas.Common;
 import aigilas.Config;
 import aigilas.net.Client;
 import aigilas.net.LanClient;
 import aigilas.net.Server;
 import aigilas.ui.SelectableButton;
 import aigilas.ui.UiAssets;
 import com.badlogic.gdx.Gdx;
 import com.badlogic.gdx.graphics.Color;
 import com.badlogic.gdx.scenes.scene2d.Actor;
 import com.badlogic.gdx.scenes.scene2d.Stage;
 import com.badlogic.gdx.scenes.scene2d.ui.Label;
 import com.badlogic.gdx.scenes.scene2d.ui.Table;
 import com.badlogic.gdx.scenes.scene2d.utils.ChangeListener;
 import sps.bridge.Contexts;
 import sps.core.Core;
 import sps.core.Logger;
 import sps.graphics.Assets;
 import sps.io.Input;
 import sps.states.State;
 import sps.states.StateManager;
 
 public class StartHostServerState implements State {
 
     private Stage stage;
     private Label connectedPlayersLbl;
 
     public StartHostServerState() {
         //Server
         Logger.info("IP is " + Config.get().serverIp());
         Server.reset();
         Client.reset(new LanClient());
 
         //UI
         Input.setContext(Contexts.get(Core.Non_Free), Client.get().getFirstPlayerIndex());
         stage = new Stage();
 
         Gdx.input.setInputProcessor(stage);
 
         Label.LabelStyle lblStyle = new Label.LabelStyle(Assets.get().font(), Color.WHITE);
         connectedPlayersLbl = new Label("0 players connected", lblStyle);
 
         final SelectableButton startGameBtn = new SelectableButton("Start", UiAssets.getButtonStyle());
         startGameBtn.addListener(new ChangeListener() {
             public void changed(ChangeEvent event, Actor actor) {
                 Logger.info("Starting the game");
                 Client.get().startGame();
             }
         });
 
         Table table = new Table();
         table.setFillParent(true);
         table.add(connectedPlayersLbl);
         table.row();
         table.add(startGameBtn);
 
         stage.addActor(table);
     }
 
     private int connectedPlayers;
 
     @Override
     public void update() {
         if (connectedPlayers != Client.get().getPlayerCount()) {
             connectedPlayers = Client.get().getPlayerCount();
             connectedPlayersLbl.setText(connectedPlayers + " players connected");
         }
         if (Client.get().isGameStarting()) {
             for (int ii = 0; ii < Client.get().getPlayerCount(); ii++) {
                 Input.setContext(Contexts.get(Common.Free), ii);
             }
 
            Input.setup(Client.get());
             StateManager.loadState(new LoadingState());
         }
     }
 
     @Override
     public void load() {
 
     }
 
     @Override
     public void unload() {
 
     }
 
     @Override
     public void draw() {
         stage.act(Gdx.graphics.getDeltaTime());
         stage.draw();
     }
 }
