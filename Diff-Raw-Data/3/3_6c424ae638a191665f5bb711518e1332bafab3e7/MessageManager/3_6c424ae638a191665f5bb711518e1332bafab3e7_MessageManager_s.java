 package org.huamuzhen.oa.biz;
 
 import java.sql.Timestamp;
 import java.util.List;
 
 import javax.annotation.Resource;
 
 import org.huamuzhen.oa.domain.dao.MessageDAO;
 import org.huamuzhen.oa.domain.entity.Message;
 import org.springframework.transaction.annotation.Transactional;
 
 public class MessageManager extends BaseManager<Message, String> {
 	
 	@Resource
 	MessageDAO messageDAO;
 	
 	@Resource
 	public void setDao(MessageDAO dao) {
 		super.setDao(dao);
 	}
 	
 	@Transactional
 	public void sendMsg(String receiverId, String msg){
 		Message message = new Message();
 		message.setMessage(msg);
 		message.setReceiverId(receiverId);
 		message.setCreatedAt(new Timestamp(System.currentTimeMillis()));
 		message.setModifiedAt(new Timestamp(System.currentTimeMillis()));
 		this.save(message);
 	}
 	
 	@Transactional
 	public List<Message> findMessageByReceiverIdOrderByCreatedAtDesc(String receiverId){
 		return messageDAO.findMessageByReceiverIdOrderByCreatedAtDesc(receiverId);
 	}
 
 }
