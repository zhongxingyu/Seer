 package com.enonic.autotests.general;
 
 import java.io.InputStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.Random;
 
 import org.testng.Assert;
 import org.testng.annotations.Test;
 
 import com.enonic.autotests.BaseTest;
 import com.enonic.autotests.model.Content;
 import com.enonic.autotests.model.ContentCategory;
 import com.enonic.autotests.model.ContentHandler;
 import com.enonic.autotests.model.ContentRepository;
 import com.enonic.autotests.model.ContentType;
 import com.enonic.autotests.model.ImageContentInfo;
 import com.enonic.autotests.model.site.Portlet;
 import com.enonic.autotests.model.site.STKResource;
 import com.enonic.autotests.model.site.SectionMenuItem;
 import com.enonic.autotests.model.site.Site;
 import com.enonic.autotests.model.site.Site.AllowedPageTypes;
 import com.enonic.autotests.pages.adminconsole.content.AbstractContentTableView;
 import com.enonic.autotests.pages.adminconsole.site.SiteMenuItemsTablePage;
 import com.enonic.autotests.pages.adminconsole.site.SitePortletsTablePage;
 import com.enonic.autotests.pages.adminconsole.site.SitesTableFrame;
 import com.enonic.autotests.services.ContentService;
 import com.enonic.autotests.services.ContentTypeService;
 import com.enonic.autotests.services.RepositoryService;
 import com.enonic.autotests.services.SiteService;
 import com.enonic.autotests.testdata.contenttype.ContentConvertor;
 import com.enonic.autotests.utils.TestUtils;
 
 /**
  * Test for verifying of  getContentByCategory datasource content.
  *
  */
 public class PortletPreviewDatasourceTest extends BaseTest
 {
 	private SiteService siteService = new SiteService();
 	private ContentTypeService contentTypeService = new ContentTypeService();
 	private RepositoryService repositoryService = new RepositoryService();
 	private ContentService contentService = new ContentService();
 	
 	private final String SITE_DS_KEY = "site_ds_key";
 	private final String TEST_CONTENT_KEY ="test_content_key";
 	private final String DS_REPOSITORY_KEY = "ds_repo";
 	private final String DS_CATEGORY_KEY = "ds_cat";
 	private final String TEST_DS_SECTION = "ds_section";
 	private final String PORTLET_DS_KEY = "ds_PORTLET";
 	private final String IMAGE_CATEGORY_NAME = "imcategory";
 	private final String POTLET_NAME = "dsportlet";
 
 	private final String DATASOURCE_GETCONTENT_BY_CATEGORY = "test-data/datasource/content-by-category.xml";
 	private final String CONTENT_NAME = "test.jpg";
 
 
 	@Test(description = "setup: create preconditions. Create a site with section and portlet. Create a repository and category with image ctype, and publish content to the section")
 	public void setup()
 	{
 		createImageCType();
 		createSite();
 		allowSection();
 		addSection();
 
 		createRepositoryAndCategory();
 		addImageContentAndPublish();
 		addPortlet();
 
 	}
 
 	@Test(dependsOnMethods = "setup",description="click by 'Preview' button and verify: published content present in a datasource content")
 	public void verifyGetContentByCategoryDataSourceContent()
 	{
 		logger.info(" getContentByCategory#### get datasource content and verify: content-name present in the source");
 		Portlet portlet = (Portlet) getTestSession().get(PORTLET_DS_KEY);
 		String pageSource = siteService.getPreviewDatasourceContent(getTestSession(), portlet);
 		Content<ImageContentInfo> content = (Content<ImageContentInfo>)getTestSession().get(TEST_CONTENT_KEY);
 		Assert.assertTrue(pageSource.contains(content.getDisplayName()), "content name was not present in the Preview Datasource xml-content");
 		Assert.assertTrue(pageSource.contains(content.getContentTab().getInfo().getDescription()), "description of content was not present in the Preview Datasource xml-content");
		logger.info(" FINISED $$$$$ TEST PREVIEW  DATASOURCE::: getContentByCategory");
 	}
 
 	/**
 	 * adds sample portlet with 'getContentByCategory' datasource
 	 */
 	private void addPortlet()
 	{
 		Site site = (Site) getTestSession().get(SITE_DS_KEY);
 		Portlet portlet = new Portlet();
 		portlet.setName(POTLET_NAME);
 		STKResource stylesheet = new STKResource();
 		stylesheet.setName("sample-module.xsl");
 		stylesheet.setPath("modules", "module-sample-site");
 		portlet.setStylesheet(stylesheet);
 		portlet.setSiteName(site.getDispalyName());
 		InputStream in = ContentConvertor.class.getClassLoader().getResourceAsStream(DATASOURCE_GETCONTENT_BY_CATEGORY);
 		String datasource = TestUtils.getInstance().readConfiguration(in);
 		int index = datasource.indexOf("categoryKeys\">");
 		StringBuffer sb = new StringBuffer(datasource);
 		int key = (Integer) getTestSession().get(DS_CATEGORY_KEY);
 		sb.insert(index + 14, key);
 
 		portlet.setDatasource(sb.toString());
 		SitePortletsTablePage table = siteService.addPortlet(getTestSession(), portlet);
 		boolean result = table.verifyIsPresent(portlet.getName());
 		Assert.assertTrue(result, "Portlet with name: " + portlet.getName() + " was not found in the table!");
 		getTestSession().put(PORTLET_DS_KEY, portlet);
 	}
 
 	/**
 	 * creates 'Image' content type.
 	 */
 	private void createImageCType()
 	{
 		logger.info("checks for the existance  of Content type, creates new content type if it does not exist");
 		ContentType imagesType = new ContentType();
 		imagesType.setName("Image");
 		imagesType.setContentHandler(ContentHandler.IMAGES);
 		imagesType.setDescription("content repository test");
 		boolean isExist = contentTypeService.findContentType(getTestSession(), "Image");
 		if (!isExist)
 		{
 			contentTypeService.createContentType(getTestSession(), imagesType);
 			logger.info("New content type with 'Images' handler was created");
 		} else
 		{
 			logger.info("Image content already exists");
 		}
 	}
 
 	/**
 	 * create repository and  category with "Image" content type.
 	 */
 	private void createRepositoryAndCategory()
 	{
 		ContentRepository repository = new ContentRepository();
 		repository.setName("testDS" + Math.abs(new Random().nextInt()));
 		repositoryService.createContentRepository(getTestSession(), repository);
 		getTestSession().put(DS_REPOSITORY_KEY, repository);
 
 		ContentCategory imageCategory = new ContentCategory();
 		imageCategory.setContentTypeName("Image");
 		imageCategory.setName(IMAGE_CATEGORY_NAME);
 		String[] parents = { repository.getName() };
 		imageCategory.setParentNames(parents);
 		repositoryService.addCategory(getTestSession(), imageCategory);
 		int catKey = repositoryService.getCategoryKey(getTestSession(), imageCategory.getName(), parents);
 		getTestSession().put(DS_CATEGORY_KEY, Integer.valueOf(catKey));
 		logger.info("category was created: cat-name" + imageCategory.getName());
 	}
 
 
 	/**
 	 * add image to the category and publish to the test-site .
 	 */
 	private void addImageContentAndPublish()
 	{
 		ContentRepository repository = (ContentRepository) getTestSession().get(DS_REPOSITORY_KEY);
 
 		String pathToFile = "test-data/contentrepository/" + CONTENT_NAME;
 		Content<ImageContentInfo> content = new Content<>();
 		String[] pathToContent = new String[] { repository.getName(), IMAGE_CATEGORY_NAME };
 		content.setParentNames(pathToContent);
 		ImageContentInfo contentTab = new ImageContentInfo();
 		contentTab.setPathToFile(pathToFile);
 		contentTab.setDescription("image for portlet data source test");
 		content.setContentTab(contentTab);
 		content.setDisplayName("image-test");
 		content.setContentHandler(ContentHandler.IMAGES);
 		// 1. add image to category, category has 'IMAGES' content handler:
 		AbstractContentTableView table = contentService.addimageContent(getTestSession(), content);
 		Assert.assertTrue(table.isContentPresentInTable(content.getDisplayName()), "content was not found in the table!");
 		SectionMenuItem section = (SectionMenuItem) getTestSession().get(TEST_DS_SECTION);
 		contentService.doPublishContentToSection(getTestSession(), content, section);
 		getTestSession().put(TEST_CONTENT_KEY, content);
 	}
 
 
 	/**
 	 * create sample test-site.
 	 */
 	private void createSite()
 	{
 		logger.info("create new site and verify.");
 		Site site = new Site();
 		String siteName = "portletds" + Math.abs(new Random().nextInt());
 		site.setDispalyName(siteName);
 		site.setLanguage("English");
 		SitesTableFrame table = siteService.createSite(getTestSession(), site);
 		boolean result = table.verifyIsPresent(site.getDispalyName());
 		Assert.assertTrue(result, "new site was not found in the table");
 		getTestSession().put(SITE_DS_KEY, site);
 		logger.info("site created: " + siteName);
 	}
 
 
 	/**
 	 * allows section page for site.
 	 */
 	private void allowSection()
 	{
 		Site site = (Site) getTestSession().get(SITE_DS_KEY);
 		logger.info("allow Section page type. site: " + site.getDispalyName());
 
 		AllowedPageTypes[] allowedPageTypes = { AllowedPageTypes.SECTION };
 		site.setAllowedPageTypes(allowedPageTypes);
 		siteService.editSite(getTestSession(), site.getDispalyName(), site);
 		logger.info("Section page type allowed. site: " + site.getDispalyName());
 
 	}
 
 	/**
 	 * add section to the test-site
 	 */
 	private void addSection()
 	{
 		logger.info("#### STARTED: add new ordered section menu item to the  Site ");
 		Site site = (Site) getTestSession().get(SITE_DS_KEY);
 		SectionMenuItem section = new SectionMenuItem();
 		section.setDisplayName("test");
 		section.setShowInMenu(true);
 		section.setMenuName("test");
 		section.setOrdered(false);
 		section.setSiteName(site.getDispalyName());
 		List<String> list = new ArrayList<>();
 		list.add("Image");
 		section.setAvailableContentTypes(list);
 		// 1. try to add a new section to Site:
 		SiteMenuItemsTablePage siteItems = siteService.addSectionMenuItem(getTestSession(), site.getDispalyName(), section);
 		// 2. verify: section present
 		boolean result = siteItems.verifyIsPresent(section.getDisplayName());
 		Assert.assertTrue(result, "section was not found in the table!");
 
 		// put new created section to the session.
 		getTestSession().put(TEST_DS_SECTION, section);
 		logger.info("section was added to site, site:" + site.getDispalyName());
 
 	}
 }
