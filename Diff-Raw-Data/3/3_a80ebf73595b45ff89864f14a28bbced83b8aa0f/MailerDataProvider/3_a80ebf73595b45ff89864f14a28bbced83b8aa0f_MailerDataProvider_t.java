 package net.abesto.treasurer.upload;
 
 import android.content.Context;
 import android.preference.PreferenceManager;
 import net.abesto.treasurer.R;
 
 public abstract class MailerDataProvider extends DataProvider {
     public static class RecipientNotSetException extends DataProvider.InvalidConfigurationException {
         public RecipientNotSetException(String detailMessage) {
             super(detailMessage);
         }
     }
 
 	public MailerDataProvider(Context context, UploadData data) throws InvalidConfigurationException {
 		super(context, data);
         ensureRecipientSet();
 	}
 
     public abstract String getTitle();
 	public abstract String getBody();
 	public abstract String getAttachmentFilename();
 	public abstract String getAttachmentText();
 
     public String getRecipient() {
        return PreferenceManager.getDefaultSharedPreferences(context).getString(
                context.getResources().getString(R.string.pref_emailTo_key), "");
     }
 
     private void ensureRecipientSet() throws RecipientNotSetException {
         if (PreferenceManager.getDefaultSharedPreferences(context).getString(
                 context.getResources().getString(R.string.pref_emailTo_key), "").isEmpty())
         {
             throw new RecipientNotSetException(context.getResources().getString(R.string.must_set_email_recipient));
         }
     }
 }
