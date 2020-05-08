 /**
  * $Id$
  * Copyright(C) 2012-2016 msnvip@msn.cn. All rights reserved.
  */
 package org.you.game.sango.action;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.struts.action.ActionForm;
 import org.apache.struts.action.ActionForward;
 import org.apache.struts.action.ActionMapping;
 import org.you.core.web.BaseAction;
 import org.you.game.sango.dao.ISkillInfoDao;
 import org.you.game.sango.dao.impl.GuiceFactory;
 import org.you.game.sango.model.SkillSimpleInfo;
 
 /**
  *
  * @author <a href="mailto:msnvip@msn.cn">msnvip</a>
  * @version 1.0
  * @since 1.0
  */
 public class SkillInfoAction extends BaseAction{
 	
 	private ISkillInfoDao skillInfoDao = GuiceFactory.getInstance(ISkillInfoDao.class);
 	
 	@Override
 	protected ActionForward customeExecute(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response) {
 		if(operate.equals("get_simple_list")){
 			return getSimpleList(mapping, form, request, response);
 		}
 		
 		return null;
 	}
 	
 	public ActionForward getSimpleList(ActionMapping mapping, ActionForm form,
 			HttpServletRequest request, HttpServletResponse response) {
 		int category = getParamInt("category", request);
 		int page = getParamInt("page", request);
 		
 		List<SkillSimpleInfo> simpleList = skillInfoDao.getSimpleList(category, page);
 		try {
 			writeJsonSuccess(simpleList, response);
 		} catch (IOException e) {
 			e.printStackTrace();
 		}
 		
 		return null;
 	}

	@Override
	protected boolean checkSkValid(String sk) {
		return super.checkSkValid(sk);
	}

	@Override
	protected long parseUserId(String sk) {
		return 10000L;
	}
 	
 	
 	
 }
