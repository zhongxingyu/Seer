 package com.googlecode.mad_schuelerturnier.business.spieldurchfuehrung;
 
 import com.googlecode.mad_schuelerturnier.business.controller.resultate.ResultateVerarbeiter;
 import com.googlecode.mad_schuelerturnier.business.impl.Business;
 import com.googlecode.mad_schuelerturnier.business.print.PrintAgent;
import com.googlecode.mad_schuelerturnier.business.print.SpielPrintManager;
 import com.googlecode.mad_schuelerturnier.business.zeit.Countdown;
 import com.googlecode.mad_schuelerturnier.business.zeit.ZeitPuls;
 import com.googlecode.mad_schuelerturnier.business.zeit.Zeitgeber;
 import com.googlecode.mad_schuelerturnier.model.enums.SpielEnum;
 import com.googlecode.mad_schuelerturnier.model.enums.SpielPhasenEnum;
 import com.googlecode.mad_schuelerturnier.model.enums.SpielZeilenPhaseEnum;
 import com.googlecode.mad_schuelerturnier.model.helper.SpielEinstellungen;
 import com.googlecode.mad_schuelerturnier.model.spiel.Spiel;
 import com.googlecode.mad_schuelerturnier.model.spiel.tabelle.SpielZeile;
 import com.googlecode.mad_schuelerturnier.persistence.repository.PenaltyRepository;
 import com.googlecode.mad_schuelerturnier.persistence.repository.SpielRepository;
 import com.googlecode.mad_schuelerturnier.persistence.repository.SpielZeilenRepository;
 import org.apache.log4j.Logger;
 import org.joda.time.DateTime;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.context.ApplicationListener;
 import org.springframework.stereotype.Component;
 
 import java.util.ArrayList;
 import java.util.List;
 
 @Component
 public class SpielDurchfuehrung implements ApplicationListener<ZeitPuls> {
 
     private static final Logger LOG = Logger.getLogger(SpielDurchfuehrung.class);
 
     private static int WARTEND_SIZE = 3;
     private static int MINUTEN_ZUM_VORBEREITEN = 3;
 
     private String delim = System.getProperty("file.separator");
 
     @Autowired
     private ResultateVerarbeiter verarbeiter;
 
     @Autowired
     private SpielZeilenRepository spielzeilenRepo;
 
     @Autowired
     private SpielRepository spielRepo;
 
     @Autowired
     private PenaltyRepository penaltyRepo;
 
     @Autowired
     private Business business;
 
     @Autowired
     private Zeitgeber zeitgeber;
 
     @Autowired
     private PrintAgent agent;
 
    @Autowired
    private SpielPrintManager spielPrinter;

      private boolean endranglistegedruckt = false;
 
     private Countdown countdown;
 
     private Countdown countdownToStart;
 
     private ZeitPuls jetzt;
 
     private List<String> text = new ArrayList<String>();
 
     private List<SpielZeile> _1_wartend = new ArrayList<SpielZeile>();
     private List<SpielZeile> _2_zum_vorbereiten = new ArrayList<SpielZeile>();
 
     private List<SpielZeile> _3_vorbereitet = new ArrayList<SpielZeile>();
     private List<SpielZeile> _4_spielend = new ArrayList<SpielZeile>();
     private List<SpielZeile> _5_beendet = new ArrayList<SpielZeile>();
 
     private List<SpielZeile> _6_eingetragen = new ArrayList<SpielZeile>();
 
     private boolean init = false;
 
     private long lastText = System.currentTimeMillis();
 
     public void onApplicationEvent(final ZeitPuls event) {
 
         //beim ersten aufruf, initialisierung
         if(! init){
            init();
             init = true;
         }
 
         this.jetzt = event;
 
         if (this.countdown != null) {
             this.countdown.signalTime(event.getSpielZeit());
         }
 
         if (this.countdownToStart != null) {
             this.countdownToStart.signalTime(event.getSpielZeit());
         }
 
         if (this.business.getSpielEinstellungen().getPhase().equals(SpielPhasenEnum.F_SPIELEN)) {
 
             prepare_1_wartend();
 
             prepare_2_zum_vorbereiten();
 
             prepare_3_warten_auf_start();
 
             prepare_4_spielend();
 
             checkSpielende();
 
         }
 
 
     }
      /*
       * dient dazu beim ersten aufruf die liste zu fuellen mit den zeilen aus der db
       */
     private synchronized  void init(){
         _2_zum_vorbereiten.addAll(spielzeilenRepo.find_B_ZurVorbereitung());
         _3_vorbereitet.addAll(spielzeilenRepo.find_C_Vorbereitet());
         _4_spielend.addAll(spielzeilenRepo.find_D_Spielend());
     }
 
     private synchronized void prepare_1_wartend() {
         // wartende auffuellen
         if (this._1_wartend.size() < WARTEND_SIZE) {
             final List<SpielZeile> list = this.spielzeilenRepo.findNextZeile();
 
             if (!list.isEmpty()) {
                 for (final SpielZeile spielZeile : list) {
                     // pruefung ob breits Mannschaften eingetragen sind bei den Finalen
                     if (spielZeile.getA() != null && spielZeile.getA().getTyp() != SpielEnum.GRUPPE) {
                         if (spielZeile.getA() != null) {
                             if (spielZeile.getA().getMannschaftA() == null) {
                                 continue;
                             }
                         }
                         if (spielZeile.getB() != null) {
                             if (spielZeile.getB().getMannschaftA() == null) {
                                 continue;
                             }
                         }
                         if (spielZeile.getC() != null) {
                             if (spielZeile.getC().getMannschaftA() == null) {
                                 continue;
                             }
                         }
 
                     }
 
                     if (!this._1_wartend.contains(spielZeile)) {
                         if (!spielZeile.checkEmty()) {
                             this._1_wartend.add(0, spielZeile);
                             SpielDurchfuehrung.LOG.info("neue zeile geholt: " + spielZeile);
                             if (this._1_wartend.size() == WARTEND_SIZE) {
                                 break;
                             }
                         }
                     }
                 }
             }
         }
     }
 
     private void prepare_2_zum_vorbereiten() {
 
         if (!this._1_wartend.isEmpty()) {
             // zum vorbereiten einstellen
             if ((this._2_zum_vorbereiten.isEmpty())) {
 
                 // pruefung ob nachste zeile bereits zur vorbereitung
                 long naechste = this._1_wartend.get(this._1_wartend.size() - 1).getStart().getTime()- (60 * MINUTEN_ZUM_VORBEREITEN * 1000);
                 long now = jetzt.getSpielZeit().getMillis();
 
                 if( naechste < now){
                 SpielZeile temp = this._1_wartend.remove(this._1_wartend.size() - 1);
                 temp.setPhase(SpielZeilenPhaseEnum.B_ZUR_VORBEREITUNG);
                 temp = this.spielzeilenRepo.save(temp);
                 this._2_zum_vorbereiten.add(temp);
 
                     // automatisches vorbereiten
                     if(this.business.getSpielEinstellungen().isAutomatischesVorbereiten()){
                         this.vorbereitet();
                     }
 
                 }
             }
         }
 
         // zeit anhalten, falls die spielzeit des zum vorbereitenden abgelaufen ist
         if (stopBecause_zumVorbereiten() && this.zeitgeber.isGameStarted()) {
             this.zeitgeber.stopGame("spielzeit des zu_vorbereitenden ist abgelaufen");
         }
 
         // zeit wieder starten, falls zum vorbereiten leer ist oder eine zeile mit start in der zukunft hat, falls evnet auf pause ist
         if (jetzt.isUnterbruch() && !stopBecause_wartenAufStart() ) {
             if (this._2_zum_vorbereiten.isEmpty() || jetzt.getSpielZeit().isBefore(new DateTime(_2_zum_vorbereiten.get(0).getStart()))) {
                 this.zeitgeber.startGame(0,"liste mit zu_vorbereiteten ist wieder leer");
             }
         }
 
     }
 
     private boolean stopBecause_zumVorbereiten() {
         boolean result = ! this._2_zum_vorbereiten.isEmpty() && jetzt.getSpielZeit().isAfter(new DateTime(_2_zum_vorbereiten.get(0).getStart()));
         return result;
     }
 
     private boolean stopBecause_wartenAufStart() {
         boolean result = ! this._3_vorbereitet.isEmpty() && jetzt.getSpielZeit().isAfter(new DateTime(_3_vorbereitet.get(0).getStart()));
         return result;
     }
 
     private void prepare_3_warten_auf_start() {
 
         // zeit anhalten, falls die spielzeit des zum vorbereiten abgelaufen ist
         if (stopBecause_wartenAufStart() ) {
             this.zeitgeber.stopGame("spielzeit des vorbereitenden ist abgelaufen");
         }
 
         // zeit wieder starten, falls vorbereiten leer ist oder eine zeile mit start in der zukunft hat, falls evnet auf pause ist
         if (jetzt.isUnterbruch() && ! stopBecause_zumVorbereiten()) {
             if (this._3_vorbereitet.isEmpty() || jetzt.getSpielZeit().isBefore(new DateTime(_3_vorbereitet.get(0).getStart()))) {
                 this.zeitgeber.startGame(0, "liste mit vorbereiteten ist wieder leer");
             }
         }
     }
 
     private void prepare_4_spielend() {
 
         // keine spielenden
         if (this.get_4_spielend().isEmpty()) {
             return;
         }
 
         // beenden, falls zeit abgelaufen ist
         if (this.countdown != null && this.getCountdown().isFertig()) {
             this.beenden();
         }
 
     }
 
     private void checkSpielende() {
         if (this.get_1_wartend().isEmpty() && this.get_2_zum_vorbereiten().isEmpty() && this.get_3_vorbereitet().isEmpty() && this.get_4_spielend().isEmpty()) {
 
             List<Spiel> einzutragende = this.spielRepo.findAllEinzutragende();
             List<Spiel> bestaetigen = this.spielRepo.findAllZuBestaetigen();
 
             if(einzutragende.size() > 0 || bestaetigen.size() > 0){
                  return;
             }
 
             if(! endranglistegedruckt){
                 agent.saveFileToPrint("rangliste",verarbeiter.getRangliste());
                 endranglistegedruckt = true;
 
                // letzte resultate drucken
                spielPrinter.printPage();

                 // phase abschliessen
                 SpielEinstellungen einstellung =  business.getSpielEinstellungen();
                 einstellung.setPhase(SpielPhasenEnum.G_ABGESCHLOSSEN);
                 business.saveEinstellungen(einstellung);
 
                 LOG.info("spiel abgeschlossen");
             }
         }
     }
 
     public void vorbereitet() {
         SpielZeile temp = this._2_zum_vorbereiten.remove(0);
         this.countdownToStart = new Countdown(this.jetzt.getSpielZeit(), new DateTime(temp.getStart()));
         temp.setPhase(SpielZeilenPhaseEnum.C_VORBEREITET);
         temp = this.spielzeilenRepo.save(temp);
         this._3_vorbereitet.add(temp);
         // tts
         generateText(temp);
     }
 
     public void spielen() {
 
         gong();
 
         this.countdown = new Countdown(this.jetzt.getSpielZeit(), 10 * 60);
         SpielZeile temp = this._3_vorbereitet.remove(0);
         temp.setPhase(SpielZeilenPhaseEnum.D_SPIELEND);
         if (temp.getA() != null) {
             temp.getA().setAmSpielen(true);
         }
         if (temp.getB() != null) {
             temp.getB().setAmSpielen(true);
         }
         if (temp.getC() != null) {
             temp.getC().setAmSpielen(true);
         }
         temp = this.spielzeilenRepo.save(temp);
         this._4_spielend.add(temp);
 
         // zeit aufholen, falls eingestellt auf automatisch
         if(this.business.getSpielEinstellungen().isAutomatischesAufholen()){
             this.business.spielzeitEinholen(this.business.getSpielEinstellungen().getAufholzeitInSekunden());
         }
     }
 
     public synchronized void beenden() {
         SpielZeile temp = this._4_spielend.remove(0);
         temp.setPhase(SpielZeilenPhaseEnum.E_BEENDET);
         temp = this.spielzeilenRepo.save(temp);
         this._5_beendet.add(temp);
 
         // einzutragende spiele vorbereiten
         if (temp.getA() != null) {
             Spiel spiel = temp.getA();
             spiel.setAmSpielen(false);
             spiel.setFertigGespielt(true);
             spielRepo.save(spiel);
         }
         if (temp.getB() != null) {
             Spiel spiel = temp.getB();
             spiel.setAmSpielen(false);
             spiel.setFertigGespielt(true);
             spielRepo.save(spiel);
         }
         if (temp.getC() != null) {
             Spiel spiel = temp.getC();
             spiel.setAmSpielen(false);
             spiel.setFertigGespielt(true);
             spielRepo.save(spiel);
         }
 
         gong();
     }
 
     public synchronized List<SpielZeile> get_1_wartend() {
         return this._1_wartend;
     }
 
     public String getText(){
         if(text.size() > 0){
             return this.text.remove(0);
         }
         return null;
     }
 
     private void gong(){
         // bell setzen
         if(business.getSpielEinstellungen().isGongEinschalten()){
             text.add("resources"+delim+"static"+delim+"sound"+delim+"bell01.mov");
         }
     }
 
     public int countText(){
         if(text.size() > 0){
             return this.text.get(0).length();
         }
         return 0;
     }
     public String getWait(){
 
         // 15 Sekunden default
         int millis = 15000;
 
         // countdown
         if(countdown != null &&!countdown.isFertig()){
             if(  millis > (countdown.getSecondsPlus2() * 1000)){
                 millis = (countdown.getSecondsPlus2() * 1000);
             }
         }
 
         // countdownToStart
         if(countdownToStart != null && !countdownToStart.isFertig()){
             if( millis > (countdownToStart.getSecondsPlus2() * 1000)){
                 millis = (countdownToStart.getSecondsPlus2() * 1000);
             }
         }
 
         // ansagetext fuer platze
         if(isTextAvailable() && (countText() == 31 ||   countText() == 30  )){
             if(millis < 25000){
                 millis = 25000;
             }
         }
 
         // bell
         if(isTextAvailable() && (countText() == 35 )){
             millis = 6000;
         }
 
         if(millis < 5000){
             millis = 5000;
         }
 
         return "" + millis;
     }
 
     public boolean isTextAvailable(){
         if(this.text.size() > 0){
             return true;
         }
         return false;
     }
 
     public void generateText(SpielZeile zeile){
         if(this.business.getSpielEinstellungen().isAutomatischesAnsagen()){
             text.add("resources/static/sound/" + zeile.getId()+".mp3");
         }
     }
 
     // getter & setter
 
     public boolean getReadyToVorbereiten() {
         return this._3_vorbereitet.isEmpty();
     }
 
     public boolean getReadyToSpielen() {
         return this._4_spielend.isEmpty();
     }
 
     public List<SpielZeile> get_2_zum_vorbereiten() {
         return this._2_zum_vorbereiten;
     }
 
     public List<SpielZeile> get_3_vorbereitet() {
         return this._3_vorbereitet;
     }
 
     public List<SpielZeile> get_4_spielend() {
         return this._4_spielend;
     }
 
     public List<SpielZeile> get_5_beendet() {
         return this._5_beendet;
     }
 
     public List<SpielZeile> get_6_eingetragen() {
         return this._6_eingetragen;
     }
 
     public void set_6_eingetragen(final List<SpielZeile> _6_eingetragen) {
         this._6_eingetragen = _6_eingetragen;
     }
 
     public Countdown getCountdown() {
         return this.countdown;
     }
 
     public Countdown getCountdownToStart() {
         return countdownToStart;
     }
 
     public void fertigesSpiel(Spiel spiel) {
         this.verarbeiter.signalFertigesSpiel(spiel.getId());
     }
 
 }
