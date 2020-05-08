 /**
  * BookEditor - Package: syam.BookEditor.Enum
  * Created: 2012/09/08 12:09:57
  */
 package syam.BookEditor.Enum;
 
 import org.bukkit.command.CommandSender;
 import org.bukkit.entity.Player;
 
 import syam.BookEditor.BookEditor;
 
 /**
  * Permission (Permission.java)
  * @author syam
  */
 public enum Permission {
 	/* 権限ノード */
 
 	// CopyCommand
	COPY ("user.copy"),
	COPY_FREE ("free.copy"),
	COPY_OTHER ("other.copy"),
 
 	// EditCommand
	EDIT ("user.edit"),
	EDIT_FREE ("free.edit"),
	EDIT_OTHER ("other.edit"),
 
 	// ReloadCommand
 	RELOAD ("reload"),
 	;
 
 	// ノードヘッダー
 	final String header = "bookeditor.";
 
 	private String node;
 
 	/**
 	 * コンストラクタ
 	 * @param node ノード
 	 */
 	Permission(final String node){
 		this.node = header + node;
 	}
 
 	/**
 	 * 指定した名前のプレイヤーが権限を持っているか
 	 * @param playerName name
 	 * @return boolean
 	 */
 	public boolean hasPerm(String playerName){
 		Player target = BookEditor.getInstance().getServer().getPlayer(playerName);
 		return target != null && hasPerm(target);
 	}
 
 	/**
 	 * 指定したプレイヤーが権限を持っているか
 	 * @param player Player
 	 * @return boolean
 	 */
 	public boolean hasPerm(Player player){
 		return player.hasPermission(this.node);
 	}
 
 	/**
 	 * 指定したCommandSenderが権限を持っているか
 	 * @param sender CommandSender
 	 * @return boolean
 	 */
 	public boolean hasPerm(CommandSender sender){
 		return sender.hasPermission(this.node);
 	}
 
 	/* getter */
 	/**
 	 * 権限ノードを取得する
 	 * @return 権限ノード
 	 */
 	public String getNode(){
 		return this.node;
 	}
 }
