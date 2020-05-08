 /*
  * cilla - Blog Management System
  *
  * Copyright (C) 2012 Richard "Shred" Körber
  *   http://cilla.shredzone.org
  *
  * This program is free software: you can redistribute it and/or modify
  * it under the terms of the GNU Affero General Public License as published
  * by the Free Software Foundation, either version 3 of the License, or
  * (at your option) any later version.
  *
  * This program is distributed in the hope that it will be useful,
  * but WITHOUT ANY WARRANTY; without even the implied warranty of
  * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
  * GNU General Public License for more details.
  *
  * You should have received a copy of the GNU Affero General Public License
  * along with this program.  If not, see <http://www.gnu.org/licenses/>.
  */
 package org.shredzone.cilla.web.info;
 
 import java.io.UnsupportedEncodingException;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.HashMap;
 import java.util.LinkedList;
 import java.util.List;
 import java.util.Map;
 
 import javax.annotation.Resource;
 
 import org.shredzone.cilla.core.model.Comment;
 import org.shredzone.cilla.core.model.Page;
 import org.shredzone.cilla.core.model.User;
 import org.shredzone.cilla.core.repository.CommentDao;
 import org.shredzone.cilla.web.format.TextFormatter;
 import org.springframework.cache.annotation.Cacheable;
 import org.springframework.stereotype.Service;
 import org.springframework.transaction.annotation.Transactional;
 
 /**
  * {@link PageInfoService} implementation.
  *
  * @author Richard "Shred" Körber
  */
 @Service
 @Transactional
 public class PageInfoServiceImpl implements PageInfoService {
 
     private @Resource CommentDao commentDao;
     private @Resource TextFormatter textFormatter;
 
     @Override
     @Cacheable(value = "pageInfo", key = "#page.id")
     public PageInfo getPageInfo(Page page) {
        PageInfo content = new PageInfo();
        content.setComments(Collections.unmodifiableList(getFlattenedComments(page)));
        content.setTags(Collections.unmodifiableCollection(page.getTags()));
        return content;
     }
 
     /**
      * Get {@link PageComment} in a threaded sequence.
      *
      * @param page
      *            {@link Page}
      * @return {@link PageComment} of the Page's comments
      */
     private List<PageComment> getFlattenedComments(Page page) {
         List<PageComment> roots = getCommentTree(page);
         List<PageComment> result = new ArrayList<PageComment>(roots.size());
         for (PageComment root : roots) {
             addNodeRecursive(root, result);
         }
         return result;
     }
 
     /**
      * Adds a {@link PageComment} and its children to the result list.
      *
      * @param node
      *            {@link PageComment} to add
      * @param result
      *            result list to add to
      */
     private void addNodeRecursive(PageComment node, List<PageComment> result) {
         result.add(node);
         if (node.getChildren() != null) {
             for (PageComment child : node.getChildren()) {
                 addNodeRecursive(child, result);
             }
         }
     }
 
     /**
      * Get {@link PageComment} as tree. The root comments are returned.
      *
      * @param page
      *            {@link Page}
      * @return {@link PageComment} tree of the Page's comments
      */
     private List<PageComment> getCommentTree(Page page) {
         List<PageComment> result = new LinkedList<PageComment>();
 
         Map<Long, PageComment> replyMap = new HashMap<Long, PageComment>();
 
         for (Comment comment : commentDao.fetchPublishedComments(page)) {
             PageComment pc = assemblePageComment(page, comment);
             replyMap.put(comment.getId(), pc);
 
             // Make sure the comment is at least added as root if the replyTo is not found
             boolean inserted = false;
 
             if (comment.getReplyTo() != null) {
                 long replyId = comment.getReplyTo().getId();
                 PageComment replyPc = replyMap.get(replyId);
                 if (replyPc != null) {
                     pc.setLevel(replyPc.getLevel() + 1);
                     List<PageComment> replyList = replyPc.getChildren();
                     if (replyList == null) {
                         replyList = new LinkedList<PageComment>();
                         replyPc.setChildren(replyList);
                     }
                     replyList.add(pc);
                     inserted = true;
                 }
             }
 
             if (!inserted) {
                 result.add(pc);
             }
         }
 
         return result;
     }
 
     /**
      * Assembles a {@link PageComment} instance for a {@link Comment}.
      *
      * @param page
      *            {@link Page}
      * @param comment
      *            {@link Comment} to assemble
      * @return Assembled {@link PageComment}
      */
     private PageComment assemblePageComment(Page page, Comment comment) {
         PageComment pc = new PageComment();
         pc.setId(comment.getId());
         pc.setName(comment.getName());
         pc.setUrl(comment.getUrl());
         pc.setCreation(comment.getCreation());
         pc.setText(textFormatter.format(comment.getText(), page).toString());
 
         String mail = comment.getMail();
         User creator = comment.getCreator();
 
         if (creator != null) {
             pc.setCreatorId(creator.getId());
             mail = creator.getMail();
         }
 
         if (mail != null) {
             pc.setMail(mail);
             pc.setMailhash(computeMailHash(mail));
         }
 
         return pc;
     }
 
     /**
      * Computes a mail hash that can be used at Gravatar.
      *
      * @param mail
      *            mail address
      * @return md5 hash
      */
     private static String computeMailHash(String mail) {
         try {
             MessageDigest md5 = MessageDigest.getInstance("MD5");
             md5.reset();
             md5.update(mail.trim().toLowerCase().getBytes("UTF-8"));
 
             StringBuilder digest = new StringBuilder();
             for (byte b : md5.digest()) {
                 digest.append(String.format("%02x", b & 0xFF));
             }
 
             return digest.toString();
         } catch (NoSuchAlgorithmException ex) {
             // we expect no exception, since MD5 is a standard digester
             throw new InternalError(ex.getMessage());
         } catch (UnsupportedEncodingException ex) {
             // we expect no exception, since UTF-8 is a standard encoding
             throw new InternalError(ex.getMessage());
         }
     }
 
 }
