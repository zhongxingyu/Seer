 /*
  User: erez
  Date: 2/21/13
  Time: 7:20 PM
  */
 package com.moshavit.persistence;
 
 import com.moshavit.model.BoardMessage;
 import org.springframework.stereotype.Repository;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.Collection;
 
 import static com.google.common.base.Preconditions.checkNotNull;
 
 @Repository
 @Transactional
 public class BoardRepository extends BaseRepository {
 
 	public Collection<BoardMessage> getBoardMessages() {
		return getSession().createQuery("from com.moshavit.model.BoardMessage").list();
 	}
 
 	public BoardMessage getBoardMessage(Long id) {
		checkNotNull(id, "Cannot return  a Message with null ID.");
 		return (BoardMessage) getSession().get(BoardMessage.class, id);
 	}
 
	public void addBoardMessage(BoardMessage boardMessage) {
 		checkNotNull(boardMessage.getText().isEmpty() ? null : boardMessage, "Cannot add a null Message.");
 		getSession().saveOrUpdate(boardMessage);
 	}
 
	public void updateBoardMessage(BoardMessage boardMessage) {
 		checkNotNull(boardMessage.getText().isEmpty() ? null : boardMessage, "Cannot save Message with no Message ID.");
 		getSession().merge(boardMessage);
 	}
 }
