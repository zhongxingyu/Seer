 package com.atlassian.javascript.ajs.selenium;
 
 import com.atlassian.core.util.ClassLoaderUtils;
 import org.apache.commons.io.DirectoryWalker;
 import org.junit.After;
 import org.junit.Before;
 import org.junit.Test;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
 
 import static junit.framework.Assert.assertTrue;
 
 public class AUIFileStructureTest
 {
     private static final String[] PROJ_STRUCTURE = new String[]{
 
         // If you're adding files here, make you've added them to all sample and test pages too!
 
             "atlassian-plugin.xml",
             "css/atlassian/basic.css",
             "css/atlassian/dialog.css",
             "css/atlassian/dropdown.css",
             "css/atlassian/firebug.css",
             "css/atlassian/forms.css",
             "css/atlassian/icons.css",
             "css/atlassian/inline-dialog.css",
             "css/atlassian/messages.css",
             "css/atlassian/tables.css",
             "css/atlassian/tabs.css",
             "css/atlassian/ie/dialog-ie.css",
             "css/atlassian/ie/dropdown-ie.css",
             "css/atlassian/ie/forms-ie.css",
             "css/atlassian/ie/icons-ie.css",
             "css/atlassian/ie/inline-dialog-ie.css",
             "css/atlassian/images/arrow.png",
             "css/atlassian/images/fav_off_16.png",
             "css/atlassian/images/fav_on_16.png",
             "css/atlassian/images/forms/icons_form.gif",
             "css/atlassian/images/icons/aui-icon-close.png",
             "css/atlassian/images/icons/aui-icon-forms.gif",
             "css/atlassian/images/icons/aui-icon-tools.gif",
             "css/atlassian/images/wait.gif",
             "js/atlassian/atlassian.js",
            "js/atlassian/atlassian.overrides.js",
             "js/atlassian/containdropdown.js",
             "js/atlassian/cookie.js",
             "js/atlassian/dialog.js",
             "js/atlassian/dropdown.js",
             "js/atlassian/event.js",
             "js/atlassian/firebug.js",
             "js/atlassian/forms.js",
             "js/atlassian/icons.js",
             "js/atlassian/inline-dialog.js",
             "js/atlassian/jquery/jquery.autocomplete.js",
             "js/atlassian/jquery/jquery.getdocheight.js",
             "js/atlassian/jquery/jquery.hotkeys.js",
             "js/atlassian/jquery/jquery.is-dirty.js",
             "js/atlassian/jquery/jquery.moveto.js",
             "js/atlassian/jquery/jquery.offsetanchors.js",
             "js/atlassian/jquery/jquery.os.js",
             "js/atlassian/jquery/jquery.progressbar.js",
             "js/atlassian/jquery/jquery.selection.js",
             "js/atlassian/jquery/jquery.stalker.js",
             "js/atlassian/jquery/jquery.throbber.js",
             "js/atlassian/messages.js",
             "js/atlassian/tables.js",
             "js/atlassian/tabs.js",
             "js/atlassian/template.js",
             "js/atlassian/whenitype.js",
             "js/external/jquery/jquery.js",
             "js/external/jquery/jquery-min.js",
             "js/external/jquery/jquery-compatibility.js",
             "js/external/jquery/jquery-ui-1.7-bug-fixes.js",
             "js/external/jquery/jquery-ui-min.js",
             "js/external/jquery/jquery-ui-other-min.js",
             "js/external/jquery/jquery-ui-other.js",
             "js/external/jquery/jquery-ui.js",
             "js/external/jquery/plugins/jquery.aop.js",
             "js/external/jquery/plugins/jquery.form.js",
             "js/external/raphael/raphael-min.js",
             "js/external/raphael/raphael.js",
             "js/external/raphael/raphael.shadow.js"
     };
     private Collection<String> knownPaths = null;
 
 
     @After
     public void tearDown()
     {
         knownPaths = null;
     }
 
     @Before
     public void setUp()
     {
         knownPaths = Arrays.asList(PROJ_STRUCTURE);
     }
 
     @Test
     public void testProjectContents() throws Exception
     {
         // Check that all known files exist in the correct location
         for (String knownPath : knownPaths)
         {
             assertTrue("File does not exist <" + knownPath + ">", ClassLoaderUtils.getResource(knownPath, this.getClass()) != null);
         }
     }
 
     @Test
     public void testForUnwantedFiles() throws Exception
     {
         final Collection<String> foundPaths = new ProjectChecker().start();
 
         // Check that all found files are on the known list (eg: alert for new files that have been created (or moved) but not yet listed as 'known')
         foundPaths.removeAll(knownPaths);
         assertTrue("Files exist that are not on the known files list: " + foundPaths, foundPaths.isEmpty());
     }
 
     private class ProjectChecker extends DirectoryWalker
     {
 
         private static final String RESOURCE_PREFIX = "src/main/resources";
 
         public Collection<String> start() throws Exception
         {
             ArrayList<String> paths = new ArrayList<String>();
             walk(new File(RESOURCE_PREFIX), paths);
             return paths;
         }
 
         protected boolean handleDirectory(File directory, int depth, Collection results) throws IOException
         {
             return !directory.isHidden();
         }
 
         protected void handleFile(File file, int depth, Collection results) throws IOException
         {
             if (!file.isHidden())
             {
                 String filePath = file.getPath().substring(RESOURCE_PREFIX.length() + 1);
                 filePath = filePath.replaceAll("\\\\", "/");
                 results.add(filePath);
             }
         }
     }
 }
