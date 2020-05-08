 /**
   Copyright 2010 Anthony Chaves
   
   This file is part of Bookmarks.
 
   Bookmarks is free software: you can redistribute it and/or modify
   it under the terms of the GNU General Public License as published by
   the Free Software Foundation, either version 3 of the License, or
   (at your option) any later version.
 
   Bookmarks is distributed in the hope that it will be useful,
   but WITHOUT ANY WARRANTY; without even the implied warranty of
   MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
   GNU General Public License for more details.
 
   You should have received a copy of the GNU General Public License
   along with Bookmarks.  If not, see <http://www.gnu.org/licenses/>.
 */
 
 package net.anthonychaves.bookmarks.web;
 
 import org.springframework.web.bind.annotation.*;
 import org.springframework.stereotype.*;
 import org.springframework.ui.*;
 import org.springframework.beans.factory.annotation.*;
 
 import javax.persistence.*;
 import javax.servlet.http.*;
 
 import net.anthonychaves.bookmarks.models.*;
 import net.anthonychaves.bookmarks.service.*;
 
 import java.util.*;
 
 @Controller
 @RequestMapping("/bookmarks")
 public class BookmarkController {
 
   @Autowired
   UserService userService;
   
   @Autowired
   TagService tagService;
   
   @Autowired
   BookmarkService bookmarkService;
 
 	@RequestMapping(method=RequestMethod.POST)
 	public String addBookmark(@RequestParam(value="title") String title,
 	                          @RequestParam(value="url") String url,
 	                          @RequestParam(value="tags") String tags,
 	                          HttpSession session,
 	                          ModelMap model) {
 
     User u = (User) session.getAttribute("user");
     // might help to validate url here...
 
     Bookmark bookmark = new Bookmark();
     bookmark.setTitle(title);
     bookmark.setUrl(url);
     bookmark.setTags(tags);
 
     User user = userService.addBookmark(u, bookmark);
     tagService.addTags(bookmark);
     session.setAttribute("user", user);
 
     BookmarkDetail b = new BookmarkDetail(bookmark.getId(), bookmark.getTitle(), bookmark.getUrl(), bookmark.getTags());
     model.clear();
     model.addAttribute("bookmark", b);
     
     return "redirect:add_bookmark_success";
 	}
 	
 	@RequestMapping(method=RequestMethod.PUT)
 	public String updateBookmark(@RequestParam(value="id") Integer id,
 	                             @RequestParam(value="tags") String tags,
 	                             HttpSession session,
 	                             ModelMap model) {
 
     User user = (User) session.getAttribute("user");
    Object[] userBookmark = bookmarkService.updateTags(user, id, tags.replaceAll("\\s", ","));
System.out.println("tags:" + tags.replaceAll("\\s", ","));    
     user = (User)userBookmark[0];
     Bookmark bookmark = (Bookmark)userBookmark[1];
     List<String> deletedTags = (List<String>)userBookmark[2];
     
     tagService.addTags(bookmark);
     for (String t : deletedTags) {
       if (t.length() > 0) {
         tagService.deleteBookmarkFromTag(bookmark, t);
       }
     }
 
     BookmarkDetail b = new BookmarkDetail(bookmark.getId(), bookmark.getTitle(), bookmark.getUrl(), bookmark.getTags());
     model.clear();
     model.addAttribute("bookmark", b);
 
     session.setAttribute("user", user);
     
     return "add_bookmark_success";
 	}
 	
 	@RequestMapping(method=RequestMethod.DELETE)
 	public String deleteBookmark(@RequestParam(value="bookmarkId") String bookmarkId,
 	                             HttpSession session,
 	                             ModelMap model) {
 
 	  User user = (User) session.getAttribute("user");
 	  user = userService.deleteBookmark(user, bookmarkId);
 	  session.setAttribute("user", user);
 
     model.clear();
     model.addAttribute("result", bookmarkId);
 
 	  return "redirect:/b/user";
 	}
 
   @RequestMapping(method=RequestMethod.GET)
   public String getUserBookmarks(HttpSession session, ModelMap model) {
     User user = (User) session.getAttribute("user");
     user = userService.findUser(user.getId());
     
     model.clear();
     model.addAttribute("bookmarks", user.getBookmarksDetail());
     
     return "user_bookmarks";
   }
   
   @RequestMapping(method=RequestMethod.GET, value="/new")
   public String newBookmark() {
     return "add_bookmark";
   }
 	
 	public void setUserService(UserService userService) {
 	  this.userService = userService;
 	}
 	
 	public void setTagService(TagService tagService) {
 	  this.tagService = tagService;
 	}
 	
 	public void setBookmarkService(BookmarkService bookmarkService) {
 	  this.bookmarkService = bookmarkService;
 	}
 }
