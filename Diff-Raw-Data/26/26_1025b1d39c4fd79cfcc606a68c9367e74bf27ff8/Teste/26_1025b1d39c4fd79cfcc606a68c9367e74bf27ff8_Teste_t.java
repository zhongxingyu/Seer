 package william.miranda.imdb;
 
 import java.io.IOException;
 
 import william.miranda.imdb.model.Filme;
 import william.miranda.imdb.parser.HtmlParser;
import william.miranda.imdb.parser.Utils;
 
 public class Teste
 {
 	public static void main(String[] args)
 	{
 		try
 		{
 			String url;
 			
 			if (args.length == 0)
 			{
 				System.out.println("Como usar:");
 				System.out.println("java -jar imdb.java URL_DO_FILME");
 				System.exit(-1);
 			}
 			
 			url = args[0];
 			
 			//url = "http://www.imdb.com/title/tt0113189";//GoldenEye
 			//url = "http://www.imdb.com/title/tt0082398";//For Your Eyes Only
 			//url = "http://www.imdb.com/title/tt1038988";//Rec
 			//url = "http://www.imdb.com/title/tt0162930";//Die Hard Dracula
 			
			url = Utils.preparaURL(url);
			
 			HtmlParser pick = new HtmlParser(url);
 			pick.parseURL();
 			pick.parseReviews();
 			
 			//neste ponto temos um objeto do tipo Filme preenchido
 			Filme f = pick.getFilme();
 			
 			System.out.println(f);
 			//f.toXML();
 		}
 		catch (IOException e)
 		{
 			// TODO Auto-generated catch block
 			e.printStackTrace();
 		}
 	}
 }
