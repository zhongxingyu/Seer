 package com.nbcedu.function.schoolmaster2.biz;
 
 import java.util.List;
 
 import com.nbcedu.function.schoolmaster2.core.pager.PagerModel;
 import com.nbcedu.function.schoolmaster2.vo.MasterSubSearchVO;
 import com.nbcedu.function.schoolmaster2.vo.StepVo;
 
 public interface SM2MasterSubBiz extends SM2SubjectBiz {
 
 	/**
 	 * 按校长和模块类型分页
 	 * @param modId
 	 * @param masterUid
 	 * @return
 	 * @author xuechong
 	 */
 	public PagerModel findByMaster(String modId,String masterUid);
 	
 	/**
 	 * 按subjectId查找所有步骤
 	 * @param subId
 	 * @return
 	 * @author xuechong
 	 */
 	public List<StepVo> findAllSteps(String subId);
 	
 	/**
 	 * 按条件分页
 	 * @param vo
 	 * @param uid 接受人的uid
 	 * @return
 	 * @author xuechong
 	 */
	public PagerModel findBySearchVo(MasterSubSearchVO vo,String uid);
 }
