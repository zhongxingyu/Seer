 package org.fofo.services.management;
 
 import java.util.ArrayList;
 import java.util.Date;
 import java.util.List;
 import java.util.Properties;
 import javax.mail.Message;
 import javax.mail.MessagingException;
 import javax.mail.Session;
 import javax.mail.Transport;
 import javax.mail.internet.InternetAddress;
 import javax.mail.internet.MimeMessage;
 import org.fofo.dao.ClubDAO;
 import org.fofo.dao.CompetitionDAO;
 import org.fofo.entity.Category;
 import org.fofo.entity.Club;
 import org.fofo.entity.Competition;
 import org.fofo.entity.Team;
 import org.fofo.entity.CompetitionType;
 import org.joda.time.DateTime;
 import org.fofo.dao.PersistException;
 import org.fofo.dao.TeamDAO;
 
 /**
  *
  * @author josepma
  */
 public class ManagementService {
 
     private ClubDAO clubDao;
     private CompetitionDAO cDao;
     private Competition competition;
     private TeamDAO teamDao;
 
     /**
      *
      * @return
      */
     public CompetitionDAO getcDao() {
         return cDao;
     }
 
     /**
      *
      * @param cDao
      */
     public void setcDao(CompetitionDAO cDao) {
         this.cDao = cDao;
     }
 
     /**
      *
      * @param clubDao
      */
     public void setClubDao(ClubDAO clubDao) {
         this.clubDao = clubDao;
     }
 
     /**
      *
      * @param teamDao
      */
     public void setTeamDao(TeamDAO teamDao) {
         this.teamDao = teamDao;
     }
 
     /**
      *
      * @param competition
      */
     public void setCompetition(Competition competition) {
         this.competition = competition;
     }
 
     /**
      *
      * @return
      */
     public ClubDAO getClubDao() {
         return clubDao;
     }
 
     /**
      *
      * @return
      */
     public Competition getCompetition() {
         return competition;
     }
 
     /**
      *
      * @param comp
      * @throws Exception
      */
     public void addCompetition(Competition comp) throws Exception {
         List<Club> clubs = new ArrayList<Club>();
         checkForExceptions(comp);
         clubs = clubDao.getClubs();
 //        if(clubs!=null){
 //            for (Club c : clubs) {
 //                sendEmail(c);
 //            }
 //        }
         cDao.addCompetition(comp);
         //THIS IS THE OPERATION TO IMPLEMENT.....
 
     }
 
     /**
      * Add a team in a competition.
      * @param competetion The competition we want to register a team.
      * @param team The team we want to register.
      * @throws InscriptionTeamException 
      */
     public void addTeam(Competition competition, Team team) throws Exception {
                   //parameter: it was competetion!!!!
         Team teamDB = teamDao.findTeamByName(team.getName());
         
         if (checkforExceptions(competition, teamDB)) {
 
           //  competition.getTeams().add(team);
                   //It adds the team to the competition parameter, not to the
                   //competiion in the DB.
             cDao.addTeam(competition, team);
         } else {
             throw new InscriptionTeamException();
         }
     }
 
     /*
      * 
      * PRIVATE OPERATIONS
      * 
      * 
      * 
      */
     private void sendEmail(Club c) {
         String servidorSMTP = "smtp.gmail.com";
         String port = "587";
         String user = "fofo@gmail.com";
         String password = "pass";
 
         String destination = c.getEmail();
         String issue = "Issue!";
         String mensaje = "This is a test message.";
 
         Properties props = new Properties();
 
         props.put("mail.debug", "true");
         props.put("mail.smtp.auth", true);
         props.put("mail.smtp.starttls.enable", true);
         props.put("mail.smtp.host", servidorSMTP);
         props.put("mail.smtp.port", port);
 
         Session session = Session.getInstance(props, null);
 
         try {
             MimeMessage message = new MimeMessage(session);
             message.addRecipient(Message.RecipientType.TO, new InternetAddress(
                     destination));
             message.setSubject(issue);
             message.setSentDate(new Date());
             message.setText(mensaje);
 
             Transport tr = session.getTransport("smtp");
             tr.connect(servidorSMTP, user, password);
             message.saveChanges();
             tr.sendMessage(message, message.getAllRecipients());
             tr.close();
 
         } catch (MessagingException e) {
             e.printStackTrace();
         }
     }
 
     private boolean isPowerOfTwo(int value) {
         return (value != 0) && ((value & (value - 1)) == 0);
     }
 
     private boolean isValidCategory(Category cat) throws IncorrectCompetitionData {
         if (cat == null) {
             throw new IncorrectCompetitionData();
         }
         if (cat.equals(Category.FEMALE) || cat.equals(Category.MALE)
                 || cat.equals(Category.VETERAN)) {
             return true;
         } else {
             return false;
         }
     }
 
     private boolean validMinTeamsInLeague(int teams) {
         if (teams >= 2 && teams % 2 != 0) {
             return true;
         } else {
             return false;
         }
     }
 
     private boolean validMinTeamsInCup(int teams) {
         return validMinTeamsInLeague(teams) && isPowerOfTwo(teams);
 
     }
 
     private boolean validMaxTeamsInLeague(int teams) {
         if (teams <= 20 && teams % 2 != 0) {
             return true;
         } else {
             return false;
         }
     }
 
     private boolean validMaxTeamsInCup(int teams) {
         if (teams <= 64 && isPowerOfTwo(teams)) {
             return true;
         } else {
             return false;
         }
     }
 
     private boolean isValidType(CompetitionType type) throws IncorrectTypeData {
         if (type == null) {
             throw new IncorrectTypeData();
         }
         if (type.equals(CompetitionType.CUP) || type.equals(CompetitionType.LEAGUE)) {
             return true;
         } else {
             return false;
         }
     }
 
     private void checkForExceptions(Competition comp) throws IncorrectCompetitionData,
             IncorrectTypeData, IncorrectMinNumberOfTeams, IncorrectMaxNumberOfTeams,
             IncorrectDate {
         DateTime date = new DateTime();
         date.plusWeeks(2);
         if (!isValidCategory(comp.getCategory())) {
             throw new IncorrectCompetitionData();
         }
         if (!isValidType(comp.getType())) {
             throw new IncorrectTypeData();
         }
         if (comp.getMinTeams() == 0) {
             throw new IncorrectMinNumberOfTeams();
         }
         if (comp.getMaxTeams() == 0) {
             throw new IncorrectMaxNumberOfTeams();
         }
         if (comp.getInici().after(date.toDate())) {
             throw new IncorrectDate();
         }
     }
 
     /**
      * Check if the period of inscription is valid for a competition.
      * @param competition: The competition to be checked.
      * @return Boolean indicate if it is open or not.
      */
     private boolean PeriodOpen(Competition competition) {
         //REIMPLEMENTAR. DONA PROBLEMES
         return true;
       /*  DateTime currentDate = new DateTime(DateTime.now());
         DateTime finishDate = new DateTime(competition.getInici());
 
         return ((currentDate.getDayOfYear() - finishDate.getDayOfYear()) < 7);
        */  
     }
 
     /**
      * Check if we have reached the maximum number of teams in a competition.
      * @param competition: The competition to be checked.
      * @return Boolean indicate if it is full or not.
      */
     private boolean TeamsSpace(Competition competition) {
         return (competition.getMaxTeams() > competition.getTeams().size());
     }
 
     /**
      * Check if competition exists in DAO
      * @param competitionDAO 
      * @param competition: Competition that we check.
      * @return Boolean indicate if the competition exist.
      */
     private boolean CompetitionExist(Competition competition) {
         return cDao.getCompetitionms().contains(competition);
     }
 
     private boolean diffCategCompetitionAndTeam(Competition competition, Team team) {
 
         return competition.getCategory().equals(team.getCategory());
     }
 
     
     /**
      * Check all the addTeam exceptions.
      * @param competetion The competition we want to register a team.
      * @param team The team we want to register.
      * @return 
      */
     private boolean checkforExceptions(Competition competition, Team team) {
  
         return  checkForTeamExceptions(team)
                 && diffCategCompetitionAndTeam(competition, team)
                 && CompetitionExist(competition)
                 && PeriodOpen(competition)
                 && TeamsSpace(competition);
     }
 
     /**
      *
      * @param team
      * @throws InscriptionTeamException
      * @throws Exception
      */
     public void addTeam(Team team) throws InscriptionTeamException, Exception {
         if(!checkForTeamExceptions(team)) throw new InscriptionTeamException();
         if(!clubExist(team.getClub())) throw new InscriptionTeamException();
         teamDao.addTeam(team);
     }
     
     private boolean checkForTeamExceptions(Team team){
         return  team != null 
                 && team.getName() != null
                 && team.getEmail() != null
                 && team.getClub() != null
                 && team.getCategory() != null;
     }
 
     private boolean clubExist(Club club) throws Exception {        
         return clubDao.getClubs().contains(club);
     }
 
     /**
      *
      * @param club
      * @throws InscriptionClubException
      * @throws Exception
      */
     public void addClub(Club club) throws InscriptionClubException, Exception {
         if(!checkForClubExceptions(club)) throw new InscriptionClubException();
         
         Club clubDB = clubDao.findClubByName(club.getName());
         if(clubDB != null) throw new InscriptionClubException();
         
         
         if(!club.getTeams().isEmpty() && anyTeamIsInDB(club.getTeams()))
             throw new InscriptionClubException();
         
         clubDao.addClub(club);  
        if(club.getTeams().size() >0) addAllTeams(club);     
     }
 
     private boolean checkForClubExceptions(Club club) {
         return  club != null 
                 && club.getName() != null
                 && club.getEmail() != null;        
     }
 
    private void addAllTeams(Club club) throws Exception {
        List<Team> teams = club.getTeams();
        int size = teams.size();
        
        for(int i=0; i<size;i++){  
            Team team = teams.get(i);
            team.setClub(club); 
             addTeam(team);
        }        
     }
 
     private boolean anyTeamIsInDB(List<Team> teams) throws PersistException{       
         boolean anyInDB = false;
         List<Team> teamsDB = teamDao.getTeams();
                 
         for(Team team : teams){
             if(teamsDB.contains(team)) anyInDB = true;
         }
         
         
         return anyInDB;
     }
     
 }
