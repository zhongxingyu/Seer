 package com.astrider.sfc.src.model;
 
 import java.util.ArrayList;
 
 import javax.servlet.http.HttpServletRequest;
 
 import com.astrider.sfc.app.lib.BaseModel;
 import com.astrider.sfc.app.lib.Mapper;
 import com.astrider.sfc.app.lib.Validator;
 import com.astrider.sfc.src.helper.SanteUtils;
 import com.astrider.sfc.src.helper.WeeklyLogUtils;
 import com.astrider.sfc.src.model.dao.NutrientDao;
 import com.astrider.sfc.src.model.dao.UserStatsDao;
 import com.astrider.sfc.src.model.dao.WeeklyLogDao;
 import com.astrider.sfc.src.model.vo.db.NutrientVo;
 import com.astrider.sfc.src.model.vo.db.UserStatsVo;
 import com.astrider.sfc.src.model.vo.db.UserVo;
 import com.astrider.sfc.src.model.vo.db.WeeklyLogVo;
 import com.astrider.sfc.src.model.vo.form.AddNutrientFormVo;
 
 /**
  * API関連Model.
  * 
  * @author astrider
  * 
  */
 public class ApiModel extends BaseModel {
 
 	/**
 	 * ユーザーステータス取得API.
 	 * 
 	 * @param request
 	 * @return
 	 */
 	public boolean getStats(HttpServletRequest request) {
 		UserVo user = SanteUtils.getLoginUser(request);
 		if (user == null) {
 			return returnFailStatus(request);
 		}
 
 		UserStatsDao userStatsDao = new UserStatsDao();
 		UserStatsVo userStats = userStatsDao.selectByUserId(user.getUserId());
 		userStatsDao.close();
 		if (userStats == null) {
 			return returnFailStatus(request);
 		}
 
 		request.setAttribute("userStats", userStats);
 		request.setAttribute("success", true);
 		return true;
 	}
 
 	/**
 	 * 栄養素取得API.
 	 * 
 	 * @param request
 	 * @return
 	 */
 	public boolean getNutrients(HttpServletRequest request) {
 		UserVo user = SanteUtils.getLoginUser(request);
 		if (user == null) {
 			return returnFailStatus(request);
 		}
 
 		WeeklyLogVo weekVo = WeeklyLogUtils.getCurrentLog(user.getUserId());
 		NutrientDao nutrientDao = new NutrientDao();
 		ArrayList<NutrientVo> nutrients = nutrientDao.selectAll();
 		nutrientDao.close();
 
 		request.setAttribute("ingested", weekVo);
 		request.setAttribute("desired", nutrients);
 		request.setAttribute("success", true);
 		return true;
 	}
 
 	/**
 	 * 円グラフ用API.
 	 * 
 	 * @param request
 	 * @return
 	 */
 	public boolean getChartSource(HttpServletRequest request) {
 		UserVo user = SanteUtils.getLoginUser(request);
 		if (user == null) {
 			return returnFailStatus(request);
 		}
 
 		// requestParametersからweekAgoを取得
 		int weekAgo = 0;
 		try {
 			weekAgo = Integer.valueOf(request.getParameter("weekAgo"));
 			if (weekAgo < 0) {
 				weekAgo = 0;
 			}
 		} catch (NumberFormatException e) {
 			weekAgo = 0;
 		}
 
 		double[] items = SanteUtils.getNutrientBalances(user.getUserId(), weekAgo);
 		if (items == null) {
 			return returnFailStatus(request);
 		}
		
		for (int i = 0; i < items.length; i++) {
			items[i] = items[i] / 2;
		}
 
 		request.setAttribute("items", items);
 		request.setAttribute("success", true);
 		return true;
 	}
 
 	public boolean addNutrient(HttpServletRequest request) {
 		UserVo user = SanteUtils.getLoginUser(request);
 		if (user == null) {
 			return returnFailStatus(request);
 		}
 
 		Mapper<AddNutrientFormVo> mapper = new Mapper<AddNutrientFormVo>();
 		AddNutrientFormVo form = mapper.fromHttpRequest(request);
 		Validator<AddNutrientFormVo> validator = new Validator<AddNutrientFormVo>(form);
 		if (!validator.valid()) {
 			return returnFailStatus(request);
 		}
 
 		WeeklyLogDao weeklyLogDao = new WeeklyLogDao();
 		WeeklyLogVo weeklyLog = weeklyLogDao.selectCurrentWeek(user.getUserId());
 		addNutrientToAmountById(weeklyLog, form.getNutrientId(), form.getAmount());
 		weeklyLogDao.update(weeklyLog);
 		weeklyLogDao.close();
 		WeeklyLogUtils.updateCurrentTotalBalance(user.getUserId());
 		
 		request.setAttribute("success", true);
 		return true;
 	}
 
 	private boolean returnFailStatus(HttpServletRequest request) {
 		request.setAttribute("success", false);
 		request.setAttribute("message", "failed");
 		return false;
 	}
 
 	private void addNutrientToAmountById(WeeklyLogVo weeklyLog, int nutrientId, int amount) {
 		switch (nutrientId) {
 		case 1:
 			weeklyLog.setMilk(weeklyLog.getMilk() + amount);
 			break;
 		case 2:
 			weeklyLog.setEgg(weeklyLog.getEgg() + amount);
 			break;
 		case 3:
 			weeklyLog.setMeat(weeklyLog.getMeat() + amount);
 			break;
 		case 4:
 			weeklyLog.setBean(weeklyLog.getBean() + amount);
 			break;
 		case 5:
 			weeklyLog.setVegetable(weeklyLog.getVegetable() + amount);
 			break;
 		case 6:
 			weeklyLog.setFruit(weeklyLog.getFruit() + amount);
 			break;
 		case 7:
 			weeklyLog.setMineral(weeklyLog.getMineral() + amount);
 			break;
 		case 8:
 			weeklyLog.setCrop(weeklyLog.getCrop() + amount);
 			break;
 		case 9:
 			weeklyLog.setPotato(weeklyLog.getPotato() + amount);
 			break;
 		case 10:
 			weeklyLog.setFat(weeklyLog.getFat() + amount);
 			break;
 		case 11:
 			weeklyLog.setSuguar(weeklyLog.getSuguar() + amount);
 			break;
 		default:
 			break;
 		}
 	}
 }
