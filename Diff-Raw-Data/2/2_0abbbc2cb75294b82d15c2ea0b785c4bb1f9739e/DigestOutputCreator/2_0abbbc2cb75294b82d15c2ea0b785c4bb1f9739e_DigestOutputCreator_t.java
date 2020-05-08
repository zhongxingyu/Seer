 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 
 package nl.minjus.nfi.dt.jhashtools;
 
 import java.io.File;
 import java.io.FileNotFoundException;
 import java.io.FileOutputStream;
 import java.io.IOException;
 import java.io.OutputStream;
 import java.io.OutputStreamWriter;
 import java.io.PrintWriter;
 import java.nio.charset.Charset;
 import java.security.NoSuchAlgorithmException;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import nl.minjus.nfi.dt.jhashtools.exceptions.PersistenceException;
 import nl.minjus.nfi.dt.jhashtools.persistence.PersistenceProvider;
 import nl.minjus.nfi.dt.jhashtools.persistence.PersistenceProviderCreator;
 import nl.minjus.nfi.dt.jhashtools.persistence.PersistenceStyle;
 
 import static java.util.logging.Logger.getLogger;
 
 /**
  *
  * @author kojak
  */
 public class DigestOutputCreator {
 
     private static Logger log = getLogger(DigestOutputCreator.class.getCanonicalName());
     private PrintWriter out;
     private DirHasher dirHasher;
     private File outputFile;
     private DirHasherResult digests;
     private PersistenceStyle persistenceStyle;
     private boolean forceOverwrite;
 
     public DigestOutputCreator(OutputStream out, DirHasher dirHasher, boolean forceOverwrite) {
         this.out = new PrintWriter(new OutputStreamWriter(out, Charset.forName("utf-8")));
         this.dirHasher = dirHasher;
         this.digests = new DirHasherResult();
         this.outputFile = null;
         this.forceOverwrite = forceOverwrite;
     }
 
     public void setPersistenceStyle(PersistenceStyle style) {
         this.persistenceStyle = style;
     }
 
     public void setOutputFile(String filename) throws FileNotFoundException {
         File file = new File(filename);
        if (!file.exists() || this.forceOverwrite) {
             this.outputFile = file;
         } else {
             throw new FileNotFoundException("File ["+filename+") not found");
         }
     }
 
     public void generate(String[] pathnames) {
         for (String pathname: pathnames) {
             dirHasher.updateDigests(digests, new File(pathname));
         }
     }
 
     public void finish() {
         DirHasherResult result = this.persistDigestsToFile();
         this.out.printf("Generated with hashtree (java) by %s\n", System.getProperty("user.name"));
         result.prettyPrint(this.out);
     }
 
     private DirHasherResult persistDigestsToFile() {
         FileOutputStream file = null;
         try {
             log.log(Level.INFO, "Writing the results to " + outputFile.getName());
             file = new FileOutputStream(outputFile);
             PersistenceProvider persistenceProvider = PersistenceProviderCreator.create(this.persistenceStyle);
             persistenceProvider.persist(file, digests);
             file.flush();
 
             DirHasher d = new DirHasher(digests.firstEntry().getValue().getAlgorithms());
             return d.getDigests(outputFile);
         } catch (PersistenceException ex) {
             log.log(Level.SEVERE, "Cannot persist content to file", ex);
         } catch (IOException ex) {
             log.log(Level.SEVERE, "Cannot create file", ex);
         } catch (NoSuchAlgorithmException ex) {
             log.log(Level.SEVERE, "Cannot create the algorithm", ex);
         } finally {
             try {
                 if (file != null) {
                     file.close();
                 }
             } catch (IOException ex) {
                 log.log(Level.SEVERE, "Cannot close file", ex);
             }
         }
         return null;
     }
 }
