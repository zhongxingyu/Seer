 package com.nbcedu.function.teachersignup.util;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.OutputStream;
import java.text.SimpleDateFormat;
 import java.util.Arrays;
 import java.util.Date;
 
 import org.apache.commons.lang.xwork.StringUtils;
 
 import com.nbcedu.function.functionsupport.core.PortalMessageUtil;
 import com.nbcedu.function.functionsupport.core.SupportManager;
 import com.nbcedu.function.functionsupport.mapping.PortalMessage;
 import com.nbcedu.function.teachersignup.model.TSActivity;
 
 public class Utils {
 	
 	private static SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
 	
 	public static final class Dates{
 		public static String fullDateStr(Date date){
 			return sdf.format(date);
 		}
 	}
 	
 	public static final class StreamUtil{
 		public static void copy(InputStream in,OutputStream out) throws IOException{
 			byte[] buffer = new byte[1024];
 			while(true){
 				int bytesRead = in.read(buffer);
 				if(bytesRead==-1){
 					break;
 				}
 				out.write(buffer,0,bytesRead);
 			}
 		}
 	}
 	
 	public static final class Message{
 		
 		private static final PortalMessageUtil msgUtil = SupportManager.getPortalMessageUtil();
 		
 		/**
 		 * 发布时发送门户消息
 		 * @param act
 		 * @author xuechong
 		 */
 		public static final void sendAddMsg(final TSActivity act){
 			class PostMsg implements Runnable{
 				@Override
 				public void run() {
 					PortalMessage msg = new PortalMessage();
 					msg.setFunctionName("teachersignup");
 					msg.setContent(StringUtils.trimToEmpty(act.getComment()));
 					msg.setTitle(StringUtils.trimToEmpty(act.getName()));
 					msg.setMessageType("type_01");
 					msg.setIdentityCodes(Arrays.asList("3022100"));//教师
 					msg.setMessageId(act.getId());
 					msg.setModuleName("教师报名");
 					msgUtil.sendPortalMsg(msg,null);
 				}
 			}
 			
 			new Thread(new PostMsg()).start();
 		}
 		
 		/**
 		 * 获取新增公告SQL
 		 * @param act
 		 * @param createUid
 		 * @return
 		 * @author xuechong
 		 */
 		public static final String getInsertSQL(TSActivity act,String createUid,String curUserName){
 			
 			String uuid = act.getId();
 			String imgPath = "";
 			String createDate = sdf.format(new Date());
 			
 			StringBuilder result = new StringBuilder();
 
 			result.append("INSERT INTO T_HSI_POST ");
 			result.append("(PK_T_HSI_POST_POSTID,");
 			result.append("THP_TITLE,");
 			result.append("THP_NAME,");
 			result.append("THP_USERID,");
 			result.append("THP_IMAGEURL,");//pg.jpg
 			result.append("THP_POST_TIME,");
 			result.append("THP_TEXT,");
 			result.append("THP_POST_CREATTIME,");
 			result.append("THP_ENABLE,");//int 
 			result.append("THP_TIMESTART,");
 			result.append("THP_TIMEEND,");
 			result.append("THP_POSTFLAG,");//int
 			result.append("THP_ZDFLAG,");//int
 			result.append("THP_POSTSTATE,");//int
 			result.append("THP_ZDTIME,");//
 			result.append("THP_SOURCE)");
 			
 			result.append(" values(");
 			result.append("'${id}','${title}','${name}','${userId}',");
 			result.append("'${img}','${postTime}','${text}','${createTime}',");
 			result.append("${enable},'${timeStart}','${timeEnd}',");
 			result.append("${postFlag},${zdFlag},'${postState}','${zdTime}',");
 			result.append("'${source}');");
 			
 			fastReplace(result, "${id}", uuid);
 			fastReplace(result, "${title}", act.getName());
 			fastReplace(result, "${name}", curUserName);
 			fastReplace(result, "${userId}", createUid);
 			fastReplace(result, "${img}", imgPath);
 			fastReplace(result, "${postTime}", createDate);
 			fastReplace(result, "${text}", StringUtils.trimToEmpty(act.getComment()));
 			fastReplace(result, "${createTime}", createDate);
 			fastReplace(result, "${enable}", "1");
 			fastReplace(result, "${timeStart}", createDate);
 			fastReplace(result, "${timeEnd}", sdf.format(act.getEndDate()));
 			fastReplace(result, "${postFlag}", "1");
 			fastReplace(result, "${zdFlag}", "0");
 			fastReplace(result, "${zdTime}", sdf.format(act.getEndDate()));
 			fastReplace(result, "${postState}", "1");
 			fastReplace(result, "${source}", "");
 			return result.toString();
 		}
 	}
 	
 	static void fastReplace(StringBuilder builder , String origin,String replaceMent){
 		int start = builder.indexOf(origin);
 		builder.replace(start,start+origin.length(),replaceMent);
 	}
 }
