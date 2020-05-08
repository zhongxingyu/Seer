 package net.contextfw.web.commons.minifier;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.StringWriter;
 import java.net.URL;
 import java.util.Iterator;
 import java.util.List;
 
 import net.contextfw.web.application.DocumentProcessor;
 import net.contextfw.web.application.WebApplicationException;
 
 import org.apache.commons.io.IOUtils;
 import org.dom4j.Document;
 import org.dom4j.Element;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 
 import com.google.inject.Singleton;
 import com.yahoo.platform.yui.compressor.CssCompressor;
 
 @Singleton
 class CssMinifierServlet extends ContentServlet implements DocumentProcessor {
 
     private MinifierFilter filter;
 
     CssMinifierServlet(String host, 
                        String minifiedPath,
                        MinifierFilter filter,
                        long started,
                        String version) {
         super(host, minifiedPath, started, version);
         this.filter = filter;
     }
 
     private static final Logger LOG = LoggerFactory.getLogger(CssMinifierServlet.class);
     
     private static final long serialVersionUID = 1L;
 
     @Override
     public void process(Document document) {
         
         @SuppressWarnings("unchecked")
         List<Element> links = document.selectNodes("//html/head//link");
         
         StringBuilder sb = new StringBuilder();
         
         Element firstMinified = null;
 
         Iterator<Element> iter = links.iterator();
 
         while (iter.hasNext()) {
             Element link = iter.next();
             String src = link.attributeValue("href");
             if (src.startsWith("{$contextPath}") && filter.include(src)) {
                 LOG.info("Including CSS: {}", src);
                 URL url = getUrl(src.replace("{$contextPath}",
                         this.getServletContext().getContextPath()));
 
                 if (filter.minify(src)) {
                     LOG.info("Minifying CSS: {}", url.toString());
                     sb.append(compress(url)).append("\n");
                 } else {
                     try {
                         LOG.info("Not minifying CSS: {}", url.toString());
                         sb.append(IOUtils.toString(url.openStream())).append("\n");
                     } catch (IOException e) {
                         throw new WebApplicationException(e);
                     }
                 }
            }
 
            if (firstMinified == null) {
                firstMinified = link;
            } else {
                link.detach();
             }
         }
 
         if (firstMinified != null) {
             firstMinified.addAttribute("href", "{$contextPath}" + getMinifiedPath());
         }
 
         setContent(sb.toString());
     }
     
     private String compress(URL url) {
         try {
             CssCompressor compressor = new CssCompressor(new InputStreamReader(url.openStream()));
             StringWriter writer = new StringWriter();
             compressor.compress(writer, 0);
             return writer.toString();
         } catch (Exception e) {
             throw new WebApplicationException(url.toString(), e);
         }
     }
 
     @Override
     protected String getContentType() {
         return "text/css";
     }
 }
