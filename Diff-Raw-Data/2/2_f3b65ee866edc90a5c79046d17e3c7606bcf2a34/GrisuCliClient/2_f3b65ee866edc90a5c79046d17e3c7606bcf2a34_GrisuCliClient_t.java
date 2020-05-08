 package grisu.frontend.view.cli;
 
 import grisu.control.ServiceInterface;
 import grisu.frontend.control.login.LoginException;
 import grisu.frontend.control.login.LoginManagerNew;
 import grith.gridsession.GridClient;
 import grith.jgrith.cred.GridLoginParameters;
 
 public class GrisuCliClient<T extends GrisuCliParameters> extends GridClient {
 
 
 	public static void main(String[] args) throws Exception {
 
 		GrisuCliClient<DefaultCliParameters> gcc = new GrisuCliClient<DefaultCliParameters>(new DefaultCliParameters(),
 				args);
 
 		gcc.run();
 
 	}
 
 	// protected final String[] args;
 
 	private ServiceInterface si;
 
 
 	private final T cliParams;
 
 	public GrisuCliClient(T params) throws Exception {
 		super(GridLoginParameters.createFromGridCliParameters(params));
 		this.cliParams = params;
 	}
 
 
 	public GrisuCliClient(T params, String[] args) throws Exception {
 		super(GridLoginParameters.createFromCommandlineArgs(params, args));
 		// this.args = args;
 		this.cliParams = params;
 
 		LoginManagerNew.initEnvironment();
 	}
 
 	public T getCliParameters() {
 
 		return this.cliParams;
 	}
 
 
 
 	public ServiceInterface getServiceInterface() throws LoginException {
 		if ((si == null) || getLoginParameters().isNologin()) {
			si = LoginManagerNew.login(getCliParameters().getBackend(),
 					getCredential(), true);
 		}
 		return si;
 	}
 
 	protected void run() {
 
 		System.out.println("Example grisu client.");
 		ServiceInterface si = null;
 		try {
 			si = getServiceInterface();
 		} catch (LoginException e) {
 			e.printStackTrace();
 			throw new RuntimeException(e);
 		}
 		System.out.println("Grid identity: " + si.getDN());
 		System.out.println("Backend info:");
 		System.out.println(si.getInterfaceInfo(null));
 
 	}
 
 
 }
