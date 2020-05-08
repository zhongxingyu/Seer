 package org.centny.mgw.servlet;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.ServletConfig;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import org.centny.jge.JGitExt;
 import org.eclipse.jetty.util.log.Log;
 import org.eclipse.jetty.util.log.Logger;
 import org.eclipse.jgit.api.Git;
 import org.eclipse.jgit.internal.storage.file.FileRepository;
 
 /**
  * the repository manager.
  * 
  * @author Centny.
  * 
  */
 public class RepoMgrServlet extends CmdServlet {
 
 	/**
 	 * serial version id.
 	 */
 	private static final long serialVersionUID = 3600259257847149848L;
 
 	/**
 	 * the repository root folder.
 	 */
 	private File repoDir;
 
 	/**
 	 * the logger.
 	 */
 	private Logger log = Log.getLogger(RepoMgrServlet.class);
 
 	/**
 	 * @return the repoDir
 	 */
 	public File getRepoDir() {
 		return repoDir;
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see javax.servlet.GenericServlet#init(javax.servlet.ServletConfig)
 	 */
 	@Override
 	public void init(ServletConfig config) throws ServletException {
 		String rdpath = config.getInitParameter("base-path");
 		if (rdpath == null || rdpath.trim().isEmpty()) {
 			throw new ServletException("repository base path is not setted.");
 		}
 		this.repoDir = new File(rdpath);
 		if (!this.repoDir.exists()) {
 			throw new ServletException("repository base path is not found.");
 		}
 		this.log.debug("initial repository manager by base path:"
 				+ this.repoDir.getAbsolutePath());
 	}
 
 	/*
 	 * (non-Javadoc)
 	 * 
 	 * @see org.centny.mgw.servlet.CmdServlet#doCmds(javax.servlet.http.
 	 * HttpServletRequest, javax.servlet.http.HttpServletResponse,
 	 * java.util.List)
 	 */
 	@Override
 	protected void doCmds(HttpServletRequest req, HttpServletResponse resp,
 			List<String> cmds) throws IOException {
 		if (cmds.size() < 2) {
 			this.sendBadRequest(resp,
 					"Usage:[add|export|unexport|auth]/<repository name>/");
 			return;
 		}
 		String rname = cmds.get(1);
 		if (!rname.matches("^.*\\.git$")) {
 			rname += ".git";
 		}
 		String cmd = cmds.get(0);
 		if (cmd.equals("add")) {
 			this.addRepositroy(req, resp, cmds, rname);
 		} else if (cmd.equals("export")) {
 			this.exportRepository(req, resp, rname, true);
 		} else if (cmd.equals("unexport")) {
 			this.exportRepository(req, resp, rname, false);
 		} else if (cmd.equals("auth")) {
 			if (cmds.size() < 5) {
 				this.sendBadRequest(resp,
 						"Usage:auth/<repository name>/[role|user]/[add|del]/<role or user name>");
 				return;
 			}
 			String targ = cmds.get(2);
 			if (!targ.equals("role") && !targ.equals("user")) {
 				this.sendBadRequest(resp, "invalid parameter.");
 				return;
 			}
 			String aarg = cmds.get(3);
 			if (!aarg.equals("add") && !aarg.equals("del")) {
 				this.sendBadRequest(resp, "invalid parameter.");
 				return;
 			}
 			boolean isRole = targ.equals("role");
 			boolean isAdd = aarg.equals("add");
 			String tname = cmds.get(4);
 			this.authRepository(req, resp, rname, tname, isRole, isAdd);
 		}
 	}
 
 	/**
 	 * add a repository.
 	 * 
 	 * @param req
 	 *            http request.
 	 * @param resp
 	 *            http response.
 	 * @param cmds
 	 *            the add command and parameter.
 	 * @param rname
 	 *            the repository name.
 	 * @throws IOException
 	 *             error exception.
 	 */
 	private void addRepositroy(HttpServletRequest req,
 			HttpServletResponse resp, List<String> cmds, String rname)
 			throws IOException {
 
 		File repo = new File(this.repoDir, rname);
 		if (repo.exists()) {
 			resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
 					"repository aleady exist.");
 			return;
 		}
 		try {
			FileRepository nrepo = new FileRepository(repo);
			nrepo.create();
 			resp.getOutputStream().write("OK".getBytes());
 		} catch (Exception e) {
 			resp.sendError(HttpServletResponse.SC_BAD_REQUEST,
 					"create repository error:" + e.toString());
 		}
 	}
 
 	/**
 	 * @param req
 	 *            http request.
 	 * @param resp
 	 *            http response.
 	 * @param rname
 	 *            the repository name.
 	 * @param export
 	 *            if mark export to repository.
 	 * @throws IOException
 	 *             error.
 	 */
 	private void exportRepository(HttpServletRequest req,
 			HttpServletResponse resp, String rname, boolean export)
 			throws IOException {
 		File repo = new File(this.repoDir, rname);
 		if (repo.exists()) {
 			try {
 				Git git = Git.open(repo);
 				if (export) {
 					JGitExt.markExport(git.getRepository());
 				} else {
 					JGitExt.markUnexport(git.getRepository());
 				}
 				resp.getOutputStream().write("Ok".getBytes());
 			} catch (Exception e) {
 				resp.getOutputStream().write("Error".getBytes());
 			}
 		} else {
 			resp.sendError(HttpServletResponse.SC_NOT_FOUND,
 					"repository not found.");
 		}
 	}
 
 	/**
 	 * authorize a repository.
 	 * 
 	 * @param req
 	 *            http request.
 	 * @param resp
 	 *            http response.
 	 * @param rname
 	 *            the repository name.
 	 * @param tname
 	 *            the target name.
 	 * @param isRole
 	 *            true is add role,or user.
 	 * @param isAdd
 	 *            true is add target to authorization,or not.
 	 * @throws IOException
 	 *             error exception.
 	 */
 	private void authRepository(HttpServletRequest req,
 			HttpServletResponse resp, String rname, String tname,
 			boolean isRole, boolean isAdd) throws IOException {
 		File repo = new File(this.repoDir, rname);
 		if (repo.exists()) {
 			try {
 				Git git = Git.open(repo);
 				boolean res = JGitExt.authRepository(git.getRepository(),
 						tname, isRole, isAdd);
 				if (res) {
 					resp.getOutputStream().write("Ok".getBytes());
 				} else {
 					resp.getOutputStream().write("Error".getBytes());
 				}
 			} catch (Exception e) {
 				resp.sendError(HttpServletResponse.SC_NOT_FOUND,
 						"mark repository error.");
 			}
 		} else {
 			resp.sendError(HttpServletResponse.SC_NOT_FOUND,
 					"repository not found.");
 		}
 	}
 }
