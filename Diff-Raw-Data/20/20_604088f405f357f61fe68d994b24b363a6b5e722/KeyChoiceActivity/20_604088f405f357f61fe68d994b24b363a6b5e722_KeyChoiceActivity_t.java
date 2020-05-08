 package cryptocast.client;
 
 import java.io.File;
 
 import cryptocast.client.fileChooser.FileChooser;
 import cryptocast.client.fileChooser.ListElement;
 
 import com.google.common.base.Optional;
 
 import android.support.v4.app.FragmentActivity;
 
 /**
  * This activity lets a user choose an encryption key file
  * which is then sent to the server for authentication.
  */
 public class KeyChoiceActivity extends FileChooser {
     private File chosenFile;
 
     /** @return The chosen file or absent on abort. */
     public Optional<File> getChosenFile() {
     }
 
    /** Called when the user clicks a file in the list.
      * @param item The clicked list item.
      */
     @Override
     public void onFileClick(ListElement item) {
     }
 }
