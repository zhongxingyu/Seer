 package net.bodz.redist.installer;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 import java.util.NoSuchElementException;
 
 import net.bodz.bas.gui.dialog.IUserDialogs;
 import net.bodz.bas.log.Logger;
 import net.bodz.bas.sio.IPrintOut;
import net.bodz.bas.vfs.IFsEntry;
 import net.bodz.bas.vfs.IFsDir;
 import net.bodz.redist.installer.util.Flags;
 
 public interface ISession {
 
     IProject getProject();
 
     Components getComponents();
 
     IComponent getComponent(String id);
 
     Scheme getScheme();
 
     void setScheme(String schemeName);
 
     void setScheme(Scheme scheme);
 
     IUserDialogs getUserDialogs();
 
     Logger getLogger();
 
     // XXX - ...
     void setLogger(Logger logger);
 
     int REBOOT = 1;
 
     Flags getFlags();
 
     void set(String variableName, Object variableValue);
 
     Object get(String variableName);
 
     /**
      * @throws IllegalArgumentException
      *             if variableName isn't defined.
      * @return never <code>null</code>
      */
     File getFile(String variableName);
 
     String expand(String s);
 
     List<IFsDir> getResFolders();
 
     boolean addResFolder(IFsDir resFolder);
 
     /**
      * @param beforeIndex
      *            set to 0 if you want to make the resFolder as default output.
      */
     void addResFolder(int beforeIndex, IFsDir resFolder);
 
     IFsEntry newResource(String resPath)
             throws IOException;
 
     /**
      * @throws NoSuchElementException
      *             if specified resource isn't existed.
      */
     IFsEntry findResource(String resPath, boolean autoCreate)
             throws IOException, NoSuchElementException;
 
     /**
      * Return attachment from pool, or get a new one.
      * 
      * @param name
      *            name of the attachment to use
      * @param autoCreate
      *            create new attachment resource if not existed
      * @return {@link Attachment} which can be opened later. After used the attachment, it can be
      *         left opened for next time use, all unclosed attachments are auto closed at the end of
      *         the session. The return value is never be <code>null</code>.
      * @throws IOException
      *             if the name is invalid or failed to get the attachment.
      */
     Attachment getAttachment(String name, boolean autoCreate)
             throws IOException;
 
     /**
      * Close all attachments.
      */
     void closeAttachments();
 
     /**
      * Load and scatter registry data into components.
      */
     void loadRegistry()
             throws SessionException;
 
     /**
      * Gather and persist registry data from components.
      */
     void saveRegistry()
             throws SessionException;
 
     void dump(IPrintOut out);
 
 }
