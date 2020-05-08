 package org.yarquen.web.enricher;
 
 import java.beans.PropertyEditorSupport;
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.net.URLDecoder;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 import java.util.Map.Entry;
 import java.util.StringTokenizer;
 
 import javax.annotation.Resource;
 import javax.servlet.http.HttpServletRequest;
 import javax.validation.Valid;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.stereotype.Controller;
 import org.springframework.ui.Model;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.WebDataBinder;
 import org.springframework.web.bind.annotation.InitBinder;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.PathVariable;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 import org.springframework.web.bind.annotation.SessionAttributes;
 import org.springframework.web.servlet.mvc.support.RedirectAttributes;
 import org.yarquen.article.Article;
 import org.yarquen.article.ArticleRepository;
 import org.yarquen.author.Author;
 import org.yarquen.author.AuthorRepository;
 import org.yarquen.category.CategoryBranch;
 import org.yarquen.category.CategoryRepository;
 import org.yarquen.category.CategoryService;
 import org.yarquen.keyword.Keyword;
 import org.yarquen.keyword.KeywordRepository;
 import org.yarquen.web.lucene.ArticleSearcher;
 
 /**
  * Search form
  * 
  * @author Jorge Riquelme Santana
  * @date 23/11/2012
  * @version $Id$
  * 
  */
 @Controller
 @SessionAttributes({ EnricherController.REFERER })
 @RequestMapping(value = "/articles/enricher/{id}")
 public class EnricherController {
 	public static final String REFERER = "referer";
 	private static final String ARTICLE = "article";
 	private static final String AUTHORS = "authors";
 	private static final String CATEGORIES = "categories";
 	private static final String KEYWORDS = "keywords";
 	private static final Logger LOGGER = LoggerFactory
 			.getLogger(EnricherController.class);
 
 	@Resource
 	private ArticleRepository articleRepository;
 	@Resource
 	private ArticleSearcher articleSearcher;
 	@Resource
 	private AuthorRepository authorRepository;
 	@Resource
 	private CategoryRepository categoryRepository;
 	@Resource
 	private CategoryService categoryService;
 	@Resource
 	private CategoryTreeBuilder categoryTreeBuilder;
 	@Resource
 	private KeywordRepository keywordRepository;
 
 	@InitBinder
 	public void initBinder(WebDataBinder binder) {
 		binder.registerCustomEditor(CategoryBranch.class,
 				new PropertyEditorSupport() {
 					@Override
 					public void setAsText(String branch) {
 						try {
 							LOGGER.trace(
 									"converting {} to a CategoryBranch object",
 									branch);
 							final CategoryBranch categoryBranch = new CategoryBranch();
 							final StringTokenizer tokenizer = new StringTokenizer(
 									branch, CategoryBranch.CODE_SEPARATOR);
 							while (tokenizer.hasMoreTokens()) {
 								final String code = tokenizer.nextToken();
 								categoryBranch.addSubCategory(code, null);
 							}
 							// fill names
 							categoryService
 									.completeCategoryBranchNodeNames(categoryBranch);
 							setValue(categoryBranch);
 						} catch (RuntimeException e) {
 							LOGGER.error(":(", e);
 							throw e;
 						}
 					}
 				});
 	}
 
 	@RequestMapping(method = RequestMethod.POST, params = "cancel")
 	public String returnToSearch(@ModelAttribute(REFERER) String referer,
 			RedirectAttributes redirAtts) {
 		if (referer != null) {
 			LOGGER.trace("cancel => referer: '{}'", referer.toString());
 			return "redirect:" + referer;
 		} else {
 			LOGGER.trace("cancel => no referer, returning to search");
 			return "redirect:/articles";
 		}
 	}
 
 	@RequestMapping(method = RequestMethod.GET)
 	public String setupForm(@PathVariable String id, Model model,
 			HttpServletRequest request) {
 		LOGGER.trace("setup enrichment form for article id={}", id);
 		final Article article = articleRepository.findOne(id);
 		if (article == null) {
 			throw new RuntimeException("Article " + id + " not found");
 		} else {
 			LOGGER.debug("enriching article id={} title={}", article.getId(),
 					article.getTitle());
 
 			// FIXME: find a better way to achieve this, this mechanism may fail
 			// in some browsers
 			// save referer
 			final String referer = request.getHeader("Referer");
 			LOGGER.trace("referer: {}", referer);
 			// model.addAttribute(REFERER, referer);
 			model.addAttribute(REFERER, referer);
 
 			// article to enrich
 			LOGGER.trace("articles: {}", article);
 			model.addAttribute(ARTICLE, article);
 
 			// authors
 			List<String> authorsName = getAuthors();
 			model.addAttribute(AUTHORS, authorsName);
 
 			// keywords
 			final List<String> keywordsName = getKeywords();
 			model.addAttribute(KEYWORDS, keywordsName);
 
 			// categories
 			final List<Map<String, Object>> categoryTree = categoryTreeBuilder
 					.buildTree();
 			model.addAttribute(CATEGORIES, categoryTree);
 		}
 		return "articles/enricher";
 	}
 
 	@RequestMapping(method = RequestMethod.POST, params = "submit")
 	public String update(@ModelAttribute(REFERER) String referer,
 			@Valid @ModelAttribute(ARTICLE) Article article,
 			BindingResult result, Model model, RedirectAttributes redirAtts) {
 
 		if (result.hasErrors()) {
 			LOGGER.trace("errors!: {}", result.getAllErrors());
 
 			// authors
 			List<String> authorsName = getAuthors();
 			model.addAttribute(AUTHORS, authorsName);
 
 			// keywords
 			final List<String> keywordsName = getKeywords();
 			model.addAttribute(KEYWORDS, keywordsName);
 
 			// categories
 			final List<Map<String, Object>> categoryTree = categoryTreeBuilder
 					.buildTree();
 			model.addAttribute(CATEGORIES, categoryTree);
 
 			return "articles/enricher";
 		} else {
 			LOGGER.trace("pars: article={}", article);
 			final String id = article.getId();
 			LOGGER.trace(
 					"id:{}\n author:{}\n date:{}\n  summary:{}\n title:{}\n url:{}",
 					new Object[] { id, article.getAuthor(), article.getDate(),
 							article.getSummary(), article.getTitle(),
 							article.getUrl() });
 			if (article.getKeywords() != null) {
 				LOGGER.trace("{} keywords: {}", article.getKeywords().size(),
 						article.getKeywords());
 			} else {
 				LOGGER.trace("no keywords");
 			}
 			if (article.getCategories() != null) {
 				LOGGER.trace("{} categories: {}", article.getCategories()
 						.size(), article.getCategories());
 			} else {
 				LOGGER.trace("no categories");
 			}
 
 			// get persisted article
 			final Article persistedArticle = articleRepository.findOne(id);
 			if (persistedArticle == null) {
 				throw new RuntimeException("Article " + id + " not found");
 			} else {
 				// update
 				LOGGER.trace("updating article {}", id);
 				persistedArticle.setAuthor(article.getAuthor());
 				persistedArticle.setCategories(article.getCategories());
 				persistedArticle.setDate(article.getDate());
 				persistedArticle.setKeywords(article.getKeywords());
 				persistedArticle.setSummary(article.getSummary());
 				persistedArticle.setTitle(article.getTitle());
 				persistedArticle.setUrl(article.getUrl());
 				final Article updatedArticle = articleRepository
 						.save(persistedArticle);
 
 				// reindex
 				LOGGER.trace("reindexing article {}", id);
 				try {
 					articleSearcher.reindexArticle(updatedArticle);
 					addAuthorAndKeywords(updatedArticle);
 				} catch (IOException e) {
 					final String msg = "something wen't wrong while reindexing Article "
 							+ id + "(" + updatedArticle.getTitle() + ")";
 					LOGGER.error(msg, e);
 					throw new RuntimeException(msg, e);
 				}
 
 				final String message = "article \"" + article.getTitle()
 						+ "\" successfully enriched";
 				LOGGER.trace("adding flash paramenter: enrichmentMessage={}",
 						message);
 				redirAtts.addFlashAttribute("enrichmentMessage", message);
 				if (referer != null) {
 					LOGGER.trace("update => referer: '{}'", referer.toString());
 					final int i = referer.indexOf('?');
 					if (i != -1) {
 						referer = referer.substring(i + 1);
 						try {
 							referer = URLDecoder.decode(referer, "UTF-8");
 						} catch (UnsupportedEncodingException e) {
 							LOGGER.error("error decoding referer", e);
 						}
 						LOGGER.trace("params extracted: {}", referer);
 					}
 
					return "redirect:/articles?{referer}";
 				} else {
 					LOGGER.trace("update => no referer, returning to search");
 					return "redirect:/articles";
 				}
 			}
 		}
 	}
 
 	private void addAuthorAndKeywords(Article article) {
 		// add author if doesn't exists
 		final String authorName = article.getAuthor();
 		if (authorName != null) {
 			final Author author = authorRepository.findByName(authorName);
 			if (author == null) {
 				final Author newAuthor = new Author();
 				newAuthor.setName(authorName);
 				LOGGER.trace("adding author {}", authorName);
 				authorRepository.save(newAuthor);
 			}
 		}
 
 		// add inexistent keywords
 		final List<String> keywords = article.getKeywords();
 		for (String kw : keywords) {
 			final Keyword keywordFound = keywordRepository.findByName(kw);
 			if (keywordFound == null) {
 				final Keyword keyword = new Keyword();
 				keyword.setName(kw);
 				LOGGER.trace("adding keyword {}", kw);
 				keywordRepository.save(keyword);
 			}
 		}
 	}
 
 	private List<String> getAuthors() {
 		final List<String> authorsName = new LinkedList<String>();
 		final Iterable<Author> authors = authorRepository.findAll();
 		for (Author author : authors) {
 			authorsName.add(author.getName());
 		}
 		return authorsName;
 	}
 
 	private List<String> getKeywords() {
 		final List<String> keywordsName = new LinkedList<String>();
 		final Iterable<Keyword> keywords = keywordRepository.findAll();
 		for (Keyword keyword : keywords) {
 			keywordsName.add(keyword.getName());
 		}
 		return keywordsName;
 	}
 }
