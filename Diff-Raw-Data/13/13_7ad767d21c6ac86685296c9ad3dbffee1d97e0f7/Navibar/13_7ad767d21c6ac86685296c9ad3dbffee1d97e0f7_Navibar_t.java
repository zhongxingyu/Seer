 package pages.navibar;
 
 import java.io.File;
import java.util.Arrays;
import java.util.Comparator;
 
 import model.Article;
 
 import config.BlogConfig;
 import utils.DirUtils;
 
 /**
  * @author chao
  */
 public class Navibar {
 	private static Article[] NavibarLs; 
 	static {
 		File[] files = DirUtils.ls(BlogConfig.getIns().inputpath+"/navibar");
 		NavibarLs = new Article[files.length];
 		
		Arrays.sort(files, new Comparator<File>() {

			@Override
			public int compare(File o1, File o2) {
				return o1.getName().compareTo(o2.getName());
			}
		});
		
 		int i = -1;
 		for (File file : files) {
 			Article article = new Article();
 			article.readArticle(file.getAbsolutePath());
 			NavibarLs[++i] = article;
 		}
 	}
 	public static Article[] getNavibarLs(){
 		return NavibarLs;
 	}
 }
