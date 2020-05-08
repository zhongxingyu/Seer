 /*
  * This file is part of TAberystwyth, a debating competition organiser
  * Copyright (C) 2010, Roberto Sarrionandia and Cal Paterson
  * 
  * This program is free software: you can redistribute it and/or modify it
  * under the terms of the GNU General Public License as published by the Free
  * Software Foundation, either version 3 of the License, or (at your option)
  * any later version.
  * 
  * This program is distributed in the hope that it will be useful, but WITHOUT
  * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or
  * FITNESS FOR A PARTICULAR PURPOSE. See the GNU General Public License for
  * more details.
  * 
  * You should have received a copy of the GNU General Public License along with
  * this program. If not, see <http://www.gnu.org/licenses/>.
  */
 
 package taberystwyth.db;
 
 import java.sql.Connection;
 import java.sql.PreparedStatement;
 import java.sql.SQLException;
 import java.util.Random;
 import org.h2.jdbcx.JdbcConnectionPool;
 
 import org.apache.log4j.Logger;
 
 /**
  * The Class Generator.
  */
 final public class Generator {
     
     /** The Constant log. */
     private final static Logger LOG = Logger.getLogger(Generator.class);
     
     /** The instance of SQLConnection. */
     private Connection conn;
     
     /** The instance. */
     private static Generator instance = new Generator();
     
     /**
      * Gets the single instance of Generator.
      * 
      * @return single instance of Generator
      */
     public static Generator getInstance() {
         return instance;
     }
     
     /** The random generator that will be used throughout. */
     private Random gen = new Random();
     
     /** The Constant N_TEAMS. */
     public static final int N_TEAMS = 32;
     
     /** The Constant N_LOCATIONS. */
     public static final int N_LOCATIONS = 8;
     
     /** The Constant N_JUDGES. */
     public static final int N_JUDGES = 30;
     
     /**
      * Instantiates a new random tab generator.
      * 
      * @throws SQLException
      */
     private Generator() {
         try {
             conn = TabServer.getConnectionPool().getConnection();
         } catch (SQLException e) {
             LOG.error("Unable to get connection", e);
         }
     }
     
     /**
      * Gen judge.
      * 
      * @return the string
      */
     @SuppressWarnings("null")
     // This is for the variable conn only
     public String genJudge() {
         while (true) {
             try {
                 String name = genName();
                 
                 PreparedStatement p = conn
                        .prepareStatement("insert into judges (name, institution, rating) values (?,?,?);");
                 p.setString(1, name);
                 p.setString(2, institutions[gen.nextInt(institutions.length)]);
                 p.setInt(3, 50);
                 p.execute();
                 p.close();
                 
                 conn.commit();
                 LOG.debug("Judge generated: " + name);
                 return name;
             } catch (SQLException e) {
                 try {
                     LOG.error("SQL Exception", e);
                     conn.rollback();
                 } catch (Exception e1) {
                     LOG.error("Exception while rolling back!", e1);
                 }
             }
         }
     }
     
     /**
      * Gen locations.
      */
     @SuppressWarnings("null")
     // This is for the variable conn only
     public void genLocation() {
         while (true) {
             try {
                String s = "insert into locations (name, rating) values (?, ?);";
                 String location = locations[gen.nextInt(locations.length)];
                 PreparedStatement p = conn.prepareStatement(s);
                 p.setString(1, location);
                 p.setInt(2, 50);
                 p.execute();
                 p.close();
                 LOG.info("Location generated: " + location);
                 conn.commit();
                 return;
             } catch (SQLException e) {
                 try {
                     LOG.error("SQL Exception", e);
                     conn.rollback();
                 } catch (Exception e1) {
                     LOG.error("Exception while trying to roll back", e1);
                     e1.printStackTrace();
                 }
             }
         }
     }
     
     /**
      * Generate a random team.
      * 
      * @return the name of the team
      */
     @SuppressWarnings("null")
     // This is for the variable conn only
     public String genTeam() {
         while (true) {
             try {
                 String speaker1;
                 String speaker2;
                 String name;
                 speaker1 = genSpeaker();
                 speaker2 = genSpeaker();
                 
                String s = "insert into teams (name, speaker1, speaker2) values(?, ?, ?);";
                 PreparedStatement p = conn.prepareStatement(s);
                 name = speaker1 + " and " + speaker2;
                 
                 p.setString(1, name);
                 p.setString(2, speaker1);
                 p.setString(3, speaker2);
                 
                 p.execute();
                 p.close();
                 
                 conn.commit();
                 LOG.debug("Team generated: " + name);
                 return name;
             } catch (SQLException e) {
                 try {
                     LOG.error("SQL Exception", e);
                     conn.rollback();
                 } catch (Exception e1) {
                     LOG.error("Exception while trying to roll back", e1);
                 }
             }
         }
     }
     
     /**
      * Generate a speaker.
      * 
      * @return the name of the speaker
      * @throws SQLException
      *             the SQL exception
      */
     private String genSpeaker() throws SQLException {
         String name = genName();
         String institution = institutions[gen.nextInt(institutions.length)];
        String s = "insert into speakers (name, institution) values(?,?);";
         PreparedStatement p = conn.prepareStatement(s);
         
         p.setString(1, name);
         p.setString(2, institution);
         
         p.execute();
         p.close();
         conn.commit();
         return name;
     }
     
     /**
      * Generate a random name.
      * 
      * @return the string
      */
     private String genName() {
         return firstNames[gen.nextInt(firstNames.length)] + " "
                 + surnames[gen.nextInt(surnames.length)];
     }
     
     /** The institutions. */
     private String[] institutions = {
             "University of Aberdeen",
             "University of Abertay Dundee",
             "Aberystwyth University",
             "Anglia Ruskin University",
             "University of the Arts London",
             "Aston University, Birmingham",
             "Bangor University",
             "University of Bath",
             "Bath Spa University",
             "University of Bedfordshire",
             "University of Birmingham",
             "Birmingham City University",
             "University Centre At Blackburn College, Blackburn",
             "University of Bolton",
             "Bournemouth University",
             "University of Bradford",
             "University of Brighton",
             "University of Bristol",
             "Brunel University",
             "University of Buckingham",
             "Buckinghamshire New University, High Wycombe",
             "University of Cambridge",
             "Canterbury Christ Church University",
             "Cardiff University",
             "University of Central Lancashire, Preston",
             "University of Chester, Chester and Warrington",
             "University of Chichester",
             "City University London",
             "Coventry University",
             "Cranfield University",
             "University of Cumbria",
             "De Montfort University, Leicester",
             "University of Derby",
             "University of Dundee",
             "Durham University",
             "University of East Anglia, Norwich",
             "University of East London",
             "Edge Hill University, Ormskirk",
             "University of Edinburgh",
             "Edinburgh Napier University",
             "University of Essex",
             "University of Exeter",
             "University of Glamorgan",
             "University of Glasgow",
             "Glasgow Caledonian University",
             "University of Gloucestershire",
             "University of Greenwich",
             "Glyndwr University, Wrexham",
             "Heriot-Watt University",
             "University of Hertfordshire",
             "University of Huddersfield",
             "University of Hull",
             "Hull York Medical School (HYMS)",
             "Imperial College London",
             "Keele University, Staffordshire",
             "University of Kent, Canterbury and Medway",
             "Kingston University",
             "Lancaster University",
             "University of Leeds",
             "Leeds Metropolitan University",
             "University of Leicester",
             "University of Lincoln",
             "University of Liverpool",
             "Liverpool Hope University",
             "Liverpool John Moores University",
             "University of London",
             "London Metropolitan University",
             "London South Bank University",
             "Loughborough University",
             "University of Manchester",
             "Manchester Metropolitan University",
             "Middlesex University, London",
             "Newcastle University",
             "University of Northampton",
             "Northumbria University, Newcastle upon Tyne",
             "University of Nottingham",
             "Nottingham Trent University",
             "The Open University",
             "University of Oxford",
             "Oxford Brookes University",
             "Peninsula College of Medicine and Dentistry",
             "University of Plymouth",
             "University of Portsmouth",
             "Queen's University Belfast",
             "Queen Margaret University, Edinburgh",
             "University of Reading",
             "The Robert Gordon University, Aberdeen",
             "Roehampton University, London",
             "Royal College of Art, London",
             "University of St Andrews",
             "University of Salford",
             "University of Sheffield",
             "Sheffield Hallam University",
             "University of Southampton",
             "Southampton Solent University",
             "Staffordshire University",
             "University of Stirling",
             "University of Strathclyde",
             "University of Sunderland",
             "University of Surrey, Guildford",
             "University of Sussex, Falmer and Brighton",
             "Swansea Metropolitan University",
             "Swansea University",
             "University of Teesside, Middlesbrough",
             "Thames Valley University",
             "University of Ulster",
             "University of Wales",
             "University of Wales Institute, Cardiff (UWIC)",
             "University of Wales, Newport",
             "University of Wales, Trinity Saint David",
             "University of Warwick, Coventry",
             "University of Westminster, London",
             "University of the West of England, Bristol",
             "University of the West of Scotland, Ayr, Hamilton, Dumfries and Paisley",
             "University of Winchester", "University of Wolverhampton",
             "University of Worcester", "University of York",
             "York St John University" };
     
     /** The first names. */
     private String[] firstNames = { "Abigail", "Alexander", "Amelia", "Amy",
             "Benjamin", "Callum", "Charlie", "Charlotte", "Chloe", "Daniel",
             "Ella", "Ellie", "Emily", "Emma", "Ethan", "George", "Grace",
             "Hannah", "Harry", "Jack", "James", "Jessica", "Joseph", "Joshua",
             "Katie", "Lewis", "Lily", "Lucy", "Luke", "Matthew", "Megan",
             "Mia", "Mohammed", "Molly", "Oliver", "Olivia", "Samuel",
             "Sophie", "Thomas", "William" };
     
     /** The locations. */
     private String[] locations = {
             "Bath",
             "Blaenavon Industrial Landscape",
             "Blenheim Palace",
             "Canterbury Cathedral, St. Augustines Abbey and St. Martins Church",
             "Castles and Town Walls of King Edward I in Gwynedd",
             "Cornwall and West Devon Mining Landscape",
             "Derwent Valley Mills", "Durham Castle and Cathedral",
             "Edinburgh Old Town and New Town",
             "Frontiers of the Roman Empire (Antonine Wall)",
             "Frontiers of the Roman Empire (Hadrians Wall)",
             "Giants Causeway", "Heart of Neolithic Orkney",
             "Ironbridge Gorge", "Jurassic Coast", "Kew Gardens",
             "Liverpool Maritime Mercantile City", "Maritime Greenwich",
             "New Lanark", "Pontcysyllte Aqueduct", "Saltaire", "St. Kilda",
             "St. Margarets Church",
             "Stonehenge, Avebury and Associated Sites",
             "Studley Royal Park and Fountains Abbey", "Tower of London",
             "Westminster Palace, Westminster Abbey" };
     
     String[] surnames = { "Jones", "Williams", "Davies", "Evans", "Thomas",
             "Roberts", "Lewis", "Hughes", "Morgan", "Griffiths", "Edwards",
             "Smith", "James", "Rees", "Jenkins", "Owen", "Price", "Phillips",
             "Morris", "Richards" };
     
 }
