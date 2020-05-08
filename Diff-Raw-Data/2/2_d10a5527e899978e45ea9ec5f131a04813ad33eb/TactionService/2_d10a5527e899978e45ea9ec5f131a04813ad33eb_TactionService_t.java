 package com.ruyicai.actioncenter.service;
 
 import java.math.BigDecimal;
 import java.util.Calendar;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import org.apache.camel.Produce;
 import org.apache.camel.ProducerTemplate;
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 import com.ruyicai.actioncenter.consts.ActionJmsType;
 import com.ruyicai.actioncenter.dao.Fund2DrawDao;
 import com.ruyicai.actioncenter.dao.TuserPrizeDetailDao;
 import com.ruyicai.actioncenter.dao.VipUserDao;
 import com.ruyicai.actioncenter.domain.Chong20Mobile;
 import com.ruyicai.actioncenter.domain.FirstChargeUser;
 import com.ruyicai.actioncenter.domain.Fund2Draw;
 import com.ruyicai.actioncenter.domain.OldUserChongZhi;
 import com.ruyicai.actioncenter.domain.SendMoneyDetails;
 import com.ruyicai.actioncenter.domain.Tactivity;
 import com.ruyicai.actioncenter.domain.TaddNumActivity;
 import com.ruyicai.actioncenter.domain.Tagent;
 import com.ruyicai.actioncenter.domain.Tjmsservice;
 import com.ruyicai.actioncenter.domain.TuserPrizeDetail;
 import com.ruyicai.actioncenter.domain.Tuseraction;
 import com.ruyicai.actioncenter.domain.VipUser;
 import com.ruyicai.actioncenter.exception.RuyicaiException;
 import com.ruyicai.actioncenter.util.DateUtil;
 import com.ruyicai.actioncenter.util.ErrorCode;
 import com.ruyicai.actioncenter.util.JsonUtil;
 import com.ruyicai.lottery.domain.Tuserinfo;
 import com.ruyicai.lottery.dto.OrderRequest;
 
 @Service
 public class TactionService {
 
 	private Logger logger = LoggerFactory.getLogger(TactionService.class);
 
 	@Autowired
 	private LotteryService lotteryService;
 
 	@Autowired
 	private TuserPrizeDetailDao tuserPrizeDetailDao;
 
 	@Autowired
 	private VipUserDao vipUserDao;
 
 	@Produce(uri = "jms:topic:sendActivityPrize")
 	private ProducerTemplate sendActivityPrizeProducer;
 
 	@Value("${addNumOneYearSwitch}")
 	private String addNumOneYearSwitch;
 
 	@Autowired
 	private Fund2DrawDao fund2DrawDao;
 
 	public void processFundJMSCustomer(String ttransactionid, Long ladderpresentflag, String userno, Long amtLong,
 			Integer actionJmsType, String businessId, Integer businessType) {
 		logger.info(
 				"processFundJMSCustomer  userno:{},amt:{},actionJmsType:{},ttransactionid:{},ladderpresentflag:{},businessId:{},businessType:{}",
 				new String[] { userno, amtLong.toString(), actionJmsType + "", ttransactionid, ladderpresentflag + "",
 						businessId, businessType + "" });
 		if (userno == null || amtLong == null || actionJmsType == null) {
 			return;
 		}
 		BigDecimal amt = new BigDecimal(amtLong);
 		try {
 			if (actionJmsType == ActionJmsType.CHONGZHI_SUCCESS.value) {
 				/** 充值可提现功能 */
 				save2cashTransaction(ttransactionid, userno, amt);
 			}
 		} catch (Exception e) {
 			logger.error("充值可提现功能异常  userno:" + userno + ",amt:" + amtLong + ",actionJmsType:" + actionJmsType, e);
 		}
 
 		Tuserinfo tuserinfo = lotteryService.findTuserinfoByUserno(userno);
 		if (tuserinfo == null) {
 			return;
 		}
 		if (tuserinfo.getChannel() != null && tuserinfo.getChannel().equals("991")) {
 			logger.info("如意彩大户渠道不参加活动userno:" + userno);
 			return;
 		}
 		try {
 			if (actionJmsType == ActionJmsType.CHONGZHI_SUCCESS.value) {
 				/** 充值满百送5%活动 */
 				chongzhiManBaiZengSong(ttransactionid, ladderpresentflag, tuserinfo, amt);
 				/** 第一次充值活动 */
 				firshChongzhiZengSong(ttransactionid, tuserinfo, amt);
 				/** 2013.5.1老用户充值赠送 */
 				oldUserZengSong(ttransactionid, tuserinfo, amt);
 			}
 			if (actionJmsType == ActionJmsType.GOUCAI_SUCCESS.value) {
 				logger.info("购彩成功事件");
 				/** vip 购彩 活动 */
 				vipCase(tuserinfo, amt, businessId, businessType);
 			}
 		} catch (Exception e) {
 			logger.error("活动异常  userno:" + userno + ",amt:" + amtLong + ",actionJmsType:" + actionJmsType, e);
 		}
 	}
 
 	@Transactional
 	private Boolean save2cashTransaction(String ttransactionid, String userno, BigDecimal amt) {
 		logger.info("保存充值等待可提现ttransactionid:{},userno:{},amt:{}", new String[] { ttransactionid, userno, amt + "" });
 		Boolean flag = false;
 		Tactivity tactivity = Tactivity.findTactivity(null, null, "00092493", null, ActionJmsType.Fund2Draw.value);
 		if (tactivity != null) {
 			Fund2Draw draw = fund2DrawDao.createFund2Draw(ttransactionid, userno, amt);
 			if (draw != null) {
 				flag = true;
 			}
 		} else {
 			logger.info("未开启");
 		}
 		return flag;
 	}
 
 	@Transactional
 	public Boolean oldUserZengSong(String ttransactionid, Tuserinfo tuserinfo, BigDecimal amt) {
 		logger.info("老用户充值赠送");
 		Boolean flag = false;
 		if (tuserinfo == null) {
 			return flag;
 		}
 		Tactivity tactivity = Tactivity.findTactivity(null, null, tuserinfo.getSubChannel(), null,
 				ActionJmsType.OLD_USER_CHONGZHI_ZENGSONG.value);
 		if (tactivity == null) {
 			logger.info("老用户充值赠送活动未开启");
 			return flag;
 		}
 		OldUserChongZhi oucz = OldUserChongZhi.findOldUserChongZhi(tuserinfo.getUserno());
 		if (oucz != null) {
 			logger.info("活动已参加userno:" + tuserinfo.getUserno());
 			return flag;
 		}
 		String express = tactivity.getExpress();
 		Map<String, Object> activity = JsonUtil.transferJson2Map(express);
 		Integer minChargeAmt = (Integer) activity.get("minChargeAmt");
 		Integer percent = (Integer) activity.get("percent");
 		String dateStr = (String) activity.get("beforedate");
 		Integer maxamt = (Integer) activity.get("maxamt");
 		if (amt.compareTo(new BigDecimal(minChargeAmt)) < 0) {
 			logger.info("用户{}充值金额{}不满足活动最小充值金额{}", new String[] { tuserinfo.getUserno(), amt + "", minChargeAmt + "" });
 			return flag;
 		}
 		Date date = DateUtil.parse(dateStr);
 		Date regtime = tuserinfo.getRegtime();
 		if (date.compareTo(regtime) > 0) {
 			BigDecimal prizeamt = amt.multiply(new BigDecimal(percent)).divideToIntegralValue(new BigDecimal(100));
 			if (prizeamt.compareTo(new BigDecimal(maxamt)) > 0) {
 				prizeamt = new BigDecimal(maxamt);
 			}
 			logger.info("老用户充值赠送,userno:{},amt:{},prizeamt:{}", new String[] { tuserinfo.getUserno(), amt + "",
 					prizeamt + "" });
 			OldUserChongZhi.createOldUserChongZhi(tuserinfo.getUserno(), amt, prizeamt);
 			sendPrize2UserJMS(tuserinfo.getUserno(), prizeamt, ActionJmsType.OLD_USER_CHONGZHI_ZENGSONG,
 					ttransactionid, ttransactionid, tactivity.getMemo());
 			flag = true;
 		} else {
 			logger.info("用户注册时间" + regtime + "不在" + dateStr + "之前");
 		}
 		return flag;
 	}
 
 	@Transactional
 	public Boolean chongzhiManBaiZengSong(String ttransactionid, Long ladderpresentflag, Tuserinfo tuserinfo,
 			BigDecimal amt) {
 		logger.info("充值满百开始");
 		Boolean flag = false;
 		if (tuserinfo != null) {
 			Tactivity tactivity = Tactivity.findTactivity(null, null, tuserinfo.getSubChannel(), null,
 					ActionJmsType.CHONGZHI_100_ZENGSONG.value);
 			if (tactivity != null) {
 				// if (ladderpresentflag != null && 1 == ladderpresentflag) {
 				String express = tactivity.getExpress();
 				Map<String, Object> activity = JsonUtil.transferJson2Map(express);
 				Integer step = (Integer) activity.get("step");
 				Integer present = (Integer) activity.get("present");
 				if (amt.compareTo(new BigDecimal(step)) >= 0) {
 					flag = true;
 					BigDecimal prizeamt = amt.multiply(new BigDecimal(present)).divideToIntegralValue(
 							new BigDecimal(100));
 					logger.info("充值满百赠送,userno:{},amt:{},prizeamt:{}", new String[] { tuserinfo.getUserno(), amt + "",
 							prizeamt + "" });
 					sendPrize2UserJMS(tuserinfo.getUserno(), prizeamt, ActionJmsType.CHONGZHI_100_ZENGSONG,
 							ttransactionid, ttransactionid, tactivity.getMemo());
 				} else {
 					logger.info("不满足充值满百赠送条件,amt:" + amt + ",userno:" + tuserinfo.getUserno());
 				}
 			} else {
 				logger.info("充值满百赠送活动未开启");
 			}
 		}
 		logger.info("充值满百结束");
 		return flag;
 	}
 
 	@Transactional
 	public Boolean firshChongzhiZengSong(String ttransactionid, Tuserinfo tuserinfo, BigDecimal amt) {
 		logger.info("第一次充值开始");
 		Boolean flag = false;
 		if (tuserinfo.getSubChannel().equals("984") || tuserinfo.getSubChannel().equals("985")) {
 			logger.info("U付如意彩用户不参与活动,user:" + tuserinfo.getUserno());
 			return flag;
 		}
 		if (tuserinfo != null) {
 			Tactivity suningtactivity = Tactivity.findTactivity(null, null, tuserinfo.getSubChannel(),
 					tuserinfo.getChannel(), ActionJmsType.SUNING_ZENGSONG.value);
 			if (suningtactivity != null) {
 				String express = suningtactivity.getExpress();
 				Map<String, Object> activity = JsonUtil.transferJson2Map(express);
 				Date regtime = tuserinfo.getRegtime();
 				String regtimeStr = DateUtil.format("yyyyMMdd", regtime);
 				String todayStr = DateUtil.format("yyyyMMdd", new Date());
 				Integer step = (Integer) activity.get("step");
 				Integer prizeamt = (Integer) activity.get("prizeamt");
 				if (amt.compareTo(new BigDecimal(step)) >= 0 && regtimeStr.equals(todayStr)) {
 					Integer count = lotteryService.findTtransaction(tuserinfo.getUserno());
 					if (count == 1) {
 						if (StringUtils.isNotBlank(tuserinfo.getMobileid())) {
 							Chong20Mobile chong20Mobile = Chong20Mobile.findChong20Mobile(tuserinfo.getMobileid());
 							if (chong20Mobile == null) {
 								logger.info("苏宁渠道第一次充值赠送,userno:{},amt:{},prizeamt:{}",
 										new String[] { tuserinfo.getUserno(), amt + "", prizeamt + "" });
 								Chong20Mobile.createChong20Mobile(tuserinfo.getMobileid(), tuserinfo.getUserno());
 								sendPrize2UserJMS(tuserinfo.getUserno(), new BigDecimal(prizeamt),
 										ActionJmsType.SUNING_ZENGSONG, ttransactionid, ttransactionid,
 										suningtactivity.getMemo());
 								flag = true;
 
 							} else {
 								logger.info("苏宁渠道第一次充值活动,用户手机号已赠送过.mobileid:{}amt:{},user:{}",
 										new String[] { tuserinfo.getMobileid(), amt + "", tuserinfo.toString() });
 							}
 						} else {
 							logger.info("苏宁渠道第一次充值活动,用户信息未完善.amt:{},user:{}",
 									new String[] { amt + "", tuserinfo.toString() });
 							FirstChargeUser fcu = FirstChargeUser.findFirstChargeUser(tuserinfo.getUserno());
 							if (fcu == null) {
 								logger.info("创建FirstChargeUser等待用户完善信息userno:" + tuserinfo.getUserno()
 										+ ",ttransactionid:" + ttransactionid);
 								FirstChargeUser.createFirstChargeUser(tuserinfo.getUserno(), 0, ttransactionid);
 							} else {
 								logger.info("等待用户完善信息已创建userno:" + tuserinfo.getUserno() + ",ttransactionid:"
 										+ ttransactionid);
 							}
 						}
 					} else {
 						logger.info("苏宁渠道不是第一次充值，不参加活动.userno:{},amt:{},count:{}", new String[] {
 								tuserinfo.getUserno(), amt + "", count + "" });
 					}
 				} else {
 					logger.info("苏宁渠道第一次充值，不满足条件.userno:{},amt:{},sep:{},regtime,{}",
 							new String[] { tuserinfo.getUserno(), amt + "", step + "", regtimeStr });
 				}
 			} else {
 				logger.info("苏宁渠道首次充值赠送活动未开启");
 			}
 			if (suningtactivity == null) {
 				Tactivity tactivity = Tactivity.findTactivity(null, null, tuserinfo.getSubChannel(), null,
 						ActionJmsType.FIRST_CHONGZHI_ZENGSONG.value);
 				if (tactivity != null) {
 					String express = tactivity.getExpress();
 					Map<String, Object> activity = JsonUtil.transferJson2Map(express);
 					Date regtime = tuserinfo.getRegtime();
 					String regtimeStr = DateUtil.format("yyyyMMdd", regtime);
 					String todayStr = DateUtil.format("yyyyMMdd", new Date());
 					Integer step = (Integer) activity.get("step");
 					Integer prizeamt = (Integer) activity.get("prizeamt");
 					if (amt.compareTo(new BigDecimal(step)) >= 0 && regtimeStr.equals(todayStr)) {
 						Integer count = lotteryService.findTtransaction(tuserinfo.getUserno());
						if (count == 1 || count == null) {
 							if (StringUtils.isNotBlank(tuserinfo.getMobileid())) {
 								Chong20Mobile chong20Mobile = Chong20Mobile.findChong20Mobile(tuserinfo.getMobileid());
 								if (chong20Mobile == null) {
 									logger.info("第一次充值赠送,userno:{},amt:{},prizeamt:{}",
 											new String[] { tuserinfo.getUserno(), amt + "", prizeamt + "" });
 									Chong20Mobile.createChong20Mobile(tuserinfo.getMobileid(), tuserinfo.getUserno());
 									sendPrize2UserJMS(tuserinfo.getUserno(), new BigDecimal(prizeamt),
 											ActionJmsType.FIRST_CHONGZHI_ZENGSONG, ttransactionid, ttransactionid,
 											tactivity.getMemo());
 									flag = true;
 								} else {
 									logger.info("第一次充值活动,用户手机号已赠送过.mobileid:{}amt:{},user:{}",
 											new String[] { tuserinfo.getMobileid(), amt + "", tuserinfo.toString() });
 								}
 							} else {
 								logger.info("第一次充值活动,用户信息未完善.amt:{},user:{}",
 										new String[] { amt + "", tuserinfo.toString() });
 								FirstChargeUser fcu = FirstChargeUser.findFirstChargeUser(tuserinfo.getUserno());
 								if (fcu == null) {
 									logger.info("创建FirstChargeUser等待用户完善信息userno:" + tuserinfo.getUserno()
 											+ ",ttransactionid:" + ttransactionid);
 									FirstChargeUser.createFirstChargeUser(tuserinfo.getUserno(), 0, ttransactionid);
 								} else {
 									logger.info("等待用户完善信息已创建userno:" + tuserinfo.getUserno() + ",ttransactionid:"
 											+ ttransactionid);
 								}
 							}
 						} else {
 							logger.info("不是第一次充值，不参加活动.userno:{},amt:{},count:{}", new String[] {
 									tuserinfo.getUserno(), amt + "", count + "" });
 						}
 					} else {
 						logger.info("第一次充值，不满足条件.userno:{},amt:{},sep:{},regtime,{}",
 								new String[] { tuserinfo.getUserno(), amt + "", step + "", regtimeStr });
 					}
 				} else {
 					logger.info("首次充值赠送活动未开启");
 				}
 			}
 		}
 		logger.info("第一次充值结束");
 		return flag;
 	}
 
 	@Transactional
 	public Boolean buyCase(String userno, BigDecimal amt) {
 		logger.info("91购彩活动开始");
 		Boolean flag = false;
 		Tuserinfo tuserinfo = lotteryService.findTuserinfoByUserno(userno);
 		if (tuserinfo != null) {
 			Tactivity tactivity = Tactivity.findTactivity(null, null, tuserinfo.getSubChannel(),
 					tuserinfo.getChannel(), ActionJmsType.GOUCAI_SUCCESS.value);
 			if (tactivity != null) {
 				String express = tactivity.getExpress();
 				Map<String, Object> activity = JsonUtil.transferJson2Map(express);
 				Integer totalamt = (Integer) activity.get("totalamt");
 				Integer prizeamt = (Integer) activity.get("prizeamt");
 				String time = (String) activity.get("effectiveTime");
 				Date effectiveTime = DateUtil.parse(time);
 				if (totalamt != null && prizeamt != null && effectiveTime != null) {
 					if (tuserinfo.getRegtime() == null) {
 						return flag;
 					}
 					if (tuserinfo.getRegtime().compareTo(effectiveTime) >= 0) {
 						Tuseraction tuseraction = Tuseraction.createIfNotExists(tuserinfo.getUserno(), amt);
 						if (tuseraction.getTotalBuyAmt().compareTo(new BigDecimal(totalamt)) >= 0) {
 							if (Tjmsservice.createTjmsservice(tuserinfo.getUserno(), ActionJmsType.GOUCAI_SUCCESS)) {
 								flag = true;
 								sendPrize2UserJMS(tuseraction.getUserno(), new BigDecimal(prizeamt),
 										ActionJmsType.GOUCAI_SUCCESS, null, null, tactivity.getMemo());
 							}
 						}
 					}
 				}
 
 			}
 		}
 		logger.info("91购彩活动结束");
 		return flag;
 	}
 
 	@Transactional
 	public Boolean vipCase(Tuserinfo tuserinfo, BigDecimal amt, String businessId, Integer businessType) {
 		Boolean flag = false;
 		logger.info("VIP购彩活动开始");
 		if (!tuserinfo.getSubChannel().equals("00092493")) {
 			return flag;
 		}
 		String currentMonth = DateUtil.format("yyyy-MM", new Date());
 		if (tuserinfo != null) {
 			VipUser vipUser = vipUserDao.findVipUser(tuserinfo.getUserno(), currentMonth, true);
 			if (vipUser == null) {
 				vipUser = vipUserDao.createVipUser(tuserinfo.getUserno(), currentMonth);
 			}
 			if (vipUser != null) {
 				vipUser.setBuyamt(vipUser.getBuyamt() == null ? amt : vipUser.getBuyamt().add(amt));
 				vipUser.setModifyTime(new Date());
 				vipUserDao.merge(vipUser);
 				logger.info("大客户userno:{},增加购买金额{}", new String[] { tuserinfo.getUserno(), amt + "" });
 			}
 		}
 		if (tuserinfo != null) {
 			Tactivity tactivity = Tactivity.findTactivity(null, null, tuserinfo.getSubChannel(), null,
 					ActionJmsType.VIP_USER_GOUCAI_ZENGSONG.value);
 			if (tactivity != null) {
 				String express = tactivity.getExpress();
 				Map<String, Object> activity = JsonUtil.transferJson2Map(express);
 				Integer step = (Integer) activity.get("step");
 				Integer present = (Integer) activity.get("present");
 				Calendar calendar = Calendar.getInstance();
 
 				calendar.add(Calendar.MONTH, -1);
 				String lastMonth = DateUtil.format("yyyy-MM", calendar.getTime());
 				VipUser lastMonthVipUser = vipUserDao.findVipUser(tuserinfo.getUserno(), lastMonth, false);
 				if (lastMonthVipUser != null && lastMonthVipUser.getBuyamt().compareTo(new BigDecimal(step)) >= 0) {
 					if (amt.compareTo(BigDecimal.ZERO) >= 0) {
 						flag = true;
 						BigDecimal prizeamt = amt.multiply(new BigDecimal(present)).divideToIntegralValue(
 								new BigDecimal(100));
 						logger.info("大客户userno:{},时间:{},总购彩金额:{},本次购彩:{},赠送金额:{}", new String[] {
 								lastMonthVipUser.getId().getUserno(), lastMonthVipUser.getId().getYearAndMonth(),
 								lastMonthVipUser.getBuyamt() + "", amt + "", prizeamt + "" });
 						sendPrize2UserJMS(tuserinfo.getUserno(), prizeamt, ActionJmsType.VIP_USER_GOUCAI_ZENGSONG,
 								null, businessId, tactivity.getMemo());
 					}
 				} else {
 					logger.info("不满大户购彩赠送条件userno:" + tuserinfo.getUserno() + ",vipUser:"
 							+ (lastMonthVipUser == null ? "" : lastMonthVipUser));
 				}
 			}
 		}
 		logger.info("VIP购彩活动结束");
 		return flag;
 	}
 
 	@Transactional
 	public Boolean chargeCase(Tagent tagent, BigDecimal amt) {
 		Boolean flag = false;
 		Tuserinfo tuserinfo = lotteryService.findTuserinfoByUserno(tagent.getUserno());
 		Tactivity tactivity = Tactivity.findTactivity(null, null, tuserinfo.getSubChannel(), null,
 				ActionJmsType.CHONGZHI_SUCCESS.value);
 		if (tactivity != null) {
 			Tagent parentAgent = Tagent.findTagent(tagent.getAgentId());
 			String express = tactivity.getExpress();
 			Map<String, Object> activity = JsonUtil.transferJson2Map(express);
 			Integer borderamt = (Integer) activity.get("borderamt");
 			Integer ltPrizeRate = (Integer) activity.get("ltPrizeRate");
 			Integer gtPrizeRate = (Integer) activity.get("gtPrizeRate");
 			BigDecimal prize = BigDecimal.ZERO;
 			if (amt.compareTo(new BigDecimal(borderamt)) < 0) {
 				prize = amt.multiply(new BigDecimal(ltPrizeRate)).divide(new BigDecimal(100));
 			} else {
 				prize = amt.multiply(new BigDecimal(gtPrizeRate)).divide(new BigDecimal(100));
 			}
 			logger.info("下线userno:{}充值{}，赠送上线userno:{}彩金{}",
 					new String[] { tagent.getUserno(), amt + "", parentAgent.getUserno(), prize.toString() });
 			flag = true;
 			sendPrize2UserJMS(parentAgent.getUserno(), prize, ActionJmsType.CHONGZHI_SUCCESS, null, null,
 					tactivity.getMemo());
 		}
 		return flag;
 	}
 
 	@Transactional
 	public void sendPrize2UserJMS(String userno, BigDecimal amt, ActionJmsType actionJmsType, String ttransactionid,
 			String businessId, String memo) {
 		TuserPrizeDetail userPrizeDetail = tuserPrizeDetailDao.createTprizeUserBuyLog(userno, amt, actionJmsType,
 				businessId);
 		Map<String, Object> headers = new HashMap<String, Object>();
 		headers.put("prizeDetailId", userPrizeDetail.getId());
 		headers.put("actionJmsType", actionJmsType.value);
 		headers.put("ttransactionid", ttransactionid);
 		headers.put("memo", memo);
 		sendActivityPrizeProducer.sendBodyAndHeaders(null, headers);
 	}
 
 	/**
 	 * 用户注册时注册代理用户
 	 * 
 	 * @param userno
 	 *            注册用户编号
 	 * @param mobileid
 	 *            上级代理手机号
 	 * @return
 	 */
 	@Transactional
 	public void registerAgent(String userno, String mobileid) {
 		if (StringUtils.isBlank(userno) || StringUtils.isBlank(mobileid)) {
 			throw new IllegalArgumentException("the arguments userno or mobileid is required");
 		}
 		Tagent agent = Tagent.findTagentByUserno(userno);
 		if (agent != null && agent.getAgentId() != null) {
 			logger.info("注册代理失败。该用户已经被代理userno:" + userno);
 			throw new RuyicaiException("注册代理失败,该用户已经被代理.");
 		}
 		Tuserinfo parentAgentUserinfo = lotteryService.findTuserinfoByMobileid(mobileid);
 		if (parentAgentUserinfo == null) {
 			logger.info("注册代理失败。上级代理用户mobileid:" + mobileid + "没有在lottery中查询到");
 			throw new RuyicaiException("注册代理失败,没有找到该手机号.");
 		}
 		if (parentAgentUserinfo.getUserno().equals(userno)) {
 			logger.info("注册用户和代理用户相同");
 			throw new RuyicaiException("注册用户和代理用户相同");
 		}
 		Tagent parentAgent = Tagent.createIfNotExists(parentAgentUserinfo.getUserno(), null, null, null);
 		logger.info("创建上级代理用户" + parentAgent);
 		Tagent childAgent = Tagent.saveOrUpdate(userno, null, parentAgent.getId(), null);
 		logger.info("创建下级代理用户" + childAgent);
 	}
 
 	@Transactional
 	public String addNumOneYear(String body) {
 		if (!addNumOneYearSwitch.equals("1")) {
 			logger.info("活动已停止或未开启,直接追号一年,不参加活动");
 			String flowno = lotteryService.subscribeOrder(body);
 			return flowno;
 		} else {
 			logger.info("参加追号包年活动");
 			OrderRequest orderRequest = JsonUtil.fromJsonToObject(body, OrderRequest.class);
 			String buyuserno = orderRequest.getBuyuserno();
 			Tuserinfo tuserinfo = lotteryService.findTuserinfoByUserno(buyuserno);
 			if (tuserinfo == null) {
 				throw new RuyicaiException(ErrorCode.UserMod_UserNotExists);
 			}
 			if (StringUtils.isBlank(tuserinfo.getMobileid()) || StringUtils.isBlank(tuserinfo.getName())
 					|| StringUtils.isBlank(tuserinfo.getCertid()) || tuserinfo.getCertid().equals("111111111111111111")) {
 				throw new RuyicaiException(ErrorCode.UserMod_InformationNotEnough);
 			}
 			TaddNumActivity activity = TaddNumActivity.findTaddNumActivity(buyuserno);
 			if (activity != null) {
 				throw new RuyicaiException(ErrorCode.ActionCenter_HadJoin);
 			}
 			TaddNumActivity taddNumActivity = TaddNumActivity.createTaddNumActivity(buyuserno, null);
 			String flowno = lotteryService.subscribeOrder(body);
 			taddNumActivity.updateTaddNumActivity(flowno);
 			return flowno;
 		}
 	}
 
 	/**
 	 * 领取赠送奖金
 	 * 
 	 * @param sendMoneyid
 	 *            赠送彩金ID
 	 * @return SendMoneyDetails
 	 */
 	@Transactional
 	public SendMoneyDetails reciveSendMoney(String sendMoneyid) {
 		logger.info("领取赠送sendMoneyid:{}", new String[] { sendMoneyid });
 		SendMoneyDetails details = SendMoneyDetails.findSendMoneyDetails(sendMoneyid, true);
 		if (details.getReciveState() == 1) {
 			throw new RuyicaiException(ErrorCode.HavereciveMoney);
 		}
 		String reciverUserno = details.getReciverUserno();
 		Tuserinfo reciveTuserinfo = lotteryService.findTuserinfoByUserno(reciverUserno);
 		lotteryService.directChargeProcess(reciveTuserinfo.getUserno(), details.getAmt(),
 				reciveTuserinfo.getSubChannel(), reciveTuserinfo.getChannel(), "领取红包" + details.getSendusername());
 
 		details.setReciveState(1);
 		details.setReciveTime(new Date());
 		details.merge();
 		logger.info("领取成功 presentId:{},reciverUserno:{}", new String[] { details.getId(), details.getReciverUserno() });
 		return details;
 	}
 }
