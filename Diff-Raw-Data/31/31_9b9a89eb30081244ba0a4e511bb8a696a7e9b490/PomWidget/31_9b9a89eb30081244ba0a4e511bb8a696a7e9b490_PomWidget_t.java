 /**
  * Copyright to the original author or authors.
  *
  * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in
  * compliance with the License. You may obtain a copy of the License at:
  *
  * http://www.apache.org/licenses/LICENSE-2.0
  *
  * Unless required by applicable law or agreed to in writing, software distributed under the License is
  * distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
  * See the License for the specific language governing permissions and limitations under the License.
  */
 package fitnesse.maven.widget;
 
 import fitnesse.html.HtmlElement;
 import fitnesse.html.HtmlTag;
 import fitnesse.html.HtmlUtil;
 import fitnesse.html.RawHtml;
 import fitnesse.maven.io.MavenException;
 import fitnesse.maven.io.PomFile;
 import fitnesse.maven.util.IdGenerator;
 import fitnesse.maven.util.MavenDependencyResolver;
 import fitnesse.maven.util.MavenOutputDirectoryResolver;
 import fitnesse.maven.util.MavenPomResolver;
 import fitnesse.wiki.PageData;
 import fitnesse.wikitext.WidgetBuilder;
 import fitnesse.wikitext.widgets.ClasspathWidget;
 import fitnesse.wikitext.widgets.ParentWidget;
 
 import java.io.ByteArrayOutputStream;
 import java.io.File;
import java.io.PrintStream;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 
 public class PomWidget extends ClasspathWidget {
 
     public static final String REGEXP = "^!pom [^\r\n]*";
 
     protected static ClasspathWidgetFactory CLASSPATH_WIDGET_FACTORY = new ClasspathWidgetFactory();
     protected static IdGenerator ID_GENERATOR = new IdGenerator();
     protected static MavenDependencyResolver MAVEN_DEPENDENCY_RESOLVER = new MavenDependencyResolver();
     protected static MavenOutputDirectoryResolver MAVEN_OUTPUT_DIR_RESOLVER = new MavenOutputDirectoryResolver();
     protected static MavenPomResolver MAVEN_POM_RESOLVER = new MavenPomResolver();
 
     private static final Pattern pattern = Pattern.compile("^!pom (.*)", Pattern.CASE_INSENSITIVE);
     private static final String collapsableClosedCss = "hidden";
     private static final String collapsableClosedImg = "/files/images/collapsableClosed.gif";
     private static final String collapsableOpenCss = "collapsable";
     private static final String collapsableOpenImg = "/files/images/collapsableOpen.gif";
 
     private PomFile pomFile;
     private String cssClass = "collapse_rim";
     private String errorMessage;
     private String pomFileInput;
    private boolean unexpectedErrorOccurred;
 
 
     static {
         PageData.classpathWidgetBuilder = new WidgetBuilder(new Class[]{ClasspathWidget.class, PomWidget.class});
     }
 
 
     public PomWidget(ParentWidget parentWidget, String inputText) throws Exception {
         super(parentWidget, inputText);
 
         pomFileInput = parsePomFile(inputText);
 
         try {
             pomFile = MAVEN_POM_RESOLVER.resolve(new File(pomFileInput));
 
             if (pomFile != null) {
                 List<String> dependencies = new ArrayList<String>();
                 dependencies.addAll(MAVEN_OUTPUT_DIR_RESOLVER.resolve(pomFile));
                 dependencies.addAll(MAVEN_DEPENDENCY_RESOLVER.resolve(pomFile));
                 CLASSPATH_WIDGET_FACTORY.build(this, dependencies);
             }
 
         } catch (MavenException err) {
             errorMessage = err.getMessage();
         } catch (Exception err) {
             ByteArrayOutputStream output = new ByteArrayOutputStream();
            err.printStackTrace(new PrintStream(output));
             errorMessage = new String(output.toByteArray());
            unexpectedErrorOccurred = true;
         }
     }
 
 
     @Override
     public String getText() throws Exception {
         ClasspathWidget classPathWidget = (ClasspathWidget) getChildren().get(0);
         return classPathWidget.getText();
     }
 
 
     @Override
     public String render() throws Exception {
         RawHtml titleElement;
         RawHtml bodyElement = new RawHtml(childHtml());
         boolean expanded = false;
 
        if (unexpectedErrorOccurred) {
            titleElement = createTitleElement("PomWidget encountered an unexpected error!!");
        } else if (pomFile == null) {
             titleElement = createTitleElement("Maven POM could NOT be found: " + pomFileInput);
         } else {
             titleElement = createTitleElement("Maven POM: " + pomFile.getFile());
         }
 
         if (errorMessage != null) {
             bodyElement = new RawHtml("<pre>" + errorMessage + "</pre>");
             expanded = true;
         }
 
         HtmlElement html = makeCollapsableSection(titleElement, bodyElement, expanded);
         return html.html();
     }
 
 
     private RawHtml createTitleElement(String text) {
         return new RawHtml("<b>" + HtmlUtil.metaText("&nbsp;" + text) + "</b>");
     }
 
 
     private String imageSrc(boolean expanded) {
         if (expanded)
             return collapsableOpenImg;
         else
             return collapsableClosedImg;
     }
 
 
     private HtmlTag makeCollapsableDiv(boolean expanded) {
         if (expanded)
             return HtmlUtil.makeDivTag(collapsableOpenCss);
         else
             return HtmlUtil.makeDivTag(collapsableClosedCss);
     }
 
 
     private HtmlElement makeCollapsableSection(HtmlElement title, HtmlElement content, boolean expanded) {
         String id = "maven-pom-" + ID_GENERATOR.generate();
 
         HtmlTag outerDiv = HtmlUtil.makeDivTag(cssClass);
 
         HtmlTag image = new HtmlTag("img");
         image.addAttribute("src", imageSrc(expanded));
         image.addAttribute("class", "left");
         image.addAttribute("id", "img" + id);
 
         HtmlTag anchor = new HtmlTag("a", image);
         anchor.addAttribute("href", "javascript:toggleCollapsable('" + id + "');");
 
         outerDiv.add(anchor);
         outerDiv.add(title);
 
         HtmlTag collapsablediv = makeCollapsableDiv(expanded);
         collapsablediv.addAttribute("id", id);
         collapsablediv.add(content);
         outerDiv.add(collapsablediv);
 
         return outerDiv;
     }
 
 
     private String parsePomFile(String input) {
         Matcher matcher = pattern.matcher(input);
         if (matcher.find()) {
             return matcher.group(1);
         }
         return null;
     }
 
 }
