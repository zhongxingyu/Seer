 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package no.hials.muldvarpweb.service;
 
 import java.util.ArrayList;
 import java.util.List;
 import javax.ejb.Stateless;
 import javax.persistence.EntityManager;
 import javax.persistence.PersistenceContext;
 import javax.persistence.TypedQuery;
 import javax.ws.rs.GET;
 import javax.ws.rs.Path;
 import javax.ws.rs.PathParam;
 import javax.ws.rs.Produces;
 import javax.ws.rs.core.MediaType;
 import no.hials.muldvarpweb.domain.Video;
 
 /**
  * Video service class providing EM functionality.
  * 
  * @author johan
  */
 @Stateless
 @Path("video")
 public class VideoService {
     
     @PersistenceContext
     EntityManager entityManager;
     
     
     /**
      * This function merges and persists a Video item .
      * 
      * @param newVideo The video to be added.
      */
     public void addVideo(Video newVideo){
         
         newVideo = entityManager.merge(newVideo);
         entityManager.persist(newVideo);
     }
     
     /**
      * This function merges and removes a Video item.
      * 
      * @param video video The Video to be removed.
      */
     public void removeVideo(Video video){
         
         video = entityManager.merge(video);
         entityManager.remove(video);
     }
     
     @GET
     @Produces({MediaType.APPLICATION_JSON})
     public List<Video> findVideos() {
         
         return entityManager.createQuery("SELECT v from Video v", Video.class).getResultList();        
     }    
         
     @GET
     @Path("{id}")
     @Produces({MediaType.APPLICATION_JSON})
     public Video getVideos(@PathParam("id") Integer id) {
         TypedQuery<Video> q = entityManager.createQuery("Select v from Video v where v.id = :id", Video.class);
         q.setParameter("id", id);        
         
         return q.getSingleResult();
     }
     
     /**
      * TempTempTemp
      * 
      * @return List of Videos, produces JAYSON
      */
     @GET
    @Path("stud")
     @Produces({MediaType.APPLICATION_JSON})
     public List<Video> findStudentVideos() {
         
         List<Video> videoList = new ArrayList<Video>();
         
         videoList.add(new Video("Et lite stykke Høgskolen i Ålesund",
                                 "YouTube/ID",
                                 "wjoKkILj9PA",
                                 "HiALS Studier",
                                 "Presentasjon av studietilbudene ved Høgskolen i Ålesund.",
                                 "lol no",
                                 "nooope"));
         
         
         return videoList;        
     }    
        
        
     /**
      * This function creates test data.
      * 
      * @return test data
      */
     public void makeVideoTestData() {
         
         String testURL[] = {"ygI-2F8ApUM", "Vxi7JRJrod4"};
         
         
         for (int i = 0; i < 15; i++) {
             
             //annenhver
             int n = 1;
             if(i % 2 == 0){
                 n = 0;
             }
             
             Video newVideo = new Video("Video " + i,
                     "Youtube/ID" ,
                     testURL[n],
                     "Video INC",
                     "This video is not an actual video.",
                     "www.db.no",
                     "www.sWmp.no");
             
             newVideo = entityManager.merge(newVideo);
             entityManager.persist(newVideo);
             
         }
         
                 
     }
     
     /**
      * This function creates test data.
      * 
      * @return test data
      */
     public List<Video> getVideoTestData() {
         
         String testURL[] = {"ygI-2F8ApUM", "Vxi7JRJrod4"};
         
         List<Video> videoList = new ArrayList<Video>();
         
         for (int i = 0; i < 15; i++) {
             
             //annenhver
             int n = 1;
             if(i % 2 == 0){
                 n = 0;
             }
             
             videoList.add(new Video("Video " + i,"Youtube/ID" , testURL[n],"Video INC", "This video is not an actual video.", "www.db.no", "www.smp.no"));
             
         }
         
         return videoList;
         
     }
     
     
     
     
     
     
     
     
     
     
     
     
     
     
     
 }
