 package com.nigorojr.typebest;
 
 import java.awt.Color;
 import java.awt.Font;
 import java.sql.SQLException;
 
 public class Preferences extends Database {
     private long id;
     private String username;
     private String keyboardLayout;
     private Color toBeTyped;
     private Color alreadyTyped;
     private Color backgroundColor;
     private Color missTypeColor;
     private Font font;
     // The number of digits to show after decimal point
     private int speedFractionDigit;
     private int timeFractionDigit;
 
     // Force shuffle for now
     // private boolean shuffled;
 
     // TODO: file or database?
     // private File settingsFile;
 
     final String defaultUsername = "Unknown Username";
 
    public static final String tableName = "PREFERENCES";

     /**
      * Attempts to retrieve a set of preference from the given ID. If the ID
      * does not match, it'll create a new set of preference with the default
      * username.
      * 
      * @param id
      *            The user ID that will be used to retrieve the preference set
      *            for that user.
      * @throws SQLException
      *             When it failed to establish connection with the database
      *             file.
      */
     public Preferences(long id) throws SQLException {
        super(tableName);
         if (!queryDatabase(id)) {
             createPreference(defaultUsername);
         }
     }
 
     /**
      * Creates a new set of preference using the given username. Note that the
      * String parameter is given only when a new set of preferences are to be
      * created. Also, the ID will be determined automatically by the database
      * manager.
      * 
      * @param username
      *            The username that the user chose.
      * @throws SQLException
      *             When it failed to establish a connection with the database
      *             file. Note that it has nothing to do with the table name.
      */
     public Preferences(String username) throws SQLException {
        super(tableName);
         createPreference(username);
     }
 
     /**
      * Queries the user preferences database to see if there are any preferences
      * for that user ID. Note that this method also updates the variables if
      * there is at least one match for that ID.
      * 
      * @param id
      *            The user ID that will be the identifier of the user.
      * @return True if there is at least one preference for the given ID, false
      *         if there is none.
      */
     private boolean queryDatabase(long id) {
         // TODO: Query part and update variables if there are existing prefs
         return false;
     }
 
     public void createPreference(String username) {
         init();
         // TODO: insert into prefs (username, blah blah) values foo bar;
     }
 
     /**
      * Initializes all the variables to the default value.
      */
     public void init() {
         keyboardLayout = "QWERTY";
         toBeTyped = Color.BLUE;
         alreadyTyped = Color.RED;
         backgroundColor = Color.GRAY;
         missTypeColor = Color.CYAN;
         font = new Font("Arial", Font.PLAIN, 30);
         // The number of digits to show after decimal point
         speedFractionDigit = 8;
         timeFractionDigit = 9;
     }
 }
