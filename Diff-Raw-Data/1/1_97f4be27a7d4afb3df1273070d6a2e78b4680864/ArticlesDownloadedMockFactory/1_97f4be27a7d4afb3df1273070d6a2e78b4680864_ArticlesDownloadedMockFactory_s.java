 /**
  *    This file is part of PubShare.
  *
  *    PubShare is free software: you can redistribute it and/or modify
  *    it under the terms of the GNU General Public License as published by
  *    the Free Software Foundation, either version 3 of the License, or
  *    (at your option) any later version.
  *
  *    Foobar is distributed in the hope that it will be useful,
  *    but WITHOUT ANY WARRANTY; without even the implied warranty of
  *    MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *    GNU General Public License for more details.
  *
  *    You should have received a copy of the GNU General Public License
  *    along with Foobar.  If not, see <http://www.gnu.org/licenses/>.
  */
 
 package br.ufrn.dimap.pubshare.download.mocks;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import br.ufrn.dimap.pubshare.domain.Article;
 import br.ufrn.dimap.pubshare.domain.ArticleDownloaded;
 import br.ufrn.dimap.pubshare.mocks.ArticleMockFactory;
 
 /**
  * 
  * @author Lucas Farias de Oliveira <i>luksrn@gmail.com</i>
  *
  */
 public class ArticlesDownloadedMockFactory {
 
 	public static List<ArticleDownloaded> makeArticleDownloadedList(){
 		
 		List<Article> articles = ArticleMockFactory.makeArticleList( ArticleMockFactory.articlesTittles.length / 2 );
 		
 		List<ArticleDownloaded> downloads = new ArrayList<ArticleDownloaded>();
 		
 		for( Article article : articles ){
 			ArticleDownloaded articleDownloaded = new ArticleDownloaded();
 			articleDownloaded.setTitle( article.getTitle() );
 			articleDownloaded.setDownloadedAt( new Date() );
 			articleDownloaded.setPathSdCard( "/sdcard/pubshare/downloads/file.pdf");
 			articleDownloaded.setDigitalLibrary("IEEE");			
 			
 			downloads.add( articleDownloaded );
 		}
 		
 		return downloads;
 	}
 }
