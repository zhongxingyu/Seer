 package jp.irof.ac;
 
 import hudson.util.PluginServletFilter;
 
 import java.io.FileWriter;
 import java.io.IOException;
 import java.util.HashMap;
 import java.util.LinkedHashMap;
 import java.util.Map;
 import java.util.regex.Pattern;
 
 import javax.servlet.FilterChain;
 import javax.servlet.ServletException;
 import javax.servlet.ServletRequest;
 import javax.servlet.ServletResponse;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /**
  * サーブレットフィルタクラス。<br>
  * Jenkinsのアプリ中のURLにアクセスされた場合、それが「特定の画像のリクエスト」である場合、<br>
  * 別の画像にリダイレクトするフィルター。
  *
  * @author Kazuhito Miura
  */
 public class ImageRequestFilter extends PluginServletFilter {
 
 	/*
 	 * url-pathの変換は正規表現を使って行う。
 	 * 正規表現(Patternオブジェクト作成)は若干のコストがかかると思われるため、ClassLoad時
 	 * (staticの確保時)に作ってしまう戦略で行く。
 	 */
 
 	/** プラグインのイメージが置いてあるpath(URL内) */
 	private static String PATH = "/plugin/irofkins/irof-images/";
 
 	/** 一次フィルタリング条件 */
 	private static final Pattern FIRST_FILTER = Pattern
 			.compile(".*/(title|jenkins|angry-jenkins|sad-jenkins).png");
 
 	/** 二次フィルタリング条件 & 置き換え文字Map */
 	@SuppressWarnings("serial")
 	private static final Map<String, String> SECCOND_FILTERS = new LinkedHashMap<String, String>() {
 		{
 			put("/plugin/emotional-jenkins-plugin/images/angry-jenkins.png",
 					PATH + "angry-jenkins.png");
 			put("/plugin/emotional-jenkins-plugin/images/sad-jenkins.png", PATH
 					+ "sad-jenkins.png");
 			put("/plugin/emotional-jenkins-plugin/images/jenkins.png", PATH
 					+ "jenkins.png");
 			put("/static/.*/images/jenkins.png", PATH + "jenkins.png");
 			put("/static/.*/images/title.png", PATH + "title.png");
 			put("/images/jenkins.png", PATH + "jenkins.png");
 			put("/images/title.png", PATH + "title.png");
 		}
 	};
 
 	/**
 	 * フィルタ処理。
 	 *
 	 * @param request HTTPリクエストオブジェクト。<br>
 	 * @param response HTTPレスポンスオブジェクト。
 	 * @param chain 伝搬する次のフィルタ。
 	 * @throws IOException IO例外。
 	 * @throws ServletException サーブレット起因の例外。
 	 */
 	@Override
 	public void doFilter(ServletRequest request, ServletResponse response,
 			FilterChain chain) throws IOException, ServletException {
 		// 作業用に変数取る。
 		String path = ((HttpServletRequest) request).getRequestURI();
 		
 		// デバッグソース
 		FileWriter f = new FileWriter("./irofkins_test.log");
 		f.write("\npath:" + path);
 			
 		// 一次フィルタリング。
 		// すべてのリクエストに4回ずつ比較は無駄が多そうなので、一つの正規表現で篩にかける。
 		if (FIRST_FILTER.matcher(path).matches()) {
 			f.write("\first match!");
 			// 引っかかったら、二次フィルタリング。
 			// 該当したら、その当該部分だけを文字列置換しリダイレクト。
 			for (String src : SECCOND_FILTERS.keySet()) {
 				if (path.matches(".*" + src)) {
 					String dest = SECCOND_FILTERS.get(src);
 					String replacedPath = path.replaceAll(src, dest);
 					f.write("\nsecand match!");
					f.write("\nsrc:" + src + " , dest:" + dest + " , replacedPath:" + replasedPath);
 					((HttpServletResponse) response).sendRedirect(replacedPath);
 					break;
 				}
 			}
 		}
 		f.close();
 		chain.doFilter(request, response);
 	}
 
 	/**
 	 * オブジェクト破棄時の処理(デストラクタ)。
 	 */
 	@Override
 	public void destroy() {
 	}
 
 }
