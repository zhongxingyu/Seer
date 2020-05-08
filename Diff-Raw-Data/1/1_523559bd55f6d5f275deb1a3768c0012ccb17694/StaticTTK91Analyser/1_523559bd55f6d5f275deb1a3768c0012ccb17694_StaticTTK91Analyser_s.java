 package fi.helsinki.cs.koskelo.analyser;
 
 import fi.helsinki.cs.koskelo.common.InvalidTTK91CriteriaException;
 import fi.helsinki.cs.koskelo.common.TTK91Constant;
 import fi.helsinki.cs.koskelo.common.TTK91TaskOptions;
 import fi.helsinki.cs.koskelo.analyser.TTK91AnalyseData;
 
 import fi.hy.eassari.showtask.trainer.AttributeCache;
 import fi.hy.eassari.showtask.trainer.Feedback;
 import fi.hy.eassari.showtask.trainer.CacheException;
 import fi.hy.eassari.showtask.trainer.CommonAnalyser;
 
 
 /**
  * Luokka staattisten TTK-91 -tehtvien vastauksien tarkastamiseen
  * @author Lauri Liuhto
  *  
  */
 
 public class StaticTTK91Analyser extends CommonAnalyser {
 
     private String taskID;
     private String language;
     private TTK91AnalyserUtils cacheUtils;
     private TTK91TaskOptions taskOptions;
     private Feedback errorFeedback;
     private AttributeCache cache;
 
     /**
      * Konstruktori, joka luo uuden alustamattoman
      * StaticTTK91Analyserin, alustettava init-metodilla.
      *
      */
 
     public StaticTTK91Analyser() {
 	this.taskID = null;
 	this.language = null;
 	this.cacheUtils = null;
 	this.taskOptions = null;
 	this.errorFeedback = null;
 	this.cache = null;
     }
 
 
     /**
      * Alustaa alustamattoman StaticTTK91Analyserin
      * @param taskid tehtvtunnus
      * @param language kielikoodi
      * @param initparams rajapinnan vaatima, ei kytss
      */
 
     public void init(String taskid, String language, String initparams) {
 	this.taskID = taskid;
 	this.language = language;
     } // init
 
     
     /**
      * Ilmoitetaan StaticTTK91Analyserille tietokanta-cache.  Samassa
      * yhteydess noudetaan TTK91AnalyserUtils- ja
      * TTK91TaskOptions-oliot.
      * @param AttributeCache
      */
     
     public void registerCache(AttributeCache c) {
 	
 	this.cache = c;
 
 	/* luodaan TTK91AnalyserUtils ja TTK91TaskOptions
 	 * -oliot. Rumaa, mutta pakko tehd ennen varsinaisen
 	 * analyse()-metodin kyntiinpotkaisemista (jotta mahdolliset
 	 * aliluokat (ainakin FillinTTK91Analyser) voivat peitt
 	 * peitt fetchExampleCoden, ja kytt tmn luokan omaa
 	 * analyse()-metodia. 
 	 */
 	
 	this.errorFeedback = fetchTTK91AnalyserUtils();  
 	if (errorFeedback == null) {
 	    this.errorFeedback = fetchTTK91TaskOptions();
 	}
     } // registerCache
     
 
     /**
      * Analysoi vastauksen Titokoneella
      * @param answer TTK91-kielinen lhdekoodi
      * @param params rajapinnan vaatima, ei kytss
      * @return palaute
      */
 
     public Feedback analyse(String[] answer, String params) { // FIXME:
 
 	if (this.errorFeedback != null) { 
 	    // virheit initialisoinnissa, ei syyt jatkaa.
 	    return this.errorFeedback; 
 	}
 
 	if (answer == null) {
 	    return new Feedback(TTK91Constant.ERROR,
 				"Virhe vastauksen vlityksess. Ei voida"+
 				"analysoida.");
 	}
 
 	TTK91AnalyseResults results = null;
 	String[] exampleCode = fetchExampleCode();
 	TTK91AnalyseData analyseData = 
 	    new TTK91AnalyseData(this.taskOptions, answer[0], exampleCode[0]);
 
 	// FIXME FillIniss tm malliratkaisun ksittelytapa ei taida toimia?
 	// FIXME FIXME FIXME FIXME FIXME FIXME FIXME FIXME FIXME FIXME FIXME
 	//  
 	// h, miksei toimi? [LL]
 
 	if (analyseData.errors()) { // tapahtuiko simulaatioissa virheit?
 	    String[] messages = analyseData.getErrorMessages();
 	    String errorMessage = "";
 	    for (int i = 0; i < messages.length; ++i) {
 		if (messages[i] != null) {
 		    errorMessage += messages[i];
 		}
 	    }
 	    
 	    return new Feedback(TTK91Constant.ERROR,
 				"Ratkaisun simulointi eponnistui:"+
 				errorMessage);
 	} // if (analyseData.errors())
 	
 	TTK91RealAnalyser realAnalyser = new TTK91RealAnalyser(analyseData);
 
 	results = realAnalyser.analyse();
 	
 	Feedback fb;
 	try {
 	    fb = 
 		TTK91FeedbackComposer.formFeedback(results,
 						   taskOptions.getTaskFeedback(),
 						   cache,
 						   taskID,
 						   language);
 	} catch (CacheException e) {
 	    fb = 
 		TTK91FeedbackComposer.formFeedback("Error while retrieving "+
 						   "error message :( "+
 						   e.getMessage() );
 	}
 	
 	return fb;
 
     }
 
 
     /**
      * Apumetodi, joka noutaa malliratkaisun taskoptionsista Tm metodi
      * kannattanee peitt StaticTTK91Analyserin periviss luokissa,
      * ainakin fillin/dynamic -analysaattoreissa!
      * @return esimerkkikoodi
      */
     
     protected String[] fetchExampleCode() {
 	String[] exampleCode = new String[1];
 	if (this.taskOptions != null) {
 	    exampleCode[0] = this.taskOptions.getExampleCode();
 	    return exampleCode;
 	}
 	else {
 	    return null;
 	}
     } // fetchExampleCode
 
 
     /**
      * Apumetodi TTK91AnalyserUtilsin luomiseen.
      * @return Feedback virheen sattuessa, null jos kaikki ok
      */
 
     private Feedback fetchTTK91AnalyserUtils() {
 	this.cacheUtils = new TTK91AnalyserUtils(this.cache,
 						 this.taskID,
 						 this.language);
 	if (this.cacheUtils == null) {
 	    return new Feedback(TTK91Constant.ERROR,
 				"*TTK91Analyser.fetchTTK91AnalyserUtils():"+
 				" TTK91AnalyserUtilsia ei saatu. Analyysia ei"+
 				" voida tehd.");
 	}
 	return null; // kaikki ok
     }
 
     /**
      * Apumetodi TTK91TaskOptionsin hakuun cachesta
      * @return Feedback virheen sattuessa, null jos kaikki ok
      */
 
     private Feedback fetchTTK91TaskOptions() {
 	try {
 	    this.taskOptions = this.cacheUtils.getTTK91TaskOptions();
 	}
 	catch (CacheException ce) {
 	    return new Feedback(TTK91Constant.ERROR, 
 				"*TTK91Analyser.getTTK91TaskOptions()->"+
 				"CacheException: "+ce.getMessage());
 	}
 	catch (InvalidTTK91CriteriaException ie) {
 	    return new Feedback(TTK91Constant.ERROR, 
 				"*TTK91Analyser.getTTK91TaskOptions()->"+
 				"InvalidTTK91CriteriaException: "+
 				ie.getMessage());
 	}	    
 	if (this.taskOptions == null) {
 	    return new Feedback(TTK91Constant.ERROR, 
 				"this.taskOptions on null,"+
 				"vastauksen tarkastamista ei voi tehd");
 	}
 	return null; // kaikki ok
     }
 }
