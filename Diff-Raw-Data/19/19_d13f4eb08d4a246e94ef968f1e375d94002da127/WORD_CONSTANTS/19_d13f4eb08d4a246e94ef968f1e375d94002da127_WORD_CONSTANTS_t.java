package com.domotica.jarviseSSH;
 
 public interface WORD_CONSTANTS {
 	String giusto=".*[Ss]i.*|" +
 			".*[Ee]satt.*|" +
 			".*[Pp]erfett.*|" +
 			".*[Gg]iusto.*|" +
 			".*[Cc]orrett.*|" +
 			".*[Rr]agion.*|" +
 			".*[Bb]rav.*|" +
 			".*[Ff]inalment.*";
 	
 	String sbagliato=".*[Nn]o.*|" +
 			".*[Nn]eanch.*|" +
 			".*[Ss]bagli.*|" +
 			".*[Nn]on.*|";
 	
 	String name=".*[Gg]iord.*|" +
 			".*[Jj]arv.*|" +
 			".*[Gg]iard.*|" +
 			".*[Gg]iar.*|" +
 			".*[Gg]ior.*|" ;
 	
 	String movimento="^.*[Mm]ovimen.*|" +
 			"^.*[Mm]os.*|"+
 			"^.*[Qq]ual.*|"+
 			"^.*[Pp]ass.*|";
 	
 	String on=".*[Aa]ccendi.*|" +
 			".*[Aa]ttiv.*|"+
 			".*[Aa]pr.*";
 	
 	String off=".*[Ss]pegni.*|" +
 			".*[Dd]isattiv.*|" +
 			".*[Cc]hiud.*";
 	
 	String inizio="^[Hh]o [Dd]ett.*|";
 	
 	String inizio1="^[Tt]i [Hh]o [Dd]ett.*|"+
 				"^[Tt]i [Ss]to [Dd]ic.*|";
 	
 	String orario=".*[Oo]ra.*|"+
 			".*[Oo]rario.*|";
 	
 	String cerca=".*[Cc]erca.*|" +
 			".*[Rr]icerca.*|" +
 			".*[Tt]rova.*|";
 	
 	String sentimento = ".*[Cc]ome.*|";
 	
 	String ciao = ".*[Cc]iao.*|" +
 				  ".*[Cc]ia.*|";
 	
 	String cerca1 = "^[Cc]erca [Ss]u [Ii]nter.*|"+
 				    "^[Cc]erca [Ss]u [Cc]ro.*|";
 	
 	String allarme = ".*[Aa]llarm.*|" +
 					 ".*[Pp]erimet.*|" +
 					 ".*[Pr]otezione.*|";
 	
 	String uscita = ".*[Uu]scend.*|" +
 			 ".*[Uu]scir.*|" +
 			 ".*[Pp]art.*|" +
 			 ".*[Aa]nda.* .*[Vv]ia.*|";
 	
 	String entrata = ".*[En]tra.*|" +
 			 ".*[Tt]orn.*|" +
 			 ".*[Vv]ene.*|" +
 			 ".*[Aa]rriv.*|";
 	
 	String garage = ".*[Gg]arag.*|" +
 			 ".*[Ss]erran.*|";
 	
 	String ritornello = ".*[Ss]opra [Ll]a [Pp]anc.*|";
 }
