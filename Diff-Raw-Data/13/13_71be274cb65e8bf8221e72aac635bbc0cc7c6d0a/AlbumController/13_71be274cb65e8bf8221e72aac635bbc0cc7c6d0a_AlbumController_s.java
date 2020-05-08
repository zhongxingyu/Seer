 package org.rakvag.hylla.web;
 
 import java.util.List;
 
 import javax.inject.Inject;
 
 import org.rakvag.hylla.domain.Album;
import org.rakvag.hylla.domain.Artist;
 import org.rakvag.hylla.domain.Sjanger;
 import org.rakvag.hylla.services.AlbumService;
 import org.rakvag.hylla.services.HylleService;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.servlet.ModelAndView;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 
 @Controller
 @Scope("request")
 public class AlbumController {
 
 	@Inject
 	private AlbumService albumService;
 	@Inject
 	private HylleService hylleService;
 	@Inject
 	private Sesjonsdata sesjonsdata;
 
 	@RequestMapping(value = "/album/utfoerSoek", method = RequestMethod.GET)
 	public ModelAndView utfoerSoek(@ModelAttribute("artist") String artistnavn,
 			@ModelAttribute("album") String albumnavn, @ModelAttribute("taMedKorteAlbum") String taMedKorteAlbum,
 			RedirectAttributes redirAttr) {
 		
 		List<Album> albumliste = albumService.soekEtterAlbumISpotify(artistnavn, albumnavn,
 				"true".equals(taMedKorteAlbum));
 		for (Album album : albumliste)
 			album.setErPaaHylleUtifraHylleId(sesjonsdata.getHylleId());
 
 		ModelAndView mv = new ModelAndView("soek");
 		mv.addObject("albumene", albumliste);
 		mv.addObject("visAarOgLengdePaaAlbum", true);
 		return mv;
 	}
 
 	@RequestMapping(value = "/album/leggTilPaaHylle", method = RequestMethod.GET)
 	public ModelAndView leggTilPaaHylle(@ModelAttribute("albumId") String albumId, RedirectAttributes redirAttr) {
 		hylleService.leggTilAlbumPaaHylle(Long.parseLong(albumId), sesjonsdata.getHylleId());
 		ModelAndView mv = new ModelAndView("redirect:/");
 		return mv;
 	}
 
 	@RequestMapping(value = "/album/fjernFraHylle", method = RequestMethod.GET)
 	public ModelAndView fjernFraHylle(@ModelAttribute("albumId") String albumId, RedirectAttributes redirAttr) {
 		hylleService.fjernAlbumFraHylle(Long.parseLong(albumId), sesjonsdata.getHylleId());
 		ModelAndView mv = new ModelAndView("redirect:/");
 		return mv;
 	}
 
 	@RequestMapping(value = "/album/", method = RequestMethod.GET)
 	public ModelAndView aapne(@ModelAttribute("albumId") String albumId) {
 		Album album = albumService.hentAlbum(Long.parseLong(albumId));
 		album.setSpor(albumService.hentSporenetilAlbumFraSpotify(album.getSpotifyURI()));
 		album.setErPaaHylleUtifraHylleId(sesjonsdata.getHylleId());
 		ModelAndView mv = new ModelAndView("album", "album", album);
 		DetaljerForm form = new DetaljerForm();
 		form.setSjanger(album.getSjanger().name());
 		form.setAlbumId(album.getId());
 		mv.addObject("command", form);
 		mv.addObject("sjangre", Sjanger.lagSjangerMap());
 		return mv;
 	}
 
 	@RequestMapping(value = "/album/lagre", method = RequestMethod.POST)
 	public ModelAndView lagre(@ModelAttribute("command") DetaljerForm form) {
 		Album album = albumService.hentAlbum(form.getAlbumId());
 		Sjanger sjanger = Sjanger.valueOf(form.getSjanger());
 		album.setSjanger(sjanger);
		Artist artist = album.getArtist();
		if (artist.getDefaultSjanger() != sjanger) {
			artist.setDefaultSjanger(sjanger);
		}
 		album = albumService.lagreAlbum(album);
 		return new ModelAndView("redirect:/album/", "albumId", form.getAlbumId());
 	}
 
 	@RequestMapping(value = "/album/masseinnlasting", method = RequestMethod.GET)
 	public ModelAndView aapneMasseinnlasting() {
 		return new ModelAndView("masseinnlasting", "command", new MasseinnlastingForm());
 	}
 
 }
