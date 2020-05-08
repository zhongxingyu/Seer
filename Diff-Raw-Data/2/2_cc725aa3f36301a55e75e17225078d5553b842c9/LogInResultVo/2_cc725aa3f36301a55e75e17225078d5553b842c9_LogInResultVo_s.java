 package locationshare.vo;
 
 import locationshare.base.vo.BaseResultVO;
 
 public class LogInResultVo extends BaseResultVO {
 
 	public String toSuccessJsonResult(int userid) {
		return "{\"code\"=\"0\"" + " \"userid\"=\"" + String.valueOf(userid)
 				+ "\"}";
 	}
 }
