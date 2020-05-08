 package directi.androidteam.training.chatclient.Roster;
 
 import android.graphics.Bitmap;
 import android.graphics.BitmapFactory;
 import android.util.Log;
import directi.androidteam.training.ChatApplication;
 import directi.androidteam.training.TagStore.Tag;
import directi.androidteam.training.chatclient.R;
 import directi.androidteam.training.chatclient.Util.Base64;
 
 import java.io.IOException;
 
 /**
  * Created with IntelliJ IDEA.
  * User: rajat
  * Date: 10/2/12
  * Time: 12:58 PM
  * To change this template use File | Settings | File Templates.
  */
 public class VCard {
     private String name;
     private String avatarType;
     private Bitmap avatar;
 
     public VCard() {
         this.name = "";
        this.avatar = BitmapFactory.decodeResource(ChatApplication.getAppContext().getResources(), R.drawable.default_user);
     }
 
     public Bitmap getAvatar() {
         return this.avatar;
     }
 
     public void setAvatar(Bitmap avatar) {
         this.avatar = avatar;
     }
 
     public String getName() {
         return this.name;
     }
 
     public void populateFromTag(Tag tag) {
         if (tag.getChildTags() != null) {
             tag = tag.getChildTag("vCard");
             if (tag.getChildTags() != null) {
                 Tag FN = tag.getChildTag("FN");
                 if (FN.getTagname() != null) {
                     this.name = FN.getContent();
                 }
                 Tag PHOTO = tag.getChildTag("PHOTO");
                 if (PHOTO.getChildTags() != null) {
                     Tag TYPE = PHOTO.getChildTag("TYPE");
                     this.avatarType = TYPE.getContent().split("/")[1];
                     Tag BINVAL = PHOTO.getChildTag("BINVAL");
                     String avatarBinVal = BINVAL.getContent();
                     this.avatar = decodeAvatar(avatarBinVal);
                 }
             }
         }
     }
 
     public Bitmap decodeAvatar(String encodedAvatar) {
         try {
             byte [] decodedAvatarBinVal = Base64.decode(encodedAvatar);
             return BitmapFactory.decodeByteArray(decodedAvatarBinVal, 0, decodedAvatarBinVal.length);
         } catch (IOException e) {
             Log.d("IOException", e.toString());
         }
         return null;
     }
 }
