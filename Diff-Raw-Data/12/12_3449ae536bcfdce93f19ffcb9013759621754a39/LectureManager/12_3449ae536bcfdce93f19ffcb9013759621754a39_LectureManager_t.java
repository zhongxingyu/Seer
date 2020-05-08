 package service;
 
 import java.util.Date;
 import java.util.List;
 
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 
 import domain.Attachment;
 import domain.FileType;
 import domain.Lecture;
 import domain.LectureDetail;
 
 @Stateless
 public class LectureManager implements LectureInterface {
 	
 	@PersistenceContext
 	EntityManager em;
 
 	/* Interface methods */	
 	@Override
 	public boolean createLecture(String name, String description, Date date,
 			long teacherId) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	public Lecture getLecture(long id) {
 		// TODO Auto-generated method stub
 		return null;
 	}
 
 	@Override
 	public boolean updateLecture(String name, String description, Date date,
 			long teacherId) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
 	@SuppressWarnings("unchecked")
 	public List<Lecture> getAllLectures() {
 		//LectureDetails will change, but Lectures wont,
 		//so we get Lectures instead of LectureDetails		
 		return em.createNamedQuery("lecture.all").getResultList();
 	}
 
 	@Override
 	public boolean deleteLecture(long id) {
 		// TODO Auto-generated method stub
 		return false;
 	}
 
 	@Override
	public boolean deleteAttachment(long lectureId, long id) {		
		Attachment attachment = em.find(Attachment.class, id);		
		em.remove(attachment);					
		
		return true;
 	}
 
 	@Override
 	public boolean addAttachment(long lectureId, String name, FileType type) {
 		Lecture lecture = em.find(Lecture.class, lectureId);
 		
 		//create new attachment from data
 		Attachment attachment = new Attachment();
 		//TODO: will attachment id be set automatically? 
 		attachment.setName(name);
 		attachment.setType(type);
 		
 		//get actual attachment list and add new attachment
 		List<Attachment> attachmentList = lecture.getAttachment();
 		attachmentList.add(attachment);
 		
 		lecture.setAttachment(attachmentList);
		
		//add attachments to lecture
		em.merge(lecture);
 				
 		return true;
 	}
 		
 	/* Additional methods */	
 	public int getLectureDetailRank(long detailId){
 		LectureDetail lectureDetail = em.find(LectureDetail.class, detailId);		
 		return lectureDetail.getRate();
 	}	
 	
 	public void setLectureDetailRank(long detailId, int rate){
 		LectureDetail lectureDetail = em.find(LectureDetail.class, detailId);
 		
 		lectureDetail.setRate(rate);		
 		em.merge(lectureDetail);			
 	}
 		
 
 }
