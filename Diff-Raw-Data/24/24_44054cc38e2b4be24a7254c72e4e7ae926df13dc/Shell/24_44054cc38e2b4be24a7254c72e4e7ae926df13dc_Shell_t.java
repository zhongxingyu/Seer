 package org.n3r.sshe.ssh;
 
 import com.jcraft.jsch.ChannelShell;
 import org.apache.commons.lang3.StringUtils;
 import org.n3r.sshe.SettingKey;
 import org.n3r.sshe.SsheConf;
 import org.n3r.sshe.SsheHost;
 
 import java.io.IOException;
 import java.io.InputStreamReader;
 import java.io.PipedInputStream;
 import java.io.PipedOutputStream;
 import java.util.regex.Pattern;
 
 public class Shell {
     static boolean excludeLinePatternParsed = false;
     static Pattern excludeLinePattern = null;
 
     public static void waitUntilExpect(SsheHost ssheHost, String expect, String command) {
         InputStreamReader is = ssheHost.getChannleOutput();
         char[] buff = new char[1024];
         StringBuilder fullResponse = new StringBuilder();
         try {
             int read;
             while ((read = is.read(buff)) != -1) {
                 String response = new String(buff, 0, read);
                 filterResponse(fullResponse, response);
                 if (response.indexOf(expect) >= 0) break;
             }
 
             while (is.ready()) {
                 read = is.read(buff);
                 String response = new String(buff, 0, read);
                 filterResponse(fullResponse, response);
             }
 
             SsheConf.collect(ssheHost.getOperationCollector(), fullResponse, null);
         } catch (IOException e) {
             throw new RuntimeException(e);
         }
     }
 
     private static void filterResponse(StringBuilder fullResponse, String response) {
         // remove ANSI terminal escape sequence
         // http://stackoverflow.com/questions/14652538/remove-ascii-color-codes
         response = response.replaceAll("\u001B\\[[;\\d]*m", "");
 
         fullResponse.append(response);
 
         if (!excludeLinePatternParsed) {
             excludeLinePatternParsed = true;
             String excludeLinePattern = SsheConf.settings.get(SettingKey.excludeLinePattern);
             if (StringUtils.isNotEmpty(excludeLinePattern))
                 Shell.excludeLinePattern = Pattern.compile(excludeLinePattern, Pattern.MULTILINE);
         }
 
         int start = 0;
         int linePos = response.indexOf("\n");
         while (linePos > 0) {
             String line = response.substring(start, linePos);
 
             if (excludeLinePattern == null || !excludeLinePattern.matcher(line).find())
                 SsheConf.console.println(line);
 
             start = linePos + 1;
             if (start >= response.length()) break;
             linePos = response.indexOf("\n", start);
         }
 
         SsheConf.console.print(response.substring(start));
     }
 
     public static void createShell(SsheHost ssheHost) throws Exception {
         PipedOutputStream pos = new PipedOutputStream();
         PipedInputStream pis = new PipedInputStream(pos);
 
         ChannelShell channel = (ChannelShell) ssheHost.getSession().openChannel("shell");
 
         channel.setInputStream(pis, true);
        // 设置一个足够大的终端大小，以免过长命令显示有问题
        channel.setPtySize(8000, 30, 8000 * 10, 30 * 20);
 
         PipedOutputStream poos = new PipedOutputStream();
         PipedInputStream pois = new PipedInputStream(poos);
         channel.setOutputStream(poos, true);
 
         String ptyType = SsheConf.settings.get(SettingKey.ptyType);
        if (StringUtils.isNotEmpty(ptyType)) channel.setPtyType(ptyType);  // eg. dump
 
         channel.connect(10 * 1000);
 
         ssheHost.setChannelShell(channel);
         ssheHost.setOutputStream(pos);
         ssheHost.setChannelOutput(pois);
     }
 }
