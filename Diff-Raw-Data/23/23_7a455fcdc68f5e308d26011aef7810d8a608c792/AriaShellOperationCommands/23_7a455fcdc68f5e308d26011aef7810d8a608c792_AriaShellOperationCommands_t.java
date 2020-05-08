 package org.mvnsearch.aria2.shell.commands;
 
 import org.apache.commons.lang3.StringUtils;
 import org.apache.commons.lang3.SystemUtils;
 import org.fusesource.jansi.Ansi;
 import org.mvnsearch.aria2.service.AriaService;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.shell.core.CommandMarker;
 import org.springframework.shell.core.annotation.CliCommand;
 import org.springframework.shell.core.annotation.CliOption;
 import org.springframework.stereotype.Component;
 
 import javax.annotation.PostConstruct;
 import java.io.BufferedReader;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.*;
 
 /**
  * Aria Shell operation commands
  *
  * @author linux_china
  */
 @SuppressWarnings("StringConcatenationInsideStringBufferAppend")
 @Component
 public class AriaShellOperationCommands implements CommandMarker {
     /**
      * log
      */
     private Logger log = LoggerFactory.getLogger(AriaShellOperationCommands.class);
     /**
      * The platform-specific line separator.
      */
     public static final String LINE_SEPARATOR = SystemUtils.LINE_SEPARATOR;
     /**
      * aria service
      */
     @Autowired
     private AriaService ariaService;
 
     /**
      * init method: load current bucket
      */
     @PostConstruct
     public void init() {
         connect("localhost", "6800");
     }
 
     /**
      * config command to save aliyun OSS information
      *
      * @return result
      */
     @CliCommand(value = "connect", help = "Connect with aria xml-rpc")
     public String connect(@CliOption(key = {"host"}, mandatory = false, help = "Host") String host,
                           @CliOption(key = {"port"}, mandatory = false, help = "Port") String port) {
         try {
             if (StringUtils.isEmpty(host)) {
                 host = "localhost";
             }
             if (StringUtils.isEmpty(port)) {
                 port = "6800";
             }
             ariaService.connect(host, Integer.valueOf(port));
         } catch (Exception e) {
             log.error("connect", e);
             return wrappedAsRed(e.getMessage());
         }
         return "Connected with " + ariaService.getXmlRpcUrl();
     }
 
     /**
      * display global state
      *
      * @return global state
      */
     @CliCommand(value = "stats", help = "Display global state")
     public String stats() {
         try {
             StringBuilder builder = new StringBuilder();
             Map<String, String> globalStat = ariaService.getGlobalStat();
             for (Map.Entry<String, String> entry : globalStat.entrySet()) {
                 builder.append(entry.getKey() + ":" + entry.getValue() + LINE_SEPARATOR);
             }
             return builder.toString().trim();
         } catch (Exception e) {
             log.error("connect", e);
             return wrappedAsRed(e.getMessage());
         }
     }
 
     /**
      * download oss object
      *
      * @return message
      */
     @CliCommand(value = "add", help = "Add download url")
     public String add(@CliOption(key = {""}, mandatory = true, help = "URL") String url) {
         try {
             String gid = ariaService.addUri(url, Collections.emptyMap());
             return "Download added and GID is " + gid;
         } catch (Exception e) {
             log.error("add", e);
             return wrappedAsRed(e.getMessage());
         }
     }
 
     /**
      * print status
      *
      * @param status status
      */
     @SuppressWarnings("unchecked")
     private void printStatus(Map<String, Object> status) {
         Object[] files = (Object[]) status.get("files");
         System.out.println("Files:");
         for (Object temp : files) {
             Map<String, Object> file = (Map<String, Object>) temp;
             for (Map.Entry<String, Object> entry : file.entrySet()) {
                 Object value = entry.getValue();
                 if (value instanceof String) {
                     System.out.println("  " + entry.getKey() + ":" + value);
                 }
             }
             System.out.println("  uris:");
             Object[] uris = (Object[]) file.get("uris");
             for (Object temp2 : uris) {
                 Map<String, String> uri = (Map<String, String>) temp2;
                 for (Map.Entry<String, String> entry2 : uri.entrySet()) {
                     System.out.println("    " + entry2.getKey() + ":" + entry2.getValue());
                 }
             }
         }
         System.out.println("Basic:");
         for (Map.Entry<String, Object> entry : status.entrySet()) {
             if (!entry.getKey().equals("files")) {
                 System.out.println("  " + entry.getKey() + ":" + entry.getValue());
             }
         }
     }
 
     /**
      * info status
      *
      * @param gid download gid
      * @return status
      */
     @SuppressWarnings("unchecked")
     @CliCommand(value = "info", help = "Display gid information")
     public String info(@CliOption(key = {""}, mandatory = true, help = "gid") String gid) {
         try {
             if (gid.equals("stopped")) {
                 tellStopped();
             } else if (gid.equals("waitting")) {
                 tellWaiting();
             } else if (gid.equals("active")) {
                 tellActive();
             } else {
                 Map<String, Object> status = ariaService.tellStatus(gid);
                 if (status != null) {
                     printStatus(status);
                 }
             }
         } catch (Exception e) {
             log.error("info", e);
             return wrappedAsRed(e.getMessage());
         }
         return null;
     }
 
     /**
      * info stopped
      *
      * @return stopped information
      */
     @CliCommand(value = "stopped", help = "Stopped Queue")
     public String tellStopped() {
         try {
             System.out.println("==============Stopped==========");
             List<Map<String, Object>> items = ariaService.tellStopped(0, 10);
             for (Map<String, Object> item : items) {
                 printStatus(item);
                 System.out.println("==============================");
             }
             return null;
         } catch (Exception e) {
             log.error("tellStopped", e);
             return wrappedAsRed(e.getMessage());
         }
     }
 
     /**
      * info stopped
      *
      * @return stopped information
      */
    @CliCommand(value = "errors", help = "Errors")
    public String tellErrors() {
        try {
            System.out.println("==============Errors==========");
            List<Map<String, Object>> items = ariaService.tellStopped(0, 100);
            for (Map<String, Object> item : items) {
                if (item.get("status").equals("error")) {
                    printStatus(item);
                    System.out.println("==============================");
                }
            }
            return null;
        } catch (Exception e) {
            log.error("tellStopped", e);
            return wrappedAsRed(e.getMessage());
        }
    }

    /**
     * info stopped
     *
     * @return stopped information
     */
     @CliCommand(value = "waiting", help = "Waiting Queue")
     public String tellWaiting() {
         try {
             System.out.println("==============Waiting==========");
             List<Map<String, Object>> items = ariaService.tellWaiting(0, 10);
             for (Map<String, Object> item : items) {
                 printStatus(item);
                 System.out.println("========================");
             }
             return null;
         } catch (Exception e) {
             log.error("tellWaiting", e);
             return wrappedAsRed(e.getMessage());
         }
     }
 
     /**
      * info stopped
      *
      * @return stopped information
      */
     @CliCommand(value = "list", help = "Active Queue")
     public String tellActive() {
         try {
             System.out.println("==============Active==========");
             List<Map<String, Object>> items = ariaService.tellActive();
             for (Map<String, Object> item : items) {
                 printStatus(item);
                 System.out.println("============================");
             }
             return null;
         } catch (Exception e) {
             log.error("tellActive", e);
             return wrappedAsRed(e.getMessage());
         }
     }
 
     /**
      * start aria2
      *
      * @return start aria2
      */
     @CliCommand(value = "start", help = "Start aria2c")
     public String startAria2() {
         try {
             List<String> args = new ArrayList<String>();
             args.add("aria2c");
             args.add("--enable-rpc");
             args.add("--rpc-allow-origin-all");
             args.add("--rpc-listen-all");
             args.add("--dir=/tmp/aria");
             args.add("--daemon=true");
             executeCommand(args.toArray(new String[args.size()]));
             return connect("localhost", "6800");
         } catch (Exception e) {
             log.error("start", e);
             return wrappedAsRed(e.getMessage());
         }
     }
 
     /**
      * stop aria2
      *
      * @return stop status
      */
     @CliCommand(value = "stop", help = "Start aria2c")
     public String stopAria2() {
         try {
             List<String> args = new ArrayList<String>();
             args.add("killall");
             args.add("-9");
             args.add("aria2c");
             executeCommand(args.toArray(new String[args.size()]));
             return "Aria2 Stopped";
         } catch (Exception e) {
             log.error("start", e);
             return wrappedAsRed(e.getMessage());
         }
     }
 
     /**
      * wrapped as red with Jansi
      *
      * @param text text
      * @return wrapped text
      */
     private String wrappedAsRed(String text) {
         return Ansi.ansi().fg(Ansi.Color.RED).a(text).toString();
     }
 
 
     /**
      * wrapped as yellow with Jansi
      *
      * @param text text
      * @return wrapped text
      */
     private String wrappedAsYellow(String text) {
         return Ansi.ansi().fg(Ansi.Color.YELLOW).a(text).toString();
     }
 
     /**
      * execute command
      *
      * @param args args
      * @return output
      * @throws Exception exception
      */
     private String executeCommand(String[] args) throws Exception {
         Process process = new ProcessBuilder(args).start();
         InputStream is = process.getInputStream();
         InputStreamReader isr = new InputStreamReader(is);
         BufferedReader br = new BufferedReader(isr);
         List<String> lines = new ArrayList<String>();
         String line;
         while ((line = br.readLine()) != null) {
             lines.add(line);
         }
         process.destroy();
         return StringUtils.join(lines, "\r\n");
     }
 
 }
