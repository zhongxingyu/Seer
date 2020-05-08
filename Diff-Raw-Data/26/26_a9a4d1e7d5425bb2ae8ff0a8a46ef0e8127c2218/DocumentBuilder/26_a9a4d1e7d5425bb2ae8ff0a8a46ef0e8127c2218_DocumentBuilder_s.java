 package com.weieditor.mds;
 
import java.io.StringReader;
import java.util.List;
 import java.util.Scanner;
 
import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang3.StringUtils;
 
 import com.weieditor.mds.model.Article;
 import com.weieditor.mds.model.Document;
 import com.weieditor.mds.model.Paragraph;
 import com.weieditor.mds.model.Sentence;
 
 public class DocumentBuilder {
 
 	public Document parse(Article article) {
 		Document doc = new Document();
 
 		String content = article.getContent();
 		Scanner scanner = new Scanner(content);
 		while (scanner.hasNextLine()) {
 			String pContent = scanner.nextLine();
 			if (StringUtils.isNotBlank(pContent)) {
 				Paragraph p = new Paragraph();
 				p.setContent(pContent.trim());
				String[] sentenceContents = pContent.split("[\\.]+");
 				for (String sentenceContent : sentenceContents) {
					sentenceContent += "";
 					Sentence sentence = new Sentence();
 					sentence.setContent(sentenceContent.trim());
 
 					p.add(sentence);
 				}
 				doc.add(p);
 			}
 		}
 		return doc;
 	}
 
 }
