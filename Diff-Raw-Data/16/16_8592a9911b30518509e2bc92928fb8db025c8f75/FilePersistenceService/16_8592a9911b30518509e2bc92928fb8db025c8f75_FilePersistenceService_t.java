 package softwareart.booking.persistence;
 
 import softwareart.booking.BookingService;
 import softwareart.booking.Participant;
 import softwareart.booking.Workshop;
 import softwareart.booking.exceptions.FileNotReadableException;
 import softwareart.booking.exceptions.FileNotRemovableException;
 import softwareart.booking.exceptions.FileNotWritableException;
 
 import java.io.*;
 import java.util.Arrays;
 import java.util.List;
 
 public class FilePersistenceService implements PersistenceService {
     public static final String SEPARATOR = ";";
     private File file;
 
     public FilePersistenceService(File file) {
         this.file = file;
     }
 
     @Override
     public void saveBooking(Participant participant, Workshop[] workshops) {
         try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))) {
             writer.write(participant.getEmail());
             writer.write(SEPARATOR);
             writer.write(participant.getName());
             writer.write(SEPARATOR);
             writer.write(participant.isConfirmed().toString());
             for (Workshop workshop : workshops) {
                 writer.write(SEPARATOR);
                 writer.write(workshop.getId().toString());
             }
             writer.newLine();
         } catch (IOException e) {
             throw new FileNotWritableException(e);
         }
     }
 
     @Override
     public void removeBookingFromFile(Participant participant) {
         File tempFile = new File(file.getAbsolutePath() + ".tmp");
         try (BufferedReader br = new BufferedReader(new FileReader(file)); PrintWriter pw = new PrintWriter(new FileWriter(tempFile), true)) {
             String line;
             //Read from the original file and write to the new
             //unless content matches data to be removed.
             while ((line = br.readLine()) != null) {
                 if (!line.trim().startsWith(participant.getEmail())) {
                     pw.println(line);
                 }
             }
         } catch (IOException ex) {
             // should not happen?
             ex.printStackTrace();
         }
 
         if (!file.delete()) {
             throw new FileNotRemovableException();
         }
         tempFile.renameTo(file);
     }
 
     @Override
     public void makeBookingsBasedOnFile(BookingService bookingService) {
         try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
             String line;
             while ((line = reader.readLine()) != null) {
                 if (line.isEmpty()) {
                     continue;
                 }
                 String[] booking = line.split(";");
                 Participant participant = new Participant(booking[0], booking[1]);
                 Integer[] workshops = new Integer[booking.length - 3];
                 for (int i = 3; i < booking.length; i++) {
                     workshops[i - 3] = Integer.parseInt(booking[i]);
                 }
 
                bookingService.bookLocal(participant, workshops);
             }
         } catch (IOException e) {
             throw new FileNotReadableException(e);
         }
     }
 
     @Override
     public void confirm(String mail, Integer... workshopIds) {
         List<Integer> idList = Arrays.asList(workshopIds);
 
         File tempFile = new File(file.getAbsolutePath() + ".tmp");
         try (BufferedReader br = new BufferedReader(new FileReader(file)); PrintWriter pw = new PrintWriter(new FileWriter(tempFile), true)) {
             String line;
             while ((line = br.readLine()) != null) {
                 if (line.isEmpty())
                     continue;
                 if (line.startsWith(mail)) {
                     String[] split = line.split(";");
                     if (split.length - 3 == workshopIds.length) {
                         for (int i = 3; i < split.length; i++) {
                             if (!split[i].equals(workshopIds[i - 3].toString())) {
                                 break;
                             }
                             pw.println(line.replace(";false;", ";true;"));
                         }
                     }
                 } else {
                     pw.println(line);
                 }
             }
         } catch (IOException ex) {
             // should not happen?
             ex.printStackTrace();
         }
 
         if (!file.delete()) {
             throw new FileNotRemovableException();
         }
         tempFile.renameTo(file);
     }
 
 }
