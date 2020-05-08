 package com.braintest.tag;
 
 import java.io.IOException;
 import java.util.Collection;
 
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.jsp.JspException;
 import javax.servlet.jsp.PageContext;
 import javax.servlet.jsp.tagext.JspFragment;
 import javax.servlet.jsp.tagext.SimpleTagSupport;
 
 import org.apache.commons.io.FilenameUtils;
 import org.apache.commons.lang.StringUtils;
 
 import com.braintest.api.WalkerService;
 import com.braintest.model.NodeModel;
 import com.braintest.service.impl.FileWalkerService;
 
 public class WalkerTag extends SimpleTagSupport {
 
     private static final String VIEW = "<div class=\"wfm\"><div class=\"wfm-parent\">%s</div>%s<ul>%s</ul></div>";
 
     private static final String NODE = "<li><span class=\"wfm-%s\">%s</span></li>";
 
     private static final String PARENT_NODE = "<div><a class=\"wfm-parent\" href=\"%s?path=%s\">..</a></div>";
 
    private static final String EXPANDABLE_NODE = "<li><a class=\"wfm-expand\" href=\"%s?path=%s\"><span class=\"%s\">%s</span></a></li>";
 
     private final WalkerService walkerService;
 
     private String path;
 
     public WalkerTag() {
         walkerService = new FileWalkerService();
     }
 
     @Override
     public void doTag() throws JspException, IOException {
         super.doTag();
 
         final String separator = walkerService.getSeparator();
         final PageContext pageContext = (PageContext) getJspContext();
 
         if (StringUtils.isBlank(path)) {
             // Get path from request or if it's empty then get default root path
             path = StringUtils.defaultIfEmpty(getPathFromRequest(pageContext), separator);
         }
 
         // Init parent and child path
         final String parentPath = FilenameUtils.getFullPathNoEndSeparator(path);
         final String childPath;
         if (path.endsWith(separator)) {
             childPath = path;
         } else {
             childPath = path + separator;
         }
 
        final Collection<NodeModel> nodes = walkerService.walk(path);
 
         final JspFragment jspBody = getJspBody();
         if (jspBody != null) {
             getJspContext().setAttribute("wfmParent", parentPath);
             getJspContext().setAttribute("wfmPath", childPath);
             getJspContext().setAttribute("wfmNodes", nodes);
             jspBody.invoke(null);
         } else {
             final String body = renderBody(parentPath, childPath, nodes, pageContext);
             pageContext.getOut().write(body);
         }
     }
 
     private String getPathFromRequest(PageContext pageContext) {
         HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
         return request.getParameter("path");
     }
 
     /**
      * Render Tag body
      *
      * @param nodes collection of node models
      * @param pageContext object
      * @return html string
      */
     private String renderBody(String parentPath, String childPath, Collection<NodeModel> nodes, PageContext pageContext) {
 
         final String contextPath = pageContext.getServletContext().getContextPath();
         final StringBuffer nodesView = new StringBuffer();
 
         final String parentLink;
         if (!childPath.equals(parentPath)) {
             parentLink = String.format(PARENT_NODE, contextPath, parentPath);
         } else {
             parentLink = "";
         }
 
         for (NodeModel node : nodes) {
 
             String nodeName = node.getName();
             String nodeType = node.getType();
             if (node.isHasChild()) {
                 final String href = childPath + nodeName;
                 nodesView.append(String.format(EXPANDABLE_NODE, contextPath, href, nodeType, nodeName));
             } else {
                 nodesView.append(String.format(NODE, nodeType, nodeName));
             }
         }
         return String.format(VIEW, path, parentLink, nodesView.toString());
     }
 
     public void setPath(String path) {
         this.path = path;
     }
 }
