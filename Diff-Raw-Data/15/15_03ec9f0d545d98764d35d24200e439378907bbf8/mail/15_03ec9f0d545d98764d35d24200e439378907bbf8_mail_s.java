 package mmo.Mail;
 
 import java.util.ArrayList;
 import java.util.Iterator;
 
 import org.bukkit.Material;
 import org.bukkit.Server;
 import org.bukkit.entity.Player;
 
 import mmo.Core.MMO;
 
 public class mail {
 	public mmoMail plugin;
 	public Server server;
 	public MMO mmo;
 	public ArrayList<MailDB> maillist = new ArrayList<MailDB>();
 	public MailDB mail;
 	public Iterator<MailDB> iterator = null;
 	
 	public mail(mmoMail plugin){
 		this.plugin = plugin;
 	}
 	
 	public int getUnreadCount(String sender){
 		int count = 0;
 		for (MailDB mlDB : maillist){
 			if(sender.equals(mlDB.getReceiver())){
 				if(!mlDB.getRead()){
 					count++;
 				}
 			}
 		}
 		return count;
 	}
 	
 	public boolean sendMail(String sender, String receiver, String message) throws Exception{
 		mail = new MailDB();
 		mail.setSender(sender);
 		mail.setReceiver(receiver);
 		mail.setMessage(message);
 		maillist.add(mail);
 		mmo.notify(receiver, "You've got mail!", Material.PAPER);
 		return true;
 	}
 	public void getMail(Player player)throws Exception{
		for (MailDB myDB : maillist){
			if(myDB.getReceiver().equals(player.getName())) {
				mmo.sendMessage(player, "&aFrom: &c%1$s &a- &c%2$s", myDB.getSender(), myDB.getMessage());
				myDB.setRead(true);
				maillist.remove(myDB);
 			}
 		}
 	}
 }
