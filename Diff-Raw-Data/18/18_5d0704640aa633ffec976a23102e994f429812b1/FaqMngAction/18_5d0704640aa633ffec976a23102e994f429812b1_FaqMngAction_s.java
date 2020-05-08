 package com.zhaoyang.action;
 
 import java.util.List;
 
 import com.zhaoyang.dao.FaqDao;
 import com.zhaoyang.dao.RuleDao;
 import com.zhaoyang.data.Faq;
 
 public class FaqMngAction extends AbstractActionSupport {
 	private FaqDao faqDao = new FaqDao();
 
 	public FaqDao getFaqDao() {
 		return faqDao;
 	}
 
 	public void setFaqDao(FaqDao faqDao) {
 		this.faqDao = faqDao;
 	}
 
 	private List<Faq> faqs;
 	
 
 	public List<Faq> getFaqs() {
 		return faqs;
 	}
 
 	public void setFaqs(List<Faq> faqs) {
 		this.faqs = faqs;
 	}
 
 	@Override
 	public String execute() throws Exception {
 		// TODO Auto-generated method stub
 		faqs = faqDao.findAll();
 		return SUCCESS;
 	}
 	private Faq faq;
 	
 	public Faq getFaq() {
 		return faq;
 	}
 
 	public void setFaq(Faq faq) {
 		this.faq = faq;
 	}
 
 	public String faqEdit() throws Exception {
 		// TODO Auto-generated method stub
 		//faqs = faqDao.findAll();
 		if(faq.getAnswer()==null||faq.getAnswer().equals("")){
 			setErrMsg("答案不能为空");
 			return SUCCESS;
 		}
 		if(faq.getQuestion()==null||faq.getQuestion().equals("")){
 			setErrMsg("问题不能为空");
 			return SUCCESS;
 		}
 		faqDao.update(faq);
 		setSucMsg("修改成功");
 		return SUCCESS;
 	}
 	public String faqAdd() throws Exception {
 		// TODO Auto-generated method stub
 		//faqs = faqDao.findAll();
 		if(faq.getAnswer()==null||faq.getAnswer().equals("")){
 			setErrMsg("答案不能为空");
 			return SUCCESS;
 		}
 		if(faq.getQuestion()==null||faq.getQuestion().equals("")){
 			setErrMsg("问题不能为空");
 			return SUCCESS;
 		}
 		faqDao.save(faq);
 		setSucMsg("添加成功");
 		return SUCCESS;
 	}
 	private RuleDao ruleDao;
 	
 	public RuleDao getRuleDao() {
 		return ruleDao;
 	}
 
 	public void setRuleDao(RuleDao ruleDao) {
 		this.ruleDao = ruleDao;
 	}
 	private String indexFaqList;
 	
 	public String getIndexFaqList() {
 		return indexFaqList;
 	}
 
 	public void setIndexFaqList(String indexFaqList) {
 		this.indexFaqList = indexFaqList;
 	}
 
 	public String indexFaqListMng(){
 		indexFaqList=ruleDao.findRuleByRuleId("IndexFaqList").getRuleDef();
 		return SUCCESS;
 	}
 	
 	public String indexFaqListEdit(){
 		if(indexFaqList==null||indexFaqList.equals("")){
 			setErrMsg("列表不能为空");
 		}else{
 			ruleDao.update("IndexFaqList", indexFaqList);
 			setSucMsg("修改成功");
 		}
 		
 		return SUCCESS;
 	}
 	
 	
 
 }
