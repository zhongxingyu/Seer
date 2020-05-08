 package com.mediaplatform.manager.media;
 
 import com.mediaplatform.manager.AntiSamyBean;
 import com.mediaplatform.model.LiveStream;
 import com.mediaplatform.security.Admin;
 import com.mediaplatform.util.ConversationUtils;
 import com.mediaplatform.util.RtmpPublishFormat;
 import com.mediaplatform.util.RunShellCmdHelper;
 import com.mediaplatform.util.jsf.FacesUtil;
 
 import javax.ejb.Stateful;
 import javax.enterprise.context.ConversationScoped;
 import javax.faces.application.FacesMessage;
 import javax.inject.Inject;
 import javax.inject.Named;
 import java.util.List;
 
 /**
  * User: timur
  * Date: 12/28/12
  * Time: 12:20 AM
  */
 @Stateful
 @ConversationScoped
 @Named
 public class LiveStreamManager extends AbstractContentManager {
     private List<LiveStream> liveStreams;
 
     private LiveStream currentStream;
 
     @Inject
     private AntiSamyBean antiSamyBean;
 
     public List<LiveStream> getLiveStreams(){
        if(liveStreams == null){
            liveStreams = appEm.createQuery("select s from LiveStream s").getResultList();
        }
         return liveStreams;
     }
 
     public void setLiveStreams(List<LiveStream> liveStreams) {
         this.liveStreams = liveStreams;
     }
 
     public LiveStream getCurrentStream() {
         if(currentStream == null){
             currentStream = new LiveStream();
         }
         return currentStream;
     }
 
     public void setCurrentStream(LiveStream currentStream) {
         this.currentStream = currentStream;
     }
 
     public void show(){
         refresh();
     }
 
     @Admin
     public void edit(LiveStream stream){
         ConversationUtils.safeBegin(conversation);
         this.currentStream = stream;
     }
 
     public void validateLiveContentId(javax.faces.context.FacesContext facesContext, javax.faces.component.UIComponent uiComponent, java.lang.Object obj){
         if("conversation_ended".equals(obj)){
             FacesUtil.redirectToEndConversation();
             return;
         }
         boolean ok = FacesUtil.validateLong(facesContext, uiComponent, obj, "Stream ID not defined");
         if(ok){
             Long id = Long.parseLong(String.valueOf(obj));
             if(getStreamById(id) == null){
                 facesContext.addMessage(null, new FacesMessage(FacesMessage.SEVERITY_ERROR, "Stream with ID '" + id + "' not found", null));
                 ok = false;
             }
         }
         if(!ok){
             FacesUtil.redirectToHomePage();
         }
     }
 
     @Admin
     public void saveOrCreate(){
         antiSamyBean.prepare(currentStream);
         if(currentStream.getId() == null){
             appEm.persist(currentStream);
         }else{
             appEm.merge(currentStream);
         }
         ConversationUtils.safeEnd(conversation);
         refresh();
     }
 
     @Admin
     public void remove(LiveStream stream){
         appEm.remove(appEm.find(LiveStream.class, stream.getId()));
         ConversationUtils.safeEnd(conversation);
         refresh();
     }
 
     private void refresh() {
         this.currentStream = null;
         this.liveStreams = null;
     }
 
     @Admin
     public void publish(LiveStream stream){
         appEm.merge(stream);
         this.currentStream = stream;
         liveStreams = null;
         //todo make unique
         String streamName = stream.getTitle() + "?" + getCallbackQueryParams();
         if(stream.isPublished()){
             RunShellCmdHelper.dropStream(stream.getTitle(), configBean.getStreamDropUrl());
             RunShellCmdHelper.publish(stream.getSource(), streamName, RtmpPublishFormat.MP4_LIVE_STREAM_HIGH);
             messages.info("Published " + stream.getSource());
         }else{
             RunShellCmdHelper.dropStream(stream.getTitle(), configBean.getStreamDropUrl());
             messages.info("Dropped " + stream.getSource());
         }
     }
 
     @Admin
     public void add(){
         ConversationUtils.safeBegin(conversation);
         currentStream = new LiveStream();
     }
 
     public void viewLiveVideo(String id) {
         ConversationUtils.safeBegin(conversation);
         this.currentStream = getStreamById(Long.parseLong(id));
     }
 
 
     @Override
     protected String getCurrentContentName() {
         return getCurrentStream().getTitle();
     }
 
     public boolean isEdit(){
         return currentStream != null && currentStream.getId() != null;
     }
 
 }
