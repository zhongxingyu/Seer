 package william.miranda.recomendacao;
 
 import java.nio.file.Path;
 import java.nio.file.Paths;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Map;
 
 import william.miranda.imdb.model.Filme;
 import william.miranda.imdb.model.FilmeRating;
 import william.miranda.imdb.parser.ResultadoPredicao;
 import william.miranda.imdb.parser.UserParser;
 import william.miranda.lucene.LuceneDatabase;
 import william.miranda.lucene.LuceneResult;
 import william.miranda.lucene.LuceneSearch;
 import william.miranda.lucene.LuceneSearch.TipoSimilaridade;
 import william.miranda.xml.XMLParser;
 
 /** Esta classe implementa o algoritmo de Recomendação baseada em Itens
  * @author william.miranda
  */
 public class Recomendacao
 {
 	//estrutura para armazenar as entradas do u.data
 	private Map<Integer, List<FilmeRating>> mapRatings;
 	private UserParser userParser;
 	
 	//engine do Lucene
 	LuceneDatabase luceneDB;
 	
 	//inicializamos os pre requisitos
 	public Recomendacao()
 	{
 		//faz o parsing dos ratings dos usuarios guardando no objeto mapRatings
 		Path p = Paths.get("data/ml-100k/u.data");
 		userParser = new UserParser(p);
 		mapRatings = userParser.getUserRatings();
 		
 		//iniciamos a engine do Lucene com os diretorios de entrada e saida
 		Path localXml = Paths.get("out/");
 		Path localIndex = Paths.get("index/");
 		
 		luceneDB = new LuceneDatabase(localXml, localIndex, false);
 	}
 	
 	//obtem as triplas do arquivo u.data, chamando o algorito de predicao para cada tripla
 	public List<ResultadoPredicao> percorrerAvaliacoes(int numFilmesSimilares, TipoSimilaridade tipoSimilaridade)
 	{
 		//lista que ira conter todos os resultados;
 		List<ResultadoPredicao> resultados = new ArrayList<>();
 		
 		//para cada user, pega a Lista de Reviews
 		for (int userId : mapRatings.keySet())
 		{
 			List<FilmeRating> lista = mapRatings.get(userId);
 			
 			//varremos a lista para obter as triplas originais
 			for (FilmeRating fr : lista)
 			{
 				int filmeId = fr.getFilmeId();
 				int rating = fr.getRating();
 				
 				//tendo a tripla original do arquivo, jogamos no algoritmo
 				float notaPredita = PredizerNota(userId, filmeId, rating, numFilmesSimilares, tipoSimilaridade);
 				
 				//agora gravamos a quadrupla em um arquivo
 				resultados.add(new ResultadoPredicao(userId, filmeId, rating, notaPredita));
 			}
 		}
 		
 		return resultados;
 	}
 	
 	/* Este metodo implementa o algorito de Predição
 	 * O objetivo eh "adiviinhar" a nota de um usuario U daria para um filme F
 	 * baseado nas outras notas da base de dados.
 	 * A tripla que entra nesse metodo é o "grupo de teste" e todo o resto eh o "grupo de treinamento" */
 	public float PredizerNota(int userId, int filmeId, int rating, int numFilmesSimilares, TipoSimilaridade tipoSimilaridade)
 	{
 		//obtemos o XML do filme que foi passado
 		Filme f = XMLParser.parseXML(Paths.get("out/" + filmeId + ".xml"));
 		
 		//obtemos a média das notas do filmeId, desconsiderando a tripla atual (que foi passada como parametro)
 		float media_i = userParser.mediaRatingFilme(filmeId, userId);
 		
 		//caso nao tenha o XML do filme (pois nao ha como calcular os dados)
 		if (f == null)
 			return media_i;
 		
 		//calculamos as similaridades para o filme passado
 		LuceneSearch luceneSearch = new LuceneSearch(f, luceneDB, numFilmesSimilares);
 		List<LuceneResult> listaSimilares = luceneSearch.getMetadado(tipoSimilaridade);
 		
 		//caso nao tenha como obter os filmes similares, retornaremos a media_i (nao ha o metadado no XML do filmeID)
 		if (listaSimilares == null || listaSimilares.size() == 0)
 			return media_i;
 		
 		/*  algoritmo  */
 		float soma = 0;
 		float sim_soma = 0;
 		
 		//pega as avaliacoes dos filmes similares a filmeId que foram avaliadas por userId
 		for (LuceneResult lr : listaSimilares)
 		{
 			FilmeRating filmeRating = userParser.getTripla(userId, lr.getId());//pega o filme similar que foi avaliado por userId
 			
 			if (filmeRating == null)//se o usuario nao avaliou o filme "lr"
 				continue;
 			
 			float media_j = userParser.mediaRatingFilme(lr.getId(), userId);//calcula a media da nota do filme similar
 			soma += lr.getSimilaridade() * (filmeRating.getRating() - media_j);
 			sim_soma += lr.getSimilaridade();
 		}
 		
 		float nota_predita_u_i;
 		
 		if (sim_soma != 0)//se deu tudo certo
 			nota_predita_u_i = media_i + (soma/sim_soma);
 		else//se o usuario nao avaliou nenhum dos filmes similares
 			nota_predita_u_i = media_i;
 		
 		if (nota_predita_u_i > 5)//trunca as notas para o teto (5.0)
 			nota_predita_u_i = 5.0f;
 		
 		if (nota_predita_u_i < 0)//trunca as notas para o piso (0.0)
 			nota_predita_u_i = 0.0f;
 		
 		return nota_predita_u_i;
 	}
 	
 	public static double RMSE(List<ResultadoPredicao> resultados)
 	{
 		float soma = 0;
 		
 		for (ResultadoPredicao r : resultados)
 		{
 			soma += Math.pow(r.getNotaOriginal()-r.getNotaPredita(), 2);
 		}
 		
 		//RMSE=sqrt(sum(nota_real -nota_predita)^2) / qtde_notas
		return Math.sqrt(soma)/UserParser.getNumeroAvaliacoes();
 	}
 }
