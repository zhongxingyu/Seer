 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package entitysuggester;
 
 import java.io.File;
 import java.io.IOException;
 import java.sql.*;
 import java.util.List;
 import java.util.logging.Level;
 import java.util.logging.Logger;
 import javax.swing.text.html.HTMLDocument;
 import net.myrrix.client.ClientRecommender;
 import net.myrrix.client.MyrrixClientConfiguration;
 import net.myrrix.client.translating.TranslatedRecommendedItem;
 import net.myrrix.client.translating.TranslatingClientRecommender;
 import net.myrrix.common.MyrrixRecommender;
 import net.myrrix.online.RescorerProvider;
 import org.apache.commons.cli.*;
 import org.apache.mahout.cf.taste.common.TasteException;
 import org.apache.mahout.cf.taste.recommender.IDRescorer;
 import org.apache.mahout.cf.taste.recommender.Rescorer;
 import org.apache.mahout.common.LongPair;
 
 /**
  *
  * @author nilesh
  */
 public class EntitySuggester {
 
     /**
      * @param args the command line arguments
      */
     public static void main(String[] args) {
         if (args.length == 0) {
             printHelp();
         }
 
         Options options1 = new Options();
         options1.addOption(OptionBuilder.hasArg().withArgName("port").withDescription("Myrrix serving layer port").withLongOpt("port").create('p'));
         options1.addOption(OptionBuilder.hasArg().withArgName("hostname/IP").withDescription("Myrrix serving layer host").withLongOpt("host").create('h'));
         options1.addOption(OptionBuilder.hasArg().withArgName("CSV file name").withDescription("Ingest CSV file").withLongOpt("ingest").create('i'));
 
         CommandLine cmd;
         try {
             cmd = new BasicParser().parse(options1, args);
 
             MyrrixClientConfiguration config = new MyrrixClientConfiguration();
 
             if (cmd.hasOption('p')) {
                 config.setPort(Integer.parseInt(cmd.getOptionValue('p')));
             }
             if (cmd.hasOption('h')) {
                 config.setHost(cmd.getOptionValue('h'));
             }
             if (cmd.hasOption('i')) {
                 ingest(config, cmd.getOptionValue('i'));
             } else {
                 Options options2 = new Options();
                 options2.addOption(OptionBuilder.hasArgs(2).isRequired().withArgName("item ID").withArgName("property|value").withDescription("Recommend properties/values for item with given id. Type of recommendation can be either 'property' or 'value'").withLongOpt("recommend").create('r'));
                 options2.addOption(OptionBuilder.hasArg().isRequired().withArgName("property list file").withDescription("File with list of properties and property:value pairs").withLongOpt("property-list").create('l'));
                 options2.addOption(OptionBuilder.hasArg().withArgName("how many").withDescription("Number of recommendations to fetch").withLongOpt("count").create('c'));
                 options2.addOption(OptionBuilder.hasArg().isRequired().withArgName("MySQL Database Host").withLongOpt("dbhost").create());
                 options2.addOption(OptionBuilder.hasArg().isRequired().withArgName("MySQL Database Name").withLongOpt("dbname").create());
                 options2.addOption(OptionBuilder.hasArg().isRequired().withArgName("MySQL Database user").withLongOpt("dbuser").create());
                 options2.addOption(OptionBuilder.hasArg().isRequired().withArgName("MySQL Database Password").withLongOpt("dbpass").create());
                 cmd = new BasicParser().parse(options2, args);
 
                 String recommendTo = cmd.getOptionValues('r')[0];
                 String recommendType = cmd.getOptionValues('r')[1];
                 String idListFile = cmd.getOptionValue('l');
                 int howMany;
                 if (cmd.hasOption('c')) {
                     howMany = Integer.parseInt(cmd.getOptionValue('c'));
                 } else {
                     howMany = 10;
                 }
 
                 recommend(config, idListFile, recommendTo, recommendType, howMany, new String[]{cmd.getOptionValue("dbhost"), cmd.getOptionValue("dbname"), cmd.getOptionValue("dbuser"), cmd.getOptionValue("dbpass")});
             }
         } catch (MissingArgumentException ex) {
             Option missingOption = ex.getOption();
             System.out.println("Argument missing for : " + missingOption.getLongOpt());
         } catch (ParseException ex) {
             System.out.println("Error parsing arguments. Aborting...");
         }
     }
 
     private static void ingest(MyrrixClientConfiguration config, String csvFile) {
         try {
             ClientRecommender clientRecommender = new ClientRecommender(config);
             TranslatingClientRecommender tcr = new TranslatingClientRecommender(clientRecommender);
             tcr.ingest(new File(csvFile));
             System.out.println("Ingest successfully completed!");
         } catch (TasteException | IOException ex) {
             Logger.getLogger(EntitySuggester.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private static void recommend(MyrrixClientConfiguration config, String recommendTo, String idListFile, String recommendType, int howMany, String[] databaseInfo) {
         try {
             ClientRecommender clientRecommender = new ClientRecommender(config);
             TranslatingClientRecommender tcr = new TranslatingClientRecommender(clientRecommender);
             tcr.addItemIDs(new File(idListFile));
             List<TranslatedRecommendedItem> recommendations = tcr.recommend(recommendTo, howMany, false, new String[]{recommendType});
             if (!recommendations.isEmpty()) {
                 writeResults(recommendations, recommendType, databaseInfo[0], databaseInfo[1], databaseInfo[2], databaseInfo[3]);
             } else {
                 System.out.println("No suggestions available");
             }
         } catch (TasteException | IOException ex) {
             Logger.getLogger(EntitySuggester.class.getName()).log(Level.SEVERE, null, ex);
         }
     }
 
     private static void writeResults(List<TranslatedRecommendedItem> recommendations, String recommendType, String dbHost, String dbName, String dbUser, String dbPassword) {
         Connection connect = null;
         Statement statement = null;
         ResultSet resultSet = null;
 
         try {
             Class.forName("com.mysql.jdbc.Driver");
             connect = DriverManager.getConnection("jdbc:mysql://"
                     + dbHost + "/" + dbName + "?"
                     + "user=" + dbUser + "&password=" + dbPassword);
 
             if (recommendType.equals("property")) {
                 System.out.println("Suggested properties:");
                 statement = connect.createStatement();
                 String query = "SELECT pd_id, pd_text FROM pdescr WHERE pd_lang='en' AND p_id IN (";
                 for (TranslatedRecommendedItem recommendation : recommendations) {
                     query += recommendation.getItemID() + ",";
                 }
                 query = query.substring(0, query.length() - 1) + ")";
 
                 resultSet = statement.executeQuery(query);
                 while (resultSet.next()) {
                     Long id = resultSet.getLong("pd_id");
                     String text = resultSet.getString("pd_text");
                     System.out.println(id + " => " + text);
                 }
             } else {
                 System.out.println("Suggested property:value pairs:");
                 for (TranslatedRecommendedItem recommendation : recommendations) {
                     System.out.println(recommendation.getItemID());
                 }
             }
 
         } catch (Exception e) {
             e.printStackTrace();
         } finally {
             try {
                 connect.close();
                 statement.close();
                 resultSet.close();
             } catch (SQLException ex) {
                 Logger.getLogger(EntitySuggester.class.getName()).log(Level.SEVERE, null, ex);
             }
         }
     }
 
     private static void printHelp() {
         Options options = new Options();
         options.addOption(OptionBuilder.hasArg().withArgName("port").withDescription("Myrrix serving layer port").withLongOpt("port").create('p'));
         options.addOption(OptionBuilder.hasArg().withArgName("hostname/IP").withDescription("Myrrix serving layer host").withLongOpt("host").create('h'));
         options.addOption(OptionBuilder.hasArg().withArgName("CSV file name").withDescription("Ingest CSV file").withLongOpt("ingest").create('i'));
        options.addOption(OptionBuilder.hasArgs(2).isRequired().withArgName("item ID").withArgName("itemID> <property|value").withDescription("Recommend properties/values for item with given id. Type of recommendation can be either 'property' or 'value'").withLongOpt("recommend").create('r'));
         options.addOption(OptionBuilder.hasArg().isRequired().withArgName("property list file").withDescription("File with list of properties and property:value pairs").withLongOpt("property-list").create('l'));
         options.addOption(OptionBuilder.hasArg().withArgName("how many").withDescription("Number of recommendations to fetch").withLongOpt("count").create('c'));
         options.addOption(OptionBuilder.hasArg().isRequired().withArgName("MySQL Database Host").withLongOpt("dbhost").create());
         options.addOption(OptionBuilder.hasArg().isRequired().withArgName("MySQL Database Name").withLongOpt("dbname").create());
         options.addOption(OptionBuilder.hasArg().isRequired().withArgName("MySQL Database user").withLongOpt("dbuser").create());
         options.addOption(OptionBuilder.hasArg().isRequired().withArgName("MySQL Database Password").withLongOpt("dbpass").create());
         
         HelpFormatter helpFormatter = new HelpFormatter();
         helpFormatter.printHelp("java -jar entity-suggester.jar", options);
     }
 }
