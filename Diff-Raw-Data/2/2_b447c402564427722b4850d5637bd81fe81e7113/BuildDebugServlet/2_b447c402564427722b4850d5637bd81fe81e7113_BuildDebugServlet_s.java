 package com.psddev.dari.util;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.security.DigestInputStream;
 import java.security.MessageDigest;
 import java.security.NoSuchAlgorithmException;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.Date;
 import java.util.LinkedHashMap;
 import java.util.List;
 import java.util.Map;
 import java.util.Properties;
 import java.util.Set;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 import javax.servlet.ServletContext;
 import javax.servlet.ServletException;
 import javax.servlet.http.HttpServlet;
 import javax.servlet.http.HttpServletRequest;
 import javax.servlet.http.HttpServletResponse;
 
 /** Debug servlet for inspecting application build information. */
 @DebugFilter.Path("build")
 @SuppressWarnings("serial")
 public class BuildDebugServlet extends HttpServlet {
 
     public static final String PROPERTIES_FILE = "/WEB-INF/classes/build.properties";
 
     /** Returns all the properties in the build file. */
     public static Properties getProperties(ServletContext context) throws IOException {
         return getEmbeddedProperties(context, null);
     }
 
     /** Returns all the properties in the build file of an embedded war file. */
     public static Properties getEmbeddedProperties(ServletContext context, String embeddedPath) throws IOException {
         Properties build = new Properties();
         InputStream stream = context.getResourceAsStream((embeddedPath != null ? embeddedPath : "") + PROPERTIES_FILE);
         if (stream != null) {
             try {
                 build.load(stream);
             } finally {
                 stream.close();
             }
         }
         return build;
     }
 
     /**
      * Returns a descriptive label that represents the build within the
      * given {@code context}.
      */
     public static String getLabel(ServletContext context) {
         Properties build = null;
         try {
             build = getProperties(context);
         } catch (IOException ex) {
         }
         if (build == null) {
             build = new Properties();
         }
         return getLabel(build);
     }
 
     // Returns a descriptive label using the given properties.
     private static String getLabel(Properties properties) {
         String title = properties.getProperty("name");
         if (ObjectUtils.isBlank(title)) {
             title = "Anonymous Application";
         }
         String version = properties.getProperty("version");
         if (!ObjectUtils.isBlank(version)) {
             title += ": " + version;
         }
         String buildNumber = properties.getProperty("buildNumber");
         if (!ObjectUtils.isBlank(buildNumber)) {
             title += " build " + buildNumber;
         }
         return title;
     }
 
     @Override
     protected void doGet(
             HttpServletRequest request,
             HttpServletResponse response)
             throws IOException, ServletException {
 
         final String buildContext = request.getParameter("context");
 
         new DebugFilter.PageWriter(getServletContext(), request, response) {{
             startPage("Build Information");
 
                 writeStart("style", "type", "text/css");
                     write("tr.merge { color: rgba(0, 0, 0, 0.3); }");
                     write("td.num { text-align: right; }");
                     write("td:not(.wrap) { white-space: nowrap; }");
                 writeEnd();
 
                 writeStart("script");
                     write("$(document).ready(function(){");
                         write("$('#contextPicker').change(function() {");
                             write("this.form.submit();");
                         write("});");
                     write("});");
                 writeEnd();
 
                 Map<String, Properties> embeddedProperties = new LinkedHashMap<String, Properties>();
                 @SuppressWarnings("unchecked")
                 Set<String> paths = (Set<String>) getServletContext().getResourcePaths("/");
                 if (paths != null) {
                     for (String path : paths) {
                         if (path.endsWith("/")) {
                             path = path.substring(0, path.length()-1);
                             Properties properties = getEmbeddedProperties(getServletContext(), path);
                             if (!properties.isEmpty()) {
                                 embeddedProperties.put(path.substring(1), properties);
                             }
                         }
                     }
                 }
 
                 Properties build = null;
                 if (embeddedProperties.containsKey(buildContext)) {
                     build = embeddedProperties.get(buildContext);
                 } else {
                     build = getProperties(getServletContext());
                 }
 
                 String issueSystem = build.getProperty("issueManagementSystem");
                 String issueUrl = build.getProperty("issueManagementUrl");
                 Pattern issuePattern = null;
                 String issueUrlFormat = null;
                 if ("JIRA".equals(issueSystem)) {
                     String prefix = "/browse/";
                     int prefixAt = issueUrl.indexOf(prefix);
                     if (prefixAt > -1) {
                         prefixAt += prefix.length();
                         int slashAt = issueUrl.indexOf("/", prefixAt);
                         String jiraId = slashAt > -1 ?
                                 issueUrl.substring(prefixAt, slashAt) :
                                 issueUrl.substring(prefixAt);
                         issuePattern = Pattern.compile("\\Q" + jiraId + "\\E-\\d+");
                         issueUrlFormat = issueUrl.substring(0, prefixAt) + "%s";
                     }
                 }
 
                 String scmUrlFormat = null;
                 String scmConnection = build.getProperty("scmConnection");
                 if (ObjectUtils.isBlank(scmConnection)) {
                     scmConnection = build.getProperty("scmDeveloperConnection");
                 }
                 if (!ObjectUtils.isBlank(scmConnection)) {
                     if (scmConnection.startsWith("scm:git:")) {
                         scmUrlFormat = build.getProperty("scmUrl") + "/commit/%s";
                     }
                 }
 
                 String commitsString = build.getProperty("gitCommits");
                 Map<String, List<GitCommit>> commitsMap = new LinkedHashMap<String, List<GitCommit>>();
                 if (!ObjectUtils.isBlank(commitsString)) {
                     String currRefNames = null;
                     for (String e : StringUtils.split(commitsString, "(?m)\\s*~-~\\s*")) {
                         GitCommit commit = new GitCommit(e, issuePattern);
                         String refNames = commit.refNames;
                         if (!ObjectUtils.isBlank(refNames)) {
                             if (refNames.startsWith("(")) {
                                 refNames = refNames.substring(1);
                             }
                             if (refNames.endsWith(")")) {
                                 refNames = refNames.substring(
                                         0, refNames.length() - 1);
                             }
                             currRefNames = refNames;
                         }
                         List<GitCommit> commits = commitsMap.get(currRefNames);
                         if (commits == null) {
                             commits = new ArrayList<GitCommit>();
                             commitsMap.put(currRefNames, commits);
                         }
                         commits.add(commit);
                     }
                 }
 
                 writeStart("h2").writeHtml("Commits").writeEnd();
 
                 writeStart("form", "action", "", "method", "GET", "class", "form-inline");
                     writeHtml("For: ");
                     writeStart("select", "style", "width:auto;", "id", "contextPicker", "name", "context", "class", "input-xlarge");
                         writeStart("option", "value", "");
                             writeHtml(getLabel(getServletContext()));
                         writeEnd();
                         for (Map.Entry<String, Properties> entry : embeddedProperties.entrySet()) {
                             writeStart("option", "value", entry.getKey(), "selected", entry.getKey().equals(buildContext) ? "selected" : null);
                                 writeHtml(getLabel(entry.getValue()));
                             writeEnd();
                         }
                     writeEnd();
                 writeEnd();
 
                 if (commitsMap.isEmpty()) {
                     writeStart("p", "class", "alert");
                         writeHtml("Not available!");
                     writeEnd();
 
                 } else {
                     int colspan = 3;
 
                     writeStart("table", "class", "table table-condensed table-striped");
                         writeStart("thead");
                             writeStart("tr");
 
                                 writeStart("th").writeHtml("Date").writeEnd();
 
                                 if (issuePattern != null) {
                                     writeStart("th").writeHtml("Issues").writeEnd();
                                     ++ colspan;
                                 }
 
                                 writeStart("th").writeHtml("Author").writeEnd();
                                 writeStart("th").writeHtml("Subject").writeEnd();
 
                                 if (scmUrlFormat != null) {
                                     writeStart("th").writeHtml("SCM").writeEnd();
                                     ++ colspan;
                                 }
                             writeEnd();
                         writeEnd();
 
                         writeStart("tbody");
                             for (Map.Entry<String, List<GitCommit>> entry : commitsMap.entrySet()) {
 
                                 writeStart("tr");
                                     writeStart("td", "class", "wrap", "colspan", colspan);
                                         writeStart("strong").writeHtml(entry.getKey()).writeEnd();
                                     writeEnd();
                                 writeEnd();
 
                                 for (GitCommit commit : entry.getValue()) {
                                    writeStart("tr", "class", commit.subject.startsWith("Merge branch ") ? "merge" : null);
 
                                         writeStart("td").writeHtml(commit.date).writeEnd();
 
                                         if (issuePattern != null) {
                                             writeStart("td");
                                                 for (String issue : commit.issues) {
                                                     if (issueUrlFormat != null) {
                                                         writeStart("a", "href", String.format(issueUrlFormat, issue), "target", "_blank");
                                                             writeHtml(issue);
                                                         writeEnd();
                                                     } else {
                                                         writeHtml(issue);
                                                     }
                                                     writeTag("br");
                                                 }
                                             writeEnd();
                                         }
 
                                         writeStart("td").writeHtml(commit.author).writeEnd();
                                         writeStart("td", "class", "wrap").writeHtml(commit.subject).writeEnd();
 
                                         if (scmUrlFormat != null) {
                                             writeStart("td");
                                                 writeStart("a", "href", String.format(scmUrlFormat, commit.hash), "target", "_blank");
                                                     writeHtml(commit.hash.substring(0, 6));
                                                 writeEnd();
                                             writeEnd();
                                         }
                                     writeEnd();
                                 }
                             }
                         writeEnd();
                     writeEnd();
                 }
 
                 writeStart("h2").writeHtml("Resources").writeEnd();
                 writeStart("table", "class", "table table-condensed");
                     writeStart("thead");
                         writeStart("tr");
                             writeStart("th").writeHtml("Path").writeEnd();
                             writeStart("th").writeHtml("Size (Bytes)").writeEnd();
                             writeStart("th").writeHtml("MD5").writeEnd();
                         writeEnd();
                     writeEnd();
                     writeStart("tbody");
                         writeResourcesOfPath("", 0, "/");
                     writeEnd();
                 writeEnd();
 
             endPage();
         }
 
             private void writeResourcesOfPath(String parentPath, int depth, String path) throws IOException {
                 writeStart("tr");
                 writeStart("td", "style", "padding-left: " + (depth * 20) + "px").writeHtml(path).writeEnd();
 
                 if (path.endsWith("/")) {
                     writeStart("td").writeEnd();
                     writeStart("td").writeEnd();
 
                     @SuppressWarnings("unchecked")
                     List<String> subPaths = new ArrayList<String>((Set<String>) getServletContext().getResourcePaths(path));
                     Collections.sort(subPaths);
 
                     int subDepth = depth + 1;
                     for (String subPath : subPaths) {
                         writeResourcesOfPath(path, subDepth, subPath);
                     }
 
                     writeEnd();
 
                 } else {
                     MessageDigest md5 = null;
                     try {
                         md5 = MessageDigest.getInstance("MD5");
                     } catch (NoSuchAlgorithmException ex) {
                     }
 
                     try {
                         InputStream input = getServletContext().getResourceAsStream(path);
                         if (input != null) {
                             try {
 
                                 if (md5 != null) {
                                     input = new DigestInputStream(input, md5);
                                 }
 
                                 int totalBytesRead = 0;
                                 int bytesRead = 0;
                                 byte[] buffer = new byte[4096];
                                 while ((bytesRead = input.read(buffer)) > 0) {
                                     totalBytesRead += bytesRead;
                                 }
 
                                 writeStart("td", "class", "num").writeObject(totalBytesRead).writeEnd();
                                 writeStart("td");
                                     if (md5 != null) {
                                         write(StringUtils.hex(md5.digest()));
                                     }
                                 writeEnd();
 
                             } finally {
                                 input.close();
                             }
                         }
 
                     } catch (IOException ex) {
                     }
 
                     writeEnd();
                 }
             }
         };
     }
 
     private static class GitCommit {
 
         public String hash;
         public String author;
         public Date date;
         public String refNames;
         public String subject;
         public String body;
         public List<String> issues;
 
         public GitCommit(String line, Pattern issuePattern) {
 
             String[] items = StringUtils.split(line, "(?m)\\s*~\\|~\\s*");
             hash = items[0];
             author = items[1];
             Long timestamp = ObjectUtils.to(Long.class, items[2]);
             if (timestamp != null) {
                 date = new Date(timestamp * 1000);
             }
 
             refNames = items.length > 3 ? items[3] : null;
             subject = items.length > 4 ? items[4] : null;
             body = items.length > 5 ? items[5] : null;
 
             if (issuePattern != null) {
                 issues = new ArrayList<String>();
                 for (String e : new String[] { subject, body }) {
                     if (e != null) {
                         Matcher matcher = issuePattern.matcher(e);
                         while (matcher.find()) {
                             issues.add(matcher.group(0));
                         }
                     }
                 }
             }
         }
     }
 }
