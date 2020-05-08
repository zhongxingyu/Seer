 package org.centny.mgw.servlet;
 
 import static javax.servlet.http.HttpServletResponse.SC_INTERNAL_SERVER_ERROR;
 import static javax.servlet.http.HttpServletResponse.SC_NOT_FOUND;
 import static org.eclipse.jgit.http.server.GitSmartHttpTools.sendError;
 
 import java.io.File;
 import java.io.IOException;
 import java.text.MessageFormat;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.centny.jge.amerge.AutoMerge;
 import org.centny.mgw.SyncMgr;
 import org.centny.mgw.jgit.AuthorizationFileResolver;
 import org.eclipse.jetty.util.log.Log;
 import org.eclipse.jetty.util.log.Logger;
 import org.eclipse.jgit.http.server.GitServlet;
 import org.eclipse.jgit.http.server.GitSmartHttpTools;
 import org.eclipse.jgit.http.server.HttpServerText;
 import org.eclipse.jgit.util.StringUtils;
 
 /**
  * the MGitServlert.
  * 
  * @author Scorpion
  * 
  */
 public class MGitServlet extends GitServlet {
 
 	/**
 	 * serial id.
 	 */
 	private static final long serialVersionUID = 5157514874051960832L;
 	/**
 	 * the logger.
 	 */
 	private Logger log = Log.getLogger(MGitServlet.class);
 
 	// //// ////// //////
 	private static File getFile(ServletConfig cfg, String param)
 			throws ServletException {
 		String n = cfg.getInitParameter(param);
 		if (n == null || "".equals(n))
 			throw new ServletException(MessageFormat.format(
 					HttpServerText.get().parameterNotSet, param));
 
 		File path = new File(n);
 		if (!path.exists()) {
 			path.mkdirs();
 		}
 		if (!path.exists())
 			throw new ServletException(MessageFormat.format(
 					HttpServerText.get().pathForParamNotFound, path, param));
 		return path;
 	}
 
 	private static boolean getBoolean(ServletConfig cfg, String param)
 			throws ServletException {
 		String n = cfg.getInitParameter(param);
 		if (n == null)
 			return false;
 		try {
 			return StringUtils.toBoolean(n);
 		} catch (IllegalArgumentException err) {
 			throw new ServletException(MessageFormat.format(
 					HttpServerText.get().invalidBoolean, param, n));
 		}
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.jgit.http.server.GitServlet#init(javax.servlet.ServletConfig)
 	 */
 	@Override
 	public void init(final ServletConfig config) throws ServletException {
 		// initial resolver.
 		File root = getFile(config, "base-path");
 		if (!root.exists()) {
 			root.mkdirs();
 		}
 		boolean exportAll = getBoolean(config, "export-all");
 		this.setRepositoryResolver(new AuthorizationFileResolver(root,
 				exportAll));
 		this.log.info("initial by(base-path:" + root.getAbsolutePath()
 				+ ",export-all:" + exportAll + ")");
 		//
 		// System.out.println("<-----ssssssddddddddddd------>");
 		// System.out.println(config.getInitParameter("base-path"));
 		// SyncMgr.smgr().setWsDir(new File("/tmp/mgit"));
 		// SyncMgr.smgr().setSync2Remoete(true);
 		// AutoMerge am = SyncMgr.smgr().amerge("jgd");
 		// if (am == null) {
 		// try {
 		// SyncMgr.smgr().addAMerge("jgd",
 		// "file:///Users/Scorpion/Temp/jgd.git",
 		// "file:///Users/Scorpion/Temp/jgdt.git");
 		// } catch (Exception e) {
 		// e.printStackTrace();
 		// }
 		// }
 		super.init(config);
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see
 	 * org.eclipse.jgit.http.server.glue.MetaServlet#service(javax.servlet.http
 	 * .HttpServletRequest, javax.servlet.http.HttpServletResponse)
 	 */
 	@Override
 	protected void service(HttpServletRequest req, HttpServletResponse res)
 			throws IOException, ServletException {
 		String name = req.getPathInfo();
 		while (name != null && 0 < name.length() && name.charAt(0) == '/')
 			name = name.substring(1);
 		if (name == null || name.length() == 0) {
 			sendError(req, res, SC_NOT_FOUND);
 			return;
 		}
 		this.log.debug("git server for " + name);
 		name = name.replaceAll("\\.git.*$", "");
 		AutoMerge am = SyncMgr.smgr().amerge(name);
 		if (am != null) {
 			if (SyncMgr.smgr().isSync2Remoete()) {
 				try {
 					am.pullR2L();
 				} catch (Exception e) {
 					e.printStackTrace();
 				}
 			}
 			if (am.isConflict()) {
 				sendError(req, res, SC_INTERNAL_SERVER_ERROR,
 						"repository conflicting...");
 				return;
 			}
 		}
 		// System.out.println(req.getPathInfo());
 		super.service(req, res);
 		if (GitSmartHttpTools.isReceivePack(req) && am != null
 				&& SyncMgr.smgr().isSync2Remoete()) {
 			try {
				am.checkLogAndL2R();
 			} catch (Exception e) {
				e.printStackTrace();
 			}
 		}
 	}
 }
