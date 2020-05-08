 package telnet;
 
 import java.util.ArrayList;
 
 public class CommandRecall {
 
 	private ArrayList<String> cmds = new ArrayList<String>();
 	private int pos = 0;
 	private static CommandRecall instance = new CommandRecall();
 
 	public static CommandRecall getInstance() {
 		return instance;
 	}
 
 	public String next() {
 		pos += pos >= cmds.size() ? 0 : 1;
 		return pos < cmds.size() ? cmds.get(pos) : "";
 	}
 
 	public String prev() {
 		pos -= pos == 0 ? 0 : 1;
 		return pos >= 0 && cmds.size() > 0 ? cmds.get(pos) : "";
 	}
 
 	public void add(String cmd) {
 		if (cmd != null && !cmd.equals("")
 				&& (cmds.size() == 0 || !cmds.get(cmds.size() - 1).equals(cmd))) {
 			cmds.add(cmd);
			pos = cmds.size();
 		}
 	}
 }
