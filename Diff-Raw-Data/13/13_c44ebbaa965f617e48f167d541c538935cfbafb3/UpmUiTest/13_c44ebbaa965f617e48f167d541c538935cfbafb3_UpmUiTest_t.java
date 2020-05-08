 package com.atlassian.refapp.ctk.ui.tests;
 
 import com.atlassian.refapp.ctk.ui.CtkSeleniumTest;
 import com.atlassian.webdriver.refapp.page.FirstPage;
 import com.atlassian.webdriver.refapp.page.RefappPages;
 
 import org.junit.Before;
 import org.junit.Test;
 
import java.net.URL;

 import static com.google.common.base.Preconditions.checkState;
 
 public class UpmUiTest extends CtkSeleniumTest
 {
     @Before
     public void loginAndLoadUpm() throws Exception
     {
         // TODO:Should be able to supply product instance here. Wait for newer atlassian-selenium.
        String baseUrl = getInfoProvider().createLocalhostUriBuilder().build().toURL().toString();
        System.setProperty("refapp-base-url", baseUrl);

         FirstPage page1 = RefappPages.FIRST_PAGE.get(getDriver());
         page1.get(false);
         page1.login(getInfoProvider().getAdminUsername(), getInfoProvider().getAdminPassword());
         checkState(page1.isLoggedIn());
         checkState(page1.isAdmin());
 
        URL upmUrl = getInfoProvider().createLocalhostUriBuilder().path("plugins").path("servlet").path("upm").build().toURL();
        getDriver().navigate().to(upmUrl);
     }
 
     @Test
     public void testUpmPageRenders()
     {
         assertTitle("Plugins");
         assertTextPresent("The Universal Plugin Manager allows you to view, manage and upgrade your installed plugins");
     }
 
     /*
      * Can't enable the remaining tests until Javascript is enabled in Webdriver
      */
 
 //    @Test
 //    public void testUpgradeTabRenders() throws Exception
 //    {
 //        clickTab("upgrade");
 //        assertTextPresent("Upgrade");
 //        assertTextPresent("Available Upgrades");
 //    }
 //
 //    @Test
 //    public void testUpmManageExistingTabRenders() throws Exception
 //    {
 //        clickTab("manage");
 //        assertTextPresent("Manage Existing");
 //        assertTextPresent("User-installed Plugins");
 //        assertTextPresent("System Plugins");
 //    }
 //
 //    @Test
 //    public void testUpmInstallTabRenders() throws Exception
 //    {
 //        clickTab("install");
 //        assertTextPresent("Install");
 //        assertTextPresent("Featured Plugins");
 //    }
 //
 //    @Test
 //    public void testUpmUpgradeCheckTabRenders() throws Exception
 //    {
 //        clickTab("compatibility");
 //        assertTextPresent("Refimpl Upgrade Check");
 //        assertTextPresent("The Universal Plugin Manager can help you prepare for a Refimpl upgrade.");
 //    }
 //
 //    @Test
 //    public void testUpmAuditLogTabRenders() throws Exception
 //    {
 //        clickTab("manage");
 //        assertTextPresent("Audit Log");
 //        assertTextPresent("Log Message");
 //        assertTextPresent("By");
 //        assertTextPresent("Date");
 //    }
 //
 //    @Test
 //    public void testSystemPluginIsNotMarkedAsUserInstalled() throws Exception
 //    {
 //        clickTab("manage");
 //        assertFalse(getPluginClasses(PLUGIN_NAME).contains("user-installed"));
 //    }
 //
 //    @Test
 //    public void testClickingPluginCausesExpansion() throws Exception
 //    {
 //        clickTab("manage");
 //        clickPlugin(PLUGIN_NAME);
 //        assertTrue(getPluginClasses(PLUGIN_NAME).contains("expanded"));
 //    }
 //
 //    @Test
 //    public void testDisableInstalledPlugin() throws Exception
 //    {
 //        try
 //        {
 //            clickTab("manage");
 //            clickPlugin(PLUGIN_NAME);
 //            disablePlugin(PLUGIN_NAME);
 //            assertTrue(getPluginClasses(PLUGIN_NAME).contains("disabled"));
 //        }
 //        finally
 //        {
 //            enablePlugin(PLUGIN_NAME);
 //        }
 //    }
 //
 //    @Test
 //    public void testEnableInstalledPlugin() throws Exception
 //    {
 //        clickTab("manage");
 //        clickPlugin(PLUGIN_NAME);
 //        disablePlugin(PLUGIN_NAME);
 //        enablePlugin(PLUGIN_NAME);
 //        assertFalse(getPluginClasses(PLUGIN_NAME).contains("disabled"));
 //    }
 //
 //    @Test
 //    public void testEnablePluginAddsMessageToAuditLog() throws Exception
 //    {
 //        clickTab("manage");
 //        clickPlugin(PLUGIN_NAME);
 //        disablePlugin(PLUGIN_NAME);
 //
 //        clickTab("log");
 //        int numMessagesBefore = getAuditLogMessageCount();
 //
 //        clickTab("manage");
 //        enablePlugin(PLUGIN_NAME);
 //
 //        clickTab("log");
 //        int numMessagesAfter = getAuditLogMessageCount();
 //
 //        assertEquals(numMessagesBefore, numMessagesAfter - 1);
 //        assertTrue(getAuditLogMessages().get(0).getText().startsWith("Enabled plugin " + PLUGIN_NAME));
 //    }
 //
 //    @Test
 //    public void testDisablePluginAddsMessageToAuditLog() throws Exception
 //    {
 //        try
 //        {
 //            clickTab("log");
 //            int numMessagesBefore = getAuditLogMessageCount();
 //
 //            clickTab("manage");
 //            clickPlugin(PLUGIN_NAME);
 //            disablePlugin(PLUGIN_NAME);
 //
 //            clickTab("log");
 //            int numMessagesAfter = getAuditLogMessageCount();
 //
 //            assertEquals(numMessagesBefore, numMessagesAfter - 1);
 //            assertTrue(getAuditLogMessages().get(0).getText().startsWith("Disabled plugin " + PLUGIN_NAME));
 //        }
 //        finally
 //        {
 //            clickTab("manage");
 //            enablePlugin(PLUGIN_NAME);
 //        }
 //    }
 //
 //    /*
 //     * Tabs
 //     */
 //
 //    private void clickTab(String tabName) throws Exception
 //    {
 //        click(By.id("upm-tab-" + tabName));
 //        pause();
 //
 //        //confirm that the page has updated to display the desired tab contents
 //        String pageTitle = findElement(By.xpath("//span[@id=\"upm-title\"]")).getText();
 //        String tabTitle = findElement(By.xpath("//li[a[@id=\"upm-tab-" + tabName + "\"]]")).getText();
 //        assertTrue("Selected tab set to '" + tabTitle + "' but still on '" + pageTitle + "'", tabTitle.startsWith(pageTitle));
 //    }
 //
 //    /*
 //     * Plugins
 //     */
 //
 //    private void clickPlugin(String pluginName) throws Exception
 //    {
 //        String showSystemPluginsLink = "upm-manage-show-system";
 //        if (hasElement(By.id(showSystemPluginsLink)))
 //        {
 //            click(By.id(showSystemPluginsLink));
 //        }
 //        click(By.xpath(getPluginNameXpath(pluginName)));
 //        pause();
 //    }
 //
 //    private void enablePlugin(String pluginName) throws Exception
 //    {
 //        getPluginElement(pluginName).findElement(By.className("upm-enable")).click();
 //        pause();
 //    }
 //
 //    private void disablePlugin(String pluginName) throws Exception
 //    {
 //        getPluginElement(pluginName).findElement(By.className("upm-disable")).click();
 //        pause();
 //    }
 //
 //    private String getPluginNameXpath(String pluginName)
 //    {
 //        return "//h4[@class=\"upm-plugin-name\" and text()=\"" + pluginName + "\"]";
 //    }
 //
 //    private String getPluginXpath(String pluginName)
 //    {
 //        return getPluginNameXpath(pluginName) + "/../..";
 //    }
 //
 //    private WebElement getPluginElement(String pluginName)
 //    {
 //        return findElement(By.xpath(getPluginXpath(PLUGIN_NAME)));
 //    }
 //
 //    private String getPluginClasses(String pluginName)
 //    {
 //        return getPluginElement(pluginName).getAttribute("class");
 //    }
 //
 //    /*
 //     * Audit log
 //     */
 //
 //    private List<WebElement> getAuditLogMessages()
 //    {
 //        return findElements(By.xpath("//div[@id=\"upm-audit-log\"]//table//tbody//tr"));
 //    }
 //
 //    private int getAuditLogMessageCount()
 //    {
 //        //only 1 page of log messages
 //        String paginationClass = findElement(By.id("upm-audit-log-pagination")).getAttribute("class");
 //        if (paginationClass != null && paginationClass.contains("hidden"))
 //        {
 //            return getAuditLogMessages().size();
 //        }
 //        //more than 1 page of log messages
 //        else
 //        {
 //            //this is a total hack, but i couldn't think of a better way to do it...
 //            String count = findElement(By.id("upm-audit-log-count")).getText();
 //            int lastIndex = count.lastIndexOf(" ");
 //            return Integer.parseInt(count.substring(lastIndex+1));
 //        }
 //    }
 }
