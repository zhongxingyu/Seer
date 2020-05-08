 package dapp;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.InputStream;
 import java.io.InputStreamReader;
 import java.util.ArrayList;
 import java.util.List;
 import java.util.regex.Matcher;
 import java.util.regex.Pattern;
 
 public class GrabScreens {
 	public static String screens(String frxFileName) throws Exception {
 		 System.out.println(new File(".").toURI());
          MediaInfo minfo = new MediaInfo();
          String assembled = "";
          assembled += minfo.grabInfo(frxFileName);
          String ffm_exe;
          if (System.getProperty("sun.arch.data.model")=="64") {
              ffm_exe = "win64\\/ffmpeg64.exe";
          } else {
              ffm_exe = "win32\\/ffmpeg32.exe";
          }
          String line, output = "";
          System.out.println("Runonce1");
          List<String> cmdargs = new ArrayList<String>();
          cmdargs.add(ffm_exe);
          cmdargs.add("-i");
          cmdargs.add(frxFileName);
          InfoApp.frame.setTitle("Processing advanced video info...");
          ProcessBuilder process = new ProcessBuilder(cmdargs);
          process.redirectErrorStream(true);
          Process p = process.start();
          InputStream is = p.getInputStream();
          InputStreamReader isr = new InputStreamReader(is);
          BufferedReader br = new BufferedReader(isr);
          output = "";
          while ((line = br.readLine()) != null) {
              System.out.println(line);
              output += line;
          }
          System.out.println("Runonce2");
          p.waitFor();
          TextUpdate.txtAppend("Waiting");
          Thread.sleep(3000);
          Pattern dar1, dar2;
          Matcher m1, m2;
          int w = 0, h = 0, parx = 0, pary = 0;
          dar1 = Pattern.compile("(\\d+)x(\\d+), PAR (\\d+):(\\d+) DAR");
          dar2 = Pattern.compile("(\\d+)x(\\d+) \\[PAR (\\d+):(\\d+) DAR");
          m1 = dar1.matcher(output);
          m2 = dar2.matcher(output);
          boolean x4 = false;
          if (m1.find()) {
              w = Integer.parseInt(m1.group(1));
              h = Integer.parseInt(m1.group(2));
              parx = Integer.parseInt(m1.group(3));
              pary = Integer.parseInt(m1.group(4));
              x4 = true;
          } else if (m2.find()) {
              w = Integer.parseInt(m2.group(1));
              h = Integer.parseInt(m2.group(2));
              parx = Integer.parseInt(m2.group(3));
              pary = Integer.parseInt(m2.group(4));
              x4 = true;
          } else {
              // Anamorphic problem?
              // need to grab mediainfo stuff here?
         	 TextUpdate.txtAppend("There was a problem finding the anamorphic resolution of this file. We have falled back to the normal resolution from mediainfo.");
          }
          int width = 0;
          int height = 0;
          if (x4) {
              // Modify resolution
              width = (w * parx) / pary;
              height = h;
          }
          TextUpdate.txtAppend("Modified res: " + w + "x" + h + " to " + width + "x" + height);
          String tempdir = System.getProperty("java.io.tmpdir");
 
          if (!(tempdir.endsWith("/") || tempdir.endsWith("\\"))) {
                  tempdir = tempdir + System.getProperty("file.separator");
          }
 
          Matcher mx;
          Pattern px = Pattern.compile("(?:(\\d+)h\\s?)?(?:(\\d+)mn\\s?)?(?:(\\d+)s\\s?)?", Pattern.MULTILINE);
          String rx = assembled.substring(assembled.indexOf("Duration")).trim();
          rx = rx.substring(0, rx.indexOf("\n"));
          System.out.println(px.pattern());
          int duration = 0;
          assembled = "";
          mx = px.matcher(rx);
          while (mx.find()) {
              if (mx.group(1) != null) {
                  duration += Integer.parseInt(mx.group(1)) * 60 * 60;
              }
              if (mx.group(2) != null) {
                  duration += Integer.parseInt(mx.group(2)) * 60;
              }
              if (mx.group(3) != null) {
                  duration += Integer.parseInt(mx.group(3));
              }
          }
          for (int y = 1; y <= 3; y++) {
         	 InfoApp.frame.setTitle("Taking screenshot "+y+" of 3");
              if (!cmdargs.isEmpty()) {
                  cmdargs.clear();
              }
              if(!new File(ffm_exe).exists()) {
             	 TextUpdate.txtAppend("FFmpeg missing! Please put ffmpeg32.exe or ffmpeg64.exe into your app directory.");
                  throw new Exception("FFmpeg missing!");
              }
              cmdargs.add(ffm_exe);
              cmdargs.add("-an");
              cmdargs.add("-sn");
              cmdargs.add("-ss");
              int len = 0;
              switch (y) {
                  case 1:
                      len = (int) (duration * 0.10);
                      break;
                  case 2:
                      len = (int) (duration * 0.15);
                      break;
                  case 3:
                      len = (int) (duration * 0.2);
                      break;
              }
 
              cmdargs.add(Integer.toString(len));
              cmdargs.add("-i");
              cmdargs.add(frxFileName);
              cmdargs.add("-vcodec");
 //, "png", "-vframes", "1", outputPngPath
              cmdargs.add("png");
              cmdargs.add("-vframes");
              cmdargs.add("1");
 
              if (x4) {
                  cmdargs.add("-s");
                  cmdargs.add(width + "x" + height);
              }
 
              cmdargs.add("-y");
              cmdargs.add(tempdir + "ss" + y + ".png");
              TextUpdate.txtAppend("Generating screenshots... ("+y+" of 3)");
              process = new ProcessBuilder(cmdargs);
              process.redirectErrorStream(true);
              p = process.start();
              is = p.getInputStream();
              isr = new InputStreamReader(is);
              br = new BufferedReader(isr);
              output = "";
              while ((line = br.readLine()) != null) {
                  System.out.println(line);
                  output += line;
              }
              InfoApp.frame.setTitle("Uploading screenshot "+y+" of 3");
              TextUpdate.txtAppend("Uploading: " + tempdir + "ss" + y + ".png\n");
              assembled += FileUpload.UploadFile(tempdir + "ss" + y + ".png");
          }
         return assembled;
 	}
 
 }
