 package com.page5of4.mustache.spring;
 
 import java.io.IOException;
 import java.io.InputStream;
 import java.io.Writer;
 import java.util.Date;
 import java.util.HashMap;
 import java.util.Map;
 
 import javax.servlet.http.HttpServletResponse;
 
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringEscapeUtils;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.beans.factory.annotation.Value;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestMethod;
 
 import com.google.common.collect.Maps;
 import com.page5of4.mustache.TemplateSourceLoader;
 import com.page5of4.mustache.TemplateSourceLoader.TemplateSource;
 
 public class MustacheTemplatesController {
    private static final Map<String, String> keywords = Maps.newHashMap();
 
    static {
       String[] reserved = new String[] {
             "break", "else", "new", "var",
             "case", "finally", "return", "void",
             "catch", "for", "switch", "while",
             "continue", "function", "this", "with",
             "default", "if", "throw",
             "delete", "in", "try",
             "do", "instanceof", "typeof", "true", "false"
       };
       for(String word : reserved) {
          keywords.put(word, "_" + word);
       }
    }
 
   @Value("${mustache.templates.controller.allow_caching:false}")
    private boolean allowCaching;
 
    @Autowired
    private TemplateSourceLoader templateSourceLoader;
 
    public void setTemplateSourceLoader(TemplateSourceLoader templateSourceLoader) {
       this.templateSourceLoader = templateSourceLoader;
    }
 
    @RequestMapping(method = RequestMethod.GET)
    public void index(HttpServletResponse servletResponse) throws IOException {
       servletResponse.setContentType("text/javascript");
       if(allowCaching) {
          addCacheHeaders(servletResponse);
       }
       templates(servletResponse.getWriter());
    }
 
    private void addCacheHeaders(HttpServletResponse response) {
       response.addHeader("Cache-Control", "public, max-age=315360000, post-check=315360000, pre-check=315360000");
       response.addHeader("Expires", new Date(new Date().getTime() + 315360000).toString());
    }
 
    public void templates(Writer writer) throws IOException {
       Writer sb = writer;
       sb.write("if (typeof(templates) == 'undefined') templates = {};\n");
       sb.write("if (typeof(templates._internal) == 'undefined') templates._internal = {};\n");
       sb.write("\n");
       sb.write("if (typeof(templates.partials) == 'undefined') templates.partials = function(parent, path) {\n");
       sb.write("  var parts = path.split(\"/\");\n");
       sb.write("  var iter = parent;\n");
       sb.write("  if (path[0] == \"/\") {\n");
       sb.write("    iter = templates._internal;\n");
       sb.write("  }\n");
       sb.write("  for ( var i = 0; i < parts.length; ++i) {\n");
       sb.write("    if (parts[i] != \"\") {\n");
       sb.write("      iter = iter[parts[i]];\n");
       sb.write("    }\n");
       sb.write("  }\n");
       sb.write("  return iter.body;\n");
       sb.write("};\n");
       sb.write("\n");
       sb.write("if (typeof(templates.prepare) == 'undefined') templates.prepare = function(model) {\n");
       sb.write("  return model;\n");
       sb.write("};\n");
       sb.write("\n");
       sb.write("var body = null;\n");
       sb.write("\n");
       for(Map.Entry<String, String> entry : all().entrySet()) {
          String key = escapePathKeywords(entry.getKey());
          String folderKey = key.lastIndexOf('.') >= 0 ? key.substring(0, key.lastIndexOf('.')) : key;
          StringBuilder path = new StringBuilder();
          String[] parts = key.split("\\.");
          for(int i = 0; i < parts.length; ++i) {
             String part = parts[i];
             if(path.length() != 0) {
                path.append(".");
             }
             path.append(part);
             if(i < parts.length - 1) {
                sb.append(String.format("if (typeof(templates._internal.%s) == 'undefined') templates._internal.%s = {};\n", path.toString(), path.toString()));
                sb.append(String.format("if (typeof(templates.%s) == 'undefined') templates.%s = {};\n", path.toString(), path.toString()));
             }
          }
          sb.write(String.format("\n"));
          sb.write(String.format("body = \"%s\";\n", StringEscapeUtils.escapeJavaScript(entry.getValue())));
          sb.write(String.format("templates._internal.%s = {\n", key));
          sb.write(String.format("  body : body,\n"));
          sb.write(String.format("  compiled : Mustache.compile(body),\n"));
          sb.write(String.format("  partials : function(name) {\n    return templates.partials(templates._internal.%s, name);\n  }\n", folderKey));
          sb.write(String.format("};\n"));
          sb.write(String.format("templates.%s = function(model) {\n", key));
          sb.write(String.format("  return templates._internal.%s.compiled(templates.prepare(model), templates._internal.%s.partials);\n", key, key));
          sb.write("}\n\n");
       }
    }
 
    private String escapePathKeywords(String path) {
       StringBuilder escaped = new StringBuilder();
       for(String part : path.split("\\.")) {
          if(escaped.length() != 0) {
             escaped.append(".");
          }
          String escapedPart = part;
          if(keywords.containsKey(part)) {
             escapedPart = keywords.get(part);
          }
          escaped.append(escapedPart);
       }
       return escaped.toString();
    }
 
    private Map<String, String> all() {
       try {
          Map<String, String> all = new HashMap<String, String>();
          for(TemplateSource template : templateSourceLoader.getTemplates()) {
             InputStream stream = template.getResource().getInputStream();
             try {
                all.put(template.getRelativePath().replace("/", ".").replace("-", "_"), IOUtils.toString(stream));
             }
             finally {
                stream.close();
             }
          }
          return all;
       }
       catch(Exception e) {
          throw new RuntimeException(e);
       }
    }
 }
