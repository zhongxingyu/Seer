 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package com.cherrot.govproject.web.controller;
 
 import com.cherrot.govproject.model.Category;
 import com.cherrot.govproject.model.Comment;
 import com.cherrot.govproject.model.Link;
 import com.cherrot.govproject.model.LinkCategory;
 import com.cherrot.govproject.model.Option;
 import com.cherrot.govproject.model.Post;
 import com.cherrot.govproject.model.Tag;
 import com.cherrot.govproject.model.User;
 import com.cherrot.govproject.service.CategoryService;
 import com.cherrot.govproject.service.CommentService;
 import com.cherrot.govproject.service.LinkService;
 import com.cherrot.govproject.service.OptionService;
 import com.cherrot.govproject.service.PostService;
 import com.cherrot.govproject.service.TagService;
 import com.cherrot.govproject.service.UserService;
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import javax.inject.Inject;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.ModelAttribute;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.servlet.ModelAndView;
 
 /**
  * 关于forward: 和 redirect: ，见Spring3.1文档P474
  * @author cherrot
  */
 @Controller
 public class HomeController {
 
     @Inject
     private CategoryService categoryService;
     @Inject
     private LinkService linkService;
     @Inject
     private PostService postService;
 
     /**
      * 顶部导航栏的文章分类
      * @return
      */
     @ModelAttribute("categories")
     public List<Category> getSecondLevelCategoryList() {
         //XXX 仅用于生成测试数据！
         initData();
         return categoryService.listSecondLevelCategories(false, false);
     }
     /**
      * 友情链接分类和分类下的友情链接
      * @return
      */
     @ModelAttribute("linkCategories")
     public List<LinkCategory> getLinkCategoryList() {
         return linkService.listLinkCategories(true);
     }
 
     @RequestMapping("/")
     public ModelAndView home() {
 
         ModelAndView mav = new ModelAndView("home");
 
         //添加首页主体部分的文章群组分类和该分类的最新文章
         List<Category> topLevelCategorys = categoryService.listTopLevelCategories(true);
         mav.addObject("categoryGroups", topLevelCategorys);
         for (Category group : topLevelCategorys) {
             for (Category category : group.getCategoryList()) {
                 mav.addObject(category.getName(), postService.listNewestPostsByCategory(category, 1, 5));
             }
         }
         return mav;
     }
 
     /**
      * TODO 以下内容仅用于生成测试数据！
      */
     @Inject private CommentService commentService;
     @Inject private UserService userService;
     @Inject private OptionService optionService;
     @Inject private TagService tagService;
     private void initData() {
         try {
             userService.findByLoginName("f@f.f", false, false, false, false);
         } catch (Exception ex) {
             //创建测试用户 用户名 f@f.f 密码 fff
             User user = new User("f@f.f", "fff", 0, new Date(), "切萝卜可爱多");
             userService.create(user);
             //创建测试分类
             Category group1 = new Category(0, "分类群组1", "group1");
             Category group2 = new Category(0, "分类群组2", "group2");
             Category group3 = new Category(0, "分类群组3", "group3");
             Category group4 = new Category(0, "分类群组4", "group4");
             Category group5 = new Category(0, "分类群组5", "group5");
             Category groupHidden = new Category(0, "隐藏群组", "hidden");
             Category groupMultimedia = new Category(0, "多媒体分组", "multimedia");
            Category wenxue = new Category(0, "文学", "wenxue");
            Category shufa = new Category(0, "书法", "shufa");
            Category meishu = new Category(0, "美术", "meishu");
            Category sheying = new Category(0, "摄影", "sheying");
          //  Category zongjiao = new Category(0, "宗教", "zongjiao");
             categoryService.create(group1);
             categoryService.create(group2);
             categoryService.create(group3);
             categoryService.create(group4);
             categoryService.create(group5);
             categoryService.create(groupHidden);
             categoryService.create(groupMultimedia);
             wenxue.setCategoryParent(group1);
             shufa.setCategoryParent(group1);
             meishu.setCategoryParent(group1);
             sheying.setCategoryParent(group1);
        //    zongjiao.setCategoryParent(groupHidden);
             categoryService.create(wenxue);
             categoryService.create(shufa);
             categoryService.create(meishu);
             categoryService.create(sheying);
        //   categoryService.create(zongjiao);
             //创建测试文章标签
             Tag tag = new Tag(0, "我是标签", "testtag");
             Tag tag1 = new Tag(0, "我也是标签", "test1");
             tagService.create(tag);
             tagService.create(tag1);
             //创建测试文章
             Post post = new Post(new Date(), new Date(), true, 0, Post.PostStatus.PUBLISHED, Post.PostType.POST, "test", "我是文章标题", "我是文章内容");
             post.setUser(user);
             List<Category> categoryList = new ArrayList<Category>();
             categoryList.add(wenxue);
             categoryList.add(sheying);
          //  categoryList.add(zongjiao);
             post.setCategoryList(categoryList);
             List<Tag> tagList = new ArrayList<Tag>();
             tagList.add(tag);
             tagList.add(tag1);
             post.setTagList(tagList);
             postService.create(post);
             //创建测试评论
             Comment comment = new Comment(new Date(), true, "Cherrot", "admin@cherrot.com", "http://www.cherrot.com", "127.0.0.1", "我是文章评论");
             comment.setPost(post);
             commentService.create(comment);
 //            //创建测试日志
 //            SiteLog siteLog = new SiteLog(new Date(), "我是操作日志");
 //            siteLog.setUser(user);
 //            siteLogService.create(siteLog);
             //创建测试全局选项
             Option option = new Option("test");
             option.setOptionValue("我是全局选项");
             optionService.create(option);
             //创建测试链接分类和链接
             LinkCategory linkCategory = new LinkCategory();
             linkCategory.setName("我是友情链接分类");
             linkService.createLinkCategory(linkCategory);
             Link link = new Link("http://www.cherrot.com", "Cherrot", Link.LinkTarget._blank);
             link.setLinkCategory(linkCategory);
             linkService.create(link);
         }
     }
 }
