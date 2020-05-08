 package com.dumbear.dumboj.spider;
 
 import com.dumbear.dumboj.util.Utility;
 
 public class LiveArchiveSpider extends Spider {
     public static final String SITE = "LiveArchive";
     public static final String DEFAULT_CHARSET = "UTF-8";
 
     @Override
     public void start(String id) throws Exception {
         id = Integer.toString(Integer.parseInt(id));
        String source = Utility.getHtmlSourceByGet("http://livearchive.onlinejudge.org/index.php?option=com_onlinejudge&Itemid=8&page=show_problem&problem=" + id, DEFAULT_CHARSET, null);
         problem = new Problem();
         problemContent = new ProblemContent();
         problem.setTitle(Utility.getMatcherString(source, "<td>\\s*<h3>\\d+ - ([\\s\\S]*?)</h3>\\s*Time limit: [\\s\\S]*? seconds\\s*</td>", 1).trim());
         if (problem.getTitle().isEmpty()) {
             throw new Exception("Problem not available");
         }
         problem.setSource("");
         problem.setTimeLimit((int)(Double.parseDouble(Utility.getMatcherString(source, "<td>\\s*<h3>\\d+ - [\\s\\S]*?</h3>\\s*Time limit: ([\\s\\S]*?) seconds\\s*</td>", 1)) * 1000.0));
         problem.setMemoryLimit(0);
         problem.setOriginalId(Utility.getMatcherString(source, "<td>\\s*<h3>(\\d+) - [\\s\\S]*?</h3>\\s*Time limit: [\\s\\S]*? seconds\\s*</td>", 1));
        source = Utility.getHtmlSourceByGet("http://livearchive.onlinejudge.org/" + Utility.getMatcherString(source, "<iframe src=\"(external/[\\s\\S]*?)\"", 1), DEFAULT_CHARSET, null);
        source = Utility.resolveHtmlLinks(source, "http://livearchive.onlinejudge.org/external/" + (Integer.parseInt(problem.getOriginalId()) / 100) + "/", "http://livearchive.onlinejudge.org/");
        problem.setOriginalUrl("http://livearchive.onlinejudge.org/index.php?option=com_onlinejudge&Itemid=8&page=show_problem&problem=" + id);
         problem.setOriginalSite(SITE);
         problemContent.setUserId(1);
         problemContent.setDescription(Utility.getMatcherString(source, "^([\\s\\S]*?)<H2><FONT size=4 COLOR=#ff0000><A NAME=\"SECTION000100\\d000000000000000\">", 1));
         if (problemContent.getDescription().isEmpty()) {
             problemContent.setDescription(source);
             problemContent.setInput("");
             problemContent.setOutput("");
             problemContent.setSampleInput("");
             problemContent.setSampleOutput("");
         } else {
             problemContent.setInput(Utility.getMatcherString(source, "Input</A>&nbsp;</FONT>\\s*</H2>([\\s\\S]*?)<H2><FONT size=4 COLOR=#ff0000><A NAME=\"SECTION000100\\d000000000000000\">", 1));
             problemContent.setOutput(Utility.getMatcherString(source, "Output</A>&nbsp;</FONT>\\s*</H2>([\\s\\S]*?)<H2><FONT size=4 COLOR=#ff0000><A NAME=\"SECTION000100\\d000000000000000\">", 1));
             problemContent.setSampleInput(Utility.getMatcherString(source, "Sample Input</A>&nbsp;</FONT>\\s*</H2>([\\s\\S]*?)<H2><FONT size=4 COLOR=#ff0000><A NAME=\"SECTION000100\\d000000000000000\">", 1));
             problemContent.setSampleOutput(Utility.getMatcherString(source, "Sample Output</A>&nbsp;</FONT>\\s*</H2>([\\s\\S]*)", 1));
         }
         problemContent.setHint("");
         problemContent.setRemark("Created by " + SITE + "Spider.");
     }
 }
