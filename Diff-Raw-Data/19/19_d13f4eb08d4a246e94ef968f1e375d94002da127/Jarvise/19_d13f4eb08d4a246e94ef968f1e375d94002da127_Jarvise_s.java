package com.domotica.jarvise;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.LinkedList;
 
 /**
  * @author raelix
  *
  */
 
 public class Jarvise {
 	private static String nameFile = "jarvise.txt";
 	private String host;
 	private static int port = 20;
 	ArrayList<String> text;
 	public boolean domanda;
 	public boolean risposta;
 	public boolean fine;
 	LinkedList<String> texts;
 	int state;
 	Configuration readFile;
 	
  public Jarvise() {
 	 this.readFile = new Configuration(nameFile);
 		this.host = readFile.getHost();
 		this.state = -1;
 		this.fine = false;
 		this.risposta = false;
 		this.domanda = true;
 		this.text = new ArrayList<String>();
 		this.texts = new LinkedList<String>();
 	};
 
 	/**
 	 * @param frase
 	 */
 	public void execute(String frase){
 
 		texts.addFirst(frase);
 /*
 		if(texts.getFirst().matches(WORD_CONSTANTS.ciao)){
 			if(texts.getFirst().contains(" ") ){
 				String c[]=texts.getFirst().split("\\s",2);
 			if(!c[1].matches(WORD_CONSTANTS.name)){
 				execute(c[1]);
 				return;
 			}
 			if(c[1].matches(WORD_CONSTANTS.name)){
 				String f[]=texts.getFirst().split("\\s",2);
 				execute(f[1]);
 				return;
 			}} else {
 				MainActivity.say("ciao, ben tornato");
 				while(MainActivity.talker.isSpeaking());
 				return;
 			}
 		}	
 	*/
 		if(texts.getFirst().matches(WORD_CONSTANTS.ciao)){
 			if(texts.getFirst().contains(" ") ){
 				if(texts.getFirst().matches(WORD_CONSTANTS.name)){
 					String f[]=texts.getFirst().split("\\s",2);
 					if(f[1].contains(" ") ){
 						String k[]=texts.getFirst().split("\\s",2);
 						execute(k[1]);
 						return;
 					}
 					else MainActivity.say("ciao, ben tornato!");
 					while(MainActivity.talker.isSpeaking());
 					return;
 				}
 				else if(texts.getFirst().contains(" ") ){
 					String k[]=texts.getFirst().split("\\s",2);
 					execute(k[1]);
 					return;
 				}
 			}
 			else {MainActivity.say("ciao, ben tornato!");
 			while(MainActivity.talker.isSpeaking());
 			return;
 			}
 		}
 		else if(texts.getFirst().matches(WORD_CONSTANTS.sbagliato)){
 			MainActivity.say("ok mi dispiace, alla prossima");
 			while(MainActivity.talker.isSpeaking());
 			fine = true;
 			return;
 		}
 		else if(texts.getFirst().matches(WORD_CONSTANTS.sentimento)){
 			MainActivity.say("Benone, grazie,,vuoi che faccia qualcosa per te?");
 			while(MainActivity.talker.isSpeaking());
 			return;
 			}
 		
 		else if(texts.getFirst().matches(WORD_CONSTANTS.ritornello)){
 			MainActivity.say("la capra campa,sotto la panca,,la capra crepa,,la conosco anche io,bella filastrocca");
 			while(MainActivity.talker.isSpeaking());
 			fine = true;
 			return;
 			}
 		
 		else if(texts.getFirst().matches(WORD_CONSTANTS.cerca)){
 			state = 1;
 			onSuccess();
 		}
 
 		else if(texts.getFirst().matches(WORD_CONSTANTS.cerca1)){
 			state = 2;
 			onSuccess();
 		}
 		
 
 		else if(texts.getFirst().matches(WORD_CONSTANTS.on)){
 			state = 3;
 			onSuccess();
 		}
 
 		else if(texts.getFirst().matches(WORD_CONSTANTS.off)){
 			state = 4;
 			onSuccess();
 		}
 
 		else if(texts.getFirst().matches(WORD_CONSTANTS.inizio) || (texts.getFirst().matches(WORD_CONSTANTS.inizio1))){
 			if(texts.getFirst().matches(WORD_CONSTANTS.inizio1)){
 				texts.addFirst(texts.getFirst().split("\\s",4)[3]);
 				onSuccess();}
 			else {
 				//					String c[]=texts.getFirst().split("\\s",3);
 				texts.addFirst(texts.getFirst().split("\\s",3)[2]);
 				onSuccess();}
 			}
 
 		else if(texts.getFirst().matches(WORD_CONSTANTS.orario)){
 			MainActivity.say("L'ora ,,,");
 			while(MainActivity.talker.isSpeaking());
 			MainActivity.say( java.text.DateFormat.getTimeInstance().format(Calendar.getInstance().getTime()));
 			while(MainActivity.talker.isSpeaking());
 			MainActivity.say("secondi");
 			while(MainActivity.talker.isSpeaking());
 			fine = true;
 			onSuccess();
 		}
 		
 		else if(texts.getFirst().matches(WORD_CONSTANTS.entrata)){
 			MainActivity.say("ok, me ne occupo io, Disattivo l'allarme e apro il garage");
 			while(MainActivity.talker.isSpeaking());
 			state = 5;
 			onSuccess();
 			}
 		
 		else if(texts.getFirst().matches(WORD_CONSTANTS.uscita)){
 			MainActivity.say("ok, me ne occupo io, Attivo l'allarme e apro il garage");
 			while(MainActivity.talker.isSpeaking());
 			state = 6;
 			onSuccess();
 			return;
 			}
 		
 		else if(texts.getFirst().matches(WORD_CONSTANTS.movimento)){
 			state = 7;
 			onSuccess();
 		}
 		
 		else {
 			while(MainActivity.talker.isSpeaking());
 			MainActivity.say("Non ho capito ripeti?");
 			fine= false;
 			while(MainActivity.talker.isSpeaking());
 			return;}}
 
 	public void onSuccess(){
 		fine = true;
 		/*if(state==0)MainActivity.say(" OK spedisco");
 		else if(state==1 || state == 2)MainActivity.say(" OK cerco su internet");
 		else if(state==3)MainActivity.say(" OK provo ad Accendere");
 		else if(state==4)MainActivity.say(" OK provo a Spegnere");
 		while(MainActivity.talker.isSpeaking());
 		*/
 		switch (state){
 		case 0:
 			MainActivity.say(" OK spedisco");
 			while(MainActivity.talker.isSpeaking());
 			new MultiThread("127.0.0.1", 9001,new Pacco1(1));
 			break;
 		case 1:
 			MainActivity.say(" OK cerco su internet");
 			while(MainActivity.talker.isSpeaking());
 			new MultiThread("127.0.0.1", 9001,new PaccoString((texts.getFirst().split("\\s",2)[1])));
 			break;
 		case 2:
 			MainActivity.say(" OK cerco su internet");
 			while(MainActivity.talker.isSpeaking());
 			new MultiThread("127.0.0.1", 9001,new PaccoString((texts.getFirst().split("\\s",4)[3])));
 			break;
 		case 3:
 			if(texts.getFirst().matches(WORD_CONSTANTS.allarme)){
 			MainActivity.say(" OK provo ad Attivarla");
 			}
 			else if(texts.getFirst().matches(WORD_CONSTANTS.garage)){
 				MainActivity.say(" OK provo ad Aprirlo");
 				}
 			else MainActivity.say(" OK provo ad Accendere");
 			while(MainActivity.talker.isSpeaking());
 			new MultiThread("127.0.0.1", 9001,new PaccoStringOn("Accensione: "+(texts.getFirst().split("\\s",2)[1])));
 			break;
 		case 4:
 			if(texts.getFirst().matches(WORD_CONSTANTS.allarme)){
 			MainActivity.say(" OK provo ad Disattivarla");
 			}
 			else if(texts.getFirst().matches(WORD_CONSTANTS.garage)){
 				MainActivity.say(" OK provo a Chiuderlo");
 				}
 			else MainActivity.say(" OK provo a Spegnere");
 			while(MainActivity.talker.isSpeaking());
 			new MultiThread("127.0.0.1", 9001,new PaccoStringOff("Spegnimento: "+(texts.getFirst().split("\\s",2)[1])));
 			break;
 		case 5:
 		new MultiThread("127.0.0.1", 9001,new PaccoStringOn("Accensione: Garage"));
 		try {
 			Thread.sleep(4000);
 			} catch (InterruptedException e) {
 			e.printStackTrace();}
 		new MultiThread("127.0.0.1", 9001,new PaccoStringOff("Spegnimento: Allarme"));
 		try {
 			Thread.sleep(3000);
 			} catch (InterruptedException e) {
 			e.printStackTrace();}
 		 MainActivity.say("Vuoi che faccia altro ?");
 		   while (MainActivity.talker.isSpeaking());
 		  	   fine = false;
 			   return;	
 		
 		case 6:
 		new MultiThread("127.0.0.1", 9001,new PaccoStringOn("Accensione: Allarme"));
 		try {
 			Thread.sleep(4000);
 			} catch (InterruptedException e) {
 			e.printStackTrace();}
 	   new MultiThread("127.0.0.1", 9001,new PaccoStringOff("Spegnimento: Garage"));
 	   try {
 			Thread.sleep(3000);
 			} catch (InterruptedException e) {
 			e.printStackTrace();}
 	   MainActivity.say("Vuoi che faccia altro ?");
 	   while (MainActivity.talker.isSpeaking());
 	  	   fine = false;
 		   return;	
 		case 7:
 			MainActivity.say(" OK controllo ultimo movimento");
 			while(MainActivity.talker.isSpeaking());
 			new MultiThread("127.0.0.1", 9001,new PaccoString((texts.getFirst().split("\\s",2)[1])));
 			break;
 		}
 		/*
 		if(state==0)new MultiThread("127.0.0.1", 9001,new Pacco1(1));
 		else if(state==1)new MultiThread("127.0.0.1", 9001,new PaccoString((texts.getFirst().split("\\s",2)[1])));
 		else if(state==2)new MultiThread("127.0.0.1", 9001,new PaccoString((texts.getFirst().split("\\s",4)[3])));
 		else if(state==3)new MultiThread("127.0.0.1", 9001,new PaccoStringOn("Accensione: "+(texts.getFirst().split("\\s",2)[1])));
 		else if(state==4)new MultiThread("127.0.0.1", 9001,new PaccoStringOff("Spegnimento: "+(texts.getFirst().split("\\s",2)[1])));
 		*/
 		state = -1 ;
 		while(MainActivity.talker.isSpeaking());
 		
 		return;
 	}
 
 };
 
 
 
