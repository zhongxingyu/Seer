 package dcll.grp3.morphoQuizz;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
 
 import net.sf.json.JSON;
 import net.sf.json.xml.XMLSerializer;
 
 import org.apache.commons.io.IOUtils;
 
 public class QuizzConverter {
 
 	/**
 	 * Lit le fichier de quizz XML xmlInput, ecrit le test convertit en JSON dans le fichier jsonOutput
 	 * @param xmlInput Ficher de Quizz XML
 	 * @param jsonOutput Fichier de sortie JSON
 	 * @throws IOException
 	 */
 	public static void XMLQuizzToJson(File xmlInput, File jsonOutput)
 			throws IOException {
 		InputStream is = new FileInputStream(xmlInput);
 		
 
 		String xml = IOUtils.toString(is);
 
 		XMLSerializer xmlSerializer = new XMLSerializer();
 		JSON json = xmlSerializer.read(xml);
 
 		OutputStream os = new FileOutputStream(jsonOutput);
 		IOUtils.write(json.toString(2), os);
 		
 		is.close();
 		os.close();
 	}
 
 	/**
 	 * Lit le fichier de quizz json jsonInput, ecrit le test convertit en XML dans le fichier xmlOutput
 	 * @param jsonInput Ficher de Quizz Json
 	 * @param xmlOutput Fichier de sortie XML
 	 * @throws IOException
 	 */
 	public static void JsonQuizzToXML(File jsonInput, File xmlOutput)
 			throws IOException {
		
 	}
 }
