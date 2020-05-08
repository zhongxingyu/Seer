 package com.nmt.nmj.editor.nls;
 
 import org.eclipse.osgi.util.NLS;
 
 public class InternationalizationMessages extends NLS {
 
    private static final String BUNDLE_NAME = "com.nmt.nmj.editor.i8n.internationalizationMessages"; //$NON-NLS-1$
 
     public static String movie_information_casting;
     public static String movie_information_casting_lowercase;
     public static String movie_information_certification;
     public static String movie_information_change_picture;
     public static String movie_information_director;
     public static String movie_information_directors;
     public static String movie_information_empty_value_not_allowed;
     public static String movie_information_filename;
     public static String movie_information_genre;
     public static String movie_information_genres;
     public static String movie_information_group_title;
     public static String movie_information_imdb;
     public static String movie_information_input;
     public static String movie_information_keyword_lowercase;
     public static String movie_information_keywords;
     public static String movie_information_movie_title;
     public static String movie_information_movie_title_click;
     public static String movie_information_new;
     public static String movie_information_new_movie_title;
     public static String movie_information_poster;
     public static String movie_information_release_date;
     public static String movie_information_save;
     public static String movie_information_search_title_text;
     public static String movie_information_synopsis;
     public static String movie_information_thumbnail;
     public static String movie_information_video_type;
     public static String movie_information_wallpaper;
 
     public static String common_movie;
     public static String common_movies;
     public static String common_tv_show;
     public static String common_tv_shows;
     public static String common_music;
     public static String common_error;
 
     public static String menu_open_database;
     public static String menu_refresh;
     public static String menu_close_database;
     public static String menu_exit_tooltip;
     public static String menu_list_view;
     public static String menu_wall_view;
     public static String menu_database;
     public static String menu_switch_view;
 
     public static String list_view_id;
     public static String list_view_movie_name;
     public static String list_view_release_date;
     public static String list_view_runtime;
     public static String list_view_rating;
     public static String list_view_system;
     public static String list_view_video_codec;
     public static String list_view_video_dimensions;
     public static String list_view_video_fps;
 
     static {
         // initialize resource bundle
         NLS.initializeMessages(BUNDLE_NAME, InternationalizationMessages.class);
     }
 
     private InternationalizationMessages() {
     }
 }
