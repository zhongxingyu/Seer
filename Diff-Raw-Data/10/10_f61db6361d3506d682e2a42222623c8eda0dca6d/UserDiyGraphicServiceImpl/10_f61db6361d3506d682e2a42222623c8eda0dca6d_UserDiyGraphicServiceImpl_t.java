 package com.server.cx.service.cx.impl;
 
 import com.cl.cx.platform.dto.MGraphicDTO;
 import com.google.common.io.ByteStreams;
 import com.server.cx.constants.Constants;
 import com.server.cx.dao.cx.GraphicResourceDao;
 import com.server.cx.dao.cx.UserCommonMGraphicDao;
 import com.server.cx.dao.cx.UserDiyGraphicDao;
 import com.server.cx.dao.cx.spec.UserCommonMGraphicSpecifications;
 import com.server.cx.entity.cx.*;
 import com.server.cx.exception.CXServerBusinessException;
 import com.server.cx.model.OperationResult;
 import com.server.cx.service.cx.UserDiyGraphicService;
 import com.server.cx.service.util.BusinessFunctions;
 import com.server.cx.util.business.AuditStatus;
 import com.server.cx.util.business.JerseyResourceUtil;
 import com.server.cx.util.business.ValidationUtil;
 import com.sun.jersey.api.client.ClientResponse;
 import com.sun.jersey.api.client.GenericType;
 import com.sun.jersey.api.client.WebResource;
 import com.sun.jersey.api.client.filter.HTTPBasicAuthFilter;
 import com.sun.jersey.core.header.FormDataContentDisposition;
 import com.sun.jersey.multipart.FormDataBodyPart;
 import com.sun.jersey.multipart.FormDataMultiPart;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Qualifier;
 import org.springframework.context.annotation.Scope;
 import org.springframework.stereotype.Component;
 import org.springframework.transaction.annotation.Transactional;
 
 import javax.ws.rs.core.MediaType;
 import javax.ws.rs.core.Response;
 import java.io.ByteArrayInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.util.Date;
 import java.util.List;
 
 @Component
 @Transactional
 @Scope("request")
 public class UserDiyGraphicServiceImpl extends CheckAndHistoryMGraphicService implements UserDiyGraphicService {
     @Autowired
     private BusinessFunctions businessFunctions;
 
     @Autowired
     private UserDiyGraphicDao userDiyGraphicDao;
 
     @Autowired
     private GraphicResourceDao graphicResourceDao;
     @Autowired
     @Qualifier("fileUploadUrl")
     private String fileUploadUrl;
 
     @Autowired
     private UserCommonMGraphicDao userCommonMGraphicDao;
     private UserDiyGraphic userDiyGraphic;
 
     @Transactional(readOnly=false)
     @Override
     public void addFileStreamToResourceServer(String imsi, InputStream fileStream) throws IOException {
         checkAndSetUserInfoExists(imsi);
         FileMeta fileMeta = uploadToResourceServer(imsi, fileStream);
         UserDiyGraphic userDiyGraphic = userDiyGraphicDao.findByUserInfo(userInfo);
         if(userDiyGraphic == null){
             userDiyGraphic = new UserDiyGraphic();
             userDiyGraphic.setName("自定义");
             userDiyGraphic.setSignature("自定义");
             userDiyGraphic.setUserInfo(userInfo);
             userDiyGraphic.setAuditStatus(AuditStatus.CHECKING);
             userDiyGraphic = userDiyGraphicDao.save(userDiyGraphic);
         }
         GraphicResource graphicResource = businessFunctions.fileMetaTransformToGraphicInfo(userDiyGraphic).apply(fileMeta);
         graphicResource.setDiyGraphic(userDiyGraphic);
         graphicResourceDao.save(graphicResource);
     }
 
     @Override
     public OperationResult delete(String id) {
         ValidationUtil.checkParametersNotNull(id);
         GraphicResource graphicResource = graphicResourceDao.findOne(id);
         graphicResource.setDiyGraphic(null);
         graphicResourceDao.save(graphicResource);
 //        userDiyGraphicDao.delete(id);
         return new OperationResult("deleteUserDIYGraphic","Success");
     }
 
     @Override
     public OperationResult create(String imsi, MGraphicDTO mGraphicDTO) {
         checkAndInitializeUserInfo(imsi);
         checkMGraphicIdMustBeNotExists(mGraphicDTO);
 
         if(mGraphicDTO.getPhoneNos() == null || mGraphicDTO.getPhoneNos().size() == 0){
             historyPreviousMGraphic();
        }else{
            Long dataRowNumber = userCommonMGraphicDao.count(UserCommonMGraphicSpecifications.userCommonMGraphicCount(userInfo));
            if (dataRowNumber >= 5) {
                throw new CXServerBusinessException("指定号码用户设置彩像最多允许5个");
            }
         }
         createAndSaveNewUserCommonMGraphic(mGraphicDTO);
         return new OperationResult("createUserCommonMGraphic", Constants.SUCCESS_FLAG);
     }
 
     //TODO Many Many code duplicate ....By Zou YanJian
     private void createAndSaveNewUserCommonMGraphic(MGraphicDTO mGraphicDTO) {
         UserCommonMGraphic userCommonMGraphic = new UserCommonMGraphic();
         GraphicResource graphicResource = graphicResourceDao.findOne(mGraphicDTO.getGraphicInfoId());
         userCommonMGraphic.setUserInfo(userInfo);
         userCommonMGraphic.setCommon(true);
         userCommonMGraphic.setGraphicResource(graphicResource);
         if(mGraphicDTO.getPhoneNos() != null && mGraphicDTO.getPhoneNos().size() > 0){
             userCommonMGraphic.setCommon(false);
             userCommonMGraphic.setPhoneNos(mGraphicDTO.getPhoneNos());
             userCommonMGraphic.setPriority(4);
         }
         userDiyGraphic = graphicResource.getDiyGraphic();
         updateMGraphicNameAndSignature(mGraphicDTO, userCommonMGraphic);
         userCommonMGraphicDao.save(userCommonMGraphic);
     }
     @Override
     protected void updateMGraphicNameAndSignature(MGraphicDTO mGraphicDTO, MGraphic mgraphic) {
         mgraphic.setName(judgeString(mGraphicDTO.getName(), userDiyGraphic.getName()));
         mgraphic.setSignature(judgeString(mGraphicDTO.getSignature(), userDiyGraphic.getSignature()));
     }
 
     private void historyPreviousMGraphic() {
         historyPreviousMGraphic(null);
     }
 
     private void historyPreviousMGraphic(MGraphic mGraphic) {
         List<UserCommonMGraphic> previousUserCommonMGraphics = userCommonMGraphicDao.findByUserInfoAndModeTypeAndCommon(userInfo, 2, true);
         for (UserCommonMGraphic userCommonMGraphic : previousUserCommonMGraphics) {
             if(mGraphic!=null && userCommonMGraphic.getId().equals(mGraphic.getId())){
                 continue;
             }
             if(userCommonMGraphic.getGraphicResource().getGraphicInfo() != null){
                 historyPreviousUserCommonMGraphic(userCommonMGraphic);
             }
             userCommonMGraphicDao.delete(userCommonMGraphic);
         }
     }
 
 
     private FileMeta uploadToResourceServer(String imsi, InputStream stream) throws IOException {
         FormDataMultiPart mp = new FormDataMultiPart();
         byte[] arrays = ByteStreams.toByteArray(stream);
         long fileSize = arrays.length;
         if(fileSize <=1){
             throw new CXServerBusinessException("上传到数据流为空!");
         }
         String fileName = imsi+ new Date().getTime();
         ByteArrayInputStream inputStream = new ByteArrayInputStream(arrays);
         FormDataBodyPart inputStreamBody = new FormDataBodyPart(FormDataContentDisposition.name("file")
                 .fileName(fileName).size(fileSize).build(),inputStream, MediaType.APPLICATION_OCTET_STREAM_TYPE);
         mp.bodyPart(inputStreamBody);
         JerseyResourceUtil.getClient().addFilter(
                 new HTTPBasicAuthFilter(Constants.RESOURCE_SERVER_USERNAME, Constants.RESOURCE_SERVER_PASSWORD));
         WebResource webResource = JerseyResourceUtil.getClient().resource(fileUploadUrl);
         ClientResponse response = webResource.type(MediaType.MULTIPART_FORM_DATA_TYPE).accept(MediaType.APPLICATION_JSON).post(ClientResponse.class, mp);
         if(response.getStatus()!= Response.Status.OK.getStatusCode()){
             throw new CXServerBusinessException("图片保存失败!");
         }
         GenericType<List<FileMeta>> genericType = new GenericType<List<FileMeta>>() {};
         List<FileMeta> imageResourceList = response.getEntity(genericType);
         if(imageResourceList != null && !imageResourceList.isEmpty()) {
             return imageResourceList.get(0);
         }
         return null;
     }
 
 }
