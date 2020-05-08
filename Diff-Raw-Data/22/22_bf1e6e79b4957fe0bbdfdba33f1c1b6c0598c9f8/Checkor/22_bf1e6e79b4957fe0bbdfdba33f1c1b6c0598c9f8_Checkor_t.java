 package fr.unice.polytech.devint.dcheckor.models;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.List;
 
 import org.jdom.Element;
 import org.jdom.JDOMException;
 import org.jdom.input.SAXBuilder;
 
 public class Checkor {
 
 	private int cdyear;
 	private String cdfolder;
 	
 	public String check() {
 		
 		String results = "<html>";
 		
 		//Test presence jre folder et subfolders
 		results += "<h2>Vérification du dossier jre</h2><ul>";
 		String jreFolderString = this.cdfolder + File.separator + "jre";
 		File jreFolder = new File (jreFolderString); 
 
     	if (jreFolder.exists() && jreFolder.isDirectory()) {
     		results += "<li><span style=\"color:green\">Le répertoire jre existe.</span>";
     		
     		results += "<ul>";
     		//Test presence win jre folder
     		String winJreFolderString = jreFolderString + File.separator + "win";
     		File winJreFolder = new File (winJreFolderString); 
 
         	if (winJreFolder.exists() && winJreFolder.isDirectory()) {
         		results += "<li><span style=\"color:green\">Le sous-répertoire jre/win existe.</span></li>";
         	} else {
         		results += "<li><span style=\"color:red\">Le sous-répertoire jre/win n'existe pas.</span></li>";
         	}
         	
         	//Test presence linux jre folder
     		String linuxJreFolderString = jreFolderString + File.separator + "linux";
     		File linuxJreFolder = new File (linuxJreFolderString); 
 
         	if (linuxJreFolder.exists() && linuxJreFolder.isDirectory()) {
         		results += "<li><span style=\"color:green\">Le sous-répertoire jre/linux existe.</span></li>";
         	} else {
         		results += "<li><span style=\"color:red\">Le sous-répertoire jre/linux n'existe pas.</span></li>";
         	}
         	
         	//Test presence mac jre folder
     		String macJreFolderString = jreFolderString + File.separator + "mac";
     		File macJreFolder = new File (macJreFolderString); 
 
         	if (macJreFolder.exists() && macJreFolder.isDirectory()) {
         		results += "<li><span style=\"color:green\">Le sous-répertoire jre/mac existe.</span></li>";
         	} else {
         		results += "<li><span style=\"color:red\">Le sous-répertoire jre/mac n'existe pas.</span></li>";
         	}
         	
         	results += "</ul>";
     		
     		results += "</li>";
     	} else {
     		results += "<li><span style=\"color:red\">Le répertoire jre n'existe pas.</span></li>";
     	}
 
     	results += "</ul>";
 		
 		//Test presence lib folder et subfolders
     	results += "<h2>Vérification du dossier lib</h2>\n<ul>\n";
 		String libFolderString = this.cdfolder + File.separator + "lib";
 		File libFolder = new File (libFolderString); 
 
     	if (libFolder.exists() && libFolder.isDirectory()) {
     		results += "<li><span style=\"color:green\">Le répertoire lib existe.</span>";
     		
     		results += "<ul>";
     		//Test presence win lib folder
     		String winLibFolderString = libFolderString + File.separator + "win";
     		File winLibFolder = new File (winLibFolderString); 
 
         	if (winLibFolder.exists() && winLibFolder.isDirectory()) {
         		results += "<li><span style=\"color:green\">Le sous-répertoire lib/win existe.</span></li>";
         	} else {
         		results += "<li><span style=\"color:red\">Le sous-répertoire lib/win n'existe pas.</span></li>";
         	}
         	
         	//Test presence linux lib folder
     		String linuxLibFolderString = libFolderString + File.separator + "linux";
     		File linuxLibFolder = new File (linuxLibFolderString); 
 
         	if (linuxLibFolder.exists() && linuxLibFolder.isDirectory()) {
         		results += "<li><span style=\"color:green\">Le sous-répertoire lib/linux existe.</span></li>";
         	} else {
         		results += "<li><span style=\"color:red\">Le sous-répertoire lib/linux n'existe pas.</span></li>";
         	}
         	
         	//Test presence mac lib folder
     		String macLibFolderString = libFolderString + File.separator + "mac";
     		File macLibFolder = new File (macLibFolderString); 
 
         	if (macLibFolder.exists() && macLibFolder.isDirectory()) {
         		results += "<li><span style=\"color:green\">Le sous-répertoire lib/mac existe.</span></li>";
         	} else {
         		results += "<li><span style=\"color:red\">Le sous-répertoire lib/mac n'existe pas.</span></li>";
         	}
         	
         	results += "</ul>";
     		
     		results += "</li>";
     	} else {
     		results += "<li><span style=\"color:red\">Le répertoire lib n'existe pas.</span></li>";
     	}
 
     	results += "</ul>";
     	
 		//Test presence Listor folder
     	results += "<h2>Vérification du dossier Listor</h2>\n<ul>\n";
 		String listorFolderString = this.cdfolder + File.separator + "Listor";
 		File listorFolder = new File (listorFolderString); 
 
     	if (listorFolder.exists() && listorFolder.isDirectory()) {
     		results += "<li><span style=\"color:green\">Le répertoire Listor existe.</span></li>";
     	} else {
     		results += "<li><span style=\"color:red\">Le répertoire Listor n'existe pas.</span></li>";
     	}
     	
     	results += "</ul>";
     	
    	//Test presence DListor folder
    	results += "<h2>Vérification du dossier DListor</h2>\n<ul>\n";
		String dlistorFolderString = this.cdfolder + File.separator + "DListor";
		File dlistorFolder = new File (dlistorFolderString); 

    	if (dlistorFolder.exists() && dlistorFolder.isDirectory()) {
    		results += "<li><span style=\"color:green\">Le répertoire DListor existe.</span></li>";
    	} else {
    		results += "<li><span style=\"color:red\">Le répertoire DListor n'existe pas.</span></li>";
    	}
    	
    	results += "</ul>";
    	
 		//Test presence VocalyzeSIVOX folder
     	results += "<h2>Vérification du dossier VocalyzeSIVOX</h2>\n<ul>\n";
 		String vocalyzeSIVOXFolderString = this.cdfolder + File.separator + "VocalyzeSIVOX";
 		File vocalyzeSIVOXFolder = new File (vocalyzeSIVOXFolderString); 
 
     	if (vocalyzeSIVOXFolder.exists() && vocalyzeSIVOXFolder.isDirectory()) {
     		results += "<li><span style=\"color:green\">Le répertoire VocalyzeSIVOX existe.</span></li>";
     	} else {
     		results += "<li><span style=\"color:red\">Le répertoire VocalyzeSIVOX n'existe pas.</span></li>";
     	}
     	
     	results += "</ul>";
     	
 		//Test presence LesLogiciels et test de structure de chaque projet
    	results += "<h2>Vérification du dossier LesLogiciels</h2>\n<ul>\n";
 		String gamesFolderString = this.cdfolder + File.separator + "LesLogiciels";
 		File gamesFolder = new File (gamesFolderString); 
 
     	if (gamesFolder.exists() && gamesFolder.isDirectory()) {
     		results += "<li><span style=\"color:green\">Le répertoire LesLogiciels existe.</span>";
     		
     		results += "<ul>";
     		
     		String[] gamesList = gamesFolder.list(); 
 
         	for (int i=0; i < gamesList.length; i++ ) {
         		String gameFolder = gamesFolder + File.separator + gamesList[i];
         		File game = new File(gameFolder); 
         		
         		if(game.exists() && game.isDirectory()) {
         			results += "<li><span><b>Vérification pour le projet dont le dossier est: " + gamesList[i] + "</b></span>";
         			results += "<ul>";
         			
        			//Vérification dossier bin et execution.bat/.sh
         			String gameBinFolder = gameFolder + File.separator + "bin";
         			File gameBin = new File(gameBinFolder); 
             		
             		if(gameBin.exists() && gameBin.isDirectory()) {
             			results += "<li><span style=\"color:green\">Le répertoire bin existe.</span>";
             			results += "<ul>";
             			
             			String gameBinExecBatFile = gameBinFolder + File.separator + "execution.bat";
             			File gameBinExecBat = new File(gameBinExecBatFile);
             			
             			if(gameBinExecBat.exists() && gameBinExecBat.isFile()) {
             				results += "<li><span style=\"color:green\">Le fichier bin/execution.bat existe.</span></li>";
             			} else {
             				results += "<li><span style=\"color:red\">Le fichier bin/execution.bat n'existe pas.</span></li>";
             			}
             			
             			String gameBinExecShFile = gameBinFolder + File.separator + "execution.sh";
             			File gameBinExecSh = new File(gameBinExecShFile);
             			
             			if(gameBinExecSh.exists() && gameBinExecSh.isFile()) {
             				results += "<li><span style=\"color:green\">Le fichier bin/execution.sh existe.</span></li>";
             			} else {
             				results += "<li><span style=\"color:red\">Le fichier bin/execution.sh n'existe pas.</span></li>";
             			}
             			
             			results += "</ul>";
             			results += "</li>";
             		} else {
             			results += "<li><span style=\"color:red\">Le répertoire bin n'existe pas.</span></li>";
             		}
             		
         			//Verification dossier src et compilation.bat/.sh
             		String gameSrcFolder = gameFolder + File.separator + "src";
         			File gameSrc = new File(gameSrcFolder); 
             		
             		if(gameSrc.exists() && gameSrc.isDirectory()) {
             			results += "<li><span style=\"color:green\">Le répertoire src existe.</span>";
             			results += "<ul>";
             			
             			String gameSrcCompilBatFile = gameSrcFolder + File.separator + "compilation.bat";
             			File gameSrcCompilBat = new File(gameSrcCompilBatFile);
             			
             			if(gameSrcCompilBat.exists() && gameSrcCompilBat.isFile()) {
             				results += "<li><span style=\"color:green\">Le fichier src/compilation.bat existe.</span></li>";
             			} else {
            				results += "<li><span style=\"color:red\">Le fichier src/compilation.bat n'existe pas.</span></li>";
             			}
             			
             			String gameSrcCompilShFile = gameSrcFolder + File.separator + "compilation.sh";
             			File gameSrcCompilSh = new File(gameSrcCompilShFile);
             			
             			if(gameSrcCompilSh.exists() && gameSrcCompilSh.isFile()) {
             				results += "<li><span style=\"color:green\">Le fichier src/compilation.sh existe.</span></li>";
             			} else {
             				results += "<li><span style=\"color:red\">Le fichier src/compilation.sh n'existe pas.</span></li>";
             			}
             			
             			results += "</ul>";
             			results += "</li>";
             		} else {
             			results += "<li><span style=\"color:red\">Le répertoire src n'existe pas.</span></li>";
             		}
             		
         			//Verification dossier doc et infos.xml
             		String gameDocFolder = gameFolder + File.separator + "doc";
         			File gameDoc = new File(gameDocFolder); 
             		
             		if(gameDoc.exists() && gameDoc.isDirectory()) {
             			results += "<li><span style=\"color:green\">Le répertoire doc existe.</span>";
             			results += "<ul>";
             			
             			String gameDocInfosFile = gameDocFolder + File.separator + "infos.xml";
             			File gameDocInfos = new File(gameDocInfosFile);
             			
             			if(gameDocInfos.exists() && gameDocInfos.isFile()) {
             				results += "<li><span style=\"color:green\">Le fichier doc/infos.xml existe.</span></li>";
             				
             				results += "<ul>" + this.checkInfosXmlFile(gameDocInfos) + "</ul>";
             				
             			} else {
             				results += "<li><span style=\"color:red\">Le fichier doc/infos.xml n'existe pas.</span></li>";
             			}
             			
             			results += "</ul>";
             			results += "</li>";
             		} else {
             			results += "<li><span style=\"color:red\">Le répertoire src n'existe pas.</span></li>";
             		}
             		
         			
         			results += "</ul>";
         			
         			results += "</li>";
         		} else {
         			results += "<li><span style=\"color:red\">Un fichier supplémentaire s'est glissé dans le répertoire LesLogciels: " + gameFolder + "</span></li>";
         		}
         	}
         	
         	results += "</ul>";
     		
     		results += "</li>";
     	} else {
     		results += "<li><span style=\"color:red;\">Le répertoire LesLogiciels n'existe pas.</span></li>";
     	}
 
     	results += "</ul>";
     	
 		results += "</html>";
 		
 		return results;
 	}
 	
 	public String checkInfosXmlFile(File infosFile) {
 		boolean ok = true;
 		String mistakes = "";
 		SAXBuilder builder = new SAXBuilder();
         try {
             Element xmlRoot = builder.build(infosFile).getRootElement();
 
             String year = xmlRoot.getChildText("year");
             
             if(year == null) {
             	ok = false;
             	mistakes += "<li><span style=\"color:red\">Il ne contient pas d'informations sur l'année (noeud 'year').</span></li>";
             }
 
             Element gameCategories = xmlRoot.getChild("gamecategories");
             
             if(gameCategories == null) {
             	ok = false;
             	mistakes += "<li><span style=\"color:red\">Il ne contient pas d'informations sur les catégories de jeu (noeud 'gamecategories').</span></li>";
             }
             
             
             List categories = gameCategories.getChildren("gamecategory");
             if(categories == null || categories.size() == 0) {
             	ok = false;
             	mistakes += "<li><span style=\"color:red\">Aucune catégorie de jeu n'est décrit (noeud 'gamecategory' sous-noeud de 'gamecategories').</span></li>";
             }
 
             String shortDescription = xmlRoot.getChildText("shortdescription");
             
             if(shortDescription == null) {
             	ok = false;
             	mistakes += "<li><span style=\"color:red\">Il ne contient pas de résumé (noeud 'shortdescription').</span></li>";
             }
             
             String publicString = xmlRoot.getChildText("public");
             
             if(publicString == null) {
             	ok = false;
             	mistakes += "<li><span style=\"color:red\">Il ne contient pas d'informations sur le type de public visé par ce jeu (noeud 'public').</span></li>";
             }
 
             String age = xmlRoot.getChildText("age");
             
             if(age == null) {
             	ok = false;
             	mistakes += "<li><span style=\"color:red\">Il ne contient pas d'informations sur l'age du public visé par ce jeu (noeud 'age').</span></li>";
             }
             
             String title = xmlRoot.getChildText("title");
 
             if(title == null) {
             	ok = false;
             	mistakes += "<li><span style=\"color:red\">Il ne contient pas de titre (noeud 'title').</span></li>";
             }
             
             Element authorsElem = xmlRoot.getChild("authors");
             
             if(authorsElem == null) {
             	ok = false;
             	mistakes += "<li><span style=\"color:red\">Il ne contient pas d'informations sur les auteurs du jeu (noeud 'authors').</span></li>";
             }
 
             List authors = authorsElem.getChildren("author");
             if(authors == null || authors.size() == 0) {
             	ok = false;
             	mistakes += "<li><span style=\"color:red\">Aucun auteur n'est enregistré pour ce jeu (noeud 'author' sous-noeud de 'authors').</span></li>";
             }
             
             Element notesElem = xmlRoot.getChild("notes");
             
             if(notesElem == null) {
             	ok = false;
             	mistakes += "<li><span style=\"color:red\">Il ne contient pas d'informations sur les notes du jeu (noeud 'notes').</span></li>";
             }
 
             List notes = notesElem.getChildren("note");
             if(notes == null || notes.size() == 0) {
             	ok = false;
             	mistakes += "<li><span style=\"color:#ED7F10\">Non Obligatoire: Aucune note n'est enregistré pour ce jeu (noeud 'note' sous-noeud de 'notes').</span></li>";
             }
 
             String gameplay = xmlRoot.getChildText("gameplay");
             
             if(gameplay == null) {
             	ok = false;
             	mistakes += "<li><span style=\"color:red\">Il ne contient pas d'informations sur les commandes du jeu (noeud 'gameplay').</span></li>";
             }
 
             String gamerules = xmlRoot.getChildText("gamerules");
             
             if(gamerules == null) {
             	ok = false;
             	mistakes += "<li><span style=\"color:red\">Il ne contient pas d'informations sur les règles du jeu (noeud 'gamerules').</span></li>";
             }
             
             if(ok) {
             	return "<li><span style=\"color:green\">Il ne manque aucune information sur ce jeu dans le fichier infos.xml.</span></li>";
             } else {
             	return mistakes;
             }
 
         } catch (JDOMException e) {
             return mistakes + "<li><span style=\"color:red\">Une erreur s'est produite pendant la lecture du fichier XML (pb de structure de fichier).</span></li>";
         } catch (IOException e) {
         	return mistakes + "<li><span style=\"color:red\">Une erreur s'est produite pendant la lecture du fichier XML (pb de lecture et non de structure du fichier).</span></li>";
         }
 	}
 	
 	public void setCDYear(int year) {
 		this.cdyear = year;
 	}
 	
 	public void setCDFolder(String cdfolder) {
 		this.cdfolder = cdfolder;
 	}
 }
