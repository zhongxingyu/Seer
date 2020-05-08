 package com.hoyoul.wordroid.dao;
 
 import java.util.List;
 
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 
 import com.hoyoul.wordroid.dto.Word;
 import com.hoyoul.wordroid.dto.Wordset;
 
 public class WordsetDAOImpl implements WordsetDAO {
 
 	@Autowired
 	private SessionFactory sessionFactory;
 
 	@SuppressWarnings("unchecked")
 	@Override
 	public List<Wordset> listWordset() {
 		return sessionFactory.getCurrentSession().createQuery("from Wordset").list();
 	}	
 	
 	@Override
 	public int addWordset(Wordset wordset) {
 		return (Integer) sessionFactory.getCurrentSession().save(wordset);
 	}
 
 	@Override
 	public Wordset getWordset(Integer id) {
 		return (Wordset) sessionFactory.getCurrentSession().get(Wordset.class, id);
 	}
 
 	@Override
 	public void updateWordset(Wordset wordset) {
 		sessionFactory.getCurrentSession().update(wordset);
 	}
 
 	@Override
 	public void deleteWordset(Integer id) {
 		Wordset wordset = getWordset(id);
 		if(null != wordset){
 			sessionFactory.getCurrentSession().delete(wordset);
 		}
 	}
 
 }
