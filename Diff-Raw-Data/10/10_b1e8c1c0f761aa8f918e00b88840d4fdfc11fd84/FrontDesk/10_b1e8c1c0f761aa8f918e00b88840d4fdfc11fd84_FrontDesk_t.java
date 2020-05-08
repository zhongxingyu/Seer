 package gutenberg.workers;
 
 import gutenberg.blocs.EntryType;
 import gutenberg.blocs.ManifestType;
 import gutenberg.blocs.StudentGroupType;
 import gutenberg.blocs.TeacherType;
 
 import java.io.BufferedReader;
 import java.io.File;
 import java.io.FileReader;
 import java.io.FileWriter;
 import java.io.InputStreamReader;
 import java.io.PrintWriter;
 
 import java.nio.file.Files;
 import java.nio.file.Path;
 import java.nio.file.StandardCopyOption;
 
 public class FrontDesk {
 
     public FrontDesk(Config config) {
         FRONTDESK = config.getPath(Resource.frontdesk);
         SHARED = config.getPath(Resource.shared);
     }
 
     public ManifestType generateSuggestionForm(TeacherType teacherInfo)
             throws Exception {
 
         EntryType teacher = teacherInfo.getTeacher();
         EntryType school = teacherInfo.getSchool();
 
         File templateDir = new File(SHARED);
         File templateFile = templateDir.toPath().resolve("suggestion.tex")
                 .toFile();
         File outputDir = new File(String.format("%s/teachers/%s-%s/petty-cash",
                 FRONTDESK, school.getId(), teacher.getId()));
         if (!outputDir.exists())
             outputDir.mkdirs();
         File workingDir = outputDir.toPath().resolve("working").toFile();
         workingDir.mkdir();
         File tex = workingDir.toPath().resolve("suggestion.tex").toFile();
 
         String line = null, trimmed = null;
         PrintWriter writer = new PrintWriter(new FileWriter(tex));
         BufferedReader reader = new BufferedReader(new FileReader(templateFile));
 
         while ((line = reader.readLine()) != null) {
             trimmed = line.trim();
             if (trimmed.startsWith(school_tag)) {
                 line = line.replace("school", school.getName());
             } else if (trimmed.startsWith(author_tag)) {
                 line = line
                         .replace("name", teacher.getName().replace('-', ' '));
             } else if (trimmed.startsWith(insertQRC_tag)) {
                 line = line.replace("qrc", String.format("%s-%s-0-0-0-1-1",
                         teacher.getId(), teacher.getName()));
             }
             writer.println(line);
         }
         writer.close();
 
         Files.createSymbolicLink(workingDir.toPath().resolve("Makefile"),
                 templateDir.toPath().resolve("suggestion.mk"));
 
         String outputFile = String.format("%s-%s.pdf", teacher.getId(), teacher
                 .getName().split("-")[0]);
         generatePdf(workingDir, outputDir.toPath().resolve(outputFile).toFile());
 
         ManifestType manifest = new ManifestType();
         manifest.setRoot(outputDir.getPath());
         EntryType document = new EntryType();
         document.setId(outputFile);
         manifest.addDocument(document);
         return manifest;
     }
 
     public ManifestType generateStudentRoster(StudentGroupType studentGroup)
             throws Exception {
 
         EntryType group = studentGroup.getGroup();
         EntryType school = studentGroup.getSchool();
         EntryType[] members = studentGroup.getMembers();
         
         if (members == null) members = new EntryType[0];
         
         if (members.length % 2 != 0) {
             throw new Exception(
                     "Doh! Come on, send an even number of elements I say!");
         }        
 
         File templateDir = new File(SHARED);
         File templateFile = templateDir.toPath().resolve("roster.tex").toFile();
         File outputDir = new File(String.format("%s/schools/%s", FRONTDESK,
                 school.getId()));
         if (!outputDir.exists())
             outputDir.mkdir();
         File workingDir = outputDir.toPath().resolve(group.getId()).toFile();
         workingDir.mkdir();
         File tex = workingDir.toPath().resolve("roster.tex").toFile();
 
         String line = null, trim = null;
         PrintWriter writer = new PrintWriter(new FileWriter(tex));
         BufferedReader reader = new BufferedReader(new FileReader(templateFile));
 
         while ((line = reader.readLine()) != null) {
             trim = line.trim();
            if (trim.contains("{school}")) {
                 line = line.replace("school", school.getName());
            } else if (trim.contains("{sektion}")) {
                 line = line.replace("sektion", group.getName());
             } else if (trim.startsWith(table_end)) {
                // number of lines = members.length / 2
                 String row = "%s & %s & & %s & %s \\\\";
                for (int i = 0; i < members.length; i+=2) {
                     writer.println(String.format(row, members[i].getName(),
                             members[i].getId(), members[i + 1].getName(),
                             members[i + 1].getId()));
                 }
             }
             writer.println(line);
         }
         writer.close();
 
         Files.createSymbolicLink(workingDir.toPath().resolve("Makefile"),
                 templateDir.toPath().resolve("roster.mk"));
 
         String outputFile = String.format("%s-%s.pdf", group.getId(), group
                 .getName().replace(' ', '_'));
         generatePdf(workingDir, outputDir.toPath().resolve(outputFile).toFile());
 
         ManifestType manifest = new ManifestType();
         manifest.setRoot(outputDir.getPath());
         EntryType document = new EntryType();
         document.setId(outputFile);
         manifest.addDocument(document);
         return manifest;
     }
 
     private String FRONTDESK, SHARED;
     private final String school_tag = "\\School", author_tag = "\\DocAuthor",
             insertQRC_tag = "\\insertQR", table_end = "\\end{tabular}";
 
     private int generatePdf(File workingDir, File outputFile) throws Exception {
         ProcessBuilder pb = new ProcessBuilder("make");
 
         pb.directory(workingDir);
         pb.redirectErrorStream(true);
 
         Process build = pb.start();
         BufferedReader messages = new BufferedReader(new InputStreamReader(
                 build.getInputStream()));
 
         String line = null;
         while ((line = messages.readLine()) != null) {
             System.out.println(line);
         }
 
         if (build.waitFor() == 0) {
             Path src = workingDir.listFiles(new NameFilter(".pdf"))[0].toPath();
             Files.move(src, outputFile.toPath(),
                     StandardCopyOption.REPLACE_EXISTING);
 
             ProcessBuilder pClean = new ProcessBuilder("make", "clean");
             pClean.directory(workingDir);
             Process clean = pClean.start();
             if (clean.waitFor() == 0) {
                 Files.delete(workingDir.toPath().resolve("Makefile"));
                 Files.delete(workingDir.toPath());
             }
         }
 
         return 0;
     }
 }
