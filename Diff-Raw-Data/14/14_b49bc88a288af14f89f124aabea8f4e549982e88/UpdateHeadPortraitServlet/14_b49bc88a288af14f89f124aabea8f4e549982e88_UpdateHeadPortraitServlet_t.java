 package locationshare.servlet;
 
 import java.io.IOException;
 import java.util.List;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 import locationshare.action.ProfileAction;
 import locationshare.base.servlet.BaseServlet;
 import locationshare.base.vo.BaseResultVO;
 import locationshare.common.util.ErrorCode;
 import locationshare.common.util.StringUtil;
 
 import org.apache.commons.fileupload.FileItem;
 import org.apache.commons.fileupload.disk.DiskFileItemFactory;
 import org.apache.commons.fileupload.servlet.ServletFileUpload;
 
 /**
  * Servlet implementation class UpdateBatchLocationServlet
  */
 public class UpdateHeadPortraitServlet extends BaseServlet {
 	private static final long serialVersionUID = -3062150377046319700L;
 
 	@Override
 	public String execute(HttpServletRequest req, HttpServletResponse res)
 			throws IOException {
 		String userid = req.getParameter("userid");
 		BaseResultVO vo = new BaseResultVO();
		logger.error("UpdateHeadPortraitServlet:userid:"+userid) ;
 		try {
 			DiskFileItemFactory factory = new DiskFileItemFactory();
 			ServletFileUpload upload = new ServletFileUpload(factory);
 			upload.setHeaderEncoding("UTF-8");
 			if (!ServletFileUpload.isMultipartContent(req)) {
				logger.error("not MultiPart");
 				return vo.toErrorJsonResult(ErrorCode.HEADPORTRAIT_NULL);
 			}
 			
 			@SuppressWarnings("unchecked")
 			List<FileItem> fileItems=upload.parseRequest(req);
 			
 			FileItem headportrait=null;
 			for (FileItem fileItem : fileItems) {
 				if(fileItem.getFieldName().equals("headportrait")){
					logger.error("file name:"+fileItem.getFieldName()) ;
 					headportrait = fileItem;
 				}
				if (fileItem.getFieldName().equals("userid")) {
					userid = fileItem.getString() ;
				}
 			}
 			if(headportrait==null){
				logger.error("headportrait is null!");
 				return vo.toErrorJsonResult(ErrorCode.HEADPORTRAIT_NULL);
 			}
 			
 			return profileAction.updatePortrait(userid, headportrait);
 
 		} catch (Exception e) {
 			logger.error("get image failed:" + StringUtil.getExceptionStack(e));
 			return vo.toErrorJsonResult(ErrorCode.UPDATE_HEADPORTRAIT_FAILED);
 		}
 
 	}
 
 	private ProfileAction profileAction;
 
 	public void setProfileAction(ProfileAction profileAction) {
 		this.profileAction = profileAction;
 	}
 
 }
