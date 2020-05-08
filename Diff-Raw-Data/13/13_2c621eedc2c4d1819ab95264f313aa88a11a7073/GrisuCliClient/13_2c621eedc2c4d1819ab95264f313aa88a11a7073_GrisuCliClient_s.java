 package grisu.frontend.view.cli;
 
 import grisu.control.ServiceInterface;
 import grisu.frontend.control.login.LoginException;
 import grisu.frontend.control.login.LoginManagerNew;
 import grith.gridsession.GridClient;
 import grith.jgrith.cred.GridLoginParameters;
 
 public class GrisuCliClient<T extends GrisuCliParameters> extends GridClient {
 
 
	public static void main(String[] args) {
 
 		GrisuCliClient<DefaultCliParameters> gcc = new GrisuCliClient<DefaultCliParameters>(new DefaultCliParameters(),
 				args);
 
		gcc.start();
 
 	}
 
 	// protected final String[] args;
 
 	private ServiceInterface si;
 
 
 	private final T cliParams;
 
	public GrisuCliClient(T params) {
 		super(GridLoginParameters.createFromGridCliParameters(params));
 		this.cliParams = params;
 	}
 
 
	public GrisuCliClient(T params, String[] args) {
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
 			si = LoginManagerNew.login(getLoginParameters().getBackend(),
 					getCredential(), true);
 		}
 		return si;
 	}
 
	@Override
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
