 package edu.cmu.cs15437.clubwebsite.model;
 
 import edu.cmu.cs15437.clubwebsite.databeans.VideoBean;
 import edu.cmu.cs15437.clubwebsite.databeans.VideoCategoryBean;
 
 import org.mybeans.dao.DAOException;
 import org.mybeans.factory.BeanTable;
 import org.mybeans.factory.BeanFactory;
 import org.mybeans.factory.BeanFactoryException;
 import org.mybeans.factory.Transaction;
 import org.mybeans.factory.RollbackException;
 import org.mybeans.factory.MatchArg;
 
 import java.util.*;
 
 public class VideoDAO {
 	private BeanFactory< VideoBean > videoFactory;
 	private VideoTagDAO videoTagDAO;
 	
 	public VideoDAO(VideoTagDAO d) throws DAOException {
 		try {
 			BeanTable< VideoBean > videoTable = BeanTable.getInstance(VideoBean.class, "clubweb_video");
 			if(! videoTable.exists()) videoTable.create("videoId");
 			videoTable.setIdleConnectionCleanup(true);
 			videoFactory = videoTable.getFactory();
 			
 			videoTagDAO = d;
 		} catch (BeanFactoryException e) {
 			throw new DAOException(e);
 		}
 	}
 	
 	protected BeanFactory< VideoBean > getFactory() {
 		return videoFactory;
 	}
 	
 	public VideoBean lookupWithVideoId(int videoId) throws DAOException {
 		try {
 			return videoFactory.lookup(videoId);
 		} catch (RollbackException e) {
 			throw new DAOException(e);
 		}
 	}
 	
 	public List< VideoBean > getUserVideos(int userId) throws DAOException {
 		try {
 			VideoBean[] videos = videoFactory.match(MatchArg.equals("ownerId", userId));
 			Arrays.sort(videos); // Sort by recency
 			return Arrays.asList(videos);
 		} catch (RollbackException e) {
 			throw new DAOException(e);
 		}
 	}
 	
 	public List< VideoBean > getVideosWithVideoIds(List< Integer > videoIds) throws DAOException {
 		List< VideoBean > videos = new ArrayList< VideoBean >(videoIds.size());
 		for (Integer videoId : videoIds) {
 			videos.add(lookupWithVideoId(videoId));
 		}
 		return videos;
 	}
 	
 	public List< VideoBean > getVideosWithVideoCategories(List< VideoCategoryBean > categories) throws DAOException {
 		if (categories.isEmpty()) {
 			return getAllVideos();
 		}
 		List< VideoBean > videos = getVideosWithVideoIds(videoTagDAO.videoIdsMatchingCategories(categories));
 		Collections.sort(videos); // Sort by recency
 		return videos;
 	}
 	
 	public List< VideoBean > getAllVideos() throws DAOException {
 		try {
 			VideoBean[] videos = videoFactory.match();
			Arrays.sort(videos); // Sort by recently
 			return Arrays.asList(videos);
 		} catch (RollbackException e) {
 			throw new DAOException(e);
 		}
 	}
 	
 	public List< VideoBean > getRecentlyAddedVideos(int videoCount, int userGroup) throws DAOException {
 		List< VideoBean > videos = getAllVideos();
 		Collections.sort(videos, new Comparator<VideoBean>() {
 
 			@Override
 			public int compare(VideoBean o1, VideoBean o2) {
 				return (int)(o2.getDateValue() - o1.getDateValue());
 			}
 			
 		});
 		
 		
		screenAccess(videos,userGroup);
 		
 		if( videos.size() > videoCount )
 			return videos.subList(0, videoCount-1);
 		else
 			return videos;
 		
 	}
 	
 	
 	public List< VideoBean > screenAccess(List< VideoBean > list, int userGroup){
 		Iterator<VideoBean> iter = list.iterator();
 		while (iter.hasNext()){
 			VideoBean video = iter.next();
			if( video.getAccessLevel() > userGroup )
				list.remove(video);
 		}
		return list;
 	}
 	
 	
 	public VideoBean create(VideoBean bean) throws DAOException {
 		try {
 			Transaction.begin();
 			VideoBean dbVideo = videoFactory.create();
 			videoFactory.copyInto(bean, dbVideo);
 			Transaction.commit();
 			return dbVideo;
 		} catch (RollbackException e) {
 			throw new DAOException(e);
 		} finally {
 			if (Transaction.isActive()) Transaction.rollback();
 		}
 	}
 	
 	public boolean destroy(int videoId, int accessLevel) throws DAOException {
 		try {
 			Transaction.begin();
 			VideoBean dbVideo = videoFactory.lookup(videoId);
 			if (dbVideo == null) {
 				throw new DAOException("VideoBean " + String.valueOf(videoId) + " does not exist");
 			}
 			videoFactory.delete(videoId);
 			Transaction.commit();
 			return true;
 		} catch (RollbackException e) {
 			throw new DAOException(e);
 		} finally {
 			if (Transaction.isActive()) Transaction.rollback();
 		}
 	}
 	
 	public boolean updateAccessLevel(int videoId, int accessLevel) throws DAOException {
 		try {
 			Transaction.begin();
 			VideoBean dbVideo = videoFactory.lookup(videoId);
 			if (dbVideo == null) {
 				throw new DAOException("VideoBean " + String.valueOf(videoId) + " does not exist");
 			}
 			dbVideo.setAccessLevel(accessLevel);
 			Transaction.commit();
 			return true;
 		} catch (RollbackException e) {
 			throw new DAOException(e);
 		} finally {
 			if (Transaction.isActive()) Transaction.rollback();
 		}
 	}
 	
 	public boolean updateLink(int videoId, String link) throws DAOException {
 		try {
 			Transaction.begin();
 			VideoBean dbVideo = videoFactory.lookup(videoId);
 			if (dbVideo == null) {
 				throw new DAOException("VideoBean " + String.valueOf(videoId) + " does not exist");
 			}
 			dbVideo.setLink(link);
 			Transaction.commit();
 			return true;
 		} catch (RollbackException e) {
 			throw new DAOException(e);
 		} finally {
 			if (Transaction.isActive()) Transaction.rollback();
 		}
 	}
 	
 	public boolean updateDescription(int videoId, String description) throws DAOException {
 		try {
 			Transaction.begin();
 			VideoBean dbVideo = videoFactory.lookup(videoId);
 			if (dbVideo == null) {
 				throw new DAOException("VideoBean " + String.valueOf(videoId) + " does not exist");
 			}
 			dbVideo.setDescription(description);
 			Transaction.commit();
 			return true;
 		} catch (RollbackException e) {
 			throw new DAOException(e);
 		} finally {
 			if (Transaction.isActive()) Transaction.rollback();
 		}
 	}
 	
 	public boolean updateDate(int videoId, Date date) throws DAOException {
 		try {
 			Transaction.begin();
 			VideoBean dbVideo = videoFactory.lookup(videoId);
 			if (dbVideo == null) {
 				throw new DAOException("VideoBean " + String.valueOf(videoId) + " does not exist");
 			}
 			dbVideo.setDateValue(date.getTime());
 			Transaction.commit();
 			return true;
 		} catch (RollbackException e) {
 			throw new DAOException(e);
 		} finally {
 			if (Transaction.isActive()) Transaction.rollback();
 		}
 	}
 }
 
