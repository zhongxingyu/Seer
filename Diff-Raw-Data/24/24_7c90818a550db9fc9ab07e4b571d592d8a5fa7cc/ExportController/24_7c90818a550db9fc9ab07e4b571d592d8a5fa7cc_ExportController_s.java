 package com.github.kolorobot.icm.dashboard;
 
 import com.github.kolorobot.icm.account.Account;
 import com.github.kolorobot.icm.account.AccountRepository;
 import com.github.kolorobot.icm.incident.Incident;
 import com.github.kolorobot.icm.incident.IncidentRepository;
 import org.springframework.security.access.prepost.PreAuthorize;
 import org.springframework.stereotype.Controller;
 import org.springframework.web.bind.annotation.RequestMapping;
 import org.springframework.web.bind.annotation.RequestParam;
 
 import javax.inject.Inject;
 import javax.servlet.http.HttpServletResponse;
 import java.io.BufferedWriter;
 import java.io.IOException;
 import java.util.List;
 
 @Controller
 @RequestMapping("export")
 class ExportController {
 
     enum OutputFormat {csv, xml}
 
     @Inject
     private IncidentRepository incidentRepository;
 
     @Inject
     private AccountRepository accountRepository;
 
     @RequestMapping("/incidents")
     @PreAuthorize("hasRole('ROLE_ADMIN')")
     public void exportIncidentsAsCsv(@RequestParam(required = false, defaultValue = "csv") OutputFormat format, HttpServletResponse response) throws IOException {
         List<Incident> incidents = incidentRepository.findAll();
         BufferedWriter writer = new BufferedWriter(response.getWriter());
 
         String file = "";
         if (format.equals(OutputFormat.csv)) {
             writeIncidentsAsCsv(incidents, writer);
             file = "export.csv";
         }
         if (format.equals(OutputFormat.xml)) {
             writeIncidentsAsXml(incidents, writer);
             file = "export.xml";
         }
         setHeader(response, file);
         writer.flush();
     }
 
     private void writeIncidentsAsXml(List<Incident> incidents, BufferedWriter writer) throws IOException {
         StringBuilder out = new StringBuilder("<?xml version=\"1.0\"?>\n");
         out.append("<incidents>\n");
         for (Incident i : incidents) {
            out.append(String.format("\t<incident id=\"%d\" created=\"%s\" status=\"%s\">\n", i.getId(), i.getCreated(), i.getStatus()));
             out.append(String.format("\t\t<type>%s</type>\n", i.getDescription()));
             out.append(String.format("\t\t<description>%s</description>\n", i.getIncidentType()));
             out.append(String.format("\t\t<address id=\"%d\">\n", i.getAddress().getId()));
             out.append(String.format("\t\t\t<cityLine>%s</cityLine>\n", i.getAddress().getAddressLine()));
             out.append(String.format("\t\t\t<addressLine>%s</addressLine>\n", i.getAddress().getCityLine()));
             out.append("\t\t</address>\n");
             out.append("\t</incident>\n");
         }
         out.append("</incidents>");
         writer.write(out.toString());
     }
 
     private void writeIncidentsAsCsv(List<Incident> incidents, BufferedWriter writer) throws IOException {
         for (Incident i : incidents) {
             writer.write(String.format("%d;%s;%s;%s",
                    i.getId(), i.getStatus(), i.getCreated(), i.getIncidentType())
             );
             writer.newLine();
         }
     }
 
     @RequestMapping("/accounts")
     // @PreAuthorize("hasRole('ROLE_ADMIN')")
     public void exportAccountsAsCsv(@RequestParam(required = false, defaultValue = "csv") OutputFormat format, HttpServletResponse response) throws IOException {
         List<Account> accounts = accountRepository.findAll();
         BufferedWriter writer = new BufferedWriter(response.getWriter());
         String file = "";
         if (format.equals(OutputFormat.csv)) {
             writeAccountsAsCsv(accounts, writer);
             file = "export.csv";
         }
         if (format.equals(OutputFormat.xml)) {
             writeAccountsAsXml(accounts, writer);
             file = "export.xml";
         }
         setHeader(response, file);
         writer.flush();
     }
 
     private void writeAccountsAsCsv(List<Account> accounts, BufferedWriter writer) throws IOException {
         for (Account a : accounts) {
             writer.write(String.format("%d;%s;%s;%s;%s",
                     a.getId(), a.getName(), a.getEmail(), a.getPhone(), a.getRole())
             );
             writer.newLine();
         }
     }
 
     private void writeAccountsAsXml(List<Account> accounts, BufferedWriter writer) throws IOException {
         StringBuilder out = new StringBuilder("<?xml version=\"1.0\"?>\n");
         out.append("<accounts>\n");
         for (Account a : accounts) {
             out.append(String.format("\t<account id=\"%d\" email=\"%s\" role=\"%s\">\n", a.getId(), a.getName(), a.getRole()));
             out.append(String.format("\t\t<name>%s</name>\n", a.getName()));
             out.append(String.format("\t\t<phone>%s</phone>\n", a.getPhone()));
             out.append(String.format("\t\t<password>%s</password>\n", a.getPassword()));
             out.append("\t</account>\n");
         }
         out.append("</accounts>\n");
         writer.write(out.toString());
     }
 
     private void setHeader(HttpServletResponse response, String file) {
         response.setHeader("Content-Disposition", "attachment; filename=\"" + file + "\"");
     }
 }
