 package net.chat.service.impl;
 
 import java.io.File;
 import java.io.IOException;
 import java.io.PrintWriter;
 import java.io.StringReader;
 import java.io.Writer;
 import java.util.Date;
 import java.util.List;
 import java.util.Scanner;
 import java.util.logging.Logger;
 
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 
 import net.chat.dao.WxAccountDao;
 import net.chat.dao.WxCmdDao;
 import net.chat.dao.WxContentDao;
 import net.chat.dao.WxGameDao;
 import net.chat.dao.WxLbsDao;
 import net.chat.dao.WxMessageDao;
 import net.chat.dao.WxMsgTypeDao;
 import net.chat.domain.WxCmd;
 import net.chat.integration.vo.CacheContant;
 import net.chat.integration.vo.WeChatReqBean;
 import net.chat.integration.vo.WeChatRespTextBean;
 import net.chat.integration.vo.WeiChatRespImageBean;
 import net.chat.integration.vo.WeiChatRespMusicAndVideoBean;
 import net.chat.service.IntegrationWeiChat;
 import net.chat.utils.BaiduAPI;
 
 import org.apache.commons.lang3.StringUtils;
 import org.springframework.beans.factory.InitializingBean;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Service;
 
 import com.sun.xml.bind.marshaller.CharacterEscapeHandler;
 
 @Service("integrationService")
 public class IntegrationWeiChatImpl implements IntegrationWeiChat,
 		InitializingBean {
 
 	@Autowired
 	private WxAccountDao accountDao;
 
 	@Autowired
 	private WxMsgTypeDao messageTypeDao;
 
 	@Autowired
 	private WxCmdDao wxCmdDao;
 
 	@Autowired
 	private WxGameDao gameDao;
 
 	@Autowired
 	private WxMessageDao messageDao;
 
 	@Autowired
 	private WxContentDao contentDao;
 
 	@Autowired
 	private WxLbsDao lbsDao;
 
 	private static Logger log = Logger.getLogger(IntegrationWeiChatImpl.class
 			.getName());
 
 	public void doGet(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		String signature = req.getParameter("signature");
 		String timestamp = req.getParameter("timestamp");
 		String nonce = req.getParameter("nonce");
 		String echostr = req.getParameter("echostr");
 		log.info(signature + " : " + timestamp + " : " + nonce + " : "
 				+ echostr);
 		PrintWriter out = resp.getWriter();
 		out.write(echostr);
 	}
 
 	public void doPost(HttpServletRequest req, HttpServletResponse resp)
 			throws ServletException, IOException {
 		Scanner scanner = new Scanner(req.getInputStream());
 		resp.setContentType("application/xml");
 		resp.setCharacterEncoding("UTF-8");
 		PrintWriter out = resp.getWriter();
 		try {
 			// 1、获取用户发送的信息
 			StringBuffer sb = new StringBuffer(100);
 			while (scanner.hasNextLine()) {
 				sb.append(scanner.nextLine());
 			}
 
 			// 2、解析用户的信息
 			JAXBContext jc = JAXBContext.newInstance(WeChatReqBean.class);
 			Unmarshaller u = jc.createUnmarshaller();
 			WeChatReqBean reqBean = (WeChatReqBean) u
 					.unmarshal(new StringReader(sb.toString()));
 
 			// 获取URL
 			String reqUrl = StringUtils.substringAfter(req.getRequestURI(),
 					req.getContextPath());
 			String reqMsgType = reqBean.getMsgType();
 			if (reqMsgType.equals("text"))
 				reqMsgType = "text";
 			else if (reqMsgType.equals("image"))
 				reqMsgType = "image";
 			else if (reqMsgType.equals("link"))
 				reqMsgType = "link";
 			else if (reqMsgType.equals("event"))
 				reqMsgType = "event";
 			else if (reqMsgType.equals("location"))
 				reqMsgType = "location";
 
 			String key = reqUrl + reqBean.getMsgType();
 			Object messageId = (Object) CacheContant.accountCache.get(key);
 			if (reqMsgType.equals("location")) {
 				String destination = (String) CacheContant.publicAccountCache
 						.get(reqUrl);
 				WeChatRespTextBean respBean = new WeChatRespTextBean();
 				respBean.setMsgType("text");
 				respBean.setCreateTime(new Date().getTime());
 				String origin = reqBean.getLocation_X().toString() + ","
 						+ reqBean.getLocation_Y().toString();
 				// 目前只支持上海
 				String conteng = BaiduAPI.navagation(origin, destination, "上海");
 				if (conteng == null || conteng.equals("")) {
 					conteng = origin;
 				}
				respBean.setContent(conteng);
 				createRespBean(reqBean, respBean, jc, out);
 			} else {
 				if (reqMsgType.equals("event")
 						&& reqBean.getEvent().equals("CLICK")) {
 					messageId = Long.valueOf(reqBean.getEventKey());
 				}
 				if (messageId instanceof String
 						&& ((String) messageId).contains("program")) {
 					String programUrl = (String) CacheContant.gameCache
 							.get(reqUrl);
 					// 说明是聊天机器人
 					if (programUrl.equals("autoreply")) {
 						@SuppressWarnings("unchecked")
 						List<WxCmd> cmds = (List<WxCmd>) CacheContant.autoReplayAndCmdCache
 								.get(reqUrl);
 						String cmdMessageId = null;
 						for (WxCmd cmd : cmds) {
 							if (cmd.getCtype().equals("whole")) {
 								if (reqBean.getContent().equals(cmd.getCmd())) {
 									cmdMessageId = String.valueOf(cmd
 											.getMessageId().longValue());
 								}
 							} else if (cmd.getCtype().equals("startwith")) {
 								if (reqBean.getContent().startsWith(
 										cmd.getCmd())) {
 									cmdMessageId = String.valueOf(cmd
 											.getMessageId().longValue());
 								}
 							} else {
 								cmdMessageId = "1";// 随意指定一条返回
 							}
 						}
 						Object respObj = CacheContant.sourceCache
 								.get(cmdMessageId);
 						createRespBean(reqBean, respObj, jc, out);
 					} else {
 						String p = "/program/" + programUrl;
 						req.getRequestDispatcher(p).include(req, resp);
 					}
 				} else {
 					Object respObj = CacheContant.sourceCache
 							.get(((Long) messageId).toString());
 					createRespBean(reqBean, respObj, jc, out);
 				}
 			}
 
 			out.flush();
 		} catch (JAXBException e) {
 			log.info(e.getMessage());
 		} finally {
 			if (scanner != null) {
 				scanner.close();
 				scanner = null;
 			}
 			if (out != null) {
 				out.close();
 				out = null;
 			}
 		}
 	}
 
 	/**
 	 * @param reqBean
 	 * @return
 	 */
 	private void createRespBean(WeChatReqBean reqBean, Object obj,
 			JAXBContext jc, PrintWriter out) {
 
 		if (obj instanceof WeChatRespTextBean) {
 			WeChatRespTextBean textBean = (WeChatRespTextBean) obj;
 			textBean.setFromUserName(reqBean.getToUserName());
 			textBean.setToUserName(reqBean.getFromUserName());
 			// 4、创建一个文本回复消息
 			try {
 				jc = JAXBContext.newInstance(WeChatRespTextBean.class);
 				Marshaller m = jc.createMarshaller();
 				m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
 				m.setProperty(CharacterEscapeHandler.class.getName(),
 						new CharacterEscapeHandler() {
 							@Override
 							public void escape(char[] arg0, int arg1, int arg2,
 									boolean arg3, Writer arg4)
 									throws IOException {
 								arg4.write(arg0, arg1, arg2);
 							}
 						});
 				m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
 				textBean.JAXBmarshal(new File(System.getProperty("ssweb.root")
 						+ "/backupMessage"));
 				m.marshal(textBean, out);
 			} catch (JAXBException e) {
 				log.info(e.getMessage());
 			}
 
 		} else if (obj instanceof WeiChatRespImageBean) {
 			WeiChatRespImageBean imageBean = (WeiChatRespImageBean) obj;
 			imageBean.setFromUserName(reqBean.getToUserName());
 			imageBean.setToUserName(reqBean.getFromUserName());
 			// 4、创建一个图文回复消息
 			try {
 				jc = JAXBContext.newInstance(WeiChatRespImageBean.class);
 				Marshaller m = jc.createMarshaller();
 				m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
 				m.setProperty(CharacterEscapeHandler.class.getName(),
 						new CharacterEscapeHandler() {
 							@Override
 							public void escape(char[] arg0, int arg1, int arg2,
 									boolean arg3, Writer arg4)
 									throws IOException {
 								arg4.write(arg0, arg1, arg2);
 							}
 						});
 				m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
 				imageBean.JAXBmarshal(new File(System.getProperty("ssweb.root")
 						+ "/backupMessage"));
 				m.marshal(imageBean, out);
 			} catch (JAXBException e) {
 				log.info(e.getMessage());
 			}
 		} else if (obj instanceof WeiChatRespMusicAndVideoBean) {
 			WeiChatRespMusicAndVideoBean musicBean = (WeiChatRespMusicAndVideoBean) obj;
 			musicBean.setFromUserName(reqBean.getToUserName());
 			musicBean.setToUserName(reqBean.getFromUserName());
 			// 4、创建一个音乐回复消息
 			try {
 				jc = JAXBContext
 						.newInstance(WeiChatRespMusicAndVideoBean.class);
 				Marshaller m = jc.createMarshaller();
 				m.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
 				m.setProperty(CharacterEscapeHandler.class.getName(),
 						new CharacterEscapeHandler() {
 							@Override
 							public void escape(char[] arg0, int arg1, int arg2,
 									boolean arg3, Writer arg4)
 									throws IOException {
 								arg4.write(arg0, arg1, arg2);
 							}
 						});
 				m.setProperty(Marshaller.JAXB_FRAGMENT, Boolean.TRUE);
 				musicBean.JAXBmarshal(new File(System.getProperty("ssweb.root")
 						+ "/backupMessage"));
 				m.marshal(musicBean, out);
 			} catch (JAXBException e) {
 				log.info(e.getMessage());
 			}
 		}
 
 	}
 
 	@Override
 	public void afterPropertiesSet() throws Exception {
 		Thread intiThread = new InitThreadService(accountDao, messageTypeDao,
 				wxCmdDao, gameDao, messageDao, contentDao, lbsDao);
 		intiThread.start();
 
 	}
 
 }
