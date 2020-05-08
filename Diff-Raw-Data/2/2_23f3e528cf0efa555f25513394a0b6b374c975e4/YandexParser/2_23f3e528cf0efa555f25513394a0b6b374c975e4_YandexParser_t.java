 package com.github.geakstr.parser.yandex;
 
 import java.io.IOException;
 import java.util.ArrayList;
 
 import org.jsoup.nodes.Element;
 import org.jsoup.select.Elements;
 
 import com.github.geakstr.parser.Parser;
 
 public class YandexParser extends Parser<YandexDoc> {
 
 	public YandexParser(String link) throws IOException {
 		super("http://yandex.ru/yandsearch?text=" + link);
 	}
 
 	public void addAllResultsToList() {
 		Elements elements = getDoc().select(".b-serp-item");
 
 		results = new ArrayList<YandexDoc>();
 
 		for (Element el : elements) {
 			Element linkEl = el.getElementsByClass("b-serp-item__title-link")
 					.get(0);
 
 			String link = linkEl.attr("href");
 			String title = linkEl.getElementsByTag("span").text();
 
 			String text = el.getElementsByClass("b-serp-item__text").text();
 
 			String greenLine = el.getElementsByClass("b-serp-url_inline_yes")
 					.text();
			greenLine = greenLine.replaceAll("\\", "  ");
 
 			YandexDoc yandexDoc = new YandexDoc(title, link, text, greenLine);
 			results.add(yandexDoc);
 		}
 	}
 }
