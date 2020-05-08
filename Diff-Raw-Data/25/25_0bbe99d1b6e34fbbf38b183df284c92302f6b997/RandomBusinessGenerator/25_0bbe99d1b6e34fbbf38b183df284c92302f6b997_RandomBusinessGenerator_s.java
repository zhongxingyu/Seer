 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.timothyyip.randompersona;
 
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.math.RandomUtils;
 
 /**
  *
  * @author Timothy
  */
 public class RandomBusinessGenerator
 {
 
     private static final List<String> COMPANY_NAMES;
     private static final List<String> DOMAIN_SUFFIX;
     private static final List<String> JOB_TITLE_PART1;
     private static final List<String> JOB_TITLE_PART2;
     private static final List<String> JOB_TITLE_PART3;
 
     static
     {
         try
         {
            COMPANY_NAMES = new ArrayList<String>(Arrays.asList(StringUtils.split(IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/timothyyip/randompersona/resources/companyNames.txt")))));
            DOMAIN_SUFFIX = new ArrayList<String>(Arrays.asList(StringUtils.split(IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/timothyyip/randompersona/resources/domainSuffix.txt")))));
            JOB_TITLE_PART1 = new ArrayList<String>(Arrays.asList(StringUtils.split(IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/timothyyip/randompersona/resources/jobTitle1.txt")))));
            JOB_TITLE_PART2 = new ArrayList<String>(Arrays.asList(StringUtils.split(IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/timothyyip/randompersona/resources/jobTitle2.txt")))));
            JOB_TITLE_PART3 = new ArrayList<String>(Arrays.asList(StringUtils.split(IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/timothyyip/randompersona/resources/jobTitle3.txt")))));
         }
         catch (IOException ex)
         {
             ex.printStackTrace();
             throw new RuntimeException("Resources cannot be read during initialization");
         }
     }
 
     public static void main(String[] args)
     {
     }
 
     public static void createBusiness(PersonaModel personaModel)
     {
         personaModel.setCompanyName(getBusinessName());
         personaModel.setEmail(getEmail(personaModel));
         personaModel.setJobTitle(getJobTitle());
     }
 
     private static String getBusinessName()
     {
         return COMPANY_NAMES.get(RandomUtils.nextInt(COMPANY_NAMES.size()));
     }
 
     private static String getEmail(PersonaModel personaModel)
     {
         return personaModel.getFirstName().toLowerCase()
                 + emptyOrDot()
                 + personaModel.getLastName().toLowerCase()
                 + "@"
                 + personaModel.getCompanyName().toLowerCase().replace("\\W", "")
                 + "."
                 + DOMAIN_SUFFIX.get(RandomUtils.nextInt(DOMAIN_SUFFIX.size()));
     }
 
     private static String emptyOrDot()
     {
         return RandomUtils.nextBoolean() ? "." : "";
     }
 
     private static String getJobTitle()
     {
         return JOB_TITLE_PART1.get(RandomUtils.nextInt(JOB_TITLE_PART1.size()))
                 + (RandomUtils.nextBoolean() ? (" " + JOB_TITLE_PART2.get(RandomUtils.nextInt(JOB_TITLE_PART2.size()))) : "")
                 + " "
                 + JOB_TITLE_PART3.get(RandomUtils.nextInt(JOB_TITLE_PART3.size()));
     }
 }
