 /**
  * 
  */
 package swa.runningeasy.business.services;
 
 import java.util.List;
 
 import org.apache.log4j.Logger;
 
 import swa.runningeasy.business.AnmeldungBA;
 import swa.runningeasy.business.LaeuferBA;
 import swa.runningeasy.business.LaufzeitBA;
 import swa.runningeasy.business.ListeneintragBA;
 import swa.runningeasy.business.VeranstaltungBA;
 import swa.runningeasy.business.VereinBA;
 import swa.runningeasy.dtos.AnmeldungDTO;
 import swa.runningeasy.dtos.LaeuferDTO;
 import swa.runningeasy.dtos.LaufzeitDTO;
 import swa.runningeasy.dtos.ListeneintragDTO;
 import swa.runningeasy.dtos.VeranstaltungDTO;
 import swa.runningeasy.dtos.VereinDTO;
 import swa.runningeasy.init.BAFactory;
 import swa.runningeasy.init.DBInit;
 import swa.runningeasy.services.Auswertung;
 import swa.runningeasy.services.RunningServices;
 
 /**
  * @author Tim Schmiedl (Cyboot)
  * 
  */
 public class RunningServiceBA implements RunningServices {
	private boolean			isInit	= false;
 	private VeranstaltungBA	veranstaltungBA;
 	private VereinBA		vereinBA;
 	private AnmeldungBA		anmeldungBA;
 	private LaeuferBA		laeuferBA;
 	private LaufzeitBA		ergebnisBA;
 	private ListeneintragBA	listeneintragBA;
 
 	private static Logger	logger	= Logger.getLogger(RunningServiceBA.class);
 
 
 	@Override
 	public void erzeugeVeranstaltung(final VeranstaltungDTO v) {
 		logger.trace("call erzeugeVeranstaltung()-method");
 		veranstaltungBA.createVeranstaltung(v);
 	}
 
 	@Override
 	public void erzeugeVerein(final VereinDTO v) {
 		logger.trace("call erzeugeVerein()-method");
 		vereinBA.createVerein(v);
 	}
 
 	@Override
 	public void erzeugeAnmeldung(final AnmeldungDTO a) {
 		logger.trace("call erzeugeAnmeldung()-method");
 		anmeldungBA.createAnmeldung(a);
 	}
 
 	@Override
 	public void erzeugeLaeufer(final LaeuferDTO a) {
 		logger.trace("call erzeugeLaeufer()-method");
 		laeuferBA.createLaeufer(a);
 	}
 
 	@Override
 	public void erzeugeLaufzeit(final LaufzeitDTO l) {
 		logger.trace("call erzeugeLaufzeit()-method");
 		ergebnisBA.createLaufzeit(l);
 	}
 
 	@Override
 	public List<VeranstaltungDTO> getVeranstaltungen() {
 		logger.trace("call getVeranstaltungen()-method");
 		return veranstaltungBA.getAllVeranstaltungen();
 	}
 
 	@Override
 	public List<VereinDTO> getVereine() {
 		logger.trace("call getVereine()-method");
 		return vereinBA.getAllVereine();
 	}
 
 	@Override
 	public List<LaeuferDTO> getLaeufer() {
 		logger.trace("call getLaeufer()-method");
 		return laeuferBA.getAllLauefer();
 	}
 
 	@Override
 	public List<AnmeldungDTO> getAnmeldungen(final String Veranstaltung) {
 		logger.trace("call getAnmeldungen()-method");
 		return anmeldungBA.getAllAnmeldungen();
 	}
 
 	@Override
 	public List<LaufzeitDTO> getLaufzeiten(final String Veranstaltung) {
 		logger.trace("call getLaufzeiten()-method");
 		return ergebnisBA.getAllLaufzeiten();
 	}
 
 	@Override
 	public List<ListeneintragDTO> getAuswertung(final Auswertung a, final String Veranstaltung) {
 		logger.trace("call getAuswertung()-method");
 		return listeneintragBA.generateListeneintrag(a, Veranstaltung);
 	}
 
 	@Override
 	public void init() {
		// only initialize one time
		if (isInit)
			return;

		logger.info("##########      Starting Runningeasy      ##########");
 		BAFactory.init();
 
 		// TODO: set Database
 		DBInit.setRealDB(true);
 		DBInit.init();
 
 		veranstaltungBA = BAFactory.getVeranstaltungBA();
 		vereinBA = BAFactory.getVereinBA();
 		anmeldungBA = BAFactory.getAnmeldungBA();
 		laeuferBA = BAFactory.getLaeuferBA();
 		ergebnisBA = BAFactory.getLaufzeitBA();
 		listeneintragBA = BAFactory.getListeneintragBA();
		isInit = true;
 	}
 
 }
