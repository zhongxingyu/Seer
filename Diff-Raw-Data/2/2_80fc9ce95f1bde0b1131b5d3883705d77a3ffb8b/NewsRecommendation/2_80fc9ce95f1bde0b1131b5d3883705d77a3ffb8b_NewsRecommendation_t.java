 import java.io.BufferedReader;
 import java.io.BufferedWriter;
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Comparator;
 import java.util.HashMap;
 import java.util.List;
 import java.util.Map.Entry;
 import java.util.Iterator;
 
 import org.atilika.kuromoji.Token;
 import org.atilika.kuromoji.Tokenizer;
 
 /*
  * NewsRecommendation
  * ニュース閲覧記録からtf-idfを用いて新着ニュースの推薦を行うプログラム
  * 各記事を形態素解析して出現する名詞を抽出、閲覧済みニュースにある名詞とのtf-idfを求める。
  */
 public class NewsRecommendation  {
 
 	/* 設定変数 */
 	
 	// 新着記事における単語の出現情報ファイル出力先ディレクトリ
 	public static final String OUTPUT_PATH_OF_TERM = "./output/";
 	// 推薦記事情報出力先ディレクトリ
 	public static final String OUTPUT_PATH_OF_OUT = "./output/";
 	// 新着記事の形態素解析結果出力ディレクトリ
 	public static final String OUTPUT_PATH_OF_MOR = "./morphol/";
 	// 過去の記事が格納されているディレクトリ
 	public static final String INPUT_PATH_OF_PAST = "./past/";
 	// 新着記事が格納されているディレクトリ
 	public static final String INPUT_PATH_OF_NEW = "./new/";
 	
 	/* 設定変数 ここまで */
 	
 
 	// ニュース閲覧記録ファイル名
 	private String favoriteFile = null;
 	// 閲覧済ニュースの総単語(名詞)数
 	private int numOfTerms = 0;
 
 	// 閲覧済単語と出現頻度を保持するMap
 	private HashMap<String, Integer> favTerms = null;
 	// 単語とそれが出現する記事を保持するするMap
 	private HashMap<String, ArrayList<String>> dictionary = null;
 	// 形態素解析情報出力ファイル名一覧
 	private ArrayList<String> morFileList = null;
 	// 各新着記事のTf-Idf値
 	private ArrayList<TfIdf> scores = null;
 
 
 	/*
 	 * コンストラクタ
 	 */
 	public NewsRecommendation(){
 		// default constructor
 	}
 
 	/*
 	 * コンストラクタ
 	 * @param
 	 * favoriteFile 閲覧済記事タイトル一覧ファイル名
 	 */
 	public NewsRecommendation(String favoriteFile){
 		this.favoriteFile = favoriteFile;
 	}
 
 	/*
 	 * Setter
 	 */
 	public void setFavoriteFile(String favoriteFile){
 		this.favoriteFile = favoriteFile;
 	}
 
 	/*
 	 * 実行関数
 	 * インスタンス生成元が呼び出すのはこのメソッドです。
 	 */
 	public void run(){
 		readFavorite();
 		readNew();
 		calcTfIdf();
 		output();
 	}
 
 	/*
 	 * 閲覧済記事読み込み関数
 	 * 閲覧済記事を読み込み、形態素解析して単語(名詞)の出現頻度を求める。
 	 * 記事中で#で始まる行は無視される。
 	 */
 	private void readFavorite(){
 
 		if( this.favoriteFile == null ){
 			System.err.println("[ERROR] : <favorite_article_list_file> is not set to variable in class.");
 			System.exit(-1);
 		}
 
 		// タイトル一覧(ファイル名)を取得
 		favTerms = new HashMap<String, Integer>();
 		BufferedReader br = null;
 		try {
 			br = new BufferedReader(new FileReader(favoriteFile));
 		} catch (FileNotFoundException e) {
 			System.err.println("[ERROR] : <favorite_article_list_file> of arguments is not found.");
 			System.exit(-1);
 		}
 		String line = null;
 		try{
 			while( (line = br.readLine()) != null ){
 				// 各記事にアクセス
 				BufferedReader br_past = new BufferedReader(new FileReader(INPUT_PATH_OF_PAST+line));
 				String pastLine, text = null;
 				while( (pastLine = br_past.readLine()) != null ){
 					if( pastLine.indexOf("#") != 0 )
 						text += pastLine;
 				}
 				br_past.close();
 				// 形態素解析・名詞を集計
 				Tokenizer tokenizer = Tokenizer.builder().build();
 				List<Token> tokens = tokenizer.tokenize(text);
 				for (Token token : tokens) {
 					if( token.getAllFeatures().indexOf("名詞") != -1 ){
 						++numOfTerms;
 						int cnt = 1;
 						if( favTerms.containsKey(token.getSurfaceForm()) )
 							cnt = favTerms.get(token.getSurfaceForm())+1;
 						favTerms.put(token.getSurfaceForm(), cnt);
 					}
 				}
 			}
 			br.close();
 		}catch(IOException e){
 			System.err.println("[ERROR] : IOException while processing file -> "+line);
 			System.exit(-1);
 		}
 		
 		// 解析結果をファイル出力
         String[] spFav = favoriteFile.split("/");
 		try{
 			BufferedWriter bw = new BufferedWriter(new FileWriter(OUTPUT_PATH_OF_TERM+spFav[spFav.length-1]+".term"));
 			bw.write(numOfTerms+"\n");
 			for (Iterator<Entry<String, Integer>> it = favTerms.entrySet().iterator(); it.hasNext();) {
 				Entry<String, Integer> entry = it.next();
 				String key = (String) entry.getKey();
 				Integer value = (Integer) entry.getValue();
 				bw.write(key+"|"+value+"\n");
 			}
 			bw.close();
 		}catch(IOException e){
 			System.err.println("[ERROR] : IOException while outputting "+OUTPUT_PATH_OF_TERM+spFav[spFav.length-1]+".terms file.");
             e.printStackTrace();
 			System.exit(-1);
 		}
 	}
 
 	/*
 	 * 新着記事読み込み関数
 	 * 新着記事を読み込み、形態素解析して単語(名詞)の出現頻度を求める。
 	 * 記事中で#で始まる行は無視される。
 	 */
 	private void readNew(){
 		dictionary = new HashMap<String, ArrayList<String>>();
 		morFileList = new ArrayList<String>();
 		// 新着記事一覧を取得
 		File dir = new File(INPUT_PATH_OF_NEW);
 		File[] files = dir.listFiles();
 		for (int i = 0; i < files.length; i++) {
 			File file = files[i];
 			try{
 				String[] spPath = file.toString().split("/");
                 String outFileName = OUTPUT_PATH_OF_MOR+spPath[spPath.length-1]+".mor";
 				morFileList.add(outFileName);
     	   		// 新着記事にアクセス
     			BufferedReader br_new = new BufferedReader(new FileReader(file));
     			String newLine, text = spPath[spPath.length-1]+"。";
     			while( (newLine = br_new.readLine()) != null ){
     				if( newLine.indexOf("#") != 0 )
     					text += newLine;
     			}
     			br_new.close();
     
     			HashMap<String, Integer> terms = new HashMap<String, Integer>();
     			int termCnt = 0;
     			// 形態素解析・名詞を集計
     			Tokenizer tokenizer = Tokenizer.builder().build();
     			List<Token> tokens = tokenizer.tokenize(text);
     			for (Token token : tokens) {
     				if( token.getAllFeatures().indexOf("名詞") != -1 ){
     					++termCnt;
     					int cnt = 1;
     					if( terms.containsKey(token.getSurfaceForm()) )
    							cnt = terms.get(token.getSurfaceForm())+1;
    						terms.put(token.getSurfaceForm(), cnt);
    					}
    				}
    
    				// 解析結果をファイル出力
     			BufferedWriter bw_new = new BufferedWriter(new FileWriter(outFileName));
     			bw_new.write(termCnt+"\n");
     			for (Iterator<Entry<String, Integer>> it = terms.entrySet().iterator(); it.hasNext();) {
     				Entry<String, Integer> entry = it.next();
     				String key = (String) entry.getKey();
     				Integer value = (Integer) entry.getValue();
     				bw_new.write(key+"|"+value+"\n");
     				ArrayList<String> list = dictionary.get(key);
     				if( list == null )
     					list = new ArrayList<String>();
     				if( !list.contains(spPath[spPath.length-1]))
     					list.add(spPath[spPath.length-1]);
     				dictionary.put(key, list);
     			}
     			bw_new.close();
 			}catch(Exception e){
 				System.err.println("[ERROR] : IOException while processing file -> "+file);
 				e.printStackTrace();
 				System.exit(-1);
 			}
 		}

 		// 全単語と出現する記事名をファイル出力
 		try{
 			BufferedWriter bw = new BufferedWriter(new FileWriter(OUTPUT_PATH_OF_MOR+"terms.txt"));
 			for (Iterator<Entry<String, ArrayList<String>>> it = dictionary.entrySet().iterator(); it.hasNext();) {
 				Entry<String, ArrayList<String>> entry = it.next();
 				String key = (String) entry.getKey();
 				ArrayList<String> value = (ArrayList<String>) entry.getValue();
 				for( int j=0; j<value.size(); j++ )
 					bw.write(key+"|"+value.get(j)+"\n");
 			}
 			bw.close();
 		}catch(IOException e){
 			System.err.println("[ERROR] : IOException while outputting terms.txt file.");
 			System.exit(-1);
 		}
 	}
 
 	/*
 	 * 閲覧済記事に含まれる単語について、Tf-Idfを求める関数
 	 * 最終的な記事のスコアはTf-Idf値の和で算出される。
 	 */
 	private void calcTfIdf(){
 		scores = new ArrayList<TfIdf>();
 		for( int i=0; i<morFileList.size(); i++ ){
 			String file = morFileList.get(i);
 			String[] spPath = file.split("/");
 			String fileName = spPath[spPath.length-1].replace(".mor","");
 			try{
 				HashMap<String, Integer> map = new HashMap<String, Integer>();
 				BufferedReader br = new BufferedReader(new FileReader(file));
 				int numTerms = Integer.parseInt(br.readLine());
 				String line = null;
 				while( (line = br.readLine()) != null ){
 					String[] spLine = line.split("\\|");
 					map.put(spLine[0], Integer.parseInt(spLine[spLine.length-1]));
 				}
 				br.close();
 				double tfIdf = 0.0;
 				for (Iterator<Entry<String, Integer>> it = favTerms.entrySet().iterator(); it.hasNext();) {
 					Entry<String, Integer> entry = it.next();
 					String key = (String) entry.getKey();
 					Integer value = (Integer) entry.getValue();
 					if( map.containsKey(key) ){
 						/* //debug
 						System.out.println(value);
 						System.out.println(numOfTerms);
 						System.out.println(map.get(key));
 						System.out.println(numTerms);
 						System.out.println(morFileList.size());
 						System.out.println(dictionary.get(key).size());
 						System.out.println("====================");		
 						*/
 						// Tf-Idf 計算式
 						tfIdf += ((double)map.get(key)/(double)numTerms)*Math.log10(morFileList.size()/dictionary.get(key).size());
 					}
 				}
 				scores.add(new TfIdf(fileName, tfIdf));
 			}catch( Exception e ){
 				System.err.println("[ERROR] : Exception while processing file => "+file);
 				e.printStackTrace();
 				System.exit(-1);
 			}
 		}
 		Collections.sort(scores, new Comparator<Object>(){
 				public int compare(Object o1, Object o2){
 				return (-1)*(int) (((((TfIdf)o1).getScore()-((TfIdf)o2).getScore()))*1e+7);
 				}
 				});
 	}
 
 	/*
 	 * 推薦記事出力関数
 	 * 推薦記事をスコアと共にファイル出力します。
 	 * 記事が選ばれるわけではなく、ランキングされた全記事が出力されます。
 	 */
 	private void output(){
 		try{
             String[] spOut = favoriteFile.split("/");
 			BufferedWriter bw = new BufferedWriter(new FileWriter(OUTPUT_PATH_OF_OUT+spOut[spOut.length-1]+".out"));
 			for( int i=0; i<scores.size(); i++ ){
 				bw.write(scores.get(i).getScore()+"\t"+scores.get(i).getTitle()+"\n");
 			}
 			bw.close();
 		}catch( IOException e ){
 			System.err.println("[ERROR] : IOException while outputting results.");
 			e.printStackTrace();
 			System.exit(-1);
 		}
 	}
 
 	/*
 	 * メインルーチン
 	 * @param
 	 * args[0] 閲覧済(お気に入り)記事タイトル一覧ファイル
 	 */
 	public static void main(String[] args) {
 
 		if( args.length < 1 ){
 			System.out.println("USAGE : java NewRecommendation <favorite_article_list_file>");
 			System.exit(-1);
 		}
 
 		NewsRecommendation nr = new NewsRecommendation(args[0]);
 		System.out.println("[Info] Favorite List File : "+args[0]);
 		nr.run();
 		System.out.println("[Finish] Complete News Recommendation!");
 
 	}
 
 
     /*
      * Tf-Idf値保持用クラス
      * 各記事の評価値(Tf-Idf値の和)を保持します。
      */
     class TfIdf {
     
     	// 記事タイトル
     	private String title;
     	// 評価値
     	private double score;
     
     	/*
     	 * コンストラクタ
     	 */
     	public TfIdf(String title, double score){
     		this.title = title;
     		this.score = score;
     	}
     
     	public String getTitle(){
     		return this.title;
     	}
     
     	public double getScore(){
     		return this.score;
     	}
     
     }
 
 
 }
 
 
