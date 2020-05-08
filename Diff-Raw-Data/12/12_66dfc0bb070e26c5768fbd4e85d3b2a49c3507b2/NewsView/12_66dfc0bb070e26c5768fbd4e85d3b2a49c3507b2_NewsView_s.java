 package org.luckystars.weixin.transfer.view;
 
 import java.io.Serializable;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 
 import org.luckystars.weixin.transfer.interfaces.XmlReply;
 
 
 /*******
  * 
 <xml>
 <ToUserName><![CDATA[toUser]]></ToUserName>
 <FromUserName><![CDATA[fromUser]]></FromUserName>
 <CreateTime>12345678</CreateTime>
 <MsgType><![CDATA[news]]></MsgType>
 <ArticleCount>2</ArticleCount>
 <Articles>
 <item>
 <Title><![CDATA[title1]]></Title> 
 <Description><![CDATA[description1]]></Description>
 <PicUrl><![CDATA[picurl]]></PicUrl>
 <Url><![CDATA[url]]></Url>
 </item>
 <item>
 <Title><![CDATA[title]]></Title>
 <Description><![CDATA[description]]></Description>
 <PicUrl><![CDATA[picurl]]></PicUrl>
 <Url><![CDATA[url]]></Url>
 </item>
 </Articles>
 </xml> 
 参数	                          是否必须	                  说明<br>
 ToUserName	             是	              接收方帐号（收到的OpenID）<br>
 FromUserName	  是	               开发者微信号<br>
 CreateTime	            是	              消息创建时间 （整型）<br>
 MsgType	                      是	       news<br>
 ArticleCount	 是	            图文消息个数，限制为10条以内<br>
 Articles	            是	                多条图文消息信息，默认第一个item为大图,注意，如果图文数超过10，则将会无响应<br>
 Title	                       否	            图文消息标题<br>
 Description	            否	           图文消息描述<br>
 PicUrl	                       否	             图片链接，支持JPG、PNG格式，较好的效果为大图360*200，小图200*200<br>
 Url	                                 否	              点击图文消息跳转链接<br>
  * @author xuechong;
  */
 @SuppressWarnings("serial")
 public class NewsView implements XmlReply{
 
 	private String fromUserName;
 	private String toUserName;
 	private String createTime;
 	private String msgType = "news";
 	/**图文消息个数，限制为10条以内***/
 	private String articleCount;
 	
 	private List<NewsView.Item> items = new ArrayList<NewsView.Item>(10);
 	
 	@Override
 	public String toWeixinStr() {
 		return toXmlString();
 	}
 
 	@Override
 	public String toXmlString() {
 		StringBuilder result = new StringBuilder();
 		result.append("<xml>");
 		
 		result.append("<ToUserName><![CDATA[");
 		result.append(this.getToUserName());
 		result.append("]]></ToUserName>");
 		
 		result.append("<FromUserName><![CDATA[");
 		result.append(this.getFromUserName());
 		result.append("]]></FromUserName>");
 		
 		result.append("<CreateTime>");
 		result.append(new Date().getTime());
 		result.append("</CreateTime>");
 		
 		result.append("<MsgType><![CDATA[news]]></MsgType>");
 		
 		result.append("<ArticleCount>");
 		result.append(this.getArticleCount());
 		result.append("</ArticleCount>");
 		
 		result.append("<Articles>");
 		result.append(buildItemsXml());
 		result.append("</Articles>");
 		
 		result.append("</xml>");
 		return result.toString();
 	}
 	
 	/***
 		<Title><![CDATA[title]]></Title>
 		<Description><![CDATA[description]]></Description>
 		<PicUrl><![CDATA[picurl]]></PicUrl>
 		<Url><![CDATA[url]]></Url>
 	 * @return
 	 * @author xuechong
 	 */
 	private String buildItemsXml() {
 		if(this.items==null||this.items.size()<=0){throw new NullPointerException("没有数据");}
 		StringBuilder result = new StringBuilder();
 
 		for (Item item : this.items) {
 			result.append("<item>");
 			
 			result.append("<Title><![CDATA[");
 			result.append(item.getTitle());
 			result.append("]]></Title>");
 			
 			result.append("<Description><![CDATA[");
 			result.append(item.getDescription());
 			result.append("]]></Description>");
 			
 			result.append("<PicUrl><![CDATA[");
 			result.append(item.getPicUrl());
 			result.append("]]></PicUrl>");
 			
			result.append("<Url><![");
 			result.append(item.getUrl());
 			result.append("]]></Url>");
 			
 			result.append("</item>");
 		}
 		
 		return result.toString();
 	}
 
 
 	public static class Item implements Serializable{
 		private String title="";
 		private String description="";
 		private String picUrl="";
 		private String url="";
 		
 		public String getTitle() {
 			return title;
 		}
 		public void setTitle(String title) {
 			this.title = title;
 		}
 		public String getDescription() {
 			return description;
 		}
 		public void setDescription(String description) {
 			this.description = description;
 		}
 		public String getPicUrl() {
 			return picUrl;
 		}
 		public void setPicUrl(String picUrl) {
 			this.picUrl = picUrl;
 		}
 		public String getUrl() {
 			return url;
 		}
 		public void setUrl(String url) {
 			this.url = url;
 		}
 	}
 	///////////////////////////
 	//////GETTERS&SETTERS//////
 	////////////////////////////
 	public String getFromUserName() {
 		return fromUserName;
 	}
 	public void setFromUserName(String fromUserName) {
 		this.fromUserName = fromUserName;
 	}
 	public String getToUserName() {
 		return toUserName;
 	}
 	public void setToUserName(String toUserName) {
 		this.toUserName = toUserName;
 	}
 	public String getCreateTime() {
 		return createTime;
 	}
 	public void setCreateTime(String createTime) {
 		this.createTime = createTime;
 	}
 	public String getMsgType() {
 		return msgType;
 	}
 	public void setMsgType(String msgType) {
 		this.msgType = msgType;
 	}
 	public List<NewsView.Item> getItems() {
 		return items;
 	}
 	public void setItems(List<NewsView.Item> items) {
 		this.items = items;
 	}
 	public String getArticleCount() {
 		return String.valueOf(items.size());
 	}
 }
 
 
