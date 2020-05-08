 package org.alt60m.cms.servlet;
 
 //controller
 import org.alt60m.cms.model.File;
 import org.alt60m.cms.model.Category;
 import org.alt60m.cms.model.FileCategory;
 import org.alt60m.cms.util.*;
 import org.alt60m.servlet.*;
 import org.alt60m.staffSite.servlet.StaffController;
 import org.alt60m.util.ObjectHashUtil;
 import org.apache.log4j.*;
 import javax.servlet.*;
 import javax.servlet.http.*;
 //uploading files
 import com.oreilly.servlet.*;
 //searching and indexing
 import org.apache.lucene.search.BooleanQuery;
 import org.apache.lucene.search.PhraseQuery;
 import org.apache.lucene.index.Term;
 //misc
 import java.util.*;
 import java.io.*;
 
 /**
  * Web controller for Resource Center (Content Management System or CMS)
  *
  * History:
  *		5/22/01	MDP	Initial Coding
  *      7/14/03 DMB Refactored for DBIO
  *
  * Completeness (0 - 5):
  *		4
  *
  * Known Issues:
  *
  * @author  Paul R. Payne
  * @version 1.0
  */
 
 public class CmsController extends Controller {
 
 	final String CMS_USERS = "/WEB-INF/cmsusers.xml";
 	final String VIEWS_FILE = "/WEB-INF/cmsDBIOviews.xml";
 	final String CMS_FILESPECS = "/WEB-INF/cmsfilespecs.xml";
 	final String CONTENT_FOLDER = "/cms/content/";
 	final String INDEX_FOLDER = "/cms/cmsIndex/";
 
 	/**a Hashtable of all the moderators
 		@ see CmsController#initUsers() initUsers()
 	*/
 	private Hashtable usersRoles = new Hashtable();
 	/**a Hashtable of all file types and their specifications
 		@ see CmsController#initFileSpecs() initFileSpecs()
 	*/
 	private Hashtable fileSpecs = new Hashtable();
 	/** initialized with the init() method */
 	String contentPath;
 
 	/**starts the log and initializes the ObjectAdaptors */
 	public CmsController() {
 		log.debug("CmsController constructor");
 	}
 
 	/**runs initUsers() and initFileSpecs() and initializes searchIndexPath and contentPath */
 	public void init() {
 		log.debug("CmsController.init()");
 
 		super.setViewsFile(getServletContext().getRealPath(VIEWS_FILE));
 		super.setDefaultAction("home");
 
 		initUsers(false);
 		initFileSpecs(false);
 		contentPath = getServletContext().getRealPath(CONTENT_FOLDER);
 		CmsIndex.SetIndexPath(getServletContext().getRealPath(INDEX_FOLDER));
 		CmsIndex.setFileSpecsPath(getServletContext().getRealPath(CMS_FILESPECS));
 		log.debug("contentPath: " + contentPath);
 	}
 
 	/**gets a list of the moderators from cmsusers.xml */
 	private void initUsers(boolean verbose) {
 		usersRoles = UsersProcessor.parse(getServletContext().getRealPath(CMS_USERS));
 		if (verbose) {
 			for (Enumeration e = usersRoles.keys(); e.hasMoreElements();) {
 				String k = (String) e.nextElement();
 				log.debug(k + " " + usersRoles.get(k));
 			}
 			log.debug("finished loading users.");
 		}
 	}
 
 	/**initializes the fileSpecs Hashtable with file specifications from cmsfilespecs.xml */
 	private void initFileSpecs(boolean verbose) {
 		fileSpecs = CmsFileSpecsProcessor.parse(getServletContext().getRealPath(CMS_FILESPECS));
 		if (verbose) {
 			for (Enumeration e = usersRoles.keys(); e.hasMoreElements();) {
 				String k = (String) e.nextElement();
 				log.debug(k + " " + fileSpecs.get(k));
 			}
 			log.debug("finished loading filespecs.");
 		}
 	}
 	/**reinitializes the users, views and file specifications */
 	public void reload() {
 		initViews(getServletContext().getRealPath(VIEWS_FILE));
 		initUsers(true);
 		initFileSpecs(true);
 	}
 
 	/**
 	 * @param ctx ActionContext
 	 * @return tub Contains top10s Hashtable, SubCats Hashtable and Moderator (true if user is a moderator)
 	 */
 	public void home(ActionContext ctx) {
 		try {
 			Hashtable tub = new Hashtable();
 
 			/*
 			String query = "select f from org.alt60m.cms.model.CmsFile as f where f.accessCount>142 order by f.accessCount DESC";
 
 			Collection top10s;
 
 			String query = "select f from org:alt60m:cms:ejb:CmsFile as f where f.accessCount(MAX)";
 			Vector temp10 = (Vector)_cmsFileAdaptor.list(query);
 			Hashtable temphash = (Hashtable)temp10.get(0);
 			top10s.add(temphash);
 			tempCount = temphash.get("AccessCount");
 
 			for (int i=0;i<9;i++) {
 				String query = "select f from org:alt60m:cms:ejb:CmsFile as f where f.accessCount(MAX) and f.accessCount<"+tempCount;
 				Vector temp10 = (Vector)_cmsFileAdaptor.list(query);
 				Hashtable temphash = (Hashtable)temp10.get(0);
 				top10s.add(temphash);
 				tempCount = temphash.get("AccessCount");
 			}
 
 			String[] fields2 = {"CmsFileID","Title"};
 			Collection top10s = _cmsFileAdaptor.list(query,fields2);
 			tub.put("top10s",top10s);
 			*/
 
 			//			String query = "select obj from org.alt60m.cms.model.CmsCategory as obj where obj.parentCategory = \"0\" order by obj.catName";
 			//			String query = "call sql select CmsCategoryId, catName from cms_cmscategory as obj where obj.parentCategory = '0' order by obj.catName as org.alt60m.persistence.castor.util.TwoFields";
 			//String query = "CALL SQL SELECT * from cms_cmscategory as obj where obj.parentCategory like \"0\" order by obj.catName";
 			//			String[] fields = {"CmsCategoryId","CatName"};
 			CmsInfo ci = new CmsInfo();
 			//			Collection subCats = _cmsCategoryAdaptor.list(query,fields);
 			Collection subCats = ci.getParentCategories();
 			tub.put("SubCats", subCats);
 
 			Hashtable profile = (Hashtable) ctx.getSessionValue("profile");
 			if (usersRoles.size() == 0) {
 				initUsers(false);
 			}
 			String username = null;
 			if (profile != null) {
 				username = (String) profile.get("UserName");
 			}
 			if (username != null && usersRoles.containsKey(username.toLowerCase())) {
 				tub.put("Moderator", "true");
 			} else {
 				tub.put("Moderator", "false");
 			}
 			ctx.setReturnValue(tub);
 			ctx.goToView("home");
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform home() action.", e);
 		}
 	}
 
 	public void browse(ActionContext ctx) {
 		try {
 			String catId = ctx.getInputString("catId");
 			Hashtable profile = (Hashtable) ctx.getSessionValue("profile");
 			if ((profile == null) && ((ctx.getSession() == null) || (ctx.getSessionValue("loggedIn") == null))) {
 
 				StaffController.recordLocation(ctx.getRequest());
 				ctx.getResponse().sendRedirect("/servlet/StaffController");
 			} else if (catId == null || catId.equals("0")) {
 				home(ctx);
 			} else {
 				Hashtable tub = new Hashtable();
 				String query = "";
 
 				Hashtable cat = ObjectHashUtil.obj2hash(new Category(catId));
 
 				//grab a list of all the sub categories
 				CmsInfo ci = new CmsInfo();
 				Collection subCats = ci.getSubCategories(catId);
 
 				//grab the files
 				if (catId.equals("1")) {
 					// need to list all that don't have a category assigned
					query = "CmsCategoryId is null";
 				} else {
 					query = "CmsCategoryId like '" + catId + "' order by title";
 				}
 
 				String[] fields2 = { "FileId", "Title", "Url", "ModeratedYet" };
 				File file = new File();
 				file.changeTargetTable("cms_viewcategoryidfiles");
 				Collection catFiles = ObjectHashUtil.list(file.selectList(query), fields2);
 
 				//set the image icon for each file
 				Iterator fi = catFiles.iterator();
 				while (fi.hasNext()) {
 					Hashtable f = (Hashtable) fi.next();
 					String url = (String) f.get("Url");
 					String ext = (url.indexOf('.') == -1) ? "" : url.toLowerCase().substring(url.lastIndexOf('.'));
 					if (url.indexOf("://") != -1) {
 						ext = ".html";
 					}
 					Hashtable fileSpec = (Hashtable) fileSpecs.get(ext);
 					f.put("Img", fileSpec.get("Img"));
 				}
 
 				// set moderator permissions
 				if (usersRoles.containsKey(profile.get("UserName").toString().toLowerCase())) {
 					tub.put("Moderator", "true");
 				} else {
 					tub.put("Moderator", "false");
 				}
 
 				// prepare the path for display
 				StringTokenizer path = new StringTokenizer((String) cat.get("Path"), ":");
 				StringTokenizer pathid = new StringTokenizer((String) cat.get("PathId"), ":");
 				if (path.countTokens() != pathid.countTokens()) {
 					throw new RuntimeException("path and pathid are different lengths");
 				}
 				StringBuffer cmsPath = new StringBuffer("<a href=\"/servlet/CmsController?action=home\">Home</a>");
 				while (path.hasMoreTokens()) {
 					cmsPath.append("&nbsp;<b>:</b>&nbsp;<a href=\"/servlet/CmsController?action=browse&catId=" + pathid.nextToken() + "\">" + path.nextToken() + "</a>");
 				}
 
 				// load up the tub
 				tub.put("SubCats", subCats);
 				tub.put("CatFiles", catFiles);
 				tub.put("CategoryId", catId);
 				tub.put("CatName", cat.get("CatName"));
 				tub.put("cmsPath", cmsPath.toString());
 
 				ctx.setReturnValue(tub);
 				ctx.goToView("browse");
 			}
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform browse() action.", e);
 		}
 	}
 
 	public void reindex(ActionContext ctx) {
 		try {
 			CmsIndex ci = new CmsIndex();
 			log.debug("Index created");
 			ci.clear();
 			log.debug("index cleared");
 			ci.populate();
 			log.debug("index populated");
 			ctx.getResponse().getWriter().print("Reindex complete");
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform reindex() action.", e);
 		}
 	}
 
 	public void showAdvancedSearch(ActionContext ctx) {
 		try {
 			ctx.goToView("advancedSearch");
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform advancedSearch() action.", e);
 		}
 	}
 
 	public void advancedSearch(ActionContext ctx) {
 		try {
 			BooleanQuery bq = new BooleanQuery();
 
 			//if (!ctx.getInputString("all").equals("")){
 			//	query.append("+all:"+ctx.getInputString("all")+" ");
 			//}
 			if (!ctx.getInputString("author").equals("")) {
 				PhraseQuery pq = new PhraseQuery();
 				pq.setBoost(1);
 				pq.add(new Term("author", ctx.getInputString("author")));
 				if (ctx.getInputString("authorLogic").equals("true")) {
 					bq.add(pq, true, false);
 				} else {
 					bq.add(pq, false, true);
 				}
 			}
 			if (!ctx.getInputString("title").equals("")) {
 				PhraseQuery pq = new PhraseQuery();
 				pq.setBoost(1);
 				pq.add(new Term("title", ctx.getInputString("title")));
 				if (ctx.getInputString("titleLogic").equals("true")) {
 					bq.add(pq, true, false);
 				} else {
 					bq.add(pq, false, true);
 				}
 			}
 			if (!ctx.getInputString("keywords").equals("")) {
 				PhraseQuery pq = new PhraseQuery();
 				pq.setBoost(1);
 				pq.add(new Term("keywords", ctx.getInputString("keywords")));
 				if (ctx.getInputString("keywordsLogic").equals("true")) {
 					bq.add(pq, true, false);
 				} else {
 					bq.add(pq, false, true);
 				}
 			}
 			if (!ctx.getInputString("summary").equals("")) {
 				PhraseQuery pq = new PhraseQuery();
 				pq.setBoost(1);
 				pq.add(new Term("summary", ctx.getInputString("summary")));
 				if (ctx.getInputString("summaryLogic").equals("true")) {
 					bq.add(pq, true, false);
 				} else {
 					bq.add(pq, false, true);
 				}
 			}
 			if (!ctx.getInputString("quality").equals("")) {
 				PhraseQuery pq = new PhraseQuery();
 				pq.setBoost(1);
 				pq.add(new Term("quality", ctx.getInputString("quality")));
 				if (ctx.getInputString("qualityLogic").equals("true")) {
 					bq.add(pq, true, false);
 				} else {
 					bq.add(pq, false, true);
 				}
 			}
 			if (!ctx.getInputString("type").equals("")) {
 				PhraseQuery pq = new PhraseQuery();
 				pq.setBoost(1);
 				pq.add(new Term("type", ctx.getInputString("type")));
 				if (ctx.getInputString("typeLogic").equals("true")) {
 					bq.add(pq, true, false);
 				} else {
 					bq.add(pq, false, true);
 				}
 			}
 			String query = bq.toString("all");
 			Hashtable results = CmsIndex.search(query);
 
 			searchResults(ctx, results, query);
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform advancedSearch() action.", e);
 		}
 	}
 
 	public void simpleSearch(ActionContext ctx) {
 		try {
 			if ((ctx.getInputString("query") != null) && !((ctx.getInputString("query").trim()).equals(""))) {
 				String query = ctx.getInputString("query").trim();
 				if (ctx.getInputString("exact") != null) {
 					if (!query.startsWith("\""))
 						query = "\"" + query;
 					if (!query.endsWith("\""))
 						query = query + "\"";
 				}
 
 				Hashtable results = CmsIndex.search(query);
 
 				searchResults(ctx, results, query);
 			}
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform simpleSearch() action.", e);
 		}
 	}
 
 	private void searchResults(ActionContext ctx, Hashtable results, String query) {
 		try {
 			Hashtable tub = new Hashtable();
 			String formFill = new String();
 
 			formFill = new String(query);
 			// replace the quotes
 			int counter = 0;
 			while ((counter = formFill.indexOf("\"", counter)) != -1) {
 				formFill = new String(new StringBuffer(formFill).replace(counter, counter + 1, "&quot;").toString());
 			}
 
 			// figure out the start and end for the results page
 			Integer start = new Integer(0);
 			Integer end;
 			int count = results.size(); //results1.size() + results2.size() + results3.size();
 			if (ctx.getInputString("start") != null)
 				start = Integer.valueOf((String) ctx.getInputString("start"));
 			if (count <= (start.intValue() + 10)) {
 				end = new Integer(count);
 			} else
 				end = new Integer(start.intValue() + 10);
 
 			tub.put("start", start);
 			tub.put("end", end);
 			tub.put("count", new Integer(count));
 			tub.put("query", formFill);
 			tub.put("results", results);
 			/*
 			tub.put("results1",results1);
 			tub.put("results2",results2);
 			tub.put("results3",results3);
 			*/
 			ctx.setReturnValue(tub);
 			ctx.goToView("searchResults");
 
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform searchResults() action.", e);
 		}
 	}
 
 	public void selectFile(ActionContext ctx) {
 		try {
 			ctx.goToView("selectFile");
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform selectFile() action.", e);
 		}
 	}
 
 	public void uploadForm(ActionContext ctx) {
 		try {
 			ctx.goToView("uploadForm");
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform uploadForm action.", e);
 		}
 	}
 
 	public void popupUploadForm(ActionContext ctx) {
 		try {
 
 			Hashtable tub = new Hashtable();
 			tub.put("CatId", ctx.getInputString("catId"));
 			ctx.setReturnValue(tub);
 			ctx.goToView("popupUploadForm");
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform popupUploadForm action.", e);
 		}
 	}
 
 	public void popupUploadFile(ActionContext ctx) {
 		try {
 
 			Hashtable tub = new Hashtable();
 			tub.put("CatId", ctx.getInputString("catId"));
 			ctx.setReturnValue(tub);
 			ctx.goToView("popupUploadFile");
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform popupUploadFile action.", e);
 		}
 	}
 
 	public void popupUploadText(ActionContext ctx) {
 		try {
 
 			Hashtable tub = new Hashtable();
 			tub.put("CatId", ctx.getInputString("catId"));
 			ctx.setReturnValue(tub);
 			ctx.goToView("popupUploadText");
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform popupUploadText action.", e);
 		}
 	}
 
 	public void popupUploadWeb(ActionContext ctx) {
 		try {
 
 			Hashtable tub = new Hashtable();
 			tub.put("CatId", ctx.getInputString("catId"));
 			ctx.setReturnValue(tub);
 			ctx.goToView("popupUploadWeb");
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform popupUploadWeb action.", e);
 		}
 	}
 
 	public void popupUpload(ActionContext ctx) {
 		String fileName = "";
 		String ext = "";
 		Hashtable fileSpec = new Hashtable();
 		Hashtable tub = new Hashtable();
 		String id = "";
 		MultipartRequest multi = null;
 		File newFile = new File();
 		newFile.persist();
 		id = newFile.getFileId();
 		tub.put("FileId", id);
 
 		try {
 			if (id == null || id == "")
 				throw new CreateMetaInfoException();
 			multi = new MultipartRequest(ctx.getRequest(), contentPath, 10 * 1024 * 1024); // 10MB Max
 			Enumeration files = multi.getFileNames();
 
 			//is it a file that's being uploaded?
 			if ("uploadFile".equals(multi.getParameter("submitType"))) {
 				String name = (String) files.nextElement();
 				//				String type = multi.getContentType(name);
 				// rename the file to {id}.ext
 				String oldFileName = multi.getFilesystemName(name);
 				java.io.File f = multi.getFile(name);
 				ext = oldFileName.toLowerCase().substring(oldFileName.lastIndexOf("."));
 				fileName = id + ext;
 				f.renameTo(new java.io.File(contentPath + java.io.File.separatorChar + fileName));
 			} else if (("typeText".equals(multi.getParameter("submitType")))) { //if not, is it text that's being typed in?
 				ext = ".html";
 				fileName = id + ext;
 				FileWriter newTextFile = new FileWriter(contentPath + java.io.File.separatorChar + fileName);
 				String theFileText = multi.getParameter("typeFile");
 				String theFileHtml = theFileText.replaceAll("\n", "<P>"); //lineFeedToParagraph(theFileText);
 				String fileContent = "<html><HEAD><TITLE>" + multi.getParameter("Title") + "</TITLE></HEAD><body><b><font size=+1>" + multi.getParameter("Title") + "</font></b><P><i>" + multi.getParameter("Author") + "</i><P>" + theFileHtml + "</body></html>";
 
 				newTextFile.write(fileContent);
 				newTextFile.close();
 			}
 
 			// check if supported mime type
 			if (("web".equals(multi.getParameter("submitType")))) {
 				ext = ".html";
 			}
 			fileSpec = (Hashtable) fileSpecs.get(ext);
 			if (fileSpec == null) { // not a supported filetype
 				throw new UnsupportedFileTypeException();
 			}
 			// save the meta-info into the ejb
 			newFile.setSubmitter(multi.getParameter("Submitter"));
 			tub.put("Submitter", multi.getParameter("Submitter"));
 			newFile.setAuthor(multi.getParameter("Author"));
 			tub.put("Author", multi.getParameter("Author"));
 			newFile.setContact(multi.getParameter("Contact"));
 			tub.put("Contact", multi.getParameter("Contact"));
 			newFile.setTitle(multi.getParameter("Title"));
 			tub.put("Title", multi.getParameter("Title"));
 			newFile.setKeywords(multi.getParameter("Keywords"));
 			tub.put("Keywords", multi.getParameter("Keywords"));
 			newFile.setSummary(multi.getParameter("Summary"));
 			tub.put("Summary", multi.getParameter("Summary"));
 			newFile.setModMsg(multi.getParameter("ModMsg"));
 			tub.put("ModMsg", multi.getParameter("ModMsg"));
 			newFile.setLanguage(multi.getParameter("Language"));
 			tub.put("Language", multi.getParameter("Language"));
 			java.util.Date nowDate = new java.util.Date();
 			//java.sql.Date sqldate = new java.sql.Date(nowDate.getTime());
 			newFile.setDateAdded(nowDate);
 			tub.put("DateAdded", nowDate);
 			newFile.setQuality("C");
 			tub.put("Quality", "C");
 			String mime = (String) fileSpec.get("Mime");
 			newFile.setMime(mime);
 			tub.put("Mime", mime);
 			String url = null;
 			if (("web".equals(multi.getParameter("submitType")))) {
 				url = multi.getParameter("webLink");
 				if ((!url.startsWith("http://"))&&(!url.startsWith("ftp://"))&&(!url.startsWith("https://"))) {
 					url = "http://"+url;
 				}
 			} else {
 				url = "/cms/content/" + id + ext;
 			}
 			tub.put("submitType", (multi.getParameter("submitType")));
 			newFile.setUrl(url);
 			tub.put("Url", url);
 			newFile.persist();
 			//if (tub==null) {
 			//	log.error("tub is null!  Throwing...");
 			//	throw new SaveMetaInfoException();
 			//}
 			assocCat(id, multi.getParameter("catId"));
 
 			//index the file for searching
 			CmsIndex.add(tub, fileSpecs);
 			tub.put("View", "/servlet/CmsController?action=browse&catId=" + multi.getParameter("catId"));
 			ctx.setReturnValue(tub);
 			ctx.goToView("popupUploadDone");
 
 		} catch (CreateMetaInfoException cmie) {
 			log.error(cmie);
 			ctx.goToView("uploadError");
 		} catch (UnsupportedFileTypeException ufte) {
 			log.error(ufte);
 			uploadRollback(fileName);
 			tub.put("FileName", fileName);
 			ctx.setReturnValue(tub);
 			ctx.goToView("fileTypeNotSupported");
 		} catch (Exception e) {
 			log.error(e, e);
 		}
 
 	}
 
 	private void uploadRollback(String fileName) {
 		//delete ejb
 		String id = fileName.substring(0, fileName.lastIndexOf("."));
 		new File(id).delete();
 		// delete file if found
 		java.io.File f = new java.io.File(contentPath + java.io.File.separatorChar + fileName);
 		if (f.exists())
 			f.delete();
 	}
 
 	public void showReplace(ActionContext ctx) {
 		try {
 			Hashtable tub = ObjectHashUtil.obj2hash(new File(ctx.fetchId()));
 			tub.put("CatReturnTo", ctx.getInputString("catReturnTo"));
 			ctx.setReturnValue(tub);
 			ctx.goToView("popupReplaceResource");
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform showReplace action.", e);
 		}
 	}
 
 	public void replaceResource(ActionContext ctx) {
 		try {
 			try {
 				// upload the file
 				MultipartRequest multi = new MultipartRequest(ctx.getRequest(), contentPath, 10 * 1024 * 1024); // 10MB Max
 				Enumeration files = multi.getFileNames();
 				String name = (String) files.nextElement();
 				//				String type = multi.getContentType(name);
 				String id = multi.getParameter("id");
 
 				// delete the existing file
 				Hashtable ofh = ObjectHashUtil.obj2hash(new File(id));
 				String oldUrl = (String) ofh.get("Url");
 				String oldExt = oldUrl.toLowerCase().substring(oldUrl.lastIndexOf("."));
 				java.io.File curF = new java.io.File(contentPath + java.io.File.separatorChar + id + oldExt);
 				curF.delete();
 
 				// rename the file to {id}.ext
 				String unnamedFile = multi.getFilesystemName(name);
 				java.io.File f = multi.getFile(name);
 				String ext = unnamedFile.toLowerCase().substring(unnamedFile.lastIndexOf("."));
 				String fileName = id + ext;
 				f.renameTo(new java.io.File(contentPath + java.io.File.separatorChar + fileName));
 				log.info("file: " + f.getName() + " renamed to : " + contentPath + java.io.File.separatorChar + fileName);
 
 				// change the meta-info
 				Hashtable values = new Hashtable();
 				values.put("Submitter", multi.getParameter("Submitter"));
 				Hashtable fileSpec = (Hashtable) fileSpecs.get(ext);
 				String mime = (String) fileSpec.get("Mime");
 				values.put("Mime", mime);
 				String url = "/cms/content/" + id + ext;
 				values.put("Url", url);
 
 				File newFile = new File(id);
 				ObjectHashUtil.hash2obj(values, newFile);
 				newFile.persist();
 				Hashtable tub = ObjectHashUtil.obj2hash(newFile);
 
 				// replace the search index info
 				CmsIndex.update(tub, fileSpecs);
 
 				// return to the right view
 				if (multi.getParameter("catReturnTo").equals("moderate")) {
 					tub.put("View", "/servlet/CmsController?action=moderateReview&id=" + id);
 				} else {
 					tub.put("View", "/servlet/CmsController?action=fileInfo&id=" + id + "&catId=" + multi.getParameter("catReturnTo"));
 				}
 				ctx.goToView("popupSuccess");
 			} catch (IOException ioe) {
 				//throw new FileUploadException();
 				log.error(ioe);
 				ctx.goToView("uploadError");
 			}
 
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToView("uploadError");
 			//ctx.goToErrorView();
 			log.error("Failed to perform replaceResource action.", e);
 		}
 	}
 
 	private void assocCat(String fileId, String catId) {
 		new Category(catId).assocFile(fileId);
 	}
 
 	public void moderate(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults("moderate");
 
 			FileCategory fc = new FileCategory();
 			String[] fields = { "CategoryId", "Path", "FileId", "Title", "Submitter", "DateAdded" };
 			String query = "moderatedYet = 'F' OR moderatedYet = '' ORDER BY path, title";
 			Vector fs = (Vector) ObjectHashUtil.list(fc.selectList(query), fields);
 
 			ar.addCollection("Files", fs);
 			ctx.setReturnValue(ar);
 			ctx.goToView("moderate");
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform moderate action.", e);
 		}
 
 	}
 
 	public void moderateReview(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults("moderateReview");
 			ar.addHashtable("f", ObjectHashUtil.obj2hash(new File(ctx.fetchId())));
 			String query = "cmsFileId = '" + ctx.fetchId() + "' order by path";
 			Category cat = new Category();
 			cat.changeTargetTable("cms_viewfileidcategories");
 			Collection cats = ObjectHashUtil.list(cat.selectList(query));
 			ar.addCollection("cats", cats);
 			ctx.setReturnValue(ar);
 			ctx.goToView("moderateReview");
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform review action.", e);
 		}
 	}
 
 	public void saveReview(ActionContext ctx) {
 		try {
 			File file = new File(ctx.fetchId());
 			ObjectHashUtil.hash2obj(ctx.getHashedRequest(), file);
 			file.persist();
 			CmsIndex.update(ObjectHashUtil.obj2hash(file), fileSpecs);
 
 			// this needs to know what view we are coming from NOT the last view loaded
 			// for now, if there is a catId then we know we are from the edit page
 			if (ctx.getInputString("catId") != null) {
 				fileInfo(ctx);
 			} else {
 				moderate(ctx);
 			}
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform saveReview action.", e);
 		}
 	}
 
 	public void edit(ActionContext ctx) {
 		try {
 			// get file info
 			ActionResults ar = new ActionResults("edit");
 			Hashtable f = ObjectHashUtil.obj2hash(new File(ctx.fetchId()));
 			ar.addHashtable("f", f);
 
 			// set submitter permissions
 			//			HttpSession session = ctx.getSession();
 			Hashtable profile = (Hashtable) ctx.getSessionValue("profile");
 			String submitter = (String) profile.get("FirstName") + " " + (String) profile.get("LastName");
 			if (submitter.equals((String) f.get("Submitter")) & !usersRoles.containsKey(profile.get("UserName").toString().toLowerCase())) {
 				ar.putValue("Submitter", "true");
 			} else {
 				ar.putValue("Submitter", "false");
 			}
 
 			// add cats
 			String query = "cmsFileId = '" + ctx.fetchId() + "' order by path";
 			Category cat = new Category();
 			cat.changeTargetTable("cms_viewfileidcategories");
 			Collection cats = ObjectHashUtil.list(cat.selectList(query)); // _cmsCategoryAdaptor.list(query);
 			ar.addCollection("cats", cats);
 
 			ar.putValue("CatReturnTo", ctx.getInputString("catReturnTo"));
 			ctx.setReturnValue(ar);
 			ctx.goToView("edit");
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform edit action.", e);
 		}
 	}
 
 	public void saveEdit(ActionContext ctx) {
 		try {
 			File file = new File(ctx.fetchId());
 			ObjectHashUtil.hash2obj(ctx.getHashedRequest(), file);
 			file.persist();
 			Hashtable f = ObjectHashUtil.obj2hash(file);
 			CmsIndex.update(f, fileSpecs);
 			fileInfo(ctx);
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform saveEdit action.", e);
 		}
 	}
 
 	public void deleteResource(ActionContext ctx) {
 		try {
 			//delete ejb
 			File file = new File(ctx.fetchId());
 			String url = file.getUrl();
 			file.delete();
 
 			// delete file if found
 			String fileName = url.substring(url.lastIndexOf("/"));
 			java.io.File f = new java.io.File(contentPath + "/" + fileName);
 			if (f.exists()) {
 				f.delete();
 			}
 			// remove from search index
 			//CmsIndex.remove(id); -- Lucene bug
 			CmsIndex.remove(url);
 
 			// this needs to know what view we are coming from NOT the last view loaded
 			// for now, if we have a catId, we know we came from the browse page
 			if (ctx.getInputString("catId") != null) {
 				browse(ctx);
 			} else {
 				moderate(ctx);
 			}
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform moderateDeleteResource action.", e);
 		}
 	}
 
 	public void fileInfo(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults("edit");
 
 			//get fileInfo
 			File file = new File();
 			try {
 				file = new File(ctx.fetchId());
 			} catch (IllegalArgumentException e) { // No need to log and send out error email
 				ctx.setError(e.getMessage());
 				ctx.goToErrorView();
 				return;
 			}
 			Hashtable f = ObjectHashUtil.obj2hash(file);
 			ar.addHashtable("f", f);
 
 			// categories
 			Collection cats = ObjectHashUtil.list(file.getCategories());
 
 			// file spec note
 			String url = (String) f.get("Url");
 			String ext = url.toLowerCase().substring(url.lastIndexOf("."));
 			if (url.indexOf("://") != -1) {
 				ext = ".htm";
 			}
 			Hashtable fileSpec = (Hashtable) fileSpecs.get(ext);
 			ar.putValue("FileNote", (String) fileSpec.get("Note"));
 			ar.putValue("FileDesc", (String) fileSpec.get("Desc"));
 
 			ar.addCollection("cats", cats);
 
 			// set ReturnTo
 			String catReturnTo;
 			if (ctx.getInputString("catReturnTo").equals("searchResults")) {
 				Hashtable cat = (Hashtable) cats.iterator().next();
 				catReturnTo = cat.get("CategoryId").toString();
 			} else {
 				catReturnTo = ctx.getInputString("catReturnTo");
 			}
 			ar.putValue("CatReturnTo", catReturnTo);
 
 			// set moderator permissions
 			Hashtable profile = (Hashtable) ctx.getSessionValue("profile");
 			if (profile != null) {
 				String submitter = (String) profile.get("FirstName") + " " + (String) profile.get("LastName");
 				if (usersRoles.containsKey(profile.get("UserName").toString().toLowerCase()) || submitter.equals((String) f.get("Submitter"))) {
 					ar.putValue("Moderator", "true");
 				} else {
 					ar.putValue("Moderator", "false");
 				}
 			} else {
 				ar.putValue("Moderator", "false");
 			}
 
 			ctx.setReturnValue(ar);
 			ctx.goToView("fileInfo");
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform fileInfo action.", e);
 		}
 	}
 
 	public void browseCat(ActionContext ctx) {
 		try {
 			ActionResults ar = new ActionResults("browseCat");
 			String catId = "1"; //Tree Root
 
 			//browse from the supplied catId
 			if (ctx.getInputString("catId") != null)
 				catId = ctx.getInputString("catId");
 
 			//put subcategories in the tub
 			String query = "parentCategory = '" + catId + "' order by catName";
 			Collection subCats = ObjectHashUtil.list(new Category().selectList(query));
 
 			ar.addCollection("subCats", subCats);
 
 			// add more values to the tub
 			ar.putValue("CategoryId", catId);
 			Hashtable cat = ObjectHashUtil.obj2hash(new Category(catId));
 			ar.putValue("CatName", (String) cat.get("CatName"));
 			ar.putValue("Path", (String) cat.get("Path"));
 			ar.putValue("id", ctx.getInputString("id")); //file id
 			ar.putValue("CatReturnTo", ctx.getInputString("catReturnTo"));
 
 			ctx.setReturnValue(ar);
 			ctx.goToView("browseCat");
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform browseCat action.", e);
 		}
 	}
 
 	public void addToCategory(ActionContext ctx) {
 		try {
 			Hashtable tub = new Hashtable();
 			assocCat(ctx.getInputString("id"), ctx.getInputString("catId"));
 			if (ctx.getInputString("catReturnTo").equals("moderate")) {
 				tub.put("View", "/servlet/CmsController?action=moderateReview&id=" + ctx.getInputString("id"));
 			} else {
 				tub.put("View", "/servlet/CmsController?action=fileInfo&catReturnTo=" + ctx.getInputString("catReturnTo") + "&id=" + ctx.getInputString("id"));
 			}
 
 			ctx.setReturnValue(tub);
 			ctx.goToView("popupSuccess");
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform addToCategory action.", e);
 		}
 	}
 
 	public void removeFromCategory(ActionContext ctx) {
 		try {
 			File f = new File(ctx.getInputString("id"));
 			f.dissocCategory(ctx.getInputString("catId"));
 
 			if (ctx.getInputString("catReturnTo").equals("moderate")) {
 				moderateReview(ctx);
 			} else {
 				fileInfo(ctx);
 			}
 
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform removeFromCategory action.", e);
 		}
 	}
 
 	public void showAddCategory(ActionContext ctx) {
 		try {
 
 			Hashtable tub = new Hashtable();
 			tub.put("catId", ctx.getInputString("catId"));
 			ctx.setReturnValue(tub);
 			ctx.goToView("addCategory");
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform showAddCategory action.", e);
 		}
 	}
 
 	public void addCategory(ActionContext ctx) {
 		try {
 			try {
 				Category coChild = new Category();
 				coChild.persist();
 				String categoryName = ctx.getInputString("CatName").replace(':', ';');
 				coChild.setCatName(categoryName);
 
 				String parentId = ctx.getInputString("catId");
 				coChild.setParentCategory(new Integer(parentId));
 				if (!parentId.equals("1")) {
 					Category coParent = new Category(parentId);
 					coChild.setPath(coParent.getPath() + ":" + categoryName);
 					coChild.setPathId(coParent.getPathId() + ":" + coChild.getCategoryId());
 				} else {
 					coChild.setPath(categoryName);
 					coChild.setPathId(String.valueOf(coChild.getCategoryId()));
 				}
 				coChild.persist();
 
 			} catch (Exception e) {
 				log.error(e, e);
 			}
 			Hashtable tub = new Hashtable();
 			tub.put("View", "/servlet/CmsController?action=browse&catId=" + ctx.getInputString("catId"));
 			ctx.setReturnValue(tub);
 			ctx.goToView("popupSuccess");
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform addCategory action.", e);
 		}
 	}
 
 	public void showDeleteCategory(ActionContext ctx) {
 		try {
 			Hashtable tub = ObjectHashUtil.obj2hash(new Category(ctx.fetchId()));
 			ctx.setReturnValue(tub);
 			ctx.goToView("deleteCategory");
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform showDeleteCategory action.", e);
 		}
 	}
 
 	public void deleteCategory(ActionContext ctx) {
 		try {
 			Hashtable tub = new Hashtable();
 
 			Category coChild = new Category(ctx.fetchId());
 			Category coParent = new Category(coChild.getParentCategory().toString());
 			coChild.delete();
 			String parentId = String.valueOf(coParent.getCategoryId());
 
 			tub.put("View", "/servlet/CmsController?action=browse&catId=" + parentId);
 			ctx.setReturnValue(tub);
 			ctx.goToView("popupSuccess");
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform deleteCategory action.", e);
 		}
 	}
 
 	public void showRenameCategory(ActionContext ctx) {
 		try {
 			Hashtable tub = ObjectHashUtil.obj2hash(new Category(ctx.fetchId()));
 			ctx.setReturnValue(tub);
 			ctx.goToView("renameCategory");
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform showRenameCategory action.", e);
 		}
 	}
 
 	public void renameCategory(ActionContext ctx) {
 		try {
 			String catID = ctx.fetchId();
 			String newCatName = ctx.getInputString("CatName", true);
 			Category current = new Category(catID);
 			current.setCatName(newCatName);
 			current.persist();
 
 			// need to also find all the subcategories (ALL of them) and change the path
 			String query = "pathId like '" + current.getPathId() + "%' order by catName";
 			Collection subCats = new Category().selectList(query);
 			Iterator catsToRename = subCats.iterator();
 			while (catsToRename.hasNext()) {
 				Category co = (Category) catsToRename.next();
 				String newPath = generatePath(co);
 				co.setPath(newPath);
 				co.persist();
 			}
 
 			ctx.goToView("popupSuccess");
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform renameCategory action.", e);
 		}
 	}
 
 	public void getFile(ActionContext ctx) throws ServletException, IOException {
 		try {
 			HttpServletResponse response = ctx.getResponse();
 
 			/* removed because it didn't work quite right -- use redirect instead
 			java.io.FileInputStream in = null;
 			ServletOutputStream op = null;
 			*/
 
 			try {
 				Hashtable fh = ObjectHashUtil.obj2hash(new File(ctx.fetchId()));
 				String Url = (String) fh.get("Url");
 
 				/* removed because it didn't work quite right -- redirect instead
 				String fileName = Url.substring(Url.lastIndexOf("/"));
 				String ext = Url.substring(Url.lastIndexOf("."));
 
 				in = new java.io.FileInputStream(contentPath + "/" + fileName);
 				response.setHeader ("Content-Disposition", "attachment; filename=\""+ (String)fh.get("Title") + ext + "\";");
 				response.setContentType ((String)fh.get("Mime"));
 				//response.setContentLength(lfile);
 				//response.setHeader("Cache-Control", "no-cache");
 				op = response.getOutputStream ();
 				int length, sentsize = 0;
 				byte buf[] = new byte[ 1024 ];
 				while ((in != null) && ((length = in.read(buf)) != -1)) {
 				  op.write(buf,0,length);
 				}
 				*/
 
 				// set file access info in the ejb
 				Integer accessCount = (Integer) fh.get("AccessCount");
 				accessCount = new Integer(accessCount.intValue() + 1);
 				try {
 					File fo = new File(ctx.fetchId());
 					fo.setAccessCount(accessCount.intValue());
 					fo.setLastAccessed(new java.sql.Date(new Date().getTime()));
 					fo.persist();
 				} catch (java.lang.Exception e) {
 					log.error(e, e);
 				}
 
 				response.sendRedirect(Url);
 
 			} catch (IOException ioe) {
 				log.error(ioe);
 			} finally {
 				/* removed because it didn't work quite right -- redirect instead
 				if (op != null)
 					op.close();
 				if (in != null)
 					in.close();
 				*/
 			}
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform getFile action.", e);
 		}
 	}
 
 	public void getFileRedirect(ActionContext ctx) throws ServletException, IOException {
 		try {
 			ActionResults ar = new ActionResults();
 
 			try {
 				Hashtable fh = ObjectHashUtil.obj2hash(new File(ctx.fetchId()));
 				String Url = (String) fh.get("Url");
 
 				// set file access info in the ejb
 				Integer accessCount = (Integer) fh.get("AccessCount");
 				accessCount = new Integer(accessCount.intValue() + 1);
 				try {
 					File fo = new File(ctx.fetchId());
 					fo.setAccessCount(accessCount.intValue());
 					fo.setLastAccessed(new java.sql.Date(new Date().getTime()));
 					fo.persist();
 				} catch (java.lang.Exception e) {
 					log.error(e, e);
 				}
 
 				ar.putValue("url", Url);
 				ctx.setReturnValue(ar);
 				ctx.goToView("downloadRedirect");
 
 			} catch (Exception e) {
 				log.error(e);
 			}
 		} catch (Exception e) {
 			ctx.setError();
 			ctx.goToErrorView();
 			log.error("Failed to perform getFile action.", e);
 		}
 	}
 
 	public String generatePath(Category co) throws java.rmi.RemoteException {
 		boolean doLoop = true;
 		Category current = new Category(co.getParentCategory().toString());
 		String path = co.getCatName();
 		if ("1".equals(co.getParentCategory().toString()))
 			doLoop = false;
 		while (doLoop) {
 			path = current.getCatName() + ":" + path;
 			if ("1".equals(current.getParentCategory().toString()))
 				doLoop = false;
 			else
 				current = new Category(current.getParentCategory().toString());
 		}
 		return path;
 	}
 
 }
