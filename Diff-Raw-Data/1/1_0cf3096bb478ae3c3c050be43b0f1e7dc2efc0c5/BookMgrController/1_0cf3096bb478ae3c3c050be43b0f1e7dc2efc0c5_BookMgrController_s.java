 package com.sjtu.onlinelibrary.web.admin;
 
 import com.sjtu.onlinelibrary.DataAccessException;
 import com.sjtu.onlinelibrary.entity.Book;
 import com.sjtu.onlinelibrary.service.IBookService;
 import com.sjtu.onlinelibrary.service.IClassificationService;
 import com.sjtu.onlinelibrary.util.LangUtil;
 import com.sjtu.onlinelibrary.web.viewmodel.BookEditModel;
 import com.sjtu.onlinelibrary.web.viewmodel.ClassificationEditModel;
 import com.sjtu.onlinelibrary.web.viewmodel.Pager;
 import org.apache.log4j.Logger;
 import org.springframework.stereotype.Controller;
 import org.springframework.validation.BindingResult;
 import org.springframework.web.bind.annotation.*;
 import org.springframework.web.servlet.ModelAndView;
 
 import javax.validation.Valid;
 import java.util.HashMap;
 import java.util.Map;
 
 /**
  * Created with IntelliJ IDEA.
  * User: Administrator
  * Date: 13-8-3
  * Time: 下午1:17
  */
 @Controller
 @RequestMapping("/admin/book")
 public class BookMgrController {
     public static final String ADMIN_BOOK_MGR_LIST = "admin/bookMgr/list";
     public static final String ADMIN_BOOK_MGR_EDIT = "admin/bookMgr/edit";
     public static final String PAGE_DATA = "pageData";
     private IBookService bookService;
     private IClassificationService classificationService;
     private Logger logger = Logger.getLogger(BookMgrController.class);
 
     public void setClassificationService(final IClassificationService classificationService) {
         this.classificationService = classificationService;
     }
 
     public void setBookService(final IBookService bookService) {
         this.bookService = bookService;
     }
 
     @RequestMapping("/list.do")
     public ModelAndView list(@RequestParam(value = "pageIndex", required = false) final String pageIndex) {
         try {
             int index = 0;
             if (!LangUtil.isNullOrEmpty(pageIndex)) {
                 index = Integer.parseInt(pageIndex);
             }
             final Pager<BookEditModel> books = this.bookService.findAll(index);
             return new ModelAndView(ADMIN_BOOK_MGR_LIST, PAGE_DATA, books);
 
         } catch (DataAccessException e) {
             return new ModelAndView("error");
         }
     }
 
     @RequestMapping("/create.do")
     public ModelAndView create() throws DataAccessException {
         final Map<String, Object> map = getMapForEdit();
         map.put("book", new BookEditModel("创建书籍", new Book()));
         return new ModelAndView(ADMIN_BOOK_MGR_EDIT, map);
     }
 
     @RequestMapping("/{id}/edit.do")
     public ModelAndView edit(@PathVariable("id") final String id) {
         try {
             final BookEditModel book = this.bookService.findById(id);
             final Map<String, Object> map = getMapForEdit();
             map.put("book", book);
             return new ModelAndView(ADMIN_BOOK_MGR_EDIT, map);
 
         } catch (DataAccessException e) {
             return new ModelAndView("error");
         }
 
     }
 
     @RequestMapping(value = "/save.do", method = RequestMethod.POST)
     public ModelAndView save(@Valid @ModelAttribute("book") final BookEditModel bookEditModel, final BindingResult bindingResult) throws DataAccessException {
         if (bindingResult.hasErrors()) {
             final Map<String, Object> map = getMapForEdit();
             bookEditModel.setEditType("编辑书籍");
             map.put("book", bookEditModel);
             return new ModelAndView(ADMIN_BOOK_MGR_EDIT, map);
         }
         ClassificationEditModel classificationEditModel = this.classificationService.findById(bookEditModel.getCategoryId());
 
         BookEditModel book = this.bookService.findById(bookEditModel.getId());
         if (book.innerBookEntity() == null) {
             book = new BookEditModel("", new Book());
         }
         book.setCategoryId(classificationEditModel.getId());
         book.setCategory(classificationEditModel.getClassificationName());
         book.setAuthor(bookEditModel.getAuthor());
         book.setBookCoverImgPath(bookEditModel.getBookCoverImgPath());
         book.setBookNumber(bookEditModel.getBookNumber());
         book.setPrice((bookEditModel.getPrice()));
         book.setPublisher(bookEditModel.getPublisher());
         book.setPublishDate(bookEditModel.getPublishDate());
         book.setKeywords(bookEditModel.getKeywords());
         book.setDescription(bookEditModel.getDescription());
         bookService.save(book.innerBookEntity());
         final Map<String, Object> map = new HashMap<String, Object>();
         map.put("message", "保存书籍成功！");
         map.put("url", "/admin/book/list.do");
         return new ModelAndView("forward:/success.jsp", map);
 
     }
 
     @RequestMapping("/{id}/delete.do")
     public ModelAndView delete(@PathVariable("id") final String id) {
         String result = "删除书籍失败！";
         if (bookService.delete(id)) {
             result = "删除书籍成功！";
         }
         final Map<String, Object> map = new HashMap<String, Object>();
         map.put("message", result);
         map.put("url", "/admin/book/list.do");
         return new ModelAndView("forward:/success.jsp", map);
     }
 
     private Map<String, Object> getMapForEdit() throws DataAccessException {
         final Map<String, Object> map = new HashMap<String, Object>();
         map.put("categories", this.classificationService.findAll(0).getList());
         return map;
     }
 }
