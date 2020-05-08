 package com.jclarity.had_one_dismissal.web;
 
 import static com.google.common.io.Files.readLines;
 import static java.nio.charset.Charset.defaultCharset;
 
 import java.io.File;
 import java.io.IOException;
 import java.net.URISyntaxException;
 import java.util.Date;
 import java.util.List;
 
 import org.slf4j.Logger;
 import org.slf4j.LoggerFactory;
 import org.springframework.beans.factory.annotation.Autowired;
 import org.springframework.stereotype.Controller;
 import org.springframework.transaction.annotation.Transactional;
 import org.springframework.web.bind.annotation.RequestMapping;
 
 import com.google.common.collect.Lists;
 import com.jclarity.had_one_dismissal.domain.Applicant;
 import com.jclarity.had_one_dismissal.domain.Company;
 import com.jclarity.had_one_dismissal.domain.JobListing;
 import com.jclarity.had_one_dismissal.domain.Location;
 import com.jclarity.had_one_dismissal.domain.Tag;
 import com.jclarity.had_one_dismissal.jmx.PerformanceProblems;
 
 @RequestMapping("/populate/**")
 @Controller
 public class Populate {
 
     private static final Logger LOGGER = LoggerFactory.getLogger(Populate.class);
 
     @Autowired private PerformanceProblems problems;
 
     @RequestMapping
     public String index() {
         try {
             if (problems.isSavingDBQueries())
                 deleteAll();
 
             List<Location> locations = loadLocations();
             List<Tag> tags = loadTags();
             loadApplicants();
             List<Company> companys = loadCompanies(locations);
             loadJobListings(companys, tags);
         } catch (Exception e) {
             LOGGER.error(e.getMessage(), e);
         }
         return "populate/index";
     }
 
     @Transactional
     public void deleteAll() {
         for (JobListing listing : JobListing.findAllJobListings()) {
             listing.remove();
         }
         for (Company company : Company.findAllCompanys()) {
             company.remove();
         }
         for (Applicant applicant : Applicant.findAllApplicants()) {
             applicant.remove();
         }
         for (Tag tag : Tag.findAllTags()) {
             tag.remove();
         }
         for (Location location : Location.findAllLocations()) {
             location.remove();
         }
     }
 
     @Transactional
     public void loadJobListings(List<Company> companys, List<Tag> tags) {
         String description = "some job description";
 
         for (int i = 0; i < tags.size(); i++) {
             JobListing job = new JobListing();
             job.setCompany(companys.get(i));
             job.setDescription(description);
             job.setPostedAt(new Date());
             int salary = i * 10000;
             job.setSalaryLowerBound(salary - 10);
             job.setSalaryUpperBound(salary + 10);
             job.setTitle(tags.get(i).getName());
             if (problems.isSavingDBQueries())
                 job.persist();
         }
     }
 
     @Transactional
     public List<Company> loadCompanies(List<Location> locations) throws URISyntaxException, IOException {
         List<String> lines = getLines("companies");
 
         List<Company> companies = Lists.newArrayList();
         int i = 0;
         for (String line : lines) {
            LOGGER.debug(line);
             Company company = new Company();
             Location location = locations.get(i % locations.size());
             company.setLocation(location);
             company.setName(line);
             if (problems.isSavingDBQueries())
                 company.persist();
             companies.add(company);
             i++;
         }
         return companies;
     }
 
     public void loadApplicants() throws URISyntaxException, IOException {
         loadApplicants(0, 10000);
     }
 
    @Transactional
     public void loadApplicants(int start, int end) throws URISyntaxException, IOException {
         List<String> lines = getLines("names").subList(start, end);
         int count = 0;
         for (String line : lines) {
            LOGGER.debug(line);
             String[] split = line.split("\t");
             if (split.length != 2)
                 continue;
 
             Applicant applicant = new Applicant();
             applicant.setForename(split[0]);
             applicant.setSurname(split[1]);
             applicant.setYearsExperience(count);
             if (problems.isSavingDBQueries())
                 applicant.persist();
             count++;
         }
     }
 
    @Transactional
     public List<Tag> loadTags() throws URISyntaxException, IOException {
         List<String> lines = getLines("jobs");
         List<Tag> tags = Lists.newArrayList();
         for (String line : lines) {
            LOGGER.debug(line);
             if (line.length() < 2)
                 continue;
 
             Tag tag = new Tag();
             tag.setName(line);
             if (problems.isSavingDBQueries())
                 tag.persist();
             tags.add(tag);
         }
         return tags;
     }
 
    @Transactional
     public List<Location> loadLocations() throws URISyntaxException, IOException {
         List<String> lines = getLines("countries");
 
         List<Location> locations = Lists.newArrayList();
         for (String name : lines) {
            LOGGER.debug(name);
             if (name.length() < 2)
                 continue;
 
             Location location = new Location();
             location.setName(name);
             if (problems.isSavingDBQueries())
                 location.persist();
             locations.add(location);
         }
         return locations;
     }
 
     private static List<String> getLines(String filename) throws URISyntaxException, IOException {
         File countryFile = new File(Populate.class.getResource(filename).toURI());
         return readLines(countryFile, defaultCharset());
     }
 
 }
