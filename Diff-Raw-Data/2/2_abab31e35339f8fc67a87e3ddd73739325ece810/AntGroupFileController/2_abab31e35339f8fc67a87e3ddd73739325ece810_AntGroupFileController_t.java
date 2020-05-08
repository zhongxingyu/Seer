 package pl.agh.enrollme.controller;
 
 import org.primefaces.model.DefaultStreamedContent;
 import org.primefaces.model.StreamedContent;
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import pl.agh.enrollme.model.Enroll;
 import pl.agh.enrollme.model.Group;
 import pl.agh.enrollme.model.Person;
 import pl.agh.enrollme.model.Subject;
 import pl.agh.enrollme.repository.IGroupDAO;
 import pl.agh.enrollme.repository.ISubjectDAO;
 
 import java.io.*;
 import java.util.List;
 
 /**
  * @author Michal Partyka
  */
 @Controller
 public class AntGroupFileController {
     private static final File groupFile = new File("/tmp/groupFile.txt");
     private static final Logger LOGGER = LoggerFactory.getLogger(AntGroupFileController.class.getName());
     private StreamedContent streamedGroups;
 
     @Autowired
     IGroupDAO groupDAO;
 
     @Autowired
     ISubjectDAO subjectDAO;
 
 
    public void generateGroupsFile(Enroll enrollment) {
         try (BufferedWriter output = new BufferedWriter( new FileWriter(groupFile))) {
             output.write("\n" + generateGroupsAntFormat(enrollment));
         } catch (IOException e) {
             LOGGER.error("Some IO Problems: ", e);
         }
 
         try {
             streamedGroups = new DefaultStreamedContent( new FileInputStream(groupFile));
         } catch (FileNotFoundException e) {
             LOGGER.debug("there is no given file :(: " + groupFile.getName(), e);
         }
     }
 
     private String generateGroupsAntFormat(Enroll enrollment) {
         StringBuilder groupsOutput = new StringBuilder();
         groupsOutput.append("\n");
         List<Subject> subjects = enrollment.getSubjects();
         for ( Subject subject : subjects ) {
             List<Group> groups = subject.getGroups();
             for ( Group group : groups) {
                 groupsOutput.append(subject.getSubjectID()).append(":");
                 List<Person> participants = group.getPersons();
                 for (Person participant : participants) {
                     groupsOutput.append(participant.getIndeks()).append(",");
                 }
             }
             groupsOutput.append("\n");
         }
         return groupsOutput.toString();
     }
 
     public StreamedContent getStreamedGroups() {
         return streamedGroups;
     }
 
 }
