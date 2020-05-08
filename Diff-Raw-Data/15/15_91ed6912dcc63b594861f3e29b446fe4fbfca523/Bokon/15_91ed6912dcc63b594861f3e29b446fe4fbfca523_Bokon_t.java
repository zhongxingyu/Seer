 package com.inc.novel.site;
 
 import java.io.IOException;
 import java.sql.Timestamp;
 import java.util.List;
 
 import org.apache.log4j.Logger;
 import org.jsoup.Jsoup;
 import org.jsoup.nodes.Document;
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import com.inc.novel.dao.Book;
 import com.inc.novel.dao.BookDAO;
 import com.inc.novel.dao.Chapter;
 import com.inc.novel.dao.ChapterDAO;
 
 public class Bokon implements Runnable{
 
	private static final Logger log = Logger.getLogger(Bokon.class);
 
 	private String bookName;
 	private int num=Integer.MAX_VALUE;
 	
 	public Bokon(String bookName) {
 		this.bookName=bookName;
 	}
 
 	public Bokon(String bookName, int num) {
 		this.bookName=bookName;
 		this.num=num;
 	}
 	
 	private static void updateBook(String bookName, int num) {
 		BookDAO bookdao = new BookDAO();
 		ChapterDAO chapterdao = new ChapterDAO();
 
 		Book book = new Book();
 		book.setBookName(bookName);
 
 		List booklist = bookdao.findByExample(book);
 		if (booklist.size() == 1) {
 			book = (Book) booklist.get(0);
 		} else {
			log.error(bookName +" does not exist or duplicate!");
 			return;
 		}
 		String lastChapter = book.getLastChapter();
 		Chapter chap = new Chapter();
 
 		List<Chapter> chaps = chapterdao.findLastChapter(book.getBookId());
 		if (chaps.size() == 0) {
 			chap = new Chapter();
 		} else {
 			chap = chaps.get(0);
 		}
 
 		String base = book.getBaseurl();
 		Document doc;
 		try {
 			doc = Jsoup.connect(book.getBaselisturl()).get();
 		} catch (IOException e1) {
 			e1.printStackTrace();
			log.error(e1.getMessage()); 
 			return;
 		}
 
 		Elements st = doc.select(".SpanTitle a[href]");
 
 		int i = 0;
 
 		for (Element el : st) {
 
 			Chapter newchap = new Chapter();
 			StringBuilder stringBuilder = new StringBuilder();
 
 			String page = el.attr("href").replaceAll("\\.\\.", "");
 			if (null != lastChapter
 					&& lastChapter.compareToIgnoreCase(page) >= 0) {
 				continue;
 			}
 			String title = el.html();
 
 			try {
 
				log.info(base + page);
 				Document doc1 = Jsoup.connect(base + page).get();
 				Element nextpage = doc1.getElementById("PageSet");
 				int pagesize = nextpage.select("a").size();
 				String txt = getContext(doc1);
 
 				stringBuilder.append(txt).append("\n");
 
 				for (int j = 2; j <= pagesize; j++) {
 					String page2 = page.replace(".html", "-" + j + ".html");
 					try {
 						System.out.println(base + page2);
 						Document nextdoc = Jsoup.connect(base + page2).get();
 						String nexttxt = getContext(nextdoc);
 
 						stringBuilder.append(nexttxt).append("\n");
 					} catch (Exception e) {
						log.info(page + " -" + j + " " + e.getMessage()); 
 						return;
 					}
 				}
 
 			} catch (Exception e) {
				log.error(page + " " + e.getMessage()); 
 				return;
 			}
 
 			newchap.setBookId(book.getBookId());
 			newchap.setChapterName(title);
 			newchap.setContent(stringBuilder.toString());
 			newchap.setPrevious(chap.getChapterId());
 			newchap.setCTime(new Timestamp(System.currentTimeMillis()));
 			newchap.setUTime(new Timestamp(System.currentTimeMillis()));
 			newchap.setSrcPage(page);
 			chapterdao.save(newchap);
 
 			if (null != chap.getChapterId()) {
 				chap.setNext(newchap.getChapterId());
 				chap.setUTime(new Timestamp(System.currentTimeMillis()));
 				chapterdao.update(chap);
 			}
 			chap = newchap;
 
 			book.setLastChapter(page);
 			book.setUTime(new Timestamp(System.currentTimeMillis()));
 			bookdao.update(book);
 
 			i++;
 			if (i > num) {
 				break;
 			}
 		}
 	}
 
 	private static String getContext(Document doc) {
 		String cont = "";
 		Elements els = doc.select("#DivContent");
 		Element element = els.get(1);
 		element.select("a").remove();
 		cont = element.html().trim();
 		cont = cont.replaceAll("\\[\\s*\\]", "");
 		cont += cont + "\n";
 		return cont;
 	}
 
 	@Override
 	public void run() {
 		updateBook(this.bookName,this.num);
 	}
 }
