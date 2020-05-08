 package gt.general.gui;
 
 import gt.general.Vote;
 import gt.general.character.Hero;
 import gt.lastgnome.scoring.HighscoreEntry;
 import gt.plugin.meta.Hello;
 
 import org.getspout.spoutapi.event.screen.ButtonClickEvent;
import org.getspout.spoutapi.event.screen.ScreenCloseEvent;
 import org.getspout.spoutapi.gui.Button;
 import org.getspout.spoutapi.gui.Color;
 import org.getspout.spoutapi.gui.Container;
 import org.getspout.spoutapi.gui.ContainerType;
 import org.getspout.spoutapi.gui.GenericButton;
 import org.getspout.spoutapi.gui.GenericContainer;
 import org.getspout.spoutapi.gui.GenericLabel;
 import org.getspout.spoutapi.gui.GenericPopup;
 import org.getspout.spoutapi.gui.Label;
 import org.getspout.spoutapi.gui.WidgetAnchor;
 
 public class GameScoreOverlay extends GenericPopup {
 
 	
 	
 	
 	public GameScoreOverlay(final HighscoreEntry entry, final Hero hero, final Vote vote) {
 				
 		
 		Label victoryLabel = new GenericLabel("Victory!");
 		victoryLabel.setTextColor(new Color(0f, 1f, 0f));
 		
 		victoryLabel.setScale(4f);
 		
 		victoryLabel.setWidth(100);
 		victoryLabel.setHeight(40);
 		
 		victoryLabel.shiftXPos(-50);
 		victoryLabel.shiftYPos(10);
 		
 		victoryLabel.setFixed(fixed);
 		
 		victoryLabel.setAnchor(WidgetAnchor.TOP_CENTER);
 		attachWidget(Hello.getPlugin(), victoryLabel);
 		
 				
 		Container outerPlayerScoreContainer = newContainer(ContainerType.HORIZONTAL);
 		outerPlayerScoreContainer.setFixed(true);
 		outerPlayerScoreContainer.shiftXPos(-100);
 		outerPlayerScoreContainer.shiftYPos(60);
 		
 		Container playersColumn = newContainer(ContainerType.VERTICAL);
 		for(String player : entry.getPlayers()) {
 			Label name = new GenericLabel(player);
 			name.setMargin(1);
 			name.setWidth(200);
 			name.setHeight(22);
 			name.setScale(1f);
 			name.setFixed(true);
 			
 			
 			playersColumn.addChild(name);
 		}
 		outerPlayerScoreContainer.addChild(playersColumn);
 		
 		
 		Label score = new GenericLabel(String.valueOf(entry.getPoints()));
 		score.setScale(2f);
 		score.setWidth(200);
 		score.setAlign(WidgetAnchor.TOP_RIGHT);
 		score.setFixed(true);
 		
 		outerPlayerScoreContainer.addChild(score);
 		
 		
 		outerPlayerScoreContainer.setAnchor(WidgetAnchor.TOP_CENTER);
 		attachWidget(Hello.getPlugin(), outerPlayerScoreContainer);	
 		
 		
 		Button restart = new GenericButton("Restart") {
 			@Override
 			public void onButtonClick(final ButtonClickEvent event) {
 				System.out.println("button accept");
 				vote.voteAccept(hero);
 			};
 		};
 		
 		restart.setWidth(100);
 		restart.setHeight(22);
 		restart.shiftXPos(-110);
 		restart.shiftYPos(200);
 		
 		restart.setAnchor(WidgetAnchor.TOP_CENTER);
 		attachWidget(Hello.getPlugin(), restart);
 		
 		Button exit = new GenericButton("Exit") {
 			@Override
 			public void onButtonClick(final ButtonClickEvent event) {
 				System.out.println("button reject");
 				vote.voteReject(hero);
 			};
 		};
 		
 		exit.setWidth(100);
 		exit.setHeight(22);
 		exit.shiftXPos(10);
 		exit.shiftYPos(200);
 		
 		exit.setAnchor(WidgetAnchor.TOP_CENTER);
 		attachWidget(Hello.getPlugin(), exit);
 		
 		
 
 		System.out.println(entry.getPoints());
 		
 	}
 	
 	private GenericContainer newContainer(final ContainerType type) {
 		GenericContainer container = new GenericContainer();
 		container.setLayout(type);
 		return container;
 	}
 }
