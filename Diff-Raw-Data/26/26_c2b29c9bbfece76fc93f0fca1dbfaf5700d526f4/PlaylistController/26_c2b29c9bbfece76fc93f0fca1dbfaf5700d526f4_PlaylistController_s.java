 package ch.hsr.mixtape.webapp.controller;
 
 import static ch.hsr.mixtape.application.ApplicationFactory.getPlaylistService;
 import static ch.hsr.mixtape.application.ApplicationFactory.getQueryService;
 
 import java.io.UnsupportedEncodingException;
 import java.security.Principal;
import java.util.HashMap;
 import java.util.List;
 import java.util.Locale;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.atmosphere.cpr.ApplicationConfig;
 import org.atmosphere.cpr.AtmosphereResource;
 import org.atmosphere.cpr.Broadcaster;
 import org.atmosphere.cpr.BroadcasterFactory;
 import org.atmosphere.cpr.FrameworkConfig;
 import org.atmosphere.cpr.HeaderConfig;
 import org.atmosphere.websocket.WebSocketEventListenerAdapter;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.propertyeditors.CustomCollectionEditor;
 import org.springframework.http.HttpStatus;
 import org.springframework.http.ResponseEntity;
 import org.springframework.mock.web.MockHttpServletResponse;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.ServletRequestDataBinder;
 import org.springframework.web.bind.annotation.ExceptionHandler;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 import org.springframework.web.bind.annotation.ResponseBody;
 import org.springframework.web.bind.annotation.ResponseStatus;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.View;
 import org.springframework.web.servlet.ViewResolver;
 
 import ch.hsr.mixtape.exception.InvalidPlaylistException;
 import ch.hsr.mixtape.exception.PlaylistChangedException;
 import ch.hsr.mixtape.model.PlaylistSettings;
 import ch.hsr.mixtape.model.Song;
 import ch.hsr.mixtape.webapp.GUIException;
 import ch.hsr.mixtape.webapp.MixtapeExceptionHandler;
 import ch.hsr.mixtape.webapp.MixtapeExceptionHandling;
 import ch.hsr.mixtape.webapp.NoOpView;
 
 /**
  * This controller is responsible for handling all playlist related requests.
  * 
  * @author Stefan Derungs
  */
 @Controller
 public class PlaylistController implements MixtapeExceptionHandling {
 
 	private static final Logger LOG = LoggerFactory
 			.getLogger(PlaylistController.class);
 
 	private static final String PLAYLIST_INITIALIZED_HEADER = "X-MixTape-isPlaylistInitialized";
 
 	private static final String ATMOSPHERE_PLAYLIST_PATH = "/playlist/push";
 
 	@Autowired
 	private ViewResolver viewResolver;
 
	private HashMap<AtmosphereResource, Principal> playlistSubscribers = new HashMap<AtmosphereResource, Principal>();

 	/**
 	 * @param term
 	 * @return songquery_viewhelper view
 	 */
 	@PreAuthorize("permitAll")
 	@RequestMapping(method = RequestMethod.GET, value = "/search")
 	public ModelAndView searchSong(
 			@RequestParam("term") String term,
 			@RequestParam(value = "maxResults", defaultValue = "0") int maxResults) {
 		return new ModelAndView("songquery_viewhelper", "queriedSongs",
 				getQueryService().findSongsByTerm(term, maxResults));
 	}
 
 	/**
 	 * @return In case an error occurred but no exception was thrown, the
 	 *         HTTP-Response-Header `Warning` contains error description.
 	 * @throws PlaylistChangedException
 	 * @throws InvalidPlaylistException
 	 * @throws GUIException
 	 */
 	@PreAuthorize("isAuthenticated and hasRole('ROLE_ADMIN')")
 	@RequestMapping(method = RequestMethod.POST, value = "/playlist/create")
 	public ResponseEntity<String> createPlaylist(
 			HttpServletRequest request,
 			HttpServletResponse response,
 			Principal principal,
 			@ModelAttribute("playlistSettings") PlaylistSettings playlistSettings)
 			throws GUIException, InvalidPlaylistException {
 
 		/*
 		 * Really dirty hack just made because error not found and not enought
 		 * time to fix the issue. Somehow Spring puts the first startSongs-entry
 		 * twice (once at the beginning and once at the end of the list) if the
 		 * list has more than one element.
 		 */
 		if (playlistSettings.getStartSongs().size() > 1)
 			playlistSettings.getStartSongs().remove(
 					playlistSettings.getStartSongs().size() - 1);
 
 		getPlaylistService().createPlaylist(playlistSettings);
 		notifyPlaylistSubscribers(request, response, principal);
 		return ControllerUtils.getResponseEntity(HttpStatus.OK);
 	}
 
 	/**
 	 * This {@link InitBinder} is needed for mapping selected songs in the
 	 * multiple-select-field to the actual instances of {@link Song} in the
 	 * {@link PlaylistSettings#startSongs} list.
 	 */
 	@InitBinder
 	protected void initBinder(HttpServletRequest request,
 			ServletRequestDataBinder binder) throws Exception {
 		binder.registerCustomEditor(List.class, new CustomCollectionEditor(
 				List.class) {
 			@Override
 			protected Object convertElement(Object element) {
 				String songId = (String) element;
 				return getQueryService().findObjectById(
 						Integer.parseInt(songId), Song.class);
 			}
 		});
 	}
 
 	/**
 	 * @see ch.hsr.mixtape.application.service.PlaylistService#alterSorting See
 	 *      PlaylistService.alterSorting for parameter information.
 	 * @return In case an error occurred but no exception was thrown, the
 	 *         HTTP-Response-Header `Warning` contains error description.
 	 * @throws PlaylistChangedException
 	 * @throws InvalidPlaylistException
 	 * @throws GUIException
 	 */
 	@PreAuthorize("isAuthenticated and hasRole('ROLE_ADMIN')")
 	@RequestMapping(method = RequestMethod.POST, value = "/playlist/sort")
 	public ResponseEntity<String> sortPlaylist(HttpServletRequest request,
 			HttpServletResponse response, Principal principal,
 			@RequestParam(value = "songId") int songId,
 			@RequestParam(value = "oldPosition") int oldPosition,
 			@RequestParam(value = "newPosition") int newPosition)
 			throws InvalidPlaylistException, PlaylistChangedException,
 			GUIException {
 
 		getPlaylistService().alterSorting(songId, oldPosition, newPosition);
 		notifyPlaylistSubscribers(request, response, principal);
 		return ControllerUtils.getResponseEntity(HttpStatus.OK);
 	}
 
 	/**
 	 * @see ch.hsr.mixtape.application.service.PlaylistService#removeSong See
 	 *      PlaylistService.alterSorting for parameter information.
 	 * @return In case an error occurred but no exception was thrown, the
 	 *         HTTP-Response-Header `Warning` contains error description.
 	 * @throws PlaylistChangedException
 	 * @throws InvalidPlaylistException
 	 * @throws GUIException
 	 */
 	@PreAuthorize("isAuthenticated and hasRole('ROLE_ADMIN')")
 	@RequestMapping(method = RequestMethod.POST, value = "/playlist/remove")
 	public ResponseEntity<String> removeSong(HttpServletRequest request,
 			HttpServletResponse response, Principal principal,
 			@RequestParam(value = "songId") int songId,
 			@RequestParam(value = "songPosition") int songPosition)
 			throws InvalidPlaylistException, PlaylistChangedException,
 			GUIException {
 
 		getPlaylistService().removeSong(songId, songPosition);
 		notifyPlaylistSubscribers(request, response, principal);
 		return ControllerUtils.getResponseEntity(HttpStatus.OK);
 	}
 
 	/**
 	 * @return If an error occured, the HTTP-Response-Header `Warning` contains
 	 *         error description.
 	 * @throws InvalidPlaylistException
 	 * @throws GUIException
 	 */
 	@PreAuthorize("permitAll")
 	@RequestMapping(method = RequestMethod.POST, value = "/playlist/wish")
 	public @ResponseBody
 	ResponseEntity<String> addWish(HttpServletRequest request,
 			HttpServletResponse response, Principal principal,
 			@RequestParam(value = "songId") int songId)
 			throws InvalidPlaylistException, GUIException {
 
 		getPlaylistService().addWish(songId);
 		notifyPlaylistSubscribers(request, response, principal);
 		return ControllerUtils.getResponseEntity(HttpStatus.OK);
 	}
 
 	/**
 	 * Atmosphere: This method takes a request to subscribe to the given
 	 * URL-path.
 	 * 
 	 * @source `subscribe` at [https
 	 *         ://github.com/Atmosphere/atmosphere-extensions/blob/master/spring
 	 *         /samples
 	 *         /spring-tiles/src/main/java/org/atmosphere/samples/pubsub/spring
 	 *         /PubSubController.java]
 	 * @param request
 	 * @return ModelAndView
 	 */
 	@PreAuthorize("permitAll")
 	@RequestMapping(value = ATMOSPHERE_PLAYLIST_PATH, method = RequestMethod.GET)
 	public ModelAndView subscribeToPlaylist(HttpServletRequest request,
 			Principal principal) throws Exception {
 		LOG.debug("Subscribing client to playlist updates.");
 		AtmosphereResource resource = (AtmosphereResource) request
 				.getAttribute(FrameworkConfig.ATMOSPHERE_RESOURCE);
 
		registerClient(resource, request, resource.getResponse(), principal);
 
 		// A NoOpView is returned to tell Spring Dispatcher framework not to
 		// render anything since it is all Atmosphere-related code.
 		return new ModelAndView(new NoOpView());
 	}
 
 	/**
 	 * Atmosphere: See AtmosphereHandlerPubSub example - same code as GET.
 	 * 
 	 * @param principal
 	 * 
 	 * @source `doGet` at [https
 	 *         ://github.com/Atmosphere/atmosphere-extensions/blob/master/spring
 	 *         /samples
 	 *         /spring-tiles/src/main/java/org/atmosphere/samples/pubsub/spring
 	 *         /PubSubController.java]
 	 */
 	private void registerClient(AtmosphereResource resource,
			HttpServletRequest request, HttpServletResponse response,
			Principal principal) {
 		LOG.debug("Registering client for playlist updates.");
 		// Log all events on the console, including WebSocket events.
 		resource.addEventListener(new WebSocketEventListenerAdapter());
 
 		response.setContentType("text/html;charset=UTF-8");
 		setPlaylistInitializedHeader(response);
 
 		Broadcaster broadcaster = lookupBroadcaster(request.getPathInfo());
 		resource.setBroadcaster(broadcaster);
 
 		String header = request.getHeader(HeaderConfig.X_ATMOSPHERE_TRANSPORT);
 		if (HeaderConfig.LONG_POLLING_TRANSPORT.equalsIgnoreCase(header)) {
 			request.setAttribute(ApplicationConfig.RESUME_ON_BROADCAST,
 					Boolean.TRUE);
 		}
 		resource.suspend(-1);

		playlistSubscribers.put(resource, principal);
 	}
 
 	/**
 	 * Atmosphere: See AtmosphereHandlerPubSub example - same code as POST
 	 * 
 	 * @see [https
 	 *      ://github.com/Atmosphere/atmosphere-extensions/blob/master/spring
 	 *      /samples
 	 *      /spring-tiles/src/main/java/org/atmosphere/samples/pubsub/spring
 	 *      /PubSubController.java]
 	 * @throws InvalidPlaylistException
 	 * @throws GUIException
 	 */
 	private void notifyPlaylistSubscribers(HttpServletRequest request,
 			HttpServletResponse response, Principal principal)
 			throws InvalidPlaylistException, GUIException {
 		final String errorMessage = "Notifying playlist subscribers failed: ";
 		try {
 			LOG.debug("Notifying playlist subscribers about an update.");
 
 			Broadcaster broadcaster = lookupBroadcaster(request.getPathInfo());
 
 			// http://stackoverflow.com/questions/9705293/render-multiple-views-within-a-single-request
 			View view = viewResolver.resolveViewName("playlist_viewhelper",
 					Locale.GERMAN);
 
 			ModelAndView playlistView = new ModelAndView(view);
 			try {
 				playlistView.addObject("playlist", getPlaylistService()
 						.getPlaylist());
 			} catch (InvalidPlaylistException e) {
 				playlistView.addObject("noPlaylist", true);
 			}
 
 			if (principal != null)
 				playlistView.addObject("isAuthenticated", true);
 
 			MockHttpServletResponse mockResponse = new MockHttpServletResponse();
 			playlistView.getView().render(playlistView.getModel(), request,
 					mockResponse);
 			setPlaylistInitializedHeader(mockResponse);
 
 			broadcaster.broadcast(mockResponse.getContentAsString());
 		} catch (UnsupportedEncodingException e) {
 			throw new GUIException(errorMessage + "Content could not be "
 					+ "retrieved from mock response.", e);
 		} catch (Exception e) {
 			throw new GUIException(
 					errorMessage + "View could not be resolved.", e);
 		}
 	}
 
 	/**
 	 * Retrieve the {@link Broadcaster} based on the request's path info.
 	 * 
 	 * @see [https
 	 *      ://github.com/Atmosphere/atmosphere-extensions/blob/master/spring
 	 *      /samples
 	 *      /spring-tiles/src/main/java/org/atmosphere/samples/pubsub/spring
 	 *      /PubSubController.java]
 	 * @param pathInfo
 	 * @return the {@link Broadcaster} based on the request's path info.
 	 */
 	private Broadcaster lookupBroadcaster(String pathInfo) {
 		LOG.debug("Looking up broadcaster");
 		if (pathInfo == null) {
 			return BroadcasterFactory.getDefault().lookup(
 					ATMOSPHERE_PLAYLIST_PATH, true);
 		} else {
 			String[] decodedPath = pathInfo.split(ATMOSPHERE_PLAYLIST_PATH);
 			return BroadcasterFactory.getDefault().lookup(
 					decodedPath[decodedPath.length - 1], true);
 		}
 	}
 
 	private void setPlaylistInitializedHeader(HttpServletResponse response) {
 		response.setHeader(PLAYLIST_INITIALIZED_HEADER,
 				Boolean.toString(getPlaylistService().isPlaylistInitialized()));
 	}
 
 	@Override
 	@ExceptionHandler(Exception.class)
 	@ResponseStatus(value = HttpStatus.BAD_REQUEST)
 	public @ResponseBody
 	ModelAndView handleException(Exception e) {
 		return MixtapeExceptionHandler.handleException(e, LOG);
 	}
 
 }
