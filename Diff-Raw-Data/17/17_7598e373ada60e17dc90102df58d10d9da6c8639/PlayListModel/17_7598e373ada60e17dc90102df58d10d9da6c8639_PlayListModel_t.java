 import java.io.*;
 import java.util.*;
import java.util.regex.*;
 import javax.swing.AbstractListModel;
 
 /**
  * Classe gérant la playlist.
  * @author Benjamin
  * @version 1.0
  */
 
 public class PlayListModel extends AbstractListModel {
 	/** ArrayList de File */
 	protected ArrayList<File> playlist;
 
 	/** Constructeur par défaut 
 	* @param list
 	*   Une liste contenant la playlist	
 	*/
 	public PlayListModel (ArrayList<File> list){
 		playlist = list;
 	}
 
 	/** Constructeur utilisant un File, fichier ou répertoire 
 	* @param file
 	*  Fichier ou répertoire contenant la playlist
 	*/
 	public PlayListModel(File file){
 		this(new ArrayList<File>());
 		add(file);
 	}
 
 	/** Constructeur utilisant un String 
 	* @param path
 	*   Chemin vers un fichier ou un répertoire contenant la playlist
 	*/
 	public PlayListModel (String path){
 		this(new File(path));
 	}
 
 	/** Ajoute à la playlist un fichier ou le contenu d'un répertoire
 	* @param file
 	*   Fichier à ajouter
 	*/
 	protected void add(File file){
 		// Vérifie si le fichier passé en argument est un fichier contenant une palylist ou un fichier musical
 		if(	Pattern.matches(".*\\.mp3$", file.getName()) |
 			Pattern.matches(".*\\.wav$", file.getName()) |
 			Pattern.matches(".*\\.ogg$", file.getName()) |
 			Pattern.matches(".*\\.flac$", file.getName()))
 		{
 			playlist.add(file);
 		}
 		else {
 			if(file.canRead())
 			{
 				if( file.isFile())
 				{
 					BufferedReader in = null;
 					try{
 						in = new BufferedReader(new FileReader(file));
 					}
 					catch (IOException e)
 					{
 						System.out.println("Unable to read " + file.getAbsolutePath());
 					}
 					String line = "";
 					try
 					{
 							line = in.readLine();
 					}
 					catch (IOException e)
 					{
 						System.out.println("Unable to read the first line");
 					}
 
 					while (line != null)
 					{
 						if(line.charAt(0) == '/')
 						{
 							playlist.add(new File(line));
 						}
 						else 
 						{
 							playlist.add(new File(file.getParent()+"/"+line));
 						}
 						try
 						{
 							line = in.readLine();
 						}
 						catch (IOException e)
 						{
 							System.out.println("Unable to read the next line");
 						}
 					}
 				}
 				if( file.isDirectory())
 				{
 					File[] temp = file.listFiles();
 					int size = temp.length;
 					for (int i = 0 ; i < size ; i++)
 					{
 						playlist.add(temp[i]);
 					}
 				} 
 			}
 			else
 			{
 				if( file.isFile())
 				{
 					System.out.println("Can't read the file "+file.getAbsolutePath());
 				}
 				if( file.isDirectory())
 				{
 					System.out.println("Can't open the directory "+file.getAbsolutePath());
 				}
 			}
 		}
 	}
 
 	/** Ajoute à la playlist un fichier ou le contenu d'un répertoire
 	* @param path
 	*   Chemin du fichier à ajouter
 	*/
 	protected void add(String path){
 		this.add(new File(path));
 	}
 
 	/** Enlève à la playlist un fichier
 	* @param file
 	*   Fichier à retirer
 	*/
 	protected void remove(File file){
 		playlist.remove(file);
 	}
 
 	/** Retourne l'élément de la playlist qui à pour index <i>index</i>
 	* @param index
 	*   Index de l'élément recherché
 	* @return
 	*   Un fichier de la playlist
 	*/
 	public File getElementAt(int index){
 		return playlist.get(index);
 	}
 
 	/** Retourne la taille de la playlist
 	* @return
 	*   La taille de la playlist
 	*/
 	public int getSize(){
 		return playlist.size();
 	}
 
 	/** Mélange la playlist	*/
 	public void randomize(){
 		ArrayList<File> result = new ArrayList<File>();
 		double size;
 		int random;
 		File temp;
 		while (!playlist.isEmpty()){
 			size = ((double)playlist.size());
 			random = ((int)(Math.random()*size));
 			temp = playlist.get(random);
 			result.add(temp);
 			playlist.remove(temp);
 		}
 		playlist=result;
 	}	
 
 	/**
 	* Affiche la playlist
 	* @return
 	*   Un String contenant le chemin de tous les fichiers de la playlist
 	*/
 	protected String displayList(){
 		String result = "";
 		int size = playlist.size();
 		for (int i = 0; i < size; i++)
 		{
 			result = result + "fichier " + i + " : " + playlist.get(i).getAbsolutePath()+"\n";
 		}
 		return result;
 	}
 
 	public void play(){
		int size = getSize();
 		for (int i = 0; i < size; i++)
 		{
 			
 			File file = getElementAt(i);
 			System.out.println("Fichier à lire : "+file);
 			Player play = new Player(file);
 		}
 	}
 
 
 	/** Main (pour tests) !
 	* @param args
 	*   Options (inutilisés pour l'instant)
 	*/
 
 	public static void main(String[] args){
 		PlayListModel test = new PlayListModel("/home/infoetu/vanryseb/Java/XP/AudioPlayer/src");
 		System.out.println(test.displayList());
 		test.randomize();
 		System.out.println(test.displayList());
 		PlayListModel test2 = new PlayListModel("/home/infoetu/vanryseb/Java/XP/AudioPlayer/test");
 		System.out.println(test2.displayList());
 	}
 
 }
 
