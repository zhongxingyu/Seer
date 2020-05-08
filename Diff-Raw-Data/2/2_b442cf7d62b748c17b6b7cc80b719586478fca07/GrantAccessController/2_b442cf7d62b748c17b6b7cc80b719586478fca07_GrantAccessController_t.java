 /*
  * Copyright (c) 2006 Oliver Stewart.  All Rights Reserved.
  *
  * This file is free software; you can redistribute it and/or modify
  * it under the terms of the GNU General Public License as published by
  * the Free Software Foundation; either version 2, or (at your option)
  * any later version.
  *
  * This file is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  */
 package com.trailmagic.image.ui;
 
 import com.trailmagic.image.Image;
 import com.trailmagic.image.ImageFactory;
 import com.trailmagic.image.ImageGroup;
 import com.trailmagic.image.ImageGroupFactory;
 import com.trailmagic.image.security.ImageSecurityFactory;
 import com.trailmagic.user.User;
 import com.trailmagic.user.UserFactory;
 import org.springframework.web.servlet.mvc.SimpleFormController;
 import org.apache.log4j.Logger;
 import org.springframework.web.servlet.ModelAndView;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import org.springframework.validation.BindException;
 import java.util.List;
 import java.util.HashMap;
 
 public class GrantAccessController extends SimpleFormController {
     private ImageSecurityFactory m_imageSecurityFactory;
     private ImageFactory m_imageFactory;
     private ImageGroupFactory m_imageGroupFactory;
     private UserFactory m_userFactory;
 
     private static Logger s_log =
         Logger.getLogger(GrantAccessController.class);
 
     private static final String GRANT_ACTION = "grant";
     private static final String MAKE_PUBLIC_ACTION = "makePublic";
 
     public ImageSecurityFactory getImageSecurityFactory() {
         return m_imageSecurityFactory;
     }
 
     public void setImageSecurityFactory(ImageSecurityFactory factory) {
         m_imageSecurityFactory = factory;
     }
 
     public ImageFactory getImageFactory() {
         return m_imageFactory;
     }
 
     public void setImageFactory(ImageFactory factory) {
         m_imageFactory = factory;
     }
 
     public ImageGroupFactory getImageGroupFactory() {
         return m_imageGroupFactory;
     }
 
     public void setImageGroupFactory(ImageGroupFactory factory) {
         m_imageGroupFactory = factory;
     }
 
     public void setUserFactory(UserFactory factory) {
         m_userFactory = factory;
     }
 
     protected ModelAndView showForm(HttpServletRequest req,
                                     HttpServletResponse res,
                                     BindException errors) throws Exception {
         String groupId = req.getParameter("groupId");
         ImageGroup group =
             m_imageGroupFactory.getById(Long.parseLong(groupId));
 
         HashMap<String,Object> model = new HashMap<String,Object>();
        model.put("imageGroupIsPublic",
                  m_imageSecurityFactory.isPublic(group));
         model.put("imageGroup", group);
         model.put("groupType", group.getType());
         model.put("groupTypeDisplay",
                   group.getType().substring(0, 1).toUpperCase()
                   + group.getType().substring(1));
         model.put("owner", group.getOwner());
         model.put("frames", group.getFrames());
         return new ModelAndView(getFormView(), model);
     }
 
     protected void doSubmitAction(Object command) throws Exception {
         GrantAccessBean bean = (GrantAccessBean) command;
 
         if (bean == null) {
             throw new Exception("null command");
         }
 
         List<String> imageIds = bean.getImageIds();
         for (String stringId : imageIds) {
             Long id = Long.parseLong(stringId);
             Image image = m_imageFactory.getById(id);
             User recipient =
                 m_userFactory.getByScreenName(bean.getRecipient());
             if (GRANT_ACTION.equals(bean.getAction())) {
                 s_log.info("Adding permission " + bean.getMask()
                            + " for " + recipient.getScreenName()
                            + " to Image: " + image);
                 m_imageSecurityFactory.addPermission(image,
                                                      recipient.getScreenName(),
                                                      bean.getMask());
             } else if (MAKE_PUBLIC_ACTION.equals(bean.getAction())) {
                 s_log.info("Making Image public: " + image);
                 m_imageSecurityFactory.makePublic(image);
             } else {
                 throw new IllegalArgumentException("Unknown action");
             }
         }
     }
 }
