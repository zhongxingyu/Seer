 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package org.timothyyip.randompersona;
 
 import java.io.File;
 import java.io.IOException;
 import java.util.ArrayList;
 import java.util.Arrays;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import org.apache.commons.io.FileUtils;
 import org.apache.commons.io.IOUtils;
 import org.apache.commons.lang.ArrayUtils;
 import org.apache.commons.lang.StringUtils;
 import org.apache.commons.lang.math.RandomUtils;
 
 /**
  *
  * @author Timothy
  */
 public class RandomNameGenerator
 {
     
     private static final List<String> MALE_FIRST_NAMES;
     private static final List<String> MALE_TITLES;
     private static final List<String> FEMALE_FIRST_NAMES;
     private static final List<String> FEMALE_TITLES;
     private static final List<String> LAST_NAMES;
     
     static
     {
         try
         {
             
             
            MALE_FIRST_NAMES = new ArrayList<String>(Arrays.asList(StringUtils.split(IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/timothyyip/randompersona/resources/maleNames.txt")),",")));
            MALE_TITLES = new ArrayList<String>(Arrays.asList(StringUtils.split(IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/timothyyip/randompersona/resources/maleTitles.txt")),",")));
            FEMALE_FIRST_NAMES = new ArrayList<String>(Arrays.asList(StringUtils.split(IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/timothyyip/randompersona/resources/femaleNames.txt")),",")));
            FEMALE_TITLES = new ArrayList<String>(Arrays.asList(StringUtils.split(IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/timothyyip/randompersona/resources/femaleTitles.txt")),",")));
            LAST_NAMES = new ArrayList<String>(Arrays.asList(StringUtils.split(IOUtils.toString(Thread.currentThread().getContextClassLoader().getResourceAsStream("org/timothyyip/randompersona/resources/lastNames.txt")),",")));
         }
         catch (IOException ex)
         {
             ex.printStackTrace();
             throw new RuntimeException("Resources cannot be read during initialization");
         }
     }
 
     public static void main(String[] args) throws IOException
     {
         
       
       
     }
     public static void createPersonaName(PersonaModel personaModel)
     {
         personaModel.setMale(RandomUtils.nextBoolean());
         personaModel.setTitle(getTitle(personaModel.isMale()));
         personaModel.setFirstName(getFirstName(personaModel.isMale()));
         personaModel.setLastName(getLastName());
         personaModel.setFullName(personaModel.getFirstName() + " " + personaModel.getLastName());
         
     }
     
     private static String getTitle(boolean isMale)
     {
         return isMale ? MALE_TITLES.get(RandomUtils.nextInt(MALE_TITLES.size())) : FEMALE_TITLES.get(RandomUtils.nextInt(FEMALE_TITLES.size()));
     }
     
     private static String getFirstName(boolean isMale)
     {
         return isMale ? MALE_FIRST_NAMES.get(RandomUtils.nextInt(MALE_FIRST_NAMES.size())) : FEMALE_FIRST_NAMES.get(RandomUtils.nextInt(FEMALE_FIRST_NAMES.size()));
     }
     
     private static String getLastName()
     {
         return LAST_NAMES.get(RandomUtils.nextInt(LAST_NAMES.size()));
     }
 }
