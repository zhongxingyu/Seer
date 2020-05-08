 package org.luckystars.weixin.framework.handlers;
 
 
 import java.io.Serializable;
 import java.util.ArrayList;
 
 import org.apache.log4j.Logger;
 import org.luckystars.weixin.framework.HandlerInvocation;
 import org.luckystars.weixin.framework.api.HandleResult;
 import org.luckystars.weixin.framework.api.Handler;
 import org.luckystars.weixin.framework.api.IncomeMessage;
 import org.luckystars.weixin.framework.api.WeixinView;
 import org.luckystars.weixin.transfer.incomemsg.WeixinMsg;
 import org.luckystars.weixin.transfer.view.TextView;
 import org.luckystars.weixin.transfer.view.annotations.AnnotationNewsViewBuilder;
 import org.luckystars.weixin.transfer.view.annotations.Description;
 import org.luckystars.weixin.transfer.view.annotations.Item;
 import org.luckystars.weixin.transfer.view.annotations.PicUrl;
 import org.luckystars.weixin.transfer.view.annotations.Title;
 import org.luckystars.weixin.transfer.view.annotations.Url;
 
 public class TestHandler implements Handler{
 
 	
 	private static final Logger logger = Logger.getLogger(TestHandler.class);
 	@Override
 	public HandleResult handle(HandlerInvocation invocation) throws Exception {
 		System.out.println("test");
 		IncomeMessage msg = invocation.getInvocationContext().getMsg();
 		
 		if(msg instanceof WeixinMsg){
 			final WeixinMsg m = (WeixinMsg)msg;
 			logger.info("\n\n" + m.getRawXml() + "\n\n");
 			String toUsr = m.getFromUserName();
 			String serverName = m.getToUserName();
 			
 			final WeixinView view = new TextView("这只是个测试\n你发送的数据是\n" 
 					+ m.toString(), toUsr, serverName);
 			
 			HandleResult result =  new HandleResult() {
 				public void setView(WeixinView view) {
 				}
 				public WeixinView getView() {
 					return new AnnotationNewsViewBuilder().build(new ArrayList<TestList>(){{
 						TestList t1 = new TestList();
						t1.picurl = "http://www.china-nbc.com/weixin/tit.png";
 						add(t1);
 						add(new TestList());
 //						add(new TestList());
 //						add(new TestList());
 					}}, m);
 				}
 			};
 			logger.info("resp is : \n");
 			logger.info(result.getView().toWeixinStr());
 			
 			return result;
 		}
 		
 		return invocation.invokeNext();
 	}
 	
 	@Item
 	private static class TestList implements Serializable {
 		@PicUrl
 		private String picurl="";
 		@Url
		private String url="www.china-nbc.com/weixin/1.html";
 		@Title
 		private String title = "title";
 		@Description
 		private String desc = "";
 	}
 	
 	public static void main(String[] args) {
 		
 	}
 }
