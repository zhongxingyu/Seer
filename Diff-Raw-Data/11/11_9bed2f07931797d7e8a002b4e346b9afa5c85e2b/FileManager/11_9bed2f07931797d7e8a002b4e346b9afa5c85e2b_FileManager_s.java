 package mission.one;
 
 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 
 public class FileManager
 {
 	/*
 	 * ReadFile permet de lire tout le contenu d'un fichier ligne par ligne et
 	 * de stocker chacunes des ces lignes dans un ArrayList. Return : - retourne
	 * un ArrayList de string contenant les lignes du fichier pass� en param�tre
 	 * - retourne null si le fichier n'existe pas
 	 */
 
 	public static ArrayList<String> readFile(String filename)
 			throws IOException
 	{
 
 		File file = new File(filename);
 		if (file.exists())
 		{
 
 			BufferedReader bufreader = new BufferedReader(new FileReader(file));
 			ArrayList<String> content = new ArrayList<String>();
 			String currentLine = null;
 			while ((currentLine = bufreader.readLine()) != null)
 			{
 				content.add(currentLine);
 			}
 			bufreader.close();
 			return content;
 		}
 		else System.out.println("Read failed : File doesn't exist");
 		return null;
 	}
 
 	/*
	 * WriteInFile est une m�thode qui permet de placer un string � la fin d'un
	 * fichier texte. Si le fichier existe d�j�, le String sera plac� en fin de
	 * fichier. Sinon, un nouveau fichier (filename) sera cr�e et le String
	 * content y sera plac�
 	 */
 	public static void writeInFile(String filename, String content)
 			throws IOException
 	{
 		BufferedWriter bufwriter = new BufferedWriter(new FileWriter(filename,
 				true));
 		bufwriter.write(content);
 		bufwriter.newLine();
 		bufwriter.close();
 	}
 }
