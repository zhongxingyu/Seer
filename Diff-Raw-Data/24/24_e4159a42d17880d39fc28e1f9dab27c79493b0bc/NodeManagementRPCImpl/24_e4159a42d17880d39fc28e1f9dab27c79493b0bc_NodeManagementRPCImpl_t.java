 package edu.berkeley.grippus.server;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 
 import com.caucho.hessian.server.HessianServlet;
 
 import edu.berkeley.grippus.Errno;
 import edu.berkeley.grippus.fs.DFileSpec;
 import edu.berkeley.grippus.fs.VFS;
 
 public class NodeManagementRPCImpl extends HessianServlet implements NodeManagementRPC {
 	private static final long serialVersionUID = 1L;
 	private Node managedNode;
 
 	public NodeManagementRPCImpl() { /* do nothing */ }
 
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		super.init(config);
 		managedNode = (Node) config.getServletContext().getAttribute("node");
 	}
 
 	@Override
 	public String version(String cmd) {
 		return "0.1";
 	}
 
 	@Override
 	public String terminate(String cmd) {
 		managedNode.terminate();
 		return "Success";
 	}
 
 	@Override
 	public String status(String cmd) {
 		return managedNode.status();
 	}
 
 	@Override
 	public Errno initCluster(String cmd, String clusterName) {
		return managedNode.initCluster(clusterName);
 	}
 
 	@Override
 	public String disconnect(String cmd) {
 		managedNode.disconnect();
 		return status(cmd);
 	}
 
 	@Override
 	public String ls(String cmd, DFileSpec path) {
 		VFS vfs = managedNode.getVFS();
 		StringBuilder result = new StringBuilder();
 		result.append(path+":\n");
 		for (String f : vfs.ls(vfs.resolve(path)).keySet()) {
 			result.append(f);
 			result.append("\n");
 		}
 		return result.toString();
 	}
 
 	@Override
 	public Errno mkdir(String cmd, DFileSpec dir) {
 		return managedNode.getVFS().find(dir).mkdir();
 	}
 
 	@Override
 	public DFileSpec canonicalizePath(DFileSpec path) {
 		return managedNode.getVFS().canonicalize(path);
 	}
 
 	@Override
 	public Errno mount(String cmd, String realPath, String vPath) {
 		DFileSpec dfs = new DFileSpec(vPath);
 		return managedNode.getVFS().mount(dfs, realPath);
 	}
 	
 	public Errno connectToNetwork(String cmd, String masterURL, String clusterPassword) {
 		return managedNode.connectToServer(masterURL, clusterPassword);
 	}
 
 	@Override
 	public Errno share(String cmd, String realPath, String vPath) {
 		DFileSpec dfs = new DFileSpec(vPath);
 		return managedNode.share(dfs, realPath);
 	}
 }
