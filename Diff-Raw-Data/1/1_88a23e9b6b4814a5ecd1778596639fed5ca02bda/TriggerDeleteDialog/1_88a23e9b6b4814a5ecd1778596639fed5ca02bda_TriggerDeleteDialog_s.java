 package de.kumpelblase2.dragonslair.commanddialogs.trigger;
 
 import org.bukkit.ChatColor;
 import org.bukkit.conversations.*;
 import de.kumpelblase2.dragonslair.DragonsLairMain;
 
 public class TriggerDeleteDialog extends ValidatingPrompt
 {
 	@Override
 	public String getPromptText(final ConversationContext arg0)
 	{
 		if(arg0.getSessionData("trigger_id") == null)
 			return ChatColor.GREEN + "Please enter the id of the trigger to delete:";
 		else
 			return ChatColor.YELLOW + "Are you sure you want to delete this trigger? Type 'delete' to confirm.";
 	}
 
 	@Override
 	protected Prompt acceptValidatedInput(final ConversationContext arg0, final String arg1)
 	{
 		if(arg1.equals("cancel"))
 		{
 			arg0.setSessionData("trigger_id", null);
 			return new TriggerManageDialog();
 		}
 		if(arg0.getSessionData("trigger_id") == null)
 		{
 			if(arg1.equals("back"))
 				return new TriggerManageDialog();
 			arg0.setSessionData("trigger_id", Integer.parseInt(arg1));
 			return this;
 		}
 		else
 		{
 			if(arg1.equals("back"))
 			{
 				arg0.setSessionData("trigger_id", null);
 				return this;
 			}
 			if(arg1.equals("delete"))
 			{
 				final int id = (Integer)arg0.getSessionData("trigger_id");
 				DragonsLairMain.debugLog("Deleted trigger with id '" + id + "'");
 				DragonsLairMain.getSettings().getTriggers().remove(id);
 				DragonsLairMain.getInstance().getEventHandler().reloadTriggers();
 			}
 			arg0.setSessionData("trigger_id", null);
 			return new TriggerManageDialog();
 		}
 	}
 
 	@Override
 	protected boolean isInputValid(final ConversationContext arg0, final String arg1)
 	{
 		if(arg1.equals("back") || arg1.equals("cancel"))
 			return true;
 		if(arg0.getSessionData("trigger_id") == null)
 			try
 			{
 				final int id = Integer.parseInt(arg1);
 				if(!DragonsLairMain.getSettings().getTriggers().containsKey(id))
 				{
 					arg0.getForWhom().sendRawMessage(ChatColor.RED + "That trigger does not exist.");
 					return false;
 				}
 				else
 					return true;
 			}
 			catch(final Exception e)
 			{
 				arg0.getForWhom().sendRawMessage(ChatColor.RED + "Not a valid number.");
 				return false;
 			}
 		else
 		{
 			if(arg1.length() > 0)
 				return true;
 			return false;
 		}
 	}
 }
