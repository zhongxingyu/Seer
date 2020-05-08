 package com.server.cx.service.cx.impl;
 
 import com.cl.cx.platform.dto.Actions;
 import com.cl.cx.platform.dto.MGraphicDTO;
 import com.google.common.base.Preconditions;
 import com.server.cx.constants.Constants;
 import com.server.cx.dao.cx.GraphicResourceDao;
 import com.server.cx.dao.cx.HolidayTypeDao;
 import com.server.cx.dao.cx.UserHolidayMGraphicDao;
 import com.server.cx.dao.cx.spec.GraphicResourceSpecifications;
 import com.server.cx.entity.cx.GraphicResource;
 import com.server.cx.entity.cx.HolidayType;
 import com.server.cx.entity.cx.UserHolidayMGraphic;
 import com.server.cx.model.OperationResult;
 import com.server.cx.service.cx.HolidayService;
 import com.server.cx.service.cx.HolidayTypeService;
 import com.server.cx.service.cx.MGraphicService;
 import com.server.cx.service.cx.UserSubscribeGraphicItemService;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import java.util.Date;
 import java.util.List;
 
 @Service(value = "holidayMGraphicService")
 @Transactional
 public class HolidayMGraphicServiceImpl extends CheckAndHistoryMGraphicService implements MGraphicService {
 
     @Autowired
     private UserHolidayMGraphicDao userHolidayMGraphicDao;
 
     @Autowired
     private HolidayTypeService holidayTypeService;
 
     @Autowired
     private HolidayTypeDao holidayTypeDao;
     @Autowired
     private GraphicResourceDao graphicResourceDao;
 
     @Autowired
     private HolidayService holidayService;
 
     @Autowired
     private UserSubscribeGraphicItemService userSubscribeGraphicItemService;
 
     private String createAndSaveNewUserCommonMGraphic(MGraphicDTO mGraphicDTO) {
         Preconditions.checkNotNull(mGraphicDTO.getHolidayType(), "holidayType未提供");
         HolidayType holidayType = holidayTypeDao.findOne(mGraphicDTO.getHolidayType());
         deletePreviousHolidayMGraphic(holidayType);
         UserHolidayMGraphic holidayMGraphic = new UserHolidayMGraphic();
 
         List<GraphicResource> graphicResourceList = graphicResourceDao.findAll(GraphicResourceSpecifications.findGraphicResourceByGraphicinfo(graphicInfo));
         if(graphicResourceList != null && graphicResourceList.size() >0){
             holidayMGraphic.setGraphicResource(graphicResourceList.get(0));
         }
         holidayMGraphic.setUserInfo(userInfo);
         holidayMGraphic.setCommon(true);
         holidayMGraphic.setSubscribe(mGraphicDTO.getSubscribe());
         holidayMGraphic.setPhoneNos(null);
         holidayMGraphic.setHolidayType(holidayType);
         if (mGraphicDTO.getPhoneNos() != null && mGraphicDTO.getPhoneNos().size() > 0) {
             holidayMGraphic.setPriority(8);
             holidayMGraphic.setCommon(false);
             holidayMGraphic.setPhoneNos(mGraphicDTO.getPhoneNos());
         }
         Date appropriateDate = holidayService.getAppropriateHolidayDate(holidayType);
         holidayMGraphic.setHoliday(appropriateDate);
         updateMGraphicNameAndSignature(mGraphicDTO, holidayMGraphic);
         userHolidayMGraphicDao.save(holidayMGraphic);
         graphicInfoService.updateGraphicInfoUseCount(graphicInfo);
         return holidayMGraphic.getId();
     }
 
     private void deletePreviousHolidayMGraphic(HolidayType holidayType) {
         //同一个节日只允许 设置一个 节日彩像.
         List<UserHolidayMGraphic> historyHolidayMGraphics = userHolidayMGraphicDao.findByUserInfoAndHolidayType(
             userInfo, holidayType);
         for (UserHolidayMGraphic userHolidayMGraphic : historyHolidayMGraphics) {
             userHolidayMGraphicDao.delete(userHolidayMGraphic);
         }
     }
 
     @Override
     public OperationResult create(String imsi, Boolean isImmediate, MGraphicDTO mGraphicDTO, Boolean subscribe) throws RuntimeException {
         checkParameters(imsi, mGraphicDTO);
 
         if (isImmediate) {
             checkAndInitializeUserInfo(imsi);
             graphicInfo = holidayTypeService.getFirstChild(mGraphicDTO.getHolidayType());
             mGraphicDTO.setGraphicInfoId(graphicInfo.getId());
         }else {
             checkAndInitializeContext(imsi,mGraphicDTO);
             if(subscribe) {
                 userSubscribeGraphicItemService.subscribeGraphicItem(imsi, mGraphicDTO.getGraphicInfoId());
             } else {
                 userSubscribeGraphicItemService.checkUserSubscribeGraphicItem(userInfo, mGraphicDTO.getGraphicInfoId());
             }
             mGraphicDTO.setSubscribe(true);
         }
         checkMGraphicIdMustBeNotExists(mGraphicDTO);
         String mgraphicId = createAndSaveNewUserCommonMGraphic(mGraphicDTO);
         OperationResult operationResult = new OperationResult("createUserHolidayMGraphic", Constants.SUCCESS_FLAG);
        if (isImmediate) {
             Actions actions = actionBuilder.buildHolidayMGraphicItemCreatedAction(imsi, mgraphicId);
             operationResult.setActions(actions);
        }
         return operationResult;
     }
 
 
 
     @Override
     public OperationResult edit(String imsi, MGraphicDTO mGraphicDTO) {
         checkAndInitializeContext(imsi, mGraphicDTO);
         checkAndInitializeUserInfo(imsi);
         mGraphicIdMustBeExists(mGraphicDTO);
 //        userSubscribeGraphicItemService.checkUserSubscribeGraphicItem(userInfo, mGraphicDTO.getGraphicInfoId());
 
         UserHolidayMGraphic mGraphic = userHolidayMGraphicDao.findOne(mGraphicDTO.getId());
         if (mGraphicDTO.getPhoneNos() == null || mGraphicDTO.getPhoneNos().size() == 0) {
             mGraphic.setPhoneNos(null);
             mGraphic.setCommon(true);
             mGraphic.setPriority(7);
             updateMGraphicNameAndSignatureInEditMode(mGraphicDTO, mGraphic);
             userHolidayMGraphicDao.save(mGraphic);
         } else {
             mGraphic.setPhoneNos(mGraphicDTO.getPhoneNos());
             mGraphic.setPriority(8);
             mGraphic.setCommon(false);
             updateMGraphicNameAndSignatureInEditMode(mGraphicDTO, mGraphic);
             userHolidayMGraphicDao.save(mGraphic);
         }
         return new OperationResult("editUserCommonMGraphic", Constants.SUCCESS_FLAG);
     }
 
     @Override
     public OperationResult disable(String imsi, String mgraphicId) {
         Preconditions.checkNotNull(imsi, "imsi为空");
         Preconditions.checkNotNull(mgraphicId, "指定对象不存在");
         checkAndSetUserInfoExists(imsi);
         UserHolidayMGraphic holidayMGraphic = userHolidayMGraphicDao.findOne(mgraphicId);
         if (holidayMGraphic != null) {
             userHolidayMGraphicDao.delete(holidayMGraphic);
         }
         return new OperationResult("deleteHolidayMGraphic", Constants.SUCCESS_FLAG);
     }
 
 }
