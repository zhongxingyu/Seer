 package net.skycraftmc.SkyQuest.action;
 
 
 import java.awt.BorderLayout;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.swing.JLabel;
 import javax.swing.JTextField;
 
 import net.skycraftmc.SkyQuest.QuestAction;
 import net.skycraftmc.SkyQuest.utilitygui.ActionEditor;
 import net.skycraftmc.SkyQuest.utilitygui.component.EmptyTextListener;
 
 import org.bukkit.Bukkit;
 import org.bukkit.ChatColor;
 import org.bukkit.entity.Player;
 
 public class MessageAction extends ActionType
 {
 	private Pattern pat = Pattern.compile("&player;");
 	public boolean apply(String player, String action) 
 	{
 		if(!isValid(action))
 			throw new IllegalArgumentException("action is not valid");
 		Player p = Bukkit.getServer().getPlayerExact(player);
 		if(p == null)return false;
 		Matcher m = pat.matcher(action);
		p.sendMessage(ChatColor.translateAlternateColorCodes('&', m.replaceAll(Matcher.quoteReplacement(player))));
 		return true;
 	}
 
 	public boolean isValid(String action) 
 	{
 		if(action == null)return false;
 		return !action.trim().isEmpty();
 	}
 
 	public String getName() 
 	{
 		return "Message";
 	}
 
 	public boolean requiresPlayer() 
 	{
 		return true;
 	}
 	
 	public String getDescription()
 	{
 		return "Sends a message to the player.";
 	}
 	
 	@Override
 	public ActionEditor createEditorPanel()
 	{
 		return new MessageEditorPanel();
 	}
 	@SuppressWarnings("serial")
 	private class MessageEditorPanel extends ActionEditor
 	{
 		private JTextField tf;
 		@Override
 		public void init()
 		{
 			tf = new JTextField();
 			setLayout(new BorderLayout());
 			add("North", new JLabel("The player can be specified with &player;"));
 			add("Center", tf);
 			add("West", new JLabel("Message"));
 			tf.getDocument().addDocumentListener(new EmptyTextListener(getFinishButton()));
 		}
 		public String createData() 
 		{
 			return tf.getText().trim();
 		}
 		public void loadFrom(QuestAction action)
 		{
 			tf.setText(action.getAction());
 		}
 	}
 }
