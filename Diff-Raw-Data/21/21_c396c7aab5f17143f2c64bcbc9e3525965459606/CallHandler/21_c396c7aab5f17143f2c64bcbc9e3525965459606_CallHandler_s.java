 /*
 
  */
 
 package org.raxa.audioplayer;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 import org.apache.log4j.Logger;
 import org.asteriskjava.fastagi.AgiChannel;
 import org.asteriskjava.fastagi.AgiException;
 import org.asteriskjava.fastagi.AgiRequest;
 import org.asteriskjava.fastagi.BaseAgiScript;
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.raxa.alertmessage.ContentFormat;
 import org.raxa.alertmessage.MedicineInformation;
 import org.raxa.alertmessage.MessageTemplate;
 import org.raxa.database.HibernateUtil;
 import org.raxa.database.Patient;
 import org.raxa.registration.Register;
 import org.raxa.alertmessage.MessageInterface;
 import com.fasterxml.jackson.databind.ObjectMapper;
 import org.raxa.database.VariableSetter;
 
 /**
  * Outgoing Call Context here sets the following channel variable
  * totalItem;item0,item1....,count
  * 
  * CAUTION:Even if the patient has hung up the program is going to execute until it meets an exception or termination.
  * @author atul
  *
  */
 public class CallHandler extends BaseAgiScript implements MessageInterface,VariableSetter
 {
 	private AgiRequest request;
 	private AgiChannel channel;
 	private Logger logger = Logger.getLogger(this.getClass());
 	String language;
 	String pnumber;
 	final String speed="1";							//The default speed to play voice file 
 	
 	/**
 	 * checks whether the call is incoming or outgoing.Handles the call accordingly
 	 */
     public void service(AgiRequest request, AgiChannel channel) throws AgiException{
     	try{
     		answer();
     		language=null;
     		pnumber=null;
 	    	this.request=request;
 	    	this.channel=channel;
 	    	if(request.getContext().equals("incoming-call"))
	        	handleIncomingCall();
 	        if(request.getContext().equals("outgoing-call"))
 	        	provideMedicalInfo();
 	        
 	        return;
     	}
     	catch(AgiException ex){
     		
     		logger.error("IMPORTANT:SOME ERROR WHILE HANDLING THE CALL with caller number:"+pnumber);
     		logger.info("Hanging the call");
     		hangup();
     	}
     	catch(Exception ex1){
     		
     		logger.error("IMPORTANT:SOME ERROR WHILE HANDLING THE CALL with caller:"+pnumber);
     		logger.info("Hanging  the call");
     		hangup();
     	}
     	
     }
     
     /**
      * Use in Incoming-Call Context
      * 
      * Handles patient incoming call.
      * 
      * Set patient number as pnumber
      * 
      * INCOMPLETE
      * 
      * @throws AgiException
      * 
      */
     private void handleIncomingCall() throws AgiException,Exception{ 
     	
     	String defaultLanguage = null,pnumber=null;
     	pnumber=channel.getName();		//IMPORTANT  DEPEND ON THE TRIE SERVICE WE WILL BE USING
    	//HARDCODE HERE pnumber=SIP/100abc;
     	defaultLanguage=getValueFromPropertyFile("0","languageMap");
     	language=defaultLanguage;
     	List<Patient> patientList=getAllPatientWithNumber(pnumber);
     	String languagePlaying=sayWelcomeAndgetLanguage(patientList,defaultLanguage);
     	if(languagePlaying==null){
     		logger.error("Unable to get Language when playing to user withn number "+pnumber);
     		return;
     	}
     	language=languagePlaying;
     	channel.setVariable("numberOftimesMenuPlayed", "0");
     	
     	playMainMenu(pnumber,patientList);
     	//Done What patient wanted.
     }
 
 	/**
 	 * Choose a random prefer Language to be played to the patient if patient is registered.If not registered plays welcome with the default language.
 	 * 
 	 * Ask patient if he is comfortable with current language(1 for yes 2 for no).If not,provide him with a set of language(to be recorded manually)which TTS support.
 	 * Enough care should be taken to map patient option with the language in langaugeMap.properties.(languageMap and voice File must go hand in hand.)
 	 * 
 	 * 
 	 * @param patientList
 	 * @param defaultLanguage
 	 * @return
 	 * @throws AgiException
 	 * @throws Exception
 	 */
     private String sayWelcomeAndgetLanguage(List<Patient> patientList,String defaultLanguage) throws AgiException,Exception{
     	
     	String allLanguageVoiceLocation=null,welcomeVoiceFileLocation=null;
 		String allLanguageText,welcomeText;
     	String languagePlaying=null;boolean isPatientRegistered=true;
 	
     	allLanguageText=getValueFromPropertyFile("allLangaugeText","english");
     	welcomeText=getValueFromPropertyFile("welcomeText","english");
     	
     
     	
     	if(patientList==null || patientList.size()==0){
     		isPatientRegistered=false;
     		languagePlaying=defaultLanguage;
     	}
     	else{
     		languagePlaying=patientList.get(0).getPatientPreferredLanguage();
     		if(languagePlaying!=null)
     			language=languagePlaying;
  
     		//If we want the welcome to be played by pre recorded voice unmark it
    /* 		welcomeVoiceFileLocation=getValueFromPropertyFile(languagePlaying.toLowerCase(),"welcome");
     		if(welcomeVoiceFileLocation==null){
     			languagePlaying=defaultLanguage;
     			welcomeVoiceFileLocation=getValueFromPropertyFile(languagePlaying.toLowerCase(),"welcome");
     		}
     */	}
     	
  
     	
     	//plays welcome in the 'languagePlaying' language.Should say "Welcome to Raxa.Do
 		// you want to continue with this Language.1 for yes 2 for no."
     	//See "continueWithLanguage.properties" for any change
     				
     					//char continueWithTheLanguage=getPatientOption(welcomeVoiceFileLocation,null,"129",1);
     	char continueWithTheLanguage=getOptionUsingTTS(welcomeText,"129","5000",2);
     	String isLanguageChanged=analysePatientOption("continueWithLanguage",continueWithTheLanguage);
     	
     	if(isLanguageChanged==null || !Boolean.parseBoolean(isLanguageChanged)){
     					//char whichLanguage=getPatientOption(allLanguageVoiceLocation,"20000","1234569",2);			//SHOULD BE PLAYED IN DIFFERENT LANGUAGE AND SHOULD BE PRERECORDED
     		
     		char whichLanguage=getOptionUsingTTS(allLanguageText,"1234569","5000",2);	//SHOULD BE DELETED
     		languagePlaying=analysePatientOption("languageMap",whichLanguage);
     	}
     	//When Patient doesnot respond for any language even after playing it a number of times.
     	if(languagePlaying==null){
 			channel.hangup();
 			logger.error("Cannot map the language chosen by user");
 			return null;
 		}
     	
     	return languagePlaying;
     }
     /**
      * Plays the main menu using TTS.The main menu String is fetched from mainmenutext property file in english.properties.
      * 
      * 
      * @param pnumber
      * @param patientList
      * @throws Exception
      */
     private void playMainMenu(String pnumber,List<Patient> patientList) throws Exception{
     	String mainmenuVoiceFileLocation=getValueFromPropertyFile(language.toLowerCase(),"mainmenu");
     	String mainmenuText=getValueFromPropertyFile("mainmenuText","english");
     	
     	channel.setVariable("numberOftimesMenuPlayed", String.valueOf((Integer.parseInt(channel.getVariable("numberOftimesMenuPlayed")))+1));
     	
     					//char option=getPatientOption(mainmenuVoiceFileLocation,"11000","1234569",2);
     		char option=getOptionUsingTTS(mainmenuText,"1234569","5000",2);
     	String KeyWord=analysePatientOption("mainmenu",option);
     	if(KeyWord==null){
     		channel.hangup();
     		logger.error("Unable to get what patient opted in main menu of language "+language);
     		return;
     	}
     	
     	doWhatPatientWant(KeyWord,patientList,language);
     	
     }
     /**
      * Takes the Keyword and all the possible patientList associated with the phone number and patient prefer langauge
      * Checks if the keyword require pid?
      * If yes,ask for the patietn identity among patientList.
      * Stores the pid and fetch asked information using the pid
      * 
      * @param keyWord
      * @param patientList
      * @param language
      * @throws AgiException
      * @throws Exception
      */
     private void doWhatPatientWant(String keyWord,List<Patient> patientList,String language) throws AgiException,Exception{
     	
     	String pid=null;boolean isPatientRegistered=true;boolean isAllowed=true;
     	int count=Integer.parseInt(channel.getVariable("numberOftimesMenuPlayed"));
     	
     	if(patientList==null || patientList.size()==0)
     		isPatientRegistered=false;
     	//All option that needs to check if the patient is in the alert System already should add that option to the or Statement
     	//This is to deal with fact that if a atient is not register and he keeps on opting for the option that needs patient to be registered
     	if(keyWord.toLowerCase().equals("reminder")||keyWord.toLowerCase().equals("deregister")){
     		if(!isPatientRegistered && count<=2){						//Play main menu only twice 
     			playUsingTTS(getValueFromPropertyFile("notRegistered","english"),getTTSNotation(language));
     			playMainMenu(pnumber,patientList);
     			return;
     		}
     		if(!isPatientRegistered){
     			channel.hangup();
     			logger.error("patient unable to choose a valid main menu option");
     			return;
     		}
     	}
     	
     	
     	if(keyWord.toLowerCase().equals("register")){
     		
     		Register r=new Register();
     		List<Patient> p=r.getpatientFromNumber(pnumber,language);
     		pid=getPid(p);
     		if(pid==null)
     			playUsingTTS(getValueFromPropertyFile("PatientNotExist", "english"),getTTSNotation(language));
     		else{
     			if(r.addReminder(pid, language, IVR_TYPE))
     				playUsingTTS(getValueFromPropertyFile("successfulRegister", "english"),getTTSNotation(language));
     			else
     				playUsingTTS(getValueFromPropertyFile("unsuccessfulRegister", "english"),getTTSNotation(language));
     		}
     		
     	}
     	
     	else if(keyWord.toLowerCase().equals("reminder")){
     		pid=getPid(patientList);
     		if(pid==null){
     			playUsingTTS(getValueFromPropertyFile("PatientNotExist", "english"),getTTSNotation(language));
     			return;
     		}
     		//There may be a chance that language currently playing is not the preferred language as specified in database.if we want to change 
     		//language to preferLanguage below code should be included.otherwise it will continue with the current language.
  /*   		for(Patient p:patientList){
     			if(p.getPatientId().equals(pid))
     				language=p.getPatientPreferredLanguage();
     		}
     		
    */ 		else
     			medicineReminder(pid);
     		
     	}
     	
     	else if (keyWord.toLowerCase().equals("deregister")){
     			if(pid==null)
     			if(new Register().deleteReminder(pid,IVR_TYPE))
     					playUsingTTS(getValueFromPropertyFile("successUnregister", "english"),getTTSNotation(language));
     			else 
     					playUsingTTS(getValueFromPropertyFile("failUnregister", "english"),getTTSNotation(language));
     	}
     	
     	else if(keyWord.toLowerCase().equals("call")){
     		//INCOMPLETE
     		//CALL SomeOne
     		//channel.exec("DIAL", " 'SIP/1000abc','100' ");
     	}
     }
    
     
     /**
      * Given a list of possible patient it returns the pid.
      * then it stores the pid as the channel variable to avoid calculation again and agai
      * 
      * Disadvantage:once patient pid is set it cannot query for anyone else while in the call.
      * @param patientList
      * @return
      * @throws AgiException
      */
     private String getPid(List<Patient> patientList) throws AgiException{
     	String pid;
     	pid=channel.getVariable("pid");
     	if(pid==null){
     		logger.info("Patient with pnumber:"+pnumber+"chose pid:"+pid);
     		pid=getPatientIdfromList(patientList);
     	}
 		channel.setVariable("pid", pid);
 		return pid;
     }
     /**
      * Plays medicine Reminder to the patient
      * @param pid
      * @throws AgiException
      */
     private void medicineReminder(String pid) throws AgiException {
     		Date time=new Date();
     		time.setHours(0);
 			time.setMinutes(1);					//set time to today's midnight so that entire medicine prescription of today can be fetched.
 			
 			List<MedicineInformation> listofinfo=new ReminderExtractor().getMedicineInfo(pid, time);
 			String header1=getValueFromPropertyFile("IncomingCallMedicineInfoHeader1","english");
 			String header2=getValueFromPropertyFile("IncomingCallMedicineInfoHeader2","english");
 			String ttsNotation=getTTSNotation(language);
 			if(header1!=null)
 				playUsingTTS(header1,ttsNotation);
 			if(header2!=null)
 				playUsingTTS(header2,ttsNotation);
 			for(MedicineInformation info : listofinfo )
 				playUsingTTS(new MessageTemplate().getTextToconvertToVoice(info),ttsNotation);
 			
 	}
     
 
 
 	/**
      * Gets value from properties file.
      */
     private String getValueFromPropertyFile(String property,String propertyFile){
     	Properties prop = new Properties();
 		try{
 			logger.debug("Trying to fetch the folder property "+property+" from properties "+propertyFile+".properties");
 			prop.load(this.getClass().getClassLoader().getResourceAsStream(propertyFile+".properties"));
 			return prop.getProperty(property);
 		}
 		catch(Exception ex){
 			logger.error("Some error occured while trying to fetch property "+property+" from properties "+propertyFile+".properties");
 			logger.error("\nCaused by:\n",ex);
 		}
 		return null;
     }
     
 
     /**
      * Play any voice file in Location fileLocation
      * 
      * Caution:Don't give any extension. eg:/home/you/music/metallica/St.Anger
      * 
      * @param fileLocation
      * @throws AgiException
      */
     private void playFile(String fileLocation) throws AgiException{
     	channel.streamFile(fileLocation);
     }
     
     /**
      * This speaks out name of all the patient name one by one and return the uuid for the name the caller chose
      * 
      * What I don't like about it:after calling every name it will produce a beep and ask for option.If not get an answer say the other patient name and
      * again beep,wait ask for an option
      * 
      * can be changed to any other file.
      * 
      *  Use in Incoming-Call Context
      * @throws AgiException 
      * 
      */
     
     private String getPatientIdfromList(List<Patient> patientList) throws AgiException{
     	int numberOfTries=2;
     	
     	if(patientList==null)
     		return null;
     	if(patientList.size()==1)
     		return patientList.get(0).getPatientId();
     	
     	String message1=getValueFromPropertyFile("patientNameMessage1","english");
     	String message2=getValueFromPropertyFile("patientNameMessage2","english");
     	String ttsNotatiton=getTTSNotation(language);
     	
     	int count=0;char option='0';
     	String space=" ";String message="";
     	
     	
     	
 	    	for(Patient patient:patientList){
 	    		
 	    		message+=message1+space+patient.getPatientName()+space+message2+space+(++count)+space;
 	    	
 	    	}
 	    	
 	    	option=getOptionUsingTTS(message,"123456","5000",2);
 	    	
 	    	int opttionNumericValue=option-'0';
 	    	
 	    	
 	    	
 	    	
 	    	
 	    	    	
 	    	if(opttionNumericValue>=1 && opttionNumericValue<=9 && opttionNumericValue<=patientList.size())
 	    		return patientList.get(opttionNumericValue-1).getPatientId();
 	    	
 	    	
 	    	else{
 	    		String toSay="Sorry You pressed an invalid input";
 	    		playUsingTTS(toSay,ttsNotatiton);
 	    	}
 	    		
 	    	
     	
     	
        		channel.hangup();
     		logger.error("Patient didnot enter a valid input");
     		return null;
     	
     }
     
     /**
      * Play the text in desired langauge and stops if get an option.If not play a beep and wait for 3 seconds.
      * escape String 
      * 
      * @param toSpeak
      * @param escapeDigits  is a set of digits,if pressed,will stop the voice playing and send the pressed digits to option variable
      * @param beepSeconds:  is time in milliseconds.After playing a beep the application will wait for beepSeconds for user to send an input
      * @param numberOfTry   if failed to get an option it will play the same thing for numberOfTry times
      * @return
      * @throws AgiException
      */
     private char getOptionUsingTTS(String toSpeak,String escapeDigits,String beepSeconds,int numberOfTry) throws AgiException{
     	char option='0';int count=0;
     	String ttsNotation=getTTSNotation(language);
     	String beepVoiceLocation=getValueFromPropertyFile("beep","english");
  //   	playUsingTTS("Please Press your option after the beep",ttsNotation);
     	Long time=Long.parseLong(beepSeconds);
     	while(option=='0'&& count<numberOfTry){
     		playUsingTTS(toSpeak,ttsNotation,escapeDigits);
     		String dtmfKey=channel.getVariable("option");
     		
     		if(!(dtmfKey==null)){
     			option=dtmfKey.charAt(0);
     			channel.setVariable("option", "0");
     		}
     		if(option=='0')
     			option=getOption(beepVoiceLocation,escapeDigits,time);
     		++count;
     	}
     	if(option=='0')
     		ifOptionNotChosenAfterManyTries();
     	
     	return option;
     	
     }
     
     /**
      * @throws AgiException 
      * 
      */
     
 
     /**
      * This method plays the sound file and wait for timeout(in millisecods) for the patient to press something.
      * 
      * @see '9' is reserved for some special function.For eg.If the patient presses 9 may be it will directly connect to
      * some human.
      * @param fileLocation:location of file to be streamed
      * @param timeout:time to wait to get option from the patient
      * @param numberOfrepeat:if patient does not select anything number of time to wait 
      * @param escapeDigits:will stop playing the file if pressed
      * @return Returns 0 if no digit being pressed, or the ASCII numerical value of the digit if one was pressed, 
      * or -1 on error or if the channel was disconnected.
      * @throws AgiException
      * 
      */
     /**
      * 
      * @param fileLocation  location where pre-recorded voice is stored
      * @param timeout		time(millisecond) to wait for patient to enter an option
      * @param escapeDigits	when pressed stops the voice file playing and take the input as user choice
      * @param numberOfRepeat number of times to repeat the voice file if user fails to press an option
      * @return
      * @throws AgiException
      */
     private char getPatientOption(String fileLocation,String timeout,String escapeDigits,int numberOfRepeat)throws AgiException{
     	//If patient press any of the escape digit the sound streaming will stop
     	String defaultTimeout="15000";	//hard coded
     	char option='0';int count=0;
     	if(timeout==null)
     		timeout=defaultTimeout;
     	Long time=Long.parseLong(timeout);
     	while(option=='0'&& count<numberOfRepeat){
     		option=channel.getOption(fileLocation,escapeDigits,time);
     		++count;
     	}
     	if(option=='0')
     		ifOptionNotChosenAfterManyTries();
     	
     	return option;
     }
     
    
     /**
      * Action to be taken when user doesnot choose any option
      * @throws AgiException
      */
     private void ifOptionNotChosenAfterManyTries() throws AgiException{
     	logger.error("Incoming Call:Patient didnot choose any option after many tries.HangingUp");
     	channel.hangup();
     }
     
     /**
      * search for keyword from a property file with option as property field
      */
     
     private String analysePatientOption(String propertyFile,char option){
     	if(option<='8' && option>='1'){
     		String keyWord=getValueFromPropertyFile(Character.toString(option),propertyFile);
     		return keyWord;
     	}
     	return null;
     }
 
     /**
      *  Return list of patient associated with a number
      * 
      * @param pnumber
      * @return
      */
     List<Patient> getAllPatientWithNumber(String pnumber){
     	List<Patient> nameAndId=new ArrayList<Patient>();
     	try{
 	    	Session session = HibernateUtil.getSessionFactory().openSession();
 	    	session.beginTransaction();
 			String hql="from Patient where (pnumber=:pnumber or snumber=:pnumber) and pid in (select pid from PAlert where alertType=:alertType)";
 	    	Query query=session.createQuery(hql);
 	    	query.setString("pnumber", pnumber);
 	    	query.setInteger("alertType", IVR_TYPE);
 	    	List<Patient> patientList=(List<Patient>)query.list();
 	    	if(patientList==null || patientList.size()<1)
 	    		return null;
 	    	for(int i=0;i<patientList.size();i++){
 	    		Patient p=(Patient)patientList.get(i);
 	    		nameAndId.add(p);
 	    	}
 	    	return nameAndId;
     	}
     	catch(Exception ex){
     		logger.error("unable to retrieve data for patient with phone number:"+pnumber);
     		logger.error("\nCaused by:\n",ex);
     		return null;
     	}
     }
     /**
      *  Use in Incoming-Call Context.
      * 
      * takes message and ttsNotation i.e (en,hi) and plays it
      * @param message
      * @param ttsNotation
      * @throws AgiException
      */
     private void playUsingTTS(String message,String ttsNotation) throws AgiException{
     	playUsingTTS(message,ttsNotation,null);
     	
     }
     
     /**
      * Play the message using TTS and stop laying it as soon as it encounters any escape digits
      * @param message
      * @param ttsNotation
      * @param escapeDigits
      * @throws AgiException
      * 
      */
     private void playUsingTTS(String message,String ttsNotation,String escapeDigits) throws AgiException{
     	if(message==null) return;
     	String contentToPlay;String comma=",";
     	String onlySpeed=comma+ttsNotation+comma+comma+speed;
     	String speedAndEscape=comma+ttsNotation+comma+escapeDigits+comma+speed;
     	
     	//eg.exec("AGI","google"hello Atul today you have to take",hi);
     	if(!ttsNotation.equals("en")){
     		//google Translate to translate first
     		//store that in a channel variable
     		//http://zaf.github.io/asterisk-googletranslate/
     		//Not Supported yet.Need API Registration.
     		String translate="googletranslate.agi,\""+message+"\""+comma+ttsNotation;
     		exec("AGI",translate);
     		message=channel.getVariable("gtranslation");
     	}
     	
     	if(escapeDigits==null)
     		contentToPlay="googletts.agi,\""+message+"\""+onlySpeed;
     	else 
     		contentToPlay="googletts.agi,\""+message+"\""+speedAndEscape;
     	
     	logger.info("Playing:"+contentToPlay);
     	
     	exec("AGI",contentToPlay);
     }
     
     /**
      *  Use in Incoming-Call Context
      * 
      *Copied from org.raxa.module.ami.Outgoing.getTTSNotation Method. Just to make the AGI completely independent. 
      *Return ttsNotation for preferLanguage else return default Notation
      */
     
     public String getTTSNotation(String preferLanguage){
     	String defaultLanguage=null;
     	Properties prop = new Properties();
 		try{
 			logger.info("Trying to fetch the notation for the prefer language:"+preferLanguage);
 			prop.load(this.getClass().getClassLoader().getResourceAsStream("tts.properties"));
 			defaultLanguage=prop.getProperty("default");
 			return(prop.getProperty(preferLanguage.toLowerCase()));
 		}
 		catch(IOException ex) {
     		
     		logger.error("Unable to set prefer language:"+preferLanguage+" playing in defaultLanguage:"+defaultLanguage);
     		logger.error("\nCaused by:\n",ex);
     		return defaultLanguage;
         }
     }
     
     /**
      * used in Outgoing-Call Context.
      * 
      * It loops between AGI and asterisk dial plan outgoing-call context.
      * asterisk dial plan contains google TTS.
      * If asked to play using TTS it returns and play it via google TTS.
      * If asked to play using audio folder it loops back using while.
      * 
      * It does use recursion.
      * 
      * Warning:Unless you are familiar with asterisk dial plan don't mess with it.
      * @throws AgiException
      */
    
     private void provideMedicalInfo() throws AgiException{
     	
     	while(true){
 	    	int readItemSoFar=Integer.parseInt(channel.getVariable("count"));
 	    	
 	    	if(readItemSoFar==0){
 	    		int msgId=Integer.parseInt(request.getParameter("msgId"));
 	            
 	            try{
 	            	List<String> content=getMessageContent(msgId);
 	            	int totalItem=content.size();
 	            	channel.setVariable("totalItem",String.valueOf(totalItem));
 	            	for(int i=0;i<totalItem;i++){
 	            		String item="item"+i;
 	            		channel.setVariable(item,(String)content.get(i));
 	            	}
 	            }
 	            catch(Exception ex){
 	            	logger.error("IMPORTANT:ERROR OCCURED WHILE IN CALL.CHECK THE ISSUE");
 	            	logger.error("\n ERROR Caused by\n",ex);
 	            	channel.hangup();
 	            	return;
 	            }
 	    	}
 	    	
 	    	
 	    	if(readItemSoFar>=Integer.parseInt(channel.getVariable("totalItem"))){
 	    		channel.hangup();
 	    		int par1=Integer.parseInt(request.getParameter("msgId"));
 	    		String aid=request.getParameter("aid");
 	    		String serviceInfo=channel.getName();//Doubt
 	    		CallSuccess obj=new CallSuccess(par1,aid,true,serviceInfo);
 	    		obj.updateAlert(IVR_TYPE);
 	    		return;
 	    	}
 	    	
 	    	updateCount(readItemSoFar);
 	    	
 	    	String itemNumber="item"+readItemSoFar;
 	    	String itemContent=channel.getVariable(itemNumber);
 	    	ContentFormat message=new MessageTemplate().parseString(itemContent);
 	    
 	    	String preferLanguage=(request.getParameter("language")).toLowerCase();
 	    	String ttsNotation=request.getParameter("ttsNotation");
 	    	
 	    	if(message==null || (message.getContent())==null){
 	    		provideMedicalInfo();
 	    		return;
 	    	}
 	    	
 	    	//Caution:Ensure that the below if statement return.
 	    	
 	    	if(message.getMode()==TTS_MODE){
 	    		logger.info("Playing "+message.getContent()+" in TTS");
 	    		channel.setVariable("message", message.getContent().toLowerCase());
 	    		channel.setVariable("language",ttsNotation);
 	    		return;
 	    	}
 	    	//The part below does not depend on dialplan.So you can mess with it..
 	    	
 	    	else if(message.getMode()==AUDIO_MODE){
 		    		Properties prop = new Properties();
 		    		try{
 		    			String fileLocation;
 		    			logger.info("Searching for "+preferLanguage+".properties file");
 			    		prop.load(this.getClass().getClassLoader().getResourceAsStream(preferLanguage+".properties"));
 			    		if(message.getField().equals(new MessageTemplate().DoseFieldRepresentation))
 			    			fileLocation=prop.getProperty(message.getField())+"/"+message.getContent(); 
 			    		else fileLocation=prop.getProperty(message.getField())+"/"+message.getField();    //if want to put un fromatted location then remove "/"+message.getField()
 			    		
 			    		logger.info("Playing "+message.getField()+" in from audio Folder with file location "+fileLocation);
 			    		channel.streamFile(fileLocation);
 			    	}
 		    		catch (IOException ex) {
 		        		
 		        		logger.error("Some error while playing AudioFile returning back");
 		        		logger.error("\nCaused by:\n",ex);
 		            }
 		    }
     	}
     }
     
     /**
      * used in Outgoing-Call Context.
      * 
      * update how many ivrMsg:itemNumber of msgId has been played.
      * @param count
      * @throws AgiException
      */
     private void updateCount(int count) throws AgiException{
 		++count;
 		channel.setVariable("count",String.valueOf(count));
 	}
     
     /**
      * used in Outgoing-Call Context.
      * @param itemContent
      * @return
      */
 
    
 	
 	/**
 	 * used in Outgoing-Call Context.
 	 * 
      * Get Message Content form IvrMsg
      * @param msgId
      * @return
      * @throws Exception
      */
 	private List<String> getMessageContent(int msgId) throws Exception{
 		logger.info("Getting content for medicine Reminder haveing msgId"+msgId);
 		String hql="select content from IvrMsg where ivrId=:msgId order by itemNumber";
 		Session session = HibernateUtil.getSessionFactory().openSession();
     	session.beginTransaction();
     	Query query=session.createQuery(hql);
     	query.setInteger("msgId", msgId);
     	List<String> content=(List<String>)query.list();
     	session.getTransaction().commit();
     	session.close();
     	logger.info("Successfully retreived msg content from database with msgId"+msgId);
     	return content;
 	}
 	
 	
     
 }
     
  
