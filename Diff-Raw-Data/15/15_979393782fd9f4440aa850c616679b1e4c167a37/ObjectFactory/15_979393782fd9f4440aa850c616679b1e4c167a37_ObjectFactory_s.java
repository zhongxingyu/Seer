 package com.server.cx.util;
 
 import java.util.List;
 import com.cl.cx.platform.dto.Actions;
 import com.cl.cx.platform.dto.OperationDescription;
 import com.cl.cx.platform.dto.SuggestionDTO;
 import com.cl.cx.platform.dto.VersionInfoDTO;
 import com.google.common.collect.Lists;
 import com.server.cx.entity.cx.Suggestion;
 import com.server.cx.entity.cx.UserSubscribeRecord;
 import com.server.cx.entity.cx.UserSubscribeType;
 
 public class ObjectFactory {
     public static VersionInfoDTO buildVersionInfoDTO(String flag, String content) {
         VersionInfoDTO versionInfoDTO = new VersionInfoDTO();
         versionInfoDTO.setFlag(flag);
         versionInfoDTO.setContent(content);
         return versionInfoDTO;
     }
 
     public static VersionInfoDTO buildVersionInfoDTO(String flag, String content, String forceUpdate, String url) {
         VersionInfoDTO versionInfoDTO = buildVersionInfoDTO(flag, content);
         versionInfoDTO.setForceUpdate(forceUpdate);
         versionInfoDTO.setUrl(url);
         return versionInfoDTO;
     }
 
     public static OperationDescription buildOperationDescription(int statusCode, String actionName, String flag) {
         OperationDescription operationDescription = new OperationDescription();
         operationDescription.setStatusCode(statusCode);
         operationDescription.setActionName(actionName);
         operationDescription.setDealResult(flag);
         return operationDescription;
     }
 
     public static OperationDescription buildErrorOperationDescription(int errorCode, String actionName, String flag) {
         OperationDescription operationDescription = new OperationDescription();
         operationDescription.setErrorCode(errorCode);
         operationDescription.setActionName(actionName);
        operationDescription.setDealResult(flag);
         return operationDescription;
     }
 
     public static List<SuggestionDTO> buildAllSuggestionDTOFromSuggestion(List<Suggestion> suggestions) {
         if(suggestions == null || suggestions.isEmpty()) 
             return null;
         
         List<SuggestionDTO> suggestionDTOs = Lists.newArrayList();
         for(Suggestion suggestion : suggestions) {
             SuggestionDTO suggestionDTO = new SuggestionDTO();
             suggestionDTO.setContent(suggestion.getContent());
             suggestionDTOs.add(suggestionDTO);
         }
         return suggestionDTOs;
     }
 
     public static UserSubscribeRecord buildUserSubscribeRecord(UserSubscribeType userSubscribeType) {
         UserSubscribeRecord userSubscribeRecord = new UserSubscribeRecord();
         userSubscribeRecord.setSubscribeType(userSubscribeType.getSubscribeType());
         userSubscribeRecord.setUserInfo(userSubscribeType.getUserInfo());
         userSubscribeRecord.setDescription("订购");
         return userSubscribeRecord;
     }
     
     public static UserSubscribeRecord buildUserCancelSubscribeRecord(UserSubscribeType userSubscribeType) {
         UserSubscribeRecord userSubscribeRecord = new UserSubscribeRecord();
         userSubscribeRecord.setSubscribeType(userSubscribeType.getSubscribeType());
         userSubscribeRecord.setUserInfo(userSubscribeType.getUserInfo());
         userSubscribeRecord.setDescription("取消订购");
         return userSubscribeRecord;
     }
     
 
     public static OperationDescription buildOperationDescription(int statusCode, String actionName, String flag,
                                                                  Actions actions) {
         OperationDescription operationDescription = new OperationDescription();
         operationDescription.setStatusCode(statusCode);
         operationDescription.setActionName(actionName);
         operationDescription.setDealResult(flag);
         operationDescription.setActions(actions);
         return operationDescription;
     }
 }
