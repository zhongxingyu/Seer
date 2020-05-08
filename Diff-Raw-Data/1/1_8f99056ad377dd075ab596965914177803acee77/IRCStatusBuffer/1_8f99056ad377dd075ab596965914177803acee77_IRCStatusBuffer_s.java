 package xtreeki.irc;
 
 import android.support.v4.app.Fragment;
 
 /**
  * Created with IntelliJ IDEA.
  * User: me
  * Date: 5/21/13
  * Time: 3:51 PM
  * To change this template use File | Settings | File Templates.
  */
 public class IRCStatusBuffer extends IRCBuffer {
 	public IRCStatusBuffer(Connection connection) {
 		super(connection);
 	}
 
 	@Override
 	public Fragment createFragment() {
 		return new BufferFragment(this);
 	}
 
 	@Override
 	public String getActionBarTitle() {
 		// This title doesn't matter in the context where we use it, so just return null?
 		return null;
 	}
 
 	@Override
 	public String getTitle() {
 		return mConnection.getConfig().networkName;
 	}
 
 	@Override
 	public void writeText(CharSequence text) {
 	}
 }
