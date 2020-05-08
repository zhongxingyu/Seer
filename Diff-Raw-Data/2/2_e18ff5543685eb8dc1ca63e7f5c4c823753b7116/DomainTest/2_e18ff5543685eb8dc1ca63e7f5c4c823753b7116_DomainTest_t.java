 package com.retech.reader.web.domain;
 
 import com.google.inject.Inject;
 import com.google.inject.Provider;
 
 import com.retech.reader.web.domain.testdata.TestDataUtil;
 import com.retech.reader.web.server.domain.Category;
 import com.retech.reader.web.server.domain.Issue;
 import com.retech.reader.web.server.domain.Page;
 import com.retech.reader.web.server.domain.Resource;
 import com.retech.reader.web.server.domain.Section;
 import com.retech.reader.web.server.service.CategoryService;
 import com.retech.reader.web.server.service.IssueService;
 import com.retech.reader.web.server.service.PageService;
 import com.retech.reader.web.server.service.ResourceService;
 import com.retech.reader.web.server.service.SectionService;
 import com.retech.reader.web.shared.proxy.MimeType;
 
 import org.cloudlet.web.test.BaseTest;
 import org.junit.Test;
 
 import java.io.IOException;
 import java.text.DateFormat;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 
 import javax.persistence.EntityManager;
 
 public class DomainTest extends BaseTest {
 
   public static Date StringToDate(final String dateStr, final String formatStr) {
     DateFormat sdf = new SimpleDateFormat(formatStr);
     Date date = null;
 
     for (int i = 1; i <= 2; i++) {
 
       try {
         date = sdf.parse(dateStr);
       } catch (Exception e) {
         e.printStackTrace();
       };
     }
 
     return date;
   }
 
   @Inject
   private Provider<EntityManager> em;
   @Inject
   private Provider<Category> categories;
 
   @Inject
   private CategoryService categoryService;
   @Inject
   private Provider<Issue> issues;
 
   @Inject
   private IssueService issueService;
   @Inject
   private Provider<Page> pages;
   @Inject
   private PageService pageService;
   @Inject
   private Provider<Section> sections;
   @Inject
   private SectionService sectionService;
   @Inject
   private Provider<Resource> resources;
 
   // @Inject
   // private PageResourceService pageResourceService;
 
   // @Inject
   // private SectionService contentService;
 
   @Inject
   private ResourceService resourceService;
 
   @Test
   public void testInsertData() throws IOException {
     em.get().getTransaction().begin();
     Category c1 = categories.get().setTitle("搜索");
     categoryService.put(c1);
     Category c2 = categories.get().setTitle("特色");
     categoryService.put(c2);
     Category c3 = categories.get().setTitle("推荐");
     categoryService.put(c3);
     Category c4 = categories.get().setTitle("科技");
     categoryService.put(c4);
     Category c5 = categories.get().setTitle("生活");
     categoryService.put(c5);
     Category c6 = categories.get().setTitle("旅游");
     categoryService.put(c6);
     Category c7 = categories.get().setTitle("时尚");
     categoryService.put(c7);
     Category c8 = categories.get().setTitle("体育");
     categoryService.put(c8);
     Category c9 = categories.get().setTitle("我的图书");
     categoryService.put(c9);
     for (int c = 1; c < 2; c++) {
       Issue i1 =
           issues
               .get()
               .setImage(
                   resources.get().setMimeType(MimeType.JPG).setData(TestDataUtil.getImage(1, 1)))
               .setCategory(c4)
               .setTitle("图说天下")
               .setCreateTime(StringToDate("2012-0" + c + "-01 00:00:00", "yyyy-MM-dd HH:mm:ss"))
               .setDetail(
                   "乔布斯是改变世界的天才，他凭敏锐的触觉和过人的智慧，勇于变革，不断创新，引领全球资讯科技和电子产品的潮流，把电脑和电子产品变得简约化、平民化，让曾经是昂贵稀罕的电子产品变为现代人生活的一部分。");
       issueService.put(i1);
       int sequence = 1;
       int pageCount = 1;
       putPage(1, pageCount++, putSection("封面", i1, sequence++));
       putPage(1, pageCount++, putSection("用数字说明乔布斯", i1, sequence++));
       Section section = putSection("去世与哀悼", i1, sequence++);
       for (int i = 0; i < 12; i++) {
         putPage(1, pageCount++, section);
 
       }
       section = putSection("影响巨大", i1, sequence++);
       for (int i = 0; i < 6; i++) {
         putPage(1, pageCount++, section);
       }
       section = putSection("传奇一生", i1, sequence++);
       for (int i = 0; i < 13; i++) {
         putPage(1, pageCount++, section);
       }
       putPage(1, pageCount++, putSection("乔布斯语录", i1, sequence++));
       putPage(1, pageCount++, putSection("封底", i1, sequence++));
     }
 
     for (int c = 1; c < 2; c++) {
       Issue i2 =
           issues.get().setImage(
               resources.get().setMimeType(MimeType.JPG).setData(TestDataUtil.getImage(2, 1)))
               .setCreateTime(StringToDate("2012-0" + c + "-01 00:00:00", "yyyy-MM-dd HH:mm:ss"))
               .setCategory(c5).setTitle("影资讯").setDetail("“筷子兄弟”新作受好评 《父亲》引爆亲情共鸣");
       issueService.put(i2);
       int sequence = 1;
       int pageCount = 1;
       putPage(2, pageCount++, putSection("封面", i2, sequence++));
       putPage(2, pageCount++, putSection("目录1", i2, sequence++));
       Section section = putSection("目录2", i2, sequence++);
       for (int i = 0; i < 16; i++) {
         putPage(2, pageCount++, section);
       }
       putPage(2, pageCount++, putSection("封底", i2, sequence++));
     }
     for (int c = 1; c < 2; c++) {
       Issue issue =
           issues.get().setImage(
               resources.get().setMimeType(MimeType.JPG).setData(TestDataUtil.getImage(3, 1)))
               .setCreateTime(StringToDate("2012-0" + c + "-01 00:00:00", "yyyy-MM-dd HH:mm:ss"))
               .setCategory(c5).setTitle("选购指南").setDetail("简约之美 主流一体机电脑推荐");
       issueService.put(issue);
       int sequence = 1;
       int pageCount = 1;
       putPage(3, pageCount++, putSection("封面", issue, sequence++));
       putPage(3, pageCount++, putSection("目录1", issue, sequence++));
       Section section = putSection("目录2", issue, sequence++);
       for (int i = 0; i < 12; i++) {
         putPage(3, pageCount++, section);
       }
       putPage(3, pageCount++, putSection("封底", issue, sequence++));
     }
     for (int c = 1; c < 2; c++) {
       Issue issue =
           issues.get().setImage(
               resources.get().setMimeType(MimeType.JPG).setData(TestDataUtil.getImage(4, 1)))
               .setCreateTime(StringToDate("2012-0" + c + "-01 00:00:00", "yyyy-MM-dd HH:mm:ss"))
               .setCategory(c6).setTitle("新旅行").setDetail(
                   "坐在美国盐湖城山谷雪场的缆车上，刚刚过了一个山头，下边是黑压压的松树，缆车下行，然后上行，阳光真好，晒在人身上暖洋洋的，也无风，所以一点都不觉得冷。");
       issueService.put(issue);
       int sequence = 1;
       int pageCount = 1;
       putPage(4, pageCount++, putSection("封面", issue, sequence++));
       putPage(4, pageCount++, putSection("目录1", issue, sequence++));
       Section section = putSection("目录2", issue, sequence++);
       for (int i = 0; i < 17; i++) {
         putPage(4, pageCount++, section);
       }
       putPage(4, pageCount++, putSection("封底", issue, sequence++));
     }
 
     for (int c = 1; c < 2; c++) {
 
       Issue issue =
           issues.get().setImage(
               resources.get().setMimeType(MimeType.JPG).setData(TestDataUtil.getImage(5, 1)))
               .setCreateTime(StringToDate("2012-0" + c + "-01 00:00:00", "yyyy-MM-dd HH:mm:ss"))
               .setCategory(c8).setTitle("超体育").setDetail("西班牙德比：巴塞罗那胜皇家马德里");
       issueService.put(issue);
       int sequence = 1;
       int pageCount = 1;
       putPage(5, pageCount++, putSection("封面", issue, sequence++));
       putPage(5, pageCount++, putSection("目录1", issue, sequence++));
       Section section = putSection("目录2", issue, sequence++);
       for (int i = 0; i < 19; i++) {
         putPage(5, pageCount++, section);
       }
       putPage(5, pageCount++, putSection("封底", issue, sequence++));
     }
 
     for (int c = 1; c < 2; c++) {
       Issue issue =
           issues
               .get()
               .setImage(
                   resources.get().setMimeType(MimeType.JPG).setData(TestDataUtil.getImage(6, 1)))
               .setCreateTime(StringToDate("2012-0" + c + "-01 00:00:00", "yyyy-MM-dd HH:mm:ss"))
               .setCategory(c6)
               .setTitle("游遍天下")
               .setDetail(
                   "这期《游遍天下》是2011年的收官之作，回想一年的工作历程，编辑部某位风华绝代之人爆料了一件真事：朋友的孩子高一地理年级第一，因为喜欢看《游遍天下》，所以喜欢上了地理，孩子的书架上整齐地码着几十本《游遍天下》。我不善于煽情，也不屑于别人的这种表达，但那刻，我很感动，我们的工作有了更深的意义。");
       issueService.put(issue);
       int sequence = 1;
       int pageCount = 1;
       putPage(6, pageCount++, putSection("封面", issue, sequence++));
       putPage(6, pageCount++, putSection("目录1", issue, sequence++));
       Section section = putSection("目录2", issue, sequence++);
       for (int i = 0; i < 14; i++) {
         putPage(6, pageCount++, section);
       }
       putPage(6, pageCount++, putSection("封底", issue, sequence++));
     }
 
     for (int c = 1; c < 2; c++) {
       Issue issue =
           issues
               .get()
               .setImage(
                   resources.get().setMimeType(MimeType.JPG).setData(TestDataUtil.getImage(7, 1)))
               .setCreateTime(StringToDate("2012-0" + c + "-01 00:00:00", "yyyy-MM-dd HH:mm:ss"))
               .setCategory(c7)
              .setTitle("用户手册")
               .setDetail(
                   "近几年电影界风生水起，异常红火，于是有N多新鲜肉小生盯上了电影这块大阵地，向大银幕发起了猛烈攻击！一时之间，银幕上的新鲜小生空降兵攻城略地，看得我们眼花缭乱。");
       issueService.put(issue);
       int sequence = 1;
       int pageCount = 1;
       putPage(7, pageCount++, putSection("封面", issue, sequence++));
       putPage(7, pageCount++, putSection("目录1", issue, sequence++));
       Section section = putSection("目录2", issue, sequence++);
       for (int i = 0; i < 7; i++) {
         putPage(7, pageCount++, section);
       }
       putPage(7, pageCount++, putSection("封底", issue, sequence++));
     }
 
     for (int c = 1; c < 2; c++) {
       Issue issue =
           issues
               .get()
               .setImage(
                   resources.get().setMimeType(MimeType.JPG).setData(TestDataUtil.getImage(8, 1)))
               .setCreateTime(StringToDate("2012-0" + c + "-01 00:00:00", "yyyy-MM-dd HH:mm:ss"))
               .setCategory(c4)
               .setTitle("当月潮流")
               .setDetail(
                   "记忆中，一进入腊月，大人们就要开始忙活了。每当我们从外边带着浑身的雪花跑进屋时，总是能看到大人们在灶头忙碌的背影，从腊月到正月，也总是能够把家乡的小吃全部吃个遍。如今长大了，为了生存漂泊在外，每年进入腊月，最想家乡的小吃，总能让人魂牵梦绕！");
       issueService.put(issue);
       int sequence = 1;
       int pageCount = 1;
       putPage(8, pageCount++, putSection("封面", issue, sequence++));
       putPage(8, pageCount++, putSection("目录1", issue, sequence++));
       Section section = putSection("目录2", issue, sequence++);
       for (int i = 0; i < 10; i++) {
         putPage(8, pageCount++, section);
       }
       putPage(8, pageCount++, putSection("封底", issue, sequence++));
     }
 
     for (int c = 1; c < 2; c++) {
       Issue issue =
           issues.get().setImage(
               resources.get().setMimeType(MimeType.JPG).setData(TestDataUtil.getImage(9, 1)))
               .setCreateTime(StringToDate("2012-0" + c + "-01 00:00:00", "yyyy-MM-dd HH:mm:ss"))
               .setCategory(c7).setTitle("B&G").setDetail("美女学英语");
       issueService.put(issue);
       int sequence = 1;
       int pageCount = 1;
       putPage(9, pageCount++, putSection("封面", issue, sequence++));
       putPage(9, pageCount++, putSection("目录1", issue, sequence++));
       Section section = putSection("目录2", issue, sequence++);
       for (int i = 0; i < 19; i++) {
         putPage(9, pageCount++, section);
       }
       putPage(9, pageCount++, putSection("封底", issue, sequence++));
     }
 
     for (int c = 1; c < 2; c++) {
       Issue issue =
           issues.get().setImage(
               resources.get().setMimeType(MimeType.JPG).setData(TestDataUtil.getImage(10, 1)))
               .setCreateTime(StringToDate("2012-0" + c + "-01 00:00:00", "yyyy-MM-dd HH:mm:ss"))
               .setCategory(c7).setTitle("TREND").setDetail(
                   "谁说皮草只是女性的时尚独享？本季秋冬也让皮草陪型男们过个美丽的圣诞节，让皮草的奢华之风与男性的阳刚之气制造出最in的混搭潮流。");
       issueService.put(issue);
       int sequence = 1;
       int pageCount = 1;
       putPage(10, pageCount++, putSection("封面", issue, sequence++));
       putPage(10, pageCount++, putSection("目录1", issue, sequence++));
       Section section = putSection("目录2", issue, sequence++);
       for (int i = 0; i < 8; i++) {
         putPage(10, pageCount++, section);
       }
       putPage(10, pageCount++, putSection("封底", issue, sequence++));
     }
 
     em.get().getTransaction().commit();
   }
 
   private void putPage(final int folder, final int pageCount, final Section section)
       throws IOException {
     String title1 = new String(TestDataUtil.getHtml(folder, pageCount));
     Page page = pages.get().setSection(section).setPageNum(pageCount);
     page.setTitle(title1.substring(title1.indexOf("<title>") + 7, title1.indexOf("</title>")));
     pageService.put(page);
     Resource singleHtml =
         resources.get().setName(TestDataUtil.getHtmlFilename(pageCount)).setData(
             TestDataUtil.getSingleHtml(folder, pageCount)).setMimeType(MimeType.HTML);
     singleHtml.setPage(page);
     resourceService.put(singleHtml);
     // Resource html =
     // resources.get().setName(TestDataUtil.getHtmlFilename(pageCount)).setData(
     // TestDataUtil.getHtml(folder, pageCount)).setMimeType(MimeType.HTML);
     // resourceService.put(html);
     // Resource font =
     // resources.get().setName("page_font").setData(TestDataUtil.getCss()).setMimeType(
     // MimeType.CSS);
     // resourceService.put(font);
     // Resource image =
     // resources.get().setName(TestDataUtil.getImageFilename(pageCount)).setData(
     // TestDataUtil.getImage(folder, pageCount)).setMimeType(MimeType.JPG);
     // resourceService.put(image);
     pageService.put(page.setMainResourceFilename(singleHtml.getFilename()));
     // pageResourceService.put(pageResources.get().setPage(page).setResource(singleHtml));
     // pageService.put(page.setMainResourceFilename(html.getFilename()));
     // pageResourceService.put(pageResources.get().setPage(page).setResource(html));
     // pageResourceService.put(pageResources.get().setPage(page).setResource(font));
     // pageResourceService.put(pageResources.get().setPage(page).setResource(image));
   }
 
   private Section putSection(final String title, final Issue issue, final int sequence) {
     Section section = sections.get().setIssue(issue).setTitle(title).setSequence(sequence);
     sectionService.put(section);
     return section;
   }
 
 }
