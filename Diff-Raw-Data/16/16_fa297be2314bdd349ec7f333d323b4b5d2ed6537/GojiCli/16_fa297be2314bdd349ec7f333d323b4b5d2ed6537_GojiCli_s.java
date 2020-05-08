 package nz.org.nesi.goji.view.cli;
 
 import grisu.jcommons.interfaces.GrinformationManagerDozer;
 import grisu.jcommons.interfaces.InformationManager;
 import grisu.jcommons.utils.EnvironmentVariableHelpers;
 import grisu.jcommons.utils.OutputHelpers;
 import grisu.model.info.dto.Directory;
 import grith.gridsession.GridClient;
 import grith.jgrith.Environment;
 import grith.jgrith.cred.AbstractCred;
 import grith.jgrith.cred.GridLoginParameters;
 import grith.jgrith.voms.VOManagement.VOManager;
 
 import java.text.SimpleDateFormat;
 import java.util.Collection;
 import java.util.Date;
 import java.util.List;
 import java.util.Set;
 
 import nz.org.nesi.goji.control.GlobusOnlineUserSession;
 import nz.org.nesi.goji.exceptions.CommandException;
 import nz.org.nesi.goji.exceptions.FileSystemException;
 import nz.org.nesi.goji.model.Endpoint;
 import nz.org.nesi.goji.model.events.EndpointActivatedEvent;
 import nz.org.nesi.goji.model.events.EndpointActivatingEvent;
 import nz.org.nesi.goji.model.events.EndpointDeactivatedEvent;
 import nz.org.nesi.goji.model.events.EndpointDeactivatingEvent;
 import nz.org.nesi.goji.model.events.EndpointEvent;
 
 import org.apache.commons.lang3.StringUtils;
 
 import com.beust.jcommander.JCommander;
 import com.google.common.base.Function;
 import com.google.common.collect.Collections2;
 import com.google.common.collect.Lists;
 import com.google.common.collect.Sets;
 import com.google.common.eventbus.Subscribe;
 
 public class GojiCli extends GridClient {
 
 	final static SimpleDateFormat format = new SimpleDateFormat(
 			"dd.MM.yy hh:mm a");
 
 	public static final String ALL_ENDPOINTS = "all";
 	public static final String ACTIVATE = "activate";
 	public static final String DEACTIVATE = "deactivate";
 	public static final String LIST = "list";
 
 	public static void main(String[] args) throws Exception {
 
 		EnvironmentVariableHelpers.loadEnvironmentVariablesToSystemProperties();
 
 		Environment.initEnvironment();
 
 		GojiMainParameters mp = new GojiMainParameters();
 		JCommander jc = new JCommander(mp);
 
 		GojiCli goji = new GojiCli(mp, args);
 
 		// System.out.println(goji.getCredential().getDN());
 		System.exit(0);
 	}
 
 	private final GojiMainParameters mainParameters;
 	private final GojiEndpointParameters endpointParameters;
 	private final GojiActivateParameters activateParameters;
 	private final GojiDeactivateParameters deactivateParameters;
 
 	private final GlobusOnlineUserSession session;
 	private final InformationManager informationManager;
 
 	private final String info_address;
 	private final String command;
 
 	private final String go_username;
 	private final String endpoint_username;
 
 	public GojiCli(GojiMainParameters params, String[] args) throws Exception {
 		super(GridLoginParameters
 				.createFromCommandlineArgs(params, args, false));
 
 		this.mainParameters = params;
 
 		JCommander jc = new JCommander(this.mainParameters);
 
 		endpointParameters = new GojiEndpointParameters();
 		jc.addCommand(LIST, endpointParameters);
 		activateParameters = new GojiActivateParameters();
 		jc.addCommand(ACTIVATE, activateParameters);
 		deactivateParameters = new GojiDeactivateParameters();
 		jc.addCommand(DEACTIVATE, deactivateParameters);
 
 		jc.parse(args);
 
 		command = jc.getParsedCommand();
 
 		if (this.mainParameters.isHelp()) {
 			if (StringUtils.isNotBlank(command)) {
 				jc.usage(command);
 				System.exit(0);
 			} else {
 				jc.usage();
 				System.exit(0);
 			}
 		}
 
 		info_address = params.getInfoFile();
 		endpoint_username = params.getEndpointUsername();
 		go_username = params.getGoUsername();
 
 		if (StringUtils.isBlank(go_username)) {
 			System.err.println("Please provide your GlobusOnline username.");
 			jc.usage();
 			System.exit(1);
 		}
 
 		informationManager = new GrinformationManagerDozer(info_address);
 		AbstractCred.DEFAULT_VO_MANAGER = new VOManager(informationManager);
 
 		session = new GlobusOnlineUserSession(go_username, endpoint_username,
 				getCredential(), informationManager);
 
 		session.registerForEvents(this);
 
 		try {
 			if (ACTIVATE.equals(command)) {
 				activate(true);
 			} else if (DEACTIVATE.equals(command)) {
 				deactivate();
 			} else if (LIST.equals(command)) {
 				list();
 			}
 		} catch (CommandException ce) {
 			ce.printStackTrace();
 		}
 
 	}
 
 	private Set<Endpoint> getRequestedEndpoints(List<String> endpoints)
 			throws CommandException {
 
 		if (endpoints.contains(ALL_ENDPOINTS) || endpoints.size() == 0) {
 			return session.getAllUserEndpoints();
 		}
 
 		Set<Endpoint> result = Sets.newLinkedHashSet();
 		for (String ep : endpoints) {
 			Endpoint endpoint = session.getEndpoint(ep);
 			if (endpoint == null) {
 				throw new CommandException("Endpoint '" + ep
 						+ "' does not exist.");
 			}
 			result.add(endpoint);
 		}
 		return result;
 	}
 
 	private Collection<Directory> getRequestedDirectories(List<String> endpoints)
 			throws CommandException {
 
 		Set<Endpoint> result = Sets.newLinkedHashSet();
 		if (endpoints.contains(ALL_ENDPOINTS) || endpoints.size() == 0) {
 			result = session.getAllUserEndpoints();
 		} else {
 
 			for (String ep : endpoints) {
 				Endpoint endpoint = session.getEndpoint(ep);
 				if (endpoint == null) {
 					throw new CommandException("Endpoint '" + ep
 							+ "' does not exist.");
 				}
 				result.add(endpoint);
 			}
 		}
 
 		result = session.filterUnusableEndpoints(result);
 
 		return Collections2.transform(result,
 				new Function<Endpoint, Directory>() {
 
 					public Directory apply(Endpoint input) {
 						try {
 							return session.getDirectory(input.getName());
 						} catch (FileSystemException e) {
 							throw new RuntimeException(
 									"Can't get directory for: "
 											+ input.getName());
 						}
 					}
 				});
 	}
 
 	private Collection<String> getRequestedEndpointNames(List<String> endpoints)
 			throws CommandException {
 
 		return Collections2.transform(getRequestedEndpoints(endpoints),
 				new Function<Endpoint, String>() {
 
 					public String apply(Endpoint input) {
 						return input.getUsername() + "#" + input.getName();
 					}
 				});
 	}
 
 	private void activate(boolean waitToFinish) throws CommandException {
 
 		Collection<Directory> endpoints = getRequestedDirectories(activateParameters
 				.getEndpoints());
 
 		session.activateEndpoints(endpoint_username, endpoints, false,
 				waitToFinish, false);
 
 		System.out.println("Endpoints activated");
 
 	}
 
 	private void deactivate() throws CommandException {
 
 		Collection<String> endpoints = getRequestedEndpointNames(deactivateParameters
 				.getEndpoints());
 
 		session.deactivateEndpoints(endpoints, true);
 
 		System.out.println("Endpoints deactivated");
 
 	}
 
 	private void list() throws CommandException {
 
 		Set<Endpoint> endpoints = getRequestedEndpoints(endpointParameters
 				.getEndpoints());
 
 		endpoints = session.filterUnusableEndpoints(endpoints);
 
 		List<List<String>> output = Lists.newArrayList();
 
 		List<String> temp = Lists.newLinkedList();
 		temp.add("Endpoint");
 		temp.add("Is active");
 		temp.add("Expires");
 		output.add(temp);
 
 		for (Endpoint ep : endpoints) {
 			Boolean active = ep.isActivated();
 			Date timeLeft = ep.getExpires();
 			String name = ep.getName();
 			temp = Lists.newLinkedList();
 			temp.add(name);
 			temp.add(active.toString());
 			if (active) {
 				temp.add(format.format(timeLeft).toLowerCase());
 			} else {
 				temp.add("n/a");
 			}
 			output.add(temp);
 		}
 
 		System.out.println(OutputHelpers.getTable(output, true));
 	}
 
 	@Subscribe
 	public void handleEndpointEvent(EndpointEvent event) {
 		Endpoint ep = null;
 		try {
 			ep = session.getEndpoint(event.getEndpoint());
 		} catch (CommandException e) {
 			e.printStackTrace();
 			return;
 		}
 		if (event instanceof EndpointActivatingEvent) {
 			System.out.println("Activating endpoint: " + ep.getName());
 		} else if (event instanceof EndpointActivatedEvent) {
 			System.out.println("Endpoint activated: " + ep.getName());
 		} else if (event instanceof EndpointDeactivatingEvent) {
 			System.out.println("Deactivating endpoint: " + ep.getName());
 		} else if (event instanceof EndpointDeactivatedEvent) {
 			System.out.println("Endpoint deactivated: " + ep.getName());
 		}
 	}
 }
