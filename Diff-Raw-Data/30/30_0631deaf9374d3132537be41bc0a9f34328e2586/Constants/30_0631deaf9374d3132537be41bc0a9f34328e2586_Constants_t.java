 package com.bookspicker.shared;
 
 public class Constants {
 
    // Matches both 10-digit and 13-digit isbns
    public static final String ISBN_REGEX_STR =
            "^(\\d{12}|\\d{9})(\\d|X)$";

    public static final String CLASS_REGEX_STR =
            "^(\\d{1,2}\\w?|\\w{2,3})\\.(\\d{1,3}\\w?|\\w{2,3})$";

    public static final String DB_ID_STR = "^\\w+_\\d+$";
 }
