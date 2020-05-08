 /*
  * To change this template, choose Tools | Templates
  * and open the template in the editor.
  */
 package cdiddy.objects.league;
 
 /**
  *
  * @author cedric
  */
 public class YahooLeague 
 {
      private String league_key;
      private String league_id;
      private String name;
      private String url;
      private String draft_status;
      private String num_teams;
      private String edit_key;
      private String weekly_deadline;
      private String league_update_timestamp;
      private String scoring_type;
      private String league_type;
      private String is_pro_league;
      private String current_week;
      private String start_week;
      private String start_week_start_date;
      private String end_week;
      private YahooLeagueSettings settings;
 
     public String getLeague_key() {
         return league_key;
     }
 
     public void setLeague_key(String league_key) {
         this.league_key = league_key;
     }
 
     public String getLeague_id() {
         return league_id;
     }
 
     public void setLeague_id(String league_id) {
         this.league_id = league_id;
     }
 
     public String getName() {
         return name;
     }
 
     public void setName(String name) {
         this.name = name;
     }
 
     public String getUrl() {
         return url;
     }
 
     public void setUrl(String url) {
         this.url = url;
     }
 
     public String getDraft_status() {
         return draft_status;
     }
 
     public void setDraft_status(String draft_status) {
         this.draft_status = draft_status;
     }
 
     public String getNum_teams() {
         return num_teams;
     }
 
     public void setNum_teams(String num_teams) {
         this.num_teams = num_teams;
     }
 
     public String getEdit_key() {
         return edit_key;
     }
 
     public void setEdit_key(String edit_key) {
         this.edit_key = edit_key;
     }
 
     public String getWeekly_deadline() {
         return weekly_deadline;
     }
 
     public void setWeekly_deadline(String weekly_deadline) {
         this.weekly_deadline = weekly_deadline;
     }
 
     public String getLeague_update_timestamp() {
         return league_update_timestamp;
     }
 
     public void setLeague_update_timestamp(String league_update_timestamp) {
         this.league_update_timestamp = league_update_timestamp;
     }
 
     public String getScoring_type() {
         return scoring_type;
     }
 
     public void setScoring_type(String scoring_type) {
         this.scoring_type = scoring_type;
     }
 
     public String getLeague_type() {
         return league_type;
     }
 
     public void setLeague_type(String league_type) {
         this.league_type = league_type;
     }
 
     public String getIs_pro_league() {
         return is_pro_league;
     }
 
     public void setIs_pro_league(String is_pro_league) {
         this.is_pro_league = is_pro_league;
     }
 
     public String getCurrent_week() {
         return current_week;
     }
 
     public void setCurrent_week(String current_week) {
         this.current_week = current_week;
     }
 
     public String getStart_week() {
         return start_week;
     }
 
     public void setStart_week(String start_week) {
         this.start_week = start_week;
     }
 
     public String getStart_week_start_date() {
         return start_week_start_date;
     }
 
     public void setStart_week_start_date(String start_week_start_date) {
         this.start_week_start_date = start_week_start_date;
     }
 
     public String getEnd_week() {
         return end_week;
     }
 
     public void setEnd_week(String end_week) {
         this.end_week = end_week;
     }
 
     public YahooLeagueSettings getSettings() {
         return settings;
     }
 
     public void setSettings(YahooLeagueSettings settings) {
         this.settings = settings;
     }
      
     @Override
     public String toString()
     {
         return name + "   " + league_id;
     }
      
 }
