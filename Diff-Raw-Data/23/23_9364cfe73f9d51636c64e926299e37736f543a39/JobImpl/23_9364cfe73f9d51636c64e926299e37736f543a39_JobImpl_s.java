 package info.mikaelsvensson.ftpbackup.model.impl;
 
 import info.mikaelsvensson.ftpbackup.model.Destination;
 import info.mikaelsvensson.ftpbackup.model.Job;
 import org.apache.commons.lang3.builder.EqualsBuilder;
 import org.apache.commons.lang3.builder.HashCodeBuilder;
 import org.apache.log4j.Logger;
 
 import javax.xml.bind.JAXBContext;
 import javax.xml.bind.JAXBException;
 import javax.xml.bind.Marshaller;
 import javax.xml.bind.Unmarshaller;
 import javax.xml.bind.annotation.XmlAttribute;
 import javax.xml.bind.annotation.XmlElement;
 import javax.xml.bind.annotation.XmlRootElement;
 import java.io.File;
 import java.io.FileWriter;
 import java.io.IOException;
 import java.io.Writer;
 import java.util.Collection;
 import java.util.Collections;
 import java.util.LinkedList;
 import java.util.UUID;
 
 @XmlRootElement(name = "job")
 public class JobImpl implements Job {
 // ------------------------------ FIELDS ------------------------------
 
     private static final Logger LOGGER = Logger.getLogger(JobImpl.class);
 
     private FileSetImpl fileSet = new FileSetImpl();
 
     private JobDestinations xmlDestinations = new JobDestinations();
     private String name;
 
     private ReportConfigurations xmlReportConfigurations = new ReportConfigurations();
 
     private UUID id;
 
 // -------------------------- STATIC METHODS --------------------------
 
     public static void toFile(JobImpl job, File file) {
         Writer writer = null;
         try {
             writer = new FileWriter(file);
             JAXBContext ctx = createJAXBContext();
             Marshaller marshaller = ctx.createMarshaller();
             marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, Boolean.TRUE);
             marshaller.marshal(job, file);
         } catch (JAXBException e) {
             LOGGER.warn("JAXB exception when reading job configuration XML file.", e);
         } catch (IOException e) {
             LOGGER.warn("I/O exception when reading job configuration XML file.", e);
         } finally {
             try {
                 if (writer != null) {
                     writer.close();
                 }
             } catch (IOException e) {
                 LOGGER.warn("Could not close job configuration XML file.", e);
             }
         }
     }
 
     private static JAXBContext createJAXBContext() throws JAXBException {
         return JAXBContext.newInstance(JobImpl.class);
     }
 
     public static JobImpl fromFile(File file) {
         try {
             JAXBContext ctx = createJAXBContext();
             Unmarshaller unmarshaller = ctx.createUnmarshaller();
             JobImpl job = (JobImpl) unmarshaller.unmarshal(file);
             if (job.getId() == null) {
                 job.setId(UUID.randomUUID());
                 toFile(job, file);
             }
             return job;
         } catch (JAXBException e) {
             e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
             return null;
         }
     }
 
 // --------------------------- CONSTRUCTORS ---------------------------
 
     public JobImpl() {
//        id = UUID.randomUUID();
     }
 
     public JobImpl(final String name, final FileSetImpl fileSet, Collection<? extends ReportConfigurationImpl> reportConfigurations, Destination... destinations) {
         this();
         this.id = UUID.fromString("d9bc6b75-d266-44bf-ad78-d021c7ab451f");
         this.name = name;
         this.fileSet = fileSet;
         for (Destination destination : destinations) {
             if (destination instanceof FtpDestinationImpl) {
                 this.xmlDestinations.ftpDestinations.add((FtpDestinationImpl) destination);
             } else if (destination instanceof LocalFolderDestinationImpl) {
                 this.xmlDestinations.localFolderDestinations.add((LocalFolderDestinationImpl) destination);
             } else {
                 throw new IllegalArgumentException("Unsupported type of destination");
             }
         }
         for (ReportConfigurationImpl reportConfiguration : reportConfigurations) {
             if (reportConfiguration instanceof ConsoleReportConfigurationImpl) {
                 this.xmlReportConfigurations.consoleReportConfigurations.add((ConsoleReportConfigurationImpl) reportConfiguration);
             } else if (reportConfiguration instanceof EmailReportConfigurationImpl) {
                 this.xmlReportConfigurations.emailReportConfigurations.add((EmailReportConfigurationImpl) reportConfiguration);
             } else if (reportConfiguration instanceof LocalFileSetReportConfigurationImpl) {
                 this.xmlReportConfigurations.localFileSetReportConfigurations.add((LocalFileSetReportConfigurationImpl) reportConfiguration);
             } else if (reportConfiguration instanceof RemoteActionsLogFileReportConfigurationImpl) {
                 this.xmlReportConfigurations.remoteActionsLogFileReportConfigurations.add((RemoteActionsLogFileReportConfigurationImpl) reportConfiguration);
             } else {
                 throw new IllegalArgumentException("Unsupported type of report");
             }
         }
     }
 
 // --------------------- GETTER / SETTER METHODS ---------------------
 
     @Override
     public FileSetImpl getFileSet() {
         return fileSet;
     }
 
     public void setFileSet(FileSetImpl fileSet) {
         this.fileSet = fileSet;
     }
 
     @Override
     @XmlAttribute
     public UUID getId() {
         return id;
     }
 
     public void setId(UUID id) {
         this.id = id;
     }
 
     @Override
     @XmlAttribute
     public String getName() {
         return name;
     }
 
     public void setName(final String name) {
         this.name = name;
     }
 
     @XmlElement(name = "destinations")
     public JobDestinations getXmlDestinations() {
         return xmlDestinations;
     }
 
     public void setXmlDestinations(final JobDestinations xmlDestinations) {
         this.xmlDestinations = xmlDestinations;
     }
 
     @XmlElement(name = "reportConfigurations")
     public ReportConfigurations getXmlReportConfigurations() {
         return xmlReportConfigurations;
     }
 
     public void setXmlReportConfigurations(ReportConfigurations xmlReportConfigurations) {
         this.xmlReportConfigurations = xmlReportConfigurations;
     }
 
 // ------------------------ CANONICAL METHODS ------------------------
 
     @Override
     public boolean equals(final Object o) {
         if (this == o) {
             return true;
         }
         if (!(o instanceof JobImpl)) {
             return false;
         }
 
         JobImpl that = (JobImpl) o;
 
         EqualsBuilder equalsBuilder = new EqualsBuilder();
         equalsBuilder.append(fileSet, that.fileSet);
         equalsBuilder.append(name, that.name);
         equalsBuilder.append(xmlDestinations.ftpDestinations, that.xmlDestinations.ftpDestinations);
         equalsBuilder.append(xmlDestinations.localFolderDestinations, that.xmlDestinations.localFolderDestinations);
         equalsBuilder.append(xmlReportConfigurations.consoleReportConfigurations, that.xmlReportConfigurations.consoleReportConfigurations);
         equalsBuilder.append(xmlReportConfigurations.emailReportConfigurations, that.xmlReportConfigurations.emailReportConfigurations);
         equalsBuilder.append(xmlReportConfigurations.localFileSetReportConfigurations, that.xmlReportConfigurations.localFileSetReportConfigurations);
         equalsBuilder.append(xmlReportConfigurations.remoteActionsLogFileReportConfigurations, that.xmlReportConfigurations.remoteActionsLogFileReportConfigurations);
 
         return equalsBuilder.isEquals();
     }
 
     @Override
     public int hashCode() {
         HashCodeBuilder builder = new HashCodeBuilder();
         builder.append(fileSet);
         builder.append(name);
         builder.append(xmlDestinations.ftpDestinations);
         builder.append(xmlDestinations.localFolderDestinations);
         builder.append(xmlReportConfigurations.consoleReportConfigurations);
         builder.append(xmlReportConfigurations.emailReportConfigurations);
         builder.append(xmlReportConfigurations.localFileSetReportConfigurations);
         builder.append(xmlReportConfigurations.remoteActionsLogFileReportConfigurations);
         return builder.toHashCode();
     }
 
 // ------------------------ INTERFACE METHODS ------------------------
 
 
 // --------------------- Interface Job ---------------------
 
     @Override
     public Collection<Destination> getDestinations() {
         LinkedList<Destination> list = new LinkedList<>();
         list.addAll(xmlDestinations.ftpDestinations);
         list.addAll(xmlDestinations.localFolderDestinations);
         return Collections.unmodifiableCollection(list);
     }
 
     @Override
     public Collection<ReportConfigurationImpl> getReportConfigurations() {
         LinkedList<ReportConfigurationImpl> list = new LinkedList<>();
         list.addAll(xmlReportConfigurations.consoleReportConfigurations);
         list.addAll(xmlReportConfigurations.emailReportConfigurations);
         list.addAll(xmlReportConfigurations.localFileSetReportConfigurations);
         list.addAll(xmlReportConfigurations.remoteActionsLogFileReportConfigurations);
         return Collections.unmodifiableCollection(list);
     }
 
 // -------------------------- OTHER METHODS --------------------------
 
     public Collection<ConsoleReportConfigurationImpl> getConsoleReportConfigurations() {
         return xmlReportConfigurations.consoleReportConfigurations;
     }
 
     public Collection<EmailReportConfigurationImpl> getEmailReportConfigurations() {
         return xmlReportConfigurations.emailReportConfigurations;
     }
 
     public Collection<FtpDestinationImpl> getFtpDestinations() {
         return xmlDestinations.ftpDestinations;
     }
 
     public Collection<LocalFileSetReportConfigurationImpl> getLocalFileSetReportConfigurations() {
         return xmlReportConfigurations.localFileSetReportConfigurations;
     }
 
     public Collection<LocalFolderDestinationImpl> getLocalFolderDestinations() {
         return xmlDestinations.localFolderDestinations;
     }
 
     public Collection<RemoteActionsLogFileReportConfigurationImpl> getRemoteActionsLogFileReportConfigurations() {
         return xmlReportConfigurations.remoteActionsLogFileReportConfigurations;
     }
 
 // -------------------------- INNER CLASSES --------------------------
 
     static class JobDestinations {
         @XmlElement(name = "ftpDestination")
         final
         Collection<FtpDestinationImpl> ftpDestinations = new LinkedList<>();
 
         @XmlElement(name = "localFolderDestination")
         final
         Collection<LocalFolderDestinationImpl> localFolderDestinations = new LinkedList<>();
     }
 
     static class ReportConfigurations {
         @XmlElement(name = "consoleReport")
         final
         Collection<ConsoleReportConfigurationImpl> consoleReportConfigurations = new LinkedList<>();
         @XmlElement(name = "emailReport")
         final
         Collection<EmailReportConfigurationImpl> emailReportConfigurations = new LinkedList<>();
         @XmlElement(name = "localFileSetReport")
         final
         Collection<LocalFileSetReportConfigurationImpl> localFileSetReportConfigurations = new LinkedList<>();
         @XmlElement(name = "remoteActionsLogFileReport")
         final
         Collection<RemoteActionsLogFileReportConfigurationImpl> remoteActionsLogFileReportConfigurations = new LinkedList<>();
     }
 }
