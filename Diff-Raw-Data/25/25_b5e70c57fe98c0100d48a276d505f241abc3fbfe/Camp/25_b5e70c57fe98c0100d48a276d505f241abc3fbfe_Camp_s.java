 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package campassignment;
 
 import java.io.BufferedReader;
 import java.io.FileReader;
 import java.util.ArrayList;
 import java.util.Calendar;
 import java.util.Collection;
 import java.util.GregorianCalendar;
 import java.util.StringTokenizer;
 
 /**
  *
  * @author tocas
  */
 public class Camp {
 
     private ArrayList<Section> sections = new ArrayList<Section>();
     private ArrayList<Camper> leaders = new ArrayList<Camper>();
     private Section root = new Section();
     private int countOfSections = 0;
     private int countOfOverSections = 0;
 
     public Camp(int countOfSections) {
         this.countOfSections = countOfSections;
         this.leaders = new ArrayList<Camper>(countOfSections);
     }
 
     private int computeAge(String stringDate) {
         StringTokenizer st =  new StringTokenizer(stringDate, ".");
 
         int day = Integer.valueOf(st.nextToken());
         int month =  Integer.valueOf(st.nextToken());
         int year = Integer.valueOf(st.nextToken());
         
         Calendar birthCal = new GregorianCalendar(year,month,day);
         Calendar nowCal = new GregorianCalendar();
         int age = nowCal.get(Calendar.YEAR) - birthCal.get(Calendar.YEAR);
 
         boolean isMonthGreater = birthCal.get(Calendar.MONTH) >= nowCal.get(Calendar.MONTH);
         boolean isMonthSameButDayGreater = birthCal.get(Calendar.MONTH) == nowCal
         .get(Calendar.MONTH)&& birthCal.get(Calendar.DAY_OF_MONTH) > nowCal.get(Calendar.DAY_OF_MONTH);
         if (isMonthGreater || isMonthSameButDayGreater) {
             age = age - 1;
         }
         return age;
 
 
     }
 
     public void importData(String fileName) {
         ArrayList<Camper> members = new ArrayList<Camper>();
         /*
         members.add(new Camper(1,"Franta",7));
         members.add(new Camper(2,"Karel",9));
         members.add(new Camper(3,"Dáša",8));
         members.add(new Camper(4,"Ivana",9));
         members.add(new Camper(5,"Jindra",8));
         members.add(new Camper(6,"Jitka",6));
         members.add(new Camper(7,"Pepa",7));
         members.add(new Camper(8,"Tomas",7));
 
          */
 
         int ID = 0;
         String name = "";
         String stringDate = "";
         int gendar = 0;
 
         try {
 
             //create BufferedReader to read csv file
             BufferedReader br = new BufferedReader(new FileReader(fileName));
             String strLine = "";
             StringTokenizer st = null;
             int lineNumber = 0, tokenNumber = 0;
 
             //read comma separated file line by line
             while ((strLine = br.readLine()) != null) {
                 lineNumber++;
                 if(lineNumber == 1) continue;
                 //break comma separated line using ","
                 st = new StringTokenizer(strLine, ";");
 
                 while (st.hasMoreTokens()) {
                     //display csv values
                     tokenNumber++;
                     switch (tokenNumber) {
                         case 1:
                             ID = Integer.valueOf(st.nextToken());
                         case 2:
                             name = st.nextToken();
                         case 3:
                             name = name + " " + st.nextToken() + ".";
                         case 4:
                             gendar = Integer.valueOf(st.nextToken());
                         case 5:
                             stringDate = st.nextToken();
                     }
                    members.add(new Camper(ID, name, computeAge(stringDate), gendar));
                 }
 
                 //reset token number
                 tokenNumber = 0;
 
             }
 
 
         } catch (Exception e) {
             System.out.println("Exception while reading csv file: " + e);
         }
 
 
 
 
         root = new Section("Full members", members);
     }
 
     public void setupLeaders(){
         for (Camper camper : root.assignCampers(countOfSections)) {
             leaders.add(camper);
         }
     }
 
     public void assignmentCampers() {
         root.sortSection();
         if (countOfSections == 0) {
             return;
         }
         int membersInSection = root.largeOfSection() / countOfSections;
 
         countOfOverSections = root.largeOfSection() % countOfSections;
 
         int normalSection = countOfSections - countOfOverSections;
 
         int sectionNumber = 0;
         for (int i = 0; i < normalSection; i++) {
             Section tmp = new Section(root.assignCampers(membersInSection));
             if(sectionNumber < leaders.size()) tmp.setLeader(leaders.get(sectionNumber++));
             sections.add(tmp);
         }
 
         for (int i = membersInSection; i < membersInSection + countOfOverSections; i++) {
             Section tmp = new Section(root.assignCampers(membersInSection + 1));
             if(sectionNumber < leaders.size()) tmp.setLeader(leaders.get(sectionNumber++));
             sections.add(tmp);
         }
     }
 
     public int getSumMaxDifference() {
         int maxDifference = 0;
         for (Section section : sections) {
             for (Section section1 : sections) {
                 if(!section.equals(section1)){
                    maxDifference =+ Section.sumDifference(section, section1);
                 }
             }
         }
         return maxDifference;
     }
 
     private boolean sameLenght() {
         if (sections.isEmpty()) {
             return true;
         }
         int size = sections.get(0).largeOfSection();
         for (int i = 1; i < sections.size(); i++) {
             if (size != sections.get(i).largeOfSection()) {
                 return false;
             }
         }
         return true;
     }
 
     @Override
     public String toString() {
         String returnString = "";
         for (Section section : sections) {
             returnString = returnString + "\n" + section.toString();
         }
         returnString = returnString + "\n Sum max difference: " + getSumMaxDifference();
         return returnString;
     }
 
     public Camp hardCopy() {
         Camp tmp = new Camp(this.countOfSections);
         tmp.countOfOverSections = this.countOfOverSections;
         for (Section section : sections) {
             tmp.sections.add(section.hardCopy());
         }
         return tmp;
     }
 
     public Camp switchCampers(int from, int to) {
         if (sections.get(from).largeOfSection() < sections.get(to).largeOfSection()) {
             return this;
         }
         sections.get(to).addMember(sections.get(from).getCamperForSwitch());
         return this;
     }
 
     public Camp shiftOverSectionts(int largeShift) {
         if (countOfOverSections == 0) {
             return this;
         }
         int stopIndex = countOfSections - countOfOverSections;
         for (int i = 0; i < largeShift; i++) {
             for (int j = countOfSections - 1 - i; j < stopIndex - i; j--) {
                 this.switchCampers(j, j - 1);
             }
         }
         return this;
     }
 
     //Precondition optimization was never perform
     public static Camp optimization(Camp camp) {
         if (camp.sameLenght()) {
             return camp;
         }
         ArrayList<Camp> allPosibility = new ArrayList<Camp>();
         allPosibility.add(camp);
         Camp tmp = camp.hardCopy();
         int startIndex = camp.countOfSections - camp.countOfOverSections;
 
         for (int i = startIndex; i < camp.countOfSections; i++) {
             tmp = camp.hardCopy();
             tmp = tmp.shiftOverSectionts(i - startIndex);
             for (int j = i; j > 0; j--) {
                 allPosibility.add(tmp.switchCampers(j, j - 1));
             }
         }
         int cmax = Integer.MAX_VALUE;
         Camp solution = new Camp(camp.countOfOverSections);
         for (Camp camp1 : allPosibility) {
             if (cmax > camp1.getSumMaxDifference()) {
                 cmax = camp1.getSumMaxDifference();
                 solution = camp1;
             }
         }
         return solution;
     }
 }
