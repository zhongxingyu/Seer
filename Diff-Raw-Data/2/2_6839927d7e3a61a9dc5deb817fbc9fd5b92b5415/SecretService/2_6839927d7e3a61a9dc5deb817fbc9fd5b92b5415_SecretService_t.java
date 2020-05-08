 package com.miracle.secretra;
 
 import info.xmark.core.Service;
 
 import java.io.IOException;
 import java.io.UnsupportedEncodingException;
 import java.security.NoSuchAlgorithmException;
 
 import javax.servlet.http.HttpServletRequest;
 
 import org.apache.log4j.Logger;
 import org.dom4j.Document;
 import org.dom4j.DocumentHelper;
 import org.dom4j.Element;
 
 import com.miracle.tool.DownPic;
 
 public class SecretService {
 
 	private static Logger log = Logger.getLogger(SecretService.class);
 
 	public static String reply(HttpServletRequest request) {
 		String ret = "";
 		// 1、分析用户请求
 		String postStr = null;
 		try {
 			postStr = Service.readPostXML(request);
 		} catch (Exception e) {
 			e.printStackTrace();
 		}
 		log.info("接收到： " + postStr);
 
 		if (null != postStr && !postStr.isEmpty()) {
 			Document document = null;
 			try {
 				document = DocumentHelper.parseText(postStr);
 			} catch (Exception e) {
 				e.printStackTrace();
 			}
 			if (null == document || document.equals("")) {
 				log.info(" document is empty !!!");
 				return "";
 			}
 
 			Element root = document.getRootElement();
 			String fromUsername = root.elementText("FromUserName");
 			String toUsername = root.elementText("ToUserName");
 			String msgType = root.elementText("MsgType");
 			String time = root.elementText("CreateTime");
 			if (msgType != null) {
 				if (msgType.equals("image")) {
 					String picUrl = root.elementText("PicUrl");
 
 					String md5 = "";
 					try {
 						md5 = DownPic.calcMD5(picUrl);
 					} catch (NoSuchAlgorithmException e) {
 						e.printStackTrace();
 					} catch (IOException e) {
 						e.printStackTrace();
 					}
 
 					if (DBDog.checkmd5(md5)) {
 						ret = BirdSing.singAsong("这个图片秘密已经上传过了，请不要重复上传，谢谢~", fromUsername, toUsername, time);
 					} else {
 						// 用户的秘密，需要回复给他一个秘密,保存秘密
 						// 保存
 						DBDog.saveSecret(fromUsername, picUrl, md5);
 						// 回复一个秘密
 						ret = BirdSing.tellSecret(fromUsername, toUsername, time);
 					}
 
 				} else if (msgType.equals("text")) {
 
 					String content = root.elementText("Content").trim();
 					if (content.startsWith("WD")) {
 						// 查看我的秘密
 						ret = BirdSing.showSecret(fromUsername, toUsername, time);
 					} else if (content.startsWith("CK") || content.startsWith("ck") || content.startsWith("Ck") || content.startsWith("cK")) {
 						// 查看我的留言
 						ret = BirdSing.showLiuyan(fromUsername, toUsername, time);
 
 					} else if (content.startsWith("LY") || content.startsWith("ly") || content.startsWith("Ly") || content.startsWith("lY")) {
 						// 留言
 						if (content.substring(2).equals("")) {
 							ret = BirdSing.singAsong("请加上你的留言，留言请以'LY'开头，谢谢", fromUsername, toUsername, time);
 						} else {
 							DBDog.saveLiuyan(fromUsername, content.substring(2));
 							ret = BirdSing.singAsong("留言成功,你可以输入CK来查看所有人的留言.", fromUsername, toUsername, time);
 						}
 					} else if (content.startsWith("MM") || content.startsWith("mm") || content.startsWith("Mm") || content.startsWith("mM")) {
 						// 文字秘密
 						if (content.substring(2).equals("")) {
							ret = BirdSing.singAsong("请加上你的秘密，秘密以'MM'开头，谢谢", fromUsername, toUsername, time);
 						} else {
 							DBDog.saveWZSecret(fromUsername, content.substring(2));
 							ret = BirdSing.singAsong("作为交换，告诉你这个秘密：" + BirdSing.getRandomWZMM(fromUsername), fromUsername, toUsername, time);
 						}
 					} else if (content.startsWith("PL") || content.startsWith("pl") || content.startsWith("pL") || content.startsWith("Pl")) {
 						// 评论
 						DBDog.saveLiuyan(fromUsername, content.substring(2));
 						ret = BirdSing.singAsong("pl成功,你可以输入CP来查看自己的评论.", fromUsername, toUsername, time);
 						// } else if (content.startsWith("EM") ||
 						// content.startsWith("em") || content.startsWith("Em")
 						// || content.startsWith("eM")) {
 						// // email
 						// SendMail.send("从交换秘密发来的邮件", content, new String[] {
 						// "liuhongyuan99@qq.com" });
 						// ret = BirdSing.singAsong("email发送完成~，去自己的邮箱查看吧",
 						// fromUsername, toUsername, time);
 						//
 						// } else if (content.startsWith("wb") ||
 						// content.startsWith("WB")) {
 						// // 回复
 						// log.info("weibotoken:-- " + WeiboTool.token);
 						// log.info(WeiboTool.sendweibo(content,
 						// WeiboTool.token));
 						// ret =
 						// BirdSing.singAsong("微博发送成功~到这里http://weibo.com/secretra查看~",
 						// fromUsername, toUsername, time);
 					} else {
 						// 非命令，提醒用户使用方法
 						ret = BirdSing.singAsong("文字的秘密请以'MM'开头，来交换别人的秘密；如要发留言板,请输入以'LY'开头的内容。谢谢", fromUsername, toUsername, time);
 					}
 				} else if (msgType.equals("event")) {
 					// 新用户订阅，提醒用户使用方法
 					ret = BirdSing.singAsong(fromUsername, toUsername, time);
 				}
 			}
 		}
 		log.info("回复内容： " + ret);
 		try {
 			ret = new String(ret.getBytes("UTF-8"), "iso-8859-1");
 		} catch (UnsupportedEncodingException e) {
 			e.printStackTrace();
 		}
 		log.info("回复内容 iso-8859-1 ： " + ret);
 		return ret;
 	}
 }
