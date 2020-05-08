 package com.hoyoul.wordroid.service;
 
 import java.util.List;
 
 import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
 
 import com.hoyoul.wordroid.dao.WordsetDAO;
 import com.hoyoul.wordroid.dto.Wordset;
 
@Service
 public class WordsetServiceImpl implements WordsetService {
 
 	@Autowired
 	private WordsetDAO wordsetDAO;
 
 	@Override
 	public List<Wordset> listWordset() {
 		return wordsetDAO.listWordset();
 	}
 	
 	@Override
 	public int addWordset(Wordset wordset) {
 		return wordsetDAO.addWordset(wordset);
 	}
 
 	@Override
 	public Wordset getWordset(Integer id) {
 		return wordsetDAO.getWordset(id);
 	}
 
 	@Override
 	public void updateWordset(Wordset wordset) {
 		wordsetDAO.updateWordset(wordset);
 	}
 
 	@Override
 	public void deleteWordset(Integer id) {
 		wordsetDAO.deleteWordset(id);
 	}
 
 }
