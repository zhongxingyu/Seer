 package jrds.probe;
 
 import java.rmi.RemoteException;
 import java.util.ArrayList;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map;
 
 import jrds.ProbeConnected;
 import jrds.agent.RProbe;
 
 import org.apache.log4j.Level;
 
 public class RMI extends ProbeConnected<String, Number, RMIConnection> {
 	List<?> args = new ArrayList<Object>(0);
 	private String remoteName = null;
 
 	public RMI() {
 		super(RMIConnection.class.getName());
 	}
 
 	public Map<String, Number> getNewSampleValuesConnected(RMIConnection cnx) {
 		Map<String, Number> retValues = new HashMap<String, Number>(0);
 		try {
 			RProbe rp = (RProbe) cnx.getConnection();
 			remoteName = rp.prepare(getPd().getSpecific("remote"), args);
 			retValues = rp.query(remoteName);
 		} catch (RemoteException e) {
			Throwable root = e.getCause().getCause();
			if(root == null)
				root = e.getCause();
 			log(Level.ERROR, root, "Remote exception on server: %s", root);
 
 		}
 		return retValues;
 	}
 
 	public List<?> getArgs() {
 		return args;
 	}
 
 	public void setArgs(List<?> l) {
 		this.args = l;
 	}
 
 	public String getSourceType() {
 		return "JRDS Agent";
 	}
 }
