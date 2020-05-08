 package kwitches.service;
 
 import java.util.Date;
 import java.util.Map;
 
 import org.slim3.controller.upload.FileItem;
 import org.slim3.util.BeanUtil;
 
 import kwitches.message.MessageFactory;
 import kwitches.message.MessageInterface;
 import kwitches.model.BBSDataModel;
 import kwitches.model.UploadedData;
 import kwitches.model.UserModel;
 import kwitches.service.dao.BBSDataModelDao;
 import kwitches.text.tokenizer.AnalyzerInterface;
 import kwitches.text.tokenizer.SimpleAnalyzer;
 
 public class SignService {
 
     private static BBSDataModelDao bbsDao = BBSDataModelDao.GetInstance();
     private static UploadFileService uploadFileService = new UploadFileService();
     private static MessageService ms = new MessageService();
     private static AnalyzerInterface analizer = new SimpleAnalyzer();

     public BBSDataModel sign(Map<String, Object> input,
            String ipAddress, Date createdDate,
             UserModel userModel, FileItem formFile) {
         String comment = (String) input.get("comment");
         comment = comment.replace("\r\n", "\n");
         input.put("comment", comment);
         BBSDataModel bbsDataModel = new BBSDataModel();
         bbsDataModel.getUserModelRef().setModel(userModel);
        if(userModel.getIconRef() != null){
            bbsDataModel.getIconRef().setModel(userModel.getIconRef().getModel());
        }
         input.put("id", BBSDataModelDao.getMaxId() + 1);
         input.put("createdDate", createdDate);
         input.put("ipAddress", ipAddress);
         input.put("invertedIndex", analizer.parse(comment));
         if (comment != null && comment.length() >= 500) {
             input.put("longComment", comment);
             input.remove("comment");
         }
         BeanUtil.copy(input, bbsDataModel);
         if (formFile != null) {
             UploadedData uploadedData = uploadFileService.upload(formFile);
             bbsDataModel.getUploadedDataRef().setKey(uploadedData.getKey());
         }
         bbsDao.putBBSData(bbsDataModel);
        MessageInterface message =
             MessageFactory.create(MessageFactory.Type.SIGN);
         message.setInformation(bbsDataModel);
         ms.sendMessageAll(message);
         return bbsDataModel;
      }
 
 }
