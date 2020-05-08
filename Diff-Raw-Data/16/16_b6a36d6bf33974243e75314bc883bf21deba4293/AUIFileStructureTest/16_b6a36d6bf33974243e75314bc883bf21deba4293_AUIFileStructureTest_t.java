 package com.atlassian.javascript.ajs.selenium;
 
 import com.atlassian.core.util.ClassLoaderUtils;
 import junit.framework.TestCase;
 import junit.framework.AssertionFailedError;
 import org.apache.commons.io.DirectoryWalker;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.Collection;
import java.util.List;
 
 public class AUIFileStructureTest extends TestCase {
     private static final String[] PROJ_STRUCTURE = new String[]{
             "atlassian-plugin.xml",
             "css/basic.css",
             "css/dialog.css",
             "css/dropdown.css",
             "css/firebug.css",
             "css/forms.css",
             "css/icons.css",
             "css/ie/dialog-ie.css",
             "css/ie/dropdown-ie.css",
             "css/ie/forms-ie.css",
             "css/ie/inline-dialog-ie.css",
             "css/images/arrow.png",
             "css/images/fav_off_16.png",
             "css/images/fav_on_16.png",
             "css/images/forms/icons_form.gif",
             "css/images/icons/aui-icon-close.png",
             "css/images/icons/aui-icon-forms.gif",
             "css/images/icons/aui-icon-tools.gif",
             "css/images/wait.gif",
             "css/inline-dialog.css",
             "js/atlassian/atlassian.js",
             "js/atlassian/cookie.js",
             "js/atlassian/dialog.js",
             "js/atlassian/dropdown.js",
             "js/atlassian/forms.js",
             "js/atlassian/firebug.js",
             "js/atlassian/inline-dialog.js",
             "js/atlassian/jquery.autocomplete.js",
             "js/atlassian/jquery.is-dirty.js",
             "js/atlassian/jquery.progressbar.js",
             "js/atlassian/jquery.selection.js",
             "js/atlassian/jquery.throbber.js",
             "js/external/jquery/jquery-1.3.2.js",
             "js/external/jquery/jquery-1.3.2-min.js",
             "js/external/jquery/jquery-compatibility.js",
             "js/external/jquery/jquery-ui-1.7-bug-fixes.js",
             "js/external/jquery/jquery-ui-1.7.2-min.js",
             "js/external/jquery/jquery-ui-1.7.2-other-min.js",
             "js/external/jquery/jquery-ui-1.7.2-other.js",
             "js/external/jquery/jquery-ui-1.7.2.js",
             "js/external/jquery/plugins/jquery.aop.js",
             "js/external/jquery/plugins/jquery.form.js",
             "js/external/raphael/raphael-min.js",
             "js/external/raphael/raphael.js",
             "js/external/raphael/raphael.shadow.js"
     };
 
     public void testProjectContents() throws Exception {
         final Collection<String> knownPaths = Arrays.asList(PROJ_STRUCTURE);
         final Collection<String> foundPaths = new ProjectChecker().start();
 
         // Check that all known files exist in the correct location
         for (String knownPath : knownPaths) {
             assertTrue("File does not exist <" + knownPath + ">", ClassLoaderUtils.getResource(knownPath, this.getClass()) != null);
         }
 
         // Check that all found files are on the known list (eg: alert for new files that have been created (or moved) but not yet listed as 'known')
         foundPaths.removeAll(knownPaths);
         assertTrue("Files exist that are not on the known files list: " + foundPaths, foundPaths.isEmpty());
     }
 
     class ProjectChecker extends DirectoryWalker {
         private static final String RESOURCE_PREFIX = "src/main/resources";
 
         public Collection<String> start() throws Exception {
             ArrayList<String> paths = new ArrayList<String>();
             walk(new File(RESOURCE_PREFIX), paths);
             return paths;
         }
 
        // Allow exception for .svn/ files
         @Override
         protected void handleFile(File file, int depth, Collection results) throws IOException {
            String filePath= file.getPath().substring(RESOURCE_PREFIX.length() + 1);
             if (!file.getName().equals(".DS_Store"))
            {
                if (!filePath.contains("/.svn/"))
                {
                    results.add(filePath);
                }
            }
         }
     }
 }
