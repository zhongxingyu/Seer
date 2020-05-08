 package gov.va.med.iss.connection;
 
 import java.util.List;
 
 import us.pwc.vista.eclipse.core.ServerData;
 import gov.va.med.foundations.adapter.cci.VistaLinkConnection;
 import gov.va.med.foundations.rpc.RpcRequest;
 import gov.va.med.foundations.rpc.RpcRequestFactory;
 import gov.va.med.foundations.rpc.RpcRequestParams;
 import gov.va.med.foundations.rpc.RpcResponse;
 import gov.va.med.foundations.security.vistalink.EclipseConnection;
 import gov.va.med.foundations.utilities.FoundationsException;
 
 public class VistAConnection {
 	private ServerData serverData;
 	private VistaLinkConnection connection;
 	private EclipseConnection eclipseConnection;
 			
	public VistAConnection(ServerData serverData, VistaLinkConnection connection, EclipseConnection eclipseConnection) {
 		super();
 		this.serverData = serverData;
 		this.connection = connection;
 		this.eclipseConnection = eclipseConnection;
 	}
 
 	public ServerData getServerData() {
 		return this.serverData;
 	}
 	
 	VistaLinkConnection getConnection() {
 		return connection;
 	}
 
	public EclipseConnection getEclipseConnection() {
 		return eclipseConnection;
 	}
 
 	public String getServerName() {
 		return this.serverData.getName();
 	}
 	
 	private String rpc(String rpcName, boolean propprietaryFormat, RPCParam... rpcParams) throws FoundationsException {
 		RpcRequest vReq = RpcRequestFactory.getRpcRequest("", rpcName);
 		vReq.setUseProprietaryMessageFormat(propprietaryFormat);
 		RpcRequestParams params = vReq.getParams();
 		for (int i=1; i<=rpcParams.length; ++i) {
 			RPCParam rpcParam = rpcParams[i-1];
 			params.setParam(i, rpcParam.getType(), rpcParam.getValue());
 		}
 		RpcResponse vResp = this.connection.executeRPC(vReq);
 		String result = vResp.getResults();
 		return result;
 	}
 
 	public String rpc(String rpcName, RPCParam... rpcParams) throws FoundationsException {
 		return this.rpc(rpcName, true, rpcParams);
 	}
 
 	public String rpcXML(String rpcName, RPCParam... rpcParams) throws FoundationsException {
 		return this.rpc(rpcName, false, rpcParams);
 	}
 
 	public String rpc(String rpcName, String param0, List<String> param1, String... paramsRest) throws FoundationsException {
 		int n = paramsRest.length;
 		RPCParam[] rpcParams = new RPCParam[n+2];
 		rpcParams[0] = RPCParam.valueOf(param0);
 		rpcParams[1] = RPCParam.valueOf(param1);
 		for (int i=0; i<n; ++i) {
 			rpcParams[i+2] = RPCParam.valueOf(paramsRest[i]);	
 		}
 		return this.rpc(rpcName, true, rpcParams);
 	}
 
 	public String rpcXML(String rpcName, String... paramNames) throws FoundationsException {
 		RPCParam[] rpcParams = RPCParam.valueOf(paramNames);
 		return this.rpcXML(rpcName, rpcParams);
 	}
 }
