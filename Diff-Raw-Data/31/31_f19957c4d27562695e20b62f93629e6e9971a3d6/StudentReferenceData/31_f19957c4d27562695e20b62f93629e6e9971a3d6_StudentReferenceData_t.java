 package org.sukrupa.student;
 
 import org.hibernate.SessionFactory;
 
 import java.text.Collator;
 import java.util.ArrayList;
 import java.util.Collections;
 import java.util.List;
 
 import static java.util.Arrays.asList;
 import static java.util.Collections.emptyList;
 
 public class StudentReferenceData {
 
    private static final List<String> STUDENT_CLASSES = asList("Day Care", "Nursery", "LKG", "UKG", "1 Std", "2 Std", "3 Std", "4 Std", "5 Std", "6 Std", "7 Std", "8 Std", "9 Std", "10 Std");
     private static final String EMPTY_OPTION = "";
     private static final List<String> GENDERS = asList(EMPTY_OPTION, "Male", "Female");
     private static final List<String> CASTES = asList(EMPTY_OPTION,"AC", "AD", "AK", "Achari", "Agnikula", "Agnikula Kshatriya",
             "Arya Vaishya", "Balija", "Balijanru", "Baljigru", "Bendha goes", "Bhovi", "Bhramin", "Bohvi", "Brahmin", "Chettiyar",
             "Chettyar", "Christian", "Ganiga", "Ganiga Shetty", "Gopwda", "Gowda", "Gowdas", "Gowdwer", "Gownder", "Islamic",
             "Kodiyan", "Kounder", "Kumbara Shetty", "Kuruba's", "MBC", "Modahaliyar", "Muslim", "Nadar", "Naidu", "Nayak",
             "Okkaligaru", "Oknikula", "Others", "Rajput", "Rathore", "Reddy", "Reddy's", "Roman Catholic", "SC", "ST", "Salaiketa",
             "Shalai Keta", "Shetty", "Syed", "Thigalru", "Tigalas", "Vannikula", "Vanniyar", "Vishwa Karma", "Vokkaliga");
     private static final List<String> SUBCASTES = asList(EMPTY_OPTION, "AD", "AK", "Achari", "Adi Drawida", "Adi Janaga",
             "Adi Karnataka", "BC", "Bale -Balijigru", "Bale Balijanru", "Bale Banjaguru", "Banjarthi", "Bhajanthri", "Devanger",
             "Ganiga Shetty", "II 'A'", "Kammala Achari", "Kounder", "Kshathriya", "Kumbar Shetty", "Kumbara", "Lambani", "Nayak",
             "Shaik", "Sharif", "Shiva Jyothipana", "Singh", "Tiwari", "Vailu", "Vailu Shetty", "Vakkaliga", "Val Nayak",
             "Vaniga Gownder", "Vannikula", "Vokkaliga");
     private static final List<String> COMMUNITY_LOCATIONS = asList(EMPTY_OPTION, "Bhuvaneshwari Slum", "Chamundi Nagar",
             "Cholanaykanahalli","Ganganagar", "Gudadhalli", "Hebbal","Kanka Nagar", "Kunthigrama", "Nagenahalli", "Rahamath  Nagar",
             "Residentail", "Subramanya Nagar");
     private static final List<String> OCCUPATIONS = asList("Assistant Captain", "Auto Driver", "Bakery Worker", "Bar Bainding",
             "Bar Shop", "Cable Work", "Candle Work", "Car Driver", "Carpenter", "Centring", "Centring Contract",
             "Construction Worker", "Contract Labourer", "Coolie", "Courier", "Daily Wages Labour", "Domestic Worker",
             "Driver", "Driving School", "Electrician", "Embroidery Worker", "Flower Seller", "Former", "Gardener",
             "Groundnut Sailer", "Hawker", "Head Canstable", "Hotel Server", "House Keeper", "House wife", "Iron Shop",
             "Kouli", "Labour", "Lorry Driver", "Mason Work", "Meason", "No job", "Office Assistant", "Office Boy",
             "Office Man", "Painter", "Paper Mart Business", "Sales Executive", "Sales Man", "Sarees Seller",
             "Security Guard", "Service", "Stone Designer", "Store work", "TV Operator", "Tailes Work", "Tailor",
             "Tea/Coffee Business", "Tiles Worker", "Truck Driver", "Vender", "Ward Boy", "Watch Man", "Welder",
             "Work shop", "Worker", "Working in Sukrupa");
 
     private static final List<String> RELIGIONS = asList(EMPTY_OPTION, "Bhovi", "Christian", "Gowda", "Gownder", "Hindu", "Muslim","Sikh","Tamil");
     private static final List<String> TALENTS_DESCRIPTIONS = asList("Acting", "Art", "Crafts", "Creative Writing", "Dancing", "Mimicry",
             "Musical Instrument", "Pick & Speak", "Public Speaking", "Reading", "Singing", "Sports", "Story Telling");
     private static final int AGES_TO = 20;
     private static final int AGES_FROM = 2;
     private static final List<String> SPONSORED = asList("Yes","No");
     private static final List<String> MARITAL_STATUSES = asList(" ", "Married", "Single", "Divorced", "Widowed", "Deceased");
     private TalentRepository talentRepository;
 
 
     public StudentReferenceData() {
 
     }
 
     public StudentReferenceData(TalentRepository talentRepository) {
         this.talentRepository = talentRepository;
     }
 
     public List<String> getStudentClasses() {
         return STUDENT_CLASSES;
     }
 
     public List<String> getGenders() {
         return GENDERS;
     }
 
     public List<String> getCastes() {
         return CASTES;
     }
 
     public List<String> getSubcastes() {
         return SUBCASTES;
     }
 
     public List<String> getCommunityLocations() {
         return COMMUNITY_LOCATIONS;
     }
 
     public List<String> getReligions() {
         return RELIGIONS;
     }
 
 
     public List<String> getTalentDescriptions()
     {
         List<Talent> talents = getTalentsFromRepository();
         if(talents.isEmpty())
         {
             talents = Collections.emptyList();
         }
 
 
         return (talentRepository.returnTalentDescriptionsInList(talents));
 
     }
 
 
     public List<String> getAges() {
         List<String> ages = new ArrayList<String>();
         for (int age = AGES_FROM; age <= AGES_TO; age++) {
             ages.add(age + EMPTY_OPTION);
         }
         return ages;
     }
 
     public int getAgesTo() {
         return AGES_TO;
     }
 
     public int getAgesFrom() {
         return AGES_FROM;
     }
 
     public List<String> getStatuses() {
         return StudentStatus.getNamesList();
     }
 
     public List<String> getSponsored() {
         return SPONSORED;
     }
 
     public List<String> getOccupations() {
         return OCCUPATIONS;
     }
 
     public List<String> getMaritalStatuses() {
         return MARITAL_STATUSES;
     }
 
     public List<String> getFamilyStatuses() {
         List<String> statusList = StudentFamilyStatus.getNameList();
         statusList.add(0, EMPTY_OPTION);
         return statusList;
     }
 
     public List<Talent> getTalentsFromRepository() {
         return (talentRepository == null)
                 ? Collections.<Talent>emptyList()
                 : talentRepository.findAllTalents();
     }
 
 }
