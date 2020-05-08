 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.StringTokenizer;
 
 import org.apache.hadoop.io.LongWritable;
 import org.apache.hadoop.io.Text;
 import org.apache.hadoop.mapreduce.InputSplit;
 import org.apache.hadoop.mapreduce.Mapper;
 import org.apache.hadoop.mapreduce.lib.input.FileSplit;
 
 /**
  * Le Mapper de MapReduce
  * En entrée on a pour clé l'offset de la ligne par rapport au début du fichier et en valeur la ligne
  * En sortie on a pour clé intermédiaire le mot , le nom du fichier et en valeur le numéro de 
  * la ligne dans lequel il se trouve
  * @author Corbel Elodie, Renou Clarisse
  * @see Mapper
  */
 public class IndexMapper extends Mapper<LongWritable, Text, Text, Text> {
 
 	/**
 	 * lineNumber int Numero de la ligne dans le fichier
 	 */
 	private int lineNumber=0;
 	/**
 	 * Fonction map, on a un mapper par fichier
 	 * @param key LongWritable l'offset de la ligne
 	 * @param values Text la ligne qu'on lit
 	 */
 	public void map(LongWritable key, Text values,
 			Context context) throws IOException, InterruptedException {
 		lineNumber++;
 		String line = values.toString().toLowerCase();//on ignore la casse
 		line=supprimerPonctuation(line);
 		StringTokenizer mots = new StringTokenizer(line, " ");
 		
 		//On recupere le nom de fichier
 		InputSplit split = context.getInputSplit();
 		String fileName= ((FileSplit) split).getPath().getName();
 		
 		/**
 		 * lineOffset int Décallage par rapport au debut de la ligne
 		 */
 		int lineOffset = 0;
 
 		while (mots.hasMoreTokens()){
 			lineOffset++;
 			Text t = new Text (mots.nextToken());//le mot
 			if (!motAIgnorer(t))
 				context.write(new Text(t + " " +fileName), new Text(""+lineNumber));
 		}
 	}
 	/**
 	 * @param text Text le mot à analyser
 	 * @return vrai si le mot est à ignorer (non pertinent)
 	 */
 	public static boolean motAIgnorer(Text text){
 		boolean res=false;
 		List<String> caracteresIgnores = new ArrayList<String>();
 		String[] tab = {"et", "ou", "où", "de", "des", "d", "le", "les","l","la","je","il","au","aux","du","un",
 				"une","a","à","or","ni","que","si","y","m","mon","ma","mes","me","ne",
 				"nous","on","sa","ses","se","qui","s","t","ta","tes","te","il","là","qu","sans","sur"};
 		caracteresIgnores=Arrays.asList(tab);
 		if (caracteresIgnores.contains(text.toString())){
 			res= true;
 		}
 		return res;
 	}
 	/**
 	 * 
 	 * @param texte String le texte dont on cherche à éliminer la ponctuation
 	 * @return le texte avec la ponctuation éliminée
 	 */
 	public static String supprimerPonctuation(String texte)
 	{
 		StringBuffer sb = new StringBuffer();
		for (String s : texte.split("[\\p{P}\\$\\+\t]")){
 			sb.append(" ");
 			sb.append(s);
 		}
 		return sb.toString();      
 	}
 
 }
