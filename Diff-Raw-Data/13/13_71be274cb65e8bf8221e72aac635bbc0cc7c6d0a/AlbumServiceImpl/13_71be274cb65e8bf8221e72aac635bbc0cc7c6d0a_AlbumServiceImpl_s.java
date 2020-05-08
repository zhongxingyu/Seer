 package org.rakvag.hylla.services;
 
 import java.util.ArrayList;
 import java.util.Collection;
 import java.util.HashSet;
 import java.util.List;
 import java.util.Map;
 import java.util.Set;
 
 import javax.inject.Inject;
 
 import org.rakvag.hylla.domain.Album;
 import org.rakvag.hylla.domain.Artist;
 import org.rakvag.hylla.domain.Sjanger;
 import org.rakvag.hylla.domain.Spor;
 import org.rakvag.spotifyapi.SpotifyAPI;
 import org.rakvag.spotifyapi.entity.SpotifyAlbum;
 import org.rakvag.spotifyapi.entity.SpotifyTrack;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Service;
 
 @Service
 public class AlbumServiceImpl extends SpotifyServiceImpl implements AlbumService {
 
 	private final static int MAX_ANTALL_ALBUM_FRA_SOEK = 28;
 	private static Logger logger = LoggerFactory.getLogger(AlbumServiceImpl.class.getName());
 
 	@Inject
 	private SpotifyAPI spotifyAPI;
 	@Inject
 	private HylleService hylleService;
 	@Inject
 	private ArtistService artistService;
 
 	@Override
 	public List<Album> soekEtterAlbumISpotify(String artistnavn, String albumnavn, boolean taMedKorteAlbum) {
 		List<SpotifyAlbum> albumFraSoek = null;
 		try {
 			albumFraSoek = spotifyAPI.soekEtterAlbum(artistnavn, albumnavn, 20);
 		} catch (Exception e) {
 			logger.warn(e.getMessage(), e);
 		}
 		if (albumFraSoek == null || albumFraSoek.size() == 0)
 			return new ArrayList<Album>();
 
 		List<String> skalVisesPaaSiden = finnHvilkeAlbumSomSkalMedISoeketreffene(albumFraSoek);
 		Collection<String> maaHentes = new HashSet<String>(skalVisesPaaSiden);
 		maaHentes.removeAll(finnHvilkeAlbumSomFinnesIDB(skalVisesPaaSiden));
 		Map<String, Sjanger> artistersSjanger = artistService.hentArtistersDefaultSjanger();
 		Collection<Album> albumHentetFraSpotify = Oversetter.oversettSpotifyAlbum(
 				spotifyAPI.hentAlbumPaaSpotifyURIer(maaHentes, 10), artistersSjanger);
 		albumHentetFraSpotify = synkroniserAlbumInklArtistMedDBEtterSpotifyURI(albumHentetFraSpotify);
 
 		Map<String, String> coverartLinker = spotifyAPI.hentBildelinker(maaHentes);
 		for (Album hentetAlbum : albumHentetFraSpotify) {
 			if (coverartLinker.containsKey(hentetAlbum.getSpotifyURI()))
 				hentetAlbum.setCoverartlink(coverartLinker.get(hentetAlbum.getSpotifyURI()));
 			lagreAlbum(hentetAlbum);
 		}
 
 		List<Album> soeketreffene = new ArrayList<Album>();
 		for (String albumURI : skalVisesPaaSiden) {
 			Album album = albumDAO.hentPaaSpotifyURI(albumURI);
 			if (taMedKorteAlbum || !album.erEtKortAlbum())
 				soeketreffene.add(album);
 		}
 
 		return soeketreffene;
 	}
 
 	@Override
 	public Album hentAlbum(long albumID) {
 		Album album = albumDAO.hent(albumID);
 		if (album.getSjanger() == Sjanger.IKKE_SATT && album.getArtist().getDefaultSjanger() != Sjanger.IKKE_SATT) {
 			album.setSjanger(album.getArtist().getDefaultSjanger());
 			album = albumDAO.lagre(album);
 		}
 		return album;
 	}
 
 	@Override
 	public Set<Spor> hentSporenetilAlbumFraSpotify(String albumsSpotifyURI) {
 		Set<Spor> sporene = new HashSet<Spor>();
 
 		ArrayList<String> spotifyURIer = new ArrayList<String>();
 		spotifyURIer.add(albumsSpotifyURI);
 		Collection<SpotifyAlbum> spotifyAlbums = spotifyAPI.hentAlbumPaaSpotifyURIer(spotifyURIer, 10);
 
 		if (!spotifyAlbums.isEmpty()) {
 			List<SpotifyTrack> tracks = spotifyAlbums.iterator().next().getTracks();
 			for (SpotifyTrack spotifyTrack : tracks)
 				sporene.add(Oversetter.oversettSpotifyTrack(spotifyTrack));
 		}
 
 		return sporene;
 	}
 
 	@Override
 	public Album lagreAlbum(Album album) {
 		album.setArtist(artistService.lagreArtist(album.getArtist()));
 		Artist artist = album.getArtist();
		if (Sjanger.IKKE_SATT.equals(album.getSjanger()) 
 				&& !Artist.URI_VARIOUS_ARTISTS_ARTIST.equals(artist.getSpotifyURI())) {
 			artist.setDefaultSjanger(album.getSjanger());
 		}
 
 		return albumDAO.lagre(album);
 	}
 
 	private List<String> finnHvilkeAlbumSomSkalMedISoeketreffene(List<SpotifyAlbum> albumFraSoek) {
 		List<String> URIerPaaAlbumSomSkalHentesOpp = new ArrayList<String>();
 		for (SpotifyAlbum albumet : albumFraSoek) {
 			if (!albumet.erTilgjengeligINorge())
 				continue;
 			URIerPaaAlbumSomSkalHentesOpp.add(albumet.getHref());
 			if (URIerPaaAlbumSomSkalHentesOpp.size() >= MAX_ANTALL_ALBUM_FRA_SOEK)
 				break;
 		}
 		return URIerPaaAlbumSomSkalHentesOpp;
 	}
 
 	private Collection<String> finnHvilkeAlbumSomFinnesIDB(List<String> albumURIer) {
 		Collection<String> finnesIDB = new HashSet<String>();
 		for (String uri : albumURIer) {
 			if (albumDAO.finnesDenneIDB(uri))
 				finnesIDB.add(uri);
 		}
 		return finnesIDB;
 	}
 
 }
