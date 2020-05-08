 package ru.tulupov.nsuconnect.protocol.commands;
 
 import android.content.Intent;
 
 import java.sql.SQLException;
 
 import ru.tulupov.nsuconnect.database.ContentUriHelper;
 import ru.tulupov.nsuconnect.database.DatabaseConstants;
 import ru.tulupov.nsuconnect.database.HelperFactory;
 import ru.tulupov.nsuconnect.helper.SoundHelper;
 import ru.tulupov.nsuconnect.protocol.Command;
 import ru.tulupov.nsuconnect.protocol.CommandContext;
 import ru.tulupov.nsuconnect.service.DataService;
 
 public class DisconnectCommand extends SystemCommand {
     @Override
     public void execute(CommandContext context) {
         SoundHelper.beep();
         writeSystem(context, "Отключено");
         context.getApplicationContext().sendBroadcast(new Intent(DatabaseConstants.ACTION_UPDATE_TYPING_STATUS).putExtra(DatabaseConstants.EXTRA_IS_TYPING, false));
 
 
         context.getApplicationContext().startService(new Intent(context.getApplicationContext(), DataService.class)
                 .setAction(DataService.ACTION_DESTROY_SESSION).putExtra(DataService.EXTRA_ID, context.getChat().getId()));
 
 
         context.getChat().setActive(false);
         try {
 
            HelperFactory.getHelper().getChatDao().update(context.getChat());
             ContentUriHelper.notifyChange(context.getApplicationContext(), ContentUriHelper.getChatUri());
 
 
         } catch (SQLException e) {
 
         }
     }
 }
