 package view.state;
 
 import java.awt.Color;
 import java.util.Observable;
 import javax.swing.JButton;
 import javax.swing.JLabel;
 
 import command.Command;
 import command.CommandFactory;
 
 import model.MainModel;
 import model.core.PlayerIndex;
 
 import utils.GuiUtils;
 
 /**
  * The main menu view state
  */
 @SuppressWarnings("serial")
 public class GameOverViewState extends AbstractMenuViewState {
 
 	private JLabel label;
 	private JButton btn;
 	private JButton submitScore;
 
 	public GameOverViewState() {
 		// Label
 		this.label = new JLabel(new String(), JLabel.CENTER);
 		this.label.setForeground(Color.WHITE);
 		this.label.setAlignmentX(CENTER_ALIGNMENT);
 		// Button
 		this.btn = GuiUtils.createButtonWithStateCommand("Back to Menu", ViewState.Menu);
 		this.btn.setAlignmentX(CENTER_ALIGNMENT);
 
 		// Add to panel
 		this.add(this.label);
 		this.add(this.btn);
 	}
 
 	@Override
 	public void update(Observable o, Object arg) {
 		MainModel gm = (MainModel) o;
 
 		if (this.submitScore != null)
 			this.remove(this.submitScore);
 
 		Command cmd = CommandFactory.createSubmitHighscoreCommand(gm.getPlayerName(PlayerIndex.One), 
 				gm.getActiveGameState().getPlayer(PlayerIndex.One).getPoints(),
 				gm.getActiveDifficulty().getId())
 				.chain(CommandFactory.createSetStateCommand(ViewState.Highscore));
 		
 		this.submitScore = GuiUtils.createButtonWithCommand(
 				String.format("Submit score as \"%s\"", gm.getPlayerName(PlayerIndex.One)),
 				cmd);
 
 		this.add(this.submitScore);
 
 		String gameOverText = "";
 		switch (gm.getActiveGameState().getState()) {
 		case Won:
 			gameOverText = "You completed all levels on %s with a score of %d";
 			break;
 		case Lost:
 			gameOverText = "You didn't complete all levels on %s, but got a score of %d";
 			break;
 		default:
 			gameOverText = "You're in a weird state";
 			break;
 		}
 		this.label.setText(String.format(gameOverText, gm.getActiveDifficulty().getName(), gm.getActiveGameState().getPlayer(PlayerIndex.One).getPoints()));
 	}
 }
