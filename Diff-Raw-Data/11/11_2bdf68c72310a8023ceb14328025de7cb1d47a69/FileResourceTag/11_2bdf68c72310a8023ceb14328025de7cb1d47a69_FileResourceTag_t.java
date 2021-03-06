 /*
  *  Weblounge: Web Content Management System
  *  Copyright (c) 2010 The Weblounge Team
  *  http://weblounge.o2it.ch
  *
  *  This program is free software; you can redistribute it and/or
  *  modify it under the terms of the GNU Lesser General Public License
  *  as published by the Free Software Foundation; either version 2
  *  of the License, or (at your option) any later version.
  *
  *  This program is distributed in the hope that it will be useful,
  *  but WITHOUT ANY WARRANTY; without even the implied warranty of
  *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  *  GNU Lesser General Public License for more details.
  *
  *  You should have received a copy of the GNU Lesser General Public License
  *  along with this program; if not, write to the Free Software Foundation
  *  Inc., 51 Franklin Street, Fifth Floor, Boston, MA 02110-1301, USA.
  */
 
 package ch.o2it.weblounge.taglib.resource;
 
 import ch.o2it.weblounge.common.content.ResourceURI;
 import ch.o2it.weblounge.common.content.file.FileContent;
 import ch.o2it.weblounge.common.content.file.FileResource;
 import ch.o2it.weblounge.common.content.repository.ContentRepository;
 import ch.o2it.weblounge.common.content.repository.ContentRepositoryException;
 import ch.o2it.weblounge.common.impl.content.file.FileResourceURIImpl;
 import ch.o2it.weblounge.common.impl.language.LanguageUtils;
 import ch.o2it.weblounge.common.language.Language;
 import ch.o2it.weblounge.common.site.Site;
 import ch.o2it.weblounge.taglib.WebloungeTag;
 
 import org.apache.commons.lang.StringUtils;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import javax.servlet.jsp.JspException;
 
 /**
  * This tag loads an file that is defined by an identifier or a path from the
  * content repository.
  * <p>
  * If it is found, the file is defined in the jsp context variable
  * <code>file</code>, otherwise, the tag body is skipped altogether.
  */
 public class FileResourceTag extends WebloungeTag {
 
   /** Serial version UID */
   private static final long serialVersionUID = 2047795554694030193L;
 
   /** Logging facility */
   private static final Logger logger = LoggerFactory.getLogger(FileResourceTag.class.getName());
 
   /** The file identifier */
   private String fileId = null;
 
   /** The file path */
   private String filePath = null;
 
   /**
    * Sets the file identifier.
    * 
    * @param id
    *          file identifier
    */
   public void setUuid(String id) {
     fileId = id;
   }
 
   /**
    * Sets the file path. If both path and uuid have been defined, the uuid takes
    * precedence.
    * 
    * @param path
    *          file path
    */
   public void setPath(String path) {
     filePath = path;
   }
 
   /**
    * {@inheritDoc}
    * 
    * @see javax.servlet.jsp.tagext.BodyTagSupport#doStartTag()
    */
   public int doStartTag() throws JspException {
     Site site = request.getSite();
     Language language = request.getLanguage();
 
     ContentRepository repository = site.getContentRepository();
     if (repository == null) {
       logger.debug("Unable to load content repository for site '{}'", site);
       response.invalidate();
       return SKIP_BODY;
     }
 
     // Create the file uri, either from the id or the path. If none is
     // specified, and we are not in jsp compilation mode, issue a warning
     ResourceURI uri = null;
     if (StringUtils.isNotBlank(fileId)) {
       uri = new FileResourceURIImpl(site, null, fileId);
     } else if (StringUtils.isNotBlank(filePath)) {
       uri = new FileResourceURIImpl(site, filePath, null);
    } else {
       logger.debug("Neither uuid nor path were specified for file");
       return SKIP_BODY;
     }
 
     // Try to load the file from the content repository
     try {
       if (!repository.exists(uri)) {
         logger.warn("Non existing file {} requested on {}", uri, request.getUrl());
         return SKIP_BODY;
       }
     } catch (ContentRepositoryException e) {
       logger.error("Error trying to look up file {} from {}", fileId, repository);
       return SKIP_BODY;
     }
 
     FileResource file = null;
     FileContent fileContent = null;
 
     // Store the result in the jsp page context
     try {
       file = (FileResource) repository.get(uri);
       language = LanguageUtils.getPreferredLanguage(file, request, site);
       file.switchTo(language);
       fileContent = file.getContent(language);
     } catch (ContentRepositoryException e) {
       logger.warn("Error trying to load file " + uri + ": " + e.getMessage(), e);
       return SKIP_BODY;
     }
 
     // TODO: Check the permissions
 
     // Store the file and the file content in the request
     pageContext.setAttribute(FileResourceTagExtraInfo.FILE, file);
     pageContext.setAttribute(FileResourceTagExtraInfo.FILE_CONTENT, fileContent);
 
     return EVAL_BODY_INCLUDE;
   }
 
   /**
    * {@inheritDoc}
    * 
    * @see javax.servlet.jsp.tagext.BodyTagSupport#doEndTag()
    */
   public int doEndTag() throws JspException {
     pageContext.removeAttribute(FileResourceTagExtraInfo.FILE);
     pageContext.removeAttribute(FileResourceTagExtraInfo.FILE_CONTENT);
     return super.doEndTag();
   }
 
   /**
    * {@inheritDoc}
    * 
    * @see ch.o2it.weblounge.taglib.WebloungeTag#reset()
    */
   @Override
   protected void reset() {
     super.reset();
     fileId = null;
     filePath = null;
   }
 
 }
