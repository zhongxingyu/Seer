 package com.ramselabs.education.dao.impl;
 
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import javax.inject.Inject;
 import javax.inject.Named;
 
 import org.hibernate.Query;
 import org.hibernate.Session;
 import org.hibernate.SessionFactory;
 import org.springframework.context.annotation.Scope;
 
 import com.ramselabs.education.dao.service.AlertDAO;
 import com.ramselabs.education.entity.Post;
 import com.ramselabs.education.entity.PostShare;
 import com.ramselabs.education.entity.UserProfile;
 import com.ramselabs.education.model.PostDescriptionModel;
 @Named
 @Scope("session")
 public class AlertDAOImpl implements AlertDAO{
 	
 	@Inject
 	SessionFactory sessionFactory;
 	
 	public void setSessionFactory(SessionFactory sessionFactory) {
 		this.sessionFactory = sessionFactory;
 	}
 	
 	@Override
 	public List<PostDescriptionModel> getAllAlert(UserProfile user) {
 		List<PostDescriptionModel> list1=getAlertOtherPersons(user);
 		List<PostDescriptionModel> list2=getAlertsFromSamePerson(user);
 		if(list1==null & list2==null)
 			return null;
 		if(list1==null){
 		Collections.sort(list2, new PostDescriptionModel());
 		return list2;
 		}
 		else if(list2==null){
 			Collections.sort(list1, new PostDescriptionModel());
 			return list1;
 			}
 		
 		list1.addAll(list2);
 		Collections.sort(list1, new PostDescriptionModel());
 		return list1;
 		
 	}
 
 	@Override
 	public int insertAlerts(Post post, PostShare pShare) {
 		Session session=sessionFactory.openSession();
 		session.beginTransaction();
 		session.save(pShare);
 		session.save(post);
 		session.getTransaction().commit();
 		session.flush();
 		session.close();
 		return 1;
 	}
 
 	@SuppressWarnings("unchecked")
 	private List<PostDescriptionModel> getAlertsFromSamePerson(UserProfile user) {
 		List<PostDescriptionModel> list=new ArrayList<PostDescriptionModel>();
 		int posterId=getUserId(user);
 		Session session=sessionFactory.openSession();
 		Query query=session.createQuery("from Post where messageType =:messageType and posterId =:posterId");
 		query.setString("messageType","alert");
 		query.setInteger("posterId", posterId);
 		List<Post> posts=(List<Post>)query.list();
 		if(posts.isEmpty())
 			return null;
 		for(Post post:posts){
 		List<PostShare> postShares=(List<PostShare>)post.getPostShare();
 		
 			for(PostShare pShare:postShares){
 			PostDescriptionModel postDescModel=new PostDescriptionModel();
 			postDescModel.setPersonName(getPoster(posterId).getDisplayName());
 			postDescModel.setUserImage(getPoster(posterId).getImagePath());
 			postDescModel.setPostDescription(pShare.getPost().getDescription());
 			postDescModel.setUserType(pShare.getUserType());
 			postDescModel.setDateOfPosting(pShare.getPostDate());
			postDescModel.setMessageType(pShare.getPost().getMessageType());
 			list.add(postDescModel);
 		    }
 		}
 		return list;
 	}
 	
 	private List<PostDescriptionModel> getAlertOtherPersons(UserProfile user) {
 		int userId=getUserId(user);
 		if(userId==0)
 			return null;
 		List<PostDescriptionModel> listPerson=new ArrayList<PostDescriptionModel>();
 		Session session=sessionFactory.openSession();
 		UserProfile userProfile=(UserProfile)session.get(UserProfile.class,userId);
 		List<PostShare> posts=(List<PostShare>)userProfile.getUserPostShare();
 //		System.out.println(posts);
 		if(posts.isEmpty())
 			return null;
 		for(PostShare postShare:posts){
 			if(postShare.getPost().getMessageType().equals("alert")){
 			PostDescriptionModel postDescription=new PostDescriptionModel();
 			postDescription.setPersonName(getPoster(postShare.getPost().getPosterId()).getDisplayName());
 			postDescription.setPostDescription(postShare.getPost().getDescription());
 			postDescription.setUserType(postShare.getUserType());
			postDescription.setMessageType(postShare.getPost().getMessageType());
 			String image=getPoster(postShare.getPost().getPosterId()).getImagePath();
 			System.out.println(image);
 			if(image==null)
 				image="/resources/img/profile-photo/default-profile.jpg";
 			postDescription.setUserImage(image);
 			System.out.println("Date of posting"+postShare.getPostDate());
 			postDescription.setDateOfPosting(postShare.getPostDate());
 			listPerson.add(postDescription);
 		   }
 		}		
 		   
 		return listPerson;
 	}
 	
 	public UserProfile getPoster(int userId) {
 		Session session=sessionFactory.openSession();
     	UserProfile user=(UserProfile)session.get(UserProfile.class, userId);
     	return user;
 	}
 	
 	@SuppressWarnings("unchecked")
 	public int getUserId(UserProfile user) {
 		Session session=sessionFactory.openSession();
 		Query query=session.createQuery("select userId from UserProfile where username = :username and password = :password");
 		query.setString("username",user.getUsername());
 		query.setString("password", user.getPassword());
 		List<Integer> list=(List<Integer>)query.list();
 		if(list.isEmpty())
 			System.out.println("List is empty for userId");
 		int userId=0;
 		  for(int j:list){
 			  System.out.println("List is not empty");
 			  j=list.get(0);
 			  userId=j;
 		  }
 		  return userId;
 	}
 }
