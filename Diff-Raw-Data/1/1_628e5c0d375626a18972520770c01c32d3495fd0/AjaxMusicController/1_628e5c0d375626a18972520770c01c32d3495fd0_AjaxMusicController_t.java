 package org.mashupmedia.controller.ajax;
 
 import java.util.ArrayList;
 import java.util.List;
 
 import org.apache.commons.lang3.BooleanUtils;
 import org.apache.commons.lang3.SerializationUtils;
 import org.mashupmedia.constants.MashUpMediaConstants;
 import org.mashupmedia.model.User;
 import org.mashupmedia.model.media.Album;
 import org.mashupmedia.model.media.Artist;
 import org.mashupmedia.model.media.MediaItem;
 import org.mashupmedia.model.media.MediaItem.MediaType;
 import org.mashupmedia.model.media.Song;
 import org.mashupmedia.model.playlist.Playlist;
 import org.mashupmedia.model.playlist.Playlist.PlaylistType;
 import org.mashupmedia.model.playlist.PlaylistMediaItem;
 import org.mashupmedia.restful.DiscogsWebServiceImpl;
 import org.mashupmedia.service.AdminManager;
 import org.mashupmedia.service.ConfigurationManager;
 import org.mashupmedia.service.MediaManager;
 import org.mashupmedia.service.MusicManager;
 import org.mashupmedia.service.PlaylistManager;
 import org.mashupmedia.util.PlaylistHelper;
 import org.mashupmedia.util.SecurityHelper;
 import org.mashupmedia.util.StringHelper;
 import org.mashupmedia.util.WebHelper;
 import org.mashupmedia.util.WebHelper.MediaContentType;
 import org.mashupmedia.web.page.AlbumPage;
 import org.mashupmedia.web.page.AlbumsPage;
 import org.mashupmedia.web.page.ArtistPage;
 import org.mashupmedia.web.page.ArtistsPage;
 import org.mashupmedia.web.remote.RemoteMediaMetaItem;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.RequestParam;
 
 @Controller
 @RequestMapping("/ajax/music")
 public class AjaxMusicController extends AjaxBaseController {
 
 	private final static int TOTAL_RANDOM_ALBUMS = 60;
 	private final static int TOTAL_ALBUMS = 30;
 
 	@Autowired
 	private MusicManager musicManager;
 
 	@Autowired
 	private MediaManager mediaManager;
 
 	@Autowired
 	private PlaylistManager playlistManager;
 
 	@Autowired
 	private AdminManager adminManager;
 
 	@Autowired
 	private DiscogsWebServiceImpl discogsWebService;
 	
	@Autowired
 	private ConfigurationManager configurationManager;
 
 	@RequestMapping(value = "/random-albums", method = RequestMethod.GET)
 	public String getMusic(@RequestParam(value = "isAppend", required = false) Boolean isAppend, Model model) {
 		List<Album> albums = musicManager.getRandomAlbums(TOTAL_RANDOM_ALBUMS);
 
 		boolean isShowTitle = true;
 		if (isAppend != null && isAppend) {
 			isShowTitle = false;
 		}
 
 		model.addAttribute("isShowTitle", isShowTitle);
 		model.addAttribute("albums", albums);
 		return "ajax/music/random-albums";
 
 	}
 
 	@RequestMapping(value = "/album/{albumId}", method = RequestMethod.GET)
 	public String getAlbum(@PathVariable("albumId") Long albumId, Model model) throws Exception {
 		Album album = musicManager.getAlbum(albumId);
 		List<Song> songs = album.getSongs();
 		Artist artist = album.getArtist();
 		RemoteMediaMetaItem remoteMediaMeta = discogsWebService.getArtistInformation(artist);
 		AlbumPage albumPage = new AlbumPage();
 		albumPage.setAlbum(album);
 		albumPage.setSongs(songs);
 		albumPage.setRemoteMediaMetaItem(remoteMediaMeta);
 		model.addAttribute(albumPage);
 		return "ajax/music/album";
 	}
 
 	@RequestMapping(value = "/artist/{artistId}", method = RequestMethod.GET)
 	public String getArtist(@PathVariable("artistId") Long artistId, Model model) throws Exception {
 		Artist artist = musicManager.getArtist(artistId);
 		RemoteMediaMetaItem remoteMediaMeta = discogsWebService.getArtistInformation(artist);
 
 		ArtistPage artistPage = new ArtistPage();
 		artistPage.setArtist(artist);
 		artistPage.setRemoteMediaMeta(remoteMediaMeta);
 
 		model.addAttribute(artistPage);
 		return "ajax/music/artist";
 	}
 
 	@RequestMapping(value = "/albums", method = RequestMethod.GET)
 	public String getAlbums(@RequestParam(value = "pageNumber", required = false) Integer pageNumber,
 			@RequestParam(value = "searchLetter", required = false) String searchLetter, Model model) {
 		AlbumsPage albumsPage = new AlbumsPage();
 		List<String> albumIndexLetters = musicManager.getAlbumIndexLetters();
 		albumsPage.setAlbumIndexLetters(albumIndexLetters);
 		if (pageNumber == null) {
 			pageNumber = 0;
 		}
 
 		List<Album> albums = musicManager.getAlbums(searchLetter, pageNumber, TOTAL_ALBUMS);
 		albumsPage.setAlbums(albums);
 		model.addAttribute(albumsPage);
 		return "ajax/music/albums";
 	}
 
 	@RequestMapping(value = "/artists", method = RequestMethod.GET)
 	public String getArtists(Model model) {
 		ArtistsPage artistsPage = new ArtistsPage();
 		List<String> artistIndexLetters = musicManager.getArtistIndexLetters();
 		artistsPage.setArtistIndexLetters(artistIndexLetters);
 		List<Artist> artists = musicManager.getArtists();
 		artistsPage.setArtists(artists);
 		model.addAttribute(artistsPage);
 		return "ajax/music/artists";
 	}
 
 	@RequestMapping(value = "/play/media-item/{mediaItemId}", method = RequestMethod.GET)
 	public String playSong(@PathVariable Long mediaItemId, @RequestParam(value = "playlistId", required = false) Long playlistId, Model model) {
 		Playlist playlist = null;
 
 		if (playlistId != null && playlistId > 0) {
 			playlist = playlistManager.getPlaylist(playlistId);
 		} else {
 			playlist = playlistManager.getLastAccessedPlaylistForCurrentUser(PlaylistType.MUSIC);
 		}
 
 		MediaItem mediaItem = mediaManager.getMediaItem(mediaItemId);
 
 		PlaylistMediaItem playlistMediaItem = new PlaylistMediaItem();
 		playlistMediaItem.setPlaylist(playlist);
 		playlistMediaItem.setMediaItem(mediaItem);
 		
 		boolean isEncoderInstalled = BooleanUtils.toBoolean(configurationManager
 				.getConfigurationValue(MashUpMediaConstants.IS_ENCODER_INSTALLED));
 		model.addAttribute(MashUpMediaConstants.IS_ENCODER_INSTALLED, isEncoderInstalled);
 		
 		MediaType mediaType = mediaItem.getMediaType();
 		if (mediaType == MediaType.SONG) {
 			Song song = (Song) mediaItem;
 			
 			playlist = updatePlayingSong(playlist, song);
 			
 			song = SerializationUtils.clone(song);
 			song.setDisplayTitle(StringHelper.escapeJavascript(song.getDisplayTitle()));
 			
 			Artist artist = song.getArtist();
 			artist.setName(StringHelper.escapeJavascript(artist.getName()));
 			song.setArtist(artist);
 			
 			Album album = song.getAlbum();
 			song.setAlbum(album);
 			album.setName(StringHelper.escapeJavascript(album.getName()));
 			
 			playlist = SerializationUtils.clone(playlist);
 			playlist.setName(StringHelper.escapeJavascript(playlist.getName()));
 
 			MediaContentType mediaContentType = WebHelper.getMediaContentType(mediaItem.getFormat(), MediaContentType.MP3);
 			model.addAttribute("format", mediaContentType.getjPlayerContentType());
 			model.addAttribute("song", song);
 			model.addAttribute("playlist", playlist);
 			return "ajax/music/player-script";
 		}
 
 		return "";
 	}
 
 	@RequestMapping(value = "/play/next", method = RequestMethod.GET)
 	public String playNextSonginLastAccessedPlaylist(Model model) {
 		playRelativeSong(model, 1);
 		return "ajax/json/media-item";
 	}
 
 	@RequestMapping(value = "/play/previous", method = RequestMethod.GET)
 	public String playPreviousSonginLastAccessedPlaylist(Model model) {
 		playRelativeSong(model, -1);
 		return "ajax/json/media-item";
 	}
 
 	private void playRelativeSong(Model model, int relativeOffset) {
 		User user = SecurityHelper.getLoggedInUser();
 		Playlist playlist = playlistManager.getLastAccessedPlaylistForCurrentUser(PlaylistType.MUSIC);
 		PlaylistMediaItem playlistMediaItem = PlaylistHelper.getRelativePlayingMediaItemFromPlaylist(playlist, relativeOffset);
 		if (playlistMediaItem.getId() > 0) {
 			playlistManager.saveUserPlaylistMediaItem(user, playlistMediaItem);
 
 		}
 
 		model.addAttribute(MashUpMediaConstants.MODEL_KEY_JSON_PLAYLIST, playlist);
 		model.addAttribute(MashUpMediaConstants.MODEL_KEY_JSON_MEDIA_ITEM, playlistMediaItem.getMediaItem());
 
 	}
 
 	private Playlist updatePlayingSong(Playlist playlist, MediaItem mediaItem) {
 
 		long mediaItemId = mediaItem.getId();
 
 		if (playlist == null) {
 			playlist = new Playlist();
 		}
 
 		List<PlaylistMediaItem> playlistMediaItems = playlist.getPlaylistMediaItems();
 		if (playlistMediaItems == null) {
 			playlistMediaItems = new ArrayList<PlaylistMediaItem>();
 		}
 
 		User user = SecurityHelper.getLoggedInUser();
 
 		boolean isFound = false;
 		for (PlaylistMediaItem playlistMediaItem : playlistMediaItems) {
 			MediaItem mi = playlistMediaItem.getMediaItem();
 			boolean isPlaying = false;
 			if (mediaItemId == mi.getId()) {
 				isFound = true;
 				isPlaying = true;
 			}
 			playlistMediaItem.setPlaying(isPlaying);
 
 			if (isPlaying) {
 				playlistManager.saveUserPlaylistMediaItem(user, playlistMediaItem);
 			}
 
 		}
 
 		if (!isFound) {
 			PlaylistMediaItem playlistMediaItem = new PlaylistMediaItem();
 			playlistMediaItem.setMediaItem(mediaItem);
 			playlistMediaItem.setPlaylist(playlist);
 			playlistMediaItem.setPlaying(true);
 			playlistMediaItems.add(playlistMediaItem);
 		}
 
 		playlist.setPlaylistMediaItems(playlistMediaItems);
 		return playlist;
 	}
 
 }
