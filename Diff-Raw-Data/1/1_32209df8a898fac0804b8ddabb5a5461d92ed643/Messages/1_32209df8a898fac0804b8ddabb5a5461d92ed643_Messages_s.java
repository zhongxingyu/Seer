 package fi.valonia.pyorallatoihin;
 
 import java.io.Serializable;
 import java.util.Locale;
 import java.util.MissingResourceException;
 import java.util.ResourceBundle;
 
 public class Messages implements Serializable {
     private static final long serialVersionUID = -3651500289758153839L;
 
     public static final String foo = "foo";
 
     public static final String header_topic = "header_topic";
     public static final String log_in = "log_in";
     public static final String log_out = "log_out";
     public static final String log_in_with_token = "log_in_with_token";
     public static final String register_workplace_into_competition = "register_workplace_into_competition";
     public static final String company_not_found = "company_not_found";
     public static final String choose_language = "choose_language";
     public static final String max_five_days_to_stats = "max_five_days_to_stats";
     public static final String name = "name";
     public static final String type = "type";
     public static final String km = "km";
     public static final String km_roundtrip = "km_roundtrip";
     public static final String km_total = "km_total";
     public static final String add_your_name = "add_your_name";
     public static final String or = "or";
     public static final String click_your_name = "click_your_name";
     public static final String mark_in_table = "mark_in_table";
     public static final String add = "add";
     public static final String save = "save";
     public static final String press_here_to_mark_today = "press_here_to_mark_today";
     public static final String you_came_today_without_a_motor = "you_came_today_without_a_motor";
     public static final String hi = "hi";
     public static final String remember_to_bookmark = "remember_to_bookmark";
     public static final String company_name_can_not_be_empty = "company_name_can_not_be_empty";
     public static final String company_size_numeric = "company_size_numeric";
     public static final String contact_person_info_can_not_be_empty = "contact_person_info_can_not_be_empty";
     public static final String company_name_already_exists = "company_name_already_exists";
     public static final String company_info = "company_info";
     public static final String company_name = "company_name";
     public static final String company_size = "company_size";
     public static final String company_address = "company_address";
     public static final String company_street = "company_street";
     public static final String company_zip = "company_zip";
     public static final String company_city = "company_city";
     public static final String competition_info = "competition_info";
     public static final String contact_person_valonia = "contact_person_valonia";
     public static final String contact_person_info = "contact_person_info";
     public static final String contact_person_name = "contact_person_name";
     public static final String contact_person_email = "contact_person_email";
     public static final String contact_person_phone = "contact_person_phone";
     public static final String contact_person_fax = "contact_person_fax";
     public static final String heard_from = "heard_from";
     public static final String first_time = "first_time";
     public static final String register = "register";
     public static final String feedback = "feedback";
     public static final String implementation = "implementation";
 
     public static final String company_stats_employees = "company_stats_employees";
     public static final String company_stats_participants = "company_stats_participants";
     public static final String company_stats_markers = "company_stats_markers";
     public static final String company_stats_km = "company_stats_km";
     public static final String company_stats_times_per_employee = "company_stats_times_per_employee";
 
     public static final String company_registed_to_competition = "company_registed_to_competition";
     public static final String company_share_token_start = "company_share_token_start";
     public static final String company_share_token_end = "company_share_token_end";
 
     public static final String competition_company_position = "competition_company_position";
     public static final String competition_company_name = "competition_company_name";
     public static final String competition_company_workers = "competition_company_workers";
     public static final String competition_company_participants = "competition_company_participants";
     public static final String competition_company_markers = "competition_company_markers";
     public static final String competition_company_ratio = "competition_company_ratio";
 
     public static final String heard_from_email_internal = "heard_from_email_internal";
     public static final String heard_from_email_external = "heard_from_email_external";
     public static final String heard_from_letter = "heard_from_letter";
     public static final String heard_from_internet = "heard_from_internet";
     public static final String heard_from_paper = "heard_from_paper";
     public static final String heard_from_radio = "heard_from_radio";
     public static final String heard_from_library = "heard_from_library";
     public static final String heard_from_sports_center = "heard_from_sports_center";
     public static final String heard_from_other = "heard_from_other";
 
     public static final String BICYCLE = "BICYCLE";
     public static final String WALKING = "WALKING";
     public static final String KICK_SCOOTER = "KICK_SCOOTER";
     public static final String ROLLER_BLADES = "ROLLER_BLADES";
     public static final String ROWING = "ROWING";
     public static final String WITH_HORSE = "WITH_HORSE";
     public static final String WITH_DOG_SLED = "WITH_DOG_SLED";
     public static final String OTHER = "OTHER";
 
     ResourceBundle rb;
 
     public Messages(Locale locale) {
         rb = ResourceBundle.getBundle("Messages", locale);
     }
 
     public String getString(String key) {
         try {
             return rb.getString(key);
         } catch (MissingResourceException e) {
             return '!' + key + '!';
         }
     }
 }
