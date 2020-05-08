 package com.base.word;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.SQLException;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import com.base.word.bean.JPWord;
 import com.base.word.dao.JPWordDao;
 import com.fasterxml.jackson.core.JsonGenerationException;
 import com.fasterxml.jackson.databind.JsonMappingException;
 import com.fasterxml.jackson.databind.ObjectMapper;
 
 public class WordsExport {
 	public static void main(String[] args) throws SQLException, JsonGenerationException, JsonMappingException, IOException {
 		JPWordDao dao = new JPWordDao();
 		JPWord[] words = dao.getWordList(null, null);
 		List<Map<String, Object>> exportList = new LinkedList<Map<String, Object>>();
 		for (JPWord word: words) {
 			Map<String, Object> exportData = new HashMap<String, Object>();
 			exportData.put("hiragana", word.getJpWord());
 			exportData.put("kanji", word.getHanzi());
 			exportData.put("chinese", word.getCnWord());
			exportData.put("lessonNo", word.getUnit());
 			exportData.put("type", word.getType());
 			exportList.add(exportData);
 		}
 		ObjectMapper mapper = new ObjectMapper();
 		mapper.writeValue(new File("words.json"), exportList);
 	}
 }
