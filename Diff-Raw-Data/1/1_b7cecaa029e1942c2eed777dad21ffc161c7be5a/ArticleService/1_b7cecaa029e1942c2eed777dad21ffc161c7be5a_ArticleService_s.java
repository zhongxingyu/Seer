 package cn.seu.cose.service;
 
 import java.util.Date;
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.cache.annotation.CacheEvict;
 import org.springframework.cache.annotation.Cacheable;
 import org.springframework.stereotype.Service;
 
 import cn.seu.cose.dao.ArticleDAO;
 import cn.seu.cose.entity.ArticlePojo;
 import cn.seu.cose.util.LinkTool;
 
 @Service
 public class ArticleService {
 	@Autowired
 	ArticleDAO articleDAOImpl;
 
 	@Cacheable(value = "articleCache", key = "'newCenterInIndex'")
 	public List<ArticlePojo> newCenterInIndex() {
 		List<ArticlePojo> news = articleDAOImpl.getArticlesByCatAndRangeBrief(
 				2, 0, 15);
 		for (ArticlePojo articlePojo : news) {
 			articlePojo.setUri(LinkTool.article(articlePojo));
 		}
 		return news;
 	}
 
 	@Cacheable(value = "articleCache", key = "'getArticleByCatIdAndPnAndPsBrief:'+ #catId +','+ #pn +','+ #pageSize ")
 	public List<ArticlePojo> getArticleByCatIdAndPnAndPsBrief(int catId,
 			int pn, int pageSize) {
 		List<ArticlePojo> news = articleDAOImpl.getArticlesByCatAndRangeBrief(
 				catId, pageSize * (pn - 1), pageSize);
 		for (ArticlePojo articlePojo : news) {
 			articlePojo.setUri(LinkTool.article(articlePojo));
 		}
 		return news;
 	}
 
 	@Cacheable(value = "articleCache", key = "'getArticleByCatIdAndPageIndex:'+ #catId +','+ #index ")
 	public List<ArticlePojo> getArticleByCatIdAndPageIndex(int catId, int index) {
 		List<ArticlePojo> articles = articleDAOImpl.getArticlesByCatAndRange(
 				catId, 10 * (index - 1), 10);
 		for (ArticlePojo articlePojo : articles) {
 			articlePojo.setUri(LinkTool.article(articlePojo));
 		}
 		return articles;
 	}
 
 	@Cacheable(value = "articleCache", key = "'getArticleByCatIdAndPageIndexAndPageSize:'+ #catId +','+#index+','+#pageSize")
 	public List<ArticlePojo> getArticleByCatIdAndPageIndexAndPageSize(
 			int catId, int index, int pageSize) {
 		List<ArticlePojo> articles = articleDAOImpl.getArticlesByCatAndRange(
 				catId, pageSize * (index - 1), pageSize);
 		for (ArticlePojo articlePojo : articles) {
 			articlePojo.setUri(LinkTool.article(articlePojo));
 		}
 		return articles;
 	}
 
 	public List<ArticlePojo> getAdminArticleByCatIdAndPageIndexAndPageSize(
 			int catId, int index, int pageSize) {
 		List<ArticlePojo> articles = articleDAOImpl.getArticlesByCatAndRange(
 				catId, pageSize * (index - 1), pageSize);
 		for (ArticlePojo articlePojo : articles) {
 			articlePojo.setUri(LinkTool.article(articlePojo));
 		}
 		return articles;
 	}
 
 	public List<ArticlePojo> searchArticle(String searchInput) {
 		List<ArticlePojo> articles = articleDAOImpl.searchArticle(searchInput);
 		for (ArticlePojo articlePojo : articles) {
 			articlePojo.setUri(LinkTool.article(articlePojo));
 		}
 		return articles;
 	}
 
 	public List<ArticlePojo> getArticlesByCatId(int subCatId) {
 		return articleDAOImpl.getArticlesBySubCatId(subCatId);
 	}
 
 	public ArticlePojo getArticleByIdBrief(int id) {
 		return articleDAOImpl.getArticleByIdBrief(id);
 	}
 
 	public ArticlePojo getArticleById(int id) {
 		ArticlePojo article = articleDAOImpl.getArticleById(id);
 		ArticlePojo previous = articleDAOImpl.getPreviousArticle(article);
 		ArticlePojo next = articleDAOImpl.getNextArticle(article);
 
 		article.setUri(LinkTool.article(article));
 		if (previous != null) {
 			previous.setUri(LinkTool.article(previous));
 		}
 		if (next != null) {
 			next.setUri(LinkTool.article(next));
 		}
 
 		article.setPrevious(previous);
 		article.setNext(next);
 		return article;
 	}
 
 	@Cacheable(value = "articleCache", key = "'getArticleCountByCatId:' + #rootCatId + ',' + #catId")
 	public int getArticleCountByCatId(int rootCatId, int catId) {
 		if (catId <= 8) {
 			return articleDAOImpl.getArticleCountByRootCatId(rootCatId);
 		}
 		return articleDAOImpl.getArticleCountByCatId(catId);
 	}
 
 	@CacheEvict(value = "articleCache", allEntries = true)
 	public void addArticle(ArticlePojo article) {
 		articleDAOImpl.insertArticle(article);
 	}
 
 	@CacheEvict(value = "articleCache", allEntries = true)
 	public void updateArticle(ArticlePojo article) {
 		articleDAOImpl.updateArticle(article);
 	}
 
 	@CacheEvict(value = "articleCache", allEntries = true)
 	public void deleteArticle(int id) {
 		articleDAOImpl.deleteArticle(id);
 	}
 
 	public ArticlePojo getExclusiveArticleByCatId(int catId) {
 		return articleDAOImpl.getExclusiveArticleByCatId(catId);
 	}
 
 	@Cacheable(value = "articleCache", key = "'getConcerns'")
 	public List<ArticlePojo> getConcerns() {
 		List<ArticlePojo> articles = articleDAOImpl
 				.getArticlesByCatAndRangeBrief(15, 0, 5);
 		for (ArticlePojo articlePojo : articles) {
 			articlePojo.setUri(LinkTool.article(articlePojo));
 		}
 		return articles;
 	}
 
 	@Cacheable(value = "articleCache", key = "'getEvents'")
 	public List<ArticlePojo> getEvents() {
 		List<ArticlePojo> articles = articleDAOImpl.getArticlesByCatAndRange(5,
 				0, 5);
 		for (ArticlePojo articlePojo : articles) {
 			articlePojo.setUri(LinkTool.article(articlePojo));
 		}
 		return articles;
 	}
 
 	@Cacheable(value = "articleCache", key = "'getTrains'")
 	public List<ArticlePojo> getTrains() {
 		List<ArticlePojo> articles = articleDAOImpl.getArticlesByCatAndRange(
 				61, 0, 5);
 		for (ArticlePojo articlePojo : articles) {
 			articlePojo.setUri(LinkTool.article(articlePojo));
 		}
 		return articles;
 	}
 
 	@Cacheable(value = "articleCache", key = "'getRelates:'+#catId")
 	public List<ArticlePojo> getRelates(int catId) {
 		List<ArticlePojo> articles = articleDAOImpl.getArticlesByCatAndRange(
 				catId, 0, 10);
 		for (ArticlePojo articlePojo : articles) {
 			articlePojo.setUri(LinkTool.article(articlePojo));
 		}
 		return articles;
 	}
 
 	public List<ArticlePojo> highlightKeyword(List<ArticlePojo> articles,
 			String keyword) {
 		for (ArticlePojo article : articles) {
 			article.setTitle(article.getTitle().replaceAll(
 					keyword,
 					"<span style='background-color:#5ca20d'>" + keyword
 							+ "</span>"));
 			article.setSubhead(article.getSubhead().replaceAll(
 					keyword,
 					"<span style='background-color:#5ca20d'>" + keyword
 							+ "</span>"));
 		}
 		return articles;
 	}
 
 	// **************************通讯员相关文章操作**************************//
 	public List<ArticlePojo> getContributedArticlesOfReporter(int reporterId) {
 		return articleDAOImpl.getContributeArticlesList(reporterId);
 	}
 
 	public List<ArticlePojo> getWaitArticlesOfReporter(int reporterId) {
 		return articleDAOImpl.getWaitArticlesOfReporter(reporterId);
 	}
 
 	public List<ArticlePojo> getAcceptArticlesOfReporter(int reporterId) {
 		return articleDAOImpl.getAcceptArticlesList(reporterId);
 	}
 
 	public List<ArticlePojo> getRejectArticlesOfReporter(int reporterId) {
 		return articleDAOImpl.getRejectArticlesList(reporterId);
 	}
 
 	public void contributeArticle(ArticlePojo article) {
 		articleDAOImpl.contributeArticle(article);
 	}
 
 	public void updateContributedArticle(ArticlePojo article) {
 		articleDAOImpl.updateContributeArticle(article.getContributedFrom(),
 				article);
 	}
 
 	public List<ArticlePojo> searchArticleOfReporter(int reporterId,
 			String searchInput) {
 		return articleDAOImpl.searchArticleOfReporter(reporterId, searchInput);
 	}
 
 	// ************************管理员对提交的文章的相应操作********************//
 	public List<ArticlePojo> getContributedArticles(Date s, Date e) {
 		return articleDAOImpl.getContributedArticlesList(s, e);
 	}
 
 	public List<ArticlePojo> getWaitingArticles(Date s, Date e) {
 		return articleDAOImpl.getWaitingArticlesList(s, e);
 	}
 
 	public List<ArticlePojo> getAcceptArticles(Date s, Date e) {
 		return articleDAOImpl.getAcceptArticlesList(s, e);
 	}
 
 	public List<ArticlePojo> getRejectArticles(Date s, Date e) {
 		return articleDAOImpl.getRejectArticlesList(s, e);
 	}
 
 	public void acceptArticle(ArticlePojo article) {
 		articleDAOImpl.acceptArticle(article);
 	}
 
 	public void rejectArticle(int id) {
 		articleDAOImpl.rejectArticle(id);
 	}
 
 	public List<ArticlePojo> searchContribute(int type, String searchInput) {
 		return articleDAOImpl.searchContribute(type, searchInput);
 	}
 }
