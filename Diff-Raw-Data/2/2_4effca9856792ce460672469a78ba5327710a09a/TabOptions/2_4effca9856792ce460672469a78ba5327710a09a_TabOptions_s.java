 package tabber;
 
 public class TabOptions {
 	public boolean delete_files = false;
 	public boolean interactive = false;
 	public boolean delete_empty_folders = false;
 	public boolean remote_source = false;
 	
 	public TabOptions(boolean _delete_files, boolean _interactive, boolean _delete_empty_folders, boolean _remote_source) {
 		delete_files = _delete_files;
 		interactive = _interactive;
 		delete_empty_folders = _delete_empty_folders;
 		remote_source = _remote_source;
 	}
 	public TabOptions(char params) { Deserialize(params); }
 	public TabOptions() {}
 	
 	public String Serialize() {
		byte ret = 0;
 		ret |= bc(delete_files) << 0;
 		ret |= bc(interactive) << 1;
 		ret |= bc(delete_empty_folders) << 2;
 		ret |= bc(remote_source) << 3;
 
 		return ((char)ret) + "";
 	}
 	
 	void Deserialize(char params) {
 		byte temp = (byte)params;
 		delete_files = cb(temp & 1);
 		interactive = cb(temp >> 1 & 1);
 		delete_empty_folders = cb(temp >> 2 & 1);
 		remote_source = cb(temp >> 3 & 1);		
 	}
 	
 	int bc(boolean v) {	return v ? 1 : 0; }
 	boolean cb(int v) {	return v == 1; }
 }
