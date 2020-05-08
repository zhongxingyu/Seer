 package com.raspitor;
 
 import org.vertx.java.core.Handler;
 import org.vertx.java.core.json.JsonArray;
 import org.vertx.java.core.json.JsonObject;
 import org.vertx.java.platform.Verticle;
 
 import java.io.BufferedReader;
 import java.io.InputStreamReader;
 import java.util.Arrays;
 import java.util.Scanner;
 
 public class StatSender extends Verticle {
     @Override
     public void start() {
         super.start();
 
         vertx.setPeriodic(2000, new Handler<Long>() {
             @Override
             public void handle(Long event) {
                 try {
                     vertx.eventBus().send("web.client", getSystemInfo());
                 } catch (Exception e) {
                    e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
                 }
                getContainer().logger().info("Message sent");
             }
         });
 
         getContainer().logger().info("StatsSender started");
     }
 
     public JsonObject getSystemInfo() throws Exception {
         JsonObject data = new JsonObject();
 
         ProcessBuilder processBuilder = new ProcessBuilder(Arrays.asList("top", "-b", "-n", "1"));
         Process top = processBuilder.start();
         top.waitFor();
         BufferedReader br = new BufferedReader(new InputStreamReader(top.getInputStream()));
         StringBuilder builder = new StringBuilder();
         String line = null;
         while ((line = br.readLine()) != null) {
             builder.append(line);
             builder.append(System.getProperty("line.separator"));
         }
         String result = builder.toString();
         Scanner scanner = new Scanner(result.replaceAll(",", "."));
 
         if (!scanner.hasNextLine()) return data;
         String firstLine = scanner.nextLine();
 
         if (!scanner.hasNextLine()) return data;
         String taskLine = scanner.nextLine();
         data.putNumber("tasks", Integer.parseInt(taskLine.substring(7, 10).trim()));
 
         if (!scanner.hasNextLine()) return data;
         String cpuLine = scanner.nextLine();
        data.putNumber("avgCpu", Double.parseDouble(cpuLine.substring(9, 13).trim()));
 
         if (!scanner.hasNextLine()) return data;
         String memLine = scanner.nextLine();
         data.putNumber("memUsed", Integer.parseInt(memLine.substring(27, 34).trim()));
         data.putNumber("memFree", Integer.parseInt(memLine.substring(42, 49).trim()));
         data.putNumber("memPerc", (data.getNumber("memUsed").intValue() * 100) / (data.getNumber("memUsed").intValue() + data.getNumber("memFree").intValue()));
 
         if (!scanner.hasNextLine()) return data;
         String swapLine = scanner.nextLine();
         data.putNumber("swapUsed", Integer.parseInt(swapLine.substring(27, 34).trim()));
         data.putNumber("swapFree", Integer.parseInt(swapLine.substring(42, 49).trim()));
         if ((data.getNumber("swapUsed").intValue() + data.getNumber("swapFree").intValue()) == 0) {
             data.putNumber("swapPerc", 0);
         } else {
             data.putNumber("swapPerc", (data.getNumber("swapUsed").intValue() / (data.getNumber("swapUsed").intValue() + data.getNumber("swapFree").intValue()) * 100));
         }
 
         if (!scanner.hasNextLine()) return data;
         String emptyLine = scanner.nextLine();
 
         if (!scanner.hasNextLine()) return data;
         String titleLine = scanner.nextLine();
 
         JsonArray array = new JsonArray();
         while (scanner.hasNextLine()) {
             JsonObject object = new JsonObject();
             String topLine = scanner.nextLine();
             object.putNumber("pid", Integer.parseInt(topLine.substring(0, 6).trim()));
             object.putString("user", topLine.substring(6, 16).trim());
             object.putNumber("cpu", Double.parseDouble(topLine.substring(42, 46).trim()));
             object.putNumber("mem", Double.parseDouble(topLine.substring(46, 51).trim()));
             object.putString("command", topLine.substring(62, topLine.length()).trim());
             array.add(object);
         }
 
         data.putArray("topProc", array);
 
         return data;
     }
 }
