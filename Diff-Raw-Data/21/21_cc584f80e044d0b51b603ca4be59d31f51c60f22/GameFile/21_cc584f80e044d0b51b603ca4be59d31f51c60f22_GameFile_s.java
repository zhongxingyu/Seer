 /**
  * 
  */
 package com.k99k.testcenter;
 
 import java.io.File;
 import java.io.FileInputStream;
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.UnsupportedEncodingException;
 
 import javax.servlet.ServletOutputStream;
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.log4j.Logger;
 
 import com.k99k.khunter.Action;
 import com.k99k.khunter.ActionMsg;
 import com.k99k.khunter.DaoInterface;
 import com.k99k.khunter.DaoManager;
 import com.k99k.khunter.HttpActionMsg;
 import com.k99k.khunter.JOut;
 import com.k99k.khunter.KFilter;
 import com.k99k.khunter.KObjManager;
 import com.k99k.khunter.KObjSchema;
 import com.k99k.khunter.KObject;
 import com.k99k.tools.StringUtil;
 
 /**
  * @author keel
  *
  */
 public class GameFile extends Action {
 
 	/**
 	 * @param name
 	 */
 	public GameFile(String name) {
 		super(name);
 	}
 	static final Logger log = Logger.getLogger(GameFile.class);
 	static DaoInterface dao;
 	static KObjSchema schema;
 	
 	/**
 	 * 真实文件存放路径,必须是绝对路径
 	 */
 	private String path = "";
 	private final static String contentType = "application/x-msdownload";
 
 	/* (non-Javadoc)
 	 * @see com.k99k.khunter.Action#act(com.k99k.khunter.ActionMsg)
 	 */
 	@Override
 	public ActionMsg act(ActionMsg msg) {
 		HttpActionMsg httpmsg = (HttpActionMsg)msg;
 		//判断权限
 		KObject u = Auth.checkCookieLogin(httpmsg);
 		if (u == null) {
 			msg.addData("[redirect]", "/login");
 			return super.act(msg);
 		}
 		//获取数据库GameFile
 		String fileId = KFilter.actPath(msg, 2, "");
 		if (!StringUtil.isDigits(fileId)) {
 			JOut.err(404,"文件不存在", httpmsg);
 			return super.act(msg);
 		}
 //		try {
 //			file = URLDecoder.decode(file, "utf-8");
 //		} catch (UnsupportedEncodingException e) {
 //			JOut.err(404,"文件不存在", httpmsg);
 //			return super.act(msg);
 //		}
 		KObject g = dao.findOne(Long.parseLong(fileId));
 		if (g==null) {
 			log.error("Gamefile not found:"+fileId);
 			JOut.err(404,"文件不存在", httpmsg);
 			return super.act(msg);
 		}
 		String file = g.getName();
		if (!(Integer.parseInt(u.getType())>2 || u.getCreatorName().equals(g.getCreatorName()))) {
 			log.error("Gamefile auth failed. fileId:"+fileId+" userid:"+u.getId());
 			JOut.err(404,"无权限", httpmsg);
 			return super.act(msg);
 		}
 		//获取真实文件名
 		String fullPath = this.path+g.getProp("fileName");
 		File f = new File(fullPath);
 		if (f.exists()) {
 			HttpServletResponse resp = httpmsg.getHttpResp();
 			resp.reset();
 			resp.setContentType(contentType);
 			try {
 				resp.addHeader("Content-Disposition", "attachment; filename=\"" + new String(file.getBytes("GBK"),"ISO8859_1")  + "\"");
 			} catch (UnsupportedEncodingException e1) {
 				log.error("file name encode error",e1);
 				JOut.err(404,"文件名处理出错", httpmsg);
 				return super.act(msg);
 			}
 			int len = (int)f.length();
 			resp.setContentLength(len);
 			if (len > 0) {
 				try {
 					InputStream inStream = new FileInputStream(f);
 					byte[] buf = new byte[4096];
 					ServletOutputStream servletOS = resp.getOutputStream();
 					int readLength;
 					while (((readLength = inStream.read(buf)) != -1)) {
 						servletOS.write(buf, 0, readLength);
 					}
 					inStream.close();
 					servletOS.flush();
 					servletOS.close();
 					//不让KFilter处理
 					msg.addData("[none]", "true");
 					return super.act(msg);
 				} catch (IOException e) {
 					log.error("Gamefile download failed:"+f);
 					JOut.err(404, "文件下载失败", httpmsg);
 					return super.act(msg);
 				}
 			}
 		}
 		log.error("Gamefile not exist:"+f);
 		JOut.err(404,"文件不存在", httpmsg);
 		return super.act(msg);
 	}
 
 	/* (non-Javadoc)
 	 * @see com.k99k.khunter.Action#init()
 	 */
 	@Override
 	public void init() {
 		dao = DaoManager.findDao("TCGameFileDao");
 		schema = KObjManager.findSchema("TCGameFile");
 		super.init();
 	}
 	
 	/**
 	 * @return the path
 	 */
 	public final String getPath() {
 		return path;
 	}
 
 	/**
 	 * @param path the path to set
 	 */
 	public final void setPath(String path) {
 		this.path = path;
 	}
 	
 }
