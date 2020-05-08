 package com.vdweem.webspotify.action;
 
 import jahspotify.media.Artist;
 import jahspotify.media.Image;
 import jahspotify.media.Link;
 import jahspotify.services.JahSpotifyService;
 import jahspotify.services.MediaHelper;
 
 import java.util.List;
 
 import com.opensymphony.xwork2.Result;
 import com.vdweem.webspotify.result.ImageResult;
 
 /**
  * Show an image. If no image is found then show the corresponding placeholder.
  * @author Niels
  */
 public class ImageAction {
 	private String id;
 
 	public Result execute() {
 		Link link = Link.create(id);
 		String img = null;
 
 		switch (link.getType()) {
 		case ALBUM:
 			img = "noAlbum.png";
 			link = JahSpotifyService.getInstance().getJahSpotify().readAlbum(link).getCover();
 			break;
 		case ARTIST:
 			img = "noArtist.png";
 			Artist artist = JahSpotifyService.getInstance().getJahSpotify().readArtist(link, true);
 			if (!MediaHelper.waitFor(artist, 2)) break;
 			List<Link> links = artist.getPortraits();
 			if (links.size() > 0) link = links.get(0);
 			else link = null;
 			break;
 		case TRACK:
 			link = Link.create(JahSpotifyService.getInstance().getJahSpotify().readTrack(link).getCover());
 			break;
 		case IMAGE:
 			break;
 		default:
 				throw new IllegalArgumentException("Images should be created from an artist, album, track or image link.");
 		}
 
 		if (link != null) {
 			Image image = JahSpotifyService.getInstance().getJahSpotify().readImage(link);
 			if (image == null) return null;
 			if (MediaHelper.waitFor(image, 2)) {
 				return new ImageResult(image.getBytes());
 			}
 		}
		return new ImageResult("images/" + img);
 	}
 
 	public String getId() {
 		return id;
 	}
 
 	public void setId(String id) {
 		this.id = id;
 	}
 }
