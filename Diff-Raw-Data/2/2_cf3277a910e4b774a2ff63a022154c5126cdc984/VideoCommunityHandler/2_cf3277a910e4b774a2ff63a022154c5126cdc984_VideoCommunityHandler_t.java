 package com.lorent.web.xmlrpc.handler;
 
 import java.net.InetAddress;
 import java.util.ArrayList;
 import java.util.List;
 
 import com.lorent.common.dto.LCMVideoClip;
 import com.lorent.model.VideoClipBean;
 
 public class VideoCommunityHandler extends BaseHandler {
 
 	/**
 	 * 
 	 */
 	private static final long serialVersionUID = 1L;
 
 	
 	//上传视频信息
 	public boolean uploadVideoClipInfo(String videoClipName,String thumbnailFtpUrl,String title,String description,String ftpSrvIp) throws Exception{
 		//VideoCommunity
		String videoClipUrl = "rtsp://"+ftpSrvIp+":554/"+videoClipName;
 		VideoClipBean bean = new VideoClipBean();
 		bean.setVideoClipUrl(videoClipUrl);
 		bean.setThumbnailUrl(thumbnailFtpUrl);
 		bean.setTitle(title);
 		bean.setDescription(description);
 		serviceFacade.getVideoClipService().addVideoClip(bean);
 		return true;
 	}
 	
 	//删除视频信息
 	public boolean deleteVideoClip(int videoClipId) throws Exception{
 		return serviceFacade.getVideoClipService().deleteVideoClip(videoClipId);
 	}
 	
 	//获得视频列表信息
 	public List<LCMVideoClip> getVideoClipList(Integer pageIndex,Integer pageSize) throws Exception{
 		 List<VideoClipBean> videoClipList = serviceFacade.getVideoClipService().getVideoClipList(pageIndex, pageSize);
 		 if (videoClipList.size() <= 0) {
 			return null;
 		 }
 		 ArrayList<LCMVideoClip> arrayList = new ArrayList<LCMVideoClip>();
 		 for (VideoClipBean videoClipBean : videoClipList) {
 			 LCMVideoClip lcmVideoClip = new LCMVideoClip();
 			 lcmVideoClip.setId(videoClipBean.getId());
 			 lcmVideoClip.setStatus(videoClipBean.getStatus());
 			 lcmVideoClip.setVideoClipUrl(videoClipBean.getVideoClipUrl());
 			 lcmVideoClip.setThumbnailUrl(videoClipBean.getThumbnailUrl());
 			 lcmVideoClip.setTitle(videoClipBean.getTitle());
 			 lcmVideoClip.setDescription(videoClipBean.getDescription());
 			 lcmVideoClip.setCategory(videoClipBean.getCategory());
 			 arrayList.add(lcmVideoClip);
 		 }
 		 return arrayList;
 	}
 	
 	public Integer getVideoListLength() throws Exception{
 		return serviceFacade.getVideoClipService().getAll().size();
 	}
 }
