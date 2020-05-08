 package com.dumbear.dumboj.spider;
 
 import com.dumbear.dumboj.util.Utility;
 
 public class HDUSpider extends Spider {
     public static final String SITE = "HDU";
     public static final String DEFAULT_CHARSET = "GB2312";
 
     @Override
     public void start(String id) throws Exception {
         id = Integer.toString(Integer.parseInt(id));
         String source = Utility.getHtmlSourceByGet("http://acm.hdu.edu.cn/showproblem.php?pid=" + id, DEFAULT_CHARSET, null);
         if (source.contains("<DIV>Invalid Parameter.</DIV>") || source.contains("<DIV>No such problem - ")) {
             throw new Exception("Problem not available");
         }
         //TODO check for it!
         source = source.replaceAll("(src|href)=(?!['\"]|[a-zA-Z0-9+.-]*:)(\\./|/)?([^\\s>]*)", "$1=\"http://acm.hdu.edu.cn/$3\"");
         source = source.replaceAll("(src|href)='(?![a-zA-Z0-9+.-]*:)(\\./|/)?([^']*)'", "$1=\"http://acm.hdu.edu.cn/$3\"");
         source = source.replaceAll("(src|href)=\"(?![a-zA-Z0-9+.-]*:)(\\./|/)?([^\"]*)\"", "$1=\"http://acm.hdu.edu.cn/$3\"");
         problem = new Problem();
         problemContent = new ProblemContent();
         problem.setTitle(Utility.getMatcherString(source, "color:#1A5CC8'>([\\s\\S]*?)</h1>", 1).trim());
         if (problem.getTitle().isEmpty()) {
             throw new Exception("Problem not available");
         }
        problem.setSource(Utility.getMatcherString(source, "<div class=panel_title align=left>Source</div> <div class=panel_content>([\\s\\S]*?)</div> <div class=panel_bottom>&nbsp;</div>", 1));
         problem.setOriginalUrl("http://acm.hdu.edu.cn/showproblem.php?pid=" + id);
         problem.setOriginalSite(SITE);
         problem.setOriginalId(id);
         problem.setTimeLimit(Integer.parseInt(Utility.getMatcherString(source, "(\\d*) MS", 1)));
         problem.setMemoryLimit(Integer.parseInt(Utility.getMatcherString(source, "/(\\d*) K", 1)));
         problemContent.setUserId(1);
        problemContent.setDescription(Utility.getMatcherString(source, "<div class=panel_title align=left>Problem Description</div> <div class=panel_content>([\\s\\S]*?)</div><div class=panel_bottom>&nbsp;</div>", 1));
        problemContent.setInput(Utility.getMatcherString(source, "<div class=panel_title align=left>Input</div> <div class=panel_content>([\\s\\S]*?)</div><div class=panel_bottom>&nbsp;</div>", 1));
        problemContent.setOutput(Utility.getMatcherString(source, "<div class=panel_title align=left>Output</div> <div class=panel_content>([\\s\\S]*?)</div><div class=panel_bottom>&nbsp;</div>", 1));
        problemContent.setSampleInput(Utility.getMatcherString(source, "<div class=panel_title align=left>Sample Input</div><div class=panel_content>([\\s\\S]*?)</div><div class=panel_bottom>&nbsp;</div>", 1));
        problemContent.setSampleOutput(Utility.getMatcherString(source, "<div class=panel_title align=left>Sample Output</div><div class=panel_content>([\\s\\S]*?)(<[^<>]*?><[^<>]*?><i>Hint|</div><div class=panel_bottom>&nbsp;</div>)", 1));
        problemContent.setHint(Utility.getMatcherString(source, "<i>Hint</i></div>([\\s\\S]*?)</div><i style='font-size:1px'> </i>", 1));
         problemContent.setRemark("Created by " + SITE + "Spider.");
     }
 }
