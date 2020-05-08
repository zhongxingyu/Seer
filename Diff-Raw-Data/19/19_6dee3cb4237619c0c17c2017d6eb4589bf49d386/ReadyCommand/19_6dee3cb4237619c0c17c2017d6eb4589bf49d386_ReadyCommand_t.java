 /**
  * TagGame - Package: syam.taggame.command
  * Created: 2012/11/08 16:40:22
  */
 package syam.taggame.command;
 
 import syam.taggame.Perms;
 import syam.taggame.enums.GameState;
 import syam.taggame.exception.CommandException;
 import syam.taggame.game.Game;
 import syam.taggame.manager.GameManager;
 
 /**
  * ReadyCommand (ReadyCommand.java)
  * @author syam(syamn)
  */
 public class ReadyCommand extends BaseCommand {
 	public ReadyCommand(){
 		bePlayer = false;
 		name = "ready";
		argLength = 0;
 		usage = "<- ready game";
 	}
 
 	@Override
 	public void execute() throws CommandException {
 		// flag ready - ゲームを開始準備中にする
 		Game game = GameManager.getGame();
 
 		// 開始状態チェック
 		if (!game.getState().equals(GameState.WAITING)){
 			throw new CommandException("&cそのゲームは待機中ではありません！");
 		}
 
 		// スポーン地点チェック
 		if (game.getSpawns().size() != 2){
 			throw new CommandException("&cチームスポーン地点が正しく設定されていません");
 		}
 
 		// TODO call event here
 
 		// ready
 		game.ready(sender);
 	}
 
 	@Override
 	public boolean permission() {
 		return Perms.READY.has(sender);
 	}
 }
