 package com.datastax.video;
 
 import com.google.common.io.Files;
 import java.io.File;
 import java.io.IOException;
 import java.nio.charset.Charset;
 import java.text.SimpleDateFormat;
 import java.util.Date;
 import java.util.UUID;
 
 /**
  *
  * @author Mark Kerzner
  */
 public class GenerateUserVideoIndex {
 
     public static void main(String argv[]) {
         if (argv.length != 2) {
             System.out.println("Arguments: number-users, average-videos-per-user");
             System.exit(0);
         }
         GenerateUserVideoIndex generator = new GenerateUserVideoIndex();
         int nUsers = Integer.parseInt(argv[0]);
         int nVideos = Integer.parseInt(argv[1]);
 
         try {
             generator.generate(nUsers, nVideos);
         } catch (Exception e) {
             System.out.println("Problem writig files: " + e.getMessage());
             e.printStackTrace(System.out);
         }
     }
 
     private void generate(int nUsers, int nAverageVideos) throws IOException {
         SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
         long DAY_MILS = 1000L * 60 * 60 * 24;
         new File(Util.ROOT_DIR).mkdirs();
         Charset charset = Charset.forName("US-ASCII");
         if (nAverageVideos < 1) {
             nAverageVideos = 1;
         }
         // users
         String userFile = Util.ROOT_DIR + "/" + Util.USER_VIDEO_INDEX;
         Util util = new Util();
         new File(userFile).delete();
         Files.append("use videodb;\n", new File(userFile), charset);
         for (int u = 1; u <= nUsers; ++u) {
             StringBuilder userPart = new StringBuilder();
             userPart.append("INSERT INTO username_video_index (username, videoid, upload_date, videoname) VALUES (");
             String firstName = util.generateName();
             String lastName = util.generateName();
             String username = firstName + lastName;
            userPart.append("'").append(username).append("',");
             // calculate the actual number of videos for this user
             // here we take an average between 1 and 2 * average, but 
             // TODO use a more realistic distribution, with long tail
            int nVideos = util.generateInt(1, 2 * nAverageVideos);          
             for (int e = 0; e < nVideos; ++e) {
                StringBuilder videoPart = new StringBuilder();
                 String videoid = UUID.randomUUID().toString();
                 String videoname = util.generateName();
                 // date the video back up to a year
                 String uploadDate = dateFormat.format(new Date().getTime() - util.generateInt(0, 365) * DAY_MILS);
                videoPart.append(videoid).append(",'").append(uploadDate).append("','").append(videoname).append("');").
                         append("\n");
                 Files.append(userPart.toString() + videoPart.toString(), new File(userFile), charset);
             }
         }
     }
 }
